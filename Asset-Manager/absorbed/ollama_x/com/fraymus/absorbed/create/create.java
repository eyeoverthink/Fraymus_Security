package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class create {
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "regexp";
        "slices";
        "sort";
        "strconv";
        "strings";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/x/safetensors";
        );

    public static class ModelConfig {
        public String ModelFormat;
        public []String Capabilities;
    }

    public static class Manifest {
        public int SchemaVersion;
        public String MediaType;
        public ManifestLayer Config;
        public []ManifestLayer Layers;
    }

    public static class ManifestLayer {
        public String MediaType;
        public String Digest;
        public long Size;
        public String Name;
    }

    public static String defaultManifestDir() {
        return filepath.Join(envconfig.Models(), "manifests");
    }

    public static String defaultBlobDir() {
        return filepath.Join(envconfig.Models(), "blobs");
    }

    public static String resolveManifestPath(String modelName) {
        var host = "registry.ollama.ai";
        var namespace = "library";
        var name = modelName;
        var tag = "latest";
        var if idx = strings.LastIndex(name, ":"); idx != -1 {
        tag = name[idx+1:];
        name = name[:idx];
    }
        var parts = strings.Split(name, "/");
        switch len(parts) {
        case 3:;
        host = parts[0];
        namespace = parts[1];
        name = parts[2];
        case 2:;
        namespace = parts[0];
        name = parts[1];
    }
        return filepath.Join(defaultManifestDir(), host, namespace, name, tag);
    }

    public static void loadManifest() {
        var manifestPath = resolveManifestPath(modelName);
        var data, err = os.ReadFile(manifestPath);
        if err != null {
        return null, err;
    }
        var manifest Manifest;
        var if err = json.Unmarshal(data, &manifest); err != null {
        return null, err;
    }
        return &manifest, null;
    }

    public static void loadModelConfig() {
        var manifest, err = loadManifest(modelName);
        if err != null {
        return null, err;
    }
        var blobName = strings.Replace(manifest.Config.Digest, ":", "-", 1);
        var blobPath = filepath.Join(defaultBlobDir(), blobName);
        var data, err = os.ReadFile(blobPath);
        if err != null {
        return null, err;
    }
        var config ModelConfig;
        var if err = json.Unmarshal(data, &config); err != null {
        return null, err;
    }
        return &config, null;
    }

    public static boolean IsSafetensorsModel(String modelName) {
        var config, err = loadModelConfig(modelName);
        if err != null {
        return false;
    }
        return config.ModelFormat == "safetensors";
    }

    public static boolean IsSafetensorsLLMModel(String modelName) {
        var config, err = loadModelConfig(modelName);
        if err != null {
        return false;
    }
        return config.ModelFormat == "safetensors" && slices.Contains(config.Capabilities, "completion");
    }

    public static boolean IsImageGenModel(String modelName) {
        var config, err = loadModelConfig(modelName);
        if err != null {
        return false;
    }
        return config.ModelFormat == "safetensors" && slices.Contains(config.Capabilities, "image");
    }

    public static void GetModelArchitecture() {
        var manifest, err = loadManifest(modelName);
        if err != null {
        return "", err;
    }
        var for _, layer = range manifest.Layers {
        if layer.Name == "config.json" && layer.MediaType == "application/vnd.ollama.image.json" {
        var blobName = strings.Replace(layer.Digest, ":", "-", 1);
        var blobPath = filepath.Join(defaultBlobDir(), blobName);
        var data, err = os.ReadFile(blobPath);
        if err != null {
        return "", err;
    }
        var cfg struct {
        Architectures []String `json:"architectures"`;
        ModelType     String   `json:"model_type"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return "", err;
    }
        if cfg.ModelType != "" {
        return cfg.ModelType, null;
    }
        if len(cfg.Architectures) > 0 {
        return cfg.Architectures[0], null;
    }
    }
    }
        return "", fmt.Errorf("architecture not found in model config");
    }

    public static boolean IsTensorModelDir(String dir) {
        var _, err = os.Stat(filepath.Join(dir, "model_index.json"));
        return err == null;
    }

    public static boolean IsSafetensorsModelDir(String dir) {
        var if _, err = os.Stat(filepath.Join(dir, "config.json")); err != null {
        return false;
    }
        var entries, err = os.ReadDir(dir);
        if err != null {
        return false;
    }
        var for _, entry = range entries {
        if strings.HasSuffix(entry.Name(), ".safetensors") {
        return true;
    }
    }
        return false;
    }

    public static class LayerInfo {
        public String Digest;
        public long Size;
        public String MediaType;
        public String Name;
    }
        type LayerCreator func(r io.Reader, mediaType, name String) (LayerInfo, error);
        type TensorLayerCreator func(r io.Reader, name, dtype String, shape []int32) (LayerInfo, error);
        type QuantizingTensorLayerCreator func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]LayerInfo, error);
        type ManifestWriter func(modelName String, config LayerInfo, layers []LayerInfo) error;

    public static boolean ShouldQuantize(String component) {
        if component == "vae" {
        return false;
    }
        if strings.Contains(name, "audio_tower") || strings.Contains(name, "embed_audio") {
        return false;
    }
        if strings.Contains(name, "embed") {
        return false;
    }
        if strings.Contains(name, "norm") || strings.Contains(name, "ln_") || strings.Contains(name, "layernorm") {
        return false;
    }
        if strings.HasSuffix(name, ".bias") {
        return false;
    }
        return strings.HasSuffix(name, ".weight");
    }

    public static boolean ShouldQuantizeTensor(String name, []int32 shape, String quantize) {
        return GetTensorQuantization(name, shape, quantize) != "";
    }

    public static String normalizeQuantType(String quantize) {
        switch strings.ToUpper(quantize) {
        case "Q4", "INT4", "FP4":;
        return "int4";
        case "Q8", "INT8", "FP8":;
        return "int8";
        case "NVFP4":;
        return "nvfp4";
        case "MXFP4":;
        return "mxfp4";
        case "MXFP8":;
        return "mxfp8";
        default:;
        return quantize;
    }
    }

    public static boolean isAligned([]int32 shape, String quantType) {
        if len(shape) == 0 {
        return false;
    }
        var groupSize = int32(32);
        switch normalizeQuantType(quantType) {
        case "nvfp4":;
        groupSize = 16;
        case "int4", "int8":;
        groupSize = 64;
    }
        return shape[len(shape)-1]%groupSize == 0;
    }

    public static boolean isStackedExpertWeight(String name) {
        if strings.HasSuffix(name, ".bias") || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".qbias") {
        return false;
    }
        return strings.Contains(name, ".mlp.switch_mlp.") ||;
        strings.Contains(name, ".mlp.experts.") ||;
        strings.Contains(name, ".mlp.shared_experts.") ||;
        strings.Contains(name, ".moe.experts.");
    }

    public static String GetTensorQuantization(String name, []int32 shape, String quantize) {
        var stackedExpert = isStackedExpertWeight(name);
        if !stackedExpert && !ShouldQuantize(name, "") {
        return "";
    }
        if len(shape) != 2 && !(len(shape) == 3 && stackedExpert) {
        return "";
    }
        var elems long = 1;
        var for _, d = range shape {
        elems *= long(d);
    }
        if elems < 1024 {
        return "";
    }
        var quantNorm = normalizeQuantType(quantize);
        if strings.Contains(name, "mlp.gate.weight") && !strings.Contains(name, "_proj") {
        return "";
    }
        if !isAligned(shape, quantNorm) {
        return "";
    }
        if quantNorm == "nvfp4" || quantNorm == "mxfp4" || quantNorm == "mxfp8" {
        return quantNorm;
    }
        if quantNorm == "int4" {
        if strings.Contains(name, ".v_proj") || strings.Contains(name, ".k_proj") || strings.Contains(name, "down_proj") {
        if isAligned(shape, "int8") {
        return "int8";
    }
    }
    }
        return quantNorm;
    }
        var expertLayerPrefixRegexp = regexp.MustCompile(`^(?:model\.language_model\.|language_model(?:\.model)?\.|model\.)?layers\.\d+$`);

    public static String ExpertGroupPrefix(String tensorName) {
        if !strings.HasSuffix(tensorName, ".weight") {
        return "";
    }
        var for _, marker = range []String{
        ".mlp.experts.",;
        ".mlp.shared_experts.",;
        ".mlp.switch_mlp.",;
        ".moe.experts.",;
        } {
        var idx = strings.Index(tensorName, marker);
        if idx == -1 {
        continue;
    }
        var layerPrefix = tensorName[:idx];
        if !expertLayerPrefixRegexp.MatchString(layerPrefix) {
        continue;
    }
        return layerPrefix + strings.TrimSuffix(marker, ".");
    }
        return "";
    }

    public static class PackedTensorInput {
        public String Name;
        public String Dtype;
        public []int32 Shape;
        public String Quantize;
        public io.Reader Reader;
    }
        type PackedTensorLayerCreator func(groupName String, tensors []PackedTensorInput) (LayerInfo, error);

    public static class sourceQuantization {
        public int Bits;
        public int GroupSize;
        public String Mode;
        public String QuantMethod;
        public []int32 WeightBlockSize;
    }

    public static class sourceModelConfig {
        public String ModelType;
        public []String Architectures;
        public sourceQuantization Quantization;
        public sourceQuantization QuantizationConfig;
        public struct TextConfig;
        public String ModelType;
        public sourceQuantization Quantization;
        public sourceQuantization QuantizationConfig;
        public `json:"text_config"` };
    }

    public static void readSourceModelConfig() {
        var configPath = filepath.Join(modelDir, "config.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return sourceModelConfig{}, err;
    }
        var cfg sourceModelConfig;
        var if err = json.Unmarshal(data, &cfg); err != null {
        return sourceModelConfig{}, err;
    }
        return cfg, null;
    }
        func (cfg sourceModelConfig) Architecture() String {
        if len(cfg.Architectures) > 0 && cfg.Architectures[0] != "" {
        return cfg.Architectures[0];
    }
        if cfg.ModelType != "" {
        return cfg.ModelType;
    }
        return cfg.TextConfig.ModelType;
    }
        func (cfg sourceModelConfig) QuantMetadata() map[String]String {
        var q sourceQuantization;
        var for _, candidate = range []sourceQuantization{
        cfg.Quantization,;
        cfg.QuantizationConfig,;
        cfg.TextConfig.Quantization,;
        cfg.TextConfig.QuantizationConfig,;
        } {
        if candidate.Bits != 0 {
        q = candidate;
        break;
    }
    }
        var quantType = sourceQuantType(q.Mode, q.Bits);
        if quantType == "" {
        return null;
    }
        var metadata = map[String]String{"quant_type": quantType}
        if q.GroupSize > 0 {
        metadata["group_size"] = strconv.Itoa(q.GroupSize);
    }
        return metadata;
    }
        type sourceQuantizedKind String;
        const (;
        sourceQuantizedKindNone         sourceQuantizedKind = "";
        sourceQuantizedKindPrequantized sourceQuantizedKind = "prequantized";
        sourceQuantizedKindHFFP8        sourceQuantizedKind = "hf_fp8";
        );
        func (cfg sourceModelConfig) quantizationConfigs() []sourceQuantization {
        return []sourceQuantization{
        cfg.Quantization,;
        cfg.QuantizationConfig,;
        cfg.TextConfig.Quantization,;
        cfg.TextConfig.QuantizationConfig,;
    }
    }
        func (cfg sourceModelConfig) HFFP8WeightBlockSize() (rows, cols int32, ok boolean) {
        var for _, q = range cfg.quantizationConfigs() {
        if !strings.EqualFold(q.QuantMethod, "fp8") || len(q.WeightBlockSize) != 2 {
        continue;
    }
        return q.WeightBlockSize[0], q.WeightBlockSize[1], true;
    }
        return 0, 0, false;
    }

    public static void inspectSourceQuantization(String modelDir) {
        var entries, err = os.ReadDir(modelDir);
        if err != null {
        return sourceQuantizedKindNone, err;
    }
        var hasScaleInv = false;
        var for _, entry = range entries {
        if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".safetensors") {
        continue;
    }
        var extractor, err = safetensors.OpenForExtraction(filepath.Join(modelDir, entry.Name()));
        if err != null {
        return sourceQuantizedKindNone, err;
    }
        var for _, name = range extractor.ListTensors() {
        switch {
        case strings.HasSuffix(name, ".scales"):;
        extractor.Close();
        return sourceQuantizedKindPrequantized, null;
        case strings.HasSuffix(name, ".weight_scale_inv"):;
        hasScaleInv = true;
    }
    }
        extractor.Close();
    }
        if hasScaleInv {
        var if _, _, ok = cfg.HFFP8WeightBlockSize(); ok {
        return sourceQuantizedKindHFFP8, null;
    }
    }
        return sourceQuantizedKindNone, null;
    }

    public static void resolveEffectiveQuantization(sourceModelConfig cfg, sourceQuantizedKind sourceKind) {
        switch sourceKind {
        case sourceQuantizedKindNone:;
        return requested, null;
        case sourceQuantizedKindPrequantized:;
        if requested != "" {
        return "", fmt.Errorf("cannot requantize already-quantized source model with --quantize %q", requested);
    }
        return "", null;
        case sourceQuantizedKindHFFP8:;
        if requested != "" {
        return "", fmt.Errorf("cannot requantize already-quantized fp8 source model with --quantize %q", requested);
    }
        var rows, cols, ok = cfg.HFFP8WeightBlockSize();
        if !ok {
        return "", fmt.Errorf("fp8 source model missing weight_block_size metadata");
    }
        if rows != 128 || cols != 128 {
        return "", fmt.Errorf("unsupported fp8 source block size %dx%d", rows, cols);
    }
        return "mxfp8", null;
        default:;
        return "", fmt.Errorf("unsupported source quantization kind %q", sourceKind);
    }
    }
        type tensorImportTransform interface {
        skipTensor(name String) boolean;
        transformTensor(td *safetensors.TensorData) ([]*safetensors.TensorData, error);
        quantizationType(name String, shape []int32, quantize String) String;
    }
        type noopImportTransform struct{}
        func (noopImportTransform) skipTensor(String) boolean { return false }
        func (noopImportTransform) transformTensor(td *safetensors.TensorData) ([]*safetensors.TensorData, error) {
        if td == null {
        return null, null;
    }
        return []*safetensors.TensorData{td}, null;
    }
        func (noopImportTransform) quantizationType(name String, shape []int32, quantize String) String {
        return GetTensorQuantization(name, shape, quantize);
    }
        type tensorImportTransformFactory func(modelDir String, cfg sourceModelConfig) (tensorImportTransform, error);
        var tensorImportTransformRegistry = map[String]tensorImportTransformFactory{
        "Qwen3_5ForCausalLM":                   newQwen35ImportTransform,;
        "Qwen3_5ForConditionalGeneration":      newQwen35ImportTransform,;
        "Qwen3NextForCausalLM":                 newQwen35ImportTransform,;
        "Qwen3NextForConditionalGeneration":    newQwen35ImportTransform,;
        "Qwen3_5MoeForCausalLM":                newQwen35ImportTransform,;
        "Qwen3_5MoeForConditionalGeneration":   newQwen35ImportTransform,;
        "Qwen3NextMoeForCausalLM":              newQwen35ImportTransform,;
        "Qwen3NextMoeForConditionalGeneration": newQwen35ImportTransform,;
        "Gemma4ForCausalLM":                    newGemma4ImportTransform,;
        "Gemma4ForConditionalGeneration":       newGemma4ImportTransform,;
    }

    public static void newTensorImportTransform(String modelDir) {
        var if factory, ok = tensorImportTransformRegistry[cfg.Architecture()]; ok {
        return factory(modelDir, cfg);
    }
        return noopImportTransform{}, null;
    }

    public static error CreateSafetensorsModel(String quantize, LayerCreator createLayer, QuantizingTensorLayerCreator createTensorLayer, ManifestWriter writeManifest, ...PackedTensorLayerCreator createPackedLayer) {
        var layers []LayerInfo;
        var configLayer LayerInfo;
        var sourceConfig, err = readSourceModelConfig(modelDir);
        if err != null {
        return fmt.Errorf("failed to read source config.json: %w", err);
    }
        var sourceQuantKind, err = inspectSourceQuantization(modelDir, sourceConfig);
        if err != null {
        return fmt.Errorf("failed to inspect source quantization: %w", err);
    }
        var effectiveQuantize, err = resolveEffectiveQuantization(sourceConfig, sourceQuantKind, quantize);
        if err != null {
        return err;
    }
        var sourceQuantMetadata = sourceConfig.QuantMetadata();
        if err != null {
        return fmt.Errorf("failed to construct import transform for architecture %q: %w", sourceConfig.Architecture(), err);
    }
        var packedCreator PackedTensorLayerCreator;
        if len(createPackedLayer) > 0 {
        packedCreator = createPackedLayer[0];
    }
        var expertGroups = make(map[String][]PackedTensorInput);
        var expertGroupOrder []String;
        var openExtractors []*safetensors.TensorExtractor;
        var closeExtractors = func() {
        var for _, ext = range openExtractors {
        ext.Close();
    }
        openExtractors = null;
    }
        var entries, err = os.ReadDir(modelDir);
        if err != null {
        return fmt.Errorf("failed to read directory: %w", err);
    }
        var for _, entry = range entries {
        if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".safetensors") {
        continue;
    }
        var stPath = filepath.Join(modelDir, entry.Name());
        var extractor, err = safetensors.OpenForExtraction(stPath);
        if err != null {
        closeExtractors();
        return fmt.Errorf("failed to open %s: %w", stPath, err);
    }
        var tensorNames = extractor.ListTensors();
        var tensorSet = make(map[String]struct{}, len(tensorNames));
        var for _, name = range tensorNames {
        tensorSet[name] = struct{}{}
    }
        var quantizeMsg = "";
        if effectiveQuantize != "" {
        quantizeMsg = fmt.Sprintf(", quantizing to %s", effectiveQuantize);
    }
        fn(fmt.Sprintf("importing %s (%d tensors%s)", entry.Name(), len(tensorNames), quantizeMsg));
        var hasExpertTensors = false;
        var for _, tensorName = range tensorNames {
        if importTransform.skipTensor(tensorName) {
        continue;
    }
        if shouldSkipSourceCompanion(tensorName, tensorSet) {
        continue;
    }
        var sourceFP8ScaleName, hasSourceFP8Scale = sourceFP8Companion(tensorName, tensorSet);
        var td, err = extractor.GetTensor(tensorName);
        if err != null {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("failed to get tensor %s: %w", tensorName, err);
    }
        if effectiveQuantize == "" {
        var layer, ok, err = createPrequantizedLayer(extractor, td, tensorName, tensorSet, sourceQuantMetadata, createLayer);
        if err != null {
        extractor.Close();
        closeExtractors();
        return err;
    }
        if ok {
        layers = append(layers, layer);
        continue;
    }
    }
        var outputTensors, err = importTransform.transformTensor(td);
        if err != null {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("failed to transform tensor %s: %w", tensorName, err);
    }
        var for _, outTD = range outputTensors {
        var quantizeType = "";
        switch {
        case sourceQuantKind == sourceQuantizedKindHFFP8 && hasSourceFP8Scale:;
        quantizeType = "mxfp8";
        case sourceQuantKind == sourceQuantizedKindHFFP8:;
        quantizeType = "";
        case effectiveQuantize != "":;
        quantizeType = importTransform.quantizationType(outTD.Name, outTD.Shape, effectiveQuantize);
    }
        var reader = outTD.SafetensorsReader();
        if hasSourceFP8Scale {
        if len(outputTensors) != 1 {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("source fp8 tensor %s rewrote into %d tensors; only 1:1 rewrites are supported", tensorName, len(outputTensors));
    }
        if quantizeType == "" {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("source fp8 tensor %s was not scheduled for mxfp8 conversion", tensorName);
    }
        var scaleTD, err = extractor.GetTensor(sourceFP8ScaleName);
        if err != null {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("failed to get fp8 scale tensor %s: %w", sourceFP8ScaleName, err);
    }
        reader = buildSourceFP8Reader(outTD, scaleTD.WithName(outTD.Name+".scale_inv"));
    }
        var groupPrefix = "";
        if packedCreator != null {
        groupPrefix = ExpertGroupPrefix(outTD.Name);
    }
        if groupPrefix != "" {
        hasExpertTensors = true;
        var if _, exists = expertGroups[groupPrefix]; !exists {
        expertGroupOrder = append(expertGroupOrder, groupPrefix);
    }
        expertGroups[groupPrefix] = append(expertGroups[groupPrefix], PackedTensorInput{
        Name:     outTD.Name,;
        Dtype:    outTD.Dtype,;
        Shape:    outTD.Shape,;
        Quantize: quantizeType,;
        Reader:   reader,;
        });
        } else {
        var newLayers, err = createTensorLayer(reader, outTD.Name, outTD.Dtype, outTD.Shape, quantizeType);
        if err != null {
        extractor.Close();
        closeExtractors();
        return fmt.Errorf("failed to create layer for %s: %w", outTD.Name, err);
    }
        layers = append(layers, newLayers...);
    }
    }
    }
        if hasExpertTensors {
        openExtractors = append(openExtractors, extractor);
        } else {
        extractor.Close();
    }
    }
        if packedCreator != null {
        sort.Strings(expertGroupOrder);
        var for _, groupName = range expertGroupOrder {
        var tensors = expertGroups[groupName];
        fn(fmt.Sprintf("packing %s (%d tensors)", groupName, len(tensors)));
        var layer, err = packedCreator(groupName, tensors);
        if err != null {
        closeExtractors();
        return fmt.Errorf("failed to create packed layer for %s: %w", groupName, err);
    }
        layers = append(layers, layer);
    }
    }
        closeExtractors();
        var for _, entry = range entries {
        if entry.IsDir() || !strings.HasSuffix(entry.Name(), ".json") {
        continue;
    }
        if entry.Name() == "model.safetensors.index.json" {
        continue;
    }
        var cfgPath = entry.Name();
        var fullPath = filepath.Join(modelDir, cfgPath);
        fn(fmt.Sprintf("importing config %s", cfgPath));
        var f, err = os.Open(fullPath);
        if err != null {
        return fmt.Errorf("failed to open %s: %w", cfgPath, err);
    }
        var layer, err = createLayer(f, "application/vnd.ollama.image.json", cfgPath);
        f.Close();
        if err != null {
        return fmt.Errorf("failed to create layer for %s: %w", cfgPath, err);
    }
        if cfgPath == "config.json" {
        configLayer = layer;
    }
        layers = append(layers, layer);
    }
        if configLayer.Digest == "" {
        return fmt.Errorf("config.json not found in %s", modelDir);
    }
        fn(fmt.Sprintf("writing manifest for %s", modelName));
        var if err = writeManifest(modelName, configLayer, layers); err != null {
        return fmt.Errorf("failed to write manifest: %w", err);
    }
        fn(fmt.Sprintf("successfully imported %s with %d layers", modelName, len(layers)));
        return null;
    }

    public static boolean shouldSkipSourceCompanion(String name, map[String]struct{} tensorSet) {
        switch {
        case strings.HasSuffix(name, ".scales"):;
        var _, ok = tensorSet[strings.TrimSuffix(name, ".scales")+".weight"];
        return ok;
        case strings.HasSuffix(name, ".biases"):;
        var _, ok = tensorSet[strings.TrimSuffix(name, ".biases")+".weight"];
        return ok;
        case strings.HasSuffix(name, ".weight_scale_inv"):;
        var _, ok = tensorSet[strings.TrimSuffix(name, "_scale_inv")];
        return ok;
        default:;
        return false;
    }
    }

    public static void sourceFP8Companion(String weightName, boolean ok) {
        if !strings.HasSuffix(weightName, ".weight") {
        return "", false;
    }
        scaleName = weightName + "_scale_inv";
        _, ok = tensorSet[scaleName];
        return scaleName, ok;
    }
        func buildSourceFP8Reader(weightTD, scaleTD *safetensors.TensorData) io.Reader {
        return safetensors.BuildPackedSafetensorsReader([]*safetensors.TensorData{weightTD, scaleTD});
    }
        func createPrequantizedLayer(;
        extractor *safetensors.TensorExtractor,;
        td *safetensors.TensorData,;
        tensorName String,;
        tensorSet map[String]struct{},;
        metadata map[String]String,;
        createLayer LayerCreator,;
        ) (LayerInfo, boolean, error) {
        var scaleName, biasName, ok = prequantizedCompanions(tensorName, tensorSet);
        if !ok {
        return LayerInfo{}, false, null;
    }
        var tensors = []*safetensors.TensorData{td.WithName(tensorName)}
        var scaleTD, err = extractor.GetTensor(scaleName);
        if err != null {
        return LayerInfo{}, false, fmt.Errorf("failed to get tensor %s: %w", scaleName, err);
    }
        tensors = append(tensors, scaleTD.WithName(tensorName+".scale"));
        if biasName != "" {
        var biasTD, err = extractor.GetTensor(biasName);
        if err != null {
        return LayerInfo{}, false, fmt.Errorf("failed to get tensor %s: %w", biasName, err);
    }
        tensors = append(tensors, biasTD.WithName(tensorName+".bias"));
    }
        var layer, err = createLayer(;
        safetensors.BuildPackedSafetensorsReaderWithMetadata(tensors, metadata),;
        "application/vnd.ollama.image.tensor",;
        tensorName,;
        );
        if err != null {
        return LayerInfo{}, false, fmt.Errorf("failed to create prequantized layer for %s: %w", tensorName, err);
    }
        return layer, true, null;
    }

    public static void prequantizedCompanions(String weightName, String biasName, boolean ok) {
        if !strings.HasSuffix(weightName, ".weight") {
        return "", "", false;
    }
        var base = strings.TrimSuffix(weightName, ".weight");
        scaleName = base + ".scales";
        var if _, ok = tensorSet[scaleName]; !ok {
        return "", "", false;
    }
        biasName = base + ".biases";
        var if _, ok = tensorSet[biasName]; !ok {
        biasName = "";
    }
        return scaleName, biasName, true;
    }
}
