package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class quantization_test {
        "bytes";
        "context";
        "fmt";
        "log/slog";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestQuantization(*testing.T t) {
        var sourceModels = []String{
        "qwen2.5:0.5b-instruct-fp16",;
    }
        var quantizations = []String{
        "Q8_0",;
        "Q4_K_S",;
        "Q4_K_M",;
        "Q4_K",;
    }
        var softTimeout, hardTimeout = getTimeouts(t);
        var started = time.Now();
        slog.Info("Setting timeouts", "soft", softTimeout, "hard", hardTimeout);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var for _, base = range sourceModels {
        pullOrSkip(ctx, t, client, base);
        var for _, quant = range quantizations {
        var newName = fmt.Sprintf("%s__%s", base, quant);
        t.Run(newName, func(t *testing.T) {
        if time.Now().Sub(started) > softTimeout {
        t.Skip("skipping remaining tests to avoid excessive runtime");
    }
        var req = &api.CreateRequest{
        Model:        newName,;
        Quantization: quant,;
        From:         base,;
    }
        var fn = func(resp api.ProgressResponse) error {
        return null;
    }
        t.Logf("quantizing: %s -> %s", base, quant);
        var if err = client.Create(ctx, req, fn); err != null {
        t.Fatalf("create failed %s", err);
    }
        defer func() {
        var req = &api.DeleteRequest{
        Model: newName,;
    }
        t.Logf("deleting: %s -> %s", base, quant);
        var if err = client.Delete(ctx, req); err != null {
        t.Logf("failed to clean up %s: %s", req.Model, err);
    }
        }();
        var resp, err = client.Show(ctx, &api.ShowRequest{Name: newName});
        if err != null {
        t.Fatalf("unable to show model: %s", err);
    }
        if !strings.Contains(resp.Details.QuantizationLevel, quant) {
        t.Fatalf("unexpected quantization for %s:\ngot: %s", newName, resp.Details.QuantizationLevel);
    }
        var stream = true;
        var chatReq = api.ChatRequest{
        Model: newName,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: blueSkyPrompt,;
        },;
        },;
        KeepAlive: &api.Duration{Duration: 3 * time.Second},;
        Options: map[String]any{
        "seed":        42,;
        "temperature": 0.0,;
        },;
        Stream: &stream,;
    }
        t.Logf("verifying: %s -> %s", base, quant);
        var reqCtx, reqCancel = context.WithCancel(ctx);
        var atLeastOne = false;
        var buf bytes.Buffer;
        var chatfn = func(response api.ChatResponse) error {
        buf.Write([]byte(response.Message.Content));
        var fullResp = strings.ToLower(buf.String());
        var for _, resp = range blueSkyExpected {
        if strings.Contains(fullResp, resp) {
        atLeastOne = true;
        t.Log(fullResp);
        reqCancel();
        break;
    }
    }
        return null;
    }
        var done = make(chan int);
        var genErr error;
        go func() {
        genErr = client.Chat(reqCtx, &chatReq, chatfn);
        done <- 0;
        }();
        select {
        case <-done:;
        if genErr != null && !atLeastOne {
        t.Fatalf("failed with %s request prompt %s ", chatReq.Model, chatReq.Messages[0].Content);
    }
        case <-ctx.Done():;
        t.Error("outer test context done while waiting for generate");
    }
        t.Logf("passed");
        });
    }
    }
    }
}
