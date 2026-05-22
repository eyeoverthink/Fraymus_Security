package com.fraymus.absorbed.models.qwen3_5;

import java.util.*;
import java.io.*;

public class qwen3_5 {
        "encoding/json";
        "fmt";
        "math";
        "strings";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model";
        "github.com/ollama/ollama/x/mlxrunner/model/base";
        "github.com/ollama/ollama/x/models/nn";
        "github.com/ollama/ollama/x/tokenizer";
        );

    public static void init() {
        base.Register("Qwen3_5ForCausalLM", NewModel);
        base.Register("Qwen3_5ForConditionalGeneration", NewModel);
        base.Register("Qwen3NextForCausalLM", NewModel);
        base.Register("Qwen3NextForConditionalGeneration", NewModel);
    }

    public static class RopeParameters {
        public String Type;
        public String RopeType;
        public float32 RopeTheta;
        public float32 PartialRotaryFactor;
    }

    public static class Config {
        public String ModelType;
        public int32 HiddenSize;
        public int32 IntermediateSize;
        public int32 NumHiddenLayers;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 HeadDim;
        public float32 RMSNormEps;
        public int32 VocabSize;
        public int32 MaxPositionEmbeddings;
        public boolean AttentionBias;
        public boolean TieWordEmbeddings;
        public []String LayerTypes;
        public int32 FullAttentionInterval;
        public int32 LinearNumValueHeads;
        public int32 LinearNumKeyHeads;
        public int32 LinearKeyHeadDim;
        public int32 LinearValueHeadDim;
        public int32 LinearConvKernelDim;
        public int32 DecoderSparseStep;
        public int32 SharedExpertGateRank;
        public int32 NumExperts;
        public int32 NumExpertsPerTok;
        public int32 SharedExpertIntermediateSize;
        public int32 MoeIntermediateSize;
        public boolean NormTopKProb;
        public []int32 MLPOnlyLayers;
        public float32 RopeTheta;
        public float32 PartialRotaryFactor;
        public map[String]any RopeScaling;
        public *RopeParameters RopeParameters;
        public int QuantGroupSize;
        public int QuantBits;
        public String QuantMode;
        public map[String]*model.TensorQuantInfo TensorQuant;
        public float32 Scale;
        public int32 RopeDim;
    }

    public static class Model {
        public nn.EmbeddingLayer EmbedTokens;
        public []*Layer Layers;
        public *nn.RMSNorm Norm;
        public nn.LinearLayer LMHead;
        public *tokenizer.Tokenizer tok;
        public String weightPrefix;
    }

    public static class Layer {
        public *nn.RMSNorm InputNorm;
        public *nn.RMSNorm PostAttentionNorm;
        public boolean IsLinear;
        public *FullAttention FullAttn;
        public *GatedDeltaNet Linear;
        public MLPBlock MLP;
    }

    public static class FullAttention {
        public nn.LinearLayer QProj;
        public nn.LinearLayer KProj;
        public nn.LinearLayer VProj;
        public nn.LinearLayer OProj;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
    }

    public static class GatedDeltaNet {
        public nn.LinearLayer InProjQKV;
        public nn.LinearLayer InProjZ;
        public nn.LinearLayer InProjB;
        public nn.LinearLayer InProjA;
        public nn.LinearLayer InProjQKVZ;
        public nn.LinearLayer InProjBA;
        public nn.LinearLayer OutProj;
        public *nn.Conv1d Conv1D;
        public *mlx.Array ConvWeight;
        public *mlx.Array NormWeight;
        public *mlx.Array DtBias;
        public *mlx.Array ALog;
        public *mlx.Array AExp;
    }
        type MLPBlock interface {
        Forward(x *mlx.Array, cfg *Config) *mlx.Array;
    }

    public static class DenseMLP {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }

    public static class SparseMoE {
        public nn.LinearLayer Gate;
        public *SwitchMLP SwitchMLP;
        public *DenseMLP SharedExpert;
        public nn.LinearLayer SharedExpertGate;
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

    public static class stackedExpertWeights {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array Biases;
        public int Bits;
        public int GroupSize;
        public String Mode;
    }

    public static void parseConfig() {
        var rawTop map[String]json.RawMessage;
        var if err = json.Unmarshal(configData, &rawTop); err != null {
        return Config{}, fmt.Errorf("parse config envelope: %w", err);
    }
        var cfg Config;
        var activeRaw = rawTop;
        var if textRaw, ok = rawTop["text_config"]; ok {
        var if err = json.Unmarshal(textRaw, &cfg); err != null {
        return Config{}, fmt.Errorf("parse text_config: %w", err);
    }
        var if err = json.Unmarshal(textRaw, &activeRaw); err != null {
        return Config{}, fmt.Errorf("parse text_config envelope: %w", err);
    }
        } else {
        var if err = json.Unmarshal(configData, &cfg); err != null {
        return Config{}, fmt.Errorf("parse config: %w", err);
    }
    }
        if cfg.HiddenSize <= 0 {
        return Config{}, fmt.Errorf("invalid hidden_size: %d", cfg.HiddenSize);
    }
        if cfg.NumHiddenLayers <= 0 {
        return Config{}, fmt.Errorf("invalid num_hidden_layers: %d", cfg.NumHiddenLayers);
    }
        if cfg.NumAttentionHeads <= 0 {
        return Config{}, fmt.Errorf("invalid num_attention_heads: %d", cfg.NumAttentionHeads);
    }
        if cfg.NumKeyValueHeads <= 0 {
        cfg.NumKeyValueHeads = cfg.NumAttentionHeads;
    }
        if cfg.HeadDim <= 0 {
        if cfg.HiddenSize%cfg.NumAttentionHeads != 0 {
        return Config{}, fmt.Errorf("hidden_size (%d) must be divisible by num_attention_heads (%d)", cfg.HiddenSize, cfg.NumAttentionHeads);
    }
        cfg.HeadDim = cfg.HiddenSize / cfg.NumAttentionHeads;
    }
        if cfg.HeadDim <= 0 {
        return Config{}, fmt.Errorf("invalid head_dim: %d", cfg.HeadDim);
    }
        if cfg.RMSNormEps == 0 {
        cfg.RMSNormEps = 1e-6;
    }
        if cfg.LinearConvKernelDim <= 0 {
        cfg.LinearConvKernelDim = 4;
    }
        if cfg.LinearNumKeyHeads <= 0 || cfg.LinearNumValueHeads <= 0 || cfg.LinearKeyHeadDim <= 0 || cfg.LinearValueHeadDim <= 0 {
        return Config{}, fmt.Errorf("invalid linear attention config (k_heads=%d v_heads=%d k_dim=%d v_dim=%d)",;
        cfg.LinearNumKeyHeads, cfg.LinearNumValueHeads, cfg.LinearKeyHeadDim, cfg.LinearValueHeadDim);
    }
        if cfg.LinearNumValueHeads%cfg.LinearNumKeyHeads != 0 {
        return Config{}, fmt.Errorf("linear_num_value_heads (%d) must be divisible by linear_num_key_heads (%d)", cfg.LinearNumValueHeads, cfg.LinearNumKeyHeads);
    }
        if cfg.RopeParameters != null {
        if cfg.RopeParameters.RopeTheta > 0 {
        cfg.RopeTheta = cfg.RopeParameters.RopeTheta;
    }
        if cfg.RopeParameters.PartialRotaryFactor > 0 {
        cfg.PartialRotaryFactor = cfg.RopeParameters.PartialRotaryFactor;
    }
    }
        if cfg.RopeTheta == 0 {
        cfg.RopeTheta = 100000.0;
    }
        if cfg.PartialRotaryFactor == 0 {
        cfg.PartialRotaryFactor = 0.25;
    }
        if cfg.PartialRotaryFactor < 0 {
        cfg.PartialRotaryFactor = 0.25;
    }
        var ropeDim = int32(float32(cfg.HeadDim) * cfg.PartialRotaryFactor);
        if ropeDim <= 0 {
        ropeDim = cfg.HeadDim;
    }
        if ropeDim > cfg.HeadDim {
        ropeDim = cfg.HeadDim;
    }
        cfg.RopeDim = ropeDim;
        if cfg.FullAttentionInterval <= 0 {
        var for i, lt = range cfg.LayerTypes {
        if strings.Contains(strings.ToLower(lt), "full") {
        cfg.FullAttentionInterval = int32(i + 1);
        break;
    }
    }
        if cfg.FullAttentionInterval <= 0 {
        cfg.FullAttentionInterval = 4;
    }
    }
        if cfg.FullAttentionInterval > cfg.NumHiddenLayers {
        cfg.FullAttentionInterval = cfg.NumHiddenLayers;
    }
        if cfg.NumExperts > 0 {
        if cfg.NumExpertsPerTok <= 0 {
        cfg.NumExpertsPerTok = 1;
    }
        if cfg.MoeIntermediateSize <= 0 {
        cfg.MoeIntermediateSize = cfg.IntermediateSize;
    }
        if cfg.SharedExpertIntermediateSize <= 0 {
        cfg.SharedExpertIntermediateSize = cfg.IntermediateSize;
    }
        var if _, ok = activeRaw["norm_topk_prob"]; !ok {
        cfg.NormTopKProb = true;
    }
        if cfg.DecoderSparseStep <= 0 {
        cfg.DecoderSparseStep = 1;
    }
    }
        cfg.Scale = float32(1.0 / math.Sqrt(double(cfg.HeadDim)));
        return cfg, null;
    }

    public static class tensorPathLayout {
        public String containerPrefix;
        public String modelPrefix;
    }
        func (l tensorPathLayout) modelPath(suffix String) String {
        return l.containerPrefix + l.modelPrefix + suffix;
    }

    public static tensorPathLayout resolveTensorPathLayout(map[String]*mlx.Array tensors) {
        var for _, layout = range []tensorPathLayout{
        {containerPrefix: "", modelPrefix: "model."},;
        {containerPrefix: "language_model.", modelPrefix: "model."},;
        {containerPrefix: "language_model.", modelPrefix: ""},;
        {containerPrefix: "model.language_model.", modelPrefix: "model."},;
        {containerPrefix: "model.language_model.", modelPrefix: ""},;
        } {
        if tensors[layout.modelPath("embed_tokens.weight")] != null {
        return layout;
    }
    }
        return tensorPathLayout{modelPrefix: "model."}
    }

    public static boolean layerIsLinear(*Config cfg, int32 layer) {
        if len(cfg.LayerTypes) == int(cfg.NumHiddenLayers) {
        var t = strings.ToLower(cfg.LayerTypes[layer]);
        return !strings.Contains(t, "full");
    }
        if cfg.FullAttentionInterval <= 0 {
        return true;
    }
        return (layer+1)%cfg.FullAttentionInterval != 0;
    }

    public static boolean layerUsesMoE(*Config cfg, int32 layer) {
        if cfg.NumExperts <= 0 {
        return false;
    }
        var for _, l = range cfg.MLPOnlyLayers {
        if l == layer {
        return false;
    }
    }
        if cfg.DecoderSparseStep <= 1 {
        return true;
    }
        return (layer+1)%cfg.DecoderSparseStep == 0;
    }

    public static void NewModel() {
        var configData, err = root.Manifest.ReadConfig("config.json");
        if err != null {
        return null, fmt.Errorf("load config: %w", err);
    }
        var cfg, err = parseConfig(configData);
        if err != null {
        return null, err;
    }
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
        var tokConfig = &tokenizer.TokenizerConfig{ConfigJSON: configData}
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
        Layers: make([]*Layer, cfg.NumHiddenLayers),;
        Config: &cfg,;
        tok:    tok,;
    }
        var for i = int32(0); i < cfg.NumHiddenLayers; i++ {
        m.Layers[i] = &Layer{IsLinear: layerIsLinear(&cfg, i)}
    }
        return m, null;
    }

    public static void tensorAny(map[String]*mlx.Array tensors) {
        var for _, k = range keys {
        var if v = tensors[k]; v != null {
        return v, k;
    }
    }
        return null, "";
    }

    public static void tensorByBase(map[String]*mlx.Array tensors) {
        return tensorAny(tensors, base+".weight", base);
    }

    public static boolean supportsGatherQMM(String mode, int bits) {
        switch mode {
        case "affine":;
        return bits == 4 || bits == 8;
        case "mxfp8":;
        return bits == 8;
        case "nvfp4", "mxfp4":;
        return bits == 4;
        default:;
        return false;
    }
    }

    public static void freeTensorKeys(map[String]*mlx.Array tensors, ...String keys) {
        var for _, k = range keys {
        if k == "" {
        continue;
    }
        var if t = tensors[k]; t != null {
        delete(tensors, k);
    }
    }
    }
        func stackAndClone(parts []*mlx.Array) *mlx.Array {
        if len(parts) == 0 {
        return null;
    }
        var stacked = mlx.Stack(parts, 0);
        var cloned = stacked.Clone();
        mlx.Eval(cloned);
        return cloned;
    }
        func transposeExpertWeightForGatherMM(w *mlx.Array) *mlx.Array {
        if w == null || !w.Valid() || w.NumDims() != 3 {
        return w;
    }
        var t = mlx.Transpose(w, 0, 2, 1);
        var cloned = t.Clone();
        mlx.Eval(cloned);
        return cloned;
    }

    public static String describeMoEProjection(String prefix, *stackedExpertWeights w) {
        if w == null {
        return prefix + "=missing";
    }
        if w.Scales != null {
        return fmt.Sprintf("%s=qmm(mode=%s,bits=%d,gs=%d)", prefix, w.Mode, w.Bits, w.GroupSize);
    }
        if w.Bits > 0 || w.Mode != "" {
        var reason = "dequantized";
        if !supportsGatherQMM(w.Mode, w.Bits) {
        reason = "unsupported_gather_qmm";
    }
        return fmt.Sprintf("%s=%s(mode=%s,bits=%d,gs=%d)", prefix, reason, w.Mode, w.Bits, w.GroupSize);
    }
        return prefix + "=fp";
    }

    public static String summarizeMoEFallbackReason(*stackedExpertWeights downW) {
        var for _, w = range []*stackedExpertWeights{gateW, upW, downW} {
        if w == null {
        return "missing_projection";
    }
        if w.Scales != null {
        continue;
    }
        if w.Bits > 0 || w.Mode != "" {
        if !supportsGatherQMM(w.Mode, w.Bits) {
        return fmt.Sprintf("unsupported_gather_qmm(mode=%s,bits=%d)", w.Mode, w.Bits);
    }
        return "dequantized_quant_weights";
    }
    }
        return "unquantized_weights";
    }
        func sliceStackedExpertAxis1(a *mlx.Array, start, stop int32) *mlx.Array {
        if a == null || !a.Valid() {
        return null;
    }
        var dims = a.Dims();
        if len(dims) < 2 {
        return null;
    }
        var beg = make([]int32, len(dims));
        var end = make([]int32, len(dims));
        var for i, d = range dims {
        end[i] = int32(d);
    }
        beg[1] = start;
        end[1] = stop;
        return mlx.SliceStartStop(a, beg, end);
    }
        func loadStackedProjection(tensors map[String]*mlx.Array, cfg *Config, useQuantized boolean, bases ...String) *stackedExpertWeights {
        var for _, base = range bases {
        var w, key = tensorByBase(tensors, base);
        if w == null {
        continue;
    }
        var scales = tensors[key+"_scale"];
        if scales == null {
        return &stackedExpertWeights{Weight: w}
    }
        var qbiases = tensors[key+"_qbias"];
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        key,;
        w,;
        scales,;
        );
        if useQuantized && supportsGatherQMM(mode, bits) {
        return &stackedExpertWeights{
        Weight:    w,;
        Scales:    scales,;
        Biases:    qbiases,;
        Bits:      bits,;
        GroupSize: groupSize,;
        Mode:      mode,;
    }
    }
        return &stackedExpertWeights{
        Weight:    mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode),;
        Bits:      bits,;
        GroupSize: groupSize,;
        Mode:      mode,;
    }
    }
        return null;
    }
        func collectPerExpertProjection(tensors map[String]*mlx.Array, cfg *Config, useQuantized boolean, layerPrefix, proj String, numExperts int32) *stackedExpertWeights {
        var weights = make([]*mlx.Array, 0, numExperts);
        var scales = make([]*mlx.Array, 0, numExperts);
        var biases = make([]*mlx.Array, 0, numExperts);
        var consumedKeys = make([]String, 0, numExperts*3);
        var bits = 0;
        var groupSize = 0;
        var mode = cfg.QuantMode;
        var for e = int32(0); e < numExperts; e++ {
        var base = fmt.Sprintf("%s.mlp.experts.%d.%s", layerPrefix, e, proj);
        var w, key = tensorByBase(tensors, base);
        if w == null {
        continue;
    }
        consumedKeys = append(consumedKeys, key);
        var s = tensors[key+"_scale"];
        if s == null {
        weights = append(weights, w);
        continue;
    }
        consumedKeys = append(consumedKeys, key+"_scale");
        var qb = tensors[key+"_qbias"];
        if qb != null {
        consumedKeys = append(consumedKeys, key+"_qbias");
    }
        var gs, b, m = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        key,;
        w,;
        s,;
        );
        if bits == 0 {
        bits = b;
        groupSize = gs;
        mode = m;
    }
        if useQuantized && supportsGatherQMM(m, b) {
        weights = append(weights, w);
        scales = append(scales, s);
        if qb != null {
        biases = append(biases, qb);
    }
        } else {
        weights = append(weights, mlx.Dequantize(w, s, qb, gs, b, m));
    }
    }
        if len(weights) == 0 {
        return null;
    }
        var out = &stackedExpertWeights{Weight: stackAndClone(weights), Bits: bits, GroupSize: groupSize, Mode: mode}
        if len(scales) == len(weights) {
        out.Scales = stackAndClone(scales);
    }
        if len(biases) == len(weights) {
        out.Biases = stackAndClone(biases);
    }
        freeTensorKeys(tensors, consumedKeys...);
        return out;
    }

    public static void splitGateUpProjection(map[String]*mlx.Array tensors, *Config cfg, boolean useQuantized, *stackedExpertWeights down) {
        var gateUp, key = tensorAny(;
        tensors,;
        layerPrefix+".mlp.experts.gate_up_proj.weight",;
        layerPrefix+".mlp.experts.gate_up_proj",;
        );
        if gateUp == null {
        return null, null, null;
    }
        var if scales = tensors[key+"_scale"]; scales != null {
        var qbiases = tensors[key+"_qbias"];
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        key,;
        gateUp,;
        scales,;
        );
        if useQuantized && supportsGatherQMM(mode, bits) {
        gate = &stackedExpertWeights{
        Bits:      bits,;
        GroupSize: groupSize,;
        Mode:      mode,;
    }
        up = &stackedExpertWeights{
        Bits:      bits,;
        GroupSize: groupSize,;
        Mode:      mode,;
    }
        if gateUp.NumDims() != 3 {
        return null, null, null;
    }
        var shape = gateUp.Dims();
        var nExperts, twoHidden, inHidden = int32(shape[0]), int32(shape[1]), int32(shape[2]);
        _ = nExperts;
        _ = inHidden;
        var mid = twoHidden / 2;
        gate.Weight = sliceStackedExpertAxis1(gateUp, 0, mid);
        up.Weight = sliceStackedExpertAxis1(gateUp, mid, twoHidden);
        gate.Scales = sliceStackedExpertAxis1(scales, 0, mid);
        up.Scales = sliceStackedExpertAxis1(scales, mid, twoHidden);
        if qbiases != null {
        gate.Biases = sliceStackedExpertAxis1(qbiases, 0, mid);
        up.Biases = sliceStackedExpertAxis1(qbiases, mid, twoHidden);
    }
        } else {
        gateUp = mlx.Dequantize(gateUp, scales, qbiases, groupSize, bits, mode);
        gate = &stackedExpertWeights{Bits: bits, GroupSize: groupSize, Mode: mode}
        up = &stackedExpertWeights{Bits: bits, GroupSize: groupSize, Mode: mode}
    }
    }
        if gateUp.NumDims() != 3 {
        return null, null, null;
    }
        var shape = gateUp.Dims();
        var nExperts, twoHidden, inHidden = int32(shape[0]), int32(shape[1]), int32(shape[2]);
        var mid = twoHidden / 2;
        if gate == null {
        gate = &stackedExpertWeights{}
    }
        if up == null {
        up = &stackedExpertWeights{}
    }
        if gate.Weight == null {
        gate.Weight = mlx.SliceStartStop(gateUp, []int32{0, 0, 0}, []int32{nExperts, mid, inHidden});
    }
        if up.Weight == null {
        up.Weight = mlx.SliceStartStop(gateUp, []int32{0, mid, 0}, []int32{nExperts, twoHidden, inHidden});
    }
        var downW, downKey = tensorAny(;
        tensors,;
        layerPrefix+".mlp.experts.down_proj.weight",;
        layerPrefix+".mlp.experts.down_proj",;
        );
        if downW == null {
        return gate, up, null;
    }
        var if scales = tensors[downKey+"_scale"]; scales != null {
        var qbiases = tensors[downKey+"_qbias"];
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize,;
        cfg.QuantBits,;
        cfg.QuantMode,;
        cfg.TensorQuant,;
        downKey,;
        downW,;
        scales,;
        );
        if useQuantized && supportsGatherQMM(mode, bits) {
        down = &stackedExpertWeights{
        Weight:    downW,;
        Scales:    scales,;
        Biases:    qbiases,;
        Bits:      bits,;
        GroupSize: groupSize,;
        Mode:      mode,;
    }
        return gate, up, down;
    }
        downW = mlx.Dequantize(downW, scales, qbiases, groupSize, bits, mode);
        down = &stackedExpertWeights{Bits: bits, GroupSize: groupSize, Mode: mode}
    }
        if down == null {
        down = &stackedExpertWeights{}
    }
        down.Weight = downW;
        return gate, up, down;
    }
        func sanitizeConvWeight(w *mlx.Array) *mlx.Array {
        if w == null {
        return null;
    }
        if w.NumDims() == 3 {
        if w.Dim(1) == 1 {
        return mlx.Squeeze(w, 1);
    }
        if w.Dim(2) == 1 {
        return mlx.Squeeze(w, 2);
    }
    }
        return w;
    }
        func depthwiseConv1dKernelWeight(w *mlx.Array) *mlx.Array {
        if w == null {
        return null;
    }
        switch w.NumDims() {
        case 2:;
        return mlx.ExpandDims(w, 2);
        case 3:;
        switch {
        case w.Dim(2) == 1:;
        return w;
        case w.Dim(1) == 1:;
        return mlx.Transpose(w, 0, 2, 1);
        case w.Dim(0) == 1:;
        return mlx.Transpose(w, 2, 1, 0);
    }
    }
        return null;
    }

    public static boolean shouldShiftNormKey(String key) {
        var for _, suffix = range []String{
        ".input_layernorm.weight",;
        ".post_attention_layernorm.weight",;
        "model.norm.weight",;
        ".self_attn.q_norm.weight",;
        ".self_attn.k_norm.weight",;
        } {
        if strings.HasSuffix(key, suffix) {
        return true;
    }
    }
        return false;
    }
        func maybeShiftNormWeight(key String, w *mlx.Array, shouldShift boolean) *mlx.Array {
        if !shouldShift || w == null || w.NumDims() != 1 || !shouldShiftNormKey(key) {
        return w;
    }
        return mlx.AddScalar(w, 1.0);
    }
        func (m *Model) LoadWeights(tensors map[String]*mlx.Array) error {
        var layout = resolveTensorPathLayout(tensors);
        m.weightPrefix = layout.containerPrefix;
        var prefix = m.weightPrefix;
        var modelPrefix = layout.containerPrefix + layout.modelPrefix;
        var cfg = m.Config;
        var linears = model.NewLinearFactory(tensors, cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant);
        var shouldShiftNormWeights = false;
        var mtpKeys = make([]String, 0);
        var for name, t = range tensors {
        if strings.Contains(name, "mtp.") {
        shouldShiftNormWeights = true;
        mtpKeys = append(mtpKeys, name);
        continue;
    }
        if !shouldShiftNormWeights && strings.Contains(name, ".linear_attn.conv1d.weight") && t != null && t.NumDims() == 3 && t.Dim(2) != 1 {
        shouldShiftNormWeights = true;
    }
    }
        if len(mtpKeys) > 0 {
        freeTensorKeys(tensors, mtpKeys...);
    }
        var embedTokens = model.MakeEmbeddingLayer(tensors, modelPrefix+"embed_tokens", cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant);
        if embedTokens == null {
        return fmt.Errorf("missing embedding weight: %sembed_tokens.weight", modelPrefix);
    }
        m.EmbedTokens = embedTokens;
        var normKey = modelPrefix + "norm.weight";
        var normWeight = maybeShiftNormWeight(normKey, tensors[normKey], shouldShiftNormWeights);
        if normWeight == null {
        return fmt.Errorf("missing final norm weight: %snorm.weight", modelPrefix);
    }
        m.Norm = nn.NewRMSNorm(normWeight, cfg.RMSNormEps);
        if cfg.TieWordEmbeddings {
        m.LMHead = m.EmbedTokens.AsLinear();
        var } else if lmHead = linears.Make(prefix + "lm_head"); lmHead != null {
        m.LMHead = lmHead;
        var } else if lmHead = linears.Make("lm_head"); lmHead != null {
        m.LMHead = lmHead;
        } else {
        m.LMHead = m.EmbedTokens.AsLinear();
    }
        var useQuantizedExperts = supportsGatherQMM(cfg.QuantMode, cfg.QuantBits);
        if !useQuantizedExperts && cfg.TensorQuant != null {
        var for _, tq = range cfg.TensorQuant {
        if tq == null {
        continue;
    }
        var _, bits, mode = model.QuantizationParams(tq.QuantType);
        if supportsGatherQMM(mode, bits) {
        useQuantizedExperts = true;
        break;
    }
    }
    }
        var moeLoadSummaries = make([]String, 0);
        var for i = int32(0); i < cfg.NumHiddenLayers; i++ {
        var layerPrefix = fmt.Sprintf("%slayers.%d", modelPrefix, i);
        var layer = &Layer{IsLinear: layerIsLinear(cfg, i)}
        var if w = maybeShiftNormWeight(layerPrefix+".input_layernorm.weight", tensors[layerPrefix+".input_layernorm.weight"], shouldShiftNormWeights); w != null {
        layer.InputNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        var if w = maybeShiftNormWeight(layerPrefix+".post_attention_layernorm.weight", tensors[layerPrefix+".post_attention_layernorm.weight"], shouldShiftNormWeights); w != null {
        layer.PostAttentionNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        if layer.InputNorm == null || layer.PostAttentionNorm == null {
        return fmt.Errorf("layer %d: missing layer norms", i);
    }
        if layer.IsLinear {
        var lin = &GatedDeltaNet{}
        lin.InProjQKV = linears.Make(layerPrefix + ".linear_attn.in_proj_qkv");
        lin.InProjZ = linears.Make(layerPrefix + ".linear_attn.in_proj_z");
        lin.InProjB = linears.Make(layerPrefix + ".linear_attn.in_proj_b");
        lin.InProjA = linears.Make(layerPrefix + ".linear_attn.in_proj_a");
        lin.InProjQKVZ = linears.Make(layerPrefix + ".linear_attn.in_proj_qkvz");
        lin.InProjBA = linears.Make(layerPrefix + ".linear_attn.in_proj_ba");
        lin.OutProj = linears.Make(layerPrefix + ".linear_attn.out_proj");
        lin.ConvWeight = sanitizeConvWeight(tensors[layerPrefix+".linear_attn.conv1d.weight"]);
        if lin.ConvWeight == null {
        lin.ConvWeight = sanitizeConvWeight(tensors[layerPrefix+".linear_attn.conv1d"]);
    }
        lin.NormWeight, _ = tensorAny(tensors,;
        layerPrefix+".linear_attn.norm.weight",;
        layerPrefix+".linear_attn.norm",;
        );
        lin.DtBias, _ = tensorAny(tensors,;
        layerPrefix+".linear_attn.dt_bias",;
        layerPrefix+".linear_attn.dt_proj",;
        );
        lin.ALog, _ = tensorAny(tensors,;
        layerPrefix+".linear_attn.A_log",;
        layerPrefix+".linear_attn.a_log",;
        );
        if lin.ALog != null {
        lin.AExp = mlx.Exp(lin.ALog.AsType(mlx.DTypeFloat32));
    }
        var hasSplit = lin.InProjQKV != null && lin.InProjZ != null && lin.InProjB != null && lin.InProjA != null;
        var hasCombined = lin.InProjQKVZ != null && lin.InProjBA != null;
        if (!hasSplit && !hasCombined) || lin.OutProj == null {
        return fmt.Errorf("layer %d: missing linear attention projections", i);
    }
        if lin.ConvWeight == null || lin.NormWeight == null || lin.DtBias == null || lin.ALog == null || lin.AExp == null {
        return fmt.Errorf("layer %d: missing linear attention state tensors", i);
    }
        if lin.ConvWeight.NumDims() != 2 {
        return fmt.Errorf("layer %d: conv1d weight must be 2D after sanitization, got %dD", i, lin.ConvWeight.NumDims());
    }
        var if convKernel = depthwiseConv1dKernelWeight(lin.ConvWeight); convKernel != null {
        lin.Conv1D = nn.NewConv1d(convKernel, null, 1, 0, 1, int32(lin.ConvWeight.Dim(0)));
    }
        layer.Linear = lin;
        } else {
        var attn = &FullAttention{}
        attn.QProj = linears.Make(layerPrefix + ".self_attn.q_proj");
        attn.KProj = linears.Make(layerPrefix + ".self_attn.k_proj");
        attn.VProj = linears.Make(layerPrefix + ".self_attn.v_proj");
        attn.OProj = linears.Make(layerPrefix + ".self_attn.o_proj");
        var if w = maybeShiftNormWeight(layerPrefix+".self_attn.q_norm.weight", tensors[layerPrefix+".self_attn.q_norm.weight"], shouldShiftNormWeights); w != null {
        attn.QNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        var if w = maybeShiftNormWeight(layerPrefix+".self_attn.k_norm.weight", tensors[layerPrefix+".self_attn.k_norm.weight"], shouldShiftNormWeights); w != null {
        attn.KNorm = nn.NewRMSNorm(w, cfg.RMSNormEps);
    }
        if attn.QProj == null || attn.KProj == null || attn.VProj == null || attn.OProj == null {
        return fmt.Errorf("layer %d: missing full attention projections", i);
    }
        if attn.QNorm == null || attn.KNorm == null {
        return fmt.Errorf("layer %d: missing full attention q/k norms", i);
    }
        layer.FullAttn = attn;
    }
        if layerUsesMoE(cfg, i) {
        var moe = &SparseMoE{}
        moe.Gate = linears.Make(layerPrefix + ".mlp.gate");
        if moe.Gate == null {
        return fmt.Errorf("layer %d: missing moe gate", i);
    }
        var gateW = loadStackedProjection(tensors, cfg, useQuantizedExperts,;
        layerPrefix+".mlp.switch_mlp.gate_proj",;
        layerPrefix+".mlp.experts.gate_proj",;
        );
        var upW = loadStackedProjection(tensors, cfg, useQuantizedExperts,;
        layerPrefix+".mlp.switch_mlp.up_proj",;
        layerPrefix+".mlp.experts.up_proj",;
        );
        var downW = loadStackedProjection(tensors, cfg, useQuantizedExperts,;
        layerPrefix+".mlp.switch_mlp.down_proj",;
        layerPrefix+".mlp.experts.down_proj",;
        );
        if gateW == null || upW == null || downW == null {
        var g2, u2, d2 = splitGateUpProjection(tensors, cfg, useQuantizedExperts, layerPrefix);
        if gateW == null {
        gateW = g2;
    }
        if upW == null {
        upW = u2;
    }
        if downW == null {
        downW = d2;
    }
    }
        if gateW == null || upW == null || downW == null {
        gateW = collectPerExpertProjection(tensors, cfg, useQuantizedExperts, layerPrefix, "gate_proj", cfg.NumExperts);
        upW = collectPerExpertProjection(tensors, cfg, useQuantizedExperts, layerPrefix, "up_proj", cfg.NumExperts);
        downW = collectPerExpertProjection(tensors, cfg, useQuantizedExperts, layerPrefix, "down_proj", cfg.NumExperts);
    }
        if gateW == null || upW == null || downW == null {
        return fmt.Errorf("layer %d: missing switch expert weights", i);
    }
        var switchMLP = &SwitchMLP{}
        if gateW.Scales != null && upW.Scales != null && downW.Scales != null {
        switchMLP.UseQuantized = true;
        switchMLP.GateWeightQ = gateW.Weight;
        switchMLP.GateScales = gateW.Scales;
        switchMLP.GateBiases = gateW.Biases;
        switchMLP.GateBits = gateW.Bits;
        switchMLP.GateGroupSize = gateW.GroupSize;
        switchMLP.UpWeightQ = upW.Weight;
        switchMLP.UpScales = upW.Scales;
        switchMLP.UpBiases = upW.Biases;
        switchMLP.UpBits = upW.Bits;
        switchMLP.UpGroupSize = upW.GroupSize;
        switchMLP.DownWeightQ = downW.Weight;
        switchMLP.DownScales = downW.Scales;
        switchMLP.DownBiases = downW.Biases;
        switchMLP.DownBits = downW.Bits;
        switchMLP.DownGroupSize = downW.GroupSize;
        } else {
        switchMLP.GateWeight = transposeExpertWeightForGatherMM(gateW.Weight);
        switchMLP.UpWeight = transposeExpertWeightForGatherMM(upW.Weight);
        switchMLP.DownWeight = transposeExpertWeightForGatherMM(downW.Weight);
        moeLoadSummaries = append(moeLoadSummaries,;
        fmt.Sprintf(;
        "layer=%d moe_fallback reason=%s %s %s %s",;
        i,;
        summarizeMoEFallbackReason(gateW, upW, downW),;
        describeMoEProjection("gate", gateW),;
        describeMoEProjection("up", upW),;
        describeMoEProjection("down", downW),;
        ),;
        );
    }
        if switchMLP.UseQuantized {
        moeLoadSummaries = append(moeLoadSummaries,;
        fmt.Sprintf(;
        "layer=%d moe_quantized %s %s %s",;
        i,;
        describeMoEProjection("gate", gateW),;
        describeMoEProjection("up", upW),;
        describeMoEProjection("down", downW),;
        ),;
        );
    }
        moe.SwitchMLP = switchMLP;
        var sharedGateProj = linears.Make(layerPrefix + ".mlp.shared_expert.gate_proj");
        var sharedUpProj = linears.Make(layerPrefix + ".mlp.shared_expert.up_proj");
        var sharedDownProj = linears.Make(layerPrefix + ".mlp.shared_expert.down_proj");
        if sharedGateProj != null && sharedUpProj != null && sharedDownProj != null {
        moe.SharedExpert = &DenseMLP{
        GateProj: sharedGateProj,;
        UpProj:   sharedUpProj,;
        DownProj: sharedDownProj,;
    }
        moe.SharedExpertGate = linears.Make(layerPrefix + ".mlp.shared_expert_gate");
    }
        layer.MLP = moe;
        } else {
        var mlp = &DenseMLP{
        GateProj: linears.Make(layerPrefix + ".mlp.gate_proj"),;
        UpProj:   linears.Make(layerPrefix + ".mlp.up_proj"),;
        DownProj: linears.Make(layerPrefix + ".mlp.down_proj"),;
    }
        if mlp.GateProj == null || mlp.UpProj == null || mlp.DownProj == null {
        return fmt.Errorf("layer %d: missing dense mlp projections", i);
    }
        layer.MLP = mlp;
    }
        m.Layers[i] = layer;
    }
        return null;
    }
        func softplus(x *mlx.Array) *mlx.Array {
        return mlx.Logaddexp(x, mlx.Zeros(x.DType(), x.Dims()...));
    }
        func depthwiseCausalConv1d(x, w *mlx.Array, outLen int32) *mlx.Array {
        if x == null || w == null {
        return null;
    }
        if w.NumDims() != 2 {
        return null;
    }
        var B = int32(x.Dim(0));
        var C = int32(w.Dim(0));
        var K = int32(w.Dim(1));
        var out *mlx.Array;
        var for i = int32(0); i < K; i++ {
        var seg = mlx.SliceStartStop(x, []int32{0, i, 0}, []int32{B, i + outLen, C});
        var wi = mlx.SliceStartStop(w, []int32{0, i}, []int32{C, i + 1});
        wi = mlx.Reshape(wi, 1, 1, C);
        var term = mlx.Mul(seg, wi);
        if out == null {
        out = term;
        } else {
        out = mlx.Add(out, term);
    }
    }
        return out;
    }

    public static void splitQKVZBA(*mlx.Array mixedBA, *Config cfg, *mlx.Array a) {
        var nk = cfg.LinearNumKeyHeads;
        var nv = cfg.LinearNumValueHeads;
        var dk = cfg.LinearKeyHeadDim;
        var dv = cfg.LinearValueHeadDim;
        var vPerK = nv / nk;
        mixedQKVZ = mlx.Reshape(mixedQKVZ, B, L, nk, 2*dk+2*vPerK*dv);
        q = mlx.SliceStartStop(mixedQKVZ, []int32{0, 0, 0, 0}, []int32{B, L, nk, dk});
        k = mlx.SliceStartStop(mixedQKVZ, []int32{0, 0, 0, dk}, []int32{B, L, nk, 2 * dk});
        v = mlx.SliceStartStop(mixedQKVZ, []int32{0, 0, 0, 2 * dk}, []int32{B, L, nk, 2*dk + vPerK*dv});
        z = mlx.SliceStartStop(mixedQKVZ, []int32{0, 0, 0, 2*dk + vPerK*dv}, []int32{B, L, nk, 2*dk + 2*vPerK*dv});
        v = mlx.Reshape(v, B, L, nv, dv);
        z = mlx.Reshape(z, B, L, nv, dv);
        mixedBA = mlx.Reshape(mixedBA, B, L, nk, 2*vPerK);
        b = mlx.SliceStartStop(mixedBA, []int32{0, 0, 0, 0}, []int32{B, L, nk, vPerK});
        a = mlx.SliceStartStop(mixedBA, []int32{0, 0, 0, vPerK}, []int32{B, L, nk, 2 * vPerK});
        b = mlx.Reshape(b, B, L, nv);
        a = mlx.Reshape(a, B, L, nv);
        return q, k, v, z, b, a;
    }
        func (a *FullAttention) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var qg = a.QProj.Forward(x);
        qg = mlx.Reshape(qg, B, L, cfg.NumAttentionHeads, cfg.HeadDim*2);
        var q = mlx.SliceStartStop(qg, []int32{0, 0, 0, 0}, []int32{B, L, cfg.NumAttentionHeads, cfg.HeadDim});
        var gate = mlx.SliceStartStop(qg, []int32{0, 0, 0, cfg.HeadDim}, []int32{B, L, cfg.NumAttentionHeads, cfg.HeadDim * 2});
        gate = mlx.Reshape(gate, B, L, cfg.NumAttentionHeads*cfg.HeadDim);
        var k = a.KProj.Forward(x);
        var v = a.VProj.Forward(x);
        k = mlx.Reshape(k, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        v = mlx.Reshape(v, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        q = a.QNorm.Forward(q, cfg.RMSNormEps);
        k = a.KNorm.Forward(k, cfg.RMSNormEps);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        var offset = 0;
        if c != null {
        offset = c.Offset();
    }
        q = mlx.RoPEWithBase(q, int(cfg.RopeDim), false, cfg.RopeTheta, 1.0, offset);
        k = mlx.RoPEWithBase(k, int(cfg.RopeDim), false, cfg.RopeTheta, 1.0, offset);
        if c != null {
        k, v = c.Update(k, v);
    }
        var out = mlx.ScaledDotProductAttentionCausal(q, k, v, cfg.Scale, L > 1);
        out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*cfg.HeadDim);
        var gateSigmoid = mlx.Sigmoid(gate);
        out = mlx.Mul(out, gateSigmoid);
        out = a.OProj.Forward(out);
        return out;
    }
        func (g *GatedDeltaNet) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var qkv, z, b, a *mlx.Array;
        var useSplitProj = g.InProjQKV != null && g.InProjZ != null && g.InProjB != null && g.InProjA != null;
        if useSplitProj {
        qkv = g.InProjQKV.Forward(x);
        z = g.InProjZ.Forward(x);
        z = mlx.Reshape(z, B, L, cfg.LinearNumValueHeads, cfg.LinearValueHeadDim);
        b = g.InProjB.Forward(x);
        a = g.InProjA.Forward(x);
        } else {
        var mixedQKVZ = g.InProjQKVZ.Forward(x);
        var mixedBA = g.InProjBA.Forward(x);
        var q, k, v *mlx.Array;
        q, k, v, z, b, a = splitQKVZBA(mixedQKVZ, mixedBA, cfg, B, L);
        qkv = mlx.Concatenate([]*mlx.Array{
        mlx.Reshape(q, B, L, cfg.LinearNumKeyHeads*cfg.LinearKeyHeadDim),;
        mlx.Reshape(k, B, L, cfg.LinearNumKeyHeads*cfg.LinearKeyHeadDim),;
        mlx.Reshape(v, B, L, cfg.LinearNumValueHeads*cfg.LinearValueHeadDim),;
        }, -1);
    }
        var convTail = cfg.LinearConvKernelDim - 1;
        var convState *mlx.Array;
        var rc *cache.RecurrentCache;
        if c != null {
        var if typed, ok = c.(*cache.RecurrentCache); ok {
        rc = typed;
        convState = rc.ConvState(int(B), x.DType());
    }
    }
        if convState == null {
        convState = mlx.Zeros(x.DType(), int(B), int(convTail), int(2*cfg.LinearNumKeyHeads*cfg.LinearKeyHeadDim+cfg.LinearNumValueHeads*cfg.LinearValueHeadDim));
    }
        var convInput = mlx.Concatenate([]*mlx.Array{convState, qkv}, 1);
        var convOut *mlx.Array;
        if g.Conv1D != null {
        convOut = g.Conv1D.Forward(convInput);
        } else {
        convOut = depthwiseCausalConv1d(convInput, g.ConvWeight, L);
    }
        convOut = mlx.SiLU(convOut);
        if rc != null {
        var total = int32(convInput.Dim(1));
        var start = total - convTail;
        var nextConv = mlx.SliceStartStop(convInput, []int32{0, start, 0}, []int32{B, total, int32(convInput.Dim(2))});
        rc.SetConvState(nextConv);
    }
        var keyDim = cfg.LinearNumKeyHeads * cfg.LinearKeyHeadDim;
        var valueDim = cfg.LinearNumValueHeads * cfg.LinearValueHeadDim;
        var q = mlx.SliceStartStop(convOut, []int32{0, 0, 0}, []int32{B, L, keyDim});
        var k = mlx.SliceStartStop(convOut, []int32{0, 0, keyDim}, []int32{B, L, 2 * keyDim});
        var v = mlx.SliceStartStop(convOut, []int32{0, 0, 2 * keyDim}, []int32{B, L, 2*keyDim + valueDim});
        q = mlx.Reshape(q, B, L, cfg.LinearNumKeyHeads, cfg.LinearKeyHeadDim);
        k = mlx.Reshape(k, B, L, cfg.LinearNumKeyHeads, cfg.LinearKeyHeadDim);
        v = mlx.Reshape(v, B, L, cfg.LinearNumValueHeads, cfg.LinearValueHeadDim);
        var invScale = float32(1.0 / math.Sqrt(double(cfg.LinearKeyHeadDim)));
        q = mlx.MulScalar(mlx.RMSNormFn(q, null, 1e-6), invScale*invScale);
        k = mlx.MulScalar(mlx.RMSNormFn(k, null, 1e-6), invScale);
        var gDecay = softplus(mlx.Add(a, g.DtBias));
        gDecay = mlx.Mul(gDecay, g.AExp);
        gDecay = mlx.Exp(mlx.MulScalar(gDecay, -1));
        gDecay = gDecay.AsType(a.DType());
        var beta = mlx.Sigmoid(b);
        var state *mlx.Array;
        if rc != null {
        state = rc.DeltaState(int(B), x.DType());
    }
        if state == null {
        state = mlx.Zeros(x.DType(), int(B), int(cfg.LinearNumValueHeads), int(cfg.LinearValueHeadDim), int(cfg.LinearKeyHeadDim));
    }
        var out, state = mlx.GatedDelta(q, k, v, gDecay, beta, state);
        var outDType = out.DType();
        out = mlx.RMSNormFn(out, g.NormWeight, cfg.RMSNormEps);
        out = mlx.Mul(out.AsType(mlx.DTypeFloat32), mlx.SiLU(z.AsType(mlx.DTypeFloat32))).AsType(outDType);
        out = mlx.Reshape(out, B, L, valueDim);
        out = g.OutProj.Forward(out);
        if rc != null {
        rc.SetDeltaState(state);
        rc.Advance(int(L));
    }
        return out;
    }
        func (m *DenseMLP) Forward(x *mlx.Array, _ *Config) *mlx.Array {
        return m.DownProj.Forward(mlx.SwiGLU(m.GateProj.Forward(x), m.UpProj.Forward(x)));
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
        gate = mlx.GatherMM(xFlat, s.GateWeight, null, idxFlat, doSort);
        up = mlx.GatherMM(xFlat, s.UpWeight, null, idxFlat, doSort);
        hidden = mlx.SwiGLU(gate, up);
        down = mlx.GatherMM(hidden, s.DownWeight, null, idxFlat, doSort);
    }
        if doSort {
        down = mlx.Reshape(mlx.Take(mlx.Squeeze(mlx.Squeeze(down, 2), 1), invOrder, 0), B*L, topK, cfg.HiddenSize);
        } else {
        down = mlx.Squeeze(down, 2);
    }
        return mlx.Reshape(down, B, L, topK, cfg.HiddenSize);
    }
        func (m *SparseMoE) Forward(x *mlx.Array, cfg *Config) *mlx.Array {
        var dims = x.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var probs = mlx.SoftmaxAxis(m.Gate.Forward(x), -1, true);
        var neg = mlx.Neg(probs);
        var inds = mlx.Argpartition(neg, int(cfg.NumExpertsPerTok)-1, -1);
        var shape = inds.Dims();
        inds = mlx.SliceStartStop(inds, []int32{0, 0, 0}, []int32{int32(shape[0]), int32(shape[1]), cfg.NumExpertsPerTok});
        var scores = mlx.TakeAlongAxis(probs, inds, -1);
        if cfg.NormTopKProb && cfg.NumExpertsPerTok > 1 {
        var sumScores = mlx.Sum(scores, -1, true);
        scores = mlx.Div(scores, sumScores);
    }
        var expertOut = m.SwitchMLP.Forward(x, inds, cfg);
        var y = mlx.Sum(mlx.Mul(expertOut, mlx.ExpandDims(scores, -1)), 2, false);
        if m.SharedExpert != null {
        var shared = m.SharedExpert.Forward(x, cfg);
        if m.SharedExpertGate != null {
        shared = mlx.Mul(shared, mlx.Sigmoid(m.SharedExpertGate.Forward(x)));
    }
        y = mlx.Add(y, shared);
    }
        return mlx.Reshape(y, B, L, cfg.HiddenSize);
    }
        func (l *Layer) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var r *mlx.Array;
        var normed = l.InputNorm.Forward(x, cfg.RMSNormEps);
        if l.IsLinear {
        r = l.Linear.Forward(normed, c, B, L, cfg);
        } else {
        r = l.FullAttn.Forward(normed, c, B, L, cfg);
    }
        var h = mlx.Add(x, r);
        r = l.MLP.Forward(l.PostAttentionNorm.Forward(h, cfg.RMSNormEps), cfg);
        return mlx.Add(h, r);
    }
        func (m *Model) Forward(tokens *mlx.Array, caches []cache.Cache) *mlx.Array {
        var dims = tokens.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var h = m.EmbedTokens.Forward(tokens);
        var for i, layer = range m.Layers {
        var c cache.Cache;
        if caches != null && i < len(caches) {
        c = caches[i];
    }
        h = layer.Forward(h, c, B, L, m.Config);
    }
        var out = m.Norm.Forward(h, m.RMSNormEps);
        return out;
    }
        func (m *Model) Unembed(x *mlx.Array) *mlx.Array {
        return m.LMHead.Forward(x);
    }
        func (m *Model) NumLayers() int {
        return len(m.Layers);
    }
        func (m *Model) MaxContextLength() int {
        return int(m.MaxPositionEmbeddings);
    }
        func (m *Model) Tokenizer() *tokenizer.Tokenizer {
        return m.tok;
    }
        func (m *Model) NewCaches() []cache.Cache {
        var caches = make([]cache.Cache, len(m.Layers));
        var convTail = m.LinearConvKernelDim - 1;
        var convDim = 2*m.LinearNumKeyHeads*m.LinearKeyHeadDim + m.LinearNumValueHeads*m.LinearValueHeadDim;
        var for i, layer = range m.Layers {
        if layer.IsLinear {
        caches[i] = cache.NewRecurrentCache(convTail, convDim, m.LinearNumValueHeads, m.LinearValueHeadDim, m.LinearKeyHeadDim);
        } else {
        caches[i] = cache.NewKVCache();
    }
    }
        return caches;
    }
}
