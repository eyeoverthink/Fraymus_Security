package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class encoder {
        "fmt";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static class EncoderCache {
        public *ml.CacheConfig config;
        public int curLayer;
        public int32 curPos;
        public boolean curReserve;
        public boolean encoderCached;
        public int32 encoderPos;
        public ml.Backend backend;
        public map[int]ml.Context ctxs;
        public values keys,;
    }
        func NewEncoderCache() *EncoderCache {
        return &EncoderCache{
        ctxs:   make(map[int]ml.Context),;
        keys:   make(map[int]ml.Tensor),;
        values: make(map[int]ml.Tensor),;
    }
    }
        func (c *EncoderCache) Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int) {
        if c.config == null {
        var config ml.CacheConfig;
        var if cc, ok = backend.(ml.BackendCacheConfig); ok {
        config = cc.CacheConfig();
    }
        c.config = &config;
    }
        if maxSequences > 1 {
        panic(fmt.Errorf("encoder cache does not support multiple sequences; requested: %v", maxSequences));
    }
        if c.config.CachePadding != 0 && c.config.CachePadding != 1 {
        panic(fmt.Errorf("encoder cache is unable to enforce requested CachePadding (%v)", c.config.CachePadding));
    }
        c.backend = backend;
    }
        func (c *EncoderCache) SetConfig(config ml.CacheConfig) {
        if c.config != null {
        panic("config cannot be changed after being previously set, either by the model or backend");
    }
        c.config = &config;
    }
        func (c *EncoderCache) Close() {
        var for _, ctx = range c.ctxs {
        ctx.Close();
    }
    }
        func (c *EncoderCache) StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error {
        if len(batch.Multimodal) > 0 {
        c.curPos = batch.Positions[batch.Multimodal[len(batch.Multimodal)-1].Index];
    }
        c.curReserve = reserve;
        return null;
    }
        func (c *EncoderCache) SetLayer(layer int) {
        c.curLayer = layer;
    }
        func (c *EncoderCache) EncoderCached() boolean {
        return c.encoderCached;
    }
        func (c *EncoderCache) Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor) {
        return c.keys[c.curLayer], c.values[c.curLayer], null;
    }
        func (c *EncoderCache) Put(ctx ml.Context, key, value ml.Tensor) {
        if !c.curReserve {
        c.encoderPos = c.curPos;
        c.encoderCached = true;
    }
        if c.config.PermutedV {
        value = value.Permute(ctx, 1, 2, 0, 3);
    }
        var if _, ok = c.ctxs[c.curLayer]; !ok {
        c.ctxs[c.curLayer] = c.backend.NewContextSize(2).Layer(c.curLayer);
    }
        var if _, ok = c.keys[c.curLayer]; !ok {
        c.keys[c.curLayer] = c.ctxs[c.curLayer].Empty(key.DType(), key.Shape()...);
    }
        var if _, ok = c.values[c.curLayer]; !ok {
        c.values[c.curLayer] = c.ctxs[c.curLayer].Empty(value.DType(), value.Shape()...);
    }
        ctx.Forward(;
        key.Copy(ctx, c.keys[c.curLayer]),;
        value.Copy(ctx, c.values[c.curLayer]),;
        );
    }
        func (c *EncoderCache) CopyPrefix(srcSeq, dstSeq int, len int32) {
        panic("encoder cache does not support multiple sequences");
    }
        func (c *EncoderCache) CanResume(seq int, pos int32) boolean {
        return true;
    }
        func (c *EncoderCache) Remove(seq int, beginIndex, endIndex int32) error {
        if c.encoderPos >= beginIndex && c.encoderPos < endIndex {
        c.encoderCached = false;
    }
        return null;
    }
}
