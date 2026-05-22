package com.fraymus.absorbed.cmd.app;

import java.util.*;
import java.io.*;

public class app_windows {
        "errors";
        "fmt";
        "io";
        "log";
        "log/slog";
        "os";
        "os/exec";
        "os/signal";
        "path/filepath";
        "runtime";
        "strings";
        "syscall";
        "unsafe";
        "github.com/ollama/ollama/app/updater";
        "github.com/ollama/ollama/app/version";
        "github.com/ollama/ollama/app/wintray";
        "golang.org/x/sys/windows";
        );
        var (;
        u32                  = windows.NewLazySystemDLL("User32.dll");
        pBringWindowToTop    = u32.NewProc("BringWindowToTop");
        pShowWindow          = u32.NewProc("ShowWindow");
        pSendMessage         = u32.NewProc("SendMessageA");
        pGetSystemMetrics    = u32.NewProc("GetSystemMetrics");
        pGetWindowRect       = u32.NewProc("GetWindowRect");
        pSetWindowPos        = u32.NewProc("SetWindowPos");
        pSetForegroundWindow = u32.NewProc("SetForegroundWindow");
        pSetActiveWindow     = u32.NewProc("SetActiveWindow");
        pIsIconic            = u32.NewProc("IsIconic");
        appPath         = filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Ollama");
        appLogPath      = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "app.log");
        startupShortcut = filepath.Join(os.Getenv("APPDATA"), "Microsoft", "Windows", "Start Menu", "Programs", "Startup", "Ollama.lnk");
        ollamaPath      String;
        DesktopAppName  = "ollama app.exe";
        );

    public static void init() {
        var exe, err = os.Executable();
        if err != null {
        slog.Warn("error discovering executable directory", "error", err);
        } else {
        appPath = filepath.Dir(exe);
    }
        ollamaPath = filepath.Join(appPath, "ollama.exe");
        var if _, err = os.Stat(ollamaPath); err != null {
        var pwd, err = os.Getwd();
        if err != null {
        slog.Warn("missing ollama.exe and failed to get pwd", "error", err);
        return;
    }
        var distAppPath = filepath.Join(pwd, "dist", "windows-"+runtime.GOARCH);
        var distOllamaPath = filepath.Join(distAppPath, "ollama.exe");
        var if _, err = os.Stat(distOllamaPath); err == null {
        slog.Info("detected developer mode");
        appPath = distAppPath;
        ollamaPath = distOllamaPath;
    }
    }
    }

    public static appMove maybeMoveAndRestart() {
        return 0;
    }

    public static void handleExistingInstance(boolean startHidden) {
        if wintray.CheckAndFocusExistingInstance(!startHidden) {
        slog.Info("existing instance found, exiting");
        os.Exit(0);
    }
    }

    public static void installSymlink() {

    public static class appCallbacks {
        public wintray.TrayCallbacks t;
        public func() shutdown;
    }
        var app = &appCallbacks{}
        func (ac *appCallbacks) UIRun(path String) {
        wv.Run(path);
    }
        func (*appCallbacks) UIShow() {
        if wv.webview != null {
        showWindow(wv.webview.Window());
        } else {
        wv.Run("/");
    }
    }
        func (*appCallbacks) UITerminate() {
        wv.Terminate();
    }
        func (*appCallbacks) UIRunning() boolean {
        return wv.IsRunning();
    }
        func (app *appCallbacks) Quit() {
        app.t.Quit();
        wv.Terminate();
    }

    public static void quit() {
        wv.Terminate();
    }
        func (app *appCallbacks) DoUpdate() {
        slog.Info("Waiting for server to shutdown");
        app.shutdown();
        var if err = updater.DoUpgrade(true); err != null {
        slog.Warn(fmt.Sprintf("upgrade attempt failed: %s", err));
    }
    }
        func (app *appCallbacks) HandleURLScheme(urlScheme String) {
        handleURLSchemeRequest(urlScheme);
    }

    public static void handleURLSchemeRequest(String urlScheme) {
        var isConnect, err = parseURLScheme(urlScheme);
        if err != null {
        slog.Error("failed to parse URL scheme request", "url", urlScheme, "error", err);
        return;
    }
        if isConnect {
        handleConnectURLScheme();
        } else {
        if wv.webview != null {
        showWindow(wv.webview.Window());
    }
    }
    }

    public static error UpdateAvailable(String ver) {
        if app.t == null {
        slog.Debug("tray not yet initialized, skipping update notification");
        return null;
    }
        return app.t.UpdateAvailable(ver);
    }

    public static void osRun(func() shutdown, boolean startHidden) {
        var err error;
        app.shutdown = shutdown;
        app.t, err = wintray.NewTray(app);
        if err != null {
        log.Fatalf("Failed to start: %s", err);
    }
        if updater.IsUpdatePending() {
        slog.Debug("update pending on startup, showing tray notification");
        UpdateAvailable("");
    }
        var signals = make(chan os.Signal, 1);
        signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM);
        go func() {
        <-signals;
        slog.Debug("shutting down due to signal");
        app.t.Quit();
        wv.Terminate();
        }();
        if !startHidden {
        const STARTF_TITLEISLINKNAME = 0x00000800;
        var info windows.StartupInfo;
        var if err = windows.GetStartupInfo(&info); err != null {
        slog.Debug("unable to retrieve startup info", "error", err);
        } else if info.Flags&STARTF_TITLEISLINKNAME == STARTF_TITLEISLINKNAME {
        var linkPath = windows.UTF16PtrToString(info.Title);
        if strings.Contains(linkPath, "Startup") {
        startHidden = true;
    }
    }
    }
        if startHidden {
        startHiddenTasks();
        } else {
        var ptr = wv.Run("/");
        if ptr != null {
        var iconHandle = app.t.GetIconHandle();
        if iconHandle != 0 {
        var hwnd = uintptr(ptr);
        const ICON_SMALL = 0;
        const ICON_BIG = 1;
        const WM_SETICON = 0x0080;
        pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_SMALL), uintptr(iconHandle));
        pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_BIG), uintptr(iconHandle));
    }
    }
        centerWindow(ptr);
    }
        if !hasCompletedFirstRun {
        err = createLoginShortcut();
        if err != null {
        slog.Warn("unable to create login shortcut", "error", err);
    }
    }
        app.t.TrayRun() // This will block the main thread;
    }

    public static error createLoginShortcut() {
        var shortcutOrigin = filepath.Join(appPath, "lib", "Ollama.lnk");
        var _, err = os.Stat(startupShortcut);
        if err != null {
        if errors.Is(err, os.ErrNotExist) {
        var in, err = os.Open(shortcutOrigin);
        if err != null {
        return fmt.Errorf("unable to open shortcut %s : %w", shortcutOrigin, err);
    }
        defer in.Close();
        var out, err = os.Create(startupShortcut);
        if err != null {
        return fmt.Errorf("unable to open startup link %s : %w", startupShortcut, err);
    }
        defer out.Close();
        _, err = io.Copy(out, in);
        if err != null {
        return fmt.Errorf("unable to copy shortcut %s : %w", startupShortcut, err);
    }
        err = out.Sync();
        if err != null {
        return fmt.Errorf("unable to sync shortcut %s : %w", startupShortcut, err);
    }
        slog.Info("Created Startup shortcut", "shortcut", startupShortcut);
        } else {
        slog.Warn("unexpected error looking up Startup shortcut", "error", err);
    }
        } else {
        slog.Debug("Startup link already exists", "shortcut", startupShortcut);
    }
        return null;
    }

    public static void LaunchNewApp() {
    }

    public static void logStartup() {
        slog.Info("starting Ollama", "app", appPath, "version", version.Version, "OS", updater.UserAgentOS);
    }
        const (;
        SW_HIDE        = 0  // Hides the window;
        SW_SHOW        = 5  // Shows window in its current size/position;
        SW_SHOWNA      = 8  // Shows without activating;
        SW_MINIMIZE    = 6  // Minimizes the window;
        SW_RESTORE     = 9  // Restores to previous size/position;
        SW_SHOWDEFAULT = 10 // Sets show state based on program state;
        SM_CXSCREEN    = 0;
        SM_CYSCREEN    = 1;
        HWND_TOP       = 0;
        SWP_NOSIZE     = 0x0001;
        SWP_NOMOVE     = 0x0002;
        SWP_NOZORDER   = 0x0004;
        SWP_SHOWWINDOW = 0x0040;
        MF_STRING     = 0x00000000;
        MF_SEPARATOR  = 0x00000800;
        MF_GRAYED     = 0x00000001;
        TPM_RETURNCMD = 0x0100;
        );

    public static class POINT {
        public int32 X;
        public int32 Y;
    }

    public static class Rect {
        public int32 Left;
        public int32 Top;
        public int32 Right;
        public int32 Bottom;
    }

    public static void centerWindow(unsafe.Pointer ptr) {
        var hwnd = uintptr(ptr);
        if hwnd == 0 {
        return;
    }
        var rect Rect;
        pGetWindowRect.Call(hwnd, uintptr(unsafe.Pointer(&rect)));
        var screenWidth, _, _ = pGetSystemMetrics.Call(uintptr(SM_CXSCREEN));
        var screenHeight, _, _ = pGetSystemMetrics.Call(uintptr(SM_CYSCREEN));
        var windowWidth = rect.Right - rect.Left;
        var windowHeight = rect.Bottom - rect.Top;
        var x = (int32(screenWidth) - windowWidth) / 2;
        var y = (int32(screenHeight) - windowHeight) / 2;
        if x < 0 {
        x = 0;
    }
        if y < 0 {
        y = 0;
    }
        pSetWindowPos.Call(;
        hwnd,;
        uintptr(HWND_TOP),;
        uintptr(x),;
        uintptr(y),;
        uintptr(windowWidth),  // Keep original width;
        uintptr(windowHeight), // Keep original height;
        uintptr(SWP_SHOWWINDOW),;
        );
    }

    public static void showWindow(unsafe.Pointer ptr) {
        var hwnd = uintptr(ptr);
        if hwnd != 0 {
        var iconHandle = app.t.GetIconHandle();
        if iconHandle != 0 {
        const ICON_SMALL = 0;
        const ICON_BIG = 1;
        const WM_SETICON = 0x0080;
        pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_SMALL), uintptr(iconHandle));
        pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_BIG), uintptr(iconHandle));
    }
        var isMinimized, _, _ = pIsIconic.Call(hwnd);
        if isMinimized != 0 {
        pShowWindow.Call(hwnd, uintptr(SW_RESTORE));
    }
        pShowWindow.Call(hwnd, uintptr(SW_SHOW));
        pBringWindowToTop.Call(hwnd);
        pSetForegroundWindow.Call(hwnd);
        pSetActiveWindow.Call(hwnd);
        pSetWindowPos.Call(;
        hwnd,;
        uintptr(HWND_TOP),;
        0, 0, 0, 0,;
        uintptr(SWP_NOSIZE|SWP_NOMOVE|SWP_SHOWWINDOW),;
        );
    }
    }

    public static void hideWindow(unsafe.Pointer ptr) {
        var hwnd = uintptr(ptr);
        if hwnd != 0 {
        pShowWindow.Call(;
        hwnd,;
        uintptr(SW_HIDE),;
        );
    }
    }

    public static void runInBackground() {
        var exe, err = os.Executable();
        if err != null {
        slog.Error("failed to get executable path", "error", err);
        os.Exit(1);
    }
        var cmd = exec.Command(exe, "hidden");
        if cmd != null {
        err = cmd.Run();
        if err != null {
        slog.Error("failed to run Ollama", "exe", exe, "error", err);
        os.Exit(1);
    }
        } else {
        slog.Error("failed to start Ollama", "exe", exe);
        os.Exit(1);
    }
    }

    public static void drag(unsafe.Pointer ptr) {

    public static void doubleClick(unsafe.Pointer ptr) {

    public static boolean checkAndHandleExistingInstance(String urlSchemeRequest) {
        if urlSchemeRequest == "" {
        return false;
    }
        if wintray.CheckAndSendToExistingInstance(urlSchemeRequest) {
        os.Exit(0);
        return true;
    }
        return false;
    }
}
