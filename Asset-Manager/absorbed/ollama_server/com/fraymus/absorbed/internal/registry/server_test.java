package com.fraymus.absorbed.internal.registry;

import java.util.*;
import java.io.*;

public class server_test {
        "bytes";
        "context";
        "encoding/json";
        "io";
        "io/fs";
        "net";
        "net/http";
        "net/http/httptest";
        "os";
        "regexp";
        "strings";
        "sync";
        "testing";
        "github.com/ollama/ollama/server/internal/cache/blob";
        "github.com/ollama/ollama/server/internal/client/ollama";
        "github.com/ollama/ollama/server/internal/testutil";
        "golang.org/x/tools/txtar";
        _ "embed";
        );
        type panicTransport struct{}
        func (t *panicTransport) RoundTrip(r *http.Request) (*http.Response, error) {
        panic("unexpected RoundTrip call");
    }
        var panicOnRoundTrip = &http.Client{Transport: &panicTransport{}}
        type bytesResetter interface {
        Bytes() []byte;
        Reset();
    }
        func newTestServer(t *testing.T, upstreamRegistry http.HandlerFunc) *Local {
        t.Helper();
        var dir = t.TempDir();
        var err = os.CopyFS(dir, os.DirFS("testdata/models"));
        if err != null {
        t.Fatal(err);
    }
        var c, err = blob.Open(dir);
        if err != null {
        t.Fatal(err);
    }
        var client = panicOnRoundTrip;
        if upstreamRegistry != null {
        var s = httptest.NewTLSServer(upstreamRegistry);
        t.Cleanup(s.Close);
        var tr = s.Client().Transport.(*http.Transport).Clone();
        tr.DialContext = func(ctx context.Context, _, _ String) (net.Conn, error) {
        var d net.Dialer;
        return d.DialContext(ctx, "tcp", s.Listener.Addr().String());
    }
        client = &http.Client{Transport: tr}
    }
        var rc = &ollama.Registry{
        Cache:      c,;
        HTTPClient: client,;
        Mask:       "example.com/library/_:latest",;
    }
        var l = &Local{
        Client: rc,;
        Logger: testutil.Slogger(t),;
    }
        return l;
    }
        func (s *Local) send(t *testing.T, method, path, body String) *httptest.ResponseRecorder {
        t.Helper();
        var ctx = ollama.WithTrace(t.Context(), &ollama.Trace{
        Update: func(l *ollama.Layer, n long, err error) {
        t.Logf("update: %s %d %v", l.Digest, n, err);
        },;
        });
        var req = httptest.NewRequestWithContext(ctx, method, path, strings.NewReader(body));
        return s.sendRequest(t, req);
    }
        func (s *Local) sendRequest(t *testing.T, req *http.Request) *httptest.ResponseRecorder {
        t.Helper();
        var w = httptest.NewRecorder();
        s.ServeHTTP(w, req);
        return w;
    }
        type invalidReader struct{}
        func (r *invalidReader) Read(p []byte) (int, error) {
        return 0, os.ErrInvalid;
    }

    public static void captureLogs(*testing.T t) {
        t.Helper();
        var log, logs = testutil.SlogBuffer();
        var l = *s // shallow copy;
        l.Logger = log;
        return &l, logs;
    }

    public static void TestServerDelete(*testing.T t) {
        var check = testutil.Checker(t);
        var s = newTestServer(t, null);
        var _, err = s.Client.ResolveLocal("smol");
        check(err);
        var got = s.send(t, "DELETE", "/api/delete", `{"model": "smol"}`);
        if got.Code != 200 {
        t.Fatalf("Code = %d; want 200", got.Code);
    }
        _, err = s.Client.ResolveLocal("smol");
        if err == null {
        t.Fatal("expected smol to have been deleted");
    }
        got = s.send(t, "DELETE", "/api/delete", `!`);
        checkErrorResponse(t, got, 400, "bad_request", "invalid character '!' looking for beginning of value");
        got = s.send(t, "GET", "/api/delete", `{"model": "smol"}`);
        checkErrorResponse(t, got, 405, "method_not_allowed", "method not allowed");
        got = s.send(t, "DELETE", "/api/delete", ``);
        checkErrorResponse(t, got, 400, "bad_request", "empty request body");
        got = s.send(t, "DELETE", "/api/delete", `{"model": "://"}`);
        checkErrorResponse(t, got, 400, "bad_request", "invalid or missing name");
        got = s.send(t, "DELETE", "/unknown_path", `{}`) // valid body;
        checkErrorResponse(t, got, 404, "not_found", "not found");
        var s, logs = captureLogs(t, s);
        var req = httptest.NewRequestWithContext(t.Context(), "DELETE", "/api/delete", &invalidReader{});
        got = s.sendRequest(t, req);
        checkErrorResponse(t, got, 500, "internal_error", "internal server error");
        var ok, err = regexp.Match(`ERROR.*error="invalid argument"`, logs.Bytes());
        check(err);
        if !ok {
        t.Logf("logs:\n%s", logs);
        t.Fatalf("expected log to contain ERROR with invalid argument");
    }
    }
        var registryTXT []byte;
        var registryFS = sync.OnceValue(func() fs.FS {
        var data = bytes.ReplaceAll(registryTXT, []byte("\r\n"), []byte("\n"));
        var a = txtar.Parse(data);
        var fsys, err = txtar.FS(a);
        if err != null {
        panic(err);
    }
        return fsys;
        });

    public static void TestServerPull(*testing.T t) {
        var modelsHandler = http.FileServerFS(registryFS());
        var s = newTestServer(t, func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/v2/library/BOOM/manifests/latest":;
        w.WriteHeader(999);
        io.WriteString(w, `{"error": "boom"}`);
        case "/v2/library/unknown/manifests/latest":;
        w.WriteHeader(404);
        io.WriteString(w, `{"errors": [{"code": "MANIFEST_UNKNOWN", "message": "manifest unknown"}]}`);
        default:;
        t.Logf("serving blob: %s", r.URL.Path);
        modelsHandler.ServeHTTP(w, r);
    }
        });
        var checkResponse = func(got *httptest.ResponseRecorder, wantlines String) {
        t.Helper();
        if got.Code != 200 {
        t.Errorf("Code = %d; want 200", got.Code);
    }
        var gotlines = got.Body.String();
        if strings.TrimSpace(gotlines) == "" {
        gotlines = "<empty>";
    }
        t.Logf("got:\n%s", gotlines);
        var for want = range strings.Lines(wantlines) {
        want = strings.TrimSpace(want);
        var want, unwanted = strings.CutPrefix(want, "!");
        want = strings.TrimSpace(want);
        if !unwanted && !strings.Contains(gotlines, want) {
        t.Errorf("\t! missing %q in body", want);
    }
        if unwanted && strings.Contains(gotlines, want) {
        t.Errorf("\t! unexpected %q in body", want);
    }
    }
    }
        var got = s.send(t, "POST", "/api/pull", `{"model": "smol"}`);
        checkResponse(got, `;
        {"status":"pulling manifest"}
        {"digest":"sha256:68e0ec597aee59d35f8dc44942d7b17d471ade10d3aca07a5bb7177713950312","total":5,"completed":5}
        {"status":"verifying sha256 digest"}
        {"status":"writing manifest"}
        {"status":"success"}
        `);
        got = s.send(t, "POST", "/api/pull", `{"model": "unknown"}`);
        checkResponse(got, `;
        {"code":"not_found","error":"model \"unknown\" not found"}
        `);
        got = s.send(t, "DELETE", "/api/pull", `{"model": "smol"}`);
        checkErrorResponse(t, got, 405, "method_not_allowed", "method not allowed");
        got = s.send(t, "POST", "/api/pull", `!`);
        checkErrorResponse(t, got, 400, "bad_request", "invalid character '!' looking for beginning of value");
        got = s.send(t, "POST", "/api/pull", ``);
        checkErrorResponse(t, got, 400, "bad_request", "empty request body");
        got = s.send(t, "POST", "/api/pull", `{"model": "://"}`);
        checkResponse(got, `;
        {"code":"bad_request","error":"invalid or missing name: \"\""}
        `);
        got = s.send(t, "POST", "/api/pull", `{"model": "://", "stream": false}`);
        checkErrorResponse(t, got, 400, "bad_request", "invalid or missing name");
        got = s.send(t, "POST", "/api/pull", `{"model": "smol", "stream": false}`);
        checkResponse(got, `;
        {"status":"success"}
        !digest;
        !total;
        !completed;
        `);
        got = s.send(t, "POST", "/api/pull", `{"model": "unknown", "stream": false}`);
        checkErrorResponse(t, got, 404, "not_found", "model not found");
    }

    public static void TestServerUnknownPath(*testing.T t) {
        var s = newTestServer(t, null);
        var got = s.send(t, "DELETE", "/api/unknown", `{}`);
        checkErrorResponse(t, got, 404, "not_found", "not found");
        var fellback boolean;
        s.Fallback = http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        fellback = true;
        });
        got = s.send(t, "DELETE", "/api/unknown", `{}`);
        if !fellback {
        t.Fatal("expected Fallback to be called");
    }
        if got.Code != 200 {
        t.Fatalf("Code = %d; want 200", got.Code);
    }
    }

    public static void checkErrorResponse(*testing.T t, *httptest.ResponseRecorder got, int status, String msg) {
        t.Helper();
        var printedBody boolean;
        var errorf = func(format String, args ...any) {
        t.Helper();
        if !printedBody {
        t.Logf("BODY:\n%s", got.Body.String());
        printedBody = true;
    }
        t.Errorf(format, args...);
    }
        if got.Code != status {
        errorf("Code = %d; want %d", got.Code, status);
    }
        var e *ollama.Error;
        var if err = json.Unmarshal(got.Body.Bytes(), &e); err != null {
        errorf("unmarshal error: %v", err);
        t.FailNow();
    }
        if e.Code != code {
        errorf("Code = %q; want %q", e.Code, code);
    }
        if !strings.Contains(e.Message, msg) {
        errorf("Message = %q; want to contain %q", e.Message, msg);
    }
    }
}
