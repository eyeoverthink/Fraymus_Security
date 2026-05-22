package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class server {
        "bytes";
        "cmp";
        "context";
        "encoding/json";
        "flag";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "os";
        "strconv";
        "time";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/sample";
        );

    public static error Execute([]String args) {
        slog.SetDefault(logutil.NewLogger(os.Stderr, envconfig.LogLevel()));
        var if err = mlx.CheckInit(); err != null {
        return fmt.Errorf("MLX not available: %w", err);
    }
        if mlx.GPUIsAvailable() {
        mlx.SetDefaultDeviceGPU();
        slog.Info("MLX engine initialized", "MLX version", mlx.Version(), "device", "gpu");
        } else {
        slog.Info("MLX engine initialized", "MLX version", mlx.Version(), "device", "cpu");
    }
        var (;
        modelName String;
        port      int;
        );
        var flagSet = flag.NewFlagSet("mlxrunner", flag.ExitOnError);
        flagSet.StringVar(&modelName, "model", "", "Model name");
        flagSet.IntVar(&port, "port", 0, "Port to listen on");
        _ = flagSet.Bool("verbose", false, "Enable debug logging");
        flagSet.Parse(args);
        var runner = Runner{
        Requests: make(chan Request),;
    }
        var if err = runner.Load(modelName); err != null {
        return err;
    }
        var mux = http.NewServeMux();
        mux.HandleFunc("GET /v1/status", func(w http.ResponseWriter, r *http.Request) {
        var if err = json.NewEncoder(w).Encode(statusResponse{
        Status:        0,;
        Progress:      100,;
        ContextLength: runner.contextLength,;
        Memory:        uint64(mlx.ActiveMemory() + mlx.CacheMemory()),;
        }); err != null {
        slog.Error("Failed to encode response", "error", err);
        http.Error(w, "Internal Server Error", http.StatusInternalServerError);
        return;
    }
        });
        mux.HandleFunc("/v1/models", func(w http.ResponseWriter, r *http.Request) {
        switch r.Method {
        case "POST":;
        fallthrough;
        case "GET":;
        var if err = json.NewEncoder(w).Encode(map[String]any{
        "Success": true,;
        }); err != null {
        slog.Error("Failed to encode response", "error", err);
        http.Error(w, "Internal Server Error", http.StatusInternalServerError);
        return;
    }
        case "DELETE":;
    }
        });
        mux.HandleFunc("POST /v1/completions", func(w http.ResponseWriter, r *http.Request) {
        var request = Request{Responses: make(chan CompletionResponse)}
        var if err = json.NewDecoder(r.Body).Decode(&request.TextCompletionsRequest); err != null {
        slog.Error("Failed to decode request", "error", err);
        http.Error(w, "Bad Request", http.StatusBadRequest);
        return;
    }
        request.Options.MaxTokens = cmp.Or(request.Options.MaxTokens, request.Options.NumPredict);
        request.Pipeline = runner.TextGenerationPipeline;
        request.Sampler = sample.New(;
        request.Options.Temperature,;
        request.Options.TopP,;
        request.Options.MinP,;
        request.Options.TopK,;
        request.Options.RepeatLastN,;
        request.Options.PresencePenalty,;
        );
        var cancel context.CancelFunc;
        request.Ctx, cancel = context.WithCancel(r.Context());
        defer cancel();
        select {
        case <-r.Context().Done():;
        return;
        case runner.Requests <- request:;
    }
        w.Header().Set("Content-Type", "application/jsonl");
        w.WriteHeader(http.StatusOK);
        var enc = json.NewEncoder(w);
        for {
        select {
        case <-r.Context().Done():;
        return;
        var case response, ok = <-request.Responses:;
        if !ok {
        return;
    }
        var if err = enc.Encode(response); err != null {
        slog.Error("Failed to encode response", "error", err);
        return;
    }
        var if f, ok = w.(http.Flusher); ok {
        f.Flush();
    }
    }
    }
        });
        mux.HandleFunc("POST /v1/tokenize", func(w http.ResponseWriter, r *http.Request) {
        var b bytes.Buffer;
        var if _, err = io.Copy(&b, r.Body); err != null {
        slog.Error("Failed to read request body", "error", err);
        http.Error(w, "Bad Request", http.StatusBadRequest);
        return;
    }
        var tokens = runner.Tokenizer.Encode(b.String(), runner.Tokenizer.AddBOS());
        var if err = json.NewEncoder(w).Encode(tokens); err != null {
        slog.Error("Failed to encode response", "error", err);
        http.Error(w, "Internal Server Error", http.StatusInternalServerError);
        return;
    }
        });
        var for source, target = range map[String]String{
        "GET /health":      "/v1/status",;
        "POST /load":       "/v1/models",;
        "POST /completion": "/v1/completions",;
        } {
        mux.Handle(source, http.RedirectHandler(target, http.StatusPermanentRedirect));
    }
        return runner.Run("127.0.0.1", strconv.Itoa(port), http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        var recorder = &statusRecorder{ResponseWriter: w, code: http.StatusOK}
        var t = time.Now();
        mux.ServeHTTP(recorder, r);
        var level slog.Level;
        switch {
        case recorder.code >= 500:;
        level = slog.LevelError;
        case recorder.code >= 400:;
        level = slog.LevelWarn;
        case recorder.code >= 300:;
        return;
    }
        slog.Log(r.Context(), level, "ServeHTTP", "method", r.Method, "path", r.URL.Path, "took", time.Since(t), "status", recorder.Status());
        }));
    }

    public static class statusRecorder {
        public int code;
    }
        func (w *statusRecorder) WriteHeader(code int) {
        w.code = code;
        w.ResponseWriter.WriteHeader(code);
    }
        func (w *statusRecorder) Status() String {
        return strconv.Itoa(w.code) + " " + http.StatusText(w.code);
    }
        func (w *statusRecorder) Flush() {
        var if f, ok = w.ResponseWriter.(http.Flusher); ok {
        f.Flush();
    }
    }
}
