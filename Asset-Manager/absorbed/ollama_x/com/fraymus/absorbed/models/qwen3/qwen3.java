package com.fraymus.absorbed.models.qwen3;

import java.util.*;
import java.io.*;

public class qwen3 {
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
        base.Register("Qwen3ForCausalLM", newModel);
    }

    public static class Config {
        public int32 HiddenSize;
        public int32 NumHiddenLayers;
        public int32 IntermediateSize;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 VocabSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public int32 HeadDim;
        public int32 MaxPositionEmbeddings;
        public boolean TieWordEmbeddings;
        public int QuantGroupSize;
        public int QuantBits;
        public String QuantMode;
        public map[String]*model.TensorQuantInfo TensorQuant;
        public float32 Scale;
        public float32 QKNormEps;
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
        public *Attention Attention;
        public *MLP MLP;
        public *nn.RMSNorm AttentionNorm;
        public *nn.RMSNorm MLPNorm;
    }

    public static class Attention {
        public nn.LinearLayer QProj;
        public nn.LinearLayer KProj;
        public nn.LinearLayer VProj;
        public nn.LinearLayer OProj;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
    }

    public static class MLP {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }

    public static String resolveWeightPrefix(map[String]*mlx.Array tensors) {
        var for _, prefix = range []String{"", "language_model."} {
        if tensors[prefix+"model.embed_tokens.weight"] != null {
        return prefix;
    }
    }
        return "";
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
        if cfg.HiddenSize <= 0 {
        return null, fmt.Errorf("invalid hidden_size: %d", cfg.HiddenSize);
    }
        if cfg.NumAttentionHeads <= 0 {
        return null, fmt.Errorf("invalid num_attention_heads: %d", cfg.NumAttentionHeads);
    }
        if cfg.NumKeyValueHeads <= 0 {
        cfg.NumKeyValueHeads = cfg.NumAttentionHeads;
    }
        if cfg.HeadDim == 0 {
        if cfg.HiddenSize%cfg.NumAttentionHeads != 0 {
        return null, fmt.Errorf("hidden_size (%d) must be divisible by num_attention_heads (%d)", cfg.HiddenSize, cfg.NumAttentionHeads);
    }
        cfg.HeadDim = cfg.HiddenSize / cfg.NumAttentionHeads;
    }
        if cfg.HeadDim <= 0 {
        return null, fmt.Errorf("invalid head_dim: %d", cfg.HeadDim);
    }
        if cfg.NumAttentionHeads%cfg.NumKeyValueHeads != 0 {
        return null, fmt.Errorf("num_attention_heads (%d) must be divisible by num_key_value_heads (%d)", cfg.NumAttentionHeads, cfg.NumKeyValueHeads);
    }
        if cfg.RMSNormEps == 0 {
        cfg.RMSNormEps = 1e-6;
    }
        if cfg.RopeTheta == 0 {
        cfg.RopeTheta = 1000000;
    }
        cfg.Scale = float32(1.0 / math.Sqrt(double(cfg.HeadDim)));
        cfg.QKNormEps = 1e-6;
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
        Layers: make([]*Layer, cfg.NumHiddenLayers),;
        Config: &cfg,;
        tok:    tok,;
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
        if m.TieWordEmbeddings {
        m.LMHead = m.EmbedTokens.AsLinear();
        var } else if lmHead = linears.Make(prefix + "lm_head"); lmHead != null {
        m.LMHead = lmHead;
        var } else if lmHead = linears.Make("lm_head"); lmHead != null {
        m.LMHead = lmHead;
        } else {
        m.LMHead = m.EmbedTokens.AsLinear();
    }
        var for i = int32(0); i < m.NumHiddenLayers; i++ {
        var layerPrefix = fmt.Sprintf("%smodel.layers.%d", prefix, i);
        var layer = &Layer{
        Attention: &Attention{},;
        MLP:       &MLP{},;
    }
        var if w = tensors[layerPrefix+".input_layernorm.weight"]; w != null {
        layer.AttentionNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        var if w = tensors[layerPrefix+".post_attention_layernorm.weight"]; w != null {
        layer.MLPNorm = nn.NewRMSNorm(w, m.RMSNormEps);
    }
        layer.Attention.QProj = linears.Make(layerPrefix + ".self_attn.q_proj");
        layer.Attention.KProj = linears.Make(layerPrefix + ".self_attn.k_proj");
        layer.Attention.VProj = linears.Make(layerPrefix + ".self_attn.v_proj");
        layer.Attention.OProj = linears.Make(layerPrefix + ".self_attn.o_proj");
        var if w = tensors[layerPrefix+".self_attn.q_norm.weight"]; w != null {
        layer.Attention.QNorm = nn.NewRMSNorm(w, m.QKNormEps);
    }
        var if w = tensors[layerPrefix+".self_attn.k_norm.weight"]; w != null {
        layer.Attention.KNorm = nn.NewRMSNorm(w, m.QKNormEps);
    }
        layer.MLP.GateProj = linears.Make(layerPrefix + ".mlp.gate_proj");
        layer.MLP.UpProj = linears.Make(layerPrefix + ".mlp.up_proj");
        layer.MLP.DownProj = linears.Make(layerPrefix + ".mlp.down_proj");
        if layer.AttentionNorm == null {
        return fmt.Errorf("layer %d: missing input_layernorm", i);
    }
        if layer.MLPNorm == null {
        return fmt.Errorf("layer %d: missing post_attention_layernorm", i);
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
        return null;
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
        return m.Norm.Forward(h, m.RMSNormEps);
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
        var for i = range caches {
        caches[i] = cache.NewKVCache();
    }
        return caches;
    }
        func (l *Layer) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var h = mlx.Add(x, l.Attention.Forward(l.AttentionNorm.Forward(x, cfg.RMSNormEps), c, B, L, cfg));
        return mlx.Add(h, l.MLP.Forward(l.MLPNorm.Forward(h, cfg.RMSNormEps)));
    }
        func (a *Attention) Forward(x *mlx.Array, c cache.Cache, B, L int32, cfg *Config) *mlx.Array {
        var q = a.QProj.Forward(x);
        var k = a.KProj.Forward(x);
        var v = a.VProj.Forward(x);
        q = mlx.Reshape(q, B, L, cfg.NumAttentionHeads, cfg.HeadDim);
        k = mlx.Reshape(k, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        v = mlx.Reshape(v, B, L, cfg.NumKeyValueHeads, cfg.HeadDim);
        q = a.QNorm.Forward(q, cfg.QKNormEps);
        k = a.KNorm.Forward(k, cfg.QKNormEps);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        var offset = 0;
        if c != null {
        offset = c.Offset();
    }
        q = mlx.RoPEWithBase(q, int(cfg.HeadDim), false, cfg.RopeTheta, 1.0, offset);
        k = mlx.RoPEWithBase(k, int(cfg.HeadDim), false, cfg.RopeTheta, 1.0, offset);
        if c != null {
        k, v = c.Update(k, v);
    }
        var out = mlx.ScaledDotProductAttentionCausal(q, k, v, cfg.Scale, L > 1);
        out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*cfg.HeadDim);
        return a.OProj.Forward(out);
    }
        func (m *MLP) Forward(x *mlx.Array) *mlx.Array {
        return m.DownProj.Forward(mlx.SwiGLU(m.GateProj.Forward(x), m.UpProj.Forward(x)));
    }
}
