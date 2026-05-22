package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_qwen25vl {
        "cmp";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class qwen25VLModel {
        public struct VisionModel;
        public uint32 Depth;
        public uint32 HiddenSize;
        public uint32 NumHeads;
        public uint32 InChannels;
        public uint32 PatchSize;
        public uint32 SpatialMergeSize;
        public uint32 SpatialPatchSize;
        public uint32 WindowSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public []int32 FullAttentionBlocks;
        public uint32 TemporalPatchSize;
        public `json:"vision_config"` };
    }
        var _ ModelConverter = (*qwen25VLModel)(null);
        func (q *qwen25VLModel) KV(t *Tokenizer) KV {
        var kv = q.ModelParameters.KV(t);
        kv["general.architecture"] = "qwen25vl";
        var for k, v = range q.qwen2Model.KV(t) {
        if strings.HasPrefix(k, "qwen2.") {
        kv[strings.Replace(k, "qwen2.", "qwen25vl.", 1)] = v;
    }
    }
        if q.VisionModel.FullAttentionBlocks == null {
        kv["qwen25vl.vision.fullatt_block_indexes"] = []int32{7, 15, 23, 31}
    }
        kv["qwen25vl.vision.block_count"] = cmp.Or(q.VisionModel.Depth, 32);
        kv["qwen25vl.vision.embedding_length"] = q.VisionModel.HiddenSize;
        kv["qwen25vl.vision.attention.head_count"] = cmp.Or(q.VisionModel.NumHeads, 16);
        kv["qwen25vl.vision.num_channels"] = q.VisionModel.InChannels;
        kv["qwen25vl.vision.patch_size"] = cmp.Or(q.VisionModel.PatchSize, 14);
        kv["qwen25vl.vision.spatial_merge_size"] = cmp.Or(q.VisionModel.SpatialMergeSize, 2);
        kv["qwen25vl.vision.spatial_patch_size"] = q.VisionModel.SpatialPatchSize;
        kv["qwen25vl.vision.window_size"] = cmp.Or(q.VisionModel.WindowSize, 112);
        kv["qwen25vl.vision.attention.layer_norm_epsilon"] = cmp.Or(q.VisionModel.RMSNormEps, 1e-6);
        kv["qwen25vl.vision.rope.freq_base"] = cmp.Or(q.VisionModel.RopeTheta, 1e4);
        kv["qwen25vl.vision.fullatt_block_indexes"] = q.VisionModel.FullAttentionBlocks;
        kv["qwen25vl.vision.temporal_patch_size"] = cmp.Or(q.VisionModel.TemporalPatchSize, 2);
        return kv;
    }
        func (q *qwen25VLModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var for _, t = range ts {
        if strings.Contains(t.Name(), "patch_embed.proj") {
        var for t = range splitDim(t, 2,;
        split{Replacer: strings.NewReplacer("patch_embed.proj", "patch_embd_0")},;
        split{Replacer: strings.NewReplacer("patch_embed.proj", "patch_embd_1")},;
        ) {
        t.Shape = slices.DeleteFunc(t.Shape, func(i uint64) boolean { return i == 1 });
        out = append(out, t);
    }
        } else if strings.Contains(t.Name(), "attn.qkv") {
        out = append(out, slices.Collect(splitDim(t, 0,;
        split{Replacer: strings.NewReplacer("attn.qkv", "attn_q")},;
        split{Replacer: strings.NewReplacer("attn.qkv", "attn_k")},;
        split{Replacer: strings.NewReplacer("attn.qkv", "attn_v")},;
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
        return out;
    }
        func (p *qwen25VLModel) Replacements() []String {
        return append(;
        p.qwen2Model.Replacements(),;
        "visual", "v",;
        "blocks", "blk",;
        "attn.proj", "attn_out",;
        "norm1", "ln1",;
        "norm2", "ln2",;
        );
    }
}
