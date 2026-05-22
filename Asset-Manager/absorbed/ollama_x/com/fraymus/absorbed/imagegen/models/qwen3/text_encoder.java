package com.fraymus.absorbed.imagegen.models.qwen3;

import java.util.*;
import java.io.*;

public class text_encoder {
        "fmt";
        "math";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/nn";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        "github.com/ollama/ollama/x/imagegen/tokenizer";
        );

    public static class Config {
        public int32 HiddenSize;
        public int32 NumHiddenLayers;
        public int32 IntermediateSize;
        public int32 NumAttentionHeads;
        public int32 NumKeyValueHeads;
        public int32 VocabSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public int32 HeadDim;
    }

    public static class Attention {
        public nn.LinearLayer QProj;
        public nn.LinearLayer KProj;
        public nn.LinearLayer VProj;
        public nn.LinearLayer OProj;
        public *nn.RMSNorm QNorm;
        public *nn.RMSNorm KNorm;
        public int32 NHeads;
        public int32 NKVHeads;
        public int32 HeadDim;
        public float32 Scale;
        public float32 RopeTheta;
    }
        func applyRoPEQwen3(x *mlx.Array, seqLen int32, theta float32) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var H = shape[2];
        var D = shape[3];
        var half = D / 2;
        var freqsArr = make([]float32, half);
        var logTheta = float32(math.Log(double(theta)));
        var for i = int32(0); i < half; i++ {
        freqsArr[i] = float32(math.Exp(double(-logTheta * float32(i) / float32(half))));
    }
        var freqs = mlx.NewArray(freqsArr, []int32{half});
        var posArr = make([]float32, seqLen);
        var for i = int32(0); i < seqLen; i++ {
        posArr[i] = float32(i);
    }
        var pos = mlx.NewArray(posArr, []int32{seqLen});
        var posExpanded = mlx.Reshape(pos, seqLen, 1);
        var freqsExpanded = mlx.Reshape(freqs, 1, half);
        var args = mlx.Mul(posExpanded, freqsExpanded);
        var cosVals = mlx.Cos(args);
        var sinVals = mlx.Sin(args);
        cosVals = mlx.Reshape(cosVals, seqLen, 1, half);
        sinVals = mlx.Reshape(sinVals, seqLen, 1, half);
        var x1 = mlx.Slice(x, []int32{0, 0, 0, 0}, []int32{B, L, H, half});
        var x2 = mlx.Slice(x, []int32{0, 0, 0, half}, []int32{B, L, H, D});
        var part1 = mlx.Sub(mlx.Mul(x1, cosVals), mlx.Mul(x2, sinVals));
        var part2 = mlx.Add(mlx.Mul(x1, sinVals), mlx.Mul(x2, cosVals));
        return mlx.Concatenate([]*mlx.Array{part1, part2}, 3);
    }
        func (attn *Attention) Forward(x *mlx.Array, mask *mlx.Array, maskMode String) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var q = attn.QProj.Forward(x);
        var k = attn.KProj.Forward(x);
        var v = attn.VProj.Forward(x);
        q = mlx.Reshape(q, B, L, attn.NHeads, attn.HeadDim);
        k = mlx.Reshape(k, B, L, attn.NKVHeads, attn.HeadDim);
        v = mlx.Reshape(v, B, L, attn.NKVHeads, attn.HeadDim);
        q = attn.QNorm.Forward(q, 1e-6);
        k = attn.KNorm.Forward(k, 1e-6);
        q = applyRoPEQwen3(q, L, attn.RopeTheta);
        k = applyRoPEQwen3(k, L, attn.RopeTheta);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        if attn.NKVHeads < attn.NHeads {
        var repeats = attn.NHeads / attn.NKVHeads;
        k = repeatKV(k, repeats);
        v = repeatKV(v, repeats);
    }
        var out = mlx.ScaledDotProductAttentionWithSinks(q, k, v, attn.Scale, maskMode, mask, null);
        out = mlx.Transpose(out, 0, 2, 1, 3);
        out = mlx.Reshape(out, B, L, attn.NHeads*attn.HeadDim);
        out = attn.OProj.Forward(out);
        return out;
    }
        func repeatKV(x *mlx.Array, repeats int32) *mlx.Array {
        if repeats == 1 {
        return x;
    }
        var shape = x.Shape();
        x = mlx.ExpandDims(x, 2);
        x = mlx.Tile(x, []int32{1, 1, repeats, 1, 1});
        return mlx.Reshape(x, shape[0], shape[1]*repeats, shape[2], shape[3]);
    }

    public static class MLP {
        public nn.LinearLayer GateProj;
        public nn.LinearLayer UpProj;
        public nn.LinearLayer DownProj;
    }
        func (m *MLP) Forward(x *mlx.Array) *mlx.Array {
        var gate = m.GateProj.Forward(x);
        gate = mlx.SiLU(gate);
        var up = m.UpProj.Forward(x);
        var h = mlx.Mul(gate, up);
        return m.DownProj.Forward(h);
    }

    public static class Block {
        public *Attention Attention;
        public *MLP MLP;
        public *nn.RMSNorm InputLayerNorm;
        public *nn.RMSNorm PostAttnLayerNorm;
    }
        func (qb *Block) Forward(x *mlx.Array, eps float32, mask *mlx.Array, maskMode String) *mlx.Array {
        var h = qb.InputLayerNorm.Forward(x, eps);
        var attnOut = qb.Attention.Forward(h, mask, maskMode);
        x = mlx.Add(x, attnOut);
        h = qb.PostAttnLayerNorm.Forward(x, eps);
        var mlpOut = qb.MLP.Forward(h);
        x = mlx.Add(x, mlpOut);
        return x;
    }

    public static class TextEncoder {
        public *nn.Embedding EmbedTokens;
        public []*Block Layers;
        public *nn.RMSNorm FinalNorm;
    }
        func (m *TextEncoder) Load(modelManifest *manifest.ModelManifest, configPath String) error {
        fmt.Print("  Loading text encoder... ");
        var cfg Config;
        var if err = modelManifest.ReadConfigJSON(configPath, &cfg); err != null {
        return fmt.Errorf("config: %w", err);
    }
        m.Config = &cfg;
        m.Layers = make([]*Block, cfg.NumHiddenLayers);
        var weights, err = manifest.LoadWeightsFromManifest(modelManifest, "text_encoder");
        if err != null {
        return fmt.Errorf("weights: %w", err);
    }
        var if err = weights.Load(0); err != null {
        return fmt.Errorf("load weights: %w", err);
    }
        defer weights.ReleaseAll();
        return m.loadWeights(weights);
    }
        func (m *TextEncoder) loadWeights(weights safetensors.WeightSource) error {
        var if err = safetensors.LoadModule(m, weights, ""); err != null {
        return fmt.Errorf("load module: %w", err);
    }
        m.initComputedFields();
        System.out.println("✓");
        return null;
    }
        func (m *TextEncoder) initComputedFields() {
        var cfg = m.Config;
        m.FinalNorm.Eps = cfg.RMSNormEps;
        var for _, block = range m.Layers {
        block.Attention.NHeads = cfg.NumAttentionHeads;
        block.Attention.NKVHeads = cfg.NumKeyValueHeads;
        block.Attention.HeadDim = cfg.HeadDim;
        block.Attention.Scale = float32(1.0 / math.Sqrt(double(cfg.HeadDim)));
        block.Attention.RopeTheta = cfg.RopeTheta;
        block.Attention.QNorm.Eps = cfg.RMSNormEps;
        block.Attention.KNorm.Eps = cfg.RMSNormEps;
        block.InputLayerNorm.Eps = cfg.RMSNormEps;
        block.PostAttnLayerNorm.Eps = cfg.RMSNormEps;
    }
    }
        func (te *TextEncoder) Forward(tokens *mlx.Array, attnMask *mlx.Array, maskMode String) *mlx.Array {
        var h = te.EmbedTokens.Forward(tokens);
        var eps = te.RMSNormEps;
        var for _, layer = range te.Layers {
        h = layer.Forward(h, eps, attnMask, maskMode);
    }
        h = te.FinalNorm.Forward(h, eps);
        return h;
    }
        func (te *TextEncoder) ForwardWithLayerOutputs(tokens *mlx.Array, layerIndices []int, attnMask *mlx.Array, maskMode String) []*mlx.Array {
        var h = te.EmbedTokens.Forward(tokens);
        var eps = te.RMSNormEps;
        var outputs = make([]*mlx.Array, len(layerIndices));
        var layerSet = make(map[int]int);
        var for i, idx = range layerIndices {
        layerSet[idx] = i;
    }
        var for i, layer = range te.Layers {
        h = layer.Forward(h, eps, attnMask, maskMode);
        var if outIdx, ok = layerSet[i]; ok {
        outputs[outIdx] = h;
    }
    }
        return outputs;
    }

    public static String ApplyChatTemplate(String prompt, boolean think) {
        var base = "<|im_start|>user\n" + prompt + "<|im_end|>\n<|im_start|>assistant\n";
        if think {
        return base + "<think>\n\n</think>\n\n";
    }
        return base;
    }
        func (te *TextEncoder) EncodePrompt(tok *tokenizer.Tokenizer, prompt String, maxLen int, think boolean) (*mlx.Array, *mlx.Array) {
        var formattedPrompt = ApplyChatTemplate(prompt, think);
        var tokens = tok.Encode(formattedPrompt, false);
        if len(tokens) > maxLen {
        tokens = tokens[:maxLen];
    }
        var maskData = make([]float32, maxLen);
        var for i = 0; i < len(tokens); i++ {
        maskData[i] = 1.0;
    }
        var padToken = tok.PAD();
        if padToken < 0 {
        padToken = tok.EOS() // fallback;
    }
        var paddedTokens = make([]int32, maxLen);
        copy(paddedTokens, tokens);
        var for i = len(tokens); i < maxLen; i++ {
        paddedTokens[i] = padToken;
    }
        var tokensArr = mlx.NewArrayInt32(paddedTokens, []int32{1, int32(maxLen)});
        var maskArr = mlx.NewArray(maskData, []int32{1, int32(maxLen)});
        var L = int32(maxLen);
        var validLen = int32(len(tokens));
        var combinedMaskData = make([]float32, L*L);
        var negInf = float32(-1e9);
        var for i = int32(0); i < L; i++ {
        var for j = int32(0); j < L; j++ {
        var idx = i*L + j;
        if j <= i && j < validLen {
        combinedMaskData[idx] = 0;
        } else {
        combinedMaskData[idx] = negInf;
    }
    }
    }
        var maskMat = mlx.NewArray(combinedMaskData, []int32{L, L});
        var embeddings = te.Forward(tokensArr, maskMat, "");
        return embeddings, maskArr;
    }
        func (te *TextEncoder) EncodePromptWithLayers(tok *tokenizer.Tokenizer, prompt String, maxLen int, layerIndices []int, think boolean) (*mlx.Array, int32) {
        var formattedPrompt = ApplyChatTemplate(prompt, think);
        var tokens = tok.Encode(formattedPrompt, false);
        if len(tokens) > maxLen {
        tokens = tokens[:maxLen];
    }
        var padToken = tok.PAD();
        if padToken < 0 {
        padToken = tok.EOS() // fallback;
    }
        var padded = make([]int32, maxLen);
        copy(padded, tokens);
        var for i = len(tokens); i < maxLen; i++ {
        padded[i] = padToken;
    }
        var tokensArr = mlx.NewArrayInt32(padded, []int32{1, int32(maxLen)});
        var L = int32(maxLen);
        var validLen = int32(len(tokens));
        var maskData = make([]float32, L*L);
        var negInf = float32(-1e9);
        var for i = int32(0); i < L; i++ {
        var for j = int32(0); j < L; j++ {
        var idx = i*L + j;
        if j <= i && j < validLen {
        maskData[idx] = 0 // allowed: causal OK and not PAD;
        } else {
        maskData[idx] = negInf // blocked: future or PAD;
    }
    }
    }
        var maskMat = mlx.NewArray(maskData, []int32{L, L});
        var layerOutputs = te.ForwardWithLayerOutputs(tokensArr, layerIndices, maskMat, "");
        var embeddings = mlx.Concatenate(layerOutputs, 2);
        return embeddings, int32(maxLen);
    }
}
