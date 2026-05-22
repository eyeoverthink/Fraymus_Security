package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class claude {
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "strconv";
        "github.com/ollama/ollama/envconfig";
        );
        type Claude struct{}
        func (c *Claude) String() String { return "Claude Code" }
        func (c *Claude) args(model String, extra []String) []String {
        var args []String;
        if model != "" {
        args = append(args, "--model", model);
    }
        args = append(args, extra...);
        return args;
    }
        func (c *Claude) findPath() (String, error) {
        var if p, err = exec.LookPath("claude"); err == null {
        return p, null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return "", err;
    }
        var name = "claude";
        if runtime.GOOS == "windows" {
        name = "claude.exe";
    }
        var fallback = filepath.Join(home, ".claude", "local", name);
        var if _, err = os.Stat(fallback); err != null {
        return "", err;
    }
        return fallback, null;
    }
        func (c *Claude) Run(model String, args []String) error {
        var claudePath, err = c.findPath();
        if err != null {
        return fmt.Errorf("claude is not installed, install from https://code.claude.com/docs/en/quickstart");
    }
        var cmd = exec.Command(claudePath, c.args(model, args)...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var env = append(os.Environ(),;
        "ANTHROPIC_BASE_URL="+envconfig.Host().String(),;
        "ANTHROPIC_API_KEY=",;
        "ANTHROPIC_AUTH_TOKEN=ollama",;
        "CLAUDE_CODE_ATTRIBUTION_HEADER=0",;
        );
        env = append(env, c.modelEnvVars(model)...);
        cmd.Env = env;
        return cmd.Run();
    }
        func (c *Claude) modelEnvVars(model String) []String {
        var env = []String{
        "ANTHROPIC_DEFAULT_OPUS_MODEL=" + model,;
        "ANTHROPIC_DEFAULT_SONNET_MODEL=" + model,;
        "ANTHROPIC_DEFAULT_HAIKU_MODEL=" + model,;
        "CLAUDE_CODE_SUBAGENT_MODEL=" + model,;
    }
        if isCloudModelName(model) {
        var if l, ok = lookupCloudModelLimit(model); ok {
        env = append(env, "CLAUDE_CODE_AUTO_COMPACT_WINDOW="+strconv.Itoa(l.Context));
    }
    }
        return env;
    }
}
