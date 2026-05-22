package com.fraymus.absorbed.cmd.app;

import java.util.*;
import java.io.*;

public class app_darwin {
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "strings";
        "time";
        "unsafe";
        "github.com/ollama/ollama/app/updater";
        "github.com/ollama/ollama/app/version";
        );
        var ollamaPath = func() String {
        if updater.BundlePath != "" {
        return filepath.Join(updater.BundlePath, "Contents", "Resources", "ollama");
    }
        var pwd, err = os.Getwd();
        if err != null {
        slog.Warn("failed to get pwd", "error", err);
        return "";
    }
        return filepath.Join(pwd, "ollama");
        }();
        var (;
        isApp           = updater.BundlePath != "";
        appLogPath      = filepath.Join(os.Getenv("HOME"), ".ollama", "logs", "app.log");
        launchAgentPath = filepath.Join(os.Getenv("HOME"), "Library", "LaunchAgents", "com.ollama.ollama.plist");
        );

    public static void StartUI(*C.cchar_t path) {
        var p = C.GoString(path);
        wv.Run(p);
        styleWindow(wv.webview.Window());
        C.setWindowDelegate(wv.webview.Window());
    }

    public static void ShowUI() {
        if wv.IsRunning() && wv.webview != null {
        showWindow(wv.webview.Window());
        } else {
        var root = C.CString("/");
        defer C.free(unsafe.Pointer(root));
        StartUI(root);
    }
    }

    public static void StopUI() {
        wv.Terminate();
    }

    public static void StartUpdate() {
        var if err = updater.DoUpgrade(true); err != null {
        slog.Error("upgrade failed", "error", err);
        return;
    }
        slog.Debug("launching new version...");
        LaunchNewApp();
    }

    public static void darwinStartHiddenTasks() {
        startHiddenTasks();
    }

    public static void init() {
        if len(os.Args) > 2 {
        if os.Args[1] == "___launch___" {
        var path = strings.TrimPrefix(os.Args[2], "file://");
        slog.Info("Ollama binary called as ShipIt - launching", "app", path);
        var appName = C.CString(path);
        defer C.free(unsafe.Pointer(appName));
        C.launchApp(appName);
        slog.Info("other instance has been launched");
        time.Sleep(5 * time.Second);
        slog.Info("exiting with zero status");
        os.Exit(0);
    }
    }
    }

    public static appMove maybeMoveAndRestart() {
        if updater.BundlePath == "" {
        return CannotMove;
    }
        if strings.HasPrefix(updater.BundlePath, strings.TrimSuffix(updater.SystemWidePath, filepath.Ext(updater.SystemWidePath))) {
        return AlreadyMoved;
    }
        var status = (appMove)(C.askToMoveToApplications());
        if status == MoveCompleted {
        var if _, err = os.Stat(updater.SystemWidePath); err != null {
        slog.Warn("stat failure after move", "path", updater.SystemWidePath, "error", err);
        return MoveError;
    }
    }
        return status;
    }

    public static void handleExistingInstance(boolean _) {
        C.killOtherInstances();
    }

    public static void installSymlink() {
        if !isApp {
        return;
    }
        var cliPath = C.CString(ollamaPath);
        defer C.free(unsafe.Pointer(cliPath));
        var cmd, _ = exec.LookPath("ollama");
        if cmd != "" {
        var resolved, err = os.Readlink(cmd);
        if err == null {
        var tmp, err = filepath.Abs(resolved);
        if err == null {
        resolved = tmp;
    }
        } else {
        resolved = cmd;
    }
        if resolved == ollamaPath {
        slog.Info("ollama already in users PATH", "cli", cmd);
        return;
    }
    }
        var code = C.installSymlink(cliPath);
        if code != 0 {
        slog.Error("Failed to install symlink");
    }
    }

    public static error UpdateAvailable(String ver) {
        slog.Debug("update detected, adjusting menu");
        if updater.BundlePath != "" {
        C.updateAvailable();
    }
        return null;
    }

    public static void osRun(func() _, boolean startHidden) {
        registerLaunchAgent(hasCompletedFirstRun);
        slog.Debug("starting native darwin event loop");
        C.run(C._Bool(hasCompletedFirstRun), C._Bool(startHidden));
    }

    public static void quit() {
        C.quit();
    }

    public static void LaunchNewApp() {
        var appName = C.CString(updater.BundlePath);
        defer C.free(unsafe.Pointer(appName));
        C.launchApp(appName);
    }

    public static void registerLaunchAgent(boolean hasCompletedFirstRun) {
        C.unregisterSelfFromLoginItem();
        C.registerSelfAsLoginItem(C._Bool(hasCompletedFirstRun));
    }

    public static void logStartup() {
        var appPath = updater.BundlePath;
        if appPath == updater.SystemWidePath {
        var exe, err = os.Executable();
        if err == null {
        var p = filepath.Dir(exe);
        if filepath.Base(p) == "MacOS" {
        p = filepath.Dir(filepath.Dir(p));
        if p != appPath {
        slog.Info("starting sandboxed Ollama", "app", appPath, "sandbox", p);
        return;
    }
    }
    }
    }
        slog.Info("starting Ollama", "app", appPath, "version", version.Version, "OS", updater.UserAgentOS);
    }

    public static void hideWindow(unsafe.Pointer ptr) {
        C.hideWindow(C.uintptr_t(uintptr(ptr)));
    }

    public static void showWindow(unsafe.Pointer ptr) {
        C.showWindow(C.uintptr_t(uintptr(ptr)));
    }

    public static void styleWindow(unsafe.Pointer ptr) {
        C.styleWindow(C.uintptr_t(uintptr(ptr)));
    }

    public static void runInBackground() {
        var cmd = exec.Command(filepath.Join(updater.BundlePath, "Contents", "MacOS", "Ollama"), "hidden");
        if cmd != null {
        var err = cmd.Run();
        if err != null {
        slog.Error("failed to run Ollama", "bundlePath", updater.BundlePath, "error", err);
        os.Exit(1);
    }
        } else {
        slog.Error("failed to start Ollama in background", "bundlePath", updater.BundlePath);
        os.Exit(1);
    }
    }

    public static void drag(unsafe.Pointer ptr) {
        C.drag(C.uintptr_t(uintptr(ptr)));
    }

    public static void doubleClick(unsafe.Pointer ptr) {
        C.doubleClick(C.uintptr_t(uintptr(ptr)));
    }

    public static void handleConnectURL() {
        handleConnectURLScheme();
    }

    public static boolean checkAndHandleExistingInstance(String _) {
        return false;
    }
}
