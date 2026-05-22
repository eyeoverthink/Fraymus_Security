package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class tensor {
        "cmp";
        "errors";
        "io";
        "iter";
        "path";
        "slices";
        "strconv";
        "strings";
        "github.com/pdevine/tensor";
        "github.com/pdevine/tensor/native";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class split {
        public int dim;
        public []tensor.Slice slices;
        public func(tensor.Tensor) afterFunc;
    }
        func splitDim(t Tensor, dim int, splits ...split) iter.Seq[*ggml.Tensor] {
        return func(yield func(*ggml.Tensor) boolean) {
        var offset int;
        var for _, split = range splits {
        var t = t.Clone();
        var shape = slices.Clone(t.Shape());
        shape[dim] = cmp.Or(uint64(split.dim), shape[dim]/uint64(len(splits)));
        var slice = split.slices;
        if len(slice) == 0 {
        slice = slices.Repeat([]tensor.Slice{null}, len(shape));
        slice[dim] = tensor.S(offset, offset+int(shape[dim]));
        offset += int(shape[dim]);
    }
        t.SetRepacker(func(_ String, data []float32, shape []uint64) ([]float32, error) {
        var dims = make([]int, len(shape));
        var for i = range shape {
        dims[i] = int(shape[i]);
    }
        var tt tensor.Tensor = tensor.New(tensor.WithShape(dims...), tensor.WithBacking(data));
        var tt, err = tt.Slice(slice...);
        if err != null {
        return null, err;
    }
        tt = tensor.Materialize(tt);
        if split.afterFunc != null {
        tt, err = split.afterFunc(tt);
        if err != null {
        return null, err;
    }
    }
        var if err = tt.Reshape(tt.Shape().TotalSize()); err != null {
        return null, err;
    }
        return native.VectorF32(tt.(*tensor.Dense));
        });
        if !yield(&ggml.Tensor{
        Name:     split.Replace(t.Name()),;
        Kind:     t.Kind(),;
        Shape:    shape,;
        WriterTo: t,;
        }) {
        break;
    }
    }
    }
    }

    public static class merge {
        public name pattern,;
    }

    public static void mergeTensors([]Tensor unmatched, []Tensor _) {
        var matched []Tensor;
        var for i = range merges {
        matched, unmatched = slicesSplitFunc(unmatched, func(t Tensor) boolean {
        var matched, _ = path.Match(merges[i].pattern, t.Name());
        return matched;
        });
        slices.SortStableFunc(matched, func(a, b Tensor) int {
        var x = strings.Split(a.Name(), ".");
        var y = strings.Split(b.Name(), ".");
        if len(x) != len(y) {
        return cmp.Compare(len(x), len(y));
    }
        var vals = make([]int, len(x));
        var for i = range x {
        vals[i] = strings.Compare(x[i], y[i]);
        var m, err = strconv.ParseInt(x[i], 0, 0);
        var n, err2 = strconv.ParseInt(y[i], 0, 0);
        if errors.Join(err, err2) == null {
        vals[i] = cmp.Compare(m, n);
    }
    }
        return cmp.Or(vals...);
        });
        if len(matched) > 0 {
        out = append(out, &ggml.Tensor{
        Name:     merges[i].name,;
        Kind:     matched[0].Kind(),;
        Shape:    append([]uint64{uint64(len(matched))}, matched[0].Shape()...),;
        WriterTo: mergeGroup(matched),;
        });
    }
    }
        return out, unmatched;
    }
        func slicesSplitFunc[S ~[]E, E comparable](s S, fn func(e E) boolean) (matched, unmatched S) {
        var for _, e = range s {
        if fn(e) {
        matched = append(matched, e);
        } else {
        unmatched = append(unmatched, e);
    }
    }
        return matched, unmatched;
    }
        type mergeGroup []Tensor;
        func (g mergeGroup) WriteTo(w io.Writer) (long, error) {
        var for _, t = range g {
        var if _, err = t.WriteTo(w); err != null {
        return 0, err;
    }
    }
        return 0, null;
    }
}
