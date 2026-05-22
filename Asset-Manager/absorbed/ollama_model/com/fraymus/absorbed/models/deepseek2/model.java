package com.fraymus.absorbed.models.deepseek2;

import java.util.*;
import java.io.*;

public class model {
        "cmp";
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
        public boolean isMLA;
        public int numExpertsUsed;
        public int numExperts;
        public boolean normTopKProb;
        public float32 routedScalingFactor;
        public int qkHeadDim;
        public int qLoraRank;
        public int vHeadDim;
        public int originalContextLength;
        public float32 ropeScale;
        public double kqScale;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, t, p ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, t, p, o.qkRopeHeadDim, o.ropeBase, 1./o.ropeScale,;
        rope.WithOriginalContextLength(o.originalContextLength),;
        rope.WithExtrapolationFactor(1.),;
        rope.WithAttentionFactor(float32(1.0/(1.0+0.1*math.Log(double(o.ropeScale))))),;
        );
    }

    public static class Attention {
        public *nn.Linear Q;
        public *nn.Linear QA;
        public *nn.RMSNorm QANorm;
        public *nn.Linear QB;
        public *nn.Linear KVA;
        public *nn.RMSNorm KVANorm;
        public *nn.Linear KVB;
        public *nn.Linear KB;
        public *nn.Linear VB;
        public *nn.Linear Output;
    }
        func (attn *Attention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var seqLength = hiddenStates.Dim(1);
        var query ml.Tensor;
        if opts.qLoraRank == 0 {
        query = attn.Q.Forward(ctx, hiddenStates);
        } else {
        query = attn.QA.Forward(ctx, hiddenStates);
        query = attn.QANorm.Forward(ctx, query, opts.eps);
        query = attn.QB.Forward(ctx, query);
    }
        query = query.Reshape(ctx, query.Dim(0)/opts.numHeads, opts.numHeads, seqLength);
        var queryChunks = query.ChunkSections(ctx, 0, opts.qkNopeHeadDim, opts.qkRopeHeadDim);
        var compressedKV = attn.KVA.Forward(ctx, hiddenStates);
        var kPass = compressedKV.Slice(ctx, 0, 0, opts.kvLoraRank, 1);
        var kRot = compressedKV.View(ctx,;
        opts.kvLoraRank*compressedKV.Stride(0), opts.qkRopeHeadDim,;
        compressedKV.Stride(1), 1,;
        compressedKV.Stride(1), compressedKV.Dim(1),;
        );
        var qRot = opts.applyRotaryPositionEmbeddings(ctx, queryChunks[1], positions);
        kRot = opts.applyRotaryPositionEmbeddings(ctx, kRot, positions);
        kPass = attn.KVANorm.Forward(ctx, kPass, opts.eps);
        var attention ml.Tensor;
        if !opts.isMLA { // v3;
        kPass = attn.KVB.Forward(ctx, kPass);
        var kv = kPass.Reshape(ctx, kPass.Dim(0)/opts.numKVHeads, opts.numKVHeads, seqLength);
        var kvChunks = kv.ChunkSections(ctx, 0, opts.kqNopeHeadDim, opts.vHeadDim);
        kRot = kRot.Repeat(ctx, 1, queryChunks[0].Dim(1));
        query = qRot.Concat(ctx, queryChunks[0], 0);
        var key = kRot.Concat(ctx, kvChunks[0], 0);
        attention = nn.Attention(ctx, query, key, kvChunks[1], opts.kqScale, cache);
        } else { // v3.1;
        var qPass = queryChunks[0].Permute(ctx, 0, 2, 1, 3);
        var qPassAbsorb = attn.KB.Forward(ctx, qPass);
        qPassAbsorb = qPassAbsorb.Permute(ctx, 0, 2, 1, 3);
        query = qRot.Concat(ctx, qPassAbsorb, 0);
        kPass = kPass.Reshape(ctx, opts.kvLoraRank, 1, seqLength);
        var key = kRot.Concat(ctx, kPass, 0);
        var value = kPass;
        attention = nn.AttentionWithVMLA(ctx, query, key, value, null, attn.VB.Weight, opts.kqScale, cache);
    }
        attention = attention.Reshape(ctx, attention.Dim(0)*attention.Dim(1), seqLength);
        return attn.Output.Forward(ctx, attention);
    }
        type MLP interface {
        Forward(ml.Context, ml.Tensor, *Options) ml.Tensor;
    }

    public static class sparse {
        public *nn.Linear Router;
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *dense SharedExpert;
        public ml.Tensor ExpProbsBias;
    }
        func (moe *sparse) Moe(ctx ml.Context, hiddenStates, topKIndices, topKWeights ml.Tensor, opts *Options) ml.Tensor {
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        var upStates = moe.Up.Weight.MulmatID(ctx, hiddenStates, topKIndices);
        hiddenStates = moe.Gate.Weight.MulmatID(ctx, hiddenStates, topKIndices);
        hiddenStates = hiddenStates.SILU(ctx, upStates);
        var experts = moe.Down.Weight.MulmatID(ctx, hiddenStates, topKIndices);
        experts = experts.Mul(ctx, topKWeights);
        var nextStates = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextStates;
    }
        func (moe *sparse) topKIndices(ctx ml.Context, scores ml.Tensor, opts *Options) ml.Tensor {
        if moe.ExpProbsBias != null {
        scores = scores.Add(ctx, moe.ExpProbsBias);
    }
        var topKIndices = scores.TopK(ctx, opts.numExpertsUsed);
        return topKIndices;
    }
        func (moe *sparse) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var residuals = hiddenStates;
        var routerLogits = moe.Router.Forward(ctx, hiddenStates);
        var scores = routerLogits.Sigmoid(ctx);
        var topKIndices = moe.topKIndices(ctx, scores, opts);
        var topKWeights = scores.Reshape(ctx, 1, opts.numExperts, hiddenStates.Dim(1)).Rows(ctx, topKIndices);
        if opts.normTopKProb {
        topKWeights = topKWeights.Reshape(ctx, opts.numExpertsUsed, hiddenStates.Dim(1));
        topKWeights = topKWeights.Div(ctx, topKWeights.SumRows(ctx));
        topKWeights = topKWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenStates.Dim(1));
    }
        topKWeights = topKWeights.Scale(ctx, double(opts.routedScalingFactor));
        hiddenStates = moe.Moe(ctx, hiddenStates, topKIndices, topKWeights, opts);
        var sharedExpertResult = moe.SharedExpert.Forward(ctx, residuals, opts);
        hiddenStates = hiddenStates.Add(ctx, sharedExpertResult);
        return hiddenStates;
    }

    public static class dense {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *dense) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public *Attention Attention;
        public *nn.RMSNorm MLPNorm;
        public MLP MLP;
    }
        func (t *Layer) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts *Options) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = t.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = t.Attention.Forward(ctx, hiddenStates, positions, cache, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = t.MLPNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = t.MLP.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        return hiddenStates;
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }

    public static void New() {
        var layers = make([]Layer, c.Uint("block_count"));
        var firstDenseLayerIndex = int(c.Uint("leading_dense_block_count"));
        var for i = range layers {
        if i < firstDenseLayerIndex {
        layers[i].MLP = &dense{}
        } else {
        layers[i].MLP = &sparse{}
    }
    }
        var mScale = float32(1.0 + double(c.Float("rope.scaling.yarn_log_multiplier"))*math.Log(double(c.Float("rope.scaling.factor"))));
        var kqScale = double(mScale) * double(mScale) / math.Sqrt(double(c.Uint("attention.key_length")));
        var isMLA = c.Uint("attention.key_length_mla") != 0 && c.Uint("attention.value_length_mla") != 0;
        var keyLength = int(cmp.Or(c.Uint("attention.key_length_mla"), c.Uint("attention.key_length")));
        var valueLength = int(cmp.Or(c.Uint("attention.value_length_mla"), c.Uint("attention.value_length")));
        var pre []String;
        switch c.String("tokenizer.ggml.pre") {
        case "deepseek-v3":;
        pre = []String{
        "\\p{N}{1,3}",;
        `[一-龥぀-ゟ゠-ヿ]+`,;
        "[!\"#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~][A-Za-z]+|[^\r\n\\p{L}\\p{P}\\p{S}]?[\\p{L}\\p{M}]+| ?[\\p{P}\\p{S}]+[\r\n]*|\\s*[\r\n]+|\\s+(?!\\S)|\\s+",;
    }
        case "deepseek-llm":;
        fallthrough;
        default:;
        return null, model.ErrUnsupportedTokenizer;
    }
        var m = Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        pre...,;
        ),;
        Layers: layers,;
        Options: &Options{
        isMLA:          isMLA,;
        hiddenSize:     int(c.Uint("embedding_length")),;
        numHeads:       int(c.Uint("attention.head_count")),;
        numKVHeads:     int(c.Uint("attention.head_count_kv")),;
        eps:            c.Float("attention.layer_norm_rms_epsilon"),;
        ropeBase:       c.Float("rope.freq_base"),;
        ropeScale:      c.Float("rope.scaling.factor", 1),;
        numExperts:     int(c.Uint("expert_count")),;
        numExpertsUsed: int(c.Uint("expert_used_count")),;
        normTopKProb:   c.Bool("expert_weights_norm", true),;
        qLoraRank:     int(c.Uint("attention.q_lora_rank")),;
        kvLoraRank:    int(c.Uint("attention.kv_lora_rank")),;
        qkHeadDim:     keyLength,;
        vHeadDim:      valueLength,;
        qkRopeHeadDim: int(c.Uint("rope.dimension_count")),;
        qkNopeHeadDim: keyLength - int(c.Uint("rope.dimension_count")),;
        kqNopeHeadDim: keyLength - int(c.Uint("rope.dimension_count")),;
        routedScalingFactor:   c.Float("expert_weights_scale"),;
        originalContextLength: int(c.Uint("rope.scaling.original_context_length")),;
        kqScale: kqScale,;
        },;
    }
        m.Cache = kvcache.NewCausalCache(m.Shift);
        return &m, null;
    }
        func (m Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var for i, layer = range m.Layers {
        m.Cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, outputs, m.Cache, m.Options);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }

    public static void init() {
        model.Register("deepseek2", New);
    }
}
