package com.fraymus.absorbed.models.mllama;

import java.util.*;
import java.io.*;

public class model_text {
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );

    public static class TextSelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public ml.Tensor RopeFactors;
    }
        func (sa *TextSelfAttention) Forward(ctx ml.Context, hiddenState, positions ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = opts.hiddenSize / opts.numHeads;
        var query = sa.Query.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, batchSize);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions, sa.RopeFactors);
        var key = sa.Key.Forward(ctx, hiddenState);
        key = key.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions, sa.RopeFactors);
        var value = sa.Value.Forward(ctx, hiddenState);
        value = value.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        var scaleFactor = 1.0 / math.Sqrt(double(headDim));
        var attention = nn.Attention(ctx, query, key, value, scaleFactor, cache);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        var if layer, ok = m.Transformer.Layers[layer].(*TextSelfAttentionDecoderLayer); ok {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift, layer.SelfAttention.RopeFactors), null;
    }
        return key, null;
    }

    public static class TextMLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *TextMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *TextModelOptions) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).SILU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class TextSelfAttentionDecoderLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextSelfAttention SelfAttention;
        public *nn.RMSNorm MLPNorm;
        public *TextMLP MLP;
    }
        func (d *TextSelfAttentionDecoderLayer) Forward(ctx ml.Context, hiddenState, positions, outputs, _, _ ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = d.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = d.SelfAttention.Forward(ctx, hiddenState, positions, cache, opts);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = d.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = d.MLP.Forward(ctx, hiddenState, opts);
        return hiddenState.Add(ctx, residual);
    }

    public static class TextCrossAttention {
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Query;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (ca *TextCrossAttention) Forward(ctx ml.Context, hiddenState, crossAttentionStates ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = opts.hiddenSize / opts.numHeads;
        var query = ca.Query.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, batchSize);
        query = ca.QueryNorm.Forward(ctx, query, opts.eps);
        var key, value ml.Tensor;
        if crossAttentionStates != null {
        var numVisionTokens, numTiles = crossAttentionStates.Dim(1), crossAttentionStates.Dim(2);
        key = ca.Key.Forward(ctx, crossAttentionStates);
        key = key.Reshape(ctx, headDim, opts.numKVHeads, numVisionTokens*numTiles);
        key = ca.KeyNorm.Forward(ctx, key, opts.eps);
        value = ca.Value.Forward(ctx, crossAttentionStates);
        value = value.Reshape(ctx, headDim, opts.numKVHeads, numVisionTokens*numTiles);
        cache.Put(ctx, key, value);
    }
        key, value, _ = cache.Get(ctx);
        var scaleFactor = 1.0 / math.Sqrt(double(headDim));
        query = query.Permute(ctx, 0, 2, 1, 3);
        key = key.Permute(ctx, 0, 2, 1, 3);
        value = value.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var kq = key.MulmatFullPrec(ctx, query);
        kq = kq.Scale(ctx, scaleFactor);
        kq = kq.Softmax(ctx);
        var kqv = value.Mulmat(ctx, kq);
        var attention = kqv.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return ca.Output.Forward(ctx, attention);
    }

    public static class TextCrossAttentionDecoderLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextCrossAttention CrossAttention;
        public ml.Tensor AttentionGate;
        public *nn.RMSNorm MLPNorm;
        public *TextMLP MLP;
        public ml.Tensor MLPGate;
    }
        func (d *TextCrossAttentionDecoderLayer) Forward(ctx ml.Context, hiddenState, _, _, crossAttentionStates, crossAttentionMask ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = d.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = d.CrossAttention.Forward(ctx, hiddenState, crossAttentionStates, cache, opts);
        hiddenState = hiddenState.Mul(ctx, d.AttentionGate.Tanh(ctx));
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = d.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = d.MLP.Forward(ctx, hiddenState, opts);
        hiddenState = hiddenState.Mul(ctx, d.MLPGate.Tanh(ctx));
        return hiddenState.Add(ctx, residual);
    }
        type TextDecoderLayer interface {
        Forward(ctx ml.Context, hiddenState, positionIDs, outputs, crossAttentionStates, crossAttentionMask ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor;
    }

    public static class TextDecoder {
        public []TextDecoderLayer Layers;
    }
        func (d *TextDecoder) Forward(ctx ml.Context, hiddenState, positionIDs, outputs, crossAttentionStates, crossAttentionMask ml.Tensor, cache *kvcache.WrapperCache, opts *TextModelOptions) ml.Tensor {
        var for i, layer = range d.Layers {
        var layerType = selfAttentionLayer;
        if slices.Contains(opts.crossAttentionLayers, int32(i)) {
        layerType = crossAttentionLayer;
    }
        cache.SetLayer(i);
        cache.SetLayerType(layerType);
        if layerType == selfAttentionLayer || crossAttentionStates != null || cache.UnderlyingCache().(*kvcache.EncoderCache).EncoderCached() {
        var lastLayerOutputs ml.Tensor;
        if i == len(d.Layers)-1 {
        lastLayerOutputs = outputs;
    }
        hiddenState = layer.Forward(ctx, hiddenState, positionIDs, lastLayerOutputs, crossAttentionStates, crossAttentionMask, cache, opts);
    }
    }
        return hiddenState;
    }

    public static class TextModelOptions {
        public numHeads, hiddenSize,;
        public int ropeDim;
        public ropeBase, eps,;
        public []int32 crossAttentionLayers;
    }
        func (o TextModelOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions, factors ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.ropeDim, o.ropeBase, 1./o.ropeScale, rope.WithFactors(factors));
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public *TextDecoder Transformer;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        func (m *TextModel) Forward(ctx ml.Context, inputIDs, positionIDs, outputs, crossAttentionStates, crossAttentionMask ml.Tensor, cache *kvcache.WrapperCache) ml.Tensor {
        var hiddenState = m.TokenEmbedding.Forward(ctx, inputIDs);
        hiddenState = m.Transformer.Forward(ctx, hiddenState, positionIDs, outputs, crossAttentionStates, crossAttentionMask, cache, m.TextModelOptions);
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        return m.Output.Forward(ctx, hiddenState);
    }
        func newTextModel(c fs.Config) *TextModel {
        var decoderLayers []TextDecoderLayer;
        var for i = range c.Uint("block_count") {
        var textDecoderLayer TextDecoderLayer;
        if slices.Contains(c.Ints("attention.cross_attention_layers"), int32(i)) {
        textDecoderLayer = &TextCrossAttentionDecoderLayer{}
        } else {
        textDecoderLayer = &TextSelfAttentionDecoderLayer{}
    }
        decoderLayers = append(decoderLayers, textDecoderLayer);
    }
        return &TextModel{
        Transformer: &TextDecoder{Layers: decoderLayers},;
        TextModelOptions: &TextModelOptions{
        hiddenSize:           int(c.Uint("embedding_length")),;
        numHeads:             int(c.Uint("attention.head_count")),;
        numKVHeads:           int(c.Uint("attention.head_count_kv")),;
        ropeDim:              int(c.Uint("rope.dimension_count")),;
        eps:                  c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:             c.Float("rope.freq_base"),;
        ropeScale:            c.Float("rope.scaling.factor", 1),;
        crossAttentionLayers: c.Ints("attention.cross_attention_layers"),;
        },;
    }
    }
}
