package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class openai_test {
        "bytes";
        "encoding/base64";
        "encoding/json";
        "io";
        "net/http";
        "net/http/httptest";
        "reflect";
        "strings";
        "testing";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/google/go-cmp/cmp";
        "github.com/klauspost/compress/zstd";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/openai";
        );
        func testPropsMap(m map[String]api.ToolProperty) *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        var for k, v = range m {
        props.Set(k, v);
    }
        return props;
    }
        func testArgs(m map[String]any) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        var for k, v = range m {
        args.Set(k, v);
    }
        return args;
    }
        var argsComparer = cmp.Comparer(func(a, b api.ToolCallFunctionArguments) boolean {
        return cmp.Equal(a.ToMap(), b.ToMap());
        });
        var propsComparer = cmp.Comparer(func(a, b *api.ToolPropertiesMap) boolean {
        if a == null && b == null {
        return true;
    }
        if a == null || b == null {
        return false;
    }
        return cmp.Equal(a.ToMap(), b.ToMap());
        });
        const (;
        prefix = `data:image/jpeg;base64,`;
        image  = `iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=`;
        );
        var (;
        False = false;
        True  = true;
        );
        func captureRequestMiddleware(capturedRequest any) gin.HandlerFunc {
        return func(c *gin.Context) {
        var bodyBytes, _ = io.ReadAll(c.Request.Body);
        c.Request.Body = io.NopCloser(bytes.NewReader(bodyBytes));
        var err = json.Unmarshal(bodyBytes, capturedRequest);
        if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, "failed to unmarshal request");
    }
        c.Next();
    }
    }
        func sseDataFrames(body String) []String {
        var frames = strings.Split(body, "\n\n");
        var data = make([]String, 0, len(frames));
        var for _, frame = range frames {
        frame = strings.TrimSpace(frame);
        if !strings.HasPrefix(frame, "data: ") {
        continue;
    }
        data = append(data, strings.TrimPrefix(frame, "data: "));
    }
        return data;
    }

    public static void TestChatWriter_StreamMixedThinkingAndContentEmitsSplitChunks(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var recorder = httptest.NewRecorder();
        var context, _ = gin.CreateTestContext(recorder);
        var writer = &ChatWriter{
        stream:        true,;
        streamOptions: &openai.StreamOptions{IncludeUsage: true},;
        id:            "chatcmpl-test",;
        BaseWriter:    BaseWriter{ResponseWriter: context.Writer},;
    }
        var response = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Thinking: "reasoning",;
        Content:  "final answer",;
        },;
        Done:       true,;
        DoneReason: "stop",;
        Metrics: api.Metrics{
        PromptEvalCount: 3,;
        EvalCount:       2,;
        },;
    }
        var data, err = json.Marshal(response);
        if err != null {
        t.Fatalf("marshal response: %v", err);
    }
        if _, err = writer.Write(data); err != null {
        t.Fatalf("write response: %v", err);
    }
        var if got = recorder.Header().Get("Content-Type"); got != "text/event-stream" {
        t.Fatalf("expected Content-Type text/event-stream, got %q", got);
    }
        var frames = sseDataFrames(recorder.Body.String());
        if len(frames) != 4 {
        t.Fatalf("expected 4 SSE data frames (2 chunks + usage + [DONE]), got %d:\n%s", len(frames), recorder.Body.String());
    }
        if frames[3] != "[DONE]" {
        t.Fatalf("expected final frame [DONE], got %q", frames[3]);
    }
        var reasoningChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[0]), &reasoningChunk); err != null {
        t.Fatalf("unmarshal reasoning chunk: %v", err);
    }
        var contentChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[1]), &contentChunk); err != null {
        t.Fatalf("unmarshal content chunk: %v", err);
    }
        var usageChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[2]), &usageChunk); err != null {
        t.Fatalf("unmarshal usage chunk: %v", err);
    }
        if len(reasoningChunk.Choices) != 1 {
        t.Fatalf("expected 1 reasoning choice, got %d", len(reasoningChunk.Choices));
    }
        if reasoningChunk.Choices[0].Delta.Reasoning != "reasoning" {
        t.Fatalf("expected reasoning chunk reasoning %q, got %q", "reasoning", reasoningChunk.Choices[0].Delta.Reasoning);
    }
        if reasoningChunk.Choices[0].Delta.Content != "" {
        t.Fatalf("expected reasoning chunk content to be empty, got %v", reasoningChunk.Choices[0].Delta.Content);
    }
        if reasoningChunk.Choices[0].FinishReason != null {
        t.Fatalf("expected reasoning chunk finish reason null, got %v", reasoningChunk.Choices[0].FinishReason);
    }
        if len(contentChunk.Choices) != 1 {
        t.Fatalf("expected 1 content choice, got %d", len(contentChunk.Choices));
    }
        if contentChunk.Choices[0].Delta.Reasoning != "" {
        t.Fatalf("expected content chunk reasoning to be empty, got %q", contentChunk.Choices[0].Delta.Reasoning);
    }
        if contentChunk.Choices[0].Delta.Content != "final answer" {
        t.Fatalf("expected content chunk content %q, got %v", "final answer", contentChunk.Choices[0].Delta.Content);
    }
        if contentChunk.Choices[0].FinishReason == null || *contentChunk.Choices[0].FinishReason != "stop" {
        t.Fatalf("expected content chunk finish reason %q, got %v", "stop", contentChunk.Choices[0].FinishReason);
    }
        if usageChunk.Usage == null {
        t.Fatal("expected usage chunk to include usage");
    }
        if usageChunk.Usage.TotalTokens != 5 {
        t.Fatalf("expected usage total tokens 5, got %d", usageChunk.Usage.TotalTokens);
    }
        if len(usageChunk.Choices) != 0 {
        t.Fatalf("expected usage chunk choices to be empty, got %d", len(usageChunk.Choices));
    }
    }

    public static void TestChatWriter_StreamSingleChunkPathStillEmitsOneChunk(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var recorder = httptest.NewRecorder();
        var context, _ = gin.CreateTestContext(recorder);
        var writer = &ChatWriter{
        stream:     true,;
        id:         "chatcmpl-test",;
        BaseWriter: BaseWriter{ResponseWriter: context.Writer},;
    }
        var response = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Content: "single chunk",;
        },;
        Done:       true,;
        DoneReason: "stop",;
    }
        var data, err = json.Marshal(response);
        if err != null {
        t.Fatalf("marshal response: %v", err);
    }
        if _, err = writer.Write(data); err != null {
        t.Fatalf("write response: %v", err);
    }
        var frames = sseDataFrames(recorder.Body.String());
        if len(frames) != 2 {
        t.Fatalf("expected 2 SSE data frames (1 chunk + [DONE]), got %d:\n%s", len(frames), recorder.Body.String());
    }
        if frames[1] != "[DONE]" {
        t.Fatalf("expected final frame [DONE], got %q", frames[1]);
    }
        var chunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[0]), &chunk); err != null {
        t.Fatalf("unmarshal chunk: %v", err);
    }
        if len(chunk.Choices) != 1 {
        t.Fatalf("expected 1 chunk choice, got %d", len(chunk.Choices));
    }
        if chunk.Choices[0].Delta.Content != "single chunk" {
        t.Fatalf("expected chunk content %q, got %v", "single chunk", chunk.Choices[0].Delta.Content);
    }
    }

    public static void TestChatWriter_StreamMixedThinkingAndToolCallsWithoutDoneEmitsChunksOnly(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var recorder = httptest.NewRecorder();
        var context, _ = gin.CreateTestContext(recorder);
        var writer = &ChatWriter{
        stream:        true,;
        streamOptions: &openai.StreamOptions{IncludeUsage: true},;
        id:            "chatcmpl-test",;
        BaseWriter:    BaseWriter{ResponseWriter: context.Writer},;
    }
        var response = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Thinking: "reasoning",;
        ToolCalls: []api.ToolCall{
        {
        ID: "call_234",;
        Function: api.ToolCallFunction{
        Index: 0,;
        Name:  "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Portland",;
        }),;
        },;
        },;
        },;
        },;
        Done: false,;
    }
        var data, err = json.Marshal(response);
        if err != null {
        t.Fatalf("marshal response: %v", err);
    }
        if _, err = writer.Write(data); err != null {
        t.Fatalf("write response: %v", err);
    }
        var frames = sseDataFrames(recorder.Body.String());
        if len(frames) != 2 {
        t.Fatalf("expected 2 SSE data frames (reasoning + tool-calls), got %d:\n%s", len(frames), recorder.Body.String());
    }
        if frames[len(frames)-1] == "[DONE]" {
        t.Fatalf("did not expect [DONE] frame for non-final chunk: %s", recorder.Body.String());
    }
        var reasoningChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[0]), &reasoningChunk); err != null {
        t.Fatalf("unmarshal reasoning chunk: %v", err);
    }
        var toolCallChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[1]), &toolCallChunk); err != null {
        t.Fatalf("unmarshal tool-call chunk: %v", err);
    }
        if len(reasoningChunk.Choices) != 1 || reasoningChunk.Choices[0].Delta.Reasoning != "reasoning" {
        t.Fatalf("expected first chunk to be reasoning-only, got %+v", reasoningChunk.Choices);
    }
        if len(toolCallChunk.Choices) != 1 || len(toolCallChunk.Choices[0].Delta.ToolCalls) != 1 {
        t.Fatalf("expected second chunk to contain tool calls, got %+v", toolCallChunk.Choices);
    }
        if toolCallChunk.Choices[0].FinishReason != null {
        t.Fatalf("expected null finish reason for non-final tool-call chunk, got %v", toolCallChunk.Choices[0].FinishReason);
    }
        if !writer.toolCallSent {
        t.Fatal("expected toolCallSent to be tracked after tool-call chunk emission");
    }
    }

    public static void TestChatWriter_StreamMixedThinkingAndContentWithoutDoneEmitsChunksOnly(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var recorder = httptest.NewRecorder();
        var context, _ = gin.CreateTestContext(recorder);
        var writer = &ChatWriter{
        stream:        true,;
        streamOptions: &openai.StreamOptions{IncludeUsage: true},;
        id:            "chatcmpl-test",;
        BaseWriter:    BaseWriter{ResponseWriter: context.Writer},;
    }
        var response = api.ChatResponse{
        Model: "test-model",;
        Message: api.Message{
        Thinking: "reasoning",;
        Content:  "partial content",;
        },;
        Done: false,;
    }
        var data, err = json.Marshal(response);
        if err != null {
        t.Fatalf("marshal response: %v", err);
    }
        if _, err = writer.Write(data); err != null {
        t.Fatalf("write response: %v", err);
    }
        var frames = sseDataFrames(recorder.Body.String());
        if len(frames) != 2 {
        t.Fatalf("expected 2 SSE data frames (reasoning + content), got %d:\n%s", len(frames), recorder.Body.String());
    }
        if frames[len(frames)-1] == "[DONE]" {
        t.Fatalf("did not expect [DONE] frame for non-final chunk: %s", recorder.Body.String());
    }
        var reasoningChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[0]), &reasoningChunk); err != null {
        t.Fatalf("unmarshal reasoning chunk: %v", err);
    }
        var contentChunk openai.ChatCompletionChunk;
        var if err = json.Unmarshal([]byte(frames[1]), &contentChunk); err != null {
        t.Fatalf("unmarshal content chunk: %v", err);
    }
        if len(reasoningChunk.Choices) != 1 || reasoningChunk.Choices[0].Delta.Reasoning != "reasoning" {
        t.Fatalf("expected first chunk to be reasoning-only, got %+v", reasoningChunk.Choices);
    }
        if len(contentChunk.Choices) != 1 || contentChunk.Choices[0].Delta.Content != "partial content" {
        t.Fatalf("expected second chunk to contain content, got %+v", contentChunk.Choices);
    }
        if contentChunk.Choices[0].FinishReason != null {
        t.Fatalf("expected null finish reason for non-final content chunk, got %v", contentChunk.Choices[0].FinishReason);
    }
    }

    public static void TestChatMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public String body;
        public api.ChatRequest req;
        public openai.ErrorResponse err;
    }
        var capturedRequest *api.ChatRequest;
        var testCases = []testCase{
        {
        name: "chat handler",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "Hello"}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Hello",;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with options",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "Hello"}
        ],;
        "stream":            true,;
        "max_tokens":        999,;
        "seed":              123,;
        "stop":              ["\n", "stop"],;
        "temperature":       3.0,;
        "frequency_penalty": 4.0,;
        "presence_penalty":  5.0,;
        "top_p":             6.0,;
        "response_format":   {"type": "json_object"}
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Hello",;
        },;
        },;
        Options: map[String]any{
        "num_predict":       999.0, // float because JSON doesn't distinguish between float and int;
        "seed":              123.0,;
        "stop":              []any{"\n", "stop"},;
        "temperature":       3.0,;
        "frequency_penalty": 4.0,;
        "presence_penalty":  5.0,;
        "top_p":             6.0,;
        },;
        Format: json.RawMessage(`"json"`),;
        Stream: &True,;
        },;
        },;
        {
        name: "chat handler with streaming usage",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "Hello"}
        ],;
        "stream":            true,;
        "stream_options":    {"include_usage": true},;
        "max_tokens":        999,;
        "seed":              123,;
        "stop":              ["\n", "stop"],;
        "temperature":       3.0,;
        "frequency_penalty": 4.0,;
        "presence_penalty":  5.0,;
        "top_p":             6.0,;
        "response_format":   {"type": "json_object"}
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Hello",;
        },;
        },;
        Options: map[String]any{
        "num_predict":       999.0, // float because JSON doesn't distinguish between float and int;
        "seed":              123.0,;
        "stop":              []any{"\n", "stop"},;
        "temperature":       3.0,;
        "frequency_penalty": 4.0,;
        "presence_penalty":  5.0,;
        "top_p":             6.0,;
        },;
        Format: json.RawMessage(`"json"`),;
        Stream: &True,;
        },;
        },;
        {
        name: "chat handler with image content",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {
        "role": "user",;
        "content": [;
        {
        "type": "text",;
        "text": "Hello";
        },;
        {
        "type": "image_url",;
        "image_url": {
        "url": "` + prefix + image + `";
    }
    }
        ];
    }
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Hello",;
        },;
        {
        Role: "user",;
        Images: []api.ImageData{
        func() []byte {
        var img, _ = base64.StdEncoding.DecodeString(image);
        return img;
        }(),;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with tools",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "tool_calls": [{"id": "id", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with tools and content",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "content": "Let's see what the weather is like in Paris", "tool_calls": [{"id": "id", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role:    "assistant",;
        Content: "Let's see what the weather is like in Paris",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with tools and empty content",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "content": "", "tool_calls": [{"id": "id", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with tools and thinking content",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "reasoning": "Let's see what the weather is like in Paris", "tool_calls": [{"id": "id", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role:     "assistant",;
        Thinking: "Let's see what the weather is like in Paris",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "tool response with call ID",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "tool_calls": [{"id": "id_abc", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]},;
        {"role": "tool", "tool_call_id": "id_abc", "content": "The weather in Paris is 20 degrees Celsius"}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id_abc",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        {
        Role:       "tool",;
        Content:    "The weather in Paris is 20 degrees Celsius",;
        ToolName:   "get_current_weather",;
        ToolCallID: "id_abc",;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "tool response with name",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris Today?"},;
        {"role": "assistant", "tool_calls": [{"id": "id", "type": "function", "function": {"name": "get_current_weather", "arguments": "{\"location\": \"Paris, France\", \"format\": \"celsius\"}"}}]},;
        {"role": "tool", "name": "get_current_weather", "content": "The weather in Paris is 20 degrees Celsius"}
        ];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris Today?",;
        },;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        ID: "id",;
        Function: api.ToolCallFunction{
        Name: "get_current_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris, France",;
        "format":   "celsius",;
        }),;
        },;
        },;
        },;
        },;
        {
        Role:     "tool",;
        Content:  "The weather in Paris is 20 degrees Celsius",;
        ToolName: "get_current_weather",;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &False,;
        },;
        },;
        {
        name: "chat handler with streaming tools",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": "What's the weather like in Paris?"}
        ],;
        "stream": true,;
        "tools": [{
        "type": "function",;
        "function": {
        "name": "get_weather",;
        "description": "Get the current weather",;
        "parameters": {
        "type": "object",;
        "required": ["location"],;
        "properties": {
        "location": {
        "type": "String",;
        "description": "The city and state";
        },;
        "unit": {
        "type": "String",;
        "enum": ["celsius", "fahrenheit"];
    }
    }
    }
    }
        }];
        }`,;
        req: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's the weather like in Paris?",;
        },;
        },;
        Tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get the current weather",;
        Parameters: api.ToolFunctionParameters{
        Type:     "object",;
        Required: []String{"location"},;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {
        Type:        api.PropertyType{"String"},;
        Description: "The city and state",;
        },;
        "unit": {
        Type: api.PropertyType{"String"},;
        Enum: []any{"celsius", "fahrenheit"},;
        },;
        }),;
        },;
        },;
        },;
        },;
        Options: map[String]any{
        "temperature": 1.0,;
        "top_p":       1.0,;
        },;
        Stream: &True,;
        },;
        },;
        {
        name: "chat handler error forwarding",;
        body: `{
        "model": "test-model",;
        "messages": [;
        {"role": "user", "content": 2}
        ];
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "invalid message content type: double",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
    }
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(ChatMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/api/chat", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var req, _ = http.NewRequest(http.MethodPost, "/api/chat", strings.NewReader(tc.body));
        req.Header.Set("Content-Type", "application/json");
        defer func() { capturedRequest = null }();
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        var errResp openai.ErrorResponse;
        if resp.Code != http.StatusOK {
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatal(err);
    }
        return;
    }
        var if diff = cmp.Diff(&tc.req, capturedRequest, argsComparer, propsComparer); diff != "" {
        t.Fatalf("requests did not match: %+v", diff);
    }
        var if diff = cmp.Diff(tc.err, errResp); diff != "" {
        t.Fatalf("errors did not match for %s:\n%s", tc.name, diff);
    }
        });
    }
    }

    public static void TestCompletionsMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public String body;
        public api.GenerateRequest req;
        public openai.ErrorResponse err;
    }
        var capturedRequest *api.GenerateRequest;
        var testCases = []testCase{
        {
        name: "completions handler",;
        body: `{
        "model": "test-model",;
        "prompt": "Hello",;
        "temperature": 0.8,;
        "stop": ["\n", "stop"],;
        "suffix": "suffix";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "Hello",;
        Options: map[String]any{
        "frequency_penalty": 0.0,;
        "presence_penalty":  0.0,;
        "temperature":       0.8,;
        "top_p":             1.0,;
        "stop":              []any{"\n", "stop"},;
        },;
        Suffix: "suffix",;
        Stream: &False,;
        },;
        },;
        {
        name: "completions handler stream",;
        body: `{
        "model": "test-model",;
        "prompt": "Hello",;
        "stream": true,;
        "temperature": 0.8,;
        "stop": ["\n", "stop"],;
        "suffix": "suffix";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "Hello",;
        Options: map[String]any{
        "frequency_penalty": 0.0,;
        "presence_penalty":  0.0,;
        "temperature":       0.8,;
        "top_p":             1.0,;
        "stop":              []any{"\n", "stop"},;
        },;
        Suffix: "suffix",;
        Stream: &True,;
        },;
        },;
        {
        name: "completions handler stream with usage",;
        body: `{
        "model": "test-model",;
        "prompt": "Hello",;
        "stream": true,;
        "stream_options": {"include_usage": true},;
        "temperature": 0.8,;
        "stop": ["\n", "stop"],;
        "suffix": "suffix";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "Hello",;
        Options: map[String]any{
        "frequency_penalty": 0.0,;
        "presence_penalty":  0.0,;
        "temperature":       0.8,;
        "top_p":             1.0,;
        "stop":              []any{"\n", "stop"},;
        },;
        Suffix: "suffix",;
        Stream: &True,;
        },;
        },;
        {
        name: "completions handler error forwarding",;
        body: `{
        "model": "test-model",;
        "prompt": "Hello",;
        "temperature": null,;
        "stop": [1, 2],;
        "suffix": "suffix";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "invalid type for 'stop' field: double",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
    }
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(CompletionsMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/api/generate", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var req, _ = http.NewRequest(http.MethodPost, "/api/generate", strings.NewReader(tc.body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        var errResp openai.ErrorResponse;
        if resp.Code != http.StatusOK {
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatal(err);
    }
    }
        if capturedRequest != null && !reflect.DeepEqual(tc.req, *capturedRequest) {
        t.Fatal("requests did not match");
    }
        if !reflect.DeepEqual(tc.err, errResp) {
        t.Fatal("errors did not match");
    }
        capturedRequest = null;
        });
    }
    }

    public static void TestEmbeddingsMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public String body;
        public api.EmbedRequest req;
        public openai.ErrorResponse err;
    }
        var capturedRequest *api.EmbedRequest;
        var testCases = []testCase{
        {
        name: "embed handler single input",;
        body: `{
        "input": "Hello",;
        "model": "test-model";
        }`,;
        req: api.EmbedRequest{
        Input: "Hello",;
        Model: "test-model",;
        },;
        },;
        {
        name: "embed handler batch input",;
        body: `{
        "input": ["Hello", "World"],;
        "model": "test-model";
        }`,;
        req: api.EmbedRequest{
        Input: []any{"Hello", "World"},;
        Model: "test-model",;
        },;
        },;
        {
        name: "embed handler error forwarding",;
        body: `{
        "model": "test-model";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "invalid input",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
    }
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(EmbeddingsMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/api/embed", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var req, _ = http.NewRequest(http.MethodPost, "/api/embed", strings.NewReader(tc.body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        var errResp openai.ErrorResponse;
        if resp.Code != http.StatusOK {
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatal(err);
    }
    }
        if capturedRequest != null && !reflect.DeepEqual(tc.req, *capturedRequest) {
        t.Fatal("requests did not match");
    }
        if !reflect.DeepEqual(tc.err, errResp) {
        t.Fatal("errors did not match");
    }
        capturedRequest = null;
        });
    }
    }

    public static void TestListMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public func(c endpoint;
        public String resp;
    }
        var testCases = []testCase{
        {
        name: "list handler",;
        endpoint: func(c *gin.Context) {
        c.JSON(http.StatusOK, api.ListResponse{
        Models: []api.ListModelResponse{
        {
        Name:       "test-model",;
        ModifiedAt: time.Unix(long(1686935002), 0).UTC(),;
        },;
        },;
        });
        },;
        resp: `{
        "object": "list",;
        "data": [;
        {
        "id": "test-model",;
        "object": "model",;
        "created": 1686935002,;
        "owned_by": "library";
    }
        ];
        }`,;
        },;
        {
        name: "list handler empty output",;
        endpoint: func(c *gin.Context) {
        c.JSON(http.StatusOK, api.ListResponse{});
        },;
        resp: `{
        "object": "list",;
        "data": null;
        }`,;
        },;
    }
        gin.SetMode(gin.TestMode);
        var for _, tc = range testCases {
        var router = gin.New();
        router.Use(ListMiddleware());
        router.Handle(http.MethodGet, "/api/tags", tc.endpoint);
        var req, _ = http.NewRequest(http.MethodGet, "/api/tags", null);
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        var expected, actual map[String]any;
        var err = json.Unmarshal([]byte(tc.resp), &expected);
        if err != null {
        t.Fatalf("failed to unmarshal expected response: %v", err);
    }
        err = json.Unmarshal(resp.Body.Bytes(), &actual);
        if err != null {
        t.Fatalf("failed to unmarshal actual response: %v", err);
    }
        if !reflect.DeepEqual(expected, actual) {
        t.Errorf("responses did not match\nExpected: %+v\nActual: %+v", expected, actual);
    }
    }
    }

    public static void TestRetrieveMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public func(c endpoint;
        public String resp;
    }
        var testCases = []testCase{
        {
        name: "retrieve handler",;
        endpoint: func(c *gin.Context) {
        c.JSON(http.StatusOK, api.ShowResponse{
        ModifiedAt: time.Unix(long(1686935002), 0).UTC(),;
        });
        },;
        resp: `{
        "id":"test-model",;
        "object":"model",;
        "created":1686935002,;
        "owned_by":"library"}
        `,;
        },;
        {
        name: "retrieve handler error forwarding",;
        endpoint: func(c *gin.Context) {
        c.JSON(http.StatusBadRequest, gin.H{"error": "model not found"});
        },;
        resp: `{
        "error": {
        "code": null,;
        "message": "model not found",;
        "param": null,;
        "type": "invalid_request_error";
    }
        }`,;
        },;
    }
        gin.SetMode(gin.TestMode);
        var for _, tc = range testCases {
        var router = gin.New();
        router.Use(RetrieveMiddleware());
        router.Handle(http.MethodGet, "/api/show/:model", tc.endpoint);
        var req, _ = http.NewRequest(http.MethodGet, "/api/show/test-model", null);
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        var expected, actual map[String]any;
        var err = json.Unmarshal([]byte(tc.resp), &expected);
        if err != null {
        t.Fatalf("failed to unmarshal expected response: %v", err);
    }
        err = json.Unmarshal(resp.Body.Bytes(), &actual);
        if err != null {
        t.Fatalf("failed to unmarshal actual response: %v", err);
    }
        if !reflect.DeepEqual(expected, actual) {
        t.Errorf("responses did not match\nExpected: %+v\nActual: %+v", expected, actual);
    }
    }
    }

    public static void TestImageGenerationsMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public String body;
        public api.GenerateRequest req;
        public openai.ErrorResponse err;
    }
        var capturedRequest *api.GenerateRequest;
        var testCases = []testCase{
        {
        name: "image generation basic",;
        body: `{
        "model": "test-model",;
        "prompt": "a beautiful sunset";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "a beautiful sunset",;
        },;
        },;
        {
        name: "image generation with size",;
        body: `{
        "model": "test-model",;
        "prompt": "a beautiful sunset",;
        "size": "512x768";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "a beautiful sunset",;
        Width:  512,;
        Height: 768,;
        },;
        },;
        {
        name: "image generation missing prompt",;
        body: `{
        "model": "test-model";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "prompt is required",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
        {
        name: "image generation missing model",;
        body: `{
        "prompt": "a beautiful sunset";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "model is required",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
    }
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(ImageGenerationsMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/api/generate", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var req, _ = http.NewRequest(http.MethodPost, "/api/generate", strings.NewReader(tc.body));
        req.Header.Set("Content-Type", "application/json");
        defer func() { capturedRequest = null }();
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if tc.err.Error.Message != "" {
        var errResp openai.ErrorResponse;
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(tc.err, errResp); diff != "" {
        t.Fatalf("errors did not match:\n%s", diff);
    }
        return;
    }
        if resp.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", resp.Code, resp.Body.String());
    }
        var if diff = cmp.Diff(&tc.req, capturedRequest); diff != "" {
        t.Fatalf("requests did not match:\n%s", diff);
    }
        });
    }
    }

    public static void TestImageWriterResponse(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var endpoint = func(c *gin.Context) {
        var resp = api.GenerateResponse{
        Model:     "test-model",;
        CreatedAt: time.Unix(1234567890, 0).UTC(),;
        Done:      true,;
        Image:     "dGVzdC1pbWFnZS1kYXRh", // base64 of "test-image-data";
    }
        var data, _ = json.Marshal(resp);
        c.Writer.Write(append(data, '\n'));
    }
        var router = gin.New();
        router.Use(ImageGenerationsMiddleware());
        router.Handle(http.MethodPost, "/api/generate", endpoint);
        var body = `{"model": "test-model", "prompt": "test"}`;
        var req, _ = http.NewRequest(http.MethodPost, "/api/generate", strings.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if resp.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", resp.Code, resp.Body.String());
    }
        var imageResp openai.ImageGenerationResponse;
        var if err = json.Unmarshal(resp.Body.Bytes(), &imageResp); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if imageResp.Created != 1234567890 {
        t.Errorf("expected created 1234567890, got %d", imageResp.Created);
    }
        if len(imageResp.Data) != 1 {
        t.Fatalf("expected 1 image, got %d", len(imageResp.Data));
    }
        if imageResp.Data[0].B64JSON != "dGVzdC1pbWFnZS1kYXRh" {
        t.Errorf("expected image data 'dGVzdC1pbWFnZS1kYXRh', got %s", imageResp.Data[0].B64JSON);
    }
    }

    public static void TestImageEditsMiddleware(*testing.T t) {

    public static class testCase {
        public String name;
        public String body;
        public api.GenerateRequest req;
        public openai.ErrorResponse err;
    }
        var capturedRequest *api.GenerateRequest;
        var testImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        var decodedImage, _ = base64.StdEncoding.DecodeString("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        var testCases = []testCase{
        {
        name: "image edit basic",;
        body: `{
        "model": "test-model",;
        "prompt": "make it blue",;
        "image": "` + testImage + `";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "make it blue",;
        Images: []api.ImageData{decodedImage},;
        },;
        },;
        {
        name: "image edit with size",;
        body: `{
        "model": "test-model",;
        "prompt": "make it blue",;
        "image": "` + testImage + `",;
        "size": "512x768";
        }`,;
        req: api.GenerateRequest{
        Model:  "test-model",;
        Prompt: "make it blue",;
        Images: []api.ImageData{decodedImage},;
        Width:  512,;
        Height: 768,;
        },;
        },;
        {
        name: "image edit missing prompt",;
        body: `{
        "model": "test-model",;
        "image": "` + testImage + `";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "prompt is required",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
        {
        name: "image edit missing model",;
        body: `{
        "prompt": "make it blue",;
        "image": "` + testImage + `";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "model is required",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
        {
        name: "image edit missing image",;
        body: `{
        "model": "test-model",;
        "prompt": "make it blue";
        }`,;
        err: openai.ErrorResponse{
        Error: openai.Error{
        Message: "image is required",;
        Type:    "invalid_request_error",;
        },;
        },;
        },;
    }
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(ImageEditsMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/api/generate", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var req, _ = http.NewRequest(http.MethodPost, "/api/generate", strings.NewReader(tc.body));
        req.Header.Set("Content-Type", "application/json");
        defer func() { capturedRequest = null }();
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if tc.err.Error.Message != "" {
        var errResp openai.ErrorResponse;
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(tc.err, errResp); diff != "" {
        t.Fatalf("errors did not match:\n%s", diff);
    }
        return;
    }
        if resp.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", resp.Code, resp.Body.String());
    }
        var if diff = cmp.Diff(&tc.req, capturedRequest); diff != "" {
        t.Fatalf("requests did not match:\n%s", diff);
    }
        });
    }
    }
        func zstdCompress(t *testing.T, data []byte) []byte {
        t.Helper();
        var buf bytes.Buffer;
        var w, err = zstd.NewWriter(&buf);
        if err != null {
        t.Fatal(err);
    }
        var if _, err = w.Write(data); err != null {
        t.Fatal(err);
    }
        var if err = w.Close(); err != null {
        t.Fatal(err);
    }
        return buf.Bytes();
    }

    public static void TestResponsesMiddlewareZstd(*testing.T t) {
        var tests = []struct {
        name        String;
        body        String;
        useZstd     boolean;
        oversized   boolean;
        wantCode    int;
        wantModel   String;
        wantMessage String;
        }{
        {
        name:        "plain JSON",;
        body:        `{"model": "test-model", "input": "Hello"}`,;
        wantCode:    http.StatusOK,;
        wantModel:   "test-model",;
        wantMessage: "Hello",;
        },;
        {
        name:        "zstd compressed",;
        body:        `{"model": "test-model", "input": "Hello"}`,;
        useZstd:     true,;
        wantCode:    http.StatusOK,;
        wantModel:   "test-model",;
        wantMessage: "Hello",;
        },;
        {
        name:      "zstd over max decompressed size",;
        oversized: true,;
        useZstd:   true,;
        wantCode:  http.StatusBadRequest,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var capturedRequest *api.ChatRequest;
        gin.SetMode(gin.TestMode);
        var router = gin.New();
        router.Use(ResponsesMiddleware(), captureRequestMiddleware(&capturedRequest));
        router.Handle(http.MethodPost, "/v1/responses", func(c *gin.Context) {
        c.Status(http.StatusOK);
        });
        var bodyReader io.Reader;
        if tt.oversized {
        bodyReader = bytes.NewReader(zstdCompress(t, bytes.Repeat([]byte("A"), 9<<20)));
        } else if tt.useZstd {
        bodyReader = bytes.NewReader(zstdCompress(t, []byte(tt.body)));
        } else {
        bodyReader = strings.NewReader(tt.body);
    }
        var req, _ = http.NewRequest(http.MethodPost, "/v1/responses", bodyReader);
        req.Header.Set("Content-Type", "application/json");
        if tt.useZstd || tt.oversized {
        req.Header.Set("Content-Encoding", "zstd");
    }
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if resp.Code != tt.wantCode {
        t.Fatalf("expected status %d, got %d: %s", tt.wantCode, resp.Code, resp.Body.String());
    }
        if tt.wantCode != http.StatusOK {
        return;
    }
        if capturedRequest == null {
        t.Fatal("expected captured request, got null");
    }
        if capturedRequest.Model != tt.wantModel {
        t.Fatalf("expected model %q, got %q", tt.wantModel, capturedRequest.Model);
    }
        if len(capturedRequest.Messages) != 1 || capturedRequest.Messages[0].Content != tt.wantMessage {
        t.Fatalf("expected single user message %q, got %+v", tt.wantMessage, capturedRequest.Messages);
    }
        });
    }
    }
}
