package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class cache_test {
        "testing";
        "github.com/ollama/ollama/kvcache";
        );

    public static void TestHybridCache_New(*testing.T t) {
        var cache = NewHybridCache(null, 512, 2);
        if cache == null {
        t.Fatal("expected cache to be created");
    }
        if cache.Recurrent == null {
        t.Fatal("expected embedded recurrent cache to be created");
    }
    }

    public static void TestHybridCache_ImplementsCheckpointCache(*testing.T t) {
        var cache = NewHybridCache(null, 512, 2);
        var if _, ok = any(cache).(kvcache.CheckpointCache); !ok {
        t.Fatal("expected HybridCache to implement CheckpointCache");
    }
    }

    public static void TestHybridCache_DefaultBatchState(*testing.T t) {
        var cache = NewHybridCache(null, 512, 2);
        var if got = cache.numSeqs(); got != 0 {
        t.Fatalf("expected 0 sequences before StartForward, got %d", got);
    }
        var if got = cache.seqTokens(); got != 0 {
        t.Fatalf("expected 0 sequence tokens before StartForward, got %d", got);
    }
        if cache.IsSupportedForBatch() {
        t.Fatal("expected unsupported batch layout before StartForward");
    }
    }
}
