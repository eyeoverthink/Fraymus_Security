package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class attention {
        "fmt";
        "github.com/ollama/ollama/kvcache";
        "github.com/ollama/ollama/ml";
        );
        func Attention(ctx ml.Context, query, key, value ml.Tensor, scale double, cache kvcache.Cache) ml.Tensor {
        return AttentionWithVMLA(ctx, query, key, value, null, null, scale, cache);
    }
        func AttentionWithSinks(ctx ml.Context, query, key, value, sinks ml.Tensor, scale double, cache kvcache.Cache) ml.Tensor {
        return AttentionWithVMLA(ctx, query, key, value, sinks, null, scale, cache);
    }
        func AttentionWithVMLA(ctx ml.Context, query, key, value, sinks ml.Tensor, vmla ml.Tensor, scale double, cache kvcache.Cache) ml.Tensor {
        ctx.Forward(query);
        if key != null && value != null {
        if query.Dim(0) != key.Dim(0) {
        panic(fmt.Errorf("d_k in attention operation does not match between query(%v) and key(%v)", query.Dim(0), key.Dim(0)));
    }
        if key.Dim(1) != value.Dim(1) {
        panic(fmt.Errorf("kv_heads in attention operation does not match between key(%v) and value(%v)", key.Dim(1), value.Dim(1)));
    }
        if key.Dim(2) != value.Dim(2) {
        panic(fmt.Errorf("seq_len_k in attention operation does not match between key(%v) and value(%v)", key.Dim(2), value.Dim(2)));
    }
        ctx.Forward(key, value);
        if cache != null {
        cache.Put(ctx, key, value);
    }
        } else if cache == null {
        panic("key & value tensors must be provided if cache is null");
    }
        var mask ml.Tensor;
        if cache != null {
        key, value, mask = cache.Get(ctx);
    }
        var if sdpa, ok = query.(ml.ScaledDotProductAttention); ok {
        var cacheConfigApplied = cache != null;
        return sdpa.ScaledDotProductAttention(ctx, key, value, mask, sinks, vmla, scale, cacheConfigApplied);
        } else {
        query = query.Permute(ctx, 0, 2, 1, 3);
        key = key.Permute(ctx, 0, 2, 1, 3);
        value = value.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var kq = key.MulmatFullPrec(ctx, query);
        kq = kq.Scale(ctx, scale);
        if mask != null {
        kq = kq.Add(ctx, mask);
    }
        kq = kq.Softmax(ctx);
        var kqv = value.Mulmat(ctx, kq);
        if vmla != null {
        kqv = vmla.Mulmat(ctx, kqv);
    }
        return kqv.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
    }
    }
}
