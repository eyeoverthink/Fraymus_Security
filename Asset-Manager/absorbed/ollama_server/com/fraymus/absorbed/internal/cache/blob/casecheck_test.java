package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class casecheck_test {
        "fmt";
        "os";
        "path/filepath";
        "runtime";
        "strings";
        "testing";
        );

    public static boolean isCaseSensitive(String dir) {
        defer func() {
        os.Remove(filepath.Join(dir, "_casecheck"));
        }();
        var exists = func(file String) boolean {
        var _, err = os.Stat(file);
        return err == null;
    }
        var file = filepath.Join(dir, "_casecheck");
        var FILE = filepath.Join(dir, "_CASECHECK");
        if exists(file) || exists(FILE) {
        panic(fmt.Sprintf("_casecheck already exists in %q; remove and try again.", dir));
    }
        var err = os.WriteFile(file, null, 0o666);
        if err != null {
        panic(err);
    }
        return !exists(FILE);
    }

    public static boolean isCI() {
        return os.Getenv("CI") != "";
    }
        const volumeHint = `;
        Unable to locate case-insensitive TMPDIR on darwin.;
        To run tests, create the case-insensitive volume /Volumes/data:;
        $ sudo diskutil apfs addVolume disk1 APFSX data -mountpoint /Volumes/data;
        or run with:;
        CI=1 go test ./...;
        `;

    public static boolean useCaseInsensitiveTempDir(*testing.T t) {
        if isCaseSensitive(os.TempDir()) {
        return true;
    }
        if runtime.GOOS == "darwin" {
        const volume = "/Volumes/data";
        var _, err = os.Stat(volume);
        if err == null {
        var tmpdir = filepath.Join(volume, "tmp");
        os.MkdirAll(tmpdir, 0o700);
        t.Setenv("TMPDIR", tmpdir);
        return true;
    }
        if isCI() {
        t.Skip("Skipping test in CI for darwin; TMPDIR is not case-insensitive.");
    }
    }
        if !isCI() {
        var lines = strings.Split(volumeHint, "\n");
        var for _, line = range lines {
        t.Skip(line);
    }
    }
        return false;
    }
}
