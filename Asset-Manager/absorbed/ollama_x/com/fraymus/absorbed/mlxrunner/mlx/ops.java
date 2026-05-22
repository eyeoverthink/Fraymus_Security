package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class ops {
        "unsafe";
        );
        func (t *Array) Abs() *Array {
        var out = New("ABS");
        C.mlx_abs(&out.ctx, t.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Add(other *Array) *Array {
        var out = New("ADD");
        C.mlx_add(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Addmm(a, b *Array, alpha, beta float32) *Array {
        var out = New("ADDMM");
        C.mlx_addmm(&out.ctx, t.ctx, a.ctx, b.ctx, C.float(alpha), C.float(beta), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Argmax(axis int, keepDims boolean) *Array {
        var out = New("ARGMAX");
        C.mlx_argmax_axis(&out.ctx, t.ctx, C.int(axis), C.boolean(keepDims), DefaultStream().ctx);
        return out;
    }
        func (t *Array) ArgpartitionAxis(kth int, axis int) *Array {
        var out = New("ARGPARTITION");
        C.mlx_argpartition_axis(&out.ctx, t.ctx, C.int(kth), C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) ArgsortAxis(axis int) *Array {
        var out = New("ARGSORT_AXIS");
        C.mlx_argsort_axis(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) AsType(dtype DType) *Array {
        var out = New("AS_TYPE");
        C.mlx_astype(&out.ctx, t.ctx, C.mlx_dtype(dtype), DefaultStream().ctx);
        return out;
    }
        func (t *Array) AsStrided(shape []int, strides []int, offset int) *Array {
        var cShape = make([]C.int, len(shape));
        var for i, s = range shape {
        cShape[i] = C.int(s);
    }
        var cStrides = make([]C.int64_t, len(strides));
        var for i, s = range strides {
        cStrides[i] = C.int64_t(s);
    }
        var out = New("AS_STRIDED");
        C.mlx_as_strided(;
        &out.ctx, t.ctx,;
        unsafe.SliceData(cShape), C.size_t(len(shape)),;
        unsafe.SliceData(cStrides), C.size_t(len(strides)),;
        C.size_t(offset),;
        DefaultStream().ctx,;
        );
        return out;
    }
        func (t *Array) Concatenate(axis int, others ...*Array) *Array {
        var vector = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(vector);
        var s = append([]*Array{t}, others...);
        var for _, other = range s {
        C.mlx_vector_array_append_value(vector, other.ctx);
    }
        var out = New("CONCATENATE");
        C.mlx_concatenate_axis(&out.ctx, vector, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Cumsum(axis int, reverse, inclusive boolean) *Array {
        var out = New("CUMSUM");
        C.mlx_cumsum(&out.ctx, t.ctx, C.int(axis), C.boolean(reverse), C.boolean(inclusive), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Divide(other *Array) *Array {
        var out = New("DIVIDE");
        C.mlx_divide(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) ExpandDims(axis int) *Array {
        var out = New("EXPAND_DIMS");
        C.mlx_expand_dims(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Flatten(startAxis, endAxis int) *Array {
        var out = New("FLATTEN");
        C.mlx_flatten(&out.ctx, t.ctx, C.int(startAxis), C.int(endAxis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) FloorDivide(other *Array) *Array {
        var out = New("FLOOR_DIVIDE");
        C.mlx_floor_divide(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) GatherMM(other, lhs, rhs *Array, sorted boolean) *Array {
        if lhs == null {
        lhs = New("");
    }
        if rhs == null {
        rhs = New("");
    }
        var out = New("GATHER_MM");
        C.mlx_gather_mm(&out.ctx, t.ctx, other.ctx, lhs.ctx, rhs.ctx, C.boolean(sorted), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Logsumexp(keepDims boolean) *Array {
        var out = New("LOGSUMEXP");
        C.mlx_logsumexp(&out.ctx, t.ctx, C.boolean(keepDims), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Less(other *Array) *Array {
        var out = New("LESS");
        C.mlx_less(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Matmul(other *Array) *Array {
        var out = New("MATMUL");
        C.mlx_matmul(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Multiply(other *Array) *Array {
        var out = New("MULTIPLY");
        C.mlx_multiply(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Negative() *Array {
        var out = New("NEGATIVE");
        C.mlx_negative(&out.ctx, t.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Power(exponent *Array) *Array {
        var out = New("POWER");
        C.mlx_power(&out.ctx, t.ctx, exponent.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) PutAlongAxis(indices, values *Array, axis int) *Array {
        var out = New("PUT_ALONG_AXIS");
        C.mlx_put_along_axis(&out.ctx, t.ctx, indices.ctx, values.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Reshape(axes ...int) *Array {
        var cAxes = make([]C.int, len(axes));
        var for i = range axes {
        cAxes[i] = C.int(axes[i]);
    }
        var out = New("RESHAPE");
        C.mlx_reshape(&out.ctx, t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Sigmoid() *Array {
        var out = New("SIGMOID");
        C.mlx_sigmoid(&out.ctx, t.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Sqrt() *Array {
        var out = New("SQRT");
        C.mlx_sqrt(&out.ctx, t.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Squeeze(axis int) *Array {
        var out = New("SQUEEZE");
        C.mlx_squeeze_axis(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) StackAxis(axis int, others ...*Array) *Array {
        var vectorData = make([]C.mlx_array, len(others)+1);
        vectorData[0] = t.ctx;
        var for i = range others {
        vectorData[i+1] = others[i].ctx;
    }
        var vector = C.mlx_vector_array_new_data(unsafe.SliceData(vectorData), C.size_t(len(vectorData)));
        defer C.mlx_vector_array_free(vector);
        var out = New("STACK_AXIS");
        C.mlx_stack_axis(&out.ctx, vector, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Subtract(other *Array) *Array {
        var out = New("SUBTRACT");
        C.mlx_subtract(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) SumAxis(axis int, keepDims boolean) *Array {
        var out = New("SUM_AXIS");
        C.mlx_sum_axis(&out.ctx, t.ctx, C.int(axis), C.boolean(keepDims), DefaultStream().ctx);
        return out;
    }
        func (t *Array) TakeAxis(indices *Array, axis int) *Array {
        var out = New("TAKE_AXIS");
        C.mlx_take_axis(&out.ctx, t.ctx, indices.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) TakeAlongAxis(indices *Array, axis int) *Array {
        var out = New("TAKE_ALONG_AXIS");
        C.mlx_take_along_axis(&out.ctx, t.ctx, indices.ctx, C.int(axis), DefaultStream().ctx);
        return out;
    }
        func (t *Array) Tanh() *Array {
        var out = New("TANH");
        C.mlx_tanh(&out.ctx, t.ctx, DefaultStream().ctx);
        return out;
    }
        func (t *Array) Transpose(axes ...int) *Array {
        var cAxes = make([]C.int, len(axes));
        var for i, axis = range axes {
        cAxes[i] = C.int(axis);
    }
        var out = New("TRANSPOSE");
        C.mlx_transpose_axes(&out.ctx, t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), DefaultStream().ctx);
        return out;
    }
        func Zeros(dtype DType, shape ...int) *Array {
        var cAxes = make([]C.int, len(shape));
        var for i = range shape {
        cAxes[i] = C.int(shape[i]);
    }
        var t = New("ZEROS");
        C.mlx_zeros(&t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), C.mlx_dtype(dtype), DefaultStream().ctx);
        return t;
    }
}
