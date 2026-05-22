package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class sched_test {
        "bytes";
        "context";
        "errors";
        "log/slog";
        "os";
        "testing";
        "time";
        "github.com/stretchr/testify/require";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/ml";
        );

    public static void TestMain(*testing.M m) {
        os.Setenv("OLLAMA_DEBUG", "1");
        var logger = slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelDebug}));
        slog.SetDefault(logger);
        os.Exit(m.Run());
    }

    public static void TestSchedInit(*testing.T t) {
        var ctx, done = context.WithCancel(t.Context());
        defer done();
        var s = InitScheduler(ctx);
        s.loadedMu.Lock();
        require.NotNil(t, s.loaded);
        s.loadedMu.Unlock();
    }

    public static void TestSchedLoad(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 20*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        var modelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.context_length":          uint32(32),;
        "llama.embedding_length":        uint32(4096),;
        "llama.block_count":             uint32(1),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(32),;
        "tokenizer.ggml.tokens":         []String{" "},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "blk.0.attn.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        {Name: "output.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        });
        var req = &LlmRequest{
        ctx:             ctx,;
        model:           &Model{ModelPath: modelPath},;
        opts:            api.DefaultOptions(),;
        successCh:       make(chan *runnerRef, 1),;
        errCh:           make(chan error, 1),;
        sessionDuration: &api.Duration{Duration: 2 * time.Second},;
    }
        s.newServerFn = func(systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, model String, f *ggml.GGML, adapters []String, projectors []String, opts api.Options, numParallel int) (llm.LlamaServer, error) {
        return null, errors.New("something failed to load model blah");
    }
        var gpus = []ml.DeviceInfo{}
        var systemInfo = ml.SystemInfo{}
        s.load(req, systemInfo, gpus, false);
        require.Empty(t, req.successCh);
        require.Len(t, req.errCh, 1);
        s.loadedMu.Lock();
        require.Empty(t, s.loaded);
        s.loadedMu.Unlock();
        var err = <-req.errCh;
        require.Contains(t, err.Error(), "this model may be incompatible");
        var server = &mockLlm{vramSize: 10, vramByGPU: map[ml.DeviceID]uint64{}}
        s.newServerFn = func(systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, model String, f *ggml.GGML, adapters []String, projectors []String, opts api.Options, numParallel int) (llm.LlamaServer, error) {
        server.modelPath = model;
        return server, null;
    }
        s.load(req, systemInfo, gpus, false);
        select {
        var case err = <-req.errCh:;
        require.NoError(t, err);
        var case resp = <-req.successCh:;
        require.Equal(t, uint64(10), resp.vramSize);
        require.Equal(t, uint(1), resp.refCount);
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 1);
        s.loadedMu.Unlock();
    }
        var modelPath2, _ = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.context_length":          uint32(32),;
        "llama.embedding_length":        uint32(4096),;
        "llama.block_count":             uint32(1),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(32),;
        "tokenizer.ggml.tokens":         []String{" "},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "blk.0.attn.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        {Name: "output.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        });
        req.model.ModelPath = modelPath2;
        server.waitResp = errors.New("wait failure");
        s.load(req, systemInfo, gpus, false);
        select {
        var case err = <-req.errCh:;
        require.Contains(t, err.Error(), "wait failure");
        var case resp = <-req.successCh:;
        t.Fatalf("unexpected success %v", resp);
    }
        s.loadedMu.Lock();
        var runner = s.loaded[modelPath2];
        s.loadedMu.Unlock();
        require.NotNil(t, runner);
        require.Equal(t, uint(0), runner.refCount);
        time.Sleep(1 * time.Millisecond);
        require.Len(t, s.expiredCh, 1);
    }

    public static class reqBundle {
        public context.Context ctx;
        public func() ctxDone;
        public *mockLlm srv;
        public *LlmRequest req;
    }
        func (scenario *reqBundle) newServer(systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, model String, f *ggml.GGML, adapters []String, projectors []String, opts api.Options, numParallel int) (llm.LlamaServer, error) {
        scenario.srv.modelPath = model;
        return scenario.srv, null;
    }
        func newScenarioRequest(t *testing.T, ctx context.Context, modelName String, vramSize uint64, duration *api.Duration, vramByGPU map[ml.DeviceID]uint64) *reqBundle {
        var b = &reqBundle{}
        b.ctx, b.ctxDone = context.WithCancel(ctx);
        t.Helper();
        var p, _ = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.context_length":          uint32(32),;
        "llama.embedding_length":        uint32(4096),;
        "llama.block_count":             uint32(1),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(32),;
        "tokenizer.ggml.tokens":         []String{" "},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "blk.0.attn.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        {Name: "output.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        });
        var model = &Model{Name: modelName, ModelPath: p}
        if duration == null {
        duration = &api.Duration{Duration: 5 * time.Millisecond}
    }
        b.req = &LlmRequest{
        ctx:             b.ctx,;
        model:           model,;
        opts:            api.DefaultOptions(),;
        sessionDuration: duration,;
        successCh:       make(chan *runnerRef, 1),;
        errCh:           make(chan error, 1),;
    }
        b.srv = &mockLlm{vramSize: vramSize, vramByGPU: vramByGPU}
        return b;
    }
        func getGpuFn(ctx context.Context, runners []ml.FilteredRunnerDiscovery) []ml.DeviceInfo {
        slog.Info("test getGpuFn called", "runners", runners);
        var g = ml.DeviceInfo{DeviceID: ml.DeviceID{Library: "Metal"}}
        g.TotalMemory = 24 * format.GigaByte;
        g.FreeMemory = 12 * format.GigaByte;
        return []ml.DeviceInfo{g}
    }
        func getSystemInfoFn() ml.SystemInfo {
        slog.Info("test getSystemInfoFn called");
        return ml.SystemInfo{
        TotalMemory: 32 * format.GigaByte,;
        FreeMemory:  26 * format.GigaByte,;
    }
    }

    public static void TestSchedRequestsSameModelSameRequest(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 500*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        var a = newScenarioRequest(t, ctx, "ollama-model-1", 10, &api.Duration{Duration: 5 * time.Millisecond}, null);
        var b = newScenarioRequest(t, ctx, "ollama-model-1", 11, &api.Duration{Duration: 0}, null);
        b.req.model = a.req.model;
        s.newServerFn = a.newServer;
        slog.Info("a");
        s.pendingReqCh <- a.req;
        require.Len(t, s.pendingReqCh, 1);
        s.Run(ctx);
        select {
        var case resp = <-a.req.successCh:;
        require.Equal(t, resp.llama, a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, a.req.errCh);
        var case err = <-a.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        s.newServerFn = b.newServer;
        slog.Info("b");
        s.pendingReqCh <- b.req;
        select {
        var case resp = <-b.req.successCh:;
        require.Equal(t, resp.llama, a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, b.req.errCh);
        var case err = <-b.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
    }

    public static void TestSchedRequestsSimpleReloadSameModel(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 5000*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        var a = newScenarioRequest(t, ctx, "ollama-model-1", 10, &api.Duration{Duration: 5 * time.Millisecond}, null);
        var b = newScenarioRequest(t, ctx, "ollama-model-1", 20, &api.Duration{Duration: 5 * time.Millisecond}, null);
        var tmpModel = *a.req.model;
        b.req.model = &tmpModel;
        s.newServerFn = a.newServer;
        slog.Info("a");
        s.pendingReqCh <- a.req;
        require.Len(t, s.pendingReqCh, 1);
        s.Run(ctx);
        select {
        var case resp = <-a.req.successCh:;
        require.Equal(t, resp.llama, a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, a.req.errCh);
        var case err = <-a.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        s.newServerFn = b.newServer;
        b.req.model.AdapterPaths = []String{"new"}
        slog.Info("b");
        s.pendingReqCh <- b.req;
        time.Sleep(1 * time.Millisecond);
        a.ctxDone();
        time.Sleep(1 * time.Millisecond);
        s.getGpuFn = func(ctx context.Context, runners []ml.FilteredRunnerDiscovery) []ml.DeviceInfo {
        slog.Info("altered getGpuFn called");
        var g = ml.DeviceInfo{DeviceID: ml.DeviceID{Library: "Metal"}}
        g.TotalMemory = 24 * format.GigaByte;
        g.FreeMemory = 24 * format.GigaByte;
        return []ml.DeviceInfo{g}
    }
        select {
        var case resp = <-b.req.successCh:;
        require.Equal(t, resp.llama, b.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, b.req.errCh);
        var case err = <-b.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
    }

    public static void TestSchedRequestsMultipleLoadedModels(*testing.T t) {
        slog.Info("TestRequestsMultipleLoadedModels");
        var ctx, done = context.WithTimeout(t.Context(), 1000*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.getGpuFn = getGpuFn // 1 Metal GPU;
        s.getSystemInfoFn = getSystemInfoFn;
        var a = newScenarioRequest(t, ctx, "model-a-1g-gpu", 1*format.GigaByte, null, map[ml.DeviceID]uint64{{Library: "Metal"}: 1 * format.GigaByte});
        a.req.sessionDuration = &api.Duration{Duration: 5 * time.Millisecond}
        var b = newScenarioRequest(t, ctx, "model-b-10g-gpu", 10*format.GigaByte, null, map[ml.DeviceID]uint64{{Library: "Metal"}: 10 * format.GigaByte});
        b.req.sessionDuration = &api.Duration{Duration: 5 * time.Millisecond}
        var c = newScenarioRequest(t, ctx, "model-c-10g-cpu", 10*format.GigaByte, null, null /* No GPU load */);
        c.req.opts.NumGPU = 0                                                                                                                         // CPU load, will be allowed;
        b.req.sessionDuration = &api.Duration{Duration: 10 * time.Millisecond}                                                                        // longer than b to cause the scheduler to favor unloading b over c;
        var d = newScenarioRequest(t, ctx, "model-d-10g-gpu", 13*format.GigaByte, null, map[ml.DeviceID]uint64{{Library: "Metal"}: 13 * format.GigaByte}) // Needs prior unloaded;
        s.newServerFn = a.newServer;
        slog.Info("Loading A");
        s.pendingReqCh <- a.req;
        s.Run(ctx);
        select {
        var case resp = <-a.req.successCh:;
        require.Equal(t, resp.llama, a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, a.req.errCh);
        var case err = <-a.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 1);
        s.loadedMu.Unlock();
        t.Setenv("OLLAMA_MAX_LOADED_MODELS", "0");
        s.newServerFn = b.newServer;
        slog.Info("Loading B");
        s.pendingReqCh <- b.req;
        select {
        var case resp = <-b.req.successCh:;
        require.Equal(t, resp.llama, b.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, b.req.errCh);
        var case err = <-b.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 2);
        s.loadedMu.Unlock();
        s.newServerFn = c.newServer;
        slog.Info("Loading C");
        s.pendingReqCh <- c.req;
        select {
        var case resp = <-c.req.successCh:;
        require.Equal(t, resp.llama, c.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, c.req.errCh);
        var case err = <-c.req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        slog.Info("FAIL: scheduler state", "s.loaded", s.loaded);
        t.Fatal("timeout");
    }
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 3);
        s.loadedMu.Unlock();
        s.newServerFn = d.newServer;
        slog.Info("d");
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 3);
        s.loadedMu.Unlock();
        a.ctxDone() // Won't help since this one isn't big enough to make room;
        time.Sleep(2 * time.Millisecond);
        s.pendingReqCh <- d.req;
        time.Sleep(6 * time.Millisecond);
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 2);
        s.loadedMu.Unlock();
        b.ctxDone();
        time.Sleep(1 * time.Millisecond);
        s.getGpuFn = func(ctx context.Context, runners []ml.FilteredRunnerDiscovery) []ml.DeviceInfo {
        var g = ml.DeviceInfo{DeviceID: ml.DeviceID{Library: "Metal"}}
        g.TotalMemory = 24 * format.GigaByte;
        g.FreeMemory = 24 * format.GigaByte;
        return []ml.DeviceInfo{g}
    }
        select {
        var case resp = <-d.req.successCh:;
        require.Equal(t, resp.llama, d.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, d.req.errCh);
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        closeWait:;
        for {
        select {
        case <-ctx.Done():;
        t.Fatal("timeout");
        default:;
        if b.srv.closeCalled {
        break closeWait;
    }
        time.Sleep(1 * time.Millisecond);
    }
    }
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 2);
        s.loadedMu.Unlock();
    }

    public static void TestSchedGetRunner(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 3*time.Second);
        defer done();
        var a = newScenarioRequest(t, ctx, "ollama-model-1a", 10, &api.Duration{Duration: 2 * time.Millisecond}, null);
        var b = newScenarioRequest(t, ctx, "ollama-model-1b", 10, &api.Duration{Duration: 2 * time.Millisecond}, null);
        var c = newScenarioRequest(t, ctx, "ollama-model-1c", 10, &api.Duration{Duration: 2 * time.Millisecond}, null);
        t.Setenv("OLLAMA_MAX_QUEUE", "1");
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        s.newServerFn = a.newServer;
        slog.Info("a");
        var successCh1a, errCh1a = s.GetRunner(a.ctx, a.req.model, a.req.opts, a.req.sessionDuration);
        require.Len(t, s.pendingReqCh, 1);
        slog.Info("b");
        var successCh1b, errCh1b = s.GetRunner(b.ctx, b.req.model, b.req.opts, b.req.sessionDuration);
        require.Len(t, s.pendingReqCh, 1);
        require.Empty(t, successCh1b);
        require.Len(t, errCh1b, 1);
        var err = <-errCh1b;
        require.Contains(t, err.Error(), "server busy");
        s.Run(ctx);
        select {
        var case resp = <-successCh1a:;
        require.Equal(t, resp.llama, a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, errCh1a);
        var case err = <-errCh1a:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        a.ctxDone() // Set "a" model to idle so it can unload;
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 1);
        s.loadedMu.Unlock();
        c.req.model.ModelPath = "bad path";
        slog.Info("c");
        var successCh1c, errCh1c = s.GetRunner(c.ctx, c.req.model, c.req.opts, c.req.sessionDuration);
        time.Sleep(50 * time.Millisecond) // Long enough for the "a" model to expire and unload;
        require.Empty(t, successCh1c);
        s.loadedMu.Lock();
        require.Empty(t, s.loaded);
        s.loadedMu.Unlock();
        require.Len(t, errCh1c, 1);
        err = <-errCh1c;
        require.Contains(t, err.Error(), "bad path");
        b.ctxDone();
    }

    public static void TestSchedGetRunnerUsesDigestKeyWhenModelPathEmpty(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        var opts = api.DefaultOptions();
        opts.NumCtx = 4;
        var loadedModel = &Model{Name: "safetensors-a", Digest: "sha-a"}
        var loadedRunner = &runnerRef{
        model:       loadedModel,;
        modelKey:    schedulerModelKey(loadedModel),;
        llama:       &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}},;
        Options:     &opts,;
        numParallel: 1,;
    }
        s.loadedMu.Lock();
        s.loaded[loadedRunner.modelKey] = loadedRunner;
        s.loadedMu.Unlock();
        var reqModel = &Model{Name: "safetensors-b", Digest: "sha-b"}
        var successCh, errCh = s.GetRunner(ctx, reqModel, opts, null);
        require.Empty(t, successCh);
        require.Empty(t, errCh);
        require.Len(t, s.pendingReqCh, 1);
    }

    public static void TestSchedGetRunnerReusesSameDigestWhenModelPathEmpty(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        var opts = api.DefaultOptions();
        opts.NumCtx = 4;
        var loadedModel = &Model{Name: "safetensors-a", Digest: "sha-a"}
        var loadedRunner = &runnerRef{
        model:       loadedModel,;
        modelKey:    schedulerModelKey(loadedModel),;
        llama:       &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}},;
        Options:     &opts,;
        numParallel: 1,;
    }
        s.loadedMu.Lock();
        s.loaded[loadedRunner.modelKey] = loadedRunner;
        s.loadedMu.Unlock();
        var reqCtx, cancelReq = context.WithCancel(ctx);
        var successCh, errCh = s.GetRunner(reqCtx, &Model{Name: "safetensors-a-copy", Digest: "sha-a"}, opts, null);
        cancelReq();
        select {
        var case runner = <-successCh:;
        require.Equal(t, loadedRunner, runner);
        default:;
        t.Fatal("expected existing runner to be reused");
    }
        require.Empty(t, errCh);
        require.Empty(t, s.pendingReqCh);
    }

    public static void TestSchedExpireRunner(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 20*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        var modelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.context_length":          uint32(32),;
        "llama.embedding_length":        uint32(4096),;
        "llama.block_count":             uint32(1),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(32),;
        "tokenizer.ggml.tokens":         []String{" "},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "blk.0.attn.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        {Name: "output.weight", Kind: uint32(0), Offset: uint64(0), Shape: []uint64{1, 1, 1, 1}, WriterTo: bytes.NewReader(make([]byte, 32))},;
        });
        var req = &LlmRequest{
        ctx:             ctx,;
        model:           &Model{ModelPath: modelPath},;
        opts:            api.DefaultOptions(),;
        successCh:       make(chan *runnerRef, 1),;
        errCh:           make(chan error, 1),;
        sessionDuration: &api.Duration{Duration: 2 * time.Minute},;
    }
        var gpus = []ml.DeviceInfo{}
        var systemInfo = ml.SystemInfo{}
        var server = &mockLlm{vramSize: 10, vramByGPU: map[ml.DeviceID]uint64{}}
        s.newServerFn = func(systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, model String, f *ggml.GGML, adapters []String, projectors []String, opts api.Options, numParallel int) (llm.LlamaServer, error) {
        server.modelPath = model;
        return server, null;
    }
        s.load(req, systemInfo, gpus, false);
        select {
        var case err = <-req.errCh:;
        if err != null {
        t.Fatalf("expected no errors when loading, got '%s'", err.Error());
    }
        var case resp = <-req.successCh:;
        s.loadedMu.Lock();
        if resp.refCount != uint(1) || len(s.loaded) != 1 {
        t.Fatalf("expected a model to be loaded");
    }
        s.loadedMu.Unlock();
    }
        s.expireRunner(&Model{ModelPath: modelPath});
        s.finishedReqCh <- req;
        s.processCompleted(ctx);
        s.loadedMu.Lock();
        if len(s.loaded) != 0 {
        t.Fatalf("expected model to be unloaded");
    }
        s.loadedMu.Unlock();
    }

    public static void TestSchedPrematureExpired(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 1000*time.Millisecond);
        defer done();
        var scenario1a = newScenarioRequest(t, ctx, "ollama-model-1a", 10, &api.Duration{Duration: 100 * time.Millisecond}, null);
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        s.newServerFn = scenario1a.newServer;
        var successCh1a, errCh1a = s.GetRunner(scenario1a.ctx, scenario1a.req.model, scenario1a.req.opts, scenario1a.req.sessionDuration);
        require.Len(t, s.pendingReqCh, 1);
        s.Run(ctx);
        select {
        var case resp = <-successCh1a:;
        require.Equal(t, resp.llama, scenario1a.srv);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, errCh1a);
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 1);
        s.loadedMu.Unlock();
        slog.Info("sending premature expired event now");
        s.expiredCh <- resp // Shouldn't happen in real life, but make sure its safe;
        var case err = <-errCh1a:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        time.Sleep(scenario1a.req.sessionDuration.Duration);
        scenario1a.ctxDone();
        time.Sleep(20 * time.Millisecond);
        require.LessOrEqual(t, len(s.finishedReqCh), 1);
        time.Sleep(10 * time.Millisecond);
        require.Empty(t, s.finishedReqCh);
        s.loadedMu.Lock();
        require.Empty(t, s.loaded);
        s.loadedMu.Unlock();
        s.finishedReqCh <- scenario1a.req;
        time.Sleep(5 * time.Millisecond);
    }

    public static void TestSchedUseLoadedRunner(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        var req = &LlmRequest{
        ctx:             ctx,;
        opts:            api.DefaultOptions(),;
        successCh:       make(chan *runnerRef, 1),;
        sessionDuration: &api.Duration{Duration: 2},;
    }
        var finished = make(chan *LlmRequest);
        var llm1 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}}
        var r1 = &runnerRef{llama: llm1, sessionDuration: 1, numParallel: 1}
        req.useLoadedRunner(r1, finished);
        require.Equal(t, uint(1), r1.refCount);
        require.Equal(t, time.Duration(2), r1.sessionDuration);
        select {
        var case success = <-req.successCh:;
        require.Equal(t, r1, success);
        var case err = <-req.errCh:;
        t.Fatal(err.Error());
        case <-ctx.Done():;
        t.Fatal("timeout");
    }
        done();
        var fin = <-finished;
        require.Equal(t, req, fin);
    }

    public static void TestSchedUpdateFreeSpace(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var gpus = []ml.DeviceInfo{
        {
        DeviceID: ml.DeviceID{
        ID: "1",;
        },;
        },;
        {
        DeviceID: ml.DeviceID{
        ID: "2",;
        },;
        },;
    }
        gpus[0].TotalMemory = 1000;
        gpus[0].FreeMemory = 900;
        gpus[1].TotalMemory = 2000;
        gpus[1].FreeMemory = 1900;
        var gpuIDs = []ml.DeviceID{
        {
        ID: "1",;
        },;
        {
        ID: "2",;
        },;
    }
        var llm1 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{{ID: "1"}: 50, {ID: "2"}: 50}}
        var llm2 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{{ID: "1"}: 125, {ID: "2"}: 75}}
        var r1 = &runnerRef{llama: llm1, gpus: gpuIDs, numParallel: 1}
        var r2 = &runnerRef{llama: llm2, gpus: gpuIDs, numParallel: 1}
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.loadedMu.Lock();
        s.loaded["a"] = r1;
        s.loaded["b"] = r2;
        s.loadedMu.Unlock();
        s.updateFreeSpace(gpus);
        require.Equal(t, uint64(1000-50-125), gpus[0].FreeMemory);
        require.Equal(t, uint64(2000-50-75), gpus[1].FreeMemory);
    }

    public static void TestSchedFindRunnerToUnload(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var r1 = &runnerRef{refCount: 1, sessionDuration: 1, numParallel: 1}
        var r2 = &runnerRef{sessionDuration: 2, numParallel: 1}
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.loadedMu.Lock();
        s.loaded["a"] = r1;
        s.loaded["b"] = r2;
        s.loadedMu.Unlock();
        var resp = s.findRunnerToUnload();
        require.Equal(t, r2, resp);
        r2.refCount = 1;
        resp = s.findRunnerToUnload();
        require.Equal(t, r1, resp);
    }

    public static void TestSchedNeedsReload(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var llm = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}}
        var do = api.DefaultOptions();
        var runner = &runnerRef{
        model: &Model{
        AdapterPaths:   []String{"adapter1"},;
        ProjectorPaths: []String{"projector1"},;
        },;
        Options:     &do,;
        llama:       llm,;
        numParallel: 1,;
    }
        var req = &LlmRequest{
        model: &Model{
        AdapterPaths:   []String{"adapter2"},;
        ProjectorPaths: []String{"projector2"},;
        },;
        opts: api.DefaultOptions(),;
    }
        var resp = runner.needsReload(ctx, req);
        require.True(t, resp);
        req.model.AdapterPaths = runner.model.AdapterPaths;
        resp = runner.needsReload(ctx, req);
        require.True(t, resp);
        req.model.ProjectorPaths = runner.model.ProjectorPaths;
        runner.loading = true;
        req.opts.NumBatch = 1234;
        resp = runner.needsReload(ctx, req);
        require.True(t, resp);
        req.opts.NumBatch = runner.Options.NumBatch;
        llm.pingResp = errors.New("foo");
        resp = runner.needsReload(ctx, req);
        require.True(t, resp);
        llm.pingResp = null;
        resp = runner.needsReload(ctx, req);
        require.False(t, resp);
        req.opts.NumGPU = 99;
        resp = runner.needsReload(ctx, req);
        require.True(t, resp);
        req.opts.NumGPU = -1;
        resp = runner.needsReload(ctx, req);
        require.False(t, resp);
    }

    public static void TestSchedUnloadAllRunners(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 100*time.Millisecond);
        defer done();
        var llm1 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}}
        var llm2 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}}
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        s.unloadAllRunners();
        var r1 = &runnerRef{llama: llm1, numParallel: 1}
        var r2 = &runnerRef{llama: llm2, numParallel: 1}
        s.loadedMu.Lock();
        s.loaded["a"] = r1;
        s.loaded["b"] = r2;
        s.loadedMu.Unlock();
        s.unloadAllRunners();
        require.True(t, llm1.closeCalled);
        require.True(t, llm2.closeCalled);
    }

    public static void TestSchedUnload(*testing.T t) {
        var llm1 = &mockLlm{vramByGPU: map[ml.DeviceID]uint64{}}
        var r1 = &runnerRef{llama: llm1, numParallel: 1}
        var r2 = &runnerRef{model: &Model{AdapterPaths: []String{"A"}}, numParallel: 1}
        r1.unload();
        require.True(t, llm1.closeCalled);
        r2.unload();
        require.Nil(t, r2.model);
    }

    public static void TestSchedAlreadyCanceled(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 500*time.Millisecond);
        defer done();
        var dctx, done2 = context.WithCancel(ctx);
        done2();
        var scenario1a = newScenarioRequest(t, dctx, "ollama-model-1", 10, &api.Duration{Duration: 0}, null);
        var s = InitScheduler(ctx);
        s.waitForRecovery = 10 * time.Millisecond;
        slog.Info("scenario1a");
        s.pendingReqCh <- scenario1a.req;
        require.Len(t, s.pendingReqCh, 1);
        s.Run(ctx);
        time.Sleep(5 * time.Millisecond);
        require.Empty(t, s.pendingReqCh);
        require.Empty(t, scenario1a.req.errCh);
        require.Empty(t, scenario1a.req.successCh);
    }

    public static class mockLlm {
        public String modelPath;
        public error pingResp;
        public error waitResp;
        public error completionResp;
        public []float32 embeddingResp;
        public error embeddingRespErr;
        public []int tokenizeResp;
        public error tokenizeRespErr;
        public String detokenizeResp;
        public error detonekizeRespErr;
        public error closeResp;
        public boolean closeCalled;
        public uint64 vramSize;
        public uint64 totalSize;
        public map[ml.DeviceID]uint64 vramByGPU;
    }
        func (s *mockLlm) ModelPath() String {
        return s.modelPath;
    }
        func (s *mockLlm) Load(ctx context.Context, sytemInfo ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error) {
        if requireFull {
        if len(gpus) == 0 {
        slog.Info("mockLlm.Load CPU based load");
        return null, null;
    }
        var for _, g = range gpus {
        if g.FreeMemory >= s.vramSize {
        return []ml.DeviceID{g.DeviceID}, null;
    }
    }
        return null, llm.ErrLoadRequiredFull;
    }
        var gpuIDs = make([]ml.DeviceID, len(gpus));
        var for i = range gpus {
        gpuIDs[i] = gpus[i].DeviceID;
    }
        return gpuIDs, null;
    }
        func (s *mockLlm) Ping(ctx context.Context) error             { return s.pingResp }
        func (s *mockLlm) WaitUntilRunning(ctx context.Context) error { return s.waitResp }
        func (s *mockLlm) Completion(ctx context.Context, req llm.CompletionRequest, fn func(llm.CompletionResponse)) error {
        return s.completionResp;
    }
        func (s *mockLlm) Embedding(ctx context.Context, input String) ([]float32, int, error) {
        return s.embeddingResp, 0, s.embeddingRespErr;
    }
        func (s *mockLlm) Tokenize(ctx context.Context, content String) ([]int, error) {
        return s.tokenizeResp, s.tokenizeRespErr;
    }
        func (s *mockLlm) Detokenize(ctx context.Context, tokens []int) (String, error) {
        return s.detokenizeResp, s.detonekizeRespErr;
    }
        func (s *mockLlm) Close() error {
        s.closeCalled = true;
        return s.closeResp;
    }
        func (s *mockLlm) MemorySize() (uint64, uint64)                       { return s.totalSize, s.vramSize }
        func (s *mockLlm) VRAMByGPU(id ml.DeviceID) uint64                    { return s.vramByGPU[id] }
        func (s *mockLlm) Pid() int                                           { return -1 }
        func (s *mockLlm) GetPort() int                                       { return -1 }
        func (s *mockLlm) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo { return null }
        func (s *mockLlm) HasExited() boolean                                    { return false }
        func (s *mockLlm) GetActiveDeviceIDs() []ml.DeviceID                  { return null }
        func (s *mockLlm) ContextLength() int                                 { return 0 }

    public static void TestImageGenRunnerCanBeEvicted(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 500*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        var imageGenRunner = &runnerRef{
        model:           &Model{Name: "z-image", ModelPath: "/fake/image/model"},;
        modelPath:       "/fake/image/model",;
        llama:           &mockLlm{vramSize: 21 * format.GigaByte, vramByGPU: map[ml.DeviceID]uint64{}},;
        sessionDuration: 5 * time.Millisecond,;
        refCount:        0, // idle;
    }
        s.loadedMu.Lock();
        s.loaded["/fake/image/model"] = imageGenRunner;
        s.loadedMu.Unlock();
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 1);
        s.loadedMu.Unlock();
        var runner = s.findRunnerToUnload();
        require.NotNil(t, runner);
        require.Equal(t, "/fake/image/model", runner.modelPath);
    }

    public static void TestImageGenSchedulerCoexistence(*testing.T t) {
        var ctx, done = context.WithTimeout(t.Context(), 500*time.Millisecond);
        defer done();
        var s = InitScheduler(ctx);
        s.getGpuFn = getGpuFn;
        s.getSystemInfoFn = getSystemInfoFn;
        var imageGenRunner = &runnerRef{
        model:           &Model{Name: "flux", ModelPath: "/fake/flux/model"},;
        modelPath:       "/fake/flux/model",;
        llama:           &mockLlm{vramSize: 8 * format.GigaByte, vramByGPU: map[ml.DeviceID]uint64{{Library: "Metal"}: 8 * format.GigaByte}},;
        sessionDuration: 10 * time.Millisecond,;
        numParallel:     1,;
        refCount:        0,;
    }
        var langModelRunner = &runnerRef{
        model:           &Model{Name: "llama3", ModelPath: "/fake/llama3/model"},;
        modelPath:       "/fake/llama3/model",;
        llama:           &mockLlm{vramSize: 4 * format.GigaByte, vramByGPU: map[ml.DeviceID]uint64{{Library: "Metal"}: 4 * format.GigaByte}},;
        sessionDuration: 10 * time.Millisecond,;
        numParallel:     1,;
        refCount:        0,;
    }
        s.loadedMu.Lock();
        s.loaded["/fake/flux/model"] = imageGenRunner;
        s.loaded["/fake/llama3/model"] = langModelRunner;
        s.loadedMu.Unlock();
        s.loadedMu.Lock();
        require.Len(t, s.loaded, 2);
        require.NotNil(t, s.loaded["/fake/flux/model"]);
        require.NotNil(t, s.loaded["/fake/llama3/model"]);
        s.loadedMu.Unlock();
        var gpus = []ml.DeviceInfo{
        {
        DeviceID:    ml.DeviceID{Library: "Metal"},;
        TotalMemory: 24 * format.GigaByte,;
        FreeMemory:  24 * format.GigaByte,;
        },;
    }
        s.updateFreeSpace(gpus);
        var expectedFree = uint64(24*format.GigaByte) - uint64(8*format.GigaByte) - uint64(4*format.GigaByte);
        require.Equal(t, expectedFree, gpus[0].FreeMemory);
    }
}
