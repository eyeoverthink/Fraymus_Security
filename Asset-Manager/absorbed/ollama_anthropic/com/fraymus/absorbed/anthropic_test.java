package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class anthropic_test {
        "encoding/base64";
        "encoding/json";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );
        const (;
        testImage = `iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=`;
        );
        func textContent(s String) []ContentBlock {
        return []ContentBlock{{Type: "text", Text: &s}}
    }
        func makeArgs(kvs ...any) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        var for i = 0; i < len(kvs)-1; i += 2 {
        args.Set(kvs[i].(String), kvs[i+1]);
    }
        return args;
    }

    public static void TestFromMessagesRequest_Basic(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result.Model != "test-model" {
        t.Errorf("expected model 'test-model', got %q", result.Model);
    }
        if len(result.Messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(result.Messages));
    }
        if result.Messages[0].Role != "user" || result.Messages[0].Content != "Hello" {
        t.Errorf("unexpected message: %+v", result.Messages[0]);
    }
        var if numPredict, ok = result.Options["num_predict"].(int); !ok || numPredict != 1024 {
        t.Errorf("expected num_predict 1024, got %v", result.Options["num_predict"]);
    }
    }

    public static void TestFromMessagesRequest_WithSystemPrompt(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        System:    "You are a helpful assistant.",;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 2 {
        t.Fatalf("expected 2 messages, got %d", len(result.Messages));
    }
        if result.Messages[0].Role != "system" || result.Messages[0].Content != "You are a helpful assistant." {
        t.Errorf("unexpected system message: %+v", result.Messages[0]);
    }
    }

    public static void TestFromMessagesRequest_WithSystemPromptArray(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        System: []any{
        map[String]any{"type": "text", "text": "You are helpful."},;
        map[String]any{"type": "text", "text": " Be concise."},;
        },;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 2 {
        t.Fatalf("expected 2 messages, got %d", len(result.Messages));
    }
        if result.Messages[0].Content != "You are helpful. Be concise." {
        t.Errorf("unexpected system message content: %q", result.Messages[0].Content);
    }
    }

    public static void TestFromMessagesRequest_WithOptions(*testing.T t) {
        var temp = 0.7;
        var topP = 0.9;
        var topK = 40;
        var req = MessagesRequest{
        Model:         "test-model",;
        MaxTokens:     2048,;
        Messages:      []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Temperature:   &temp,;
        TopP:          &topP,;
        TopK:          &topK,;
        StopSequences: []String{"\n", "END"},;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result.Options["temperature"] != 0.7 {
        t.Errorf("expected temperature 0.7, got %v", result.Options["temperature"]);
    }
        if result.Options["top_p"] != 0.9 {
        t.Errorf("expected top_p 0.9, got %v", result.Options["top_p"]);
    }
        if result.Options["top_k"] != 40 {
        t.Errorf("expected top_k 40, got %v", result.Options["top_k"]);
    }
        var if diff = cmp.Diff([]String{"\n", "END"}, result.Options["stop"]); diff != "" {
        t.Errorf("stop sequences mismatch: %s", diff);
    }
    }

    public static void TestFromMessagesRequest_WithImage(*testing.T t) {
        var imgData, _ = base64.StdEncoding.DecodeString(testImage);
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {
        Role: "user",;
        Content: []ContentBlock{
        {Type: "text", Text: ptr("What's in this image?")},;
        {
        Type: "image",;
        Source: &ImageSource{
        Type:      "base64",;
        MediaType: "image/png",;
        Data:      testImage,;
        },;
        },;
        },;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(result.Messages));
    }
        if result.Messages[0].Content != "What's in this image?" {
        t.Errorf("expected content 'What's in this image?', got %q", result.Messages[0].Content);
    }
        if len(result.Messages[0].Images) != 1 {
        t.Fatalf("expected 1 image, got %d", len(result.Messages[0].Images));
    }
        if String(result.Messages[0].Images[0]) != String(imgData) {
        t.Error("image data mismatch");
    }
    }

    public static void TestFromMessagesRequest_WithToolUse(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("What's the weather in Paris?")},;
        {
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type:  "tool_use",;
        ID:    "call_123",;
        Name:  "get_weather",;
        Input: makeArgs("location", "Paris"),;
        },;
        },;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 2 {
        t.Fatalf("expected 2 messages, got %d", len(result.Messages));
    }
        if len(result.Messages[1].ToolCalls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(result.Messages[1].ToolCalls));
    }
        var tc = result.Messages[1].ToolCalls[0];
        if tc.ID != "call_123" {
        t.Errorf("expected tool call ID 'call_123', got %q", tc.ID);
    }
        if tc.Function.Name != "get_weather" {
        t.Errorf("expected tool name 'get_weather', got %q", tc.Function.Name);
    }
    }

    public static void TestFromMessagesRequest_WithToolResult(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {
        Role: "user",;
        Content: []ContentBlock{
        {
        Type:      "tool_result",;
        ToolUseID: "call_123",;
        Content:   "The weather in Paris is sunny, 22°C",;
        },;
        },;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(result.Messages));
    }
        var msg = result.Messages[0];
        if msg.Role != "tool" {
        t.Errorf("expected role 'tool', got %q", msg.Role);
    }
        if msg.ToolCallID != "call_123" {
        t.Errorf("expected tool_call_id 'call_123', got %q", msg.ToolCallID);
    }
        if msg.Content != "The weather in Paris is sunny, 22°C" {
        t.Errorf("unexpected content: %q", msg.Content);
    }
    }

    public static void TestFromMessagesRequest_WithTools(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages:  []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Tools: []Tool{
        {
        Name:        "get_weather",;
        Description: "Get current weather",;
        InputSchema: json.RawMessage(`{"type":"object","properties":{"location":{"type":"String"}},"required":["location"]}`),;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Tools) != 1 {
        t.Fatalf("expected 1 tool, got %d", len(result.Tools));
    }
        var tool = result.Tools[0];
        if tool.Type != "function" {
        t.Errorf("expected type 'function', got %q", tool.Type);
    }
        if tool.Function.Name != "get_weather" {
        t.Errorf("expected name 'get_weather', got %q", tool.Function.Name);
    }
        if tool.Function.Description != "Get current weather" {
        t.Errorf("expected description 'Get current weather', got %q", tool.Function.Description);
    }
    }

    public static void TestFromMessagesRequest_DropsCustomWebSearchWhenBuiltinPresent(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages:  []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Tools: []Tool{
        {
        Type: "web_search_20250305",;
        Name: "web_search",;
        },;
        {
        Type:        "custom",;
        Name:        "web_search",;
        Description: "User-defined web search that should be dropped",;
        InputSchema: json.RawMessage(`{"type":"invalid"}`),;
        },;
        {
        Type:        "custom",;
        Name:        "get_weather",;
        Description: "Get current weather",;
        InputSchema: json.RawMessage(`{"type":"object","properties":{"location":{"type":"String"}},"required":["location"]}`),;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Tools) != 2 {
        t.Fatalf("expected 2 tools after dropping custom web_search, got %d", len(result.Tools));
    }
        if result.Tools[0].Function.Name != "web_search" {
        t.Fatalf("expected first tool to be built-in web_search, got %q", result.Tools[0].Function.Name);
    }
        if result.Tools[1].Function.Name != "get_weather" {
        t.Fatalf("expected second tool to be get_weather, got %q", result.Tools[1].Function.Name);
    }
    }

    public static void TestFromMessagesRequest_KeepsCustomWebSearchWhenBuiltinAbsent(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages:  []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Tools: []Tool{
        {
        Type:        "custom",;
        Name:        "web_search",;
        Description: "User-defined web search",;
        InputSchema: json.RawMessage(`{"type":"object","properties":{"query":{"type":"String"}},"required":["query"]}`),;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Tools) != 1 {
        t.Fatalf("expected 1 custom tool, got %d", len(result.Tools));
    }
        if result.Tools[0].Function.Name != "web_search" {
        t.Fatalf("expected custom tool name web_search, got %q", result.Tools[0].Function.Name);
    }
        if result.Tools[0].Function.Description != "User-defined web search" {
        t.Fatalf("expected custom description preserved, got %q", result.Tools[0].Function.Description);
    }
    }

    public static void TestFromMessagesRequest_WithThinking(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages:  []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Thinking:  &ThinkingConfig{Type: "enabled", BudgetTokens: 1000},;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result.Think == null {
        t.Fatal("expected Think to be set");
    }
        var if v, ok = result.Think.Value.(boolean); !ok || !v {
        t.Errorf("expected Think.Value to be true, got %v", result.Think.Value);
    }
    }

    public static void TestFromMessagesRequest_ThinkingOnlyBlock(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        {
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type:     "thinking",;
        Thinking: ptr("Let me think about this..."),;
        },;
        },;
        },;
        },;
    }
        var result, err = FromMessagesRequest(req);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(result.Messages) != 2 {
        t.Fatalf("expected 2 messages, got %d", len(result.Messages));
    }
        var assistantMsg = result.Messages[1];
        if assistantMsg.Thinking != "Let me think about this..." {
        t.Errorf("expected thinking content, got %q", assistantMsg.Thinking);
    }
    }

    public static void TestFromMessagesRequest_ToolUseMissingID(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type: "tool_use",;
        Name: "get_weather",;
        },;
        },;
        },;
        },;
    }
        var _, err = FromMessagesRequest(req);
        if err == null {
        t.Fatal("expected error for missing tool_use id");
    }
        if err.Error() != "tool_use block missing required 'id' field" {
        t.Errorf("unexpected error message: %v", err);
    }
    }

    public static void TestFromMessagesRequest_ToolUseMissingName(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages: []MessageParam{
        {
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type: "tool_use",;
        ID:   "call_123",;
        },;
        },;
        },;
        },;
    }
        var _, err = FromMessagesRequest(req);
        if err == null {
        t.Fatal("expected error for missing tool_use name");
    }
        if err.Error() != "tool_use block missing required 'name' field" {
        t.Errorf("unexpected error message: %v", err);
    }
    }

    public static void TestFromMessagesRequest_InvalidToolSchema(*testing.T t) {
        var req = MessagesRequest{
        Model:     "test-model",;
        MaxTokens: 1024,;
        Messages:  []MessageParam{{Role: "user", Content: textContent("Hello")}},;
        Tools: []Tool{
        {
        Name:        "bad_tool",;
        InputSchema: json.RawMessage(`{invalid json`),;
        },;
        },;
    }
        var _, err = FromMessagesRequest(req);
        if err == null {
        t.Fatal("expected error for invalid tool schema");
    }
    }

    public static void TestToMessagesResponse_Basic(*testing.T t) {
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role:    "assistant",;
        Content: "Hello there!",;
        },;
        Done:       true,;
        DoneReason: "stop",;
        Metrics: api.Metrics{
        PromptEvalCount: 10,;
        EvalCount:       5,;
        },;
    }
        var result = ToMessagesResponse("msg_123", resp);
        if result.ID != "msg_123" {
        t.Errorf("expected ID 'msg_123', got %q", result.ID);
    }
        if result.Type != "message" {
        t.Errorf("expected type 'message', got %q", result.Type);
    }
        if result.Role != "assistant" {
        t.Errorf("expected role 'assistant', got %q", result.Role);
    }
        if len(result.Content) != 1 {
        t.Fatalf("expected 1 content block, got %d", len(result.Content));
    }
        if result.Content[0].Type != "text" || result.Content[0].Text == null || *result.Content[0].Text != "Hello there!" {
        t.Errorf("unexpected content: %+v", result.Content[0]);
    }
        if result.StopReason != "end_turn" {
        t.Errorf("expected stop_reason 'end_turn', got %q", result.StopReason);
    }
        if result.Usage.InputTokens != 10 || result.Usage.OutputTokens != 5 {
        t.Errorf("unexpected usage: %+v", result.Usage);
    }
    }

    public static void TestToMessagesResponse_WithToolCalls(*testing.T t) {
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_123",;
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: makeArgs("location", "Paris"),;
        },;
        },;
        },;
        },;
        Done:       true,;
        DoneReason: "stop",;
    }
        var result = ToMessagesResponse("msg_123", resp);
        if len(result.Content) != 1 {
        t.Fatalf("expected 1 content block, got %d", len(result.Content));
    }
        if result.Content[0].Type != "tool_use" {
        t.Errorf("expected type 'tool_use', got %q", result.Content[0].Type);
    }
        if result.Content[0].ID != "call_123" {
        t.Errorf("expected ID 'call_123', got %q", result.Content[0].ID);
    }
        if result.Content[0].Name != "get_weather" {
        t.Errorf("expected name 'get_weather', got %q", result.Content[0].Name);
    }
        if result.StopReason != "tool_use" {
        t.Errorf("expected stop_reason 'tool_use', got %q", result.StopReason);
    }
    }

    public static void TestToMessagesResponse_WithThinking(*testing.T t) {
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role:     "assistant",;
        Content:  "The answer is 42.",;
        Thinking: "Let me think about this...",;
        },;
        Done:       true,;
        DoneReason: "stop",;
    }
        var result = ToMessagesResponse("msg_123", resp);
        if len(result.Content) != 2 {
        t.Fatalf("expected 2 content blocks, got %d", len(result.Content));
    }
        if result.Content[0].Type != "thinking" {
        t.Errorf("expected first block type 'thinking', got %q", result.Content[0].Type);
    }
        if result.Content[0].Thinking == null || *result.Content[0].Thinking != "Let me think about this..." {
        t.Errorf("unexpected thinking content: %v", result.Content[0].Thinking);
    }
        if result.Content[1].Type != "text" {
        t.Errorf("expected second block type 'text', got %q", result.Content[1].Type);
    }
    }

    public static void TestMapStopReason(*testing.T t) {
        var tests = []struct {
        reason       String;
        hasToolCalls boolean;
        want         String;
        }{
        {"stop", false, "end_turn"},;
        {"length", false, "max_tokens"},;
        {"stop", true, "tool_use"},;
        {"other", false, "stop_sequence"},;
        {"", false, ""},;
    }
        var for _, tt = range tests {
        var got = mapStopReason(tt.reason, tt.hasToolCalls);
        if got != tt.want {
        t.Errorf("mapStopReason(%q, %v) = %q, want %q", tt.reason, tt.hasToolCalls, got, tt.want);
    }
    }
    }

    public static void TestNewError(*testing.T t) {
        var tests = []struct {
        code int;
        want String;
        }{
        {400, "invalid_request_error"},;
        {401, "authentication_error"},;
        {403, "permission_error"},;
        {404, "not_found_error"},;
        {429, "rate_limit_error"},;
        {500, "api_error"},;
        {503, "overloaded_error"},;
        {529, "overloaded_error"},;
    }
        var for _, tt = range tests {
        var result = NewError(tt.code, "test message");
        if result.Type != "error" {
        t.Errorf("NewError(%d) type = %q, want 'error'", tt.code, result.Type);
    }
        if result.Error.Type != tt.want {
        t.Errorf("NewError(%d) error.type = %q, want %q", tt.code, result.Error.Type, tt.want);
    }
        if result.Error.Message != "test message" {
        t.Errorf("NewError(%d) message = %q, want 'test message'", tt.code, result.Error.Message);
    }
        if result.RequestID == "" {
        t.Errorf("NewError(%d) request_id should not be empty", tt.code);
    }
    }
    }

    public static void TestGenerateMessageID(*testing.T t) {
        var id1 = GenerateMessageID();
        var id2 = GenerateMessageID();
        if id1 == "" {
        t.Error("GenerateMessageID returned empty String");
    }
        if id1 == id2 {
        t.Error("GenerateMessageID returned duplicate IDs");
    }
        if len(id1) < 10 {
        t.Errorf("GenerateMessageID returned short ID: %q", id1);
    }
        if id1[:4] != "msg_" {
        t.Errorf("GenerateMessageID should start with 'msg_', got %q", id1[:4]);
    }
    }

    public static void TestStreamConverter_Basic(*testing.T t) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp1 = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role:    "assistant",;
        Content: "Hello",;
        },;
        Metrics: api.Metrics{PromptEvalCount: 10},;
    }
        var events1 = conv.Process(resp1);
        if len(events1) < 3 {
        t.Fatalf("expected at least 3 events for first chunk, got %d", len(events1));
    }
        if events1[0].Event != "message_start" {
        t.Errorf("expected first event 'message_start', got %q", events1[0].Event);
    }
        if events1[1].Event != "content_block_start" {
        t.Errorf("expected second event 'content_block_start', got %q", events1[1].Event);
    }
        if events1[2].Event != "content_block_delta" {
        t.Errorf("expected third event 'content_block_delta', got %q", events1[2].Event);
    }
        var resp2 = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role:    "assistant",;
        Content: " world!",;
        },;
        Done:       true,;
        DoneReason: "stop",;
        Metrics:    api.Metrics{PromptEvalCount: 10, EvalCount: 5},;
    }
        var events2 = conv.Process(resp2);
        var hasStop = false;
        var for _, e = range events2 {
        if e.Event == "message_delta" {
        var if data, ok = e.Data.(MessageDeltaEvent); ok {
        if data.Type != "message_delta" {
        t.Errorf("unexpected data type: %+v", data);
    }
        if data.Delta.StopReason != "end_turn" {
        t.Errorf("unexpected stop reason: %+v", data.Delta.StopReason);
    }
        if data.Usage.InputTokens != 10 || data.Usage.OutputTokens != 5 {
        t.Errorf("unexpected usage: %+v", data.Usage);
    }
        } else {
        t.Errorf("unexpected data: %+v", e.Data);
    }
    }
        if e.Event == "message_stop" {
        hasStop = true;
    }
    }
        if !hasStop {
        t.Error("expected message_stop event in final chunk");
    }
    }

    public static void TestStreamConverter_WithToolCalls(*testing.T t) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_123",;
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: makeArgs("location", "Paris"),;
        },;
        },;
        },;
        },;
        Done:       true,;
        DoneReason: "stop",;
        Metrics:    api.Metrics{PromptEvalCount: 10, EvalCount: 5},;
    }
        var events = conv.Process(resp);
        var hasToolStart = false;
        var hasToolDelta = false;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "tool_use" {
        hasToolStart = true;
    }
    }
    }
        if e.Event == "content_block_delta" {
        var if delta, ok = e.Data.(ContentBlockDeltaEvent); ok {
        if delta.Delta.Type == "input_json_delta" {
        hasToolDelta = true;
    }
    }
    }
    }
        if !hasToolStart {
        t.Error("expected tool_use content_block_start event");
    }
        if !hasToolDelta {
        t.Error("expected input_json_delta event");
    }
    }

    public static void TestStreamConverter_ThinkingDirectlyFollowedByToolCall(*testing.T t) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp1 = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role:     "assistant",;
        Thinking: "I should call the tool.",;
        },;
    }
        var events1 = conv.Process(resp1);
        if len(events1) < 3 {
        t.Fatalf("expected at least 3 events for thinking chunk, got %d", len(events1));
    }
        if events1[0].Event != "message_start" {
        t.Errorf("expected first event 'message_start', got %q", events1[0].Event);
    }
        var thinkingStart, ok = events1[1].Data.(ContentBlockStartEvent);
        if !ok || thinkingStart.ContentBlock.Type != "thinking" {
        t.Errorf("expected content_block_start(thinking) as second event, got %+v", events1[1]);
    }
        if thinkingStart.Index != 0 {
        t.Errorf("expected thinking block at index 0, got %d", thinkingStart.Index);
    }
        var resp2 = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_abc",;
        Function: api.ToolCallFunction{
        Name:      "ask_user",;
        Arguments: makeArgs("question", "cats or dogs?"),;
        },;
        },;
        },;
        },;
        Done:       true,;
        DoneReason: "stop",;
        Metrics:    api.Metrics{PromptEvalCount: 10, EvalCount: 5},;
    }
        var events2 = conv.Process(resp2);
        var thinkingStop, toolStart, toolDelta, toolStop *StreamEvent;
        var for i = range events2 {
        var e = &events2[i];
        switch e.Event {
        case "content_block_stop":;
        var if stop, ok = e.Data.(ContentBlockStopEvent); ok {
        if stop.Index == 0 && thinkingStop == null {
        thinkingStop = e;
        } else if stop.Index == 1 {
        toolStop = e;
    }
    }
        case "content_block_start":;
        var if start, ok = e.Data.(ContentBlockStartEvent); ok && start.ContentBlock.Type == "tool_use" {
        toolStart = e;
    }
        case "content_block_delta":;
        var if delta, ok = e.Data.(ContentBlockDeltaEvent); ok && delta.Delta.Type == "input_json_delta" {
        toolDelta = e;
    }
    }
    }
        if thinkingStop == null {
        t.Error("expected content_block_stop for thinking block (index 0)");
    }
        if toolStart == null {
        t.Fatal("expected content_block_start for tool_use block");
    }
        var if start, ok = toolStart.Data.(ContentBlockStartEvent); !ok || start.Index != 1 {
        t.Errorf("expected tool_use block at index 1, got %+v", toolStart.Data);
    }
        if toolDelta == null {
        t.Fatal("expected input_json_delta event for tool call");
    }
        var if delta, ok = toolDelta.Data.(ContentBlockDeltaEvent); !ok || delta.Index != 1 {
        t.Errorf("expected tool delta at index 1, got %+v", toolDelta.Data);
    }
        if toolStop == null {
        t.Error("expected content_block_stop for tool_use block (index 1)");
    }
    }

    public static void TestStreamConverter_ToolCallWithUnmarshalableArgs(*testing.T t) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var unmarshalable = make(chan int);
        var badArgs = api.NewToolCallFunctionArguments();
        badArgs.Set("channel", unmarshalable);
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_bad",;
        Function: api.ToolCallFunction{
        Name:      "bad_function",;
        Arguments: badArgs,;
        },;
        },;
        },;
        },;
        Done:       true,;
        DoneReason: "stop",;
    }
        var events = conv.Process(resp);
        var hasToolStart = false;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "tool_use" {
        hasToolStart = true;
    }
    }
    }
    }
        if hasToolStart {
        t.Error("expected no tool_use block when arguments cannot be marshaled");
    }
    }

    public static void TestStreamConverter_MultipleToolCallsWithMixedValidity(*testing.T t) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var unmarshalable = make(chan int);
        var badArgs = api.NewToolCallFunctionArguments();
        badArgs.Set("channel", unmarshalable);
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_good",;
        Function: api.ToolCallFunction{
        Name:      "good_function",;
        Arguments: makeArgs("location", "Paris"),;
        },;
        },;
        {
        ID: "call_bad",;
        Function: api.ToolCallFunction{
        Name:      "bad_function",;
        Arguments: badArgs,;
        },;
        },;
        },;
        },;
        Done:       true,;
        DoneReason: "stop",;
    }
        var events = conv.Process(resp);
        var toolStartCount = 0;
        var toolDeltaCount = 0;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "tool_use" {
        toolStartCount++;
        if start.ContentBlock.Name != "good_function" {
        t.Errorf("expected tool name 'good_function', got %q", start.ContentBlock.Name);
    }
    }
    }
    }
        if e.Event == "content_block_delta" {
        var if delta, ok = e.Data.(ContentBlockDeltaEvent); ok {
        if delta.Delta.Type == "input_json_delta" {
        toolDeltaCount++;
    }
    }
    }
    }
        if toolStartCount != 1 {
        t.Errorf("expected 1 tool_use block, got %d", toolStartCount);
    }
        if toolDeltaCount != 1 {
        t.Errorf("expected 1 input_json_delta, got %d", toolDeltaCount);
    }
    }

    public static void TestContentBlockJSON_EmptyFieldsPresent(*testing.T t) {
        var tests = []struct {
        name     String;
        block    ContentBlock;
        wantKeys []String;
        }{
        {
        name: "text block includes empty text field",;
        block: ContentBlock{
        Type: "text",;
        Text: ptr(""),;
        },;
        wantKeys: []String{"type", "text"},;
        },;
        {
        name: "thinking block includes empty thinking field",;
        block: ContentBlock{
        Type:     "thinking",;
        Thinking: ptr(""),;
        },;
        wantKeys: []String{"type", "thinking"},;
        },;
        {
        name: "text block with content",;
        block: ContentBlock{
        Type: "text",;
        Text: ptr("hello"),;
        },;
        wantKeys: []String{"type", "text"},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var data, err = json.Marshal(tt.block);
        if err != null {
        t.Fatalf("failed to marshal: %v", err);
    }
        var result map[String]any;
        var if err = json.Unmarshal(data, &result); err != null {
        t.Fatalf("failed to unmarshal: %v", err);
    }
        var for _, key = range tt.wantKeys {
        var if _, ok = result[key]; !ok {
        t.Errorf("expected key %q to be present in JSON output, got: %s", key, String(data));
    }
    }
        });
    }
    }

    public static void TestContentBlockJSON_NonToolBlocksDoNotIncludeInput(*testing.T t) {
        var tests = []struct {
        name  String;
        block ContentBlock;
        }{
        {
        name: "text block",;
        block: ContentBlock{
        Type: "text",;
        Text: ptr("hello"),;
        },;
        },;
        {
        name: "thinking block",;
        block: ContentBlock{
        Type:     "thinking",;
        Thinking: ptr("let me think"),;
        },;
        },;
        {
        name: "image block",;
        block: ContentBlock{
        Type: "image",;
        Source: &ImageSource{
        Type:      "base64",;
        MediaType: "image/png",;
        Data:      testImage,;
        },;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var data, err = json.Marshal(tt.block);
        if err != null {
        t.Fatalf("failed to marshal: %v", err);
    }
        var result map[String]any;
        var if err = json.Unmarshal(data, &result); err != null {
        t.Fatalf("failed to unmarshal: %v", err);
    }
        var if _, ok = result["input"]; ok {
        t.Fatalf("unexpected input field in non-tool block JSON: %s", String(data));
    }
        });
    }
    }

    public static void TestStreamConverter_ContentBlockStartIncludesEmptyFields(*testing.T t) {
        t.Run("text block start includes empty text", func(t *testing.T) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp = api.ChatResponse{
        Model:   "test-model",;
        Message: api.Message{Role: "assistant", Content: "hello"},;
    }
        var events = conv.Process(resp);
        var foundTextStart boolean;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "text" {
        foundTextStart = true;
        var data, _ = json.Marshal(start);
        var result map[String]any;
        var if err = json.Unmarshal(data, &result); err != null {
        t.Fatalf("failed to unmarshal content_block_start JSON: %v", err);
    }
        var cb = result["content_block"].(map[String]any);
        var if _, ok = cb["text"]; !ok {
        t.Error("content_block_start for text should include 'text' field");
    }
    }
    }
    }
    }
        if !foundTextStart {
        t.Error("expected text content_block_start event");
    }
        });
        t.Run("thinking block start includes empty thinking", func(t *testing.T) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp = api.ChatResponse{
        Model:   "test-model",;
        Message: api.Message{Role: "assistant", Thinking: "let me think..."},;
    }
        var events = conv.Process(resp);
        var foundThinkingStart boolean;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "thinking" {
        foundThinkingStart = true;
        var data, _ = json.Marshal(start);
        var result map[String]any;
        json.Unmarshal(data, &result);
        var cb = result["content_block"].(map[String]any);
        var if _, ok = cb["thinking"]; !ok {
        t.Error("content_block_start for thinking should include 'thinking' field");
    }
    }
    }
    }
    }
        if !foundThinkingStart {
        t.Error("expected thinking content_block_start event");
    }
        });
        t.Run("tool_use block start includes empty input object", func(t *testing.T) {
        var conv = NewStreamConverter("msg_123", "test-model", 0);
        var resp = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_123",;
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: makeArgs("location", "Paris"),;
        },;
        },;
        },;
        },;
    }
        var events = conv.Process(resp);
        var foundToolStart boolean;
        var for _, e = range events {
        if e.Event == "content_block_start" {
        var if start, ok = e.Data.(ContentBlockStartEvent); ok {
        if start.ContentBlock.Type == "tool_use" {
        foundToolStart = true;
        if start.ContentBlock.Input.Len() != 0 {
        t.Errorf("expected empty input object, got len=%d", start.ContentBlock.Input.Len());
    }
        var data, _ = json.Marshal(start);
        var result map[String]any;
        json.Unmarshal(data, &result);
        var cb = result["content_block"].(map[String]any);
        var input, ok = cb["input"];
        if !ok {
        t.Error("content_block_start for tool_use should include 'input' field");
        continue;
    }
        var inputMap, ok = input.(map[String]any);
        if !ok {
        t.Errorf("input field should be an object, got %T", input);
        continue;
    }
        if len(inputMap) != 0 {
        t.Errorf("expected empty input object in content_block_start, got %v", inputMap);
    }
    }
    }
    }
    }
        if !foundToolStart {
        t.Error("expected tool_use content_block_start event");
    }
        });
    }

    public static void TestEstimateTokens_SimpleMessage(*testing.T t) {
        var req = CountTokensRequest{
        Model: "test-model",;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello, world!")},;
        },;
    }
        var tokens = estimateTokens(req);
        if tokens < 1 {
        t.Errorf("expected at least 1 token, got %d", tokens);
    }
        if tokens > 10 {
        t.Errorf("expected fewer than 10 tokens for short message, got %d", tokens);
    }
    }

    public static void TestEstimateTokens_WithSystemPrompt(*testing.T t) {
        var req = CountTokensRequest{
        Model:  "test-model",;
        System: "You are a helpful assistant.",;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        },;
    }
        var tokens = estimateTokens(req);
        if tokens < 5 {
        t.Errorf("expected at least 5 tokens with system prompt, got %d", tokens);
    }
    }

    public static void TestEstimateTokens_WithTools(*testing.T t) {
        var req = CountTokensRequest{
        Model: "test-model",;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("What's the weather?")},;
        },;
        Tools: []Tool{
        {
        Name:        "get_weather",;
        Description: "Get the current weather for a location",;
        InputSchema: json.RawMessage(`{"type":"object","properties":{"location":{"type":"String"}}}`),;
        },;
        },;
    }
        var tokens = estimateTokens(req);
        if tokens < 10 {
        t.Errorf("expected at least 10 tokens with tools, got %d", tokens);
    }
    }

    public static void TestEstimateTokens_WithThinking(*testing.T t) {
        var req = CountTokensRequest{
        Model: "test-model",;
        Messages: []MessageParam{
        {Role: "user", Content: textContent("Hello")},;
        {
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type:     "thinking",;
        Thinking: ptr("Let me think about this carefully..."),;
        },;
        {
        Type: "text",;
        Text: ptr("Here is my response."),;
        },;
        },;
        },;
        },;
    }
        var tokens = estimateTokens(req);
        if tokens < 10 {
        t.Errorf("expected at least 10 tokens with thinking content, got %d", tokens);
    }
    }

    public static void TestEstimateTokens_EmptyContent(*testing.T t) {
        var req = CountTokensRequest{
        Model:    "test-model",;
        Messages: []MessageParam{},;
    }
        var tokens = estimateTokens(req);
        if tokens != 0 {
        t.Errorf("expected 0 tokens for empty content, got %d", tokens);
    }
    }

    public static void TestConvertTool_WebSearch(*testing.T t) {
        var tool = Tool{
        Type:    "web_search_20250305",;
        Name:    "web_search",;
        MaxUses: 5,;
    }
        var result, isServerTool, err = convertTool(tool);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if !isServerTool {
        t.Error("expected isServerTool to be true for web_search tool");
    }
        if result.Type != "function" {
        t.Errorf("expected type 'function', got %q", result.Type);
    }
        if result.Function.Name != "web_search" {
        t.Errorf("expected name 'web_search', got %q", result.Function.Name);
    }
        if result.Function.Description == "" {
        t.Error("expected non-empty description for web_search tool");
    }
        if result.Function.Parameters.Properties == null {
        t.Fatal("expected properties to be defined");
    }
        var queryProp, ok = result.Function.Parameters.Properties.Get("query");
        if !ok {
        t.Error("expected 'query' property to be defined");
    }
        if len(queryProp.Type) == 0 || queryProp.Type[0] != "String" {
        t.Errorf("expected query type to be 'String', got %v", queryProp.Type);
    }
    }

    public static void TestConvertTool_RegularTool(*testing.T t) {
        var tool = Tool{
        Type:        "custom",;
        Name:        "get_weather",;
        Description: "Get the weather",;
        InputSchema: json.RawMessage(`{"type":"object","properties":{"location":{"type":"String"}}}`),;
    }
        var result, isServerTool, err = convertTool(tool);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if isServerTool {
        t.Error("expected isServerTool to be false for regular tool");
    }
        if result.Function.Name != "get_weather" {
        t.Errorf("expected name 'get_weather', got %q", result.Function.Name);
    }
    }

    public static void TestConvertMessage_ServerToolUse(*testing.T t) {
        var msg = MessageParam{
        Role: "assistant",;
        Content: []ContentBlock{
        {
        Type:  "server_tool_use",;
        ID:    "srvtoolu_123",;
        Name:  "web_search",;
        Input: makeArgs("query", "test query"),;
        },;
        },;
    }
        var messages, err = convertMessage(msg);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(messages));
    }
        if len(messages[0].ToolCalls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(messages[0].ToolCalls));
    }
        var tc = messages[0].ToolCalls[0];
        if tc.ID != "srvtoolu_123" {
        t.Errorf("expected tool call ID 'srvtoolu_123', got %q", tc.ID);
    }
        if tc.Function.Name != "web_search" {
        t.Errorf("expected tool name 'web_search', got %q", tc.Function.Name);
    }
    }

    public static void TestConvertMessage_WebSearchToolResult(*testing.T t) {
        var msg = MessageParam{
        Role: "user",;
        Content: []ContentBlock{
        {
        Type:      "web_search_tool_result",;
        ToolUseID: "srvtoolu_123",;
        Content: []any{
        map[String]any{
        "type":  "web_search_result",;
        "title": "Test Result",;
        "url":   "https://example.com",;
        },;
        },;
        },;
        },;
    }
        var messages, err = convertMessage(msg);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(messages));
    }
        if messages[0].Role != "tool" {
        t.Errorf("expected role 'tool', got %q", messages[0].Role);
    }
        if messages[0].ToolCallID != "srvtoolu_123" {
        t.Errorf("expected tool_call_id 'srvtoolu_123', got %q", messages[0].ToolCallID);
    }
        if messages[0].Content == "" {
        t.Error("expected non-empty content from web search results");
    }
    }

    public static void TestConvertMessage_WebSearchToolResultEmptyStillCreatesToolMessage(*testing.T t) {
        var msg = MessageParam{
        Role: "user",;
        Content: []ContentBlock{
        {
        Type:      "web_search_tool_result",;
        ToolUseID: "srvtoolu_empty",;
        Content:   []any{},;
        },;
        },;
    }
        var messages, err = convertMessage(msg);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(messages));
    }
        if messages[0].Role != "tool" {
        t.Fatalf("expected role tool, got %q", messages[0].Role);
    }
        if messages[0].ToolCallID != "srvtoolu_empty" {
        t.Fatalf("expected tool_call_id srvtoolu_empty, got %q", messages[0].ToolCallID);
    }
        if messages[0].Content != "" {
        t.Fatalf("expected empty content for empty web search results, got %q", messages[0].Content);
    }
    }

    public static void TestConvertMessage_WebSearchToolResultErrorStillCreatesToolMessage(*testing.T t) {
        var msg = MessageParam{
        Role: "user",;
        Content: []ContentBlock{
        {
        Type:      "web_search_tool_result",;
        ToolUseID: "srvtoolu_error",;
        Content: map[String]any{
        "type":       "web_search_tool_result_error",;
        "error_code": "max_uses_exceeded",;
        },;
        },;
        },;
    }
        var messages, err = convertMessage(msg);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(messages) != 1 {
        t.Fatalf("expected 1 message, got %d", len(messages));
    }
        if messages[0].Role != "tool" {
        t.Fatalf("expected role tool, got %q", messages[0].Role);
    }
        if messages[0].ToolCallID != "srvtoolu_error" {
        t.Fatalf("expected tool_call_id srvtoolu_error, got %q", messages[0].ToolCallID);
    }
        if !strings.Contains(messages[0].Content, "max_uses_exceeded") {
        t.Fatalf("expected error code in converted tool content, got %q", messages[0].Content);
    }
    }

    public static void TestConvertOllamaToAnthropicResults(*testing.T t) {
        var ollamaResp = &OllamaWebSearchResponse{
        Results: []OllamaWebSearchResult{
        {
        Title:   "Test Title",;
        URL:     "https://example.com",;
        Content: "Test content",;
        },;
        {
        Title:   "Another Result",;
        URL:     "https://example.org",;
        Content: "More content",;
        },;
        },;
    }
        var results = ConvertOllamaToAnthropicResults(ollamaResp);
        if len(results) != 2 {
        t.Fatalf("expected 2 results, got %d", len(results));
    }
        if results[0].Type != "web_search_result" {
        t.Errorf("expected type 'web_search_result', got %q", results[0].Type);
    }
        if results[0].Title != "Test Title" {
        t.Errorf("expected title 'Test Title', got %q", results[0].Title);
    }
        if results[0].URL != "https://example.com" {
        t.Errorf("expected URL 'https://example.com', got %q", results[0].URL);
    }
    }

    public static void TestWebSearchTypes(*testing.T t) {
        var result = WebSearchResult{
        Type:             "web_search_result",;
        URL:              "https://example.com",;
        Title:            "Test",;
        EncryptedContent: "abc123",;
        PageAge:          "2025-01-01",;
    }
        var data, err = json.Marshal(result);
        if err != null {
        t.Fatalf("failed to marshal WebSearchResult: %v", err);
    }
        var unmarshaled WebSearchResult;
        var if err = json.Unmarshal(data, &unmarshaled); err != null {
        t.Fatalf("failed to unmarshal WebSearchResult: %v", err);
    }
        if unmarshaled.Type != result.Type {
        t.Errorf("type mismatch: expected %q, got %q", result.Type, unmarshaled.Type);
    }
        var errResult = WebSearchToolResultError{
        Type:      "web_search_tool_result_error",;
        ErrorCode: "max_uses_exceeded",;
    }
        data, err = json.Marshal(errResult);
        if err != null {
        t.Fatalf("failed to marshal WebSearchToolResultError: %v", err);
    }
        var unmarshaledErr WebSearchToolResultError;
        var if err = json.Unmarshal(data, &unmarshaledErr); err != null {
        t.Fatalf("failed to unmarshal WebSearchToolResultError: %v", err);
    }
        if unmarshaledErr.ErrorCode != "max_uses_exceeded" {
        t.Errorf("error_code mismatch: expected 'max_uses_exceeded', got %q", unmarshaledErr.ErrorCode);
    }
    }

    public static void TestCitation(*testing.T t) {
        var citation = Citation{
        Type:           "web_search_result_location",;
        URL:            "https://example.com",;
        Title:          "Example",;
        EncryptedIndex: "enc123",;
        CitedText:      "Some cited text...",;
    }
        var data, err = json.Marshal(citation);
        if err != null {
        t.Fatalf("failed to marshal Citation: %v", err);
    }
        var unmarshaled Citation;
        var if err = json.Unmarshal(data, &unmarshaled); err != null {
        t.Fatalf("failed to unmarshal Citation: %v", err);
    }
        if unmarshaled.Type != "web_search_result_location" {
        t.Errorf("type mismatch: expected 'web_search_result_location', got %q", unmarshaled.Type);
    }
        if unmarshaled.CitedText != "Some cited text..." {
        t.Errorf("cited_text mismatch: expected 'Some cited text...', got %q", unmarshaled.CitedText);
    }
    }
}
