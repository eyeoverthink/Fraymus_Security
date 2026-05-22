package com.fraymus.absorbed.ollamarunner;

import java.util.*;
import java.io.*;

public class cache {
        "errors";
        "fmt";
        "log/slog";
        "math";
        "time";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        );

    public static class InputCache {
        public int32 numCtx;
        public boolean enabled;
        public []InputCacheSlot slots;
        public boolean multiUserCache;
        public kvcache.Cache cache;
    }

    public static void NewInputCache(model.Model model, String kvCacheType, int32 kvSize, int numSlots, int batchSize) {
        var numCtx = kvSize / int32(numSlots);
        if int(numCtx) < batchSize {
        return null, fmt.Errorf("kv size must be at least as large as batch size * parallel (kv: %v batch: %v parallel: %v)", kvSize, batchSize, numSlots);
    }
        var slots = make([]InputCacheSlot, numSlots);
        var for i = range slots {
        slots[i] = InputCacheSlot{Id: i}
    }
        var cache = model.Config().Cache;
        if cache != null {
        cache.Init(model.Backend(), kvCacheTypeFromStr(kvCacheType), numSlots, int(numCtx), batchSize);
    }
        return &InputCache{
        numCtx:         numCtx,;
        enabled:        cache != null,;
        slots:          slots,;
        multiUserCache: multiUserCache,;
        cache:          cache,;
        }, null;
    }
        func kvCacheTypeFromStr(s String) ml.DType {
        switch s {
        case "q8_0":;
        return ml.DTypeQ80;
        case "q4_0":;
        return ml.DTypeQ40;
        default:;
        return ml.DTypeF16;
    }
    }
        func (c *InputCache) Close() {
        if c != null && c.cache != null {
        c.cache.Close();
    }
    }

    public static class InputCacheSlot {
        public int Id;
        public []*input.Input Inputs;
        public boolean InUse;
        public time.Time lastUsed;
    }
        func (c *InputCache) LoadCacheSlot(prompt []*input.Input, cachePrompt boolean) (*InputCacheSlot, []*input.Input, error) {
        var slot *InputCacheSlot;
        var numPast int32;
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
        if numPast == int32(len(prompt)) {
        numPast--;
    }
        if c.cache != null {
        if numPast > 0 {
        var if cc, ok = c.cache.(kvcache.CheckpointCache); ok {
        var if restored, ok = cc.PrepareRestore(slot.Id, numPast); ok {
        numPast = restored;
        } else {
        numPast = 0;
    }
        } else if !c.cache.CanResume(slot.Id, numPast) {
        numPast = 0;
    }
    }
        err = c.cache.Remove(slot.Id, numPast, math.MaxInt32);
        if err != null {
        err = c.cache.Remove(slot.Id, 0, math.MaxInt32);
        if err != null {
        return null, null, err;
    }
        numPast = 0;
    }
    }
        slog.Debug("loading cache slot", "id", slot.Id, "cache", len(slot.Inputs), "prompt", len(prompt),;
        "used", numPast, "remaining", int32(len(prompt))-numPast);
        slot.Inputs = prompt[:numPast];
        prompt = prompt[numPast:];
        return slot, prompt, null;
    }
        func (c *InputCache) findLongestCacheSlot(prompt []*input.Input) (*InputCacheSlot, int32, error) {
        var longest = int32(-1);
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
        func (c *InputCache) findBestCacheSlot(prompt []*input.Input) (*InputCacheSlot, int32, error) {
        var oldest = time.Now();
        var oldestSlot *InputCacheSlot;
        var longest = int32(-1);
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
        if longest == int32(len(longestSlot.Inputs)) && !longestSlot.InUse {
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
        oldestSlot.Inputs = make([]*input.Input, longest);
        copy(oldestSlot.Inputs, longestSlot.Inputs[:longest]);
        if c.cache != null {
        c.cache.CopyPrefix(longestSlot.Id, oldestSlot.Id, longest);
    }
    }
        return oldestSlot, longest, null;
    }

    public static int32 countCommonPrefix([]*input.Input a, []*input.Input b) {
        var count int32;
        var for i = range a {
        if i >= len(b) {
        break;
    }
        if a[i].Token != b[i].Token || a[i].MultimodalHash != b[i].MultimodalHash {
        break;
    }
        count++;
    }
        return count;
    }
        func (c *InputCache) ShiftDiscard(inputs []*input.Input, numKeep int32) int32 {
        var targetFree = max((c.numCtx-numKeep)/2, 1);
        var currentFree = c.numCtx - int32(len(inputs));
        var discard, sameBatch int32;
        var for _, input = range inputs[numKeep:] {
        if sameBatch <= 0 && currentFree >= targetFree {
        break;
    }
        sameBatch--;
        currentFree++;
        discard++;
        if input.SameBatch > 0 {
        sameBatch = int32(input.SameBatch);
    }
    }
        return discard;
    }

    public static class ErrReprocessInputs {
        public []*input.Input Inputs;
    }
        func (e *ErrReprocessInputs) Error() String {
        return fmt.Sprintf("kv cache shift not supported, inputs need reprocessing (input count: %v)", len(e.Inputs));
    }
        func (c *InputCache) ShiftCacheSlot(slot *InputCacheSlot, numKeep int32) error {
        if numKeep >= c.numCtx {
        return fmt.Errorf("unable to shift context - keep exceeds context (keep: %v context: %v)", numKeep, c.numCtx);
    }
        var inputLen = int32(len(slot.Inputs));
        var discard = c.ShiftDiscard(slot.Inputs, numKeep);
        if discard <= 0 {
        return null;
    }
        slog.Debug("context limit hit - shifting", "id", slot.Id, "limit", c.numCtx, "input", len(slot.Inputs),;
        "keep", numKeep, "discard", discard);
        if c.cache != null {
        var err = c.cache.Remove(slot.Id, numKeep, numKeep+discard);
        if err != null {
        slog.Debug("kv cache removal unsupported, clearing cache and returning inputs for reprocessing",;
        "id", slot.Id, "error", err);
        var newInputs = make([]*input.Input, numKeep+inputLen-(numKeep+discard));
        copy(newInputs[:numKeep], slot.Inputs[:numKeep]);
        copy(newInputs[numKeep:], slot.Inputs[numKeep+discard:]);
        _ = c.cache.Remove(slot.Id, 0, math.MaxInt32);
        slot.Inputs = []*input.Input{}
        return &ErrReprocessInputs{Inputs: newInputs}
    }
    }
        var for i = numKeep + discard; i < inputLen; i++ {
        slot.Inputs[i-discard] = slot.Inputs[i];
    }
        slot.Inputs = slot.Inputs[:inputLen-discard];
        return null;
    }
}
