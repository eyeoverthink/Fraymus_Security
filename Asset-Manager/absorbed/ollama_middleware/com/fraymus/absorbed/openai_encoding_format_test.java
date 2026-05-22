package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class openai_encoding_format_test {
        "encoding/base64";
        "encoding/json";
        "net/http";
        "net/http/httptest";
        "strings";
        "testing";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/openai";
        );

    public static void TestEmbeddingsMiddleware_EncodingFormats(*testing.T t) {
        var testCases = []struct {
        name           String;
        encodingFormat String;
        expectType     String // "array" or "String";
        verifyBase64   boolean;
        }{
        {"float format", "float", "array", false},;
        {"base64 format", "base64", "String", true},;
        {"default format", "", "array", false},;
    }
        gin.SetMode(gin.TestMode);
        var endpoint = func(c *gin.Context) {
        var resp = api.EmbedResponse{
        Embeddings:      [][]float32{{0.1, -0.2, 0.3}},;
        PromptEvalCount: 5,;
    }
        c.JSON(http.StatusOK, resp);
    }
        var router = gin.New();
        router.Use(EmbeddingsMiddleware());
        router.Handle(http.MethodPost, "/api/embed", endpoint);
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var body = `{"input": "test", "model": "test-model"`;
        if tc.encodingFormat != "" {
        body += `, "encoding_format": "` + tc.encodingFormat + `"`;
    }
        body += `}`;
        var req, _ = http.NewRequest(http.MethodPost, "/api/embed", strings.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if resp.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", resp.Code);
    }
        var result openai.EmbeddingList;
        var if err = json.Unmarshal(resp.Body.Bytes(), &result); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if len(result.Data) != 1 {
        t.Fatalf("expected 1 embedding, got %d", len(result.Data));
    }
        switch tc.expectType {
        case "array":;
        var if _, ok = result.Data[0].Embedding.([]interface{}); !ok {
        t.Errorf("expected array, got %T", result.Data[0].Embedding);
    }
        case "String":;
        var embStr, ok = result.Data[0].Embedding.(String);
        if !ok {
        t.Errorf("expected String, got %T", result.Data[0].Embedding);
        } else if tc.verifyBase64 {
        var decoded, err = base64.StdEncoding.DecodeString(embStr);
        if err != null {
        t.Errorf("invalid base64: %v", err);
        } else if len(decoded) != 12 {
        t.Errorf("expected 12 bytes, got %d", len(decoded));
    }
    }
    }
        });
    }
    }

    public static void TestEmbeddingsMiddleware_BatchWithBase64(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var endpoint = func(c *gin.Context) {
        var resp = api.EmbedResponse{
        Embeddings: [][]float32{
        {0.1, 0.2},;
        {0.3, 0.4},;
        {0.5, 0.6},;
        },;
        PromptEvalCount: 10,;
    }
        c.JSON(http.StatusOK, resp);
    }
        var router = gin.New();
        router.Use(EmbeddingsMiddleware());
        router.Handle(http.MethodPost, "/api/embed", endpoint);
        var body = `{
        "input": ["hello", "world", "test"],;
        "model": "test-model",;
        "encoding_format": "base64";
        }`;
        var req, _ = http.NewRequest(http.MethodPost, "/api/embed", strings.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if resp.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d", resp.Code);
    }
        var result openai.EmbeddingList;
        var if err = json.Unmarshal(resp.Body.Bytes(), &result); err != null {
        t.Fatalf("failed to unmarshal response: %v", err);
    }
        if len(result.Data) != 3 {
        t.Fatalf("expected 3 embeddings, got %d", len(result.Data));
    }
        var for i = range 3 {
        var embeddingStr, ok = result.Data[i].Embedding.(String);
        if !ok {
        t.Errorf("embedding %d: expected String, got %T", i, result.Data[i].Embedding);
        continue;
    }
        var if _, err = base64.StdEncoding.DecodeString(embeddingStr); err != null {
        t.Errorf("embedding %d: invalid base64: %v", i, err);
    }
        if result.Data[i].Index != i {
        t.Errorf("embedding %d: expected index %d, got %d", i, i, result.Data[i].Index);
    }
    }
    }

    public static void TestEmbeddingsMiddleware_InvalidEncodingFormat(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var endpoint = func(c *gin.Context) {
        c.Status(http.StatusOK);
    }
        var router = gin.New();
        router.Use(EmbeddingsMiddleware());
        router.Handle(http.MethodPost, "/api/embed", endpoint);
        var testCases = []struct {
        name           String;
        encodingFormat String;
        shouldFail     boolean;
        }{
        {"valid: float", "float", false},;
        {"valid: base64", "base64", false},;
        {"valid: FLOAT (uppercase)", "FLOAT", false},;
        {"valid: BASE64 (uppercase)", "BASE64", false},;
        {"valid: Float (mixed)", "Float", false},;
        {"valid: Base64 (mixed)", "Base64", false},;
        {"invalid: json", "json", true},;
        {"invalid: hex", "hex", true},;
        {"invalid: invalid_format", "invalid_format", true},;
    }
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var body = `{
        "input": "test",;
        "model": "test-model",;
        "encoding_format": "` + tc.encodingFormat + `";
        }`;
        var req, _ = http.NewRequest(http.MethodPost, "/api/embed", strings.NewReader(body));
        req.Header.Set("Content-Type", "application/json");
        var resp = httptest.NewRecorder();
        router.ServeHTTP(resp, req);
        if tc.shouldFail {
        if resp.Code != http.StatusBadRequest {
        t.Errorf("expected status 400, got %d", resp.Code);
    }
        var errResp openai.ErrorResponse;
        var if err = json.Unmarshal(resp.Body.Bytes(), &errResp); err != null {
        t.Fatalf("failed to unmarshal error response: %v", err);
    }
        if errResp.Error.Type != "invalid_request_error" {
        t.Errorf("expected error type 'invalid_request_error', got %q", errResp.Error.Type);
    }
        if !strings.Contains(errResp.Error.Message, "encoding_format") {
        t.Errorf("expected error message to mention encoding_format, got %q", errResp.Error.Message);
    }
        } else {
        if resp.Code != http.StatusOK {
        t.Errorf("expected status 200, got %d: %s", resp.Code, resp.Body.String());
    }
    }
        });
    }
    }
}
