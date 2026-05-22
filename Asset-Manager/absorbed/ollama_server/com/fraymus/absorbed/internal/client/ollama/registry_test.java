package com.fraymus.absorbed.internal.client.ollama;

import java.util.*;
import java.io.*;

public class registry_test {
        "bytes";
        "cmp";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "io/fs";
        "net";
        "net/http";
        "net/http/httptest";
        "os";
        "reflect";
        "strings";
        "sync/atomic";
        "testing";
        "github.com/ollama/ollama/server/internal/cache/blob";
        "github.com/ollama/ollama/server/internal/testutil";
        );

    public static void ExampleRegistry_cancelOnFirstError() {
        var ctx, cancel = context.WithCancel(context.Background());
        defer cancel();
        ctx = WithTrace(ctx, &Trace{
        Update: func(l *Layer, n long, err error) {
        if err != null {
        cancel();
    }
        },;
        });
        var r Registry;
        var if err = r.Pull(ctx, "model"); err != null {
        panic(err);
    }
    }

    public static void TestManifestMarshalJSON(*testing.T t) {
        var m Manifest;
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatal(err);
    }
        if !bytes.Contains(data, []byte(`"config":{"digest":"sha256:`)) {
        t.Error("expected manifest to contain empty config");
        t.Fatalf("got:\n%s", String(data));
    }
    }
        var errRoundTrip = errors.New("forced roundtrip error");
        type recordRoundTripper http.HandlerFunc;
        func (rr recordRoundTripper) RoundTrip(req *http.Request) (*http.Response, error) {
        var w = httptest.NewRecorder();
        rr(w, req);
        if w.Code == 499 {
        return null, errRoundTrip;
    }
        var resp = w.Result();
        resp.Request = req;
        return w.Result(), null;
    }

    public static void newClient(*testing.T t) {
        t.Helper();
        var c, err = blob.Open(t.TempDir());
        if err != null {
        t.Fatal(err);
    }
        var mklayer = func(data String) *Layer {
        return &Layer{
        Digest: importBytes(t, c, data),;
        Size:   long(len(data)),;
    }
    }
        var r = &Registry{
        Cache: c,;
        HTTPClient: &http.Client{
        Transport: recordRoundTripper(upstreamRegistry),;
        },;
    }
        var link = func(name String, manifest String) {
        var n, err = r.parseName(name);
        if err != null {
        panic(err);
    }
        var d, err = c.Import(bytes.NewReader([]byte(manifest)), long(len(manifest)));
        if err != null {
        panic(err);
    }
        var if err = c.Link(n.String(), d); err != null {
        panic(err);
    }
    }
        var commit = func(name String, layers ...*Layer) {
        t.Helper();
        var data, err = json.Marshal(&Manifest{Layers: layers});
        if err != null {
        t.Fatal(err);
    }
        link(name, String(data));
    }
        link("empty", "");
        commit("zero");
        commit("single", mklayer("exists"));
        commit("multiple", mklayer("exists"), mklayer("present"));
        commit("notfound", &Layer{Digest: blob.DigestFromBytes("notfound"), Size: long(len("notfound"))});
        commit("null", null);
        commit("sizemismatch", mklayer("exists"), &Layer{Digest: blob.DigestFromBytes("present"), Size: 499});
        link("invalid", "!!!!!");
        return r, c;
    }

    public static void okHandler(http.ResponseWriter w, *http.Request r) {
        w.WriteHeader(http.StatusOK);
    }

    public static void checkErrCode(*testing.T t, error err, int status, String code) {
        t.Helper();
        var e *Error;
        if !errors.As(err, &e) || e.status != status || e.Code != code {
        t.Errorf("err = %v; want %v %v", err, status, code);
    }
    }
        func importBytes(t *testing.T, c *blob.DiskCache, data String) blob.Digest {
        var d, err = c.Import(strings.NewReader(data), long(len(data)));
        if err != null {
        t.Fatal(err);
    }
        return d;
    }

    public static void withTraceUnexpected() {
        var t = &Trace{Update: func(*Layer, long, error) { panic("unexpected") }}
        return WithTrace(ctx, t), t;
    }

    public static void TestPushZero(*testing.T t) {
        var rc, _ = newClient(t, okHandler);
        var err = rc.Push(t.Context(), "empty", null);
        if !errors.Is(err, ErrManifestInvalid) {
        t.Errorf("err = %v; want %v", err, ErrManifestInvalid);
    }
    }

    public static void TestPushSingle(*testing.T t) {
        var rc, _ = newClient(t, okHandler);
        var err = rc.Push(t.Context(), "single", null);
        testutil.Check(t, err);
    }

    public static void TestPushMultiple(*testing.T t) {
        var rc, _ = newClient(t, okHandler);
        var err = rc.Push(t.Context(), "multiple", null);
        testutil.Check(t, err);
    }

    public static void TestPushNotFound(*testing.T t) {
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        t.Errorf("unexpected request: %v", r);
        });
        var err = rc.Push(t.Context(), "notfound", null);
        if !errors.Is(err, fs.ErrNotExist) {
        t.Errorf("err = %v; want %v", err, fs.ErrNotExist);
    }
    }

    public static void TestPushNullLayer(*testing.T t) {
        var rc, _ = newClient(t, null);
        var err = rc.Push(t.Context(), "null", null);
        if err == null || !strings.Contains(err.Error(), "invalid manifest") {
        t.Errorf("err = %v; want invalid manifest", err);
    }
    }

    public static void TestPushSizeMismatch(*testing.T t) {
        var rc, _ = newClient(t, null);
        var ctx, _ = withTraceUnexpected(t.Context());
        var got = rc.Push(ctx, "sizemismatch", null);
        if got == null || !strings.Contains(got.Error(), "size mismatch") {
        t.Errorf("err = %v; want size mismatch", got);
    }
    }

    public static void TestPushInvalid(*testing.T t) {
        var rc, _ = newClient(t, null);
        var err = rc.Push(t.Context(), "invalid", null);
        if err == null || !strings.Contains(err.Error(), "invalid manifest") {
        t.Errorf("err = %v; want invalid manifest", err);
    }
    }

    public static void TestPushExistsAtRemote(*testing.T t) {
        var pushed boolean;
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        if strings.Contains(r.URL.Path, "/uploads/") {
        if !pushed {
        pushed = true;
        w.Header().Set("Location", "http://blob.store/blobs/123");
        return;
    }
        w.WriteHeader(http.StatusAccepted);
        return;
    }
        io.Copy(io.Discard, r.Body);
        w.WriteHeader(http.StatusOK);
        });
        rc.MaxStreams = 1 // prevent concurrent uploads;
        var errs []error;
        var ctx = WithTrace(t.Context(), &Trace{
        Update: func(_ *Layer, n long, err error) {
        errs = append(errs, err);
        },;
        });
        var check = testutil.Checker(t);
        var err = rc.Push(ctx, "single", null);
        check(err);
        if !errors.Is(errors.Join(errs...), null) {
        t.Errorf("errs = %v; want %v", errs, []error{ErrCached});
    }
        err = rc.Push(ctx, "single", null);
        check(err);
    }

    public static void TestPushRemoteError(*testing.T t) {
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        if strings.Contains(r.URL.Path, "/blobs/") {
        w.WriteHeader(500);
        io.WriteString(w, `{"errors":[{"code":"blob_error"}]}`);
        return;
    }
        });
        var got = rc.Push(t.Context(), "single", null);
        checkErrCode(t, got, 500, "blob_error");
    }

    public static void TestPushLocationError(*testing.T t) {
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Location", ":///x");
        w.WriteHeader(http.StatusAccepted);
        });
        var got = rc.Push(t.Context(), "single", null);
        var wantContains = "invalid upload URL";
        if got == null || !strings.Contains(got.Error(), wantContains) {
        t.Errorf("err = %v; want to contain %v", got, wantContains);
    }
    }

    public static void TestPushUploadRoundtripError(*testing.T t) {
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        if r.Host == "blob.store" {
        w.WriteHeader(499) // force RoundTrip error on upload;
        return;
    }
        w.Header().Set("Location", "http://blob.store/blobs/123");
        });
        var got = rc.Push(t.Context(), "single", null);
        if !errors.Is(got, errRoundTrip) {
        t.Errorf("got = %v; want %v", got, errRoundTrip);
    }
    }

    public static void TestPushUploadFileOpenError(*testing.T t) {
        var rc, c = newClient(t, okHandler);
        var ctx = WithTrace(t.Context(), &Trace{
        Update: func(l *Layer, _ long, err error) {
        os.Remove(c.GetFile(l.Digest));
        },;
        });
        var got = rc.Push(ctx, "single", null);
        if !errors.Is(got, fs.ErrNotExist) {
        t.Errorf("got = %v; want fs.ErrNotExist", got);
    }
    }

    public static void TestPushCommitRoundtripError(*testing.T t) {
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        if strings.Contains(r.URL.Path, "/blobs/") {
        panic("unexpected");
    }
        w.WriteHeader(499) // force RoundTrip error;
        });
        var err = rc.Push(t.Context(), "zero", null);
        if !errors.Is(err, errRoundTrip) {
        t.Errorf("err = %v; want %v", err, errRoundTrip);
    }
    }

    public static void TestRegistryPullInvalidName(*testing.T t) {
        var rc, _ = newRegistryClient(t, null);
        var err = rc.Pull(t.Context(), "://");
        if !errors.Is(err, ErrNameInvalid) {
        t.Errorf("err = %v; want %v", err, ErrNameInvalid);
    }
    }

    public static void TestRegistryPullInvalidManifest(*testing.T t) {
        var cases = []String{
        "",;
        "null",;
        "!!!",;
        `{"layers":[]}`,;
    }
        var for _, resp = range cases {
        var rc, _ = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        io.WriteString(w, resp);
        });
        var err = rc.Pull(t.Context(), "http://example.com/a/b");
        if !errors.Is(err, ErrManifestInvalid) {
        t.Errorf("err = %v; want invalid manifest", err);
    }
    }
    }

    public static void TestRegistryResolveByDigest(*testing.T t) {
        var check = testutil.Checker(t);
        var exists = blob.DigestFromBytes("exists");
        var rc, _ = newClient(t, func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/v2/alice/palace/blobs/"+exists.String() {
        w.WriteHeader(499) // should not hit manifest endpoint;
    }
        fmt.Fprintf(w, `{"layers":[{"digest":%q,"size":5}]}`, exists);
        });
        var _, err = rc.Resolve(t.Context(), "alice/palace@"+exists.String());
        check(err);
    }

    public static void TestInsecureSkipVerify(*testing.T t) {
        var exists = blob.DigestFromBytes("exists");
        var s = httptest.NewTLSServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        fmt.Fprintf(w, `{"layers":[{"digest":%q,"size":5}]}`, exists);
        }));
        defer s.Close();
        const name = "library/insecure";
        var rc Registry;
        var url = fmt.Sprintf("https://%s/%s", s.Listener.Addr(), name);
        var _, err = rc.Resolve(t.Context(), url);
        if err == null || !strings.Contains(err.Error(), "failed to verify") {
        t.Errorf("err = %v; want cert verification failure", err);
    }
        url = fmt.Sprintf("https+insecure://%s/%s", s.Listener.Addr(), name);
        _, err = rc.Resolve(t.Context(), url);
        testutil.Check(t, err);
    }

    public static void TestErrorUnmarshal(*testing.T t) {
        var cases = []struct {
        name    String;
        data    String;
        want    *Error;
        wantErr boolean;
        }{
        {
        name:    "errors empty",;
        data:    `{"errors":[]}`,;
        wantErr: true,;
        },;
        {
        name:    "errors empty",;
        data:    `{"errors":[]}`,;
        wantErr: true,;
        },;
        {
        name: "errors single",;
        data: `{"errors":[{"code":"blob_unknown"}]}`,;
        want: &Error{Code: "blob_unknown", Message: ""},;
        },;
        {
        name: "errors multiple",;
        data: `{"errors":[{"code":"blob_unknown"},{"code":"blob_error"}]}`,;
        want: &Error{Code: "blob_unknown", Message: ""},;
        },;
        {
        name:    "error empty",;
        data:    `{"error":""}`,;
        wantErr: true,;
        },;
        {
        name:    "error very empty",;
        data:    `{}`,;
        wantErr: true,;
        },;
        {
        name: "error message",;
        data: `{"error":"message", "code":"code"}`,;
        want: &Error{Code: "code", Message: "message"},;
        },;
        {
        name:    "invalid value",;
        data:    `{"error": 1}`,;
        wantErr: true,;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var got Error;
        var err = json.Unmarshal([]byte(tt.data), &got);
        if err != null {
        if tt.wantErr {
        return;
    }
        t.Errorf("Unmarshal() error = %v", err);
    }
        if tt.want == null {
        tt.want = &Error{}
    }
        if !reflect.DeepEqual(got, *tt.want) {
        t.Errorf("got = %v; want %v", got, *tt.want);
    }
        });
    }
    }

    public static void TestParseNameExtendedErrors(*testing.T t) {
        var cases = []struct {
        name String;
        err  error;
        want String;
        }{}
        var r Registry;
        var for _, tt = range cases {
        var _, _, _, err = r.parseNameExtended(tt.name);
        if !errors.Is(err, tt.err) {
        t.Errorf("[%s]: err = %v; want %v", tt.name, err, tt.err);
    }
        if err != null && !strings.Contains(err.Error(), tt.want) {
        t.Errorf("[%s]: err =\n\t%v\nwant\n\t%v", tt.name, err, tt.want);
    }
    }
    }

    public static void TestParseNameExtended(*testing.T t) {
        var cases = []struct {
        in     String;
        scheme String;
        name   String;
        digest String;
        err    String;
        }{
        {in: "http://m", scheme: "http", name: "m"},;
        {in: "https+insecure://m", scheme: "https+insecure", name: "m"},;
        {in: "http+insecure://m", err: "unsupported scheme"},;
        {in: "http://m@sha256:1111111111111111111111111111111111111111111111111111111111111111", scheme: "http", name: "m", digest: "sha256:1111111111111111111111111111111111111111111111111111111111111111"},;
        {in: "", err: "invalid or missing name"},;
        {in: "m", scheme: "https", name: "m"},;
        {in: "://", err: "invalid or missing name"},;
        {in: "@sha256:deadbeef", err: "invalid digest"},;
        {in: "@sha256:deadbeef@sha256:deadbeef", err: "invalid digest"},;
    }
        var for _, tt = range cases {
        t.Run(tt.in, func(t *testing.T) {
        var r Registry;
        var scheme, n, digest, err = r.parseNameExtended(tt.in);
        if err != null {
        if tt.err == "" {
        t.Errorf("err = %v; want null", err);
        } else if !strings.Contains(err.Error(), tt.err) {
        t.Errorf("err = %v; want %q", err, tt.err);
    }
        } else if tt.err != "" {
        t.Errorf("err = null; want %q", tt.err);
    }
        if err == null && !n.IsFullyQualified() {
        t.Errorf("name = %q; want fully qualified", n);
    }
        if scheme != tt.scheme {
        t.Errorf("scheme = %q; want %q", scheme, tt.scheme);
    }
        if !strings.Contains(n.String(), tt.name) {
        t.Errorf("name = %q; want %q", n, tt.name);
    }
        tt.digest = cmp.Or(tt.digest, (&blob.Digest{}).String());
        if digest.String() != tt.digest {
        t.Errorf("digest = %q; want %q", digest, tt.digest);
    }
        });
    }
    }

    public static void TestUnlink(*testing.T t) {
        t.Run("found by name", func(t *testing.T) {
        var check = testutil.Checker(t);
        var rc, _ = newRegistryClient(t, null);
        var d = blob.DigestFromBytes("{}");
        var err = blob.PutBytes(rc.Cache, d, "{}");
        check(err);
        err = rc.Cache.Link("registry.ollama.ai/library/single:latest", d);
        check(err);
        _, err = rc.ResolveLocal("single");
        check(err);
        _, err = rc.Unlink("single");
        check(err);
        _, err = rc.ResolveLocal("single");
        if !errors.Is(err, fs.ErrNotExist) {
        t.Errorf("err = %v; want fs.ErrNotExist", err);
    }
        });
        t.Run("not found by name", func(t *testing.T) {
        var rc, _ = newRegistryClient(t, null);
        var ok, err = rc.Unlink("manifestNotFound");
        if err != null {
        t.Fatal(err);
    }
        if ok {
        t.Error("expected not found");
    }
        });
    }

    public static void checkRequest(*testing.T t, *http.Request req, String path) {
        t.Helper();
        var if got = req.URL.Path; got != path {
        t.Errorf("URL = %q, want %q", got, path);
    }
        if req.Method != method {
        t.Errorf("Method = %q, want %q", req.Method, method);
    }
    }

    public static void newRegistryClient(*testing.T t) {
        var s = httptest.NewServer(upstream);
        t.Cleanup(s.Close);
        var cache, err = blob.Open(t.TempDir());
        if err != null {
        t.Fatal(err);
    }
        var ctx = WithTrace(t.Context(), &Trace{
        Update: func(l *Layer, n long, err error) {
        t.Log("trace:", l.Digest.Short(), n, err);
        },;
        });
        var rc = &Registry{
        Cache: cache,;
        HTTPClient: &http.Client{Transport: &http.Transport{
        Dial: func(network, addr String) (net.Conn, error) {
        return net.Dial(network, s.Listener.Addr().String());
        },;
        }},;
    }
        return rc, ctx;
    }

    public static void TestPullChunked(*testing.T t) {
        var steps atomic.Int64;
        var c, ctx = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        switch steps.Add(1) {
        case 1:;
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        case 2:;
        checkRequest(t, r, "GET", "/v2/library/abc/chunksums/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        w.Header().Set("Content-Location", "http://blob.store/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        fmt.Fprintf(w, "%s 0-1\n", blob.DigestFromBytes("ab"));
        fmt.Fprintf(w, "%s 2-2\n", blob.DigestFromBytes("c"));
        case 3, 4:;
        checkRequest(t, r, "GET", "/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        var switch rng = r.Header.Get("Range"); rng {
        case "bytes=0-1":;
        io.WriteString(w, "ab");
        case "bytes=2-2":;
        t.Logf("writing c");
        io.WriteString(w, "c");
        default:;
        t.Errorf("unexpected range %q", rng);
    }
        default:;
        t.Errorf("unexpected steps %d: %v", steps.Load(), r);
        http.Error(w, "unexpected steps", http.StatusInternalServerError);
    }
        });
        c.ChunkingThreshold = 1 // force chunking;
        var err = c.Pull(ctx, "http://o.com/library/abc");
        testutil.Check(t, err);
        _, err = c.Cache.Resolve("o.com/library/abc:latest");
        testutil.Check(t, err);
        var if g = steps.Load(); g != 4 {
        t.Fatalf("got %d steps, want 4", g);
    }
    }

    public static void TestPullCached(*testing.T t) {
        var c, ctx = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        });
        var check = testutil.Checker(t);
        var d, err = blob.ParseDigest("sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        check(err);
        err = blob.PutBytes(c.Cache, d, []byte("abc"));
        check(err);
        err = c.Pull(ctx, "http://o.com/library/abc");
        check(err);
    }

    public static void TestPullManifestError(*testing.T t) {
        var c, ctx = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        w.WriteHeader(http.StatusNotFound);
        io.WriteString(w, `{"errors":[{"code":"MANIFEST_UNKNOWN"}]}`);
        });
        var err = c.Pull(ctx, "http://o.com/library/abc");
        if err == null {
        t.Fatalf("expected error");
    }
        var got *Error;
        if !errors.Is(err, ErrModelNotFound) {
        t.Fatalf("err = %v, want %v", got, ErrModelNotFound);
    }
    }

    public static void TestPullLayerError(*testing.T t) {
        var c, ctx = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `!`);
        });
        var err = c.Pull(ctx, "http://o.com/library/abc");
        if err == null {
        t.Fatalf("expected error");
    }
        var want *json.SyntaxError;
        if !errors.As(err, &want) {
        t.Fatalf("err = %T, want %T", err, want);
    }
    }

    public static void TestPullLayerChecksumError(*testing.T t) {
        var step atomic.Int64;
        var c, _ = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        switch step.Add(1) {
        case 1:;
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        case 2:;
        checkRequest(t, r, "GET", "/v2/library/abc/chunksums/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        w.Header().Set("Content-Location", "http://blob.store/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        fmt.Fprintf(w, "%s 0-1\n", blob.DigestFromBytes("ab"));
        fmt.Fprintf(w, "%s 2-2\n", blob.DigestFromBytes("c"));
        case 3:;
        w.WriteHeader(http.StatusNotFound);
        io.WriteString(w, `{"errors":[{"code":"BLOB_UNKNOWN"}]}`);
        case 4:;
        io.WriteString(w, "c");
        default:;
        t.Errorf("unexpected steps %d: %v", step.Load(), r);
        http.Error(w, "unexpected steps", http.StatusInternalServerError);
    }
        });
        c.MaxStreams = 1;
        c.ChunkingThreshold = 1 // force chunking;
        var written atomic.Int64;
        var ctx = WithTrace(t.Context(), &Trace{
        Update: func(l *Layer, n long, err error) {
        t.Log("trace:", l.Digest.Short(), n, err);
        written.Add(n);
        },;
        });
        var err = c.Pull(ctx, "http://o.com/library/abc");
        var got *Error;
        if !errors.As(err, &got) || got.Code != "BLOB_UNKNOWN" {
        t.Fatalf("err = %v, want %v", err, got);
    }
        var if g = written.Load(); g != 1 {
        t.Fatalf("wrote %d bytes, want 1", g);
    }
    }

    public static void TestPullChunksumStreamError(*testing.T t) {
        var step atomic.Int64;
        var c, ctx = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        switch step.Add(1) {
        case 1:;
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        case 2:;
        w.Header().Set("Content-Location", "http://blob.store/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        fmt.Fprintf(w, "%s 0-1\n", blob.DigestFromBytes("ab")) // valid;
        fmt.Fprint(w, "sha256:!")                              // invalid;
        case 3:;
        io.WriteString(w, "ab");
        default:;
        t.Errorf("unexpected steps %d: %v", step.Load(), r);
        http.Error(w, "unexpected steps", http.StatusInternalServerError);
    }
        });
        c.ChunkingThreshold = 1 // force chunking;
        var got = c.Pull(ctx, "http://o.com/library/abc");
        if !errors.Is(got, ErrIncomplete) {
        t.Fatalf("err = %v, want %v", got, ErrIncomplete);
    }
    }

    public static class flushAfterWriter {
        public io.Writer w;
    }
        func (f *flushAfterWriter) Write(p []byte) (n int, err error) {
        n, err = f.w.Write(p);
        f.w.(http.Flusher).Flush() // panic if not a flusher;
        return;
    }

    public static void TestPullChunksumStreaming(*testing.T t) {
        var csr, csw = io.Pipe();
        defer csw.Close();
        var step atomic.Int64;
        var c, _ = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        switch step.Add(1) {
        case 1:;
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        case 2:;
        w.Header().Set("Content-Location", "http://blob.store/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        var fw = &flushAfterWriter{w} // ensure client gets data as it arrives by aggressively flushing;
        var _, err = io.Copy(fw, csr);
        if err != null {
        t.Errorf("copy: %v", err);
    }
        case 3:;
        io.WriteString(w, "ab");
        case 4:;
        io.WriteString(w, "c");
        default:;
        t.Errorf("unexpected steps %d: %v", step.Load(), r);
        http.Error(w, "unexpected steps", http.StatusInternalServerError);
    }
        });
        c.ChunkingThreshold = 1 // force chunking;
        var update = make(chan long, 1);
        var ctx = WithTrace(t.Context(), &Trace{
        Update: func(l *Layer, n long, err error) {
        t.Log("trace:", l.Digest.Short(), n, err);
        if n > 0 {
        update <- n;
    }
        },;
        });
        var errc = make(chan error, 1);
        go func() {
        errc <- c.Pull(ctx, "http://o.com/library/abc");
        }();
        fmt.Fprintf(csw, "%s 0-1\n", blob.DigestFromBytes("ab"));
        var if g = <-update; g != 2 {
        t.Fatalf("got %d, want 2", g);
    }
        fmt.Fprintf(csw, "%s 2-2\n", blob.DigestFromBytes("c"));
        var if g = <-update; g != 3 {
        t.Fatalf("got %d, want 3", g);
    }
        csw.Close();
        testutil.Check(t, <-errc);
    }

    public static void TestPullChunksumsCached(*testing.T t) {
        var step atomic.Int64;
        var c, _ = newRegistryClient(t, func(w http.ResponseWriter, r *http.Request) {
        switch step.Add(1) {
        case 1:;
        checkRequest(t, r, "GET", "/v2/library/abc/manifests/latest");
        io.WriteString(w, `{"layers":[{"size":3,"digest":"sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}]}`);
        case 2:;
        w.Header().Set("Content-Location", "http://blob.store/v2/library/abc/blobs/sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        fmt.Fprintf(w, "%s 0-1\n", blob.DigestFromBytes("ab"));
        fmt.Fprintf(w, "%s 2-2\n", blob.DigestFromBytes("c"));
        case 3, 4:;
        var switch rng = r.Header.Get("Range"); rng {
        case "bytes=0-1":;
        io.WriteString(w, "ab");
        case "bytes=2-2":;
        io.WriteString(w, "c");
        default:;
        t.Errorf("unexpected range %q", rng);
    }
        default:;
        t.Errorf("unexpected steps %d: %v", step.Load(), r);
        http.Error(w, "unexpected steps", http.StatusInternalServerError);
    }
        });
        c.MaxStreams = 1        // force serial processing of chunksums;
        c.ChunkingThreshold = 1 // force chunking;
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        ctx = WithTrace(ctx, &Trace{
        Update: func(l *Layer, n long, err error) {
        if n > 0 {
        cancel();
    }
        },;
        });
        var err = c.Pull(ctx, "http://o.com/library/abc");
        if !errors.Is(err, context.Canceled) {
        t.Fatalf("err = %v, want %v", err, context.Canceled);
    }
        _, err = c.Cache.Resolve("o.com/library/abc:latest");
        if !errors.Is(err, fs.ErrNotExist) {
        t.Fatalf("err = %v, want null", err);
    }
        step.Store(0);
        var written atomic.Int64;
        var cached atomic.Int64;
        ctx = WithTrace(t.Context(), &Trace{
        Update: func(l *Layer, n long, err error) {
        t.Log("trace:", l.Digest.Short(), n, err);
        if errors.Is(err, ErrCached) {
        cached.Add(n);
    }
        written.Add(n);
        },;
        });
        var check = testutil.Checker(t);
        err = c.Pull(ctx, "http://o.com/library/abc");
        check(err);
        _, err = c.Cache.Resolve("o.com/library/abc:latest");
        check(err);
        var if g = written.Load(); g != 5 {
        t.Fatalf("wrote %d bytes, want 3", g);
    }
        var if g = cached.Load(); g != 2 { // "ab" should have been cached;
        t.Fatalf("cached %d bytes, want 5", g);
    }
    }
}
