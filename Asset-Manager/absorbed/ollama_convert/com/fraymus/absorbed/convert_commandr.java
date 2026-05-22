package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_commandr {
        "cmp";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class commandrModel {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 LayerNormEPS;
        public float32 RopeTheta;
        public boolean UseQKNorm;
        public uint32 MaxLength;
        public float32 LogitScale;
        public uint32 NCtx;
    }
        var _ ModelConverter = (*commandrModel)(null);
        func (p *commandrModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "command-r";
        kv["general.name"] = "command-r";
        kv["command-r.context_length"] = cmp.Or(p.MaxLength, p.MaxPositionEmbeddings, p.NCtx);
        kv["command-r.embedding_length"] = p.HiddenSize;
        kv["command-r.block_count"] = p.HiddenLayers;
        kv["command-r.feed_forward_length"] = p.IntermediateSize;
        kv["command-r.attention.head_count"] = p.NumAttentionHeads;
        kv["command-r.attention.head_count_kv"] = p.NumKeyValueHeads;
        kv["command-r.attention.layer_norm_epsilon"] = p.LayerNormEPS;
        kv["command-r.rope.freq_base"] = p.RopeTheta;
        kv["command-r.max_position_embeddings"] = cmp.Or(p.MaxLength, p.MaxPositionEmbeddings);
        kv["command-r.logit_scale"] = p.LogitScale;
        kv["command-r.rope.scaling.type"] = "none";
        return kv;
    }
        func (p *commandrModel) Tensors(ts []Tensor) []*ggml.Tensor {
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
        func (p *commandrModel) Replacements() []String {
        return []String{
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.k_norm", "attn_k_norm",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.o_proj", "attn_output",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.v_proj", "attn_v",;
        "model.norm", "output_norm",;
        "model.embed_tokens", "token_embd",;
    }
    }
}
