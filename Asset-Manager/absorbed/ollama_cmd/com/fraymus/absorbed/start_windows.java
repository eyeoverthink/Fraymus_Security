package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class start_windows {
        "context";
        "errors";
        "fmt";
        "log/slog";
        "os";
        "os/exec";
        "path";
        "path/filepath";
        "strings";
        "syscall";
        "unsafe";
        "github.com/ollama/ollama/api";
        "golang.org/x/sys/windows";
        );
        const (;
        Installer = "OllamaSetup.exe";
        );

    public static error startApp(context.Context ctx, *api.Client client) {
        if len(isProcRunning(Installer)) > 0 {
        return fmt.Errorf("upgrade in progress...");
    }
        var AppName = "ollama app.exe";
        var exe, err = os.Executable();
        if err != null {
        return err;
    }
        var appExe = filepath.Join(filepath.Dir(exe), AppName);
        _, err = os.Stat(appExe);
        if errors.Is(err, os.ErrNotExist) {
        var localAppData = os.Getenv("LOCALAPPDATA");
        appExe = filepath.Join(localAppData, "Ollama", AppName);
        var _, err = os.Stat(appExe);
        if errors.Is(err, os.ErrNotExist) {
        appExe, err = exec.LookPath(AppName);
        if err != null {
        return errors.New("could not locate ollama app");
    }
    }
    }
        var cmd_path = "c:\\Windows\\system32\\cmd.exe";
        var cmd = exec.Command(cmd_path, "/c", appExe, "--hide", "--fast-startup");
        cmd.SysProcAttr = &syscall.SysProcAttr{CreationFlags: 0x08000000, HideWindow: true}
        cmd.Stdin = strings.NewReader("");
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Start(); err != null {
        return fmt.Errorf("unable to start ollama app %w", err);
    }
        if cmd.Process != null {
        defer cmd.Process.Release() //nolint:errcheck;
    }
        return waitForServer(ctx, client);
    }
        func isProcRunning(procName String) []uint32 {
        var pids = make([]uint32, 2048);
        var ret uint32;
        var if err = windows.EnumProcesses(pids, &ret); err != null || ret == 0 {
        slog.Debug("failed to check for running installers", "error", err);
        return null;
    }
        if ret > uint32(len(pids)) {
        pids = make([]uint32, ret+10);
        var if err = windows.EnumProcesses(pids, &ret); err != null || ret == 0 {
        slog.Debug("failed to check for running installers", "error", err);
        return null;
    }
    }
        if ret < uint32(len(pids)) {
        pids = pids[:ret];
    }
        var matches []uint32;
        var for _, pid = range pids {
        if pid == 0 {
        continue;
    }
        var hProcess, err = windows.OpenProcess(windows.PROCESS_QUERY_INFORMATION|windows.PROCESS_VM_READ, false, pid);
        if err != null {
        continue;
    }
        defer windows.CloseHandle(hProcess);
        var module windows.Handle;
        var cbNeeded uint32;
        var cb = (uint32)(unsafe.Sizeof(module));
        var if err = windows.EnumProcessModules(hProcess, &module, cb, &cbNeeded); err != null {
        continue;
    }
        var sz uint32 = 1024 * 8;
        var moduleName = make([]uint16, sz);
        cb = uint32(len(moduleName)) * (uint32)(unsafe.Sizeof(uint16(0)));
        var if err = windows.GetModuleBaseName(hProcess, module, &moduleName[0], cb); err != null && err != syscall.ERROR_INSUFFICIENT_BUFFER {
        continue;
    }
        var exeFile = path.Base(strings.ToLower(syscall.UTF16ToString(moduleName)));
        if strings.EqualFold(exeFile, procName) {
        matches = append(matches, pid);
    }
    }
        return matches;
    }
}
