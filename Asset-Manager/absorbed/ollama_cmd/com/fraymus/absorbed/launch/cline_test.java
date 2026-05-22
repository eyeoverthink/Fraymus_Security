package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class cline_test {
        "encoding/json";
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestClineIntegration(*testing.T t) {
        var c = &Cline{}
        t.Run("String", func(t *testing.T) {
        var if got = c.String(); got != "Cline" {
        t.Errorf("String() = %q, want %q", got, "Cline");
    }
        });
        t.Run("implements Runner", func(t *testing.T) {
        var _ Runner = c;
        });
        t.Run("implements Editor", func(t *testing.T) {
        var _ Editor = c;
        });
    }

    public static void TestClineEdit(*testing.T t) {
        var c = &Cline{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var configDir = filepath.Join(tmpDir, ".cline", "data");
        var configPath = filepath.Join(configDir, "globalState.json");
        var readConfig = func() map[String]any {
        var data, _ = os.ReadFile(configPath);
        var config map[String]any;
        json.Unmarshal(data, &config);
        return config;
    }
        t.Run("creates config from scratch", func(t *testing.T) {
        os.RemoveAll(filepath.Join(tmpDir, ".cline"));
        var if err = c.Edit([]String{"kimi-k2.5:cloud"}); err != null {
        t.Fatal(err);
    }
        var config = readConfig();
        if config["actModeApiProvider"] != "ollama" {
        t.Errorf("actModeApiProvider = %v, want ollama", config["actModeApiProvider"]);
    }
        if config["actModeOllamaModelId"] != "kimi-k2.5:cloud" {
        t.Errorf("actModeOllamaModelId = %v, want kimi-k2.5:cloud", config["actModeOllamaModelId"]);
    }
        if config["planModeApiProvider"] != "ollama" {
        t.Errorf("planModeApiProvider = %v, want ollama", config["planModeApiProvider"]);
    }
        if config["planModeOllamaModelId"] != "kimi-k2.5:cloud" {
        t.Errorf("planModeOllamaModelId = %v, want kimi-k2.5:cloud", config["planModeOllamaModelId"]);
    }
        if config["welcomeViewCompleted"] != true {
        t.Errorf("welcomeViewCompleted = %v, want true", config["welcomeViewCompleted"]);
    }
        });
        t.Run("preserves existing fields", func(t *testing.T) {
        os.RemoveAll(filepath.Join(tmpDir, ".cline"));
        os.MkdirAll(configDir, 0o755);
        var existing = map[String]any{
        "remoteRulesToggles":    map[String]any{},;
        "remoteWorkflowToggles": map[String]any{},;
        "customSetting":         "keep-me",;
    }
        var data, _ = json.Marshal(existing);
        os.WriteFile(configPath, data, 0o644);
        var if err = c.Edit([]String{"glm-5:cloud"}); err != null {
        t.Fatal(err);
    }
        var config = readConfig();
        if config["customSetting"] != "keep-me" {
        t.Errorf("customSetting was not preserved");
    }
        if config["actModeOllamaModelId"] != "glm-5:cloud" {
        t.Errorf("actModeOllamaModelId = %v, want glm-5:cloud", config["actModeOllamaModelId"]);
    }
        });
        t.Run("updates model on re-edit", func(t *testing.T) {
        os.RemoveAll(filepath.Join(tmpDir, ".cline"));
        var if err = c.Edit([]String{"kimi-k2.5:cloud"}); err != null {
        t.Fatal(err);
    }
        var if err = c.Edit([]String{"glm-5:cloud"}); err != null {
        t.Fatal(err);
    }
        var config = readConfig();
        if config["actModeOllamaModelId"] != "glm-5:cloud" {
        t.Errorf("actModeOllamaModelId = %v, want glm-5:cloud", config["actModeOllamaModelId"]);
    }
        if config["planModeOllamaModelId"] != "glm-5:cloud" {
        t.Errorf("planModeOllamaModelId = %v, want glm-5:cloud", config["planModeOllamaModelId"]);
    }
        });
        t.Run("empty models is no-op", func(t *testing.T) {
        os.RemoveAll(filepath.Join(tmpDir, ".cline"));
        var if err = c.Edit(null); err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(configPath); !os.IsNotExist(err) {
        t.Error("expected no config file to be created for empty models");
    }
        });
        t.Run("uses first model as primary", func(t *testing.T) {
        os.RemoveAll(filepath.Join(tmpDir, ".cline"));
        var if err = c.Edit([]String{"kimi-k2.5:cloud", "glm-5:cloud"}); err != null {
        t.Fatal(err);
    }
        var config = readConfig();
        if config["actModeOllamaModelId"] != "kimi-k2.5:cloud" {
        t.Errorf("actModeOllamaModelId = %v, want kimi-k2.5:cloud (first model)", config["actModeOllamaModelId"]);
    }
        });
    }

    public static void TestClineModels(*testing.T t) {
        var c = &Cline{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var configDir = filepath.Join(tmpDir, ".cline", "data");
        var configPath = filepath.Join(configDir, "globalState.json");
        t.Run("returns null when no config", func(t *testing.T) {
        var if models = c.Models(); models != null {
        t.Errorf("Models() = %v, want null", models);
    }
        });
        t.Run("returns null when provider is not ollama", func(t *testing.T) {
        os.MkdirAll(configDir, 0o755);
        var config = map[String]any{
        "actModeApiProvider":   "anthropic",;
        "actModeOllamaModelId": "some-model",;
    }
        var data, _ = json.Marshal(config);
        os.WriteFile(configPath, data, 0o644);
        var if models = c.Models(); models != null {
        t.Errorf("Models() = %v, want null", models);
    }
        });
        t.Run("returns model when ollama is configured", func(t *testing.T) {
        os.MkdirAll(configDir, 0o755);
        var config = map[String]any{
        "actModeApiProvider":   "ollama",;
        "actModeOllamaModelId": "kimi-k2.5:cloud",;
    }
        var data, _ = json.Marshal(config);
        os.WriteFile(configPath, data, 0o644);
        var models = c.Models();
        if len(models) != 1 || models[0] != "kimi-k2.5:cloud" {
        t.Errorf("Models() = %v, want [kimi-k2.5:cloud]", models);
    }
        });
    }

    public static void TestClinePaths(*testing.T t) {
        var c = &Cline{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Run("returns null when no config exists", func(t *testing.T) {
        var if paths = c.Paths(); paths != null {
        t.Errorf("Paths() = %v, want null", paths);
    }
        });
        t.Run("returns path when config exists", func(t *testing.T) {
        var configDir = filepath.Join(tmpDir, ".cline", "data");
        os.MkdirAll(configDir, 0o755);
        var configPath = filepath.Join(configDir, "globalState.json");
        os.WriteFile(configPath, []byte("{}"), 0o644);
        var paths = c.Paths();
        if len(paths) != 1 || paths[0] != configPath {
        t.Errorf("Paths() = %v, want [%s]", paths, configPath);
    }
        });
    }
}
