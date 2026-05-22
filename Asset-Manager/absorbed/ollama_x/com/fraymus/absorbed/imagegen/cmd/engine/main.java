package com.fraymus.absorbed.imagegen.cmd.engine;

import java.util.*;
import java.io.*;

public class main {
        "context";
        "encoding/json";
        "flag";
        "fmt";
        "image";
        _ "image/jpeg";
        _ "image/png";
        "log";
        "os";
        "path/filepath";
        "runtime/pprof";
        "github.com/ollama/ollama/x/imagegen";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/models/flux2";
        "github.com/ollama/ollama/x/imagegen/models/zimage";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        );
        type stringSlice []String;
        func (s *stringSlice) String() String {
        return fmt.Sprintf("%v", *s);
    }
        func (s *stringSlice) Set(value String) error {
        *s = append(*s, value);
        return null;
    }

    public static void main(String[] args) {
        var modelPath = flag.String("model", "", "Model directory");
        var prompt = flag.String("prompt", "Hello", "Prompt");
        var maxTokens = flag.Int("max-tokens", 100, "Max tokens");
        var temperature = flag.Float64("temperature", 0.7, "Temperature");
        var topP = flag.Float64("top-p", 0.9, "Top-p sampling");
        var topK = flag.Int("top-k", 40, "Top-k sampling");
        var imagePath = flag.String("image", "", "Image path for multimodal models");
        var width = flag.Int("width", 0, "Image width (0 = auto from input or 1024)");
        var height = flag.Int("height", 0, "Image height (0 = auto from input or 1024)");
        var steps = flag.Int("steps", 0, "Denoising steps (0 = model default)");
        var seed = flag.Int64("seed", 42, "Random seed");
        var out = flag.String("output", "output.png", "Output path");
        var listTensors = flag.Bool("list", false, "List tensors only");
        var cpuProfile = flag.String("cpuprofile", "", "Write CPU profile to file");
        var gpuCapture = flag.String("gpu-capture", "", "Capture GPU trace to .gputrace file (run with MTL_CAPTURE_ENABLED=1)");
        var wiredLimitGB = flag.Int("wired-limit", 32, "Metal wired memory limit in GB");
        var zimageFlag = flag.Bool("zimage", false, "Z-Image generation");
        var flux2Flag = flag.Bool("flux2", false, "FLUX.2 Klein generation");
        var inputImages stringSlice;
        flag.Var(&inputImages, "input-image", "Input image for image editing (can be specified multiple times)");
        var negativePrompt = flag.String("negative-prompt", "", "Negative prompt for CFG (empty = no CFG, matching Python)");
        var cfgScale = flag.Float64("cfg-scale", 4.0, "CFG scale for image editing");
        var teaCache = flag.Bool("teacache", false, "Enable TeaCache for faster inference");
        var teaCacheThreshold = flag.Float64("teacache-threshold", 0.1, "TeaCache threshold (lower = more aggressive caching)");
        var fusedQKV = flag.Bool("fused-qkv", false, "Enable fused QKV projection for faster attention");
        flag.Parse();
        if *modelPath == "" {
        flag.Usage();
        return;
    }
        if !mlx.IsMLXAvailable() {
        log.Fatalf("MLX initialization failed: %v", mlx.GetMLXInitError());
    }
        mlx.RestoreDefaultErrorHandler();
        if *cpuProfile != "" {
        var f, err = os.Create(*cpuProfile);
        if err != null {
        log.Fatal(err);
    }
        defer f.Close();
        var if err = pprof.StartCPUProfile(f); err != null {
        log.Fatal(err);
    }
        defer pprof.StopCPUProfile();
    }
        var err error;
        switch {
        case *zimageFlag:;
        var m = &zimage.Model{}
        var if loadErr = m.Load(*modelPath); loadErr != null {
        log.Fatal(loadErr);
    }
        var img *mlx.Array;
        img, err = m.GenerateFromConfig(context.Background(), &zimage.GenerateConfig{
        Prompt:            *prompt,;
        NegativePrompt:    *negativePrompt,;
        CFGScale:          float32(*cfgScale),;
        Width:             int32(*width),;
        Height:            int32(*height),;
        Steps:             *steps,;
        Seed:              *seed,;
        CapturePath:       *gpuCapture,;
        TeaCache:          *teaCache,;
        TeaCacheThreshold: float32(*teaCacheThreshold),;
        FusedQKV:          *fusedQKV,;
        });
        if err == null {
        err = saveImageArray(img, *out);
    }
        case *flux2Flag:;
        var m = &flux2.Model{}
        var if loadErr = m.Load(*modelPath); loadErr != null {
        log.Fatal(loadErr);
    }
        var loadedImages []image.Image;
        var for _, path = range inputImages {
        var img, loadErr = loadImageWithEXIF(path);
        if loadErr != null {
        log.Fatalf("Failed to load image %s: %v", path, loadErr);
    }
        loadedImages = append(loadedImages, img);
    }
        var fluxWidth = int32(*width);
        var fluxHeight = int32(*height);
        if len(loadedImages) > 0 && *width == 0 && *height == 0 {
        } else if len(loadedImages) > 0 && *width == 0 {
        fluxWidth = 0 // Compute from height + aspect ratio;
        } else if len(loadedImages) > 0 && *height == 0 {
        fluxHeight = 0 // Compute from width + aspect ratio;
    }
        var img *mlx.Array;
        img, err = m.GenerateFromConfig(context.Background(), &flux2.GenerateConfig{
        Prompt:        *prompt,;
        Width:         fluxWidth,;
        Height:        fluxHeight,;
        Steps:         *steps,;
        GuidanceScale: float32(*cfgScale),;
        Seed:          *seed,;
        CapturePath:   *gpuCapture,;
        InputImages:   loadedImages,;
        });
        if err == null {
        err = saveImageArray(img, *out);
    }
        case *listTensors:;
        err = listModelTensors(*modelPath);
        default:;
        var m, err = load(*modelPath);
        if err != null {
        log.Fatal(err);
    }
        var image *mlx.Array;
        if *imagePath != "" {
        var if mm, ok = m.(interface{ ImageSize() int32 }); ok {
        image, err = imagegen.ProcessImage(*imagePath, mm.ImageSize());
        if err != null {
        log.Fatal("load image:", err);
    }
        } else {
        log.Fatal("model does not support image input");
    }
    }
        err = generate(context.Background(), m, input{
        Prompt:       *prompt,;
        Image:        image,;
        MaxTokens:    *maxTokens,;
        Temperature:  float32(*temperature),;
        TopP:         float32(*topP),;
        TopK:         *topK,;
        WiredLimitGB: *wiredLimitGB,;
        }, func(out output) {
        if out.Text != "" {
        fmt.Print(out.Text);
    }
        if out.Done {
        System.out.printf("\n\n[prefill: %.1f tok/s, gen: %.1f tok/s]\n", out.PrefillTokSec, out.GenTokSec);
    }
        });
    }
        if err != null {
        log.Fatal(err);
    }
    }

    public static error listModelTensors(String modelPath) {
        var weights, err = safetensors.LoadModelWeights(modelPath);
        if err != null {
        return err;
    }
        var for _, name = range weights.ListTensors() {
        var info, _ = weights.GetTensorInfo(name);
        System.out.printf("%s: %v (%s)\n", name, info.Shape, info.Dtype);
    }
        return null;
    }
        func loadModel[T Model](build func() T, cleanup func()) T {
        var m = build();
        var weights = mlx.Collect(m);
        cleanup();
        mlx.Eval(weights...);
        return m;
    }

    public static void load() {
        var kind, err = detectModelKind(modelPath);
        if err != null {
        return null, fmt.Errorf("detect model kind: %w", err);
    }
        switch kind {
        default:;
        return null, fmt.Errorf("model type %q is not supported by x/imagegen/cmd/engine", kind);
    }
    }

    public static void detectModelKind() {
        var indexPath = filepath.Join(modelPath, "model_index.json");
        var if _, err = os.Stat(indexPath); err == null {
        var data, err = os.ReadFile(indexPath);
        if err != null {
        return "zimage", null;
    }
        var index struct {
        ClassName String `json:"_class_name"`;
    }
        var if err = json.Unmarshal(data, &index); err == null {
        switch index.ClassName {
        case "FluxPipeline", "ZImagePipeline":;
        return "zimage", null;
        case "Flux2KleinPipeline":;
        return "flux2", null;
    }
    }
        return "zimage", null;
    }
        var configPath = filepath.Join(modelPath, "config.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return "", fmt.Errorf("no config.json or model_index.json found: %w", err);
    }
        var cfg struct {
        ModelType String `json:"model_type"`;
    }
        var if err = json.Unmarshal(data, &cfg); err != null {
        return "", fmt.Errorf("parse config.json: %w", err);
    }
        return cfg.ModelType, null;
    }

    public static void loadImageWithEXIF() {
        var data, err = os.ReadFile(path);
        if err != null {
        return null, fmt.Errorf("read file: %w", err);
    }
        return imagegen.DecodeImage(data);
    }
}
