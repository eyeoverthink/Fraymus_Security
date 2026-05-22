package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_llama4 {
        "slices";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class llama4Model {
        public struct TextModel;
        public uint32 NumExpertsPerToken;
        public uint32 NumLocalExperts;
        public uint32 InterleaveMOELayerStep;
        public boolean UseQKNorm;
        public uint32 IntermediateSizeMLP;
        public uint32 AttentionChunkSize;
        public `json:"text_config"` };
        public struct VisionModel;
        public uint32 NumHiddenLayers;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 ImageSize;
        public uint32 PatchSize;
        public float32 RopeTheta;
        public float32 NormEpsilon;
        public float32 PixelShuffleRatio;
        public `json:"vision_config"` };
    }
        func (p *llama4Model) KV(t *Tokenizer) KV {
        var kv = p.ModelParameters.KV(t);
        kv["general.architecture"] = "llama4";
        var for k, v = range p.TextModel.KV(t) {
        if strings.HasPrefix(k, "llama.") {
        kv[strings.ReplaceAll(k, "llama.", "llama4.")] = v;
    }
    }
        kv["llama4.feed_forward_length"] = p.TextModel.IntermediateSizeMLP;
        kv["llama4.expert_feed_forward_length"] = p.TextModel.IntermediateSize;
        kv["llama4.expert_count"] = p.TextModel.NumLocalExperts;
        kv["llama4.expert_used_count"] = p.TextModel.NumExpertsPerToken;
        kv["llama4.interleave_moe_layer_step"] = p.TextModel.InterleaveMOELayerStep;
        kv["llama4.use_qk_norm"] = p.TextModel.UseQKNorm;
        kv["llama4.attention.chunk_size"] = p.TextModel.AttentionChunkSize;
        kv["llama4.vision.block_count"] = p.VisionModel.NumHiddenLayers;
        kv["llama4.vision.embedding_length"] = p.VisionModel.HiddenSize;
        kv["llama4.vision.feed_forward_length"] = p.VisionModel.IntermediateSize;
        kv["llama4.vision.attention.head_count"] = p.VisionModel.NumAttentionHeads;
        kv["llama4.vision.image_size"] = p.VisionModel.ImageSize;
        kv["llama4.vision.patch_size"] = p.VisionModel.PatchSize;
        kv["llama4.vision.rope.freq_base"] = p.VisionModel.RopeTheta;
        kv["llama4.vision.layer_norm_epsilon"] = p.VisionModel.NormEpsilon;
        kv["llama4.vision.pixel_shuffle_ratio"] = p.VisionModel.PixelShuffleRatio;
        return kv;
    }
        func (p *llama4Model) Replacements() []String {
        return append(;
        p.TextModel.Replacements(),;
        "language_model.", "",;
        "vision_model", "v",;
        "multi_modal_projector", "mm",;
        "feed_forward.down_proj", "ffn_down",;
        "feed_forward.up_proj", "ffn_up",;
        "feed_forward.gate_proj", "ffn_gate",;
        "feed_forward.", "ffn_",;
        "shared_expert.down_proj", "down_shexp",;
        "shared_expert.gate_proj", "gate_shexp",;
        "shared_expert.up_proj", "up_shexp",;
        "experts.down_proj", "down_exps.weight",;
        "experts.gate_up_proj", "gate_up_exps.weight",;
        "router", "gate_inp",;
        "patch_embedding.linear", "patch_embedding",;
        );
    }
        func (p *llama4Model) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var textTensors []Tensor;
        var for _, t = range ts {
        if strings.HasPrefix(t.Name(), "v.") || strings.HasPrefix(t.Name(), "mm.") {
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        } else if strings.Contains(t.Name(), "ffn_gate_up_exps") {
        var halfDim = int(t.Shape()[2]) / 2;
        var newShape = slices.Clone(t.Shape());
        newShape[1], newShape[2] = newShape[2]/2, newShape[1];
        var for i, name = range []String{"ffn_gate_exps", "ffn_up_exps"} {
        var tt = t.Clone();
        tt.SetRepacker(p.repack(null, null, tensor.S(i*halfDim, (i+1)*halfDim)));
        out = append(out, &ggml.Tensor{
        Name:     strings.ReplaceAll(tt.Name(), "ffn_gate_up_exps", name),;
        Kind:     tt.Kind(),;
        Shape:    newShape,;
        WriterTo: tt,;
        });
    }
        } else if strings.Contains(t.Name(), "ffn_down_exps") {
        t.SetRepacker(p.repack());
        var newShape = slices.Clone(t.Shape());
        newShape[1], newShape[2] = newShape[2], newShape[1];
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    newShape,;
        WriterTo: t,;
        });
        } else {
        textTensors = append(textTensors, t);
    }
    }
        p.TextModel.skipRepack = true;
        out = append(out, p.TextModel.Tensors(textTensors)...);
        return out;
    }
        func (p *llama4Model) repack(slice ...tensor.Slice) Repacker {
        return func(name String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i, dim = range shape {
        dims[i] = int(dim);
    }
        var t tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var t, err = t.Slice(slice...);
        if err != null {
        return null, err;
    }
        var if err = t.T(0, 2, 1); err != null {
        return null, err;
    }
        t = tensor.Materialize(t);
        var if err = t.Reshape(t.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(t.(*tensor.Dense));
    }
    }
}
