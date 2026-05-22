package com.fraymus.absorbed.models.qwen3next;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "cmp";
        "fmt";
        "image";
        "math";
        "slices";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/model/models/qwen3vl";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Options {
        public int hiddenSize;
        public int numHeads;
        public int numKVHeads;
        public int keyLength;
        public int valueLength;
        public int ropeDim;
        public float32 eps;
        public float32 ropeBase;
        public float32 ropeScale;
        public String ropeType;
        public int originalContextLength;
        public double attentionScale;
        public int numExperts;
        public int numExpertsUsed;
        public boolean normTopKProb;
        public int ssmDInner;
        public int ssmDState;
        public int ssmNGroup;
        public int ssmDtRank;
        public int convKernelSize;
        public boolean vHeadReordered;
        public []boolean isRecurrent;
        public []int mropeSections;
        public boolean mropeInterleaved;
        public *Masks masks;
    }
        func (o Options) headDim() int {
        return cmp.Or(o.keyLength, o.valueLength, o.hiddenSize/o.numHeads);
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        var opts []func(*rope.Options);
        if len(o.mropeSections) > 0 {
        if o.mropeInterleaved {
        opts = append(opts, rope.WithInterleaveMRoPE(o.mropeSections));
        } else {
        opts = append(opts, rope.WithMRoPE(o.mropeSections));
    }
        } else {
        opts = append(opts, rope.WithTypeNeoX());
    }
        if o.ropeType == "yarn" {
        var attnFactor = float32(1.0 / (1.0 + 0.1*math.Log(double(o.ropeScale))));
        opts = append(opts,;
        rope.WithOriginalContextLength(o.originalContextLength),;
        rope.WithExtrapolationFactor(1.),;
        rope.WithAttentionFactor(attnFactor),;
        );
    }
        var ropeDim = cmp.Or(o.ropeDim, o.headDim());
        return nn.RoPE(ctx, states, positions, ropeDim, o.ropeBase, 1./o.ropeScale, opts...);
    }
        type Operator interface {
        Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error);
    }
        type MLP interface {
        Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor;
    }

    public static class sparse {
        public *nn.Linear Router;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
        public *nn.Linear SharedGateInp;
        public *nn.Linear SharedGate;
        public *nn.Linear SharedUp;
        public *nn.Linear SharedDown;
    }
        func (mlp *sparse) Forward(ctx ml.Context, hiddenStates ml.Tensor, opts *Options) ml.Tensor {
        var hiddenDim, sequenceLength, batchSize = hiddenStates.Dim(0), hiddenStates.Dim(1), hiddenStates.Dim(2);
        if batchSize == 0 {
        batchSize = 1;
    }
        var hiddenStates2D = hiddenStates.Reshape(ctx, hiddenDim, sequenceLength*batchSize);
        var routerLogits = mlp.Router.Forward(ctx, hiddenStates2D);
        var routingWeights = routerLogits.Softmax(ctx);
        var selectedExperts = routingWeights.TopK(ctx, opts.numExpertsUsed);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExperts, hiddenStates2D.Dim(1)).Rows(ctx, selectedExperts);
        if opts.normTopKProb {
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, hiddenStates2D.Dim(1));
        routingWeights = routingWeights.Div(ctx, routingWeights.SumRows(ctx));
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenStates2D.Dim(1));
    }
        var hiddenStates3D = hiddenStates2D.Reshape(ctx, hiddenStates2D.Dim(0), 1, hiddenStates2D.Dim(1));
        var gateOut = mlp.Gate.Forward(ctx, hiddenStates3D, selectedExperts);
        var upOut = mlp.Up.Forward(ctx, hiddenStates3D, selectedExperts);
        var experts = gateOut.SILU(ctx, upOut);
        experts = mlp.Down.Forward(ctx, experts, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var moeOut = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        moeOut = moeOut.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        if mlp.SharedUp != null {
        var sharedGate = mlp.SharedGate.Forward(ctx, hiddenStates2D);
        var sharedUp = mlp.SharedUp.Forward(ctx, hiddenStates2D);
        var sharedOut = sharedGate.SILU(ctx, sharedUp);
        sharedOut = mlp.SharedDown.Forward(ctx, sharedOut);
        if mlp.SharedGateInp != null {
        var sharedGateVal = mlp.SharedGateInp.Forward(ctx, hiddenStates2D);
        sharedGateVal = sharedGateVal.SigmoidOut(ctx);
        sharedGateVal = sharedGateVal.Repeat(ctx, 0, sharedOut.Dim(0));
        sharedOut = sharedOut.Mul(ctx, sharedGateVal);
    }
        moeOut = moeOut.Add(ctx, sharedOut);
    }
        return moeOut;
    }

    public static class dense {
        public *nn.Linear Gate;
        public *nn.Linear Up;
        public *nn.Linear Down;
    }
        func (mlp *dense) Forward(ctx ml.Context, hiddenStates ml.Tensor, _ *Options) ml.Tensor {
        hiddenStates = mlp.Gate.Forward(ctx, hiddenStates).SILU(ctx, mlp.Up.Forward(ctx, hiddenStates));
        return mlp.Down.Forward(ctx, hiddenStates);
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public *nn.RMSNorm AttentionPostNorm;
        public Operator Operator;
        public *nn.RMSNorm FFNNorm;
        public MLP MLP;
    }
        func (l *Layer) Forward(ctx ml.Context, layer int, hiddenStates, positions, outputs ml.Tensor, cache *HybridCache, opts *Options) (ml.Tensor, error) {
        var residual = hiddenStates;
        hiddenStates = l.AttentionNorm.Forward(ctx, hiddenStates, opts.eps);
        var err error;
        hiddenStates, err = l.Operator.Forward(ctx, hiddenStates, positions, cache, opts);
        if err != null {
        return null, err;
    }
        if outputs != null {
        hiddenStates = hiddenStates.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenStates = hiddenStates.Add(ctx, residual);
        var ffnResidual = hiddenStates;
        hiddenStates = l.AttentionPostNorm.Forward(ctx, hiddenStates, opts.eps);
        hiddenStates = l.MLP.Forward(ctx, hiddenStates, opts);
        return hiddenStates.Add(ctx, ffnResidual), null;
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public []Layer Layers;
        public *qwen3vl.VisionModel Vision;
        public *qwen3vl.ImageProcessor ImageProcessor;
        public []int32 positionCache;
        public int32 imageToken;
        public int32 visionStart;
        public int32 visionEnd;
        public uint32 spatialMergeSize;
    }
        func (m *Model) mapPosition(id int32) int32 {
        if id < int32(len(m.positionCache)) {
        return m.positionCache[id];
    }
        if len(m.positionCache) > 0 {
        return id - int32(len(m.positionCache)) + m.positionCache[len(m.positionCache)-1] + 1;
    }
        return id;
    }
        func (m *Model) buildPositions(ctx ml.Context, batch input.Batch) ml.Tensor {
        if len(m.mropeSections) == 0 {
        return ctx.Input().FromInts(batch.Positions, len(batch.Positions));
    }
        var positionSlice = [][]int32{
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
        make([]int32, len(batch.Positions)),;
    }
        var for i, id = range batch.Positions {
        var p = m.mapPosition(id);
        positionSlice[0][i] = p;
        positionSlice[1][i] = p;
        positionSlice[2][i] = p;
    }
        if m.Vision != null {
        var for _, mi = range batch.Multimodal {
        var grid, ok = mi.Multimodal[0].Data.(*qwen3vl.Grid);
        if !ok {
        continue;
    }
        var w = max(1, grid.Width/int(m.spatialMergeSize));
        var for i = range mi.Multimodal[0].Tensor.Dim(1) {
        positionSlice[1][mi.Index+i] += int32(i / w);
        positionSlice[2][mi.Index+i] += int32(i % w);
    }
    }
    }
        return ctx.Input().FromInts(slices.Concat(positionSlice...), len(positionSlice[0])*len(positionSlice));
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if m.Vision == null || m.ImageProcessor == null || len(m.Vision.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var pixelValues, grid, err = m.ImageProcessor.ProcessImage(ctx, img);
        if err != null {
        return null, err;
    }
        var visionOutputs, deepstackVisualEmbeds = m.Vision.Forward(ctx, pixelValues, grid);
        var mm = []input.Multimodal{{Tensor: visionOutputs, Data: grid}}
        var for i = range deepstackVisualEmbeds {
        mm = append(mm, input.Multimodal{Tensor: deepstackVisualEmbeds[i]});
    }
        return mm, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        m.positionCache = m.positionCache[:0];
        var result []*input.Input;
        var appendInput = func(inp *input.Input, position int32) {
        result = append(result, inp);
        m.positionCache = append(m.positionCache, position);
    }
        var p int32;
        var for _, inp = range inputs {
        if inp.Multimodal == null {
        appendInput(inp, p);
        p++;
        continue;
    }
        var grid = inp.Multimodal[0].Data.(*qwen3vl.Grid);
        var tokensPerGrid = inp.Multimodal[0].Tensor.Dim(1);
        appendInput(&input.Input{
        Token:     m.visionStart,;
        SameBatch: tokensPerGrid + 1,;
        }, p);
        p++;
        appendInput(&input.Input{
        Token:          m.imageToken,;
        Multimodal:     inp.Multimodal,;
        MultimodalHash: inp.MultimodalHash,;
        }, p);
        for range tokensPerGrid - 1 {
        appendInput(&input.Input{
        Token: m.imageToken,;
        }, p);
    }
        var gridSpan = max(grid.Width/int(m.spatialMergeSize), grid.Height/int(m.spatialMergeSize));
        p = p + int32(gridSpan);
        appendInput(&input.Input{
        Token: m.visionEnd,;
        }, p);
        p++;
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = m.buildPositions(ctx, batch);
        var hiddenStates = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        if len(batch.Multimodal) > 0 {
        hiddenStates = hiddenStates.Duplicate(ctx);
        var deepstackVisualEmbeds []ml.Tensor;
        var for _, mi = range batch.Multimodal {
        var visionOutputs = mi.Multimodal[0].Tensor;
        ctx.Forward(visionOutputs.Copy(ctx, hiddenStates.View(ctx, mi.Index*hiddenStates.Stride(1), visionOutputs.Dim(0)*visionOutputs.Dim(1))));
        if len(mi.Multimodal[1:]) > len(deepstackVisualEmbeds) {
        deepstackVisualEmbeds = append(deepstackVisualEmbeds, make([]ml.Tensor, len(mi.Multimodal[1:])-len(deepstackVisualEmbeds))...);
    }
        var for i, mm = range mi.Multimodal[1:] {
        if deepstackVisualEmbeds[i] == null {
        deepstackVisualEmbeds[i] = ctx.Input().Zeros(mm.Tensor.DType(), hiddenStates.Shape()...);
    }
        ctx.Forward(mm.Tensor.Copy(ctx, deepstackVisualEmbeds[i].View(ctx, mi.Index*deepstackVisualEmbeds[i].Stride(1), mm.Tensor.Dim(0)*mm.Tensor.Dim(1))));
    }
    }
        var cache = m.Cache.(*HybridCache);
        m.Options.masks = null;
        var for i, layer = range m.Layers {
        cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        var err error;
        hiddenStates, err = layer.Forward(ctx, i, hiddenStates, positions, outputs, cache, m.Options);
        if err != null {
        return null, err;
    }
        if i < len(deepstackVisualEmbeds) {
        hiddenStates = hiddenStates.Add(ctx, deepstackVisualEmbeds[i]);
    }
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }
        var cache = m.Cache.(*HybridCache);
        m.Options.masks = null;
        var for i, layer = range m.Layers {
        cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        var err error;
        hiddenStates, err = layer.Forward(ctx, i, hiddenStates, positions, outputs, cache, m.Options);
        if err != null {
        return null, err;
    }
    }
        hiddenStates = m.OutputNorm.Forward(ctx, hiddenStates, m.eps);
        return m.Output.Forward(ctx, hiddenStates), null;
    }
        func (m *Model) Validate() error {
        if m.Options == null {
        return fmt.Errorf("qwen3next: missing model options");
    }
        if len(m.Layers) != len(m.Options.isRecurrent) {
        return fmt.Errorf("qwen3next: layer config mismatch: have %d layers, %d recurrent flags", len(m.Layers), len(m.Options.isRecurrent));
    }
        var for i, layer = range m.Layers {
        if !m.Options.isRecurrent[i] {
        continue;
    }
        var gdn, ok = layer.Operator.(*GatedDeltaNet);
        if !ok || gdn == null {
        return fmt.Errorf("qwen3next: layer %d expected recurrent operator", i);
    }
        if gdn.SSMIn == null && (gdn.SSMQKV == null || gdn.SSMQKVGate == null) {
        return fmt.Errorf("qwen3next: layer %d missing attn_qkv/attn_gate projections", i);
    }
        if gdn.SSMBetaAlpha == null && (gdn.SSMBeta == null || gdn.SSMAlpha == null) {
        return fmt.Errorf("qwen3next: layer %d missing linear attention beta/alpha projections", i);
    }
        if gdn.SSMDT == null {
        return fmt.Errorf("qwen3next: layer %d missing ssm_dt tensor", i);
    }
        if gdn.SSMA == null {
        return fmt.Errorf("qwen3next: layer %d missing ssm_a tensor", i);
    }
        if gdn.SSMConv1D == null || gdn.SSMConv1D.Weight == null {
        return fmt.Errorf("qwen3next: layer %d missing ssm_conv1d tensor", i);
    }
        if gdn.SSMNorm == null || gdn.SSMOut == null {
        return fmt.Errorf("qwen3next: layer %d missing ssm_norm/ssm_out projections", i);
    }
    }
        return null;
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        m.positionCache = null;
        if len(m.mropeSections) > 0 {
        shift = shift.Repeat(ctx, 1, 4).Reshape(ctx, -1);
    }
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }
        var (;
        _ model.Model               = (*Model)(null);
        _ model.MultimodalProcessor = (*Model)(null);
        );

    public static boolean defaultVHeadReordered(String arch) {
        return arch == "qwen35" || arch == "qwen35moe";
    }

    public static void inferRecurrentLayers([]uint64 headCountKV, int numLayers) {
        var isRecurrent = make([]boolean, numLayers);
        var hasZero = false;
        var hasFull = false;
        var for i = range numLayers {
        if i >= len(headCountKV) {
        continue;
    }
        if headCountKV[i] == 0 {
        isRecurrent[i] = true;
        hasZero = true;
        } else {
        hasFull = true;
    }
    }
        if hasZero && hasFull {
        return isRecurrent, null;
    }
        if !hasFull {
        return null, fmt.Errorf("qwen3next: attention.head_count_kv must include at least one non-zero value");
    }
        var interval = int(fullAttentionInterval);
        if interval == 0 {
        interval = min(4, numLayers);
    }
        if interval <= 0 {
        return null, fmt.Errorf("qwen3next: invalid block_count (%d)", numLayers);
    }
        if interval > numLayers {
        return null, fmt.Errorf("qwen3next: full_attention_interval (%d) exceeds block_count (%d)", interval, numLayers);
    }
        hasZero = false;
        hasFull = false;
        var for i = range numLayers {
        isRecurrent[i] = (i+1)%interval != 0;
        if isRecurrent[i] {
        hasZero = true;
        } else {
        hasFull = true;
    }
    }
        if !hasZero || !hasFull {
        return null, fmt.Errorf("qwen3next: full_attention_interval (%d) does not produce a mixed recurrent/full layout", interval);
    }
        return isRecurrent, null;
    }

    public static void New() {
        var numLayers = int(c.Uint("block_count"));
        var layers = make([]Layer, numLayers);
        type headCounts interface {
        HeadCount() []uint64;
        HeadCountKV() []uint64;
    }
        var headCountKV []uint64;
        var if hc, ok = c.(headCounts); ok {
        headCountKV = hc.HeadCountKV();
    }
        var isRecurrent, err = inferRecurrentLayers(headCountKV, numLayers, c.Uint("full_attention_interval"));
        if err != null {
        return null, err;
    }
        var isMoE = c.Uint("expert_count") > 0;
        var for i = range layers {
        if isRecurrent[i] {
        layers[i].Operator = &GatedDeltaNet{Layer: i}
        } else {
        layers[i].Operator = &FullAttention{}
    }
        if isMoE {
        layers[i].MLP = &sparse{}
        } else {
        layers[i].MLP = &dense{}
    }
    }
        var mropeSections = c.Ints("mrope_sections", null);
        if len(mropeSections) == 0 {
        mropeSections = c.Ints("rope.mrope_section", null);
    }
        if len(mropeSections) == 0 {
        mropeSections = c.Ints("rope.dimension_sections", null);
    }
        if len(mropeSections) > 4 {
        mropeSections = mropeSections[:4];
    }
        var ropeType = c.String("rope.scaling.type");
        if ropeType == "" {
        ropeType = c.String("rope.type");
    }
        var opts = &Options{
        hiddenSize: int(c.Uint("embedding_length")),;
        numHeads:   int(c.Uint("attention.head_count")),;
        numKVHeads: func() int {
        var for _, v = range headCountKV {
        if v > 0 {
        return int(v);
    }
    }
        return 0;
        }(),;
        keyLength:             int(c.Uint("attention.key_length")),;
        valueLength:           int(c.Uint("attention.value_length")),;
        ropeDim:               int(c.Uint("rope.dimension_count")),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeType:              ropeType,;
        ropeBase:              c.Float("rope.freq_base"),;
        ropeScale:             c.Float("rope.scaling.factor", 1),;
        originalContextLength: int(c.Uint("rope.scaling.original_context_length")),;
        attentionScale:        double(c.Float("attention.scale")),;
        numExperts:            int(c.Uint("expert_count")),;
        numExpertsUsed:        int(c.Uint("expert_used_count")),;
        normTopKProb:          c.Bool("norm_top_k_prob", true),;
        ssmDInner:             int(c.Uint("ssm.inner_size")),;
        ssmDState:             int(c.Uint("ssm.state_size")),;
        ssmNGroup:             int(c.Uint("ssm.group_count")),;
        ssmDtRank:             int(c.Uint("ssm.time_step_rank")),;
        convKernelSize:        int(c.Uint("ssm.conv_kernel")),;
        vHeadReordered:        c.Bool("ssm.v_head_reordered", defaultVHeadReordered(c.Architecture())),;
        isRecurrent:           isRecurrent,;
        mropeSections: slices.Collect(func(yield func(int) boolean) {
        var for _, section = range mropeSections {
        if !yield(int(section)) {
        return;
    }
    }
        }),;
        mropeInterleaved: c.Bool("rope.mrope_interleaved", c.Bool("mrope_interleaved", false)),;
    }
        if opts.numKVHeads == 0 {
        return null, fmt.Errorf("qwen3next: attention.head_count_kv must include at least one non-zero value");
    }
        var convDim = max(0, opts.convKernelSize-1);
        var convChannels = opts.ssmDInner + 2*opts.ssmNGroup*opts.ssmDState;
        var headVDim = 0;
        var numVHeads = opts.ssmDtRank;
        if numVHeads > 0 {
        headVDim = opts.ssmDInner / numVHeads;
    }
        var deltaStateSize = headVDim * headVDim * numVHeads;
        var headKDim = opts.ssmDState;
        if headKDim != headVDim && headKDim > 0 && headVDim > 0 {
        return null, fmt.Errorf("qwen3next: headKDim (%d) != headVDim (%d) not supported; state computations require equal dimensions", headKDim, headVDim);
    }
        var vision *qwen3vl.VisionModel;
        var imageProcessor *qwen3vl.ImageProcessor;
        if c.Uint("vision.block_count", 0) > 0 {
        vision = qwen3vl.NewVisionModel(c);
        var processor = qwen3vl.NewImageProcessor(c);
        imageProcessor = &processor;
    }
        var spatialMergeSize = c.Uint("vision.spatial_merge_size", 2);
        if spatialMergeSize == 0 {
        spatialMergeSize = 2;
    }
        var m = Model{
        Tokenizer: tokenizer.NewBytePairEncoding(;
        &tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", false),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
        },;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        ),;
        Layers:           layers,;
        Vision:           vision,;
        ImageProcessor:   imageProcessor,;
        Options:          opts,;
        imageToken:       int32(c.Uint("image_token_id", 151655)),;
        visionStart:      int32(c.Uint("vision_start_token_id", 151652)),;
        visionEnd:        int32(c.Uint("vision_end_token_id", 151653)),;
        spatialMergeSize: spatialMergeSize,;
    }
        m.Cache = NewHybridCache(m.Shift, convDim, convChannels, deltaStateSize);
        return &m, null;
    }

    public static void init() {
        model.Register("qwen35", New);
        model.Register("qwen35moe", New);
        model.Register("qwen3next", New);
    }
}
