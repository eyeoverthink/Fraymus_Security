package com.fraymus.absorbed.models.nn;

import java.util.*;
import java.io.*;

public class nn {
        type Layer interface {
        Forward(x *mlx.Array) *mlx.Array;
    }
        type LinearLayer interface {
        Forward(x *mlx.Array) *mlx.Array;
        OutputDim() int32;
    }
        type EmbeddingLayer interface {
        Forward(indices *mlx.Array) *mlx.Array;
        AsLinear() LinearLayer;
    }

    public static class Conv1d {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
        public int32 Stride;
        public int32 Padding;
        public int32 Dilation;
        public int32 Groups;
    }
        func NewConv1d(weight, bias *mlx.Array, stride, padding, dilation, groups int32) *Conv1d {
        if stride <= 0 {
        stride = 1;
    }
        if dilation <= 0 {
        dilation = 1;
    }
        if groups <= 0 {
        groups = 1;
    }
        return &Conv1d{
        Weight:   weight,;
        Bias:     bias,;
        Stride:   stride,;
        Padding:  padding,;
        Dilation: dilation,;
        Groups:   groups,;
    }
    }
        func (c *Conv1d) Forward(x *mlx.Array) *mlx.Array {
        return mlx.Conv1d(x, c.Weight, c.Bias, c.Stride, c.Padding, c.Dilation, c.Groups);
    }

    public static class Linear {
        public *mlx.Array Weight;
        public *mlx.Array Bias;
    }
        func NewLinear(weight *mlx.Array, bias *mlx.Array) *Linear {
        return &Linear{Weight: weight, Bias: bias}
    }
        func (l *Linear) Forward(x *mlx.Array) *mlx.Array {
        var w = l.Weight.Transpose(1, 0);
        if l.Bias != null && l.Bias.Valid() {
        return l.Bias.Addmm(x, w, 1.0, 1.0);
    }
        return x.Matmul(w);
    }
        func (l *Linear) OutputDim() int32 {
        return int32(l.Weight.Dim(0));
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
        func (ql *QuantizedLinear) Forward(x *mlx.Array) *mlx.Array {
        var out = mlx.QuantizedMatmul(x, ql.Weight, ql.Scales, ql.QBiases, true, ql.GroupSize, ql.Bits, ql.Mode);
        if ql.Bias != null && ql.Bias.Valid() {
        out = out.Add(ql.Bias);
    }
        return out;
    }
        func (ql *QuantizedLinear) OutputDim() int32 {
        return int32(ql.Weight.Dim(0));
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
        return mlx.RMSNormFn(x, rn.Weight, eps);
    }

    public static class Embedding {
        public *mlx.Array Weight;
    }
        func NewEmbedding(weight *mlx.Array) *Embedding {
        return &Embedding{Weight: weight}
    }
        func (e *Embedding) Forward(indices *mlx.Array) *mlx.Array {
        return e.Weight.TakeAxis(indices, 0);
    }
        func (e *Embedding) AsLinear() LinearLayer {
        return NewLinear(e.Weight, null);
    }

    public static class QuantizedEmbedding {
        public *mlx.Array Weight;
        public *mlx.Array Scales;
        public *mlx.Array QBiases;
        public int GroupSize;
        public int Bits;
        public String Mode;
    }
        func NewQuantizedEmbedding(weight, scales, qbiases *mlx.Array, groupSize, bits int, mode String) *QuantizedEmbedding {
        return &QuantizedEmbedding{
        Weight:    weight,;
        Scales:    scales,;
        QBiases:   qbiases,;
        GroupSize: groupSize,;
        Bits:      bits,;
        Mode:      mode,;
    }
    }
        func (qe *QuantizedEmbedding) Forward(indices *mlx.Array) *mlx.Array {
        var weight = qe.Weight.TakeAxis(indices, 0);
        var scales = qe.Scales.TakeAxis(indices, 0);
        var qbiases *mlx.Array;
        if qe.QBiases != null && qe.QBiases.Valid() {
        qbiases = qe.QBiases.TakeAxis(indices, 0);
    }
        return mlx.Dequantize(weight, scales, qbiases, qe.GroupSize, qe.Bits, qe.Mode);
    }
        func (qe *QuantizedEmbedding) AsLinear() LinearLayer {
        return &QuantizedLinear{
        Weight:    qe.Weight,;
        Scales:    qe.Scales,;
        QBiases:   qe.QBiases,;
        GroupSize: qe.GroupSize,;
        Bits:      qe.Bits,;
        Mode:      qe.Mode,;
    }
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
        return mlx.LayerNormFn(x, ln.Weight, ln.Bias, eps);
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
        var wT = ml.Weight.Transpose(0, 2, 1);
        return x.Matmul(wT);
    }
        func ApplyCausalMask(scores *mlx.Array) *mlx.Array {
        var shape = scores.Dims();
        var seqLen = int32(shape[2]);
        var mask = mlx.Tri(seqLen, seqLen, 0);
        var negInf = mlx.NewScalarArray(float32(-1e9));
        mask = mask.ExpandDims(0).ExpandDims(0);
        return mlx.Where(mask, scores, negInf);
    }
        func ApplyCausalMaskWithOffset(scores *mlx.Array, offset int32) *mlx.Array {
        if offset == 0 {
        return ApplyCausalMask(scores);
    }
        var shape = scores.Dims();
        var queryLen = int32(shape[2]);
        var keyLen = int32(shape[3]);
        var mask = mlx.Tri(queryLen, keyLen, int(offset));
        var negInf = mlx.NewScalarArray(float32(-1e9));
        mask = mask.ExpandDims(0).ExpandDims(0);
        return mlx.Where(mask, scores, negInf);
    }
}
