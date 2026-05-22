package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_phi3 {
        "cmp";
        "encoding/binary";
        "io";
        "math";
        "strings";
        "sync";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class phi3Model {
        public uint32 NumHiddenLayers;
        public uint32 NLayers;
        public uint32 HiddenSize;
        public uint32 NEmbd;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NHead;
        public uint32 NumKeyValueHeads;
        public uint32 NHeadKV;
        public float32 RopeTheta;
        public struct RopeScaling;
        public String Type;
        public ropeFactor LongFactor;
        public ropeFactor ShortFactor;
        public `json:"rope_scaling"` };
        public float32 RMSNormEPS;
        public uint32 NPositions;
        public uint32 MaxPositionEmbeddings;
        public uint32 OriginalMaxPositionEmbeddings;
        public uint32 SlidingWindow;
    }
        var _ ModelConverter = (*phi3Model)(null);
        func (p *phi3Model) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "phi3";
        kv["phi3.context_length"] = p.MaxPositionEmbeddings;
        kv["phi3.embedding_length"] = cmp.Or(p.HiddenSize, p.NEmbd);
        kv["phi3.feed_forward_length"] = p.IntermediateSize;
        kv["phi3.block_count"] = cmp.Or(p.NumHiddenLayers, p.NLayers);
        kv["phi3.attention.head_count"] = cmp.Or(p.NumAttentionHeads, p.NHead);
        kv["phi3.attention.head_count_kv"] = cmp.Or(p.NumKeyValueHeads, p.NHeadKV);
        kv["phi3.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS;
        kv["phi3.rope.dimension_count"] = p.HiddenSize / cmp.Or(p.NumAttentionHeads, p.NHead);
        kv["phi3.rope.freq_base"] = p.RopeTheta;
        kv["phi3.rope.scaling.original_context_length"] = p.OriginalMaxPositionEmbeddings;
        kv["phi3.attention.sliding_window"] = p.SlidingWindow;
        var scale = double(p.MaxPositionEmbeddings) / double(p.OriginalMaxPositionEmbeddings);
        switch p.RopeScaling.Type {
        case "":;
        case "su", "longrope":;
        kv["phi3.rope.scaling.attn_factor"] = float32(max(math.Sqrt(1+math.Log(scale)/math.Log(double(p.OriginalMaxPositionEmbeddings))), 1.0));
        case "yarn":;
        kv["phi3.rope.scaling.attn_factor"] = float32(max(0.1*math.Log(scale)+1.0, 1.0));
        default:;
        panic("unknown rope scaling type");
    }
        return kv;
    }
        func (p *phi3Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var addRopeFactors sync.Once;
        var out = make([]*ggml.Tensor, 0, len(ts)+2);
        var for _, t = range ts {
        if strings.HasPrefix(t.Name(), "blk.0.") {
        addRopeFactors.Do(func() {
        out = append(out, &ggml.Tensor{
        Name:     "rope_factors_long.weight",;
        Kind:     0,;
        Shape:    []uint64{uint64(len(p.RopeScaling.LongFactor))},;
        WriterTo: p.RopeScaling.LongFactor,;
        }, &ggml.Tensor{
        Name:     "rope_factors_short.weight",;
        Kind:     0,;
        Shape:    []uint64{uint64(len(p.RopeScaling.ShortFactor))},;
        WriterTo: p.RopeScaling.ShortFactor,;
        });
        });
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
        func (p *phi3Model) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.qkv_proj", "attn_qkv",;
        "self_attn.o_proj", "attn_output",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_up_proj", "ffn_up",;
        "post_attention_layernorm", "ffn_norm",;
    }
    }
        type ropeFactor []float32;
        func (r ropeFactor) WriteTo(w io.Writer) (long, error) {
        return 0, binary.Write(w, binary.LittleEndian, r);
    }
}
