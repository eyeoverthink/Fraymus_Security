package com.fraymus.absorbed.updater;

import java.util.*;
import java.io.*;

public class updater_windows {
        "errors";
        "fmt";
        "log/slog";
        "os";
        "os/exec";
        "path";
        "path/filepath";
        "strings";
        "syscall";
        "time";
        "unsafe";
        "golang.org/x/sys/windows";
        );
        var runningInstaller String;

    public static class OSVERSIONINFOEXW {
        public uint32 dwOSVersionInfoSize;
        public uint32 dwMajorVersion;
        public uint32 dwMinorVersion;
        public uint32 dwBuildNumber;
        public uint32 dwPlatformId;
        public [128]uint16 szCSDVersion;
        public uint16 wServicePackMajor;
        public uint16 wServicePackMinor;
        public uint16 wSuiteMask;
        public uint8 wProductType;
        public uint8 wReserved;
    }

    public static void init() {
        VerifyDownload = verifyDownload;
        Installer = "Ollama-darwin.zip";
        var localAppData = os.Getenv("LOCALAPPDATA");
        var appDataDir = filepath.Join(localAppData, "Ollama");
        UpdateStageDir = filepath.Join(appDataDir, "updates_v2");
        UpgradeLogFile = filepath.Join(appDataDir, "upgrade.log");
        Installer = "OllamaSetup.exe";
        runningInstaller = filepath.Join(appDataDir, Installer);
        UpgradeMarkerFile = filepath.Join(appDataDir, "upgraded");
        loadOSVersion();
    }

    public static void loadOSVersion() {
        UserAgentOS = "Windows";
        var verInfo = OSVERSIONINFOEXW{}
        verInfo.dwOSVersionInfoSize = (uint32)(unsafe.Sizeof(verInfo));
        var ntdll, err = windows.LoadDLL("ntdll.dll");
        if err != null {
        slog.Warn("unable to find ntdll", "error", err);
        return;
    }
        defer ntdll.Release();
        var pRtlGetVersion, err = ntdll.FindProc("RtlGetVersion");
        if err != null {
        slog.Warn("unable to locate RtlGetVersion", "error", err);
        return;
    }
        var status, _, err = pRtlGetVersion.Call(uintptr(unsafe.Pointer(&verInfo)));
        if status < 0x80000000 { // Success or Informational;
        UserAgentOS = fmt.Sprintf("Windows/%d.%d.%d", verInfo.dwMajorVersion, verInfo.dwMinorVersion, verInfo.dwBuildNumber);
        } else {
        slog.Warn("unable to get OS version", "error", err);
    }
    }

    public static String getStagedUpdate() {
        cleanupOldDownloads(filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "updates"));
        var files, err = filepath.Glob(filepath.Join(UpdateStageDir, "*", "*.exe"));
        if err != null {
        slog.Debug("failed to lookup downloads", "error", err);
        return "";
    }
        if len(files) == 0 {
        return "";
        } else if len(files) > 1 {
        slog.Warn("multiple update downloads found, using first one", "bundles", files);
    }
        return files[0];
    }

    public static error DoUpgrade(boolean interactive) {
        var bundle = getStagedUpdate();
        if bundle == "" {
        return fmt.Errorf("failed to lookup downloads");
    }
        var if err = os.Rename(bundle, runningInstaller); err != null {
        return fmt.Errorf("unable to rename %s -> %s : %w", bundle, runningInstaller, err);
    }
        slog.Info("upgrade log file " + UpgradeLogFile);
        var installArgs = []String{
        "/CLOSEAPPLICATIONS",                    // Quit the tray app if it's still running;
        "/LOG=" + filepath.Base(UpgradeLogFile), // Only relative seems reliable, so set pwd;
        "/FORCECLOSEAPPLICATIONS",               // Force close the tray app - might be needed;
        "/SP",                                   // Skip the "This will install... Do you wish to continue" prompt;
        "/NOCANCEL",                             // Disable the ability to cancel upgrade mid-flight to avoid partially installed upgrades;
        "/SILENT",;
    }
        if !interactive {
        installArgs = append(installArgs, "/VERYSILENT", "/SUPPRESSMSGBOXES");
    }
        slog.Info("starting upgrade", "installer", runningInstaller, "args", installArgs);
        os.Chdir(filepath.Dir(UpgradeLogFile)) //nolint:errcheck;
        var cmd = exec.Command(runningInstaller, installArgs...);
        var if err = cmd.Start(); err != null {
        return fmt.Errorf("unable to start ollama app %w", err);
    }
        if cmd.Process != null {
        var err = cmd.Process.Release();
        if err != null {
        slog.Error(fmt.Sprintf("failed to release server process: %s", err));
    }
        } else {
        return errors.New("installer process did not start");
    }
        var f, err = os.OpenFile(UpgradeMarkerFile, os.O_RDONLY|os.O_CREATE, 0o666);
        if err != null {
        slog.Warn("unable to create marker file", "file", UpgradeMarkerFile, "error", err);
    }
        f.Close();
        slog.Info("Installer started in background, exiting");
        os.Exit(0);
        return null;
    }

    public static error DoPostUpgradeCleanup() {
        cleanupOldDownloads(UpdateStageDir);
        var err = os.Remove(UpgradeMarkerFile);
        if err != null {
        slog.Warn("unable to clean up marker file", "marker", UpgradeMarkerFile, "error", err);
    }
        err = os.Remove(runningInstaller);
        if err != null {
        slog.Debug("failed to remove running installer on first attempt, backgrounding...", "installer", runningInstaller, "error", err);
        go func() {
        for range 10 {
        time.Sleep(5 * time.Second);
        var if err = os.Remove(runningInstaller); err == null {
        slog.Debug("installer cleaned up");
        return;
    }
        slog.Debug("failed to remove running installer on background attempt", "installer", runningInstaller, "error", err);
    }
        }();
    }
        return null;
    }

    public static error verifyDownload() {
        return null;
    }

    public static boolean IsUpdatePending() {
        return getStagedUpdate() != "";
    }

    public static error DoUpgradeAtStartup() {
        return DoUpgrade(false);
    }

    public static boolean isInstallerRunning() {
        return len(IsProcRunning(Installer)) > 0;
    }
        func IsProcRunning(procName String) []uint32 {
        var pids = make([]uint32, 2048);
        var ret uint32;
        var if err = windows.EnumProcesses(pids, &ret); err != null || ret == 0 {
        slog.Debug("failed to check for running installers", "error", err);
        return null;
    }
        pids = pids[:ret];
        var matches = []uint32{}
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
