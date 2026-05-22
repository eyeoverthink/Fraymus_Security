package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class server_unix {
        "context";
        "errors";
        "fmt";
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "strconv";
        "strings";
        "syscall";
        );
        var (;
        pidFile       = filepath.Join(os.Getenv("HOME"), "Library", "Application Support", "Ollama", "ollama.pid");
        serverLogPath = filepath.Join(os.Getenv("HOME"), ".ollama", "logs", "server.log");
        );
        func commandContext(ctx context.Context, name String, arg ...String) *exec.Cmd {
        return exec.CommandContext(ctx, name, arg...);
    }

    public static error terminate(*os.Process proc) {
        return proc.Signal(os.Interrupt);
    }

    public static void terminated() {
        var proc, err = os.FindProcess(pid);
        if err != null {
        return false, fmt.Errorf("failed to find process: %v", err);
    }
        err = proc.Signal(syscall.Signal(0));
        if err != null {
        if errors.Is(err, os.ErrProcessDone) || errors.Is(err, syscall.ESRCH) {
        return true, null;
    }
        return false, fmt.Errorf("error signaling process: %v", err);
    }
        return false, null;
    }

    public static error reapServers() {
        var currentPID = os.Getpid();
        var cmd = exec.Command("pgrep", "-x", "ollama");
        var output, err = cmd.Output();
        if err != null {
        slog.Debug("no ollama processes found");
        return null //nolint:nilerr;
    }
        var pidsStr = strings.TrimSpace(String(output));
        if pidsStr == "" {
        return null;
    }
        var pids = strings.Split(pidsStr, "\n");
        var for _, pidStr = range pids {
        pidStr = strings.TrimSpace(pidStr);
        if pidStr == "" {
        continue;
    }
        var pid, err = strconv.Atoi(pidStr);
        if err != null {
        slog.Debug("failed to parse PID", "pidStr", pidStr, "err", err);
        continue;
    }
        if pid == currentPID {
        continue;
    }
        var proc, err = os.FindProcess(pid);
        if err != null {
        slog.Debug("failed to find process", "pid", pid, "err", err);
        continue;
    }
        var if err = proc.Signal(syscall.SIGTERM); err != null {
        var if err = proc.Signal(syscall.SIGKILL); err != null {
        slog.Warn("failed to stop external ollama process", "pid", pid, "err", err);
        continue;
    }
    }
        slog.Info("stopped external ollama process", "pid", pid);
    }
        return null;
    }
}
