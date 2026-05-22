package com.fraymus.absorbed.imagegen.models.flux2;

import java.util.*;
import java.io.*;

public class vae {
        "fmt";
        "math";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/nn";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        "github.com/ollama/ollama/x/imagegen/vae";
        );

    public static class VAEConfig {
        public String ActFn;
        public float32 BatchNormEps;
        public float32 BatchNormMomentum;
        public []int32 BlockOutChannels;
        public boolean ForceUpcast;
        public int32 InChannels;
        public int32 LatentChannels;
        public int32 LayersPerBlock;
        public boolean MidBlockAddAttn;
        public int32 NormNumGroups;
        public int32 OutChannels;
        public []int32 PatchSize;
        public int32 SampleSize;
        public boolean UsePostQuantConv;
        public boolean UseQuantConv;
    }

    public static class BatchNorm2D {
        public *mlx.Array RunningMean;
        public *mlx.Array RunningVar;
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public float32 Eps;
        public float32 Momentum;
    }
        func (bn *BatchNorm2D) Forward(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var C = shape[3];
        var mean = mlx.Reshape(bn.RunningMean, 1, 1, 1, C);
        var variance = mlx.Reshape(bn.RunningVar, 1, 1, 1, C);
        var xNorm = mlx.Sub(x, mean);
        xNorm = mlx.Div(xNorm, mlx.Sqrt(mlx.AddScalar(variance, bn.Eps)));
        if bn.Weight != null {
        var weight = mlx.Reshape(bn.Weight, 1, 1, 1, C);
        xNorm = mlx.Mul(xNorm, weight);
    }
        if bn.Bias != null {
        var bias = mlx.Reshape(bn.Bias, 1, 1, 1, C);
        xNorm = mlx.Add(xNorm, bias);
    }
        return xNorm;
    }
        func (bn *BatchNorm2D) Denormalize(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var C = shape[3];
        var mean = mlx.Reshape(bn.RunningMean, 1, 1, 1, C);
        var variance = mlx.Reshape(bn.RunningVar, 1, 1, 1, C);
        if bn.Bias != null {
        var bias = mlx.Reshape(bn.Bias, 1, 1, 1, C);
        x = mlx.Sub(x, bias);
    }
        if bn.Weight != null {
        var weight = mlx.Reshape(bn.Weight, 1, 1, 1, C);
        x = mlx.Div(x, weight);
    }
        x = mlx.Mul(x, mlx.Sqrt(mlx.AddScalar(variance, bn.Eps)));
        x = mlx.Add(x, mean);
        return x;
    }

    public static class GroupNormLayer {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public int32 NumGroups;
        public float32 Eps;
    }
        func (gn *GroupNormLayer) Forward(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var H = shape[1];
        var W = shape[2];
        var C = shape[3];
        var groupSize = C / gn.NumGroups;
        x = mlx.Reshape(x, B, H, W, gn.NumGroups, groupSize);
        var mean = mlx.Mean(x, 1, true);
        mean = mlx.Mean(mean, 2, true);
        mean = mlx.Mean(mean, 4, true);
        var xCentered = mlx.Sub(x, mean);
        var sq = mlx.Square(xCentered);
        var variance = mlx.Mean(sq, 1, true);
        variance = mlx.Mean(variance, 2, true);
        variance = mlx.Mean(variance, 4, true);
        var xNorm = mlx.Div(xCentered, mlx.Sqrt(mlx.AddScalar(variance, gn.Eps)));
        xNorm = mlx.Reshape(xNorm, B, H, W, C);
        if gn.Weight != null {
        var weight = mlx.Reshape(gn.Weight, 1, 1, 1, C);
        xNorm = mlx.Mul(xNorm, weight);
    }
        if gn.Bias != null {
        var bias = mlx.Reshape(gn.Bias, 1, 1, 1, C);
        xNorm = mlx.Add(xNorm, bias);
    }
        return xNorm;
    }

    public static class Conv2D {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public int32 Stride;
        public int32 Padding;
    }
        func (conv *Conv2D) Transform(field String, arr *mlx.Array) *mlx.Array {
        if field == "Weight" {
        return mlx.Transpose(arr, 0, 2, 3, 1);
    }
        return arr;
    }
        func (conv *Conv2D) Forward(x *mlx.Array) *mlx.Array {
        var out = mlx.Conv2d(x, conv.Weight, conv.Stride, conv.Padding);
        if conv.Bias != null {
        var bias = mlx.Reshape(conv.Bias, 1, 1, 1, conv.Bias.Dim(0));
        out = mlx.Add(out, bias);
    }
        return out;
    }

    public static class ResnetBlock2D {
        public *GroupNormLayer Norm1;
        public *Conv2D Conv1;
        public *GroupNormLayer Norm2;
        public *Conv2D Conv2;
        public *Conv2D ConvShortcut;
    }
        func (rb *ResnetBlock2D) Forward(x *mlx.Array) *mlx.Array {
        var h = rb.Norm1.Forward(x);
        h = mlx.SiLU(h);
        h = rb.Conv1.Forward(h);
        h = rb.Norm2.Forward(h);
        h = mlx.SiLU(h);
        h = rb.Conv2.Forward(h);
        if rb.ConvShortcut != null {
        x = rb.ConvShortcut.Forward(x);
    }
        return mlx.Add(h, x);
    }

    public static class VAEAttentionBlock {
        public *GroupNormLayer GroupNorm;
        public nn.LinearLayer ToQ;
        public nn.LinearLayer ToK;
        public nn.LinearLayer ToV;
        public nn.LinearLayer ToOut;
    }
        func (ab *VAEAttentionBlock) Forward(x *mlx.Array) *mlx.Array {
        var residual = x;
        var shape = x.Shape();
        var B = shape[0];
        var H = shape[1];
        var W = shape[2];
        var C = shape[3];
        var h = ab.GroupNorm.Forward(x);
        h = mlx.Reshape(h, B, H*W, C);
        var q = ab.ToQ.Forward(h);
        var k = ab.ToK.Forward(h);
        var v = ab.ToV.Forward(h);
        q = mlx.ExpandDims(q, 1);
        k = mlx.ExpandDims(k, 1);
        v = mlx.ExpandDims(v, 1);
        var scale = float32(1.0 / math.Sqrt(double(C)));
        var out = mlx.ScaledDotProductAttention(q, k, v, scale, false);
        out = mlx.Squeeze(out, 1);
        out = ab.ToOut.Forward(out);
        out = mlx.Reshape(out, B, H, W, C);
        out = mlx.Add(out, residual);
        return out;
    }

    public static class UpDecoderBlock2D {
        public []*ResnetBlock2D ResnetBlocks;
        public *Conv2D Upsample;
    }
        func (ub *UpDecoderBlock2D) Forward(x *mlx.Array) *mlx.Array {
        var for _, resnet = range ub.ResnetBlocks {
        x = resnet.Forward(x);
    }
        if ub.Upsample != null {
        x = upsample2x(x);
        x = ub.Upsample.Forward(x);
    }
        return x;
    }
        func upsample2x(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var H = shape[1];
        var W = shape[2];
        var hIdx = mlx.ArangeInt(0, H, 1, mlx.DtypeInt32);
        hIdx = mlx.Reshape(hIdx, H, 1);
        hIdx = mlx.BroadcastTo(hIdx, []int32{H, 2});
        hIdx = mlx.Reshape(hIdx, H*2);
        var wIdx = mlx.ArangeInt(0, W, 1, mlx.DtypeInt32);
        wIdx = mlx.Reshape(wIdx, W, 1);
        wIdx = mlx.BroadcastTo(wIdx, []int32{W, 2});
        wIdx = mlx.Reshape(wIdx, W*2);
        x = mlx.Take(x, hIdx, 1);
        x = mlx.Take(x, wIdx, 2);
        return x;
    }

    public static class VAEMidBlock {
        public *ResnetBlock2D Resnet1;
        public *VAEAttentionBlock Attention;
        public *ResnetBlock2D Resnet2;
    }
        func (mb *VAEMidBlock) Forward(x *mlx.Array) *mlx.Array {
        x = mb.Resnet1.Forward(x);
        x = mb.Attention.Forward(x);
        x = mb.Resnet2.Forward(x);
        return x;
    }
        func DefaultTilingConfig() *vae.TilingConfig {
        return vae.DefaultTilingConfig();
    }

    public static class AutoencoderKLFlux2 {
        public *VAEConfig Config;
        public *Conv2D EncoderConvIn;
        public *VAEMidBlock EncoderMid;
        public []*DownEncoderBlock2D EncoderDown;
        public *GroupNormLayer EncoderNormOut;
        public *Conv2D EncoderConvOut;
        public *Conv2D DecoderConvIn;
        public *VAEMidBlock DecoderMid;
        public []*UpDecoderBlock2D DecoderUp;
        public *GroupNormLayer DecoderNormOut;
        public *Conv2D DecoderConvOut;
        public *Conv2D QuantConv;
        public *Conv2D PostQuantConv;
        public *BatchNorm2D LatentBN;
        public *vae.TilingConfig Tiling;
    }

    public static class DownEncoderBlock2D {
        public []*ResnetBlock2D ResnetBlocks;
        public *Conv2D Downsample;
    }
        func (db *DownEncoderBlock2D) Forward(x *mlx.Array) *mlx.Array {
        var for _, resnet = range db.ResnetBlocks {
        x = resnet.Forward(x);
    }
        if db.Downsample != null {
        x = mlx.Pad(x, []int32{0, 0, 0, 1, 0, 1, 0, 0});
        x = db.Downsample.Forward(x);
    }
        return x;
    }
        func (m *AutoencoderKLFlux2) Load(modelManifest *manifest.ModelManifest) error {
        fmt.Print("  Loading VAE... ");
        var cfg VAEConfig;
        var if err = modelManifest.ReadConfigJSON("vae/config.json", &cfg); err != null {
        return fmt.Errorf("config: %w", err);
    }
        m.Config = &cfg;
        var weights, err = manifest.LoadWeightsFromManifest(modelManifest, "vae");
        if err != null {
        return fmt.Errorf("weights: %w", err);
    }
        var if err = weights.Load(0); err != null {
        return fmt.Errorf("load weights: %w", err);
    }
        defer weights.ReleaseAll();
        return m.loadWeights(weights, &cfg);
    }
        func (m *AutoencoderKLFlux2) loadWeights(weights safetensors.WeightSource, cfg *VAEConfig) error {
        var err error;
        var if err = m.loadEncoderWeights(weights, cfg); err != null {
        return fmt.Errorf("encoder: %w", err);
    }
        m.DecoderConvIn = &Conv2D{Stride: 1, Padding: 1}
        var if err = safetensors.LoadModule(m.DecoderConvIn, weights, "decoder.conv_in"); err != null {
        return fmt.Errorf("decoder.conv_in: %w", err);
    }
        m.DecoderMid, err = loadVAEMidBlock(weights, "decoder.mid_block", cfg.NormNumGroups);
        if err != null {
        return fmt.Errorf("decoder.mid_block: %w", err);
    }
        var numBlocks = len(cfg.BlockOutChannels);
        m.DecoderUp = make([]*UpDecoderBlock2D, numBlocks);
        var for i = 0; i < numBlocks; i++ {
        var prefix = fmt.Sprintf("decoder.up_blocks.%d", i);
        var hasUpsample = i < numBlocks-1;
        m.DecoderUp[i], err = loadUpDecoderBlock2D(weights, prefix, cfg.LayersPerBlock+1, cfg.NormNumGroups, hasUpsample);
        if err != null {
        return fmt.Errorf("%s: %w", prefix, err);
    }
    }
        m.DecoderNormOut = &GroupNormLayer{NumGroups: cfg.NormNumGroups, Eps: 1e-5}
        var if err = safetensors.LoadModule(m.DecoderNormOut, weights, "decoder.conv_norm_out"); err != null {
        return fmt.Errorf("decoder.conv_norm_out: %w", err);
    }
        m.DecoderConvOut = &Conv2D{Stride: 1, Padding: 1}
        var if err = safetensors.LoadModule(m.DecoderConvOut, weights, "decoder.conv_out"); err != null {
        return fmt.Errorf("decoder.conv_out: %w", err);
    }
        if cfg.UsePostQuantConv {
        m.PostQuantConv = &Conv2D{Stride: 1, Padding: 0}
        var if err = safetensors.LoadModule(m.PostQuantConv, weights, "post_quant_conv"); err != null {
        return fmt.Errorf("post_quant_conv: %w", err);
    }
    }
        var bnMean, err = weights.GetTensor("bn.running_mean");
        if err != null {
        return fmt.Errorf("bn.running_mean: %w", err);
    }
        var bnVar, err = weights.GetTensor("bn.running_var");
        if err != null {
        return fmt.Errorf("bn.running_var: %w", err);
    }
        m.LatentBN = &BatchNorm2D{
        RunningMean: bnMean,;
        RunningVar:  bnVar,;
        Weight:      null, // affine=False;
        Bias:        null, // affine=False;
        Eps:         cfg.BatchNormEps,;
        Momentum:    cfg.BatchNormMomentum,;
    }
        System.out.println("✓");
        return null;
    }

    public static void loadVAEMidBlock(safetensors.WeightSource weights, String prefix) {
        var resnet1, err = loadResnetBlock2D(weights, prefix+".resnets.0", numGroups);
        if err != null {
        return null, err;
    }
        var attention, err = loadVAEAttentionBlock(weights, prefix+".attentions.0", numGroups);
        if err != null {
        return null, err;
    }
        var resnet2, err = loadResnetBlock2D(weights, prefix+".resnets.1", numGroups);
        if err != null {
        return null, err;
    }
        return &VAEMidBlock{
        Resnet1:   resnet1,;
        Attention: attention,;
        Resnet2:   resnet2,;
        }, null;
    }

    public static void loadResnetBlock2D(safetensors.WeightSource weights, String prefix) {
        var block = &ResnetBlock2D{
        Norm1:        &GroupNormLayer{NumGroups: numGroups, Eps: 1e-5},;
        Conv1:        &Conv2D{Stride: 1, Padding: 1},;
        Norm2:        &GroupNormLayer{NumGroups: numGroups, Eps: 1e-5},;
        Conv2:        &Conv2D{Stride: 1, Padding: 1},;
        ConvShortcut: &Conv2D{Stride: 1, Padding: 0}, // Pre-allocate for optional loading;
    }
        var if err = safetensors.LoadModule(block, weights, prefix); err != null {
        return null, err;
    }
        if block.ConvShortcut.Weight == null {
        block.ConvShortcut = null;
    }
        return block, null;
    }

    public static void loadVAEAttentionBlock(safetensors.WeightSource weights, String prefix) {
        var ab = &VAEAttentionBlock{
        GroupNorm: &GroupNormLayer{NumGroups: numGroups, Eps: 1e-5},;
    }
        var if err = safetensors.LoadModule(ab, weights, prefix); err != null {
        return null, err;
    }
        return ab, null;
    }

    public static void loadUpDecoderBlock2D(safetensors.WeightSource weights, String prefix, int32 numGroups) {
        var resnets = make([]*ResnetBlock2D, numLayers);
        var for i = int32(0); i < numLayers; i++ {
        var resPrefix = fmt.Sprintf("%s.resnets.%d", prefix, i);
        var resnet, err = loadResnetBlock2D(weights, resPrefix, numGroups);
        if err != null {
        return null, err;
    }
        resnets[i] = resnet;
    }
        var upsample *Conv2D;
        if hasUpsample {
        upsample = &Conv2D{Stride: 1, Padding: 1}
        var if err = safetensors.LoadModule(upsample, weights, prefix+".upsamplers.0.conv"); err != null {
        return null, err;
    }
    }
        return &UpDecoderBlock2D{
        ResnetBlocks: resnets,;
        Upsample:     upsample,;
        }, null;
    }
        func (vae *AutoencoderKLFlux2) Patchify(latents *mlx.Array) *mlx.Array {
        var shape = latents.Shape();
        var B = shape[0];
        var C = shape[1];
        var H = shape[2];
        var W = shape[3];
        var patchH = vae.Config.PatchSize[0];
        var patchW = vae.Config.PatchSize[1];
        var pH = H / patchH;
        var pW = W / patchW;
        var x = mlx.Reshape(latents, B, C, pH, patchH, pW, patchW);
        x = mlx.Transpose(x, 0, 2, 4, 1, 3, 5);
        return mlx.Reshape(x, B, pH*pW, C*patchH*patchW);
    }
        func (vae *AutoencoderKLFlux2) Unpatchify(patches *mlx.Array, pH, pW, C int32) *mlx.Array {
        var shape = patches.Shape();
        var B = shape[0];
        var patchH = vae.Config.PatchSize[0];
        var patchW = vae.Config.PatchSize[1];
        var x = mlx.Reshape(patches, B, pH, pW, C, patchH, patchW);
        x = mlx.Transpose(x, 0, 3, 1, 4, 2, 5);
        var H = pH * patchH;
        var W = pW * patchW;
        return mlx.Reshape(x, B, C, H, W);
    }
        func (vae *AutoencoderKLFlux2) denormalizePatchified(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var C = shape[2] // 128;
        var mean = mlx.Reshape(vae.LatentBN.RunningMean, 1, 1, C);
        var variance = mlx.Reshape(vae.LatentBN.RunningVar, 1, 1, C);
        if vae.LatentBN.Bias != null {
        var bias = mlx.Reshape(vae.LatentBN.Bias, 1, 1, C);
        x = mlx.Sub(x, bias);
    }
        if vae.LatentBN.Weight != null {
        var weight = mlx.Reshape(vae.LatentBN.Weight, 1, 1, C);
        x = mlx.Div(x, weight);
    }
        x = mlx.Mul(x, mlx.Sqrt(mlx.AddScalar(variance, vae.LatentBN.Eps)));
        x = mlx.Add(x, mean);
        return x;
    }
        func (v *AutoencoderKLFlux2) Decode(latents *mlx.Array, pH, pW int32) *mlx.Array {
        var z = v.denormalizePatchified(latents);
        z = v.Unpatchify(z, pH, pW, v.Config.LatentChannels);
        z = mlx.Transpose(z, 0, 2, 3, 1);
        if v.Tiling != null {
        mlx.Eval(z);
        return vae.DecodeTiled(z, v.Tiling, v.decodeTile);
    }
        var h = v.decodeTile(z);
        h = mlx.ClipScalar(h, 0.0, 1.0, true, true);
        h = mlx.Transpose(h, 0, 3, 1, 2);
        return h;
    }
        func (vae *AutoencoderKLFlux2) decodeTile(z *mlx.Array) *mlx.Array {
        if vae.PostQuantConv != null {
        z = vae.PostQuantConv.Forward(z);
    }
        var h = vae.DecoderConvIn.Forward(z);
        h = vae.DecoderMid.Forward(h);
        var for _, upBlock = range vae.DecoderUp {
        h = upBlock.Forward(h);
    }
        h = vae.DecoderNormOut.Forward(h);
        h = mlx.SiLU(h);
        h = vae.DecoderConvOut.Forward(h);
        h = mlx.MulScalar(h, 0.5);
        h = mlx.AddScalar(h, 0.5);
        return h;
    }
        func (m *AutoencoderKLFlux2) loadEncoderWeights(weights safetensors.WeightSource, cfg *VAEConfig) error {
        var err error;
        m.EncoderConvIn = &Conv2D{Stride: 1, Padding: 1}
        var if err = safetensors.LoadModule(m.EncoderConvIn, weights, "encoder.conv_in"); err != null {
        return fmt.Errorf("encoder.conv_in: %w", err);
    }
        var numBlocks = len(cfg.BlockOutChannels);
        m.EncoderDown = make([]*DownEncoderBlock2D, numBlocks);
        var for i = 0; i < numBlocks; i++ {
        var prefix = fmt.Sprintf("encoder.down_blocks.%d", i);
        var hasDownsample = i < numBlocks-1;
        m.EncoderDown[i], err = loadDownEncoderBlock2D(weights, prefix, cfg.LayersPerBlock, cfg.NormNumGroups, hasDownsample);
        if err != null {
        return fmt.Errorf("%s: %w", prefix, err);
    }
    }
        m.EncoderMid, err = loadVAEMidBlock(weights, "encoder.mid_block", cfg.NormNumGroups);
        if err != null {
        return fmt.Errorf("encoder.mid_block: %w", err);
    }
        m.EncoderNormOut = &GroupNormLayer{NumGroups: cfg.NormNumGroups, Eps: 1e-5}
        var if err = safetensors.LoadModule(m.EncoderNormOut, weights, "encoder.conv_norm_out"); err != null {
        return fmt.Errorf("encoder.conv_norm_out: %w", err);
    }
        m.EncoderConvOut = &Conv2D{Stride: 1, Padding: 1}
        var if err = safetensors.LoadModule(m.EncoderConvOut, weights, "encoder.conv_out"); err != null {
        return fmt.Errorf("encoder.conv_out: %w", err);
    }
        if cfg.UseQuantConv {
        m.QuantConv = &Conv2D{Stride: 1, Padding: 0}
        var if err = safetensors.LoadModule(m.QuantConv, weights, "quant_conv"); err != null {
        return fmt.Errorf("quant_conv: %w", err);
    }
    }
        return null;
    }

    public static void loadDownEncoderBlock2D(safetensors.WeightSource weights, String prefix, int32 numGroups) {
        var resnets = make([]*ResnetBlock2D, numLayers);
        var for i = int32(0); i < numLayers; i++ {
        var resPrefix = fmt.Sprintf("%s.resnets.%d", prefix, i);
        var resnet, err = loadResnetBlock2D(weights, resPrefix, numGroups);
        if err != null {
        return null, err;
    }
        resnets[i] = resnet;
    }
        var downsample *Conv2D;
        if hasDownsample {
        downsample = &Conv2D{Stride: 2, Padding: 0}
        var if err = safetensors.LoadModule(downsample, weights, prefix+".downsamplers.0.conv"); err != null {
        return null, err;
    }
    }
        return &DownEncoderBlock2D{
        ResnetBlocks: resnets,;
        Downsample:   downsample,;
        }, null;
    }
        func (vae *AutoencoderKLFlux2) EncodeImage(image *mlx.Array) *mlx.Array {
        var x = mlx.Transpose(image, 0, 2, 3, 1);
        var h = vae.EncoderConvIn.Forward(x);
        var for _, downBlock = range vae.EncoderDown {
        h = downBlock.Forward(h);
    }
        h = vae.EncoderMid.Forward(h);
        h = vae.EncoderNormOut.Forward(h);
        h = mlx.SiLU(h);
        h = vae.EncoderConvOut.Forward(h);
        if vae.QuantConv != null {
        h = vae.QuantConv.Forward(h);
    }
        var shape = h.Shape();
        var latentChannels = vae.Config.LatentChannels // 32;
        h = mlx.Slice(h, []int32{0, 0, 0, 0}, []int32{shape[0], shape[1], shape[2], latentChannels});
        h = mlx.Transpose(h, 0, 3, 1, 2);
        h = vae.Patchify(h);
        h = vae.normalizePatchified(h);
        return h;
    }
        func (vae *AutoencoderKLFlux2) normalizePatchified(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var C = shape[2] // 128;
        var mean = mlx.Reshape(vae.LatentBN.RunningMean, 1, 1, C);
        var variance = mlx.Reshape(vae.LatentBN.RunningVar, 1, 1, C);
        var xNorm = mlx.Sub(x, mean);
        xNorm = mlx.Div(xNorm, mlx.Sqrt(mlx.AddScalar(variance, vae.LatentBN.Eps)));
        if vae.LatentBN.Weight != null {
        var weight = mlx.Reshape(vae.LatentBN.Weight, 1, 1, C);
        xNorm = mlx.Mul(xNorm, weight);
    }
        if vae.LatentBN.Bias != null {
        var bias = mlx.Reshape(vae.LatentBN.Bias, 1, 1, C);
        xNorm = mlx.Add(xNorm, bias);
    }
        return xNorm;
    }
}
