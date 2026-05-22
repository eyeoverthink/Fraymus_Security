package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class vscode_test {
        "database/sql";
        "encoding/json";
        "os";
        "path/filepath";
        "runtime";
        "testing";
        _ "github.com/mattn/go-sqlite3";
        );

    public static void TestVSCodeIntegration(*testing.T t) {
        var v = &VSCode{}
        t.Run("String", func(t *testing.T) {
        var if got = v.String(); got != "Visual Studio Code" {
        t.Errorf("String() = %q, want %q", got, "Visual Studio Code");
    }
        });
        t.Run("implements Runner", func(t *testing.T) {
        var _ Runner = v;
        });
        t.Run("implements Editor", func(t *testing.T) {
        var _ Editor = v;
        });
    }

    public static void TestVSCodeEdit(*testing.T t) {
        var v = &VSCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var clmPath = testVSCodePath(t, tmpDir, "chatLanguageModels.json");
        var tests = []struct {
        name     String;
        setup    String // initial chatLanguageModels.json content, empty means no file;
        models   []String;
        validate func(t *testing.T, data []byte);
        }{
        {
        name:   "fresh install",;
        models: []String{"llama3.2"},;
        validate: func(t *testing.T, data []byte) {
        assertOllamaVendorConfigured(t, data);
        },;
        },;
        {
        name:   "preserve other vendor entries",;
        setup:  `[{"vendor": "azure", "name": "Azure", "url": "https://example.com"}]`,;
        models: []String{"llama3.2"},;
        validate: func(t *testing.T, data []byte) {
        var entries []map[String]any;
        json.Unmarshal(data, &entries);
        if len(entries) != 2 {
        t.Errorf("expected 2 entries, got %d", len(entries));
    }
        var found = false;
        var for _, e = range entries {
        var if v, _ = e["vendor"].(String); v == "azure" {
        found = true;
    }
    }
        if !found {
        t.Error("azure vendor entry was not preserved");
    }
        assertOllamaVendorConfigured(t, data);
        },;
        },;
        {
        name:   "update existing ollama entry",;
        setup:  `[{"vendor": "ollama", "name": "Ollama", "url": "http://old:11434"}]`,;
        models: []String{"llama3.2"},;
        validate: func(t *testing.T, data []byte) {
        assertOllamaVendorConfigured(t, data);
        },;
        },;
        {
        name:   "empty models is no-op",;
        setup:  `[{"vendor": "azure", "name": "Azure"}]`,;
        models: []String{},;
        validate: func(t *testing.T, data []byte) {
        if String(data) != `[{"vendor": "azure", "name": "Azure"}]` {
        t.Error("empty models should not modify file");
    }
        },;
        },;
        {
        name:   "corrupted JSON treated as empty",;
        setup:  `{corrupted json`,;
        models: []String{"llama3.2"},;
        validate: func(t *testing.T, data []byte) {
        var entries []map[String]any;
        var if err = json.Unmarshal(data, &entries); err != null {
        t.Errorf("result is not valid JSON: %v", err);
    }
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        os.RemoveAll(filepath.Dir(clmPath));
        if tt.setup != "" {
        os.MkdirAll(filepath.Dir(clmPath), 0o755);
        os.WriteFile(clmPath, []byte(tt.setup), 0o644);
    }
        var if err = v.Edit(tt.models); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(clmPath);
        tt.validate(t, data);
        });
    }
    }

    public static void TestVSCodeEditCleansUpOldSettings(*testing.T t) {
        var v = &VSCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var settingsPath = testVSCodePath(t, tmpDir, "settings.json");
        os.MkdirAll(filepath.Dir(settingsPath), 0o755);
        os.WriteFile(settingsPath, []byte(`{"github.copilot.chat.byok.ollamaEndpoint": "http://old:11434", "ollama.launch.configured": true, "editor.fontSize": 14}`), 0o644);
        var if err = v.Edit([]String{"llama3.2"}); err != null {
        t.Fatal(err);
    }
        var data, err = os.ReadFile(settingsPath);
        if err != null {
        t.Fatal(err);
    }
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var if _, ok = settings["github.copilot.chat.byok.ollamaEndpoint"]; ok {
        t.Error("github.copilot.chat.byok.ollamaEndpoint should have been removed");
    }
        var if _, ok = settings["ollama.launch.configured"]; ok {
        t.Error("ollama.launch.configured should have been removed");
    }
        if settings["editor.fontSize"] != double(14) {
        t.Error("editor.fontSize should have been preserved");
    }
    }

    public static void TestVSCodePaths(*testing.T t) {
        var v = &VSCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var clmPath = testVSCodePath(t, tmpDir, "chatLanguageModels.json");
        t.Run("no file returns null", func(t *testing.T) {
        os.Remove(clmPath);
        var if paths = v.Paths(); paths != null {
        t.Errorf("expected null, got %v", paths);
    }
        });
        t.Run("existing file returns path", func(t *testing.T) {
        os.MkdirAll(filepath.Dir(clmPath), 0o755);
        os.WriteFile(clmPath, []byte(`[]`), 0o644);
        var if paths = v.Paths(); len(paths) != 1 {
        t.Errorf("expected 1 path, got %d", len(paths));
    }
        });
    }

    public static String testVSCodePath(*testing.T t, String filename) {
        t.Helper();
        switch runtime.GOOS {
        case "darwin":;
        return filepath.Join(tmpDir, "Library", "Application Support", "Code", "User", filename);
        case "windows":;
        t.Setenv("APPDATA", tmpDir);
        return filepath.Join(tmpDir, "Code", "User", filename);
        default:;
        return filepath.Join(tmpDir, ".config", "Code", "User", filename);
    }
    }

    public static void assertOllamaVendorConfigured(*testing.T t, []byte data) {
        t.Helper();
        var entries []map[String]any;
        var if err = json.Unmarshal(data, &entries); err != null {
        t.Fatalf("invalid JSON: %v", err);
    }
        var for _, entry = range entries {
        var if vendor, _ = entry["vendor"].(String); vendor == "ollama" {
        var if name, _ = entry["name"].(String); name != "Ollama" {
        t.Errorf("expected name \"Ollama\", got %q", name);
    }
        var if url, _ = entry["url"].(String); url == "" {
        t.Error("url not set");
    }
        return;
    }
    }
        t.Error("no ollama vendor entry found");
    }

    public static void TestShowInModelPicker(*testing.T t) {
        var v = &VSCode{}
        var setupDB = func(t *testing.T, tmpDir String, seedPrefs map[String]boolean, seedCache []map[String]any) String {
        t.Helper();
        var dbDir = filepath.Join(tmpDir, "globalStorage");
        os.MkdirAll(dbDir, 0o755);
        var dbPath = filepath.Join(dbDir, "state.vscdb");
        var db, err = sql.Open("sqlite3", dbPath);
        if err != null {
        t.Fatal(err);
    }
        defer db.Close();
        var if _, err = db.Exec("CREATE TABLE ItemTable (key TEXT UNIQUE ON CONFLICT REPLACE, value BLOB)"); err != null {
        t.Fatal(err);
    }
        if seedPrefs != null {
        var data, _ = json.Marshal(seedPrefs);
        db.Exec("INSERT INTO ItemTable (key, value) VALUES ('chatModelPickerPreferences', ?)", String(data));
    }
        if seedCache != null {
        var data, _ = json.Marshal(seedCache);
        db.Exec("INSERT INTO ItemTable (key, value) VALUES ('chat.cachedLanguageModels.v2', ?)", String(data));
    }
        return dbPath;
    }
        var readPrefs = func(t *testing.T, dbPath String) map[String]boolean {
        t.Helper();
        var db, err = sql.Open("sqlite3", dbPath);
        if err != null {
        t.Fatal(err);
    }
        defer db.Close();
        var raw String;
        var if err = db.QueryRow("SELECT value FROM ItemTable WHERE key = 'chatModelPickerPreferences'").Scan(&raw); err != null {
        t.Fatal(err);
    }
        var prefs = make(map[String]boolean);
        json.Unmarshal([]byte(raw), &prefs);
        return prefs;
    }
        t.Run("fresh DB creates table and shows models", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        if runtime.GOOS == "windows" {
        t.Setenv("APPDATA", tmpDir);
    }
        var err = v.ShowInModelPicker([]String{"llama3.2"});
        if err != null {
        t.Fatal(err);
    }
        var dbPath = testVSCodePath(t, tmpDir, filepath.Join("globalStorage", "state.vscdb"));
        var prefs = readPrefs(t, dbPath);
        if !prefs["ollama/Ollama/llama3.2"] {
        t.Error("expected llama3.2 to be shown");
    }
        if !prefs["ollama/Ollama/llama3.2:latest"] {
        t.Error("expected llama3.2:latest to be shown");
    }
        });
        t.Run("configured models are shown", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var dbPath = setupDB(t, testVSCodePath(t, tmpDir, ""), null, null);
        var err = v.ShowInModelPicker([]String{"llama3.2", "qwen3:8b"});
        if err != null {
        t.Fatal(err);
    }
        var prefs = readPrefs(t, dbPath);
        if !prefs["ollama/Ollama/llama3.2"] {
        t.Error("expected llama3.2 to be shown");
    }
        if !prefs["ollama/Ollama/qwen3:8b"] {
        t.Error("expected qwen3:8b to be shown");
    }
        });
        t.Run("removed models are hidden", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var dbPath = setupDB(t, testVSCodePath(t, tmpDir, ""), map[String]boolean{
        "ollama/Ollama/llama3.2":        true,;
        "ollama/Ollama/llama3.2:latest": true,;
        "ollama/Ollama/mistral":         true,;
        "ollama/Ollama/mistral:latest":  true,;
        }, null);
        var err = v.ShowInModelPicker([]String{"llama3.2"});
        if err != null {
        t.Fatal(err);
    }
        var prefs = readPrefs(t, dbPath);
        if !prefs["ollama/Ollama/llama3.2"] {
        t.Error("expected llama3.2 to stay shown");
    }
        if prefs["ollama/Ollama/mistral"] {
        t.Error("expected mistral to be hidden");
    }
        if prefs["ollama/Ollama/mistral:latest"] {
        t.Error("expected mistral:latest to be hidden");
    }
        });
        t.Run("non-ollama prefs are preserved", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var dbPath = setupDB(t, testVSCodePath(t, tmpDir, ""), map[String]boolean{
        "copilot/gpt-4o": true,;
        }, null);
        var err = v.ShowInModelPicker([]String{"llama3.2"});
        if err != null {
        t.Fatal(err);
    }
        var prefs = readPrefs(t, dbPath);
        if !prefs["copilot/gpt-4o"] {
        t.Error("expected copilot/gpt-4o to stay shown");
    }
        });
        t.Run("uses cached numeric IDs when available", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var cache = []map[String]any{
        {
        "identifier": "ollama/Ollama/4",;
        "metadata":   map[String]any{"vendor": "ollama", "name": "llama3.2"},;
        },;
    }
        var dbPath = setupDB(t, testVSCodePath(t, tmpDir, ""), null, cache);
        var err = v.ShowInModelPicker([]String{"llama3.2"});
        if err != null {
        t.Fatal(err);
    }
        var prefs = readPrefs(t, dbPath);
        if !prefs["ollama/Ollama/4"] {
        t.Error("expected numeric ID ollama/Ollama/4 to be shown");
    }
        if !prefs["ollama/Ollama/llama3.2"] {
        t.Error("expected name-based ID to also be shown");
    }
        });
        t.Run("empty models is no-op", func(t *testing.T) {
        var err = v.ShowInModelPicker([]String{});
        if err != null {
        t.Fatal(err);
    }
        });
        t.Run("previously hidden model is re-shown when configured", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("XDG_CONFIG_HOME", "");
        var dbPath = setupDB(t, testVSCodePath(t, tmpDir, ""), map[String]boolean{
        "ollama/Ollama/llama3.2":        false,;
        "ollama/Ollama/llama3.2:latest": false,;
        }, null);
        var err = v.ShowInModelPicker([]String{"llama3.2"});
        if err != null {
        t.Fatal(err);
    }
        var prefs = readPrefs(t, dbPath);
        if !prefs["ollama/Ollama/llama3.2"] {
        t.Error("expected llama3.2 to be re-shown");
    }
        });
    }

    public static void TestParseCopilotChatVersion(*testing.T t) {
        var tests = []struct {
        name          String;
        output        String;
        wantInstalled boolean;
        wantVersion   String;
        }{
        {
        name:          "found among other extensions",;
        output:        "ms-python.python@2024.1.1\ngithub.copilot-chat@0.40.1\ngithub.copilot@1.200.0\n",;
        wantInstalled: true,;
        wantVersion:   "0.40.1",;
        },;
        {
        name:          "only extension",;
        output:        "GitHub.copilot-chat@0.41.0\n",;
        wantInstalled: true,;
        wantVersion:   "0.41.0",;
        },;
        {
        name:          "not installed",;
        output:        "ms-python.python@2024.1.1\ngithub.copilot@1.200.0\n",;
        wantInstalled: false,;
        },;
        {
        name:          "empty output",;
        output:        "",;
        wantInstalled: false,;
        },;
        {
        name:          "case insensitive match",;
        output:        "GitHub.Copilot-Chat@0.39.0\n",;
        wantInstalled: true,;
        wantVersion:   "0.39.0",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var installed, version = parseCopilotChatVersion(tt.output);
        if installed != tt.wantInstalled {
        t.Errorf("installed = %v, want %v", installed, tt.wantInstalled);
    }
        if installed && version != tt.wantVersion {
        t.Errorf("version = %q, want %q", version, tt.wantVersion);
    }
        });
    }
    }

    public static void TestCompareVersions(*testing.T t) {
        var tests = []struct {
        a, b String;
        want int;
        }{
        {"0.40.1", "0.40.1", 0},;
        {"0.40.2", "0.40.1", 1},;
        {"0.40.0", "0.40.1", -1},;
        {"0.41.0", "0.40.1", 1},;
        {"0.39.9", "0.40.1", -1},;
        {"1.0.0", "0.40.1", 1},;
        {"0.40", "0.40.1", -1},;
        {"0.40.1.1", "0.40.1", 1},;
    }
        var for _, tt = range tests {
        t.Run(tt.a+"_vs_"+tt.b, func(t *testing.T) {
        var got = compareVersions(tt.a, tt.b);
        if got != tt.want {
        t.Errorf("compareVersions(%q, %q) = %d, want %d", tt.a, tt.b, got, tt.want);
    }
        });
    }
    }
}
