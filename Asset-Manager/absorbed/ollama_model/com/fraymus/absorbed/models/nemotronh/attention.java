package com.fraymus.absorbed.models.nemotronh;

import java.util.*;
import java.io.*;

public class attention {
        "fmt";
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class Attention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (a *Attention) Forward(ctx ml.Context, hiddenStates ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var hiddenDim = hiddenStates.Dim(0);
        var nSeqTokens = hiddenStates.Dim(1);
        switch hiddenStates.Dim(2) {
        case 0:;
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, nSeqTokens, 1);
        case 1:;
        default:;
        return null, ErrUnsupportedBatchLayout;
    }
        if cache != null && cache.IsSupportedForBatch() {
        if cache.numSeqs() != 1 {
        return null, ErrUnsupportedBatchLayout;
    }
        var if seqTokens = cache.seqTokens(); seqTokens > 0 && nSeqTokens != seqTokens {
        return null, ErrUnsupportedBatchLayout;
    }
    }
        var batchSize = nSeqTokens;
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, batchSize);
        var headDim = opts.getHeadDim();
        if headDim <= 0 {
        return null, fmt.Errorf("nemotronh: invalid attention head dimension %d", headDim);
    }
        var query = a.Query.Forward(ctx, hiddenStates);
        if query.Dim(0)%headDim != 0 {
        return null, fmt.Errorf("nemotronh: query dim %d not divisible by head dim %d", query.Dim(0), headDim);
    }
        var numHeads = query.Dim(0) / headDim;
        query = query.Reshape(ctx, headDim, numHeads, batchSize);
        var key = a.Key.Forward(ctx, hiddenStates);
        if key.Dim(0)%headDim != 0 {
        return null, fmt.Errorf("nemotronh: key dim %d not divisible by head dim %d", key.Dim(0), headDim);
    }
        var numKVHeads = key.Dim(0) / headDim;
        key = key.Reshape(ctx, headDim, numKVHeads, batchSize);
        var value = a.Value.Forward(ctx, hiddenStates);
        if value.Dim(0)%headDim != 0 {
        return null, fmt.Errorf("nemotronh: value dim %d not divisible by head dim %d", value.Dim(0), headDim);
    }
        if value.Dim(0)/headDim != numKVHeads {
        return null, fmt.Errorf("nemotronh: key heads %d and value heads %d do not match", numKVHeads, value.Dim(0)/headDim);
    }
        value = value.Reshape(ctx, headDim, numKVHeads, batchSize);
        var scale = opts.attentionScale;
        if scale == 0 {
        scale = 1.0 / math.Sqrt(double(headDim));
    }
        var attention = nn.Attention(ctx, query, key, value, scale, cache);
        attention = attention.Reshape(ctx, headDim*numHeads, batchSize);
        return a.Output.Forward(ctx, attention), null;
    }
}
