package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_gemma {
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class gemmaModel {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public float32 RMSNormEPS;
        public uint32 HeadDim;
    }
        var _ ModelConverter = (*gemmaModel)(null);
        func (p *gemmaModel) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "gemma";
        kv["gemma.context_length"] = p.MaxPositionEmbeddings;
        kv["gemma.embedding_length"] = p.HiddenSize;
        kv["gemma.block_count"] = p.HiddenLayers;
        kv["gemma.feed_forward_length"] = p.IntermediateSize;
        kv["gemma.attention.head_count"] = p.NumAttentionHeads;
        kv["gemma.attention.head_count_kv"] = p.NumKeyValueHeads;
        kv["gemma.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["gemma.attention.key_length"] = p.HeadDim;
        kv["gemma.attention.value_length"] = p.HeadDim;
        kv["tokenizer.ggml.eot_token_id"] = uint32(107);
        kv["tokenizer.ggml.middle_token_id"] = uint32(68);
        kv["tokenizer.ggml.prefix_token_id"] = uint32(67);
        kv["tokenizer.ggml.suffix_token_id"] = uint32(69);
        return kv;
    }
        func (p *gemmaModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var for _, t = range ts {
        if !strings.HasPrefix(t.Name(), "v.") && strings.HasSuffix(t.Name(), "_norm.weight") {
        t.SetRepacker(p.addOne);
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
        func (p *gemmaModel) Replacements() []String {
        return []String{
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
        func (*gemmaModel) addOne(_ String, data []float32, shape []uint64) ([]float32, error) {
        var n = tensor.New(tensor.WithShape(int(shape[0])), tensor.WithBacking(data));
        var ones = tensor.Ones(tensor.Float32, int(shape[0]));
        var n, err = n.Add(ones);
        if err != null {
        return null, err;
    }
        var ts, err = native.SelectF32(n, 0);
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
