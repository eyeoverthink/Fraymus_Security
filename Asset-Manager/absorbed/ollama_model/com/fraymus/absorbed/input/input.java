package com.fraymus.absorbed.input;

import java.util.*;
import java.io.*;

public class input {

    public static class Multimodal {
        public ml.Tensor Tensor;
        public any Data;
    }

    public static class Input {
        public int32 Token;
        public []Multimodal Multimodal;
        public uint64 MultimodalHash;
        public int SameBatch;
    }

    public static class MultimodalIndex {
        public int Index;
        public []Multimodal Multimodal;
    }

    public static class Batch {
        public ml.Tensor Inputs;
        public ml.Tensor Outputs;
        public []int32 Positions;
        public []int Sequences;
        public []MultimodalIndex Multimodal;
    }
}
