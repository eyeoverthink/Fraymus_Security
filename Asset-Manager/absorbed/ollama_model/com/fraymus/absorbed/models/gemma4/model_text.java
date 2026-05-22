package com.fraymus.absorbed.models.gemma4;

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
        const (;
        cacheTypeSWA = iota;
        cacheTypeCausal;
        );

    public static class TextOptions {
        public int hiddenSize;
        public numKVHeads numHeads,;
        public int numGlobalKVHeads;
        public globalHeadDim headDim,;
        public int hiddenLayers;
        public int hiddenSizePerLayerInput;
        public float32 eps;
        public float32 ropeBase;
        public float32 ropeLocalBase;
        public int partialRotaryDims;
        public []boolean slidingWindowPattern;
        public map[int]int kvDonorMap;
        public float32 finalLogitSoftcap;
        public int numExperts;
        public int numExpertsUsed;
    }
        func (o *TextOptions) isLocal(layer int) boolean {
        if layer < len(o.slidingWindowPattern) {
        return o.slidingWindowPattern[layer];
    }
        return false;
    }
        func (o *TextOptions) ropeForLayer(layer int) (base float32, dims int) {
        if o.isLocal(layer) {
        return o.ropeLocalBase, o.headDim;
    }
        return o.ropeBase, o.partialRotaryDims;
    }
        func (o *TextOptions) kvHeadsForLayer(layer int) int {
        if o.isLocal(layer) {
        return o.numKVHeads;
    }
        if o.numGlobalKVHeads > 0 {
        return o.numGlobalKVHeads;
    }
        return o.numKVHeads;
    }
        func (o *TextOptions) headDimForLayer(layer int) int {
        if o.isLocal(layer) {
        return o.headDim;
    }
        return o.globalHeadDim;
    }

    public static class TextModel {
        public *nn.Embedding TokenEmbedding;
        public []TextLayer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        func newTextModel(c fs.Config) *TextModel {
        var numLayers = int(c.Uint("block_count"));
        var globalHeadDim = int(c.Uint("attention.key_length", 512));
        var headDim = int(c.Uint("attention.key_length_swa", 256));
        var partialRotaryDims = int(c.Uint("rope.dimension_count", 0));
        if partialRotaryDims == 0 {
        var partialFactor = c.Float("rope.partial_rotary_factor", 1.0);
        partialRotaryDims = int(float32(globalHeadDim) * partialFactor);
    }
        var ropeBase = c.Float("rope.freq_base", 1000000.0);
        var ropeLocalBase = c.Float("rope.freq_base_swa", 0);
        if ropeLocalBase == 0 {
        ropeLocalBase = c.Float("rope.local.freq_base", 10000.0);
    }
        var numGlobalKVHeads = int(c.Uint("attention.global_head_count_kv", 0));
        var slidingPattern = c.Bools("attention.sliding_window_pattern");
        var numKVHeads = 0;
        var kvHeadsArray = c.Ints("attention.head_count_kv");
        if len(kvHeadsArray) > 0 {
        numKVHeads = int(kvHeadsArray[0]);
        if numGlobalKVHeads == 0 && len(slidingPattern) > 0 {
        var for i, isLocal = range slidingPattern {
        if !isLocal && i < len(kvHeadsArray) {
        numGlobalKVHeads = int(kvHeadsArray[i]);
        break;
    }
    }
    }
    }
        if numKVHeads == 0 {
        numKVHeads = int(c.Uint("attention.head_count_kv", 0));
    }
        var sharedLayers = int(c.Uint("attention.shared_kv_layers", 0));
        var kvDonorMap = make(map[int]int);
        if sharedLayers > 0 && len(slidingPattern) > 0 {
        var firstShared = numLayers - sharedLayers;
        var for i = firstShared; i < numLayers; i++ {
        var isLocal = slidingPattern[i];
        var for j = firstShared - 1; j >= 0; j-- {
        if slidingPattern[j] == isLocal {
        kvDonorMap[i] = j;
        break;
    }
    }
    }
    }
        return &TextModel{
        Layers: make([]TextLayer, numLayers),;
        TextOptions: TextOptions{
        hiddenSize:              int(c.Uint("embedding_length")),;
        numHeads:                int(c.Uint("attention.head_count")),;
        numKVHeads:              numKVHeads,;
        numGlobalKVHeads:        numGlobalKVHeads,;
        headDim:                 headDim,;
        globalHeadDim:           globalHeadDim,;
        hiddenLayers:            numLayers,;
        hiddenSizePerLayerInput: int(c.Uint("embedding_length_per_layer_input", 0)),;
        eps:                     c.Float("attention.layer_norm_rms_epsilon", 1e-06),;
        ropeBase:                ropeBase,;
        ropeLocalBase:           ropeLocalBase,;
        partialRotaryDims:       partialRotaryDims,;
        slidingWindowPattern:    slidingPattern,;
        kvDonorMap:              kvDonorMap,;
        finalLogitSoftcap:       c.Float("final_logit_softcapping", 0.0),;
        numExperts:              int(c.Uint("expert_count", 0)),;
        numExpertsUsed:          int(c.Uint("expert_used_count", 0)),;
        },;
    }
    }
        func (m *TextModel) Forward(ctx ml.Context, batch input.Batch, cache kvcache.Cache) ml.Tensor {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        hiddenState = hiddenState.Scale(ctx, math.Sqrt(double(m.hiddenSize)));
        var except []int;
        var for _, image = range batch.Multimodal {
        var visionOutputs = image.Multimodal[0].Tensor;
        ctx.Forward(visionOutputs.Copy(ctx, hiddenState.View(ctx, image.Index*hiddenState.Stride(1), visionOutputs.Dim(0)*visionOutputs.Dim(1))));
        var for i = range visionOutputs.Dim(1) {
        except = append(except, image.Index+i);
    }
    }
        var perLayerInputs ml.Tensor;
        if m.PerLayerProjector != null {
        perLayerInputs = m.PerLayerProjector.Forward(ctx, batch, hiddenState, &m.TextOptions);
    }
        var for i = range len(m.Layers) {
        var layer = m.Layers[i];
        if cache != null {
        cache.SetLayer(i);
        var cacheType = cacheTypeSWA;
        if !m.isLocal(i) {
        cacheType = cacheTypeCausal;
    }
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
        var perLayerInput ml.Tensor;
        if perLayerInputs != null {
        perLayerInput = perLayerInputs.View(ctx, i*perLayerInputs.Stride(1), perLayerInputs.Dim(0), perLayerInputs.Stride(2), perLayerInputs.Dim(2));
    }
        var isShared = false;
        var if donorLayer, ok = m.kvDonorMap[i]; ok {
        cache.SetLayer(donorLayer);
        isShared = true;
    }
        hiddenState = layer.Forward(ctx, i, hiddenState, positions, perLayerInput, lastLayerOutputs, cache, isShared, &m.TextOptions);
    }
        return m.OutputNorm.Forward(ctx, hiddenState, m.eps);
    }

    public static class PerLayerProjector {
        public *nn.Embedding TokenEmbedding;
        public *nn.Linear Projector;
        public *nn.RMSNorm Norm;
    }
        func (p *PerLayerProjector) Forward(ctx ml.Context, batch input.Batch, inputs ml.Tensor, opts *TextOptions) ml.Tensor {
        var inputsPerLayer = p.TokenEmbedding.Forward(ctx, batch.Inputs);
        inputsPerLayer = inputsPerLayer.Scale(ctx, math.Sqrt(double(opts.hiddenSizePerLayerInput)));
        inputsPerLayer = inputsPerLayer.Reshape(ctx, opts.hiddenSizePerLayerInput, opts.hiddenLayers, inputs.Dim(1));
        var perLayerProjection = p.Projector.Forward(ctx, inputs);
        perLayerProjection = perLayerProjection.Scale(ctx, 1.0/math.Sqrt(double(opts.hiddenSize)));
        perLayerProjection = perLayerProjection.Reshape(ctx, opts.hiddenSizePerLayerInput, opts.hiddenLayers, inputs.Dim(1));
        perLayerProjection = p.Norm.Forward(ctx, perLayerProjection, opts.eps);
        if inputsPerLayer != null {
        perLayerProjection = perLayerProjection.Add(ctx, inputsPerLayer);
        perLayerProjection = perLayerProjection.Scale(ctx, 1/math.Sqrt(2));
    }
        return perLayerProjection;
    }

    public static class TextSelfAttention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
        public ml.Tensor RopeFactors;
    }
        func (sa *TextSelfAttention) Forward(ctx ml.Context, layer int, hiddenState, positions ml.Tensor, cache kvcache.Cache, sharedKV boolean, opts *TextOptions) ml.Tensor {
        var batchSize = hiddenState.Dim(1);
        var hd = opts.headDimForLayer(layer);
        var kvHeads = opts.kvHeadsForLayer(layer);
        var ropeBase, ropeDims = opts.ropeForLayer(layer);
        var q = sa.Query.Forward(ctx, hiddenState);
        q = q.Reshape(ctx, hd, opts.numHeads, batchSize);
        q = sa.QueryNorm.Forward(ctx, q, opts.eps);
        var k, v ml.Tensor;
        if !sharedKV {
        k = sa.Key.Forward(ctx, hiddenState);
        k = k.Reshape(ctx, hd, kvHeads, batchSize);
        if sa.Value != null {
        v = sa.Value.Forward(ctx, hiddenState);
        v = v.Reshape(ctx, hd, kvHeads, batchSize);
        } else {
        v = k;
    }
        k = sa.KeyNorm.Forward(ctx, k, opts.eps);
        v = v.RMSNorm(ctx, null, opts.eps) // V norm: unweighted RMSNorm;
    }
        var ropeOpts = []func(*rope.Options){rope.WithTypeNeoX()}
        if sa.RopeFactors != null && !opts.isLocal(layer) {
        ropeOpts = append(ropeOpts, rope.WithFactors(sa.RopeFactors));
    }
        q = nn.RoPE(ctx, q, positions, ropeDims, ropeBase, 1.0, ropeOpts...);
        if k != null {
        k = nn.RoPE(ctx, k, positions, ropeDims, ropeBase, 1.0, ropeOpts...);
    }
        var attention = nn.Attention(ctx, q, k, v, 1.0, cache);
        attention = attention.Reshape(ctx, hd*opts.numHeads, batchSize);
        return sa.Output.Forward(ctx, attention);
    }

    public static class TextMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *TextMLP) Forward(ctx ml.Context, hiddenState ml.Tensor) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).GELU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class TextRouter {
        public *nn.Linear Proj;
        public ml.Tensor Scale;
    }
        func (r *TextRouter) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *TextOptions) (routingWeights, selectedExperts ml.Tensor) {
        var x = hiddenState.RMSNorm(ctx, null, opts.eps);
        x = x.Scale(ctx, 1.0/math.Sqrt(double(opts.hiddenSize)));
        x = x.Mul(ctx, r.Scale);
        var expertScores = r.Proj.Forward(ctx, x);
        routingWeights = expertScores.Softmax(ctx);
        selectedExperts = routingWeights.TopK(ctx, opts.numExpertsUsed);
        return routingWeights, selectedExperts;
    }

    public static class TextMoEBlock {
        public *nn.LinearBatch GateUp;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
        public ml.Tensor DownScale;
    }
        func (moe *TextMoEBlock) Forward(ctx ml.Context, hiddenState, routingWeights, selectedExperts ml.Tensor, opts *TextOptions) ml.Tensor {
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExperts, hiddenState.Dim(1)).Rows(ctx, selectedExperts);
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, hiddenState.Dim(1));
        routingWeights = routingWeights.Div(ctx, routingWeights.SumRows(ctx));
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenState.Dim(1));
        hiddenState = hiddenState.Reshape(ctx, hiddenState.Dim(0), 1, hiddenState.Dim(1));
        var gateOut, upOut ml.Tensor;
        if moe.GateUp != null && moe.GateUp.Weight != null {
        var gateUp = moe.GateUp.Forward(ctx, hiddenState, selectedExperts);
        var nFF = gateUp.Dim(0) / 2;
        gateOut = gateUp.Slice(ctx, 0, 0, nFF, 1);
        upOut = gateUp.Slice(ctx, 0, nFF, gateUp.Dim(0), 1);
        } else {
        gateOut = moe.Gate.Forward(ctx, hiddenState, selectedExperts);
        upOut = moe.Up.Forward(ctx, hiddenState, selectedExperts);
    }
        hiddenState = gateOut.GELU(ctx, upOut);
        var experts = moe.Down.Forward(ctx, hiddenState, selectedExperts);
        if moe.DownScale != null {
        var expertScales = moe.DownScale.Reshape(ctx, opts.numExperts, 1);
        expertScales = expertScales.Repeat(ctx, 1, hiddenState.Dim(2));
        expertScales = expertScales.Reshape(ctx, 1, opts.numExperts, hiddenState.Dim(2)).Rows(ctx, selectedExperts);
        expertScales = expertScales.Reshape(ctx, opts.numExpertsUsed, hiddenState.Dim(2));
        expertScales = expertScales.Reshape(ctx, 1, opts.numExpertsUsed, hiddenState.Dim(2));
        experts = experts.Mul(ctx, expertScales);
    }
        experts = experts.Mul(ctx, routingWeights);
        var nextStates = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextStates;
    }

    public static class TextLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextSelfAttention SelfAttention;
        public *nn.RMSNorm PostAttentionNorm;
        public *nn.RMSNorm MLPNorm;
        public *TextMLP MLP;
        public *nn.RMSNorm PostMLPNorm;
        public *TextRouter Router;
        public *TextMoEBlock MoE;
        public *nn.RMSNorm MoENorm;
        public *nn.RMSNorm PostMoENorm;
        public *nn.RMSNorm PostMLPNorm1;
        public *nn.Linear PerLayerInputGate;
        public *nn.Linear PerLayerProjection;
        public *nn.RMSNorm PostPerLayerNorm;
        public ml.Tensor LayerScalar;
    }
        func (l *TextLayer) Forward(ctx ml.Context, layer int, hiddenState, positions, perLayerInput, outputs ml.Tensor, cache kvcache.Cache, sharedKV boolean, opts *TextOptions) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.SelfAttention.Forward(ctx, layer, hiddenState, positions, cache, sharedKV, opts);
        hiddenState = l.PostAttentionNorm.Forward(ctx, hiddenState, opts.eps);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
        if perLayerInput != null {
        perLayerInput = perLayerInput.Rows(ctx, outputs);
    }
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        var hasSplitExperts = l.MoE != null && l.MoE.Gate != null && l.MoE.Up != null && l.MoE.Gate.Weight != null && l.MoE.Up.Weight != null;
        var hasFusedExperts = l.MoE != null && l.MoE.GateUp != null && l.MoE.GateUp.Weight != null;
        if l.Router != null && l.MoE != null && l.MoE.Down != null && l.MoE.Down.Weight != null && (hasSplitExperts || hasFusedExperts) {
        var mlpState = l.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        mlpState = l.MLP.Forward(ctx, mlpState);
        mlpState = l.PostMLPNorm1.Forward(ctx, mlpState, opts.eps);
        var routingWeights, selectedExperts = l.Router.Forward(ctx, hiddenState, opts);
        var moeState = l.MoENorm.Forward(ctx, hiddenState, opts.eps);
        moeState = l.MoE.Forward(ctx, moeState, routingWeights, selectedExperts, opts);
        moeState = l.PostMoENorm.Forward(ctx, moeState, opts.eps);
        var combined = mlpState.Add(ctx, moeState);
        combined = l.PostMLPNorm.Forward(ctx, combined, opts.eps);
        hiddenState = combined.Add(ctx, residual);
        } else {
        hiddenState = l.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.MLP.Forward(ctx, hiddenState);
        hiddenState = l.PostMLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = hiddenState.Add(ctx, residual);
    }
        if perLayerInput != null && l.PerLayerInputGate != null {
        var pleState = l.PerLayerInputGate.Forward(ctx, hiddenState);
        pleState = pleState.GELU(ctx, perLayerInput);
        pleState = l.PerLayerProjection.Forward(ctx, pleState);
        pleState = l.PostPerLayerNorm.Forward(ctx, pleState, opts.eps);
        hiddenState = hiddenState.Add(ctx, pleState);
    }
        if l.LayerScalar != null {
        hiddenState = hiddenState.Mul(ctx, l.LayerScalar);
    }
        return hiddenState;
    }
}
