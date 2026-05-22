package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class gemma4 {
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
        base.Register("Gemma4ForCausalLM", newModel);
        base.Register("Gemma4ForConditionalGeneration", newModel);
    }
        var _ base.Model = (*Model)(null);

    public static class RopeParams {
        public float32 PartialRotaryFactor;
        public float32 RopeTheta;
        public String RopeType;
    }

    public static class TextConfig {
        public int32 HiddenSize;
        public int32 NumHiddenLayers;
        public int32 IntermediateSize;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 HeadDim;
        public int32 GlobalHeadDim;
        public int32 VocabSize;
        public float32 RMSNormEps;
        public int32 MaxPositionEmbeddings;
        public int32 SlidingWindow;
        public int32 SlidingWindowPattern;
        public []String LayerTypes;
        public boolean TieWordEmbeddings;
        public float32 FinalLogitSoftcapping;
        public boolean UseDoubleWideMLP;
        public int32 NumKVSharedLayers;
        public int32 HiddenSizePerLayer;
        public int32 VocabSizePerLayer;
        public boolean AttentionKEqV;
        public int32 NumGlobalKeyValueHeads;
        public boolean EnableMoeBlock;
        public int32 NumExperts;
        public int32 TopKExperts;
        public int32 ExpertIntermediateSize;
        public map[String]*RopeParams RopeParameters;
        public int32 ImageTokenIDValue;
        public int QuantGroupSize;
        public int QuantBits;
        public String QuantMode;
        public map[String]*model.TensorQuantInfo TensorQuant;
        public float32 SlidingScale;
        public float32 FullScale;
        public int SlidingRopeDims;
        public int FullRopeDims;
        public float32 SlidingRopeBase;
        public float32 FullRopeBase;
        public *mlx.Array FullRopeFreqs;
        public float32 EmbedScale;
        public float32 PLEScale;
        public float32 PLEProjScale;
        public float32 PLECombineScale;
        public float32 RouterScale;
        public map[int32]int32 KVShareMap;
        public map[int32]boolean KVDonors;
    }

    public static class sharedKVEntry {
        public V K,;
        public int Offset;
    }

    public static class Attention {
        public nn.LinearLayer QProj;
        public nn.LinearLayer KProj;
        public nn.LinearLayer VProj;
        public nn.LinearLayer OProj;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
        public *mlx.Array QNormScaled;
        public *mlx.Array KNormScaled;
    }

    public static class MLP {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }

    public static class stackedExpertResult {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array Biases;
        public int Bits;
        public int GroupSize;
        public String Mode;
    }
        func firstNonNil(tensors map[String]*mlx.Array, keys ...String) *mlx.Array {
        var for _, k = range keys {
        var if t = tensors[k]; t != null {
        return t;
    }
    }
        return null;
    }
        func buildCausalMaskWindow(Q, K, window int32) *mlx.Array {
        var offset = K - Q // cache offset: kv positions before the current query chunk;
        var vals = make([]float32, Q*K);
        var negInf = float32(math.Inf(-1));
        var for q = range Q {
        var absQ = offset + q;
        var lo int32;
        if window > 0 {
        lo = max(absQ-window+1, 0);
    }
        var for kv = range K {
        if kv > absQ || kv < lo {
        vals[q*K+kv] = negInf;
    }
    }
    }
        return mlx.FromValues(vals, 1, 1, int(Q), int(K));
    }
        func sliceAxis1(a *mlx.Array, start, stop int32) *mlx.Array {
        var dims = a.Dims();
        var beg = make([]int32, len(dims));
        var end = make([]int32, len(dims));
        var for i, d = range dims {
        end[i] = int32(d);
    }
        beg[1] = start;
        end[1] = stop;
        return mlx.SliceStartStop(a, beg, end);
    }
        func transposeForGatherMM(w *mlx.Array) *mlx.Array {
        if w == null || !w.Valid() || w.NumDims() != 3 {
        return w;
    }
        var t = mlx.Transpose(w, 0, 2, 1).Clone();
        mlx.Eval(t);
        return t;
    }
        func collectExpertProjection(tensors map[String]*mlx.Array, cfg *TextConfig, prefix, proj String, numExperts int32) *stackedExpertResult {
        var weights = make([]*mlx.Array, 0, numExperts);
        var scales = make([]*mlx.Array, 0, numExperts);
        var biases = make([]*mlx.Array, 0, numExperts);
        var bits, groupSize = 0, 0;
        var mode = cfg.QuantMode;
        var for e = range numExperts {
        var base = fmt.Sprintf("%s.%d.%s", prefix, e, proj);
        var w = tensors[base+".weight"];
        var key = base + ".weight";
        if w == null {
        w = tensors[base];
        key = base;
    }
        if w == null {
        return null;
    }
        var s = tensors[key+"_scale"];
        if s == null {
        weights = append(weights, w);
        continue;
    }
        var qb = tensors[key+"_qbias"];
        var gs, b, m = model.ResolveLinearQuantParams(;
        cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode,;
        cfg.TensorQuant, key, w, s,;
        );
        if bits == 0 {
        bits = b;
        groupSize = gs;
        mode = m;
    }
        weights = append(weights, w);
        scales = append(scales, s);
        if qb != null {
        biases = append(biases, qb);
    }
    }
        if len(weights) == 0 {
        return null;
    }
        var stacked = mlx.Stack(weights, 0).Clone();
        mlx.Eval(stacked);
        var out = &stackedExpertResult{Weight: stacked, Bits: bits, GroupSize: groupSize, Mode: mode}
        if len(scales) == len(weights) {
        out.Scales = mlx.Stack(scales, 0).Clone();
        mlx.Eval(out.Scales);
    }
        if len(biases) == len(weights) {
        out.Biases = mlx.Stack(biases, 0).Clone();
        mlx.Eval(out.Biases);
    }
        return out;
    }

    public static class Router {
        public nn.LinearLayer Proj;
        public *mlx.Array Scale;
    }

    public static class MoEBlock {
        public *mlx.Array GateUpWeight;
        public *mlx.Array GateWeight;
        public *mlx.Array UpWeight;
        public *mlx.Array DownWeight;
        public GateUpScales, GateUpWeightQ,;
        public GateScales, GateWeightQ,;
        public UpScales, UpWeightQ,;
        public DownScales, DownWeightQ,;
        public *mlx.Array PerExpertScale;
        public boolean UseQuantized;
        public boolean UseFusedGateUp;
        public GateUpBits GateUpGroupSize,;
        public UpGroupSize GateGroupSize,;
        public int DownGroupSize;
        public UpBits, GateBits,;
        public String QuantMode;
        public String DownQuantMode;
    }

    public static class PLELayer {
        public nn.LinearLayer InputGate;
        public nn.LinearLayer Projection;
        public *nn.RMSNorm PostNorm;
        public *mlx.Array PostNormScaled;
    }

    public static class DecoderLayer {
        public *nn.RMSNorm InputNorm;
        public *Attention Attention;
        public *nn.RMSNorm PostAttnNorm;
        public *nn.RMSNorm PreFFNorm;
        public *MLP MLP;
        public *nn.RMSNorm PostFFNorm;
        public *PLELayer PLE;
        public *Router Router;
        public *MoEBlock MoE;
        public *nn.RMSNorm PostFFNorm1;
        public *nn.RMSNorm PostFFNorm2;
        public *nn.RMSNorm PreFFNorm2;
        public *mlx.Array InputNormScaled;
        public *mlx.Array PostAttnNormScaled;
        public *mlx.Array PreFFNormScaled;
        public *mlx.Array PostFFNormScaled;
        public *mlx.Array PostFFNorm1Scaled;
        public *mlx.Array PostFFNorm2Scaled;
        public *mlx.Array PreFFNorm2Scaled;
        public *mlx.Array LayerScalar;
        public boolean IsSliding;
        public int32 LayerIdx;
        public int32 KVShareDonor;
        public boolean IsDonor;
    }

    public static class Model {
        public nn.EmbeddingLayer EmbedTokens;
        public []*DecoderLayer Layers;
        public *nn.RMSNorm Norm;
        public nn.LinearLayer LMHead;
        public nn.EmbeddingLayer EmbedTokensPerLayer;
        public nn.LinearLayer PerLayerModelProj;
        public *nn.RMSNorm PerLayerProjNorm;
        public *mlx.Array NormScaled;
        public *mlx.Array PerLayerProjNormWeight;
        public *tokenizer.Tokenizer tok;
        public String weightPrefix;
    }

    public static void parseTextConfig() {
        var cfg TextConfig;
        var if err = json.Unmarshal(configData, &cfg); err != null {
        return TextConfig{}, fmt.Errorf("parse config: %w", err);
    }
        var wrapped struct {
        TextConfig *TextConfig `json:"text_config"`;
    }
        var if err = json.Unmarshal(configData, &wrapped); err != null {
        return TextConfig{}, fmt.Errorf("parse nested text config: %w", err);
    }
        if wrapped.TextConfig != null {
        cfg = *wrapped.TextConfig;
    }
        if cfg.HeadDim == 0 {
        cfg.HeadDim = 256;
    }
        if cfg.GlobalHeadDim == 0 {
        cfg.GlobalHeadDim = cfg.HeadDim;
    }
        if cfg.NumAttentionHeads == 0 {
        cfg.NumAttentionHeads = 8;
    }
        if cfg.NumKeyValueHeads == 0 {
        cfg.NumKeyValueHeads = 1;
    }
        if cfg.RMSNormEps == 0 {
        cfg.RMSNormEps = 1e-6;
    }
        if cfg.VocabSize == 0 {
        cfg.VocabSize = 262144;
    }
        if cfg.SlidingWindowPattern <= 0 && len(cfg.LayerTypes) == 0 {
        cfg.SlidingWindowPattern = 5;
    }
        if cfg.MaxPositionEmbeddings == 0 {
        cfg.MaxPositionEmbeddings = 131072;
    }
        cfg.SlidingScale = 1.0;
        cfg.FullScale = 1.0;
        cfg.SlidingRopeDims = int(cfg.HeadDim) // full rotation for sliding;
        cfg.SlidingRopeBase = 10000;
        cfg.FullRopeDims = int(cfg.HeadDim) // default: full rotation;
        cfg.FullRopeBase = 1000000;
        var if rp = cfg.RopeParameters; rp != null {
        var if sp = rp["sliding_attention"]; sp != null && sp.RopeTheta > 0 {
        cfg.SlidingRopeBase = sp.RopeTheta;
    }
        var if fp = rp["full_attention"]; fp != null {
        if fp.RopeTheta > 0 {
        cfg.FullRopeBase = fp.RopeTheta;
    }
        if fp.PartialRotaryFactor > 0 {
        var ghd = int(cfg.GlobalHeadDim);
        cfg.FullRopeDims = ghd;
        var halfDim = ghd / 2;
        var ropeAngles = int(fp.PartialRotaryFactor * float32(ghd) / 2);
        var freqs = make([]float32, halfDim);
        var for i = range ropeAngles {
        freqs[i] = float32(math.Pow(double(cfg.FullRopeBase), double(2*i)/double(ghd)));
    }
        var for i = ropeAngles; i < halfDim; i++ {
        freqs[i] = 1e10;
    }
        cfg.FullRopeFreqs = mlx.FromValues(freqs, halfDim);
        mlx.Eval(cfg.FullRopeFreqs);
    }
    }
    }
        cfg.EmbedScale = float32(math.Sqrt(double(cfg.HiddenSize)));
        if cfg.HiddenSizePerLayer > 0 {
        cfg.PLEScale = float32(math.Sqrt(double(cfg.HiddenSizePerLayer)));
        cfg.PLEProjScale = float32(1.0 / math.Sqrt(double(cfg.HiddenSize)));
        cfg.PLECombineScale = float32(math.Pow(2.0, -0.5));
    }
        cfg.RouterScale = float32(1.0 / math.Sqrt(double(cfg.HiddenSize)));
        cfg.KVShareMap = make(map[int32]int32);
        cfg.KVDonors = make(map[int32]boolean);
        if cfg.NumKVSharedLayers > 0 && len(cfg.LayerTypes) > 0 {
        var firstShared = cfg.NumHiddenLayers - cfg.NumKVSharedLayers;
        var prevLayers = cfg.LayerTypes[:firstShared];
        var for i = firstShared; i < cfg.NumHiddenLayers; i++ {
        var layerType = cfg.LayerTypes[i];
        var donor = int32(-1);
        var for j = len(prevLayers) - 1; j >= 0; j-- {
        if prevLayers[j] == layerType {
        donor = int32(j);
        break;
    }
    }
        if donor >= 0 {
        cfg.KVShareMap[i] = donor;
        cfg.KVDonors[donor] = true;
    }
    }
    }
        return cfg, null;
    }
        func (m *Model) EnableCompile() boolean {
        return true;
    }

    public static String resolveWeightPrefix(map[String]*mlx.Array tensors) {
        var for _, prefix = range []String{"", "language_model.", "model.language_model."} {
        if tensors[prefix+"embed_tokens.weight"] != null {
        return prefix;
    }
    }
        var for _, prefix = range []String{"", "language_model.", "model.language_model."} {
        if tensors[prefix+"model.embed_tokens.weight"] != null {
        return prefix + "model.";
    }
    }
        return "";
    }

    public static boolean isLayerSliding(int32 layerIdx, *TextConfig cfg) {
        if len(cfg.LayerTypes) > 0 && int(layerIdx) < len(cfg.LayerTypes) {
        return cfg.LayerTypes[layerIdx] == "sliding_attention";
    }
        if cfg.SlidingWindowPattern <= 0 {
        return false;
    }
        return (layerIdx+1)%cfg.SlidingWindowPattern != 0;
    }

    public static void precomputeGemmaScaledWeights(*Model m) {
        if m.Norm != null {
        m.NormScaled = m.Norm.Weight;
    }
        if m.PerLayerProjNorm != null {
        m.PerLayerProjNormWeight = m.PerLayerProjNorm.Weight;
    }
        var for _, layer = range m.Layers {
        if layer == null || layer.Attention == null {
        continue;
    }
        if layer.InputNorm != null {
        layer.InputNormScaled = layer.InputNorm.Weight;
    }
        if layer.PostAttnNorm != null {
        layer.PostAttnNormScaled = layer.PostAttnNorm.Weight;
    }
        if layer.PreFFNorm != null {
        layer.PreFFNormScaled = layer.PreFFNorm.Weight;
    }
        if layer.PostFFNorm != null {
        layer.PostFFNormScaled = layer.PostFFNorm.Weight;
    }
        if layer.Attention.QNorm != null {
        layer.Attention.QNormScaled = layer.Attention.QNorm.Weight;
    }
        if layer.Attention.KNorm != null {
        layer.Attention.KNormScaled = layer.Attention.KNorm.Weight;
    }
        if layer.PLE != null && layer.PLE.PostNorm != null {
        layer.PLE.PostNormScaled = layer.PLE.PostNorm.Weight;
    }
        if layer.PostFFNorm1 != null {
        layer.PostFFNorm1Scaled = layer.PostFFNorm1.Weight;
    }
        if layer.PostFFNorm2 != null {
        layer.PostFFNorm2Scaled = layer.PostFFNorm2.Weight;
    }
        if layer.PreFFNorm2 != null {
        layer.PreFFNorm2Scaled = layer.PreFFNorm2.Weight;
    }
    }
    }

    public static void newModel() {
        var configData, err = root.Manifest.ReadConfig("config.json");
        if err != null {
        return null, fmt.Errorf("load config: %w", err);
    }
        var cfg, err = parseTextConfig(configData);
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
        Layers:     make([]*DecoderLayer, cfg.NumHiddenLayers),;
        TextConfig: &cfg,;
        tok:        tok,;
    }
        var for i = range m.Layers {
        var donor, isShared = cfg.KVShareMap[int32(i)];
        if !isShared {
        donor = -1;
    }
        m.Layers[i] = &DecoderLayer{
        LayerIdx:     int32(i),;
        IsSliding:    isLayerSliding(int32(i), m.TextConfig),;
        KVShareDonor: donor,;
        IsDonor:      cfg.KVDonors[int32(i)],;
    }
    }
        return m, null;
    }
        func (m *Model) LoadWeights(tensors map[String]*mlx.Array) error {
        m.weightPrefix = resolveWeightPrefix(tensors);
        var prefix = m.weightPrefix;
        var linears = model.NewLinearFactory(tensors, m.QuantGroupSize, m.QuantBits, m.QuantMode, m.TensorQuant);
        var embedTokens = model.MakeEmbeddingLayer(tensors, prefix+"embed_tokens", m.QuantGroupSize, m.QuantBits, m.QuantMode, m.TensorQuant);
        if embedTokens == null {
        return fmt.Errorf("missing embedding weight: %sembed_tokens.weight", prefix);
    }
        m.EmbedTokens = embedTokens;
        var normWeight = tensors[prefix+"norm.weight"];
        if normWeight == null {
        return fmt.Errorf("missing final norm weight: %snorm.weight", prefix);
    }
        m.Norm = nn.NewRMSNorm(normWeight, m.RMSNormEps);
        var if lmHead = linears.Make(prefix + "lm_head"); lmHead != null {
        m.LMHead = lmHead;
        var } else if lmHead = linears.Make("lm_head"); lmHead != null {
        m.LMHead = lmHead;
        } else {
        m.LMHead = m.EmbedTokens.AsLinear();
    }
        if m.HiddenSizePerLayer > 0 {
        var pleEmbed = model.MakeEmbeddingLayer(tensors, prefix+"embed_tokens_per_layer", m.QuantGroupSize, m.QuantBits, m.QuantMode, m.TensorQuant);
        if pleEmbed == null {
        return fmt.Errorf("missing PLE embedding weight");
    }
        m.EmbedTokensPerLayer = pleEmbed;
        m.PerLayerModelProj = linears.Make(prefix + "per_layer_model_projection");
        if m.PerLayerModelProj == null {
        return fmt.Errorf("missing per_layer_model_projection weight");
    }
        var pleProjNormWeight = tensors[prefix+"per_layer_projection_norm.weight"];
        if pleProjNormWeight == null {
        return fmt.Errorf("missing per_layer_projection_norm weight");
    }
        m.PerLayerProjNorm = nn.NewRMSNorm(pleProjNormWeight, m.RMSNormEps);
    }
        var for i = range m.NumHiddenLayers {
        var layerPrefix = fmt.Sprintf("%slayers.%d", prefix, i);
        var isSliding = isLayerSliding(i, m.TextConfig);
        var donor, isShared = m.KVShareMap[i];
        if !isShared {
        donor = -1;
    }
        var layer = &DecoderLayer{
        LayerIdx:     i,;
        IsSliding:    isSliding,;
        KVShareDonor: donor,;
        IsDonor:      m.KVDonors[i],;
        Attention:    &Attention{},;
        MLP:          &MLP{},;
    }
        var if w = tensors[layerPrefix+".input_layernorm.weight"]; w != null {
        layer.InputNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".post_attention_layernorm.weight"]; w != null {
        layer.PostAttnNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".pre_feedforward_layernorm.weight"]; w != null {
        layer.PreFFNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".post_feedforward_layernorm.weight"]; w != null {
        layer.PostFFNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        layer.Attention.QProj = linears.Make(layerPrefix + ".self_attn.q_proj");
        layer.Attention.KProj = linears.Make(layerPrefix + ".self_attn.k_proj");
        layer.Attention.VProj = linears.Make(layerPrefix + ".self_attn.v_proj");
        layer.Attention.OProj = linears.Make(layerPrefix + ".self_attn.o_proj");
        var if w = tensors[layerPrefix+".self_attn.q_norm.weight"]; w != null {
        layer.Attention.QNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".self_attn.k_norm.weight"]; w != null {
        layer.Attention.KNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        layer.MLP.GateProj = linears.Make(layerPrefix + ".mlp.gate_proj");
        layer.MLP.UpProj = linears.Make(layerPrefix + ".mlp.up_proj");
        layer.MLP.DownProj = linears.Make(layerPrefix + ".mlp.down_proj");
        var if w = tensors[layerPrefix+".layer_scalar"]; w != null {
        layer.LayerScalar = w;
    }
        if m.EnableMoeBlock {
        var routerProj = linears.Make(layerPrefix + ".router.proj");
        var routerScale = tensors[layerPrefix+".router.scale"];
        if routerScale == null {
        routerScale = tensors[layerPrefix+".router_scale"];
    }
        if routerProj == null || routerScale == null {
        return fmt.Errorf("layer %d: missing router weights", i);
    }
        layer.Router = &Router{
        Proj:  routerProj,;
        Scale: routerScale,;
    }
        var perExpertScale = tensors[layerPrefix+".router.per_expert_scale"];
        if perExpertScale == null {
        perExpertScale = tensors[layerPrefix+".moe.per_expert_scale"];
    }
        if perExpertScale == null {
        return fmt.Errorf("layer %d: missing MoE per_expert_scale", i);
    }
        var moe = &MoEBlock{PerExpertScale: perExpertScale}
        var gateUpW = tensors[layerPrefix+".experts.gate_up_proj"];
        if gateUpW == null {
        gateUpW = tensors[layerPrefix+".moe.gate_up_proj"];
    }
        var gateW = tensors[layerPrefix+".experts.gate_proj"];
        if gateW == null {
        gateW = tensors[layerPrefix+".moe.gate_proj"];
    }
        if gateUpW != null {
        var dims = gateUpW.Dims();
        var half = int32(dims[1] / 2);
        var gateSlice = sliceAxis1(gateUpW, 0, half);
        var upSlice = sliceAxis1(gateUpW, half, int32(dims[1]));
        moe.GateWeight = transposeForGatherMM(gateSlice);
        moe.UpWeight = transposeForGatherMM(upSlice);
        var downW = tensors[layerPrefix+".experts.down_proj"];
        if downW == null {
        downW = tensors[layerPrefix+".moe.down_proj"];
    }
        if downW == null {
        return fmt.Errorf("layer %d: missing MoE down_proj with fused gate_up_proj", i);
    }
        moe.DownWeight = transposeForGatherMM(downW);
        } else if gateW != null {
        moe.GateWeight = transposeForGatherMM(gateW);
        var upW = tensors[layerPrefix+".experts.up_proj"];
        if upW == null {
        upW = tensors[layerPrefix+".moe.up_proj"];
    }
        var downW = tensors[layerPrefix+".experts.down_proj"];
        if downW == null {
        downW = tensors[layerPrefix+".moe.down_proj"];
    }
        moe.UpWeight = transposeForGatherMM(upW);
        moe.DownWeight = transposeForGatherMM(downW);
        if moe.UpWeight == null || moe.DownWeight == null {
        return fmt.Errorf("layer %d: incomplete pre-stacked MoE weights", i);
    }
        var } else if switchGateUp = firstNonNil(tensors,;
        layerPrefix+".moe.switch_mlp.gate_up_proj.weight",;
        layerPrefix+".moe.switch_mlp.gate_up_proj"); switchGateUp != null {
        var switchDown = firstNonNil(tensors,;
        layerPrefix+".moe.switch_mlp.down_proj.weight",;
        layerPrefix+".moe.switch_mlp.down_proj");
        if switchDown == null {
        return fmt.Errorf("layer %d: missing switch_mlp down_proj", i);
    }
        var gateUpKey = layerPrefix + ".moe.switch_mlp.gate_up_proj.weight";
        if tensors[gateUpKey] == null {
        gateUpKey = layerPrefix + ".moe.switch_mlp.gate_up_proj";
    }
        var downKey = layerPrefix + ".moe.switch_mlp.down_proj.weight";
        if tensors[downKey] == null {
        downKey = layerPrefix + ".moe.switch_mlp.down_proj";
    }
        var gateUpScales = firstNonNil(tensors, gateUpKey+"_scale", gateUpKey+".scale");
        var downScales = firstNonNil(tensors, downKey+"_scale", downKey+".scale");
        if gateUpScales != null && downScales != null {
        var gateUpBiases = firstNonNil(tensors, gateUpKey+"_qbias", gateUpKey+".bias");
        var downBiases = firstNonNil(tensors, downKey+"_qbias", downKey+".bias");
        moe.GateUpWeightQ = switchGateUp;
        moe.GateUpScales = gateUpScales;
        moe.GateUpBiases = gateUpBiases;
        moe.DownWeightQ = switchDown;
        moe.DownScales = downScales;
        if downBiases != null {
        moe.DownBiases = downBiases;
    }
        var groupSize, bits, mode = model.ResolveLinearQuantParams(;
        m.QuantGroupSize, m.QuantBits, m.QuantMode,;
        m.TensorQuant, gateUpKey, switchGateUp, gateUpScales,;
        );
        moe.UseQuantized = true;
        moe.UseFusedGateUp = true;
        moe.GateUpGroupSize = groupSize;
        moe.GateUpBits = bits;
        moe.QuantMode = mode;
        var dGroupSize, dBits, dMode = model.ResolveLinearQuantParams(;
        m.QuantGroupSize, m.QuantBits, m.QuantMode,;
        m.TensorQuant, downKey, switchDown, downScales,;
        );
        moe.DownGroupSize = dGroupSize;
        moe.DownBits = dBits;
        moe.DownQuantMode = dMode;
        } else {
        moe.GateUpWeight = transposeForGatherMM(switchGateUp);
        moe.UseFusedGateUp = true;
        moe.DownWeight = transposeForGatherMM(switchDown);
    }
        } else {
        var gateStacked = collectExpertProjection(tensors, m.TextConfig,;
        layerPrefix+".moe.experts", "gate_proj", m.NumExperts);
        var upStacked = collectExpertProjection(tensors, m.TextConfig,;
        layerPrefix+".moe.experts", "up_proj", m.NumExperts);
        var downStacked = collectExpertProjection(tensors, m.TextConfig,;
        layerPrefix+".moe.experts", "down_proj", m.NumExperts);
        if gateStacked == null && upStacked == null {
        var gateUpStacked = collectExpertProjection(tensors, m.TextConfig,;
        layerPrefix+".moe.experts", "gate_up_proj", m.NumExperts);
        if gateUpStacked != null {
        var dims = gateUpStacked.Weight.Dims();
        if len(dims) >= 2 {
        var mid = int32(dims[1] / 2);
        gateStacked = &stackedExpertResult{
        Weight:    sliceAxis1(gateUpStacked.Weight, 0, mid),;
        Bits:      gateUpStacked.Bits,;
        GroupSize: gateUpStacked.GroupSize,;
        Mode:      gateUpStacked.Mode,;
    }
        upStacked = &stackedExpertResult{
        Weight:    sliceAxis1(gateUpStacked.Weight, mid, int32(dims[1])),;
        Bits:      gateUpStacked.Bits,;
        GroupSize: gateUpStacked.GroupSize,;
        Mode:      gateUpStacked.Mode,;
    }
        if gateUpStacked.Scales != null {
        var sDims = gateUpStacked.Scales.Dims();
        var sMid = int32(sDims[1] / 2);
        gateStacked.Scales = sliceAxis1(gateUpStacked.Scales, 0, sMid);
        upStacked.Scales = sliceAxis1(gateUpStacked.Scales, sMid, int32(sDims[1]));
    }
        if gateUpStacked.Biases != null {
        var bDims = gateUpStacked.Biases.Dims();
        var bMid = int32(bDims[1] / 2);
        gateStacked.Biases = sliceAxis1(gateUpStacked.Biases, 0, bMid);
        upStacked.Biases = sliceAxis1(gateUpStacked.Biases, bMid, int32(bDims[1]));
    }
    }
    }
    }
        if gateStacked == null || upStacked == null || downStacked == null {
        return fmt.Errorf("layer %d: missing MoE expert weights", i);
    }
        if gateStacked.Scales != null && upStacked.Scales != null && downStacked.Scales != null {
        moe.GateWeightQ = gateStacked.Weight;
        moe.GateScales = gateStacked.Scales;
        moe.GateBiases = gateStacked.Biases;
        moe.UpWeightQ = upStacked.Weight;
        moe.UpScales = upStacked.Scales;
        moe.UpBiases = upStacked.Biases;
        moe.DownWeightQ = downStacked.Weight;
        moe.DownScales = downStacked.Scales;
        moe.DownBiases = downStacked.Biases;
        moe.UseQuantized = true;
        moe.GateGroupSize = gateStacked.GroupSize;
        moe.GateBits = gateStacked.Bits;
        moe.UpGroupSize = upStacked.GroupSize;
        moe.UpBits = upStacked.Bits;
        moe.DownGroupSize = downStacked.GroupSize;
        moe.DownBits = downStacked.Bits;
        moe.QuantMode = gateStacked.Mode;
        moe.DownQuantMode = downStacked.Mode;
        } else {
        moe.GateWeight = transposeForGatherMM(gateStacked.Weight);
        moe.UpWeight = transposeForGatherMM(upStacked.Weight);
        moe.DownWeight = transposeForGatherMM(downStacked.Weight);
    }
    }
        layer.MoE = moe;
        var if w = tensors[layerPrefix+".post_feedforward_layernorm_1.weight"]; w != null {
        layer.PostFFNorm1 = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".post_feedforward_layernorm_2.weight"]; w != null {
        layer.PostFFNorm2 = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".pre_feedforward_layernorm_2.weight"]; w != null {
        layer.PreFFNorm2 = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        if layer.PostFFNorm1 == null || layer.PostFFNorm2 == null || layer.PreFFNorm2 == null {
        return fmt.Errorf("layer %d: missing MoE norm weights", i);
    }
    }
        if m.HiddenSizePerLayer > 0 {
        layer.PLE = &PLELayer{}
        layer.PLE.InputGate = linears.Make(layerPrefix + ".per_layer_input_gate");
        layer.PLE.Projection = linears.Make(layerPrefix + ".per_layer_projection");
        var if w = tensors[layerPrefix+".post_per_layer_input_norm.weight"]; w != null {
        layer.PLE.PostNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        if layer.PLE.InputGate == null || layer.PLE.Projection == null || layer.PLE.PostNorm == null {
        return fmt.Errorf("layer %d: missing PLE weights", i);
    }
    }
        if layer.InputNorm == null {
        return fmt.Errorf("layer %d: missing input_layernorm", i);
    }
        if layer.PostAttnNorm == null {
        return fmt.Errorf("layer %d: missing post_attention_layernorm", i);
    }
        if layer.PreFFNorm == null {
        return fmt.Errorf("layer %d: missing pre_feedforward_layernorm", i);
    }
        if layer.PostFFNorm == null {
        return fmt.Errorf("layer %d: missing post_feedforward_layernorm", i);
    }
        if layer.Attention.QProj == null || layer.Attention.OProj == null {
        return fmt.Errorf("layer %d: missing attention q/o projections", i);
    }
        if layer.Attention.KProj == null {
        return fmt.Errorf("layer %d: missing attention k projection", i);
    }
        var useAltAttn = m.AttentionKEqV && !isSliding;
        if layer.Attention.VProj == null && !useAltAttn {
        return fmt.Errorf("layer %d: missing attention v projection", i);
    }
        if layer.Attention.QNorm == null || layer.Attention.KNorm == null {
        return fmt.Errorf("layer %d: missing attention q/k norms", i);
    }
        if layer.MLP.GateProj == null || layer.MLP.UpProj == null || layer.MLP.DownProj == null {
        return fmt.Errorf("layer %d: missing mlp projections", i);
    }
        m.Layers[i] = layer;
    }
        precomputeGemmaScaledWeights(m);
        if m.NormScaled == null {
        return fmt.Errorf("missing precomputed final norm weight");
    }
        return null;
    }
        func (m *Model) Forward(tokens *mlx.Array, caches []cache.Cache) *mlx.Array {
        var dims = tokens.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var h = m.EmbedTokens.Forward(tokens);
        h = mlx.MulScalar(h, m.EmbedScale);
        var perLayerInputs *mlx.Array;
        if m.HiddenSizePerLayer > 0 && m.EmbedTokensPerLayer != null {
        perLayerInputs = m.computePLEInputs(tokens, h);
    }
        var sharedKV map[int32]sharedKVEntry;
        if len(m.KVShareMap) > 0 {
        sharedKV = make(map[int32]sharedKVEntry);
    }
        var smc *slidingMaskCache;
        if L > 1 && m.SlidingWindow > 0 {
        smc = &slidingMaskCache{}
    }
        var for i, layer = range m.Layers {
        var c cache.Cache;
        if caches != null && i < len(caches) {
        c = caches[i];
    }
        var pleInput *mlx.Array;
        if perLayerInputs != null {
        pleInput = sliceLayerDim(perLayerInputs, int32(i), B, L, m.HiddenSizePerLayer);
    }
        var donorEntry *sharedKVEntry;
        if layer.KVShareDonor >= 0 {
        var if entry, ok = sharedKV[layer.KVShareDonor]; ok {
        donorEntry = &entry;
    }
    }
        var donorKV *sharedKVEntry;
        h, donorKV = layer.Forward(h, c, B, L, m.TextConfig, pleInput, donorEntry, smc);
        if layer.IsDonor && donorKV != null {
        sharedKV[layer.LayerIdx] = *donorKV;
    }
    }
        return mlx.RMSNormFn(h, m.NormScaled, m.RMSNormEps);
    }

    public static class slidingMaskCache {
        public *mlx.Array mask;
        public int32 L;
        public int32 kLen;
        public int32 window;
    }
        func (c *slidingMaskCache) get(L, kLen, window int32, dtype mlx.DType) *mlx.Array {
        if c == null {
        return buildCausalMaskWindow(L, kLen, window).AsType(dtype);
    }
        if c.mask != null && c.L == L && c.kLen == kLen && c.window == window {
        return c.mask;
    }
        c.mask = buildCausalMaskWindow(L, kLen, window).AsType(dtype);
        c.L = L;
        c.kLen = kLen;
        c.window = window;
        return c.mask;
    }
        func (m *Model) Unembed(x *mlx.Array) *mlx.Array {
        var logits = m.LMHead.Forward(x);
        if m.FinalLogitSoftcapping > 0 {
        var cap = mlx.FromValue(m.FinalLogitSoftcapping).AsType(logits.DType());
        logits = mlx.LogitSoftcap(logits, cap);
    }
        return logits;
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
        var cacheLayers = len(m.Layers);
        var for i, layer = range m.Layers {
        if layer.KVShareDonor >= 0 {
        cacheLayers = i;
        break;
    }
    }
        var caches = make([]cache.Cache, cacheLayers);
        var for i, layer = range m.Layers[:cacheLayers] {
        if m.SlidingWindow > 0 && layer.IsSliding {
        caches[i] = cache.NewRotatingKVCache(int(m.SlidingWindow));
        } else {
        caches[i] = cache.NewKVCache();
    }
    }
        return caches;
    }
        func (m *Model) computePLEInputs(tokens, h *mlx.Array) *mlx.Array {
        var dims = tokens.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var pleScale = m.PLEScale;
        var projScale = m.PLEProjScale;
        var pleEmb = m.EmbedTokensPerLayer.Forward(tokens);
        pleEmb = mlx.MulScalar(pleEmb, pleScale);
        pleEmb = mlx.Reshape(pleEmb, B, L, m.NumHiddenLayers, m.HiddenSizePerLayer);
        var pleProj = m.PerLayerModelProj.Forward(h);
        pleProj = mlx.MulScalar(pleProj, projScale);
        pleProj = mlx.Reshape(pleProj, B, L, m.NumHiddenLayers, m.HiddenSizePerLayer);
        pleProj = mlx.RMSNormFn(pleProj, m.PerLayerProjNormWeight, m.RMSNormEps);
        var combined = mlx.Add(pleProj, pleEmb);
        combined = mlx.MulScalar(combined, m.PLECombineScale);
        return combined;
    }
        func sliceLayerDim(combined *mlx.Array, layerIdx, B, L, pleDim int32) *mlx.Array {
        var sliced = mlx.SliceStartStop(combined,;
        []int32{0, 0, layerIdx, 0},;
        []int32{B, L, layerIdx + 1, pleDim},;
        );
        return mlx.Squeeze(sliced, 2);
    }
        func (l *DecoderLayer) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *TextConfig, pleInput *mlx.Array, donorEntry *sharedKVEntry, slidingMaskCache *slidingMaskCache) (*mlx.Array, *sharedKVEntry) {
        var normed = mlx.RMSNormFn(x, l.InputNormScaled, cfg.RMSNormEps);
        var attnOut, donorKV = l.Attention.Forward(normed, c, B, L, l.IsSliding, cfg, donorEntry, slidingMaskCache);
        attnOut = mlx.RMSNormFn(attnOut, l.PostAttnNormScaled, cfg.RMSNormEps);
        var h = mlx.Add(x, attnOut);
        if l.Router != null && l.MoE != null {
        var residual = h;
        normed = mlx.RMSNormFn(h, l.PreFFNormScaled, cfg.RMSNormEps);
        var mlpOut = l.MLP.Forward(normed);
        mlpOut = mlx.RMSNormFn(mlpOut, l.PostFFNorm1Scaled, cfg.RMSNormEps);
        var scores, inds = l.Router.Forward(h, cfg);
        var normed2 = mlx.RMSNormFn(h, l.PreFFNorm2Scaled, cfg.RMSNormEps);
        var moeOut = l.MoE.Forward(normed2, scores, inds, cfg);
        moeOut = mlx.RMSNormFn(moeOut, l.PostFFNorm2Scaled, cfg.RMSNormEps);
        var combined = mlx.Add(mlpOut, moeOut);
        combined = mlx.RMSNormFn(combined, l.PostFFNormScaled, cfg.RMSNormEps);
        h = mlx.Add(residual, combined);
        } else {
        normed = mlx.RMSNormFn(h, l.PreFFNormScaled, cfg.RMSNormEps);
        var mlpOut = l.MLP.Forward(normed);
        mlpOut = mlx.RMSNormFn(mlpOut, l.PostFFNormScaled, cfg.RMSNormEps);
        h = mlx.Add(h, mlpOut);
    }
        if l.PLE != null && pleInput != null {
        var residual = h;
        var gated = mlx.GeGLU(l.PLE.InputGate.Forward(h), pleInput);
        var projected = l.PLE.Projection.Forward(gated);
        projected = mlx.RMSNormFn(projected, l.PLE.PostNormScaled, cfg.RMSNormEps);
        h = mlx.Add(residual, projected);
    }
        if l.LayerScalar != null {
        h = mlx.Mul(h, l.LayerScalar);
    }
        return h, donorKV;
    }
        func (a *Attention) Forward(x *mlx.Array, c cache.Cache, B, L int32, isSliding boolean, cfg *TextConfig, donorEntry *sharedKVEntry, slidingMaskCache *slidingMaskCache) (*mlx.Array, *sharedKVEntry) {
        var headDim = cfg.HeadDim;
        var scale = cfg.SlidingScale;
        var ropeDims = cfg.SlidingRopeDims;
        var ropeBase = cfg.SlidingRopeBase;
        if !isSliding {
        headDim = cfg.GlobalHeadDim;
        scale = cfg.FullScale;
        ropeDims = cfg.FullRopeDims;
        ropeBase = cfg.FullRopeBase;
    }
        var q = a.QProj.Forward(x);
        q = mlx.Reshape(q, B, L, cfg.NumAttentionHeads, headDim);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        q = mlx.RMSNormFn(q, a.QNormScaled, cfg.RMSNormEps);
        var offset = 0;
        if donorEntry != null {
        offset = donorEntry.Offset - int(L);
        } else if c != null {
        offset = c.Offset();
    }
        var ropeFreqs *mlx.Array;
        if !isSliding {
        ropeFreqs = cfg.FullRopeFreqs;
    }
        q = mlx.RoPEWithFreqs(q, ropeDims, false, ropeBase, 1.0, offset, ropeFreqs);
        var k, v *mlx.Array;
        var donorKV *sharedKVEntry;
        if donorEntry != null {
        k = donorEntry.K;
        v = donorEntry.V;
        } else {
        var kvHeads = cfg.NumKeyValueHeads;
        if a.VProj == null && !isSliding && cfg.NumGlobalKeyValueHeads > 0 {
        kvHeads = cfg.NumGlobalKeyValueHeads;
    }
        k = a.KProj.Forward(x);
        k = mlx.Reshape(k, B, L, kvHeads, headDim);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        if a.VProj != null {
        v = a.VProj.Forward(x);
        v = mlx.Reshape(v, B, L, kvHeads, headDim);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        } else {
        v = k;
    }
        k = mlx.RMSNormFn(k, a.KNormScaled, cfg.RMSNormEps);
        k = mlx.RoPEWithFreqs(k, ropeDims, false, ropeBase, 1.0, offset, ropeFreqs);
        v = mlx.RMSNormFn(v, null, cfg.RMSNormEps);
        if c != null {
        k, v = c.Update(k, v);
        donorKV = &sharedKVEntry{K: k, V: v, Offset: c.Offset()}
    }
    }
        var window int32;
        if isSliding && L > 1 && cfg.SlidingWindow > 0 {
        window = cfg.SlidingWindow;
    }
        var out *mlx.Array;
        switch {
        case headDim > 128 && L > 1 && !mlx.MetalIsAvailable():;
        var kvHeads = int32(k.Dim(1));
        var nRepeats = cfg.NumAttentionHeads / kvHeads;
        var kLen = int32(k.Dim(2));
        q = mlx.MulScalar(q, scale);
        q = mlx.Reshape(q, B, kvHeads, nRepeats, L, headDim);
        k = mlx.Reshape(k, B, kvHeads, 1, kLen, headDim);
        v = mlx.Reshape(v, B, kvHeads, 1, kLen, headDim);
        var kT = mlx.Transpose(k, 0, 1, 2, 4, 3);
        var scores = mlx.Matmul(q, kT);
        var mask = buildCausalMaskWindow(L, kLen, window);
        scores = mlx.Add(scores, mask);
        scores = mlx.SoftmaxAxis(scores, -1, true);
        out = mlx.Matmul(scores, v);
        out = mlx.Reshape(out, B, cfg.NumAttentionHeads, L, headDim);
        case window > 0:;
        var kLen = int32(k.Dim(2));
        var mask = slidingMaskCache.get(L, kLen, window, q.DType());
        out = mlx.ScaledDotProductAttentionMasked(q, k, v, scale, mask);
        default:;
        out = mlx.ScaledDotProductAttentionCausal(q, k, v, scale, L > 1);
    }
        out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*headDim);
        if !mlx.MetalIsAvailable() {
        out = mlx.Contiguous(out, false);
    }
        return a.OProj.Forward(out), donorKV;
    }
        func (m *MLP) Forward(x *mlx.Array) *mlx.Array {
        var gate = m.GateProj.Forward(x);
        var up = m.UpProj.Forward(x);
        return m.DownProj.Forward(mlx.GeGLU(gate, up));
    }
        func (r *Router) Forward(x *mlx.Array, cfg *TextConfig) (*mlx.Array, *mlx.Array) {
        var dims = x.Dims();
        var BL = int32(dims[0]) * int32(dims[1]);
        var xFlat = mlx.Reshape(x, BL, cfg.HiddenSize);
        var normed = mlx.RMSNormFn(xFlat, null, cfg.RMSNormEps);
        normed = mlx.MulScalar(normed, cfg.RouterScale);
        normed = mlx.Mul(normed, r.Scale);
        var expertScores = r.Proj.Forward(normed);
        var neg = mlx.Neg(expertScores);
        var inds = mlx.Argpartition(neg, int(cfg.TopKExperts)-1, -1);
        inds = mlx.SliceStartStop(inds,;
        []int32{0, 0},;
        []int32{BL, cfg.TopKExperts},;
        );
        var scores = mlx.TakeAlongAxis(expertScores, inds, -1);
        scores = mlx.SoftmaxAxis(scores, -1, true) // [B*L, topK];
        return scores, inds;
    }
        func (m *MoEBlock) Forward(x *mlx.Array, scores, inds *mlx.Array, cfg *TextConfig) *mlx.Array {
        var dims = x.Dims();
        var B, L = int32(dims[0]), int32(dims[1]);
        var topK = cfg.TopKExperts;
        var xFlat = mlx.Reshape(x, B*L, 1, 1, cfg.HiddenSize);
        var idxFlat = mlx.Reshape(inds, B*L, topK);
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
        var hidden, down *mlx.Array;
        if m.UseQuantized {
        if m.UseFusedGateUp {
        var gateUp = mlx.GatherQMM(xFlat, m.GateUpWeightQ, m.GateUpScales, m.GateUpBiases,;
        null, idxFlat, true, m.GateUpGroupSize, m.GateUpBits, m.QuantMode, doSort);
        var guDims = gateUp.Dims();
        var mid = int32(guDims[len(guDims)-1] / 2);
        var gate = mlx.SliceStartStop(gateUp,;
        []int32{0, 0, 0, 0},;
        []int32{int32(guDims[0]), int32(guDims[1]), int32(guDims[2]), mid});
        var up = mlx.SliceStartStop(gateUp,;
        []int32{0, 0, 0, mid},;
        []int32{int32(guDims[0]), int32(guDims[1]), int32(guDims[2]), int32(guDims[len(guDims)-1])});
        hidden = mlx.GeGLU(gate, up);
        } else {
        var gate = mlx.GatherQMM(xFlat, m.GateWeightQ, m.GateScales, m.GateBiases,;
        null, idxFlat, true, m.GateGroupSize, m.GateBits, m.QuantMode, doSort);
        var up = mlx.GatherQMM(xFlat, m.UpWeightQ, m.UpScales, m.UpBiases,;
        null, idxFlat, true, m.UpGroupSize, m.UpBits, m.QuantMode, doSort);
        hidden = mlx.GeGLU(gate, up);
    }
        var downMode = m.DownQuantMode;
        if downMode == "" {
        downMode = m.QuantMode;
    }
        down = mlx.GatherQMM(hidden, m.DownWeightQ, m.DownScales, m.DownBiases,;
        null, idxFlat, true, m.DownGroupSize, m.DownBits, downMode, doSort);
        } else {
        if m.UseFusedGateUp && m.GateUpWeight != null {
        var gateUp = mlx.GatherMM(xFlat, m.GateUpWeight, null, idxFlat, doSort);
        var guDims = gateUp.Dims();
        var mid = int32(guDims[len(guDims)-1] / 2);
        var gate = mlx.SliceStartStop(gateUp,;
        []int32{0, 0, 0, 0},;
        []int32{int32(guDims[0]), int32(guDims[1]), int32(guDims[2]), mid});
        var up = mlx.SliceStartStop(gateUp,;
        []int32{0, 0, 0, mid},;
        []int32{int32(guDims[0]), int32(guDims[1]), int32(guDims[2]), int32(guDims[len(guDims)-1])});
        hidden = mlx.GeGLU(gate, up);
        } else {
        var gate = mlx.GatherMM(xFlat, m.GateWeight, null, idxFlat, doSort);
        var up = mlx.GatherMM(xFlat, m.UpWeight, null, idxFlat, doSort);
        hidden = mlx.GeGLU(gate, up);
    }
        down = mlx.GatherMM(hidden, m.DownWeight, null, idxFlat, doSort);
    }
        if doSort {
        down = mlx.Reshape(mlx.Take(mlx.Squeeze(mlx.Squeeze(down, 2), 1), invOrder, 0), B*L, topK, cfg.HiddenSize);
        } else {
        down = mlx.Squeeze(down, 2);
    }
        down = mlx.Reshape(down, B*L, topK, cfg.HiddenSize);
        var indsFlat = mlx.Reshape(inds, B*L*topK);
        var expertScales = mlx.Take(m.PerExpertScale, indsFlat, 0) // [B*L*topK];
        expertScales = mlx.Reshape(expertScales, B*L, topK)     // [B*L, topK];
        down = mlx.Mul(down, mlx.ExpandDims(expertScales, -1));
        var y = mlx.Sum(mlx.Mul(down, mlx.ExpandDims(scores, -1)), 1, false) // [B*L, hidden_size];
        return mlx.Reshape(y, B, L, cfg.HiddenSize);
    }
}
