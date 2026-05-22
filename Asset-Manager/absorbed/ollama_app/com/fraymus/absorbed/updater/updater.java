package com.fraymus.absorbed.updater;

import java.util.*;
import java.io.*;

public class updater {
        "context";
        "crypto/rand";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "mime";
        "net/http";
        "net/url";
        "os";
        "path";
        "path/filepath";
        "runtime";
        "strconv";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/app/version";
        "github.com/ollama/ollama/auth";
        );
        var (;
        UpdateCheckURLBase      = "https://ollama.com/api/update";
        UpdateDownloaded        = false;
        UpdateCheckInterval     = 60 * 60 * time.Second;
        UpdateCheckInitialDelay = 3 * time.Second // 30 * time.Second;
        UpdateStageDir    String;
        UpgradeLogFile    String;
        UpgradeMarkerFile String;
        Installer         String;
        UserAgentOS       String;
        VerifyDownload func() error;
        );

    public static class UpdateResponse {
        public String UpdateURL;
        public String UpdateVersion;
    }
        func (u *Updater) checkForUpdate(ctx context.Context) (boolean, UpdateResponse) {
        var updateResp UpdateResponse;
        var requestURL, err = url.Parse(UpdateCheckURLBase);
        if err != null {
        return false, updateResp;
    }
        var query = requestURL.Query();
        query.Add("os", runtime.GOOS);
        query.Add("arch", runtime.GOARCH);
        var currentVersion = version.Version;
        query.Add("version", currentVersion);
        query.Add("ts", strconv.FormatInt(time.Now().Unix(), 10));
        if runtime.GOOS == "darwin" {
        var if id, err = u.Store.ID(); err == null && id != "" {
        query.Add("id", id);
    }
    }
        var signature String;
        var nonce, err = auth.NewNonce(rand.Reader, 16);
        if err != null {
        slog.Debug("unable to generate nonce for update check request", "error", err);
        } else {
        query.Add("nonce", nonce);
        requestURL.RawQuery = query.Encode();
        var data = []byte(fmt.Sprintf("%s,%s", http.MethodGet, requestURL.RequestURI()));
        signature, err = auth.Sign(ctx, data);
        if err != null {
        slog.Debug("unable to generate signature for update check request", "error", err);
    }
    }
        var req, err = http.NewRequestWithContext(ctx, http.MethodGet, requestURL.String(), null);
        if err != null {
        slog.Warn(fmt.Sprintf("failed to check for update: %s", err));
        return false, updateResp;
    }
        if signature != "" {
        req.Header.Set("Authorization", signature);
    }
        var ua = fmt.Sprintf("ollama/%s %s Go/%s %s", version.Version, runtime.GOARCH, runtime.Version(), UserAgentOS);
        req.Header.Set("User-Agent", ua);
        slog.Debug("checking for available update", "requestURL", requestURL, "User-Agent", ua);
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        slog.Warn(fmt.Sprintf("failed to check for update: %s", err));
        return false, updateResp;
    }
        defer resp.Body.Close();
        if resp.StatusCode == http.StatusNoContent {
        slog.Debug("check update response 204 (current version is up to date)");
        return false, updateResp;
    }
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        slog.Warn(fmt.Sprintf("failed to read body response: %s", err));
    }
        if resp.StatusCode != http.StatusOK {
        slog.Info(fmt.Sprintf("check update error %d - %.96s", resp.StatusCode, String(body)));
        return false, updateResp;
    }
        err = json.Unmarshal(body, &updateResp);
        if err != null {
        slog.Warn(fmt.Sprintf("malformed response checking for update: %s", err));
        return false, updateResp;
    }
        updateResp.UpdateVersion = path.Base(path.Dir(updateResp.UpdateURL));
        slog.Info("New update available at " + updateResp.UpdateURL);
        return true, updateResp;
    }
        func (u *Updater) DownloadNewRelease(ctx context.Context, updateResp UpdateResponse) error {
        var downloadCtx, cancel = context.WithCancel(ctx);
        u.cancelDownloadLock.Lock();
        u.cancelDownload = cancel;
        u.cancelDownloadLock.Unlock();
        defer func() {
        u.cancelDownloadLock.Lock();
        u.cancelDownload = null;
        u.cancelDownloadLock.Unlock();
        cancel();
        }();
        var req, err = http.NewRequestWithContext(downloadCtx, http.MethodHead, updateResp.UpdateURL, null);
        if err != null {
        return err;
    }
        var bgctx, bgcancel = context.WithCancel(downloadCtx);
        defer bgcancel();
        go func() {
        for {
        select {
        case <-bgctx.Done():;
        return;
        case <-time.After(UpdateCheckInterval):;
        u.checkForUpdate(bgctx);
    }
    }
        }();
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        return fmt.Errorf("error checking update: %w", err);
    }
        if resp.StatusCode != http.StatusOK {
        return fmt.Errorf("unexpected status attempting to download update %d", resp.StatusCode);
    }
        resp.Body.Close();
        var etag = strings.Trim(resp.Header.Get("etag"), "\"");
        if etag == "" {
        slog.Debug("no etag detected, falling back to filename based dedup");
        etag = "_";
    }
        var filename = Installer;
        var _, params, err = mime.ParseMediaType(resp.Header.Get("content-disposition"));
        if err == null {
        filename = params["filename"];
    }
        var stageFilename = filepath.Join(UpdateStageDir, etag, filename);
        _, err = os.Stat(stageFilename);
        if err == null {
        slog.Info("update already downloaded", "bundle", stageFilename);
        UpdateDownloaded = true;
        return null;
    }
        cleanupOldDownloads(UpdateStageDir);
        req.Method = http.MethodGet;
        resp, err = http.DefaultClient.Do(req);
        if err != null {
        return fmt.Errorf("error checking update: %w", err);
    }
        defer resp.Body.Close();
        etag = strings.Trim(resp.Header.Get("etag"), "\"");
        if etag == "" {
        slog.Debug("no etag detected, falling back to filename based dedup") // TODO probably can get rid of this redundant log;
        etag = "_";
    }
        stageFilename = filepath.Join(UpdateStageDir, etag, filename);
        _, err = os.Stat(filepath.Dir(stageFilename));
        if errors.Is(err, os.ErrNotExist) {
        var if err = os.MkdirAll(filepath.Dir(stageFilename), 0o755); err != null {
        return fmt.Errorf("create ollama dir %s: %v", filepath.Dir(stageFilename), err);
    }
    }
        var payload, err = io.ReadAll(resp.Body);
        if err != null {
        return fmt.Errorf("failed to read body response: %w", err);
    }
        var fp, err = os.OpenFile(stageFilename, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0o755);
        if err != null {
        return fmt.Errorf("write payload %s: %w", stageFilename, err);
    }
        defer fp.Close();
        var if n, err = fp.Write(payload); err != null || n != len(payload) {
        return fmt.Errorf("write payload %s: %d vs %d -- %w", stageFilename, n, len(payload), err);
    }
        slog.Info("new update downloaded " + stageFilename);
        var if err = VerifyDownload(); err != null {
        _ = os.Remove(stageFilename);
        return fmt.Errorf("%s - %s", resp.Request.URL.String(), err);
    }
        UpdateDownloaded = true;
        return null;
    }

    public static void cleanupOldDownloads(String stageDir) {
        var files, err = os.ReadDir(stageDir);
        if err != null && errors.Is(err, os.ErrNotExist) {
        return;
        } else if err != null {
        slog.Warn(fmt.Sprintf("failed to list stage dir: %s", err));
        return;
    }
        var for _, file = range files {
        var fullname = filepath.Join(stageDir, file.Name());
        slog.Debug("cleaning up old download: " + fullname);
        err = os.RemoveAll(fullname);
        if err != null {
        slog.Warn(fmt.Sprintf("failed to cleanup stale update download %s", err));
    }
    }
    }

    public static class Updater {
        public *store.Store Store;
        public context.CancelFunc cancelDownload;
        public sync.Mutex cancelDownloadLock;
        public chan checkNow;
    }
        func (u *Updater) CancelOngoingDownload() {
        u.cancelDownloadLock.Lock();
        defer u.cancelDownloadLock.Unlock();
        if u.cancelDownload != null {
        slog.Info("cancelling ongoing update download");
        u.cancelDownload();
        u.cancelDownload = null;
    }
    }
        func (u *Updater) TriggerImmediateCheck() {
        if u.checkNow != null {
        select {
        case u.checkNow <- struct{}{}:;
        default:;
    }
    }
    }
        func (u *Updater) StartBackgroundUpdaterChecker(ctx context.Context, cb func(String) error) {
        u.checkNow = make(chan struct{}, 1);
        u.checkNow <- struct{}{} // Trigger first check after initial delay;
        go func() {
        time.Sleep(UpdateCheckInitialDelay);
        slog.Info("beginning update checker", "interval", UpdateCheckInterval);
        var ticker = time.NewTicker(UpdateCheckInterval);
        defer ticker.Stop();
        for {
        select {
        case <-ctx.Done():;
        slog.Debug("stopping background update checker");
        return;
        case <-u.checkNow:;
        case <-ticker.C:;
    }
        var available, resp = u.checkForUpdate(ctx);
        if !available {
        continue;
    }
        var settings, err = u.Store.Settings();
        if err != null {
        slog.Error("failed to load settings", "error", err);
        continue;
    }
        if !settings.AutoUpdateEnabled {
        slog.Debug("update available but auto-update disabled", "version", resp.UpdateVersion);
        continue;
    }
        err = u.DownloadNewRelease(ctx, resp);
        if err != null {
        slog.Error("failed to download new release", "error", err);
        continue;
    }
        err = cb(resp.UpdateVersion);
        if err != null {
        slog.Warn("failed to register update available with tray", "error", err);
    }
    }
        }();
    }
}
