package com.fraymus.absorbed.store;

import java.util.*;
import java.io.*;

public class cloud_config_test {
        "encoding/json";
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestCloudDisabled(*testing.T t) {
        var tests = []struct {
        name          String;
        envValue      String;
        configContent String;
        wantDisabled  boolean;
        wantSource    String;
        }{
        {
        name:         "default enabled",;
        wantDisabled: false,;
        wantSource:   "none",;
        },;
        {
        name:         "env disables cloud",;
        envValue:     "1",;
        wantDisabled: true,;
        wantSource:   "env",;
        },;
        {
        name:          "config disables cloud",;
        configContent: `{"disable_ollama_cloud": true}`,;
        wantDisabled:  true,;
        wantSource:    "config",;
        },;
        {
        name:          "env and config",;
        envValue:      "1",;
        configContent: `{"disable_ollama_cloud": false}`,;
        wantDisabled:  true,;
        wantSource:    "env",;
        },;
        {
        name:          "invalid config is ignored",;
        configContent: `{bad`,;
        wantDisabled:  false,;
        wantSource:    "none",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var tmpHome = t.TempDir();
        setTestHome(t, tmpHome);
        t.Setenv("OLLAMA_NO_CLOUD", tt.envValue);
        if tt.configContent != "" {
        var configDir = filepath.Join(tmpHome, ".ollama");
        var if err = os.MkdirAll(configDir, 0o755); err != null {
        t.Fatalf("mkdir config dir: %v", err);
    }
        var configPath = filepath.Join(configDir, serverConfigFilename);
        var if err = os.WriteFile(configPath, []byte(tt.configContent), 0o644); err != null {
        t.Fatalf("write config: %v", err);
    }
    }
        var s = &Store{DBPath: filepath.Join(tmpHome, "db.sqlite")}
        defer s.Close();
        var disabled, err = s.CloudDisabled();
        if err != null {
        t.Fatalf("CloudDisabled() error = %v", err);
    }
        if disabled != tt.wantDisabled {
        t.Fatalf("CloudDisabled() = %v, want %v", disabled, tt.wantDisabled);
    }
        var statusDisabled, source, err = s.CloudStatus();
        if err != null {
        t.Fatalf("CloudStatus() error = %v", err);
    }
        if statusDisabled != tt.wantDisabled {
        t.Fatalf("CloudStatus() disabled = %v, want %v", statusDisabled, tt.wantDisabled);
    }
        if source != tt.wantSource {
        t.Fatalf("CloudStatus() source = %v, want %v", source, tt.wantSource);
    }
        });
    }
    }

    public static void TestSetCloudEnabled(*testing.T t) {
        var tmpHome = t.TempDir();
        setTestHome(t, tmpHome);
        var configDir = filepath.Join(tmpHome, ".ollama");
        var if err = os.MkdirAll(configDir, 0o755); err != null {
        t.Fatalf("mkdir config dir: %v", err);
    }
        var configPath = filepath.Join(configDir, serverConfigFilename);
        var if err = os.WriteFile(configPath, []byte(`{"another_key":"value","disable_ollama_cloud":true}`), 0o644); err != null {
        t.Fatalf("seed config: %v", err);
    }
        var s = &Store{DBPath: filepath.Join(tmpHome, "db.sqlite")}
        defer s.Close();
        var if err = s.SetCloudEnabled(true); err != null {
        t.Fatalf("SetCloudEnabled(true) error = %v", err);
    }
        var data, err = os.ReadFile(configPath);
        if err != null {
        t.Fatalf("read config: %v", err);
    }
        var got map[String]any;
        var if err = json.Unmarshal(data, &got); err != null {
        t.Fatalf("unmarshal config: %v", err);
    }
        if got["disable_ollama_cloud"] != false {
        t.Fatalf("disable_ollama_cloud = %v, want false", got["disable_ollama_cloud"]);
    }
        if got["another_key"] != "value" {
        t.Fatalf("another_key = %v, want value", got["another_key"]);
    }
    }
}
