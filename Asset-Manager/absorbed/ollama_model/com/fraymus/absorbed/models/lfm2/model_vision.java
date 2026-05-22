package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );
        const lfm2VisionBatchSize = 1;

    public static class visionPatchGrid {
        public int Width;
        public int Height;
    }

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
        query = query.Reshape(ctx, headDim, opts.numHeads, query.Dim(1), lfm2VisionBatchSize);
        key = key.Reshape(ctx, headDim, opts.numHeads, key.Dim(1), lfm2VisionBatchSize);
        value = value.Reshape(ctx, headDim, opts.numHeads, value.Dim(1), lfm2VisionBatchSize);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), lfm2VisionBatchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenState ml.Tensor) ml.Tensor {
        return mlp.Down.Forward(ctx, mlp.Up.Forward(ctx, hiddenState).GELU(ctx));
    }

    public static class VisionEncoderLayer {
        public *nn.LayerNorm LayerNorm1;
        public *VisionSelfAttention SelfAttention;
        public *nn.LayerNorm LayerNorm2;
        public *VisionMLP MLP;
    }
        func (l *VisionEncoderLayer) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.LayerNorm1.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, opts);
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = l.LayerNorm2.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.MLP.Forward(ctx, hiddenState);
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
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor, patches visionPatchGrid) ml.Tensor {
        var numPatches = patches.Width * patches.Height;
        var hiddenState = m.PatchEmbedding.Forward(ctx, pixelValues, m.patchSize, m.patchSize, 0, 0, 1, 1);
        hiddenState = hiddenState.Reshape(ctx, numPatches, m.hiddenSize);
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        if m.PositionEmbedding != null {
        var posTokens = m.PositionEmbedding.Weight.Dim(1);
        var source = int(math.Sqrt(double(posTokens)));
        var positionEmbeddings ml.Tensor;
        if source > 0 && source*source == posTokens && (source != patches.Width || source != patches.Height) {
        var positionIDs = ctx.Arange(0, float32(posTokens), 1, ml.DTypeI32);
        positionEmbeddings = m.PositionEmbedding.Forward(ctx, positionIDs);
        positionEmbeddings = positionEmbeddings.Reshape(ctx, -1, source, source);
        positionEmbeddings = positionEmbeddings.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        positionEmbeddings = positionEmbeddings.Interpolate(ctx, [4]int{
        patches.Width,;
        patches.Height,;
        hiddenState.Dim(0),;
        1,;
        }, ml.SamplingModeBilinear);
        positionEmbeddings = positionEmbeddings.Permute(ctx, 1, 2, 0, 3);
        positionEmbeddings = positionEmbeddings.Contiguous(ctx, -1, patches.Width*patches.Height);
        } else {
        var positionIDs = ctx.Arange(0, float32(numPatches), 1, ml.DTypeI32);
        positionEmbeddings = m.PositionEmbedding.Forward(ctx, positionIDs);
    }
        hiddenState = hiddenState.Add(ctx, positionEmbeddings);
    }
        var for _, layer = range m.Layers {
        hiddenState = layer.Forward(ctx, hiddenState, m.VisionModelOptions);
    }
        return m.PostLayerNorm.Forward(ctx, hiddenState, m.eps);
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count")),;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize: int(c.Uint("vision.embedding_length", 1152)),;
        numHeads:   int(c.Uint("vision.attention.head_count", 16)),;
        imageSize:  int(c.Uint("vision.image_size", 256)),;
        patchSize:  int(c.Uint("vision.patch_size", 16)),;
        eps:        c.Float("vision.attention.layer_norm_epsilon", 1e-6),;
        },;
    }
    }

    public static class VisionProjector {
        public *nn.LayerNorm LayerNorm;
        public *nn.Linear Linear1;
        public *nn.Linear Linear2;
    }

    public static class VisionProjectorOptions {
        public int scaleFactor;
        public boolean useLayerNorm;
    }
        func (p *VisionProjector) Forward(ctx ml.Context, visionOutputs ml.Tensor, patches visionPatchGrid, opts VisionProjectorOptions) ml.Tensor {
        var hiddenSize = visionOutputs.Dim(0);
        var featureMap = visionOutputs;
        var merge = max(opts.scaleFactor, 1);
        if merge > 1 {
        var width = patches.Width;
        var height = patches.Height;
        featureMap = featureMap.Reshape(ctx, hiddenSize, width, height);
        var padWidth = (merge - width%merge) % merge;
        var padHeight = (merge - height%merge) % merge;
        if padWidth != 0 || padHeight != 0 {
        featureMap = featureMap.Pad(ctx, 0, padWidth, padHeight, 0);
        width += padWidth;
        height += padHeight;
    }
        featureMap = featureMap.Reshape(ctx, hiddenSize*merge, width/merge, height);
        featureMap = featureMap.Permute(ctx, 0, 2, 1).Contiguous(ctx, hiddenSize*merge*merge, height/merge, width/merge);
        featureMap = featureMap.Permute(ctx, 0, 2, 1).Contiguous(ctx);
        featureMap = featureMap.Contiguous(ctx, featureMap.Dim(0), featureMap.Dim(1)*featureMap.Dim(2));
    }
        if opts.useLayerNorm && p.LayerNorm != null {
        featureMap = p.LayerNorm.Forward(ctx, featureMap, 1e-5);
    }
        featureMap = p.Linear1.Forward(ctx, featureMap).GELU(ctx);
        return p.Linear2.Forward(ctx, featureMap);
    }
}
