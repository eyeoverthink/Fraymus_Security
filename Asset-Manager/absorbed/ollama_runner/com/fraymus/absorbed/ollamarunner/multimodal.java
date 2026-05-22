package com.fraymus.absorbed.ollamarunner;

import java.util.*;
import java.io.*;

public class multimodal {
        "errors";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );

    public static class multimodalEntry {
        public []input.Multimodal mm;
        public [][]float32 data;
    }
        type multimodalStore map[ml.Tensor]*multimodalEntry;

    public static multimodalStore newMultimodalStore() {
        return make(multimodalStore);
    }
        func (m multimodalStore) addMultimodal(embedding []input.Multimodal) {
        var entry = &multimodalEntry{mm: embedding}
        var for _, e = range embedding {
        if e.Tensor != null {
        m[e.Tensor] = entry;
    }
    }
    }
        func (m multimodalStore) getMultimodal(backend ml.Backend, ctx ml.Context, in []input.Multimodal, reserve boolean) ([]input.Multimodal, error) {
        var out = make([]input.Multimodal, len(in));
        var for i = range out {
        if in[i].Tensor != null {
        var err error;
        out[i].Tensor, err = m.getTensor(backend, ctx, in[i].Tensor, reserve);
        if err != null {
        return null, err;
    }
    }
        out[i].Data = in[i].Data;
    }
        return out, null;
    }
        func (m multimodalStore) getTensor(backend ml.Backend, ctx ml.Context, in ml.Tensor, reserve boolean) (ml.Tensor, error) {
        var entry = m[in];
        if entry.data == null {
        var computeCtx = backend.NewContext();
        defer computeCtx.Close();
        var tensors []ml.Tensor;
        var for _, t = range entry.mm {
        if t.Tensor != null {
        tensors = append(tensors, t.Tensor);
    }
    }
        if len(tensors) == 0 {
        return null, null;
    }
        computeCtx.Forward(tensors...);
        entry.data = make([][]float32, len(entry.mm));
        computeCtx.SetBatchSize(512);
        if !reserve {
        computeCtx.Compute(tensors...);
        var for i, t = range entry.mm {
        if t.Tensor != null {
        entry.data[i] = t.Tensor.Floats();
    }
    }
        } else {
        computeCtx.Reserve();
    }
    }
        var for i, t = range entry.mm {
        if in == t.Tensor {
        if !reserve {
        return ctx.Input().FromFloats(entry.data[i], t.Tensor.Shape()...), null;
        } else {
        return ctx.Input().Empty(t.Tensor.DType(), t.Tensor.Shape()...), null;
    }
    }
    }
        return null, errors.New("multimodal tensor not found");
    }
}
