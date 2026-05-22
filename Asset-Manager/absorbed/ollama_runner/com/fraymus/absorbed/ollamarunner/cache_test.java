package com.fraymus.absorbed.ollamarunner;

import java.util.*;
import java.io.*;

public class cache_test {
        "errors";
        "fmt";
        "slices";
        "testing";
        "time";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static void TestCountCommon(*testing.T t) {
        var tests = []struct {
        name     String;
        t1       []*input.Input;
        t2       []*input.Input;
        expected int32;
        }{
        {
        name:     "Equal",;
        t1:       []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        t2:       []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        expected: 3,;
        },;
        {
        name:     "Prefix",;
        t1:       []*input.Input{{Token: 1}},;
        t2:       []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        expected: 1,;
        },;
        {
        name:     "Image Prefix",;
        t1:       []*input.Input{{MultimodalHash: 1}},;
        t2:       []*input.Input{{MultimodalHash: 1}, {MultimodalHash: 2}, {MultimodalHash: 3}},;
        expected: 1,;
        },;
        {
        name:     "Mixed",;
        t1:       []*input.Input{{Token: 1}, {MultimodalHash: 1}},;
        t2:       []*input.Input{{Token: 1}, {MultimodalHash: 1}, {Token: 5}},;
        expected: 2,;
        },;
        {
        name:     "Mixed, Same Length",;
        t1:       []*input.Input{{Token: 1}, {MultimodalHash: 1}},;
        t2:       []*input.Input{{Token: 1}, {MultimodalHash: 2}},;
        expected: 1,;
        },;
        {
        name:     "Empty",;
        t1:       []*input.Input{},;
        t2:       []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        expected: 0,;
        },;
        {
        name:     "Both Empty",;
        t1:       []*input.Input{},;
        t2:       []*input.Input{},;
        expected: 0,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result = countCommonPrefix(tt.t1, tt.t2);
        if result != tt.expected {
        t.Errorf("countCommonPrefix(%v, %v): have %v; want %v", tt.t1, tt.t2, result, tt.expected);
    }
        });
    }
    }

    public static void TestFindCacheSlot(*testing.T t) {

    public static class expected {
        public int result;
        public int32 len;
    }
        var tests = []struct {
        name    String;
        cache   InputCache;
        prompt  []*input.Input;
        longest expected;
        best    expected;
        }{
        {
        name: "Empty",;
        cache: InputCache{slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Time{},;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Time{},;
        },;
        }},;
        prompt:  []*input.Input{{Token: 1}},;
        longest: expected{result: 0, len: 0},;
        best:    expected{result: 0, len: 0},;
        },;
        {
        name: "Extend",;
        cache: InputCache{slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-2 * time.Second),;
        },;
        }},;
        prompt:  []*input.Input{{Token: 1}, {Token: 2}},;
        longest: expected{result: 1, len: 2},;
        best:    expected{result: 1, len: 2},;
        },;
        {
        name: "New",;
        cache: InputCache{slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Time{},;
        },;
        }},;
        prompt:  []*input.Input{{Token: 2}},;
        longest: expected{result: 0, len: 0},;
        best:    expected{result: 1, len: 0},;
        },;
        {
        name: "Fork",;
        cache: InputCache{
        slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Time{},;
        },;
        },;
        },;
        prompt:  []*input.Input{{Token: 1}},;
        longest: expected{result: 0, len: 1},;
        best:    expected{result: 1, len: 1},;
        },;
        {
        name: "Evict",;
        cache: InputCache{slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-2 * time.Second),;
        },;
        }},;
        prompt:  []*input.Input{{Token: 2}, {Token: 3}},;
        longest: expected{result: 0, len: 0},;
        best:    expected{result: 1, len: 0},;
        },;
        {
        name: "In use",;
        cache: InputCache{slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    true,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{{Token: 1}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-2 * time.Second),;
        },;
        }},;
        prompt:  []*input.Input{{Token: 1}, {Token: 2}},;
        longest: expected{result: 1, len: 1},;
        best:    expected{result: 1, len: 2},;
        },;
    }
        var for _, tt = range tests {
        t.Run("Longest-"+tt.name, func(t *testing.T) {
        var result, resultLen, err = tt.cache.findLongestCacheSlot(tt.prompt);
        if err != null {
        t.Errorf("findLongestCacheSlot: err %v", err);
        } else if result.Id != tt.longest.result || resultLen != tt.longest.len {
        t.Errorf("findLongestCacheSlot: slot have %v, want %v len have %v, want %v",;
        result.Id, tt.longest.result, resultLen, tt.longest.len);
    }
        });
    }
        var for _, tt = range tests {
        t.Run("Best-"+tt.name, func(t *testing.T) {
        var result, resultLen, err = tt.cache.findBestCacheSlot(tt.prompt);
        if err != null {
        t.Errorf("findBestCacheSlot: err %v", err);
        } else if result.Id != tt.best.result || resultLen != tt.best.len {
        t.Errorf("findBestCacheSlot: slot have %v, want %v len have %v, want %v",;
        result.Id, tt.best.result, resultLen, tt.best.len);
    }
        });
    }
    }

    public static void TestShiftDiscard(*testing.T t) {
        var tests = []struct {
        name     String;
        numCtx   int32;
        numKeep  int32;
        inputs   []*input.Input;
        expected int32;
        }{
        {
        name:     "Shift",;
        numCtx:   2048,;
        numKeep:  5,;
        inputs:   slices.Repeat([]*input.Input{{}}, 2048),;
        expected: 1021,;
        },;
        {
        name:     "Max Keep",;
        numCtx:   2048,;
        numKeep:  2047,;
        inputs:   slices.Repeat([]*input.Input{{}}, 2048),;
        expected: 1,;
        },;
        {
        name:     "No Keep",;
        numCtx:   2048,;
        numKeep:  0,;
        inputs:   slices.Repeat([]*input.Input{{}}, 2048),;
        expected: 1024,;
        },;
        {
        name:     "Truncate",;
        numCtx:   2048,;
        numKeep:  5,;
        inputs:   slices.Repeat([]*input.Input{{}}, 5000),;
        expected: 3973,;
        },;
        {
        name:     "Truncate Keep",;
        numCtx:   2048,;
        numKeep:  2047,;
        inputs:   slices.Repeat([]*input.Input{{}}, 5000),;
        expected: 2953,;
        },;
        {
        name:     "No Op",;
        numCtx:   2048,;
        numKeep:  5,;
        inputs:   slices.Repeat([]*input.Input{{}}, 512),;
        expected: 0,;
        },;
        {
        name:    "Same Batch",;
        numCtx:  2048,;
        numKeep: 5,;
        inputs: slices.Collect(func(yield func(*input.Input) boolean) {
        for range 1024 {
        if !yield(&input.Input{}) {
        return;
    }
    }
        if !yield(&input.Input{SameBatch: 512 - 1}) {
        return;
    }
        for range 2048 - 1024 - 1 {
        if !yield(&input.Input{}) {
        return;
    }
    }
        }),;
        expected: 1531,;
        },;
        {
        name:    "Same Batch Near Start",;
        numCtx:  2048,;
        numKeep: 5,;
        inputs: slices.Collect(func(yield func(*input.Input) boolean) {
        for range 10 {
        if !yield(&input.Input{}) {
        return;
    }
    }
        if !yield(&input.Input{SameBatch: 512 - 1}) {
        return;
    }
        for range 2048 - 10 - 1 {
        if !yield(&input.Input{}) {
        return;
    }
    }
        }),;
        expected: 1021,;
        },;
        {
        name:   "Consecutive Same Batch",;
        numCtx: 32,;
        inputs: slices.Collect(func(yield func(*input.Input) boolean) {
        var for i = range 32 {
        var input = input.Input{}
        if i%10 == 0 {
        input.SameBatch = 10 - 1;
    }
        if !yield(&input) {
        return;
    }
    }
        }),;
        expected: 20,;
        },;
        {
        name:   "Overlapping Same Batch",;
        numCtx: 32,;
        inputs: slices.Collect(func(yield func(*input.Input) boolean) {
        var for i = range 32 {
        var input = input.Input{}
        if slices.Contains([]int{4, 8, 14}, i) {
        input.SameBatch = 10 - 1;
    }
        if !yield(&input) {
        return;
    }
    }
        }),;
        expected: 24,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var c = InputCache{numCtx: tt.numCtx}
        var result = c.ShiftDiscard(tt.inputs, tt.numKeep);
        if result != tt.expected {
        t.Errorf("shiftDiscard(ctx: %v, keep: %v inputs: %v): have %v; want %v", tt.numCtx, tt.numKeep, len(tt.inputs), result, tt.expected);
    }
        });
    }
    }

    public static void TestLoadCacheSlot(*testing.T t) {
        var tests = []struct {
        name           String;
        cache          InputCache;
        prompt         []*input.Input;
        wantErr        boolean;
        expectedSlotId int;
        expectedPrompt int // expected length of remaining prompt;
        }{
        {
        name: "Basic cache hit - single user",;
        cache: InputCache{
        multiUserCache: false,;
        slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Now().Add(-2 * time.Second),;
        },;
        },;
        },;
        prompt:         []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        wantErr:        false,;
        expectedSlotId: 0,;
        expectedPrompt: 1, // Only token 3 remains;
        },;
        {
        name: "Basic cache hit - multi user",;
        cache: InputCache{
        multiUserCache: true,;
        slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        {
        Id:       1,;
        Inputs:   []*input.Input{},;
        InUse:    false,;
        lastUsed: time.Now().Add(-2 * time.Second),;
        },;
        },;
        },;
        prompt:         []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        wantErr:        false,;
        expectedSlotId: 0,;
        expectedPrompt: 1, // Only token 3 remains;
        },;
        {
        name: "Exact match - leave one input",;
        cache: InputCache{
        multiUserCache: false,;
        slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    false,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        },;
        },;
        prompt:         []*input.Input{{Token: 1}, {Token: 2}},;
        wantErr:        false,;
        expectedSlotId: 0,;
        expectedPrompt: 1, // Should leave 1 token for sampling;
        },;
        {
        name: "No available slots",;
        cache: InputCache{
        multiUserCache: false,;
        slots: []InputCacheSlot{
        {
        Id:       0,;
        Inputs:   []*input.Input{{Token: 1}, {Token: 2}},;
        InUse:    true,;
        lastUsed: time.Now().Add(-time.Second),;
        },;
        },;
        },;
        prompt:         []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}},;
        wantErr:        true,;
        expectedSlotId: -1,;
        expectedPrompt: -1,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var slot, remainingPrompt, err = tt.cache.LoadCacheSlot(tt.prompt, true);
        if (err != null) != tt.wantErr {
        t.Errorf("LoadCacheSlot() error = %v, wantErr %v", err, tt.wantErr);
        return;
    }
        if tt.wantErr {
        return // Skip further checks if we expected an error;
    }
        if slot.Id != tt.expectedSlotId {
        t.Errorf("LoadCacheSlot() slot ID = %v, expected %v", slot.Id, tt.expectedSlotId);
    }
        if !slot.InUse {
        t.Errorf("LoadCacheSlot() slot not marked InUse");
    }
        if len(remainingPrompt) != tt.expectedPrompt {
        t.Errorf("LoadCacheSlot() remaining prompt length = %v, expected %v",;
        len(remainingPrompt), tt.expectedPrompt);
    }
        });
    }
    }

    public static class mockCache {
        public boolean shouldFail;
    }
        func (m *mockCache) Remove(seq int, beginIndex, endIndex int32) error {
        if m.shouldFail {
        return fmt.Errorf("mock cache removal error");
    }
        return null;
    }
        func (m *mockCache) SetLayer(layer int)                                                            {}
        func (m *mockCache) Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor)                          { return null, null, null }
        func (m *mockCache) Put(ctx ml.Context, key, value ml.Tensor)                                      {}
        func (m *mockCache) Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int) {}
        func (m *mockCache) Close()                                                                        {}
        func (m *mockCache) StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error            { return null }
        func (m *mockCache) CopyPrefix(srcSeq, dstSeq int, len int32)                                      {}
        func (m *mockCache) SetConfig(ml.CacheConfig)                                                      {}
        func (m *mockCache) CanResume(seq int, pos int32) boolean                                             { return true }

    public static void TestShiftCacheSlot(*testing.T t) {
        var tests = []struct {
        name          String;
        numCtx        int32;
        inputs        []*input.Input;
        numKeep       int32;
        cacheErr      boolean;
        wantErr       any;
        wantInputsLen int;
        }{
        {
        name:          "Normal shift",;
        numCtx:        10,;
        inputs:        []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}, {Token: 4}, {Token: 5}, {Token: 6}, {Token: 7}, {Token: 8}, {Token: 9}, {Token: 10}},;
        numKeep:       2,;
        cacheErr:      false, // No error;
        wantErr:       null,;
        wantInputsLen: 6, // After discarding 4 tokens;
        },;
        {
        name:          "Cache removal fails",;
        numCtx:        10,;
        inputs:        []*input.Input{{Token: 1}, {Token: 2}, {Token: 3}, {Token: 4}, {Token: 5}, {Token: 6}, {Token: 7}, {Token: 8}, {Token: 9}, {Token: 10}},;
        numKeep:       2,;
        cacheErr:      true,;
        wantErr:       &ErrReprocessInputs{},;
        wantInputsLen: 0, // Original inputs should be cleared;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var mock = &mockCache{shouldFail: tt.cacheErr}
        var c = InputCache{
        numCtx: tt.numCtx,;
        cache:  mock,;
    }
        var slot = &InputCacheSlot{
        Id:     123,;
        Inputs: make([]*input.Input, len(tt.inputs)),;
    }
        copy(slot.Inputs, tt.inputs);
        var err = c.ShiftCacheSlot(slot, tt.numKeep);
        if tt.wantErr != null {
        if err == null {
        t.Errorf("Expected error but got null");
        return;
    }
        if !errors.As(err, &tt.wantErr) {
        t.Errorf("Expected error of type %T but got %T: %v", tt.wantErr, err, err);
    }
        } else if err != null {
        t.Errorf("Unexpected error: %v", err);
    }
        if len(slot.Inputs) != tt.wantInputsLen {
        t.Errorf("Slot inputs length after operation: got %v, want %v", len(slot.Inputs), tt.wantInputsLen);
    }
        });
    }
    }
}
