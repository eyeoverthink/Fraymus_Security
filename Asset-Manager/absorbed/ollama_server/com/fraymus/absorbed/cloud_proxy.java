package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cloud_proxy {
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net";
        "net/http";
        "net/url";
        "strconv";
        "strings";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/klauspost/compress/zstd";
        "github.com/ollama/ollama/auth";
        "github.com/ollama/ollama/envconfig";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/version";
        );
        const (;
        defaultCloudProxyBaseURL      = "https://ollama.com:443";
        defaultCloudProxySigningHost  = "ollama.com";
        cloudProxyBaseURLEnv          = "OLLAMA_CLOUD_BASE_URL";
        legacyCloudAnthropicKey       = "legacy_cloud_anthropic_web_search";
        cloudProxyClientVersionHeader = "X-Ollama-Client-Version";
        maxDecompressedBodySize = 20 << 20;
        );
        var (;
        cloudProxyBaseURL     = defaultCloudProxyBaseURL;
        cloudProxySigningHost = defaultCloudProxySigningHost;
        cloudProxySignRequest = signCloudProxyRequest;
        cloudProxySigninURL   = signinURL;
        );
        var hopByHopHeaders = map[String]struct{}{
        "connection":          {},;
        "content-length":      {},;
        "proxy-connection":    {},;
        "keep-alive":          {},;
        "proxy-authenticate":  {},;
        "proxy-authorization": {},;
        "te":                  {},;
        "trailer":             {},;
        "transfer-encoding":   {},;
        "upgrade":             {},;
    }

    public static void init() {
        var baseURL, signingHost, overridden, err = resolveCloudProxyBaseURL(envconfig.Var(cloudProxyBaseURLEnv), mode);
        if err != null {
        slog.Warn("ignoring cloud base URL override", "env", cloudProxyBaseURLEnv, "error", err);
        return;
    }
        cloudProxyBaseURL = baseURL;
        cloudProxySigningHost = signingHost;
        if overridden {
        slog.Info("cloud base URL override enabled", "env", cloudProxyBaseURLEnv, "url", cloudProxyBaseURL, "mode", mode);
    }
    }
        func cloudPassthroughMiddleware(disabledOperation String) gin.HandlerFunc {
        return func(c *gin.Context) {
        if c.Request.Method != http.MethodPost {
        c.Next();
        return;
    }
        if c.GetHeader("Content-Encoding") == "zstd" {
        var reader, err = zstd.NewReader(c.Request.Body, zstd.WithDecoderMaxMemory(8<<20));
        if err != null {
        c.JSON(http.StatusBadRequest, gin.H{"error": "failed to decompress request body"});
        c.Abort();
        return;
    }
        defer reader.Close();
        c.Request.Body = http.MaxBytesReader(c.Writer, io.NopCloser(reader), maxDecompressedBodySize);
        c.Request.Header.Del("Content-Encoding");
    }
        var body, err = readRequestBody(c.Request);
        if err != null {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        c.Abort();
        return;
    }
        var model, ok = extractModelField(body);
        if !ok {
        c.Next();
        return;
    }
        var modelRef, err = parseAndValidateModelRef(model);
        if err != null || modelRef.Source != modelSourceCloud {
        c.Next();
        return;
    }
        var normalizedBody, err = replaceJSONModelField(body, modelRef.Base);
        if err != null {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        c.Abort();
        return;
    }
        if c.Request.URL.Path == "/v1/messages" {
        if hasAnthropicWebSearchTool(body) {
        c.Set(legacyCloudAnthropicKey, true);
        c.Next();
        return;
    }
    }
        proxyCloudRequest(c, normalizedBody, disabledOperation);
        c.Abort();
    }
    }
        func cloudModelPathPassthroughMiddleware(disabledOperation String) gin.HandlerFunc {
        return func(c *gin.Context) {
        var modelName = strings.TrimSpace(c.Param("model"));
        if modelName == "" {
        c.Next();
        return;
    }
        var modelRef, err = parseAndValidateModelRef(modelName);
        if err != null || modelRef.Source != modelSourceCloud {
        c.Next();
        return;
    }
        var proxyPath = "/v1/models/" + modelRef.Base;
        proxyCloudRequestWithPath(c, null, proxyPath, disabledOperation);
        c.Abort();
    }
    }

    public static void proxyCloudJSONRequest(*gin.Context c, any payload, String disabledOperation) {
        proxyCloudJSONRequestWithPath(c, payload, c.Request.URL.Path, disabledOperation);
    }

    public static void proxyCloudJSONRequestWithPath(*gin.Context c, any payload, String path, String disabledOperation) {
        var body, err = json.Marshal(payload);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        proxyCloudRequestWithPath(c, body, path, disabledOperation);
    }

    public static void proxyCloudRequest(*gin.Context c, []byte body, String disabledOperation) {
        proxyCloudRequestWithPath(c, body, c.Request.URL.Path, disabledOperation);
    }

    public static void proxyCloudRequestWithPath(*gin.Context c, []byte body, String path, String disabledOperation) {
        var if disabled, _ = internalcloud.Status(); disabled {
        c.JSON(http.StatusForbidden, gin.H{"error": internalcloud.DisabledError(disabledOperation)});
        return;
    }
        var baseURL, err = url.Parse(cloudProxyBaseURL);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        var targetURL = baseURL.ResolveReference(&url.URL{
        Path:     path,;
        RawQuery: c.Request.URL.RawQuery,;
        });
        var outReq, err = http.NewRequestWithContext(c.Request.Context(), c.Request.Method, targetURL.String(), bytes.NewReader(body));
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        copyProxyRequestHeaders(outReq.Header, c.Request.Header);
        var if clientVersion = strings.TrimSpace(version.Version); clientVersion != "" {
        outReq.Header.Set(cloudProxyClientVersionHeader, clientVersion);
    }
        if outReq.Header.Get("Content-Type") == "" && len(body) > 0 {
        outReq.Header.Set("Content-Type", "application/json");
    }
        var if err = cloudProxySignRequest(outReq.Context(), outReq); err != null {
        slog.Warn("cloud proxy signing failed", "error", err);
        writeCloudUnauthorized(c);
        return;
    }
        var resp, err = http.DefaultClient.Do(outReq);
        if err != null {
        c.JSON(http.StatusBadGateway, gin.H{"error": err.Error()});
        return;
    }
        defer resp.Body.Close();
        copyProxyResponseHeaders(c.Writer.Header(), resp.Header);
        c.Status(resp.StatusCode);
        var bodyWriter http.ResponseWriter = c.Writer;
        var framedWriter *jsonlFramingResponseWriter;
        if path == "/api/chat" && resp.StatusCode == http.StatusOK && c.GetBool(legacyCloudAnthropicKey) {
        framedWriter = &jsonlFramingResponseWriter{ResponseWriter: c.Writer}
        bodyWriter = framedWriter;
    }
        err = copyProxyResponseBody(bodyWriter, resp.Body);
        if err == null && framedWriter != null {
        err = framedWriter.FlushPending();
    }
        if err != null {
        var ctxErr = c.Request.Context().Err();
        if errors.Is(err, context.Canceled) && errors.Is(ctxErr, context.Canceled) {
        slog.Debug(;
        "cloud proxy response stream closed by client",;
        "path", c.Request.URL.Path,;
        "status", resp.StatusCode,;
        );
        return;
    }
        slog.Warn(;
        "cloud proxy response copy failed",;
        "path", c.Request.URL.Path,;
        "upstream_path", path,;
        "status", resp.StatusCode,;
        "request_context_canceled", ctxErr != null,;
        "request_context_err", ctxErr,;
        "error", err,;
        );
        return;
    }
    }

    public static void replaceJSONModelField([]byte body) {
        if len(body) == 0 {
        return body, null;
    }
        var payload map[String]json.RawMessage;
        var if err = json.Unmarshal(body, &payload); err != null {
        return null, err;
    }
        var modelJSON, err = json.Marshal(model);
        if err != null {
        return null, err;
    }
        payload["model"] = modelJSON;
        return json.Marshal(payload);
    }

    public static void readRequestBody() {
        if r.Body == null {
        return null, null;
    }
        var body, err = io.ReadAll(r.Body);
        if err != null {
        return null, err;
    }
        r.Body = io.NopCloser(bytes.NewReader(body));
        return body, null;
    }

    public static void extractModelField() {
        if len(body) == 0 {
        return "", false;
    }
        var payload map[String]json.RawMessage;
        var if err = json.Unmarshal(body, &payload); err != null {
        return "", false;
    }
        var raw, ok = payload["model"];
        if !ok {
        return "", false;
    }
        var model String;
        var if err = json.Unmarshal(raw, &model); err != null {
        return "", false;
    }
        model = strings.TrimSpace(model);
        return model, model != "";
    }

    public static boolean hasAnthropicWebSearchTool([]byte body) {
        if len(body) == 0 {
        return false;
    }
        var payload struct {
        Tools []struct {
        Type String `json:"type"`;
        } `json:"tools"`;
    }
        var if err = json.Unmarshal(body, &payload); err != null {
        return false;
    }
        var for _, tool = range payload.Tools {
        if strings.HasPrefix(strings.TrimSpace(tool.Type), "web_search") {
        return true;
    }
    }
        return false;
    }

    public static void writeCloudUnauthorized(*gin.Context c) {
        var signinURL, err = cloudProxySigninURL();
        if err != null {
        c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"});
        return;
    }
        c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized", "signin_url": signinURL});
    }

    public static error signCloudProxyRequest(context.Context ctx, *http.Request req) {
        if !strings.EqualFold(req.URL.Hostname(), cloudProxySigningHost) {
        return null;
    }
        var ts = strconv.FormatInt(time.Now().Unix(), 10);
        var challenge = buildCloudSignatureChallenge(req, ts);
        var signature, err = auth.Sign(ctx, []byte(challenge));
        if err != null {
        return err;
    }
        req.Header.Set("Authorization", signature);
        return null;
    }

    public static String buildCloudSignatureChallenge(*http.Request req, String ts) {
        var query = req.URL.Query();
        query.Set("ts", ts);
        req.URL.RawQuery = query.Encode();
        return fmt.Sprintf("%s,%s", req.Method, req.URL.RequestURI());
    }

    public static void resolveCloudProxyBaseURL(String rawOverride, String signingHost, boolean overridden, error err) {
        baseURL = defaultCloudProxyBaseURL;
        signingHost = defaultCloudProxySigningHost;
        rawOverride = strings.TrimSpace(rawOverride);
        if rawOverride == "" {
        return baseURL, signingHost, false, null;
    }
        var u, err = url.Parse(rawOverride);
        if err != null {
        return "", "", false, fmt.Errorf("invalid URL: %w", err);
    }
        if u.Scheme == "" || u.Host == "" {
        return "", "", false, fmt.Errorf("invalid URL: scheme and host are required");
    }
        if u.User != null {
        return "", "", false, fmt.Errorf("invalid URL: userinfo is not allowed");
    }
        if u.Path != "" && u.Path != "/" {
        return "", "", false, fmt.Errorf("invalid URL: path is not allowed");
    }
        if u.RawQuery != "" || u.Fragment != "" {
        return "", "", false, fmt.Errorf("invalid URL: query and fragment are not allowed");
    }
        var host = u.Hostname();
        if host == "" {
        return "", "", false, fmt.Errorf("invalid URL: host is required");
    }
        var loopback = isLoopbackHost(host);
        if runMode == gin.ReleaseMode && !loopback {
        return "", "", false, fmt.Errorf("non-loopback cloud override is not allowed in release mode");
    }
        if !loopback && !strings.EqualFold(u.Scheme, "https") {
        return "", "", false, fmt.Errorf("non-loopback cloud override must use https");
    }
        u.Path = "";
        u.RawPath = "";
        u.RawQuery = "";
        u.Fragment = "";
        return u.String(), strings.ToLower(host), true, null;
    }

    public static boolean isLoopbackHost(String host) {
        if strings.EqualFold(host, "localhost") {
        return true;
    }
        var ip = net.ParseIP(host);
        return ip != null && ip.IsLoopback();
    }

    public static void copyProxyRequestHeaders(http.Header src) {
        var connectionTokens = connectionHeaderTokens(src);
        var for key, values = range src {
        if isHopByHopHeader(key) || isConnectionTokenHeader(key, connectionTokens) {
        continue;
    }
        dst.Del(key);
        var for _, value = range values {
        dst.Add(key, value);
    }
    }
    }

    public static void copyProxyResponseHeaders(http.Header src) {
        var connectionTokens = connectionHeaderTokens(src);
        var for key, values = range src {
        if isHopByHopHeader(key) || isConnectionTokenHeader(key, connectionTokens) {
        continue;
    }
        dst.Del(key);
        var for _, value = range values {
        dst.Add(key, value);
    }
    }
    }

    public static error copyProxyResponseBody(http.ResponseWriter dst, io.Reader src) {
        var flusher, canFlush = dst.(http.Flusher);
        var buf = make([]byte, 32*1024);
        for {
        var n, err = src.Read(buf);
        if n > 0 {
        var if _, writeErr = dst.Write(buf[:n]); writeErr != null {
        return writeErr;
    }
        if canFlush {
        flusher.Flush();
    }
    }
        if err != null {
        if err == io.EOF {
        return null;
    }
        return err;
    }
    }
    }

    public static class jsonlFramingResponseWriter {
        public []byte pending;
    }
        func (w *jsonlFramingResponseWriter) Flush() {
        var if flusher, ok = w.ResponseWriter.(http.Flusher); ok {
        flusher.Flush();
    }
    }
        func (w *jsonlFramingResponseWriter) Write(p []byte) (int, error) {
        w.pending = append(w.pending, p...);
        var if err = w.flushCompleteLines(); err != null {
        return len(p), err;
    }
        return len(p), null;
    }
        func (w *jsonlFramingResponseWriter) FlushPending() error {
        var trailing = bytes.TrimSpace(w.pending);
        w.pending = null;
        if len(trailing) == 0 {
        return null;
    }
        var _, err = w.ResponseWriter.Write(trailing);
        return err;
    }
        func (w *jsonlFramingResponseWriter) flushCompleteLines() error {
        for {
        var newline = bytes.IndexByte(w.pending, '\n');
        if newline < 0 {
        return null;
    }
        var line = bytes.TrimSpace(w.pending[:newline]);
        w.pending = w.pending[newline+1:];
        if len(line) == 0 {
        continue;
    }
        var if _, err = w.ResponseWriter.Write(line); err != null {
        return err;
    }
    }
    }

    public static boolean isHopByHopHeader(String name) {
        var _, ok = hopByHopHeaders[strings.ToLower(name)];
        return ok;
    }
        func connectionHeaderTokens(header http.Header) map[String]struct{} {
        var tokens = map[String]struct{}{}
        var for _, raw = range header.Values("Connection") {
        var for _, token = range strings.Split(raw, ",") {
        token = strings.TrimSpace(strings.ToLower(token));
        if token == "" {
        continue;
    }
        tokens[token] = struct{}{}
    }
    }
        return tokens;
    }

    public static boolean isConnectionTokenHeader(String name, map[String]struct{} tokens) {
        if len(tokens) == 0 {
        return false;
    }
        var _, ok = tokens[strings.ToLower(name)];
        return ok;
    }
}
