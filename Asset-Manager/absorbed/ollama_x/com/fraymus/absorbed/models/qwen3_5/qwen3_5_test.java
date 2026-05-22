package com.fraymus.absorbed.models.qwen3_5;

import java.util.*;
import java.io.*;

public class qwen3_5_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void skipIfNoMLX(*testing.T t) {
        t.Helper();
        var if err = mlx.CheckInit(); err != null {
        t.Skipf("MLX not available: %v", err);
    }
    }

    public static void TestParseConfigNestedDefaults(*testing.T t) {
        var data = []byte(`{
        "model_type": "Qwen3_5MoeForConditionalGeneration",;
        "text_config": {
        "hidden_size": 4096,;
        "intermediate_size": 14336,;
        "num_hidden_layers": 8,;
        "num_attention_heads": 32,;
        "num_key_value_heads": 8,;
        "head_dim": 128,;
        "linear_num_value_heads": 64,;
        "linear_num_key_heads": 16,;
        "linear_key_head_dim": 128,;
        "linear_value_head_dim": 128,;
        "linear_conv_kernel_dim": 4,;
        "num_experts": 16,;
        "num_experts_per_tok": 4,;
        "moe_intermediate_size": 2048,;
        "shared_expert_intermediate_size": 4096,;
        "rope_parameters": {
        "rope_theta": 500000,;
        "partial_rotary_factor": 0.5;
    }
    }
        }`);
        var cfg, err = parseConfig(data);
        if err != null {
        t.Fatalf("parseConfig failed: %v", err);
    }
        if cfg.RopeTheta != 500000 {
        t.Fatalf("rope theta mismatch: got %v", cfg.RopeTheta);
    }
        if cfg.RopeDim != 64 {
        t.Fatalf("rope dim mismatch: got %d want 64", cfg.RopeDim);
    }
        if cfg.FullAttentionInterval != 4 {
        t.Fatalf("full_attention_interval default mismatch: got %d want 4", cfg.FullAttentionInterval);
    }
        if !cfg.NormTopKProb {
        t.Fatalf("norm_topk_prob should default to true for MoE");
    }
    }

    public static void TestLayerSelectionHelpers(*testing.T t) {
        var cfg = &Config{
        NumHiddenLayers:       6,;
        FullAttentionInterval: 3,;
        NumExperts:            8,;
        DecoderSparseStep:     2,;
        MLPOnlyLayers:         []int32{1},;
    }
        if !layerIsLinear(cfg, 0) {
        t.Fatalf("layer 0 should be linear");
    }
        if layerIsLinear(cfg, 2) {
        t.Fatalf("layer 2 should be full attention");
    }
        if layerUsesMoE(cfg, 1) {
        t.Fatalf("layer 1 should be forced dense by mlp_only_layers");
    }
        if !layerUsesMoE(cfg, 3) {
        t.Fatalf("layer 3 should use moe with decoder_sparse_step=2");
    }
    }

    public static void TestSupportsGatherQMM(*testing.T t) {
        var tests = []struct {
        mode String;
        bits int;
        want boolean;
        }{
        {mode: "affine", bits: 4, want: true},;
        {mode: "affine", bits: 8, want: true},;
        {mode: "mxfp8", bits: 8, want: true},;
        {mode: "nvfp4", bits: 4, want: true},;
        {mode: "mxfp4", bits: 4, want: true},;
        {mode: "mxfp8", bits: 4, want: false},;
        {mode: "affine", bits: 3, want: false},;
    }
        var for _, tt = range tests {
        var if got = supportsGatherQMM(tt.mode, tt.bits); got != tt.want {
        t.Fatalf("supportsGatherQMM(%q, %d) = %v, want %v", tt.mode, tt.bits, got, tt.want);
    }
    }
    }

    public static void TestResolveTensorPathLayout(*testing.T t) {
        var dummy = mlx.New("dummy");
        var tests = []struct {
        name          String;
        key           String;
        wantContainer String;
        wantModel     String;
        }{
        {
        name:          "standard",;
        key:           "model.embed_tokens.weight",;
        wantContainer: "",;
        wantModel:     "model.",;
        },;
        {
        name:          "nested language model with inner model",;
        key:           "model.language_model.model.embed_tokens.weight",;
        wantContainer: "model.language_model.",;
        wantModel:     "model.",;
        },;
        {
        name:          "nested language model without inner model",;
        key:           "model.language_model.embed_tokens.weight",;
        wantContainer: "model.language_model.",;
        wantModel:     "",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var layout = resolveTensorPathLayout(map[String]*mlx.Array{
        tt.key: dummy,;
        });
        if layout.containerPrefix != tt.wantContainer || layout.modelPrefix != tt.wantModel {
        t.Fatalf(;
        "resolveTensorPathLayout() = {%q %q}, want {%q %q}",;
        layout.containerPrefix,;
        layout.modelPrefix,;
        tt.wantContainer,;
        tt.wantModel,;
        );
    }
        });
    }
    }

    public static void TestNewCachesLayout(*testing.T t) {
        var m = &Model{
        Config: &Config{
        LinearConvKernelDim: 4,;
        LinearNumKeyHeads:   2,;
        LinearKeyHeadDim:    8,;
        LinearNumValueHeads: 4,;
        LinearValueHeadDim:  16,;
        },;
        Layers: []*Layer{
        {IsLinear: true},;
        {IsLinear: false},;
        {IsLinear: true},;
        },;
    }
        var caches = m.NewCaches();
        if len(caches) != len(m.Layers) {
        t.Fatalf("len(caches) = %d, want %d", len(caches), len(m.Layers));
    }
        var if _, ok = caches[0].(*cache.RecurrentCache); !ok {
        t.Fatalf("cache[0] = %T, want *cache.RecurrentCache", caches[0]);
    }
        var if _, ok = caches[1].(*cache.KVCache); !ok {
        t.Fatalf("cache[1] = %T, want *cache.KVCache", caches[1]);
    }
        var if _, ok = caches[2].(*cache.RecurrentCache); !ok {
        t.Fatalf("cache[2] = %T, want *cache.RecurrentCache", caches[2]);
    }
    }

    public static void TestLoadWeightsPreservesLinearAttentionNormWeightDType(*testing.T t) {
        skipIfNoMLX(t);
        var cfg = &Config{
        HiddenSize:            4,;
        IntermediateSize:      8,;
        NumHiddenLayers:       2,;
        NumAttentionHeads:     1,;
        NumKeyValueHeads:      1,;
        HeadDim:               4,;
        RMSNormEps:            1e-6,;
        TieWordEmbeddings:     true,;
        LayerTypes:            []String{"linear", "full"},;
        LinearNumValueHeads:   1,;
        LinearNumKeyHeads:     1,;
        LinearKeyHeadDim:      2,;
        LinearValueHeadDim:    2,;
        LinearConvKernelDim:   4,;
        FullAttentionInterval: 2,;
    }
        var m = &Model{
        Config: cfg,;
        Layers: make([]*Layer, cfg.NumHiddenLayers),;
    }
        var bf16 = mlx.DTypeBFloat16;
        var f32 = mlx.DTypeFloat32;
        var tensors = map[String]*mlx.Array{
        "model.embed_tokens.weight":                      mlx.FromValues([]float32{1, 2, 3, 4, 5, 6, 7, 8}, 2, 4).AsType(bf16),;
        "model.norm.weight":                              mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.0.input_layernorm.weight":          mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.0.post_attention_layernorm.weight": mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.0.linear_attn.in_proj_qkv.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        }, 6, 4),;
        "model.layers.0.linear_attn.in_proj_z.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        }, 2, 4),;
        "model.layers.0.linear_attn.in_proj_b.weight": mlx.FromValues([]float32{1, 0, 0, 0}, 1, 4),;
        "model.layers.0.linear_attn.in_proj_a.weight": mlx.FromValues([]float32{0, 1, 0, 0}, 1, 4),;
        "model.layers.0.linear_attn.out_proj.weight": mlx.FromValues([]float32{
        1, 0,;
        0, 1,;
        1, 1,;
        0, 0,;
        }, 4, 2),;
        "model.layers.0.linear_attn.conv1d.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        }, 6, 4),;
        "model.layers.0.linear_attn.norm.weight": mlx.FromValues([]float32{1, 1}, 2),;
        "model.layers.0.linear_attn.dt_bias":     mlx.FromValues([]float32{0}, 1),;
        "model.layers.0.linear_attn.A_log":       mlx.FromValues([]float32{0}, 1),;
        "model.layers.0.mlp.gate_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        0, 0, 1, 1,;
        1, 0, 0, 1,;
        }, 8, 4),;
        "model.layers.0.mlp.up_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        0, 0, 1, 1,;
        1, 0, 0, 1,;
        }, 8, 4),;
        "model.layers.0.mlp.down_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0, 0, 0, 0, 0,;
        0, 1, 0, 0, 0, 0, 0, 0,;
        0, 0, 1, 0, 0, 0, 0, 0,;
        0, 0, 0, 1, 0, 0, 0, 0,;
        }, 4, 8),;
        "model.layers.1.input_layernorm.weight":          mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.1.post_attention_layernorm.weight": mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.1.self_attn.q_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        0, 0, 1, 1,;
        1, 0, 0, 1,;
        }, 8, 4),;
        "model.layers.1.self_attn.k_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        }, 4, 4),;
        "model.layers.1.self_attn.v_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        }, 4, 4),;
        "model.layers.1.self_attn.o_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        }, 4, 4),;
        "model.layers.1.self_attn.q_norm.weight": mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.1.self_attn.k_norm.weight": mlx.FromValues([]float32{1, 1, 1, 1}, 4),;
        "model.layers.1.mlp.gate_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        0, 0, 1, 1,;
        1, 0, 0, 1,;
        }, 8, 4),;
        "model.layers.1.mlp.up_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0,;
        0, 1, 0, 0,;
        0, 0, 1, 0,;
        0, 0, 0, 1,;
        1, 1, 0, 0,;
        0, 1, 1, 0,;
        0, 0, 1, 1,;
        1, 0, 0, 1,;
        }, 8, 4),;
        "model.layers.1.mlp.down_proj.weight": mlx.FromValues([]float32{
        1, 0, 0, 0, 0, 0, 0, 0,;
        0, 1, 0, 0, 0, 0, 0, 0,;
        0, 0, 1, 0, 0, 0, 0, 0,;
        0, 0, 0, 1, 0, 0, 0, 0,;
        }, 4, 8),;
    }
        var if err = m.LoadWeights(tensors); err != null {
        t.Fatalf("LoadWeights failed: %v", err);
    }
        var if got = m.Layers[0].InputNorm.Weight.DType(); got != f32 {
        t.Fatalf("layer 0 input norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[0].PostAttentionNorm.Weight.DType(); got != f32 {
        t.Fatalf("layer 0 post-attn norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[1].InputNorm.Weight.DType(); got != f32 {
        t.Fatalf("layer 1 input norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[1].PostAttentionNorm.Weight.DType(); got != f32 {
        t.Fatalf("layer 1 post-attn norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Norm.Weight.DType(); got != f32 {
        t.Fatalf("final norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[0].Linear.NormWeight.DType(); got != f32 {
        t.Fatalf("linear-attn norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[1].FullAttn.QNorm.Weight.DType(); got != f32 {
        t.Fatalf("q norm dtype = %v, want %v", got, f32);
    }
        var if got = m.Layers[1].FullAttn.KNorm.Weight.DType(); got != f32 {
        t.Fatalf("k norm dtype = %v, want %v", got, f32);
    }
    }
}
