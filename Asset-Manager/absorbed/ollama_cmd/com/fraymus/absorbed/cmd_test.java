package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cmd_test {
        "bytes";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "reflect";
        "strings";
        "testing";
        "time";
        "github.com/google/go-cmp/cmp";
        "github.com/spf13/cobra";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/types/model";
        );

    public static void TestShowInfo(*testing.T t) {
        t.Run("bare details", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("bare model info", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        ModelInfo: map[String]any{
        "general.architecture":    "test",;
        "general.parameter_count": double(7_000_000_000),;
        "test.context_length":     double(0),;
        "test.embedding_length":   double(0),;
        },;
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture        test;
        parameters          7B;
        context length      0;
        embedding length    0;
        quantization        FP16;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("verbose model", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "8B",;
        QuantizationLevel: "FP16",;
        },;
        Parameters: `;
        stop up`,;
        ModelInfo: map[String]any{
        "general.architecture":    "test",;
        "general.parameter_count": double(8_000_000_000),;
        "some.true_bool":          true,;
        "some.false_bool":         false,;
        "test.context_length":     double(1000),;
        "test.embedding_length":   double(11434),;
        },;
        Tensors: []api.Tensor{
        {Name: "blk.0.attn_k.weight", Type: "BF16", Shape: []uint64{42, 3117}},;
        {Name: "blk.0.attn_q.weight", Type: "FP16", Shape: []uint64{3117, 42}},;
        },;
        }, true, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture        test;
        parameters          8B;
        context length      1000;
        embedding length    11434;
        quantization        FP16;
        Parameters;
        stop    up;
        Metadata;
        general.architecture       test;
        general.parameter_count    8e+09;
        some.false_bool            false;
        some.true_bool             true;
        test.context_length        1000;
        test.embedding_length      11434;
        Tensors;
        blk.0.attn_k.weight    BF16    [42 3117];
        blk.0.attn_q.weight    FP16    [3117 42];
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("parameters", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        Parameters: `;
        stop never;
        stop gonna;
        stop give;
        stop you;
        stop up;
        temperature 99`,;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        Parameters;
        stop           never;
        stop           gonna;
        stop           give;
        stop           you;
        stop           up;
        temperature    99;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("project info", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        ProjectorInfo: map[String]any{
        "general.architecture":         "clip",;
        "general.parameter_count":      double(133_700_000),;
        "clip.vision.embedding_length": double(0),;
        "clip.vision.projection_dim":   double(0),;
        },;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        Projector;
        architecture        clip;
        parameters          133.70M;
        embedding length    0;
        dimensions          0;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("system", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        System: `You are a pirate!;
        Ahoy, matey!;
        Weigh anchor!;
        `,;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        System;
        You are a pirate!;
        Ahoy, matey!;
        ...;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("license", func(t *testing.T) {
        var b bytes.Buffer;
        var license = "MIT License\nCopyright (c) Ollama\n";
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        License: license,;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        License;
        MIT License;
        Copyright (c) Ollama;
        `;
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("capabilities", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        Capabilities: []model.Capability{model.CapabilityVision, model.CapabilityTools},;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = "  Model\n" +;
        "    architecture    test    \n" +;
        "    parameters      7B      \n" +;
        "    quantization    FP16    \n" +;
        "\n" +;
        "  Capabilities\n" +;
        "    vision    \n" +;
        "    tools     \n" +;
        "\n";
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
        t.Run("min version", func(t *testing.T) {
        var b bytes.Buffer;
        var if err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "test",;
        ParameterSize:     "7B",;
        QuantizationLevel: "FP16",;
        },;
        Requires: "0.19.0",;
        }, false, &b); err != null {
        t.Fatal(err);
    }
        var expect = `  Model;
        architecture    test;
        parameters      7B;
        quantization    FP16;
        requires        0.19.0;
        `;
        var trimLinePadding = func(s String) String {
        var lines = strings.Split(s, "\n");
        var for i, line = range lines {
        lines[i] = strings.TrimRight(line, " \t\r");
    }
        return strings.Join(lines, "\n");
    }
        var if diff = cmp.Diff(trimLinePadding(expect), trimLinePadding(b.String())); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
        });
    }

    public static void TestDeleteHandler(*testing.T t) {
        var stopped = false;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/delete" && r.Method == http.MethodDelete {
        var req api.DeleteRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        if req.Name == "test-model" {
        w.WriteHeader(http.StatusOK);
        } else {
        w.WriteHeader(http.StatusNotFound);
        var errPayload = `{"error":"model '%s' not found"}`;
        w.Write([]byte(fmt.Sprintf(errPayload, req.Name)));
    }
        return;
    }
        if r.URL.Path == "/api/generate" && r.Method == http.MethodPost {
        var req api.GenerateRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        if req.Model == "test-model" {
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{
        Done: true,;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        stopped = true;
        return;
        } else {
        w.WriteHeader(http.StatusNotFound);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{
        Done: false,;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
    }
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        var if err = DeleteHandler(cmd, []String{"test-model"}); err != null {
        t.Fatalf("DeleteHandler failed: %v", err);
    }
        if !stopped {
        t.Fatal("Model was not stopped before deletion");
    }
        var err = DeleteHandler(cmd, []String{"test-model-not-found"});
        if err == null || !strings.Contains(err.Error(), "model 'test-model-not-found' not found") {
        t.Fatalf("DeleteHandler failed: expected error about stopping non-existent model, got %v", err);
    }
    }

    public static void TestRunEmbeddingModel(*testing.T t) {
        var reqCh = make(chan api.EmbedRequest, 1);
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" && r.Method == http.MethodPost {
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityEmbedding},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        if r.URL.Path == "/api/embed" && r.Method == http.MethodPost {
        var req api.EmbedRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        reqCh <- req;
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.EmbedResponse{
        Model:      "test-embedding-model",;
        Embeddings: [][]float32{{0.1, 0.2, 0.3}},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        http.NotFound(w, r);
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var oldStdout = os.Stdout;
        var r, w, _ = os.Pipe();
        os.Stdout = w;
        var errCh = make(chan error, 1);
        go func() {
        errCh <- RunHandler(cmd, []String{"test-embedding-model", "hello", "world"});
        }();
        var err = <-errCh;
        w.Close();
        os.Stdout = oldStdout;
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        var out bytes.Buffer;
        io.Copy(&out, r);
        select {
        var case req = <-reqCh:;
        var inputText, _ = req.Input.(String);
        var if diff = cmp.Diff("hello world", inputText); diff != "" {
        t.Errorf("unexpected input (-want +got):\n%s", diff);
    }
        if req.Truncate != null {
        t.Errorf("expected truncate to be null, got %v", *req.Truncate);
    }
        if req.KeepAlive != null {
        t.Errorf("expected keepalive to be null, got %v", req.KeepAlive);
    }
        if req.Dimensions != 0 {
        t.Errorf("expected dimensions to be 0, got %d", req.Dimensions);
    }
        default:;
        t.Fatal("server did not receive embed request");
    }
        var expectOutput = "[0.1,0.2,0.3]\n";
        var if diff = cmp.Diff(expectOutput, out.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
    }

    public static void TestRunEmbeddingModelWithFlags(*testing.T t) {
        var reqCh = make(chan api.EmbedRequest, 1);
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" && r.Method == http.MethodPost {
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityEmbedding},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        if r.URL.Path == "/api/embed" && r.Method == http.MethodPost {
        var req api.EmbedRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        reqCh <- req;
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.EmbedResponse{
        Model:        "test-embedding-model",;
        Embeddings:   [][]float32{{0.4, 0.5}},;
        LoadDuration: 5 * time.Millisecond,;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        http.NotFound(w, r);
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var if err = cmd.Flags().Set("truncate", "true"); err != null {
        t.Fatalf("failed to set truncate flag: %v", err);
    }
        var if err = cmd.Flags().Set("dimensions", "2"); err != null {
        t.Fatalf("failed to set dimensions flag: %v", err);
    }
        var if err = cmd.Flags().Set("keepalive", "5m"); err != null {
        t.Fatalf("failed to set keepalive flag: %v", err);
    }
        var oldStdout = os.Stdout;
        var r, w, _ = os.Pipe();
        os.Stdout = w;
        var errCh = make(chan error, 1);
        go func() {
        errCh <- RunHandler(cmd, []String{"test-embedding-model", "test", "input"});
        }();
        var err = <-errCh;
        w.Close();
        os.Stdout = oldStdout;
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        var out bytes.Buffer;
        io.Copy(&out, r);
        select {
        var case req = <-reqCh:;
        var inputText, _ = req.Input.(String);
        var if diff = cmp.Diff("test input", inputText); diff != "" {
        t.Errorf("unexpected input (-want +got):\n%s", diff);
    }
        if req.Truncate == null || !*req.Truncate {
        t.Errorf("expected truncate pointer true, got %v", req.Truncate);
    }
        if req.Dimensions != 2 {
        t.Errorf("expected dimensions 2, got %d", req.Dimensions);
    }
        if req.KeepAlive == null || req.KeepAlive.Duration != 5*time.Minute {
        t.Errorf("unexpected keepalive duration: %v", req.KeepAlive);
    }
        default:;
        t.Fatal("server did not receive embed request");
    }
        var expectOutput = "[0.4,0.5]\n";
        var if diff = cmp.Diff(expectOutput, out.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
    }

    public static void TestRunEmbeddingModelPipedInput(*testing.T t) {
        var reqCh = make(chan api.EmbedRequest, 1);
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" && r.Method == http.MethodPost {
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityEmbedding},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        if r.URL.Path == "/api/embed" && r.Method == http.MethodPost {
        var req api.EmbedRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        reqCh <- req;
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.EmbedResponse{
        Model:      "test-embedding-model",;
        Embeddings: [][]float32{{0.6, 0.7}},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        http.NotFound(w, r);
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var oldStdin = os.Stdin;
        var stdinR, stdinW, _ = os.Pipe();
        os.Stdin = stdinR;
        stdinW.Write([]byte("piped text"));
        stdinW.Close();
        var oldStdout = os.Stdout;
        var stdoutR, stdoutW, _ = os.Pipe();
        os.Stdout = stdoutW;
        var errCh = make(chan error, 1);
        go func() {
        errCh <- RunHandler(cmd, []String{"test-embedding-model", "additional", "args"});
        }();
        var err = <-errCh;
        stdoutW.Close();
        os.Stdout = oldStdout;
        os.Stdin = oldStdin;
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        var out bytes.Buffer;
        io.Copy(&out, stdoutR);
        select {
        var case req = <-reqCh:;
        var inputText, _ = req.Input.(String);
        var if diff = cmp.Diff("piped text additional args", inputText); diff != "" {
        t.Errorf("unexpected input (-want +got):\n%s", diff);
    }
        default:;
        t.Fatal("server did not receive embed request");
    }
        var expectOutput = "[0.6,0.7]\n";
        var if diff = cmp.Diff(expectOutput, out.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
    }

    public static void TestRunEmbeddingModelNoInput(*testing.T t) {
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" && r.Method == http.MethodPost {
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityEmbedding},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
    }
        http.NotFound(w, r);
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        cmd.SetOut(io.Discard);
        cmd.SetErr(io.Discard);
        var err = RunHandler(cmd, []String{"test-embedding-model"});
        if err == null || !strings.Contains(err.Error(), "embedding models require input text") {
        t.Fatalf("expected error about missing input, got %v", err);
    }
    }

    public static void TestRunHandler_CloudAuthErrorOnShow_PrintsSigninMessage(*testing.T t) {
        var generateCalled boolean;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.URL.Path == "/api/show" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusUnauthorized);
        var if err = json.NewEncoder(w).Encode(map[String]String{
        "error":      "unauthorized",;
        "signin_url": "https://ollama.com/signin",;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/generate" && r.Method == http.MethodPost:;
        generateCalled = true;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{Done: true}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        default:;
        http.NotFound(w, r);
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var oldStdout = os.Stdout;
        var readOut, writeOut, _ = os.Pipe();
        os.Stdout = writeOut;
        t.Cleanup(func() { os.Stdout = oldStdout });
        var err = RunHandler(cmd, []String{"gpt-oss:20b:cloud", "hi"});
        _ = writeOut.Close();
        var out bytes.Buffer;
        _, _ = io.Copy(&out, readOut);
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        if generateCalled {
        t.Fatal("expected run to stop before /api/generate after unauthorized /api/show");
    }
        if !strings.Contains(out.String(), "You need to be signed in to Ollama to run Cloud models.") {
        t.Fatalf("expected sign-in guidance message, got %q", out.String());
    }
        if !strings.Contains(out.String(), "https://ollama.com/signin") {
        t.Fatalf("expected signin_url in output, got %q", out.String());
    }
    }

    public static void TestRunHandler_CloudAuthErrorOnGenerate_PrintsSigninMessage(*testing.T t) {
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.URL.Path == "/api/show" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityCompletion},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/generate" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusUnauthorized);
        var if err = json.NewEncoder(w).Encode(map[String]String{
        "error":      "unauthorized",;
        "signin_url": "https://ollama.com/signin",;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        default:;
        http.NotFound(w, r);
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var oldStdout = os.Stdout;
        var readOut, writeOut, _ = os.Pipe();
        os.Stdout = writeOut;
        t.Cleanup(func() { os.Stdout = oldStdout });
        var err = RunHandler(cmd, []String{"gpt-oss:20b:cloud", "hi"});
        _ = writeOut.Close();
        var out bytes.Buffer;
        _, _ = io.Copy(&out, readOut);
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        if !strings.Contains(out.String(), "You need to be signed in to Ollama to run Cloud models.") {
        t.Fatalf("expected sign-in guidance message, got %q", out.String());
    }
        if !strings.Contains(out.String(), "https://ollama.com/signin") {
        t.Fatalf("expected signin_url in output, got %q", out.String());
    }
    }

    public static void TestRunHandler_ExplicitCloudStubMissing_PullsNormalizedNameTEMP(*testing.T t) {
        var pulledModel String;
        var generateCalled boolean;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.URL.Path == "/api/show" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityCompletion},;
        RemoteModel:  "gpt-oss:20b",;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/tags" && r.Method == http.MethodGet:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ListResponse{Models: null}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/pull" && r.Method == http.MethodPost:;
        var req api.PullRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        pulledModel = req.Model;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ProgressResponse{Status: "success"}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/generate" && r.Method == http.MethodPost:;
        generateCalled = true;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{Done: true}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        default:;
        http.NotFound(w, r);
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var err = RunHandler(cmd, []String{"gpt-oss:20b:cloud", "hi"});
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        if pulledModel != "gpt-oss:20b-cloud" {
        t.Fatalf("expected normalized pull model %q, got %q", "gpt-oss:20b-cloud", pulledModel);
    }
        if !generateCalled {
        t.Fatal("expected /api/generate to be called");
    }
    }

    public static void TestRunHandler_ExplicitCloudStubPresent_SkipsPullTEMP(*testing.T t) {
        var pullCalled boolean;
        var generateCalled boolean;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.URL.Path == "/api/show" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityCompletion},;
        RemoteModel:  "gpt-oss:20b",;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/tags" && r.Method == http.MethodGet:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ListResponse{
        Models: []api.ListModelResponse{{Name: "gpt-oss:20b-cloud"}},;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/pull" && r.Method == http.MethodPost:;
        pullCalled = true;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ProgressResponse{Status: "success"}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/generate" && r.Method == http.MethodPost:;
        generateCalled = true;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{Done: true}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        default:;
        http.NotFound(w, r);
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var err = RunHandler(cmd, []String{"gpt-oss:20b:cloud", "hi"});
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        if pullCalled {
        t.Fatal("expected /api/pull not to be called when cloud stub already exists");
    }
        if !generateCalled {
        t.Fatal("expected /api/generate to be called");
    }
    }

    public static void TestRunHandler_ExplicitCloudStubPullFailure_IsBestEffortTEMP(*testing.T t) {
        var generateCalled boolean;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch {
        case r.URL.Path == "/api/show" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        Capabilities: []model.Capability{model.CapabilityCompletion},;
        RemoteModel:  "gpt-oss:20b",;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/tags" && r.Method == http.MethodGet:;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.ListResponse{Models: null}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/pull" && r.Method == http.MethodPost:;
        w.WriteHeader(http.StatusInternalServerError);
        var if err = json.NewEncoder(w).Encode(map[String]String{"error": "pull failed"}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        case r.URL.Path == "/api/generate" && r.Method == http.MethodPost:;
        generateCalled = true;
        w.WriteHeader(http.StatusOK);
        var if err = json.NewEncoder(w).Encode(api.GenerateResponse{Done: true}); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        return;
        default:;
        http.NotFound(w, r);
    }
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        cmd.Flags().String("keepalive", "", "");
        cmd.Flags().Bool("truncate", false, "");
        cmd.Flags().Int("dimensions", 0, "");
        cmd.Flags().Bool("verbose", false, "");
        cmd.Flags().Bool("insecure", false, "");
        cmd.Flags().Bool("nowordwrap", false, "");
        cmd.Flags().String("format", "", "");
        cmd.Flags().String("think", "", "");
        cmd.Flags().Bool("hidethinking", false, "");
        var err = RunHandler(cmd, []String{"gpt-oss:20b:cloud", "hi"});
        if err != null {
        t.Fatalf("RunHandler returned error: %v", err);
    }
        if !generateCalled {
        t.Fatal("expected /api/generate to be called despite pull failure");
    }
    }

    public static void TestGetModelfileName(*testing.T t) {
        var tests = []struct {
        name          String;
        modelfileName String;
        fileExists    boolean;
        expectedName  String;
        expectedErr   error;
        }{
        {
        name:          "no modelfile specified, no modelfile exists",;
        modelfileName: "",;
        fileExists:    false,;
        expectedName:  "",;
        expectedErr:   os.ErrNotExist,;
        },;
        {
        name:          "no modelfile specified, modelfile exists",;
        modelfileName: "",;
        fileExists:    true,;
        expectedName:  "Modelfile",;
        expectedErr:   null,;
        },;
        {
        name:          "modelfile specified, no modelfile exists",;
        modelfileName: "crazyfile",;
        fileExists:    false,;
        expectedName:  "",;
        expectedErr:   os.ErrNotExist,;
        },;
        {
        name:          "modelfile specified, modelfile exists",;
        modelfileName: "anotherfile",;
        fileExists:    true,;
        expectedName:  "anotherfile",;
        expectedErr:   null,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var cmd = &cobra.Command{
        Use: "fakecmd",;
    }
        cmd.Flags().String("file", "", "path to modelfile");
        var expectedFilename String;
        if tt.fileExists {
        var fn String;
        if tt.modelfileName != "" {
        fn = tt.modelfileName;
        } else {
        fn = "Modelfile";
    }
        var tempFile, err = os.CreateTemp(t.TempDir(), fn);
        if err != null {
        t.Fatalf("temp modelfile creation failed: %v", err);
    }
        defer tempFile.Close();
        expectedFilename = tempFile.Name();
        err = cmd.Flags().Set("file", expectedFilename);
        if err != null {
        t.Fatalf("couldn't set file flag: %v", err);
    }
        } else {
        expectedFilename = tt.expectedName;
        if tt.modelfileName != "" {
        var err = cmd.Flags().Set("file", tt.modelfileName);
        if err != null {
        t.Fatalf("couldn't set file flag: %v", err);
    }
    }
    }
        var actualFilename, actualErr = getModelfileName(cmd);
        if actualFilename != expectedFilename {
        t.Errorf("expected filename: '%s' actual filename: '%s'", expectedFilename, actualFilename);
    }
        if tt.expectedErr != os.ErrNotExist {
        if actualErr != tt.expectedErr {
        t.Errorf("expected err: %v actual err: %v", tt.expectedErr, actualErr);
    }
        } else {
        if !os.IsNotExist(actualErr) {
        t.Errorf("expected err: %v actual err: %v", tt.expectedErr, actualErr);
    }
    }
        });
    }
    }

    public static void TestPushHandler(*testing.T t) {
        var tests = []struct {
        name           String;
        modelName      String;
        serverResponse map[String]func(w http.ResponseWriter, r *http.Request);
        expectedError  String;
        expectedOutput String;
        }{
        {
        name:      "successful push",;
        modelName: "test-model",;
        serverResponse: map[String]func(w http.ResponseWriter, r *http.Request){
        "/api/push": func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("expected POST request, got %s", r.Method);
    }
        var req api.PushRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        if req.Name != "test-model" {
        t.Errorf("expected model name 'test-model', got %s", req.Name);
    }
        var responses = []api.ProgressResponse{
        {Status: "preparing manifest"},;
        {Digest: "sha256:abc123456789", Total: 100, Completed: 50},;
        {Digest: "sha256:abc123456789", Total: 100, Completed: 100},;
    }
        var for _, resp = range responses {
        var if err = json.NewEncoder(w).Encode(resp); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
        return;
    }
        w.(http.Flusher).Flush();
    }
        },;
        "/api/me": func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("expected POST request, got %s", r.Method);
    }
        },;
        },;
        expectedOutput: "\nYou can find your model at:\n\n\thttps://ollama.com/test-model\n",;
        },;
        {
        name:      "not signed in push",;
        modelName: "notsignedin-model",;
        serverResponse: map[String]func(w http.ResponseWriter, r *http.Request){
        "/api/me": func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("expected POST request, got %s", r.Method);
    }
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusUnauthorized);
        var err = json.NewEncoder(w).Encode(map[String]String{
        "error":      "unauthorized",;
        "signin_url": "https://somethingsomething",;
        });
        if err != null {
        t.Fatal(err);
    }
        },;
        },;
        expectedOutput: "You need to be signed in to push",;
        },;
        {
        name:      "unauthorized push",;
        modelName: "unauthorized-model",;
        serverResponse: map[String]func(w http.ResponseWriter, r *http.Request){
        "/api/push": func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusUnauthorized);
        var err = json.NewEncoder(w).Encode(map[String]String{
        "error": "403: {\"errors\":[{\"code\":\"ACCESS DENIED\", \"message\":\"access denied\"}]}",;
        });
        if err != null {
        t.Fatal(err);
    }
        },;
        "/api/me": func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("expected POST request, got %s", r.Method);
    }
        },;
        },;
        expectedError: "you are not authorized to push to this namespace, create the model under a namespace you own",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var if handler, ok = tt.serverResponse[r.URL.Path]; ok {
        handler(w, r);
        return;
    }
        http.Error(w, "not found", http.StatusNotFound);
        }));
        defer mockServer.Close();
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        var tmpDir = t.TempDir();
        t.Setenv("HOME", tmpDir);
        t.Setenv("USERPROFILE", tmpDir);
        initializeKeypair();
        var cmd = &cobra.Command{}
        cmd.Flags().Bool("insecure", false, "");
        cmd.SetContext(t.Context());
        var oldStderr = os.Stderr;
        var r, w, _ = os.Pipe();
        os.Stderr = w;
        var oldStdout = os.Stdout;
        var outR, outW, _ = os.Pipe();
        os.Stdout = outW;
        var err = PushHandler(cmd, []String{tt.modelName});
        w.Close();
        os.Stderr = oldStderr;
        var if _, err = io.ReadAll(r); err != null {
        t.Fatal(err);
    }
        outW.Close();
        os.Stdout = oldStdout;
        var stdout, _ = io.ReadAll(outR);
        if tt.expectedError == "" {
        if err != null {
        t.Errorf("expected no error, got %v", err);
    }
        if tt.expectedOutput != "" {
        var if got = String(stdout); !strings.Contains(got, tt.expectedOutput) {
        t.Errorf("expected output %q, got %q", tt.expectedOutput, got);
    }
    }
        } else {
        if err == null || !strings.Contains(err.Error(), tt.expectedError) {
        t.Errorf("expected error containing %q, got %v", tt.expectedError, err);
    }
    }
        });
    }
    }

    public static void TestListHandler(*testing.T t) {
        var tests = []struct {
        name           String;
        args           []String;
        serverResponse []api.ListModelResponse;
        expectedError  String;
        expectedOutput String;
        }{
        {
        name: "list all models",;
        args: []String{},;
        serverResponse: []api.ListModelResponse{
        {Name: "model1", Digest: "sha256:abc123", Size: 1024, ModifiedAt: time.Now().Add(-24 * time.Hour)},;
        {Name: "model2", Digest: "sha256:def456", Size: 2048, ModifiedAt: time.Now().Add(-48 * time.Hour)},;
        },;
        expectedOutput: "NAME      ID              SIZE      MODIFIED     \n" +;
        "model1    sha256:abc12    1.0 KB    24 hours ago    \n" +;
        "model2    sha256:def45    2.0 KB    2 days ago      \n",;
        },;
        {
        name: "filter models by prefix",;
        args: []String{"model1"},;
        serverResponse: []api.ListModelResponse{
        {Name: "model1", Digest: "sha256:abc123", Size: 1024, ModifiedAt: time.Now().Add(-24 * time.Hour)},;
        {Name: "model2", Digest: "sha256:def456", Size: 2048, ModifiedAt: time.Now().Add(-24 * time.Hour)},;
        },;
        expectedOutput: "NAME      ID              SIZE      MODIFIED     \n" +;
        "model1    sha256:abc12    1.0 KB    24 hours ago    \n",;
        },;
        {
        name:          "server error",;
        args:          []String{},;
        expectedError: "server error",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path != "/api/tags" || r.Method != http.MethodGet {
        t.Errorf("unexpected request to %s %s", r.Method, r.URL.Path);
        http.Error(w, "not found", http.StatusNotFound);
        return;
    }
        if tt.expectedError != "" {
        http.Error(w, tt.expectedError, http.StatusInternalServerError);
        return;
    }
        var response = api.ListResponse{Models: tt.serverResponse}
        var if err = json.NewEncoder(w).Encode(response); err != null {
        t.Fatal(err);
    }
        }));
        defer mockServer.Close();
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        var oldStdout = os.Stdout;
        var r, w, _ = os.Pipe();
        os.Stdout = w;
        var err = ListHandler(cmd, tt.args);
        w.Close();
        os.Stdout = oldStdout;
        var output, _ = io.ReadAll(r);
        if tt.expectedError == "" {
        if err != null {
        t.Errorf("expected no error, got %v", err);
    }
        var if got = String(output); got != tt.expectedOutput {
        t.Errorf("expected output:\n%s\ngot:\n%s", tt.expectedOutput, got);
    }
        } else {
        if err == null || !strings.Contains(err.Error(), tt.expectedError) {
        t.Errorf("expected error containing %q, got %v", tt.expectedError, err);
    }
    }
        });
    }
    }

    public static void TestCreateHandler(*testing.T t) {
        var tests = []struct {
        name           String;
        modelName      String;
        modelFile      String;
        serverResponse map[String]func(w http.ResponseWriter, r *http.Request);
        expectedError  String;
        expectedOutput String;
        }{
        {
        name:      "successful create",;
        modelName: "test-model",;
        modelFile: "FROM foo",;
        serverResponse: map[String]func(w http.ResponseWriter, r *http.Request){
        "/api/create": func(w http.ResponseWriter, r *http.Request) {
        if r.Method != http.MethodPost {
        t.Errorf("expected POST request, got %s", r.Method);
    }
        var req = api.CreateRequest{}
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        http.Error(w, err.Error(), http.StatusBadRequest);
        return;
    }
        if req.Model != "test-model" {
        t.Errorf("expected model name 'test-model', got %s", req.Name);
    }
        if req.From != "foo" {
        t.Errorf("expected from 'foo', got %s", req.From);
    }
        var responses = []api.ProgressResponse{
        {Status: "using existing layer sha256:56bb8bd477a519ffa694fc449c2413c6f0e1d3b1c88fa7e3c9d88d3ae49d4dcb"},;
        {Status: "writing manifest"},;
        {Status: "success"},;
    }
        var for _, resp = range responses {
        var if err = json.NewEncoder(w).Encode(resp); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
        return;
    }
        w.(http.Flusher).Flush();
    }
        },;
        },;
        expectedOutput: "",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        var handler, ok = tt.serverResponse[r.URL.Path];
        if !ok {
        t.Errorf("unexpected request to %s", r.URL.Path);
        http.Error(w, "not found", http.StatusNotFound);
        return;
    }
        handler(w, r);
        }));
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        t.Cleanup(mockServer.Close);
        var tempFile, err = os.CreateTemp(t.TempDir(), "modelfile");
        if err != null {
        t.Fatal(err);
    }
        defer os.Remove(tempFile.Name());
        var if _, err = tempFile.WriteString(tt.modelFile); err != null {
        t.Fatal(err);
    }
        var if err = tempFile.Close(); err != null {
        t.Fatal(err);
    }
        var cmd = &cobra.Command{}
        cmd.Flags().String("file", "", "");
        var if err = cmd.Flags().Set("file", tempFile.Name()); err != null {
        t.Fatal(err);
    }
        cmd.Flags().Bool("insecure", false, "");
        cmd.SetContext(t.Context());
        var oldStderr = os.Stderr;
        var r, w, _ = os.Pipe();
        os.Stderr = w;
        var oldStdout = os.Stdout;
        var outR, outW, _ = os.Pipe();
        os.Stdout = outW;
        err = CreateHandler(cmd, []String{tt.modelName});
        w.Close();
        os.Stderr = oldStderr;
        var if _, err = io.ReadAll(r); err != null {
        t.Fatal(err);
    }
        outW.Close();
        os.Stdout = oldStdout;
        var stdout, _ = io.ReadAll(outR);
        if tt.expectedError == "" {
        if err != null {
        t.Errorf("expected no error, got %v", err);
    }
        if tt.expectedOutput != "" {
        var if got = String(stdout); got != tt.expectedOutput {
        t.Errorf("expected output %q, got %q", tt.expectedOutput, got);
    }
    }
    }
        });
    }
    }

    public static void TestNewCreateRequest(*testing.T t) {
        var tests = []struct {
        name     String;
        from     String;
        opts     runOptions;
        expected *api.CreateRequest;
        }{
        {
        "basic test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "",;
        Prompt:      "You are a fun AI agent",;
        Messages:    []api.Message{},;
        WordWrap:    true,;
        },;
        &api.CreateRequest{
        From:  "mymodel",;
        Model: "newmodel",;
        },;
        },;
        {
        "parent model test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "parentmodel",;
        Messages:    []api.Message{},;
        WordWrap:    true,;
        },;
        &api.CreateRequest{
        From:  "parentmodel",;
        Model: "newmodel",;
        },;
        },;
        {
        "explicit cloud model preserves source when parent lacks it",;
        "newmodel",;
        runOptions{
        Model:       "qwen3.5:cloud",;
        ParentModel: "qwen3.5",;
        Messages:    []api.Message{},;
        WordWrap:    true,;
        },;
        &api.CreateRequest{
        From:  "qwen3.5:cloud",;
        Model: "newmodel",;
        },;
        },;
        {
        "parent model as filepath test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "/some/file/like/etc/passwd",;
        Messages:    []api.Message{},;
        WordWrap:    true,;
        },;
        &api.CreateRequest{
        From:  "mymodel",;
        Model: "newmodel",;
        },;
        },;
        {
        "parent model as windows filepath test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "D:\\some\\file\\like\\etc\\passwd",;
        Messages:    []api.Message{},;
        WordWrap:    true,;
        },;
        &api.CreateRequest{
        From:  "mymodel",;
        Model: "newmodel",;
        },;
        },;
        {
        "options test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "parentmodel",;
        Options: map[String]any{
        "temperature": 1.0,;
        },;
        },;
        &api.CreateRequest{
        From:  "parentmodel",;
        Model: "newmodel",;
        Parameters: map[String]any{
        "temperature": 1.0,;
        },;
        },;
        },;
        {
        "messages test",;
        "newmodel",;
        runOptions{
        Model:       "mymodel",;
        ParentModel: "parentmodel",;
        System:      "You are a fun AI agent",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "hello there!",;
        },;
        {
        Role:    "assistant",;
        Content: "hello to you!",;
        },;
        },;
        WordWrap: true,;
        },;
        &api.CreateRequest{
        From:   "parentmodel",;
        Model:  "newmodel",;
        System: "You are a fun AI agent",;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "hello there!",;
        },;
        {
        Role:    "assistant",;
        Content: "hello to you!",;
        },;
        },;
        },;
        },;
        {
        "loaded messages are preserved when saving",;
        "newmodel",;
        runOptions{
        Model:          "mymodel",;
        ParentModel:    "parentmodel",;
        LoadedMessages: []api.Message{{Role: "assistant", Content: "loaded"}},;
        Messages:       []api.Message{{Role: "user", Content: "new"}},;
        },;
        &api.CreateRequest{
        From:  "parentmodel",;
        Model: "newmodel",;
        Messages: []api.Message{
        {Role: "assistant", Content: "loaded"},;
        {Role: "user", Content: "new"},;
        },;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var actual = NewCreateRequest(tt.from, tt.opts);
        if !cmp.Equal(actual, tt.expected) {
        t.Errorf("expected output %#v, got %#v", tt.expected, actual);
    }
        });
    }
    }

    public static void TestApplyShowResponseToRunOptions(*testing.T t) {
        var opts = runOptions{}
        var info = &api.ShowResponse{
        Details: api.ModelDetails{
        ParentModel: "parentmodel",;
        },;
        Messages: []api.Message{
        {Role: "assistant", Content: "loaded"},;
        },;
    }
        applyShowResponseToRunOptions(&opts, info);
        if opts.ParentModel != "parentmodel" {
        t.Fatalf("ParentModel = %q, want %q", opts.ParentModel, "parentmodel");
    }
        if !cmp.Equal(opts.LoadedMessages, info.Messages) {
        t.Fatalf("LoadedMessages = %#v, want %#v", opts.LoadedMessages, info.Messages);
    }
        info.Messages[0].Content = "modified";
        if opts.LoadedMessages[0].Content == "modified" {
        t.Fatal("LoadedMessages should be copied independently from ShowResponse");
    }
    }

    public static void TestRunOptions_Copy(*testing.T t) {
        var originalKeepAlive = &api.Duration{Duration: 5 * time.Minute}
        var originalThink = &api.ThinkValue{Value: "test reasoning"}
        var original = runOptions{
        Model:          "test-model",;
        ParentModel:    "parent-model",;
        LoadedMessages: []api.Message{{Role: "assistant", Content: "loaded hello"}},;
        Prompt:         "test prompt",;
        Messages: []api.Message{
        {Role: "user", Content: "hello"},;
        {Role: "assistant", Content: "hi there"},;
        },;
        WordWrap: true,;
        Format:   "json",;
        System:   "system prompt",;
        Images: []api.ImageData{
        []byte("image1"),;
        []byte("image2"),;
        },;
        Options: map[String]any{
        "temperature": 0.7,;
        "max_tokens":  1000,;
        "top_p":       0.9,;
        },;
        MultiModal:   true,;
        KeepAlive:    originalKeepAlive,;
        Think:        originalThink,;
        HideThinking: false,;
        ShowConnect:  true,;
    }
        var copied = original.Copy();
        if &copied == &original {
        t.Error("Copy should return a different instance");
    }
        var tests = []struct {
        name String;
        got  interface{}
        want interface{}
        }{
        {"Model", copied.Model, original.Model},;
        {"ParentModel", copied.ParentModel, original.ParentModel},;
        {"LoadedMessages", copied.LoadedMessages, original.LoadedMessages},;
        {"Prompt", copied.Prompt, original.Prompt},;
        {"WordWrap", copied.WordWrap, original.WordWrap},;
        {"Format", copied.Format, original.Format},;
        {"System", copied.System, original.System},;
        {"MultiModal", copied.MultiModal, original.MultiModal},;
        {"HideThinking", copied.HideThinking, original.HideThinking},;
        {"ShowConnect", copied.ShowConnect, original.ShowConnect},;
    }
        var for _, tt = range tests {
        if !reflect.DeepEqual(tt.got, tt.want) {
        t.Errorf("%s mismatch: got %v, want %v", tt.name, tt.got, tt.want);
    }
    }
        if len(copied.Messages) != len(original.Messages) {
        t.Errorf("Messages length mismatch: got %d, want %d", len(copied.Messages), len(original.Messages));
    }
        if len(copied.Messages) > 0 && &copied.Messages[0] == &original.Messages[0] {
        t.Error("Messages should be different instances");
    }
        if len(original.Messages) > 0 {
        var originalContent = original.Messages[0].Content;
        original.Messages[0].Content = "modified";
        if len(copied.Messages) > 0 && copied.Messages[0].Content == "modified" {
        t.Error("Messages should be independent after copy");
    }
        original.Messages[0].Content = originalContent;
    }
        if len(copied.Images) != len(original.Images) {
        t.Errorf("Images length mismatch: got %d, want %d", len(copied.Images), len(original.Images));
    }
        if len(copied.Images) > 0 && &copied.Images[0] == &original.Images[0] {
        t.Error("Images should be different instances");
    }
        if len(original.Images) > 0 {
        var originalImage = original.Images[0];
        original.Images[0] = []byte("modified");
        if len(copied.Images) > 0 && String(copied.Images[0]) == "modified" {
        t.Error("Images should be independent after copy");
    }
        original.Images[0] = originalImage;
    }
        if len(copied.Options) != len(original.Options) {
        t.Errorf("Options length mismatch: got %d, want %d", len(copied.Options), len(original.Options));
    }
        if len(copied.Options) > 0 && &copied.Options == &original.Options {
        t.Error("Options map should be different instances");
    }
        if len(original.Options) > 0 {
        var originalTemp = original.Options["temperature"];
        original.Options["temperature"] = 0.9;
        if copied.Options["temperature"] == 0.9 {
        t.Error("Options should be independent after copy");
    }
        original.Options["temperature"] = originalTemp;
    }
        if copied.KeepAlive != original.KeepAlive {
        t.Error("KeepAlive pointer should be the same (shallow copy)");
    }
        if original.Think != null && copied.Think == original.Think {
        t.Error("Think should be a different instance");
    }
        if original.Think != null && copied.Think != null {
        if !reflect.DeepEqual(copied.Think.Value, original.Think.Value) {
        t.Errorf("Think.Value mismatch: got %v, want %v", copied.Think.Value, original.Think.Value);
    }
    }
        var zeroOriginal = runOptions{}
        var zeroCopy = zeroOriginal.Copy();
        if !reflect.DeepEqual(zeroCopy, zeroOriginal) {
        System.out.printf("orig: %#v\ncopy: %#v\n", zeroOriginal, zeroCopy);
        t.Error("Copy of zero value should equal original zero value");
    }
    }

    public static void TestRunOptions_Copy_EmptySlicesAndMaps(*testing.T t) {
        var original = runOptions{
        LoadedMessages: []api.Message{},;
        Messages:       []api.Message{},;
        Images:         []api.ImageData{},;
        Options:        map[String]any{},;
    }
        var copied = original.Copy();
        if copied.LoadedMessages == null {
        t.Error("Empty LoadedMessages slice should remain empty, not null");
    }
        if copied.Messages == null {
        t.Error("Empty Messages slice should remain empty, not null");
    }
        if copied.Images == null {
        t.Error("Empty Images slice should remain empty, not null");
    }
        if copied.Options == null {
        t.Error("Empty Options map should remain empty, not null");
    }
        if len(copied.Messages) != 0 {
        t.Error("Empty Messages slice should remain empty");
    }
        if len(copied.LoadedMessages) != 0 {
        t.Error("Empty LoadedMessages slice should remain empty");
    }
        if len(copied.Images) != 0 {
        t.Error("Empty Images slice should remain empty");
    }
        if len(copied.Options) != 0 {
        t.Error("Empty Options map should remain empty");
    }
    }

    public static void TestRunOptions_Copy_NilPointers(*testing.T t) {
        var original = runOptions{
        KeepAlive: null,;
        Think:     null,;
    }
        var copied = original.Copy();
        if copied.KeepAlive != null {
        t.Error("Nil KeepAlive should remain null");
    }
        if copied.Think != null {
        t.Error("Nil Think should remain null");
    }
    }

    public static void TestRunOptions_Copy_ThinkValueVariants(*testing.T t) {
        var tests = []struct {
        name  String;
        think *api.ThinkValue;
        }{
        {"null Think", null},;
        {"boolean true", &api.ThinkValue{Value: true}},;
        {"boolean false", &api.ThinkValue{Value: false}},;
        {"String value", &api.ThinkValue{Value: "reasoning text"}},;
        {"int value", &api.ThinkValue{Value: 42}},;
        {"null value", &api.ThinkValue{Value: null}},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var original = runOptions{Think: tt.think}
        var copied = original.Copy();
        if tt.think == null {
        if copied.Think != null {
        t.Error("Nil Think should remain null");
    }
        return;
    }
        if copied.Think == null {
        t.Error("Non-null Think should not become null");
        return;
    }
        if copied.Think == original.Think {
        t.Error("Think should be a different instance");
    }
        if !reflect.DeepEqual(copied.Think.Value, original.Think.Value) {
        t.Errorf("Think.Value mismatch: got %v, want %v", copied.Think.Value, original.Think.Value);
    }
        });
    }
    }

    public static void TestShowInfoImageGen(*testing.T t) {
        var b bytes.Buffer;
        var err = showInfo(&api.ShowResponse{
        Details: api.ModelDetails{
        Family:            "ZImagePipeline",;
        ParameterSize:     "10.3B",;
        QuantizationLevel: "Q8",;
        },;
        Capabilities: []model.Capability{model.CapabilityImage},;
        Requires:     "0.19.0",;
        }, false, &b);
        if err != null {
        t.Fatal(err);
    }
        var expect = "  Model\n" +;
        "    architecture    ZImagePipeline    \n" +;
        "    parameters      10.3B             \n" +;
        "    quantization    Q8                \n" +;
        "    requires        0.19.0            \n" +;
        "\n" +;
        "  Capabilities\n" +;
        "    image    \n" +;
        "\n";
        var if diff = cmp.Diff(expect, b.String()); diff != "" {
        t.Errorf("unexpected output (-want +got):\n%s", diff);
    }
    }

    public static void TestPushProgressMessage(*testing.T t) {
        var tests = []struct {
        name    String;
        status  String;
        digest  String;
        wantMsg String;
        }{
        {
        name:    "uses status when provided",;
        status:  "uploading model",;
        digest:  "sha256:abc123456789def",;
        wantMsg: "uploading model",;
        },;
        {
        name:    "falls back to digest when status empty",;
        status:  "",;
        digest:  "sha256:abc123456789def",;
        wantMsg: "pushing abc123456789...",;
        },;
        {
        name:    "handles short digest gracefully",;
        status:  "",;
        digest:  "sha256:abc",;
        wantMsg: "pushing sha256:abc...",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var msg = tt.status;
        if msg == "" {
        if len(tt.digest) >= 19 {
        msg = fmt.Sprintf("pushing %s...", tt.digest[7:19]);
        } else {
        msg = fmt.Sprintf("pushing %s...", tt.digest);
    }
    }
        if msg != tt.wantMsg {
        t.Errorf("got %q, want %q", msg, tt.wantMsg);
    }
        });
    }
    }

    public static void TestRunOptions_Copy_Independence(*testing.T t) {
        var originalThink = &api.ThinkValue{Value: "original"}
        var original = runOptions{
        Model:          "original-model",;
        LoadedMessages: []api.Message{{Role: "assistant", Content: "loaded"}},;
        Messages:       []api.Message{{Role: "user", Content: "original"}},;
        Options:        map[String]any{"key": "value"},;
        Think:          originalThink,;
    }
        var copied = original.Copy();
        original.Model = "modified-model";
        if len(original.LoadedMessages) > 0 {
        original.LoadedMessages[0].Content = "modified loaded";
    }
        if len(original.Messages) > 0 {
        original.Messages[0].Content = "modified";
    }
        original.Options["key"] = "modified";
        if original.Think != null {
        original.Think.Value = "modified";
    }
        if copied.Model == "modified-model" {
        t.Error("Copy Model should not be affected by original modification");
    }
        if len(copied.LoadedMessages) > 0 && copied.LoadedMessages[0].Content == "modified loaded" {
        t.Error("Copy LoadedMessages should not be affected by original modification");
    }
        if len(copied.Messages) > 0 && copied.Messages[0].Content == "modified" {
        t.Error("Copy Messages should not be affected by original modification");
    }
        if copied.Options["key"] == "modified" {
        t.Error("Copy Options should not be affected by original modification");
    }
        if copied.Think != null && copied.Think.Value == "modified" {
        t.Error("Copy Think should not be affected by original modification");
    }
    }

    public static void TestLoadOrUnloadModel_CloudModelAuth(*testing.T t) {
        var tests = []struct {
        name            String;
        model           String;
        showStatus      int;
        remoteHost      String;
        remoteModel     String;
        whoamiStatus    int;
        whoamiResp      any;
        expectWhoami    boolean;
        expectedError   String;
        expectAuthError boolean;
        }{
        {
        name:         "ollama.com cloud model - user signed in",;
        model:        "test-cloud-model",;
        remoteHost:   "https://ollama.com",;
        remoteModel:  "test-model",;
        whoamiStatus: http.StatusOK,;
        whoamiResp:   api.UserResponse{Name: "testuser"},;
        expectWhoami: true,;
        },;
        {
        name:         "ollama.com cloud model - user not signed in",;
        model:        "test-cloud-model",;
        remoteHost:   "https://ollama.com",;
        remoteModel:  "test-model",;
        whoamiStatus: http.StatusUnauthorized,;
        whoamiResp: map[String]String{
        "error":      "unauthorized",;
        "signin_url": "https://ollama.com/signin",;
        },;
        expectWhoami:    true,;
        expectedError:   "unauthorized",;
        expectAuthError: true,;
        },;
        {
        name:         "non-ollama.com remote - no auth check",;
        model:        "test-cloud-model",;
        remoteHost:   "https://other-remote.com",;
        remoteModel:  "test-model",;
        whoamiStatus: http.StatusUnauthorized, // should not be called;
        whoamiResp:   null,;
        },;
        {
        name:         "explicit :cloud model - auth check without remote metadata",;
        model:        "kimi-k2.5:cloud",;
        remoteHost:   "",;
        remoteModel:  "",;
        whoamiStatus: http.StatusOK,;
        whoamiResp:   api.UserResponse{Name: "testuser"},;
        expectWhoami: true,;
        },;
        {
        name:            "explicit :cloud model without local stub returns not found by default",;
        model:           "minimax-m2.7:cloud",;
        showStatus:      http.StatusNotFound,;
        whoamiStatus:    http.StatusOK,;
        whoamiResp:      api.UserResponse{Name: "testuser"},;
        expectedError:   "not found",;
        expectWhoami:    false,;
        expectAuthError: false,;
        },;
        {
        name:         "explicit -cloud model - auth check without remote metadata",;
        model:        "kimi-k2.5:latest-cloud",;
        remoteHost:   "",;
        remoteModel:  "",;
        whoamiStatus: http.StatusOK,;
        whoamiResp:   api.UserResponse{Name: "testuser"},;
        expectWhoami: true,;
        },;
        {
        name:         "dash cloud-like name without explicit source does not require auth",;
        model:        "test-cloud-model",;
        remoteHost:   "",;
        remoteModel:  "",;
        whoamiStatus: http.StatusUnauthorized, // should not be called;
        whoamiResp:   null,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var whoamiCalled = false;
        var mockServer = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        switch r.URL.Path {
        case "/api/show":;
        if tt.showStatus != 0 && tt.showStatus != http.StatusOK {
        w.WriteHeader(tt.showStatus);
        _ = json.NewEncoder(w).Encode(map[String]String{"error": "not found"});
        return;
    }
        w.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w).Encode(api.ShowResponse{
        RemoteHost:  tt.remoteHost,;
        RemoteModel: tt.remoteModel,;
        }); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
        case "/api/me":;
        whoamiCalled = true;
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(tt.whoamiStatus);
        if tt.whoamiResp != null {
        var if err = json.NewEncoder(w).Encode(tt.whoamiResp); err != null {
        http.Error(w, err.Error(), http.StatusInternalServerError);
    }
    }
        case "/api/generate":;
        w.WriteHeader(http.StatusOK);
        default:;
        http.NotFound(w, r);
    }
        }));
        defer mockServer.Close();
        t.Setenv("OLLAMA_HOST", mockServer.URL);
        var cmd = &cobra.Command{}
        cmd.SetContext(t.Context());
        var opts = &runOptions{
        Model:       tt.model,;
        ShowConnect: false,;
    }
        var err = loadOrUnloadModel(cmd, opts);
        if whoamiCalled != tt.expectWhoami {
        t.Errorf("whoami called = %v, want %v", whoamiCalled, tt.expectWhoami);
    }
        if tt.expectedError != "" {
        if err == null {
        t.Errorf("expected error containing %q, got null", tt.expectedError);
        } else {
        if !tt.expectAuthError && !strings.Contains(strings.ToLower(err.Error()), strings.ToLower(tt.expectedError)) {
        t.Errorf("expected error containing %q, got %v", tt.expectedError, err);
    }
        if tt.expectAuthError {
        var authErr api.AuthorizationError;
        if !errors.As(err, &authErr) {
        t.Errorf("expected AuthorizationError, got %T: %v", err, err);
    }
    }
    }
        } else {
        if err != null {
        t.Errorf("expected no error, got %v", err);
    }
    }
        });
    }
    }

    public static void TestIsLocalhost(*testing.T t) {
        var tests = []struct {
        name     String;
        host     String;
        expected boolean;
        }{
        {"default empty", "", true},;
        {"localhost no port", "localhost", true},;
        {"localhost with port", "localhost:11435", true},;
        {"127.0.0.1 no port", "127.0.0.1", true},;
        {"127.0.0.1 with port", "127.0.0.1:11434", true},;
        {"0.0.0.0 no port", "0.0.0.0", true},;
        {"0.0.0.0 with port", "0.0.0.0:11434", true},;
        {"::1 no port", "::1", true},;
        {"[::1] with port", "[::1]:11434", true},;
        {"loopback with scheme", "http://localhost:11434", true},;
        {"remote hostname", "example.com", false},;
        {"remote hostname with port", "example.com:11434", false},;
        {"remote IP", "192.168.1.1", false},;
        {"remote IP with port", "192.168.1.1:11434", false},;
        {"remote with scheme", "http://example.com:11434", false},;
        {"https remote", "https://example.com:443", false},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        t.Setenv("OLLAMA_HOST", tt.host);
        var got = isLocalhost();
        if got != tt.expected {
        t.Errorf("isLocalhost() with OLLAMA_HOST=%q = %v, want %v", tt.host, got, tt.expected);
    }
        });
    }
    }
}
