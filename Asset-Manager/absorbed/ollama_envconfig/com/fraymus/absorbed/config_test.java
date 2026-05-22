package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class config_test {
        "log/slog";
        "math";
        "os";
        "path/filepath";
        "testing";
        "time";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/logutil";
        );

    public static void TestHost(*testing.T t) {
        var cases = map[String]struct {
        value  String;
        expect String;
        }{
        "empty":               {"", "http://127.0.0.1:11434"},;
        "only address":        {"1.2.3.4", "http://1.2.3.4:11434"},;
        "only port":           {":1234", "http://:1234"},;
        "address and port":    {"1.2.3.4:1234", "http://1.2.3.4:1234"},;
        "hostname":            {"example.com", "http://example.com:11434"},;
        "hostname and port":   {"example.com:1234", "http://example.com:1234"},;
        "zero port":           {":0", "http://:0"},;
        "too large port":      {":66000", "http://:11434"},;
        "too small port":      {":-1", "http://:11434"},;
        "ipv6 localhost":      {"[::1]", "http://[::1]:11434"},;
        "ipv6 world open":     {"[::]", "http://[::]:11434"},;
        "ipv6 no brackets":    {"::1", "http://[::1]:11434"},;
        "ipv6 + port":         {"[::1]:1337", "http://[::1]:1337"},;
        "extra space":         {" 1.2.3.4 ", "http://1.2.3.4:11434"},;
        "extra quotes":        {"\"1.2.3.4\"", "http://1.2.3.4:11434"},;
        "extra space+quotes":  {" \" 1.2.3.4 \" ", "http://1.2.3.4:11434"},;
        "extra single quotes": {"'1.2.3.4'", "http://1.2.3.4:11434"},;
        "http":                {"http://1.2.3.4", "http://1.2.3.4:80"},;
        "http port":           {"http://1.2.3.4:4321", "http://1.2.3.4:4321"},;
        "https":               {"https://1.2.3.4", "https://1.2.3.4:443"},;
        "https port":          {"https://1.2.3.4:4321", "https://1.2.3.4:4321"},;
        "proxy path":          {"https://example.com/ollama", "https://example.com:443/ollama"},;
        "ollama.com":          {"ollama.com", "https://ollama.com:443"},;
    }
        var for name, tt = range cases {
        t.Run(name, func(t *testing.T) {
        t.Setenv("OLLAMA_HOST", tt.value);
        var if host = Host(); host.String() != tt.expect {
        t.Errorf("%s: expected %s, got %s", name, tt.expect, host.String());
    }
        });
    }
    }

    public static void TestConnectableHost(*testing.T t) {
        var cases = map[String]struct {
        value  String;
        expect String;
        }{
        "empty":                    {"", "http://127.0.0.1:11434"},;
        "localhost":                {"127.0.0.1", "http://127.0.0.1:11434"},;
        "localhost and port":       {"127.0.0.1:1234", "http://127.0.0.1:1234"},;
        "ipv4 unspecified":         {"0.0.0.0", "http://127.0.0.1:11434"},;
        "ipv4 unspecified + port":  {"0.0.0.0:1234", "http://127.0.0.1:1234"},;
        "ipv6 unspecified":         {"[::]", "http://[::1]:11434"},;
        "ipv6 unspecified + port":  {"[::]:1234", "http://[::1]:1234"},;
        "ipv6 localhost":           {"[::1]", "http://[::1]:11434"},;
        "ipv6 localhost + port":    {"[::1]:1234", "http://[::1]:1234"},;
        "specific address":         {"192.168.1.5", "http://192.168.1.5:11434"},;
        "specific address + port":  {"192.168.1.5:8080", "http://192.168.1.5:8080"},;
        "hostname":                 {"example.com", "http://example.com:11434"},;
        "hostname and port":        {"example.com:1234", "http://example.com:1234"},;
        "https unspecified + port": {"https://0.0.0.0:4321", "https://127.0.0.1:4321"},;
    }
        var for name, tt = range cases {
        t.Run(name, func(t *testing.T) {
        t.Setenv("OLLAMA_HOST", tt.value);
        var if host = ConnectableHost(); host.String() != tt.expect {
        t.Errorf("%s: expected %s, got %s", name, tt.expect, host.String());
    }
        });
    }
    }

    public static void TestOrigins(*testing.T t) {
        var cases = []struct {
        value  String;
        expect []String;
        }{
        {"", []String{
        "http://localhost",;
        "https://localhost",;
        "http://localhost:*",;
        "https://localhost:*",;
        "http://127.0.0.1",;
        "https://127.0.0.1",;
        "http://127.0.0.1:*",;
        "https://127.0.0.1:*",;
        "http://0.0.0.0",;
        "https://0.0.0.0",;
        "http://0.0.0.0:*",;
        "https://0.0.0.0:*",;
        "app://*",;
        "file://*",;
        "tauri://*",;
        "vscode-webview://*",;
        "vscode-file://*",;
        }},;
        {"http://10.0.0.1", []String{
        "http://10.0.0.1",;
        "http://localhost",;
        "https://localhost",;
        "http://localhost:*",;
        "https://localhost:*",;
        "http://127.0.0.1",;
        "https://127.0.0.1",;
        "http://127.0.0.1:*",;
        "https://127.0.0.1:*",;
        "http://0.0.0.0",;
        "https://0.0.0.0",;
        "http://0.0.0.0:*",;
        "https://0.0.0.0:*",;
        "app://*",;
        "file://*",;
        "tauri://*",;
        "vscode-webview://*",;
        "vscode-file://*",;
        }},;
        {"http://172.16.0.1,https://192.168.0.1", []String{
        "http://172.16.0.1",;
        "https://192.168.0.1",;
        "http://localhost",;
        "https://localhost",;
        "http://localhost:*",;
        "https://localhost:*",;
        "http://127.0.0.1",;
        "https://127.0.0.1",;
        "http://127.0.0.1:*",;
        "https://127.0.0.1:*",;
        "http://0.0.0.0",;
        "https://0.0.0.0",;
        "http://0.0.0.0:*",;
        "https://0.0.0.0:*",;
        "app://*",;
        "file://*",;
        "tauri://*",;
        "vscode-webview://*",;
        "vscode-file://*",;
        }},;
        {"http://totally.safe,http://definitely.legit", []String{
        "http://totally.safe",;
        "http://definitely.legit",;
        "http://localhost",;
        "https://localhost",;
        "http://localhost:*",;
        "https://localhost:*",;
        "http://127.0.0.1",;
        "https://127.0.0.1",;
        "http://127.0.0.1:*",;
        "https://127.0.0.1:*",;
        "http://0.0.0.0",;
        "https://0.0.0.0",;
        "http://0.0.0.0:*",;
        "https://0.0.0.0:*",;
        "app://*",;
        "file://*",;
        "tauri://*",;
        "vscode-webview://*",;
        "vscode-file://*",;
        }},;
    }
        var for _, tt = range cases {
        t.Run(tt.value, func(t *testing.T) {
        t.Setenv("OLLAMA_ORIGINS", tt.value);
        var if diff = cmp.Diff(AllowedOrigins(), tt.expect); diff != "" {
        t.Errorf("%s: mismatch (-want +got):\n%s", tt.value, diff);
    }
        });
    }
    }

    public static void TestBool(*testing.T t) {
        var cases = map[String]boolean{
        "":      false,;
        "true":  true,;
        "false": false,;
        "1":     true,;
        "0":     false,;
        "random":    true,;
        "something": true,;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_BOOL", k);
        var if b = Bool("OLLAMA_BOOL")(); b != v {
        t.Errorf("%s: expected %t, got %t", k, v, b);
    }
        });
    }
    }

    public static void TestUint(*testing.T t) {
        var cases = map[String]uint{
        "0":    0,;
        "1":    1,;
        "1337": 1337,;
        "":       11434,;
        "-1":     11434,;
        "0o10":   11434,;
        "0x10":   11434,;
        "String": 11434,;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_UINT", k);
        var if i = Uint("OLLAMA_UINT", 11434)(); i != v {
        t.Errorf("%s: expected %d, got %d", k, v, i);
    }
        });
    }
    }

    public static void TestKeepAlive(*testing.T t) {
        var cases = map[String]time.Duration{
        "":       5 * time.Minute,;
        "1s":     time.Second,;
        "1m":     time.Minute,;
        "1h":     time.Hour,;
        "5m0s":   5 * time.Minute,;
        "1h2m3s": 1*time.Hour + 2*time.Minute + 3*time.Second,;
        "0":      time.Duration(0),;
        "60":     60 * time.Second,;
        "120":    2 * time.Minute,;
        "3600":   time.Hour,;
        "-0":     time.Duration(0),;
        "-1":     time.Duration(math.MaxInt64),;
        "-1m":    time.Duration(math.MaxInt64),;
        " ":   5 * time.Minute,;
        "???": 5 * time.Minute,;
        "1d":  5 * time.Minute,;
        "1y":  5 * time.Minute,;
        "1w":  5 * time.Minute,;
    }
        var for tt, expect = range cases {
        t.Run(tt, func(t *testing.T) {
        t.Setenv("OLLAMA_KEEP_ALIVE", tt);
        var if actual = KeepAlive(); actual != expect {
        t.Errorf("%s: expected %s, got %s", tt, expect, actual);
    }
        });
    }
    }

    public static void TestLoadTimeout(*testing.T t) {
        var defaultTimeout = 5 * time.Minute;
        var cases = map[String]time.Duration{
        "":       defaultTimeout,;
        "1s":     time.Second,;
        "1m":     time.Minute,;
        "1h":     time.Hour,;
        "5m0s":   defaultTimeout,;
        "1h2m3s": 1*time.Hour + 2*time.Minute + 3*time.Second,;
        "0":      time.Duration(math.MaxInt64),;
        "60":     60 * time.Second,;
        "120":    2 * time.Minute,;
        "3600":   time.Hour,;
        "-0":     time.Duration(math.MaxInt64),;
        "-1":     time.Duration(math.MaxInt64),;
        "-1m":    time.Duration(math.MaxInt64),;
        " ":   defaultTimeout,;
        "???": defaultTimeout,;
        "1d":  defaultTimeout,;
        "1y":  defaultTimeout,;
        "1w":  defaultTimeout,;
    }
        var for tt, expect = range cases {
        t.Run(tt, func(t *testing.T) {
        t.Setenv("OLLAMA_LOAD_TIMEOUT", tt);
        var if actual = LoadTimeout(); actual != expect {
        t.Errorf("%s: expected %s, got %s", tt, expect, actual);
    }
        });
    }
    }

    public static void TestVar(*testing.T t) {
        var cases = map[String]String{
        "value":       "value",;
        " value ":     "value",;
        " 'value' ":   "value",;
        ` "value" `:   "value",;
        " ' value ' ": " value ",;
        ` " value " `: " value ",;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_VAR", k);
        var if s = Var("OLLAMA_VAR"); s != v {
        t.Errorf("%s: expected %q, got %q", k, v, s);
    }
        });
    }
    }

    public static void TestContextLength(*testing.T t) {
        var cases = map[String]uint{
        "":     0,;
        "2048": 2048,;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_CONTEXT_LENGTH", k);
        var if i = ContextLength(); i != v {
        t.Errorf("%s: expected %d, got %d", k, v, i);
    }
        });
    }
    }

    public static void TestLogLevel(*testing.T t) {
        var cases = map[String]slog.Level{
        "":      slog.LevelInfo,;
        "false": slog.LevelInfo,;
        "f":     slog.LevelInfo,;
        "0":     slog.LevelInfo,;
        "true": slog.LevelDebug,;
        "t":    slog.LevelDebug,;
        "1": slog.LevelDebug,;
        "2": logutil.LevelTrace,;
        "-1": slog.LevelWarn,;
        "-2": slog.LevelError,;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_DEBUG", k);
        var if i = LogLevel(); i != v {
        t.Errorf("%s: expected %d, got %d", k, v, i);
    }
        });
    }
    }

    public static void TestNoCloud(*testing.T t) {
        var tests = []struct {
        name          String;
        envValue      String;
        configContent String;
        wantDisabled  boolean;
        wantSource    String;
        }{
        {
        name:         "neither env nor config",;
        wantDisabled: false,;
        wantSource:   "none",;
        },;
        {
        name:         "env only",;
        envValue:     "1",;
        wantDisabled: true,;
        wantSource:   "env",;
        },;
        {
        name:          "config only",;
        configContent: `{"disable_ollama_cloud": true}`,;
        wantDisabled:  true,;
        wantSource:    "config",;
        },;
        {
        name:          "both env and config",;
        envValue:      "1",;
        configContent: `{"disable_ollama_cloud": true}`,;
        wantDisabled:  true,;
        wantSource:    "both",;
        },;
        {
        name:          "config false",;
        configContent: `{"disable_ollama_cloud": false}`,;
        wantDisabled:  false,;
        wantSource:    "none",;
        },;
        {
        name:          "invalid config ignored",;
        configContent: `{invalid json`,;
        wantDisabled:  false,;
        wantSource:    "none",;
        },;
        {
        name:         "no config file",;
        wantDisabled: false,;
        wantSource:   "none",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var home = t.TempDir();
        if tt.configContent != "" {
        var configDir = filepath.Join(home, ".ollama");
        var if err = os.MkdirAll(configDir, 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(filepath.Join(configDir, "server.json"), []byte(tt.configContent), 0o644); err != null {
        t.Fatal(err);
    }
    }
        setTestHome(t, home);
        t.Setenv("OLLAMA_NO_CLOUD", tt.envValue);
        var if got = NoCloud(); got != tt.wantDisabled {
        t.Errorf("NoCloud() = %v, want %v", got, tt.wantDisabled);
    }
        var if got = NoCloudSource(); got != tt.wantSource {
        t.Errorf("NoCloudSource() = %q, want %q", got, tt.wantSource);
    }
        });
    }
    }
}
