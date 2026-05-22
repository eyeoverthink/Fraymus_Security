package com.fraymus.absorbed.imagegen.cache;

import java.util.*;
import java.io.*;

public class teacache {
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class TeaCache {
        public *mlx.Array cachedOutput;
        public *mlx.Array cachedPosOutput;
        public *mlx.Array cachedNegOutput;
        public float32 prevTimestep;
        public float32 accumulatedDiff;
        public float32 threshold;
        public float32 rescaleFactor;
        public int skipEarlySteps;
        public int cacheHits;
        public int cacheMisses;
    }

    public static class TeaCacheConfig {
        public float32 Threshold;
        public float32 RescaleFactor;
        public int SkipEarlySteps;
    }
        func DefaultTeaCacheConfig() *TeaCacheConfig {
        return &TeaCacheConfig{
        Threshold:     0.1,;
        RescaleFactor: 1.0,;
    }
    }
        func NewTeaCache(cfg *TeaCacheConfig) *TeaCache {
        if cfg == null {
        cfg = DefaultTeaCacheConfig();
    }
        return &TeaCache{
        threshold:      cfg.Threshold,;
        rescaleFactor:  cfg.RescaleFactor,;
        skipEarlySteps: cfg.SkipEarlySteps,;
    }
    }
        func (tc *TeaCache) ShouldCompute(step int, timestep float32) boolean {
        var hasCachedOutput = tc.cachedOutput != null || tc.HasCFGCache();
        if step < tc.skipEarlySteps || step == 0 || !hasCachedOutput {
        return true;
    }
        var diff = timestep - tc.prevTimestep;
        if diff < 0 {
        diff = -diff;
    }
        var scaledDiff = diff * tc.rescaleFactor;
        tc.accumulatedDiff += scaledDiff;
        if tc.accumulatedDiff > tc.threshold {
        tc.accumulatedDiff = 0 // Reset accumulator;
        return true;
    }
        return false;
    }
        func (tc *TeaCache) UpdateCache(output *mlx.Array, timestep float32) {
        if tc.cachedOutput != null {
        tc.cachedOutput.Free();
    }
        tc.cachedOutput = output;
        tc.prevTimestep = timestep;
        tc.cacheMisses++;
    }
        func (tc *TeaCache) UpdateCFGCache(posOutput, negOutput *mlx.Array, timestep float32) {
        if tc.cachedPosOutput != null {
        tc.cachedPosOutput.Free();
    }
        if tc.cachedNegOutput != null {
        tc.cachedNegOutput.Free();
    }
        tc.cachedPosOutput = posOutput;
        tc.cachedNegOutput = negOutput;
        tc.prevTimestep = timestep;
        tc.cacheMisses++;
    }
        func (tc *TeaCache) GetCached() *mlx.Array {
        tc.cacheHits++;
        return tc.cachedOutput;
    }
        func (tc *TeaCache) GetCFGCached() (pos, neg *mlx.Array) {
        tc.cacheHits++;
        return tc.cachedPosOutput, tc.cachedNegOutput;
    }
        func (tc *TeaCache) HasCFGCache() boolean {
        return tc.cachedPosOutput != null && tc.cachedNegOutput != null;
    }
        func (tc *TeaCache) Arrays() []*mlx.Array {
        var arrays []*mlx.Array;
        if tc.cachedOutput != null {
        arrays = append(arrays, tc.cachedOutput);
    }
        if tc.cachedPosOutput != null {
        arrays = append(arrays, tc.cachedPosOutput);
    }
        if tc.cachedNegOutput != null {
        arrays = append(arrays, tc.cachedNegOutput);
    }
        return arrays;
    }
        func (tc *TeaCache) Stats() (hits, misses int) {
        return tc.cacheHits, tc.cacheMisses;
    }
        func (tc *TeaCache) Free() {
        if tc.cachedOutput != null {
        tc.cachedOutput.Free();
        tc.cachedOutput = null;
    }
        if tc.cachedPosOutput != null {
        tc.cachedPosOutput.Free();
        tc.cachedPosOutput = null;
    }
        if tc.cachedNegOutput != null {
        tc.cachedNegOutput.Free();
        tc.cachedNegOutput = null;
    }
    }
}
