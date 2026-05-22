package com.fraymus.absorbed.models.gemma3;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "image";
        "math";
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

    public static class MultiModalProjector {
        public *nn.RMSNorm SoftEmbNorm;
        public *nn.Linear InputProjection;
        public int tokensPerImage;
    }
        func (p *MultiModalProjector) Forward(ctx ml.Context, visionOutputs ml.Tensor, imageSize, patchSize int, eps float32) ml.Tensor {
        var l = visionOutputs.Dim(0);
        visionOutputs = visionOutputs.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        var patchesPerImage = imageSize / patchSize;
        visionOutputs = visionOutputs.Reshape(ctx, patchesPerImage, patchesPerImage, l);
        var kernelSize = patchesPerImage / int(math.Sqrt(double(p.tokensPerImage)));
        visionOutputs = visionOutputs.AvgPool2D(ctx, kernelSize, kernelSize, 0);
        visionOutputs = visionOutputs.Reshape(ctx, visionOutputs.Dim(0)*visionOutputs.Dim(1), l);
        visionOutputs = visionOutputs.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx);
        visionOutputs = p.SoftEmbNorm.Forward(ctx, visionOutputs, eps);
        visionOutputs = p.InputProjection.Weight.Permute(ctx, 1, 0, 2, 3).Contiguous(ctx).Mulmat(ctx, visionOutputs);
        return visionOutputs;
    }

    public static void New() {
        var vocabulary = tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{
        int32(c.Uint("tokenizer.ggml.eos_token_id")),;
        },;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
    }
        var t tokenizer.Tokenizer;
        switch c.String("tokenizer.ggml.model") {
        case "gpt2":;
        t = tokenizer.NewBytePairEncoding(&vocabulary);
        default:;
        vocabulary.EOS = append(vocabulary.EOS, int32(c.Uint("tokenizer.ggml.eot_token_id", 106)));
        t = tokenizer.NewSentencePiece(&vocabulary);
    }
        var m = Model{
        Tokenizer:      t,;
        ImageProcessor: newImageProcessor(c),;
        VisionModel:    newVisionModel(c),;
        TextModel:      newTextModel(c),;
        MultiModalProjector: &MultiModalProjector{
        tokensPerImage: int(c.Uint("mm_tokens_per_image", 256)),;
        },;
    }
        var slidingWindowLen = int32(c.Uint("attention.sliding_window"));
        m.Cache = kvcache.NewWrapperCache(kvcache.NewSWACache(slidingWindowLen, m.Shift), kvcache.NewCausalCache(m.Shift));
        return &m, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var image, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var f32s, err = m.ImageProcessor.ProcessImage(image);
        if err != null {
        return null, err;
    }
        var pixelValues = ctx.Input().FromFloats(f32s,;
        m.ImageProcessor.imageSize,;
        m.ImageProcessor.imageSize,;
        m.ImageProcessor.numChannels,;
        );
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues);
        visionOutputs = m.MultiModalProjector.Forward(ctx, visionOutputs, m.imageSize, m.patchSize, m.VisionModel.eps);
        return []input.Multimodal{{Tensor: visionOutputs}}, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        var for _, inp = range inputs {
        if len(inp.Multimodal) == 0 {
        result = append(result, inp);
        } else {
        var inputMultimodal = inp.Multimodal[0].Tensor;
        result = append(result,;
        &input.Input{Token: 108, SameBatch: inputMultimodal.Dim(1) + 3}, // "\n\n";
        &input.Input{Token: 255999},                                     // "<start_of_image>"";
        &input.Input{Multimodal: []input.Multimodal{{Tensor: inputMultimodal}}, MultimodalHash: inp.MultimodalHash}, // image data is on the first placeholder;
        );
        result = append(result, slices.Repeat([]*input.Input{{Token: 0}}, inputMultimodal.Dim(1)-1)...);
        result = append(result,;
        &input.Input{Token: 256000}, // <end_of_image>;
        &input.Input{Token: 108},    // "\n\n";
        );
    }
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenState = m.TextModel.Forward(ctx, batch, m.Cache);
        hiddenState = m.Output.Forward(ctx, hiddenState);
        if m.TextConfig.finalLogitSoftcap > 0.0 {
        hiddenState = hiddenState.Scale(ctx, 1.0/double(m.TextConfig.finalLogitSoftcap));
        hiddenState = hiddenState.Tanh(ctx);
        hiddenState = hiddenState.Scale(ctx, double(m.TextConfig.finalLogitSoftcap));
    }
        return hiddenState, null;
    }

    public static void init() {
        model.Register("gemma3", New);
        model.Register("gemma3_embed", newEmbedModel);
    }
}
