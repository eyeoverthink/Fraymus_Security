package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class fast {
        "unsafe";
        );
        func ScaledDotProductAttention(query, key, value, mask *Array, scale float32) *Array {
        if mask == null {
        mask = New("");
    }
        var sinks = New("");
        var mode = "causal";
        var cMode = C.CString(mode);
        defer C.free(unsafe.Pointer(cMode));
        var out = New("FAST_SDPA");
        C.mlx_fast_scaled_dot_product_attention(&out.ctx, query.ctx, key.ctx, value.ctx, C.float(scale), cMode, mask.ctx, sinks.ctx, DefaultStream().ctx);
        return out;
    }

    public static class LayerNorm {
        public Array Weight;
        public Array Bias;
    }
        func (r *LayerNorm) Forward(x *Array, eps float32) *Array {
        var out = New("FAST_LAYERNORM");
        C.mlx_fast_layer_norm(&out.ctx, x.ctx, r.Weight.ctx, r.Bias.ctx, C.float(eps), DefaultStream().ctx);
        return out;
    }

    public static class RMSNorm {
        public Array Weight;
    }
        func (r RMSNorm) Forward(x *Array, eps float32) *Array {
        var out = New("FAST_RMSNORM");
        C.mlx_fast_rms_norm(&out.ctx, x.ctx, r.Weight.ctx, C.float(eps), DefaultStream().ctx);
        return out;
    }

    public static class RoPE {
        public int Dims;
        public boolean Traditional;
        public float32 Base;
        public float32 Scale;
    }
        func (r RoPE) Forward(t *Array, offset int) *Array {
        var freqs = New("");
        var out = New("FAST_ROPE");
        C.mlx_fast_rope(;
        &out.ctx,;
        t.ctx,;
        C.int(r.Dims),;
        C._Bool(r.Traditional),;
        C.mlx_optional_float{
        value:     C.float(r.Base),;
        has_value: C._Bool(func() boolean { return r.Base != 0 }()),;
        },;
        C.float(r.Scale),;
        C.int(offset),;
        freqs.ctx,;
        DefaultStream().ctx,;
        );
        return out;
    }
}
