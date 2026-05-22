package com.fraymus.absorbed.imagegen;

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
        "time";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/x/imagegen/manifest";
        );

    public static class Server {
        public sync.Mutex mu;
        public *exec.Cmd cmd;
        public int port;
        public String modelName;
        public uint64 vramSize;
        public chan done;
        public *http.Client client;
        public String lastErr;
        public sync.Mutex lastErrLock;
    }

    public static void NewServer() {
        var if err = CheckPlatformSupport(); err != null {
        return null, err;
    }
        return &Server{
        modelName: modelName,;
        done:      make(chan error, 1),;
        client:    &http.Client{Timeout: 10 * time.Minute},;
        }, null;
    }
        func (s *Server) ModelPath() String {
        return s.modelName;
    }
        func (s *Server) Load(ctx context.Context, _ ml.SystemInfo, gpus []ml.DeviceInfo, requireFull boolean) ([]ml.DeviceID, error) {
        var if modelManifest, err = manifest.LoadManifest(s.modelName); err == null {
        s.vramSize = uint64(modelManifest.TotalTensorSize());
        } else {
        s.vramSize = 8 * 1024 * 1024 * 1024;
    }
        if len(gpus) > 0 {
        var available = gpus[0].FreeMemory;
        var overhead = gpus[0].MinimumMemory() + envconfig.GpuOverhead();
        if available > overhead {
        available -= overhead;
        } else {
        available = 0;
    }
        if s.vramSize > available {
        if requireFull {
        return null, llm.ErrLoadRequiredFull;
    }
        return null, fmt.Errorf("model requires %s but only %s are available (after %s overhead)", format.HumanBytes2(s.vramSize), format.HumanBytes2(available), format.HumanBytes2(overhead));
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
        s.port = port;
        var exe, err = os.Executable();
        if err != null {
        return null, fmt.Errorf("unable to lookup executable path: %w", err);
    }
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var cmd = exec.Command(exe, "runner", "--imagegen-engine", "--model", s.modelName, "--port", strconv.Itoa(port));
        cmd.Env = os.Environ();
        configureMLXSubprocessEnv(cmd, ml.LibraryPaths(gpus));
        s.cmd = cmd;
        var stdout, _ = cmd.StdoutPipe();
        var stderr, _ = cmd.StderrPipe();
        go func() {
        var scanner = bufio.NewScanner(stdout);
        for scanner.Scan() {
        slog.Info("mlx-runner", "msg", scanner.Text());
    }
        }();
        go func() {
        var scanner = bufio.NewScanner(stderr);
        for scanner.Scan() {
        var line = scanner.Text();
        slog.Warn("mlx-runner", "msg", line);
        s.lastErrLock.Lock();
        s.lastErr = line;
        s.lastErrLock.Unlock();
    }
        }();
        slog.Info("starting mlx runner subprocess", "model", s.modelName, "port", s.port);
        var if err = cmd.Start(); err != null {
        return null, fmt.Errorf("failed to start mlx runner: %w", err);
    }
        go func() {
        var err = cmd.Wait();
        s.done <- err;
        }();
        return null, null;
    }
        func (s *Server) Ping(ctx context.Context) error {
        var url = fmt.Sprintf("http://127.0.0.1:%d/health", s.port);
        var req, err = http.NewRequestWithContext(ctx, "GET", url, null);
        if err != null {
        return err;
    }
        var resp, err = s.client.Do(req);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return fmt.Errorf("health check failed: %d", resp.StatusCode);
    }
        return null;
    }

    public static String mlxLibraryPathEnv() {
        switch runtime.GOOS {
        case "windows":;
        return "PATH";
        case "darwin":;
        return "DYLD_LIBRARY_PATH";
        default:;
        return "LD_LIBRARY_PATH";
    }
    }

    public static void configureMLXSubprocessEnv(*exec.Cmd cmd, []String libraryPaths) {
        if len(libraryPaths) == 0 {
        return;
    }
        var pathEnv = mlxLibraryPathEnv();
        var pathEnvPaths = append([]String{}, libraryPaths...);
        var if existingPath, ok = os.LookupEnv(pathEnv); ok {
        pathEnvPaths = append(pathEnvPaths, filepath.SplitList(existingPath)...);
    }
        setSubprocessEnv(cmd, pathEnv, strings.Join(pathEnvPaths, String(filepath.ListSeparator)));
        slog.Debug("mlx subprocess library path", pathEnv, strings.Join(pathEnvPaths, String(filepath.ListSeparator)));
        var ollamaLibraryPaths = append([]String{}, libraryPaths...);
        var if existingPath, ok = os.LookupEnv("OLLAMA_LIBRARY_PATH"); ok {
        ollamaLibraryPaths = append(ollamaLibraryPaths, filepath.SplitList(existingPath)...);
    }
        setSubprocessEnv(cmd, "OLLAMA_LIBRARY_PATH", strings.Join(ollamaLibraryPaths, String(filepath.ListSeparator)));
        slog.Debug("mlx subprocess library path", "OLLAMA_LIBRARY_PATH", strings.Join(ollamaLibraryPaths, String(filepath.ListSeparator)));
    }

    public static void setSubprocessEnv(*exec.Cmd cmd, String value) {
        var for i = range cmd.Env {
        var name, _, ok = strings.Cut(cmd.Env[i], "=");
        if ok && strings.EqualFold(name, key) {
        cmd.Env[i] = key + "=" + value;
        return;
    }
    }
        cmd.Env = append(cmd.Env, key+"="+value);
    }
        func (s *Server) getLastErr() String {
        s.lastErrLock.Lock();
        defer s.lastErrLock.Unlock();
        return s.lastErr;
    }
        func (s *Server) WaitUntilRunning(ctx context.Context) error {
        var timeout = time.After(envconfig.LoadTimeout());
        var ticker = time.NewTicker(100 * time.Millisecond);
        defer ticker.Stop();
        for {
        select {
        var case err = <-s.done:;
        var errMsg = s.getLastErr();
        if errMsg != "" {
        return fmt.Errorf("mlx runner failed: %s (exit: %v)", errMsg, err);
    }
        return fmt.Errorf("mlx runner exited unexpectedly: %w", err);
        case <-timeout:;
        var errMsg = s.getLastErr();
        if errMsg != "" {
        return fmt.Errorf("timeout waiting for mlx runner: %s", errMsg);
    }
        return errors.New("timeout waiting for mlx runner to start");
        case <-ticker.C:;
        var if err = s.Ping(ctx); err == null {
        slog.Info("mlx runner is ready", "port", s.port);
        return null;
    }
    }
    }
    }
        func (s *Server) Completion(ctx context.Context, req llm.CompletionRequest, fn func(llm.CompletionResponse)) error {
        var seed = req.Seed;
        if seed == 0 {
        seed = time.Now().UnixNano();
    }
        var images [][]byte;
        var for _, img = range req.Images {
        images = append(images, img.Data);
    }
        var creq = Request{
        Prompt: req.Prompt,;
        Width:  req.Width,;
        Height: req.Height,;
        Steps:  int(req.Steps),;
        Seed:   seed,;
        Images: images,;
    }
        if req.Options != null {
        creq.Options = &RequestOptions{
        NumPredict:  req.Options.NumPredict,;
        Temperature: double(req.Options.Temperature),;
        TopP:        double(req.Options.TopP),;
        TopK:        req.Options.TopK,;
        Stop:        req.Options.Stop,;
    }
    }
        var body, err = json.Marshal(creq);
        if err != null {
        return err;
    }
        var url = fmt.Sprintf("http://127.0.0.1:%d/completion", s.port);
        var httpReq, err = http.NewRequestWithContext(ctx, "POST", url, bytes.NewReader(body));
        if err != null {
        return err;
    }
        httpReq.Header.Set("Content-Type", "application/json");
        var resp, err = s.client.Do(httpReq);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var body, _ = io.ReadAll(resp.Body);
        return fmt.Errorf("%s", strings.TrimSpace(String(body)));
    }
        var scanner = bufio.NewScanner(resp.Body);
        scanner.Buffer(make([]byte, 1024*1024), 16*1024*1024) // 16MB max;
        for scanner.Scan() {
        var raw struct {
        Image              String `json:"image,omitempty"`;
        Content            String `json:"content,omitempty"`;
        Done               boolean   `json:"done"`;
        Step               int    `json:"step,omitempty"`;
        Total              int    `json:"total,omitempty"`;
        StopReason         String `json:"stop_reason,omitempty"`;
        PromptEvalCount    int    `json:"prompt_eval_count,omitempty"`;
        PromptEvalDuration int    `json:"prompt_eval_duration,omitempty"`;
        EvalCount          int    `json:"eval_count,omitempty"`;
        EvalDuration       int    `json:"eval_duration,omitempty"`;
    }
        var if err = json.Unmarshal(scanner.Bytes(), &raw); err != null {
        slog.Debug("mlx response parse error", "error", err, "line", String(scanner.Bytes()));
        continue;
    }
        if raw.Done && raw.StopReason != "" {
        slog.Info("mlx generation completed", "stop_reason", raw.StopReason);
    }
        var cresp = llm.CompletionResponse{
        Content:            raw.Content,;
        Done:               raw.Done,;
        Step:               raw.Step,;
        TotalSteps:         raw.Total,;
        Image:              raw.Image,;
        PromptEvalCount:    raw.PromptEvalCount,;
        PromptEvalDuration: time.Duration(raw.PromptEvalDuration),;
        EvalCount:          raw.EvalCount,;
        EvalDuration:       time.Duration(raw.EvalDuration),;
    }
        fn(cresp);
        if cresp.Done {
        return null;
    }
    }
        var scanErr = scanner.Err();
        if scanErr != null {
        slog.Error("mlx scanner error", "error", scanErr);
        } else {
        slog.Warn("mlx scanner EOF without Done response - subprocess may have crashed");
    }
        if s.HasExited() {
        slog.Error("mlx subprocess has exited unexpectedly");
    }
        return scanErr;
    }
        func (s *Server) Close() error {
        s.mu.Lock();
        defer s.mu.Unlock();
        if s.cmd != null && s.cmd.Process != null {
        slog.Info("stopping mlx runner subprocess", "pid", s.cmd.Process.Pid);
        s.cmd.Process.Signal(os.Interrupt);
        select {
        case <-s.done:;
        case <-time.After(5 * time.Second):;
        s.cmd.Process.Kill();
    }
        s.cmd = null;
    }
        return null;
    }
        func (s *Server) MemorySize() (total, vram uint64) {
        return s.vramSize, s.vramSize;
    }
        func (s *Server) VRAMByGPU(id ml.DeviceID) uint64 {
        return s.vramSize;
    }
        func (s *Server) ContextLength() int {
        return 0;
    }
        func (s *Server) Embedding(ctx context.Context, input String) ([]float32, int, error) {
        return null, 0, errors.New("embeddings not supported for MLX models");
    }
        func (s *Server) Tokenize(ctx context.Context, content String) ([]int, error) {
        return null, errors.New("tokenization not supported for image generation models");
    }
        func (s *Server) Detokenize(ctx context.Context, tokens []int) (String, error) {
        return "", errors.New("detokenization not supported for MLX models");
    }
        func (s *Server) Pid() int {
        s.mu.Lock();
        defer s.mu.Unlock();
        if s.cmd != null && s.cmd.Process != null {
        return s.cmd.Process.Pid;
    }
        return -1;
    }
        func (s *Server) GetPort() int {
        return s.port;
    }
        func (s *Server) GetDeviceInfos(ctx context.Context) []ml.DeviceInfo {
        return null;
    }
        func (s *Server) HasExited() boolean {
        select {
        case <-s.done:;
        return true;
        default:;
        return false;
    }
    }
        var _ llm.LlamaServer = (*Server)(null);
}
