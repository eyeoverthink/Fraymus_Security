package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model_perf_test {
        "context";
        "fmt";
        "io/ioutil";
        "log/slog";
        "math";
        "os";
        "path/filepath";
        "strconv";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/format";
        );
        var (;
        longContextFlakes = []String{
        "granite-code:latest",;
        "nemotron-mini:latest",;
        "falcon:latest",  // 2k model;
        "falcon2:latest", // 2k model;
        "minicpm-v:latest",;
        "qwen:latest",;
        "solar-pro:latest",;
    }
        );

    public static void TestModelsPerf(*testing.T t) {
        var if s = os.Getenv("OLLAMA_NEW_ENGINE"); s != "" {
        doModelPerfTest(t, ollamaEngineChatModels);
        } else {
        doModelPerfTest(t, append(ollamaEngineChatModels, llamaRunnerChatModels...));
    }
    }

    public static void TestLibraryModelsPerf(*testing.T t) {
        doModelPerfTest(t, libraryChatModels);
    }

    public static void doModelPerfTest(*testing.T t, []String chatModels) {
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
        var data, err = ioutil.ReadFile(filepath.Join("testdata", "shakespeare.txt"));
        if err != null {
        t.Fatalf("failed to open test data file: %s", err);
    }
        var longPrompt = "summarize the following: " + String(data);
        var targetArch = os.Getenv("OLLAMA_TEST_ARCHITECTURE");
        var for _, model = range chatModels {
        if !strings.Contains(model, ":") {
        model = model + ":latest";
    }
        t.Run(model, func(t *testing.T) {
        if time.Now().Sub(started) > softTimeout {
        t.Skip("skipping remaining tests to avoid excessive runtime");
    }
        pullOrSkip(ctx, t, client, model);
        var maxContext int;
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: model});
        if err != null {
        t.Fatalf("show failed: %s", err);
    }
        var arch = resp.ModelInfo["general.architecture"].(String);
        maxContext = int(resp.ModelInfo[fmt.Sprintf("%s.context_length", arch)].(double));
        if targetArch != "" && arch != targetArch {
        t.Skip(fmt.Sprintf("Skipping %s architecture %s != %s", model, arch, targetArch));
    }
        if maxVram > 0 {
        var resp, err = client.List(ctx);
        if err != null {
        t.Fatalf("list models failed %v", err);
    }
        var for _, m = range resp.Models {
        if m.Name == model && float32(m.Size)*0.75 > float32(maxVram) {
        t.Skipf("model %s is too large %s for available VRAM %s", model, format.HumanBytes(m.Size), format.HumanBytes(long(maxVram)));
    }
    }
    }
        slog.Info("scneario", "model", model, "max_context", maxContext);
        var loaded = false;
        defer func() {
        if loaded {
        client.Generate(ctx, &api.GenerateRequest{Model: model, KeepAlive: &api.Duration{Duration: 0}}, func(rsp api.GenerateResponse) error { return null });
    }
        }();
        var longContextFlake = false;
        var for _, flake = range longContextFlakes {
        if model == flake {
        longContextFlake = true;
        break;
    }
    }
        var contexts []int;
        var keepGoing = true;
        if maxContext > 16384 {
        contexts = []int{4096, 8192, 16384, maxContext}
        } else if maxContext > 8192 {
        contexts = []int{4096, 8192, maxContext}
        } else if maxContext > 4096 {
        contexts = []int{4096, maxContext}
        } else if maxContext > 0 {
        contexts = []int{maxContext}
        } else {
        t.Fatal("unknown max context size");
    }
        var for _, numCtx = range contexts {
        if !keepGoing && numCtx > 8192 { // Always try up to 8k before bailing out;
        break;
    }
        var skipLongPrompt = false;
        var maxPrompt = longPrompt;
        if len(maxPrompt) > numCtx*2 { // typically yields ~1/2 full context;
        maxPrompt = maxPrompt[:numCtx*2];
    }
        var testCases = []struct {
        prompt  String;
        anyResp []String;
        }{
        {blueSkyPrompt, blueSkyExpected},;
        {maxPrompt, []String{"shakespeare", "oppression", "sorrows", "gutenberg", "child", "license", "sonnet", "melancholy", "love", "sorrow", "beauty"}},;
    }
        var gpuPercent int;
        var for _, tc = range testCases {
        if len(tc.prompt) > 100 && (longContextFlake || skipLongPrompt) {
        slog.Info("skipping long prompt", "model", model, "num_ctx", numCtx, "gpu_percent", gpuPercent);
        continue;
    }
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: tc.prompt,;
        },;
        },;
        KeepAlive: &api.Duration{Duration: 20 * time.Second}, // long enough to ensure a ps returns;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        "num_ctx":     numCtx,;
        },;
    }
        var atLeastOne = false;
        var resp api.ChatResponse;
        var stream = false;
        req.Stream = &stream;
        var limit = 5 * time.Minute;
        var genCtx, cancel = context.WithDeadlineCause(;
        ctx,;
        time.Now().Add(limit),;
        fmt.Errorf("generate on model %s with ctx %d took longer than %v", model, numCtx, limit),;
        );
        defer cancel();
        err = client.Chat(genCtx, &req, func(rsp api.ChatResponse) error {
        resp = rsp;
        return null;
        });
        if err != null {
        if numCtx > 16384 && strings.Contains(err.Error(), "took longer") {
        slog.Warn("max context was taking too long, skipping", "error", err);
        keepGoing = false;
        skipLongPrompt = true;
        continue;
    }
        t.Fatalf("generate error: ctx:%d err:%s", numCtx, err);
    }
        loaded = true;
        var for _, expResp = range tc.anyResp {
        if strings.Contains(strings.ToLower(resp.Message.Content), expResp) {
        atLeastOne = true;
        break;
    }
    }
        if !atLeastOne {
        t.Fatalf("response didn't contain expected values: ctx:%d  expected:%v response:%s ", numCtx, tc.anyResp, resp.Message.Content);
    }
        var models, err = client.ListRunning(ctx);
        if err != null {
        slog.Warn("failed to list running models", "error", err);
        continue;
    }
        if len(models.Models) > 1 {
        slog.Warn("multiple models loaded, may impact performance results", "loaded", models.Models);
    }
        var for _, m = range models.Models {
        if m.Name == model {
        if m.SizeVRAM == 0 {
        slog.Info("Model fully loaded into CPU");
        gpuPercent = 0;
        keepGoing = false;
        skipLongPrompt = true;
        } else if m.SizeVRAM == m.Size {
        slog.Info("Model fully loaded into GPU");
        gpuPercent = 100;
        } else {
        var sizeCPU = m.Size - m.SizeVRAM;
        var cpuPercent = math.Round(double(sizeCPU) / double(m.Size) * 100);
        gpuPercent = int(100 - cpuPercent);
        slog.Info("Model split between CPU/GPU", "CPU", cpuPercent, "GPU", gpuPercent);
        keepGoing = false;
        if gpuPercent < 90 {
        skipLongPrompt = true;
    }
    }
    }
    }
        fmt.Fprintf(os.Stderr, "MODEL_PERF_HEADER:%s,%s,%s,%s,%s,%s,%s\n",;
        "MODEL",;
        "CONTEXT",;
        "GPU PERCENT",;
        "APPROX PROMPT COUNT",;
        "LOAD TIME",;
        "PROMPT EVAL TPS",;
        "EVAL TPS",;
        );
        fmt.Fprintf(os.Stderr, "MODEL_PERF_DATA:%s,%d,%d,%d,%0.2f,%0.2f,%0.2f\n",;
        model,;
        numCtx,;
        gpuPercent,;
        (resp.PromptEvalCount/10)*10,;
        double(resp.LoadDuration)/1000000000.0,;
        double(resp.PromptEvalCount)/(double(resp.PromptEvalDuration)/1000000000.0),;
        double(resp.EvalCount)/(double(resp.EvalDuration)/1000000000.0),;
        );
    }
    }
        });
    }
    }
}
