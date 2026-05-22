package com.fraymus.absorbed.models.mistral3;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );
        var batchSize int = 1;
        func rotateHalf(ctx ml.Context, t ml.Tensor) ml.Tensor {
        var x1 = t.Slice(ctx, 0, 0, t.Dim(0)/2, 1);
        var x2 = t.Slice(ctx, 0, t.Dim(0)/2, t.Dim(0), 1).Contiguous(ctx);
        return x2.Scale(ctx, -1).Concat(ctx, x1, 0);
    }
        func applyRotaryPositionEmbeddings(ctx ml.Context, states, cos, sin ml.Tensor) ml.Tensor {
        return states.Mul(ctx, cos).Add(ctx, rotateHalf(ctx, states).Mul(ctx, sin));
    }

    public static class VisionSelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *VisionSelfAttention) Forward(ctx ml.Context, hiddenStates, cos, sin ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var query = sa.Query.Forward(ctx, hiddenStates);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim, opts.numHeads, query.Dim(1), batchSize);
        key = key.Reshape(ctx, opts.headDim, opts.numHeads, key.Dim(1), batchSize);
        value = value.Reshape(ctx, opts.headDim, opts.numHeads, value.Dim(1), batchSize);
        query = applyRotaryPositionEmbeddings(ctx, query, cos, sin);
        key = applyRotaryPositionEmbeddings(ctx, key, cos, sin);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(opts.headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), batchSize);
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
        public *nn.RMSNorm AttentionNorm;
        public *VisionSelfAttention SelfAttention;
        public *nn.RMSNorm FFNNorm;
        public *VisionMLP MLP;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenStates, cos, sin ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.SelfAttention.Forward(ctx, hiddenStates, cos, sin, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = e.FFNNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.MLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class VisionModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int headDim;
        public int intermediateSize;
        public int imageSize;
        public int patchSize;
        public int numChannels;
        public float32 eps;
        public float32 ropeBase;
    }

    public static class VisionModel {
        public *nn.Conv2D PatchEmbedding;
        public *nn.RMSNorm EncoderNorm;
        public []VisionEncoderLayer Layers;
    }
        func (m *VisionModel) positionalEmbedding(ctx ml.Context, positionIDs ml.Tensor) ml.Tensor {
        var maxPatchesPerSide = m.imageSize / m.patchSize;
        var frequencies = m.headDim / 2;
        var frequenciesHeight = make([]float32, frequencies/2*maxPatchesPerSide);
        var frequenciesWidth = make([]float32, frequencies/2*maxPatchesPerSide);
        var for i = range frequencies {
        var for j = range maxPatchesPerSide {
        var frequency = float32(j) / float32(math.Pow(double(m.ropeBase), double(i)*2/double(m.headDim)));
        if i%2 == 0 {
        frequenciesHeight[i/2*maxPatchesPerSide+j] = frequency;
        } else {
        frequenciesWidth[i/2*maxPatchesPerSide+j] = frequency;
    }
    }
    }
        var h = ctx.Input().FromFloats(frequenciesHeight, maxPatchesPerSide, frequencies/2);
        var w = ctx.Input().FromFloats(frequenciesWidth, maxPatchesPerSide, frequencies/2);
        h = h.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        w = w.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        h = h.Repeat(ctx, 1, maxPatchesPerSide);
        h = h.Reshape(ctx, frequencies/2, maxPatchesPerSide, maxPatchesPerSide).Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        w = w.Repeat(ctx, 2, maxPatchesPerSide);
        var inverseFrequencies = h.Concat(ctx, w, 0).Reshape(ctx, frequencies, maxPatchesPerSide*maxPatchesPerSide);
        inverseFrequencies = inverseFrequencies.Concat(ctx, inverseFrequencies, 0);
        return inverseFrequencies.Rows(ctx, positionIDs);
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor) ml.Tensor {
        var numPatchesW = pixelValues.Dim(0) / m.patchSize;
        var numPatchesH = pixelValues.Dim(1) / m.patchSize;
        var numPatches = numPatchesW * numPatchesH;
        var hiddenStates = m.PatchEmbedding.Forward(ctx, pixelValues, m.patchSize, m.patchSize, 0, 0, 1, 1);
        hiddenStates = hiddenStates.Reshape(ctx, numPatches, m.hiddenSize);
        hiddenStates = hiddenStates.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        hiddenStates = m.EncoderNorm.Forward(ctx, hiddenStates, m.VisionModelOptions.eps);
        var positions = make([]int32, numPatches);
        var for h = range numPatchesH {
        var for w = range numPatchesW {
        var idx = h*numPatchesW + w;
        positions[idx] = int32(h*m.imageSize/m.patchSize + w);
    }
    }
        var positionIDs = ctx.Input().FromInts(positions, len(positions));
        var positionEmbedding = m.positionalEmbedding(ctx, positionIDs);
        var cos, sin = positionEmbedding.Cos(ctx), positionEmbedding.Sin(ctx);
        cos = cos.Reshape(ctx, cos.Dim(0), 1, cos.Dim(1));
        sin = sin.Reshape(ctx, sin.Dim(0), 1, sin.Dim(1));
        var for _, layer = range m.Layers {
        hiddenStates = layer.Forward(ctx, hiddenStates, cos, sin, m.VisionModelOptions);
    }
        return hiddenStates;
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count")),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize:       int(c.Uint("vision.embedding_length", 1024)),;
        numHeads:         int(c.Uint("vision.attention.head_count", 16)),;
        headDim:          int(c.Uint("vision.attention.key_length", 64)),;
        intermediateSize: int(c.Uint("vision.feed_forward_length", 4096)),;
        imageSize:        int(c.Uint("vision.image_size", 1540)),;
        patchSize:        int(c.Uint("vision.patch_size", 14)),;
        numChannels:      int(c.Uint("vision.num_channels", 3)),;
        eps:              c.Float("vision.attention.layer_norm_epsilon", 1e-5),;
        ropeBase:         c.Float("vision.rope.freq_base", 10000.0),;
        },;
    }
    }
}
