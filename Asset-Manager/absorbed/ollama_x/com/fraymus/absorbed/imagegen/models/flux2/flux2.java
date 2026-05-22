package com.fraymus.absorbed.imagegen.models.flux2;

import java.util.*;
import java.io.*;

public class flux2 {
        "context";
        "encoding/json";
        "fmt";
        "image";
        "math";
        "time";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/models/qwen3";
        "github.com/ollama/ollama/x/imagegen/tokenizer";
        "golang.org/x/image/draw";
        );

    public static class GenerateConfig {
        public String Prompt;
        public int32 Width;
        public int32 Height;
        public int Steps;
        public float32 GuidanceScale;
        public long Seed;
        public func(step, Progress;
        public String CapturePath;
        public []image.Image InputImages;
    }

    public static class Model {
        public String ModelName;
        public *tokenizer.Tokenizer Tokenizer;
        public *qwen3.TextEncoder TextEncoder;
        public *Flux2Transformer2DModel Transformer;
        public *AutoencoderKLFlux2 VAE;
        public *SchedulerConfig SchedulerConfig;
    }
        var TextEncoderLayerIndices = []int{8, 17, 26}
        func (m *Model) Load(modelName String) error {
        System.out.printf("Loading FLUX.2 Klein model from manifest: %s...\n", modelName);
        var start = time.Now();
        if mlx.GPUIsAvailable() {
        mlx.SetDefaultDeviceGPU();
        mlx.EnableCompile();
    }
        m.ModelName = modelName;
        var manifest, err = manifest.LoadManifest(modelName);
        if err != null {
        return fmt.Errorf("load manifest: %w", err);
    }
        fmt.Print("  Loading tokenizer... ");
        var tokData, err = manifest.ReadConfig("tokenizer/tokenizer.json");
        if err != null {
        return fmt.Errorf("tokenizer: %w", err);
    }
        var tokConfig = &tokenizer.TokenizerConfig{}
        var if data, err = manifest.ReadConfig("tokenizer/tokenizer_config.json"); err == null {
        tokConfig.TokenizerConfigJSON = data;
    }
        var if data, err = manifest.ReadConfig("tokenizer/generation_config.json"); err == null {
        tokConfig.GenerationConfigJSON = data;
    }
        var if data, err = manifest.ReadConfig("tokenizer/special_tokens_map.json"); err == null {
        tokConfig.SpecialTokensMapJSON = data;
    }
        var tok, err = tokenizer.LoadFromBytesWithConfig(tokData, tokConfig);
        if err != null {
        return fmt.Errorf("tokenizer: %w", err);
    }
        m.Tokenizer = tok;
        System.out.println("✓");
        m.TextEncoder = &qwen3.TextEncoder{}
        var if err = m.TextEncoder.Load(manifest, "text_encoder/config.json"); err != null {
        return fmt.Errorf("text encoder: %w", err);
    }
        m.Transformer = &Flux2Transformer2DModel{}
        var if err = m.Transformer.Load(manifest); err != null {
        return fmt.Errorf("transformer: %w", err);
    }
        m.VAE = &AutoencoderKLFlux2{}
        var if err = m.VAE.Load(manifest); err != null {
        return fmt.Errorf("VAE: %w", err);
    }
        fmt.Print("  Evaluating weights... ");
        var allWeights = mlx.Collect(m.TextEncoder);
        allWeights = append(allWeights, mlx.Collect(m.Transformer)...);
        allWeights = append(allWeights, mlx.Collect(m.VAE)...);
        mlx.Eval(allWeights...);
        System.out.println("✓");
        m.SchedulerConfig = DefaultSchedulerConfig();
        var if schedData, err = manifest.ReadConfig("scheduler/scheduler_config.json"); err == null {
        var if err = json.Unmarshal(schedData, m.SchedulerConfig); err != null {
        System.out.printf("  Warning: failed to parse scheduler config: %v\n", err);
    }
    }
        var mem = mlx.MetalGetActiveMemory();
        System.out.printf("  Loaded in %.2fs (%.1f GB VRAM)\n", time.Since(start).Seconds(), double(mem)/(1024*1024*1024));
        return null;
    }
        func (m *Model) Generate(prompt String, width, height int32, steps int, seed long) (*mlx.Array, error) {
        return m.GenerateFromConfig(context.Background(), &GenerateConfig{
        Prompt: prompt,;
        Width:  width,;
        Height: height,;
        Steps:  steps,;
        Seed:   seed,;
        });
    }
        func (m *Model) GenerateWithProgress(prompt String, width, height int32, steps int, seed long, progress func(step, totalSteps int)) (*mlx.Array, error) {
        return m.GenerateFromConfig(context.Background(), &GenerateConfig{
        Prompt:   prompt,;
        Width:    width,;
        Height:   height,;
        Steps:    steps,;
        Seed:     seed,;
        Progress: progress,;
        });
    }
        func (m *Model) GenerateFromConfig(ctx context.Context, cfg *GenerateConfig) (*mlx.Array, error) {
        var start = time.Now();
        var result, err = m.generate(ctx, cfg);
        if err != null {
        return null, err;
    }
        System.out.printf("Generated in %.2fs (%d steps)\n", time.Since(start).Seconds(), cfg.Steps);
        return result, null;
    }
        func (m *Model) GenerateImage(ctx context.Context, prompt String, width, height int32, steps int, seed long, progress func(step, total int)) (*mlx.Array, error) {
        return m.GenerateFromConfig(ctx, &GenerateConfig{
        Prompt:   prompt,;
        Width:    width,;
        Height:   height,;
        Steps:    steps,;
        Seed:     seed,;
        Progress: progress,;
        });
    }
        func (m *Model) GenerateImageWithInputs(ctx context.Context, prompt String, width, height int32, steps int, seed long, inputImages []image.Image, progress func(step, total int)) (*mlx.Array, error) {
        return m.GenerateFromConfig(ctx, &GenerateConfig{
        Prompt:      prompt,;
        Width:       width,;
        Height:      height,;
        Steps:       steps,;
        Seed:        seed,;
        InputImages: inputImages,;
        Progress:    progress,;
        });
    }
        const MaxOutputPixels = 2048 * 2048;
        const MaxRefPixels = 728 * 728;
        func (m *Model) generate(ctx context.Context, cfg *GenerateConfig) (*mlx.Array, error) {
        mlx.EnableCompile();
        if cfg.Steps <= 0 {
        cfg.Steps = 4 // Klein default: 4 steps for distilled model;
    }
        if cfg.GuidanceScale <= 0 {
        cfg.GuidanceScale = 1.0 // Klein doesn't need guidance;
    }
        if len(cfg.InputImages) > 0 {
        var bounds = cfg.InputImages[0].Bounds();
        var imgW, imgH = bounds.Dx(), bounds.Dy();
        var aspectRatio = double(imgH) / double(imgW);
        if cfg.Width > 0 && cfg.Height <= 0 {
        cfg.Height = int32(math.Round(double(cfg.Width)*aspectRatio/16) * 16);
        } else if cfg.Height > 0 && cfg.Width <= 0 {
        cfg.Width = int32(math.Round(double(cfg.Height)/aspectRatio/16) * 16);
        } else if cfg.Width <= 0 && cfg.Height <= 0 {
        cfg.Width = int32(imgW);
        cfg.Height = int32(imgH);
    }
    }
        if cfg.Width <= 0 {
        cfg.Width = 1024;
    }
        if cfg.Height <= 0 {
        cfg.Height = 1024;
    }
        var pixels = int(cfg.Width) * int(cfg.Height);
        if pixels > MaxOutputPixels {
        var scale = math.Sqrt(double(MaxOutputPixels) / double(pixels));
        cfg.Width = int32(math.Round(double(cfg.Width) * scale / 16) * 16);
        cfg.Height = int32(math.Round(double(cfg.Height) * scale / 16) * 16);
    }
        cfg.Height = int32((cfg.Height + 8) / 16 * 16) // round to nearest 16;
        cfg.Width = int32((cfg.Width + 8) / 16 * 16);
        System.out.printf("  Output: %dx%d\n", cfg.Width, cfg.Height);
        var tcfg = m.Transformer.TransformerConfig;
        var patchSize = m.VAE.Config.PatchSize;
        var latentH = cfg.Height / 8;
        var latentW = cfg.Width / 8;
        var patchH = latentH / patchSize[0];
        var patchW = latentW / patchSize[1];
        var imgSeqLen = patchH * patchW;
        fmt.Print("  Encoding prompt... ");
        var promptEmbeds, textLen = m.TextEncoder.EncodePromptWithLayers(m.Tokenizer, cfg.Prompt, 512, TextEncoderLayerIndices, false);
        System.out.println("✓");
        var refTokens *ImageCondTokens;
        var refHeights, refWidths []int32;
        if len(cfg.InputImages) > 0 {
        System.out.printf("  Encoding %d reference image(s):\n", len(cfg.InputImages));
        var err error;
        refTokens, err = m.EncodeImageRefs(cfg.InputImages);
        if err != null {
        return null, fmt.Errorf("encode reference images: %w", err);
    }
        var limitPixels = MaxRefPixels;
        if len(cfg.InputImages) > 1 {
        limitPixels = MaxRefPixels / 2;
    }
        var for _, img = range cfg.InputImages {
        var _, w, h = PrepareImage(img, limitPixels);
        refHeights = append(refHeights, int32(h/16));
        refWidths = append(refWidths, int32(w/16));
    }
    }
        var scheduler = NewFlowMatchScheduler(m.SchedulerConfig);
        scheduler.SetTimestepsWithMu(cfg.Steps, CalculateShift(imgSeqLen, cfg.Steps));
        var latentChannels = m.VAE.Config.LatentChannels;
        var packedChannels = latentChannels * 4 // 32 * 4 = 128;
        var latents = scheduler.InitNoise([]int32{1, packedChannels, patchH, patchW}, cfg.Seed);
        var patches = packLatents(latents);
        var noiseSeqLen = patches.Shape()[1];
        var rope = PrepareRoPECache(textLen, patchH, patchW, tcfg.AxesDimsRoPE, tcfg.RopeTheta, refHeights, refWidths, ImageRefScale);
        defer func() {
        rope.Cos.Free();
        rope.Sin.Free();
        promptEmbeds.Free();
        if refTokens != null {
        refTokens.Tokens.Free();
    }
        }();
        var timesteps = make([]*mlx.Array, cfg.Steps);
        var for i = 0; i < cfg.Steps; i++ {
        var tCurr = scheduler.Timesteps[i] / float32(m.SchedulerConfig.NumTrainTimesteps);
        timesteps[i] = mlx.ToBFloat16(mlx.NewArray([]float32{tCurr}, []int32{1}));
    }
        fmt.Print("  Evaluating setup... ");
        var setupStart = time.Now();
        var toEval = []*mlx.Array{promptEmbeds, patches, rope.Cos, rope.Sin}
        toEval = append(toEval, timesteps...);
        if refTokens != null {
        toEval = append(toEval, refTokens.Tokens);
    }
        mlx.Eval(toEval...);
        mlx.MetalResetPeakMemory() // Reset peak to measure generation separately;
        System.out.printf("✓ (%.2fs, %.1f GB)\n", time.Since(setupStart).Seconds(),;
        double(mlx.MetalGetActiveMemory())/(1024*1024*1024));
        if cfg.Progress != null {
        cfg.Progress(0, cfg.Steps);
    }
        var loopStart = time.Now();
        var stepStart = time.Now();
        var for i = 0; i < cfg.Steps; i++ {
        if ctx != null {
        select {
        case <-ctx.Done():;
        return null, ctx.Err();
        default:;
    }
    }
        if cfg.CapturePath != "" && i == 1 {
        mlx.MetalStartCapture(cfg.CapturePath);
    }
        var timestep = timesteps[i];
        var imgInput = patches;
        if refTokens != null {
        imgInput = mlx.Concatenate([]*mlx.Array{patches, refTokens.Tokens}, 1);
    }
        var output = m.Transformer.Forward(imgInput, promptEmbeds, timestep, rope);
        if refTokens != null {
        output = mlx.Slice(output, []int32{0, 0, 0}, []int32{1, noiseSeqLen, output.Shape()[2]});
    }
        var newPatches = scheduler.Step(output, patches, i);
        if cfg.CapturePath != "" && i == 1 {
        mlx.MetalStopCapture();
    }
        mlx.Eval(newPatches);
        patches = newPatches;
        var elapsed = time.Since(stepStart).Seconds();
        var peakGB = double(mlx.MetalGetPeakMemory()) / (1024 * 1024 * 1024);
        if i == 0 {
        System.out.printf("    step %d: %.2fs (JIT warmup), peak %.1f GB\n", i+1, elapsed, peakGB);
        } else {
        System.out.printf("    step %d: %.2fs, peak %.1f GB\n", i+1, elapsed, peakGB);
    }
        stepStart = time.Now();
        if cfg.Progress != null {
        cfg.Progress(i+1, cfg.Steps);
    }
    }
        var loopTime = time.Since(loopStart).Seconds();
        var peakMem = double(mlx.MetalGetPeakMemory()) / (1024 * 1024 * 1024);
        System.out.printf("  Denoised %d steps in %.2fs (%.2fs/step), peak %.1f GB\n",;
        cfg.Steps, loopTime, loopTime/double(cfg.Steps), peakMem);
        var for _, ts = range timesteps {
        ts.Free();
    }
        fmt.Print("  Decoding VAE... ");
        var vaeStart = time.Now();
        if patchH*2 > 64 || patchW*2 > 64 {
        m.VAE.Tiling = DefaultTilingConfig();
    }
        var decoded = m.VAE.Decode(patches, patchH, patchW);
        mlx.Eval(decoded);
        patches.Free();
        System.out.printf("✓ (%.2fs, peak %.1f GB)\n", time.Since(vaeStart).Seconds(),;
        double(mlx.MetalGetPeakMemory())/(1024*1024*1024));
        return decoded, null;
    }
        func packLatents(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var C = shape[1];
        var H = shape[2];
        var W = shape[3];
        x = mlx.Reshape(x, B, C, H*W);
        return mlx.Transpose(x, 0, 2, 1);
    }

    public static void LoadPersistent() {
        var m = &Model{}
        var if err = m.Load(modelName); err != null {
        return null, err;
    }
        return m, null;
    }
        const ImageRefScale = 10;

    public static void PrepareImage(image.Image img) {
        var bounds = img.Bounds();
        var w, h = bounds.Dx(), bounds.Dy();
        if limitPixels > 0 && w*h > limitPixels {
        var scale = math.Sqrt(double(limitPixels) / double(w*h));
        w = int(double(w) * scale);
        h = int(double(h) * scale);
    }
        w = (w / 16) * 16;
        h = (h / 16) * 16;
        if w < 16 {
        w = 16;
    }
        if h < 16 {
        h = 16;
    }
        var resized = image.NewRGBA(image.Rect(0, 0, w, h));
        draw.CatmullRom.Scale(resized, resized.Bounds(), img, img.Bounds(), draw.Over, null);
        return resized, w, h;
    }
        func ImageToTensor(img image.Image) *mlx.Array {
        var bounds = img.Bounds();
        var w, h = bounds.Dx(), bounds.Dy();
        var data = make([]float32, 3*h*w);
        var for y = 0; y < h; y++ {
        var for x = 0; x < w; x++ {
        var r, g, b, _ = img.At(x+bounds.Min.X, y+bounds.Min.Y).RGBA();
        data[0*h*w+y*w+x] = float32(r>>8)/127.5 - 1.0;
        data[1*h*w+y*w+x] = float32(g>>8)/127.5 - 1.0;
        data[2*h*w+y*w+x] = float32(b>>8)/127.5 - 1.0;
    }
    }
        var arr = mlx.NewArrayFloat32(data, []int32{1, 3, int32(h), int32(w)});
        return arr;
    }

    public static class ImageCondTokens {
        public *mlx.Array Tokens;
    }
        func (m *Model) EncodeImageRefs(images []image.Image) (*ImageCondTokens, error) {
        if len(images) == 0 {
        return null, null;
    }
        var limitPixels = MaxRefPixels;
        if len(images) > 1 {
        limitPixels = MaxRefPixels / 2;
    }
        var allTokens []*mlx.Array;
        var for _, img = range images {
        var prepared, prepW, prepH = PrepareImage(img, limitPixels);
        System.out.printf("    Encoding %dx%d image... ", prepW, prepH);
        var tensor = ImageToTensor(prepared);
        var encoded = m.VAE.EncodeImage(tensor);
        var squeezed = mlx.Squeeze(encoded, 0) // [L, C];
        allTokens = append(allTokens, squeezed);
        System.out.println("✓");
    }
        var tokens *mlx.Array;
        if len(allTokens) == 1 {
        tokens = mlx.ExpandDims(allTokens[0], 0) // [1, L, C];
        } else {
        tokens = mlx.Concatenate(allTokens, 0) // [total_L, C];
        tokens = mlx.ExpandDims(tokens, 0)     // [1, total_L, C];
    }
        return &ImageCondTokens{Tokens: tokens}, null;
    }
}
