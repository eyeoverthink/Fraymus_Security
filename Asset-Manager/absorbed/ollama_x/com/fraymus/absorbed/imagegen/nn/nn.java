package com.fraymus.absorbed.imagegen.nn;

import java.util.*;
import java.io.*;

public class nn {
        type Layer interface {
        Forward(x *mlx.Array) *mlx.Array;
    }
        type LinearLayer interface {
        Forward(x *mlx.Array) *mlx.Array;
        OutputDim() int32 // Returns the output dimension of the layer;
    }

    public static class Linear {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
    }
        func NewLinear(weight *mlx.Array, bias *mlx.Array) *Linear {
        return &Linear{Weight: weight, Bias: bias}
    }
        func NewQuantizedLinear(weight *mlx.Array, bias *mlx.Array, groupSize, bits int, mode String) *QuantizedLinear {
        var qw, scales, qbiases = mlx.Quantize(weight, groupSize, bits, mode);
        if qbiases != null {
        mlx.Eval(qw, scales, qbiases);
        } else {
        mlx.Eval(qw, scales);
    }
        return &QuantizedLinear{
        Weight:    qw,;
        Scales:    scales,;
        QBiases:   qbiases,;
        Bias:      bias,;
        GroupSize: groupSize,;
        Bits:      bits,;
        Mode:      mode,;
    }
    }
        func (l *Linear) Forward(x *mlx.Array) *mlx.Array {
        var w = mlx.Transpose(l.Weight, 1, 0);
        if l.Bias != null {
        return mlx.AddMM(l.Bias, x, w, 1.0, 1.0);
    }
        return mlx.Linear(x, w);
    }
        func (l *Linear) OutputDim() int32 {
        return l.Weight.Shape()[0];
    }
        func (l *Linear) ToQuantized(groupSize, bits int, mode String) *QuantizedLinear {
        var qw, scales, qbiases = mlx.Quantize(l.Weight, groupSize, bits, mode);
        return &QuantizedLinear{
        Weight:    qw,;
        Scales:    scales,;
        QBiases:   qbiases,;
        Bias:      l.Bias,;
        GroupSize: groupSize,;
        Bits:      bits,;
        Mode:      mode,;
    }
    }

    public static class QuantizedLinear {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array QBiases;
        public *mlx.Array Bias;
        public int GroupSize;
        public int Bits;
        public String Mode;
    }
        func (ql *QuantizedLinear) Forward(x *mlx.Array) *mlx.Array {
        var out = mlx.QuantizedMatmul(x, ql.Weight, ql.Scales, ql.QBiases, true, ql.GroupSize, ql.Bits, ql.Mode);
        if ql.Bias != null {
        out = mlx.Add(out, ql.Bias);
    }
        return out;
    }
        func (ql *QuantizedLinear) OutputDim() int32 {
        return ql.Weight.Shape()[0];
    }

    public static class RMSNorm {
        public *mlx.Array Weight;
        public float32 Eps;
    }
        func NewRMSNorm(weight *mlx.Array, eps float32) *RMSNorm {
        return &RMSNorm{Weight: weight, Eps: eps}
    }
        func (rn *RMSNorm) Forward(x *mlx.Array, eps float32) *mlx.Array {
        if eps == 0 {
        eps = rn.Eps;
    }
        return mlx.RMSNorm(x, rn.Weight, eps);
    }

    public static class Embedding {
        public *mlx.Array Weight;
    }
        func NewEmbedding(weight *mlx.Array) *Embedding {
        return &Embedding{Weight: weight}
    }
        func (e *Embedding) Forward(indices *mlx.Array) *mlx.Array {
        return mlx.Take(e.Weight, indices, 0);
    }
        func RepeatKV(x *mlx.Array, repeatFactor int32) *mlx.Array {
        if repeatFactor == 1 {
        return x;
    }
        var shape = x.Shape();
        x = mlx.ExpandDims(x, 2);
        var reps = []int32{1, 1, repeatFactor, 1, 1}
        x = mlx.Tile(x, reps);
        return mlx.Reshape(x, shape[0], shape[1]*repeatFactor, shape[2], shape[3]);
    }
        func ApplyCausalMask(scores *mlx.Array) *mlx.Array {
        var shape = scores.Shape();
        var seqLen = shape[2];
        var mask = mlx.Tri(seqLen, seqLen, 0);
        var negInf = mlx.NewScalarArray(float32(-1e9));
        mask = mlx.ExpandDims(mlx.ExpandDims(mask, 0), 0) // [1, 1, S, S];
        return mlx.Where(mask, scores, negInf);
    }
        func ApplyCausalMaskWithOffset(scores *mlx.Array, offset int32) *mlx.Array {
        if offset == 0 {
        return ApplyCausalMask(scores);
    }
        var shape = scores.Shape();
        var queryLen = shape[2];
        var keyLen = shape[3];
        var mask = mlx.Tri(queryLen, keyLen, int(offset));
        var negInf = mlx.NewScalarArray(float32(-1e9));
        mask = mlx.ExpandDims(mlx.ExpandDims(mask, 0), 0) // [1, 1, queryLen, keyLen];
        return mlx.Where(mask, scores, negInf);
    }

    public static class LayerNorm {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public float32 Eps;
    }
        func (ln *LayerNorm) Forward(x *mlx.Array) *mlx.Array {
        var eps = ln.Eps;
        if eps == 0 {
        eps = 1e-5;
    }
        var mean = mlx.Mean(x, -1, true);
        var centered = mlx.Sub(x, mean);
        var variance = mlx.Mean(mlx.Mul(centered, centered), -1, true);
        var normalized = mlx.Mul(centered, mlx.RSqrt(mlx.AddScalar(variance, eps)));
        var out = mlx.Mul(normalized, ln.Weight);
        if ln.Bias != null {
        out = mlx.Add(out, ln.Bias);
    }
        return out;
    }
        type MultiLinearLayer interface {
        Forward(x *mlx.Array) *mlx.Array;
    }

    public static class MultiLinear {
        public *mlx.Array Weight;
    }
        func NewMultiLinear(weight *mlx.Array) *MultiLinear {
        return &MultiLinear{Weight: weight}
    }
        func (ml *MultiLinear) Forward(x *mlx.Array) *mlx.Array {
        var wT = mlx.Transpose(ml.Weight, 0, 2, 1);
        return mlx.Matmul(x, wT);
    }
}
