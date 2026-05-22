package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );
        const batchSize = 1;

    public static class ClippableLinear {
        public ml.Tensor Weight;
        public ml.Tensor InputMin;
        public ml.Tensor InputMax;
        public ml.Tensor OutputMin;
        public ml.Tensor OutputMax;
        public inMax, inMin,;
        public boolean hasClamp;
        public boolean clampsLoaded;
    }

    public static void scalarValue() {
        if t == null {
        return 0, false;
    }
        var data = t.BackendGet();
        if len(data) == 0 {
        return 0, false;
    }
        return data[0], true;
    }
        func (l *ClippableLinear) loadClampFromScalars() {
        if l.clampsLoaded {
        return;
    }
        l.clampsLoaded = true;
        const (;
        defaultMin = -math.MaxFloat32;
        defaultMax = math.MaxFloat32;
        );
        var inMin, hasInMin = scalarValue(l.InputMin);
        var inMax, hasInMax = scalarValue(l.InputMax);
        var outMin, hasOutMin = scalarValue(l.OutputMin);
        var outMax, hasOutMax = scalarValue(l.OutputMax);
        if !(hasInMin || hasInMax || hasOutMin || hasOutMax) {
        return;
    }
        l.hasClamp = true;
        l.inMin = defaultMin;
        l.inMax = defaultMax;
        l.outMin = defaultMin;
        l.outMax = defaultMax;
        if hasInMin {
        l.inMin = inMin;
    }
        if hasInMax {
        l.inMax = inMax;
    }
        if hasOutMin {
        l.outMin = outMin;
    }
        if hasOutMax {
        l.outMax = outMax;
    }
    }
        func (l *ClippableLinear) Forward(ctx ml.Context, x ml.Tensor) ml.Tensor {
        if l.hasClamp {
        x = x.Clamp(ctx, l.inMin, l.inMax);
    }
        var out = l.Weight.Mulmat(ctx, x);
        if l.hasClamp {
        out = out.Clamp(ctx, l.outMin, l.outMax);
    }
        return out;
    }
        func (m *VisionModel) InitClamp(proj *MultiModalProjector) {
        if m.clampInitDone {
        return;
    }
        m.clampInitDone = true;
        var linears = func(l *VisionEncoderLayer) []*ClippableLinear {
        return []*ClippableLinear{
        l.SelfAttention.Query, l.SelfAttention.Key, l.SelfAttention.Value,;
        l.SelfAttention.Output, l.MLP.Gate, l.MLP.Up, l.MLP.Down,;
    }
    }
        var for i = range m.Layers {
        var for _, cl = range linears(&m.Layers[i]) {
        if cl != null {
        cl.loadClampFromScalars();
    }
    }
    }
        if proj != null && proj.Projection != null {
        proj.Projection.loadClampFromScalars();
    }
        if m.ClampData == null {
        return;
    }
        var data = m.ClampData.BackendGet();
        if len(data) == 0 {
        return;
    }
        var for i = range m.Layers {
        var for li, cl = range linears(&m.Layers[i]) {
        if cl == null {
        continue;
    }
        var idx = (i*7 + li) * 4;
        if idx+3 < len(data) {
        cl.inMin = data[idx];
        cl.inMax = data[idx+1];
        cl.outMin = data[idx+2];
        cl.outMax = data[idx+3];
        cl.hasClamp = true;
    }
    }
    }
        if proj != null && proj.Projection != null {
        var projIdx = len(m.Layers) * 7 * 4;
        if projIdx+3 < len(data) {
        proj.Projection.inMin = data[projIdx];
        proj.Projection.inMax = data[projIdx+1];
        proj.Projection.outMin = data[projIdx+2];
        proj.Projection.outMax = data[projIdx+3];
        proj.Projection.hasClamp = true;
    }
    }
    }

    public static class VisionSelfAttention {
        public *ClippableLinear Query;
        public *ClippableLinear Key;
        public *ClippableLinear Value;
        public *nn.RMSNorm QueryNorm;
        public *nn.RMSNorm KeyNorm;
        public *ClippableLinear Output;
    }
        func (sa *VisionSelfAttention) Forward(ctx ml.Context, hiddenState, posX, posY, attnMask ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var numPatches = hiddenState.Dim(1);
        var headDim = opts.hiddenSize / opts.numHeads;
        var query = sa.Query.Forward(ctx, hiddenState);
        var key = sa.Key.Forward(ctx, hiddenState);
        var value = sa.Value.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, numPatches, batchSize);
        key = key.Reshape(ctx, headDim, opts.numHeads, numPatches, batchSize);
        value = value.Reshape(ctx, headDim, opts.numHeads, numPatches, batchSize);
        query = sa.QueryNorm.Forward(ctx, query, opts.eps);
        key = sa.KeyNorm.Forward(ctx, key, opts.eps);
        value = value.RMSNorm(ctx, null, opts.eps);
        var halfDim = headDim / 2;
        var ropeOpts = rope.WithTypeNeoX();
        var qFirst = query.View(ctx, 0, halfDim, query.Stride(1), opts.numHeads, query.Stride(2), numPatches);
        qFirst = nn.RoPE(ctx, qFirst, posX, halfDim, opts.ropeTheta, 1.0, ropeOpts);
        var kFirst = key.View(ctx, 0, halfDim, key.Stride(1), opts.numHeads, key.Stride(2), numPatches);
        kFirst = nn.RoPE(ctx, kFirst, posX, halfDim, opts.ropeTheta, 1.0, ropeOpts);
        var halfOffset = halfDim * query.Stride(0);
        var qSecond = query.View(ctx, halfOffset, halfDim, query.Stride(1), opts.numHeads, query.Stride(2), numPatches);
        qSecond = nn.RoPE(ctx, qSecond, posY, halfDim, opts.ropeTheta, 1.0, ropeOpts);
        var halfOffsetK = halfDim * key.Stride(0);
        var kSecond = key.View(ctx, halfOffsetK, halfDim, key.Stride(1), opts.numHeads, key.Stride(2), numPatches);
        kSecond = nn.RoPE(ctx, kSecond, posY, halfDim, opts.ropeTheta, 1.0, ropeOpts);
        query = qFirst.Concat(ctx, qSecond, 0);
        key = kFirst.Concat(ctx, kSecond, 0);
        var attention = nn.Attention(ctx, query, key, value, 1.0, null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), batchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *ClippableLinear Gate;
        public *ClippableLinear Up;
        public *ClippableLinear Down;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenState ml.Tensor) ml.Tensor {
        var gate = mlp.Gate.Forward(ctx, hiddenState);
        var up = mlp.Up.Forward(ctx, hiddenState);
        hiddenState = gate.QuickGELU(ctx, up);
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class VisionEncoderLayer {
        public *nn.RMSNorm AttentionNorm;
        public *VisionSelfAttention SelfAttention;
        public *nn.RMSNorm PostAttentionNorm;
        public *nn.RMSNorm FFNNorm;
        public *VisionMLP MLP;
        public *nn.RMSNorm PostFFNNorm;
        public ml.Tensor LayerOutputScale;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenState, posX, posY, attnMask ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = e.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.SelfAttention.Forward(ctx, hiddenState, posX, posY, attnMask, opts);
        hiddenState = e.PostAttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = e.FFNNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.MLP.Forward(ctx, hiddenState);
        hiddenState = e.PostFFNNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = hiddenState.Add(ctx, residual);
        if e.LayerOutputScale != null {
        hiddenState = hiddenState.Mul(ctx, e.LayerOutputScale);
    }
        return hiddenState;
    }

    public static class VisionModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int patchSize;
        public int nMerge;
        public float32 eps;
        public float32 ropeTheta;
    }

    public static class VisionModel {
        public *nn.Conv2D PatchEmbedding;
        public ml.Tensor PositionEmbedding;
        public ml.Tensor ClampData;
        public ml.Tensor StdBias;
        public ml.Tensor StdScale;
        public []VisionEncoderLayer Layers;
        public boolean clampInitDone;
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor, numPatchesX, numPatchesY int) ml.Tensor {
        var numPatches = numPatchesX * numPatchesY;
        var hiddenState = m.PatchEmbedding.Forward(ctx, pixelValues, m.patchSize, m.patchSize, 0, 0, 1, 1);
        hiddenState = hiddenState.Reshape(ctx, numPatches, m.hiddenSize);
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        hiddenState = hiddenState.Cast(ctx, ml.DTypeF32);
        var posSize = m.PositionEmbedding.Dim(1);
        var nb1 = m.PositionEmbedding.Stride(1);
        var tblX = m.PositionEmbedding.View(ctx, 0, m.hiddenSize, nb1, posSize);
        var tblY = m.PositionEmbedding.View(ctx, posSize*nb1, m.hiddenSize, nb1, posSize);
        var posXData = make([]int32, numPatches);
        var posYData = make([]int32, numPatches);
        var for i = range numPatches {
        posXData[i] = int32(i % numPatchesX);
        posYData[i] = int32(i / numPatchesX);
    }
        var posXEmb = ctx.Input().FromInts(posXData, numPatches);
        var posYEmb = ctx.Input().FromInts(posYData, numPatches);
        hiddenState = hiddenState.Add(ctx, tblX.Rows(ctx, posXEmb));
        hiddenState = hiddenState.Add(ctx, tblY.Rows(ctx, posYEmb));
        var attnMask ml.Tensor;
        var posXRope = ctx.Input().FromInts(posXData, numPatches);
        var posYRope = ctx.Input().FromInts(posYData, numPatches);
        var for i = range m.Layers {
        hiddenState = m.Layers[i].Forward(ctx, hiddenState, posXRope, posYRope, attnMask, m.VisionModelOptions);
    }
        return hiddenState;
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count")),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize: int(c.Uint("vision.embedding_length")),;
        numHeads:   int(c.Uint("vision.attention.head_count")),;
        patchSize:  int(c.Uint("vision.patch_size", 16)),;
        nMerge:     int(c.Uint("vision.projector.scale_factor", 3)),;
        eps:        c.Float("vision.attention.layer_norm_epsilon", 1e-6),;
        ropeTheta:  100.0,;
        },;
    }
    }
        func visionPoolAndProject(ctx ml.Context, hiddenState ml.Tensor, numPatchesX, numPatchesY int, opts *VisionModelOptions, proj *MultiModalProjector, stdBias, stdScale ml.Tensor) ml.Tensor {
        var hiddenSize = opts.hiddenSize;
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        hiddenState = hiddenState.Reshape(ctx, numPatchesX, numPatchesY, hiddenSize);
        hiddenState = hiddenState.AvgPool2D(ctx, opts.nMerge, opts.nMerge, 0);
        var mergedX = numPatchesX / opts.nMerge;
        var mergedY = numPatchesY / opts.nMerge;
        hiddenState = hiddenState.Reshape(ctx, mergedX*mergedY, hiddenSize);
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        hiddenState = hiddenState.Cast(ctx, ml.DTypeF32);
        hiddenState = hiddenState.Scale(ctx, math.Sqrt(double(hiddenSize)));
        if stdBias != null && stdScale != null {
        hiddenState = hiddenState.Sub(ctx, stdBias);
        hiddenState = hiddenState.Mul(ctx, stdScale);
    }
        hiddenState = proj.Forward(ctx, hiddenState, opts.eps);
        return hiddenState;
    }
}
