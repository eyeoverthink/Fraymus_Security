package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_cloud_test {
        "bufio";
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "io";
        "net/http";
        "net/http/httptest";
        "strings";
        "testing";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/api";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/middleware";
        "github.com/ollama/ollama/version";
        );

    public static void TestStatusHandler(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        t.Setenv("OLLAMA_NO_CLOUD", "1");
        var s = Server{}
        var w = createRequest(t, s.StatusHandler, null);
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        var resp api.StatusResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if !resp.Cloud.Disabled {
        t.Fatalf("expected cloud.disabled true, got false");
    }
        if resp.Cloud.Source != "env" {
        t.Fatalf("expected cloud.source env, got %q", resp.Cloud.Source);
    }
    }

    public static void TestCloudDisabledBlocksRemoteOperations(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        t.Setenv("OLLAMA_NO_CLOUD", "1");
        var s = Server{}
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:      "test-cloud",;
        RemoteHost: "example.com",;
        From:       "test",;
        Info: map[String]any{
        "capabilities": []String{"completion"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        t.Run("chat remote blocked", func(t *testing.T) {
        var w = createRequest(t, s.ChatHandler, api.ChatRequest{
        Model:    "test-cloud",;
        Messages: []api.Message{{Role: "user", Content: "hi"}},;
        });
        if w.Code != http.StatusForbidden {
        t.Fatalf("expected status 403, got %d", w.Code);
    }
        var if got = w.Body.String(); got != `{"error":"`+internalcloud.DisabledError(cloudErrRemoteInferenceUnavailable)+`"}` {
        t.Fatalf("unexpected response: %s", got);
    }
        });
        t.Run("generate remote blocked", func(t *testing.T) {
        var w = createRequest(t, s.GenerateHandler, api.GenerateRequest{
        Model:  "test-cloud",;
        Prompt: "hi",;
        });
        if w.Code != http.StatusForbidden {
        t.Fatalf("expected status 403, got %d", w.Code);
    }
        var if got = w.Body.String(); got != `{"error":"`+internalcloud.DisabledError(cloudErrRemoteInferenceUnavailable)+`"}` {
        t.Fatalf("unexpected response: %s", got);
    }
        });
        t.Run("show remote blocked", func(t *testing.T) {
        var w = createRequest(t, s.ShowHandler, api.ShowRequest{
        Model: "test-cloud",;
        });
        if w.Code != http.StatusForbidden {
        t.Fatalf("expected status 403, got %d", w.Code);
    }
        var if got = w.Body.String(); got != `{"error":"`+internalcloud.DisabledError(cloudErrRemoteModelDetailsUnavailable)+`"}` {
        t.Fatalf("unexpected response: %s", got);
    }
        });
    }

    public static void TestDeleteHandlerNormalizesExplicitSourceSuffixes(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var s = Server{}
        var tests = []String{
        "gpt-oss:20b:local",;
        "gpt-oss:20b:cloud",;
        "qwen3:cloud",;
    }
        var for _, modelName = range tests {
        t.Run(modelName, func(t *testing.T) {
        var w = createRequest(t, s.DeleteHandler, api.DeleteRequest{
        Model: modelName,;
        });
        if w.Code != http.StatusNotFound {
        t.Fatalf("expected status 404, got %d (%s)", w.Code, w.Body.String());
    }
        var resp map[String]String;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        var want = "model '" + modelName + "' not found";
        if resp["error"] != want {
        t.Fatalf("unexpected error: got %q, want %q", resp["error"], want);
    }
        });
    }
    }

    public static void TestExplicitCloudPassthroughAPIAndV1(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());

    public static class upstreamCapture {
        public String path;
        public String body;
        public http.Header header;
    }
        var newUpstream = func(t *testing.T, responseBody String) (*httptest.Server, *upstreamCapture) {
        t.Helper();
        var capture = &upstreamCapture{}
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var payload, _ = io.ReadAll(r.Body);
        capture.path = r.URL.Path;
        capture.body = String(payload);
        capture.header = r.Header.Clone();
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusOK);
        _, _ = w.Write([]byte(responseBody));
        }));
        return srv, capture;
    }
        t.Run("api generate", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"ok":"api"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","prompt":"hello","stream":false}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/generate", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        req.Header.Set("X-Test-Header", "api-header");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/generate" {
        t.Fatalf("expected upstream path /api/generate, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        var if got = capture.header.Get("X-Test-Header"); got != "api-header" {
        t.Fatalf("expected forwarded X-Test-Header=api-header, got %q", got);
    }
        var if got = capture.header.Get(cloudProxyClientVersionHeader); got != version.Version {
        t.Fatalf("expected %s=%q, got %q", cloudProxyClientVersionHeader, version.Version, got);
    }
        });
        t.Run("api chat", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"message":{"role":"assistant","content":"ok"},"done":true}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","messages":[{"role":"user","content":"hello"}],"stream":false}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/chat", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/chat" {
        t.Fatalf("expected upstream path /api/chat, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        });
        t.Run("api embed", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"model":"kimi-k2.5:cloud","embeddings":[[0.1,0.2]]}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","input":"hello"}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/embed", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/embed" {
        t.Fatalf("expected upstream path /api/embed, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        });
        t.Run("api embeddings", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"embedding":[0.1,0.2]}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","prompt":"hello"}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/embeddings", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/embeddings" {
        t.Fatalf("expected upstream path /api/embeddings, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        });
        t.Run("api show", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"details":{"format":"gguf"}}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud"}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/show", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/show" {
        t.Fatalf("expected upstream path /api/show, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        });
        t.Run("v1 chat completions bypasses conversion", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"chatcmpl_test","object":"chat.completion"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"gpt-oss:120b:cloud","messages":[{"role":"user","content":"hi"}],"max_tokens":7}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/chat/completions", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        req.Header.Set("X-Test-Header", "v1-header");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/chat/completions" {
        t.Fatalf("expected upstream path /v1/chat/completions, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"max_tokens":7`) {
        t.Fatalf("expected original OpenAI request body, got %q", capture.body);
    }
        if !strings.Contains(capture.body, `"model":"gpt-oss:120b"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        if strings.Contains(capture.body, `"options"`) {
        t.Fatalf("expected no converted Ollama options in upstream body, got %q", capture.body);
    }
        var if got = capture.header.Get("X-Test-Header"); got != "v1-header" {
        t.Fatalf("expected forwarded X-Test-Header=v1-header, got %q", got);
    }
        });
        t.Run("v1 chat completions bypasses conversion with legacy cloud suffix", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"chatcmpl_test","object":"chat.completion"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"gpt-oss:120b-cloud","messages":[{"role":"user","content":"hi"}],"max_tokens":7}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/chat/completions", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        req.Header.Set("X-Test-Header", "v1-legacy-header");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/chat/completions" {
        t.Fatalf("expected upstream path /v1/chat/completions, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"max_tokens":7`) {
        t.Fatalf("expected original OpenAI request body, got %q", capture.body);
    }
        if !strings.Contains(capture.body, `"model":"gpt-oss:120b"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        if strings.Contains(capture.body, `"options"`) {
        t.Fatalf("expected no converted Ollama options in upstream body, got %q", capture.body);
    }
        var if got = capture.header.Get("X-Test-Header"); got != "v1-legacy-header" {
        t.Fatalf("expected forwarded X-Test-Header=v1-legacy-header, got %q", got);
    }
        });
        t.Run("v1 messages bypasses conversion", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"msg_1","type":"message"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","max_tokens":10,"messages":[{"role":"user","content":"hi"}]}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/messages" {
        t.Fatalf("expected upstream path /v1/messages, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"max_tokens":10`) {
        t.Fatalf("expected original Anthropic request body, got %q", capture.body);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        if strings.Contains(capture.body, `"options"`) {
        t.Fatalf("expected no converted Ollama options in upstream body, got %q", capture.body);
    }
        });
        t.Run("v1 messages bypasses conversion with legacy cloud suffix", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"msg_1","type":"message"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:latest-cloud","max_tokens":10,"messages":[{"role":"user","content":"hi"}]}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/messages" {
        t.Fatalf("expected upstream path /v1/messages, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"max_tokens":10`) {
        t.Fatalf("expected original Anthropic request body, got %q", capture.body);
    }
        if !strings.Contains(capture.body, `"model":"kimi-k2.5:latest"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        if strings.Contains(capture.body, `"options"`) {
        t.Fatalf("expected no converted Ollama options in upstream body, got %q", capture.body);
    }
        });
        t.Run("v1 messages web_search fallback uses legacy cloud /api/chat path", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"model":"gpt-oss:120b","created_at":"2024-01-01T00:00:00Z","message":{"role":"assistant","content":"hello"},"done":true}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{
        "model":"gpt-oss:120b-cloud",;
        "max_tokens":10,;
        "messages":[{"role":"user","content":"search the web"}],;
        "tools":[{"type":"web_search_20250305","name":"web_search"}],;
        "stream":false;
        }`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages?beta=true", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/chat" {
        t.Fatalf("expected upstream path /api/chat for web_search fallback, got %q", capture.path);
    }
        if !strings.Contains(capture.body, `"model":"gpt-oss:120b"`) {
        t.Fatalf("expected normalized model in upstream body, got %q", capture.body);
    }
        if !strings.Contains(capture.body, `"num_predict":10`) {
        t.Fatalf("expected converted ollama options in upstream body, got %q", capture.body);
    }
        });
        t.Run("v1 messages web_search fallback frames coalesced jsonl chunks", func(t *testing.T) {

    public static class upstreamCapture {
        public String path;
    }
        var capture = &upstreamCapture{}
        var upstream = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        capture.path = r.URL.Path;
        w.Header().Set("Content-Type", "application/x-ndjson");
        w.WriteHeader(http.StatusOK);
        var combined = strings.Join([]String{
        `{"model":"gpt-oss:120b","created_at":"2024-01-01T00:00:00Z","message":{"role":"assistant","content":"Hel"},"done":false}`,;
        `{"model":"gpt-oss:120b","created_at":"2024-01-01T00:00:00Z","message":{"role":"assistant","content":"lo"},"done":true}`,;
        }, "\n") + "\n";
        _, _ = w.Write([]byte(combined));
        }));
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{
        "model":"gpt-oss:120b-cloud",;
        "max_tokens":10,;
        "stream":true,;
        "messages":[{"role":"user","content":"search the web"}],;
        "tools":[{"type":"web_search_20250305","name":"web_search"}];
        }`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages?beta=true", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/api/chat" {
        t.Fatalf("expected upstream path /api/chat for web_search fallback, got %q", capture.path);
    }
        if !strings.Contains(String(body), "event: message_stop") {
        t.Fatalf("expected anthropic streaming message_stop event, got body %q", String(body));
    }
        });
        t.Run("v1 model retrieve bypasses conversion", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"kimi-k2.5:cloud","object":"model","created":1,"owned_by":"ollama"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodGet, local.URL+"/v1/models/kimi-k2.5:cloud", null);
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("X-Test-Header", "v1-model-header");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/models/kimi-k2.5" {
        t.Fatalf("expected upstream path /v1/models/kimi-k2.5, got %q", capture.path);
    }
        if capture.body != "" {
        t.Fatalf("expected empty request body, got %q", capture.body);
    }
        var if got = capture.header.Get("X-Test-Header"); got != "v1-model-header" {
        t.Fatalf("expected forwarded X-Test-Header=v1-model-header, got %q", got);
    }
        });
        t.Run("v1 model retrieve normalizes legacy cloud suffix", func(t *testing.T) {
        var upstream, capture = newUpstream(t, `{"id":"kimi-k2.5:latest","object":"model","created":1,"owned_by":"ollama"}`);
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodGet, local.URL+"/v1/models/kimi-k2.5:latest-cloud", null);
        if err != null {
        t.Fatal(err);
    }
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != "/v1/models/kimi-k2.5:latest" {
        t.Fatalf("expected upstream path /v1/models/kimi-k2.5:latest, got %q", capture.path);
    }
        });
    }

    public static void TestCloudDisabledBlocksExplicitCloudPassthrough(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        t.Setenv("OLLAMA_NO_CLOUD", "1");
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/chat/completions", bytes.NewBufferString(`{"model":"kimi-k2.5:cloud","messages":[{"role":"user","content":"hi"}]}`));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusForbidden {
        t.Fatalf("expected status 403, got %d (%s)", resp.StatusCode, String(body));
    }
        var got map[String]String;
        var if err = json.Unmarshal(body, &got); err != null {
        t.Fatalf("expected json error body, got: %q", String(body));
    }
        if got["error"] != internalcloud.DisabledError(cloudErrRemoteInferenceUnavailable) {
        t.Fatalf("unexpected error message: %q", got["error"]);
    }
    }

    public static void TestCloudPassthroughStreamsPromptly(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var upstream = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/x-ndjson");
        var flusher, ok = w.(http.Flusher);
        if !ok {
        t.Fatal("upstream writer is not a flusher");
    }
        _, _ = w.Write([]byte(`{"response":"first"}` + "\n"));
        flusher.Flush();
        time.Sleep(700 * time.Millisecond);
        _, _ = w.Write([]byte(`{"response":"second"}` + "\n"));
        flusher.Flush();
        }));
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","messages":[{"role":"user","content":"hi"}],"stream":true}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/chat", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var body, _ = io.ReadAll(resp.Body);
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        var reader = bufio.NewReader(resp.Body);
        var start = time.Now();
        var firstLine, err = reader.ReadString('\n');
        if err != null {
        t.Fatalf("failed reading first streamed line: %v", err);
    }
        var if elapsed = time.Since(start); elapsed > 400*time.Millisecond {
        t.Fatalf("first streamed line arrived too late (%s), likely not flushing", elapsed);
    }
        if !strings.Contains(firstLine, `"first"`) {
        t.Fatalf("expected first line to contain first chunk, got %q", firstLine);
    }
        var secondLine, err = reader.ReadString('\n');
        if err != null {
        t.Fatalf("failed reading second streamed line: %v", err);
    }
        if !strings.Contains(secondLine, `"second"`) {
        t.Fatalf("expected second line to contain second chunk, got %q", secondLine);
    }
    }

    public static void TestCloudPassthroughSkipsAnthropicWebSearch(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());

    public static class upstreamCapture {
        public String path;
    }
        var capture = &upstreamCapture{}
        var upstream = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        capture.path = r.URL.Path;
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusOK);
        _, _ = w.Write([]byte(`{"id":"msg_1","type":"message"}`));
        }));
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var router = gin.New();
        router.POST(;
        "/v1/messages",;
        cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable),;
        middleware.AnthropicMessagesMiddleware(),;
        func(c *gin.Context) { c.Status(http.StatusTeapot) },;
        );
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{
        "model":"kimi-k2.5:cloud",;
        "max_tokens":10,;
        "messages":[{"role":"user","content":"hi"}],;
        "tools":[{"type":"web_search_20250305","name":"web_search"}];
        }`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusTeapot {
        var body, _ = io.ReadAll(resp.Body);
        t.Fatalf("expected local middleware path status %d, got %d (%s)", http.StatusTeapot, resp.StatusCode, String(body));
    }
        if capture.path != "" {
        t.Fatalf("expected no passthrough for web_search requests, got upstream path %q", capture.path);
    }
    }

    public static void TestCloudPassthroughSkipsAnthropicWebSearchLegacySuffix(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());

    public static class upstreamCapture {
        public String path;
    }
        var capture = &upstreamCapture{}
        var upstream = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        capture.path = r.URL.Path;
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusOK);
        _, _ = w.Write([]byte(`{"id":"msg_1","type":"message"}`));
        }));
        defer upstream.Close();
        var original = cloudProxyBaseURL;
        cloudProxyBaseURL = upstream.URL;
        t.Cleanup(func() { cloudProxyBaseURL = original });
        var router = gin.New();
        router.POST(;
        "/v1/messages",;
        cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable),;
        middleware.AnthropicMessagesMiddleware(),;
        func(c *gin.Context) { c.Status(http.StatusTeapot) },;
        );
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{
        "model":"kimi-k2.5:latest-cloud",;
        "max_tokens":10,;
        "messages":[{"role":"user","content":"hi"}],;
        "tools":[{"type":"web_search_20250305","name":"web_search"}];
        }`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/v1/messages", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusTeapot {
        var body, _ = io.ReadAll(resp.Body);
        t.Fatalf("expected local middleware path status %d, got %d (%s)", http.StatusTeapot, resp.StatusCode, String(body));
    }
        if capture.path != "" {
        t.Fatalf("expected no passthrough for web_search requests, got upstream path %q", capture.path);
    }
    }

    public static void TestCloudPassthroughSigningFailureReturnsUnauthorized(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var origSignRequest = cloudProxySignRequest;
        var origSigninURL = cloudProxySigninURL;
        cloudProxySignRequest = func(context.Context, *http.Request) error {
        return errors.New("ssh: no key found");
    }
        cloudProxySigninURL = func() (String, error) {
        return "https://ollama.com/signin/example", null;
    }
        t.Cleanup(func() {
        cloudProxySignRequest = origSignRequest;
        cloudProxySigninURL = origSigninURL;
        });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","prompt":"hello","stream":false}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/generate", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusUnauthorized {
        t.Fatalf("expected status 401, got %d (%s)", resp.StatusCode, String(body));
    }
        var got map[String]any;
        var if err = json.Unmarshal(body, &got); err != null {
        t.Fatalf("expected json error body, got: %q", String(body));
    }
        if got["error"] != "unauthorized" {
        t.Fatalf("unexpected error message: %v", got["error"]);
    }
        if got["signin_url"] != "https://ollama.com/signin/example" {
        t.Fatalf("unexpected signin_url: %v", got["signin_url"]);
    }
    }

    public static void TestCloudPassthroughSigningFailureWithoutSigninURL(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var origSignRequest = cloudProxySignRequest;
        var origSigninURL = cloudProxySigninURL;
        cloudProxySignRequest = func(context.Context, *http.Request) error {
        return errors.New("ssh: no key found");
    }
        cloudProxySigninURL = func() (String, error) {
        return "", errors.New("key missing");
    }
        t.Cleanup(func() {
        cloudProxySignRequest = origSignRequest;
        cloudProxySigninURL = origSigninURL;
        });
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var reqBody = `{"model":"kimi-k2.5:cloud","prompt":"hello","stream":false}`;
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/generate", bytes.NewBufferString(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusUnauthorized {
        t.Fatalf("expected status 401, got %d (%s)", resp.StatusCode, String(body));
    }
        var got map[String]any;
        var if err = json.Unmarshal(body, &got); err != null {
        t.Fatalf("expected json error body, got: %q", String(body));
    }
        if got["error"] != "unauthorized" {
        t.Fatalf("unexpected error message: %v", got["error"]);
    }
        var if _, ok = got["signin_url"]; ok {
        t.Fatalf("did not expect signin_url when helper fails, got %v", got["signin_url"]);
    }
    }
}
