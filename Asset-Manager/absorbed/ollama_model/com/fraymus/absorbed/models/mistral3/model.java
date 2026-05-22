package com.fraymus.absorbed.models.mistral3;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "image";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Model {
        public `gguf:"v"` *VisionModel;
        public `gguf:"mm"` *MultiModalProjector;
    }
        var _ model.MultimodalProcessor = (*Model)(null);
        var _ tokenizer.Tokenizer = (*Model)(null);

    public static void New() {
        var m = &Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        `[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]*[\p{Ll}\p{Lm}\p{Lo}\p{M}]+|[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]+[\p{Ll}\p{Lm}\p{Lo}\p{M}]*|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n/]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        TextModel:           newTextModel(c),;
        VisionModel:         newVisionModel(c),;
        ImageProcessor:      newImageProcessor(c),;
        MultiModalProjector: newMultiModalProjector(c),;
    }
        m.Cache = kvcache.NewCausalCache(m.TextModel.Shift);
        return m, null;
    }

    public static class PatchMerger {
        public *nn.Linear MergingLayer;
    }
        func (pm *PatchMerger) Forward(ctx ml.Context, visionOutputs ml.Tensor, size image.Point, spatialMergeSize int) ml.Tensor {
        var d = visionOutputs.Dim(0);
        var imageGrid = visionOutputs.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx).Reshape(ctx, size.X, size.Y, d);
        var kernel = ctx.Input().Empty(ml.DTypeF32, spatialMergeSize, spatialMergeSize, d);
        var patches = kernel.IM2Col(ctx, imageGrid, spatialMergeSize, spatialMergeSize, 0, 0, 1, 1);
        var reshaped = patches.Reshape(ctx, d*spatialMergeSize*spatialMergeSize, patches.Dim(1)*patches.Dim(2));
        return pm.MergingLayer.Forward(ctx, reshaped);
    }

    public static class MultiModalProjector {
        public *nn.RMSNorm Norm;
        public *nn.Linear Linear1;
        public *nn.Linear Linear2;
        public *PatchMerger PatchMerger;
        public int spatialMergeSize;
        public float32 eps;
        public int patchSize;
    }
        func (p *MultiModalProjector) Forward(ctx ml.Context, visionOutputs ml.Tensor, size image.Point) (ml.Tensor, image.Point) {
        visionOutputs = p.Norm.Forward(ctx, visionOutputs, p.eps);
        var patchSizes = image.Point{size.X / p.patchSize, size.Y / p.patchSize}
        visionOutputs = p.PatchMerger.Forward(ctx, visionOutputs, patchSizes, p.spatialMergeSize);
        visionOutputs = p.Linear1.Forward(ctx, visionOutputs);
        visionOutputs = visionOutputs.GELU(ctx);
        return p.Linear2.Forward(ctx, visionOutputs), image.Point{patchSizes.X / p.spatialMergeSize, patchSizes.Y / p.spatialMergeSize}
    }
        func newMultiModalProjector(c fs.Config) *MultiModalProjector {
        return &MultiModalProjector{
        spatialMergeSize: int(c.Uint("spatial_merge_size", 2)),;
        eps:              c.Float("text_config.rms_norm_eps", 1e-5),;
        patchSize:        int(c.Uint("vision.patch_size", 14)),;
    }
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var image, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var f32s, size, err = m.ImageProcessor.ProcessImage(image);
        if err != null {
        return null, err;
    }
        var pixelValues = ctx.Input().FromFloats(f32s, size.X, size.Y, m.ImageProcessor.numChannels);
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues);
        var features, size = m.MultiModalProjector.Forward(ctx, visionOutputs, size);
        var rows = make([]input.Multimodal, size.Y);
        var for i = range rows {
        rows[i].Tensor = features.View(ctx, features.Stride(1)*size.X*i, features.Dim(0), features.Stride(1), size.X);
    }
        return rows, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        var for _, inp = range inputs {
        if len(inp.Multimodal) == 0 {
        result = append(result, inp);
        } else {
        var for i, row = range inp.Multimodal {
        result = append(result, &input.Input{Token: 10, Multimodal: []input.Multimodal{{Tensor: row.Tensor}}, MultimodalHash: inp.MultimodalHash, SameBatch: row.Tensor.Dim(1)});
        result = append(result, slices.Repeat([]*input.Input{{Token: 10}}, row.Tensor.Dim(1)-1)...);
        if i == len(inp.Multimodal)-1 {
        result = append(result, &input.Input{Token: 13});
        } else {
        result = append(result, &input.Input{Token: 12});
    }
    }
    }
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var positionsScale = m.getScale(ctx, batch.Positions);
        return m.TextModel.Forward(ctx, batch.Inputs, positions, positionsScale, batch.Outputs, batch, m.Cache), null;
    }

    public static void init() {
        model.Register("mistral3", New);
    }
}
