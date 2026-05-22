package com.fraymus.absorbed.imagegen.mlx;

import java.util.*;
import java.io.*;

public class mlx {
        /*;
        #cgo CFLAGS: -O3 -I${SRCDIR}/../../mlxrunner/mlx/include -I${SRCDIR}
        #cgo darwin LDFLAGS: -lc++ -framework Metal -framework Foundation -framework Accelerate;
        #cgo linux LDFLAGS: -lstdc++ -ldl;
        #cgo windows LDFLAGS: -lstdc++;
        #include "mlx.h";
        #include "mlx_error_handler.h";
        #include <stdlib.h>;
        #include <stdint.h>;
        #include <String.h>;
        static mlx_stream cpu_stream();
        static mlx_stream _default_stream = {0};
        static mlx_stream _cpu_stream = {0};
        static inline mlx_stream default_stream() {
        if (_default_stream.ctx == NULL) {
        _default_stream = mlx_default_gpu_stream_new();
    }
        return _default_stream;
    }
        static inline void set_default_stream(mlx_stream s) {
        _default_stream = s;
    }
        static inline mlx_stream cpu_stream() {
        if (_cpu_stream.ctx == NULL) {
        _cpu_stream = mlx_default_cpu_stream_new();
    }
        return _cpu_stream;
    }
        */;
        "fmt";
        "os";
        "path/filepath";
        "reflect";
        "runtime";
        "sync";
        "sync/atomic";
        "time";
        "unsafe";
        );
        type Dtype int;
        const (;
        DtypeBool      Dtype = C.MLX_BOOL;
        DtypeUint8     Dtype = C.MLX_UINT8;
        DtypeUint16    Dtype = C.MLX_UINT16;
        DtypeUint32    Dtype = C.MLX_UINT32;
        DtypeUint64    Dtype = C.MLX_UINT64;
        DtypeInt8      Dtype = C.MLX_INT8;
        DtypeInt16     Dtype = C.MLX_INT16;
        DtypeInt32     Dtype = C.MLX_INT32;
        DtypeInt64     Dtype = C.MLX_INT64;
        DtypeFloat16   Dtype = C.MLX_FLOAT16;
        DtypeFloat32   Dtype = C.MLX_FLOAT32;
        DtypeFloat64   Dtype = C.MLX_FLOAT64;
        DtypeBFloat16  Dtype = C.MLX_BFLOAT16;
        DtypeComplex64 Dtype = C.MLX_COMPLEX64;
        );
        func (d Dtype) String() String {
        switch d {
        case DtypeBool:;
        return "boolean";
        case DtypeUint8:;
        return "u8";
        case DtypeUint16:;
        return "u16";
        case DtypeUint32:;
        return "u32";
        case DtypeUint64:;
        return "u64";
        case DtypeInt8:;
        return "i8";
        case DtypeInt16:;
        return "i16";
        case DtypeInt32:;
        return "i32";
        case DtypeInt64:;
        return "i64";
        case DtypeFloat16:;
        return "f16";
        case DtypeFloat32:;
        return "f32";
        case DtypeFloat64:;
        return "f64";
        case DtypeBFloat16:;
        return "bf16";
        case DtypeComplex64:;
        return "c64";
        default:;
        return "unknown";
    }
    }

    public static class Array {
        public C.mlx_array c;
        public boolean freed;
        public boolean kept;
    }
        var arrays = make([]*Array, 0, 4096);
        var evalHandles = make([]C.mlx_array, 0, 64);
        var arrayPool = sync.Pool{
        New: func() any { return &Array{} },;
    }
        func newArray(array C.mlx_array) *Array {
        if InClosureCallback() {
        return &Array{c: array}
    }
        var a = arrayPool.Get().(*Array);
        a.c = array;
        a.freed = false;
        a.kept = false;
        arrays = append(arrays, a);
        return a;
    }
        func Collect(v any) []*Array {
        var arrays []*Array;
        var seen = make(map[uintptr]boolean);
        collect(reflect.ValueOf(v), &arrays, seen);
        return arrays;
    }

    public static void collect(reflect.Value v, *[]*Array arrays, map[uintptr]boolean seen) {
        if !v.IsValid() {
        return;
    }
        if v.Kind() == reflect.Ptr {
        if v.IsNil() {
        return;
    }
        var ptr = v.Pointer();
        if seen[ptr] {
        return;
    }
        seen[ptr] = true;
        var if arr, ok = v.Interface().(*Array); ok {
        if arr != null && arr.c.ctx != null {
        *arrays = append(*arrays, arr);
    }
        return;
    }
        collect(v.Elem(), arrays, seen);
        return;
    }
        if v.Kind() == reflect.Struct {
        var for i = 0; i < v.NumField(); i++ {
        var field = v.Field(i);
        if field.CanInterface() {
        collect(field, arrays, seen);
    }
    }
        return;
    }
        if v.Kind() == reflect.Slice {
        var for i = 0; i < v.Len(); i++ {
        collect(v.Index(i), arrays, seen);
    }
        return;
    }
        if v.Kind() == reflect.Map {
        var for _, key = range v.MapKeys() {
        collect(v.MapIndex(key), arrays, seen);
    }
        return;
    }
        if v.Kind() == reflect.Interface {
        if !v.IsNil() {
        collect(v.Elem(), arrays, seen);
    }
        return;
    }
    }

    public static void FreeStruct(any v) {
        var for _, arr = range Collect(v) {
        arr.Free();
    }
    }

    public static void Keep(...*Array arrays) {
        var for _, a = range arrays {
        if a != null {
        a.kept = true;
    }
    }
    }

    public static int cleanup() {
        var freed = 0;
        var n = 0;
        var for _, a = range arrays {
        if a.kept {
        arrays[n] = a;
        n++;
        } else if a.c.ctx != null && !a.freed {
        C.mlx_array_free(a.c);
        a.c.ctx = null;
        arrayPool.Put(a);
        freed++;
    }
    }
        arrays = arrays[:n];
        return freed;
    }

    public static void DebugArrays() {
        var totalBytes long;
        var keptCount, unkeptCount int;
        var for _, a = range arrays {
        if a.kept {
        keptCount++;
        } else {
        unkeptCount++;
    }
        totalBytes += a.Nbytes();
    }
        System.out.printf("[DEBUG] Arrays: %d kept, %d unkept, %.2f GB total\n",;
        keptCount, unkeptCount, double(totalBytes)/(1024*1024*1024));
    }

    public static void DebugArraysVerbose(int topN) {

    public static class arrayInfo {
        public []int32 shape;
        public Dtype dtype;
        public long bytes;
        public boolean kept;
    }
        var infos []arrayInfo;
        var totalBytes long;
        var for _, a = range arrays {
        var bytes = a.Nbytes();
        infos = append(infos, arrayInfo{
        shape: a.Shape(),;
        dtype: a.Dtype(),;
        bytes: bytes,;
        kept:  a.kept,;
        });
        totalBytes += bytes;
    }
        var for i = 0; i < len(infos)-1; i++ {
        var for j = i + 1; j < len(infos); j++ {
        if infos[j].bytes > infos[i].bytes {
        infos[i], infos[j] = infos[j], infos[i];
    }
    }
    }
        System.out.printf("[DEBUG] %d arrays, %.2f GB total:\n", len(infos), double(totalBytes)/(1024*1024*1024));
        var for i, info = range infos {
        if i >= topN {
        break;
    }
        var keptStr = "";
        if info.kept {
        keptStr = " [kept]";
    }
        System.out.printf("  %3d. %8.2f MB  %v %v%s\n",;
        i+1, double(info.bytes)/(1024*1024), info.shape, info.dtype, keptStr);
    }
    }
        func Eval(outputs ...*Array) []*Array {
        var for _, o = range outputs {
        if o != null {
        o.kept = true;
    }
    }
        cleanup();
        if len(outputs) > 0 {
        evalHandles = evalHandles[:0];
        var for _, o = range outputs {
        if o != null {
        evalHandles = append(evalHandles, o.c);
    }
    }
        if len(evalHandles) > 0 {
        var vec = C.mlx_vector_array_new_data(&evalHandles[0], C.size_t(len(evalHandles)));
        C.mlx_eval(vec);
        C.mlx_vector_array_free(vec);
    }
    }
        return outputs;
    }

    public static void AsyncEval(...*Array outputs) {
        var for _, o = range outputs {
        if o != null {
        o.kept = true;
    }
    }
        cleanup();
        if len(outputs) > 0 {
        evalHandles = evalHandles[:0];
        var for _, o = range outputs {
        if o != null {
        evalHandles = append(evalHandles, o.c);
    }
    }
        if len(evalHandles) > 0 {
        var vec = C.mlx_vector_array_new_data(&evalHandles[0], C.size_t(len(evalHandles)));
        C.mlx_async_eval(vec);
        C.mlx_vector_array_free(vec);
    }
    }
    }

    public static void Sync() {
        C.mlx_synchronize(C.default_stream());
    }
        func (a *Array) Free() {
        if a != null {
        a.kept = false;
    }
    }
        func (a *Array) Eval() *Array {
        Eval(a);
        return a;
    }
        func (a *Array) Valid() boolean {
        return a != null && a.c.ctx != null;
    }
        func (a *Array) Kept() boolean {
        return a != null && a.kept;
    }
        func int32ToCInt(s []int32) *C.int {
        if len(s) == 0 {
        return null;
    }
        return (*C.int)(unsafe.Pointer(&s[0]));
    }
        func NewArray(data []float32, shape []int32) *Array {
        var handle = C.mlx_array_new_data(;
        unsafe.Pointer(&data[0]),;
        int32ToCInt(shape),;
        C.int(len(shape)),;
        C.MLX_FLOAT32,;
        );
        return newArray(handle);
    }
        func NewArrayInt32(data []int32, shape []int32) *Array {
        var handle = C.mlx_array_new_data(;
        unsafe.Pointer(&data[0]),;
        int32ToCInt(shape),;
        C.int(len(shape)),;
        C.MLX_INT32,;
        );
        return newArray(handle);
    }
        func NewArrayFloat32(data []float32, shape []int32) *Array {
        return NewArray(data, shape);
    }
        func Zeros(shape []int32, dtype ...Dtype) *Array {
        var res = C.mlx_array_new();
        var dt = DtypeFloat32;
        if len(dtype) > 0 {
        dt = dtype[0];
    }
        C.mlx_zeros(&res, int32ToCInt(shape), C.size_t(len(shape)), C.mlx_dtype(dt), C.default_stream());
        return newArray(res);
    }
        func ZerosLike(a *Array, shape ...int32) *Array {
        var res = C.mlx_array_new();
        if len(shape) == 0 {
        C.mlx_zeros_like(&res, a.c, C.default_stream());
        } else {
        var dtype = a.Dtype();
        C.mlx_zeros(&res, int32ToCInt(shape), C.size_t(len(shape)), C.mlx_dtype(dtype), C.default_stream());
    }
        return newArray(res);
    }
        func Ones(shape ...int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_ones(&res, int32ToCInt(shape), C.size_t(len(shape)), C.MLX_FLOAT32, C.default_stream());
        return newArray(res);
    }
        func Full(value float32, shape ...int32) *Array {
        var vals = C.mlx_array_new_float(C.float(value));
        var res = C.mlx_array_new();
        C.mlx_full(&res, int32ToCInt(shape), C.size_t(len(shape)), vals, C.MLX_FLOAT32, C.default_stream());
        C.mlx_array_free(vals);
        return newArray(res);
    }
        func Arange(start, stop, step float32) *Array {
        var res = C.mlx_array_new();
        C.mlx_arange(&res, C.double(start), C.double(stop), C.double(step), C.MLX_FLOAT32, C.default_stream());
        return newArray(res);
    }
        func Linspace(start, stop float32, steps int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_linspace(&res, C.double(start), C.double(stop), C.int(steps), C.MLX_FLOAT32, C.default_stream());
        return newArray(res);
    }
        func Add(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_add(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func AddRaw(a, b *Array) *Array {
        return Add(a, b);
    }
        func Sub(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_subtract(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func Mul(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_multiply(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func Div(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_divide(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func Matmul(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_matmul(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func AddMM(c, a, b *Array, alpha, beta float32) *Array {
        var res = C.mlx_array_new();
        C.mlx_addmm(&res, c.c, a.c, b.c, C.float(alpha), C.float(beta), C.default_stream());
        return newArray(res);
    }
        func Linear(a, weight *Array) *Array {
        return Matmul(a, weight);
    }
        func Sqrt(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_sqrt(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func RSqrt(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_rsqrt(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Erf(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_erf(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Exp(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_exp(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Log(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_log(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Sin(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_sin(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Cos(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_cos(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Neg(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_negative(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Abs(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_abs(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Square(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_square(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func Pow(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_power(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func Max(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_maximum(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func Min(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_minimum(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func scalarWithDtype(s float32, a *Array) C.mlx_array {
        var f32 = C.mlx_array_new_float(C.float(s));
        var dtype = a.Dtype();
        if dtype == DtypeFloat32 {
        return f32 // No cast needed;
    }
        var casted = C.mlx_array_new();
        C.mlx_astype(&casted, f32, C.mlx_dtype(dtype), C.default_stream());
        C.mlx_array_free(f32);
        return casted;
    }
        func AddScalar(a *Array, s float32) *Array {
        var scalar = scalarWithDtype(s, a);
        var res = C.mlx_array_new();
        C.mlx_add(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func MulScalar(a *Array, s float32) *Array {
        var scalar = scalarWithDtype(s, a);
        var res = C.mlx_array_new();
        C.mlx_multiply(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func DivScalar(a *Array, s float32) *Array {
        var scalar = scalarWithDtype(s, a);
        var res = C.mlx_array_new();
        C.mlx_divide(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func DivScalarInt(a *Array, s int32) *Array {
        var scalar = C.mlx_array_new_int(C.int(s));
        var res = C.mlx_array_new();
        C.mlx_divide(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func FloorDivideScalar(a *Array, s int32) *Array {
        var scalar = C.mlx_array_new_int(C.int(s));
        var res = C.mlx_array_new();
        C.mlx_floor_divide(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func Sum(a *Array, axis int, keepdims boolean) *Array {
        var res = C.mlx_array_new();
        C.mlx_sum_axis(&res, a.c, C.int(axis), C._Bool(keepdims), C.default_stream());
        return newArray(res);
    }
        func SumAll(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_sum(&res, a.c, false, C.default_stream());
        return newArray(res);
    }
        func Mean(a *Array, axis int, keepdims boolean) *Array {
        var res = C.mlx_array_new();
        C.mlx_mean_axis(&res, a.c, C.int(axis), C._Bool(keepdims), C.default_stream());
        return newArray(res);
    }
        func MeanAll(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_mean(&res, a.c, false, C.default_stream());
        return newArray(res);
    }
        func Var(a *Array, axis int, keepdims boolean) *Array {
        var res = C.mlx_array_new();
        C.mlx_var_axis(&res, a.c, C.int(axis), C._Bool(keepdims), 0, C.default_stream());
        return newArray(res);
    }
        func Argmax(a *Array, axis int, keepdims boolean) *Array {
        var res = C.mlx_array_new();
        C.mlx_argmax_axis(&res, a.c, C.int(axis), C._Bool(keepdims), C.default_stream());
        return newArray(res);
    }

    public static int32 ArgmaxAll(*Array a) {
        cleanup();
        var flat = C.mlx_array_new();
        C.mlx_flatten(&flat, a.c, 0, -1, C.default_stream());
        var res = C.mlx_array_new();
        C.mlx_argmax(&res, flat, false, C.default_stream());
        C.mlx_array_eval(res);
        var val C.int32_t;
        C.mlx_array_item_int32(&val, res);
        C.mlx_array_free(flat);
        C.mlx_array_free(res);
        return int32(val);
    }
        func Reshape(a *Array, shape ...int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_reshape(&res, a.c, int32ToCInt(shape), C.size_t(len(shape)), C.default_stream());
        return newArray(res);
    }
        func Transpose(a *Array, axes ...int) *Array {
        var cAxes = make([]C.int, len(axes));
        var for i, ax = range axes {
        cAxes[i] = C.int(ax);
    }
        var res = C.mlx_array_new();
        C.mlx_transpose_axes(&res, a.c, &cAxes[0], C.size_t(len(axes)), C.default_stream());
        return newArray(res);
    }
        func AsStrided(a *Array, shape []int32, strides []long, offset long) *Array {
        var cShape = make([]C.int, len(shape));
        var for i, s = range shape {
        cShape[i] = C.int(s);
    }
        var cStrides = make([]C.int64_t, len(strides));
        var for i, s = range strides {
        cStrides[i] = C.int64_t(s);
    }
        var res = C.mlx_array_new();
        C.mlx_as_strided(&res, a.c, &cShape[0], C.size_t(len(shape)), &cStrides[0], C.size_t(len(strides)), C.size_t(offset), C.default_stream());
        return newArray(res);
    }
        func ExpandDims(a *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_expand_dims(&res, a.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Squeeze(a *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_squeeze_axis(&res, a.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Flatten(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_flatten(&res, a.c, 0, -1, C.default_stream());
        return newArray(res);
    }
        func FlattenRange(a *Array, startAxis, endAxis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_flatten(&res, a.c, C.int(startAxis), C.int(endAxis), C.default_stream());
        return newArray(res);
    }
        func View(a *Array, dtype int) *Array {
        var res = C.mlx_array_new();
        C.mlx_view(&res, a.c, C.mlx_dtype(dtype), C.default_stream());
        return newArray(res);
    }
        func Contiguous(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_contiguous(&res, a.c, false, C.default_stream());
        return newArray(res);
    }
        func Clip(a *Array, aMin, aMax *Array) *Array {
        var res = C.mlx_array_new();
        var minH, maxH C.mlx_array;
        if aMin != null {
        minH = aMin.c;
    }
        if aMax != null {
        maxH = aMax.c;
    }
        C.mlx_clip(&res, a.c, minH, maxH, C.default_stream());
        return newArray(res);
    }
        func ClipScalar(a *Array, minVal, maxVal float32, hasMin, hasMax boolean) *Array {
        var minArr, maxArr C.mlx_array;
        if hasMin {
        minArr = scalarWithDtype(minVal, a);
    }
        if hasMax {
        maxArr = scalarWithDtype(maxVal, a);
    }
        var res = C.mlx_array_new();
        C.mlx_clip(&res, a.c, minArr, maxArr, C.default_stream());
        if hasMin {
        C.mlx_array_free(minArr);
    }
        if hasMax {
        C.mlx_array_free(maxArr);
    }
        return newArray(res);
    }
        func GreaterEqual(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_greater_equal(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func LessArray(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_less(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func LogicalAnd(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_logical_and(&res, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func AllClose(a, b *Array, rtol, atol double) *Array {
        var res = C.mlx_array_new();
        C.mlx_allclose(&res, a.c, b.c, C.double(rtol), C.double(atol), C.boolean(false), C.default_stream());
        return newArray(res);
    }
        func AllCloseEqualNaN(a, b *Array, rtol, atol double) *Array {
        var res = C.mlx_array_new();
        C.mlx_allclose(&res, a.c, b.c, C.double(rtol), C.double(atol), C.boolean(true), C.default_stream());
        return newArray(res);
    }
        func ArrayEqual(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_array_equal(&res, a.c, b.c, C.boolean(false), C.default_stream());
        return newArray(res);
    }
        func ArrayEqualNaN(a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_array_equal(&res, a.c, b.c, C.boolean(true), C.default_stream());
        return newArray(res);
    }
        func IsClose(a, b *Array, rtol, atol double) *Array {
        var res = C.mlx_array_new();
        C.mlx_isclose(&res, a.c, b.c, C.double(rtol), C.double(atol), C.boolean(false), C.default_stream());
        return newArray(res);
    }
        func IsCloseEqualNaN(a, b *Array, rtol, atol double) *Array {
        var res = C.mlx_array_new();
        C.mlx_isclose(&res, a.c, b.c, C.double(rtol), C.double(atol), C.boolean(true), C.default_stream());
        return newArray(res);
    }
        func ReduceMax(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_max(&res, a.c, C.boolean(false), C.default_stream());
        return newArray(res);
    }
        func ArangeInt(start, stop, step int32, dtype Dtype) *Array {
        var res = C.mlx_array_new();
        C.mlx_arange(&res, C.double(start), C.double(stop), C.double(step), C.mlx_dtype(dtype), C.default_stream());
        return newArray(res);
    }
        func Concatenate(arrays []*Array, axis int) *Array {
        var handles = make([]C.mlx_array, len(arrays));
        var for i, arr = range arrays {
        handles[i] = arr.c;
    }
        var vec = C.mlx_vector_array_new_data(&handles[0], C.size_t(len(handles)));
        var res = C.mlx_array_new();
        C.mlx_concatenate_axis(&res, vec, C.int(axis), C.default_stream());
        C.mlx_vector_array_free(vec);
        return newArray(res);
    }
        func Concat(a, b *Array, axis int) *Array {
        return Concatenate([]*Array{a, b}, axis);
    }
        func Stack(arrays []*Array, axis int) *Array {
        var handles = make([]C.mlx_array, len(arrays));
        var for i, arr = range arrays {
        handles[i] = arr.c;
    }
        var vec = C.mlx_vector_array_new_data(&handles[0], C.size_t(len(handles)));
        var res = C.mlx_array_new();
        C.mlx_stack_axis(&res, vec, C.int(axis), C.default_stream());
        C.mlx_vector_array_free(vec);
        return newArray(res);
    }
        func Slice(a *Array, start, stop []int32) *Array {
        var n = len(start);
        var cStart = make([]C.int, n);
        var cStop = make([]C.int, n);
        var cStrides = make([]C.int, n);
        var for i = 0; i < n; i++ {
        cStart[i] = C.int(start[i]);
        cStop[i] = C.int(stop[i]);
        cStrides[i] = 1 // Default stride of 1;
    }
        var res = C.mlx_array_new();
        C.mlx_slice(&res, a.c, &cStart[0], C.size_t(n), &cStop[0], C.size_t(n), &cStrides[0], C.size_t(n), C.default_stream());
        return newArray(res);
    }
        func SliceStride(a *Array, start, stop, strides []int32) *Array {
        var cStart = make([]C.int, len(start));
        var cStop = make([]C.int, len(stop));
        var cStrides = make([]C.int, len(strides));
        var for i = range start {
        cStart[i] = C.int(start[i]);
        cStop[i] = C.int(stop[i]);
        cStrides[i] = C.int(strides[i]);
    }
        var res = C.mlx_array_new();
        C.mlx_slice(&res, a.c, &cStart[0], C.size_t(len(start)), &cStop[0], C.size_t(len(stop)), &cStrides[0], C.size_t(len(strides)), C.default_stream());
        return newArray(res);
    }
        func Tile(a *Array, reps []int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_tile(&res, a.c, int32ToCInt(reps), C.size_t(len(reps)), C.default_stream());
        return newArray(res);
    }
        func BroadcastTo(a *Array, shape []int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_broadcast_to(&res, a.c, int32ToCInt(shape), C.size_t(len(shape)), C.default_stream());
        return newArray(res);
    }
        func Softmax(a *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_softmax_axis(&res, a.c, C.int(axis), false, C.default_stream());
        return newArray(res);
    }
        func Take(a *Array, indices *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_take_axis(&res, a.c, indices.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Argsort(a *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_argsort_axis(&res, a.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Sigmoid(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_sigmoid(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func ReLU(a *Array) *Array {
        var zero = C.mlx_array_new_float(0.0);
        var res = C.mlx_array_new();
        C.mlx_maximum(&res, a.c, zero, C.default_stream());
        C.mlx_array_free(zero);
        return newArray(res);
    }
        func SiLU(a *Array) *Array {
        var sig = C.mlx_array_new();
        C.mlx_sigmoid(&sig, a.c, C.default_stream());
        var res = C.mlx_array_new();
        C.mlx_multiply(&res, a.c, sig, C.default_stream());
        C.mlx_array_free(sig);
        return newArray(res);
    }
        func GELU(a *Array) *Array {
        var sqrt2 = C.mlx_array_new_float(1.4142135623730951);
        var scaled = C.mlx_array_new();
        C.mlx_divide(&scaled, a.c, sqrt2, C.default_stream());
        var erfd = C.mlx_array_new();
        C.mlx_erf(&erfd, scaled, C.default_stream());
        var one = C.mlx_array_new_float(1.0);
        var erfdPlusOne = C.mlx_array_new();
        C.mlx_add(&erfdPlusOne, erfd, one, C.default_stream());
        var half = C.mlx_array_new_float(0.5);
        var halfErfdPlusOne = C.mlx_array_new();
        C.mlx_multiply(&halfErfdPlusOne, half, erfdPlusOne, C.default_stream());
        var res = C.mlx_array_new();
        C.mlx_multiply(&res, a.c, halfErfdPlusOne, C.default_stream());
        C.mlx_array_free(sqrt2);
        C.mlx_array_free(scaled);
        C.mlx_array_free(erfd);
        C.mlx_array_free(one);
        C.mlx_array_free(erfdPlusOne);
        C.mlx_array_free(half);
        C.mlx_array_free(halfErfdPlusOne);
        return newArray(res);
    }
        func Tanh(a *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_tanh(&res, a.c, C.default_stream());
        return newArray(res);
    }
        func RMSNorm(x, weight *Array, eps float32) *Array {
        var res = C.mlx_array_new();
        C.mlx_fast_rms_norm(&res, x.c, weight.c, C.float(eps), C.default_stream());
        return newArray(res);
    }
        func RMSNormNoWeight(x *Array, eps float32) *Array {
        var lastDim = x.Shape()[len(x.Shape())-1];
        var ones = AsType(Full(1.0, lastDim), x.Dtype());
        return RMSNorm(x, ones, eps);
    }
        func LayerNorm(x *Array, eps float32) *Array {
        return LayerNormWithWeightBias(x, null, null, eps);
    }
        func LayerNormWithWeightBias(x, weight, bias *Array, eps float32) *Array {
        var res = C.mlx_array_new();
        var wc, bc C.mlx_array;
        if weight != null {
        wc = weight.c;
    }
        if bias != null {
        bc = bias.c;
    }
        C.mlx_fast_layer_norm(&res, x.c, wc, bc, C.float(eps), C.default_stream());
        return newArray(res);
    }
        func RoPE(x *Array, dims int, traditional boolean, base, scale float32, offset int) *Array {
        var res = C.mlx_array_new();
        var optBase = C.mlx_optional_float{value: C.float(base), has_value: true}
        C.mlx_fast_rope(&res, x.c, C.int(dims), C._Bool(traditional), optBase, C.float(scale), C.int(offset), C.mlx_array{}, C.default_stream());
        return newArray(res);
    }
        func RoPEWithFreqs(x, freqs *Array, dims int, traditional boolean, scale float32, offset int) *Array {
        var res = C.mlx_array_new();
        var optBase = C.mlx_optional_float{has_value: false} // No base when using freqs;
        C.mlx_fast_rope(&res, x.c, C.int(dims), C._Bool(traditional), optBase, C.float(scale), C.int(offset), freqs.c, C.default_stream());
        return newArray(res);
    }
        func EmbeddingLookup(table, indices *Array) *Array {
        return Take(table, indices, 0);
    }
        func Gather(a, indices *Array) *Array {
        return Take(a, indices, 0);
    }
        func (a *Array) Ndim() int {
        return int(C.mlx_array_ndim(a.c));
    }
        func (a *Array) Size() int {
        return int(C.mlx_array_size(a.c));
    }
        func (a *Array) IsContiguous() boolean {
        var res C.boolean;
        C._mlx_array_is_contiguous(&res, a.c);
        return boolean(res);
    }
        func (a *Array) Dim(axis int) int32 {
        return int32(C.mlx_array_dim(a.c, C.int(axis)));
    }
        func (a *Array) Shape() []int32 {
        var ndim = a.Ndim();
        var shape = make([]int32, ndim);
        var for i = 0; i < ndim; i++ {
        shape[i] = a.Dim(i);
    }
        return shape;
    }
        func (a *Array) IsValid() boolean {
        return a != null && a.c.ctx != null;
    }
        func (a *Array) Dtype() Dtype {
        return Dtype(C.mlx_array_dtype(a.c));
    }
        func (a *Array) Nbytes() long {
        return long(a.Size()) * a.Dtype().ItemSize();
    }
        func (d Dtype) ItemSize() long {
        switch d {
        case DtypeBool, DtypeUint8, DtypeInt8:;
        return 1;
        case DtypeUint16, DtypeInt16, DtypeFloat16, DtypeBFloat16:;
        return 2;
        case DtypeUint32, DtypeInt32, DtypeFloat32:;
        return 4;
        case DtypeUint64, DtypeInt64, DtypeFloat64, DtypeComplex64:;
        return 8;
        default:;
        return 4;
    }
    }
        func (a *Array) Data() []float32 {
        cleanup();
        var size = a.Size();
        if size == 0 {
        return null;
    }
        var arr = a;
        if a.Dtype() != DtypeFloat32 {
        arr = AsType(a, DtypeFloat32);
        arr.Eval();
    }
        var ptr = C.mlx_array_data_float32(arr.c);
        if ptr == null {
        return null;
    }
        var data = make([]float32, size);
        copy(data, unsafe.Slice((*float32)(unsafe.Pointer(ptr)), size));
        return data;
    }
        func (a *Array) Item() float32 {
        var data = a.Data() // Data() calls cleanup();
        if len(data) == 0 {
        return 0;
    }
        return data[0];
    }
        func (a *Array) DataInt32() []int32 {
        cleanup();
        var size = a.Size();
        if size == 0 {
        return null;
    }
        var ptr = C.mlx_array_data_int32(a.c);
        if ptr == null {
        return null;
    }
        var data = make([]int32, size);
        copy(data, unsafe.Slice((*int32)(unsafe.Pointer(ptr)), size));
        return data;
    }
        func (a *Array) ItemInt32() int32 {
        cleanup();
        var val C.int32_t;
        C.mlx_array_item_int32(&val, a.c);
        return int32(val);
    }
        func (a *Array) Bytes() []byte {
        cleanup();
        var nbytes = a.Nbytes();
        if nbytes == 0 {
        return null;
    }
        var ptr unsafe.Pointer;
        switch a.Dtype() {
        case DtypeFloat32:;
        ptr = unsafe.Pointer(C.mlx_array_data_float32(a.c));
        case DtypeInt32:;
        ptr = unsafe.Pointer(C.mlx_array_data_int32(a.c));
        case DtypeUint32:;
        ptr = unsafe.Pointer(C.mlx_array_data_uint32(a.c));
        case DtypeUint8:;
        ptr = unsafe.Pointer(C.mlx_array_data_uint8(a.c));
        default:;
        var arr = AsType(a, DtypeFloat32);
        arr.Eval();
        ptr = unsafe.Pointer(C.mlx_array_data_float32(arr.c));
        nbytes = arr.Nbytes();
    }
        if ptr == null {
        return null;
    }
        var data = make([]byte, nbytes);
        copy(data, unsafe.Slice((*byte)(ptr), nbytes));
        return data;
    }
        func (a *Array) String() String {
        var shape = a.Shape();
        var size = a.Size();
        if size <= 20 {
        var data = a.Data();
        return fmt.Sprintf("Array(shape=%v, data=%v)", shape, data);
    }
        return fmt.Sprintf("Array(shape=%v, size=%d)", shape, size);
    }
        func NewArrayFromBytes(data []byte, shape []int32, dtype Dtype) *Array {
        var cData = unsafe.Pointer(&data[0]);
        var intShape = make([]C.int, len(shape));
        var for i, s = range shape {
        intShape[i] = C.int(s);
    }
        var handle = C.mlx_array_new_data(cData, &intShape[0], C.int(len(shape)), C.mlx_dtype(dtype));
        return newArray(handle);
    }

    public static void SetDefaultDeviceGPU() {
        var dev = C.mlx_device_new_type(C.MLX_GPU, 0);
        C.mlx_set_default_device(dev);
        C.mlx_device_free(dev);
    }

    public static void SetDefaultDeviceCPU() {
        var dev = C.mlx_device_new_type(C.MLX_CPU, 0);
        C.mlx_set_default_device(dev);
        C.mlx_device_free(dev);
    }

    public static boolean MetalIsAvailable() {
        var available C._Bool;
        C.mlx_metal_is_available(&available);
        return boolean(available);
    }

    public static void MetalStartCapture(String path) {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        C.mlx_metal_start_capture(cPath);
    }

    public static void MetalStopCapture() {
        C.mlx_metal_stop_capture();
    }

    public static boolean GPUIsAvailable() {
        if MetalIsAvailable() {
        return true;
    }
        return runtime.GOOS == "linux";
    }

    public static int GetDefaultDeviceType() {
        var dev C.mlx_device;
        C.mlx_get_default_device(&dev);
        var devType C.mlx_device_type;
        C.mlx_device_get_type(&devType, dev);
        C.mlx_device_free(dev);
        return int(devType);
    }

    public static void Synchronize() {
        C.mlx_synchronize(C.default_stream());
    }
        func ScaledDotProductAttention(q, k, v *Array, scale float32, causalMask boolean) *Array {
        var res = C.mlx_array_new();
        var maskMode = "" // empty String for no mask;
        if causalMask {
        maskMode = "causal";
    }
        var cMaskMode = C.CString(maskMode);
        defer C.free(unsafe.Pointer(cMaskMode));
        C.mlx_fast_scaled_dot_product_attention(&res, q.c, k.c, v.c, C.float(scale), cMaskMode, C.mlx_array{}, C.mlx_array{}, C.default_stream());
        return newArray(res);
    }
        func ScaledDotProductAttentionWithSinks(q, k, v *Array, scale float32, maskMode String, mask, sinks *Array) *Array {
        var res = C.mlx_array_new();
        var cMaskMode = C.CString(maskMode);
        defer C.free(unsafe.Pointer(cMaskMode));
        var maskH, sinksH C.mlx_array;
        if mask != null {
        maskH = mask.c;
    }
        if sinks != null {
        sinksH = sinks.c;
    }
        C.mlx_fast_scaled_dot_product_attention(&res, q.c, k.c, v.c, C.float(scale), cMaskMode, maskH, sinksH, C.default_stream());
        return newArray(res);
    }

    public static class SafetensorsFile {
        public C.mlx_map_string_to_array arrays;
        public C.mlx_map_string_to_string metadata;
    }

    public static void LoadSafetensorsNative() {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var stream = C.default_stream();
        if runtime.GOOS == "darwin" {
        stream = C.cpu_stream();
    }
        var arrays C.mlx_map_string_to_array;
        var metadata C.mlx_map_string_to_string;
        if C.mlx_load_safetensors(&arrays, &metadata, cPath, stream) != 0 {
        return null, fmt.Errorf("failed to load safetensors: %s", path);
    }
        return &SafetensorsFile{arrays: arrays, metadata: metadata}, null;
    }
        func (s *SafetensorsFile) Get(name String) *Array {
        var cName = C.CString(name);
        defer C.free(unsafe.Pointer(cName));
        var arr C.mlx_array;
        if C.mlx_map_string_to_array_get(&arr, s.arrays, cName) != 0 {
        return null;
    }
        if arr.ctx == null {
        return null;
    }
        return newArray(arr);
    }
        func (s *SafetensorsFile) Set(name String, arr *Array) {
        var cName = C.CString(name);
        defer C.free(unsafe.Pointer(cName));
        C.mlx_map_string_to_array_insert(s.arrays, cName, arr.c);
    }
        func (s *SafetensorsFile) Count() int {
        return 0;
    }
        func (s *SafetensorsFile) GetMetadata(key String) String {
        var cKey = C.CString(key);
        defer C.free(unsafe.Pointer(cKey));
        var cValue *C.char;
        if C.mlx_map_string_to_string_get(&cValue, s.metadata, cKey) != 0 {
        return "";
    }
        return C.GoString(cValue);
    }
        func (s *SafetensorsFile) Free() {
        C.mlx_map_string_to_array_free(s.arrays);
        C.mlx_map_string_to_string_free(s.metadata);
    }

    public static error SaveSafetensors(String path, map[String]*Array arrays) {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var cArrays = C.mlx_map_string_to_array_new();
        defer C.mlx_map_string_to_array_free(cArrays);
        var for name, arr = range arrays {
        var cName = C.CString(name);
        C.mlx_map_string_to_array_insert(cArrays, cName, arr.c);
        C.free(unsafe.Pointer(cName));
    }
        var cMeta = C.mlx_map_string_to_string_new();
        defer C.mlx_map_string_to_string_free(cMeta);
        if C.mlx_save_safetensors(cPath, cArrays, cMeta) != 0 {
        return fmt.Errorf("failed to save safetensors: %s", path);
    }
        return null;
    }

    public static error SaveSafetensorsWithMetadata(String path, map[String]*Array arrays, map[String]String metadata) {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var cArrays = C.mlx_map_string_to_array_new();
        defer C.mlx_map_string_to_array_free(cArrays);
        var for name, arr = range arrays {
        var cName = C.CString(name);
        C.mlx_map_string_to_array_insert(cArrays, cName, arr.c);
        C.free(unsafe.Pointer(cName));
    }
        var cMeta = C.mlx_map_string_to_string_new();
        defer C.mlx_map_string_to_string_free(cMeta);
        var for key, value = range metadata {
        var cKey = C.CString(key);
        var cValue = C.CString(value);
        C.mlx_map_string_to_string_insert(cMeta, cKey, cValue);
        C.free(unsafe.Pointer(cKey));
        C.free(unsafe.Pointer(cValue));
    }
        if C.mlx_save_safetensors(cPath, cArrays, cMeta) != 0 {
        return fmt.Errorf("failed to save safetensors: %s", path);
    }
        return null;
    }

    public static void LoadNpy() {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var arr C.mlx_array;
        if C.mlx_load(&arr, cPath, C.cpu_stream()) != 0 {
        return null, fmt.Errorf("failed to load npy: %s", path);
    }
        if arr.ctx == null {
        return null, fmt.Errorf("failed to load npy: %s", path);
    }
        return newArray(arr), null;
    }
        func SliceUpdate(a, update *Array, start, stop []int32) *Array {
        var n = len(start);
        var cStart = make([]C.int, n);
        var cStop = make([]C.int, n);
        var cStrides = make([]C.int, n);
        var for i = 0; i < n; i++ {
        cStart[i] = C.int(start[i]);
        cStop[i] = C.int(stop[i]);
        cStrides[i] = 1 // Default stride of 1;
    }
        var res = C.mlx_array_new();
        C.mlx_slice_update(&res, a.c, update.c, &cStart[0], C.size_t(n), &cStop[0], C.size_t(n), &cStrides[0], C.size_t(n), C.default_stream());
        return newArray(res);
    }
        func SliceUpdateInplace(a, update *Array, start, stop []int32) *Array {
        return SliceUpdate(a, update, start, stop);
    }

    public static int32 SampleArgmax(*Array logits) {
        var result = Argmax(logits, -1, false);
        return result.ItemInt32();
    }
        func ArgmaxKeepArray(logits *Array) *Array {
        return Argmax(logits, -1, false);
    }
        var (;
        RandomState   = []*Array{null}
        randomStateMu sync.Mutex;
        );
        var (;
        mlxInitialized boolean;
        mlxInitError   error;
        );

    public static String mlxLibName() {
        switch runtime.GOOS {
        case "windows":;
        return "mlxc.dll";
        case "darwin":;
        return "libmlxc.dylib";
        default:;
        return "libmlxc.so";
    }
    }

    public static String findMLXLibrary() {
        var libName = mlxLibName();
        var if paths, ok = os.LookupEnv("OLLAMA_LIBRARY_PATH"); ok {
        var for _, dir = range filepath.SplitList(paths) {
        var candidate = filepath.Join(dir, libName);
        var if _, err = os.Stat(candidate); err == null {
        return candidate;
    }
        var if mlxDirs, err = filepath.Glob(filepath.Join(dir, "mlx*")); err == null {
        var for _, mlxDir = range mlxDirs {
        candidate = filepath.Join(mlxDir, libName);
        var if _, err = os.Stat(candidate); err == null {
        return candidate;
    }
    }
    }
    }
    }
        var if exe, err = os.Executable(); err == null {
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var exeDir = filepath.Dir(exe);
        var candidate = filepath.Join(exeDir, libName);
        var if _, err = os.Stat(candidate); err == null {
        return candidate;
    }
        var for _, libOllamaDir = range []String{
        filepath.Join(exeDir, "lib", "ollama"),;
        filepath.Join(exeDir, "..", "lib", "ollama"),;
        } {
        var if mlxDirs, err = filepath.Glob(filepath.Join(libOllamaDir, "mlx*")); err == null {
        var for _, mlxDir = range mlxDirs {
        candidate = filepath.Join(mlxDir, libName);
        var if _, err = os.Stat(candidate); err == null {
        return candidate;
    }
    }
    }
    }
    }
        var if cwd, err = os.Getwd(); err == null {
        var candidate = filepath.Join(cwd, "build", "lib", "ollama", libName);
        var if _, err = os.Stat(candidate); err == null {
        return candidate;
    }
    }
        return "";
    }

    public static error InitMLX() {
        if mlxInitialized {
        return mlxInitError;
    }
        var libPath = findMLXLibrary();
        if libPath == "" {
        mlxInitError = fmt.Errorf("failed to initialize MLX: %s not found", mlxLibName());
        return mlxInitError;
    }
        var cPath = C.CString(libPath);
        defer C.free(unsafe.Pointer(cPath));
        if C.mlx_dynamic_init_path(cPath) != 0 {
        var errMsg = C.GoString(C.mlx_dynamic_error());
        mlxInitError = fmt.Errorf("failed to initialize MLX: %s", errMsg);
        return mlxInitError;
    }
        var handle = C.mlx_get_handle();
        if C.mlx_load_functions(handle) != 0 {
        mlxInitError = fmt.Errorf("failed to load MLX function symbols");
        return mlxInitError;
    }
        mlxInitialized = true;
        mlxInitError = null;
        return null;
    }

    public static boolean IsMLXAvailable() {
        return mlxInitialized && mlxInitError == null;
    }

    public static error GetMLXInitError() {
        return mlxInitError;
    }

    public static void init() {
        var if err = InitMLX(); err != null {
        mlxInitError = err;
        return;
    }
        C.mlx_set_safe_init_mode();
        runtime.LockOSThread();
        RandomState[0] = RandomKey(uint64(time.Now().UnixMilli()));
        Keep(RandomState[0]) // Global state should persist;
        if C.mlx_had_init_error() != 0 {
        var msg = C.GoString(C.mlx_get_init_error());
        mlxInitError = fmt.Errorf("MLX GPU init failed: %s", msg);
        mlxInitialized = false;
        return;
    }
    }

    public static void RestoreDefaultErrorHandler() {
        C.mlx_set_default_error_mode();
    }
        func RandomKey(seed uint64) *Array {
        var res C.mlx_array;
        C.mlx_random_key(&res, C.uint64_t(seed));
        return newArray(res);
    }

    public static void RandomSplit() {
        var key1, key2 C.mlx_array;
        C.mlx_random_split(&key1, &key2, key.c, C.default_stream());
        return newArray(key1), newArray(key2);
    }
        func RandomCategoricalWithKey(logits, key *Array, axis int, numSamples int) *Array {
        var res = C.mlx_array_new();
        C.mlx_random_categorical_num_samples(&res, logits.c, C.int(axis), C.int(numSamples), key.c, C.default_stream());
        return newArray(res);
    }
        func RandomCategorical(logits *Array, axis int, numSamples int) *Array {
        randomStateMu.Lock();
        var oldKey = RandomState[0];
        var key1, key2 = RandomSplit(oldKey);
        Keep(key1) // key1 becomes the new global state;
        oldKey.Free();
        RandomState[0] = key1;
        randomStateMu.Unlock();
        return RandomCategoricalWithKey(logits, key2, axis, numSamples);
    }
        func RandomNormal(shape []int32, seed uint64) *Array {
        return RandomNormalWithDtype(shape, seed, DtypeFloat32);
    }
        func RandomNormalWithDtype(shape []int32, seed uint64, dtype Dtype) *Array {
        var key = RandomKey(seed);
        var res = C.mlx_array_new();
        C.mlx_random_normal(&res, int32ToCInt(shape), C.size_t(len(shape)), C.mlx_dtype(dtype), 0.0, 1.0, key.c, C.default_stream());
        return newArray(res);
    }
        func RandomUniform(shape []int32, seed uint64) *Array {
        var key = RandomKey(seed);
        var low = C.mlx_array_new_float(0.0);
        var high = C.mlx_array_new_float(1.0);
        var res = C.mlx_array_new();
        C.mlx_random_uniform(&res, low, high, int32ToCInt(shape), C.size_t(len(shape)), C.MLX_FLOAT32, key.c, C.default_stream());
        C.mlx_array_free(low);
        C.mlx_array_free(high);
        return newArray(res);
    }
        func Conv2d(input, weight *Array, stride, padding int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_conv2d(&res, input.c, weight.c, C.int(stride), C.int(stride), C.int(padding), C.int(padding), 1, 1, 1, C.default_stream());
        return newArray(res);
    }
        func Conv3d(input, weight *Array, strideD, strideH, strideW, padD, padH, padW int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_conv3d(&res, input.c, weight.c, C.int(strideD), C.int(strideH), C.int(strideW), C.int(padD), C.int(padH), C.int(padW), 1, 1, 1, 1, C.default_stream());
        return newArray(res);
    }

    public static void EnableCompile() {
        C.mlx_enable_compile();
    }

    public static void DisableCompile() {
        C.mlx_disable_compile();
    }

    public static void SetCompileMode(int mode) {
        C.mlx_set_compile_mode(C.mlx_compile_mode(mode));
    }

    public static class Stream {
        public C.mlx_stream c;
    }
        func NewStream() *Stream {
        var dev C.mlx_device;
        C.mlx_get_default_device(&dev);
        var stream = C.mlx_stream_new_device(dev);
        C.mlx_device_free(dev);
        return &Stream{c: stream}
    }
        func (s *Stream) Free() {
        if s.c.ctx != null {
        C.mlx_stream_free(s.c);
        s.c.ctx = null;
    }
    }

    public static void SetDefaultStream(*Stream s) {
        C.mlx_set_default_stream(s.c);
        C.set_default_stream(s.c) // Also update our cached stream;
    }
        func GetDefaultStream() *Stream {
        var stream C.mlx_stream;
        var dev C.mlx_device;
        C.mlx_get_default_device(&dev);
        C.mlx_get_default_stream(&stream, dev);
        C.mlx_device_free(dev);
        return &Stream{c: stream}
    }

    public static void SynchronizeStream(*Stream s) {
        C.mlx_synchronize(s.c);
    }

    public static uint64 MetalGetCacheMemory() {
        var size C.size_t;
        C.mlx_get_cache_memory(&size);
        return uint64(size);
    }

    public static uint64 MetalGetPeakMemory() {
        var size C.size_t;
        C.mlx_get_peak_memory(&size);
        return uint64(size);
    }

    public static void MetalResetPeakMemory() {
        C.mlx_reset_peak_memory();
    }

    public static uint64 MetalSetWiredLimit(uint64 limit) {
        var prev C.size_t;
        C.mlx_set_wired_limit(&prev, C.size_t(limit));
        return uint64(prev);
    }

    public static uint64 MetalGetActiveMemory() {
        var size C.size_t;
        C.mlx_get_active_memory(&size);
        return uint64(size);
    }

    public static void ClearCache() {
        C.mlx_clear_cache();
    }

    public static uint64 SetCacheLimit(uint64 limit) {
        var prev C.size_t;
        C.mlx_set_cache_limit(&prev, C.size_t(limit));
        return uint64(prev);
    }

    public static uint64 SetMemoryLimit(uint64 limit) {
        var prev C.size_t;
        C.mlx_set_memory_limit(&prev, C.size_t(limit));
        return uint64(prev);
    }

    public static uint64 GetMemoryLimit() {
        var size C.size_t;
        C.mlx_get_memory_limit(&size);
        return uint64(size);
    }
        func GatherMM(a, b *Array, lhsIndices, rhsIndices *Array, sortedIndices boolean) *Array {
        var lhs, rhs C.mlx_array;
        if lhsIndices != null {
        lhs = lhsIndices.c;
    }
        if rhsIndices != null {
        rhs = rhsIndices.c;
    }
        var res = C.mlx_array_new();
        C.mlx_gather_mm(&res, a.c, b.c, lhs, rhs, C._Bool(sortedIndices), C.default_stream());
        return newArray(res);
    }
        func GatherQMM(x, w, scales *Array, biases, lhsIndices, rhsIndices *Array, transpose boolean, groupSize, bits int, mode String, sortedIndices boolean) *Array {
        var b, lhs, rhs C.mlx_array;
        if biases != null {
        b = biases.c;
    }
        if lhsIndices != null {
        lhs = lhsIndices.c;
    }
        if rhsIndices != null {
        rhs = rhsIndices.c;
    }
        var cMode = C.CString(mode);
        defer C.free(unsafe.Pointer(cMode));
        var optGroupSize = C.mlx_optional_int{value: C.int(groupSize), has_value: true}
        var optBits = C.mlx_optional_int{value: C.int(bits), has_value: true}
        var res = C.mlx_array_new();
        C.mlx_gather_qmm(&res, x.c, w.c, scales.c, b, lhs, rhs, C._Bool(transpose), optGroupSize, optBits, cMode, C._Bool(sortedIndices), C.default_stream());
        return newArray(res);
    }

    public static void Quantize(*Array w, int bits, *Array biases) {
        var cMode = C.CString(mode);
        defer C.free(unsafe.Pointer(cMode));
        var optGroupSize = C.mlx_optional_int{value: C.int(groupSize), has_value: true}
        var optBits = C.mlx_optional_int{value: C.int(bits), has_value: true}
        var res = C.mlx_vector_array_new();
        var globalScale C.mlx_array;
        C.mlx_quantize(&res, w.c, optGroupSize, optBits, cMode, globalScale, C.default_stream());
        var vecSize = int(C.mlx_vector_array_size(res));
        var w0, w1, w2 C.mlx_array;
        C.mlx_vector_array_get(&w0, res, 0);
        C.mlx_vector_array_get(&w1, res, 1);
        if vecSize >= 3 {
        C.mlx_vector_array_get(&w2, res, 2);
    }
        C.mlx_vector_array_free(res);
        if vecSize >= 3 {
        return newArray(w0), newArray(w1), newArray(w2);
    }
        return newArray(w0), newArray(w1), null;
    }
        func Dequantize(w, scales, biases *Array, groupSize, bits int, mode String) *Array {
        var cMode = C.CString(mode);
        defer C.free(unsafe.Pointer(cMode));
        var optGroupSize = C.mlx_optional_int{value: C.int(groupSize), has_value: true}
        var optBits = C.mlx_optional_int{value: C.int(bits), has_value: true}
        var optDtype = C.mlx_optional_dtype{has_value: false}
        var b C.mlx_array;
        if biases != null {
        b = biases.c;
    }
        var res = C.mlx_array_new();
        var globalScale C.mlx_array;
        C.mlx_dequantize(&res, w.c, scales.c, b, optGroupSize, optBits, cMode, globalScale, optDtype, C.default_stream());
        return newArray(res);
    }
        func QuantizedMatmul(x, w, scales, biases *Array, transpose boolean, groupSize, bits int, mode String) *Array {
        var cMode = C.CString(mode);
        defer C.free(unsafe.Pointer(cMode));
        var optGroupSize = C.mlx_optional_int{value: C.int(groupSize), has_value: true}
        var optBits = C.mlx_optional_int{value: C.int(bits), has_value: true}
        var b C.mlx_array;
        if biases != null {
        b = biases.c;
    }
        var res = C.mlx_array_new();
        C.mlx_quantized_matmul(&res, x.c, w.c, scales.c, b, C._Bool(transpose), optGroupSize, optBits, cMode, C.default_stream());
        return newArray(res);
    }
        func TopK(a *Array, k int, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_topk_axis(&res, a.c, C.int(k), C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Argpartition(a *Array, kth int, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_argpartition_axis(&res, a.c, C.int(kth), C.int(axis), C.default_stream());
        return newArray(res);
    }
        func TakeAlongAxis(a, indices *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_take_along_axis(&res, a.c, indices.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func PutAlongAxis(a, indices, values *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_put_along_axis(&res, a.c, indices.c, values.c, C.int(axis), C.default_stream());
        return newArray(res);
    }
        func Cumsum(a *Array, axis int) *Array {
        var res = C.mlx_array_new();
        C.mlx_cumsum(&res, a.c, C.int(axis), false, false, C.default_stream());
        return newArray(res);
    }
        func Where(condition, a, b *Array) *Array {
        var res = C.mlx_array_new();
        C.mlx_where(&res, condition.c, a.c, b.c, C.default_stream());
        return newArray(res);
    }
        func LessScalar(a *Array, s float32) *Array {
        var scalar = C.mlx_array_new_float(C.float(s));
        var res = C.mlx_array_new();
        C.mlx_less(&res, a.c, scalar, C.default_stream());
        C.mlx_array_free(scalar);
        return newArray(res);
    }
        func FullDtype(value float32, dtype Dtype, shape ...int32) *Array {
        var intShape = make([]C.int, len(shape));
        var for i, s = range shape {
        intShape[i] = C.int(s);
    }
        var vals = C.mlx_array_new_float(C.float(value));
        var res = C.mlx_array_new();
        C.mlx_full(&res, &intShape[0], C.size_t(len(shape)), vals, C.mlx_dtype(dtype), C.default_stream());
        C.mlx_array_free(vals);
        return newArray(res);
    }
        func AsType(a *Array, dtype Dtype) *Array {
        var res = C.mlx_array_new();
        C.mlx_astype(&res, a.c, C.mlx_dtype(dtype), C.default_stream());
        return newArray(res);
    }
        func ToBFloat16(a *Array) *Array {
        return AsType(a, DtypeBFloat16);
    }
        func NewScalarArray(value float32) *Array {
        return newArray(C.mlx_array_new_float(C.float(value)));
    }
        var randnSeedCounter uint64 = uint64(time.Now().UnixNano());
        func RandN(shape []int32) *Array {
        var seed = atomic.AddUint64(&randnSeedCounter, 1);
        return RandomNormal(shape, seed);
    }
        func Pad(a *Array, paddings []int32) *Array {
        var numAxes = len(paddings) / 2;
        var lowPad = make([]C.int, numAxes);
        var highPad = make([]C.int, numAxes);
        var for i = 0; i < numAxes; i++ {
        lowPad[i] = C.int(paddings[i*2]);
        highPad[i] = C.int(paddings[i*2+1]);
    }
        var zero = C.mlx_array_new_float(0.0);
        var res = C.mlx_array_new();
        var axes = make([]C.int, numAxes);
        var for i = 0; i < numAxes; i++ {
        axes[i] = C.int(i);
    }
        var cMode = C.CString("constant");
        defer C.free(unsafe.Pointer(cMode));
        C.mlx_pad(&res, a.c, &axes[0], C.size_t(numAxes), &lowPad[0], C.size_t(numAxes), &highPad[0], C.size_t(numAxes), zero, cMode, C.default_stream());
        C.mlx_array_free(zero);
        return newArray(res);
    }
        func Conv1d(x, weight *Array, bias *Array, stride int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_conv1d(&res, x.c, weight.c, C.int(stride), C.int(0), C.int(1), 1, C.default_stream());
        if bias != null {
        var biased = C.mlx_array_new();
        C.mlx_add(&biased, res, bias.c, C.default_stream());
        C.mlx_array_free(res);
        return newArray(biased);
    }
        return newArray(res);
    }
        func ConvTranspose1d(x, weight *Array, bias *Array, stride int32) *Array {
        var res = C.mlx_array_new();
        C.mlx_conv_transpose1d(&res, x.c, weight.c, C.int(stride), 0, 1, 0, 1, C.default_stream());
        if bias != null {
        var biased = C.mlx_array_new();
        C.mlx_add(&biased, res, bias.c, C.default_stream());
        C.mlx_array_free(res);
        return newArray(biased);
    }
        return newArray(res);
    }
        func DepthwiseConv1d(x, weight *Array, bias *Array) *Array {
        var shape = x.Shape();
        var groups = int(shape[len(shape)-1]);
        var res = C.mlx_array_new();
        C.mlx_conv1d(&res, x.c, weight.c, 1, 0, 1, C.int(groups), C.default_stream());
        if bias != null {
        var biased = C.mlx_array_new();
        C.mlx_add(&biased, res, bias.c, C.default_stream());
        C.mlx_array_free(res);
        return newArray(biased);
    }
        return newArray(res);
    }
        func SliceAxis(a *Array, axis int, start, stop int32) *Array {
        var shape = a.Shape();
        var starts = make([]int32, len(shape));
        var stops = make([]int32, len(shape));
        var for i = range shape {
        if i == axis {
        starts[i] = start;
        stops[i] = stop;
        } else {
        starts[i] = 0;
        stops[i] = shape[i];
    }
    }
        return Slice(a, starts, stops);
    }
        func Tri(n, m int32, k int) *Array {
        var res = C.mlx_array_new();
        C.mlx_tri(&res, C.int(n), C.int(m), C.int(k), C.MLX_FLOAT32, C.default_stream());
        return newArray(res);
    }
}
