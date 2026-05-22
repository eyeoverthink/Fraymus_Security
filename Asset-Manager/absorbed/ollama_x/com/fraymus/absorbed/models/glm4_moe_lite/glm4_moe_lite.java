package com.fraymus.absorbed.models.glm4_moe_lite;

import java.util.*;
import java.io.*;

public class glm4_moe_lite {
        "encoding/json";
        "fmt";
        "math";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model";
        "github.com/ollama/ollama/x/mlxrunner/model/base";
        "github.com/ollama/ollama/x/models/nn";
        "github.com/ollama/ollama/x/tokenizer";
        );

    public static void init() {
        base.Register("Glm4MoeLiteForCausalLM", newModel);
        base.Register("GLM4MoeLite", newModel);
    }

    public static class RopeScaling {
        public float32 Factor;
        public float32 MscaleAllDim;
    }

    public static class Config {
        public int32 HiddenSize;
        public int32 NumHiddenLayers;
        public int32 IntermediateSize;
        public int32 MoEIntermediateSize;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 VocabSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public int32 MaxPositionEmbeddings;
        public boolean AttentionBias;
        public int32 QLoraRank;
        public int32 KVLoraRank;
        public int32 QKRopeHeadDim;
        public int32 QKNopeHeadDim;
        public int32 VHeadDim;
        public int32 NRoutedExperts;
        public int32 NSharedExperts;
        public int32 NumExpertsPerTok;
        public float32 RoutedScalingFactor;
        public boolean NormTopKProb;
        public int32 FirstKDenseReplace;
        public int32 NGroup;
        public int32 TopKGroup;
        public *RopeScaling RopeScaling;
        public int QuantGroupSize;
        public int QuantBits;
        public String QuantMode;
        public map[String]*model.TensorQuantInfo TensorQuant;
        public int32 QHeadDim;
        public float32 Scale;
    }

    public static class MLAAttention {
        public nn.LinearLayer QAProj;
        public *nn.RMSNorm QALayerNorm;
        public nn.LinearLayer QBProj;
        public nn.LinearLayer KVAProjWithMQA;
        public *nn.RMSNorm KVALayerNorm;
        public *nn.MultiLinear EmbedQ;
        public *nn.MultiLinear UnembedOut;
        public nn.LinearLayer OProj;
    }
        func (a *MLAAttention) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var q = a.QAProj.Forward(x);
        q = a.QALayerNorm.Forward(q, cfg.RMSNormEps);
        q = a.QBProj.Forward(q);
        q = mlx.Reshape(q, B, L, cfg.NumAttentionHeads, cfg.QHeadDim);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        var qNope = mlx.SliceStartStop(q, []int32{0, 0, 0, 0}, []int32{B, cfg.NumAttentionHeads, L, cfg.QKNopeHeadDim});
        var qPE = mlx.SliceStartStop(q, []int32{0, 0, 0, cfg.QKNopeHeadDim}, []int32{B, cfg.NumAttentionHeads, L, cfg.QHeadDim});
        var compressedKV = a.KVAProjWithMQA.Forward(x);
        var kvCompressed = mlx.SliceStartStop(compressedKV, []int32{0, 0, 0}, []int32{B, L, cfg.KVLoraRank});
        var kPE = mlx.SliceStartStop(compressedKV, []int32{0, 0, cfg.KVLoraRank}, []int32{B, L, cfg.KVLoraRank + cfg.QKRopeHeadDim});
        kPE = mlx.Reshape(kPE, B, L, 1, cfg.QKRopeHeadDim);
        kPE = mlx.Transpose(kPE, 0, 2, 1, 3);
        var kvLatent = a.KVALayerNorm.Forward(kvCompressed, cfg.RMSNormEps);
        kvLatent = mlx.ExpandDims(kvLatent, 1);
        var offset = 0;
        if c != null {
        offset = c.Offset();
    }
        qPE = mlx.RoPEWithBase(qPE, int(cfg.QKRopeHeadDim), true, cfg.RopeTheta, 1.0, offset);
        kPE = mlx.RoPEWithBase(kPE, int(cfg.QKRopeHeadDim), true, cfg.RopeTheta, 1.0, offset);
        var qLatent = a.EmbedQ.Forward(qNope);
        var keys = mlx.Concatenate([]*mlx.Array{kvLatent, kPE}, 3);
        var cachedL = L;
        if c != null {
        var placeholderValues = mlx.ZerosF32([]int32{B, 1, L, 0});
        keys, _ = c.Update(keys, placeholderValues);
        cachedL = int32(keys.Dim(2));
    }
        var values = mlx.SliceStartStop(keys, []int32{0, 0, 0, 0}, []int32{B, 1, cachedL, cfg.KVLoraRank});
        var queries = mlx.Concatenate([]*mlx.Array{qLatent, qPE}, 3);
        var out = mlx.ScaledDotProductAttentionCausal(queries, keys, values, cfg.Scale, L > 1);
        out = a.UnembedOut.Forward(out);
        out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*cfg.VHeadDim);
        return a.OProj.Forward(out);
    }

    public static class DenseMLP {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }
        func (m *DenseMLP) Forward(x *mlx.Array) *mlx.Array {
        return m.DownProj.Forward(mlx.SwiGLU(m.GateProj.Forward(x), m.UpProj.Forward(x)));
    }

    public static class MoEGate {
        public nn.LinearLayer Gate;
        public *mlx.Array EScoreCorrectionBias;
    }
        func (g *MoEGate) Forward(x *mlx.Array, cfg *Config) (*mlx.Array, *mlx.Array) {
        var gates = g.Gate.Forward(x);
        var scores = mlx.Sigmoid(gates);
        var origScores = scores;
        if g.EScoreCorrectionBias != null {
        scores = mlx.Add(scores, g.EScoreCorrectionBias);
    }
        var topK = cfg.NumExpertsPerTok;
        var negScores = mlx.Neg(scores);
        var inds = mlx.Argpartition(negScores, int(topK)-1, -1);
        var dims = inds.Dims();
        inds = mlx.SliceStartStop(inds, []int32{0, 0, 0}, []int32{int32(dims[0]), int32(dims[1]), topK});
        scores = mlx.TakeAlongAxis(origScores, inds, -1);
        if topK > 1 && cfg.NormTopKProb {
        var sumScores = mlx.Sum(scores, -1, true);
        scores = mlx.Div(scores, sumScores);
    }
        scores = mlx.MulScalar(scores, cfg.RoutedScalingFactor);
        return inds, scores;
    }

    public static class SwitchMLP {
        public *mlx.Array GateWeight;
        public *mlx.Array UpWeight;
        public *mlx.Array DownWeight;
        public GateScales, GateWeightQ,;
        public UpScales, UpWeightQ,;
        public DownScales, DownWeightQ,;
        public int GateBits;
        public int UpBits;
        public int DownBits;
        public int GateGroupSize;
        public int UpGroupSize;
        public int DownGroupSize;
        public boolean UseQuantized;
    }
        func (s *SwitchMLP) Forward(x *mlx.Array, indices *mlx.Array, cfg *Config) *mlx.Array {
        var dims = x.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var topK = cfg.NumExpertsPerTok;
        var xExpanded = mlx.ExpandDims(mlx.ExpandDims(x, -2), -2);
        var xFlat = mlx.Reshape(xExpanded, B*L, 1, 1, cfg.HiddenSize);
        var idxFlat = mlx.Reshape(indices, B*L, topK);
        var doSort = B*L >= 64;
        var invOrder *mlx.Array;
        var n = B * L * topK;
        if doSort {
        var idxAll = mlx.Flatten(idxFlat);
        var order = mlx.Argsort(idxAll, 0);
        invOrder = mlx.Argsort(order, 0);
        xFlat = mlx.ExpandDims(mlx.Take(mlx.Squeeze(xFlat, 1), mlx.FloorDivideScalar(order, topK), 0), 1);
        idxFlat = mlx.Reshape(mlx.Take(idxAll, order, 0), n, 1);
    }
        var gate, up, hidden, down *mlx.Array;
        if s.UseQuantized {
        gate = mlx.GatherQMM(xFlat, s.GateWeightQ, s.GateScales, s.GateBiases,;
        null, idxFlat, true, s.GateGroupSize, s.GateBits, cfg.QuantMode, doSort);
        up = mlx.GatherQMM(xFlat, s.UpWeightQ, s.UpScales, s.UpBiases,;
        null, idxFlat, true, s.UpGroupSize, s.UpBits, cfg.QuantMode, doSort);
        hidden = mlx.SwiGLU(gate, up);
        down = mlx.GatherQMM(hidden, s.DownWeightQ, s.DownScales, s.DownBiases,;
        null, idxFlat, true, s.DownGroupSize, s.DownBits, cfg.QuantMode, doSort);
        } else {
        gate = mlx.GatherMM(xFlat, mlx.Transpose(s.GateWeight, 0, 2, 1), null, idxFlat, doSort);
        up = mlx.GatherMM(xFlat, mlx.Transpose(s.UpWeight, 0, 2, 1), null, idxFlat, doSort);
        hidden = mlx.SwiGLU(gate, up);
        down = mlx.GatherMM(hidden, mlx.Transpose(s.DownWeight, 0, 2, 1), null, idxFlat, doSort);
    }
        if doSort {
        down = mlx.Reshape(mlx.Take(mlx.Squeeze(mlx.Squeeze(down, 2), 1), invOrder, 0), B*L, topK, cfg.HiddenSize);
        } else {
        down = mlx.Squeeze(down, 2);
    }
        return mlx.Reshape(down, B, L, topK, cfg.HiddenSize);
    }

    public static class SharedExperts {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }
        func (s *SharedExperts) Forward(x *mlx.Array) *mlx.Array {
        return s.DownProj.Forward(mlx.SwiGLU(s.GateProj.Forward(x), s.UpProj.Forward(x)));
    }

    public static class MoE {
        public *MoEGate Gate;
        public *SwitchMLP SwitchMLP;
        public *SharedExperts SharedExperts;
    }
        func (m *MoE) Forward(x *mlx.Array, cfg *Config) *mlx.Array {
        var dims = x.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var inds, scores = m.Gate.Forward(x, cfg);
        var expertOut = m.SwitchMLP.Forward(x, inds, cfg);
        var scoresExpanded = mlx.ExpandDims(scores, -1);
        var y = mlx.Sum(mlx.Mul(expertOut, scoresExpanded), 2, false);
        if m.SharedExperts != null {
        y = mlx.Add(y, m.SharedExperts.Forward(x));
    }
        return mlx.Reshape(y, B, L, cfg.HiddenSize);
    }

    public static class DenseBlock {
        public *MLAAttention Attention;
        public *DenseMLP MLP;
        public *nn.RMSNorm InputLayerNorm;
        public *nn.RMSNorm PostAttentionLayerNorm;
    }
        func (b *DenseBlock) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var r = b.Attention.Forward(b.InputLayerNorm.Forward(x, cfg.RMSNormEps), c, B, L, cfg);
        var h = mlx.Add(x, r);
        r = b.MLP.Forward(b.PostAttentionLayerNorm.Forward(h, cfg.RMSNormEps));
        return mlx.Add(h, r);
    }

    public static class MoEBlock {
        public *MLAAttention Attention;
        public *MoE MoE;
        public *nn.RMSNorm InputLayerNorm;
        public *nn.RMSNorm PostAttentionLayerNorm;
    }
        func (b *MoEBlock) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var r = b.Attention.Forward(b.InputLayerNorm.Forward(x, cfg.RMSNormEps), c, B, L, cfg);
        var h = mlx.Add(x, r);
        r = b.MoE.Forward(b.PostAttentionLayerNorm.Forward(h, cfg.RMSNormEps), cfg);
        return mlx.Add(h, r);
    }
        type Block interface {
        Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array;
    }

    public static class Model {
        public nn.EmbeddingLayer EmbedTokens;
        public []Block Layers;
        public *nn.RMSNorm Norm;
        public nn.LinearLayer LMHead;
        public *tokenizer.Tokenizer tok;
    }

    public static float32 computeScale(*Config cfg) {
        var keyLength = cfg.QKNopeHeadDim + cfg.QKRopeHeadDim;
        var scale = float32(1.0 / math.Sqrt(double(keyLength)));
        if cfg.RopeScaling != null && cfg.RopeScaling.MscaleAllDim > 0 && cfg.RopeScaling.Factor > 1 {
        var s = 0.1*cfg.RopeScaling.MscaleAllDim*float32(math.Log(double(cfg.RopeScaling.Factor))) + 1.0;
        scale *= s * s;
    }
        return scale;
    }

    public static boolean supportsGatherQMM(String mode, int bits) {
        return mode == "affine" && (bits == 4 || bits == 8);
    }

    public static class ExpertWeight {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array Biases;
        public int Bits;
        public int GroupSize;
    }
        func loadExpertWeight(tensors map[String]*mlx.Array, path String, useQuantized boolean, cfg *Config) *ExpertWeight {
        var w = tensors[path+".weight"];
        if w == null {
        return null;
    }
        var scales = tensors[path+".weight_scale"];
        if scales != null {
        var qbiases = tensors[path+".weight_qbias"];
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        path+".weight",;
        w,;
        scales,;
        );
        if useQuantized && supportsGatherQMM(mode, bits) {
        return &ExpertWeight{Weight: w, Scales: scales, Biases: qbiases, Bits: bits, GroupSize: groupSize}
    }
        return &ExpertWeight{Weight: mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode)}
    }
        return &ExpertWeight{Weight: w}
    }

    public static class StackedExpertWeights {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array Biases;
        public int Bits;
        public int GroupSize;
    }
        func collectAndStackExpertWeights(;
        tensors map[String]*mlx.Array,;
        prefix String,;
        projName String,;
        numExperts int32,;
        useQuantized boolean,;
        cfg *Config,;
        ) *StackedExpertWeights {
        var w, s, b []*mlx.Array;
        var bits, groupSize int;
        var for e = int32(0); e < numExperts; e++ {
        var path = fmt.Sprintf("%s.mlp.experts.%d.%s", prefix, e, projName);
        var ew = loadExpertWeight(tensors, path, useQuantized, cfg);
        if ew == null {
        continue;
    }
        w = append(w, ew.Weight);
        if ew.Scales != null {
        s = append(s, ew.Scales);
    }
        if ew.Biases != null {
        b = append(b, ew.Biases);
    }
        if e == 0 {
        bits = ew.Bits;
        groupSize = ew.GroupSize;
    }
    }
        var result = &StackedExpertWeights{Bits: bits, GroupSize: groupSize}
        if len(w) > 0 {
        result.Weight = mlx.Stack(w, 0);
        if len(s) > 0 {
        result.Scales = mlx.Stack(s, 0);
    }
        if len(b) > 0 {
        result.Biases = mlx.Stack(b, 0);
    }
    }
        return result;
    }

    public static void sanitizeExpertWeights(map[String]*mlx.Array tensors, String prefix, int32 numExperts, boolean useQuantized, *StackedExpertWeights down) {
        gate = collectAndStackExpertWeights(tensors, prefix, "gate_proj", numExperts, useQuantized, cfg);
        up = collectAndStackExpertWeights(tensors, prefix, "up_proj", numExperts, useQuantized, cfg);
        down = collectAndStackExpertWeights(tensors, prefix, "down_proj", numExperts, useQuantized, cfg);
        return gate, up, down;
    }

    public static void sanitizeMLAWeights(map[String]*mlx.Array tensors, String prefix) {
        var path = prefix + ".self_attn.kv_b_proj";
        var w = tensors[path+".weight"];
        if w == null {
        return null, null;
    }
        var if scales = tensors[path+".weight_scale"]; scales != null {
        var qbiases = tensors[path+".weight_qbias"];
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        path+".weight",;
        w,;
        scales,;
        );
        w = mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode);
    }
        var headDim = cfg.QKNopeHeadDim + cfg.VHeadDim;
        w = mlx.Reshape(w, cfg.NumAttentionHeads, headDim, cfg.KVLoraRank);
        var wk = mlx.SliceStartStop(w, []int32{0, 0, 0}, []int32{cfg.NumAttentionHeads, cfg.QKNopeHeadDim, cfg.KVLoraRank});
        var wv = mlx.SliceStartStop(w, []int32{0, cfg.QKNopeHeadDim, 0}, []int32{cfg.NumAttentionHeads, headDim, cfg.KVLoraRank});
        var embedQ = mlx.Transpose(wk, 0, 2, 1);
        var unembedOut = wv;
        return embedQ, unembedOut;
    }

    public static void newModel() {
        var configData, err = root.Manifest.ReadConfig("config.json");
        if err != null {
        return null, fmt.Errorf("load config: %w", err);
    }
        var cfg Config;
        var if err = json.Unmarshal(configData, &cfg); err != null {
        return null, fmt.Errorf("parse config: %w", err);
    }
        cfg.QHeadDim = cfg.QKNopeHeadDim + cfg.QKRopeHeadDim;
        cfg.Scale = computeScale(&cfg);
        var if qt = root.QuantType(); qt != "" {
        cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode = model.QuantizationParams(qt);
        var if gs = root.GroupSize(); gs > 0 {
        cfg.QuantGroupSize = gs;
    }
        } else {
        cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode = model.QuantizationParams("");
    }
        cfg.TensorQuant = root.AllTensorQuant();
        var tokData, err = root.Manifest.ReadConfig("tokenizer.json");
        if err != null {
        return null, fmt.Errorf("load tokenizer config: %w", err);
    }
        var tokConfig = &tokenizer.TokenizerConfig{
        ConfigJSON: configData,;
    }
        var if genConfigData, err = root.Manifest.ReadConfig("generation_config.json"); err == null {
        tokConfig.GenerationConfigJSON = genConfigData;
    }
        var if tokConfigData, err = root.Manifest.ReadConfig("tokenizer_config.json"); err == null {
        tokConfig.TokenizerConfigJSON = tokConfigData;
    }
        var tok, err = tokenizer.LoadFromBytesWithConfig(tokData, tokConfig);
        if err != null {
        return null, fmt.Errorf("parse tokenizer: %w", err);
    }
        var m = &Model{
        Layers: make([]Block, cfg.NumHiddenLayers),;
        Config: &cfg,;
        tok:    tok,;
    }
        return m, null;
    }
        func (m *Model) LoadWeights(tensors map[String]*mlx.Array) error {
        var cfg = m.Config;
        var linears = model.NewLinearFactory(tensors, cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant);
        var useQuantized = supportsGatherQMM(cfg.QuantMode, cfg.QuantBits);
        if !useQuantized && cfg.TensorQuant != null {
        var for _, tq = range cfg.TensorQuant {
        if tq == null {
        continue;
    }
        var _, bits, mode = model.QuantizationParams(tq.QuantType);
        if supportsGatherQMM(mode, bits) {
        useQuantized = true;
        break;
    }
    }
    }
        m.EmbedTokens = model.MakeEmbeddingLayer(tensors, "model.embed_tokens", cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant);
        var if w = tensors["model.norm.weight"]; w != null {
        m.Norm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        m.LMHead = linears.Make("lm_head");
        var for i = int32(0); i < cfg.NumHiddenLayers; i++ {
        var prefix = fmt.Sprintf("model.layers.%d", i);
        var attn = &MLAAttention{}
        attn.QAProj = linears.Make(prefix + ".self_attn.q_a_proj");
        var if w = tensors[prefix+".self_attn.q_a_layernorm.weight"]; w != null {
        attn.QALayerNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        attn.QBProj = linears.Make(prefix + ".self_attn.q_b_proj");
        attn.KVAProjWithMQA = linears.Make(prefix + ".self_attn.kv_a_proj_with_mqa");
        var if w = tensors[prefix+".self_attn.kv_a_layernorm.weight"]; w != null {
        attn.KVALayerNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        attn.OProj = linears.Make(prefix + ".self_attn.o_proj");
        var embedQ, unembedOut = sanitizeMLAWeights(tensors, prefix, cfg);
        attn.EmbedQ = nn.NewMultiLinear(embedQ);
        attn.UnembedOut = nn.NewMultiLinear(unembedOut);
        var inputLN = tensors[prefix+".input_layernorm.weight"];
        var postAttnLN = tensors[prefix+".post_attention_layernorm.weight"];
        if i < cfg.FirstKDenseReplace {
        var block = &DenseBlock{Attention: attn}
        if inputLN != null {
        block.InputLayerNorm = nn.NewRMSNorm(inputLN, cfg.RMSNormEps);
    }
        if postAttnLN != null {
        block.PostAttentionLayerNorm = nn.NewRMSNorm(postAttnLN, cfg.RMSNormEps);
    }
        block.MLP = &DenseMLP{
        GateProj: linears.Make(prefix + ".mlp.gate_proj"),;
        UpProj:   linears.Make(prefix + ".mlp.up_proj"),;
        DownProj: linears.Make(prefix + ".mlp.down_proj"),;
    }
        m.Layers[i] = block;
        } else {
        var block = &MoEBlock{Attention: attn}
        if inputLN != null {
        block.InputLayerNorm = nn.NewRMSNorm(inputLN, cfg.RMSNormEps);
    }
        if postAttnLN != null {
        block.PostAttentionLayerNorm = nn.NewRMSNorm(postAttnLN, cfg.RMSNormEps);
    }
        var gate, up, down = sanitizeExpertWeights(tensors, prefix, cfg.NRoutedExperts, useQuantized, cfg);
        var switchMLP = &SwitchMLP{UseQuantized: useQuantized}
        if useQuantized {
        switchMLP.GateWeightQ = gate.Weight;
        switchMLP.GateScales = gate.Scales;
        switchMLP.GateBiases = gate.Biases;
        switchMLP.GateBits = gate.Bits;
        switchMLP.GateGroupSize = gate.GroupSize;
        switchMLP.UpWeightQ = up.Weight;
        switchMLP.UpScales = up.Scales;
        switchMLP.UpBiases = up.Biases;
        switchMLP.UpBits = up.Bits;
        switchMLP.UpGroupSize = up.GroupSize;
        switchMLP.DownWeightQ = down.Weight;
        switchMLP.DownScales = down.Scales;
        switchMLP.DownBiases = down.Biases;
        switchMLP.DownBits = down.Bits;
        switchMLP.DownGroupSize = down.GroupSize;
        } else {
        switchMLP.GateWeight = gate.Weight;
        switchMLP.UpWeight = up.Weight;
        switchMLP.DownWeight = down.Weight;
    }
        var moeGate = &MoEGate{}
        moeGate.Gate = linears.Make(prefix + ".mlp.gate");
        var if bias = tensors[prefix+".mlp.gate.e_score_correction_bias"]; bias != null {
        moeGate.EScoreCorrectionBias = bias;
    }
        block.MoE = &MoE{
        Gate:      moeGate,;
        SwitchMLP: switchMLP,;
    }
        if cfg.NSharedExperts > 0 {
        block.MoE.SharedExperts = &SharedExperts{
        GateProj: linears.Make(prefix + ".mlp.shared_experts.gate_proj"),;
        UpProj:   linears.Make(prefix + ".mlp.shared_experts.up_proj"),;
        DownProj: linears.Make(prefix + ".mlp.shared_experts.down_proj"),;
    }
    }
        m.Layers[i] = block;
    }
    }
        return null;
    }
        func (m *Model) Forward(tokens *mlx.Array, caches []cache.Cache) *mlx.Array {
        var dims = tokens.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var h = m.EmbedTokens.Forward(tokens);
        var for i, layer = range m.Layers {
        var c cache.Cache;
        if caches != null {
        c = caches[i];
    }
        h = layer.Forward(h, c, B, L, m.Config);
    }
        h = m.Norm.Forward(h, m.RMSNormEps);
        return h;
    }
        func (m *Model) Unembed(x *mlx.Array) *mlx.Array {
        return m.LMHead.Forward(x);
    }
        func (m *Model) NumLayers() int { return len(m.Layers) }
        func (m *Model) MaxContextLength() int { return int(m.MaxPositionEmbeddings) }
        func (m *Model) VocabSize() int32 { return m.Config.VocabSize }
        func (m *Model) Tokenizer() *tokenizer.Tokenizer { return m.tok }
        func (m *Model) NewCache(maxSeqLen int32) []cache.Cache {
        var caches = make([]cache.Cache, len(m.Layers));
        var for i = range caches {
        caches[i] = cache.NewKVCache();
    }
        return caches;
    }
        func (m *Model) FormatPrompt(prompt String) String {
        return "[gMASK]<sop><|user|>" + prompt + "<|assistant|><think>";
    }
        func (m *Model) FormatPromptWithThinking(prompt String, think boolean) String {
        if think {
        return "[gMASK]<sop><|user|>" + prompt + "<|assistant|><think>";
    }
        return "[gMASK]<sop><|user|>" + prompt + "<|assistant|></think>";
    }
        func (m *Model) NewRenderer() *Renderer {
        return &Renderer{}
    }
        func (m *Model) NewParser() *Parser {
        return &Parser{}
    }
}
