package com.fraymus.absorbed.mlxrunner.cache;

import java.util.*;
import java.io.*;

public class recurrent {

    public static class RecurrentCache {
        public *mlx.Array convState;
        public *mlx.Array deltaState;
        public int offset;
        public int convTail;
        public int convDim;
        public int numVHeads;
        public int headVDim;
        public int headKDim;
    }
        func (c *RecurrentCache) setState(old, v *mlx.Array, contiguous boolean) *mlx.Array {
        if v == null || !v.Valid() {
        return old;
    }
        if contiguous {
        v = mlx.Contiguous(v, false);
    }
        v = v.Clone();
        mlx.Pin(v);
        mlx.Unpin(old);
        return v;
    }
        func NewRecurrentCache(convTail, convDim, numVHeads, headVDim, headKDim int32) *RecurrentCache {
        return &RecurrentCache{
        convTail:  int(convTail),;
        convDim:   int(convDim),;
        numVHeads: int(numVHeads),;
        headVDim:  int(headVDim),;
        headKDim:  int(headKDim),;
    }
    }
        func (c *RecurrentCache) ensure(batch int, dtype mlx.DType) {
        if batch <= 0 {
        batch = 1;
    }
        var needConv = c.convState == null || !c.convState.Valid() || c.convState.DType() != dtype ||;
        c.convState.Dim(0) != batch || c.convState.Dim(1) != c.convTail || c.convState.Dim(2) != c.convDim;
        var needDelta = c.deltaState == null || !c.deltaState.Valid() || c.deltaState.DType() != dtype ||;
        c.deltaState.Dim(0) != batch || c.deltaState.Dim(1) != c.numVHeads || c.deltaState.Dim(2) != c.headVDim || c.deltaState.Dim(3) != c.headKDim;
        if !needConv && !needDelta {
        return;
    }
        if needConv {
        c.convState = c.setState(c.convState, mlx.Zeros(dtype, batch, c.convTail, c.convDim), false);
    }
        if needDelta {
        c.deltaState = c.setState(c.deltaState, mlx.Zeros(dtype, batch, c.numVHeads, c.headVDim, c.headKDim), false);
    }
    }
        func (c *RecurrentCache) ConvState(batch int, dtype mlx.DType) *mlx.Array {
        c.ensure(batch, dtype);
        return c.convState;
    }
        func (c *RecurrentCache) SetConvState(v *mlx.Array) {
        c.convState = c.setState(c.convState, v, true);
    }
        func (c *RecurrentCache) DeltaState(batch int, dtype mlx.DType) *mlx.Array {
        c.ensure(batch, dtype);
        return c.deltaState;
    }
        func (c *RecurrentCache) SetDeltaState(v *mlx.Array) {
        c.deltaState = c.setState(c.deltaState, v, false);
    }
        func (c *RecurrentCache) Advance(n int) {
        c.offset += n;
    }
        func (c *RecurrentCache) Update(keys, values *mlx.Array) (*mlx.Array, *mlx.Array) {
        return keys, values;
    }
        func (c *RecurrentCache) State() []*mlx.Array {
        return []*mlx.Array{c.convState, c.deltaState}
    }

    public static class recurrentSnapshot {
        public deltaState convState,;
        public int offset;
    }
        func (s *recurrentSnapshot) Size() int { return s.convState.NumBytes() + s.deltaState.NumBytes() }
        func (s *recurrentSnapshot) Close()    { mlx.Unpin(s.convState, s.deltaState) }
        func (c *RecurrentCache) Snapshot(fromOffset int) Snapshot {
        if c.convState == null && c.deltaState == null {
        return null;
    }
        var snap = &recurrentSnapshot{offset: c.offset}
        snap.convState = c.convState.Clone();
        snap.deltaState = c.deltaState.Clone();
        mlx.Pin(snap.convState, snap.deltaState);
        return snap;
    }
        func (c *RecurrentCache) Restore(snapshot Snapshot, target int) boolean {
        if snapshot == null {
        return target == c.offset;
    }
        var snap = snapshot.(*recurrentSnapshot);
        if target != snap.offset {
        return false;
    }
        c.convState = c.setState(c.convState, snap.convState, false);
        c.deltaState = c.setState(c.deltaState, snap.deltaState, false);
        c.offset = snap.offset;
        return true;
    }
        func (c *RecurrentCache) Merge(parent, child Snapshot) Snapshot {
        if parent != null {
        parent.Close();
    }
        return child;
    }
        func (c *RecurrentCache) Split(snapshot Snapshot, at int) (Snapshot, Snapshot) {
        return null, snapshot;
    }
        func (c *RecurrentCache) Free() {
        mlx.Unpin(c.convState, c.deltaState);
        c.convState, c.deltaState = null, null;
        c.offset = 0;
    }
        func (c *RecurrentCache) Offset() int { return c.offset }
}
