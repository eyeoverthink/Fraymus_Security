package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class linear {

    public static class Linear {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (m *Linear) Forward(ctx ml.Context, t ml.Tensor) ml.Tensor {
        t = m.Weight.Mulmat(ctx, t);
        if m.Bias != null {
        t = t.Add(ctx, m.Bias);
    }
        return t;
    }

    public static class LinearBatch {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (m *LinearBatch) Forward(ctx ml.Context, t, indices ml.Tensor) ml.Tensor {
        t = m.Weight.MulmatID(ctx, t, indices);
        if m.Bias != null {
        t = t.AddID(ctx, m.Bias, indices);
    }
        return t;
    }
}
