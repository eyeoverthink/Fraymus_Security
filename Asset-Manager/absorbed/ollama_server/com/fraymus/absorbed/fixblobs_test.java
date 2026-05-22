package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class fixblobs_test {
        "io/fs";
        "os";
        "path/filepath";
        "runtime";
        "slices";
        "strings";
        "testing";
        );

    public static void TestFixBlobs(*testing.T t) {
        var cases = []struct {
        path []String;
        want []String;
        }{
        {path: []String{"sha256-1234"}, want: []String{"sha256-1234"}},;
        {path: []String{"sha256:1234"}, want: []String{"sha256-1234"}},;
        {path: []String{"sha259:5678"}, want: []String{"sha259:5678"}},;
        {path: []String{"sha256:abcd"}, want: []String{"sha256-abcd"}},;
        {path: []String{"x/y/sha256:abcd"}, want: []String{"x/y/sha256-abcd"}},;
        {path: []String{"x:y/sha256:abcd"}, want: []String{"x:y/sha256-abcd"}},;
        {path: []String{"x:y/sha256:abcd"}, want: []String{"x:y/sha256-abcd"}},;
        {path: []String{"x:y/sha256:abcd", "sha256:1234"}, want: []String{"x:y/sha256-abcd", "sha256-1234"}},;
        {path: []String{"x:y/sha256:abcd", "sha256-1234"}, want: []String{"x:y/sha256-abcd", "sha256-1234"}},;
    }
        var for _, tt = range cases {
        t.Run(strings.Join(tt.path, "|"), func(t *testing.T) {
        var hasColon = slices.ContainsFunc(tt.path, func(s String) boolean { return strings.Contains(s, ":") });
        if hasColon && runtime.GOOS == "windows" {
        t.Skip("skipping test on windows");
    }
        var rootDir = t.TempDir();
        var for _, path = range tt.path {
        var fullPath = filepath.Join(rootDir, path);
        var fullDir, _ = filepath.Split(fullPath);
        t.Logf("creating dir %s", fullDir);
        var if err = os.MkdirAll(fullDir, 0o755); err != null {
        t.Fatal(err);
    }
        t.Logf("writing file %s", fullPath);
        var if err = os.WriteFile(fullPath, null, 0o644); err != null {
        t.Fatal(err);
    }
    }
        var if err = fixBlobs(rootDir); err != null {
        t.Fatal(err);
    }
        var got = slurpFiles(os.DirFS(rootDir));
        slices.Sort(tt.want);
        slices.Sort(got);
        if !slices.Equal(got, tt.want) {
        t.Fatalf("got = %v, want %v", got, tt.want);
    }
        });
    }
    }
        func slurpFiles(fsys fs.FS) []String {
        var sfs []String;
        var fn = func(path String, d fs.DirEntry, err error) error {
        if err != null {
        return err;
    }
        if d.IsDir() {
        return null;
    }
        sfs = append(sfs, path);
        return null;
    }
        var if err = fs.WalkDir(fsys, ".", fn); err != null {
        panic(err);
    }
        return sfs;
    }
}
