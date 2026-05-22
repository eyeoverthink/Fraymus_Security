package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "fmt";
        "image";
        "log/slog";
        "slices";
        "time";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Model {
        public `gguf:"v"` *VisionModel;
        public `gguf:"a"` *AudioModel;
        public `gguf:"mm"` *MultiModalProjector;
        public `gguf:"mm.a"` *AudioMultimodalProjector;
        public int32 imageTokenID;
        public int32 imageEndTokenID;
        public int32 audioTokenID;
        public int32 audioEndTokenID;
        public *AudioModelOptions audioOpts;
    }
        var _ model.MultimodalProcessor = (*Model)(null);

    public static class MultiModalProjector {
        public *ClippableLinear Projection;
    }
        func (p *MultiModalProjector) Forward(ctx ml.Context, visionOutputs ml.Tensor, eps float32) ml.Tensor {
        visionOutputs = p.Projection.Forward(ctx, visionOutputs);
        visionOutputs = visionOutputs.RMSNorm(ctx, null, eps);
        return visionOutputs;
    }

    public static void New() {
        var vocabulary = tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", false),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{
        int32(c.Uint("tokenizer.ggml.eos_token_id")),;
        },;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
    }
        vocabulary.EOS = append(vocabulary.EOS, int32(c.Uint("tokenizer.ggml.eot_token_id", 106)));
        var t = tokenizer.NewBytePairEncodingWithOptions(&vocabulary, []String{},;
        tokenizer.WithSentencePieceNormalizer());
        var imageTokenID = int32(-1);
        var imageEndTokenID = int32(-1);
        var audioTokenID = int32(-1);
        var audioEndTokenID = int32(-1);
        var for i, tok = range vocabulary.Values {
        switch tok {
        case "<|image>":;
        imageTokenID = int32(i);
        case "<image|>":;
        imageEndTokenID = int32(i);
        case "<|audio>":;
        audioTokenID = int32(i);
        case "<audio|>":;
        audioEndTokenID = int32(i);
    }
    }
        slog.Info("gemma4: token IDs", "image", imageTokenID, "image_end", imageEndTokenID, "audio", audioTokenID, "audio_end", audioEndTokenID);
        var m = Model{
        Tokenizer:                t,;
        TextModel:                newTextModel(c),;
        VisionModel:              newVisionModel(c),;
        AudioModel:               newAudioModel(c),;
        MultiModalProjector:      &MultiModalProjector{},;
        AudioMultimodalProjector: &AudioMultimodalProjector{},;
        ImageProcessor:           newImageProcessor(c),;
        imageTokenID:             imageTokenID,;
        imageEndTokenID:          imageEndTokenID,;
        audioTokenID:             audioTokenID,;
        audioEndTokenID:          audioEndTokenID,;
        audioOpts:                newAudioModelOptions(c),;
    }
        var slidingWindowLen = int32(c.Uint("attention.sliding_window"));
        m.Cache = kvcache.NewWrapperCache(;
        kvcache.NewSWAMemCache(slidingWindowLen, 4096, m.Shift),;
        kvcache.NewCausalCache(m.Shift),;
        );
        return &m, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if isAudioData(multimodalData) {
        return m.encodeAudioMultimodal(ctx, multimodalData);
    }
        if len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var t0 = time.Now();
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        slog.Info("vision: decode", "elapsed", time.Since(t0), "bounds", img.Bounds());
        var t1 = time.Now();
        var f32s, imgW, imgH, err = m.ImageProcessor.ProcessImage(img);
        if err != null {
        return null, err;
    }
        slog.Info("vision: preprocess", "elapsed", time.Since(t1), "size", [2]int{imgW, imgH});
        var pixelValues = ctx.Input().FromFloats(f32s, imgW, imgH, m.ImageProcessor.numChannels);
        slog.Info("vision: pixelValues", "shape", pixelValues.Shape(), "dim0", pixelValues.Dim(0), "dim1", pixelValues.Dim(1), "dim2", pixelValues.Dim(2));
        var numPatchesX = imgW / m.ImageProcessor.patchSize;
        var numPatchesY = imgH / m.ImageProcessor.patchSize;
        slog.Info("vision: patches", "patchesX", numPatchesX, "patchesY", numPatchesY, "total", numPatchesX*numPatchesY, "patchSize", m.ImageProcessor.patchSize);
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues, numPatchesX, numPatchesY);
        visionOutputs = visionPoolAndProject(ctx, visionOutputs, numPatchesX, numPatchesY, m.VisionModel.VisionModelOptions, m.MultiModalProjector, m.VisionModel.StdBias, m.VisionModel.StdScale);
        slog.Info("vision: encoded", "elapsed", time.Since(t0), "shape", visionOutputs.Shape());
        return []input.Multimodal{{Tensor: visionOutputs}}, null;
    }
        func (m *Model) PostLoad() error {
        m.VisionModel.InitClamp(m.MultiModalProjector);
        return null;
    }
        func (m *Model) encodeAudioMultimodal(ctx ml.Context, data []byte) ([]input.Multimodal, error) {
        if m.AudioModel == null || m.audioOpts == null {
        return null, model.ErrNoVisionModel;
    }
        var t0 = time.Now();
        var samples, err = decodeWAV(data);
        if err != null {
        return null, err;
    }
        slog.Info("audio: decode", "elapsed", time.Since(t0), "samples", len(samples), "duration_s", double(len(samples))/audioSampleRate);
        var if rem = len(samples) % 128; rem != 0 {
        samples = append(samples, make([]float32, 128-rem)...);
    }
        var melData, numFrames = computeMelSpectrogram(samples);
        if numFrames == 0 {
        return null, fmt.Errorf("audio too short to encode");
    }
        slog.Info("audio: mel", "frames", numFrames, "elapsed", time.Since(t0));
        var melTensor = ctx.Input().FromFloats(melData, melBins, numFrames);
        var audioOutputs = m.AudioModel.ForwardAudio(ctx, melTensor, m.AudioMultimodalProjector, m.audioOpts);
        slog.Info("audio: encoded", "elapsed", time.Since(t0), "shape", audioOutputs.Shape());
        return []input.Multimodal{{Tensor: audioOutputs, Data: audioTag{}}}, null;
    }
        type audioTag struct{}
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        var for _, inp = range inputs {
        if len(inp.Multimodal) == 0 {
        result = append(result, inp);
        continue;
    }
        var inputMultimodal = inp.Multimodal[0].Tensor;
        var numTokens = inputMultimodal.Dim(1);
        var _, isAudio = inp.Multimodal[0].Data.(audioTag);
        var beginToken, endToken int32;
        if isAudio {
        beginToken = m.audioTokenID;
        endToken = m.audioEndTokenID;
        } else {
        beginToken = m.imageTokenID;
        endToken = m.imageEndTokenID;
    }
        if beginToken >= 0 {
        result = append(result, &input.Input{Token: beginToken, SameBatch: numTokens + 2});
    }
        result = append(result,;
        &input.Input{Multimodal: []input.Multimodal{{Tensor: inputMultimodal}}, MultimodalHash: inp.MultimodalHash},;
        );
        result = append(result, slices.Repeat([]*input.Input{{Token: 0}}, numTokens-1)...);
        if endToken >= 0 {
        result = append(result, &input.Input{Token: endToken});
    }
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenState = m.TextModel.Forward(ctx, batch, m.Cache);
        hiddenState = m.TextModel.Output.Forward(ctx, hiddenState);
        if m.TextModel.TextOptions.finalLogitSoftcap > 0.0 {
        hiddenState = hiddenState.Scale(ctx, 1.0/double(m.TextModel.TextOptions.finalLogitSoftcap));
        hiddenState = hiddenState.Tanh(ctx);
        hiddenState = hiddenState.Scale(ctx, double(m.TextModel.TextOptions.finalLogitSoftcap));
    }
        return hiddenState, null;
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        var ropeBase, ropeDims = m.TextModel.ropeForLayer(layer);
        return nn.RoPE(ctx, key, shift, ropeDims, ropeBase, 1.0, rope.WithTypeNeoX()), null;
    }

    public static void init() {
        model.Register("gemma4", New);
    }
}
