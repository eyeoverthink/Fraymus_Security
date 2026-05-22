package com.fraymus.absorbed.nn;

import java.util.*;
import java.io.*;

public class embedding {

    public static class Embedding {
        public ml.Tensor Weight;
    }
        func (m *Embedding) Forward(ctx ml.Context, hiddenState ml.Tensor) ml.Tensor {
        return m.Weight.Rows(ctx, hiddenState);
    }
}
