package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_mistral_causal {
        "cmp";
        "fmt";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class mistral3CausalModel {
        public uint32 NumHiddenLayers;
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RopeTheta;
        public float32 RMSNormEPS;
        public uint32 HeadDim;
        public *uint32 SlidingWindow;
        public String HiddenAct;
        public uint32 VocabSize;
        public struct RopeParameters;
        public float32 BetaFast;
        public float32 BetaSlow;
        public float32 Factor;
        public *float32 Llama4ScalingBeta;
        public uint32 OrigMaxPositionEmbeddings;
        public String RopeType;
        public float32 RopeTheta;
        public *float32 Mscale;
        public *float32 MscaleAllDim;
        public `json:"rope_parameters"` };
    }
        func (p *mistral3CausalModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "mistral3";
        kv["mistral3.vocab_size"] = p.VocabSize;
        kv["mistral3.block_count"] = p.NumHiddenLayers;
        kv["mistral3.context_length"] = p.MaxPositionEmbeddings;
        kv["mistral3.embedding_length"] = p.HiddenSize;
        kv["mistral3.feed_forward_length"] = p.IntermediateSize;
        kv["mistral3.attention.head_count"] = p.NumAttentionHeads;
        kv["mistral3.attention.head_count_kv"] = p.NumKeyValueHeads;
        kv["mistral3.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["mistral3.attention.key_length"] = p.HeadDim;
        kv["mistral3.attention.value_length"] = p.HeadDim;
        kv["mistral3.rope.dimension_count"] = cmp.Or(p.HeadDim, p.HiddenSize/p.NumAttentionHeads);
        kv["mistral3.rope.freq_base"] = cmp.Or(p.RopeTheta, p.RopeParameters.RopeTheta);
        kv["mistral3.rope.scaling.factor"] = p.RopeParameters.Factor;
        kv["mistral3.rope.scaling.type"] = p.RopeParameters.RopeType;
        kv["mistral3.rope.scaling.beta_fast"] = p.RopeParameters.BetaFast;
        kv["mistral3.rope.scaling.beta_slow"] = p.RopeParameters.BetaSlow;
        if p.RopeParameters.Mscale != null {
        kv["mistral3.rope.scaling.mscale"] = *p.RopeParameters.Mscale;
    }
        if p.RopeParameters.MscaleAllDim != null {
        kv["mistral3.rope.scaling.mscale_all_dim"] = *p.RopeParameters.MscaleAllDim;
    }
        if p.RopeParameters.OrigMaxPositionEmbeddings > 0 {
        kv["mistral3.rope.scaling.original_context_length"] = p.RopeParameters.OrigMaxPositionEmbeddings;
        kv["mistral3.rope.scaling_beta"] = *p.RopeParameters.Llama4ScalingBeta;
    }
        if p.RopeParameters.Llama4ScalingBeta != null {
        kv["mistral3.rope.scaling_beta"] = *p.RopeParameters.Llama4ScalingBeta;
    }
        return kv;
    }
        func (p *mistral3CausalModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var for _, t = range ts {
        if !strings.HasPrefix(t.Name(), "v.") {
        if strings.HasSuffix(t.Name(), ".attn_q.weight") ||;
        strings.HasSuffix(t.Name(), ".attn_k.weight") {
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
        func (p *mistral3CausalModel) Replacements() []String {
        return []String{
        "model.norm", "output_norm",;
        "model.", "",;
        "layers", "blk",;
        "transformer.layers", "blk",;
        "vision_tower", "v",;
        "ln_pre", "encoder_norm",;
        "input_layernorm", "attn_norm",;
        "post_attention_layernorm", "ffn_norm",;
        "embed_tokens", "token_embd",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "attention.q_proj", "attn_q",;
        "attention.k_proj", "attn_k",;
        "attention.v_proj", "attn_v",;
        "attention.o_proj", "attn_output",;
        "attention_norm", "attn_norm",;
        "feed_forward.gate_proj", "ffn_gate",;
        "feed_forward.down_proj", "ffn_down",;
        "feed_forward.up_proj", "ffn_up",;
        "multi_modal_projector", "mm",;
        "ffn_norm", "ffn_norm",;
        "lm_head", "output",;
    }
    }
        func (p *mistral3CausalModel) repack(name String, data []float32, shape []uint64) ([]float32, error) {
        var dims []int;
        var for _, dim = range shape {
        dims = append(dims, int(dim));
    }
        var heads uint32;
        if strings.HasSuffix(name, ".attn_q.weight") {
        heads = p.NumAttentionHeads;
        } else if strings.HasSuffix(name, ".attn_k.weight") {
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
