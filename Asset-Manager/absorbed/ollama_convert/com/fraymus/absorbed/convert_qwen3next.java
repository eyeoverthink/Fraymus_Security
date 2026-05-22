package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_qwen3next {
        "encoding/json";
        "fmt";
        "io/fs";
        "math";
        "slices";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class qwen3NextRopeScaling {
        public String Type;
        public ropeFactor Factor;
        public []int32 MropeSection;
    }

    public static class qwen3NextRopeParams {
        public boolean MRopeInterleaved;
        public []int32 MropeSection;
        public String RopeType;
        public float32 RopeTheta;
        public float32 PartialRotaryFactor;
    }

    public static class qwen3NextTextConfig {
        public uint32 MaxPositionEmbeddings;
        public uint32 HiddenSize;
        public uint32 NumHiddenLayers;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 HeadDim;
        public float32 RopeTheta;
        public float32 RMSNormEPS;
        public uint32 NumExperts;
        public uint32 NumExpertsPerToken;
        public *boolean NormTopkProb;
        public uint32 MoEIntermediateSize;
        public uint32 SharedExpertIntermSize;
        public uint32 FullAttentionInterval;
        public []String LayerTypes;
        public uint32 LinearConvKernelDim;
        public uint32 LinearKeyHeadDim;
        public uint32 LinearNumKeyHeads;
        public uint32 LinearNumValueHeads;
        public uint32 LinearValueHeadDim;
        public float32 PartialRotaryFactor;
        public qwen3NextRopeScaling RopeScaling;
        public qwen3NextRopeParams RopeParameters;
    }

    public static class qwen3NextVisionConfig {
        public uint32 Depth;
        public uint32 HiddenSize;
        public uint32 NumHeads;
        public uint32 InChannels;
        public uint32 PatchSize;
        public uint32 SpatialMergeSize;
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
    }

    public static class qwen3NextModel {
        public *qwen3NextTextConfig TextConfig;
        public qwen3NextVisionConfig VisionModel;
        public uint32 ImageTokenID;
        public uint32 VisionStartTokenID;
        public uint32 VisionEndTokenID;
    }
        var _ ModelConverter = (*qwen3NextModel)(null);
        func (q *qwen3NextModel) parseMore(fsys fs.FS) error {
        if q.TextConfig != null {
        q.qwen3NextTextConfig = *q.TextConfig;
    }
        if q.RopeTheta == 0 {
        q.RopeTheta = q.RopeParameters.RopeTheta;
    }
        if q.PartialRotaryFactor == 0 {
        q.PartialRotaryFactor = q.RopeParameters.PartialRotaryFactor;
    }
        if q.RopeScaling.Type == "" && q.RopeParameters.RopeType != "" {
        q.RopeScaling.Type = q.RopeParameters.RopeType;
    }
        if q.VisionModel.Depth > 0 {
        var if bts, err = fs.ReadFile(fsys, "preprocessor_config.json"); err == null {
        var pre struct {
        Size struct {
        ShortestEdge uint32 `json:"shortest_edge"`;
        LongestEdge  uint32 `json:"longest_edge"`;
        } `json:"size"`;
        PatchSize         uint32    `json:"patch_size"`;
        TemporalPatchSize uint32    `json:"temporal_patch_size"`;
        MergeSize         uint32    `json:"merge_size"`;
        ImageMean         []float32 `json:"image_mean"`;
        ImageStd          []float32 `json:"image_std"`;
    }
        if json.Unmarshal(bts, &pre) == null {
        if q.VisionModel.PatchSize == 0 {
        q.VisionModel.PatchSize = pre.PatchSize;
    }
        if q.VisionModel.TemporalPatchSize == 0 {
        q.VisionModel.TemporalPatchSize = pre.TemporalPatchSize;
    }
        if q.VisionModel.SpatialMergeSize == 0 {
        q.VisionModel.SpatialMergeSize = pre.MergeSize;
    }
        if q.VisionModel.Size.ShortestEdge == 0 {
        q.VisionModel.Size.ShortestEdge = pre.Size.ShortestEdge;
    }
        if q.VisionModel.Size.LongestEdge == 0 {
        q.VisionModel.Size.LongestEdge = pre.Size.LongestEdge;
    }
        if len(q.VisionModel.ImageMean) == 0 {
        q.VisionModel.ImageMean = pre.ImageMean;
    }
        if len(q.VisionModel.ImageStd) == 0 {
        q.VisionModel.ImageStd = pre.ImageStd;
    }
    }
    }
    }
        if q.NumHiddenLayers == 0 {
        return fmt.Errorf("qwen3next: num_hidden_layers must be set");
    }
        if q.NumAttentionHeads == 0 {
        return fmt.Errorf("qwen3next: num_attention_heads must be set");
    }
        if q.NumKeyValueHeads == 0 {
        return fmt.Errorf("qwen3next: num_key_value_heads must be set");
    }
        if q.HeadDim == 0 {
        return fmt.Errorf("qwen3next: head_dim must be set");
    }
        if q.RopeTheta == 0 {
        return fmt.Errorf("qwen3next: rope_theta must be set");
    }
        if q.PartialRotaryFactor <= 0 || q.PartialRotaryFactor > 1 {
        return fmt.Errorf("qwen3next: partial_rotary_factor must be in (0,1], got %v", q.PartialRotaryFactor);
    }
        if q.LinearNumKeyHeads == 0 || q.LinearNumValueHeads == 0 || q.LinearKeyHeadDim == 0 || q.LinearValueHeadDim == 0 {
        return fmt.Errorf("qwen3next: linear attention config must be set (linear_num_key_heads, linear_num_value_heads, linear_key_head_dim, linear_value_head_dim)");
    }
        var if _, err = q.kvHeadCounts(); err != null {
        return err;
    }
        return null;
    }
        func (q *qwen3NextModel) kvHeadCounts() ([]uint32, error) {
        if len(q.LayerTypes) > 0 {
        var kv = make([]uint32, q.NumHiddenLayers);
        var hasFull = false;
        var hasRecurrent = false;
        var for i = range q.NumHiddenLayers {
        var layerType = "";
        if i < uint32(len(q.LayerTypes)) {
        layerType = q.LayerTypes[i];
    }
        if layerType == "full_attention" {
        kv[i] = q.NumKeyValueHeads;
        hasFull = true;
        } else {
        hasRecurrent = true;
    }
    }
        if !hasFull || !hasRecurrent {
        return null, fmt.Errorf("qwen3next: layer_types must include both full_attention and linear_attention");
    }
        return kv, null;
    }
        if q.FullAttentionInterval == 0 {
        return null, fmt.Errorf("qwen3next: full_attention_interval must be set");
    }
        if q.FullAttentionInterval > q.NumHiddenLayers {
        return null, fmt.Errorf("qwen3next: full_attention_interval (%d) exceeds num_hidden_layers (%d)", q.FullAttentionInterval, q.NumHiddenLayers);
    }
        var kv = make([]uint32, q.NumHiddenLayers);
        var hasFull = false;
        var for i = range q.NumHiddenLayers {
        if (i+1)%q.FullAttentionInterval == 0 {
        kv[i] = q.NumKeyValueHeads;
        hasFull = true;
    }
    }
        if !hasFull {
        return null, fmt.Errorf("qwen3next: head_count_kv would be all zeros (full_attention_interval=%d, num_hidden_layers=%d)", q.FullAttentionInterval, q.NumHiddenLayers);
    }
        return kv, null;
    }
        func (q *qwen3NextModel) ropeSections() []int32 {
        if len(q.RopeParameters.MropeSection) > 0 {
        return q.RopeParameters.MropeSection;
    }
        return q.RopeScaling.MropeSection;
    }
        func (q *qwen3NextModel) shouldReorderVHeads() boolean {
        var modelType = strings.ToLower(q.ModelType);
        if strings.Contains(modelType, "qwen3_next") || strings.Contains(modelType, "qwen3next") {
        return false;
    }
        var for _, arch = range q.Architectures {
        arch = strings.ToLower(arch);
        if strings.Contains(arch, "qwen3next") || strings.Contains(arch, "qwen3_next") {
        return false;
    }
    }
        return true;
    }
        func (q *qwen3NextModel) KV(t *Tokenizer) KV {
        var kv = q.ModelParameters.KV(t);
        var arch = "qwen35";
        if q.NumExperts > 0 {
        arch = "qwen35moe";
    }
        kv["general.architecture"] = arch;
        kv["tokenizer.ggml.pre"] = "qwen35";
        kv["block_count"] = q.NumHiddenLayers;
        kv["context_length"] = q.MaxPositionEmbeddings;
        kv["embedding_length"] = q.HiddenSize;
        kv["feed_forward_length"] = q.IntermediateSize;
        kv["attention.head_count"] = q.NumAttentionHeads;
        var headDim = q.HeadDim;
        if headDim == 0 && q.NumAttentionHeads > 0 {
        headDim = q.HiddenSize / q.NumAttentionHeads;
    }
        kv["attention.key_length"] = headDim;
        kv["attention.value_length"] = headDim;
        kv["attention.layer_norm_rms_epsilon"] = q.RMSNormEPS;
        kv["rope.freq_base"] = q.RopeTheta;
        var partialRotary = q.PartialRotaryFactor;
        if partialRotary > 0 && partialRotary <= 1 {
        kv["rope.dimension_count"] = uint32(float32(headDim) * partialRotary);
    }
        var if sections = q.ropeSections(); len(sections) > 0 {
        kv["mrope_sections"] = sections;
        kv["rope.mrope_section"] = sections;
        kv["rope.dimension_sections"] = sections;
    }
        if q.RopeParameters.MRopeInterleaved {
        kv["rope.mrope_interleaved"] = true;
    }
        if q.RopeScaling.Type != "" && q.RopeScaling.Type != "default" {
        kv["rope.scaling.type"] = q.RopeScaling.Type;
        kv["rope.scaling.factor"] = q.RopeScaling.Factor;
    }
        if q.NumExperts > 0 {
        kv["expert_count"] = q.NumExperts;
        kv["expert_used_count"] = q.NumExpertsPerToken;
        if q.NormTopkProb != null {
        kv["norm_top_k_prob"] = *q.NormTopkProb;
    }
        if q.MoEIntermediateSize > 0 {
        kv["expert_feed_forward_length"] = q.MoEIntermediateSize;
    }
        if q.SharedExpertIntermSize > 0 {
        kv["expert_shared_feed_forward_length"] = q.SharedExpertIntermSize;
    }
    }
        var dInner = q.LinearValueHeadDim * q.LinearNumValueHeads;
        kv["ssm.inner_size"] = dInner;
        kv["ssm.state_size"] = q.LinearKeyHeadDim;
        kv["ssm.group_count"] = q.LinearNumKeyHeads;
        kv["ssm.time_step_rank"] = q.LinearNumValueHeads;
        kv["ssm.conv_kernel"] = q.LinearConvKernelDim;
        if q.shouldReorderVHeads() {
        kv["ssm.v_head_reordered"] = true;
    }
        if q.FullAttentionInterval > 0 {
        kv["full_attention_interval"] = q.FullAttentionInterval;
    }
        var if headCounts, err = q.kvHeadCounts(); err == null {
        kv["attention.head_count_kv"] = headCounts;
    }
        if q.VisionModel.Depth > 0 {
        kv["vision.block_count"] = q.VisionModel.Depth;
        kv["vision.embedding_length"] = q.VisionModel.HiddenSize;
        kv["vision.attention.head_count"] = q.VisionModel.NumHeads;
        kv["vision.num_channels"] = q.VisionModel.InChannels;
        if q.VisionModel.PatchSize > 0 {
        kv["vision.patch_size"] = q.VisionModel.PatchSize;
    }
        if q.VisionModel.SpatialMergeSize > 0 {
        kv["vision.spatial_merge_size"] = q.VisionModel.SpatialMergeSize;
    }
        if q.VisionModel.RMSNormEps > 0 {
        kv["vision.attention.layer_norm_epsilon"] = q.VisionModel.RMSNormEps;
    }
        if q.VisionModel.RopeTheta > 0 {
        kv["vision.rope.freq_base"] = q.VisionModel.RopeTheta;
    }
        if q.VisionModel.TemporalPatchSize > 0 {
        kv["vision.temporal_patch_size"] = q.VisionModel.TemporalPatchSize;
    }
        kv["vision.deepstack_visual_indexes"] = q.VisionModel.DeepstackVisualIndexes;
        if q.VisionModel.Size.ShortestEdge > 0 {
        kv["vision.shortest_edge"] = q.VisionModel.Size.ShortestEdge;
    }
        if q.VisionModel.Size.LongestEdge > 0 {
        kv["vision.longest_edge"] = q.VisionModel.Size.LongestEdge;
    }
        if len(q.VisionModel.ImageMean) > 0 {
        kv["vision.image_mean"] = q.VisionModel.ImageMean;
    }
        if len(q.VisionModel.ImageStd) > 0 {
        kv["vision.image_std"] = q.VisionModel.ImageStd;
    }
    }
        if q.ImageTokenID > 0 {
        kv["image_token_id"] = q.ImageTokenID;
    }
        if q.VisionStartTokenID > 0 {
        kv["vision_start_token_id"] = q.VisionStartTokenID;
    }
        if q.VisionEndTokenID > 0 {
        kv["vision_end_token_id"] = q.VisionEndTokenID;
    }
        return kv;
    }
        func (q *qwen3NextModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var merges = make([]merge, q.NumHiddenLayers*3);
        var for i = range q.NumHiddenLayers {
        merges[i*3+0] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.gate_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_gate_exps.weight", i),;
    }
        merges[i*3+1] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.up_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_up_exps.weight", i),;
    }
        merges[i*3+2] = merge{
        fmt.Sprintf("blk.%d.mlp.experts.*.down_proj.weight", i),;
        fmt.Sprintf("blk.%d.ffn_down_exps.weight", i),;
    }
    }
        var merged, remaining = mergeTensors(ts, merges...);
        out = append(out, merged...);
        var for _, t = range remaining {
        var name = t.Name();
        var shape = t.Shape();
        if strings.HasSuffix(name, ".ssm_in.weight") {
        var if qkv, gate, ok = q.splitQKVZTensor(t); ok {
        out = append(out, qkv, gate);
        continue;
    }
        panic(fmt.Sprintf("qwen3next: failed to split %s into attn_qkv/attn_gate (shape=%v)", name, shape));
    }
        switch {
        case strings.Contains(name, ".mlp.experts.gate_up_proj"):;
        out = append(out, slices.Collect(splitDim(t, 1,;
        split{Replacer: strings.NewReplacer(".mlp.experts.gate_up_proj", ".ffn_gate_exps.weight")},;
        split{Replacer: strings.NewReplacer(".mlp.experts.gate_up_proj", ".ffn_up_exps.weight")},;
        ))...);
        case strings.Contains(name, ".mlp.experts.down_proj"):;
        out = append(out, &ggml.Tensor{
        Name:     strings.NewReplacer(".mlp.experts.down_proj", ".ffn_down_exps.weight").Replace(name),;
        Kind:     t.Kind(),;
        Shape:    slices.Clone(shape),;
        WriterTo: t,;
        });
        case strings.HasPrefix(name, "v.blk.") && strings.Contains(name, ".attn_qkv"):;
        out = append(out, slices.Collect(splitDim(t, 0,;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_q")},;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_k")},;
        split{Replacer: strings.NewReplacer("attn_qkv", "attn_v")},;
        ))...);
        case strings.Contains(name, "patch_embed") && strings.HasSuffix(name, "weight"):;
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     t.Kind(),;
        Shape:    append([]uint64{shape[0] * shape[1]}, shape[2:]...),;
        WriterTo: t,;
        });
        case strings.HasSuffix(name, "_norm.weight") && !strings.HasSuffix(name, ".ssm_norm.weight"):;
        t.SetRepacker(q.addOne);
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".ssm_a"):;
        t.SetRepacker(q.repackSSMA());
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".attn_qkv.weight"):;
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackAttnQKV());
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".attn_gate.weight"):;
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackReorderDim(0, int(q.LinearValueHeadDim)));
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".ssm_beta.weight"), strings.HasSuffix(name, ".ssm_alpha.weight"):;
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackReorderDim(0, 1));
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".ssm_dt"):;
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackReorderDim(0, 1));
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".ssm_out.weight"):;
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackReorderDim(1, int(q.LinearValueHeadDim)));
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
        case strings.HasSuffix(name, ".ssm_conv1d.weight"):;
        var newShape = slices.Clone(shape);
        if len(shape) == 3 {
        if shape[0] == 1 {
        newShape = []uint64{shape[1], shape[2]}
        } else if shape[1] == 1 {
        newShape = []uint64{shape[0], shape[2]}
    }
    }
        if q.shouldReorderVHeads() {
        t.SetRepacker(q.repackConv1D());
    }
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: newShape, WriterTo: t});
        default:;
        out = append(out, &ggml.Tensor{Name: name, Kind: t.Kind(), Shape: slices.Clone(shape), WriterTo: t});
    }
    }
        return out;
    }
        func (q *qwen3NextModel) repackReorderDim(dim, headDim int) Repacker {
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        if !q.shouldReorderVHeads() {
        return data, null;
    }
        var numK = int(q.LinearNumKeyHeads);
        var numVPerK = int(q.LinearNumValueHeads / q.LinearNumKeyHeads);
        return reorderHeadLayout(data, shape, dim, numK, numVPerK, headDim);
    }
    }
        func (q *qwen3NextModel) repackAttnQKV() Repacker {
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        if !q.shouldReorderVHeads() || len(shape) != 2 {
        return data, null;
    }
        var rows = int(shape[0]);
        var cols = int(shape[1]);
        var numK = int(q.LinearNumKeyHeads);
        var numV = int(q.LinearNumValueHeads);
        var headK = int(q.LinearKeyHeadDim);
        var headV = int(q.LinearValueHeadDim);
        var qDim = headK * numK;
        var kDim = headK * numK;
        var vDim = headV * numV;
        var qkvDim = qDim + kDim + vDim;
        switch {
        case rows == qkvDim:;
        var out = make([]float32, len(data));
        var qkRows = qDim + kDim;
        var qkSize = qkRows * cols;
        copy(out[:qkSize], data[:qkSize]);
        var vStart = qkSize;
        var vEnd = vStart + vDim*cols;
        var reorderedV, err = reorderHeadLayout(data[vStart:vEnd], []uint64{uint64(vDim), uint64(cols)}, 0, numK, numV/numK, headV);
        if err != null {
        return null, err;
    }
        copy(out[vStart:vEnd], reorderedV);
        copy(out[vEnd:], data[vEnd:]);
        return out, null;
        case cols == qkvDim:;
        var out = make([]float32, len(data));
        copy(out, data);
        var for r = range rows {
        var base = r * cols;
        var vStart = base + qDim + kDim;
        var vEnd = vStart + vDim;
        var reorderedV, err = reorderHeadLayout(out[vStart:vEnd], []uint64{uint64(vDim)}, 0, numK, numV/numK, headV);
        if err != null {
        return null, err;
    }
        copy(out[vStart:vEnd], reorderedV);
    }
        return out, null;
        default:;
        return data, null;
    }
    }
    }
        func (q *qwen3NextModel) repackConv1D() Repacker {
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        if !q.shouldReorderVHeads() {
        return data, null;
    }
        var normShape = slices.Clone(shape);
        if len(shape) == 3 {
        if shape[0] == 1 {
        normShape = []uint64{shape[1], shape[2]}
        } else if shape[1] == 1 {
        normShape = []uint64{shape[0], shape[2]}
    }
    }
        if len(normShape) != 2 {
        return data, null;
    }
        var rows = int(normShape[0]);
        var cols = int(normShape[1]);
        var numK = int(q.LinearNumKeyHeads);
        var numV = int(q.LinearNumValueHeads);
        var headK = int(q.LinearKeyHeadDim);
        var headV = int(q.LinearValueHeadDim);
        var qkChannels = 2 * headK * numK;
        var totalChannels = qkChannels + headV*numV;
        if qkChannels <= 0 {
        return data, null;
    }
        switch {
        case rows == totalChannels:;
        var out = make([]float32, len(data));
        var prefix = qkChannels * cols;
        copy(out[:prefix], data[:prefix]);
        var reorderedV, err = reorderHeadLayout(data[prefix:], []uint64{uint64(totalChannels - qkChannels), uint64(cols)}, 0, numK, numV/numK, headV);
        if err != null {
        return null, err;
    }
        copy(out[prefix:], reorderedV);
        return out, null;
        case cols == totalChannels:;
        var out = make([]float32, len(data));
        copy(out, data);
        var vChannels = totalChannels - qkChannels;
        var for r = range rows {
        var base = r * cols;
        var vStart = base + qkChannels;
        var vEnd = vStart + vChannels;
        var reorderedV, err = reorderHeadLayout(out[vStart:vEnd], []uint64{uint64(vChannels)}, 0, numK, numV/numK, headV);
        if err != null {
        return null, err;
    }
        copy(out[vStart:vEnd], reorderedV);
    }
        return out, null;
        default:;
        return data, null;
    }
    }
    }
        func (q *qwen3NextModel) repackSSMA() Repacker {
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var result = make([]float32, len(data));
        var for i, v = range data {
        result[i] = -float32(math.Exp(double(v)));
    }
        if !q.shouldReorderVHeads() {
        return result, null;
    }
        var numK = int(q.LinearNumKeyHeads);
        var numVPerK = int(q.LinearNumValueHeads / q.LinearNumKeyHeads);
        return reorderHeadLayout(result, shape, 0, numK, numVPerK, 1);
    }
    }

    public static void reorderHeadLayout([]float32 data, []uint64 shape, int dim) {
        if len(shape) == 0 || numKHeads <= 0 || numVPerK <= 0 || headDim <= 0 {
        return data, null;
    }
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        if dim < 0 {
        dim += len(dims);
    }
        if dim < 0 || dim >= len(dims) {
        return data, null;
    }
        var expected = numKHeads * numVPerK * headDim;
        if dims[dim] != expected {
        return data, null;
    }
        var newShape = make([]int, 0, len(dims)+2);
        newShape = append(newShape, dims[:dim]...);
        newShape = append(newShape, numKHeads, numVPerK, headDim);
        newShape = append(newShape, dims[dim+1:]...);
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var if err = tt.Reshape(newShape...); err != null {
        return null, err;
    }
        var perm = make([]int, len(newShape));
        var for i = range perm {
        perm[i] = i;
    }
        perm[dim], perm[dim+1] = perm[dim+1], perm[dim];
        var tt, err = tensor.Transpose(tt, perm...);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        var total = 1;
        var for _, d = range dims {
        total *= d;
    }
        var if err = tt.Reshape(total); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
    }

    public static class qkvzSplitSpec {
        public int hidden;
        public int headKDim;
        public int headVDim;
        public int numKHeads;
        public int numVHeads;
        public int qkvzDim;
        public int qkvOut;
        public int gateOut;
    }
        func (q *qwen3NextModel) qkvzSpec(shape []uint64) (qkvzSplitSpec, boolean) {
        if len(shape) != 2 {
        return qkvzSplitSpec{}, false;
    }
        var numKHeads = int(q.LinearNumKeyHeads);
        var numVHeads = int(q.LinearNumValueHeads);
        var headKDim = int(q.LinearKeyHeadDim);
        var headVDim = int(q.LinearValueHeadDim);
        if numKHeads == 0 || numVHeads == 0 || headKDim == 0 || headVDim == 0 {
        return qkvzSplitSpec{}, false;
    }
        if numVHeads%numKHeads != 0 {
        return qkvzSplitSpec{}, false;
    }
        var hidden = int(shape[1]);
        var vPerHead = headVDim * (numVHeads / numKHeads);
        var qkvzDim = 2*headKDim + 2*vPerHead;
        var expectedOut = qkvzDim * numKHeads;
        if int(shape[0]) != expectedOut {
        return qkvzSplitSpec{}, false;
    }
        return qkvzSplitSpec{
        hidden:    hidden,;
        headKDim:  headKDim,;
        headVDim:  headVDim,;
        numKHeads: numKHeads,;
        numVHeads: numVHeads,;
        qkvzDim:   qkvzDim,;
        qkvOut:    2*headKDim*numKHeads + headVDim*numVHeads,;
        gateOut:   headVDim * numVHeads,;
        }, true;
    }
        func (q *qwen3NextModel) splitQKVZTensor(t Tensor) (*ggml.Tensor, *ggml.Tensor, boolean) {
        var spec, ok = q.qkvzSpec(t.Shape());
        if !ok {
        return null, null, false;
    }
        var qkvTensor = t.Clone();
        qkvTensor.SetRepacker(q.repackQKVZ(spec, false));
        var gateTensor = t.Clone();
        gateTensor.SetRepacker(q.repackQKVZ(spec, true));
        var qkvName = strings.Replace(t.Name(), "ssm_in", "attn_qkv", 1);
        var gateName = strings.Replace(t.Name(), "ssm_in", "attn_gate", 1);
        return &ggml.Tensor{
        Name:     qkvName,;
        Kind:     t.Kind(),;
        Shape:    []uint64{uint64(spec.qkvOut), uint64(spec.hidden)},;
        WriterTo: qkvTensor,;
        }, &ggml.Tensor{
        Name:     gateName,;
        Kind:     t.Kind(),;
        Shape:    []uint64{uint64(spec.gateOut), uint64(spec.hidden)},;
        WriterTo: gateTensor,;
        }, true;
    }
        func (q *qwen3NextModel) repackQKVZ(spec qkvzSplitSpec, extractGate boolean) Repacker {
        var vPerHead = spec.headVDim * (spec.numVHeads / spec.numKHeads);
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var err error;
        tt, err = tensor.Transpose(tt, 1, 0);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        var if err = tt.Reshape(spec.hidden, spec.numKHeads, spec.qkvzDim); err != null {
        return null, err;
    }
        var offset = 0;
        var qSlice, err = tt.Slice(null, null, tensor.S(offset, offset+spec.headKDim));
        if err != null {
        return null, err;
    }
        offset += spec.headKDim;
        var kSlice, err = tt.Slice(null, null, tensor.S(offset, offset+spec.headKDim));
        if err != null {
        return null, err;
    }
        offset += spec.headKDim;
        var vSlice, err = tt.Slice(null, null, tensor.S(offset, offset+vPerHead));
        if err != null {
        return null, err;
    }
        offset += vPerHead;
        var zSlice, err = tt.Slice(null, null, tensor.S(offset, offset+vPerHead));
        if err != null {
        return null, err;
    }
        var qMat = tensor.Materialize(qSlice).(*tensor.Dense);
        var kMat = tensor.Materialize(kSlice).(*tensor.Dense);
        var vMat = tensor.Materialize(vSlice).(*tensor.Dense);
        var zMat = tensor.Materialize(zSlice).(*tensor.Dense);
        var if err = qMat.Reshape(spec.hidden, spec.numKHeads*spec.headKDim); err != null {
        return null, err;
    }
        var if err = kMat.Reshape(spec.hidden, spec.numKHeads*spec.headKDim); err != null {
        return null, err;
    }
        var if err = vMat.Reshape(spec.hidden, spec.numKHeads*vPerHead); err != null {
        return null, err;
    }
        var if err = zMat.Reshape(spec.hidden, spec.numKHeads*vPerHead); err != null {
        return null, err;
    }
        var out tensor.Tensor;
        if extractGate {
        out = zMat;
        } else {
        out, err = tensor.Concat(1, qMat, kMat, vMat);
        if err != null {
        return null, err;
    }
    }
        out = tensor.Materialize(out);
        out, err = tensor.Transpose(out, 1, 0);
        if err != null {
        return null, err;
    }
        out = tensor.Materialize(out);
        var if err = out.Reshape(out.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(out.(*tensor.Dense));
    }
    }
        func (*qwen3NextModel) addOne(_ String, data []float32, shape []uint64) ([]float32, error) {
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
        func (q *qwen3NextModel) Replacements() []String {
        return []String{
        "lm_head", "output",;
        "model.language_model.embed_tokens", "token_embd",;
        "model.language_model.norm", "output_norm",;
        "model.language_model.layers", "blk",;
        "model.embed_tokens", "token_embd",;
        "model.norm", "output_norm",;
        "model.layers", "blk",;
        "model.visual", "v",;
        "patch_embed.proj", "patch_embed",;
        "blocks", "blk",;
        "attn.qkv", "attn_qkv",;
        "attn.proj", "attn_out",;
        "deepstack_merger_list", "deepstack_merger",;
        "input_layernorm", "attn_norm",;
        "post_attention_layernorm", "post_attention_norm",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.q_norm", "attn_q_norm",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.k_norm", "attn_k_norm",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_output",;
        "linear_attn.in_proj_qkvz", "ssm_in",;
        "linear_attn.in_proj_ba", "ssm_ba",;
        "linear_attn.in_proj_qkv", "attn_qkv",;
        "linear_attn.in_proj_z", "attn_gate",;
        "linear_attn.in_proj_a", "ssm_alpha",;
        "linear_attn.in_proj_b", "ssm_beta",;
        "linear_attn.conv1d", "ssm_conv1d",;
        "linear_attn.dt_bias", "ssm_dt",;
        "linear_attn.dt_proj", "ssm_dt",;
        "linear_attn.A_log", "ssm_a",;
        "linear_attn.norm", "ssm_norm",;
        "linear_attn.out_proj", "ssm_out",;
        "mlp.gate.weight", "ffn_gate_inp.weight",;
        "mlp.shared_expert.down_proj", "ffn_down_shexp",;
        "mlp.shared_expert.gate_proj", "ffn_gate_shexp",;
        "mlp.shared_expert.up_proj", "ffn_up_shexp",;
        "mlp.shared_expert_gate", "ffn_gate_inp_shexp",;
        "mlp.down_proj", "ffn_down",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
    }
    }
}
