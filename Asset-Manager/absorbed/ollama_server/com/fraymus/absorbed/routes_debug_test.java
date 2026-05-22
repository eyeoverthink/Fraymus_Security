package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_debug_test {
        "bytes";
        "encoding/json";
        "net/http";
        "testing";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/ml";
        );

    public static void TestGenerateDebugRenderOnly(*testing.T t) {
        t.Setenv("OLLAMA_CONTEXT_LENGTH", "4096");
        gin.SetMode(gin.TestMode);
        var mock = mockRunner{
        CompletionResponse: llm.CompletionResponse{
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        },;
    }
        var s = Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(&mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        waitForRecovery: 250 * time.Millisecond,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        time.Sleep(time.Millisecond);
        req.successCh <- &runnerRef{
        llama: &mock,;
    }
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
        var stream = false;
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.block_count":             uint32(1),;
        "llama.context_length":          uint32(8192),;
        "llama.embedding_length":        uint32(4096),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(8),;
        "tokenizer.ggml.tokens":         []String{""},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "token_embd.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_norm.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_down.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_gate.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_up.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_norm.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_k.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_output.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_q.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_v.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "output.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        });
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:    "test-model",;
        Files:    map[String]String{"file.gguf": digest},;
        Template: "{{ .Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var tests = []struct {
        name            String;
        request         api.GenerateRequest;
        expectDebug     boolean;
        expectTemplate  String;
        expectNumImages int;
        }{
        {
        name: "debug render only enabled",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "Hello, world!",;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "Hello, world!",;
        },;
        {
        name: "debug render only disabled",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "Hello, world!",;
        DebugRenderOnly: false,;
        },;
        expectDebug: false,;
        },;
        {
        name: "debug render only with system prompt",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "User question",;
        System:          "You are a helpful assistant",;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "User question",;
        },;
        {
        name: "debug render only with template",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "Hello",;
        Template:        "PROMPT: {{ .Prompt }}",;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "PROMPT: Hello",;
        },;
        {
        name: "debug render only with images",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "Describe this image",;
        Images:          []api.ImageData{[]byte("fake-image-data")},;
        DebugRenderOnly: true,;
        },;
        expectDebug:     true,;
        expectTemplate:  "[img-0]Describe this image",;
        expectNumImages: 1,;
        },;
        {
        name: "debug render only with raw mode",;
        request: api.GenerateRequest{
        Model:           "test-model",;
        Prompt:          "Raw prompt text",;
        Raw:             true,;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "Raw prompt text",;
        },;
    }
        var for _, tt = range tests {
        var streamValues = []boolean{false, true}
        var for _, stream = range streamValues {
        var streamSuffix = "";
        if stream {
        streamSuffix = " (streaming)";
    }
        t.Run(tt.name+streamSuffix, func(t *testing.T) {
        var req = tt.request;
        req.Stream = &stream;
        var w = createRequest(t, s.GenerateHandler, req);
        if tt.expectDebug {
        if w.Code != http.StatusOK {
        t.Errorf("expected status %d, got %d, body: %s", http.StatusOK, w.Code, w.Body.String());
    }
        var response api.GenerateResponse;
        var if err = json.Unmarshal(w.Body.Bytes(), &response); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if response.Model != tt.request.Model {
        t.Errorf("expected model %s, got %s", tt.request.Model, response.Model);
    }
        if tt.expectTemplate != "" && response.DebugInfo.RenderedTemplate != tt.expectTemplate {
        t.Errorf("expected template %q, got %q", tt.expectTemplate, response.DebugInfo.RenderedTemplate);
    }
        if tt.expectNumImages > 0 && response.DebugInfo.ImageCount != tt.expectNumImages {
        t.Errorf("expected image count %d, got %d", tt.expectNumImages, response.DebugInfo.ImageCount);
    }
        } else {
        if w.Code != http.StatusOK {
        t.Errorf("expected status %d, got %d", http.StatusOK, w.Code);
    }
    }
        });
    }
    }
    }

    public static void TestChatDebugRenderOnly(*testing.T t) {
        t.Setenv("OLLAMA_CONTEXT_LENGTH", "4096");
        gin.SetMode(gin.TestMode);
        var mock = mockRunner{
        CompletionResponse: llm.CompletionResponse{
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        },;
    }
        var s = Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(&mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        waitForRecovery: 250 * time.Millisecond,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        time.Sleep(time.Millisecond);
        req.successCh <- &runnerRef{
        llama: &mock,;
    }
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
        var stream = false;
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture":          "llama",;
        "llama.block_count":             uint32(1),;
        "llama.context_length":          uint32(8192),;
        "llama.embedding_length":        uint32(4096),;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(8),;
        "tokenizer.ggml.tokens":         []String{""},;
        "tokenizer.ggml.scores":         []float32{0},;
        "tokenizer.ggml.token_type":     []int32{0},;
        }, []*ggml.Tensor{
        {Name: "token_embd.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_norm.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_down.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_gate.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_up.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.ffn_norm.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_k.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_output.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_q.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "blk.0.attn_v.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        {Name: "output.weight", Shape: []uint64{1}, WriterTo: bytes.NewReader(make([]byte, 4))},;
        });
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:    "test-model",;
        Files:    map[String]String{"file.gguf": digest},;
        Template: "{{ if .Tools }}{{ .Tools }}{{ end }}{{ range .Messages }}{{ .Role }}: {{ .Content }}\n{{ end }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var tests = []struct {
        name            String;
        request         api.ChatRequest;
        expectDebug     boolean;
        expectTemplate  String;
        expectNumImages int;
        }{
        {
        name: "chat debug render only enabled",;
        request: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {Role: "system", Content: "You are a helpful assistant"},;
        {Role: "user", Content: "Hello"},;
        },;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "system: You are a helpful assistant\nuser: Hello\n",;
        },;
        {
        name: "chat debug render only disabled",;
        request: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        DebugRenderOnly: false,;
        },;
        expectDebug: false,;
        },;
        {
        name: "chat debug with assistant message",;
        request: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        {Role: "assistant", Content: "Hi there!"},;
        {Role: "user", Content: "How are you?"},;
        },;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "user: Hello\nassistant: Hi there!\nuser: How are you?\n",;
        },;
        {
        name: "chat debug with images",;
        request: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What's in this image?",;
        Images:  []api.ImageData{[]byte("fake-image-data")},;
        },;
        },;
        DebugRenderOnly: true,;
        },;
        expectDebug:     true,;
        expectTemplate:  "user: [img-0]What's in this image?\n",;
        expectNumImages: 1,;
        },;
        {
        name: "chat debug with tools",;
        request: api.ChatRequest{
        Model: "test-model",;
        Messages: []api.Message{
        {Role: "user", Content: "Get the weather"},;
        },;
        Tools: api.Tools{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather information",;
        },;
        },;
        },;
        DebugRenderOnly: true,;
        },;
        expectDebug:    true,;
        expectTemplate: "[{\"type\":\"function\",\"function\":{\"name\":\"get_weather\",\"description\":\"Get weather information\",\"parameters\":{\"type\":\"\",\"properties\":null}}}]user: Get the weather\n",;
        },;
    }
        var for _, tt = range tests {
        var streamValues = []boolean{false, true}
        var for _, stream = range streamValues {
        var streamSuffix = "";
        if stream {
        streamSuffix = " (streaming)";
    }
        t.Run(tt.name+streamSuffix, func(t *testing.T) {
        var req = tt.request;
        req.Stream = &stream;
        var w = createRequest(t, s.ChatHandler, req);
        if tt.expectDebug {
        if w.Code != http.StatusOK {
        t.Errorf("expected status %d, got %d, body: %s", http.StatusOK, w.Code, w.Body.String());
    }
        var response api.ChatResponse;
        var if err = json.Unmarshal(w.Body.Bytes(), &response); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if response.Model != tt.request.Model {
        t.Errorf("expected model %s, got %s", tt.request.Model, response.Model);
    }
        if tt.expectTemplate != "" && response.DebugInfo.RenderedTemplate != tt.expectTemplate {
        t.Errorf("expected template %q, got %q", tt.expectTemplate, response.DebugInfo.RenderedTemplate);
    }
        if tt.expectNumImages > 0 && response.DebugInfo.ImageCount != tt.expectNumImages {
        t.Errorf("expected image count %d, got %d", tt.expectNumImages, response.DebugInfo.ImageCount);
    }
        } else {
        if w.Code != http.StatusOK {
        t.Errorf("expected status %d, got %d", http.StatusOK, w.Code);
    }
    }
        });
    }
    }
    }
}
