package com.fraymus.absorbed.models.nn;

import java.util.*;
import java.io.*;

public class nn_test {
        "math";
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void skipIfNoMLX(*testing.T t) {
        t.Helper();
        var if err = mlx.CheckInit(); err != null {
        t.Skipf("MLX not available: %v", err);
    }
    }

    public static boolean approxEqual(float32 tol) {
        return float32(math.Abs(double(a-b))) < tol;
    }

    public static void TestLayerNormNoBias(*testing.T t) {
        skipIfNoMLX(t);
        var x = mlx.FromValues([]float32{1, 2, 3, 4}, 1, 4);
        var weight = mlx.FromValues([]float32{1, 1, 1, 1}, 4);
        mlx.Eval(x, weight);
        var ln = &LayerNorm{Weight: weight, Eps: 1e-5}
        var out = ln.Forward(x);
        mlx.Eval(out);
        var data = out.Floats();
        if len(data) != 4 {
        t.Fatalf("expected 4 values, got %d", len(data));
    }
        var mean = float32(2.5);
        var variance = float32(1.25);
        var std = float32(math.Sqrt(double(variance + 1e-5)));
        var for i, v = range []float32{1, 2, 3, 4} {
        var expected = (v - mean) / std;
        if !approxEqual(data[i], expected, 1e-4) {
        t.Errorf("index %d: expected %.6f, got %.6f", i, expected, data[i]);
    }
    }
    }

    public static void TestLayerNormWithBias(*testing.T t) {
        skipIfNoMLX(t);
        var x = mlx.FromValues([]float32{1, 2, 3, 4}, 1, 4);
        var weight = mlx.FromValues([]float32{2, 2, 2, 2}, 4);
        var bias = mlx.FromValues([]float32{10, 20, 30, 40}, 4);
        mlx.Eval(x, weight, bias);
        var ln = &LayerNorm{Weight: weight, Bias: bias, Eps: 1e-5}
        var out = ln.Forward(x);
        mlx.Eval(out);
        var data = out.Floats();
        if len(data) != 4 {
        t.Fatalf("expected 4 values, got %d", len(data));
    }
        var mean = float32(2.5);
        var variance = float32(1.25);
        var std = float32(math.Sqrt(double(variance + 1e-5)));
        var biases = []float32{10, 20, 30, 40}
        var for i, v = range []float32{1, 2, 3, 4} {
        var expected = ((v-mean)/std)*2 + biases[i];
        if !approxEqual(data[i], expected, 1e-4) {
        t.Errorf("index %d: expected %.6f, got %.6f", i, expected, data[i]);
    }
    }
    }

    public static void TestLayerNormBatched(*testing.T t) {
        skipIfNoMLX(t);
        var x = mlx.FromValues([]float32{
        1, 2, 3,;
        10, 20, 30,;
        }, 2, 3);
        var weight = mlx.FromValues([]float32{1, 1, 1}, 3);
        mlx.Eval(x, weight);
        var ln = &LayerNorm{Weight: weight, Eps: 1e-5}
        var out = ln.Forward(x);
        mlx.Eval(out);
        var data = out.Floats();
        if len(data) != 6 {
        t.Fatalf("expected 6 values, got %d", len(data));
    }
        var for i = range 3 {
        if !approxEqual(data[i], data[i+3], 1e-4) {
        t.Errorf("row 0 elem %d (%.6f) != row 1 elem %d (%.6f); expected identical normalized values",;
        i, data[i], i, data[i+3]);
    }
    }
        var sum = data[0] + data[1] + data[2];
        if !approxEqual(sum, 0, 1e-4) {
        t.Errorf("normalized row sum should be ~0, got %.6f", sum);
    }
    }

    public static void TestLayerNormDefaultEps(*testing.T t) {
        skipIfNoMLX(t);
        var x = mlx.FromValues([]float32{1, 2, 3, 4}, 1, 4);
        var weight = mlx.FromValues([]float32{1, 1, 1, 1}, 4);
        mlx.Eval(x, weight);
        var ln0 = &LayerNorm{Weight: weight, Eps: 0}
        var out0 = ln0.Forward(x);
        mlx.Eval(out0);
        var lnExplicit = &LayerNorm{Weight: weight, Eps: 1e-5}
        var outExplicit = lnExplicit.Forward(x);
        mlx.Eval(outExplicit);
        var d0 = out0.Floats();
        var dE = outExplicit.Floats();
        var for i = range d0 {
        if !approxEqual(d0[i], dE[i], 1e-6) {
        t.Errorf("index %d: Eps=0 gave %.6f, Eps=1e-5 gave %.6f", i, d0[i], dE[i]);
    }
    }
    }

    public static void TestQuantizedLinearMXFP4MatchesDequantizedWeight(*testing.T t) {
        skipIfNoMLX(t);
        var weightVals = make([]float32, 3*32);
        var for i = range weightVals {
        weightVals[i] = float32((i%11)-5) / 7;
    }
        var inputVals = make([]float32, 2*32);
        var for i = range inputVals {
        inputVals[i] = float32((i%7)-3) / 5;
    }
        var weight = mlx.FromValues(weightVals, 3, 32).AsType(mlx.DTypeBFloat16);
        var input = mlx.FromValues(inputVals, 2, 32).AsType(mlx.DTypeBFloat16);
        mlx.Eval(weight, input);
        var ql = NewQuantizedLinear(weight, null, 32, 4, "mxfp4");
        if ql.QBiases != null {
        t.Fatalf("mxfp4 qbiases = %v, want null", ql.QBiases);
    }
        var dequantizedWeight = mlx.Dequantize(ql.Weight, ql.Scales, ql.QBiases, 32, 4, "mxfp4");
        mlx.Eval(dequantizedWeight);
        var qOut = ql.Forward(input);
        var dOut = NewLinear(dequantizedWeight, null).Forward(input);
        mlx.Eval(qOut, dOut);
        var got = qOut.Floats();
        var want = dOut.Floats();
        if len(got) != len(want) {
        t.Fatalf("output length = %d, want %d", len(got), len(want));
    }
        var for i = range got {
        if !approxEqual(got[i], want[i], 1e-3) {
        t.Fatalf("output[%d] = %.6f, want %.6f", i, got[i], want[i]);
    }
    }
    }
}
