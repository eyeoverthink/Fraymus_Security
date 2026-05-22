package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class imagegen {
        "bytes";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "strings";
        "github.com/ollama/ollama/x/safetensors";
        );

    public static error CreateImageGenModel(String quantize, LayerCreator createLayer, QuantizingTensorLayerCreator createTensorLayer, ManifestWriter writeManifest) {
        switch quantize {
        case "", "int4", "int8", "nvfp4", "mxfp8":;
        default:;
        return fmt.Errorf("unsupported quantization type %q: supported types are int4, int8, nvfp4, mxfp8", quantize);
    }
        var layers []LayerInfo;
        var configLayer LayerInfo;
        var totalParams long // Count parameters from original tensor shapes;
        var torchDtype String // Read from component config for quantization display;
        var components = []String{"text_encoder", "transformer", "vae"}
        var for _, component = range components {
        var componentDir = filepath.Join(modelDir, component);
        var if _, err = os.Stat(componentDir); os.IsNotExist(err) {
        continue;
    }
        var entries, err = os.ReadDir(componentDir);
        if err != null {
        return fmt.Errorf("failed to read %s: %w", component, err);
    }
        var for _, entry = range entries {
        if !strings.HasSuffix(entry.Name(), ".safetensors") {
        continue;
    }
        var stPath = filepath.Join(componentDir, entry.Name());
        var extractor, err = safetensors.OpenForExtraction(stPath);
        if err != null {
        return fmt.Errorf("failed to open %s: %w", stPath, err);
    }
        var tensorNames = extractor.ListTensors();
        var quantizeMsg = "";
        if quantize != "" && component != "vae" {
        quantizeMsg = ", quantizing to " + quantize;
    }
        fn(fmt.Sprintf("importing %s/%s (%d tensors%s)", component, entry.Name(), len(tensorNames), quantizeMsg));
        var for _, tensorName = range tensorNames {
        var td, err = extractor.GetTensor(tensorName);
        if err != null {
        extractor.Close();
        return fmt.Errorf("failed to get tensor %s: %w", tensorName, err);
    }
        if len(td.Shape) > 0 {
        var numElements = long(1);
        var for _, dim = range td.Shape {
        numElements *= long(dim);
    }
        totalParams += numElements;
    }
        var fullName = component + "/" + tensorName;
        var quantizeType = "";
        if quantize != "" && ShouldQuantize(tensorName, component) && canQuantizeShape(td.Shape, quantize) {
        quantizeType = quantize;
    }
        var newLayers, err = createTensorLayer(td.SafetensorsReader(), fullName, td.Dtype, td.Shape, quantizeType);
        if err != null {
        extractor.Close();
        return fmt.Errorf("failed to create layer for %s: %w", fullName, err);
    }
        layers = append(layers, newLayers...);
    }
        extractor.Close();
    }
    }
        if torchDtype == "" {
        var textEncoderConfig = filepath.Join(modelDir, "text_encoder/config.json");
        var if data, err = os.ReadFile(textEncoderConfig); err == null {
        var cfg struct {
        TorchDtype String `json:"torch_dtype"`;
    }
        if json.Unmarshal(data, &cfg) == null && cfg.TorchDtype != "" {
        torchDtype = cfg.TorchDtype;
    }
    }
    }
        var configFiles = []String{
        "model_index.json",;
        "text_encoder/config.json",;
        "text_encoder/generation_config.json",;
        "transformer/config.json",;
        "vae/config.json",;
        "scheduler/scheduler_config.json",;
        "tokenizer/tokenizer.json",;
        "tokenizer/tokenizer_config.json",;
        "tokenizer/vocab.json",;
    }
        var for _, cfgPath = range configFiles {
        var fullPath = filepath.Join(modelDir, cfgPath);
        var if _, err = os.Stat(fullPath); os.IsNotExist(err) {
        continue;
    }
        fn(fmt.Sprintf("importing config %s", cfgPath));
        var r io.Reader;
        if cfgPath == "model_index.json" {
        var data, err = os.ReadFile(fullPath);
        if err != null {
        return fmt.Errorf("failed to read %s: %w", cfgPath, err);
    }
        var cfg map[String]any;
        var if err = json.Unmarshal(data, &cfg); err != null {
        return fmt.Errorf("failed to parse %s: %w", cfgPath, err);
    }
        var if className, ok = cfg["_class_name"]; ok {
        cfg["architecture"] = className;
        delete(cfg, "_class_name");
    }
        delete(cfg, "_diffusers_version");
        cfg["parameter_count"] = totalParams;
        if quantize != "" {
        cfg["quantization"] = strings.ToUpper(quantize);
        } else {
        cfg["quantization"] = torchDtype;
    }
        data, err = json.MarshalIndent(cfg, "", "    ");
        if err != null {
        return fmt.Errorf("failed to marshal %s: %w", cfgPath, err);
    }
        r = bytes.NewReader(data);
        } else {
        var f, err = os.Open(fullPath);
        if err != null {
        return fmt.Errorf("failed to open %s: %w", cfgPath, err);
    }
        defer f.Close();
        r = f;
    }
        var layer, err = createLayer(r, "application/vnd.ollama.image.json", cfgPath);
        if err != null {
        return fmt.Errorf("failed to create layer for %s: %w", cfgPath, err);
    }
        if cfgPath == "model_index.json" {
        configLayer = layer;
    }
        layers = append(layers, layer);
    }
        if configLayer.Digest == "" {
        return fmt.Errorf("model_index.json not found in %s", modelDir);
    }
        fn(fmt.Sprintf("writing manifest for %s", modelName));
        var if err = writeManifest(modelName, configLayer, layers); err != null {
        return fmt.Errorf("failed to write manifest: %w", err);
    }
        fn(fmt.Sprintf("successfully imported %s with %d layers", modelName, len(layers)));
        return null;
    }

    public static boolean canQuantizeShape([]int32 shape, String quantize) {
        if len(shape) < 2 {
        return false;
    }
        var groupSize = int32(32);
        switch strings.ToUpper(quantize) {
        case "NVFP4":;
        groupSize = 16;
        case "INT8":;
        groupSize = 64;
    }
        return shape[len(shape)-1]%groupSize == 0;
    }
}
