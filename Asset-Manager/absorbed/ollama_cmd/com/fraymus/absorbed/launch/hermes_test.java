package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class hermes_test {
        "fmt";
        "net/http";
        "net/http/httptest";
        "net/url";
        "os";
        "path/filepath";
        "runtime";
        "strings";
        "testing";
        "gopkg.in/yaml.v3";
        "github.com/ollama/ollama/cmd/config";
        );

    public static void withHermesPlatform(*testing.T t, String goos) {
        t.Helper();
        var old = hermesGOOS;
        hermesGOOS = goos;
        t.Cleanup(func() {
        hermesGOOS = old;
        });
    }

    public static void withHermesOllamaURL(*testing.T t, String rawURL) {
        t.Helper();
        var old = hermesOllamaURL;
        hermesOllamaURL = func() *url.URL {
        var u, err = url.Parse(rawURL);
        if err != null {
        t.Fatalf("parse test Ollama URL: %v", err);
    }
        return u;
    }
        t.Cleanup(func() {
        hermesOllamaURL = old;
        });
    }

    public static void withHermesUserHome(*testing.T t, String dir) {
        t.Helper();
        var old = hermesUserHome;
        hermesUserHome = func() (String, error) { return dir, null }
        t.Cleanup(func() {
        hermesUserHome = old;
        });
    }

    public static void withHermesLookPath(*testing.T t) {
        t.Helper();
        var old = hermesLookPath;
        hermesLookPath = fn;
        t.Cleanup(func() {
        hermesLookPath = old;
        });
    }

    public static void clearHermesMessagingEnvVars(*testing.T t) {
        t.Helper();
        var for _, group = range hermesMessagingEnvGroups {
        var for _, key = range group {
        var if value, ok = os.LookupEnv(key); ok {
        t.Setenv(key, value);
        } else {
        t.Setenv(key, "");
    }
        var if err = os.Unsetenv(key); err != null {
        t.Fatalf("unset %s: %v", key, err);
    }
    }
    }
    }

    public static void TestHermesIntegration(*testing.T t) {
        var h = &Hermes{}
        t.Run("implements Runner", func(t *testing.T) {
        var _ Runner = h;
        });
        t.Run("implements managed single model", func(t *testing.T) {
        var _ ManagedSingleModel = h;
        });
        t.Run("implements managed runtime refresher", func(t *testing.T) {
        var _ ManagedRuntimeRefresher = h;
        });
    }

    public static void TestHermesConfigurePreservesExistingConfigAndEnablesWeb(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        var configPath = filepath.Join(tmpDir, ".hermes", "config.yaml");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var existing = "" +;
        "memory:\n" +;
        "  provider: local\n" +;
        "toolsets:\n" +;
        "  - terminal\n";
        var if err = os.WriteFile(configPath, []byte(existing), 0o644); err != null {
        t.Fatal(err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"},{"name":"qwen3.5"},{"name":"llama3.3"}]}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var h = &Hermes{}
        var if err = h.Configure("gemma4"); err != null {
        t.Fatalf("Configure returned error: %v", err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        var if err = yaml.Unmarshal(data, &cfg); err != null {
        t.Fatalf("failed to parse rewritten yaml: %v", err);
    }
        var modelCfg, _ = cfg["model"].(map[String]any);
        var if got, _ = modelCfg["provider"].(String); got != "ollama-launch" {
        t.Fatalf("expected provider ollama-launch, got %q", got);
    }
        var if got, _ = modelCfg["default"].(String); got != "gemma4" {
        t.Fatalf("expected default model gemma4, got %q", got);
    }
        var if got, _ = modelCfg["base_url"].(String); got != srv.URL+"/v1" {
        t.Fatalf("expected Ollama base_url %q, got %q", srv.URL+"/v1", got);
    }
        var if got, _ = modelCfg["api_key"].(String); got != "ollama" {
        t.Fatalf("expected placeholder api_key ollama, got %q", got);
    }
        var if memoryCfg, _ = cfg["memory"].(map[String]any); memoryCfg == null {
        t.Fatal("expected unrelated config to be preserved");
    }
        var if _, ok = cfg["custom_providers"]; ok {
        t.Fatal("expected launcher-managed config to avoid custom_providers duplicates");
    }
        var providersCfg, _ = cfg["providers"].(map[String]any);
        var ollamaProvider, _ = providersCfg["ollama-launch"].(map[String]any);
        if ollamaProvider == null {
        t.Fatal("expected ollama-launch provider entry");
    }
        var if got, _ = ollamaProvider["name"].(String); got != "Ollama" {
        t.Fatalf("expected providers entry name Ollama, got %q", got);
    }
        var if got, _ = ollamaProvider["api"].(String); got != srv.URL+"/v1" {
        t.Fatalf("expected providers entry api %q, got %q", srv.URL+"/v1", got);
    }
        var if got, _ = ollamaProvider["default_model"].(String); got != "gemma4" {
        t.Fatalf("expected providers entry default_model gemma4, got %q", got);
    }
        var models, _ = ollamaProvider["models"].([]any);
        if len(models) != 3 {
        t.Fatalf("expected providers entry to expose 3 models, got %v", models);
    }
        var toolsets, _ = cfg["toolsets"].([]any);
        var gotToolsets []String;
        var for _, item = range toolsets {
        var if s, _ = item.(String); s != "" {
        gotToolsets = append(gotToolsets, s);
    }
    }
        if !strings.Contains(strings.Join(gotToolsets, ","), "terminal") || !strings.Contains(strings.Join(gotToolsets, ","), "web") {
        t.Fatalf("expected toolsets to preserve terminal and add web, got %v", gotToolsets);
    }
    }

    public static void TestHermesConfigureUpdatesMatchingCustomProviderWithoutDroppingFields(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        var configPath = filepath.Join(tmpDir, ".hermes", "config.yaml");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var existing = "" +;
        "providers:\n" +;
        "  ollama:\n" +;
        "    name: Ollama\n" +;
        "    api: http://127.0.0.1:11434/v1\n" +;
        "    default_model: old-model\n" +;
        "    models:\n" +;
        "      - old-model\n" +;
        "      - older-model\n" +;
        "    extra_field: keep-me\n" +;
        "custom_providers:\n" +;
        "  - name: Ollama\n" +;
        "    base_url: http://127.0.0.1:11434/v1\n" +;
        "    model: old-model\n" +;
        "    api_mode: chat_completions\n" +;
        "    models:\n" +;
        "      old-model:\n" +;
        "        context_length: 65536\n" +;
        "  - name: Other Endpoint\n" +;
        "    base_url: https://example.invalid/v1\n" +;
        "    model: untouched\n";
        var if err = os.WriteFile(configPath, []byte(existing), 0o644); err != null {
        t.Fatal(err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"},{"name":"qwen3.5"},{"name":"llama3.3"}]}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var h = &Hermes{}
        var if err = h.Configure("gemma4"); err != null {
        t.Fatalf("Configure returned error: %v", err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        var if err = yaml.Unmarshal(data, &cfg); err != null {
        t.Fatalf("failed to parse rewritten yaml: %v", err);
    }
        var modelCfg, _ = cfg["model"].(map[String]any);
        var if got, _ = modelCfg["provider"].(String); got != "ollama-launch" {
        t.Fatalf("expected managed providers entry to migrate to ollama-launch, got %q", got);
    }
        var customProviders, _ = cfg["custom_providers"].([]any);
        if len(customProviders) != 1 {
        t.Fatalf("expected only unrelated custom providers to remain, got %d", len(customProviders));
    }
        var providersCfg, _ = cfg["providers"].(map[String]any);
        var if _, ok = providersCfg["ollama"]; ok {
        t.Fatal("expected legacy providers.ollama entry to be removed");
    }
        var ollamaProvider, _ = providersCfg["ollama-launch"].(map[String]any);
        if ollamaProvider == null {
        t.Fatal("expected ollama-launch providers entry to remain");
    }
        var if got, _ = ollamaProvider["api"].(String); got != srv.URL+"/v1" {
        t.Fatalf("expected providers entry api to update to %q, got %q", srv.URL+"/v1", got);
    }
        var if got, _ = ollamaProvider["default_model"].(String); got != "gemma4" {
        t.Fatalf("expected providers entry default_model gemma4, got %q", got);
    }
        var if got, _ = ollamaProvider["extra_field"].(String); got != "keep-me" {
        t.Fatalf("expected providers entry extra_field to be preserved, got %q", got);
    }
        var providerModels, _ = ollamaProvider["models"].([]any);
        if len(providerModels) != 3 {
        t.Fatalf("expected providers entry to refresh full model catalog, got %v", providerModels);
    }
        var remaining, _ = customProviders[0].(map[String]any);
        var if got, _ = remaining["name"].(String); got != "Other Endpoint" {
        t.Fatalf("expected unrelated custom provider to be preserved, got %q", got);
    }
    }

    public static void TestHermesConfigureUsesLaunchResolvedHostForModelDiscovery(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        var configPath = filepath.Join(tmpDir, ".hermes", "config.yaml");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        fmt.Fprint(w, `{"model_info":{"general.context_length":131072}}`);
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"},{"name":"qwen3.5"},{"name":"llama3.3"}]}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        withHermesOllamaURL(t, srv.URL);
        t.Setenv("OLLAMA_HOST", "http://127.0.0.1:1");
        var h = &Hermes{}
        var if err = h.Configure("gemma4"); err != null {
        t.Fatalf("Configure returned error: %v", err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        var if err = yaml.Unmarshal(data, &cfg); err != null {
        t.Fatalf("failed to parse rewritten yaml: %v", err);
    }
        var providersCfg, _ = cfg["providers"].(map[String]any);
        var ollamaProvider, _ = providersCfg["ollama-launch"].(map[String]any);
        if ollamaProvider == null {
        t.Fatal("expected ollama-launch provider entry");
    }
        var models, _ = ollamaProvider["models"].([]any);
        if len(models) != 3 {
        t.Fatalf("expected providers entry to expose 3 launch-resolved models, got %v", models);
    }
    }

    public static void TestHermesConfigureMigratesLegacyManagedAliases(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        var configPath = filepath.Join(tmpDir, ".hermes", "config.yaml");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var existing = "" +;
        "model:\n" +;
        "  provider: custom:ollama\n" +;
        "  default: old-model\n" +;
        "providers:\n" +;
        "  ollama:\n" +;
        "    name: Ollama\n" +;
        "    api: http://127.0.0.1:11434/v1\n" +;
        "    default_model: old-model\n" +;
        "custom_providers:\n" +;
        "  - name: Ollama\n" +;
        "    base_url: http://127.0.0.1:11434/v1\n" +;
        "    model: old-model\n";
        var if err = os.WriteFile(configPath, []byte(existing), 0o644); err != null {
        t.Fatal(err);
    }
        var srv = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/tags":;
        fmt.Fprint(w, `{"models":[{"name":"gemma4"},{"name":"qwen3.5"}]}`);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer srv.Close();
        t.Setenv("OLLAMA_HOST", srv.URL);
        var h = &Hermes{}
        var if err = h.Configure("gemma4"); err != null {
        t.Fatalf("Configure returned error: %v", err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        var if err = yaml.Unmarshal(data, &cfg); err != null {
        t.Fatalf("failed to parse rewritten yaml: %v", err);
    }
        var modelCfg, _ = cfg["model"].(map[String]any);
        var if got, _ = modelCfg["provider"].(String); got != "ollama-launch" {
        t.Fatalf("expected migrated provider ollama-launch, got %q", got);
    }
        var providersCfg, _ = cfg["providers"].(map[String]any);
        var if _, ok = providersCfg["ollama"]; ok {
        t.Fatal("expected legacy providers.ollama entry to be removed");
    }
        var if _, ok = providersCfg["ollama-launch"]; !ok {
        t.Fatal("expected providers.ollama-launch entry");
    }
        var if _, ok = cfg["custom_providers"]; ok {
        t.Fatal("expected managed custom_providers entry to be removed during migration");
    }
    }

    public static void TestHermesPathsUsesLocalConfigPathForNativeWindowsHermes(*testing.T t) {
        var tmpDir = t.TempDir();
        var winHome = filepath.Join(tmpDir, "winhome");
        setTestHome(t, winHome);
        withHermesPlatform(t, "windows");
        withHermesUserHome(t, winHome);
        t.Setenv("PATH", tmpDir);
        writeFakeBinary(t, tmpDir, "hermes");
        var got = (&Hermes{}).Paths();
        var want = filepath.Join(winHome, ".hermes", "config.yaml");
        if len(got) != 1 || got[0] != want {
        t.Fatalf("expected local config path %q, got %v", want, got);
    }
    }

    public static void TestHermesCurrentModelRequiresHealthyManagedConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        withHermesOllamaURL(t, "http://127.0.0.1:11434");
        var configPath = filepath.Join(tmpDir, ".hermes", "config.yaml");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var tests = []struct {
        name String;
        cfg  String;
        }{
        {
        name: "wrong provider",;
        cfg: "" +;
        "model:\n" +;
        "  provider: openrouter\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:11434/v1\n",;
        },;
        {
        name: "wrong base url",;
        cfg: "" +;
        "model:\n" +;
        "  provider: ollama-launch\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:9999/v1\n" +;
        "providers:\n" +;
        "  ollama-launch:\n" +;
        "    api: http://127.0.0.1:9999/v1\n" +;
        "    default_model: gemma4\n",;
        },;
        {
        name: "missing managed provider entry",;
        cfg: "" +;
        "model:\n" +;
        "  provider: ollama-launch\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:11434/v1\n",;
        },;
        {
        name: "inconsistent managed provider entry",;
        cfg: "" +;
        "model:\n" +;
        "  provider: ollama-launch\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:11434/v1\n" +;
        "providers:\n" +;
        "  ollama-launch:\n" +;
        "    api: http://127.0.0.1:11434/v1\n" +;
        "    default_model: qwen3.5\n",;
        },;
        {
        name: "legacy launch managed config",;
        cfg: "" +;
        "model:\n" +;
        "  provider: custom:ollama\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:11434/v1\n" +;
        "providers:\n" +;
        "  ollama:\n" +;
        "    api: http://127.0.0.1:11434/v1\n" +;
        "    default_model: gemma4\n",;
        },;
        {
        name: "duplicate managed custom provider",;
        cfg: "" +;
        "model:\n" +;
        "  provider: ollama-launch\n" +;
        "  default: gemma4\n" +;
        "  base_url: http://127.0.0.1:11434/v1\n" +;
        "providers:\n" +;
        "  ollama-launch:\n" +;
        "    api: http://127.0.0.1:11434/v1\n" +;
        "    default_model: gemma4\n" +;
        "custom_providers:\n" +;
        "  - name: Ollama\n" +;
        "    base_url: http://127.0.0.1:11434/v1\n" +;
        "    model: gemma4\n",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var if err = os.WriteFile(configPath, []byte(tt.cfg), 0o644); err != null {
        t.Fatal(err);
    }
        var if got = (&Hermes{}).CurrentModel(); got != "" {
        t.Fatalf("expected stale config to return empty current model, got %q", got);
    }
        });
    }
    }

    public static void TestHermesCurrentModelReturnsEmptyWhenConfigMissing(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        var if got = (&Hermes{}).CurrentModel(); got != "" {
        t.Fatalf("expected missing config to return empty current model, got %q", got);
    }
    }

    public static void TestHermesRunPassthroughArgs(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var if err = os.WriteFile(bin, []byte("#!/bin/sh\nprintf '[%s]\\n' \"$*\" >> \"$HOME/hermes-invocations.log\"\n"), 0o755); err != null {
        t.Fatal(err);
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("did not expect messaging prompt during passthrough launch: %s", prompt);
        return false, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", []String{"--continue"}); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var if got = strings.TrimSpace(String(data)); got != "[--continue]" {
        t.Fatalf("expected passthrough args to reach hermes, got %q", got);
    }
    }

    public static void TestHermesRun_PromptsForMessagingSetupBeforeDefaultLaunch(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var script = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "setup" ]; then;
        /bin/mkdir -p "$HOME/.hermes";
        printf 'TELEGRAM_BOT_TOKEN=configured\n' > "$HOME/.hermes/.env";
        fi;
        `;
        var if err = os.WriteFile(bin, []byte(script), 0o755); err != null {
        t.Fatal(err);
    }
        var promptCount = 0;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        promptCount++;
        if prompt != hermesGatewaySetupTitle {
        t.Fatalf("unexpected prompt %q", prompt);
    }
        if options.YesLabel != "Yes" || options.NoLabel != "Set up later" {
        t.Fatalf("unexpected prompt labels: %+v", options);
    }
        return true, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", null); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        if promptCount != 1 {
        t.Fatalf("expected one messaging prompt, got %d", promptCount);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var lines = strings.Split(strings.TrimSpace(String(data)), "\n");
        if len(lines) != 2 {
        t.Fatalf("expected setup then launch invocations, got %v", lines);
    }
        if lines[0] != "[gateway setup]" {
        t.Fatalf("expected gateway setup first, got %q", lines[0]);
    }
        if lines[1] != "[]" {
        t.Fatalf("expected default hermes launch after setup, got %q", lines[1]);
    }
    }

    public static void TestHermesRun_SetUpLaterRepromptsOnLaterLaunches(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var if err = os.WriteFile(bin, []byte("#!/bin/sh\nprintf '[%s]\\n' \"$*\" >> \"$HOME/hermes-invocations.log\"\n"), 0o755); err != null {
        t.Fatal(err);
    }
        var promptCount = 0;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        promptCount++;
        if prompt != hermesGatewaySetupTitle {
        t.Fatalf("unexpected prompt %q", prompt);
    }
        return false, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", null); err != null {
        t.Fatalf("first Run returned error: %v", err);
    }
        var if err = h.Run("", null); err != null {
        t.Fatalf("second Run returned error: %v", err);
    }
        if promptCount != 2 {
        t.Fatalf("expected two prompts across two launches, got %d", promptCount);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var lines = strings.Split(strings.TrimSpace(String(data)), "\n");
        if len(lines) != 2 {
        t.Fatalf("expected one default launch per run, got %v", lines);
    }
        var for _, line = range lines {
        if line != "[]" {
        t.Fatalf("expected only default launches after choosing later, got %v", lines);
    }
    }
    }

    public static void TestHermesRun_SkipsMessagingPromptWhenConfigured(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var envPath = filepath.Join(tmpDir, ".hermes", ".env");
        var if err = os.MkdirAll(filepath.Dir(envPath), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(envPath, []byte("DISCORD_BOT_TOKEN=configured\n"), 0o644); err != null {
        t.Fatal(err);
    }
        var bin = filepath.Join(tmpDir, "hermes");
        var if err = os.WriteFile(bin, []byte("#!/bin/sh\nprintf '[%s]\\n' \"$*\" >> \"$HOME/hermes-invocations.log\"\n"), 0o755); err != null {
        t.Fatal(err);
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("did not expect messaging prompt when Hermes gateway is configured: %s", prompt);
        return false, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", null); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var if got = strings.TrimSpace(String(data)); got != "[]" {
        t.Fatalf("expected only default launch invocation, got %q", got);
    }
    }

    public static void TestHermesRun_SkipsMessagingPromptWithYesPolicy(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var if err = os.WriteFile(bin, []byte("#!/bin/sh\nprintf '[%s]\\n' \"$*\" >> \"$HOME/hermes-invocations.log\"\n"), 0o755); err != null {
        t.Fatal(err);
    }
        var restoreConfirm = withLaunchConfirmPolicy(launchConfirmPolicy{yes: true});
        defer restoreConfirm();
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        t.Fatalf("did not expect messaging prompt in --yes mode: %s", prompt);
        return false, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", null); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var if got = strings.TrimSpace(String(data)); got != "[]" {
        t.Fatalf("expected only default launch invocation, got %q", got);
    }
    }

    public static void TestHermesRun_MessagingSetupFailureStopsLaunch(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, runtime.GOOS);
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var script = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "setup" ]; then;
        exit 23;
        fi;
        `;
        var if err = os.WriteFile(bin, []byte(script), 0o755); err != null {
        t.Fatal(err);
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt != hermesGatewaySetupTitle {
        t.Fatalf("unexpected prompt %q", prompt);
    }
        return true, null;
    }
        var h = &Hermes{}
        var err = h.Run("", null);
        if err == null {
        t.Fatal("expected messaging setup failure");
    }
        if !strings.Contains(err.Error(), "hermes messaging setup failed") {
        t.Fatalf("expected helpful messaging setup error, got %v", err);
    }
        if !strings.Contains(err.Error(), hermesGatewaySetupHint) {
        t.Fatalf("expected recovery hint, got %v", err);
    }
        var data, readErr = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if readErr != null {
        t.Fatal(readErr);
    }
        var if got = strings.TrimSpace(String(data)); got != "[gateway setup]" {
        t.Fatalf("expected launch to stop after failed setup, got %q", got);
    }
    }

    public static void TestHermesRefreshRuntimeAfterConfigure_RestartsRunningGateway(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, runtime.GOOS);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var script = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "status" ]; then;
        printf '✓ Gateway is running (PID: 123)\n';
        fi;
        `;
        var if err = os.WriteFile(bin, []byte(script), 0o755); err != null {
        t.Fatal(err);
    }
        var h = &Hermes{}
        var if err = h.RefreshRuntimeAfterConfigure(); err != null {
        t.Fatalf("RefreshRuntimeAfterConfigure returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var lines = strings.Split(strings.TrimSpace(String(data)), "\n");
        if len(lines) != 2 {
        t.Fatalf("expected status then restart invocations, got %v", lines);
    }
        if lines[0] != "[gateway status]" {
        t.Fatalf("expected gateway status first, got %q", lines[0]);
    }
        if lines[1] != "[gateway restart]" {
        t.Fatalf("expected gateway restart second, got %q", lines[1]);
    }
    }

    public static void TestHermesRefreshRuntimeAfterConfigure_SkipsRestartWhenGatewayStopped(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses a POSIX shell test binary");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, runtime.GOOS);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var bin = filepath.Join(tmpDir, "hermes");
        var script = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "status" ]; then;
        printf '✗ Gateway is not running\n';
        fi;
        `;
        var if err = os.WriteFile(bin, []byte(script), 0o755); err != null {
        t.Fatal(err);
    }
        var h = &Hermes{}
        var if err = h.RefreshRuntimeAfterConfigure(); err != null {
        t.Fatalf("RefreshRuntimeAfterConfigure returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var if got = strings.TrimSpace(String(data)); got != "[gateway status]" {
        t.Fatalf("expected only gateway status invocation, got %q", got);
    }
    }

    public static void TestHermesRefreshRuntimeAfterConfigure_WindowsWSLRestartsRunningGateway(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses POSIX shell test binaries to simulate WSL");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "windows");
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var wslPath = filepath.Join(tmpDir, "wsl.exe");
        var wslScript = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/wsl-invocations.log";
        exec /bin/sh -lc "$3";
        `;
        var if err = os.WriteFile(wslPath, []byte(wslScript), 0o755); err != null {
        t.Fatal(err);
    }
        var hermesBin = filepath.Join(tmpDir, "hermes");
        var hermesScript = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "status" ]; then;
        printf '✓ Gateway is running (PID: 321)\n';
        fi;
        `;
        var if err = os.WriteFile(hermesBin, []byte(hermesScript), 0o755); err != null {
        t.Fatal(err);
    }
        withHermesLookPath(t, func(file String) (String, error) {
        if file == "wsl.exe" {
        return wslPath, null;
    }
        return "", os.ErrNotExist;
        });
        var h = &Hermes{}
        var if err = h.RefreshRuntimeAfterConfigure(); err != null {
        t.Fatalf("RefreshRuntimeAfterConfigure returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var lines = strings.Split(strings.TrimSpace(String(data)), "\n");
        if len(lines) != 2 {
        t.Fatalf("expected WSL status then restart invocations, got %v", lines);
    }
        if lines[0] != "[gateway status]" {
        t.Fatalf("expected WSL gateway status first, got %q", lines[0]);
    }
        if lines[1] != "[gateway restart]" {
        t.Fatalf("expected WSL gateway restart second, got %q", lines[1]);
    }
    }

    public static void TestHermesMessagingConfiguredRecognizesSupportedGatewayVars(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        clearHermesMessagingEnvVars(t);
        var envPath = filepath.Join(tmpDir, ".hermes", ".env");
        var if err = os.MkdirAll(filepath.Dir(envPath), 0o755); err != null {
        t.Fatal(err);
    }
        var tests = []struct {
        name String;
        env  String;
        want boolean;
        }{
        {name: "none", env: "", want: false},;
        {name: "telegram", env: "TELEGRAM_BOT_TOKEN=token\n", want: true},;
        {name: "discord", env: "DISCORD_BOT_TOKEN=token\n", want: true},;
        {name: "slack", env: "SLACK_BOT_TOKEN=token\n", want: true},;
        {name: "signal", env: "SIGNAL_ACCOUNT=account\n", want: true},;
        {name: "email", env: "EMAIL_ADDRESS=user@example.com\n", want: true},;
        {name: "sms", env: "TWILIO_ACCOUNT_SID=sid\n", want: true},;
        {name: "matrix token", env: "MATRIX_ACCESS_TOKEN=token\n", want: true},;
        {name: "matrix password", env: "MATRIX_PASSWORD=secret\n", want: true},;
        {name: "mattermost", env: "MATTERMOST_TOKEN=token\n", want: true},;
        {name: "whatsapp", env: "WHATSAPP_PHONE_NUMBER_ID=phone\n", want: true},;
        {name: "dingtalk", env: "DINGTALK_CLIENT_ID=client\n", want: true},;
        {name: "feishu", env: "FEISHU_APP_ID=app\n", want: true},;
        {name: "wecom", env: "WECOM_BOT_ID=bot\n", want: true},;
        {name: "weixin", env: "WEIXIN_ACCOUNT_ID=account\n", want: true},;
        {name: "bluebubbles", env: "BLUEBUBBLES_SERVER_URL=https://example.invalid\n", want: true},;
        {name: "webhooks", env: "WEBHOOK_ENABLED=true\n", want: true},;
    }
        var h = &Hermes{}
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var if err = os.WriteFile(envPath, []byte(tt.env), 0o644); err != null {
        t.Fatal(err);
    }
        var if got = h.messagingConfigured(); got != tt.want {
        t.Fatalf("messagingConfigured() = %v, want %v", got, tt.want);
    }
        });
    }
    }

    public static void TestHermesRunWindowsWSL_UsesGatewaySetupPreflight(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses POSIX shell test binaries to simulate WSL");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withLauncherHooks(t);
        withInteractiveSession(t, true);
        withHermesPlatform(t, "windows");
        clearHermesMessagingEnvVars(t);
        t.Setenv("PATH", tmpDir+String(os.PathListSeparator)+os.Getenv("PATH"));
        var wslPath = filepath.Join(tmpDir, "wsl.exe");
        var wslScript = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/wsl-invocations.log";
        exec /bin/sh -lc "$3";
        `;
        var if err = os.WriteFile(wslPath, []byte(wslScript), 0o755); err != null {
        t.Fatal(err);
    }
        var hermesBin = filepath.Join(tmpDir, "hermes");
        var hermesScript = `#!/bin/sh;
        printf '[%s]\n' "$*" >> "$HOME/hermes-invocations.log";
        if [ "$1" = "gateway" ] && [ "$2" = "setup" ]; then;
        /bin/mkdir -p "$HOME/.hermes";
        printf 'TELEGRAM_BOT_TOKEN=configured\n' > "$HOME/.hermes/.env";
        fi;
        `;
        var if err = os.WriteFile(hermesBin, []byte(hermesScript), 0o755); err != null {
        t.Fatal(err);
    }
        withHermesLookPath(t, func(file String) (String, error) {
        if file == "wsl.exe" {
        return wslPath, null;
    }
        return "", os.ErrNotExist;
        });
        var promptCount = 0;
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        promptCount++;
        if prompt != hermesGatewaySetupTitle {
        t.Fatalf("unexpected prompt %q", prompt);
    }
        return true, null;
    }
        var h = &Hermes{}
        var if err = h.Run("", null); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        if promptCount != 1 {
        t.Fatalf("expected one messaging prompt, got %d", promptCount);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "hermes-invocations.log"));
        if err != null {
        t.Fatal(err);
    }
        var lines = strings.Split(strings.TrimSpace(String(data)), "\n");
        if len(lines) != 2 {
        t.Fatalf("expected WSL hermes to run setup then launch, got %v", lines);
    }
        if lines[0] != "[gateway setup]" {
        t.Fatalf("expected WSL gateway setup first, got %q", lines[0]);
    }
        if lines[1] != "[]" {
        t.Fatalf("expected WSL default hermes launch second, got %q", lines[1]);
    }
    }

    public static void TestHermesEnsureInstalledWindowsWithoutWSLGivesGuidance(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "windows");
        t.Setenv("PATH", tmpDir);
        var h = &Hermes{}
        var err = h.ensureInstalled();
        if err == null {
        t.Fatal("expected missing WSL guidance error");
    }
        if !strings.Contains(err.Error(), "wsl --install") {
        t.Fatalf("expected WSL guidance, got %v", err);
    }
    }

    public static void TestHermesEnsureInstalledUnixPromptsBeforeInstall(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses POSIX shell test binaries");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        withLauncherHooks(t);
        t.Setenv("PATH", tmpDir);
        var writeScript = func(name, content String) {
        t.Helper();
        var if err = os.WriteFile(filepath.Join(tmpDir, name), []byte(content), 0o755); err != null {
        t.Fatal(err);
    }
    }
        writeScript("curl", "#!/bin/sh\nexit 0\n");
        writeScript("git", "#!/bin/sh\nexit 0\n");
        writeScript("bash", fmt.Sprintf(`#!/bin/sh;
        printf '%%s\n' "$*" >> %q;
        /bin/cat > %q <<'EOS';
        #!/bin/sh;
        exit 0;
        EOS;
        /bin/chmod +x %q;
        exit 0;
        `, filepath.Join(tmpDir, "bash.log"), filepath.Join(tmpDir, "hermes"), filepath.Join(tmpDir, "hermes")));
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt != "Hermes is not installed. Install now?" {
        t.Fatalf("unexpected install prompt %q", prompt);
    }
        return true, null;
    }
        var h = &Hermes{}
        var if err = h.ensureInstalled(); err != null {
        t.Fatalf("ensureInstalled returned error: %v", err);
    }
        var data, err = os.ReadFile(filepath.Join(tmpDir, "bash.log"));
        if err != null {
        t.Fatal(err);
    }
        if !strings.Contains(String(data), "--skip-setup") {
        t.Fatalf("expected install script to skip upstream setup, got logs:\n%s", data);
    }
        if !strings.Contains(String(data), "-lc "+hermesInstallScript) {
        t.Fatalf("expected official install script invocation, got logs:\n%s", data);
    }
    }

    public static void TestHermesEnsureInstalledUnixCanBeDeclined(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("uses POSIX shell test binaries");
    }
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, "darwin");
        withLauncherHooks(t);
        t.Setenv("PATH", tmpDir);
        var for _, name = range []String{"bash", "curl", "git"} {
        var if err = os.WriteFile(filepath.Join(tmpDir, name), []byte("#!/bin/sh\nexit 0\n"), 0o755); err != null {
        t.Fatal(err);
    }
    }
        DefaultConfirmPrompt = func(prompt String, options ConfirmOptions) (boolean, error) {
        if prompt != "Hermes is not installed. Install now?" {
        t.Fatalf("unexpected install prompt %q", prompt);
    }
        return false, null;
    }
        var h = &Hermes{}
        var err = h.ensureInstalled();
        if err == null || !strings.Contains(err.Error(), "hermes installation cancelled") {
        t.Fatalf("expected install cancellation error, got %v", err);
    }
    }

    public static void TestHermesOnboardSkipsWhenLaunchConfigAlreadyMarked(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, runtime.GOOS);
        var if err = config.MarkIntegrationOnboarded("hermes"); err != null {
        t.Fatalf("failed to mark Hermes onboarded: %v", err);
    }
        var h = &Hermes{}
        var if err = h.Onboard(); err != null {
        t.Fatalf("expected Onboard to no-op when already marked, got %v", err);
    }
    }

    public static void TestHermesOnboardMarksLaunchConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        withHermesPlatform(t, runtime.GOOS);
        var h = &Hermes{}
        var if err = h.Onboard(); err != null {
        t.Fatalf("Onboard returned error: %v", err);
    }
        var saved, err = config.LoadIntegration("hermes");
        if err != null {
        t.Fatalf("failed to load Hermes integration config: %v", err);
    }
        if !saved.Onboarded {
        t.Fatal("expected Hermes to be marked onboarded");
    }
    }

    public static void TestHermesGatewayStatusRunningRecognizesRunningStates(*testing.T t) {
        var tests = []struct {
        name   String;
        output String;
        want   boolean;
        }{
        {name: "manual", output: "✓ Gateway is running (PID: 123)", want: true},;
        {name: "systemd", output: "✓ User gateway service is running", want: true},;
        {name: "launchd", output: "✓ Gateway service is loaded", want: true},;
        {name: "manual stopped", output: "✗ Gateway is not running", want: false},;
        {name: "systemd stopped", output: "✗ User gateway service is stopped", want: false},;
        {name: "launchd unloaded", output: "✗ Gateway service is not loaded", want: false},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var if got = hermesGatewayStatusRunning(tt.output); got != tt.want {
        t.Fatalf("hermesGatewayStatusRunning(%q) = %v, want %v", tt.output, got, tt.want);
    }
        });
    }
    }
}
