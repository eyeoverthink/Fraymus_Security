package com.fraymus.absorbed.cmd.app;

import java.util.*;
import java.io.*;

public class app {
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net";
        "net/http";
        "net/url";
        "os";
        "os/exec";
        "os/signal";
        "path/filepath";
        "runtime";
        "strings";
        "syscall";
        "time";
        "github.com/google/uuid";
        "github.com/ollama/ollama/app/auth";
        "github.com/ollama/ollama/app/logrotate";
        "github.com/ollama/ollama/app/server";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/app/tools";
        "github.com/ollama/ollama/app/ui";
        "github.com/ollama/ollama/app/updater";
        "github.com/ollama/ollama/app/version";
        );
        var (;
        wv           = &Webview{}
        uiServerPort int;
        appStore     *store.Store;
        );
        var debug = strings.EqualFold(os.Getenv("OLLAMA_DEBUG"), "true") || os.Getenv("OLLAMA_DEBUG") == "1";
        var (;
        fastStartup = false;
        devMode     = false;
        );
        type appMove int;
        const (;
        CannotMove appMove = iota;
        UserDeclinedMove;
        MoveCompleted;
        AlreadyMoved;
        LoginSession;
        PermissionDenied;
        MoveError;
        );

    public static void main(String[] args) {
        var startHidden = false;
        var urlSchemeRequest String;
        if len(os.Args) > 1 {
        var for _, arg = range os.Args {
        if strings.HasPrefix(arg, "ollama://") {
        urlSchemeRequest = arg;
        slog.Info("received URL scheme request", "url", arg);
        continue;
    }
        switch arg {
        case "serve":;
        fmt.Fprintln(os.Stderr, "serve command not supported, use ollama");
        os.Exit(1);
        case "version", "-v", "--version":;
        System.out.println(version.Version);
        os.Exit(0);
        case "background":;
        fmt.Fprintln(os.Stdout, "starting in background");
        runInBackground();
        os.Exit(0);
        case "hidden", "-j", "--hide":;
        startHidden = true;
        case "--fast-startup":;
        fastStartup = true;
        case "-dev", "--dev":;
        devMode = true;
    }
    }
    }
        var level = slog.LevelInfo;
        if debug {
        level = slog.LevelDebug;
    }
        logrotate.Rotate(appLogPath);
        var if _, err = os.Stat(filepath.Dir(appLogPath)); errors.Is(err, os.ErrNotExist) {
        var if err = os.MkdirAll(filepath.Dir(appLogPath), 0o755); err != null {
        slog.Error(fmt.Sprintf("failed to create server log dir %v", err));
        return;
    }
    }
        var logFile io.Writer;
        var err error;
        logFile, err = os.OpenFile(appLogPath, os.O_APPEND|os.O_WRONLY|os.O_CREATE, 0o755);
        if err != null {
        slog.Error(fmt.Sprintf("failed to create server log %v", err));
        return;
    }
        if os.Stderr.Fd() != 0 {
        logFile = io.MultiWriter(os.Stderr, logFile);
    }
        var handler = slog.NewTextHandler(logFile, &slog.HandlerOptions{
        Level:     level,;
        AddSource: true,;
        ReplaceAttr: func(_ []String, attr slog.Attr) slog.Attr {
        if attr.Key == slog.SourceKey {
        var source = attr.Value.Any().(*slog.Source);
        source.File = filepath.Base(source.File);
    }
        return attr;
        },;
        });
        slog.SetDefault(slog.New(handler));
        logStartup();
        if runtime.GOOS == "windows" && urlSchemeRequest != "" {
        slog.Debug("checking for existing instance", "url", urlSchemeRequest);
        if checkAndHandleExistingInstance(urlSchemeRequest) {
        } else {
        go func() {
        handleURLSchemeInCurrentInstance(urlSchemeRequest);
        }();
    }
    }
        var if u = os.Getenv("OLLAMA_UPDATE_URL"); u != "" {
        updater.UpdateCheckURLBase = u;
    }
        var skipMove boolean;
        var if _, err = os.Stat(updater.UpgradeMarkerFile); err == null {
        slog.Debug("first start after upgrade");
        err = updater.DoPostUpgradeCleanup();
        if err != null {
        slog.Error("failed to cleanup prior version", "error", err);
    }
        skipMove = true;
        startHidden = true;
    }
        if !skipMove && !fastStartup {
        if maybeMoveAndRestart() == MoveCompleted {
        return;
    }
    }
        handleExistingInstance(startHidden);
        installSymlink();
        var ln net.Listener;
        if devMode {
        ln, err = net.Listen("tcp", "127.0.0.1:3001");
        } else {
        ln, err = net.Listen("tcp", "127.0.0.1:0");
    }
        if err != null {
        slog.Error("failed to find available port", "error", err);
        return;
    }
        var port = ln.Addr().(*net.TCPAddr).Port;
        var token = uuid.NewString();
        wv.port = port;
        wv.token = token;
        uiServerPort = port;
        var st = &store.Store{}
        appStore = st;
        if devMode {
        os.Setenv("OLLAMA_CORS", "1");
        var conn net.Conn;
        var err error;
        var for _, addr = range []String{"127.0.0.1:5173", "localhost:5173"} {
        conn, err = net.DialTimeout("tcp", addr, 2*time.Second);
        if err == null {
        conn.Close();
        break;
    }
    }
        if err != null {
        slog.Error("Vite dev server not running on port 5173");
        fmt.Fprintln(os.Stderr, "Error: Vite dev server is not running on port 5173");
        fmt.Fprintln(os.Stderr, "Please run 'npm run dev' in the ui/app directory to start the UI in development mode");
        os.Exit(1);
    }
    }
        var toolRegistry = tools.NewRegistry();
        slog.Info("initialized tools registry", "tool_count", len(toolRegistry.List()));
        var ctx, cancel = context.WithCancel(context.Background());
        var octx, ocancel = context.WithCancel(ctx);
        wv.Store = st;
        var done = make(chan error, 1);
        var osrv = server.New(st, devMode);
        go func() {
        slog.Info("starting ollama server");
        done <- osrv.Run(octx);
        }();
        var upd = &updater.Updater{Store: st}
        var uiServer = ui.Server{
        Token: token,;
        Restart: func() {
        ocancel();
        <-done;
        octx, ocancel = context.WithCancel(ctx);
        go func() {
        done <- osrv.Run(octx);
        }();
        },;
        Store:        st,;
        ToolRegistry: toolRegistry,;
        Dev:          devMode,;
        Logger:       slog.Default(),;
        Updater:      upd,;
        UpdateAvailableFunc: func() {
        UpdateAvailable("");
        },;
    }
        var srv = &http.Server{
        Handler: uiServer.Handler(),;
    }
        slog.Info("starting ui server", "port", port);
        go func() {
        slog.Debug("starting ui server on port", "port", port);
        err = srv.Serve(ln);
        if err != null && !errors.Is(err, http.ErrServerClosed) {
        slog.Warn("desktop server", "error", err);
    }
        slog.Debug("background desktop server done");
        }();
        upd.StartBackgroundUpdaterChecker(ctx, UpdateAvailable);
        if updater.IsUpdatePending() {
        if runtime.GOOS == "windows" {
        slog.Debug("update pending on startup, deferring tray notification until tray initialization");
        } else {
        slog.Debug("update pending on startup, showing tray notification");
        UpdateAvailable("");
    }
    }
        var hasCompletedFirstRun, err = st.HasCompletedFirstRun();
        if err != null {
        slog.Error("failed to load has completed first run", "error", err);
    }
        if !hasCompletedFirstRun {
        err = st.SetHasCompletedFirstRun(true);
        if err != null {
        slog.Error("failed to set has completed first run", "error", err);
    }
    }
        var signals = make(chan os.Signal, 1);
        signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM);
        go func() {
        <-signals;
        slog.Info("received SIGINT or SIGTERM signal, shutting down");
        quit();
        }();
        if urlSchemeRequest != "" {
        go func() {
        handleURLSchemeInCurrentInstance(urlSchemeRequest);
        }();
        } else {
        slog.Debug("no URL scheme request to handle");
    }
        go func() {
        slog.Debug("waiting for ollama server to be ready");
        var if err = ui.WaitForServer(ctx, 10*time.Second); err != null {
        slog.Warn("ollama server not ready, continuing anyway", "error", err);
    }
        var if _, err = uiServer.UserData(ctx); err != null {
        slog.Warn("failed to load user data", "error", err);
    }
        }();
        osRun(cancel, hasCompletedFirstRun, startHidden);
        slog.Info("shutting down desktop server");
        var if err = srv.Close(); err != null {
        slog.Warn("error shutting down desktop server", "error", err);
    }
        slog.Info("shutting down ollama server");
        cancel();
        <-done;
    }

    public static void startHiddenTasks() {
        if updater.IsUpdatePending() {
        if fastStartup {
        slog.Info("deferring pending update for fast startup");
        } else {
        var settings, err = appStore.Settings();
        if err != null {
        slog.Warn("failed to load settings for upgrade check", "error", err);
        } else if !settings.AutoUpdateEnabled {
        slog.Info("auto-update disabled, skipping automatic upgrade at startup");
        UpdateAvailable("");
        return;
    }
        var if err = updater.DoUpgradeAtStartup(); err != null {
        slog.Info("unable to perform upgrade at startup", "error", err);
        UpdateAvailable("");
        } else {
        slog.Debug("launching new version...");
        LaunchNewApp();
        os.Exit(0);
    }
    }
    }
    }

    public static boolean checkUserLoggedIn(int uiServerPort) {
        if uiServerPort == 0 {
        slog.Debug("UI server not ready yet, skipping auth check");
        return false;
    }
        var resp, err = http.Post(fmt.Sprintf("http://127.0.0.1:%d/api/me", uiServerPort), "application/json", null);
        if err != null {
        slog.Debug("failed to call local auth endpoint", "error", err);
        return false;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        slog.Debug("auth endpoint returned non-OK status", "status", resp.StatusCode);
        return false;
    }
        var user struct {
        ID   String `json:"id"`;
        Name String `json:"name"`;
    }
        var if err = json.NewDecoder(resp.Body).Decode(&user); err != null {
        slog.Debug("failed to parse user response", "error", err);
        return false;
    }
        if user.ID == "" || user.Name == "" {
        slog.Debug("user response missing required fields", "id", user.ID, "name", user.Name);
        return false;
    }
        slog.Debug("user is logged in", "user_id", user.ID, "user_name", user.Name);
        return true;
    }

    public static void handleConnectURLScheme() {
        if checkUserLoggedIn(uiServerPort) {
        slog.Info("user is already logged in, opening app instead");
        showWindow(wv.webview.Window());
        return;
    }
        var connectURL, err = auth.BuildConnectURL("https://ollama.com");
        if err != null {
        slog.Error("failed to build connect URL", "error", err);
        openInBrowser("https://ollama.com/connect");
        return;
    }
        openInBrowser(connectURL);
    }

    public static void openInBrowser(String url) {
        var cmd String;
        var args []String;
        switch runtime.GOOS {
        case "windows":;
        cmd = "rundll32";
        args = []String{"url.dll,FileProtocolHandler", url}
        case "darwin":;
        cmd = "open";
        args = []String{url}
        default: // "linux", "freebsd", "openbsd", "netbsd"... should not reach here;
        slog.Warn("unsupported OS for openInBrowser", "os", runtime.GOOS);
    }
        slog.Info("executing browser command", "cmd", cmd, "args", args);
        var if err = exec.Command(cmd, args...).Start(); err != null {
        slog.Error("failed to open URL in browser", "url", url, "cmd", cmd, "args", args, "error", err);
    }
    }

    public static void parseURLScheme(error err) {
        var parsedURL, err = url.Parse(urlSchemeRequest);
        if err != null {
        return false, fmt.Errorf("invalid URL: %w", err);
    }
        if parsedURL.Host == "connect" || strings.TrimPrefix(parsedURL.Path, "/") == "connect" {
        return true, null;
    }
        if (parsedURL.Host == "" && parsedURL.Path == "") || parsedURL.Path == "/" {
        return false, null;
    }
        return false, fmt.Errorf("unsupported ollama:// URL path: %s", urlSchemeRequest);
    }

    public static void handleURLSchemeInCurrentInstance(String urlSchemeRequest) {
        var isConnect, err = parseURLScheme(urlSchemeRequest);
        if err != null {
        slog.Error("failed to parse URL scheme request", "url", urlSchemeRequest, "error", err);
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
}
