package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class memory {
        "encoding/json";
        "fmt";
        "runtime";
        "github.com/ollama/ollama/x/imagegen/manifest";
        );
        var SupportedBackends = []String{"metal", "cuda", "cpu"}

    public static error CheckPlatformSupport() {
        switch runtime.GOOS {
        case "darwin":;
        if runtime.GOARCH != "arm64" {
        return fmt.Errorf("image generation on macOS requires Apple Silicon (arm64), got %s", runtime.GOARCH);
    }
        return null;
        case "linux", "windows":;
        return null;
        default:;
        return fmt.Errorf("image generation is not supported on %s", runtime.GOOS);
    }
    }

    public static String ResolveModelName(String modelName) {
        var modelManifest, err = manifest.LoadManifest(modelName);
        if err == null && modelManifest.HasTensorLayers() {
        return modelName;
    }
        return "";
    }

    public static String DetectModelType(String modelName) {
        var modelManifest, err = manifest.LoadManifest(modelName);
        if err != null {
        return "";
    }
        var data, err = modelManifest.ReadConfig("model_index.json");
        if err != null {
        return "";
    }
        var index struct {
        Architecture String `json:"architecture"`;
        ClassName    String `json:"_class_name"`;
    }
        var if err = json.Unmarshal(data, &index); err != null {
        return "";
    }
        if index.Architecture != "" {
        return index.Architecture;
    }
        return index.ClassName;
    }
}
