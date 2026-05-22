package com.fraymus.absorbed.models.qwen3vl;

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
        public []int32 positionCache;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var pixelValues, grid, err = m.ProcessImage(ctx, img);
        if err != null {
        return null, err;
    }
        var visionOutputs, deepstackVisualEmbeds = m.VisionModel.Forward(ctx, pixelValues, grid);
        var mm = []input.Multimodal{{Tensor: visionOutputs, Data: grid}}
        var for i = range deepstackVisualEmbeds {
        mm = append(mm, input.Multimodal{Tensor: deepstackVisualEmbeds[i]});
    }
        return mm, null;
    }
        var (;
        tokenVision      int32 = 151655;
        tokenVisionStart int32 = 151652;
        tokenVisionEnd   int32 = 151653;
        );
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        m.positionCache = m.positionCache[:0];
        var result []*input.Input;
        var appendInput = func(inp *input.Input, position int32) {
        result = append(result, inp);
        m.positionCache = append(m.positionCache, position);
    }
        var p int32;
        var for _, inp = range inputs {
        if inp.Multimodal == null {
        appendInput(inp, p);
        p++;
        continue;
    }
        var grid = inp.Multimodal[0].Data.(*Grid);
        var tokensPerGrid = inp.Multimodal[0].Tensor.Dim(1);
        appendInput(&input.Input{Token: tokenVisionStart}, p);
        p++;
        appendInput(&input.Input{
        Token:          tokenVision,;
        Multimodal:     inp.Multimodal,;
        MultimodalHash: inp.MultimodalHash,;
        SameBatch:      tokensPerGrid,;
        }, p);
        for range tokensPerGrid - 1 {
        appendInput(&input.Input{Token: tokenVision}, p);
    }
        p = p + int32(grid.Width/m.spatialMergeSize);
        appendInput(&input.Input{Token: tokenVisionEnd}, p);
        p++;
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positionSlice = slices.Collect(makeSlice2D[int32](4, len(batch.Positions)));
        var for i, id = range batch.Positions {
        if id < int32(len(m.positionCache)) {
        id = m.positionCache[id];
        } else if len(m.positionCache) > 0 {
        id = id - int32(len(m.positionCache)) + m.positionCache[len(m.positionCache)-1] + 1;
    }
        positionSlice[0][i] = id;
        positionSlice[1][i] = id;
        positionSlice[2][i] = id;
    }
        var hiddenStates = m.TextModel.TokenEmbedding.Forward(ctx, batch.Inputs).Duplicate(ctx);
        var deepstackVisualEmbeds []ml.Tensor;
        var for _, mi = range batch.Multimodal {
        var visionOutputs = mi.Multimodal[0].Tensor;
        ctx.Forward(visionOutputs.Copy(ctx, hiddenStates.View(ctx, mi.Index*hiddenStates.Stride(1), visionOutputs.Dim(0)*visionOutputs.Dim(1))));
        var if grid, ok = mi.Multimodal[0].Data.(*Grid); ok {
        var for i = range visionOutputs.Dim(1) {
        var w = grid.Width / m.spatialMergeSize;
        positionSlice[1][mi.Index+i] += int32(i / w);
        positionSlice[2][mi.Index+i] += int32(i % w);
    }
    }
        if len(mi.Multimodal[1:]) > len(deepstackVisualEmbeds) {
        deepstackVisualEmbeds = append(deepstackVisualEmbeds, make([]ml.Tensor, len(mi.Multimodal[1:])-len(deepstackVisualEmbeds))...);
    }
        var for i, mm = range mi.Multimodal[1:] {
        if deepstackVisualEmbeds[i] == null {
        deepstackVisualEmbeds[i] = ctx.Input().Zeros(mm.Tensor.DType(), hiddenStates.Shape()...);
    }
        ctx.Forward(mm.Tensor.Copy(ctx, deepstackVisualEmbeds[i].View(ctx, mi.Index*deepstackVisualEmbeds[i].Stride(1), mm.Tensor.Dim(0)*mm.Tensor.Dim(1))));
    }
    }
        var positions = ctx.Input().FromInts(slices.Concat(positionSlice...), len(positionSlice[0])*len(positionSlice));
        var for i, layer = range m.TextModel.Layers {
        if m.Cache != null {
        m.Cache.SetLayer(i);
    }
        var outputs ml.Tensor;
        if i == len(m.TextModel.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, outputs, m.Cache, m.Options);
        if i < len(deepstackVisualEmbeds) {
        hiddenStates = hiddenStates.Add(ctx, deepstackVisualEmbeds[i]);
    }
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, 1e-06);
        return m.Output.Forward(ctx, hiddenStates), null;
    }

    public static void New() {
        var m = Model{
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
        TextModel:      newTextModel(c),;
        VisionModel:    NewVisionModel(c),;
        ImageProcessor: NewImageProcessor(c),;
    }
        m.Cache = kvcache.NewCausalCache(func(ctx ml.Context, layer int, key, positions ml.Tensor) (ml.Tensor, error) {
        m.positionCache = null;
        positions = positions.Repeat(ctx, 1, 4).Reshape(ctx, -1);
        return m.Options.applyRotaryPositionEmbeddings(ctx, key, positions), null;
        });
        return &m, null;
    }

    public static void init() {
        model.Register("qwen3vl", New);
        model.Register("qwen3vlmoe", New);
    }
}
