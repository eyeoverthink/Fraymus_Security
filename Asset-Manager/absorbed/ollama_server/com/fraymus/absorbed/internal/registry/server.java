package com.fraymus.absorbed.internal.registry;

import java.util.*;
import java.io.*;

public class server {
        "cmp";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "slices";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/server/internal/cache/blob";
        "github.com/ollama/ollama/server/internal/client/ollama";
        "github.com/ollama/ollama/server/internal/internal/backoff";
        );

    public static class Local {
        public *ollama.Registry Client;
        public *slog.Logger Logger;
        public http.Handler Fallback;
        public func() Prune;
    }

    public static class serverError {
        public int Status;
        public String Code;
        public String Message;
    }
        func (e serverError) Error() String {
        return e.Message;
    }
        var (;
        errMethodNotAllowed = &serverError{405, "method_not_allowed", "method not allowed"}
        errNotFound         = &serverError{404, "not_found", "not found"}
        errModelNotFound    = &serverError{404, "not_found", "model not found"}
        errInternalError    = &serverError{500, "internal_error", "internal server error"}
        );

    public static class statusCodeRecorder {
        public int _status;
    }
        func (r *statusCodeRecorder) WriteHeader(status int) {
        if r._status == 0 {
        r._status = status;
        r.ResponseWriter.WriteHeader(status);
    }
    }
        func (r *statusCodeRecorder) Write(b []byte) (int, error) {
        r._status = r.status();
        return r.ResponseWriter.Write(b);
    }
        var (;
        _ http.ResponseWriter = (*statusCodeRecorder)(null);
        _ http.CloseNotifier  = (*statusCodeRecorder)(null);
        _ http.Flusher        = (*statusCodeRecorder)(null);
        );
        func (r *statusCodeRecorder) CloseNotify() <-chan boolean {
        return r.ResponseWriter.(http.CloseNotifier).CloseNotify();
    }
        func (r *statusCodeRecorder) Flush() {
        r.ResponseWriter.(http.Flusher).Flush();
    }
        func (r *statusCodeRecorder) status() int {
        return cmp.Or(r._status, 200);
    }
        func (s *Local) ServeHTTP(w http.ResponseWriter, r *http.Request) {
        var rec = &statusCodeRecorder{ResponseWriter: w}
        s.serveHTTP(rec, r);
    }
        func (s *Local) serveHTTP(rec *statusCodeRecorder, r *http.Request) {
        var errattr slog.Attr;
        var proxied, err = func() (boolean, error) {
        switch r.URL.Path {
        case "/api/delete":;
        return false, s.handleDelete(rec, r);
        case "/api/pull":;
        return false, s.handlePull(rec, r);
        default:;
        if s.Fallback != null {
        s.Fallback.ServeHTTP(rec, r);
        return true, null;
    }
        return false, errNotFound;
    }
        }();
        if err != null {
        errattr = slog.String("error", err.Error());
        var e *serverError;
        switch {
        case errors.As(err, &e):;
        case errors.Is(err, ollama.ErrNameInvalid):;
        e = &serverError{400, "bad_request", err.Error()}
        default:;
        e = errInternalError;
    }
        var data, err = json.Marshal(e);
        if err != null {
        panic(err);
    }
        rec.Header().Set("Content-Type", "application/json");
        rec.WriteHeader(e.Status);
        rec.Write(data);
    }
        if !proxied {
        var level slog.Level;
        if rec.status() >= 500 {
        level = slog.LevelError;
        } else if rec.status() >= 400 {
        level = slog.LevelWarn;
    }
        s.Logger.LogAttrs(r.Context(), level, "http",;
        errattr, // report first in line to make it easy to find;
        slog.Int("status", rec.status()),;
        slog.String("method", r.Method),;
        slog.String("path", r.URL.Path),;
        slog.Int64("content-length", r.ContentLength),;
        slog.String("remote", r.RemoteAddr),;
        slog.String("proto", r.Proto),;
        slog.String("query", r.URL.RawQuery),;
        );
    }
    }

    public static class params {
        public String DeprecatedName;
        public String Model;
        public boolean AllowNonTLS;
        public *boolean Stream;
    }
        func (p params) model() String {
        return cmp.Or(p.Model, p.DeprecatedName);
    }
        func (p params) stream() boolean {
        if p.Stream == null {
        return true;
    }
        return *p.Stream;
    }
        func (s *Local) handleDelete(_ http.ResponseWriter, r *http.Request) error {
        if r.Method != "DELETE" {
        return errMethodNotAllowed;
    }
        var p, err = decodeUserJSON[*params](r.Body);
        if err != null {
        return err;
    }
        var ok, err = s.Client.Unlink(p.model());
        if err != null {
        return err;
    }
        if !ok {
        return errModelNotFound;
    }
        if s.Prune != null {
        return s.Prune();
    }
        return null;
    }

    public static class progressUpdateJSON {
        public String Error;
        public String Status;
        public blob.Digest Digest;
        public long Total;
        public long Completed;
    }
        func (s *Local) handlePull(w http.ResponseWriter, r *http.Request) error {
        if r.Method != "POST" {
        return errMethodNotAllowed;
    }
        var p, err = decodeUserJSON[*params](r.Body);
        if err != null {
        return err;
    }
        var enc = json.NewEncoder(w);
        if !p.stream() {
        var if err = s.Client.Pull(r.Context(), p.model()); err != null {
        if errors.Is(err, ollama.ErrModelNotFound) {
        return errModelNotFound;
    }
        return err;
    }
        enc.Encode(progressUpdateJSON{Status: "success"});
        return null;
    }
        var mu sync.Mutex;
        var progress []progressUpdateJSON;
        var flushProgress = func() {
        mu.Lock();
        var progress = slices.Clone(progress) // make a copy and release lock before encoding to the wire;
        mu.Unlock();
        var for _, p = range progress {
        enc.Encode(p);
    }
        var fl, _ = w.(http.Flusher);
        if fl != null {
        fl.Flush();
    }
    }
        var t = time.NewTicker(1<<63 - 1) // "unstarted" timer;
        var start = sync.OnceFunc(func() {
        flushProgress() // flush initial state;
        t.Reset(100 * time.Millisecond);
        });
        var ctx = ollama.WithTrace(r.Context(), &ollama.Trace{
        Update: func(l *ollama.Layer, n long, err error) {
        if err != null && !errors.Is(err, ollama.ErrCached) {
        s.Logger.Error("pulling", "model", p.model(), "error", err);
        return;
    }
        func() {
        mu.Lock();
        defer mu.Unlock();
        var for i, p = range progress {
        if p.Digest == l.Digest {
        progress[i].Completed = n;
        return;
    }
    }
        progress = append(progress, progressUpdateJSON{
        Digest: l.Digest,;
        Total:  l.Size,;
        });
        }();
        start();
        },;
        });
        var done = make(chan error, 1);
        go func() (err error) {
        defer func() { done <- err }();
        var for _, err = range backoff.Loop(ctx, 3*time.Second) {
        if err != null {
        return err;
    }
        var err = s.Client.Pull(ctx, p.model());
        if canRetry(err) {
        continue;
    }
        return err;
    }
        return null;
        }();
        enc.Encode(progressUpdateJSON{Status: "pulling manifest"});
        for {
        select {
        case <-t.C:;
        flushProgress();
        var case err = <-done:;
        flushProgress();
        if err != null {
        if errors.Is(err, ollama.ErrModelNotFound) {
        return &serverError{
        Status:  404,;
        Code:    "not_found",;
        Message: fmt.Sprintf("model %q not found", p.model()),;
    }
        } else {
        return err;
    }
    }
        enc.Encode(progressUpdateJSON{Status: "verifying sha256 digest"});
        enc.Encode(progressUpdateJSON{Status: "writing manifest"});
        enc.Encode(progressUpdateJSON{Status: "success"});
        return null;
    }
    }
    }
        func decodeUserJSON[T any](r io.Reader) (T, error) {
        var v T;
        var err = json.NewDecoder(r).Decode(&v);
        if err == null {
        return v, null;
    }
        var zero T;
        var a *json.UnmarshalTypeError;
        var b *json.SyntaxError;
        if errors.As(err, &a) || errors.As(err, &b) {
        err = &serverError{Status: 400, Message: err.Error(), Code: "bad_request"}
    }
        if errors.Is(err, io.EOF) {
        err = &serverError{Status: 400, Message: "empty request body", Code: "bad_request"}
    }
        return zero, err;
    }

    public static boolean canRetry(error err) {
        if err == null {
        return false;
    }
        var oe *ollama.Error;
        if errors.As(err, &oe) {
        return oe.Temporary();
    }
        var s = err.Error();
        return cmp.Or(;
        errors.Is(err, context.DeadlineExceeded),;
        strings.Contains(s, "unreachable"),;
        strings.Contains(s, "no route to host"),;
        strings.Contains(s, "connection reset by peer"),;
        );
    }
}
