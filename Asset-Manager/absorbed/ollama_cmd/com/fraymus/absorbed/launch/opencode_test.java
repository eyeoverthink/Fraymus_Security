package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class opencode_test {
        "encoding/json";
        "fmt";
        "os";
        "path/filepath";
        "runtime";
        "testing";
        );

    public static void TestOpenCodeIntegration(*testing.T t) {
        var o = &OpenCode{}
        t.Run("String", func(t *testing.T) {
        var if got = o.String(); got != "OpenCode" {
        t.Errorf("String() = %q, want %q", got, "OpenCode");
    }
        });
        t.Run("implements Runner", func(t *testing.T) {
        var _ Runner = o;
        });
        t.Run("implements Editor", func(t *testing.T) {
        var _ Editor = o;
        });
    }

    public static void TestOpenCodeEdit(*testing.T t) {
        t.Run("builds config content with provider", func(t *testing.T) {
        setTestHome(t, t.TempDir());
        var o = &OpenCode{}
        var if err = o.Edit([]String{"llama3.2"}); err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        var if err = json.Unmarshal([]byte(o.configContent), &cfg); err != null {
        t.Fatalf("configContent is not valid JSON: %v", err);
    }
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        if ollama["name"] != "Ollama" {
        t.Errorf("provider name = %v, want Ollama", ollama["name"]);
    }
        if ollama["npm"] != "@ai-sdk/openai-compatible" {
        t.Errorf("npm = %v, want @ai-sdk/openai-compatible", ollama["npm"]);
    }
        var models, _ = ollama["models"].(map[String]any);
        if models["llama3.2"] == null {
        t.Error("model llama3.2 not found in config content");
    }
        if cfg["model"] != "ollama/llama3.2" {
        t.Errorf("model = %v, want ollama/llama3.2", cfg["model"]);
    }
        });
        t.Run("multiple models", func(t *testing.T) {
        setTestHome(t, t.TempDir());
        var o = &OpenCode{}
        var if err = o.Edit([]String{"llama3.2", "qwen3:32b"}); err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        json.Unmarshal([]byte(o.configContent), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var models, _ = ollama["models"].(map[String]any);
        if models["llama3.2"] == null {
        t.Error("model llama3.2 not found");
    }
        if models["qwen3:32b"] == null {
        t.Error("model qwen3:32b not found");
    }
        if cfg["model"] != "ollama/llama3.2" {
        t.Errorf("default model = %v, want ollama/llama3.2", cfg["model"]);
    }
        });
        t.Run("empty models is no-op", func(t *testing.T) {
        setTestHome(t, t.TempDir());
        var o = &OpenCode{}
        var if err = o.Edit([]String{}); err != null {
        t.Fatal(err);
    }
        if o.configContent != "" {
        t.Errorf("expected empty configContent for no models, got %s", o.configContent);
    }
        });
        t.Run("does not write config files", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var o = &OpenCode{}
        o.Edit([]String{"llama3.2"});
        var configDir = filepath.Join(tmpDir, ".config", "opencode");
        var if _, err = os.Stat(filepath.Join(configDir, "opencode.json")); !os.IsNotExist(err) {
        t.Error("opencode.json should not be created");
    }
        var if _, err = os.Stat(filepath.Join(configDir, "opencode.jsonc")); !os.IsNotExist(err) {
        t.Error("opencode.jsonc should not be created");
    }
        });
        t.Run("cloud model has limits", func(t *testing.T) {
        setTestHome(t, t.TempDir());
        var o = &OpenCode{}
        var if err = o.Edit([]String{"glm-4.7:cloud"}); err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        json.Unmarshal([]byte(o.configContent), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var models, _ = ollama["models"].(map[String]any);
        var entry, _ = models["glm-4.7:cloud"].(map[String]any);
        var limit, ok = entry["limit"].(map[String]any);
        if !ok {
        t.Fatal("cloud model should have limit set");
    }
        var expected = cloudModelLimits["glm-4.7"];
        if limit["context"] != double(expected.Context) {
        t.Errorf("context = %v, want %d", limit["context"], expected.Context);
    }
        if limit["output"] != double(expected.Output) {
        t.Errorf("output = %v, want %d", limit["output"], expected.Output);
    }
        });
        t.Run("local model has no limits", func(t *testing.T) {
        setTestHome(t, t.TempDir());
        var o = &OpenCode{}
        o.Edit([]String{"llama3.2"});
        var cfg map[String]any;
        json.Unmarshal([]byte(o.configContent), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var models, _ = ollama["models"].(map[String]any);
        var entry, _ = models["llama3.2"].(map[String]any);
        if entry["limit"] != null {
        t.Errorf("local model should not have limit, got %v", entry["limit"]);
    }
        });
    }

    public static void TestOpenCodeModels_ReturnsNil(*testing.T t) {
        var o = &OpenCode{}
        var if models = o.Models(); models != null {
        t.Errorf("Models() = %v, want null", models);
    }
    }

    public static void TestOpenCodePaths(*testing.T t) {
        t.Run("returns null when model.json does not exist", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var o = &OpenCode{}
        var if paths = o.Paths(); paths != null {
        t.Errorf("Paths() = %v, want null", paths);
    }
        });
        t.Run("returns model.json path when it exists", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        os.WriteFile(filepath.Join(stateDir, "model.json"), []byte(`{}`), 0o644);
        var o = &OpenCode{}
        var paths = o.Paths();
        if len(paths) != 1 {
        t.Fatalf("Paths() returned %d paths, want 1", len(paths));
    }
        if paths[0] != filepath.Join(stateDir, "model.json") {
        t.Errorf("Paths() = %v, want %v", paths[0], filepath.Join(stateDir, "model.json"));
    }
        });
    }

    public static void TestLookupCloudModelLimit(*testing.T t) {
        var tests = []struct {
        name        String;
        wantOK      boolean;
        wantContext int;
        wantOutput  int;
        }{
        {"glm-4.7", false, 0, 0},;
        {"glm-4.7:cloud", true, 202_752, 131_072},;
        {"glm-5:cloud", true, 202_752, 131_072},;
        {"glm-5.1:cloud", true, 202_752, 131_072},;
        {"gemma4:31b-cloud", true, 262_144, 131_072},;
        {"gpt-oss:120b-cloud", true, 131_072, 131_072},;
        {"gpt-oss:20b-cloud", true, 131_072, 131_072},;
        {"kimi-k2.5", false, 0, 0},;
        {"kimi-k2.5:cloud", true, 262_144, 262_144},;
        {"deepseek-v3.2", false, 0, 0},;
        {"deepseek-v3.2:cloud", true, 163_840, 65_536},;
        {"qwen3.5", false, 0, 0},;
        {"qwen3.5:cloud", true, 262_144, 32_768},;
        {"qwen3-coder:480b", false, 0, 0},;
        {"qwen3-coder:480b:cloud", true, 262_144, 65_536},;
        {"qwen3-coder-next:cloud", true, 262_144, 32_768},;
        {"llama3.2", false, 0, 0},;
        {"unknown-model:cloud", false, 0, 0},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var l, ok = lookupCloudModelLimit(tt.name);
        if ok != tt.wantOK {
        t.Errorf("lookupCloudModelLimit(%q) ok = %v, want %v", tt.name, ok, tt.wantOK);
    }
        if ok {
        if l.Context != tt.wantContext {
        t.Errorf("context = %d, want %d", l.Context, tt.wantContext);
    }
        if l.Output != tt.wantOutput {
        t.Errorf("output = %d, want %d", l.Output, tt.wantOutput);
    }
    }
        });
    }
    }

    public static void TestFindOpenCode(*testing.T t) {
        t.Run("fallback to ~/.opencode/bin", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        t.Setenv("PATH", tmpDir);
        var if _, ok = findOpenCode(); ok {
        t.Fatal("findOpenCode should fail when binary is not on PATH or in fallback location");
    }
        var binDir = filepath.Join(tmpDir, ".opencode", "bin");
        os.MkdirAll(binDir, 0o755);
        var name = "opencode";
        if runtime.GOOS == "windows" {
        name = "opencode.exe";
    }
        var fakeBin = filepath.Join(binDir, name);
        os.WriteFile(fakeBin, []byte("#!/bin/sh\n"), 0o755);
        var path, ok = findOpenCode();
        if !ok {
        t.Fatal("findOpenCode should succeed with fallback binary");
    }
        if path != fakeBin {
        t.Errorf("findOpenCode = %q, want %q", path, fakeBin);
    }
        });
    }

    public static void TestOpenCodeEdit_CloudModelLimitStructure(*testing.T t) {
        var o = &OpenCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var expected = cloudModelLimits["glm-4.7"];
        var if err = o.Edit([]String{"glm-4.7:cloud"}); err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        json.Unmarshal([]byte(o.configContent), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var models, _ = ollama["models"].(map[String]any);
        var entry, _ = models["glm-4.7:cloud"].(map[String]any);
        var limit, ok = entry["limit"].(map[String]any);
        if !ok {
        t.Fatal("cloud model limit was not set");
    }
        if limit["context"] != double(expected.Context) {
        t.Errorf("context = %v, want %d", limit["context"], expected.Context);
    }
        if limit["output"] != double(expected.Output) {
        t.Errorf("output = %v, want %d", limit["output"], expected.Output);
    }
    }

    public static void TestOpenCodeEdit_SpecialCharsInModelName(*testing.T t) {
        var o = &OpenCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var specialModel = `model-with-"quotes"`;
        var err = o.Edit([]String{specialModel});
        if err != null {
        t.Fatalf("Edit with special chars failed: %v", err);
    }
        var cfg map[String]any;
        var if err = json.Unmarshal([]byte(o.configContent), &cfg); err != null {
        t.Fatalf("resulting config is invalid JSON: %v", err);
    }
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var models, _ = ollama["models"].(map[String]any);
        if models[specialModel] == null {
        t.Errorf("model with special chars not found in config");
    }
    }

    public static void TestReadModelJSONModels(*testing.T t) {
        t.Run("reads ollama models from model.json", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        map[String]any{"providerID": "ollama", "modelID": "qwen3:32b"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var models = readModelJSONModels();
        if len(models) != 2 {
        t.Fatalf("got %d models, want 2", len(models));
    }
        if models[0] != "llama3.2" || models[1] != "qwen3:32b" {
        t.Errorf("got %v, want [llama3.2 qwen3:32b]", models);
    }
        });
        t.Run("skips non-ollama providers", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "openai", "modelID": "gpt-4"},;
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var models = readModelJSONModels();
        if len(models) != 1 || models[0] != "llama3.2" {
        t.Errorf("got %v, want [llama3.2]", models);
    }
        });
        t.Run("returns null when file does not exist", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var if models = readModelJSONModels(); models != null {
        t.Errorf("got %v, want null", models);
    }
        });
        t.Run("returns null for corrupt JSON", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        os.WriteFile(filepath.Join(stateDir, "model.json"), []byte(`{corrupt`), 0o644);
        var if models = readModelJSONModels(); models != null {
        t.Errorf("got %v, want null", models);
    }
        });
    }

    public static void TestOpenCodeResolveContent(*testing.T t) {
        t.Run("returns Edit's content when set", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"gemma4"}); err != null {
        t.Fatal(err);
    }
        var editContent = o.configContent;
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "different-model"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var got = o.resolveContent("gemma4");
        if got != editContent {
        t.Errorf("resolveContent returned different content than Edit set\ngot:  %s\nwant: %s", got, editContent);
    }
        });
        t.Run("falls back to model.json when Edit was not called", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        map[String]any{"providerID": "ollama", "modelID": "qwen3:32b"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var content = o.resolveContent("llama3.2");
        if content == "" {
        t.Fatal("resolveContent returned empty");
    }
        var cfg map[String]any;
        json.Unmarshal([]byte(content), &cfg);
        if cfg["model"] != "ollama/llama3.2" {
        t.Errorf("primary = %v, want ollama/llama3.2", cfg["model"]);
    }
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var cfgModels, _ = ollama["models"].(map[String]any);
        if cfgModels["llama3.2"] == null || cfgModels["qwen3:32b"] == null {
        t.Errorf("expected both models in config, got %v", cfgModels);
    }
        });
        t.Run("uses requested model as primary even when not first in model.json", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        map[String]any{"providerID": "ollama", "modelID": "qwen3:32b"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var content = o.resolveContent("qwen3:32b");
        var cfg map[String]any;
        json.Unmarshal([]byte(content), &cfg);
        if cfg["model"] != "ollama/qwen3:32b" {
        t.Errorf("primary = %v, want ollama/qwen3:32b", cfg["model"]);
    }
        });
        t.Run("injects requested model when missing from model.json", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var content = o.resolveContent("gemma4");
        var cfg map[String]any;
        json.Unmarshal([]byte(content), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var cfgModels, _ = ollama["models"].(map[String]any);
        if cfgModels["gemma4"] == null {
        t.Error("requested model gemma4 not injected into config");
    }
        if cfg["model"] != "ollama/gemma4" {
        t.Errorf("primary = %v, want ollama/gemma4", cfg["model"]);
    }
        });
        t.Run("returns empty when no model.json and no model param", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var o = &OpenCode{}
        var if got = o.resolveContent(""); got != "" {
        t.Errorf("resolveContent(\"\") = %q, want empty", got);
    }
        });
        t.Run("does not mutate configContent on fallback", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var state = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        },;
    }
        var data, _ = json.MarshalIndent(state, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        _ = o.resolveContent("llama3.2");
        if o.configContent != "" {
        t.Errorf("resolveContent should not mutate configContent, got %q", o.configContent);
    }
        });
    }

    public static void TestBuildInlineConfig(*testing.T t) {
        t.Run("returns error for empty primary", func(t *testing.T) {
        var if _, err = buildInlineConfig("", []String{"llama3.2"}); err == null {
        t.Error("expected error for empty primary");
    }
        });
        t.Run("returns error for empty models", func(t *testing.T) {
        var if _, err = buildInlineConfig("llama3.2", null); err == null {
        t.Error("expected error for empty models");
    }
        });
        t.Run("primary differs from first model in list", func(t *testing.T) {
        var content, err = buildInlineConfig("qwen3:32b", []String{"llama3.2", "qwen3:32b"});
        if err != null {
        t.Fatal(err);
    }
        var cfg map[String]any;
        json.Unmarshal([]byte(content), &cfg);
        if cfg["model"] != "ollama/qwen3:32b" {
        t.Errorf("primary = %v, want ollama/qwen3:32b", cfg["model"]);
    }
        });
    }

    public static void TestOpenCodeEdit_PreservesRecentEntries(*testing.T t) {
        t.Run("prepends new models to existing recent", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var initial = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "old-A"},;
        map[String]any{"providerID": "ollama", "modelID": "old-B"},;
        },;
    }
        var data, _ = json.MarshalIndent(initial, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"new-X"}); err != null {
        t.Fatal(err);
    }
        var stored, _ = os.ReadFile(filepath.Join(stateDir, "model.json"));
        var state map[String]any;
        json.Unmarshal(stored, &state);
        var recent, _ = state["recent"].([]any);
        if len(recent) != 3 {
        t.Fatalf("expected 3 entries, got %d", len(recent));
    }
        var first, _ = recent[0].(map[String]any);
        if first["modelID"] != "new-X" {
        t.Errorf("first entry = %v, want new-X", first["modelID"]);
    }
        });
        t.Run("prepends multiple new models in order", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var initial = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "old-A"},;
        map[String]any{"providerID": "ollama", "modelID": "old-B"},;
        },;
    }
        var data, _ = json.MarshalIndent(initial, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"X", "Y", "Z"}); err != null {
        t.Fatal(err);
    }
        var stored, _ = os.ReadFile(filepath.Join(stateDir, "model.json"));
        var state map[String]any;
        json.Unmarshal(stored, &state);
        var recent, _ = state["recent"].([]any);
        var want = []String{"X", "Y", "Z", "old-A", "old-B"}
        if len(recent) != len(want) {
        t.Fatalf("expected %d entries, got %d", len(want), len(recent));
    }
        var for i, w = range want {
        var e, _ = recent[i].(map[String]any);
        if e["modelID"] != w {
        t.Errorf("recent[%d] = %v, want %v", i, e["modelID"], w);
    }
    }
        });
        t.Run("preserves non-ollama entries", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var initial = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "openai", "modelID": "gpt-4"},;
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        },;
    }
        var data, _ = json.MarshalIndent(initial, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"qwen3:32b"}); err != null {
        t.Fatal(err);
    }
        var stored, _ = os.ReadFile(filepath.Join(stateDir, "model.json"));
        var state map[String]any;
        json.Unmarshal(stored, &state);
        var recent, _ = state["recent"].([]any);
        var foundOpenAI boolean;
        var for _, entry = range recent {
        var e, _ = entry.(map[String]any);
        if e["providerID"] == "openai" && e["modelID"] == "gpt-4" {
        foundOpenAI = true;
    }
    }
        if !foundOpenAI {
        t.Errorf("non-ollama gpt-4 entry was not preserved, got %v", recent);
    }
        });
        t.Run("deduplicates ollama models being re-added", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var initial = map[String]any{
        "recent": []any{
        map[String]any{"providerID": "ollama", "modelID": "llama3.2"},;
        },;
    }
        var data, _ = json.MarshalIndent(initial, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"llama3.2"}); err != null {
        t.Fatal(err);
    }
        var stored, _ = os.ReadFile(filepath.Join(stateDir, "model.json"));
        var state map[String]any;
        json.Unmarshal(stored, &state);
        var recent, _ = state["recent"].([]any);
        var count = 0;
        var for _, entry = range recent {
        var e, _ = entry.(map[String]any);
        if e["modelID"] == "llama3.2" {
        count++;
    }
    }
        if count != 1 {
        t.Errorf("expected 1 llama3.2 entry, got %d", count);
    }
        });
        t.Run("caps recent list at 10", func(t *testing.T) {
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        var stateDir = filepath.Join(tmpDir, ".local", "state", "opencode");
        os.MkdirAll(stateDir, 0o755);
        var recentEntries = make([]any, 0, 9);
        var for i = range 9 {
        recentEntries = append(recentEntries, map[String]any{
        "providerID": "ollama",;
        "modelID":    fmt.Sprintf("old-%d", i),;
        });
    }
        var initial = map[String]any{"recent": recentEntries}
        var data, _ = json.MarshalIndent(initial, "", "  ");
        os.WriteFile(filepath.Join(stateDir, "model.json"), data, 0o644);
        var o = &OpenCode{}
        var if err = o.Edit([]String{"new-0", "new-1", "new-2", "new-3", "new-4"}); err != null {
        t.Fatal(err);
    }
        var stored, _ = os.ReadFile(filepath.Join(stateDir, "model.json"));
        var state map[String]any;
        json.Unmarshal(stored, &state);
        var recent, _ = state["recent"].([]any);
        if len(recent) != 10 {
        t.Errorf("expected 10 entries (capped), got %d", len(recent));
    }
        });
    }

    public static void TestOpenCodeEdit_BaseURL(*testing.T t) {
        var o = &OpenCode{}
        var tmpDir = t.TempDir();
        setTestHome(t, tmpDir);
        o.Edit([]String{"llama3.2"});
        var cfg map[String]any;
        json.Unmarshal([]byte(o.configContent), &cfg);
        var provider, _ = cfg["provider"].(map[String]any);
        var ollama, _ = provider["ollama"].(map[String]any);
        var options, _ = ollama["options"].(map[String]any);
        var baseURL, _ = options["baseURL"].(String);
        if baseURL == "" {
        t.Error("baseURL should be set");
    }
    }
}
