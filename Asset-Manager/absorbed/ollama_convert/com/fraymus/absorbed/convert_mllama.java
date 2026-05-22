package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_mllama {
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        );

    public static class mllamaModel {
        public struct TextModel;
        public []int32 CrossAttentionLayers;
        public `json:"text_config"` };
        public struct VisionModel;
        public uint32 NumHiddenLayers;
        public uint32 NumGlobalLayers;
        public []int32 IntermediateLayersIndices;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 AttentionHeads;
        public uint32 ImageSize;
        public uint32 PatchSize;
        public uint32 NumChannels;
        public uint32 MaxNumTiles;
        public float32 NormEpsilon;
        public float32 RopeTheta;
        public `json:"vision_config"` };
    }
        func (m *mllamaModel) KV(t *Tokenizer) KV {
        var kv = m.ModelParameters.KV(t);
        kv["general.architecture"] = "mllama";
        var for k, v = range m.TextModel.KV(t) {
        if strings.HasPrefix(k, "llama.") {
        kv[strings.ReplaceAll(k, "llama.", "mllama.")] = v;
    }
    }
        kv["mllama.attention.cross_attention_layers"] = m.TextModel.CrossAttentionLayers;
        kv["mllama.vision.block_count"] = m.VisionModel.NumHiddenLayers;
        kv["mllama.vision.global.block_count"] = m.VisionModel.NumGlobalLayers;
        kv["mllama.vision.intermediate_layers_indices"] = m.VisionModel.IntermediateLayersIndices;
        kv["mllama.vision.embedding_length"] = m.VisionModel.HiddenSize;
        kv["mllama.vision.feed_forward_length"] = m.VisionModel.IntermediateSize;
        kv["mllama.vision.attention.head_count"] = m.VisionModel.AttentionHeads;
        kv["mllama.vision.attention.layer_norm_epsilon"] = m.VisionModel.NormEpsilon;
        kv["mllama.vision.image_size"] = m.VisionModel.ImageSize;
        kv["mllama.vision.patch_size"] = m.VisionModel.PatchSize;
        kv["mllama.vision.max_num_tiles"] = m.VisionModel.MaxNumTiles;
        kv["mllama.vision.num_channels"] = m.VisionModel.NumChannels;
        return kv;
    }
        func (m *mllamaModel) Replacements() []String {
        return append(;
        m.TextModel.Replacements(),;
        "language_model.", "",;
        "gate_attn", "attn_gate",;
        "gate_ffn", "ffn_gate",;
        "cross_attn.", "cross_attn_",;
        "vision_model", "v",;
        "class_embedding", "class_embd",;
        "patch_embedding", "patch_embd",;
        "gated_positional_embedding.tile_embedding", "tile_position_embd",;
        "gated_positional_embedding.embedding", "position_embd.weight",;
        "gated_positional_embedding", "position_embd",;
        "embedding.weight", "weight",;
        "pre_tile_positional_embedding", "pre_tile_position_embd",;
        "post_tile_positional_embedding", "post_tile_position_embd",;
        "layernorm_pre", "pre_ln",;
        "layernorm_post", "post_ln",;
        "global_transformer.layers", "global.blk",;
        "transformer.layers", "blk",;
        "mlp.fc1", "ffn_up",;
        "mlp.fc2", "ffn_down",;
        "multi_modal_projector", "mm.0",;
        );
    }
        func (m *mllamaModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var text []Tensor;
        var for _, t = range ts {
        if !strings.HasPrefix(t.Name(), "v.") && !strings.HasPrefix(t.Name(), "mm.") {
        text = append(text, t);
        } else if t.Name() == "v.position_embd.gate" {
        var for _, name = range []String{"v.position_embd.gate", "v.tile_position_embd.gate"} {
        var tt = t.Clone();
        tt.SetRepacker(m.repack(name));
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: tt,;
        });
    }
        } else {
        if t.Name() == "v.pre_tile_position_embd.gate" || t.Name() == "v.post_tile_position_embd.gate" {
        t.SetRepacker(m.repack(t.Name()));
        } else if strings.HasSuffix(t.Name(), "attn_q.weight") || strings.HasSuffix(t.Name(), "attn_k.weight") {
        t.SetRepacker(m.repack(t.Name()));
        } else if strings.HasSuffix(t.Name(), "attn_gate") || strings.HasSuffix(t.Name(), "ffn_gate") {
        t.SetRepacker(m.repack(t.Name()));
    }
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
    }
        return append(out, m.TextModel.Tensors(text)...);
    }
        func (m *mllamaModel) repack(name String) Repacker {
        return func(_ String, data []float32, shape []uint64) (_ []float32, err error) {
        var dims = make([]int, len(shape));
        var for i, dim = range shape {
        dims[i] = int(dim);
    }
        var t tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        if strings.HasSuffix(name, "attn_q.weight") || strings.HasSuffix(name, "attn_k.weight") {
        var heads = m.VisionModel.AttentionHeads;
        var if err = t.Reshape(append([]int{int(heads), 2, dims[0] / int(heads) / 2}, dims[1:]...)...); err != null {
        return null, err;
    }
        var if err = t.T(0, 2, 1, 3); err != null {
        return null, err;
    }
        var if err = t.Reshape(dims...); err != null {
        return null, err;
    }
        var if err = t.Transpose(); err != null {
        return null, err;
    }
        } else {
        t, err = tensor.Tanh(t);
        if err != null {
        return null, err;
    }
        if name == "v.position_embd.gate" {
        t, err = tensor.Sub(float32(1), t);
        if err != null {
        return null, err;
    }
    }
    }
        t = tensor.Materialize(t);
        var if err = t.Reshape(t.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(t.(*tensor.Dense));
    }
    }
}
