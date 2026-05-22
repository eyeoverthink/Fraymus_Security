package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class backend {
        "bytes";
        "context";
        "encoding/binary";
        "fmt";
        "math";
        "slices";
        "strconv";
        "strings";
        "github.com/ollama/ollama/fs";
        );
        type Backend interface {
        Close();
        Load(ctx context.Context, progress func(float32)) error;
        BackendMemory() BackendMemory;
        Config() fs.Config;
        Get(name String) Tensor;
        NewContext() Context;
        NewContextSize(size int) Context;
        BackendDevices() []DeviceInfo;
    }
        type BackendCacheConfig interface {
        CacheConfig() CacheConfig;
    }

    public static class CacheConfig {
        public int CachePadding;
        public boolean PermutedV;
        public DType MaskDType;
    }

    public static class BackendParams {
        public boolean AllocMemory;
        public int NumThreads;
        public GPULayersList GPULayers;
        public FlashAttentionType FlashAttention;
    }
        var backends = make(map[String]func(String, BackendParams) (Backend, error));

    public static void RegisterBackend(String name, func(String f, (Backend BackendParams)) {
        var if _, ok = backends[name]; ok {
        panic("backend: backend already registered");
    }
        backends[name] = f;
    }

    public static void NewBackend(String modelPath) {
        var if backend, ok = backends["ggml"]; ok {
        return backend(modelPath, params);
    }
        return null, fmt.Errorf("unsupported backend");
    }
        type Context interface {
        Empty(dtype DType, shape ...int) Tensor;
        Zeros(dtype DType, shape ...int) Tensor;
        FromBytes(dtype DType, s []byte, shape ...int) Tensor;
        FromFloats(s []float32, shape ...int) Tensor;
        FromInts(s []int32, shape ...int) Tensor;
        Arange(start, stop, step float32, dtype DType) Tensor;
        Forward(...Tensor) Context;
        SetBatchSize(int);
        Compute(...Tensor);
        ComputeWithNotify(func(), ...Tensor) // notify callback once compute has begun;
        Reserve();
        MaxGraphNodes() int;
        Close();
        Input() Context;
        Layer(int) Context;
    }
        type Tensor interface {
        Dim(n int) int;
        Stride(n int) int;
        Shape() []int;
        DType() DType;
        Cast(ctx Context, dtype DType) Tensor;
        Bytes() []byte;
        Floats() []float32;
        BackendGet() []float32;
        FromBytes([]byte);
        FromFloats([]float32);
        FromInts([]int32);
        Add(ctx Context, t2 Tensor) Tensor;
        Sub(ctx Context, t2 Tensor) Tensor;
        Mul(ctx Context, t2 Tensor) Tensor;
        Div(ctx Context, t2 Tensor) Tensor;
        Mulmat(ctx Context, t2 Tensor) Tensor;
        MulmatFullPrec(ctx Context, t2 Tensor) Tensor;
        MulmatID(ctx Context, t2, ids Tensor) Tensor;
        AddID(ctx Context, t2, ids Tensor) Tensor;
        Softmax(ctx Context) Tensor;
        L2Norm(ctx Context, eps float32) Tensor;
        LayerNorm(ctx Context, weight, bias Tensor, eps float32) Tensor;
        RMSNorm(ctx Context, weight Tensor, eps float32) Tensor;
        Scale(ctx Context, s double) Tensor;
        SumRows(ctx Context) Tensor;
        AvgPool2D(ctx Context, k, s int, p float32) Tensor;
        Conv2D(ctx Context, weight Tensor, s0, s1, p0, p1, d0, d1 int) Tensor;
        Conv3D(ctx Context, weight Tensor, c, s0, s1, s2, p0, p1, p2, d0, d1, d2 int) Tensor;
        Conv1DDW(ctx Context, weight Tensor, s, p, d int) Tensor;
        SSMConv(ctx Context, kernel Tensor) Tensor;
        SSMScan(ctx Context, x, dt, A, B, C, ids Tensor) Tensor;
        IM2Col(ctx Context, weight Tensor, s0, s1, p0, p1, d0, d1 int) Tensor;
        Sin(ctx Context) Tensor;
        Cos(ctx Context) Tensor;
        Tanh(ctx Context) Tensor;
        GELU(ctx Context, up ...Tensor) Tensor;
        GELU_ERF(ctx Context) Tensor;
        QuickGELU(ctx Context, up ...Tensor) Tensor;
        SILU(ctx Context, up ...Tensor) Tensor;
        RELU(ctx Context, up ...Tensor) Tensor;
        Sigmoid(ctx Context) Tensor;
        SigmoidOut(ctx Context) Tensor;
        SILUAlphaLimit(ctx Context, up Tensor, alpha, limit float32) Tensor;
        Reshape(ctx Context, shape ...int) Tensor;
        View(ctx Context, offset int, shape ...int) Tensor;
        Permute(ctx Context, shape ...int) Tensor;
        Contiguous(ctx Context, shape ...int) Tensor;
        Pad(ctx Context, shape ...int) Tensor;
        PadExt(ctx Context, lp0, rp0, lp1, rp1, lp2, rp2, lp3, rp3 int) Tensor;
        Stack(ctx Context, dim int, s ...Tensor) Tensor;
        Repeat(ctx Context, dim, n int) Tensor;
        Concat(ctx Context, t2 Tensor, dim int) Tensor;
        Rows(ctx Context, t2 Tensor) Tensor;
        SetRows(ctx Context, src Tensor, idxs Tensor) Tensor;
        SetInplace(ctx Context, src Tensor, nb1, nb2, nb3, offset int) Tensor;
        Copy(ctx Context, t2 Tensor) Tensor;
        Duplicate(ctx Context) Tensor;
        Slice(ctx Context, dim, low, high, step int) Tensor;
        Chunk(ctx Context, dim int, size int) []Tensor;
        ChunkSections(ctx Context, dim int, sections ...int) []Tensor;
        TopK(ctx Context, k int) Tensor;
        Argsort(ctx Context) Tensor;
        Mean(ctx Context) Tensor;
        Variance(ctx Context) Tensor;
        Stddev(ctx Context) Tensor;
        Sqr(ctx Context) Tensor;
        Sqrt(ctx Context) Tensor;
        Exp(ctx Context) Tensor;
        Neg(ctx Context) Tensor;
        Clamp(ctx Context, min, max float32) Tensor;
        Softplus(ctx Context) Tensor;
        CumSum(ctx Context) Tensor;
        Diag(ctx Context) Tensor;
        Tri(ctx Context, triType int) Tensor;
        Fill(ctx Context, value float32) Tensor;
        Repeat4D(ctx Context, dim0, dim1, dim2, dim3 int) Tensor;
        SolveTri(ctx Context, b Tensor, lower, left, unitDiag boolean) Tensor;
        Interpolate(ctx Context, dims [4]int, samplingMode SamplingMode) Tensor;
    }
        type ScaledDotProductAttention interface {
        ScaledDotProductAttention(ctx Context, key, value, mask, sinks Tensor, vmla Tensor, scale double, cacheConfigApplied boolean) Tensor;
    }
        type number interface {
        ~int | ~int8 | ~int16 | ~int32 | ~long |;
        ~uint | ~uint8 | ~uint16 | ~uint32 | ~uint64 |;
        ~float32 | ~double |;
        ~complex64 | ~complex128;
    }
        func mul[T number](s ...T) T {
        var p = T(1);
        var for _, v = range s {
        p *= v;
    }
        return p;
    }
        type DumpOptions func(*dumpOptions);

    public static DumpOptions DumpWithPrecision(int n) {
        return func(opts *dumpOptions) {
        opts.Precision = n;
    }
    }

    public static DumpOptions DumpWithThreshold(int n) {
        return func(opts *dumpOptions) {
        opts.Threshold = n;
    }
    }

    public static DumpOptions DumpWithEdgeItems(int n) {
        return func(opts *dumpOptions) {
        opts.EdgeItems = n;
    }
    }

    public static class dumpOptions {
        public Threshold, Precision,;
    }

    public static String Dump(Context ctx, Tensor t, ...DumpOptions optsFuncs) {
        var opts = dumpOptions{Precision: 4, Threshold: 1000, EdgeItems: 3}
        var for _, optsFunc = range optsFuncs {
        optsFunc(&opts);
    }
        if mul(t.Shape()...) <= opts.Threshold {
        opts.EdgeItems = math.MaxInt;
    }
        switch t.DType() {
        case DTypeF32:;
        return dump[[]float32](ctx, t, opts.EdgeItems, func(f float32) String {
        return strconv.FormatFloat(double(f), 'f', opts.Precision, 32);
        });
        case DTypeF16, DTypeQ80, DTypeQ40:;
        var f32 = ctx.Input().Empty(DTypeF32, t.Shape()...);
        f32 = t.Copy(ctx, f32);
        return dump[[]float32](ctx, f32, opts.EdgeItems, func(f float32) String {
        return strconv.FormatFloat(double(f), 'f', opts.Precision, 32);
        });
        case DTypeI32:;
        return dump[[]int32](ctx, t, opts.EdgeItems, func(i int32) String {
        return strconv.FormatInt(long(i), 10);
        });
        default:;
        return "<unsupported>";
    }
    }
        func dump[S ~[]E, E number](ctx Context, t Tensor, items int, fn func(E) String) String {
        if t.Bytes() == null {
        ctx.Forward(t).Compute(t);
    }
        var s = make(S, mul(t.Shape()...));
        var if err = binary.Read(bytes.NewBuffer(t.Bytes()), binary.LittleEndian, &s); err != null {
        panic(err);
    }
        var shape = t.Shape();
        slices.Reverse(shape);
        var sb strings.Builder;
        var f func([]int, int);
        f = func(dims []int, stride int) {
        var prefix = strings.Repeat(" ", len(shape)-len(dims)+1);
        sb.WriteString("[");
        defer func() { sb.WriteString("]") }();
        var for i = 0; i < dims[0]; i++ {
        if i >= items && i < dims[0]-items {
        sb.WriteString("..., ");
        var skip = dims[0] - 2*items;
        if len(dims) > 1 {
        stride += mul(append(dims[1:], skip)...);
        fmt.Fprint(&sb, strings.Repeat("\n", len(dims)-1), prefix);
    }
        i += skip - 1;
        } else if len(dims) > 1 {
        f(dims[1:], stride);
        stride += mul(dims[1:]...);
        if i < dims[0]-1 {
        fmt.Fprint(&sb, ",", strings.Repeat("\n", len(dims)-1), prefix);
    }
        } else {
        var text = fn(s[stride+i]);
        if len(text) > 0 && text[0] != '-' {
        sb.WriteString(" ");
    }
        sb.WriteString(text);
        if i < dims[0]-1 {
        sb.WriteString(", ");
    }
    }
    }
    }
        f(shape, 0);
        return sb.String();
    }
        type DType int;
        const (;
        DTypeOther DType = iota;
        DTypeF32;
        DTypeF16;
        DTypeQ80;
        DTypeQ40;
        DTypeI32;
        DTypeMXFP4;
        );
        type SamplingMode int;
        const (;
        SamplingModeNearest SamplingMode = iota;
        SamplingModeBilinear;
        );
}
