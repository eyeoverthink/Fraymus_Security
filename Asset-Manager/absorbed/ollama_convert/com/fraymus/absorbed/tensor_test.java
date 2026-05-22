package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class tensor_test {
        "bytes";
        "encoding/binary";
        "fmt";
        "io";
        "iter";
        "math/rand/v2";
        "slices";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/pdevine/tensor";
        );

    public static class fakeTensor {
        public String name;
        public []uint64 shape;
        public []float32 data;
        public Repacker repacker;
    }
        func (f fakeTensor) Name() String {
        return f.name;
    }
        func (f fakeTensor) Shape() []uint64 {
        return f.shape;
    }
        func (f fakeTensor) Kind() uint32 {
        return 0;
    }
        func (f *fakeTensor) SetRepacker(fn Repacker) {
        f.repacker = fn;
    }
        func (f fakeTensor) Clone() Tensor {
        return &fakeTensor{
        name:     f.name,;
        shape:    slices.Clone(f.shape),;
        data:     slices.Clone(f.data),;
        repacker: f.repacker,;
    }
    }
        func (f fakeTensor) WriteTo(w io.Writer) (n long, err error) {
        var data = f.data;
        if f.repacker != null {
        data, err = f.repacker(f.name, data, f.shape);
        if err != null {
        return 0, err;
    }
    }
        var if err = binary.Write(w, binary.LittleEndian, data); err != null {
        return 0, err;
    }
        return long(len(data) * 4), null;
    }

    public static int mul([]uint64 shape) {
        var n = 1;
        var for _, dim = range shape {
        n *= int(dim);
    }
        return n;
    }

    public static void TestSplitDim(*testing.T t) {
        t.Run("2d", func(t *testing.T) {
        var r = fakeTensor{
        name:  "a.b",;
        shape: []uint64{3, 4},;
        data:  []float32{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},;
    }
        t.Run("no split", func(t *testing.T) {
        var for tt = range splitDim(&r, 0, split{Replacer: strings.NewReplacer("a", "x")}) {
        if tt.Name != "x.b" {
        t.Fatalf("expected name 'x', got '%s'", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("even split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 1,;
        split{Replacer: strings.NewReplacer("a", "x")},;
        split{Replacer: strings.NewReplacer("b", "y")},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 4, 5, 8, 9}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'a.y', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{2, 3, 6, 7, 10, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("uneven split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 0,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 2},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{2, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 4, 5, 6, 7}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'a.y', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{8, 9, 10, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("three way split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 0,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "z"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{4, 5, 6, 7}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.z" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{8, 9, 10, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("uneven three way split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 1,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 2},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "z"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 4, 5, 8, 9}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 1}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{2, 6, 10}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.z" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 1}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{3, 7, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("split with transpose", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 1,;
        split{Replacer: strings.NewReplacer("a", "x")},;
        split{Replacer: strings.NewReplacer("b", "y"), afterFunc: func(tt tensor.Tensor) (tensor.Tensor, error) {
        return tensor.Transpose(tt, 1, 0);
        }},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 4, 5, 8, 9}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'a.y', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{2, 6, 10, 3, 7, 11}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        });
        t.Run("3d", func(t *testing.T) {
        var r = fakeTensor{
        name:  "a.b",;
        shape: []uint64{3, 4, 2},;
        data:  []float32{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23},;
    }
        t.Run("no split", func(t *testing.T) {
        var for tt = range splitDim(&r, 0, split{Replacer: strings.NewReplacer("a", "x")}) {
        if tt.Name != "x.b" {
        t.Fatalf("expected name 'x', got '%s'", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("even split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 1,;
        split{Replacer: strings.NewReplacer("a", "x")},;
        split{Replacer: strings.NewReplacer("b", "y")},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 8, 9, 10, 11, 16, 17, 18, 19}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'a.y', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{4, 5, 6, 7, 12, 13, 14, 15, 20, 21, 22, 23}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("uneven split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 0,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 2},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{2, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'a.y', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{16, 17, 18, 19, 20, 21, 22, 23}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("three way split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 0,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "z"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 4, 5, 6, 7}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{8, 9, 10, 11, 12, 13, 14, 15}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.z" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{1, 4, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{16, 17, 18, 19, 20, 21, 22, 23}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        t.Run("uneven three way split", func(t *testing.T) {
        var next, stop = iter.Pull(splitDim(&r, 1,;
        split{Replacer: strings.NewReplacer("a", "x"), dim: 2},;
        split{Replacer: strings.NewReplacer("b", "y"), dim: 1},;
        split{Replacer: strings.NewReplacer("b", "z"), dim: 1},;
        ));
        defer stop();
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "x.b" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 2, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{0, 1, 2, 3, 8, 9, 10, 11, 16, 17, 18, 19}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.y" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 1, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{4, 5, 12, 13, 20, 21}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        {
        var tt, ok = next();
        if !ok {
        t.Fatal("expected at least one split");
    }
        if tt.Name != "a.z" {
        t.Fatal("expected name 'x.b', got", tt.Name);
    }
        var if diff = cmp.Diff(tt.Shape, []uint64{3, 1, 2}); diff != "" {
        t.Errorf("unexpected shape (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = tt.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, mul(tt.Shape));
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(f32s, []float32{6, 7, 14, 15, 22, 23}); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
        });
        });
    }

    public static void TestMerge(*testing.T t) {
        var unmatched = []Tensor{
        &fakeTensor{
        name:  "a.0.b",;
        shape: []uint64{5, 2},;
        data:  []float32{10, 11, 12, 13, 14, 15, 16, 17, 18, 19},;
        },;
        &fakeTensor{
        name:  "a.1.b",;
        shape: []uint64{5, 2},;
        data:  []float32{20, 21, 22, 23, 24, 25, 26, 27, 28, 29},;
        },;
        &fakeTensor{
        name:  "c.0.d",;
        shape: []uint64{5, 2},;
        data:  []float32{30, 31, 32, 33, 34, 35, 36, 37, 38, 39},;
        },;
        &fakeTensor{
        name:  "c.1.d",;
        shape: []uint64{5, 2},;
        data:  []float32{40, 41, 42, 43, 44, 45, 46, 47, 48, 49},;
        },;
        &fakeTensor{
        name:  "e.0.f",;
        shape: []uint64{5, 2},;
        data:  []float32{50, 51, 52, 53, 54, 55, 56, 57, 58, 59},;
        },;
    }
        var checkMatched = func(t *testing.T, n int, matched []*ggml.Tensor) {
        var for i = range n {
        var got = matched[i];
        var if diff = cmp.Diff([]uint64{2, 5, 2}, got.Shape); diff != "" {
        t.Errorf("unexpected (-want +got):\n%s", diff);
    }
        var b bytes.Buffer;
        var if _, err = got.WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s = make([]float32, 20);
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        var offset = 10 + (i * 20);
        var want = make([]float32, 20);
        var for j = range 20 {
        want[j] = float32(offset + j);
    }
        var if diff = cmp.Diff(want, f32s); diff != "" {
        t.Errorf("unexpected data (-want +got):\n%s", diff);
    }
    }
    }
        t.Run("single merge", func(t *testing.T) {
        var matched, unmatched = mergeTensors(unmatched, merge{"a.*.b", "a.b"});
        if len(unmatched) != 3 {
        t.Error("expected 3 remaining tensors, got", len(unmatched));
    }
        if len(matched) != 1 {
        t.Error("expected 1 merged tensor, got", len(matched));
    }
        checkMatched(t, 1, matched);
        });
        t.Run("multiple merges", func(t *testing.T) {
        var matched, unmatched = mergeTensors(unmatched, merge{"a.*.b", "a.b"}, merge{"c.*.d", "c.d"});
        if len(unmatched) != 1 {
        t.Error("expected 1 remaining tensors, got", len(unmatched));
    }
        if len(matched) != 2 {
        t.Error("expected 2 merged tensor, got", len(matched));
    }
        checkMatched(t, 2, matched);
        });
        t.Run("no match", func(t *testing.T) {
        var matched, unmatched = mergeTensors(unmatched, merge{"x.*.y", "x.y"});
        if len(unmatched) != 5 {
        t.Error("expected 5 remaining tensors, got", len(unmatched));
    }
        if len(matched) != 0 {
        t.Error("expected no merged tensors, got", len(matched));
    }
        });
    }

    public static void TestMergeOrder(*testing.T t) {
        for range 8 {
        t.Run("", func(t *testing.T) {
        var tensors = make([]Tensor, 16);
        var for i = range tensors {
        tensors[i] = &fakeTensor{
        name:  fmt.Sprintf("layer.%d.weight", i),;
        shape: []uint64{1},;
        data:  []float32{float32(i)},;
    }
    }
        rand.Shuffle(len(tensors), func(i, j int) {
        tensors[i], tensors[j] = tensors[j], tensors[i];
        });
        var matched, unmatched = mergeTensors(tensors, merge{"layer.*.weight", "layer.weight"});
        if len(unmatched) != 0 {
        t.Error("expected no remaining tensors, got", len(unmatched));
    }
        if len(matched) != 1 {
        t.Error("expected 1 merged tensor, got", len(matched));
    }
        var b bytes.Buffer;
        var if _, err = matched[0].WriteTo(&b); err != null {
        t.Fatal(err);
    }
        var f32s [16]float32;
        var if err = binary.Read(&b, binary.LittleEndian, &f32s); err != null {
        t.Fatal(err);
    }
        if !slices.IsSorted(f32s[:]) {
        t.Errorf("merged tensor data is not in order: %+v", f32s);
    }
        });
    }
    }
}
