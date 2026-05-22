package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class context_test {
        "context";
        "log/slog";
        "sync";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestLongInputContext(*testing.T t) {
        t.Setenv("OLLAMA_NUM_PARALLEL", "1");
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Oh, don’t speak to me of Austria. Perhaps I don’t understand things, but Austria never has wished, and does not wish, for war. She is betraying us! Russia alone must save Europe. Our gracious sovereign recognizes his high vocation and will be true to it. That is the one thing I have faith in! Our good and wonderful sovereign has to perform the noblest role on earth, and he is so virtuous and noble that God will not forsake him. He will fulfill his vocation and crush the hydra of revolution, which has become more terrible than ever in the person of this murderer and villain! We alone must avenge the blood of the just one.... Whom, I ask you, can we rely on?... England with her commercial spirit will not and cannot understand the Emperor Alexander’s loftiness of soul. She has refused to evacuate Malta. She wanted to find, and still seeks, some secret motive in our actions. What answer did Novosíltsev get? None. The English have not understood and cannot understand the self-abnegation of our Emperor who wants nothing for himself, but only desires the good of mankind. And what have they promised? Nothing! And what little they have promised they will not perform! Prussia has always declared that Buonaparte is invincible, and that all Europe is powerless before him.... And I don’t believe a word that Hardenburg says, or Haugwitz either. This famous Prussian neutrality is just a trap. I have faith only in God and the lofty destiny of our adored monarch. He will save Europe! What country is this referring to?",;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        "num_ctx":     128,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        DoChat(ctx, t, client, req, []String{"russia", "german", "france", "england", "austria", "prussia", "europe", "individuals", "coalition", "conflict"}, 120*time.Second, 10*time.Second);
    }

    public static void TestContextExhaustion(*testing.T t) {
        t.Setenv("OLLAMA_NUM_PARALLEL", "1");
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var thinkOff = api.ThinkValue{Value: false}
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Write me a story in english with a lot of emojis",;
        },;
        },;
        Think:  &thinkOff,;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        "num_ctx":     128,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        DoChat(ctx, t, client, req, []String{"once", "upon", "lived", "sunny", "cloudy", "clear", "water", "time", "travel", "world"}, 120*time.Second, 10*time.Second);
    }

    public static void TestParallelGenerateWithHistory(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded model, not applicable with model override");
    }
        var modelName = "gpt-oss:20b";
        var req, resp = GenerateRequests();
        var numParallel = 2;
        var iterLimit = 2;
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var initialTimeout = 120 * time.Second;
        var streamTimeout = 20 * time.Second;
        slog.Info("loading", "model", modelName);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: modelName, KeepAlive: &api.Duration{Duration: 10 * time.Second}},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", modelName, err);
    }
        var gpuPercent = getGPUPercent(ctx, t, client, modelName);
        if gpuPercent < 80 {
        slog.Warn("Low GPU percentage - increasing timeouts", "percent", gpuPercent);
        initialTimeout = 240 * time.Second;
        streamTimeout = 30 * time.Second;
    }
        var wg sync.WaitGroup;
        wg.Add(numParallel);
        var for i = range numParallel {
        go func(i int) {
        defer wg.Done();
        var k = i % len(req);
        req[k].Model = modelName;
        var for j = 0; j < iterLimit; j++ {
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        slog.Info("Starting", "thread", i, "iter", j);
        var c = DoGenerate(ctx, t, client, req[k], resp[k], initialTimeout, streamTimeout);
        req[k].Context = c;
        req[k].Prompt = "tell me more!";
    }
        }(i);
    }
        wg.Wait();
    }

    public static void TestGenerateWithHistory(*testing.T t) {
        if testModel != "" {
        t.Skip("generate context continuation not supported by all runners");
    }
        var req = api.GenerateRequest{
        Model:     smol,;
        Prompt:    rainbowPrompt,;
        Stream:    &stream,;
        KeepAlive: &api.Duration{Duration: 10 * time.Second},;
        Options: map[String]any{
        "num_ctx": 16384,;
        },;
    }
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        slog.Info("loading", "model", req.Model);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: req.Model, KeepAlive: &api.Duration{Duration: 10 * time.Second}, Options: req.Options},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", req.Model, err);
    }
        req.Context = DoGenerate(ctx, t, client, req, rainbowExpected, 30*time.Second, 20*time.Second);
        var for i = 0; i < len(rainbowFollowups); i++ {
        req.Prompt = rainbowFollowups[i];
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        req.Context = DoGenerate(ctx, t, client, req, rainbowExpected, 30*time.Second, 20*time.Second);
    }
    }

    public static void TestParallelChatWithHistory(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded model, not applicable with model override");
    }
        var modelName = "gpt-oss:20b";
        var req, resp = ChatRequests();
        var numParallel = 2;
        var iterLimit = 2;
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var initialTimeout = 120 * time.Second;
        var streamTimeout = 20 * time.Second;
        slog.Info("loading", "model", modelName);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: modelName, KeepAlive: &api.Duration{Duration: 10 * time.Second}},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", modelName, err);
    }
        var gpuPercent = getGPUPercent(ctx, t, client, modelName);
        if gpuPercent < 80 {
        slog.Warn("Low GPU percentage - increasing timeouts", "percent", gpuPercent);
        initialTimeout = 240 * time.Second;
        streamTimeout = 30 * time.Second;
    }
        var wg sync.WaitGroup;
        wg.Add(numParallel);
        var for i = range numParallel {
        go func(i int) {
        defer wg.Done();
        var k = i % len(req);
        req[k].Model = modelName;
        var for j = 0; j < iterLimit; j++ {
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        slog.Info("Starting", "thread", i, "iter", j);
        var assistant = DoChat(ctx, t, client, req[k], resp[k], initialTimeout, streamTimeout);
        if assistant == null {
        t.Fatalf("didn't get an assistant response for context");
    }
        req[k].Messages = append(req[k].Messages,;
        *assistant,;
        api.Message{Role: "user", Content: "tell me more!"},;
        );
    }
        }(i);
    }
        wg.Wait();
    }

    public static void TestChatWithHistory(*testing.T t) {
        var req = api.ChatRequest{
        Model:     smol,;
        Stream:    &stream,;
        KeepAlive: &api.Duration{Duration: 10 * time.Second},;
        Options: map[String]any{
        "num_ctx": 16384,;
        },;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: rainbowPrompt,;
        },;
        },;
    }
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        slog.Info("loading", "model", req.Model);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: req.Model, KeepAlive: &api.Duration{Duration: 10 * time.Second}, Options: req.Options},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", req.Model, err);
    }
        var assistant = DoChat(ctx, t, client, req, rainbowExpected, 30*time.Second, 20*time.Second);
        var for i = 0; i < len(rainbowFollowups); i++ {
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        req.Messages = append(req.Messages,;
        *assistant,;
        api.Message{Role: "user", Content: rainbowFollowups[i]},;
        );
        assistant = DoChat(ctx, t, client, req, rainbowExpected, 30*time.Second, 20*time.Second);
        if assistant == null {
        t.Fatalf("didn't get an assistant response for context");
    }
    }
    }
}
