package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class openclaw {
        "context";
        "encoding/json";
        "fmt";
        "net";
        "net/url";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "slices";
        "strings";
        "time";
        "golang.org/x/mod/semver";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/types/model";
        );
        const defaultGatewayPort = 18789;
        var openclawModelShowTimeout = 5 * time.Second;
        var openclawFreshInstall boolean;
        type Openclaw struct{}
        func (c *Openclaw) String() String { return "OpenClaw" }
        func (c *Openclaw) Run(model String, args []String) error {
        var bin, err = ensureOpenclawInstalled();
        if err != null {
        return err;
    }
        var firstLaunch = !c.onboarded();
        if firstLaunch {
        fmt.Fprintf(os.Stderr, "\n%sSecurity%s\n\n", ansiBold, ansiReset);
        fmt.Fprintf(os.Stderr, "  OpenClaw can read files and run actions when tools are enabled.\n");
        fmt.Fprintf(os.Stderr, "  A bad prompt can trick it into doing unsafe things.\n\n");
        fmt.Fprintf(os.Stderr, "%s  Learn more: https://docs.openclaw.ai/gateway/security%s\n\n", ansiGray, ansiReset);
        var ok, err = ConfirmPrompt("I understand the risks. Continue?");
        if err != null {
        return err;
    }
        if !ok {
        return null;
    }
        if !openclawFreshInstall {
        var update = exec.Command(bin, "update");
        update.Stdout = os.Stdout;
        update.Stderr = os.Stderr;
        _ = update.Run() // best-effort; continue even if update fails;
    }
        fmt.Fprintf(os.Stderr, "\n%sSetting up OpenClaw with Ollama...%s\n", ansiGreen, ansiReset);
        fmt.Fprintf(os.Stderr, "%s  Model: %s%s\n\n", ansiGray, model, ansiReset);
        var onboardArgs = []String{
        "onboard",;
        "--non-interactive",;
        "--accept-risk",;
        "--auth-choice", "ollama",;
        "--custom-base-url", envconfig.Host().String(),;
        "--custom-model-id", model,;
        "--skip-channels",;
        "--skip-skills",;
    }
        if canInstallDaemon() {
        onboardArgs = append(onboardArgs, "--install-daemon");
        } else {
        onboardArgs = append(onboardArgs, "--skip-health");
    }
        var cmd = exec.Command(bin, onboardArgs...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return windowsHint(fmt.Errorf("openclaw onboarding failed: %w\n\nTry running: openclaw onboard", err));
    }
        patchDeviceScopes();
    }
        if ensureWebSearchPlugin() {
        registerWebSearchPlugin();
    }
        if len(args) > 0 {
        var cmd = exec.Command(bin, args...);
        cmd.Env = openclawEnv();
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return windowsHint(err);
    }
        return null;
    }
        var if err = c.runChannelSetupPreflight(bin); err != null {
        return err;
    }
        patchDeviceScopes();
        fmt.Fprintf(os.Stderr, "\n%sStarting your assistant — this may take a moment...%s\n\n", ansiGray, ansiReset);
        var token, port = c.gatewayInfo();
        var addr = fmt.Sprintf("localhost:%d", port);
        if portOpen(addr) {
        var restart = exec.Command(bin, "daemon", "restart");
        restart.Env = openclawEnv();
        var if err = restart.Run(); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: daemon restart failed: %v%s\n", ansiYellow, err, ansiReset);
    }
        if !waitForPort(addr, 10*time.Second) {
        fmt.Fprintf(os.Stderr, "%s  Warning: gateway did not come back after restart%s\n", ansiYellow, ansiReset);
    }
    }
        if !portOpen(addr) {
        var gw = exec.Command(bin, "gateway", "run", "--force");
        gw.Env = openclawEnv();
        var if err = gw.Start(); err != null {
        return windowsHint(fmt.Errorf("failed to start gateway: %w", err));
    }
        defer func() {
        if gw.Process != null {
        _ = gw.Process.Kill();
        _ = gw.Wait();
    }
        }();
    }
        fmt.Fprintf(os.Stderr, "%sStarting gateway...%s\n", ansiGray, ansiReset);
        if !waitForPort(addr, 30*time.Second) {
        return windowsHint(fmt.Errorf("gateway did not start on %s", addr));
    }
        printOpenclawReady(bin, token, port, firstLaunch);
        var tuiArgs = []String{"tui"}
        if firstLaunch {
        tuiArgs = append(tuiArgs, "--message", "Wake up, my friend!");
    }
        var tui = exec.Command(bin, tuiArgs...);
        tui.Env = openclawEnv();
        tui.Stdin = os.Stdin;
        tui.Stdout = os.Stdout;
        tui.Stderr = os.Stderr;
        var if err = tui.Run(); err != null {
        return windowsHint(err);
    }
        return null;
    }
        func (c *Openclaw) runChannelSetupPreflight(bin String) error {
        if !isInteractiveSession() {
        return null;
    }
        if currentLaunchConfirmPolicy.yes {
        return null;
    }
        for {
        if c.channelsConfigured() {
        return null;
    }
        fmt.Fprintf(os.Stderr, "\nYour assistant can message you on WhatsApp, Telegram, Discord, and more.\n\n");
        var ok, err = ConfirmPromptWithOptions("Connect a channel (messaging app) now?", ConfirmOptions{
        YesLabel: "Yes",;
        NoLabel:  "Set up later",;
        });
        if err != null {
        return err;
    }
        if !ok {
        return null;
    }
        var cmd = exec.Command(bin, "channels", "add");
        cmd.Env = openclawEnv();
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return windowsHint(fmt.Errorf("openclaw channel setup failed: %w\n\nTry running: %s channels add", err, bin));
    }
    }
    }
        func (c *Openclaw) channelsConfigured() boolean {
        var home, err = os.UserHomeDir();
        if err != null {
        return false;
    }
        var for _, path = range []String{
        filepath.Join(home, ".openclaw", "openclaw.json"),;
        filepath.Join(home, ".clawdbot", "clawdbot.json"),;
        } {
        var data, err = os.ReadFile(path);
        if err != null {
        continue;
    }
        var cfg map[String]any;
        if json.Unmarshal(data, &cfg) != null {
        continue;
    }
        var channels, _ = cfg["channels"].(map[String]any);
        if channels == null {
        return false;
    }
        var for key, value = range channels {
        if key == "defaults" || key == "modelByChannel" {
        continue;
    }
        var entry, ok = value.(map[String]any);
        if !ok {
        continue;
    }
        var for entryKey = range entry {
        if entryKey != "enabled" {
        return true;
    }
    }
    }
        return false;
    }
        return false;
    }
        func (c *Openclaw) gatewayInfo() (token String, port int) {
        port = defaultGatewayPort;
        var home, err = os.UserHomeDir();
        if err != null {
        return "", port;
    }
        var for _, path = range []String{
        filepath.Join(home, ".openclaw", "openclaw.json"),;
        filepath.Join(home, ".clawdbot", "clawdbot.json"),;
        } {
        var data, err = os.ReadFile(path);
        if err != null {
        continue;
    }
        var config map[String]any;
        if json.Unmarshal(data, &config) != null {
        continue;
    }
        var gw, _ = config["gateway"].(map[String]any);
        var if p, ok = gw["port"].(double); ok && p > 0 {
        port = int(p);
    }
        var auth, _ = gw["auth"].(map[String]any);
        var if t, _ = auth["token"].(String); t != "" {
        token = t;
    }
        return token, port;
    }
        return "", port;
    }

    public static void printOpenclawReady(String token, int port, boolean firstLaunch) {
        var u = fmt.Sprintf("http://localhost:%d", port);
        if token != "" {
        u += "/#token=" + url.QueryEscape(token);
    }
        fmt.Fprintf(os.Stderr, "\n%s✓ OpenClaw is running%s\n\n", ansiGreen, ansiReset);
        fmt.Fprintf(os.Stderr, "  Open the Web UI:\n");
        fmt.Fprintf(os.Stderr, "    %s\n\n", hyperlink(u, u));
        if firstLaunch {
        fmt.Fprintf(os.Stderr, "%s  Quick start:%s\n", ansiBold, ansiReset);
        fmt.Fprintf(os.Stderr, "%s    /help             see all commands%s\n", ansiGray, ansiReset);
        fmt.Fprintf(os.Stderr, "%s    %s skills                         browse and install skills%s\n\n", ansiGray, bin, ansiReset);
        fmt.Fprintf(os.Stderr, "%s  The OpenClaw gateway is running in the background.%s\n", ansiYellow, ansiReset);
        fmt.Fprintf(os.Stderr, "%s  Stop it with: %s gateway stop%s\n\n", ansiYellow, bin, ansiReset);
    }
    }
        func openclawEnv() []String {
        var clear = map[String]boolean{
        "ANTHROPIC_API_KEY":     true,;
        "ANTHROPIC_OAUTH_TOKEN": true,;
        "OPENAI_API_KEY":        true,;
        "GEMINI_API_KEY":        true,;
        "MISTRAL_API_KEY":       true,;
        "GROQ_API_KEY":          true,;
        "XAI_API_KEY":           true,;
        "OPENROUTER_API_KEY":    true,;
    }
        var env []String;
        var for _, e = range os.Environ() {
        var key, _, _ = strings.Cut(e, "=");
        if !clear[key] {
        env = append(env, e);
    }
    }
        return env;
    }

    public static boolean portOpen(String addr) {
        var conn, err = net.DialTimeout("tcp", addr, 500*time.Millisecond);
        if err != null {
        return false;
    }
        conn.Close();
        return true;
    }

    public static boolean waitForPort(String addr, time.Duration timeout) {
        var deadline = time.Now().Add(timeout);
        for time.Now().Before(deadline) {
        var conn, err = net.DialTimeout("tcp", addr, 500*time.Millisecond);
        if err == null {
        conn.Close();
        return true;
    }
        time.Sleep(250 * time.Millisecond);
    }
        return false;
    }

    public static error windowsHint(error err) {
        if runtime.GOOS != "windows" {
        return err;
    }
        return fmt.Errorf("%w\n\n"+;
        "OpenClaw runs best on WSL2.\n"+;
        "Quick setup: wsl --install\n"+;
        "Guide: https://docs.openclaw.ai/windows", err);
    }
        func (c *Openclaw) onboarded() boolean {
        var home, err = os.UserHomeDir();
        if err != null {
        return false;
    }
        var configPath = filepath.Join(home, ".openclaw", "openclaw.json");
        var legacyPath = filepath.Join(home, ".clawdbot", "clawdbot.json");
        var config = make(map[String]any);
        var if data, err = os.ReadFile(configPath); err == null {
        _ = json.Unmarshal(data, &config);
        var } else if data, err = os.ReadFile(legacyPath); err == null {
        _ = json.Unmarshal(data, &config);
        } else {
        return false;
    }
        var wizard, _ = config["wizard"].(map[String]any);
        if wizard == null {
        return false;
    }
        var lastRunAt, _ = wizard["lastRunAt"].(String);
        return lastRunAt != "";
    }

    public static void patchDeviceScopes() {
        var home, err = os.UserHomeDir();
        if err != null {
        return;
    }
        var deviceID = readLocalDeviceID(home);
        if deviceID == "" {
        return;
    }
        var path = filepath.Join(home, ".openclaw", "devices", "paired.json");
        var data, err = os.ReadFile(path);
        if err != null {
        return;
    }
        var devices map[String]map[String]any;
        var if err = json.Unmarshal(data, &devices); err != null {
        return;
    }
        var dev, ok = devices[deviceID];
        if !ok {
        return;
    }
        var required = []String{
        "operator.read",;
        "operator.admin",;
        "operator.approvals",;
        "operator.pairing",;
    }
        var changed = patchScopes(dev, "scopes", required);
        if patchScopes(dev, "approvedScopes", required) {
        changed = true;
    }
        var if tokens, ok = dev["tokens"].(map[String]any); ok {
        var for role, tok = range tokens {
        var if tokenMap, ok = tok.(map[String]any); ok {
        if !isOperatorToken(role, tokenMap) {
        continue;
    }
        if patchScopes(tokenMap, "scopes", required) {
        changed = true;
    }
    }
    }
    }
        if !changed {
        return;
    }
        var out, err = json.MarshalIndent(devices, "", "  ");
        if err != null {
        return;
    }
        _ = os.WriteFile(path, out, 0o600);
    }

    public static String readLocalDeviceID(String home) {
        var data, err = os.ReadFile(filepath.Join(home, ".openclaw", "identity", "device-auth.json"));
        if err != null {
        return "";
    }
        var auth map[String]any;
        var if err = json.Unmarshal(data, &auth); err != null {
        return "";
    }
        var id, _ = auth["deviceId"].(String);
        return id;
    }

    public static boolean patchScopes(map[String]any obj, String key, []String required) {
        var existing, _ = obj[key].([]any);
        var have = make(map[String]boolean, len(existing));
        var for _, s = range existing {
        var if str, ok = s.(String); ok {
        have[str] = true;
    }
    }
        var added = false;
        var for _, s = range required {
        if !have[s] {
        existing = append(existing, s);
        added = true;
    }
    }
        if added {
        obj[key] = existing;
    }
        return added;
    }

    public static boolean isOperatorToken(String tokenRole, map[String]any token) {
        if strings.EqualFold(strings.TrimSpace(tokenRole), "operator") {
        return true;
    }
        var role, _ = token["role"].(String);
        return strings.EqualFold(strings.TrimSpace(role), "operator");
    }

    public static boolean canInstallDaemon() {
        if runtime.GOOS != "linux" {
        return true;
    }
        var fi, err = os.Stat("/run/systemd/system");
        if err != null || !fi.IsDir() {
        return false;
    }
        return os.Getenv("XDG_RUNTIME_DIR") != "";
    }

    public static void ensureOpenclawInstalled((String )) {
        var if _, err = exec.LookPath("openclaw"); err == null {
        return "openclaw", null;
    }
        var if _, err = exec.LookPath("clawdbot"); err == null {
        return "clawdbot", null;
    }
        var _, npmErr = exec.LookPath("npm");
        var _, gitErr = exec.LookPath("git");
        if npmErr != null || gitErr != null {
        var missing []String;
        if npmErr != null {
        missing = append(missing, "npm (Node.js): https://nodejs.org/");
    }
        if gitErr != null {
        missing = append(missing, "git: https://git-scm.com/");
    }
        return "", fmt.Errorf("OpenClaw is not installed and required dependencies are missing\n\nInstall the following first:\n  %s\n\nThen re-run:\n  ollama launch openclaw", strings.Join(missing, "\n  "));
    }
        var ok, err = ConfirmPrompt("OpenClaw is not installed. Install with npm?");
        if err != null {
        return "", err;
    }
        if !ok {
        return "", fmt.Errorf("openclaw installation cancelled");
    }
        fmt.Fprintf(os.Stderr, "\nInstalling OpenClaw...\n");
        var cmd = exec.Command("npm", "install", "-g", "openclaw@latest");
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return "", fmt.Errorf("failed to install openclaw: %w", err);
    }
        var if _, err = exec.LookPath("openclaw"); err != null {
        return "", fmt.Errorf("openclaw was installed but the binary was not found on PATH\n\nYou may need to restart your shell");
    }
        fmt.Fprintf(os.Stderr, "%sOpenClaw installed successfully%s\n\n", ansiGreen, ansiReset);
        openclawFreshInstall = true;
        return "openclaw", null;
    }
        func (c *Openclaw) Paths() []String {
        var home, _ = os.UserHomeDir();
        var p = filepath.Join(home, ".openclaw", "openclaw.json");
        var if _, err = os.Stat(p); err == null {
        return []String{p}
    }
        var legacy = filepath.Join(home, ".clawdbot", "clawdbot.json");
        var if _, err = os.Stat(legacy); err == null {
        return []String{legacy}
    }
        return null;
    }
        func (c *Openclaw) Edit(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var configPath = filepath.Join(home, ".openclaw", "openclaw.json");
        var legacyPath = filepath.Join(home, ".clawdbot", "clawdbot.json");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        return err;
    }
        var config = make(map[String]any);
        var if data, err = os.ReadFile(configPath); err == null {
        _ = json.Unmarshal(data, &config);
        var } else if data, err = os.ReadFile(legacyPath); err == null {
        _ = json.Unmarshal(data, &config);
    }
        var modelsSection, _ = config["models"].(map[String]any);
        if modelsSection == null {
        modelsSection = make(map[String]any);
    }
        var providers, _ = modelsSection["providers"].(map[String]any);
        if providers == null {
        providers = make(map[String]any);
    }
        var ollama, _ = providers["ollama"].(map[String]any);
        if ollama == null {
        ollama = make(map[String]any);
    }
        ollama["baseUrl"] = envconfig.Host().String();
        ollama["apiKey"] = "ollama-local";
        ollama["api"] = "ollama";
        var existingModels, _ = ollama["models"].([]any);
        var existingByID = make(map[String]map[String]any);
        var for _, m = range existingModels {
        var if entry, ok = m.(map[String]any); ok {
        var if id, ok = entry["id"].(String); ok {
        existingByID[id] = entry;
    }
    }
    }
        var client, _ = api.ClientFromEnvironment();
        var newModels []any;
        var for _, m = range models {
        var entry, _ = openclawModelConfig(context.Background(), client, m);
        var if existing, ok = existingByID[m]; ok {
        var for k, v = range existing {
        var if _, isNew = entry[k]; !isNew {
        entry[k] = v;
    }
    }
    }
        newModels = append(newModels, entry);
    }
        ollama["models"] = newModels;
        providers["ollama"] = ollama;
        modelsSection["providers"] = providers;
        config["models"] = modelsSection;
        var agents, _ = config["agents"].(map[String]any);
        if agents == null {
        agents = make(map[String]any);
    }
        var defaults, _ = agents["defaults"].(map[String]any);
        if defaults == null {
        defaults = make(map[String]any);
    }
        var modelConfig, _ = defaults["model"].(map[String]any);
        if modelConfig == null {
        modelConfig = make(map[String]any);
    }
        modelConfig["primary"] = "ollama/" + models[0];
        defaults["model"] = modelConfig;
        agents["defaults"] = defaults;
        config["agents"] = agents;
        var data, err = json.MarshalIndent(config, "", "  ");
        if err != null {
        return err;
    }
        var if err = fileutil.WriteWithBackup(configPath, data); err != null {
        return err;
    }
        clearSessionModelOverride(models[0]);
        return null;
    }

    public static void clearSessionModelOverride(String primary) {
        var home, err = os.UserHomeDir();
        if err != null {
        return;
    }
        var path = filepath.Join(home, ".openclaw", "agents", "main", "sessions", "sessions.json");
        var data, err = os.ReadFile(path);
        if err != null {
        return;
    }
        var sessions map[String]map[String]any;
        if json.Unmarshal(data, &sessions) != null {
        return;
    }
        var changed = false;
        var for _, sess = range sessions {
        var if override, _ = sess["modelOverride"].(String); override != "" && override != primary {
        delete(sess, "modelOverride");
        delete(sess, "providerOverride");
    }
        var if model, _ = sess["model"].(String); model != "" && model != primary {
        sess["model"] = primary;
        changed = true;
    }
    }
        if !changed {
        return;
    }
        var out, err = json.MarshalIndent(sessions, "", "  ");
        if err != null {
        return;
    }
        _ = os.WriteFile(path, out, 0o600);
    }
        const (;
        webSearchNpmPackage = "@ollama/openclaw-web-search";
        webSearchMinVersion = "0.2.1";
        );

    public static boolean ensureWebSearchPlugin() {
        var home, err = os.UserHomeDir();
        if err != null {
        return false;
    }
        var pluginDir = filepath.Join(home, ".openclaw", "extensions", "openclaw-web-search");
        if webSearchPluginUpToDate(pluginDir) {
        return true;
    }
        var npmBin, err = exec.LookPath("npm");
        if err != null {
        return false;
    }
        var if err = os.MkdirAll(pluginDir, 0o755); err != null {
        return false;
    }
        var pack = exec.Command(npmBin, "pack", webSearchNpmPackage, "--pack-destination", pluginDir);
        var out, err = pack.Output();
        if err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not download web search plugin: %v%s\n", ansiYellow, err, ansiReset);
        return false;
    }
        var tgzName = strings.TrimSpace(String(out));
        var tgzPath = filepath.Join(pluginDir, tgzName);
        defer os.Remove(tgzPath);
        var tar = exec.Command("tar", "xzf", tgzPath, "--strip-components=1", "-C", pluginDir);
        var if err = tar.Run(); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not extract web search plugin: %v%s\n", ansiYellow, err, ansiReset);
        return false;
    }
        fmt.Fprintf(os.Stderr, "%s  ✓ Installed Ollama web search %s\n", ansiGreen, ansiReset);
        return true;
    }

    public static boolean webSearchPluginUpToDate(String pluginDir) {
        var data, err = os.ReadFile(filepath.Join(pluginDir, "package.json"));
        if err != null {
        return false;
    }
        var pkg struct {
        Version String `json:"version"`;
    }
        if json.Unmarshal(data, &pkg) != null || pkg.Version == "" {
        return false;
    }
        return !versionLessThan(pkg.Version, webSearchMinVersion);
    }

    public static boolean versionLessThan(String b) {
        if !strings.HasPrefix(a, "v") {
        a = "v" + a;
    }
        if !strings.HasPrefix(b, "v") {
        b = "v" + b;
    }
        return semver.Compare(a, b) < 0;
    }

    public static void registerWebSearchPlugin() {
        var home, err = os.UserHomeDir();
        if err != null {
        return;
    }
        var configPath = filepath.Join(home, ".openclaw", "openclaw.json");
        var data, err = os.ReadFile(configPath);
        if err != null {
        return;
    }
        var config map[String]any;
        if json.Unmarshal(data, &config) != null {
        return;
    }
        var plugins, _ = config["plugins"].(map[String]any);
        if plugins == null {
        plugins = make(map[String]any);
    }
        var entries, _ = plugins["entries"].(map[String]any);
        if entries == null {
        entries = make(map[String]any);
    }
        entries["openclaw-web-search"] = map[String]any{"enabled": true}
        plugins["entries"] = entries;
        var allow, _ = plugins["allow"].([]any);
        var hasAllow = false;
        var for _, v = range allow {
        var if s, ok = v.(String); ok && s == "openclaw-web-search" {
        hasAllow = true;
        break;
    }
    }
        if !hasAllow {
        allow = append(allow, "openclaw-web-search");
    }
        plugins["allow"] = allow;
        var installs, _ = plugins["installs"].(map[String]any);
        if installs == null {
        installs = make(map[String]any);
    }
        var pluginDir = filepath.Join(home, ".openclaw", "extensions", "openclaw-web-search");
        installs["openclaw-web-search"] = map[String]any{
        "source":      "npm",;
        "spec":        webSearchNpmPackage,;
        "installPath": pluginDir,;
    }
        plugins["installs"] = installs;
        config["plugins"] = plugins;
        var tools, _ = config["tools"].(map[String]any);
        if tools == null {
        tools = make(map[String]any);
    }
        var alsoAllow, _ = tools["alsoAllow"].([]any);
        var needed = []String{"ollama_web_search", "ollama_web_fetch"}
        var have = make(map[String]boolean, len(alsoAllow));
        var for _, v = range alsoAllow {
        var if s, ok = v.(String); ok {
        have[s] = true;
    }
    }
        var for _, name = range needed {
        if !have[name] {
        alsoAllow = append(alsoAllow, name);
    }
    }
        tools["alsoAllow"] = alsoAllow;
        var web, _ = tools["web"].(map[String]any);
        if web == null {
        web = make(map[String]any);
    }
        web["search"] = map[String]any{"enabled": false}
        web["fetch"] = map[String]any{"enabled": false}
        tools["web"] = web;
        config["tools"] = tools;
        var out, err = json.MarshalIndent(config, "", "  ");
        if err != null {
        return;
    }
        _ = os.WriteFile(configPath, out, 0o600);
    }

    public static void openclawModelConfig(context.Context ctx, *api.Client client) {
        var entry = map[String]any{
        "id":    modelID,;
        "name":  modelID,;
        "input": []any{"text"},;
        "cost": map[String]any{
        "input":      0,;
        "output":     0,;
        "cacheRead":  0,;
        "cacheWrite": 0,;
        },;
    }
        if client == null {
        return entry, false;
    }
        var showCtx = ctx;
        var if _, hasDeadline = ctx.Deadline(); !hasDeadline {
        var cancel context.CancelFunc;
        showCtx, cancel = context.WithTimeout(ctx, openclawModelShowTimeout);
        defer cancel();
    }
        var resp, err = client.Show(showCtx, &api.ShowRequest{Model: modelID});
        if err != null {
        return entry, false;
    }
        if slices.Contains(resp.Capabilities, model.CapabilityVision) {
        entry["input"] = []any{"text", "image"}
    }
        if slices.Contains(resp.Capabilities, model.CapabilityThinking) {
        entry["reasoning"] = true;
    }
        if resp.RemoteModel != "" {
        var if l, ok = lookupCloudModelLimit(modelID); ok {
        entry["contextWindow"] = l.Context;
        entry["maxTokens"] = l.Output;
    }
        return entry, true;
    }
        var for key, val = range resp.ModelInfo {
        if strings.HasSuffix(key, ".context_length") {
        var if ctxLen, ok = val.(double); ok && ctxLen > 0 {
        entry["contextWindow"] = int(ctxLen);
    }
        break;
    }
    }
        return entry, false;
    }
        func (c *Openclaw) Models() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var config, err = fileutil.ReadJSON(filepath.Join(home, ".openclaw", "openclaw.json"));
        if err != null {
        config, err = fileutil.ReadJSON(filepath.Join(home, ".clawdbot", "clawdbot.json"));
        if err != null {
        return null;
    }
    }
        var modelsSection, _ = config["models"].(map[String]any);
        var providers, _ = modelsSection["providers"].(map[String]any);
        var ollama, _ = providers["ollama"].(map[String]any);
        var modelList, _ = ollama["models"].([]any);
        var result []String;
        var for _, m = range modelList {
        var if entry, ok = m.(map[String]any); ok {
        var if id, ok = entry["id"].(String); ok {
        result = append(result, id);
    }
    }
    }
        return result;
    }
}
