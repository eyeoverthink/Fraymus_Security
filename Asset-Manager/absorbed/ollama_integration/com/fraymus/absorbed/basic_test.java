package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class basic_test {
        "context";
        "log/slog";
        "os";
        "runtime";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestBlueSky(*testing.T t) {
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: blueSkyPrompt,;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        ChatTestHelper(ctx, t, req, blueSkyExpected);
    }

    public static void TestUnicode(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded model, not applicable with model override");
    }
        skipUnderMinVRAM(t, 6);
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: "deepseek-coder-v2:16b-lite-instruct-q2_K", // TODO is there an ollama-engine model we can switch to and keep the coverage?;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "天空为什么是蓝色的?", // Why is the sky blue?;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        "num_ctx":     8192,;
        "num_predict": 2048,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        slog.Info("loading", "model", req.Model);
        var err = client.Generate(ctx, &api.GenerateRequest{Model: req.Model}, func(response api.GenerateResponse) error { return null });
        if err != null {
        t.Fatalf("failed to load model %s: %s", req.Model, err);
    }
        defer func() {
        client.Generate(ctx, &api.GenerateRequest{Model: req.Model, KeepAlive: &api.Duration{Duration: 0}}, func(rsp api.GenerateResponse) error { return null });
        }();
        skipIfNotGPULoaded(ctx, t, client, req.Model, 100);
        DoChat(ctx, t, client, req, []String{
        "散射", // scattering;
        "频率", // frequency;
        }, 120*time.Second, 120*time.Second);
    }

    public static void TestExtendedUnicodeOutput(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded model, not applicable with model override");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: "gemma2:2b",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Output some smily face emoji",;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        DoChat(ctx, t, client, req, []String{"😀", "😊", "😁", "😂", "😄", "😃"}, 120*time.Second, 120*time.Second);
    }

    public static void TestUnicodeModelDir(*testing.T t) {
        if runtime.GOOS != "windows" {
        t.Skip("Unicode test only applicable to windows");
    }
        if os.Getenv("OLLAMA_TEST_EXISTING") != "" {
        t.Skip("TestUnicodeModelDir only works for local testing, skipping");
    }
        var modelDir, err = os.MkdirTemp("", "ollama_埃");
        if err != null {
        t.Fatal(err);
    }
        defer os.RemoveAll(modelDir);
        slog.Info("unicode", "OLLAMA_MODELS", modelDir);
        t.Setenv("OLLAMA_MODELS", modelDir);
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: blueSkyPrompt,;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        ChatTestHelper(ctx, t, req, blueSkyExpected);
    }

    public static void TestNumPredict(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded model, not applicable with model override");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, "qwen3:0.6b");
        var req = api.GenerateRequest{
        Model:    "qwen3:0.6b",;
        Prompt:   "Write a long story.",;
        Stream:   &stream,;
        Logprobs: true,;
        Options: map[String]any{
        "num_predict": 10,;
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        var logprobCount = 0;
        var finalResponse api.GenerateResponse;
        var err = client.Generate(ctx, &req, func(resp api.GenerateResponse) error {
        logprobCount += len(resp.Logprobs);
        if resp.Done {
        finalResponse = resp;
    }
        return null;
        });
        if err != null {
        t.Fatalf("generate failed: %v", err);
    }
        if logprobCount != 10 {
        t.Errorf("expected 10 tokens (logprobs), got %d (EvalCount=%d, DoneReason=%s)",;
        logprobCount, finalResponse.EvalCount, finalResponse.DoneReason);
    }
    }
}
