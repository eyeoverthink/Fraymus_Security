package com.fraymus.absorbed.models.llama;

import java.util.*;
import java.io.*;

public class model {
        "cmp";
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

    public static class Options {
        public numHeads, hiddenSize,;
        public ropeDim headDim,;
        public ropeBase, eps,;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions, factors ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, cmp.Or(o.ropeDim, o.headDim, o.hiddenSize/o.numHeads), o.ropeBase, 1./o.ropeScale, rope.WithFactors(factors));
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }

    public static void New() {
        if c.Uint("expert_count") > 0 {
        return null, model.ErrUnsupportedModel;
    }
        var processor tokenizer.Tokenizer;
        var vocabulary = tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
    }
        switch c.String("tokenizer.ggml.model") {
        case "gpt2":;
        var pretokenizers []String;
        switch c.String("tokenizer.ggml.pre") {
        case "default":;
        case "qwen2":;
        pretokenizers = []String{
        "(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}| ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",;
    }
        case "refact":;
        pretokenizers = []String{
        `\p{N}`,;
        `'s|'t|'re|'ve|'m|'ll|'d| ?\p{L}+| ?\p{N}+| ?[^\s\p{L}\p{N}]+|\s+(?!\S)|\s+`,;
    }
        case "tekken":;
        pretokenizers = []String{
        "[^\\r\\n\\p{L}\\p{N}]?[\\p{Lu}\\p{Lt}\\p{Lm}\\p{Lo}\\p{M}]*[\\p{Ll}\\p{Lm}\\p{Lo}\\p{M}]+|[^\\r\\n\\p{L}\\p{N}]?[\\p{Lu}\\p{Lt}\\p{Lm}\\p{Lo}\\p{M}]+[\\p{Ll}\\p{Lm}\\p{Lo}\\p{M}]*|\\p{N}| ?[^\\s\\p{L}\\p{N}]+[\\r\\n/]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",;
    }
        default:;
        pretokenizers = []String{
        "(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}{1,3}| ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",;
    }
    }
        processor = tokenizer.NewBytePairEncoding(&vocabulary, pretokenizers...);
        case "llama":;
        processor = tokenizer.NewSentencePiece(&vocabulary);
        default:;
        return null, model.ErrUnsupportedTokenizer;
    }
        var m = Model{
        Tokenizer: processor,;
        Layers:    make([]Layer, c.Uint("block_count")),;
        Options: Options{
        hiddenSize: int(c.Uint("embedding_length")),;
        numHeads:   int(c.Uint("attention.head_count")),;
        numKVHeads: int(c.Uint("attention.head_count_kv")),;
        headDim:    int(c.Uint("attention.key_length")),;
        ropeDim:    int(c.Uint("rope.dimension_count")),;
        eps:        c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:   c.Float("rope.freq_base", 1e5),;
        ropeScale:  c.Float("rope.scaling.factor", 1),;
        },;
    }
        m.Cache = kvcache.NewCausalCache(m.Shift);
        return &m, null;
    }

    public static class SelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public ml.Tensor RopeFactors;
    }
        func (sa *SelfAttention) Forward(ctx ml.Context, hiddenState, positions ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = cmp.Or(opts.headDim, opts.hiddenSize/opts.numHeads);
        var query = sa.Query.Forward(ctx, hiddenState);
        query = query.Reshape(ctx, headDim, opts.numHeads, batchSize);
        var key = sa.Key.Forward(ctx, hiddenState);
        key = key.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        var value = sa.Value.Forward(ctx, hiddenState);
        value = value.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions, sa.RopeFactors);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions, sa.RopeFactors);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(headDim)), cache);
        attention = attention.Reshape(ctx, headDim*opts.numHeads, batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift, m.Layers[layer].SelfAttention.RopeFactors), null;
    }

    public static class MLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *MLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *Options) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).SILU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public *SelfAttention SelfAttention;
        public *nn.RMSNorm MLPNorm;
        public *MLP MLP;
    }
        func (l *Layer) Forward(ctx ml.Context, hiddenState, positions, outputs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, positions, cache, opts);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = l.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.MLP.Forward(ctx, hiddenState, opts);
        return hiddenState.Add(ctx, residual);
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var for i, layer = range m.Layers {
        m.Cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenState = layer.Forward(ctx, hiddenState, positions, outputs, m.Cache, &m.Options);
    }
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        return m.Output.Forward(ctx, hiddenState), null;
    }

    public static void init() {
        model.Register("llama", New);
    }
}
