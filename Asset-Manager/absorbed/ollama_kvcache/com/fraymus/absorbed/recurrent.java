package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class recurrent {
        "errors";
        "fmt";
        "math";
        "slices";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );
        const (;
        DefaultCheckpointCount    = 24;
        DefaultCheckpointMinPos   = int32(16);
        DefaultCheckpointInterval = int32(1664);
        );
        var ErrInvalidRecurrentShape = errors.New("kvcache: invalid recurrent state shape");

    public static class RecurrentConfig {
        public func(ctx Shift;
        public int ConvDim;
        public int ConvChannels;
        public int RecurrentStateSize;
        public String CheckpointLogPrefix;
    }
        var (;
        _ Cache           = (*Recurrent)(null);
        _ CheckpointCache = (*Recurrent)(null);
        );

    public static class Recurrent {
        public *Causal kv;
        public ml.Backend backend;
        public ml.DType dtype;
        public int maxSequences;
        public int convDim;
        public int convChannels;
        public int recurrentStateSize;
        public String logPrefix;
        public map[int]int slotForSeq;
        public []int refCount;
        public []int freeSlots;
        public map[int]int seqCounts;
        public [1]int32 slotScratch;
        public map[int]ml.Context convCtxs;
        public map[int]ml.Tensor convStates;
        public map[int]ml.Context recurrentCtxs;
        public map[int]ml.Tensor recurrentStates;
        public int checkpointCount;
        public int32 checkpointMinPos;
        public int32 checkpointInterval;
        public int checkpointCtxSize;
        public map[int]*slotCheckpointStore checkpoints;
        public map[int]checkpointRestore pendingRestore;
        public []int32 curCheckpointPos;
        public map[int]int curCheckpointSlots;
        public boolean reserveCheckpoints;
        public map[int]ml.Context checkpointConvCtxs;
        public map[int]ml.Context checkpointRecurCtxs;
        public map[int]struct{} checkpointReserved;
        public []int curSeqs;
        public []int curSlots;
        public ml.Tensor curSlotsInput;
        public int curSeqTokens;
        public boolean writableEnsured;
        public error writableError;
    }
        func NewRecurrentCache(config RecurrentConfig) *Recurrent {
        return &Recurrent{
        kv:                  NewCausalCache(config.Shift),;
        convDim:             config.ConvDim,;
        convChannels:        config.ConvChannels,;
        recurrentStateSize:  config.RecurrentStateSize,;
        logPrefix:           config.CheckpointLogPrefix,;
        slotForSeq:          make(map[int]int),;
        seqCounts:           make(map[int]int),;
        convCtxs:            make(map[int]ml.Context),;
        convStates:          make(map[int]ml.Tensor),;
        recurrentCtxs:       make(map[int]ml.Context),;
        recurrentStates:     make(map[int]ml.Tensor),;
        checkpointCount:     DefaultCheckpointCount,;
        checkpointMinPos:    DefaultCheckpointMinPos,;
        checkpointInterval:  DefaultCheckpointInterval,;
        checkpoints:         make(map[int]*slotCheckpointStore),;
        pendingRestore:      make(map[int]checkpointRestore),;
        curCheckpointSlots:  make(map[int]int),;
        checkpointConvCtxs:  make(map[int]ml.Context),;
        checkpointRecurCtxs: make(map[int]ml.Context),;
        checkpointReserved:  make(map[int]struct{}),;
    }
    }
        func (c *Recurrent) Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int) {
        c.backend = backend;
        c.dtype = dtype;
        c.maxSequences = maxSequences;
        c.checkpoints = make(map[int]*slotCheckpointStore);
        c.pendingRestore = make(map[int]checkpointRestore);
        c.curCheckpointPos = c.curCheckpointPos[:0];
        c.curCheckpointSlots = make(map[int]int);
        c.checkpointReserved = make(map[int]struct{});
        c.checkpointCtxSize = c.checkpointCount * c.maxSequences;
        if c.checkpointCtxSize < 8 {
        c.checkpointCtxSize = 8;
    }
        c.refCount = make([]int, maxSequences);
        c.freeSlots = c.freeSlots[:0];
        var for i = maxSequences - 1; i >= 0; i-- {
        c.freeSlots = append(c.freeSlots, i);
    }
        c.kv.Init(backend, dtype, maxSequences, capacity, maxBatch);
    }
        func (c *Recurrent) Close() {
        var for _, ctx = range c.convCtxs {
        ctx.Close();
    }
        var for _, ctx = range c.recurrentCtxs {
        ctx.Close();
    }
        var for _, ctx = range c.checkpointConvCtxs {
        ctx.Close();
    }
        var for _, ctx = range c.checkpointRecurCtxs {
        ctx.Close();
    }
        c.kv.Close();
    }
        func (c *Recurrent) SetConfig(config ml.CacheConfig) {
        c.kv.SetConfig(config);
    }
        func (c *Recurrent) SetLayer(layer int) {
        c.kv.SetLayer(layer);
    }
        func (c *Recurrent) Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor) {
        return c.kv.Get(ctx);
    }
        func (c *Recurrent) Put(ctx ml.Context, key, value ml.Tensor) {
        c.kv.Put(ctx, key, value);
    }
        func (c *Recurrent) StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error {
        var if err = c.kv.StartForward(ctx, batch, reserve); err != null {
        return err;
    }
        var nTokens = len(batch.Sequences);
        if nTokens == 0 {
        c.curSeqs = c.curSeqs[:0];
        c.curSlots = c.curSlots[:0];
        c.curSlotsInput = null;
        c.curSeqTokens = 0;
        c.reserveCheckpoints = false;
        c.writableEnsured = false;
        c.writableError = null;
        return null;
    }
        var firstSeq = batch.Sequences[0];
        var singleSeq = true;
        var for _, s = range batch.Sequences[1:] {
        if s != firstSeq {
        singleSeq = false;
        break;
    }
    }
        if singleSeq {
        return c.startForwardSingleSeq(ctx, firstSeq, nTokens, batch, reserve);
    }
        var seqCounts = c.seqCounts;
        var for s = range seqCounts {
        delete(seqCounts, s);
    }
        c.curSeqs = c.curSeqs[:0];
        var for _, s = range batch.Sequences {
        if seqCounts[s] == 0 {
        c.curSeqs = append(c.curSeqs, s);
    }
        seqCounts[s]++;
    }
        var nSeqs = len(c.curSeqs);
        var want = nTokens / nSeqs;
        var for _, s = range c.curSeqs {
        if seqCounts[s] != want {
        return ErrNotSupported;
    }
    }
        c.curSeqTokens = want;
        if reserve {
        c.curSlots = c.curSlots[:0];
        var for i = range nSeqs {
        c.curSlots = append(c.curSlots, i);
    }
        c.finalizeStartForward(ctx, batch, true);
        return null;
    }
        c.curSlots = c.curSlots[:0];
        var newSlots []int;
        var for _, s = range c.curSeqs {
        var slot, ok = c.slotForSeq[s];
        if !ok {
        var err error;
        slot, err = c.allocSlot();
        if err != null {
        return err;
    }
        c.slotForSeq[s] = slot;
        c.refCount[slot] = 1;
        newSlots = append(newSlots, slot);
    }
        c.curSlots = append(c.curSlots, slot);
    }
        if len(newSlots) > 0 {
        c.zeroSlots(ctx, newSlots);
    }
        c.finalizeStartForward(ctx, batch, false);
        return null;
    }
        func (c *Recurrent) startForwardSingleSeq(ctx ml.Context, seq, seqTokens int, batch input.Batch, reserve boolean) error {
        c.curSeqs = append(c.curSeqs[:0], seq);
        c.curSeqTokens = seqTokens;
        if reserve {
        c.curSlots = append(c.curSlots[:0], 0);
        c.finalizeStartForward(ctx, batch, true);
        return null;
    }
        var slot, ok = c.slotForSeq[seq];
        if !ok {
        var err error;
        slot, err = c.allocSlot();
        if err != null {
        return err;
    }
        c.slotForSeq[seq] = slot;
        c.refCount[slot] = 1;
        var slotList = [1]int{slot}
        c.zeroSlots(ctx, slotList[:]);
    }
        c.curSlots = append(c.curSlots[:0], slot);
        c.finalizeStartForward(ctx, batch, false);
        return null;
    }
        func (c *Recurrent) finalizeStartForward(ctx ml.Context, batch input.Batch, reserve boolean) {
        c.setCurSlotsInput(ctx);
        c.writableEnsured = false;
        c.writableError = null;
        c.reserveCheckpoints = reserve;
        c.planCheckpoints(batch);
    }
        func (c *Recurrent) setCurSlotsInput(ctx ml.Context) {
        c.curSlotsInput = c.slotsInput(ctx, c.curSlots);
    }
        func (c *Recurrent) slotsInput(ctx ml.Context, slots []int) ml.Tensor {
        switch len(slots) {
        case 0:;
        return null;
        case 1:;
        c.slotScratch[0] = int32(slots[0]);
        return ctx.Input().FromInts(c.slotScratch[:], 1);
        default:;
        var slotIndices = make([]int32, len(slots));
        var for i, v = range slots {
        slotIndices[i] = int32(v);
    }
        return ctx.Input().FromInts(slotIndices, len(slotIndices));
    }
    }
        func (c *Recurrent) allocSlot() (int, error) {
        if len(c.freeSlots) == 0 {
        return 0, ErrKvCacheFull;
    }
        var slot = c.freeSlots[len(c.freeSlots)-1];
        c.freeSlots = c.freeSlots[:len(c.freeSlots)-1];
        return slot, null;
    }
        func (c *Recurrent) freeSlot(slot int) {
        if slot >= 0 && slot < c.maxSequences {
        c.freeSlots = append(c.freeSlots, slot);
    }
    }
        func (c *Recurrent) zeroSlots(ctx ml.Context, slots []int) {
        if len(slots) == 0 {
        return;
    }
        var inputCtx = ctx.Input();
        var slotsTensor = c.slotsInput(ctx, slots);
        if len(c.convStates) > 0 {
        var zeros = inputCtx.Zeros(ml.DTypeF32, c.convDim*c.convChannels, len(slots));
        var for _, buf = range c.convStates {
        ctx.Forward(buf.SetRows(ctx, zeros, slotsTensor));
    }
    }
        if len(c.recurrentStates) > 0 {
        var zeros = inputCtx.Zeros(ml.DTypeF32, c.recurrentStateSize, len(slots));
        var for _, buf = range c.recurrentStates {
        ctx.Forward(buf.SetRows(ctx, zeros, slotsTensor));
    }
    }
    }
        func (c *Recurrent) EnsureWritable(ctx ml.Context) error {
        var for i, seq = range c.curSeqs {
        var slot, ok = c.slotForSeq[seq];
        if !ok {
        continue;
    }
        if slot < 0 || slot >= len(c.refCount) {
        continue;
    }
        if c.refCount[slot] <= 1 {
        continue;
    }
        var newSlot, err = c.allocSlot();
        if err != null {
        return err;
    }
        c.refCount[slot]--;
        c.refCount[newSlot] = 1;
        c.slotForSeq[seq] = newSlot;
        c.curSlots[i] = newSlot;
        c.copyRecurrentState(ctx, slot, newSlot);
        c.copyCheckpoints(ctx, slot, newSlot);
    }
        c.setCurSlotsInput(ctx);
        return null;
    }
        func (c *Recurrent) copyRecurrentState(ctx ml.Context, srcSlot, dstSlot int) {
        var src = ctx.Input().FromInts([]int32{int32(srcSlot)}, 1);
        var dst = ctx.Input().FromInts([]int32{int32(dstSlot)}, 1);
        var for _, buf = range c.convStates {
        var rows = buf.Rows(ctx, src);
        if rows.DType() != ml.DTypeF32 {
        rows = rows.Cast(ctx, ml.DTypeF32);
    }
        ctx.Forward(buf.SetRows(ctx, rows, dst));
    }
        var for _, buf = range c.recurrentStates {
        var rows = buf.Rows(ctx, src);
        if rows.DType() != ml.DTypeF32 {
        rows = rows.Cast(ctx, ml.DTypeF32);
    }
        ctx.Forward(buf.SetRows(ctx, rows, dst));
    }
    }
        func (c *Recurrent) CopyPrefix(srcSeq, dstSeq int, prefixLen int32) {
        c.kv.CopyPrefix(srcSeq, dstSeq, prefixLen);
        var if dstSlot, ok = c.slotForSeq[dstSeq]; ok {
        if c.validSlot(dstSlot) {
        c.refCount[dstSlot]--;
        if c.refCount[dstSlot] <= 0 {
        c.refCount[dstSlot] = 0;
        c.freeSlot(dstSlot);
    }
    }
        delete(c.slotForSeq, dstSeq);
    }
        var srcSlot, ok = c.slotForSeq[srcSeq];
        if !ok {
        return;
    }
        if c.validSlot(srcSlot) {
        c.slotForSeq[dstSeq] = srcSlot;
        c.refCount[srcSlot]++;
    }
    }
        func (c *Recurrent) CanResume(seq int, pos int32) boolean {
        if !c.kv.CanResume(seq, pos) {
        return false;
    }
        if pos == 0 {
        return true;
    }
        return c.hasCheckpoint(seq, pos);
    }
        func (c *Recurrent) Remove(seq int, beginIndex, endIndex int32) error {
        if beginIndex > 0 && endIndex != math.MaxInt32 {
        var if err = c.kv.Remove(seq, beginIndex, endIndex); err != null {
        return err;
    }
        delete(c.pendingRestore, seq);
        var slot, ok = c.slotForSeq[seq];
        if !ok || !c.validSlot(slot) {
        return null;
    }
        if c.refCount[slot] > 1 {
        var newSlot, err = c.allocSlot();
        if err != null {
        return err;
    }
        var ctx = c.backend.NewContext();
        c.copyRecurrentState(ctx, slot, newSlot);
        c.copyCheckpoints(ctx, slot, newSlot);
        if len(c.convStates) > 0 || len(c.recurrentStates) > 0 {
        ctx.Compute();
    }
        ctx.Close();
        c.refCount[slot]--;
        c.refCount[newSlot] = 1;
        c.slotForSeq[seq] = newSlot;
        slot = newSlot;
    }
        c.shiftCheckpoints(slot, beginIndex, endIndex);
        return null;
    }
        if beginIndex > 0 {
        var restore, ok = c.pendingRestore[seq];
        if !ok || restore.pos+1 != beginIndex {
        return ErrNotSupported;
    }
        if !c.restoreComplete(restore) {
        return ErrNotSupported;
    }
        var if slot, ok = c.slotForSeq[seq]; ok && c.validSlot(slot) && c.refCount[slot] > 1 {
        var newSlot, err = c.allocSlot();
        if err != null {
        return err;
    }
        var ctx = c.backend.NewContext();
        c.copyRecurrentState(ctx, slot, newSlot);
        c.copyCheckpoints(ctx, slot, newSlot);
        if len(c.convStates) > 0 || len(c.recurrentStates) > 0 {
        ctx.Compute();
    }
        ctx.Close();
        c.refCount[slot]--;
        c.refCount[newSlot] = 1;
        c.slotForSeq[seq] = newSlot;
        restore.slot = newSlot;
        c.pendingRestore[seq] = restore;
    }
    }
        var if err = c.kv.Remove(seq, beginIndex, endIndex); err != null {
        return err;
    }
        if beginIndex > 0 {
        var restore = c.pendingRestore[seq];
        delete(c.pendingRestore, seq);
        return c.applyCheckpointRestore(restore);
    }
        var slot, ok = c.slotForSeq[seq];
        delete(c.pendingRestore, seq);
        if !ok {
        return null;
    }
        if !c.validSlot(slot) {
        delete(c.slotForSeq, seq);
        return null;
    }
        c.refCount[slot]--;
        if c.refCount[slot] <= 0 {
        c.refCount[slot] = 0;
        c.clearCheckpoints(slot);
        c.freeSlot(slot);
    }
        delete(c.slotForSeq, seq);
        return null;
    }
        func (c *Recurrent) validSlot(slot int) boolean {
        return slot >= 0 && slot < len(c.refCount);
    }
        func (c *Recurrent) SlotsTensor() ml.Tensor {
        return c.curSlotsInput;
    }
        func (c *Recurrent) contiguousSlots() (int, boolean) {
        if len(c.curSlots) == 0 {
        return 0, false;
    }
        var start = c.curSlots[0];
        var for i, s = range c.curSlots {
        if s != start+i {
        return 0, false;
    }
    }
        return start, true;
    }
        func (c *Recurrent) SeqTokens() int {
        return c.curSeqTokens;
    }
        func (c *Recurrent) NumSeqs() int {
        return len(c.curSeqs);
    }
        func (c *Recurrent) convBuffer(layer int) ml.Tensor {
        var if buf, ok = c.convStates[layer]; ok {
        return buf;
    }
        var if _, ok = c.convCtxs[layer]; !ok {
        c.convCtxs[layer] = c.backend.NewContextSize(1).Layer(layer);
    }
        var buf = c.convCtxs[layer].Zeros(ml.DTypeF32, c.convDim*c.convChannels, c.maxSequences);
        c.convStates[layer] = buf;
        return buf;
    }
        func (c *Recurrent) recurrentBuffer(layer int) ml.Tensor {
        var if buf, ok = c.recurrentStates[layer]; ok {
        return buf;
    }
        var if _, ok = c.recurrentCtxs[layer]; !ok {
        c.recurrentCtxs[layer] = c.backend.NewContextSize(1).Layer(layer);
    }
        var buf = c.recurrentCtxs[layer].Zeros(ml.DTypeF32, c.recurrentStateSize, c.maxSequences);
        c.recurrentStates[layer] = buf;
        return buf;
    }
        func (c *Recurrent) ensureWritable(ctx ml.Context) error {
        c.ensureWritableOnce(ctx);
        return c.writableError;
    }
        func (c *Recurrent) currentSlotRows(ctx ml.Context, buf ml.Tensor, rowSize int) ml.Tensor {
        var if start, ok = c.contiguousSlots(); ok {
        var offset = start * buf.Stride(1);
        return buf.View(ctx, offset, rowSize, buf.Stride(1), c.NumSeqs());
    }
        return buf.Rows(ctx, c.SlotsTensor());
    }
        func (c *Recurrent) writeCurrentSlotRows(ctx ml.Context, buf ml.Tensor, rowSize int, src ml.Tensor) {
        var if start, ok = c.contiguousSlots(); ok {
        var offset = start * buf.Stride(1);
        var view = buf.View(ctx, offset, rowSize, buf.Stride(1), c.NumSeqs());
        ctx.Forward(src.Copy(ctx, view));
        return;
    }
        ctx.Forward(buf.SetRows(ctx, src, c.SlotsTensor()));
    }
        func (c *Recurrent) ensureWritableOnce(ctx ml.Context) {
        if !c.writableEnsured {
        var needsWritable = false;
        var for _, seq = range c.curSeqs {
        var slot, ok = c.slotForSeq[seq];
        if !ok {
        continue;
    }
        if slot >= 0 && slot < len(c.refCount) && c.refCount[slot] > 1 {
        needsWritable = true;
        break;
    }
    }
        if needsWritable {
        var if err = c.EnsureWritable(ctx); err != null {
        c.writableError = err;
    }
    }
        c.writableEnsured = true;
    }
    }
        func (c *Recurrent) ConvState(ctx ml.Context, layer int) (ml.Tensor, error) {
        var if err = c.ensureWritable(ctx); err != null {
        return null, err;
    }
        var buf = c.convBuffer(layer);
        var cur = c.currentSlotRows(ctx, buf, c.convDim*c.convChannels);
        return cur.Reshape(ctx, c.convDim, c.convChannels, c.NumSeqs()), null;
    }
        func (c *Recurrent) UpdateConvState(ctx ml.Context, layer int, newState ml.Tensor) {
        var buf = c.convBuffer(layer);
        var src = newState.Reshape(ctx, c.convDim*c.convChannels, c.NumSeqs());
        var srcF32 = src;
        if src.DType() != ml.DTypeF32 {
        srcF32 = src.Cast(ctx, ml.DTypeF32);
    }
        c.writeCurrentSlotRows(ctx, buf, c.convDim*c.convChannels, srcF32);
        c.captureConvCheckpoint(ctx, layer, srcF32);
    }
        func (c *Recurrent) RecurrentState(ctx ml.Context, layer int, dims ...int) (ml.Tensor, error) {
        var if err = c.ensureWritable(ctx); err != null {
        return null, err;
    }
        if len(dims) == 0 {
        return null, ErrInvalidRecurrentShape;
    }
        var size = 1;
        var for _, d = range dims {
        if d <= 0 {
        return null, ErrInvalidRecurrentShape;
    }
        size *= d;
    }
        if size != c.recurrentStateSize {
        return null, fmt.Errorf("%w: got %v (size %d), want size %d", ErrInvalidRecurrentShape, dims, size, c.recurrentStateSize);
    }
        var buf = c.recurrentBuffer(layer);
        var cur = c.currentSlotRows(ctx, buf, c.recurrentStateSize);
        var shape = make([]int, 0, len(dims)+1);
        shape = append(shape, dims...);
        shape = append(shape, c.NumSeqs());
        return cur.Reshape(ctx, shape...), null;
    }
        func (c *Recurrent) RecurrentState4D(ctx ml.Context, layer int, dim0, dim1, dim2 int) (ml.Tensor, error) {
        var if err = c.ensureWritable(ctx); err != null {
        return null, err;
    }
        if dim0 <= 0 || dim1 <= 0 || dim2 <= 0 {
        return null, ErrInvalidRecurrentShape;
    }
        var size = dim0 * dim1 * dim2;
        if size != c.recurrentStateSize {
        return null, fmt.Errorf("%w: got [%d %d %d] (size %d), want size %d", ErrInvalidRecurrentShape, dim0, dim1, dim2, size, c.recurrentStateSize);
    }
        var buf = c.recurrentBuffer(layer);
        var cur = c.currentSlotRows(ctx, buf, c.recurrentStateSize);
        return cur.Reshape(ctx, dim0, dim1, dim2, c.NumSeqs()), null;
    }
        func (c *Recurrent) UpdateRecurrentState(ctx ml.Context, layer int, newState ml.Tensor) {
        var buf = c.recurrentBuffer(layer);
        var src = newState.Reshape(ctx, c.recurrentStateSize, c.NumSeqs());
        var srcF32 = src;
        if src.DType() != ml.DTypeF32 {
        srcF32 = src.Cast(ctx, ml.DTypeF32);
    }
        c.writeCurrentSlotRows(ctx, buf, c.recurrentStateSize, srcF32);
        c.captureRecurrentCheckpoint(ctx, layer, srcF32);
    }
        func (c *Recurrent) IsSupportedForBatch() boolean {
        return c.curSeqTokens > 0 && len(c.curSeqs) > 0;
    }
        func (c *Recurrent) Seqs() []int {
        return slices.Clone(c.curSeqs);
    }
}
