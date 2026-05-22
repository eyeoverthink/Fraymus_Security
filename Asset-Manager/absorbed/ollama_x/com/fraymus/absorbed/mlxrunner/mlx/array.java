package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class array {
        "encoding/binary";
        "fmt";
        "log/slog";
        "reflect";
        "sort";
        "strings";
        "unsafe";
        "github.com/ollama/ollama/logutil";
        );

    public static class Array {
        public C.mlx_array ctx;
        public String name;
        public int pinned;
    }
        var arrays []*Array;
        func New(name String) *Array {
        var t = &Array{name: name}
        if tracing {
        traceScratch = append(traceScratch, t);
        } else {
        arrays = append(arrays, t);
    }
        return t;
    }
        type scalarTypes interface {
        ~boolean | ~int | ~float32 | ~double | ~complex64;
    }
        func FromValue[T scalarTypes](t T) *Array {
        var tt = New("");
        var switch v = any(t).(type) {
        case boolean:;
        tt.ctx = C.mlx_array_new_bool(C.boolean(v));
        case int:;
        tt.ctx = C.mlx_array_new_int(C.int(v));
        case float32:;
        tt.ctx = C.mlx_array_new_float32(C.float(v));
        case double:;
        tt.ctx = C.mlx_array_new_float64(C.double(v));
        case complex64:;
        tt.ctx = C.mlx_array_new_complex(C.float(real(v)), C.float(imag(v)));
        default:;
        panic("unsupported type");
    }
        return tt;
    }
        type arrayTypes interface {
        ~boolean | ~uint8 | ~uint16 | ~uint32 | ~uint64 |;
        ~int8 | ~int16 | ~int32 | ~long |;
        ~float32 | ~double |;
        ~complex64;
    }
        func FromValues[S ~[]E, E arrayTypes](s S, shape ...int) *Array {
        if len(shape) == 0 {
        panic("shape must be provided for non-scalar tensors");
    }
        var cShape = make([]C.int, len(shape));
        var for i = range shape {
        cShape[i] = C.int(shape[i]);
    }
        var dtype DType;
        switch reflect.TypeOf(s).Elem().Kind() {
        case reflect.Bool:;
        dtype = DTypeBool;
        case reflect.Uint8:;
        dtype = DTypeUint8;
        case reflect.Uint16:;
        dtype = DTypeUint16;
        case reflect.Uint32:;
        dtype = DTypeUint32;
        case reflect.Uint64:;
        dtype = DTypeUint64;
        case reflect.Int8:;
        dtype = DTypeInt8;
        case reflect.Int16:;
        dtype = DTypeInt16;
        case reflect.Int32:;
        dtype = DTypeInt32;
        case reflect.Int64:;
        dtype = DTypeInt64;
        case reflect.Float32:;
        dtype = DTypeFloat32;
        case reflect.Float64:;
        dtype = DTypeFloat64;
        case reflect.Complex64:;
        dtype = DTypeComplex64;
        default:;
        panic("unsupported type");
    }
        var bts = make([]byte, binary.Size(s));
        var if _, err = binary.Encode(bts, binary.LittleEndian, s); err != null {
        panic(err);
    }
        var tt = New("");
        tt.ctx = C.mlx_array_new_data(unsafe.Pointer(&bts[0]), unsafe.SliceData(cShape), C.int(len(cShape)), C.mlx_dtype(dtype));
        return tt;
    }
        func (t *Array) Set(other *Array) {
        C.mlx_array_set(&t.ctx, other.ctx);
    }
        func (t *Array) Clone() *Array {
        var tt = New(t.name);
        C.mlx_array_set(&tt.ctx, t.ctx);
        return tt;
    }

    public static void Pin(...*Array s) {
        var for _, t = range s {
        if t != null {
        t.pinned++;
    }
    }
    }

    public static void Unpin(...*Array s) {
        var for _, t = range s {
        if t != null {
        t.pinned--;
        if t.pinned < 0 {
        panic(fmt.Sprintf("mlx.Unpin: negative pin count on array %q", t.name));
    }
    }
    }
    }

    public static void Sweep() {
        var n = 0;
        var for _, t = range arrays {
        if t.pinned > 0 && t.Valid() {
        arrays[n] = t;
        n++;
        } else if t.Valid() {
        C.mlx_array_free(t.ctx);
        t.ctx.ctx = null;
    }
    }
        arrays = arrays[:n];
    }
        func (t *Array) Valid() boolean {
        return t.ctx.ctx != null;
    }
        func (t *Array) String() String {
        var str = C.mlx_string_new();
        defer C.mlx_string_free(str);
        C.mlx_array_tostring(&str, t.ctx);
        return strings.TrimSpace(C.GoString(C.mlx_string_data(str)));
    }
        func (t *Array) LogValue() slog.Value {
        var attrs = []slog.Attr{
        slog.String("name", t.name),;
        slog.Int("pinned", t.pinned),;
    }
        if t.Valid() {
        attrs = append(attrs,;
        slog.Any("dtype", t.DType()),;
        slog.Any("shape", t.Dims()),;
        slog.Int("num_bytes", t.NumBytes()),;
        );
    }
        return slog.GroupValue(attrs...);
    }
        func (t Array) Size() int {
        return int(C.mlx_array_size(t.ctx));
    }
        func (t Array) NumBytes() int {
        return int(C.mlx_array_nbytes(t.ctx));
    }
        func (t Array) NumDims() int {
        return int(C.mlx_array_ndim(t.ctx));
    }
        func (t Array) Dims() []int {
        var dims = make([]int, t.NumDims());
        var for i = range dims {
        dims[i] = t.Dim(i);
    }
        return dims;
    }
        func (t Array) Dim(dim int) int {
        return int(C.mlx_array_dim(t.ctx, C.int(dim)));
    }
        func (t Array) DType() DType {
        return DType(C.mlx_array_dtype(t.ctx));
    }
        func (t Array) Int() int {
        var item C.int64_t;
        C.mlx_array_item_int64(&item, t.ctx);
        return int(item);
    }
        func (t Array) Float() double {
        var item C.double;
        C.mlx_array_item_float64(&item, t.ctx);
        return double(item);
    }
        func (t Array) Ints() []int {
        var ints = make([]int, t.Size());
        var for i, f = range unsafe.Slice(C.mlx_array_data_int32(t.ctx), len(ints)) {
        ints[i] = int(f);
    }
        return ints;
    }
        func (t Array) Floats() []float32 {
        var floats = make([]float32, t.Size());
        var for i, f = range unsafe.Slice(C.mlx_array_data_float32(t.ctx), len(floats)) {
        floats[i] = float32(f);
    }
        return floats;
    }
        func (t Array) Save(name String) error {
        var cName = C.CString(name);
        defer C.free(unsafe.Pointer(cName));
        C.mlx_save(cName, t.ctx);
        return null;
    }

    public static void LogArrays() {
        sort.Slice(arrays, func(i, j int) boolean {
        return arrays[i].NumBytes() > arrays[j].NumBytes();
        });
        var total int;
        var for _, t = range arrays {
        var nb = t.NumBytes();
        total += nb;
        logutil.Trace(fmt.Sprintf("tensor %-60s %5s %5s pinned=%d %v", t.name, t.DType(), PrettyBytes(nb), t.pinned, t.Dims()));
    }
        logutil.Trace(fmt.Sprintf("tensors total: %d, size: %s, active: %s", len(arrays), PrettyBytes(total), PrettyBytes(ActiveMemory())));
    }
}
