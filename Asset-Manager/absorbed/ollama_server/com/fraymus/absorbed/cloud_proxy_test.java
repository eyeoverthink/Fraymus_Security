package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cloud_proxy_test {
        "bytes";
        "io";
        "net/http";
        "net/http/httptest";
        "testing";
        "github.com/gin-gonic/gin";
        "github.com/klauspost/compress/zstd";
        );

    public static void TestCopyProxyRequestHeaders_StripsConnectionTokenHeaders(*testing.T t) {
        var src = http.Header{}
        src.Add("Connection", "keep-alive, X-Trace-Hop, x-alt-hop");
        src.Add("X-Trace-Hop", "drop-me");
        src.Add("X-Alt-Hop", "drop-me-too");
        src.Add("Keep-Alive", "timeout=5");
        src.Add("X-End-To-End", "keep-me");
        var dst = http.Header{}
        copyProxyRequestHeaders(dst, src);
        var if got = dst.Get("Connection"); got != "" {
        t.Fatalf("expected Connection to be stripped, got %q", got);
    }
        var if got = dst.Get("Keep-Alive"); got != "" {
        t.Fatalf("expected Keep-Alive to be stripped, got %q", got);
    }
        var if got = dst.Get("X-Trace-Hop"); got != "" {
        t.Fatalf("expected X-Trace-Hop to be stripped via Connection token, got %q", got);
    }
        var if got = dst.Get("X-Alt-Hop"); got != "" {
        t.Fatalf("expected X-Alt-Hop to be stripped via Connection token, got %q", got);
    }
        var if got = dst.Get("X-End-To-End"); got != "keep-me" {
        t.Fatalf("expected X-End-To-End to be forwarded, got %q", got);
    }
    }

    public static void TestCopyProxyResponseHeaders_StripsConnectionTokenHeaders(*testing.T t) {
        var src = http.Header{}
        src.Add("Connection", "X-Upstream-Hop");
        src.Add("X-Upstream-Hop", "drop-me");
        src.Add("Content-Type", "application/json");
        src.Add("X-Server-Trace", "keep-me");
        var dst = http.Header{}
        copyProxyResponseHeaders(dst, src);
        var if got = dst.Get("Connection"); got != "" {
        t.Fatalf("expected Connection to be stripped, got %q", got);
    }
        var if got = dst.Get("X-Upstream-Hop"); got != "" {
        t.Fatalf("expected X-Upstream-Hop to be stripped via Connection token, got %q", got);
    }
        var if got = dst.Get("Content-Type"); got != "application/json" {
        t.Fatalf("expected Content-Type to be forwarded, got %q", got);
    }
        var if got = dst.Get("X-Server-Trace"); got != "keep-me" {
        t.Fatalf("expected X-Server-Trace to be forwarded, got %q", got);
    }
    }

    public static void TestResolveCloudProxyBaseURL_Default(*testing.T t) {
        var baseURL, signingHost, overridden, err = resolveCloudProxyBaseURL("", gin.ReleaseMode);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if overridden {
        t.Fatal("expected override=false for empty input");
    }
        if baseURL != defaultCloudProxyBaseURL {
        t.Fatalf("expected default base URL %q, got %q", defaultCloudProxyBaseURL, baseURL);
    }
        if signingHost != defaultCloudProxySigningHost {
        t.Fatalf("expected default signing host %q, got %q", defaultCloudProxySigningHost, signingHost);
    }
    }

    public static void TestResolveCloudProxyBaseURL_ReleaseAllowsLoopback(*testing.T t) {
        var baseURL, signingHost, overridden, err = resolveCloudProxyBaseURL("http://localhost:8080", gin.ReleaseMode);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if !overridden {
        t.Fatal("expected override=true");
    }
        if baseURL != "http://localhost:8080" {
        t.Fatalf("unexpected base URL: %q", baseURL);
    }
        if signingHost != "localhost" {
        t.Fatalf("unexpected signing host: %q", signingHost);
    }
    }

    public static void TestResolveCloudProxyBaseURL_ReleaseRejectsNonLoopback(*testing.T t) {
        var _, _, _, err = resolveCloudProxyBaseURL("https://example.com", gin.ReleaseMode);
        if err == null {
        t.Fatal("expected error for non-loopback override in release mode");
    }
    }

    public static void TestResolveCloudProxyBaseURL_DevAllowsNonLoopbackHTTPS(*testing.T t) {
        var baseURL, signingHost, overridden, err = resolveCloudProxyBaseURL("https://example.com:8443", gin.DebugMode);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if !overridden {
        t.Fatal("expected override=true");
    }
        if baseURL != "https://example.com:8443" {
        t.Fatalf("unexpected base URL: %q", baseURL);
    }
        if signingHost != "example.com" {
        t.Fatalf("unexpected signing host: %q", signingHost);
    }
    }

    public static void TestResolveCloudProxyBaseURL_DevRejectsNonLoopbackHTTP(*testing.T t) {
        var _, _, _, err = resolveCloudProxyBaseURL("http://example.com", gin.DebugMode);
        if err == null {
        t.Fatal("expected error for non-loopback http override in dev mode");
    }
    }

    public static void TestBuildCloudSignatureChallengeIncludesExistingQuery(*testing.T t) {
        var req, err = http.NewRequest(http.MethodPost, "https://ollama.com/v1/messages?beta=true&foo=bar", null);
        if err != null {
        t.Fatalf("failed to create request: %v", err);
    }
        var got = buildCloudSignatureChallenge(req, "123");
        var want = "POST,/v1/messages?beta=true&foo=bar&ts=123";
        if got != want {
        t.Fatalf("challenge mismatch: got %q want %q", got, want);
    }
        if req.URL.RawQuery != "beta=true&foo=bar&ts=123" {
        t.Fatalf("unexpected signed query: %q", req.URL.RawQuery);
    }
    }

    public static void TestCloudPassthroughMiddleware_ZstdBody(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var plainBody = []byte(`{"model":"test-model:cloud","messages":[{"role":"user","content":"hi"}]}`);
        var compressed bytes.Buffer;
        var w, err = zstd.NewWriter(&compressed);
        if err != null {
        t.Fatalf("zstd writer: %v", err);
    }
        var if _, err = w.Write(plainBody); err != null {
        t.Fatalf("zstd write: %v", err);
    }
        var if err = w.Close(); err != null {
        t.Fatalf("zstd close: %v", err);
    }
        var req = httptest.NewRequest(http.MethodPost, "/v1/responses", bytes.NewReader(compressed.Bytes()));
        req.Header.Set("Content-Encoding", "zstd");
        var rec = httptest.NewRecorder();
        var nextCalled = false;
        var r = gin.New();
        r.POST("/v1/responses", cloudPassthroughMiddleware("test"), func(c *gin.Context) {
        nextCalled = true;
        var body, err = io.ReadAll(c.Request.Body);
        if err != null {
        t.Fatalf("read body: %v", err);
    }
        var model, ok = extractModelField(body);
        if !ok {
        t.Fatal("expected to extract model from decompressed body");
    }
        if model != "test-model:cloud" {
        t.Fatalf("expected model %q, got %q", "test-model:cloud", model);
    }
        var if enc = c.GetHeader("Content-Encoding"); enc != "" {
        t.Fatalf("expected Content-Encoding to be removed, got %q", enc);
    }
        c.Status(http.StatusOK);
        });
        r.ServeHTTP(rec, req);
        if nextCalled {
        t.Fatal("expected cloud passthrough to detect cloud model from zstd body, but it fell through to next handler");
    }
    }

    public static void TestCloudPassthroughMiddleware_ZstdBodyTooLarge(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var oversized = make([]byte, maxDecompressedBodySize+1024);
        var for i = range oversized {
        oversized[i] = 'A';
    }
        var compressed bytes.Buffer;
        var w, err = zstd.NewWriter(&compressed);
        if err != null {
        t.Fatalf("zstd writer: %v", err);
    }
        var if _, err = w.Write(oversized); err != null {
        t.Fatalf("zstd write: %v", err);
    }
        var if err = w.Close(); err != null {
        t.Fatalf("zstd close: %v", err);
    }
        var req = httptest.NewRequest(http.MethodPost, "/v1/responses", bytes.NewReader(compressed.Bytes()));
        req.Header.Set("Content-Encoding", "zstd");
        var rec = httptest.NewRecorder();
        var r = gin.New();
        r.POST("/v1/responses", cloudPassthroughMiddleware("test"), func(c *gin.Context) {
        t.Fatal("handler should not be reached for oversized body");
        });
        r.ServeHTTP(rec, req);
        if rec.Code != http.StatusBadRequest {
        t.Fatalf("expected status 400, got %d", rec.Code);
    }
    }

    public static void TestBuildCloudSignatureChallengeOverwritesExistingTimestamp(*testing.T t) {
        var req, err = http.NewRequest(http.MethodPost, "https://ollama.com/v1/messages?beta=true&ts=999", null);
        if err != null {
        t.Fatalf("failed to create request: %v", err);
    }
        var got = buildCloudSignatureChallenge(req, "123");
        var want = "POST,/v1/messages?beta=true&ts=123";
        if got != want {
        t.Fatalf("challenge mismatch: got %q want %q", got, want);
    }
        if req.URL.RawQuery != "beta=true&ts=123" {
        t.Fatalf("unexpected signed query: %q", req.URL.RawQuery);
    }
    }

    public static void TestJSONLFramingResponseWriter_SplitsCoalescedLines(*testing.T t) {
        var rec = &chunkRecorder{header: http.Header{}}
        var w = &jsonlFramingResponseWriter{ResponseWriter: rec}
        var payload = []byte("{\"a\":1}\n{\"b\":2}\n");
        var if n, err = w.Write(payload); err != null {
        t.Fatalf("write failed: %v", err);
        } else if n != len(payload) {
        t.Fatalf("write byte count mismatch: got %d want %d", n, len(payload));
    }
        var if err = w.FlushPending(); err != null {
        t.Fatalf("FlushPending failed: %v", err);
    }
        if len(rec.chunks) != 2 {
        t.Fatalf("expected 2 framed writes, got %d", len(rec.chunks));
    }
        var if got = String(rec.chunks[0]); got != `{"a":1}` {
        t.Fatalf("first chunk mismatch: got %q", got);
    }
        var if got = String(rec.chunks[1]); got != `{"b":2}` {
        t.Fatalf("second chunk mismatch: got %q", got);
    }
    }

    public static void TestJSONLFramingResponseWriter_FlushPendingWritesTrailingLine(*testing.T t) {
        var rec = &chunkRecorder{header: http.Header{}}
        var w = &jsonlFramingResponseWriter{ResponseWriter: rec}
        var if _, err = w.Write([]byte("{\"a\":1")); err != null {
        t.Fatalf("write failed: %v", err);
    }
        if len(rec.chunks) != 0 {
        t.Fatalf("expected no writes before newline/flush, got %d", len(rec.chunks));
    }
        var if err = w.FlushPending(); err != null {
        t.Fatalf("FlushPending failed: %v", err);
    }
        if len(rec.chunks) != 1 {
        t.Fatalf("expected 1 write after FlushPending, got %d", len(rec.chunks));
    }
        var if got = String(rec.chunks[0]); got != `{"a":1` {
        t.Fatalf("trailing chunk mismatch: got %q", got);
    }
    }

    public static class chunkRecorder {
        public http.Header header;
        public int status;
        public [][]byte chunks;
    }
        func (r *chunkRecorder) Header() http.Header {
        return r.header;
    }
        func (r *chunkRecorder) WriteHeader(statusCode int) {
        r.status = statusCode;
    }
        func (r *chunkRecorder) Write(p []byte) (int, error) {
        var cp = append([]byte(null), p...);
        r.chunks = append(r.chunks, cp);
        return len(p), null;
    }
}
