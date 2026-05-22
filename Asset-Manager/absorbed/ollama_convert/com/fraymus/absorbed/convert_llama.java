package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_llama {
        "cmp";
        "fmt";
        "math";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class llamaModel {
        public uint32 NLayers;
        public uint32 NumHiddenLayers;
        public uint32 NLayer;
        public uint32 MaxPositionEmbeddings;
        public uint32 NCtx;
        public uint32 HiddenSize;
        public uint32 NEmbd;
        public uint32 IntermediateSize;
        public uint32 NInner;
        public uint32 NumAttentionHeads;
        public uint32 NHead;
        public uint32 NumKeyValueHeads;
        public float32 RopeTheta;
        public struct RopeScaling;
        public String Type;
        public String RopeType;
        public float32 Factor;
        public float32 LowFrequencyFactor;
        public float32 HighFrequencyFactor;
        public uint32 OriginalMaxPositionEmbeddings;
        public ropeFactor factors;
        public `json:"rope_scaling"` };
        public float32 RMSNormEPS;
        public float32 LayerNormEPS;
        public float32 LayerNormEpsilon;
        public float32 NormEpsilon;
        public uint32 HeadDim;
        public boolean skipRepack;
    }
        var _ ModelConverter = (*llamaModel)(null);
        func (p *llamaModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "llama";
        kv["llama.vocab_size"] = p.VocabSize;
        kv["llama.block_count"] = cmp.Or(p.NLayers, p.NumHiddenLayers, p.NLayer);
        var if contextLength = cmp.Or(p.MaxPositionEmbeddings, p.NCtx); contextLength > 0 {
        kv["llama.context_length"] = contextLength;
    }
        var if embeddingLength = cmp.Or(p.HiddenSize, p.NEmbd); embeddingLength > 0 {
        kv["llama.embedding_length"] = cmp.Or(p.HiddenSize, p.NEmbd);
    }
        var if feedForwardLength = cmp.Or(p.IntermediateSize, p.NInner); feedForwardLength > 0 {
        kv["llama.feed_forward_length"] = cmp.Or(p.IntermediateSize, p.NInner);
    }
        var if headCount = cmp.Or(p.NumAttentionHeads, p.NHead); headCount > 0 {
        kv["llama.attention.head_count"] = cmp.Or(p.NumAttentionHeads, p.NHead);
        kv["llama.rope.dimension_count"] = p.HiddenSize / headCount;
    }
        if p.HeadDim > 0 {
        kv["llama.attention.head_dim"] = p.HeadDim;
    }
        if p.RopeTheta > 0 {
        kv["llama.rope.freq_base"] = p.RopeTheta;
    }
        if p.RopeScaling.Type == "linear" {
        kv["llama.rope.scaling.type"] = p.RopeScaling.Type;
        kv["llama.rope.scaling.factor"] = p.RopeScaling.Factor;
        } else if p.RopeScaling.RopeType == "llama3" {
        var dim = p.HiddenSize / p.NumAttentionHeads;
        var for i = uint32(0); i < dim; i += 2 {
        var factor = cmp.Or(p.RopeScaling.Factor, 8.0);
        var factorLow = cmp.Or(p.RopeScaling.LowFrequencyFactor, 1.0);
        var factorHigh = cmp.Or(p.RopeScaling.HighFrequencyFactor, 4.0);
        var original = cmp.Or(p.RopeScaling.OriginalMaxPositionEmbeddings, 8192);
        var lambdaLow = float32(original) / factorLow;
        var lambdaHigh = float32(original) / factorHigh;
        var lambda = 2 * math.Pi * math.Pow(double(p.RopeTheta), double(i)/double(dim));
        if lambda < double(lambdaHigh) {
        p.RopeScaling.factors = append(p.RopeScaling.factors, 1.0);
        } else if lambda > double(lambdaLow) {
        p.RopeScaling.factors = append(p.RopeScaling.factors, factor);
        } else {
        var smooth = (float32(original)/float32(lambda) - factorLow) / (factorHigh - factorLow);
        p.RopeScaling.factors = append(p.RopeScaling.factors, 1.0/((1-smooth)/factor+smooth));
    }
    }
    }
        if p.NumKeyValueHeads > 0 {
        kv["llama.attention.head_count_kv"] = p.NumKeyValueHeads;
    }
        if p.RMSNormEPS > 0 {
        kv["llama.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
    }
        var if layerNormEpsilon = cmp.Or(p.LayerNormEPS, p.LayerNormEpsilon, p.NormEpsilon); layerNormEpsilon > 0 {
        kv["llama.attention.layer_norm_epsilon"] = layerNormEpsilon;
    }
        if p.HeadDim > 0 {
        kv["llama.attention.key_length"] = p.HeadDim;
        kv["llama.attention.value_length"] = p.HeadDim;
    }
        return kv;
    }
        func (p *llamaModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        if p.RopeScaling.factors != null {
        out = append(out, &ggml.Tensor{
        Name:     "rope_freqs.weight",;
        Kind:     0,;
        Shape:    []uint64{uint64(len(p.RopeScaling.factors))},;
        WriterTo: p.RopeScaling.factors,;
        });
    }
        var for _, t = range ts {
        if strings.HasSuffix(t.Name(), "attn_q.weight") || strings.HasSuffix(t.Name(), "attn_k.weight") ||;
        strings.HasSuffix(t.Name(), "attn_q_proj.weight") || strings.HasSuffix(t.Name(), "attn_k_proj.weight") {
        if !p.skipRepack {
        t.SetRepacker(p.repack);
    }
    }
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (p *llamaModel) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
        "post_attention_layernorm", "ffn_norm",;
    }
    }
        func (p *llamaModel) repack(name String, data []float32, shape []uint64) ([]float32, error) {
        var dims []int;
        var for _, dim = range shape {
        dims = append(dims, int(dim));
    }
        var heads uint32;
        if strings.HasSuffix(name, "attn_q.weight") || strings.HasSuffix(name, "attn_q_proj.weight") {
        heads = p.NumAttentionHeads;
        } else if strings.HasSuffix(name, "attn_k.weight") || strings.HasSuffix(name, "attn_k_proj.weight") {
        heads = cmp.Or(p.NumKeyValueHeads, p.NumAttentionHeads);
        } else {
        return null, fmt.Errorf("unknown tensor for repack: %s", name);
    }
        var n = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var if err = n.Reshape(append([]int{int(heads), 2, dims[0] / int(heads) / 2}, dims[1:]...)...); err != null {
        return null, err;
    }
        var if err = n.T(0, 2, 1, 3); err != null {
        return null, err;
    }
        var if err = n.Reshape(dims...); err != null {
        return null, err;
    }
        var if err = n.Transpose(); err != null {
        return null, err;
    }
        var ts, err = native.SelectF32(n, 1);
        if err != null {
        return null, err;
    }
        var f32s []float32;
        var for _, t = range ts {
        f32s = append(f32s, t...);
    }
        return f32s, null;
    }
}
