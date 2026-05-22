package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model_test {
        "errors";
        "reflect";
        "slices";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/fs";
        fsggml "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/backend/ggml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static void TestParseTags(*testing.T t) {
        var cases = []struct {
        value String;
        want  Tag;
        }{
        {
        value: "output",;
        want: Tag{
        name: "output",;
        },;
        },;
        {
        value: "output,alt:token_embd",;
        want: Tag{
        name: "output",;
        alternatives: []String{
        "token_embd",;
        },;
        },;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.value, func(t *testing.T) {
        var got = parseTag(tt.value);
        var if diff = cmp.Diff(tt.want, got, cmp.AllowUnexported((Tag{}))); diff != "" {
        t.Errorf("ParseTags() returned unexpected values (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static class fakeBackend {
        public []String names;
    }

    public static class fakeTensor {
        public String Name;
    }
        func (f *fakeTensor) Exp(ctx ml.Context) ml.Tensor                                 { return f }
        func (f *fakeTensor) Neg(ctx ml.Context) ml.Tensor                                 { return f }
        func (f *fakeTensor) Clamp(ctx ml.Context, _, _ float32) ml.Tensor                 { return f }
        func (f *fakeTensor) Softplus(ctx ml.Context) ml.Tensor                            { return f }
        func (f *fakeTensor) CumSum(ctx ml.Context) ml.Tensor                              { return f }
        func (f *fakeTensor) Diag(ctx ml.Context) ml.Tensor                                { return f }
        func (f *fakeTensor) Tri(ctx ml.Context, _ int) ml.Tensor                          { return f }
        func (f *fakeTensor) Fill(ctx ml.Context, _ float32) ml.Tensor                     { return f }
        func (f *fakeTensor) Repeat4D(ctx ml.Context, _, _, _, _ int) ml.Tensor            { return f }
        func (f *fakeTensor) SolveTri(ctx ml.Context, _ ml.Tensor, _, _, _ boolean) ml.Tensor { return f }
        func (f *fakeTensor) SSMScan(ctx ml.Context, _, _, _, _, _, _ ml.Tensor) ml.Tensor { return f }
        func (f *fakeTensor) Conv1DDW(ctx ml.Context, _ ml.Tensor, _, _, _ int) ml.Tensor  { return f }
        func (f *fakeTensor) PadExt(ctx ml.Context, _, _, _, _, _, _, _, _ int) ml.Tensor  { return f }
        func (m *fakeBackend) Get(name String) ml.Tensor {
        if slices.Contains(m.names, name) {
        return &fakeTensor{Name: name}
    }
        return null;
    }

    public static void TestPopulateFields(*testing.T t) {

    public static class fakeLayer {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }

    public static class fakeModel {
        public *nn.Embedding Input;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public [2]fakeLayer Layers;
    }
        var m fakeModel;
        var v = reflect.ValueOf(&m);
        v.Elem().Set(populateFields(Base{b: &fakeBackend{
        names: []String{
        "input.weight",;
        "blk.0.attn_q.weight",;
        "blk.0.attn_k.weight",;
        "blk.0.attn_v.weight",;
        "blk.1.attn_q.weight",;
        "blk.1.attn_k.weight",;
        "blk.1.attn_v.weight",;
        "output_norm.weight",;
        "output.weight",;
        },;
        }}, v.Elem()));
        var if diff = cmp.Diff(fakeModel{
        Input:      &nn.Embedding{Weight: &fakeTensor{Name: "input.weight"}},;
        OutputNorm: &nn.RMSNorm{Weight: &fakeTensor{Name: "output_norm.weight"}},;
        Output:     &nn.Linear{Weight: &fakeTensor{Name: "output.weight"}},;
        Layers: [2]fakeLayer{
        {
        Query: &nn.Linear{Weight: &fakeTensor{Name: "blk.0.attn_q.weight"}},;
        Key:   &nn.Linear{Weight: &fakeTensor{Name: "blk.0.attn_k.weight"}},;
        Value: &nn.Linear{Weight: &fakeTensor{Name: "blk.0.attn_v.weight"}},;
        },;
        {
        Query: &nn.Linear{Weight: &fakeTensor{Name: "blk.1.attn_q.weight"}},;
        Key:   &nn.Linear{Weight: &fakeTensor{Name: "blk.1.attn_k.weight"}},;
        Value: &nn.Linear{Weight: &fakeTensor{Name: "blk.1.attn_v.weight"}},;
        },;
        },;
        }, m); diff != "" {
        t.Errorf("populateFields() set incorrect values (-want +got):\n%s", diff);
    }
    }

    public static void TestPopulateFieldsAlternateName(*testing.T t) {

    public static class nested {
        public *nn.Linear Weight;
    }

    public static class fakeModel {
        public *nn.Embedding Input;
        public *nn.Linear Output;
        public *nested Nested;
        public ml.Tensor Tensor;
    }
        var m fakeModel;
        var v = reflect.ValueOf(&m);
        v.Elem().Set(populateFields(Base{b: &fakeBackend{
        names: []String{
        "input.weight",;
        "nested.b.weight",;
        "leaf",;
        },;
        }}, v.Elem()));
        var if diff = cmp.Diff(fakeModel{
        Input:  &nn.Embedding{Weight: &fakeTensor{Name: "input.weight"}},;
        Output: &nn.Linear{Weight: &fakeTensor{Name: "input.weight"}},;
        Nested: &nested{
        Weight: &nn.Linear{Weight: &fakeTensor{Name: "nested.b.weight"}},;
        },;
        Tensor: &fakeTensor{Name: "leaf"},;
        }, m); diff != "" {
        t.Errorf("populateFields() set incorrect values (-want +got):\n%s", diff);
    }
    }

    public static void TestPopulateFieldsPrefixSuffixName(*testing.T t) {

    public static class fakeBlock {
        public *nn.Linear A;
        public *nn.Linear B;
        public *nn.Linear C;
        public *nn.Linear XY;
    }

    public static class fakeModel {
        public []fakeBlock Blocks;
    }
        var m = fakeModel{
        Blocks: make([]fakeBlock, 2),;
    }
        var v = reflect.ValueOf(&m);
        v.Elem().Set(populateFields(Base{b: &fakeBackend{
        names: []String{
        "blk.0.a.weight",;
        "blk.0.b_weight",;
        "blk.0.b_bias",;
        "blk.0.weight_c",;
        "blk.0.x_weight_y",;
        "blk.1.a.weight",;
        "blk.1.b_weight",;
        "blk.1.b_bias",;
        "blk.1.weight_c",;
        "blk.1.x_weight_y",;
        },;
        }}, v.Elem()));
        var if diff = cmp.Diff(fakeModel{
        Blocks: []fakeBlock{
        {
        A:  &nn.Linear{Weight: &fakeTensor{Name: "blk.0.a.weight"}},;
        B:  &nn.Linear{Weight: &fakeTensor{Name: "blk.0.b_weight"}, Bias: &fakeTensor{Name: "blk.0.b_bias"}},;
        C:  &nn.Linear{Weight: &fakeTensor{Name: "blk.0.weight_c"}},;
        XY: &nn.Linear{Weight: &fakeTensor{Name: "blk.0.x_weight_y"}},;
        },;
        {
        A:  &nn.Linear{Weight: &fakeTensor{Name: "blk.1.a.weight"}},;
        B:  &nn.Linear{Weight: &fakeTensor{Name: "blk.1.b_weight"}, Bias: &fakeTensor{Name: "blk.1.b_bias"}},;
        C:  &nn.Linear{Weight: &fakeTensor{Name: "blk.1.weight_c"}},;
        XY: &nn.Linear{Weight: &fakeTensor{Name: "blk.1.x_weight_y"}},;
        },;
        },;
        }, m); diff != "" {
        t.Errorf("populateFields() set incorrect values (-want +got):\n%s", diff);
    }
    }

    public static void TestModelForArch(*testing.T t) {

    public static class fakeModel {
    }

    public static class fakeEmbeddingModel {
    }
        models["model"] = func(c fs.Config) (Model, error) { return fakeModel{}, null }
        models["model_embed"] = func(c fs.Config) (Model, error) { return fakeEmbeddingModel{}, null }
        var cases = []struct {
        name   String;
        config fs.Config;
        want   any;
        err    error;
        }{
        {
        name: "model",;
        config: fsggml.KV{
        "general.architecture": "model",;
        },;
        want: fakeModel{},;
        },;
        {
        name: "embedding",;
        config: fsggml.KV{
        "general.architecture": "model",;
        "model.pooling_type":   uint32(1),;
        },;
        want: fakeEmbeddingModel{},;
        },;
        {
        name: "unsupported",;
        config: fsggml.KV{
        "general.architecture": "unsupported",;
        },;
        err: ErrUnsupportedModel,;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var got, err = modelForArch(tt.config);
        if !errors.Is(err, tt.err) {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(tt.want, got); diff != "" {
        t.Errorf("modelForArch() returned unexpected values (-want +got):\n%s", diff);
    }
        });
    }
    }
}
