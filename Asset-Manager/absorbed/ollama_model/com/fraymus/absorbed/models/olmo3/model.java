package com.fraymus.absorbed.models.olmo3;

import java.util.*;
import java.io.*;

public class model {
        "fmt";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );
        const (;
        cacheTypeSWA    = 0;
        cacheTypeCausal = 1;
        );

    public static class Options {
        public numHeads, hiddenSize,;
        public ropeBase, eps,;
        public int originalContextLength;
        public float32 attnFactor;
        public String ropeType;
        public float32 ropeExtrapolation;
        public []boolean slidingWindowPattern;
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }

    public static void New() {
        var m = Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", false),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        "(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}{1,3}| ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",;
        ),;
        Layers: make([]Layer, c.Uint("block_count")),;
        Options: Options{
        hiddenSize:            int(c.Uint("embedding_length")),;
        numHeads:              int(c.Uint("attention.head_count")),;
        numKVHeads:            int(c.Uint("attention.head_count_kv")),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:              c.Float("rope.freq_base", 1e4),;
        ropeScale:             c.Float("rope.scaling.factor", 1),;
        originalContextLength: int(c.Uint("rope.scaling.original_context_length")),;
        attnFactor:            c.Float("rope.scaling.attn_factor", 1),;
        ropeType:              c.String("rope.scaling.type"),;
        ropeExtrapolation:     c.Float("rope.scaling.extrapolation_factor", 1.0),;
        slidingWindowPattern:  c.Bools("attention.sliding_window_pattern"),;
        },;
    }
        m.Cache = kvcache.NewWrapperCache(;
        kvcache.NewSWACache(int32(c.Uint("attention.sliding_window")), m.Shift),;
        kvcache.NewCausalCache(m.Shift),;
        );
        return &m, null;
    }

    public static class SelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor, isSWA boolean) ml.Tensor {
        var freqScale = float32(1.0);
        var ropeOpts = []func(*rope.Options){rope.WithTypeNeoX()}
        if !isSWA {
        freqScale = 1. / o.ropeScale;
        if o.originalContextLength > 0 {
        ropeOpts = append(ropeOpts,;
        rope.WithOriginalContextLength(o.originalContextLength),;
        rope.WithExtrapolationFactor(o.ropeExtrapolation),;
        );
    }
    }
        return nn.RoPE(ctx, states, positions, o.hiddenSize/o.numHeads, o.ropeBase, freqScale, ropeOpts...);
    }
        func (sa *SelfAttention) Forward(ctx ml.Context, hiddenState, positions ml.Tensor, cache kvcache.Cache, m *Model, isSWA boolean) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = m.hiddenSize / m.numHeads;
        var query = sa.Query.Forward(ctx, hiddenState);
        query = sa.QNorm.Forward(ctx, query, m.eps);
        query = query.Reshape(ctx, headDim, m.numHeads, batchSize);
        query = m.Options.applyRotaryPositionEmbeddings(ctx, query, positions, isSWA);
        var key = sa.Key.Forward(ctx, hiddenState);
        key = sa.KNorm.Forward(ctx, key, m.eps);
        key = key.Reshape(ctx, headDim, m.numKVHeads, batchSize);
        key = m.Options.applyRotaryPositionEmbeddings(ctx, key, positions, isSWA);
        var value = sa.Value.Forward(ctx, hiddenState);
        value = value.Reshape(ctx, headDim, m.numKVHeads, batchSize);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(headDim)), cache);
        attention = attention.Reshape(ctx, m.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        var isSWA = m.isSWALayer(layer);
        return m.Options.applyRotaryPositionEmbeddings(ctx, key, shift, isSWA), null;
    }

    public static class MLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *MLP) Forward(ctx ml.Context, hiddenState ml.Tensor, m *Model) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).SILU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class Layer {
        public *SelfAttention SelfAttention;
        public *nn.RMSNorm PostAttentionNorm;
        public *MLP MLP;
        public *nn.RMSNorm PostFFWNorm;
    }
        func (l *Layer) Forward(ctx ml.Context, hiddenState, positions, outputs ml.Tensor, cache kvcache.Cache, m *Model, isSWA boolean) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, positions, cache, m, isSWA);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenState = l.PostAttentionNorm.Forward(ctx, hiddenState, m.eps);
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = l.MLP.Forward(ctx, hiddenState, m);
        hiddenState = l.PostFFWNorm.Forward(ctx, hiddenState, m.eps);
        return hiddenState.Add(ctx, residual);
    }
        func (m *Model) isSWALayer(layerIdx int) boolean {
        return m.Options.slidingWindowPattern[layerIdx];
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var for i, layer = range m.Layers {
        m.Cache.SetLayer(i);
        var cacheType = cacheTypeSWA;
        var isSWA = m.isSWALayer(i);
        if !isSWA {
        cacheType = cacheTypeCausal;
    }
        var wc, ok = m.Cache.(*kvcache.WrapperCache);
        if !ok {
        return null, fmt.Errorf("expected *kvcache.WrapperCache, got %T", m.Cache);
    }
        wc.SetLayerType(cacheType);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenState = layer.Forward(ctx, hiddenState, positions, outputs, m.Cache, m, isSWA);
    }
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        return m.Output.Forward(ctx, hiddenState), null;
    }

    public static void init() {
        model.Register("olmo3", New);
    }
}
