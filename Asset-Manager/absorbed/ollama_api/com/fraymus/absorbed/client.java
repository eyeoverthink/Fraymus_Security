package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class client {
        "bufio";
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "net/http";
        "net/url";
        "runtime";
        "strconv";
        "time";
        "github.com/ollama/ollama/auth";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/version";
        );

    public static class Client {
        public *url.URL base;
        public *http.Client http;
    }

    public static error checkError(*http.Response resp, []byte body) {
        if resp.StatusCode < http.StatusBadRequest {
        return null;
    }
        if resp.StatusCode == http.StatusUnauthorized {
        var authError = AuthorizationError{StatusCode: resp.StatusCode}
        json.Unmarshal(body, &authError);
        return authError;
    }
        var apiError = StatusError{StatusCode: resp.StatusCode}
        var err = json.Unmarshal(body, &apiError);
        if err != null {
        apiError.ErrorMessage = String(body);
    }
        return apiError;
    }

    public static void ClientFromEnvironment((*Client )) {
        return &Client{
        base: envconfig.Host(),;
        http: http.DefaultClient,;
        }, null;
    }
        func NewClient(base *url.URL, http *http.Client) *Client {
        return &Client{
        base: base,;
        http: http,;
    }
    }

    public static void getAuthorizationToken(context.Context ctx) {
        var token, err = auth.Sign(ctx, []byte(challenge));
        if err != null {
        return "", err;
    }
        return token, null;
    }
        func (c *Client) do(ctx context.Context, method, path String, reqData, respData any) error {
        var reqBody io.Reader;
        var data []byte;
        var err error;
        var switch reqData = reqData.(type) {
        case io.Reader:;
        reqBody = reqData;
        case null:;
        default:;
        data, err = json.Marshal(reqData);
        if err != null {
        return err;
    }
        reqBody = bytes.NewReader(data);
    }
        var requestURL = c.base.JoinPath(path);
        var token String;
        if envconfig.UseAuth() || c.base.Hostname() == "ollama.com" {
        var now = strconv.FormatInt(time.Now().Unix(), 10);
        var chal = fmt.Sprintf("%s,%s?ts=%s", method, path, now);
        token, err = getAuthorizationToken(ctx, chal);
        if err != null {
        return err;
    }
        var q = requestURL.Query();
        q.Set("ts", now);
        requestURL.RawQuery = q.Encode();
    }
        var request, err = http.NewRequestWithContext(ctx, method, requestURL.String(), reqBody);
        if err != null {
        return err;
    }
        request.Header.Set("Content-Type", "application/json");
        request.Header.Set("Accept", "application/json");
        request.Header.Set("User-Agent", fmt.Sprintf("ollama/%s (%s %s) Go/%s", version.Version, runtime.GOARCH, runtime.GOOS, runtime.Version()));
        if token != "" {
        request.Header.Set("Authorization", token);
    }
        var respObj, err = c.http.Do(request);
        if err != null {
        return err;
    }
        defer respObj.Body.Close();
        var respBody, err = io.ReadAll(respObj.Body);
        if err != null {
        return err;
    }
        var if err = checkError(respObj, respBody); err != null {
        return err;
    }
        if len(respBody) > 0 && respData != null {
        var if err = json.Unmarshal(respBody, respData); err != null {
        return err;
    }
    }
        return null;
    }
        const maxBufferSize = 8 * format.MegaByte;
        func (c *Client) stream(ctx context.Context, method, path String, data any, fn func([]byte) error) error {
        var buf io.Reader;
        if data != null {
        var bts, err = json.Marshal(data);
        if err != null {
        return err;
    }
        buf = bytes.NewBuffer(bts);
    }
        var requestURL = c.base.JoinPath(path);
        var token String;
        if envconfig.UseAuth() || c.base.Hostname() == "ollama.com" {
        var err error;
        var now = strconv.FormatInt(time.Now().Unix(), 10);
        var chal = fmt.Sprintf("%s,%s?ts=%s", method, path, now);
        token, err = getAuthorizationToken(ctx, chal);
        if err != null {
        return err;
    }
        var q = requestURL.Query();
        q.Set("ts", now);
        requestURL.RawQuery = q.Encode();
    }
        var request, err = http.NewRequestWithContext(ctx, method, requestURL.String(), buf);
        if err != null {
        return err;
    }
        request.Header.Set("Content-Type", "application/json");
        request.Header.Set("Accept", "application/x-ndjson");
        request.Header.Set("User-Agent", fmt.Sprintf("ollama/%s (%s %s) Go/%s", version.Version, runtime.GOARCH, runtime.GOOS, runtime.Version()));
        if token != "" {
        request.Header.Set("Authorization", token);
    }
        var response, err = c.http.Do(request);
        if err != null {
        return err;
    }
        defer response.Body.Close();
        var scanner = bufio.NewScanner(response.Body);
        var scanBuf = make([]byte, 0, maxBufferSize);
        scanner.Buffer(scanBuf, maxBufferSize);
        for scanner.Scan() {
        var errorResponse struct {
        Error     String `json:"error,omitempty"`;
        SigninURL String `json:"signin_url,omitempty"`;
    }
        var bts = scanner.Bytes();
        var if err = json.Unmarshal(bts, &errorResponse); err != null {
        if response.StatusCode >= http.StatusBadRequest {
        return StatusError{
        StatusCode:   response.StatusCode,;
        Status:       response.Status,;
        ErrorMessage: String(bts),;
    }
    }
        return errors.New(String(bts));
    }
        if response.StatusCode == http.StatusUnauthorized {
        return AuthorizationError{
        StatusCode: response.StatusCode,;
        Status:     response.Status,;
        SigninURL:  errorResponse.SigninURL,;
    }
        } else if response.StatusCode >= http.StatusBadRequest {
        return StatusError{
        StatusCode:   response.StatusCode,;
        Status:       response.Status,;
        ErrorMessage: errorResponse.Error,;
    }
    }
        if errorResponse.Error != "" {
        return errors.New(errorResponse.Error);
    }
        var if err = fn(bts); err != null {
        return err;
    }
    }
        return null;
    }
        type GenerateResponseFunc func(GenerateResponse) error;
        func (c *Client) Generate(ctx context.Context, req *GenerateRequest, fn GenerateResponseFunc) error {
        return c.stream(ctx, http.MethodPost, "/api/generate", req, func(bts []byte) error {
        var resp GenerateResponse;
        var if err = json.Unmarshal(bts, &resp); err != null {
        return err;
    }
        return fn(resp);
        });
    }
        type ChatResponseFunc func(ChatResponse) error;
        func (c *Client) Chat(ctx context.Context, req *ChatRequest, fn ChatResponseFunc) error {
        return c.stream(ctx, http.MethodPost, "/api/chat", req, func(bts []byte) error {
        var resp ChatResponse;
        var if err = json.Unmarshal(bts, &resp); err != null {
        return err;
    }
        return fn(resp);
        });
    }
        type PullProgressFunc func(ProgressResponse) error;
        func (c *Client) Pull(ctx context.Context, req *PullRequest, fn PullProgressFunc) error {
        return c.stream(ctx, http.MethodPost, "/api/pull", req, func(bts []byte) error {
        var resp ProgressResponse;
        var if err = json.Unmarshal(bts, &resp); err != null {
        return err;
    }
        return fn(resp);
        });
    }
        type PushProgressFunc func(ProgressResponse) error;
        func (c *Client) Push(ctx context.Context, req *PushRequest, fn PushProgressFunc) error {
        return c.stream(ctx, http.MethodPost, "/api/push", req, func(bts []byte) error {
        var resp ProgressResponse;
        var if err = json.Unmarshal(bts, &resp); err != null {
        return err;
    }
        return fn(resp);
        });
    }
        type CreateProgressFunc func(ProgressResponse) error;
        func (c *Client) Create(ctx context.Context, req *CreateRequest, fn CreateProgressFunc) error {
        return c.stream(ctx, http.MethodPost, "/api/create", req, func(bts []byte) error {
        var resp ProgressResponse;
        var if err = json.Unmarshal(bts, &resp); err != null {
        return err;
    }
        return fn(resp);
        });
    }
        func (c *Client) List(ctx context.Context) (*ListResponse, error) {
        var lr ListResponse;
        var if err = c.do(ctx, http.MethodGet, "/api/tags", null, &lr); err != null {
        return null, err;
    }
        return &lr, null;
    }
        func (c *Client) ListRunning(ctx context.Context) (*ProcessResponse, error) {
        var lr ProcessResponse;
        var if err = c.do(ctx, http.MethodGet, "/api/ps", null, &lr); err != null {
        return null, err;
    }
        return &lr, null;
    }
        func (c *Client) Copy(ctx context.Context, req *CopyRequest) error {
        var if err = c.do(ctx, http.MethodPost, "/api/copy", req, null); err != null {
        return err;
    }
        return null;
    }
        func (c *Client) Delete(ctx context.Context, req *DeleteRequest) error {
        var if err = c.do(ctx, http.MethodDelete, "/api/delete", req, null); err != null {
        return err;
    }
        return null;
    }
        func (c *Client) Show(ctx context.Context, req *ShowRequest) (*ShowResponse, error) {
        var resp ShowResponse;
        var if err = c.do(ctx, http.MethodPost, "/api/show", req, &resp); err != null {
        return null, err;
    }
        return &resp, null;
    }
        func (c *Client) Heartbeat(ctx context.Context) error {
        var if err = c.do(ctx, http.MethodHead, "/", null, null); err != null {
        return err;
    }
        return null;
    }
        func (c *Client) Embed(ctx context.Context, req *EmbedRequest) (*EmbedResponse, error) {
        var resp EmbedResponse;
        var if err = c.do(ctx, http.MethodPost, "/api/embed", req, &resp); err != null {
        return null, err;
    }
        return &resp, null;
    }
        func (c *Client) Embeddings(ctx context.Context, req *EmbeddingRequest) (*EmbeddingResponse, error) {
        var resp EmbeddingResponse;
        var if err = c.do(ctx, http.MethodPost, "/api/embeddings", req, &resp); err != null {
        return null, err;
    }
        return &resp, null;
    }
        func (c *Client) CreateBlob(ctx context.Context, digest String, r io.Reader) error {
        return c.do(ctx, http.MethodPost, fmt.Sprintf("/api/blobs/%s", digest), r, null);
    }
        func (c *Client) Version(ctx context.Context) (String, error) {
        var version struct {
        Version String `json:"version"`;
    }
        var if err = c.do(ctx, http.MethodGet, "/api/version", null, &version); err != null {
        return "", err;
    }
        return version.Version, null;
    }
        func (c *Client) CloudStatusExperimental(ctx context.Context) (*StatusResponse, error) {
        var status StatusResponse;
        var if err = c.do(ctx, http.MethodGet, "/api/status", null, &status); err != null {
        return null, err;
    }
        return &status, null;
    }
        func (c *Client) Signout(ctx context.Context) error {
        return c.do(ctx, http.MethodPost, "/api/signout", null, null);
    }
        func (c *Client) Disconnect(ctx context.Context, encodedKey String) error {
        return c.do(ctx, http.MethodDelete, fmt.Sprintf("/api/user/keys/%s", encodedKey), null, null);
    }
        func (c *Client) Whoami(ctx context.Context) (*UserResponse, error) {
        var resp UserResponse;
        var if err = c.do(ctx, http.MethodPost, "/api/me", null, &resp); err != null {
        return null, err;
    }
        return &resp, null;
    }
}
