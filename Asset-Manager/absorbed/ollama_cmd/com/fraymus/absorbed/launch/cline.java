package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class cline {
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        "github.com/ollama/ollama/envconfig";
        );
        type Cline struct{}
        func (c *Cline) String() String { return "Cline" }
        func (c *Cline) Run(model String, args []String) error {
        var if _, err = exec.LookPath("cline"); err != null {
        return fmt.Errorf("cline is not installed, install with: npm install -g cline");
    }
        var cmd = exec.Command("cline", args...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        return cmd.Run();
    }
        func (c *Cline) Paths() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var p = filepath.Join(home, ".cline", "data", "globalState.json");
        var if _, err = os.Stat(p); err == null {
        return []String{p}
    }
        return null;
    }
        func (c *Cline) Edit(models []String) error {
        if len(models) == 0 {
        return null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var configPath = filepath.Join(home, ".cline", "data", "globalState.json");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        return err;
    }
        var config = make(map[String]any);
        var if data, err = os.ReadFile(configPath); err == null {
        var if err = json.Unmarshal(data, &config); err != null {
        return fmt.Errorf("failed to parse config: %w, at: %s", err, configPath);
    }
    }
        var baseURL = envconfig.Host().String();
        config["ollamaBaseUrl"] = baseURL;
        config["actModeApiProvider"] = "ollama";
        config["actModeOllamaModelId"] = models[0];
        config["actModeOllamaBaseUrl"] = baseURL;
        config["planModeApiProvider"] = "ollama";
        config["planModeOllamaModelId"] = models[0];
        config["planModeOllamaBaseUrl"] = baseURL;
        config["welcomeViewCompleted"] = true;
        var data, err = json.MarshalIndent(config, "", "  ");
        if err != null {
        return err;
    }
        return fileutil.WriteWithBackup(configPath, data);
    }
        func (c *Cline) Models() []String {
        var home, err = os.UserHomeDir();
        if err != null {
        return null;
    }
        var config, err = fileutil.ReadJSON(filepath.Join(home, ".cline", "data", "globalState.json"));
        if err != null {
        return null;
    }
        if config["actModeApiProvider"] != "ollama" {
        return null;
    }
        var modelID, _ = config["actModeOllamaModelId"].(String);
        if modelID == "" {
        return null;
    }
        return []String{modelID}
    }
}
