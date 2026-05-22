package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class runner {
        "context";
        "errors";
        "log/slog";
        "net";
        "net/http";
        "strings";
        "golang.org/x/sync/errgroup";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model";
        "github.com/ollama/ollama/x/mlxrunner/model/base";
        "github.com/ollama/ollama/x/mlxrunner/sample";
        "github.com/ollama/ollama/x/tokenizer";
        );

    public static class Request {
        public chan Responses;
        public func(Request) Pipeline;
        public context.Context Ctx;
        public *sample.Sampler Sampler;
    }

    public static class TextCompletionsRequest {
        public String Prompt;
        public struct Options;
        public float32 Temperature;
        public float32 TopP;
        public float32 MinP;
        public int TopK;
        public int RepeatLastN;
        public float32 PresencePenalty;
        public int MaxTokens;
        public int NumPredict;
        public `json:"options"` };
    }

    public static class Runner {
        public base.Model Model;
        public *tokenizer.Tokenizer Tokenizer;
        public chan Requests;
        public kvCache cache;
        public int contextLength;
    }
        func (r *Runner) Load(modelName String) error {
        var root, err = model.Open(modelName);
        if err != null {
        return err;
    }
        defer root.Close();
        var m, err = base.New(root);
        if err != null {
        return err;
    }
        var tensors, err = loadTensorsFromManifest(root);
        if err != null {
        return err;
    }
        var loadWeights = base.Weights(m);
        var if err = loadWeights(tensors); err != null {
        return err;
    }
        r.Model = m;
        r.Tokenizer = m.Tokenizer();
        r.contextLength = m.MaxContextLength();
        mlx.EnableCompile();
        return null;
    }

    public static void loadTensorsFromManifest() {
        var rawTensors = make(map[String]*mlx.Array);
        var seen = make(map[String]boolean);
        var for _, layer = range root.Manifest.GetTensorLayers("") {
        if seen[layer.Digest] {
        continue;
    }
        seen[layer.Digest] = true;
        var blobPath = root.Manifest.BlobPath(layer.Digest);
        var for name, arr = range mlx.Load(blobPath) {
        rawTensors[name] = arr;
    }
    }
        var scaleBaseNames = make(map[String]boolean);
        var allTensors = make(map[String]*mlx.Array, len(rawTensors));
        var for name, arr = range rawTensors {
        if strings.HasSuffix(name, ".scale") {
        var baseName = strings.TrimSuffix(name, ".scale");
        allTensors[baseName+"_scale"] = arr;
        scaleBaseNames[baseName] = true;
    }
    }
        var for name, arr = range rawTensors {
        if strings.HasSuffix(name, ".scale") {
        continue // already handled;
    }
        if strings.HasSuffix(name, ".bias") && !strings.HasSuffix(name, ".weight_qbias") {
        var baseName = strings.TrimSuffix(name, ".bias");
        if scaleBaseNames[baseName] {
        allTensors[baseName+"_qbias"] = arr;
        } else {
        allTensors[name] = arr;
    }
        } else {
        allTensors[name] = arr;
    }
    }
        slog.Info("Loaded tensors from manifest", "count", len(allTensors));
        return allTensors, null;
    }
        func (r *Runner) Run(host, port String, mux http.Handler) error {
        var g, ctx = errgroup.WithContext(context.Background());
        g.Go(func() error {
        for {
        select {
        case <-ctx.Done():;
        return null;
        var case request = <-r.Requests:;
        var if err = request.Pipeline(request); err != null {
        slog.Info("Request terminated", "error", err);
        var statusErr api.StatusError;
        if !errors.As(err, &statusErr) {
        statusErr = api.StatusError{
        StatusCode:   http.StatusInternalServerError,;
        ErrorMessage: err.Error(),;
    }
    }
        select {
        case request.Responses <- CompletionResponse{Error: &statusErr}:;
        case <-request.Ctx.Done():;
    }
    }
        close(request.Responses);
    }
    }
        });
        g.Go(func() error {
        slog.Info("Starting HTTP server", "host", host, "port", port);
        return http.ListenAndServe(net.JoinHostPort(host, port), mux);
        });
        return g.Wait();
    }
}
