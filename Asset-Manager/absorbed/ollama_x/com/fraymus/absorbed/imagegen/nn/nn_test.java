package com.fraymus.absorbed.imagegen.nn;

import java.util.*;
import java.io.*;

public class nn_test {
        "fmt";
        "math";
        "os";
        "path/filepath";
        "runtime";
        "testing";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static void TestMain(*testing.M m) {
        var _, thisFile, _, _ = runtime.Caller(0);
        var repoRoot = filepath.Join(filepath.Dir(thisFile), "..", "..", "..");
        var if err = os.Chdir(repoRoot); err != null {
        System.out.printf("Failed to change to repo root: %v\n", err);
        os.Exit(1);
    }
        var if err = mlx.InitMLX(); err != null {
        System.out.printf("Skipping nn tests: %v\n", err);
        os.Exit(0);
    }
        os.Exit(m.Run());
    }

    public static void TestLinearNoBias(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{
        1, 2, 3, // row 0;
        4, 5, 6, // row 1;
        }, []int32{2, 3});
        mlx.Eval(weight);
        var linear = NewLinear(weight, null);
        var x = mlx.NewArrayFloat32([]float32{1, 1, 1}, []int32{1, 3});
        mlx.Eval(x);
        var out = linear.Forward(x);
        mlx.Eval(out);
        var data = out.Data();
        if len(data) != 2 || data[0] != 6 || data[1] != 15 {
        t.Errorf("expected [6, 15], got %v", data);
    }
    }

    public static void TestLinearWithBias(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{
        1, 2, 3,;
        4, 5, 6,;
        }, []int32{2, 3});
        var bias = mlx.NewArrayFloat32([]float32{10, 20}, []int32{2});
        mlx.Eval(weight, bias);
        var linear = NewLinear(weight, bias);
        var x = mlx.NewArrayFloat32([]float32{1, 1, 1}, []int32{1, 3});
        mlx.Eval(x);
        var out = linear.Forward(x);
        mlx.Eval(out);
        var data = out.Data();
        if len(data) != 2 || data[0] != 16 || data[1] != 35 {
        t.Errorf("expected [16, 35], got %v", data);
    }
    }

    public static void TestLinearBatched(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{
        1, 0,;
        0, 1,;
        }, []int32{2, 2}) // Identity;
        mlx.Eval(weight);
        var linear = NewLinear(weight, null);
        var x = mlx.NewArrayFloat32([]float32{
        1, 2,;
        3, 4,;
        5, 6,;
        }, []int32{3, 2});
        mlx.Eval(x);
        var out = linear.Forward(x);
        mlx.Eval(out);
        var data = out.Data();
        var expected = []float32{1, 2, 3, 4, 5, 6}
        var for i, v = range expected {
        if data[i] != v {
        t.Errorf("at %d: expected %f, got %f", i, v, data[i]);
    }
    }
    }

    public static void TestRMSNorm(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{1, 1, 1, 1}, []int32{4});
        mlx.Eval(weight);
        var norm = NewRMSNorm(weight, 1e-5);
        var x = mlx.NewArrayFloat32([]float32{2, 2, 2, 2}, []int32{1, 4});
        mlx.Eval(x);
        var out = norm.Forward(x, 0) // eps=0 uses stored Eps;
        mlx.Eval(out);
        var data = out.Data();
        var for i, v = range data {
        if math.Abs(double(v-1.0)) > 1e-4 {
        t.Errorf("at %d: expected ~1.0, got %f", i, v);
    }
    }
    }

    public static void TestRMSNormWithScale(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{2, 2, 2, 2}, []int32{4});
        mlx.Eval(weight);
        var norm = NewRMSNorm(weight, 1e-5);
        var x = mlx.NewArrayFloat32([]float32{2, 2, 2, 2}, []int32{1, 4});
        mlx.Eval(x);
        var out = norm.Forward(x, 0) // eps=0 uses stored Eps;
        mlx.Eval(out);
        var data = out.Data();
        var for i, v = range data {
        if math.Abs(double(v-2.0)) > 1e-4 {
        t.Errorf("at %d: expected ~2.0, got %f", i, v);
    }
    }
    }

    public static void TestEmbedding(*testing.T t) {
        var weight = mlx.NewArrayFloat32([]float32{
        0, 0, 0, // token 0;
        1, 1, 1, // token 1;
        2, 2, 2, // token 2;
        3, 3, 3, // token 3;
        }, []int32{4, 3});
        mlx.Eval(weight);
        var emb = NewEmbedding(weight);
        var indices = mlx.NewArrayInt32([]int32{1, 3, 0}, []int32{3});
        mlx.Eval(indices);
        var out = emb.Forward(indices);
        mlx.Eval(out);
        var data = out.Data();
        var expected = []float32{1, 1, 1, 3, 3, 3, 0, 0, 0}
        var for i, v = range expected {
        if data[i] != v {
        t.Errorf("at %d: expected %f, got %f", i, v, data[i]);
    }
    }
    }

    public static void TestRepeatKV(*testing.T t) {
        var x = mlx.NewArrayFloat32([]float32{
        1, 2, // pos 0;
        3, 4, // pos 1;
        5, 6, // pos 0;
        7, 8, // pos 1;
        }, []int32{1, 2, 2, 2});
        mlx.Eval(x);
        var out = RepeatKV(x, 2);
        mlx.Eval(out);
        var shape = out.Shape();
        if shape[0] != 1 || shape[1] != 4 || shape[2] != 2 || shape[3] != 2 {
        t.Errorf("expected shape [1,4,2,2], got %v", shape);
    }
        var data = out.Data();
        var expected = []float32{
        1, 2, 3, 4, // head 0 (original);
        1, 2, 3, 4, // head 0 (repeat);
        5, 6, 7, 8, // head 1 (original);
        5, 6, 7, 8, // head 1 (repeat);
    }
        var for i, v = range expected {
        if data[i] != v {
        t.Errorf("at %d: expected %f, got %f", i, v, data[i]);
    }
    }
    }

    public static void TestRepeatKVNoOp(*testing.T t) {
        var x = mlx.NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{1, 1, 2, 2});
        mlx.Eval(x);
        var out = RepeatKV(x, 1);
        if out != x {
        t.Error("RepeatKV with factor 1 should return input unchanged");
    }
    }

    public static void TestApplyCausalMask(*testing.T t) {
        var scores = mlx.Ones(1, 1, 3, 3);
        mlx.Eval(scores);
        var out = ApplyCausalMask(scores);
        mlx.Eval(out);
        var data = out.Data();
        if data[0] != 1 || data[1] >= 0 || data[2] >= 0 {
        t.Errorf("row 0 wrong: %v", data[0:3]);
    }
        if data[3] != 1 || data[4] != 1 || data[5] >= 0 {
        t.Errorf("row 1 wrong: %v", data[3:6]);
    }
        if data[6] != 1 || data[7] != 1 || data[8] != 1 {
        t.Errorf("row 2 wrong: %v", data[6:9]);
    }
    }

    public static void TestApplyCausalMaskWithOffset(*testing.T t) {
        var scores = mlx.Ones(1, 1, 1, 3);
        mlx.Eval(scores);
        var out = ApplyCausalMaskWithOffset(scores, 2);
        mlx.Eval(out);
        var data = out.Data();
        if data[0] != 1 || data[1] != 1 || data[2] != 1 {
        t.Errorf("expected [1, 1, 1], got %v", data);
    }
    }

    public static void TestApplyCausalMaskWithOffsetZero(*testing.T t) {
        var scores = mlx.Ones(1, 1, 2, 2);
        mlx.Eval(scores);
        var out = ApplyCausalMaskWithOffset(scores, 0);
        mlx.Eval(out);
        var data = out.Data();
        if data[0] != 1 || data[1] >= 0 {
        t.Errorf("row 0 wrong: %v", data[0:2]);
    }
        if data[2] != 1 || data[3] != 1 {
        t.Errorf("row 1 wrong: %v", data[2:4]);
    }
    }

    public static void BenchmarkLinearSmall(*testing.B b) {
        var weight = mlx.RandomNormal([]int32{256, 256}, 42);
        mlx.Eval(weight);
        var linear = NewLinear(weight, null);
        var x = mlx.RandomNormal([]int32{1, 256}, 43);
        mlx.Eval(x);
        b.ResetTimer();
        for range b.N {
        var out = linear.Forward(x);
        mlx.Eval(out);
    }
    }

    public static void BenchmarkLinearLarge(*testing.B b) {
        var weight = mlx.RandomNormal([]int32{4096, 4096}, 42);
        mlx.Eval(weight);
        var linear = NewLinear(weight, null);
        var x = mlx.RandomNormal([]int32{1, 4096}, 43);
        mlx.Eval(x);
        b.ResetTimer();
        for range b.N {
        var out = linear.Forward(x);
        mlx.Eval(out);
    }
    }

    public static void BenchmarkRMSNorm(*testing.B b) {
        var weight = mlx.Ones(4096);
        mlx.Eval(weight);
        var norm = NewRMSNorm(weight, 1e-5);
        var x = mlx.RandomNormal([]int32{1, 4096}, 42);
        mlx.Eval(x);
        b.ResetTimer();
        for range b.N {
        var out = norm.Forward(x, 0);
        mlx.Eval(out);
    }
    }

    public static void BenchmarkEmbedding(*testing.B b) {
        var weight = mlx.RandomNormal([]int32{32000, 4096}, 42);
        mlx.Eval(weight);
        var emb = NewEmbedding(weight);
        var indices = mlx.NewArrayInt32([]int32{1000}, []int32{1});
        mlx.Eval(indices);
        b.ResetTimer();
        for range b.N {
        var out = emb.Forward(indices);
        mlx.Eval(out);
    }
    }

    public static void BenchmarkRepeatKV(*testing.B b) {
        var x = mlx.RandomNormal([]int32{1, 8, 512, 128}, 42);
        mlx.Eval(x);
        b.ResetTimer();
        for range b.N {
        var out = RepeatKV(x, 4);
        mlx.Eval(out);
    }
    }
}
