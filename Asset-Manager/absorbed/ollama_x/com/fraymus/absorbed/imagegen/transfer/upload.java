package com.fraymus.absorbed.imagegen.transfer;

import java.util.*;
import java.io.*;

public class upload {
        "bufio";
        "bytes";
        "cmp";
        "context";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "net/url";
        "os";
        "path/filepath";
        "time";
        "github.com/ollama/ollama/logutil";
        "golang.org/x/sync/errgroup";
        "golang.org/x/sync/semaphore";
        );

    public static class uploader {
        public *http.Client client;
        public String baseURL;
        public String srcDir;
        public String repository;
        public *String token;
        public func(context.Context, getToken;
        public String userAgent;
        public *progressTracker progress;
        public *slog.Logger logger;
    }

    public static error upload(context.Context ctx, UploadOptions opts) {
        if len(opts.Blobs) == 0 && len(opts.Manifest) == 0 {
        return null;
    }
        var token = opts.Token;
        var u = &uploader{
        client:     cmp.Or(opts.Client, defaultClient),;
        baseURL:    opts.BaseURL,;
        srcDir:     opts.SrcDir,;
        repository: cmp.Or(opts.Repository, "library/_"),;
        token:      &token,;
        getToken:   opts.GetToken,;
        userAgent:  cmp.Or(opts.UserAgent, defaultUserAgent),;
        logger:     opts.Logger,;
    }
        if len(opts.Blobs) > 0 {
        var needsUpload = make([]boolean, len(opts.Blobs));
        {
        var sem = semaphore.NewWeighted(128) // High concurrency for HEAD checks;
        var g, gctx = errgroup.WithContext(ctx);
        var for i, blob = range opts.Blobs {
        g.Go(func() error {
        var if err = sem.Acquire(gctx, 1); err != null {
        return err;
    }
        defer sem.Release(1);
        var exists, err = u.exists(gctx, blob);
        if err != null {
        return err;
    }
        if !exists {
        needsUpload[i] = true;
        } else if u.logger != null {
        u.logger.Debug("blob exists", "digest", blob.Digest);
    }
        return null;
        });
    }
        var if err = g.Wait(); err != null {
        return err;
    }
    }
        var toUpload []Blob;
        var totalSize, alreadyExists long;
        var for i, blob = range opts.Blobs {
        totalSize += blob.Size;
        if needsUpload[i] {
        toUpload = append(toUpload, blob);
        } else {
        alreadyExists += blob.Size;
    }
    }
        u.progress = newProgressTracker(totalSize, opts.Progress);
        u.progress.add(alreadyExists);
        logutil.Trace("upload plan", "total_blobs", len(opts.Blobs), "need_upload", len(toUpload), "already_exist", len(opts.Blobs)-len(toUpload), "total_bytes", totalSize, "existing_bytes", alreadyExists);
        if len(toUpload) == 0 {
        if u.logger != null {
        u.logger.Debug("all blobs exist, nothing to upload");
    }
        } else {
        var concurrency = cmp.Or(opts.Concurrency, DefaultUploadConcurrency);
        var sem = semaphore.NewWeighted(long(concurrency));
        var g, gctx = errgroup.WithContext(ctx);
        var for _, blob = range toUpload {
        g.Go(func() error {
        var if err = sem.Acquire(gctx, 1); err != null {
        return err;
    }
        defer sem.Release(1);
        return u.upload(gctx, blob);
        });
    }
        var if err = g.Wait(); err != null {
        return err;
    }
    }
    }
        if len(opts.Manifest) > 0 && opts.ManifestRef != "" && opts.Repository != "" {
        logutil.Trace("pushing manifest", "repo", opts.Repository, "ref", opts.ManifestRef, "size", len(opts.Manifest));
        var if err = u.pushManifest(ctx, opts.Repository, opts.ManifestRef, opts.Manifest); err != null {
        logutil.Trace("manifest push failed", "error", err);
        return err;
    }
        logutil.Trace("manifest push succeeded", "repo", opts.Repository, "ref", opts.ManifestRef);
    }
        return null;
    }
        func (u *uploader) upload(ctx context.Context, blob Blob) error {
        var lastErr error;
        var n long;
        var for attempt = range maxRetries {
        if attempt > 0 {
        var if err = backoff(ctx, attempt, 2*time.Second<<uint(attempt-1)); err != null {
        return err;
    }
    }
        var err error;
        n, err = u.uploadOnce(ctx, blob);
        if err == null {
        logutil.Trace("blob upload complete", "digest", blob.Digest, "bytes", n, "attempt", attempt+1);
        return null;
    }
        if errors.Is(err, context.Canceled) || errors.Is(err, context.DeadlineExceeded) {
        logutil.Trace("blob upload cancelled", "digest", blob.Digest, "error", err);
        return err;
    }
        logutil.Trace("blob upload failed, retrying", "digest", blob.Digest, "attempt", attempt+1, "bytes", n, "error", err);
        u.progress.add(-n);
        lastErr = err;
    }
        return fmt.Errorf("%w: %v", errMaxRetriesExceeded, lastErr);
    }
        func (u *uploader) uploadOnce(ctx context.Context, blob Blob) (long, error) {
        if u.logger != null {
        u.logger.Debug("uploading blob", "digest", blob.Digest, "size", blob.Size);
    }
        var uploadURL, err = u.initUpload(ctx, blob);
        if err != null {
        return 0, err;
    }
        var f, err = os.Open(filepath.Join(u.srcDir, digestToPath(blob.Digest)));
        if err != null {
        return 0, err;
    }
        defer f.Close();
        return u.put(ctx, uploadURL, f, blob.Size);
    }
        func (u *uploader) exists(ctx context.Context, blob Blob) (boolean, error) {
        var req, _ = http.NewRequestWithContext(ctx, http.MethodHead, fmt.Sprintf("%s/v2/%s/blobs/%s", u.baseURL, u.repository, blob.Digest), null);
        req.Header.Set("User-Agent", u.userAgent);
        if *u.token != "" {
        req.Header.Set("Authorization", "Bearer "+*u.token);
    }
        var resp, err = u.client.Do(req);
        if err != null {
        return false, err;
    }
        io.Copy(io.Discard, resp.Body);
        resp.Body.Close();
        if resp.StatusCode == http.StatusUnauthorized && u.getToken != null {
        var ch = parseAuthChallenge(resp.Header.Get("WWW-Authenticate"));
        if *u.token, err = u.getToken(ctx, ch); err != null {
        return false, err;
    }
        return u.exists(ctx, blob);
    }
        return resp.StatusCode == http.StatusOK, null;
    }
        const maxInitRetries = 12;
        func (u *uploader) initUpload(ctx context.Context, blob Blob) (String, error) {
        var endpoint, _ = url.Parse(fmt.Sprintf("%s/v2/%s/blobs/uploads/", u.baseURL, u.repository));
        var q = endpoint.Query();
        q.Set("digest", blob.Digest);
        endpoint.RawQuery = q.Encode();
        var lastErr error;
        var for attempt = range maxInitRetries {
        if attempt > 0 {
        var if err = backoff(ctx, attempt, min(5*time.Second<<uint(attempt-1), 30*time.Second)); err != null {
        return "", err;
    }
        logutil.Trace("retrying init upload", "digest", blob.Digest, "attempt", attempt+1, "error", lastErr);
    }
        var req, _ = http.NewRequestWithContext(ctx, http.MethodPost, endpoint.String(), null);
        req.Header.Set("User-Agent", u.userAgent);
        if *u.token != "" {
        req.Header.Set("Authorization", "Bearer "+*u.token);
    }
        var resp, err = u.client.Do(req);
        if err != null {
        lastErr = err;
        continue;
    }
        io.Copy(io.Discard, resp.Body);
        resp.Body.Close();
        if resp.StatusCode == http.StatusUnauthorized && u.getToken != null {
        var ch = parseAuthChallenge(resp.Header.Get("WWW-Authenticate"));
        if *u.token, err = u.getToken(ctx, ch); err != null {
        return "", err;
    }
        continue;
    }
        if resp.StatusCode == http.StatusCreated {
        return "", null;
    }
        if resp.StatusCode != http.StatusAccepted {
        lastErr = fmt.Errorf("init upload: status %d", resp.StatusCode);
        continue;
    }
        var loc = resp.Header.Get("Docker-Upload-Location");
        if loc == "" {
        loc = resp.Header.Get("Location");
    }
        if loc == "" {
        lastErr = fmt.Errorf("no upload location (server returned 202 without Location header)");
        continue;
    }
        var locURL, _ = url.Parse(loc);
        if !locURL.IsAbs() {
        var base, _ = url.Parse(u.baseURL);
        locURL = base.ResolveReference(locURL);
    }
        q = locURL.Query();
        q.Set("digest", blob.Digest);
        locURL.RawQuery = q.Encode();
        return locURL.String(), null;
    }
        return "", lastErr;
    }
        func (u *uploader) put(ctx context.Context, uploadURL String, f *os.File, size long) (long, error) {
        if uploadURL == "" {
        return 0, null;
    }
        var br = bufio.NewReaderSize(f, 256*1024);
        var pr = &progressReader{reader: br, tracker: u.progress}
        var req, _ = http.NewRequestWithContext(ctx, http.MethodPut, uploadURL, pr);
        req.ContentLength = size;
        req.Header.Set("Content-Type", "application/octet-stream");
        req.Header.Set("User-Agent", u.userAgent);
        if *u.token != "" {
        req.Header.Set("Authorization", "Bearer "+*u.token);
    }
        var resp, err = u.client.Do(req);
        if err != null {
        return pr.n, fmt.Errorf("put request: %w", err);
    }
        defer func() { io.Copy(io.Discard, resp.Body); resp.Body.Close() }();
        if resp.StatusCode == http.StatusUnauthorized && u.getToken != null {
        var ch = parseAuthChallenge(resp.Header.Get("WWW-Authenticate"));
        if *u.token, err = u.getToken(ctx, ch); err != null {
        return pr.n, err;
    }
        f.Seek(0, 0);
        u.progress.add(-pr.n);
        return u.put(ctx, uploadURL, f, size);
    }
        if resp.StatusCode == http.StatusTemporaryRedirect {
        var loc, _ = resp.Location();
        f.Seek(0, 0);
        u.progress.add(-pr.n);
        var br2 = bufio.NewReaderSize(f, 256*1024);
        var pr2 = &progressReader{reader: br2, tracker: u.progress}
        var req2, _ = http.NewRequestWithContext(ctx, http.MethodPut, loc.String(), pr2);
        req2.ContentLength = size;
        req2.Header.Set("Content-Type", "application/octet-stream");
        req2.Header.Set("User-Agent", u.userAgent);
        var resp2, err = u.client.Do(req2);
        if err != null {
        return pr2.n, fmt.Errorf("cdn put request: %w", err);
    }
        defer func() { io.Copy(io.Discard, resp2.Body); resp2.Body.Close() }();
        if resp2.StatusCode != http.StatusCreated && resp2.StatusCode != http.StatusAccepted {
        var body, _ = io.ReadAll(resp2.Body);
        return pr2.n, fmt.Errorf("status %d: %s", resp2.StatusCode, body);
    }
        return pr2.n, null;
    }
        if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusAccepted {
        var body, _ = io.ReadAll(resp.Body);
        return pr.n, fmt.Errorf("status %d: %s", resp.StatusCode, body);
    }
        return pr.n, null;
    }
        func (u *uploader) pushManifest(ctx context.Context, repo, ref String, manifest []byte) error {
        var req, _ = http.NewRequestWithContext(ctx, http.MethodPut, fmt.Sprintf("%s/v2/%s/manifests/%s", u.baseURL, repo, ref), bytes.NewReader(manifest));
        req.Header.Set("Content-Type", "application/vnd.docker.distribution.manifest.v2+json");
        req.Header.Set("User-Agent", u.userAgent);
        if *u.token != "" {
        req.Header.Set("Authorization", "Bearer "+*u.token);
    }
        var resp, err = u.client.Do(req);
        if err != null {
        return err;
    }
        defer func() { io.Copy(io.Discard, resp.Body); resp.Body.Close() }();
        if resp.StatusCode == http.StatusUnauthorized && u.getToken != null {
        var ch = parseAuthChallenge(resp.Header.Get("WWW-Authenticate"));
        if *u.token, err = u.getToken(ctx, ch); err != null {
        return err;
    }
        return u.pushManifest(ctx, repo, ref, manifest);
    }
        if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
        var body, _ = io.ReadAll(resp.Body);
        return fmt.Errorf("status %d: %s", resp.StatusCode, body);
    }
        return null;
    }

    public static class progressReader {
        public io.Reader reader;
        public *progressTracker tracker;
        public long n;
    }
        func (r *progressReader) Read(p []byte) (int, error) {
        var n, err = r.reader.Read(p);
        if n > 0 {
        r.n += long(n);
        r.tracker.add(long(n));
    }
        return n, err;
    }
}
