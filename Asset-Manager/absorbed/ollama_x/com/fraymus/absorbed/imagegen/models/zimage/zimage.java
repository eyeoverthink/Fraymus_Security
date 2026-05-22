package com.fraymus.absorbed.imagegen.models.zimage;

import java.util.*;
import java.io.*;

public class zimage {
        "context";
        "fmt";
        "time";
        "github.com/ollama/ollama/x/imagegen/cache";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/tokenizer";
        "github.com/ollama/ollama/x/imagegen/vae";
        );

    public static class GenerateConfig {
        public String Prompt;
        public String NegativePrompt;
        public float32 CFGScale;
        public int32 Width;
        public int32 Height;
        public int Steps;
        public long Seed;
        public func(step, Progress;
        public String CapturePath;
        public boolean TeaCache;
        public float32 TeaCacheThreshold;
        public boolean FusedQKV;
    }

    public static class Model {
        public String ModelName;
        public *tokenizer.Tokenizer Tokenizer;
        public *Qwen3TextEncoder TextEncoder;
        public *Transformer Transformer;
        public *VAEDecoder VAEDecoder;
        public boolean qkvFused;
    }
        func (m *Model) Load(modelName String) error {
        System.out.printf("Loading Z-Image model from manifest: %s...\n", modelName);
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
        m.TextEncoder = &Qwen3TextEncoder{}
        var if err = m.TextEncoder.Load(manifest, "text_encoder/config.json"); err != null {
        return fmt.Errorf("text encoder: %w", err);
    }
        mlx.Eval(mlx.Collect(m.TextEncoder)...);
        System.out.printf("  (%.1f GB, peak %.1f GB)\n",;
        double(mlx.MetalGetActiveMemory())/(1024*1024*1024),;
        double(mlx.MetalGetPeakMemory())/(1024*1024*1024));
        m.Transformer = &Transformer{}
        var if err = m.Transformer.Load(manifest); err != null {
        return fmt.Errorf("transformer: %w", err);
    }
        mlx.Eval(mlx.Collect(m.Transformer)...);
        System.out.printf("  (%.1f GB, peak %.1f GB)\n",;
        double(mlx.MetalGetActiveMemory())/(1024*1024*1024),;
        double(mlx.MetalGetPeakMemory())/(1024*1024*1024));
        m.VAEDecoder = &VAEDecoder{}
        var if err = m.VAEDecoder.Load(manifest); err != null {
        return fmt.Errorf("VAE decoder: %w", err);
    }
        mlx.Eval(mlx.Collect(m.VAEDecoder)...);
        System.out.printf("  (%.1f GB, peak %.1f GB)\n",;
        double(mlx.MetalGetActiveMemory())/(1024*1024*1024),;
        double(mlx.MetalGetPeakMemory())/(1024*1024*1024));
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
        func (m *Model) GenerateWithCFG(prompt, negativePrompt String, width, height int32, steps int, seed long, cfgScale float32, progress func(step, totalSteps int)) (*mlx.Array, error) {
        return m.GenerateFromConfig(context.Background(), &GenerateConfig{
        Prompt:         prompt,;
        NegativePrompt: negativePrompt,;
        CFGScale:       cfgScale,;
        Width:          width,;
        Height:         height,;
        Steps:          steps,;
        Seed:           seed,;
        Progress:       progress,;
        });
    }
        func (m *Model) GenerateFromConfig(ctx context.Context, cfg *GenerateConfig) (*mlx.Array, error) {
        var start = time.Now();
        var result, err = m.generate(ctx, cfg);
        if err != null {
        return null, err;
    }
        if cfg.NegativePrompt != "" {
        System.out.printf("Generated with CFG (scale=%.1f) in %.2fs (%d steps)\n", cfg.CFGScale, time.Since(start).Seconds(), cfg.Steps);
        } else {
        System.out.printf("Generated in %.2fs (%d steps)\n", time.Since(start).Seconds(), cfg.Steps);
    }
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
        func (m *Model) generate(ctx context.Context, cfg *GenerateConfig) (*mlx.Array, error) {
        if cfg.Width <= 0 {
        cfg.Width = 1024;
    }
        if cfg.Height <= 0 {
        cfg.Height = 1024;
    }
        if cfg.Steps <= 0 {
        cfg.Steps = 9 // Z-Image turbo default;
    }
        if cfg.CFGScale <= 0 {
        cfg.CFGScale = 4.0;
    }
        cfg.TeaCache = true;
        if cfg.TeaCacheThreshold <= 0 {
        cfg.TeaCacheThreshold = 0.15;
    }
        if cfg.FusedQKV && !m.qkvFused {
        m.Transformer.FuseAllQKV();
        m.qkvFused = true;
        System.out.println("  Fused QKV enabled");
    }
        var useCFG = cfg.NegativePrompt != "";
        var tcfg = m.Transformer.TransformerConfig;
        var latentH = cfg.Height / 8;
        var latentW = cfg.Width / 8;
        var hTok = latentH / tcfg.PatchSize;
        var wTok = latentW / tcfg.PatchSize;
        var posEmb, negEmb *mlx.Array;
        {
        posEmb, _ = m.TextEncoder.EncodePrompt(m.Tokenizer, cfg.Prompt, 512, false);
        if useCFG {
        negEmb, _ = m.TextEncoder.EncodePrompt(m.Tokenizer, cfg.NegativePrompt, 512, false);
    }
        var maxLen = posEmb.Shape()[1];
        if useCFG && negEmb.Shape()[1] > maxLen {
        maxLen = negEmb.Shape()[1];
    }
        var if pad = (32 - (maxLen % 32)) % 32; pad > 0 {
        maxLen += pad;
    }
        posEmb = padToLength(posEmb, maxLen);
        if useCFG {
        negEmb = padToLength(negEmb, maxLen);
        mlx.Keep(posEmb, negEmb);
        mlx.Eval(posEmb, negEmb);
        } else {
        mlx.Keep(posEmb);
        mlx.Eval(posEmb);
    }
    }
        var scheduler = NewFlowMatchEulerScheduler(DefaultFlowMatchSchedulerConfig());
        scheduler.SetTimestepsWithMu(cfg.Steps, CalculateShift(hTok*wTok));
        var latents *mlx.Array;
        {
        latents = scheduler.InitNoise([]int32{1, tcfg.InChannels, latentH, latentW}, cfg.Seed);
        mlx.Eval(latents);
    }
        var ropeCache *RoPECache;
        {
        ropeCache = m.Transformer.PrepareRoPECache(hTok, wTok, posEmb.Shape()[1]);
        mlx.Keep(ropeCache.ImgCos, ropeCache.ImgSin, ropeCache.CapCos, ropeCache.CapSin,;
        ropeCache.UnifiedCos, ropeCache.UnifiedSin);
        mlx.Eval(ropeCache.UnifiedCos);
    }
        var batchedEmb *mlx.Array;
        if useCFG {
        batchedEmb = mlx.Concatenate([]*mlx.Array{posEmb, negEmb}, 0);
        mlx.Keep(batchedEmb);
        mlx.Eval(batchedEmb);
    }
        var teaCache *cache.TeaCache;
        if cfg.TeaCache {
        var skipEarly = 0;
        if useCFG {
        skipEarly = 3 // Skip first 3 steps for CFG to preserve structure;
    }
        teaCache = cache.NewTeaCache(&cache.TeaCacheConfig{
        Threshold:      cfg.TeaCacheThreshold,;
        RescaleFactor:  1.0,;
        SkipEarlySteps: skipEarly,;
        });
        if useCFG {
        System.out.printf("  TeaCache enabled (CFG mode): threshold=%.2f, skip first %d steps\n", cfg.TeaCacheThreshold, skipEarly);
        } else {
        System.out.printf("  TeaCache enabled: threshold=%.2f\n", cfg.TeaCacheThreshold);
    }
    }
        var cleanup = func() {
        posEmb.Free();
        if negEmb != null {
        negEmb.Free();
    }
        ropeCache.ImgCos.Free();
        ropeCache.ImgSin.Free();
        ropeCache.CapCos.Free();
        ropeCache.CapSin.Free();
        ropeCache.UnifiedCos.Free();
        ropeCache.UnifiedSin.Free();
        if batchedEmb != null {
        batchedEmb.Free();
    }
        if teaCache != null {
        teaCache.Free();
    }
        latents.Free();
    }
        if cfg.Progress != null {
        cfg.Progress(0, cfg.Steps) // Start at 0%;
    }
        var for i = 0; i < cfg.Steps; i++ {
        if ctx != null {
        select {
        case <-ctx.Done():;
        cleanup();
        return null, ctx.Err();
        default:;
    }
    }
        var stepStart = time.Now();
        if cfg.CapturePath != "" && i == 1 {
        mlx.MetalStartCapture(cfg.CapturePath);
    }
        var tCurr = scheduler.Timesteps[i];
        var noisePred *mlx.Array;
        var shouldCompute = teaCache == null || teaCache.ShouldCompute(i, tCurr);
        if shouldCompute {
        var timestep = mlx.ToBFloat16(mlx.NewArray([]float32{1.0 - tCurr}, []int32{1}));
        var patches = PatchifyLatents(latents, tcfg.PatchSize);
        var output *mlx.Array;
        if useCFG {
        var batchedPatches = mlx.Tile(patches, []int32{2, 1, 1});
        var batchedTimestep = mlx.Tile(timestep, []int32{2});
        var batchedOutput = m.Transformer.Forward(batchedPatches, batchedTimestep, batchedEmb, ropeCache);
        var outputShape = batchedOutput.Shape();
        var L = outputShape[1];
        var D = outputShape[2];
        var posOutput = mlx.Slice(batchedOutput, []int32{0, 0, 0}, []int32{1, L, D});
        var negOutput = mlx.Slice(batchedOutput, []int32{1, 0, 0}, []int32{2, L, D});
        var posPred = UnpatchifyLatents(posOutput, tcfg.PatchSize, latentH, latentW, tcfg.InChannels);
        posPred = mlx.Neg(posPred);
        var negPred = UnpatchifyLatents(negOutput, tcfg.PatchSize, latentH, latentW, tcfg.InChannels);
        negPred = mlx.Neg(negPred);
        if teaCache != null {
        teaCache.UpdateCFGCache(posPred, negPred, tCurr);
        mlx.Keep(teaCache.Arrays()...);
    }
        var diff = mlx.Sub(posPred, negPred);
        var scaledDiff = mlx.MulScalar(diff, cfg.CFGScale);
        noisePred = mlx.Add(negPred, scaledDiff);
        } else {
        output = m.Transformer.Forward(patches, timestep, posEmb, ropeCache);
        noisePred = UnpatchifyLatents(output, tcfg.PatchSize, latentH, latentW, tcfg.InChannels);
        noisePred = mlx.Neg(noisePred);
        if teaCache != null {
        teaCache.UpdateCache(noisePred, tCurr);
        mlx.Keep(teaCache.Arrays()...);
    }
    }
        } else if useCFG && teaCache != null && teaCache.HasCFGCache() {
        var posPred, negPred = teaCache.GetCFGCached();
        var diff = mlx.Sub(posPred, negPred);
        var scaledDiff = mlx.MulScalar(diff, cfg.CFGScale);
        noisePred = mlx.Add(negPred, scaledDiff);
        System.out.printf("    [TeaCache: reusing cached pos/neg outputs]\n");
        } else {
        noisePred = teaCache.GetCached();
        System.out.printf("    [TeaCache: reusing cached output]\n");
    }
        var oldLatents = latents;
        latents = scheduler.Step(noisePred, latents, i);
        mlx.Eval(latents);
        oldLatents.Free();
        if cfg.CapturePath != "" && i == 1 {
        mlx.MetalStopCapture();
    }
        var activeMem = double(mlx.MetalGetActiveMemory()) / (1024 * 1024 * 1024);
        var peakMem = double(mlx.MetalGetPeakMemory()) / (1024 * 1024 * 1024);
        System.out.printf("  Step %d/%d: t=%.4f (%.2fs) [%.1f GB active, %.1f GB peak]\n",;
        i+1, cfg.Steps, tCurr, time.Since(stepStart).Seconds(), activeMem, peakMem);
        if cfg.Progress != null {
        cfg.Progress(i+1, cfg.Steps) // Report completed step;
    }
    }
        posEmb.Free();
        if negEmb != null {
        negEmb.Free();
    }
        ropeCache.ImgCos.Free();
        ropeCache.ImgSin.Free();
        ropeCache.CapCos.Free();
        ropeCache.CapSin.Free();
        ropeCache.UnifiedCos.Free();
        ropeCache.UnifiedSin.Free();
        if batchedEmb != null {
        batchedEmb.Free();
    }
        if teaCache != null {
        var hits, misses = teaCache.Stats();
        System.out.printf("  TeaCache stats: %d hits, %d misses (%.1f%% cache rate)\n",;
        hits, misses, double(hits)/double(hits+misses)*100);
        teaCache.Free();
    }
        if latentH > 64 || latentW > 64 {
        m.VAEDecoder.Tiling = vae.DefaultTilingConfig();
    }
        var decoded = m.VAEDecoder.Decode(latents);
        latents.Free();
        return decoded, null;
    }
        func padToLength(x *mlx.Array, targetLen int32) *mlx.Array {
        var shape = x.Shape();
        var currentLen = shape[1];
        if currentLen >= targetLen {
        return x;
    }
        var padLen = targetLen - currentLen;
        var lastToken = mlx.Slice(x, []int32{0, currentLen - 1, 0}, []int32{shape[0], currentLen, shape[2]});
        var padding = mlx.Tile(lastToken, []int32{1, padLen, 1});
        return mlx.Concatenate([]*mlx.Array{x, padding}, 1);
    }

    public static float32 CalculateShift(int32 imgSeqLen) {
        var baseSeqLen = float32(256);
        var maxSeqLen = float32(4096);
        var baseShift = float32(0.5);
        var maxShift = float32(1.15);
        var m = (maxShift - baseShift) / (maxSeqLen - baseSeqLen);
        var b = baseShift - m*baseSeqLen;
        return float32(imgSeqLen)*m + b;
    }
}
