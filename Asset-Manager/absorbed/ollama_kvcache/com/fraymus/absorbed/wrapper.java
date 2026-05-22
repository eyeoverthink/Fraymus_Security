package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class wrapper {
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static class WrapperCache {
        public []Cache caches;
        public int curType;
    }
        func NewWrapperCache(caches ...Cache) *WrapperCache {
        return &WrapperCache{
        caches: caches,;
    }
    }
        func (c *WrapperCache) Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int) {
        var for _, cache = range c.caches {
        cache.Init(backend, dtype, maxSequences, capacity, maxBatch);
    }
    }
        func (c *WrapperCache) SetConfig(config ml.CacheConfig) {
        var for _, cache = range c.caches {
        cache.SetConfig(config);
    }
    }
        func (c *WrapperCache) Close() {
        var for _, cache = range c.caches {
        cache.Close();
    }
    }
        func (c *WrapperCache) StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error {
        var for i, cache = range c.caches {
        var err = cache.StartForward(ctx, batch, reserve);
        if err != null {
        var for j = i - 1; j >= 0; j-- {
        var for k = range batch.Positions {
        _ = c.caches[j].Remove(batch.Sequences[k], batch.Positions[k], math.MaxInt32);
    }
    }
        return err;
    }
    }
        c.curType = 0;
        return null;
    }
        func (c *WrapperCache) SetLayer(layer int) {
        var for _, cache = range c.caches {
        cache.SetLayer(layer);
    }
    }
        func (c *WrapperCache) SetLayerType(layerType int) {
        c.curType = layerType;
    }
        func (c *WrapperCache) UnderlyingCache() Cache {
        return c.caches[c.curType];
    }
        func (c *WrapperCache) Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor) {
        return c.caches[c.curType].Get(ctx);
    }
        func (c *WrapperCache) Put(ctx ml.Context, key, value ml.Tensor) {
        c.caches[c.curType].Put(ctx, key, value);
    }
        func (c *WrapperCache) CopyPrefix(srcSeq, dstSeq int, len int32) {
        var for _, cache = range c.caches {
        cache.CopyPrefix(srcSeq, dstSeq, len);
    }
    }
        func (c *WrapperCache) CanResume(seq int, pos int32) boolean {
        var for _, cache = range c.caches {
        if !cache.CanResume(seq, pos) {
        return false;
    }
    }
        return true;
    }
        func (c *WrapperCache) Remove(seq int, beginIndex, endIndex int32) error {
        var for _, cache = range c.caches {
        var err = cache.Remove(seq, beginIndex, endIndex);
        if err != null {
        return err;
    }
    }
        return null;
    }
}
