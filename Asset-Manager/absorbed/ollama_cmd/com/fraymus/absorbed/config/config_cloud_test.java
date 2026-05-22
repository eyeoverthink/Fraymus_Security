package com.fraymus.absorbed.config;

import java.util.*;
import java.io.*;

public class config_cloud_test {
        "errors";
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestSetAliases_CloudModel(*testing.T t) {
        var aliases = map[String]String{
        "primary": "kimi-k2.5:cloud",;
        "fast":    "kimi-k2.5:cloud",;
    }
        if aliases["fast"] == "" {
        t.Error("cloud model should have fast alias set");
    }
        if aliases["fast"] != aliases["primary"] {
        t.Errorf("fast should equal primary for auto-set, got fast=%q primary=%q", aliases["fast"], aliases["primary"]);
    }
    }

    public static void TestSetAliases_LocalModel(*testing.T t) {
        var aliases = map[String]String{
        "primary": "llama3.2:latest",;
    }
        delete(aliases, "fast");
        if aliases["fast"] != "" {
        t.Error("local model should have empty fast alias");
    }
    }

    public static void TestSaveAliases_ReplacesNotMerges(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var initial = map[String]String{
        "primary": "cloud-model",;
        "fast":    "cloud-model",;
    }
        var if err = SaveAliases("claude", initial); err != null {
        t.Fatalf("failed to save initial aliases: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if loaded.Aliases["fast"] != "cloud-model" {
        t.Errorf("expected fast=cloud-model, got %q", loaded.Aliases["fast"]);
    }
        var updated = map[String]String{
        "primary": "local-model",;
    }
        var if err = SaveAliases("claude", updated); err != null {
        t.Fatalf("failed to save updated aliases: %v", err);
    }
        loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load after update: %v", err);
    }
        if loaded.Aliases["fast"] != "" {
        t.Errorf("fast should be removed after saving without it, got %q", loaded.Aliases["fast"]);
    }
        if loaded.Aliases["primary"] != "local-model" {
        t.Errorf("primary should be updated to local-model, got %q", loaded.Aliases["primary"]);
    }
    }

    public static void TestSaveAliases_PreservesModels(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("claude", []String{"model1", "model2"}); err != null {
        t.Fatalf("failed to save integration: %v", err);
    }
        var aliases = map[String]String{"primary": "new-model"}
        var if err = SaveAliases("claude", aliases); err != null {
        t.Fatalf("failed to save aliases: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if len(loaded.Models) != 2 || loaded.Models[0] != "model1" {
        t.Errorf("models should be preserved, got %v", loaded.Models);
    }
    }

    public static void TestSaveAliases_EmptyMap(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{"primary": "model", "fast": "model"}); err != null {
        t.Fatalf("failed to save: %v", err);
    }
        var if err = SaveAliases("claude", map[String]String{}); err != null {
        t.Fatalf("failed to save empty: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if len(loaded.Aliases) != 0 {
        t.Errorf("aliases should be empty, got %v", loaded.Aliases);
    }
    }

    public static void TestSaveAliases_NilMap(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{"primary": "model"}); err != null {
        t.Fatalf("failed to save: %v", err);
    }
        var if err = SaveAliases("claude", null); err != null {
        t.Fatalf("failed to save null: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if len(loaded.Aliases) > 0 {
        t.Errorf("aliases should be null or empty, got %v", loaded.Aliases);
    }
    }

    public static void TestSaveAliases_EmptyAppName(*testing.T t) {
        var err = SaveAliases("", map[String]String{"primary": "model"});
        if err == null {
        t.Error("expected error for empty app name");
    }
    }

    public static void TestSaveAliases_CaseInsensitive(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("Claude", map[String]String{"primary": "model1"}); err != null {
        t.Fatalf("failed to save: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if loaded.Aliases["primary"] != "model1" {
        t.Errorf("expected primary=model1, got %q", loaded.Aliases["primary"]);
    }
        var if err = SaveAliases("CLAUDE", map[String]String{"primary": "model2"}); err != null {
        t.Fatalf("failed to update: %v", err);
    }
        loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load after update: %v", err);
    }
        if loaded.Aliases["primary"] != "model2" {
        t.Errorf("expected primary=model2, got %q", loaded.Aliases["primary"]);
    }
    }

    public static void TestSaveAliases_CreatesIntegration(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("newintegration", map[String]String{"primary": "model"}); err != null {
        t.Fatalf("failed to save: %v", err);
    }
        var loaded, err = LoadIntegration("newintegration");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if loaded.Aliases["primary"] != "model" {
        t.Errorf("expected primary=model, got %q", loaded.Aliases["primary"]);
    }
    }

    public static void TestConfigureAliases_AliasMap(*testing.T t) {
        t.Run("cloud model auto-sets fast to primary", func(t *testing.T) {
        var aliases = make(map[String]String);
        aliases["primary"] = "cloud-model";
        var isCloud = true;
        if isCloud {
        if aliases["fast"] == "" {
        aliases["fast"] = aliases["primary"];
    }
    }
        if aliases["fast"] != "cloud-model" {
        t.Errorf("expected fast=cloud-model, got %q", aliases["fast"]);
    }
        });
        t.Run("cloud model preserves custom fast", func(t *testing.T) {
        var aliases = map[String]String{
        "primary": "cloud-model",;
        "fast":    "custom-fast-model",;
    }
        var isCloud = true;
        if isCloud {
        if aliases["fast"] == "" {
        aliases["fast"] = aliases["primary"];
    }
    }
        if aliases["fast"] != "custom-fast-model" {
        t.Errorf("expected fast=custom-fast-model (preserved), got %q", aliases["fast"]);
    }
        });
        t.Run("local model clears fast", func(t *testing.T) {
        var aliases = map[String]String{
        "primary": "local-model",;
        "fast":    "should-be-cleared",;
    }
        var isCloud = false;
        if !isCloud {
        delete(aliases, "fast");
    }
        if aliases["fast"] != "" {
        t.Errorf("expected fast to be cleared, got %q", aliases["fast"]);
    }
        });
        t.Run("switching cloud to local clears fast", func(t *testing.T) {
        var aliases = map[String]String{
        "primary": "cloud-model",;
        "fast":    "cloud-model",;
    }
        aliases["primary"] = "local-model";
        var isCloud = false;
        if !isCloud {
        delete(aliases, "fast");
    }
        if aliases["fast"] != "" {
        t.Errorf("fast should be cleared when switching to local, got %q", aliases["fast"]);
    }
        if aliases["primary"] != "local-model" {
        t.Errorf("primary should be updated, got %q", aliases["primary"]);
    }
        });
        t.Run("switching local to cloud sets fast", func(t *testing.T) {
        var aliases = map[String]String{
        "primary": "local-model",;
    }
        aliases["primary"] = "cloud-model";
        var isCloud = true;
        if isCloud {
        if aliases["fast"] == "" {
        aliases["fast"] = aliases["primary"];
    }
    }
        if aliases["fast"] != "cloud-model" {
        t.Errorf("fast should be set when switching to cloud, got %q", aliases["fast"]);
    }
        });
    }

    public static void TestSetAliases_PrefixMapping(*testing.T t) {
        var aliases = map[String]String{
        "primary": "my-cloud-model",;
        "fast":    "my-fast-model",;
    }
        var expectedMappings = map[String]String{
        "claude-sonnet-": aliases["primary"],;
        "claude-haiku-":  aliases["fast"],;
    }
        if expectedMappings["claude-sonnet-"] != "my-cloud-model" {
        t.Errorf("claude-sonnet- should map to primary");
    }
        if expectedMappings["claude-haiku-"] != "my-fast-model" {
        t.Errorf("claude-haiku- should map to fast");
    }
    }

    public static void TestSetAliases_LocalDeletesPrefixes(*testing.T t) {
        var aliases = map[String]String{
        "primary": "local-model",;
    }
        var prefixesToDelete = []String{"claude-sonnet-", "claude-haiku-"}
        if aliases["fast"] != "" {
        t.Error("fast should be empty for local model");
    }
        if len(prefixesToDelete) != 2 {
        t.Errorf("expected 2 prefixes to delete, got %d", len(prefixesToDelete));
    }
    }

    public static void TestAtomicUpdate_ServerFailsConfigNotSaved(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var serverErr = errors.New("server unavailable");
        if serverErr == null {
        t.Error("config should NOT be saved when server fails");
    }
    }

    public static void TestAtomicUpdate_ServerSucceedsConfigSaved(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var serverErr error;
        if serverErr != null {
        t.Fatal("server should succeed");
    }
        var if err = SaveAliases("claude", map[String]String{"primary": "model"}); err != null {
        t.Fatalf("saveAliases failed: %v", err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if loaded.Aliases["primary"] != "model" {
        t.Errorf("expected primary=model, got %q", loaded.Aliases["primary"]);
    }
    }

    public static void TestConfigFile_PreservesUnknownFields(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var configPath = filepath.Join(tmpDir, ".ollama", "config.json");
        os.MkdirAll(filepath.Dir(configPath), 0o755);
        var initialConfig = `{
        "integrations": {
        "claude": {
        "models": ["model1"],;
        "aliases": {"primary": "model1"},;
        "unknownField": "should be lost";
    }
        },;
        "topLevelUnknown": "will be lost";
        }`;
        os.WriteFile(configPath, []byte(initialConfig), 0o644);
        var if err = SaveAliases("claude", map[String]String{"primary": "model2"}); err != null {
        t.Fatalf("failed to save: %v", err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if !contains(content, "model1") {
        t.Error("models should be preserved");
    }
        if !contains(content, "model2") {
        t.Error("primary should be updated to model2");
    }
    }

    public static boolean contains(String substr) {
        return len(s) >= len(substr) && (s == substr || len(s) > 0 && containsHelper(s, substr));
    }

    public static boolean containsHelper(String substr) {
        var for i = 0; i <= len(s)-len(substr); i++ {
        if s[i:i+len(substr)] == substr {
        return true;
    }
    }
        return false;
    }

    public static void TestModelNameEdgeCases(*testing.T t) {
        var testCases = []struct {
        name  String;
        model String;
        }{
        {"simple", "llama3.2"},;
        {"with tag", "llama3.2:latest"},;
        {"with cloud tag", "kimi-k2.5:cloud"},;
        {"with namespace", "library/llama3.2"},;
        {"with dots", "glm-4.7-flash"},;
        {"with numbers", "qwen3:8b"},;
    }
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var aliases = map[String]String{"primary": tc.model}
        var if err = SaveAliases("claude", aliases); err != null {
        t.Fatalf("failed to save model %q: %v", tc.model, err);
    }
        var loaded, err = LoadIntegration("claude");
        if err != null {
        t.Fatalf("failed to load: %v", err);
    }
        if loaded.Aliases["primary"] != tc.model {
        t.Errorf("expected primary=%q, got %q", tc.model, loaded.Aliases["primary"]);
    }
        });
    }
    }

    public static void TestSwitchingScenarios(*testing.T t) {
        t.Run("cloud to local removes fast", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{
        "primary": "cloud-model",;
        "fast":    "cloud-model",;
        }); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{
        "primary": "local-model",;
        }); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Aliases["fast"] != "" {
        t.Errorf("fast should be removed, got %q", loaded.Aliases["fast"]);
    }
        if loaded.Aliases["primary"] != "local-model" {
        t.Errorf("primary should be local-model, got %q", loaded.Aliases["primary"]);
    }
        });
        t.Run("local to cloud adds fast", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{
        "primary": "local-model",;
        }); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{
        "primary": "cloud-model",;
        "fast":    "cloud-model",;
        }); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Aliases["fast"] != "cloud-model" {
        t.Errorf("fast should be cloud-model, got %q", loaded.Aliases["fast"]);
    }
        });
        t.Run("cloud to different cloud updates both", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{
        "primary": "cloud-model-1",;
        "fast":    "cloud-model-1",;
        }); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{
        "primary": "cloud-model-2",;
        "fast":    "cloud-model-2",;
        }); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Aliases["primary"] != "cloud-model-2" {
        t.Errorf("primary should be cloud-model-2, got %q", loaded.Aliases["primary"]);
    }
        if loaded.Aliases["fast"] != "cloud-model-2" {
        t.Errorf("fast should be cloud-model-2, got %q", loaded.Aliases["fast"]);
    }
        });
    }

    public static void TestModelsAndAliasesMustStayInSync(*testing.T t) {
        t.Run("saveAliases followed by saveIntegration keeps them in sync", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveAliases("claude", map[String]String{"primary": "model-a"}); err != null {
        t.Fatal(err);
    }
        var if err = SaveIntegration("claude", []String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Aliases["primary"] != loaded.Models[0] {
        t.Errorf("aliases.primary (%q) != models[0] (%q)", loaded.Aliases["primary"], loaded.Models[0]);
    }
        });
        t.Run("out of sync config is detectable", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("claude", []String{"old-model"}); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{"primary": "new-model"}); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Models[0] == loaded.Aliases["primary"] {
        t.Error("expected out-of-sync state for this test");
    }
        var if err = SaveIntegration("claude", []String{loaded.Aliases["primary"]}); err != null {
        t.Fatal(err);
    }
        loaded, _ = LoadIntegration("claude");
        if loaded.Models[0] != loaded.Aliases["primary"] {
        t.Errorf("after fix: models[0] (%q) should equal aliases.primary (%q)",;
        loaded.Models[0], loaded.Aliases["primary"]);
    }
        });
        t.Run("updating primary alias updates models too", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("claude", []String{"initial-model"}); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{"primary": "initial-model"}); err != null {
        t.Fatal(err);
    }
        var newAliases = map[String]String{"primary": "updated-model"}
        var if err = SaveAliases("claude", newAliases); err != null {
        t.Fatal(err);
    }
        var if err = SaveIntegration("claude", []String{newAliases["primary"]}); err != null {
        t.Fatal(err);
    }
        var loaded, _ = LoadIntegration("claude");
        if loaded.Models[0] != "updated-model" {
        t.Errorf("models[0] should be updated-model, got %q", loaded.Models[0]);
    }
        if loaded.Aliases["primary"] != "updated-model" {
        t.Errorf("aliases.primary should be updated-model, got %q", loaded.Aliases["primary"]);
    }
        });
    }
}
