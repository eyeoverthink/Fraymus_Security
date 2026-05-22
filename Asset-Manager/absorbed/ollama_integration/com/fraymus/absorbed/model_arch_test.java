package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model_arch_test {
        "context";
        "encoding/json";
        "fmt";
        "io/ioutil";
        "log/slog";
        "os";
        "path/filepath";
        "strconv";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/format";
        );

    public static void TestModelsChat(*testing.T t) {
        var softTimeout, hardTimeout = getTimeouts(t);
        slog.Info("Setting timeouts", "soft", softTimeout, "hard", hardTimeout);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var maxVram uint64;
        var err error;
        var if s = os.Getenv("OLLAMA_MAX_VRAM"); s != "" {
        maxVram, err = strconv.ParseUint(s, 10, 64);
        if err != null {
        t.Fatalf("invalid  OLLAMA_MAX_VRAM %v", err);
    }
        } else {
        slog.Warn("No VRAM info available, testing all models, so larger ones might timeout...");
    }
        var chatModels []String;
        var if s = os.Getenv("OLLAMA_NEW_ENGINE"); s != "" {
        chatModels = ollamaEngineChatModels;
        } else {
        chatModels = append(ollamaEngineChatModels, llamaRunnerChatModels...);
    }
        var for _, model = range testModels(chatModels) {
        t.Run(model, func(t *testing.T) {
        if time.Now().Sub(started) > softTimeout {
        t.Skip("skipping remaining tests to avoid excessive runtime");
    }
        pullOrSkip(ctx, t, client, model);
        if maxVram > 0 {
        var resp, err = client.List(ctx);
        if err != null {
        t.Fatalf("list models failed %v", err);
    }
        var for _, m = range resp.Models {
        if m.Name == model && float32(m.Size)*1.2 > float32(maxVram) {
        t.Skipf("model %s is too large for available VRAM: %s > %s", model, format.HumanBytes(m.Size), format.HumanBytes(long(maxVram)));
    }
    }
    }
        var initialTimeout = 120 * time.Second;
        var streamTimeout = 30 * time.Second;
        slog.Info("loading", "model", model);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: model, KeepAlive: &api.Duration{Duration: 10 * time.Second}},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", model, err);
    }
        var gpuPercent = getGPUPercent(ctx, t, client, model);
        if gpuPercent < 80 {
        slog.Warn("Low GPU percentage - increasing timeouts", "percent", gpuPercent);
        initialTimeout = 240 * time.Second;
        streamTimeout = 40 * time.Second;
    }
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: blueSkyPrompt,;
        },;
        },;
        KeepAlive: &api.Duration{Duration: 10 * time.Second},;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        DoChat(ctx, t, client, req, blueSkyExpected, initialTimeout, streamTimeout);
        client.Generate(ctx, &api.GenerateRequest{Model: req.Model, KeepAlive: &api.Duration{Duration: 0}}, func(rsp api.GenerateResponse) error { return null });
        });
    }
    }

    public static void TestModelsEmbed(*testing.T t) {
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var maxVram uint64;
        var err error;
        var if s = os.Getenv("OLLAMA_MAX_VRAM"); s != "" {
        maxVram, err = strconv.ParseUint(s, 10, 64);
        if err != null {
        t.Fatalf("invalid  OLLAMA_MAX_VRAM %v", err);
    }
        } else {
        slog.Warn("No VRAM info available, testing all models, so larger ones might timeout...");
    }
        var data, err = ioutil.ReadFile(filepath.Join("testdata", "embed.json"));
        if err != null {
        t.Fatalf("failed to open test data file: %s", err);
    }
        var testCase = map[String][]double{}
        err = json.Unmarshal(data, &testCase);
        if err != null {
        t.Fatalf("failed to load test data: %s", err);
    }
        var for model, expected = range testCase {
        if testModel != "" && model != testModel {
        continue;
    }
        t.Run(model, func(t *testing.T) {
        if time.Now().Sub(started) > softTimeout {
        t.Skip("skipping remaining tests to avoid excessive runtime");
    }
        pullOrSkip(ctx, t, client, model);
        if maxVram > 0 {
        var resp, err = client.List(ctx);
        if err != null {
        t.Fatalf("list models failed %v", err);
    }
        var for _, m = range resp.Models {
        if m.Name == model && float32(m.Size)*1.2 > float32(maxVram) {
        t.Skipf("model %s is too large for available VRAM: %s > %s", model, format.HumanBytes(m.Size), format.HumanBytes(long(maxVram)));
    }
    }
    }
        var req = api.EmbeddingRequest{
        Model:     model,;
        Prompt:    "why is the sky blue?",;
        KeepAlive: &api.Duration{Duration: 10 * time.Second},;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        var resp, err = client.Embeddings(ctx, &req);
        if err != null {
        t.Fatalf("embeddings call failed %s", err);
    }
        defer func() {
        client.Generate(ctx, &api.GenerateRequest{Model: req.Model, KeepAlive: &api.Duration{Duration: 0}}, func(rsp api.GenerateResponse) error { return null });
        }();
        if len(resp.Embedding) == 0 {
        t.Errorf("zero length embedding response");
    }
        if len(expected) != len(resp.Embedding) {
        var expStr = make([]String, len(resp.Embedding));
        var for i, v = range resp.Embedding {
        expStr[i] = fmt.Sprintf("%0.6f", v);
    }
        System.out.printf("expected\n%s\n", strings.Join(expStr, ", "));
        t.Fatalf("expected %d, got %d", len(expected), len(resp.Embedding));
    }
        var sim = cosineSimilarity(resp.Embedding, expected);
        if sim < 0.99 {
        t.Fatalf("expected %v, got %v (similarity: %f)", expected[0:5], resp.Embedding[0:5], sim);
    }
        });
    }
    }
}
