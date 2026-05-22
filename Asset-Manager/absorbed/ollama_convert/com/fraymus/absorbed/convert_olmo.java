package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_olmo {
        "cmp";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class ropeScaling {
        public float32 Factor;
        public uint32 OriginalMaxPositionEmbeds;
        public float32 AttentionFactor;
        public float32 BetaFast;
        public float32 BetaSlow;
        public String RopeType;
        public float32 ExtrapolationFactor;
    }

    public static class olmoModel {
        public uint32 HiddenSize;
        public uint32 NumHiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 MaxPositionEmbeddings;
        public float32 RMSNormEPS;
        public float32 RopeTheta;
        public *ropeScaling RopeScaling;
        public uint32 SlidingWindow;
        public []String LayerTypes;
    }
        var _ ModelConverter = (*olmoModel)(null);
        func (p *olmoModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "olmo3";
        kv["olmo3.block_count"] = p.NumHiddenLayers;
        kv["olmo3.context_length"] = p.MaxPositionEmbeddings;
        kv["olmo3.embedding_length"] = p.HiddenSize;
        kv["olmo3.feed_forward_length"] = p.IntermediateSize;
        kv["olmo3.attention.head_count"] = p.NumAttentionHeads;
        kv["olmo3.attention.head_count_kv"] = cmp.Or(p.NumKeyValueHeads, p.NumAttentionHeads);
        if p.RopeTheta > 0 {
        kv["olmo3.rope.freq_base"] = p.RopeTheta;
    }
        if p.RopeScaling != null {
        if p.RopeScaling.Factor > 0 {
        kv["olmo3.rope.scaling.factor"] = p.RopeScaling.Factor;
    }
        if p.RopeScaling.OriginalMaxPositionEmbeds > 0 {
        kv["olmo3.rope.scaling.original_context_length"] = p.RopeScaling.OriginalMaxPositionEmbeds;
    }
        if p.RopeScaling.AttentionFactor > 0 {
        kv["olmo3.rope.scaling.attn_factor"] = p.RopeScaling.AttentionFactor;
    }
        if p.RopeScaling.RopeType != "" {
        kv["olmo3.rope.scaling.type"] = p.RopeScaling.RopeType;
    }
    }
        if p.RMSNormEPS > 0 {
        kv["olmo3.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
    }
        if p.SlidingWindow > 0 {
        kv["olmo3.attention.sliding_window"] = p.SlidingWindow;
    }
        if len(p.LayerTypes) > 0 {
        var slidingPattern = make([]boolean, len(p.LayerTypes));
        var for i, layerType = range p.LayerTypes {
        slidingPattern[i] = (layerType == "sliding_attention");
    }
        kv["olmo3.attention.sliding_window_pattern"] = slidingPattern;
    }
        return kv;
    }
        func (p *olmoModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out = make([]*ggml.Tensor, 0, len(ts));
        var for _, t = range ts {
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (p *olmoModel) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.layers", "blk",;
        "model.norm", "output_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.k_norm", "attn_k_norm",;
        "post_attention_layernorm", "post_attention_norm",;
        "post_feedforward_layernorm", "post_ffw_norm",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
    }
    }
}
