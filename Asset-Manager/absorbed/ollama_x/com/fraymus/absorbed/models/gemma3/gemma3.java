package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class gemma3 {
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
        base.Register("Gemma3ForCausalLM", newModel);
        base.Register("Gemma3ForConditionalGeneration", newModel);
    }

    public static class TextConfig {
        public int32 HiddenSize;
        public int32 NumHiddenLayers;
        public int32 IntermediateSize;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 HeadDim;
        public int32 VocabSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public float32 RopeLocalBaseFreq;
        public int32 MaxPositionEmbeddings;
        public int32 SlidingWindow;
        public int32 SlidingWindowPattern;
        public []String LayerTypes;
        public boolean TieWordEmbeddings;
        public int QuantGroupSize;
        public int QuantBits;
        public String QuantMode;
        public map[String]*model.TensorQuantInfo TensorQuant;
        public float32 Scale;
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

    public static class DecoderLayer {
        public *nn.RMSNorm InputNorm;
        public *Attention Attention;
        public *nn.RMSNorm PostAttnNorm;
        public *nn.RMSNorm PreFFNorm;
        public *MLP MLP;
        public *nn.RMSNorm PostFFNorm;
        public *mlx.Array InputNormScaled;
        public *mlx.Array PostAttnNormScaled;
        public *mlx.Array PreFFNormScaled;
        public *mlx.Array PostFFNormScaled;
        public boolean IsSliding;
        public int32 LayerIdx;
    }

    public static class Model {
        public nn.EmbeddingLayer EmbedTokens;
        public []*DecoderLayer Layers;
        public *nn.RMSNorm Norm;
        public nn.LinearLayer LMHead;
        public *mlx.Array NormScaled;
        public *tokenizer.Tokenizer tok;
        public String weightPrefix;
    }

    public static void defaultHeads(int32 numKVHeads) {
        switch numLayers {
        case 34:;
        return 8, 4;
        case 48:;
        return 16, 8;
        case 62:;
        return 32, 16;
        default:;
        return 8, 4;
    }
    }

    public static void parseTextConfig() {
        var cfg TextConfig;
        var if err = json.Unmarshal(configData, &cfg); err != null {
        return TextConfig{}, false, fmt.Errorf("parse config: %w", err);
    }
        var wrapped struct {
        TextConfig *TextConfig `json:"text_config"`;
    }
        var if err = json.Unmarshal(configData, &wrapped); err != null {
        return TextConfig{}, false, fmt.Errorf("parse nested text config: %w", err);
    }
        var fromConditional = wrapped.TextConfig != null;
        if fromConditional {
        cfg = *wrapped.TextConfig;
        if cfg.HeadDim == 0 {
        cfg.HeadDim = 256;
    }
        if cfg.NumAttentionHeads == 0 {
        cfg.NumAttentionHeads, cfg.NumKeyValueHeads = defaultHeads(cfg.NumHiddenLayers);
    }
        if cfg.NumKeyValueHeads == 0 {
        _, cfg.NumKeyValueHeads = defaultHeads(cfg.NumHiddenLayers);
    }
        if cfg.VocabSize == 0 {
        cfg.VocabSize = 262208;
    }
        if cfg.SlidingWindowPattern == 0 && len(cfg.LayerTypes) == 0 {
        cfg.SlidingWindowPattern = 6;
    }
        if cfg.MaxPositionEmbeddings == 0 {
        cfg.MaxPositionEmbeddings = 131072;
    }
    }
        if cfg.HeadDim == 0 {
        cfg.HeadDim = 256;
    }
        if cfg.NumAttentionHeads == 0 {
        cfg.NumAttentionHeads, cfg.NumKeyValueHeads = defaultHeads(cfg.NumHiddenLayers);
    }
        if cfg.NumKeyValueHeads == 0 {
        cfg.NumKeyValueHeads = max(1, cfg.NumAttentionHeads/2);
    }
        if cfg.RopeTheta == 0 {
        cfg.RopeTheta = 1000000;
    }
        if cfg.RopeLocalBaseFreq == 0 {
        cfg.RopeLocalBaseFreq = 10000;
    }
        if cfg.RMSNormEps == 0 {
        cfg.RMSNormEps = 1e-6;
    }
        if cfg.VocabSize == 0 {
        cfg.VocabSize = 262208;
    }
        cfg.Scale = float32(1.0 / math.Sqrt(double(cfg.HeadDim)));
        return cfg, fromConditional, null;
    }

    public static String resolveWeightPrefix(map[String]*mlx.Array tensors) {
        var for _, prefix = range []String{"", "language_model."} {
        if tensors[prefix+"model.embed_tokens.weight"] != null {
        return prefix;
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
        m.NormScaled = mlx.AddScalar(m.Norm.Weight, 1.0);
    }
        var scaled []*mlx.Array;
        if m.NormScaled != null {
        scaled = append(scaled, m.NormScaled);
    }
        var for _, layer = range m.Layers {
        if layer == null || layer.Attention == null {
        continue;
    }
        if layer.InputNorm != null {
        layer.InputNormScaled = mlx.AddScalar(layer.InputNorm.Weight, 1.0);
        scaled = append(scaled, layer.InputNormScaled);
    }
        if layer.PostAttnNorm != null {
        layer.PostAttnNormScaled = mlx.AddScalar(layer.PostAttnNorm.Weight, 1.0);
        scaled = append(scaled, layer.PostAttnNormScaled);
    }
        if layer.PreFFNorm != null {
        layer.PreFFNormScaled = mlx.AddScalar(layer.PreFFNorm.Weight, 1.0);
        scaled = append(scaled, layer.PreFFNormScaled);
    }
        if layer.PostFFNorm != null {
        layer.PostFFNormScaled = mlx.AddScalar(layer.PostFFNorm.Weight, 1.0);
        scaled = append(scaled, layer.PostFFNormScaled);
    }
        if layer.Attention.QNorm != null {
        layer.Attention.QNormScaled = mlx.AddScalar(layer.Attention.QNorm.Weight, 1.0);
        scaled = append(scaled, layer.Attention.QNormScaled);
    }
        if layer.Attention.KNorm != null {
        layer.Attention.KNormScaled = mlx.AddScalar(layer.Attention.KNorm.Weight, 1.0);
        scaled = append(scaled, layer.Attention.KNormScaled);
    }
    }
        if len(scaled) > 0 {
        mlx.Eval(scaled...);
    }
    }

    public static void newModel() {
        var configData, err = root.Manifest.ReadConfig("config.json");
        if err != null {
        return null, fmt.Errorf("load config: %w", err);
    }
        var cfg, _, err = parseTextConfig(configData);
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
        m.Layers[i] = &DecoderLayer{
        LayerIdx:  int32(i),;
        IsSliding: isLayerSliding(int32(i), m.TextConfig),;
    }
    }
        return m, null;
    }
        func (m *Model) LoadWeights(tensors map[String]*mlx.Array) error {
        m.weightPrefix = resolveWeightPrefix(tensors);
        var prefix = m.weightPrefix;
        var linears = model.NewLinearFactory(tensors, m.QuantGroupSize, m.QuantBits, m.QuantMode, m.TensorQuant);
        var embedTokens = model.MakeEmbeddingLayer(tensors, prefix+"model.embed_tokens", m.QuantGroupSize, m.QuantBits, m.QuantMode, m.TensorQuant);
        if embedTokens == null {
        return fmt.Errorf("missing embedding weight: %smodel.embed_tokens.weight", prefix);
    }
        m.EmbedTokens = embedTokens;
        var normWeight = tensors[prefix+"model.norm.weight"];
        if normWeight == null {
        return fmt.Errorf("missing final norm weight: %smodel.norm.weight", prefix);
    }
        m.Norm = nn.NewRMSNorm(normWeight, m.RMSNormEps);
        var if lmHead = linears.Make(prefix + "lm_head"); lmHead != null {
        m.LMHead = lmHead;
        var } else if lmHead = linears.Make("lm_head"); lmHead != null {
        m.LMHead = lmHead;
        } else {
        m.LMHead = m.EmbedTokens.AsLinear();
    }
        var for i = int32(0); i < m.NumHiddenLayers; i++ {
        var layerPrefix = fmt.Sprintf("%smodel.layers.%d", prefix, i);
        var layer = &DecoderLayer{
        LayerIdx:  i,;
        IsSliding: isLayerSliding(i, m.TextConfig),;
        Attention: &Attention{},;
        MLP:       &MLP{},;
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
        if layer.Attention.QProj == null || layer.Attention.KProj == null || layer.Attention.VProj == null || layer.Attention.OProj == null {
        return fmt.Errorf("layer %d: missing attention projections", i);
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
        h = mlx.MulScalar(h, float32(math.Sqrt(double(m.HiddenSize))));
        var for i, layer = range m.Layers {
        var c cache.Cache;
        if caches != null && i < len(caches) {
        c = caches[i];
    }
        h = layer.Forward(h, c, B, L, m.TextConfig);
    }
        return mlx.RMSNormFn(h, m.NormScaled, m.RMSNormEps);
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
        var for i, layer = range m.Layers {
        if m.SlidingWindow > 0 && layer.IsSliding {
        caches[i] = cache.NewRotatingKVCache(int(m.SlidingWindow));
        } else {
        caches[i] = cache.NewKVCache();
    }
    }
        return caches;
    }
        func (m *Model) FormatPrompt(prompt String) String {
        return fmt.Sprintf("<start_of_turn>user\n%s<end_of_turn>\n<start_of_turn>model\n", prompt);
    }
        func (l *DecoderLayer) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *TextConfig) *mlx.Array {
        var normed = mlx.RMSNormFn(x, l.InputNormScaled, cfg.RMSNormEps);
        var attnOut = l.Attention.Forward(normed, c, B, L, l.IsSliding, cfg);
        attnOut = mlx.RMSNormFn(attnOut, l.PostAttnNormScaled, cfg.RMSNormEps);
        var h = mlx.Add(x, attnOut);
        normed = mlx.RMSNormFn(h, l.PreFFNormScaled, cfg.RMSNormEps);
        var mlpOut = l.MLP.Forward(normed);
        mlpOut = mlx.RMSNormFn(mlpOut, l.PostFFNormScaled, cfg.RMSNormEps);
        return mlx.Add(h, mlpOut);
    }
        func (a *Attention) Forward(x *mlx.Array, c cache.Cache, B, L int32, isSliding boolean, cfg *TextConfig) *mlx.Array {
        var q = a.QProj.Forward(x);
        var k = a.KProj.Forward(x);
        var v = a.VProj.Forward(x);
        q = mlx.Reshape(q, B, L, cfg.NumAttentionHeads, cfg.HeadDim);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Reshape(k, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Reshape(v, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        q = mlx.RMSNormFn(q, a.QNormScaled, cfg.RMSNormEps);
        k = mlx.RMSNormFn(k, a.KNormScaled, cfg.RMSNormEps);
        var ropeTheta = cfg.RopeTheta;
        if isSliding {
        ropeTheta = cfg.RopeLocalBaseFreq;
    }
        var offset = 0;
        if c != null {
        offset = c.Offset();
    }
        q = mlx.RoPEWithBase(q, int(cfg.HeadDim), false, ropeTheta, 1.0, offset);
        k = mlx.RoPEWithBase(k, int(cfg.HeadDim), false, ropeTheta, 1.0, offset);
        if c != null {
        k, v = c.Update(k, v);
    }
        var out = mlx.ScaledDotProductAttentionCausal(q, k, v, cfg.Scale, L > 1);
        out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*cfg.HeadDim);
        return a.OProj.Forward(out);
    }
        func (m *MLP) Forward(x *mlx.Array) *mlx.Array {
        var gate = mlx.GELUApprox(m.GateProj.Forward(x));
        var up = m.UpProj.Forward(x);
        return m.DownProj.Forward(mlx.Mul(gate, up));
    }
}
