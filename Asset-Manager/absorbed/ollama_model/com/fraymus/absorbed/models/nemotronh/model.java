package com.fraymus.absorbed.models.nemotronh;

import java.util.*;
import java.io.*;

public class model {
        "fmt";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Options {
        public int hiddenSize;
        public int numHeads;
        public int numKVHeads;
        public int headDim;
        public float32 eps;
        public int ssmDConv;
        public int ssmDInner;
        public int ssmDState;
        public int ssmNHead;
        public int ssmNGroup;
        public []boolean isRecurrent;
        public []int nFF;
        public double attentionScale;
        public int numExperts;
        public int numExpertsUsed;
        public boolean expertWeightsNorm;
        public float32 expertWeightsScale;
        public float32 expertWeightsNormClip;
    }
        func (o Options) getHeadDim() int {
        if o.headDim > 0 {
        return o.headDim;
    }
        if o.numHeads <= 0 {
        return 0;
    }
        return o.hiddenSize / o.numHeads;
    }
        type Operator interface {
        Forward(ctx ml.Context, hiddenStates ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error);
    }
        type MLP interface {
        Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor;
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public Operator Operator;
        public MLP MLP;
    }
        func (l *Layer) Forward(ctx ml.Context, layer int, hiddenStates, outputs ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var residual = hiddenStates;
        hiddenStates = l.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        if l.Operator != null {
        var err error;
        hiddenStates, err = l.Operator.Forward(ctx, hiddenStates, cache, opts);
        if err != null {
        return null, err;
    }
        } else if l.MLP != null {
        hiddenStates = l.MLP.Forward(ctx, hiddenStates, opts);
    }
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        return hiddenStates.Add(ctx, residual), null;
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public []Layer Layers;
    }

    public static void Shift(ml.Context ctx, int layer) {
        return key, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var cache = m.Cache.(*HybridCache);
        var for i, layer = range m.Layers {
        cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        var err error;
        hiddenStates, err = layer.Forward(ctx, i, hiddenStates, outputs, cache, m.Options);
        if err != null {
        return null, err;
    }
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }

    public static void New() {
        var numLayers = int(c.Uint("block_count"));
        var layers = make([]Layer, numLayers);
        type perLayerConfig interface {
        HeadCount() []uint64;
        HeadCountKV() []uint64;
        FFNLength() []uint64;
    }
        var headCount []uint64;
        var headCountKV []uint64;
        var ffnLength []uint64;
        var if plc, ok = c.(perLayerConfig); ok {
        headCount = plc.HeadCount();
        headCountKV = plc.HeadCountKV();
        ffnLength = plc.FFNLength();
    }
        var isRecurrent = make([]boolean, numLayers);
        var nFF = make([]int, numLayers);
        var for i = range numLayers {
        var kvHeads = uint64(1) // Default non-zero;
        if i < len(headCountKV) {
        kvHeads = headCountKV[i];
    }
        var ff = uint64(0);
        if i < len(ffnLength) {
        ff = ffnLength[i];
    }
        nFF[i] = int(ff);
        isRecurrent[i] = kvHeads == 0 && ff == 0;
    }
        var isMoE = c.Uint("expert_count") > 0;
        var for i = range layers {
        if isRecurrent[i] {
        layers[i].Operator = &Mamba2{Layer: i}
        } else if nFF[i] == 0 {
        layers[i].Operator = &Attention{}
        } else {
        if isMoE {
        layers[i].MLP = &MoESparse{}
        } else {
        layers[i].MLP = &Dense{}
    }
    }
    }
        var numHeads = int(c.Uint("attention.head_count"));
        if numHeads == 0 {
        var for i = range numLayers {
        if i < len(headCount) && i < len(headCountKV) && headCount[i] > 0 && headCountKV[i] > 0 {
        numHeads = int(headCount[i]);
        break;
    }
    }
    }
        var numKVHeads = int(c.Uint("attention.head_count_kv"));
        if numKVHeads == 0 {
        var for i = range numLayers {
        if i < len(headCountKV) && i < len(ffnLength) && headCountKV[i] > 0 && ffnLength[i] == 0 {
        numKVHeads = int(headCountKV[i]);
        break;
    }
    }
        if numKVHeads == 0 {
        numKVHeads = numHeads;
    }
    }
        var headDim = int(c.Uint("attention.head_dim"));
        if headDim == 0 {
        var if keyLength = int(c.Uint("attention.key_length")); keyLength > 0 {
        headDim = keyLength;
        } else if numHeads > 0 {
        headDim = int(c.Uint("embedding_length")) / numHeads;
    }
    }
        if headDim <= 0 {
        return null, fmt.Errorf("nemotronh: invalid attention head dimension");
    }
        if numHeads <= 0 {
        numHeads = 1;
    }
        var numExperts = int(c.Uint("expert_count"));
        var numExpertsUsed = int(c.Uint("expert_used_count"));
        if numExperts > 0 {
        if numExpertsUsed <= 0 || numExpertsUsed > numExperts {
        return null, fmt.Errorf("nemotronh: invalid expert_used_count=%d for expert_count=%d", numExpertsUsed, numExperts);
    }
    }
        var opts = &Options{
        hiddenSize:            int(c.Uint("embedding_length")),;
        numHeads:              numHeads,;
        numKVHeads:            numKVHeads,;
        headDim:               headDim,;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ssmDConv:              int(c.Uint("ssm.conv_kernel")),;
        ssmDInner:             int(c.Uint("ssm.inner_size")),;
        ssmDState:             int(c.Uint("ssm.state_size")),;
        ssmNHead:              int(c.Uint("ssm.time_step_rank")),;
        ssmNGroup:             int(c.Uint("ssm.group_count")),;
        isRecurrent:           isRecurrent,;
        nFF:                   nFF,;
        attentionScale:        double(c.Float("attention.scale")),;
        numExperts:            numExperts,;
        numExpertsUsed:        numExpertsUsed,;
        expertWeightsNorm:     c.Bool("expert_weights_norm", false),;
        expertWeightsScale:    c.Float("expert_weights_scale", 1.0),;
        expertWeightsNormClip: c.Float("expert_weights_norm_clip", 0),;
    }
        var convDim = max(0, opts.ssmDConv-1);
        var convChannels = opts.ssmDInner + 2*opts.ssmNGroup*opts.ssmDState;
        var ssmHeadDim = 0;
        if opts.ssmNHead > 0 {
        ssmHeadDim = opts.ssmDInner / opts.ssmNHead;
    }
        var ssmStateSize = opts.ssmDState * ssmHeadDim * opts.ssmNHead;
        var m = Model{
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
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        Layers:  layers,;
        Options: opts,;
    }
        m.Cache = NewHybridCache(convDim, convChannels, ssmStateSize);
        return &m, null;
    }

    public static void init() {
        model.Register("nemotron_h", New);
        model.Register("nemotron_h_moe", New);
    }
        var _ model.Model = (*Model)(null);

    public static class Dense {
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (d *Dense) Forward(ctx ml.Context, x ml.Tensor, opts *Options) ml.Tensor {
        var up = d.Up.Forward(ctx, x);
        up = up.RELU(ctx);
        up = up.Mul(ctx, up) // Square;
        return d.Down.Forward(ctx, up);
    }

    public static class MoESparse {
        public *nn.Linear Router;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
        public ml.Tensor Bias;
        public *nn.Linear LatentIn;
        public *nn.Linear LatentOut;
        public *nn.Linear SharedUp;
        public *nn.Linear SharedDown;
    }
        func (m *MoESparse) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var hiddenDim = hiddenStates.Dim(0);
        var seqLen = hiddenStates.Dim(1);
        var batchSize = hiddenStates.Dim(2);
        if batchSize == 0 {
        batchSize = 1;
    }
        var hiddenStates2D = hiddenStates.Reshape(ctx, hiddenDim, seqLen*batchSize);
        var routerLogits = m.Router.Forward(ctx, hiddenStates2D);
        var probs = routerLogits.Sigmoid(ctx);
        var selectionProbs = probs;
        if m.Bias != null {
        selectionProbs = selectionProbs.Add(ctx, m.Bias);
    }
        var selectedExperts = selectionProbs.TopK(ctx, opts.numExpertsUsed);
        var routingWeights = probs.Reshape(ctx, 1, opts.numExperts, hiddenStates2D.Dim(1)).Rows(ctx, selectedExperts);
        if opts.expertWeightsNorm {
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, hiddenStates2D.Dim(1));
        var weightsSum = routingWeights.SumRows(ctx);
        weightsSum = weightsSum.Clamp(ctx, 6.103515625e-5, float32(math.MaxFloat32));
        routingWeights = routingWeights.Div(ctx, weightsSum);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenStates2D.Dim(1));
    }
        if opts.expertWeightsScale != 1.0 {
        routingWeights = routingWeights.Scale(ctx, double(opts.expertWeightsScale));
    }
        var routedInput = hiddenStates2D;
        if m.LatentIn != null {
        routedInput = m.LatentIn.Forward(ctx, routedInput);
    }
        var hiddenStates3D = routedInput.Reshape(ctx, routedInput.Dim(0), 1, routedInput.Dim(1));
        var upOut = m.Up.Forward(ctx, hiddenStates3D, selectedExperts);
        upOut = upOut.RELU(ctx);
        upOut = upOut.Mul(ctx, upOut) // Square;
        var experts = m.Down.Forward(ctx, upOut, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var moeOut = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        moeOut = moeOut.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        if m.LatentOut != null {
        moeOut = m.LatentOut.Forward(ctx, moeOut);
    }
        if m.SharedUp != null {
        var sharedUp = m.SharedUp.Forward(ctx, hiddenStates2D);
        sharedUp = sharedUp.RELU(ctx);
        sharedUp = sharedUp.Mul(ctx, sharedUp) // Square;
        var sharedOut = m.SharedDown.Forward(ctx, sharedUp);
        moeOut = moeOut.Add(ctx, sharedOut);
    }
        return moeOut;
    }
}
