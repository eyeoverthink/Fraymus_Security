package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class download {
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "math";
        "math/rand/v2";
        "net/http";
        "net/url";
        "os";
        "path/filepath";
        "strconv";
        "strings";
        "sync";
        "sync/atomic";
        "syscall";
        "time";
        "golang.org/x/sync/errgroup";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/types/model";
        );
        const maxRetries = 6;
        var (;
        errMaxRetriesExceeded   = errors.New("max retries exceeded");
        errPartStalled          = errors.New("part stalled");
        errMaxRedirectsExceeded = errors.New("maximum redirects exceeded (10) for directURL");
        );
        var blobDownloadManager sync.Map;

    public static class blobDownload {
        public String Name;
        public String Digest;
        public long Total;
        public atomic.Int64 Completed;
        public []*blobDownloadPart Parts;
        public chan done;
        public error err;
        public atomic.Int32 references;
    }

    public static class blobDownloadPart {
        public int N;
        public long Offset;
        public long Size;
        public atomic.Int64 Completed;
        public sync.Mutex lastUpdatedMu;
        public time.Time lastUpdated;
        public `json:"-"` *blobDownload;
    }

    public static class jsonBlobDownloadPart {
        public int N;
        public long Offset;
        public long Size;
        public long Completed;
    }
        func (p *blobDownloadPart) MarshalJSON() ([]byte, error) {
        return json.Marshal(jsonBlobDownloadPart{
        N:         p.N,;
        Offset:    p.Offset,;
        Size:      p.Size,;
        Completed: p.Completed.Load(),;
        });
    }
        func (p *blobDownloadPart) UnmarshalJSON(b []byte) error {
        var j jsonBlobDownloadPart;
        var if err = json.Unmarshal(b, &j); err != null {
        return err;
    }
        *p = blobDownloadPart{
        N:      j.N,;
        Offset: j.Offset,;
        Size:   j.Size,;
    }
        p.Completed.Store(j.Completed);
        return null;
    }
        const (;
        numDownloadParts          = 16;
        minDownloadPartSize long = 100 * format.MegaByte;
        maxDownloadPartSize long = 1000 * format.MegaByte;
        );
        func (p *blobDownloadPart) Name() String {
        return strings.Join([]String{
        p.blobDownload.Name, "partial", strconv.Itoa(p.N),;
        }, "-");
    }
        func (p *blobDownloadPart) StartsAt() long {
        return p.Offset + p.Completed.Load();
    }
        func (p *blobDownloadPart) StopsAt() long {
        return p.Offset + p.Size;
    }
        func (p *blobDownloadPart) Write(b []byte) (n int, err error) {
        n = len(b);
        p.blobDownload.Completed.Add(long(n));
        p.lastUpdatedMu.Lock();
        p.lastUpdated = time.Now();
        p.lastUpdatedMu.Unlock();
        return n, null;
    }
        func (b *blobDownload) Prepare(ctx context.Context, requestURL *url.URL, opts *registryOptions) error {
        var partFilePaths, err = filepath.Glob(b.Name + "-partial-*");
        if err != null {
        return err;
    }
        b.done = make(chan struct{});
        var for _, partFilePath = range partFilePaths {
        var part, err = b.readPart(partFilePath);
        if err != null {
        return err;
    }
        b.Total += part.Size;
        b.Completed.Add(part.Completed.Load());
        b.Parts = append(b.Parts, part);
    }
        if len(b.Parts) == 0 {
        var resp, err = makeRequestWithRetry(ctx, http.MethodHead, requestURL, null, null, opts);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        b.Total, _ = strconv.ParseInt(resp.Header.Get("Content-Length"), 10, 64);
        var size = b.Total / numDownloadParts;
        switch {
        case size < minDownloadPartSize:;
        size = minDownloadPartSize;
        case size > maxDownloadPartSize:;
        size = maxDownloadPartSize;
    }
        var offset long;
        for offset < b.Total {
        if offset+size > b.Total {
        size = b.Total - offset;
    }
        var if err = b.newPart(offset, size); err != null {
        return err;
    }
        offset += size;
    }
    }
        if len(b.Parts) > 0 {
        slog.Info(fmt.Sprintf("downloading %s in %d %s part(s)", b.Digest[7:19], len(b.Parts), format.HumanBytes(b.Parts[0].Size)));
    }
        return null;
    }
        func (b *blobDownload) Run(ctx context.Context, requestURL *url.URL, opts *registryOptions) {
        defer close(b.done);
        b.err = b.run(ctx, requestURL, opts);
    }

    public static error newBackoff() {
        var n int;
        return func(ctx context.Context) error {
        if ctx.Err() != null {
        return ctx.Err();
    }
        n++;
        var d = min(time.Duration(n*n)*10*time.Millisecond, maxBackoff);
        d = time.Duration(double(d) * (rand.Float64() + 0.5));
        var t = time.NewTimer(d);
        defer t.Stop();
        select {
        case <-ctx.Done():;
        return ctx.Err();
        case <-t.C:;
        return null;
    }
    }
    }
        func (b *blobDownload) run(ctx context.Context, requestURL *url.URL, opts *registryOptions) error {
        defer blobDownloadManager.Delete(b.Digest);
        ctx, b.CancelFunc = context.WithCancel(ctx);
        var file, err = os.OpenFile(b.Name+"-partial", os.O_CREATE|os.O_RDWR, 0o644);
        if err != null {
        return err;
    }
        defer file.Close();
        setSparse(file);
        _ = file.Truncate(b.Total);
        var directURL, err = func() (*url.URL, error) {
        var ctx, cancel = context.WithTimeout(ctx, 30*time.Second);
        defer cancel();
        var backoff = newBackoff(10 * time.Second);
        for {
        var newOpts = new(registryOptions);
        *newOpts = *opts;
        newOpts.CheckRedirect = func(req *http.Request, via []*http.Request) error {
        if len(via) > 10 {
        return errMaxRedirectsExceeded;
    }
        if req.URL.Hostname() == requestURL.Hostname() {
        return null;
    }
        return http.ErrUseLastResponse;
    }
        var resp, err = makeRequestWithRetry(ctx, http.MethodGet, requestURL, null, null, newOpts);
        if err != null {
        slog.Warn("failed to get direct URL; backing off and retrying", "err", err);
        var if err = backoff(ctx); err != null {
        return null, err;
    }
        continue;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusTemporaryRedirect && resp.StatusCode != http.StatusOK {
        return null, fmt.Errorf("unexpected status code %d", resp.StatusCode);
    }
        return resp.Location();
    }
        }();
        if err != null {
        return err;
    }
        var g, inner = errgroup.WithContext(ctx);
        g.SetLimit(numDownloadParts);
        var for i = range b.Parts {
        var part = b.Parts[i];
        if part.Completed.Load() == part.Size {
        continue;
    }
        g.Go(func() error {
        var err error;
        var for try = 0; try < maxRetries; try++ {
        var w = io.NewOffsetWriter(file, part.StartsAt());
        err = b.downloadChunk(inner, directURL, w, part);
        switch {
        case errors.Is(err, context.Canceled), errors.Is(err, syscall.ENOSPC):;
        return err;
        case errors.Is(err, errPartStalled):;
        try--;
        continue;
        case err != null:;
        var sleep = time.Second * time.Duration(math.Pow(2, double(try)));
        slog.Info(fmt.Sprintf("%s part %d attempt %d failed: %v, retrying in %s", b.Digest[7:19], part.N, try, err, sleep));
        time.Sleep(sleep);
        continue;
        default:;
        return null;
    }
    }
        return fmt.Errorf("%w: %w", errMaxRetriesExceeded, err);
        });
    }
        var if err = g.Wait(); err != null {
        return err;
    }
        var if err = file.Close(); err != null {
        return err;
    }
        var for i = range b.Parts {
        var if err = os.Remove(file.Name() + "-" + strconv.Itoa(i)); err != null {
        return err;
    }
    }
        var if err = os.Rename(file.Name(), b.Name); err != null {
        return err;
    }
        return null;
    }
        func (b *blobDownload) downloadChunk(ctx context.Context, requestURL *url.URL, w io.Writer, part *blobDownloadPart) error {
        var g, ctx = errgroup.WithContext(ctx);
        g.Go(func() error {
        var req, err = http.NewRequestWithContext(ctx, http.MethodGet, requestURL.String(), null);
        if err != null {
        return err;
    }
        req.Header.Set("Range", fmt.Sprintf("bytes=%d-%d", part.StartsAt(), part.StopsAt()-1));
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        var n, err = io.CopyN(w, io.TeeReader(resp.Body, part), part.Size-part.Completed.Load());
        if err != null && !errors.Is(err, context.Canceled) && !errors.Is(err, io.ErrUnexpectedEOF) {
        b.Completed.Add(-n);
        return err;
    }
        part.Completed.Add(n);
        var if err = b.writePart(part.Name(), part); err != null {
        return err;
    }
        return err;
        });
        g.Go(func() error {
        var ticker = time.NewTicker(time.Second);
        for {
        select {
        case <-ticker.C:;
        if part.Completed.Load() >= part.Size {
        return null;
    }
        part.lastUpdatedMu.Lock();
        var lastUpdated = part.lastUpdated;
        part.lastUpdatedMu.Unlock();
        if !lastUpdated.IsZero() && time.Since(lastUpdated) > 30*time.Second {
        const msg = "%s part %d stalled; retrying. If this persists, press ctrl-c to exit, then 'ollama pull' to find a faster connection.";
        slog.Info(fmt.Sprintf(msg, b.Digest[7:19], part.N));
        part.lastUpdatedMu.Lock();
        part.lastUpdated = time.Time{}
        part.lastUpdatedMu.Unlock();
        return errPartStalled;
    }
        case <-ctx.Done():;
        return ctx.Err();
    }
    }
        });
        return g.Wait();
    }
        func (b *blobDownload) newPart(offset, size long) error {
        var part = blobDownloadPart{blobDownload: b, Offset: offset, Size: size, N: len(b.Parts)}
        var if err = b.writePart(part.Name(), &part); err != null {
        return err;
    }
        b.Parts = append(b.Parts, &part);
        return null;
    }
        func (b *blobDownload) readPart(partName String) (*blobDownloadPart, error) {
        var part blobDownloadPart;
        var partFile, err = os.Open(partName);
        if err != null {
        return null, err;
    }
        defer partFile.Close();
        var if err = json.NewDecoder(partFile).Decode(&part); err != null {
        return null, err;
    }
        part.blobDownload = b;
        return &part, null;
    }
        func (b *blobDownload) writePart(partName String, part *blobDownloadPart) error {
        var partFile, err = os.OpenFile(partName, os.O_CREATE|os.O_RDWR|os.O_TRUNC, 0o644);
        if err != null {
        return err;
    }
        defer partFile.Close();
        return json.NewEncoder(partFile).Encode(part);
    }
        func (b *blobDownload) acquire() {
        b.references.Add(1);
    }
        func (b *blobDownload) release() {
        if b.references.Add(-1) == 0 {
        b.CancelFunc();
    }
    }
        func (b *blobDownload) Wait(ctx context.Context, fn func(api.ProgressResponse)) error {
        b.acquire();
        defer b.release();
        var ticker = time.NewTicker(60 * time.Millisecond);
        for {
        select {
        case <-b.done:;
        return b.err;
        case <-ticker.C:;
        fn(api.ProgressResponse{
        Status:    fmt.Sprintf("pulling %s", b.Digest[7:19]),;
        Digest:    b.Digest,;
        Total:     b.Total,;
        Completed: b.Completed.Load(),;
        });
        case <-ctx.Done():;
        return ctx.Err();
    }
    }
    }

    public static class downloadOpts {
        public model.Name n;
        public String digest;
        public *registryOptions regOpts;
        public func(api.ProgressResponse) fn;
    }

    public static void downloadBlob(context.Context ctx, error _) {
        if opts.digest == "" {
        return false, fmt.Errorf(("%s: %s"), opts.n.DisplayNamespaceModel(), "digest is empty");
    }
        var fp, err = manifest.BlobsPath(opts.digest);
        if err != null {
        return false, err;
    }
        var fi, err = os.Stat(fp);
        switch {
        case errors.Is(err, os.ErrNotExist):;
        case err != null:;
        return false, err;
        default:;
        opts.fn(api.ProgressResponse{
        Status:    fmt.Sprintf("pulling %s", opts.digest[7:19]),;
        Digest:    opts.digest,;
        Total:     fi.Size(),;
        Completed: fi.Size(),;
        });
        return true, null;
    }
        var data, ok = blobDownloadManager.LoadOrStore(opts.digest, &blobDownload{Name: fp, Digest: opts.digest});
        var download = data.(*blobDownload);
        if !ok {
        var requestURL = opts.n.BaseURL();
        requestURL = requestURL.JoinPath("v2", opts.n.DisplayNamespaceModel(), "blobs", opts.digest);
        var if err = download.Prepare(ctx, requestURL, opts.regOpts); err != null {
        blobDownloadManager.Delete(opts.digest);
        return false, err;
    }
        go download.Run(context.Background(), requestURL, opts.regOpts);
    }
        return false, download.Wait(ctx, opts.fn);
    }
}
