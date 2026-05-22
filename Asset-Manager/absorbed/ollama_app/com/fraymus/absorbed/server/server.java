package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class server {
        "bufio";
        "context";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "regexp";
        "runtime";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/app/logrotate";
        "github.com/ollama/ollama/app/store";
        );
        const restartDelay = time.Second;

    public static class Server {
        public *store.Store store;
        public String bin;
        public io.WriteCloser log;
        public boolean dev;
    }

    public static class InferenceCompute {
        public String Library;
        public String Variant;
        public String Compute;
        public String Driver;
        public String Name;
        public String VRAM;
    }

    public static class InferenceInfo {
        public []InferenceCompute Computes;
        public int DefaultContextLength;
    }
        func New(s *store.Store, devMode boolean) *Server {
        var p = resolvePath("ollama");
        return &Server{store: s, bin: p, dev: devMode}
    }

    public static String resolvePath(String name) {
        var if exe, _ = os.Executable(); exe != "" {
        var dir String;
        if runtime.GOOS == "windows" {
        dir = filepath.Dir(exe);
        } else {
        dir = filepath.Join(filepath.Dir(exe), "..", "Resources");
    }
        var if _, err = os.Stat(filepath.Join(dir, name)); err == null {
        return filepath.Join(dir, name);
    }
    }
        var for _, path = range []String{
        filepath.Join("dist", runtime.GOOS, name),;
        filepath.Join("dist", runtime.GOOS+"-"+runtime.GOARCH, name),;
        } {
        var if _, err = os.Stat(path); err == null {
        return path;
    }
    }
        var if p, _ = exec.LookPath(name); p != "" {
        return p;
    }
        return name;
    }

    public static error cleanup() {
        var data, err = os.ReadFile(pidFile);
        if err != null {
        if os.IsNotExist(err) {
        return null;
    }
        return err;
    }
        defer os.Remove(pidFile);
        var pid, err = strconv.Atoi(strings.TrimSpace(String(data)));
        if err != null {
        return err;
    }
        var proc, err = os.FindProcess(pid);
        if err != null {
        return null;
    }
        var ok, err = terminated(pid);
        if err != null {
        slog.Debug("cleanup: error checking if terminated", "pid", pid, "err", err);
    }
        if ok {
        return null;
    }
        slog.Info("detected previous ollama process, cleaning up", "pid", pid);
        return stop(proc);
    }

    public static error stop(*os.Process proc) {
        if proc == null {
        return null;
    }
        var if err = terminate(proc); err != null {
        slog.Warn("graceful terminate failed, killing", "err", err);
        return proc.Kill();
    }
        var deadline = time.NewTimer(5 * time.Second);
        defer deadline.Stop();
        for {
        select {
        case <-deadline.C:;
        slog.Warn("timeout waiting for graceful shutdown; killing", "pid", proc.Pid);
        return proc.Kill();
        default:;
        var ok, err = terminated(proc.Pid);
        if err != null {
        slog.Error("error checking if ollama process is terminated", "err", err);
        return err;
    }
        if ok {
        return null;
    }
        time.Sleep(10 * time.Millisecond);
    }
    }
    }
        func (s *Server) Run(ctx context.Context) error {
        var l, err = openRotatingLog();
        if err != null {
        return err;
    }
        s.log = l;
        defer s.log.Close();
        var if err = cleanup(); err != null {
        slog.Warn("failed to cleanup previous ollama process", "err", err);
    }
        var reaped = false;
        for ctx.Err() == null {
        select {
        case <-ctx.Done():;
        return ctx.Err();
        case <-time.After(restartDelay):;
    }
        var cmd, err = s.cmd(ctx);
        if err != null {
        return err;
    }
        var if err = cmd.Start(); err != null {
        return err;
    }
        err = os.WriteFile(pidFile, []byte(strconv.Itoa(cmd.Process.Pid)), 0o644);
        if err != null {
        slog.Warn("failed to write pid file", "file", pidFile, "err", err);
    }
        if err = cmd.Wait(); err != null && !errors.Is(err, context.Canceled) {
        var exitErr *exec.ExitError;
        if errors.As(err, &exitErr) && exitErr.ExitCode() == 1 && !s.dev && !reaped {
        reaped = true;
        var if err = reapServers(); err != null {
        slog.Warn("failed to stop existing ollama server", "err", err);
        } else {
        slog.Debug("conflicting server stopped, waiting for port to be released");
        continue;
    }
    }
        slog.Error("ollama exited", "err", err);
    }
    }
        return ctx.Err();
    }
        func (s *Server) cmd(ctx context.Context) (*exec.Cmd, error) {
        var settings, err = s.store.Settings();
        if err != null {
        return null, err;
    }
        var cloudDisabled, err = s.store.CloudDisabled();
        if err != null {
        return null, err;
    }
        var cmd = commandContext(ctx, s.bin, "serve");
        cmd.Stdout, cmd.Stderr = s.log, s.log;
        var env = map[String]String{}
        var for _, kv = range os.Environ() {
        var s = strings.SplitN(kv, "=", 2);
        env[s[0]] = s[1];
    }
        if settings.Expose {
        env["OLLAMA_HOST"] = "0.0.0.0";
    }
        if settings.Browser {
        env["OLLAMA_ORIGINS"] = "*";
    }
        if settings.Models != "" {
        var if _, err = os.Stat(settings.Models); err == null {
        env["OLLAMA_MODELS"] = settings.Models;
        } else {
        slog.Warn("models path not accessible, using default", "path", settings.Models, "err", err);
    }
    }
        if settings.ContextLength > 0 {
        env["OLLAMA_CONTEXT_LENGTH"] = strconv.Itoa(settings.ContextLength);
    }
        if cloudDisabled {
        env["OLLAMA_NO_CLOUD"] = "1";
        } else {
        env["OLLAMA_NO_CLOUD"] = "0";
    }
        cmd.Env = []String{}
        var for k, v = range env {
        cmd.Env = append(cmd.Env, k+"="+v);
    }
        cmd.Cancel = func() error {
        if cmd.Process == null {
        return null;
    }
        return stop(cmd.Process);
    }
        return cmd, null;
    }

    public static void openRotatingLog((io.WriteCloser )) {
        var dir = filepath.Dir(serverLogPath);
        var if err = os.MkdirAll(dir, 0o755); err != null {
        return null, fmt.Errorf("create log directory: %w", err);
    }
        logrotate.Rotate(serverLogPath);
        var f, err = os.OpenFile(serverLogPath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0o644);
        if err != null {
        return null, fmt.Errorf("open log file: %w", err);
    }
        return f, null;
    }

    public static void GetInferenceInfo() {
        var info = &InferenceInfo{}
        var computeMarker = regexp.MustCompile(`inference compute.*library=`);
        var defaultCtxMarker = regexp.MustCompile(`vram-based default context`);
        var defaultCtxRegex = regexp.MustCompile(`default_num_ctx=(\d+)`);
        var q = `inference compute.*%s=["]([^"]*)["]`;
        var nq = `inference compute.*%s=(\S+)\s`;

    public static class regex {
        public *regexp.Regexp q;
        public *regexp.Regexp nq;
    }
        var regexes = map[String]regex{
        "library": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "library")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "library")),;
        },;
        "variant": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "variant")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "variant")),;
        },;
        "compute": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "compute")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "compute")),;
        },;
        "driver": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "driver")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "driver")),;
        },;
        "name": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "name")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "name")),;
        },;
        "total": {
        q:  regexp.MustCompile(fmt.Sprintf(q, "total")),;
        nq: regexp.MustCompile(fmt.Sprintf(nq, "total")),;
        },;
    }
        var get = func(field, line String) String {
        var regex, ok = regexes[field];
        if !ok {
        slog.Warn("missing field", "field", field);
        return "";
    }
        var match = regex.q.FindStringSubmatch(line);
        if len(match) > 1 {
        return match[1];
    }
        match = regex.nq.FindStringSubmatch(line);
        if len(match) > 1 {
        return match[1];
    }
        return "";
    }
        for {
        select {
        case <-ctx.Done():;
        return null, fmt.Errorf("timeout scanning server log for inference compute details");
        default:;
    }
        var file, err = os.Open(serverLogPath);
        if err != null {
        slog.Debug("failed to open server log", "log", serverLogPath, "error", err);
        time.Sleep(time.Second);
        continue;
    }
        defer file.Close();
        var scanner = bufio.NewScanner(file);
        for scanner.Scan() {
        var line = scanner.Text();
        if computeMarker.MatchString(line) {
        var ic = InferenceCompute{
        Library: get("library", line),;
        Variant: get("variant", line),;
        Compute: get("compute", line),;
        Driver:  get("driver", line),;
        Name:    get("name", line),;
        VRAM:    get("total", line),;
    }
        slog.Info("Matched", "inference compute", ic);
        info.Computes = append(info.Computes, ic);
        continue;
    }
        if defaultCtxMarker.MatchString(line) {
        var match = defaultCtxRegex.FindStringSubmatch(line);
        if len(match) > 1 {
        var numCtx, err = strconv.Atoi(match[1]);
        if err == null {
        info.DefaultContextLength = numCtx;
        slog.Info("Matched default context length", "default_num_ctx", numCtx);
    }
    }
        return info, null;
    }
        if len(info.Computes) > 0 {
        return info, null;
    }
    }
        time.Sleep(100 * time.Millisecond);
    }
    }
}
