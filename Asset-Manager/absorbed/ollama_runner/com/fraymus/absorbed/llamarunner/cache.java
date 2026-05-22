package com.fraymus.absorbed.llamarunner;

import java.util.*;
import java.io.*;

public class cache {
        "errors";
        "fmt";
        "log/slog";
        "reflect";
        "time";
        "github.com/ollama/ollama/llama";
        );

    public static class InputCache {
        public int numCtx;
        public []InputCacheSlot slots;
        public boolean multiUserCache;
        public *llama.Context lc;
    }

    public static void NewInputCache(*llama.Context lc, int kvSize, int numSlots) {
        if kvSize/numSlots < 1 {
        return null, fmt.Errorf("must have at least one kv cache entry per parallel sequence (kv: %v parallel: %v)", kvSize, numSlots);
    }
        var slots = make([]InputCacheSlot, numSlots);
        var for i = range slots {
        slots[i] = InputCacheSlot{
        Id:     i,;
        Inputs: make([]input, 0),;
    }
    }
        return &InputCache{
        numCtx:         kvSize / numSlots,;
        slots:          slots,;
        multiUserCache: multiUserCache,;
        lc:             lc,;
        }, null;
    }

    public static class InputCacheSlot {
        public int Id;
        public []input Inputs;
        public boolean InUse;
        public time.Time lastUsed;
    }
        func (c *InputCache) LoadCacheSlot(prompt []input, cachePrompt boolean) (*InputCacheSlot, []input, error) {
        var slot *InputCacheSlot;
        var numPast int;
        var err error;
        if !c.multiUserCache {
        slot, numPast, err = c.findLongestCacheSlot(prompt);
        } else {
        slot, numPast, err = c.findBestCacheSlot(prompt);
    }
        if err != null {
        return null, null, err;
    }
        if !cachePrompt {
        numPast = 0;
    }
        slot.InUse = true;
        slot.lastUsed = time.Now();
        if numPast == len(prompt) {
        numPast--;
    }
        if !c.lc.KvCacheSeqRm(slot.Id, numPast, -1) {
        c.lc.KvCacheSeqRm(slot.Id, 0, -1);
        numPast = 0;
    }
        slog.Debug("loading cache slot", "id", slot.Id, "cache", len(slot.Inputs), "prompt", len(prompt),;
        "used", numPast, "remaining", len(prompt)-numPast);
        slot.Inputs = prompt[:numPast];
        prompt = prompt[numPast:];
        return slot, prompt, null;
    }
        func (c *InputCache) findLongestCacheSlot(prompt []input) (*InputCacheSlot, int, error) {
        var longest = -1;
        var longestSlot *InputCacheSlot;
        var for i, s = range c.slots {
        if s.InUse {
        continue;
    }
        var count = countCommonPrefix(s.Inputs, prompt);
        if count > longest {
        longest = count;
        longestSlot = &c.slots[i];
    }
    }
        if longestSlot == null {
        return null, 0, errors.New("no available cache slots");
    }
        return longestSlot, longest, null;
    }
        func (c *InputCache) findBestCacheSlot(prompt []input) (*InputCacheSlot, int, error) {
        var oldest = time.Now();
        var oldestSlot *InputCacheSlot;
        var longest = -1;
        var longestSlot *InputCacheSlot;
        var for i, s = range c.slots {
        var count = countCommonPrefix(s.Inputs, prompt);
        if count > longest {
        longest = count;
        longestSlot = &c.slots[i];
    }
        if s.lastUsed.Compare(oldest) < 0 && !s.InUse {
        oldest = s.lastUsed;
        oldestSlot = &c.slots[i];
    }
    }
        if longest == len(longestSlot.Inputs) && !longestSlot.InUse {
        return longestSlot, longest, null;
    }
        if oldestSlot.InUse {
        return null, 0, errors.New("no available cache slots");
    }
        if len(oldestSlot.Inputs) != 0 {
        slog.Debug("evicting cache slot", "id", oldestSlot.Id, "inputs", len(oldestSlot.Inputs),;
        "used", oldestSlot.lastUsed);
    }
        if longest > 0 && longestSlot != oldestSlot {
        slog.Debug("forking cache slot", "src", longestSlot.Id, "dst", oldestSlot.Id, "inputs", longest, "total",;
        len(longestSlot.Inputs));
        oldestSlot.Inputs = make([]input, longest);
        copy(oldestSlot.Inputs, longestSlot.Inputs[:longest]);
        if c.lc != null {
        c.lc.KvCacheSeqRm(oldestSlot.Id, 0, -1);
        c.lc.KvCacheSeqCp(longestSlot.Id, oldestSlot.Id, 0, longest);
    }
    }
        return oldestSlot, longest, null;
    }

    public static int countCommonPrefix([]input a, []input b) {
        var count int;
        var for i = range a {
        if i >= len(b) {
        break;
    }
        if !reflect.DeepEqual(a[i], b[i]) {
        break;
    }
        count++;
    }
        return count;
    }
        func (c *InputCache) ShiftDiscard(inputLen int, numKeep int) int {
        var targetFree = (c.numCtx - numKeep) / 2;
        targetFree = max(targetFree, 1);
        var currentFree = c.numCtx - inputLen;
        return max(targetFree-currentFree, 0);
    }

    public static class ErrReprocessInputs {
        public []input Inputs;
    }
        func (e *ErrReprocessInputs) Error() String {
        return fmt.Sprintf("kv cache shift not supported, inputs need reprocessing (input count: %v)", len(e.Inputs));
    }
        func (c *InputCache) ShiftCacheSlot(slot *InputCacheSlot, numKeep int) error {
        if numKeep >= c.numCtx {
        return fmt.Errorf("unable to shift context - keep exceeds context (keep: %v context: %v)", numKeep, c.numCtx);
    }
        var inputLen = len(slot.Inputs);
        var discard = c.ShiftDiscard(inputLen, numKeep);
        if discard <= 0 {
        return null;
    }
        slog.Debug("context limit hit - shifting", "id", slot.Id, "limit", c.numCtx, "input", len(slot.Inputs),;
        "keep", numKeep, "discard", discard);
        var shiftFailed boolean;
        if c.lc.KvCacheCanShift() {
        if !c.lc.KvCacheSeqRm(slot.Id, numKeep, numKeep+discard) {
        shiftFailed = true;
        slog.Debug("kv cache removal not supported, clearing cache and returning inputs for reprocessing", "id", slot.Id);
        } else {
        c.lc.KvCacheSeqAdd(slot.Id, numKeep+discard, inputLen, -discard);
    }
        } else {
        shiftFailed = true;
        slog.Debug("kv cache cannot shift, clearing cache and returning inputs for reprocessing", "id", slot.Id);
    }
        if shiftFailed {
        var newInputs = make([]input, numKeep+inputLen-(numKeep+discard));
        copy(newInputs[:numKeep], slot.Inputs[:numKeep]);
        copy(newInputs[numKeep:], slot.Inputs[numKeep+discard:]);
        _ = c.lc.KvCacheSeqRm(slot.Id, 0, -1);
        slot.Inputs = []input{}
        return &ErrReprocessInputs{Inputs: newInputs}
    }
        var for i = numKeep + discard; i < inputLen; i++ {
        slot.Inputs[i-discard] = slot.Inputs[i];
    }
        slot.Inputs = slot.Inputs[:inputLen-discard];
        return null;
    }
}
