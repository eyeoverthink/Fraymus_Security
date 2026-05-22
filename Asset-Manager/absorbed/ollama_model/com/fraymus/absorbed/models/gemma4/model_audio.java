package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class model_audio {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class AudioModel {
        public *AudioConvBlock SSCPConv0;
        public *AudioConvBlock SSCPConv1;
        public *nn.Linear SSCPInputProj;
        public []AudioConformerBlock Layers;
        public *AudioOutputProj OutputProj;
    }

    public static class AudioOutputProj {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }

    public static class AudioModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int headDim;
        public int ffnSize;
        public int numLayers;
        public int melBins;
        public int chunkSize;
        public int maxPast;
        public int maxFuture;
        public int contextSize;
        public float32 logitCap;
        public float32 residualWeight;
        public float32 gradClip;
        public int convKernelSize;
        public float32 eps;
    }

    public static class AudioConvBlock {
        public ml.Tensor Weight;
        public *nn.LayerNorm Norm;
    }

    public static class AudioConformerBlock {
        public *nn.RMSNorm Norm;
        public *nn.RMSNorm FFWNorm;
        public *AudioClippableLinear FFWUp;
        public *AudioClippableLinear FFWDown;
        public *nn.RMSNorm FFWPostNorm;
        public *nn.RMSNorm FFWNorm1;
        public *AudioClippableLinear FFWUp1;
        public *AudioClippableLinear FFWDown1;
        public *nn.RMSNorm FFWPostNorm1;
        public *AudioClippableLinear AttnQ;
        public *AudioClippableLinear AttnK;
        public *AudioClippableLinear AttnV;
        public *AudioClippableLinear AttnOut;
        public *nn.RMSNorm AttnPreNorm;
        public *nn.RMSNorm AttnPostNorm;
        public ml.Tensor LinearPos;
        public ml.Tensor PerDimScale;
        public *AudioClippableLinear ConvPW1;
        public *AudioClippableLinear ConvPW2;
        public ml.Tensor ConvDW;
        public *nn.RMSNorm ConvNorm;
        public *nn.RMSNorm NormConv;
    }

    public static class AudioClippableLinear {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
        public ml.Tensor InputMin;
        public ml.Tensor InputMax;
        public ml.Tensor OutputMin;
        public ml.Tensor OutputMax;
        public inMax, inMin,;
        public boolean clampsLoaded;
    }
        func (l *AudioClippableLinear) loadClamps() {
        if l.clampsLoaded {
        return;
    }
        l.clampsLoaded = true;
        if l.InputMin != null {
        var vals = l.InputMin.BackendGet();
        if len(vals) > 0 {
        l.inMin = vals[0];
    }
    }
        if l.InputMax != null {
        var vals = l.InputMax.BackendGet();
        if len(vals) > 0 {
        l.inMax = vals[0];
    }
    }
        if l.OutputMin != null {
        var vals = l.OutputMin.BackendGet();
        if len(vals) > 0 {
        l.outMin = vals[0];
    }
    }
        if l.OutputMax != null {
        var vals = l.OutputMax.BackendGet();
        if len(vals) > 0 {
        l.outMax = vals[0];
    }
    }
    }
        func (l *AudioClippableLinear) Forward(ctx ml.Context, x ml.Tensor) ml.Tensor {
        l.loadClamps();
        if l.inMax != 0 {
        x = x.Clamp(ctx, l.inMin, l.inMax);
    }
        var out = l.Weight.Mulmat(ctx, x);
        if l.Bias != null {
        out = out.Add(ctx, l.Bias);
    }
        if l.outMax != 0 {
        out = out.Clamp(ctx, l.outMin, l.outMax);
    }
        return out;
    }

    public static class AudioMultimodalProjector {
        public *AudioClippableLinear Projection;
        public *AudioFC FC;
    }

    public static class AudioFC {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (p *AudioMultimodalProjector) Forward(ctx ml.Context, x ml.Tensor, eps float32) ml.Tensor {
        x = p.FC.Weight.Mulmat(ctx, x);
        if p.FC.Bias != null {
        x = x.Add(ctx, p.FC.Bias);
    }
        x = x.RMSNorm(ctx, null, eps);
        x = p.Projection.Forward(ctx, x);
        return x;
    }
        func (m *AudioModel) ForwardAudio(ctx ml.Context, melFeatures ml.Tensor, proj *AudioMultimodalProjector, opts *AudioModelOptions) ml.Tensor {
        var x = melFeatures.Reshape(ctx, melFeatures.Dim(0), melFeatures.Dim(1), 1, 1);
        x = forwardConvBlock(ctx, m.SSCPConv0, x, opts);
        x = forwardConvBlock(ctx, m.SSCPConv1, x, opts);
        var fOut = x.Dim(0);
        var tOut = x.Dim(1);
        var cOut = x.Dim(2);
        x = x.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        x = x.Reshape(ctx, cOut*fOut, tOut);
        x = m.SSCPInputProj.Forward(ctx, x);
        var causalMask = buildCausalValidMaskF32(opts.chunkSize, opts.maxPast, opts.maxFuture);
        var for i = range m.Layers {
        x = m.Layers[i].Forward(ctx, x, causalMask, opts, i);
    }
        if m.OutputProj != null {
        x = m.OutputProj.Weight.Mulmat(ctx, x);
        if m.OutputProj.Bias != null {
        x = x.Add(ctx, m.OutputProj.Bias);
    }
    }
        if proj != null {
        x = proj.Forward(ctx, x, opts.eps);
    }
        return x;
    }
        func forwardConvBlock(ctx ml.Context, block *AudioConvBlock, x ml.Tensor, opts *AudioModelOptions) ml.Tensor {
        var weight = block.Weight.Contiguous(ctx);
        x = weight.Conv2D(ctx, x, 2, 2, 1, 1, 1, 1);
        x = x.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        x = block.Norm.Forward(ctx, x, opts.eps);
        x = x.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        x = x.RELU(ctx);
        return x;
    }
        func (cb *AudioConformerBlock) Forward(ctx ml.Context, x ml.Tensor, causalMask []float32, opts *AudioModelOptions, blockIdx int) ml.Tensor {
        x = cb.forwardFFW(ctx, cb.FFWNorm, cb.FFWUp, cb.FFWDown, cb.FFWPostNorm, x, opts);
        x = cb.forwardAttention(ctx, x, causalMask, opts, blockIdx);
        x = cb.forwardLightConv(ctx, x, opts, blockIdx);
        x = cb.forwardFFW(ctx, cb.FFWNorm1, cb.FFWUp1, cb.FFWDown1, cb.FFWPostNorm1, x, opts);
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = cb.Norm.Forward(ctx, x, opts.eps);
        return x;
    }
        func (cb *AudioConformerBlock) forwardFFW(ctx ml.Context, preNorm *nn.RMSNorm, up, down *AudioClippableLinear, postNorm *nn.RMSNorm, x ml.Tensor, opts *AudioModelOptions) ml.Tensor {
        var residual = x;
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = preNorm.Forward(ctx, x, opts.eps);
        x = up.Forward(ctx, x);
        x = x.SILU(ctx);
        x = down.Forward(ctx, x);
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = postNorm.Forward(ctx, x, opts.eps);
        x = x.Scale(ctx, double(opts.residualWeight));
        return residual.Add(ctx, x);
    }
        func (cb *AudioConformerBlock) forwardAttention(ctx ml.Context, x ml.Tensor, causalMask []float32, opts *AudioModelOptions, blockIdx int) ml.Tensor {
        var residual = x;
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = cb.AttnPreNorm.Forward(ctx, x, opts.eps);
        var hiddenSize = x.Dim(0);
        var seqLen = x.Dim(1);
        var q = cb.AttnQ.Forward(ctx, x).Reshape(ctx, opts.headDim, opts.numHeads, seqLen);
        var k = cb.AttnK.Forward(ctx, x).Reshape(ctx, opts.headDim, opts.numHeads, seqLen);
        var v = cb.AttnV.Forward(ctx, x).Reshape(ctx, opts.headDim, opts.numHeads, seqLen);
        var qScale = double(math.Pow(double(opts.headDim), -0.5)) / math.Log(2);
        q = q.Scale(ctx, qScale);
        if cb.PerDimScale != null {
        q = q.Mul(ctx, cb.PerDimScale);
    }
        var kScale = math.Log(1+math.E) / math.Log(2);
        k = k.Scale(ctx, kScale);
        var maxSpan = opts.maxPast + opts.maxFuture + 1 // 13 unique relative positions;
        var posEmb = cb.buildPositionEmbeddings(ctx, maxSpan, opts);
        var chunkSize = opts.chunkSize;
        var numChunks = (seqLen + chunkSize - 1) / chunkSize;
        var contextSize = opts.contextSize;
        var padT = numChunks*chunkSize - seqLen;
        if padT > 0 {
        q = q.Pad(ctx, 0, 0, padT, 0);
        k = k.Pad(ctx, 0, 0, padT, 0);
        v = v.Pad(ctx, 0, 0, padT, 0);
    }
        var paddedLen = numChunks * chunkSize;
        var padLeft = opts.maxPast;
        var padRight = opts.maxFuture + chunkSize - 1;
        var zeroLeft = ctx.Input().FromFloats(make([]float32, opts.headDim*opts.numHeads*padLeft), opts.headDim, opts.numHeads, padLeft);
        var zeroRight = ctx.Input().FromFloats(make([]float32, opts.headDim*opts.numHeads*padRight), opts.headDim, opts.numHeads, padRight);
        var kPadded = zeroLeft.Concat(ctx, k, 2).Concat(ctx, zeroRight, 2);
        var vPadded = zeroLeft.Concat(ctx, v, 2).Concat(ctx, zeroRight, 2);
        var qChunked = q.Reshape(ctx, opts.headDim, opts.numHeads, numChunks, chunkSize);
        var chunkOutputs = make([]ml.Tensor, numChunks);
        var for u = range numChunks {
        var qBlock = qChunked.Slice(ctx, 2, u, u+1, 1).Reshape(ctx, opts.headDim, opts.numHeads, chunkSize);
        var cStart = u * chunkSize // offset in kPadded (padLeft already accounts for left context);
        var kCtx = kPadded.Slice(ctx, 2, cStart, cStart+contextSize, 1).Contiguous(ctx);
        var vCtx = vPadded.Slice(ctx, 2, cStart, cStart+contextSize, 1).Contiguous(ctx);
        var qP = qBlock.Permute(ctx, 0, 2, 1, 3) // [headDim, chunkSize, numHeads];
        var kP = kCtx.Permute(ctx, 0, 2, 1, 3)   // [headDim, contextSize, numHeads];
        var termAC = kP.MulmatFullPrec(ctx, qP) // [contextSize, chunkSize, numHeads];
        var pP = posEmb.Permute(ctx, 0, 2, 1, 3)   // [headDim, maxSpan, numHeads];
        var termBDRaw = pP.MulmatFullPrec(ctx, qP) // [maxSpan, chunkSize, numHeads];
        var termBD = cb.relativeShiftGGML(ctx, termBDRaw, maxSpan, chunkSize, contextSize, opts.numHeads);
        var logits = termAC.Add(ctx, termBD);
        logits = logits.Scale(ctx, 1.0/double(opts.logitCap));
        logits = logits.Tanh(ctx);
        logits = logits.Scale(ctx, double(opts.logitCap));
        var maskData = make([]float32, contextSize*chunkSize);
        var for i = range chunkSize {
        var for j = range contextSize {
        var actualTime = u*chunkSize + j - padLeft;
        var causalOK = causalMask[i*contextSize+j] > 0;
        var validOK = actualTime >= 0 && actualTime < seqLen;
        if causalOK && validOK {
        maskData[i*contextSize+j] = 0;
        } else {
        maskData[i*contextSize+j] = -1e9;
    }
    }
    }
        var mask = ctx.Input().FromFloats(maskData, contextSize, chunkSize, 1) // 3D for broadcasting over numHeads;
        logits = logits.Add(ctx, mask);
        logits = logits.Softmax(ctx) // softmax over ne[0]=contextSize;
        var vP = vCtx.Permute(ctx, 0, 2, 1, 3) // [headDim, contextSize, numHeads];
        var vPT = vP.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx) // [contextSize, headDim, numHeads];
        var chunkOut = vPT.Mulmat(ctx, logits)                // [headDim, chunkSize, numHeads];
        chunkOut = chunkOut.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        chunkOutputs[u] = chunkOut;
    }
        var attnOut ml.Tensor;
        if numChunks == 1 {
        attnOut = chunkOutputs[0];
        } else {
        attnOut = chunkOutputs[0];
        var for _, co = range chunkOutputs[1:] {
        attnOut = attnOut.Concat(ctx, co, 2);
    }
    }
        if paddedLen > seqLen {
        attnOut = attnOut.Slice(ctx, 2, 0, seqLen, 1).Contiguous(ctx);
    }
        attnOut = attnOut.Reshape(ctx, hiddenSize, seqLen);
        x = cb.AttnOut.Forward(ctx, attnOut);
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = cb.AttnPostNorm.Forward(ctx, x, opts.eps);
        return residual.Add(ctx, x);
    }
        func (cb *AudioConformerBlock) buildPositionEmbeddings(ctx ml.Context, maxSpan int, opts *AudioModelOptions) ml.Tensor {
        var halfDim = opts.hiddenSize / 2;
        var hiddenSize = opts.hiddenSize;
        var logInc = math.Log(10000.0) / math.Max(double(halfDim-1), 1);
        var posData = make([]float32, hiddenSize*maxSpan);
        var for p = range maxSpan {
        var relPos = double(opts.maxPast - p);
        var for d = range halfDim {
        var angle = relPos * math.Exp(double(-d)*logInc);
        posData[p*hiddenSize+d] = float32(math.Sin(angle));
        posData[p*hiddenSize+halfDim+d] = float32(math.Cos(angle));
    }
    }
        var posEmb = ctx.Input().FromFloats(posData, hiddenSize, maxSpan);
        var projPos = cb.LinearPos.Mulmat(ctx, posEmb);
        return projPos.Reshape(ctx, opts.headDim, opts.numHeads, maxSpan);
    }
        func (cb *AudioConformerBlock) relativeShiftGGML(ctx ml.Context, x ml.Tensor, maxSpan, chunkSize, contextSize, numHeads int) ml.Tensor {
        var padAmt = contextSize + 1 - maxSpan;
        if padAmt > 0 {
        x = x.Pad(ctx, padAmt, 0, 0, 0) // [maxSpan+padAmt, chunkSize, numHeads] = [contextSize+1, chunkSize, numHeads];
    }
        x = x.Reshape(ctx, (contextSize+1)*chunkSize, numHeads);
        x = x.Slice(ctx, 0, 0, contextSize*chunkSize, 1).Contiguous(ctx);
        return x.Reshape(ctx, contextSize, chunkSize, numHeads);
    }
        func (cb *AudioConformerBlock) forwardLightConv(ctx ml.Context, x ml.Tensor, opts *AudioModelOptions, blockIdx int) ml.Tensor {
        var residual = x;
        x = cb.ConvNorm.Forward(ctx, x, opts.eps);
        x = cb.ConvPW1.Forward(ctx, x) // [2*D, T, B];
        var d = x.Dim(0) / 2;
        var data = x.Slice(ctx, 0, 0, d, 1).Contiguous(ctx);
        var gate = x.Slice(ctx, 0, d, d*2, 1).Contiguous(ctx).Sigmoid(ctx);
        x = data.Mul(ctx, gate) // [D, T, B];
        var kernelSize = cb.ConvDW.Dim(0) // K=5;
        var seqLen = x.Dim(1);
        var kernelT = cb.ConvDW.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx) // [D, K];
        var convOut ml.Tensor;
        var for k = range kernelSize {
        var shift = kernelSize - 1 - k;
        var shifted ml.Tensor;
        if shift == 0 {
        shifted = x;
        } else {
        var trimmed = x.Slice(ctx, 1, 0, seqLen-shift, 1).Contiguous(ctx);
        shifted = trimmed.PadExt(ctx, 0, 0, shift, 0, 0, 0, 0, 0);
    }
        var wk = kernelT.Slice(ctx, 1, k, k+1, 1).Contiguous(ctx) // [D, 1];
        var term = shifted.Mul(ctx, wk);
        if convOut == null {
        convOut = term;
        } else {
        convOut = convOut.Add(ctx, term);
    }
    }
        x = convOut;
        x = x.Clamp(ctx, -opts.gradClip, opts.gradClip);
        x = cb.NormConv.Forward(ctx, x, opts.eps);
        x = x.SILU(ctx);
        x = cb.ConvPW2.Forward(ctx, x);
        return x.Add(ctx, residual);
    }
        func newAudioModel(c fs.Config) *AudioModel {
        var numLayers = int(c.Uint("audio.block_count", 0));
        if numLayers == 0 {
        return null;
    }
        return &AudioModel{
        Layers: make([]AudioConformerBlock, numLayers),;
    }
    }
        func newAudioModelOptions(c fs.Config) *AudioModelOptions {
        var hiddenSize = int(c.Uint("audio.embedding_length", 0));
        if hiddenSize == 0 {
        return null;
    }
        var numHeads = int(c.Uint("audio.attention.head_count", 8));
        var headDim = hiddenSize / numHeads;
        var chunkSize = 12 // default conformer chunk size;
        var maxPast = 12   // conf_attention_context_left - 1;
        var maxFuture = 0  // conf_attention_context_right;
        var convKernel = int(c.Uint("audio.conv_kernel_size", 5));
        var eps = c.Float("audio.attention.layer_norm_epsilon", 1e-6);
        return &AudioModelOptions{
        hiddenSize:     hiddenSize,;
        numHeads:       numHeads,;
        headDim:        headDim,;
        ffnSize:        int(c.Uint("audio.feed_forward_length", uint32(hiddenSize*4))),;
        numLayers:      int(c.Uint("audio.block_count", 12)),;
        melBins:        int(c.Uint("audio.num_mel_bins", 128)),;
        chunkSize:      chunkSize,;
        maxPast:        maxPast,;
        maxFuture:      maxFuture,;
        contextSize:    chunkSize + maxPast + maxFuture,;
        logitCap:       50.0,;
        residualWeight: 0.5,;
        gradClip:       1e10,;
        convKernelSize: convKernel,;
        eps:            float32(eps),;
    }
    }
        func buildCausalValidMaskF32(chunkSize, maxPast, maxFuture int) []float32 {
        var contextSize = chunkSize + maxPast + maxFuture;
        var upperDiag = maxPast + maxFuture;
        var result = make([]float32, chunkSize*contextSize);
        var for r = range chunkSize {
        var for c = range contextSize {
        var lower = (r <= c)           // tril(contextSize, chunkSize) transposed;
        var upper = (c <= r+upperDiag) // tril(chunkSize, contextSize, diag=upperDiag);
        if lower && upper {
        result[r*contextSize+c] = 1.0;
    }
    }
    }
        return result;
    }
}
