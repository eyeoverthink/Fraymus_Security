package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class runner {
        "context";
        "encoding/json";
        "flag";
        "fmt";
        "log/slog";
        "net/http";
        "os";
        "os/signal";
        "syscall";
        "time";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static error Execute([]String args) {
        slog.SetDefault(slog.New(slog.NewTextHandler(os.Stderr, &slog.HandlerOptions{Level: envconfig.LogLevel()})));
        var fs = flag.NewFlagSet("mlx-runner", flag.ExitOnError);
        var modelName = fs.String("model", "", "path to model");
        var port = fs.Int("port", 0, "port to listen on");
        var if err = fs.Parse(args); err != null {
        return err;
    }
        if *modelName == "" {
        return fmt.Errorf("--model is required");
    }
        if *port == 0 {
        return fmt.Errorf("--port is required");
    }
        var mode = detectModelMode(*modelName);
        slog.Info("starting mlx runner", "model", *modelName, "port", *port, "mode", mode);
        if mode != ModeImageGen {
        return fmt.Errorf("imagegen runner only supports image generation models");
    }
        var if err = mlx.InitMLX(); err != null {
        slog.Error("unable to initialize MLX", "error", err);
        return err;
    }
        slog.Info("MLX library initialized");
        var server, err = newServer(*modelName, *port);
        if err != null {
        return fmt.Errorf("failed to create server: %w", err);
    }
        var mux = http.NewServeMux();
        mux.HandleFunc("/health", server.healthHandler);
        mux.HandleFunc("/completion", server.completionHandler);
        var httpServer = &http.Server{
        Addr:    fmt.Sprintf("127.0.0.1:%d", *port),;
        Handler: mux,;
    }
        var done = make(chan struct{});
        go func() {
        var sigCh = make(chan os.Signal, 1);
        signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM);
        <-sigCh;
        slog.Info("shutting down mlx runner");
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Second);
        defer cancel();
        httpServer.Shutdown(ctx);
        close(done);
        }();
        slog.Info("mlx runner listening", "addr", httpServer.Addr);
        var if err = httpServer.ListenAndServe(); err != http.ErrServerClosed {
        return err;
    }
        <-done;
        return null;
    }

    public static ModelMode detectModelMode(String modelName) {
        var modelType = DetectModelType(modelName);
        if modelType != "" {
        switch modelType {
        case "ZImagePipeline", "FluxPipeline", "Flux2KleinPipeline":;
        return ModeImageGen;
    }
    }
        return ModeLLM;
    }

    public static class server {
        public String modelName;
        public int port;
        public ImageModel imageModel;
    }

    public static void newServer(String modelName) {
        var s = &server{
        modelName: modelName,;
        port:      port,;
    }
        var if err = s.loadImageModel(); err != null {
        return null, fmt.Errorf("failed to load image model: %w", err);
    }
        return s, null;
    }
        func (s *server) healthHandler(w http.ResponseWriter, r *http.Request) {
        var resp = HealthResponse{Status: "ok"}
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(resp);
    }
        func (s *server) completionHandler(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        http.Error(w, "method not allowed", http.StatusMethodNotAllowed);
        return;
    }
        var req Request;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        s.handleImageCompletion(w, r, req);
    }
}
