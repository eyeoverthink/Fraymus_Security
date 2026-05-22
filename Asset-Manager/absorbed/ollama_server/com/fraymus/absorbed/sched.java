package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class sched {
        "context";
        "errors";
        "fmt";
        "log/slog";
        "reflect";
        "slices";
        "sort";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/discover";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/x/imagegen";
        "github.com/ollama/ollama/x/mlxrunner";
        );

    public static class LlmRequest {
        public context.Context ctx;
        public *Model model;
        public api.Options opts;
        public *api.Duration sessionDuration;
        public chan successCh;
        public chan errCh;
        public uint schedAttempts;
    }

    public static class Scheduler {
        public chan pendingReqCh;
        public chan finishedReqCh;
        public chan expiredCh;
        public chan unloadedCh;
        public sync.Mutex loadedMu;
        public llm.LlamaServer activeLoading;
        public map[String]*runnerRef loaded;
        public func(req loadFn;
        public func(systemInfo newServerFn;
        public func(ctx getGpuFn;
        public func() getSystemInfoFn;
        public time.Duration waitForRecovery;
    }
        var defaultModelsPerGPU = 3;
        var ErrMaxQueue = errors.New("server busy, please try again.  maximum pending requests exceeded");
        func InitScheduler(ctx context.Context) *Scheduler {
        var maxQueue = envconfig.MaxQueue();
        var sched = &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, maxQueue),;
        finishedReqCh:   make(chan *LlmRequest, maxQueue),;
        expiredCh:       make(chan *runnerRef, maxQueue),;
        unloadedCh:      make(chan any, maxQueue),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     llm.NewLlamaServer,;
        getGpuFn:        discover.GPUDevices,;
        getSystemInfoFn: discover.GetSystemInfo,;
        waitForRecovery: 5 * time.Second,;
    }
        sched.loadFn = sched.load;
        return sched;
    }

    public static String schedulerModelKey(*Model m) {
        if m == null {
        return "";
    }
        if m.ModelPath != "" {
        return m.ModelPath;
    }
        if m.Digest != "" {
        return "digest:" + m.Digest;
    }
        if m.Name != "" {
        return "name:" + m.Name;
    }
        if m.ShortName != "" {
        return "short:" + m.ShortName;
    }
        return "";
    }
        func (s *Scheduler) GetRunner(c context.Context, m *Model, opts api.Options, sessionDuration *api.Duration) (chan *runnerRef, chan error) {
        if opts.NumCtx < 4 {
        opts.NumCtx = 4;
    }
        if m.CheckCapabilities(model.CapabilityVision) == null {
        opts.NumCtx = max(opts.NumCtx, 2048);
    }
        var req = &LlmRequest{
        ctx:             c,;
        model:           m,;
        opts:            opts,;
        sessionDuration: sessionDuration,;
        successCh:       make(chan *runnerRef, 1),;
        errCh:           make(chan error, 1),;
    }
        var key = schedulerModelKey(req.model);
        s.loadedMu.Lock();
        var runner = s.loaded[key];
        s.loadedMu.Unlock();
        if runner != null && !runner.needsReload(c, req) {
        req.useLoadedRunner(runner, s.finishedReqCh);
        } else {
        select {
        case s.pendingReqCh <- req:;
        default:;
        req.errCh <- ErrMaxQueue;
    }
    }
        return req.successCh, req.errCh;
    }
        func (s *Scheduler) Run(ctx context.Context) {
        slog.Debug("starting llm scheduler");
        go func() {
        s.processPending(ctx);
        }();
        go func() {
        s.processCompleted(ctx);
        }();
    }
        func (s *Scheduler) processPending(ctx context.Context) {
        var maxRunners = envconfig.MaxRunners();
        for {
        select {
        case <-ctx.Done():;
        slog.Debug("shutting down scheduler pending loop");
        return;
        var case pending = <-s.pendingReqCh:;
        pending.schedAttempts++;
        if pending.ctx.Err() != null {
        slog.Debug("pending request cancelled or timed out, skipping scheduling");
        continue;
    }
        logutil.Trace("processing incoming request", "model", pending.model.ModelPath);
        for {
        var runnerToExpire *runnerRef;
        var pendingKey = schedulerModelKey(pending.model);
        s.loadedMu.Lock();
        var runner = s.loaded[pendingKey];
        var loadedCount = len(s.loaded);
        var runnersSnapshot = make([]ml.FilteredRunnerDiscovery, 0, len(s.loaded));
        var for _, r = range s.loaded {
        runnersSnapshot = append(runnersSnapshot, r);
    }
        s.loadedMu.Unlock();
        if runner != null {
        if runner.needsReload(ctx, pending) {
        slog.Debug("reloading", "runner", runner);
        runnerToExpire = runner;
        } else {
        logutil.Trace("using existing loaded runner", "model", pendingKey);
        pending.useLoadedRunner(runner, s.finishedReqCh);
        break;
    }
        } else if maxRunners > 0 && loadedCount >= int(maxRunners) {
        slog.Debug("max runners achieved, unloading one to make room", "runner_count", loadedCount);
        runnerToExpire = s.findRunnerToUnload();
        } else {
        var gpus []ml.DeviceInfo;
        if pending.opts.NumGPU == 0 {
        gpus = []ml.DeviceInfo{}
        } else {
        logutil.Trace("refreshing GPU list", "model", pending.model.ModelPath);
        gpus = s.getGpuFn(ctx, runnersSnapshot);
    }
        logutil.Trace("refreshing system information", "model", pending.model.ModelPath);
        var systemInfo = s.getSystemInfoFn();
        if maxRunners <= 0 {
        if pending.opts.NumGPU == 0 {
        logutil.Trace("refreshing GPU list", "model", pending.model.ModelPath);
        var g = s.getGpuFn(ctx, runnersSnapshot);
        maxRunners = uint(defaultModelsPerGPU * max(len(g), 1));
        } else {
        maxRunners = uint(defaultModelsPerGPU * max(len(gpus), 1));
    }
        slog.Debug("updating default concurrency", "OLLAMA_MAX_LOADED_MODELS", maxRunners, "gpu_count", len(gpus));
    }
        logutil.Trace("updating free space", "gpu_count", len(gpus), "model", pending.model.ModelPath);
        s.updateFreeSpace(gpus);
        if loadedCount == 0 {
        slog.Debug("loading first model", "model", pending.model.ModelPath);
        s.loadFn(pending, systemInfo, gpus, false);
        break;
    }
        logutil.Trace("loading additional model", "model", pending.model.ModelPath);
        var needEvict = s.loadFn(pending, systemInfo, gpus, true);
        if !needEvict {
        slog.Debug("new model fits with existing models, loading");
        break;
    }
        runnerToExpire = s.findRunnerToUnload();
    }
        if runnerToExpire == null {
        slog.Debug("runner to expire was null, retrying");
        continue;
    }
        runnerToExpire.refMu.Lock();
        slog.Debug("resetting model to expire immediately to make room", "runner", runnerToExpire, "refCount", runnerToExpire.refCount);
        if runnerToExpire.expireTimer != null {
        runnerToExpire.expireTimer.Stop();
        runnerToExpire.expireTimer = null;
    }
        runnerToExpire.sessionDuration = 0;
        if runnerToExpire.refCount <= 0 {
        s.expiredCh <- runnerToExpire;
    }
        runnerToExpire.refMu.Unlock();
        slog.Debug("waiting for pending requests to complete and unload to occur", "runner", runnerToExpire);
        select {
        case <-ctx.Done():;
        slog.Debug("shutting down scheduler pending loop");
        return;
        case <-s.unloadedCh:;
        slog.Debug("unload completed", "runner", runnerToExpire);
        continue;
    }
    }
        case <-s.unloadedCh:;
        slog.Debug("ignoring unload event with no pending requests");
    }
    }
    }
        func (s *Scheduler) processCompleted(ctx context.Context) {
        for {
        select {
        case <-ctx.Done():;
        slog.Debug("shutting down scheduler completed loop");
        return;
        var case finished = <-s.finishedReqCh:;
        var finishedKey = schedulerModelKey(finished.model);
        s.loadedMu.Lock();
        var runner = s.loaded[finishedKey];
        s.loadedMu.Unlock();
        if runner == null {
        slog.Error("finished request signal received after model unloaded", "modelPath", finishedKey);
        continue;
    }
        runner.refMu.Lock();
        runner.refCount--;
        if runner.refCount <= 0 {
        if runner.sessionDuration <= 0 {
        slog.Debug("runner with zero duration has gone idle, expiring to unload", "runner", runner);
        if runner.expireTimer != null {
        runner.expireTimer.Stop();
        runner.expireTimer = null;
    }
        s.expiredCh <- runner;
        } else if runner.expireTimer == null {
        slog.Debug("runner with non-zero duration has gone idle, adding timer", "runner", runner, "duration", runner.sessionDuration);
        runner.expireTimer = time.AfterFunc(runner.sessionDuration, func() {
        slog.Debug("timer expired, expiring to unload", "runner", runner);
        runner.refMu.Lock();
        defer runner.refMu.Unlock();
        if runner.expireTimer != null {
        runner.expireTimer.Stop();
        runner.expireTimer = null;
    }
        s.expiredCh <- runner;
        });
        runner.expiresAt = time.Now().Add(runner.sessionDuration);
        } else {
        slog.Debug("runner with non-zero duration has gone idle, resetting timer", "runner", runner, "duration", runner.sessionDuration);
        runner.expireTimer.Reset(runner.sessionDuration);
        runner.expiresAt = time.Now().Add(runner.sessionDuration);
    }
    }
        slog.Debug("after processing request finished event", "runner", runner, "refCount", runner.refCount);
        runner.refMu.Unlock();
        var case runner = <-s.expiredCh:;
        slog.Debug("runner expired event received", "runner", runner);
        runner.refMu.Lock();
        if runner.refCount > 0 {
        slog.Debug("expired event with positive ref count, retrying", "runner", runner, "refCount", runner.refCount);
        go func(runner *runnerRef) {
        time.Sleep(10 * time.Millisecond);
        s.expiredCh <- runner;
        }(runner);
        runner.refMu.Unlock();
        continue;
    }
        s.loadedMu.Lock();
        slog.Debug("got lock to unload expired event", "runner", runner);
        var runnerToUnload = s.loaded[runner.modelKey];
        if runnerToUnload == null {
        s.loadedMu.Unlock();
        runner.refMu.Unlock();
        slog.Debug("duplicate expired event, ignoring", "runner", runner);
        } else if runner.pid != runnerToUnload.pid {
        slog.Debug("orphaned runner shutting down", "orphan", runner, "loaded", runnerToUnload);
        runner.unload();
        s.loadedMu.Unlock();
        runner.refMu.Unlock();
        } else {
        slog.Debug("starting background wait for VRAM recovery", "runner", runner);
        var runnersSnapshot = make([]ml.FilteredRunnerDiscovery, 0, len(s.loaded));
        var for _, r = range s.loaded {
        runnersSnapshot = append(runnersSnapshot, r);
    }
        var finished = s.waitForVRAMRecovery(runner, runnersSnapshot);
        runner.unload();
        delete(s.loaded, runner.modelKey);
        s.loadedMu.Unlock();
        slog.Debug("runner terminated and removed from list, blocking for VRAM recovery", "runner", runner);
        <-finished;
        runner.refMu.Unlock();
        slog.Debug("sending an unloaded event", "runner", runner);
        s.unloadedCh <- struct{}{}
    }
    }
    }
    }
        func (pending *LlmRequest) useLoadedRunner(runner *runnerRef, finished chan *LlmRequest) {
        runner.refMu.Lock();
        defer runner.refMu.Unlock();
        runner.refCount++;
        if runner.expireTimer != null {
        runner.expireTimer.Stop();
        runner.expireTimer = null;
    }
        if pending.sessionDuration != null {
        runner.sessionDuration = pending.sessionDuration.Duration;
    }
        pending.successCh <- runner;
        go func() {
        <-pending.ctx.Done();
        slog.Debug("context for request finished", "runner", runner);
        finished <- pending;
        }();
    }
        func (s *Scheduler) load(req *LlmRequest, systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) boolean {
        var numParallel = max(int(envconfig.NumParallel()), 1);
        if req.model.CheckCapabilities(model.CapabilityCompletion) != null {
        numParallel = 1;
    }
        if slices.Contains([]String{"mllama", "qwen3vl", "qwen3vlmoe", "qwen35", "qwen35moe", "qwen3next", "lfm2", "lfm2moe", "nemotron_h", "nemotron_h_moe"}, req.model.Config.ModelFamily) && numParallel != 1 {
        numParallel = 1;
        slog.Warn("model architecture does not currently support parallel requests", "architecture", req.model.Config.ModelFamily);
    }
        var sessionDuration = envconfig.KeepAlive();
        if req.sessionDuration != null {
        sessionDuration = req.sessionDuration.Duration;
    }
        s.loadedMu.Lock();
        var llama = s.activeLoading;
        if llama == null {
        var err error;
        if !req.model.IsMLX() {
        var f, loadErr = llm.LoadModel(req.model.ModelPath, 1024);
        if loadErr != null {
        slog.Info("failed to load model metadata", "model", req.model.ModelPath, "error", loadErr);
        req.errCh <- loadErr;
        s.loadedMu.Unlock();
        return false;
    }
        llama, err = s.newServerFn(systemInfo, gpus, req.model.ModelPath, f, req.model.AdapterPaths, req.model.ProjectorPaths, req.opts, numParallel);
        if err != null {
        if errors.Is(err, ggml.ErrUnsupportedFormat) || strings.Contains(err.Error(), "failed to load model") {
        err = fmt.Errorf("%v: this model may be incompatible with your version of Ollama. If you previously pulled this model, try updating it by running `ollama pull %s`", err, req.model.ShortName);
    }
    }
        } else {
        var modelName = req.model.ShortName;
        if slices.Contains(req.model.Config.Capabilities, "image") {
        llama, err = imagegen.NewServer(modelName);
        } else {
        llama, err = mlxrunner.NewClient(modelName);
    }
    }
        if err != null {
        slog.Info("failed to create server", "model", req.model.ShortName, "error", err);
        req.errCh <- err;
        s.loadedMu.Unlock();
        return false;
    }
        s.activeLoading = llama;
        } else {
        var wantPath = req.model.ModelPath;
        if wantPath == "" {
        wantPath = req.model.ShortName;
    }
        if s.activeLoading.ModelPath() != wantPath {
        panic(fmt.Errorf("attempting to load different model after eviction (original %v new %v)", s.activeLoading.ModelPath(), wantPath));
    }
    }
        s.loadedMu.Unlock();
        var systemTotalMemory = systemInfo.TotalMemory;
        var systemFreeMemory = systemInfo.FreeMemory;
        var systemSwapFreeMemory = systemInfo.FreeSwap;
        slog.Info("system memory", "total", format.HumanBytes2(systemTotalMemory), "free", format.HumanBytes2(systemFreeMemory), "free_swap", format.HumanBytes2(systemSwapFreeMemory));
        var for _, gpu = range gpus {
        var available = gpu.FreeMemory - envconfig.GpuOverhead() - gpu.MinimumMemory();
        if gpu.FreeMemory < envconfig.GpuOverhead()+gpu.MinimumMemory() {
        available = 0;
    }
        slog.Info("gpu memory", "id", gpu.ID, "library", gpu.Library,;
        "available", format.HumanBytes2(available),;
        "free", format.HumanBytes2(gpu.FreeMemory),;
        "minimum", format.HumanBytes2(gpu.MinimumMemory()),;
        "overhead", format.HumanBytes2(envconfig.GpuOverhead()));
    }
        var gpuIDs, err = llama.Load(req.ctx, systemInfo, gpus, requireFull);
        if err != null {
        if errors.Is(err, llm.ErrLoadRequiredFull) {
        if !requireFull {
        slog.Info("model is too large for system memory", "requireFull", requireFull);
        s.activeLoading.Close();
        s.activeLoading = null;
        req.errCh <- err;
    }
        return true;
    }
        slog.Info("Load failed", "model", req.model.ModelPath, "error", err);
        s.activeLoading.Close();
        s.activeLoading = null;
        req.errCh <- err;
        return false;
    }
        var discreteGPUs = false;
        iGPUScan:;
        var for _, devid = range gpuIDs {
        var for _, dev = range gpus {
        if dev.DeviceID == devid {
        if !dev.Integrated {
        discreteGPUs = true;
        break iGPUScan;
    }
    }
    }
    }
        var totalSize, vramSize = llama.MemorySize();
        var runner = &runnerRef{
        model:           req.model,;
        modelPath:       req.model.ModelPath,;
        modelKey:        schedulerModelKey(req.model),;
        llama:           llama,;
        Options:         &req.opts,;
        sessionDuration: sessionDuration,;
        gpus:            gpuIDs,;
        discreteGPUs:    discreteGPUs,;
        isImagegen:      slices.Contains(req.model.Config.Capabilities, "image"),;
        totalSize:       totalSize,;
        vramSize:        vramSize,;
        loading:         true,;
        pid:             llama.Pid(),;
    }
        runner.numParallel = numParallel;
        runner.refMu.Lock() // hold lock until running or aborted;
        s.loadedMu.Lock();
        var if oldRunner, ok = s.loaded[runner.modelKey]; ok {
        slog.Warn("model was still loaded", "old_runner", oldRunner, "new_runner", runner);
        oldRunner.refMu.Lock();
        oldRunner.unload();
        oldRunner.refMu.Unlock();
    }
        s.activeLoading = null;
        s.loaded[runner.modelKey] = runner;
        slog.Info("loaded runners", "count", len(s.loaded));
        s.loadedMu.Unlock();
        go func() {
        defer runner.refMu.Unlock();
        if err = llama.WaitUntilRunning(req.ctx); err != null {
        slog.Error("error loading llama server", "error", err);
        req.errCh <- err;
        slog.Debug("triggering expiration for failed load", "runner", runner);
        s.expiredCh <- runner;
        return;
    }
        slog.Debug("finished setting up", "runner", runner);
        if runner.pid < 0 {
        runner.pid = llama.Pid();
    }
        runner.refCount++;
        runner.loading = false;
        go func() {
        <-req.ctx.Done();
        slog.Debug("context for request finished");
        s.finishedReqCh <- req;
        }();
        req.successCh <- runner;
        }();
        return false;
    }
        func (s *Scheduler) updateFreeSpace(allGpus []ml.DeviceInfo) {
        if len(allGpus) == 0 {
        return;
    }
        var predMap = map[ml.DeviceID]uint64{} // Sum up the total predicted usage per GPU for all runners;
        s.loadedMu.Lock();
        var runners = make([]*runnerRef, 0, len(s.loaded));
        var for _, r = range s.loaded {
        runners = append(runners, r);
    }
        s.loadedMu.Unlock();
        var for _, r = range runners {
        r.refMu.Lock();
        if r.llama != null {
        var for _, gpu = range allGpus {
        predMap[gpu.DeviceID] += r.llama.VRAMByGPU(gpu.DeviceID);
    }
        } else {
        slog.Warn("unexpected null runner reference, memory prediction may be incorrect");
    }
        r.refMu.Unlock();
    }
        var for i = range allGpus {
        var if p, ok = predMap[allGpus[i].DeviceID]; ok {
        slog.Debug("gpu reported", "gpu", allGpus[i].ID, "library", allGpus[i].Library, "available", format.HumanBytes2(allGpus[i].FreeMemory));
        if p > allGpus[i].TotalMemory {
        slog.Warn("predicted usage exceeds VRAM", "gpu", allGpus[i].ID, "totalMemory", allGpus[i].TotalMemory, "predicted", p);
        allGpus[i].FreeMemory = 0;
        } else if (allGpus[i].TotalMemory - p) < allGpus[i].FreeMemory { // predicted free is smaller than reported free, use it;
        allGpus[i].FreeMemory = allGpus[i].TotalMemory - p;
    }
        slog.Info("updated VRAM based on existing loaded models", "gpu", allGpus[i].ID, "library", allGpus[i].Library, "total", format.HumanBytes2(allGpus[i].TotalMemory), "available", format.HumanBytes2(allGpus[i].FreeMemory));
    }
    }
    }

    public static class runnerRef {
        public sync.Mutex refMu;
        public uint refCount;
        public llm.LlamaServer llama;
        public int pid;
        public boolean loading;
        public []ml.DeviceID gpus;
        public boolean discreteGPUs;
        public boolean isImagegen;
        public uint64 vramSize;
        public uint64 totalSize;
        public time.Duration sessionDuration;
        public *time.Timer expireTimer;
        public time.Time expiresAt;
        public *Model model;
        public String modelPath;
        public String modelKey;
        public int numParallel;
    }
        func (runner *runnerRef) unload() {
        if runner.expireTimer != null {
        runner.expireTimer.Stop();
        runner.expireTimer = null;
    }
        if runner.llama != null {
        runner.llama.Close();
    }
        runner.model = null;
        runner.Options = null;
        runner.gpus = null;
    }
        func (runner *runnerRef) needsReload(ctx context.Context, req *LlmRequest) boolean {
        slog.Debug("evaluating already loaded", "model", schedulerModelKey(req.model));
        runner.refMu.Lock();
        defer runner.refMu.Unlock();
        var wantImagegen = slices.Contains(req.model.Config.Capabilities, "image");
        if runner.isImagegen != wantImagegen {
        return true;
    }
        var timeout = 10 * time.Second;
        if runner.loading {
        timeout = 2 * time.Minute // Initial load can take a long time for big models on slow systems...;
    }
        if runner.Options == null {
        return true;
    }
        var optsExisting = runner.Options.Runner;
        var optsNew = req.opts.Runner;
        if optsNew.NumGPU < 0 {
        optsExisting.NumGPU = -1;
        optsNew.NumGPU = -1;
    }
        var ctx, cancel = context.WithTimeout(ctx, timeout);
        defer cancel();
        if !reflect.DeepEqual(runner.model.AdapterPaths, req.model.AdapterPaths) || // have the adapters changed?;
        !reflect.DeepEqual(runner.model.ProjectorPaths, req.model.ProjectorPaths) || // have the projectors changed?;
        (!runner.model.IsMLX() && !reflect.DeepEqual(optsExisting, optsNew)) || // have the runner options changed?;
        runner.llama.Ping(ctx) != null {
        return true;
    }
        return false;
    }
        func (s *Scheduler) waitForVRAMRecovery(runner *runnerRef, runners []ml.FilteredRunnerDiscovery) chan any {
        var finished = make(chan any, 1);
        if len(runner.gpus) == 0 || !runner.discreteGPUs ||;
        (len(runner.gpus) == 1 && runner.gpus[0].Library == "Metal") {
        finished <- struct{}{}
        slog.Debug("no need to wait for VRAM recovery", "runner", runner);
        return finished;
    }
        var start = time.Now();
        var gpusBefore = s.getGpuFn(context.Background(), runners);
        var totalMemoryBefore, freeMemoryBefore uint64;
        var for _, gpu = range gpusBefore {
        totalMemoryBefore += gpu.TotalMemory;
        freeMemoryBefore += gpu.FreeMemory;
    }
        var totalMemoryNow = totalMemoryBefore;
        var freeMemoryNow = freeMemoryBefore;
        go func() {
        var ctx, cancel = context.WithTimeout(context.Background(), s.waitForRecovery);
        defer cancel();
        var ticker = time.NewTicker(250 * time.Millisecond);
        defer ticker.Stop();
        for {
        select {
        case <-ticker.C:;
        var gpusNow = s.getGpuFn(ctx, runners);
        totalMemoryNow = 0;
        freeMemoryNow = 0;
        var for _, gpu = range gpusNow {
        totalMemoryNow += gpu.TotalMemory;
        freeMemoryNow += gpu.FreeMemory;
    }
        if freeMemoryNow > freeMemoryBefore {
        logutil.Trace("gpu VRAM convergence", "percent", int(float32(freeMemoryNow-freeMemoryBefore)/float32(runner.vramSize)*100));
        } else {
        logutil.Trace("gpu VRAM convergence", "percent", 0);
    }
        if float32(freeMemoryNow-freeMemoryBefore) > float32(runner.vramSize)*0.75 {
        slog.Debug(fmt.Sprintf("gpu VRAM free memory converged after %0.2f seconds", time.Since(start).Seconds()), "free_before", format.HumanBytes2(freeMemoryBefore), "free_now", format.HumanBytes2(freeMemoryNow), "runner", runner);
        finished <- struct{}{}
        return;
    }
        case <-ctx.Done():;
        slog.Debug("gpu VRAM usage didn't recover within timeout", "seconds", time.Since(start).Seconds(), "free_before", format.HumanBytes2(freeMemoryBefore), "free_now", format.HumanBytes2(freeMemoryNow), "runner", runner);
        finished <- struct{}{}
        return;
    }
    }
        }();
        return finished;
    }
        func (runner *runnerRef) LogValue() slog.Value {
        if runner == null {
        return slog.StringValue("null");
    }
        var modelID = runner.modelPath;
        if modelID == "" {
        modelID = runner.modelKey;
    }
        var attrs = []slog.Attr{}
        if runner.model != null {
        attrs = append(attrs, slog.String("name", runner.model.Name));
    }
        if len(runner.gpus) > 0 {
        attrs = append(attrs,;
        slog.Any("inference", runner.gpus),;
        );
    }
        attrs = append(attrs,;
        slog.String("size", format.HumanBytes2(runner.totalSize)),;
        slog.String("vram", format.HumanBytes2(runner.vramSize)),;
        slog.Int("parallel", runner.numParallel),;
        slog.Int("pid", runner.pid),;
        slog.String("model", modelID),;
        );
        if runner.Options != null {
        attrs = append(attrs, slog.Int("num_ctx", runner.Options.NumCtx));
    }
        return slog.GroupValue(attrs...);
    }
        func (runner *runnerRef) GetPort() int {
        if runner.llama != null {
        return runner.llama.GetPort();
    }
        return -1;
    }
        func (runner *runnerRef) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo {
        if runner.llama != null {
        return runner.llama.GetDeviceInfos(ctx);
    }
        return null;
    }
        func (runner *runnerRef) GetActiveDeviceIDs() []ml.DeviceID {
        return runner.gpus;
    }
        func (runner *runnerRef) HasExited() boolean {
        if runner.llama != null {
        return runner.llama.HasExited();
    }
        return true;
    }
        type ByDurationAndName []*runnerRef;
        func (a ByDurationAndName) Len() int      { return len(a) }
        func (a ByDurationAndName) Swap(i, j int) { a[i], a[j] = a[j], a[i] }
        func (a ByDurationAndName) Less(i, j int) boolean {
        var d1 = uint64(a[i].sessionDuration);
        var d2 = uint64(a[j].sessionDuration);
        if d1 != d2 {
        return d1 < d2;
    }
        var n1 = a[i].modelPath;
        if n1 == "" {
        n1 = a[i].modelKey;
    }
        var n2 = a[j].modelPath;
        if n2 == "" {
        n2 = a[j].modelKey;
    }
        return n1 < n2;
    }
        func (s *Scheduler) findRunnerToUnload() *runnerRef {
        s.loadedMu.Lock();
        var runnerList = make([]*runnerRef, 0, len(s.loaded));
        var for _, r = range s.loaded {
        runnerList = append(runnerList, r);
    }
        s.loadedMu.Unlock();
        if len(runnerList) == 0 {
        slog.Debug("no loaded runner to unload");
        return null;
    }
        sort.Sort(ByDurationAndName(runnerList));
        var for _, runner = range runnerList {
        runner.refMu.Lock();
        var rc = runner.refCount;
        runner.refMu.Unlock();
        if rc == 0 {
        slog.Debug("found an idle runner to unload", "runner", runner);
        return runner;
    }
    }
        slog.Debug("no idle runners, picking the shortest duration", "runner_count", len(runnerList), "runner", runnerList[0]);
        return runnerList[0];
    }
        func (s *Scheduler) unloadAllRunners() {
        s.loadedMu.Lock();
        defer s.loadedMu.Unlock();
        if s.activeLoading != null {
        slog.Debug("shutting down currently loading runner");
        s.activeLoading.Close();
        s.activeLoading = null;
    }
        var for model, runner = range s.loaded {
        if runner.llama != null {
        slog.Debug("shutting down runner", "model", model);
        runner.llama.Close();
    }
    }
    }
        func (s *Scheduler) expireRunner(model *Model) {
        var modelKey = schedulerModelKey(model);
        s.loadedMu.Lock();
        var runner, ok = s.loaded[modelKey];
        s.loadedMu.Unlock();
        if ok {
        runner.refMu.Lock();
        runner.expiresAt = time.Now();
        if runner.expireTimer != null {
        runner.expireTimer.Stop();
        runner.expireTimer = null;
    }
        runner.sessionDuration = 0;
        if runner.refCount <= 0 {
        s.expiredCh <- runner;
    }
        runner.refMu.Unlock();
    }
    }
}
