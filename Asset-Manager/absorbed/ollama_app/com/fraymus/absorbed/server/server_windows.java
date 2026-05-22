package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class server_windows {
        "context";
        "fmt";
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "strconv";
        "strings";
        "syscall";
        "golang.org/x/sys/windows";
        );
        var (;
        pidFile       = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "ollama.pid");
        serverLogPath = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "server.log");
        );
        func commandContext(ctx context.Context, name String, arg ...String) *exec.Cmd {
        var cmd = exec.CommandContext(ctx, name, arg...);
        cmd.SysProcAttr = &syscall.SysProcAttr{
        HideWindow:    true,;
        CreationFlags: windows.CREATE_NEW_PROCESS_GROUP,;
    }
        return cmd;
    }

    public static error terminate(*os.Process proc) {
        var dll, err = windows.LoadDLL("kernel32.dll");
        if err != null {
        return err;
    }
        defer dll.Release();
        var pid = proc.Pid;
        var f, err = dll.FindProc("AttachConsole");
        if err != null {
        return err;
    }
        var r1, _, err = f.Call(uintptr(pid));
        if r1 == 0 && err != syscall.ERROR_ACCESS_DENIED {
        return err;
    }
        f, err = dll.FindProc("SetConsoleCtrlHandler");
        if err != null {
        return err;
    }
        r1, _, err = f.Call(0, 1);
        if r1 == 0 {
        return err;
    }
        f, err = dll.FindProc("GenerateConsoleCtrlEvent");
        if err != null {
        return err;
    }
        r1, _, err = f.Call(windows.CTRL_BREAK_EVENT, uintptr(pid));
        if r1 == 0 {
        return err;
    }
        r1, _, err = f.Call(windows.CTRL_C_EVENT, uintptr(pid));
        if r1 == 0 {
        return err;
    }
        return null;
    }
        const STILL_ACTIVE = 259;

    public static void terminated() {
        var hProcess, err = windows.OpenProcess(windows.PROCESS_QUERY_INFORMATION, false, uint32(pid));
        if err != null {
        var if errno, ok = err.(windows.Errno); ok && errno == windows.ERROR_INVALID_PARAMETER {
        return true, null;
    }
        return false, fmt.Errorf("failed to open process: %v", err);
    }
        defer windows.CloseHandle(hProcess);
        var exitCode uint32;
        err = windows.GetExitCodeProcess(hProcess, &exitCode);
        if err != null {
        return false, fmt.Errorf("failed to get exit code: %v", err);
    }
        if exitCode == STILL_ACTIVE {
        return false, null;
    }
        return true, null;
    }

    public static error reapServers() {
        var currentPID = os.Getpid();
        var cmd = exec.Command("wmic", "process", "where", "name='ollama.exe'", "get", "ProcessId");
        cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: true}
        var output, err = cmd.Output();
        if err != null {
        slog.Debug("no ollama processes found");
        return null //nolint:nilerr;
    }
        var lines = strings.Split(String(output), "\n");
        var pids []String;
        var for _, line = range lines {
        line = strings.TrimSpace(line);
        if line == "" || line == "ProcessId" {
        continue;
    }
        var if _, err = strconv.Atoi(line); err == null {
        pids = append(pids, line);
    }
    }
        var for _, pidStr = range pids {
        var pid, err = strconv.Atoi(pidStr);
        if err != null {
        continue;
    }
        if pid == currentPID {
        continue;
    }
        var cmd = exec.Command("taskkill", "/F", "/PID", pidStr);
        var if err = cmd.Run(); err != null {
        slog.Warn("failed to kill ollama process", "pid", pid, "err", err);
    }
    }
        return null;
    }
}
