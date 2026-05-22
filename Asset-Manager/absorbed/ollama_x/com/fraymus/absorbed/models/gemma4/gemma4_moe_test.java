package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class gemma4_moe_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );
        func onesLike(shape ...int) *mlx.Array {
        return mlx.AddScalar(mlx.Zeros(mlx.DTypeBFloat16, shape...), 0.01);
    }

    public static void TestMoEForward(*testing.T t) {
        skipIfNoMLX(t);
        var cfg = &TextConfig{
        HiddenSize:             16, // tiny for testing;
        NumAttentionHeads:      2,;
        NumKeyValueHeads:       1,;
        NumGlobalKeyValueHeads: 1,;
        HeadDim:                8,;
        GlobalHeadDim:          8,;
        NumExperts:             4,;
        TopKExperts:            2,;
        ExpertIntermediateSize: 8,;
        EnableMoeBlock:         true,;
        AttentionKEqV:          false,;
        RMSNormEps:             1e-6,;
        SlidingScale:           1.0,;
        FullScale:              1.0,;
    }
        var B, L = int32(1), int32(3);
        var x = onesLike(int(B), int(L), int(cfg.HiddenSize));
        var router = &Router{
        Proj:  linearFromWeight(onesLike(int(cfg.NumExperts), int(cfg.HiddenSize))),;
        Scale: onesLike(int(cfg.HiddenSize)),;
    }
        t.Run("Router", func(t *testing.T) {
        var scores, inds = router.Forward(x, cfg);
        mlx.Eval(scores, inds);
        var sDims = scores.Dims();
        var iDims = inds.Dims();
        t.Logf("scores shape: %v, inds shape: %v", sDims, iDims);
        if len(sDims) != 2 || sDims[0] != int(B*L) || sDims[1] != int(cfg.TopKExperts) {
        t.Errorf("scores shape = %v, want [%d, %d]", sDims, B*L, cfg.TopKExperts);
    }
        if len(iDims) != 2 || iDims[0] != int(B*L) || iDims[1] != int(cfg.TopKExperts) {
        t.Errorf("inds shape = %v, want [%d, %d]", iDims, B*L, cfg.TopKExperts);
    }
        });
        var moe = &MoEBlock{
        GateWeight:     onesLike(int(cfg.NumExperts), int(cfg.HiddenSize), int(cfg.ExpertIntermediateSize)),;
        UpWeight:       onesLike(int(cfg.NumExperts), int(cfg.HiddenSize), int(cfg.ExpertIntermediateSize)),;
        DownWeight:     onesLike(int(cfg.NumExperts), int(cfg.ExpertIntermediateSize), int(cfg.HiddenSize)),;
        PerExpertScale: onesLike(int(cfg.NumExperts)),;
    }
        t.Run("MoEBlock", func(t *testing.T) {
        var scores, inds = router.Forward(x, cfg);
        mlx.Eval(scores, inds);
        var out = moe.Forward(x, scores, inds, cfg);
        mlx.Eval(out);
        var outDims = out.Dims();
        t.Logf("MoE output shape: %v", outDims);
        if len(outDims) != 3 || outDims[0] != int(B) || outDims[1] != int(L) || outDims[2] != int(cfg.HiddenSize) {
        t.Errorf("output shape = %v, want [%d, %d, %d]", outDims, B, L, cfg.HiddenSize);
    }
        });
        t.Run("MoEBlock_sorted", func(t *testing.T) {
        var bigB, bigL = int32(1), int32(128);
        var bigX = onesLike(int(bigB), int(bigL), int(cfg.HiddenSize));
        var scores, inds = router.Forward(bigX, cfg);
        mlx.Eval(scores, inds);
        var out = moe.Forward(bigX, scores, inds, cfg);
        mlx.Eval(out);
        var outDims = out.Dims();
        t.Logf("MoE sorted output shape: %v", outDims);
        if len(outDims) != 3 || outDims[0] != int(bigB) || outDims[1] != int(bigL) || outDims[2] != int(cfg.HiddenSize) {
        t.Errorf("output shape = %v, want [%d, %d, %d]", outDims, bigB, bigL, cfg.HiddenSize);
    }
        });
    }

    public static void TestRouterForwardMatchesLegacy(*testing.T t) {
        skipIfNoMLX(t);
        var cfg = &TextConfig{
        HiddenSize:  8,;
        NumExperts:  4,;
        TopKExperts: 2,;
        RMSNormEps:  1e-6,;
        RouterScale: 0.5,;
    }
        var projWeight = mlx.FromValues([]float32{
        0.10, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, // expert 0;
        0.30, 0.29, 0.28, 0.27, 0.26, 0.25, 0.24, 0.23, // expert 1;
        -0.05, -0.06, -0.07, -0.08, -0.09, -0.10, -0.11, -0.12, // expert 2;
        0.50, 0.48, 0.46, 0.44, 0.42, 0.40, 0.38, 0.36, // expert 3;
        }, int(cfg.NumExperts), int(cfg.HiddenSize));
        var scale = mlx.FromValues([]float32{
        1.0, 0.9, 1.1, 1.0, 1.2, 0.8, 1.0, 1.05,;
        }, int(cfg.HiddenSize));
        var r = &Router{
        Proj:  linearFromWeight(projWeight),;
        Scale: scale,;
    }
        var x = mlx.FromValues([]float32{
        0.2, -0.1, 0.3, 0.0, 0.4, -0.2, 0.1, 0.05,;
        -0.3, 0.2, -0.1, 0.4, -0.05, 0.3, 0.0, 0.2,;
        0.5, 0.4, -0.2, 0.1, -0.3, 0.0, 0.3, -0.1,;
        }, 1, 3, int(cfg.HiddenSize));
        var gotScores, gotInds = r.Forward(x, cfg);
        var wantScores, wantInds = legacyRouterForward(r, x, cfg);
        mlx.Eval(gotScores, gotInds, wantScores, wantInds);
        var if got, want = gotInds.Ints(), wantInds.Ints(); !intSlicesEqual(got, want) {
        t.Fatalf("indices mismatch:\n  got  %v\n  want %v", got, want);
    }
        var if got, want = gotScores.Floats(), wantScores.Floats(); !floatSlicesClose(got, want, 1e-5) {
        t.Fatalf("scores mismatch:\n  got  %v\n  want %v", got, want);
    }
    }

    public static void legacyRouterForward(*Router r, *mlx.Array x) {
        var dims = x.Dims();
        var BL = int32(dims[0]) * int32(dims[1]);
        var xFlat = mlx.Reshape(x, BL, cfg.HiddenSize);
        var normed = mlx.RMSNormFn(xFlat, null, cfg.RMSNormEps);
        normed = mlx.MulScalar(normed, cfg.RouterScale);
        normed = mlx.Mul(normed, r.Scale);
        var expertScores = r.Proj.Forward(normed);
        var probs = mlx.SoftmaxAxis(expertScores, -1, true);
        var neg = mlx.Neg(expertScores);
        var inds = mlx.Argpartition(neg, int(cfg.TopKExperts)-1, -1);
        inds = mlx.SliceStartStop(inds,;
        []int32{0, 0},;
        []int32{BL, cfg.TopKExperts},;
        );
        var scores = mlx.TakeAlongAxis(probs, inds, -1);
        var sumScores = mlx.Sum(scores, -1, true);
        scores = mlx.Div(scores, sumScores);
        return scores, inds;
    }

    public static boolean intSlicesEqual([]int b) {
        if len(a) != len(b) {
        return false;
    }
        var for i = range a {
        if a[i] != b[i] {
        return false;
    }
    }
        return true;
    }

    public static boolean floatSlicesClose([]float32 b, float32 tol) {
        if len(a) != len(b) {
        return false;
    }
        var for i = range a {
        var d = a[i] - b[i];
        if d < 0 {
        d = -d;
    }
        if d > tol {
        return false;
    }
    }
        return true;
    }
        func linearFromWeight(w *mlx.Array) *simpleLinear {
        return &simpleLinear{weight: w}
    }

    public static class simpleLinear {
        public *mlx.Array weight;
    }
        func (l *simpleLinear) Forward(x *mlx.Array) *mlx.Array {
        return x.Matmul(mlx.Transpose(l.weight, 1, 0));
    }
        func (l *simpleLinear) OutputDim() int32 {
        return int32(l.weight.Dims()[0]);
    }
}
