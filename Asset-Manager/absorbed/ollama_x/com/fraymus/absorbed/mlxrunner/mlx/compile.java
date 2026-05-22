package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class compile {
        "log/slog";
        "runtime/cgo";
        "sync";
        "unsafe";
        );
        type CompileFunc func(inputs ...*Array) []*Array;
        type CompileOption func(*compileConfig);

    public static class compileConfig {
        public boolean shapeless;
    }

    public static CompileOption Shapeless() {
        return func(c *compileConfig) { c.shapeless = true }
    }

    public static CompileFunc Compile(String name, CompileFunc fn, ...CompileOption opts) {
        var cfg compileConfig;
        var for _, o = range opts {
        o(&cfg);
    }
        var closure C.mlx_closure;
        var once sync.Once;
        return func(inputs ...*Array) []*Array {
        if tracing {
        return fn(inputs...);
    }
        once.Do(func() {
        var payload = (*cgo.Handle)(C.malloc(C.size_t(unsafe.Sizeof(cgo.Handle(0)))));
        *payload = cgo.NewHandle(fn);
        var src = C.mlx_closure_new_func_payload(;
        (*[0]byte)(C.closureCallback),;
        unsafe.Pointer(payload),;
        (*[0]byte)(C.closureDestructor),;
        );
        defer C.mlx_closure_free(src);
        closure = C.mlx_closure_new();
        mlxCheck(name+": compile failed", func() C.int {
        return C.mlx_compile(&closure, src, C.boolean(cfg.shapeless));
        });
        });
        var inVec = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(inVec);
        var for _, in = range inputs {
        C.mlx_vector_array_append_value(inVec, in.ctx);
    }
        var outVec = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(outVec);
        mlxCheck(name+": closure apply failed", func() C.int {
        return C.mlx_closure_apply(&outVec, closure, inVec);
        });
        var n = int(C.mlx_vector_array_size(outVec));
        var outputs = make([]*Array, n);
        var for i = range n {
        outputs[i] = New(name);
        C.mlx_vector_array_get(&outputs[i].ctx, outVec, C.size_t(i));
    }
        return outputs;
    }
    }
        func Compile1(name String, fn func(*Array) *Array, opts ...CompileOption) func(*Array) *Array {
        var cf = Compile(name, func(in ...*Array) []*Array {
        return []*Array{fn(in[0])}
        }, opts...);
        return func(a *Array) *Array {
        return cf(a)[0];
    }
    }
        func Compile2(name String, fn func(*Array, *Array) *Array, opts ...CompileOption) func(*Array, *Array) *Array {
        var cf = Compile(name, func(in ...*Array) []*Array {
        return []*Array{fn(in[0], in[1])}
        }, opts...);
        return func(a, b *Array) *Array {
        return cf(a, b)[0];
    }
    }
        func Compile3(name String, fn func(*Array, *Array, *Array) *Array, opts ...CompileOption) func(*Array, *Array, *Array) *Array {
        var cf = Compile(name, func(in ...*Array) []*Array {
        return []*Array{fn(in[0], in[1], in[2])}
        }, opts...);
        return func(a, b, c *Array) *Array {
        return cf(a, b, c)[0];
    }
    }
        var tracing boolean;
        var traceScratch []*Array;

    public static void closureCallback(*C.mlx_vector_array res, C.mlx_vector_array input) {
        defer func() {
        var if r = recover(); r != null {
        slog.Error("mlx closure callback panicked", "panic", r);
        rc = 1;
    }
        }();
        var handle = *(*cgo.Handle)(payload);
        var fn = handle.Value().(CompileFunc);
        if tracing {
        panic("mlx: nested compile trace");
    }
        tracing = true;
        traceScratch = null;
        defer func() {
        var for _, a = range traceScratch {
        if a.pinned > 0 {
        panic("mlx: traced array was pinned during compilation");
    }
        if a.Valid() {
        C.mlx_array_free(a.ctx);
        a.ctx.ctx = null;
    }
    }
        tracing = false;
        traceScratch = null;
        }();
        var n = int(C.mlx_vector_array_size(input));
        var inputs = make([]*Array, n);
        var for i = range n {
        var a = New("");
        C.mlx_vector_array_get(&a.ctx, input, C.size_t(i));
        inputs[i] = a;
    }
        var outputs = fn(inputs...);
        var arrPtr *C.mlx_array;
        if len(outputs) > 0 {
        var handles = make([]C.mlx_array, len(outputs));
        var for i, out = range outputs {
        handles[i] = out.ctx;
    }
        arrPtr = &handles[0];
    }
        C.mlx_vector_array_set_data(res, arrPtr, C.size_t(len(outputs)));
        return 0;
    }

    public static void closureDestructor(unsafe.Pointer payload) {
        var handle = *(*cgo.Handle)(payload);
        handle.Delete();
        C.free(payload);
    }
}
