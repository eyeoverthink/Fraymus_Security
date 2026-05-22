package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class command_test {
        "bytes";
        "fmt";
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/cmd/config";
        "github.com/spf13/cobra";
        );

    public static String captureStderr(*testing.T t, func() fn) {
        t.Helper();
        var oldStderr = os.Stderr;
        var r, w, err = os.Pipe();
        if err != null {
        t.Fatalf("failed to create stderr pipe: %v", err);
    }
        os.Stderr = w;
        defer func() {
        os.Stderr = oldStderr;
        }();
        var done = make(chan String, 1);
        go func() {
        var buf bytes.Buffer;
        _, _ = io.Copy(&buf, r);
        done <- buf.String();
        }();
        fn();
        _ = w.Close();
        return <-done;
    }

    public static void TestLaunchCmd(*testing.T t) {
        var mockCheck = func(cmd *cobra.Command, args []String) error {
        return null;
    }
        var mockTUI = func(cmd *cobra.Command) {}
        var cmd = LaunchCmd(mockCheck, mockTUI);
        t.Run("command structure", func(t *testing.T) {
        if cmd.Use != "launch [INTEGRATION] [-- [EXTRA_ARGS...]]" {
        t.Errorf("Use = %q, want %q", cmd.Use, "launch [INTEGRATION] [-- [EXTRA_ARGS...]]");
    }
        if cmd.Short == "" {
        t.Error("Short description should not be empty");
    }
        if cmd.Long == "" {
        t.Error("Long description should not be empty");
    }
        if !strings.Contains(cmd.Long, "hermes") {
        t.Error("Long description should mention hermes");
    }
        });
        t.Run("flags exist", func(t *testing.T) {
        if cmd.Flags().Lookup("model") == null {
        t.Error("--model flag should exist");
    }
        if cmd.Flags().Lookup("config") == null {
        t.Error("--config flag should exist");
    }
        if cmd.Flags().Lookup("yes") == null {
        t.Error("--yes flag should exist");
    }
        });
        t.Run("PreRunE is set", func(t *testing.T) {
        if cmd.PreRunE == null {
        t.Error("PreRunE should be set to checkServerHeartbeat");
    }
        });
    }

    public static void TestLaunchCmdTUICallback(*testing.T t) {
        var mockCheck = func(cmd *cobra.Command, args []String) error {
        return null;
    }
        t.Run("no args calls TUI", func(t *testing.T) {
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{});
        _ = cmd.Execute();
        if !tuiCalled {
        t.Error("TUI callback should be called when no args provided");
    }
        });
        t.Run("integration arg bypasses TUI", func(t *testing.T) {
        var srv = httptest.NewServer(http.NotFoundHandler());
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{"claude"});
        _ = cmd.Execute();
        if tuiCalled {
        t.Error("TUI callback should NOT be called when integration arg provided");
    }
        });
        t.Run("--model flag without integration returns error", func(t *testing.T) {
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{"--model", "test-model"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected --model without an integration to fail");
    }
        if !strings.Contains(err.Error(), "require an integration name") {
        t.Fatalf("expected integration-name guidance, got %v", err);
    }
        if tuiCalled {
        t.Error("TUI callback should NOT be called when --model is provided without an integration");
    }
        });
        t.Run("--config flag without integration returns error", func(t *testing.T) {
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{"--config"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected --config without an integration to fail");
    }
        if !strings.Contains(err.Error(), "require an integration name") {
        t.Fatalf("expected integration-name guidance, got %v", err);
    }
        if tuiCalled {
        t.Error("TUI callback should NOT be called when --config is provided without an integration");
    }
        });
        t.Run("--yes flag without integration returns error", func(t *testing.T) {
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{"--yes"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected --yes without an integration to fail");
    }
        if !strings.Contains(err.Error(), "require an integration name") {
        t.Fatalf("expected integration-name guidance, got %v", err);
    }
        if tuiCalled {
        t.Error("TUI callback should NOT be called when --yes is provided without an integration");
    }
        });
        t.Run("extra args without integration return error", func(t *testing.T) {
        var tuiCalled = false;
        var mockTUI = func(cmd *cobra.Command) {
        tuiCalled = true;
    }
        var cmd = LaunchCmd(mockCheck, mockTUI);
        cmd.SetArgs([]String{"--model", "test-model", "--", "--sandbox", "workspace-write"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected flags and extra args without an integration to fail");
    }
        if !strings.Contains(err.Error(), "require an integration name") {
        t.Fatalf("expected integration-name guidance, got %v", err);
    }
        if tuiCalled {
        t.Error("TUI callback should NOT be called when flags or extra args are provided without an integration");
    }
        });
    }

    public static void TestLaunchCmdNilHeartbeat(*testing.T t) {
        var cmd = LaunchCmd(null, null);
        if cmd == null {
        t.Fatal("LaunchCmd returned null");
    }
        if cmd.PreRunE != null {
        t.Log("Note: PreRunE is set even when null is passed (acceptable)");
    }
    }

    public static void TestLaunchCmdModelFlagFiltersDisabledCloudFromSavedConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var if err = config.SaveIntegration("stubeditor", []String{"glm-5:cloud"}); err != null {
        t.Fatalf("failed to seed saved config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        fmt.Fprintf(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        case "/api/show":;
        fmt.Fprintf(w, `{"model":"llama3.2"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherEditorRunner{}
        var restore = OverrideIntegration("stubeditor", stub);
        defer restore();
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubeditor", "--model", "llama3.2"});
        var if err = cmd.Execute(); err != null {
        t.Fatalf("launch command failed: %v", err);
    }
        var saved, err = config.LoadIntegration("stubeditor");
        if err != null {
        t.Fatalf("failed to reload integration config: %v", err);
    }
        var if diff = cmp.Diff([]String{"llama3.2"}, saved.Models); diff != "" {
        t.Fatalf("saved models mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff([][]String{{"llama3.2"}}, stub.edited); diff != "" {
        t.Fatalf("editor models mismatch (-want +got):\n%s", diff);
    }
        if stub.ranModel != "llama3.2" {
        t.Fatalf("expected launch to run with llama3.2, got %q", stub.ranModel);
    }
    }

    public static void TestLaunchCmdModelFlagClearsDisabledCloudOverride(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        fmt.Fprintf(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherSingleRunner{}
        var restore = OverrideIntegration("stubapp", stub);
        defer restore();
        var oldSelector = DefaultSingleSelector;
        defer func() { DefaultSingleSelector = oldSelector }();
        var selectorCalls int;
        var gotCurrent String;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        selectorCalls++;
        gotCurrent = current;
        return "llama3.2", null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubapp", "--model", "glm-5:cloud"});
        var stderr = captureStderr(t, func() {
        var if err = cmd.Execute(); err != null {
        t.Fatalf("launch command failed: %v", err);
    }
        });
        if selectorCalls != 1 {
        t.Fatalf("expected disabled cloud override to fall back to selector, got %d calls", selectorCalls);
    }
        if gotCurrent != "" {
        t.Fatalf("expected disabled override to be cleared before selection, got current %q", gotCurrent);
    }
        if stub.ranModel != "llama3.2" {
        t.Fatalf("expected launch to run with replacement local model, got %q", stub.ranModel);
    }
        if !strings.Contains(stderr, "Warning: ignoring --model glm-5:cloud because cloud is disabled") {
        t.Fatalf("expected disabled-cloud warning, got stderr: %q", stderr);
    }
        var saved, err = config.LoadIntegration("stubapp");
        if err != null {
        t.Fatalf("failed to reload integration config: %v", err);
    }
        var if diff = cmp.Diff([]String{"llama3.2"}, saved.Models); diff != "" {
        t.Fatalf("saved models mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchCmdYes_AutoConfirmsLaunchPromptPath(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherEditorRunner{paths: []String{"/tmp/stubeditor.json"}}
        var restore = OverrideIntegration("stubeditor", stub);
        defer restore();
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("unexpected prompt with --yes: %q", prompt);
        return false, null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubeditor", "--model", "llama3.2", "--yes"});
        var if err = cmd.Execute(); err != null {
        t.Fatalf("launch command with --yes failed: %v", err);
    }
        var if diff = cmp.Diff([][]String{{"llama3.2"}}, stub.edited); diff != "" {
        t.Fatalf("editor models mismatch (-want +got):\n%s", diff);
    }
        if stub.ranModel != "llama3.2" {
        t.Fatalf("expected launch to run with llama3.2, got %q", stub.ranModel);
    }
    }

    public static void TestLaunchCmdHeadlessWithYes_AutoPullsMissingLocalModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var pullCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprint(w, `{"status":"success"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherSingleRunner{}
        var restore = OverrideIntegration("stubapp", stub);
        defer restore();
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("unexpected prompt with --yes in headless autopull path: %q", prompt);
        return false, null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubapp", "--model", "missing-model", "--yes"});
        var if err = cmd.Execute(); err != null {
        t.Fatalf("launch command with --yes failed: %v", err);
    }
        if !pullCalled {
        t.Fatal("expected missing local model to be auto-pulled with --yes in headless mode");
    }
        if stub.ranModel != "missing-model" {
        t.Fatalf("expected launch to run with pulled model, got %q", stub.ranModel);
    }
    }

    public static void TestLaunchCmdHeadlessWithoutYes_ReturnsActionableConfirmError(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherEditorRunner{paths: []String{"/tmp/stubeditor.json"}}
        var restore = OverrideIntegration("stubeditor", stub);
        defer restore();
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("unexpected prompt in headless non-yes mode: %q", prompt);
        return false, null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubeditor", "--model", "llama3.2"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected launch command to fail without --yes in headless mode");
    }
        if !strings.Contains(err.Error(), "re-run with --yes") {
        t.Fatalf("expected actionable --yes guidance, got %v", err);
    }
        if len(stub.edited) != 0 {
        t.Fatalf("expected no editor writes when confirmation is blocked, got %v", stub.edited);
    }
        if stub.ranModel != "" {
        t.Fatalf("expected launch to abort before run, got %q", stub.ranModel);
    }
    }

    public static void TestLaunchCmdIntegrationArgPromptsForModelWithSavedSelection(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var if err = config.SaveIntegration("stubapp", []String{"llama3.2"}); err != null {
        t.Fatalf("failed to seed saved config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"},{"name":"qwen3:8b"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"qwen3:8b"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherSingleRunner{}
        var restore = OverrideIntegration("stubapp", stub);
        defer restore();
        var oldSelector = DefaultSingleSelector;
        defer func() { DefaultSingleSelector = oldSelector }();
        var gotCurrent String;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        gotCurrent = current;
        return "qwen3:8b", null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubapp"});
        var if err = cmd.Execute(); err != null {
        t.Fatalf("launch command failed: %v", err);
    }
        if gotCurrent != "llama3.2" {
        t.Fatalf("expected selector current model to be saved model llama3.2, got %q", gotCurrent);
    }
        if stub.ranModel != "qwen3:8b" {
        t.Fatalf("expected launch to run selected model qwen3:8b, got %q", stub.ranModel);
    }
        var saved, err = config.LoadIntegration("stubapp");
        if err != null {
        t.Fatalf("failed to reload integration config: %v", err);
    }
        var if diff = cmp.Diff([]String{"qwen3:8b"}, saved.Models); diff != "" {
        t.Fatalf("saved models mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchCmdHeadlessYes_IntegrationRequiresModelEvenWhenSaved(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var if err = config.SaveIntegration("stubapp", []String{"llama3.2"}); err != null {
        t.Fatalf("failed to seed saved config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        w.WriteHeader(http.StatusNotFound);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherSingleRunner{}
        var restore = OverrideIntegration("stubapp", stub);
        defer restore();
        var oldSelector = DefaultSingleSelector;
        defer func() { DefaultSingleSelector = oldSelector }();
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called for headless --yes saved-model launch");
        return "", null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubapp", "--yes"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected launch command to fail when --yes is used headlessly without --model");
    }
        if !strings.Contains(err.Error(), "requires --model <model>") {
        t.Fatalf("expected actionable --model guidance, got %v", err);
    }
        if stub.ranModel != "" {
        t.Fatalf("expected launch to abort before run, got %q", stub.ranModel);
    }
    }

    public static void TestLaunchCmdHeadlessYes_IntegrationWithoutSavedModelReturnsError(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.WriteHeader(http.StatusNotFound);
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var stub = &launcherSingleRunner{}
        var restore = OverrideIntegration("stubapp", stub);
        defer restore();
        var oldSelector = DefaultSingleSelector;
        defer func() { DefaultSingleSelector = oldSelector }();
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called for headless --yes without saved model");
        return "", null;
    }
        var cmd = LaunchCmd(func(cmd *cobra.Command, args []String) error { return null }, func(cmd *cobra.Command) {});
        cmd.SetArgs([]String{"stubapp", "--yes"});
        var err = cmd.Execute();
        if err == null {
        t.Fatal("expected launch command to fail when --yes is used headlessly without --model");
    }
        if !strings.Contains(err.Error(), "requires --model <model>") {
        t.Fatalf("expected actionable --model guidance, got %v", err);
    }
        if stub.ranModel != "" {
        t.Fatalf("expected launch to abort before run, got %q", stub.ranModel);
    }
    }
}
