package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class shortconv {
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class shortConvKernel {
        public ml.Tensor Weight;
    }

    public static class ShortConv {
        public *shortConvKernel Conv;
        public *nn.Linear InProj;
        public *nn.Linear OutProj;
    }
        func (sc *ShortConv) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ ml.Tensor, cache *HybridCache, layer int, opts *Options) ml.Tensor {
        var nSeqs = cache.numSeqs();
        var seqTokens = cache.seqTokens();
        var hiddenSize = hiddenStates.Dim(0);
        if nSeqs <= 0 || seqTokens <= 0 || hiddenStates.Dim(1) != nSeqs*seqTokens {
        panic("lfm2: unsupported batch layout for shortconv");
    }
        var bcx = sc.InProj.Forward(ctx, hiddenStates).Reshape(ctx, 3*hiddenSize, seqTokens, nSeqs);
        var elementSize = bcx.Stride(0);
        var b = bcx.View(ctx, 0*hiddenSize*elementSize, hiddenSize, bcx.Stride(1), seqTokens, bcx.Stride(2), nSeqs);
        var c = bcx.View(ctx, 1*hiddenSize*elementSize, hiddenSize, bcx.Stride(1), seqTokens, bcx.Stride(2), nSeqs);
        var x = bcx.View(ctx, 2*hiddenSize*elementSize, hiddenSize, bcx.Stride(1), seqTokens, bcx.Stride(2), nSeqs);
        var bx = b.Mul(ctx, x).Permute(ctx, 1, 0, 2, 3);
        var state, err = cache.ConvState(ctx, layer);
        if err != null {
        panic("lfm2: failed to get conv state: " + err.Error());
    }
        var sx = state.Concat(ctx, bx, 0);
        var convOut = sx.SSMConv(ctx, sc.Conv.Weight);
        var y = c.Mul(ctx, convOut);
        var dConv = sx.Dim(0) - seqTokens;
        cache.UpdateConvState(ctx, layer, sx.Slice(ctx, 0, sx.Dim(0)-dConv, sx.Dim(0), 1));
        return sc.OutProj.Forward(ctx, y.Reshape(ctx, hiddenSize, seqTokens*nSeqs));
    }
}
