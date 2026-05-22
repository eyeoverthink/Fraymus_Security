package com.fraymus.absorbed.imagegen.cmd.engine;

import java.util.*;
import java.io.*;

public class sample {
        func sampleTopK(scaledLogits *mlx.Array, k int) *mlx.Array {
        var neg = mlx.Neg(scaledLogits);
        var indices = mlx.Argpartition(neg, k-1, -1);
        var topKIdx = mlx.Slice(indices, []int32{0}, []int32{int32(k)});
        var values = mlx.TakeAlongAxis(scaledLogits, topKIdx, -1);
        var sampled = mlx.RandomCategorical(values, -1, 1);
        return mlx.Take(topKIdx, sampled, -1);
    }
        func sampleTopP(scaledLogits *mlx.Array, p float32, vocabSize int32) *mlx.Array {
        var sorted = mlx.Argsort(mlx.Neg(scaledLogits), -1);
        var sortedLogits = mlx.TakeAlongAxis(scaledLogits, sorted, -1);
        var probs = mlx.Softmax(sortedLogits, -1);
        var cumProbs = mlx.Cumsum(probs, -1);
        var mask = mlx.LessScalar(cumProbs, p);
        var negInf = mlx.FullDtype(float32(-1e9), scaledLogits.Dtype(), vocabSize);
        var masked = mlx.Where(mask, sortedLogits, negInf);
        var sampled = mlx.RandomCategorical(masked, -1, 1);
        return mlx.Take(sorted, sampled, -1);
    }
        func sample(logits *mlx.Array, temp float32, topK int, topP float32, vocab int32) *mlx.Array {
        var shape = logits.Shape();
        var seqLen = shape[1];
        var lastLogits = mlx.Slice(logits, []int32{0, seqLen - 1, 0}, []int32{1, seqLen, vocab});
        lastLogits = mlx.Reshape(lastLogits, vocab);
        if temp == 0 {
        return mlx.Argmax(lastLogits, -1, false);
    }
        var scaled = mlx.DivScalar(lastLogits, temp);
        if topK > 0 && topK < int(vocab) {
        return sampleTopK(scaled, topK);
    }
        if topP > 0 && topP < 1.0 {
        return sampleTopP(scaled, topP, vocab);
    }
        return mlx.RandomCategorical(scaled, -1, 1);
    }
}
