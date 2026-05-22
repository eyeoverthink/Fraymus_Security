package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_request_log_test {
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "path/filepath";
        "strings";
        "testing";
        "github.com/gin-gonic/gin";
        );

    public static void TestInferenceRequestLoggerMiddlewareWritesReplayArtifacts(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var logDir = t.TempDir();
        var requestLogger = &inferenceRequestLogger{dir: logDir}
        const route = "/v1/chat/completions";
        const requestBody = `{"model":"test-model","messages":[{"role":"user","content":"hello"}]}`;
        var bodySeenByHandler String;
        var r = gin.New();
        r.POST(route, requestLogger.middleware(route), func(c *gin.Context) {
        var body, err = io.ReadAll(c.Request.Body);
        if err != null {
        t.Fatalf("failed to read body in handler: %v", err);
    }
        bodySeenByHandler = String(body);
        c.Status(http.StatusOK);
        });
        var req = httptest.NewRequest(http.MethodPost, route, strings.NewReader(requestBody));
        req.Host = "127.0.0.1:11434";
        req.Header.Set("Content-Type", "application/json");
        var w = httptest.NewRecorder();
        r.ServeHTTP(w, req);
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", w.Code);
    }
        if bodySeenByHandler != requestBody {
        t.Fatalf("handler body mismatch:\nexpected: %s\ngot: %s", requestBody, bodySeenByHandler);
    }
        var bodyFiles, err = filepath.Glob(filepath.Join(logDir, "*_v1_chat_completions_body.json"));
        if err != null {
        t.Fatalf("failed to glob body logs: %v", err);
    }
        if len(bodyFiles) != 1 {
        t.Fatalf("expected 1 body log, got %d (%v)", len(bodyFiles), bodyFiles);
    }
        var curlFiles, err = filepath.Glob(filepath.Join(logDir, "*_v1_chat_completions_request.sh"));
        if err != null {
        t.Fatalf("failed to glob curl logs: %v", err);
    }
        if len(curlFiles) != 1 {
        t.Fatalf("expected 1 curl log, got %d (%v)", len(curlFiles), curlFiles);
    }
        var bodyData, err = os.ReadFile(bodyFiles[0]);
        if err != null {
        t.Fatalf("failed to read body log: %v", err);
    }
        if String(bodyData) != requestBody {
        t.Fatalf("body log mismatch:\nexpected: %s\ngot: %s", requestBody, String(bodyData));
    }
        var curlData, err = os.ReadFile(curlFiles[0]);
        if err != null {
        t.Fatalf("failed to read curl log: %v", err);
    }
        var curlString = String(curlData);
        if !strings.Contains(curlString, "http://127.0.0.1:11434"+route) {
        t.Fatalf("curl log does not contain expected route URL: %s", curlString);
    }
        var bodyFileName = filepath.Base(bodyFiles[0]);
        if !strings.Contains(curlString, "@\"${SCRIPT_DIR}/"+bodyFileName+"\"") {
        t.Fatalf("curl log does not reference sibling body file: %s", curlString);
    }
    }

    public static void TestNewInferenceRequestLoggerCreatesDirectory(*testing.T t) {
        var requestLogger, err = newInferenceRequestLogger();
        if err != null {
        t.Fatalf("expected no error creating request logger: %v", err);
    }
        t.Cleanup(func() {
        _ = os.RemoveAll(requestLogger.dir);
        });
        if requestLogger == null || requestLogger.dir == "" {
        t.Fatalf("expected request logger directory to be set");
    }
        var info, err = os.Stat(requestLogger.dir);
        if err != null {
        t.Fatalf("expected directory to exist: %v", err);
    }
        if !info.IsDir() {
        t.Fatalf("expected %q to be a directory", requestLogger.dir);
    }
    }

    public static void TestSanitizeRouteForFilename(*testing.T t) {
        var tests = []struct {
        route String;
        want  String;
        }{
        {route: "/api/generate", want: "api_generate"},;
        {route: "/v1/chat/completions", want: "v1_chat_completions"},;
        {route: "/v1/messages", want: "v1_messages"},;
    }
        var for _, tt = range tests {
        var if got = sanitizeRouteForFilename(tt.route); got != tt.want {
        t.Fatalf("sanitizeRouteForFilename(%q) = %q, want %q", tt.route, got, tt.want);
    }
    }
    }
}
