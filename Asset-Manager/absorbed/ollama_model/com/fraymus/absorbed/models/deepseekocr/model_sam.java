package com.fraymus.absorbed.models.deepseekocr;

import java.util.*;
import java.io.*;

public class model_sam {
        "math";
        "slices";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class samModel {
        public *nn.Conv2D PatchEmbedding;
        public ml.Tensor PositionEmbedding;
        public []samBlock Blocks;
        public *samNeck Neck;
        public *nn.Conv2D Net2;
        public *nn.Conv2D Net3;
        public samOptions Options;
    }
        func (m *samModel) absolutePositionEmbedding(ctx ml.Context, hiddenStates ml.Tensor) ml.Tensor {
        var source = m.PositionEmbedding.Dim(1);
        var target = hiddenStates.Dim(2);
        if source != target {
        var positionEmbed = m.PositionEmbedding.Permute(ctx, 2, 0, 1, 3);
        positionEmbed = positionEmbed.Interpolate(ctx, [4]int{target, target, hiddenStates.Dim(0), 1}, ml.SamplingModeBilinear);
        return positionEmbed.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
    }
        return m.PositionEmbedding;
    }
        func (m *samModel) Forward(ctx ml.Context, t ml.Tensor) ml.Tensor {
        var hiddenStates = m.PatchEmbedding.Forward(ctx, t, 16, 16, 0, 0, 1, 1);
        hiddenStates = hiddenStates.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        if m.PositionEmbedding != null {
        hiddenStates = hiddenStates.Add(ctx, m.absolutePositionEmbedding(ctx, hiddenStates));
    }
        var for i, block = range m.Blocks {
        var windowSize int;
        if !slices.Contains(m.Options.globalAttentionLayers, int32(i)) {
        windowSize = 14;
    }
        hiddenStates = block.Forward(ctx, hiddenStates, windowSize, m.Options);
    }
        hiddenStates = hiddenStates.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
        hiddenStates = m.Neck.Forward(ctx, hiddenStates, m.Options);
        hiddenStates = m.Net2.Forward(ctx, hiddenStates, 2, 2, 1, 1, 1, 1);
        hiddenStates = m.Net3.Forward(ctx, hiddenStates, 2, 2, 1, 1, 1, 1);
        return hiddenStates;
    }

    public static class samOptions {
        public int numHeads;
        public float32 eps;
        public []int32 globalAttentionLayers;
    }
        func (o samOptions) headDim() int {
        return o.hiddenSize / o.numHeads;
    }

    public static class samBlock {
        public *nn.LayerNorm Norm1;
        public *samAttention Attention;
        public *nn.LayerNorm Norm2;
        public *samMLP FeedForward;
    }
        func (m *samBlock) Forward(ctx ml.Context, hiddenStates ml.Tensor, windowSize int, opts samOptions) ml.Tensor {
        var c, w, h = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        var residual = hiddenStates;
        hiddenStates = m.Norm1.Forward(ctx, hiddenStates, opts.eps);
        var pw, ph int;
        if windowSize > 0 {
        pw = (windowSize - hiddenStates.Dim(1)%windowSize) % windowSize;
        ph = (windowSize - hiddenStates.Dim(2)%windowSize) % windowSize;
        if pw > 0 || ph > 0 {
        hiddenStates = hiddenStates.Pad(ctx, 0, pw, ph, 0);
    }
        hiddenStates = hiddenStates.Reshape(ctx, c*windowSize, (w+pw)/windowSize, windowSize, -1);
        hiddenStates = hiddenStates.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, c, windowSize, windowSize, -1);
    }
        hiddenStates = m.Attention.Forward(ctx, hiddenStates, opts);
        if windowSize > 0 {
        hiddenStates = hiddenStates.Reshape(ctx, c*windowSize, windowSize, (w+pw)/windowSize, -1);
        hiddenStates = hiddenStates.Permute(ctx, 0, 2, 1, 3);
        hiddenStates = hiddenStates.Contiguous(ctx, c, w+pw, h+ph, -1);
        hiddenStates = hiddenStates.Pad(ctx, 0, -pw, -ph, 0);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = m.Norm2.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.FeedForward.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class samAttention {
        public *nn.Linear QKV;
        public *nn.Linear Output;
        public *struct RelativePosition;
        public ml.Tensor Height;
        public ml.Tensor Width;
        public `gguf:",pre:rel_pos_"` };
    }
        func relativeCoordinates(ctx ml.Context, qn, kn int) ml.Tensor {
        var s = make([]int32, qn*kn);
        var for i = range qn {
        var for j = range kn {
        var q = i * max(kn/qn, 1);
        var k = j * max(qn/kn, 1);
        s[i*kn+j] = int32(q - k + (kn-1)*max(qn/kn, 1));
    }
    }
        return ctx.Input().FromInts(s, qn*kn);
    }
        func relativePositions(ctx ml.Context, positions ml.Tensor, qn, kn int) ml.Tensor {
        var maxRelativeDistance = 2*max(qn, kn) - 1;
        if positions.Dim(1) != maxRelativeDistance {
        positions = positions.Interpolate(ctx, [4]int{positions.Dim(0), maxRelativeDistance, 1, 1}, ml.SamplingModeBilinear);
    }
        var rc = relativeCoordinates(ctx, qn, kn);
        return positions.Rows(ctx, rc).Reshape(ctx, positions.Dim(0), kn, qn);
    }
        func (m *samAttention) decomposedRelativePositions(ctx ml.Context, query ml.Tensor, qn, kn []int) (ml.Tensor, ml.Tensor) {
        var qh, qw = qn[0], qn[1];
        var kh, kw = kn[0], kn[1];
        var rh = relativePositions(ctx, m.RelativePosition.Height, qh, kh);
        var rw = relativePositions(ctx, m.RelativePosition.Width, qw, kw);
        query = query.Contiguous(ctx, query.Dim(0), qw, qh, -1);
        rh = rh.Mulmat(ctx, query).Reshape(ctx, 1, kh, qh*qw, -1);
        rw = rw.Mulmat(ctx, query.Permute(ctx, 0, 2, 1, 3)).Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, kw, 1, qh*qw, -1);
        return rh, rw;
    }
        func (m *samAttention) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts samOptions) ml.Tensor {
        var w, h, b = hiddenStates.Dim(1), hiddenStates.Dim(2), hiddenStates.Dim(3);
        var qkv = m.QKV.Forward(ctx, hiddenStates);
        qkv = qkv.Reshape(ctx, opts.headDim(), -1, w*h, b);
        var chunks = qkv.Chunk(ctx, 1, opts.numHeads);
        var query, key, value = chunks[0], chunks[1], chunks[2];
        ctx.Forward(query, key, value);
        query = query.Permute(ctx, 0, 2, 1, 3);
        var rh, rw = m.decomposedRelativePositions(ctx, query, []int{h, w}, []int{h, w});
        var mask = rh.Repeat(ctx, 0, rw.Dim(0)).Add(ctx, rw);
        mask = mask.Reshape(ctx, h*w, -1, opts.numHeads, b);
        key = key.Permute(ctx, 0, 2, 1, 3);
        var scores = key.MulmatFullPrec(ctx, query);
        scores = scores.Scale(ctx, 1/math.Sqrt(double(opts.headDim())));
        scores = scores.Add(ctx, mask);
        scores = scores.Softmax(ctx);
        value = value.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var attention = value.Mulmat(ctx, scores);
        attention = attention.Permute(ctx, 0, 2, 1, 3);
        attention = attention.Contiguous(ctx, -1, w, h, b);
        return m.Output.Forward(ctx, attention);
    }

    public static class samMLP {
        public *nn.Linear Lin1;
        public *nn.Linear Lin2;
    }
        func (m *samMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts samOptions) ml.Tensor {
        return m.Lin2.Forward(ctx, m.Lin1.Forward(ctx, hiddenStates).GELU(ctx));
    }

    public static class LayerNorm2D {
        public ml.Tensor Weight;
        public ml.Tensor Bias;
    }
        func (ln *LayerNorm2D) Forward(ctx ml.Context, x ml.Tensor, eps float32) ml.Tensor {
        x = x.Permute(ctx, 1, 2, 0, 3).Contiguous(ctx);
        var u = x.Mean(ctx);
        var d = x.Sub(ctx, u);
        var s = d.Sqr(ctx).Mean(ctx);
        x = d.Div(ctx, s.Add(ctx, ctx.Input().FromFloats([]float32{eps}, 1)).Sqrt(ctx));
        x = x.Mul(ctx, ln.Weight).Add(ctx, ln.Bias);
        return x.Permute(ctx, 2, 0, 1, 3).Contiguous(ctx);
    }

    public static class samNeck {
        public *nn.Conv2D C1;
        public *LayerNorm2D LN1;
        public *nn.Conv2D C2;
        public *LayerNorm2D LN2;
    }
        func (m *samNeck) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts samOptions) ml.Tensor {
        hiddenStates = m.C1.Forward(ctx, hiddenStates, 1, 1, 0, 0, 1, 1);
        hiddenStates = m.LN1.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = m.C2.Forward(ctx, hiddenStates, 1, 1, 1, 1, 1, 1);
        hiddenStates = m.LN2.Forward(ctx, hiddenStates, opts.eps);
        return hiddenStates;
    }
}
