package com.fraymus.absorbed.models.glmocr;

import java.util.*;
import java.io.*;

public class model_vision {
        "log/slog";
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );

    public static class Grid {
        public int Height;
        public int Width;
        public int Temporal;
        public int ImageHeight;
        public int ImageWidth;
    }

    public static class VisionModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int headDim;
        public int numChannels;
        public int patchSize;
        public int temporalPatchSize;
        public int imageSize;
        public int spatialMergeSize;
        public int outHiddenSize;
        public int intermediateSize;
        public float32 eps;
    }

    public static class VisionPatchEmbed {
        public *nn.Conv2D Proj;
        public *nn.Conv2D Proj1;
        public ml.Tensor Bias;
    }
        func (pe *VisionPatchEmbed) Forward(ctx ml.Context, pixelValues ml.Tensor, grid *Grid, opts *VisionModelOptions) ml.Tensor {
        _ = grid // patches are already in merge-block order;
        var numPatches = pixelValues.Shape()[1];
        pixelValues = pixelValues.Reshape(ctx, opts.patchSize*opts.patchSize, opts.temporalPatchSize, opts.numChannels, numPatches);
        pixelValues = pixelValues.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var in0 = pixelValues.View(ctx, 0, 1, pixelValues.Stride(1), pixelValues.Dim(1), pixelValues.Stride(2), pixelValues.Dim(2), pixelValues.Stride(3), pixelValues.Dim(3)).Contiguous(ctx);
        in0 = in0.Reshape(ctx, opts.patchSize, opts.patchSize, opts.numChannels, numPatches);
        var s0, s1 = opts.patchSize, opts.patchSize;
        var p0, p1 = 0, 0;
        var d0, d1 = 1, 1;
        var hiddenStates = pe.Proj.Forward(ctx, in0, s0, s1, p0, p1, d0, d1);
        if pe.Proj1 != null && opts.temporalPatchSize > 1 {
        var in1 = pixelValues.View(ctx, pixelValues.Stride(0), 1, pixelValues.Stride(1), pixelValues.Dim(1), pixelValues.Stride(2), pixelValues.Dim(2), pixelValues.Stride(3), pixelValues.Dim(3)).Contiguous(ctx);
        in1 = in1.Reshape(ctx, opts.patchSize, opts.patchSize, opts.numChannels, numPatches);
        var out1 = pe.Proj1.Forward(ctx, in1, s0, s1, p0, p1, d0, d1);
        hiddenStates = hiddenStates.Add(ctx, out1);
    }
        hiddenStates = hiddenStates.Reshape(ctx, opts.hiddenSize, numPatches);
        if pe.Bias != null {
        hiddenStates = hiddenStates.Add(ctx, pe.Bias.Reshape(ctx, opts.hiddenSize, 1));
    }
        return hiddenStates;
    }

    public static class VisionSelfAttention {
        public *nn.Linear QKV;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
        public *nn.Linear Output;
    }
        func (sa *VisionSelfAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var qkv = sa.QKV.Forward(ctx, hiddenStates);
        var chunks = qkv.ChunkSections(ctx, 0, opts.hiddenSize, opts.hiddenSize, opts.hiddenSize);
        var q = chunks[0].Contiguous(ctx);
        var k = chunks[1].Contiguous(ctx);
        var v = chunks[2].Contiguous(ctx);
        q = q.Reshape(ctx, opts.headDim, opts.numHeads, batchSize);
        k = k.Reshape(ctx, opts.headDim, opts.numHeads, batchSize);
        v = v.Reshape(ctx, opts.headDim, opts.numHeads, batchSize);
        q = sa.QNorm.Forward(ctx, q, opts.eps);
        k = sa.KNorm.Forward(ctx, k, opts.eps);
        var ropeFreqBase = float32(10000.0);
        var section = opts.headDim / 4;
        if section <= 0 {
        section = 1;
    }
        var sections = []int{section, section, 0, 0}
        q = nn.RoPE(ctx, q, positions, opts.headDim/2, ropeFreqBase, 1.0, rope.WithVision(sections));
        k = nn.RoPE(ctx, k, positions, opts.headDim/2, ropeFreqBase, 1.0, rope.WithVision(sections));
        var scale = 1.0 / math.Sqrt(double(opts.headDim));
        var if sdpa, ok = q.(ml.ScaledDotProductAttention); ok {
        var attention = sdpa.ScaledDotProductAttention(ctx, k, v, null, null, null, scale, false);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        slog.Warn("glmocr: vision attention falling back to manual attention",;
        "batchSize", batchSize, "numHeads", opts.numHeads,;
        "hint", "set OLLAMA_FLASH_ATTENTION=1 to enable flash attention");
        q = q.Permute(ctx, 0, 2, 1, 3);
        k = k.Permute(ctx, 0, 2, 1, 3);
        v = v.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var kq = k.MulmatFullPrec(ctx, q);
        kq = kq.Scale(ctx, scale);
        kq = kq.Softmax(ctx);
        var kqv = v.Mulmat(ctx, kq);
        var attention = kqv.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor) ml.Tensor {
        var gate = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, gate);
    }

    public static class VisionBlock {
        public *nn.RMSNorm Norm1;
        public *VisionSelfAttention SelfAttention;
        public *nn.RMSNorm Norm2;
        public *VisionMLP MLP;
    }
        func (b *VisionBlock) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = b.Norm1.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = b.SelfAttention.Forward(ctx, hiddenStates, positions, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = b.Norm2.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = b.MLP.Forward(ctx, hiddenStates);
        hiddenStates = hiddenStates.Add(ctx, residual);
        return hiddenStates;
    }

    public static class VisionDownsample {
    }
        func (d *VisionDownsample) Forward(ctx ml.Context, hiddenStates ml.Tensor, grid *Grid, opts *VisionModelOptions) ml.Tensor {
        if d.Conv2D == null || d.Weight == null {
        slog.Error("VisionDownsample weights not loaded - model may be corrupted or incompatible");
        return hiddenStates // Return input unchanged as fallback;
    }
        var merge = opts.spatialMergeSize;
        var numOutputTokens = (grid.Height / merge) * (grid.Width / merge);
        hiddenStates = hiddenStates.Reshape(ctx, opts.hiddenSize, merge, merge, numOutputTokens);
        hiddenStates = hiddenStates.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        var s0, s1 = merge, merge;
        var p0, p1 = 0, 0;
        var d0, d1 = 1, 1;
        hiddenStates = d.Weight.Conv2D(ctx, hiddenStates, s0, s1, p0, p1, d0, d1);
        hiddenStates = hiddenStates.Reshape(ctx, opts.outHiddenSize, numOutputTokens);
        if d.Bias != null {
        hiddenStates = hiddenStates.Add(ctx, d.Bias.Reshape(ctx, opts.outHiddenSize, 1));
    }
        return hiddenStates;
    }

    public static class PatchMerger {
        public *nn.Linear Proj;
        public *nn.LayerNorm PostLN;
        public *nn.Linear GateProj;
        public *nn.Linear UpProj;
        public *nn.Linear DownProj;
    }
        func (m *PatchMerger) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        hiddenStates = m.Proj.Forward(ctx, hiddenStates);
        hiddenStates = m.PostLN.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = hiddenStates.GELU_ERF(ctx);
        hiddenStates = hiddenStates.Contiguous(ctx);
        var gateOut = m.GateProj.Forward(ctx, hiddenStates);
        var upOut = m.UpProj.Forward(ctx, hiddenStates);
        var gate = gateOut.SILU(ctx, upOut);
        return m.DownProj.Forward(ctx, gate);
    }

    public static class VisionModel {
        public *VisionPatchEmbed PatchEmbed;
        public []VisionBlock Blocks;
        public *nn.RMSNorm PostLN;
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor, grid *Grid) ml.Tensor {
        var hiddenStates = m.PatchEmbed.Forward(ctx, pixelValues, grid, m.VisionModelOptions);
        var positions = m.createPositions(ctx, grid);
        var for _, block = range m.Blocks {
        hiddenStates = block.Forward(ctx, hiddenStates, positions, m.VisionModelOptions);
    }
        hiddenStates = m.PostLN.Forward(ctx, hiddenStates, m.eps);
        return hiddenStates;
    }
        func (m *VisionModel) createPositions(ctx ml.Context, grid *Grid) ml.Tensor {
        var numPatches = grid.Height * grid.Width;
        var mergeRatio = m.spatialMergeSize;
        var hpos = make([]int32, numPatches);
        var wpos = make([]int32, numPatches);
        var ptr = 0;
        var for y = 0; y < grid.Height; y += mergeRatio {
        var for x = 0; x < grid.Width; x += mergeRatio {
        var for dy = range mergeRatio {
        var for dx = range mergeRatio {
        hpos[ptr] = int32(y + dy);
        wpos[ptr] = int32(x + dx);
        ptr++;
    }
    }
    }
    }
        var zeros = make([]int32, numPatches);
        var s = [][]int32{
        hpos,  // Section 0: height;
        wpos,  // Section 1: width;
        zeros, // Section 2: unused;
        zeros, // Section 3: unused;
    }
        return ctx.Input().FromInts(slices.Concat(s...), numPatches*4);
    }
        func newVisionModel(c fs.Config) *VisionModel {
        var hiddenSize = int(c.Uint("vision.embedding_length", 1024));
        var numHeads = int(c.Uint("vision.attention.head_count", 16));
        var numChannels = int(c.Uint("vision.num_channels", 3));
        var patchSize = int(c.Uint("vision.patch_size", 14));
        var temporalPatchSize = int(c.Uint("vision.temporal_patch_size", 2));
        var imageSize = int(c.Uint("vision.image_size", 336));
        var spatialMergeSize = int(c.Uint("vision.spatial_merge_size", 2));
        var outHiddenSize = int(c.Uint("vision.out_hidden_size", 1536));
        var intermediateSize = int(c.Uint("vision.intermediate_size", 4096));
        var eps = c.Float("vision.attention.layer_norm_rms_epsilon", 1e-5);
        return &VisionModel{
        Blocks: make([]VisionBlock, c.Uint("vision.block_count", 24)),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize:        hiddenSize,;
        numHeads:          numHeads,;
        headDim:           hiddenSize / numHeads,;
        numChannels:       numChannels,;
        patchSize:         patchSize,;
        temporalPatchSize: temporalPatchSize,;
        imageSize:         imageSize,;
        spatialMergeSize:  spatialMergeSize,;
        outHiddenSize:     outHiddenSize,;
        intermediateSize:  intermediateSize,;
        eps:               eps,;
        },;
    }
    }
}
