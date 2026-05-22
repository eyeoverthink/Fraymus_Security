package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class create_test {
        "bytes";
        "encoding/binary";
        "encoding/json";
        "io";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        "github.com/d4l3k/go-bfloat16";
        st "github.com/ollama/ollama/x/safetensors";
        );

    public static void TestIsTensorModelDir(*testing.T t) {
        var tests = []struct {
        name     String;
        setup    func(dir String) error;
        expected boolean;
        }{
        {
        name: "valid diffusers model with model_index.json",;
        setup: func(dir String) error {
        return os.WriteFile(filepath.Join(dir, "model_index.json"), []byte(`{"_class_name": "FluxPipeline"}`), 0o644);
        },;
        expected: true,;
        },;
        {
        name: "empty directory",;
        setup: func(dir String) error {
        return null;
        },;
        expected: false,;
        },;
        {
        name: "directory with other files but no model_index.json",;
        setup: func(dir String) error {
        return os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{}`), 0o644);
        },;
        expected: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var dir = t.TempDir();
        var if err = tt.setup(dir); err != null {
        t.Fatalf("setup failed: %v", err);
    }
        var got = IsTensorModelDir(dir);
        if got != tt.expected {
        t.Errorf("IsTensorModelDir() = %v, want %v", got, tt.expected);
    }
        });
    }
    }

    public static void TestIsSafetensorsModelDir(*testing.T t) {
        var tests = []struct {
        name     String;
        setup    func(dir String) error;
        expected boolean;
        }{
        {
        name: "valid safetensors model with config.json and .safetensors file",;
        setup: func(dir String) error {
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{"model_type": "gemma3"}`), 0o644); err != null {
        return err;
    }
        return os.WriteFile(filepath.Join(dir, "model.safetensors"), []byte("dummy"), 0o644);
        },;
        expected: true,;
        },;
        {
        name: "config.json only, no safetensors files",;
        setup: func(dir String) error {
        return os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{}`), 0o644);
        },;
        expected: false,;
        },;
        {
        name: "safetensors file only, no config.json",;
        setup: func(dir String) error {
        return os.WriteFile(filepath.Join(dir, "model.safetensors"), []byte("dummy"), 0o644);
        },;
        expected: false,;
        },;
        {
        name: "empty directory",;
        setup: func(dir String) error {
        return null;
        },;
        expected: false,;
        },;
        {
        name: "multiple safetensors files with config.json",;
        setup: func(dir String) error {
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{}`), 0o644); err != null {
        return err;
    }
        var if err = os.WriteFile(filepath.Join(dir, "model-00001-of-00002.safetensors"), []byte("dummy"), 0o644); err != null {
        return err;
    }
        return os.WriteFile(filepath.Join(dir, "model-00002-of-00002.safetensors"), []byte("dummy"), 0o644);
        },;
        expected: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var dir = t.TempDir();
        var if err = tt.setup(dir); err != null {
        t.Fatalf("setup failed: %v", err);
    }
        var got = IsSafetensorsModelDir(dir);
        if got != tt.expected {
        t.Errorf("IsSafetensorsModelDir() = %v, want %v", got, tt.expected);
    }
        });
    }
    }

    public static void TestIsSafetensorsModelDir_NonexistentDir(*testing.T t) {
        var got = IsSafetensorsModelDir("/nonexistent/path/that/does/not/exist");
        if got != false {
        t.Errorf("IsSafetensorsModelDir() = %v for nonexistent dir, want false", got);
    }
    }

    public static void createMinimalSafetensors(*testing.T t, String path) {
        t.Helper();
        var header = map[String]interface{}{
        "test_tensor": map[String]interface{}{
        "dtype":        "F32",;
        "shape":        []int{2, 2},;
        "data_offsets": []int{0, 16}, // 4 float32 values = 16 bytes;
        },;
    }
        var headerJSON, err = json.Marshal(header);
        if err != null {
        t.Fatalf("failed to marshal header: %v", err);
    }
        var padding = (8 - len(headerJSON)%8) % 8;
        headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...);
        var f, err = os.Create(path);
        if err != null {
        t.Fatalf("failed to create file: %v", err);
    }
        defer f.Close();
        var if err = binary.Write(f, binary.LittleEndian, uint64(len(headerJSON))); err != null {
        t.Fatalf("failed to write header size: %v", err);
    }
        var if _, err = f.Write(headerJSON); err != null {
        t.Fatalf("failed to write header: %v", err);
    }
        var if _, err = f.Write(make([]byte, 16)); err != null {
        t.Fatalf("failed to write tensor data: %v", err);
    }
    }

    public static void createTestSafetensors(*testing.T t, String path, []*st.TensorData tensors) {
        t.Helper();
        var data, err = io.ReadAll(st.BuildPackedSafetensorsReader(tensors));
        if err != null {
        t.Fatalf("failed to build packed safetensors: %v", err);
    }
        var if err = os.WriteFile(path, data, 0o644); err != null {
        t.Fatalf("failed to write safetensors: %v", err);
    }
    }

    public static void readSingleTensorHeader(*testing.T t) {
        t.Helper();
        var headerSize uint64;
        var if err = binary.Read(bytes.NewReader(data[:8]), binary.LittleEndian, &headerSize); err != null {
        t.Fatalf("failed to read header size: %v", err);
    }
        var header map[String]struct {
        Dtype String  `json:"dtype"`;
        Shape []int32 `json:"shape"`;
    }
        var if err = json.Unmarshal(data[8:8+headerSize], &header); err != null {
        t.Fatalf("failed to parse header: %v", err);
    }
        var for name, info = range header {
        if name == "__metadata__" {
        continue;
    }
        return info.Dtype, info.Shape;
    }
        t.Fatal("no tensor entry found in header");
        return "", null;
    }
        func readSingleTensorRaw(t *testing.T, data []byte) []byte {
        t.Helper();
        var headerSize uint64;
        var if err = binary.Read(bytes.NewReader(data[:8]), binary.LittleEndian, &headerSize); err != null {
        t.Fatalf("failed to read header size: %v", err);
    }
        var header map[String]struct {
        Dtype       String  `json:"dtype"`;
        Shape       []int32 `json:"shape"`;
        DataOffsets [2]int  `json:"data_offsets"`;
    }
        var if err = json.Unmarshal(data[8:8+headerSize], &header); err != null {
        t.Fatalf("failed to parse header: %v", err);
    }
        var for name, info = range header {
        if name == "__metadata__" {
        continue;
    }
        var start = 8 + int(headerSize) + info.DataOffsets[0];
        var end = 8 + int(headerSize) + info.DataOffsets[1];
        return data[start:end];
    }
        t.Fatal("no tensor entry found in header");
        return null;
    }
        func readSafetensorsHeaderNames(t *testing.T, data []byte) []String {
        t.Helper();
        var headerSize uint64;
        var if err = binary.Read(bytes.NewReader(data[:8]), binary.LittleEndian, &headerSize); err != null {
        t.Fatalf("failed to read header size: %v", err);
    }
        var header map[String]json.RawMessage;
        var if err = json.Unmarshal(data[8:8+headerSize], &header); err != null {
        t.Fatalf("failed to parse header: %v", err);
    }
        var names = make([]String, 0, len(header));
        var for name = range header {
        if name == "__metadata__" {
        continue;
    }
        names = append(names, name);
    }
        slices.Sort(names);
        return names;
    }

    public static void TestCreateSafetensorsModel(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{"model_type": "test", "architectures": ["TestModel"]}`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createMinimalSafetensors(t, filepath.Join(dir, "model.safetensors"));
        var createdLayers []LayerInfo;
        var manifestWritten boolean;
        var manifestModelName String;
        var manifestConfigLayer LayerInfo;
        var manifestLayers []LayerInfo;
        var statusMessages []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        var data, err = io.ReadAll(r);
        if err != null {
        return LayerInfo{}, err;
    }
        var layer = LayerInfo{
        Digest:    "sha256:test",;
        Size:      long(len(data)),;
        MediaType: mediaType,;
        Name:      name,;
    }
        createdLayers = append(createdLayers, layer);
        return layer, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        var data, err = io.ReadAll(r);
        if err != null {
        return null, err;
    }
        var layer = LayerInfo{
        Digest:    "sha256:tensor_" + name,;
        Size:      long(len(data)),;
        MediaType: "application/vnd.ollama.image.tensor",;
        Name:      name,;
    }
        createdLayers = append(createdLayers, layer);
        return []LayerInfo{layer}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        manifestWritten = true;
        manifestModelName = modelName;
        manifestConfigLayer = config;
        manifestLayers = layers;
        return null;
    }
        var progressFn = func(status String) {
        statusMessages = append(statusMessages, status);
    }
        var err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        if !manifestWritten {
        t.Error("manifest was not written");
    }
        if manifestModelName != "test-model" {
        t.Errorf("manifest model name = %q, want %q", manifestModelName, "test-model");
    }
        if manifestConfigLayer.Name != "config.json" {
        t.Errorf("config layer name = %q, want %q", manifestConfigLayer.Name, "config.json");
    }
        var hasTensor = false;
        var hasConfig = false;
        var for _, layer = range manifestLayers {
        if layer.Name == "test_tensor" {
        hasTensor = true;
    }
        if layer.Name == "config.json" {
        hasConfig = true;
    }
    }
        if !hasTensor {
        t.Error("no tensor layer found in manifest");
    }
        if !hasConfig {
        t.Error("no config layer found in manifest");
    }
        if len(statusMessages) == 0 {
        t.Error("no status messages received");
    }
    }

    public static void TestCreateSafetensorsModel_NoConfigJson(*testing.T t) {
        var dir = t.TempDir();
        createMinimalSafetensors(t, filepath.Join(dir, "model.safetensors"));
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        return LayerInfo{Name: name}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        return []LayerInfo{{Name: name}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err == null {
        t.Error("expected error for missing config.json, got null");
    }
    }

    public static void TestCreateSafetensorsModel_EmptyDir(*testing.T t) {
        var dir = t.TempDir();
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        return LayerInfo{}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        return []LayerInfo{{}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err == null {
        t.Error("expected error for empty directory, got null");
    }
    }

    public static void TestCreateSafetensorsModel_SkipsIndexJson(*testing.T t) {
        var dir = t.TempDir();
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{}`), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        var indexJSON = `{"metadata": {"total_size": 100}, "weight_map": {}}`;
        var if err = os.WriteFile(filepath.Join(dir, "model.safetensors.index.json"), []byte(indexJSON), 0o644); err != null {
        t.Fatalf("failed to write index.json: %v", err);
    }
        createMinimalSafetensors(t, filepath.Join(dir, "model.safetensors"));
        var configNames []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        configNames = append(configNames, name);
        return LayerInfo{Name: name, Digest: "sha256:test"}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        return []LayerInfo{{Name: name}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        var for _, name = range configNames {
        if name == "model.safetensors.index.json" {
        t.Error("model.safetensors.index.json should have been skipped");
    }
    }
    }

    public static void TestCreateSafetensorsModel_PacksPrequantizedTensorTriplets(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["TestModel"],;
        "quantization": {"group_size": 64, "bits": 4, "mode": "affine"}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("linear.weight", "U32", []int32{4, 4}, make([]byte, 16)),;
        st.NewTensorDataFromBytes("linear.scales", "BF16", []int32{4, 1}, make([]byte, 8)),;
        st.NewTensorDataFromBytes("linear.biases", "BF16", []int32{4, 1}, make([]byte, 8)),;
        st.NewTensorDataFromBytes("plain.weight", "F32", []int32{2, 2}, make([]byte, 16)),;
        });
        var packedHeader map[String]json.RawMessage;
        var tensorLayerNames []String;
        var createTensorLayerNames []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        var data, err = io.ReadAll(r);
        if err != null {
        return LayerInfo{}, err;
    }
        if mediaType == "application/vnd.ollama.image.tensor" && name == "linear.weight" {
        var headerSize uint64;
        var if err = binary.Read(bytes.NewReader(data[:8]), binary.LittleEndian, &headerSize); err != null {
        return LayerInfo{}, err;
    }
        var if err = json.Unmarshal(data[8:8+headerSize], &packedHeader); err != null {
        return LayerInfo{}, err;
    }
    }
        tensorLayerNames = append(tensorLayerNames, name);
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType, Size: long(len(data))}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        var if _, err = io.ReadAll(r); err != null {
        return null, err;
    }
        createTensorLayerNames = append(createTensorLayerNames, name);
        return []LayerInfo{{Name: name, Digest: "sha256:tensor_" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var if err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, progressFn); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        if packedHeader == null {
        t.Fatal("expected packed quantized header for linear.weight");
    }
        var if _, ok = packedHeader["linear.weight"]; !ok {
        t.Fatalf("packed header missing linear.weight: %v", packedHeader);
    }
        var if _, ok = packedHeader["linear.weight.scale"]; !ok {
        t.Fatalf("packed header missing linear.weight.scale: %v", packedHeader);
    }
        var if _, ok = packedHeader["linear.weight.bias"]; !ok {
        t.Fatalf("packed header missing linear.weight.bias: %v", packedHeader);
    }
        var metadata map[String]String;
        var if metaRaw, ok = packedHeader["__metadata__"]; ok {
        var if err = json.Unmarshal(metaRaw, &metadata); err != null {
        t.Fatalf("failed to parse packed metadata: %v", err);
    }
    }
        if metadata["quant_type"] != "int4" {
        t.Fatalf("quant_type = %q, want %q", metadata["quant_type"], "int4");
    }
        if metadata["group_size"] != "64" {
        t.Fatalf("group_size = %q, want %q", metadata["group_size"], "64");
    }
        if slices.Contains(createTensorLayerNames, "linear.weight") {
        t.Fatalf("linear.weight unexpectedly handled by createTensorLayer: %v", createTensorLayerNames);
    }
        if slices.Contains(createTensorLayerNames, "linear.scales") || slices.Contains(createTensorLayerNames, "linear.biases") {
        t.Fatalf("quantized companions unexpectedly handled separately: %v", createTensorLayerNames);
    }
        if !slices.Contains(createTensorLayerNames, "plain.weight") {
        t.Fatalf("plain.weight missing from createTensorLayer calls: %v", createTensorLayerNames);
    }
        if slices.Contains(tensorLayerNames, "linear.scales") || slices.Contains(tensorLayerNames, "linear.biases") {
        t.Fatalf("quantized companions unexpectedly emitted as layers: %v", tensorLayerNames);
    }
    }

    public static void TestCreateSafetensorsModel_HFFP8AutoConvertsToMXFP8(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["TestModel"],;
        "quantization_config": {"quant_method": "fp8", "weight_block_size": [128, 128]}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("linear.weight", "F8_E4M3", []int32{2, 2}, []byte{1, 2, 3, 4}),;
        st.NewTensorDataFromBytes("linear.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("dense.weight", "BF16", []int32{128, 128}, make([]byte, 128*128*2)),;
        st.NewTensorDataFromBytes("norm.weight", "BF16", []int32{2}, make([]byte, 4)),;
        });
        var quantizeByName = make(map[String]String);
        var headerNamesByName = make(map[String][]String);
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        var _, err = io.ReadAll(r);
        if err != null {
        return LayerInfo{}, err;
    }
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        var data, err = io.ReadAll(r);
        if err != null {
        return null, err;
    }
        quantizeByName[name] = quantize;
        headerNamesByName[name] = readSafetensorsHeaderNames(t, data);
        return []LayerInfo{{Name: name, Digest: "sha256:tensor_" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error { return null }
        var if err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, func(String) {}); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        var if got = quantizeByName["linear.weight"]; got != "mxfp8" {
        t.Fatalf("linear.weight quantization = %q, want %q", got, "mxfp8");
    }
        var if got = quantizeByName["norm.weight"]; got != "" {
        t.Fatalf("norm.weight quantization = %q, want empty", got);
    }
        var if got = quantizeByName["dense.weight"]; got != "" {
        t.Fatalf("dense.weight quantization = %q, want empty", got);
    }
        var if _, ok = quantizeByName["linear.weight_scale_inv"]; ok {
        t.Fatal("linear.weight_scale_inv should not be imported as a standalone tensor");
    }
        var if got = headerNamesByName["linear.weight"]; !slices.Equal(got, []String{"linear.weight", "linear.weight.scale_inv"}) {
        t.Fatalf("linear.weight blob tensors = %v, want %v", got, []String{"linear.weight", "linear.weight.scale_inv"});
    }
        var if got = headerNamesByName["norm.weight"]; !slices.Equal(got, []String{"norm.weight"}) {
        t.Fatalf("norm.weight blob tensors = %v, want %v", got, []String{"norm.weight"});
    }
        var if got = headerNamesByName["dense.weight"]; !slices.Equal(got, []String{"dense.weight"}) {
        t.Fatalf("dense.weight blob tensors = %v, want %v", got, []String{"dense.weight"});
    }
    }

    public static void TestCreateSafetensorsModel_RejectsRequantizingQuantizedSources(*testing.T t) {
        var tests = []struct {
        name       String;
        configJSON String;
        tensors    []*st.TensorData;
        wantErr    String;
        }{
        {
        name:       "prequantized affine",;
        configJSON: `{"model_type": "test", "architectures": ["TestModel"]}`,;
        tensors: []*st.TensorData{
        st.NewTensorDataFromBytes("linear.weight", "U32", []int32{4, 4}, make([]byte, 16)),;
        st.NewTensorDataFromBytes("linear.scales", "BF16", []int32{4, 1}, make([]byte, 8)),;
        },;
        wantErr: `cannot requantize already-quantized source model with --quantize "int4"`,;
        },;
        {
        name: "hf fp8 source",;
        configJSON: `{
        "model_type": "test",;
        "architectures": ["TestModel"],;
        "quantization_config": {"quant_method": "fp8", "weight_block_size": [128, 128]}
        }`,;
        tensors: []*st.TensorData{
        st.NewTensorDataFromBytes("linear.weight", "F8_E4M3", []int32{2, 2}, []byte{1, 2, 3, 4}),;
        st.NewTensorDataFromBytes("linear.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        },;
        wantErr: `cannot requantize already-quantized fp8 source model with --quantize "int4"`,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var dir = t.TempDir();
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(tt.configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), tt.tensors);
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        return LayerInfo{}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        return null, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error { return null }
        var err = CreateSafetensorsModel("test-model", dir, "int4", createLayer, createTensorLayer, writeManifest, func(String) {});
        if err == null {
        t.Fatal("expected error, got null");
    }
        if !strings.Contains(err.Error(), tt.wantErr) {
        t.Fatalf("error = %q, want substring %q", err, tt.wantErr);
    }
        });
    }
    }

    public static void TestCreateSafetensorsModel_HFFP8PacksExperts(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["Qwen3_5MoeForConditionalGeneration"],;
        "quantization_config": {"quant_method": "fp8", "weight_block_size": [128, 128]}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.gate_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.gate_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.up_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.up_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.down_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.0.down_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.gate_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.gate_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.up_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.up_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.down_proj.weight", "F8_E4M3", []int32{128, 128}, make([]byte, 128*128)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.1.down_proj.weight_scale_inv", "BF16", []int32{1, 1}, make([]byte, 2)),;
        });
        var packedLayerNames []String;
        var packedLayerTensors [][]PackedTensorInput;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        var if _, err = io.ReadAll(r); err != null {
        return LayerInfo{}, err;
    }
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        var if _, err = io.ReadAll(r); err != null {
        return null, err;
    }
        return []LayerInfo{{Name: name, Digest: "sha256:tensor_" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var createPackedLayer = func(groupName String, tensors []PackedTensorInput) (LayerInfo, error) {
        packedLayerNames = append(packedLayerNames, groupName);
        packedLayerTensors = append(packedLayerTensors, tensors);
        return LayerInfo{Name: groupName, Digest: "sha256:packed_" + groupName, MediaType: "application/vnd.ollama.image.tensor"}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error { return null }
        var if err = CreateSafetensorsModel("test-model", dir, "", createLayer, createTensorLayer, writeManifest, func(String) {}, createPackedLayer); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        if len(packedLayerNames) != 1 {
        t.Fatalf("expected 1 packed layer, got %d: %v", len(packedLayerNames), packedLayerNames);
    }
        if packedLayerNames[0] != "language_model.model.layers.0.mlp.experts" {
        t.Fatalf("unexpected packed layer name: %s", packedLayerNames[0]);
    }
        var tensors = packedLayerTensors[0];
        if len(tensors) != 6 {
        t.Fatalf("expected 6 tensors in packed group, got %d", len(tensors));
    }
        var for _, tensor = range tensors {
        if tensor.Quantize != "mxfp8" {
        t.Fatalf("expected mxfp8 quantize for %s, got %q", tensor.Name, tensor.Quantize);
    }
    }
    }

    public static void TestCreateSafetensorsModel_Qwen35Transforms(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["Qwen3_5MoeForConditionalGeneration"],;
        "text_config": {"dtype": "bfloat16"}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        var gateUpValues = make([]float32, 2*128*64);
        var for expert = range 2 {
        var base = expert * 128 * 64;
        var for i = range 64 * 64 {
        gateUpValues[base+i] = 1;
        gateUpValues[base+64*64+i] = 2;
    }
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("model.language_model.embed_tokens.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.input_layernorm.weight", "F32", []int32{64}, make([]byte, 64*4)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.linear_attn.A_log", "F32", []int32{32}, make([]byte, 32*4)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.linear_attn.conv1d.weight", "BF16", []int32{64, 1, 4}, make([]byte, 64*1*4*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.gate.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.gate_up_proj", "BF16", []int32{2, 128, 64}, bfloat16.EncodeFloat32(gateUpValues)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.down_proj", "BF16", []int32{2, 64, 64}, bfloat16.EncodeFloat32(make([]float32, 2*64*64))),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.shared_expert.down_proj.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.visual.blocks.0.attn.proj.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("mtp.layers.0.foo.weight", "F32", []int32{64, 64}, make([]byte, 64*64*4)),;
        });

    public static class tensorCall {
        public String dtype;
        public []int32 shape;
        public String quantize;
        public []byte raw;
    }
        var calls = make(map[String]tensorCall);
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        _, _ = io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        var data, err = io.ReadAll(r);
        if err != null {
        return null, err;
    }
        var headerDType, headerShape = readSingleTensorHeader(t, data);
        calls[name] = tensorCall{
        dtype:    headerDType,;
        shape:    headerShape,;
        quantize: quantize,;
        raw:      readSingleTensorRaw(t, data),;
    }
        return []LayerInfo{{Name: name, Digest: "sha256:" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var if err = CreateSafetensorsModel("test-model", dir, "int4", createLayer, createTensorLayer, writeManifest, func(String) {}); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        var if _, ok = calls["mtp.layers.0.foo.weight"]; ok {
        t.Fatal("mtp tensor should have been dropped");
    }
        var layerNorm = calls["language_model.model.layers.0.input_layernorm.weight"];
        if layerNorm.dtype != "BF16" {
        t.Fatalf("input_layernorm dtype = %q, want %q", layerNorm.dtype, "BF16");
    }
        if layerNorm.quantize != "" {
        t.Fatalf("input_layernorm quantize = %q, want empty", layerNorm.quantize);
    }
        var layerNormValues = bfloat16.DecodeFloat32(layerNorm.raw);
        if len(layerNormValues) == 0 || layerNormValues[0] != 1.0 {
        t.Fatalf("input_layernorm first value = %v, want 1.0 after +1 shift", layerNormValues[0]);
    }
        var alog = calls["language_model.model.layers.0.linear_attn.A_log"];
        if alog.dtype != "F32" {
        t.Fatalf("A_log dtype = %q, want %q", alog.dtype, "F32");
    }
        var conv = calls["language_model.model.layers.0.linear_attn.conv1d.weight"];
        if !slices.Equal(conv.shape, []int32{64, 4, 1}) {
        t.Fatalf("conv1d shape = %v, want %v", conv.shape, []int32{64, 4, 1});
    }
        var if got = calls["language_model.model.embed_tokens.weight"].quantize; got != "int4" {
        t.Fatalf("embed_tokens quantize = %q, want %q", got, "int4");
    }
        var if got = calls["language_model.model.layers.0.mlp.gate.weight"].quantize; got != "int4" {
        t.Fatalf("mlp.gate quantize = %q, want %q", got, "int4");
    }
        var if got = calls["language_model.model.layers.0.mlp.shared_expert.down_proj.weight"].quantize; got != "int4" {
        t.Fatalf("down_proj quantize = %q, want %q", got, "int4");
    }
        var if _, ok = calls["language_model.model.layers.0.mlp.experts.gate_up_proj"]; ok {
        t.Fatal("combined gate_up_proj tensor should have been rewritten");
    }
        var if _, ok = calls["language_model.model.layers.0.mlp.experts.down_proj"]; ok {
        t.Fatal("combined down_proj tensor should have been rewritten");
    }
        var gateProj = calls["language_model.model.layers.0.mlp.switch_mlp.gate_proj.weight"];
        if !slices.Equal(gateProj.shape, []int32{2, 64, 64}) {
        t.Fatalf("gate_proj shape = %v, want %v", gateProj.shape, []int32{2, 64, 64});
    }
        var gateProjValues = bfloat16.DecodeFloat32(gateProj.raw);
        if len(gateProjValues) == 0 || gateProjValues[0] != 1.0 {
        t.Fatalf("gate_proj first value = %v, want 1.0", gateProjValues[0]);
    }
        var upProj = calls["language_model.model.layers.0.mlp.switch_mlp.up_proj.weight"];
        if !slices.Equal(upProj.shape, []int32{2, 64, 64}) {
        t.Fatalf("up_proj shape = %v, want %v", upProj.shape, []int32{2, 64, 64});
    }
        var upProjValues = bfloat16.DecodeFloat32(upProj.raw);
        if len(upProjValues) == 0 || upProjValues[0] != 2.0 {
        t.Fatalf("up_proj first value = %v, want 2.0", upProjValues[0]);
    }
        var if got = calls["language_model.model.layers.0.mlp.switch_mlp.down_proj.weight"].quantize; got != "int4" {
        t.Fatalf("switch_mlp down_proj quantize = %q, want %q", got, "int4");
    }
        var vision = calls["vision_tower.blocks.0.attn.proj.weight"];
        if vision.dtype != "BF16" {
        t.Fatalf("vision weight dtype = %q, want %q", vision.dtype, "BF16");
    }
        if vision.quantize != "" {
        t.Fatalf("vision weight quantize = %q, want empty", vision.quantize);
    }
        var if _, ok = calls["language_model.model.visual.blocks.0.attn.proj.weight"]; ok {
        t.Fatal("vision tensor should have been rewritten to vision_tower.*");
    }
    }

    public static void TestCreateSafetensorsModel_Qwen35DirectNonAffineKeepsSensitiveWeightsBF16(*testing.T t) {
        var for _, quantize = range []String{"nvfp4", "mxfp8", "mxfp4"} {
        t.Run(quantize, func(t *testing.T) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["Qwen3_5MoeForConditionalGeneration"],;
        "text_config": {"dtype": "bfloat16"}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        var gateUpValues = make([]float32, 2*128*64);
        var for expert = range 2 {
        var base = expert * 128 * 64;
        var for i = range 64 * 64 {
        gateUpValues[base+i] = 1;
        gateUpValues[base+64*64+i] = 2;
    }
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("model.language_model.embed_tokens.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("lm_head.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.linear_attn.in_proj_a.weight", "BF16", []int32{32, 64}, make([]byte, 32*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.linear_attn.in_proj_b.weight", "BF16", []int32{32, 64}, make([]byte, 32*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.gate.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.shared_expert_gate.weight", "BF16", []int32{1, 64}, make([]byte, 64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.self_attn.q_proj.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.gate_up_proj", "BF16", []int32{2, 128, 64}, bfloat16.EncodeFloat32(gateUpValues)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.down_proj", "BF16", []int32{2, 64, 64}, bfloat16.EncodeFloat32(make([]float32, 2*64*64))),;
        });

    public static class tensorCall {
        public String quantize;
    }

    public static class packedTensorCall {
        public String Name;
        public String Quantize;
    }
        var tensorCalls = make(map[String]tensorCall);
        var packedCalls = make(map[String][]packedTensorCall);
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        _, _ = io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantizeType String) ([]LayerInfo, error) {
        _, _ = io.ReadAll(r);
        tensorCalls[name] = tensorCall{quantize: quantizeType}
        return []LayerInfo{{Name: name, Digest: "sha256:" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var createPackedLayer = func(groupName String, tensors []PackedTensorInput) (LayerInfo, error) {
        var group = make([]packedTensorCall, 0, len(tensors));
        var for _, tensor = range tensors {
        group = append(group, packedTensorCall{
        Name:     tensor.Name,;
        Quantize: tensor.Quantize,;
        });
    }
        packedCalls[groupName] = group;
        return LayerInfo{Name: groupName, Digest: "sha256:" + groupName, MediaType: "application/vnd.ollama.image.tensor"}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var if err = CreateSafetensorsModel("test-model", dir, quantize, createLayer, createTensorLayer, writeManifest, func(String) {}, createPackedLayer); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        var for _, name = range []String{
        "language_model.model.embed_tokens.weight",;
        "language_model.lm_head.weight",;
        "language_model.model.layers.0.linear_attn.in_proj_a.weight",;
        "language_model.model.layers.0.linear_attn.in_proj_b.weight",;
        "language_model.model.layers.0.mlp.gate.weight",;
        "language_model.model.layers.0.mlp.shared_expert_gate.weight",;
        } {
        var if got = tensorCalls[name].quantize; got != "" {
        t.Fatalf("%s quantize = %q, want empty", name, got);
    }
    }
        var if got = tensorCalls["language_model.model.layers.0.self_attn.q_proj.weight"].quantize; got != quantize {
        t.Fatalf("q_proj quantize = %q, want %q", got, quantize);
    }
        var group = packedCalls["language_model.model.layers.0.mlp.switch_mlp"];
        if len(group) != 3 {
        t.Fatalf("packed switch_mlp tensor count = %d, want 3", len(group));
    }
        var for _, tensor = range group {
        if tensor.Quantize != quantize {
        t.Fatalf("packed tensor %q quantize = %q, want %q", tensor.Name, tensor.Quantize, quantize);
    }
    }
        });
    }
    }

    public static void TestResolveManifestPath(*testing.T t) {
        var tests = []struct {
        name      String;
        modelName String;
        wantParts []String // Parts that should appear in the path;
        }{
        {
        name:      "simple model name",;
        modelName: "llama2",;
        wantParts: []String{"registry.ollama.ai", "library", "llama2", "latest"},;
        },;
        {
        name:      "model name with tag",;
        modelName: "llama2:7b",;
        wantParts: []String{"registry.ollama.ai", "library", "llama2", "7b"},;
        },;
        {
        name:      "model name with namespace",;
        modelName: "myuser/mymodel",;
        wantParts: []String{"registry.ollama.ai", "myuser", "mymodel", "latest"},;
        },;
        {
        name:      "model name with namespace and tag",;
        modelName: "myuser/mymodel:v1",;
        wantParts: []String{"registry.ollama.ai", "myuser", "mymodel", "v1"},;
        },;
        {
        name:      "fully qualified model name",;
        modelName: "registry.example.com/namespace/model:tag",;
        wantParts: []String{"registry.example.com", "namespace", "model", "tag"},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = resolveManifestPath(tt.modelName);
        var for _, part = range tt.wantParts {
        if !strings.Contains(got, part) {
        t.Errorf("resolveManifestPath(%q) = %q, missing part %q", tt.modelName, got, part);
    }
    }
        });
    }
    }

    public static void TestLayerInfo(*testing.T t) {
        var layer = LayerInfo{
        Digest:    "sha256:abc123",;
        Size:      1024,;
        MediaType: "application/vnd.ollama.image.tensor",;
        Name:      "model.weight",;
    }
        if layer.Digest != "sha256:abc123" {
        t.Errorf("Digest = %q, want %q", layer.Digest, "sha256:abc123");
    }
        if layer.Size != 1024 {
        t.Errorf("Size = %d, want %d", layer.Size, 1024);
    }
        if layer.MediaType != "application/vnd.ollama.image.tensor" {
        t.Errorf("MediaType = %q, want %q", layer.MediaType, "application/vnd.ollama.image.tensor");
    }
        if layer.Name != "model.weight" {
        t.Errorf("Name = %q, want %q", layer.Name, "model.weight");
    }
    }

    public static void TestModelConfig(*testing.T t) {
        var config = ModelConfig{
        ModelFormat:  "safetensors",;
        Capabilities: []String{"completion", "chat"},;
    }
        if config.ModelFormat != "safetensors" {
        t.Errorf("ModelFormat = %q, want %q", config.ModelFormat, "safetensors");
    }
        if len(config.Capabilities) != 2 {
        t.Errorf("Capabilities length = %d, want %d", len(config.Capabilities), 2);
    }
    }

    public static void TestManifest(*testing.T t) {
        var manifest = Manifest{
        SchemaVersion: 2,;
        MediaType:     "application/vnd.oci.image.manifest.v1+json",;
        Config: ManifestLayer{
        MediaType: "application/vnd.docker.container.image.v1+json",;
        Digest:    "sha256:config",;
        Size:      100,;
        },;
        Layers: []ManifestLayer{
        {
        MediaType: "application/vnd.ollama.image.tensor",;
        Digest:    "sha256:layer1",;
        Size:      1000,;
        Name:      "weight.bin",;
        },;
        },;
    }
        if manifest.SchemaVersion != 2 {
        t.Errorf("SchemaVersion = %d, want %d", manifest.SchemaVersion, 2);
    }
        if manifest.Config.Digest != "sha256:config" {
        t.Errorf("Config.Digest = %q, want %q", manifest.Config.Digest, "sha256:config");
    }
        if len(manifest.Layers) != 1 {
        t.Errorf("Layers length = %d, want %d", len(manifest.Layers), 1);
    }
        if manifest.Layers[0].Name != "weight.bin" {
        t.Errorf("Layers[0].Name = %q, want %q", manifest.Layers[0].Name, "weight.bin");
    }
    }

    public static void TestShouldQuantize(*testing.T t) {
        var tests = []struct {
        name      String;
        tensor    String;
        component String;
        want      boolean;
        }{
        {"vae weight", "decoder.weight", "vae", false},;
        {"vae bias", "decoder.bias", "vae", false},;
        {"embedding weight", "embed_tokens.weight", "", false},;
        {"embedding in name", "token_embedding.weight", "", false},;
        {"layer norm", "layer_norm.weight", "", false},;
        {"rms norm", "rms_norm.weight", "", false},;
        {"ln prefix", "ln_1.weight", "", false},;
        {"layernorm in name", "input_layernorm.weight", "", false},;
        {"audio tower weight", "model.audio_tower.layers.0.weight", "", false},;
        {"audio tower norm", "model.audio_tower.norm.weight", "", false},;
        {"embed audio weight", "embed_audio.weight", "", false},;
        {"bias tensor", "attention.bias", "", false},;
        {"proj bias", "o_proj.bias", "", false},;
        {"linear weight", "q_proj.weight", "", true},;
        {"attention weight", "self_attn.weight", "", true},;
        {"mlp weight", "mlp.gate_proj.weight", "", true},;
        {"transformer weight", "layers.0.weight", "transformer", true},;
        {"text_encoder weight", "encoder.weight", "text_encoder", true},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = ShouldQuantize(tt.tensor, tt.component);
        if got != tt.want {
        t.Errorf("ShouldQuantize(%q, %q) = %v, want %v", tt.tensor, tt.component, got, tt.want);
    }
        });
    }
    }

    public static void TestShouldQuantizeTensor(*testing.T t) {
        var tests = []struct {
        name     String;
        tensor   String;
        shape    []int32;
        quantize String;
        want     boolean;
        }{
        {"large 2D weight fp8", "q_proj.weight", []int32{4096, 4096}, "fp8", true},;
        {"medium 2D weight fp8", "small_proj.weight", []int32{128, 128}, "fp8", true},;
        {"large 2D weight nvfp4", "q_proj.weight", []int32{4096, 4096}, "nvfp4", true},;
        {"large 2D weight mxfp4", "q_proj.weight", []int32{4096, 4096}, "mxfp4", true},;
        {"tiny 2D weight", "tiny.weight", []int32{16, 16}, "fp8", false},;
        {"small 2D weight", "small.weight", []int32{31, 31}, "fp8", false},;
        {"1D tensor", "layer_norm.weight", []int32{4096}, "fp8", false},;
        {"3D tensor", "conv.weight", []int32{64, 64, 3}, "fp8", false},;
        {"4D tensor", "conv2d.weight", []int32{64, 64, 3, 3}, "fp8", false},;
        {"stacked expert switch_mlp gate_up 3D int8", "model.layers.1.mlp.switch_mlp.gate_up_proj.weight", []int32{64, 22016, 4096}, "int8", true},;
        {"stacked expert experts down_proj 3D int8", "model.layers.1.mlp.experts.down_proj.weight", []int32{64, 4096, 14336}, "int8", true},;
        {"stacked expert combined gate_up 3D int8", "model.language_model.layers.0.mlp.experts.gate_up_proj", []int32{256, 1024, 2048}, "int8", true},;
        {"stacked expert combined down_proj 3D int8", "model.language_model.layers.0.mlp.experts.down_proj", []int32{256, 2048, 512}, "int8", true},;
        {"embedding 2D", "embed_tokens.weight", []int32{32000, 4096}, "fp8", false},;
        {"norm 2D", "layer_norm.weight", []int32{4096, 1}, "fp8", false},;
        {"bias 2D", "proj.bias", []int32{4096, 1}, "fp8", false},;
        {"not divisible by 32 fp8", "proj.weight", []int32{128, 48}, "fp8", false},;
        {"divisible by 32 fp8", "proj.weight", []int32{128, 64}, "fp8", true},;
        {"not divisible by 32 mxfp4", "proj.weight", []int32{128, 48}, "mxfp4", false},;
        {"divisible by 32 mxfp4", "proj.weight", []int32{128, 64}, "mxfp4", true},;
        {"not divisible by 16 nvfp4", "proj.weight", []int32{128, 24}, "nvfp4", false},;
        {"divisible by 16 nvfp4", "proj.weight", []int32{128, 48}, "nvfp4", true},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = ShouldQuantizeTensor(tt.tensor, tt.shape, tt.quantize);
        if got != tt.want {
        t.Errorf("ShouldQuantizeTensor(%q, %v, %q) = %v, want %v", tt.tensor, tt.shape, tt.quantize, got, tt.want);
    }
        });
    }
    }

    public static void TestExpertGroupPrefix(*testing.T t) {
        var tests = []struct {
        name String;
        want String;
        }{
        {"model.layers.1.mlp.experts.0.down_proj.weight", "model.layers.1.mlp.experts"},;
        {"model.layers.1.mlp.experts.63.gate_proj.weight", "model.layers.1.mlp.experts"},;
        {"model.layers.0.mlp.experts.0.up_proj.weight", "model.layers.0.mlp.experts"},;
        {"model.layers.0.moe.experts.0.gate_proj.weight", "model.layers.0.moe.experts"},;
        {"model.layers.1.moe.experts.42.down_proj.weight", "model.layers.1.moe.experts"},;
        {"language_model.model.layers.2.moe.experts.127.up_proj.weight", "language_model.model.layers.2.moe.experts"},;
        {"language_model.model.layers.0.mlp.experts.0.gate_proj.weight", "language_model.model.layers.0.mlp.experts"},;
        {"language_model.model.layers.1.mlp.experts.255.down_proj.weight", "language_model.model.layers.1.mlp.experts"},;
        {"model.layers.1.mlp.shared_experts.down_proj.weight", "model.layers.1.mlp.shared_experts"},;
        {"model.layers.2.mlp.shared_experts.gate_proj.weight", "model.layers.2.mlp.shared_experts"},;
        {"model.layers.1.mlp.switch_mlp.down_proj.weight", "model.layers.1.mlp.switch_mlp"},;
        {"language_model.layers.2.mlp.switch_mlp.gate_proj.weight", "language_model.layers.2.mlp.switch_mlp"},;
        {"language_model.model.layers.3.mlp.switch_mlp.up_proj.weight", "language_model.model.layers.3.mlp.switch_mlp"},;
        {"model.language_model.layers.4.mlp.switch_mlp.gate_proj.weight", "model.language_model.layers.4.mlp.switch_mlp"},;
        {"model.layers.0.mlp.down_proj.weight", ""},    // dense layer, no experts;
        {"model.layers.1.mlp.gate.weight", ""},         // routing gate, not an expert;
        {"model.embed_tokens.weight", ""},              // embedding;
        {"model.layers.0.self_attn.q_proj.weight", ""}, // attention;
        {"model.norm.weight", ""},                      // norm;
        {"lm_head.weight", ""},                         // output head;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = ExpertGroupPrefix(tt.name);
        if got != tt.want {
        t.Errorf("ExpertGroupPrefix(%q) = %q, want %q", tt.name, got, tt.want);
    }
        });
    }
    }

    public static void TestGetTensorQuantization_StackedExpert3D(*testing.T t) {
        var gateUp = GetTensorQuantization(;
        "model.layers.1.mlp.switch_mlp.gate_up_proj.weight",;
        []int32{64, 22016, 4096},;
        "int4",;
        );
        if gateUp != "int4" {
        t.Fatalf("gate_up_proj quantization = %q, want %q", gateUp, "int4");
    }
        var down = GetTensorQuantization(;
        "model.layers.1.mlp.experts.down_proj.weight",;
        []int32{64, 4096, 14336},;
        "int4",;
        );
        if down != "int8" {
        t.Fatalf("down_proj quantization = %q, want %q", down, "int8");
    }
        var combinedGateUp = GetTensorQuantization(;
        "model.language_model.layers.0.mlp.experts.gate_up_proj",;
        []int32{256, 1024, 2048},;
        "int8",;
        );
        if combinedGateUp != "int8" {
        t.Fatalf("combined gate_up_proj quantization = %q, want %q", combinedGateUp, "int8");
    }
        var combinedDown = GetTensorQuantization(;
        "model.language_model.layers.0.mlp.experts.down_proj",;
        []int32{256, 2048, 512},;
        "int4",;
        );
        if combinedDown != "int8" {
        t.Fatalf("combined down_proj quantization = %q, want %q", combinedDown, "int8");
    }
        var nvfp4GateUp = GetTensorQuantization(;
        "language_model.model.layers.0.mlp.switch_mlp.gate_proj.weight",;
        []int32{64, 11008, 4096},;
        "nvfp4",;
        );
        if nvfp4GateUp != "nvfp4" {
        t.Fatalf("nvfp4 gate_proj quantization = %q, want %q", nvfp4GateUp, "nvfp4");
    }
        var nvfp4Down = GetTensorQuantization(;
        "language_model.model.layers.0.mlp.switch_mlp.down_proj.weight",;
        []int32{64, 4096, 11008},;
        "nvfp4",;
        );
        if nvfp4Down != "nvfp4" {
        t.Fatalf("nvfp4 down_proj quantization = %q, want %q", nvfp4Down, "nvfp4");
    }
        var mxfp4GateUp = GetTensorQuantization(;
        "language_model.model.layers.0.mlp.switch_mlp.gate_proj.weight",;
        []int32{64, 11008, 4096},;
        "mxfp4",;
        );
        if mxfp4GateUp != "mxfp4" {
        t.Fatalf("mxfp4 gate_proj quantization = %q, want %q", mxfp4GateUp, "mxfp4");
    }
        var mxfp4Down = GetTensorQuantization(;
        "language_model.model.layers.0.mlp.switch_mlp.down_proj.weight",;
        []int32{64, 4096, 11008},;
        "mxfp4",;
        );
        if mxfp4Down != "mxfp4" {
        t.Fatalf("mxfp4 down_proj quantization = %q, want %q", mxfp4Down, "mxfp4");
    }
    }

    public static void TestIsAligned(*testing.T t) {
        var tests = []struct {
        name      String;
        shape     []int32;
        quantType String;
        want      boolean;
        }{
        {"int4 aligned", []int32{1024, 4096}, "int4", true},;
        {"int4 unaligned", []int32{1024, 48}, "int4", false},;
        {"int8 aligned", []int32{1024, 128}, "int8", true},;
        {"int8 unaligned", []int32{1024, 32}, "int8", false},;
        {"nvfp4 aligned", []int32{1024, 48}, "nvfp4", true},;
        {"nvfp4 unaligned", []int32{1024, 24}, "nvfp4", false},;
        {"nvfp4 aligned 16", []int32{1024, 16}, "nvfp4", true},;
        {"mxfp4 aligned", []int32{1024, 64}, "mxfp4", true},;
        {"mxfp4 unaligned", []int32{1024, 48}, "mxfp4", false},;
        {"mxfp8 aligned", []int32{1024, 32}, "mxfp8", true},;
        {"mxfp8 unaligned", []int32{1024, 24}, "mxfp8", false},;
        {"empty shape", []int32{}, "int4", false},;
        {"1D tensor", []int32{4096}, "int4", true},;
        {"3D stacked expert", []int32{128, 4096, 2816}, "int4", true},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = isAligned(tt.shape, tt.quantType);
        if got != tt.want {
        t.Errorf("isAligned(%v, %q) = %v, want %v", tt.shape, tt.quantType, got, tt.want);
    }
        });
    }
    }

    public static void TestGetTensorQuantization_MixedPrecisionPromotion(*testing.T t) {
        var aligned = []int32{4096, 4096} // divisible by 64;
        var tests = []struct {
        name     String;
        tensor   String;
        shape    []int32;
        quantize String;
        want     String;
        }{
        {"v_proj int4 promoted", "model.layers.0.self_attn.v_proj.weight", aligned, "int4", "int8"},;
        {"k_proj int4 promoted", "model.layers.0.self_attn.k_proj.weight", aligned, "int4", "int8"},;
        {"down_proj int4 promoted", "model.layers.0.mlp.down_proj.weight", aligned, "int4", "int8"},;
        {"q_proj int4 stays", "model.layers.0.self_attn.q_proj.weight", aligned, "int4", "int4"},;
        {"o_proj int4 stays", "model.layers.0.self_attn.o_proj.weight", aligned, "int4", "int4"},;
        {"gate_proj int4 stays", "model.layers.0.mlp.gate_proj.weight", aligned, "int4", "int4"},;
        {"up_proj int4 stays", "model.layers.0.mlp.up_proj.weight", aligned, "int4", "int4"},;
        {"v_proj nvfp4 uniform", "model.layers.0.self_attn.v_proj.weight", aligned, "nvfp4", "nvfp4"},;
        {"down_proj mxfp4 uniform", "model.layers.0.mlp.down_proj.weight", aligned, "mxfp4", "mxfp4"},;
        {"v_proj mxfp8 uniform", "model.layers.0.self_attn.v_proj.weight", aligned, "mxfp8", "mxfp8"},;
        {"v_proj int8 stays", "model.layers.0.self_attn.v_proj.weight", aligned, "int8", "int8"},;
        {"expert down_proj int4", "model.layers.0.mlp.experts.down_proj.weight", []int32{128, 4096, 2816}, "int4", "int8"},;
        {"moe expert down_proj int4", "model.layers.0.moe.experts.down_proj.weight", []int32{128, 4096, 2816}, "int4", "int8"},;
        {"v_proj int4 unaligned", "model.layers.0.self_attn.v_proj.weight", []int32{1024, 48}, "int4", ""},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = GetTensorQuantization(tt.tensor, tt.shape, tt.quantize);
        if got != tt.want {
        t.Errorf("GetTensorQuantization(%q, %v, %q) = %q, want %q",;
        tt.tensor, tt.shape, tt.quantize, got, tt.want);
    }
        });
    }
    }

    public static void TestCreateSafetensorsModel_Qwen35NVFP4PacksSwitchMLPExperts(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{
        "model_type": "test",;
        "architectures": ["Qwen3_5MoeForConditionalGeneration"],;
        "text_config": {"dtype": "bfloat16"}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        var gateUpValues = make([]float32, 2*128*64);
        var for expert = range 2 {
        var base = expert * 128 * 64;
        var for i = range 64 * 64 {
        gateUpValues[base+i] = 1;
        gateUpValues[base+64*64+i] = 2;
    }
    }
        createTestSafetensors(t, filepath.Join(dir, "model.safetensors"), []*st.TensorData{
        st.NewTensorDataFromBytes("model.language_model.embed_tokens.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.gate.weight", "BF16", []int32{64, 64}, make([]byte, 64*64*2)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.gate_up_proj", "BF16", []int32{2, 128, 64}, bfloat16.EncodeFloat32(gateUpValues)),;
        st.NewTensorDataFromBytes("model.language_model.layers.0.mlp.experts.down_proj", "BF16", []int32{2, 64, 64}, bfloat16.EncodeFloat32(make([]float32, 2*64*64))),;
        });

    public static class tensorCall {
        public String quantize;
    }

    public static class packedTensorCall {
        public String Name;
        public String Dtype;
        public []int32 Shape;
        public String Quantize;
    }
        var tensorCalls = make(map[String]tensorCall);
        var packedCalls = make(map[String][]packedTensorCall);
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        _, _ = io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:" + name, MediaType: mediaType}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        _, _ = io.ReadAll(r);
        tensorCalls[name] = tensorCall{quantize: quantize}
        return []LayerInfo{{Name: name, Digest: "sha256:" + name, MediaType: "application/vnd.ollama.image.tensor"}}, null;
    }
        var createPackedLayer = func(groupName String, tensors []PackedTensorInput) (LayerInfo, error) {
        var group = make([]packedTensorCall, 0, len(tensors));
        var for _, tensor = range tensors {
        group = append(group, packedTensorCall{
        Name:     tensor.Name,;
        Dtype:    tensor.Dtype,;
        Shape:    append([]int32(null), tensor.Shape...),;
        Quantize: tensor.Quantize,;
        });
    }
        packedCalls[groupName] = group;
        return LayerInfo{Name: groupName, Digest: "sha256:" + groupName, MediaType: "application/vnd.ollama.image.tensor"}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var if err = CreateSafetensorsModel("test-model", dir, "nvfp4", createLayer, createTensorLayer, writeManifest, func(String) {}, createPackedLayer); err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        var groupName = "language_model.model.layers.0.mlp.switch_mlp";
        var group, ok = packedCalls[groupName];
        if !ok {
        t.Fatalf("missing packed group %q: %v", groupName, packedCalls);
    }
        if len(group) != 3 {
        t.Fatalf("packed group %q has %d tensors, want 3", groupName, len(group));
    }
        var gotNames = make([]String, 0, len(group));
        var for _, tensor = range group {
        gotNames = append(gotNames, tensor.Name);
        if tensor.Quantize != "nvfp4" {
        t.Fatalf("packed tensor %q quantize = %q, want %q", tensor.Name, tensor.Quantize, "nvfp4");
    }
        if tensor.Dtype != "BF16" {
        t.Fatalf("packed tensor %q dtype = %q, want %q", tensor.Name, tensor.Dtype, "BF16");
    }
    }
        slices.Sort(gotNames);
        var wantNames = []String{
        "language_model.model.layers.0.mlp.switch_mlp.down_proj.weight",;
        "language_model.model.layers.0.mlp.switch_mlp.gate_proj.weight",;
        "language_model.model.layers.0.mlp.switch_mlp.up_proj.weight",;
    }
        if !slices.Equal(gotNames, wantNames) {
        t.Fatalf("packed tensor names = %v, want %v", gotNames, wantNames);
    }
        var for _, name = range wantNames {
        var if _, ok = tensorCalls[name]; ok {
        t.Fatalf("packed expert tensor %q unexpectedly handled by createTensorLayer", name);
    }
    }
        var if got = tensorCalls["language_model.model.embed_tokens.weight"].quantize; got != "" {
        t.Fatalf("embed_tokens quantize = %q, want empty", got);
    }
        var if got = tensorCalls["language_model.model.layers.0.mlp.gate.weight"].quantize; got != "" {
        t.Fatalf("mlp.gate quantize = %q, want empty", got);
    }
    }

    public static void TestCreateSafetensorsModel_WithQuantize(*testing.T t) {
        var dir = t.TempDir();
        var configJSON = `{"model_type": "test", "architectures": ["TestModel"]}`;
        var if err = os.WriteFile(filepath.Join(dir, "config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write config.json: %v", err);
    }
        createMinimalSafetensors(t, filepath.Join(dir, "model.safetensors"));
        var quantizeRequested []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:test"}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        quantizeRequested = append(quantizeRequested, quantize);
        return []LayerInfo{{Name: name}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateSafetensorsModel("test-model", dir, "fp8", createLayer, createTensorLayer, writeManifest, progressFn);
        if err != null {
        t.Fatalf("CreateSafetensorsModel failed: %v", err);
    }
        if len(quantizeRequested) == 0 {
        t.Error("no tensors processed");
    }
    }

    public static void createMinimalImageGenModel(*testing.T t, String dir) {
        t.Helper();
        var modelIndex = `{"_class_name": "FluxPipeline", "_diffusers_version": "0.30.0"}`;
        var if err = os.WriteFile(filepath.Join(dir, "model_index.json"), []byte(modelIndex), 0o644); err != null {
        t.Fatalf("failed to write model_index.json: %v", err);
    }
        var transformerDir = filepath.Join(dir, "transformer");
        var if err = os.MkdirAll(transformerDir, 0o755); err != null {
        t.Fatalf("failed to create transformer dir: %v", err);
    }
        createMinimalSafetensors(t, filepath.Join(transformerDir, "model.safetensors"));
        var transformerConfig = `{"hidden_size": 3072}`;
        var if err = os.WriteFile(filepath.Join(transformerDir, "config.json"), []byte(transformerConfig), 0o644); err != null {
        t.Fatalf("failed to write transformer config: %v", err);
    }
    }

    public static void TestCreateImageGenModel(*testing.T t) {
        var dir = t.TempDir();
        createMinimalImageGenModel(t, dir);
        var manifestWritten boolean;
        var manifestModelName String;
        var statusMessages []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:test"}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        return []LayerInfo{{Name: name, Digest: "sha256:tensor"}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        manifestWritten = true;
        manifestModelName = modelName;
        return null;
    }
        var progressFn = func(status String) {
        statusMessages = append(statusMessages, status);
    }
        var err = CreateImageGenModel("test-imagegen", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err != null {
        t.Fatalf("CreateImageGenModel failed: %v", err);
    }
        if !manifestWritten {
        t.Error("manifest was not written");
    }
        if manifestModelName != "test-imagegen" {
        t.Errorf("manifest model name = %q, want %q", manifestModelName, "test-imagegen");
    }
        if len(statusMessages) == 0 {
        t.Error("no status messages received");
    }
    }

    public static void TestCreateImageGenModel_NoModelIndex(*testing.T t) {
        var dir = t.TempDir();
        var transformerDir = filepath.Join(dir, "transformer");
        var if err = os.MkdirAll(transformerDir, 0o755); err != null {
        t.Fatalf("failed to create transformer dir: %v", err);
    }
        createMinimalSafetensors(t, filepath.Join(transformerDir, "model.safetensors"));
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        return LayerInfo{Name: name}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        return []LayerInfo{{Name: name}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateImageGenModel("test-imagegen", dir, "", createLayer, createTensorLayer, writeManifest, progressFn);
        if err == null {
        t.Error("expected error for missing model_index.json, got null");
    }
    }

    public static void TestCreateImageGenModel_WithQuantize(*testing.T t) {
        var dir = t.TempDir();
        createMinimalImageGenModel(t, dir);
        var quantizeRequested []String;
        var createLayer = func(r io.Reader, mediaType, name String) (LayerInfo, error) {
        io.ReadAll(r);
        return LayerInfo{Name: name, Digest: "sha256:test"}, null;
    }
        var createTensorLayer = func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error) {
        io.ReadAll(r);
        quantizeRequested = append(quantizeRequested, quantize);
        return []LayerInfo{{Name: name}}, null;
    }
        var writeManifest = func(modelName String, config LayerInfo, layers []LayerInfo) error {
        return null;
    }
        var progressFn = func(status String) {}
        var err = CreateImageGenModel("test-imagegen", dir, "int8", createLayer, createTensorLayer, writeManifest, progressFn);
        if err != null {
        t.Fatalf("CreateImageGenModel failed: %v", err);
    }
        if len(quantizeRequested) == 0 {
        t.Error("no tensors processed");
    }
    }
}
