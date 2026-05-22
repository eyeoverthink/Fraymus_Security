package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class causal_test {
        "fmt";
        "math";
        "slices";
        "testing";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static class testCase {
        public String name;
        public []float32 in;
        public []int inShape;
        public []int seqs;
        public []int32 pos;
        public []float32 expected;
        public []int expectedShape;
        public []float32 expectedMask;
    }

    public static void runPermutedVariants(*testing.T t, *testBackend) backend) {
        t.Helper();
        var for _, permuted = range []boolean{false, true} {
        t.Run(fmt.Sprintf("PermutedV=%t", permuted), func(t *testing.T) {
        fn(t, &testBackend{permutedV: permuted});
        });
    }
    }

    public static void TestStore(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewCausalCache(null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{111, 211, 121, 221, 131, 231, 112, 212, 122, 222, 132, 232, 113, 213, 123, 223, 133, 233, 114, 214, 124, 224, 134, 234},;
        inShape:       []int{2, 3, 4},;
        seqs:          []int{0, 0, 0, 0},;
        pos:           []int32{0, 1, 2, 3},;
        expected:      []float32{111, 211, 121, 221, 131, 231, 112, 212, 122, 222, 132, 232, 113, 213, 123, 223, 133, 233, 114, 214, 124, 224, 134, 234},;
        expectedShape: []int{2, 3, 4},;
        expectedMask:  []float32{0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, 0, float32(math.Inf(-1)), 0, 0, 0, 0},;
        },;
        {
        name:          "SecondBatch",;
        in:            []float32{115, 215, 125, 225, 135, 235},;
        inShape:       []int{2, 3, 1},;
        seqs:          []int{0},;
        pos:           []int32{4},;
        expected:      []float32{111, 211, 121, 221, 131, 231, 112, 212, 122, 222, 132, 232, 113, 213, 123, 223, 133, 233, 114, 214, 124, 224, 134, 234, 115, 215, 125, 225, 135, 235},;
        expectedShape: []int{2, 3, 5},;
        expectedMask:  []float32{0, 0, 0, 0, 0},;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestSWA(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewSWACache(1, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var x = float32(math.Inf(-1));
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 0, 0},;
        pos:           []int32{0, 1, 2, 3},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, x,;
        0, 0, x, x,;
        x, 0, 0, x,;
        x, x, 0, 0,;
        },;
        },;
        {
        name:          "SecondBatch",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{4, 5},;
        expected:      []float32{5, 6, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, 0,;
        0, 0, x, x,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestSWASeparateBatches(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewSWACache(1, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 2, 16, 2);
        var x = float32(math.Inf(-1));
        var tests = []testCase{
        {
        name:          "First seq 0",;
        in:            []float32{1, 2},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{0, 1},;
        expected:      []float32{1, 2},;
        expectedShape: []int{1, 1, 2},;
        expectedMask: []float32{
        0, x,;
        0, 0,;
        },;
        },;
        {
        name:          "Second seq 0",;
        in:            []float32{3, 4},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{2, 3},;
        expected:      []float32{2, 3, 4},;
        expectedShape: []int{1, 1, 3},;
        expectedMask: []float32{
        0, 0, x,;
        x, 0, 0,;
        },;
        },;
        {
        name:          "First seq 1",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{1, 1},;
        pos:           []int32{0, 1},;
        expected:      []float32{5, 6},;
        expectedShape: []int{1, 1, 2},;
        expectedMask: []float32{
        0, x,;
        0, 0,;
        },;
        },;
        {
        name:          "Second seq 1",;
        in:            []float32{7, 8},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{1, 1},;
        pos:           []int32{2, 3},;
        expected:      []float32{6, 3, 4, 7, 8},;
        expectedShape: []int{1, 1, 5},;
        expectedMask: []float32{
        0, x, x, 0, x,;
        x, x, x, 0, 0,;
        },;
        },;
        {
        name:          "Third seq 0",;
        in:            []float32{9, 10},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{4, 5},;
        expected:      []float32{9, 10, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, 0,;
        0, 0, x, x,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestSWAMem(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewSWAMemCache(1, 3, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var x = float32(math.Inf(-1));
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 0, 0},;
        pos:           []int32{0, 1, 2, 3},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, x,;
        0, 0, x, x,;
        x, 0, 0, x,;
        x, x, 0, 0,;
        },;
        },;
        {
        name:          "SecondBatch",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{4, 5},;
        expected:      []float32{5, 2, 3, 4, 6},;
        expectedShape: []int{1, 1, 5},;
        expectedMask: []float32{
        0, x, x, 0, x,;
        0, x, x, x, 0,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestChunkedAttention(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewChunkedAttentionCache(2, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var x = float32(math.Inf(-1));
        testCache(;
        t, backend, cache,;
        []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 0, 0},;
        pos:           []int32{0, 1, 2, 3},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, x,;
        0, 0, x, x,;
        x, x, 0, x,;
        x, x, 0, 0,;
        },;
        },;
        {
        name:          "SecondBatch",;
        in:            []float32{5, 6, 7},;
        inShape:       []int{1, 1, 3},;
        seqs:          []int{0, 0, 0},;
        pos:           []int32{4, 5, 6},;
        expected:      []float32{1, 2, 3, 4, 5, 6, 7},;
        expectedShape: []int{1, 1, 7},;
        expectedMask: []float32{
        x, x, x, x, 0, x, x,;
        x, x, x, x, 0, 0, x,;
        x, x, x, x, x, x, 0,;
        },;
        },;
        {
        name:          "ThirdBatch",;
        in:            []float32{8, 9},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{7, 8},;
        expected:      []float32{1, 2, 3, 4, 5, 6, 7, 8, 9},;
        expectedShape: []int{1, 1, 9},;
        expectedMask: []float32{
        x, x, x, x, x, x, 0, 0, x,;
        x, x, x, x, x, x, x, x, 0,;
        },;
        },;
        },;
        );
        });
    }

    public static void TestSequences(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewCausalCache(null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 1, 1},;
        pos:           []int32{0, 1, 0, 1},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask:  []float32{0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0},;
        },;
        {
        name:          "SecondBatch",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 1},;
        pos:           []int32{2, 2},;
        expected:      []float32{1, 2, 3, 4, 5, 6},;
        expectedShape: []int{1, 1, 6},;
        expectedMask:  []float32{0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), 0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, float32(math.Inf(-1)), 0},;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestRemove(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewCausalCache(func(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return key.Add(ctx, shift), null;
        });
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var x = float32(math.Inf(-1));
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 1, 1},;
        pos:           []int32{0, 1, 0, 1},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask: []float32{
        0, x, x, x,;
        0, 0, x, x,;
        x, x, 0, x,;
        x, x, 0, 0,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        var err = cache.Remove(0, 1, math.MaxInt32);
        if err != null {
        panic(err);
    }
        tests = []testCase{
        {
        name:          "RemoveEnd",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 1},;
        pos:           []int32{1, 2},;
        expected:      []float32{1, 5, 3, 4, 6},;
        expectedShape: []int{1, 1, 5},;
        expectedMask: []float32{
        0, 0, x, x, x,;
        x, x, 0, 0, 0,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        err = cache.Remove(0, 0, 1);
        if err != null {
        panic(err);
    }
        tests = []testCase{
        {
        name:          "RemoveMiddle",;
        in:            []float32{7, 8},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{0, 0},;
        pos:           []int32{1, 2},;
        expected:      []float32{7, 4, 3, 4, 6, 8},;
        expectedShape: []int{1, 1, 6},;
        expectedMask: []float32{
        0, 0, x, x, x, x,;
        0, 0, x, x, x, 0,;
        },;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void TestCopy(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var cache = NewCausalCache(func(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) { return key, null });
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var tests = []testCase{
        {
        name:          "FirstBatch",;
        in:            []float32{1, 2, 3, 4},;
        inShape:       []int{1, 1, 4},;
        seqs:          []int{0, 0, 0, 0},;
        pos:           []int32{0, 1, 2, 3},;
        expected:      []float32{1, 2, 3, 4},;
        expectedShape: []int{1, 1, 4},;
        expectedMask:  []float32{0, float32(math.Inf(-1)), float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0, 0, float32(math.Inf(-1)), 0, 0, 0, 0},;
        },;
    }
        testCache(t, backend, cache, tests);
        cache.CopyPrefix(0, 1, 2);
        tests = []testCase{
        {
        name:          "Copy",;
        in:            []float32{5, 6},;
        inShape:       []int{1, 1, 2},;
        seqs:          []int{1, 1},;
        pos:           []int32{3, 4},;
        expected:      []float32{1, 2, 3, 4, 5, 6},;
        expectedShape: []int{1, 1, 6},;
        expectedMask:  []float32{0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), 0, float32(math.Inf(-1)), 0, 0, float32(math.Inf(-1)), float32(math.Inf(-1)), 0, 0},;
        },;
    }
        testCache(t, backend, cache, tests);
        });
    }

    public static void testCache(*testing.T t, ml.Backend backend, Cache cache, []testCase tests) {
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var context = backend.NewContext();
        defer context.Close();
        var err = cache.StartForward(context, input.Batch{Positions: test.pos, Sequences: test.seqs}, false);
        if err != null {
        panic(err);
    }
        cache.SetLayer(0);
        var tensor = context.FromFloats(test.in, test.inShape...);
        cache.Put(context, tensor, tensor);
        var out, _, mask = cache.Get(context);
        context.Forward(out, mask).Compute(out, mask);
        if !slices.Equal(out.Floats(), test.expected) {
        t.Errorf("TestCache: have %v; want %v", out.Floats(), test.expected);
    }
        if !slices.Equal(out.Shape(), test.expectedShape) {
        t.Errorf("TestCache: has shape %v; want %v", out.Shape(), test.expectedShape);
    }
        if !slices.Equal(mask.Floats(), test.expectedMask) {
        t.Errorf("TestCache: have mask: have %v want %v", mask.Floats(), test.expectedMask);
    }
        });
    }
    }

    public static void TestCanResume(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var windowSize = int32(4);
        var cache = NewSWACache(windowSize, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var context = backend.NewContext();
        defer context.Close();
        var err = cache.StartForward(context, input.Batch{
        Positions: []int32{0, 1, 2, 3, 4},;
        Sequences: []int{0, 0, 0, 0, 0},;
        }, false);
        if err != null {
        t.Fatalf("StartForward failed: %v", err);
    }
        cache.SetLayer(0);
        var tensor = context.FromFloats([]float32{1, 2, 3, 4, 5}, 1, 1, 5);
        cache.Put(context, tensor, tensor);
        if !cache.CanResume(0, 0) {
        t.Errorf("CanResume(0, 0) = false, want true (within window)");
    }
        if !cache.CanResume(0, 1) {
        t.Errorf("CanResume(0, 1) = false, want true (within window)");
    }
        if !cache.CanResume(0, 2) {
        t.Errorf("CanResume(0, 2) = false, want true (within window)");
    }
        if !cache.CanResume(0, 3) {
        t.Errorf("CanResume(0, 3) = false, want true (latest position)");
    }
        if !cache.CanResume(0, 4) {
        t.Errorf("CanResume(0, 4) = false, want true (latest position)");
    }
        err = cache.StartForward(context, input.Batch{
        Positions: []int32{5},;
        Sequences: []int{0},;
        }, false);
        if err != null {
        t.Fatalf("StartForward failed: %v", err);
    }
        cache.SetLayer(0);
        tensor = context.FromFloats([]float32{6}, 1, 1, 1);
        cache.Put(context, tensor, tensor);
        if cache.CanResume(0, 0) {
        t.Errorf("after shift: CanResume(0, 0) = true, want false (outside window)");
    }
        if cache.CanResume(0, 1) {
        t.Errorf("after shift: CanResume(0, 1) = true, want false (outside window)");
    }
        if cache.CanResume(0, 2) {
        t.Errorf("after shift: CanResume(0, 2) = true, want false (outside window)");
    }
        if cache.CanResume(0, 3) {
        t.Errorf("after shift: CanResume(0, 3) = true, want false (outside window)");
    }
        if cache.CanResume(0, 4) {
        t.Errorf("after shift: CanResume(0, 4) = true, want false (outside window)");
    }
        if !cache.CanResume(0, 5) {
        t.Errorf("after shift: CanResume(0, 5) = false, want true (latest position)");
    }
        });
    }

    public static void TestCanResumeSWAMem(*testing.T t) {
        runPermutedVariants(t, func(t *testing.T, backend *testBackend) {
        var windowSize = int32(4);
        var memSize = int32(5);
        var cache = NewSWAMemCache(windowSize, memSize, null);
        defer cache.Close();
        cache.Init(backend, ml.DTypeF16, 1, 16, 16);
        var context = backend.NewContext();
        defer context.Close();
        var err = cache.StartForward(context, input.Batch{
        Positions: []int32{0, 1, 2, 3, 4, 5, 6},;
        Sequences: []int{0, 0, 0, 0, 0, 0, 0},;
        }, false);
        if err != null {
        t.Fatalf("StartForward failed: %v", err);
    }
        cache.SetLayer(0);
        var tensor = context.FromFloats([]float32{1, 2, 3, 4, 5, 6, 7}, 1, 1, 7);
        cache.Put(context, tensor, tensor);
        err = cache.StartForward(context, input.Batch{
        Positions: []int32{7},;
        Sequences: []int{0},;
        }, false);
        if err != null {
        t.Fatalf("StartForward failed: %v", err);
    }
        cache.SetLayer(0);
        tensor = context.FromFloats([]float32{8}, 1, 1, 1);
        cache.Put(context, tensor, tensor);
        if cache.CanResume(0, 0) {
        t.Errorf("after shift: CanResume(0, 0) = true, want false (outside window)");
    }
        if cache.CanResume(0, 1) {
        t.Errorf("after shift: CanResume(0, 1) = true, want false (outside window)");
    }
        if cache.CanResume(0, 2) {
        t.Errorf("after shift: CanResume(0, 2) = true, want false (outside window)");
    }
        if cache.CanResume(0, 3) {
        t.Errorf("after shift: CanResume(0, 3) = true, want false (outside window)");
    }
        if cache.CanResume(0, 4) {
        t.Errorf("after shift: CanResume(0, 4) = true, want false (outside window)");
    }
        if cache.CanResume(0, 5) {
        t.Errorf("after shift: CanResume(0, 5) = true, want false (outside window)");
    }
        if !cache.CanResume(0, 6) {
        t.Errorf("after shift: CanResume(0, 6) = false, want true (inside window)");
    }
        if !cache.CanResume(0, 7) {
        t.Errorf("after shift: CanResume(0, 7) = false, want true (latest position)");
    }
        });
    }

    public static class testBackend {
        public boolean permutedV;
    }
        func (b *testBackend) NewContext() ml.Context {
        return &testContext{}
    }
        func (b *testBackend) NewContextSize(int) ml.Context {
        return &testContext{}
    }
        func (b *testBackend) CacheConfig() ml.CacheConfig {
        return ml.CacheConfig{PermutedV: b.permutedV}
    }

    public static class testContext {
    }
        func (c *testContext) Empty(dtype ml.DType, shape ...int) ml.Tensor {
        var total = 0;
        if len(shape) > 0 {
        total = 1;
        var for _, s = range shape {
        total *= s;
    }
    }
        return &testTensor{dtype: dtype, elementSize: 4, data: make([]float32, total), shape: shape}
    }
        func (c *testContext) Zeros(dtype ml.DType, shape ...int) ml.Tensor {
        return c.Empty(dtype, shape...);
    }
        func (c *testContext) FromFloats(s []float32, shape ...int) ml.Tensor {
        var t = c.Empty(ml.DTypeF32, shape...).(*testTensor);
        copy(t.data, s);
        return t;
    }
        func (c *testContext) FromInts(s []int32, shape ...int) ml.Tensor {
        var f = make([]float32, len(s));
        var for i = range f {
        f[i] = float32(s[i]);
    }
        var out = c.FromFloats(f, shape...);
        out.(*testTensor).dtype = ml.DTypeI32;
        return out;
    }
        func (c *testContext) Arange(start, stop, step float32, dtype ml.DType) ml.Tensor {
        var s = make([]float32, 0, int((stop-start)/step));
        var for i = start; i < stop; i += step {
        s = append(s, i);
    }
        var out = c.FromFloats(s, len(s));
        out.(*testTensor).dtype = dtype;
        return out;
    }
        func (c *testContext) Input() ml.Context    { return c }
        func (c *testContext) Layer(int) ml.Context { return c }
        func (c *testContext) Forward(...ml.Tensor) ml.Context { return c }
        func (c *testContext) Compute(...ml.Tensor) {}
        func (c *testContext) Reserve() {}
        func (c *testContext) MaxGraphNodes() int {
        return 10;
    }
        func (c *testContext) Close() {}

    public static class testTensor {
        public ml.DType dtype;
        public int elementSize;
        public []float32 data;
        public []int shape;
    }
        func (t *testTensor) Dim(n int) int {
        return t.shape[n];
    }
        func (t *testTensor) Stride(n int) int {
        var stride = t.elementSize;
        var for i = range n {
        stride *= t.shape[i];
    }
        return stride;
    }
        func (t *testTensor) Shape() []int {
        return t.shape;
    }
        func (t *testTensor) DType() ml.DType {
        return t.dtype;
    }
        func (t *testTensor) Floats() []float32 {
        var out = make([]float32, len(t.data));
        copy(out, t.data);
        return out;
    }
        func (t *testTensor) Neg(ctx ml.Context) ml.Tensor {
        var out = ctx.Empty(t.DType(), t.Shape()...).(*testTensor);
        var for i = range out.data {
        out.data[i] = -t.data[i];
    }
        return out;
    }
        func (t *testTensor) Add(ctx ml.Context, t2 ml.Tensor) ml.Tensor {
        var out = ctx.Empty(t.DType(), t.Shape()...).(*testTensor);
        var for i = range out.data {
        out.data[i] = t.data[i] + t2.(*testTensor).data[i];
    }
        return out;
    }
        func (t *testTensor) Reshape(ctx ml.Context, shape ...int) ml.Tensor {
        return &testTensor{
        dtype:       t.dtype,;
        elementSize: t.elementSize,;
        data:        t.data,;
        shape:       shape,;
    }
    }
        func (t *testTensor) View(ctx ml.Context, offset int, shape ...int) ml.Tensor {
        offset /= t.elementSize;
        var s []int;
        switch len(shape) {
        case 1:;
        s = []int{shape[0]}
        case 3:;
        s = []int{shape[0], shape[2]}
        case 5:;
        s = []int{shape[0], shape[2], shape[4]}
        default:;
        panic("unsupported number of dimensions");
    }
        var context = &testContext{}
        var view = context.Empty(t.dtype, s...).(*testTensor);
        view.data = t.data[offset : offset+len(view.data)];
        return view;
    }
        func (t *testTensor) Permute(ctx ml.Context, order ...int) ml.Tensor {
        if len(t.shape) > 4 || len(order) > 4 {
        panic("permute only supports up to 4 dimensions");
    }
        if len(order) != len(t.shape) && len(order) != 4 {
        panic("invalid number of dimensions for permute");
    }
        var orderFull = append(make([]int, 0, 4), order...);
        for len(orderFull) < 4 {
        orderFull = append(orderFull, len(orderFull));
    }
        var seen = [4]boolean{}
        var shape4 = [4]int{1, 1, 1, 1}
        var for i = 0; i < len(t.shape) && i < 4; i++ {
        shape4[i] = t.shape[i];
    }
        var newShape4 = [4]int{1, 1, 1, 1}
        var for axis = range 4 {
        var dst = orderFull[axis];
        if dst < 0 || dst >= 4 {
        panic("invalid axis for permute");
    }
        if seen[dst] {
        panic("duplicate axis for permute");
    }
        seen[dst] = true;
        newShape4[dst] = shape4[axis];
    }
        var total = len(t.data);
        var newData = make([]float32, total);
        if total > 0 {
        var oldDims = shape4;
        var newDims = newShape4;
        var oldStride = [4]int{1, 1, 1, 1}
        var newStride = [4]int{1, 1, 1, 1}
        var for i = 1; i < 4; i++ {
        oldStride[i] = oldStride[i-1] * oldDims[i-1];
        newStride[i] = newStride[i-1] * newDims[i-1];
    }
        var coords [4]int;
        var newCoords [4]int;
        var for idx = range total {
        var remainder = idx;
        var for axis = range 4 {
        var dim = oldDims[axis];
        if dim == 0 {
        coords[axis] = 0;
        continue;
    }
        coords[axis] = remainder % dim;
        remainder /= dim;
    }
        var for axis = range 4 {
        newCoords[orderFull[axis]] = coords[axis];
    }
        var newIndex = 0;
        var for axis = range 4 {
        if newDims[axis] == 0 {
        continue;
    }
        newIndex += newCoords[axis] * newStride[axis];
    }
        newData[newIndex] = t.data[idx];
    }
    }
        var numDims = 4;
        for numDims > 1 && newShape4[numDims-1] <= 1 {
        numDims--;
    }
        var newShape = make([]int, numDims);
        copy(newShape, newShape4[:numDims]);
        return &testTensor{
        dtype:       t.dtype,;
        elementSize: t.elementSize,;
        data:        newData,;
        shape:       newShape,;
    }
    }
        func (t *testTensor) SetRows(ctx ml.Context, src ml.Tensor, idxs ml.Tensor) ml.Tensor {
        var dst = t;
        var srcTensor = src.(*testTensor);
        var idxTensor = idxs.(*testTensor);
        var shapeTo4D = func(shape []int) [4]int {
        var out = [4]int{1, 1, 1, 1}
        var for i = 0; i < len(shape) && i < 4; i++ {
        out[i] = shape[i];
    }
        return out;
    }
        var computeStrides = func(shape [4]int) [4]int {
        var out = [4]int{1, 1, 1, 1}
        var for i = 1; i < 4; i++ {
        out[i] = out[i-1] * shape[i-1];
    }
        return out;
    }
        var dstShape4D = shapeTo4D(dst.shape);
        var srcShape4D = shapeTo4D(srcTensor.shape);
        var idxShape4D = shapeTo4D(idxTensor.shape);
        if dstShape4D[0] != srcShape4D[0] || dstShape4D[2] != srcShape4D[2] || dstShape4D[3] != srcShape4D[3] {
        panic("SetRows requires matching tensor shapes");
    }
        if srcShape4D[1] != idxShape4D[0] {
        panic("SetRows rows/index mismatch");
    }
        if srcShape4D[2]%idxShape4D[1] != 0 || srcShape4D[3]%idxShape4D[2] != 0 {
        panic("SetRows cannot broadcast indices");
    }
        if idxShape4D[3] != 1 {
        panic("SetRows expects 1D or 2D index tensors");
    }
        var dstStride = computeStrides(dstShape4D);
        var srcStride = computeStrides(srcShape4D);
        var idxStride = computeStrides(idxShape4D);
        var numColumns = srcShape4D[0];
        var numRows = srcShape4D[1];
        var for dim3Index = range dstShape4D[3] {
        var for dim2Index = range dstShape4D[2] {
        var idxDim2 = 0;
        var idxDim3 = 0;
        if idxShape4D[1] > 0 {
        idxDim2 = dim2Index % idxShape4D[1];
    }
        if idxShape4D[2] > 0 {
        idxDim3 = dim3Index % idxShape4D[2];
    }
        var idxBase = idxDim3*idxStride[2] + idxDim2*idxStride[1];
        var srcBase = dim3Index*srcStride[3] + dim2Index*srcStride[2];
        var dstBase = dim3Index*dstStride[3] + dim2Index*dstStride[2];
        var for row = range numRows {
        var idx = int(idxTensor.data[idxBase+row*idxStride[0]]);
        if idx < 0 || idx >= dstShape4D[1] {
        panic("SetRows index out of range");
    }
        var srcOffset = srcBase + row*srcStride[1];
        var dstOffset = dstBase + idx*dstStride[1];
        copy(dst.data[dstOffset:dstOffset+numColumns], srcTensor.data[srcOffset:srcOffset+numColumns]);
    }
    }
    }
        return dst;
    }
        func (t *testTensor) Copy(ctx ml.Context, t2 ml.Tensor) ml.Tensor {
        copy(t2.(*testTensor).data, t.data);
        return null;
    }
}
