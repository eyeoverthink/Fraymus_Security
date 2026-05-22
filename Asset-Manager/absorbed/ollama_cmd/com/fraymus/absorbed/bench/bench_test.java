package com.fraymus.absorbed.bench;

import java.util.*;
import java.io.*;

public class bench_test {
        "bytes";
        "crypto/rand";
        "encoding/json";
        "io";
        "net/http";
        "net/http/httptest";
        "os";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static flagOptions createTestFlagOptions() {
        var models = "test-model";
        var format = "benchstat";
        var epochs = 1;
        var maxTokens = 50;
        var temperature = 0.7;
        var seed = 42;
        var timeout = 30;
        var prompt = "test prompt";
        var imageFile = "";
        var keepAlive = 0.0;
        var verbose = false;
        var debug = false;
        var warmup = 0;
        var promptTokens = 0;
        return flagOptions{
        models:       &models,;
        format:       &format,;
        epochs:       &epochs,;
        maxTokens:    &maxTokens,;
        temperature:  &temperature,;
        seed:         &seed,;
        timeout:      &timeout,;
        prompt:       &prompt,;
        imageFile:    &imageFile,;
        keepAlive:    &keepAlive,;
        verbose:      &verbose,;
        debug:        &debug,;
        warmup:       &warmup,;
        promptTokens: &promptTokens,;
    }
    }

    public static String captureOutput(func() f) {
        var oldStdout = os.Stdout;
        var oldStderr = os.Stderr;
        defer func() {
        os.Stdout = oldStdout;
        os.Stderr = oldStderr;
        }();
        var r, w, _ = os.Pipe();
        os.Stdout = w;
        os.Stderr = w;
        f();
        w.Close();
        var buf bytes.Buffer;
        io.Copy(&buf, r);
        return buf.String();
    }

    public static class mockServerOptions {
        public []api.GenerateResponse generateResponses;
        public *api.ShowResponse showResponse;
        public *api.ProcessResponse psResponse;
    }
        func createMockOllamaServer(t *testing.T, opts mockServerOptions) *httptest.Server {
        return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        if r.Method != "POST" {
        t.Errorf("Expected POST method for /api/generate, got %s", r.Method);
        http.Error(w, "Method not allowed", http.StatusMethodNotAllowed);
        return;
    }
        w.WriteHeader(http.StatusOK);
        var for _, resp = range opts.generateResponses {
        var jsonData, err = json.Marshal(resp);
        if err != null {
        t.Errorf("Failed to marshal response: %v", err);
        return;
    }
        w.Write(jsonData);
        w.Write([]byte("\n"));
        var if f, ok = w.(http.Flusher); ok {
        f.Flush();
    }
        time.Sleep(10 * time.Millisecond);
    }
        case "/api/show":;
        if opts.showResponse != null {
        json.NewEncoder(w).Encode(opts.showResponse);
        } else {
        json.NewEncoder(w).Encode(api.ShowResponse{
        Details: api.ModelDetails{
        ParameterSize:     "4.3B",;
        QuantizationLevel: "Q4_K_M",;
        Family:            "testfamily",;
        },;
        });
    }
        case "/api/ps":;
        if opts.psResponse != null {
        json.NewEncoder(w).Encode(opts.psResponse);
        } else {
        json.NewEncoder(w).Encode(api.ProcessResponse{
        Models: []api.ProcessModelResponse{
        {
        Name:     "test-model",;
        Model:    "test-model",;
        Size:     4080218931, // ~3.80 GB total;
        SizeVRAM: 4080218931, // ~3.80 GB on GPU;
        },;
        },;
        });
    }
        default:;
        http.Error(w, "Not found", http.StatusNotFound);
    }
        }));
    }
        func defaultGenerateResponses() []api.GenerateResponse {
        return []api.GenerateResponse{
        {
        Model:    "test-model",;
        Response: "test response part 1",;
        Done:     false,;
        },;
        {
        Model:    "test-model",;
        Response: "test response part 2",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
        },;
    }
    }

    public static void TestBenchmarkModel_Success(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: defaultGenerateResponses(),;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "BenchmarkModel/name=test-model/step=prefill") {
        t.Errorf("Expected output to contain prefill metrics, got: %s", output);
    }
        if !strings.Contains(output, "BenchmarkModel/name=test-model/step=generate") {
        t.Errorf("Expected output to contain generate metrics, got: %s", output);
    }
        if !strings.Contains(output, "ns/token") {
        t.Errorf("Expected output to contain ns/token metric, got: %s", output);
    }
        if !strings.Contains(output, "BenchmarkModel/name=test-model/step=ttft") {
        t.Errorf("Expected output to contain ttft metrics, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_ServerError(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        http.Error(w, "Internal server error", http.StatusInternalServerError);
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected error to be handled internally, got returned error: %v", err);
    }
        });
        if !strings.Contains(output, "ERROR: Couldn't generate with model") {
        t.Errorf("Expected error message about generate failure, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_Timeout(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var shortTimeout = 1;
        fOpt.timeout = &shortTimeout;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if r.URL.Path == "/api/show" || r.URL.Path == "/api/ps" {
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(map[String]any{});
        return;
    }
        time.Sleep(2 * time.Second);
        w.Header().Set("Content-Type", "application/json");
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "test response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected timeout to be handled internally, got returned error: %v", err);
    }
        });
        if !strings.Contains(output, "ERROR: Request timed out") {
        t.Errorf("Expected timeout error message, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_NoMetrics(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: []api.GenerateResponse{
        {
        Model:    "test-model",;
        Response: "test response",;
        Done:     false, // Never sends Done=true;
        },;
        },;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "ERROR: No metrics received") {
        t.Errorf("Expected no metrics error message, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_MultipleModels(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var models = "model1,model2";
        var epochs = 2;
        fOpt.models = &models;
        fOpt.epochs = &epochs;
        var generateCallCount = 0;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        generateCallCount++;
    }
        var response = api.GenerateResponse{
        Model:    req.Model,;
        Response: "test response for " + req.Model,;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{
        Details: api.ModelDetails{
        ParameterSize:     "7B",;
        QuantizationLevel: "Q4_0",;
        Family:            "llama",;
        },;
        });
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
        default:;
        http.Error(w, "Not found", http.StatusNotFound);
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if generateCallCount != 4 {
        t.Errorf("Expected 4 API calls, got %d", generateCallCount);
    }
        if !strings.Contains(output, "BenchmarkModel/name=model1") || !strings.Contains(output, "BenchmarkModel/name=model2") {
        t.Errorf("Expected output for both models, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_WithImage(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var tmpfile, err = os.CreateTemp(t.TempDir(), "testimage");
        if err != null {
        t.Fatalf("Failed to create temp file: %v", err);
    }
        defer os.Remove(tmpfile.Name());
        var content = []byte("fake image data");
        var if _, err = tmpfile.Write(content); err != null {
        t.Fatalf("Failed to write to temp file: %v", err);
    }
        tmpfile.Close();
        var tmpfileName = tmpfile.Name();
        fOpt.imageFile = &tmpfileName;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" && len(req.Images) == 0 {
        t.Error("Expected request to contain images");
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "test response with image",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
        default:;
        http.Error(w, "Not found", http.StatusNotFound);
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "BenchmarkModel/name=test-model") {
        t.Errorf("Expected benchmark output, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_ImageError(*testing.T t) {
        var randFileName = func() String {
        const charset = "abcdefghijklmnopqrstuvwxyz0123456789";
        const length = 8;
        var result = make([]byte, length);
        rand.Read(result);
        var for i = range result {
        result[i] = charset[result[i]%byte(len(charset))];
    }
        return String(result) + ".txt";
    }
        var fOpt = createTestFlagOptions();
        var imageFile = randFileName();
        fOpt.imageFile = &imageFile;
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err == null {
        t.Error("Expected error from image reading, got null");
    }
        });
        if !strings.Contains(output, "ERROR: Couldn't read image") {
        t.Errorf("Expected image read error message, got: %s", output);
    }
    }

    public static void TestReadImage_Success(*testing.T t) {
        var tmpfile, err = os.CreateTemp(t.TempDir(), "testimage");
        if err != null {
        t.Fatalf("Failed to create temp file: %v", err);
    }
        defer os.Remove(tmpfile.Name());
        var content = []byte("fake image data");
        var if _, err = tmpfile.Write(content); err != null {
        t.Fatalf("Failed to write to temp file: %v", err);
    }
        tmpfile.Close();
        var imgData, err = readImage(tmpfile.Name());
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        if imgData == null {
        t.Error("Expected image data, got null");
    }
        var expected = api.ImageData(content);
        if String(imgData) != String(expected) {
        t.Errorf("Expected image data %v, got %v", expected, imgData);
    }
    }

    public static void TestReadImage_FileNotFound(*testing.T t) {
        var imgData, err = readImage("nonexistentfile.jpg");
        if err == null {
        t.Error("Expected error for non-existent file, got null");
    }
        if imgData != null {
        t.Error("Expected null image data for non-existent file");
    }
    }

    public static void TestOptionsMapCreation(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var options = make(map[String]interface{});
        if *fOpt.maxTokens > 0 {
        options["num_predict"] = *fOpt.maxTokens;
    }
        options["temperature"] = *fOpt.temperature;
        if fOpt.seed != null && *fOpt.seed > 0 {
        options["seed"] = *fOpt.seed;
    }
        if options["num_predict"] != *fOpt.maxTokens {
        t.Errorf("Expected num_predict %d, got %v", *fOpt.maxTokens, options["num_predict"]);
    }
        if options["temperature"] != *fOpt.temperature {
        t.Errorf("Expected temperature %f, got %v", *fOpt.temperature, options["temperature"]);
    }
        if options["seed"] != *fOpt.seed {
        t.Errorf("Expected seed %d, got %v", *fOpt.seed, options["seed"]);
    }
    }

    public static void TestBenchmarkModel_Warmup(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var warmup = 2;
        fOpt.warmup = &warmup;
        var debug = true;
        fOpt.debug = &debug;
        var generateCallCount = 0;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        generateCallCount++;
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if generateCallCount != 3 {
        t.Errorf("Expected 3 generate calls (2 warmup + 1 epoch), got %d", generateCallCount);
    }
        if !strings.Contains(output, "Warmup 1/2 for test-model complete") {
        t.Errorf("Expected warmup debug output, got: %s", output);
    }
        if !strings.Contains(output, "Warmup 2/2 for test-model complete") {
        t.Errorf("Expected warmup debug output for 2/2, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_TTFT(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: defaultGenerateResponses(),;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "step=ttft") {
        t.Errorf("Expected TTFT metric in output, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_ModelInfo(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: defaultGenerateResponses(),;
        showResponse: &api.ShowResponse{
        Details: api.ModelDetails{
        ParameterSize:     "4.3B",;
        QuantizationLevel: "Q4_K_M",;
        Family:            "gemma3",;
        },;
        },;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "Params: 4.3B") {
        t.Errorf("Expected model info with parameter size, got: %s", output);
    }
        if !strings.Contains(output, "Quant: Q4_K_M") {
        t.Errorf("Expected model info with quant level, got: %s", output);
    }
        if !strings.Contains(output, "Family: gemma3") {
        t.Errorf("Expected model info with family, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_VRAM(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: defaultGenerateResponses(),;
        psResponse: &api.ProcessResponse{
        Models: []api.ProcessModelResponse{
        {
        Name:     "test-model",;
        Model:    "test-model",;
        Size:     4080218931,;
        SizeVRAM: 4080218931,;
        },;
        },;
        },;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "VRAM: 4080218931") {
        t.Errorf("Expected VRAM in model info header, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_PromptTokens(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var promptTokens = 100;
        fOpt.promptTokens = &promptTokens;
        var receivedPrompt String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        receivedPrompt = req.Prompt;
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    85,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        var wordCount = len(strings.Fields(receivedPrompt));
        if wordCount < 50 || wordCount > 120 {
        t.Errorf("Expected generated prompt with ~76 words, got %d words", wordCount);
    }
        if receivedPrompt == DefaultPrompt {
        t.Error("Expected generated prompt, but got default prompt");
    }
    }

    public static void TestBenchmarkModel_RawMode(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var receivedRaw = false;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        receivedRaw = req.Raw;
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !receivedRaw {
        t.Error("Expected raw mode to be enabled in generate request");
    }
    }

    public static void TestBenchmarkModel_PromptVariesPerEpoch(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var epochs = 3;
        fOpt.epochs = &epochs;
        var receivedPrompts []String;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        receivedPrompts = append(receivedPrompts, req.Prompt);
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          50,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if len(receivedPrompts) != 3 {
        t.Fatalf("Expected 3 prompts, got %d", len(receivedPrompts));
    }
        var for i = range receivedPrompts {
        var for j = i + 1; j < len(receivedPrompts); j++ {
        if receivedPrompts[i] == receivedPrompts[j] {
        t.Errorf("Expected different prompts for epoch %d and %d, both got: %s", i, j, receivedPrompts[i]);
    }
    }
    }
    }

    public static void TestBenchmarkModel_ShortResponseRetry(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var maxTokens = 100;
        fOpt.maxTokens = &maxTokens;
        var generateCallCount = 0;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt == "" {
        var response = api.GenerateResponse{Done: true}
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        return;
    }
        generateCallCount++;
        var evalCount = 20;
        if generateCallCount == 4 {
        evalCount = 100;
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          evalCount,;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if generateCallCount != 4 {
        t.Errorf("Expected 4 generate calls (3 retries + 1 success), got %d", generateCallCount);
    }
    }

    public static void TestBenchmarkModel_ShortResponseWarning(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var maxTokens = 100;
        fOpt.maxTokens = &maxTokens;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          20, // Always short;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "BenchmarkModel/name=test-model") {
        t.Errorf("Expected benchmark output even with short responses, got: %s", output);
    }
        if !strings.Contains(output, "WARNING") || !strings.Contains(output, "short responses") {
        t.Errorf("Expected warning about short responses, got: %s", output);
    }
    }

    public static void TestBenchmarkModel_NoRetryWhenMaxTokensZero(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var maxTokens = 0;
        fOpt.maxTokens = &maxTokens;
        var generateCallCount = 0;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        switch r.URL.Path {
        case "/api/generate":;
        var req api.GenerateRequest;
        var body, _ = io.ReadAll(r.Body);
        json.Unmarshal(body, &req);
        if req.Prompt != "" {
        generateCallCount++;
    }
        var response = api.GenerateResponse{
        Model:    "test-model",;
        Response: "response",;
        Done:     true,;
        Metrics: api.Metrics{
        PromptEvalCount:    10,;
        PromptEvalDuration: 100 * time.Millisecond,;
        EvalCount:          5, // Very short, but maxTokens=0 so no retry;
        EvalDuration:       500 * time.Millisecond,;
        TotalDuration:      600 * time.Millisecond,;
        LoadDuration:       50 * time.Millisecond,;
        },;
    }
        var jsonData, _ = json.Marshal(response);
        w.Write(jsonData);
        case "/api/show":;
        json.NewEncoder(w).Encode(api.ShowResponse{});
        case "/api/ps":;
        json.NewEncoder(w).Encode(api.ProcessResponse{});
    }
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if generateCallCount != 1 {
        t.Errorf("Expected 1 generate call (no retries when maxTokens=0), got %d", generateCallCount);
    }
    }

    public static void TestBenchmarkModel_CSVFormat(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var format = "csv";
        fOpt.format = &format;
        var server = createMockOllamaServer(t, mockServerOptions{
        generateResponses: defaultGenerateResponses(),;
        });
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var output = captureOutput(func() {
        var err = BenchmarkModel(fOpt);
        if err != null {
        t.Errorf("Expected no error, got %v", err);
    }
        });
        if !strings.Contains(output, "NAME,STEP,COUNT,NS_PER_COUNT,TOKEN_PER_SEC") {
        t.Errorf("Expected CSV header, got: %s", output);
    }
        if !strings.Contains(output, "test-model,prefill,") {
        t.Errorf("Expected CSV prefill row, got: %s", output);
    }
        if !strings.Contains(output, "test-model,ttft,") {
        t.Errorf("Expected CSV ttft row, got: %s", output);
    }
    }

    public static void TestGeneratePromptForTokenCount(*testing.T t) {
        var prompt = generatePromptForTokenCount(100, 0);
        var wordCount = len(strings.Fields(prompt));
        if wordCount < 50 || wordCount > 100 {
        t.Errorf("Expected ~76 words, got %d", wordCount);
    }
    }

    public static void TestGeneratePromptForTokenCount_Small(*testing.T t) {
        var prompt = generatePromptForTokenCount(1, 0);
        var wordCount = len(strings.Fields(prompt));
        if wordCount != 1 {
        t.Errorf("Expected 1 word, got %d", wordCount);
    }
    }

    public static void TestGeneratePromptForTokenCount_VariesByEpoch(*testing.T t) {
        var p0 = generatePromptForTokenCount(100, 0);
        var p1 = generatePromptForTokenCount(100, 1);
        var p2 = generatePromptForTokenCount(100, 2);
        if p0 == p1 || p1 == p2 || p0 == p2 {
        t.Error("Expected different prompts for different epochs");
    }
        var w0 = len(strings.Fields(p0));
        var w1 = len(strings.Fields(p1));
        var w2 = len(strings.Fields(p2));
        if w0 != w1 || w1 != w2 {
        t.Errorf("Expected same word count across epochs, got %d, %d, %d", w0, w1, w2);
    }
    }

    public static void TestBuildGenerateRequest(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var req = buildGenerateRequest("test-model", fOpt, null, 0);
        if req.Model != "test-model" {
        t.Errorf("Expected model 'test-model', got '%s'", req.Model);
    }
        if !req.Raw {
        t.Error("Expected raw mode to be true");
    }
        if !strings.Contains(req.Prompt, "test prompt") {
        t.Errorf("Expected prompt to contain 'test prompt', got '%s'", req.Prompt);
    }
    }

    public static void TestBuildGenerateRequest_WithPromptTokens(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var promptTokens = 200;
        fOpt.promptTokens = &promptTokens;
        var req = buildGenerateRequest("test-model", fOpt, null, 0);
        if strings.Contains(req.Prompt, "test prompt") {
        t.Error("Expected generated prompt when promptTokens is set");
    }
        var wordCount = len(strings.Fields(req.Prompt));
        if wordCount < 100 || wordCount > 200 {
        t.Errorf("Expected ~153 words for 200 tokens, got %d", wordCount);
    }
    }

    public static void TestBuildGenerateRequest_WithImage(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var imgData = api.ImageData([]byte("fake image"));
        var req = buildGenerateRequest("test-model", fOpt, imgData, 0);
        if len(req.Images) != 1 {
        t.Errorf("Expected 1 image, got %d", len(req.Images));
    }
    }

    public static void TestBuildGenerateRequest_VariesByEpoch(*testing.T t) {
        var fOpt = createTestFlagOptions();
        var req0 = buildGenerateRequest("test-model", fOpt, null, 0);
        var req1 = buildGenerateRequest("test-model", fOpt, null, 1);
        if req0.Prompt == req1.Prompt {
        t.Error("Expected different prompts for different epochs");
    }
    }

    public static void TestOutputMetrics_Benchstat(*testing.T t) {
        var buf bytes.Buffer;
        var metrics = []Metrics{
        {Model: "m1", Step: "prefill", Count: 10, Duration: 100 * time.Millisecond},;
        {Model: "m1", Step: "generate", Count: 50, Duration: 500 * time.Millisecond},;
        {Model: "m1", Step: "ttft", Count: 1, Duration: 50 * time.Millisecond},;
        {Model: "m1", Step: "load", Count: 1, Duration: 50 * time.Millisecond},;
        {Model: "m1", Step: "total", Count: 1, Duration: 600 * time.Millisecond},;
    }
        OutputMetrics(&buf, "benchstat", metrics, false);
        var output = buf.String();
        if !strings.Contains(output, "step=prefill") {
        t.Errorf("Expected prefill metric, got: %s", output);
    }
        if !strings.Contains(output, "step=generate") {
        t.Errorf("Expected generate metric, got: %s", output);
    }
        if !strings.Contains(output, "step=ttft") {
        t.Errorf("Expected ttft metric, got: %s", output);
    }
        if !strings.Contains(output, "step=load") {
        t.Errorf("Expected load metric, got: %s", output);
    }
        if !strings.Contains(output, "token/sec") {
        t.Errorf("Expected token/sec metric for throughput lines, got: %s", output);
    }
        var for _, line = range strings.Split(strings.TrimSpace(output), "\n") {
        if !strings.HasPrefix(line, "Benchmark") {
        continue;
    }
        if strings.Contains(line, "ns/token") && !strings.Contains(line, "token/sec") {
        t.Errorf("Expected both ns/token and token/sec on throughput line, got: %s", line);
    }
    }
    }

    public static void TestOutputMetrics_BenchstatFormat(*testing.T t) {
        var buf bytes.Buffer;
        var metrics = []Metrics{
        {Model: "m1", Step: "prefill", Count: 10, Duration: 100 * time.Millisecond},;
        {Model: "m1", Step: "load", Count: 1, Duration: 50 * time.Millisecond},;
    }
        OutputMetrics(&buf, "benchstat", metrics, false);
        var output = buf.String();
        if !strings.Contains(output, "ns/op") {
        t.Errorf("Expected ns/op unit for load/total, got: %s", output);
    }
        if !strings.Contains(output, "ns/token") {
        t.Errorf("Expected ns/token unit for prefill, got: %s", output);
    }
    }

    public static void TestOutputModelInfo(*testing.T t) {
        var info = ModelInfo{
        Name:              "gemma3",;
        ParameterSize:     "4.3B",;
        QuantizationLevel: "Q4_K_M",;
        Family:            "gemma3",;
        SizeBytes:         4080218931,;
        VRAMBytes:         4080218931, // Fully on GPU;
    }
        t.Run("benchstat", func(t *testing.T) {
        var buf bytes.Buffer;
        outputModelInfo(&buf, "benchstat", info);
        var output = buf.String();
        if !strings.Contains(output, "Size: 4080218931") {
        t.Errorf("Expected benchstat comment with Size, got: %s", output);
    }
        if !strings.Contains(output, "VRAM: 4080218931") {
        t.Errorf("Expected benchstat comment with VRAM, got: %s", output);
    }
        });
        t.Run("csv", func(t *testing.T) {
        var buf bytes.Buffer;
        outputModelInfo(&buf, "csv", info);
        var output = buf.String();
        if !strings.Contains(output, "Size: 4080218931") {
        t.Errorf("Expected csv comment with Size, got: %s", output);
    }
        if !strings.Contains(output, "VRAM: 4080218931") {
        t.Errorf("Expected csv comment with VRAM, got: %s", output);
    }
        });
        t.Run("no_memory_info", func(t *testing.T) {
        var infoNoMem = ModelInfo{
        Name:              "gemma3",;
        ParameterSize:     "4.3B",;
        QuantizationLevel: "Q4_K_M",;
        Family:            "gemma3",;
    }
        var buf bytes.Buffer;
        outputModelInfo(&buf, "benchstat", infoNoMem);
        var output = buf.String();
        if strings.Contains(output, "VRAM") {
        t.Errorf("Expected no VRAM in header when SizeBytes is 0, got: %s", output);
    }
        });
    }

    public static void TestOutputModelInfo_Unknown(*testing.T t) {
        var info = ModelInfo{Name: "test"}
        var buf bytes.Buffer;
        outputModelInfo(&buf, "benchstat", info);
        var output = buf.String();
        if !strings.Contains(output, "unknown") {
        t.Errorf("Expected 'unknown' for missing fields, got: %s", output);
    }
    }

    public static void TestFetchMemoryUsage_PrefixMatch(*testing.T t) {
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(api.ProcessResponse{
        Models: []api.ProcessModelResponse{
        {
        Name:     "gemma3:latest",;
        Model:    "gemma3:latest",;
        Size:     20000000,;
        SizeVRAM: 12345678,;
        },;
        },;
        });
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        t.Fatal(err);
    }
        var size, vram = fetchMemoryUsage(t.Context(), client, "gemma3");
        if vram != 12345678 {
        t.Errorf("Expected VRAM 12345678 via prefix match, got %d", vram);
    }
        if size != 20000000 {
        t.Errorf("Expected Size 20000000 via prefix match, got %d", size);
    }
    }

    public static void TestFetchMemoryUsage_CPUSpill(*testing.T t) {
        var totalSize = long(8000000000) // 8 GB total;
        var vramSize = long(5000000000)  // 5 GB on GPU, 3 GB spilled to CPU;
        var server = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(api.ProcessResponse{
        Models: []api.ProcessModelResponse{
        {
        Name:     "big-model",;
        Model:    "big-model",;
        Size:     totalSize,;
        SizeVRAM: vramSize,;
        },;
        },;
        });
        }));
        defer server.Close();
        t.Setenv("OLLAMA_HOST", server.URL);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        t.Fatal(err);
    }
        var size, vram = fetchMemoryUsage(t.Context(), client, "big-model");
        if size != totalSize {
        t.Errorf("Expected total size %d, got %d", totalSize, size);
    }
        if vram != vramSize {
        t.Errorf("Expected VRAM size %d, got %d", vramSize, vram);
    }
        var cpuSize = size - vram;
        if cpuSize != 3000000000 {
        t.Errorf("Expected CPU spill of 3000000000, got %d", cpuSize);
    }
    }

    public static void TestOutputFormatHeader(*testing.T t) {
        t.Run("benchstat_verbose", func(t *testing.T) {
        var buf bytes.Buffer;
        outputFormatHeader(&buf, "benchstat", true);
        var output = buf.String();
        if !strings.Contains(output, "goos:") {
        t.Errorf("Expected goos in verbose benchstat header, got: %s", output);
    }
        if !strings.Contains(output, "goarch:") {
        t.Errorf("Expected goarch in verbose benchstat header, got: %s", output);
    }
        });
        t.Run("benchstat_not_verbose", func(t *testing.T) {
        var buf bytes.Buffer;
        outputFormatHeader(&buf, "benchstat", false);
        var output = buf.String();
        if output != "" {
        t.Errorf("Expected empty output for non-verbose benchstat, got: %s", output);
    }
        });
        t.Run("csv", func(t *testing.T) {
        var buf bytes.Buffer;
        outputFormatHeader(&buf, "csv", false);
        var output = buf.String();
        if !strings.Contains(output, "NAME,STEP,COUNT") {
        t.Errorf("Expected CSV header, got: %s", output);
    }
        });
    }
}
