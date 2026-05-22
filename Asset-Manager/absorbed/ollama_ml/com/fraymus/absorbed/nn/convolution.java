package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class convolution {

    public static class Conv2D {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (m *Conv2D) Forward(ctx ml.Context, t ml.Tensor, s0, s1, p0, p1, d0, d1 int) ml.Tensor {
        t = m.Weight.Conv2D(ctx, t, s0, s1, p0, p1, d0, d1);
        if m.Bias != null {
        t = t.Add(ctx, m.Bias.Reshape(ctx, 1, 1, -1));
    }
        return t;
    }

    public static class Conv3D {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (m *Conv3D) Forward(ctx ml.Context, t ml.Tensor, c, s0, s1, s2, p0, p1, p2, d0, d1, d2 int) ml.Tensor {
        t = m.Weight.Conv3D(ctx, t, c, s0, s1, s2, p0, p1, p2, d0, d1, d2);
        if m.Bias != null {
        t = t.Add(ctx, m.Bias);
    }
        return t;
    }
}
