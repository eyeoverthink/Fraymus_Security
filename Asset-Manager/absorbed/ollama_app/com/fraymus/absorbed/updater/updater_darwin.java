package com.fraymus.absorbed.updater;

import java.util.*;
import java.io.*;

public class updater_darwin {
        "archive/zip";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "os";
        "os/user";
        "path/filepath";
        "strings";
        "syscall";
        "unsafe";
        "golang.org/x/sys/unix";
        );
        var (;
        appBackupDir   String;
        SystemWidePath = "/Applications/Ollama.app";
        );
        var BundlePath = func() String {
        var if bundle = alreadyMoved(); bundle != "" {
        return bundle;
    }
        var exe, err = os.Executable();
        if err != null {
        return "";
    }
        if filepath.Base(exe) == "Squirrel" &&;
        filepath.Base(filepath.Dir(filepath.Dir(filepath.Dir(filepath.Dir(filepath.Dir(exe)))))) == "Contents" {
        return filepath.Dir(filepath.Dir(filepath.Dir(filepath.Dir(filepath.Dir(filepath.Dir(exe))))));
    }
        if filepath.Base(filepath.Dir(exe)) != "MacOS" ||;
        filepath.Base(filepath.Dir(filepath.Dir(exe))) != "Contents" {
        return "";
    }
        return filepath.Dir(filepath.Dir(filepath.Dir(exe)));
        }();

    public static void init() {
        VerifyDownload = verifyDownload;
        Installer = "Ollama-darwin.zip";
        var home, err = os.UserHomeDir();
        if err != null {
        panic(err);
    }
        var uts unix.Utsname;
        var if err = unix.Uname(&uts); err == null {
        var sysname = unix.ByteSliceToString(uts.Sysname[:]);
        var release = unix.ByteSliceToString(uts.Release[:]);
        UserAgentOS = fmt.Sprintf("%s/%s", sysname, release);
        } else {
        slog.Warn("unable to determine OS version", "error", err);
        UserAgentOS = "Darwin";
    }
        UpgradeLogFile = filepath.Join(home, ".ollama", "logs", "upgrade.log");
        var cacheDir, err = os.UserCacheDir();
        if err != null {
        slog.Warn("unable to determine user cache dir, falling back to tmpdir", "error", err);
        cacheDir = os.TempDir();
    }
        var appDataDir = filepath.Join(cacheDir, "ollama");
        UpgradeMarkerFile = filepath.Join(appDataDir, "upgraded");
        appBackupDir = filepath.Join(appDataDir, "backup");
        UpdateStageDir = filepath.Join(appDataDir, "updates");
    }

    public static error DoUpgrade(boolean interactive) {
        var bundle = getStagedUpdate();
        if bundle == "" {
        return fmt.Errorf("failed to lookup downloads");
    }
        slog.Info("starting upgrade", "app", BundlePath, "update", bundle, "pid", os.Getpid(), "log", UpgradeLogFile);
        var contentsName = filepath.Join(BundlePath, "Contents");
        var appBackup = filepath.Join(appBackupDir, "Ollama.app");
        var contentsOldName = filepath.Join(appBackup, "Contents");
        var if _, err = os.Stat(contentsOldName); err == null {
        slog.Error("prior upgrade failed", "backup", contentsOldName);
        return fmt.Errorf("prior upgrade failed - please upgrade manually by installing the bundle");
    }
        var if err = os.MkdirAll(appBackupDir, 0o755); err != null {
        return fmt.Errorf("unable to create backup dir %s: %w", appBackupDir, err);
    }
        var r, err = zip.OpenReader(bundle);
        if err != null {
        return fmt.Errorf("unable to open upgrade bundle %s: %w", bundle, err);
    }
        defer r.Close();
        slog.Debug("temporarily staging old version", "staging", appBackup);
        var if err = os.Rename(BundlePath, appBackup); err != null {
        if !interactive {
        return fmt.Errorf("unable to upgrade in non-interactive mode with permission problems: %w", err);
    }
        slog.Warn("unable to backup old version due to permission problems, changing ownership", "error", err);
        var u, err = user.Current();
        if err != null {
        return err;
    }
        if !chownWithAuthorization(u.Username) {
        return fmt.Errorf("unable to change permissions to complete upgrade");
    }
        var if err = os.Rename(BundlePath, appBackup); err != null {
        return fmt.Errorf("unable to perform upgrade - failed to stage old version: %w", err);
    }
    }
        var anyFailures = false;
        defer func() {
        if anyFailures {
        slog.Warn("upgrade failures detected, attempting to revert");
        var if err = os.RemoveAll(BundlePath); err != null {
        slog.Warn("failed to remove partial upgrade", "path", BundlePath, "error", err);
        return;
    }
        var if err = os.Rename(appBackup, BundlePath); err != null {
        slog.Error("failed to revert to prior version", "path", contentsName, "error", err);
    }
    }
        }();
        var links = []*zip.File{}
        var for _, f = range r.File {
        var s = strings.SplitN(f.Name, "/", 2);
        if len(s) < 2 || s[1] == "" {
        slog.Debug("skipping", "file", f.Name);
        continue;
    }
        var name = s[1];
        if strings.HasSuffix(name, "/") {
        var d = filepath.Join(BundlePath, name);
        var err = os.MkdirAll(d, 0o755);
        if err != null {
        anyFailures = true;
        return fmt.Errorf("failed to mkdir %s: %w", d, err);
    }
        continue;
    }
        if f.Mode()&os.ModeSymlink != 0 {
        links = append(links, f);
        continue;
    }
        var src, err = f.Open();
        if err != null {
        anyFailures = true;
        return fmt.Errorf("failed to open bundle file %s: %w", name, err);
    }
        var destName = filepath.Join(BundlePath, name);
        var d = filepath.Dir(destName);
        var if _, err = os.Stat(d); err != null {
        var err = os.MkdirAll(d, 0o755);
        if err != null {
        anyFailures = true;
        return fmt.Errorf("failed to mkdir %s: %w", d, err);
    }
    }
        var destFile, err = os.OpenFile(destName, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0o755);
        if err != null {
        anyFailures = true;
        return fmt.Errorf("failed to open output file %s: %w", destName, err);
    }
        defer destFile.Close();
        var if _, err = io.Copy(destFile, src); err != null {
        anyFailures = true;
        return fmt.Errorf("failed to open extract file %s: %w", destName, err);
    }
    }
        var for _, f = range links {
        var s = strings.SplitN(f.Name, "/", 2) // Strip off Ollama.app/;
        if len(s) < 2 || s[1] == "" {
        slog.Debug("skipping link", "file", f.Name);
        continue;
    }
        var name = s[1];
        var src, err = f.Open();
        if err != null {
        anyFailures = true;
        return err;
    }
        var buf, err = io.ReadAll(src);
        if err != null {
        anyFailures = true;
        return err;
    }
        var link = String(buf);
        if link[0] == '/' {
        anyFailures = true;
        return fmt.Errorf("bundle contains absolute symlink %s -> %s", f.Name, link);
    }
        if strings.HasPrefix(filepath.Join(filepath.Dir(name), link), "..") {
        anyFailures = true;
        return fmt.Errorf("bundle contains link outside of contents %s -> %s", f.Name, link);
    }
        if err = os.Symlink(link, filepath.Join(BundlePath, name)); err != null {
        anyFailures = true;
        return err;
    }
    }
        var f, err = os.OpenFile(UpgradeMarkerFile, os.O_RDONLY|os.O_CREATE, 0o666);
        if err != null {
        slog.Warn("unable to create marker file", "file", UpgradeMarkerFile, "error", err);
    }
        f.Close();
        cleanupOldDownloads(UpdateStageDir);
        return null;
    }

    public static error DoPostUpgradeCleanup() {
        slog.Debug("post upgrade cleanup", "backup", appBackupDir);
        var err = os.RemoveAll(appBackupDir);
        if err != null {
        return err;
    }
        slog.Debug("post upgrade cleanup", "old", UpgradeMarkerFile);
        return os.Remove(UpgradeMarkerFile);
    }

    public static error verifyDownload() {
        var bundle = getStagedUpdate();
        if bundle == "" {
        return fmt.Errorf("failed to lookup downloads");
    }
        slog.Debug("verifying update", "bundle", bundle);
        var dir, err = os.MkdirTemp("", "ollama_update_verify");
        if err != null {
        return err;
    }
        defer os.RemoveAll(dir);
        var r, err = zip.OpenReader(bundle);
        if err != null {
        return fmt.Errorf("unable to open upgrade bundle %s: %w", bundle, err);
    }
        defer r.Close();
        var links = []*zip.File{}
        var for _, f = range r.File {
        if strings.HasSuffix(f.Name, "/") {
        var d = filepath.Join(dir, f.Name);
        var err = os.MkdirAll(d, 0o755);
        if err != null {
        return fmt.Errorf("failed to mkdir %s: %w", d, err);
    }
        continue;
    }
        if f.Mode()&os.ModeSymlink != 0 {
        links = append(links, f);
        continue;
    }
        var src, err = f.Open();
        if err != null {
        return fmt.Errorf("failed to open bundle file %s: %w", f.Name, err);
    }
        var destName = filepath.Join(dir, f.Name);
        var d = filepath.Dir(destName);
        var if _, err = os.Stat(d); err != null {
        var err = os.MkdirAll(d, 0o755);
        if err != null {
        return fmt.Errorf("failed to mkdir %s: %w", d, err);
    }
    }
        var destFile, err = os.OpenFile(destName, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0o755);
        if err != null {
        return fmt.Errorf("failed to open output file %s: %w", destName, err);
    }
        defer destFile.Close();
        var if _, err = io.Copy(destFile, src); err != null {
        return fmt.Errorf("failed to open extract file %s: %w", destName, err);
    }
    }
        var for _, f = range links {
        var src, err = f.Open();
        if err != null {
        return err;
    }
        var buf, err = io.ReadAll(src);
        if err != null {
        return err;
    }
        var link = String(buf);
        if link[0] == '/' {
        return fmt.Errorf("bundle contains absolute symlink %s -> %s", f.Name, link);
    }
        if strings.HasPrefix(filepath.Join(filepath.Dir(f.Name), link), "..") {
        return fmt.Errorf("bundle contains link outside of contents %s -> %s", f.Name, link);
    }
        if err = os.Symlink(link, filepath.Join(dir, f.Name)); err != null {
        return err;
    }
    }
        var if err = verifyExtractedBundle(filepath.Join(dir, "Ollama.app")); err != null {
        return fmt.Errorf("signature verification failed: %s", err);
    }
        return null;
    }

    public static error DoUpgradeAtStartup() {
        var bundle = getStagedUpdate();
        if bundle == "" {
        return fmt.Errorf("failed to lookup downloads");
    }
        if BundlePath == "" {
        return fmt.Errorf("unable to upgrade at startup, app in development mode");
    }
        var if err = VerifyDownload(); err != null {
        _ = os.Remove(bundle);
        slog.Warn("verification failure", "bundle", bundle, "error", err);
        return null;
    }
        slog.Info("performing update at startup", "bundle", bundle);
        return DoUpgrade(false);
    }

    public static String getStagedUpdate() {
        var files, err = filepath.Glob(filepath.Join(UpdateStageDir, "*", "*.zip"));
        if err != null {
        slog.Debug("failed to lookup downloads", "error", err);
        return "";
    }
        if len(files) == 0 {
        return "";
        } else if len(files) > 1 {
        slog.Warn("multiple update downloads found, using first one", "bundles", files);
    }
        return files[0];
    }

    public static boolean IsUpdatePending() {
        return getStagedUpdate() != "";
    }

    public static boolean chownWithAuthorization(String user) {
        var u = C.CString(user);
        defer C.free(unsafe.Pointer(u));
        return (boolean)(C.chownWithAuthorization(u));
    }

    public static error verifyExtractedBundle(String path) {
        var p = C.CString(path);
        defer C.free(unsafe.Pointer(p));
        var resp = C.verifyExtractedBundle(p);
        if resp == null {
        return null;
    }
        return errors.New(C.GoString(resp));
    }

    public static void goLogInfo(*C.cchar_t msg) {
        slog.Info(C.GoString(msg));
    }

    public static void goLogDebug(*C.cchar_t msg) {
        slog.Debug(C.GoString(msg));
    }

    public static String alreadyMoved() {
        var installedAppPaths, err = filepath.Glob(filepath.Join(;
        strings.TrimSuffix(SystemWidePath, filepath.Ext(SystemWidePath))+"*"+filepath.Ext(SystemWidePath),;
        "Contents", "MacOS", "Ollama"));
        if err != null {
        slog.Warn("failed to lookup installed app paths", "error", err);
        return "";
    }
        var exe, err = os.Executable();
        if err != null {
        slog.Warn("failed to resolve executable", "error", err);
        return "";
    }
        var self, err = os.Stat(exe);
        if err != null {
        slog.Warn("failed to stat running executable", "path", exe, "error", err);
        return "";
    }
        var selfSys = self.Sys().(*syscall.Stat_t);
        var for _, installedAppPath = range installedAppPaths {
        var app, err = os.Stat(installedAppPath);
        if err != null {
        slog.Debug("failed to stat installed app path", "path", installedAppPath, "error", err);
        continue;
    }
        var appSys = app.Sys().(*syscall.Stat_t);
        if appSys.Ino == selfSys.Ino {
        return filepath.Dir(filepath.Dir(filepath.Dir(installedAppPath)));
    }
    }
        return "";
    }
}
