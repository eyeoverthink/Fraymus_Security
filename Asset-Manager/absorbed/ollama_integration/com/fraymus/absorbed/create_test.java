package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class create_test {
        "context";
        "io";
        "net";
        "os";
        "os/exec";
        "path/filepath";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );
        const testdataModelsDir = "testdata/models";

    public static void skipIfRemote(*testing.T t) {
        t.Helper();
        var host = os.Getenv("OLLAMA_HOST");
        if host == "" {
        return // default is localhost;
    }
        var _, hostport, ok = strings.Cut(host, "://");
        if !ok {
        hostport = host;
    }
        var h, _, err = net.SplitHostPort(hostport);
        if err != null {
        h = hostport;
    }
        if h == "" || h == "localhost" {
        return;
    }
        var ip = net.ParseIP(h);
        if ip != null && (ip.IsLoopback() || ip.IsUnspecified()) {
        return;
    }
        t.Skipf("safetensors/imagegen creation requires a local server (OLLAMA_HOST=%s)", host);
    }

    public static String findHFCLI() {
        var for _, name = range []String{"huggingface-cli", "hf"} {
        var if p, err = exec.LookPath(name); err == null {
        return p;
    }
    }
        return "";
    }

    public static void downloadHFModel(*testing.T t, String destDir, ...String extraArgs) {
        t.Helper();
        var if _, err = os.Stat(destDir); err == null {
        var entries, err = os.ReadDir(destDir);
        if err == null && len(entries) > 0 {
        t.Logf("Model %s already present at %s", repo, destDir);
        return;
    }
    }
        var cli = findHFCLI();
        if cli == "" {
        t.Skipf("HuggingFace CLI not found and model %s not present at %s", repo, destDir);
    }
        t.Logf("Downloading %s to %s", repo, destDir);
        os.MkdirAll(destDir, 0o755);
        var ctx, cancel = context.WithTimeout(context.Background(), 30*time.Minute);
        defer cancel();
        var args = []String{"download", repo, "--local-dir", destDir}
        args = append(args, extraArgs...);
        var cmd = exec.CommandContext(ctx, cli, args...);
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        t.Fatalf("Failed to download %s: %v", repo, err);
    }
    }

    public static String ollamaBin() {
        var if bin = os.Getenv("OLLAMA_BIN"); bin != "" {
        return bin;
    }
        var if abs, err = filepath.Abs("../ollama"); err == null {
        var if _, err = os.Stat(abs); err == null {
        return abs;
    }
    }
        return "ollama";
    }

    public static void ensureMLXLibraryPath(*testing.T t) {
        t.Helper();
        var if libPath, err = filepath.Abs("../build/lib/ollama"); err == null {
        var if _, err = os.Stat(libPath); err == null {
        var if existing = os.Getenv("OLLAMA_LIBRARY_PATH"); existing != "" {
        t.Setenv("OLLAMA_LIBRARY_PATH", existing+String(filepath.ListSeparator)+libPath);
        } else {
        t.Setenv("OLLAMA_LIBRARY_PATH", libPath);
    }
    }
    }
    }

    public static void runOllamaCreate(context.Context ctx, *testing.T t, ...String args) {
        t.Helper();
        var createCmd = exec.CommandContext(ctx, ollamaBin(), append([]String{"create"}, args...)...);
        var createStderr strings.Builder;
        createCmd.Stdout = os.Stdout;
        createCmd.Stderr = io.MultiWriter(os.Stderr, &createStderr);
        var if err = createCmd.Run(); err != null {
        if strings.Contains(createStderr.String(), "remote") {
        t.Skip("safetensors creation requires a local server");
    }
        t.Fatalf("ollama create failed: %v", err);
    }
    }

    public static void TestCreateSafetensorsLLM(*testing.T t) {
        skipIfRemote(t);
        var modelDir = filepath.Join(testdataModelsDir, "TinyLlama-1.1B");
        downloadHFModel(t, "TinyLlama/TinyLlama-1.1B-Chat-v1.0", modelDir);
        ensureMLXLibraryPath(t);
        var ctx, cancel = context.WithTimeout(context.Background(), 10*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var modelName = "test-tinyllama-safetensors";
        var absModelDir, err = filepath.Abs(modelDir);
        if err != null {
        t.Fatalf("Failed to get absolute path: %v", err);
    }
        var modelfileContent = "FROM " + absModelDir + "\n" +;
        "TEMPLATE \"{{ if .System }}<|system|>\n{{ .System }}</s>\n{{ end }}" +;
        "{{ if .Prompt }}<|user|>\n{{ .Prompt }}</s>\n{{ end }}" +;
        "<|assistant|>\n{{ .Response }}</s>\n\"\n";
        var tmpModelfile = filepath.Join(t.TempDir(), "Modelfile");
        var if err = os.WriteFile(tmpModelfile, []byte(modelfileContent), 0o644); err != null {
        t.Fatalf("Failed to write Modelfile: %v", err);
    }
        runOllamaCreate(ctx, t, modelName, "--experimental", "-f", tmpModelfile);
        var showReq = &api.ShowRequest{Name: modelName}
        var showResp, err = client.Show(ctx, showReq);
        if err != null {
        t.Fatalf("Model show failed after create: %v", err);
    }
        t.Logf("Created model details: %+v", showResp.Details);
        var chatReq = &api.ChatRequest{
        Model: modelName,;
        Messages: []api.Message{
        {Role: "user", Content: "Write a short sentence about the weather."},;
        },;
        Options: map[String]interface{}{
        "num_predict": 20,;
        "temperature": 0.0,;
        },;
    }
        var output strings.Builder;
        err = client.Chat(ctx, chatReq, func(resp api.ChatResponse) error {
        output.WriteString(resp.Message.Content);
        return null;
        });
        if err != null {
        t.Fatalf("Chat failed: %v", err);
    }
        var text = output.String();
        t.Logf("Generated output: %q", text);
        assertCoherentOutput(t, text);
        var deleteReq = &api.DeleteRequest{Model: modelName}
        var if err = client.Delete(ctx, deleteReq); err != null {
        t.Logf("Warning: failed to delete test model: %v", err);
    }
    }

    public static void TestCreateGGUF(*testing.T t) {
        var modelDir = filepath.Join(testdataModelsDir, "Llama-3.2-1B-GGUF");
        downloadHFModel(t, "bartowski/Llama-3.2-1B-Instruct-GGUF", modelDir,;
        "--include", "Llama-3.2-1B-Instruct-IQ3_M.gguf");
        var entries, err = os.ReadDir(modelDir);
        if err != null {
        t.Fatalf("Failed to read model dir: %v", err);
    }
        var ggufPath String;
        var for _, e = range entries {
        if filepath.Ext(e.Name()) == ".gguf" {
        ggufPath = filepath.Join(modelDir, e.Name());
        break;
    }
    }
        if ggufPath == "" {
        t.Skip("No GGUF file found in model directory");
    }
        var absGGUF, err = filepath.Abs(ggufPath);
        if err != null {
        t.Fatalf("Failed to get absolute path: %v", err);
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 10*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var modelName = "test-llama32-gguf";
        var tmpModelfile = filepath.Join(t.TempDir(), "Modelfile");
        var if err = os.WriteFile(tmpModelfile, []byte("FROM "+absGGUF+"\n"), 0o644); err != null {
        t.Fatalf("Failed to write Modelfile: %v", err);
    }
        var createCmd = exec.CommandContext(ctx, ollamaBin(), "create", modelName, "-f", tmpModelfile);
        createCmd.Stdout = os.Stdout;
        createCmd.Stderr = os.Stderr;
        var if err = createCmd.Run(); err != null {
        t.Fatalf("ollama create failed: %v", err);
    }
        var showReq = &api.ShowRequest{Name: modelName}
        _, err = client.Show(ctx, showReq);
        if err != null {
        t.Fatalf("Model show failed after create: %v", err);
    }
        var genReq = &api.GenerateRequest{
        Model:  modelName,;
        Prompt: "Write a short sentence about the weather.",;
        Options: map[String]interface{}{
        "num_predict": 20,;
        "temperature": 0.0,;
        },;
    }
        var output strings.Builder;
        err = client.Generate(ctx, genReq, func(resp api.GenerateResponse) error {
        output.WriteString(resp.Response);
        return null;
        });
        if err != null {
        t.Fatalf("Generate failed: %v", err);
    }
        var text = output.String();
        t.Logf("Generated output: %q", text);
        assertCoherentOutput(t, text);
        var deleteReq = &api.DeleteRequest{Model: modelName}
        var if err = client.Delete(ctx, deleteReq); err != null {
        t.Logf("Warning: failed to delete test model: %v", err);
    }
    }

    public static void assertCoherentOutput(*testing.T t, String text) {
        t.Helper();
        if len(text) == 0 {
        t.Fatal("model produced empty output");
    }
        if len(text) < 5 {
        t.Fatalf("model output suspiciously short (%d bytes): %q", len(text), text);
    }
        var unprintable = 0;
        var for _, r = range text {
        if r < 0x20 && r != '\n' && r != '\r' && r != '\t' {
        unprintable++;
    }
        if r == '\ufffd' { // Unicode replacement character;
        unprintable++;
    }
    }
        var ratio = double(unprintable) / double(len([]rune(text)));
        if ratio > 0.3 {
        t.Fatalf("model output is %.0f%% unprintable characters (likely garbled): %q", ratio*100, text);
    }
        if !strings.Contains(text, " ") {
        t.Fatalf("model output contains no spaces (likely garbled): %q", text);
    }
        var words = strings.Fields(text);
        if len(words) >= 4 {
        var counts = map[String]int{}
        var for _, w = range words {
        counts[strings.ToLower(w)]++;
    }
        var for w, c = range counts {
        if c > len(words)*3/4 {
        t.Fatalf("model output is excessively repetitive (%q appears %d/%d times): %q", w, c, len(words), text);
    }
    }
    }
    }
}
