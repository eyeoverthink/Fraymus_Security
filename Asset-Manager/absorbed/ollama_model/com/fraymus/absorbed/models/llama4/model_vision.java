package com.fraymus.absorbed.models.llama4;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class VisionAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func applyVisionRotaryEmbedding(ctx ml.Context, t, cos, sin ml.Tensor) ml.Tensor {
        var width, height, channels, tiles = t.Dim(0), t.Dim(1), t.Dim(2), t.Dim(3);
        var t1 = t.Slice(ctx, 0, 0, t.Dim(0), 2);
        var t2 = t.Slice(ctx, 0, 1, t.Dim(0), 2);
        var cosOut = t1.Mul(ctx, cos).Concat(ctx, t2.Mul(ctx, cos), 0);
        cosOut = cosOut.Reshape(ctx, cosOut.Dim(0)/2, 2, -1);
        cosOut = cosOut.Permute(ctx, 1, 0, 2, 3);
        cosOut = cosOut.Contiguous(ctx, width, height, channels, tiles);
        var sinOut = t2.Scale(ctx, -1).Mul(ctx, sin).Concat(ctx, t1.Mul(ctx, sin), 0);
        sinOut = sinOut.Reshape(ctx, sinOut.Dim(0)/2, 2, -1);
        sinOut = sinOut.Permute(ctx, 1, 0, 2, 3);
        sinOut = sinOut.Contiguous(ctx, width, height, channels, tiles);
        return cosOut.Add(ctx, sinOut);
    }
        func (sa *VisionAttention) Forward(ctx ml.Context, hiddenState, cos, sin ml.Tensor, opts *VisionOptions) ml.Tensor {
        var headDim = opts.hiddenSize / opts.numHeads;
        var query = sa.Query.Forward(ctx, hiddenState);
        var key = sa.Key.Forward(ctx, hiddenState);
        var value = sa.Value.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, query.Dim(1), query.Dim(2));
        key = key.Reshape(ctx, headDim, opts.numHeads, key.Dim(1), key.Dim(2));
        value = value.Reshape(ctx, headDim, opts.numHeads, value.Dim(1), value.Dim(2));
        query = applyVisionRotaryEmbedding(ctx, query, cos, sin);
        key = applyVisionRotaryEmbedding(ctx, key, cos, sin);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), attention.Dim(3));
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *VisionOptions) ml.Tensor {
        hiddenStates = mlp.FC1.Forward(ctx, hiddenStates).GELU(ctx);
        hiddenStates = mlp.FC2.Forward(ctx, hiddenStates);
        return hiddenStates;
    }

    public static class VisionLayer {
        public *nn.LayerNorm InputLayerNorm;
        public *nn.LayerNorm PostAttentionNorm;
        public `gguf:"mlp"` *VisionMLP;
    }
        func (e *VisionLayer) Forward(ctx ml.Context, hiddenStates, cos, sin ml.Tensor, opts *VisionOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.InputLayerNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.VisionAttention.Forward(ctx, hiddenStates, cos, sin, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = e.PostAttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.VisionMLP.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        return hiddenStates;
    }

    public static class VisionAdapter {
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (a *VisionAdapter) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *VisionOptions) ml.Tensor {
        var patches = hiddenStates.Dim(1);
        var patchSize = int(math.Sqrt(double(patches)));
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), patchSize, patchSize, hiddenStates.Dim(2));
        var channels, width, height, tiles = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2), hiddenStates.Dim(3);
        channels, width = int(float32(channels)/opts.pixelShuffleRatio), int(float32(width)*opts.pixelShuffleRatio);
        hiddenStates = hiddenStates.Reshape(ctx, channels, width, height, tiles);
        hiddenStates = hiddenStates.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        channels, height = int(float32(channels)/opts.pixelShuffleRatio), int(float32(height)*opts.pixelShuffleRatio);
        hiddenStates = hiddenStates.Reshape(ctx, channels, width, height, tiles);
        hiddenStates = hiddenStates.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        hiddenStates = hiddenStates.Reshape(ctx, channels, width*height, tiles);
        hiddenStates = a.FC1.Forward(ctx, hiddenStates).GELU(ctx);
        hiddenStates = a.FC2.Forward(ctx, hiddenStates).GELU(ctx);
        return hiddenStates;
    }

    public static class VisionOptions {
        public numHeads hiddenSize,;
        public patchSize imageSize,;
        public float32 ropeTheta;
        public float32 eps;
        public float32 pixelShuffleRatio;
    }

    public static class PatchEmbedding {
    }
        func (p *PatchEmbedding) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *VisionOptions) ml.Tensor {
        var kernel = ctx.Input().Empty(ml.DTypeF32, opts.patchSize, opts.patchSize, hiddenStates.Dim(2));
        hiddenStates = kernel.IM2Col(ctx, hiddenStates, opts.patchSize, opts.patchSize, 0, 0, 1, 1);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), hiddenStates.Dim(1)*hiddenStates.Dim(2), hiddenStates.Dim(3));
        return p.Linear.Forward(ctx, hiddenStates);
    }

    public static class VisionModel {
        public []VisionLayer Layers;
        public `gguf:"patch_embedding"` *PatchEmbedding;
        public ml.Tensor ClassEmbedding;
        public ml.Tensor PositionalEmbedding;
        public *nn.LayerNorm LayerNormPre;
        public *nn.LayerNorm LayerNormPost;
        public `gguf:"vision_adapter"` *VisionAdapter;
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Layers: make([]VisionLayer, c.Uint("vision.block_count")),;
        VisionOptions: &VisionOptions{
        hiddenSize:        int(c.Uint("vision.embedding_length")),;
        numHeads:          int(c.Uint("vision.attention.head_count")),;
        imageSize:         int(c.Uint("vision.image_size")),;
        patchSize:         int(c.Uint("vision.patch_size")),;
        ropeTheta:         float32(c.Float("vision.rope.freq_base")),;
        eps:               c.Float("vision.layer_norm_epsilon"),;
        pixelShuffleRatio: float32(c.Float("vision.pixel_shuffle_ratio")),;
        },;
    }
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor) ml.Tensor {
        var hiddenStates = m.PatchEmbedding.Forward(ctx, pixelValues, m.VisionOptions);
        hiddenStates = hiddenStates.Concat(ctx, m.ClassEmbedding.Repeat(ctx, 2, hiddenStates.Dim(2)), 1);
        hiddenStates = hiddenStates.Add(ctx, m.PositionalEmbedding);
        hiddenStates = m.LayerNormPre.Forward(ctx, hiddenStates, m.eps);
        var cos, sin = m.rotaryEmbedding(ctx);
        var for _, layer = range m.Layers {
        hiddenStates = layer.Forward(ctx, hiddenStates, cos, sin, m.VisionOptions);
    }
        hiddenStates = m.LayerNormPost.Forward(ctx, hiddenStates, m.eps);
        hiddenStates = hiddenStates.Pad(ctx, 0, -1, 0, 0);
        hiddenStates = m.VisionAdapter.Forward(ctx, hiddenStates, m.VisionOptions);
        return hiddenStates;
    }
        func floorDiv[T int | int16 | int32 | long | uint | uint16 | uint32 | uint64](a, b T) T {
        if b == 0 {
        panic("division by zero");
    }
        if (a >= 0 && b > 0) || (a <= 0 && b < 0) || a%b == 0 {
        return a / b;
    }
        return a/b - 1;
    }
        func (m *VisionModel) rotaryEmbedding(ctx ml.Context) (ml.Tensor, ml.Tensor) {
        var patchesPerSide = m.imageSize / m.patchSize;
        var numPatches = patchesPerSide*patchesPerSide + 1;
        var headDim = m.hiddenSize / m.numHeads;
        var freqDim = headDim / 2;
        var freqs = make([]float32, numPatches*freqDim);
        var for i = range numPatches - 1 {
        var for j = 0; j < freqDim; j += 2 {
        var positionX = i*freqDim/2 + j/2;
        var positionY = (i+numPatches)*freqDim/2 + j/2;
        var ropeFreq = math.Pow(double(m.ropeTheta), double(j)*2/double(headDim));
        freqs[positionX] = float32(double(1+i-floorDiv(i, patchesPerSide)*patchesPerSide) / ropeFreq);
        freqs[positionY] = float32(double(1+floorDiv(i, patchesPerSide)) / ropeFreq);
    }
    }
        var ropeFreqs = ctx.Input().FromFloats(freqs, freqDim/2, numPatches, 2);
        ropeFreqs = ropeFreqs.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        ropeFreqs = ropeFreqs.Reshape(ctx, freqDim, 1, numPatches);
        return ropeFreqs.Cos(ctx), ropeFreqs.Sin(ctx);
    }
}
