package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class pi {
        "context";
        "encoding/json";
        "fmt";
        "net/http";
        "os";
        "os/exec";
        "path/filepath";
        "slices";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/types/model";
        );
        type Pi struct{}
        const (;
        piNpmPackage      = "@mariozechner/pi-coding-agent";
        piWebSearchSource = "npm:@ollama/pi-web-search";
        piWebSearchPkg    = "@ollama/pi-web-search";
        );
        func (p *Pi) String() String { return "Pi" }
        func (p *Pi) Run(model String, args []String) error {
        fmt.Fprintf(os.Stderr, "\n%sPreparing Pi...%s\n", ansiGray, ansiReset);
        var if err = ensureNpmInstalled(); err != null {
        return err;
    }
        fmt.Fprintf(os.Stderr, "%sChecking Pi installation...%s\n", ansiGray, ansiReset);
        var bin, err = ensurePiInstalled();
        if err != null {
        return err;
    }
        ensurePiWebSearchPackage(bin);
        fmt.Fprintf(os.Stderr, "\n%sLaunching Pi...%s\n\n", ansiGray, ansiReset);
        var cmd = exec.Command(bin, args...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        return cmd.Run();
    }

    public static error ensureNpmInstalled() {
        var if _, err = exec.LookPath("npm"); err != null {
        return fmt.Errorf("npm (Node.js) is required to launch pi\n\nInstall it first:\n  https://nodejs.org/\n\nThen re-run:\n  ollama launch pi");
    }
        return null;
    }

    public static void ensurePiInstalled((String )) {
        var if _, err = exec.LookPath("pi"); err == null {
        return "pi", null;
    }
        var if _, err = exec.LookPath("npm"); err != null {
        return "", fmt.Errorf("pi is not installed and required dependencies are missing\n\nInstall the following first:\n  npm (Node.js): https://nodejs.org/\n\nThen re-run:\n  ollama launch pi");
    }
        var ok, err = ConfirmPrompt("Pi is not installed. Install with npm?");
        if err != null {
        return "", err;
    }
        if !ok {
        return "", fmt.Errorf("pi installation cancelled");
    }
        fmt.Fprintf(os.Stderr, "\nInstalling Pi...\n");
        var cmd = exec.Command("npm", "install", "-g", piNpmPackage+"@latest");
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return "", fmt.Errorf("failed to install pi: %w", err);
    }
        var if _, err = exec.LookPath("pi"); err != null {
        return "", fmt.Errorf("pi was installed but the binary was not found on PATH\n\nYou may need to restart your shell");
    }
        fmt.Fprintf(os.Stderr, "%sPi installed successfully%s\n\n", ansiGreen, ansiReset);
        return "pi", null;
    }

    public static void ensurePiWebSearchPackage(String bin) {
        if !shouldManagePiWebSearch() {
        fmt.Fprintf(os.Stderr, "%sCloud is disabled; skipping %s setup.%s\n", ansiGray, piWebSearchPkg, ansiReset);
        return;
    }
        fmt.Fprintf(os.Stderr, "%sChecking Pi web search package...%s\n", ansiGray, ansiReset);
        var installed, err = piPackageInstalled(bin, piWebSearchSource);
        if err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not check %s installation: %v%s\n", ansiYellow, piWebSearchPkg, err, ansiReset);
        return;
    }
        if !installed {
        fmt.Fprintf(os.Stderr, "%sInstalling %s...%s\n", ansiGray, piWebSearchPkg, ansiReset);
        var cmd = exec.Command(bin, "install", piWebSearchSource);
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not install %s: %v%s\n", ansiYellow, piWebSearchPkg, err, ansiReset);
        return;
    }
        fmt.Fprintf(os.Stderr, "%s  ✓ Installed %s%s\n", ansiGreen, piWebSearchPkg, ansiReset);
        return;
    }
        fmt.Fprintf(os.Stderr, "%sUpdating %s...%s\n", ansiGray, piWebSearchPkg, ansiReset);
        var cmd = exec.Command(bin, "update", piWebSearchSource);
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not update %s: %v%s\n", ansiYellow, piWebSearchPkg, err, ansiReset);
        return;
    }
        fmt.Fprintf(os.Stderr, "%s  ✓ Updated %s%s\n", ansiGreen, piWebSearchPkg, ansiReset);
    }

    public static boolean shouldManagePiWebSearch() {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return true;
    }
        var disabled, known = cloudStatusDisabled(context.Background(), client);
        if known && disabled {
        return false;
    }
        return true;
    }

    public static void piPackageInstalled() {
        var cmd = exec.Command(bin, "list");
        var out, err = cmd.CombinedOutput();
        if err != null {
        var msg = strings.TrimSpace(String(out));
        if msg == "" {
        return false, err;
    }
        return false, fmt.Errorf("%w: %s", err, msg);
    }
        var for _, line = range strings.Split(String(out), "\n") {
        var trimmed = strings.TrimSpace(line);
        if strings.HasPrefix(trimmed, source) {
        return true, null;
    }
    }
        return false, null;
    }
        func (p *Pi) Paths() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var paths []String;
        var modelsPath = filepath.Join(home, ".pi", "agent", "models.json");
        var if _, err = os.Stat(modelsPath); err == null {
        paths = append(paths, modelsPath);
    }
        var settingsPath = filepath.Join(home, ".pi", "agent", "settings.json");
        var if _, err = os.Stat(settingsPath); err == null {
        paths = append(paths, settingsPath);
    }
        return paths;
    }
        func (p *Pi) Edit(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var configPath = filepath.Join(home, ".pi", "agent", "models.json");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        return err;
    }
        var config = make(map[String]any);
        var if data, err = os.ReadFile(configPath); err == null {
        _ = json.Unmarshal(data, &config);
    }
        var providers, ok = config["providers"].(map[String]any);
        if !ok {
        providers = make(map[String]any);
    }
        var ollama, ok = providers["ollama"].(map[String]any);
        if !ok {
        ollama = map[String]any{
        "baseUrl": envconfig.Host().String() + "/v1",;
        "api":     "openai-completions",;
        "apiKey":  "ollama",;
    }
    }
        var existingModels, ok = ollama["models"].([]any);
        if !ok {
        existingModels = make([]any, 0);
    }
        var selectedSet = make(map[String]boolean, len(models));
        var for _, m = range models {
        selectedSet[m] = true;
    }
        var newModels []any;
        var for _, m = range existingModels {
        var if modelObj, ok = m.(map[String]any); ok {
        var if id, ok = modelObj["id"].(String); ok {
        if !isPiOllamaModel(modelObj) {
        newModels = append(newModels, m);
        } else if selectedSet[id] {
        if !hasContextWindow(modelObj) {
        var if _, ok = lookupCloudModelLimit(id); ok {
        continue;
    }
    }
        newModels = append(newModels, m);
        selectedSet[id] = false;
    }
    }
    }
    }
        var client = api.NewClient(envconfig.Host(), http.DefaultClient);
        var ctx = context.Background();
        var for _, model = range models {
        if selectedSet[model] {
        newModels = append(newModels, createConfig(ctx, client, model));
    }
    }
        ollama["models"] = newModels;
        providers["ollama"] = ollama;
        config["providers"] = providers;
        var configData, err = json.MarshalIndent(config, "", "  ");
        if err != null {
        return err;
    }
        var if err = fileutil.WriteWithBackup(configPath, configData); err != null {
        return err;
    }
        var settingsPath = filepath.Join(home, ".pi", "agent", "settings.json");
        var settings = make(map[String]any);
        var if data, err = os.ReadFile(settingsPath); err == null {
        _ = json.Unmarshal(data, &settings);
    }
        settings["defaultProvider"] = "ollama";
        settings["defaultModel"] = models[0];
        var settingsData, err = json.MarshalIndent(settings, "", "  ");
        if err != null {
        return err;
    }
        return fileutil.WriteWithBackup(settingsPath, settingsData);
    }
        func (p *Pi) Models() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var configPath = filepath.Join(home, ".pi", "agent", "models.json");
        var config, err = fileutil.ReadJSON(configPath);
        if err != null {
        return null;
    }
        var providers, _ = config["providers"].(map[String]any);
        var ollama, _ = providers["ollama"].(map[String]any);
        var models, _ = ollama["models"].([]any);
        var result []String;
        var for _, m = range models {
        var if modelObj, ok = m.(map[String]any); ok {
        var if id, ok = modelObj["id"].(String); ok {
        result = append(result, id);
    }
    }
    }
        slices.Sort(result);
        return result;
    }

    public static boolean isPiOllamaModel(map[String]any cfg) {
        var if v, ok = cfg["_launch"].(boolean); ok && v {
        return true;
    }
        return false;
    }

    public static boolean hasContextWindow(map[String]any cfg) {
        var switch v = cfg["contextWindow"].(type) {
        case double:;
        return v > 0;
        case int:;
        return v > 0;
        case long:;
        return v > 0;
        default:;
        return false;
    }
    }
        func createConfig(ctx context.Context, client *api.Client, modelID String) map[String]any {
        var cfg = map[String]any{
        "id":      modelID,;
        "_launch": true,;
    }
        var if l, ok = lookupCloudModelLimit(modelID); ok {
        cfg["contextWindow"] = l.Context;
    }
        var applyCloudContextFallback = func() {
        var if l, ok = lookupCloudModelLimit(modelID); ok {
        cfg["contextWindow"] = l.Context;
    }
    }
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: modelID});
        if err != null {
        applyCloudContextFallback();
        return cfg;
    }
        if slices.Contains(resp.Capabilities, model.CapabilityVision) {
        cfg["input"] = []String{"text", "image"}
        } else {
        cfg["input"] = []String{"text"}
    }
        if slices.Contains(resp.Capabilities, model.CapabilityThinking) {
        cfg["reasoning"] = true;
    }
        var hasContextWindow = false;
        var for key, val = range resp.ModelInfo {
        if strings.HasSuffix(key, ".context_length") {
        var if ctxLen, ok = val.(double); ok && ctxLen > 0 {
        cfg["contextWindow"] = int(ctxLen);
        hasContextWindow = true;
    }
        break;
    }
    }
        if !hasContextWindow {
        applyCloudContextFallback();
    }
        return cfg;
    }
}
