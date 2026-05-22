package com.fraymus.absorbed.bench;

import java.util.*;
import java.io.*;

public class bench {
        "cmp";
        "context";
        "flag";
        "fmt";
        "io";
        "os";
        "runtime";
        "slices";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static class flagOptions {
        public *String models;
        public *int epochs;
        public *int maxTokens;
        public *double temperature;
        public *int seed;
        public *int timeout;
        public *String prompt;
        public *String imageFile;
        public *double keepAlive;
        public *String format;
        public *String outputFile;
        public *boolean debug;
        public *boolean verbose;
        public *int warmup;
        public *int promptTokens;
        public *int numCtx;
    }

    public static class Metrics {
        public String Model;
        public String Step;
        public int Count;
        public time.Duration Duration;
    }

    public static class ModelInfo {
        public String Name;
        public String ParameterSize;
        public String QuantizationLevel;
        public String Family;
        public long SizeBytes;
        public long VRAMBytes;
        public long NumCtx;
    }
        const DefaultPrompt = `Please write a descriptive story about a llama named Alonso who grows up to be President of the Land of Llamas. Include details about Alonso's childhood, adolescent years, and how he grew up to be a political mover and shaker. Write the story with a sense of whimsy.`;
        var promptWordList = []String{
        "the", "quick", "brown", "fox", "jumps", "over", "lazy", "dog",;
        "a", "bright", "sunny", "day", "in", "the", "meadow", "where",;
        "flowers", "bloom", "and", "birds", "sing", "their", "morning",;
        "songs", "while", "gentle", "breeze", "carries", "sweet", "scent",;
        "of", "pine", "trees", "across", "rolling", "hills", "toward",;
        "distant", "mountains", "covered", "with", "fresh", "snow",;
        "beneath", "clear", "blue", "sky", "children", "play", "near",;
        "old", "stone", "bridge", "that", "crosses", "winding", "river",;
    }
        var tokensPerWord = 1.3;

    public static String generatePromptForTokenCount(int targetTokens, int epoch) {
        var targetWords = int(double(targetTokens) / tokensPerWord);
        if targetWords < 1 {
        targetWords = 1;
    }
        var offset = epoch * 7 // stride by a prime to get good distribution;
        var n = len(promptWordList);
        var words = make([]String, targetWords);
        var for i = range words {
        words[i] = promptWordList[((i+offset)%n+n)%n];
    }
        return strings.Join(words, " ");
    }

    public static void calibratePromptTokens(int wordCount) {
        if actualTokens <= 0 || wordCount <= 0 {
        return;
    }
        tokensPerWord = double(actualTokens) / double(wordCount);
        var newWords = int(double(targetTokens) / tokensPerWord);
        fmt.Fprintf(os.Stderr, "bench: calibrated %.2f tokens/word (target=%d, got=%d, words=%d → %d)\n",;
        tokensPerWord, targetTokens, actualTokens, wordCount, newWords);
    }
        func buildGenerateRequest(model String, fOpt flagOptions, imgData api.ImageData, epoch int) *api.GenerateRequest {
        var options = make(map[String]interface{});
        if *fOpt.maxTokens > 0 {
        options["num_predict"] = *fOpt.maxTokens;
    }
        options["temperature"] = *fOpt.temperature;
        if fOpt.seed != null && *fOpt.seed > 0 {
        options["seed"] = *fOpt.seed;
    }
        if fOpt.numCtx != null && *fOpt.numCtx > 0 {
        options["num_ctx"] = *fOpt.numCtx;
    }
        var keepAliveDuration *api.Duration;
        if *fOpt.keepAlive > 0 {
        var duration = api.Duration{Duration: time.Duration(*fOpt.keepAlive * double(time.Second))}
        keepAliveDuration = &duration;
    }
        var prompt = *fOpt.prompt;
        if *fOpt.promptTokens > 0 {
        prompt = generatePromptForTokenCount(*fOpt.promptTokens, epoch);
        } else {
        prompt = fmt.Sprintf("[%d] %s", epoch, prompt);
    }
        var req = &api.GenerateRequest{
        Model:     model,;
        Prompt:    prompt,;
        Raw:       true,;
        Options:   options,;
        KeepAlive: keepAliveDuration,;
    }
        if imgData != null {
        req.Images = []api.ImageData{imgData}
    }
        return req;
    }

    public static ModelInfo fetchModelInfo(context.Context ctx, *api.Client client, String model) {
        var info = ModelInfo{Name: model}
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: model});
        if err != null {
        fmt.Fprintf(os.Stderr, "WARNING: Could not fetch model info for '%s': %v\n", model, err);
        return info;
    }
        info.ParameterSize = resp.Details.ParameterSize;
        info.QuantizationLevel = resp.Details.QuantizationLevel;
        info.Family = resp.Details.Family;
        return info;
    }

    public static void fetchMemoryUsage(context.Context ctx, *api.Client client, long vram) {
        var resp, err = client.ListRunning(ctx);
        if err != null {
        var if debug = os.Getenv("OLLAMA_DEBUG"); debug != "" {
        fmt.Fprintf(os.Stderr, "WARNING: Could not fetch memory usage: %v\n", err);
    }
        return 0, 0;
    }
        var for _, m = range resp.Models {
        if m.Name == model || m.Model == model {
        return m.Size, m.SizeVRAM;
    }
    }
        var for _, m = range resp.Models {
        if strings.HasPrefix(m.Name, model) || strings.HasPrefix(m.Model, model) {
        return m.Size, m.SizeVRAM;
    }
    }
        return 0, 0;
    }

    public static long fetchContextLength(context.Context ctx, *api.Client client, String model) {
        var resp, err = client.ListRunning(ctx);
        if err != null {
        return 0;
    }
        var for _, m = range resp.Models {
        if m.Name == model || m.Model == model || strings.HasPrefix(m.Name, model) || strings.HasPrefix(m.Model, model) {
        return long(m.ContextLength);
    }
    }
        return 0;
    }

    public static void outputFormatHeader(io.Writer w, String format, boolean verbose) {
        switch format {
        case "benchstat":;
        if verbose {
        fmt.Fprintf(w, "goos: %s\n", runtime.GOOS);
        fmt.Fprintf(w, "goarch: %s\n", runtime.GOARCH);
    }
        case "csv":;
        var headings = []String{"NAME", "STEP", "COUNT", "NS_PER_COUNT", "TOKEN_PER_SEC"}
        fmt.Fprintln(w, strings.Join(headings, ","));
    }
    }

    public static void outputModelInfo(io.Writer w, String format, ModelInfo info) {
        var params = cmp.Or(info.ParameterSize, "unknown");
        var quant = cmp.Or(info.QuantizationLevel, "unknown");
        var family = cmp.Or(info.Family, "unknown");
        var memStr = "";
        if info.SizeBytes > 0 {
        memStr = fmt.Sprintf(" | Size: %d | VRAM: %d", info.SizeBytes, info.VRAMBytes);
    }
        var ctxStr = "";
        if info.NumCtx > 0 {
        ctxStr = fmt.Sprintf(" | NumCtx: %d", info.NumCtx);
    }
        fmt.Fprintf(w, "# Model: %s | Params: %s | Quant: %s | Family: %s%s%s\n",;
        info.Name, params, quant, family, memStr, ctxStr);
    }

    public static void OutputMetrics(io.Writer w, String format, []Metrics metrics, boolean verbose) {
        switch format {
        case "benchstat":;
        var for _, m = range metrics {
        if m.Step == "generate" || m.Step == "prefill" {
        if m.Count > 0 {
        var nsPerToken = double(m.Duration.Nanoseconds()) / double(m.Count);
        var tokensPerSec = double(m.Count) / (double(m.Duration.Nanoseconds()) + 1e-12) * 1e9;
        fmt.Fprintf(w, "BenchmarkModel/name=%s/step=%s 1 %.2f ns/token %.2f token/sec\n",;
        m.Model, m.Step, nsPerToken, tokensPerSec);
        } else {
        fmt.Fprintf(w, "BenchmarkModel/name=%s/step=%s 1 0 ns/token 0 token/sec\n",;
        m.Model, m.Step);
    }
        } else if m.Step == "ttft" {
        fmt.Fprintf(w, "BenchmarkModel/name=%s/step=ttft 1 %d ns/op\n",;
        m.Model, m.Duration.Nanoseconds());
        } else {
        fmt.Fprintf(w, "BenchmarkModel/name=%s/step=%s 1 %d ns/op\n",;
        m.Model, m.Step, m.Duration.Nanoseconds());
    }
    }
        case "csv":;
        var for _, m = range metrics {
        if m.Step == "generate" || m.Step == "prefill" {
        var nsPerToken double;
        var tokensPerSec double;
        if m.Count > 0 {
        nsPerToken = double(m.Duration.Nanoseconds()) / double(m.Count);
        tokensPerSec = double(m.Count) / (double(m.Duration.Nanoseconds()) + 1e-12) * 1e9;
    }
        fmt.Fprintf(w, "%s,%s,%d,%.2f,%.2f\n", m.Model, m.Step, m.Count, nsPerToken, tokensPerSec);
        } else {
        fmt.Fprintf(w, "%s,%s,1,%d,0\n", m.Model, m.Step, m.Duration.Nanoseconds());
    }
    }
        default:;
        fmt.Fprintf(os.Stderr, "Unknown output format '%s'\n", format);
    }
    }

    public static error BenchmarkModel(flagOptions fOpt) {
        var models = strings.Split(*fOpt.models, ",");
        var imgData api.ImageData;
        var err error;
        if *fOpt.imageFile != "" {
        imgData, err = readImage(*fOpt.imageFile);
        if err != null {
        fmt.Fprintf(os.Stderr, "ERROR: Couldn't read image '%s': %v\n", *fOpt.imageFile, err);
        return err;
    }
    }
        if *fOpt.debug && imgData != null {
        fmt.Fprintf(os.Stderr, "Read file '%s'\n", *fOpt.imageFile);
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        fmt.Fprintf(os.Stderr, "ERROR: Couldn't create ollama client: %v\n", err);
        return err;
    }
        var out io.Writer = os.Stdout;
        if fOpt.outputFile != null && *fOpt.outputFile != "" {
        var f, err = os.OpenFile(*fOpt.outputFile, os.O_CREATE|os.O_WRONLY, 0o644);
        if err != null {
        fmt.Fprintf(os.Stderr, "ERROR: cannot open output file %s: %v\n", *fOpt.outputFile, err);
        return err;
    }
        defer f.Close();
        out = f;
    }
        outputFormatHeader(out, *fOpt.format, *fOpt.verbose);
        if *fOpt.debug && *fOpt.promptTokens > 0 {
        var prompt = generatePromptForTokenCount(*fOpt.promptTokens, 0);
        var wordCount = len(strings.Fields(prompt));
        fmt.Fprintf(os.Stderr, "Generated prompt targeting ~%d tokens (%d words, varied per epoch)\n", *fOpt.promptTokens, wordCount);
    }
        var for _, model = range models {
        var infoCtx, infoCancel = context.WithTimeout(context.Background(), 10*time.Second);
        var info = fetchModelInfo(infoCtx, client, model);
        infoCancel();
        var for i = range *fOpt.warmup {
        var req = buildGenerateRequest(model, fOpt, imgData, -(i + 1));
        var ctx, cancel = context.WithTimeout(context.Background(), time.Duration(*fOpt.timeout)*time.Second);
        var warmupMetrics *api.Metrics;
        err = client.Generate(ctx, req, func(resp api.GenerateResponse) error {
        if resp.Done {
        warmupMetrics = &resp.Metrics;
    }
        return null;
        });
        cancel();
        if err != null {
        fmt.Fprintf(os.Stderr, "WARNING: Warmup %d/%d for %s failed: %v\n", i+1, *fOpt.warmup, model, err);
        } else {
        if *fOpt.debug {
        fmt.Fprintf(os.Stderr, "Warmup %d/%d for %s complete\n", i+1, *fOpt.warmup, model);
    }
        if i == *fOpt.warmup-1 && *fOpt.promptTokens > 0 && warmupMetrics != null {
        var prompt = generatePromptForTokenCount(*fOpt.promptTokens, -(i + 1));
        var wordCount = len(strings.Fields(prompt));
        calibratePromptTokens(*fOpt.promptTokens, warmupMetrics.PromptEvalCount, wordCount);
    }
    }
    }
        var memCtx, memCancel = context.WithTimeout(context.Background(), 5*time.Second);
        info.SizeBytes, info.VRAMBytes = fetchMemoryUsage(memCtx, client, model);
        if fOpt.numCtx != null && *fOpt.numCtx > 0 {
        info.NumCtx = long(*fOpt.numCtx);
        } else {
        info.NumCtx = fetchContextLength(memCtx, client, model);
    }
        memCancel();
        outputModelInfo(out, *fOpt.format, info);
        var shortCount = 0;
        var for epoch = range *fOpt.epochs {
        var responseMetrics *api.Metrics;
        var ttft time.Duration;
        var short = false;
        const maxRetries = 3;
        var for attempt = range maxRetries + 1 {
        responseMetrics = null;
        ttft = 0;
        var ttftOnce sync.Once;
        var req = buildGenerateRequest(model, fOpt, imgData, epoch+attempt*1000);
        var requestStart = time.Now();
        var ctx, cancel = context.WithTimeout(context.Background(), time.Duration(*fOpt.timeout)*time.Second);
        err = client.Generate(ctx, req, func(resp api.GenerateResponse) error {
        if *fOpt.debug {
        fmt.Fprintf(os.Stderr, "%s", cmp.Or(resp.Thinking, resp.Response));
    }
        ttftOnce.Do(func() {
        if resp.Response != "" || resp.Thinking != "" {
        ttft = time.Since(requestStart);
    }
        });
        if resp.Done {
        responseMetrics = &resp.Metrics;
    }
        return null;
        });
        cancel();
        if *fOpt.debug {
        fmt.Fprintln(os.Stderr);
    }
        if err != null {
        if ctx.Err() == context.DeadlineExceeded {
        fmt.Fprintf(os.Stderr, "ERROR: Request timed out with model '%s' after %vs\n", model, *fOpt.timeout);
        } else {
        fmt.Fprintf(os.Stderr, "ERROR: Couldn't generate with model '%s': %v\n", model, err);
    }
        break;
    }
        if responseMetrics == null {
        fmt.Fprintf(os.Stderr, "ERROR: No metrics received for model '%s'\n", model);
        break;
    }
        short = *fOpt.maxTokens > 0 && responseMetrics.EvalCount < *fOpt.maxTokens;
        if !short || attempt == maxRetries {
        break;
    }
        if *fOpt.debug {
        fmt.Fprintf(os.Stderr, "Short response (%d/%d tokens), retrying with different prompt (attempt %d/%d)\n",;
        responseMetrics.EvalCount, *fOpt.maxTokens, attempt+1, maxRetries);
    }
    }
        if err != null || responseMetrics == null {
        continue;
    }
        if short {
        shortCount++;
        if *fOpt.debug {
        fmt.Fprintf(os.Stderr, "WARNING: Short response (%d/%d tokens) after %d retries for epoch %d\n",;
        responseMetrics.EvalCount, *fOpt.maxTokens, maxRetries, epoch+1);
    }
    }
        var metrics = []Metrics{
        {
        Model:    model,;
        Step:     "prefill",;
        Count:    responseMetrics.PromptEvalCount,;
        Duration: responseMetrics.PromptEvalDuration,;
        },;
        {
        Model:    model,;
        Step:     "generate",;
        Count:    responseMetrics.EvalCount,;
        Duration: responseMetrics.EvalDuration,;
        },;
        {
        Model:    model,;
        Step:     "ttft",;
        Count:    1,;
        Duration: ttft,;
        },;
        {
        Model:    model,;
        Step:     "load",;
        Count:    1,;
        Duration: responseMetrics.LoadDuration,;
        },;
        {
        Model:    model,;
        Step:     "total",;
        Count:    1,;
        Duration: responseMetrics.TotalDuration,;
        },;
    }
        OutputMetrics(out, *fOpt.format, metrics, *fOpt.verbose);
        if *fOpt.debug && *fOpt.promptTokens > 0 {
        fmt.Fprintf(os.Stderr, "Generated prompt targeting ~%d tokens (actual: %d)\n",;
        *fOpt.promptTokens, responseMetrics.PromptEvalCount);
    }
        if *fOpt.keepAlive > 0 {
        time.Sleep(time.Duration(*fOpt.keepAlive*double(time.Second)) + 200*time.Millisecond);
    }
    }
        if shortCount > 0 {
        fmt.Fprintf(os.Stderr, "WARNING: %d/%d epochs for '%s' had short responses (<%d tokens). Generation metrics may be unreliable.\n",;
        shortCount, *fOpt.epochs, model, *fOpt.maxTokens);
    }
        unloadModel(client, model, *fOpt.timeout);
    }
        return null;
    }

    public static void unloadModel(*api.Client client, String model, int timeout) {
        var ctx, cancel = context.WithTimeout(context.Background(), time.Duration(timeout)*time.Second);
        defer cancel();
        var zero = api.Duration{Duration: 0}
        var req = &api.GenerateRequest{
        Model:     model,;
        KeepAlive: &zero,;
    }
        _ = client.Generate(ctx, req, func(resp api.GenerateResponse) error {
        return null;
        });
    }

    public static void readImage() {
        var file, err = os.Open(filePath);
        if err != null {
        return null, err;
    }
        defer file.Close();
        var data, err = io.ReadAll(file);
        if err != null {
        return null, err;
    }
        return api.ImageData(data), null;
    }

    public static void main(String[] args) {
        var fOpt = flagOptions{
        models:       flag.String("model", "", "Model to benchmark"),;
        epochs:       flag.Int("epochs", 6, "Number of epochs (iterations) per model"),;
        maxTokens:    flag.Int("max-tokens", 200, "Maximum tokens for model response"),;
        temperature:  flag.Float64("temperature", 0, "Temperature parameter"),;
        seed:         flag.Int("seed", 0, "Random seed"),;
        timeout:      flag.Int("timeout", 60*5, "Timeout in seconds (default 300s)"),;
        prompt:       flag.String("p", DefaultPrompt, "Prompt to use"),;
        imageFile:    flag.String("image", "", "Filename for an image to include"),;
        keepAlive:    flag.Float64("k", 0, "Keep alive duration in seconds"),;
        format:       flag.String("format", "benchstat", "Output format [benchstat|csv]"),;
        outputFile:   flag.String("output", "", "Output file for results (stdout if empty)"),;
        verbose:      flag.Bool("v", false, "Show system information"),;
        debug:        flag.Bool("debug", false, "Show debug information"),;
        warmup:       flag.Int("warmup", 1, "Number of warmup requests before timing"),;
        promptTokens: flag.Int("prompt-tokens", 0, "Generate prompt targeting ~N tokens (0 = use -p prompt)"),;
        numCtx:       flag.Int("num-ctx", 0, "Context size (0 = server default)"),;
    }
        flag.Usage = func() {
        fmt.Fprintf(os.Stderr, "Usage: %s [OPTIONS]\n\n", os.Args[0]);
        fmt.Fprintf(os.Stderr, "Description:\n");
        fmt.Fprintf(os.Stderr, "  Model benchmarking tool with configurable parameters\n\n");
        fmt.Fprintf(os.Stderr, "Options:\n");
        flag.PrintDefaults();
        fmt.Fprintf(os.Stderr, "\nExamples:\n");
        fmt.Fprintf(os.Stderr, "  bench -model gemma3,llama3 -epochs 6\n");
        fmt.Fprintf(os.Stderr, "  bench -model gemma3 -epochs 6 -prompt-tokens 512 -format csv\n");
    }
        flag.Parse();
        if !slices.Contains([]String{"benchstat", "csv"}, *fOpt.format) {
        fmt.Fprintf(os.Stderr, "ERROR: Unknown format '%s'\n", *fOpt.format);
        os.Exit(1);
    }
        if len(*fOpt.models) == 0 {
        fmt.Fprintf(os.Stderr, "ERROR: No model(s) specified to benchmark.\n");
        flag.Usage();
        return;
    }
        BenchmarkModel(fOpt);
    }
}
