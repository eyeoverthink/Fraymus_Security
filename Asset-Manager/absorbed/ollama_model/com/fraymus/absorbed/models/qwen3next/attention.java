package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class attention {
        "errors";
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );
        var ErrUnsupportedBatchLayout = errors.New("qwen3next: unsupported batch layout");

    public static class FullAttention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *FullAttention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var hiddenDim = hiddenStates.Dim(0);
        var batchSize = hiddenStates.Dim(1);
        var nSeqs = hiddenStates.Dim(2) // 0 if 2D tensor;
        if cache != null && cache.IsSupportedForBatch() {
        var seqTokens = cache.seqTokens();
        var seqs = cache.numSeqs();
        if seqTokens > 0 && seqs > 0 {
        if nSeqs > 0 {
        if batchSize != seqTokens || nSeqs != seqs {
        return null, ErrUnsupportedBatchLayout;
    }
        hiddenStates = hiddenStates.Reshape(ctx, hiddenDim, seqTokens*seqs);
        batchSize = seqTokens * seqs;
        } else if batchSize != seqTokens*seqs {
        return null, ErrUnsupportedBatchLayout;
    }
    }
    }
        var headDim = opts.headDim();
        var numHeads = opts.numHeads;
        var qFull = sa.Query.Forward(ctx, hiddenStates);
        qFull = qFull.Reshape(ctx, headDim*2, numHeads, batchSize);
        var query = qFull.Slice(ctx, 0, 0, headDim, 1);
        var gate = qFull.Slice(ctx, 0, headDim, headDim*2, 1);
        query = query.Contiguous(ctx, headDim, numHeads, batchSize);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        var numKVHeads = key.Dim(0) / headDim;
        key = key.Reshape(ctx, headDim, numKVHeads, batchSize);
        value = value.Reshape(ctx, headDim, numKVHeads, batchSize);
        query = sa.QueryNorm.Forward(ctx, query, opts.eps);
        key = sa.KeyNorm.Forward(ctx, key, opts.eps);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var scale = opts.attentionScale;
        if scale == 0 {
        scale = 1.0 / math.Sqrt(double(headDim));
    }
        var attention = nn.Attention(ctx, query, key, value, scale, cache);
        attention = attention.Reshape(ctx, headDim*numHeads, batchSize);
        gate = gate.Contiguous(ctx, headDim*numHeads, batchSize);
        var gateSigmoid = gate.Sigmoid(ctx);
        attention = attention.Mul(ctx, gateSigmoid);
        return sa.Output.Forward(ctx, attention), null;
    }
}
