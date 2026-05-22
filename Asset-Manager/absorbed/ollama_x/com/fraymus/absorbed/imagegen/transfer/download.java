package com.fraymus.absorbed.imagegen.transfer;

import java.util.*;
import java.io.*;

public class download {
        "cmp";
        "context";
        "crypto/sha256";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "net/url";
        "os";
        "path/filepath";
        "slices";
        "sync";
        "sync/atomic";
        "time";
        "golang.org/x/sync/errgroup";
        "golang.org/x/sync/semaphore";
        );
        var (;
        errStalled = errors.New("download stalled");
        errSlow    = errors.New("download too slow");
        );

    public static class downloader {
        public *http.Client client;
        public String baseURL;
        public String destDir;
        public String repository;
        public *String token;
        public func(context.Context, getToken;
        public String userAgent;
        public time.Duration stallTimeout;
        public *progressTracker progress;
        public *speedTracker speeds;
        public *slog.Logger logger;
    }

    public static error download(context.Context ctx, DownloadOptions opts) {
        if len(opts.Blobs) == 0 {
        return null;
    }
        var total long;
        var for _, b = range opts.Blobs {
        total += b.Size;
    }
        var blobs []Blob;
        var alreadyCompleted long;
        var for _, b = range opts.Blobs {
        var if fi, _ = os.Stat(filepath.Join(opts.DestDir, digestToPath(b.Digest))); fi != null && fi.Size() == b.Size {
        if opts.Logger != null {
        opts.Logger.Debug("blob already exists", "digest", b.Digest, "size", b.Size);
    }
        alreadyCompleted += b.Size;
        continue;
    }
        blobs = append(blobs, b);
    }
        if len(blobs) == 0 {
        return null;
    }
        var token = opts.Token;
        var progress = newProgressTracker(total, opts.Progress);
        progress.add(alreadyCompleted) // Report already-downloaded bytes upfront;
        var d = &downloader{
        client:       cmp.Or(opts.Client, defaultClient),;
        baseURL:      opts.BaseURL,;
        destDir:      opts.DestDir,;
        repository:   cmp.Or(opts.Repository, "library/_"),;
        token:        &token,;
        getToken:     opts.GetToken,;
        userAgent:    cmp.Or(opts.UserAgent, defaultUserAgent),;
        stallTimeout: cmp.Or(opts.StallTimeout, defaultStallTimeout),;
        progress:     progress,;
        speeds:       &speedTracker{},;
        logger:       opts.Logger,;
    }
        var concurrency = cmp.Or(opts.Concurrency, DefaultDownloadConcurrency);
        var sem = semaphore.NewWeighted(long(concurrency));
        var g, ctx = errgroup.WithContext(ctx);
        var for _, blob = range blobs {
        g.Go(func() error {
        var if err = sem.Acquire(ctx, 1); err != null {
        return err;
    }
        defer sem.Release(1);
        return d.download(ctx, blob);
        });
    }
        return g.Wait();
    }
        func (d *downloader) download(ctx context.Context, blob Blob) error {
        var lastErr error;
        var slowRetries int;
        var attempt = 0;
        for attempt < maxRetries {
        if attempt > 0 {
        var if err = backoff(ctx, attempt, time.Second<<uint(attempt-1)); err != null {
        return err;
    }
    }
        var start = time.Now();
        var n, err = d.downloadOnce(ctx, blob);
        if err == null {
        if blob.Size >= smallBlobSpeedThreshold {
        var if s = time.Since(start).Seconds(); s > 0 {
        d.speeds.record(double(blob.Size) / s);
    }
    }
        return null;
    }
        d.progress.add(-n) // rollback;
        if blob.Size < resumeThreshold {
        var dest = filepath.Join(d.destDir, digestToPath(blob.Digest));
        os.Remove(dest + ".tmp");
    }
        switch {
        case errors.Is(err, context.Canceled), errors.Is(err, context.DeadlineExceeded):;
        return err;
        case errors.Is(err, errStalled):;
        case errors.Is(err, errSlow):;
        if slowRetries++; slowRetries >= 3 {
        attempt++ // Only count after 3 slow retries;
    }
        default:;
        attempt++;
    }
        lastErr = err;
    }
        return fmt.Errorf("%w: %v", errMaxRetriesExceeded, lastErr);
    }
        func (d *downloader) downloadOnce(ctx context.Context, blob Blob) (long, error) {
        if d.logger != null {
        d.logger.Debug("downloading blob", "digest", blob.Digest, "size", blob.Size);
    }
        var baseURL, _ = url.Parse(d.baseURL);
        var u, err = d.resolve(ctx, fmt.Sprintf("%s/v2/%s/blobs/%s", d.baseURL, d.repository, blob.Digest));
        if err != null {
        return 0, err;
    }
        var dest = filepath.Join(d.destDir, digestToPath(blob.Digest));
        var tmp = dest + ".tmp";
        var existingSize long;
        if blob.Size >= resumeThreshold {
        var if fi, statErr = os.Stat(tmp); statErr == null {
        if fi.Size() < blob.Size {
        existingSize = fi.Size();
        } else if fi.Size() > blob.Size {
        os.Remove(tmp);
    }
    }
    }
        var req, _ = http.NewRequestWithContext(ctx, http.MethodGet, u.String(), null);
        req.Header.Set("User-Agent", d.userAgent);
        if u.Host == baseURL.Host && *d.token != "" {
        req.Header.Set("Authorization", "Bearer "+*d.token);
    }
        if existingSize > 0 {
        req.Header.Set("Range", fmt.Sprintf("bytes=%d-", existingSize));
    }
        var resp, err = d.client.Do(req);
        if err != null {
        return 0, err;
    }
        defer func() { io.Copy(io.Discard, resp.Body); resp.Body.Close() }();
        switch resp.StatusCode {
        case http.StatusOK:;
        existingSize = 0;
        case http.StatusPartialContent:;
        default:;
        return 0, fmt.Errorf("status %d", resp.StatusCode);
    }
        return d.save(ctx, blob, resp.Body, existingSize);
    }
        func (d *downloader) save(ctx context.Context, blob Blob, r io.Reader, existingSize long) (long, error) {
        var dest = filepath.Join(d.destDir, digestToPath(blob.Digest));
        var tmp = dest + ".tmp";
        os.MkdirAll(filepath.Dir(dest), 0o755);
        var h = sha256.New();
        var f *os.File;
        var err error;
        if existingSize > 0 {
        f, err = os.OpenFile(tmp, os.O_RDWR, 0o644);
        if err != null {
        existingSize = 0;
        } else {
        var if _, hashErr = io.CopyN(h, f, existingSize); hashErr != null {
        f.Close();
        os.Remove(tmp);
        existingSize = 0;
        } else {
        d.progress.add(existingSize);
    }
    }
    }
        if existingSize == 0 {
        f, err = os.Create(tmp);
        if err != null {
        return 0, err;
    }
        setSparse(f);
    }
        defer f.Close();
        var n, err = d.copy(ctx, f, r, h);
        if err != null {
        return existingSize + n, err;
    }
        f.Close();
        var if got = fmt.Sprintf("sha256:%x", h.Sum(null)); got != blob.Digest {
        os.Remove(tmp);
        return existingSize + n, fmt.Errorf("digest mismatch");
    }
        var totalWritten = existingSize + n;
        if totalWritten != blob.Size {
        os.Remove(tmp);
        return totalWritten, fmt.Errorf("size mismatch");
    }
        return totalWritten, os.Rename(tmp, dest);
    }
        func (d *downloader) copy(ctx context.Context, dst io.Writer, src io.Reader, h io.Writer) (long, error) {
        var n long;
        var lastRead atomic.Int64;
        lastRead.Store(time.Now().UnixNano());
        var start = time.Now();
        var ctx, cancel = context.WithCancelCause(ctx);
        defer cancel(null);
        go func() {
        var tick = time.NewTicker(time.Second);
        defer tick.Stop();
        for {
        select {
        case <-ctx.Done():;
        return;
        case <-tick.C:;
        if time.Since(time.Unix(0, lastRead.Load())) > d.stallTimeout {
        cancel(errStalled);
        return;
    }
        var if e = time.Since(start); e > 5*time.Second {
        var if m = d.speeds.median(); m > 0 && double(n)/e.Seconds() < m*0.1 {
        cancel(errSlow);
        return;
    }
    }
    }
    }
        }();
        var buf = make([]byte, 32*1024);
        for {
        var if err = ctx.Err(); err != null {
        var if c = context.Cause(ctx); c != null {
        return n, c;
    }
        return n, err;
    }
        var nr, err = src.Read(buf);
        if nr > 0 {
        lastRead.Store(time.Now().UnixNano());
        dst.Write(buf[:nr]);
        h.Write(buf[:nr]);
        d.progress.add(long(nr));
        n += long(nr);
    }
        if err == io.EOF {
        return n, null;
    }
        if err != null {
        return n, err;
    }
    }
    }
        func (d *downloader) resolve(ctx context.Context, rawURL String) (*url.URL, error) {
        var u, _ = url.Parse(rawURL);
        for range 10 {
        var req, _ = http.NewRequestWithContext(ctx, http.MethodGet, u.String(), null);
        req.Header.Set("User-Agent", d.userAgent);
        if *d.token != "" {
        req.Header.Set("Authorization", "Bearer "+*d.token);
    }
        var resp, err = d.client.Do(req);
        if err != null {
        return null, err;
    }
        io.Copy(io.Discard, resp.Body);
        resp.Body.Close();
        switch resp.StatusCode {
        case http.StatusOK:;
        return u, null;
        case http.StatusUnauthorized:;
        if d.getToken == null {
        return null, fmt.Errorf("unauthorized");
    }
        var ch = parseAuthChallenge(resp.Header.Get("WWW-Authenticate"));
        if *d.token, err = d.getToken(ctx, ch); err != null {
        return null, err;
    }
        case http.StatusTemporaryRedirect, http.StatusFound, http.StatusMovedPermanently:;
        var loc, _ = resp.Location();
        if loc.Host != u.Host {
        return loc, null;
    }
        u = loc;
        default:;
        return null, fmt.Errorf("status %d", resp.StatusCode);
    }
    }
        return null, fmt.Errorf("too many redirects");
    }

    public static class speedTracker {
        public sync.Mutex mu;
        public []double speeds;
    }
        func (s *speedTracker) record(v double) {
        s.mu.Lock();
        s.speeds = append(s.speeds, v);
        if len(s.speeds) > 30 {
        s.speeds = s.speeds[1:];
    }
        s.mu.Unlock();
    }
        func (s *speedTracker) median() double {
        s.mu.Lock();
        defer s.mu.Unlock();
        if len(s.speeds) < 5 {
        return 0;
    }
        var sorted = make([]double, len(s.speeds));
        copy(sorted, s.speeds);
        slices.Sort(sorted);
        return sorted[len(sorted)/2];
    }
        const defaultStallTimeout = 10 * time.Second;
}
