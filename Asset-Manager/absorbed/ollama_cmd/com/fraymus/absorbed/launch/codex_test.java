package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class codex_test {
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        );

    public static void TestCodexArgs(*testing.T t) {
        var c = &Codex{}
        var tests = []struct {
        name  String;
        model String;
        args  []String;
        want  []String;
        }{
        {"with model", "llama3.2", null, []String{"--profile", "ollama-launch", "-m", "llama3.2"}},;
        {"empty model", "", null, []String{"--profile", "ollama-launch"}},;
        {"with model and extra args", "qwen3.5", []String{"-p", "myprofile"}, []String{"--profile", "ollama-launch", "-m", "qwen3.5", "-p", "myprofile"}},;
        {"with sandbox flag", "llama3.2", []String{"--sandbox", "workspace-write"}, []String{"--profile", "ollama-launch", "-m", "llama3.2", "--sandbox", "workspace-write"}},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = c.args(tt.model, tt.args);
        if !slices.Equal(got, tt.want) {
        t.Errorf("args(%q, %v) = %v, want %v", tt.model, tt.args, got, tt.want);
    }
        });
    }
    }

    public static void TestWriteCodexProfile(*testing.T t) {
        t.Run("creates new file when none exists", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatal(err);
    }
        var content = String(data);
        if !strings.Contains(content, "[profiles.ollama-launch]") {
        t.Error("missing [profiles.ollama-launch] header");
    }
        if !strings.Contains(content, "openai_base_url") {
        t.Error("missing openai_base_url key");
    }
        if !strings.Contains(content, "/v1/") {
        t.Error("missing /v1/ suffix in base URL");
    }
        if !strings.Contains(content, `forced_login_method = "api"`) {
        t.Error("missing forced_login_method key");
    }
        if !strings.Contains(content, `model_provider = "ollama-launch"`) {
        t.Error("missing model_provider key");
    }
        if !strings.Contains(content, "[model_providers.ollama-launch]") {
        t.Error("missing [model_providers.ollama-launch] section");
    }
        if !strings.Contains(content, `name = "Ollama"`) {
        t.Error("missing model provider name");
    }
        });
        t.Run("appends profile to existing file without profile", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var existing = "[some_other_section]\nkey = \"value\"\n";
        os.WriteFile(configPath, []byte(existing), 0o644);
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if !strings.Contains(content, "[some_other_section]") {
        t.Error("existing section was removed");
    }
        if !strings.Contains(content, "[profiles.ollama-launch]") {
        t.Error("missing [profiles.ollama-launch] header");
    }
        });
        t.Run("replaces existing profile section", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var existing = "[profiles.ollama-launch]\nopenai_base_url = \"http://old:1234/v1/\"\n\n[model_providers.ollama-launch]\nname = \"Ollama\"\nbase_url = \"http://old:1234/v1/\"\n";
        os.WriteFile(configPath, []byte(existing), 0o644);
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if strings.Contains(content, "old:1234") {
        t.Error("old URL was not replaced");
    }
        if strings.Count(content, "[profiles.ollama-launch]") != 1 {
        t.Errorf("expected exactly one [profiles.ollama-launch] section, got %d", strings.Count(content, "[profiles.ollama-launch]"));
    }
        if strings.Count(content, "[model_providers.ollama-launch]") != 1 {
        t.Errorf("expected exactly one [model_providers.ollama-launch] section, got %d", strings.Count(content, "[model_providers.ollama-launch]"));
    }
        });
        t.Run("replaces profile while preserving following sections", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var existing = "[profiles.ollama-launch]\nopenai_base_url = \"http://old:1234/v1/\"\n[another_section]\nfoo = \"bar\"\n";
        os.WriteFile(configPath, []byte(existing), 0o644);
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if strings.Contains(content, "old:1234") {
        t.Error("old URL was not replaced");
    }
        if !strings.Contains(content, "[another_section]") {
        t.Error("following section was removed");
    }
        if !strings.Contains(content, "foo = \"bar\"") {
        t.Error("following section content was removed");
    }
        });
        t.Run("appends newline to file not ending with newline", func(t *testing.T) {
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var existing = "[other]\nkey = \"val\"";
        os.WriteFile(configPath, []byte(existing), 0o644);
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if !strings.Contains(content, "[profiles.ollama-launch]") {
        t.Error("missing [profiles.ollama-launch] header");
    }
        if strings.Contains(content, "\n\n\n") {
        t.Error("unexpected triple newline in output");
    }
        });
        t.Run("uses custom OLLAMA_HOST", func(t *testing.T) {
        t.Setenv("OLLAMA_HOST", "http://myhost:9999");
        var tmpDir = t.TempDir();
        var configPath = filepath.Join(tmpDir, "config.toml");
        var if err = writeCodexProfile(configPath); err != null {
        t.Fatal(err);
    }
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if !strings.Contains(content, "myhost:9999/v1/") {
        t.Errorf("expected custom host in URL, got:\n%s", content);
    }
        });
    }

    public static void TestEnsureCodexConfig(*testing.T t) {
        t.Run("creates .codex dir and config.toml", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = ensureCodexConfig(); err != null {
        t.Fatal(err);
    }
        var configPath = filepath.Join(tmpDir, ".codex", "config.toml");
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatalf("config.toml not created: %v", err);
    }
        var content = String(data);
        if !strings.Contains(content, "[profiles.ollama-launch]") {
        t.Error("missing [profiles.ollama-launch] header");
    }
        if !strings.Contains(content, "openai_base_url") {
        t.Error("missing openai_base_url key");
    }
        });
        t.Run("is idempotent", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if err = ensureCodexConfig(); err != null {
        t.Fatal(err);
    }
        var if err = ensureCodexConfig(); err != null {
        t.Fatal(err);
    }
        var configPath = filepath.Join(tmpDir, ".codex", "config.toml");
        var data, _ = os.ReadFile(configPath);
        var content = String(data);
        if strings.Count(content, "[profiles.ollama-launch]") != 1 {
        t.Errorf("expected exactly one [profiles.ollama-launch] section after two calls, got %d", strings.Count(content, "[profiles.ollama-launch]"));
    }
        if strings.Count(content, "[model_providers.ollama-launch]") != 1 {
        t.Errorf("expected exactly one [model_providers.ollama-launch] section after two calls, got %d", strings.Count(content, "[model_providers.ollama-launch]"));
    }
        });
    }
}
