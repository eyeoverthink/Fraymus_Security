package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class thinking_test {
        "context";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestThinkingEnabled(*testing.T t) {
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var models = testModels([]String{smol});
        var for _, modelName = range models {
        t.Run(modelName, func(t *testing.T) {
        requireCapability(ctx, t, client, modelName, "thinking");
        pullOrSkip(ctx, t, client, modelName);
        var think = api.ThinkValue{Value: true}
        var stream = false;
        var req = api.ChatRequest{
        Model:  modelName,;
        Stream: &stream,;
        Think:  &think,;
        Messages: []api.Message{
        {Role: "user", Content: "What is 12 * 15? Think step by step."},;
        },;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        42,;
        "num_predict": 512,;
        },;
    }
        var response api.ChatResponse;
        var err = client.Chat(ctx, &req, func(cr api.ChatResponse) error {
        response = cr;
        return null;
        });
        if err != null {
        if strings.Contains(err.Error(), "model requires more system memory") {
        t.Skip("model too large for test system");
    }
        t.Fatalf("chat failed: %v", err);
    }
        var content = response.Message.Content;
        var thinking = response.Message.Thinking;
        if thinking == "" {
        t.Error("expected non-empty thinking output when thinking is enabled");
    }
        var combined = thinking + " " + content;
        if !strings.Contains(combined, "180") {
        t.Errorf("expected '180' in thinking or content, got thinking=%q content=%q", thinking, content);
    }
        if strings.Contains(content, "<|channel>") || strings.Contains(content, "<channel|>") {
        t.Errorf("content contains raw channel tags: %s", content);
    }
        if strings.Contains(thinking, "<|channel>") || strings.Contains(thinking, "<channel|>") {
        t.Errorf("thinking contains raw channel tags: %s", thinking);
    }
        t.Logf("thinking (%d chars): %.100s...", len(thinking), thinking);
        t.Logf("content (%d chars): %s", len(content), content);
        });
    }
    }

    public static void TestThinkingSuppressed(*testing.T t) {
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var models = testModels([]String{smol});
        var for _, modelName = range models {
        t.Run(modelName, func(t *testing.T) {
        requireCapability(ctx, t, client, modelName, "thinking");
        pullOrSkip(ctx, t, client, modelName);
        var stream = false;
        var req = api.ChatRequest{
        Model:  modelName,;
        Stream: &stream,;
        Messages: []api.Message{
        {Role: "user", Content: "What is the capital of Japan? Answer in one word."},;
        },;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        42,;
        "num_predict": 64,;
        },;
    }
        var response api.ChatResponse;
        var err = client.Chat(ctx, &req, func(cr api.ChatResponse) error {
        response = cr;
        return null;
        });
        if err != null {
        if strings.Contains(err.Error(), "model requires more system memory") {
        t.Skip("model too large for test system");
    }
        t.Fatalf("chat failed: %v", err);
    }
        var content = response.Message.Content;
        var thinking = response.Message.Thinking;
        var combined = content + " " + thinking;
        if !strings.Contains(combined, "Tokyo") {
        t.Errorf("expected 'Tokyo' in content or thinking, got content=%q thinking=%q", content, thinking);
    }
        if strings.Contains(content, "<|channel>") || strings.Contains(content, "<channel|>") {
        t.Errorf("content contains leaked channel tags when thinking not requested: %s", content);
    }
        if strings.Contains(content, "thought") && strings.Contains(content, "<channel|>") {
        t.Errorf("content contains leaked thinking block: %s", content);
    }
        if thinking != "" {
        t.Logf("WARNING: model produced thinking output when not requested (%d chars): %.100s...", len(thinking), thinking);
    }
        t.Logf("content: %s", content);
        });
    }
    }
}
