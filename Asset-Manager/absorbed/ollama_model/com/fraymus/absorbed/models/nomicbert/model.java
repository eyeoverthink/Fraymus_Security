package com.fraymus.absorbed.models.nomicbert;

import java.util.*;
import java.io.*;

public class model {
        "cmp";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/pooling";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public *nn.Embedding TypeEmbedding;
        public *nn.LayerNorm TokenEmbeddingNorm;
        public []EncoderLayer Layers;
    }

    public static class Options {
        public int hiddenSize;
        public int numHeads;
        public int headDim;
        public float32 eps;
        public pooling.Type poolingType;
        public boolean normalize;
        public float32 ropeFreqBase;
        public int numExperts;
        public int numExpertsUsed;
        public int moeEveryNLayers;
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.headDim, o.ropeFreqBase, 1.0, rope.WithTypeNeoX());
    }

    public static class EncoderLayer {
        public *nn.LayerNorm AttentionNorm;
        public FeedForward FeedForward;
        public *nn.LayerNorm MLPNorm;
    }

    public static class Attention {
        public *nn.Linear QKV;
        public *nn.Linear Output;
    }
        type FeedForward interface {
        Forward(ml.Context, ml.Tensor, *Options) ml.Tensor;
    }

    public static class dense {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *dense) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ *Options) ml.Tensor {
        var hidden = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hidden);
    }

    public static class denseGELU {
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *denseGELU) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ *Options) ml.Tensor {
        return mlp.Down.Forward(ctx, mlp.Up.Forward(ctx, hiddenStates).GELU(ctx));
    }

    public static class sparse {
        public *nn.Linear Router;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
    }
        func (moe *sparse) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var hiddenDim, sequenceLength, batchSize = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, sequenceLength*batchSize);
        var routerLogits = moe.Router.Forward(ctx, hiddenStates);
        var routingWeights = routerLogits.Softmax(ctx);
        var selectedExperts = routingWeights.TopK(ctx, opts.numExpertsUsed);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExperts, hiddenStates.Dim(1)).Rows(ctx, selectedExperts);
        hiddenStates = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        hiddenStates = moe.Up.Forward(ctx, hiddenStates, selectedExperts).GELU(ctx);
        var experts = moe.Down.Forward(ctx, hiddenStates, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var nextStates = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextStates = nextStates.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextStates;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        var typeEmbed = m.TypeEmbedding.Weight.Slice(ctx, 1, 0, 1, 1);
        hiddenStates = hiddenStates.Add(ctx, typeEmbed);
        hiddenStates = m.TokenEmbeddingNorm.Forward(ctx, hiddenStates, m.eps);
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var for _, layer = range m.Layers {
        hiddenStates = layer.Forward(ctx, hiddenStates, positions, &m.Options);
    }
        hiddenStates = m.poolingType.Forward(ctx, hiddenStates);
        if m.normalize {
        hiddenStates = hiddenStates.L2Norm(ctx, 1e-12);
    }
        return hiddenStates, null;
    }
        func (e *EncoderLayer) Forward(ctx ml.Context, hiddenStates ml.Tensor, positions ml.Tensor, opts *Options) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.Attention.Forward(ctx, hiddenStates, positions, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        hiddenStates = e.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        residual = hiddenStates;
        hiddenStates = e.FeedForward.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        hiddenStates = e.MLPNorm.Forward(ctx, hiddenStates, opts.eps);
        return hiddenStates;
    }
        func (a *Attention) Forward(ctx ml.Context, hiddenStates ml.Tensor, positions ml.Tensor, opts *Options) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var qkv = a.QKV.Forward(ctx, hiddenStates);
        qkv = qkv.Reshape(ctx, opts.headDim, opts.numHeads*3, batchSize);
        var chunks = qkv.Chunk(ctx, 1, opts.numHeads);
        var query, key, value = chunks[0], chunks[1], chunks[2];
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.Attention(ctx, query, key, value, 1.0/math.Sqrt(double(opts.headDim)), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return a.Output.Forward(ctx, attention);
    }

    public static void New() {
        var hiddenSize = int(c.Uint("embedding_length"));
        var numHeads = int(c.Uint("attention.head_count"));
        var headDim = hiddenSize / numHeads;
        var blockCount = int(c.Uint("block_count"));
        var moeEveryNLayers = int(c.Uint("moe_every_n_layers", 0));
        var layers = make([]EncoderLayer, blockCount);
        var for i = range layers {
        if moeEveryNLayers > 0 {
        if (i+1)%moeEveryNLayers == 0 {
        layers[i].FeedForward = &sparse{}
        } else {
        layers[i].FeedForward = &denseGELU{}
    }
        } else {
        layers[i].FeedForward = &dense{}
    }
    }
        return &Model{
        Tokenizer: tokenizer.NewWordPiece(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS: []int32{
        int32(cmp.Or(;
        c.Uint("tokenizer.ggml.cls_token_id"),;
        c.Uint("tokenizer.ggml.bos_token_id"),;
        )),;
        },;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", true),;
        EOS: []int32{
        int32(cmp.Or(;
        c.Uint("tokenizer.ggml.separator_token_id"),;
        c.Uint("tokenizer.ggml.eos_token_id"),;
        )),;
        },;
        },;
        false,;
        ),;
        Layers: layers,;
        Options: Options{
        hiddenSize:      hiddenSize,;
        numHeads:        numHeads,;
        headDim:         headDim,;
        eps:             c.Float("attention.layer_norm_epsilon"),;
        poolingType:     pooling.Type(c.Uint("pooling_type")),;
        normalize:       c.Bool("normalize_embeddings", false),;
        ropeFreqBase:    c.Float("rope.freq_base", 1000.0),;
        numExperts:      int(c.Uint("expert_count")),;
        numExpertsUsed:  int(c.Uint("expert_used_count")),;
        moeEveryNLayers: moeEveryNLayers,;
        },;
        }, null;
    }

    public static void init() {
        model.Register("nomic-bert", New);
        model.Register("nomic-bert_embed", New);
        model.Register("nomic-bert-moe", New);
        model.Register("nomic-bert-moe_embed", New);
    }
}
