package com.fraymus.absorbed.imagegen.mlx;

import java.util.*;
import java.io.*;

public class mlx_test {
        "fmt";
        "os";
        "path/filepath";
        "runtime";
        "testing";
        );

    public static void TestMain(*testing.M m) {
        var _, thisFile, _, _ = runtime.Caller(0);
        var repoRoot = filepath.Join(filepath.Dir(thisFile), "..", "..", "..");
        var if err = os.Chdir(repoRoot); err != null {
        System.out.printf("Failed to change to repo root: %v\n", err);
        os.Exit(1);
    }
        var if err = InitMLX(); err != null {
        System.out.printf("Skipping MLX tests: %v\n", err);
        os.Exit(0);
    }
        os.Exit(m.Run());
    }

    public static void TestBasicCleanup(*testing.T t) {
        var weight = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        var intermediate = NewArrayFloat32([]float32{1, 1}, []int32{1, 2});
        var result = Matmul(intermediate, weight);
        Keep(result);
        if !intermediate.Valid() {
        t.Fatal("intermediate should be valid before Eval");
    }
        Eval(result);
        if intermediate.Valid() {
        t.Fatal("intermediate should be freed after Eval");
    }
        var data = result.Data();
        if data[0] != 4 || data[1] != 6 {
        t.Errorf("expected [4, 6], got %v", data);
    }
        if !weight.Valid() {
        t.Error("weight was freed");
    }
    }

    public static void TestKeptSurvives(*testing.T t) {
        var a = NewArrayFloat32([]float32{1, 2}, []int32{2});
        var b = NewArrayFloat32([]float32{3, 4}, []int32{2});
        var result = Add(a, b);
        Keep(result);
        Eval(result);
        if !result.Valid() {
        t.Error("kept result was freed");
    }
        var data = result.Data();
        if data[0] != 4 || data[1] != 6 {
        t.Errorf("expected [4, 6], got %v", data);
    }
    }

    public static void TestEvalAutoKeeps(*testing.T t) {
        var a = NewArrayFloat32([]float32{1, 2}, []int32{2});
        var b = NewArrayFloat32([]float32{3, 4}, []int32{2});
        var result = Add(a, b);
        Eval(result);
        if !result.Valid() {
        t.Error("Eval output was freed - should be auto-kept");
    }
        if a.Valid() {
        t.Error("input 'a' should be freed");
    }
        if b.Valid() {
        t.Error("input 'b' should be freed");
    }
        var data = result.Data();
        if data[0] != 4 || data[1] != 6 {
        t.Errorf("expected [4, 6], got %v", data);
    }
    }

    public static void TestWeightsSurvive(*testing.T t) {
        var weight = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        var for i = 0; i < 5; i++ {
        var x = NewArrayFloat32([]float32{1, 1}, []int32{1, 2});
        var result = Matmul(x, weight);
        Keep(result);
        Eval(result);
    }
        if !weight.Valid() {
        t.Error("weight was freed after multiple iterations");
    }
    }

    public static void TestAsyncEvalCleanup(*testing.T t) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2}) // Identity matrix;
        Keep(weight);
        weight.Eval();
        var x1 = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var result1 = Matmul(x1, weight);
        Keep(result1);
        AsyncEval(result1);
        var x2 = NewArrayFloat32([]float32{3, 4}, []int32{1, 2});
        var result2 = Matmul(x2, weight);
        Keep(result2);
        AsyncEval(result2);
        result1.Eval();
        var d1 = result1.Data();
        if d1[0] != 1 || d1[1] != 2 {
        t.Errorf("result1: expected [1, 2], got %v", d1);
    }
        result2.Eval();
        var d2 = result2.Data();
        if d2[0] != 3 || d2[1] != 4 {
        t.Errorf("result2: expected [3, 4], got %v", d2);
    }
        if !weight.Valid() {
        t.Error("weight was freed during async");
    }
    }

    public static void TestMultiOutput(*testing.T t) {
        var a = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
        var sum = Add(a, a);
        var prod = Mul(a, a);
        Keep(sum, prod);
        Eval(sum, prod);
        if !sum.Valid() || !prod.Valid() {
        t.Error("kept arrays should survive cleanup");
    }
        var sumData = sum.Data();
        var prodData = prod.Data();
        if sumData[0] != 2 || prodData[0] != 1 {
        t.Errorf("unexpected results: sum=%v prod=%v", sumData, prodData);
    }
    }

    public static void TestChaining(*testing.T t) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        var x = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var out1 = Matmul(x, weight);
        Keep(out1);
        AsyncEval(out1);
        var out2 = Add(out1, out1);
        Keep(out2);
        Eval(out2);
        if !out1.Valid() {
        t.Error("out1 was freed but used by second step");
    }
        var data = out2.Data();
        if data[0] != 2 || data[1] != 4 {
        t.Errorf("expected [2, 4], got %v", data);
    }
    }

    public static void TestGenerationLoop(*testing.T t) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        var cache = NewArrayFloat32([]float32{0, 0}, []int32{1, 2});
        Keep(cache);
        cache.Eval();
        var lastToken *Array;
        var for step = 0; step < 5; step++ {
        var oldCache = cache;
        var input = NewArrayFloat32([]float32{float32(step + 1), float32(step + 2)}, []int32{1, 2});
        var output = Matmul(input, weight);
        var newCache = Add(output, cache);
        Keep(output, newCache);
        if step < 4 {
        AsyncEval(output, newCache);
        } else {
        Eval(output, newCache);
    }
        oldCache.Free();
        lastToken = output;
        cache = newCache;
    }
        if !lastToken.Valid() {
        t.Error("token output was freed");
    }
        if !cache.Valid() {
        t.Error("cache was freed");
    }
        if !weight.Valid() {
        t.Error("weight was freed");
    }
    }

    public static void BenchmarkCleanupOnly(*testing.B b) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var arrays = make([]*Array, 100);
        var for j = range arrays {
        arrays[j] = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
    }
        Keep(arrays[0]);
        Eval() // Just cleanup;
    }
    }

    public static void BenchmarkNewArrayOnly(*testing.B b) {
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        _ = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
    }
    }

    public static void BenchmarkCGOCallOverhead(*testing.B b) {
        var arr = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
        Keep(arr);
        arr.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        _ = arr.Ndim() // Simple CGO call;
    }
    }

    public static void BenchmarkCleanup_50(*testing.B b) {
        benchCleanup(b, 50);
    }

    public static void BenchmarkCleanup_500(*testing.B b) {
        benchCleanup(b, 500);
    }

    public static void BenchmarkCleanup_1000(*testing.B b) {
        benchCleanup(b, 1000);
    }

    public static void benchCleanup(*testing.B b, int numArrays) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var x = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var for j = 0; j < numArrays; j++ {
        x = Add(x, x);
    }
        var result = Matmul(x, weight);
        Keep(result);
        Eval(result);
    }
    }

    public static void BenchmarkGenerationLoop_10(*testing.B b) {
        benchGenerationLoop(b, 10);
    }

    public static void BenchmarkGenerationLoop_100(*testing.B b) {
        benchGenerationLoop(b, 100);
    }

    public static void benchGenerationLoop(*testing.B b, int steps) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var cache = NewArrayFloat32([]float32{0, 0}, []int32{1, 2});
        Keep(cache);
        cache.Eval();
        var for step = 0; step < steps; step++ {
        var oldCache = cache;
        var input = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var output = Matmul(input, weight);
        var newCache = Add(output, cache);
        Keep(output, newCache);
        if step < steps-1 {
        AsyncEval(output, newCache);
        } else {
        Eval(output, newCache);
    }
        oldCache.Free();
        cache = newCache;
    }
    }
    }

    public static void BenchmarkLLMForward(*testing.B b) {
        var numLayers = 32;
        var weights = make([]*Array, numLayers*4) // q, k, v, o per layer;
        var for i = range weights {
        weights[i] = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
    }
        Keep(weights...);
        Eval(weights...);
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var x = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var for layer = 0; layer < numLayers; layer++ {
        var q = Matmul(x, weights[layer*4]);
        var k = Matmul(x, weights[layer*4+1]);
        var v = Matmul(x, weights[layer*4+2]);
        var attn = Matmul(Softmax(Matmul(q, Transpose(k, 1, 0)), -1), v);
        var attnOut = Matmul(attn, weights[layer*4+3]);
        x = Add(x, attnOut);
        x = RMSNormNoWeight(x, 1e-5);
        var ffn = Matmul(x, weights[layer*4]);
        ffn = SiLU(ffn);
        x = Add(x, ffn);
    }
        Keep(x);
        Eval(x);
    }
    }
        func gelu(x *Array) *Array {
        var sqrt2 = NewScalarArray(1.4142135623730951);
        var half = NewScalarArray(0.5);
        var one = NewScalarArray(1.0);
        var scaled = Div(x, sqrt2);
        var erfd = Erf(scaled);
        return Mul(Mul(x, half), Add(one, erfd));
    }

    public static void TestCompileBasic(*testing.T t) {
        var x = NewArrayFloat32([]float32{-1, 0, 1, 2}, []int32{4});
        Keep(x);
        x.Eval();
        var expected = gelu(x);
        Keep(expected);
        Eval(expected);
        var compiled = Compile(func(inputs []*Array) []*Array {
        return []*Array{gelu(inputs[0])}
        });
        defer compiled.Free();
        var result = compiled.Call(x)[0];
        Keep(result);
        Eval(result);
        var expData = expected.Data();
        var resData = result.Data();
        var for i = range expData {
        var diff = expData[i] - resData[i];
        if diff < 0 {
        diff = -diff;
    }
        if diff > 1e-5 {
        t.Errorf("mismatch at %d: expected %f, got %f (diff=%e)", i, expData[i], resData[i], diff);
    }
    }
    }

    public static void TestCompileMultipleInputs(*testing.T t) {
        var a = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{4});
        var b = NewArrayFloat32([]float32{5, 6, 7, 8}, []int32{4});
        Keep(a, b);
        Eval(a, b);
        var compiled = Compile(func(inputs []*Array) []*Array {
        var sum = Add(inputs[0], inputs[1]);
        var prod = Mul(inputs[0], inputs[1]);
        return []*Array{sum, prod}
        });
        defer compiled.Free();
        var outputs = compiled.Call(a, b);
        Keep(outputs...);
        Eval(outputs...);
        var sumData = outputs[0].Data();
        var prodData = outputs[1].Data();
        if sumData[0] != 6 || prodData[0] != 5 {
        t.Errorf("unexpected: sum[0]=%f, prod[0]=%f", sumData[0], prodData[0]);
    }
    }

    public static void TestCompileReuse(*testing.T t) {
        var compiled = Compile(func(inputs []*Array) []*Array {
        return []*Array{Add(inputs[0], inputs[0])}
        });
        defer compiled.Free();
        var for i = 0; i < 5; i++ {
        var x = NewArrayFloat32([]float32{float32(i)}, []int32{1});
        Keep(x);
        x.Eval();
        var result = compiled.Call(x)[0];
        Keep(result);
        Eval(result);
        var data = result.Data();
        var expected = float32(i * 2);
        if data[0] != expected {
        t.Errorf("iteration %d: expected %f, got %f", i, expected, data[0]);
    }
    }
    }

    public static void BenchmarkGELUUncompiled(*testing.B b) {
        var x = RandomNormal([]int32{1000, 1024}, 42);
        Keep(x);
        x.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var y = x;
        var for j = 0; j < 10; j++ {
        y = gelu(y);
    }
        Keep(y);
        Eval(y);
    }
    }

    public static void BenchmarkGELUCompiled(*testing.B b) {
        var x = RandomNormal([]int32{1000, 1024}, 42);
        Keep(x);
        x.Eval();
        var compiled = Compile(func(inputs []*Array) []*Array {
        var y = inputs[0];
        var for j = 0; j < 10; j++ {
        y = gelu(y);
    }
        return []*Array{y}
        });
        defer compiled.Free();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var result = compiled.Call(x);
        Keep(result[0]);
        Eval(result[0]);
    }
    }

    public static void TestCompileNoMemoryLeak(*testing.T t) {
        var x = RandomNormal([]int32{100, 100}, 42);
        Keep(x);
        x.Eval();
        var compiled = Compile(func(inputs []*Array) []*Array {
        var y = inputs[0];
        var for j = 0; j < 5; j++ {
        y = gelu(y);
    }
        return []*Array{y}
        });
        defer compiled.Free();
        var for i = 0; i < 10; i++ {
        var result = compiled.Call(x);
        Keep(result[0]);
        Eval(result[0]);
        result[0].Free();
    }
        MetalResetPeakMemory();
        var initialMem = MetalGetActiveMemory();
        var for i = 0; i < 100; i++ {
        var result = compiled.Call(x);
        Keep(result[0]);
        Eval(result[0]);
        result[0].Free();
    }
        Eval() // Final cleanup;
        var finalMem = MetalGetActiveMemory();
        var peakMem = MetalGetPeakMemory();
        var growth = long(finalMem) - long(initialMem);
        if growth > 10*1024*1024 {
        t.Errorf("memory grew by %d bytes over 100 iterations", growth);
    }
        t.Logf("memory: initial=%dMB, final=%dMB, peak=%dMB, growth=%dKB",;
        initialMem/(1<<20), finalMem/(1<<20), peakMem/(1<<20), growth/1024);
    }

    public static void TestCompileWithRandomState(*testing.T t) {
        var logits = NewArrayFloat32([]float32{0.1, 0.2, 0.3, 0.4}, []int32{1, 4});
        Keep(logits);
        logits.Eval();
        var key = RandomKey(42);
        Keep(key);
        var compiled = Compile(func(inputs []*Array) []*Array {
        var logits = inputs[0];
        var keyIn = inputs[1];
        var key1, key2 = RandomSplit(keyIn);
        var sample = RandomCategoricalWithKey(logits, key2, -1, 1);
        return []*Array{sample, key1}
        });
        defer compiled.Free();
        var samples = make([]int32, 10);
        var for i = 0; i < 10; i++ {
        var outputs = compiled.Call(logits, key);
        Keep(outputs...);
        Eval(outputs...);
        samples[i] = outputs[0].ItemInt32();
        key.Free();
        key = outputs[1];
    }
        var for i, s = range samples {
        if s < 0 || s > 3 {
        t.Errorf("sample %d out of range: %d", i, s);
    }
    }
        t.Logf("samples: %v", samples);
        var allSame = true;
        var for i = 1; i < len(samples); i++ {
        if samples[i] != samples[0] {
        allSame = false;
        break;
    }
    }
        if allSame {
        t.Error("all samples are the same - random state may not be updating");
    }
    }
        func swiGLU(gate, up *Array, alpha, limit float32) *Array {
        var gateClipped = ClipScalar(gate, 0, limit, false, true);
        var upClipped = ClipScalar(up, -limit, limit, true, true);
        var gluScaled = MulScalar(gateClipped, alpha);
        var sig = Sigmoid(gluScaled);
        var outGlu = Mul(gateClipped, sig);
        return Mul(outGlu, AddScalar(upClipped, 1.0));
    }

    public static void TestCompileSwiGLU(*testing.T t) {
        var gate = NewArrayFloat32([]float32{-1, 0, 1, 2, 5, 10}, []int32{6});
        var up = NewArrayFloat32([]float32{-5, -1, 0, 1, 5, 10}, []int32{6});
        Keep(gate, up);
        Eval(gate, up);
        const alpha float32 = 1.702;
        const limit float32 = 7.0;
        var expected = swiGLU(gate, up, alpha, limit);
        Keep(expected);
        Eval(expected);
        var compiled = Compile(func(inputs []*Array) []*Array {
        return []*Array{swiGLU(inputs[0], inputs[1], alpha, limit)}
        });
        defer compiled.Free();
        var result = compiled.Call(gate, up)[0];
        Keep(result);
        Eval(result);
        var expData = expected.Data();
        var resData = result.Data();
        var for i = range expData {
        var diff = expData[i] - resData[i];
        if diff < 0 {
        diff = -diff;
    }
        if diff > 1e-5 {
        t.Errorf("mismatch at %d: expected %f, got %f", i, expData[i], resData[i]);
    }
    }
        t.Logf("SwiGLU results: %v", resData);
    }

    public static void BenchmarkSwiGLUUncompiled(*testing.B b) {
        var gate = RandomNormal([]int32{1, 2880}, 42);
        var up = RandomNormal([]int32{1, 2880}, 43);
        Keep(gate, up);
        Eval(gate, up);
        const alpha float32 = 1.702;
        const limit float32 = 7.0;
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var result = swiGLU(gate, up, alpha, limit);
        Keep(result);
        Eval(result);
    }
    }

    public static void BenchmarkSwiGLUCompiled(*testing.B b) {
        var gate = RandomNormal([]int32{1, 2880}, 42);
        var up = RandomNormal([]int32{1, 2880}, 43);
        Keep(gate, up);
        Eval(gate, up);
        const alpha float32 = 1.702;
        const limit float32 = 7.0;
        var compiled = Compile(func(inputs []*Array) []*Array {
        return []*Array{swiGLU(inputs[0], inputs[1], alpha, limit)}
        });
        defer compiled.Free();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var result = compiled.Call(gate, up);
        Keep(result[0]);
        Eval(result[0]);
    }
    }

    public static void BenchmarkSwiGLU10xUncompiled(*testing.B b) {
        var x = RandomNormal([]int32{1, 2880}, 42);
        Keep(x);
        x.Eval();
        const alpha float32 = 1.702;
        const limit float32 = 7.0;
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var y = x;
        var for j = 0; j < 10; j++ {
        y = swiGLU(y, y, alpha, limit);
    }
        Keep(y);
        Eval(y);
    }
    }

    public static void BenchmarkSwiGLU10xCompiled(*testing.B b) {
        var x = RandomNormal([]int32{1, 2880}, 42);
        Keep(x);
        x.Eval();
        const alpha float32 = 1.702;
        const limit float32 = 7.0;
        var compiled = Compile(func(inputs []*Array) []*Array {
        var y = inputs[0];
        var for j = 0; j < 10; j++ {
        y = swiGLU(y, y, alpha, limit);
    }
        return []*Array{y}
        });
        defer compiled.Free();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var result = compiled.Call(x);
        Keep(result[0]);
        Eval(result[0]);
    }
    }

    public static void sampleTopK(*Array key) {
        var neg = Neg(logits);
        var indices = Argpartition(neg, k-1, -1);
        var topK = Slice(indices, []int32{0}, []int32{int32(k)});
        var values = TakeAlongAxis(logits, topK, -1);
        var key1, key2 = RandomSplit(key);
        var sampled = RandomCategoricalWithKey(values, key2, -1, 1);
        return Take(topK, sampled, -1), key1;
    }

    public static void sampleTopP(*Array key, float32 p) {
        var sorted = Argsort(Neg(logits), -1);
        var sortedLogits = TakeAlongAxis(logits, sorted, -1);
        var probs = Softmax(sortedLogits, -1);
        var cumProbs = Cumsum(probs, -1);
        var mask = LessScalar(cumProbs, p);
        var negInf = FullDtype(float32(-1e9), logits.Dtype(), vocabSize);
        var masked = Where(mask, sortedLogits, negInf);
        var key1, key2 = RandomSplit(key);
        var sampled = RandomCategoricalWithKey(masked, key2, -1, 1);
        return Take(sorted, sampled, -1), key1;
    }

    public static void BenchmarkSampleTopKUncompiled(*testing.B b) {
        var vocabSize = int32(32000);
        var logits = RandomNormal([]int32{vocabSize}, 42);
        var key = RandomKey(42);
        Keep(logits, key);
        Eval(logits, key);
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var token *Array;
        token, key = sampleTopK(logits, key, 40);
        Keep(token, key);
        Eval(token);
    }
    }

    public static void BenchmarkSampleTopKCompiled(*testing.B b) {
        var vocabSize = int32(32000);
        var logits = RandomNormal([]int32{vocabSize}, 42);
        var key = RandomKey(42);
        Keep(logits, key);
        Eval(logits, key);
        var compiled = Compile(func(inputs []*Array) []*Array {
        var token, newKey = sampleTopK(inputs[0], inputs[1], 40);
        return []*Array{token, newKey}
        });
        defer compiled.Free();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var outputs = compiled.Call(logits, key);
        Keep(outputs...);
        Eval(outputs[0]);
        key = outputs[1];
    }
    }

    public static void BenchmarkSampleTopPUncompiled(*testing.B b) {
        var vocabSize = int32(32000);
        var logits = RandomNormal([]int32{vocabSize}, 42);
        var key = RandomKey(42);
        Keep(logits, key);
        Eval(logits, key);
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var token *Array;
        token, key = sampleTopP(logits, key, 0.9, vocabSize);
        Keep(token, key);
        Eval(token);
    }
    }

    public static void BenchmarkSampleTopPCompiled(*testing.B b) {
        var vocabSize = int32(32000);
        var logits = RandomNormal([]int32{vocabSize}, 42);
        var key = RandomKey(42);
        Keep(logits, key);
        Eval(logits, key);
        var compiled = Compile(func(inputs []*Array) []*Array {
        var token, newKey = sampleTopP(inputs[0], inputs[1], 0.9, vocabSize);
        return []*Array{token, newKey}
        });
        defer compiled.Free();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var outputs = compiled.Call(logits, key);
        Keep(outputs...);
        Eval(outputs[0]);
        key = outputs[1];
    }
    }

    public static void TestCompiledSamplerMemoryStable(*testing.T t) {
        var vocabSize = int32(32000);
        var logits = RandomNormal([]int32{vocabSize}, 42);
        var key = RandomKey(42);
        Keep(logits, key);
        Eval(logits, key);
        var compiledTopK = Compile(func(inputs []*Array) []*Array {
        var token, newKey = sampleTopK(inputs[0], inputs[1], 40);
        return []*Array{token, newKey}
        });
        defer compiledTopK.Free();
        var compiledTopP = Compile(func(inputs []*Array) []*Array {
        var token, newKey = sampleTopP(inputs[0], inputs[1], 0.9, vocabSize);
        return []*Array{token, newKey}
        });
        defer compiledTopP.Free();
        var for i = 0; i < 10; i++ {
        var out = compiledTopK.Call(logits, key);
        Keep(out...);
        Eval(out[0]);
        out[0].Free();
        key = out[1];
    }
        MetalResetPeakMemory();
        var initialMem = MetalGetActiveMemory();
        var for i = 0; i < 500; i++ {
        var out = compiledTopK.Call(logits, key);
        Keep(out...);
        Eval(out[0]);
        out[0].Free();
        key = out[1];
        out = compiledTopP.Call(logits, key);
        Keep(out...);
        Eval(out[0]);
        out[0].Free();
        key = out[1];
    }
        Eval() // Final cleanup;
        var finalMem = MetalGetActiveMemory();
        var peakMem = MetalGetPeakMemory();
        var growth = long(finalMem) - long(initialMem);
        t.Logf("memory: initial=%dMB, final=%dMB, peak=%dMB, growth=%dKB",;
        initialMem/(1<<20), finalMem/(1<<20), peakMem/(1<<20), growth/1024);
        if growth > 20*1024*1024 {
        t.Errorf("memory grew by %d bytes over 1000 sampler calls - possible leak!", growth);
    }
    }

    public static void BenchmarkSimpleOps(*testing.B b) {
        var weight = NewArrayFloat32([]float32{1, 0, 0, 1}, []int32{2, 2});
        Keep(weight);
        weight.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var x = NewArrayFloat32([]float32{1, 2}, []int32{1, 2});
        var result = Matmul(x, weight);
        Keep(result);
        AsyncEval(result);
        result.Eval();
    }
    }

    public static void BenchmarkLayerLike(*testing.B b) {
        var hidden = int32(256);
        var w = Ones(hidden, hidden);
        Keep(w);
        w.Eval();
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var x = Ones(1, hidden);
        var h = Matmul(x, w)                  // [1, 256] @ [256, 256] = [1, 256];
        h = Add(h, Matmul(h, w))           // residual;
        h = Mul(h, Sigmoid(Matmul(h, w)))  // gating;
        h = Matmul(h, w)                   // output projection;
        h = Add(x, RMSNormNoWeight(h, 1e-5)) // residual + norm;
        Keep(h);
        AsyncEval(h);
        Eval(h);
    }
    }

    public static void BenchmarkManyOps(*testing.B b) {
        var w = Ones(64, 64);
        Keep(w);
        w.Eval();
        var for _, numOps = range []int{10, 50, 100, 500, 1000} {
        b.Run(fmt.Sprintf("ops_%d", numOps), func(b *testing.B) {
        var for i = 0; i < b.N; i++ {
        var x = Ones(1, 64);
        var for j = 0; j < numOps; j++ {
        x = Add(x, Matmul(x, w));
    }
        Keep(x);
        AsyncEval(x);
        Eval(x);
    }
        });
    }
    }

    public static void BenchmarkLLMScale(*testing.B b) {
        var numLayers = 24;
        var opsPerLayer = 56;
        var hidden = int32(64);
        var weights = make([]*Array, numLayers*4);
        var for i = range weights {
        weights[i] = Ones(hidden, hidden);
    }
        Keep(weights...);
        Eval(weights...);
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var x = Ones(1, hidden);
        var for layer = 0; layer < numLayers; layer++ {
        var for op = 0; op < opsPerLayer/4; op++ {
        x = Add(x, Matmul(x, weights[layer*4]));
        x = Mul(x, Sigmoid(x));
    }
    }
        Keep(x);
        AsyncEval(x);
        Eval(x);
    }
    }

    public static void BenchmarkArrayFreeLoop(*testing.B b) {
        var for _, count = range []int{100, 500, 1000, 1500} {
        b.Run(fmt.Sprintf("arrays_%d", count), func(b *testing.B) {
        var for i = 0; i < b.N; i++ {
        b.StopTimer();
        var arrays = make([]*Array, count);
        var for j = 0; j < count; j++ {
        arrays[j] = NewArrayFloat32([]float32{1, 2, 3, 4}, []int32{2, 2});
    }
        b.StartTimer();
        Eval();
    }
        });
    }
    }

    public static void BenchmarkCleanupIsolated(*testing.B b) {
        var w = NewArrayFloat32([]float32{1}, []int32{1, 1});
        Keep(w);
        w.Eval();
        var for _, count = range []int{100, 500, 1000, 1500} {
        b.Run(fmt.Sprintf("arrays_%d", count), func(b *testing.B) {
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        b.StopTimer();
        var x = NewArrayFloat32([]float32{1}, []int32{1});
        var for j = 0; j < count; j++ {
        x = Add(x, x);
    }
        Keep(x);
        b.StartTimer();
        Eval() // Just cleanup;
    }
        });
    }
    }

    public static void TestMemoryStable(*testing.T t) {
        if testing.Short() {
        t.Skip("skipping memory test in short mode");
    }
        var batchSize = int32(1);
        var numHeads = int32(8);
        var seqLen = int32(256);
        var headDim = int32(64);
        var cacheShape = []int32{batchSize, numHeads, seqLen, headDim}
        var cacheSize = batchSize * numHeads * seqLen * headDim * 4 // float32 = 4 bytes;
        var keys = Zeros(cacheShape, DtypeFloat32);
        var values = Zeros(cacheShape, DtypeFloat32);
        Keep(keys, values);
        Eval(keys, values);
        var for i = 0; i < 5; i++ {
        var oldKeys, oldValues = keys, values;
        var newKeys = Add(keys, keys);
        var newValues = Add(values, values);
        Keep(newKeys, newValues);
        Eval(newKeys, newValues);
        oldKeys.Free();
        oldValues.Free();
        keys, values = newKeys, newValues;
    }
        MetalResetPeakMemory();
        var initialMem = MetalGetActiveMemory();
        var for step = 0; step < 100; step++ {
        var oldKeys, oldValues = keys, values;
        var newKeys = Add(keys, keys);
        var newValues = Add(values, values);
        Keep(newKeys, newValues);
        Eval(newKeys, newValues);
        oldKeys.Free();
        oldValues.Free();
        keys, values = newKeys, newValues;
    }
        Eval() // Final cleanup;
        var finalMem = MetalGetActiveMemory();
        var peakMem = MetalGetPeakMemory();
        var growth = long(finalMem) - long(initialMem);
        var expectedMaxGrowth = long(cacheSize * 4 * 10);
        t.Logf("cache size: %d bytes", cacheSize*2);
        t.Logf("memory: initial=%dMB, final=%dMB, peak=%dMB, growth=%dKB",;
        initialMem/(1<<20), finalMem/(1<<20), peakMem/(1<<20), growth/1024);
        if growth > expectedMaxGrowth {
        t.Errorf("memory grew by %d bytes over 100 steps (expected max %d) - possible leak",;
        growth, expectedMaxGrowth);
    }
    }
}
