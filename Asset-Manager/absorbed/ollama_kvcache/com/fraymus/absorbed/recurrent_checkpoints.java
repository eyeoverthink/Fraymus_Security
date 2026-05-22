package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class recurrent_checkpoints {
        "log/slog";
        "math";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static class checkpointEntry {
        public int32 pos;
        public map[int]ml.Tensor conv;
        public map[int]ml.Tensor recurrent;
    }

    public static class slotCheckpointStore {
        public []checkpointEntry entries;
        public int size;
        public int next;
        public int32 lastPos;
    }

    public static class checkpointRestore {
        public int slot;
        public int idx;
        public int32 pos;
    }
        func newSlotCheckpointStore(n int) *slotCheckpointStore {
        var entries = make([]checkpointEntry, n);
        var for i = range entries {
        entries[i].pos = -1;
    }
        return &slotCheckpointStore{
        entries: entries,;
        lastPos: -1,;
    }
    }
        func (s *slotCheckpointStore) reset() {
        s.size = 0;
        s.next = 0;
        s.lastPos = -1;
        var for i = range s.entries {
        s.entries[i].pos = -1;
    }
    }
        func (s *slotCheckpointStore) record(pos int32) int {
        if len(s.entries) == 0 {
        return -1;
    }
        var idx = s.next;
        s.next = (s.next + 1) % len(s.entries);
        if s.size < len(s.entries) {
        s.size++;
    }
        s.entries[idx].pos = pos;
        s.lastPos = pos;
        return idx;
    }
        func (s *slotCheckpointStore) bestIndex(targetPos int32) (int, int32, boolean) {
        var bestIdx = -1;
        var bestPos = int32(-1);
        var for i = range s.entries {
        var pos = s.entries[i].pos;
        if pos < 0 || pos >= targetPos {
        continue;
    }
        if pos > bestPos {
        bestPos = pos;
        bestIdx = i;
    }
    }
        if bestIdx < 0 {
        return -1, -1, false;
    }
        return bestIdx, bestPos, true;
    }
        func (s *slotCheckpointStore) pruneAfter(pos int32) {
        if len(s.entries) == 0 {
        s.size = 0;
        s.next = 0;
        s.lastPos = -1;
        return;
    }
        var size = 0;
        var next = -1;
        var minPos = int32(math.MaxInt32);
        var minIdx = 0;
        var for i = range s.entries {
        if s.entries[i].pos > pos {
        s.entries[i].pos = -1;
    }
        if s.entries[i].pos >= 0 {
        size++;
        if s.entries[i].pos < minPos {
        minPos = s.entries[i].pos;
        minIdx = i;
    }
        } else if next == -1 {
        next = i;
    }
    }
        s.size = size;
        if size == 0 {
        s.next = 0;
        s.lastPos = -1;
        return;
    }
        if next != -1 {
        s.next = next;
        } else {
        s.next = minIdx;
    }
        s.lastPos = pos;
    }
        func (s *slotCheckpointStore) shiftRange(beginIndex, endIndex int32) {
        if len(s.entries) == 0 {
        s.size = 0;
        s.next = 0;
        s.lastPos = -1;
        return;
    }
        var offset = beginIndex - endIndex;
        var size = 0;
        var next = -1;
        var minPos = int32(math.MaxInt32);
        var maxPos = int32(-1);
        var minIdx = 0;
        var for i = range s.entries {
        var pos = s.entries[i].pos;
        if pos >= 0 {
        if pos >= beginIndex && pos < endIndex {
        s.entries[i].pos = -1;
        } else if pos >= endIndex {
        s.entries[i].pos = pos + offset;
    }
    }
        pos = s.entries[i].pos;
        if pos >= 0 {
        size++;
        if pos < minPos {
        minPos = pos;
        minIdx = i;
    }
        if pos > maxPos {
        maxPos = pos;
    }
        } else if next == -1 {
        next = i;
    }
    }
        s.size = size;
        if size == 0 {
        s.next = 0;
        s.lastPos = -1;
        return;
    }
        if next != -1 {
        s.next = next;
        } else {
        s.next = minIdx;
    }
        s.lastPos = maxPos;
    }
        func (s *slotCheckpointStore) window() (size int, minPos, maxPos, lastPos int32) {
        minPos = int32(math.MaxInt32);
        maxPos = int32(-1);
        var for i = range s.entries {
        var pos = s.entries[i].pos;
        if pos < 0 {
        continue;
    }
        size++;
        if pos < minPos {
        minPos = pos;
    }
        if pos > maxPos {
        maxPos = pos;
    }
    }
        if size == 0 {
        minPos = -1;
        maxPos = -1;
    }
        return size, minPos, maxPos, s.lastPos;
    }
        func (c *Recurrent) checkpointTag() String {
        if c.logPrefix == "" {
        return "kvcache.recurrent";
    }
        return c.logPrefix;
    }
        func (c *Recurrent) planCheckpoints(batch input.Batch) {
        if c.checkpointCount == 0 || len(c.curSeqs) == 0 {
        c.curCheckpointPos = c.curCheckpointPos[:0];
        var for k = range c.curCheckpointSlots {
        delete(c.curCheckpointSlots, k);
    }
        return;
    }
        if cap(c.curCheckpointPos) < len(c.curSeqs) {
        c.curCheckpointPos = make([]int32, len(c.curSeqs));
        } else {
        c.curCheckpointPos = c.curCheckpointPos[:len(c.curSeqs)];
    }
        var for i = range c.curCheckpointPos {
        c.curCheckpointPos[i] = -1;
    }
        var for k = range c.curCheckpointSlots {
        delete(c.curCheckpointSlots, k);
    }
        var posMax = make(map[int]int32, len(c.curSeqs));
        var for i, seq = range batch.Sequences {
        var pos = batch.Positions[i];
        var if cur, ok = posMax[seq]; !ok || pos > cur {
        posMax[seq] = pos;
    }
    }
        var for i, seq = range c.curSeqs {
        var pos, ok = posMax[seq];
        if !ok {
        continue;
    }
        if pos < c.checkpointMinPos {
        continue;
    }
        var slot = c.curSlots[i];
        var store = c.checkpointStore(slot);
        var lastPos = store.lastPos;
        if lastPos < 0 || pos-lastPos >= c.checkpointInterval {
        c.curCheckpointPos[i] = pos;
    }
    }
    }
        func (c *Recurrent) checkpointStore(slot int) *slotCheckpointStore {
        var store, ok = c.checkpoints[slot];
        if ok {
        return store;
    }
        store = newSlotCheckpointStore(c.checkpointCount);
        c.checkpoints[slot] = store;
        return store;
    }
        func (c *Recurrent) checkpointIndexForSlot(slot int, pos int32) int {
        if c.checkpointCount == 0 {
        return -1;
    }
        var if idx, ok = c.curCheckpointSlots[slot]; ok {
        return idx;
    }
        var store = c.checkpointStore(slot);
        var idx = store.record(pos);
        if idx >= 0 {
        c.curCheckpointSlots[slot] = idx;
    }
        return idx;
    }
        func (c *Recurrent) hasCheckpoint(seq int, pos int32) boolean {
        if pos <= 0 {
        return false;
    }
        var slot, ok = c.slotForSeq[seq];
        if !ok {
        return false;
    }
        var store, ok = c.checkpoints[slot];
        if !ok {
        return false;
    }
        _, _, ok = store.bestIndex(pos);
        return ok;
    }
        func (c *Recurrent) PrepareRestore(seq int, targetPos int32) (int32, boolean) {
        if targetPos <= 0 {
        return 0, false;
    }
        var slot, ok = c.slotForSeq[seq];
        if !ok {
        return 0, false;
    }
        var store, ok = c.checkpoints[slot];
        if !ok {
        slog.Debug(c.checkpointTag()+": checkpoint miss", "seq", seq, "slot", slot, "target", targetPos, "size", 0);
        return 0, false;
    }
        var idx, pos, ok = store.bestIndex(targetPos);
        if !ok {
        var size, minPos, maxPos, lastPos = store.window();
        slog.Debug(c.checkpointTag()+": checkpoint miss", "seq", seq, "slot", slot, "target", targetPos, "size", size,;
        "min", minPos, "max", maxPos, "last", lastPos);
        return 0, false;
    }
        c.pendingRestore[seq] = checkpointRestore{
        slot: slot,;
        idx:  idx,;
        pos:  pos,;
    }
        return pos + 1, true;
    }
        func (c *Recurrent) applyCheckpointRestore(restore checkpointRestore) error {
        var entry, ok = c.restoreEntry(restore);
        if !ok {
        return ErrNotSupported;
    }
        var ctx = c.backend.NewContext();
        defer ctx.Close();
        var slotIdx = ctx.Input().FromInts([]int32{int32(restore.slot)}, 1);
        var for layer, src = range entry.conv {
        var buf = c.convBuffer(layer);
        ctx.Forward(buf.SetRows(ctx, src, slotIdx));
    }
        var for layer, src = range entry.recurrent {
        var buf = c.recurrentBuffer(layer);
        ctx.Forward(buf.SetRows(ctx, src, slotIdx));
    }
        if len(entry.conv) > 0 || len(entry.recurrent) > 0 {
        ctx.Compute();
    }
        var store = c.checkpoints[restore.slot];
        store.pruneAfter(restore.pos);
        return null;
    }
        func (c *Recurrent) restoreComplete(restore checkpointRestore) boolean {
        var _, ok = c.restoreEntry(restore);
        return ok;
    }
        func (c *Recurrent) restoreEntry(restore checkpointRestore) (*checkpointEntry, boolean) {
        var store, ok = c.checkpoints[restore.slot];
        if !ok || restore.idx < 0 || restore.idx >= len(store.entries) {
        return null, false;
    }
        var entry = &store.entries[restore.idx];
        if entry.pos < 0 {
        return null, false;
    }
        if !c.entryComplete(entry) {
        return null, false;
    }
        return entry, true;
    }
        func (c *Recurrent) entryComplete(entry *checkpointEntry) boolean {
        var for layer = range c.convStates {
        if entry.conv == null || entry.conv[layer] == null {
        return false;
    }
    }
        var for layer = range c.recurrentStates {
        if entry.recurrent == null || entry.recurrent[layer] == null {
        return false;
    }
    }
        return true;
    }
        func (c *Recurrent) clearCheckpoints(slot int) {
        var if store, ok = c.checkpoints[slot]; ok {
        store.reset();
    }
    }
        func (c *Recurrent) shiftCheckpoints(slot int, beginIndex, endIndex int32) {
        var if store, ok = c.checkpoints[slot]; ok {
        store.shiftRange(beginIndex, endIndex);
    }
    }
        func (c *Recurrent) copyCheckpoints(ctx ml.Context, srcSlot, dstSlot int) {
        if c.checkpointCount == 0 {
        return;
    }
        var srcStore, ok = c.checkpoints[srcSlot];
        if !ok || srcStore.size == 0 {
        return;
    }
        var dstStore = c.checkpointStore(dstSlot);
        dstStore.size = srcStore.size;
        dstStore.next = srcStore.next;
        dstStore.lastPos = srcStore.lastPos;
        var for i = range srcStore.entries {
        var srcEntry = &srcStore.entries[i];
        var dstEntry = &dstStore.entries[i];
        dstEntry.pos = srcEntry.pos;
        if srcEntry.conv != null {
        if dstEntry.conv == null {
        dstEntry.conv = make(map[int]ml.Tensor);
    }
        var for layer, src = range srcEntry.conv {
        var dst = c.ensureCheckpointConv(layer, dstEntry);
        ctx.Forward(src.Copy(ctx, dst));
    }
    }
        if srcEntry.recurrent != null {
        if dstEntry.recurrent == null {
        dstEntry.recurrent = make(map[int]ml.Tensor);
    }
        var for layer, src = range srcEntry.recurrent {
        var dst = c.ensureCheckpointRecurrent(layer, dstEntry);
        ctx.Forward(src.Copy(ctx, dst));
    }
    }
    }
    }
        func (c *Recurrent) captureConvCheckpoint(ctx ml.Context, layer int, src ml.Tensor) {
        if c.checkpointCount == 0 {
        return;
    }
        if c.reserveCheckpoints {
        c.reserveCheckpointConv(layer);
        return;
    }
        if len(c.curCheckpointPos) == 0 {
        return;
    }
        var for i, pos = range c.curCheckpointPos {
        if pos < 0 {
        continue;
    }
        var slot = c.curSlots[i];
        var idx = c.checkpointIndexForSlot(slot, pos);
        if idx < 0 {
        continue;
    }
        var entry = &c.checkpoints[slot].entries[idx];
        var dst = c.ensureCheckpointConv(layer, entry);
        var seqSlice = src.Slice(ctx, 1, i, i+1, 1);
        ctx.Forward(seqSlice.Copy(ctx, dst));
    }
    }
        func (c *Recurrent) captureRecurrentCheckpoint(ctx ml.Context, layer int, src ml.Tensor) {
        if c.checkpointCount == 0 {
        return;
    }
        if c.reserveCheckpoints {
        c.reserveCheckpointRecurrent(layer);
        return;
    }
        if len(c.curCheckpointPos) == 0 {
        return;
    }
        var for i, pos = range c.curCheckpointPos {
        if pos < 0 {
        continue;
    }
        var slot = c.curSlots[i];
        var idx = c.checkpointIndexForSlot(slot, pos);
        if idx < 0 {
        continue;
    }
        var entry = &c.checkpoints[slot].entries[idx];
        var dst = c.ensureCheckpointRecurrent(layer, entry);
        var seqSlice = src.Slice(ctx, 1, i, i+1, 1);
        ctx.Forward(seqSlice.Copy(ctx, dst));
    }
    }
        func (c *Recurrent) ensureCheckpointConv(layer int, entry *checkpointEntry) ml.Tensor {
        if entry.conv == null {
        entry.conv = make(map[int]ml.Tensor);
    }
        var if t, ok = entry.conv[layer]; ok {
        return t;
    }
        var ctx, ok = c.checkpointConvCtxs[layer];
        if !ok {
        ctx = c.backend.NewContextSize(c.checkpointCtxSize).Layer(layer);
        c.checkpointConvCtxs[layer] = ctx;
    }
        var t = ctx.Zeros(ml.DTypeF32, c.convDim*c.convChannels, 1);
        entry.conv[layer] = t;
        return t;
    }
        func (c *Recurrent) ensureCheckpointRecurrent(layer int, entry *checkpointEntry) ml.Tensor {
        if entry.recurrent == null {
        entry.recurrent = make(map[int]ml.Tensor);
    }
        var if t, ok = entry.recurrent[layer]; ok {
        return t;
    }
        var ctx, ok = c.checkpointRecurCtxs[layer];
        if !ok {
        ctx = c.backend.NewContextSize(c.checkpointCtxSize).Layer(layer);
        c.checkpointRecurCtxs[layer] = ctx;
    }
        var t = ctx.Zeros(ml.DTypeF32, c.recurrentStateSize, 1);
        entry.recurrent[layer] = t;
        return t;
    }
        func (c *Recurrent) reserveCheckpointConv(layer int) {
        var key = checkpointReserveKey(layer, 0);
        var if _, ok = c.checkpointReserved[key]; ok {
        return;
    }
        var for slot = range c.maxSequences {
        var store = c.checkpointStore(slot);
        var for i = range store.entries {
        var entry = &store.entries[i];
        _ = c.ensureCheckpointConv(layer, entry);
    }
    }
        c.checkpointReserved[key] = struct{}{}
    }
        func (c *Recurrent) reserveCheckpointRecurrent(layer int) {
        var key = checkpointReserveKey(layer, 1);
        var if _, ok = c.checkpointReserved[key]; ok {
        return;
    }
        var for slot = range c.maxSequences {
        var store = c.checkpointStore(slot);
        var for i = range store.entries {
        var entry = &store.entries[i];
        _ = c.ensureCheckpointRecurrent(layer, entry);
    }
    }
        c.checkpointReserved[key] = struct{}{}
    }

    public static int checkpointReserveKey(int layer, int kind) {
        return layer*2 + kind;
    }
}
