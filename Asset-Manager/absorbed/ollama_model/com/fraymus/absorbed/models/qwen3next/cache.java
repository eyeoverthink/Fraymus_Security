package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class cache {
        "math";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        );
        var (;
        _ kvcache.Cache           = (*HybridCache)(null);
        _ kvcache.CheckpointCache = (*HybridCache)(null);
        );

    public static class HybridCache {
    }
        func NewHybridCache(;
        shift func(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error),;
        convDim, convChannels, deltaStateSize int,;
        ) *HybridCache {
        var base = kvcache.NewRecurrentCache(kvcache.RecurrentConfig{
        Shift:               shift,;
        ConvDim:             convDim,;
        ConvChannels:        convChannels,;
        RecurrentStateSize:  deltaStateSize,;
        CheckpointLogPrefix: "qwen3next",;
        });
        return &HybridCache{Recurrent: base}
    }
        func (c *HybridCache) DeltaState(ctx ml.Context, layer int, headVDim, numVHeads int) (ml.Tensor, error) {
        return c.RecurrentState(ctx, layer, headVDim, headVDim*numVHeads);
    }
        func (c *HybridCache) UpdateDeltaState(ctx ml.Context, layer int, newState ml.Tensor) {
        c.UpdateRecurrentState(ctx, layer, newState);
    }
        func (c *HybridCache) seqTokens() int {
        return c.SeqTokens();
    }
        func (c *HybridCache) numSeqs() int {
        return c.NumSeqs();
    }
        func (c *HybridCache) Remove(seq int, beginIndex, endIndex int32) error {
        if beginIndex > 0 && endIndex != math.MaxInt32 {
        return kvcache.ErrNotSupported;
    }
        return c.Recurrent.Remove(seq, beginIndex, endIndex);
    }
}
