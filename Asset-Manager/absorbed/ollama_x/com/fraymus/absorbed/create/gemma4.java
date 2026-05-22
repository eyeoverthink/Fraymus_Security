package com.fraymus.absorbed.create;

import java.util.*;
import java.io.*;

public class gemma4 {
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "regexp";
        "strconv";
        "strings";
        "github.com/ollama/ollama/x/safetensors";
        );

    public static class gemma4ImportTransform {
        public int numLayers;
        public int numExperts;
    }

    public static class gemma4Config {
        public int NumHiddenLayers;
        public int NumExperts;
        public struct TextConfig;
        public int NumHiddenLayers;
        public int NumExperts;
        public `json:"text_config"` };
    }

    public static void newGemma4ImportTransform(String modelDir) {
        var data, err = os.ReadFile(filepath.Join(modelDir, "config.json"));
        if err != null {
        return gemma4ImportTransform{}, null //nolint:nilerr // fallback to no heuristic;
    }
        var cfg gemma4Config;
        var if err = json.Unmarshal(data, &cfg); err != null {
        return gemma4ImportTransform{}, null //nolint:nilerr // fallback to no heuristic;
    }
        var numLayers = cfg.NumHiddenLayers;
        if numLayers == 0 {
        numLayers = cfg.TextConfig.NumHiddenLayers;
    }
        var numExperts = cfg.NumExperts;
        if numExperts == 0 {
        numExperts = cfg.TextConfig.NumExperts;
    }
        return gemma4ImportTransform{numLayers: numLayers, numExperts: numExperts}, null;
    }
        func (t gemma4ImportTransform) skipTensor(name String) boolean {
        return false;
    }
        var layerIndexRe = regexp.MustCompile(`\.layers\.(\d+)\.`);

    public static boolean useMoreBits(int numLayers) {
        return layerIdx < numLayers/8 ||;
        layerIdx >= 7*numLayers/8 ||;
        (layerIdx-numLayers/8)%3 == 2;
    }
        func (t gemma4ImportTransform) quantizationType(name String, shape []int32, quantize String) String {
        var quantNorm = normalizeQuantType(quantize);
        if isEmbedTokensWeight(name) {
        switch quantNorm {
        case "int4", "int8":;
        if isAligned(shape, "int8") {
        return "int8";
    }
        case "mxfp4", "nvfp4", "mxfp8":;
        if isAligned(shape, "mxfp8") {
        return "mxfp8";
    }
    }
        if isAligned(shape, quantNorm) {
        return quantNorm;
    }
        return "";
    }
        if isGemma4RouterProjection(name) {
        return "";
    }
        if t.numLayers > 0 {
        var layerIdx = -1;
        var if m = layerIndexRe.FindStringSubmatch(name); m != null {
        var if idx, err = strconv.Atoi(m[1]); err == null {
        layerIdx = idx;
    }
    }
        var promote = "";
        switch quantNorm {
        case "int4":;
        promote = "int8";
        case "mxfp4", "nvfp4":;
        promote = "mxfp8";
    }
        var isModelTensor = !strings.Contains(name, "audio_tower") &&;
        !strings.Contains(name, "vision_tower");
        var isSensitive = isModelTensor &&;
        (strings.Contains(name, ".v_proj") || strings.Contains(name, "down_proj"));
        var isSensitiveK = isModelTensor && strings.Contains(name, "k_proj");
        if promote != "" && (isSensitive || isSensitiveK) {
        var shouldPromote = false;
        if t.numExperts == 8 && (strings.Contains(name, ".v_proj") || isSensitiveK) {
        shouldPromote = true;
    }
        if isSensitive && layerIdx >= 0 && useMoreBits(layerIdx, t.numLayers) {
        shouldPromote = true;
    }
        if shouldPromote && isAligned(shape, promote) {
        return promote;
    }
        if !isAligned(shape, quantNorm) {
        return "";
    }
        return quantNorm;
    }
    }
        return GetTensorQuantization(name, shape, quantize);
    }

    public static boolean isEmbedTokensWeight(String name) {
        return strings.HasSuffix(name, "embed_tokens.weight") &&;
        !strings.Contains(name, "per_layer");
    }

    public static boolean isGemma4RouterProjection(String name) {
        return strings.HasSuffix(name, ".router.proj.weight") &&;
        !strings.Contains(name, "audio_tower") &&;
        !strings.Contains(name, "vision_tower");
    }
        func (t gemma4ImportTransform) transformTensor(td *safetensors.TensorData) ([]*safetensors.TensorData, error) {
        if td == null {
        return null, null;
    }
        if isGemma4StackedMoETensor(td.Name, td.Shape) {
        return splitStackedMoETensor(td);
    }
        return []*safetensors.TensorData{td}, null;
    }

    public static boolean isGemma4StackedMoETensor(String name, []int32 shape) {
        if len(shape) != 3 {
        return false;
    }
        if strings.Contains(name, ".moe.") || strings.Contains(name, ".experts.") {
        return strings.HasSuffix(name, "_proj") || strings.HasSuffix(name, "_proj.weight");
    }
        return false;
    }

    public static void splitStackedMoETensor() {
        var raw, err = io.ReadAll(td.Reader());
        if err != null {
        return null, fmt.Errorf("failed to read tensor %s: %w", td.Name, err);
    }
        var numExperts = int(td.Shape[0]);
        var rows = int(td.Shape[1]) // out_features in HF layout;
        var cols = int(td.Shape[2]) // in_features in HF layout;
        var elemSize, err = DTypeSize(td.Dtype);
        if err != null {
        return null, fmt.Errorf("failed to get dtype size for %s: %w", td.Dtype, err);
    }
        var perExpertBytes = rows * cols * elemSize;
        if len(raw) != numExperts*perExpertBytes {
        return null, fmt.Errorf("tensor %s: raw byte length %d does not match shape %v and dtype %s",;
        td.Name, len(raw), td.Shape, td.Dtype);
    }
        var baseName = td.Name;
        baseName = strings.TrimSuffix(baseName, ".weight");
        var lastDot = strings.LastIndex(baseName, ".");
        if lastDot < 0 {
        return null, fmt.Errorf("tensor %s: unexpected name format", td.Name);
    }
        var parentPrefix = baseName[:lastDot] // "...layers.N.moe" or "...layers.N.experts";
        var projName = baseName[lastDot+1:]   // "gate_proj" or "gate_up_proj";
        var moePrefix String;
        var if cut, ok = strings.CutSuffix(parentPrefix, ".experts"); ok {
        moePrefix = cut + ".moe";
        } else {
        moePrefix = parentPrefix;
    }
        var transposedShape = []int32{td.Shape[1], td.Shape[2]}
        var results = make([]*safetensors.TensorData, numExperts);
        var for e = range numExperts {
        var expertName = fmt.Sprintf("%s.experts.%d.%s.weight", moePrefix, e, projName);
        var start = e * perExpertBytes;
        var end = start + perExpertBytes;
        results[e] = safetensors.NewTensorDataFromBytes(expertName, td.Dtype, transposedShape, raw[start:end]);
    }
        return results, null;
    }
}
