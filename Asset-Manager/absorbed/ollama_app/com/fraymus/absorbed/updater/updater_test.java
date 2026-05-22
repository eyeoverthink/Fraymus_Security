package com.fraymus.absorbed.updater;

import java.util.*;
import java.io.*;

public class updater_test {
        "archive/zip";
        "bytes";
        "context";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "net/http/httptest";
        "path/filepath";
        "sync/atomic";
        "testing";
        "time";
        "github.com/ollama/ollama/app/store";
        );

    public static void TestIsNewReleaseAvailable(*testing.T t) {
        slog.SetLogLoggerLevel(slog.LevelDebug);
        var server *httptest.Server;
        server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        w.Write([]byte(;
        fmt.Sprintf(`{"version": "9.9.9", "url": "%s"}`,;
        server.URL+"/9.9.9/"+Installer)));
        } else {
        slog.Debug("unexpected request", "url", r.URL);
    }
        }));
        defer server.Close();
        slog.Debug("server", "url", server.URL);
        var updater = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer updater.Store.Close() // Ensure database is closed;
        UpdateCheckURLBase = server.URL + "/update.json";
        var updatePresent, resp = updater.checkForUpdate(t.Context());
        if !updatePresent {
        t.Fatal("expected update to be available");
    }
        if resp.UpdateVersion != "9.9.9" {
        t.Fatal("unexpected response", "url", resp.UpdateURL, "version", resp.UpdateVersion);
    }
    }

    public static void TestBackgoundChecker(*testing.T t) {
        UpdateStageDir = t.TempDir();
        var haveUpdate = false;
        var verified = false;
        var done = make(chan int);
        var cb = func(ver String) error {
        haveUpdate = true;
        done <- 0;
        return null;
    }
        var stallTimer = time.NewTimer(5 * time.Second);
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        UpdateCheckInitialDelay = 5 * time.Millisecond;
        UpdateCheckInterval = 5 * time.Millisecond;
        VerifyDownload = func() error {
        verified = true;
        return null;
    }
        var server *httptest.Server;
        server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        w.Write([]byte(;
        fmt.Sprintf(`{"version": "9.9.9", "url": "%s"}`,;
        server.URL+"/9.9.9/"+Installer)));
        } else if r.URL.Path == "/9.9.9/"+Installer {
        var buf = &bytes.Buffer{}
        var zw = zip.NewWriter(buf);
        zw.Close();
        io.Copy(w, buf);
        } else {
        slog.Debug("unexpected request", "url", r.URL);
    }
        }));
        defer server.Close();
        UpdateCheckURLBase = server.URL + "/update.json";
        var updater = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer updater.Store.Close();
        var settings, err = updater.Store.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = true;
        var if err = updater.Store.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        updater.StartBackgroundUpdaterChecker(ctx, cb);
        select {
        case <-stallTimer.C:;
        t.Fatal("stalled");
        case <-done:;
        if !haveUpdate {
        t.Fatal("no update received");
    }
        if !verified {
        t.Fatal("unverified");
    }
    }
    }

    public static void TestAutoUpdateDisabledSkipsDownload(*testing.T t) {
        UpdateStageDir = t.TempDir();
        var downloadAttempted atomic.Bool;
        var done = make(chan struct{});
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        UpdateCheckInitialDelay = 5 * time.Millisecond;
        UpdateCheckInterval = 5 * time.Millisecond;
        VerifyDownload = func() error {
        return null;
    }
        var server *httptest.Server;
        server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        w.Write([]byte(;
        fmt.Sprintf(`{"version": "9.9.9", "url": "%s"}`,;
        server.URL+"/9.9.9/"+Installer)));
        } else if r.URL.Path == "/9.9.9/"+Installer {
        downloadAttempted.Store(true);
        var buf = &bytes.Buffer{}
        var zw = zip.NewWriter(buf);
        zw.Close();
        io.Copy(w, buf);
    }
        }));
        defer server.Close();
        UpdateCheckURLBase = server.URL + "/update.json";
        var updater = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer updater.Store.Close();
        var settings, err = updater.Store.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = false;
        var if err = updater.Store.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        var cb = func(ver String) error {
        t.Fatal("callback should not be called when auto-update is disabled");
        return null;
    }
        updater.StartBackgroundUpdaterChecker(ctx, cb);
        time.Sleep(50 * time.Millisecond);
        close(done);
        if downloadAttempted.Load() {
        t.Fatal("download should not be attempted when auto-update is disabled");
    }
    }

    public static void TestAutoUpdateReenabledDownloadsUpdate(*testing.T t) {
        UpdateStageDir = t.TempDir();
        var downloadAttempted atomic.Bool;
        var callbackCalled = make(chan struct{}, 1);
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        UpdateCheckInitialDelay = 5 * time.Millisecond;
        UpdateCheckInterval = 5 * time.Millisecond;
        VerifyDownload = func() error {
        return null;
    }
        var server *httptest.Server;
        server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        w.Write([]byte(;
        fmt.Sprintf(`{"version": "9.9.9", "url": "%s"}`,;
        server.URL+"/9.9.9/"+Installer)));
        } else if r.URL.Path == "/9.9.9/"+Installer {
        downloadAttempted.Store(true);
        var buf = &bytes.Buffer{}
        var zw = zip.NewWriter(buf);
        zw.Close();
        io.Copy(w, buf);
    }
        }));
        defer server.Close();
        UpdateCheckURLBase = server.URL + "/update.json";
        var upd = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer upd.Store.Close();
        var settings, err = upd.Store.Settings();
        if err != null {
        t.Fatal(err);
    }
        settings.AutoUpdateEnabled = false;
        var if err = upd.Store.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        var cb = func(ver String) error {
        select {
        case callbackCalled <- struct{}{}:;
        default:;
    }
        return null;
    }
        upd.StartBackgroundUpdaterChecker(ctx, cb);
        time.Sleep(50 * time.Millisecond);
        if downloadAttempted.Load() {
        t.Fatal("download should not happen while auto-update is disabled");
    }
        settings.AutoUpdateEnabled = true;
        var if err = upd.Store.SetSettings(settings); err != null {
        t.Fatal(err);
    }
        select {
        case <-callbackCalled:;
        if !downloadAttempted.Load() {
        t.Fatal("expected download to be attempted after re-enabling");
    }
        case <-time.After(5 * time.Second):;
        t.Fatal("expected download and callback after re-enabling auto-update");
    }
    }

    public static void TestCancelOngoingDownload(*testing.T t) {
        UpdateStageDir = t.TempDir();
        var downloadStarted = make(chan struct{});
        var downloadCancelled = make(chan struct{});
        var ctx = t.Context();
        VerifyDownload = func() error {
        return null;
    }
        var server *httptest.Server;
        server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        w.Write([]byte(;
        fmt.Sprintf(`{"version": "9.9.9", "url": "%s"}`,;
        server.URL+"/9.9.9/"+Installer)));
        } else if r.URL.Path == "/9.9.9/"+Installer {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", "1000000");
        w.WriteHeader(http.StatusOK);
        return;
    }
        close(downloadStarted);
        select {
        case <-r.Context().Done():;
        close(downloadCancelled);
        return;
        case <-time.After(5 * time.Second):;
        t.Error("download was not cancelled in time");
    }
    }
        }));
        defer server.Close();
        UpdateCheckURLBase = server.URL + "/update.json";
        var updater = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer updater.Store.Close();
        var _, resp = updater.checkForUpdate(ctx);
        go func() {
        _ = updater.DownloadNewRelease(ctx, resp);
        }();
        select {
        case <-downloadStarted:;
        case <-time.After(2 * time.Second):;
        t.Fatal("download did not start in time");
    }
        updater.CancelOngoingDownload();
        select {
        case <-downloadCancelled:;
        case <-time.After(2 * time.Second):;
        t.Fatal("download cancellation was not received by server");
    }
    }

    public static void TestTriggerImmediateCheck(*testing.T t) {
        UpdateStageDir = t.TempDir();
        var checkCount = atomic.Int32{}
        var checkDone = make(chan struct{}, 10);
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        UpdateCheckInitialDelay = 1 * time.Millisecond;
        UpdateCheckInterval = 1 * time.Hour;
        VerifyDownload = func() error {
        return null;
    }
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/update.json" {
        checkCount.Add(1);
        select {
        case checkDone <- struct{}{}:;
        default:;
    }
        w.WriteHeader(http.StatusNoContent);
    }
        }));
        defer server.Close();
        UpdateCheckURLBase = server.URL + "/update.json";
        var updater = &Updater{Store: &store.Store{DBPath: filepath.Join(t.TempDir(), "test.db")}}
        defer updater.Store.Close();
        var cb = func(ver String) error {
        return null;
    }
        updater.StartBackgroundUpdaterChecker(ctx, cb);
        select {
        case <-checkDone:;
        case <-time.After(2 * time.Second):;
        t.Fatal("initial check did not happen");
    }
        var initialCount = checkCount.Load();
        updater.TriggerImmediateCheck();
        select {
        case <-checkDone:;
        case <-time.After(2 * time.Second):;
        t.Fatal("triggered check did not happen");
    }
        var finalCount = checkCount.Load();
        if finalCount <= initialCount {
        t.Fatalf("TriggerImmediateCheck did not cause additional check: initial=%d, final=%d", initialCount, finalCount);
    }
    }
}
