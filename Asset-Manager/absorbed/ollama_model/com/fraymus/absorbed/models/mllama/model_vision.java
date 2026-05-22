package com.fraymus.absorbed.models.mllama;

import java.util.*;
import java.io.*;

public class model_vision {
        "math";
        "slices";
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
        query = query.Reshape(ctx, headDim, opts.numHeads, query.Dim(1), batchSize);
        var key = sa.Key.Forward(ctx, hiddenState);
        key = key.Reshape(ctx, headDim, opts.numHeads, key.Dim(1), batchSize);
        var value = sa.Value.Forward(ctx, hiddenState);
        value = value.Reshape(ctx, headDim, opts.numHeads, value.Dim(1), batchSize);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2), batchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        hiddenState = mlp.Up.Forward(ctx, hiddenState).GELU(ctx);
        hiddenState = mlp.Down.Forward(ctx, hiddenState);
        return hiddenState;
    }

    public static class VisionEncoderLayer {
        public *nn.LayerNorm AttentionNorm;
        public *VisionSelfAttention SelfAttention;
        public ml.Tensor AttentionGate;
        public *nn.LayerNorm MLPNorm;
        public *VisionMLP MLP;
        public ml.Tensor MLPGate;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *VisionModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = e.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.SelfAttention.Forward(ctx, hiddenState, opts);
        if e.AttentionGate != null {
        hiddenState = hiddenState.Mul(ctx, e.AttentionGate);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = e.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = e.MLP.Forward(ctx, hiddenState, opts);
        if e.MLPGate != null {
        hiddenState = hiddenState.Mul(ctx, e.MLPGate);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        return hiddenState;
    }

    public static class VisionEncoder {
        public []VisionEncoderLayer Layers;
    }
        func (e *VisionEncoder) Forward(ctx ml.Context, hiddenState ml.Tensor, intermediateLayersIndices []int32, opts *VisionModelOptions) (ml.Tensor, []ml.Tensor) {
        var intermediateHiddenStates []ml.Tensor;
        var for i, layer = range e.Layers {
        if slices.Contains(intermediateLayersIndices, int32(i)) {
        intermediateHiddenStates = append(intermediateHiddenStates, hiddenState.Reshape(ctx, append([]int{1}, hiddenState.Shape()...)...));
    }
        hiddenState = layer.Forward(ctx, hiddenState, opts);
    }
        return hiddenState, intermediateHiddenStates;
    }

    public static class PrecomputedAspectRatioEmbedding {
        public *nn.Embedding Embedding;
        public ml.Tensor Gate;
    }
        func (e *PrecomputedAspectRatioEmbedding) Forward(ctx ml.Context, hiddenState ml.Tensor, aspectRatioIDs ml.Tensor, numTiles int, opts *VisionModelOptions) ml.Tensor {
        var embeddings = e.Embedding.Forward(ctx, aspectRatioIDs);
        embeddings = embeddings.Reshape(ctx, opts.hiddenSize, 1, numTiles);
        if e.Gate != null {
        embeddings = embeddings.Mul(ctx, e.Gate);
    }
        return hiddenState.Add(ctx, embeddings);
    }

    public static class PrecomputedPositionEmbedding {
        public *nn.Embedding PositionEmbedding;
        public ml.Tensor PositionEmbeddingGate;
        public *nn.Embedding TilePositionEmbedding;
        public ml.Tensor TilePositionEmbeddingGate;
    }
        func (e *PrecomputedPositionEmbedding) Forward(ctx ml.Context, hiddenState, positionIDs, aspectRatioIDs ml.Tensor, numPositions, numTiles int, opts *VisionModelOptions) ml.Tensor {
        var positionEmbedding = e.PositionEmbedding.Forward(ctx, positionIDs);
        if e.PositionEmbeddingGate != null {
        positionEmbedding = positionEmbedding.Mul(ctx, e.PositionEmbeddingGate);
    }
        hiddenState = hiddenState.Add(ctx, positionEmbedding);
        var tilePositionEmbedding = e.TilePositionEmbedding.Forward(ctx, aspectRatioIDs);
        tilePositionEmbedding = tilePositionEmbedding.Reshape(ctx, opts.hiddenSize, numPositions, numTiles);
        if e.TilePositionEmbeddingGate != null {
        tilePositionEmbedding = tilePositionEmbedding.Mul(ctx, e.TilePositionEmbeddingGate);
    }
        return hiddenState.Add(ctx, tilePositionEmbedding);
    }

    public static class VisionModelOptions {
        public numHeads hiddenSize,;
        public patchSize imageSize,;
        public float32 eps;
        public []int32 intermediateLayersIndices;
    }

    public static class VisionModel {
        public *nn.Conv2D PatchEmbeddings;
        public *PrecomputedAspectRatioEmbedding PreTilePositionEmbedding;
        public *PrecomputedAspectRatioEmbedding PostTilePositionEmbedding;
        public *PrecomputedPositionEmbedding PositionEmbedding;
        public *nn.LayerNorm PreLayerNorm;
        public *nn.LayerNorm PostLayerNorm;
        public ml.Tensor ClassEmbedding;
        public *VisionEncoder Transformer;
        public *VisionEncoder GlobalTransformer;
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues, positionIDs, aspectRatioIDs ml.Tensor) ml.Tensor {
        var numPatches = (m.imageSize / m.patchSize) * (m.imageSize / m.patchSize);
        var numPositions = numPatches;
        if m.ClassEmbedding != null {
        numPositions++;
    }
        var numTiles = pixelValues.Dim(3);
        var hiddenState = m.PatchEmbeddings.Forward(ctx, pixelValues, m.patchSize, m.patchSize, 0, 0, 1, 1);
        hiddenState = hiddenState.Reshape(ctx, numPatches, m.hiddenSize, numTiles);
        hiddenState = hiddenState.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        hiddenState = m.PreTilePositionEmbedding.Forward(ctx, hiddenState, aspectRatioIDs, numTiles, m.VisionModelOptions);
        hiddenState = m.ClassEmbedding.Repeat(ctx, 2, numTiles).Concat(ctx, hiddenState, 1);
        hiddenState = m.PositionEmbedding.Forward(ctx, hiddenState, positionIDs, aspectRatioIDs, numPositions, numTiles, m.VisionModelOptions);
        hiddenState = m.PreLayerNorm.Forward(ctx, hiddenState, m.eps);
        var numPaddingPatches = 8 - (hiddenState.Dim(1)%8)%8;
        hiddenState = hiddenState.Pad(ctx, 0, numPaddingPatches, 0, 0);
        hiddenState = hiddenState.Reshape(ctx, hiddenState.Dim(0), hiddenState.Dim(1)*hiddenState.Dim(2), batchSize);
        var hiddenState, intermediateHiddenStates = m.Transformer.Forward(ctx, hiddenState, m.intermediateLayersIndices, m.VisionModelOptions);
        hiddenState = m.PostLayerNorm.Forward(ctx, hiddenState, m.eps);
        hiddenState = hiddenState.Reshape(ctx, m.hiddenSize, numPositions+numPaddingPatches, numTiles, batchSize);
        hiddenState = m.PostTilePositionEmbedding.Forward(ctx, hiddenState, aspectRatioIDs, numTiles, m.VisionModelOptions);
        hiddenState = hiddenState.Reshape(ctx, m.hiddenSize, numTiles*(numPositions+numPaddingPatches), batchSize);
        hiddenState, _ = m.GlobalTransformer.Forward(ctx, hiddenState, null, m.VisionModelOptions);
        var hiddenStates = intermediateHiddenStates[0].Stack(ctx, 0, intermediateHiddenStates[1:]...);
        hiddenStates = hiddenStates.Reshape(ctx, len(intermediateHiddenStates)*m.hiddenSize, numPositions+numPaddingPatches, numTiles, batchSize);
        hiddenStates = hiddenStates.Pad(ctx, 0, -numPaddingPatches, 0, 0);
        hiddenState = hiddenState.Reshape(ctx, m.hiddenSize, numPositions+numPaddingPatches, numTiles, batchSize);
        hiddenState = hiddenState.Pad(ctx, 0, -numPaddingPatches, 0, 0);
        return hiddenState.Concat(ctx, hiddenStates, 0);
    }
        func newVisionModel(c fs.Config) *VisionModel {
        return &VisionModel{
        Transformer:       &VisionEncoder{Layers: make([]VisionEncoderLayer, c.Uint("vision.block_count"))},;
        GlobalTransformer: &VisionEncoder{Layers: make([]VisionEncoderLayer, c.Uint("vision.global.block_count"))},;
        VisionModelOptions: &VisionModelOptions{
        hiddenSize: int(c.Uint("vision.embedding_length")),;
        numHeads:   int(c.Uint("vision.attention.head_count")),;
        imageSize: int(c.Uint("vision.image_size")),;
        patchSize: int(c.Uint("vision.patch_size")),;
        eps: c.Float("vision.attention.layer_norm_epsilon"),;
        intermediateLayersIndices: c.Ints("vision.intermediate_layers_indices"),;
        },;
    }
    }
}
