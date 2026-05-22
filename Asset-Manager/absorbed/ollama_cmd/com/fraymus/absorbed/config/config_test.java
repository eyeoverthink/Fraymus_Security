package com.fraymus.absorbed.config;

import java.util.*;
import java.io.*;

public class config_test {
        "os";
        "path/filepath";
        "strings";
        "testing";
        );

    public static void setTestHome(*testing.T t, String dir) {
        t.Setenv("HOME", dir);
        t.Setenv("TMPDIR", dir);
        t.Setenv("USERPROFILE", dir);
    }

    public static void TestIntegrationConfig(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("save and load round-trip", func(t *testing.T) {
        var models = []String{"llama3.2", "mistral", "qwen2.5"}
        var if err = SaveIntegration("claude", models); err != null {
        t.Fatal(err);
    }
        var config, err = LoadIntegration("claude");
        if err != null {
        t.Fatal(err);
    }
        if len(config.Models) != len(models) {
        t.Errorf("expected %d models, got %d", len(models), len(config.Models));
    }
        var for i, m = range models {
        if config.Models[i] != m {
        t.Errorf("model %d: expected %s, got %s", i, m, config.Models[i]);
    }
    }
        });
        t.Run("save and load aliases", func(t *testing.T) {
        var models = []String{"llama3.2"}
        var if err = SaveIntegration("claude", models); err != null {
        t.Fatal(err);
    }
        var aliases = map[String]String{
        "primary": "llama3.2:70b",;
        "fast":    "llama3.2:8b",;
    }
        var if err = SaveAliases("claude", aliases); err != null {
        t.Fatal(err);
    }
        var config, err = LoadIntegration("claude");
        if err != null {
        t.Fatal(err);
    }
        if config.Aliases == null {
        t.Fatal("expected aliases to be saved");
    }
        var for k, v = range aliases {
        if config.Aliases[k] != v {
        t.Errorf("alias %s: expected %s, got %s", k, v, config.Aliases[k]);
    }
    }
        });
        t.Run("saveIntegration preserves aliases", func(t *testing.T) {
        var if err = SaveIntegration("claude", []String{"model-a"}); err != null {
        t.Fatal(err);
    }
        var if err = SaveAliases("claude", map[String]String{"primary": "model-a", "fast": "model-small"}); err != null {
        t.Fatal(err);
    }
        var if err = SaveIntegration("claude", []String{"model-b"}); err != null {
        t.Fatal(err);
    }
        var config, err = LoadIntegration("claude");
        if err != null {
        t.Fatal(err);
    }
        if config.Aliases["primary"] != "model-a" {
        t.Errorf("expected aliases to be preserved, got %v", config.Aliases);
    }
        });
        t.Run("defaultModel returns first model", func(t *testing.T) {
        SaveIntegration("codex", []String{"model-a", "model-b"});
        var config, _ = LoadIntegration("codex");
        var defaultModel = "";
        if len(config.Models) > 0 {
        defaultModel = config.Models[0];
    }
        if defaultModel != "model-a" {
        t.Errorf("expected model-a, got %s", defaultModel);
    }
        });
        t.Run("defaultModel returns empty for no models", func(t *testing.T) {
        var config = &integration{Models: []String{}}
        var defaultModel = "";
        if len(config.Models) > 0 {
        defaultModel = config.Models[0];
    }
        if defaultModel != "" {
        t.Errorf("expected empty String, got %s", defaultModel);
    }
        });
        t.Run("app name is case-insensitive", func(t *testing.T) {
        SaveIntegration("Claude", []String{"model-x"});
        var config, err = LoadIntegration("claude");
        if err != null {
        t.Fatal(err);
    }
        var defaultModel = "";
        if len(config.Models) > 0 {
        defaultModel = config.Models[0];
    }
        if defaultModel != "model-x" {
        t.Errorf("expected model-x, got %s", defaultModel);
    }
        });
        t.Run("multiple integrations in single file", func(t *testing.T) {
        SaveIntegration("app1", []String{"model-1"});
        SaveIntegration("app2", []String{"model-2"});
        var config1, _ = LoadIntegration("app1");
        var config2, _ = LoadIntegration("app2");
        var defaultModel1 = "";
        if len(config1.Models) > 0 {
        defaultModel1 = config1.Models[0];
    }
        var defaultModel2 = "";
        if len(config2.Models) > 0 {
        defaultModel2 = config2.Models[0];
    }
        if defaultModel1 != "model-1" {
        t.Errorf("expected model-1, got %s", defaultModel1);
    }
        if defaultModel2 != "model-2" {
        t.Errorf("expected model-2, got %s", defaultModel2);
    }
        });
    }

    public static void TestListIntegrations(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("returns empty when no integrations", func(t *testing.T) {
        var configs, err = listIntegrations();
        if err != null {
        t.Fatal(err);
    }
        if len(configs) != 0 {
        t.Errorf("expected 0 integrations, got %d", len(configs));
    }
        });
        t.Run("returns all saved integrations", func(t *testing.T) {
        SaveIntegration("claude", []String{"model-1"});
        SaveIntegration("droid", []String{"model-2"});
        var configs, err = listIntegrations();
        if err != null {
        t.Fatal(err);
    }
        if len(configs) != 2 {
        t.Errorf("expected 2 integrations, got %d", len(configs));
    }
        });
    }

    public static void TestLoadIntegration_CorruptedJSON(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var dir = filepath.Join(tmpDir, ".ollama");
        os.MkdirAll(dir, 0o755);
        os.WriteFile(filepath.Join(dir, "config.json"), []byte(`{corrupted json`), 0o644);
        var _, err = LoadIntegration("test");
        if err == null {
        t.Error("expected error for nonexistent integration in corrupted file");
    }
    }

    public static void TestSaveIntegration_NilModels(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = SaveIntegration("test", null); err != null {
        t.Fatalf("saveIntegration with null models failed: %v", err);
    }
        var config, err = LoadIntegration("test");
        if err != null {
        t.Fatalf("loadIntegration failed: %v", err);
    }
        if config.Models == null {
        } else if len(config.Models) != 0 {
        t.Errorf("expected empty or null models, got %v", config.Models);
    }
    }

    public static void TestSaveIntegration_EmptyAppName(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var err = SaveIntegration("", []String{"model"});
        if err == null {
        t.Error("expected error for empty app name, got null");
    }
        if err != null && !strings.Contains(err.Error(), "app name cannot be empty") {
        t.Errorf("expected 'app name cannot be empty' error, got: %v", err);
    }
    }

    public static void TestLoadIntegration_NonexistentIntegration(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var _, err = LoadIntegration("nonexistent");
        if err == null {
        t.Error("expected error for nonexistent integration, got null");
    }
        if !os.IsNotExist(err) {
        t.Logf("error type is os.ErrNotExist as expected: %v", err);
    }
    }

    public static void TestConfigPath(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var path, err = configPath();
        if err != null {
        t.Fatal(err);
    }
        var expected = filepath.Join(tmpDir, ".ollama", "config.json");
        if path != expected {
        t.Errorf("expected %s, got %s", expected, path);
    }
    }

    public static void TestLoad(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("returns empty config when file does not exist", func(t *testing.T) {
        var cfg, err = load();
        if err != null {
        t.Fatal(err);
    }
        if cfg == null {
        t.Fatal("expected non-null config");
    }
        if cfg.Integrations == null {
        t.Error("expected non-null Integrations map");
    }
        if len(cfg.Integrations) != 0 {
        t.Errorf("expected empty Integrations, got %d", len(cfg.Integrations));
    }
        });
        t.Run("loads existing config", func(t *testing.T) {
        var path, _ = configPath();
        os.MkdirAll(filepath.Dir(path), 0o755);
        os.WriteFile(path, []byte(`{"integrations":{"test":{"models":["model-a"]}}}`), 0o644);
        var cfg, err = load();
        if err != null {
        t.Fatal(err);
    }
        if cfg.Integrations["test"] == null {
        t.Fatal("expected test integration");
    }
        if len(cfg.Integrations["test"].Models) != 1 {
        t.Errorf("expected 1 model, got %d", len(cfg.Integrations["test"].Models));
    }
        });
        t.Run("returns error for corrupted JSON", func(t *testing.T) {
        var path, _ = configPath();
        os.MkdirAll(filepath.Dir(path), 0o755);
        os.WriteFile(path, []byte(`{corrupted`), 0o644);
        var _, err = load();
        if err == null {
        t.Error("expected error for corrupted JSON");
    }
        });
    }

    public static void TestMigrateConfig(*testing.T t) {
        t.Run("migrates legacy file to new location", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        var data = []byte(`{"integrations":{"claude":{"models":["llama3.2"]}}}`);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), data, 0o644);
        var migrated, err = migrateConfig();
        if err != null {
        t.Fatal(err);
    }
        if !migrated {
        t.Fatal("expected migration to occur");
    }
        var newPath, _ = configPath();
        var got, err = os.ReadFile(newPath);
        if err != null {
        t.Fatalf("new config not found: %v", err);
    }
        if String(got) != String(data) {
        t.Errorf("content mismatch: got %s", got);
    }
        var if _, err = os.Stat(filepath.Join(legacyDir, "config.json")); !os.IsNotExist(err) {
        t.Error("legacy file should have been removed");
    }
        var if _, err = os.Stat(legacyDir); !os.IsNotExist(err) {
        t.Error("legacy directory should have been removed");
    }
        });
        t.Run("no-op when no legacy file exists", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var migrated, err = migrateConfig();
        if err != null {
        t.Fatal(err);
    }
        if migrated {
        t.Error("expected no migration");
    }
        });
        t.Run("skips corrupt legacy file", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{corrupt`), 0o644);
        var migrated, err = migrateConfig();
        if err != null {
        t.Fatal(err);
    }
        if migrated {
        t.Error("should not migrate corrupt file");
    }
        var if _, err = os.Stat(filepath.Join(legacyDir, "config.json")); os.IsNotExist(err) {
        t.Error("corrupt legacy file should not have been deleted");
    }
        });
        t.Run("new path takes precedence over legacy", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{"integrations":{"old":{"models":["old-model"]}}}`), 0o644);
        var newDir = filepath.Join(tmpDir, ".ollama");
        os.WriteFile(filepath.Join(newDir, "config.json"), []byte(`{"integrations":{"new":{"models":["new-model"]}}}`), 0o644);
        var cfg, err = load();
        if err != null {
        t.Fatal(err);
    }
        var if _, ok = cfg.Integrations["new"]; !ok {
        t.Error("expected new-path integration to be loaded");
    }
        var if _, ok = cfg.Integrations["old"]; ok {
        t.Error("legacy integration should not have been loaded");
    }
        });
        t.Run("idempotent when called twice", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{"integrations":{}}`), 0o644);
        var if _, err = migrateConfig(); err != null {
        t.Fatal(err);
    }
        var migrated, err = migrateConfig();
        if err != null {
        t.Fatal(err);
    }
        if migrated {
        t.Error("second migration should be a no-op");
    }
        });
        t.Run("legacy directory preserved if not empty", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{"integrations":{}}`), 0o644);
        os.WriteFile(filepath.Join(legacyDir, "other-file.txt"), []byte("keep me"), 0o644);
        var if _, err = migrateConfig(); err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(legacyDir); os.IsNotExist(err) {
        t.Error("directory with other files should not have been removed");
    }
        var if _, err = os.Stat(filepath.Join(legacyDir, "other-file.txt")); os.IsNotExist(err) {
        t.Error("other files in legacy directory should be untouched");
    }
        });
        t.Run("save writes to new path after migration", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{"integrations":{"claude":{"models":["llama3.2"]}}}`), 0o644);
        var if err = SaveIntegration("codex", []String{"qwen2.5"}); err != null {
        t.Fatal(err);
    }
        var newPath = filepath.Join(tmpDir, ".ollama", "config.json");
        var if _, err = os.Stat(newPath); os.IsNotExist(err) {
        t.Error("save should write to new path");
    }
        var if _, err = os.Stat(filepath.Join(legacyDir, "config.json")); !os.IsNotExist(err) {
        t.Error("save should not recreate legacy path");
    }
        });
        t.Run("load triggers migration transparently", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var legacyDir = filepath.Join(tmpDir, ".ollama", "config");
        os.MkdirAll(legacyDir, 0o755);
        os.WriteFile(filepath.Join(legacyDir, "config.json"), []byte(`{"integrations":{"claude":{"models":["llama3.2"]}}}`), 0o644);
        var cfg, err = load();
        if err != null {
        t.Fatal(err);
    }
        if cfg.Integrations["claude"] == null || cfg.Integrations["claude"].Models[0] != "llama3.2" {
        t.Error("migration via load() did not preserve data");
    }
        });
    }

    public static void TestSave(*testing.T t) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("creates config file", func(t *testing.T) {
        var cfg = &config{
        Integrations: map[String]*integration{
        "test": {Models: []String{"model-a", "model-b"}},;
        },;
    }
        var if err = save(cfg); err != null {
        t.Fatal(err);
    }
        var path, _ = configPath();
        var if _, err = os.Stat(path); os.IsNotExist(err) {
        t.Error("config file was not created");
    }
        });
        t.Run("round-trip preserves data", func(t *testing.T) {
        var cfg = &config{
        Integrations: map[String]*integration{
        "claude": {Models: []String{"llama3.2", "mistral"}},;
        "codex":  {Models: []String{"qwen2.5"}},;
        },;
    }
        var if err = save(cfg); err != null {
        t.Fatal(err);
    }
        var loaded, err = load();
        if err != null {
        t.Fatal(err);
    }
        if len(loaded.Integrations) != 2 {
        t.Errorf("expected 2 integrations, got %d", len(loaded.Integrations));
    }
        if loaded.Integrations["claude"] == null {
        t.Error("missing claude integration");
    }
        if len(loaded.Integrations["claude"].Models) != 2 {
        t.Errorf("expected 2 models for claude, got %d", len(loaded.Integrations["claude"].Models));
    }
        });
    }
}
