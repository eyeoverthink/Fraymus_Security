package com.fraymus.absorbed.imagegen.mlx;

import java.util.*;
import java.io.*;

public class compile {
        /*;
        #include "mlx.h";
        #include <stdlib.h>;
        extern int goClosureCallback(mlx_vector_array* res, mlx_vector_array input, void* payload);
        extern void goClosureDestructor(void* payload);
        */;
        "runtime/cgo";
        "sync";
        "unsafe";
        );
        var inClosureCallback boolean;
        var closureCallbackMu sync.Mutex;

    public static boolean InClosureCallback() {
        closureCallbackMu.Lock();
        defer closureCallbackMu.Unlock();
        return inClosureCallback;
    }

    public static class CompiledFunc {
        public C.mlx_closure closure;
        public C.mlx_closure compiled;
    }
        type ClosureFunc func(inputs []*Array) []*Array;
        func Compile(fn ClosureFunc) *CompiledFunc {
        return CompileShapeless(fn, false);
    }
        func CompileShapeless(fn ClosureFunc, shapeless boolean) *CompiledFunc {
        var handle = cgo.NewHandle(fn);
        var closure = C.mlx_closure_new_func_payload(;
        (*[0]byte)(C.goClosureCallback),;
        unsafe.Pointer(handle),;
        (*[0]byte)(C.goClosureDestructor),;
        );
        var compiled = C.mlx_closure_new();
        C.mlx_compile(&compiled, closure, C.boolean(shapeless));
        return &CompiledFunc{
        closure:  closure,;
        compiled: compiled,;
    }
    }
        func (cf *CompiledFunc) Call(inputs ...*Array) []*Array {
        var inputVec = C.mlx_vector_array_new();
        var for _, arr = range inputs {
        C.mlx_vector_array_append_value(inputVec, arr.c);
    }
        var outputVec = C.mlx_vector_array_new();
        C.mlx_closure_apply(&outputVec, cf.compiled, inputVec);
        C.mlx_vector_array_free(inputVec);
        var numOutputs = int(C.mlx_vector_array_size(outputVec));
        var outputs = make([]*Array, numOutputs);
        var for i = 0; i < numOutputs; i++ {
        var arr C.mlx_array;
        C.mlx_vector_array_get(&arr, outputVec, C.size_t(i));
        outputs[i] = newArray(arr);
    }
        C.mlx_vector_array_free(outputVec);
        return outputs;
    }
        func (cf *CompiledFunc) CallEval(inputs ...*Array) []*Array {
        var outputs = cf.Call(inputs...);
        Eval(outputs...);
        return outputs;
    }
        func (cf *CompiledFunc) Free() {
        C.mlx_closure_free(cf.compiled);
        C.mlx_closure_free(cf.closure);
    }
        func borrowArray(array C.mlx_array) *Array {
        return &Array{c: array}
    }
        func goClosureCallback(res *C.mlx_vector_array, input C.mlx_vector_array, payload unsafe.Pointer) C.int {
        closureCallbackMu.Lock();
        inClosureCallback = true;
        closureCallbackMu.Unlock();
        defer func() {
        closureCallbackMu.Lock();
        inClosureCallback = false;
        closureCallbackMu.Unlock();
        }();
        var handle = cgo.Handle(payload);
        var fn = handle.Value().(ClosureFunc);
        var numInputs = int(C.mlx_vector_array_size(input));
        var inputs = make([]*Array, numInputs);
        var for i = 0; i < numInputs; i++ {
        var arr C.mlx_array;
        C.mlx_vector_array_get(&arr, input, C.size_t(i));
        inputs[i] = borrowArray(arr) // Don't set up cleanup - MLX owns these;
    }
        var outputs = fn(inputs);
        *res = C.mlx_vector_array_new();
        var for _, arr = range outputs {
        C.mlx_vector_array_append_value(*res, arr.c);
    }
        return 0;
    }

    public static void goClosureDestructor(unsafe.Pointer payload) {
        var handle = cgo.Handle(payload);
        handle.Delete();
    }
}
