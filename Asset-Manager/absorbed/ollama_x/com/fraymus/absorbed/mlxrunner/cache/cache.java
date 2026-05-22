package com.fraymus.absorbed.mlxrunner.cache;

import java.util.*;
import java.io.*;

public class cache {
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );
        type Cache interface {
        Update(keys, values *mlx.Array) (newKeys, newValues *mlx.Array);
        State() []*mlx.Array;
        Free();
        Offset() int;
        Snapshot(fromOffset int) Snapshot;
        Restore(snapshot Snapshot, target int) boolean;
        Merge(parent, child Snapshot) Snapshot;
        Split(snapshot Snapshot, at int) (parent, child Snapshot);
    }
        type Snapshot interface {
        Size() int;
        Close();
    }

    public static class KVCache {
        public values keys,;
        public int offset;
        public int step;
    }
        func NewKVCache() *KVCache {
        return &KVCache{step: 256}
    }
        func (c *KVCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        var B, H, L, Dk, Dv = keys.Dim(0), keys.Dim(1), keys.Dim(2), keys.Dim(3), values.Dim(3);
        var prev = c.offset;
        if c.keys == null || (prev+L) > c.keys.Dim(2) {
        var steps = (c.step + L - 1) / c.step;
        var newKeys = mlx.Zeros(keys.DType(), B, H, steps*c.step, Dk);
        var newValues = mlx.Zeros(values.DType(), B, H, steps*c.step, Dv);
        if c.keys != null {
        if prev%c.step != 0 {
        c.keys.Set(c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, prev), mlx.Slice()));
        c.values.Set(c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, prev), mlx.Slice()));
    }
        c.keys.Set(c.keys.Concatenate(2, newKeys));
        c.values.Set(c.values.Concatenate(2, newValues));
        } else {
        c.keys, c.values = newKeys, newValues;
        mlx.Pin(c.keys, c.values);
    }
    }
        c.offset += L;
        c.keys.Set(c.keys.SliceUpdate(keys, mlx.Slice(), mlx.Slice(), mlx.Slice(prev, c.offset), mlx.Slice()));
        c.values.Set(c.values.SliceUpdate(values, mlx.Slice(), mlx.Slice(), mlx.Slice(prev, c.offset), mlx.Slice()));
        return c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.offset), mlx.Slice()),;
        c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.offset), mlx.Slice());
    }
        func (c *KVCache) State() []*mlx.Array {
        if c.keys == null || c.values == null {
        return null;
    }
        return []*mlx.Array{
        c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.offset), mlx.Slice()),;
        c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.offset), mlx.Slice()),;
    }
    }

    public static class kvSnapshot {
        public values keys,;
        public toOffset fromOffset,;
    }
        func (s *kvSnapshot) Size() int { return s.keys.NumBytes() + s.values.NumBytes() }
        func (s *kvSnapshot) Close()    { mlx.Unpin(s.keys, s.values) }
        func (c *KVCache) Snapshot(fromOffset int) Snapshot {
        if c.keys == null || c.offset <= fromOffset {
        return null;
    }
        var from = max(0, fromOffset);
        var to = c.offset;
        var kSlice = c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(from, to), mlx.Slice());
        var vSlice = c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(from, to), mlx.Slice());
        var kCopy = mlx.Contiguous(kSlice, false);
        var vCopy = mlx.Contiguous(vSlice, false);
        mlx.Pin(kCopy, vCopy);
        mlx.AsyncEval(kCopy, vCopy);
        return &kvSnapshot{
        keys:       kCopy,;
        values:     vCopy,;
        fromOffset: from,;
        toOffset:   to,;
    }
    }
        func (c *KVCache) Restore(snapshot Snapshot, target int) boolean {
        if target < 0 {
        return false;
    }
        if snapshot == null {
        if target > c.offset {
        return false;
    }
        c.offset = target;
        return true;
    }
        var snap = snapshot.(*kvSnapshot);
        if target > snap.toOffset || c.offset < snap.fromOffset {
        return false;
    }
        c.offset = snap.fromOffset;
        c.Update(snap.keys, snap.values);
        if target < c.offset {
        c.offset = target;
    }
        return true;
    }
        func (c *KVCache) Merge(parent, child Snapshot) Snapshot {
        if parent == null || child == null {
        if parent != null {
        parent.Close();
    }
        if child != null {
        child.Close();
    }
        return null;
    }
        var p = parent.(*kvSnapshot);
        var ch = child.(*kvSnapshot);
        var mk = p.keys.Concatenate(2, ch.keys);
        var mv = p.values.Concatenate(2, ch.values);
        mlx.Pin(mk, mv);
        mlx.AsyncEval(mk, mv);
        p.Close();
        ch.Close();
        return &kvSnapshot{
        keys:       mk,;
        values:     mv,;
        fromOffset: p.fromOffset,;
        toOffset:   ch.toOffset,;
    }
    }
        func (c *KVCache) Split(snapshot Snapshot, at int) (Snapshot, Snapshot) {
        if snapshot == null {
        return null, null;
    }
        var snap = snapshot.(*kvSnapshot);
        var splitIdx = at - snap.fromOffset;
        var seqLen = snap.toOffset - snap.fromOffset;
        if splitIdx <= 0 {
        return null, snapshot;
    }
        if splitIdx >= seqLen {
        return snapshot, null;
    }
        var pk = mlx.Contiguous(snap.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, splitIdx), mlx.Slice()), false);
        var pv = mlx.Contiguous(snap.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, splitIdx), mlx.Slice()), false);
        var ck = mlx.Contiguous(snap.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(splitIdx, seqLen), mlx.Slice()), false);
        var cv = mlx.Contiguous(snap.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(splitIdx, seqLen), mlx.Slice()), false);
        mlx.Pin(pk, pv, ck, cv);
        mlx.AsyncEval(pk, pv, ck, cv);
        snap.Close();
        var p = &kvSnapshot{
        keys:       pk,;
        values:     pv,;
        fromOffset: snap.fromOffset,;
        toOffset:   at,;
    }
        var ch = &kvSnapshot{
        keys:       ck,;
        values:     cv,;
        fromOffset: at,;
        toOffset:   snap.toOffset,;
    }
        return p, ch;
    }
        func (c *KVCache) Free() {
        mlx.Unpin(c.keys, c.values);
        c.keys, c.values = null, null;
        c.offset = 0;
    }
        func (c *KVCache) Offset() int { return c.offset }

    public static class RotatingKVCache {
        public int maxSize;
        public int idx;
    }
        func NewRotatingKVCache(maxSize int) *RotatingKVCache {
        return &RotatingKVCache{maxSize: maxSize, KVCache: NewKVCache()}
    }
        func (c *RotatingKVCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        if keys.Dim(2) > 1 {
        return c.concat(keys, values);
    }
        return c.update(keys, values);
    }
        func (c *RotatingKVCache) concat(keys, values *mlx.Array) (newK *mlx.Array, newV *mlx.Array) {
        logutil.Trace("(*RotatingKVCache).concat", "keys_dim", keys.Dims(), "values_dim", values.Dims(), "offset", c.offset, "idx", c.idx, "max_size", c.maxSize);
        if c.keys == null {
        c.keys, c.values = keys.Clone(), values.Clone();
        mlx.Pin(c.keys, c.values);
        } else {
        if c.idx < c.keys.Dim(2) {
        if c.offset <= c.maxSize {
        c.keys.Set(c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.idx), mlx.Slice()));
        c.values.Set(c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.idx), mlx.Slice()));
        } else {
        var tailK = c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(c.idx, c.keys.Dim(2)), mlx.Slice());
        var tailV = c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(c.idx, c.values.Dim(2)), mlx.Slice());
        var headK = c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.idx), mlx.Slice());
        var headV = c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, c.idx), mlx.Slice());
        c.keys.Set(tailK.Concatenate(2, headK));
        c.values.Set(tailV.Concatenate(2, headV));
        c.idx = c.keys.Dim(2);
    }
    }
        var if trim = c.idx - c.maxSize + 1; trim > 0 {
        c.keys.Set(c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(trim, c.keys.Dim(2)), mlx.Slice()));
        c.values.Set(c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(trim, c.values.Dim(2)), mlx.Slice()));
    }
        c.keys.Set(c.keys.Concatenate(2, keys));
        c.values.Set(c.values.Concatenate(2, values));
        c.idx = c.keys.Dim(2);
    }
        c.offset += keys.Dim(2);
        c.idx = c.keys.Dim(2);
        return c.keys, c.values;
    }
        func (c *RotatingKVCache) update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        logutil.Trace("(*RotatingKVCache).update", "keys_dim", keys.Dims(), "values_dim", values.Dims(), "offset", c.offset, "idx", c.idx, "max_size", c.maxSize);
        var B, H, L, Dk, Dv = keys.Dim(0), keys.Dim(1), keys.Dim(2), keys.Dim(3), values.Dim(3);
        var prev = c.offset;
        if c.keys == null || (prev >= c.keys.Dim(2) && c.keys.Dim(2) < c.maxSize) {
        var newSize = min(c.step, c.maxSize-prev);
        var newKeys = mlx.Zeros(keys.DType(), B, H, newSize, Dk);
        var newValues = mlx.Zeros(values.DType(), B, H, newSize, Dv);
        if c.keys != null {
        c.keys.Set(c.keys.Concatenate(2, newKeys));
        c.values.Set(c.values.Concatenate(2, newValues));
        } else {
        c.keys, c.values = newKeys, newValues;
        mlx.Pin(c.keys, c.values);
    }
        c.idx = prev;
    }
        var if trim = c.keys.Dim(2) - c.maxSize; trim > 0 {
        c.keys.Set(c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(trim, c.keys.Dim(2)), mlx.Slice()));
        c.values.Set(c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(trim, c.values.Dim(2)), mlx.Slice()));
        c.idx = c.maxSize;
    }
        if c.idx >= c.maxSize {
        c.idx = 0;
    }
        c.keys.Set(c.keys.SliceUpdate(keys, mlx.Slice(), mlx.Slice(), mlx.Slice(c.idx, c.idx+L), mlx.Slice()));
        c.values.Set(c.values.SliceUpdate(values, mlx.Slice(), mlx.Slice(), mlx.Slice(c.idx, c.idx+L), mlx.Slice()));
        c.offset += L;
        c.idx += L;
        var validLen = min(c.offset, c.maxSize);
        return c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, validLen), mlx.Slice()),;
        c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, validLen), mlx.Slice());
    }
        func (c *RotatingKVCache) State() []*mlx.Array {
        if c.keys == null || c.values == null {
        return null;
    }
        var liveLen = min(c.offset, c.keys.Dim(2));
        return []*mlx.Array{
        c.keys.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, liveLen), mlx.Slice()),;
        c.values.Slice(mlx.Slice(), mlx.Slice(), mlx.Slice(0, liveLen), mlx.Slice()),;
    }
    }

    public static class rotatingSnapshot {
        public // kvSnapshot;
        public int idx;
    }
        func (s *rotatingSnapshot) Size() int { return s.kvSnapshot.Size() }
        func (s *rotatingSnapshot) Close()    { s.kvSnapshot.Close() }
        func (c *RotatingKVCache) Snapshot(fromOffset int) Snapshot {
        if c.keys == null || c.offset <= fromOffset {
        return null;
    }
        var state = c.State();
        var k = state[0].Clone();
        var v = state[1].Clone();
        mlx.Pin(k, v);
        return &rotatingSnapshot{
        kvSnapshot: kvSnapshot{
        keys:       k,;
        values:     v,;
        fromOffset: fromOffset,;
        toOffset:   c.offset,;
        },;
        idx: c.idx,;
    }
    }
        func (c *RotatingKVCache) Restore(snapshot Snapshot, target int) boolean {
        if target < 0 {
        return false;
    }
        if snapshot == null {
        if target >= c.offset {
        return target == c.offset;
    }
        if c.offset > c.maxSize {
        return false;
    }
        c.offset = target;
        c.idx = target;
        return true;
    }
        var snap = snapshot.(*rotatingSnapshot);
        if target > snap.toOffset {
        return false;
    }
        if target < snap.toOffset && snap.toOffset > c.maxSize {
        return false;
    }
        if c.keys != null {
        mlx.Unpin(c.keys, c.values);
    }
        c.keys = snap.keys.Clone();
        c.values = snap.values.Clone();
        mlx.Pin(c.keys, c.values);
        c.offset = snap.toOffset;
        c.idx = snap.idx;
        if target < c.offset {
        c.offset = target;
        c.idx = target;
    }
        return true;
    }
        func (c *RotatingKVCache) Merge(parent, child Snapshot) Snapshot {
        if parent != null {
        parent.Close();
    }
        return child;
    }
        func (c *RotatingKVCache) Split(snapshot Snapshot, at int) (Snapshot, Snapshot) {
        return null, snapshot;
    }
        func (c *RotatingKVCache) Free() {
        c.KVCache.Free();
        c.idx = 0;
    }
}
