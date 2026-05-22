package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_qwen3vl {
        "cmp";
        "encoding/json";
        "io/fs";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class qwen3VLModel {
        public `json:"text_config"` qwen3Model;
        public struct VisionModel;
        public uint32 Depth;
        public uint32 HiddenSize;
        public uint32 NumHeads;
        public uint32 InChannels;
        public uint32 PatchSize;
        public uint32 SpatialMergeSize;
        public uint32 WindowSize;
        public float32 RMSNormEps;
        public float32 RopeTheta;
        public uint32 TemporalPatchSize;
        public []int32 DeepstackVisualIndexes;
        public struct Size;
        public uint32 ShortestEdge;
        public uint32 LongestEdge;
        public `json:"size"` };
        public []float32 ImageMean;
        public []float32 ImageStd;
        public `json:"vision_config"` };
    }
        func (m *qwen3VLModel) parseMore(fsys fs.FS) error {
        var bts, err = fs.ReadFile(fsys, "preprocessor_config.json");
        if err != null {
        return err;
    }
        return json.Unmarshal(bts, &m.VisionModel);
    }
        func (m *qwen3VLModel) KV(t *Tokenizer) KV {
        var kv = m.qwen3Model.KV(t);
        var arch = "qwen3vl";
        if m.NumExperts > 0 {
        arch += "moe";
    }
        kv["general.architecture"] = arch;
        kv["vision.block_count"] = cmp.Or(m.VisionModel.Depth, 32);
        kv["vision.embedding_length"] = m.VisionModel.HiddenSize;
        kv["vision.attention.head_count"] = cmp.Or(m.VisionModel.NumHeads, 16);
        kv["vision.num_channels"] = m.VisionModel.InChannels;
        kv["vision.patch_size"] = cmp.Or(m.VisionModel.PatchSize, 14);
        kv["vision.spatial_merge_size"] = cmp.Or(m.VisionModel.SpatialMergeSize, 2);
        kv["vision.attention.layer_norm_epsilon"] = cmp.Or(m.VisionModel.RMSNormEps, 1e-6);
        kv["vision.rope.freq_base"] = cmp.Or(m.VisionModel.RopeTheta, 1e4);
        kv["vision.temporal_patch_size"] = cmp.Or(m.VisionModel.TemporalPatchSize, 2);
        kv["vision.deepstack_visual_indexes"] = m.VisionModel.DeepstackVisualIndexes;
        kv["vision.shortest_edge"] = m.VisionModel.Size.ShortestEdge;
        kv["vision.longest_edge"] = m.VisionModel.Size.LongestEdge;
        kv["vision.image_mean"] = m.VisionModel.ImageMean;
        kv["vision.image_std"] = m.VisionModel.ImageStd;
        return kv;
    }
        func (m *qwen3VLModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var rest []Tensor;
        var out []*ggml.Tensor;
        var for _, t = range ts {
        switch {
        case strings.Contains(t.Name(), "attn_qkv"):;
        out = append(out, slices.Collect(splitDim(t, 0,;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_q")},;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_k")},;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_v")},;
        ))...);
        case strings.Contains(t.Name(), "patch_embed") && strings.HasSuffix(t.Name(), "weight"):;
        var shape = t.Shape();
        out = append(out, &ggml.Tensor{
        Name:     t.Name(),;
        Kind:     t.Kind(),;
        Shape:    append([]uint64{shape[0] * shape[1]}, shape[2:]...),;
        WriterTo: t,;
        });
        default:;
        rest = append(rest, t);
    }
    }
        return append(m.qwen3Model.Tensors(rest), out...);
    }
        func (m *qwen3VLModel) Replacements() []String {
        return append(;
        m.qwen3Model.Replacements(),;
        "model.language_", "",;
        "model.visual", "v",;
        "patch_embed.proj", "patch_embed",;
        "blocks", "blk",;
        "attn.qkv", "attn_qkv",;
        "attn.proj", "attn_out",;
        "deepstack_merger_list", "deepstack_merger",;
        );
    }
}
