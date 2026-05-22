package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class copilot {
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "github.com/ollama/ollama/envconfig";
        );
        type Copilot struct{}
        func (c *Copilot) String() String { return "Copilot CLI" }
        func (c *Copilot) args(model String, extra []String) []String {
        var args []String;
        if model != "" {
        args = append(args, "--model", model);
    }
        args = append(args, extra...);
        return args;
    }
        func (c *Copilot) findPath() (String, error) {
        var if p, err = exec.LookPath("copilot"); err == null {
        return p, null;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        return "", err;
    }
        var name = "copilot";
        if runtime.GOOS == "windows" {
        name = "copilot.exe";
    }
        var fallback = filepath.Join(home, ".local", "bin", name);
        var if _, err = os.Stat(fallback); err != null {
        return "", err;
    }
        return fallback, null;
    }
        func (c *Copilot) Run(model String, args []String) error {
        var copilotPath, err = c.findPath();
        if err != null {
        return fmt.Errorf("copilot is not installed, install from https://docs.github.com/en/copilot/how-tos/set-up/install-copilot-cli");
    }
        var cmd = exec.Command(copilotPath, c.args(model, args)...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        cmd.Env = append(os.Environ(), c.envVars(model)...);
        return cmd.Run();
    }
        func (c *Copilot) envVars(model String) []String {
        var env = []String{
        "COPILOT_PROVIDER_BASE_URL=" + envconfig.Host().String() + "/v1",;
        "COPILOT_PROVIDER_API_KEY=",;
        "COPILOT_PROVIDER_WIRE_API=responses",;
    }
        if model != "" {
        env = append(env, "COPILOT_MODEL="+model);
    }
        return env;
    }
}
