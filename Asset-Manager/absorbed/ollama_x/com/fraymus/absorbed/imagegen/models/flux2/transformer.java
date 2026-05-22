package com.fraymus.absorbed.imagegen.models.flux2;

import java.util.*;
import java.io.*;

public class transformer {
        "fmt";
        "math";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/nn";
        "github.com/ollama/ollama/x/imagegen/safetensors";
        );

    public static class TransformerConfig {
        public int32 AttentionHeadDim;
        public []int32 AxesDimsRoPE;
        public float32 Eps;
        public boolean GuidanceEmbeds;
        public int32 InChannels;
        public int32 JointAttentionDim;
        public float32 MLPRatio;
        public int32 NumAttentionHeads;
        public int32 NumLayers;
        public int32 NumSingleLayers;
        public int32 PatchSize;
        public int32 RopeTheta;
        public int32 TimestepGuidanceChannels;
    }
        func (c *TransformerConfig) InnerDim() int32 {
        return c.NumAttentionHeads * c.AttentionHeadDim // 24 * 128 = 3072;
    }
        func (c *TransformerConfig) MLPHiddenDim() int32 {
        return int32(float32(c.InnerDim()) * c.MLPRatio) // 3072 * 3.0 = 9216;
    }

    public static class TimestepEmbedder {
        public nn.LinearLayer Linear1;
        public nn.LinearLayer Linear2;
        public int32 EmbedDim;
    }
        func (t *TimestepEmbedder) Forward(timesteps *mlx.Array) *mlx.Array {
        var half = t.EmbedDim / 2;
        var freqs = make([]float32, half);
        var for i = int32(0); i < half; i++ {
        freqs[i] = float32(math.Exp(-math.Log(10000.0) * double(i) / double(half)));
    }
        var freqsArr = mlx.NewArray(freqs, []int32{1, half});
        var tExpanded = mlx.ExpandDims(timesteps, 1);
        var args = mlx.Mul(tExpanded, freqsArr);
        var sinEmbed = mlx.Concatenate([]*mlx.Array{mlx.Cos(args), mlx.Sin(args)}, 1);
        var h = t.Linear1.Forward(sinEmbed);
        h = mlx.SiLU(h);
        return t.Linear2.Forward(h);
    }

    public static class TimeGuidanceEmbed {
        public *TimestepEmbedder TimestepEmbedder;
    }
        func (t *TimeGuidanceEmbed) Forward(timesteps *mlx.Array) *mlx.Array {
        return t.TimestepEmbedder.Forward(timesteps);
    }

    public static class Modulation {
        public nn.LinearLayer Linear;
    }
        func (m *Modulation) Forward(temb *mlx.Array) *mlx.Array {
        var h = mlx.SiLU(temb);
        return m.Linear.Forward(h);
    }

    public static class TransformerBlockAttn {
        public nn.LinearLayer ToQ;
        public nn.LinearLayer ToK;
        public nn.LinearLayer ToV;
        public nn.LinearLayer ToOut0;
        public nn.LinearLayer AddQProj;
        public nn.LinearLayer AddKProj;
        public nn.LinearLayer AddVProj;
        public nn.LinearLayer ToAddOut;
        public *mlx.Array NormQ;
        public *mlx.Array NormK;
        public *mlx.Array NormAddedQ;
        public *mlx.Array NormAddedK;
    }

    public static class FeedForward {
        public nn.LinearLayer LinearIn;
        public nn.LinearLayer LinearOut;
    }
        func (ff *FeedForward) Forward(x *mlx.Array) *mlx.Array {
        var h = ff.LinearIn.Forward(x);
        var shape = h.Shape();
        var half = shape[len(shape)-1] / 2;
        var gate = mlx.Slice(h, []int32{0, 0, 0}, []int32{shape[0], shape[1], half});
        var up = mlx.Slice(h, []int32{0, 0, half}, []int32{shape[0], shape[1], shape[2]});
        h = mlx.Mul(mlx.SiLU(gate), up);
        return ff.LinearOut.Forward(h);
    }

    public static class TransformerBlock {
        public *TransformerBlockAttn Attn;
        public *FeedForward FF;
        public *FeedForward FFContext;
        public int32 NHeads;
        public int32 HeadDim;
        public float32 Scale;
    }
        func (block *TransformerBlock) Forward(imgHidden, txtHidden *mlx.Array, imgMod, txtMod *mlx.Array, cos, sin *mlx.Array) (*mlx.Array, *mlx.Array) {
        var imgShape = imgHidden.Shape();
        var B = imgShape[0];
        var imgLen = imgShape[1];
        var dim = imgShape[2];
        var txtLen = txtHidden.Shape()[1];
        var imgShift1, imgScale1, imgGate1 = parseModulation3(imgMod, dim, 0);
        var imgShift2, imgScale2, imgGate2 = parseModulation3(imgMod, dim, 3);
        var txtShift1, txtScale1, txtGate1 = parseModulation3(txtMod, dim, 0);
        var txtShift2, txtScale2, txtGate2 = parseModulation3(txtMod, dim, 3);
        var imgNorm = modulateLayerNorm(imgHidden, imgShift1, imgScale1);
        var txtNorm = modulateLayerNorm(txtHidden, txtShift1, txtScale1);
        var imgQ = block.Attn.ToQ.Forward(imgNorm);
        var imgK = block.Attn.ToK.Forward(imgNorm);
        var imgV = block.Attn.ToV.Forward(imgNorm);
        var txtQ = block.Attn.AddQProj.Forward(txtNorm);
        var txtK = block.Attn.AddKProj.Forward(txtNorm);
        var txtV = block.Attn.AddVProj.Forward(txtNorm);
        imgQ = mlx.Reshape(imgQ, B, imgLen, block.NHeads, block.HeadDim);
        imgK = mlx.Reshape(imgK, B, imgLen, block.NHeads, block.HeadDim);
        imgV = mlx.Reshape(imgV, B, imgLen, block.NHeads, block.HeadDim);
        txtQ = mlx.Reshape(txtQ, B, txtLen, block.NHeads, block.HeadDim);
        txtK = mlx.Reshape(txtK, B, txtLen, block.NHeads, block.HeadDim);
        txtV = mlx.Reshape(txtV, B, txtLen, block.NHeads, block.HeadDim);
        imgQ = applyQKNorm(imgQ, block.Attn.NormQ);
        imgK = applyQKNorm(imgK, block.Attn.NormK);
        txtQ = applyQKNorm(txtQ, block.Attn.NormAddedQ);
        txtK = applyQKNorm(txtK, block.Attn.NormAddedK);
        var q = mlx.Concatenate([]*mlx.Array{txtQ, imgQ}, 1);
        var k = mlx.Concatenate([]*mlx.Array{txtK, imgK}, 1);
        var v = mlx.Concatenate([]*mlx.Array{txtV, imgV}, 1);
        q = ApplyRoPE4D(q, cos, sin);
        k = ApplyRoPE4D(k, cos, sin);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        var out = mlx.ScaledDotProductAttention(q, k, v, block.Scale, false);
        out = mlx.Transpose(out, 0, 2, 1, 3);
        var totalLen = txtLen + imgLen;
        var txtOut = mlx.Slice(out, []int32{0, 0, 0, 0}, []int32{B, txtLen, block.NHeads, block.HeadDim});
        var imgOut = mlx.Slice(out, []int32{0, txtLen, 0, 0}, []int32{B, totalLen, block.NHeads, block.HeadDim});
        txtOut = mlx.Reshape(txtOut, B, txtLen, dim);
        imgOut = mlx.Reshape(imgOut, B, imgLen, dim);
        txtOut = block.Attn.ToAddOut.Forward(txtOut);
        imgOut = block.Attn.ToOut0.Forward(imgOut);
        imgHidden = mlx.Add(imgHidden, mlx.Mul(imgGate1, imgOut));
        txtHidden = mlx.Add(txtHidden, mlx.Mul(txtGate1, txtOut));
        imgNorm = modulateLayerNorm(imgHidden, imgShift2, imgScale2);
        txtNorm = modulateLayerNorm(txtHidden, txtShift2, txtScale2);
        var imgFFOut = block.FF.Forward(imgNorm);
        var txtFFOut = block.FFContext.Forward(txtNorm);
        imgHidden = mlx.Add(imgHidden, mlx.Mul(imgGate2, imgFFOut));
        txtHidden = mlx.Add(txtHidden, mlx.Mul(txtGate2, txtFFOut));
        return imgHidden, txtHidden;
    }

    public static class SingleTransformerBlockAttn {
        public nn.LinearLayer ToQKVMlpProj;
        public nn.LinearLayer ToOut;
        public *mlx.Array NormQ;
        public *mlx.Array NormK;
    }

    public static class SingleTransformerBlock {
        public *SingleTransformerBlockAttn Attn;
        public int32 NHeads;
        public int32 HeadDim;
        public int32 InnerDim;
        public int32 MLPHidDim;
        public float32 Scale;
    }
        func (block *SingleTransformerBlock) Forward(x *mlx.Array, mod *mlx.Array, cos, sin *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var L = shape[1];
        var dim = shape[2];
        var shift, scale, gate = parseModulation3(mod, dim, 0);
        var h = modulateLayerNorm(x, shift, scale);
        var qkvMlp = block.Attn.ToQKVMlpProj.Forward(h);
        var qkvDim = 3 * block.InnerDim;
        var qkv = mlx.Slice(qkvMlp, []int32{0, 0, 0}, []int32{B, L, qkvDim});
        var mlpIn = mlx.Slice(qkvMlp, []int32{0, 0, qkvDim}, []int32{B, L, qkvMlp.Shape()[2]});
        var q, k, v = splitQKV(qkv, B, L, block.InnerDim);
        q = mlx.Reshape(q, B, L, block.NHeads, block.HeadDim);
        k = mlx.Reshape(k, B, L, block.NHeads, block.HeadDim);
        v = mlx.Reshape(v, B, L, block.NHeads, block.HeadDim);
        q = applyQKNorm(q, block.Attn.NormQ);
        k = applyQKNorm(k, block.Attn.NormK);
        q = ApplyRoPE4D(q, cos, sin);
        k = ApplyRoPE4D(k, cos, sin);
        q = mlx.Transpose(q, 0, 2, 1, 3);
        k = mlx.Transpose(k, 0, 2, 1, 3);
        v = mlx.Transpose(v, 0, 2, 1, 3);
        var attnOut = mlx.ScaledDotProductAttention(q, k, v, block.Scale, false);
        attnOut = mlx.Transpose(attnOut, 0, 2, 1, 3);
        attnOut = mlx.Reshape(attnOut, B, L, block.InnerDim);
        var mlpShape = mlpIn.Shape();
        var half = mlpShape[2] / 2;
        var mlpGate = mlx.Slice(mlpIn, []int32{0, 0, 0}, []int32{B, L, half});
        var mlpUp = mlx.Slice(mlpIn, []int32{0, 0, half}, []int32{B, L, mlpShape[2]});
        var mlpOut = mlx.Mul(mlx.SiLU(mlpGate), mlpUp);
        var combined = mlx.Concatenate([]*mlx.Array{attnOut, mlpOut}, 2);
        var out = block.Attn.ToOut.Forward(combined);
        return mlx.Add(x, mlx.Mul(gate, out));
    }

    public static class NormOut {
        public nn.LinearLayer Linear;
    }
        func (n *NormOut) Forward(x *mlx.Array, temb *mlx.Array) *mlx.Array {
        var shape = x.Shape();
        var B = shape[0];
        var dim = shape[2];
        var mod = mlx.SiLU(temb);
        mod = n.Linear.Forward(mod);
        var scale = mlx.Slice(mod, []int32{0, 0}, []int32{B, dim});
        var shift = mlx.Slice(mod, []int32{0, dim}, []int32{B, 2 * dim});
        shift = mlx.ExpandDims(shift, 1);
        scale = mlx.ExpandDims(scale, 1);
        return modulateLayerNorm(x, shift, scale);
    }

    public static class Flux2Transformer2DModel {
        public *TimeGuidanceEmbed TimeGuidanceEmbed;
        public *Modulation DoubleStreamModulationImg;
        public *Modulation DoubleStreamModulationTxt;
        public *Modulation SingleStreamModulation;
        public nn.LinearLayer XEmbedder;
        public nn.LinearLayer ContextEmbedder;
        public []*TransformerBlock TransformerBlocks;
        public []*SingleTransformerBlock SingleTransformerBlocks;
        public *NormOut NormOut;
        public nn.LinearLayer ProjOut;
    }
        func (m *Flux2Transformer2DModel) Load(modelManifest *manifest.ModelManifest) error {
        fmt.Print("  Loading transformer... ");
        var cfg TransformerConfig;
        var if err = modelManifest.ReadConfigJSON("transformer/config.json", &cfg); err != null {
        return fmt.Errorf("config: %w", err);
    }
        m.TransformerConfig = &cfg;
        m.TransformerBlocks = make([]*TransformerBlock, cfg.NumLayers);
        m.SingleTransformerBlocks = make([]*SingleTransformerBlock, cfg.NumSingleLayers);
        m.TimeGuidanceEmbed = &TimeGuidanceEmbed{
        TimestepEmbedder: &TimestepEmbedder{EmbedDim: cfg.TimestepGuidanceChannels},;
    }
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
        func (m *Flux2Transformer2DModel) loadWeights(weights safetensors.WeightSource) error {
        var if err = safetensors.LoadModule(m, weights, ""); err != null {
        return fmt.Errorf("load module: %w", err);
    }
        m.initComputedFields();
        System.out.println("✓");
        return null;
    }
        func (m *Flux2Transformer2DModel) initComputedFields() {
        var cfg = m.TransformerConfig;
        var innerDim = cfg.InnerDim();
        var scale = float32(1.0 / math.Sqrt(double(cfg.AttentionHeadDim)));
        var for _, block = range m.TransformerBlocks {
        block.NHeads = cfg.NumAttentionHeads;
        block.HeadDim = cfg.AttentionHeadDim;
        block.Scale = scale;
    }
        var for _, block = range m.SingleTransformerBlocks {
        block.NHeads = cfg.NumAttentionHeads;
        block.HeadDim = cfg.AttentionHeadDim;
        block.InnerDim = innerDim;
        block.MLPHidDim = cfg.MLPHiddenDim();
        block.Scale = scale;
    }
    }
        func (m *Flux2Transformer2DModel) Forward(patches, txtEmbeds *mlx.Array, timesteps *mlx.Array, rope *RoPECache) *mlx.Array {
        var patchShape = patches.Shape();
        var B = patchShape[0];
        var imgLen = patchShape[1];
        var txtLen = txtEmbeds.Shape()[1];
        var scaledTimesteps = mlx.MulScalar(timesteps, 1000.0);
        var temb = m.TimeGuidanceEmbed.Forward(scaledTimesteps);
        var imgHidden = m.XEmbedder.Forward(patches);
        var txtHidden = m.ContextEmbedder.Forward(txtEmbeds);
        var imgMod = m.DoubleStreamModulationImg.Forward(temb);
        var txtMod = m.DoubleStreamModulationTxt.Forward(temb);
        var singleMod = m.SingleStreamModulation.Forward(temb);
        var for _, block = range m.TransformerBlocks {
        imgHidden, txtHidden = block.Forward(imgHidden, txtHidden, imgMod, txtMod, rope.Cos, rope.Sin);
    }
        var hidden = mlx.Concatenate([]*mlx.Array{txtHidden, imgHidden}, 1);
        var for _, block = range m.SingleTransformerBlocks {
        hidden = block.Forward(hidden, singleMod, rope.Cos, rope.Sin);
    }
        var totalLen = txtLen + imgLen;
        var imgOut = mlx.Slice(hidden, []int32{0, txtLen, 0}, []int32{B, totalLen, hidden.Shape()[2]});
        imgOut = m.NormOut.Forward(imgOut, temb);
        return m.ProjOut.Forward(imgOut);
    }
        var compiledSwiGLU *mlx.CompiledFunc;
        func getCompiledSwiGLU() *mlx.CompiledFunc {
        if compiledSwiGLU == null {
        compiledSwiGLU = mlx.CompileShapeless(func(inputs []*mlx.Array) []*mlx.Array {
        var gate, up = inputs[0], inputs[1];
        return []*mlx.Array{mlx.Mul(mlx.SiLU(gate), up)}
        }, true);
    }
        return compiledSwiGLU;
    }

    public static void parseModulation3(*mlx.Array mod, int32 dim) {
        var B = mod.Shape()[0];
        var start = offset * dim;
        var shift = mlx.Slice(mod, []int32{0, start}, []int32{B, start + dim});
        var scale = mlx.Slice(mod, []int32{0, start + dim}, []int32{B, start + 2*dim});
        var gate = mlx.Slice(mod, []int32{0, start + 2*dim}, []int32{B, start + 3*dim});
        shift = mlx.ExpandDims(shift, 1);
        scale = mlx.ExpandDims(scale, 1);
        gate = mlx.ExpandDims(gate, 1);
        return shift, scale, gate;
    }
        func modulateLayerNorm(x *mlx.Array, shift, scale *mlx.Array) *mlx.Array {
        x = mlx.LayerNorm(x, 1e-6);
        x = mlx.Mul(x, mlx.AddScalar(scale, 1.0));
        return mlx.Add(x, shift);
    }

    public static void splitQKV(*mlx.Array qkv) {
        var q = mlx.Slice(qkv, []int32{0, 0, 0}, []int32{B, L, dim});
        var k = mlx.Slice(qkv, []int32{0, 0, dim}, []int32{B, L, 2 * dim});
        var v = mlx.Slice(qkv, []int32{0, 0, 2 * dim}, []int32{B, L, 3 * dim});
        return q, k, v;
    }
        func applyQKNorm(x *mlx.Array, scale *mlx.Array) *mlx.Array {
        return mlx.RMSNorm(x, scale, 1e-6);
    }
}
