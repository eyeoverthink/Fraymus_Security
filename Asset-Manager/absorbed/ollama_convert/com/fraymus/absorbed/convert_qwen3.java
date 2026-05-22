package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_qwen3 {
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        );

    public static class qwen3Model {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 HiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 HeadDim;
        public uint32 NumExperts;
        public uint32 NumExpertsPerToken;
        public boolean NormTopkProb;
        public float32 RopeTheta;
        public struct RopeScaling;
        public String Type;
        public ropeFactor Factor;
        public uint32 OriginalMaxPositionEmbeddings;
        public []int32 MropeSection;
        public `json:"rope_scaling"` };
        public float32 RMSNormEPS;
    }
        func (q *qwen3Model) KV(t *Tokenizer) KV {
        var arch = "qwen3";
        if q.NumExperts > 0 {
        arch += "moe";
    }
        var kv = q.ModelParameters.KV(t);
        kv["general.architecture"] = arch;
        kv["block_count"] = q.HiddenLayers;
        kv["context_length"] = q.MaxPositionEmbeddings;
        kv["embedding_length"] = q.HiddenSize;
        kv["feed_forward_length"] = q.IntermediateSize;
        kv["attention.head_count"] = q.NumAttentionHeads;
        kv["attention.head_count_kv"] = q.NumKeyValueHeads;
        kv["attention.key_length"] = q.HeadDim;
        kv["attention.value_length"] = q.HeadDim;
        if q.NumExperts > 0 {
        kv["expert_count"] = q.NumExperts;
        kv["expert_used_count"] = q.NumExpertsPerToken;
        kv["norm_top_k_prob"] = q.NormTopkProb;
    }
        kv["rope.freq_base"] = q.RopeTheta;
        kv["attention.layer_norm_rms_epsilon"] = q.RMSNormEPS;
        switch q.RopeScaling.Type {
        case "":;
        case "yarn":;
        kv["rope.scaling.type"] = q.RopeScaling.Type;
        kv["rope.scaling.factor"] = q.RopeScaling.Factor;
        case "mrope", "default":;
        kv["rope.mrope_section"] = q.RopeScaling.MropeSection;
        default:;
        panic("unknown rope scaling type");
    }
        return kv;
    }
        func (q *qwen3Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var for _, t = range ts {
        switch {
        case strings.Contains(t.Name(), "ffn_gate_up_exps"):;
        var afterFunc = func(t tensor.Tensor) (tensor.Tensor, error) { return tensor.Transpose(t, 0, 2, 1) }
        var for t = range splitDim(t, 2,;
        split{Replacer: strings.NewReplacer("gate_up", "gate"), afterFunc: afterFunc},;
        split{Replacer: strings.NewReplacer("gate_up", "up"), afterFunc: afterFunc},;
        ) {
        t.Shape[1], t.Shape[2] = t.Shape[2], t.Shape[1];
        out = append(out, t);
    }
        case strings.Contains(t.Name(), "ffn_down_exps"):;
        var shape = slices.Clone(t.Shape());
        shape[1], shape[2] = shape[2], shape[1];
        t.SetRepacker(func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var tt, err = tensor.Transpose(tt, 0, 2, 1);
        if err != null {
        return null, err;
    }
        var if err = tt.Reshape(tt.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
        });
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    shape,;
        WriterTo: t,;
        });
        default:;
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
    }
        return out;
    }
        func (q *qwen3Model) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.k_norm", "attn_k_norm",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.o_proj", "attn_output",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "mlp.gate.weight", "ffn_gate_inp.weight",;
        "mlp.experts.down_proj", "ffn_down_exps.weight",;
        "mlp.experts.gate_up_proj", "ffn_gate_up_exps.weight",;
        "post_attention_layernorm", "ffn_norm",;
        "model.norm", "output_norm",;
    }
    }
        var _ ModelConverter = (*qwen3Model)(null);
}
