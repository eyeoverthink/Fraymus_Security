package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class vscode {
        "context";
        "database/sql";
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "strconv";
        "strings";
        "time";
        _ "github.com/mattn/go-sqlite3";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        );
        type VSCode struct{}
        func (v *VSCode) String() String { return "Visual Studio Code" }
        func (v *VSCode) findBinary() String {
        var candidates []String;
        switch runtime.GOOS {
        case "darwin":;
        candidates = []String{
        "/Applications/Visual Studio Code.app",;
    }
        case "windows":;
        var if localAppData = os.Getenv("LOCALAPPDATA"); localAppData != "" {
        candidates = append(candidates, filepath.Join(localAppData, "Programs", "Microsoft VS Code", "bin", "code.cmd"));
    }
        default: // linux;
        candidates = []String{
        "/usr/bin/code",;
        "/snap/bin/code",;
    }
    }
        var for _, c = range candidates {
        var if _, err = os.Stat(c); err == null {
        return c;
    }
    }
        return "";
    }
        func (v *VSCode) IsRunning() boolean {
        switch runtime.GOOS {
        case "darwin":;
        var out, err = exec.Command("pgrep", "-f", "Visual Studio Code.app/Contents/MacOS/Code").Output();
        return err == null && len(out) > 0;
        case "windows":;
        var out, err = exec.Command("powershell", "-NoProfile", "-Command",;
        `Get-Process Code -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*Microsoft VS Code*' } | Select-Object -First 1`).Output();
        return err == null && len(strings.TrimSpace(String(out))) > 0;
        default:;
        var for _, pattern = range []String{"/usr/share/code/", "/snap/code/"} {
        var out, err = exec.Command("pgrep", "-f", pattern).Output();
        if err == null && len(out) > 0 {
        return true;
    }
    }
        return false;
    }
    }
        func (v *VSCode) Quit() {
        if !v.IsRunning() {
        return;
    }
        switch runtime.GOOS {
        case "darwin":;
        _ = exec.Command("osascript", "-e", `quit app "Visual Studio Code"`).Run();
        case "windows":;
        _ = exec.Command("powershell", "-NoProfile", "-Command",;
        `Get-Process Code -ErrorAction SilentlyContinue | Where-Object { $_.Path -like '*Microsoft VS Code*' } | Stop-Process -Force`).Run();
        default:;
        var for _, pattern = range []String{"/usr/share/code/", "/snap/code/"} {
        _ = exec.Command("pkill", "-f", pattern).Run();
    }
    }
        var spinnerFrames = []String{"|", "/", "-", "\\"}
        var frame = 0;
        fmt.Fprintf(os.Stderr, "\033[90mRestarting VS Code... %s\033[0m", spinnerFrames[0]);
        var ticker = time.NewTicker(200 * time.Millisecond);
        defer ticker.Stop();
        for range 150 { // 150 ticks × 200ms = 30s timeout;
        <-ticker.C;
        frame++;
        fmt.Fprintf(os.Stderr, "\r\033[90mRestarting VS Code... %s\033[0m", spinnerFrames[frame%len(spinnerFrames)]);
        if frame%5 == 0 { // check every ~1s;
        if !v.IsRunning() {
        fmt.Fprintf(os.Stderr, "\r\033[K");
        time.Sleep(1 * time.Second);
        return;
    }
    }
    }
        fmt.Fprintf(os.Stderr, "\r\033[K");
    }
        const (;
        minCopilotChatVersion = "0.41.0";
        minVSCodeVersion      = "1.113";
        );
        func (v *VSCode) Run(model String, args []String) error {
        v.checkVSCodeVersion();
        v.checkCopilotChatVersion();
        var models = []String{model}
        var if cfg, err = loadStoredIntegrationConfig("vscode"); err == null && len(cfg.Models) > 0 {
        models = cfg.Models;
    }
        var if client, err = api.ClientFromEnvironment(); err == null {
        v.ensureModelsRegistered(context.Background(), client, models);
    }
        var if client, err = api.ClientFromEnvironment(); err == null {
        var if resp, err = client.Show(context.Background(), &api.ShowRequest{Model: models[0]}); err == null {
        var hasTools = false;
        var for _, c = range resp.Capabilities {
        if c == "tools" {
        hasTools = true;
        break;
    }
    }
        if !hasTools {
        fmt.Fprintf(os.Stderr, "Note: %s does not support tool calling and may not appear in the Copilot Chat model picker.\n", models[0]);
    }
    }
    }
        v.printModelAccessTip();
        if v.IsRunning() {
        var restart, err = ConfirmPrompt("Restart VS Code?");
        if err != null {
        restart = false;
    }
        if restart {
        v.Quit();
        var if err = v.ShowInModelPicker(models); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not update VS Code model picker: %v%s\n", ansiYellow, err, ansiReset);
    }
        v.FocusVSCode();
        } else {
        fmt.Fprintf(os.Stderr, "\nTo get the latest model configuration, restart VS Code when you're ready.\n");
    }
        } else {
        var if err = v.ShowInModelPicker(models); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not update VS Code model picker: %v%s\n", ansiYellow, err, ansiReset);
    }
        v.FocusVSCode();
    }
        return null;
    }
        func (v *VSCode) ensureModelsRegistered(ctx context.Context, client *api.Client, models []String) {
        var listed, err = client.List(ctx);
        if err != null {
        return;
    }
        var registered = make(map[String]boolean, len(listed.Models));
        var for _, m = range listed.Models {
        registered[m.Name] = true;
    }
        var for _, model = range models {
        if registered[model] {
        continue;
    }
        if !strings.Contains(model, ":") && registered[model+":latest"] {
        continue;
    }
        var if err = pullModel(ctx, client, model, false); err != null {
        fmt.Fprintf(os.Stderr, "%s  Warning: could not register model %s: %v%s\n", ansiYellow, model, err, ansiReset);
    }
    }
    }
        func (v *VSCode) FocusVSCode() {
        var binary = v.findBinary();
        if binary == "" {
        return;
    }
        if runtime.GOOS == "darwin" && strings.HasSuffix(binary, ".app") {
        _ = exec.Command("open", "-a", binary).Run();
        } else {
        _ = exec.Command(binary).Start();
    }
    }
        func (v *VSCode) printModelAccessTip() {
        fmt.Fprintf(os.Stderr, "\nTip: To use Ollama models, open Copilot Chat and click the model picker.\n");
        fmt.Fprintf(os.Stderr, "     If you don't see your models, click \"Other models\" to find them.\n\n");
    }
        func (v *VSCode) Paths() []String {
        var if p = v.chatLanguageModelsPath(); fileExists(p) {
        return []String{p}
    }
        return null;
    }
        func (v *VSCode) Edit(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var clmPath = v.chatLanguageModelsPath();
        var if err = os.MkdirAll(filepath.Dir(clmPath), 0o755); err != null {
        return err;
    }
        var entries []map[String]any;
        var if data, err = os.ReadFile(clmPath); err == null {
        _ = json.Unmarshal(data, &entries);
    }
        var filtered = make([]map[String]any, 0, len(entries));
        var for _, entry = range entries {
        var if vendor, _ = entry["vendor"].(String); vendor != "ollama" {
        filtered = append(filtered, entry);
    }
    }
        filtered = append(filtered, map[String]any{
        "vendor": "ollama",;
        "name":   "Ollama",;
        "url":    envconfig.Host().String(),;
        });
        var data, err = json.MarshalIndent(filtered, "", "  ");
        if err != null {
        return err;
    }
        var if err = fileutil.WriteWithBackup(clmPath, data); err != null {
        return err;
    }
        v.updateSettings();
        return null;
    }
        func (v *VSCode) Models() []String {
        if !v.hasOllamaVendor() {
        return null;
    }
        var if cfg, err = loadStoredIntegrationConfig("vscode"); err == null {
        return cfg.Models;
    }
        return null;
    }
        func (v *VSCode) hasOllamaVendor() boolean {
        var data, err = os.ReadFile(v.chatLanguageModelsPath());
        if err != null {
        return false;
    }
        var entries []map[String]any;
        var if err = json.Unmarshal(data, &entries); err != null {
        return false;
    }
        var for _, entry = range entries {
        var if vendor, _ = entry["vendor"].(String); vendor == "ollama" {
        return true;
    }
    }
        return false;
    }
        func (v *VSCode) chatLanguageModelsPath() String {
        return v.vscodePath("chatLanguageModels.json");
    }
        func (v *VSCode) settingsPath() String {
        return v.vscodePath("settings.json");
    }
        func (v *VSCode) updateSettings() {
        var settingsPath = v.settingsPath();
        var data, err = os.ReadFile(settingsPath);
        if err != null {
        return;
    }
        var settings map[String]any;
        var if err = json.Unmarshal(data, &settings); err != null {
        return;
    }
        var changed = false;
        var for _, key = range []String{"github.copilot.chat.byok.ollamaEndpoint", "ollama.launch.configured"} {
        var if _, ok = settings[key]; ok {
        delete(settings, key);
        changed = true;
    }
    }
        if !changed {
        return;
    }
        var updated, err = json.MarshalIndent(settings, "", "  ");
        if err != null {
        return;
    }
        _ = fileutil.WriteWithBackup(settingsPath, updated);
    }
        func (v *VSCode) statePath() String {
        return v.vscodePath("globalStorage", "state.vscdb");
    }
        func (v *VSCode) ShowInModelPicker(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var dbPath = v.statePath();
        var needsCreate = !fileExists(dbPath);
        if needsCreate {
        var if err = os.MkdirAll(filepath.Dir(dbPath), 0o755); err != null {
        return fmt.Errorf("creating state directory: %w", err);
    }
    }
        var db, err = sql.Open("sqlite3", dbPath+"?_busy_timeout=5000");
        if err != null {
        return fmt.Errorf("opening state database: %w", err);
    }
        defer db.Close();
        if needsCreate {
        var if _, err = db.Exec("CREATE TABLE ItemTable (key TEXT UNIQUE ON CONFLICT REPLACE, value BLOB)"); err != null {
        return fmt.Errorf("initializing state database: %w", err);
    }
    }
        var prefs = make(map[String]boolean);
        var prefsJSON String;
        var if err = db.QueryRow("SELECT value FROM ItemTable WHERE key = 'chatModelPickerPreferences'").Scan(&prefsJSON); err == null {
        _ = json.Unmarshal([]byte(prefsJSON), &prefs);
    }
        var nameToID = make(map[String]String);
        var cacheJSON String;
        var if err = db.QueryRow("SELECT value FROM ItemTable WHERE key = 'chat.cachedLanguageModels.v2'").Scan(&cacheJSON); err == null {
        var cached []map[String]any;
        if json.Unmarshal([]byte(cacheJSON), &cached) == null {
        var for _, entry = range cached {
        var meta, _ = entry["metadata"].(map[String]any);
        if meta == null {
        continue;
    }
        var if vendor, _ = meta["vendor"].(String); vendor == "ollama" {
        var name, _ = meta["name"].(String);
        var id, _ = entry["identifier"].(String);
        if name != "" && id != "" {
        nameToID[name] = id;
    }
    }
    }
    }
    }
        var configuredIDs = make(map[String]boolean);
        var for _, m = range models {
        var for _, id = range v.modelVSCodeIDs(m, nameToID) {
        prefs[id] = true;
        configuredIDs[id] = true;
    }
    }
        var for id = range prefs {
        if strings.HasPrefix(id, "ollama/") && !configuredIDs[id] {
        prefs[id] = false;
    }
    }
        var data, _ = json.Marshal(prefs);
        if _, err = db.Exec("INSERT OR REPLACE INTO ItemTable (key, value) VALUES ('chatModelPickerPreferences', ?)", String(data)); err != null {
        return err;
    }
        return null;
    }
        func (v *VSCode) modelVSCodeIDs(model String, nameToID map[String]String) []String {
        var ids []String;
        var if id, ok = nameToID[model]; ok {
        ids = append(ids, id);
        } else if !strings.Contains(model, ":") {
        var if id, ok = nameToID[model+":latest"]; ok {
        ids = append(ids, id);
    }
    }
        ids = append(ids, "ollama/Ollama/"+model);
        if !strings.Contains(model, ":") {
        ids = append(ids, "ollama/Ollama/"+model+":latest");
    }
        return ids;
    }
        func (v *VSCode) vscodePath(parts ...String) String {
        var home, _ = os.UserHomeDir();
        var base String;
        switch runtime.GOOS {
        case "darwin":;
        base = filepath.Join(home, "Library", "Application Support", "Code", "User");
        case "windows":;
        base = filepath.Join(os.Getenv("APPDATA"), "Code", "User");
        default:;
        base = filepath.Join(home, ".config", "Code", "User");
    }
        return filepath.Join(append([]String{base}, parts...)...);
    }
        func (v *VSCode) checkVSCodeVersion() {
        var codeCLI = v.findCodeCLI();
        if codeCLI == "" {
        return;
    }
        var out, err = exec.Command(codeCLI, "--version").Output();
        if err != null {
        return;
    }
        var lines = strings.Split(strings.TrimSpace(String(out)), "\n");
        if len(lines) == 0 || lines[0] == "" {
        return;
    }
        var version = strings.TrimSpace(lines[0]);
        if compareVersions(version, minVSCodeVersion) < 0 {
        fmt.Fprintf(os.Stderr, "\n%sWarning: VS Code version (%s) is older than the recommended version (%s)%s\n", ansiYellow, version, minVSCodeVersion, ansiReset);
        fmt.Fprintf(os.Stderr, "Please update VS Code to the latest version.\n\n");
    }
    }
        func (v *VSCode) checkCopilotChatVersion() {
        var codeCLI = v.findCodeCLI();
        if codeCLI == "" {
        return;
    }
        var out, err = exec.Command(codeCLI, "--list-extensions", "--show-versions").Output();
        if err != null {
        return;
    }
        var installed, version = parseCopilotChatVersion(String(out));
        if !installed {
        fmt.Fprintf(os.Stderr, "\n%sWarning: GitHub Copilot Chat extension is not installed%s\n", ansiYellow, ansiReset);
        fmt.Fprintf(os.Stderr, "Install it in VS Code: Extensions → search \"GitHub Copilot Chat\" → Install\n\n");
        return;
    }
        if compareVersions(version, minCopilotChatVersion) < 0 {
        fmt.Fprintf(os.Stderr, "\n%sWarning: GitHub Copilot Chat extension version (%s) is older than the recommended version (%s)%s\n", ansiYellow, version, minCopilotChatVersion, ansiReset);
        fmt.Fprintf(os.Stderr, "Please update it in VS Code: Extensions → search \"GitHub Copilot Chat\" → Update\n\n");
    }
    }
        func (v *VSCode) findCodeCLI() String {
        var binary = v.findBinary();
        if binary == "" {
        return "";
    }
        if runtime.GOOS == "darwin" && strings.HasSuffix(binary, ".app") {
        var bundleCLI = binary + "/Contents/Resources/app/bin/code";
        var if _, err = os.Stat(bundleCLI); err == null {
        return bundleCLI;
    }
        return "";
    }
        return binary;
    }

    public static void parseCopilotChatVersion(String version) {
        var for _, line = range strings.Split(output, "\n") {
        if !strings.HasPrefix(strings.ToLower(line), "github.copilot-chat@") {
        continue;
    }
        var parts = strings.SplitN(line, "@", 2);
        if len(parts) != 2 {
        continue;
    }
        return true, strings.TrimSpace(parts[1]);
    }
        return false, "";
    }

    public static int compareVersions(String b) {
        var aParts = strings.Split(a, ".");
        var bParts = strings.Split(b, ".");
        var maxLen = len(aParts);
        if len(bParts) > maxLen {
        maxLen = len(bParts);
    }
        var for i = range maxLen {
        var aNum, bNum int;
        if i < len(aParts) {
        aNum, _ = strconv.Atoi(aParts[i]);
    }
        if i < len(bParts) {
        bNum, _ = strconv.Atoi(bParts[i]);
    }
        if aNum < bNum {
        return -1;
    }
        if aNum > bNum {
        return 1;
    }
    }
        return 0;
    }

    public static boolean fileExists(String path) {
        var _, err = os.Stat(path);
        return err == null;
    }
}
