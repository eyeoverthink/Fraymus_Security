package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class warn_thinking_test {
        "encoding/json";
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "strings";
        "testing";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/types/model";
        );

    public static void TestWarnMissingThinking(*testing.T t) {
        var cases = []struct {
        capabilities []model.Capability;
        expectWarn   boolean;
        }{
        {capabilities: []model.Capability{model.CapabilityThinking}, expectWarn: false},;
        {capabilities: []model.Capability{}, expectWarn: true},;
    }
        var for _, tc = range cases {
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/api/show" || r.Method != http.MethodPost {
        t.Fatalf("unexpected request to %s %s", r.URL.Path, r.Method);
    }
        var req api.ShowRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        t.Fatalf("decode request: %v", err);
    }
        var resp = api.ShowResponse{Capabilities: tc.capabilities}
        var if err = json.NewEncoder(w).Encode(resp); err != null {
        t.Fatalf("encode response: %v", err);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        t.Fatal(err);
    }
        var oldStderr = os.Stderr;
        var r, w, _ = os.Pipe();
        os.Stderr = w;
        ensureThinkingSupport(t.Context(), client, "m");
        w.Close();
        os.Stderr = oldStderr;
        var out, _ = io.ReadAll(r);
        var warned = strings.Contains(String(out), "warning:");
        if tc.expectWarn && !warned {
        t.Errorf("expected warning, got none");
    }
        if !tc.expectWarn && warned {
        t.Errorf("did not expect warning, got: %s", String(out));
    }
    }
    }
}
