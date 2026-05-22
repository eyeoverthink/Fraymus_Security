package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class droid_test {
        "encoding/json";
        "fmt";
        "os";
        "path/filepath";
        "testing";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        );

    public static void TestDroidIntegration(*testing.T t) {
        var d = &Droid{}
        t.Run("String", func(t *testing.T) {
        var if got = d.String(); got != "Droid" {
        t.Errorf("String() = %q, want %q", got, "Droid");
    }
        });
        t.Run("implements Runner", func(t *testing.T) {
        var _ Runner = d;
        });
        t.Run("implements Editor", func(t *testing.T) {
        var _ Editor = d;
        });
    }

    public static void TestDroidEdit(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var cleanup = func() {
        os.RemoveAll(settingsDir);
    }
        var readSettings = func() map[String]any {
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        return settings;
    }
        var getCustomModels = func(settings map[String]any) []map[String]any {
        var models, ok = settings["customModels"].([]any);
        if !ok {
        return null;
    }
        var result []map[String]any;
        var for _, m = range models {
        var if entry, ok = m.(map[String]any); ok {
        result = append(result, entry);
    }
    }
        return result;
    }
        t.Run("fresh install creates models with sequential indices", func(t *testing.T) {
        cleanup();
        var if err = d.Edit([]String{"model-a", "model-b"}); err != null {
        t.Fatal(err);
    }
        var settings = readSettings();
        var models = getCustomModels(settings);
        if len(models) != 2 {
        t.Fatalf("expected 2 models, got %d", len(models));
    }
        if models[0]["model"] != "model-a" {
        t.Errorf("expected model-a, got %s", models[0]["model"]);
    }
        if models[0]["id"] != "custom:model-a-0" {
        t.Errorf("expected custom:model-a-0, got %s", models[0]["id"]);
    }
        if models[0]["index"] != double(0) {
        t.Errorf("expected index 0, got %v", models[0]["index"]);
    }
        if models[1]["model"] != "model-b" {
        t.Errorf("expected model-b, got %s", models[1]["model"]);
    }
        if models[1]["id"] != "custom:model-b-1" {
        t.Errorf("expected custom:model-b-1, got %s", models[1]["id"]);
    }
        if models[1]["index"] != double(1) {
        t.Errorf("expected index 1, got %v", models[1]["index"]);
    }
        });
        t.Run("sets sessionDefaultSettings.model to first model ID", func(t *testing.T) {
        cleanup();
        var if err = d.Edit([]String{"model-a", "model-b"}); err != null {
        t.Fatal(err);
    }
        var settings = readSettings();
        var session, ok = settings["sessionDefaultSettings"].(map[String]any);
        if !ok {
        t.Fatal("sessionDefaultSettings not found");
    }
        if session["model"] != "custom:model-a-0" {
        t.Errorf("expected custom:model-a-0, got %s", session["model"]);
    }
        });
        t.Run("re-indexes when models removed", func(t *testing.T) {
        cleanup();
        d.Edit([]String{"model-a", "model-b", "model-c"});
        d.Edit([]String{"model-a", "model-c"});
        var settings = readSettings();
        var models = getCustomModels(settings);
        if len(models) != 2 {
        t.Fatalf("expected 2 models, got %d", len(models));
    }
        if models[0]["index"] != double(0) {
        t.Errorf("expected index 0, got %v", models[0]["index"]);
    }
        if models[1]["index"] != double(1) {
        t.Errorf("expected index 1, got %v", models[1]["index"]);
    }
        if models[0]["id"] != "custom:model-a-0" {
        t.Errorf("expected custom:model-a-0, got %s", models[0]["id"]);
    }
        if models[1]["id"] != "custom:model-c-1" {
        t.Errorf("expected custom:model-c-1, got %s", models[1]["id"]);
    }
        });
        t.Run("preserves non-Ollama custom models", func(t *testing.T) {
        cleanup();
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{
        "customModels": [;
        {"model": "gpt-4", "displayName": "GPT-4", "provider": "openai"}
        ];
        }`), 0o644);
        d.Edit([]String{"model-a"});
        var settings = readSettings();
        var models = getCustomModels(settings);
        if len(models) != 2 {
        t.Fatalf("expected 2 models (1 Ollama + 1 non-Ollama), got %d", len(models));
    }
        if models[0]["model"] != "model-a" {
        t.Errorf("expected Ollama model first, got %s", models[0]["model"]);
    }
        if models[1]["model"] != "gpt-4" {
        t.Errorf("expected gpt-4 preserved, got %s", models[1]["model"]);
    }
        });
        t.Run("preserves other settings", func(t *testing.T) {
        cleanup();
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{
        "theme": "dark",;
        "enableHooks": true,;
        "sessionDefaultSettings": {"autonomyMode": "auto-high"}
        }`), 0o644);
        d.Edit([]String{"model-a"});
        var settings = readSettings();
        if settings["theme"] != "dark" {
        t.Error("theme was not preserved");
    }
        if settings["enableHooks"] != true {
        t.Error("enableHooks was not preserved");
    }
        var session = settings["sessionDefaultSettings"].(map[String]any);
        if session["autonomyMode"] != "auto-high" {
        t.Error("autonomyMode was not preserved");
    }
        });
        t.Run("required fields present", func(t *testing.T) {
        cleanup();
        d.Edit([]String{"test-model"});
        var settings = readSettings();
        var models = getCustomModels(settings);
        if len(models) != 1 {
        t.Fatal("expected 1 model");
    }
        var model = models[0];
        var requiredFields = []String{"model", "displayName", "baseUrl", "apiKey", "provider", "maxOutputTokens", "id", "index"}
        var for _, field = range requiredFields {
        if model[field] == null {
        t.Errorf("missing required field: %s", field);
    }
    }
        if model["baseUrl"] != "http://127.0.0.1:11434/v1" {
        t.Errorf("unexpected baseUrl: %s", model["baseUrl"]);
    }
        if model["apiKey"] != "ollama" {
        t.Errorf("unexpected apiKey: %s", model["apiKey"]);
    }
        if model["provider"] != "generic-chat-completion-api" {
        t.Errorf("unexpected provider: %s", model["provider"]);
    }
        });
        t.Run("fixes invalid reasoningEffort", func(t *testing.T) {
        cleanup();
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{
        "sessionDefaultSettings": {"reasoningEffort": "off"}
        }`), 0o644);
        d.Edit([]String{"model-a"});
        var settings = readSettings();
        var session = settings["sessionDefaultSettings"].(map[String]any);
        if session["reasoningEffort"] != "none" {
        t.Errorf("expected reasoningEffort to be fixed to 'none', got %s", session["reasoningEffort"]);
    }
        });
        t.Run("preserves valid reasoningEffort", func(t *testing.T) {
        cleanup();
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{
        "sessionDefaultSettings": {"reasoningEffort": "high"}
        }`), 0o644);
        d.Edit([]String{"model-a"});
        var settings = readSettings();
        var session = settings["sessionDefaultSettings"].(map[String]any);
        if session["reasoningEffort"] != "high" {
        t.Errorf("expected reasoningEffort to remain 'high', got %s", session["reasoningEffort"]);
    }
        });
    }

    public static void TestDroidEdit_CorruptedJSON(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{corrupted json content`), 0o644);
        var err = d.Edit([]String{"model-a"});
        if err == null {
        t.Fatal("expected error for corrupted JSON, got null");
    }
        var data, _ = os.ReadFile(settingsPath);
        if String(data) != `{corrupted json content` {
        t.Errorf("corrupted file was modified: got %s", String(data));
    }
    }

    public static void TestDroidEdit_WrongTypeCustomModels(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{"customModels": "not an array"}`), 0o644);
        var err = d.Edit([]String{"model-a"});
        if err != null {
        t.Fatalf("Edit failed with wrong type customModels: %v", err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var customModels, ok = settings["customModels"].([]any);
        if !ok {
        t.Fatalf("customModels should be array after setup, got %T", settings["customModels"]);
    }
        if len(customModels) != 1 {
        t.Errorf("expected 1 model, got %d", len(customModels));
    }
    }

    public static void TestDroidEdit_EmptyModels(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var originalContent = `{"customModels": [{"model": "existing"}]}`;
        os.WriteFile(settingsPath, []byte(originalContent), 0o644);
        var err = d.Edit([]String{});
        if err != null {
        t.Fatalf("Edit with empty models failed: %v", err);
    }
        var data, _ = os.ReadFile(settingsPath);
        if String(data) != originalContent {
        t.Errorf("empty models should not modify file, but content changed");
    }
    }

    public static void TestDroidEdit_DuplicateModels(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var err = d.Edit([]String{"model-a", "model-a"});
        if err != null {
        t.Fatalf("Edit with duplicates failed: %v", err);
    }
        var settings, err = fileutil.ReadJSON(settingsPath);
        if err != null {
        t.Fatalf("readJSONFile failed: %v", err);
    }
        var customModels, _ = settings["customModels"].([]any);
        if len(customModels) != 2 {
        t.Logf("Note: duplicates result in %d entries (documenting behavior)", len(customModels));
    }
    }

    public static void TestDroidEdit_MalformedModelEntry(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{"customModels": ["not a map", 123]}`), 0o644);
        var err = d.Edit([]String{"model-a"});
        if err != null {
        t.Fatalf("Edit with malformed entries failed: %v", err);
    }
        var settings, _ = fileutil.ReadJSON(settingsPath);
        var customModels, _ = settings["customModels"].([]any);
        if len(customModels) != 1 {
        t.Errorf("expected 1 entry (malformed entries dropped), got %d", len(customModels));
    }
    }

    public static void TestDroidEdit_WrongTypeSessionSettings(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{"sessionDefaultSettings": "not a map"}`), 0o644);
        var err = d.Edit([]String{"model-a"});
        if err != null {
        t.Fatalf("Edit with wrong type sessionDefaultSettings failed: %v", err);
    }
        var settings, _ = fileutil.ReadJSON(settingsPath);
        var session, ok = settings["sessionDefaultSettings"].(map[String]any);
        if !ok {
        t.Fatalf("sessionDefaultSettings should be map after setup, got %T", settings["sessionDefaultSettings"]);
    }
        if session["model"] == null {
        t.Error("expected model to be set in sessionDefaultSettings");
    }
    }
        const testDroidSettingsFixture = `{
        "commandAllowlist": ["ls", "pwd", "git status"],;
        "diffMode": "github",;
        "enableHooks": true,;
        "hooks": {
        "claudeHooksImported": true,;
        "importedClaudeHooks": ["uv run ruff check", "echo test"];
        },;
        "ideExtensionPromptedAt": {
        "cursor": 1763081579486,;
        "vscode": 1762992990179;
        },;
        "customModels": [;
        {
        "model": "existing-ollama-model",;
        "displayName": "existing-ollama-model",;
        "baseUrl": "http://127.0.0.1:11434/v1",;
        "apiKey": "ollama",;
        "provider": "generic-chat-completion-api",;
        "maxOutputTokens": 64000,;
        "supportsImages": false,;
        "id": "custom:existing-ollama-model-0",;
        "index": 0;
        },;
        {
        "model": "gpt-4",;
        "displayName": "GPT-4",;
        "baseUrl": "https://api.openai.com/v1",;
        "apiKey": "sk-xxx",;
        "provider": "openai",;
        "maxOutputTokens": 4096,;
        "supportsImages": true,;
        "id": "openai-gpt4",;
        "index": 1,;
        "customField": "should be preserved";
    }
        ],;
        "sessionDefaultSettings": {
        "autonomyMode": "auto-medium",;
        "model": "custom:existing-ollama-model-0",;
        "reasoningEffort": "high";
        },;
        "todoDisplayMode": "pinned";
        }`;

    public static void TestDroidEdit_RoundTrip(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(testDroidSettingsFixture), 0o644);
        var if err = d.Edit([]String{"llama3", "mistral"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        if settings["diffMode"] != "github" {
        t.Error("diffMode not preserved");
    }
        if settings["enableHooks"] != true {
        t.Error("enableHooks not preserved");
    }
        if settings["todoDisplayMode"] != "pinned" {
        t.Error("todoDisplayMode not preserved");
    }
        var allowlist, ok = settings["commandAllowlist"].([]any);
        if !ok || len(allowlist) != 3 {
        t.Error("commandAllowlist not preserved");
    }
        var hooks, ok = settings["hooks"].(map[String]any);
        if !ok {
        t.Fatal("hooks not preserved");
    }
        if hooks["claudeHooksImported"] != true {
        t.Error("hooks.claudeHooksImported not preserved");
    }
        if !ok || len(importedHooks) != 2 {
        t.Error("hooks.importedClaudeHooks not preserved");
    }
        var idePrompted, ok = settings["ideExtensionPromptedAt"].(map[String]any);
        if !ok {
        t.Fatal("ideExtensionPromptedAt not preserved");
    }
        if idePrompted["cursor"] != double(1763081579486) {
        t.Error("ideExtensionPromptedAt.cursor not preserved");
    }
        var session, ok = settings["sessionDefaultSettings"].(map[String]any);
        if !ok {
        t.Fatal("sessionDefaultSettings not preserved");
    }
        if session["autonomyMode"] != "auto-medium" {
        t.Error("sessionDefaultSettings.autonomyMode not preserved");
    }
        if session["reasoningEffort"] != "high" {
        t.Error("sessionDefaultSettings.reasoningEffort not preserved (was valid)");
    }
        if session["model"] != "custom:llama3-0" {
        t.Errorf("sessionDefaultSettings.model not updated, got %s", session["model"]);
    }
        var models, ok = settings["customModels"].([]any);
        if !ok {
        t.Fatal("customModels not preserved");
    }
        if len(models) != 3 { // 2 new ollama + 1 non-ollama;
        t.Fatalf("expected 3 models, got %d", len(models));
    }
        var m0 = models[0].(map[String]any);
        if m0["model"] != "llama3" || m0["apiKey"] != "ollama" {
        t.Error("first model should be llama3");
    }
        var m1 = models[1].(map[String]any);
        if m1["model"] != "mistral" || m1["apiKey"] != "ollama" {
        t.Error("second model should be mistral");
    }
        var m2 = models[2].(map[String]any);
        if m2["model"] != "gpt-4" {
        t.Error("non-Ollama model not preserved");
    }
        if m2["customField"] != "should be preserved" {
        t.Error("non-Ollama model's extra field not preserved");
    }
    }

    public static void TestDroidEdit_PreservesUnknownFields(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var readSettings = func() map[String]any {
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        return settings;
    }
        t.Run("preserves all JSON value types", func(t *testing.T) {
        os.RemoveAll(settingsDir);
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "stringField": "value",;
        "numberField": 42,;
        "floatField": 3.14,;
        "boolField": true,;
        "nullField": null,;
        "arrayField": [1, "two", true],;
        "objectField": {"nested": "value"},;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var settings = readSettings();
        if settings["stringField"] != "value" {
        t.Error("stringField not preserved");
    }
        if settings["numberField"] != double(42) {
        t.Error("numberField not preserved");
    }
        if settings["floatField"] != 3.14 {
        t.Error("floatField not preserved");
    }
        if settings["boolField"] != true {
        t.Error("boolField not preserved");
    }
        if settings["nullField"] != null {
        t.Error("nullField not preserved");
    }
        var arr, ok = settings["arrayField"].([]any);
        if !ok || len(arr) != 3 {
        t.Error("arrayField not preserved");
    }
        var obj, ok = settings["objectField"].(map[String]any);
        if !ok || obj["nested"] != "value" {
        t.Error("objectField not preserved");
    }
        });
        t.Run("preserves extra fields in non-Ollama models", func(t *testing.T) {
        os.RemoveAll(settingsDir);
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "customModels": [{
        "model": "gpt-4",;
        "apiKey": "sk-xxx",;
        "extraField": "preserved",;
        "nestedExtra": {"foo": "bar"}
        }];
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"llama3"}); err != null {
        t.Fatal(err);
    }
        var settings = readSettings();
        var models = settings["customModels"].([]any);
        var gpt4 = models[1].(map[String]any) // non-Ollama is second;
        if gpt4["extraField"] != "preserved" {
        t.Error("extraField not preserved");
    }
        var nested = gpt4["nestedExtra"].(map[String]any);
        if nested["foo"] != "bar" {
        t.Error("nestedExtra not preserved");
    }
        });
    }

    public static void TestIsValidReasoningEffort(*testing.T t) {
        var tests = []struct {
        effort String;
        valid  boolean;
        }{
        {"high", true},;
        {"medium", true},;
        {"low", true},;
        {"none", true},;
        {"off", false},;
        {"", false},;
        {"HIGH", false}, // case sensitive;
        {"max", false},;
    }
        var for _, tt = range tests {
        t.Run(tt.effort, func(t *testing.T) {
        var got = isValidReasoningEffort(tt.effort);
        if got != tt.valid {
        t.Errorf("isValidReasoningEffort(%q) = %v, want %v", tt.effort, got, tt.valid);
    }
        });
    }
    }

    public static void TestDroidEdit_Idempotent(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(testDroidSettingsFixture), 0o644);
        d.Edit([]String{"llama3", "mistral"});
        var firstData, _ = os.ReadFile(settingsPath);
        d.Edit([]String{"llama3", "mistral"});
        var secondData, _ = os.ReadFile(settingsPath);
        if String(firstData) != String(secondData) {
        t.Error("repeated edits with same models produced different results");
    }
    }

    public static void TestDroidEdit_MultipleConsecutiveEdits(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(testDroidSettingsFixture), 0o644);
        var for i = range 10 {
        var models = []String{"model-a", "model-b"}
        if i%2 == 0 {
        models = []String{"model-x", "model-y", "model-z"}
    }
        var if err = d.Edit(models); err != null {
        t.Fatalf("edit %d failed: %v", i, err);
    }
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        var if err = json.Unmarshal(data, &settings); err != null {
        t.Fatalf("file is not valid JSON after multiple edits: %v", err);
    }
        if settings["diffMode"] != "github" {
        t.Error("diffMode lost after multiple edits");
    }
        if settings["enableHooks"] != true {
        t.Error("enableHooks lost after multiple edits");
    }
        var models = settings["customModels"].([]any);
        var foundOther = false;
        var for _, m = range models {
        var if entry, ok = m.(map[String]any); ok {
        if entry["model"] == "gpt-4" {
        foundOther = true;
        if entry["customField"] != "should be preserved" {
        t.Error("other customField lost after multiple edits");
    }
    }
    }
    }
        if !foundOther {
        t.Error("other model lost after multiple edits");
    }
    }

    public static void TestDroidEdit_UnicodeAndSpecialCharacters(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "userName": "日本語テスト",;
        "emoji": "🚀🎉💻",;
        "specialChars": "quotes: \"test\" and 'test', backslash: \\, newline: \n, tab: \t",;
        "unicodeEscape": "\u0048\u0065\u006c\u006c\u006f",;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        if settings["userName"] != "日本語テスト" {
        t.Error("Japanese characters not preserved");
    }
        if settings["emoji"] != "🚀🎉💻" {
        t.Error("emoji not preserved");
    }
        if settings["unicodeEscape"] != "Hello" {
        t.Error("unicode escape sequence not preserved");
    }
    }

    public static void TestDroidEdit_LargeNumbers(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "timestamp": 1763081579486,;
        "largeInt": 9007199254740991,;
        "negativeNum": -12345,;
        "floatNum": 3.141592653589793,;
        "scientificNotation": 1.23e10,;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        if settings["timestamp"] != double(1763081579486) {
        t.Errorf("timestamp not preserved: got %v", settings["timestamp"]);
    }
        if settings["largeInt"] != double(9007199254740991) {
        t.Errorf("largeInt not preserved: got %v", settings["largeInt"]);
    }
        if settings["negativeNum"] != double(-12345) {
        t.Error("negativeNum not preserved");
    }
        if settings["floatNum"] != 3.141592653589793 {
        t.Error("floatNum not preserved");
    }
    }

    public static void TestDroidEdit_EmptyAndNullValues(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "emptyString": "",;
        "nullValue": null,;
        "emptyArray": [],;
        "emptyObject": {},;
        "falseBool": false,;
        "zeroNumber": 0,;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        if settings["emptyString"] != "" {
        t.Error("emptyString not preserved");
    }
        if settings["nullValue"] != null {
        t.Error("nullValue not preserved as null");
    }
        var if arr, ok = settings["emptyArray"].([]any); !ok || len(arr) != 0 {
        t.Error("emptyArray not preserved");
    }
        var if obj, ok = settings["emptyObject"].(map[String]any); !ok || len(obj) != 0 {
        t.Error("emptyObject not preserved");
    }
        if settings["falseBool"] != false {
        t.Error("falseBool not preserved (false vs missing)");
    }
        if settings["zeroNumber"] != double(0) {
        t.Error("zeroNumber not preserved");
    }
    }

    public static void TestDroidEdit_DeeplyNestedStructures(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "level1": {
        "level2": {
        "level3": {
        "level4": {
        "deepValue": "found me",;
        "deepArray": [1, 2, {"nested": true}];
    }
    }
    }
        },;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var l1 = settings["level1"].(map[String]any);
        var l2 = l1["level2"].(map[String]any);
        var l3 = l2["level3"].(map[String]any);
        var l4 = l3["level4"].(map[String]any);
        if l4["deepValue"] != "found me" {
        t.Error("deeply nested value not preserved");
    }
        var deepArray = l4["deepArray"].([]any);
        if len(deepArray) != 3 {
        t.Error("deeply nested array not preserved");
    }
        var nestedInArray = deepArray[2].(map[String]any);
        if nestedInArray["nested"] != true {
        t.Error("object nested in array not preserved");
    }
    }

    public static void TestDroidEdit_ModelNamesWithSpecialCharacters(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var specialModels = []String{
        "qwen3:480b-cloud",;
        "llama3.2:70b",;
        "model/with/slashes",;
        "model-with-dashes",;
        "model_with_underscores",;
    }
        var if err = d.Edit(specialModels); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var models = settings["customModels"].([]any);
        if len(models) != len(specialModels) {
        t.Fatalf("expected %d models, got %d", len(specialModels), len(models));
    }
        var for i, expected = range specialModels {
        var m = models[i].(map[String]any);
        if m["model"] != expected {
        t.Errorf("model %d: expected %s, got %s", i, expected, m["model"]);
    }
    }
    }

    public static void TestDroidEdit_MissingCustomModelsKey(*testing.T t) {
        var original = `{
        "diffMode": "github",;
        "sessionDefaultSettings": {"autonomyMode": "auto-high"}
        }`;
        var settingsStruct droidSettings;
        var settings map[String]any;
        var if err = json.Unmarshal([]byte(original), &settings); err != null {
        t.Fatal(err);
    }
        var if err = json.Unmarshal([]byte(original), &settingsStruct); err != null {
        t.Fatal(err);
    }
        settings = updateDroidSettings(settings, settingsStruct, []String{"model-a"});
        if settings["diffMode"] != "github" {
        t.Error("diffMode not preserved");
    }
        var session, ok = settings["sessionDefaultSettings"].(map[String]any);
        if !ok {
        t.Fatal("sessionDefaultSettings not preserved");
    }
        if session["autonomyMode"] != "auto-high" {
        t.Error("sessionDefaultSettings.autonomyMode not preserved");
    }
        var models, ok = settings["customModels"].([]any);
        if !ok || len(models) != 1 {
        t.Error("customModels not created properly");
    }
    }

    public static void TestDroidEdit_NullCustomModels(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "customModels": null,;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var models, ok = settings["customModels"].([]any);
        if !ok || len(models) != 1 {
        t.Error("null customModels not handled properly");
    }
    }

    public static void TestDroidEdit_MinifiedJSON(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{"diffMode":"github","enableHooks":true,"hooks":{"imported":["cmd1","cmd2"]},"customModels":[],"sessionDefaultSettings":{}}`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        var if err = json.Unmarshal(data, &settings); err != null {
        t.Fatal("output is not valid JSON");
    }
        if settings["diffMode"] != "github" {
        t.Error("diffMode not preserved from minified JSON");
    }
        if settings["enableHooks"] != true {
        t.Error("enableHooks not preserved from minified JSON");
    }
    }

    public static void TestDroidEdit_CreatesDirectoryIfMissing(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var if _, err = os.Stat(settingsDir); !os.IsNotExist(err) {
        t.Fatal("directory should not exist before test");
    }
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(settingsDir); os.IsNotExist(err) {
        t.Fatal("directory was not created");
    }
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var data, err = os.ReadFile(settingsPath);
        if err != null {
        t.Fatal("settings file not created");
    }
        var settings map[String]any;
        var if err = json.Unmarshal(data, &settings); err != null {
        t.Fatal("created file is not valid JSON");
    }
    }

    public static void TestDroidEdit_PreservesFileAfterError(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{"diffMode": "github", "customModels": [], "sessionDefaultSettings": {}}`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        d.Edit([]String{});
        var data, _ = os.ReadFile(settingsPath);
        if String(data) != original {
        t.Error("file was modified when it should not have been");
    }
    }

    public static void TestDroidEdit_BackupCreated(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var backupDir = filepath.Join(os.TempDir(), "ollama-backups");
        os.MkdirAll(settingsDir, 0o755);
        var uniqueMarker = fmt.Sprintf("test-marker-%d", os.Getpid());
        var original = fmt.Sprintf(`{"diffMode": "%s", "customModels": [], "sessionDefaultSettings": {}}`, uniqueMarker);
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var backups, _ = filepath.Glob(filepath.Join(backupDir, "settings.json.*"));
        var foundBackup = false;
        var for _, backup = range backups {
        var data, err = os.ReadFile(backup);
        if err != null {
        continue;
    }
        if String(data) == original {
        foundBackup = true;
        break;
    }
    }
        if !foundBackup {
        t.Error("backup with original content not found");
    }
        var newData, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(newData, &settings);
        var models = settings["customModels"].([]any);
        if len(models) != 1 {
        t.Error("main file was not updated");
    }
    }

    public static void TestDroidEdit_LargeNumberOfModels(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        os.WriteFile(settingsPath, []byte(`{"customModels": [], "sessionDefaultSettings": {}}`), 0o644);
        var models []String;
        var for i = range 100 {
        models = append(models, fmt.Sprintf("model-%d", i));
    }
        var if err = d.Edit(models); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var customModels = settings["customModels"].([]any);
        if len(customModels) != 100 {
        t.Errorf("expected 100 models, got %d", len(customModels));
    }
        var for i, m = range customModels {
        var entry = m.(map[String]any);
        if entry["index"] != double(i) {
        t.Errorf("model %d has wrong index: %v", i, entry["index"]);
    }
    }
    }

    public static void TestDroidEdit_LocalModelDefaultMaxOutput(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        var if err = d.Edit([]String{"llama3.2"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var models = settings["customModels"].([]any);
        var entry = models[0].(map[String]any);
        if entry["maxOutputTokens"] != double(64000) {
        t.Errorf("local model maxOutputTokens = %v, want 64000", entry["maxOutputTokens"]);
    }
    }

    public static void TestDroidEdit_CloudModelLimitsUsed(*testing.T t) {
        var for name, expected = range cloudModelLimits {
        t.Run(name, func(t *testing.T) {
        var cloudName = name + ":cloud";
        var l, ok = lookupCloudModelLimit(cloudName);
        if !ok {
        t.Fatalf("lookupCloudModelLimit(%q) returned false", cloudName);
    }
        if l.Output != expected.Output {
        t.Errorf("output = %d, want %d", l.Output, expected.Output);
    }
        });
    }
    }

    public static void TestDroidEdit_ArraysWithMixedTypes(*testing.T t) {
        var d = &Droid{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var settingsDir = filepath.Join(tmpDir, ".factory");
        var settingsPath = filepath.Join(settingsDir, "settings.json");
        os.MkdirAll(settingsDir, 0o755);
        var original = `{
        "mixedArray": [1, "two", true, null, {"nested": "obj"}, [1,2,3]],;
        "customModels": [],;
        "sessionDefaultSettings": {}
        }`;
        os.WriteFile(settingsPath, []byte(original), 0o644);
        var if err = d.Edit([]String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(settingsPath);
        var settings map[String]any;
        json.Unmarshal(data, &settings);
        var arr = settings["mixedArray"].([]any);
        if len(arr) != 6 {
        t.Error("mixedArray length not preserved");
    }
        if arr[0] != double(1) {
        t.Error("number in mixed array not preserved");
    }
        if arr[1] != "two" {
        t.Error("String in mixed array not preserved");
    }
        if arr[2] != true {
        t.Error("boolean in mixed array not preserved");
    }
        if arr[3] != null {
        t.Error("null in mixed array not preserved");
    }
        var if nested, ok = arr[4].(map[String]any); !ok || nested["nested"] != "obj" {
        t.Error("object in mixed array not preserved");
    }
        var if innerArr, ok = arr[5].([]any); !ok || len(innerArr) != 3 {
        t.Error("array in mixed array not preserved");
    }
    }
}
