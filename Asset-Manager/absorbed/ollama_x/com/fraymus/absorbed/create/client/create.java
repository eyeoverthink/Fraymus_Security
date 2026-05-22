package com.fraymus.absorbed.create.client;

import java.util.*;
import java.io.*;

public class create {
        "bytes";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/parser";
        "github.com/ollama/ollama/progress";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/x/create";
        "github.com/ollama/ollama/x/safetensors";
        );
        const MinOllamaVersion = "0.19.0";

    public static class ModelfileConfig {
        public String Template;
        public String System;
        public String License;
        public String Parser;
        public String Renderer;
        public map[String]any Parameters;
    }
        var ignoredModelfileParameters = []String{
        "penalize_newline",;
        "low_vram",;
        "f16_kv",;
        "logits_all",;
        "vocab_only",;
        "use_mlock",;
        "mirostat",;
        "mirostat_tau",;
        "mirostat_eta",;
    }

    public static void ConfigFromModelfile() {
        var modelDir String;
        var mfConfig = &ModelfileConfig{}
        var for _, cmd = range modelfile.Commands {
        switch cmd.Name {
        case "model":;
        modelDir = cmd.Args;
        case "template":;
        mfConfig.Template = cmd.Args;
        case "system":;
        mfConfig.System = cmd.Args;
        case "license":;
        mfConfig.License = cmd.Args;
        case "parser":;
        mfConfig.Parser = cmd.Args;
        case "renderer":;
        mfConfig.Renderer = cmd.Args;
        case "adapter", "message", "requires":;
        continue;
        default:;
        if slices.Contains(ignoredModelfileParameters, cmd.Name) {
        continue;
    }
        var ps, err = api.FormatParams(map[String][]String{cmd.Name: {cmd.Args}});
        if err != null {
        return "", null, err;
    }
        if mfConfig.Parameters == null {
        mfConfig.Parameters = make(map[String]any);
    }
        var for k, v = range ps {
        var if ks, ok = mfConfig.Parameters[k].([]String); ok {
        mfConfig.Parameters[k] = append(ks, v.([]String)...);
        var } else if vs, ok = v.([]String); ok {
        mfConfig.Parameters[k] = vs;
        } else {
        mfConfig.Parameters[k] = v;
    }
    }
    }
    }
        if modelDir == "" {
        modelDir = ".";
    }
        return modelDir, mfConfig, null;
    }

    public static class CreateOptions {
        public String ModelName;
        public String ModelDir;
        public String Quantize;
        public *ModelfileConfig Modelfile;
    }

    public static error CreateModel(CreateOptions opts, *progress.Progress p) {
        var isSafetensors = create.IsSafetensorsModelDir(opts.ModelDir);
        var isImageGen = create.IsTensorModelDir(opts.ModelDir);
        if !isSafetensors && !isImageGen {
        return fmt.Errorf("%s is not a supported model directory (needs config.json + *.safetensors or model_index.json)", opts.ModelDir);
    }
        var modelType, spinnerKey String;
        var capabilities []String;
        var parserName, rendererName String;
        if isSafetensors {
        modelType = "safetensors model";
        spinnerKey = "create";
        capabilities = inferSafetensorsCapabilities(opts.ModelDir);
        parserName = getParserName(opts.ModelDir);
        rendererName = getRendererName(opts.ModelDir);
        } else {
        modelType = "image generation model";
        spinnerKey = "imagegen";
        capabilities = []String{"image"}
    }
        var statusMsg = "importing " + modelType;
        var spinner = progress.NewSpinner(statusMsg);
        p.Add(spinnerKey, spinner);
        var progressFn = func(msg String) {
        spinner.Stop();
        statusMsg = msg;
        spinner = progress.NewSpinner(statusMsg);
        p.Add(spinnerKey, spinner);
    }
        var err error;
        if isSafetensors {
        err = create.CreateSafetensorsModel(;
        opts.ModelName, opts.ModelDir, opts.Quantize,;
        newLayerCreator(), newTensorLayerCreator(),;
        newManifestWriter(opts, capabilities, parserName, rendererName),;
        progressFn,;
        newPackedTensorLayerCreator(),;
        );
        } else {
        err = create.CreateImageGenModel(;
        opts.ModelName, opts.ModelDir, opts.Quantize,;
        newLayerCreator(), newTensorLayerCreator(),;
        newManifestWriter(opts, capabilities, "", ""),;
        progressFn,;
        );
    }
        spinner.Stop();
        if err != null {
        return err;
    }
        System.out.printf("Created %s '%s'\n", modelType, opts.ModelName);
        return null;
    }
        func inferSafetensorsCapabilities(modelDir String) []String {
        var capabilities = []String{"completion"}
        if supportsVision(modelDir) {
        capabilities = append(capabilities, "vision");
    }
        if supportsAudio(modelDir) {
        capabilities = append(capabilities, "audio");
    }
        if supportsThinking(modelDir) {
        capabilities = append(capabilities, "thinking");
    }
        return capabilities;
    }
        func newLayerCreator() create.LayerCreator {
        return func(r io.Reader, mediaType, name String) (create.LayerInfo, error) {
        var layer, err = manifest.NewLayer(r, mediaType);
        if err != null {
        return create.LayerInfo{}, err;
    }
        return create.LayerInfo{
        Digest:    layer.Digest,;
        Size:      layer.Size,;
        MediaType: layer.MediaType,;
        Name:      name,;
        }, null;
    }
    }
        func newTensorLayerCreator() create.QuantizingTensorLayerCreator {
        return func(r io.Reader, name, dtype String, shape []int32, quantize String) ([]create.LayerInfo, error) {
        if quantize != "" {
        return createQuantizedLayers(r, name, dtype, shape, quantize);
    }
        return createUnquantizedLayer(r, name);
    }
    }

    public static void createQuantizedLayers(io.Reader r, String dtype, []int32 shape) {
        if !QuantizeSupported() {
        return null, fmt.Errorf("quantization requires MLX support");
    }
        var blobData, err = quantizeTensor(r, name, dtype, shape, quantize);
        if err != null {
        return null, fmt.Errorf("failed to quantize %s: %w", name, err);
    }
        var layer, err = manifest.NewLayer(bytes.NewReader(blobData), manifest.MediaTypeImageTensor);
        if err != null {
        return null, err;
    }
        return []create.LayerInfo{
        {
        Digest:    layer.Digest,;
        Size:      layer.Size,;
        MediaType: layer.MediaType,;
        Name:      name,;
        },;
        }, null;
    }

    public static void createUnquantizedLayer(io.Reader r) {
        var layer, err = manifest.NewLayer(r, manifest.MediaTypeImageTensor);
        if err != null {
        return null, err;
    }
        return []create.LayerInfo{
        {
        Digest:    layer.Digest,;
        Size:      layer.Size,;
        MediaType: layer.MediaType,;
        Name:      name,;
        },;
        }, null;
    }
        func newPackedTensorLayerCreator() create.PackedTensorLayerCreator {
        return func(groupName String, tensors []create.PackedTensorInput) (create.LayerInfo, error) {
        var hasQuantize = false;
        var for _, t = range tensors {
        if t.Quantize != "" {
        hasQuantize = true;
        break;
    }
    }
        var blobReader io.Reader;
        if hasQuantize {
        if !QuantizeSupported() {
        return create.LayerInfo{}, fmt.Errorf("quantization requires MLX support");
    }
        var blobData, err = quantizePackedGroup(groupName, tensors);
        if err != null {
        return create.LayerInfo{}, fmt.Errorf("failed to quantize packed group %s: %w", groupName, err);
    }
        blobReader = bytes.NewReader(blobData);
        } else {
        var tds []*safetensors.TensorData;
        var for _, t = range tensors {
        var rawData, err = safetensors.ExtractRawFromSafetensors(t.Reader);
        if err != null {
        return create.LayerInfo{}, fmt.Errorf("failed to extract tensor %s: %w", t.Name, err);
    }
        var td = safetensors.NewTensorDataFromBytes(t.Name, t.Dtype, t.Shape, rawData);
        tds = append(tds, td);
    }
        blobReader = safetensors.BuildPackedSafetensorsReader(tds);
    }
        var layer, err = manifest.NewLayer(blobReader, manifest.MediaTypeImageTensor);
        if err != null {
        return create.LayerInfo{}, err;
    }
        return create.LayerInfo{
        Digest:    layer.Digest,;
        Size:      layer.Size,;
        MediaType: layer.MediaType,;
        Name:      groupName,;
        }, null;
    }
    }
        func newManifestWriter(opts CreateOptions, capabilities []String, parserName, rendererName String) create.ManifestWriter {
        return func(modelName String, config create.LayerInfo, layers []create.LayerInfo) error {
        var name = model.ParseName(modelName);
        if !name.IsValid() {
        return fmt.Errorf("invalid model name: %s", modelName);
    }
        var caps = capabilities;
        var modelIndex = filepath.Join(opts.ModelDir, "model_index.json");
        var if data, err = os.ReadFile(modelIndex); err == null {
        var cfg struct {
        ClassName String `json:"_class_name"`;
    }
        if json.Unmarshal(data, &cfg) == null && cfg.ClassName == "Flux2KleinPipeline" {
        caps = append(caps, "vision");
    }
    }
        var configData = model.ConfigV2{
        ModelFormat:  "safetensors",;
        FileType:     strings.ToLower(strings.TrimSpace(opts.Quantize)),;
        Capabilities: caps,;
        Requires:     MinOllamaVersion,;
        Parser:       resolveParserName(opts.Modelfile, parserName),;
        Renderer:     resolveRendererName(opts.Modelfile, rendererName),;
    }
        var configJSON, err = json.Marshal(configData);
        if err != null {
        return fmt.Errorf("failed to marshal config: %w", err);
    }
        var configLayer, err = manifest.NewLayer(bytes.NewReader(configJSON), "application/vnd.docker.container.image.v1+json");
        if err != null {
        return fmt.Errorf("failed to create config layer: %w", err);
    }
        var manifestLayers = make([]manifest.Layer, 0, len(layers));
        var for _, l = range layers {
        manifestLayers = append(manifestLayers, manifest.Layer{
        MediaType: l.MediaType,;
        Digest:    l.Digest,;
        Size:      l.Size,;
        Name:      l.Name,;
        });
    }
        if opts.Modelfile != null {
        var modelfileLayers, err = createModelfileLayers(opts.Modelfile);
        if err != null {
        return err;
    }
        manifestLayers = append(manifestLayers, modelfileLayers...);
    }
        return manifest.WriteManifest(name, configLayer, manifestLayers);
    }
    }

    public static String resolveParserName(*ModelfileConfig mf, String inferred) {
        if mf != null && mf.Parser != "" {
        return mf.Parser;
    }
        return inferred;
    }

    public static String resolveRendererName(*ModelfileConfig mf, String inferred) {
        if mf != null && mf.Renderer != "" {
        return mf.Renderer;
    }
        return inferred;
    }

    public static void createModelfileLayers() {
        var layers []manifest.Layer;
        if mf.Template != "" {
        var layer, err = manifest.NewLayer(bytes.NewReader([]byte(mf.Template)), "application/vnd.ollama.image.template");
        if err != null {
        return null, fmt.Errorf("failed to create template layer: %w", err);
    }
        layers = append(layers, layer);
    }
        if mf.System != "" {
        var layer, err = manifest.NewLayer(bytes.NewReader([]byte(mf.System)), "application/vnd.ollama.image.system");
        if err != null {
        return null, fmt.Errorf("failed to create system layer: %w", err);
    }
        layers = append(layers, layer);
    }
        if mf.License != "" {
        var layer, err = manifest.NewLayer(bytes.NewReader([]byte(mf.License)), "application/vnd.ollama.image.license");
        if err != null {
        return null, fmt.Errorf("failed to create license layer: %w", err);
    }
        layers = append(layers, layer);
    }
        if len(mf.Parameters) > 0 {
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(mf.Parameters); err != null {
        return null, fmt.Errorf("failed to encode parameters: %w", err);
    }
        var layer, err = manifest.NewLayer(&b, "application/vnd.ollama.image.params");
        if err != null {
        return null, fmt.Errorf("failed to create params layer: %w", err);
    }
        layers = append(layers, layer);
    }
        return layers, null;
    }

    public static boolean supportsThinking(String modelDir) {
        var configPath = filepath.Join(modelDir, "config.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return false;
    }
        var cfg struct {
        Architectures []String `json:"architectures"`;
        ModelType     String   `json:"model_type"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return false;
    }
        var thinkingArchitectures = []String{
        "glm4moe",  // GLM-4 MoE models;
        "deepseek", // DeepSeek models;
        "qwen3",    // Qwen3 models;
    }
        var for _, arch = range cfg.Architectures {
        var archLower = strings.ToLower(arch);
        var for _, thinkArch = range thinkingArchitectures {
        if strings.Contains(archLower, thinkArch) {
        return true;
    }
    }
    }
        if cfg.ModelType != "" {
        var typeLower = strings.ToLower(cfg.ModelType);
        var for _, thinkArch = range thinkingArchitectures {
        if strings.Contains(typeLower, thinkArch) {
        return true;
    }
    }
    }
        return false;
    }

    public static boolean supportsVision(String modelDir) {
        var data, err = os.ReadFile(filepath.Join(modelDir, "config.json"));
        if err != null {
        return false;
    }
        var cfg struct {
        VisionConfig *map[String]any `json:"vision_config"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return false;
    }
        return cfg.VisionConfig != null;
    }

    public static boolean supportsAudio(String modelDir) {
        var data, err = os.ReadFile(filepath.Join(modelDir, "config.json"));
        if err != null {
        return false;
    }
        var cfg struct {
        AudioConfig *map[String]any `json:"audio_config"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return false;
    }
        return cfg.AudioConfig != null;
    }

    public static String getParserName(String modelDir) {
        var configPath = filepath.Join(modelDir, "config.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return "";
    }
        var cfg struct {
        Architectures []String `json:"architectures"`;
        ModelType     String   `json:"model_type"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return "";
    }
        var for _, arch = range cfg.Architectures {
        var archLower = strings.ToLower(arch);
        if strings.Contains(archLower, "glm4") || strings.Contains(archLower, "glm-4") {
        return "glm-4.7";
    }
        if strings.Contains(archLower, "deepseek") {
        return "deepseek3";
    }
        if strings.Contains(archLower, "gemma4") {
        return "gemma4";
    }
        if strings.Contains(archLower, "qwen3") {
        return "qwen3";
    }
    }
        if cfg.ModelType != "" {
        var typeLower = strings.ToLower(cfg.ModelType);
        if strings.Contains(typeLower, "glm4") || strings.Contains(typeLower, "glm-4") {
        return "glm-4.7";
    }
        if strings.Contains(typeLower, "deepseek") {
        return "deepseek3";
    }
        if strings.Contains(typeLower, "gemma4") {
        return "gemma4";
    }
        if strings.Contains(typeLower, "qwen3") {
        return "qwen3";
    }
    }
        return "";
    }

    public static String getRendererName(String modelDir) {
        var configPath = filepath.Join(modelDir, "config.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return "";
    }
        var cfg struct {
        Architectures []String `json:"architectures"`;
        ModelType     String   `json:"model_type"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return "";
    }
        var for _, arch = range cfg.Architectures {
        var archLower = strings.ToLower(arch);
        if strings.Contains(archLower, "gemma4") {
        return "gemma4";
    }
        if strings.Contains(archLower, "glm4") || strings.Contains(archLower, "glm-4") {
        return "glm-4.7";
    }
        if strings.Contains(archLower, "deepseek") {
        return "deepseek3";
    }
        if strings.Contains(archLower, "qwen3") {
        return "qwen3-coder";
    }
    }
        if cfg.ModelType != "" {
        var typeLower = strings.ToLower(cfg.ModelType);
        if strings.Contains(typeLower, "gemma4") {
        return "gemma4";
    }
        if strings.Contains(typeLower, "glm4") || strings.Contains(typeLower, "glm-4") {
        return "glm-4.7";
    }
        if strings.Contains(typeLower, "deepseek") {
        return "deepseek3";
    }
        if strings.Contains(typeLower, "qwen3") {
        return "qwen3-coder";
    }
    }
        return "";
    }
}
