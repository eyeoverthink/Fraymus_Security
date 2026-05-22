package com.fraymus.absorbed.models.qwen3vl;

import java.util.*;
import java.io.*;

public class model_text {
        "cmp";
        "math";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        );

    public static class TextOptions {
        public int valueLength;
        public float32 ropeScale;
        public []int mropeSections;
        public numExpertsUsed numExperts,;
        public boolean normTopKProb;
    }
        func (o TextOptions) headDim() int {
        return cmp.Or(o.keyLength, o.valueLength, o.hiddenSize/o.numHeads);
    }
        func (o TextOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.headDim(), o.ropeBase, 1/float32(math.Sqrt(double(o.ropeScale))),;
        rope.WithInterleaveMRoPE(o.mropeSections),;
        );
    }

    public static class TextAttention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *TextAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var query = sa.Query.Forward(ctx, hiddenStates);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, batchSize);
        key = key.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
        value = value.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
        query = sa.QueryNorm.Forward(ctx, query, opts.eps);
        key = sa.KeyNorm.Forward(ctx, key, opts.eps);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(opts.headDim())), cache);
        attention = attention.Reshape(ctx, attention.Dim(0)*attention.Dim(1), batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        type TextMLP interface {
        Forward(ml.Context, ml.Tensor, *TextOptions) ml.Tensor;
    }

    public static class sparse {
        public *nn.Linear Router;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
    }
        func (mlp *sparse) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        var hiddenDim, sequenceLength, batchSize = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, sequenceLength*batchSize);
        var routerLogits = mlp.Router.Forward(ctx, hiddenStates);
        var routingWeights = routerLogits.Softmax(ctx);
        var selectedExperts = routingWeights.TopK(ctx, opts.numExpertsUsed);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExperts, hiddenStates.Dim(1)).Rows(ctx, selectedExperts);
        if opts.normTopKProb {
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, hiddenStates.Dim(1));
        routingWeights = routingWeights.Div(ctx, routingWeights.SumRows(ctx));
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenStates.Dim(1));
    }
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates, selectedExperts).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates, selectedExperts));
        var experts = mlp.Down.Forward(ctx, hiddenStates, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var nextStates = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextStates;
    }

    public static class dense {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *dense) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ *TextOptions) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class TextLayer {
        public *nn.RMSNorm AttentionNorm;
        public *nn.RMSNorm MLPNorm;
    }
        func (d *TextLayer) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = d.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.TextAttention.Forward(ctx, hiddenStates, positions, cache, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = d.MLPNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.TextMLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public []TextLayer Layers;
        public *TextOptions Options;
    }
        var _ model.Model = (*Model)(null);
        func newTextModel(c fs.Config) *TextModel {
        var layers = make([]TextLayer, c.Uint("block_count"));
        var for i = range layers {
        if strings.HasSuffix(c.String("general.architecture"), "moe") {
        layers[i].TextMLP = &sparse{}
        } else {
        layers[i].TextMLP = &dense{}
    }
    }
        var m = TextModel{
        Layers: layers,;
        Options: &TextOptions{
        hiddenSize:     int(c.Uint("embedding_length")),;
        numHeads:       int(c.Uint("attention.head_count")),;
        numKVHeads:     int(c.Uint("attention.head_count_kv")),;
        keyLength:      int(c.Uint("attention.key_length")),;
        valueLength:    int(c.Uint("attention.value_length")),;
        eps:            c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:       c.Float("rope.freq_base"),;
        ropeScale:      c.Float("rope.scaling.factor", 1),;
        numExperts:     int(c.Uint("expert_count")),;
        numExpertsUsed: int(c.Uint("expert_used_count")),;
        normTopKProb:   c.Bool("norm_top_k_prob", true),;
        mropeSections: slices.Collect(func(yield func(int) boolean) {
        var for _, section = range c.Ints("mrope_sections", []int32{24, 20, 20}) {
        if !yield(int(section)) {
        return;
    }
    }
        }),;
        },;
    }
        return &m;
    }
}
