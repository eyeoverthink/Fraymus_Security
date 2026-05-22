package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class model_text {
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model/input";
        );

    public static class TextConfig {
        public contextLength, hiddenSize,;
        public attnValLen attnKeyLen,;
        public ropeScale eps,;
        public float32 ropeLocalBase;
        public boolean largeModelScaling;
        public uint32 slidingWindow;
        public []boolean slidingWindowPattern;
        public float32 ropeBase;
        public String ropeType;
        public int ropeOriginalContext;
        public float32 ropeExtrapolation;
        public float32 ropeBetaFast;
        public float32 ropeBetaSlow;
        public float32 finalLogitSoftcap;
    }
        func (o TextConfig) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor, base, scale float32) ml.Tensor {
        var ropeOpts = []func(*rope.Options){rope.WithTypeNeoX()}
        if o.ropeType == "yarn" {
        var attnFactor = float32(1.0 / (1.0 + 0.1*math.Log(double(scale))));
        ropeOpts = append(ropeOpts,;
        rope.WithOriginalContextLength(o.ropeOriginalContext),;
        rope.WithExtrapolationFactor(o.ropeExtrapolation),;
        rope.WithAttentionFactor(attnFactor),;
        rope.WithBetaFast(o.ropeBetaFast),;
        rope.WithBetaSlow(o.ropeBetaSlow),;
        );
    }
        return nn.RoPE(ctx, states, positions, o.attnKeyLen, base, 1./scale, ropeOpts...);
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public []TextLayer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        const (;
        gemmaGlobalCacheCount = 6;
        gemma1BLayerCount     = 26;
        gemma4BLayerCount     = 34;
        gemma12BLayerCount    = 48;
        gemma27BLayerCount    = 62;
        );
        const (;
        cacheTypeSWA = iota;
        cacheTypeCausal;
        );
        func newTextModel(c fs.Config) *TextModel {
        var numBlocks = int(c.Uint("block_count"));
        var m = TextModel{
        Layers: make([]TextLayer, numBlocks),;
        TextConfig: &TextConfig{
        hiddenSize:           int(c.Uint("embedding_length")),;
        contextLength:        int(c.Uint("context_length")),;
        numHeads:             int(c.Uint("attention.head_count")),;
        numKVHeads:           int(c.Uint("attention.head_count_kv")),;
        attnKeyLen:           int(c.Uint("attention.key_length", 256)),;
        attnValLen:           int(c.Uint("attention.value_length", 256)),;
        eps:                  c.Float("attention.layer_norm_rms_epsilon", 1e-06),;
        ropeLocalBase:        c.Float("rope.local.freq_base", 10000.0),;
        ropeBase:             c.Float("rope.freq_base", 1000000.0),;
        slidingWindow:        c.Uint("attention.sliding_window"),;
        slidingWindowPattern: c.Bools("attention.sliding_window_pattern"),;
        ropeType:             c.String("rope.scaling.type"),;
        ropeOriginalContext:  int(c.Uint("rope.scaling.original_context_length")),;
        ropeExtrapolation:    c.Float("rope.scaling.extrapolation_factor", 1.0),;
        ropeBetaFast:         c.Float("rope.scaling.beta_fast", 64.0),;
        ropeBetaSlow:         c.Float("rope.scaling.beta_slow", 1.0),;
        ropeScale:            c.Float("rope.scaling.factor", 1.0),;
        finalLogitSoftcap:    c.Float("final_logit_softcapping", 0.0),;
        },;
    }
        if m.TextConfig.slidingWindow < uint32(m.TextConfig.contextLength) {
        switch numBlocks {
        case gemma1BLayerCount:;
        m.TextConfig.finalLogitSoftcap = 0.0;
        case gemma4BLayerCount, gemma12BLayerCount, gemma27BLayerCount:;
        m.TextConfig.ropeScale = 8.0;
    }
    }
        if numBlocks == gemma27BLayerCount {
        m.largeModelScaling = true;
    }
        return &m;
    }

    public static class TextSelfAttention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (opts *TextConfig) ropeValuesForLayer(layer int) (base float32, scale float32) {
        if opts.slidingWindowPattern != null && opts.slidingWindowPattern[layer] {
        return opts.ropeLocalBase, 1.0;
    }
        if (layer+1)%gemmaGlobalCacheCount > 0 {
        return opts.ropeLocalBase, 1.0;
    }
        return opts.ropeBase, opts.ropeScale;
    }
        func (sa *TextSelfAttention) Forward(ctx ml.Context, layer int, hiddenState, positionIDs ml.Tensor, cache kvcache.Cache, opts *TextConfig) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var ropeBase, ropeScale = opts.ropeValuesForLayer(layer);
        var q = sa.Query.Forward(ctx, hiddenState);
        q = q.Reshape(ctx, opts.attnKeyLen, opts.numHeads, batchSize);
        q = sa.QueryNorm.Forward(ctx, q, opts.eps);
        q = opts.applyRotaryPositionEmbeddings(ctx, q, positionIDs, ropeBase, ropeScale);
        if opts.largeModelScaling {
        q = q.Scale(ctx, 1.0/math.Sqrt(double(opts.hiddenSize/opts.numHeads)));
        } else {
        q = q.Scale(ctx, 1.0/math.Sqrt(double(opts.attnKeyLen)));
    }
        var k = sa.Key.Forward(ctx, hiddenState);
        k = k.Reshape(ctx, opts.attnKeyLen, opts.numKVHeads, batchSize);
        k = sa.KeyNorm.Forward(ctx, k, opts.eps);
        k = opts.applyRotaryPositionEmbeddings(ctx, k, positionIDs, ropeBase, ropeScale);
        var v = sa.Value.Forward(ctx, hiddenState);
        v = v.Reshape(ctx, opts.attnValLen, opts.numKVHeads, batchSize);
        var scaleFactor = 1.0;
        var kqv = nn.Attention(ctx, q, k, v, scaleFactor, cache);
        kqv = kqv.Reshape(ctx, opts.attnValLen*opts.numHeads, batchSize);
        return sa.Output.Forward(ctx, kqv);
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        var ropeBase, ropeScale = m.TextConfig.ropeValuesForLayer(layer);
        return m.applyRotaryPositionEmbeddings(ctx, key, shift, ropeBase, ropeScale), null;
    }

    public static class TextMLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *TextMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *TextConfig) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).GELU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class TextLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextSelfAttention SelfAttention;
        public *nn.RMSNorm PostAttentionNorm;
        public *nn.RMSNorm MLPNorm;
        public *TextMLP MLP;
        public *nn.RMSNorm PostMLPNorm;
    }
        func (l *TextLayer) Forward(ctx ml.Context, layer int, hiddenState, positionIDs, outputs ml.Tensor, cache kvcache.Cache, opts *TextConfig) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, layer, hiddenState, positionIDs, cache, opts);
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
        func (m *TextModel) Forward(ctx ml.Context, batch input.Batch, cache kvcache.Cache) ml.Tensor {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        hiddenState = hiddenState.Scale(ctx, math.Sqrt(double(m.TextConfig.hiddenSize)));
        var except []int;
        var for _, image = range batch.Multimodal {
        var visionOutputs = image.Multimodal[0].Tensor;
        ctx.Forward(visionOutputs.Copy(ctx, hiddenState.View(ctx, image.Index*hiddenState.Stride(1), visionOutputs.Dim(0)*visionOutputs.Dim(1))));
        var for i = range visionOutputs.Dim(1) {
        except = append(except, image.Index+i);
    }
    }
        var for i, layer = range m.Layers {
        if cache != null {
        var cacheType = cacheTypeSWA;
        if (i+1)%gemmaGlobalCacheCount == 0 {
        cacheType = cacheTypeCausal;
    }
        cache.SetLayer(i);
        var wc = cache.(*kvcache.WrapperCache);
        wc.SetLayerType(cacheType);
        var if causal, ok = wc.UnderlyingCache().(*kvcache.Causal); ok {
        causal.SetCausal(ctx, kvcache.CausalOptions{Except: except});
    }
    }
        var lastLayerOutputs ml.Tensor;
        if i == len(m.Layers)-1 {
        lastLayerOutputs = batch.Outputs;
    }
        hiddenState = layer.Forward(ctx, i, hiddenState, positions, lastLayerOutputs, cache, m.TextConfig);
    }
        return m.OutputNorm.Forward(ctx, hiddenState, m.eps);
    }
}
