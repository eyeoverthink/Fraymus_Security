package com.fraymus.absorbed.models.qwen25vl;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );
        func blockDiagonalMask(ctx ml.Context, seqLength int, bounds []int) ml.Tensor {
        var s = make([][]float32, seqLength);
        var for i = range s {
        s[i] = slices.Repeat([]float32{float32(math.Inf(-1))}, seqLength);
    }
        var for i = 1; i < len(bounds); i++ {
        var start, end = bounds[i-1], bounds[i];
        var for row = start; row < end; row++ {
        var for col = start; col < end; col++ {
        s[row][col] = 0.0;
    }
    }
    }
        return ctx.Input().FromFloats(slices.Concat(s...), seqLength, seqLength);
    }

    public static class VisionSelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *VisionSelfAttention) Forward(ctx ml.Context, hiddenStates, positions, mask ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var query = sa.Query.Forward(ctx, hiddenStates);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim, opts.numHeads, query.Dim(1));
        key = key.Reshape(ctx, opts.headDim, opts.numHeads, key.Dim(1));
        value = value.Reshape(ctx, opts.headDim, opts.numHeads, value.Dim(1));
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var scale = 1.0 / math.Sqrt(double(opts.headDim));
        query = query.Permute(ctx, 0, 2, 1, 3);
        key = key.Permute(ctx, 0, 2, 1, 3);
        value = value.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var kq = key.MulmatFullPrec(ctx, query);
        kq = kq.Scale(ctx, scale);
        if mask != null {
        kq = kq.Add(ctx, mask);
    }
        kq = kq.Softmax(ctx);
        var kqv = value.Mulmat(ctx, kq);
        var attention = kqv.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2));
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class VisionEncoderLayer {
        public *nn.RMSNorm Norm1;
        public *VisionSelfAttention SelfAttention;
        public *nn.RMSNorm Norm2;
        public *VisionMLP MLP;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenStates, positions, mask ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.Norm1.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.SelfAttention.Forward(ctx, hiddenStates, positions, mask, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = e.Norm2.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.MLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class VisionModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int headDim;
        public int patchSize;
        public int numChannels;
        public float32 eps;
        public float32 ropeTheta;
        public int spatialMergeSize;
        public int windowSize;
        public []int32 fullAttnBlocks;
        public int temporalPatchSize;
    }
        func (o VisionModelOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.headDim/2, o.ropeTheta, 1,;
        rope.WithVision([]int{
        o.headDim / 4,;
        o.headDim / 4,;
        o.headDim / 4,;
        o.headDim / 4,;
        }),;
        );
    }

    public static class PatchEmbedding {
        public *nn.Conv2D PatchConv0;
        public *nn.Conv2D PatchConv1;
    }
        func (pe *PatchEmbedding) Forward(ctx ml.Context, pixelValues ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var numPatches = pixelValues.Shape()[1];
        pixelValues = pixelValues.Reshape(ctx, opts.patchSize*opts.patchSize, opts.temporalPatchSize, opts.numChannels, numPatches);
        pixelValues = pixelValues.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var in0 = pixelValues.View(ctx, 0, 1, pixelValues.Stride(1), pixelValues.Dim(1), pixelValues.Stride(2), pixelValues.Dim(2), pixelValues.Stride(3), pixelValues.Dim(3)).Contiguous(ctx);
        in0 = in0.Reshape(ctx, opts.patchSize, opts.patchSize, opts.numChannels, numPatches);
        var in1 = pixelValues.View(ctx, pixelValues.Stride(0), 1, pixelValues.Stride(1), pixelValues.Dim(1), pixelValues.Stride(2), pixelValues.Dim(2), pixelValues.Stride(3), pixelValues.Dim(3)).Contiguous(ctx);
        in1 = in1.Reshape(ctx, opts.patchSize, opts.patchSize, opts.numChannels, numPatches);
        var s0, s1 = opts.patchSize, opts.patchSize // Use full stride;
        var p0, p1 = 0, 0                           // padding;
        var d0, d1 = 1, 1                           // dilation;
        var out0 = pe.PatchConv0.Forward(ctx, in0, s0, s1, p0, p1, d0, d1);
        var out1 = pe.PatchConv1.Forward(ctx, in1, s0, s1, p0, p1, d0, d1);
        var out = out0.Add(ctx, out1);
        return out.Reshape(ctx, opts.hiddenSize, numPatches);
    }

    public static class VisionPatchMerger {
        public *nn.RMSNorm LNQ;
        public *nn.Linear MLP0;
        public *nn.Linear MLP2;
    }
        func (pm *VisionPatchMerger) Forward(ctx ml.Context, visionOutputs ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var normalized = pm.LNQ.Forward(ctx, visionOutputs, opts.eps);
        var hiddenSize = visionOutputs.Dim(0) * (opts.spatialMergeSize * opts.spatialMergeSize);
        var reshaped = normalized.Reshape(ctx, hiddenSize, normalized.Dim(1)/(opts.spatialMergeSize*opts.spatialMergeSize));
        var hidden = pm.MLP0.Forward(ctx, reshaped);
        var activated = hidden.GELU(ctx);
        var output = pm.MLP2.Forward(ctx, activated);
        return output;
    }

    public static class VisionModel {
        public *PatchEmbedding PatchEmbedding;
        public []VisionEncoderLayer Layers;
        public *VisionPatchMerger PatchMerger;
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor, grid *Grid) ml.Tensor {
        var hiddenStates = m.PatchEmbedding.Forward(ctx, pixelValues, m.VisionModelOptions);
        var index, bounds = m.windowIndex(grid);
        var spatialMergeUnit = m.spatialMergeSize * m.spatialMergeSize;
        var windowIndex = ctx.Input().FromInts(index, len(index));
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0)*spatialMergeUnit, hiddenStates.Dim(1)/spatialMergeUnit);
        hiddenStates = hiddenStates.Rows(ctx, windowIndex.Argsort(ctx));
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0)/spatialMergeUnit, hiddenStates.Dim(1)*spatialMergeUnit);
        var positions = ctx.Input().FromInts(func() []int32 {
        var s = [][]int32{
        make([]int32, grid.Height*grid.Width),;
        make([]int32, grid.Height*grid.Width),;
        make([]int32, grid.Height*grid.Width),;
        make([]int32, grid.Height*grid.Width),;
    }
        var cur int;
        var for y = 0; y < grid.Height; y += m.spatialMergeSize {
        var for x = 0; x < grid.Width; x += m.spatialMergeSize {
        var for dy = range 2 {
        var for dx = range 2 {
        var i = int(index[cur/spatialMergeUnit]) * spatialMergeUnit;
        i += cur % spatialMergeUnit;
        s[0][i] = int32(y + dy);
        s[1][i] = int32(x + dx);
        s[2][i] = int32(y + dy);
        s[3][i] = int32(x + dx);
        cur++;
    }
    }
    }
    }
        return slices.Concat(s...);
        }(), grid.Height*grid.Width*4);
        var mask = blockDiagonalMask(ctx, hiddenStates.Dim(1), bounds);
        var for i, layer = range m.Layers {
        if slices.Contains(m.fullAttnBlocks, int32(i)) {
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, null, m.VisionModelOptions);
        } else {
        hiddenStates = layer.Forward(;
        ctx,;
        hiddenStates,;
        positions,;
        mask,;
        m.VisionModelOptions,;
        );
    }
    }
        hiddenStates = m.PatchMerger.Forward(ctx, hiddenStates, m.VisionModelOptions);
        return hiddenStates.Rows(ctx, windowIndex);
    }
        func (m *VisionModel) windowIndex(grid *Grid) (index []int32, bounds []int) {
        var height = grid.Height / m.spatialMergeSize;
        var width = grid.Width / m.spatialMergeSize;
        var window = m.windowSize / m.patchSize / m.spatialMergeSize;
        index = make([]int32, height*width);
        bounds = make([]int, 0, ((height+window-1)/window)*((width+window-1)/window)+1);
        bounds = append(bounds, 0);
        var cur int32;
        var for y = 0; y < height; y += window {
        var for x = 0; x < width; x += window {
        var h1 = min(window, height-y);
        var w1 = min(window, width-x);
        var for dy = range h1 {
        var for dx = range w1 {
        var win = (y+dy)*width + (x + dx);
        index[win] = cur;
        cur++;
    }
    }
        bounds = append(bounds, int(cur)*window);
    }
    }
        return index, bounds;
    }
        func newVisionModel(c fs.Config) *VisionModel {
        var patchSize = int(c.Uint("vision.patch_size", 14));
        var hiddenSize = int(c.Uint("vision.embedding_length", 1280));
        var numHeads = int(c.Uint("vision.attention.head_count", 16));
        var numChannels = int(c.Uint("vision.num_channels", 3));
        var eps = c.Float("vision.attention.layer_norm_epsilon", 1e-6);
        var ropeTheta = c.Float("vision.rope.freq_base", 10000.0);
        var spatialMergeSize = int(c.Uint("vision.spatial_merge_size", 2));
        var windowSize = int(c.Uint("vision.window_size", 112));
        var fullAttnBlocks = c.Ints("qwen25vl.vision.fullatt_block_indexes", []int32{7, 15, 23, 31});
        var temporalPatchSize = int(c.Uint("vision.temporal_patch_size", 2));
        var model = &VisionModel{
        Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count", 32)),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize:        hiddenSize,;
        numHeads:          numHeads,;
        headDim:           hiddenSize / numHeads,;
        patchSize:         patchSize,;
        numChannels:       numChannels,;
        eps:               eps,;
        ropeTheta:         ropeTheta,;
        spatialMergeSize:  spatialMergeSize,;
        windowSize:        windowSize,;
        temporalPatchSize: temporalPatchSize,;
        fullAttnBlocks:    fullAttnBlocks,;
        },;
    }
        return model;
    }
}
