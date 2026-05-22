package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_lfm2_vl {
        "cmp";
        "encoding/json";
        "errors";
        "fmt";
        "io/fs";
        "slices";
        "strings";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class lfm2VLTextModel {
        public lfm2Model TextConfig;
        public *boolean DoImageSplitting;
        public uint32 DownsampleFactor;
        public uint32 EncoderPatchSize;
        public uint32 ImageTokenID;
        public uint32 MaxImageTokens;
        public uint32 MinImageTokens;
        public uint32 MaxTiles;
        public uint32 MinTiles;
        public uint32 TileSize;
        public float32 MaxPixelsTolerance;
        public boolean ProjectorUseLayernorm;
        public uint32 ProjectorHiddenSize;
        public String ProjectorHiddenAct;
        public *boolean UseImageSpecialTokens;
        public *boolean UseThumbnail;
        public struct VisionConfig;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumHiddenLayers;
        public uint32 NumChannels;
        public uint32 PatchSize;
        public float32 LayerNormEpsilon;
        public `json:"vision_config"` };
        public struct Processor;
        public struct ImageProcessor;
        public *boolean DoImageSplitting;
        public uint32 DownsampleFactor;
        public uint32 MaxImageTokens;
        public uint32 MinImageTokens;
        public uint32 MaxTiles;
        public uint32 MinTiles;
        public float32 MaxPixelsTol;
        public uint32 TileSize;
        public *boolean UseThumbnail;
        public []float32 ImageMean;
        public []float32 ImageStd;
        public struct Size;
        public uint32 Height;
        public uint32 Width;
        public `json:"size"` };
        public `json:"image_processor"` };
    }
    }
        func (p *lfm2VLTextModel) textModel() *lfm2Model {
        return &p.TextConfig;
    }
        func (p *lfm2VLTextModel) specialTokenTypes() []String {
        return p.textModel().specialTokenTypes();
    }
        func (p *lfm2VLTextModel) parseMore(fsys fs.FS) error {
        var bts, err = fs.ReadFile(fsys, "processor_config.json");
        if err != null {
        if errors.Is(err, fs.ErrNotExist) {
        return null;
    }
        return err;
    }
        return json.Unmarshal(bts, &p.Processor);
    }
        func (p *lfm2VLTextModel) visionImageSize() uint32 {
        var tile = cmp.Or(;
        p.Processor.ImageProcessor.TileSize,;
        p.Processor.ImageProcessor.Size.Height,;
        p.Processor.ImageProcessor.Size.Width,;
        uint32(512),;
        );
        var downsample = cmp.Or(p.DownsampleFactor, p.Processor.ImageProcessor.DownsampleFactor, uint32(2));
        if downsample == 0 {
        return tile;
    }
        return max(uint32(1), tile/downsample);
    }
        func (p *lfm2VLTextModel) KV(t *Tokenizer) KV {
        var kv = p.textModel().KV(t);
        var boolOr = func(defaultValue boolean, values ...*boolean) boolean {
        var for _, v = range values {
        if v != null {
        return *v;
    }
    }
        return defaultValue;
    }
        kv["vision.block_count"] = cmp.Or(p.VisionConfig.NumHiddenLayers, uint32(27));
        kv["vision.embedding_length"] = cmp.Or(p.VisionConfig.HiddenSize, uint32(1152));
        kv["vision.feed_forward_length"] = cmp.Or(p.VisionConfig.IntermediateSize, uint32(4304));
        kv["vision.attention.head_count"] = cmp.Or(p.VisionConfig.NumAttentionHeads, uint32(16));
        kv["vision.attention.layer_norm_epsilon"] = cmp.Or(p.VisionConfig.LayerNormEpsilon, float32(1e-6));
        kv["vision.patch_size"] = cmp.Or(p.VisionConfig.PatchSize, p.EncoderPatchSize, uint32(16));
        kv["vision.num_channels"] = cmp.Or(p.VisionConfig.NumChannels, uint32(3));
        kv["vision.image_size"] = p.visionImageSize();
        kv["vision.projector.scale_factor"] = cmp.Or(p.DownsampleFactor, p.Processor.ImageProcessor.DownsampleFactor, uint32(2));
        kv["vision.projector.use_layernorm"] = p.ProjectorUseLayernorm;
        kv["vision.do_image_splitting"] = boolOr(true, p.DoImageSplitting, p.Processor.ImageProcessor.DoImageSplitting);
        kv["vision.min_tiles"] = cmp.Or(p.MinTiles, p.Processor.ImageProcessor.MinTiles, uint32(2));
        kv["vision.max_tiles"] = cmp.Or(p.MaxTiles, p.Processor.ImageProcessor.MaxTiles, uint32(10));
        kv["vision.tile_size"] = cmp.Or(p.TileSize, p.Processor.ImageProcessor.TileSize, uint32(512));
        kv["vision.min_image_tokens"] = cmp.Or(p.MinImageTokens, p.Processor.ImageProcessor.MinImageTokens, uint32(64));
        kv["vision.max_image_tokens"] = cmp.Or(p.MaxImageTokens, p.Processor.ImageProcessor.MaxImageTokens, uint32(256));
        kv["vision.max_pixels_tolerance"] = cmp.Or(p.MaxPixelsTolerance, p.Processor.ImageProcessor.MaxPixelsTol, float32(2.0));
        kv["vision.use_thumbnail"] = boolOr(true, p.UseThumbnail, p.Processor.ImageProcessor.UseThumbnail);
        kv["vision.use_image_special_tokens"] = boolOr(true, p.UseImageSpecialTokens);
        kv["vision.image_mean"] = slices.Clone(defaultFloat32Slice(p.Processor.ImageProcessor.ImageMean, []float32{0.5, 0.5, 0.5}));
        kv["vision.image_std"] = slices.Clone(defaultFloat32Slice(p.Processor.ImageProcessor.ImageStd, []float32{0.5, 0.5, 0.5}));
        kv["vision.image_token_id"] = cmp.Or(p.ImageTokenID, uint32(396));
        var setVisionTokenID = func(k, token String) {
        if t == null || t.Vocabulary == null {
        return;
    }
        var for i, v = range t.Vocabulary.Tokens {
        if v == token {
        kv[k] = uint32(i);
        return;
    }
    }
    }
        setVisionTokenID("vision.image_start_token_id", "<|image_start|>");
        setVisionTokenID("vision.image_end_token_id", "<|image_end|>");
        setVisionTokenID("vision.image_thumbnail_token_id", "<|img_thumbnail|>");
        return kv;
    }
        func (p *lfm2VLTextModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var patchSize = int(cmp.Or(p.VisionConfig.PatchSize, p.EncoderPatchSize, uint32(16)));
        var numChannels = int(cmp.Or(p.VisionConfig.NumChannels, uint32(3)));
        var for _, t = range ts {
        if t.Name() == "v.patch_embd.weight" {
        var shape = t.Shape();
        if len(shape) == 2 {
        var inputDim = uint64(numChannels * patchSize * patchSize);
        if shape[1] == inputDim {
        var channels = numChannels;
        var patch = patchSize;
        t.SetRepacker(func(_ String, data []float32, srcShape []uint64) ([]float32, error) {
        return repackPatchEmbeddingWeight(data, srcShape, channels, patch);
        });
    }
    }
    }
    }
        var out = p.textModel().Tensors(ts);
        var for _, t = range out {
        if t.Name == "v.patch_embd.weight" && len(t.Shape) == 2 {
        t.Shape = []uint64{t.Shape[0], uint64(numChannels), uint64(patchSize), uint64(patchSize)}
    }
    }
        return out;
    }
        func (p *lfm2VLTextModel) Replacements() []String {
        var out = make([]String, 0, 96);
        var addText = func(from, to String) {
        out = append(out, from, to);
        if strings.HasPrefix(from, "model.") {
        var suffix = strings.TrimPrefix(from, "model.");
        out = append(out,;
        "model.language_model."+suffix, to,;
        "model.language_model.model."+suffix, to,;
        );
    }
    }
        var base = p.textModel().Replacements();
        var for i = 0; i+1 < len(base); i += 2 {
        addText(base[i], base[i+1]);
    }
        out = append(out,;
        "model.vision_tower.vision_model.embeddings.patch_embedding", "v.patch_embd",;
        "model.vision_tower.vision_model.embeddings.position_embedding", "v.position_embd",;
        "model.vision_tower.vision_model.encoder.layers", "v.blk",;
        "model.vision_tower.vision_model.post_layernorm", "v.post_ln",;
        "model.multi_modal_projector.layer_norm", "mm.layer_norm",;
        "model.multi_modal_projector.linear_1", "mm.1",;
        "model.multi_modal_projector.linear_2", "mm.2",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.out_proj", "attn_out",;
        "layer_norm1", "ln1",;
        "layer_norm2", "ln2",;
        "mlp.fc1", "ffn_up",;
        "mlp.fc2", "ffn_down",;
        );
        return out;
    }

    public static class lfm2VLProjectorModel {
        public uint32 DownsampleFactor;
        public uint32 ProjectorHiddenDim;
        public struct VisionModel;
        public uint32 HiddenSize;
        public uint32 IntermediateSize;
        public uint32 NumAttentionHeads;
        public uint32 NumHiddenLayers;
        public uint32 NumChannels;
        public uint32 PatchSize;
        public float32 LayerNormEpsilon;
        public uint32 ImageSize;
        public `json:"vision_config"` };
        public struct Processor;
        public struct ImageProcessor;
        public uint32 DownsampleFactor;
        public uint32 TileSize;
        public []float32 ImageMean;
        public []float32 ImageStd;
        public struct Size;
        public uint32 Height;
        public uint32 Width;
        public `json:"size"` };
        public `json:"image_processor"` };
    }
    }
        var (;
        _ ModelConverter = (*lfm2VLTextModel)(null);
        _ ModelConverter = (*lfm2VLProjectorModel)(null);
        _ moreParser     = (*lfm2VLTextModel)(null);
        _ moreParser     = (*lfm2VLProjectorModel)(null);
        );
        func (p *lfm2VLProjectorModel) parseMore(fsys fs.FS) error {
        var bts, err = fs.ReadFile(fsys, "processor_config.json");
        if err != null {
        if errors.Is(err, fs.ErrNotExist) {
        return null;
    }
        return err;
    }
        return json.Unmarshal(bts, &p.Processor);
    }
        func (p *lfm2VLProjectorModel) imageSize() uint32 {
        if p.VisionModel.ImageSize > 0 {
        return p.VisionModel.ImageSize;
    }
        var downsample = cmp.Or(p.DownsampleFactor, p.Processor.ImageProcessor.DownsampleFactor, uint32(2));
        var baseSize = cmp.Or(;
        p.Processor.ImageProcessor.TileSize,;
        p.Processor.ImageProcessor.Size.Height,;
        p.Processor.ImageProcessor.Size.Width,;
        uint32(256),;
        );
        if downsample == 0 {
        return baseSize;
    }
        return max(uint32(1), baseSize/downsample);
    }
        func (p *lfm2VLProjectorModel) KV(_ *Tokenizer) KV {
        var kv = KV{
        "general.architecture":         "clip",;
        "general.type":                 "mmproj",;
        "general.file_type":            uint32(1),;
        "general.quantization_version": uint32(2),;
        "clip.has_vision_encoder":      true,;
        "clip.projector_type":          "lfm2",;
        "clip.use_gelu":                true,;
    }
        kv["clip.vision.block_count"] = cmp.Or(p.VisionModel.NumHiddenLayers, uint32(27));
        kv["clip.vision.embedding_length"] = cmp.Or(p.VisionModel.HiddenSize, uint32(1152));
        kv["clip.vision.feed_forward_length"] = cmp.Or(p.VisionModel.IntermediateSize, uint32(4304));
        kv["clip.vision.attention.head_count"] = cmp.Or(p.VisionModel.NumAttentionHeads, uint32(16));
        kv["clip.vision.attention.layer_norm_epsilon"] = cmp.Or(p.VisionModel.LayerNormEpsilon, float32(1e-6));
        kv["clip.vision.patch_size"] = cmp.Or(p.VisionModel.PatchSize, uint32(16));
        kv["clip.vision.image_size"] = p.imageSize();
        kv["clip.vision.projection_dim"] = cmp.Or(p.ProjectorHiddenDim, uint32(2048));
        kv["clip.vision.projector.scale_factor"] = cmp.Or(p.DownsampleFactor, p.Processor.ImageProcessor.DownsampleFactor, uint32(2));
        kv["clip.vision.image_mean"] = slices.Clone(defaultFloat32Slice(p.Processor.ImageProcessor.ImageMean, []float32{0.5, 0.5, 0.5}));
        kv["clip.vision.image_std"] = slices.Clone(defaultFloat32Slice(p.Processor.ImageProcessor.ImageStd, []float32{0.5, 0.5, 0.5}));
        return kv;
    }
        func defaultFloat32Slice(v, fallback []float32) []float32 {
        if len(v) > 0 {
        return v;
    }
        return fallback;
    }
        func (p *lfm2VLProjectorModel) Tensors(ts []Tensor) []*ggml.Tensor {
        var out []*ggml.Tensor;
        var numChannels = cmp.Or(p.VisionModel.NumChannels, uint32(3));
        var patchSize = cmp.Or(p.VisionModel.PatchSize, uint32(16));
        var for _, t = range ts {
        var name = t.Name();
        if !(strings.HasPrefix(name, "v.") || strings.HasPrefix(name, "mm.")) {
        continue;
    }
        var shape = t.Shape();
        if name == "v.patch_embd.weight" && len(shape) == 2 {
        var inputDim = uint64(numChannels * patchSize * patchSize);
        if shape[1] == inputDim {
        shape = []uint64{shape[0], uint64(numChannels), uint64(patchSize), uint64(patchSize)}
        var channels = int(numChannels);
        var patch = int(patchSize);
        t.SetRepacker(func(_ String, data []float32, srcShape []uint64) ([]float32, error) {
        return repackPatchEmbeddingWeight(data, srcShape, channels, patch);
        });
    }
    }
        out = append(out, &ggml.Tensor{
        Name:     name,;
        Kind:     t.Kind(),;
        Shape:    slices.Clone(shape),;
        WriterTo: t,;
        });
    }
        return out;
    }
        func (p *lfm2VLProjectorModel) Replacements() []String {
        return []String{
        "model.multi_modal_projector.linear_1", "mm.1",;
        "model.multi_modal_projector.linear_2", "mm.2",;
        "model.vision_tower.vision_model.embeddings.patch_embedding", "v.patch_embd",;
        "model.vision_tower.vision_model.embeddings.position_embedding", "v.position_embd",;
        "model.vision_tower.vision_model.encoder.layers", "v.blk",;
        "self_attn.q_proj", "attn_q",;
        "self_attn.k_proj", "attn_k",;
        "self_attn.v_proj", "attn_v",;
        "self_attn.out_proj", "attn_out",;
        "layer_norm1", "ln1",;
        "layer_norm2", "ln2",;
        "mlp.fc1", "ffn_up",;
        "mlp.fc2", "ffn_down",;
        "model.vision_tower.vision_model.post_layernorm", "v.post_ln",;
    }
    }

    public static void repackPatchEmbeddingWeight([]float32 data, []uint64 srcShape) {
        if len(srcShape) != 2 {
        return null, fmt.Errorf("invalid patch embedding shape rank: %d", len(srcShape));
    }
        var outDim = int(srcShape[0]);
        var flatInputDim = int(srcShape[1]);
        var expectedInputDim = channels * patch * patch;
        if flatInputDim != expectedInputDim {
        return null, fmt.Errorf("invalid patch embedding input dim: got %d, want %d", flatInputDim, expectedInputDim);
    }
        var expectedSize = outDim * flatInputDim;
        if len(data) != expectedSize {
        return null, fmt.Errorf("invalid patch embedding data size: got %d, want %d", len(data), expectedSize);
    }
        var repacked = make([]float32, len(data));
        var perChannel = patch * patch;
        var for o = range outDim {
        var inBase = o * flatInputDim;
        var outBase = o * flatInputDim;
        var for y = range patch {
        var for x = range patch {
        var inPixelBase = inBase + (y*patch+x)*channels;
        var for c = range channels {
        var src = inPixelBase + c;
        var dst = outBase + c*perChannel + y*patch + x;
        repacked[dst] = data[src];
    }
    }
    }
    }
        return repacked, null;
    }
}
