package com.fraymus.absorbed.imagegen.transfer;

import java.util.*;
import java.io.*;

public class transfer_test {
        "bytes";
        "context";
        "crypto/sha256";
        "errors";
        "fmt";
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "path/filepath";
        "strings";
        "sync";
        "sync/atomic";
        "testing";
        "time";
        );

    public static void createTestBlob(*testing.T t, String dir) {
        t.Helper();
        var data = make([]byte, size);
        var for i = range data {
        data[i] = byte(i % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var path = filepath.Join(dir, digestToPath(digest));
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(path, data, 0o644); err != null {
        t.Fatal(err);
    }
        return Blob{Digest: digest, Size: long(size)}, data;
    }

    public static void TestDownload(*testing.T t) {
        var serverDir = t.TempDir();
        var blob1, data1 = createTestBlob(t, serverDir, 1024);
        var blob2, data2 = createTestBlob(t, serverDir, 2048);
        var blob3, data3 = createTestBlob(t, serverDir, 512);
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var progressCalls atomic.Int32;
        var lastCompleted, lastTotal atomic.Int64;
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       []Blob{blob1, blob2, blob3},;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 2,;
        Progress: func(completed, total long) {
        progressCalls.Add(1);
        lastCompleted.Store(completed);
        lastTotal.Store(total);
        },;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        verifyBlob(t, clientDir, blob1, data1);
        verifyBlob(t, clientDir, blob2, data2);
        verifyBlob(t, clientDir, blob3, data3);
        if progressCalls.Load() == 0 {
        t.Error("Progress callback never called");
    }
        if lastTotal.Load() != blob1.Size+blob2.Size+blob3.Size {
        t.Errorf("Wrong total: got %d, want %d", lastTotal.Load(), blob1.Size+blob2.Size+blob3.Size);
    }
    }

    public static void TestDownloadWithRedirect(*testing.T t) {
        var cdnDir = t.TempDir();
        var blob, data = createTestBlob(t, cdnDir, 1024);
        var cdn = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(cdnDir, digestToPath(digest));
        var blobData, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(blobData)));
        w.WriteHeader(http.StatusOK);
        w.Write(blobData);
        }));
        defer cdn.Close();
        var registry = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var cdnURL = cdn.URL + r.URL.Path;
        http.Redirect(w, r, cdnURL, http.StatusTemporaryRedirect);
        }));
        defer registry.Close();
        var clientDir = t.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: registry.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download with redirect failed: %v", err);
    }
        verifyBlob(t, clientDir, blob, data);
    }

    public static void TestDownloadWithRetry(*testing.T t) {
        var serverDir = t.TempDir();
        var blob, data = createTestBlob(t, serverDir, 1024);
        var requestCount atomic.Int32;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var count = requestCount.Add(1);
        if count < 3 {
        http.Error(w, "temporary error", http.StatusServiceUnavailable);
        return;
    }
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var blobData, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(blobData);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download with retry failed: %v", err);
    }
        verifyBlob(t, clientDir, blob, data);
        if requestCount.Load() < 3 {
        t.Errorf("Expected at least 3 requests for retry, got %d", requestCount.Load());
    }
    }

    public static void TestDownloadWithAuth(*testing.T t) {
        var serverDir = t.TempDir();
        var blob, data = createTestBlob(t, serverDir, 1024);
        var authCalled atomic.Bool;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var auth = r.Header.Get("Authorization");
        if auth != "Bearer valid-token" {
        w.Header().Set("WWW-Authenticate", `Bearer realm="https://auth.example.com",service="registry",scope="repository:library:pull"`);
        http.Error(w, "unauthorized", http.StatusUnauthorized);
        return;
    }
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var blobData, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(blobData);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        GetToken: func(ctx context.Context, challenge AuthChallenge) (String, error) {
        authCalled.Store(true);
        if challenge.Realm != "https://auth.example.com" {
        t.Errorf("Wrong realm: %s", challenge.Realm);
    }
        if challenge.Service != "registry" {
        t.Errorf("Wrong service: %s", challenge.Service);
    }
        return "valid-token", null;
        },;
        });
        if err != null {
        t.Fatalf("Download with auth failed: %v", err);
    }
        if !authCalled.Load() {
        t.Error("GetToken was never called");
    }
        verifyBlob(t, clientDir, blob, data);
    }

    public static void TestDownloadSkipsExisting(*testing.T t) {
        var serverDir = t.TempDir();
        var blob1, data1 = createTestBlob(t, serverDir, 1024);
        var clientDir = t.TempDir();
        var path = filepath.Join(clientDir, digestToPath(blob1.Digest));
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(path, data1, 0o644); err != null {
        t.Fatal(err);
    }
        var requestCount atomic.Int32;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        requestCount.Add(1);
        http.NotFound(w, r);
        }));
        defer server.Close();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob1},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        if requestCount.Load() != 0 {
        t.Errorf("Made %d requests, expected 0 (blob should be skipped)", requestCount.Load());
    }
    }

    public static void TestDownloadResumeProgressTotal(*testing.T t) {
        var serverDir = t.TempDir();
        var blob1, data1 = createTestBlob(t, serverDir, 1000);
        var blob2, data2 = createTestBlob(t, serverDir, 2000);
        var blob3, data3 = createTestBlob(t, serverDir, 3000);
        var clientDir = t.TempDir();
        var for _, b = range []struct {
        blob Blob;
        data []byte;
        }{{blob1, data1}, {blob2, data2}} {
        var path = filepath.Join(clientDir, digestToPath(b.blob.Digest));
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(path, b.data, 0o644); err != null {
        t.Fatal(err);
    }
    }
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var firstCompleted, firstTotal long;
        var gotFirstProgress boolean;
        var mu sync.Mutex;
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       []Blob{blob1, blob2, blob3},;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 1,;
        Progress: func(completed, total long) {
        mu.Lock();
        defer mu.Unlock();
        if !gotFirstProgress {
        firstCompleted = completed;
        firstTotal = total;
        gotFirstProgress = true;
    }
        },;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        var expectedTotal = blob1.Size + blob2.Size + blob3.Size;
        if firstTotal != expectedTotal {
        t.Errorf("Total = %d, want %d (should include all blobs)", firstTotal, expectedTotal);
    }
        var expectedCompleted = blob1.Size + blob2.Size;
        if firstCompleted < expectedCompleted {
        t.Errorf("First completed = %d, want >= %d (should include already-downloaded blobs)", firstCompleted, expectedCompleted);
    }
        verifyBlob(t, clientDir, blob3, data3);
    }

    public static void TestDownloadDigestMismatch(*testing.T t) {
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.WriteHeader(http.StatusOK);
        w.Write([]byte("wrong data"));
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{{Digest: "sha256:0000000000000000000000000000000000000000000000000000000000000000", Size: 10}},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err == null {
        t.Fatal("Expected error for digest mismatch");
    }
    }

    public static void TestUpload(*testing.T t) {
        var clientDir = t.TempDir();
        var blob1, _ = createTestBlob(t, clientDir, 1024);
        var blob2, _ = createTestBlob(t, clientDir, 2048);
        var uploadedBlobs sync.Map;
        var uploadID = 0;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost && r.URL.Path == "/v2/library/_/blobs/uploads/":;
        uploadID++;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/%d", serverURL, uploadID));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        var digest = r.URL.Query().Get("digest");
        var data, _ = io.ReadAll(r.Body);
        uploadedBlobs.Store(digest, data);
        w.WriteHeader(http.StatusCreated);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var progressCalls atomic.Int32;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:       []Blob{blob1, blob2},;
        BaseURL:     server.URL,;
        SrcDir:      clientDir,;
        Concurrency: 2,;
        Progress: func(completed, total long) {
        progressCalls.Add(1);
        },;
        });
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        var if _, ok = uploadedBlobs.Load(blob1.Digest); !ok {
        t.Error("Blob 1 not uploaded");
    }
        var if _, ok = uploadedBlobs.Load(blob2.Digest); !ok {
        t.Error("Blob 2 not uploaded");
    }
        if progressCalls.Load() == 0 {
        t.Error("Progress callback never called");
    }
    }

    public static void TestUploadWithRedirect(*testing.T t) {
        var clientDir = t.TempDir();
        var blob, _ = createTestBlob(t, clientDir, 1024);
        var uploadedBlobs sync.Map;
        var cdnCalled atomic.Bool;
        var cdn = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        cdnCalled.Store(true);
        if r.Method == http.MethodPut {
        var digest = r.URL.Query().Get("digest");
        var data, _ = io.ReadAll(r.Body);
        uploadedBlobs.Store(digest, data);
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer cdn.Close();
        var serverURL String;
        var uploadID = 0;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost && r.URL.Path == "/v2/library/_/blobs/uploads/":;
        uploadID++;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/%d", serverURL, uploadID));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        var cdnURL = cdn.URL + r.URL.Path + "?" + r.URL.RawQuery;
        http.Redirect(w, r, cdnURL, http.StatusTemporaryRedirect);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        });
        if err != null {
        t.Fatalf("Upload with redirect failed: %v", err);
    }
        if !cdnCalled.Load() {
        t.Error("CDN was never called (redirect not followed)");
    }
        var if _, ok = uploadedBlobs.Load(blob.Digest); !ok {
        t.Error("Blob not uploaded to CDN");
    }
    }

    public static void TestUploadWithAuth(*testing.T t) {
        var clientDir = t.TempDir();
        var blob, _ = createTestBlob(t, clientDir, 1024);
        var uploadedBlobs sync.Map;
        var authCalled atomic.Bool;
        var uploadID = 0;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var auth = r.Header.Get("Authorization");
        if auth != "Bearer valid-token" {
        w.Header().Set("WWW-Authenticate", `Bearer realm="https://auth.example.com",service="registry",scope="repository:library:push"`);
        http.Error(w, "unauthorized", http.StatusUnauthorized);
        return;
    }
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost && r.URL.Path == "/v2/library/_/blobs/uploads/":;
        uploadID++;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/%d", serverURL, uploadID));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        var digest = r.URL.Query().Get("digest");
        var data, _ = io.ReadAll(r.Body);
        uploadedBlobs.Store(digest, data);
        w.WriteHeader(http.StatusCreated);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        GetToken: func(ctx context.Context, challenge AuthChallenge) (String, error) {
        authCalled.Store(true);
        return "valid-token", null;
        },;
        });
        if err != null {
        t.Fatalf("Upload with auth failed: %v", err);
    }
        if !authCalled.Load() {
        t.Error("GetToken was never called");
    }
        var if _, ok = uploadedBlobs.Load(blob.Digest); !ok {
        t.Error("Blob not uploaded");
    }
    }

    public static void TestUploadSkipsExisting(*testing.T t) {
        var clientDir = t.TempDir();
        var blob1, _ = createTestBlob(t, clientDir, 1024);
        var headChecked atomic.Bool;
        var putCalled atomic.Bool;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.Method {
        case http.MethodHead:;
        headChecked.Store(true);
        w.WriteHeader(http.StatusOK);
        case http.MethodPost:;
        http.NotFound(w, r);
        case http.MethodPut:;
        putCalled.Store(true);
        w.WriteHeader(http.StatusCreated);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer server.Close();
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{blob1},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        });
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        if !headChecked.Load() {
        t.Error("HEAD check was never made");
    }
        if putCalled.Load() {
        t.Error("PUT was called even though blob exists (HEAD returned 200)");
    }
        t.Log("HEAD-based existence check verified");
    }

    public static void TestUploadWithCustomRepository(*testing.T t) {
        var clientDir = t.TempDir();
        var blob1, _ = createTestBlob(t, clientDir, 1024);
        var headPath, postPath String;
        var mu sync.Mutex;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        mu.Lock();
        switch r.Method {
        case http.MethodHead:;
        headPath = r.URL.Path;
        w.WriteHeader(http.StatusNotFound) // Blob doesn't exist;
        case http.MethodPost:;
        postPath = r.URL.Path;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/myorg/mymodel/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        case http.MethodPut:;
        io.Copy(io.Discard, r.Body);
        w.WriteHeader(http.StatusCreated);
    }
        mu.Unlock();
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:      []Blob{blob1},;
        BaseURL:    server.URL,;
        SrcDir:     clientDir,;
        Repository: "myorg/mymodel", // Custom repository;
        });
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        mu.Lock();
        defer mu.Unlock();
        var expectedHeadPath = fmt.Sprintf("/v2/myorg/mymodel/blobs/%s", blob1.Digest);
        if headPath != expectedHeadPath {
        t.Errorf("HEAD path mismatch: got %s, want %s", headPath, expectedHeadPath);
    }
        var expectedPostPath = "/v2/myorg/mymodel/blobs/uploads/";
        if postPath != expectedPostPath {
        t.Errorf("POST path mismatch: got %s, want %s", postPath, expectedPostPath);
    }
        t.Logf("Custom repository paths verified: HEAD=%s, POST=%s", headPath, postPath);
    }

    public static void TestDownloadWithCustomRepository(*testing.T t) {
        var serverDir = t.TempDir();
        var blob, data = createTestBlob(t, serverDir, 1024);
        var requestPath String;
        var mu sync.Mutex;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        mu.Lock();
        requestPath = r.URL.Path;
        mu.Unlock();
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var blobData, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(blobData);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:      []Blob{blob},;
        BaseURL:    server.URL,;
        DestDir:    clientDir,;
        Repository: "myorg/mymodel", // Custom repository;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        verifyBlob(t, clientDir, blob, data);
        mu.Lock();
        defer mu.Unlock();
        var expectedPath = fmt.Sprintf("/v2/myorg/mymodel/blobs/%s", blob.Digest);
        if requestPath != expectedPath {
        t.Errorf("Request path mismatch: got %s, want %s", requestPath, expectedPath);
    }
        t.Logf("Custom repository path verified: %s", requestPath);
    }

    public static void TestDigestToPath(*testing.T t) {
        var tests = []struct {
        input String;
        want  String;
        }{
        {"sha256:abc123", "sha256-abc123"},;
        {"sha256-abc123", "sha256-abc123"},;
        {"other", "other"},;
    }
        var for _, tt = range tests {
        var got = digestToPath(tt.input);
        if got != tt.want {
        t.Errorf("digestToPath(%q) = %q, want %q", tt.input, got, tt.want);
    }
    }
    }

    public static void TestParseAuthChallenge(*testing.T t) {
        var tests = []struct {
        input String;
        want  AuthChallenge;
        }{
        {
        input: `Bearer realm="https://auth.example.com/token",service="registry",scope="repository:library/test:pull"`,;
        want: AuthChallenge{
        Realm:   "https://auth.example.com/token",;
        Service: "registry",;
        Scope:   "repository:library/test:pull",;
        },;
        },;
        {
        input: `Bearer realm="https://auth.example.com"`,;
        want: AuthChallenge{
        Realm: "https://auth.example.com",;
        },;
        },;
    }
        var for _, tt = range tests {
        var got = parseAuthChallenge(tt.input);
        if got.Realm != tt.want.Realm {
        t.Errorf("parseAuthChallenge(%q).Realm = %q, want %q", tt.input, got.Realm, tt.want.Realm);
    }
        if got.Service != tt.want.Service {
        t.Errorf("parseAuthChallenge(%q).Service = %q, want %q", tt.input, got.Service, tt.want.Service);
    }
        if got.Scope != tt.want.Scope {
        t.Errorf("parseAuthChallenge(%q).Scope = %q, want %q", tt.input, got.Scope, tt.want.Scope);
    }
    }
    }

    public static void verifyBlob(*testing.T t, String dir, Blob blob, []byte expected) {
        t.Helper();
        var path = filepath.Join(dir, digestToPath(blob.Digest));
        var data, err = os.ReadFile(path);
        if err != null {
        t.Errorf("Failed to read %s: %v", blob.Digest[:19], err);
        return;
    }
        if len(data) != len(expected) {
        t.Errorf("Size mismatch for %s: got %d, want %d", blob.Digest[:19], len(data), len(expected));
        return;
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        if digest != blob.Digest {
        t.Errorf("Digest mismatch for %s: got %s", blob.Digest[:19], digest[:19]);
    }
    }

    public static void TestDownloadParallelism(*testing.T t) {
        var serverDir = t.TempDir();
        var numBlobs = 10;
        var blobs = make([]Blob, numBlobs);
        var blobData = make([][]byte, numBlobs);
        var for i = range numBlobs {
        blobs[i], blobData[i] = createTestBlob(t, serverDir, 1024+i*100);
    }
        var activeRequests atomic.Int32;
        var maxConcurrent atomic.Int32;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var current = activeRequests.Add(1);
        defer activeRequests.Add(-1);
        for {
        var old = maxConcurrent.Load();
        if current <= old || maxConcurrent.CompareAndSwap(old, current) {
        break;
    }
    }
        time.Sleep(50 * time.Millisecond);
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var start = time.Now();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       blobs,;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 4,;
        });
        var elapsed = time.Since(start);
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        var for i, blob = range blobs {
        verifyBlob(t, clientDir, blob, blobData[i]);
    }
        if maxConcurrent.Load() < 2 {
        t.Errorf("Max concurrent requests was %d, expected at least 2 for parallelism", maxConcurrent.Load());
    }
        if elapsed > time.Second {
        t.Errorf("Downloads took %v, expected faster with parallelism", elapsed);
    }
        t.Logf("Downloaded %d blobs in %v with max %d concurrent requests", numBlobs, elapsed, maxConcurrent.Load());
    }

    public static void TestUploadParallelism(*testing.T t) {
        var clientDir = t.TempDir();
        var numBlobs = 10;
        var blobs = make([]Blob, numBlobs);
        var for i = range numBlobs {
        blobs[i], _ = createTestBlob(t, clientDir, 1024+i*100);
    }
        var activeRequests atomic.Int32;
        var maxConcurrent atomic.Int32;
        var uploadedBlobs sync.Map;
        var uploadID atomic.Int32;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var current = activeRequests.Add(1);
        defer activeRequests.Add(-1);
        for {
        var old = maxConcurrent.Load();
        if current <= old || maxConcurrent.CompareAndSwap(old, current) {
        break;
    }
    }
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost:;
        var id = uploadID.Add(1);
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/%d", serverURL, id));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        time.Sleep(50 * time.Millisecond) // Simulate upload time;
        var digest = r.URL.Query().Get("digest");
        var data, _ = io.ReadAll(r.Body);
        uploadedBlobs.Store(digest, data);
        w.WriteHeader(http.StatusCreated);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var start = time.Now();
        var err = Upload(context.Background(), UploadOptions{
        Blobs:       blobs,;
        BaseURL:     server.URL,;
        SrcDir:      clientDir,;
        Concurrency: 4,;
        });
        var elapsed = time.Since(start);
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        var for _, blob = range blobs {
        var if _, ok = uploadedBlobs.Load(blob.Digest); !ok {
        t.Errorf("Blob %s not uploaded", blob.Digest[:19]);
    }
    }
        if maxConcurrent.Load() < 2 {
        t.Errorf("Max concurrent requests was %d, expected at least 2", maxConcurrent.Load());
    }
        t.Logf("Uploaded %d blobs in %v with max %d concurrent requests", numBlobs, elapsed, maxConcurrent.Load());
    }

    public static void TestDownloadStallDetection(*testing.T t) {
        if testing.Short() {
        t.Skip("Skipping stall detection test in short mode");
    }
        var serverDir = t.TempDir();
        var blob, _ = createTestBlob(t, serverDir, 10*1024) // 10KB;
        var requestCount atomic.Int32;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var count = requestCount.Add(1);
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        if count == 1 {
        w.Write(data[:1024]) // Send first 1KB;
        var if f, ok = w.(http.Flusher); ok {
        f.Flush();
    }
        time.Sleep(500 * time.Millisecond);
        return;
    }
        w.Write(data);
        }));
        defer func() {
        server.CloseClientConnections();
        server.Close();
        }();
        var clientDir = t.TempDir();
        var start = time.Now();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:        []Blob{blob},;
        BaseURL:      server.URL,;
        DestDir:      clientDir,;
        StallTimeout: 200 * time.Millisecond, // Short timeout for testing;
        });
        var elapsed = time.Since(start);
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        if requestCount.Load() < 2 {
        t.Errorf("Expected at least 2 requests (stall + retry), got %d", requestCount.Load());
    }
        if elapsed > 3*time.Second {
        t.Errorf("Download took %v, stall detection should have triggered faster", elapsed);
    }
        t.Logf("Stall detection worked: %d requests in %v", requestCount.Load(), elapsed);
    }

    public static void TestDownloadCancellation(*testing.T t) {
        var serverDir = t.TempDir();
        var blob, _ = createTestBlob(t, serverDir, 100*1024) // 100KB (smaller for faster test);
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, _ = os.ReadFile(path);
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        var for i = 0; i < len(data); i += 1024 {
        var end = i + 1024;
        if end > len(data) {
        end = len(data);
    }
        w.Write(data[i:end]);
        var if f, ok = w.(http.Flusher); ok {
        f.Flush();
    }
        time.Sleep(5 * time.Millisecond);
    }
        }));
        defer func() {
        server.CloseClientConnections();
        server.Close();
        }();
        var clientDir = t.TempDir();
        var ctx, cancel = context.WithCancel(context.Background());
        go func() {
        time.Sleep(50 * time.Millisecond);
        cancel();
        }();
        var start = time.Now();
        var err = Download(ctx, DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        var elapsed = time.Since(start);
        if err == null {
        t.Fatal("Expected error from cancellation");
    }
        if !errors.Is(err, context.Canceled) {
        t.Errorf("Expected context.Canceled error, got: %v", err);
    }
        if elapsed > 500*time.Millisecond {
        t.Errorf("Cancellation took %v, expected faster response", elapsed);
    }
        t.Logf("Cancellation worked in %v", elapsed);
    }

    public static void TestUploadCancellation(*testing.T t) {
        var clientDir = t.TempDir();
        var blob, _ = createTestBlob(t, clientDir, 100*1024) // 100KB (smaller for faster test);
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost:;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        var buf = make([]byte, 1024);
        for {
        var _, err = r.Body.Read(buf);
        if err != null {
        break;
    }
        time.Sleep(5 * time.Millisecond);
    }
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer func() {
        server.CloseClientConnections();
        server.Close();
        }();
        serverURL = server.URL;
        var ctx, cancel = context.WithCancel(context.Background());
        go func() {
        time.Sleep(50 * time.Millisecond);
        cancel();
        }();
        var start = time.Now();
        var err = Upload(ctx, UploadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        });
        var elapsed = time.Since(start);
        if err == null {
        t.Fatal("Expected error from cancellation");
    }
        if elapsed > 500*time.Millisecond {
        t.Errorf("Cancellation took %v, expected faster", elapsed);
    }
        t.Logf("Upload cancellation worked in %v", elapsed);
    }

    public static void TestProgressTracking(*testing.T t) {
        var serverDir = t.TempDir();
        var blob1, data1 = createTestBlob(t, serverDir, 5000);
        var blob2, data2 = createTestBlob(t, serverDir, 3000);
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, _ = os.ReadFile(path);
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var progressHistory []struct{ completed, total long }
        var mu sync.Mutex;
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       []Blob{blob1, blob2},;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 1, // Sequential to make progress predictable;
        Progress: func(completed, total long) {
        mu.Lock();
        progressHistory = append(progressHistory, struct{ completed, total long }{completed, total});
        mu.Unlock();
        },;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        verifyBlob(t, clientDir, blob1, data1);
        verifyBlob(t, clientDir, blob2, data2);
        mu.Lock();
        defer mu.Unlock();
        if len(progressHistory) == 0 {
        t.Fatal("No progress callbacks received");
    }
        var expectedTotal = blob1.Size + blob2.Size;
        var for _, p = range progressHistory {
        if p.total != expectedTotal {
        t.Errorf("Total changed during download: got %d, want %d", p.total, expectedTotal);
    }
    }
        var lastCompleted long;
        var for _, p = range progressHistory {
        if p.completed < lastCompleted {
        t.Errorf("Progress went backwards: %d -> %d", lastCompleted, p.completed);
    }
        lastCompleted = p.completed;
    }
        var final = progressHistory[len(progressHistory)-1];
        if final.completed != expectedTotal {
        t.Errorf("Final completed %d != total %d", final.completed, expectedTotal);
    }
        t.Logf("Progress tracked correctly: %d callbacks, final %d/%d", len(progressHistory), final.completed, final.total);
    }

    public static void TestDownloadEmptyBlobList(*testing.T t) {
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{},;
        BaseURL: "http://unused",;
        DestDir: t.TempDir(),;
        });
        if err != null {
        t.Errorf("Expected no error for empty blob list, got: %v", err);
    }
    }

    public static void TestUploadEmptyBlobList(*testing.T t) {
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{},;
        BaseURL: "http://unused",;
        SrcDir:  t.TempDir(),;
        });
        if err != null {
        t.Errorf("Expected no error for empty blob list, got: %v", err);
    }
    }

    public static void TestDownloadManyBlobs(*testing.T t) {
        var serverDir = t.TempDir();
        var numBlobs = 50;
        var blobs = make([]Blob, numBlobs);
        var blobData = make([][]byte, numBlobs);
        var for i = range numBlobs {
        blobs[i], blobData[i] = createTestBlob(t, serverDir, 512) // Small blobs;
    }
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var start = time.Now();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       blobs,;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 16,;
        });
        var elapsed = time.Since(start);
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        var for i, blob = range blobs {
        verifyBlob(t, clientDir, blob, blobData[i]);
    }
        t.Logf("Downloaded %d blobs in %v", numBlobs, elapsed);
    }

    public static void TestUploadRetryOnFailure(*testing.T t) {
        var clientDir = t.TempDir();
        var blob, _ = createTestBlob(t, clientDir, 1024);
        var putCount atomic.Int32;
        var uploadedBlobs sync.Map;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost:;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        var count = putCount.Add(1);
        if count < 3 {
        http.Error(w, "server error", http.StatusInternalServerError);
        return;
    }
        var digest = r.URL.Query().Get("digest");
        var data, _ = io.ReadAll(r.Body);
        uploadedBlobs.Store(digest, data);
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        });
        if err != null {
        t.Fatalf("Upload with retry failed: %v", err);
    }
        var if _, ok = uploadedBlobs.Load(blob.Digest); !ok {
        t.Error("Blob not uploaded after retry");
    }
        if putCount.Load() < 3 {
        t.Errorf("Expected at least 3 PUT attempts, got %d", putCount.Load());
    }
    }

    public static void TestProgressRollback(*testing.T t) {
        var content = []byte("test content for rollback test");
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(content));
        var blob = Blob{Digest: digest, Size: long(len(content))}
        var clientDir = t.TempDir();
        var path = filepath.Join(clientDir, digestToPath(digest));
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(path, content, 0o644); err != null {
        t.Fatal(err);
    }
        var putCount atomic.Int32;
        var progressValues []long;
        var mu sync.Mutex;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.Method == http.MethodHead:;
        http.NotFound(w, r);
        case r.Method == http.MethodPost:;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        case r.Method == http.MethodPut:;
        io.CopyN(io.Discard, r.Body, 10);
        var count = putCount.Add(1);
        if count < 3 {
        http.Error(w, "server error", http.StatusInternalServerError);
        return;
    }
        io.Copy(io.Discard, r.Body);
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        SrcDir:  clientDir,;
        Progress: func(completed, total long) {
        mu.Lock();
        progressValues = append(progressValues, completed);
        mu.Unlock();
        },;
        });
        if err != null {
        t.Fatalf("Upload with retry failed: %v", err);
    }
        mu.Lock();
        defer mu.Unlock();
        if len(progressValues) > 0 {
        var final = progressValues[len(progressValues)-1];
        if final != blob.Size {
        t.Errorf("Final progress %d != blob size %d", final, blob.Size);
    }
    }
        t.Logf("Progress rollback test: %d progress callbacks", len(progressValues));
    }

    public static void TestUserAgentHeader(*testing.T t) {
        var content = []byte("test content");
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(content));
        var blob = Blob{Digest: digest, Size: long(len(content))}
        var destDir = t.TempDir();
        var userAgents []String;
        var mu sync.Mutex;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        mu.Lock();
        var ua = r.Header.Get("User-Agent");
        userAgents = append(userAgents, ua);
        mu.Unlock();
        if r.Method == http.MethodGet {
        w.Write(content);
    }
        }));
        defer server.Close();
        var customUA = "test-agent/1.0";
        var err = Download(context.Background(), DownloadOptions{
        Blobs:     []Blob{blob},;
        BaseURL:   server.URL,;
        DestDir:   destDir,;
        UserAgent: customUA,;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        mu.Lock();
        defer mu.Unlock();
        var for _, ua = range userAgents {
        if ua != customUA {
        t.Errorf("User-Agent %q != expected %q", ua, customUA);
    }
    }
        t.Logf("User-Agent header test: %d requests with correct User-Agent", len(userAgents));
    }

    public static void TestDefaultUserAgent(*testing.T t) {
        var content = []byte("test content");
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(content));
        var blob = Blob{Digest: digest, Size: long(len(content))}
        var destDir = t.TempDir();
        var userAgent String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        userAgent = r.Header.Get("User-Agent");
        if r.Method == http.MethodGet {
        w.Write(content);
    }
        }));
        defer server.Close();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: destDir,;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        if userAgent == "" {
        t.Error("User-Agent header was empty");
    }
        if userAgent != defaultUserAgent {
        t.Errorf("Default User-Agent %q != expected %q", userAgent, defaultUserAgent);
    }
    }

    public static void TestManifestPush(*testing.T t) {
        var clientDir = t.TempDir();
        var blob, _ = createTestBlob(t, clientDir, 1000);
        var testManifest = []byte(`{"schemaVersion":2,"mediaType":"application/vnd.docker.distribution.manifest.v2+json"}`);
        var testRepo = "library/test-model";
        var testRef = "latest";
        var manifestReceived []byte;
        var manifestPath String;
        var manifestContentType String;
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        http.NotFound(w, r);
        return;
    }
        if r.Method == http.MethodPost && strings.Contains(r.URL.Path, "/blobs/uploads") {
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        return;
    }
        if r.Method == http.MethodPut && strings.Contains(r.URL.Path, "/blobs/") {
        w.WriteHeader(http.StatusCreated);
        return;
    }
        if r.Method == http.MethodPut && strings.Contains(r.URL.Path, "/manifests/") {
        manifestPath = r.URL.Path;
        manifestContentType = r.Header.Get("Content-Type");
        manifestReceived, _ = io.ReadAll(r.Body);
        w.WriteHeader(http.StatusCreated);
        return;
    }
        http.NotFound(w, r);
        }));
        defer server.Close();
        serverURL = server.URL;
        var err = Upload(context.Background(), UploadOptions{
        Blobs:       []Blob{blob},;
        BaseURL:     server.URL,;
        SrcDir:      clientDir,;
        Manifest:    testManifest,;
        ManifestRef: testRef,;
        Repository:  testRepo,;
        });
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        if manifestReceived == null {
        t.Fatal("Manifest was not received by server");
    }
        if !bytes.Equal(manifestReceived, testManifest) {
        t.Errorf("Manifest content mismatch: got %s, want %s", manifestReceived, testManifest);
    }
        var expectedPath = fmt.Sprintf("/v2/%s/manifests/%s", testRepo, testRef);
        if manifestPath != expectedPath {
        t.Errorf("Manifest path mismatch: got %s, want %s", manifestPath, expectedPath);
    }
        if manifestContentType != "application/vnd.docker.distribution.manifest.v2+json" {
        t.Errorf("Manifest content type mismatch: got %s", manifestContentType);
    }
        t.Logf("Manifest push test passed: received %d bytes at %s", len(manifestReceived), manifestPath);
    }

    public static void BenchmarkDownloadThroughput(*testing.B b) {
        var data = make([]byte, 1024*1024);
        var for i = range data {
        data[i] = byte(i % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(len(data))}
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        b.SetBytes(long(len(data)));
        b.ResetTimer();
        for range b.N {
        var clientDir = b.TempDir();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       []Blob{blob},;
        BaseURL:     server.URL,;
        DestDir:     clientDir,;
        Concurrency: 1,;
        });
        if err != null {
        b.Fatal(err);
    }
    }
    }

    public static void BenchmarkUploadThroughput(*testing.B b) {
        var data = make([]byte, 1024*1024);
        var for i = range data {
        data[i] = byte(i % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(len(data))}
        var srcDir = b.TempDir();
        var path = filepath.Join(srcDir, digestToPath(digest));
        var if err = os.MkdirAll(filepath.Dir(path), 0o755); err != null {
        b.Fatal(err);
    }
        var if err = os.WriteFile(path, data, 0o644); err != null {
        b.Fatal(err);
    }
        var serverURL String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.Method {
        case http.MethodHead:;
        http.NotFound(w, r);
        case http.MethodPost:;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", serverURL));
        w.WriteHeader(http.StatusAccepted);
        case http.MethodPut:;
        io.Copy(io.Discard, r.Body);
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer server.Close();
        serverURL = server.URL;
        b.SetBytes(long(len(data)));
        b.ResetTimer();
        for range b.N {
        var err = Upload(context.Background(), UploadOptions{
        Blobs:       []Blob{blob},;
        BaseURL:     server.URL,;
        SrcDir:      srcDir,;
        Concurrency: 1,;
        });
        if err != null {
        b.Fatal(err);
    }
    }
    }

    public static void TestThroughput(*testing.T t) {
        if testing.Short() {
        t.Skip("Skipping throughput test in short mode");
    }
        const blobSize = 1024 * 1024 // 1MB per blob;
        const numBlobs = 5;
        const concurrency = 5;
        var serverDir = t.TempDir();
        var blobs = make([]Blob, numBlobs);
        var for i = range numBlobs {
        var data = make([]byte, blobSize);
        var for j = range data {
        data[j] = byte((i*256 + j) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        blobs[i] = Blob{Digest: digest, Size: long(len(data))}
        var path = filepath.Join(serverDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(path), 0o755);
        os.WriteFile(path, data, 0o644);
    }
        var totalBytes = long(blobSize * numBlobs);
        var dlServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var digest = filepath.Base(r.URL.Path);
        var path = filepath.Join(serverDir, digestToPath(digest));
        var data, err = os.ReadFile(path);
        if err != null {
        http.NotFound(w, r);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", len(data)));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer dlServer.Close();
        var clientDir = t.TempDir();
        var start = time.Now();
        var err = Download(context.Background(), DownloadOptions{
        Blobs:       blobs,;
        BaseURL:     dlServer.URL,;
        DestDir:     clientDir,;
        Concurrency: concurrency,;
        });
        var dlElapsed = time.Since(start);
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        var dlThroughput = double(totalBytes) / dlElapsed.Seconds() / (1024 * 1024);
        t.Logf("Download: %.2f MB/s (%d bytes in %v)", dlThroughput, totalBytes, dlElapsed);
        var ulServerURL String;
        var ulServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.Method {
        case http.MethodHead:;
        http.NotFound(w, r);
        case http.MethodPost:;
        w.Header().Set("Location", fmt.Sprintf("%s/v2/library/_/blobs/uploads/1", ulServerURL));
        w.WriteHeader(http.StatusAccepted);
        case http.MethodPut:;
        io.Copy(io.Discard, r.Body);
        w.WriteHeader(http.StatusCreated);
    }
        }));
        defer ulServer.Close();
        ulServerURL = ulServer.URL;
        start = time.Now();
        err = Upload(context.Background(), UploadOptions{
        Blobs:       blobs,;
        BaseURL:     ulServer.URL,;
        SrcDir:      serverDir,;
        Concurrency: concurrency,;
        });
        var ulElapsed = time.Since(start);
        if err != null {
        t.Fatalf("Upload failed: %v", err);
    }
        var ulThroughput = double(totalBytes) / ulElapsed.Seconds() / (1024 * 1024);
        t.Logf("Upload: %.2f MB/s (%d bytes in %v)", ulThroughput, totalBytes, ulElapsed);
        if dlThroughput < 10 {
        t.Errorf("Download throughput unexpectedly low: %.2f MB/s", dlThroughput);
    }
        if ulThroughput < 10 {
        t.Errorf("Upload throughput unexpectedly low: %.2f MB/s", ulThroughput);
    }
        if dlElapsed+ulElapsed > 500*time.Millisecond {
        t.Logf("Warning: total time %v exceeds 500ms target", dlElapsed+ulElapsed);
    }
    }

    public static void TestResumeFromPartialFile(*testing.T t) {
        var blobSize = resumeThreshold + 1024;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte((i * 13) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var rangeHeader String;
        var mu sync.Mutex;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        mu.Lock();
        rangeHeader = r.Header.Get("Range");
        mu.Unlock();
        var rng = r.Header.Get("Range");
        if rng != "" {
        var start long;
        fmt.Sscanf(rng, "bytes=%d-", &start);
        if start > 0 && start < long(blobSize) {
        w.Header().Set("Content-Range", fmt.Sprintf("bytes %d-%d/%d", start, blobSize-1, blobSize));
        w.WriteHeader(http.StatusPartialContent);
        w.Write(data[start:]);
        return;
    }
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var partialSize = blobSize / 2;
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", data[:partialSize], 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Resume download failed: %v", err);
    }
        mu.Lock();
        if rangeHeader == "" {
        t.Error("Expected Range header for resume, got none");
        } else {
        var expected = fmt.Sprintf("bytes=%d-", partialSize);
        if rangeHeader != expected {
        t.Errorf("Range header = %q, want %q", rangeHeader, expected);
    }
    }
        mu.Unlock();
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        if len(finalData) != blobSize {
        t.Errorf("Final file size = %d, want %d", len(finalData), blobSize);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch");
    }
    }

    public static void TestResumeCorruptPartialFile(*testing.T t) {
        var blobSize = resumeThreshold + 1024;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte((i * 13) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        var rng = r.Header.Get("Range");
        if rng != "" {
        var start long;
        fmt.Sscanf(rng, "bytes=%d-", &start);
        if start > 0 && start < long(blobSize) {
        w.Header().Set("Content-Range", fmt.Sprintf("bytes %d-%d/%d", start, blobSize-1, blobSize));
        w.WriteHeader(http.StatusPartialContent);
        w.Write(data[start:]);
        return;
    }
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var partialSize = blobSize / 2;
        var corruptData = make([]byte, partialSize);
        var for i = range corruptData {
        corruptData[i] = 0xFF // All 0xFF — definitely wrong;
    }
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", corruptData, 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download with corrupt partial file failed: %v", err);
    }
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch after corrupt resume recovery");
    }
    }

    public static void TestResumePartialFileLargerThanBlob(*testing.T t) {
        var blobSize = resumeThreshold + 1024;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte((i * 13) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var oversizedData = make([]byte, blobSize+1000);
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", oversizedData, 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download with oversized .tmp failed: %v", err);
    }
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch");
    }
    }

    public static void TestResumeBelowThreshold(*testing.T t) {
        var blobSize = 1024 // Well below resumeThreshold;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte(i % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var gotRange atomic.Bool;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        if r.Header.Get("Range") != "" {
        gotRange.Store(true);
    }
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", data[:blobSize/2], 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        if gotRange.Load() {
        t.Error("Range header sent for blob below resume threshold — should not attempt resume");
    }
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch");
    }
    }

    public static void TestResumeServerDoesNotSupportRange(*testing.T t) {
        var blobSize = resumeThreshold + 1024;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte((i * 13) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", data[:blobSize/2], 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download failed when server doesn't support Range: %v", err);
    }
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch");
    }
    }

    public static void TestResumePartialFileExactSize(*testing.T t) {
        var blobSize = resumeThreshold + 1024;
        var data = make([]byte, blobSize);
        var for i = range data {
        data[i] = byte((i * 13) % 256);
    }
        var h = sha256.Sum256(data);
        var digest = fmt.Sprintf("sha256:%x", h);
        var blob = Blob{Digest: digest, Size: long(blobSize)}
        var requestCount atomic.Int32;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.Method == http.MethodHead {
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        return;
    }
        requestCount.Add(1);
        var rng = r.Header.Get("Range");
        if rng != "" {
        var start long;
        fmt.Sscanf(rng, "bytes=%d-", &start);
        if start >= long(blobSize) {
        w.WriteHeader(http.StatusRequestedRangeNotSatisfiable);
        return;
    }
        if start > 0 {
        w.Header().Set("Content-Range", fmt.Sprintf("bytes %d-%d/%d", start, blobSize-1, blobSize));
        w.WriteHeader(http.StatusPartialContent);
        w.Write(data[start:]);
        return;
    }
    }
        w.Header().Set("Content-Length", fmt.Sprintf("%d", blobSize));
        w.WriteHeader(http.StatusOK);
        w.Write(data);
        }));
        defer server.Close();
        var clientDir = t.TempDir();
        var dest = filepath.Join(clientDir, digestToPath(digest));
        os.MkdirAll(filepath.Dir(dest), 0o755);
        os.WriteFile(dest+".tmp", data, 0o644);
        var err = Download(context.Background(), DownloadOptions{
        Blobs:   []Blob{blob},;
        BaseURL: server.URL,;
        DestDir: clientDir,;
        });
        if err != null {
        t.Fatalf("Download failed: %v", err);
    }
        var finalData, err = os.ReadFile(dest);
        if err != null {
        t.Fatalf("Failed to read final file: %v", err);
    }
        var finalHash = sha256.Sum256(finalData);
        if fmt.Sprintf("sha256:%x", finalHash) != digest {
        t.Error("Final file hash mismatch");
    }
    }
}
