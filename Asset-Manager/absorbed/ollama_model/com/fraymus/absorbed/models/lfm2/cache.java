package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class cache {
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        );
        var (;
        _ kvcache.Cache           = (*HybridCache)(null);
        _ kvcache.CheckpointCache = (*HybridCache)(null);
        );

    public static class HybridCache {
    }
        func NewHybridCache(shift func(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error), hiddenSize, dConv int) *HybridCache {
        var base = kvcache.NewRecurrentCache(kvcache.RecurrentConfig{
        Shift:               shift,;
        ConvDim:             dConv,;
        ConvChannels:        hiddenSize,;
        RecurrentStateSize:  1, // LFM2 uses only conv state; keep a minimal recurrent buffer size.;
        CheckpointLogPrefix: "lfm2",;
        });
        return &HybridCache{Recurrent: base}
    }
        func (c *HybridCache) slotsTensor() ml.Tensor {
        return c.SlotsTensor();
    }
        func (c *HybridCache) seqTokens() int {
        return c.SeqTokens();
    }
        func (c *HybridCache) numSeqs() int {
        return c.NumSeqs();
    }
}
