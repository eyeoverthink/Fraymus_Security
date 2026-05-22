package com.fraymus.absorbed.models.glmocr;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "errors";
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
        public *VisionDownsample VisionDownsample;
        public *PatchMerger PatchMerger;
        public int32 imageTokenID;
        public int32 imageStartTokenID;
        public int32 imageEndTokenID;
    }
        var _ model.MultimodalProcessor = (*Model)(null);

    public static void New() {
        var eosTokenID = int32(c.Uint("tokenizer.ggml.eos_token_id"));
        var eosTokenIDs = c.Ints("tokenizer.ggml.eos_token_ids");
        var allEOS = append([]int32{eosTokenID}, eosTokenIDs...);
        var m = &Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", false),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS:    allEOS,;
        },;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        TextModel:         newTextModel(c),;
        VisionModel:       newVisionModel(c),;
        ImageProcessor:    newImageProcessor(c),;
        imageTokenID:      int32(c.Uint("image_token_id", 59280)),;
        imageStartTokenID: int32(c.Uint("image_start_token_id", 59256)),;
        imageEndTokenID:   int32(c.Uint("image_end_token_id", 59257)),;
    }
        m.Cache = kvcache.NewCausalCache(m.TextModel.Shift);
        return m, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Blocks) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var f32s, grid, err = m.ImageProcessor.ProcessImage(img);
        if err != null {
        return null, err;
    }
        var patchDim = m.VisionModel.numChannels * m.temporalPatchSize * m.patchSize * m.patchSize;
        var numPatches = grid.Temporal * grid.Height * grid.Width;
        var pixelValues = ctx.Input().FromFloats(f32s, patchDim, numPatches);
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues, grid);
        if m.VisionDownsample == null || m.VisionDownsample.Weight == null {
        return null, errors.New("glmocr: missing vision downsample weights");
    }
        visionOutputs = m.VisionDownsample.Forward(ctx, visionOutputs, grid, m.VisionModel.VisionModelOptions);
        if m.PatchMerger == null {
        return null, errors.New("glmocr: missing patch merger weights");
    }
        visionOutputs = m.PatchMerger.Forward(ctx, visionOutputs, m.VisionModel.VisionModelOptions);
        return []input.Multimodal{{Tensor: visionOutputs, Data: grid}}, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        m.TextModel.positionCache = m.TextModel.positionCache[:0];
        m.TextModel.ropeDelta = 0;
        var pos = int32(0);
        var for _, inp = range inputs {
        if inp.Multimodal == null {
        result = append(result, inp);
        m.TextModel.positionCache = append(m.TextModel.positionCache, pos);
        pos++;
        continue;
    }
        var grid = inp.Multimodal[0].Data.(*Grid);
        var mergedH = grid.Height / m.VisionModel.spatialMergeSize;
        var mergedW = grid.Width / m.VisionModel.spatialMergeSize;
        result = append(result, &input.Input{Token: m.imageStartTokenID});
        m.TextModel.positionCache = append(m.TextModel.positionCache, pos);
        pos++;
        var tokensPerGrid = inp.Multimodal[0].Tensor.Dim(1);
        var basePos = pos;
        var sameBatch = tokensPerGrid - 1;
        if sameBatch < 0 {
        sameBatch = 0;
    }
        result = append(result, &input.Input{
        Token:          m.imageTokenID,;
        Multimodal:     inp.Multimodal,;
        MultimodalHash: inp.MultimodalHash,;
        SameBatch:      sameBatch,;
        });
        m.TextModel.positionCache = append(m.TextModel.positionCache, basePos);
        for range tokensPerGrid - 1 {
        result = append(result, &input.Input{Token: m.imageTokenID});
        m.TextModel.positionCache = append(m.TextModel.positionCache, basePos);
    }
        pos = basePos + int32(max(mergedH, mergedW));
        result = append(result, &input.Input{Token: m.imageEndTokenID});
        m.TextModel.positionCache = append(m.TextModel.positionCache, pos);
        pos++;
    }
        if len(m.TextModel.positionCache) > 0 {
        var last = m.TextModel.positionCache[len(m.TextModel.positionCache)-1];
        m.TextModel.ropeDelta = last + 1 - int32(len(m.TextModel.positionCache));
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs).Duplicate(ctx);
        ctx.Forward(hiddenStates);
        var positionSlice = func() [][]int32 {
        var s = [][]int32{
        make([]int32, len(batch.Positions)), // temporal;
        make([]int32, len(batch.Positions)), // height;
        make([]int32, len(batch.Positions)), // width;
        make([]int32, len(batch.Positions)), // unused (zeros);
    }
        var for i, position = range batch.Positions {
        if position < int32(len(m.TextModel.positionCache)) {
        position = m.TextModel.positionCache[position];
        } else if len(m.TextModel.positionCache) > 0 {
        position = position + m.TextModel.ropeDelta;
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
        var w = grid.Width / m.VisionModel.spatialMergeSize;
        var for i = range img.Dim(1) {
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
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, lastLayerOutputs, m.Cache, m.TextModel.TextModelOptions);
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.TextModel.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }

    public static void init() {
        model.Register("glmocr", New);
    }
}
