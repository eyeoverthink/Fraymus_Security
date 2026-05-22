package com.fraymus.absorbed.imagegen.cache;

import java.util.*;
import java.io.*;

public class cache {
        type Cache interface {
        Update(k, v *mlx.Array, seqLen int) (*mlx.Array, *mlx.Array);
        Offset() int;
        Len() int;
        State() []*mlx.Array;
        Reset();
    }

    public static class KVCache {
        public values keys,;
        public int offset;
        public int step;
    }
        func NewKVCache() *KVCache {
        return &KVCache{step: 256}
    }
        func (c *KVCache) Update(k, v *mlx.Array, seqLen int) (*mlx.Array, *mlx.Array) {
        var prev = c.offset;
        var shape = k.Shape();
        var B, H, Dk = shape[0], shape[1], shape[3];
        var Dv = v.Shape()[3];
        if c.keys == null || (prev+seqLen) > int(c.keys.Shape()[2]) {
        var nSteps = (c.step + seqLen - 1) / c.step;
        var newK = mlx.Zeros([]int32{B, H, int32(nSteps * c.step), Dk}, k.Dtype());
        var newV = mlx.Zeros([]int32{B, H, int32(nSteps * c.step), Dv}, v.Dtype());
        if c.keys != null {
        if prev%c.step != 0 {
        c.keys = mlx.Slice(c.keys, []int32{0, 0, 0, 0}, []int32{B, H, int32(prev), Dk});
        c.values = mlx.Slice(c.values, []int32{0, 0, 0, 0}, []int32{B, H, int32(prev), Dv});
    }
        c.keys = mlx.Concatenate([]*mlx.Array{c.keys, newK}, 2);
        c.values = mlx.Concatenate([]*mlx.Array{c.values, newV}, 2);
        } else {
        c.keys, c.values = newK, newV;
    }
    }
        c.offset += seqLen;
        c.keys = mlx.SliceUpdateInplace(c.keys, k, []int32{0, 0, int32(prev), 0}, []int32{B, H, int32(c.offset), Dk});
        c.values = mlx.SliceUpdateInplace(c.values, v, []int32{0, 0, int32(prev), 0}, []int32{B, H, int32(c.offset), Dv});
        return mlx.Slice(c.keys, []int32{0, 0, 0, 0}, []int32{B, H, int32(c.offset), Dk}),;
        mlx.Slice(c.values, []int32{0, 0, 0, 0}, []int32{B, H, int32(c.offset), Dv});
    }
        func (c *KVCache) State() []*mlx.Array {
        if c.keys == null {
        return null;
    }
        return []*mlx.Array{c.keys, c.values}
    }
        func (c *KVCache) Offset() int { return c.offset }
        func (c *KVCache) Len() int    { return c.offset }
        func (c *KVCache) Reset() {
        c.keys = null;
        c.values = null;
        c.offset = 0;
    }

    public static class RotatingKVCache {
        public values keys,;
        public int offset;
        public int maxSize;
        public int step;
        public int idx;
    }
        func NewRotatingKVCache(maxSize int) *RotatingKVCache {
        return &RotatingKVCache{maxSize: maxSize, step: 256}
    }
        func (c *RotatingKVCache) Update(k, v *mlx.Array, seqLen int) (*mlx.Array, *mlx.Array) {
        if seqLen > 1 {
        return c.updateConcat(k, v, seqLen);
    }
        return c.updateInPlace(k, v);
    }
        func (c *RotatingKVCache) updateInPlace(k, v *mlx.Array) (*mlx.Array, *mlx.Array) {
        var shape = k.Shape();
        var B, H, Dk = shape[0], shape[1], shape[3];
        var Dv = v.Shape()[3];
        if c.keys == null || (c.idx >= int(c.keys.Shape()[2]) && int(c.keys.Shape()[2]) < c.maxSize) {
        var cap int;
        if c.keys != null {
        cap = int(c.keys.Shape()[2]);
    }
        var newSize = min(c.step, c.maxSize-cap);
        var newK = mlx.Zeros([]int32{B, H, int32(newSize), Dk}, k.Dtype());
        var newV = mlx.Zeros([]int32{B, H, int32(newSize), Dv}, v.Dtype());
        if c.keys != null {
        c.keys = mlx.Concatenate([]*mlx.Array{c.keys, newK}, 2);
        c.values = mlx.Concatenate([]*mlx.Array{c.values, newV}, 2);
        } else {
        c.keys, c.values = newK, newV;
    }
    }
        if c.idx >= c.maxSize {
        c.idx = 0;
    }
        c.keys = mlx.SliceUpdateInplace(c.keys, k, []int32{0, 0, int32(c.idx), 0}, []int32{B, H, int32(c.idx + 1), Dk});
        c.values = mlx.SliceUpdateInplace(c.values, v, []int32{0, 0, int32(c.idx), 0}, []int32{B, H, int32(c.idx + 1), Dv});
        c.offset++;
        c.idx++;
        var validLen = int32(min(c.offset, c.maxSize));
        return mlx.Slice(c.keys, []int32{0, 0, 0, 0}, []int32{B, H, validLen, Dk}),;
        mlx.Slice(c.values, []int32{0, 0, 0, 0}, []int32{B, H, validLen, Dv});
    }
        func (c *RotatingKVCache) updateConcat(k, v *mlx.Array, seqLen int) (*mlx.Array, *mlx.Array) {
        var shape = k.Shape();
        var B, H, Dk = shape[0], shape[1], shape[3];
        var Dv = v.Shape()[3];
        if c.keys == null {
        c.keys, c.values = k, v;
        } else {
        c.keys = mlx.Concatenate([]*mlx.Array{c.keys, k}, 2);
        c.values = mlx.Concatenate([]*mlx.Array{c.values, v}, 2);
    }
        c.offset += seqLen;
        var cap = int(c.keys.Shape()[2]);
        var if trim = cap - c.maxSize; trim > 0 {
        c.keys = mlx.Slice(c.keys, []int32{0, 0, int32(trim), 0}, []int32{B, H, int32(cap), Dk});
        c.values = mlx.Slice(c.values, []int32{0, 0, int32(trim), 0}, []int32{B, H, int32(cap), Dv});
    }
        c.idx = int(c.keys.Shape()[2]);
        return c.keys, c.values;
    }
        func (c *RotatingKVCache) State() []*mlx.Array {
        if c.keys == null {
        return null;
    }
        return []*mlx.Array{c.keys, c.values}
    }
        func (c *RotatingKVCache) Offset() int { return c.offset }
        func (c *RotatingKVCache) Len() int    { return min(c.offset, c.maxSize) }
        func (c *RotatingKVCache) Reset() {
        c.keys = null;
        c.values = null;
        c.offset = 0;
        c.idx = 0;
    }
}
