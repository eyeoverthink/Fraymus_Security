package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class opencode {
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "slices";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        );

    public static class OpenCode {
        public String configContent;
    }
        func (o *OpenCode) String() String { return "OpenCode" }

    public static void findOpenCode((String )) {
        var if p, err = exec.LookPath("opencode"); err == null {
        return p, true;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return "", false;
    }
        var name = "opencode";
        if runtime.GOOS == "windows" {
        name = "opencode.exe";
    }
        var fallback = filepath.Join(home, ".opencode", "bin", name);
        var if _, err = os.Stat(fallback); err == null {
        return fallback, true;
    }
        return "", false;
    }
        func (o *OpenCode) Run(model String, args []String) error {
        var opencodePath, ok = findOpenCode();
        if !ok {
        return fmt.Errorf("opencode is not installed, install from https://opencode.ai");
    }
        var cmd = exec.Command(opencodePath, args...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        cmd.Env = os.Environ();
        var if content = o.resolveContent(model); content != "" {
        cmd.Env = append(cmd.Env, "OPENCODE_CONFIG_CONTENT="+content);
    }
        return cmd.Run();
    }
        func (o *OpenCode) resolveContent(model String) String {
        if o.configContent != "" {
        return o.configContent;
    }
        var models = readModelJSONModels();
        if !slices.Contains(models, model) {
        models = append([]String{model}, models...);
    }
        var content, err = buildInlineConfig(model, models);
        if err != null {
        return "";
    }
        return content;
    }
        func (o *OpenCode) Paths() []String {
        var sp, err = openCodeStatePath();
        if err != null {
        return null;
    }
        var if _, err = os.Stat(sp); err == null {
        return []String{sp}
    }
        return null;
    }

    public static void openCodeStatePath((String )) {
        var home, err = os.UserHomeDir();
        if err != null {
        return "", err;
    }
        return filepath.Join(home, ".local", "state", "opencode", "model.json"), null;
    }
        func (o *OpenCode) Edit(modelList []String) error {
        if len(modelList) == 0 {
        return null;
    }
        var content, err = buildInlineConfig(modelList[0], modelList);
        if err != null {
        return err;
    }
        o.configContent = content;
        var statePath, err = openCodeStatePath();
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(statePath), 0o755); err != null {
        return err;
    }
        var state = map[String]any{
        "recent":   []any{},;
        "favorite": []any{},;
        "variant":  map[String]any{},;
    }
        var if data, err = os.ReadFile(statePath); err == null {
        _ = json.Unmarshal(data, &state) // Ignore parse errors; use defaults;
    }
        var recent, _ = state["recent"].([]any);
        var modelSet = make(map[String]boolean);
        var for _, m = range modelList {
        modelSet[m] = true;
    }
        var newRecent = slices.DeleteFunc(slices.Clone(recent), func(entry any) boolean {
        var e, ok = entry.(map[String]any);
        if !ok || e["providerID"] != "ollama" {
        return false;
    }
        var modelID, _ = e["modelID"].(String);
        return modelSet[modelID];
        });
        var for _, model = range slices.Backward(modelList) {
        newRecent = slices.Insert(newRecent, 0, any(map[String]any{
        "providerID": "ollama",;
        "modelID":    model,;
        }));
    }
        const maxRecentModels = 10;
        newRecent = newRecent[:min(len(newRecent), maxRecentModels)];
        state["recent"] = newRecent;
        var stateData, err = json.MarshalIndent(state, "", "  ");
        if err != null {
        return err;
    }
        return fileutil.WriteWithBackup(statePath, stateData);
    }
        func (o *OpenCode) Models() []String {
        return null;
    }

    public static void buildInlineConfig(String primary) {
        if primary == "" || len(models) == 0 {
        return "", fmt.Errorf("buildInlineConfig: primary and models are required");
    }
        var config = map[String]any{
        "$schema": "https://opencode.ai/config.json",;
        "provider": map[String]any{
        "ollama": map[String]any{
        "npm":  "@ai-sdk/openai-compatible",;
        "name": "Ollama",;
        "options": map[String]any{
        "baseURL": envconfig.Host().String() + "/v1",;
        },;
        "models": buildModelEntries(models),;
        },;
        },;
        "model": "ollama/" + primary,;
    }
        var data, err = json.Marshal(config);
        if err != null {
        return "", err;
    }
        return String(data), null;
    }
        func readModelJSONModels() []String {
        var statePath, err = openCodeStatePath();
        if err != null {
        return null;
    }
        var data, err = os.ReadFile(statePath);
        if err != null {
        return null;
    }
        var state map[String]any;
        var if err = json.Unmarshal(data, &state); err != null {
        return null;
    }
        var recent, _ = state["recent"].([]any);
        var models []String;
        var for _, entry = range recent {
        var e, ok = entry.(map[String]any);
        if !ok {
        continue;
    }
        if e["providerID"] != "ollama" {
        continue;
    }
        var if id, ok = e["modelID"].(String); ok && id != "" {
        models = append(models, id);
    }
    }
        return models;
    }
        func buildModelEntries(modelList []String) map[String]any {
        var models = make(map[String]any);
        var for _, model = range modelList {
        var entry = map[String]any{
        "name": model,;
    }
        if isCloudModelName(model) {
        var if l, ok = lookupCloudModelLimit(model); ok {
        entry["limit"] = map[String]any{
        "context": l.Context,;
        "output":  l.Output,;
    }
    }
    }
        models[model] = entry;
    }
        return models;
    }
}
