package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class nn {

    public static class Linear {
        public Array Weight;
        public Array Bias;
    }
        func (m Linear) Forward(x *Array) *Array {
        var w = m.Weight.Transpose(1, 0);
        if m.Bias.Valid() {
        return m.Bias.Addmm(x, w, 1.0, 1.0);
    }
        return x.Matmul(w);
    }
        func (m Linear) Gather(x, lhs, rhs *Array, sorted boolean) *Array {
        var w = m.Weight.Transpose(0, 2, 1);
        return x.GatherMM(w, lhs, rhs, sorted);
    }

    public static class Embedding {
        public Array Weight;
    }
        func (e *Embedding) Forward(indices *Array) *Array {
        return e.Weight.TakeAxis(indices, 0);
    }
        func (e *Embedding) AsLinear() Linear {
        return Linear{
        Weight: e.Weight,;
    }
    }
}
