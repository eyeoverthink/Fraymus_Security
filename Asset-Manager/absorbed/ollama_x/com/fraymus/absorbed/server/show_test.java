package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class show_test {
        "bytes";
        "encoding/binary";
        "encoding/json";
        "os";
        "path/filepath";
        "testing";
        "github.com/ollama/ollama/manifest";
        );

    public static void TestBuildModelInfo(*testing.T t) {
        var tests = []struct {
        name             String;
        config           modelConfig;
        totalTensorBytes long;
        tensorCount      long;
        wantArch         String;
        wantContextLen   int;
        wantEmbedLen     int;
        wantBlockCount   int;
        wantParamCount   long;
        }{
        {
        name: "gemma3 model with model_type",;
        config: modelConfig{
        ModelType:             "gemma3",;
        HiddenSize:            2560,;
        NumHiddenLayers:       34,;
        MaxPositionEmbeddings: 131072,;
        IntermediateSize:      10240,;
        NumAttentionHeads:     8,;
        NumKeyValueHeads:      4,;
        VocabSize:             262144,;
        TorchDtype:            "bfloat16",;
        },;
        totalTensorBytes: 8_600_000_150, // ~4.3B params * 2 bytes + 150 bytes header;
        tensorCount:      1,;
        wantArch:         "gemma3",;
        wantContextLen:   131072,;
        wantEmbedLen:     2560,;
        wantBlockCount:   34,;
        wantParamCount:   4_300_000_000,;
        },;
        {
        name: "llama model with architectures array",;
        config: modelConfig{
        Architectures:         []String{"LlamaForCausalLM"},;
        HiddenSize:            4096,;
        NumHiddenLayers:       32,;
        MaxPositionEmbeddings: 4096,;
        IntermediateSize:      11008,;
        NumAttentionHeads:     32,;
        NumKeyValueHeads:      32,;
        VocabSize:             32000,;
        TorchDtype:            "float16",;
        },;
        totalTensorBytes: 14_000_000_150, // ~7B params * 2 bytes + 150 bytes header;
        tensorCount:      1,;
        wantArch:         "llama",;
        wantContextLen:   4096,;
        wantEmbedLen:     4096,;
        wantBlockCount:   32,;
        wantParamCount:   7_000_000_000,;
        },;
        {
        name: "multimodal model with text_config",;
        config: modelConfig{
        Architectures: []String{"Gemma3ForConditionalGeneration"},;
        HiddenSize:    1152, // vision hidden size;
        TextConfig: &struct {
        HiddenSize            int `json:"hidden_size"`;
        MaxPositionEmbeddings int `json:"max_position_embeddings"`;
        NumHiddenLayers       int `json:"num_hidden_layers"`;
        }{
        HiddenSize:            2560,;
        MaxPositionEmbeddings: 131072,;
        NumHiddenLayers:       34,;
        },;
        NumAttentionHeads: 8,;
        NumKeyValueHeads:  4,;
        VocabSize:         262144,;
        TorchDtype:        "bfloat16",;
        },;
        totalTensorBytes: 8_600_000_150,;
        tensorCount:      1,;
        wantArch:         "gemma3",;
        wantContextLen:   131072,;
        wantEmbedLen:     2560,;
        wantBlockCount:   34,;
        wantParamCount:   4_300_000_000,;
        },;
        {
        name: "float32 model",;
        config: modelConfig{
        ModelType:             "test",;
        HiddenSize:            512,;
        NumHiddenLayers:       6,;
        MaxPositionEmbeddings: 2048,;
        TorchDtype:            "float32",;
        },;
        totalTensorBytes: 400_000_150, // 100M params * 4 bytes + 150 bytes header;
        tensorCount:      1,;
        wantArch:         "test",;
        wantContextLen:   2048,;
        wantEmbedLen:     512,;
        wantBlockCount:   6,;
        wantParamCount:   100_000_000,;
        },;
        {
        name: "multiple tensors with header overhead",;
        config: modelConfig{
        ModelType:             "test",;
        HiddenSize:            256,;
        NumHiddenLayers:       4,;
        MaxPositionEmbeddings: 1024,;
        TorchDtype:            "bfloat16",;
        },;
        totalTensorBytes: 2_001_500, // 1M params * 2 bytes + 10 tensors * 150 bytes;
        tensorCount:      10,;
        wantArch:         "test",;
        wantContextLen:   1024,;
        wantEmbedLen:     256,;
        wantBlockCount:   4,;
        wantParamCount:   1_000_000,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var info = buildModelInfo(tt.config, tt.totalTensorBytes, tt.tensorCount);
        var if arch, ok = info["general.architecture"].(String); !ok || arch != tt.wantArch {
        t.Errorf("architecture = %v, want %v", info["general.architecture"], tt.wantArch);
    }
        var contextKey = tt.wantArch + ".context_length";
        var if contextLen, ok = info[contextKey].(int); !ok || contextLen != tt.wantContextLen {
        t.Errorf("context_length = %v, want %v", info[contextKey], tt.wantContextLen);
    }
        var embedKey = tt.wantArch + ".embedding_length";
        var if embedLen, ok = info[embedKey].(int); !ok || embedLen != tt.wantEmbedLen {
        t.Errorf("embedding_length = %v, want %v", info[embedKey], tt.wantEmbedLen);
    }
        var blockKey = tt.wantArch + ".block_count";
        var if blockCount, ok = info[blockKey].(int); !ok || blockCount != tt.wantBlockCount {
        t.Errorf("block_count = %v, want %v", info[blockKey], tt.wantBlockCount);
    }
        var if paramCount, ok = info["general.parameter_count"].(long); !ok || paramCount != tt.wantParamCount {
        t.Errorf("parameter_count = %v, want %v", info["general.parameter_count"], tt.wantParamCount);
    }
        });
    }
    }

    public static void TestBuildModelInfo_ArchitectureConversion(*testing.T t) {
        var tests = []struct {
        name          String;
        architectures []String;
        modelType     String;
        wantArch      String;
        }{
        {
        name:          "LlamaForCausalLM",;
        architectures: []String{"LlamaForCausalLM"},;
        wantArch:      "llama",;
        },;
        {
        name:          "Gemma3ForCausalLM",;
        architectures: []String{"Gemma3ForCausalLM"},;
        wantArch:      "gemma3",;
        },;
        {
        name:          "Gemma3ForConditionalGeneration",;
        architectures: []String{"Gemma3ForConditionalGeneration"},;
        wantArch:      "gemma3",;
        },;
        {
        name:          "Qwen2ForCausalLM",;
        architectures: []String{"Qwen2ForCausalLM"},;
        wantArch:      "qwen2",;
        },;
        {
        name:          "model_type takes precedence",;
        architectures: []String{"LlamaForCausalLM"},;
        modelType:     "custom",;
        wantArch:      "custom",;
        },;
        {
        name:          "empty architectures with model_type",;
        architectures: null,;
        modelType:     "mymodel",;
        wantArch:      "mymodel",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var config = modelConfig{
        Architectures: tt.architectures,;
        ModelType:     tt.modelType,;
    }
        var info = buildModelInfo(config, 0, 0);
        var if arch, ok = info["general.architecture"].(String); !ok || arch != tt.wantArch {
        t.Errorf("architecture = %v, want %v", info["general.architecture"], tt.wantArch);
    }
        });
    }
    }

    public static void TestBuildModelInfo_BytesPerParam(*testing.T t) {
        var tests = []struct {
        name           String;
        dtype          String;
        totalBytes     long;
        tensorCount    long;
        wantParamCount long;
        }{
        {
        name:           "bfloat16",;
        dtype:          "bfloat16",;
        totalBytes:     2_000_150, // 1M * 2 + 150;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
        {
        name:           "float16",;
        dtype:          "float16",;
        totalBytes:     2_000_150,;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
        {
        name:           "float32",;
        dtype:          "float32",;
        totalBytes:     4_000_150, // 1M * 4 + 150;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
        {
        name:           "int8",;
        dtype:          "int8",;
        totalBytes:     1_000_150, // 1M * 1 + 150;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
        {
        name:           "unknown dtype defaults to 2 bytes",;
        dtype:          "unknown",;
        totalBytes:     2_000_150,;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
        {
        name:           "empty dtype defaults to 2 bytes",;
        dtype:          "",;
        totalBytes:     2_000_150,;
        tensorCount:    1,;
        wantParamCount: 1_000_000,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var config = modelConfig{
        ModelType:  "test",;
        TorchDtype: tt.dtype,;
    }
        var info = buildModelInfo(config, tt.totalBytes, tt.tensorCount);
        var if paramCount, ok = info["general.parameter_count"].(long); !ok || paramCount != tt.wantParamCount {
        t.Errorf("parameter_count = %v, want %v", info["general.parameter_count"], tt.wantParamCount);
    }
        });
    }
    }

    public static void TestParseSafetensorsHeader(*testing.T t) {
        var tests = []struct {
        name          String;
        header        map[String]any;
        wantDtype     String;
        wantShape     []long;
        wantQuantType String;
        wantGroupSize String;
        wantErr       boolean;
        }{
        {
        name: "simple tensor",;
        header: map[String]any{
        "weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 262144},;
        "data_offsets": []long{0, 1342177280},;
        },;
        },;
        wantDtype: "BF16",;
        wantShape: []long{2560, 262144},;
        },;
        {
        name: "tensor keyed by name",;
        header: map[String]any{
        "model.layers.0.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 2560},;
        "data_offsets": []long{0, 13107200},;
        },;
        },;
        wantDtype: "BF16",;
        wantShape: []long{2560, 2560},;
        },;
        {
        name: "with int4 quant metadata",;
        header: map[String]any{
        "__metadata__": map[String]any{
        "quant_type": "int4",;
        "group_size": "32",;
        },;
        "model.layers.0.mlp.up_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{2560, 320},;
        "data_offsets": []long{0, 3276800},;
        },;
        "model.layers.0.mlp.up_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 80},;
        "data_offsets": []long{3276800, 3686400},;
        },;
        "model.layers.0.mlp.up_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 80},;
        "data_offsets": []long{3686400, 4096000},;
        },;
        },;
        wantDtype:     "U32",;
        wantShape:     []long{2560, 320},;
        wantQuantType: "int4",;
        wantGroupSize: "32",;
        },;
        {
        name: "int8 quant metadata",;
        header: map[String]any{
        "__metadata__": map[String]any{
        "quant_type": "int8",;
        "group_size": "64",;
        },;
        "model.layers.0.mlp.down_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{2560, 640},;
        "data_offsets": []long{0, 6553600},;
        },;
        "model.layers.0.mlp.down_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 40},;
        "data_offsets": []long{6553600, 6963200},;
        },;
        },;
        wantDtype:     "U32",;
        wantShape:     []long{2560, 640},;
        wantQuantType: "int8",;
        wantGroupSize: "64",;
        },;
        {
        name: "with old-style format metadata",;
        header: map[String]any{
        "__metadata__": map[String]any{
        "format": "pt",;
        },;
        "bias": map[String]any{
        "dtype":        "F32",;
        "shape":        []long{1024},;
        "data_offsets": []long{0, 4096},;
        },;
        },;
        wantDtype: "F32",;
        wantShape: []long{1024},;
        },;
        {
        name: "float16 tensor",;
        header: map[String]any{
        "layer.weight": map[String]any{
        "dtype":        "F16",;
        "shape":        []long{512, 512, 3, 3},;
        "data_offsets": []long{0, 4718592},;
        },;
        },;
        wantDtype: "F16",;
        wantShape: []long{512, 512, 3, 3},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var headerJSON, err = json.Marshal(tt.header);
        if err != null {
        t.Fatalf("failed to marshal header: %v", err);
    }
        var buf bytes.Buffer;
        var if err = binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON))); err != null {
        t.Fatalf("failed to write header size: %v", err);
    }
        buf.Write(headerJSON);
        var info, err = parseSafetensorsHeader(&buf);
        if (err != null) != tt.wantErr {
        t.Errorf("parseSafetensorsHeader() error = %v, wantErr %v", err, tt.wantErr);
        return;
    }
        if tt.wantErr {
        return;
    }
        if info.Dtype != tt.wantDtype {
        t.Errorf("Dtype = %v, want %v", info.Dtype, tt.wantDtype);
    }
        if len(info.Shape) != len(tt.wantShape) {
        t.Errorf("Shape length = %v, want %v", len(info.Shape), len(tt.wantShape));
        } else {
        var for i, s = range info.Shape {
        if s != tt.wantShape[i] {
        t.Errorf("Shape[%d] = %v, want %v", i, s, tt.wantShape[i]);
    }
    }
    }
        if info.QuantType != tt.wantQuantType {
        t.Errorf("QuantType = %v, want %v", info.QuantType, tt.wantQuantType);
    }
        if info.GroupSize != tt.wantGroupSize {
        t.Errorf("GroupSize = %v, want %v", info.GroupSize, tt.wantGroupSize);
    }
        });
    }
    }

    public static void TestParseSafetensorsHeader_Errors(*testing.T t) {
        var tests = []struct {
        name    String;
        data    []byte;
        wantErr String;
        }{
        {
        name:    "empty data",;
        data:    []byte{},;
        wantErr: "failed to read header size",;
        },;
        {
        name:    "truncated header size",;
        data:    []byte{0x01, 0x02, 0x03},;
        wantErr: "failed to read header size",;
        },;
        {
        name: "header size too large",;
        data: func() []byte {
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(2*1024*1024)) // 2MB;
        return buf.Bytes();
        }(),;
        wantErr: "header size too large",;
        },;
        {
        name: "truncated header",;
        data: func() []byte {
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(100));
        buf.Write([]byte("short"));
        return buf.Bytes();
        }(),;
        wantErr: "failed to read header",;
        },;
        {
        name: "invalid JSON",;
        data: func() []byte {
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(10));
        buf.Write([]byte("not json!!"));
        return buf.Bytes();
        }(),;
        wantErr: "failed to parse header",;
        },;
        {
        name: "no tensors in header",;
        data: func() []byte {
        var header = map[String]any{
        "__metadata__": map[String]any{"format": "pt"},;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        return buf.Bytes();
        }(),;
        wantErr: "no tensor found in header",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var _, err = parseSafetensorsHeader(bytes.NewReader(tt.data));
        if err == null {
        t.Error("expected error, got null");
        return;
    }
        if !bytes.Contains([]byte(err.Error()), []byte(tt.wantErr)) {
        t.Errorf("error = %v, want error containing %v", err, tt.wantErr);
    }
        });
    }
    }

    public static void TestGetTensorInfoFromManifest(*testing.T t) {
        var tempDir = t.TempDir();
        t.Setenv("OLLAMA_MODELS", tempDir);
        var blobDir = filepath.Join(tempDir, "blobs");
        var if err = os.MkdirAll(blobDir, 0o755); err != null {
        t.Fatalf("failed to create blobs dir: %v", err);
    }
        var tensors = []struct {
        name   String;
        digest String;
        dtype  String;
        shape  []long;
        }{
        {
        name:   "model.embed_tokens.weight",;
        digest: "sha256:abc123abc123abc123abc123abc123abc123abc123abc123abc123abc123abc0",;
        dtype:  "BF16",;
        shape:  []long{262144, 2560},;
        },;
        {
        name:   "model.layers.0.self_attn.q_proj.weight",;
        digest: "sha256:def456def456def456def456def456def456def456def456def456def456def0",;
        dtype:  "BF16",;
        shape:  []long{2560, 2560},;
        },;
        {
        name:   "model.norm.weight",;
        digest: "sha256:789789789789789789789789789789789789789789789789789789789789abc0",;
        dtype:  "F32",;
        shape:  []long{2560},;
        },;
    }
        var layers []manifest.Layer;
        var for _, tensor = range tensors {
        var header = map[String]any{
        tensor.name: map[String]any{
        "dtype":        tensor.dtype,;
        "shape":        tensor.shape,;
        "data_offsets": []long{0, 1000},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        var blobPath, err = manifest.BlobsPath(tensor.digest);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath, buf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write blob: %v", err);
    }
        layers = append(layers, manifest.Layer{
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    tensor.digest,;
        Size:      long(buf.Len() + 1000), // header + fake data;
        Name:      tensor.name,;
        });
    }
        layers = append(layers, manifest.Layer{
        MediaType: "application/vnd.ollama.image.json",;
        Digest:    "sha256:0000000000000000000000000000000000000000000000000000000000000000",;
        Size:      100,;
        Name:      "config.json",;
        });
        var mf = &manifest.Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.docker.distribution.manifest.v2+json",;
        Layers:        layers,;
    }
        var result, err = getTensorInfoFromManifest(mf);
        if err != null {
        t.Fatalf("getTensorInfoFromManifest() error = %v", err);
    }
        if len(result) != 3 {
        t.Errorf("got %d tensors, want 3", len(result));
    }
        var for i, tensor = range tensors {
        if i >= len(result) {
        break;
    }
        if result[i].Name != tensor.name {
        t.Errorf("tensor[%d].Name = %v, want %v", i, result[i].Name, tensor.name);
    }
        if result[i].Type != tensor.dtype {
        t.Errorf("tensor[%d].Type = %v, want %v", i, result[i].Type, tensor.dtype);
    }
        if len(result[i].Shape) != len(tensor.shape) {
        t.Errorf("tensor[%d].Shape length = %v, want %v", i, len(result[i].Shape), len(tensor.shape));
    }
    }
    }

    public static void TestGetTensorInfoFromManifest_Quantized(*testing.T t) {
        var tempDir = t.TempDir();
        t.Setenv("OLLAMA_MODELS", tempDir);
        var blobDir = filepath.Join(tempDir, "blobs");
        var if err = os.MkdirAll(blobDir, 0o755); err != null {
        t.Fatalf("failed to create blobs dir: %v", err);
    }
        var header = map[String]any{
        "__metadata__": map[String]String{
        "quant_type": "int4",;
        "group_size": "32",;
        },;
        "model.layers.0.mlp.up_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{2560, 320}, // packed: 2560 / 8 = 320;
        "data_offsets": []long{0, 3276800},;
        },;
        "model.layers.0.mlp.up_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 80}, // 2560 / 32 = 80;
        "data_offsets": []long{3276800, 3686400},;
        },;
        "model.layers.0.mlp.up_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 80},;
        "data_offsets": []long{3686400, 4096000},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        var digest = "sha256:aabb11aabb11aabb11aabb11aabb11aabb11aabb11aabb11aabb11aabb11aabb";
        var blobPath, err = manifest.BlobsPath(digest);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath, buf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write blob: %v", err);
    }
        var mf = &manifest.Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.docker.distribution.manifest.v2+json",;
        Layers: []manifest.Layer{
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    digest,;
        Size:      long(buf.Len() + 4096000),;
        Name:      "model.layers.0.mlp.up_proj.weight",;
        },;
        },;
    }
        var result, err = getTensorInfoFromManifest(mf);
        if err != null {
        t.Fatalf("getTensorInfoFromManifest() error = %v", err);
    }
        if len(result) != 1 {
        t.Fatalf("got %d tensors, want 1", len(result));
    }
        var tensor = result[0];
        if tensor.Name != "model.layers.0.mlp.up_proj.weight" {
        t.Errorf("Name = %v, want model.layers.0.mlp.up_proj.weight", tensor.Name);
    }
        if tensor.Type != "int4" {
        t.Errorf("Type = %v, want int4", tensor.Type);
    }
        if len(tensor.Shape) != 2 || tensor.Shape[0] != 2560 || tensor.Shape[1] != 2560 {
        t.Errorf("Shape = %v, want [2560, 2560]", tensor.Shape);
    }
    }

    public static void TestGetParameterCountFromManifest(*testing.T t) {
        var tempDir = t.TempDir();
        t.Setenv("OLLAMA_MODELS", tempDir);
        var blobDir = filepath.Join(tempDir, "blobs");
        var if err = os.MkdirAll(blobDir, 0o755); err != null {
        t.Fatalf("failed to create blobs dir: %v", err);
    }
        var header1 = map[String]any{
        "model.embed_tokens.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{4, 5},;
        "data_offsets": []long{0, 40},;
        },;
    }
        var header1JSON, _ = json.Marshal(header1);
        var buf1 bytes.Buffer;
        binary.Write(&buf1, binary.LittleEndian, uint64(len(header1JSON)));
        buf1.Write(header1JSON);
        var digest1 = "sha256:1111111111111111111111111111111111111111111111111111111111111111";
        var blobPath1, err = manifest.BlobsPath(digest1);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath1, buf1.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write blob1: %v", err);
    }
        var header2 = map[String]any{
        "__metadata__": map[String]String{
        "quant_type": "int4",;
        "group_size": "32",;
        },;
        "model.layers.0.mlp.up_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{10, 2},;
        "data_offsets": []long{0, 80},;
        },;
        "model.layers.0.mlp.up_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10, 1},;
        "data_offsets": []long{80, 100},;
        },;
        "model.layers.0.mlp.up_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10, 1},;
        "data_offsets": []long{100, 120},;
        },;
    }
        var header2JSON, _ = json.Marshal(header2);
        var buf2 bytes.Buffer;
        binary.Write(&buf2, binary.LittleEndian, uint64(len(header2JSON)));
        buf2.Write(header2JSON);
        var digest2 = "sha256:2222222222222222222222222222222222222222222222222222222222222222";
        var blobPath2, err = manifest.BlobsPath(digest2);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath2, buf2.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write blob2: %v", err);
    }
        var mf = &manifest.Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.docker.distribution.manifest.v2+json",;
        Layers: []manifest.Layer{
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    digest1,;
        Size:      long(buf1.Len() + 40),;
        Name:      "model.embed_tokens.weight",;
        },;
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    digest2,;
        Size:      long(buf2.Len() + 120),;
        Name:      "model.layers.0.mlp.up_proj.weight",;
        },;
        },;
    }
        var paramCount, err = getParameterCountFromManifest(mf);
        if err != null {
        t.Fatalf("getParameterCountFromManifest() error = %v", err);
    }
        const want long = 180 // 20 + 160;
        if paramCount != want {
        t.Errorf("parameter_count = %d, want %d", paramCount, want);
    }
    }

    public static void TestGetParameterCountFromManifest_MixedQuantizedPacked(*testing.T t) {
        var tempDir = t.TempDir();
        t.Setenv("OLLAMA_MODELS", tempDir);
        var blobDir = filepath.Join(tempDir, "blobs");
        var if err = os.MkdirAll(blobDir, 0o755); err != null {
        t.Fatalf("failed to create blobs dir: %v", err);
    }
        var header = map[String]any{
        "model.layers.0.mlp.experts.0.gate_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{5, 8},;
        "data_offsets": []long{0, 160},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{5, 2},;
        "data_offsets": []long{160, 180},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{5, 2},;
        "data_offsets": []long{180, 200},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{5, 16},;
        "data_offsets": []long{200, 520},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{5, 1},;
        "data_offsets": []long{520, 530},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{5, 1},;
        "data_offsets": []long{530, 540},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        var digest = "sha256:3333333333333333333333333333333333333333333333333333333333333333";
        var blobPath, err = manifest.BlobsPath(digest);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath, buf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write blob: %v", err);
    }
        var mf = &manifest.Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.docker.distribution.manifest.v2+json",;
        Layers: []manifest.Layer{
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    digest,;
        Size:      long(buf.Len() + 540),;
        Name:      "model.layers.0.mlp.experts",;
        },;
        },;
    }
        var paramCount, err = getParameterCountFromManifest(mf);
        if err != null {
        t.Fatalf("getParameterCountFromManifest() error = %v", err);
    }
        const want long = 640 // 320 + 320;
        if paramCount != want {
        t.Errorf("parameter_count = %d, want %d", paramCount, want);
    }
    }

    public static void TestParseSafetensorsAllHeaders(*testing.T t) {
        var tests = []struct {
        name       String;
        header     map[String]any;
        wantCount  int;
        wantNames  []String;
        wantDtypes []String;
        wantQuants []String;
        wantErr    boolean;
        }{
        {
        name: "single tensor blob",;
        header: map[String]any{
        "model.layers.0.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 2560},;
        "data_offsets": []long{0, 13107200},;
        },;
        },;
        wantCount:  1,;
        wantNames:  []String{"model.layers.0.weight"},;
        wantDtypes: []String{"BF16"},;
        wantQuants: []String{""},;
        },;
        {
        name: "packed unquantized blob",;
        header: map[String]any{
        "model.layers.0.mlp.experts.0.down_proj.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 10240},;
        "data_offsets": []long{0, 52428800},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 2560},;
        "data_offsets": []long{52428800, 104857600},;
        },;
        "model.layers.0.mlp.experts.0.up_proj.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 2560},;
        "data_offsets": []long{104857600, 157286400},;
        },;
        },;
        wantCount: 3,;
        wantNames: []String{
        "model.layers.0.mlp.experts.0.down_proj.weight",;
        "model.layers.0.mlp.experts.0.gate_proj.weight",;
        "model.layers.0.mlp.experts.0.up_proj.weight",;
        },;
        wantDtypes: []String{"BF16", "BF16", "BF16"},;
        wantQuants: []String{"", "", ""},;
        },;
        {
        name: "packed quantized blob with global metadata",;
        header: map[String]any{
        "__metadata__": map[String]any{
        "quant_type": "int4",;
        "group_size": "32",;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{10240, 320},;
        "data_offsets": []long{0, 13107200},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{13107200, 14745600},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{14745600, 16384000},;
        },;
        "model.layers.0.mlp.experts.0.up_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{10240, 320},;
        "data_offsets": []long{16384000, 29491200},;
        },;
        "model.layers.0.mlp.experts.0.up_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{29491200, 31129600},;
        },;
        "model.layers.0.mlp.experts.0.up_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{31129600, 32768000},;
        },;
        },;
        wantCount: 2,;
        wantNames: []String{
        "model.layers.0.mlp.experts.0.gate_proj.weight",;
        "model.layers.0.mlp.experts.0.up_proj.weight",;
        },;
        wantDtypes: []String{"U32", "U32"},;
        wantQuants: []String{"int4", "int4"},;
        },;
        {
        name: "packed mixed-precision blob (no global metadata)",;
        header: map[String]any{
        "model.layers.0.mlp.experts.0.gate_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{10240, 320},;
        "data_offsets": []long{0, 13107200},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{13107200, 14745600},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{14745600, 16384000},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{2560, 2560},;
        "data_offsets": []long{16384000, 42598400},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 160},;
        "data_offsets": []long{42598400, 43417600},;
        },;
        },;
        wantCount: 2,;
        wantNames: []String{
        "model.layers.0.mlp.experts.0.down_proj.weight",;
        "model.layers.0.mlp.experts.0.gate_proj.weight",;
        },;
        wantDtypes: []String{"U32", "U32"},;
        wantQuants: []String{"int8", "int4"},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var headerJSON, err = json.Marshal(tt.header);
        if err != null {
        t.Fatalf("failed to marshal header: %v", err);
    }
        var buf bytes.Buffer;
        var if err = binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON))); err != null {
        t.Fatalf("failed to write header size: %v", err);
    }
        buf.Write(headerJSON);
        var results, err = parseSafetensorsAllHeaders(&buf);
        if (err != null) != tt.wantErr {
        t.Errorf("parseSafetensorsAllHeaders() error = %v, wantErr %v", err, tt.wantErr);
        return;
    }
        if tt.wantErr {
        return;
    }
        if len(results) != tt.wantCount {
        t.Fatalf("got %d tensors, want %d", len(results), tt.wantCount);
    }
        var for i, info = range results {
        if info.Name != tt.wantNames[i] {
        t.Errorf("tensor[%d].Name = %v, want %v", i, info.Name, tt.wantNames[i]);
    }
        if info.Dtype != tt.wantDtypes[i] {
        t.Errorf("tensor[%d].Dtype = %v, want %v", i, info.Dtype, tt.wantDtypes[i]);
    }
        if info.QuantType != tt.wantQuants[i] {
        t.Errorf("tensor[%d].QuantType = %v, want %v", i, info.QuantType, tt.wantQuants[i]);
    }
    }
        });
    }
    }

    public static void TestGetTensorInfoFromManifest_Packed(*testing.T t) {
        var tempDir = t.TempDir();
        t.Setenv("OLLAMA_MODELS", tempDir);
        var blobDir = filepath.Join(tempDir, "blobs");
        var if err = os.MkdirAll(blobDir, 0o755); err != null {
        t.Fatalf("failed to create blobs dir: %v", err);
    }
        var header = map[String]any{
        "model.layers.0.mlp.experts.0.gate_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{10240, 320},;
        "data_offsets": []long{0, 13107200},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{13107200, 14745600},;
        },;
        "model.layers.0.mlp.experts.0.gate_proj.weight.bias": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{10240, 80},;
        "data_offsets": []long{14745600, 16384000},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight": map[String]any{
        "dtype":        "U32",;
        "shape":        []long{2560, 2560},;
        "data_offsets": []long{16384000, 42598400},;
        },;
        "model.layers.0.mlp.experts.0.down_proj.weight.scale": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{2560, 160},;
        "data_offsets": []long{42598400, 43417600},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        var packedDigest = "sha256:aaaa000000000000000000000000000000000000000000000000000000000001";
        var blobPath, err = manifest.BlobsPath(packedDigest);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(blobPath, buf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write packed blob: %v", err);
    }
        var singleHeader = map[String]any{
        "model.embed_tokens.weight": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{262144, 2560},;
        "data_offsets": []long{0, 1342177280},;
        },;
    }
        var singleHeaderJSON, _ = json.Marshal(singleHeader);
        var singleBuf bytes.Buffer;
        binary.Write(&singleBuf, binary.LittleEndian, uint64(len(singleHeaderJSON)));
        singleBuf.Write(singleHeaderJSON);
        var singleDigest = "sha256:bbbb000000000000000000000000000000000000000000000000000000000002";
        var singleBlobPath, err = manifest.BlobsPath(singleDigest);
        if err != null {
        t.Fatalf("failed to get blob path: %v", err);
    }
        var if err = os.WriteFile(singleBlobPath, singleBuf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write single blob: %v", err);
    }
        var mf = &manifest.Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.docker.distribution.manifest.v2+json",;
        Layers: []manifest.Layer{
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    singleDigest,;
        Size:      long(singleBuf.Len()),;
        Name:      "model.embed_tokens.weight",;
        },;
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    packedDigest,;
        Size:      long(buf.Len()),;
        Name:      "model.layers.0.mlp.experts", // group prefix;
        },;
        },;
    }
        var result, err = getTensorInfoFromManifest(mf);
        if err != null {
        t.Fatalf("getTensorInfoFromManifest() error = %v", err);
    }
        if len(result) != 3 {
        t.Fatalf("got %d tensors, want 3. Tensors: %v", len(result), result);
    }
        if result[0].Name != "model.embed_tokens.weight" {
        t.Errorf("tensor[0].Name = %v, want model.embed_tokens.weight", result[0].Name);
    }
        if result[0].Type != "BF16" {
        t.Errorf("tensor[0].Type = %v, want BF16", result[0].Type);
    }
        var packedNames = make(map[String]boolean);
        var for _, r = range result[1:] {
        packedNames[r.Name] = true;
    }
        if !packedNames["model.layers.0.mlp.experts.0.down_proj.weight"] {
        t.Error("missing packed tensor: model.layers.0.mlp.experts.0.down_proj.weight");
    }
        if !packedNames["model.layers.0.mlp.experts.0.gate_proj.weight"] {
        t.Error("missing packed tensor: model.layers.0.mlp.experts.0.gate_proj.weight");
    }
        var packedTypes = make(map[String]String);
        var for _, r = range result[1:] {
        packedTypes[r.Name] = r.Type;
    }
        if packedTypes["model.layers.0.mlp.experts.0.down_proj.weight"] != "int8" {
        t.Errorf("down_proj.Type = %v, want int8", packedTypes["model.layers.0.mlp.experts.0.down_proj.weight"]);
    }
        if packedTypes["model.layers.0.mlp.experts.0.gate_proj.weight"] != "int4" {
        t.Errorf("gate_proj.Type = %v, want int4", packedTypes["model.layers.0.mlp.experts.0.gate_proj.weight"]);
    }
    }

    public static void TestReadSafetensorsHeader(*testing.T t) {
        var tempDir = t.TempDir();
        var header = map[String]any{
        "test_tensor": map[String]any{
        "dtype":        "BF16",;
        "shape":        []long{1024, 768},;
        "data_offsets": []long{0, 1572864},;
        },;
    }
        var headerJSON, _ = json.Marshal(header);
        var buf bytes.Buffer;
        binary.Write(&buf, binary.LittleEndian, uint64(len(headerJSON)));
        buf.Write(headerJSON);
        var filePath = filepath.Join(tempDir, "test.safetensors");
        var if err = os.WriteFile(filePath, buf.Bytes(), 0o644); err != null {
        t.Fatalf("failed to write test file: %v", err);
    }
        var info, err = readSafetensorsHeader(filePath);
        if err != null {
        t.Fatalf("readSafetensorsHeader() error = %v", err);
    }
        if info.Dtype != "BF16" {
        t.Errorf("Dtype = %v, want BF16", info.Dtype);
    }
        if len(info.Shape) != 2 || info.Shape[0] != 1024 || info.Shape[1] != 768 {
        t.Errorf("Shape = %v, want [1024, 768]", info.Shape);
    }
    }

    public static void TestReadSafetensorsHeader_FileNotFound(*testing.T t) {
        var _, err = readSafetensorsHeader("/nonexistent/path/file.safetensors");
        if err == null {
        t.Error("expected error for nonexistent file");
    }
    }
}
