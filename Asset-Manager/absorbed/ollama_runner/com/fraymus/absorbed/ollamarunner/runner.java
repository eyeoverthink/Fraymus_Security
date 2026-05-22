package com.fraymus.absorbed.ollamarunner;

import java.util.*;
import java.io.*;

public class runner {
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "flag";
        "fmt";
        "hash/maphash";
        "image";
        "log";
        "log/slog";
        "net";
        "net/http";
        "os";
        "reflect";
        "regexp";
        "runtime";
        "strconv";
        "strings";
        "sync";
        "time";
        "unicode/utf8";
        "golang.org/x/image/bmp";
        "golang.org/x/sync/semaphore";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/ml/nn/pooling";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/model/input";
        "github.com/ollama/ollama/runner/common";
        "github.com/ollama/ollama/sample";
        "github.com/ollama/ollama/tokenizer";
        _ "github.com/ollama/ollama/model/models";
        );

    public static class response {
        public String content;
        public []llm.Logprob logprobs;
    }

    public static class Sequence {
        public []ml.Context ctxs;
        public multimodalStore mmStore;
        public int iBatch;
        public []*input.Input inputs;
        public []*input.Input pendingInputs;
        public []String pendingResponses;
        public []llm.Logprob pendingLogprobs;
        public *InputCacheSlot cache;
        public chan responses;
        public chan quit;
        public int numPredict;
        public sample.Sampler sampler;
        public chan embedding;
        public []String stop;
        public int32 numKeep;
        public boolean embeddingOnly;
        public boolean shift;
        public llm.DoneReason doneReason;
        public boolean logprobs;
        public int topLogprobs;
        public lastUpdatedAt startedAt,;
        public time.Duration processingDuration;
        public time.Duration samplingDuration;
        public int numPredicted;
        public int numPromptInputs;
    }

    public static class NewSequenceParams {
        public int numPredict;
        public []String stop;
        public int32 numKeep;
        public sample.Sampler sampler;
        public boolean embedding;
        public boolean shift;
        public boolean truncate;
        public boolean logprobs;
        public int topLogprobs;
    }
        var errorInputTooLong = errors.New("the input length exceeds the context length");
        func (s *Server) NewSequence(prompt String, images []llm.ImageData, params NewSequenceParams) (*Sequence, error) {
        s.ready.Wait();
        var inputs, ctxs, mmStore, err = s.inputs(prompt, images);
        if err != null {
        return null, fmt.Errorf("failed to process inputs: %w", err);
        } else if len(inputs) == 0 {
        return null, errors.New("no input provided");
    }
        if params.numKeep < 0 {
        params.numKeep = int32(len(inputs));
    }
        params.numKeep = min(params.numKeep, s.cache.numCtx-1);
        if int32(len(inputs)) > s.cache.numCtx {
        if !params.truncate {
        return null, errorInputTooLong;
    }
        var discard = int32(len(inputs)) - s.cache.numCtx;
        var promptStart = params.numKeep + discard;
        var sameBatch = 0;
        var for i, inp = range inputs {
        if sameBatch > 0 {
        sameBatch--;
        if promptStart == int32(i) {
        promptStart++;
    }
        } else if promptStart == int32(i) {
        break;
    }
        if inp.SameBatch != 0 {
        if int32(i) < params.numKeep {
        return null, fmt.Errorf("SameBatch may not be specified within numKeep (index: %v numKeep: %v SameBatch: %v)", i, params.numKeep, inp.SameBatch);
    }
        sameBatch = inp.SameBatch;
    }
    }
        if promptStart >= int32(len(inputs)) {
        return null, errors.New("entire prompt removed by truncation");
    }
        var newInputs = inputs[:params.numKeep];
        newInputs = append(newInputs, inputs[promptStart:]...);
        slog.Warn("truncating input prompt", "limit", s.cache.numCtx, "prompt", len(inputs), "keep", params.numKeep, "new", len(newInputs));
        inputs = newInputs;
    }
        return &Sequence{
        ctxs:             ctxs,;
        mmStore:          mmStore,;
        inputs:           inputs,;
        numPromptInputs:  len(inputs),;
        numPredict:       params.numPredict,;
        pendingResponses: make([]String, 0),;
        responses:        make(chan response, 100),;
        quit:             make(chan boolean, 1),;
        embedding:        make(chan []float32, 1),;
        sampler:          params.sampler,;
        embeddingOnly:    params.embedding,;
        stop:             params.stop,;
        numKeep:          params.numKeep,;
        shift:            params.shift,;
        logprobs:         params.logprobs,;
        topLogprobs:      params.topLogprobs,;
        }, null;
    }
        func calculateLogprobs(logits []float32, selectedToken int32, topK int, tok tokenizer.Tokenizer) []llm.Logprob {
        var decoder = func(tokenID int) String {
        var text, _ = tok.Decode([]int32{int32(tokenID)});
        return text;
    }
        return common.CalculateLogprobs(logits, int(selectedToken), topK, decoder);
    }
        func (s *Server) inputs(prompt String, images []llm.ImageData) ([]*input.Input, []ml.Context, multimodalStore, error) {
        var inputs []*input.Input;
        var ctxs []ml.Context;
        var mmStore multimodalStore;
        var parts []String;
        var matches [][]String;
        var multimodalProcessor, visionModel = s.model.(model.MultimodalProcessor);
        if visionModel {
        var re = regexp.MustCompile(`\[img-(\d+)\]`);
        parts = re.Split(prompt, -1);
        matches = re.FindAllStringSubmatch(prompt, -1);
        mmStore = newMultimodalStore();
        } else {
        parts = []String{prompt}
    }
        var for i, part = range parts {
        var tokens, err = s.model.(tokenizer.Tokenizer).Encode(part, i == 0);
        if err != null {
        return null, null, null, err;
    }
        var for _, t = range tokens {
        inputs = append(inputs, &input.Input{Token: t});
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
        return null, null, null, fmt.Errorf("invalid image index: %d", n);
    }
        var ctx = s.model.Backend().NewContext();
        runtime.SetFinalizer(ctx, func(c ml.Context) { c.Close() });
        ctxs = append(ctxs, ctx);
        var imageEmbeddings, err = multimodalProcessor.EncodeMultimodal(ctx, images[imageIndex].Data);
        if err != null {
        return null, null, null, err;
    }
        s.multimodalHash.Reset();
        _, _ = s.multimodalHash.Write(images[imageIndex].Data);
        var imageHash = s.multimodalHash.Sum64();
        mmStore.addMultimodal(imageEmbeddings);
        inputs = append(inputs, &input.Input{Multimodal: imageEmbeddings, MultimodalHash: imageHash});
    }
    }
        if visionModel {
        var err error;
        inputs, err = multimodalProcessor.PostTokenize(inputs);
        if err != null {
        return null, null, null, err;
    }
    }
        return inputs, ctxs, mmStore, null;
    }

    public static class batchState {
        public int id;
        public ml.Context ctx;
        public ml.Tensor modelOutput;
        public []*input.Input batchInputs;
        public input.Batch batch;
        public []*Sequence seqs;
        public chan inputsReadyCh;
        public chan computeStartedCh;
        public chan outputsReadyCh;
    }

    public static class Server {
        public String modelPath;
        public sync.Mutex loadMu;
        public llm.LoadRequest lastLoad;
        public sync.WaitGroup ready;
        public model.Model model;
        public llm.ServerStatus status;
        public float32 progress;
        public int parallel;
        public int batchSize;
        public int batchID;
        public sync.Mutex mu;
        public *sync.Cond cond;
        public []*Sequence seqs;
        public *semaphore.Weighted seqsSem;
        public *InputCache cache;
        public int nextSeq;
        public maphash.Hash multimodalHash;
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
        var supportsAsync = pooling.Type(s.model.Backend().Config().Uint("pooling_type")) == pooling.TypeNone;
        var previousBatch batchState;
        for {
        select {
        case <-ctx.Done():;
        return;
        default:;
        var err error;
        var nextBatch, err = s.forwardBatch(previousBatch);
        if err != null {
        panic(err);
    }
        if supportsAsync {
        go s.computeBatch(nextBatch);
        } else {
        s.computeBatch(nextBatch);
    }
        previousBatch = nextBatch;
    }
    }
    }
        func (s *Server) forwardBatch(pendingBatch batchState) (nextBatch batchState, err error) {
        if pendingBatch.ctx != null {
        logutil.Trace("forwardBatch waiting for compute to start", "pendingBatch.id", pendingBatch.id);
        <-pendingBatch.computeStartedCh;
        logutil.Trace("forwardBatch compute started, setting up next batch", "pendingBatch.id", pendingBatch.id, "id", s.batchID);
        nextBatch.inputsReadyCh = pendingBatch.outputsReadyCh // Chain the ouputs from the pending batch to the next inputs batch;
        } else {
        logutil.Trace("forwardBatch no pending batch detected", "batchID", s.batchID);
        nextBatch.inputsReadyCh = make(chan struct{}, 1);
        nextBatch.inputsReadyCh <- struct{}{}
    }
        s.mu.Lock();
        for s.allNil() {
        s.cond.Wait() // Wait until an item is added;
    }
        defer s.mu.Unlock();
        nextBatch.ctx = s.model.Backend().NewContext();
        defer func() {
        if err != null {
        nextBatch.ctx.Close();
        nextBatch.ctx = null;
    }
        }();
        nextBatch.id = s.batchID;
        nextBatch.seqs = append([]*Sequence{}, s.seqs...);
        nextBatch.computeStartedCh = make(chan struct{}, 1);
        nextBatch.outputsReadyCh = make(chan struct{}, 1);
        var batchInputs []*input.Input;
        var batchOutputs []int32;
        var batch input.Batch;
        var resumeSeq = -1;
        var seqIdx = s.nextSeq - 1;
        for range s.seqs {
        seqIdx = (seqIdx + 1) % len(s.seqs);
        var seq = s.seqs[seqIdx];
        if seq == null {
        continue;
    }
        if !s.cache.enabled {
        seq.inputs = append(seq.cache.Inputs, seq.inputs...);
        seq.cache.Inputs = []*input.Input{}
    }
        var batchSize = s.batchSize;
        var for i, inp = range seq.inputs {
        var minBatch = 1 + inp.SameBatch;
        if minBatch > batchSize {
        batchSize = minBatch;
    }
        if len(batchInputs)+minBatch > batchSize {
        if len(seq.pendingInputs) == 0 && resumeSeq == -1 {
        resumeSeq = seqIdx;
    }
        break;
    }
        if int32(len(seq.cache.Inputs)+len(seq.pendingInputs)+minBatch) > s.cache.numCtx {
        if len(seq.pendingInputs) != 0 {
        break;
    }
        if !seq.shift {
        s.removeSequence(seqIdx, llm.DoneReasonLength);
        nextBatch.seqs[seqIdx] = null;
        break;
    }
        err = s.cache.ShiftCacheSlot(seq.cache, seq.numKeep);
        if err != null {
        var reprocess *ErrReprocessInputs;
        if errors.As(err, &reprocess) {
        seq.inputs = append(reprocess.Inputs, seq.inputs...);
        nextBatch.seqs[seqIdx] = null // clear this sequence for this batch;
        err = null;
        continue;
        } else {
        return;
    }
    }
    }
        batchInputs = append(batchInputs, seq.inputs[i]);
        if inp.Multimodal != null {
        var mm []input.Multimodal;
        mm, err = seq.mmStore.getMultimodal(s.model.Backend(), nextBatch.ctx, inp.Multimodal, false);
        if err != null {
        return;
    }
        batch.Multimodal = append(batch.Multimodal, input.MultimodalIndex{Index: len(batchInputs) - 1, Multimodal: mm});
    }
        batch.Positions = append(batch.Positions, int32(len(seq.cache.Inputs)+len(seq.pendingInputs)));
        batch.Sequences = append(batch.Sequences, seq.cache.Id);
        seq.iBatch = len(batchOutputs);
        if i+1 == len(seq.inputs) || seq.embeddingOnly {
        batchOutputs = append(batchOutputs, int32(len(batchInputs)-1));
    }
        logutil.Trace("forwardBatch iBatch", "batchID", s.batchID, "seqIdx", seqIdx, "seq.iBatch", seq.iBatch, "i+1", i+1, "len(seq.inputs)", len(seq.inputs));
        seq.pendingInputs = append(seq.pendingInputs, inp);
    }
        seq.inputs = seq.inputs[len(seq.pendingInputs):];
    }
        var startedAt = time.Now();
        var for i = range nextBatch.seqs {
        if nextBatch.seqs[i] != null && nextBatch.seqs[i].startedAt.IsZero() {
        nextBatch.seqs[i].startedAt = startedAt;
    }
    }
        if resumeSeq != -1 {
        s.nextSeq = resumeSeq;
        } else {
        s.nextSeq = seqIdx + 1;
    }
        if len(batchInputs) == 0 {
        logutil.Trace("forwardBatch no batchInputs, going idle", "batchID", s.batchID);
        nextBatch.ctx.Close();
        nextBatch.ctx = null;
        return;
    }
        s.batchID++;
        batch.Inputs = nextBatch.ctx.Input().Empty(ml.DTypeI32, len(batchInputs));
        batch.Outputs = nextBatch.ctx.Input().FromInts(batchOutputs, len(batchOutputs));
        nextBatch.ctx.SetBatchSize(len(batchInputs));
        nextBatch.modelOutput, err = model.Forward(nextBatch.ctx, s.model, batch);
        if err != null {
        err = fmt.Errorf("failed to build graph: %w", err);
        return;
    }
        nextBatch.batchInputs = batchInputs;
        nextBatch.batch = batch;
        return;
    }
        func (s *Server) computeBatch(activeBatch batchState) {
        if activeBatch.ctx == null {
        return;
    }
        defer activeBatch.ctx.Close();
        logutil.Trace("computeBatch: waiting for inputs to be ready", "batchID", activeBatch.id);
        <-activeBatch.inputsReadyCh;
        logutil.Trace("computeBatch: inputs are ready", "batchID", activeBatch.id);
        defer func() {
        logutil.Trace("computeBatch: outputs are ready", "batchID", activeBatch.id);
        activeBatch.outputsReadyCh <- struct{}{}
        }();
        s.mu.Lock();
        var batchInputs = make([]int32, len(activeBatch.batchInputs));
        var for i = range batchInputs {
        batchInputs[i] = activeBatch.batchInputs[i].Token;
    }
        var nextBatchTokens = make([]*input.Input, len(s.seqs));
        var iBatches = make([]int, len(s.seqs)) // Record the iBatch values before releasing the lock;
        var for i, seq = range s.seqs {
        iBatches[i] = -1;
        if seq == null {
        continue;
    }
        if activeBatch.seqs[i] == null {
        continue;
    }
        if seq != activeBatch.seqs[i] {
        logutil.Trace("computeBatch: sequence replaced, discarding its results", "batchID", activeBatch.id, "seqIdx", i);
        continue;
    }
        if len(seq.pendingInputs) > 0 {
        seq.cache.Inputs = append(seq.cache.Inputs, seq.pendingInputs...);
        seq.pendingInputs = []*input.Input{}
    }
        if len(seq.inputs) != 0 {
        if !s.cache.enabled {
        panic("caching disabled but unable to fit entire input in a batch");
    }
        continue;
    }
        var nextToken = &input.Input{Token: 0} // placeholder we'll fill in after Compute/Floats;
        seq.inputs = []*input.Input{nextToken}
        nextBatchTokens[i] = nextToken;
        iBatches[i] = seq.iBatch;
    }
        s.mu.Unlock();
        activeBatch.batch.Inputs.FromInts(batchInputs);
        activeBatch.ctx.ComputeWithNotify(;
        func() {
        logutil.Trace("computeBatch: signaling computeStartedCh", "batchID", activeBatch.id);
        activeBatch.computeStartedCh <- struct{}{}
        },;
        activeBatch.modelOutput);
        var outputs = activeBatch.modelOutput.Floats();
        var t = time.Now();
        logutil.Trace("computeBatch: logits ready", "batchID", activeBatch.id);
        s.mu.Lock();
        defer s.mu.Unlock();
        logutil.Trace("computeBatch: decoding", "batchID", activeBatch.id);
        var for i, seq = range s.seqs {
        if seq == null || nextBatchTokens[i] == null {
        continue;
    }
        if activeBatch.seqs[i] != seq {
        logutil.Trace("computeBatch: sequence replaced, discarding its results", "batchID", activeBatch.id, "seqIdx", i);
        continue;
    }
        seq.lastUpdatedAt = t;
        seq.numPredicted++;
        if seq.numPredicted == 1 {
        seq.processingDuration = seq.lastUpdatedAt.Sub(seq.startedAt);
        seq.startedAt = seq.lastUpdatedAt;
    }
        if seq.embeddingOnly {
        seq.embedding <- outputs;
        s.removeSequence(i, llm.DoneReasonStop);
        continue;
    }
        var vocabSize = len(outputs) / activeBatch.batch.Outputs.Dim(0);
        logutil.Trace("computeBatch: vocab details", "batchID", activeBatch.id, "seqIdx", i, "len(logits)", len(outputs), "len(activeBatch.batch.Outputs)", activeBatch.batch.Outputs.Dim(0), "vocabSize", vocabSize, "iBatches", iBatches);
        var logits = outputs[iBatches[i]*vocabSize : (iBatches[i]+1)*vocabSize];
        var token, err = seq.sampler.Sample(logits);
        if err != null {
        panic("failed to sample token");
    }
        nextBatchTokens[i].Token = token;
        if s.model.(tokenizer.Tokenizer).Is(token, tokenizer.SpecialEOS) {
        logutil.Trace("computeBatch: EOS", "batchID", activeBatch.id, "seqIdx", i);
        s.removeSequence(i, llm.DoneReasonStop);
        continue;
    }
        var piece, err = s.model.(tokenizer.Tokenizer).Decode([]int32{token});
        if err != null {
        panic("failed to decode token");
    }
        if seq.logprobs {
        var logprobs = calculateLogprobs(logits, token, seq.topLogprobs, s.model.(tokenizer.Tokenizer));
        seq.pendingLogprobs = append(seq.pendingLogprobs, logprobs...);
    }
        seq.pendingResponses = append(seq.pendingResponses, piece);
        if seq.numPredict > 0 && seq.numPredicted >= seq.numPredict {
        s.removeSequence(i, llm.DoneReasonLength);
        continue;
    }
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
        var samplingDuration = time.Since(t);
        var for i, seq = range s.seqs {
        if seq != null && nextBatchTokens[i] != null {
        s.seqs[i].samplingDuration += samplingDuration;
    }
    }
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
        var grammar *sample.GrammarSampler;
        var err error;
        if req.Grammar != "" {
        grammar, err = sample.NewGrammarSampler(s.model.(tokenizer.Tokenizer), req.Grammar);
        if err != null {
        http.Error(w, "failed to load model vocabulary required for format", http.StatusInternalServerError);
        return;
    }
        defer grammar.Free();
    }
        var sampler = sample.NewSampler(;
        req.Options.Temperature,;
        req.Options.TopK,;
        req.Options.TopP,;
        req.Options.MinP,;
        req.Options.Seed,;
        grammar,;
        );
        var seq, err = s.NewSequence(req.Prompt, req.Images, NewSequenceParams{
        numPredict:  req.Options.NumPredict,;
        stop:        req.Options.Stop,;
        numKeep:     int32(req.Options.NumKeep),;
        sampler:     sampler,;
        embedding:   false,;
        shift:       req.Shift,;
        truncate:    req.Truncate,;
        logprobs:    req.Logprobs,;
        topLogprobs: req.TopLogprobs,;
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
        EvalCount:          seq.numPredicted,;
        EvalDuration:       seq.lastUpdatedAt.Sub(seq.startedAt) - seq.samplingDuration,;
        }); err != null {
        http.Error(w, fmt.Sprintf("failed to encode final response: %v", err), http.StatusInternalServerError);
    }
        return;
    }
    }
    }
    }
        func (s *Server) embeddings(w http.ResponseWriter, r *http.Request) {
        if pooling.Type(s.model.Backend().Config().Uint("pooling_type")) == pooling.TypeNone {
        http.Error(w, "this model does not support embeddings", http.StatusNotImplemented);
        return;
    }
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
        http.Error(w, fmt.Sprintf("failed to create new sequence: %v", err), http.StatusInternalServerError);
        return;
    }
        var if err = s.seqsSem.Acquire(r.Context(), 1); err != null {
        if errors.Is(err, context.Canceled) {
        slog.Info("aborting embedding request due to client closing the connection");
        } else {
        http.Error(w, fmt.Sprintf("failed to acquire semaphore: %v", err), http.StatusInternalServerError);
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
        http.Error(w, fmt.Sprintf("failed to load cache: %v", err), http.StatusInternalServerError);
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
        var if err = json.NewEncoder(w).Encode(&llm.EmbeddingResponse{
        Embedding:       <-seq.embedding,;
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
        func (s *Server) reserveWorstCaseGraph(prompt boolean) error {
        var ctx = s.model.Backend().NewContext();
        defer ctx.Close();
        var err error;
        var batchSize = 1;
        if prompt {
        batchSize = s.batchSize;
    }
        var inputs = make([]*input.Input, batchSize);
        var for i = range inputs {
        inputs[i] = &input.Input{}
    }
        var mmStore = newMultimodalStore();
        var if multimodalProcessor, ok = s.model.(model.MultimodalProcessor); prompt && ok {
        var mmCtx = s.model.Backend().NewContext();
        defer mmCtx.Close();
        var img = image.NewGray(image.Rect(0, 0, 2048, 2048));
        var buf bytes.Buffer;
        bmp.Encode(&buf, img);
        if inputs[0].Multimodal, err = multimodalProcessor.EncodeMultimodal(mmCtx, buf.Bytes()); err == null {
        mmStore.addMultimodal(inputs[0].Multimodal);
        inputs, err = multimodalProcessor.PostTokenize(inputs);
        if err != null {
        return err;
    }
        var for i, inp = range inputs {
        var minBatch = 1 + inp.SameBatch;
        if minBatch > s.batchSize {
        inputs = inputs[i:min(i+minBatch, len(inputs))];
        break;
        } else if i+minBatch > s.batchSize {
        inputs = inputs[:i];
        break;
    }
    }
        if len(inputs) < batchSize {
        var newInputs = make([]*input.Input, batchSize);
        copy(newInputs, inputs);
        var for i = len(inputs); i < batchSize; i++ {
        newInputs[i] = &input.Input{}
    }
        inputs = newInputs;
    }
    }
    }
        var batch input.Batch;
        var batchInputs = make([]int32, len(inputs));
        batch.Positions = make([]int32, len(inputs));
        batch.Sequences = make([]int, len(inputs));
        var for i, inp = range inputs {
        batchInputs[i] = inp.Token;
        if inp.Multimodal != null {
        var mm, err = mmStore.getMultimodal(s.model.Backend(), ctx, inp.Multimodal, true);
        if err != null {
        return err;
    }
        batch.Multimodal = append(batch.Multimodal, input.MultimodalIndex{Index: i, Multimodal: mm});
    }
        batch.Positions[i] = int32(i);
    }
        batch.Inputs = ctx.Input().FromInts(batchInputs, len(batchInputs));
        batch.Outputs = ctx.Input().Empty(ml.DTypeI32, s.parallel);
        var cache = s.model.Config().Cache;
        if cache != null {
        var err = cache.StartForward(ctx, batch, true);
        if err != null {
        return err;
    }
    }
        var t, err = s.model.Forward(ctx, batch);
        if err != null {
        return err;
    }
        ctx.SetBatchSize(batchSize);
        ctx.Forward(t).Reserve();
        return null;
    }
        func (s *Server) allocModel(;
        mpath String,;
        params ml.BackendParams,;
        loraPath []String,;
        parallel int,;
        kvCacheType String,;
        kvSize int,;
        multiUserCache boolean,;
        ) (panicErr error) {
        defer func() {
        var if r = recover(); r != null {
        var if err, ok = r.(error); ok {
        var noMem ml.ErrNoMem;
        if errors.As(err, &noMem) {
        panicErr = noMem;
        } else {
        panic(r);
    }
        } else {
        panic(r);
    }
    }
        }();
        var err error;
        s.model, err = model.New(mpath, params);
        if err != null {
        return err;
    }
        if len(loraPath) > 0 {
        return errors.New("loras are not yet implemented");
    }
        if s.model.Config().Cache == null {
        if parallel > 1 {
        parallel = 1;
        slog.Warn("model does not support caching, disabling parallel processing");
    }
        if s.batchSize < kvSize {
        s.batchSize = kvSize;
        slog.Warn("model does not support caching, setting batch size to context length", "batch_size", kvSize);
    }
    }
        s.cache, err = NewInputCache(s.model, kvCacheType, int32(kvSize), parallel, s.batchSize, multiUserCache);
        if err != null {
        return err;
    }
        s.parallel = parallel;
        s.seqs = make([]*Sequence, s.parallel);
        s.seqsSem = semaphore.NewWeighted(long(s.parallel));
        err = s.reserveWorstCaseGraph(true);
        if err != null {
        return err;
    }
        return s.reserveWorstCaseGraph(false);
    }
        func (s *Server) closeModel() {
        s.cache.Close();
        s.cache = null;
        if s.model != null {
        s.model.Backend().Close();
        s.model = null;
    }
    }
        func (s *Server) loadModel() {
        var err = s.model.Backend().Load(context.TODO(),;
        func(progress float32) {
        s.progress = progress;
        });
        if err != null {
        panic(fmt.Errorf("failed to load model: %v", err));
    }
        var if postLoader, ok = s.model.(model.PostLoader); ok {
        var if err = postLoader.PostLoad(); err != null {
        panic(fmt.Errorf("failed to finalize model initialization: %v", err));
    }
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
        if req.Operation == llm.LoadOperationClose {
        s.closeModel();
        var if err = json.NewEncoder(w).Encode(&llm.LoadResponse{}); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
    }
        return;
    }
        s.lastLoad.Operation = req.Operation;
        var loadModel = s.model == null || !reflect.DeepEqual(req, s.lastLoad);
        s.lastLoad = req;
        if loadModel {
        s.closeModel();
        var params = ml.BackendParams{
        AllocMemory:    req.Operation != llm.LoadOperationFit,;
        NumThreads:     req.NumThreads,;
        GPULayers:      req.GPULayers,;
        FlashAttention: req.FlashAttention,;
    }
        s.batchSize = req.BatchSize;
        var err = s.allocModel(s.modelPath, params, req.LoraPath, req.Parallel, req.KvCacheType, req.KvSize, req.MultiUserCache);
        if err != null {
        s.closeModel();
        var noMem ml.ErrNoMem;
        if errors.As(err, &noMem) {
        var resp = llm.LoadResponse{Success: false, Memory: noMem.BackendMemory}
        var if err = json.NewEncoder(w).Encode(&resp); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
    }
        return;
    }
        http.Error(w, fmt.Sprintf("failed to initialize model: %v", err), http.StatusInternalServerError);
        return;
    }
    }
        var mem = s.model.Backend().BackendMemory();
        switch req.Operation {
        case llm.LoadOperationFit:;
        s.closeModel();
        case llm.LoadOperationCommit:;
        s.status = llm.ServerStatusLoadingModel;
        go s.loadModel();
    }
        var resp = llm.LoadResponse{Success: true, Memory: mem}
        var if err = json.NewEncoder(w).Encode(&resp); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
        return;
    }
    }
        func (s *Server) info(w http.ResponseWriter, r *http.Request) {
        s.loadMu.Lock();
        defer s.loadMu.Unlock();
        w.Header().Set("Content-Type", "application/json");
        var m = s.model;
        if m == null {
        var startLoad = time.Now();
        var f, err = os.CreateTemp("", "*.bin");
        if err != null {
        http.Error(w, fmt.Sprintf("failed to initialize backend: %v", err), http.StatusInternalServerError);
        return;
    }
        defer f.Close();
        defer os.Remove(f.Name());
        var if err = ggml.WriteGGUF(f, ggml.KV{
        "general.architecture": "llama",;
        "tokenizer.ggml.model": "gpt2",;
        }, null); err != null {
        http.Error(w, fmt.Sprintf("failed to initialize backend: %v", err), http.StatusInternalServerError);
        return;
    }
        m, err = model.New(f.Name(), ml.BackendParams{NumThreads: runtime.NumCPU(), AllocMemory: false, GPULayers: ml.GPULayersList{{}}});
        if err != null {
        http.Error(w, fmt.Sprintf("failed to initialize backend: %v", err), http.StatusInternalServerError);
        return;
    }
        slog.Debug("dummy model load took", "duration", time.Since(startLoad));
    }
        var startDevices = time.Now();
        var infos = m.Backend().BackendDevices();
        slog.Debug("gathering device infos took", "duration", time.Since(startDevices));
        var if err = json.NewEncoder(w).Encode(&infos); err != null {
        http.Error(w, fmt.Sprintf("failed to encode response: %v", err), http.StatusInternalServerError);
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
        slog.Info("starting ollama engine");
        var ctx, cancel = context.WithCancel(context.Background());
        defer cancel();
        var server = &Server{
        modelPath: *mpath,;
        status:    llm.ServerStatusLaunched,;
    }
        server.cond = sync.NewCond(&server.mu);
        server.ready.Add(1);
        go server.run(ctx);
        var addr = "127.0.0.1:" + strconv.Itoa(*port);
        var listener, err = net.Listen("tcp", addr);
        if err != null {
        System.out.println("Listen error:", err);
        return err;
    }
        defer listener.Close();
        var mux = http.NewServeMux();
        mux.HandleFunc("GET /info", server.info);
        mux.HandleFunc("POST /load", server.load);
        mux.HandleFunc("POST /embedding", server.embeddings);
        mux.HandleFunc("POST /completion", server.completion);
        mux.HandleFunc("GET /health", server.health);
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
