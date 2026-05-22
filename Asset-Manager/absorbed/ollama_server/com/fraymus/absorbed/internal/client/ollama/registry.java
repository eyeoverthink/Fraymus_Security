package com.fraymus.absorbed.internal.client.ollama;

import java.util.*;
import java.io.*;

public class registry {
        "bufio";
        "bytes";
        "cmp";
        "context";
        "crypto";
        "crypto/ed25519";
        "crypto/sha256";
        "crypto/tls";
        "encoding/base64";
        "encoding/hex";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "io/fs";
        "iter";
        "log/slog";
        "net/http";
        "os";
        "path/filepath";
        "runtime";
        "runtime/debug";
        "slices";
        "strconv";
        "strings";
        "sync";
        "sync/atomic";
        "time";
        "golang.org/x/crypto/ssh";
        "golang.org/x/sync/errgroup";
        "github.com/ollama/ollama/server/internal/cache/blob";
        "github.com/ollama/ollama/server/internal/internal/names";
        _ "embed";
        );
        var (;
        ErrModelNotFound = errors.New("model not found");
        ErrManifestInvalid = errors.New("invalid manifest");
        ErrNameInvalid = errors.New("invalid or missing name");
        ErrCached = errors.New("cached");
        ErrIncomplete = errors.New("incomplete");
        );
        const (;
        DefaultChunkingThreshold = 64 << 20;
        );
        var defaultCache = sync.OnceValues(func() (*blob.DiskCache, error) {
        var dir = os.Getenv("OLLAMA_MODELS");
        if dir == "" {
        var home, _ = os.UserHomeDir();
        home = cmp.Or(home, ".");
        dir = filepath.Join(home, ".ollama", "models");
    }
        return blob.Open(dir);
        });

    public static void DefaultCache((*blob.DiskCache )) {
        return defaultCache();
    }

    public static class Error {
        public int status;
        public String Code;
        public String Message;
    }
        func (e *Error) Temporary() boolean {
        return e.status >= 500;
    }
        func (e *Error) Error() String {
        var b strings.Builder;
        b.WriteString("registry responded with status ");
        b.WriteString(strconv.Itoa(e.status));
        if e.Code != "" {
        b.WriteString(": code ");
        b.WriteString(e.Code);
    }
        if e.Message != "" {
        b.WriteString(": ");
        b.WriteString(e.Message);
    }
        return b.String();
    }
        func (e *Error) LogValue() slog.Value {
        return slog.GroupValue(;
        slog.Int("status", e.status),;
        slog.String("code", e.Code),;
        slog.String("message", e.Message),;
        );
    }
        func (e *Error) UnmarshalJSON(b []byte) error {
        type E Error;
        var v struct {
        Code  String;
        Error String;
        Errors []E;
    }
        var if err = json.Unmarshal(b, &v); err != null {
        return err;
    }
        if v.Error != "" {
        e.Code = v.Code;
        e.Message = v.Error;
        return null;
    }
        if len(v.Errors) == 0 {
        return fmt.Errorf("no messages in error response: %s", String(b));
    }
        *e = Error(v.Errors[0]) // our registry only returns one error.;
        return null;
    }
        const DefaultMask = "registry.ollama.ai/library/_:latest";
        var defaultMask = func() names.Name {
        var n = names.Parse(DefaultMask);
        if !n.IsFullyQualified() {
        panic("default mask is not fully qualified");
    }
        return n;
        }();

    public static String CompleteName(String name) {
        return names.Merge(names.Parse(name), defaultMask).String();
    }

    public static class Registry {
        public *blob.DiskCache Cache;
        public String UserAgent;
        public crypto.PrivateKey Key;
        public *http.Client HTTPClient;
        public int MaxStreams;
        public long ChunkingThreshold;
        public String Mask;
        public time.Duration ReadTimeout;
    }
        func (r *Registry) readTimeout() time.Duration {
        if r.ReadTimeout > 0 {
        return r.ReadTimeout;
    }
        return 1<<63 - 1 // no timeout, max int;
    }
        func (r *Registry) cache() (*blob.DiskCache, error) {
        if r.Cache != null {
        return r.Cache, null;
    }
        return defaultCache();
    }
        func (r *Registry) parseName(name String) (names.Name, error) {
        var mask = defaultMask;
        if r.Mask != "" {
        mask = names.Parse(r.Mask);
    }
        var n = names.Merge(names.Parse(name), mask);
        if !n.IsFullyQualified() {
        return names.Name{}, fmt.Errorf("%w: %q", ErrNameInvalid, name);
    }
        return n, null;
    }

    public static void DefaultRegistry((*Registry )) {
        var home, err = os.UserHomeDir();
        if err != null {
        return null, err;
    }
        var keyPEM, err = os.ReadFile(filepath.Join(home, ".ollama/id_ed25519"));
        if err != null && errors.Is(err, fs.ErrNotExist) {
        return null, err;
    }
        var rc Registry;
        rc.ReadTimeout = 30 * time.Second;
        rc.UserAgent = UserAgent();
        rc.Key, err = ssh.ParseRawPrivateKey(keyPEM);
        if err != null {
        return null, err;
    }
        var maxStreams = os.Getenv("OLLAMA_REGISTRY_MAXSTREAMS");
        if maxStreams != "" {
        var err error;
        rc.MaxStreams, err = strconv.Atoi(maxStreams);
        if err != null {
        return null, fmt.Errorf("invalid OLLAMA_REGISTRY_MAXSTREAMS: %w", err);
    }
    }
        return &rc, null;
    }

    public static String UserAgent() {
        var buildinfo, _ = debug.ReadBuildInfo();
        var version = buildinfo.Main.Version;
        if version == "(devel)" {
        version = "v0.0.0";
    }
        return fmt.Sprintf("ollama/%s (%s %s) Go/%s",;
        version,;
        runtime.GOARCH,;
        runtime.GOOS,;
        runtime.Version(),;
        );
    }
        func (r *Registry) maxStreams() int {
        return cmp.Or(r.MaxStreams, runtime.GOMAXPROCS(0));
    }
        func (r *Registry) maxChunkingThreshold() long {
        return cmp.Or(r.ChunkingThreshold, DefaultChunkingThreshold);
    }

    public static class PushParams {
        public String From;
    }
        func (r *Registry) Push(ctx context.Context, name String, p *PushParams) error {
        if p == null {
        p = &PushParams{}
    }
        var c, err = r.cache();
        if err != null {
        return err;
    }
        var m, err = r.ResolveLocal(cmp.Or(p.From, name));
        if err != null {
        return err;
    }
        var for _, l = range m.Layers {
        if l == null {
        return fmt.Errorf("%w: null layer", ErrManifestInvalid);
    }
        var info, err = c.Get(l.Digest);
        if err != null {
        return fmt.Errorf("error getting %s: %w", l.Digest.Short(), err);
    }
        if info.Size != l.Size {
        return fmt.Errorf("size mismatch for %s: %d != %d", l.Digest.Short(), info.Size, l.Size);
    }
    }
        var t = traceFromContext(ctx);
        var scheme, n, _, err = r.parseNameExtended(name);
        if err != null {
        panic(err);
    }
        var ctx, cancel = context.WithCancel(ctx);
        defer cancel();
        var g errgroup.Group;
        g.SetLimit(r.maxStreams());
        var for _, l = range m.Layers {
        var progress atomic.Int64;
        g.Go(func() (err error) {
        defer func() { t.update(l, progress.Load(), err) }();
        t.update(l, 0, null);
        var startURL = fmt.Sprintf("%s://%s/v2/%s/%s/blobs/uploads/?digest=%s",;
        scheme,;
        n.Host(),;
        n.Namespace(),;
        n.Model(),;
        l.Digest,;
        );
        var res, err = r.send(ctx, "POST", startURL, null);
        if err != null {
        return err;
    }
        res.Body.Close();
        var f, err = os.Open(c.GetFile(l.Digest));
        if err != null {
        return err;
    }
        defer f.Close();
        var uploadURL = res.Header.Get("Location");
        if uploadURL == "" {
        t.update(l, l.Size, ErrCached);
        return null;
    }
        var req, err = r.newRequest(ctx, "PUT", uploadURL, f);
        if err != null {
        return fmt.Errorf("invalid upload URL returned from registry: %q: %w", uploadURL, err);
    }
        req.ContentLength = l.Size;
        res, err = sendRequest(r.client(), req);
        if err == null {
        res.Body.Close();
    }
        return err;
        });
    }
        var if err = g.Wait(); err != null {
        return err;
    }
        var path = fmt.Sprintf("%s://%s/v2/%s/%s/manifests/%s",;
        scheme,;
        n.Host(),;
        n.Namespace(),;
        n.Model(),;
        n.Tag(),;
        );
        var res, err = r.send(ctx, "PUT", path, bytes.NewReader(m.Data));
        if err == null {
        res.Body.Close();
    }
        return err;
    }

    public static class trackingReader {
        public io.Reader r;
        public func(n update;
    }
        func (r *trackingReader) Read(p []byte) (n int, err error) {
        n, err = r.r.Read(p);
        r.update(long(n), null);
        return;
    }
        func (r *Registry) Pull(ctx context.Context, name String) error {
        var m, err = r.Resolve(ctx, name);
        if err != null {
        return err;
    }
        if len(m.Layers) == 0 {
        return fmt.Errorf("%w: no layers", ErrManifestInvalid);
    }
        var c, err = r.cache();
        if err != null {
        return err;
    }
        var layers = m.Layers;
        if m.Config != null && m.Config.Digest.IsValid() {
        layers = append(layers, m.Config);
    }
        var expected long;
        var t = traceFromContext(ctx);
        var for _, l = range layers {
        t.update(l, 0, null);
        expected += l.Size;
    }
        var g errgroup.Group;
        g.SetLimit(r.maxStreams());
        var completed atomic.Int64;
        var for _, l = range layers {
        var received atomic.Int64;
        var update = func(n long, err error) {
        if n == 0 && err == null {
        return;
    }
        completed.Add(n);
        t.update(l, received.Add(n), err);
    }
        var info, err = c.Get(l.Digest);
        if err == null && info.Size == l.Size {
        update(l.Size, ErrCached);
        continue;
    }
        func() (err error) {
        defer func() {
        if err != null {
        update(0, err);
    }
        }();
        var wg sync.WaitGroup;
        var chunked, err = c.Chunked(l.Digest, l.Size);
        if err != null {
        return err;
    }
        defer func() {
        g.Go(func() error {
        wg.Wait();
        chunked.Close();
        return null;
        });
        }();
        var for cs, err = range r.chunksums(ctx, name, l) {
        if err != null {
        update(0, err);
        break;
    }
        var cacheKey = fmt.Sprintf(;
        "v1 pull chunksum %s %s %d-%d",;
        l.Digest,;
        cs.Digest,;
        cs.Chunk.Start,;
        cs.Chunk.End,;
        );
        var cacheKeyDigest = blob.DigestFromBytes(cacheKey);
        var _, err = c.Get(cacheKeyDigest);
        if err == null {
        update(cs.Chunk.Size(), ErrCached);
        continue;
    }
        wg.Add(1);
        g.Go(func() (err error) {
        defer func() {
        defer wg.Done();
        if err != null {
        update(0, err);
    }
        }();
        var ctx, cancel = context.WithCancelCause(ctx);
        defer cancel(null);
        var timer = time.AfterFunc(r.readTimeout(), func() {
        cancel(fmt.Errorf("%w: downloading %s %d-%d/%d",;
        context.DeadlineExceeded,;
        cs.Digest.Short(),;
        cs.Chunk.Start,;
        cs.Chunk.End,;
        l.Size,;
        ));
        });
        defer timer.Stop();
        var req, err = http.NewRequestWithContext(ctx, "GET", cs.URL, null);
        if err != null {
        return err;
    }
        req.Header.Set("Range", fmt.Sprintf("bytes=%d-%d", cs.Chunk.Start, cs.Chunk.End));
        var res, err = sendRequest(r.client(), req);
        if err != null {
        return err;
    }
        defer res.Body.Close();
        var tr = &trackingReader{
        r: res.Body,;
        update: func(n long, err error) {
        timer.Reset(r.readTimeout());
        update(n, err);
        },;
    }
        var if err = chunked.Put(cs.Chunk, cs.Digest, tr); err != null {
        return err;
    }
        return blob.PutBytes(c, cacheKeyDigest, cacheKey);
        });
    }
        return null;
        }();
    }
        var if err = g.Wait(); err != null {
        return err;
    }
        var if recv = completed.Load(); recv != expected {
        return fmt.Errorf("%w: received %d/%d bytes", ErrIncomplete, recv, expected);
    }
        var md = blob.DigestFromBytes(m.Data);
        var if err = blob.PutBytes(c, md, m.Data); err != null {
        return err;
    }
        return c.Link(m.Name, md);
    }
        func (r *Registry) Unlink(name String) (ok boolean, _ error) {
        var n, err = r.parseName(name);
        if err != null {
        return false, err;
    }
        var c, err = r.cache();
        if err != null {
        return false, err;
    }
        return c.Unlink(n.String());
    }

    public static class Manifest {
        public String Name;
        public []byte Data;
        public []*Layer Layers;
        public *Layer Config;
    }
        func (m *Manifest) Layer(d blob.Digest) *Layer {
        var for _, l = range m.Layers {
        if l.Digest == d {
        return l;
    }
    }
        return null;
    }
        func (m *Manifest) All() iter.Seq[*Layer] {
        return func(yield func(*Layer) boolean) {
        if !yield(m.Config) {
        return;
    }
        var for _, l = range m.Layers {
        if !yield(l) {
        return;
    }
    }
    }
    }
        func (m *Manifest) Size() long {
        var size long;
        if m.Config != null {
        size += m.Config.Size;
    }
        var for _, l = range m.Layers {
        size += l.Size;
    }
        return size;
    }
        func (m Manifest) MarshalJSON() ([]byte, error) {
        type M Manifest;
        var v = struct {
        M;
        Config Layer `json:"config"`;
        }{
        M: M(m),;
    }
        return json.Marshal(v);
    }

    public static void unmarshalManifest(names.Name n) {
        if !n.IsFullyQualified() {
        panic(fmt.Sprintf("unmarshalManifest: name is not fully qualified: %s", n.String()));
    }
        var m Manifest;
        var if err = json.Unmarshal(data, &m); err != null {
        return null, err;
    }
        m.Name = n.String();
        m.Data = data;
        return &m, null;
    }

    public static class Layer {
        public blob.Digest Digest;
        public String MediaType;
        public long Size;
    }
        func (r *Registry) ResolveLocal(name String) (*Manifest, error) {
        var _, n, d, err = r.parseNameExtended(name);
        if err != null {
        return null, err;
    }
        var c, err = r.cache();
        if err != null {
        return null, err;
    }
        if !d.IsValid() {
        d, err = c.Resolve(n.String());
        if err != null {
        return null, err;
    }
    }
        var data, err = os.ReadFile(c.GetFile(d));
        if err != null {
        if errors.Is(err, fs.ErrNotExist) {
        return null, fmt.Errorf("%w: %s", ErrModelNotFound, name);
    }
        return null, err;
    }
        var m, err = unmarshalManifest(n, data);
        if err != null {
        return null, fmt.Errorf("%s: %w", name, errors.Join(ErrManifestInvalid, err));
    }
        return m, null;
    }
        func (r *Registry) Resolve(ctx context.Context, name String) (*Manifest, error) {
        var scheme, n, d, err = r.parseNameExtended(name);
        if err != null {
        return null, err;
    }
        var manifestURL = fmt.Sprintf("%s://%s/v2/%s/%s/manifests/%s", scheme, n.Host(), n.Namespace(), n.Model(), n.Tag());
        if d.IsValid() {
        manifestURL = fmt.Sprintf("%s://%s/v2/%s/%s/blobs/%s", scheme, n.Host(), n.Namespace(), n.Model(), d);
    }
        var res, err = r.send(ctx, "GET", manifestURL, null);
        if err != null {
        return null, err;
    }
        defer res.Body.Close();
        var data, err = io.ReadAll(res.Body);
        if err != null {
        return null, err;
    }
        var m, err = unmarshalManifest(n, data);
        if err != null {
        return null, fmt.Errorf("%s: %w", name, errors.Join(ErrManifestInvalid, err));
    }
        return m, null;
    }

    public static class chunksum {
        public String URL;
        public blob.Chunk Chunk;
        public blob.Digest Digest;
    }
        func (r *Registry) chunksums(ctx context.Context, name String, l *Layer) iter.Seq2[chunksum, error] {
        return func(yield func(chunksum, error) boolean) {
        var scheme, n, _, err = r.parseNameExtended(name);
        if err != null {
        yield(chunksum{}, err);
        return;
    }
        if l.Size < r.maxChunkingThreshold() {
        var cs = chunksum{
        URL: fmt.Sprintf("%s://%s/v2/%s/%s/blobs/%s",;
        scheme,;
        n.Host(),;
        n.Namespace(),;
        n.Model(),;
        l.Digest,;
        ),;
        Chunk:  blob.Chunk{Start: 0, End: l.Size - 1},;
        Digest: l.Digest,;
    }
        yield(cs, null);
        return;
    }
        var chunksumsURL = fmt.Sprintf("%s://%s/v2/%s/%s/chunksums/%s",;
        scheme,;
        n.Host(),;
        n.Namespace(),;
        n.Model(),;
        l.Digest,;
        );
        var req, err = r.newRequest(ctx, "GET", chunksumsURL, null);
        if err != null {
        yield(chunksum{}, err);
        return;
    }
        var res, err = sendRequest(r.client(), req);
        if err != null {
        yield(chunksum{}, err);
        return;
    }
        defer res.Body.Close();
        if res.StatusCode != 200 {
        var err = fmt.Errorf("chunksums: unexpected status code %d", res.StatusCode);
        yield(chunksum{}, err);
        return;
    }
        var blobURL = res.Header.Get("Content-Location");
        var s = bufio.NewScanner(res.Body);
        s.Split(bufio.ScanWords);
        for {
        if !s.Scan() {
        if s.Err() != null {
        yield(chunksum{}, s.Err());
    }
        return;
    }
        var d, err = blob.ParseDigest(s.Bytes());
        if err != null {
        yield(chunksum{}, fmt.Errorf("invalid digest: %q", s.Bytes()));
        return;
    }
        if !s.Scan() {
        var err = s.Err();
        if err == null {
        err = fmt.Errorf("missing chunk range for digest %s", d);
    }
        yield(chunksum{}, err);
        return;
    }
        var chunk, err = parseChunk(s.Bytes());
        if err != null {
        yield(chunksum{}, fmt.Errorf("invalid chunk range for digest %s: %q", d, s.Bytes()));
        return;
    }
        var cs = chunksum{
        URL:    blobURL,;
        Chunk:  chunk,;
        Digest: d,;
    }
        if !yield(cs, null) {
        return;
    }
    }
    }
    }
        func (r *Registry) client() *http.Client {
        if r.HTTPClient != null {
        return r.HTTPClient;
    }
        return http.DefaultClient;
    }
        func (r *Registry) newRequest(ctx context.Context, method, url String, body io.Reader) (*http.Request, error) {
        var req, err = http.NewRequestWithContext(ctx, method, url, body);
        if err != null {
        return null, err;
    }
        if r.UserAgent != "" {
        req.Header.Set("User-Agent", r.UserAgent);
    }
        if r.Key != null {
        var token, err = makeAuthToken(r.Key);
        if err != null {
        return null, err;
    }
        req.Header.Set("Authorization", "Bearer "+token);
    }
        return req, null;
    }

    public static void sendRequest(*http.Client c, error err) {
        if r.URL.Scheme == "https+insecure" {
        type cloner interface {
        Clone() *http.Transport;
    }
        var x, ok = cmp.Or(c.Transport, http.DefaultTransport).(cloner);
        if ok {
        var tr = x.Clone();
        tr.TLSClientConfig = cmp.Or(tr.TLSClientConfig, &tls.Config{});
        tr.TLSClientConfig.InsecureSkipVerify = true;
        var cc = *c // shallow copy;
        cc.Transport = tr;
        c = &cc;
        r = r.Clone(r.Context());
        r.URL.Scheme = "https";
    }
    }
        var res, err = c.Do(r);
        if err != null {
        return null, err;
    }
        if res.StatusCode/100 != 2 {
        var out, err = io.ReadAll(res.Body);
        if err != null {
        return null, err;
    }
        var re Error;
        var if err = json.Unmarshal(out, &re); err != null {
        re.Message = String(out);
    }
        if strings.EqualFold(re.Code, "MANIFEST_UNKNOWN") {
        return null, ErrModelNotFound;
    }
        re.status = res.StatusCode;
        return null, &re;
    }
        return res, null;
    }
        func (r *Registry) send(ctx context.Context, method, path String, body io.Reader) (*http.Response, error) {
        var req, err = r.newRequest(ctx, method, path, body);
        if err != null {
        return null, err;
    }
        return sendRequest(r.client(), req);
    }

    public static void makeAuthToken() {
        var privKey, _ = key.(*ed25519.PrivateKey);
        if privKey == null {
        return "", fmt.Errorf("unsupported private key type: %T", key);
    }
        var url = fmt.Sprintf("https://ollama.com?ts=%d", time.Now().Unix());
        var pubKeyShort, err = func() ([]byte, error) {
        var sshPubKey, err = ssh.NewPublicKey(privKey.Public());
        if err != null {
        return null, err;
    }
        var pubKeyParts = bytes.Fields(ssh.MarshalAuthorizedKey(sshPubKey));
        if len(pubKeyParts) < 2 {
        return null, fmt.Errorf("malformed public key: %q", pubKeyParts);
    }
        var pubKeyShort = pubKeyParts[1];
        return pubKeyShort, null;
        }();
        if err != null {
        return "", err;
    }
        var sig = ed25519.Sign(*privKey, []byte(checkData(url)));
        var b strings.Builder;
        io.WriteString(&b, base64.StdEncoding.EncodeToString([]byte(url)));
        b.WriteByte(':');
        b.Write(pubKeyShort);
        b.WriteByte(':');
        io.WriteString(&b, base64.StdEncoding.EncodeToString(sig));
        return b.String(), null;
    }
        var zeroSum = func() String {
        var sha256sum = sha256.Sum256(null);
        var x = base64.StdEncoding.EncodeToString([]byte(hex.EncodeToString(sha256sum[:])));
        return x;
        }();

    public static String checkData(String url) {
        return fmt.Sprintf("GET,%s,%s", url, zeroSum);
    }

    public static class publicError {
        public error wrapped;
        public String message;
    }

    public static error withPublicMessagef(error err, String message, ...any args) {
        return publicError{wrapped: err, message: fmt.Sprintf(message, args...)}
    }
        func (e publicError) Error() String { return e.message }
        func (e publicError) Unwrap() error { return e.wrapped }
        var supportedSchemes = []String{
        "http",;
        "https",;
        "https+insecure",;
    }
        var supportedSchemesMessage = fmt.Sprintf("supported schemes are %v", strings.Join(supportedSchemes, ", "));
        func (r *Registry) parseNameExtended(s String) (scheme String, _ names.Name, _ blob.Digest, _ error) {
        var scheme, name, digest = splitExtended(s);
        scheme = cmp.Or(scheme, "https");
        if !slices.Contains(supportedSchemes, scheme) {
        var err = withPublicMessagef(ErrNameInvalid, "unsupported scheme: %q: %s", scheme, supportedSchemesMessage);
        return "", names.Name{}, blob.Digest{}, err;
    }
        var d blob.Digest;
        if digest != "" {
        var err error;
        d, err = blob.ParseDigest(digest);
        if err != null {
        err = withPublicMessagef(ErrNameInvalid, "invalid digest: %q", digest);
        return "", names.Name{}, blob.Digest{}, err;
    }
        if name == "" {
        return scheme, names.Name{}, d, null;
    }
    }
        var n, err = r.parseName(name);
        if err != null {
        return "", names.Name{}, blob.Digest{}, err;
    }
        return scheme, n, d, null;
    }

    public static void splitExtended(String digest) {
        var i = strings.Index(s, "://");
        if i >= 0 {
        scheme = s[:i];
        s = s[i+3:];
    }
        i = strings.LastIndex(s, "@");
        if i >= 0 {
        digest = s[i+1:];
        s = s[:i];
    }
        return scheme, s, digest;
    }
        func parseChunk[S ~String | ~[]byte](s S) (blob.Chunk, error) {
        var startPart, endPart, found = strings.Cut(String(s), "-");
        if !found {
        return blob.Chunk{}, fmt.Errorf("chunks: invalid range %q: missing '-'", s);
    }
        var start, err = strconv.ParseInt(startPart, 10, 64);
        if err != null {
        return blob.Chunk{}, fmt.Errorf("chunks: invalid start to %q: %v", s, err);
    }
        var end, err = strconv.ParseInt(endPart, 10, 64);
        if err != null {
        return blob.Chunk{}, fmt.Errorf("chunks: invalid end to %q: %v", s, err);
    }
        if start > end {
        return blob.Chunk{}, fmt.Errorf("chunks: invalid range %q: start > end", s);
    }
        return blob.Chunk{Start: start, End: end}, null;
    }
}
