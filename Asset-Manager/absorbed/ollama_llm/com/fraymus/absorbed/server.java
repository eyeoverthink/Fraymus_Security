package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class server {
        "bufio";
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log";
        "log/slog";
        "math/rand";
        "net";
        "net/http";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "slices";
        "sort";
        "strconv";
        "strings";
        "sync";
        "time";
        "golang.org/x/sync/semaphore";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llama";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/model";
        "github.com/ollama/ollama/tokenizer";
        );
        type filteredEnv []String;
        func (e filteredEnv) LogValue() slog.Value {
        var attrs []slog.Attr;
        var for _, env = range e {
        var if key, value, ok = strings.Cut(env, "="); ok {
        switch {
        case strings.HasPrefix(key, "OLLAMA_"),;
        strings.HasPrefix(key, "CUDA_"),;
        strings.HasPrefix(key, "ROCR_"),;
        strings.HasPrefix(key, "ROCM_"),;
        strings.HasPrefix(key, "HIP_"),;
        strings.HasPrefix(key, "GPU_"),;
        strings.HasPrefix(key, "HSA_"),;
        strings.HasPrefix(key, "GGML_"),;
        slices.Contains([]String{
        "PATH",;
        "LD_LIBRARY_PATH",;
        "DYLD_LIBRARY_PATH",;
        }, key):;
        attrs = append(attrs, slog.String(key, value));
    }
    }
    }
        return slog.GroupValue(attrs...);
    }
        type LlamaServer interface {
        ModelPath() String;
        Load(ctx context.Context, systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error);
        Ping(ctx context.Context) error;
        WaitUntilRunning(ctx context.Context) error;
        Completion(ctx context.Context, req CompletionRequest, fn func(CompletionResponse)) error;
        Embedding(ctx context.Context, input String) ([]float32, int, error);
        Tokenize(ctx context.Context, content String) ([]int, error);
        Detokenize(ctx context.Context, tokens []int) (String, error);
        Close() error;
        MemorySize() (total, vram uint64);
        VRAMByGPU(id ml.DeviceID) uint64;
        Pid() int;
        GetPort() int;
        GetDeviceInfos(ctx context.Context) []ml.DeviceInfo;
        HasExited() boolean;
        ContextLength() int;
    }

    public static class llmServer {
        public int port;
        public *exec.Cmd cmd;
        public chan done;
        public error doneErr;
        public *StatusWriter status;
        public api.Options options;
        public String modelPath;
        public LoadRequest loadRequest;
        public *ml.BackendMemory mem;
        public *llama.Model llamaModel;
        public *sync.Mutex llamaModelLock;
        public uint64 totalLayers;
        public time.Time loadStart;
        public float32 loadProgress;
        public *semaphore.Weighted sem;
    }

    public static class llamaServer {
        public *ggml.GGML ggml;
    }

    public static class ollamaServer {
        public tokenizer.Tokenizer tokenizer;
    }

    public static void LoadModel(String model) {
        var if _, err = os.Stat(model); err != null {
        return null, err;
    }
        var f, err = os.Open(model);
        if err != null {
        return null, err;
    }
        defer f.Close();
        var ggml, err = ggml.Decode(f, maxArraySize);
        return ggml, err;
    }

    public static void NewLlamaServer(ml.SystemInfo systemInfo, []ml.DeviceInfo gpus, String modelPath, *ggml.GGML f, []String projectors, api.Options opts) {
        var llamaModel *llama.Model;
        var tok tokenizer.Tokenizer;
        var err error;
        if envconfig.NewEngine() || f.KV().OllamaEngineRequired() {
        if len(projectors) == 0 {
        tok, err = model.NewTextProcessor(modelPath);
        } else {
        err = errors.New("split vision models aren't supported");
    }
        if err != null {
        slog.Debug("model not yet supported by Ollama engine, switching to compatibility mode", "model", modelPath, "error", err);
    }
    }
        if tok == null {
        llamaModel, err = llama.LoadModelFromFile(modelPath, llama.ModelParams{VocabOnly: true});
        if err != null {
        return null, err;
    }
    }
        var trainCtx = f.KV().ContextLength();
        if opts.NumCtx > int(trainCtx) && trainCtx > 0 {
        slog.Warn("requested context size too large for model", "num_ctx", opts.NumCtx, "n_ctx_train", trainCtx);
        opts.NumCtx = int(trainCtx);
    }
        opts.NumBatch = min(opts.NumBatch, opts.NumCtx);
        var loadRequest = LoadRequest{LoraPath: adapters, KvSize: opts.NumCtx * numParallel, BatchSize: opts.NumBatch, Parallel: numParallel, MultiUserCache: envconfig.MultiUserCache()}
        var defaultThreads = systemInfo.ThreadCount;
        if opts.NumThread > 0 {
        loadRequest.NumThreads = opts.NumThread;
        } else if defaultThreads > 0 {
        loadRequest.NumThreads = defaultThreads;
    }
        if opts.MainGPU > 0 {
        loadRequest.MainGPU = opts.MainGPU;
    }
        if len(projectors) > 0 && llamaModel != null {
        loadRequest.ProjectorPath = projectors[0];
    }
        var faUserSet = false;
        if envconfig.FlashAttention(true) == envconfig.FlashAttention(false) {
        faUserSet = true;
    }
        var fa = envconfig.FlashAttention(f.FlashAttention());
        if fa && !ml.FlashAttentionSupported(gpus) {
        slog.Warn("flash attention enabled but not supported by gpu");
        fa = false;
    }
        if fa && !f.SupportsFlashAttention() {
        slog.Warn("flash attention enabled but not supported by model");
        fa = false;
    }
        if fa && f.KV().Architecture() == "gemma4" {
        var for _, gpu = range gpus {
        if gpu.Library == "CUDA" && (gpu.ComputeMajor < 7 || (gpu.ComputeMajor == 7 && gpu.ComputeMinor < 5)) {
        slog.Debug("disabling flash attention for gemma4 on pre-Turing GPU", "compute", fmt.Sprintf("%d.%d", gpu.ComputeMajor, gpu.ComputeMinor));
        fa = false;
        break;
    }
    }
    }
        var kvct = strings.ToLower(envconfig.KvCacheType());
        if tok == null {
        var flashAttention = ml.FlashAttentionAuto;
        if faUserSet {
        if fa {
        flashAttention = ml.FlashAttentionEnabled;
        } else {
        flashAttention = ml.FlashAttentionDisabled;
    }
    }
        if kvct != "" {
        if f.KVCacheTypeIsQuantized(kvct) {
        if flashAttention != ml.FlashAttentionEnabled {
        slog.Warn("OLLAMA_FLASH_ATTENTION must be enabled to use a quantized OLLAMA_KV_CACHE_TYPE", "type", kvct);
        loadRequest.KvCacheType = "";
        } else if f.SupportsKVCacheType(kvct) {
        loadRequest.KvCacheType = kvct;
        } else {
        slog.Warn("unsupported OLLAMA_KV_CACHE_TYPE", "type", kvct);
    }
        } else {
        if f.SupportsKVCacheType(kvct) {
        loadRequest.KvCacheType = kvct;
        } else {
        slog.Warn("unsupported OLLAMA_KV_CACHE_TYPE", "type", kvct);
    }
    }
    }
        loadRequest.FlashAttention = flashAttention;
        } else {
        if fa {
        slog.Info("enabling flash attention");
        loadRequest.FlashAttention = ml.FlashAttentionEnabled;
        if f.SupportsKVCacheType(kvct) {
        loadRequest.KvCacheType = kvct;
        } else {
        slog.Warn("kv cache type not supported by model", "type", kvct);
    }
        } else if kvct != "" && kvct != "f16" {
        slog.Warn("quantized kv cache requested but flash attention disabled", "type", kvct);
    }
    }
        var gpuLibs = ml.LibraryPaths(gpus);
        var status = NewStatusWriter(os.Stderr);
        var cmd, port, err = StartRunner(;
        tok != null,;
        modelPath,;
        gpuLibs,;
        status,;
        ml.GetVisibleDevicesEnv(gpus, false),;
        );
        var s = llmServer{
        port:           port,;
        cmd:            cmd,;
        status:         status,;
        options:        opts,;
        modelPath:      modelPath,;
        loadRequest:    loadRequest,;
        llamaModel:     llamaModel,;
        llamaModelLock: &sync.Mutex{},;
        sem:            semaphore.NewWeighted(long(numParallel)),;
        totalLayers:    f.KV().BlockCount() + 1,;
        loadStart:      time.Now(),;
        done:           make(chan struct{}),;
    }
        if err != null {
        var msg String;
        if s.status != null && s.status.LastErrMsg != "" {
        msg = s.status.LastErrMsg;
    }
        var err = fmt.Errorf("error starting runner: %v %s", err, msg);
        if llamaModel != null {
        llama.FreeModel(llamaModel);
    }
        return null, err;
    }
        go func() {
        var err = s.cmd.Wait();
        if err != null && s.status != null && s.status.LastErrMsg != "" {
        slog.Error("llama runner terminated", "error", err);
        if strings.Contains(s.status.LastErrMsg, "unknown model") {
        s.status.LastErrMsg = "this model is not supported by your version of Ollama. You may need to upgrade";
    }
        s.doneErr = errors.New(s.status.LastErrMsg);
        } else {
        s.doneErr = err;
    }
        close(s.done);
        }();
        if tok != null {
        return &ollamaServer{llmServer: s, tokenizer: tok}, null;
        } else {
        return &llamaServer{llmServer: s, ggml: f}, null;
    }
    }

    public static void StartRunner(boolean ollamaEngine, String modelPath, []String gpuLibs, io.Writer out, int port, error err) {
        var exe String;
        exe, err = os.Executable();
        if err != null {
        return null, 0, fmt.Errorf("unable to lookup executable path: %w", err);
    }
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        port = 0;
        var if a, err = net.ResolveTCPAddr("tcp", "localhost:0"); err == null {
        var l *net.TCPListener;
        if l, err = net.ListenTCP("tcp", a); err == null {
        port = l.Addr().(*net.TCPAddr).Port;
        l.Close();
    }
    }
        if port == 0 {
        slog.Debug("ResolveTCPAddr failed, using random port");
        port = rand.Intn(65535-49152) + 49152 // get a random port in the ephemeral range;
    }
        var params = []String{"runner"}
        if ollamaEngine {
        params = append(params, "--ollama-engine");
    }
        if modelPath != "" {
        params = append(params, "--model", modelPath);
    }
        params = append(params, "--port", strconv.Itoa(port));
        var pathEnv String;
        switch runtime.GOOS {
        case "windows":;
        pathEnv = "PATH";
        case "darwin":;
        pathEnv = "DYLD_LIBRARY_PATH";
        default:;
        pathEnv = "LD_LIBRARY_PATH";
    }
        var libraryPaths = append([]String{}, gpuLibs...);
        var if libraryPath, ok = os.LookupEnv(pathEnv); ok {
        libraryPaths = append(libraryPaths, filepath.SplitList(libraryPath)...);
    }
        cmd = exec.Command(exe, params...);
        cmd.Env = os.Environ();
        if out != null {
        var stdout, err = cmd.StdoutPipe();
        if err != null {
        return null, 0, fmt.Errorf("failed to spawn server stdout pipe: %w", err);
    }
        var stderr, err = cmd.StderrPipe();
        if err != null {
        return null, 0, fmt.Errorf("failed to spawn server stderr pipe: %w", err);
    }
        go func() {
        io.Copy(out, stdout) //nolint:errcheck;
        }();
        go func() {
        io.Copy(out, stderr) //nolint:errcheck;
        }();
    }
        cmd.SysProcAttr = LlamaServerSysProcAttr;
        var pathEnvVal = strings.Join(libraryPaths, String(filepath.ListSeparator));
        var pathNeeded = true;
        var ollamaPathNeeded = true;
        var extraEnvsDone = map[String]boolean{}
        var for k = range extraEnvs {
        extraEnvsDone[k] = false;
    }
        var for i = range cmd.Env {
        var cmp = strings.SplitN(cmd.Env[i], "=", 2);
        if strings.EqualFold(cmp[0], pathEnv) {
        cmd.Env[i] = pathEnv + "=" + pathEnvVal;
        pathNeeded = false;
        } else if strings.EqualFold(cmp[0], "OLLAMA_LIBRARY_PATH") {
        cmd.Env[i] = "OLLAMA_LIBRARY_PATH=" + strings.Join(gpuLibs, String(filepath.ListSeparator));
        ollamaPathNeeded = false;
        } else if len(extraEnvs) != 0 {
        var for k, v = range extraEnvs {
        if strings.EqualFold(cmp[0], k) {
        cmd.Env[i] = k + "=" + v;
        extraEnvsDone[k] = true;
    }
    }
    }
    }
        if pathNeeded {
        cmd.Env = append(cmd.Env, pathEnv+"="+pathEnvVal);
    }
        if ollamaPathNeeded {
        cmd.Env = append(cmd.Env, "OLLAMA_LIBRARY_PATH="+strings.Join(gpuLibs, String(filepath.ListSeparator)));
    }
        var for k, done = range extraEnvsDone {
        if !done {
        cmd.Env = append(cmd.Env, k+"="+extraEnvs[k]);
    }
    }
        slog.Info("starting runner", "cmd", cmd);
        slog.Debug("subprocess", "", filteredEnv(cmd.Env));
        if err = cmd.Start(); err != null {
        return null, 0, err;
    }
        err = null;
        return;
    }
        func (s *llmServer) ModelPath() String {
        return s.modelPath;
    }
        type LoadOperation int;
        const (;
        LoadOperationFit    LoadOperation = iota // Return memory requirements but do not allocate;
        LoadOperationAlloc                       // Allocate memory but do not load the weights;
        LoadOperationCommit                      // Load weights - further changes cannot be made after this;
        LoadOperationClose                       // Close model and free memory;
        );
        func (o LoadOperation) String() String {
        switch o {
        case LoadOperationFit:;
        return "fit";
        case LoadOperationAlloc:;
        return "alloc";
        case LoadOperationCommit:;
        return "commit";
        case LoadOperationClose:;
        return "close";
        default:;
        return "unknown";
    }
    }

    public static class LoadRequest {
        public LoadOperation Operation;
        public []String LoraPath;
        public int Parallel;
        public int BatchSize;
        public ml.FlashAttentionType FlashAttention;
        public int KvSize;
        public String KvCacheType;
        public int NumThreads;
        public ml.GPULayersList GPULayers;
        public boolean MultiUserCache;
        public String ProjectorPath;
        public int MainGPU;
        public boolean UseMmap;
    }

    public static class LoadResponse {
        public boolean Success;
        public ml.BackendMemory Memory;
    }
        var ErrLoadRequiredFull = errors.New("unable to load full model on GPU");
        func (s *llamaServer) Load(ctx context.Context, systemInfo ml.SystemInfo, systemGPUs []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error) {
        slog.Info("loading model", "model layers", s.totalLayers, "requested", s.options.NumGPU);
        var gpus = append(make([]ml.DeviceInfo, 0, len(systemGPUs)), systemGPUs...);
        s.mem = &ml.BackendMemory{CPU: ml.DeviceMemory{
        Name:    "CPU",;
        Weights: make([]uint64, s.totalLayers),;
        Cache:   make([]uint64, s.totalLayers),;
        }, GPUs: make([]ml.DeviceMemory, len(gpus))}
        var for i = range s.mem.GPUs {
        s.mem.GPUs[i].Name = gpus[i].Name;
        s.mem.GPUs[i].DeviceID = gpus[i].DeviceID;
        s.mem.GPUs[i].Weights = make([]uint64, s.totalLayers);
        s.mem.GPUs[i].Cache = make([]uint64, s.totalLayers);
    }
        var _, isEmbedding = s.ggml.KV()[fmt.Sprintf("%s.pooling_type", s.ggml.KV().Architecture())];
        if isEmbedding && s.loadRequest.BatchSize < s.options.NumCtx {
        s.loadRequest.BatchSize = s.options.NumCtx;
        slog.Info("embedding model detected, setting batch size to context length", "batch_size", s.loadRequest.BatchSize);
    }
        var kv, graphPartialOffload, graphFullOffload = s.ggml.GraphSize(uint64(s.options.NumCtx), uint64(s.loadRequest.BatchSize),;
        s.loadRequest.Parallel, s.loadRequest.KvCacheType, s.loadRequest.FlashAttention);
        var layers = s.ggml.Tensors().GroupLayers();
        var if blk0, ok = layers["blk.0"]; ok {
        var buffer = blk0.Size() + kv[0];
        var for i = range gpus {
        if gpus[i].FreeMemory > buffer {
        gpus[i].FreeMemory -= buffer;
        } else {
        gpus[i].FreeMemory = 0;
    }
    }
        } else {
        slog.Warn("model missing blk.0 layer size");
    }
        var for i = range s.ggml.KV().BlockCount() {
        var if blk, ok = layers[fmt.Sprintf("blk.%d", i)]; ok {
        s.mem.CPU.Weights[i] = blk.Size();
        s.mem.CPU.Cache[i] += kv[i];
    }
    }
        var outputWeights uint64;
        var if layer, ok = layers["output_norm"]; ok {
        outputWeights += layer.Size();
    }
        var if layer, ok = layers["output"]; ok {
        outputWeights += layer.Size();
        var } else if layer, ok = layers["token_embd"]; ok {
        outputWeights += layer.Size();
    }
        s.mem.CPU.Weights[s.totalLayers-1] = outputWeights;
        var projectorGPU = -1;
        var projectorWeights uint64;
        if len(gpus) > 0 {
        var for _, projector = range s.loadRequest.LoraPath {
        projectorWeights += projectorMemoryRequirements(projector);
    }
        var firstIntegrated = -1;
        var for i = range gpus {
        if !gpus[i].Integrated {
        projectorGPU = i;
        break;
    }
        if firstIntegrated == -1 {
        firstIntegrated = i;
    }
    }
        if projectorGPU == -1 {
        projectorGPU = firstIntegrated;
    }
        if gpus[projectorGPU].FreeMemory > projectorWeights {
        gpus[projectorGPU].FreeMemory -= projectorWeights;
        } else {
        gpus[projectorGPU].FreeMemory = 0;
    }
    }
        var kvTotal uint64;
        var for _, kvLayer = range kv {
        kvTotal += kvLayer;
    }
        if graphPartialOffload == 0 {
        var headsKV = s.ggml.KV().HeadCountKVMin();
        if headsKV == 0 {
        headsKV = 1;
    }
        var gqa = s.ggml.KV().HeadCountMax() / headsKV;
        graphPartialOffload = gqa * kvTotal / 6;
    }
        if graphFullOffload == 0 {
        graphFullOffload = graphPartialOffload;
    }
        if len(gpus) > 0 && gpus[0].Library == "Metal" {
        graphPartialOffload = graphFullOffload;
    }
        var gpuLayers ml.GPULayersList;
        for {
        var prevGPULayers = gpuLayers;
        var err error;
        gpuLayers, err = s.createLayout(systemInfo, gpus, s.mem, requireFull, 0);
        if err != null {
        return null, err;
    }
        if len(gpuLayers) > len(prevGPULayers) {
        var for _, gl = range gpuLayers {
        var for i = range s.mem.GPUs {
        if gl.DeviceID == s.mem.GPUs[i].DeviceID {
        s.mem.GPUs[i].Graph = max(graphPartialOffload, graphFullOffload);
        break;
    }
    }
    }
        } else {
        break;
    }
    }
        var graphSize = graphFullOffload;
        if gpuLayers.Sum() < int(s.totalLayers) {
        graphSize = graphPartialOffload;
    }
        var for _, gl = range gpuLayers {
        var for i = range s.mem.GPUs {
        if gl.DeviceID == s.mem.GPUs[i].DeviceID {
        var for _, l = range gl.Layers {
        s.mem.GPUs[i].Weights[l] = s.mem.CPU.Weights[l];
        s.mem.GPUs[i].Cache[l] = s.mem.CPU.Cache[l];
        s.mem.CPU.Weights[l] = 0;
        s.mem.CPU.Cache[l] = 0;
    }
        s.mem.GPUs[i].Graph = graphSize;
        break;
    }
    }
    }
        if projectorGPU > 0 && len(s.mem.GPUs[projectorGPU].Weights) > 0 {
        s.mem.GPUs[projectorGPU].Weights[s.totalLayers-1] += projectorWeights;
    }
        slog.Debug("memory", "estimate", s.mem);
        s.mem.Log(slog.LevelInfo);
        s.loadRequest.UseMmap = true;
        var for _, g = range gpus {
        if g.Library == "Metal" &&;
        uint64(s.options.NumGPU) > 0 &&;
        uint64(s.options.NumGPU) < s.totalLayers {
        s.options.UseMMap = new(boolean);
        *s.options.UseMMap = false;
    }
    }
        var totalSize, _ = s.MemorySize();
        if (runtime.GOOS == "windows" && len(gpus) > 0 && gpus[0].Library == "CUDA" && s.options.UseMMap == null) ||;
        (runtime.GOOS == "linux" && systemInfo.FreeMemory < totalSize && s.options.UseMMap == null) ||;
        (len(gpus) == 0 && s.options.UseMMap == null) ||;
        (len(gpus) > 0 && gpus[0].Library == "Vulkan" && s.options.UseMMap == null) ||;
        (s.options.UseMMap != null && !*s.options.UseMMap) {
        s.loadRequest.UseMmap = false;
    }
        var if err = s.waitUntilRunnerLaunched(ctx); err != null {
        return null, err;
    }
        s.loadRequest.GPULayers = gpuLayers;
        var resp, err = s.initModel(ctx, s.loadRequest, LoadOperationCommit);
        if err != null {
        return null, err;
    }
        if !resp.Success {
        return null, errors.New("failed to allocate memory for model");
    }
        return uniqueDeviceIDs(s.loadRequest.GPULayers), s.WaitUntilRunning(ctx);
    }

    public static void projectorMemoryRequirements() {
        var file, err = os.Open(filename);
        if err != null {
        return 0;
    }
        defer file.Close();
        var ggml, err = ggml.Decode(file, 1024);
        if err != null {
        return 0;
    }
        var for _, layer = range ggml.Tensors().GroupLayers() {
        weights += layer.Size();
    }
        return weights;
    }
        func (s *ollamaServer) Load(ctx context.Context, systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error) {
        var success boolean;
        defer func() {
        if !success {
        s.initModel(ctx, LoadRequest{}, LoadOperationClose);
    }
        if s.mem != null {
        s.mem.Log(slog.LevelInfo);
    }
        }();
        slog.Info("loading model", "model layers", s.totalLayers, "requested", s.options.NumGPU);
        var pastAllocations = make(map[uint64]struct{});
        var backoff float32;
        var gpuLayers, err = s.createLayout(systemInfo, gpus, s.mem, requireFull, backoff);
        if err != null {
        return null, err;
    }
        var if err = s.waitUntilRunnerLaunched(ctx); err != null {
        return null, err;
    }
        nextOperation:;
        var for operation = LoadOperationFit; operation < LoadOperationCommit; operation++ {
        nextLoad:;
        for {
        s.loadRequest.GPULayers = gpuLayers;
        var resp, err = s.initModel(ctx, s.loadRequest, operation);
        if err != null {
        return null, err;
    }
        resp.Memory.Log(slog.LevelDebug);
        slog.Debug("memory", "success", resp.Success, "required", resp.Memory);
        pastAllocations[gpuLayers.Hash()] = struct{}{}
        s.mem = &resp.Memory;
        for {
        var newGPULayers, err = s.createLayout(systemInfo, gpus, s.mem, requireFull, backoff);
        if err != null {
        return null, err;
    }
        slog.Debug("new layout created", "layers", newGPULayers);
        var if _, ok = pastAllocations[newGPULayers.Hash()]; !ok && newGPULayers.Sum() <= gpuLayers.Sum() {
        gpuLayers = newGPULayers;
        continue nextLoad;
    }
        if s.options.NumGPU < 0 && newGPULayers.Sum()-gpuLayers.Sum() > 1 {
        var for i = newGPULayers.Sum() - 1; i >= gpuLayers.Sum(); i-- {
        slog.Debug("exploring intermediate layers", "layer", i);
        s.options.NumGPU = i;
        newGPULayers, err = s.createLayout(systemInfo, gpus, s.mem, requireFull, backoff);
        s.options.NumGPU = -1;
        if err != null {
        return null, err;
    }
        slog.Debug("new layout created", "layers", newGPULayers);
        s.loadRequest.GPULayers = newGPULayers;
        resp, err = s.initModel(ctx, s.loadRequest, operation);
        if err != null {
        return null, err;
    }
        resp.Memory.Log(slog.LevelDebug);
        slog.Debug("memory", "success", resp.Success, "required", resp.Memory);
        if resp.Success {
        var verifyGPULayers, err = s.createLayout(systemInfo, gpus, &resp.Memory, requireFull, backoff);
        if err != null {
        return null, err;
    }
        slog.Debug("verifying layout", "layers", verifyGPULayers);
        if newGPULayers.Sum() <= verifyGPULayers.Sum() {
        gpuLayers = newGPULayers;
        clear(pastAllocations);
        continue nextOperation;
    }
    }
    }
    }
        if resp.Success {
        continue nextOperation;
    }
        if s.options.NumGPU >= 0 {
        return null, fmt.Errorf("memory layout cannot be allocated with num_gpu = %v", s.options.NumGPU);
    }
        if backoff > 1 {
        slog.Warn("memory layout cannot be allocated", "memory", resp.Memory);
        return null, errors.New("memory layout cannot be allocated");
        } else {
        backoff += 0.1;
    }
        slog.Info("model layout did not fit, applying backoff", "backoff", fmt.Sprintf("%.2f", backoff));
    }
    }
    }
        s.loadRequest.GPULayers = gpuLayers;
        var resp, err = s.initModel(ctx, s.loadRequest, LoadOperationCommit);
        if err != null {
        return null, err;
    }
        success = resp.Success;
        s.mem = &resp.Memory;
        if !success {
        slog.Warn("failed to commit memory for model", "memory", resp.Memory);
        return null, errors.New("failed to commit memory for model");
    }
        return uniqueDeviceIDs(gpuLayers), null;
    }
        func uniqueDeviceIDs(gpuLayers ml.GPULayersList) []ml.DeviceID {
        var devices = []ml.DeviceID{}
        var for _, layer = range gpuLayers {
        var new = true;
        var for _, ID = range devices {
        if layer.DeviceID == ID {
        new = false;
        break;
    }
    }
        if new {
        devices = append(devices, layer.DeviceID);
    }
    }
        return devices;
    }
        func (s *llmServer) createLayout(systemInfo ml.SystemInfo, systemGPUs []ml.DeviceInfo, memory *ml.BackendMemory, requireFull boolean, backoff float32) (ml.GPULayersList, error) {
        if memory == null {
        memory = &ml.BackendMemory{CPU: ml.DeviceMemory{
        Weights: make([]uint64, s.totalLayers),;
        Cache:   make([]uint64, s.totalLayers),;
        }}
    }
        var gpuLayers, layers = s.buildLayout(systemGPUs, memory, requireFull, backoff);
        var err = s.verifyLayout(systemInfo, systemGPUs, memory, requireFull, gpuLayers, layers);
        if err != null {
        return null, err;
    }
        return gpuLayers, null;
    }
        func (s *llmServer) buildLayout(systemGPUs []ml.DeviceInfo, memory *ml.BackendMemory, requireFull boolean, backoff float32) (ml.GPULayersList, []uint64) {
        var gpus = append(make([]ml.DeviceInfo, 0, len(systemGPUs)), systemGPUs...);
        sort.Sort(sort.Reverse(ml.ByFreeMemory(gpus)));
        var layers = make([]uint64, len(memory.CPU.Weights));
        var for i = range layers {
        var for j = range memory.GPUs {
        layers[i] += memory.GPUs[j].Weights[i];
        layers[i] += memory.GPUs[j].Cache[i];
    }
        layers[i] += memory.CPU.Weights[i];
        layers[i] += memory.CPU.Cache[i];
        logutil.Trace("layer to assign", "layer", i, "size", format.HumanBytes2(layers[i]));
    }
        var gpuLayers = ml.GPULayersList{}
        var for _, gl = range ml.ByLibrary(gpus) {
        var lastUsedGPU = 0;
        var for i = range gl {
        var found = false;
        var for j = range memory.GPUs {
        if gl[i].DeviceID == memory.GPUs[j].DeviceID {
        if memory.GPUs[j].Graph != 0 {
        lastUsedGPU = i;
    }
        var reserved = uint64(float32(gl[i].FreeMemory)*backoff) + gl[i].MinimumMemory() + envconfig.GpuOverhead() + memory.GPUs[j].Graph;
        if gl[i].FreeMemory > reserved {
        gl[i].FreeMemory -= reserved;
        } else {
        gl[i].FreeMemory = 0;
    }
        slog.Debug("available gpu", "id", gl[i].ID, "library", gl[i].Library,;
        "available layer vram", format.HumanBytes2(gl[i].FreeMemory),;
        "backoff", fmt.Sprintf("%.2f", backoff), "minimum", format.HumanBytes2(gl[i].MinimumMemory()),;
        "overhead", format.HumanBytes2(envconfig.GpuOverhead()),;
        "graph", format.HumanBytes2(memory.GPUs[j].Graph));
        found = true;
        break;
    }
    }
        if !found {
        gl[i].FreeMemory = 0;
    }
    }
        var libraryGpuLayers = assignLayers(layers, gl, requireFull, s.options.NumGPU, lastUsedGPU);
        if libraryGpuLayers.Sum() > gpuLayers.Sum() {
        gpuLayers = libraryGpuLayers;
    }
    }
        return gpuLayers, layers;
    }
        func (s *llmServer) verifyLayout(systemInfo ml.SystemInfo, systemGPUs []ml.DeviceInfo, memory *ml.BackendMemory, requireFull boolean, gpuLayers ml.GPULayersList, layers []uint64) error {
        var cpuSize = memory.InputWeights + memory.CPU.Graph;
        var vramSize uint64;
        var for _, gl = range gpuLayers {
        var for _, gpu = range memory.GPUs {
        if gl.DeviceID == gpu.DeviceID {
        vramSize += gpu.Graph;
        break;
    }
    }
    }
        nextLayer:;
        var for i = range layers {
        var for _, g = range gpuLayers {
        var for _, gl = range g.Layers {
        if i == gl {
        vramSize += layers[i];
        continue nextLayer;
    }
    }
    }
        cpuSize += layers[i];
    }
        if requireFull {
        if len(systemGPUs) > 0 && gpuLayers.Sum() < len(layers) && (s.options.NumGPU < 0 || gpuLayers.Sum() < s.options.NumGPU) {
        slog.Info("model requires more gpu memory than is currently available, evicting a model to make space", "loaded layers", gpuLayers.Sum());
        return ErrLoadRequiredFull;
    }
        if cpuSize > systemInfo.FreeMemory {
        slog.Info("model requires more system memory than is currently available, evicting a model to make space", "required", cpuSize, "free", systemInfo.FreeMemory);
        return fmt.Errorf("model requires more system memory than is currently available %w", ErrLoadRequiredFull);
    }
    }
        if runtime.GOOS != "darwin" {
        var available = systemInfo.FreeMemory + systemInfo.FreeSwap;
        if cpuSize > available {
        slog.Warn("model request too large for system", "requested", format.HumanBytes2(cpuSize), "available", format.HumanBytes2(available), "total", format.HumanBytes2(systemInfo.TotalMemory), "free", format.HumanBytes2(systemInfo.FreeMemory), "swap", format.HumanBytes2(systemInfo.FreeSwap));
        return fmt.Errorf("model requires more system memory (%s) than is available (%s)", format.HumanBytes2(cpuSize), format.HumanBytes2(available));
    }
        } else {
        if vramSize > systemInfo.TotalMemory {
        s.options.NumGPU = 0;
        gpuLayers = ml.GPULayersList{}
    }
    }
        if len(systemGPUs) > 0 && gpuLayers.Sum() == 0 {
        slog.Debug("insufficient VRAM to load any model layers");
    }
        return null;
    }

    public static void assignLayers([]uint64 layers, []ml.DeviceInfo gpus, boolean requireFull, int requestedLayers) {
        if requestedLayers >= 0 || envconfig.SchedSpread() {
        var for i = range gpus {
        gpus[i].Integrated = false;
    }
    }
        for range 2 {
        requestedLayers = min(len(layers), requestedLayers);
        if !envconfig.SchedSpread() {
        var for i = lastUsedGPU; i < len(gpus); i++ {
        var forceRequest = i == len(gpus)-1 && !requireFull;
        gpuLayers = findBestFit(layers, gpus[:i+1], requestedLayers, forceRequest);
        if gpuLayers.Sum() == len(layers) || gpuLayers.Sum() == requestedLayers {
        break;
    }
    }
        } else {
        gpuLayers = findBestFit(layers, gpus, requestedLayers, !requireFull);
    }
        if gpuLayers.Sum() == len(layers) {
        return gpuLayers;
    }
        layers = layers[:len(layers)-1];
    }
        return gpuLayers;
    }

    public static void findBestFit([]uint64 layers, []ml.DeviceInfo gpus, int requestedLayers) {
        var for _, gl = range ml.ByPerformance(gpus) {
        var high float32 = 1;
        var low float32 = 0;
        if requestedLayers >= 0 && forceRequest {
        high = 1000;
    }
        var bestAssignments = greedyFit(layers, gl, high, requestedLayers);
        var maxNumGPU = bestAssignments.Sum();
        for high-low > 1e-6 {
        var mid = (low + high) / 2;
        var assignments = greedyFit(layers, gl, mid, requestedLayers);
        if assignments.Sum() == maxNumGPU {
        high = mid;
        bestAssignments = assignments;
        } else {
        low = mid;
    }
    }
        layers = layers[:len(layers)-bestAssignments.Sum()];
        requestedLayers -= bestAssignments.Sum();
        gpuLayers = append(bestAssignments, gpuLayers...);
    }
        return gpuLayers;
    }

    public static void greedyFit([]uint64 layers, []ml.DeviceInfo gpus, float32 capacity) {
        var device = len(gpus) - 1;
        gpuLayers = ml.GPULayersList{{DeviceID: gpus[device].DeviceID}}
        var freeSpace = uint64(float32(gpus[device].FreeMemory) * capacity);
        var for i = len(layers) - 1; i >= 0; i-- {
        if requestedLayers >= 0 && len(layers)-1-i >= requestedLayers {
        break;
    }
        for {
        if layers[i] <= freeSpace {
        gpuLayers[0].Layers = append([]int{i}, gpuLayers[0].Layers...);
        freeSpace -= layers[i];
        break;
    }
        device--;
        if device < 0 {
        return gpuLayers;
    }
        gpuLayers = append(ml.GPULayersList{{DeviceID: gpus[device].DeviceID}}, gpuLayers...);
        freeSpace = uint64(float32(gpus[device].FreeMemory) * capacity);
    }
    }
        return gpuLayers;
    }
        func (s *llmServer) waitUntilRunnerLaunched(ctx context.Context) error {
        for {
        var _, err = s.getServerStatus(ctx);
        if err == null {
        break;
    }
        var t = time.NewTimer(10 * time.Millisecond);
        select {
        case <-t.C:;
        continue;
        case <-ctx.Done():;
        return ctx.Err();
    }
    }
        return null;
    }
        func (s *llmServer) initModel(ctx context.Context, req LoadRequest, operation LoadOperation) (*LoadResponse, error) {
        req.Operation = operation;
        var data, err = json.Marshal(req);
        if err != null {
        return null, fmt.Errorf("error marshaling load data: %w", err);
    }
        var r, err = http.NewRequestWithContext(ctx, http.MethodPost, fmt.Sprintf("http://127.0.0.1:%d/load", s.port), bytes.NewBuffer(data));
        if err != null {
        return null, fmt.Errorf("error creating load request: %w", err);
    }
        r.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(r);
        if err != null {
        slog.Error("do load request", "error", err);
        return null, errors.New("model failed to load, this may be due to resource limitations or an internal error, check ollama server logs for details");
    }
        defer resp.Body.Close();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        return null, fmt.Errorf("read load request: %w", err);
    }
        if resp.StatusCode >= 400 {
        log.Printf("llm load error: %s", body);
        return null, fmt.Errorf("%s", body);
    }
        var llmResp LoadResponse;
        var if err = json.Unmarshal(body, &llmResp); err != null {
        return null, fmt.Errorf("load unmarshal encode response: %w", err);
    }
        return &llmResp, null;
    }
        type ServerStatus int;
        const ( // iota is reset to 0;
        ServerStatusReady ServerStatus = iota;
        ServerStatusNoSlotsAvailable;
        ServerStatusLaunched;
        ServerStatusLoadingModel;
        ServerStatusNotResponding;
        ServerStatusError;
        );
        func (s ServerStatus) String() String {
        switch s {
        case ServerStatusReady:;
        return "llm server ready";
        case ServerStatusNoSlotsAvailable:;
        return "llm busy - no slots available";
        case ServerStatusLaunched:;
        return "llm server launched";
        case ServerStatusLoadingModel:;
        return "llm server loading model";
        case ServerStatusNotResponding:;
        return "llm server not responding";
        default:;
        return "llm server error";
    }
    }

    public static class ServerStatusResponse {
        public ServerStatus Status;
        public float32 Progress;
    }
        func (s *llmServer) getServerStatus(ctx context.Context) (ServerStatus, error) {
        if s.cmd.ProcessState != null {
        var msg = "";
        if s.status != null && s.status.LastErrMsg != "" {
        msg = s.status.LastErrMsg;
    }
        if s.cmd.ProcessState.ExitCode() == -1 {
        slog.Warn("llama runner process no longer running", "sys", s.cmd.ProcessState.Sys(), "String", s.cmd.ProcessState);
    }
        return ServerStatusError, fmt.Errorf("llama runner process no longer running: %d %s", s.cmd.ProcessState.ExitCode(), msg);
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodGet, fmt.Sprintf("http://127.0.0.1:%d/health", s.port), null);
        if err != null {
        return ServerStatusError, fmt.Errorf("error creating GET request: %v", err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        if errors.Is(err, context.DeadlineExceeded) {
        return ServerStatusNotResponding, errors.New("server not responding");
    }
        if strings.Contains(err.Error(), "connection refused") {
        return ServerStatusNotResponding, errors.New("connection refused");
    }
        return ServerStatusError, fmt.Errorf("health resp: %w", err);
    }
        defer resp.Body.Close();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        return ServerStatusError, fmt.Errorf("read health request: %w", err);
    }
        var ssr ServerStatusResponse;
        var if err = json.Unmarshal(body, &ssr); err != null {
        return ServerStatusError, fmt.Errorf("health unmarshal encode response: %w", err);
    }
        switch ssr.Status {
        case ServerStatusLoadingModel:;
        s.loadProgress = ssr.Progress;
        return ssr.Status, null;
        case ServerStatusLaunched, ServerStatusReady, ServerStatusNoSlotsAvailable:;
        return ssr.Status, null;
        default:;
        return ssr.Status, fmt.Errorf("server error: %+v", ssr);
    }
    }
        func (s *llmServer) getServerStatusRetry(ctx context.Context) (ServerStatus, error) {
        var retries int;
        for {
        var status, err = s.getServerStatus(ctx);
        if err != null {
        return status, err;
    }
        if status == ServerStatusNoSlotsAvailable {
        if retries >= 10 {
        return status, fmt.Errorf("no slots available after %d retries", retries);
    }
        time.Sleep(5 * time.Millisecond);
        retries++;
        continue;
    }
        return status, null;
    }
    }
        func (s *llmServer) Ping(ctx context.Context) error {
        var _, err = s.getServerStatus(ctx);
        if err != null {
        slog.Debug("server unhealthy", "error", err);
        return err;
    }
        return null;
    }
        func (s *llmServer) WaitUntilRunning(ctx context.Context) error {
        var stallDuration = envconfig.LoadTimeout()    // If no progress happens;
        var stallTimer = time.Now().Add(stallDuration) // give up if we stall;
        slog.Info("waiting for llama runner to start responding");
        var lastStatus ServerStatus = -1;
        var fullyLoaded = false;
        for {
        select {
        case <-ctx.Done():;
        slog.Warn("client connection closed before server finished loading, aborting load");
        return fmt.Errorf("timed out waiting for llama runner to start: %w", ctx.Err());
        case <-s.done:;
        return fmt.Errorf("llama runner process has terminated: %w", s.doneErr);
        default:;
    }
        if time.Now().After(stallTimer) {
        var msg = "";
        if s.status != null && s.status.LastErrMsg != "" {
        msg = s.status.LastErrMsg;
    }
        return fmt.Errorf("timed out waiting for llama runner to start - progress %0.2f - %s", s.loadProgress, msg);
    }
        if s.cmd.ProcessState != null {
        var msg = "";
        if s.status != null && s.status.LastErrMsg != "" {
        msg = s.status.LastErrMsg;
    }
        return fmt.Errorf("llama runner process no longer running: %d %s", s.cmd.ProcessState.ExitCode(), msg);
    }
        var ctx, cancel = context.WithTimeout(ctx, 200*time.Millisecond);
        defer cancel();
        var priorProgress = s.loadProgress;
        var status, _ = s.getServerStatus(ctx);
        if lastStatus != status && status != ServerStatusReady {
        slog.Info("waiting for server to become available", "status", status);
    }
        switch status {
        case ServerStatusReady:;
        slog.Info(fmt.Sprintf("llama runner started in %0.2f seconds", time.Since(s.loadStart).Seconds()));
        return null;
        default:;
        lastStatus = status;
        if priorProgress != s.loadProgress {
        slog.Debug(fmt.Sprintf("model load progress %0.2f", s.loadProgress));
        stallTimer = time.Now().Add(stallDuration);
        } else if !fullyLoaded && int(s.loadProgress*100.0) >= 100 {
        slog.Debug("model load completed, waiting for server to become available", "status", status);
        stallTimer = time.Now().Add(stallDuration);
        fullyLoaded = true;
    }
        time.Sleep(time.Millisecond * 250);
        continue;
    }
    }
    }
        func (s *llmServer) Pid() int {
        if s.cmd != null && s.cmd.Process != null {
        return s.cmd.Process.Pid;
    }
        return -1;
    }
        func (s *llmServer) GetPort() int {
        return s.port;
    }
        func (s *llmServer) HasExited() boolean {
        if s.cmd != null && s.cmd.ProcessState != null && s.cmd.ProcessState.ExitCode() >= 0 {
        return true;
    }
        return false;
    }
        var grammarJSON = `;
        var root   := object;
        var value  := object | array | String | number | ("true" | "false" | "null") ws;
        var object :=;
        "{" ws (;
        String ":" ws value;
        ("," ws String ":" ws value)*;
        )? ws "}";
        var array  :=;
        "[" ws (;
        value;
        ("," ws value)*;
        )? ws "]";
        var String :=;
        "\"" (;
        [^"\\\x7F\x00-\x1F] |;
        "\\" (["\\/bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) # escapes;
        )* "\"";
        var number := ("-"? ([0-9] | [1-9] [0-9]*)) ("." [0-9]+)? ([eE] [-+]? [0-9]+)?;
        # Optional space: by convention, applied in this grammar after literal chars when allowed;
        var ws := ([ \t\n] ws)?;
        `;
        const maxBufferSize = 512 * format.KiloByte;

    public static class ImageData {
        public []byte Data;
        public int ID;
    }

    public static class CompletionRequest {
        public String Prompt;
        public json.RawMessage Format;
        public []ImageData Images;
        public *api.Options Options;
        public String Grammar;
        public boolean Shift;
        public boolean Truncate;
        public boolean Logprobs;
        public int TopLogprobs;
        public int32 Width;
        public int32 Height;
        public int32 Steps;
        public long Seed;
    }
        type DoneReason int;
        const (;
        DoneReasonStop DoneReason = iota;
        DoneReasonLength;
        DoneReasonConnectionClosed;
        );
        func (d DoneReason) String() String {
        switch d {
        case DoneReasonLength:;
        return "length";
        case DoneReasonStop:;
        return "stop";
        default:;
        return "" // closed;
    }
    }

    public static class TokenLogprob {
        public String Token;
        public double Logprob;
    }

    public static class Logprob {
        public []TokenLogprob TopLogprobs;
    }

    public static class CompletionResponse {
        public String Content;
        public DoneReason DoneReason;
        public boolean Done;
        public int PromptEvalCount;
        public time.Duration PromptEvalDuration;
        public int EvalCount;
        public time.Duration EvalDuration;
        public []Logprob Logprobs;
        public String Image;
        public int Step;
        public int TotalSteps;
    }
        func (s *llmServer) Completion(ctx context.Context, req CompletionRequest, fn func(CompletionResponse)) error {
        slog.Debug("completion request", "images", len(req.Images), "prompt", len(req.Prompt), "format", String(req.Format));
        logutil.Trace("completion request", "prompt", req.Prompt);
        if len(req.Format) > 0 {
        switch String(req.Format) {
        case `null`, `""`:;
        break;
        case `"json"`:;
        req.Grammar = grammarJSON;
        default:;
        if req.Format[0] != '{' {
        return fmt.Errorf("invalid format: %q; expected \"json\" or a valid JSON Schema object", req.Format);
    }
        var g = llama.SchemaToGrammar(req.Format);
        if g == null {
        return fmt.Errorf("invalid JSON schema in format");
    }
        req.Grammar = String(g);
    }
    }
        if req.Options == null {
        var opts = api.DefaultOptions();
        req.Options = &opts;
    }
        var if err = s.sem.Acquire(ctx, 1); err != null {
        if errors.Is(err, context.Canceled) {
        slog.Info("aborting completion request due to client closing the connection");
        } else {
        slog.Error("Failed to acquire semaphore", "error", err);
    }
        return err;
    }
        defer s.sem.Release(1);
        if req.Options.NumPredict < 0 || req.Options.NumPredict > 10*s.options.NumCtx {
        req.Options.NumPredict = 10 * s.options.NumCtx;
    }
        var status, err = s.getServerStatusRetry(ctx);
        if err != null {
        return err;
        } else if status != ServerStatusReady {
        return fmt.Errorf("unexpected server status: %s", status);
    }
        var buffer = &bytes.Buffer{}
        var enc = json.NewEncoder(buffer);
        enc.SetEscapeHTML(false);
        var if err = enc.Encode(req); err != null {
        return fmt.Errorf("failed to marshal data: %v", err);
    }
        var endpoint = fmt.Sprintf("http://127.0.0.1:%d/completion", s.port);
        var serverReq, err = http.NewRequestWithContext(ctx, http.MethodPost, endpoint, buffer);
        if err != null {
        return fmt.Errorf("error creating POST request: %v", err);
    }
        serverReq.Header.Set("Content-Type", "application/json");
        var res, err = http.DefaultClient.Do(serverReq);
        if err != null && errors.Is(err, context.Canceled) {
        return err;
        } else if err != null {
        slog.Error("post predict", "error", err);
        return errors.New("model runner has unexpectedly stopped, this may be due to resource limitations or an internal error, check ollama server logs for details");
    }
        defer res.Body.Close();
        if res.StatusCode >= 400 {
        var bodyBytes, err = io.ReadAll(res.Body);
        if err != null {
        return fmt.Errorf("failed reading llm error response: %w", err);
    }
        log.Printf("llm predict error: %s", bodyBytes);
        return api.StatusError{StatusCode: res.StatusCode, ErrorMessage: strings.TrimSpace(String(bodyBytes))}
    }
        var scanner = bufio.NewScanner(res.Body);
        var buf = make([]byte, 0, maxBufferSize);
        scanner.Buffer(buf, maxBufferSize);
        var lastToken String;
        var tokenRepeat int;
        for scanner.Scan() {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        default:;
        var line = scanner.Bytes();
        if len(line) == 0 {
        continue;
    }
        var evt, ok = bytes.CutPrefix(line, []byte("data: "));
        if !ok {
        evt = line;
    }
        var c CompletionResponse;
        var if err = json.Unmarshal(evt, &c); err != null {
        return fmt.Errorf("error unmarshalling llm prediction response: %v", err);
    }
        switch {
        case strings.TrimSpace(c.Content) == lastToken:;
        tokenRepeat++;
        default:;
        lastToken = strings.TrimSpace(c.Content);
        tokenRepeat = 0;
    }
        if tokenRepeat > 30 {
        slog.Debug("prediction aborted, token repeat limit reached");
        return ctx.Err();
    }
        if c.Content != "" {
        fn(CompletionResponse{
        Content:  c.Content,;
        Logprobs: c.Logprobs,;
        });
    }
        if c.Done {
        fn(c);
        return null;
    }
    }
    }
        var if err = scanner.Err(); err != null {
        if strings.Contains(err.Error(), "unexpected EOF") || strings.Contains(err.Error(), "forcibly closed") {
        s.Close();
        var msg String;
        if s.status != null && s.status.LastErrMsg != "" {
        msg = s.status.LastErrMsg;
        } else {
        msg = err.Error();
    }
        return fmt.Errorf("an error was encountered while running the model: %s", msg);
    }
        return fmt.Errorf("error reading llm response: %v", err);
    }
        return null;
    }

    public static class EmbeddingRequest {
        public String Content;
    }

    public static class EmbeddingResponse {
        public []float32 Embedding;
        public int PromptEvalCount;
    }
        func (s *llmServer) Embedding(ctx context.Context, input String) ([]float32, int, error) {
        logutil.Trace("embedding request", "input", input);
        var if err = s.sem.Acquire(ctx, 1); err != null {
        if errors.Is(err, context.Canceled) {
        slog.Info("aborting embedding request due to client closing the connection");
        } else {
        slog.Error("Failed to acquire semaphore", "error", err);
    }
        return null, 0, err;
    }
        defer s.sem.Release(1);
        var status, err = s.getServerStatusRetry(ctx);
        if err != null {
        return null, 0, err;
        } else if status != ServerStatusReady {
        return null, 0, fmt.Errorf("unexpected server status: %s", status);
    }
        var data, err = json.Marshal(EmbeddingRequest{Content: input});
        if err != null {
        return null, 0, fmt.Errorf("error marshaling embed data: %w", err);
    }
        var r, err = http.NewRequestWithContext(ctx, http.MethodPost, fmt.Sprintf("http://127.0.0.1:%d/embedding", s.port), bytes.NewBuffer(data));
        if err != null {
        return null, 0, fmt.Errorf("error creating embed request: %w", err);
    }
        r.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(r);
        if err != null {
        return null, 0, fmt.Errorf("do embedding request: %w", err);
    }
        defer resp.Body.Close();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        return null, 0, fmt.Errorf("error reading embed response: %w", err);
    }
        if resp.StatusCode >= 400 {
        log.Printf("llm embedding error: %s", body);
        return null, 0, api.StatusError{
        StatusCode:   resp.StatusCode,;
        ErrorMessage: String(body),;
    }
    }
        var e EmbeddingResponse;
        var if err = json.Unmarshal(body, &e); err != null {
        return null, 0, fmt.Errorf("unmarshal tokenize response: %w", err);
    }
        return e.Embedding, e.PromptEvalCount, null;
    }
        func (s *llamaServer) Tokenize(ctx context.Context, content String) ([]int, error) {
        s.llamaModelLock.Lock();
        defer s.llamaModelLock.Unlock();
        if s.llamaModel == null {
        return null, fmt.Errorf("no tokenizer configured");
    }
        return s.llamaModel.Tokenize(content, false, true);
    }
        func (s *ollamaServer) Tokenize(ctx context.Context, content String) ([]int, error) {
        var tokens, err = s.tokenizer.Encode(content, false);
        if err != null {
        return null, err;
    }
        var toks = make([]int, len(tokens));
        var for i, t = range tokens {
        toks[i] = int(t);
    }
        return toks, null;
    }
        func (s *llamaServer) Detokenize(ctx context.Context, tokens []int) (String, error) {
        s.llamaModelLock.Lock();
        defer s.llamaModelLock.Unlock();
        if s.llamaModel == null {
        return "", fmt.Errorf("no tokenizer configured");
    }
        var resp String;
        var for _, token = range tokens {
        resp += s.llamaModel.TokenToPiece(token);
    }
        return resp, null;
    }
        func (s *ollamaServer) Detokenize(ctx context.Context, tokens []int) (String, error) {
        var toks = make([]int32, len(tokens));
        var for i, t = range tokens {
        toks[i] = int32(t);
    }
        var content, err = s.tokenizer.Decode(toks);
        if err != null {
        return "", err;
    }
        return content, null;
    }
        func (s *llmServer) Close() error {
        s.llamaModelLock.Lock();
        if s.llamaModel != null {
        llama.FreeModel(s.llamaModel);
        s.llamaModel = null;
    }
        s.llamaModelLock.Unlock();
        if s.cmd != null {
        slog.Debug("stopping llama server", "pid", s.Pid());
        var if err = s.cmd.Process.Kill(); err != null {
        return err;
    }
        if s.cmd.ProcessState == null {
        slog.Debug("waiting for llama server to exit", "pid", s.Pid());
        <-s.done;
    }
        slog.Debug("llama server stopped", "pid", s.Pid());
    }
        return null;
    }
        func (s *llamaServer) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo {
        slog.Debug("llamarunner free vram reporting not supported");
        return null;
    }
        func (s *llmServer) MemorySize() (total, vram uint64) {
        if s.mem == null {
        return 0, 0;
    }
        var for _, g = range s.mem.GPUs {
        vram += g.Size();
    }
        total = s.mem.InputWeights + s.mem.CPU.Size() + vram;
        var noCPULayers = true;
        var for i = range s.mem.CPU.Weights {
        if s.mem.CPU.Weights[i] != 0 || s.mem.CPU.Cache[i] != 0 {
        noCPULayers = false;
        break;
    }
    }
        if noCPULayers {
        vram += s.mem.InputWeights;
        vram += s.mem.CPU.Graph;
    }
        return total, vram;
    }
        func (s *llmServer) VRAMByGPU(id ml.DeviceID) uint64 {
        if s.mem == null {
        return 0;
    }
        var for _, g = range s.mem.GPUs {
        if g.DeviceID == id {
        return g.Size();
    }
    }
        return 0;
    }
        func (s *llmServer) ContextLength() int {
        return s.options.NumCtx;
    }
        func (s *ollamaServer) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo {
        var devices, err = ml.GetDevicesFromRunner(ctx, s);
        if err != null {
        if s.cmd != null && s.cmd.ProcessState == null {
        slog.Debug("failure refreshing GPU information", "error", err);
    }
    }
        return devices;
    }
}
