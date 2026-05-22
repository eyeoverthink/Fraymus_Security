package com.fraymus.absorbed.models.deepseekocr;

import java.util.*;
import java.io.*;

public class model_text {
        "math";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        );

    public static class textModel {
        public *nn.Embedding TokenEmbedding;
        public []textBlock Blocks;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public textOptions Options;
    }
        func (m *textModel) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.Options.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static class textOptions {
        public int numExpertsUsed;
        public float32 eps;
    }
        func (o textOptions) headDim() int {
        return o.hiddenSize / o.numHeads;
    }
        func (o textOptions) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        return nn.RoPE(ctx, states, positions, o.headDim(), o.ropeBase, 1/o.ropeScale, rope.WithTypeNeoX());
    }

    public static class textBlock {
        public *nn.RMSNorm AttentionNorm;
        public *textAttention Attention;
        public *nn.RMSNorm MLPNNorm;
        public textFeedForward FeedForward;
    }
        func (m *textBlock) Forward(ctx ml.Context, hiddenStates, positions, outputs ml.Tensor, cache kvcache.Cache, opts textOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = m.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.Attention.Forward(ctx, hiddenStates, positions, cache, opts);
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = m.MLPNNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.FeedForward.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class textAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (m *textAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache kvcache.Cache, opts textOptions) ml.Tensor {
        var query = m.Query.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, -1);
        var key = m.Key.Forward(ctx, hiddenStates);
        key = key.Reshape(ctx, opts.headDim(), opts.numKVHeads, -1);
        var value = m.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, opts.headDim(), opts.numKVHeads, -1);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(opts.headDim())), cache);
        attention = attention.Reshape(ctx, -1, attention.Dim(2));
        return m.Output.Forward(ctx, attention);
    }
        type textFeedForward interface {
        Forward(ml.Context, ml.Tensor, textOptions) ml.Tensor;
    }

    public static class textMoe {
        public *nn.Linear Router;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
        public *textMLP SharedExperts;
    }
        func (m *textMoe) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts textOptions) ml.Tensor {
        var scores = m.Router.Forward(ctx, hiddenStates).Softmax(ctx);
        var indices = scores.TopK(ctx, opts.numExpertsUsed);
        var weights = scores.Reshape(ctx, 1, opts.numExperts, hiddenStates.Dim(1)).Rows(ctx, indices);
        var experts = hiddenStates.Reshape(ctx, hiddenStates.Dim(0), 1, hiddenStates.Dim(1));
        experts = m.Gate.Forward(ctx, experts, indices).SILU(ctx, m.Up.Forward(ctx, experts, indices));
        experts = m.Down.Forward(ctx, experts, indices);
        experts = experts.Mul(ctx, weights);
        var expert = func(i int) ml.Tensor {
        return experts.View(;
        ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2),;
        );
    }
        var routedStates = expert(0);
        var for i = 1; i < opts.numExpertsUsed; i++ {
        routedStates = routedStates.Add(ctx, expert(i));
    }
        var sharedStates = m.SharedExperts.Forward(ctx, hiddenStates, opts);
        return routedStates.Add(ctx, sharedStates);
    }

    public static class textMLP {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (m *textMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ textOptions) ml.Tensor {
        hiddenStates = m.Gate.Forward(ctx, hiddenStates).SILU(ctx, m.Up.Forward(ctx, hiddenStates));
        return m.Down.Forward(ctx, hiddenStates);
    }
}
