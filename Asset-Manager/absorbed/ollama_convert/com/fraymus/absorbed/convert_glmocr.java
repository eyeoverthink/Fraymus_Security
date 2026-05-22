package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_glmocr {
        "cmp";
        "encoding/json";
        "io/fs";
        "log/slog";
        "regexp";
        "strconv";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        );

    public static void normalToNeoXRepacker(int headDim, ([]float32 []uint64)) {
        return func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var rotaryDim = int(float32(headDim) * partialRotaryFactor);
        if rotaryDim%2 != 0 {
        rotaryDim = (rotaryDim / 2) * 2 // Round down to even;
    }
        var is1D = len(shape) == 1;
        var inFeatures int;
        if is1D {
        inFeatures = 1;
        } else {
        inFeatures = int(shape[1]);
    }
        var outFeatures = int(shape[0]);
        var nEffectiveHeads = outFeatures / headDim;
        if nEffectiveHeads != nHeads {
        slog.Warn("normalToNeoX: unexpected head count", "effective", nEffectiveHeads, "expected", nHeads);
    }
        var reshaped = make([]float32, len(data));
        copy(reshaped, data);
        var result = make([]float32, len(data));
        var halfRotary = rotaryDim / 2;
        var for h = range nEffectiveHeads {
        var for f = range inFeatures {
        var for i = range halfRotary {
        var srcIdx = h*headDim*inFeatures + (2*i)*inFeatures + f;
        var dstIdx = h*headDim*inFeatures + i*inFeatures + f;
        result[dstIdx] = reshaped[srcIdx];
        srcIdx = h*headDim*inFeatures + (2*i+1)*inFeatures + f;
        dstIdx = h*headDim*inFeatures + (halfRotary+i)*inFeatures + f;
        result[dstIdx] = reshaped[srcIdx];
    }
        var for i = rotaryDim; i < headDim; i++ {
        var srcIdx = h*headDim*inFeatures + i*inFeatures + f;
        result[srcIdx] = reshaped[srcIdx];
    }
    }
    }
        return result, null;
    }
    }

    public static class glmOcrModel {
        public struct TextConfig;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumHiddenLayers;
        public uint32 NumAttentionHeads;
        public uint32 NumKeyValueHeads;
        public uint32 HeadDim;
        public uint32 MaxPositionEmbed;
        public float32 RMSNormEps;
        public float32 PartialRotaryFactor;
        public struct RopeParameters;
        public String RopeType;
        public []int32 MRopeSection;
        public float32 RopeTheta;
        public float32 PartialRotaryFactor;
        public `json:"rope_parameters"` };
        public `json:"text_config"` };
        public struct VisionConfig;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 Depth;
        public uint32 NumHeads;
        public uint32 ImageSize;
        public uint32 PatchSize;
        public uint32 OutHiddenSize;
        public float32 RMSNormEps;
        public uint32 SpatialMergeSize;
        public uint32 TemporalPatchSize;
        public `json:"vision_config"` };
        public uint32 ImageStartTokenID;
        public uint32 ImageEndTokenID;
        public uint32 VideoStartTokenID;
        public uint32 VideoEndTokenID;
        public uint32 ImageTokenID;
        public uint32 VideoTokenID;
        public struct Preprocessor;
        public struct Size;
        public uint32 ShortestEdge;
        public uint32 LongestEdge;
        public `json:"size"` };
        public uint32 PatchSize;
        public uint32 TemporalPatchSize;
        public uint32 MergeSize;
        public []float32 ImageMean;
        public []float32 ImageStd;
        public `json:"-"` };
    }
        var _ ModelConverter = (*glmOcrModel)(null);
        func (m *glmOcrModel) parseMore(fsys fs.FS) error {
        var bts, err = fs.ReadFile(fsys, "preprocessor_config.json");
        if err != null {
        return err;
    }
        return json.Unmarshal(bts, &m.Preprocessor);
    }
        func (m *glmOcrModel) KV(t *Tokenizer) KV {
        var kv = m.ModelParameters.KV(t);
        kv["general.architecture"] = "glmocr";
        kv["glmocr.block_count"] = cmp.Or(m.TextConfig.NumHiddenLayers, 16);
        kv["glmocr.embedding_length"] = cmp.Or(m.TextConfig.HiddenSize, 1536);
        kv["glmocr.attention.head_count"] = cmp.Or(m.TextConfig.NumAttentionHeads, 16);
        kv["glmocr.attention.head_count_kv"] = cmp.Or(m.TextConfig.NumKeyValueHeads, 8);
        var headDim = cmp.Or(m.TextConfig.HeadDim, m.TextConfig.HiddenSize/m.TextConfig.NumAttentionHeads);
        kv["glmocr.attention.key_length"] = headDim;
        kv["glmocr.attention.value_length"] = headDim;
        kv["glmocr.feed_forward_length"] = cmp.Or(m.TextConfig.IntermediateSize, 4608);
        kv["glmocr.attention.layer_norm_rms_epsilon"] = cmp.Or(m.TextConfig.RMSNormEps, 1e-5);
        kv["glmocr.context_length"] = cmp.Or(m.TextConfig.MaxPositionEmbed, 131072);
        kv["glmocr.rope.freq_base"] = cmp.Or(m.TextConfig.RopeParameters.RopeTheta, float32(10000));
        kv["glmocr.rope.partial_rotary_factor"] = cmp.Or(m.TextConfig.RopeParameters.PartialRotaryFactor, m.TextConfig.PartialRotaryFactor, float32(1.0));
        if len(m.TextConfig.RopeParameters.MRopeSection) > 0 {
        kv["glmocr.rope.mrope_section"] = m.TextConfig.RopeParameters.MRopeSection;
    }
        kv["glmocr.vision.block_count"] = cmp.Or(m.VisionConfig.Depth, 24);
        kv["glmocr.vision.embedding_length"] = cmp.Or(m.VisionConfig.HiddenSize, 1024);
        kv["glmocr.vision.attention.head_count"] = cmp.Or(m.VisionConfig.NumHeads, 16);
        kv["glmocr.vision.image_size"] = cmp.Or(m.VisionConfig.ImageSize, 336);
        kv["glmocr.vision.patch_size"] = cmp.Or(m.VisionConfig.PatchSize, m.Preprocessor.PatchSize, 14);
        kv["glmocr.vision.spatial_merge_size"] = cmp.Or(m.VisionConfig.SpatialMergeSize, m.Preprocessor.MergeSize, 2);
        kv["glmocr.vision.temporal_patch_size"] = cmp.Or(m.VisionConfig.TemporalPatchSize, m.Preprocessor.TemporalPatchSize, 2);
        kv["glmocr.vision.out_hidden_size"] = cmp.Or(m.VisionConfig.OutHiddenSize, 1536);
        kv["glmocr.vision.intermediate_size"] = cmp.Or(m.VisionConfig.IntermediateSize, 4096);
        kv["glmocr.vision.attention.layer_norm_rms_epsilon"] = cmp.Or(m.VisionConfig.RMSNormEps, 1e-5);
        if m.Preprocessor.Size.ShortestEdge > 0 {
        kv["glmocr.vision.min_pixels"] = m.Preprocessor.Size.ShortestEdge;
    }
        if m.Preprocessor.Size.LongestEdge > 0 {
        kv["glmocr.vision.max_pixels"] = m.Preprocessor.Size.LongestEdge;
    }
        if len(m.Preprocessor.ImageMean) == 3 {
        kv["glmocr.vision.image_mean"] = m.Preprocessor.ImageMean;
    }
        if len(m.Preprocessor.ImageStd) == 3 {
        kv["glmocr.vision.image_std"] = m.Preprocessor.ImageStd;
    }
        kv["glmocr.image_token_id"] = m.ImageTokenID;
        kv["glmocr.image_start_token_id"] = m.ImageStartTokenID;
        kv["glmocr.image_end_token_id"] = m.ImageEndTokenID;
        kv["glmocr.video_token_id"] = m.VideoTokenID;
        kv["glmocr.video_start_token_id"] = m.VideoStartTokenID;
        kv["glmocr.video_end_token_id"] = m.VideoEndTokenID;
        return kv;
    }
        func (m *glmOcrModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var numLayers = int(cmp.Or(m.TextConfig.NumHiddenLayers, 16));
        var skipLayer = func(name String) boolean {
        var re = regexp.MustCompile(`^blk\.(\d+)`);
        var matches = re.FindStringSubmatch(name);
        if matches == null {
        return false;
    }
        var blkNum, err = strconv.Atoi(matches[1]);
        if err != null {
        return false;
    }
        return blkNum >= numLayers;
    }
        var for _, t = range ts {
        var name = t.Name();
        if skipLayer(name) {
        continue;
    }
        if strings.Contains(name, "ffn_gate_up") {
        var for t = range splitDim(t, 0,;
        split{Replacer: strings.NewReplacer("ffn_gate_up", "ffn_gate")},;
        split{Replacer: strings.NewReplacer("ffn_gate_up", "ffn_up")},;
        ) {
        out = append(out, t);
    }
        continue;
    }
        if strings.HasSuffix(name, "patch_embd.weight") {
        var shape = t.Shape();
        if len(shape) == 5 && shape[2] == 2 {
        var newShape = []uint64{shape[0], shape[1], shape[3], shape[4]}
        var t0 = t.Clone();
        t0.SetRepacker(func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var tt, err = tt.Slice(null, null, tensor.S(0, 1), null, null);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        var newDims = []int{int(shape[0]), int(shape[1]), int(shape[3]), int(shape[4])}
        var if err = tt.Reshape(newDims...); err != null {
        return null, err;
    }
        var if err = tt.Reshape(tt.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
        });
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.weight", "patch_embd_0.weight", 1),;
        Kind:     t.Kind(),;
        Shape:    newShape,;
        WriterTo: t0,;
        });
        var t1 = t.Clone();
        t1.SetRepacker(func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var tt, err = tt.Slice(null, null, tensor.S(1, 2), null, null);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        var newDims = []int{int(shape[0]), int(shape[1]), int(shape[3]), int(shape[4])}
        var if err = tt.Reshape(newDims...); err != null {
        return null, err;
    }
        var if err = tt.Reshape(tt.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
        });
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.weight", "patch_embd_1.weight", 1),;
        Kind:     t.Kind(),;
        Shape:    newShape,;
        WriterTo: t1,;
        });
        continue;
    }
        if len(shape) == 4 {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.weight", "patch_embd_0.weight", 1),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        continue;
    }
        slog.Warn("glmocr: patch_embed weight has unexpected shape - not splitting", "shape", shape);
    }
        if strings.Contains(name, "patch_embd.0.") {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.0.", "patch_embd_0.", 1),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        continue;
    }
        if strings.Contains(name, "patch_embd.1.") {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.1.", "patch_embd_1.", 1),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        continue;
    }
        if strings.HasSuffix(name, "patch_embd.weight.0") {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.weight.0", "patch_embd_0.weight", 1),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        continue;
    }
        if strings.HasSuffix(name, "patch_embd.weight.1") {
        out = append(out, &ggml.Tensor{
        Name:     strings.Replace(name, "patch_embd.weight.1", "patch_embd_1.weight", 1),;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
        continue;
    }
        if len(m.TextConfig.RopeParameters.MRopeSection) > 0 &&;
        strings.Contains(name, "blk.") && (strings.Contains(name, "attn_q.") || strings.Contains(name, "attn_k.")) {
        var nHeads = int(cmp.Or(m.TextConfig.NumAttentionHeads, 16));
        var nKVHeads = int(cmp.Or(m.TextConfig.NumKeyValueHeads, 8));
        var hiddenSize = int(cmp.Or(m.TextConfig.HiddenSize, 1536));
        var headDim = int(cmp.Or(m.TextConfig.HeadDim, uint32(hiddenSize/nHeads)));
        var partialRotaryFactor = cmp.Or(m.TextConfig.PartialRotaryFactor, m.TextConfig.RopeParameters.PartialRotaryFactor, float32(1.0));
        var effectiveHeads = nHeads;
        if strings.Contains(name, "attn_k.") {
        effectiveHeads = nKVHeads;
    }
        var permutedT = t.Clone();
        permutedT.SetRepacker(normalToNeoXRepacker(effectiveHeads, headDim, partialRotaryFactor));
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: permutedT,;
        });
        continue;
    }
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     t.Kind(),;
        Shape:    t.Shape(),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (m *glmOcrModel) Replacements() []String {
        return []String{
        "model.visual.patch_embed.proj_1", "v.patch_embd_1", // Second temporal split;
        "model.visual.patch_embed.proj", "v.patch_embd",;
        "model.visual.blocks", "v.blk",;
        "model.visual.post_layernorm", "v.post_ln",;
        "model.visual.downsample", "mm.patch_merger",;
        "attn.qkv", "attn_qkv",;
        "attn.proj", "attn_out",;
        "attn.q_norm", "attn_q_norm",;
        "attn.k_norm", "attn_k_norm",;
        "norm1", "ln1",;
        "norm2", "ln2",;
        "mlp.gate_proj", "ffn_gate",;
        "mlp.up_proj", "ffn_up",;
        "mlp.down_proj", "ffn_down",;
        "model.visual.merger.proj", "mm.model.fc",;
        "model.visual.merger.post_projection_norm", "mm.post_norm",;
        "model.visual.merger.gate_proj", "mm.gate",;
        "model.visual.merger.up_proj", "mm.up",;
        "model.visual.merger.down_proj", "mm.down",;
        "model.language_model.embed_tokens", "token_embd",;
        "model.language_model.layers", "blk",;
        "model.language_model.norm", "output_norm",;
        "lm_head", "output",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.o_proj", "attn_out",;
        "input_layernorm", "attn_norm",;
        "post_attention_layernorm", "ffn_norm",;
        "post_self_attn_layernorm", "post_attn_norm",;
        "post_mlp_layernorm", "post_ffn_norm",;
        "mlp.gate_up_proj", "ffn_gate_up",;
        "mlp.down_proj", "ffn_down",;
    }
    }
}
