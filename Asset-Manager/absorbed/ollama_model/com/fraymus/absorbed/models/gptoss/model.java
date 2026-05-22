package com.fraymus.absorbed.models.gptoss;

import java.util.*;
import java.io.*;

public class model {
        "cmp";
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

    public static class Transformer {
        public *nn.Embedding TokenEmbedding;
        public []TransformerBlock TransformerBlocks;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        func (m *Transformer) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var for i, block = range m.TransformerBlocks {
        m.Cache.SetLayer(i);
        var if c, ok = m.Cache.(*kvcache.WrapperCache); ok {
        c.SetLayerType(i % 2);
    }
        var outputs ml.Tensor;
        if i == len(m.TransformerBlocks)-1 {
        outputs = batch.Outputs;
    }
        hiddenStates = block.Forward(ctx, hiddenStates, positions, outputs, m.Cache, &m.Options);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }
        func (m *Transformer) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static class Options {
        public int originalContextLength;
        public float32 ropeScale;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.headDim(), o.ropeBase, 1./o.ropeScale,;
        rope.WithTypeNeoX(),;
        rope.WithOriginalContextLength(o.originalContextLength),;
        rope.WithExtrapolationFactor(1.),;
        );
    }
        func (o Options) headDim() int {
        return cmp.Or(o.keyLength, o.valueLength, o.hiddenSize/o.numHeads);
    }

    public static class TransformerBlock {
        public *AttentionBlock Attention;
        public *MLPBlock MLP;
    }
        func (d *TransformerBlock) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        hiddenStates = d.Attention.Forward(ctx, hiddenStates, positions, cache, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
    }
        hiddenStates = d.MLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates;
    }

    public static class AttentionBlock {
        public *nn.RMSNorm Norm;
        public *nn.Linear QKV;
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public ml.Tensor Sinks;
    }
        func (attn *AttentionBlock) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var residual = hiddenStates;
        hiddenStates = attn.Norm.Forward(ctx, hiddenStates, opts.eps);
        var query, key, value ml.Tensor;
        if attn.QKV != null {
        var qkv = attn.QKV.Forward(ctx, hiddenStates);
        qkv = qkv.Reshape(ctx, opts.headDim(), -1, batchSize);
        var chunks = qkv.ChunkSections(ctx, 1, opts.numHeads, opts.numKVHeads, opts.numKVHeads);
        query, key, value = chunks[0], chunks[1], chunks[2];
        } else {
        query = attn.Query.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, batchSize);
        key = attn.Key.Forward(ctx, hiddenStates);
        key = key.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
        value = attn.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
    }
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.AttentionWithSinks(ctx, query, key, value, attn.Sinks, 1/math.Sqrt(double(opts.headDim())), cache);
        attention = attention.Reshape(ctx, attention.Dim(0)*attention.Dim(1), batchSize);
        return attn.Output.Forward(ctx, attention).Add(ctx, residual);
    }

    public static class MLPBlock {
        public *nn.RMSNorm Norm;
        public *nn.Linear Router;
        public *nn.LinearBatch GateUp;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
    }
        func (mlp *MLPBlock) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var hiddenDim, sequenceLength, batchSize = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        var residual = hiddenStates;
        hiddenStates = mlp.Norm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, sequenceLength*batchSize);
        var routingWeights = mlp.Router.Forward(ctx, hiddenStates);
        var selectedExperts = routingWeights.TopK(ctx, opts.numExpertsUsed);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExperts, sequenceLength*batchSize).Rows(ctx, selectedExperts);
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, sequenceLength*batchSize).Softmax(ctx);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, sequenceLength*batchSize);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        var gate, up ml.Tensor;
        if mlp.GateUp != null {
        hiddenStates = mlp.GateUp.Forward(ctx, hiddenStates, selectedExperts);
        gate = hiddenStates.Slice(ctx, 0, 0, hiddenStates.Dim(0), 2);
        up = hiddenStates.Slice(ctx, 0, 1, hiddenStates.Dim(0), 2);
        } else {
        gate = mlp.Gate.Forward(ctx, hiddenStates, selectedExperts);
        up = mlp.Up.Forward(ctx, hiddenStates, selectedExperts);
    }
        hiddenStates = gate.SILUAlphaLimit(ctx, up, 1.702, 7);
        var experts = mlp.Down.Forward(ctx, hiddenStates, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var nextStates = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextStates.Add(ctx, residual);
    }

    public static void New() {
        var m = Transformer{
        TransformerBlocks: make([]TransformerBlock, c.Uint("block_count")),;
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
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
        strings.Join([]String{
        `[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]*[\p{Ll}\p{Lm}\p{Lo}\p{M}]+(?i:'s|'t|'re|'ve|'m|'ll|'d)?`,;
        `[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]+[\p{Ll}\p{Lm}\p{Lo}\p{M}]*(?i:'s|'t|'re|'ve|'m|'ll|'d)?`,;
        `\p{N}{1,3}`,;
        ` ?[^\s\p{L}\p{N}]+[\r\n/]*`,;
        `\s*[\r\n]+`,;
        `\s+(?!\S)`,;
        `\s+`,;
        }, "|"),;
        ),;
        Options: Options{
        hiddenSize:            int(c.Uint("embedding_length")),;
        numHeads:              int(c.Uint("attention.head_count")),;
        numKVHeads:            int(c.Uint("attention.head_count_kv")),;
        keyLength:             int(c.Uint("attention.key_length")),;
        valueLength:           int(c.Uint("attention.value_length")),;
        numExperts:            int(c.Uint("expert_count")),;
        numExpertsUsed:        int(c.Uint("expert_used_count")),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:              c.Float("rope.freq_base"),;
        ropeScale:             c.Float("rope.scaling.factor", 1.),;
        originalContextLength: int(c.Uint("rope.scaling.original_context_length")),;
        },;
    }
        m.Cache = kvcache.NewWrapperCache(;
        kvcache.NewSWAMemCache(int32(c.Uint("attention.sliding_window")), 4096, m.Shift),;
        kvcache.NewCausalCache(m.Shift),;
        );
        return &m, null;
    }

    public static void init() {
        model.Register("gptoss", New);
        model.Register("gpt-oss", New);
    }
}
