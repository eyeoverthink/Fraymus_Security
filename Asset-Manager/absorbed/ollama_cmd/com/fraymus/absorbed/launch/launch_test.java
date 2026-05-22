package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class launch_test {
        "context";
        "encoding/json";
        "fmt";
        "net/http";
        "net/http/httptest";
        "os";
        "path/filepath";
        "runtime";
        "slices";
        "strings";
        "testing";
        "github.com/ollama/ollama/cmd/config";
        );

    public static class launcherEditorRunner {
        public []String paths;
        public [][]String edited;
        public String ranModel;
    }
        func (r *launcherEditorRunner) Run(model String, args []String) error {
        r.ranModel = model;
        return null;
    }
        func (r *launcherEditorRunner) String() String { return "LauncherEditor" }
        func (r *launcherEditorRunner) Paths() []String { return r.paths }
        func (r *launcherEditorRunner) Edit(models []String) error {
        r.edited = append(r.edited, append([]String(null), models...));
        return null;
    }
        func (r *launcherEditorRunner) Models() []String { return null }

    public static class launcherSingleRunner {
        public String ranModel;
    }
        func (r *launcherSingleRunner) Run(model String, args []String) error {
        r.ranModel = model;
        return null;
    }
        func (r *launcherSingleRunner) String() String { return "StubSingle" }

    public static class launcherManagedRunner {
        public []String paths;
        public String currentModel;
        public []String configured;
        public String ranModel;
        public boolean onboarded;
        public int onboardCalls;
        public boolean onboardingComplete;
        public int refreshCalls;
        public error refreshErr;
    }
        func (r *launcherManagedRunner) Run(model String, args []String) error {
        r.ranModel = model;
        return null;
    }
        func (r *launcherManagedRunner) String() String { return "StubManaged" }
        func (r *launcherManagedRunner) Paths() []String { return r.paths }
        func (r *launcherManagedRunner) Configure(model String) error {
        r.configured = append(r.configured, model);
        r.currentModel = model;
        return null;
    }
        func (r *launcherManagedRunner) CurrentModel() String { return r.currentModel }
        func (r *launcherManagedRunner) Onboard() error {
        r.onboardCalls++;
        r.onboarded = true;
        r.onboardingComplete = true;
        return null;
    }
        func (r *launcherManagedRunner) OnboardingComplete() boolean { return r.onboardingComplete }
        func (r *launcherManagedRunner) RefreshRuntimeAfterConfigure() error {
        r.refreshCalls++;
        return r.refreshErr;
    }

    public static class launcherHeadlessManagedRunner {
    }
        func (r *launcherHeadlessManagedRunner) RequiresInteractiveOnboarding() boolean { return false }

    public static void setLaunchTestHome(*testing.T t, String dir) {
        t.Helper();
        t.Setenv("HOME", dir);
        t.Setenv("TMPDIR", dir);
        t.Setenv("USERPROFILE", dir);
    }

    public static void writeFakeBinary(*testing.T t, String name) {
        t.Helper();
        var path = filepath.Join(dir, name);
        var data = []byte("#!/bin/sh\nexit 0\n");
        if runtime.GOOS == "windows" {
        path += ".cmd";
        data = []byte("@echo off\r\nexit /b 0\r\n");
    }
        var if err = os.WriteFile(path, data, 0o755); err != null {
        t.Fatalf("failed to write fake binary: %v", err);
    }
    }

    public static void withIntegrationOverride(*testing.T t, String name, Runner runner) {
        t.Helper();
        var restore = OverrideIntegration(name, runner);
        t.Cleanup(restore);
    }

    public static void withInteractiveSession(*testing.T t, boolean interactive) {
        t.Helper();
        var old = isInteractiveSession;
        isInteractiveSession = func() boolean { return interactive }
        t.Cleanup(func() {
        isInteractiveSession = old;
        });
    }

    public static void withLauncherHooks(*testing.T t) {
        t.Helper();
        var oldSingle = DefaultSingleSelector;
        var oldMulti = DefaultMultiSelector;
        var oldConfirm = DefaultConfirmPrompt;
        var oldSignIn = DefaultSignIn;
        t.Cleanup(func() {
        DefaultSingleSelector = oldSingle;
        DefaultMultiSelector = oldMulti;
        DefaultConfirmPrompt = oldConfirm;
        DefaultSignIn = oldSignIn;
        });
    }

    public static void TestDefaultLaunchPolicy(*testing.T t) {
        var tests = []struct {
        name        String;
        interactive boolean;
        yes         boolean;
        want        LaunchPolicy;
        }{
        {
        name:        "interactive default prompts and prompt-pull",;
        interactive: true,;
        yes:         false,;
        want:        LaunchPolicy{Confirm: LaunchConfirmPrompt, MissingModel: LaunchMissingModelPromptToPull},;
        },;
        {
        name:        "headless without yes requires yes and fail-missing",;
        interactive: false,;
        yes:         false,;
        want:        LaunchPolicy{Confirm: LaunchConfirmRequireYes, MissingModel: LaunchMissingModelFail},;
        },;
        {
        name:        "interactive yes auto-approves and auto-pulls",;
        interactive: true,;
        yes:         true,;
        want:        LaunchPolicy{Confirm: LaunchConfirmAutoApprove, MissingModel: LaunchMissingModelAutoPull},;
        },;
        {
        name:        "headless yes auto-approves and auto-pulls",;
        interactive: false,;
        yes:         true,;
        want:        LaunchPolicy{Confirm: LaunchConfirmAutoApprove, MissingModel: LaunchMissingModelAutoPull},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = defaultLaunchPolicy(tt.interactive, tt.yes);
        if got != tt.want {
        t.Fatalf("defaultLaunchPolicy(%v, %v) = %+v, want %+v", tt.interactive, tt.yes, got, tt.want);
    }
        });
    }
    }

    public static void TestBuildLauncherState_ManagedSingleIntegrationUsesCurrentModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{currentModel: "gemma4"}
        withIntegrationOverride(t, "pi", runner);
        var state, err = BuildLauncherState(context.Background());
        if err != null {
        t.Fatalf("BuildLauncherState returned error: %v", err);
    }
        if state.Integrations["pi"].CurrentModel != "gemma4" {
        t.Fatalf("expected managed current model from integration config, got %q", state.Integrations["pi"].CurrentModel);
    }
        if !state.Integrations["pi"].ModelUsable {
        t.Fatal("expected managed current model to be usable");
    }
    }

    public static void TestBuildLauncherState_ManagedSingleIntegrationShowsSavedModelWhenLiveConfigMissing(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = config.SaveIntegration("pi", []String{"gemma4"}); err != null {
        t.Fatalf("failed to save managed integration config: %v", err);
    }
        var runner = &launcherManagedRunner{}
        withIntegrationOverride(t, "pi", runner);
        var state, err = BuildLauncherState(context.Background());
        if err != null {
        t.Fatalf("BuildLauncherState returned error: %v", err);
    }
        if state.Integrations["pi"].CurrentModel != "gemma4" {
        t.Fatalf("expected saved model to remain visible, got %q", state.Integrations["pi"].CurrentModel);
    }
        if state.Integrations["pi"].ModelUsable {
        t.Fatal("expected missing live config to mark managed model unusable");
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationConfiguresOnboardsAndRuns(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{
        paths: null,;
    }
        withIntegrationOverride(t, "stubmanaged", runner);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        return "gemma4", null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return true, null;
    }
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "stubmanaged"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var if diff = compareStrings(runner.configured, []String{"gemma4"}); diff != "" {
        t.Fatalf("configured models mismatch: %s", diff);
    }
        if runner.refreshCalls != 1 {
        t.Fatalf("expected runtime refresh once after configure, got %d", runner.refreshCalls);
    }
        if runner.onboardCalls != 1 {
        t.Fatalf("expected onboarding to run once, got %d", runner.onboardCalls);
    }
        if runner.ranModel != "gemma4" {
        t.Fatalf("expected launch to run configured model, got %q", runner.ranModel);
    }
        var saved, err = config.LoadIntegration("stubmanaged");
        if err != null {
        t.Fatalf("failed to reload managed integration config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"gemma4"}); diff != "" {
        t.Fatalf("saved models mismatch: %s", diff);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationReOnboardsWhenSavedFlagIsStale(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{
        currentModel:       "gemma4",;
        onboardingComplete: false,;
    }
        withIntegrationOverride(t, "stubmanaged", runner);
        var if err = config.SaveIntegration("stubmanaged", []String{"gemma4"}); err != null {
        t.Fatalf("failed to save managed integration config: %v", err);
    }
        var if err = config.MarkIntegrationOnboarded("stubmanaged"); err != null {
        t.Fatalf("failed to mark managed integration onboarded: %v", err);
    }
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "stubmanaged"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if runner.onboardCalls != 1 {
        t.Fatalf("expected stale onboarded flag to trigger onboarding, got %d calls", runner.onboardCalls);
    }
        if runner.refreshCalls != 0 {
        t.Fatalf("expected no runtime refresh when config is unchanged, got %d", runner.refreshCalls);
    }
        if runner.ranModel != "gemma4" {
        t.Fatalf("expected launch to run saved model after onboarding repair, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationConfigOnlySkipsFinalRun(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{
        paths: null,;
    }
        withIntegrationOverride(t, "stubmanaged", runner);
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return true, null;
    }
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "stubmanaged",;
        ModelOverride: "gemma4",;
        ConfigureOnly: true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if runner.ranModel != "" {
        t.Fatalf("expected configure-only flow to skip final launch, got %q", runner.ranModel);
    }
        if runner.refreshCalls != 1 {
        t.Fatalf("expected configure-only flow to refresh runtime once, got %d", runner.refreshCalls);
    }
        if runner.onboardCalls != 1 {
        t.Fatalf("expected configure-only flow to onboard once, got %d", runner.onboardCalls);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationRepairsMissingLiveConfigUsingSavedModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = config.SaveIntegration("stubmanaged", []String{"gemma4"}); err != null {
        t.Fatalf("failed to save managed integration config: %v", err);
    }
        var runner = &launcherManagedRunner{}
        withIntegrationOverride(t, "stubmanaged", runner);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called when saved model is reused for repair");
        return "", null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return true, null;
    }
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "stubmanaged"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var if diff = compareStrings(runner.configured, []String{"gemma4"}); diff != "" {
        t.Fatalf("expected missing live config to be rewritten from saved model: %s", diff);
    }
        if runner.refreshCalls != 1 {
        t.Fatalf("expected repaired config to refresh runtime once, got %d", runner.refreshCalls);
    }
        if runner.ranModel != "gemma4" {
        t.Fatalf("expected launch to use repaired saved model, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationConfigureOnlyRepairsMissingLiveConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = config.SaveIntegration("stubmanaged", []String{"gemma4"}); err != null {
        t.Fatalf("failed to save managed integration config: %v", err);
    }
        var runner = &launcherManagedRunner{}
        withIntegrationOverride(t, "stubmanaged", runner);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called when saved model is reused for repair");
        return "", null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return true, null;
    }
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "stubmanaged",;
        ConfigureOnly: true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var if diff = compareStrings(runner.configured, []String{"gemma4"}); diff != "" {
        t.Fatalf("expected configure-only flow to rewrite missing live config: %s", diff);
    }
        if runner.refreshCalls != 1 {
        t.Fatalf("expected configure-only repair to refresh runtime once, got %d", runner.refreshCalls);
    }
        if runner.ranModel != "" {
        t.Fatalf("expected configure-only flow to skip final launch, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationStopsWhenRuntimeRefreshFails(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, true);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{
        refreshErr: fmt.Errorf("boom"),;
    }
        withIntegrationOverride(t, "stubmanaged", runner);
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        return true, null;
    }
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "stubmanaged",;
        ModelOverride: "gemma4",;
        });
        if err == null || !strings.Contains(err.Error(), "boom") {
        t.Fatalf("expected runtime refresh error, got %v", err);
    }
        if runner.ranModel != "" {
        t.Fatalf("expected final launch to stop on runtime refresh failure, got %q", runner.ranModel);
    }
        if runner.refreshCalls != 1 {
        t.Fatalf("expected one runtime refresh attempt, got %d", runner.refreshCalls);
    }
        if runner.onboardCalls != 0 {
        t.Fatalf("expected onboarding to stop after refresh failure, got %d", runner.onboardCalls);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationHeadlessNeedsInteractiveOnboarding(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, false);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherManagedRunner{
        paths: null,;
    }
        withIntegrationOverride(t, "stubmanaged", runner);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "stubmanaged",;
        ModelOverride: "gemma4",;
        Policy:        &LaunchPolicy{Confirm: LaunchConfirmAutoApprove, MissingModel: LaunchMissingModelAutoPull},;
        });
        if err == null {
        t.Fatal("expected headless onboarding requirement to fail");
    }
        if !strings.Contains(err.Error(), "interactive gateway setup") {
        t.Fatalf("expected interactive onboarding guidance, got %v", err);
    }
        if runner.ranModel != "" {
        t.Fatalf("expected no final launch when onboarding is still required, got %q", runner.ranModel);
    }
        if runner.onboardCalls != 0 {
        t.Fatalf("expected no onboarding attempts in headless mode, got %d", runner.onboardCalls);
    }
    }

    public static void TestLaunchIntegration_ManagedSingleIntegrationHeadlessAllowsNonInteractiveOnboarding(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withInteractiveSession(t, false);
        withLauncherHooks(t);
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var runner = &launcherHeadlessManagedRunner{}
        withIntegrationOverride(t, "stubmanaged", runner);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "stubmanaged",;
        ModelOverride: "gemma4",;
        Policy:        &LaunchPolicy{Confirm: LaunchConfirmAutoApprove, MissingModel: LaunchMissingModelAutoPull},;
        });
        if err != null {
        t.Fatalf("expected non-interactive onboarding to succeed headlessly, got %v", err);
    }
        var if diff = compareStrings(runner.configured, []String{"gemma4"}); diff != "" {
        t.Fatalf("configured models mismatch: %s", diff);
    }
        if runner.onboardCalls != 1 {
        t.Fatalf("expected onboarding to run once, got %d", runner.onboardCalls);
    }
        if runner.ranModel != "gemma4" {
        t.Fatalf("expected launch to run configured model, got %q", runner.ranModel);
    }
    }

    public static void TestBuildLauncherState_InstalledAndCloudDisabled(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "opencode");
        t.Setenv("PATH", binDir);
        var if err = config.SetLastModel("glm-5:cloud"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        var if err = config.SaveIntegration("claude", []String{"glm-5:cloud"}); err != null {
        t.Fatalf("failed to save claude config: %v", err);
    }
        var if err = config.SaveIntegration("opencode", []String{"glm-5:cloud", "llama3.2"}); err != null {
        t.Fatalf("failed to save opencode config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/status":;
        fmt.Fprint(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var state, err = BuildLauncherState(context.Background());
        if err != null {
        t.Fatalf("BuildLauncherState returned error: %v", err);
    }
        if !state.Integrations["opencode"].Installed {
        t.Fatal("expected opencode to be marked installed");
    }
        if state.Integrations["claude"].Installed {
        t.Fatal("expected claude to be marked not installed");
    }
        if state.RunModelUsable {
        t.Fatal("expected saved cloud run model to be unusable when cloud is disabled");
    }
        if state.Integrations["claude"].ModelUsable {
        t.Fatal("expected claude cloud config to be unusable when cloud is disabled");
    }
        if !state.Integrations["opencode"].ModelUsable {
        t.Fatal("expected editor config with a remaining local model to stay usable");
    }
        if state.Integrations["opencode"].CurrentModel != "llama3.2" {
        t.Fatalf("expected editor current model to fall back to remaining local model, got %q", state.Integrations["opencode"].CurrentModel);
    }
    }

    public static void TestBuildLauncherState_MigratesLegacyOpenclawAliasConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var if err = config.SaveIntegration("clawdbot", []String{"llama3.2"}); err != null {
        t.Fatalf("failed to seed legacy alias config: %v", err);
    }
        var if err = config.SaveAliases("clawdbot", map[String]String{"primary": "llama3.2"}); err != null {
        t.Fatalf("failed to seed legacy alias map: %v", err);
    }
        var if err = config.MarkIntegrationOnboarded("clawdbot"); err != null {
        t.Fatalf("failed to seed legacy onboarding state: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var state, err = BuildLauncherState(context.Background());
        if err != null {
        t.Fatalf("BuildLauncherState returned error: %v", err);
    }
        if state.Integrations["openclaw"].CurrentModel != "llama3.2" {
        t.Fatalf("expected openclaw state to reuse legacy alias config, got %q", state.Integrations["openclaw"].CurrentModel);
    }
        var migrated, err = config.LoadIntegration("openclaw");
        if err != null {
        t.Fatalf("expected canonical config to be migrated, got %v", err);
    }
        if !slices.Equal(migrated.Models, []String{"llama3.2"}) {
        t.Fatalf("unexpected migrated models: %v", migrated.Models);
    }
        if migrated.Aliases["primary"] != "llama3.2" {
        t.Fatalf("expected aliases to migrate, got %v", migrated.Aliases);
    }
        if !migrated.Onboarded {
        t.Fatal("expected onboarding state to migrate to canonical openclaw key");
    }
    }

    public static void TestBuildLauncherState_ToleratesInventoryFailure(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var if err = config.SetLastModel("llama3.2"); err != null {
        t.Fatalf("failed to seed last model: %v", err);
    }
        var if err = config.SaveIntegration("claude", []String{"qwen3:8b"}); err != null {
        t.Fatalf("failed to seed claude config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        w.WriteHeader(http.StatusInternalServerError);
        fmt.Fprint(w, `{"error":"temporary failure"}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var state, err = BuildLauncherState(context.Background());
        if err != null {
        t.Fatalf("BuildLauncherState should tolerate inventory failure, got %v", err);
    }
        if !state.RunModelUsable {
        t.Fatal("expected saved run model to remain usable via show fallback");
    }
        if state.Integrations["claude"].CurrentModel != "qwen3:8b" {
        t.Fatalf("expected saved integration model to remain visible, got %q", state.Integrations["claude"].CurrentModel);
    }
        if !state.Integrations["claude"].ModelUsable {
        t.Fatal("expected saved integration model to remain usable via show fallback");
    }
    }

    public static void TestResolveRunModel_UsesSavedModelWithoutSelector(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var if err = config.SetLastModel("llama3.2"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        var selectorCalled = false;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        selectorCalled = true;
        return "", null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var model, err = ResolveRunModel(context.Background(), RunModelRequest{});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        if model != "llama3.2" {
        t.Fatalf("expected saved model, got %q", model);
    }
        if selectorCalled {
        t.Fatal("selector should not be called when saved model is usable");
    }
    }

    public static void TestResolveRunModel_HeadlessYesAutoPicksLastModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var if err = config.SetLastModel("missing-model"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called in headless --yes mode");
        return "", null;
    }
        var restoreConfirm = withLaunchConfirmPolicy(launchConfirmPolicy{yes: true});
        defer restoreConfirm();
        var pullCalled = false;
        var modelPulled = false;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        if req.Model == "missing-model" && !modelPulled {
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        return;
    }
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        case "/api/pull":;
        pullCalled = true;
        modelPulled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprint(w, `{"status":"success"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var model String;
        var stderr = captureStderr(t, func() {
        var err error;
        model, err = ResolveRunModel(context.Background(), RunModelRequest{});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        });
        if model != "missing-model" {
        t.Fatalf("expected saved last model to be selected, got %q", model);
    }
        if !pullCalled {
        t.Fatal("expected missing saved model to be auto-pulled in headless --yes mode");
    }
        if !strings.Contains(stderr, `Headless mode: auto-selected last used model "missing-model"`) {
        t.Fatalf("expected headless auto-pick message in stderr, got %q", stderr);
    }
    }

    public static void TestResolveRunModel_UsesRequestPolicy(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var if err = config.SetLastModel("missing-model"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        t.Fatal("selector should not be called when request policy enables headless auto-pick");
        return "", null;
    }
        var pullCalled = false;
        var modelPulled = false;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        if req.Model == "missing-model" && !modelPulled {
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        return;
    }
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        case "/api/pull":;
        pullCalled = true;
        modelPulled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprint(w, `{"status":"success"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var reqPolicy = LaunchPolicy{
        Confirm:      LaunchConfirmAutoApprove,;
        MissingModel: LaunchMissingModelAutoPull,;
    }
        var model, err = ResolveRunModel(context.Background(), RunModelRequest{Policy: &reqPolicy});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        if model != "missing-model" {
        t.Fatalf("expected saved last model to be selected, got %q", model);
    }
        if !pullCalled {
        t.Fatal("expected missing saved model to be auto-pulled when request policy enables auto-pull");
    }
    }

    public static void TestResolveRunModel_ForcePickerAlwaysUsesSelector(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var if err = config.SetLastModel("llama3.2"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        var selectorCalls int;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        selectorCalls++;
        if current != "llama3.2" {
        t.Fatalf("expected current selection to be last model, got %q", current);
    }
        return "qwen3:8b", null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"},{"name":"qwen3:8b"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"qwen3:8b"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var model, err = ResolveRunModel(context.Background(), RunModelRequest{ForcePicker: true});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        if selectorCalls != 1 {
        t.Fatalf("expected selector to be called once, got %d", selectorCalls);
    }
        if model != "qwen3:8b" {
        t.Fatalf("expected forced selection to win, got %q", model);
    }
        var if got = config.LastModel(); got != "qwen3:8b" {
        t.Fatalf("expected last model to be updated, got %q", got);
    }
    }

    public static void TestResolveRunModel_ForcePicker_DoesNotReorderByLastModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var if err = config.SetLastModel("qwen3.5"); err != null {
        t.Fatalf("failed to save last model: %v", err);
    }
        var gotNames []String;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        if current != "qwen3.5" {
        t.Fatalf("expected current selection to be last model, got %q", current);
    }
        gotNames = make([]String, 0, len(items));
        var for _, item = range items {
        gotNames = append(gotNames, item.Name);
    }
        return "qwen3.5", null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"qwen3.5"},{"name":"gemma4"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"qwen3.5"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var _, err = ResolveRunModel(context.Background(), RunModelRequest{ForcePicker: true});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        if len(gotNames) == 0 {
        t.Fatal("expected selector to receive model items");
    }
        var glmIdx = slices.Index(gotNames, "gemma4");
        var qwenIdx = slices.Index(gotNames, "qwen3.5");
        if glmIdx == -1 || qwenIdx == -1 {
        t.Fatalf("expected recommended local models in selector items, got %v", gotNames);
    }
        if qwenIdx < glmIdx {
        t.Fatalf("expected list order to stay stable and not float last model to top, got %v", gotNames);
    }
    }

    public static void TestResolveRunModel_UsesSignInHookForCloudModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        return "glm-5:cloud", null;
    }
        var signInCalled = false;
        DefaultSignIn = func(modelName, signInURL String) (String, error) {
        signInCalled = true;
        if modelName != "glm-5:cloud" {
        t.Fatalf("unexpected model passed to sign-in: %q", modelName);
    }
        return "test-user", null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[]}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        case "/api/show":;
        fmt.Fprint(w, `{"remote_model":"glm-5"}`);
        case "/api/me":;
        w.WriteHeader(http.StatusUnauthorized);
        fmt.Fprint(w, `{"error":"unauthorized","signin_url":"https://example.com/signin"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var model, err = ResolveRunModel(context.Background(), RunModelRequest{ForcePicker: true});
        if err != null {
        t.Fatalf("ResolveRunModel returned error: %v", err);
    }
        if model != "glm-5:cloud" {
        t.Fatalf("expected selected cloud model, got %q", model);
    }
        if !signInCalled {
        t.Fatal("expected sign-in hook to be used for cloud model");
    }
    }

    public static void TestLaunchIntegration_EditorForceConfigure(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{paths: []String{"/tmp/settings.json"}}
        withIntegrationOverride(t, "droid", editor);
        var multiCalled boolean;
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        multiCalled = true;
        return []String{"llama3.2", "qwen3:8b"}, null;
    }
        var proceedPrompt boolean;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Proceed?" {
        proceedPrompt = true;
    }
        return true, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"},{"name":"qwen3:8b"}]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if !multiCalled {
        t.Fatal("expected multi selector to be used for forced editor configure");
    }
        if !proceedPrompt {
        t.Fatal("expected backup warning confirmation before edit");
    }
        var if diff = compareStringSlices(editor.edited, [][]String{{"llama3.2", "qwen3:8b"}}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use first selected model, got %q", editor.ranModel);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2", "qwen3:8b"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchIntegration_EditorForceConfigure_FloatsCheckedModelsInPicker(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"qwen3.5:cloud", "qwen3.5"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var gotItems []String;
        var gotPreChecked []String;
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        var for _, item = range items {
        gotItems = append(gotItems, item.Name);
    }
        gotPreChecked = append([]String(null), preChecked...);
        return []String{"qwen3.5:cloud", "qwen3.5"}, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"qwen3.5:cloud","remote_model":"qwen3.5"},{"name":"qwen3.5"}]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        if req.Model == "qwen3.5:cloud" {
        fmt.Fprint(w, `{"remote_model":"qwen3.5"}`);
        return;
    }
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        case "/api/me":;
        fmt.Fprint(w, `{"name":"test-user"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if len(gotItems) == 0 {
        t.Fatal("expected multi selector to receive items");
    }
        if gotItems[0] != "qwen3.5:cloud" {
        t.Fatalf("expected checked models floated to top with qwen3.5:cloud first, got %v", gotItems);
    }
        if len(gotPreChecked) < 2 {
        t.Fatalf("expected prechecked models to be preserved, got %v", gotPreChecked);
    }
        if gotPreChecked[0] != "qwen3.5:cloud" {
        t.Fatalf("expected saved default to remain first in prechecked, got %v", gotPreChecked);
    }
    }

    public static void TestLaunchIntegration_EditorModelOverridePreservesExtras(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"llama3.2", "mistral"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        return;
    }
        http.NotFound(w, r);
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "droid",;
        ModelOverride: "qwen3:8b",;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var want = []String{"qwen3:8b", "llama3.2", "mistral"}
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, want); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        var if diff = compareStringSlices(editor.edited, [][]String{want}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        if editor.ranModel != "qwen3:8b" {
        t.Fatalf("expected override model to launch first, got %q", editor.ranModel);
    }
    }

    public static void TestLaunchIntegration_EditorCloudDisabledFallsBackToSelector(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"glm-5:cloud"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var multiCalled boolean;
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        multiCalled = true;
        return []String{"llama3.2"}, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/status":;
        fmt.Fprint(w, `{"cloud":{"disabled":true,"source":"config"}}`);
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "droid"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if !multiCalled {
        t.Fatal("expected editor flow to reopen selector when cloud-only config is unusable");
    }
    }

    public static void TestLaunchIntegration_EditorConfigureMultiSkipsMissingLocalAndPersistsAccepted(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        return []String{"glm-5:cloud", "missing-local"}, null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Proceed?" {
        return true, null;
    }
        if prompt == "Download missing-local?" {
        return false, null;
    }
        t.Fatalf("unexpected prompt: %q", prompt);
        return false, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"glm-5:cloud","remote_model":"glm-5"}]}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        switch req.Model {
        case "glm-5:cloud":;
        fmt.Fprint(w, `{"remote_model":"glm-5"}`);
        case "missing-local":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        default:;
        http.NotFound(w, r);
    }
        case "/api/me":;
        fmt.Fprint(w, `{"name":"test-user"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var launchErr error;
        var stderr = captureStderr(t, func() {
        launchErr = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        });
        });
        if launchErr != null {
        t.Fatalf("LaunchIntegration returned error: %v", launchErr);
    }
        if editor.ranModel != "glm-5:cloud" {
        t.Fatalf("expected launch to use cloud primary, got %q", editor.ranModel);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"glm-5:cloud"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        var if diff = compareStringSlices(editor.edited, [][]String{{"glm-5:cloud"}}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        if !strings.Contains(stderr, "Skipped missing-local:") {
        t.Fatalf("expected skip reason in stderr, got %q", stderr);
    }
    }

    public static void TestLaunchIntegration_EditorConfigureMultiSkipsUnauthedCloudAndPersistsAccepted(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        return []String{"llama3.2", "glm-5:cloud"}, null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Proceed?" {
        return true, null;
    }
        t.Fatalf("unexpected prompt: %q", prompt);
        return false, null;
    }
        DefaultSignIn = func(modelName, signInURL String) (String, error) {
        return "", ErrCancelled;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"},{"name":"glm-5:cloud","remote_model":"glm-5"}]}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        switch req.Model {
        case "llama3.2":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        case "glm-5:cloud":;
        fmt.Fprint(w, `{"remote_model":"glm-5"}`);
        default:;
        http.NotFound(w, r);
    }
        case "/api/me":;
        w.WriteHeader(http.StatusUnauthorized);
        fmt.Fprint(w, `{"error":"unauthorized","signin_url":"https://example.com/signin"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var launchErr error;
        var stderr = captureStderr(t, func() {
        launchErr = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        });
        });
        if launchErr != null {
        t.Fatalf("LaunchIntegration returned error: %v", launchErr);
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use local primary, got %q", editor.ranModel);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        var if diff = compareStringSlices(editor.edited, [][]String{{"llama3.2"}}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        if !strings.Contains(stderr, "Skipped glm-5:cloud: sign in was cancelled") {
        t.Fatalf("expected skip reason in stderr, got %q", stderr);
    }
    }

    public static void TestLaunchIntegration_EditorConfigureMultiRemovesReselectedFailingModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"glm-5:cloud", "llama3.2"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        return append([]String(null), preChecked...), null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Proceed?" {
        return true, null;
    }
        t.Fatalf("unexpected prompt: %q", prompt);
        return false, null;
    }
        DefaultSignIn = func(modelName, signInURL String) (String, error) {
        return "", ErrCancelled;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"glm-5:cloud","remote_model":"glm-5"},{"name":"llama3.2"}]}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        if req.Model == "glm-5:cloud" {
        fmt.Fprint(w, `{"remote_model":"glm-5"}`);
        return;
    }
        if req.Model == "llama3.2" {
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        return;
    }
        http.NotFound(w, r);
        case "/api/me":;
        w.WriteHeader(http.StatusUnauthorized);
        fmt.Fprint(w, `{"error":"unauthorized","signin_url":"https://example.com/signin"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var launchErr error;
        var stderr = captureStderr(t, func() {
        launchErr = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        });
        });
        if launchErr != null {
        t.Fatalf("LaunchIntegration returned error: %v", launchErr);
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use surviving model, got %q", editor.ranModel);
    }
        var if diff = compareStringSlices(editor.edited, [][]String{{"llama3.2"}}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        var saved, loadErr = config.LoadIntegration("droid");
        if loadErr != null {
        t.Fatalf("failed to reload saved config: %v", loadErr);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        if !strings.Contains(stderr, "Skipped glm-5:cloud: sign in was cancelled") {
        t.Fatalf("expected skip reason in stderr, got %q", stderr);
    }
    }

    public static void TestLaunchIntegration_EditorConfigureMultiAllFailuresKeepsExistingAndSkipsLaunch(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"llama3.2"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        return []String{"missing-local-a", "missing-local-b"}, null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Download missing-local-a?" || prompt == "Download missing-local-b?" {
        return false, null;
    }
        if prompt == "Proceed?" {
        t.Fatal("did not expect proceed prompt when no models are accepted");
    }
        t.Fatalf("unexpected prompt: %q", prompt);
        return false, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        switch req.Model {
        case "missing-local-a", "missing-local-b":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        default:;
        http.NotFound(w, r);
    }
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var launchErr error;
        var stderr = captureStderr(t, func() {
        launchErr = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        });
        });
        if launchErr != null {
        t.Fatalf("LaunchIntegration returned error: %v", launchErr);
    }
        if editor.ranModel != "" {
        t.Fatalf("expected no launch when all selected models are skipped, got %q", editor.ranModel);
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected no editor writes when all selections fail, got %v", editor.edited);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        if !strings.Contains(stderr, "Skipped missing-local-a:") {
        t.Fatalf("expected first skip reason in stderr, got %q", stderr);
    }
        if !strings.Contains(stderr, "Skipped missing-local-b:") {
        t.Fatalf("expected second skip reason in stderr, got %q", stderr);
    }
    }

    public static void TestLaunchIntegration_ConfiguredEditorLaunchValidatesPrimaryOnly(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"llama3.2", "missing-local"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("did not expect prompt during normal configured launch: %q", prompt);
        return false, null;
    }
        var missingShowCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/api/show" {
        http.NotFound(w, r);
        return;
    }
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        switch req.Model {
        case "llama3.2":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        case "missing-local":;
        missingShowCalled = true;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "droid"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if missingShowCalled {
        t.Fatal("expected configured launch to validate only the primary model");
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use saved primary model, got %q", editor.ranModel);
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected no editor writes during normal launch, got %v", editor.edited);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2", "missing-local"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchIntegration_ConfiguredEditorLaunchSkipsReconfigure(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{paths: []String{"/tmp/settings.json"}}
        withIntegrationOverride(t, "droid", editor);
        var if err = config.SaveIntegration("droid", []String{"llama3.2", "qwen3:8b"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("did not expect prompt during a normal editor launch: %s", prompt);
        return false, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        return;
    }
        http.NotFound(w, r);
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "droid"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected normal launch to skip editor rewrites, got %v", editor.edited);
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use saved primary model, got %q", editor.ranModel);
    }
        var saved, err = config.LoadIntegration("droid");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2", "qwen3:8b"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchIntegration_OpenclawPreservesExistingModelList(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "openclaw");
        t.Setenv("PATH", binDir);
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "openclaw", editor);
        var if err = config.SaveIntegration("openclaw", []String{"llama3.2", "mistral"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" {
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        return;
    }
        http.NotFound(w, r);
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "openclaw"}); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected launch to preserve the existing OpenClaw config, got rewrites %v", editor.edited);
    }
        if editor.ranModel != "llama3.2" {
        t.Fatalf("expected launch to use first saved model, got %q", editor.ranModel);
    }
        var saved, err = config.LoadIntegration("openclaw");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2", "mistral"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchIntegration_OpenclawInstallsBeforeConfigSideEffects(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        t.Setenv("PATH", t.TempDir());
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "openclaw", editor);
        var selectorCalled = false;
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        selectorCalled = true;
        return []String{"llama3.2"}, null;
    }
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "openclaw"});
        if err == null {
        t.Fatal("expected launch to fail before configuration when OpenClaw is missing");
    }
        if !strings.Contains(err.Error(), "required dependencies are missing") {
        t.Fatalf("expected install prerequisite error, got %v", err);
    }
        if selectorCalled {
        t.Fatal("expected install check to happen before model selection");
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected no editor writes before install succeeds, got %v", editor.edited);
    }
        var if _, statErr = os.Stat(filepath.Join(tmpDir, ".openclaw", "openclaw.json")); !os.IsNotExist(statErr) {
        t.Fatalf("expected no OpenClaw config file to be created, stat err = %v", statErr);
    }
    }

    public static void TestLaunchIntegration_PiInstallsBeforeConfigSideEffects(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        t.Setenv("PATH", t.TempDir());
        var editor = &launcherEditorRunner{}
        withIntegrationOverride(t, "pi", editor);
        var selectorCalled = false;
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        selectorCalled = true;
        return []String{"llama3.2"}, null;
    }
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{Name: "pi"});
        if err == null {
        t.Fatal("expected launch to fail before configuration when Pi is missing");
    }
        if !strings.Contains(err.Error(), "required dependencies are missing") {
        t.Fatalf("expected install prerequisite error, got %v", err);
    }
        if selectorCalled {
        t.Fatal("expected install check to happen before model selection");
    }
        if len(editor.edited) != 0 {
        t.Fatalf("expected no editor writes before install succeeds, got %v", editor.edited);
    }
        var if _, statErr = os.Stat(filepath.Join(tmpDir, ".pi", "agent", "models.json")); !os.IsNotExist(statErr) {
        t.Fatalf("expected no Pi config file to be created, stat err = %v", statErr);
    }
    }

    public static void TestLaunchIntegration_ConfigureOnlyDoesNotRequireInstalledBinary(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        t.Setenv("PATH", t.TempDir());
        var editor = &launcherEditorRunner{paths: []String{"/tmp/settings.json"}}
        withIntegrationOverride(t, "droid", editor);
        DefaultMultiSelector = func(title String, items []ModelItem, preChecked []String) ([]String, error) {
        return []String{"llama3.2"}, null;
    }
        var prompts []String;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        prompts = append(prompts, prompt);
        if strings.Contains(prompt, "Launch LauncherEditor now?") {
        return false, null;
    }
        return true, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        ConfigureOnly:  true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var if diff = compareStringSlices(editor.edited, [][]String{{"llama3.2"}}); diff != "" {
        t.Fatalf("unexpected edited models (-want +got):\n%s", diff);
    }
        if editor.ranModel != "" {
        t.Fatalf("expected configure-only flow to skip launch, got %q", editor.ranModel);
    }
        if !slices.Contains(prompts, "Proceed?") {
        t.Fatalf("expected editor warning prompt, got %v", prompts);
    }
        if !slices.Contains(prompts, "Launch LauncherEditor now?") {
        t.Fatalf("expected configure-only launch prompt, got %v", prompts);
    }
    }

    public static void TestLaunchIntegration_ClaudeSavesPrimaryModel(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "claude");
        t.Setenv("PATH", binDir);
        var aliasSyncCalled boolean;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[]}`);
        case "/api/status":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"not found"}`);
        case "/api/show":;
        fmt.Fprint(w, `{"remote_model":"glm-5"}`);
        case "/api/me":;
        fmt.Fprint(w, `{"name":"test-user"}`);
        case "/api/experimental/aliases":;
        aliasSyncCalled = true;
        t.Fatalf("did not expect alias sync call after removing Claude alias flow");
        default:;
        t.Fatalf("unexpected request: %s %s", r.Method, r.URL.Path);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "claude",;
        ModelOverride: "glm-5:cloud",;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        var saved, err = config.LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        var if diff = compareStrings(saved.Models, []String{"glm-5:cloud"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
        if aliasSyncCalled {
        t.Fatal("expected Claude launch flow not to sync aliases");
    }
    }

    public static void TestLaunchIntegration_ClaudeForceConfigureReprompts(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "claude");
        t.Setenv("PATH", binDir);
        var if err = config.SaveIntegration("claude", []String{"qwen3:8b"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        var selectorCalls int;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        selectorCalls++;
        return "glm-5:cloud", null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"qwen3:8b"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"qwen3:8b"}`);
        case "/api/me":;
        fmt.Fprint(w, `{"name":"test-user"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "claude",;
        ForceConfigure: true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if selectorCalls != 1 {
        t.Fatalf("expected forced configure to reprompt for model selection, got %d calls", selectorCalls);
    }
        var saved, err = config.LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        if saved.Models[0] != "glm-5:cloud" {
        t.Fatalf("expected saved primary to be replaced, got %q", saved.Models[0]);
    }
    }

    public static void TestLaunchIntegration_ClaudeForceConfigureMissingSelectionDoesNotSave(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "claude");
        t.Setenv("PATH", binDir);
        var if err = config.SaveIntegration("claude", []String{"llama3.2"}); err != null {
        t.Fatalf("failed to seed config: %v", err);
    }
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        return "missing-model", null;
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt == "Download missing-model?" {
        return false, null;
    }
        t.Fatalf("unexpected prompt: %q", prompt);
        return false, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        var req apiShowRequest;
        _ = json.NewDecoder(r.Body).Decode(&req);
        if req.Model == "missing-model" {
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        return;
    }
        fmt.Fprintf(w, `{"model":%q}`, req.Model);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "claude",;
        ForceConfigure: true,;
        });
        if err == null {
        t.Fatal("expected missing selected model to abort launch");
    }
        var saved, loadErr = config.LoadIntegration("claude");
        if loadErr != null {
        t.Fatalf("failed to reload saved config: %v", loadErr);
    }
        var if diff = compareStrings(saved.Models, []String{"llama3.2"}); diff != "" {
        t.Fatalf("unexpected saved models (-want +got):\n%s", diff);
    }
    }

    public static void TestLaunchIntegration_ClaudeModelOverrideSkipsSelector(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "claude");
        t.Setenv("PATH", binDir);
        var selectorCalls int;
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        selectorCalls++;
        return "", fmt.Errorf("selector should not run when --model override is set");
    }
        var confirmCalls int;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        confirmCalls++;
        if !strings.Contains(prompt, "glm-4") {
        t.Fatalf("expected download prompt for override model, got %q", prompt);
    }
        return true, null;
    }
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
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "claude",;
        ModelOverride: "glm-4",;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if selectorCalls != 0 {
        t.Fatalf("expected model override to skip selector, got %d calls", selectorCalls);
    }
        if confirmCalls == 0 {
        t.Fatal("expected missing override model to prompt for download in interactive mode");
    }
        if !pullCalled {
        t.Fatal("expected missing override model to be pulled after confirmation");
    }
        var saved, err = config.LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to reload saved config: %v", err);
    }
        if saved.Models[0] != "glm-4" {
        t.Fatalf("expected saved primary to match override, got %q", saved.Models[0]);
    }
    }

    public static void TestLaunchIntegration_ConfigureOnlyPrompt(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        var runner = &launcherSingleRunner{}
        withIntegrationOverride(t, "stubsingle", runner);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        return "llama3.2", null;
    }
        var prompts []String;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        prompts = append(prompts, prompt);
        if strings.Contains(prompt, "Launch StubSingle now?") {
        return false, null;
    }
        return true, null;
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        fmt.Fprint(w, `{"model":"llama3.2"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "stubsingle",;
        ForceConfigure: true,;
        ConfigureOnly:  true,;
        }); err != null {
        t.Fatalf("LaunchIntegration returned error: %v", err);
    }
        if runner.ranModel != "" {
        t.Fatalf("expected configure-only flow to skip launch when prompt is declined, got %q", runner.ranModel);
    }
        if !slices.Contains(prompts, "Launch StubSingle now?") {
        t.Fatalf("expected launch confirmation prompt, got %v", prompts);
    }
    }

    public static void TestLaunchIntegration_ModelOverrideHeadlessMissingFailsWithoutPrompt(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var runner = &launcherSingleRunner{}
        withIntegrationOverride(t, "droid", runner);
        var confirmCalled = false;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        confirmCalled = true;
        return true, null;
    }
        var pullCalled = false;
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
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "droid",;
        ModelOverride: "missing-model",;
        });
        if err == null {
        t.Fatal("expected missing model to fail in headless mode");
    }
        if !strings.Contains(err.Error(), "ollama pull missing-model") {
        t.Fatalf("expected actionable missing model error, got %v", err);
    }
        if confirmCalled {
        t.Fatal("expected no confirmation prompt in headless mode");
    }
        if pullCalled {
        t.Fatal("expected pull request not to run in headless mode");
    }
        if runner.ranModel != "" {
        t.Fatalf("expected launch to abort before running integration, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_ModelOverrideHeadlessCanOverrideMissingModelPolicy(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var runner = &launcherSingleRunner{}
        withIntegrationOverride(t, "droid", runner);
        var confirmCalled = false;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        confirmCalled = true;
        if !strings.Contains(prompt, "missing-model") {
        t.Fatalf("expected prompt to mention missing model, got %q", prompt);
    }
        return true, null;
    }
        var pullCalled = false;
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
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var customPolicy = LaunchPolicy{MissingModel: LaunchMissingModelPromptToPull}
        var if err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "droid",;
        ModelOverride: "missing-model",;
        Policy:        &customPolicy,;
        }); err != null {
        t.Fatalf("expected policy override to allow prompt/pull in headless mode, got %v", err);
    }
        if !confirmCalled {
        t.Fatal("expected confirmation prompt when missing-model policy is overridden to prompt/pull");
    }
        if !pullCalled {
        t.Fatal("expected pull request to run when missing-model policy is overridden to prompt/pull");
    }
        if runner.ranModel != "missing-model" {
        t.Fatalf("expected integration to launch after pull, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_ModelOverrideInteractiveMissingPromptsAndPulls(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var runner = &launcherSingleRunner{}
        withIntegrationOverride(t, "droid", runner);
        var confirmCalled = false;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        confirmCalled = true;
        if !strings.Contains(prompt, "missing-model") {
        t.Fatalf("expected prompt to mention missing model, got %q", prompt);
    }
        return true, null;
    }
        var pullCalled = false;
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
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:          "droid",;
        ModelOverride: "missing-model",;
        });
        if err != null {
        t.Fatalf("expected interactive override to prompt/pull and succeed, got %v", err);
    }
        if !confirmCalled {
        t.Fatal("expected interactive flow to prompt before pulling missing model");
    }
        if !pullCalled {
        t.Fatal("expected pull request to run after interactive confirmation");
    }
        if runner.ranModel != "missing-model" {
        t.Fatalf("expected integration to run with pulled model, got %q", runner.ranModel);
    }
    }

    public static void TestLaunchIntegration_HeadlessSelectorFlowFailsWithoutPrompt(*testing.T t) {
        var tmpDir = t.TempDir();
        setLaunchTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, false);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, "droid");
        t.Setenv("PATH", binDir);
        var runner = &launcherSingleRunner{}
        withIntegrationOverride(t, "droid", runner);
        DefaultSingleSelector = func(title String, items []ModelItem, current String) (String, error) {
        return "missing-model", null;
    }
        var confirmCalled = false;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        confirmCalled = true;
        return true, null;
    }
        var pullCalled = false;
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"llama3.2"}]}`);
        case "/api/show":;
        w.WriteHeader(http.StatusNotFound);
        fmt.Fprint(w, `{"error":"model not found"}`);
        case "/api/pull":;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        fmt.Fprint(w, `{"status":"success"}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var err = LaunchIntegration(context.Background(), IntegrationLaunchRequest{
        Name:           "droid",;
        ForceConfigure: true,;
        });
        if err == null {
        t.Fatal("expected headless selector flow to fail on missing model");
    }
        if !strings.Contains(err.Error(), "ollama pull missing-model") {
        t.Fatalf("expected actionable missing model error, got %v", err);
    }
        if confirmCalled {
        t.Fatal("expected no confirmation prompt in headless selector flow");
    }
        if pullCalled {
        t.Fatal("expected no pull request in headless selector flow");
    }
        if runner.ranModel != "" {
        t.Fatalf("expected flow to abort before launch, got %q", runner.ranModel);
    }
    }

    public static class apiShowRequest {
        public String Model;
    }

    public static String compareStrings([]String want) {
        if slices.Equal(got, want) {
        return "";
    }
        return fmt.Sprintf("want %v got %v", want, got);
    }

    public static String compareStringSlices([][]String want) {
        if len(got) != len(want) {
        return fmt.Sprintf("want %v got %v", want, got);
    }
        var for i = range got {
        if !slices.Equal(got[i], want[i]) {
        return fmt.Sprintf("want %v got %v", want, got);
    }
    }
        return "";
    }
}
