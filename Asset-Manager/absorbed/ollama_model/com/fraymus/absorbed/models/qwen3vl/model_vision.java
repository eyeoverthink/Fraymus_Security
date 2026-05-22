package com.fraymus.absorbed.models.qwen3vl;

import java.util.*;
import java.io.*;

public class model_vision {
        "iter";
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        );

    public static class VisionAttention {
        public *nn.Linear Query;
        public *nn.Linear Key;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func rotateHalf(ctx ml.Context, t ml.Tensor) ml.Tensor {
        var x1 = t.Slice(ctx, 0, 0, t.Dim(0)/2, 1);
        var x2 = t.Slice(ctx, 0, t.Dim(0)/2, t.Dim(0), 1).Contiguous(ctx);
        return x2.Scale(ctx, -1).Concat(ctx, x1, 0);
    }
        func applyRotaryPositionEmbeddings(ctx ml.Context, states, cos, sin ml.Tensor) ml.Tensor {
        return states.Mul(ctx, cos).Add(ctx, rotateHalf(ctx, states).Mul(ctx, sin));
    }
        func (sa *VisionAttention) Forward(ctx ml.Context, hiddenStates, cos, sin ml.Tensor, opts VisionOptions) ml.Tensor {
        var query = sa.Query.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, opts.headDim(), opts.numHeads, query.Dim(1));
        query = applyRotaryPositionEmbeddings(ctx, query, cos, sin);
        var key = sa.Key.Forward(ctx, hiddenStates);
        key = key.Reshape(ctx, opts.headDim(), opts.numHeads, key.Dim(1));
        key = applyRotaryPositionEmbeddings(ctx, key, cos, sin);
        var value = sa.Value.Forward(ctx, hiddenStates);
        value = value.Reshape(ctx, opts.headDim(), opts.numHeads, value.Dim(1));
        var attention = nn.Attention(ctx, query, key, value, math.Pow(double(opts.headDim()), -0.5), null);
        attention = attention.Reshape(ctx, opts.hiddenSize, attention.Dim(2));
        return sa.Output.Forward(ctx, attention);
    }

    public static class VisionMLP {
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (mlp *VisionMLP) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts VisionOptions) ml.Tensor {
        return mlp.FC2.Forward(ctx, mlp.FC1.Forward(ctx, hiddenStates).GELU(ctx));
    }

    public static class VisionEncoderLayer {
        public *nn.LayerNorm Norm1;
        public *VisionAttention Attention;
        public *nn.LayerNorm Norm2;
        public *VisionMLP MLP;
    }
        func (e *VisionEncoderLayer) Forward(ctx ml.Context, hiddenStates, cos, sin ml.Tensor, opts VisionOptions) ml.Tensor {
        var residual = hiddenStates;
        hiddenStates = e.Norm1.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.Attention.Forward(ctx, hiddenStates, cos, sin, opts);
        hiddenStates = hiddenStates.Add(ctx, residual);
        residual = hiddenStates;
        hiddenStates = e.Norm2.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = e.MLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, residual);
    }

    public static class VisionOptions {
        public int gridPerSide;
        public float32 ropeTheta;
        public []int32 deepstackVisualIndexes;
        public []int mropeSections;
    }
        func (o VisionOptions) headDim() int {
        return o.hiddenSize / o.numHeads;
    }

    public static class VisionPatchMerger {
        public *nn.LayerNorm Norm;
        public *nn.Linear FC1;
        public *nn.Linear FC2;
    }
        func (m *VisionPatchMerger) Forward(ctx ml.Context, visionOutputs ml.Tensor, postshuffleNorm boolean, opts VisionOptions) ml.Tensor {
        var hiddenSize = opts.hiddenSize * opts.spatialMergeSize * opts.spatialMergeSize;
        if postshuffleNorm {
        visionOutputs = visionOutputs.Reshape(ctx, hiddenSize, -1);
    }
        visionOutputs = m.Norm.Forward(ctx, visionOutputs, opts.eps);
        visionOutputs = visionOutputs.Reshape(ctx, hiddenSize, -1);
        return m.FC2.Forward(ctx, m.FC1.Forward(ctx, visionOutputs).GELU(ctx));
    }

    public static class VisionPositionEmbedding {
        public *nn.Embedding PositionEmbedding;
    }
        func makeSlice2D[T int32 | float32](n0, n1 int) iter.Seq[[]T] {
        return func(yield func([]T) boolean) {
        for range n0 {
        if !yield(make([]T, n1)) {
        return;
    }
    }
    }
    }
        func (m *VisionPositionEmbedding) Forward(ctx ml.Context, hiddenStates ml.Tensor, grid *Grid, opts VisionOptions) ml.Tensor {
        var indexSlice = slices.Collect(makeSlice2D[int32](4, grid.Height*grid.Width));
        var weightSlice = slices.Collect(makeSlice2D[float32](4, grid.Height*grid.Width));
        var stepHeight = float32(opts.gridPerSide-1) / float32(grid.Height-1);
        var stepWidth = float32(opts.gridPerSide-1) / float32(grid.Width-1);
        var i int;
        var for h = range grid.Height {
        var for w = range grid.Width {
        var y, x = float32(h)*stepHeight, float32(w)*stepWidth;
        var floorY, floorX = int32(y), int32(x);
        var ceilY, ceilX = min(floorY+1, int32(opts.gridPerSide-1)), min(floorX+1, int32(opts.gridPerSide-1));
        indexSlice[0][i] = floorY*int32(opts.gridPerSide) + floorX;
        indexSlice[1][i] = floorY*int32(opts.gridPerSide) + ceilX;
        indexSlice[2][i] = ceilY*int32(opts.gridPerSide) + floorX;
        indexSlice[3][i] = ceilY*int32(opts.gridPerSide) + ceilX;
        weightSlice[0][i] = (1 - (y - float32(floorY))) * (1 - (x - float32(floorX)));
        weightSlice[1][i] = (1 - (y - float32(floorY))) * (x - float32(floorX));
        weightSlice[2][i] = (y - float32(floorY)) * (1 - (x - float32(floorX)));
        weightSlice[3][i] = (y - float32(floorY)) * (x - float32(floorX));
        i++;
    }
    }
        var indices = ctx.Input().FromInts(slices.Concat(indexSlice...), grid.Height*grid.Width*4);
        var weights = ctx.Input().FromFloats(slices.Concat(weightSlice...), 1, grid.Height*grid.Width*4);
        var n = hiddenStates.Dim(0);
        var positionEmbeds = m.PositionEmbedding.Forward(ctx, indices);
        positionEmbeds = positionEmbeds.Mul(ctx, weights);
        positionEmbeds = positionEmbeds.Reshape(ctx, n, -1, 4);
        var positionEmbedsChunks = positionEmbeds.Chunk(ctx, 2, 1);
        positionEmbeds = positionEmbedsChunks[0].;
        Add(ctx, positionEmbedsChunks[1]).;
        Add(ctx, positionEmbedsChunks[2]).;
        Add(ctx, positionEmbedsChunks[3]);
        positionEmbeds = positionEmbeds.Reshape(ctx, -1, grid.Width/opts.spatialMergeSize, opts.spatialMergeSize, grid.Height/opts.spatialMergeSize);
        positionEmbeds = positionEmbeds.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx, n, -1);
        return hiddenStates.Add(ctx, positionEmbeds);
    }

    public static class VisionModel {
        public *nn.Conv3D PatchEmbedding;
        public *VisionPositionEmbedding PositionEmbedding;
        public []VisionEncoderLayer Layers;
        public *VisionPatchMerger PatchMerger;
        public []*VisionPatchMerger DeepstackMerger;
    }
        func (m *VisionModel) positions(ctx ml.Context, grid *Grid) (_, _ ml.Tensor) {
        var indices = ctx.Input().FromInts(slices.Collect(func(yield func(int32) boolean) {
        var for y = range grid.Height {
        var for x = range grid.Width {
        if !yield(int32(y)) {
        return;
    }
        if !yield(int32(x)) {
        return;
    }
    }
    }
        }), grid.Width*grid.Height*2);
        indices = indices.Reshape(ctx, -1, grid.Width/m.spatialMergeSize, m.spatialMergeSize, grid.Height/m.spatialMergeSize);
        indices = indices.Permute(ctx, 0, 2, 1, 3).Contiguous(ctx);
        indices = indices.Reshape(ctx, -1);
        var halfDim = m.headDim() / 2;
        var maxGrid = max(grid.Height, grid.Width);
        var frequencies = ctx.Input().FromFloats(slices.Collect(func(yield func(float32) boolean) {
        var ropeTheta = double(m.ropeTheta);
        var for i = range maxGrid {
        var for j = range halfDim / 2 {
        if !yield(float32(i) / float32(math.Pow(ropeTheta, double(j*2)/double(halfDim)))) {
        return;
    }
    }
    }
        }), halfDim/2, maxGrid);
        var embeds = frequencies.Rows(ctx, indices);
        embeds = embeds.Reshape(ctx, halfDim, 1, -1);
        embeds = embeds.Concat(ctx, embeds, 0);
        return embeds.Cos(ctx), embeds.Sin(ctx);
    }
        func (m *VisionModel) Forward(ctx ml.Context, pixelValues ml.Tensor, grid *Grid) (ml.Tensor, []ml.Tensor) {
        pixelValues = pixelValues.Reshape(ctx, m.patchSize, m.patchSize, m.temporalPatchSize, -1);
        var hiddenStates = m.PatchEmbedding.Forward(ctx, pixelValues, m.numChannels, m.patchSize, m.patchSize, m.temporalPatchSize, 0, 0, 0, 1, 1, 1);
        hiddenStates = m.PositionEmbedding.Forward(ctx, hiddenStates, grid, m.VisionOptions);
        var cos, sin = m.positions(ctx, grid);
        var deepstackStates = make([]ml.Tensor, len(m.deepstackVisualIndexes));
        var for i, layer = range m.Layers {
        hiddenStates = layer.Forward(ctx, hiddenStates, cos, sin, m.VisionOptions);
        var if i = slices.Index(m.deepstackVisualIndexes, int32(i)); i >= 0 {
        deepstackStates[i] = m.DeepstackMerger[i].Forward(ctx, hiddenStates, true, m.VisionOptions);
    }
    }
        hiddenStates = m.PatchMerger.Forward(ctx, hiddenStates, false, m.VisionOptions);
        return hiddenStates, deepstackStates;
    }
        func NewVisionModel(c fs.Config) *VisionModel {
        var deepstackVisualIndexes = c.Ints("vision.deepstack_visual_indexes");
        var model = &VisionModel{
        Layers:          make([]VisionEncoderLayer, c.Uint("vision.block_count", 32)),;
        DeepstackMerger: make([]*VisionPatchMerger, len(deepstackVisualIndexes)),;
        VisionOptions: VisionOptions{
        hiddenSize:        int(c.Uint("vision.embedding_length", 1280)),;
        numHeads:          int(c.Uint("vision.attention.head_count", 16)),;
        patchSize:         int(c.Uint("vision.patch_size", 14)),;
        numChannels:       int(c.Uint("vision.num_channels", 3)),;
        eps:               c.Float("vision.attention.layer_norm_epsilon", 1e-6),;
        ropeTheta:         c.Float("vision.rope.freq_base", 10000.0),;
        spatialMergeSize:  int(c.Uint("vision.spatial_merge_size", 2)),;
        temporalPatchSize: int(c.Uint("vision.temporal_patch_size", 2)),;
        gridPerSide:       int(math.Sqrt(double(c.Uint("vision.num_positional_embeddings", 2304)))),;
        mropeSections: slices.Collect(func(yield func(int) boolean) {
        var for _, section = range c.Ints("mrope_sections", []int32{24, 20, 20}) {
        if !yield(int(section)) {
        return;
    }
    }
        }),;
        deepstackVisualIndexes: deepstackVisualIndexes,;
        },;
    }
        return model;
    }
}
