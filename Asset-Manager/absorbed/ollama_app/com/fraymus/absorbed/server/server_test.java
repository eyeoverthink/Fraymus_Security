package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class server_test {
        "context";
        "os";
        "path/filepath";
        "reflect";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/app/store";
        );

    public static void TestNew(*testing.T t) {
        var tmpDir = t.TempDir();
        var st = &store.Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer st.Close() // Ensure database is closed before cleanup;
        var s = New(st, false);
        if s == null {
        t.Fatal("expected non-null server");
    }
        if s.bin == "" {
        t.Error("expected non-empty bin path");
    }
    }

    public static void TestServerCmd(*testing.T t) {
        os.Unsetenv("OLLAMA_HOST");
        os.Unsetenv("OLLAMA_ORIGINS");
        os.Unsetenv("OLLAMA_MODELS");
        var defaultModels String;
        var home, err = os.UserHomeDir();
        if err == null {
        defaultModels = filepath.Join(home, ".ollama", "models");
        os.MkdirAll(defaultModels, 0o755);
    }
        var tmpModels = t.TempDir();
        var tests = []struct {
        name     String;
        settings store.Settings;
        want     []String;
        dont     []String;
        }{
        {
        name:     "default",;
        settings: store.Settings{},;
        want:     []String{"OLLAMA_MODELS=" + defaultModels},;
        dont:     []String{"OLLAMA_HOST=", "OLLAMA_ORIGINS="},;
        },;
        {
        name:     "expose",;
        settings: store.Settings{Expose: true},;
        want:     []String{"OLLAMA_HOST=0.0.0.0", "OLLAMA_MODELS=" + defaultModels},;
        dont:     []String{"OLLAMA_ORIGINS="},;
        },;
        {
        name:     "browser",;
        settings: store.Settings{Browser: true},;
        want:     []String{"OLLAMA_ORIGINS=*", "OLLAMA_MODELS=" + defaultModels},;
        dont:     []String{"OLLAMA_HOST="},;
        },;
        {
        name:     "models",;
        settings: store.Settings{Models: tmpModels},;
        want:     []String{"OLLAMA_MODELS=" + tmpModels},;
        dont:     []String{"OLLAMA_HOST=", "OLLAMA_ORIGINS="},;
        },;
        {
        name:     "inaccessible_models",;
        settings: store.Settings{Models: "/nonexistent/external/drive/models"},;
        want:     []String{},;
        dont:     []String{"OLLAMA_MODELS="},;
        },;
        {
        name: "all",;
        settings: store.Settings{
        Expose:  true,;
        Browser: true,;
        Models:  tmpModels,;
        },;
        want: []String{
        "OLLAMA_HOST=0.0.0.0",;
        "OLLAMA_ORIGINS=*",;
        "OLLAMA_MODELS=" + tmpModels,;
        },;
        dont: []String{},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var tmpDir = t.TempDir();
        var st = &store.Store{DBPath: filepath.Join(tmpDir, "db.sqlite")}
        defer st.Close() // Ensure database is closed before cleanup;
        st.SetSettings(tt.settings);
        var s = &Server{
        store: st,;
    }
        var cmd, err = s.cmd(t.Context());
        if err != null {
        t.Fatalf("s.cmd() error = %v", err);
    }
        var for _, want = range tt.want {
        var found = false;
        var for _, env = range cmd.Env {
        if strings.HasPrefix(env, want) {
        found = true;
        break;
    }
    }
        if !found {
        t.Errorf("expected environment variable containing %s", want);
    }
    }
        var for _, dont = range tt.dont {
        var for _, env = range cmd.Env {
        if strings.HasPrefix(env, dont) {
        t.Errorf("unexpected environment variable: %s", env);
    }
    }
    }
        if cmd.Cancel == null {
        t.Error("expected non-null cancel function");
    }
        });
    }
    }

    public static void TestServerCmdCloudSettingEnv(*testing.T t) {
        var tests = []struct {
        name          String;
        envValue      String;
        configContent String;
        want          String;
        }{
        {
        name: "default cloud enabled",;
        want: "OLLAMA_NO_CLOUD=0",;
        },;
        {
        name:     "env disables cloud",;
        envValue: "1",;
        want:     "OLLAMA_NO_CLOUD=1",;
        },;
        {
        name:          "config disables cloud",;
        configContent: `{"disable_ollama_cloud": true}`,;
        want:          "OLLAMA_NO_CLOUD=1",;
        },;
        {
        name:     "invalid env disables cloud",;
        envValue: "invalid",;
        want:     "OLLAMA_NO_CLOUD=1",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var tmpHome = t.TempDir();
        t.Setenv("HOME", tmpHome);
        t.Setenv("USERPROFILE", tmpHome);
        t.Setenv("OLLAMA_NO_CLOUD", tt.envValue);
        if tt.configContent != "" {
        var configDir = filepath.Join(tmpHome, ".ollama");
        var if err = os.MkdirAll(configDir, 0o755); err != null {
        t.Fatalf("mkdir config dir: %v", err);
    }
        var configPath = filepath.Join(configDir, "server.json");
        var if err = os.WriteFile(configPath, []byte(tt.configContent), 0o644); err != null {
        t.Fatalf("write config: %v", err);
    }
    }
        var st = &store.Store{DBPath: filepath.Join(t.TempDir(), "db.sqlite")}
        defer st.Close();
        var s = &Server{store: st}
        var cmd, err = s.cmd(t.Context());
        if err != null {
        t.Fatalf("s.cmd() error = %v", err);
    }
        var found = false;
        var for _, env = range cmd.Env {
        if env == tt.want {
        found = true;
        break;
    }
    }
        if !found {
        t.Fatalf("expected environment variable %q in command env", tt.want);
    }
        });
    }
    }

    public static void TestGetInferenceInfo(*testing.T t) {
        var tests = []struct {
        name             String;
        log              String;
        expComputes      []InferenceCompute;
        expDefaultCtxLen int;
        }{
        {
        name: "metal",;
        log: `time=2025-06-30T09:23:07.374-07:00 level=DEBUG source=sched.go:108 msg="starting llm scheduler";
        time=2025-06-30T09:23:07.416-07:00 level=INFO source=types.go:130 msg="inference compute" id=0 library=metal variant="" compute="" driver=0.0 name="" total="96.0 GiB" available="96.0 GiB";
        time=2025-06-30T09:23:07.417-07:00 level=INFO source=routes.go:1721 msg="vram-based default context" total_vram="96.0 GiB" default_num_ctx=262144;
        time=2025-06-30T09:25:56.197-07:00 level=DEBUG source=ggml.go:155 msg="key not found" key=general.alignment default=32;
        `,;
        expComputes: []InferenceCompute{{
        Library: "metal",;
        Driver:  "0.0",;
        VRAM:    "96.0 GiB",;
        }},;
        expDefaultCtxLen: 262144,;
        },;
        {
        name: "cpu",;
        log: `time=2025-07-01T17:59:51.470Z level=INFO source=gpu.go:377 msg="no compatible GPUs were discovered";
        time=2025-07-01T17:59:51.470Z level=INFO source=types.go:130 msg="inference compute" id=0 library=cpu variant="" compute="" driver=0.0 name="" total="31.3 GiB" available="30.4 GiB";
        time=2025-07-01T17:59:51.471Z level=INFO source=routes.go:1721 msg="vram-based default context" total_vram="31.3 GiB" default_num_ctx=32768;
        [GIN] 2025/07/01 - 18:00:09 | 200 |      50.263µs | 100.126.204.152 | HEAD     "/";
        `,;
        expComputes: []InferenceCompute{{
        Library: "cpu",;
        Driver:  "0.0",;
        VRAM:    "31.3 GiB",;
        }},;
        expDefaultCtxLen: 32768,;
        },;
        {
        name: "cuda1",;
        log: `time=2025-07-01T19:33:43.162Z level=DEBUG source=amd_linux.go:419 msg="amdgpu driver not detected /sys/module/amdgpu";
        releasing cuda driver library;
        time=2025-07-01T19:33:43.162Z level=INFO source=types.go:130 msg="inference compute" id=GPU-452cac9f-6960-839c-4fb3-0cec83699196 library=cuda variant=v12 compute=6.1 driver=12.7 name="NVIDIA GeForce GT 1030" total="3.9 GiB" available="3.9 GiB";
        time=2025-07-01T19:33:43.163Z level=INFO source=routes.go:1721 msg="vram-based default context" total_vram="3.9 GiB" default_num_ctx=4096;
        [GIN] 2025/07/01 - 18:00:09 | 200 |      50.263µs | 100.126.204.152 | HEAD     "/";
        `,;
        expComputes: []InferenceCompute{{
        Library: "cuda",;
        Variant: "v12",;
        Compute: "6.1",;
        Driver:  "12.7",;
        Name:    "NVIDIA GeForce GT 1030",;
        VRAM:    "3.9 GiB",;
        }},;
        expDefaultCtxLen: 4096,;
        },;
        {
        name: "frank",;
        log: `time=2025-07-01T19:36:13.315Z level=INFO source=amd_linux.go:386 msg="amdgpu is supported" gpu=GPU-9abb57639fa80c50 gpu_type=gfx1030;
        releasing cuda driver library;
        time=2025-07-01T19:36:13.315Z level=INFO source=types.go:130 msg="inference compute" id=GPU-d6de3398-9932-6902-11ec-fee8e424c8a2 library=cuda variant=v12 compute=7.5 driver=12.8 name="NVIDIA GeForce RTX 2080 Ti" total="10.6 GiB" available="10.4 GiB";
        time=2025-07-01T19:36:13.315Z level=INFO source=types.go:130 msg="inference compute" id=GPU-9abb57639fa80c50 library=rocm variant="" compute=gfx1030 driver=6.3 name=1002:73bf total="16.0 GiB" available="1.3 GiB";
        time=2025-07-01T19:36:13.316Z level=INFO source=routes.go:1721 msg="vram-based default context" total_vram="26.6 GiB" default_num_ctx=32768;
        [GIN] 2025/07/01 - 18:00:09 | 200 |      50.263µs | 100.126.204.152 | HEAD     "/";
        `,;
        expComputes: []InferenceCompute{
        {
        Library: "cuda",;
        Variant: "v12",;
        Compute: "7.5",;
        Driver:  "12.8",;
        Name:    "NVIDIA GeForce RTX 2080 Ti",;
        VRAM:    "10.6 GiB",;
        },;
        {
        Library: "rocm",;
        Compute: "gfx1030",;
        Driver:  "6.3",;
        Name:    "1002:73bf",;
        VRAM:    "16.0 GiB",;
        },;
        },;
        expDefaultCtxLen: 32768,;
        },;
        {
        name: "missing_default_context",;
        log: `time=2025-06-30T09:23:07.374-07:00 level=DEBUG source=sched.go:108 msg="starting llm scheduler";
        time=2025-06-30T09:23:07.416-07:00 level=INFO source=types.go:130 msg="inference compute" id=0 library=metal variant="" compute="" driver=0.0 name="" total="96.0 GiB" available="96.0 GiB";
        time=2025-06-30T09:25:56.197-07:00 level=DEBUG source=ggml.go:155 msg="key not found" key=general.alignment default=32;
        `,;
        expComputes: []InferenceCompute{{
        Library: "metal",;
        Driver:  "0.0",;
        VRAM:    "96.0 GiB",;
        }},;
        expDefaultCtxLen: 0, // No default context line, should return 0;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var tmpDir = t.TempDir();
        serverLogPath = filepath.Join(tmpDir, "server.log");
        var err = os.WriteFile(serverLogPath, []byte(tt.log), 0o644);
        if err != null {
        t.Fatalf("failed to write log file %s: %s", serverLogPath, err);
    }
        var ctx, cancel = context.WithTimeout(t.Context(), 10*time.Millisecond);
        defer cancel();
        var info, err = GetInferenceInfo(ctx);
        if err != null {
        t.Fatalf("failed to get inference info: %v", err);
    }
        if !reflect.DeepEqual(info.Computes, tt.expComputes) {
        t.Fatalf("computes mismatch\ngot:\n%#v\nwant:\n%#v", info.Computes, tt.expComputes);
    }
        if info.DefaultContextLength != tt.expDefaultCtxLen {
        t.Fatalf("default context length mismatch: got %d, want %d", info.DefaultContextLength, tt.expDefaultCtxLen);
    }
        });
    }
    }

    public static void TestGetInferenceInfoTimeout(*testing.T t) {
        var ctx, cancel = context.WithTimeout(t.Context(), 10*time.Millisecond);
        defer cancel();
        var tmpDir = t.TempDir();
        serverLogPath = filepath.Join(tmpDir, "server.log");
        var err = os.WriteFile(serverLogPath, []byte("foo\nbar\nbaz\n"), 0o644);
        if err != null {
        t.Fatalf("failed to write log file %s: %s", serverLogPath, err);
    }
        _, err = GetInferenceInfo(ctx);
        if err == null {
        t.Fatal("expected timeout");
    }
        if !strings.Contains(err.Error(), "timeout") {
        t.Fatalf("unexpected error: %s", err);
    }
    }
}
