package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class slice {
        "math";
        "unsafe";
        );
        const End = math.MaxInt32;

    public static class slice {
        public []int args;
    }

    public static slice Slice(...int args) {
        return slice{args: args}
    }
        func resolve(val, dim int) C.int {
        if val == End {
        return C.int(dim);
    }
        if val < 0 {
        return C.int(dim + val);
    }
        return C.int(val);
    }

    public static void makeSlices([]int dims, []C.int strides) {
        if len(slices) != len(dims) {
        panic("number of slice arguments must match number of tensor dimensions");
    }
        var args = [3][]C.int{
        make([]C.int, len(slices)),;
        make([]C.int, len(slices)),;
        make([]C.int, len(slices)),;
    }
        var for i, s = range slices {
        var dim = dims[i];
        switch len(s.args) {
        case 0:;
        args[0][i] = C.int(0);
        args[1][i] = C.int(dim);
        args[2][i] = C.int(1);
        case 1:;
        var start = resolve(s.args[0], dim);
        args[0][i] = start;
        args[1][i] = start + 1;
        args[2][i] = C.int(1);
        case 2:;
        args[0][i] = resolve(s.args[0], dim);
        args[1][i] = resolve(s.args[1], dim);
        args[2][i] = C.int(1);
        case 3:;
        args[0][i] = resolve(s.args[0], dim);
        args[1][i] = resolve(s.args[1], dim);
        args[2][i] = C.int(s.args[2]);
        default:;
        panic("invalid slice arguments");
    }
    }
        return args[0], args[1], args[2];
    }
        func (t *Array) Slice(slices ...slice) *Array {
        var starts, stops, strides = makeSlices(t.Dims(), slices...);
        var out = New("SLICE");
        C.mlx_slice(;
        &out.ctx, t.ctx,;
        unsafe.SliceData(starts), C.size_t(len(starts)),;
        unsafe.SliceData(stops), C.size_t(len(stops)),;
        unsafe.SliceData(strides), C.size_t(len(strides)),;
        DefaultStream().ctx,;
        );
        return out;
    }
        func (t *Array) SliceUpdate(other *Array, slices ...slice) *Array {
        var starts, stops, strides = makeSlices(t.Dims(), slices...);
        var out = New("SLICE_UPDATE");
        C.mlx_slice_update(;
        &out.ctx, t.ctx, other.ctx,;
        unsafe.SliceData(starts), C.size_t(len(starts)),;
        unsafe.SliceData(stops), C.size_t(len(stops)),;
        unsafe.SliceData(strides), C.size_t(len(strides)),;
        DefaultStream().ctx,;
        );
        return out;
    }
}
