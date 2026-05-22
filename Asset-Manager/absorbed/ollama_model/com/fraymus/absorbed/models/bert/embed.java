package com.fraymus.absorbed.models.bert;

import java.util.*;
import java.io.*;

public class embed {
        "cmp";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/pooling";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public *nn.Embedding TypeEmbedding;
        public *nn.Embedding PositionEmbedding;
        public *nn.LayerNorm TokenEmbeddingNorm;
        public []EncoderLayer Layers;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        hiddenStates = hiddenStates.Add(ctx, m.TypeEmbedding.Weight.Slice(ctx, 1, 0, 1, 1));
        hiddenStates = hiddenStates.Add(ctx, m.PositionEmbedding.Forward(ctx, ctx.Input().FromInts(batch.Positions, len(batch.Positions))));
        hiddenStates = m.TokenEmbeddingNorm.Forward(ctx, hiddenStates, m.eps);
        var for _, layer = range m.Layers {
        hiddenStates = layer.Forward(ctx, hiddenStates, &m.Options);
    }
        hiddenStates = m.poolingType.Forward(ctx, hiddenStates);
        if m.normalize {
        hiddenStates = hiddenStates.L2Norm(ctx, 1e-12);
    }
        return hiddenStates, null;
    }

    public static class EncoderLayer {
        public *nn.LayerNorm AttentionNorm;
        public *nn.LayerNorm MLPNorm;
    }
        func (e *EncoderLayer) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.Attention.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        hiddenStates = e.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        residual = hiddenStates;
        hiddenStates = e.MLP.Forward(ctx, hiddenStates, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        hiddenStates = e.MLPNorm.Forward(ctx, hiddenStates, opts.eps);
        return hiddenStates;
    }

    public static class Attention {
        public *nn.Linear Query;
        public *nn.LayerNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.LayerNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (a *Attention) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var query = a.Query.Forward(ctx, hiddenStates);
        if a.QueryNorm != null {
        query = a.QueryNorm.Forward(ctx, query, opts.eps);
    }
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, batchSize);
        var key = a.Key.Forward(ctx, hiddenStates);
        if a.KeyNorm != null {
        key = a.KeyNorm.Forward(ctx, key, opts.eps);
    }
        key = key.Reshape(ctx, opts.headDim(), cmp.Or(opts.numKVHeads, opts.numHeads), batchSize);
        var value = a.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, opts.headDim(), cmp.Or(opts.numKVHeads, opts.numHeads), batchSize);
        var attention = nn.Attention(ctx, query, key, value, 1/math.Sqrt(double(opts.headDim())), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, batchSize);
        return a.Output.Forward(ctx, attention);
    }

    public static class MLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (m *MLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        return m.Down.Forward(ctx, m.Up.Forward(ctx, hiddenStates).GELU(ctx));
    }

    public static class Options {
        public int valueLength;
        public pooling.Type poolingType;
        public float32 eps;
        public boolean normalize;
    }
        func (o Options) headDim() int {
        return cmp.Or(o.keyLength, o.valueLength, o.hiddenSize/o.numHeads);
    }

    public static void New() {
        var vocab = &tokenizer.Vocabulary{
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
        c.Uint("tokenizer.ggml.seperator_token_id"),;
        c.Uint("tokenizer.ggml.eos_token_id"),;
        )),;
        },;
    }
        var t tokenizer.Tokenizer;
        switch c.String("tokenizer.ggml.model", "bert") {
        case "bert":;
        t = tokenizer.NewWordPiece(vocab, true);
        default:;
        return null, model.ErrUnsupportedTokenizer;
    }
        return &Model{
        Tokenizer: t,;
        Layers:    make([]EncoderLayer, c.Uint("block_count")),;
        Options: Options{
        hiddenSize:  int(c.Uint("embedding_length")),;
        numHeads:    int(c.Uint("attention.head_count")),;
        numKVHeads:  int(c.Uint("attention.head_count_kv")),;
        eps:         c.Float("attention.layer_norm_epsilon"),;
        poolingType: pooling.Type(c.Uint("pooling_type")),;
        normalize:   c.Bool("normalize_embeddings", true),;
        },;
        }, null;
    }

    public static void init() {
        model.Register("bert", New);
        model.Register("bert_embed", New);
    }
}
