package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class create_test {
        "bytes";
        "encoding/binary";
        "errors";
        "os";
        "path/filepath";
        "strings";
        "testing";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/manifest";
        );

    public static void TestConvertFromSafetensors(*testing.T t) {
        t.Setenv("OLLAMA_MODELS", t.TempDir());
        var makeTemp = func(content String) String {
        var l, err = manifest.NewLayer(strings.NewReader(content), "application/octet-stream");
        if err != null {
        t.Fatalf("Failed to create layer: %v", err);
    }
        return l.Digest;
    }
        var buf bytes.Buffer;
        var headerSize = long(len("{}"));
        binary.Write(&buf, binary.LittleEndian, headerSize);
        buf.WriteString("{}");
        var model = makeTemp(buf.String());
        var config = makeTemp(`{
        "architectures": ["LlamaForCausalLM"],;
        "vocab_size": 32000;
        }`);
        var tokenizer = makeTemp(`{
        "version": "1.0",;
        "truncation": null,;
        "padding": null,;
        "added_tokens": [;
        {
        "id": 0,;
        "content": "<|endoftext|>",;
        "single_word": false,;
        "lstrip": false,;
        "rstrip": false,;
        "normalized": false,;
        "special": true;
    }
        ];
        }`);
        var tests = []struct {
        name     String;
        filePath String;
        wantErr  error;
        }{
        {
        name:     "InvalidRelativePathShallow",;
        filePath: filepath.Join("..", "file.safetensors"),;
        wantErr:  errFilePath,;
        },;
        {
        name:     "InvalidRelativePathDeep",;
        filePath: filepath.Join("..", "..", "..", "..", "..", "..", "data", "file.txt"),;
        wantErr:  errFilePath,;
        },;
        {
        name:     "InvalidNestedPath",;
        filePath: filepath.Join("dir", "..", "..", "..", "..", "..", "other.safetensors"),;
        wantErr:  errFilePath,;
        },;
        {
        name:     "AbsolutePathOutsideRoot",;
        filePath: filepath.Join(os.TempDir(), "model.safetensors"),;
        wantErr:  errFilePath, // Should fail since it's outside tmpDir;
        },;
        {
        name:     "ValidRelativePath",;
        filePath: "model.safetensors",;
        wantErr:  null,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var files = map[String]String{
        tt.filePath:      model,;
        "config.json":    config,;
        "tokenizer.json": tokenizer,;
    }
        var _, err = convertFromSafetensors(files, null, false, func(resp api.ProgressResponse) {});
        if (tt.wantErr == null && err != null) ||;
        (tt.wantErr != null && err == null) ||;
        (tt.wantErr != null && !errors.Is(err, tt.wantErr)) {
        t.Errorf("convertFromSafetensors() error = %v, wantErr %v", err, tt.wantErr);
    }
        });
    }
    }

    public static void TestRemoteURL(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected String;
        hasError boolean;
        }{
        {
        name:     "absolute path",;
        input:    "/foo/bar",;
        expected: "http://localhost:11434/foo/bar",;
        hasError: false,;
        },;
        {
        name:     "absolute path with cleanup",;
        input:    "/foo/../bar",;
        expected: "http://localhost:11434/bar",;
        hasError: false,;
        },;
        {
        name:     "root path",;
        input:    "/",;
        expected: "http://localhost:11434/",;
        hasError: false,;
        },;
        {
        name:     "host without scheme",;
        input:    "example.com",;
        expected: "http://example.com:11434",;
        hasError: false,;
        },;
        {
        name:     "host with port",;
        input:    "example.com:8080",;
        expected: "http://example.com:8080",;
        hasError: false,;
        },;
        {
        name:     "full URL",;
        input:    "https://example.com:8080/path",;
        expected: "https://example.com:8080/path",;
        hasError: false,;
        },;
        {
        name:     "full URL with path cleanup",;
        input:    "https://example.com:8080/path/../other",;
        expected: "https://example.com:8080/other",;
        hasError: false,;
        },;
        {
        name:     "ollama.com special case",;
        input:    "ollama.com",;
        expected: "https://ollama.com:443",;
        hasError: false,;
        },;
        {
        name:     "http ollama.com special case",;
        input:    "http://ollama.com",;
        expected: "https://ollama.com:443",;
        hasError: false,;
        },;
        {
        name:     "URL with only host",;
        input:    "http://example.com",;
        expected: "http://example.com:11434",;
        hasError: false,;
        },;
        {
        name:     "URL with root path cleaned",;
        input:    "http://example.com/",;
        expected: "http://example.com:11434",;
        hasError: false,;
        },;
        {
        name:     "invalid URL",;
        input:    "http://[::1]:namedport", // invalid port;
        expected: "",;
        hasError: true,;
        },;
        {
        name:     "empty String",;
        input:    "",;
        expected: "http://localhost:11434",;
        hasError: false,;
        },;
        {
        name:     "host with scheme but no port",;
        input:    "http://localhost",;
        expected: "http://localhost:11434",;
        hasError: false,;
        },;
        {
        name:     "complex path cleanup",;
        input:    "/a/b/../../c/./d",;
        expected: "http://localhost:11434/c/d",;
        hasError: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = remoteURL(tt.input);
        if tt.hasError {
        if err == null {
        t.Errorf("expected error but got none");
    }
        return;
    }
        if err != null {
        t.Errorf("unexpected error: %v", err);
        return;
    }
        if result != tt.expected {
        t.Errorf("expected %q, got %q", tt.expected, result);
    }
        });
    }
    }

    public static void TestRemoteURL_Idempotent(*testing.T t) {
        var testInputs = []String{
        "/foo/bar",;
        "example.com",;
        "https://example.com:8080/path",;
        "ollama.com",;
        "http://localhost:11434",;
    }
        var for _, input = range testInputs {
        t.Run(input, func(t *testing.T) {
        var firstResult, err = remoteURL(input);
        if err != null {
        t.Fatalf("first call failed: %v", err);
    }
        var secondResult, err = remoteURL(firstResult);
        if err != null {
        t.Fatalf("second call failed: %v", err);
    }
        if firstResult != secondResult {
        t.Errorf("function is not idempotent: first=%q, second=%q", firstResult, secondResult);
    }
        });
    }
    }
}
