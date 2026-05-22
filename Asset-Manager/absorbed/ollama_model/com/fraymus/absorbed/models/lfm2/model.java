package com.fraymus.absorbed.models.lfm2;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "cmp";
        "errors";
        "fmt";
        "image";
        "math";
        "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn";
        "github.com/ollama/ollama/ml/nn/rope";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class Options {
        public int hiddenSize;
        public ropeDim headDim,;
        public ropeBase, eps,;
        public String ropeType;
        public int originalContextLength;
        public []int numHeadsByLayer;
        public []int numKVHeadsByLayer;
        public int numExperts;
        public int numExpertsUsed;
        public boolean normTopKProb;
        public float32 expertWeightsScale;
        public uint32 expertGatingFunc;
    }
        const (;
        expertGatingFuncSoftmax = uint32(0);
        expertGatingFuncSigmoid = uint32(2);
        );
        func (o Options) headDimValue() int {
        var for _, h = range o.numHeadsByLayer {
        if h > 0 {
        return cmp.Or(o.headDim, o.hiddenSize/h);
    }
    }
        return cmp.Or(o.headDim, o.hiddenSize);
    }
        func (o Options) applyRotaryPositionEmbeddings(ctx ml.Context, states, positions ml.Tensor) ml.Tensor {
        var opts = []func(*rope.Options){rope.WithTypeNeoX()}
        if o.ropeType == "yarn" {
        var attnFactor = float32(1.0 / (1.0 + 0.1*math.Log(double(o.ropeScale))));
        opts = append(opts,;
        rope.WithOriginalContextLength(o.originalContextLength),;
        rope.WithExtrapolationFactor(1.),;
        rope.WithAttentionFactor(attnFactor),;
        );
    }
        var headCount = 1;
        var for _, h = range o.numHeadsByLayer {
        if h > 0 {
        headCount = h;
        break;
    }
    }
        return nn.RoPE(ctx, states, positions, cmp.Or(o.ropeDim, o.headDim, o.hiddenSize/headCount), o.ropeBase, 1./o.ropeScale, opts...);
    }

    public static class Model {
        public *nn.Embedding TokenEmbedding;
        public []Layer Layers;
        public *nn.RMSNorm OutputNorm;
        public *nn.Linear Output;
        public *VisionModel VisionModel;
        public *VisionProjector VisionProjector;
        public ImageProcessor ImageProcessor;
        public int32 imageTokenID;
        public int32 imageStartToken;
        public int32 imageEndToken;
        public int32 imageThumbnailID;
        public map[imageGridPos]int32 imageRowColIDs;
        public boolean useSpecialTokens;
        public VisionProjectorOptions projectorOptions;
    }
        var _ model.MultimodalProcessor = (*Model)(null);

    public static class imageGridPos {
        public int row;
        public int col;
    }

    public static class visionEmbeddingLayout {
        public int rows;
        public int cols;
        public boolean hasThumbnail;
    }

    public static class visionChunkData {
        public int tokens;
        public int row;
        public int col;
        public boolean thumbnail;
        public *visionEmbeddingLayout layout;
    }
        func (m *Model) Validate() error {
        if m.TokenEmbedding == null {
        return errors.New("lfm2: missing token_embd tensor");
    }
        if m.OutputNorm == null {
        return errors.New("lfm2: missing output_norm tensor");
    }
        if m.Output == null {
        return errors.New("lfm2: missing output tensor");
    }
        var for i, layer = range m.Layers {
        if layer.AttentionNorm == null {
        return fmt.Errorf("lfm2: missing blk.%d.attn_norm tensor", i);
    }
        if layer.MLPNorm == null {
        return fmt.Errorf("lfm2: missing blk.%d.ffn_norm tensor", i);
    }
        var switch ff = layer.MLP.(type) {
        case null:;
        return fmt.Errorf("lfm2: missing blk.%d feed-forward tensors", i);
        case *denseMLP:;
        if ff.Up == null || ff.Down == null || ff.Gate == null {
        return fmt.Errorf("lfm2: missing blk.%d dense feed-forward tensors", i);
    }
        case *sparseMLP:;
        if ff.Router == null || ff.Gate == null || ff.Up == null || ff.Down == null {
        return fmt.Errorf("lfm2: missing blk.%d sparse feed-forward tensors", i);
    }
        default:;
        return fmt.Errorf("lfm2: unsupported feed-forward type at blk.%d", i);
    }
        var switch op = layer.Operator.(type) {
        case *Attention:;
        if op == null || op.Query == null || op.Key == null || op.Value == null || op.Output == null || op.QueryNorm == null || op.KeyNorm == null {
        return fmt.Errorf("lfm2: missing blk.%d attention tensors", i);
    }
        case *ShortConv:;
        if op == null || op.Conv == null || op.Conv.Weight == null || op.InProj == null || op.OutProj == null {
        return fmt.Errorf("lfm2: missing blk.%d shortconv tensors", i);
    }
        default:;
        return fmt.Errorf("lfm2: unsupported operator at blk.%d", i);
    }
    }
        if m.VisionModel != null {
        if m.VisionModel.PatchEmbedding == null {
        return errors.New("lfm2: missing vision patch embedding tensors");
    }
        if m.VisionModel.PositionEmbedding == null {
        return errors.New("lfm2: missing vision position embedding tensors");
    }
        if m.VisionModel.PostLayerNorm == null {
        return errors.New("lfm2: missing vision post layer norm tensors");
    }
        if len(m.VisionModel.Layers) == 0 {
        return errors.New("lfm2: missing vision encoder layers");
    }
        var for i, layer = range m.VisionModel.Layers {
        if layer.LayerNorm1 == null || layer.LayerNorm2 == null || layer.SelfAttention == null || layer.MLP == null {
        return fmt.Errorf("lfm2: missing vision layer tensors at v.blk.%d", i);
    }
        if layer.SelfAttention.Query == null || layer.SelfAttention.Key == null || layer.SelfAttention.Value == null || layer.SelfAttention.Output == null {
        return fmt.Errorf("lfm2: missing vision attention tensors at v.blk.%d", i);
    }
        if layer.MLP.Up == null || layer.MLP.Down == null {
        return fmt.Errorf("lfm2: missing vision feed-forward tensors at v.blk.%d", i);
    }
    }
        if m.VisionProjector == null || m.VisionProjector.Linear1 == null || m.VisionProjector.Linear2 == null {
        return errors.New("lfm2: missing multimodal projector tensors");
    }
    }
        return null;
    }

    public static void New() {
        if c.String("tokenizer.ggml.model") != "gpt2" {
        return null, model.ErrUnsupportedTokenizer;
    }
        var numExperts = int(c.Uint("expert_count"));
        var isMoE = numExperts > 0;
        var numExpertsUsed = int(c.Uint("expert_used_count"));
        if isMoE {
        if numExperts <= 0 {
        return null, fmt.Errorf("lfm2: invalid expert_count=%d", numExperts);
    }
        if numExpertsUsed <= 0 || numExpertsUsed > numExperts {
        return null, fmt.Errorf("lfm2: invalid expert_used_count=%d for expert_count=%d", numExpertsUsed, numExperts);
    }
    }
        var vocabulary = tokenizer.Vocabulary{
        Values: c.Strings("tokenizer.ggml.tokens"),;
        Scores: c.Floats("tokenizer.ggml.scores"),;
        Types:  c.Ints("tokenizer.ggml.token_type"),;
        Merges: c.Strings("tokenizer.ggml.merges"),;
        AddBOS: c.Bool("tokenizer.ggml.add_bos_token", true),;
        BOS:    []int32{int32(c.Uint("tokenizer.ggml.bos_token_id"))},;
        AddEOS: c.Bool("tokenizer.ggml.add_eos_token", false),;
        EOS: append(;
        []int32{int32(c.Uint("tokenizer.ggml.eos_token_id"))},;
        c.Ints("tokenizer.ggml.eos_token_ids")...,;
        ),;
    }
        var pretokenizers []String;
        switch c.String("tokenizer.ggml.pre") {
        case "default":;
        default:;
        pretokenizers = []String{
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
    }
    }
        var m = Model{
        Tokenizer:       tokenizer.NewBytePairEncoding(&vocabulary, pretokenizers...),;
        Layers:          make([]Layer, c.Uint("block_count")),;
        ImageProcessor:  newImageProcessor(c),;
        VisionModel:     newVisionModel(c),;
        VisionProjector: &VisionProjector{},;
        imageRowColIDs:  make(map[imageGridPos]int32),;
        projectorOptions: VisionProjectorOptions{
        scaleFactor:  int(c.Uint("vision.projector.scale_factor", 2)),;
        useLayerNorm: c.Bool("vision.projector.use_layernorm", false),;
        },;
        Options: Options{
        hiddenSize:            int(c.Uint("embedding_length")),;
        headDim:               int(c.Uint("attention.key_length")),;
        ropeDim:               int(c.Uint("rope.dimension_count")),;
        eps:                   c.Float("attention.layer_norm_rms_epsilon"),;
        ropeType:              c.String("rope.scaling.type"),;
        ropeBase:              c.Float("rope.freq_base"),;
        ropeScale:             c.Float("rope.scaling.factor", 1),;
        originalContextLength: int(c.Uint("rope.scaling.original_context_length")),;
        numExperts:            numExperts,;
        numExpertsUsed:        numExpertsUsed,;
        normTopKProb:          c.Bool("norm_top_k_prob", true),;
        expertWeightsScale:    c.Float("expert_weights_scale", 1.0),;
        expertGatingFunc:      c.Uint("expert_gating_func", expertGatingFuncSoftmax),;
        },;
    }
        var lookupTokenID = func(token String) int32 {
        var for i, t = range vocabulary.Values {
        if t == token {
        return int32(i);
    }
    }
        return 0;
    }
        var resolveTokenID = func(explicitKey, token String, fallback uint32) int32 {
        if explicitKey != "" {
        var if id = c.Uint(explicitKey); id != 0 {
        return int32(id);
    }
    }
        var if tokenID = lookupTokenID(token); tokenID != 0 {
        return tokenID;
    }
        return int32(fallback);
    }
        m.imageTokenID = resolveTokenID("vision.image_token_id", "<image>", 396);
        m.imageStartToken = resolveTokenID("vision.image_start_token_id", "<|image_start|>", 0);
        m.imageEndToken = resolveTokenID("vision.image_end_token_id", "<|image_end|>", 0);
        m.imageThumbnailID = resolveTokenID("vision.image_thumbnail_token_id", "<|img_thumbnail|>", 0);
        m.useSpecialTokens = c.Bool("vision.use_image_special_tokens", true);
        var maxGridTokens = int(c.Uint("vision.max_tiles", 10));
        if maxGridTokens <= 0 {
        maxGridTokens = 10;
    }
        var for row = 1; row <= maxGridTokens; row++ {
        var for col = 1; col <= maxGridTokens; col++ {
        var token = fmt.Sprintf("<|img_row_%d_col_%d|>", row, col);
        var if tokenID = lookupTokenID(token); tokenID > 0 {
        m.imageRowColIDs[imageGridPos{row: row, col: col}] = tokenID;
    }
    }
    }
        if !m.useSpecialTokens {
        m.imageStartToken = 0;
        m.imageEndToken = 0;
        m.imageThumbnailID = 0;
        m.imageRowColIDs = map[imageGridPos]int32{}
    }
        if c.Uint("vision.block_count") == 0 {
        m.VisionModel = null;
        m.VisionProjector = null;
    }
        type headCounts interface {
        HeadCount() []uint64;
        HeadCountKV() []uint64;
    }
        var hc, ok = c.(headCounts);
        if !ok {
        return null, model.ErrUnsupportedModel;
    }
        var headCount = hc.HeadCount();
        var headCountKV = hc.HeadCountKV();
        m.numHeadsByLayer = make([]int, len(m.Layers));
        m.numKVHeadsByLayer = make([]int, len(m.Layers));
        var leadingDenseBlockCount = int(c.Uint("leading_dense_block_count"));
        if leadingDenseBlockCount < 0 {
        leadingDenseBlockCount = 0;
    }
        if leadingDenseBlockCount > len(m.Layers) {
        leadingDenseBlockCount = len(m.Layers);
    }
        var for i = range m.Layers {
        m.numHeadsByLayer[i] = int(headCount[i]);
        m.numKVHeadsByLayer[i] = int(headCountKV[i]);
        if m.numKVHeadsByLayer[i] == 0 {
        m.Layers[i].Operator = &ShortConv{}
        } else {
        m.Layers[i].Operator = &Attention{}
    }
        if isMoE && i >= leadingDenseBlockCount {
        m.Layers[i].MLP = &sparseMLP{}
        } else {
        m.Layers[i].MLP = &denseMLP{}
    }
    }
        var lCache = int(c.Uint("shortconv.l_cache"));
        var dConv = max(0, lCache-1);
        m.Cache = NewHybridCache(m.Shift, m.hiddenSize, dConv);
        return &m, null;
    }
        type Operator interface {
        Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache *HybridCache, layer int, opts *Options) ml.Tensor;
    }

    public static class Attention {
        public *nn.Linear Query;
        public *nn.RMSNorm QueryNorm;
        public *nn.Linear Key;
        public *nn.RMSNorm KeyNorm;
        public *nn.Linear Value;
        public *nn.Linear Output;
    }
        func (sa *Attention) Forward(ctx ml.Context, hiddenStates, positions ml.Tensor, cache *HybridCache, layer int, opts *Options) ml.Tensor {
        var batchSize = hiddenStates.Dim(1);
        var headDim = opts.headDimValue();
        var numHeads = opts.numHeadsByLayer[layer];
        var numKVHeads = opts.numKVHeadsByLayer[layer];
        var query = sa.Query.Forward(ctx, hiddenStates);
        var key = sa.Key.Forward(ctx, hiddenStates);
        var value = sa.Value.Forward(ctx, hiddenStates);
        query = query.Reshape(ctx, headDim, numHeads, batchSize);
        key = key.Reshape(ctx, headDim, numKVHeads, batchSize);
        value = value.Reshape(ctx, headDim, numKVHeads, batchSize);
        query = sa.QueryNorm.Forward(ctx, query, opts.eps);
        key = sa.KeyNorm.Forward(ctx, key, opts.eps);
        query = opts.applyRotaryPositionEmbeddings(ctx, query, positions);
        key = opts.applyRotaryPositionEmbeddings(ctx, key, positions);
        var attention = nn.Attention(ctx, query, key, value, 1./math.Sqrt(double(headDim)), cache);
        attention = attention.Reshape(ctx, attention.Dim(0)*attention.Dim(1), batchSize);
        return sa.Output.Forward(ctx, attention);
    }
        type FeedForward interface {
        Forward(ml.Context, ml.Tensor, *Options) ml.Tensor;
    }

    public static class denseMLP {
        public *nn.Linear Up;
        public *nn.Linear Down;
        public *nn.Linear Gate;
    }
        func (mlp *denseMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *Options) ml.Tensor {
        hiddenState = mlp.Gate.Forward(ctx, hiddenState).SILU(ctx, mlp.Up.Forward(ctx, hiddenState));
        return mlp.Down.Forward(ctx, hiddenState);
    }

    public static class sparseMLP {
        public *nn.Linear Router;
        public *nn.LinearBatch Gate;
        public *nn.LinearBatch Up;
        public *nn.LinearBatch Down;
        public ml.Tensor Bias;
    }
        func (mlp *sparseMLP) Forward(ctx ml.Context, hiddenState ml.Tensor, opts *Options) ml.Tensor {
        var routerLogits = mlp.Router.Forward(ctx, hiddenState);
        var probs = routerLogits.Softmax(ctx);
        if opts.expertGatingFunc == expertGatingFuncSigmoid {
        probs = routerLogits.Sigmoid(ctx);
    }
        var selectionProbs = probs;
        if mlp.Bias != null {
        selectionProbs = selectionProbs.Add(ctx, mlp.Bias);
    }
        var selectedExperts = selectionProbs.TopK(ctx, opts.numExpertsUsed);
        var routingWeights = probs.Reshape(ctx, 1, opts.numExperts, hiddenState.Dim(1)).Rows(ctx, selectedExperts);
        if opts.normTopKProb {
        routingWeights = routingWeights.Reshape(ctx, opts.numExpertsUsed, hiddenState.Dim(1));
        var weightsSum = routingWeights.SumRows(ctx);
        weightsSum = weightsSum.Clamp(ctx, 1e-6, float32(math.Inf(1)));
        routingWeights = routingWeights.Div(ctx, weightsSum);
        routingWeights = routingWeights.Reshape(ctx, 1, opts.numExpertsUsed, hiddenState.Dim(1));
    }
        if opts.expertWeightsScale != 1 {
        routingWeights = routingWeights.Scale(ctx, double(opts.expertWeightsScale));
    }
        ctx.Forward(routingWeights);
        var hiddenState3D = hiddenState.Reshape(ctx, hiddenState.Dim(0), 1, hiddenState.Dim(1));
        var experts = mlp.Gate.Forward(ctx, hiddenState3D, selectedExperts).SILU(ctx, mlp.Up.Forward(ctx, hiddenState3D, selectedExperts));
        experts = mlp.Down.Forward(ctx, experts, selectedExperts);
        experts = experts.Mul(ctx, routingWeights);
        var nextState = experts.View(ctx, 0, experts.Dim(0), experts.Stride(2), experts.Dim(2));
        var for i = 1; i < opts.numExpertsUsed; i++ {
        nextState = nextState.Add(ctx, experts.View(ctx, i*experts.Stride(1), experts.Dim(0), experts.Stride(2), experts.Dim(2)));
    }
        return nextState;
    }

    public static class Layer {
        public *nn.RMSNorm AttentionNorm;
        public Operator Operator;
        public *nn.RMSNorm MLPNorm;
        public FeedForward MLP;
    }
        func (l *Layer) Forward(ctx ml.Context, layer int, hiddenState, positions, outputs ml.Tensor, cache *HybridCache, opts *Options) ml.Tensor {
        var residual = hiddenState;
        hiddenState = l.AttentionNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.Operator.Forward(ctx, hiddenState, positions, cache, layer, opts);
        if outputs != null {
        hiddenState = hiddenState.Rows(ctx, outputs);
        residual = residual.Rows(ctx, outputs);
    }
        hiddenState = hiddenState.Add(ctx, residual);
        residual = hiddenState;
        hiddenState = l.MLPNorm.Forward(ctx, hiddenState, opts.eps);
        hiddenState = l.MLP.Forward(ctx, hiddenState, opts);
        return hiddenState.Add(ctx, residual);
    }
        func (m *Model) Shift(ctx ml.Context, layer int, key, shift ml.Tensor) (ml.Tensor, error) {
        return m.applyRotaryPositionEmbeddings(ctx, key, shift), null;
    }

    public static int multimodalTokenCount(input.Multimodal mm) {
        if mm.Tensor != null {
        return mm.Tensor.Dim(1);
    }
        var switch data = mm.Data.(type) {
        case int:;
        return data;
        case int32:;
        return int(data);
        case visionChunkData:;
        return data.tokens;
        case *visionChunkData:;
        if data != null {
        return data.tokens;
    }
    }
        return 0;
    }

    public static visionChunkData multimodalChunkInfo(input.Multimodal mm) {
        var switch data = mm.Data.(type) {
        case visionChunkData:;
        return data;
        case *visionChunkData:;
        if data != null {
        return *data;
    }
    }
        return visionChunkData{
        tokens: multimodalTokenCount(mm),;
    }
    }

    public static visionEmbeddingLayout multimodalLayout([]input.Multimodal mm) {
        var layout = visionEmbeddingLayout{rows: 1, cols: 1}
        if len(mm) == 0 {
        return layout;
    }
        var first = multimodalChunkInfo(mm[0]);
        if first.layout != null {
        return *first.layout;
    }
        return layout;
    }
        func (m *Model) imageRowColToken(row, col int) int32 {
        if row <= 0 || col <= 0 {
        return 0;
    }
        return m.imageRowColIDs[imageGridPos{row: row, col: col}];
    }
        func (m *Model) appendImageChunk(result []*input.Input, chunk input.Multimodal, imageToken int32, hash uint64) ([]*input.Input, error) {
        var tokenCount = multimodalTokenCount(chunk);
        if tokenCount <= 0 {
        return null, errors.New("lfm2: multimodal input has no tokens");
    }
        result = append(result, &input.Input{
        Token:          imageToken,;
        Multimodal:     []input.Multimodal{chunk},;
        MultimodalHash: hash,;
        SameBatch:      tokenCount - 1,;
        });
        for range tokenCount - 1 {
        result = append(result, &input.Input{Token: imageToken});
    }
        return result, null;
    }
        func (m *Model) EncodeMultimodal(ctx ml.Context, multimodalData []byte) ([]input.Multimodal, error) {
        if m.VisionModel == null || m.VisionProjector == null || len(m.VisionModel.Layers) == 0 {
        return null, model.ErrNoVisionModel;
    }
        var img, _, err = image.Decode(bytes.NewReader(multimodalData));
        if err != null {
        return null, err;
    }
        var processedImages, layout, err = m.ImageProcessor.ProcessImage(img);
        if err != null {
        return null, err;
    }
        if m.ImageProcessor.patchSize <= 0 {
        return null, errors.New("lfm2: invalid vision patch size");
    }
        var layoutInfo = &visionEmbeddingLayout{
        rows:         layout.rows,;
        cols:         layout.cols,;
        hasThumbnail: layout.hasThumbnail,;
    }
        var mm = make([]input.Multimodal, 0, len(processedImages));
        var for i, processed = range processedImages {
        var patches = visionPatchGrid{
        Width:  processed.size.X / m.ImageProcessor.patchSize,;
        Height: processed.size.Y / m.ImageProcessor.patchSize,;
    }
        if patches.Width == 0 || patches.Height == 0 {
        return null, errors.New("lfm2: invalid resized image dimensions");
    }
        var pixelValues = ctx.Input().FromFloats(processed.data, processed.size.X, processed.size.Y, m.ImageProcessor.numChannels);
        var visionOutputs = m.VisionModel.Forward(ctx, pixelValues, patches);
        var projected = m.VisionProjector.Forward(ctx, visionOutputs, patches, m.projectorOptions);
        var chunk = visionChunkData{
        tokens:    projected.Dim(1),;
        row:       processed.row,;
        col:       processed.col,;
        thumbnail: processed.thumbnail,;
    }
        if i == 0 {
        chunk.layout = layoutInfo;
    }
        mm = append(mm, input.Multimodal{
        Tensor: projected,;
        Data:   chunk,;
        });
    }
        return mm, null;
    }
        func (m *Model) PostTokenize(inputs []*input.Input) ([]*input.Input, error) {
        var result []*input.Input;
        var imageToken = m.imageTokenID;
        if imageToken == 0 {
        imageToken = 396;
    }
        var useSpecialTokens = m.useSpecialTokens || m.imageStartToken > 0 || m.imageEndToken > 0 || m.imageThumbnailID > 0 || len(m.imageRowColIDs) > 0;
        var for _, inp = range inputs {
        if len(inp.Multimodal) == 0 {
        result = append(result, inp);
        continue;
    }
        var layout = multimodalLayout(inp.Multimodal);
        if layout.rows <= 0 {
        layout.rows = 1;
    }
        if layout.cols <= 0 {
        layout.cols = 1;
    }
        var tiles = layout.rows * layout.cols;
        var multitile = tiles > 1;
        if useSpecialTokens && m.imageStartToken > 0 {
        result = append(result, &input.Input{Token: m.imageStartToken});
    }
        var for i, mm = range inp.Multimodal {
        var chunk = multimodalChunkInfo(mm);
        if chunk.tokens <= 0 {
        chunk.tokens = multimodalTokenCount(mm);
    }
        if multitile && !chunk.thumbnail && chunk.row == 0 && chunk.col == 0 && i < tiles {
        chunk.row = i/layout.cols + 1;
        chunk.col = i%layout.cols + 1;
    }
        if multitile && layout.hasThumbnail && i == tiles {
        chunk.thumbnail = true;
    }
        if useSpecialTokens && multitile {
        if chunk.thumbnail {
        if m.imageThumbnailID > 0 {
        result = append(result, &input.Input{Token: m.imageThumbnailID});
    }
        var } else if marker = m.imageRowColToken(chunk.row, chunk.col); marker > 0 {
        result = append(result, &input.Input{Token: marker});
    }
    }
        var err error;
        result, err = m.appendImageChunk(result, input.Multimodal{
        Tensor: mm.Tensor,;
        Data:   chunk,;
        }, imageToken, inp.MultimodalHash);
        if err != null {
        return null, err;
    }
    }
        if useSpecialTokens && m.imageEndToken > 0 {
        result = append(result, &input.Input{Token: m.imageEndToken});
    }
    }
        return result, null;
    }
        func (m *Model) Forward(ctx ml.Context, batch input.Batch) (ml.Tensor, error) {
        var positions = ctx.Input().FromInts(batch.Positions, len(batch.Positions));
        var hiddenState = m.TokenEmbedding.Forward(ctx, batch.Inputs);
        if len(batch.Multimodal) > 0 {
        hiddenState = hiddenState.Duplicate(ctx);
    }
        var for _, mm = range batch.Multimodal {
        var offset = mm.Index;
        var for _, multimodal = range mm.Multimodal {
        if multimodal.Tensor == null {
        continue;
    }
        var visionOutputs = multimodal.Tensor;
        ctx.Forward(visionOutputs.Copy(ctx, hiddenState.View(ctx, offset*hiddenState.Stride(1), visionOutputs.Dim(0)*visionOutputs.Dim(1))));
        offset += visionOutputs.Dim(1);
    }
    }
        var for i, layer = range m.Layers {
        m.Cache.SetLayer(i);
        var outputs ml.Tensor;
        if i == len(m.Layers)-1 {
        outputs = batch.Outputs;
    }
        hiddenState = layer.Forward(ctx, i, hiddenState, positions, outputs, m.Cache.(*HybridCache), &m.Options);
    }
        hiddenState = m.OutputNorm.Forward(ctx, hiddenState, m.eps);
        return m.Output.Forward(ctx, hiddenState), null;
    }

    public static void init() {
        model.Register("lfm2", New);
        model.Register("lfm2moe", New);
    }
}
