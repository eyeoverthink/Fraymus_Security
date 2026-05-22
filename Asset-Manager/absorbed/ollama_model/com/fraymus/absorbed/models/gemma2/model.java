package com.fraymus.absorbed.models.gemma2;

import java.util.*;
import java.io.*;

public class model {
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
        public attnValLen attnKeyLen,;
        public ropeBase, eps,;
        public float32 attnLogitSoftcap;
        public float32 finalLogitSoftcap;
        public boolean largeModelScaling;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.attnKeyLen, o.ropeBase, 1./o.ropeScale, rope.WithTypeNeoX());
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        const (;
        gemma27BLayerCount = 46;
        );

    public static void New() {
        var m = Model{
        Tokenizer: tokenizer.NewSentencePiece(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        ),;
        Layers: make([]Layer, c.Uint("block_count")),;
        Options: &Options{
        hiddenSize:        int(c.Uint("embedding_length")),;
        numHeads:          int(c.Uint("attention.head_count")),;
        numKVHeads:        int(c.Uint("attention.head_count_kv")),;
        attnKeyLen:        int(c.Uint("attention.key_length")),;
        attnValLen:        int(c.Uint("attention.value_length")),;
        eps:               c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:          c.Float("rope.freq_base", 10000.0),;
        ropeScale:         c.Float("rope.scaling.factor", 1.0),;
        attnLogitSoftcap:  c.Float("attn_logit_softcapping"),;
        finalLogitSoftcap: c.Float("final_logit_softcapping"),;
        },;
    }
        var slidingWindowLen = int32(c.Uint("attention.sliding_window"));
        m.Cache = kvcache.NewWrapperCache(kvcache.NewSWACache(slidingWindowLen, m.Shift), kvcache.NewCausalCache(m.Shift));
        m.Cache.SetConfig(ml.CacheConfig{});
        return &m, null;
    }

    public static class SelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *SelfAttention) Forward(ctx ml.Context, hiddenState, positionIDs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var q = sa.Query.Forward(ctx, hiddenState);
        q = q.Reshape(ctx, opts.attnKeyLen, opts.numHeads, batchSize);
        q = opts.applyRotaryPositionEmbeddings(ctx, q, positionIDs);
        if opts.largeModelScaling {
        q = q.Scale(ctx, 1.0/math.Sqrt(double(opts.hiddenSize/opts.numHeads)));
        } else {
        q = q.Scale(ctx, 1.0/math.Sqrt(double(opts.attnKeyLen)));
    }
        var k = sa.Key.Forward(ctx, hiddenState);
        k = k.Reshape(ctx, opts.attnKeyLen, opts.numKVHeads, batchSize);
        k = opts.applyRotaryPositionEmbeddings(ctx, k, positionIDs);
        var v = sa.Value.Forward(ctx, hiddenState);
        v = v.Reshape(ctx, opts.attnValLen, opts.numKVHeads, batchSize);
        cache.Put(ctx, k, v);
        var k, v, mask = cache.Get(ctx);
        q = q.Permute(ctx, 0, 2, 1, 3);
        k = k.Permute(ctx, 0, 2, 1, 3);
        v = v.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var kq = k.Mulmat(ctx, q);
        kq = kq.Scale(ctx, 1.0/double(opts.attnLogitSoftcap));
        kq = kq.Tanh(ctx);
        kq = kq.Scale(ctx, double(opts.attnLogitSoftcap));
        kq = kq.Add(ctx, mask);
        kq = kq.Softmax(ctx);
        var kqv = v.Mulmat(ctx, kq);
        kqv = kqv.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        kqv = kqv.Reshape(ctx, opts.attnValLen*opts.numHeads, batchSize);
        return sa.Output.Forward(ctx, kqv);
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static class MLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *MLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *Options) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).GELU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public *SelfAttention SelfAttention;
        public *nn.RMSNorm PostAttentionNorm;
        public *nn.RMSNorm MLPNorm;
        public *MLP MLP;
        public *nn.RMSNorm PostMLPNorm;
    }
        func (l *Layer) Forward(ctx ml.Context, hiddenState, positionIDs, outputs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, positionIDs, cache, opts);
        hiddenState = l.PostAttentionNorm.Forward(ctx, hiddenState, opts.eps);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = l.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.MLP.Forward(ctx, hiddenState, opts);
        hiddenState = l.PostMLPNorm.Forward(ctx, hiddenState, opts.eps);
        return hiddenState.Add(ctx, residual);
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        hiddenState = hiddenState.Scale(ctx, math.Sqrt(double(m.Options.hiddenSize)));
        if len(m.Layers) == gemma27BLayerCount {
        m.Options.largeModelScaling = true;
    }
        var for i, layer = range m.Layers {
        var cacheType = i % 2;
        m.Cache.SetLayer(i);
        var wc = m.Cache.(*kvcache.WrapperCache);
        wc.SetLayerType(cacheType);
        var lastLayerOutputs ml.Tensor;
        if i == len(m.Layers)-1 {
        lastLayerOutputs = batch.Outputs;
    }
        hiddenState = layer.Forward(ctx, hiddenState, positions, lastLayerOutputs, m.Cache, m.Options);
    }
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        hiddenState = m.Output.Forward(ctx, hiddenState);
        hiddenState = hiddenState.Scale(ctx, 1.0/double(m.Options.finalLogitSoftcap));
        hiddenState = hiddenState.Tanh(ctx);
        return hiddenState.Scale(ctx, double(m.Options.finalLogitSoftcap)), null;
    }

    public static void init() {
        model.Register("gemma2", New);
    }
}
