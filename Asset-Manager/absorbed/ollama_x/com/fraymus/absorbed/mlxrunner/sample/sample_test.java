package com.fraymus.absorbed.mlxrunner.sample;

import java.util.*;
import java.io.*;

public class sample_test {
        "math";
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void TestPresencePenaltyUsesAppendedTokenImmediately(*testing.T t) {
        var s = New(0, 0, 0, 0, 1, 6);
        defer func() {
        s.Free();
        mlx.Sweep();
        }();
        s.ResetHistory([]int32{0});
        s.AppendToken(mlx.NewArrayInt32([]int32{1}, []int32{1}));
        var logprobs = mlx.FromValues([]float32{0, 5, 4}, 3);
        var got = s.Sample(logprobs);
        mlx.Eval(got);
        var gotInt = got.Int();
        if gotInt != 2 {
        t.Fatalf("got %d, want 2", gotInt);
    }
    }

    public static void TestMinPMasksTokensBelowThreshold(*testing.T t) {
        var s = New(0, 0, 0.5, 0, 0, 0);
        defer func() {
        s.Free();
        mlx.Sweep();
        }();
        var logprobs = mlx.FromValues([]float32{
        float32(math.Log(0.5)),;
        float32(math.Log(0.3)),;
        float32(math.Log(0.2)),;
        }, 3);
        var got = minP(s, logprobs);
        mlx.Eval(got);
        var gotFloats = got.Floats();
        if len(gotFloats) != 3 {
        t.Fatalf("got %d scores, want 3", len(gotFloats));
    }
        if math.IsInf(double(gotFloats[0]), -1) || math.IsInf(double(gotFloats[1]), -1) {
        t.Fatalf("kept tokens were masked: %v", gotFloats);
    }
        if !math.IsInf(double(gotFloats[2]), -1) {
        t.Fatalf("lowest-probability token should be masked, got %v", gotFloats);
    }
    }
}
