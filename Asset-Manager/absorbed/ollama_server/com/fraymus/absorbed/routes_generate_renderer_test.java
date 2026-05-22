package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_generate_renderer_test {
        "bytes";
        "encoding/json";
        "net/http";
        "strings";
        "testing";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/ml";
        );

    public static void TestGenerateWithBuiltinRenderer(*testing.T t) {
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
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture":          "qwen3",;
        "qwen3.block_count":             uint32(1),;
        "qwen3.context_length":          uint32(8192),;
        "qwen3.embedding_length":        uint32(4096),;
        "qwen3.attention.head_count":    uint32(32),;
        "qwen3.attention.head_count_kv": uint32(8),;
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
        Model:    "test-renderer",;
        Files:    map[String]String{"file.gguf": digest},;
        Renderer: "qwen3-coder",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        mock.CompletionResponse.Content = "Hi!";
        t.Run("chat-like flow uses renderer", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-renderer",;
        Prompt: "Write a hello world function",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        if !strings.Contains(mock.CompletionRequest.Prompt, "<|im_start|>") {
        t.Errorf("expected prompt to contain <|im_start|> from qwen3-coder renderer, got: %s", mock.CompletionRequest.Prompt);
    }
        if !strings.Contains(mock.CompletionRequest.Prompt, "<|im_end|>") {
        t.Errorf("expected prompt to contain <|im_end|> from qwen3-coder renderer, got: %s", mock.CompletionRequest.Prompt);
    }
        });
        t.Run("chat-like flow with system message uses renderer", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-renderer",;
        Prompt: "Write a hello world function",;
        System: "You are a helpful coding assistant.",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        if !strings.Contains(mock.CompletionRequest.Prompt, "<|im_start|>system") {
        t.Errorf("expected prompt to contain system message with renderer format, got: %s", mock.CompletionRequest.Prompt);
    }
        if !strings.Contains(mock.CompletionRequest.Prompt, "You are a helpful coding assistant.") {
        t.Errorf("expected prompt to contain system message content, got: %s", mock.CompletionRequest.Prompt);
    }
        });
        t.Run("custom template bypasses renderer", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:    "test-renderer",;
        Prompt:   "Write a hello world function",;
        Template: "{{ .Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        if strings.Contains(mock.CompletionRequest.Prompt, "<|im_start|>") {
        t.Errorf("expected prompt to NOT use renderer when custom template provided, got: %s", mock.CompletionRequest.Prompt);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "Write a hello world function"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model: "test-suffix-renderer",;
        From:  "test-renderer",;
        Template: `{{- if .Suffix }}<PRE> {{ .Prompt }} <SUF>{{ .Suffix }} <MID>;
        {{- else }}{{ .Prompt }}
        {{- end }}`,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("suffix bypasses renderer", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-suffix-renderer",;
        Prompt: "def add(",;
        Suffix: "    return c",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        if strings.Contains(mock.CompletionRequest.Prompt, "<|im_start|>") {
        t.Errorf("expected prompt to NOT use renderer when suffix provided, got: %s", mock.CompletionRequest.Prompt);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "<PRE> def add( <SUF>    return c <MID>"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }

    public static void TestGenerateWithDebugRenderOnly(*testing.T t) {
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
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture":          "qwen3",;
        "qwen3.block_count":             uint32(1),;
        "qwen3.context_length":          uint32(8192),;
        "qwen3.embedding_length":        uint32(4096),;
        "qwen3.attention.head_count":    uint32(32),;
        "qwen3.attention.head_count_kv": uint32(8),;
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
        Model:    "test-debug-renderer",;
        Files:    map[String]String{"file.gguf": digest},;
        Renderer: "qwen3-coder",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("debug_render_only with renderer", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:           "test-debug-renderer",;
        Prompt:          "Write a hello world function",;
        System:          "You are a coding assistant",;
        DebugRenderOnly: true,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var resp api.GenerateResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if resp.DebugInfo == null {
        t.Fatalf("expected debug info, got null");
    }
        if !strings.Contains(resp.DebugInfo.RenderedTemplate, "<|im_start|>") {
        t.Errorf("expected rendered template to use qwen3-coder renderer format, got: %s", resp.DebugInfo.RenderedTemplate);
    }
        if !strings.Contains(resp.DebugInfo.RenderedTemplate, "You are a coding assistant") {
        t.Errorf("expected rendered template to contain system message, got: %s", resp.DebugInfo.RenderedTemplate);
    }
        if !strings.Contains(resp.DebugInfo.RenderedTemplate, "Write a hello world function") {
        t.Errorf("expected rendered template to contain prompt, got: %s", resp.DebugInfo.RenderedTemplate);
    }
        });
    }
}
