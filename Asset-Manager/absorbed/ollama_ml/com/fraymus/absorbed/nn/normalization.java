package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class normalization {
        "github.com/ollama/ollama/ml";
        );

    public static class LayerNorm {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (m *LayerNorm) Forward(ctx ml.Context, t ml.Tensor, eps float32) ml.Tensor {
        return t.LayerNorm(ctx, m.Weight, m.Bias, eps);
    }

    public static class RMSNorm {
        public ml.Tensor Weight;
    }
        func (m *RMSNorm) Forward(ctx ml.Context, t ml.Tensor, eps float32) ml.Tensor {
        return t.RMSNorm(ctx, m.Weight, eps);
    }
}
