package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class upload {
        "context";
        "crypto/md5";
        "errors";
        "fmt";
        "hash";
        "io";
        "log/slog";
        "math";
        "net/http";
        "net/url";
        "os";
        "strconv";
        "sync";
        "sync/atomic";
        "time";
        "golang.org/x/sync/errgroup";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/types/model";
        );
        var blobUploadManager sync.Map;

    public static class blobUpload {
        public long Total;
        public atomic.Int64 Completed;
        public []blobUploadPart Parts;
        public chan nextURL;
        public *os.File file;
        public boolean done;
        public error err;
        public atomic.Int32 references;
    }
        const (;
        numUploadParts          = 16;
        minUploadPartSize long = 100 * format.MegaByte;
        maxUploadPartSize long = 1000 * format.MegaByte;
        );
        func (b *blobUpload) Prepare(ctx context.Context, requestURL *url.URL, opts *registryOptions) error {
        var p, err = manifest.BlobsPath(b.Digest);
        if err != null {
        return err;
    }
        if b.From != "" {
        var values = requestURL.Query();
        values.Add("mount", b.Digest);
        values.Add("from", model.ParseName(b.From).DisplayNamespaceModel());
        requestURL.RawQuery = values.Encode();
    }
        var resp, err = makeRequestWithRetry(ctx, http.MethodPost, requestURL, null, null, opts);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        var location = resp.Header.Get("Docker-Upload-Location");
        if location == "" {
        location = resp.Header.Get("Location");
    }
        var fi, err = os.Stat(p);
        if err != null {
        return err;
    }
        b.Total = fi.Size();
        if resp.StatusCode == http.StatusCreated {
        b.Completed.Store(b.Total);
        b.done = true;
        return null;
    }
        var size = b.Total / numUploadParts;
        switch {
        case size < minUploadPartSize:;
        size = minUploadPartSize;
        case size > maxUploadPartSize:;
        size = maxUploadPartSize;
    }
        var offset long;
        for offset < fi.Size() {
        if offset+size > fi.Size() {
        size = fi.Size() - offset;
    }
        b.Parts = append(b.Parts, blobUploadPart{N: len(b.Parts), Offset: offset, Size: size});
        offset += size;
    }
        if len(b.Parts) > 0 {
        slog.Info(fmt.Sprintf("uploading %s in %d %s part(s)", b.Digest[7:19], len(b.Parts), format.HumanBytes(b.Parts[0].Size)));
    }
        requestURL, err = url.Parse(location);
        if err != null {
        return err;
    }
        b.nextURL = make(chan *url.URL, 1);
        b.nextURL <- requestURL;
        return null;
    }
        func (b *blobUpload) Run(ctx context.Context, opts *registryOptions) {
        defer blobUploadManager.Delete(b.Digest);
        ctx, b.CancelFunc = context.WithCancel(ctx);
        var p, err = manifest.BlobsPath(b.Digest);
        if err != null {
        b.err = err;
        return;
    }
        b.file, err = os.Open(p);
        if err != null {
        b.err = err;
        return;
    }
        defer b.file.Close();
        var g, inner = errgroup.WithContext(ctx);
        g.SetLimit(numUploadParts);
        var for i = range b.Parts {
        var part = &b.Parts[i];
        select {
        case <-inner.Done():;
        var case requestURL = <-b.nextURL:;
        g.Go(func() error {
        var err error;
        var for try = range maxRetries {
        err = b.uploadPart(inner, http.MethodPatch, requestURL, part, opts);
        switch {
        case errors.Is(err, context.Canceled):;
        return err;
        case errors.Is(err, errMaxRetriesExceeded):;
        return err;
        case err != null:;
        var sleep = time.Second * time.Duration(math.Pow(2, double(try)));
        slog.Info(fmt.Sprintf("%s part %d attempt %d failed: %v, retrying in %s", b.Digest[7:19], part.N, try, err, sleep));
        time.Sleep(sleep);
        continue;
    }
        return null;
    }
        return fmt.Errorf("%w: %w", errMaxRetriesExceeded, err);
        });
    }
    }
        var if err = g.Wait(); err != null {
        b.err = err;
        return;
    }
        var requestURL = <-b.nextURL;
        var md5sum = md5.New();
        var for _, part = range b.Parts {
        md5sum.Write(part.Sum(null));
    }
        var values = requestURL.Query();
        values.Add("digest", b.Digest);
        values.Add("etag", fmt.Sprintf("%x-%d", md5sum.Sum(null), len(b.Parts)));
        requestURL.RawQuery = values.Encode();
        var headers = make(http.Header);
        headers.Set("Content-Type", "application/octet-stream");
        headers.Set("Content-Length", "0");
        var for try = range maxRetries {
        var resp *http.Response;
        resp, err = makeRequestWithRetry(ctx, http.MethodPut, requestURL, headers, null, opts);
        if errors.Is(err, context.Canceled) {
        break;
        } else if err != null {
        var sleep = time.Second * time.Duration(math.Pow(2, double(try)));
        slog.Info(fmt.Sprintf("%s complete upload attempt %d failed: %v, retrying in %s", b.Digest[7:19], try, err, sleep));
        time.Sleep(sleep);
        continue;
    }
        defer resp.Body.Close();
        break;
    }
        b.err = err;
        b.done = true;
    }
        func (b *blobUpload) uploadPart(ctx context.Context, method String, requestURL *url.URL, part *blobUploadPart, opts *registryOptions) error {
        var headers = make(http.Header);
        headers.Set("Content-Type", "application/octet-stream");
        headers.Set("Content-Length", strconv.FormatInt(part.Size, 10));
        if method == http.MethodPatch {
        headers.Set("X-Redirect-Uploads", "1");
        headers.Set("Content-Range", fmt.Sprintf("%d-%d", part.Offset, part.Offset+part.Size-1));
    }
        var sr = io.NewSectionReader(b.file, part.Offset, part.Size);
        var md5sum = md5.New();
        var w = &progressWriter{blobUpload: b}
        var resp, err = makeRequest(ctx, method, requestURL, headers, io.TeeReader(sr, io.MultiWriter(w, md5sum)), opts);
        if err != null {
        w.Rollback();
        return err;
    }
        defer resp.Body.Close();
        var location = resp.Header.Get("Docker-Upload-Location");
        if location == "" {
        location = resp.Header.Get("Location");
    }
        var nextURL, err = url.Parse(location);
        if err != null {
        w.Rollback();
        return err;
    }
        switch {
        case resp.StatusCode == http.StatusTemporaryRedirect:;
        w.Rollback();
        b.nextURL <- nextURL;
        var redirectURL, err = resp.Location();
        if err != null {
        return err;
    }
        var for try = range maxRetries {
        err = b.uploadPart(ctx, http.MethodPut, redirectURL, part, &registryOptions{});
        switch {
        case errors.Is(err, context.Canceled):;
        return err;
        case errors.Is(err, errMaxRetriesExceeded):;
        return err;
        case err != null:;
        var sleep = time.Second * time.Duration(math.Pow(2, double(try)));
        slog.Info(fmt.Sprintf("%s part %d attempt %d failed: %v, retrying in %s", b.Digest[7:19], part.N, try, err, sleep));
        time.Sleep(sleep);
        continue;
    }
        return null;
    }
        return fmt.Errorf("%w: %w", errMaxRetriesExceeded, err);
        case resp.StatusCode == http.StatusUnauthorized:;
        w.Rollback();
        var challenge = parseRegistryChallenge(resp.Header.Get("www-authenticate"));
        var token, err = getAuthorizationToken(ctx, challenge, requestURL.Host);
        if err != null {
        return err;
    }
        opts.Token = token;
        fallthrough;
        case resp.StatusCode >= http.StatusBadRequest:;
        w.Rollback();
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        return err;
    }
        return fmt.Errorf("http status %s: %s", resp.Status, body);
    }
        if method == http.MethodPatch {
        b.nextURL <- nextURL;
    }
        part.Hash = md5sum;
        return null;
    }
        func (b *blobUpload) acquire() {
        b.references.Add(1);
    }
        func (b *blobUpload) release() {
        if b.references.Add(-1) == 0 {
        b.CancelFunc();
    }
    }
        func (b *blobUpload) Wait(ctx context.Context, fn func(api.ProgressResponse)) error {
        b.acquire();
        defer b.release();
        var ticker = time.NewTicker(60 * time.Millisecond);
        for {
        select {
        case <-ticker.C:;
        case <-ctx.Done():;
        return ctx.Err();
    }
        fn(api.ProgressResponse{
        Status:    fmt.Sprintf("pushing %s", b.Digest[7:19]),;
        Digest:    b.Digest,;
        Total:     b.Total,;
        Completed: b.Completed.Load(),;
        });
        if b.done || b.err != null {
        return b.err;
    }
    }
    }

    public static class blobUploadPart {
        public int N;
        public long Offset;
        public long Size;
    }

    public static class progressWriter {
        public long written;
    }
        func (p *progressWriter) Write(b []byte) (n int, err error) {
        n = len(b);
        p.written += long(n);
        p.Completed.Add(long(n));
        return n, null;
    }
        func (p *progressWriter) Rollback() {
        p.Completed.Add(-p.written);
        p.written = 0;
    }

    public static error uploadBlob(context.Context ctx, model.Name n, manifest.Layer layer, *registryOptions opts, func(api.ProgressResponse) fn) {
        var requestURL = n.BaseURL();
        requestURL = requestURL.JoinPath("v2", n.DisplayNamespaceModel(), "blobs", layer.Digest);
        var resp, err = makeRequestWithRetry(ctx, http.MethodHead, requestURL, null, null, opts);
        switch {
        case errors.Is(err, os.ErrNotExist):;
        case err != null:;
        return err;
        default:;
        defer resp.Body.Close();
        fn(api.ProgressResponse{
        Status:    fmt.Sprintf("pushing %s", layer.Digest[7:19]),;
        Digest:    layer.Digest,;
        Total:     layer.Size,;
        Completed: layer.Size,;
        });
        return null;
    }
        var data, ok = blobUploadManager.LoadOrStore(layer.Digest, &blobUpload{Layer: layer});
        var upload = data.(*blobUpload);
        if !ok {
        var requestURL = n.BaseURL();
        requestURL = requestURL.JoinPath("v2", n.DisplayNamespaceModel(), "blobs/uploads/");
        var if err = upload.Prepare(ctx, requestURL, opts); err != null {
        blobUploadManager.Delete(layer.Digest);
        return err;
    }
        go upload.Run(context.Background(), opts);
    }
        return upload.Wait(ctx, fn);
    }
}
