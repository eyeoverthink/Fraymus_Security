package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );
        var batchSize int = 1;

    public static class VisionSelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *VisionSelfAttention) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var headDim = opts.hiddenSize / opts.numHeads;
        var query = sa.Query.Forward(ctx, hiddenState);
        var key = sa.Key.Forward(ctx, hiddenState);
        var value = sa.Value.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, query.Dim(1), batchSize);
        key = key.Reshape(ctx, headDim, opts.numHeads, key.Dim(1), batchSize);
        value = value.Reshape(ctx, headDim, opts.numHeads, value.Dim(1), batchSize);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), batchSize);
        hiddenState = sa.Output.Forward(ctx, attention);
        return hiddenState;
    }

    public static class VisionMLP {
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        hiddenState = mlp.FC1.Forward(ctx, hiddenState).GELU(ctx);
        hiddenState = mlp.FC2.Forward(ctx, hiddenState);
        return hiddenState;
    }

    public static class VisionEncoderLayer {
        public *nn.LayerNorm LayerNorm1;
        public *VisionSelfAttention SelfAttention;
        public *nn.LayerNorm LayerNorm2;
        public *VisionMLP MLP;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = e.LayerNorm1.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.SelfAttention.Forward(ctx, hiddenState, opts);
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = e.LayerNorm2.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.MLP.Forward(ctx, hiddenState, opts);
        return hiddenState.Add(ctx, residual);
    }

    public static class VisionModelOptions {
        public numHeads hiddenSize,;
        public patchSize imageSize,;
        public float32 eps;
    }

    public static class VisionModel {
        public *nn.Conv2D PatchEmbedding;
        public *nn.Embedding PositionEmbedding;
        public *nn.LayerNorm PostLayerNorm;
        public []VisionEncoderLayer Layers;
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor) ml.Tensor {
        var numPatches = (m.imageSize / m.patchSize) * (m.imageSize / m.patchSize);
        var hiddenState = m.PatchEmbedding.Forward(ctx, pixelValues, m.patchSize, m.patchSize, 0, 0, 1, 1);
        hiddenState = hiddenState.Reshape(ctx, numPatches, m.hiddenSize);
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var positionIDs = ctx.Arange(0, float32(numPatches), 1, ml.DTypeI32);
        hiddenState = hiddenState.Add(ctx, m.PositionEmbedding.Forward(ctx, positionIDs));
        var for _, layer = range m.Layers {
        hiddenState = layer.Forward(ctx, hiddenState, m.VisionModelOptions);
    }
        hiddenState = m.PostLayerNorm.Forward(ctx, hiddenState, m.eps);
        return hiddenState;
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count")),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize: int(c.Uint("vision.embedding_length")),;
        numHeads:   int(c.Uint("vision.attention.head_count")),;
        imageSize: int(c.Uint("vision.image_size")),;
        patchSize: int(c.Uint("vision.patch_size")),;
        eps: c.Float("vision.attention.layer_norm_epsilon"),;
        },;
    }
    }
}
