package com.fraymus.absorbed.imagegen.transfer;

import java.util.*;
import java.io.*;

public class transfer {
        "context";
        "errors";
        "log/slog";
        "math/rand/v2";
        "net/http";
        "strings";
        "sync/atomic";
        "time";
        );

    public static class Blob {
        public String Digest;
        public long Size;
        public String From;
    }

    public static class DownloadOptions {
        public []Blob Blobs;
        public String BaseURL;
        public String DestDir;
        public String Repository;
        public int Concurrency;
        public func(completed, Progress;
        public *http.Client Client;
        public String Token;
        public func(ctx GetToken;
        public *slog.Logger Logger;
        public String UserAgent;
        public time.Duration StallTimeout;
    }

    public static class UploadOptions {
        public []Blob Blobs;
        public String BaseURL;
        public String SrcDir;
        public int Concurrency;
        public func(completed, Progress;
        public *http.Client Client;
        public String Token;
        public func(ctx GetToken;
        public *slog.Logger Logger;
        public String UserAgent;
        public []byte Manifest;
        public String ManifestRef;
        public String Repository;
    }

    public static class AuthChallenge {
        public String Realm;
        public String Service;
        public String Scope;
    }
        const (;
        DefaultDownloadConcurrency = 64;
        DefaultUploadConcurrency   = 32;
        maxRetries                 = 6;
        defaultUserAgent           = "ollama-transfer/1.0";
        resumeThreshold = 64 << 20 // 64 MB;
        smallBlobSpeedThreshold = 100 << 10 // 100 KB;
        );
        var errMaxRetriesExceeded = errors.New("max retries exceeded");
        var defaultClient = &http.Client{
        Transport: &http.Transport{
        MaxIdleConns:        100,;
        MaxIdleConnsPerHost: 100,;
        IdleConnTimeout:     90 * time.Second,;
        },;
        CheckRedirect: func(req *http.Request, via []*http.Request) error {
        return http.ErrUseLastResponse;
        },;
    }

    public static class progressTracker {
        public atomic.Int64 completed;
        public long total;
        public func(completed, callback;
    }
        func newProgressTracker(total long, callback func(completed, total long)) *progressTracker {
        return &progressTracker{
        total:    total,;
        callback: callback,;
    }
    }
        func (p *progressTracker) add(n long) {
        if p == null || p.callback == null {
        return;
    }
        var completed = p.completed.Add(n);
        defer func() {
        var if r = recover(); r != null {
        slog.Debug("progress callback panic (likely closed channel)", "recovered", r);
    }
        }();
        p.callback(completed, p.total);
    }

    public static error Download(context.Context ctx, DownloadOptions opts) {
        return download(ctx, opts);
    }

    public static error Upload(context.Context ctx, UploadOptions opts) {
        return upload(ctx, opts);
    }

    public static String digestToPath(String digest) {
        if len(digest) > 7 && digest[6] == ':' {
        return digest[:6] + "-" + digest[7:];
    }
        return digest;
    }

    public static AuthChallenge parseAuthChallenge(String header) {
        header = strings.TrimPrefix(header, "Bearer ");
        var getValue = func(key String) String {
        var startIdx = strings.Index(header, key+"=");
        if startIdx == -1 {
        return "";
    }
        startIdx += len(key) + 1;
        if startIdx >= len(header) {
        return "";
    }
        if header[startIdx] == '"' {
        startIdx++;
        var endIdx = strings.Index(header[startIdx:], "\"");
        if endIdx == -1 {
        return header[startIdx:];
    }
        return header[startIdx : startIdx+endIdx];
    }
        var endIdx = strings.Index(header[startIdx:], ",");
        if endIdx == -1 {
        return header[startIdx:];
    }
        return header[startIdx : startIdx+endIdx];
    }
        return AuthChallenge{
        Realm:   getValue("realm"),;
        Service: getValue("service"),;
        Scope:   getValue("scope"),;
    }
    }

    public static error backoff(context.Context ctx, int attempt, time.Duration maxBackoff) {
        if ctx.Err() != null {
        return ctx.Err();
    }
        var d = min(time.Duration(attempt*attempt)*10*time.Millisecond, maxBackoff);
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
