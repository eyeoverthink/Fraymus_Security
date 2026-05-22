package com.fraymus.absorbed.models.glmocr;

import java.util.*;
import java.io.*;

public class model_text {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );

    public static class TextModelOptions {
        public int hiddenSize;
        public int numHeads;
        public int numKVHeads;
        public int headDim;
        public int rotaryDim;
        public int intermediateSize;
        public float32 eps;
        public float32 ropeBase;
        public []int mropeSections;
    }
        func (o *TextModelOptions) applyMRoPE(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.rotaryDim, o.ropeBase, 1.0, rope.WithMRoPE(o.mropeSections));
    }

    public static class TextSelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *TextSelfAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts *TextModelOptions) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var q = sa.Query.Forward(ctx, hiddenStates);
        var k = sa.Key.Forward(ctx, hiddenStates);
        var v = sa.Value.Forward(ctx, hiddenStates);
        q = q.Reshape(ctx, opts.headDim, opts.numHeads, batchSize);
        k = k.Reshape(ctx, opts.headDim, opts.numKVHeads, batchSize);
        v = v.Reshape(ctx, opts.headDim, opts.numKVHeads, batchSize);
        q = opts.applyMRoPE(ctx, q, positions);
        k = opts.applyMRoPE(ctx, k, positions);
        var scaleFactor = 1.0 / math.Sqrt(double(opts.headDim));
        var kqv = nn.Attention(ctx, q, k, v, scaleFactor, cache);
        kqv = kqv.Reshape(ctx, opts.numHeads*opts.headDim, batchSize);
        return sa.Output.Forward(ctx, kqv);
    }

    public static class TextMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *TextMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextModelOptions) ml.Tensor {
        var gate = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, gate);
    }

    public static class TextDecoderLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextSelfAttention SelfAttention;
        public *nn.RMSNorm PostAttnNorm;
        public *nn.RMSNorm FFNNorm;
        public *TextMLP MLP;
        public *nn.RMSNorm PostFFNNorm;
    }
        func (l *TextDecoderLayer) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts *TextModelOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = l.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = l.SelfAttention.Forward(ctx, hiddenStates, positions, cache, opts);
        hiddenStates = l.PostAttnNorm.Forward(ctx, hiddenStates, opts.eps);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = l.FFNNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = l.MLP.Forward(ctx, hiddenStates, opts);
        hiddenStates = l.PostFFNNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = hiddenStates.Add(ctx, residual);
        return hiddenStates;
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public []TextDecoderLayer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public []int32 positionCache;
        public int32 ropeDelta;
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        m.positionCache = null;
        m.ropeDelta = 0;
        return m.applyMRoPE(ctx, key, shift), null;
    }
        func newTextModel(c fs.Config) *TextModel {
        var hiddenSize = int(c.Uint("embedding_length", 1536));
        var numHeads = int(c.Uint("attention.head_count", 16));
        var numKVHeads = int(c.Uint("attention.head_count_kv", 8));
        var intermediateSize = int(c.Uint("feed_forward_length", 4608));
        var eps = c.Float("attention.layer_norm_rms_epsilon", 1e-5);
        var ropeBase = c.Float("rope.freq_base", 10000);
        var headDim = int(c.Uint("attention.key_length", uint32(hiddenSize/numHeads)));
        var ropeDim = int(c.Uint("rope.dimension_count", uint32(headDim)));
        if ropeDim <= 0 {
        ropeDim = headDim;
    }
        var mropeSections = c.Ints("rope.mrope_section");
        var sectionInts []int;
        if len(mropeSections) > 0 {
        sectionInts = make([]int, len(mropeSections));
        var for i, section = range mropeSections {
        sectionInts[i] = int(section);
    }
        } else {
        var total = ropeDim / 2;
        if total <= 0 {
        total = 32;
    }
        var s0 = total * 2 / 8;
        var s1 = total * 3 / 8;
        var s2 = total - s0 - s1;
        sectionInts = []int{s0, s1, s2}
    }
        var rotaryDim = ropeDim;
        return &TextModel{
        Layers: make([]TextDecoderLayer, c.Uint("block_count", 16)),;
        TextModelOptions: &TextModelOptions{
        hiddenSize:       hiddenSize,;
        numHeads:         numHeads,;
        numKVHeads:       numKVHeads,;
        headDim:          headDim,;
        rotaryDim:        rotaryDim,;
        intermediateSize: intermediateSize,;
        eps:              eps,;
        ropeBase:         ropeBase,;
        mropeSections:    sectionInts,;
        },;
    }
    }
}
