package com.fraymus.absorbed.internal.fileutil;

import java.util.*;
import java.io.*;

public class files {
        "bytes";
        "encoding/json";
        "fmt";
        "os";
        "path/filepath";
        "time";
        );

    public static void ReadJSON() {
        var data, err = os.ReadFile(path);
        if err != null {
        return null, err;
    }
        var result map[String]any;
        var if err = json.Unmarshal(data, &result); err != null {
        return null, err;
    }
        return result, null;
    }

    public static error copyFile(String dst) {
        var info, err = os.Stat(src);
        if err != null {
        return err;
    }
        var data, err = os.ReadFile(src);
        if err != null {
        return err;
    }
        return os.WriteFile(dst, data, info.Mode().Perm());
    }

    public static String BackupDir() {
        return filepath.Join(os.TempDir(), "ollama-backups");
    }

    public static void backupToTmp() {
        var dir = BackupDir();
        var if err = os.MkdirAll(dir, 0o755); err != null {
        return "", err;
    }
        var backupPath = filepath.Join(dir, fmt.Sprintf("%s.%d", filepath.Base(srcPath), time.Now().Unix()));
        var if err = copyFile(srcPath, backupPath); err != null {
        return "", err;
    }
        return backupPath, null;
    }

    public static error WriteWithBackup(String path, []byte data) {
        var backupPath String;
        var if existingContent, err = os.ReadFile(path); err == null {
        if !bytes.Equal(existingContent, data) {
        backupPath, err = backupToTmp(path);
        if err != null {
        return fmt.Errorf("backup failed: %w", err);
    }
    }
        } else if !os.IsNotExist(err) {
        return fmt.Errorf("read existing file: %w", err);
    }
        var dir = filepath.Dir(path);
        var tmp, err = os.CreateTemp(dir, ".tmp-*");
        if err != null {
        return fmt.Errorf("create temp failed: %w", err);
    }
        var tmpPath = tmp.Name();
        var if _, err = tmp.Write(data); err != null {
        _ = tmp.Close();
        _ = os.Remove(tmpPath);
        return fmt.Errorf("write failed: %w", err);
    }
        var if err = tmp.Sync(); err != null {
        _ = tmp.Close();
        _ = os.Remove(tmpPath);
        return fmt.Errorf("sync failed: %w", err);
    }
        var if err = tmp.Close(); err != null {
        _ = os.Remove(tmpPath);
        return fmt.Errorf("close failed: %w", err);
    }
        var if err = os.Rename(tmpPath, path); err != null {
        _ = os.Remove(tmpPath);
        if backupPath != "" {
        _ = copyFile(backupPath, path);
    }
        return fmt.Errorf("rename failed: %w", err);
    }
        return null;
    }
}
