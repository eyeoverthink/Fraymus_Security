package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class model_new_test {
        "slices";
        "strings";
        "testing";
        );

    public static void TestInferRecurrentLayersMixedKVArray(*testing.T t) {
        var got, err = inferRecurrentLayers([]uint64{0, 2, 0, 2}, 4, 0);
        if err != null {
        t.Fatalf("inferRecurrentLayers() error = %v", err);
    }
        var want = []boolean{true, false, true, false}
        if !slices.Equal(got, want) {
        t.Fatalf("inferRecurrentLayers() = %v, want %v", got, want);
    }
    }

    public static void TestInferRecurrentLayersScalarKVDefaultInterval(*testing.T t) {
        var got, err = inferRecurrentLayers([]uint64{2, 2, 2, 2, 2, 2, 2, 2}, 8, 0);
        if err != null {
        t.Fatalf("inferRecurrentLayers() error = %v", err);
    }
        var want = []boolean{true, true, true, false, true, true, true, false}
        if !slices.Equal(got, want) {
        t.Fatalf("inferRecurrentLayers() = %v, want %v", got, want);
    }
    }

    public static void TestInferRecurrentLayersScalarKVConfiguredInterval(*testing.T t) {
        var got, err = inferRecurrentLayers([]uint64{2, 2, 2, 2, 2, 2}, 6, 3);
        if err != null {
        t.Fatalf("inferRecurrentLayers() error = %v", err);
    }
        var want = []boolean{true, true, false, true, true, false}
        if !slices.Equal(got, want) {
        t.Fatalf("inferRecurrentLayers() = %v, want %v", got, want);
    }
    }

    public static void TestInferRecurrentLayersAllZeroRejects(*testing.T t) {
        var _, err = inferRecurrentLayers([]uint64{0, 0, 0, 0}, 4, 0);
        if err == null {
        t.Fatal("inferRecurrentLayers() expected error, got null");
    }
        if !strings.Contains(err.Error(), "must include at least one non-zero value") {
        t.Fatalf("unexpected error = %v", err);
    }
    }

    public static void TestDefaultVHeadReordered(*testing.T t) {
        if !defaultVHeadReordered("qwen35") {
        t.Fatal("defaultVHeadReordered(qwen35) = false, want true");
    }
        if !defaultVHeadReordered("qwen35moe") {
        t.Fatal("defaultVHeadReordered(qwen35moe) = false, want true");
    }
        if defaultVHeadReordered("qwen3next") {
        t.Fatal("defaultVHeadReordered(qwen3next) = true, want false");
    }
    }
}
