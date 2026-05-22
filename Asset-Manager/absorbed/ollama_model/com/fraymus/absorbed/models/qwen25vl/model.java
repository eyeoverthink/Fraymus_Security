package com.fraymus.absorbed.models.qwen25vl;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "image";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Model {
        public `gguf:"v"` *VisionModel;
    }
        var _ model.MultimodalProcessor = (*Model)(null);

    public static void New() {
        var m = &Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", false),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        TextModel:      NewTextModel(c),;
        VisionModel:    newVisionModel(c),;
        ImageProcessor: newImageProcessor(c),;
    }
        m.Cache = kvcache.NewCausalCache(m.TextModel.Shift);
        return m, null;
    }
        func (m *Model) PixelValues(ctx ml.Context, multimodalData []byte) (ml.Tensor, *Grid, error) {
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, null, err;
    }
        var f32s, grid, err = m.ImageProcessor.ProcessImage(img);
        if err != null {
        return null, null, err;
    }
        var patchDim = m.numChannels * m.temporalPatchSize * m.patchSize * m.patchSize;
        var numPatches = grid.Temporal * grid.Height * grid.Width;
        var pixelValues = ctx.Input().FromFloats(f32s, patchDim, numPatches);
        return pixelValues, grid, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var pixels, grid, err = m.PixelValues(ctx, multimodalData);
        if err != null {
        return null, err;
    }
        var visionOutputs = m.VisionModel.Forward(ctx, pixels, grid);
        return []input.Multimodal{{Tensor: visionOutputs, Data: grid}}, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        m.positionCache = m.positionCache[:0];
        var result []*input.Input;
        var (;
        imageToken       int32 = 151655;
        visionStartToken int32 = 151652;
        visionEndToken   int32 = 151653;
        );
        var appendInput = func(i *input.Input, p int) int {
        result = append(result, i);
        m.positionCache = append(m.positionCache, int32(p));
        return p + 1;
    }
        var p int;
        var for _, inp = range inputs {
        if inp.Multimodal == null {
        p = appendInput(inp, p);
        } else {
        p = appendInput(&input.Input{Token: visionStartToken}, p);
        var tokensPerGrid = inp.Multimodal[0].Tensor.Dim(1);
        appendInput(&input.Input{
        Token:          imageToken,;
        Multimodal:     inp.Multimodal,;
        MultimodalHash: inp.MultimodalHash,;
        SameBatch:      tokensPerGrid,;
        }, p);
        for range tokensPerGrid - 1 {
        appendInput(&input.Input{Token: imageToken}, p);
    }
        var grid = inp.Multimodal[0].Data.(*Grid);
        p = appendInput(&input.Input{Token: visionEndToken}, p+max(grid.Width/m.spatialMergeSize, grid.Height/m.spatialMergeSize));
    }
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs).Duplicate(ctx);
        var positionSlice = func() [][]int32 {
        var s = [][]int32{
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
    }
        var for i, position = range batch.Positions {
        if position < int32(len(m.positionCache)) {
        position = m.positionCache[position];
        } else if len(m.positionCache) > 0 {
        position = position - int32(len(m.positionCache)) + m.positionCache[len(m.positionCache)-1] + 1;
    }
        s[0][i] = position;
        s[1][i] = position;
        s[2][i] = position;
    }
        return s;
        }();
        var for _, mi = range batch.Multimodal {
        var img = mi.Multimodal[0].Tensor;
        ctx.Forward(img.Copy(ctx, hiddenStates.View(ctx, mi.Index*hiddenStates.Stride(1), img.Dim(0)*img.Dim(1))));
        var if grid, ok = mi.Multimodal[0].Data.(*Grid); ok {
        var for i = range img.Dim(1) {
        var w = grid.Width / m.spatialMergeSize;
        positionSlice[1][mi.Index+i] += int32(i / w);
        positionSlice[2][mi.Index+i] += int32(i % w);
    }
    }
    }
        var positions = ctx.Input().FromInts(slices.Concat(positionSlice...), len(positionSlice[0])*len(positionSlice));
        var for i, layer = range m.TextModel.Layers {
        m.Cache.SetLayer(i);
        var lastLayerOutputs ml.Tensor;
        if i == len(m.TextModel.Layers)-1 {
        lastLayerOutputs = batch.Outputs;
    }
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, lastLayerOutputs, m.Cache, m.TextOptions);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.TextModel.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }

    public static void init() {
        model.Register("qwen25vl", New);
    }
}
