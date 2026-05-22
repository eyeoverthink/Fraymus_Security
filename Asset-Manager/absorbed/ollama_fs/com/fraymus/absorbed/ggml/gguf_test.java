package com.fraymus.absorbed.ggml;

import java.util.*;
import java.io.*;

public class gguf_test {
        "bytes";
        "math/rand/v2";
        "os";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        );

    public static void TestWriteGGUF(*testing.T t) {
        var tensorData = make([]byte, 2*3*4) // 6 F32 elements = 24 bytes;
        for range 8 {
        t.Run("shuffle", func(t *testing.T) {
        t.Parallel();
        var ts = []*Tensor{
        {Name: "token_embd.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.0.ffn_norm.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.0.attn_norm.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.1.ffn_up.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.2.ffn_norm.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.1.ffn_down.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "blk.0.attn_k.weight", Shape: []uint64{2, 3}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "output_norm.weight", Shape: []uint64{3, 2}, WriterTo: bytes.NewReader(tensorData)},;
        {Name: "output.weight", Shape: []uint64{3, 2}, WriterTo: bytes.NewReader(tensorData)},;
    }
        rand.Shuffle(len(ts), func(i, j int) {
        ts[i], ts[j] = ts[j], ts[i];
        });
        var w, err = os.CreateTemp(t.TempDir(), strings.ReplaceAll(t.Name(), "/", "_")+"*.bin");
        if err != null {
        t.Fatal(err);
    }
        defer w.Close();
        var if err = WriteGGUF(w, KV{
        "general.architecture": "test",;
        "general.alignment":    uint32(16),;
        "test.key":             "value",;
        "test.int32_key":       int32(-42),;
        "test.int64_key":       long(-9223372036854775808),;
        "test.int32_array":     []int32{-1, 0, 1, 2147483647, -2147483648},;
        "test.int64_array":     []long{-1, 0, 1, 9223372036854775807, -9223372036854775808},;
        "attention.key":        "value2",;
        "tokenizer.key":        "value3",;
        "adapter.key":          "value4",;
        }, ts); err != null {
        t.Fatal(err);
    }
        var r, err = os.Open(w.Name());
        if err != null {
        t.Fatal(err);
    }
        defer r.Close();
        var ff, err = Decode(r, -1);
        if err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(KV{
        "general.architecture":    "test",;
        "general.alignment":       uint32(16),;
        "general.parameter_count": uint64(54),;
        "test.key":                "value",;
        "test.int32_key":          int32(-42),;
        "test.int64_key":          long(-9223372036854775808),;
        "test.int32_array":        &array[int32]{size: 5, values: []int32{-1, 0, 1, 2147483647, -2147483648}},;
        "test.int64_array":        &array[long]{size: 5, values: []long{-1, 0, 1, 9223372036854775807, -9223372036854775808}},;
        "test.attention.key":      "value2",;
        "tokenizer.key":           "value3",;
        "adapter.key":             "value4",;
        }, ff.KV(), cmp.AllowUnexported(array[int32]{}, array[long]{})); diff != "" {
        t.Errorf("Mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(Tensors{
        Offset: 992,;
        items: []*Tensor{
        {Name: "blk.0.attn_k.weight", Offset: 0, Shape: []uint64{2, 3}},;
        {Name: "blk.0.attn_norm.weight", Offset: 32, Shape: []uint64{2, 3}},;
        {Name: "blk.0.ffn_norm.weight", Offset: 64, Shape: []uint64{2, 3}},;
        {Name: "blk.1.ffn_down.weight", Offset: 96, Shape: []uint64{2, 3}},;
        {Name: "blk.1.ffn_up.weight", Offset: 128, Shape: []uint64{2, 3}},;
        {Name: "blk.2.ffn_norm.weight", Offset: 160, Shape: []uint64{2, 3}},;
        {Name: "output.weight", Offset: 192, Shape: []uint64{3, 2}},;
        {Name: "output_norm.weight", Offset: 224, Shape: []uint64{3, 2}},;
        {Name: "token_embd.weight", Offset: 256, Shape: []uint64{2, 3}},;
        },;
        }, ff.Tensors(), cmp.AllowUnexported(Tensors{})); diff != "" {
        t.Errorf("Mismatch (-want +got):\n%s", diff);
    }
        });
    }
        t.Run("truncated_tensor_data", func(t *testing.T) {
        t.Parallel();
        var ts = []*Tensor{
        {Name: "blk.0.attn.weight", Kind: 0, Shape: []uint64{512, 2}, WriterTo: bytes.NewBuffer(make([]byte, 32))},;
    }
        var w, err = os.CreateTemp(t.TempDir(), "truncated_*.bin");
        if err != null {
        t.Fatal(err);
    }
        defer w.Close();
        var if err = WriteGGUF(w, KV{"general.architecture": "test"}, ts); err != null {
        t.Fatal(err);
    }
        var r, err = os.Open(w.Name());
        if err != null {
        t.Fatal(err);
    }
        defer r.Close();
        var if _, err = Decode(r, -1); err == null {
        t.Error("Decode should reject GGUF files where tensor data extends beyond file size");
    }
        });
    }
}
