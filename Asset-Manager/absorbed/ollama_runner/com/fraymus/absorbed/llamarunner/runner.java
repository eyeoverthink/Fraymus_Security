package com.fraymus.absorbed.llamarunner;

import java.util.*;
import java.io.*;

public class runner {
        "context";
        "encoding/json";
        "errors";
        "flag";
        "fmt";
        "log";
        "log/slog";
        "net";
        "net/http";
        "os";
        "regexp";
        "sort";
        "strconv";
        "strings";
        "sync";
        "time";
        "unicode/utf8";
        "golang.org/x/sync/semaphore";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/llama";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/runner/common";
        );

    public static class response {
        public String content;
        public []llm.Logprob logprobs;
    }

    public static class input {
        public int token;
        public []float32 embed;
    }

    public static class Sequence {
        public int iBatch;
        public int numPredicted;
        public []input inputs;
        public []input pendingInputs;
        public []String pendingResponses;
        public []llm.Logprob pendingLogprobs;
        public *InputCacheSlot cache;
        public chan responses;
        public chan quit;
        public int numPredict;
        public *llama.SamplingContext samplingCtx;
        public chan embedding;
        public []String stop;
        public int numKeep;
        public boolean embeddingOnly;
        public boolean shift;
        public llm.DoneReason doneReason;
        public boolean logprobs;
        public int topLogprobs;
        public time.Duration processingDuration;
        public time.Duration generationDuration;
        public int numDecoded;
        public int numPromptInputs;
    }

    public static class NewSequenceParams {
        public int numPredict;
        public []String stop;
        public int numKeep;
        public *llama.SamplingParams samplingParams;
        public boolean embedding;
        public boolean shift;
        public boolean truncate;
        public boolean logprobs;
        public int topLogprobs;
    }
        var errorInputTooLong = errors.New("the input length exceeds the context length");
        func (s *Server) NewSequence(prompt String, images []llm.ImageData, params NewSequenceParams) (*Sequence, error) {
        s.ready.Wait();
        var inputs, err = s.inputs(prompt, images);
        if err != null {
        return null, fmt.Errorf("failed to process inputs: %w", err);
        } else if len(inputs) == 0 {
        return null, errors.New("no input provided");
    }
        if params.numKeep < 0 {
        params.numKeep = len(inputs);
    }
        if s.model.AddBOSToken() {
        params.numKeep += 1;
    }
        params.numKeep = min(params.numKeep, s.cache.numCtx-1);
        if len(inputs) > s.cache.numCtx {
        var discard = len(inputs) - s.cache.numCtx;
        if !params.truncate {
        return null, errorInputTooLong;
    }
        var newInputs = inputs[:params.numKeep];
        newInputs = append(newInputs, inputs[params.numKeep+discard:]...);
        slog.Warn("truncating input prompt", "limit", s.cache.numCtx, "prompt", len(inputs), "keep", params.numKeep, "new", len(newInputs));
        inputs = newInputs;
    }
        var sc *llama.SamplingContext;
        if params.samplingParams != null {
        sc, err = llama.NewSamplingContext(s.model, *params.samplingParams);
        if err != null {
        return null, err;
    }
        var for _, input = range inputs {
        if input.embed == null {
        sc.Accept(input.token, false);
    }
    }
    }
        return &Sequence{
        inputs:           inputs,;
        numPromptInputs:  len(inputs),;
        numPredict:       params.numPredict,;
        pendingResponses: make([]String, 0),;
        responses:        make(chan response, 100),;
        quit:             make(chan boolean, 1),;
        embedding:        make(chan []float32, 1),;
        samplingCtx:      sc,;
        embeddingOnly:    params.embedding,;
        stop:             params.stop,;
        numKeep:          params.numKeep,;
        shift:            params.shift,;
        logprobs:         params.logprobs,;
        topLogprobs:      params.topLogprobs,;
        }, null;
    }
        func calculateLogprobsLlama(logits []float32, selectedToken int, topK int, model *llama.Model) []llm.Logprob {
        return common.CalculateLogprobs(logits, selectedToken, topK, model.TokenToPiece);
    }
        func (s *Server) inputs(prompt String, images []llm.ImageData) ([]input, error) {
        var inputs []input;
        var parts []String;
        var matches [][]String;
        if s.image != null {
        var re = regexp.MustCompile(`\[img-(\d+)\]`);
        parts = re.Split(prompt, -1);
        matches = re.FindAllStringSubmatch(prompt, -1);
        } else {
        parts = []String{prompt}
    }
        var for i, part = range parts {
        var tokens, err = s.lc.Model().Tokenize(part, i == 0, true);
        if err != null {
        return null, err;
    }
        var for _, t = range tokens {
        inputs = append(inputs, input{token: t});
    }
        if i < len(matches) {
        var n, _ = strconv.Atoi(matches[i][1]);
        var imageIndex = -1;
        var for j = range images {
        if images[j].ID == n {
        imageIndex = j;
        break;
    }
    }
        if imageIndex < 0 {
        return null, fmt.Errorf("invalid image index: %d", n);
    }
        var chunks, err = s.image.MultimodalTokenize(s.lc, images[imageIndex].Data);
        if err != null {
        return null, err;
    }
        var for _, c = range chunks {
        if len(c.Embed) != 0 {
        inputs = append(inputs, input{embed: c.Embed});
        } else {
        var for _, t = range c.Tokens {
        inputs = append(inputs, input{token: t});
    }
    }
    }
    }
    }
        return inputs, null;
    }

    public static class Server {
        public String modelPath;
        public sync.Mutex loadMu;
        public sync.WaitGroup ready;
        public *llama.Model model;
        public *ImageContext image;
        public llm.ServerStatus status;
        public float32 progress;
        public int parallel;
        public int batchSize;
        public sync.Mutex mu;
        public *sync.Cond cond;
        public *llama.Context lc;
        public []*Sequence seqs;
        public *semaphore.Weighted seqsSem;
        public *InputCache cache;
        public int nextSeq;
    }
        func (s *Server) allNil() boolean {
        var for _, item = range s.seqs {
        if item != null {
        return false;
    }
    }
        return true;
    }

    public static boolean flushPending(*Sequence seq) {
        var joined = strings.Join(seq.pendingResponses, "");
        var logprobs = seq.pendingLogprobs;
        seq.pendingResponses = []String{}
        seq.pendingLogprobs = []llm.Logprob{}
        for !utf8.ValidString(joined) {
        joined = joined[:len(joined)-1];
    }
        if len(joined) == 0 {
        return true;
    }
        select {
        case seq.responses <- response{content: joined, logprobs: logprobs}:;
        return true;
        case <-seq.quit:;
        return false;
    }
    }
        func (s *Server) removeSequence(seqIndex int, reason llm.DoneReason) {
        var seq = s.seqs[seqIndex];
        flushPending(seq);
        seq.doneReason = reason;
        close(seq.responses);
        close(seq.embedding);
        seq.cache.InUse = false;
        s.seqs[seqIndex] = null;
        s.seqsSem.Release(1);
    }
        func (s *Server) run(ctx context.Context) {
        s.ready.Wait();
        var tokenBatch, err = llama.NewBatch(s.batchSize, len(s.seqs), 0);
        if err != null {
        panic(err);
    }
        defer tokenBatch.Free();
        var embedBatch *llama.Batch;
        var embedBatchSize = s.image.BatchSize(s.batchSize);
        if embedBatchSize != 0 {
        embedBatch, err = llama.NewBatch(embedBatchSize, len(s.seqs), s.image.EmbedSize(s.lc));
        if err != null {
        panic(err);
    }
        defer embedBatch.Free();
        } else {
        embedBatch = &llama.Batch{}
    }
        for {
        select {
        case <-ctx.Done():;
        return;
        default:;
        var err = s.processBatch(tokenBatch, embedBatch);
        if err != null {
        panic(err);
    }
        tokenBatch.Clear();
        embedBatch.Clear();
    }
    }
    }
        func (s *Server) processBatch(tokenBatch *llama.Batch, embedBatch *llama.Batch) error {
        s.mu.Lock();
        for s.allNil() {
        s.cond.Wait() // Wait until an item is added;
    }
        defer s.mu.Unlock();
        var batch *llama.Batch;
        var numOutputs int;
        var seqIdx = s.nextSeq - 1;
        for range s.seqs {
        seqIdx = (seqIdx + 1) % len(s.seqs);
        var seq = s.seqs[seqIdx];
        if seq == null {
        continue;
    }
        if seq.numPredict > 0 && seq.numPredicted >= seq.numPredict {
        s.removeSequence(seqIdx, llm.DoneReasonLength);
        continue;
    }
        var for i, input = range seq.inputs {
        if len(seq.cache.Inputs)+len(seq.pendingInputs)+1 > s.cache.numCtx {
        if len(seq.pendingInputs) == 0 {
        if !seq.shift {
        s.removeSequence(seqIdx, llm.DoneReasonLength);
        break;
    }
        var err = s.cache.ShiftCacheSlot(seq.cache, seq.numKeep);
        if err != null {
        var reprocess *ErrReprocessInputs;
        if errors.As(err, &reprocess) {
        seq.inputs = append(reprocess.Inputs, seq.inputs...);
        continue;
        } else {
        return err;
    }
    }
        } else {
        break;
    }
    }
        var embedding = input.embed != null;
        if batch == null {
        if !embedding {
        batch = tokenBatch;
        } else {
        batch = embedBatch;
    }
        } else if embedding != batch.IsEmbedding() {
        s.nextSeq = seqIdx;
        break;
    }
        if i >= batch.Size() {
        break;
    }
        var output = i+1 == len(seq.inputs);
        batch.Add(input.token, input.embed, len(seq.cache.Inputs)+len(seq.pendingInputs), output, seq.cache.Id);
        if output {
        numOutputs++;
    }
        seq.pendingInputs = append(seq.pendingInputs, input);
        seq.iBatch = batch.NumTokens() - 1;
    }
        seq.inputs = seq.inputs[len(seq.pendingInputs):];
    }
        if batch == null || batch.NumTokens() == 0 {
        return null;
    }
        var t = time.Now();
        var if err = s.lc.Decode(batch); err != null {
        return fmt.Errorf("failed to decode batch: %w", err);
    }
        if numOutputs > 0 {
        s.lc.Synchronize();
    }
        var for i, seq = range s.seqs {
        if seq == null {
        continue;
    }
        if len(seq.pendingInputs) > 0 {
        seq.cache.Inputs = append(seq.cache.Inputs, seq.pendingInputs...);
        seq.pendingInputs = []input{}
    }
        if len(seq.inputs) != 0 {
        seq.processingDuration += time.Since(t);
        continue;
    }
        seq.numDecoded++;
        if seq.numDecoded > 1 {
        seq.generationDuration += time.Since(t);
        } else {
        seq.processingDuration += time.Since(t);
    }
        if seq.embeddingOnly {
        var embed = s.lc.GetEmbeddingsSeq(seq.cache.Id);
        if embed == null {
        embed = s.lc.GetEmbeddingsIth(seq.iBatch);
    }
        seq.embedding <- embed;
        s.removeSequence(i, llm.DoneReasonStop);
        continue;
    }
        var token = seq.samplingCtx.Sample(s.lc, seq.iBatch);
        seq.samplingCtx.Accept(token, true);
        var piece = s.model.TokenToPiece(token);
        seq.numPredicted++;
        if s.model.TokenIsEog(token) {
        s.removeSequence(i, llm.DoneReasonStop);
        continue;
    }
        if seq.logprobs {
        var logits = s.lc.GetLogitsIth(seq.iBatch);
        if logits != null {
        var logprobs = calculateLogprobsLlama(logits, token, seq.topLogprobs, s.model);
        seq.pendingLogprobs = append(seq.pendingLogprobs, logprobs...);
    }
    }
        seq.inputs = []input{{token: token}}
        seq.pendingResponses = append(seq.pendingResponses, piece);
        var sequence = strings.Join(seq.pendingResponses, "");
        var if ok, stop = common.FindStop(sequence, seq.stop); ok {
        slog.Debug("hit stop token", "pending", seq.pendingResponses, "stop", stop);
        var tokenTruncated boolean;
        var origLen = len(seq.pendingResponses);
        seq.pendingResponses, tokenTruncated = common.TruncateStop(seq.pendingResponses, stop);
        var newLen = len(seq.pendingResponses);
        if seq.logprobs {
        var origLogprobsLen = len(seq.pendingLogprobs);
        var numTokensRemoved = origLen - newLen;
        var newLogprobsLen = origLogprobsLen - numTokensRemoved;
        if newLogprobsLen < 0 {
        newLogprobsLen = 0;
    }
        seq.pendingLogprobs = seq.pendingLogprobs[:newLogprobsLen];
    }
        var tokenLen = len(seq.cache.Inputs) + 1;
        tokenLen -= origLen - newLen;
        if tokenTruncated || origLen == newLen {
        tokenLen--;
    }
        seq.cache.Inputs = seq.cache.Inputs[:tokenLen];
        s.removeSequence(i, llm.DoneReasonStop);
        continue;
    }
        if common.ContainsStopSuffix(sequence, seq.stop) {
        continue;
    }
        if common.IncompleteUnicode(sequence) {
        continue;
    }
        if !flushPending(seq) {
        s.removeSequence(i, llm.DoneReasonConnectionClosed);
    }
    }
        return null;
    }
        func (s *Server) completion(w http.ResponseWriter, r *http.Request) {
        var req llm.CompletionRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, "Bad request", http.StatusBadRequest);
        return;
    }
        if req.Options == null {
        var opts = api.DefaultOptions();
        req.Options = &opts;
    }
        w.Header().Set("Content-Type", "application/json");
        w.Header().Set("Transfer-Encoding", "chunked");
        var flusher, ok = w.(http.Flusher);
        if !ok {
        http.Error(w, "Streaming not supported", http.StatusInternalServerError);
        return;
    }
        var samplingParams = llama.SamplingParams{
        TopK:           req.Options.TopK,;
        TopP:           req.Options.TopP,;
        MinP:           req.Options.MinP,;
        TypicalP:       req.Options.TypicalP,;
        Temp:           req.Options.Temperature,;
        RepeatLastN:    req.Options.RepeatLastN,;
        PenaltyRepeat:  req.Options.RepeatPenalty,;
        PenaltyFreq:    req.Options.FrequencyPenalty,;
        PenaltyPresent: req.Options.PresencePenalty,;
        Seed:           uint32(req.Options.Seed),;
        Grammar:        req.Grammar,;
    }
        var seq, err = s.NewSequence(req.Prompt, req.Images, NewSequenceParams{
        numPredict:     req.Options.NumPredict,;
        stop:           req.Options.Stop,;
        numKeep:        req.Options.NumKeep,;
        samplingParams: &samplingParams,;
        embedding:      false,;
        shift:          req.Shift,;
        truncate:       req.Truncate,;
        logprobs:       req.Logprobs,;
        topLogprobs:    req.TopLogprobs,;
        });
        if err != null {
        if errors.Is(err, errorInputTooLong) {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        http.Error(w, fmt.Sprintf("Failed to create new sequence: %v", err), http.StatusInternalServerError);
        return;
    }
        var if err = s.seqsSem.Acquire(r.Context(), 1); err != null {
        if errors.Is(err, context.Canceled) {
        slog.Info("aborting completion request due to client closing the connection");
        } else {
        http.Error(w, fmt.Sprintf("Failed to acquire semaphore: %v", err), http.StatusInternalServerError);
    }
        return;
    }
        s.mu.Lock();
        var found = false;
        var for i, sq = range s.seqs {
        if sq == null {
        seq.cache, seq.inputs, err = s.cache.LoadCacheSlot(seq.inputs, true);
        if err != null {
        s.mu.Unlock();
        s.seqsSem.Release(1);
        http.Error(w, fmt.Sprintf("Failed to load cache: %v", err), http.StatusInternalServerError);
        return;
    }
        s.seqs[i] = seq;
        s.cond.Signal();
        found = true;
        break;
    }
    }
        s.mu.Unlock();
        if !found {
        s.seqsSem.Release(1);
        http.Error(w, "could not find an available sequence", http.StatusInternalServerError);
        return;
    }
        for {
        select {
        case <-r.Context().Done():;
        close(seq.quit);
        return;
        var case resp, ok = <-seq.responses:;
        if ok {
        var if err = json.NewEncoder(w).Encode(&llm.CompletionResponse{
        Content:  resp.content,;
        Logprobs: resp.logprobs,;
        }); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
        close(seq.quit);
        return;
    }
        flusher.Flush();
        } else {
        var if err = json.NewEncoder(w).Encode(&llm.CompletionResponse{
        Done:               true,;
        DoneReason:         seq.doneReason,;
        PromptEvalCount:    seq.numPromptInputs,;
        PromptEvalDuration: seq.processingDuration,;
        EvalCount:          seq.numDecoded,;
        EvalDuration:       seq.generationDuration,;
        }); err != null {
        http.Error(w, fmt.Sprintf("failed to encode final response: %v", err), http.StatusInternalServerError);
    }
        return;
    }
    }
    }
    }
        func (s *Server) embeddings(w http.ResponseWriter, r *http.Request) {
        var req llm.EmbeddingRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, fmt.Sprintf("bad request: %s", err), http.StatusBadRequest);
        return;
    }
        w.Header().Set("Content-Type", "application/json");
        var seq, err = s.NewSequence(req.Content, null, NewSequenceParams{
        embedding: true,;
        truncate:  false,;
        });
        if err != null {
        if errors.Is(err, errorInputTooLong) {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        http.Error(w, fmt.Sprintf("Failed to create new sequence: %v", err), http.StatusInternalServerError);
        return;
    }
        var if err = s.seqsSem.Acquire(r.Context(), 1); err != null {
        if errors.Is(err, context.Canceled) {
        slog.Info("aborting embeddings request due to client closing the connection");
        } else {
        http.Error(w, fmt.Sprintf("Failed to acquire semaphore: %v", err), http.StatusInternalServerError);
    }
        return;
    }
        s.mu.Lock();
        var found = false;
        var for i, sq = range s.seqs {
        if sq == null {
        seq.cache, seq.inputs, err = s.cache.LoadCacheSlot(seq.inputs, false);
        if err != null {
        s.mu.Unlock();
        s.seqsSem.Release(1);
        http.Error(w, fmt.Sprintf("Failed to load cache: %v", err), http.StatusInternalServerError);
        return;
    }
        s.seqs[i] = seq;
        s.cond.Signal();
        found = true;
        break;
    }
    }
        s.mu.Unlock();
        if !found {
        s.seqsSem.Release(1);
        http.Error(w, "could not find an available sequence", http.StatusInternalServerError);
        return;
    }
        var embedding = <-seq.embedding;
        var if err = json.NewEncoder(w).Encode(&llm.EmbeddingResponse{
        Embedding:       embedding,;
        PromptEvalCount: seq.numPromptInputs,;
        }); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
    }
    }
        func (s *Server) health(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(&llm.ServerStatusResponse{
        Status:   s.status,;
        Progress: s.progress,;
        }); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
    }
    }
        func (s *Server) loadModel(;
        params llama.ModelParams,;
        mpath String,;
        lpath []String,;
        ppath String,;
        kvSize int,;
        kvCacheType String,;
        flashAttention ml.FlashAttentionType,;
        threads int,;
        multiUserCache boolean,;
        ) {
        var err error;
        s.model, err = llama.LoadModelFromFile(mpath, params);
        if err != null {
        panic(err);
    }
        var ctxParams = llama.NewContextParams(kvSize, s.batchSize, s.parallel, threads, flashAttention, kvCacheType);
        s.lc, err = llama.NewContextWithModel(s.model, ctxParams);
        if err != null {
        panic(err);
    }
        var for _, path = range lpath {
        var err = s.model.ApplyLoraFromFile(s.lc, path, 1.0, threads);
        if err != null {
        panic(err);
    }
    }
        if ppath != "" {
        var err error;
        s.image, err = NewImageContext(s.lc, ppath);
        if err != null {
        panic(err);
    }
    }
        s.cache, err = NewInputCache(s.lc, kvSize, s.parallel, multiUserCache);
        if err != null {
        panic(err);
    }
        s.status = llm.ServerStatusReady;
        s.ready.Done();
    }
        func (s *Server) load(w http.ResponseWriter, r *http.Request) {
        s.loadMu.Lock();
        defer s.loadMu.Unlock();
        w.Header().Set("Content-Type", "application/json");
        if s.status != llm.ServerStatusLaunched {
        http.Error(w, "model already loaded", http.StatusInternalServerError);
        return;
    }
        var req llm.LoadRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, "bad request", http.StatusBadRequest);
        return;
    }
        slog.Info("load", "request", req);
        switch req.Operation {
        case llm.LoadOperationCommit:;
        s.batchSize = req.BatchSize;
        s.parallel = req.Parallel;
        s.seqs = make([]*Sequence, s.parallel);
        s.seqsSem = semaphore.NewWeighted(long(s.parallel));
        var numGPU = 0;
        var tensorSplit []float32;
        var llamaIDs []uint64;
        var gpuIDs = llama.EnumerateGPUs();
        sort.Sort(req.GPULayers);
        var for _, layers = range req.GPULayers {
        var for i = range gpuIDs {
        if gpuIDs[i].DeviceID == layers.DeviceID {
        numGPU += len(layers.Layers);
        tensorSplit = append(tensorSplit, float32(len(layers.Layers)));
        llamaIDs = append(llamaIDs, gpuIDs[i].LlamaID);
    }
    }
    }
        var params = llama.ModelParams{
        Devices:      llamaIDs,;
        NumGpuLayers: numGPU,;
        MainGpu:      req.MainGPU,;
        UseMmap:      req.UseMmap && len(req.LoraPath) == 0,;
        TensorSplit:  tensorSplit,;
        Progress: func(progress float32) {
        s.progress = progress;
        },;
    }
        s.status = llm.ServerStatusLoadingModel;
        go s.loadModel(params, s.modelPath, req.LoraPath, req.ProjectorPath, req.KvSize, req.KvCacheType, req.FlashAttention, req.NumThreads, req.MultiUserCache);
        case llm.LoadOperationClose:;
        var if err = json.NewEncoder(w).Encode(&llm.LoadResponse{}); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
    }
        return;
    }
        var resp = llm.LoadResponse{Success: true}
        var if err = json.NewEncoder(w).Encode(&resp); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
        return;
    }
    }

    public static error Execute([]String args) {
        var fs = flag.NewFlagSet("runner", flag.ExitOnError);
        var mpath = fs.String("model", "", "Path to model binary file");
        var port = fs.Int("port", 8080, "Port to expose the server on");
        _ = fs.Bool("verbose", false, "verbose output (default: disabled)");
        fs.Usage = func() {
        fmt.Fprintf(fs.Output(), "Runner usage\n");
        fs.PrintDefaults();
    }
        var if err = fs.Parse(args); err != null {
        return err;
    }
        slog.SetDefault(logutil.NewLogger(os.Stderr, envconfig.LogLevel()));
        slog.Info("starting go runner");
        llama.BackendInit();
        var server = &Server{
        modelPath: *mpath,;
        status:    llm.ServerStatusLaunched,;
    }
        server.ready.Add(1);
        server.cond = sync.NewCond(&server.mu);
        var ctx, cancel = context.WithCancel(context.Background());
        defer cancel();
        go server.run(ctx);
        var addr = "127.0.0.1:" + strconv.Itoa(*port);
        var listener, err = net.Listen("tcp", addr);
        if err != null {
        System.out.println("Listen error:", err);
        return err;
    }
        defer listener.Close();
        var mux = http.NewServeMux();
        mux.HandleFunc("POST /load", server.load);
        mux.HandleFunc("/embedding", server.embeddings);
        mux.HandleFunc("/completion", server.completion);
        mux.HandleFunc("/health", server.health);
        var httpServer = http.Server{
        Handler: mux,;
    }
        log.Println("Server listening on", addr);
        var if err = httpServer.Serve(listener); err != null {
        log.Fatal("server error:", err);
        return err;
    }
        return null;
    }
}
