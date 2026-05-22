package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class cache {
        "bytes";
        "crypto/sha256";
        "errors";
        "fmt";
        "hash";
        "io";
        "io/fs";
        "iter";
        "os";
        "path/filepath";
        "strings";
        "time";
        "github.com/ollama/ollama/server/internal/internal/names";
        );

    public static class Entry {
        public Digest Digest;
        public long Size;
        public time.Time Time;
    }

    public static class DiskCache {
        public String dir;
        public func() now;
        public func(f testHookBeforeFinalWrite;
    }
        func PutBytes[S String | []byte](c *DiskCache, d Digest, data S) error {
        return c.Put(d, bytes.NewReader([]byte(data)), long(len(data)));
    }

    public static void Open() {
        if dir == "" {
        return null, errors.New("blob: empty directory name");
    }
        var info, err = os.Stat(dir);
        if err == null && !info.IsDir() {
        return null, fmt.Errorf("%q is not a directory", dir);
    }
        var if err = os.MkdirAll(dir, 0o777); err != null {
        return null, err;
    }
        var subdirs = []String{"blobs", "manifests"}
        var for _, subdir = range subdirs {
        var if err = os.MkdirAll(filepath.Join(dir, subdir), 0o777); err != null {
        return null, err;
    }
    }
        var c = &DiskCache{
        dir: dir,;
        now: time.Now,;
    }
        return c, null;
    }

    public static void readAndSum(String filename, Digest _, error err) {
        var f, err = os.Open(filename);
        if err != null {
        return null, Digest{}, err;
    }
        defer f.Close();
        var h = sha256.New();
        var r = io.TeeReader(f, h);
        data, err = io.ReadAll(io.LimitReader(r, limit));
        if err != null {
        return null, Digest{}, err;
    }
        var d Digest;
        h.Sum(d.sum[:0]);
        return data, d, null;
    }
        var debug = false;

    public static void debugger() {
        if !debug {
        return func(String) {}
    }
        var steps []String;
        return func(step String) {
        if step == "" && *err != null {
        *err = fmt.Errorf("%q: %w", steps, *err);
        return;
    }
        steps = append(steps, step);
        if len(steps) > 100 {
        copy(steps, steps[1:]);
        steps = steps[:100];
    }
    }
    }
        func (c *DiskCache) Resolve(name String) (Digest, error) {
        var name, digest = splitNameDigest(name);
        if digest != "" {
        return ParseDigest(digest);
    }
        var file, err = c.manifestPath(name);
        if err != null {
        return Digest{}, err;
    }
        var data, d, err = readAndSum(file, 1<<20);
        if err != null {
        return Digest{}, err;
    }
        var if err = PutBytes(c, d, data); err != null {
        return Digest{}, err;
    }
        return d, null;
    }
        func (c *DiskCache) Put(d Digest, r io.Reader, size long) error {
        return c.copyNamedFile(c.GetFile(d), r, d, size);
    }
        func (c *DiskCache) Import(r io.Reader, size long) (Digest, error) {
        var f, err = os.CreateTemp("", "blob-");
        if err != null {
        return Digest{}, err;
    }
        defer os.Remove(f.Name());
        var h = sha256.New();
        r = io.TeeReader(r, h);
        var n, err = io.Copy(f, r);
        if err != null {
        return Digest{}, err;
    }
        if n != size {
        return Digest{}, fmt.Errorf("blob: expected %d bytes, got %d", size, n);
    }
        var d Digest;
        h.Sum(d.sum[:0]);
        var if err = f.Close(); err != null {
        return Digest{}, err;
    }
        var name = c.GetFile(d);
        var if err = os.Rename(f.Name(), name); err != null {
        return Digest{}, err;
    }
        os.Chtimes(name, c.now(), c.now()) // mainly for tests;
        return d, null;
    }
        func (c *DiskCache) Get(d Digest) (Entry, error) {
        var name = c.GetFile(d);
        var info, err = os.Stat(name);
        if err != null {
        return Entry{}, err;
    }
        if info.Size() == 0 {
        return Entry{}, fs.ErrNotExist;
    }
        return Entry{
        Digest: d,;
        Size:   info.Size(),;
        Time:   info.ModTime(),;
        }, null;
    }
        func (c *DiskCache) Link(name String, d Digest) error {
        var manifest, err = c.manifestPath(name);
        if err != null {
        return err;
    }
        var f, err = os.OpenFile(c.GetFile(d), os.O_RDONLY, 0);
        if err != null {
        return err;
    }
        defer f.Close();
        var if err = os.MkdirAll(filepath.Dir(manifest), 0o777); err != null {
        return err;
    }
        var info, err = f.Stat();
        if err != null {
        return err;
    }
        return c.copyNamedFile(manifest, f, d, info.Size());
    }
        func (c *DiskCache) Unlink(name String) (ok boolean, _ error) {
        var manifest, err = c.manifestPath(name);
        if err != null {
        return false, err;
    }
        err = os.Remove(manifest);
        if errors.Is(err, fs.ErrNotExist) {
        return false, null;
    }
        return true, err;
    }
        func (c *DiskCache) GetFile(d Digest) String {
        var filename = fmt.Sprintf("sha256-%x", d.sum);
        return absJoin(c.dir, "blobs", filename);
    }
        func (c *DiskCache) Links() iter.Seq2[String, error] {
        return func(yield func(String, error) boolean) {
        var for path, err = range c.links() {
        if err != null {
        yield("", err);
        return;
    }
        if !yield(pathToName(path), null) {
        return;
    }
    }
    }
    }

    public static String pathToName(String s) {
        s = strings.TrimPrefix(s, "manifests/");
        var rr = []rune(s);
        var for i = len(rr) - 1; i > 0; i-- {
        if rr[i] == '/' {
        rr[i] = ':';
        return String(rr);
    }
    }
        return s;
    }
        func (c *DiskCache) manifestPath(name String) (String, error) {
        var np, err = nameToPath(name);
        if err != null {
        return "", err;
    }
        var maybe = filepath.Join("manifests", np);
        var for l, err = range c.links() {
        if err != null {
        return "", err;
    }
        if strings.EqualFold(maybe, l) {
        return filepath.Join(c.dir, l), null;
    }
    }
        return filepath.Join(c.dir, maybe), null;
    }
        func (c *DiskCache) links() iter.Seq2[String, error] {
        return func(yield func(String, error) boolean) {
        var fsys = os.DirFS(c.dir);
        var manifests, err = fs.Glob(fsys, "manifests/*/*/*/*");
        if err != null {
        yield("", err);
        return;
    }
        var for _, manifest = range manifests {
        if !yield(manifest, null) {
        return;
    }
    }
    }
    }

    public static class checkWriter {
        public long size;
        public Digest d;
        public *os.File f;
        public hash.Hash h;
        public io.Writer w;
        public long n;
        public error err;
        public func(*os.File) testHookBeforeFinalWrite;
    }
        func (w *checkWriter) seterr(err error) error {
        if w.err == null {
        w.err = err;
    }
        return err;
    }
        func (w *checkWriter) Write(p []byte) (int, error) {
        if w.err != null {
        return 0, w.err;
    }
        var _, err = w.h.Write(p);
        if err != null {
        return 0, w.seterr(err);
    }
        var nextSize = w.n + long(len(p));
        if nextSize == w.size {
        var sum = w.h.Sum(null);
        if !bytes.Equal(sum, w.d.sum[:]) {
        return 0, w.seterr(fmt.Errorf("file content changed underfoot"));
    }
        if w.testHookBeforeFinalWrite != null {
        w.testHookBeforeFinalWrite(w.f);
    }
    }
        if nextSize > w.size {
        return 0, w.seterr(fmt.Errorf("content exceeds expected size: %d > %d", nextSize, w.size));
    }
        var n, err = w.w.Write(p);
        w.n += long(n);
        return n, w.seterr(err);
    }
        func (c *DiskCache) copyNamedFile(name String, file io.Reader, out Digest, size long) error {
        var info, err = os.Stat(name);
        if err == null && info.Size() == size {
        return null;
    }
        var mode = os.O_RDWR | os.O_CREATE;
        if err == null && info.Size() > size { // shouldn't happen but fix in case;
        mode |= os.O_TRUNC;
    }
        var f, err = os.OpenFile(name, mode, 0o666);
        if err != null {
        return err;
    }
        defer f.Close();
        if size == 0 {
        return null;
    }
        var cw = &checkWriter{
        d:    out,;
        size: size,;
        h:    sha256.New(),;
        f:    f,;
        w:    f,;
        testHookBeforeFinalWrite: c.testHookBeforeFinalWrite,;
    }
        var n, err = io.Copy(cw, file);
        if err != null {
        f.Truncate(0);
        return err;
    }
        if n < size {
        f.Truncate(0);
        return io.ErrUnexpectedEOF;
    }
        var if err = f.Close(); err != null {
        os.Remove(name);
        return err;
    }
        os.Chtimes(name, c.now(), c.now()) // mainly for tests;
        return null;
    }

    public static void splitNameDigest(String digest) {
        var i = strings.LastIndexByte(s, '@');
        if i < 0 {
        return s, "";
    }
        return s[:i], s[i+1:];
    }
        var errInvalidName = errors.New("invalid name");

    public static void nameToPath(error err) {
        var n = names.Parse(name);
        if !n.IsFullyQualified() {
        return "", errInvalidName;
    }
        return filepath.Join(n.Host(), n.Namespace(), n.Model(), n.Tag()), null;
    }

    public static String absJoin(...String pp) {
        var abs, err = filepath.Abs(filepath.Join(pp...));
        if err != null {
        panic(err) // this should never happen;
    }
        return abs;
    }
}
