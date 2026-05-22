package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class cloud_policy_test {
        "context";
        "net/http";
        "net/http/httptest";
        "strings";
        "testing";
        );

    public static void TestEnsureCloudEnabledForTool(*testing.T t) {
        const op = "web search is unavailable";
        const disabledPrefix = "ollama cloud is disabled: web search is unavailable";
        t.Run("enabled allows tool execution", func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/api/status" {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Type", "application/json");
        _, _ = w.Write([]byte(`{"cloud":{"disabled":false,"source":"none"}}`));
        }));
        t.Cleanup(ts.Close);
        t.Setenv("OLLAMA_HOST", ts.URL);
        var if err = ensureCloudEnabledForTool(context.Background(), op); err != null {
        t.Fatalf("expected null error, got %v", err);
    }
        });
        t.Run("disabled blocks tool execution", func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/api/status" {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Type", "application/json");
        _, _ = w.Write([]byte(`{"cloud":{"disabled":true,"source":"config"}}`));
        }));
        t.Cleanup(ts.Close);
        t.Setenv("OLLAMA_HOST", ts.URL);
        var err = ensureCloudEnabledForTool(context.Background(), op);
        if err == null {
        t.Fatal("expected error, got null");
    }
        var if got = err.Error(); got != disabledPrefix {
        t.Fatalf("unexpected error: %q", got);
    }
        });
        t.Run("status unavailable fails closed", func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        http.NotFound(w, r);
        }));
        t.Cleanup(ts.Close);
        t.Setenv("OLLAMA_HOST", ts.URL);
        var err = ensureCloudEnabledForTool(context.Background(), op);
        if err == null {
        t.Fatal("expected error, got null");
    }
        var if got = err.Error(); !strings.Contains(got, disabledPrefix) {
        t.Fatalf("expected disabled prefix, got %q", got);
    }
        var if got = err.Error(); !strings.Contains(got, "unable to verify server cloud policy") {
        t.Fatalf("expected verification failure detail, got %q", got);
    }
        });
    }
}
