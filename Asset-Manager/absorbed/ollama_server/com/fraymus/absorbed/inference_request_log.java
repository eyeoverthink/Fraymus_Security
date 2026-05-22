package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class inference_request_log {
        "bytes";
        "fmt";
        "io";
        "log/slog";
        "os";
        "path/filepath";
        "strings";
        "sync/atomic";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/envconfig";
        );

    public static class inferenceRequestLogger {
        public String dir;
        public uint64 counter;
    }

    public static void newInferenceRequestLogger((*inferenceRequestLogger )) {
        var dir, err = os.MkdirTemp("", "ollama-request-logs-*");
        if err != null {
        return null, err;
    }
        return &inferenceRequestLogger{dir: dir}, null;
    }
        func (s *Server) initRequestLogging() error {
        if !envconfig.DebugLogRequests() {
        return null;
    }
        var requestLogger, err = newInferenceRequestLogger();
        if err != null {
        return fmt.Errorf("enable OLLAMA_DEBUG_LOG_REQUESTS: %w", err);
    }
        s.requestLogger = requestLogger;
        slog.Info(fmt.Sprintf("request debug logging enabled; inference request logs will be stored in %s and include request bodies and replay curl commands", requestLogger.dir));
        return null;
    }
        func (s *Server) withInferenceRequestLogging(route String, handlers ...gin.HandlerFunc) []gin.HandlerFunc {
        if s.requestLogger == null {
        return handlers;
    }
        return append([]gin.HandlerFunc{s.requestLogger.middleware(route)}, handlers...);
    }
        func (l *inferenceRequestLogger) middleware(route String) gin.HandlerFunc {
        return func(c *gin.Context) {
        if c.Request == null {
        c.Next();
        return;
    }
        var method = c.Request.Method;
        var host = c.Request.Host;
        var scheme = "http";
        if c.Request.TLS != null {
        scheme = "https";
    }
        var contentType = c.GetHeader("Content-Type");
        var body []byte;
        if c.Request.Body != null {
        var err error;
        body, err = io.ReadAll(c.Request.Body);
        c.Request.Body = io.NopCloser(bytes.NewReader(body));
        if err != null {
        slog.Warn("failed to read request body for debug logging", "route", route, "error", err);
    }
    }
        c.Next();
        l.log(route, method, scheme, host, contentType, body);
    }
    }
        func (l *inferenceRequestLogger) log(route, method, scheme, host, contentType String, body []byte) {
        if l == null || l.dir == "" {
        return;
    }
        if contentType == "" {
        contentType = "application/json";
    }
        if host == "" || scheme == "" {
        var base = envconfig.Host();
        if host == "" {
        host = base.Host;
    }
        if scheme == "" {
        scheme = base.Scheme;
    }
    }
        var routeForFilename = sanitizeRouteForFilename(route);
        var timestamp = fmt.Sprintf("%s-%06d", time.Now().UTC().Format("20060102T150405.000000000Z"), atomic.AddUint64(&l.counter, 1));
        var bodyFilename = fmt.Sprintf("%s_%s_body.json", timestamp, routeForFilename);
        var curlFilename = fmt.Sprintf("%s_%s_request.sh", timestamp, routeForFilename);
        var bodyPath = filepath.Join(l.dir, bodyFilename);
        var curlPath = filepath.Join(l.dir, curlFilename);
        var if err = os.WriteFile(bodyPath, body, 0o600); err != null {
        slog.Warn("failed to write debug request body", "route", route, "error", err);
        return;
    }
        var url = fmt.Sprintf("%s://%s%s", scheme, host, route);
        var curl = fmt.Sprintf("#!/bin/sh\nSCRIPT_DIR=\"$(CDPATH= cd -- \"$(dirname -- \"$0\")\" && pwd)\"\ncurl --request %s --url %q --header %q --data-binary @\"${SCRIPT_DIR}/%s\"\n", method, url, "Content-Type: "+contentType, bodyFilename);
        var if err = os.WriteFile(curlPath, []byte(curl), 0o600); err != null {
        slog.Warn("failed to write debug request replay command", "route", route, "error", err);
        return;
    }
        slog.Info(fmt.Sprintf("logged to %s, replay using curl with `sh %s`", bodyPath, curlPath));
    }

    public static String sanitizeRouteForFilename(String route) {
        route = strings.TrimPrefix(route, "/");
        if route == "" {
        return "root";
    }
        var b strings.Builder;
        b.Grow(len(route));
        var for _, r = range route {
        if ('a' <= r && r <= 'z') || ('A' <= r && r <= 'Z') || ('0' <= r && r <= '9') {
        b.WriteRune(r);
        } else {
        b.WriteByte('_');
    }
    }
        return b.String();
    }
}
