package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class recurrent_checkpoints_test {
        "errors";
        "math";
        "slices";
        "testing";
        "github.com/ollama/ollama/ml";
        );
        func newTestCache() *Recurrent {
        return NewRecurrentCache(RecurrentConfig{ConvDim: 1, ConvChannels: 2, RecurrentStateSize: 2});
    }

    public static void TestSlotCheckpointStoreBestIndex(*testing.T t) {
        var store = newSlotCheckpointStore(2);
        store.record(10);
        store.record(20);
        var _, pos, ok = store.bestIndex(15);
        if !ok || pos != 10 {
        t.Fatalf("expected best pos 10, got pos=%d ok=%v", pos, ok);
    }
        store.record(30) // overwrite oldest (10);
        var if _, _, ok = store.bestIndex(15); ok {
        t.Fatalf("expected no checkpoint for targetPos=15 after overwrite");
    }
        _, pos, ok = store.bestIndex(40);
        if !ok || pos != 30 {
        t.Fatalf("expected best pos 30, got pos=%d ok=%v", pos, ok);
    }
    }

    public static void TestCachePrepareRestore(*testing.T t) {
        var cache = newTestCache();
        cache.checkpointCount = 3;
        cache.checkpoints = make(map[int]*slotCheckpointStore);
        cache.pendingRestore = make(map[int]checkpointRestore);
        cache.slotForSeq[1] = 0;
        var store = cache.checkpointStore(0);
        store.record(5);
        store.record(9);
        store.record(15);
        var restorePos, ok = cache.PrepareRestore(1, 12);
        if !ok {
        t.Fatalf("expected restore ok");
    }
        if restorePos != 10 {
        t.Fatalf("expected restorePos 10, got %d", restorePos);
    }
        var rest, ok = cache.pendingRestore[1];
        if !ok {
        t.Fatalf("expected pending restore entry");
    }
        if rest.pos != 9 {
        t.Fatalf("expected pending restore pos 9, got %d", rest.pos);
    }
    }

    public static void TestSlotCheckpointStorePruneAfter(*testing.T t) {
        var store = newSlotCheckpointStore(3);
        store.record(10);
        store.record(20);
        store.record(30);
        store.pruneAfter(20);
        if store.lastPos != 20 {
        t.Fatalf("expected lastPos 20, got %d", store.lastPos);
    }
        var _, pos, ok = store.bestIndex(25);
        if !ok || pos != 20 {
        t.Fatalf("expected best pos 20 after prune, got pos=%d ok=%v", pos, ok);
    }
        _, pos, ok = store.bestIndex(35);
        if !ok || pos != 20 {
        t.Fatalf("expected pruned best pos 20 for targetPos=35, got pos=%d ok=%v", pos, ok);
    }
    }

    public static void TestCacheRestoreRejectsIncompleteCheckpoint(*testing.T t) {
        var cache = newTestCache();
        cache.checkpointCount = 3;
        cache.checkpoints = make(map[int]*slotCheckpointStore);
        cache.pendingRestore = make(map[int]checkpointRestore);
        cache.slotForSeq[1] = 0;
        cache.refCount = []int{1}
        cache.freeSlots = null;
        cache.convStates[0] = null;
        cache.recurrentStates[0] = null;
        var store = cache.checkpointStore(0);
        var idx = store.record(9);
        var entry = &store.entries[idx];
        entry.conv = map[int]ml.Tensor{0: null}
        cache.pendingRestore[1] = checkpointRestore{slot: 0, idx: idx, pos: 9}
        var err = cache.Remove(1, 10, math.MaxInt32);
        if !errors.Is(err, ErrNotSupported) {
        t.Fatalf("expected ErrNotSupported for incomplete checkpoint, got %v", err);
    }
    }

    public static void TestCacheRestoreAcceptsCompleteCheckpoint(*testing.T t) {
        var cache = newTestCache();
        cache.checkpointCount = 3;
        cache.checkpoints = make(map[int]*slotCheckpointStore);
        cache.pendingRestore = make(map[int]checkpointRestore);
        cache.slotForSeq[1] = 0;
        cache.refCount = []int{1}
        cache.freeSlots = null;
        var store = cache.checkpointStore(0);
        var idx = store.record(9);
        cache.pendingRestore[1] = checkpointRestore{slot: 0, idx: idx, pos: 9}
        var restore = cache.pendingRestore[1];
        if !cache.restoreComplete(restore) {
        t.Fatalf("expected restoreComplete to return true for complete checkpoint");
    }
    }

    public static void TestCacheRecurrentStateShapeValidation(*testing.T t) {
        var cache = newTestCache();
        var _, err = cache.RecurrentState(null, 0, 3);
        if !errors.Is(err, ErrInvalidRecurrentShape) {
        t.Fatalf("expected ErrInvalidRecurrentShape, got %v", err);
    }
    }

    public static void TestSlotCheckpointStoreShiftRange(*testing.T t) {
        var store = newSlotCheckpointStore(5);
        store.record(1);
        store.record(4);
        store.record(7);
        store.record(10);
        store.shiftRange(2, 6);
        var positions []int32;
        var for i = range store.entries {
        if store.entries[i].pos >= 0 {
        positions = append(positions, store.entries[i].pos);
    }
    }
        slices.Sort(positions);
        var want = []int32{1, 3, 6}
        if !slices.Equal(positions, want) {
        t.Fatalf("unexpected shifted positions: got=%v want=%v", positions, want);
    }
        if store.lastPos != 6 {
        t.Fatalf("expected lastPos 6, got %d", store.lastPos);
    }
    }

    public static void TestCacheRemoveMiddleShiftsCheckpoints(*testing.T t) {
        var cache = newTestCache();
        cache.slotForSeq[1] = 0;
        cache.refCount = []int{1}
        cache.pendingRestore[1] = checkpointRestore{slot: 0, idx: 0, pos: 1}
        var store = cache.checkpointStore(0);
        store.record(1);
        store.record(4);
        store.record(7);
        store.record(10);
        var if err = cache.Remove(1, 2, 6); err != null {
        t.Fatalf("expected middle remove to succeed, got %v", err);
    }
        var if _, ok = cache.pendingRestore[1]; ok {
        t.Fatalf("expected pending restore to be cleared after middle remove");
    }
        var positions []int32;
        var for i = range store.entries {
        if store.entries[i].pos >= 0 {
        positions = append(positions, store.entries[i].pos);
    }
    }
        slices.Sort(positions);
        var want = []int32{1, 3, 6}
        if !slices.Equal(positions, want) {
        t.Fatalf("unexpected checkpoint positions after remove: got=%v want=%v", positions, want);
    }
    }

    public static void TestSlotCheckpointStoreRingBufferWrapAround(*testing.T t) {
        var store = newSlotCheckpointStore(3);
        store.record(10);
        store.record(20);
        store.record(30);
        store.entries[0].conv = make(map[int]ml.Tensor);
        store.entries[0].conv[0] = null;
        store.entries[0].recurrent = make(map[int]ml.Tensor);
        store.entries[0].recurrent[0] = null;
        store.record(40);
        if store.entries[0].conv == null {
        t.Fatalf("expected conv map to be preserved on reuse");
    }
        if store.entries[0].recurrent == null {
        t.Fatalf("expected recurrent map to be preserved on reuse");
    }
        if store.entries[0].pos != 40 {
        t.Fatalf("expected entry 0 pos to be 40, got %d", store.entries[0].pos);
    }
    }

    public static void TestSlotCheckpointStoreFullCapacity(*testing.T t) {
        var store = newSlotCheckpointStore(2);
        var idx1 = store.record(10);
        var idx2 = store.record(20);
        if idx1 != 0 || idx2 != 1 {
        t.Fatalf("expected indices 0, 1, got %d, %d", idx1, idx2);
    }
        if store.size != 2 {
        t.Fatalf("expected size 2, got %d", store.size);
    }
        var _, pos1, ok1 = store.bestIndex(15);
        var _, pos2, ok2 = store.bestIndex(25);
        if !ok1 || pos1 != 10 {
        t.Fatalf("expected best pos 10 for target 15, got pos=%d ok=%v", pos1, ok1);
    }
        if !ok2 || pos2 != 20 {
        t.Fatalf("expected best pos 20 for target 25, got pos=%d ok=%v", pos2, ok2);
    }
    }

    public static void TestSlotCheckpointStoreEmptyBuffer(*testing.T t) {
        var store = newSlotCheckpointStore(0);
        var idx = store.record(10);
        if idx != -1 {
        t.Fatalf("expected record to return -1 for empty buffer, got %d", idx);
    }
        var _, _, ok = store.bestIndex(15);
        if ok {
        t.Fatalf("expected no checkpoint for empty buffer");
    }
    }

    public static void TestSlotCheckpointStorePruneAfterAll(*testing.T t) {
        var store = newSlotCheckpointStore(3);
        store.record(10);
        store.record(20);
        store.record(30);
        store.pruneAfter(5);
        if store.size != 0 {
        t.Fatalf("expected size 0 after pruning all, got %d", store.size);
    }
        if store.lastPos != -1 {
        t.Fatalf("expected lastPos -1 after pruning all, got %d", store.lastPos);
    }
        var _, _, ok = store.bestIndex(100);
        if ok {
        t.Fatalf("expected no checkpoint after pruning all");
    }
    }
}
