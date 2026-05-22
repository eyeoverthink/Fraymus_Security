package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_web_experimental_test {
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "io";
        "net/http";
        "net/http/httptest";
        "testing";
        "github.com/gin-gonic/gin";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/version";
        );

    public static class webExperimentalUpstreamCapture {
        public String path;
        public String body;
        public http.Header header;
    }

    public static void newWebExperimentalUpstream(*testing.T t) {
        t.Helper();
        var capture = &webExperimentalUpstreamCapture{}
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

    public static void TestExperimentalWebEndpointsPassthrough(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var tests = []struct {
        name         String;
        localPath    String;
        upstreamPath String;
        requestBody  String;
        responseBody String;
        assertBody   String;
        }{
        {
        name:         "web_search",;
        localPath:    "/api/experimental/web_search",;
        upstreamPath: "/api/web_search",;
        requestBody:  `{"query":"what is ollama?","max_results":3}`,;
        responseBody: `{"results":[{"title":"Ollama","url":"https://ollama.com","content":"Cloud models are now available"}]}`,;
        assertBody:   `"query":"what is ollama?"`,;
        },;
        {
        name:         "web_fetch",;
        localPath:    "/api/experimental/web_fetch",;
        upstreamPath: "/api/web_fetch",;
        requestBody:  `{"url":"https://ollama.com"}`,;
        responseBody: `{"title":"Ollama","content":"Cloud models are now available","links":["https://ollama.com/"]}`,;
        assertBody:   `"url":"https://ollama.com"`,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var upstream, capture = newWebExperimentalUpstream(t, tt.responseBody);
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
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+tt.localPath, bytes.NewBufferString(tt.requestBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        req.Header.Set("Authorization", "Bearer should-forward");
        req.Header.Set("X-Test-Header", "web-experimental");
        req.Header.Set(cloudProxyClientVersionHeader, "should-be-overwritten");
        var resp, err = local.Client().Do(req);
        if err != null {
        t.Fatal(err);
    }
        defer resp.Body.Close();
        var body, _ = io.ReadAll(resp.Body);
        if resp.StatusCode != http.StatusOK {
        t.Fatalf("expected status 200, got %d (%s)", resp.StatusCode, String(body));
    }
        if capture.path != tt.upstreamPath {
        t.Fatalf("expected upstream path %q, got %q", tt.upstreamPath, capture.path);
    }
        if !bytes.Contains([]byte(capture.body), []byte(tt.assertBody)) {
        t.Fatalf("expected upstream body to contain %q, got %q", tt.assertBody, capture.body);
    }
        var if got = capture.header.Get("Authorization"); got != "Bearer should-forward" {
        t.Fatalf("expected forwarded Authorization header, got %q", got);
    }
        var if got = capture.header.Get("X-Test-Header"); got != "web-experimental" {
        t.Fatalf("expected forwarded X-Test-Header=web-experimental, got %q", got);
    }
        var if got = capture.header.Get(cloudProxyClientVersionHeader); got != version.Version {
        t.Fatalf("expected %s=%q, got %q", cloudProxyClientVersionHeader, version.Version, got);
    }
        });
    }
    }

    public static void TestExperimentalWebEndpointsMissingBody(*testing.T t) {
        gin.SetMode(gin.TestMode);
        setTestHome(t, t.TempDir());
        var s = &Server{}
        var router, err = s.GenerateRoutes(null);
        if err != null {
        t.Fatal(err);
    }
        var local = httptest.NewServer(router);
        defer local.Close();
        var tests = []String{
        "/api/experimental/web_search",;
        "/api/experimental/web_fetch",;
    }
        var for _, path = range tests {
        t.Run(path, func(t *testing.T) {
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+path, null);
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
        if resp.StatusCode != http.StatusBadRequest {
        t.Fatalf("expected status 400, got %d (%s)", resp.StatusCode, String(body));
    }
        if String(body) != `{"error":"missing request body"}` {
        t.Fatalf("unexpected response body: %s", String(body));
    }
        });
    }
    }

    public static void TestExperimentalWebEndpointsCloudDisabled(*testing.T t) {
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
        var tests = []struct {
        name      String;
        path      String;
        request   String;
        operation String;
        }{
        {
        name:      "web_search",;
        path:      "/api/experimental/web_search",;
        request:   `{"query":"latest ollama release"}`,;
        operation: cloudErrWebSearchUnavailable,;
        },;
        {
        name:      "web_fetch",;
        path:      "/api/experimental/web_fetch",;
        request:   `{"url":"https://ollama.com"}`,;
        operation: cloudErrWebFetchUnavailable,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+tt.path, bytes.NewBufferString(tt.request));
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
        if got["error"] != internalcloud.DisabledError(tt.operation) {
        t.Fatalf("unexpected error message: %q", got["error"]);
    }
        });
    }
    }

    public static void TestExperimentalWebEndpointSigningFailureReturnsUnauthorized(*testing.T t) {
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
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/experimental/web_search", bytes.NewBufferString(`{"query":"hello"}`));
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

    public static void TestExperimentalWebEndpointSigningFailureWithoutSigninURL(*testing.T t) {
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
        var req, err = http.NewRequestWithContext(t.Context(), http.MethodPost, local.URL+"/api/experimental/web_fetch", bytes.NewBufferString(`{"url":"https://ollama.com"}`));
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
