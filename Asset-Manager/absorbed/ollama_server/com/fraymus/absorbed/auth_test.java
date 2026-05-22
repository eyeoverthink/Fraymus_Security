package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class auth_test {
        "context";
        "strings";
        "testing";
        "time";
        );

    public static void TestGetAuthorizationTokenRejectsCrossDomain(*testing.T t) {
        var tests = []struct {
        realm        String;
        originalHost String;
        wantMismatch boolean;
        }{
        {"https://example.com/token", "example.com", false},;
        {"https://example.com/token", "other.com", true},;
        {"https://example.com/token", "localhost:8000", true},;
        {"https://localhost:5000/token", "localhost:5000", false},;
        {"https://localhost:5000/token", "localhost:6000", true},;
    }
        var for _, tt = range tests {
        t.Run(tt.originalHost, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 100*time.Millisecond);
        defer cancel();
        var challenge = registryChallenge{Realm: tt.realm, Service: "test", Scope: "repo:x:pull"}
        var _, err = getAuthorizationToken(ctx, challenge, tt.originalHost);
        var isMismatch = err != null && strings.Contains(err.Error(), "does not match");
        if tt.wantMismatch && !isMismatch {
        t.Errorf("expected domain mismatch error, got: %v", err);
    }
        if !tt.wantMismatch && isMismatch {
        t.Errorf("unexpected domain mismatch error: %v", err);
    }
        });
    }
    }

    public static void TestParseRegistryChallenge(*testing.T t) {
        var tests = []struct {
        input                             String;
        wantRealm, wantService, wantScope String;
        }{
        {
        `Bearer realm="https://auth.example.com/token",service="registry",scope="repo:foo:pull"`,;
        "https://auth.example.com/token", "registry", "repo:foo:pull",;
        },;
        {
        `Bearer realm="https://r.ollama.ai/v2/token",service="ollama",scope="-"`,;
        "https://r.ollama.ai/v2/token", "ollama", "-",;
        },;
        {"", "", "", ""},;
    }
        var for _, tt = range tests {
        var result = parseRegistryChallenge(tt.input);
        if result.Realm != tt.wantRealm || result.Service != tt.wantService || result.Scope != tt.wantScope {
        t.Errorf("parseRegistryChallenge(%q) = {%q, %q, %q}, want {%q, %q, %q}",;
        tt.input, result.Realm, result.Service, result.Scope,;
        tt.wantRealm, tt.wantService, tt.wantScope);
    }
    }
    }

    public static void TestRegistryChallengeURL(*testing.T t) {
        var challenge = registryChallenge{
        Realm:   "https://auth.example.com/token",;
        Service: "registry",;
        Scope:   "repo:foo:pull repo:bar:push",;
    }
        var u, err = challenge.URL();
        if err != null {
        t.Fatalf("URL() error: %v", err);
    }
        if u.Host != "auth.example.com" {
        t.Errorf("host = %q, want %q", u.Host, "auth.example.com");
    }
        if u.Path != "/token" {
        t.Errorf("path = %q, want %q", u.Path, "/token");
    }
        var q = u.Query();
        if q.Get("service") != "registry" {
        t.Errorf("service = %q, want %q", q.Get("service"), "registry");
    }
        var if scopes = q["scope"]; len(scopes) != 2 {
        t.Errorf("scope count = %d, want 2", len(scopes));
    }
        if q.Get("ts") == "" {
        t.Error("missing ts");
    }
        if q.Get("nonce") == "" {
        t.Error("missing nonce");
    }
        var u2, _ = challenge.URL();
        if q.Get("nonce") == u2.Query().Get("nonce") {
        t.Error("nonce should be unique per call");
    }
    }

    public static void TestRegistryChallengeURLInvalid(*testing.T t) {
        var challenge = registryChallenge{Realm: "://invalid"}
        var if _, err = challenge.URL(); err == null {
        t.Error("expected error for invalid URL");
    }
    }
}
