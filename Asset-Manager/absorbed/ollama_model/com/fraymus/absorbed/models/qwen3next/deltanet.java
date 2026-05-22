package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class deltanet {
        "errors";
        "log/slog";
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );
        const chunkSize = 64;
        const (;
        TriTypeUpperDiag = 0;
        TriTypeUpper     = 1;
        TriTypeLowerDiag = 2;
        TriTypeLower     = 3;
        );

    public static class convKernel {
        public ml.Tensor Weight;
    }

    public static class Masks {
        public ml.Tensor Causal;
        public ml.Tensor Identity;
        public ml.Tensor Diag;
    }

    public static class GatedDeltaNet {
        public *nn.Linear SSMQKV;
        public *nn.Linear SSMQKVGate;
        public *nn.Linear SSMIn;
        public *nn.Linear SSMBetaAlpha;
        public *nn.Linear SSMBeta;
        public *nn.Linear SSMAlpha;
        public *convKernel SSMConv1D;
        public ml.Tensor SSMDT;
        public ml.Tensor SSMA;
        public *nn.RMSNorm SSMNorm;
        public *nn.Linear SSMOut;
        public int Layer;
    }
        func createMasks(ctx ml.Context) *Masks {
        var ones = ctx.Input().Zeros(ml.DTypeF32, chunkSize, chunkSize);
        ones = ones.Fill(ctx, 1.0);
        var causalMask = ones.Tri(ctx, TriTypeLower);
        var onesVec = ctx.Input().Zeros(ml.DTypeF32, chunkSize);
        onesVec = onesVec.Fill(ctx, 1.0);
        var identity = onesVec.Diag(ctx);
        var diagMask = causalMask.Add(ctx, identity);
        return &Masks{
        Causal:   causalMask,;
        Identity: identity,;
        Diag:     diagMask,;
    }
    }
        func (gdn *GatedDeltaNet) Forward(ctx ml.Context, hiddenStates, _ ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var layer = gdn.Layer;
        var nSeqTokens = hiddenStates.Dim(1);
        var nSeqs = hiddenStates.Dim(2);
        if cache != null && cache.IsSupportedForBatch() {
        var seqTokens = cache.seqTokens();
        var seqs = cache.numSeqs();
        if seqTokens > 0 && seqs > 0 {
        if nSeqs > 1 {
        if nSeqTokens != seqTokens || nSeqs != seqs {
        return null, ErrUnsupportedBatchLayout;
    }
        } else {
        if nSeqTokens != seqTokens*seqs {
        return null, ErrUnsupportedBatchLayout;
    }
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), seqTokens, seqs);
        nSeqTokens = seqTokens;
        nSeqs = seqs;
    }
    }
    }
        var headKDim = opts.ssmDState;
        var numKHeads = opts.ssmNGroup;
        var numVHeads = opts.ssmDtRank;
        var headVDim = opts.ssmDInner / numVHeads;
        var convKernelSize = opts.convKernelSize;
        var qkvDim = headKDim*numKHeads*2 + headVDim*numVHeads;
        var qkvMixed, z ml.Tensor;
        switch {
        case gdn.SSMQKV != null && gdn.SSMQKVGate != null:;
        qkvMixed = gdn.SSMQKV.Forward(ctx, hiddenStates).Reshape(ctx, qkvDim, nSeqTokens, nSeqs);
        z = gdn.SSMQKVGate.Forward(ctx, hiddenStates);
        case gdn.SSMIn != null:;
        var vPerHead = headVDim * numVHeads / numKHeads;
        var qkvzDim = 2*headKDim + 2*vPerHead;
        var combined = gdn.SSMIn.Forward(ctx, hiddenStates).Reshape(ctx, qkvzDim, numKHeads, nSeqTokens, nSeqs);
        var qPart = combined.Slice(ctx, 0, 0, headKDim, 1).Contiguous(ctx, headKDim*numKHeads, nSeqTokens, nSeqs);
        var kPart = combined.Slice(ctx, 0, headKDim, 2*headKDim, 1).Contiguous(ctx, headKDim*numKHeads, nSeqTokens, nSeqs);
        var vPart = combined.Slice(ctx, 0, 2*headKDim, 2*headKDim+vPerHead, 1).Contiguous(ctx, headVDim*numVHeads, nSeqTokens, nSeqs);
        var zPart = combined.Slice(ctx, 0, 2*headKDim+vPerHead, qkvzDim, 1).Contiguous(ctx, headVDim*numVHeads, nSeqTokens, nSeqs);
        qkvMixed = qPart.Concat(ctx, kPart, 0).Concat(ctx, vPart, 0);
        z = zPart;
        default:;
        return null, errors.New("qwen3next: missing attn_qkv/attn_gate or ssm_in projections");
    }
        var beta ml.Tensor;
        var alpha ml.Tensor;
        switch {
        case gdn.SSMBetaAlpha != null:;
        var mixedBA = gdn.SSMBetaAlpha.Forward(ctx, hiddenStates);
        var baNewDim = 2 * numVHeads / numKHeads;
        var mixedBAReshaped = mixedBA.Reshape(ctx, baNewDim, numKHeads, nSeqTokens, nSeqs);
        var betaSize = numVHeads / numKHeads;
        var alphaSize = numVHeads / numKHeads;
        var b = mixedBAReshaped.Slice(ctx, 0, 0, betaSize, 1);
        var a = mixedBAReshaped.Slice(ctx, 0, betaSize, betaSize+alphaSize, 1);
        beta = b.Contiguous(ctx, 1, numVHeads, nSeqTokens, nSeqs);
        alpha = a.Contiguous(ctx, numVHeads, nSeqTokens, nSeqs);
        case gdn.SSMBeta != null && gdn.SSMAlpha != null:;
        beta = gdn.SSMBeta.Forward(ctx, hiddenStates).Reshape(ctx, 1, numVHeads, nSeqTokens, nSeqs);
        alpha = gdn.SSMAlpha.Forward(ctx, hiddenStates).Reshape(ctx, numVHeads, nSeqTokens, nSeqs);
        default:;
        return null, errors.New("qwen3next: missing linear attention beta/alpha projections");
    }
        if gdn.SSMDT == null {
        return null, errors.New("qwen3next: missing linear attention ssm_dt tensor");
    }
        if gdn.SSMA == null {
        return null, errors.New("qwen3next: missing linear attention ssm_a tensor");
    }
        if gdn.SSMConv1D == null || gdn.SSMConv1D.Weight == null {
        return null, errors.New("qwen3next: missing linear attention ssm_conv1d tensor");
    }
        if gdn.SSMNorm == null || gdn.SSMOut == null {
        return null, errors.New("qwen3next: missing linear attention ssm_norm/ssm_out projections");
    }
        var alphaBiased = alpha.Add(ctx, gdn.SSMDT);
        var alphaSoftplus = alphaBiased.Softplus(ctx);
        var gate = alphaSoftplus.Mul(ctx, gdn.SSMA);
        gate = gate.Reshape(ctx, 1, numVHeads, nSeqTokens, nSeqs);
        qkvMixed = qkvMixed.Permute(ctx, 1, 0, 2, 3);
        var convStates, err = cache.ConvState(ctx, layer);
        if err != null {
        slog.Warn("qwen3next: failed to get conv state, using zeros", "layer", layer, "error", err);
        convStates = ctx.Input().Zeros(ml.DTypeF32, convKernelSize-1, qkvDim, nSeqs);
    }
        convStates = convStates.Reshape(ctx, convKernelSize-1, qkvDim, nSeqs);
        var convInput = convStates.Concat(ctx, qkvMixed, 0);
        var lastConvStates = convInput.Slice(ctx, 0, nSeqTokens, nSeqTokens+convKernelSize-1, 1);
        cache.UpdateConvState(ctx, layer, lastConvStates);
        var convOutput = convInput.SSMConv(ctx, gdn.SSMConv1D.Weight);
        convOutput = convOutput.SILU(ctx);
        var convQKVMix = convOutput.Contiguous(ctx, qkvDim, nSeqTokens*nSeqs);
        var qConv = convQKVMix.Slice(ctx, 0, 0, headKDim*numKHeads, 1);
        var kConv = convQKVMix.Slice(ctx, 0, headKDim*numKHeads, 2*headKDim*numKHeads, 1);
        var vConv = convQKVMix.Slice(ctx, 0, 2*headKDim*numKHeads, qkvDim, 1);
        qConv = qConv.Contiguous(ctx, headKDim, numKHeads, nSeqTokens, nSeqs);
        kConv = kConv.Contiguous(ctx, headKDim, numKHeads, nSeqTokens, nSeqs);
        vConv = vConv.Contiguous(ctx, headVDim, numVHeads, nSeqTokens, nSeqs);
        var state, err = cache.DeltaState(ctx, layer, headVDim, numVHeads);
        if err != null {
        slog.Warn("qwen3next: failed to get delta state, using zeros", "layer", layer, "error", err);
        state = ctx.Input().Zeros(ml.DTypeF32, headVDim, headVDim*numVHeads, nSeqs);
    }
        state = state.Reshape(ctx, headVDim, headVDim*numVHeads, 1, nSeqs);
        if numKHeads != numVHeads {
        if opts.vHeadReordered {
        qConv = qConv.Repeat4D(ctx, headKDim, numVHeads, nSeqTokens, nSeqs);
        kConv = kConv.Repeat4D(ctx, headKDim, numVHeads, nSeqTokens, nSeqs);
        } else {
        var repeatFactor = numVHeads / numKHeads;
        var qReshaped = qConv.Reshape(ctx, headKDim, 1, numKHeads*nSeqTokens*nSeqs);
        var kReshaped = kConv.Reshape(ctx, headKDim, 1, numKHeads*nSeqTokens*nSeqs);
        var qRepeated = qReshaped.Repeat4D(ctx, headKDim, repeatFactor, numKHeads*nSeqTokens*nSeqs, 1);
        var kRepeated = kReshaped.Repeat4D(ctx, headKDim, repeatFactor, numKHeads*nSeqTokens*nSeqs, 1);
        qConv = qRepeated.Reshape(ctx, headKDim, numKHeads*repeatFactor, nSeqTokens, nSeqs);
        kConv = kRepeated.Reshape(ctx, headKDim, numKHeads*repeatFactor, nSeqTokens, nSeqs);
    }
    }
        var attnOut ml.Tensor;
        if nSeqTokens == 1 {
        attnOut = gdn.deltaNetAutoregressive(ctx, qConv, kConv, vConv, gate, beta, state, opts, layer, cache);
        } else {
        if opts.masks == null {
        opts.masks = createMasks(ctx);
    }
        attnOut = gdn.deltaNetChunked(ctx, qConv, kConv, vConv, gate, beta, state, opts.masks, opts, layer, cache);
    }
        var attnOut2D = attnOut.Contiguous(ctx, headVDim, numVHeads*nSeqTokens*nSeqs);
        var z2D = z.Contiguous(ctx, headVDim, numVHeads*nSeqTokens*nSeqs);
        var attnOutNorm = gdn.SSMNorm.Forward(ctx, attnOut2D, opts.eps);
        var zSilu = z2D.SILU(ctx);
        var attnOutGated = attnOutNorm.Mul(ctx, zSilu);
        var finalOutput = attnOutGated.Reshape(ctx, headVDim*numVHeads, nSeqTokens, nSeqs);
        var out = gdn.SSMOut.Forward(ctx, finalOutput);
        return out.Reshape(ctx, out.Dim(0), nSeqTokens*nSeqs), null;
    }
        func (gdn *GatedDeltaNet) deltaNetAutoregressive(;
        ctx ml.Context,;
        q, k, v, gate, beta, state ml.Tensor,;
        opts *Options,;
        layer int,;
        cache *HybridCache,;
        ) ml.Tensor {
        var numVHeads = v.Dim(1);
        var headVDim = v.Dim(0);
        var nSeqs = q.Dim(3);
        q = q.L2Norm(ctx, opts.eps);
        k = k.L2Norm(ctx, opts.eps);
        var scale = 1.0 / math.Sqrt(double(headVDim));
        q = q.Scale(ctx, scale);
        beta = beta.Sigmoid(ctx);
        state = state.Reshape(ctx, headVDim, headVDim, numVHeads, nSeqs);
        var gT = gate.Permute(ctx, 1, 0, 2, 3).Reshape(ctx, 1, 1, numVHeads, nSeqs);
        var betaT = beta.Permute(ctx, 1, 0, 2, 3).Reshape(ctx, 1, 1, numVHeads, nSeqs);
        gT = gT.Exp(ctx);
        state = state.Mul(ctx, gT);
        var kTUnsqueezed = k.Reshape(ctx, 1, headVDim, numVHeads, nSeqs);
        var kvMem = state.Mul(ctx, kTUnsqueezed);
        kvMem = kvMem.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        kvMem = kvMem.SumRows(ctx);
        kvMem = kvMem.Permute(ctx, 1, 0, 2, 3);
        var vT = v.Reshape(ctx, headVDim, 1, numVHeads, nSeqs);
        var vDiff = vT.Sub(ctx, kvMem);
        var delta = vDiff.Mul(ctx, betaT);
        var kTUnsqueezedBroad = kTUnsqueezed.Repeat4D(ctx, headVDim, headVDim, numVHeads, nSeqs);
        var kTDelta = kTUnsqueezedBroad.Mul(ctx, delta);
        state = state.Add(ctx, kTDelta);
        var qTUnsqueezed = q.Reshape(ctx, 1, headVDim, numVHeads, nSeqs);
        var stateQ = state.Mul(ctx, qTUnsqueezed);
        stateQ = stateQ.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var coreAttnOut = stateQ.SumRows(ctx);
        coreAttnOut = coreAttnOut.Permute(ctx, 1, 0, 2, 3);
        cache.UpdateDeltaState(ctx, layer, state.Reshape(ctx, headVDim, headVDim*numVHeads, nSeqs));
        return coreAttnOut.Reshape(ctx, headVDim, numVHeads, 1, nSeqs);
    }
        func (gdn *GatedDeltaNet) deltaNetChunked(;
        ctx ml.Context,;
        q, k, v, gate, beta, state ml.Tensor,;
        masks *Masks,;
        opts *Options,;
        layer int,;
        cache *HybridCache,;
        ) ml.Tensor {
        var headKDim = q.Dim(0);
        var numVHeads = v.Dim(1);
        var headVDim = v.Dim(0);
        var nTokens = q.Dim(2);
        var nSeqs = q.Dim(3);
        q = q.L2Norm(ctx, opts.eps);
        k = k.L2Norm(ctx, opts.eps);
        var scale = 1.0 / math.Sqrt(double(headVDim));
        q = q.Scale(ctx, scale);
        beta = beta.Sigmoid(ctx);
        q = q.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, headKDim, nTokens, numVHeads, nSeqs);
        k = k.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, headKDim, nTokens, numVHeads, nSeqs);
        v = v.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, headVDim, nTokens, numVHeads, nSeqs);
        gate = gate.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, 1, nTokens, numVHeads, nSeqs);
        beta = beta.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, 1, nTokens, numVHeads, nSeqs);
        state = state.Reshape(ctx, headVDim, headVDim, numVHeads, nSeqs);
        var pad = (chunkSize - nTokens%chunkSize) % chunkSize;
        var nChunks = (nTokens + pad) / chunkSize;
        if pad > 0 {
        q = q.Pad(ctx, 0, pad, 0, 0);
        k = k.Pad(ctx, 0, pad, 0, 0);
        v = v.Pad(ctx, 0, pad, 0, 0);
        gate = gate.Pad(ctx, 0, pad, 0, 0);
        beta = beta.Pad(ctx, 0, pad, 0, 0);
    }
        var causalMask = masks.Causal;
        var identity = masks.Identity;
        var diagMask = masks.Diag;
        var identity4D = identity.Reshape(ctx, chunkSize, chunkSize, 1, 1);
        var vBeta = v.Mul(ctx, beta);
        var kBeta = k.Mul(ctx, beta);
        q = q.Reshape(ctx, headKDim, chunkSize, nChunks, numVHeads*nSeqs);
        k = k.Reshape(ctx, headKDim, chunkSize, nChunks, numVHeads*nSeqs);
        kBeta = kBeta.Reshape(ctx, headKDim, chunkSize, nChunks, numVHeads*nSeqs);
        vBeta = vBeta.Reshape(ctx, headVDim, chunkSize, nChunks, numVHeads*nSeqs);
        gate = gate.Reshape(ctx, 1, chunkSize, nChunks, numVHeads*nSeqs);
        var gCumsum = gate.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx, chunkSize, 1, nChunks, numVHeads*nSeqs).CumSum(ctx);
        var gcsI = gCumsum.Reshape(ctx, chunkSize, 1, nChunks, numVHeads*nSeqs);
        var gcsJ = gCumsum.Reshape(ctx, 1, chunkSize, nChunks, numVHeads*nSeqs);
        var gcsBroadcast = gcsJ.Repeat4D(ctx, chunkSize, chunkSize, nChunks, numVHeads*nSeqs);
        var decayMask = gcsBroadcast.Sub(ctx, gcsI);
        decayMask = decayMask.Mul(ctx, diagMask);
        decayMask = decayMask.Exp(ctx);
        decayMask = decayMask.Mul(ctx, diagMask);
        var kMulKBeta = k.Mulmat(ctx, kBeta);
        var kDecay = kMulKBeta.Mul(ctx, decayMask);
        var attn = kDecay.Neg(ctx).Mul(ctx, causalMask);
        var attnLower = attn.Mul(ctx, causalMask);
        var lhs = attnLower.Neg(ctx).Add(ctx, identity4D);
        var linSolve = lhs.SolveTri(ctx, attn, true, true, false);
        attn = linSolve.Mul(ctx, causalMask);
        attn = attn.Add(ctx, identity4D);
        var vBetaT = vBeta.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        v = vBetaT.Mulmat(ctx, attn);
        var gCumsumT = gCumsum.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var gExp = gCumsumT.Exp(ctx);
        var kBetaGExp = kBeta.Mul(ctx, gExp);
        var kBetaGExpT = kBetaGExp.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var kCumdecay = attn.Mulmat(ctx, kBetaGExpT);
        kCumdecay = kCumdecay.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var attnKQ = k.Mulmat(ctx, q);
        attnKQ = attnKQ.Mul(ctx, decayMask);
        attnKQ = attnKQ.Mul(ctx, diagMask);
        var gLast = gCumsum.Slice(ctx, 0, chunkSize-1, chunkSize, 1).Contiguous(ctx, 1, 1, nChunks, numVHeads*nSeqs);
        var gLastExp = gLast.Exp(ctx);
        var gDiff = gCumsum.Neg(ctx).Add(ctx, gLast);
        var gDiffExp = gDiff.Exp(ctx);
        var gDiffExpReshaped = gDiffExp.Reshape(ctx, 1, chunkSize, nChunks, numVHeads*nSeqs);
        var keyGDiff = k.Mul(ctx, gDiffExpReshaped);
        var keyGDiffT = keyGDiff.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var vT = v.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx, chunkSize, headVDim, nChunks, numVHeads*nSeqs);
        var stateT = state.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx, headVDim, headVDim, 1, numVHeads*nSeqs);
        var chunks = make([]ml.Tensor, nChunks);
        var for chunk = range nChunks {
        var qChunk = q.Slice(ctx, 2, chunk, chunk+1, 1);
        var vTChunk = vT.Slice(ctx, 2, chunk, chunk+1, 1);
        var gExpChunk = gExp.Slice(ctx, 2, chunk, chunk+1, 1);
        var kCumdecayChunk = kCumdecay.Slice(ctx, 2, chunk, chunk+1, 1);
        var attnChunk = attnKQ.Slice(ctx, 2, chunk, chunk+1, 1) // Pre-computed!;
        var vTPrime = kCumdecayChunk.Mulmat(ctx, stateT);
        var vTNewChunk = vTChunk.Sub(ctx, vTPrime);
        var qGExp = qChunk.Mul(ctx, gExpChunk);
        var attnInter = stateT.Mulmat(ctx, qGExp);
        var vAttn = vTNewChunk.Mulmat(ctx, attnChunk);
        var coreAttnOutChunk = attnInter.Add(ctx, vAttn);
        chunks[chunk] = coreAttnOutChunk;
        var gExpLastChunk = gLastExp.Slice(ctx, 2, chunk, chunk+1, 1);
        var kGDiffChunkT = keyGDiffT.Slice(ctx, 2, chunk, chunk+1, 1);
        var kgdMulVNew = kGDiffChunkT.Mulmat(ctx, vTNewChunk);
        stateT = stateT.Mul(ctx, gExpLastChunk);
        stateT = stateT.Add(ctx, kgdMulVNew);
    }
        for len(chunks) > 1 {
        var merged = make([]ml.Tensor, 0, (len(chunks)+1)/2);
        var for i = 0; i < len(chunks); i += 2 {
        if i+1 < len(chunks) {
        merged = append(merged, chunks[i].Concat(ctx, chunks[i+1], 2));
        } else {
        merged = append(merged, chunks[i]);
    }
    }
        chunks = merged;
    }
        v = chunks[0];
        var coreAttnOut = v.Contiguous(ctx, headVDim, chunkSize*nChunks, numVHeads, nSeqs);
        if pad > 0 {
        coreAttnOut = coreAttnOut.Slice(ctx, 1, 0, nTokens, 1);
    }
        var newState = stateT.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx, headVDim, headVDim, numVHeads, nSeqs);
        cache.UpdateDeltaState(ctx, layer, newState.Reshape(ctx, headVDim, headVDim*numVHeads, nSeqs));
        return coreAttnOut.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, headVDim, numVHeads, nTokens, nSeqs);
    }
}
