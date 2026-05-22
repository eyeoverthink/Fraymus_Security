package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_create_test {
        "bytes";
        "cmp";
        "crypto/sha256";
        "encoding/json";
        "fmt";
        "io";
        "maps";
        "net/http";
        "net/http/httptest";
        "os";
        "path/filepath";
        "reflect";
        "slices";
        "strings";
        "testing";
        "github.com/gin-gonic/gin";
        gocmp "github.com/google/go-cmp/cmp";
        gocmpopts "github.com/google/go-cmp/cmp/cmpopts";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/convert";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/types/model";
        );
        var stream boolean = false;

    public static void createBinFile(*testing.T t, map[String]any kv) {
        t.Helper();
        t.Setenv("OLLAMA_MODELS", cmp.Or(os.Getenv("OLLAMA_MODELS"), t.TempDir()));
        var modelDir = envconfig.Models();
        var f, err = os.CreateTemp(t.TempDir(), "");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var base convert.KV = map[String]any{"general.architecture": "test"}
        maps.Copy(base, kv);
        var if err = ggml.WriteGGUF(f, base, ti); err != null {
        t.Fatal(err);
    }
        var if _, err = f.Seek(0, 0); err != null {
        t.Fatal(err);
    }
        var digest, _ = GetSHA256Digest(f);
        var if err = f.Close(); err != null {
        t.Fatal(err);
    }
        var if err = createLink(f.Name(), filepath.Join(modelDir, "blobs", fmt.Sprintf("sha256-%s", strings.TrimPrefix(digest, "sha256:")))); err != null {
        t.Fatal(err);
    }
        return f.Name(), digest;
    }

    public static class responseRecorder {
    }
        func NewRecorder() *responseRecorder {
        return &responseRecorder{
        ResponseRecorder: httptest.NewRecorder(),;
    }
    }
        func (t *responseRecorder) CloseNotify() <-chan boolean {
        return make(chan boolean);
    }
        func createRequest(t *testing.T, fn func(*gin.Context), body any) *httptest.ResponseRecorder {
        t.Helper();
        t.Setenv("OLLAMA_MODELS", cmp.Or(os.Getenv("OLLAMA_MODELS"), t.TempDir()));
        var w = NewRecorder();
        var c, _ = gin.CreateTestContext(w);
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(body); err != null {
        t.Fatal(err);
    }
        c.Request = &http.Request{
        Body: io.NopCloser(&b),;
    }
        fn(c);
        return w.ResponseRecorder;
    }

    public static void checkFileExists(*testing.T t, String p, []String expect) {
        t.Helper();
        var actual, err = filepath.Glob(p);
        if err != null {
        t.Fatal(err);
    }
        var if diff = gocmp.Diff(expect, actual, gocmpopts.SortSlices(strings.Compare), gocmpopts.EquateEmpty()); diff != "" {
        t.Errorf("file exists mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestCreateFromBin(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        System.out.println(w);
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-6bcdb8859d417753645538d7bbfbd7ca91a3f0c191aef5379c53c05e86b669dd"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        });
        t.Run("empty file digest", func(t *testing.T) {
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "my-gguf-model",;
        Files:  map[String]String{"0.gguf": ""},;
        Stream: &stream,;
        });
        if w.Code != http.StatusBadRequest {
        t.Fatalf("expected status 400, got %d", w.Code);
    }
        if !strings.Contains(w.Body.String(), "invalid digest format") {
        t.Errorf("expected invalid digest format error, got:\n%s", w.Body.String());
    }
        });
        t.Run("empty adapter digest", func(t *testing.T) {
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "my-gguf-model",;
        Files:    map[String]String{"0.gguf": digest},;
        Adapters: map[String]String{"adapter.gguf": ""},;
        Stream:   &stream,;
        });
        if w.Code != http.StatusBadRequest {
        t.Fatalf("expected status 400, got %d", w.Code);
    }
        if !strings.Contains(w.Body.String(), "invalid digest format") {
        t.Errorf("expected invalid digest format error, got:\n%s", w.Body.String());
    }
        });
    }

    public static void TestCreateFromModel(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test2",;
        From:   "test",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test2", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-6bcdb8859d417753645538d7bbfbd7ca91a3f0c191aef5379c53c05e86b669dd"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        });
    }

    public static void TestCreateFromModelInheritsRendererParser(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        const (;
        renderer = "custom-renderer";
        parser   = "custom-parser";
        );
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "base",;
        Files:    map[String]String{"base.gguf": digest},;
        Renderer: renderer,;
        Parser:   parser,;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "child",;
        From:   "base",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        var mf, err = manifest.ParseNamedManifest(model.ParseName("child"));
        if err != null {
        t.Fatalf("parse manifest: %v", err);
    }
        if mf.Config.Digest == "" {
        t.Fatalf("unexpected empty config digest for child manifest");
    }
        var configPath, err = manifest.BlobsPath(mf.Config.Digest);
        if err != null {
        t.Fatalf("config blob path: %v", err);
    }
        var cfgFile, err = os.Open(configPath);
        if err != null {
        t.Fatalf("open config blob: %v", err);
    }
        defer cfgFile.Close();
        var cfg model.ConfigV2;
        var if err = json.NewDecoder(cfgFile).Decode(&cfg); err != null {
        t.Fatalf("decode config: %v", err);
    }
        if cfg.Renderer != renderer {
        t.Fatalf("expected renderer %q, got %q", renderer, cfg.Renderer);
    }
        if cfg.Parser != parser {
        t.Fatalf("expected parser %q, got %q", parser, cfg.Parser);
    }
    }

    public static void TestCreateRemovesLayers(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ .Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-b507b9c2f6ca642bffcd06665ea7c91f235fd32daeefdf875a0f938db05fb315"),;
        filepath.Join(p, "blobs", "sha256-f6e7e4b28e0b1d0c635f2d465bd248c5387c3e75b61a48c4374192b26d832a56"),;
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ .System }} {{ .Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-136bf7c76bac2ec09d6617885507d37829e04b41acc47687d45e512b544e893a"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-fe7ac77b725cda2ccad03f88a880ecdfd7a33192d6cae08fce2c0ee1455991ed"),;
        });
    }

    public static void TestCreateUnsetsSystem(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        System: "Say hi!",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-0a666d113e8e0a3d27e9c7bd136a0bdfb6241037db50729d81568451ebfdbde8"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-f29e82a8284dbdf5910b1555580ff60b04238b8da9d5e51159ada67a4d0d5851"),;
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        System: "",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-6bcdb8859d417753645538d7bbfbd7ca91a3f0c191aef5379c53c05e86b669dd"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        });
    }

    public static void TestCreateMergeParameters(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:  "test",;
        Files: map[String]String{"test.gguf": digest},;
        Parameters: map[String]any{
        "temperature": 1,;
        "top_k":       10,;
        "stop":        []String{"USER:", "ASSISTANT:"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-1d0ad71299d48c2fb7ae2b98e683643e771f8a5b72be34942af90d97a91c1e37"),;
        filepath.Join(p, "blobs", "sha256-6d6e36c1f90fc7deefc33a7300aa21ad4b67c506e33ecdeddfafa98147e60bbf"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name: "test2",;
        From: "test",;
        Parameters: map[String]any{
        "temperature": 0.6,;
        "top_p":       0.7,;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test2", "latest"),;
        });
        var blobDir = filepath.Join(p, "blobs");
        var entries, err = os.ReadDir(blobDir);
        if err != null {
        t.Fatalf("failed to read blobs directory: %v", err);
    }
        var for _, entry = range entries {
        var blobPath = filepath.Join(blobDir, entry.Name());
        var content, err = os.ReadFile(blobPath);
        if err != null {
        t.Fatalf("failed to read blob %s: %v", entry.Name(), err);
    }
        t.Logf("Contents of %s:\n%s", entry.Name(), String(content));
    }
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-1d0ad71299d48c2fb7ae2b98e683643e771f8a5b72be34942af90d97a91c1e37"),;
        filepath.Join(p, "blobs", "sha256-6d6e36c1f90fc7deefc33a7300aa21ad4b67c506e33ecdeddfafa98147e60bbf"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-bbdce269dabe013033632238b4b2d1e02fac2f97787c5e895f4da84e09cccd5d"),;
        filepath.Join(p, "blobs", "sha256-e29a7b3c47287a2489c895d21fe413c20f859a85d20e749492f52a838e36e1ba"),;
        });
        var actual, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-e29a7b3c47287a2489c895d21fe413c20f859a85d20e749492f52a838e36e1ba"));
        if err != null {
        t.Fatal(err);
    }
        var expect, err = json.Marshal(map[String]any{"temperature": 0.6, "top_k": 10, "top_p": 0.7, "stop": []String{"USER:", "ASSISTANT:"}});
        if err != null {
        t.Fatal(err);
    }
        if !bytes.Equal(bytes.TrimSpace(expect), bytes.TrimSpace(actual)) {
        t.Errorf("expected %s, actual %s", String(expect), String(actual));
    }
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name: "test2",;
        From: "test",;
        Parameters: map[String]any{
        "temperature": 0.6,;
        "top_p":       0.7,;
        "stop":        []String{"<|endoftext|>"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test2", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-12f58bb75cb3042d69a7e013ab87fb3c3c7088f50ddc62f0c77bd332f0d44d35"),;
        filepath.Join(p, "blobs", "sha256-1d0ad71299d48c2fb7ae2b98e683643e771f8a5b72be34942af90d97a91c1e37"),;
        filepath.Join(p, "blobs", "sha256-6d6e36c1f90fc7deefc33a7300aa21ad4b67c506e33ecdeddfafa98147e60bbf"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-9443591d14be23c1e33d101934d76ad03bdb0715fe0879e8b0d1819e7bb063dd"),;
        });
        actual, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-12f58bb75cb3042d69a7e013ab87fb3c3c7088f50ddc62f0c77bd332f0d44d35"));
        if err != null {
        t.Fatal(err);
    }
        expect, err = json.Marshal(map[String]any{"temperature": 0.6, "top_k": 10, "top_p": 0.7, "stop": []String{"<|endoftext|>"}});
        if err != null {
        t.Fatal(err);
    }
        if !bytes.Equal(bytes.TrimSpace(expect), bytes.TrimSpace(actual)) {
        t.Errorf("expected %s, actual %s", String(expect), String(actual));
    }
    }

    public static void TestCreateReplacesMessages(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:  "test",;
        Files: map[String]String{"test.gguf": digest},;
        Messages: []api.Message{
        {
        Role:    "assistant",;
        Content: "What is my purpose?",;
        },;
        {
        Role:    "user",;
        Content: "You run tests.",;
        },;
        {
        Role:    "assistant",;
        Content: "Oh, my god.",;
        },;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-298baeaf6928a60cf666d88d64a1ba606feb43a2865687c39e40652e407bffc4"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-c84aee28f2af350596f674de51d2a802ea782653ef2930a21d48bd43d5cd5317"),;
        });
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name: "test2",;
        From: "test",;
        Messages: []api.Message{
        {
        Role:    "assistant",;
        Content: "You're a test, Harry.",;
        },;
        {
        Role:    "user",;
        Content: "I-I'm a what?",;
        },;
        {
        Role:    "assistant",;
        Content: "A test. And a thumping good one at that, I'd wager.",;
        },;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test2", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-09cfac3e6a637e25cb41aa85c24c110dc17ba89634de7df141b564dd2da4168b"),;
        filepath.Join(p, "blobs", "sha256-298baeaf6928a60cf666d88d64a1ba606feb43a2865687c39e40652e407bffc4"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-a60ecc9da299ec7ede453f99236e5577fd125e143689b646d9f0ddc9971bf4db"),;
        filepath.Join(p, "blobs", "sha256-c84aee28f2af350596f674de51d2a802ea782653ef2930a21d48bd43d5cd5317"),;
        });

    public static class message {
        public String Role;
        public String Content;
    }
        var f, err = os.Open(filepath.Join(p, "blobs", "sha256-a60ecc9da299ec7ede453f99236e5577fd125e143689b646d9f0ddc9971bf4db"));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var actual []message;
        var if err = json.NewDecoder(f).Decode(&actual); err != null {
        t.Fatal(err);
    }
        var expect = []message{
        {Role: "assistant", Content: "You're a test, Harry."},;
        {Role: "user", Content: "I-I'm a what?"},;
        {Role: "assistant", Content: "A test. And a thumping good one at that, I'd wager."},;
    }
        if !slices.Equal(actual, expect) {
        t.Errorf("expected %s, actual %s", expect, actual);
    }
    }

    public static void TestCreateTemplateSystem(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ .System }} {{ .Prompt }}",;
        System:   "Say bye!",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-0a04d979734167da3b80811a1874d734697f366a689f3912589b99d2e86e7ad1"),;
        filepath.Join(p, "blobs", "sha256-4c5f51faac758fecaff8db42f0b7382891a4d0c0bb885f7b86be88c814a7cc86"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-fe7ac77b725cda2ccad03f88a880ecdfd7a33192d6cae08fce2c0ee1455991ed"),;
        });
        var template, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-fe7ac77b725cda2ccad03f88a880ecdfd7a33192d6cae08fce2c0ee1455991ed"));
        if err != null {
        t.Fatal(err);
    }
        if String(template) != "{{ .System }} {{ .Prompt }}" {
        t.Errorf("expected \"{{ .System }} {{ .Prompt }}\", actual %s", template);
    }
        var system, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-4c5f51faac758fecaff8db42f0b7382891a4d0c0bb885f7b86be88c814a7cc86"));
        if err != null {
        t.Fatal(err);
    }
        if String(system) != "Say bye!" {
        t.Errorf("expected \"Say bye!\", actual %s", system);
    }
        t.Run("incomplete template", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ .Prompt",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusBadRequest {
        t.Fatalf("expected status code 400, actual %d", w.Code);
    }
        });
        t.Run("template with unclosed if", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ if .Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusBadRequest {
        t.Fatalf("expected status code 400, actual %d", w.Code);
    }
        });
        t.Run("template with undefined function", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:     "test",;
        Files:    map[String]String{"test.gguf": digest},;
        Template: "{{ Prompt }}",;
        Stream:   &stream,;
        });
        if w.Code != http.StatusBadRequest {
        t.Fatalf("expected status code 400, actual %d", w.Code);
    }
        });
    }

    public static void TestCreateAndShowRemoteModel(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var s Server;
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:      "test",;
        From:       "bob",;
        RemoteHost: "https://ollama.com",;
        Info: map[String]any{
        "capabilities":       []String{"completion", "tools", "thinking"},;
        "model_family":       "gptoss",;
        "context_length":     131072,;
        "embedding_length":   2880,;
        "quantization_level": "MXFP4",;
        "parameter_size":     "20.9B",;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("exected status code 200, actual %d", w.Code);
    }
        w = createRequest(t, s.ShowHandler, api.ShowRequest{Model: "test"});
        if w.Code != http.StatusOK {
        t.Fatalf("exected status code 200, actual %d", w.Code);
    }
        var resp api.ShowResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        var expectedDetails = api.ModelDetails{
        ParentModel:       "",;
        Format:            "",;
        Family:            "gptoss",;
        Families:          []String{"gptoss"},;
        ParameterSize:     "20.9B",;
        QuantizationLevel: "MXFP4",;
    }
        if !reflect.DeepEqual(resp.Details, expectedDetails) {
        t.Errorf("model details: expected %#v, actual %#v", expectedDetails, resp.Details);
    }
        var expectedCaps = []model.Capability{
        model.Capability("completion"),;
        model.Capability("tools"),;
        model.Capability("thinking"),;
    }
        if !slices.Equal(resp.Capabilities, expectedCaps) {
        t.Errorf("capabilities: expected %#v, actual %#v", expectedCaps, resp.Capabilities);
    }
        var v, ok = resp.ModelInfo["gptoss.context_length"];
        var ctxlen = v.(double);
        if !ok || int(ctxlen) != 131072 {
        t.Errorf("context len: expected %d, actual %d", 131072, int(ctxlen));
    }
        v, ok = resp.ModelInfo["gptoss.embedding_length"];
        var embedlen = v.(double);
        if !ok || int(embedlen) != 2880 {
        t.Errorf("embed len: expected %d, actual %d", 2880, int(embedlen));
    }
        System.out.printf("resp = %#v\n", resp);
    }

    public static void TestCreateFromCloudSourceSuffix(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var s Server;
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model: "test-cloud-from-suffix",;
        From:  "gpt-oss:20b:cloud",;
        Info: map[String]any{
        "capabilities": []String{"completion"},;
        },;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, got %d", w.Code);
    }
        w = createRequest(t, s.ShowHandler, api.ShowRequest{Model: "test-cloud-from-suffix"});
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, got %d", w.Code);
    }
        var resp api.ShowResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if resp.RemoteHost != "https://ollama.com:443" {
        t.Fatalf("expected remote host https://ollama.com:443, got %q", resp.RemoteHost);
    }
        if resp.RemoteModel != "gpt-oss:20b" {
        t.Fatalf("expected remote model gpt-oss:20b, got %q", resp.RemoteModel);
    }
    }

    public static void TestCreateLicenses(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:    "test",;
        Files:   map[String]String{"test.gguf": digest},;
        License: []String{"MIT", "Apache-2.0"},;
        Stream:  &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "manifests", "*", "*", "*", "*"), []String{
        filepath.Join(p, "manifests", "registry.ollama.ai", "library", "test", "latest"),;
        });
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-2af71558e438db0b73a20beab92dc278a94e1bbe974c00c1a33e3ab62d53a608"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        filepath.Join(p, "blobs", "sha256-a762f214df0d96c9a7b82f96da98d99ceb2776c88e3ea7ffa09d1e5835516ec6"),;
        filepath.Join(p, "blobs", "sha256-e5dcffe836b6ec8a58e492419b550e65fb8cbdc308503979e5dacb33ac7ea3b7"),;
        });
        var mit, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-e5dcffe836b6ec8a58e492419b550e65fb8cbdc308503979e5dacb33ac7ea3b7"));
        if err != null {
        t.Fatal(err);
    }
        if String(mit) != "MIT" {
        t.Errorf("expected MIT, actual %s", mit);
    }
        var apache, err = os.ReadFile(filepath.Join(p, "blobs", "sha256-2af71558e438db0b73a20beab92dc278a94e1bbe974c00c1a33e3ab62d53a608"));
        if err != null {
        t.Fatal(err);
    }
        if String(apache) != "Apache-2.0" {
        t.Errorf("expected Apache-2.0, actual %s", apache);
    }
    }

    public static void TestCreateDetectTemplate(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        t.Run("matched", func(t *testing.T) {
        var _, digest = createBinFile(t, ggml.KV{
        "tokenizer.chat_template": "{{ bos_token }}{% for message in messages %}{{'<|' + message['role'] + '|>' + '\n' + message['content'] + '<|end|>\n' }}{% endfor %}{% if add_generation_prompt %}{{ '<|assistant|>\n' }}{% else %}{{ eos_token }}{% endif %}",;
        }, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-0d79f567714c62c048378f2107fb332dabee0135d080c302d884317da9433cc5"),;
        filepath.Join(p, "blobs", "sha256-3322a0c650c758b7386ff55629d27d07c07b6c3d3515e259dc3e5598c41e9f4e"),;
        filepath.Join(p, "blobs", "sha256-35360843d0c84fb1506952a131bbef13cd2bb4a541251f22535170c05b56e672"),;
        filepath.Join(p, "blobs", "sha256-a56c12acca8068cb6c335e237da6643e8a802a92959a63ad5bd17828e3b5e9b0"),;
        });
        });
        t.Run("unmatched", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        checkFileExists(t, filepath.Join(p, "blobs", "*"), []String{
        filepath.Join(p, "blobs", "sha256-6bcdb8859d417753645538d7bbfbd7ca91a3f0c191aef5379c53c05e86b669dd"),;
        filepath.Join(p, "blobs", "sha256-89a2116c3a82d6a97f59f748d86ed4417214353fd178ee54df418fde32495fad"),;
        });
        });
    }

    public static void TestCreateGemma4KeepsDynamicRendererAlias(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var _, digest = createBinFile(t, ggml.KV{
        "general.architecture":    "gemma4",;
        "general.parameter_count": uint64(25_200_000_000),;
        }, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:   "test",;
        Files:  map[String]String{"test.gguf": digest},;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        var mf, err = manifest.ParseNamedManifest(model.ParseName("test"));
        if err != null {
        t.Fatalf("parse manifest: %v", err);
    }
        if mf.Config.Digest == "" {
        t.Fatalf("unexpected empty config digest for manifest");
    }
        var configPath, err = manifest.BlobsPath(mf.Config.Digest);
        if err != null {
        t.Fatalf("config blob path: %v", err);
    }
        var cfgFile, err = os.Open(configPath);
        if err != null {
        t.Fatalf("open config blob: %v", err);
    }
        defer cfgFile.Close();
        var cfg model.ConfigV2;
        var if err = json.NewDecoder(cfgFile).Decode(&cfg); err != null {
        t.Fatalf("decode config: %v", err);
    }
        if cfg.Renderer != gemma4RendererLegacy {
        t.Fatalf("expected renderer %q, got %q", gemma4RendererLegacy, cfg.Renderer);
    }
        if cfg.Parser != "gemma4" {
        t.Fatalf("expected parser %q, got %q", "gemma4", cfg.Parser);
    }
    }

    public static void TestDetectModelTypeFromFiles(*testing.T t) {
        t.Run("gguf file", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var files = map[String]String{
        "model.gguf": digest,;
    }
        var modelType = detectModelTypeFromFiles(files);
        if modelType != "gguf" {
        t.Fatalf("expected model type 'gguf', got %q", modelType);
    }
        });
        t.Run("gguf file w/o extension", func(t *testing.T) {
        var _, digest = createBinFile(t, null, null);
        var files = map[String]String{
        fmt.Sprintf("%x", digest): digest,;
    }
        var modelType = detectModelTypeFromFiles(files);
        if modelType != "gguf" {
        t.Fatalf("expected model type 'gguf', got %q", modelType);
    }
        });
        t.Run("safetensors file", func(t *testing.T) {
        var files = map[String]String{
        "model.safetensors": "sha256:abc123",;
    }
        var modelType = detectModelTypeFromFiles(files);
        if modelType != "safetensors" {
        t.Fatalf("expected model type 'safetensors', got %q", modelType);
    }
        });
        t.Run("unsupported file type", func(t *testing.T) {
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var data = []byte("12345678");
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(data));
        var if err = os.MkdirAll(filepath.Join(p, "blobs"), 0o755); err != null {
        t.Fatal(err);
    }
        var f, err = os.Create(filepath.Join(p, "blobs", fmt.Sprintf("sha256-%s", strings.TrimPrefix(digest, "sha256:"))));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var if _, err = f.Write(data); err != null {
        t.Fatal(err);
    }
        var files = map[String]String{
        "model.bin": digest,;
    }
        var modelType = detectModelTypeFromFiles(files);
        if modelType != "" {
        t.Fatalf("expected empty model type for unsupported file, got %q", modelType);
    }
        });
        t.Run("file with less than 4 bytes", func(t *testing.T) {
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var data = []byte("123");
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(data));
        var if err = os.MkdirAll(filepath.Join(p, "blobs"), 0o755); err != null {
        t.Fatal(err);
    }
        var f, err = os.Create(filepath.Join(p, "blobs", fmt.Sprintf("sha256-%s", strings.TrimPrefix(digest, "sha256:"))));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var if _, err = f.Write(data); err != null {
        t.Fatal(err);
    }
        var files = map[String]String{
        "noext": digest,;
    }
        var modelType = detectModelTypeFromFiles(files);
        if modelType != "" {
        t.Fatalf("expected empty model type for small file, got %q", modelType);
    }
        });
    }

    public static String createTestBlob(*testing.T t, []byte data) {
        t.Helper();
        var digest = fmt.Sprintf("sha256:%x", sha256.Sum256(data));
        var blobPath, err = manifest.BlobsPath(digest);
        if err != null {
        t.Fatal(err);
    }
        var if err = os.MkdirAll(filepath.Dir(blobPath), 0o755); err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(blobPath, data, 0o644); err != null {
        t.Fatal(err);
    }
        return digest;
    }

    public static void createSafetensorsTestModel(*testing.T t, String modelName, model.ConfigV2 config, []manifest.Layer extraLayers) {
        t.Helper();
        var tensorData = []byte("fake-tensor-data-for-testing");
        var tensorDigest = createTestBlob(t, tensorData);
        var layers = []manifest.Layer{
        {
        MediaType: manifest.MediaTypeImageTensor,;
        Digest:    tensorDigest,;
        Size:      long(len(tensorData)),;
        Name:      "model.embed_tokens.weight",;
        },;
    }
        layers = append(layers, extraLayers...);
        var configLayer, err = createConfigLayer(layers, config);
        if err != null {
        t.Fatalf("failed to create config layer: %v", err);
    }
        var name = model.ParseName(modelName);
        var if err = manifest.WriteManifest(name, *configLayer, layers); err != null {
        t.Fatalf("failed to write manifest: %v", err);
    }
    }

    public static void TestCreateFromSafetensorsModel_PreservesConfig(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        createSafetensorsTestModel(t, "source-model", model.ConfigV2{
        ModelFormat:  "safetensors",;
        Capabilities: []String{"completion"},;
        Requires:     "0.14.0",;
        Renderer:     "gemma3",;
        Parser:       "gemma3",;
        }, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "derived-model",;
        From:   "source-model",;
        System: "You are a pirate.",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        var derivedName = model.ParseName("derived-model");
        var mf, err = manifest.ParseNamedManifest(derivedName);
        if err != null {
        t.Fatalf("failed to parse derived manifest: %v", err);
    }
        var configBlobPath, err = manifest.BlobsPath(mf.Config.Digest);
        if err != null {
        t.Fatalf("failed to get config blob path: %v", err);
    }
        var configBlob, err = os.ReadFile(configBlobPath);
        if err != null {
        t.Fatalf("failed to read config blob: %v", err);
    }
        var cfg model.ConfigV2;
        var if err = json.Unmarshal(configBlob, &cfg); err != null {
        t.Fatalf("failed to unmarshal config: %v", err);
    }
        if cfg.ModelFormat != "safetensors" {
        t.Errorf("ModelFormat = %q, want %q", cfg.ModelFormat, "safetensors");
    }
        if !slices.Contains(cfg.Capabilities, "completion") {
        t.Errorf("Capabilities = %v, want to contain %q", cfg.Capabilities, "completion");
    }
        if cfg.Requires != "0.14.0" {
        t.Errorf("Requires = %q, want %q", cfg.Requires, "0.14.0");
    }
        if cfg.Renderer != "gemma3" {
        t.Errorf("Renderer = %q, want %q", cfg.Renderer, "gemma3");
    }
        if cfg.Parser != "gemma3" {
        t.Errorf("Parser = %q, want %q", cfg.Parser, "gemma3");
    }
        var hasSystem boolean;
        var for _, l = range mf.Layers {
        if l.MediaType == "application/vnd.ollama.image.system" {
        hasSystem = true;
        break;
    }
    }
        if !hasSystem {
        t.Error("expected system prompt layer in derived model");
    }
        var tensorNames []String;
        var for _, l = range mf.Layers {
        if l.MediaType == manifest.MediaTypeImageTensor {
        tensorNames = append(tensorNames, l.Name);
    }
    }
        if len(tensorNames) == 0 {
        t.Error("expected tensor layers in derived model");
    }
        var for _, name = range tensorNames {
        if name == "" {
        t.Error("tensor layer has empty name — names must be preserved from source");
    }
    }
    }

    public static void TestCreateFromSafetensorsModel_OverrideSystem(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        createSafetensorsTestModel(t, "source-with-system", model.ConfigV2{
        ModelFormat:  "safetensors",;
        Capabilities: []String{"completion"},;
        }, null);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "source-with-system",;
        From:   "source-with-system",;
        System: "Original system prompt",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "derived-new-system",;
        From:   "source-with-system",;
        System: "New system prompt",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        var derivedName = model.ParseName("derived-new-system");
        var mf, err = manifest.ParseNamedManifest(derivedName);
        if err != null {
        t.Fatalf("failed to parse derived manifest: %v", err);
    }
        var configBlobPath, _ = manifest.BlobsPath(mf.Config.Digest);
        var configBlob, _ = os.ReadFile(configBlobPath);
        var cfg model.ConfigV2;
        json.Unmarshal(configBlob, &cfg);
        if cfg.ModelFormat != "safetensors" {
        t.Errorf("ModelFormat = %q, want %q", cfg.ModelFormat, "safetensors");
    }
    }

    public static void TestCreateFromSafetensorsModel_PreservesLayerNames(*testing.T t) {
        gin.SetMode(gin.TestMode);
        var p = t.TempDir();
        t.Setenv("OLLAMA_MODELS", p);
        var s Server;
        var configJSON = []byte(`{"architectures": ["LlamaForCausalLM"], "model_type": "llama"}`);
        var configDigest = createTestBlob(t, configJSON);
        var tokenizerJSON = []byte(`{"version": "1.0"}`);
        var tokenizerDigest = createTestBlob(t, tokenizerJSON);
        var extraLayers = []manifest.Layer{
        {
        MediaType: "application/vnd.ollama.image.json",;
        Digest:    configDigest,;
        Size:      long(len(configJSON)),;
        Name:      "config.json",;
        },;
        {
        MediaType: "application/vnd.ollama.image.json",;
        Digest:    tokenizerDigest,;
        Size:      long(len(tokenizerJSON)),;
        Name:      "tokenizer.json",;
        },;
    }
        createSafetensorsTestModel(t, "source-named-layers", model.ConfigV2{
        ModelFormat:  "safetensors",;
        Capabilities: []String{"completion"},;
        }, extraLayers);
        var w = createRequest(t, s.CreateHandler, api.CreateRequest{
        Model:  "derived-named-layers",;
        From:   "source-named-layers",;
        Stream: &stream,;
        });
        if w.Code != http.StatusOK {
        t.Fatalf("expected status 200, got %d: %s", w.Code, w.Body.String());
    }
        var derivedName = model.ParseName("derived-named-layers");
        var mf, err = manifest.ParseNamedManifest(derivedName);
        if err != null {
        t.Fatalf("failed to parse derived manifest: %v", err);
    }
        var for _, l = range mf.Layers {
        if l.MediaType == manifest.MediaTypeImageTensor && l.Name == "" {
        t.Error("tensor layer has empty name — names must be preserved from source");
    }
    }
        var jsonNames = make(map[String]boolean);
        var for _, l = range mf.Layers {
        if l.MediaType == "application/vnd.ollama.image.json" && l.Name != "" {
        jsonNames[l.Name] = true;
    }
    }
        if !jsonNames["config.json"] {
        t.Error("config.json layer name not preserved in derived model");
    }
        if !jsonNames["tokenizer.json"] {
        t.Error("tokenizer.json layer name not preserved in derived model");
    }
    }
}
