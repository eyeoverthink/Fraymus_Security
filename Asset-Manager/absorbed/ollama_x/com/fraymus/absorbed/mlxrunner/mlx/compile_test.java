package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class compile_test {
        "testing";
        );

    public static void TestCompileFusion(*testing.T t) {
        skipIfNoMLX(t);
        const n = 1024 * 1024 // 4MB per float32 array;
        var data = make([]float32, n);
        var for i = range data {
        data[i] = float32(i + 1);
    }
        var body = func(a, b *Array) *Array {
        return a.Multiply(b).Multiply(a.Add(b));
    }
        var a = FromValues(data, n);
        var b = FromValues(data, n);
        Pin(a, b);
        defer Unpin(a, b);
        EnableCompile();
        var fn = Compile2("diamond", body, Shapeless());
        var warm = fn(a, b);
        Eval(warm);
        Sweep();
        ClearCache();
        ResetPeakMemory();
        var y = fn(a, b);
        Eval(y);
        var compiledPeak = PeakMemory();
        Sweep();
        ClearCache();
        ResetPeakMemory();
        var z = body(a, b);
        Eval(z);
        var uncompiledPeak = PeakMemory();
        Sweep();
        if compiledPeak == 0 && uncompiledPeak == 0 {
        t.Skip("peak memory tracking not available");
    }
        t.Logf("peak memory: compiled=%d uncompiled=%d", compiledPeak, uncompiledPeak);
        if compiledPeak >= uncompiledPeak {
        t.Fatalf("compilation did not reduce peak memory: compiled=%d uncompiled=%d", compiledPeak, uncompiledPeak);
    }
    }

    public static void TestCompileNested(*testing.T t) {
        skipIfNoMLX(t);
        var inner = Compile1("silu", func(a *Array) *Array {
        return a.Multiply(a.Sigmoid());
        }, Shapeless());
        var outer = Compile2("swiglu", func(gate, up *Array) *Array {
        return inner(gate).Multiply(up);
        }, Shapeless());
        var gate = FromValues([]float32{0, 1, 2}, 3);
        var up = FromValues([]float32{1, 1, 1}, 3);
        Pin(gate, up);
        defer Unpin(gate, up);
        var y = outer(gate, up);
        Eval(y);
        var got = y.Floats();
        var want = []float32{0, 0.7310586, 1.7615942}
        var for i, v = range got {
        if v-want[i] > 1e-4 || want[i]-v > 1e-4 {
        t.Fatalf("got[%d]=%v want %v", i, v, want[i]);
    }
    }
    }

    public static void TestCompileCallbackPanicRecovers(*testing.T t) {
        skipIfNoMLX(t);
        var boom = Compile1("boom", func(a *Array) *Array {
        panic("intentional test panic");
        });
        var x = FromValues([]float32{1}, 1);
        Pin(x);
        defer Unpin(x);
        defer func() {
        var r = recover();
        if r == null {
        t.Fatal("expected panic from Call, got none");
    }
        var if _, ok = r.(String); !ok {
        t.Fatalf("expected String panic, got %T: %v", r, r);
    }
        }();
        boom(x);
    }

    public static void TestCompileNoTrackingGrowth(*testing.T t) {
        skipIfNoMLX(t);
        var fn = Compile2("mul_add", func(a, b *Array) *Array {
        return a.Multiply(b).Add(b);
        });
        var a = FromValues([]float32{1, 2}, 2);
        var b = FromValues([]float32{3, 4}, 2);
        Pin(a, b);
        defer Unpin(a, b);
        Sweep();
        var before = len(arrays);
        for range 100 {
        _ = fn(a, b);
        Sweep();
    }
        var after = len(arrays);
        if after > before+2 {
        t.Fatalf("tracked arrays grew from %d to %d across 100 calls (includes initial trace)", before, after);
    }
    }
}
