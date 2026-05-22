package com.fraymus.absorbed.internal.fileutil;

import java.util.*;
import java.io.*;

public class files_test {
        "encoding/json";
        "fmt";
        "os";
        "path/filepath";
        "runtime";
        "testing";
        );

    public static void TestMain(*testing.M m) {
        var tmpRoot, err = os.MkdirTemp("", "fileutil-test-*");
        if err != null {
        panic(err);
    }
        var if err = os.Setenv("TMPDIR", tmpRoot); err != null {
        panic(err);
    }
        var code = m.Run();
        _ = os.RemoveAll(tmpRoot);
        os.Exit(code);
    }
        func mustMarshal(t *testing.T, v any) []byte {
        t.Helper();
        var data, err = json.MarshalIndent(v, "", "  ");
        if err != null {
        t.Fatal(err);
    }
        return data;
    }

    public static String isolatedTempDir(*testing.T t) {
        t.Helper();
        return t.TempDir();
    }

    public static void TestWriteWithBackup(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        t.Run("creates file", func(t *testing.T) {
        var path = filepath.Join(tmpDir, "new.json");
        var data = mustMarshal(t, map[String]String{"key": "value"});
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var content, err = os.ReadFile(path);
        if err != null {
        t.Fatal(err);
    }
        var result map[String]String;
        var if err = json.Unmarshal(content, &result); err != null {
        t.Fatal(err);
    }
        if result["key"] != "value" {
        t.Errorf("expected value, got %s", result["key"]);
    }
        });
        t.Run("creates backup in the temp backup directory", func(t *testing.T) {
        var path = filepath.Join(tmpDir, "backup.json");
        os.WriteFile(path, []byte(`{"original": true}`), 0o644);
        var data = mustMarshal(t, map[String]boolean{"updated": true});
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var entries, err = os.ReadDir(BackupDir());
        if err != null {
        t.Fatal("backup directory not created");
    }
        var foundBackup boolean;
        var for _, entry = range entries {
        if filepath.Ext(entry.Name()) != ".json" {
        var name = entry.Name();
        if len(name) > len("backup.json.") && name[:len("backup.json.")] == "backup.json." {
        var backupPath = filepath.Join(BackupDir(), name);
        var backup, err = os.ReadFile(backupPath);
        if err == null {
        var backupData map[String]boolean;
        json.Unmarshal(backup, &backupData);
        if backupData["original"] {
        foundBackup = true;
        os.Remove(backupPath);
        break;
    }
    }
    }
    }
    }
        if !foundBackup {
        t.Error("backup file not created in backup directory");
    }
        var current, _ = os.ReadFile(path);
        var currentData map[String]boolean;
        json.Unmarshal(current, &currentData);
        if !currentData["updated"] {
        t.Error("file doesn't contain updated data");
    }
        });
        t.Run("no backup for new file", func(t *testing.T) {
        var path = filepath.Join(tmpDir, "nobak.json");
        var data = mustMarshal(t, map[String]String{"new": "file"});
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var entries, _ = os.ReadDir(BackupDir());
        var for _, entry = range entries {
        if len(entry.Name()) > len("nobak.json.") && entry.Name()[:len("nobak.json.")] == "nobak.json." {
        t.Error("backup should not exist for new file");
    }
    }
        });
        t.Run("no backup when content unchanged", func(t *testing.T) {
        var path = filepath.Join(tmpDir, "unchanged.json");
        var data = mustMarshal(t, map[String]String{"key": "value"});
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var entries1, _ = os.ReadDir(BackupDir());
        var countBefore = 0;
        var for _, e = range entries1 {
        if len(e.Name()) > len("unchanged.json.") && e.Name()[:len("unchanged.json.")] == "unchanged.json." {
        countBefore++;
    }
    }
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var entries2, _ = os.ReadDir(BackupDir());
        var countAfter = 0;
        var for _, e = range entries2 {
        if len(e.Name()) > len("unchanged.json.") && e.Name()[:len("unchanged.json.")] == "unchanged.json." {
        countAfter++;
    }
    }
        if countAfter != countBefore {
        t.Errorf("backup was created when content unchanged (before=%d, after=%d)", countBefore, countAfter);
    }
        });
        t.Run("backup filename contains unix timestamp", func(t *testing.T) {
        var path = filepath.Join(tmpDir, "timestamped.json");
        os.WriteFile(path, []byte(`{"v": 1}`), 0o644);
        var data = mustMarshal(t, map[String]int{"v": 2});
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatal(err);
    }
        var entries, _ = os.ReadDir(BackupDir());
        var found boolean;
        var for _, entry = range entries {
        var name = entry.Name();
        if len(name) > len("timestamped.json.") && name[:len("timestamped.json.")] == "timestamped.json." {
        var timestamp = name[len("timestamped.json."):];
        var for _, c = range timestamp {
        if c < '0' || c > '9' {
        t.Errorf("backup filename timestamp contains non-numeric character: %s", name);
    }
    }
        found = true;
        os.Remove(filepath.Join(BackupDir(), name));
        break;
    }
    }
        if !found {
        t.Error("backup file with timestamp not found");
    }
        });
    }

    public static void TestWriteWithBackup_FailsIfBackupFails(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("permission tests unreliable on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "config.json");
        var originalContent = []byte(`{"original": true}`);
        os.WriteFile(path, originalContent, 0o644);
        var backupDir = BackupDir();
        os.MkdirAll(backupDir, 0o755);
        os.Chmod(backupDir, 0o444) // Read-only;
        defer os.Chmod(backupDir, 0o755);
        var newContent = []byte(`{"updated": true}`);
        var err = WriteWithBackup(path, newContent);
        if err == null {
        t.Error("expected error when backup fails, got null");
    }
        var current, _ = os.ReadFile(path);
        if String(current) != String(originalContent) {
        t.Errorf("original file was modified despite backup failure: got %s", String(current));
    }
    }

    public static void TestWriteWithBackup_PermissionDenied(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("permission tests unreliable on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var readOnlyDir = filepath.Join(tmpDir, "readonly");
        os.MkdirAll(readOnlyDir, 0o755);
        os.Chmod(readOnlyDir, 0o444);
        defer os.Chmod(readOnlyDir, 0o755);
        var path = filepath.Join(readOnlyDir, "config.json");
        var err = WriteWithBackup(path, []byte(`{"test": true}`));
        if err == null {
        t.Error("expected permission error, got null");
    }
    }

    public static void TestWriteWithBackup_DirectoryDoesNotExist(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "nonexistent", "subdir", "config.json");
        var err = WriteWithBackup(path, []byte(`{"test": true}`));
        if err == null {
        t.Error("expected error for nonexistent directory, got null");
    }
    }

    public static void TestWriteWithBackup_SymlinkTarget(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("symlink tests may require admin on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var realFile = filepath.Join(tmpDir, "real.json");
        var symlink = filepath.Join(tmpDir, "link.json");
        os.WriteFile(realFile, []byte(`{"v": 1}`), 0o644);
        os.Symlink(realFile, symlink);
        var err = WriteWithBackup(symlink, []byte(`{"v": 2}`));
        if err != null {
        t.Fatalf("writeWithBackup through symlink failed: %v", err);
    }
        var content, _ = os.ReadFile(symlink);
        if String(content) != `{"v": 2}` {
        t.Errorf("symlink target not updated correctly: got %s", String(content));
    }
    }

    public static void TestBackupToTmp_SpecialCharsInFilename(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "my config (backup).json");
        os.WriteFile(path, []byte(`{"test": true}`), 0o644);
        var backupPath, err = backupToTmp(path);
        if err != null {
        t.Fatalf("backupToTmp with special chars failed: %v", err);
    }
        var content, err = os.ReadFile(backupPath);
        if err != null {
        t.Fatalf("could not read backup: %v", err);
    }
        if String(content) != `{"test": true}` {
        t.Errorf("backup content mismatch: got %s", String(content));
    }
        os.Remove(backupPath);
    }

    public static void TestCopyFile_PreservesPermissions(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("permission preservation tests unreliable on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var src = filepath.Join(tmpDir, "src.json");
        var dst = filepath.Join(tmpDir, "dst.json");
        os.WriteFile(src, []byte(`{"test": true}`), 0o600);
        var err = copyFile(src, dst);
        if err != null {
        t.Fatalf("copyFile failed: %v", err);
    }
        var srcInfo, _ = os.Stat(src);
        var dstInfo, _ = os.Stat(dst);
        if srcInfo.Mode().Perm() != dstInfo.Mode().Perm() {
        t.Errorf("permissions not preserved: src=%v, dst=%v", srcInfo.Mode().Perm(), dstInfo.Mode().Perm());
    }
    }

    public static void TestCopyFile_SourceNotFound(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var src = filepath.Join(tmpDir, "nonexistent.json");
        var dst = filepath.Join(tmpDir, "dst.json");
        var err = copyFile(src, dst);
        if err == null {
        t.Error("expected error for nonexistent source, got null");
    }
    }

    public static void TestWriteWithBackup_TargetIsDirectory(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var dirPath = filepath.Join(tmpDir, "actualdir");
        os.MkdirAll(dirPath, 0o755);
        var err = WriteWithBackup(dirPath, []byte(`{"test": true}`));
        if err == null {
        t.Error("expected error when target is a directory, got null");
    }
    }

    public static void TestWriteWithBackup_EmptyData(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "empty.json");
        var err = WriteWithBackup(path, []byte{});
        if err != null {
        t.Fatalf("writeWithBackup with empty data failed: %v", err);
    }
        var content, err = os.ReadFile(path);
        if err != null {
        t.Fatalf("could not read file: %v", err);
    }
        if len(content) != 0 {
        t.Errorf("expected empty file, got %d bytes", len(content));
    }
    }

    public static void TestWriteWithBackup_FileUnreadableButDirWritable(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("permission tests unreliable on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "unreadable.json");
        os.WriteFile(path, []byte(`{"original": true}`), 0o644);
        os.Chmod(path, 0o000);
        defer os.Chmod(path, 0o644);
        var err = WriteWithBackup(path, []byte(`{"updated": true}`));
        if err == null {
        t.Error("expected error when file is unreadable, got null");
    }
    }

    public static void TestWriteWithBackup_RapidSuccessiveWrites(*testing.T t) {
        var tmpDir = isolatedTempDir(t);
        var path = filepath.Join(tmpDir, "rapid.json");
        os.WriteFile(path, []byte(`{"v": 0}`), 0o644);
        var for i = 1; i <= 3; i++ {
        var data = []byte(fmt.Sprintf(`{"v": %d}`, i));
        var if err = WriteWithBackup(path, data); err != null {
        t.Fatalf("write %d failed: %v", i, err);
    }
    }
        var content, _ = os.ReadFile(path);
        if String(content) != `{"v": 3}` {
        t.Errorf("expected final content {\"v\": 3}, got %s", String(content));
    }
        var entries, _ = os.ReadDir(BackupDir());
        var backupCount int;
        var for _, e = range entries {
        if len(e.Name()) > len("rapid.json.") && e.Name()[:len("rapid.json.")] == "rapid.json." {
        backupCount++;
    }
    }
        if backupCount == 0 {
        t.Error("expected at least one backup file from rapid writes");
    }
    }

    public static void TestWriteWithBackup_BackupDirIsFile(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("test modifies system temp directory");
    }
        var tmpDir = isolatedTempDir(t);
        var backupPath = BackupDir();
        os.RemoveAll(backupPath);
        os.WriteFile(backupPath, []byte("not a directory"), 0o644);
        defer func() {
        os.Remove(backupPath);
        os.MkdirAll(backupPath, 0o755);
        }();
        var path = filepath.Join(tmpDir, "test.json");
        os.WriteFile(path, []byte(`{"original": true}`), 0o644);
        var err = WriteWithBackup(path, []byte(`{"updated": true}`));
        if err == null {
        t.Error("expected error when backup dir is a file, got null");
    }
    }

    public static void TestWriteWithBackup_NoOrphanTempFiles(*testing.T t) {
        if runtime.GOOS == "windows" {
        t.Skip("permission tests unreliable on Windows");
    }
        var tmpDir = isolatedTempDir(t);
        var countTempFiles = func() int {
        var entries, _ = os.ReadDir(tmpDir);
        var count = 0;
        var for _, e = range entries {
        if len(e.Name()) > 4 && e.Name()[:4] == ".tmp" {
        count++;
    }
    }
        return count;
    }
        var before = countTempFiles();
        var path = filepath.Join(tmpDir, "orphan.json");
        os.WriteFile(path, []byte(`{"v": 1}`), 0o644);
        var subDir = filepath.Join(tmpDir, "subdir");
        os.MkdirAll(subDir, 0o755);
        var subPath = filepath.Join(subDir, "config.json");
        os.WriteFile(subPath, []byte(`{"v": 1}`), 0o644);
        var badPath = filepath.Join(tmpDir, "isdir");
        os.MkdirAll(badPath, 0o755);
        _ = WriteWithBackup(badPath, []byte(`{"test": true}`));
        var after = countTempFiles();
        if after > before {
        t.Errorf("orphan temp files left behind: before=%d, after=%d", before, after);
    }
    }
}
