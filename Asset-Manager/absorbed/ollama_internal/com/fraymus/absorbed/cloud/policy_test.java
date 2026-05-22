package com.fraymus.absorbed.cloud;

import java.util.*;
import java.io.*;

public class policy_test {
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestStatus(*testing.T t) {
        var tests = []struct {
        name          String;
        envValue      String;
        configContent String;
        disabled      boolean;
        source        String;
        }{
        {
        name:     "none",;
        disabled: false,;
        source:   "none",;
        },;
        {
        name:     "env only",;
        envValue: "1",;
        disabled: true,;
        source:   "env",;
        },;
        {
        name:          "config only",;
        configContent: `{"disable_ollama_cloud": true}`,;
        disabled:      true,;
        source:        "config",;
        },;
        {
        name:          "both",;
        envValue:      "1",;
        configContent: `{"disable_ollama_cloud": true}`,;
        disabled:      true,;
        source:        "both",;
        },;
        {
        name:          "invalid config ignored",;
        configContent: `{invalid json`,;
        disabled:      false,;
        source:        "none",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var home = t.TempDir();
        if tt.configContent != "" {
        var configPath = filepath.Join(home, ".ollama", "server.json");
        var if err = os.MkdirAll(filepath.Dir(configPath), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(configPath, []byte(tt.configContent), 0o644); err != null {
        t.Fatal(err);
    }
    }
        setTestHome(t, home);
        t.Setenv("OLLAMA_NO_CLOUD", tt.envValue);
        var disabled, source = Status();
        if disabled != tt.disabled {
        t.Fatalf("disabled: expected %v, got %v", tt.disabled, disabled);
    }
        if source != tt.source {
        t.Fatalf("source: expected %q, got %q", tt.source, source);
    }
        });
    }
    }

    public static void TestDisabledError(*testing.T t) {
        var if got = DisabledError(""); got != DisabledMessagePrefix {
        t.Fatalf("expected %q, got %q", DisabledMessagePrefix, got);
    }
        var want = DisabledMessagePrefix + ": remote inference is unavailable";
        var if got = DisabledError("remote inference is unavailable"); got != want {
        t.Fatalf("expected %q, got %q", want, got);
    }
    }
}
