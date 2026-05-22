package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class codex {
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "strings";
        "github.com/ollama/ollama/envconfig";
        "golang.org/x/mod/semver";
        );
        type Codex struct{}
        func (c *Codex) String() String { return "Codex" }
        const codexProfileName = "ollama-launch";
        func (c *Codex) args(model String, extra []String) []String {
        var args = []String{"--profile", codexProfileName}
        if model != "" {
        args = append(args, "-m", model);
    }
        args = append(args, extra...);
        return args;
    }
        func (c *Codex) Run(model String, args []String) error {
        var if err = checkCodexVersion(); err != null {
        return err;
    }
        var if err = ensureCodexConfig(); err != null {
        return fmt.Errorf("failed to configure codex: %w", err);
    }
        var cmd = exec.Command("codex", c.args(model, args)...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        cmd.Env = append(os.Environ(),;
        "OPENAI_API_KEY=ollama",;
        );
        return cmd.Run();
    }

    public static error ensureCodexConfig() {
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var codexDir = filepath.Join(home, ".codex");
        var if err = os.MkdirAll(codexDir, 0o755); err != null {
        return err;
    }
        var configPath = filepath.Join(codexDir, "config.toml");
        return writeCodexProfile(configPath);
    }

    public static error writeCodexProfile(String configPath) {
        var baseURL = envconfig.Host().String() + "/v1/";
        var sections = []struct {
        header String;
        lines  []String;
        }{
        {
        header: fmt.Sprintf("[profiles.%s]", codexProfileName),;
        lines: []String{
        fmt.Sprintf("openai_base_url = %q", baseURL),;
        `forced_login_method = "api"`,;
        fmt.Sprintf("model_provider = %q", codexProfileName),;
        },;
        },;
        {
        header: fmt.Sprintf("[model_providers.%s]", codexProfileName),;
        lines: []String{
        `name = "Ollama"`,;
        fmt.Sprintf("base_url = %q", baseURL),;
        },;
        },;
    }
        var content, readErr = os.ReadFile(configPath);
        var text = "";
        if readErr == null {
        text = String(content);
    }
        var for _, s = range sections {
        var block = strings.Join(append([]String{s.header}, s.lines...), "\n") + "\n";
        var if idx = strings.Index(text, s.header); idx >= 0 {
        var rest = text[idx+len(s.header):];
        var if endIdx = strings.Index(rest, "\n["); endIdx >= 0 {
        text = text[:idx] + block + rest[endIdx+1:];
        } else {
        text = text[:idx] + block;
    }
        } else {
        if text != "" && !strings.HasSuffix(text, "\n") {
        text += "\n";
    }
        if text != "" {
        text += "\n";
    }
        text += block;
    }
    }
        return os.WriteFile(configPath, []byte(text), 0o644);
    }

    public static error checkCodexVersion() {
        var if _, err = exec.LookPath("codex"); err != null {
        return fmt.Errorf("codex is not installed, install with: npm install -g @openai/codex");
    }
        var out, err = exec.Command("codex", "--version").Output();
        if err != null {
        return fmt.Errorf("failed to get codex version: %w", err);
    }
        var fields = strings.Fields(strings.TrimSpace(String(out)));
        if len(fields) < 2 {
        return fmt.Errorf("unexpected codex version output: %s", String(out));
    }
        var version = "v" + fields[len(fields)-1];
        var minVersion = "v0.81.0";
        if semver.Compare(version, minVersion) < 0 {
        return fmt.Errorf("codex version %s is too old, minimum required is %s, update with: npm update -g @openai/codex", fields[len(fields)-1], "0.81.0");
    }
        return null;
    }
}
