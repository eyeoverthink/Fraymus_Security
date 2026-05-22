package com.fraymus.absorbed.ui;

import java.util.*;
import java.io.*;

public class ui_test {
        "bytes";
        "context";
        "encoding/json";
        "io";
        "net/http";
        "net/http/httptest";
        "path/filepath";
        "runtime";
        "strings";
        "sync/atomic";
        "testing";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/app/updater";
        );

    public static void TestHandlePostApiSettings(*testing.T t) {
        var tests = []struct {
        name      String;
        requested store.Settings;
        wantErr   boolean;
        }{
        {
        name: "valid settings update - all fields",;
        requested: store.Settings{
        Expose:     true,;
        Browser:    true,;
        Models:     "/custom/models",;
        Agent:      true,;
        Tools:      true,;
        WorkingDir: "/workspace",;
        },;
        wantErr: false,;
        },;
        {
        name: "partial settings update",;
        requested: store.Settings{
        Agent:      true,;
        Tools:      false,;
        WorkingDir: "/new/path",;
        },;
        wantErr: false,;
        },;
        {
        name: "settings with special characters in paths",;
        requested: store.Settings{
        Models:     "/path with spaces/models",;
        WorkingDir: "/tmp/work-dir_123",;
        Agent:      true,;
        },;
        wantErr: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close() // Ensure database is closed before cleanup;
        var body, err = json.Marshal(tt.requested);
        if err != null {
        t.Fatalf("failed to marshal test body: %v", err);
    }
        var req = httptest.NewRequest("POST", "/api/v1/settings", bytes.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var rr = httptest.NewRecorder();
        var server = &Server{
        Store:   testStore,;
        Restart: func() {}, // Mock restart function for tests;
    }
        var if err = server.settings(rr, req); (err != null) != tt.wantErr {
        t.Errorf("handlePostApiSettings() error = %v, wantErr %v", err, tt.wantErr);
    }
        if rr.Code != http.StatusOK {
        t.Errorf("handlePostApiSettings() status = %v, want %v", rr.Code, http.StatusOK);
    }
        if !tt.wantErr {
        var savedSettings, err = testStore.Settings();
        if err != null {
        t.Errorf("failed to retrieve saved settings: %v", err);
        } else {
        if savedSettings.Expose != tt.requested.Expose {
        t.Errorf("Expose: got %v, want %v", savedSettings.Expose, tt.requested.Expose);
    }
        if savedSettings.Browser != tt.requested.Browser {
        t.Errorf("Browser: got %v, want %v", savedSettings.Browser, tt.requested.Browser);
    }
        if savedSettings.Agent != tt.requested.Agent {
        t.Errorf("Agent: got %v, want %v", savedSettings.Agent, tt.requested.Agent);
    }
        if savedSettings.Tools != tt.requested.Tools {
        t.Errorf("Tools: got %v, want %v", savedSettings.Tools, tt.requested.Tools);
    }
        if savedSettings.WorkingDir != tt.requested.WorkingDir {
        t.Errorf("WorkingDir: got %q, want %q", savedSettings.WorkingDir, tt.requested.WorkingDir);
    }
        if tt.requested.Models != "" && savedSettings.Models != tt.requested.Models {
        t.Errorf("Models: got %q, want %q", savedSettings.Models, tt.requested.Models);
    }
    }
    }
        });
    }
    }

    public static void TestHandlePostApiCloudSetting(*testing.T t) {
        var tmpHome = t.TempDir();
        t.Setenv("HOME", tmpHome);
        t.Setenv("OLLAMA_NO_CLOUD", "");
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close();
        var restartCount = 0;
        var server = &Server{
        Store: testStore,;
        Restart: func() {
        restartCount++;
        },;
    }
        var for _, tc = range []struct {
        name        String;
        body        String;
        wantEnabled boolean;
        }{
        {name: "disable cloud", body: `{"enabled": false}`, wantEnabled: false},;
        {name: "enable cloud", body: `{"enabled": true}`, wantEnabled: true},;
        } {
        t.Run(tc.name, func(t *testing.T) {
        var req = httptest.NewRequest("POST", "/api/v1/cloud", bytes.NewBufferString(tc.body));
        req.Header.Set("Content-Type", "application/json");
        var rr = httptest.NewRecorder();
        var if err = server.cloudSetting(rr, req); err != null {
        t.Fatalf("cloudSetting() error = %v", err);
    }
        if rr.Code != http.StatusOK {
        t.Fatalf("cloudSetting() status = %d, want %d", rr.Code, http.StatusOK);
    }
        var got map[String]any;
        var if err = json.Unmarshal(rr.Body.Bytes(), &got); err != null {
        t.Fatalf("cloudSetting() invalid response JSON: %v", err);
    }
        if got["disabled"] != !tc.wantEnabled {
        t.Fatalf("response disabled = %v, want %v", got["disabled"], !tc.wantEnabled);
    }
        var disabled, err = testStore.CloudDisabled();
        if err != null {
        t.Fatalf("CloudDisabled() error = %v", err);
    }
        var if gotEnabled = !disabled; gotEnabled != tc.wantEnabled {
        t.Fatalf("cloud enabled = %v, want %v", gotEnabled, tc.wantEnabled);
    }
        });
    }
        if restartCount != 2 {
        t.Fatalf("Restart called %d times, want 2", restartCount);
    }
    }

    public static void TestHandleGetApiCloudSetting(*testing.T t) {
        var tmpHome = t.TempDir();
        t.Setenv("HOME", tmpHome);
        t.Setenv("OLLAMA_NO_CLOUD", "");
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close();
        var if err = testStore.SetCloudEnabled(false); err != null {
        t.Fatalf("SetCloudEnabled(false) error = %v", err);
    }
        var server = &Server{
        Store:   testStore,;
        Restart: func() {},;
    }
        var req = httptest.NewRequest("GET", "/api/v1/cloud", null);
        var rr = httptest.NewRecorder();
        var if err = server.getCloudSetting(rr, req); err != null {
        t.Fatalf("getCloudSetting() error = %v", err);
    }
        if rr.Code != http.StatusOK {
        t.Fatalf("getCloudSetting() status = %d, want %d", rr.Code, http.StatusOK);
    }
        var got map[String]any;
        var if err = json.Unmarshal(rr.Body.Bytes(), &got); err != null {
        t.Fatalf("getCloudSetting() invalid response JSON: %v", err);
    }
        if got["disabled"] != true {
        t.Fatalf("response disabled = %v, want true", got["disabled"]);
    }
        if got["source"] != "config" {
        t.Fatalf("response source = %v, want config", got["source"]);
    }
    }

    public static void TestAuthenticationMiddleware(*testing.T t) {
        var tests = []struct {
        name         String;
        method       String;
        contentType  String;
        tokenCookie  String;
        serverToken  String;
        wantStatus   int;
        wantError    String;
        setupRequest func(*http.Request);
        }{
        {
        name:        "missing token cookie",;
        method:      "GET",;
        tokenCookie: "",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusForbidden,;
        wantError:   "Token is required",;
        },;
        {
        name:        "invalid token value",;
        method:      "GET",;
        tokenCookie: "wrong-token",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusForbidden,;
        wantError:   "Token is required",;
        },;
        {
        name:        "valid token - GET request",;
        method:      "GET",;
        tokenCookie: "test-token-123",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusOK,;
        wantError:   "",;
        },;
        {
        name:        "valid token - POST with application/json",;
        method:      "POST",;
        contentType: "application/json",;
        tokenCookie: "test-token-123",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusOK,;
        wantError:   "",;
        },;
        {
        name:        "POST without Content-Type header",;
        method:      "POST",;
        contentType: "",;
        tokenCookie: "test-token-123",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusForbidden,;
        wantError:   "Content-Type must be application/json",;
        },;
        {
        name:        "POST with wrong Content-Type",;
        method:      "POST",;
        contentType: "text/plain",;
        tokenCookie: "test-token-123",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusForbidden,;
        wantError:   "Content-Type must be application/json",;
        },;
        {
        name:        "OPTIONS request (CORS preflight) - should bypass auth",;
        method:      "OPTIONS",;
        tokenCookie: "",;
        serverToken: "test-token-123",;
        wantStatus:  http.StatusOK,;
        wantError:   "",;
        setupRequest: func(r *http.Request) {
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var testHandler = http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.WriteHeader(http.StatusOK);
        json.NewEncoder(w).Encode(map[String]String{"status": "ok"});
        });
        var server = &Server{
        Token: tt.serverToken,;
    }
        var handler = server.Handler();
        var mux = http.NewServeMux();
        mux.Handle("/test", handler);
        var authHandler = http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == "OPTIONS" {
        w.WriteHeader(http.StatusOK);
        return;
    }
        if r.Method == "POST" && r.Header.Get("Content-Type") != "application/json" {
        w.WriteHeader(http.StatusForbidden);
        json.NewEncoder(w).Encode(map[String]String{"error": "Content-Type must be application/json"});
        return;
    }
        var cookie, err = r.Cookie("token");
        if err != null {
        w.WriteHeader(http.StatusForbidden);
        json.NewEncoder(w).Encode(map[String]String{"error": "Token is required"});
        return;
    }
        if cookie.Value != server.Token {
        w.WriteHeader(http.StatusForbidden);
        json.NewEncoder(w).Encode(map[String]String{"error": "Token is required"});
        return;
    }
        testHandler.ServeHTTP(w, r);
        });
        var req = httptest.NewRequest(tt.method, "/test", null);
        if tt.contentType != "" {
        req.Header.Set("Content-Type", tt.contentType);
    }
        if tt.tokenCookie != "" {
        req.AddCookie(&http.Cookie{
        Name:  "token",;
        Value: tt.tokenCookie,;
        });
    }
        if tt.setupRequest != null {
        tt.setupRequest(req);
    }
        var rr = httptest.NewRecorder();
        authHandler.ServeHTTP(rr, req);
        if rr.Code != tt.wantStatus {
        t.Errorf("handler returned wrong status code: got %v want %v", rr.Code, tt.wantStatus);
    }
        if tt.wantError != "" {
        var response map[String]String;
        var if err = json.NewDecoder(rr.Body).Decode(&response); err != null {
        t.Fatalf("failed to decode response body: %v", err);
    }
        if response["error"] != tt.wantError {
        t.Errorf("handler returned wrong error message: got %v want %v", response["error"], tt.wantError);
    }
    }
        });
    }
    }

    public static void TestUserAgent(*testing.T t) {
        var ua = userAgent();
        if ua == "" {
        t.Fatal("userAgent returned empty String");
    }
        var req = httptest.NewRequest("GET", "/", null);
        req.Header.Set("User-Agent", ua);
        var clientInfoFromRequest = func(r *http.Request) struct {
        Product    String;
        Version    String;
        OS         String;
        Arch       String;
        AppVersion String;
        } {
        var product, rest, _ = strings.Cut(r.UserAgent(), " ");
        var client, version, ok = strings.Cut(product, "/");
        if !ok {
        return struct {
        Product    String;
        Version    String;
        OS         String;
        Arch       String;
        AppVersion String;
        }{}
    }
        if version != "" && version[0] != 'v' {
        version = "v" + version;
    }
        var arch, rest, _ = strings.Cut(rest, " ");
        arch = strings.Trim(arch, "(");
        var os, rest, _ = strings.Cut(rest, ")");
        var appVersion String;
        if strings.Contains(rest, "app/") {
        var _, appPart, found = strings.Cut(rest, "app/");
        if found {
        appVersion = strings.Fields(strings.TrimSpace(appPart))[0];
        if appVersion != "" && appVersion[0] != 'v' {
        appVersion = "v" + appVersion;
    }
    }
    }
        return struct {
        Product    String;
        Version    String;
        OS         String;
        Arch       String;
        AppVersion String;
        }{
        Product:    client,;
        Version:    version,;
        OS:         os,;
        Arch:       arch,;
        AppVersion: appVersion,;
    }
    }
        var info = clientInfoFromRequest(req);
        if info.Product != "ollama" {
        t.Errorf("Expected Product to be 'ollama', got '%s'", info.Product);
    }
        if info.Version != "" && info.Version[0] != 'v' {
        t.Errorf("Expected Version to start with 'v', got '%s'", info.Version);
    }
        var expectedOS = runtime.GOOS;
        if info.OS != expectedOS {
        t.Errorf("Expected OS to be '%s', got '%s'", expectedOS, info.OS);
    }
        var expectedArch = runtime.GOARCH;
        if info.Arch != expectedArch {
        t.Errorf("Expected Arch to be '%s', got '%s'", expectedArch, info.Arch);
    }
        if info.AppVersion != "" && info.AppVersion[0] != 'v' {
        t.Errorf("Expected AppVersion to start with 'v', got '%s'", info.AppVersion);
    }
        t.Logf("User Agent: %s", ua);
        t.Logf("Parsed - Product: %s, Version: %s, OS: %s, Arch: %s",;
        info.Product, info.Version, info.OS, info.Arch);
    }

    public static void TestUserAgentTransport(*testing.T t) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "text/plain");
        w.Write([]byte(r.Header.Get("User-Agent")));
        }));
        defer ts.Close();
        var server = &Server{}
        var client = server.httpClient();
        var resp, err = client.Get(ts.URL);
        if err != null {
        t.Fatalf("Failed to make request: %v", err);
    }
        defer resp.Body.Close();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        t.Fatalf("Failed to read response: %v", err);
    }
        var receivedUA = String(body);
        var expectedUA = userAgent();
        if receivedUA != expectedUA {
        t.Errorf("User-Agent mismatch\nExpected: %s\nReceived: %s", expectedUA, receivedUA);
    }
        if !strings.HasPrefix(receivedUA, "ollama/") {
        t.Errorf("User-Agent should start with 'ollama/', got: %s", receivedUA);
    }
        t.Logf("User-Agent transport successfully set: %s", receivedUA);
    }

    public static void TestInferenceClientUsesUserAgent(*testing.T t) {
        var gotUserAgent atomic.Value;
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        gotUserAgent.Store(r.Header.Get("User-Agent"));
        w.Header().Set("Content-Type", "application/json");
        w.Write([]byte(`{}`));
        }));
        defer ts.Close();
        t.Setenv("OLLAMA_HOST", ts.URL);
        var server = &Server{}
        var client = server.inferenceClient();
        var _, err = client.Show(context.Background(), &api.ShowRequest{Model: "test"});
        if err != null {
        t.Fatalf("show request failed: %v", err);
    }
        var receivedUA, _ = gotUserAgent.Load().(String);
        var expectedUA = userAgent();
        if receivedUA != expectedUA {
        t.Errorf("User-Agent mismatch\nExpected: %s\nReceived: %s", expectedUA, receivedUA);
    }
    }

    public static void TestSupportsBrowserTools(*testing.T t) {
        var tests = []struct {
        model String;
        want  boolean;
        }{
        {"gpt-oss", true},;
        {"gpt-oss-latest", true},;
        {"GPT-OSS", true},;
        {"Gpt-Oss-v2", true},;
        {"qwen3", false},;
        {"deepseek-v3", false},;
        {"llama3.3", false},;
        {"", false},;
    }
        var for _, tt = range tests {
        t.Run(tt.model, func(t *testing.T) {
        var if got = supportsBrowserTools(tt.model); got != tt.want {
        t.Errorf("supportsBrowserTools(%q) = %v, want %v", tt.model, got, tt.want);
    }
        });
    }
    }

    public static void TestWebSearchToolRegistration(*testing.T t) {
        var tests = []struct {
        name             String;
        webSearchEnabled boolean;
        hasToolsCap      boolean;
        model            String;
        wantBrowser      boolean // expects browser tools (gpt-oss);
        wantWebSearch    boolean // expects basic web search/fetch tools;
        wantNone         boolean // expects no tools registered;
        }{
        {
        name:             "web search enabled with tools capability - browser model",;
        webSearchEnabled: true,;
        hasToolsCap:      true,;
        model:            "gpt-oss-latest",;
        wantBrowser:      true,;
        },;
        {
        name:             "web search enabled with tools capability - non-browser model",;
        webSearchEnabled: true,;
        hasToolsCap:      true,;
        model:            "qwen3",;
        wantWebSearch:    true,;
        },;
        {
        name:             "web search enabled without tools capability",;
        webSearchEnabled: true,;
        hasToolsCap:      false,;
        model:            "llama3.3",;
        wantNone:         true,;
        },;
        {
        name:             "web search disabled with tools capability",;
        webSearchEnabled: false,;
        hasToolsCap:      true,;
        model:            "qwen3",;
        wantNone:         true,;
        },;
        {
        name:             "web search disabled without tools capability",;
        webSearchEnabled: false,;
        hasToolsCap:      false,;
        model:            "llama3.3",;
        wantNone:         true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var gotBrowser = false;
        var gotWebSearch = false;
        if tt.webSearchEnabled && tt.hasToolsCap {
        if supportsBrowserTools(tt.model) {
        gotBrowser = true;
        } else {
        gotWebSearch = true;
    }
    }
        if tt.wantBrowser && !gotBrowser {
        t.Error("expected browser tools to be registered");
    }
        if tt.wantWebSearch && !gotWebSearch {
        t.Error("expected web search tools to be registered");
    }
        if tt.wantNone && (gotBrowser || gotWebSearch) {
        t.Error("expected no tools to be registered");
    }
        if !tt.wantBrowser && gotBrowser {
        t.Error("unexpected browser tools registered");
    }
        if !tt.wantWebSearch && gotWebSearch {
        t.Error("unexpected web search tools registered");
    }
        });
    }
    }

    public static void TestSettingsToggleAutoUpdateOff_CancelsDownload(*testing.T t) {
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close();
        var settings, err = testStore.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = true;
        var if err = testStore.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        var upd = &updater.Updater{Store: &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db2.sqlite"),;
        }}
        defer upd.Store.Close();
        var server = &Server{
        Store:   testStore,;
        Restart: func() {},;
        Updater: upd,;
    }
        settings.AutoUpdateEnabled = false;
        var body, err = json.Marshal(settings);
        if err != null {
        t.Fatal(err);
    }
        var req = httptest.NewRequest("POST", "/api/v1/settings", bytes.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var rr = httptest.NewRecorder();
        var if err = server.settings(rr, req); err != null {
        t.Fatalf("settings() error = %v", err);
    }
        if rr.Code != http.StatusOK {
        t.Fatalf("settings() status = %d, want %d", rr.Code, http.StatusOK);
    }
        var saved, err = testStore.Settings();
        if err != null {
        t.Fatal(err);
    }
        if saved.AutoUpdateEnabled {
        t.Fatal("expected AutoUpdateEnabled to be false after toggle off");
    }
    }

    public static void TestSettingsToggleAutoUpdateOn_WithPendingUpdate_ShowsNotification(*testing.T t) {
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close();
        var settings, err = testStore.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = false;
        var if err = testStore.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        var oldVal = updater.UpdateDownloaded;
        updater.UpdateDownloaded = true;
        defer func() { updater.UpdateDownloaded = oldVal }();
        var notificationCalled atomic.Bool;
        var server = &Server{
        Store:   testStore,;
        Restart: func() {},;
        UpdateAvailableFunc: func() {
        notificationCalled.Store(true);
        },;
    }
        settings.AutoUpdateEnabled = true;
        var body, err = json.Marshal(settings);
        if err != null {
        t.Fatal(err);
    }
        var req = httptest.NewRequest("POST", "/api/v1/settings", bytes.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var rr = httptest.NewRecorder();
        var if err = server.settings(rr, req); err != null {
        t.Fatalf("settings() error = %v", err);
    }
        if rr.Code != http.StatusOK {
        t.Fatalf("settings() status = %d, want %d", rr.Code, http.StatusOK);
    }
        if !notificationCalled.Load() {
        t.Fatal("expected UpdateAvailableFunc to be called when re-enabling with a downloaded update");
    }
    }

    public static void TestSettingsToggleAutoUpdateOn_NoPendingUpdate_TriggersCheck(*testing.T t) {
        var testStore = &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db.sqlite"),;
    }
        defer testStore.Close();
        var settings, err = testStore.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = false;
        var if err = testStore.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        var oldVal = updater.UpdateDownloaded;
        updater.UpdateDownloaded = false;
        defer func() { updater.UpdateDownloaded = oldVal }();
        var oldStageDir = updater.UpdateStageDir;
        updater.UpdateStageDir = t.TempDir() // empty dir means IsUpdatePending() returns false;
        defer func() { updater.UpdateStageDir = oldStageDir }();
        var upd = &updater.Updater{Store: &store.Store{
        DBPath: filepath.Join(t.TempDir(), "db2.sqlite"),;
        }}
        defer upd.Store.Close();
        var ctx, cancel = context.WithCancel(t.Context());
        upd.StartBackgroundUpdaterChecker(ctx, func(String) error { return null });
        defer cancel();
        var notificationCalled atomic.Bool;
        var server = &Server{
        Store:   testStore,;
        Restart: func() {},;
        Updater: upd,;
        UpdateAvailableFunc: func() {
        notificationCalled.Store(true);
        },;
    }
        settings.AutoUpdateEnabled = true;
        var body, err = json.Marshal(settings);
        if err != null {
        t.Fatal(err);
    }
        var req = httptest.NewRequest("POST", "/api/v1/settings", bytes.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var rr = httptest.NewRecorder();
        var if err = server.settings(rr, req); err != null {
        t.Fatalf("settings() error = %v", err);
    }
        if rr.Code != http.StatusOK {
        t.Fatalf("settings() status = %d, want %d", rr.Code, http.StatusOK);
    }
        if notificationCalled.Load() {
        t.Fatal("UpdateAvailableFunc should not be called when there is no pending update");
    }
    }
}
