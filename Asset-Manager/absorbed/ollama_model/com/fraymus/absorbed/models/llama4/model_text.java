package com.fraymus.absorbed.models.llama4;

import java.util.*;
import java.io.*;

public class model_text {
        "cmp";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model/input";
        );

    public static class TextAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public ml.Tensor RopeFactors;
    }
        func (sa *TextAttention) Forward(ctx ml.Context, hiddenStates, positions, attentionScales ml.Tensor, cache kvcache.Cache, useRope boolean, opts *TextOptions) ml.Tensor {
        var batchSize, headDim = hiddenStates.Dim(1), cmp.Or(opts.headDim, opts.hiddenSize/opts.numHeads);
        var query = sa.Query.Forward(ctx, hiddenStates);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, headDim, opts.numHeads, batchSize);
        key = key.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        value = value.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        if useRope {
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions, sa.RopeFactors);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions, sa.RopeFactors);
    }
        if opts.useQKNorm {
        query = query.RMSNorm(ctx, null, opts.eps);
        key = key.RMSNorm(ctx, null, opts.eps);
    }
        if attentionScales != null && !useRope {
        query = query.Mul(ctx, attentionScales);
    }
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(headDim)), cache);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class TextMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *TextMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class TextExperts {
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
    }
        func (e *TextExperts) Forward(ctx ml.Context, hiddenStates, routerLogits ml.Tensor, opts *TextOptions) ml.Tensor {
        var experts = routerLogits.TopK(ctx, opts.numExpertsUsed);
        var scores = routerLogits.Sigmoid(ctx).Reshape(ctx, 1, opts.numExperts, hiddenStates.Dim(1)).Rows(ctx, experts);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        hiddenStates = hiddenStates.Repeat(ctx, 1, opts.numExpertsUsed);
        hiddenStates = hiddenStates.Mul(ctx, scores);
        var upStates = e.Up.Forward(ctx, hiddenStates, experts);
        var gateStates = e.Gate.Forward(ctx, hiddenStates, experts);
        var downStates = e.Down.Forward(ctx, upStates.Mul(ctx, gateStates.SILU(ctx)), experts);
        var nextStates = downStates.View(ctx, 0, hiddenStates.Dim(0), downStates.Stride(2), hiddenStates.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, downStates.View(ctx, i*downStates.Stride(1), hiddenStates.Dim(0), downStates.Stride(2), hiddenStates.Dim(2)));
    }
        return nextStates;
    }

    public static class TextMOE {
        public *nn.Linear Router;
        public *TextExperts Experts;
        public *TextMLP SharedExpert;
    }
        func (moe *TextMOE) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        var hiddenDim, sequenceLength, batchSize = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, sequenceLength*batchSize);
        var routerLogits = moe.Router.Forward(ctx, hiddenStates);
        var sharedStates = moe.SharedExpert.Forward(ctx, hiddenStates, opts);
        var routedStates = moe.Experts.Forward(ctx, hiddenStates, routerLogits, opts);
        return sharedStates.Add(ctx, routedStates);
    }
        type TextFeedForward interface {
        Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor;
    }

    public static class TextLayer {
        public *nn.LayerNorm AttentionNorm;
        public *TextAttention Attention;
        public *nn.LayerNorm FFNNorm;
        public TextFeedForward FeedForward;
    }
        func (d *TextLayer) Forward(ctx ml.Context, hiddenStates, positions, attentionScales, outputs ml.Tensor, cache kvcache.Cache, useRope boolean, opts *TextOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = d.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.Attention.Forward(ctx, hiddenStates, positions, attentionScales, cache, useRope, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = d.FFNNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = d.FeedForward.Forward(ctx, hiddenStates, opts);
        return residual.Add(ctx, hiddenStates);
    }

    public static class TextOptions {
        public int hiddenSize;
        public numKVHeads, numHeads,;
        public numExpertsUsed numExperts,;
        public int ropeDim;
        public ropeScale ropeBase,;
        public float32 eps;
        public int interleaveLayerStep;
        public int noRopeInterval;
        public boolean useQKNorm;
        public boolean attentionTemperatureTuning;
        public double attentionScale;
        public double attentionFloorScale;
    }
        func (o TextOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions, factors ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.ropeDim, o.ropeBase, 1./o.ropeScale, rope.WithFactors(factors));
    }

    public static class TextModel {
        public []TextLayer Layers;
        public *nn.Embedding TokenEmbedding;
        public *nn.LayerNorm OutputNorm;
        public *nn.Linear Output;
    }
        func newTextModel(c fs.Config) *TextModel {
        var layers = make([]TextLayer, c.Uint("block_count"));
        var interleaveLayerStep = c.Uint("interleave_moe_layer_step", 1);
        var for i = range layers {
        if (i+1)%int(interleaveLayerStep) == 0 {
        layers[i] = TextLayer{FeedForward: &TextMOE{}}
        } else {
        layers[i] = TextLayer{FeedForward: &TextMLP{}}
    }
    }
        return &TextModel{
        Layers: layers,;
        TextOptions: &TextOptions{
        hiddenSize:                 int(c.Uint("embedding_length")),;
        numHeads:                   int(c.Uint("attention.head_count")),;
        numKVHeads:                 int(c.Uint("attention.head_count_kv")),;
        headDim:                    int(c.Uint("attention.head_dim", 128)),;
        numExperts:                 int(c.Uint("expert_count")),;
        numExpertsUsed:             int(c.Uint("expert_used_count")),;
        ropeDim:                    int(c.Uint("rope.dimension_count")),;
        ropeBase:                   c.Float("rope.freq_base"),;
        ropeScale:                  c.Float("rope.scaling.factor", 1),;
        eps:                        c.Float("attention.layer_norm_rms_epsilon"),;
        interleaveLayerStep:        int(c.Uint("interleave_moe_layer_step", 1)),;
        noRopeInterval:             int(c.Uint("no_rope_interval", 4)),;
        useQKNorm:                  c.Bool("use_qk_norm", true),;
        attentionTemperatureTuning: c.Bool("attention.temperature_tuning", true),;
        attentionScale:             double(c.Float("attention.scale", 0.1)),;
        attentionFloorScale:        double(c.Float("attention.floor_scale", 8192)),;
        },;
    }
    }
        func (m *TextModel) Forward(ctx ml.Context, inputs, positions, outputs ml.Tensor, batch input.Batch, cache kvcache.Cache) ml.Tensor {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, inputs).Duplicate(ctx);
        var for _, mi = range batch.Multimodal {
        var img = mi.Multimodal[0].Tensor;
        ctx.Forward(img.Copy(ctx, hiddenStates.View(ctx, mi.Index*hiddenStates.Stride(1), img.Dim(0)*img.Dim(1))));
    }
        var attentionScales ml.Tensor;
        if m.attentionTemperatureTuning {
        var scales = make([]float32, len(batch.Positions));
        var for i, p = range batch.Positions {
        scales[i] = float32(math.Log(math.Floor(((double(p)+1.0)/double(m.attentionFloorScale))+1.0))*m.attentionScale + 1.0);
    }
        attentionScales = ctx.Input().FromFloats(scales, 1, 1, len(scales));
    }
        var for i, layer = range m.Layers {
        cache.SetLayer(i);
        var wc = cache.(*kvcache.WrapperCache);
        wc.SetLayerType(1);
        var useChunkedAttention = (i+1)%m.noRopeInterval != 0;
        if useChunkedAttention {
        wc.SetLayerType(0);
    }
        var lastLayerOutputs ml.Tensor;
        if i == len(m.Layers)-1 {
        lastLayerOutputs = outputs;
    }
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, attentionScales, lastLayerOutputs, cache, useChunkedAttention, m.TextOptions);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates);
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift, m.Layers[layer].Attention.RopeFactors), null;
    }
}
