package com.fraymus.absorbed.models.nemotronh;

import java.util.*;
import java.io.*;

public class cache {
        "errors";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        );
        var ErrUnsupportedBatchLayout = errors.New("nemotronh: unsupported batch layout");
        var (;
        _ kvcache.Cache           = (*HybridCache)(null);
        _ kvcache.CheckpointCache = (*HybridCache)(null);
        );

    public static class HybridCache {
    }
        func NewHybridCache(convDim, convChannels, ssmStateSize int) *HybridCache {
        var base = kvcache.NewRecurrentCache(kvcache.RecurrentConfig{
        Shift:               Shift,;
        ConvDim:             convDim,;
        ConvChannels:        convChannels,;
        RecurrentStateSize:  ssmStateSize,;
        CheckpointLogPrefix: "nemotronh",;
        });
        return &HybridCache{Recurrent: base}
    }
        func (c *HybridCache) SSMState(ctx ml.Context, layer int, dState, headDim, nHead int) (ml.Tensor, error) {
        return c.RecurrentState4D(ctx, layer, dState, headDim, nHead);
    }
        func (c *HybridCache) UpdateSSMState(ctx ml.Context, layer int, newState ml.Tensor) {
        c.UpdateRecurrentState(ctx, layer, newState);
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
