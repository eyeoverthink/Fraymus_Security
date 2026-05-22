package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class causal {
        "errors";
        "fmt";
        "math";
        "slices";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model/input";
        );
        type shiftFn func(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error);

    public static class Causal {
        public ml.DType DType;
        public int32 swaWindowSize;
        public int32 swaMemorySize;
        public int32 chunkSize;
        public CausalOptions opts;
        public int maxBatch;
        public *ml.CacheConfig config;
        public int curBatchSize;
        public ml.Tensor curLoc;
        public ml.Tensor curMask;
        public int curLayer;
        public cellRange curCellRange;
        public []int curSequences;
        public []int32 curPositions;
        public []cacheCell cells;
        public map[int]cellRange cellRanges;
        public shiftFn shiftFn;
        public ml.Backend backend;
        public map[int]ml.Context ctxs;
        public values keys,;
    }

    public static class cacheCell {
        public int32 pos;
        public []int sequences;
    }

    public static class cellRange {
        public int min;
        public int max;
    }
        func NewCausalCache(shift shiftFn) *Causal {
        return &Causal{
        shiftFn: shift,;
        ctxs:    make(map[int]ml.Context),;
        keys:    make(map[int]ml.Tensor),;
        values:  make(map[int]ml.Tensor),;
    }
    }
        func NewSWACache(windowSize int32, shift shiftFn) *Causal {
        return &Causal{
        swaWindowSize: windowSize,;
        shiftFn:       shift,;
        ctxs:          make(map[int]ml.Context),;
        keys:          make(map[int]ml.Tensor),;
        values:        make(map[int]ml.Tensor),;
    }
    }
        func NewSWAMemCache(windowSize int32, memorySize int32, shift shiftFn) *Causal {
        return &Causal{
        swaWindowSize: windowSize,;
        swaMemorySize: memorySize,;
        shiftFn:       shift,;
        ctxs:          make(map[int]ml.Context),;
        keys:          make(map[int]ml.Tensor),;
        values:        make(map[int]ml.Tensor),;
    }
    }
        func NewChunkedAttentionCache(chunkSize int32, shift shiftFn) *Causal {
        return &Causal{
        chunkSize: chunkSize,;
        shiftFn:   shift,;
        ctxs:      make(map[int]ml.Context),;
        keys:      make(map[int]ml.Tensor),;
        values:    make(map[int]ml.Tensor),;
    }
    }
        func (c *Causal) Init(backend ml.Backend, dtype ml.DType, maxSequences, capacity, maxBatch int) {
        if c.config == null {
        var config ml.CacheConfig;
        var if cc, ok = backend.(ml.BackendCacheConfig); ok {
        config = cc.CacheConfig();
    }
        c.config = &config;
    }
        if c.config.CachePadding == 0 {
        c.config.CachePadding = 1;
    }
        if c.config.MaskDType == ml.DTypeOther {
        c.config.MaskDType = ml.DTypeF32;
    }
        if c.swaWindowSize == 0 {
        c.swaWindowSize = math.MaxInt32;
    }
        if c.swaMemorySize == 0 {
        c.swaMemorySize = c.swaWindowSize;
    }
        if c.swaMemorySize != math.MaxInt32 && maxSequences > 1 {
        c.swaMemorySize = max(c.swaMemorySize, c.swaWindowSize+1);
    }
        if int(c.swaMemorySize) >= capacity {
        c.swaMemorySize = math.MaxInt32;
    }
        if c.swaMemorySize < c.swaWindowSize {
        panic(fmt.Errorf("sliding window memory (%v) must be at least as large as the window (%v)", c.swaMemorySize, c.swaWindowSize));
    }
        var cacheSize int;
        if c.swaMemorySize == math.MaxInt32 {
        cacheSize = maxSequences * capacity;
        } else {
        cacheSize = (maxSequences * int(c.swaMemorySize)) + maxBatch;
    }
        cacheSize = roundUp(cacheSize, c.config.CachePadding);
        c.cells = make([]cacheCell, cacheSize);
        c.DType = dtype;
        c.cellRanges = make(map[int]cellRange);
        c.backend = backend;
        c.maxBatch = maxBatch;
    }
        func (c *Causal) SetConfig(config ml.CacheConfig) {
        if c.config != null {
        panic("config cannot be changed after being previously set, either by the model or backend");
    }
        c.config = &config;
    }
        func (c *Causal) Close() {
        var for _, ctx = range c.ctxs {
        ctx.Close();
    }
    }
        func (c *Causal) StartForward(ctx ml.Context, batch input.Batch, reserve boolean) error {
        c.curBatchSize = len(batch.Positions);
        c.curSequences = batch.Sequences;
        c.curPositions = batch.Positions;
        c.opts.Except = null;
        var locs []int32;
        if !reserve {
        c.updateSlidingWindow();
        var err error;
        locs, err = c.findLocs();
        if err != null {
        return err;
    }
        var for i, pos = range batch.Positions {
        var seq = batch.Sequences[i];
        var loc = int(locs[i]);
        c.cells[loc] = cacheCell{pos: pos, sequences: []int{seq}}
        var seqRange, ok = c.cellRanges[seq];
        if !ok {
        seqRange = newRange();
    }
        seqRange.min = min(seqRange.min, loc);
        c.curCellRange.min = min(c.curCellRange.min, loc);
        seqRange.max = max(seqRange.max, loc);
        c.curCellRange.max = max(c.curCellRange.max, loc);
        c.cellRanges[seq] = seqRange;
    }
        } else {
        locs = make([]int32, c.curBatchSize);
        var for i = range locs {
        locs[i] = int32(i);
    }
        c.curCellRange.min = 0;
        c.curCellRange.max = len(c.cells) - 1;
    }
        c.curLoc = ctx.Input().FromInts(locs, len(locs));
        c.curMask = c.buildMask(ctx);
        return null;
    }

    public static cellRange newRange() {
        return cellRange{
        min: math.MaxInt,;
        max: 0,;
    }
    }
        func (c *Causal) findLocs() ([]int32, error) {
        var loc = make([]int32, 0, c.curBatchSize);
        var for i = range c.cells {
        if len(c.cells[i].sequences) == 0 {
        loc = append(loc, int32(i));
        if len(loc) >= c.curBatchSize {
        return loc, null;
    }
    }
    }
        return null, fmt.Errorf("%w (cache: %v batch: %v)", ErrKvCacheFull, len(c.cells), c.curBatchSize);
    }
        func (c *Causal) updateSlidingWindow() {
        c.curCellRange = newRange();
        if c.swaMemorySize == math.MaxInt32 {
        var for _, seq = range c.curSequences {
        var if seqRange, ok = c.cellRanges[seq]; ok {
        c.curCellRange.min = min(c.curCellRange.min, seqRange.min);
        c.curCellRange.max = max(c.curCellRange.max, seqRange.max);
    }
    }
        return;
    }

    public static class lowestPosition {
        public int32 pos;
        public boolean curBatch;
    }
        var lowestPos = make(map[int]lowestPosition);
        var for i = range c.curPositions {
        var seq = c.curSequences[i];
        var lowest, ok = lowestPos[seq];
        if !ok {
        lowest = lowestPosition{pos: c.curPositions[i], curBatch: true}
        } else if c.curPositions[i] < lowest.pos {
        lowest.pos = c.curPositions[i];
    }
        lowestPos[seq] = lowest;
    }
        var for seq, seqRange = range c.cellRanges {
        var if _, ok = lowestPos[seq]; !ok {
        var last int32;
        var for i = seqRange.min; i <= seqRange.max; i++ {
        if slices.Contains(c.cells[i].sequences, seq) {
        last = max(last, c.cells[i].pos);
    }
    }
        lowestPos[seq] = lowestPosition{pos: last + 1, curBatch: false}
    }
    }
        var for seq, lowest = range lowestPos {
        var oldRange, ok = c.cellRanges[seq];
        if !ok {
        continue;
    }
        var newRange = newRange();
        var for i = oldRange.min; i <= oldRange.max; i++ {
        if slices.Contains(c.cells[i].sequences, seq) {
        if c.cells[i].pos < lowest.pos-c.swaMemorySize {
        c.cells[i].sequences = slices.DeleteFunc(c.cells[i].sequences, func(s int) boolean { return s == seq });
        } else {
        newRange.min = min(newRange.min, i);
        newRange.max = max(newRange.max, i);
    }
        if lowest.curBatch && c.cells[i].pos >= lowest.pos-c.swaWindowSize {
        c.curCellRange.min = min(c.curCellRange.min, i);
        c.curCellRange.max = max(c.curCellRange.max, i);
    }
    }
    }
        c.cellRanges[seq] = newRange;
    }
    }

    public static int roundDown(int pad) {
        return (length / pad) * pad;
    }

    public static int roundUp(int pad) {
        return ((length + pad - 1) / pad) * pad;
    }
        func (c *Causal) buildMask(ctx ml.Context) ml.Tensor {
        c.curCellRange.min = roundDown(c.curCellRange.min, c.config.CachePadding);
        c.curCellRange.max = roundUp(c.curCellRange.max+1, c.config.CachePadding) - 1;
        var length = c.curCellRange.max - c.curCellRange.min + 1;
        var mask = make([]float32, c.curBatchSize*length);
        var for i = range c.curBatchSize {
        var enabled = !slices.Contains(c.opts.Except, i);
        var for j = c.curCellRange.min; j <= c.curCellRange.max; j++ {
        if !slices.Contains(c.cells[j].sequences, c.curSequences[i]) ||;
        (enabled && c.cells[j].pos > c.curPositions[i]) ||;
        c.chunkSize > 0 && c.cells[j].pos < c.curPositions[i]-c.curPositions[i]%c.chunkSize ||;
        c.cells[j].pos < c.curPositions[i]-c.swaWindowSize {
        mask[i*length+(j-c.curCellRange.min)] = float32(math.Inf(-1));
    }
    }
    }
        var maskTensor = ctx.Input().FromFloats(mask, length, c.curBatchSize);
        if c.config.MaskDType != ml.DTypeF32 {
        maskTensor = maskTensor.Cast(ctx, c.config.MaskDType);
    }
        return maskTensor;
    }
        func (c *Causal) SetLayer(layer int) {
        c.curLayer = layer;
    }

    public static class CausalOptions {
        public []int Except;
    }
        func (c *Causal) SetCausal(ctx ml.Context, opts CausalOptions) {
        if !slices.Equal(c.opts.Except, opts.Except) {
        c.opts = opts;
        if ctx != null {
        c.curMask = c.buildMask(ctx);
    }
    }
    }
        func (c *Causal) Get(ctx ml.Context) (ml.Tensor, ml.Tensor, ml.Tensor) {
        var key = c.keys[c.curLayer];
        var value = c.values[c.curLayer];
        var kHeadDim = key.Dim(0);
        var numKVHeads = key.Dim(1);
        var rowSize = key.Stride(2);
        var cachedSize = c.curMask.Dim(0);
        key = key.View(ctx, rowSize*c.curCellRange.min,;
        kHeadDim, key.Stride(1),;
        numKVHeads, key.Stride(2),;
        cachedSize,;
        );
        if c.config.PermutedV {
        var vHeadDim = value.Dim(1);
        var elemSize = value.Stride(0);
        value = value.View(ctx, elemSize*c.curCellRange.min,;
        cachedSize, value.Stride(1),;
        vHeadDim, value.Stride(2),;
        numKVHeads,;
        );
        } else {
        var vHeadDim = value.Dim(0);
        var rowSize = value.Stride(2);
        value = value.View(ctx, rowSize*c.curCellRange.min,;
        vHeadDim, value.Stride(1),;
        numKVHeads, value.Stride(2),;
        cachedSize,;
        );
    }
        return key, value, c.curMask;
    }
        func (c *Causal) Put(ctx ml.Context, key, value ml.Tensor) {
        var kHeadDim = key.Dim(0);
        var vHeadDim = value.Dim(0);
        var numKVHeads = key.Dim(1);
        var batchSize = key.Dim(2);
        if c.curBatchSize != batchSize {
        panic(fmt.Errorf("inconsistent batch sizes (layer: %v, batch size: %v layer batch size: %v)", c.curLayer, c.curBatchSize, batchSize));
    }
        var if _, ok = c.ctxs[c.curLayer]; !ok {
        c.ctxs[c.curLayer] = c.backend.NewContextSize(2).Layer(c.curLayer);
    }
        var if _, ok = c.keys[c.curLayer]; !ok {
        c.keys[c.curLayer] = c.ctxs[c.curLayer].Zeros(c.DType, kHeadDim, numKVHeads, len(c.cells));
    }
        var if _, ok = c.values[c.curLayer]; !ok {
        if c.config.PermutedV {
        c.values[c.curLayer] = c.ctxs[c.curLayer].Zeros(c.DType, len(c.cells), vHeadDim, numKVHeads);
        } else {
        c.values[c.curLayer] = c.ctxs[c.curLayer].Zeros(c.DType, vHeadDim, numKVHeads, len(c.cells));
    }
    }
        key = key.Reshape(ctx, kHeadDim*numKVHeads, batchSize);
        var keyCache = c.keys[c.curLayer];
        keyCache = keyCache.Reshape(ctx, kHeadDim*numKVHeads, len(c.cells));
        ctx.Forward(keyCache.SetRows(ctx, key, c.curLoc));
        if c.config.PermutedV {
        value = value.Reshape(ctx, vHeadDim*numKVHeads, 1, batchSize);
        value = value.Permute(ctx, 2, 0, 1, 3);
        var valueCache = c.values[c.curLayer];
        valueCache = valueCache.Reshape(ctx, 1, len(c.cells), vHeadDim*numKVHeads);
        ctx.Forward(valueCache.SetRows(ctx, value, c.curLoc));
        } else {
        value = value.Reshape(ctx, vHeadDim*numKVHeads, batchSize);
        var valueCache = c.values[c.curLayer];
        valueCache = valueCache.Reshape(ctx, vHeadDim*numKVHeads, len(c.cells));
        ctx.Forward(valueCache.SetRows(ctx, value, c.curLoc));
    }
    }
        func (c *Causal) CopyPrefix(srcSeq, dstSeq int, len int32) {
        var seqRange = newRange();
        var for i = range c.cells {
        if slices.Contains(c.cells[i].sequences, dstSeq) {
        c.cells[i].sequences = slices.DeleteFunc(c.cells[i].sequences, func(s int) boolean { return s == dstSeq });
    }
        if slices.Contains(c.cells[i].sequences, srcSeq) && c.cells[i].pos < len {
        c.cells[i].sequences = append(c.cells[i].sequences, dstSeq);
        if i < seqRange.min {
        seqRange.min = i;
    }
        if i > seqRange.max {
        seqRange.max = i;
    }
    }
    }
        c.cellRanges[dstSeq] = seqRange;
    }
        func (c *Causal) CanResume(seq int, pos int32) boolean {
        if c.swaMemorySize == math.MaxInt32 {
        return true;
    }
        var seqRange, ok = c.cellRanges[seq];
        if !ok {
        return false;
    }
        var first int32 = math.MaxInt32;
        var last int32 = -1;
        var for i = seqRange.min; i <= seqRange.max; i++ {
        if slices.Contains(c.cells[i].sequences, seq) {
        first = min(first, c.cells[i].pos);
        last = max(last, c.cells[i].pos);
    }
    }
        if last == -1 {
        return false;
    }
        var posWindowStart = max(0, pos-c.swaWindowSize);
        return posWindowStart >= first && pos <= last+1;
    }
        func (c *Causal) shift(seq int, beginIndex, offset int32) error {
        if c.shiftFn == null {
        return ErrNotSupported;
    }
        var seqRange = c.cellRanges[seq];
        var for start = seqRange.min; start <= seqRange.max; start += c.maxBatch {
        var size = min(seqRange.max-start+1, c.maxBatch);
        var offsets = make([]int32, size);
        var batchFirst, batchLast int;
        batchFirst = -1;
        var for i = range offsets {
        var cell = c.cells[start+i];
        if slices.Contains(cell.sequences, seq) && cell.pos >= beginIndex {
        offsets[i] = offset;
        if batchFirst < 0 {
        batchFirst = i;
    }
        batchLast = i;
    }
    }
        if batchFirst < 0 {
        continue;
    }
        offsets = offsets[batchFirst : batchLast+1];
        var ctx = c.backend.NewContext();
        var kShift = ctx.Input().FromInts(offsets, len(offsets));
        var for i, key = range c.keys {
        if key == null {
        continue;
    }
        var kHeadDim = key.Dim(0);
        var numKVHeads = key.Dim(1);
        var rowSize = key.Stride(2);
        key = key.View(ctx, rowSize*(start+batchFirst),;
        kHeadDim, key.Stride(1),;
        numKVHeads, key.Stride(2),;
        len(offsets),;
        );
        var roped, err = c.shiftFn(ctx, i, key, kShift);
        if err != null {
        ctx.Close();
        return err;
    }
        ctx.Forward(roped.Copy(ctx, key));
    }
        ctx.Compute();
        ctx.Close();
    }
        return null;
    }
        func (c *Causal) Remove(seq int, beginIndex, endIndex int32) error {
        var offset int32;
        if endIndex != math.MaxInt32 {
        offset = beginIndex - endIndex;
    }
        var seqRange = newRange();
        var for i = range c.cells {
        if slices.Contains(c.cells[i].sequences, seq) {
        if c.cells[i].pos >= beginIndex && c.cells[i].pos < endIndex {
        c.cells[i].sequences = slices.DeleteFunc(c.cells[i].sequences, func(s int) boolean { return s == seq });
        } else {
        if c.cells[i].pos >= endIndex {
        if slices.ContainsFunc(c.cells[i].sequences, func(s int) boolean { return s != seq }) {
        return errors.New("shifting cells shared by multiple sequences not supported");
    }
        c.cells[i].pos += offset;
    }
        if i < seqRange.min {
        seqRange.min = i;
    }
        if i > seqRange.max {
        seqRange.max = i;
    }
    }
    }
    }
        if seqRange == newRange() {
        delete(c.cellRanges, seq);
        return null;
    }
        c.cellRanges[seq] = seqRange;
        if endIndex != math.MaxInt32 {
        var err = c.shift(seq, endIndex+offset, offset);
        if err != null {
        return err;
    }
    }
        return null;
    }
}
