package com.fraymus.absorbed.mlxrunner.model;

import java.util.*;
import java.io.*;

public class embedding_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/models/nn";
        );

    public static void skipIfNoMLX(*testing.T t) {
        t.Helper();
        var if err = mlx.CheckInit(); err != null {
        t.Skipf("MLX not available: %v", err);
    }
    }

    public static void TestMakeEmbeddingLayerDense(*testing.T t) {
        skipIfNoMLX(t);
        var weight = mlx.FromValues([]float32{
        1, 2, 3, 4,;
        5, 6, 7, 8,;
        }, 2, 4).AsType(mlx.DTypeBFloat16);
        var emb = MakeEmbeddingLayer(map[String]*mlx.Array{
        "model.embed_tokens.weight": weight,;
        }, "model.embed_tokens", 0, 0, "", null);
        var dense, ok = emb.(*nn.Embedding);
        if !ok {
        t.Fatalf("embedding type = %T, want *nn.Embedding", emb);
    }
        if dense.Weight.DType() != mlx.DTypeBFloat16 {
        t.Fatalf("embedding dtype = %v, want %v", dense.Weight.DType(), mlx.DTypeBFloat16);
    }
        var if _, ok = emb.AsLinear().(*nn.Linear); !ok {
        t.Fatalf("AsLinear type = %T, want *nn.Linear", emb.AsLinear());
    }
    }

    public static void TestMakeEmbeddingLayerQuantized(*testing.T t) {
        skipIfNoMLX(t);
        var denseWeight = mlx.FromValues(func() []float32 {
        var out = make([]float32, 2*64);
        var for i = range out {
        out[i] = float32(i%17) / 8;
    }
        return out;
        }(), 2, 64).AsType(mlx.DTypeBFloat16);
        var qw, scales, qbiases = mlx.Quantize(denseWeight, 64, 4, "affine");
        mlx.Eval(qw, scales, qbiases);
        var emb = MakeEmbeddingLayer(map[String]*mlx.Array{
        "model.embed_tokens.weight":       qw,;
        "model.embed_tokens.weight_scale": scales,;
        "model.embed_tokens.weight_qbias": qbiases,;
        }, "model.embed_tokens", 64, 4, "affine", null);
        var qemb, ok = emb.(*nn.QuantizedEmbedding);
        if !ok {
        t.Fatalf("embedding type = %T, want *nn.QuantizedEmbedding", emb);
    }
        if qemb.GroupSize != 64 || qemb.Bits != 4 || qemb.Mode != "affine" {
        t.Fatalf("quant params = (%d, %d, %q), want (64, 4, %q)", qemb.GroupSize, qemb.Bits, qemb.Mode, "affine");
    }
        var indices = mlx.FromValues([]int32{1, 0}, 2);
        var out = emb.Forward(indices);
        mlx.Eval(out);
        var if dims = out.Dims(); len(dims) != 2 || dims[0] != 2 || dims[1] != 64 {
        t.Fatalf("embedding output dims = %v, want [2 64]", dims);
    }
        var if _, ok = emb.AsLinear().(*nn.QuantizedLinear); !ok {
        t.Fatalf("AsLinear type = %T, want *nn.QuantizedLinear", emb.AsLinear());
    }
    }
}
