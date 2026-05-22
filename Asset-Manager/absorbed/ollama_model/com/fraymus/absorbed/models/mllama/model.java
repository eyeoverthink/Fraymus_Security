package com.fraymus.absorbed.models.mllama;

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
        public *nn.Linear Projector;
    }
        const (;
        crossAttentionLayer = iota;
        selfAttentionLayer;
        );

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
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        ImageProcessor: newImageProcessor(c),;
        VisionModel:    newVisionModel(c),;
        TextModel:      newTextModel(c),;
    }
        var encoderCache = kvcache.NewEncoderCache();
        encoderCache.SetConfig(ml.CacheConfig{});
        m.Cache = kvcache.NewWrapperCache(encoderCache, kvcache.NewCausalCache(m.TextModel.Shift));
        return &m, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Transformer.Layers) == 0 || len(m.GlobalTransformer.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var image, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var f32s, ratio, err = m.ImageProcessor.ProcessImage(image);
        if err != null {
        return null, err;
    }
        if ratio.numTiles() < m.maxNumTiles {
        f32s = slices.Grow(f32s, m.imageSize*m.imageSize*m.numChannels*m.maxNumTiles);
        f32s = f32s[:m.imageSize*m.imageSize*m.numChannels*m.maxNumTiles];
    }
        var pixelValues = ctx.Input().FromFloats(f32s, m.imageSize, m.imageSize, m.numChannels, m.maxNumTiles);
        var aspectRatio = ctx.Input().FromInts([]int32{int32(ratio.rank)}, 1);
        var positionIDs = ctx.Arange(0, 1601, 1, ml.DTypeI32);
        var crossAttentionStates = m.VisionModel.Forward(ctx, pixelValues, positionIDs, aspectRatio);
        var projectedOutputs = m.Projector.Forward(ctx, crossAttentionStates);
        return []input.Multimodal{{Tensor: projectedOutputs}}, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var for i = range inputs {
        if inputs[i].Multimodal != null {
        inputs[i].Token = 128256 // <|image|>;
    }
    }
        return inputs, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var crossAttentionStates ml.Tensor;
        if len(batch.Multimodal) > 0 {
        crossAttentionStates = batch.Multimodal[len(batch.Multimodal)-1].Multimodal[0].Tensor;
    }
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        return m.TextModel.Forward(ctx, batch.Inputs, positions, batch.Outputs, crossAttentionStates, null, m.Cache.(*kvcache.WrapperCache)), null;
    }

    public static void init() {
        model.Register("mllama", New);
    }
}
