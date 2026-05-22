package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class rope {
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn/rope";
        );
        type fastRoPE interface {
        RoPE(ctx ml.Context, positions ml.Tensor, dim int, base, scale float32, options ...func(*rope.Options)) ml.Tensor;
    }
        func RoPE(ctx ml.Context, t, positions ml.Tensor, dim int, base, scale float32, options ...func(*rope.Options)) ml.Tensor {
        var if t, ok = t.(fastRoPE); ok {
        return t.RoPE(ctx, positions, dim, base, scale, options...);
    }
        panic("RoPE not implemented for this tensor type");
    }
}
