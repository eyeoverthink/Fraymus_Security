package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_gptoss {
        "bytes";
        "cmp";
        "encoding/binary";
        "io";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        );

    public static class gptossModel {
        public uint32 HiddenLayers;
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 AttentionHeads;
        public uint32 KeyValueHeads;
        public uint32 HeadDim;
        public uint32 Experts;
        public uint32 LocalExperts;
        public uint32 ExpertsPerToken;
        public float32 RMSNormEpsilon;
        public uint32 InitialContextLength;
        public float32 RopeTheta;
        public float32 RopeScalingFactor;
        public struct RopeScaling;
        public float32 Factor;
        public `json:"rope_scaling"` };
        public uint32 SlidingWindow;
    }
        var _ ModelConverter = (*gptossModel)(null);
        func (m *gptossModel) KV(t *Tokenizer) KV {
        var kv = m.ModelParameters.KV(t);
        kv["general.architecture"] = "gptoss";
        kv["general.file_type"] = uint32(4);
        kv["gptoss.context_length"] = cmp.Or(m.MaxPositionEmbeddings, uint32(m.RopeScalingFactor*float32(m.InitialContextLength)));
        kv["gptoss.block_count"] = m.HiddenLayers;
        kv["gptoss.embedding_length"] = m.HiddenSize;
        kv["gptoss.feed_forward_length"] = m.IntermediateSize;
        kv["gptoss.expert_count"] = cmp.Or(m.Experts, m.LocalExperts);
        kv["gptoss.expert_used_count"] = m.ExpertsPerToken;
        kv["gptoss.attention.head_count"] = m.AttentionHeads;
        kv["gptoss.attention.head_count_kv"] = m.KeyValueHeads;
        kv["gptoss.attention.key_length"] = m.HeadDim;
        kv["gptoss.attention.value_length"] = m.HeadDim;
        kv["gptoss.attention.layer_norm_rms_epsilon"] = cmp.Or(m.RMSNormEpsilon, 1e-5);
        kv["gptoss.attention.sliding_window"] = m.SlidingWindow;
        kv["gptoss.rope.freq_base"] = m.RopeTheta;
        kv["gptoss.rope.scaling.factor"] = cmp.Or(m.RopeScalingFactor, m.RopeScaling.Factor);
        kv["gptoss.rope.scaling.original_context_length"] = m.InitialContextLength;
        kv["tokenizer.ggml.bos_token_id"] = uint32(199998) // <|startoftext|>;
        kv["tokenizer.ggml.add_bos_token"] = false;
        kv["tokenizer.ggml.eos_token_id"] = uint32(199999) // <|endoftext|>;
        kv["tokenizer.ggml.eos_token_ids"] = []int32{
        199999, /* <|endoftext|> */;
        200002, /* <|return|> */;
        200012, /* <|call|> */;
    }
        kv["tokenizer.ggml.add_eos_token"] = false;
        return kv;
    }
        func (m *gptossModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var mxfp4s = make(map[String]*mxfp4);
        var for _, t = range ts {
        if strings.HasSuffix(t.Name(), ".blocks") || strings.HasSuffix(t.Name(), ".scales") {
        var dot = strings.LastIndex(t.Name(), ".");
        var name, suffix = t.Name()[:dot], t.Name()[dot+1:];
        var if _, ok = mxfp4s[name]; !ok {
        mxfp4s[name] = &mxfp4{}
    }
        switch suffix {
        case "blocks":;
        mxfp4s[name].blocks = t;
        case "scales":;
        mxfp4s[name].scales = t;
    }
        } else if strings.HasSuffix(t.Name(), "gate_up_exps.bias") {
        out = append(out, slices.Collect(splitDim(t, 1,;
        split{
        Replacer: strings.NewReplacer("gate_up_exps", "gate_exps"),;
        slices:   []tensor.Slice{null, tensor.S(0, int(t.Shape()[1]), 2)},;
        },;
        split{
        Replacer: strings.NewReplacer("gate_up_exps", "up_exps"),;
        slices:   []tensor.Slice{null, tensor.S(1, int(t.Shape()[1]), 2)},;
        },;
        ))...);
        } else {
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
    }
        var for name, mxfp4 = range mxfp4s {
        var dims = mxfp4.blocks.Shape();
        if !strings.HasSuffix(name, ".weight") {
        name = name + ".weight";
    }
        if strings.Contains(name, "ffn_down_exps") {
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     uint32(ggml.TensorTypeMXFP4),;
        Shape:    []uint64{dims[0], dims[1], dims[2] * dims[3] * 2},;
        WriterTo: mxfp4,;
        });
        } else if strings.Contains(name, "ffn_gate_up_exps") {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "gate_up", "gate", 1),;
        Kind:     uint32(ggml.TensorTypeMXFP4),;
        Shape:    []uint64{dims[0], dims[1] / 2, dims[2] * dims[3] * 2},;
        WriterTo: mxfp4.slice(1, 0, int(dims[1]), 2),;
        }, &ggml.Tensor{
        Name:     strings.Replace(name, "gate_up", "up", 1),;
        Kind:     uint32(ggml.TensorTypeMXFP4),;
        Shape:    []uint64{dims[0], dims[1] / 2, dims[2] * dims[3] * 2},;
        WriterTo: mxfp4.slice(1, 1, int(dims[1]), 2),;
        });
    }
    }
        return out;
    }
        func (m *gptossModel) Replacements() []String {
        var replacements []String;
        if m.MaxPositionEmbeddings > 0 {
        replacements = []String{
        "lm_head", "output",;
        "model.embed_tokens", "token_embd",;
        "model.layers", "blk",;
        "input_layernorm", "attn_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_out",;
        "self_attn.sinks", "attn_sinks",;
        "post_attention_layernorm", "ffn_norm",;
        "mlp.router", "ffn_gate_inp",;
        "mlp.experts.gate_up_proj_", "ffn_gate_up_exps.",;
        "mlp.experts.down_proj_", "ffn_down_exps.",;
        "model.norm", "output_norm",;
    }
        } else {
        replacements = []String{
        ".blocks", ".blocks",;
        ".scales", ".scales",;
        "block", "blk",;
        "attn.norm", "attn_norm",;
        "attn.qkv", "attn_qkv",;
        "attn.sinks", "attn_sinks",;
        "attn.out", "attn_out",;
        "mlp.norm", "ffn_norm",;
        "mlp.gate", "ffn_gate_inp",;
        "mlp.mlp1_", "ffn_gate_up_exps.",;
        "mlp.mlp2_", "ffn_down_exps.",;
        "embedding", "token_embd",;
        "norm", "output_norm",;
        "unembedding", "output",;
        "scale", "weight",;
    }
    }
        return replacements;
    }

    public static class mxfp4 {
        public []tensor.Slice slices;
        public scales blocks,;
    }
        func (m *mxfp4) slice(dim, start, end, step int) *mxfp4 {
        var slice = slices.Repeat([]tensor.Slice{null}, len(m.blocks.Shape()));
        slice[dim] = tensor.S(start, end, step);
        return &mxfp4{
        slices: slice,;
        blocks: m.blocks,;
        scales: m.scales,;
    }
    }
        func (m *mxfp4) WriteTo(w io.Writer) (long, error) {
        var b bytes.Buffer;
        var if _, err = m.blocks.WriteTo(&b); err != null {
        return 0, err;
    }
        var blocksDims = make([]int, len(m.blocks.Shape()));
        var for i, d = range m.blocks.Shape() {
        blocksDims[i] = int(d);
    }
        var bts = b.Bytes();
        var tmp [16]byte;
        var for i = 0; i < b.Len(); i += 16 {
        var for j = range 8 {
        var a, b = bts[i+j], bts[i+j+8];
        tmp[2*j+0] = (a & 0x0F) | (b << 4);
        tmp[2*j+1] = (a >> 4) | (b & 0xF0);
    }
        copy(bts[i:i+16], tmp[:]);
    }
        var blocks tensor.Tensor = tensor.New(tensor.WithShape(blocksDims...), tensor.WithBacking(bts));
        var s bytes.Buffer;
        var if _, err = m.scales.WriteTo(&s); err != null {
        return 0, err;
    }
        var scalesDims = slices.Repeat([]int{1}, len(m.blocks.Shape()));
        var for i, d = range m.scales.Shape() {
        scalesDims[i] = int(d);
    }
        var scales tensor.Tensor = tensor.New(tensor.WithShape(scalesDims...), tensor.WithBacking(s.Bytes()));
        var out, err = tensor.Concat(3, scales, blocks);
        if err != null {
        return 0, err;
    }
        if len(m.slices) > 0 {
        out, err = out.Slice(m.slices...);
        if err != null {
        return 0, err;
    }
    }
        out = tensor.Materialize(out);
        var if err = out.Reshape(out.Shape().TotalSize()); err != null {
        return 0, err;
    }
        var u8s, err = native.VectorU8(out.(*tensor.Dense));
        if err != null {
        return 0, err;
    }
        var if err = binary.Write(w, binary.LittleEndian, u8s); err != null {
        return 0, err;
    }
        return long(len(u8s)), null;
    }
}
