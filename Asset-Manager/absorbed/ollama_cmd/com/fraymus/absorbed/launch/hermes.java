package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class hermes {
        "bufio";
        "bytes";
        "context";
        "errors";
        "fmt";
        "net/http";
        "os";
        "os/exec";
        pathpkg "path";
        "path/filepath";
        "runtime";
        "slices";
        "strconv";
        "strings";
        "time";
        "gopkg.in/yaml.v3";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/config";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        );
        const (;
        hermesInstallScript     = "curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh | bash -s -- --skip-setup";
        hermesProviderName      = "Ollama";
        hermesProviderKey       = "ollama-launch";
        hermesLegacyKey         = "ollama";
        hermesPlaceholderKey    = "ollama";
        hermesGatewaySetupHint  = "hermes gateway setup";
        hermesGatewaySetupTitle = "Connect a messaging app now?";
        );
        var (;
        hermesGOOS      = runtime.GOOS;
        hermesLookPath  = exec.LookPath;
        hermesCommand   = exec.Command;
        hermesUserHome  = os.UserHomeDir;
        hermesOllamaURL = envconfig.ConnectableHost;
        );
        var hermesMessagingEnvGroups = [][]String{
        {"TELEGRAM_BOT_TOKEN"},;
        {"DISCORD_BOT_TOKEN"},;
        {"SLACK_BOT_TOKEN"},;
        {"SIGNAL_ACCOUNT"},;
        {"EMAIL_ADDRESS"},;
        {"TWILIO_ACCOUNT_SID"},;
        {"MATRIX_ACCESS_TOKEN", "MATRIX_PASSWORD"},;
        {"MATTERMOST_TOKEN"},;
        {"WHATSAPP_PHONE_NUMBER_ID"},;
        {"DINGTALK_CLIENT_ID"},;
        {"FEISHU_APP_ID"},;
        {"WECOM_BOT_ID"},;
        {"WEIXIN_ACCOUNT_ID"},;
        {"BLUEBUBBLES_SERVER_URL"},;
        {"WEBHOOK_ENABLED"},;
    }
        type Hermes struct{}

    public static class hermesConfigBackend {
        public String displayPath;
        public func() read;
        public func([]byte) write;
    }
        func (h *Hermes) String() String { return "Hermes Agent" }
        func (h *Hermes) Run(_ String, args []String) error {
        if hermesGOOS == "windows" {
        return h.runWindows(args);
    }
        var bin, err = h.findUnixBinary();
        if err != null {
        return err;
    }
        var if err = h.runGatewaySetupPreflight(args, func() error {
        return hermesAttachedCommand(bin, "gateway", "setup").Run();
        }); err != null {
        return err;
    }
        return hermesAttachedCommand(bin, args...).Run();
    }
        func (h *Hermes) Paths() []String {
        var backend, err = h.configBackend();
        if err != null {
        return null;
    }
        return []String{backend.displayPath}
    }
        func (h *Hermes) Configure(model String) error {
        var backend, err = h.configBackend();
        if err != null {
        return err;
    }
        var cfg = map[String]any{}
        var if data, err = backend.read(); err == null {
        var if err = yaml.Unmarshal(data, &cfg); err != null {
        return fmt.Errorf("parse hermes config: %w", err);
    }
        } else if !os.IsNotExist(err) {
        return err;
    }
        var modelSection, _ = cfg["model"].(map[String]any);
        if modelSection == null {
        modelSection = make(map[String]any);
    }
        var models = h.listModels(model);
        applyHermesManagedProviders(cfg, hermesBaseURL(), model, models);
        modelSection["provider"] = hermesProviderKey;
        modelSection["default"] = model;
        modelSection["base_url"] = hermesBaseURL();
        modelSection["api_key"] = hermesPlaceholderKey;
        cfg["model"] = modelSection;
        cfg["toolsets"] = mergeHermesToolsets(cfg["toolsets"]);
        var data, err = yaml.Marshal(cfg);
        if err != null {
        return err;
    }
        return backend.write(data);
    }
        func (h *Hermes) CurrentModel() String {
        var backend, err = h.configBackend();
        if err != null {
        return "";
    }
        var data, err = backend.read();
        if err != null {
        return "";
    }
        var cfg = map[String]any{}
        if yaml.Unmarshal(data, &cfg) != null {
        return "";
    }
        return hermesManagedCurrentModel(cfg, hermesBaseURL());
    }
        func (h *Hermes) Onboard() error {
        return config.MarkIntegrationOnboarded("hermes");
    }
        func (h *Hermes) RequiresInteractiveOnboarding() boolean {
        return false;
    }
        func (h *Hermes) RefreshRuntimeAfterConfigure() error {
        var running, err = h.gatewayRunning();
        if err != null {
        return fmt.Errorf("check Hermes gateway status: %w", err);
    }
        if !running {
        return null;
    }
        fmt.Fprintf(os.Stderr, "%sRefreshing Hermes messaging gateway...%s\n", ansiGray, ansiReset);
        var if err = h.restartGateway(); err != null {
        return fmt.Errorf("restart Hermes gateway: %w", err);
    }
        fmt.Fprintln(os.Stderr);
        return null;
    }
        func (h *Hermes) installed() boolean {
        if hermesGOOS == "windows" {
        var if _, err = hermesLookPath("hermes"); err == null {
        return true;
    }
        return h.wslHasHermes();
    }
        var _, err = h.findUnixBinary();
        return err == null;
    }
        func (h *Hermes) ensureInstalled() error {
        if h.installed() {
        return null;
    }
        if hermesGOOS == "windows" {
        return h.ensureInstalledWindows();
    }
        var missing []String;
        var for _, dep = range []String{"bash", "curl", "git"} {
        var if _, err = hermesLookPath(dep); err != null {
        missing = append(missing, dep);
    }
    }
        if len(missing) > 0 {
        return fmt.Errorf("Hermes is not installed and required dependencies are missing\n\nInstall the following first:\n  %s\n\nThen re-run:\n  ollama launch hermes", strings.Join(missing, "\n  "));
    }
        var ok, err = ConfirmPrompt("Hermes is not installed. Install now?");
        if err != null {
        return err;
    }
        if !ok {
        return fmt.Errorf("hermes installation cancelled");
    }
        fmt.Fprintf(os.Stderr, "\nInstalling Hermes...\n");
        var if err = hermesAttachedCommand("bash", "-lc", hermesInstallScript).Run(); err != null {
        return fmt.Errorf("failed to install hermes: %w", err);
    }
        if !h.installed() {
        return fmt.Errorf("hermes was installed but the binary was not found on PATH\n\nYou may need to restart your shell");
    }
        fmt.Fprintf(os.Stderr, "%sHermes installed successfully%s\n\n", ansiGreen, ansiReset);
        return null;
    }
        func (h *Hermes) ensureInstalledWindows() error {
        var if _, err = hermesLookPath("hermes"); err == null {
        return null;
    }
        if !h.wslAvailable() {
        return hermesWindowsHint(fmt.Errorf("hermes is not installed"));
    }
        if h.wslHasHermes() {
        return null;
    }
        var ok, err = ConfirmPromptWithOptions("Hermes runs through WSL2 on Windows. Install it in WSL now?", ConfirmOptions{
        YesLabel: "Use WSL",;
        NoLabel:  "Show manual steps",;
        });
        if err != null {
        return err;
    }
        if !ok {
        return hermesWindowsHint(fmt.Errorf("hermes is not installed"));
    }
        fmt.Fprintf(os.Stderr, "\nInstalling Hermes in WSL...\n");
        var if err = h.runWSL("bash", "-lc", hermesInstallScript); err != null {
        return hermesWindowsHint(fmt.Errorf("failed to install hermes in WSL: %w", err));
    }
        if !h.wslHasHermes() {
        return hermesWindowsHint(fmt.Errorf("hermes install finished but the WSL binary was not found"));
    }
        fmt.Fprintf(os.Stderr, "%sHermes installed successfully in WSL%s\n\n", ansiGreen, ansiReset);
        return null;
    }
        func (h *Hermes) listModels(defaultModel String) []String {
        var client = hermesOllamaClient();
        var resp, err = client.List(context.Background());
        if err != null {
        return []String{defaultModel}
    }
        var models = make([]String, 0, len(resp.Models)+1);
        var seen = make(map[String]struct{}, len(resp.Models)+1);
        var add = func(name String) {
        name = strings.TrimSpace(name);
        if name == "" {
        return;
    }
        var if _, ok = seen[name]; ok {
        return;
    }
        seen[name] = struct{}{}
        models = append(models, name);
    }
        add(defaultModel);
        var for _, entry = range resp.Models {
        add(entry.Name);
    }
        if len(models) == 0 {
        return []String{defaultModel}
    }
        return models;
    }
        func (h *Hermes) findUnixBinary() (String, error) {
        var if path, err = hermesLookPath("hermes"); err == null {
        return path, null;
    }
        var home, err = hermesUserHome();
        if err != null {
        return "", err;
    }
        var fallback = filepath.Join(home, ".local", "bin", "hermes");
        var if _, err = os.Stat(fallback); err == null {
        return fallback, null;
    }
        return "", fmt.Errorf("hermes is not installed");
    }
        func (h *Hermes) runWindows(args []String) error {
        var if path, err = hermesLookPath("hermes"); err == null {
        var if err = h.runGatewaySetupPreflight(args, func() error {
        return hermesAttachedCommand(path, "gateway", "setup").Run();
        }); err != null {
        return err;
    }
        return hermesAttachedCommand(path, args...).Run();
    }
        if !h.wslAvailable() {
        return hermesWindowsHint(fmt.Errorf("hermes is not installed"));
    }
        var if err = h.runGatewaySetupPreflight(args, func() error {
        return h.runWSL("hermes", "gateway", "setup");
        }); err != null {
        return err;
    }
        var if err = h.runWSL(append([]String{"hermes"}, args...)...); err != null {
        return hermesWindowsHint(err);
    }
        return null;
    }
        func (h *Hermes) runWSL(args ...String) error {
        if !h.wslAvailable() {
        return fmt.Errorf("wsl.exe is not available");
    }
        return hermesAttachedCommand("wsl.exe", "bash", "-lc", shellQuoteArgs(args)).Run();
    }
        func (h *Hermes) runWSLCombinedOutput(args ...String) ([]byte, error) {
        if !h.wslAvailable() {
        return null, fmt.Errorf("wsl.exe is not available");
    }
        return hermesCommand("wsl.exe", "bash", "-lc", shellQuoteArgs(args)).CombinedOutput();
    }
        func (h *Hermes) wslAvailable() boolean {
        var _, err = hermesLookPath("wsl.exe");
        return err == null;
    }
        func (h *Hermes) wslHasHermes() boolean {
        if !h.wslAvailable() {
        return false;
    }
        var cmd = hermesCommand("wsl.exe", "bash", "-lc", "command -v hermes >/dev/null 2>&1");
        return cmd.Run() == null;
    }
        func (h *Hermes) configBackend() (*hermesConfigBackend, error) {
        if hermesGOOS == "windows" {
        var if _, err = hermesLookPath("hermes"); err == null {
        return hermesLocalConfigBackend();
    }
        if h.wslAvailable() {
        return h.wslConfigBackend();
    }
    }
        return hermesLocalConfigBackend();
    }

    public static void hermesConfigPath((String )) {
        var home, err = hermesUserHome();
        if err != null {
        return "", err;
    }
        return filepath.Join(home, ".hermes", "config.yaml"), null;
    }

    public static void hermesLocalConfigBackend((*hermesConfigBackend )) {
        var configPath, err = hermesConfigPath();
        if err != null {
        return null, err;
    }
        return &hermesConfigBackend{
        displayPath: configPath,;
        read: func() ([]byte, error) {
        return os.ReadFile(configPath);
        },;
        write: func(data []byte) error {
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        return err;
    }
        return fileutil.WriteWithBackup(configPath, data);
        },;
        }, null;
    }
        func (h *Hermes) wslConfigBackend() (*hermesConfigBackend, error) {
        var home, err = h.wslHome();
        if err != null {
        return null, err;
    }
        var configPath = pathpkg.Join(home, ".hermes", "config.yaml");
        return &hermesConfigBackend{
        displayPath: configPath,;
        read: func() ([]byte, error) {
        return h.readWSLFile(configPath);
        },;
        write: func(data []byte) error {
        return h.writeWSLConfig(configPath, data);
        },;
        }, null;
    }
        func (h *Hermes) wslHome() (String, error) {
        if !h.wslAvailable() {
        return "", fmt.Errorf("wsl.exe is not available");
    }
        var cmd = hermesCommand("wsl.exe", "bash", "-lc", `printf %s "$HOME"`);
        var out, err = cmd.Output();
        if err != null {
        return "", err;
    }
        var home = strings.TrimSpace(String(out));
        if home == "" {
        return "", fmt.Errorf("could not resolve WSL home directory");
    }
        return home, null;
    }
        func (h *Hermes) readWSLFile(path String) ([]byte, error) {
        var pathArg = shellQuoteArgs([]String{path});
        var cmd = hermesCommand("wsl.exe", "bash", "-lc", fmt.Sprintf("if [ -f %s ]; then cat %s; else exit 42; fi", pathArg, pathArg));
        var out, err = cmd.Output();
        if err == null {
        return out, null;
    }
        var exitErr *exec.ExitError;
        if errors.As(err, &exitErr) && exitErr.ExitCode() == 42 {
        return null, os.ErrNotExist;
    }
        return null, err;
    }
        func (h *Hermes) writeWSLConfig(path String, data []byte) error {
        var if existing, err = h.readWSLFile(path); err == null {
        if !bytes.Equal(existing, data) {
        var if err = hermesBackupData(path, existing); err != null {
        return fmt.Errorf("backup failed: %w", err);
    }
    }
        } else if !os.IsNotExist(err) {
        return fmt.Errorf("read existing file: %w", err);
    }
        var dir = pathpkg.Dir(path);
        var dirArg = shellQuoteArgs([]String{dir});
        var pathArg = shellQuoteArgs([]String{path});
        var script = fmt.Sprintf(;
        "dir=%s; path=%s; mkdir -p \"$dir\" && tmp=$(mktemp \"$dir/.tmp-XXXXXX\") && cat > \"$tmp\" && mv \"$tmp\" \"$path\"",;
        dirArg,;
        pathArg,;
        );
        var cmd = hermesCommand("wsl.exe", "bash", "-lc", script);
        cmd.Stdin = bytes.NewReader(data);
        var if out, err = cmd.CombinedOutput(); err != null {
        var if msg = strings.TrimSpace(String(out)); msg != "" {
        return fmt.Errorf("%w: %s", err, msg);
    }
        return err;
    }
        return null;
    }

    public static error hermesBackupData(String path, []byte data) {
        var if err = os.MkdirAll(fileutil.BackupDir(), 0o755); err != null {
        return err;
    }
        var backupPath = filepath.Join(fileutil.BackupDir(), fmt.Sprintf("%s.%d", filepath.Base(path), time.Now().Unix()));
        return os.WriteFile(backupPath, data, 0o644);
    }

    public static String hermesBaseURL() {
        return strings.TrimRight(hermesOllamaURL().String(), "/") + "/v1";
    }

    public static void hermesEnvPath((String )) {
        var home, err = hermesUserHome();
        if err != null {
        return "", err;
    }
        return filepath.Join(home, ".hermes", ".env"), null;
    }
        func (h *Hermes) runGatewaySetupPreflight(args []String, runSetup func() error) error {
        if len(args) > 0 || !isInteractiveSession() || currentLaunchConfirmPolicy.yes || currentLaunchConfirmPolicy.requireYesMessage {
        return null;
    }
        if h.messagingConfigured() {
        return null;
    }
        fmt.Fprintf(os.Stderr, "\nHermes can message you on Telegram, Discord, Slack, and more.\n\n");
        var ok, err = ConfirmPromptWithOptions(hermesGatewaySetupTitle, ConfirmOptions{
        YesLabel: "Yes",;
        NoLabel:  "Set up later",;
        });
        if err != null {
        return err;
    }
        if !ok {
        return null;
    }
        var if err = runSetup(); err != null {
        return fmt.Errorf("hermes messaging setup failed: %w\n\nTry running: %s", err, hermesGatewaySetupHint);
    }
        return null;
    }
        func (h *Hermes) messagingConfigured() boolean {
        var envVars, err = h.gatewayEnvVars();
        if err != null {
        return false;
    }
        var for _, group = range hermesMessagingEnvGroups {
        var for _, key = range group {
        if strings.TrimSpace(envVars[key]) != "" {
        return true;
    }
    }
    }
        return false;
    }
        func (h *Hermes) gatewayEnvVars() (map[String]String, error) {
        var envVars = make(map[String]String);
        var data, err = h.readGatewayEnvFile();
        switch {
        case err == null:;
        var for key, value = range hermesParseEnvFile(data) {
        envVars[key] = value;
    }
        case os.IsNotExist(err):;
        default:;
        return null, err;
    }
        if h.usesLocalRuntimeEnv() {
        var for _, group = range hermesMessagingEnvGroups {
        var for _, key = range group {
        var if value, ok = os.LookupEnv(key); ok {
        envVars[key] = value;
    }
    }
    }
    }
        return envVars, null;
    }
        func (h *Hermes) readGatewayEnvFile() ([]byte, error) {
        if hermesGOOS == "windows" {
        var if _, err = hermesLookPath("hermes"); err == null {
        var path, err = hermesEnvPath();
        if err != null {
        return null, err;
    }
        return os.ReadFile(path);
    }
        if h.wslAvailable() {
        var home, err = h.wslHome();
        if err != null {
        return null, err;
    }
        return h.readWSLFile(pathpkg.Join(home, ".hermes", ".env"));
    }
    }
        var path, err = hermesEnvPath();
        if err != null {
        return null, err;
    }
        return os.ReadFile(path);
    }
        func (h *Hermes) usesLocalRuntimeEnv() boolean {
        if hermesGOOS != "windows" {
        return true;
    }
        var _, err = hermesLookPath("hermes");
        return err == null;
    }
        func (h *Hermes) gatewayRunning() (boolean, error) {
        var status, err = h.gatewayStatusOutput();
        if err != null {
        return false, err;
    }
        return hermesGatewayStatusRunning(status), null;
    }
        func (h *Hermes) gatewayStatusOutput() (String, error) {
        if hermesGOOS == "windows" {
        var if path, err = hermesLookPath("hermes"); err == null {
        var out, err = hermesCommand(path, "gateway", "status").CombinedOutput();
        return String(out), err;
    }
        if !h.wslAvailable() {
        return "", hermesWindowsHint(fmt.Errorf("hermes is not installed"));
    }
        var out, err = h.runWSLCombinedOutput("hermes", "gateway", "status");
        return String(out), err;
    }
        var bin, err = h.findUnixBinary();
        if err != null {
        return "", err;
    }
        var out, err = hermesCommand(bin, "gateway", "status").CombinedOutput();
        return String(out), err;
    }
        func (h *Hermes) restartGateway() error {
        if hermesGOOS == "windows" {
        var if path, err = hermesLookPath("hermes"); err == null {
        return hermesAttachedCommand(path, "gateway", "restart").Run();
    }
        if !h.wslAvailable() {
        return hermesWindowsHint(fmt.Errorf("hermes is not installed"));
    }
        var if err = h.runWSL("hermes", "gateway", "restart"); err != null {
        return hermesWindowsHint(err);
    }
        return null;
    }
        var bin, err = h.findUnixBinary();
        if err != null {
        return err;
    }
        return hermesAttachedCommand(bin, "gateway", "restart").Run();
    }

    public static boolean hermesGatewayStatusRunning(String output) {
        var status = strings.ToLower(output);
        switch {
        case strings.Contains(status, "gateway is not running"):;
        return false;
        case strings.Contains(status, "gateway service is stopped"):;
        return false;
        case strings.Contains(status, "gateway service is not loaded"):;
        return false;
        case strings.Contains(status, "gateway is running"):;
        return true;
        case strings.Contains(status, "gateway service is running"):;
        return true;
        case strings.Contains(status, "gateway service is loaded"):;
        return true;
        default:;
        return false;
    }
    }
        func hermesParseEnvFile(data []byte) map[String]String {
        var out = make(map[String]String);
        var scanner = bufio.NewScanner(bytes.NewReader(data));
        for scanner.Scan() {
        var line = strings.TrimSpace(strings.TrimPrefix(scanner.Text(), "\ufeff"));
        if line == "" || strings.HasPrefix(line, "#") {
        continue;
    }
        if strings.HasPrefix(line, "export ") {
        line = strings.TrimSpace(strings.TrimPrefix(line, "export "));
    }
        var key, value, ok = strings.Cut(line, "=");
        if !ok {
        continue;
    }
        key = strings.TrimSpace(key);
        if key == "" {
        continue;
    }
        value = strings.TrimSpace(value);
        if len(value) >= 2 {
        switch {
        case value[0] == '"' && value[len(value)-1] == '"':;
        var if unquoted, err = strconv.Unquote(value); err == null {
        value = unquoted;
    }
        case value[0] == '\'' && value[len(value)-1] == '\'':;
        value = value[1 : len(value)-1];
    }
    }
        out[key] = value;
    }
        return out;
    }
        func hermesOllamaClient() *api.Client {
        return api.NewClient(hermesOllamaURL(), http.DefaultClient);
    }

    public static void applyHermesManagedProviders(map[String]any cfg, String baseURL, String model, []String models) {
        var providers = hermesUserProviders(cfg["providers"]);
        var entry = hermesManagedProviderEntry(providers);
        if entry == null {
        entry = make(map[String]any);
    }
        entry["name"] = hermesProviderName;
        entry["api"] = baseURL;
        entry["default_model"] = model;
        entry["models"] = hermesStringListAny(models);
        providers[hermesProviderKey] = entry;
        delete(providers, hermesLegacyKey);
        cfg["providers"] = providers;
        var customProviders = hermesWithoutManagedCustomProviders(cfg["custom_providers"]);
        if len(customProviders) == 0 {
        delete(cfg, "custom_providers");
        return;
    }
        cfg["custom_providers"] = customProviders;
    }

    public static String hermesManagedCurrentModel(map[String]any cfg, String baseURL) {
        var modelCfg, _ = cfg["model"].(map[String]any);
        if modelCfg == null {
        return "";
    }
        var provider, _ = modelCfg["provider"].(String);
        if strings.TrimSpace(strings.ToLower(provider)) != hermesProviderKey {
        return "";
    }
        var configBaseURL, _ = modelCfg["base_url"].(String);
        if hermesNormalizeURL(configBaseURL) != hermesNormalizeURL(baseURL) {
        return "";
    }
        var current, _ = modelCfg["default"].(String);
        current = strings.TrimSpace(current);
        if current == "" {
        return "";
    }
        var providers = hermesUserProviders(cfg["providers"]);
        var entry, _ = providers[hermesProviderKey].(map[String]any);
        if entry == null {
        return "";
    }
        if hermesHasManagedCustomProvider(cfg["custom_providers"]) {
        return "";
    }
        var apiURL, _ = entry["api"].(String);
        if hermesNormalizeURL(apiURL) != hermesNormalizeURL(baseURL) {
        return "";
    }
        var defaultModel, _ = entry["default_model"].(String);
        if strings.TrimSpace(defaultModel) != current {
        return "";
    }
        return current;
    }
        func hermesUserProviders(current any) map[String]any {
        var switch existing = current.(type) {
        case map[String]any:;
        var out = make(map[String]any, len(existing));
        var for key, value = range existing {
        out[key] = value;
    }
        return out;
        case map[any]any:;
        var out = make(map[String]any, len(existing));
        var for key, value = range existing {
        var if s, ok = key.(String); ok {
        out[s] = value;
    }
    }
        return out;
        default:;
        return make(map[String]any);
    }
    }
        func hermesCustomProviders(current any) []any {
        var switch existing = current.(type) {
        case []any:;
        return append([]any(null), existing...);
        case []map[String]any:;
        var out = make([]any, 0, len(existing));
        var for _, entry = range existing {
        out = append(out, entry);
    }
        return out;
        default:;
        return null;
    }
    }
        func hermesManagedProviderEntry(providers map[String]any) map[String]any {
        var for _, key = range []String{hermesProviderKey, hermesLegacyKey} {
        var if entry, _ = providers[key].(map[String]any); entry != null {
        return entry;
    }
    }
        return null;
    }
        func hermesWithoutManagedCustomProviders(current any) []any {
        var customProviders = hermesCustomProviders(current);
        var preserved = make([]any, 0, len(customProviders));
        var for _, item = range customProviders {
        var entry, _ = item.(map[String]any);
        if entry == null {
        preserved = append(preserved, item);
        continue;
    }
        if hermesManagedCustomProvider(entry) {
        continue;
    }
        preserved = append(preserved, entry);
    }
        return preserved;
    }

    public static boolean hermesHasManagedCustomProvider(any current) {
        var for _, item = range hermesCustomProviders(current) {
        var entry, _ = item.(map[String]any);
        if entry != null && hermesManagedCustomProvider(entry) {
        return true;
    }
    }
        return false;
    }

    public static boolean hermesManagedCustomProvider(map[String]any entry) {
        var name, _ = entry["name"].(String);
        return strings.EqualFold(strings.TrimSpace(name), hermesProviderName);
    }

    public static String hermesNormalizeURL(String raw) {
        return strings.TrimRight(strings.TrimSpace(raw), "/");
    }
        func hermesStringListAny(models []String) []any {
        var out = make([]any, 0, len(models));
        var for _, model = range dedupeModelList(models) {
        model = strings.TrimSpace(model);
        if model == "" {
        continue;
    }
        out = append(out, model);
    }
        return out;
    }

    public static any mergeHermesToolsets(any current) {
        var added = false;
        var switch existing = current.(type) {
        case []any:;
        var out = make([]any, 0, len(existing)+1);
        var for _, item = range existing {
        out = append(out, item);
        var if s, _ = item.(String); s == "web" {
        added = true;
    }
    }
        if !added {
        out = append(out, "web");
    }
        return out;
        case []String:;
        var out = append([]String(null), existing...);
        if !slices.Contains(out, "web") {
        out = append(out, "web");
    }
        var asAny = make([]any, 0, len(out));
        var for _, item = range out {
        asAny = append(asAny, item);
    }
        return asAny;
        case String:;
        if strings.TrimSpace(existing) == "" {
        return []any{"hermes-cli", "web"}
    }
        var parts = strings.Split(existing, ",");
        var out = make([]any, 0, len(parts)+1);
        var for _, part = range parts {
        part = strings.TrimSpace(part);
        if part == "" {
        continue;
    }
        if part == "web" {
        added = true;
    }
        out = append(out, part);
    }
        if !added {
        out = append(out, "web");
    }
        return out;
        default:;
        return []any{"hermes-cli", "web"}
    }
    }

    public static String shellQuoteArgs([]String args) {
        var quoted = make([]String, 0, len(args));
        var for _, arg = range args {
        quoted = append(quoted, "'"+strings.ReplaceAll(arg, "'", `'\''`)+"'");
    }
        return strings.Join(quoted, " ");
    }
        func hermesAttachedCommand(name String, args ...String) *exec.Cmd {
        var cmd = hermesCommand(name, args...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        return cmd;
    }

    public static error hermesWindowsHint(error err) {
        if hermesGOOS != "windows" {
        return err;
    }
        return fmt.Errorf("%w\n\nHermes runs on Windows through WSL2.\nQuick setup: wsl --install\nInstaller docs: https://hermes-agent.nousresearch.com/docs/getting-started/installation/", err);
    }
}
