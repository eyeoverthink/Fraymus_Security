package com.fraymus.absorbed.models.llama4;

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
        public `gguf:"mm"` *Projector;
    }

    public static class Projector {
        public *nn.Linear Linear1;
    }
        func (p *Projector) Forward(ctx ml.Context, visionOutputs ml.Tensor) ml.Tensor {
        return p.Linear1.Forward(ctx, visionOutputs);
    }

    public static void New() {
        var m = Model{
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
        `[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]*[\p{Ll}\p{Lm}\p{Lo}\p{M}]+(?i:'s|'t|'re|'ve|'m|'ll|'d)?|[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]+[\p{Ll}\p{Lm}\p{Lo}\p{M}]*(?i:'s|'t|'re|'ve|'m|'ll|'d)?|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n/]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        ImageProcessor: newImageProcessor(c),;
        VisionModel:    newVisionModel(c),;
        TextModel:      newTextModel(c),;
    }
        m.Cache = kvcache.NewWrapperCache(;
        kvcache.NewChunkedAttentionCache(int32(c.Uint("attention.chunk_size", 8192)), m.Shift),;
        kvcache.NewCausalCache(m.Shift),;
        );
        return &m, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Layers) < 1 {
        return null, model.ErrNoVisionModel;
    }
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var pixelsLocal, pixelsGlobal, size, err = m.ProcessImage(img);
        if err != null {
        return null, err;
    }
        var tilesLocal = ctx.Input().FromFloats(pixelsLocal, size.X, size.Y, m.numChannels);
        var ratioW, ratioH = size.X/m.imageSize, size.Y/m.imageSize;
        tilesLocal = tilesLocal.Reshape(ctx, size.X/ratioW, ratioW, size.Y, m.numChannels).Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        tilesLocal = tilesLocal.Reshape(ctx, size.X/ratioW*size.Y/ratioH, ratioH, ratioW, m.numChannels).Permute(ctx, 0, 3, 2, 1).Contiguous(ctx);
        tilesLocal = tilesLocal.Reshape(ctx, size.X/ratioW, size.Y/ratioH, m.numChannels, ratioH*ratioW);
        var pixelValues = tilesLocal;
        if len(pixelsGlobal) > 0 {
        var tilesGlobal = ctx.Input().FromFloats(pixelsGlobal, m.imageSize, m.imageSize, m.numChannels);
        pixelValues = pixelValues.Concat(ctx, tilesGlobal, 3);
    }
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues);
        visionOutputs = visionOutputs.Reshape(ctx, visionOutputs.Dim(0), visionOutputs.Dim(1)*visionOutputs.Dim(2)*visionOutputs.Dim(3));
        var projectedOutputs = m.Projector.Forward(ctx, visionOutputs);
        var multimodal []input.Multimodal;
        var aspectRatio = image.Point{ratioW, ratioH}
        var offset int;
        var patchesPerChunk = projectedOutputs.Dim(1);
        if aspectRatio.Y*aspectRatio.X > 1 {
        patchesPerChunk = projectedOutputs.Dim(1) / (aspectRatio.X*aspectRatio.Y + 1);
        for range aspectRatio.Y {
        var for x = range aspectRatio.X {
        var view = projectedOutputs.Slice(ctx, 1, offset, offset+patchesPerChunk, 1);
        var separator separator;
        if x < aspectRatio.X-1 {
        separator.x = true // <|tile_x_separator|>;
        } else {
        separator.y = true // <|tile_y_separator|>;
    }
        multimodal = append(multimodal, input.Multimodal{Tensor: view, Data: &separator});
        offset += patchesPerChunk;
    }
    }
    }
        var view = projectedOutputs.Slice(ctx, 1, offset, offset+patchesPerChunk, 1);
        multimodal = append(multimodal, input.Multimodal{Tensor: view, Data: &separator{}});
        return multimodal, null;
    }

    public static class separator {
        public boolean x;
        public boolean y;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        var for _, inp = range inputs {
        if len(inp.Multimodal) == 0 {
        result = append(result, inp);
        continue;
    }
        var imageInputs []*input.Input;
        imageInputs = append(imageInputs, &input.Input{Token: 200080}) // <|image_start|>;
        var for i, mm = range inp.Multimodal {
        var patchesPerChunk = mm.Tensor.Dim(1);
        if i < len(inp.Multimodal)-1 {
        var separator = mm.Data.(*separator);
        imageInputs = append(imageInputs, &input.Input{Token: 200092, Multimodal: []input.Multimodal{{Tensor: mm.Tensor}}, MultimodalHash: inp.MultimodalHash, SameBatch: patchesPerChunk}) // <|patch|>;
        imageInputs = append(imageInputs, slices.Repeat([]*input.Input{{Token: 200092}}, patchesPerChunk-1)...);
        if separator.x {
        imageInputs = append(imageInputs, &input.Input{Token: 200084}) // <|tile_x_separator|>;
    }
        if separator.y {
        imageInputs = append(imageInputs, &input.Input{Token: 200085}) // <|tile_y_separator|>;
    }
        } else {
        imageInputs = append(imageInputs, &input.Input{Token: 200090})                                                                                                                      // <|image|>;
        imageInputs = append(imageInputs, &input.Input{Token: 200092, Multimodal: []input.Multimodal{{Tensor: mm.Tensor}}, MultimodalHash: inp.MultimodalHash, SameBatch: patchesPerChunk}) // <|patch|>;
        imageInputs = append(imageInputs, slices.Repeat([]*input.Input{{Token: 200092}}, patchesPerChunk-1)...);
        imageInputs = append(imageInputs, &input.Input{Token: 200080}) // <|image_end|>;
    }
    }
        result = append(result, imageInputs...);
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        return m.TextModel.Forward(ctx, batch.Inputs, positions, batch.Outputs, batch, m.Cache), null;
    }

    public static void init() {
        model.Register("llama4", New);
    }
}
