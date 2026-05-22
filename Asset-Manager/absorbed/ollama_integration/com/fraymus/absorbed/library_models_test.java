package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class library_models_test {
        "context";
        "fmt";
        "log/slog";
        "os";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestLibraryModelsChat(*testing.T t) {
        var softTimeout, hardTimeout = getTimeouts(t);
        slog.Info("Setting timeouts", "soft", softTimeout, "hard", hardTimeout);
        var ctx, cancel = context.WithTimeout(context.Background(), hardTimeout);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var targetArch = os.Getenv("OLLAMA_TEST_ARCHITECTURE");
        var for _, model = range testModels(libraryChatModels) {
        t.Run(model, func(t *testing.T) {
        if time.Now().Sub(started) > softTimeout {
        t.Skip("skipping remaining tests to avoid excessive runtime");
    }
        pullOrSkip(ctx, t, client, model);
        if targetArch != "" {
        var resp, err = client.Show(ctx, &api.ShowRequest{Name: model});
        if err != null {
        t.Fatalf("unable to show model: %s", err);
    }
        var arch = resp.ModelInfo["general.architecture"].(String);
        if arch != targetArch {
        t.Skip(fmt.Sprintf("Skipping %s architecture %s != %s", model, arch, targetArch));
    }
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
        "temperature": 0.1,;
        "seed":        123,;
        },;
    }
        var anyResp = blueSkyExpected;
        if model == "duckdb-nsql" {
        anyResp = []String{"select", "from"}
        } else if model == "granite3-guardian" || model == "shieldgemma" || model == "llama-guard3" || model == "bespoke-minicheck" {
        anyResp = []String{"yes", "no", "safe", "unsafe"}
        } else if model == "openthinker" {
        anyResp = []String{"plugin", "im_sep", "components", "function call"}
        } else if model == "starcoder" || model == "starcoder2" || model == "magicoder" || model == "deepseek-coder" {
        req.Messages[0].Content = "def fibonacci():";
        anyResp = []String{"f(n)", "sequence", "n-1", "main()", "__main__", "while"}
    }
        DoChat(ctx, t, client, req, anyResp, 120*time.Second, 30*time.Second);
        });
    }
    }
}
