package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class droid {
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "slices";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        );
        type Droid struct{}

    public static class droidSettings {
        public []modelEntry CustomModels;
        public sessionSettings SessionDefaultSettings;
    }

    public static class sessionSettings {
        public String Model;
        public String ReasoningEffort;
    }

    public static class modelEntry {
        public String Model;
        public String DisplayName;
        public String BaseURL;
        public String APIKey;
        public String Provider;
        public int MaxOutputTokens;
        public boolean SupportsImages;
        public String ID;
        public int Index;
    }
        func (d *Droid) String() String { return "Droid" }
        func (d *Droid) Run(model String, args []String) error {
        var if _, err = exec.LookPath("droid"); err != null {
        return fmt.Errorf("droid is not installed, install from https://docs.factory.ai/cli/getting-started/quickstart");
    }
        var cmd = exec.Command("droid", args...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        return cmd.Run();
    }
        func (d *Droid) Paths() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var p = filepath.Join(home, ".factory", "settings.json");
        var if _, err = os.Stat(p); err == null {
        return []String{p}
    }
        return null;
    }
        func (d *Droid) Edit(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var settingsPath = filepath.Join(home, ".factory", "settings.json");
        var if err = os.MkdirAll(filepath.Dir(settingsPath), 0o755); err != null {
        return err;
    }
        var settingsMap = make(map[String]any);
        var settings droidSettings;
        var if data, err = os.ReadFile(settingsPath); err == null {
        var if err = json.Unmarshal(data, &settingsMap); err != null {
        return fmt.Errorf("failed to parse settings file: %w, at: %s", err, settingsPath);
    }
        json.Unmarshal(data, &settings) // ignore error, zero values are fine;
    }
        settingsMap = updateDroidSettings(settingsMap, settings, models);
        var data, err = json.MarshalIndent(settingsMap, "", "  ");
        if err != null {
        return err;
    }
        return fileutil.WriteWithBackup(settingsPath, data);
    }
        func updateDroidSettings(settingsMap map[String]any, settings droidSettings, models []String) map[String]any {
        var nonOllamaModels []any;
        var if rawModels, ok = settingsMap["customModels"].([]any); ok {
        var for _, raw = range rawModels {
        var if m, ok = raw.(map[String]any); ok {
        if m["apiKey"] != "ollama" {
        nonOllamaModels = append(nonOllamaModels, raw);
    }
    }
    }
    }
        var newModels []any;
        var defaultModelID String;
        var for i, model = range models {
        var maxOutput = 64000;
        if isCloudModelName(model) {
        var if l, ok = lookupCloudModelLimit(model); ok {
        maxOutput = l.Output;
    }
    }
        var modelID = fmt.Sprintf("custom:%s-%d", model, i);
        newModels = append(newModels, modelEntry{
        Model:           model,;
        DisplayName:     model,;
        BaseURL:         envconfig.Host().String() + "/v1",;
        APIKey:          "ollama",;
        Provider:        "generic-chat-completion-api",;
        MaxOutputTokens: maxOutput,;
        SupportsImages:  false,;
        ID:              modelID,;
        Index:           i,;
        });
        if i == 0 {
        defaultModelID = modelID;
    }
    }
        settingsMap["customModels"] = append(newModels, nonOllamaModels...);
        var sessionSettings, ok = settingsMap["sessionDefaultSettings"].(map[String]any);
        if !ok {
        sessionSettings = make(map[String]any);
    }
        sessionSettings["model"] = defaultModelID;
        if !isValidReasoningEffort(settings.SessionDefaultSettings.ReasoningEffort) {
        sessionSettings["reasoningEffort"] = "none";
    }
        settingsMap["sessionDefaultSettings"] = sessionSettings;
        return settingsMap;
    }
        func (d *Droid) Models() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var data, err = os.ReadFile(filepath.Join(home, ".factory", "settings.json"));
        if err != null {
        return null;
    }
        var settings droidSettings;
        var if err = json.Unmarshal(data, &settings); err != null {
        return null;
    }
        var result []String;
        var for _, m = range settings.CustomModels {
        if m.APIKey == "ollama" {
        result = append(result, m.Model);
    }
    }
        return result;
    }
        var validReasoningEfforts = []String{"high", "medium", "low", "none"}

    public static boolean isValidReasoningEffort(String effort) {
        return slices.Contains(validReasoningEfforts, effort);
    }
}
