package com.fraymus.absorbed.imagegen.models.zimage;

import java.util.*;
import java.io.*;

public class vae {
        "fmt";
        "math";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        "github.com/ollama/ollama/x/imagegen/vae";
        );

    public static class VAEConfig {
        public int32 InChannels;
        public int32 OutChannels;
        public int32 LatentChannels;
        public []int32 BlockOutChannels;
        public int32 LayersPerBlock;
        public int32 NormNumGroups;
        public float32 ScalingFactor;
        public float32 ShiftFactor;
    }

    public static class GroupNormLayer {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public int32 NumGroups;
        public float32 Eps;
    }
        func NewGroupNorm(weight, bias *mlx.Array, numGroups int32) *GroupNormLayer {
        return &GroupNormLayer{
        Weight:    weight,;
        Bias:      bias,;
        NumGroups: numGroups,;
        Eps:       1e-5,;
    }
    }
        func (gn *GroupNormLayer) Forward(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var H = shape[1];
        var W = shape[2];
        var C = shape[3];
        if H*W > 512*512 {
        return gn.forwardTiled(x, B, H, W, C);
    }
        return gn.forwardSmall(x, B, H, W, C);
    }
        func (gn *GroupNormLayer) forwardSmall(x *mlx.Array, B, H, W, C int32) *mlx.Array {
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
        func (gn *GroupNormLayer) forwardTiled(x *mlx.Array, B, H, W, C int32) *mlx.Array {
        var groupSize = C / gn.NumGroups;
        var wasKept = x.Kept();
        mlx.Keep(x);
        var xFlat = mlx.Reshape(x, B, H*W, gn.NumGroups, groupSize);
        var mean1 = mlx.Mean(xFlat, 1, true);
        var mean = mlx.Mean(mean1, 3, true);
        var xSq = mlx.Square(xFlat);
        var meanSq1 = mlx.Mean(xSq, 1, true);
        var meanSq = mlx.Mean(meanSq1, 3, true);
        var meanSquared = mlx.Square(mean);
        var variance = mlx.Sub(meanSq, meanSquared);
        var varPlusEps = mlx.AddScalar(variance, gn.Eps);
        var stdDev = mlx.Sqrt(varPlusEps);
        var one = mlx.Full(1.0, 1);
        var invStd = mlx.Div(one, stdDev);
        mlx.Keep(mean, invStd);
        mlx.Eval(mean, invStd);
        var tileH = int32(512 * 512 / W);
        if tileH < 1 {
        tileH = 1;
    }
        if tileH > H {
        tileH = H;
    }
        var weightGN, biasGN *mlx.Array;
        if gn.Weight != null {
        weightGN = mlx.Reshape(gn.Weight, 1, 1, gn.NumGroups, groupSize);
        mlx.Keep(weightGN);
        mlx.Eval(weightGN);
    }
        if gn.Bias != null {
        biasGN = mlx.Reshape(gn.Bias, 1, 1, gn.NumGroups, groupSize);
        mlx.Keep(biasGN);
        mlx.Eval(biasGN);
    }
        var tiles []*mlx.Array;
        var for hStart = int32(0); hStart < H; hStart += tileH {
        var hEnd = hStart + tileH;
        if hEnd > H {
        hEnd = H;
    }
        var tileHeight = hEnd - hStart;
        var spatialSize = tileHeight * W;
        var tile = mlx.Slice(x, []int32{0, hStart, 0, 0}, []int32{B, hEnd, W, C});
        var tileFlat = mlx.Reshape(tile, B, spatialSize, gn.NumGroups, groupSize);
        var tileCentered = mlx.Sub(tileFlat, mean);
        var tileNorm = mlx.Mul(tileCentered, invStd);
        if weightGN != null {
        tileNorm = mlx.Mul(tileNorm, weightGN);
    }
        if biasGN != null {
        tileNorm = mlx.Add(tileNorm, biasGN);
    }
        var tileOut = mlx.Reshape(tileNorm, B, tileHeight, W, C);
        mlx.Keep(tileOut);
        mlx.Eval(tileOut);
        tiles = append(tiles, tileOut);
    }
        var result *mlx.Array;
        if len(tiles) == 1 {
        result = tiles[0];
        } else {
        result = mlx.Concatenate(tiles, 1);
        mlx.Eval(result);
        var for _, t = range tiles {
        t.Free();
    }
    }
        if !wasKept {
        x.Free();
    }
        mean.Free();
        invStd.Free();
        if weightGN != null {
        weightGN.Free();
    }
        if biasGN != null {
        biasGN.Free();
    }
        return result;
    }

    public static class Conv2D {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public int32 Stride;
        public int32 Padding;
    }
        func NewConv2D(weight, bias *mlx.Array, stride, padding int32) *Conv2D {
        var weightOHWI = mlx.Transpose(weight, 0, 2, 3, 1);
        return &Conv2D{
        Weight:  weightOHWI,;
        Bias:    bias,;
        Stride:  stride,;
        Padding: padding,;
    }
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

    public static void NewResnetBlock2D(safetensors.WeightSource weights, String prefix) {
        var norm1Weight, err = weights.GetTensor(prefix + ".norm1.weight");
        if err != null {
        return null, err;
    }
        var norm1Bias, err = weights.GetTensor(prefix + ".norm1.bias");
        if err != null {
        return null, err;
    }
        var conv1Weight, err = weights.GetTensor(prefix + ".conv1.weight");
        if err != null {
        return null, err;
    }
        var conv1Bias, err = weights.GetTensor(prefix + ".conv1.bias");
        if err != null {
        return null, err;
    }
        var norm2Weight, err = weights.GetTensor(prefix + ".norm2.weight");
        if err != null {
        return null, err;
    }
        var norm2Bias, err = weights.GetTensor(prefix + ".norm2.bias");
        if err != null {
        return null, err;
    }
        var conv2Weight, err = weights.GetTensor(prefix + ".conv2.weight");
        if err != null {
        return null, err;
    }
        var conv2Bias, err = weights.GetTensor(prefix + ".conv2.bias");
        if err != null {
        return null, err;
    }
        var block = &ResnetBlock2D{
        Norm1: NewGroupNorm(norm1Weight, norm1Bias, numGroups),;
        Conv1: NewConv2D(conv1Weight, conv1Bias, 1, 1),;
        Norm2: NewGroupNorm(norm2Weight, norm2Bias, numGroups),;
        Conv2: NewConv2D(conv2Weight, conv2Bias, 1, 1),;
    }
        if weights.HasTensor(prefix + ".conv_shortcut.weight") {
        var shortcutWeight, err = weights.GetTensor(prefix + ".conv_shortcut.weight");
        if err != null {
        return null, err;
    }
        var shortcutBias, err = weights.GetTensor(prefix + ".conv_shortcut.bias");
        if err != null {
        return null, err;
    }
        block.ConvShortcut = NewConv2D(shortcutWeight, shortcutBias, 1, 0);
    }
        return block, null;
    }
        func (rb *ResnetBlock2D) Forward(x *mlx.Array) *mlx.Array {
        var h *mlx.Array;
        {
        h = rb.Norm1.Forward(x);
        mlx.Eval(h);
    }
        {
        var prev = h;
        h = mlx.SiLU(h);
        h = rb.Conv1.Forward(h);
        prev.Free();
        mlx.Eval(h);
    }
        {
        var prev = h;
        h = rb.Norm2.Forward(h);
        prev.Free();
        mlx.Eval(h);
    }
        {
        var prev = h;
        h = mlx.SiLU(h);
        h = rb.Conv2.Forward(h);
        prev.Free();
        mlx.Eval(h);
    }
        {
        var prev = h;
        if rb.ConvShortcut != null {
        var shortcut = rb.ConvShortcut.Forward(x);
        h = mlx.Add(h, shortcut);
        } else {
        h = mlx.Add(h, x);
    }
        prev.Free();
        mlx.Eval(h);
    }
        return h;
    }

    public static class VAEAttentionBlock {
        public *GroupNormLayer GroupNorm;
        public *mlx.Array ToQWeight;
        public *mlx.Array ToQBias;
        public *mlx.Array ToKWeight;
        public *mlx.Array ToKBias;
        public *mlx.Array ToVWeight;
        public *mlx.Array ToVBias;
        public *mlx.Array ToOutWeight;
        public *mlx.Array ToOutBias;
        public int32 NumHeads;
    }

    public static void NewVAEAttentionBlock(safetensors.WeightSource weights, String prefix) {
        var normWeight, err = weights.GetTensor(prefix + ".group_norm.weight");
        if err != null {
        return null, err;
    }
        var normBias, err = weights.GetTensor(prefix + ".group_norm.bias");
        if err != null {
        return null, err;
    }
        var toQWeight, err = weights.GetTensor(prefix + ".to_q.weight");
        if err != null {
        return null, err;
    }
        var toQBias, err = weights.GetTensor(prefix + ".to_q.bias");
        if err != null {
        return null, err;
    }
        var toKWeight, err = weights.GetTensor(prefix + ".to_k.weight");
        if err != null {
        return null, err;
    }
        var toKBias, err = weights.GetTensor(prefix + ".to_k.bias");
        if err != null {
        return null, err;
    }
        var toVWeight, err = weights.GetTensor(prefix + ".to_v.weight");
        if err != null {
        return null, err;
    }
        var toVBias, err = weights.GetTensor(prefix + ".to_v.bias");
        if err != null {
        return null, err;
    }
        var toOutWeight, err = weights.GetTensor(prefix + ".to_out.0.weight");
        if err != null {
        return null, err;
    }
        var toOutBias, err = weights.GetTensor(prefix + ".to_out.0.bias");
        if err != null {
        return null, err;
    }
        return &VAEAttentionBlock{
        GroupNorm:   NewGroupNorm(normWeight, normBias, numGroups),;
        ToQWeight:   mlx.Transpose(toQWeight, 1, 0),;
        ToQBias:     toQBias,;
        ToKWeight:   mlx.Transpose(toKWeight, 1, 0),;
        ToKBias:     toKBias,;
        ToVWeight:   mlx.Transpose(toVWeight, 1, 0),;
        ToVBias:     toVBias,;
        ToOutWeight: mlx.Transpose(toOutWeight, 1, 0),;
        ToOutBias:   toOutBias,;
        NumHeads:    1,;
        }, null;
    }
        func (ab *VAEAttentionBlock) Forward(x *mlx.Array) *mlx.Array {
        var residual = x;
        var shape = x.Shape();
        var B = shape[0];
        var H = shape[1];
        var W = shape[2];
        var C = shape[3];
        var h *mlx.Array;
        {
        h = ab.GroupNorm.Forward(x);
        h = mlx.Reshape(h, B, H*W, C);
        mlx.Eval(h);
    }
        var out *mlx.Array;
        {
        var q = mlx.Linear(h, ab.ToQWeight);
        q = mlx.Add(q, ab.ToQBias);
        var k = mlx.Linear(h, ab.ToKWeight);
        k = mlx.Add(k, ab.ToKBias);
        var v = mlx.Linear(h, ab.ToVWeight);
        v = mlx.Add(v, ab.ToVBias);
        h.Free();
        q = mlx.ExpandDims(q, 1);
        k = mlx.ExpandDims(k, 1);
        v = mlx.ExpandDims(v, 1);
        var scale = float32(1.0 / math.Sqrt(double(C)));
        out = mlx.ScaledDotProductAttention(q, k, v, scale, false);
        out = mlx.Squeeze(out, 1);
        mlx.Eval(out);
    }
        {
        var prev = out;
        out = mlx.Linear(out, ab.ToOutWeight);
        out = mlx.Add(out, ab.ToOutBias);
        out = mlx.Reshape(out, B, H, W, C);
        out = mlx.Add(out, residual);
        prev.Free();
        mlx.Eval(out);
    }
        return out;
    }

    public static class UpDecoderBlock2D {
        public []*ResnetBlock2D ResnetBlocks;
        public *Conv2D Upsample;
    }

    public static void NewUpDecoderBlock2D(safetensors.WeightSource weights, String prefix, int32 numGroups) {
        var resnets = make([]*ResnetBlock2D, numLayers);
        var for i = int32(0); i < numLayers; i++ {
        var resPrefix = fmt.Sprintf("%s.resnets.%d", prefix, i);
        var resnet, err = NewResnetBlock2D(weights, resPrefix, numGroups);
        if err != null {
        return null, err;
    }
        resnets[i] = resnet;
    }
        var upsample *Conv2D;
        if hasUpsample {
        var upWeight, err = weights.GetTensor(prefix + ".upsamplers.0.conv.weight");
        if err != null {
        return null, err;
    }
        var upBias, err = weights.GetTensor(prefix + ".upsamplers.0.conv.bias");
        if err != null {
        return null, err;
    }
        upsample = NewConv2D(upWeight, upBias, 1, 1);
    }
        return &UpDecoderBlock2D{
        ResnetBlocks: resnets,;
        Upsample:     upsample,;
        }, null;
    }
        func (ub *UpDecoderBlock2D) Forward(x *mlx.Array) *mlx.Array {
        var for _, resnet = range ub.ResnetBlocks {
        var prev = x;
        x = resnet.Forward(x) // ResNet handles its own pools;
        prev.Free();
    }
        if ub.Upsample != null {
        {
        var prev = x;
        x = Upsample2x(x);
        prev.Free();
        mlx.Eval(x);
    }
        {
        var prev = x;
        x = ub.Upsample.Forward(x);
        prev.Free();
        mlx.Eval(x);
    }
    }
        return x;
    }

    public static class VAEMidBlock {
        public *ResnetBlock2D Resnet1;
        public *VAEAttentionBlock Attention;
        public *ResnetBlock2D Resnet2;
    }

    public static void NewVAEMidBlock(safetensors.WeightSource weights, String prefix) {
        var resnet1, err = NewResnetBlock2D(weights, prefix+".resnets.0", numGroups);
        if err != null {
        return null, err;
    }
        var attention, err = NewVAEAttentionBlock(weights, prefix+".attentions.0", numGroups);
        if err != null {
        return null, err;
    }
        var resnet2, err = NewResnetBlock2D(weights, prefix+".resnets.1", numGroups);
        if err != null {
        return null, err;
    }
        return &VAEMidBlock{
        Resnet1:   resnet1,;
        Attention: attention,;
        Resnet2:   resnet2,;
        }, null;
    }
        func (mb *VAEMidBlock) Forward(x *mlx.Array) *mlx.Array {
        var prev = x;
        x = mb.Resnet1.Forward(x) // ResNet handles its own pools;
        prev.Free();
        prev = x;
        x = mb.Attention.Forward(x);
        prev.Free();
        prev = x;
        x = mb.Resnet2.Forward(x) // ResNet handles its own pools;
        prev.Free();
        return x;
    }

    public static class VAEDecoder {
        public *VAEConfig Config;
        public *Conv2D ConvIn;
        public *VAEMidBlock MidBlock;
        public []*UpDecoderBlock2D UpBlocks;
        public *GroupNormLayer ConvNormOut;
        public *Conv2D ConvOut;
        public *vae.TilingConfig Tiling;
    }
        func (m *VAEDecoder) Load(modelManifest *manifest.ModelManifest) error {
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
        func (m *VAEDecoder) loadWeights(weights safetensors.WeightSource, cfg *VAEConfig) error {
        var err error;
        fmt.Print("  Loading conv_in... ");
        var convInWeight, err = weights.GetTensor("decoder.conv_in.weight");
        if err != null {
        return err;
    }
        var convInBias, err = weights.GetTensor("decoder.conv_in.bias");
        if err != null {
        return err;
    }
        m.ConvIn = NewConv2D(convInWeight, convInBias, 1, 1);
        System.out.println("✓");
        fmt.Print("  Loading mid block... ");
        m.MidBlock, err = NewVAEMidBlock(weights, "decoder.mid_block", cfg.NormNumGroups);
        if err != null {
        return err;
    }
        System.out.println("✓");
        fmt.Print("  Loading up blocks... ");
        var numBlocks = len(cfg.BlockOutChannels);
        m.UpBlocks = make([]*UpDecoderBlock2D, numBlocks);
        var for i = 0; i < numBlocks; i++ {
        var prefix = fmt.Sprintf("decoder.up_blocks.%d", i);
        var hasUpsample = i < numBlocks-1;
        m.UpBlocks[i], err = NewUpDecoderBlock2D(weights, prefix, cfg.LayersPerBlock+1, cfg.NormNumGroups, hasUpsample);
        if err != null {
        return err;
    }
    }
        System.out.printf("✓ [%d blocks]\n", numBlocks);
        fmt.Print("  Loading conv_norm_out... ");
        var normWeight, err = weights.GetTensor("decoder.conv_norm_out.weight");
        if err != null {
        return err;
    }
        var normBias, err = weights.GetTensor("decoder.conv_norm_out.bias");
        if err != null {
        return err;
    }
        m.ConvNormOut = NewGroupNorm(normWeight, normBias, cfg.NormNumGroups);
        System.out.println("✓");
        fmt.Print("  Loading conv_out... ");
        var convOutWeight, err = weights.GetTensor("decoder.conv_out.weight");
        if err != null {
        return err;
    }
        var convOutBias, err = weights.GetTensor("decoder.conv_out.bias");
        if err != null {
        return err;
    }
        m.ConvOut = NewConv2D(convOutWeight, convOutBias, 1, 1);
        System.out.println("✓");
        return null;
    }
        func (v *VAEDecoder) Decode(latents *mlx.Array) *mlx.Array {
        var z = mlx.DivScalar(latents, v.Config.ScalingFactor);
        z = mlx.AddScalar(z, v.Config.ShiftFactor);
        z = mlx.Transpose(z, 0, 2, 3, 1);
        if v.Tiling != null {
        mlx.Eval(z);
        return vae.DecodeTiled(z, v.Tiling, v.decodeTile);
    }
        var h = v.decodeTile(z);
        h = mlx.ClipScalar(h, 0.0, 1.0, true, true);
        h = mlx.Transpose(h, 0, 3, 1, 2);
        mlx.Eval(h);
        return h;
    }
        func (v *VAEDecoder) decodeTile(z *mlx.Array) *mlx.Array {
        var h = v.ConvIn.Forward(z);
        mlx.Eval(h);
        var prev = h;
        h = v.MidBlock.Forward(h);
        prev.Free();
        var for _, upBlock = range v.UpBlocks {
        prev = h;
        h = upBlock.Forward(h);
        prev.Free();
    }
        prev = h;
        h = v.ConvNormOut.Forward(h);
        mlx.Eval(h) // Eval after GroupNorm to avoid grid dimension issues;
        prev.Free();
        prev = h;
        h = mlx.SiLU(h);
        h = v.ConvOut.Forward(h);
        mlx.Eval(h);
        prev.Free();
        h = mlx.MulScalar(h, 0.5);
        h = mlx.AddScalar(h, 0.5);
        return h;
    }
        func Upsample2x(x *mlx.Array) *mlx.Array {
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
}
