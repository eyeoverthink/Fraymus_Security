package com.fraymus.absorbed.models.qwen2;

import java.util.*;
import java.io.*;

public class model {
        "cmp";
        "fmt";
        "math";
        "strings";
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
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, cmp.Or(o.ropeDim, o.headDim, o.hiddenSize/o.numHeads), o.ropeBase, 1./o.ropeScale, rope.WithTypeNeoX());
    }

    public static class Attention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (attn Attention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var headDim = cmp.Or(opts.headDim, opts.hiddenSize/opts.numHeads);
        var query = attn.Query.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, headDim, opts.numHeads, batchSize);
        var key = attn.Key.Forward(ctx, hiddenStates);
        key = key.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        var value = attn.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(headDim)), cache);
        attention = attention.Reshape(ctx, headDim*opts.numHeads, batchSize);
        return attn.Output.Forward(ctx, attention);
    }

    public static class MLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp MLP) Forward(ctx ml.Context, hiddenStates ml.Tensor) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class DecoderLayer {
        public *nn.RMSNorm AttentionNorm;
        public *Attention Attention;
        public *nn.RMSNorm MLPNorm;
        public *MLP MLP;
    }
        func (d DecoderLayer) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = d.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.Attention.Forward(ctx, hiddenStates, positions, cache, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = d.MLPNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.MLP.Forward(ctx, hiddenStates);
        return hiddenStates.Add(ctx, residual);
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []DecoderLayer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        func (m Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var for i, layer = range m.Layers {
        m.Cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, outputs, m.Cache, &m.Options);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        hiddenStates = m.Output.Forward(ctx, hiddenStates);
        return hiddenStates, null;
    }
        func (m Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static void New() {
        if c.String("tokenizer.ggml.model") == "llama" {
        return null, fmt.Errorf("unsupported tokenizer: llama");
    }
        if strings.HasPrefix(c.String("general.name"), "Qwen2-beta") {
        return null, fmt.Errorf("unsupported model: %s", c.String("general.name"));
    }
        var m = Model{
        Layers: make([]DecoderLayer, c.Uint("block_count")),;
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        Options: Options{
        hiddenSize: int(c.Uint("embedding_length")),;
        numHeads:   int(c.Uint("attention.head_count")),;
        numKVHeads: int(c.Uint("attention.head_count_kv")),;
        headDim:    int(c.Uint("attention.key_length")),;
        ropeDim:    int(c.Uint("rope.dimension_count")),;
        ropeBase:   c.Float("rope.freq_base"),;
        ropeScale:  c.Float("rope.scaling.factor", 1),;
        eps:        c.Float("attention.layer_norm_rms_epsilon"),;
        },;
    }
        m.Cache = kvcache.NewCausalCache(m.Shift);
        return &m, null;
    }

    public static void init() {
        model.Register("qwen2", New);
    }
}
