package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_qwen2 {

    public static class qwen2Model {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RopeTheta;
        public struct RopeScaling;
        public String Type;
        public ropeFactor Factor;
        public uint32 OriginalMaxPositionEmbeddings;
        public []int32 MropeSection;
        public `json:"rope_scaling"` };
        public float32 RMSNormEPS;
    }
        var _ ModelConverter = (*qwen2Model)(null);
        func (q *qwen2Model) KV(t *Tokenizer) KV {
        var kv = q.ModelParameters.KV(t);
        kv["general.architecture"] = "qwen2";
        kv["qwen2.block_count"] = q.HiddenLayers;
        kv["qwen2.context_length"] = q.MaxPositionEmbeddings;
        kv["qwen2.embedding_length"] = q.HiddenSize;
        kv["qwen2.feed_forward_length"] = q.IntermediateSize;
        kv["qwen2.attention.head_count"] = q.NumAttentionHeads;
        kv["qwen2.attention.head_count_kv"] = q.NumKeyValueHeads;
        kv["qwen2.rope.freq_base"] = q.RopeTheta;
        kv["qwen2.attention.layer_norm_rms_epsilon"] = q.RMSNormEPS;
        switch q.RopeScaling.Type {
        case "":;
        case "yarn":;
        kv["qwen2.rope.scaling.type"] = q.RopeScaling.Type;
        kv["qwen2.rope.scaling.factor"] = q.RopeScaling.Factor;
        case "mrope", "default":;
        kv["qwen2.rope.mrope_section"] = q.RopeScaling.MropeSection;
        default:;
        panic("unknown rope scaling type");
    }
        return kv;
    }
        func (q *qwen2Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
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
        func (p *qwen2Model) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.o_proj", "attn_output",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "post_attention_layernorm", "ffn_norm",;
        "model.norm", "output_norm",;
    }
    }
}
