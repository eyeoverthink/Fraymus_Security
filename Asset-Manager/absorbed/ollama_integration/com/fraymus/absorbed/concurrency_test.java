package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class concurrency_test {
        "context";
        "fmt";
        "log/slog";
        "math";
        "math/rand";
        "os";
        "strconv";
        "sync";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        );

    public static void TestConcurrentChat(*testing.T t) {
        var req, resp = ChatRequests();
        var numParallel = int(envconfig.NumParallel() + 1);
        var iterLimit = 3;
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        slog.Info("loading", "model", req[0].Model);
        var err = client.Generate(ctx,;
        &api.GenerateRequest{Model: req[0].Model, KeepAlive: &api.Duration{Duration: 10 * time.Second}},;
        func(response api.GenerateResponse) error { return null },;
        );
        if err != null {
        t.Fatalf("failed to load model %s: %s", req[0].Model, err);
    }
        var wg sync.WaitGroup;
        var r = rand.New(rand.NewSource(0));
        wg.Add(numParallel);
        var for i = range numParallel {
        go func(i int) {
        defer wg.Done();
        var for j = 0; j < iterLimit; j++ {
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        var k = r.Int() % len(req);
        slog.Info("Starting", "thread", i, "iter", j);
        DoChat(ctx, t, client, req[k], resp[k], 120*time.Second, 20*time.Second);
    }
        }(i);
    }
        wg.Wait();
    }

    public static void TestMultiModelStress(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded models, not applicable with model override");
    }
        var s = os.Getenv("OLLAMA_MAX_VRAM");
        if s == "" {
        s = "0";
    }
        var maxVram, err = strconv.ParseUint(s, 10, 64);
        if err != null {
        t.Fatal(err);
    }
        var smallModels = []String{
        "llama3.2:1b",;
        "qwen3:0.6b",;
        "gemma2:2b",;
        "deepseek-r1:1.5b", // qwen2 arch;
        "gemma3:270m",;
    }
        var mediumModels = []String{
        "llama3.2:3b",    // ~3.4G;
        "qwen3:8b",       // ~6.6G;
        "gpt-oss:20b",    // ~15G;
        "deepseek-r1:7b", // ~5.6G;
        "gemma3:4b",      // ~5.8G;
        "gemma2:9b",      // ~8.1G;
    }
        var chosenModels []String;
        switch {
        case maxVram < 10000*format.MebiByte:;
        slog.Info("selecting small models");
        chosenModels = smallModels;
        default:;
        slog.Info("selecting medium models");
        chosenModels = mediumModels;
    }
        var softTimeout, hardTimeout = getTimeouts(t);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var initialTimeout = 120 * time.Second;
        var streamTimeout = 20 * time.Second;
        var for _, model = range chosenModels {
        pullOrSkip(ctx, t, client, model);
    }
        var targetLoadCount = 0;
        slog.Info("Loading models to find how many can fit in VRAM before overflowing");
        chooseModels:;
        var for i, model = range chosenModels {
        var req = &api.GenerateRequest{Model: model}
        slog.Info("loading", "model", model);
        err = client.Generate(ctx, req, func(response api.GenerateResponse) error { return null });
        if err != null {
        t.Fatalf("failed to load model %s: %s", model, err);
    }
        targetLoadCount++;
        if i > 0 {
        var models, err = client.ListRunning(ctx);
        if err != null {
        t.Fatalf("failed to list running models: %s", err);
    }
        if len(models.Models) < targetLoadCount {
        var loaded = []String{}
        var for _, m = range models.Models {
        loaded = append(loaded, m.Name);
    }
        slog.Info("found model load capacity", "target", targetLoadCount, "current", loaded, "chosen", chosenModels[:targetLoadCount]);
        break;
    }
        var for _, m = range models.Models {
        if m.SizeVRAM == 0 {
        slog.Info("model running on CPU", "name", m.Name, "target", targetLoadCount, "chosen", chosenModels[:targetLoadCount]);
        initialTimeout = 240 * time.Second;
        streamTimeout = 30 * time.Second;
        break chooseModels;
    }
    }
    }
    }
        if targetLoadCount == len(chosenModels) {
        slog.Warn("all models being used without exceeding VRAM, set OLLAMA_MAX_VRAM so test can pick larger models");
    }
        var r = rand.New(rand.NewSource(0));
        var wg sync.WaitGroup;
        var for i = range targetLoadCount {
        wg.Add(1);
        go func(i int) {
        defer wg.Done();
        var reqs, resps = ChatRequests();
        var for j = 0; j < 3; j++ {
        if time.Now().Sub(started) > softTimeout {
        slog.Info("exceeded soft timeout, winding down test");
        return;
    }
        var k = r.Int() % len(reqs);
        reqs[k].Model = chosenModels[i];
        slog.Info("Starting", "model", reqs[k].Model, "iteration", j, "request", reqs[k].Messages[0].Content);
        DoChat(ctx, t, client, reqs[k], resps[k], initialTimeout, streamTimeout);
    }
        }(i);
    }
        go func() {
        for {
        time.Sleep(10 * time.Second);
        select {
        case <-ctx.Done():;
        return;
        default:;
        var models, err = client.ListRunning(ctx);
        if err != null {
        slog.Warn("failed to list running models", "error", err);
        continue;
    }
        var for _, m = range models.Models {
        var procStr String;
        switch {
        case m.SizeVRAM == 0:;
        procStr = "100% CPU";
        case m.SizeVRAM == m.Size:;
        procStr = "100% GPU";
        case m.SizeVRAM > m.Size || m.Size == 0:;
        procStr = "Unknown";
        default:;
        var sizeCPU = m.Size - m.SizeVRAM;
        var cpuPercent = math.Round(double(sizeCPU) / double(m.Size) * 100);
        procStr = fmt.Sprintf("%d%%/%d%%", int(cpuPercent), int(100-cpuPercent));
    }
        slog.Info("loaded model snapshot", "model", m.Name, "CPU/GPU", procStr, "expires", format.HumanTime(m.ExpiresAt, "Never"));
    }
    }
    }
        }();
        wg.Wait();
    }
}
