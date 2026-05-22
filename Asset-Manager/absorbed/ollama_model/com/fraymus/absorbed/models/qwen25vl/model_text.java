package com.fraymus.absorbed.models.qwen25vl;

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

    public static class TextOptions {
        public numHeads, hiddenSize,;
        public originalContextLength ropeDim,;
        public ropeBase, eps,;
        public []int mropeSections;
    }
        func (o TextOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.ropeDim, o.ropeBase, 1./o.ropeScale, rope.WithMRoPE(o.mropeSections));
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public []int32 positionCache;
    }
        func NewTextModel(c fs.Config) *TextModel {
        var m = TextModel{
        Layers: make([]Layer, c.Uint("block_count")),;
        TextOptions: &TextOptions{
        hiddenSize:            int(c.Uint("embedding_length")),;
        numHeads:              int(c.Uint("attention.head_count")),;
        numKVHeads:            int(c.Uint("attention.head_count_kv")),;
        ropeDim:               int(c.Uint("rope.dimension_count", 128)),;
        originalContextLength: int(c.Uint("context_length", 128000)),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:              c.Float("rope.freq_base"),;
        ropeScale:             c.Float("rope.scaling.factor", 1),;
        mropeSections: func() []int {
        var sections = c.Ints("rope.mrope_section");
        var s = make([]int, len(sections));
        var for i, section = range sections {
        s[i] = int(section);
    }
        return s;
        }(),;
        },;
    }
        return &m;
    }

    public static class SelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *SelfAttention) Forward(ctx ml.Context, hiddenState, positionIDs ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = opts.hiddenSize / opts.numHeads;
        var q = sa.Query.Forward(ctx, hiddenState);
        q = q.Reshape(ctx, headDim, opts.numHeads, batchSize);
        q = opts.applyRotaryPositionEmbeddings(ctx, q, positionIDs);
        var k = sa.Key.Forward(ctx, hiddenState);
        k = k.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        k = opts.applyRotaryPositionEmbeddings(ctx, k, positionIDs);
        var v = sa.Value.Forward(ctx, hiddenState);
        v = v.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        var scaleFactor = 1.0 / math.Sqrt(double(headDim));
        var kqv = nn.Attention(ctx, q, k, v, scaleFactor, cache);
        kqv = kqv.Reshape(ctx, opts.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, kqv);
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        m.positionCache = null;
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static class MLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *MLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *TextOptions) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).SILU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public *SelfAttention SelfAttention;
        public *nn.RMSNorm MLPNorm;
        public *MLP MLP;
    }
        func (l *Layer) Forward(ctx ml.Context, hiddenState, positionIDs, outputs ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, positionIDs, cache, opts);
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
}
