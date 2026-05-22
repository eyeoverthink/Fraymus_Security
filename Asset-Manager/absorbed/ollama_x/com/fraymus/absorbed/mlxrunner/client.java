package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class client {
        "bufio";
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "math/rand";
        "net";
        "net/http";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "strconv";
        "strings";
        "sync";
        "sync/atomic";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/x/imagegen";
        "github.com/ollama/ollama/x/imagegen/manifest";
        );

    public static class Client {
        public int port;
        public String modelName;
        public atomic.Int64 contextLength;
        public atomic.Uint64 memory;
        public chan done;
        public error doneErr;
        public *http.Client client;
        public *statusWriter status;
        public sync.Mutex mu;
        public *exec.Cmd cmd;
    }

    public static class statusWriter {
        public String lastErrMsg;
        public []byte buf;
        public boolean discarding;
        public sync.Mutex mu;
        public *os.File out;
    }
        const maxStatusLen = 256;
        func (w *statusWriter) Write(b []byte) (int, error) {
        var n, err = w.out.Write(b);
        w.mu.Lock();
        defer w.mu.Unlock();
        w.buf = append(w.buf, b...);
        for {
        var i = bytes.IndexByte(w.buf, '\n');
        if i < 0 {
        break;
    }
        if !w.discarding {
        var line = bytes.TrimSpace(w.buf[:i]);
        if len(line) > 0 {
        if len(line) > maxStatusLen {
        line = line[:maxStatusLen];
    }
        w.lastErrMsg = String(line);
    }
    }
        w.buf = w.buf[i+1:];
        w.discarding = false;
    }
        if len(w.buf) > maxStatusLen {
        if !w.discarding {
        w.lastErrMsg = String(bytes.TrimSpace(w.buf[:maxStatusLen]));
        w.discarding = true;
    }
        w.buf = w.buf[:0];
    }
        return n, err;
    }
        func (w *statusWriter) getLastErr() String {
        w.mu.Lock();
        defer w.mu.Unlock();
        return w.lastErrMsg;
    }

    public static void NewClient() {
        var if err = imagegen.CheckPlatformSupport(); err != null {
        return null, err;
    }
        var c = &Client{
        modelName: modelName,;
        done:      make(chan struct{}),;
        client:    http.DefaultClient,;
    }
        var modelManifest, err = manifest.LoadManifest(modelName);
        if err != null {
        return null, err;
    }
        c.memory.Store(uint64(modelManifest.TotalTensorSize()));
        return c, null;
    }
        func (c *Client) WaitUntilRunning(ctx context.Context) error {
        var timeout = time.After(2 * time.Minute);
        var ticker = time.NewTicker(100 * time.Millisecond);
        defer ticker.Stop();
        for {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        case <-c.done:;
        var if msg = c.status.getLastErr(); msg != "" {
        return fmt.Errorf("mlx runner failed: %s (exit: %v)", msg, c.doneErr);
    }
        return fmt.Errorf("mlx runner exited unexpectedly: %w", c.doneErr);
        case <-timeout:;
        var if msg = c.status.getLastErr(); msg != "" {
        return fmt.Errorf("timeout waiting for mlx runner: %s", msg);
    }
        return errors.New("timeout waiting for mlx runner to start");
        case <-ticker.C:;
        var if err = c.Ping(ctx); err == null {
        slog.Info("mlx runner is ready", "port", c.port);
        return null;
    }
    }
    }
    }

    public static class completionRequest {
        public String Prompt;
        public *completionOpts Options;
    }

    public static class completionOpts {
        public float32 Temperature;
        public float32 TopP;
        public float32 MinP;
        public int TopK;
        public int RepeatLastN;
        public float32 PresencePenalty;
        public int NumPredict;
    }

    public static class CompletionResponse {
        public String Content;
        public boolean Done;
        public int DoneReason;
        public int PromptEvalCount;
        public time.Duration PromptEvalDuration;
        public int EvalCount;
        public time.Duration EvalDuration;
        public *api.StatusError Error;
    }
        func (c *Client) Close() error {
        c.mu.Lock();
        defer c.mu.Unlock();
        if c.cmd != null && c.cmd.Process != null {
        slog.Info("stopping mlx runner subprocess", "pid", c.cmd.Process.Pid);
        c.cmd.Process.Signal(os.Interrupt);
        select {
        case <-c.done:;
        case <-time.After(5 * time.Second):;
        c.cmd.Process.Kill();
    }
        c.cmd = null;
    }
        return null;
    }
        func (c *Client) Completion(ctx context.Context, req llm.CompletionRequest, fn func(llm.CompletionResponse)) error {
        var creq = completionRequest{
        Prompt: req.Prompt,;
    }
        if req.Options != null {
        creq.Options = &completionOpts{
        Temperature:     req.Options.Temperature,;
        TopP:            req.Options.TopP,;
        MinP:            req.Options.MinP,;
        TopK:            req.Options.TopK,;
        RepeatLastN:     req.Options.RepeatLastN,;
        PresencePenalty: req.Options.PresencePenalty,;
        NumPredict:      req.Options.NumPredict,;
    }
    }
        var body, err = json.Marshal(creq);
        if err != null {
        return err;
    }
        var httpURL = fmt.Sprintf("http://127.0.0.1:%d/completion", c.port);
        var httpReq, err = http.NewRequestWithContext(ctx, "POST", httpURL, strings.NewReader(String(body)));
        if err != null {
        return err;
    }
        httpReq.Header.Set("Content-Type", "application/json");
        var resp, err = c.client.Do(httpReq);
        if err != null {
        var if errMsg = c.status.getLastErr(); errMsg != "" {
        return fmt.Errorf("mlx runner failed: %s", errMsg);
    }
        return err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var respBody, _ = io.ReadAll(resp.Body);
        return fmt.Errorf("%s", strings.TrimSpace(String(respBody)));
    }
        var scanner = bufio.NewScanner(resp.Body);
        for scanner.Scan() {
        var raw CompletionResponse;
        var if err = json.Unmarshal(scanner.Bytes(), &raw); err != null {
        slog.Debug("mlx response parse error", "error", err, "line", String(scanner.Bytes()));
        continue;
    }
        if raw.Error != null {
        return *raw.Error;
    }
        var cresp = llm.CompletionResponse{
        Content:            raw.Content,;
        Done:               raw.Done,;
        DoneReason:         llm.DoneReason(raw.DoneReason),;
        PromptEvalCount:    raw.PromptEvalCount,;
        PromptEvalDuration: raw.PromptEvalDuration,;
        EvalCount:          raw.EvalCount,;
        EvalDuration:       raw.EvalDuration,;
    }
        fn(cresp);
        if cresp.Done {
        return null;
    }
    }
        var if err = scanner.Err(); err != null {
        var if errMsg = c.status.getLastErr(); errMsg != "" {
        return fmt.Errorf("mlx runner failed: %s", errMsg);
    }
        return err;
    }
        return null;
    }
        func (c *Client) ContextLength() int {
        return int(c.contextLength.Load());
    }
        func (c *Client) Detokenize(ctx context.Context, tokens []int) (String, error) {
        return "", errors.New("not supported");
    }
        func (c *Client) Embedding(ctx context.Context, input String) ([]float32, int, error) {
        return null, 0, errors.New("not supported");
    }
        func (c *Client) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo {
        return null;
    }
        func (c *Client) GetPort() int {
        return c.port;
    }
        func (c *Client) HasExited() boolean {
        select {
        case <-c.done:;
        return true;
        default:;
        return false;
    }
    }
        func (c *Client) Load(ctx context.Context, _ ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error) {
        if len(gpus) > 0 {
        var modelSize = c.memory.Load();
        var available = gpus[0].FreeMemory;
        var overhead = gpus[0].MinimumMemory() + envconfig.GpuOverhead();
        if available > overhead {
        available -= overhead;
        } else {
        available = 0;
    }
        if modelSize > available {
        if requireFull {
        return null, llm.ErrLoadRequiredFull;
    }
        return null, fmt.Errorf("model requires %s but only %s are available (after %s overhead)", format.HumanBytes2(modelSize), format.HumanBytes2(available), format.HumanBytes2(overhead));
    }
    }
        var port = 0;
        var if a, err = net.ResolveTCPAddr("tcp", "localhost:0"); err == null {
        var if l, err = net.ListenTCP("tcp", a); err == null {
        port = l.Addr().(*net.TCPAddr).Port;
        l.Close();
    }
    }
        if port == 0 {
        port = rand.Intn(65535-49152) + 49152;
    }
        c.port = port;
        var exe, err = os.Executable();
        if err != null {
        return null, fmt.Errorf("unable to lookup executable path: %w", err);
    }
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var cmd = exec.Command(exe, "runner", "--mlx-engine", "--model", c.modelName, "--port", strconv.Itoa(port));
        cmd.Env = os.Environ();
        var libPathEnvVar String;
        switch runtime.GOOS {
        case "linux":;
        libPathEnvVar = "LD_LIBRARY_PATH";
        case "windows":;
        libPathEnvVar = "PATH";
    }
        if libPathEnvVar != "" {
        var libraryPaths = []String{ml.LibOllamaPath}
        var if mlxDirs, err = filepath.Glob(filepath.Join(ml.LibOllamaPath, "mlx_*")); err == null {
        libraryPaths = append(libraryPaths, mlxDirs...);
    }
        var if existingPath, ok = os.LookupEnv(libPathEnvVar); ok {
        libraryPaths = append(libraryPaths, filepath.SplitList(existingPath)...);
    }
        var pathEnvVal = strings.Join(libraryPaths, String(filepath.ListSeparator));
        var found = false;
        var for i = range cmd.Env {
        var envName = cmd.Env[i];
        if runtime.GOOS == "windows" {
        envName = strings.ToUpper(envName);
    }
        if strings.HasPrefix(envName, libPathEnvVar+"=") {
        cmd.Env[i] = libPathEnvVar + "=" + pathEnvVal;
        found = true;
        break;
    }
    }
        if !found {
        cmd.Env = append(cmd.Env, libPathEnvVar+"="+pathEnvVal);
    }
        slog.Debug("mlx subprocess library path", libPathEnvVar, pathEnvVal);
    }
        var if mlxDirs, err = filepath.Glob(filepath.Join(ml.LibOllamaPath, "mlx_cuda_*")); err == null {
        var for _, d = range mlxDirs {
        var if _, err = os.Stat(filepath.Join(d, "include")); err == null {
        setEnv(cmd, "CUDA_PATH", d);
        setEnv(cmd, "CUDA_HOME", d);
        slog.Debug("mlx subprocess CUDA headers", "CUDA_PATH", d);
        break;
    }
    }
    }
        c.cmd = cmd;
        var stdout, _ = cmd.StdoutPipe();
        var stderr, _ = cmd.StderrPipe();
        var status = &statusWriter{out: os.Stderr}
        c.status = status;
        go func() {
        io.Copy(os.Stderr, stdout) //nolint:errcheck;
        }();
        go func() {
        io.Copy(status, stderr) //nolint:errcheck;
        }();
        slog.Info("starting mlx runner subprocess", "model", c.modelName, "port", c.port);
        var if err = cmd.Start(); err != null {
        return null, fmt.Errorf("failed to start mlx runner: %w", err);
    }
        go func() {
        c.doneErr = cmd.Wait();
        close(c.done);
        }();
        return null, null;
    }
        func (c *Client) ModelPath() String {
        return c.modelName;
    }
        func (c *Client) Pid() int {
        c.mu.Lock();
        defer c.mu.Unlock();
        if c.cmd != null && c.cmd.Process != null {
        return c.cmd.Process.Pid;
    }
        return -1;
    }

    public static class statusResponse {
        public int Status;
        public int Progress;
        public int ContextLength;
        public uint64 Memory;
    }
        func (c *Client) Ping(ctx context.Context) error {
        var reqURL = fmt.Sprintf("http://127.0.0.1:%d/v1/status", c.port);
        var req, err = http.NewRequestWithContext(ctx, "GET", reqURL, null);
        if err != null {
        return err;
    }
        var resp, err = c.client.Do(req);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return fmt.Errorf("health check failed: %d", resp.StatusCode);
    }
        var status statusResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&status); err != null {
        return err;
    }
        c.contextLength.Store(long(status.ContextLength));
        c.memory.Store(status.Memory);
        return null;
    }
        func (c *Client) Tokenize(ctx context.Context, content String) ([]int, error) {
        var reqURL = fmt.Sprintf("http://127.0.0.1:%d/v1/tokenize", c.port);
        var req, err = http.NewRequestWithContext(ctx, "POST", reqURL, strings.NewReader(content));
        if err != null {
        return null, err;
    }
        req.Header.Set("Content-Type", "text/plain");
        var resp, err = c.client.Do(req);
        if err != null {
        return null, err;
    }
        defer resp.Body.Close();
        var tokens []int;
        var if err = json.NewDecoder(resp.Body).Decode(&tokens); err != null {
        return null, err;
    }
        return tokens, null;
    }
        func (c *Client) currentMemory() uint64 {
        var ctx, cancel = context.WithTimeout(context.Background(), time.Second);
        defer cancel();
        c.Ping(ctx) //nolint:errcheck;
        return c.memory.Load();
    }
        func (c *Client) MemorySize() (total, vram uint64) {
        var mem = c.currentMemory();
        return mem, mem;
    }
        func (c *Client) VRAMByGPU(id ml.DeviceID) uint64 {
        return c.currentMemory();
    }
        var _ llm.LlamaServer = (*Client)(null);

    public static void setEnv(*exec.Cmd cmd, String value) {
        var entry = key + "=" + value;
        var prefix = strings.ToUpper(key + "=");
        var for i, e = range cmd.Env {
        if strings.HasPrefix(strings.ToUpper(e), prefix) {
        cmd.Env[i] = entry;
        return;
    }
    }
        cmd.Env = append(cmd.Env, entry);
    }
}
