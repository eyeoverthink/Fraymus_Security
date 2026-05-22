package com.fraymus.absorbed.models.gemma3n;

import java.util.*;
import java.io.*;

public class model_text {
        "cmp";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model/input";
        );

    public static class TextModel {
        public *TextScaledWordEmbedding TokenEmbedding;
        public *nn.Linear AltupEmbd;
        public *nn.Linear AltupUnembd;
        public []TextLayer TextLayers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
    }
        func (m *TextModel) Forward(ctx ml.Context, batch input.Batch, cache kvcache.Cache) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var one = ctx.Input().FromFloats([]float32{1.0}, 1);
        var inputs = m.TokenEmbedding.Forward(ctx, batch.Inputs, math.Sqrt(double(m.hiddenSize)));
        var inputsPerLayer = m.PerLayerProjector.Forward(ctx, batch, inputs, &m.TextOptions);
        var targetMagnitude = inputs.Sqr(ctx).Mean(ctx).Sqrt(ctx);
        targetMagnitude = targetMagnitude.Repeat(ctx, 2, m.altupInputs-1);
        var hiddenState = inputs.Repeat(ctx, 2, m.altupInputs-1);
        var altupProj = m.AltupEmbd.Forward(ctx, hiddenState);
        altupProj = altupProj.Mul(ctx, targetMagnitude.Div(ctx, altupProj.Sqr(ctx).Mean(ctx).Sqrt(ctx)));
        var hiddenStates = inputs.Concat(ctx, altupProj, 2);
        var firstSharedKeyValue = m.hiddenLayers - m.sharedKeyValueLayers;
        var for i, layer = range m.TextLayers {
        if i < firstSharedKeyValue {
        cache.SetLayer(i);
        } else if m.isLocal(i) {
        cache.SetLayer(firstSharedKeyValue - 2);
        } else {
        cache.SetLayer(firstSharedKeyValue - 1);
    }
        var layerType int;
        var ropeBase = m.ropeBase;
        if m.isLocal(i) {
        layerType = 1;
        ropeBase = m.ropeBaseLocal;
    }
        cache.(*kvcache.WrapperCache).SetLayerType(layerType);
        var inputPerLayer = inputsPerLayer.View(ctx, i*inputsPerLayer.Stride(1), inputsPerLayer.Dim(0), inputsPerLayer.Stride(2), inputsPerLayer.Dim(2));
        hiddenStates = layer.Forward(ctx, hiddenStates, inputPerLayer, positions, one, cache, i >= firstSharedKeyValue, ropeBase, double(m.activationSparsityScale[i]), &m.TextOptions);
    }
        var hiddenStates0 = hiddenStates.Slice(ctx, 2, 0, 1, 1);
        targetMagnitude = hiddenStates0.Sqr(ctx).Mean(ctx).Sqrt(ctx);
        targetMagnitude = targetMagnitude.Repeat(ctx, 2, m.altupInputs-1);
        hiddenState = hiddenStates.Slice(ctx, 2, 1, hiddenStates.Dim(2), 1);
        var altupUnembdProj = m.AltupUnembd.Forward(ctx, hiddenState);
        altupUnembdProj = altupUnembdProj.Mul(ctx, targetMagnitude.Div(ctx, altupUnembdProj.Sqr(ctx).Mean(ctx).Sqrt(ctx)));
        hiddenStates = hiddenStates0.Concat(ctx, altupUnembdProj, 2);
        hiddenStates = hiddenStates.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx).Mean(ctx);
        hiddenStates = hiddenStates.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        hiddenStates = hiddenStates.Rows(ctx, batch.Outputs);
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }
        func (m *TextModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        var ropeBase = m.ropeBase;
        if m.isLocal(layer) {
        ropeBase = m.ropeBaseLocal;
    }
        return m.applyRotaryPositionEmbeddings(ctx, key, shift, ropeBase), null;
    }

    public static class TextScaledWordEmbedding {
    }
        func (e TextScaledWordEmbedding) Forward(ctx ml.Context, inputIDs ml.Tensor, scale double) ml.Tensor {
        return e.Embedding.Forward(ctx, inputIDs).Scale(ctx, scale);
    }

    public static class PerLayerProjector {
        public *TextScaledWordEmbedding TokenEmbedding;
        public *nn.Linear Projector;
        public *nn.RMSNorm Norm;
    }
        func (p PerLayerProjector) Forward(ctx ml.Context, batch input.Batch, inputs ml.Tensor, opts *TextOptions) ml.Tensor {
        var inputsPerLayer = p.TokenEmbedding.Forward(ctx, batch.Inputs, math.Sqrt(double(opts.hiddenSizePerLayerInput)));
        inputsPerLayer = inputsPerLayer.Reshape(ctx, opts.hiddenSizePerLayerInput, opts.hiddenLayers, batch.Inputs.Dim(0), batch.Inputs.Dim(1));
        var perLayerProjection = p.Projector.Forward(ctx, inputs);
        perLayerProjection = perLayerProjection.Scale(ctx, math.Sqrt(double(opts.hiddenSize)));
        perLayerProjection = perLayerProjection.Reshape(ctx, opts.hiddenSizePerLayerInput, opts.hiddenLayers, inputs.Dim(1));
        perLayerProjection = p.Norm.Forward(ctx, perLayerProjection, opts.eps);
        if inputsPerLayer != null {
        perLayerProjection = perLayerProjection.Add(ctx, inputsPerLayer);
        perLayerProjection = perLayerProjection.Scale(ctx, 1/math.Sqrt(2));
    }
        return perLayerProjection;
    }

    public static class TextLayer {
        public *nn.RMSNorm AttentionNorm;
        public *TextAttention Attention;
        public *nn.RMSNorm PostAttentionNorm;
        public *nn.RMSNorm MLPNorm;
        public *TextMLP MLP;
        public *nn.RMSNorm PostMLPNorm;
        public *nn.Linear PerLayerInputGate;
        public *nn.Linear PerLayerProjection;
        public *nn.RMSNorm PostPerLayerNorm;
    }
        func (d TextLayer) Forward(ctx ml.Context, hiddenStates, perLayerInput, positions, one ml.Tensor, cache kvcache.Cache, sharedKV boolean, ropeBase float32, activationSparsityScale double, opts *TextOptions) ml.Tensor {
        var predictions = d.Predict(ctx, hiddenStates, opts);
        var active = opts.altupActive(ctx, predictions);
        var attn = d.AttentionNorm.Forward(ctx, active, opts.eps);
        var laurel = d.Laurel.Forward(ctx, attn, opts);
        attn = d.Attention.Forward(ctx, attn, positions, cache, sharedKV, ropeBase, opts);
        attn = d.PostAttentionNorm.Forward(ctx, attn, opts.eps);
        attn = active.Add(ctx, attn);
        attn = attn.Add(ctx, laurel).Scale(ctx, 1/math.Sqrt(2));
        var mlp = d.MLPNorm.Forward(ctx, attn, opts.eps);
        mlp = d.MLP.Forward(ctx, mlp, activationSparsityScale);
        mlp = d.PostMLPNorm.Forward(ctx, mlp, opts.eps);
        mlp = attn.Add(ctx, mlp);
        predictions = d.Correct(ctx, predictions, mlp, one, opts);
        active = opts.altupActive(ctx, predictions);
        if opts.altupCorrectScale {
        active = d.ScaleCorrectedOutput(ctx, active);
    }
        active = d.PerLayerInputGate.Forward(ctx, active);
        active = active.GELU(ctx, perLayerInput);
        active = d.PerLayerProjection.Forward(ctx, active);
        active = d.PostPerLayerNorm.Forward(ctx, active, opts.eps);
        var inactive = predictions.Slice(ctx, 2, 1, predictions.Dim(2), 1);
        active = inactive.Add(ctx, active);
        var predictions0 = predictions.Slice(ctx, 2, 0, 1, 1);
        return predictions0.Concat(ctx, active, 2);
    }

    public static class AltUp {
        public ml.Tensor CorrectionScale;
        public *nn.Linear PredictionCoefficient;
        public *nn.Linear CorrectionCoefficient;
        public *nn.Linear Router;
        public *nn.RMSNorm RouterNorm;
    }
        func (a AltUp) computeRouterModalities(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        var routerInputs = a.RouterNorm.Forward(ctx, hiddenStates, opts.eps).Scale(ctx, 1.0/double(opts.hiddenSize));
        return a.Router.Forward(ctx, routerInputs).Tanh(ctx);
    }
        func (a AltUp) Predict(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        var modalities = a.computeRouterModalities(ctx, opts.altupActive(ctx, hiddenStates), opts);
        var coefficients = a.PredictionCoefficient.Forward(ctx, modalities);
        coefficients = coefficients.Reshape(ctx, opts.altupInputs, opts.altupInputs, coefficients.Dim(1), coefficients.Dim(2));
        var predictions = coefficients.Mulmat(ctx, hiddenStates.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx));
        predictions = predictions.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        return predictions.Add(ctx, hiddenStates);
    }
        func (a AltUp) Correct(ctx ml.Context, predictions, activated, one ml.Tensor, opts *TextOptions) ml.Tensor {
        var innovation = activated.Sub(ctx, opts.altupActive(ctx, predictions));
        innovation = innovation.Repeat(ctx, 2, opts.altupInputs);
        var modalities = a.computeRouterModalities(ctx, activated, opts);
        var coefficients = a.CorrectionCoefficient.Forward(ctx, modalities);
        coefficients = coefficients.Add(ctx, one);
        coefficients = coefficients.Reshape(ctx, 1, coefficients.Dim(0), coefficients.Dim(1));
        coefficients = coefficients.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        var corrected = innovation.Mul(ctx, coefficients);
        corrected = corrected.Add(ctx, predictions);
        return corrected;
    }
        func (a AltUp) ScaleCorrectedOutput(ctx ml.Context, predictions ml.Tensor) ml.Tensor {
        return predictions.Mul(ctx, a.CorrectionScale);
    }

    public static class Laurel {
        public *nn.Linear LinearLeft;
        public *nn.Linear LinearRight;
        public *nn.RMSNorm PostLaurelNorm;
    }
        func (l Laurel) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *TextOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = l.LinearLeft.Forward(ctx, hiddenStates);
        hiddenStates = l.LinearRight.Forward(ctx, hiddenStates);
        hiddenStates = l.PostLaurelNorm.Forward(ctx, hiddenStates, opts.eps);
        return hiddenStates.Add(ctx, residual);
    }

    public static class TextAttention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (attn TextAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, sharedKV boolean, ropeBase float32, opts *TextOptions) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var query = attn.Query.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, batchSize);
        query = attn.QueryNorm.Forward(ctx, query, opts.eps);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions, ropeBase);
        var key, value ml.Tensor;
        if !sharedKV {
        key = attn.Key.Forward(ctx, hiddenStates);
        key = key.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
        key = attn.KeyNorm.Forward(ctx, key, opts.eps);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions, ropeBase);
        value = attn.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, opts.headDim(), opts.numKVHeads, batchSize);
        value = value.RMSNorm(ctx, null, opts.eps);
    }
        var attention = nn.Attention(ctx, query, key, value, 1., cache);
        attention = attention.Reshape(ctx, attention.Dim(0)*attention.Dim(1), batchSize);
        return attn.Output.Forward(ctx, attention);
    }

    public static class TextMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp TextMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, activationSparsityScale double) ml.Tensor {
        var upStates = mlp.Up.Forward(ctx, hiddenStates);
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates);
        if activationSparsityScale > 0 {
        var mean = hiddenStates.Mean(ctx);
        var std = hiddenStates.Stddev(ctx).Scale(ctx, activationSparsityScale);
        var cutoff = mean.Add(ctx, std);
        hiddenStates = hiddenStates.Sub(ctx, cutoff).RELU(ctx);
    }
        hiddenStates = hiddenStates.GELU(ctx, upStates);
        hiddenStates = mlp.Down.Forward(ctx, hiddenStates);
        return hiddenStates;
    }

    public static class TextOptions {
        public int hiddenLayers;
        public int hiddenSize;
        public int hiddenSizePerLayerInput;
        public numKVHeads numHeads,;
        public valueLength keyLength,;
        public int sharedKeyValueLayers;
        public int altupActiveIndex;
        public int altupInputs;
        public boolean altupCorrectScale;
        public float32 eps;
        public float32 ropeBase;
        public float32 ropeBaseLocal;
        public float32 ropeScale;
        public []boolean slidingWindowPattern;
        public []float32 activationSparsityScale;
    }
        func (o *TextOptions) altupActive(ctx ml.Context, t ml.Tensor) ml.Tensor {
        return t.Slice(ctx, 2, o.altupActiveIndex, o.altupActiveIndex+1, 1);
    }
        func (o *TextOptions) headDim() int {
        return cmp.Or(o.keyLength, o.valueLength, o.hiddenSize/o.numHeads);
    }
        func (o *TextOptions) isLocal(i int) boolean {
        return o.slidingWindowPattern[i];
    }
        func (o TextOptions) applyRotaryPositionEmbeddings(ctx ml.Context, t, p ml.Tensor, base float32) ml.Tensor {
        return nn.RoPE(ctx, t, p, o.headDim(), base, 1./o.ropeScale, rope.WithTypeNeoX());
    }
        func newTextModel(c fs.Config) *TextModel {
        return &TextModel{
        TextLayers: make([]TextLayer, c.Uint("block_count")),;
        TextOptions: TextOptions{
        hiddenLayers:            int(c.Uint("block_count")),;
        hiddenSize:              int(c.Uint("embedding_length")),;
        hiddenSizePerLayerInput: int(c.Uint("embedding_length_per_layer_input")),;
        numHeads:                int(c.Uint("attention.head_count")),;
        numKVHeads:              int(c.Uint("attention.head_count_kv")),;
        keyLength:               int(c.Uint("attention.key_length")),;
        valueLength:             int(c.Uint("attention.value_length")),;
        sharedKeyValueLayers:    int(c.Uint("attention.shared_kv_layers")),;
        altupActiveIndex: int(c.Uint("altup.active_idx")),;
        altupInputs:      int(c.Uint("altup.num_inputs")),;
        eps:           c.Float("attention.layer_norm_rms_epsilon", 1e-06),;
        ropeBase:      c.Float("rope.freq_base", 1_000_000),;
        ropeBaseLocal: c.Float("rope.freq_base_local", 10_000),;
        ropeScale:     c.Float("rope.scaling.factor", 1.0),;
        slidingWindowPattern:    c.Bools("attention.sliding_window_pattern"),;
        activationSparsityScale: c.Floats("activation_sparsity_scale"),;
        },;
    }
    }
}
