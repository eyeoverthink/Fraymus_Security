package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_generate_test {
        "bytes";
        "context";
        "encoding/json";
        "io";
        "net/http";
        "net/http/httptest";
        "net/url";
        "strings";
        "sync";
        "testing";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/ml";
        "github.com/ollama/ollama/types/model";
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

    public static class mockRunner {
        public func(context.Context, CompletionFn;
    }
        func (m *mockRunner) Completion(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        m.CompletionRequest = r;
        if m.CompletionFn != null {
        return m.CompletionFn(ctx, r, fn);
    }
        fn(m.CompletionResponse);
        return null;
    }
        func (mockRunner) Tokenize(_ context.Context, s String) (tokens []int, err error) {
        for range strings.Fields(s) {
        tokens = append(tokens, len(tokens));
    }
        return;
    }
        func (mockRunner) Ping(_ context.Context) error { return null }

    public static void newMockServer((llm.LlamaServer int)) {
        return func(_ ml.SystemInfo, _ []ml.DeviceInfo, _ String, _ *ggml.GGML, _, _ []String, _ api.Options, _ int) (llm.LlamaServer, error) {
        return mock, null;
    }
    }

    public static void TestGenerateChatRemote(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var rs = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("Expected POST request, got %s", r.Method);
    }
        if r.URL.Path != "/api/chat" {
        t.Errorf("Expected path '/api/chat', got %s", r.URL.Path);
    }
        w.WriteHeader(http.StatusOK);
        w.Header().Set("Content-Type", "application/json");
        var resp = api.ChatResponse{
        Model:      "test",;
        Done:       true,;
        DoneReason: "load",;
    }
        var if err = json.NewEncoder(w).Encode(&resp); err != null {
        t.Fatal(err);
    }
        }));
        defer rs.Close();
        var p, err = url.Parse(rs.URL);
        if err != null {
        t.Fatal(err);
    }
        t.Setenv("OLLAMA_REMOTES", p.Hostname());
        var s = Server{}
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:      "test-cloud",;
        RemoteHost: rs.URL,;
        From:       "test",;
        Info: map[String]any{
        "capabilities": []String{"completion", "thinking"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("missing messages", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-cloud",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var actual api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&actual); err != null {
        t.Fatal(err);
    }
        if actual.Model != "test-cloud" {
        t.Errorf("expected model test-cloud, got %s", actual.Model);
    }
        if actual.RemoteModel != "test" {
        t.Errorf("expected remote model test, got %s", actual.RemoteModel);
    }
        if actual.RemoteHost != rs.URL {
        t.Errorf("expected remote host '%s', got %s", rs.URL, actual.RemoteHost);
    }
        if !actual.Done {
        t.Errorf("expected done true, got false");
    }
        if actual.DoneReason != "load" {
        t.Errorf("expected done reason load, got %s", actual.DoneReason);
    }
        });
    }

    public static void TestGenerateChat(*testing.T t) {
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
        Model: "test",;
        Files: map[String]String{"file.gguf": digest},;
        Template: `;
        {{- if .Tools }}
        {{ .Tools }}
        {{ end }}
        {{- range .Messages }}
        {{- .Role }}: {{ .Content }}
        {{- range .ToolCalls }}{"name": "{{ .Function.Name }}", "arguments": {{ .Function.Arguments }}}
        {{- end }}
        {{ end }}`,;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("missing body", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, null);
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model is required"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("missing thinking capability", func(t *testing.T) {
        var think = true;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        Think: &api.ThinkValue{Value: think},;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"\"test\" does not support thinking"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("model can't think but think set false", func(t *testing.T) {
        var think = false;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        Think: &api.ThinkValue{Value: think},;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        });
        t.Run("missing model", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{});
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model is required"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("missing capabilities chat", func(t *testing.T) {
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture": "bert",;
        "bert.pooling_type":    uint32(0),;
        }, []*ggml.Tensor{});
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "bert",;
        Files:  map[String]String{"bert.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "bert",;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"\"bert\" does not support chat"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("load model", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var actual api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&actual); err != null {
        t.Fatal(err);
    }
        if actual.Model != "test" {
        t.Errorf("expected model test, got %s", actual.Model);
    }
        if !actual.Done {
        t.Errorf("expected done true, got false");
    }
        if actual.DoneReason != "load" {
        t.Errorf("expected done reason load, got %s", actual.DoneReason);
    }
        });
        var checkChatResponse = func(t *testing.T, body io.Reader, model, content String) {
        t.Helper();
        var actual api.ChatResponse;
        var if err = json.NewDecoder(body).Decode(&actual); err != null {
        t.Fatal(err);
    }
        if actual.Model != model {
        t.Errorf("expected model test, got %s", actual.Model);
    }
        if !actual.Done {
        t.Errorf("expected done false, got true");
    }
        if actual.DoneReason != "stop" {
        t.Errorf("expected done reason stop, got %s", actual.DoneReason);
    }
        var if diff = cmp.Diff(actual.Message, api.Message{
        Role:    "assistant",;
        Content: content,;
        }); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        if actual.PromptEvalCount == 0 {
        t.Errorf("expected prompt eval count > 0, got 0");
    }
        if actual.PromptEvalDuration == 0 {
        t.Errorf("expected prompt eval duration > 0, got 0");
    }
        if actual.EvalCount == 0 {
        t.Errorf("expected eval count > 0, got 0");
    }
        if actual.EvalDuration == 0 {
        t.Errorf("expected eval duration > 0, got 0");
    }
        if actual.LoadDuration == 0 {
        t.Errorf("expected load duration > 0, got 0");
    }
        if actual.TotalDuration == 0 {
        t.Errorf("expected total duration > 0, got 0");
    }
    }
        mock.CompletionResponse.Content = "Hi!";
        t.Run("messages", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "user: Hello!\n"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkChatResponse(t, w.Body, "test", "Hi!");
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "test-system",;
        From:   "test",;
        System: "You are a helpful assistant.",;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("messages with model system", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "system: You are a helpful assistant.\nuser: Hello!\n"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkChatResponse(t, w.Body, "test-system", "Hi!");
        });
        mock.CompletionResponse.Content = "Abra kadabra!";
        t.Run("messages with system", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "system", Content: "You can perform magic tricks."},;
        {Role: "user", Content: "Hello!"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "system: You can perform magic tricks.\nuser: Hello!\n"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkChatResponse(t, w.Body, "test-system", "Abra kadabra!");
        });
        t.Run("messages with interleaved system", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        {Role: "assistant", Content: "I can help you with that."},;
        {Role: "system", Content: "You can perform magic tricks."},;
        {Role: "user", Content: "Help me write tests."},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "system: You are a helpful assistant.\nuser: Hello!\nassistant: I can help you with that.\nsystem: You can perform magic tricks.\nuser: Help me write tests.\n"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkChatResponse(t, w.Body, "test-system", "Abra kadabra!");
        });
        t.Run("messages with tools (non-streaming)", func(t *testing.T) {
        if w.Code != http.StatusOK {
        t.Fatalf("failed to create test-system model: %d", w.Code);
    }
        var tools = []api.Tool{
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
    }
        mock.CompletionResponse = llm.CompletionResponse{
        Content:            `{"name":"get_weather","arguments":{"location":"Seattle, WA","unit":"celsius"}}`,;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
    }
        var streamRequest = true;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "user", Content: "What's the weather in Seattle?"},;
        },;
        Tools:  tools,;
        Stream: &streamRequest,;
        });
        if w.Code != http.StatusOK {
        var errResp struct {
        Error String `json:"error"`;
    }
        var if err = json.NewDecoder(w.Body).Decode(&errResp); err != null {
        t.Logf("Failed to decode error response: %v", err);
        } else {
        t.Logf("Error response: %s", errResp.Error);
    }
    }
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var resp api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if resp.Message.ToolCalls == null {
        t.Error("expected tool calls, got null");
    }
        var gotToolCall = resp.Message.ToolCalls[0];
        if gotToolCall.ID == "" {
        t.Error("expected tool call ID to be populated");
    }
        if !strings.HasPrefix(gotToolCall.ID, "call_") {
        t.Errorf("expected tool call ID to have call_ prefix, got %q", gotToolCall.ID);
    }
        var expectedToolCall = api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Seattle, WA",;
        "unit":     "celsius",;
        }),;
        },;
    }
        expectedToolCall.ID = gotToolCall.ID;
        var if diff = cmp.Diff(gotToolCall, expectedToolCall, argsComparer); diff != "" {
        t.Errorf("tool call mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("messages with tools (streaming)", func(t *testing.T) {
        var tools = []api.Tool{
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
    }
        var wg sync.WaitGroup;
        wg.Add(1);
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        defer wg.Done();
        var responses = []llm.CompletionResponse{
        {
        Content:            `{"name":"get_`,;
        Done:               false,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        },;
        {
        Content:            `weather","arguments":{"location":"Seattle`,;
        Done:               false,;
        PromptEvalCount:    2,;
        PromptEvalDuration: 1,;
        },;
        {
        Content:            `, WA","unit":"celsius"}}`,;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    3,;
        PromptEvalDuration: 1,;
        },;
    }
        var for _, resp = range responses {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        default:;
        fn(resp);
        time.Sleep(10 * time.Millisecond) // Small delay between chunks;
    }
    }
        return null;
    }
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "user", Content: "What's the weather in Seattle?"},;
        },;
        Tools:  tools,;
        Stream: &stream,;
        });
        wg.Wait();
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var decoder = json.NewDecoder(w.Body);
        var finalToolCall api.ToolCall;
        for {
        var resp api.ChatResponse;
        var if err = decoder.Decode(&resp); err == io.EOF {
        break;
        } else if err != null {
        t.Fatal(err);
    }
        if len(resp.Message.ToolCalls) > 0 {
        var for _, call = range resp.Message.ToolCalls {
        if call.ID == "" {
        t.Fatal("expected streaming tool call to have an ID");
    }
        if !strings.HasPrefix(call.ID, "call_") {
        t.Fatalf("expected streaming tool call ID to have call_ prefix, got %q", call.ID);
    }
    }
    }
        if resp.Done {
        if len(resp.Message.ToolCalls) != 1 {
        t.Errorf("expected 1 tool call in final response, got %d", len(resp.Message.ToolCalls));
    }
        finalToolCall = resp.Message.ToolCalls[0];
    }
    }
        var expectedToolCall = api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Seattle, WA",;
        "unit":     "celsius",;
        }),;
        },;
    }
        if finalToolCall.ID == "" {
        t.Fatal("expected final tool call to have an ID");
    }
        if !strings.HasPrefix(finalToolCall.ID, "call_") {
        t.Fatalf("expected final tool call ID to have call_ prefix, got %q", finalToolCall.ID);
    }
        expectedToolCall.ID = finalToolCall.ID;
        var if diff = cmp.Diff(finalToolCall, expectedToolCall, argsComparer); diff != "" {
        t.Errorf("final tool call mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("messages with tools and logprobs (streaming)", func(t *testing.T) {
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {Type: api.PropertyType{"String"}},;
        }),;
        },;
        },;
        },;
    }
        var wg sync.WaitGroup;
        wg.Add(1);
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        defer wg.Done();
        var responses = []llm.CompletionResponse{
        {
        Content:  `{ "name": "get_weather"`,;
        Done:     false,;
        Logprobs: []llm.Logprob{{}},;
        },;
        {
        Content:  `,"arguments":{"location":"Seattle, WA","unit":"celsius"}}`,;
        Done:     false,;
        Logprobs: []llm.Logprob{{}},;
        },;
        {
        Content:    ``,;
        Done:       true,;
        DoneReason: llm.DoneReasonStop,;
        Logprobs:   null,;
        },;
    }
        var for _, resp = range responses {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        default:;
        fn(resp);
        time.Sleep(10 * time.Millisecond);
    }
    }
        return null;
    }
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-system",;
        Messages: []api.Message{
        {Role: "user", Content: "Weather?"},;
        },;
        Tools:  tools,;
        Stream: &stream,;
        });
        wg.Wait();
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var decoder = json.NewDecoder(w.Body);
        var totalLogprobs int;
        for {
        var resp api.ChatResponse;
        var if err = decoder.Decode(&resp); err == io.EOF {
        break;
        } else if err != null {
        t.Fatal(err);
    }
        totalLogprobs += len(resp.Logprobs);
    }
        var expectedLogprobs = 2;
        if totalLogprobs != expectedLogprobs {
        t.Errorf("expected %d logprobs, got %d", expectedLogprobs, totalLogprobs);
    }
        });
        t.Run("status error non-streaming", func(t *testing.T) {
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        return api.StatusError{
        StatusCode:   http.StatusServiceUnavailable,;
        Status:       "Service Unavailable",;
        ErrorMessage: "model is overloaded",;
    }
    }
        var stream = false;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusServiceUnavailable {
        t.Errorf("expected status 503, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model is overloaded"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("status error streaming", func(t *testing.T) {
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        return api.StatusError{
        StatusCode:   http.StatusTooManyRequests,;
        Status:       "Too Many Requests",;
        ErrorMessage: "rate limit exceeded",;
    }
    }
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello!"},;
        },;
        });
        if w.Code != http.StatusTooManyRequests {
        t.Errorf("expected status 429, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"rate limit exceeded"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }

    public static void TestGenerate(*testing.T t) {
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
        Model: "test",;
        Files: map[String]String{"file.gguf": digest},;
        Template: `;
        {{- if .System }}System: {{ .System }} {{ end }}
        {{- if .Prompt }}User: {{ .Prompt }} {{ end }}
        {{- if .Response }}Assistant: {{ .Response }} {{ end }}
        `,;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("missing body", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, null);
        if w.Code != http.StatusNotFound {
        t.Errorf("expected status 404, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model '' not found"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("missing model", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{});
        if w.Code != http.StatusNotFound {
        t.Errorf("expected status 404, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model '' not found"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("missing capabilities generate", func(t *testing.T) {
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture": "bert",;
        "bert.pooling_type":    uint32(0),;
        }, []*ggml.Tensor{});
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "bert",;
        Files:  map[String]String{"file.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model: "bert",;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"\"bert\" does not support generate"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("missing capabilities suffix", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "def add(",;
        Suffix: "    return c",;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"registry.ollama.ai/library/test:latest does not support insert"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("load model", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model: "test",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var actual api.GenerateResponse;
        var if err = json.NewDecoder(w.Body).Decode(&actual); err != null {
        t.Fatal(err);
    }
        if actual.Model != "test" {
        t.Errorf("expected model test, got %s", actual.Model);
    }
        if !actual.Done {
        t.Errorf("expected done true, got false");
    }
        if actual.DoneReason != "load" {
        t.Errorf("expected done reason load, got %s", actual.DoneReason);
    }
        });
        var checkGenerateResponse = func(t *testing.T, body io.Reader, model, content String) {
        t.Helper();
        var actual api.GenerateResponse;
        var if err = json.NewDecoder(body).Decode(&actual); err != null {
        t.Fatal(err);
    }
        if actual.Model != model {
        t.Errorf("expected model test, got %s", actual.Model);
    }
        if !actual.Done {
        t.Errorf("expected done false, got true");
    }
        if actual.DoneReason != "stop" {
        t.Errorf("expected done reason stop, got %s", actual.DoneReason);
    }
        if actual.Response != content {
        t.Errorf("expected response %s, got %s", content, actual.Response);
    }
        if actual.Context == null {
        t.Errorf("expected context not null");
    }
        if actual.PromptEvalCount == 0 {
        t.Errorf("expected prompt eval count > 0, got 0");
    }
        if actual.PromptEvalDuration == 0 {
        t.Errorf("expected prompt eval duration > 0, got 0");
    }
        if actual.EvalCount == 0 {
        t.Errorf("expected eval count > 0, got 0");
    }
        if actual.EvalDuration == 0 {
        t.Errorf("expected eval duration > 0, got 0");
    }
        if actual.LoadDuration == 0 {
        t.Errorf("expected load duration > 0, got 0");
    }
        if actual.TotalDuration == 0 {
        t.Errorf("expected total duration > 0, got 0");
    }
    }
        mock.CompletionResponse.Content = "Hi!";
        t.Run("prompt", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Hello!",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "User: Hello! "); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkGenerateResponse(t, w.Body, "test", "Hi!");
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "test-system",;
        From:   "test",;
        System: "You are a helpful assistant.",;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("prompt with model system", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-system",;
        Prompt: "Hello!",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "System: You are a helpful assistant. User: Hello! "); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkGenerateResponse(t, w.Body, "test-system", "Hi!");
        });
        mock.CompletionResponse.Content = "Abra kadabra!";
        t.Run("prompt with system", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-system",;
        Prompt: "Hello!",;
        System: "You can perform magic tricks.",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "System: You can perform magic tricks. User: Hello! "); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkGenerateResponse(t, w.Body, "test-system", "Abra kadabra!");
        });
        t.Run("prompt with template", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-system",;
        Prompt: "Help me write tests.",;
        System: "You can perform magic tricks.",;
        Template: `{{- if .System }}{{ .System }} {{ end }}
        {{- if .Prompt }}### USER {{ .Prompt }} {{ end }}
        {{- if .Response }}### ASSISTANT {{ .Response }} {{ end }}`,;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "You can perform magic tricks. ### USER Help me write tests. "); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        checkGenerateResponse(t, w.Body, "test-system", "Abra kadabra!");
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model: "test-suffix",;
        Template: `{{- if .Suffix }}<PRE> {{ .Prompt }} <SUF>{{ .Suffix }} <MID>;
        {{- else }}{{ .Prompt }}
        {{- end }}`,;
        From: "test",;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("prompt with suffix", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-suffix",;
        Prompt: "def add(",;
        Suffix: "    return c",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "<PRE> def add( <SUF>    return c <MID>"); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("prompt without suffix", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-suffix",;
        Prompt: "def add(",;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "def add("); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("raw", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-system",;
        Prompt: "Help me write tests.",;
        Raw:    true,;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var if diff = cmp.Diff(mock.CompletionRequest.Prompt, "Help me write tests."); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("status error non-streaming", func(t *testing.T) {
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        return api.StatusError{
        StatusCode:   http.StatusServiceUnavailable,;
        Status:       "Service Unavailable",;
        ErrorMessage: "model is overloaded",;
    }
    }
        var streamRequest = false;
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Hello!",;
        Stream: &streamRequest,;
        });
        if w.Code != http.StatusServiceUnavailable {
        t.Errorf("expected status 503, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"model is overloaded"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("status error streaming", func(t *testing.T) {
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        return api.StatusError{
        StatusCode:   http.StatusTooManyRequests,;
        Status:       "Too Many Requests",;
        ErrorMessage: "rate limit exceeded",;
    }
    }
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Hello!",;
        Stream: &stream,;
        });
        if w.Code != http.StatusTooManyRequests {
        t.Errorf("expected status 429, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"rate limit exceeded"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }

    public static void TestGenerateLogprobs(*testing.T t) {
        t.Run("invalid top_logprobs negative", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var s = Server{}
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:       "test",;
        Prompt:      "Hello",;
        TopLogprobs: -1,;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"top_logprobs must be between 0 and 20"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("invalid top_logprobs too high", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var s = Server{}
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:       "test",;
        Prompt:      "Hello",;
        TopLogprobs: 21,;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"top_logprobs must be between 0 and 20"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("returns logprob bytes when requested", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var mock = &mockRunner{}
        var expectedPrimary = llm.TokenLogprob{
        Token:   "Hi",;
        Logprob: -0.01,;
    }
        var expectedAlternatives = []llm.TokenLogprob{
        {
        Token:   "Hello",;
        Logprob: -0.25,;
        },;
        {
        Token:   "Hey",;
        Logprob: -0.5,;
        },;
    }
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(llm.CompletionResponse)) error {
        fn(llm.CompletionResponse{
        Content:            "Hi",;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        Logprobs: []llm.Logprob{
        {
        TokenLogprob: expectedPrimary,;
        TopLogprobs:  expectedAlternatives,;
        },;
        },;
        });
        return null;
    }
        var s = &Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        waitForRecovery: 250 * time.Millisecond,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        req.successCh <- &runnerRef{llama: mock}
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
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
        var if w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:    "test-logprob-bytes",;
        Files:    map[String]String{"file.gguf": digest},;
        Template: `{{ .Prompt }}`,;
        Stream:   &stream,;
        }); w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var stream = false;
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:       "test-logprob-bytes",;
        Prompt:      "Hi",;
        Stream:      &stream,;
        Logprobs:    true,;
        TopLogprobs: len(expectedAlternatives),;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var resp api.GenerateResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatalf("failed to decode response: %v", err);
    }
        if len(resp.Logprobs) != 1 {
        t.Fatalf("expected 1 logprob entry, got %d", len(resp.Logprobs));
    }
        var expectedPrimaryBytes = stringToByteInts(expectedPrimary.Token);
        var expectedAlternativesBytes = make([][]int, len(expectedAlternatives));
        var for i, alternative = range expectedAlternatives {
        expectedAlternativesBytes[i] = stringToByteInts(alternative.Token);
    }
        var if diff = cmp.Diff(expectedPrimaryBytes, resp.Logprobs[0].Bytes); diff != "" {
        t.Fatalf("primary token bytes mismatch (-want +got):\n%s", diff);
    }
        if len(resp.Logprobs[0].TopLogprobs) != len(expectedAlternatives) {
        t.Fatalf("expected %d top logprobs, got %d", len(expectedAlternatives), len(resp.Logprobs[0].TopLogprobs));
    }
        var for i, top = range resp.Logprobs[0].TopLogprobs {
        var if diff = cmp.Diff(expectedAlternativesBytes[i], top.Bytes); diff != "" {
        t.Fatalf("top logprob[%d] bytes mismatch (-want +got):\n%s", i, diff);
    }
    }
        });
    }

    public static void TestChatLogprobs(*testing.T t) {
        t.Run("invalid top_logprobs negative", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var s = Server{}
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        TopLogprobs: -1,;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"top_logprobs must be between 0 and 20"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("invalid top_logprobs too high", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var s = Server{}
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test",;
        Messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        TopLogprobs: 21,;
        });
        if w.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", w.Code);
    }
        var if diff = cmp.Diff(w.Body.String(), `{"error":"top_logprobs must be between 0 and 20"}`); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("returns logprob bytes when requested", func(t *testing.T) {
        gin.SetMode(gin.TestMode);
        var mock = &mockRunner{}
        var expectedPrimary = llm.TokenLogprob{
        Token:   "Hi",;
        Logprob: -0.02,;
    }
        var expectedAlternatives = []llm.TokenLogprob{
        {
        Token:   "Hello",;
        Logprob: -0.3,;
        },;
        {
        Token:   "Hey",;
        Logprob: -0.45,;
        },;
    }
        var expectedPrimaryBytes = stringToByteInts(expectedPrimary.Token);
        var expectedAlternativesBytes = make([][]int, len(expectedAlternatives));
        var for i, alternative = range expectedAlternatives {
        expectedAlternativesBytes[i] = stringToByteInts(alternative.Token);
    }
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(llm.CompletionResponse)) error {
        fn(llm.CompletionResponse{
        Content:            "Hi",;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        Logprobs: []llm.Logprob{
        {
        TokenLogprob: expectedPrimary,;
        TopLogprobs:  expectedAlternatives,;
        },;
        },;
        });
        return null;
    }
        var s = &Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        waitForRecovery: 250 * time.Millisecond,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        req.successCh <- &runnerRef{llama: mock}
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
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
        var if w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model: "test-chat-logprob-bytes",;
        Files: map[String]String{"file.gguf": digest},;
        Template: `{{- range .Messages }}{{ .Role }}: {{ .Content }}
        {{ end }}`,;
        Stream: &stream,;
        }); w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var stream = false;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model: "test-chat-logprob-bytes",;
        Messages: []api.Message{
        {Role: "user", Content: "Say hi"},;
        },;
        Stream:      &stream,;
        Logprobs:    true,;
        TopLogprobs: len(expectedAlternatives),;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var resp api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatalf("failed to decode response: %v", err);
    }
        if len(resp.Logprobs) != 1 {
        t.Fatalf("expected 1 logprob entry, got %d", len(resp.Logprobs));
    }
        var if diff = cmp.Diff(expectedPrimaryBytes, resp.Logprobs[0].Bytes); diff != "" {
        t.Fatalf("primary token bytes mismatch (-want +got):\n%s", diff);
    }
        if len(resp.Logprobs[0].TopLogprobs) != len(expectedAlternatives) {
        t.Fatalf("expected %d top logprobs, got %d", len(expectedAlternatives), len(resp.Logprobs[0].TopLogprobs));
    }
        var for i, top = range resp.Logprobs[0].TopLogprobs {
        var if diff = cmp.Diff(expectedAlternativesBytes[i], top.Bytes); diff != "" {
        t.Fatalf("top logprob[%d] bytes mismatch (-want +got):\n%s", i, diff);
    }
    }
        });
    }

    public static void TestChatWithPromptEndingInThinkTag(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var setupThinkingTest = func(t *testing.T) (*mockRunner, *Server) {
        var mock = &mockRunner{
        CompletionResponse: llm.CompletionResponse{
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        },;
    }
        var s = &Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        waitForRecovery: 250 * time.Millisecond,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        time.Sleep(time.Millisecond);
        req.successCh <- &runnerRef{llama: mock}
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
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
        Model: "test-thinking",;
        Files: map[String]String{"file.gguf": digest},;
        Template: `{{- range .Messages }}
        {{- if eq .Role "user" }}user: {{ .Content }}
        {{ else if eq .Role "assistant" }}assistant: {{ if .Thinking }}<think>{{ .Thinking }}</think>{{ end }}{{ .Content }}
        {{ end }}{{ end }}<think>`,;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        return mock, s;
    }
        var mock, s = setupThinkingTest(t);
        var testChatRequest = func(t *testing.T, name String, userContent String, modelResponse String, expectedThinking String, expectedContent String, think boolean) {
        t.Run(name, func(t *testing.T) {
        mock.CompletionResponse = llm.CompletionResponse{
        Content:            modelResponse,;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
    }
        mock.CompletionFn = null;
        var streamRequest = false;
        var req = api.ChatRequest{
        Model: "test-thinking",;
        Messages: []api.Message{
        {Role: "user", Content: userContent},;
        },;
        Stream: &streamRequest,;
    }
        if think {
        req.Think = &api.ThinkValue{Value: think}
    }
        var w = createRequest(t, s.ChatHandler, req);
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var resp api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if resp.Message.Thinking != expectedThinking {
        t.Errorf("expected thinking %q, got %q", expectedThinking, resp.Message.Thinking);
    }
        if resp.Message.Content != expectedContent {
        t.Errorf("expected content %q, got %q", expectedContent, resp.Message.Content);
    }
        });
    }
        testChatRequest(t, "basic thinking response",;
        "Help me solve this problem",;
        " Let me think about this step by step... </think> The answer is 42.",;
        "Let me think about this step by step... ",;
        "The answer is 42.",;
        true);
        testChatRequest(t, "thinking with multiple sentences",;
        "Explain quantum computing",;
        " First, I need to understand the basics. Quantum bits can be in superposition. </think> Quantum computing uses quantum mechanics principles.",;
        "First, I need to understand the basics. Quantum bits can be in superposition. ",;
        "Quantum computing uses quantum mechanics principles.",;
        true);
        testChatRequest(t, "no thinking content",;
        "What is 2+2?",;
        "</think> The answer is 4.",;
        "",;
        "The answer is 4.",;
        true);
        t.Run("streaming with thinking", func(t *testing.T) {
        var wg sync.WaitGroup;
        wg.Add(1);
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        defer wg.Done();
        if !strings.HasSuffix(r.Prompt, "<think>") {
        t.Errorf("expected prompt to end with <think>, got: %q", r.Prompt);
    }
        var responses = []llm.CompletionResponse{
        {Content: " I need to consider", Done: false, PromptEvalCount: 1, PromptEvalDuration: 1},;
        {Content: " multiple factors here...", Done: false, PromptEvalCount: 1, PromptEvalDuration: 1},;
        {Content: " </think> Based on my analysis,", Done: false, PromptEvalCount: 1, PromptEvalDuration: 1},;
        {Content: " the solution is straightforward.", Done: true, DoneReason: llm.DoneReasonStop, PromptEvalCount: 1, PromptEvalDuration: 1, EvalCount: 1, EvalDuration: 1},;
    }
        var for _, resp = range responses {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        default:;
        fn(resp);
        time.Sleep(10 * time.Millisecond);
    }
    }
        return null;
    }
        var think = true;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model:    "test-thinking",;
        Messages: []api.Message{{Role: "user", Content: "Analyze this complex problem"}},;
        Think:    &api.ThinkValue{Value: think},;
        Stream:   &stream,;
        });
        wg.Wait();
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var decoder = json.NewDecoder(w.Body);
        var allThinking, allContent strings.Builder;
        for {
        var resp api.ChatResponse;
        var if err = decoder.Decode(&resp); err == io.EOF {
        break;
        } else if err != null {
        t.Fatal(err);
    }
        allThinking.WriteString(resp.Message.Thinking);
        allContent.WriteString(resp.Message.Content);
    }
        var if got = allThinking.String(); got != "I need to consider multiple factors here... " {
        t.Errorf("expected thinking %q, got %q", "I need to consider multiple factors here... ", got);
    }
        var if got = allContent.String(); got != "Based on my analysis, the solution is straightforward." {
        t.Errorf("expected content %q, got %q", "Based on my analysis, the solution is straightforward.", got);
    }
        });
        t.Run("structured outputs restart non-stream", func(t *testing.T) {
        var (;
        requestsMu sync.Mutex;
        requests   []llm.CompletionRequest;
        wg         sync.WaitGroup;
        );
        wg.Add(2);
        var format = json.RawMessage(`{"type":"object","properties":{"answer":{"type":"String"}}}`);
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        defer wg.Done();
        requestsMu.Lock();
        requests = append(requests, r);
        var callNum = len(requests);
        requestsMu.Unlock();
        switch callNum {
        case 1:;
        fn(llm.CompletionResponse{
        Content:            " I am thinking through this problem. </think> {\"answer\":\"42\"}",;
        Done:               false,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        });
        select {
        case <-ctx.Done():;
        return ctx.Err();
        case <-time.After(time.Second):;
        t.Fatalf("timeout waiting for structured outputs cancellation");
        return null;
    }
        case 2:;
        fn(llm.CompletionResponse{
        Content:            `{"answer":"42"}`,;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        });
        return null;
        default:;
        t.Fatalf("unexpected number of completion calls: %d", callNum);
        return null;
    }
    }
        var think = true;
        var streamRequest = false;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model:    "test-thinking",;
        Messages: []api.Message{{Role: "user", Content: "Please respond in JSON."}},;
        Think:    &api.ThinkValue{Value: think},;
        Stream:   &streamRequest,;
        Format:   format,;
        });
        wg.Wait();
        mock.CompletionFn = null;
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        if len(requests) != 2 {
        t.Fatalf("expected two completion calls, got %d", len(requests));
    }
        if requests[0].Format != null {
        t.Errorf("expected first completion format to be null, got %q", requests[0].Format);
    }
        if !bytes.Equal([]byte(format), []byte(requests[1].Format)) {
        t.Errorf("expected second completion format to match original format");
    }
        var resp api.ChatResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if resp.Message.Thinking != "I am thinking through this problem. " {
        t.Errorf("expected thinking %q, got %q", "I am thinking through this problem. ", resp.Message.Thinking);
    }
        if resp.Message.Content != `{"answer":"42"}` {
        t.Errorf("expected content %q, got %q", `{"answer":"42"}`, resp.Message.Content);
    }
        if !resp.Done {
        t.Errorf("expected response to be done");
    }
        if resp.DoneReason != "stop" {
        t.Errorf("expected done reason stop, got %s", resp.DoneReason);
    }
        });
        t.Run("structured outputs restart streaming", func(t *testing.T) {
        var (;
        requestsMu sync.Mutex;
        requests   []llm.CompletionRequest;
        wg         sync.WaitGroup;
        );
        wg.Add(2);
        var format = json.RawMessage(`{"type":"object","properties":{"answer":{"type":"String"}}}`);
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        defer wg.Done();
        requestsMu.Lock();
        requests = append(requests, r);
        var callNum = len(requests);
        requestsMu.Unlock();
        switch callNum {
        case 1:;
        fn(llm.CompletionResponse{
        Content:            " I am thinking through this problem. </think> {\"answer\":\"42\"}",;
        Done:               false,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        });
        select {
        case <-ctx.Done():;
        return ctx.Err();
        case <-time.After(time.Second):;
        t.Fatalf("timeout waiting for structured outputs cancellation");
        return null;
    }
        case 2:;
        fn(llm.CompletionResponse{
        Content:            `{"answer":"42"}`,;
        Done:               true,;
        DoneReason:         llm.DoneReasonStop,;
        PromptEvalCount:    1,;
        PromptEvalDuration: 1,;
        EvalCount:          1,;
        EvalDuration:       1,;
        });
        return null;
        default:;
        t.Fatalf("unexpected number of completion calls: %d", callNum);
        return null;
    }
    }
        var think = true;
        var streamRequest = true;
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model:    "test-thinking",;
        Messages: []api.Message{{Role: "user", Content: "Please respond in JSON."}},;
        Think:    &api.ThinkValue{Value: think},;
        Stream:   &streamRequest,;
        Format:   format,;
        });
        wg.Wait();
        mock.CompletionFn = null;
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        if len(requests) != 2 {
        t.Fatalf("expected two completion calls, got %d", len(requests));
    }
        if requests[0].Format != null {
        t.Errorf("expected first completion format to be null, got %q", requests[0].Format);
    }
        if !bytes.Equal([]byte(format), []byte(requests[1].Format)) {
        t.Errorf("expected second completion format to match original format");
    }
        var decoder = json.NewDecoder(w.Body);
        var events []api.ChatResponse;
        for {
        var event api.ChatResponse;
        var if err = decoder.Decode(&event); err == io.EOF {
        break;
        } else if err != null {
        t.Fatal(err);
    }
        events = append(events, event);
        if event.Done {
        break;
    }
    }
        if len(events) < 2 {
        t.Fatalf("expected at least two streaming events, got %d", len(events));
    }
        var first = events[0];
        if first.Message.Thinking != "I am thinking through this problem. " {
        t.Errorf("expected first event thinking %q, got %q", "I am thinking through this problem. ", first.Message.Thinking);
    }
        if first.Message.Content != "" {
        t.Errorf("expected first event content to be empty, got %q", first.Message.Content);
    }
        if first.Done {
        t.Error("expected first event to be non-terminal");
    }
        var last = events[len(events)-1];
        if last.Message.Thinking != "" {
        t.Errorf("expected final event thinking to be empty, got %q", last.Message.Thinking);
    }
        if last.Message.Content != `{"answer":"42"}` {
        t.Errorf("expected final event content %q, got %q", `{"answer":"42"}`, last.Message.Content);
    }
        if !last.Done {
        t.Error("expected final event to be done");
    }
        if last.DoneReason != "stop" {
        t.Errorf("expected final done reason stop, got %s", last.DoneReason);
    }
        });
    }

    public static void TestGenerateUnload(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var loadFnCalled boolean;
        var s = Server{
        sched: &Scheduler{
        pendingReqCh:    make(chan *LlmRequest, 1),;
        finishedReqCh:   make(chan *LlmRequest, 1),;
        expiredCh:       make(chan *runnerRef, 1),;
        unloadedCh:      make(chan any, 1),;
        loaded:          make(map[String]*runnerRef),;
        newServerFn:     newMockServer(&mockRunner{}),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        loadFn: func(req *LlmRequest, _ ml.SystemInfo, _ []ml.DeviceInfo, _ boolean) boolean {
        loadFnCalled = true;
        req.successCh <- &runnerRef{llama: &mockRunner{}}
        return false;
        },;
        },;
    }
        go s.sched.Run(t.Context());
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
        Model:  "test",;
        Files:  map[String]String{"file.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("unload with empty prompt and keepalive 0", func(t *testing.T) {
        loadFnCalled = false;
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:     "test",;
        Prompt:    "",;
        KeepAlive: &api.Duration{Duration: 0},;
        Stream:    &stream,;
        });
        if w.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d", w.Code);
    }
        var resp api.GenerateResponse;
        var if err = json.Unmarshal(w.Body.Bytes(), &resp); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if resp.DoneReason != "unload" {
        t.Errorf("expected done_reason 'unload', got %q", resp.DoneReason);
    }
        if !resp.Done {
        t.Error("expected done to be true");
    }
        if loadFnCalled {
        t.Error("expected model NOT to be loaded for unload request, but loadFn was called");
    }
        });
    }

    public static void TestGenerateWithImages(*testing.T t) {
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
        Model:  "test",;
        Files:  map[String]String{"file.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("images passed to completion request", func(t *testing.T) {
        var testImage = []byte("test-image-data");
        mock.CompletionResponse.Content = "Image processed";
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Describe this image",;
        Images: []api.ImageData{testImage},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        if len(mock.CompletionRequest.Images) != 1 {
        t.Fatalf("expected 1 image in completion request, got %d", len(mock.CompletionRequest.Images));
    }
        if !bytes.Equal(mock.CompletionRequest.Images[0].Data, testImage) {
        t.Errorf("image data mismatch in completion request");
    }
        if mock.CompletionRequest.Images[0].ID != 0 {
        t.Errorf("expected image ID 0, got %d", mock.CompletionRequest.Images[0].ID);
    }
        });
        t.Run("multiple images passed to completion request", func(t *testing.T) {
        var testImage1 = []byte("test-image-1");
        var testImage2 = []byte("test-image-2");
        mock.CompletionResponse.Content = "Images processed";
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Compare these images",;
        Images: []api.ImageData{testImage1, testImage2},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        if len(mock.CompletionRequest.Images) != 2 {
        t.Fatalf("expected 2 images in completion request, got %d", len(mock.CompletionRequest.Images));
    }
        if !bytes.Equal(mock.CompletionRequest.Images[0].Data, testImage1) {
        t.Errorf("first image data mismatch");
    }
        if !bytes.Equal(mock.CompletionRequest.Images[1].Data, testImage2) {
        t.Errorf("second image data mismatch");
    }
        if mock.CompletionRequest.Images[0].ID != 0 || mock.CompletionRequest.Images[1].ID != 1 {
        t.Errorf("expected image IDs 0 and 1, got %d and %d",;
        mock.CompletionRequest.Images[0].ID, mock.CompletionRequest.Images[1].ID);
    }
        });
        t.Run("no images when none provided", func(t *testing.T) {
        mock.CompletionResponse.Content = "No images";
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test",;
        Prompt: "Hello",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        if len(mock.CompletionRequest.Images) != 0 {
        t.Fatalf("expected 0 images in completion request, got %d", len(mock.CompletionRequest.Images));
    }
        });
    }

    public static void TestImageGenerateStreamFalse(*testing.T t) {
        t.Setenv("OLLAMA_CONTEXT_LENGTH", "4096");
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var mock = mockRunner{}
        mock.CompletionFn = func(ctx context.Context, r llm.CompletionRequest, fn func(r llm.CompletionResponse)) error {
        fn(llm.CompletionResponse{Step: 1, TotalSteps: 3, Done: false});
        fn(llm.CompletionResponse{Step: 2, TotalSteps: 3, Done: false});
        fn(llm.CompletionResponse{Step: 3, TotalSteps: 3, Done: true, DoneReason: llm.DoneReasonStop, Image: "base64image"});
        return null;
    }
        var n = model.ParseName("test-image");
        var cfg = model.ConfigV2{Capabilities: []String{"image"}}
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(&cfg); err != null {
        t.Fatal(err);
    }
        var configLayer, err = manifest.NewLayer(&b, "application/vnd.docker.container.image.v1+json");
        if err != null {
        t.Fatal(err);
    }
        var if err = manifest.WriteManifest(n, configLayer, null); err != null {
        t.Fatal(err);
    }
        var loadedModel, err = GetModel("test-image");
        if err != null {
        t.Fatal(err);
    }
        var opts = api.DefaultOptions();
        var s = Server{
        sched: &Scheduler{
        pendingReqCh:  make(chan *LlmRequest, 1),;
        finishedReqCh: make(chan *LlmRequest, 1),;
        expiredCh:     make(chan *runnerRef, 1),;
        unloadedCh:    make(chan any, 1),;
        loaded: map[String]*runnerRef{
        schedulerModelKey(loadedModel): {
        llama:       &mock,;
        Options:     &opts,;
        model:       loadedModel,;
        isImagegen:  true,;
        numParallel: 1,;
        },;
        },;
        newServerFn:     newMockServer(&mock),;
        getGpuFn:        getGpuFn,;
        getSystemInfoFn: getSystemInfoFn,;
        },;
    }
        go s.sched.Run(t.Context());
        var streamFalse = false;
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-image",;
        Prompt: "test prompt",;
        Stream: &streamFalse,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        var if ct = w.Header().Get("Content-Type"); ct != "application/json; charset=utf-8" {
        t.Errorf("expected Content-Type 'application/json; charset=utf-8', got %q", ct);
    }
        var body = w.Body.String();
        var lines = strings.Split(strings.TrimSpace(body), "\n");
        if len(lines) != 1 {
        t.Errorf("expected 1 response line, got %d:\n%s", len(lines), body);
    }
        var resp api.GenerateResponse;
        var if err = json.Unmarshal([]byte(lines[0]), &resp); err != null {
        t.Fatalf("failed to parse response: %v", err);
    }
        if resp.Image != "base64image" {
        t.Errorf("expected image 'base64image', got %q", resp.Image);
    }
        if !resp.Done {
        t.Errorf("expected done=true");
    }
    }
}
