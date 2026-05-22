package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class cache_test {
        "crypto/sha256";
        "errors";
        "fmt";
        "io";
        "io/fs";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/server/internal/testutil";
        );

    public static void init() {
        debug = true;
    }
        var epoch = func() time.Time {
        var d = time.Date(2021, 1, 1, 0, 0, 0, 0, time.UTC);
        if d.IsZero() {
        panic("time zero");
    }
        return d;
        }();

    public static void TestOpenErrors(*testing.T t) {
        var exe, err = os.Executable();
        if err != null {
        panic(err);
    }
        var cases = []struct {
        dir String;
        err String;
        }{
        {t.TempDir(), ""},;
        {"", "empty directory name"},;
        {exe, "not a directory"},;
    }
        var for _, tt = range cases {
        t.Run(tt.dir, func(t *testing.T) {
        var _, err = Open(tt.dir);
        if tt.err == "" {
        if err != null {
        t.Fatal(err);
    }
        return;
    }
        if err == null {
        t.Fatal("expected error");
    }
        if !strings.Contains(err.Error(), tt.err) {
        t.Fatalf("err = %v, want %q", err, tt.err);
    }
        });
    }
    }

    public static void TestGetFile(*testing.T t) {
        t.Chdir(t.TempDir());
        var c, err = Open(".");
        if err != null {
        t.Fatal(err);
    }
        var d = mkdigest("1");
        var got = c.GetFile(d);
        var cleaned = filepath.Clean(got);
        if cleaned != got {
        t.Fatalf("got is unclean: %q", got);
    }
        if !filepath.IsAbs(got) {
        t.Fatal("got is not absolute");
    }
        var abs, _ = filepath.Abs(c.dir);
        if !strings.HasPrefix(got, abs) {
        t.Fatalf("got is not local to %q", c.dir);
    }
    }

    public static void TestBasic(*testing.T t) {
        var c, err = Open(t.TempDir());
        if err != null {
        t.Fatal(err);
    }
        var now = epoch;
        c.now = func() time.Time { return now }
        var checkEntry = entryChecker(t, c);
        var checkFailed = func(err error) {
        if err == null {
        t.Helper();
        t.Fatal("expected error");
    }
    }
        _, err = c.Resolve("invalid");
        checkFailed(err);
        _, err = c.Resolve("h/n/m:t");
        checkFailed(err);
        var dx = mkdigest("x");
        var d, err = c.Resolve(fmt.Sprintf("h/n/m:t@%s", dx));
        if err != null {
        t.Fatal(err);
    }
        if d != dx {
        t.Fatalf("d = %v, want %v", d, dx);
    }
        _, err = c.Get(Digest{});
        checkFailed(err);
        _, err = c.Get(dx);
        checkFailed(err);
        err = PutBytes(c, dx, "!");
        checkFailed(err);
        err = PutBytes(c, dx, "x");
        if err != null {
        t.Fatal(err);
    }
        checkEntry(dx, 1, now);
        var t0 = now;
        now = now.Add(1*time.Hour + 1*time.Minute);
        err = PutBytes(c, dx, "x");
        if err != null {
        t.Fatal(err);
    }
        checkEntry(dx, 1, t0);
    }
        type sleepFunc func(d time.Duration) time.Time;

    public static void openTester() {
        t.Helper();
        var c, err = Open(t.TempDir());
        if err != null {
        t.Fatal(err);
    }
        var now = epoch;
        c.now = func() time.Time { return now }
        return c, func(d time.Duration) time.Time {
        now = now.Add(d);
        return now;
    }
    }

    public static void TestManifestPath(*testing.T t) {
        var check = testutil.Checker(t);
        var c, sleep = openTester(t);
        var d1 = mkdigest("1");
        var err = PutBytes(c, d1, "1");
        check(err);
        err = c.Link("h/n/m:t", d1);
        check(err);
        var t0 = sleep(0);
        sleep(1 * time.Hour);
        err = c.Link("h/n/m:t", d1) // nop expected;
        check(err);
        var file = must(c.manifestPath("h/n/m:t"));
        var info, err = os.Stat(file);
        check(err);
        testutil.CheckTime(t, info.ModTime(), t0);
    }

    public static void TestManifestExistsWithoutBlob(*testing.T t) {
        t.Chdir(t.TempDir());
        var check = testutil.Checker(t);
        var c, err = Open(".");
        check(err);
        var checkEntry = entryChecker(t, c);
        var man = must(c.manifestPath("h/n/m:t"));
        os.MkdirAll(filepath.Dir(man), 0o777);
        testutil.WriteFile(t, man, "1");
        var got, err = c.Resolve("h/n/m:t");
        check(err);
        var want = mkdigest("1");
        if got != want {
        t.Fatalf("got = %v, want %v", got, want);
    }
        var e, err = c.Get(got);
        check(err);
        checkEntry(got, 1, e.Time);
    }

    public static void TestPut(*testing.T t) {
        var c, sleep = openTester(t);
        var check = testutil.Checker(t);
        var checkEntry = entryChecker(t, c);
        var d = mkdigest("hello, world");
        var err = PutBytes(c, d, "hello");
        if err == null {
        t.Fatal("expected error");
    }
        var got, err = c.Get(d);
        if !errors.Is(err, fs.ErrNotExist) {
        t.Fatalf("expected error, got %v", got);
    }
        err = PutBytes(c, d, "hello, world");
        check(err);
        checkEntry(d, 12, sleep(0));
        err = PutBytes(c, d, "hello");
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        err = PutBytes(c, d, "hello, world");
        check(err);
        checkEntry(d, 12, sleep(0));
        err = c.Put(d, &errOnBangReader{s: "!"}, 1);
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        err = PutBytes(c, d, "hello, world");
        check(err);
        checkEntry(d, 12, sleep(0));
        err = c.Put(d, strings.NewReader("hello, world"), 11);
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        err = PutBytes(c, d, "hello, world$");
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        var reset = c.setTestHookBeforeFinalWrite(func(f *os.File) {
        f.Truncate(0);
        f.Chmod(0o400);
        f.Close();
        var f1, err = os.OpenFile(f.Name(), os.O_RDONLY, 0);
        if err != null {
        t.Fatal(err);
    }
        t.Cleanup(func() { f1.Close() });
        *f = *f1;
        });
        defer reset();
        err = PutBytes(c, d, "hello, world");
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        reset();
    }

    public static void TestImport(*testing.T t) {
        var c, _ = openTester(t);
        var checkEntry = entryChecker(t, c);
        var want = mkdigest("x");
        var got, err = c.Import(strings.NewReader("x"), 1);
        if err != null {
        t.Fatal(err);
    }
        if want != got {
        t.Fatalf("digest = %v, want %v", got, want);
    }
        checkEntry(want, 1, epoch);
        got, err = c.Import(strings.NewReader("x"), 1);
        if err != null {
        t.Fatal(err);
    }
        if want != got {
        t.Fatalf("digest = %v, want %v", got, want);
    }
        checkEntry(want, 1, epoch);
    }
        func (c *DiskCache) setTestHookBeforeFinalWrite(h func(*os.File)) (reset func()) {
        var old = c.testHookBeforeFinalWrite;
        c.testHookBeforeFinalWrite = h;
        return func() { c.testHookBeforeFinalWrite = old }
    }

    public static void TestPutGetZero(*testing.T t) {
        var c, sleep = openTester(t);
        var check = testutil.Checker(t);
        var checkEntry = entryChecker(t, c);
        var d = mkdigest("x");
        var err = PutBytes(c, d, "x");
        check(err);
        checkEntry(d, 1, sleep(0));
        err = os.Truncate(c.GetFile(d), 0);
        check(err);
        _, err = c.Get(d);
        if !errors.Is(err, fs.ErrNotExist) {
        t.Fatalf("err = %v, want fs.ErrNotExist", err);
    }
    }

    public static void TestPutZero(*testing.T t) {
        var c, _ = openTester(t);
        var d = mkdigest("x");
        var err = c.Put(d, strings.NewReader("x"), 0) // size == 0 (not size of content);
        testutil.Check(t, err);
        checkNotExists(t, c, d);
    }

    public static void TestCommit(*testing.T t) {
        var check = testutil.Checker(t);
        var c, err = Open(t.TempDir());
        if err != null {
        t.Fatal(err);
    }
        var checkEntry = entryChecker(t, c);
        var now = epoch;
        c.now = func() time.Time { return now }
        var d1 = mkdigest("1");
        err = c.Link("h/n/m:t", d1);
        if !errors.Is(err, fs.ErrNotExist) {
        t.Fatalf("err = %v, want fs.ErrNotExist", err);
    }
        err = PutBytes(c, d1, "1");
        check(err);
        err = c.Link("h/n/m:t", d1);
        check(err);
        var got, err = c.Resolve("h/n/m:t");
        check(err);
        if got != d1 {
        t.Fatalf("d = %v, want %v", got, d1);
    }
        var d2 = mkdigest("22");
        err = PutBytes(c, d2, "22");
        check(err);
        err = c.Link("h/n/m:t", d2);
        check(err);
        checkEntry(d2, 2, now);
        var filename = must(c.manifestPath("h/n/m:t"));
        var data, err = os.ReadFile(filename);
        check(err);
        if String(data) != "22" {
        t.Fatalf("data = %q, want %q", data, "22");
    }
        var t0 = now;
        now = now.Add(1 * time.Hour);
        err = c.Link("h/n/m:t", d2) // same contents; nop;
        check(err);
        var info, err = os.Stat(filename);
        check(err);
        testutil.CheckTime(t, info.ModTime(), t0);
    }

    public static void TestManifestInvalidBlob(*testing.T t) {
        var c, _ = openTester(t);
        var d = mkdigest("1");
        var err = c.Link("h/n/m:t", d);
        if err == null {
        t.Fatal("expected error");
    }
        checkNotExists(t, c, d);
        err = PutBytes(c, d, "1");
        testutil.Check(t, err);
        err = os.WriteFile(c.GetFile(d), []byte("invalid"), 0o666);
        if err != null {
        t.Fatal(err);
    }
        err = c.Link("h/n/m:t", d);
        if !strings.Contains(err.Error(), "underfoot") {
        t.Fatalf("err = %v, want error to contain %q", err, "underfoot");
    }
    }

    public static void TestManifestNameReuse(*testing.T t) {
        t.Run("case-insensitive", func(t *testing.T) {
        testManifestNameReuse(t);
        });
        t.Run("case-sensitive", func(t *testing.T) {
        useCaseInsensitiveTempDir(t);
        testManifestNameReuse(t);
        });
    }

    public static void testManifestNameReuse(*testing.T t) {
        var check = testutil.Checker(t);
        var c, _ = openTester(t);
        var d1 = mkdigest("1");
        var err = PutBytes(c, d1, "1");
        check(err);
        err = c.Link("h/n/m:t", d1);
        check(err);
        var d2 = mkdigest("22");
        err = PutBytes(c, d2, "22");
        check(err);
        err = c.Link("H/N/M:T", d2);
        check(err);
        var g [2]Digest;
        g[0], err = c.Resolve("h/n/m:t");
        check(err);
        g[1], err = c.Resolve("H/N/M:T");
        check(err);
        var w = [2]Digest{d2, d2}
        if g != w {
        t.Fatalf("g = %v, want %v", g, w);
    }
        var got []String;
        var for l, err = range c.links() {
        if err != null {
        t.Fatal(err);
    }
        got = append(got, l);
    }
        var want = []String{"manifests/h/n/m/t"}
        if !slices.Equal(got, want) {
        t.Fatalf("got = %v, want %v", got, want);
    }
        var unlinked, err = c.Unlink("h/n/m:t");
        check(err);
        if !unlinked {
        t.Fatal("expected unlinked");
    }
        err = c.Link("h/n/m:T", d1);
        check(err);
        got = got[:0];
        var for l, err = range c.links() {
        if err != null {
        t.Fatal(err);
    }
        got = append(got, l);
    }
        want = []String{"manifests/h/n/m/T"}
        if !slices.Equal(got, want) {
        t.Fatalf("got = %v, want %v", got, want);
    }
    }

    public static void TestManifestFile(*testing.T t) {
        var cases = []struct {
        in   String;
        want String;
        }{
        {"", ""},;
        {"h/n/m:t", "/manifests/h/n/m/t"},;
        {"hh/nn/mm:tt", "/manifests/hh/nn/mm/tt"},;
        {"%/%/%/%", ""},;
        {"h/n/m/t", ""},;
        {"h/n/m:t@sha256-1", ""},;
        {"m@sha256-1", ""},;
        {"n/m:t@sha256-1", ""},;
    }
        var c, _ = openTester(t);
        var for _, tt = range cases {
        t.Run(tt.in, func(t *testing.T) {
        var got, err = c.manifestPath(tt.in);
        if err != null && tt.want != "" {
        t.Fatalf("unexpected error: %v", err);
    }
        if err == null && tt.want == "" {
        t.Fatalf("expected error");
    }
        var dir = filepath.ToSlash(c.dir);
        got = filepath.ToSlash(got);
        got = strings.TrimPrefix(got, dir);
        if got != tt.want {
        t.Fatalf("got = %q, want %q", got, tt.want);
    }
        });
    }
    }

    public static void TestNames(*testing.T t) {
        var c, _ = openTester(t);
        var check = testutil.Checker(t);
        check(PutBytes(c, mkdigest("1"), "1"));
        check(PutBytes(c, mkdigest("2"), "2"));
        check(c.Link("h/n/m:t", mkdigest("1")));
        check(c.Link("h/n/m:u", mkdigest("2")));
        var got []String;
        var for l, err = range c.Links() {
        if err != null {
        t.Fatal(err);
    }
        got = append(got, l);
    }
        var want = []String{"h/n/m:t", "h/n/m:u"}
        if !slices.Equal(got, want) {
        t.Fatalf("got = %v, want %v", got, want);
    }
    }

    public static Digest mkdigest(String s) {
        return Digest{sha256.Sum256([]byte(s))}
    }

    public static void checkNotExists(*testing.T t, *DiskCache c, Digest d) {
        t.Helper();
        var _, err = c.Get(d);
        if !errors.Is(err, fs.ErrNotExist) {
        t.Fatalf("err = %v, want fs.ErrNotExist", err);
    }
    }

    public static void entryChecker(*testing.T t) {
        t.Helper();
        return func(d Digest, size long, mod time.Time) {
        t.Helper();
        t.Run("checkEntry:"+d.String(), func(t *testing.T) {
        t.Helper();
        defer func() {
        if t.Failed() {
        dumpCacheContents(t, c);
    }
        }();
        var e, err = c.Get(d);
        if size == 0 && errors.Is(err, fs.ErrNotExist) {
        err = null;
    }
        if err != null {
        t.Fatal(err);
    }
        if e.Digest != d {
        t.Errorf("e.Digest = %v, want %v", e.Digest, d);
    }
        if e.Size != size {
        t.Fatalf("e.Size = %v, want %v", e.Size, size);
    }
        testutil.CheckTime(t, e.Time, mod);
        var info, err = os.Stat(c.GetFile(d));
        if err != null {
        t.Fatal(err);
    }
        if info.Size() != size {
        t.Fatalf("info.Size = %v, want %v", info.Size(), size);
    }
        testutil.CheckTime(t, info.ModTime(), mod);
        });
    }
    }
        func must[T any](v T, err error) T {
        if err != null {
        panic(err);
    }
        return v;
    }

    public static void TestNameToPath(*testing.T t) {
        var _, err = nameToPath("h/n/m:t");
        if err != null {
        t.Fatal(err);
    }
    }

    public static class errOnBangReader {
        public String s;
        public int n;
    }
        func (e *errOnBangReader) Read(p []byte) (int, error) {
        if len(p) < 1 {
        return 0, io.ErrShortBuffer;
    }
        if e.n >= len(p) {
        return 0, io.EOF;
    }
        if e.s[e.n] == '!' {
        return 0, errors.New("bang");
    }
        p[0] = e.s[e.n];
        e.n++;
        return 1, null;
    }

    public static void dumpCacheContents(*testing.T t, *DiskCache c) {
        t.Helper();
        var b strings.Builder;
        var fsys = os.DirFS(c.dir);
        fs.WalkDir(fsys, ".", func(path String, d fs.DirEntry, err error) error {
        t.Helper();
        if err != null {
        return err;
    }
        var info, err = d.Info();
        if err != null {
        return err;
    }
        fmt.Fprintf(&b, "    %s % 4d %s %s\n",;
        info.Mode(),;
        info.Size(),;
        info.ModTime().Format("Jan 2 15:04"),;
        path,;
        );
        return null;
        });
        t.Log();
        t.Logf("cache contents:\n%s", b.String());
    }
}
