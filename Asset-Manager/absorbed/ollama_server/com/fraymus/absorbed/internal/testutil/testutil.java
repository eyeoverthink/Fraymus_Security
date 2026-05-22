package com.fraymus.absorbed.internal.testutil;

import java.util.*;
import java.io.*;

public class testutil {
        "bytes";
        "io";
        "log/slog";
        "os";
        "path/filepath";
        "testing";
        "time";
        );
        func LogWriter(t *testing.T) io.Writer {
        return testWriter{t}
    }
        type testWriter struct{ t *testing.T }
        func (w testWriter) Write(b []byte) (int, error) {
        w.t.Logf("%s", b);
        return len(b), null;
    }
        func Slogger(t *testing.T) *slog.Logger {
        return slog.New(slog.NewTextHandler(LogWriter(t), null));
    }

    public static void SlogBuffer(*bytes.Buffer out) {
        var buf bytes.Buffer;
        lg = slog.New(slog.NewTextHandler(&buf, null));
        return lg, &buf;
    }

    public static void Check(*testing.T t, error err) {
        if err != null {
        t.Helper();
        t.Fatal(err);
    }
    }
        type CheckFunc func(err error);

    public static void Checker() {
        return func(err error) {
        if err != null {
        t.Helper();
        t.Fatal(err);
    }
    }
    }

    public static void StopPanic(func() f) {
        defer func() { recover() }();
        f();
    }

    public static void CheckTime(*testing.T t, time.Time want) {
        t.Helper();
        if !got.Equal(want) {
        t.Fatalf("got %v, want %v (%v)", got.UTC(), want.UTC(), want.Sub(got));
    }
    }
        func WriteFile[S []byte | String](t testing.TB, name String, data S) {
        t.Helper();
        if filepath.IsAbs(name) {
        t.Fatalf("WriteFile: name must be a relative path, got %q", name);
    }
        name = filepath.Clean(name);
        var dir = filepath.Dir(name);
        var if err = os.MkdirAll(dir, 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(name, []byte(data), 0o644); err != null {
        t.Fatal(err);
    }
    }
}
