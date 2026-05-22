package com.fraymus.absorbed.models.mistral3;

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

    public static class TextOptions {
        public numHeads, hiddenSize,;
        public ropeDim headDim,;
        public ropeBase, eps,;
        public int ropeOrigPosEmbeddings;
        public float32 ropeScalingBeta;
        public String ropeType;
        public float32 ropeExtrapolation;
        public float32 ropeBetaFast;
        public float32 ropeBetaSlow;
        public float32 ropeMscale;
        public float32 ropeMscaleAllDim;
    }
        func (o TextOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        var ropeOpts []func(*rope.Options);
        if o.ropeType == "yarn" {
        if o.ropeMscale != 0 && o.ropeMscaleAllDim != 0 {
        ropeOpts = append(ropeOpts, rope.WithAttentionFactor(1.0/float32(0.1*math.Log(double(o.ropeScale))+1.0)));
    }
        ropeOpts = append(ropeOpts,;
        rope.WithOriginalContextLength(o.ropeOrigPosEmbeddings),;
        rope.WithExtrapolationFactor(o.ropeExtrapolation),;
        rope.WithBetaFast(o.ropeBetaFast),;
        rope.WithBetaSlow(o.ropeBetaSlow),;
        );
    }
        return nn.RoPE(ctx, states, positions, o.ropeDim, o.ropeBase, 1./o.ropeScale, ropeOpts...);
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }

    public static class SelfAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *SelfAttention) Forward(ctx ml.Context, hiddenState, positionIDs, positionsScale ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var headDim = cmp.Or(opts.headDim, opts.hiddenSize/opts.numHeads);
        var q = sa.Query.Forward(ctx, hiddenState);
        q = q.Reshape(ctx, headDim, opts.numHeads, batchSize);
        q = opts.applyRotaryPositionEmbeddings(ctx, q, positionIDs);
        var k = sa.Key.Forward(ctx, hiddenState);
        k = k.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        k = opts.applyRotaryPositionEmbeddings(ctx, k, positionIDs);
        var v = sa.Value.Forward(ctx, hiddenState);
        v = v.Reshape(ctx, headDim, opts.numKVHeads, batchSize);
        if opts.ropeOrigPosEmbeddings > 0 {
        q = q.Mul(ctx, positionsScale);
    }
        var kqv = nn.Attention(ctx, q, k, v, 1.0/math.Sqrt(double(headDim)), cache);
        kqv = kqv.Reshape(ctx, headDim*opts.numHeads, batchSize);
        return sa.Output.Forward(ctx, kqv);
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
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
        func (l *Layer) Forward(ctx ml.Context, hiddenState, positionIDs, positionsScale, outputs ml.Tensor, cache kvcache.Cache, opts *TextOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, hiddenState, positionIDs, positionsScale, cache, opts);
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
        func (m *TextModel) Forward(ctx ml.Context, inputs, positions, positionsScale, outputs ml.Tensor, batch input.Batch, cache kvcache.Cache) ml.Tensor {
        var hiddenState = m.TokenEmbedding.Forward(ctx, inputs).Duplicate(ctx);
        var for _, image = range batch.Multimodal {
        var imageFeature = image.Multimodal[0].Tensor;
        ctx.Forward(imageFeature.Copy(ctx, hiddenState.View(ctx, image.Index*hiddenState.Stride(1), imageFeature.Dim(0)*imageFeature.Dim(1))));
    }
        var for i, layer = range m.Layers {
        cache.SetLayer(i);
        var lastLayerOutputs ml.Tensor;
        if i == len(m.Layers)-1 {
        lastLayerOutputs = outputs;
    }
        hiddenState = layer.Forward(ctx, hiddenState, positions, positionsScale, lastLayerOutputs, cache, m.TextOptions);
    }
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        return m.Output.Forward(ctx, hiddenState);
    }
        func (m *TextModel) getScale(ctx ml.Context, positions []int32) ml.Tensor {
        var posScale = make([]float32, len(positions));
        var for n, pos = range positions {
        var interval = math.Floor(double(pos) / double(m.ropeOrigPosEmbeddings));
        posScale[n] = float32(1.0 + double(m.ropeScalingBeta)*math.Log(1.0+interval));
    }
        return ctx.Input().FromFloats(posScale, 1, 1, len(posScale));
    }
        func newTextModel(c fs.Config) *TextModel {
        return &TextModel{
        Layers: make([]Layer, c.Uint("block_count")),;
        TextOptions: &TextOptions{
        hiddenSize:            int(c.Uint("embedding_length")),;
        numHeads:              int(c.Uint("attention.head_count")),;
        numKVHeads:            int(c.Uint("attention.head_count_kv")),;
        headDim:               int(c.Uint("attention.key_length")),;
        ropeDim:               int(c.Uint("rope.dimension_count")),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:              c.Float("rope.freq_base"),;
        ropeScale:             c.Float("rope.scaling.factor", 1.0),;
        ropeOrigPosEmbeddings: int(c.Uint("rope.scaling.original_context_length")),;
        ropeScalingBeta:       c.Float("rope.scaling_beta", 0.1),;
        ropeBetaFast:          c.Float("rope.scaling.beta_fast", 32.0),;
        ropeBetaSlow:          c.Float("rope.scaling.beta_slow", 1.0),;
        ropeType:              c.String("rope.scaling.type"),;
        ropeMscale:            c.Float("rope.scaling.mscale"),;
        ropeMscaleAllDim:      c.Float("rope.scaling.mscale_all_dim"),;
        ropeExtrapolation:     c.Float("rope.scaling.extrapolation_factor", 1),;
        },;
    }
    }
}
