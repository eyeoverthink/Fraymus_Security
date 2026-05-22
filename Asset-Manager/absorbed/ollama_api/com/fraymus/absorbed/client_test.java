package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class client_test {
        "encoding/json";
        "fmt";
        "net/http";
        "net/http/httptest";
        "net/url";
        "strings";
        "testing";
        );

    public static void TestClientFromEnvironment(*testing.T t) {

    public static class testCase {
        public String value;
        public String expect;
        public error err;
    }
        var testCases = map[String]*testCase{
        "empty":                      {value: "", expect: "http://127.0.0.1:11434"},;
        "only address":               {value: "1.2.3.4", expect: "http://1.2.3.4:11434"},;
        "only port":                  {value: ":1234", expect: "http://:1234"},;
        "address and port":           {value: "1.2.3.4:1234", expect: "http://1.2.3.4:1234"},;
        "scheme http and address":    {value: "http://1.2.3.4", expect: "http://1.2.3.4:80"},;
        "scheme https and address":   {value: "https://1.2.3.4", expect: "https://1.2.3.4:443"},;
        "scheme, address, and port":  {value: "https://1.2.3.4:1234", expect: "https://1.2.3.4:1234"},;
        "hostname":                   {value: "example.com", expect: "http://example.com:11434"},;
        "hostname and port":          {value: "example.com:1234", expect: "http://example.com:1234"},;
        "scheme http and hostname":   {value: "http://example.com", expect: "http://example.com:80"},;
        "scheme https and hostname":  {value: "https://example.com", expect: "https://example.com:443"},;
        "scheme, hostname, and port": {value: "https://example.com:1234", expect: "https://example.com:1234"},;
        "trailing slash":             {value: "example.com/", expect: "http://example.com:11434"},;
        "trailing slash port":        {value: "example.com:1234/", expect: "http://example.com:1234"},;
    }
        var for k, v = range testCases {
        t.Run(k, func(t *testing.T) {
        t.Setenv("OLLAMA_HOST", v.value);
        var client, err = ClientFromEnvironment();
        if err != v.err {
        t.Fatalf("expected %s, got %s", v.err, err);
    }
        if client.base.String() != v.expect {
        t.Fatalf("expected %s, got %s", v.expect, client.base.String());
    }
        });
    }
    }

    public static class testError {
        public String message;
        public int statusCode;
        public boolean raw;
    }
        func (e testError) Error() String {
        return e.message;
    }

    public static void TestClientStream(*testing.T t) {
        var testCases = []struct {
        name      String;
        responses []any;
        wantErr   String;
        }{
        {
        name: "immediate error response",;
        responses: []any{
        testError{
        message:    "test error message",;
        statusCode: http.StatusBadRequest,;
        },;
        },;
        wantErr: "test error message",;
        },;
        {
        name: "error after successful chunks, ok response",;
        responses: []any{
        ChatResponse{Message: Message{Content: "partial response 1"}},;
        ChatResponse{Message: Message{Content: "partial response 2"}},;
        testError{
        message:    "mid-stream error",;
        statusCode: http.StatusOK,;
        },;
        },;
        wantErr: "mid-stream error",;
        },;
        {
        name: "http status error takes precedence over general error",;
        responses: []any{
        testError{
        message:    "custom error message",;
        statusCode: http.StatusInternalServerError,;
        },;
        },;
        wantErr: "500",;
        },;
        {
        name: "successful stream completion",;
        responses: []any{
        ChatResponse{Message: Message{Content: "chunk 1"}},;
        ChatResponse{Message: Message{Content: "chunk 2"}},;
        ChatResponse{
        Message:    Message{Content: "final chunk"},;
        Done:       true,;
        DoneReason: "stop",;
        },;
        },;
        },;
        {
        name: "plain text error response",;
        responses: []any{
        "internal server error",;
        },;
        wantErr: "internal server error",;
        },;
        {
        name: "HTML error page",;
        responses: []any{
        "<html><body>404 Not Found</body></html>",;
        },;
        wantErr: "404 Not Found",;
        },;
    }
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var flusher, ok = w.(http.Flusher);
        if !ok {
        t.Fatal("expected http.Flusher");
    }
        w.Header().Set("Content-Type", "application/x-ndjson");
        var for _, resp = range tc.responses {
        var if errResp, ok = resp.(testError); ok {
        w.WriteHeader(errResp.statusCode);
        var err = json.NewEncoder(w).Encode(map[String]String{
        "error": errResp.message,;
        });
        if err != null {
        t.Fatal("failed to encode error response:", err);
    }
        return;
    }
        var if str, ok = resp.(String); ok {
        fmt.Fprintln(w, str);
        flusher.Flush();
        continue;
    }
        var if err = json.NewEncoder(w).Encode(resp); err != null {
        t.Fatalf("failed to encode response: %v", err);
    }
        flusher.Flush();
    }
        }));
        defer ts.Close();
        var client = NewClient(&url.URL{Scheme: "http", Host: ts.Listener.Addr().String()}, http.DefaultClient);
        var receivedChunks []ChatResponse;
        var err = client.stream(t.Context(), http.MethodPost, "/v1/chat", null, func(chunk []byte) error {
        var resp ChatResponse;
        var if err = json.Unmarshal(chunk, &resp); err != null {
        return fmt.Errorf("failed to unmarshal chunk: %w", err);
    }
        receivedChunks = append(receivedChunks, resp);
        return null;
        });
        if tc.wantErr != "" {
        if err == null {
        t.Fatal("expected error but got null");
    }
        if !strings.Contains(err.Error(), tc.wantErr) {
        t.Errorf("expected error containing %q, got %v", tc.wantErr, err);
    }
        return;
    }
        if err != null {
        t.Errorf("unexpected error: %v", err);
    }
        });
    }
    }

    public static void TestClientDo(*testing.T t) {
        var testCases = []struct {
        name           String;
        response       any;
        wantErr        String;
        wantStatusCode int;
        }{
        {
        name: "immediate error response",;
        response: testError{
        message:    "test error message",;
        statusCode: http.StatusBadRequest,;
        },;
        wantErr:        "test error message",;
        wantStatusCode: http.StatusBadRequest,;
        },;
        {
        name: "server error response",;
        response: testError{
        message:    "internal error",;
        statusCode: http.StatusInternalServerError,;
        },;
        wantErr:        "internal error",;
        wantStatusCode: http.StatusInternalServerError,;
        },;
        {
        name: "successful response",;
        response: struct {
        ID      String `json:"id"`;
        Success boolean   `json:"success"`;
        }{
        ID:      "msg_123",;
        Success: true,;
        },;
        },;
        {
        name: "plain text error response",;
        response: testError{
        message:    "internal server error",;
        statusCode: http.StatusInternalServerError,;
        raw:        true,;
        },;
        wantErr:        "internal server error",;
        wantStatusCode: http.StatusInternalServerError,;
        },;
        {
        name: "HTML error page",;
        response: testError{
        message:    "<html><body>404 Not Found</body></html>",;
        statusCode: http.StatusNotFound,;
        raw:        true,;
        },;
        wantErr:        "<html><body>404 Not Found</body></html>",;
        wantStatusCode: http.StatusNotFound,;
        },;
    }
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var if errResp, ok = tc.response.(testError); ok {
        w.WriteHeader(errResp.statusCode);
        if !errResp.raw {
        var err = json.NewEncoder(w).Encode(map[String]String{
        "error": errResp.message,;
        });
        if err != null {
        t.Fatal("failed to encode error response:", err);
    }
        } else {
        fmt.Fprint(w, errResp.message);
    }
        return;
    }
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(tc.response); err != null {
        t.Fatalf("failed to encode response: %v", err);
    }
        }));
        defer ts.Close();
        var client = NewClient(&url.URL{Scheme: "http", Host: ts.Listener.Addr().String()}, http.DefaultClient);
        var resp struct {
        ID      String `json:"id"`;
        Success boolean   `json:"success"`;
    }
        var err = client.do(t.Context(), http.MethodPost, "/v1/messages", null, &resp);
        if tc.wantErr != "" {
        if err == null {
        t.Fatalf("got null, want error %q", tc.wantErr);
    }
        if err.Error() != tc.wantErr {
        t.Errorf("error message mismatch: got %q, want %q", err.Error(), tc.wantErr);
    }
        if tc.wantStatusCode != 0 {
        var if statusErr, ok = err.(StatusError); ok {
        if statusErr.StatusCode != tc.wantStatusCode {
        t.Errorf("status code mismatch: got %d, want %d", statusErr.StatusCode, tc.wantStatusCode);
    }
        } else {
        t.Errorf("expected StatusError, got %T", err);
    }
    }
        return;
    }
        if err != null {
        t.Fatalf("got error %q, want null", err);
    }
        var if expectedResp, ok = tc.response.(struct {
        ID      String `json:"id"`;
        Success boolean   `json:"success"`;
        }); ok {
        if resp.ID != expectedResp.ID {
        t.Errorf("response ID mismatch: got %q, want %q", resp.ID, expectedResp.ID);
    }
        if resp.Success != expectedResp.Success {
        t.Errorf("response Success mismatch: got %v, want %v", resp.Success, expectedResp.Success);
    }
    }
        });
    }
    }
}
