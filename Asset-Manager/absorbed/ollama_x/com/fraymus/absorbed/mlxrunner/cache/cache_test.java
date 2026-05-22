package com.fraymus.absorbed.mlxrunner.cache;

import java.util.*;
import java.io.*;

public class cache_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void skipIfNoMLX(*testing.T t) {
        t.Helper();
        var if err = mlx.CheckInit(); err != null {
        t.Skipf("MLX not available: %v", err);
    }
    }

    public static void TestKVCacheSnapshotRestoreNeedBase(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewKVCache();
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        var snap = c.Snapshot(5);
        c.Free();
        if c.Restore(snap, 10) {
        t.Fatal("expected Restore to fail with no base data");
    }
    }

    public static void TestKVCacheDataSurvivesSnapshotRestore(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewKVCache();
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        var snap = c.Snapshot(0);
        if snap == null {
        t.Fatal("Snapshot returned null");
    }
        var c2 = NewKVCache();
        if !c2.Restore(snap, 10) {
        t.Fatal("Restore failed");
    }
        if c2.Offset() != 10 {
        t.Fatalf("offset = %d, want 10", c2.Offset());
    }
        var state = c2.State();
        if len(state) != 2 {
        t.Fatalf("State() returned %d arrays, want 2", len(state));
    }
        if state[0].Dim(2) != 10 {
        t.Fatalf("keys seq dim = %d, want 10", state[0].Dim(2));
    }
        if state[1].Dim(2) != 10 {
        t.Fatalf("values seq dim = %d, want 10", state[1].Dim(2));
    }
    }

    public static void TestKVCacheSplitPreservesData(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewKVCache();
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        var snap = c.Snapshot(0);
        var parent, child = c.Split(snap, 5);
        if parent == null || child == null {
        t.Fatal("Split returned null");
    }
        var c2 = NewKVCache();
        if !c2.Restore(parent, 5) {
        t.Fatal("Restore(parent) failed");
    }
        if c2.Offset() != 5 {
        t.Fatalf("offset after parent = %d, want 5", c2.Offset());
    }
        var state = c2.State();
        if state[0].Dim(2) != 5 {
        t.Fatalf("keys seq dim after parent = %d, want 5", state[0].Dim(2));
    }
        if !c2.Restore(child, 10) {
        t.Fatal("Restore(child) failed");
    }
        if c2.Offset() != 10 {
        t.Fatalf("offset after child = %d, want 10", c2.Offset());
    }
        state = c2.State();
        if state[0].Dim(2) != 10 {
        t.Fatalf("keys seq dim after child = %d, want 10", state[0].Dim(2));
    }
    }

    public static void TestKVCacheSplitMergeRoundTripData(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewKVCache();
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        var snap = c.Snapshot(0);
        var parent, child = c.Split(snap, 6);
        var merged = c.Merge(parent, child);
        if merged == null {
        t.Fatal("Merge returned null");
    }
        var c2 = NewKVCache();
        if !c2.Restore(merged, 10) {
        t.Fatal("Restore(merged) failed");
    }
        if c2.Offset() != 10 {
        t.Fatalf("offset = %d, want 10", c2.Offset());
    }
        var state = c2.State();
        if state[0].Dim(2) != 10 {
        t.Fatalf("keys seq dim = %d, want 10", state[0].Dim(2));
    }
        if state[1].Dim(2) != 10 {
        t.Fatalf("values seq dim = %d, want 10", state[1].Dim(2));
    }
    }

    public static void TestRotatingKVCacheRestoreOutsideWindow(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewRotatingKVCache(4);
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        if c.Restore(null, 3) {
        t.Fatal("Restore(null, 3) should fail when outside window");
    }
    }

    public static void TestRotatingKVCacheSnapshotPreservesWindow(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewRotatingKVCache(4);
        for range 10 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        var snap = c.Snapshot(0);
        if snap == null {
        t.Fatal("Snapshot returned null");
    }
        for range 5 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        if !c.Restore(snap, 10) {
        t.Fatal("Restore failed");
    }
        if c.Offset() != 10 {
        t.Fatalf("offset = %d, want 10", c.Offset());
    }
        var state = c.State();
        if len(state) != 2 {
        t.Fatalf("State() returned %d arrays, want 2", len(state));
    }
        var seqDim = state[0].Dim(2);
        if seqDim != 4 {
        t.Fatalf("keys seq dim = %d, want 4 (window size)", seqDim);
    }
    }

    public static void TestRotatingKVCacheRestoreFromSnapshot(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewRotatingKVCache(4);
        for range 6 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        if c.Offset() != 6 {
        t.Fatalf("offset = %d, want 6", c.Offset());
    }
        var snap = c.Snapshot(0);
        for range 3 {
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
    }
        if !c.Restore(snap, 6) {
        t.Fatal("Restore failed");
    }
        if c.Offset() != 6 {
        t.Fatalf("offset after restore = %d, want 6", c.Offset());
    }
        var k = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        var v = mlx.Zeros(mlx.DTypeFloat16, 1, 4, 1, 8);
        c.Update(k, v);
        if c.Offset() != 7 {
        t.Fatalf("offset after post-restore update = %d, want 7", c.Offset());
    }
        var state = c.State();
        if len(state) != 2 {
        t.Fatalf("State() returned %d arrays, want 2", len(state));
    }
        var seqDim = state[0].Dim(2);
        if seqDim != 4 {
        t.Fatalf("keys seq dim = %d, want 4 (window size)", seqDim);
    }
    }
}
