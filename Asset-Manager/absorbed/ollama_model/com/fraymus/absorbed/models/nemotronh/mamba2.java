package com.fraymus.absorbed.models.nemotronh;

import java.util.*;
import java.io.*;

public class mamba2 {
        "log/slog";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class convKernel {
        public ml.Tensor Weight;
    }

    public static class Mamba2 {
        public *nn.Linear SSMIn;
        public *convKernel SSMConv1D;
        public ml.Tensor SSMConv1DB;
        public ml.Tensor SSMDtB;
        public ml.Tensor SSMA;
        public ml.Tensor SSMD;
        public *nn.RMSNorm SSMNorm;
        public *nn.Linear SSMOut;
        public int Layer;
    }
        func (m *Mamba2) Forward(ctx ml.Context, hiddenStates ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var layer = m.Layer;
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
        var nSeqs = 1;
        var dConv = opts.ssmDConv;
        var dInner = opts.ssmDInner;
        var dState = opts.ssmDState;
        var nHead = opts.ssmNHead;
        var headDim = dInner / nHead;
        var nGroup = opts.ssmNGroup;
        var zxBCdt = m.SSMIn.Forward(ctx, hiddenStates);
        var z = zxBCdt.Slice(ctx, 0, 0, dInner, 1);
        z = z.Reshape(ctx, headDim, nHead, nSeqTokens, nSeqs);
        var xBCSize = dInner + 2*nGroup*dState;
        var xBC = zxBCdt.Slice(ctx, 0, dInner, dInner+xBCSize, 1);
        if nSeqTokens == 1 {
        xBC = xBC.Reshape(ctx, xBCSize, 1, nSeqs);
    }
        var dt = zxBCdt.Slice(ctx, 0, 2*dInner+2*nGroup*dState, 2*dInner+2*nGroup*dState+nHead, 1);
        if nSeqTokens == 1 {
        dt = dt.Reshape(ctx, nHead, 1, nSeqs);
        } else {
        dt = dt.Contiguous(ctx, nHead, nSeqTokens, nSeqs);
    }
        var convStates, err = cache.ConvState(ctx, layer);
        if err != null {
        slog.Warn("nemotronh: failed to get conv state, using zeros", "layer", layer, "error", err);
        convStates = ctx.Input().Zeros(ml.DTypeF32, dConv-1, xBCSize, nSeqs);
    }
        convStates = convStates.Reshape(ctx, dConv-1, xBCSize, nSeqs);
        var xBCT ml.Tensor;
        if nSeqTokens == 1 {
        xBCT = xBC.Reshape(ctx, 1, xBCSize, nSeqs);
        } else {
        xBCT = xBC.Permute(ctx, 1, 0, 2, 3);
    }
        var convInput = convStates.Concat(ctx, xBCT, 0);
        var lastConvStates = convInput.Slice(ctx, 0, nSeqTokens, nSeqTokens+dConv-1, 1);
        cache.UpdateConvState(ctx, layer, lastConvStates);
        xBC = convInput.SSMConv(ctx, m.SSMConv1D.Weight);
        if m.SSMConv1DB != null {
        xBC = xBC.Add(ctx, m.SSMConv1DB);
    }
        xBC = xBC.SILU(ctx);
        var x = xBC.Slice(ctx, 0, 0, dInner, 1);
        x = x.Reshape(ctx, headDim, nHead, nSeqTokens, nSeqs);
        var B = xBC.Slice(ctx, 0, dInner, dInner+nGroup*dState, 1);
        B = B.Reshape(ctx, dState, nGroup, nSeqTokens, nSeqs);
        var C = xBC.Slice(ctx, 0, dInner+nGroup*dState, dInner+2*nGroup*dState, 1);
        C = C.Reshape(ctx, dState, nGroup, nSeqTokens, nSeqs);
        dt = dt.Add(ctx, m.SSMDtB);
        var state, err = cache.SSMState(ctx, layer, dState, headDim, nHead);
        if err != null {
        slog.Warn("nemotronh: failed to get SSM state, using zeros", "layer", layer, "error", err);
        state = ctx.Input().Zeros(ml.DTypeF32, dState, headDim, nHead, nSeqs);
    }
        var ySsm = state.SSMScan(ctx, x, dt, m.SSMA, B, C, cache.slotsTensor());
        var yElems = headDim * nHead * nSeqTokens * nSeqs;
        var y = ySsm.View(ctx, 0, yElems).Reshape(ctx, headDim, nHead, nSeqTokens, nSeqs);
        var stateOffsetBytes = yElems * x.Stride(0);
        var stateElems = dState * headDim * nHead * nSeqs;
        var newState = ySsm.View(ctx, stateOffsetBytes, stateElems);
        newState = newState.Reshape(ctx, dState, headDim, nHead, nSeqs);
        cache.UpdateSSMState(ctx, layer, newState);
        if m.SSMD != null {
        var xD = x.Mul(ctx, m.SSMD);
        y = y.Add(ctx, xD);
    }
        y = z.SILU(ctx, y);
        if m.SSMNorm != null {
        var innerPerGroup = dInner / nGroup;
        y = y.Reshape(ctx, innerPerGroup, nGroup, nSeqTokens, nSeqs);
        y = m.SSMNorm.Forward(ctx, y, opts.eps);
    }
        y = y.Reshape(ctx, dInner, nSeqTokens, nSeqs);
        var out = m.SSMOut.Forward(ctx, y);
        return out.Reshape(ctx, out.Dim(0), nSeqTokens*nSeqs), null;
    }
}
