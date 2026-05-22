package com.fraymus.absorbed.imagegen.models.zimage;

import java.util.*;
import java.io.*;

public class transformer {
        "fmt";
        "math";
        "github.com/ollama/ollama/x/imagegen/cache";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/nn";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        );

    public static class TransformerConfig {
        public int32 Dim;
        public int32 NHeads;
        public int32 NKVHeads;
        public int32 NLayers;
        public int32 NRefinerLayers;
        public int32 InChannels;
        public int32 PatchSize;
        public int32 CapFeatDim;
        public float32 NormEps;
        public float32 RopeTheta;
        public float32 TScale;
        public boolean QKNorm;
        public []int32 AxesDims;
        public []int32 AxesLens;
        public []int32 AllPatchSize;
    }

    public static class TimestepEmbedder {
        public nn.LinearLayer Linear1;
        public nn.LinearLayer Linear2;
        public int32 FreqEmbedSize;
    }
        func (te *TimestepEmbedder) Forward(t *mlx.Array) *mlx.Array {
        var half = te.FreqEmbedSize / 2;
        var freqs = make([]float32, half);
        var for i = int32(0); i < half; i++ {
        freqs[i] = float32(math.Exp(-math.Log(10000.0) * double(i) / double(half)));
    }
        var freqsArr = mlx.NewArray(freqs, []int32{1, half});
        var tExpanded = mlx.ExpandDims(t, 1) // [B, 1];
        var args = mlx.Mul(tExpanded, freqsArr);
        var cosArgs = mlx.Cos(args);
        var sinArgs = mlx.Sin(args);
        var embedding = mlx.Concatenate([]*mlx.Array{cosArgs, sinArgs}, 1);
        var h = te.Linear1.Forward(embedding);
        h = mlx.SiLU(h);
        h = te.Linear2.Forward(h);
        return h;
    }

    public static class XEmbedder {
        public nn.LinearLayer Linear;
    }
        func (xe *XEmbedder) Forward(x *mlx.Array) *mlx.Array {
        return xe.Linear.Forward(x);
    }

    public static class CapEmbedder {
        public *nn.RMSNorm Norm;
        public nn.LinearLayer Linear;
        public *mlx.Array PadToken;
    }
        func (ce *CapEmbedder) Forward(capFeats *mlx.Array) *mlx.Array {
        var h = ce.Norm.Forward(capFeats, 1e-6);
        return ce.Linear.Forward(h);
    }

    public static class FeedForward {
        public nn.LinearLayer W1;
        public nn.LinearLayer W2;
        public nn.LinearLayer W3;
        public int32 OutDim;
    }
        func (ff *FeedForward) Forward(x *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var D = shape[2];
        x = mlx.Reshape(x, B*L, D);
        var gate = ff.W1.Forward(x);
        gate = mlx.SiLU(gate);
        var up = ff.W3.Forward(x);
        var h = mlx.Mul(gate, up);
        var out = ff.W2.Forward(h);
        return mlx.Reshape(out, B, L, ff.OutDim);
    }

    public static class Attention {
        public nn.LinearLayer ToQ;
        public nn.LinearLayer ToK;
        public nn.LinearLayer ToV;
        public nn.LinearLayer ToOut;
        public *mlx.Array NormQ;
        public *mlx.Array NormK;
        public nn.LinearLayer ToQKV;
        public boolean Fused;
        public int32 NHeads;
        public int32 HeadDim;
        public int32 Dim;
        public float32 Scale;
    }
        func (attn *Attention) FuseQKV() {
        if attn.ToQ == null || attn.ToK == null || attn.ToV == null {
        return;
    }
        var toQ, qOk = attn.ToQ.(*nn.Linear);
        var toK, kOk = attn.ToK.(*nn.Linear);
        var toV, vOk = attn.ToV.(*nn.Linear);
        if !qOk || !kOk || !vOk {
        return;
    }
        if toQ.Weight == null || toK.Weight == null || toV.Weight == null {
        return;
    }
        var qWeight = toQ.Weight;
        var kWeight = toK.Weight;
        var vWeight = toV.Weight;
        var fusedWeight = mlx.Concatenate([]*mlx.Array{qWeight, kWeight, vWeight}, 0);
        mlx.Eval(fusedWeight);
        var fusedLinear = &nn.Linear{Weight: fusedWeight}
        if toQ.Bias != null && toK.Bias != null && toV.Bias != null {
        var fusedBias = mlx.Concatenate([]*mlx.Array{toQ.Bias, toK.Bias, toV.Bias}, 0);
        mlx.Eval(fusedBias);
        fusedLinear.Bias = fusedBias;
    }
        attn.ToQKV = fusedLinear;
        attn.Fused = true;
    }
        func (attn *Attention) Forward(x *mlx.Array, cos, sin *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var D = shape[2];
        var xFlat = mlx.Reshape(x, B*L, D);
        var q, k, v *mlx.Array;
        if attn.Fused && attn.ToQKV != null {
        var qkv = attn.ToQKV.Forward(xFlat) // [B*L, 3*dim];
        q = mlx.Slice(qkv, []int32{0, 0}, []int32{B * L, attn.Dim});
        k = mlx.Slice(qkv, []int32{0, attn.Dim}, []int32{B * L, 2 * attn.Dim});
        v = mlx.Slice(qkv, []int32{0, 2 * attn.Dim}, []int32{B * L, 3 * attn.Dim});
        } else {
        q = attn.ToQ.Forward(xFlat);
        k = attn.ToK.Forward(xFlat);
        v = attn.ToV.Forward(xFlat);
    }
        q = mlx.Reshape(q, B, L, attn.NHeads, attn.HeadDim);
        k = mlx.Reshape(k, B, L, attn.NHeads, attn.HeadDim);
        v = mlx.Reshape(v, B, L, attn.NHeads, attn.HeadDim);
        q = mlx.RMSNorm(q, attn.NormQ, 1e-5);
        k = mlx.RMSNorm(k, attn.NormK, 1e-5);
        if cos != null && sin != null {
        q = applyRoPE3D(q, cos, sin);
        k = applyRoPE3D(k, cos, sin);
    }
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        var out = mlx.ScaledDotProductAttention(q, k, v, attn.Scale, false);
        out = mlx.Transpose(out, 0, 2, 1, 3);
        out = mlx.Reshape(out, B*L, attn.Dim);
        out = attn.ToOut.Forward(out);
        return mlx.Reshape(out, B, L, attn.Dim);
    }
        func applyRoPE3D(x *mlx.Array, cos, sin *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var nheads = shape[2];
        var headDim = shape[3];
        var half = headDim / 2;
        var evenIdx = make([]int32, half);
        var oddIdx = make([]int32, half);
        var for i = int32(0); i < half; i++ {
        evenIdx[i] = i * 2;
        oddIdx[i] = i*2 + 1;
    }
        var evenIndices = mlx.NewArrayInt32(evenIdx, []int32{half});
        var oddIndices = mlx.NewArrayInt32(oddIdx, []int32{half});
        var x1 = mlx.Take(x, evenIndices, 3) // [B, L, nheads, half];
        var x2 = mlx.Take(x, oddIndices, 3)  // [B, L, nheads, half];
        var r1 = mlx.Sub(mlx.Mul(x1, cos), mlx.Mul(x2, sin));
        var r2 = mlx.Add(mlx.Mul(x1, sin), mlx.Mul(x2, cos));
        r1 = mlx.ExpandDims(r1, 4)                          // [B, L, nheads, half, 1];
        r2 = mlx.ExpandDims(r2, 4)                          // [B, L, nheads, half, 1];
        var stacked = mlx.Concatenate([]*mlx.Array{r1, r2}, 4) // [B, L, nheads, half, 2];
        return mlx.Reshape(stacked, B, L, nheads, headDim);
    }

    public static class TransformerBlock {
        public *Attention Attention;
        public *FeedForward FeedForward;
        public *nn.RMSNorm AttentionNorm1;
        public *nn.RMSNorm AttentionNorm2;
        public *nn.RMSNorm FFNNorm1;
        public *nn.RMSNorm FFNNorm2;
        public nn.LinearLayer AdaLN;
        public boolean HasModulation;
        public int32 Dim;
    }
        func (tb *TransformerBlock) Forward(x *mlx.Array, adaln *mlx.Array, cos, sin *mlx.Array, eps float32) *mlx.Array {
        if tb.AdaLN != null && adaln != null {
        var chunks = tb.AdaLN.Forward(adaln);
        var chunkShape = chunks.Shape();
        var chunkDim = chunkShape[1] / 4;
        var scaleMSA = mlx.Slice(chunks, []int32{0, 0}, []int32{chunkShape[0], chunkDim});
        var gateMSA = mlx.Slice(chunks, []int32{0, chunkDim}, []int32{chunkShape[0], chunkDim * 2});
        var scaleMLP = mlx.Slice(chunks, []int32{0, chunkDim * 2}, []int32{chunkShape[0], chunkDim * 3});
        var gateMLP = mlx.Slice(chunks, []int32{0, chunkDim * 3}, []int32{chunkShape[0], chunkDim * 4});
        scaleMSA = mlx.ExpandDims(scaleMSA, 1);
        gateMSA = mlx.ExpandDims(gateMSA, 1);
        scaleMLP = mlx.ExpandDims(scaleMLP, 1);
        gateMLP = mlx.ExpandDims(gateMLP, 1);
        var normX = tb.AttentionNorm1.Forward(x, eps);
        normX = mlx.Mul(normX, mlx.AddScalar(scaleMSA, 1.0));
        var attnOut = tb.Attention.Forward(normX, cos, sin);
        attnOut = tb.AttentionNorm2.Forward(attnOut, eps);
        x = mlx.Add(x, mlx.Mul(mlx.Tanh(gateMSA), attnOut));
        var normFFN = tb.FFNNorm1.Forward(x, eps);
        normFFN = mlx.Mul(normFFN, mlx.AddScalar(scaleMLP, 1.0));
        var ffnOut = tb.FeedForward.Forward(normFFN);
        ffnOut = tb.FFNNorm2.Forward(ffnOut, eps);
        x = mlx.Add(x, mlx.Mul(mlx.Tanh(gateMLP), ffnOut));
        } else {
        var attnOut = tb.Attention.Forward(tb.AttentionNorm1.Forward(x, eps), cos, sin);
        x = mlx.Add(x, tb.AttentionNorm2.Forward(attnOut, eps));
        var ffnOut = tb.FeedForward.Forward(tb.FFNNorm1.Forward(x, eps));
        x = mlx.Add(x, tb.FFNNorm2.Forward(ffnOut, eps));
    }
        return x;
    }

    public static class FinalLayer {
        public nn.LinearLayer AdaLN;
        public nn.LinearLayer Output;
        public int32 OutDim;
    }
        func (fl *FinalLayer) Forward(x *mlx.Array, c *mlx.Array) *mlx.Array {
        var scale = mlx.SiLU(c);
        scale = fl.AdaLN.Forward(scale);
        scale = mlx.ExpandDims(scale, 1) // [B, 1, dim];
        x = layerNormNoAffine(x, 1e-6);
        x = mlx.Mul(x, mlx.AddScalar(scale, 1.0));
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var D = shape[2];
        x = mlx.Reshape(x, B*L, D);
        x = fl.Output.Forward(x);
        return mlx.Reshape(x, B, L, fl.OutDim);
    }
        func layerNormNoAffine(x *mlx.Array, eps float32) *mlx.Array {
        var ndim = x.Ndim();
        var lastAxis = ndim - 1;
        var mean = mlx.Mean(x, lastAxis, true);
        var xCentered = mlx.Sub(x, mean);
        var variance = mlx.Mean(mlx.Square(xCentered), lastAxis, true);
        return mlx.Div(xCentered, mlx.Sqrt(mlx.AddScalar(variance, eps)));
    }

    public static class Transformer {
        public *TimestepEmbedder TEmbed;
        public *XEmbedder XEmbed;
        public *CapEmbedder CapEmbed;
        public []*TransformerBlock NoiseRefiners;
        public []*TransformerBlock ContextRefiners;
        public []*TransformerBlock Layers;
        public *FinalLayer FinalLayer;
        public *mlx.Array XPadToken;
        public *mlx.Array CapPadToken;
    }
        func (m *Transformer) Load(modelManifest *manifest.ModelManifest) error {
        fmt.Print("  Loading transformer... ");
        var cfg TransformerConfig;
        var if err = modelManifest.ReadConfigJSON("transformer/config.json", &cfg); err != null {
        return fmt.Errorf("config: %w", err);
    }
        if len(cfg.AllPatchSize) > 0 {
        cfg.PatchSize = cfg.AllPatchSize[0];
    }
        m.TransformerConfig = &cfg;
        m.NoiseRefiners = make([]*TransformerBlock, cfg.NRefinerLayers);
        m.ContextRefiners = make([]*TransformerBlock, cfg.NRefinerLayers);
        m.Layers = make([]*TransformerBlock, cfg.NLayers);
        var weights, err = manifest.LoadWeightsFromManifest(modelManifest, "transformer");
        if err != null {
        return fmt.Errorf("weights: %w", err);
    }
        var if err = weights.Load(0); err != null {
        return fmt.Errorf("load weights: %w", err);
    }
        defer weights.ReleaseAll();
        return m.loadWeights(weights);
    }
        func (m *Transformer) loadWeights(weights safetensors.WeightSource) error {
        var if err = safetensors.LoadModule(m, weights, ""); err != null {
        return fmt.Errorf("load module: %w", err);
    }
        m.initComputedFields();
        System.out.println("✓");
        return null;
    }
        func (m *Transformer) initComputedFields() {
        var cfg = m.TransformerConfig;
        m.TEmbed.FreqEmbedSize = 256;
        m.FinalLayer.OutDim = m.FinalLayer.Output.OutputDim();
        m.CapEmbed.Norm.Eps = 1e-6;
        var for _, block = range m.NoiseRefiners {
        initTransformerBlock(block, cfg);
    }
        var for _, block = range m.ContextRefiners {
        initTransformerBlock(block, cfg);
    }
        var for _, block = range m.Layers {
        initTransformerBlock(block, cfg);
    }
    }
        func (m *Transformer) FuseAllQKV() {
        var for _, block = range m.NoiseRefiners {
        block.Attention.FuseQKV();
    }
        var for _, block = range m.ContextRefiners {
        block.Attention.FuseQKV();
    }
        var for _, block = range m.Layers {
        block.Attention.FuseQKV();
    }
    }

    public static void initTransformerBlock(*TransformerBlock block, *TransformerConfig cfg) {
        block.Dim = cfg.Dim;
        block.HasModulation = block.AdaLN != null;
        var attn = block.Attention;
        attn.NHeads = cfg.NHeads;
        attn.HeadDim = cfg.Dim / cfg.NHeads;
        attn.Dim = cfg.Dim;
        attn.Scale = float32(1.0 / math.Sqrt(double(attn.HeadDim)));
        block.FeedForward.OutDim = block.FeedForward.W2.OutputDim();
        block.AttentionNorm1.Eps = cfg.NormEps;
        block.AttentionNorm2.Eps = cfg.NormEps;
        block.FFNNorm1.Eps = cfg.NormEps;
        block.FFNNorm2.Eps = cfg.NormEps;
    }

    public static class RoPECache {
        public *mlx.Array ImgCos;
        public *mlx.Array ImgSin;
        public *mlx.Array CapCos;
        public *mlx.Array CapSin;
        public *mlx.Array UnifiedCos;
        public *mlx.Array UnifiedSin;
        public int32 ImgLen;
        public int32 CapLen;
        public int32 GridH;
        public int32 GridW;
    }
        func (m *Transformer) PrepareRoPECache(hTok, wTok, capLen int32) *RoPECache {
        var imgLen = hTok * wTok;
        var imgPos = createCoordinateGrid(1, hTok, wTok, capLen+1, 0, 0);
        imgPos = mlx.ToBFloat16(imgPos);
        var capPos = createCoordinateGrid(capLen, 1, 1, 1, 0, 0);
        capPos = mlx.ToBFloat16(capPos);
        var unifiedPos = mlx.Concatenate([]*mlx.Array{imgPos, capPos}, 1);
        var unifiedCos, unifiedSin = prepareRoPE3D(unifiedPos, m.TransformerConfig.AxesDims);
        var imgCos = mlx.Slice(unifiedCos, []int32{0, 0, 0, 0}, []int32{1, imgLen, 1, 64});
        var imgSin = mlx.Slice(unifiedSin, []int32{0, 0, 0, 0}, []int32{1, imgLen, 1, 64});
        var capCos = mlx.Slice(unifiedCos, []int32{0, imgLen, 0, 0}, []int32{1, imgLen + capLen, 1, 64});
        var capSin = mlx.Slice(unifiedSin, []int32{0, imgLen, 0, 0}, []int32{1, imgLen + capLen, 1, 64});
        return &RoPECache{
        ImgCos:     imgCos,;
        ImgSin:     imgSin,;
        CapCos:     capCos,;
        CapSin:     capSin,;
        UnifiedCos: unifiedCos,;
        UnifiedSin: unifiedSin,;
        ImgLen:     imgLen,;
        CapLen:     capLen,;
        GridH:      hTok,;
        GridW:      wTok,;
    }
    }
        func (m *Transformer) Forward(x *mlx.Array, t *mlx.Array, capFeats *mlx.Array, rope *RoPECache) *mlx.Array {
        var imgLen = rope.ImgLen;
        var temb = m.TEmbed.Forward(mlx.MulScalar(t, m.TransformerConfig.TScale));
        x = m.XEmbed.Forward(x);
        var capEmb = m.CapEmbed.Forward(capFeats);
        var eps = m.NormEps;
        var for _, refiner = range m.NoiseRefiners {
        x = refiner.Forward(x, temb, rope.ImgCos, rope.ImgSin, eps);
    }
        var for _, refiner = range m.ContextRefiners {
        capEmb = refiner.Forward(capEmb, null, rope.CapCos, rope.CapSin, eps);
    }
        var unified = mlx.Concatenate([]*mlx.Array{x, capEmb}, 1);
        var for _, layer = range m.Layers {
        unified = layer.Forward(unified, temb, rope.UnifiedCos, rope.UnifiedSin, eps);
    }
        var unifiedShape = unified.Shape();
        var B = unifiedShape[0];
        var imgOut = mlx.Slice(unified, []int32{0, 0, 0}, []int32{B, imgLen, unifiedShape[2]});
        return m.FinalLayer.Forward(imgOut, temb);
    }
        func (m *Transformer) ForwardWithCache(;
        x *mlx.Array,;
        t *mlx.Array,;
        capFeats *mlx.Array,;
        rope *RoPECache,;
        stepCache *cache.StepCache,;
        step int,;
        cacheInterval int,;
        ) *mlx.Array {
        var imgLen = rope.ImgLen;
        var cacheLayers = stepCache.NumLayers();
        var eps = m.NormEps;
        var temb = m.TEmbed.Forward(mlx.MulScalar(t, m.TransformerConfig.TScale));
        x = m.XEmbed.Forward(x);
        var capEmb *mlx.Array;
        if stepCache.GetConstant() != null {
        capEmb = stepCache.GetConstant();
        } else {
        capEmb = m.CapEmbed.Forward(capFeats);
        var for _, refiner = range m.ContextRefiners {
        capEmb = refiner.Forward(capEmb, null, rope.CapCos, rope.CapSin, eps);
    }
        stepCache.SetConstant(capEmb);
    }
        var for _, refiner = range m.NoiseRefiners {
        x = refiner.Forward(x, temb, rope.ImgCos, rope.ImgSin, eps);
    }
        var unified = mlx.Concatenate([]*mlx.Array{x, capEmb}, 1);
        var refreshCache = stepCache.ShouldRefresh(step, cacheInterval);
        var for i, layer = range m.Layers {
        if i < cacheLayers && !refreshCache && stepCache.Get(i) != null {
        unified = stepCache.Get(i);
        } else {
        unified = layer.Forward(unified, temb, rope.UnifiedCos, rope.UnifiedSin, eps);
        if i < cacheLayers && refreshCache {
        stepCache.Set(i, unified);
    }
    }
    }
        var unifiedShape = unified.Shape();
        var B = unifiedShape[0];
        var imgOut = mlx.Slice(unified, []int32{0, 0, 0}, []int32{B, imgLen, unifiedShape[2]});
        return m.FinalLayer.Forward(imgOut, temb);
    }
        func createCoordinateGrid(d0, d1, d2, s0, s1, s2 int32) *mlx.Array {
        var total = d0 * d1 * d2;
        var coords = make([]float32, total*3);
        var idx = 0;
        var for i = int32(0); i < d0; i++ {
        var for j = int32(0); j < d1; j++ {
        var for k = int32(0); k < d2; k++ {
        coords[idx*3+0] = float32(s0 + i);
        coords[idx*3+1] = float32(s1 + j);
        coords[idx*3+2] = float32(s2 + k);
        idx++;
    }
    }
    }
        return mlx.NewArray(coords, []int32{1, total, 3});
    }

    public static void prepareRoPE3D(*mlx.Array positions) {
        var ropeTheta = float32(256.0);
        var freqs = make([]*mlx.Array, 3);
        var for axis = 0; axis < 3; axis++ {
        var half = axesDims[axis] / 2;
        var f = make([]float32, half);
        var for i = int32(0); i < half; i++ {
        f[i] = float32(math.Exp(-math.Log(double(ropeTheta)) * double(i) / double(half)));
    }
        freqs[axis] = mlx.NewArray(f, []int32{1, 1, 1, half});
    }
        var shape = positions.Shape();
        var B = shape[0];
        var L = shape[1];
        var posH = mlx.Slice(positions, []int32{0, 0, 0}, []int32{B, L, 1});
        var posW = mlx.Slice(positions, []int32{0, 0, 1}, []int32{B, L, 2});
        var posT = mlx.Slice(positions, []int32{0, 0, 2}, []int32{B, L, 3});
        posH = mlx.ExpandDims(posH, 3) // [B, L, 1, 1];
        posW = mlx.ExpandDims(posW, 3);
        posT = mlx.ExpandDims(posT, 3);
        var argsH = mlx.Mul(posH, freqs[0]) // [B, L, 1, 16];
        var argsW = mlx.Mul(posW, freqs[1]) // [B, L, 1, 24];
        var argsT = mlx.Mul(posT, freqs[2]) // [B, L, 1, 24];
        var args = mlx.Concatenate([]*mlx.Array{argsH, argsW, argsT}, 3);
        return mlx.Cos(args), mlx.Sin(args);
    }
        func PatchifyLatents(latents *mlx.Array, patchSize int32) *mlx.Array {
        var shape = latents.Shape();
        var C = shape[1];
        var H = shape[2];
        var W = shape[3];
        var pH = H / patchSize // H_tok;
        var pW = W / patchSize // W_tok;
        var x = mlx.Reshape(latents, C, 1, 1, pH, patchSize, pW, patchSize);
        x = mlx.Transpose(x, 1, 2, 3, 5, 4, 6, 0);
        return mlx.Reshape(x, 1, pH*pW, C*patchSize*patchSize);
    }
        func UnpatchifyLatents(patches *mlx.Array, patchSize, H, W, C int32) *mlx.Array {
        var pH = H / patchSize;
        var pW = W / patchSize;
        var x = mlx.Reshape(patches, 1, 1, pH, pW, patchSize, patchSize, C);
        x = mlx.Transpose(x, 6, 0, 1, 2, 4, 3, 5);
        return mlx.Reshape(x, 1, C, H, W);
    }
}
