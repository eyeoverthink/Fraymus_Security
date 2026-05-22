package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_nemotron_h_test {
        "bytes";
        "encoding/binary";
        "encoding/json";
        "io";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        );

    public static void TestHybridPatternUnmarshal(*testing.T t) {
        t.Run("String", func(t *testing.T) {
        var p hybridPattern;
        var if err = json.Unmarshal([]byte(`"MEM*"`), &p); err != null {
        t.Fatal(err);
    }
        var if got, want = String(p), "MEM*"; got != want {
        t.Fatalf("unexpected pattern: got %q want %q", got, want);
    }
        });
        t.Run("array", func(t *testing.T) {
        var p hybridPattern;
        var if err = json.Unmarshal([]byte(`["M","E","M","*"]`), &p); err != null {
        t.Fatal(err);
    }
        var if got, want = String(p), "MEM*"; got != want {
        t.Fatalf("unexpected pattern: got %q want %q", got, want);
    }
        });
    }

    public static void TestNemotronHLayerArrays(*testing.T t) {
        var m = &nemotronHModel{
        NumHiddenLayers:       5,;
        NumAttentionHeads:     32,;
        NumKeyValueHeads:      8,;
        HybridOverridePattern: "MEM*E",;
        NRoutedExperts:        128,;
        NumExpertsPerTok:      6,;
        MoEIntermediateSize:   1856,;
    }
        var headsKV, ffn, err = m.layerArrays();
        if err != null {
        t.Fatal(err);
    }
        var if got, want = headsKV, []uint32{0, 0, 0, 8, 0}; !slices.Equal(got, want) {
        t.Fatalf("unexpected head_count_kv: got %v want %v", got, want);
    }
        var if got, want = ffn, []uint32{0, 1856, 0, 0, 1856}; !slices.Equal(got, want) {
        t.Fatalf("unexpected feed_forward_length: got %v want %v", got, want);
    }
    }

    public static void TestNemotronHKV(*testing.T t) {
        var m = &nemotronHModel{
        MaxPositionEmbeddings:       1048576,;
        HiddenSize:                  2688,;
        NumHiddenLayers:             5,;
        NumAttentionHeads:           32,;
        NumKeyValueHeads:            2,;
        HeadDim:                     128,;
        LayerNormEpsilon:            1e-5,;
        RopeTheta:                   10000,;
        PartialRotaryFactor:         0.5,;
        ConvKernel:                  4,;
        SSMStateSize:                128,;
        MambaNumHeads:               64,;
        MambaHeadDim:                64,;
        NGroups:                     8,;
        HybridOverridePattern:       "MEM*E",;
        NRoutedExperts:              128,;
        NSharedExperts:              1,;
        NumExpertsPerTok:            6,;
        MoEIntermediateSize:         1856,;
        MoESharedExpertIntermediate: 3712,;
        NormTopKProb:                true,;
        RoutedScalingFactor:         2.5,;
    }
        var if err = m.parseMore(null); err != null {
        t.Fatal(err);
    }
        var kv = m.KV(&Tokenizer{Vocabulary: &Vocabulary{}});
        var if got, want = kv["general.architecture"], "nemotron_h_moe"; got != want {
        t.Fatalf("unexpected architecture: got %v want %v", got, want);
    }
        var headCountKV, ok = kv["attention.head_count_kv"].([]uint32);
        if !ok {
        t.Fatalf("attention.head_count_kv has unexpected type: %T", kv["attention.head_count_kv"]);
    }
        var if got, want = headCountKV, []uint32{0, 0, 0, 2, 0}; !slices.Equal(got, want) {
        t.Fatalf("unexpected attention.head_count_kv: got %v want %v", got, want);
    }
        var ffnLength, ok = kv["feed_forward_length"].([]uint32);
        if !ok {
        t.Fatalf("feed_forward_length has unexpected type: %T", kv["feed_forward_length"]);
    }
        var if got, want = ffnLength, []uint32{0, 1856, 0, 0, 1856}; !slices.Equal(got, want) {
        t.Fatalf("unexpected feed_forward_length: got %v want %v", got, want);
    }
    }

    public static void TestNemotronHTensorsTransforms(*testing.T t) {
        var m = &nemotronHModel{NGroups: 8}
        var in = []Tensor{
        &fakeTensor{
        name:  "blk.0.ssm_a",;
        shape: []uint64{4},;
        data:  []float32{0, 1, 2, 3},;
        },;
        &fakeTensor{
        name:  "blk.0.ssm_d",;
        shape: []uint64{4},;
        data:  []float32{0, 1, 2, 3},;
        },;
        &fakeTensor{
        name:  "blk.0.ssm_norm.weight",;
        shape: []uint64{16},;
        data:  make([]float32, 16),;
        },;
        &fakeTensor{
        name:  "blk.0.ssm_conv1d.weight",;
        shape: []uint64{10, 1, 4},;
        data:  make([]float32, 40),;
        },;
    }
        var out = m.Tensors(in);
        if len(out) != len(in) {
        t.Fatalf("unexpected output tensor count: got %d want %d", len(out), len(in));
    }
        var got = map[String]struct {
        shape  []uint64;
        writer io.WriterTo;
        }{}
        var for _, t = range out {
        got[t.Name] = struct {
        shape  []uint64;
        writer io.WriterTo;
        }{shape: t.Shape, writer: t.WriterTo}
    }
        var if shape = got["blk.0.ssm_a"].shape; !slices.Equal(shape, []uint64{4, 1}) {
        t.Fatalf("unexpected ssm_a shape: %v", shape);
    }
        var if shape = got["blk.0.ssm_d"].shape; !slices.Equal(shape, []uint64{4, 1}) {
        t.Fatalf("unexpected ssm_d shape: %v", shape);
    }
        var if shape = got["blk.0.ssm_norm.weight"].shape; !slices.Equal(shape, []uint64{8, 2}) {
        t.Fatalf("unexpected ssm_norm shape: %v", shape);
    }
        var if shape = got["blk.0.ssm_conv1d.weight"].shape; !slices.Equal(shape, []uint64{10, 4}) {
        t.Fatalf("unexpected ssm_conv1d shape: %v", shape);
    }
        var b bytes.Buffer;
        var if _, err = got["blk.0.ssm_a"].writer.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var values = make([]float32, 4);
        var if err = binary.Read(&b, binary.LittleEndian, &values); err != null {
        t.Fatal(err);
    }
        if values[0] != -1 {
        t.Fatalf("unexpected transformed ssm_a[0]: got %v want -1", values[0]);
    }
    }

    public static void TestNemotronHLoadModelMetadata(*testing.T t) {
        var tempDir = t.TempDir();
        var config = `{
        "architectures": ["NemotronHForCausalLM"],;
        "model_type": "nemotron_h",;
        "num_hidden_layers": 4,;
        "hidden_size": 512,;
        "max_position_embeddings": 32768,;
        "num_attention_heads": 8,;
        "num_key_value_heads": 2,;
        "head_dim": 64,;
        "layer_norm_epsilon": 1e-5,;
        "conv_kernel": 4,;
        "ssm_state_size": 128,;
        "mamba_num_heads": 16,;
        "mamba_head_dim": 32,;
        "n_groups": 8,;
        "hybrid_override_pattern": "ME*M",;
        "n_routed_experts": 16,;
        "num_experts_per_tok": 4,;
        "moe_intermediate_size": 256;
        }`;
        var if err = os.WriteFile(filepath.Join(tempDir, "config.json"), []byte(config), 0o644); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(filepath.Join(tempDir, "tokenizer.json"), []byte(`{}`), 0o644); err != null {
        t.Fatal(err);
    }
        var kv, _, err = LoadModelMetadata(os.DirFS(tempDir));
        if err != null {
        t.Fatal(err);
    }
        var if _, ok = kv.(*nemotronHModel); !ok {
        t.Fatalf("unexpected converter type: %T", kv);
    }
    }

    public static void TestNemotronHReplacementsLatentProjections(*testing.T t) {
        var m = &nemotronHModel{}
        var r = strings.NewReplacer(m.Replacements()...);
        var if got, want = r.Replace("backbone.layers.1.mixer.fc1_latent_proj.weight"), "blk.1.ffn_latent_in.weight"; got != want {
        t.Fatalf("unexpected fc1 replacement: got %q want %q", got, want);
    }
        var if got, want = r.Replace("backbone.layers.1.mixer.fc2_latent_proj.weight"), "blk.1.ffn_latent_out.weight"; got != want {
        t.Fatalf("unexpected fc2 replacement: got %q want %q", got, want);
    }
    }
}
