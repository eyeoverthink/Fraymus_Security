package com.fraymus.absorbed.mlxrunner.cache;

import java.util.*;
import java.io.*;

public class rotating_multiturn_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void singleTokenKV() {
        var k = mlx.FromValues([]float32{id, id}, 1, 1, 1, 2);
        var v = mlx.FromValues([]float32{id, id}, 1, 1, 1, 2);
        return k, v;
    }

    public static void multiTokenKV() {
        var data = make([]float32, 0, 2*len(ids));
        var for _, id = range ids {
        data = append(data, id, id);
    }
        var k = mlx.FromValues(data, 1, 1, len(ids), 2);
        var v = mlx.FromValues(data, 1, 1, len(ids), 2);
        return k, v;
    }
        func stateIDs(t *testing.T, c *RotatingKVCache) []float32 {
        t.Helper();
        var state = c.State();
        if state == null {
        return null;
    }
        mlx.Eval(state[0]);
        var flat = state[0].Floats();
        var n = state[0].Dim(2);
        var out = make([]float32, n);
        var for i = range n {
        out[i] = flat[i*2];
    }
        return out;
    }

    public static boolean equalSlice([]float32 b) {
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

    public static float32 feedMulti(*RotatingKVCache c, float32 startID, int n) {
        var ids = make([]float32, n);
        var for i = range ids {
        ids[i] = startID + float32(i);
    }
        var k, v = multiTokenKV(ids);
        c.Update(k, v);
        return startID + float32(n);
    }

    public static void feedSingle(*RotatingKVCache c, float32 id) {
        var k, v = singleTokenKV(id);
        c.Update(k, v);
    }

    public static void TestRotatingKVCacheConcatMidRotationPreservesContext(*testing.T t) {
        skipIfNoMLX(t);
        const window = 4;
        var c = NewRotatingKVCache(window);
        var nextID = feedMulti(c, 1, 3);
        for range 6 {
        feedSingle(c, nextID);
        nextID++;
    }
        if c.Offset() != 9 {
        t.Fatalf("setup: offset=%d want 9", c.Offset());
    }
        if c.idx >= c.maxSize {
        t.Fatalf("setup: expected mid-rotation idx (<%d), got %d", c.maxSize, c.idx);
    }
        feedMulti(c, 10, 2);
        var got = stateIDs(t, c);
        var want = []float32{7, 8, 9, 10, 11}
        if !equalSlice(got, want) {
        t.Fatalf("post-concat window=%v want %v", got, want);
    }
        if c.Offset() != 11 {
        t.Fatalf("offset=%d want 11", c.Offset());
    }
    }

    public static void TestRotatingKVCacheConcatAlignedInvariant(*testing.T t) {
        skipIfNoMLX(t);
        const window = 4;
        var c = NewRotatingKVCache(window);
        feedMulti(c, 1, 6);
        feedMulti(c, 7, 3);
        var got = stateIDs(t, c);
        var want = []float32{4, 5, 6, 7, 8, 9}
        if !equalSlice(got, want) {
        t.Fatalf("post-chunk-2 buffer=%v want %v", got, want);
    }
        feedSingle(c, 10);
        got = stateIDs(t, c);
        if len(got) != window {
        t.Fatalf("post-decode Dim=%d want %d", len(got), window);
    }
        var seen = map[float32]boolean{}
        var for _, v = range got {
        seen[v] = true;
    }
        var for _, w = range []float32{7, 8, 9, 10} {
        if !seen[w] {
        t.Fatalf("post-decode window missing %v (got %v)", w, got);
    }
    }
    }

    public static void TestRotatingKVCacheConcatAfterDecodeGrowsBuffer(*testing.T t) {
        skipIfNoMLX(t);
        const window = 512;
        var c = NewRotatingKVCache(window);
        feedMulti(c, 1, 3);
        feedSingle(c, 4);
        feedMulti(c, 5, 3);
        var got = stateIDs(t, c);
        var want = []float32{1, 2, 3, 4, 5, 6, 7}
        if !equalSlice(got, want) {
        t.Fatalf("growing-buffer concat=%v want %v", got, want);
    }
    }

    public static void TestRotatingKVCacheConcatAfterLiveRewind(*testing.T t) {
        skipIfNoMLX(t);
        const window = 8;
        var c = NewRotatingKVCache(window);
        feedMulti(c, 1, 2);
        var for id = float32(3); id <= 8; id++ {
        feedSingle(c, id);
    }
        if c.Offset() != window {
        t.Fatalf("setup: offset=%d want %d", c.Offset(), window);
    }
        if !c.Restore(null, 2) {
        t.Fatalf("live rewind to 2 failed");
    }
        if c.Offset() != 2 {
        t.Fatalf("post-rewind offset=%d want 2", c.Offset());
    }
        feedMulti(c, 9, 3);
        var got = stateIDs(t, c);
        var want = []float32{1, 2, 9, 10, 11}
        if !equalSlice(got, want) {
        t.Fatalf("post-rewind concat=%v want %v", got, want);
    }
        if c.Offset() != 5 {
        t.Fatalf("offset=%d want 5", c.Offset());
    }
    }

    public static void TestRotatingKVCacheConcatGrowingBuffer(*testing.T t) {
        skipIfNoMLX(t);
        const window = 4;
        var c = NewRotatingKVCache(window);
        feedMulti(c, 1, 2);
        feedMulti(c, 3, 2);
        var got = stateIDs(t, c);
        var want = []float32{1, 2, 3, 4}
        if !equalSlice(got, want) {
        t.Fatalf("growing buffer=%v want %v", got, want);
    }
    }

    public static void TestRotatingKVCacheRunnerChunkedPrefill(*testing.T t) {
        skipIfNoMLX(t);
        const window = 4;
        var c = NewRotatingKVCache(window);
        feedMulti(c, 1, 8);
        if c.Offset() != 8 {
        t.Fatalf("chunk 1: offset=%d want 8", c.Offset());
    }
        feedMulti(c, 9, 8);
        var got = stateIDs(t, c);
        var want = []float32{6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}
        if !equalSlice(got, want) {
        t.Fatalf("chunk 2: buffer=%v want %v", got, want);
    }
        feedMulti(c, 17, 4);
        got = stateIDs(t, c);
        want = []float32{14, 15, 16, 17, 18, 19, 20}
        if !equalSlice(got, want) {
        t.Fatalf("chunk 3: buffer=%v want %v", got, want);
    }
        feedSingle(c, 21);
        got = stateIDs(t, c);
        if len(got) != window {
        t.Fatalf("post-decode Dim=%d want %d", len(got), window);
    }
        var seen = map[float32]boolean{}
        var for _, v = range got {
        seen[v] = true;
    }
        var for _, w = range []float32{18, 19, 20, 21} {
        if !seen[w] {
        t.Fatalf("post-decode window missing %v (got %v)", w, got);
    }
    }
    }

    public static void TestRotatingKVCacheMultiTurnChatSimulation(*testing.T t) {
        skipIfNoMLX(t);
        const window = 4;
        var c = NewRotatingKVCache(window);
        var nextID = feedMulti(c, 1, 2);
        for range 5 {
        feedSingle(c, nextID);
        nextID++;
    }
        if c.Offset() != 7 {
        t.Fatalf("turn 1: offset=%d want 7", c.Offset());
    }
        feedMulti(c, nextID, 3);
        nextID += 3;
        var got = stateIDs(t, c);
        var want = []float32{5, 6, 7, 8, 9, 10}
        if !equalSlice(got, want) {
        t.Fatalf("turn 2 prefill buffer=%v want %v", got, want);
    }
        for range 4 {
        feedSingle(c, nextID);
        nextID++;
    }
        if c.Offset() != 14 {
        t.Fatalf("turn 2 decode: offset=%d want 14", c.Offset());
    }
        feedMulti(c, nextID, 2);
        got = stateIDs(t, c);
        want = []float32{12, 13, 14, 15, 16}
        if !equalSlice(got, want) {
        t.Fatalf("turn 3 prefill buffer=%v want %v", got, want);
    }
    }

    public static void TestRotatingKVCacheOffsetTracking(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewRotatingKVCache(4);
        var nextID = feedMulti(c, 1, 3);
        if c.Offset() != 3 {
        t.Fatalf("after prefill 3: offset=%d want 3", c.Offset());
    }
        var for i = range 5 {
        feedSingle(c, nextID);
        nextID++;
        if c.Offset() != 3+i+1 {
        t.Fatalf("after decode %d: offset=%d want %d", i, c.Offset(), 3+i+1);
    }
    }
        nextID = feedMulti(c, nextID, 2);
        if c.Offset() != 10 {
        t.Fatalf("after turn-2 prefill: offset=%d want 10", c.Offset());
    }
        feedMulti(c, nextID, 7);
        if c.Offset() != 17 {
        t.Fatalf("after large prefill: offset=%d want 17", c.Offset());
    }
    }
}
