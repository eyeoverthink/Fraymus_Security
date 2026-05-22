package com.fraymus.absorbed.models.deepseekocr;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class visionModel {
        public *nn.Conv2D PatchEmbedding;
        public ml.Tensor ClassEmbedding;
        public *nn.Embedding PositionEmbedding;
        public *nn.LayerNorm PreLayerNorm;
        public []visionBlock Blocks;
        public visionOptions Options;
    }
        func (m *visionModel) absolutePositionEmbedding(ctx ml.Context, embeds ml.Tensor) ml.Tensor {
        var numPatches = m.Options.imageSize / m.Options.patchSize * m.Options.imageSize / m.Options.patchSize;
        var positions = ctx.Arange(0, float32(numPatches+1), 1, ml.DTypeI32);
        var positionEmbeds = m.PositionEmbedding.Forward(ctx, positions);
        var source = int(math.Sqrt(double(positionEmbeds.Dim(1) - 1)));
        var target = int(math.Sqrt(double(embeds.Dim(1) - 1)));
        if source != target {
        var newPositionEmbeds = positionEmbeds.Slice(ctx, 1, 1, positionEmbeds.Dim(1), 1);
        newPositionEmbeds = newPositionEmbeds.Reshape(ctx, -1, source, source);
        newPositionEmbeds = newPositionEmbeds.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        newPositionEmbeds = newPositionEmbeds.Interpolate(ctx, [4]int{target, target, embeds.Dim(0), 1}, ml.SamplingModeBilinear);
        newPositionEmbeds = newPositionEmbeds.Permute(ctx, 1, 2, 0, 3);
        newPositionEmbeds = newPositionEmbeds.Contiguous(ctx, -1, target*target);
        positionEmbeds = positionEmbeds.Slice(ctx, 1, 0, 1, 1).Concat(ctx, newPositionEmbeds, 1);
    }
        return positionEmbeds;
    }
        func (m *visionModel) Forward(ctx ml.Context, pixelValues, patchEmbeds ml.Tensor) ml.Tensor {
        if patchEmbeds == null {
        patchEmbeds = m.PatchEmbedding.Forward(ctx, pixelValues, m.Options.patchSize, m.Options.patchSize, 0, 0, 1, 1);
    }
        patchEmbeds = patchEmbeds.Reshape(ctx, -1, patchEmbeds.Dim(2), patchEmbeds.Dim(3));
        patchEmbeds = patchEmbeds.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var classEmbeds = m.ClassEmbedding.Repeat(ctx, 2, patchEmbeds.Dim(2));
        var embeds = classEmbeds.Concat(ctx, patchEmbeds, 1);
        embeds = embeds.Add(ctx, m.absolutePositionEmbedding(ctx, embeds));
        var hiddenStates = m.PreLayerNorm.Forward(ctx, embeds, m.Options.eps);
        var for _, block = range m.Blocks {
        hiddenStates = block.Forward(ctx, hiddenStates, m.Options);
    }
        return hiddenStates;
    }

    public static class visionOptions {
        public int numHeads;
        public float32 eps;
        public patchSize imageSize,;
    }
        func (o visionOptions) headDim() int {
        return o.hiddenSize / o.numHeads;
    }

    public static class visionBlock {
        public *nn.LayerNorm Norm1;
        public *visionAttention Attention;
        public *nn.LayerNorm Norm2;
        public *visionMLP FeedForward;
    }
        func (m *visionBlock) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts visionOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = m.Norm1.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.Attention.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = m.Norm2.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.FeedForward.Forward(ctx, hiddenStates);
        hiddenStates = hiddenStates.Add(ctx, residual);
        return hiddenStates;
    }

    public static class visionAttention {
        public *nn.Linear QKV;
        public *nn.Linear Output;
    }
        func (m *visionAttention) Forward(ctx ml.Context, t ml.Tensor, opts visionOptions) ml.Tensor {
        var qkv = m.QKV.Forward(ctx, t);
        qkv = qkv.Reshape(ctx, opts.headDim(), -1, qkv.Dim(1), qkv.Dim(2));
        var chunks = qkv.Chunk(ctx, 1, opts.numHeads);
        var query, key, value = chunks[0], chunks[1], chunks[2];
        var attention = nn.Attention(ctx, query, key, value, 1/math.Sqrt(double(opts.headDim())), null);
        attention = attention.Reshape(ctx, -1, attention.Dim(2), attention.Dim(3));
        return m.Output.Forward(ctx, attention);
    }

    public static class visionMLP {
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (m *visionMLP) Forward(ctx ml.Context, t ml.Tensor) ml.Tensor {
        return m.FC2.Forward(ctx, m.FC1.Forward(ctx, t).QuickGELU(ctx));
    }
}
