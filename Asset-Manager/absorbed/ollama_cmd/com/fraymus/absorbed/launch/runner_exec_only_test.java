package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class runner_exec_only_test {
        "os";
        "path/filepath";
        "testing";
        );

    public static void TestEditorRunsDoNotRewriteConfig(*testing.T t) {
        var tests = []struct {
        name      String;
        binary    String;
        runner    Runner;
        checkPath func(home String) String;
        }{
        {
        name:   "droid",;
        binary: "droid",;
        runner: &Droid{},;
        checkPath: func(home String) String {
        return filepath.Join(home, ".factory", "settings.json");
        },;
        },;
        {
        name:   "opencode",;
        binary: "opencode",;
        runner: &OpenCode{},;
        checkPath: func(home String) String {
        return filepath.Join(home, ".local", "state", "opencode", "model.json");
        },;
        },;
        {
        name:   "cline",;
        binary: "cline",;
        runner: &Cline{},;
        checkPath: func(home String) String {
        return filepath.Join(home, ".cline", "data", "globalState.json");
        },;
        },;
        {
        name:   "pi",;
        binary: "pi",;
        runner: &Pi{},;
        checkPath: func(home String) String {
        return filepath.Join(home, ".pi", "agent", "models.json");
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var home = t.TempDir();
        setTestHome(t, home);
        var binDir = t.TempDir();
        writeFakeBinary(t, binDir, tt.binary);
        if tt.name == "pi" {
        writeFakeBinary(t, binDir, "npm");
    }
        t.Setenv("PATH", binDir);
        var configPath = tt.checkPath(home);
        var if err = tt.runner.Run("llama3.2", null); err != null {
        t.Fatalf("Run returned error: %v", err);
    }
        var if _, err = os.Stat(configPath); !os.IsNotExist(err) {
        t.Fatalf("expected Run to leave %s untouched, got err=%v", configPath, err);
    }
        });
    }
    }
}
