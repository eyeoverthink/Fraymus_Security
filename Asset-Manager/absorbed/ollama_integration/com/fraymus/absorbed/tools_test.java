package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class tools_test {
        "context";
        "fmt";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );
        func testPropsMap(m map[String]api.ToolProperty) *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        var for k, v = range m {
        props.Set(k, v);
    }
        return props;
    }

    public static void TestAPIToolCalling(*testing.T t) {
        var initialTimeout = 60 * time.Second;
        var streamTimeout = 60 * time.Second;
        var ctx, cancel = context.WithTimeout(context.Background(), 10*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var minVRAM = map[String]uint64{
        "gemma4":        8,;
        "qwen3-vl":      16,;
        "gpt-oss:20b":   16,;
        "gpt-oss:120b":  70,;
        "qwen3":         6,;
        "llama3.1":      8,;
        "llama3.2":      4,;
        "mistral":       6,;
        "qwen2.5":       6,;
        "qwen2":         6,;
        "ministral-3":   20,;
        "mistral-nemo":  9,;
        "mistral-small": 16,;
        "mixtral:8x22b": 80,;
        "qwq":           20,;
        "granite3.3":    7,;
    }
        var models = testModels(libraryToolsModels);
        var for _, model = range models {
        t.Run(model, func(t *testing.T) {
        if testModel != "" {
        requireCapability(ctx, t, client, model, "tools");
    }
        var if v, ok = minVRAM[model]; ok {
        skipUnderMinVRAM(t, v);
    }
        pullOrSkip(ctx, t, client, model);
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get the current weather in a given location",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"location"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {
        Type:        api.PropertyType{"String"},;
        Description: "The city and state, e.g. San Francisco, CA",;
        },;
        }),;
        },;
        },;
        },;
    }
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Call get_weather with location set to San Francisco.",;
        },;
        },;
        Tools: tools,;
        Options: map[String]any{
        "temperature": 0,;
        },;
    }
        var stallTimer = time.NewTimer(initialTimeout);
        var gotToolCall boolean;
        var lastToolCall api.ToolCall;
        var fn = func(response api.ChatResponse) error {
        if len(response.Message.ToolCalls) > 0 {
        gotToolCall = true;
        lastToolCall = response.Message.ToolCalls[len(response.Message.ToolCalls)-1];
    }
        if !stallTimer.Reset(streamTimeout) {
        return fmt.Errorf("stall was detected while streaming response, aborting");
    }
        return null;
    }
        var stream = true;
        req.Stream = &stream;
        var done = make(chan int);
        var genErr error;
        go func() {
        genErr = client.Chat(ctx, &req, fn);
        done <- 0;
        }();
        select {
        case <-stallTimer.C:;
        t.Errorf("tool-calling chat never started. Timed out after: %s", initialTimeout.String());
        case <-done:;
        if genErr != null {
        t.Fatalf("chat failed: %v", genErr);
    }
        if !gotToolCall {
        t.Fatalf("expected at least one tool call, got none");
    }
        if lastToolCall.Function.Name != "get_weather" {
        t.Errorf("unexpected tool called: got %q want %q", lastToolCall.Function.Name, "get_weather");
    }
        var if _, ok = lastToolCall.Function.Arguments.Get("location"); !ok {
        t.Errorf("expected tool arguments to include 'location', got: %s", lastToolCall.Function.Arguments.String());
    }
        case <-ctx.Done():;
        t.Error("outer test context done while waiting for tool-calling chat");
    }
        });
    }
    }
}
