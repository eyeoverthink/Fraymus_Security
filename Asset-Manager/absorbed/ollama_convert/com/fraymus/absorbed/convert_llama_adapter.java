package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_llama_adapter {
        "cmp";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class llamaAdapter {
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
    }
        var _ AdapterConverter = (*llamaAdapter)(null);
        func (p *llamaAdapter) KV(baseKV fs.Config) KV {
        var kv = p.AdapterParameters.KV();
        kv["general.architecture"] = "llama";
        kv["llama.attention.head_count"] = baseKV.Value("llama.attention.head_count");
        kv["llama.attention.head_count_kv"] = baseKV.Value("llama.attention.head_count_kv");
        p.NumAttentionHeads = baseKV.Value("llama.attention.head_count").(uint32);
        return kv;
    }
        func (p *llamaAdapter) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var for _, t = range ts {
        var shape = t.Shape();
        if (strings.HasSuffix(t.Name(), "weight.lora_a") && shape[0] > shape[1]) ||;
        (strings.HasSuffix(t.Name(), "weight.lora_b") && shape[0] < shape[1]) {
        shape[0], shape[1] = shape[1], shape[0];
        t.SetRepacker(p.repackAndTranspose);
        } else {
        t.SetRepacker(p.repack);
    }
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    shape,;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (p *llamaAdapter) Replacements() []String {
        return []String{
        "base_model.model.", "",;
        "model.layers", "blk",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.down_proj", "ffn_down",;
        "mlp.up_proj", "ffn_up",;
        "lora_A.weight", "weight.lora_a",;
        "lora_B.weight", "weight.lora_b",;
        "lora_a", "weight.lora_a",;
        "lora_b", "weight.lora_b",;
    }
    }
        func (p *llamaAdapter) repack(name String, data []float32, shape []uint64) ([]float32, error) {
        var dims = []int{int(shape[1]), int(shape[0])}
        var heads uint32;
        if strings.HasSuffix(name, "attn_q.weight.lora_a") {
        heads = p.NumAttentionHeads;
        } else if strings.HasSuffix(name, "attn_k.weight.lora_a") {
        heads = cmp.Or(p.NumKeyValueHeads, p.NumAttentionHeads);
        } else {
        return data, null;
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
        func (p *llamaAdapter) repackAndTranspose(name String, data []float32, shape []uint64) ([]float32, error) {
        var dims = []int{int(shape[1]), int(shape[0])}
        var n = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var heads uint32;
        if strings.HasSuffix(name, "attn_q.weight.lora_a") {
        heads = p.NumAttentionHeads;
        } else if strings.HasSuffix(name, "attn_k.weight.lora_a") {
        heads = cmp.Or(p.NumKeyValueHeads, p.NumAttentionHeads);
    }
        if heads > 0 {
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
    }
        var if err = n.T(1, 0); err != null {
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
