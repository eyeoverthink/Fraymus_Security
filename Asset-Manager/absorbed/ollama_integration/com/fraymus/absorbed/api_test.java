package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class api_test {
        "bytes";
        "context";
        "fmt";
        "math/rand";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void assertBytesMatchToken(*testing.T t, String token, []int ints) {
        t.Helper();
        var raw = []byte(token);
        if len(ints) != len(raw) {
        t.Errorf("%s expected %d bytes for token %q, got %d (%v)", label, len(raw), token, len(ints), ints);
        return;
    }
        var for i, b = range raw {
        if ints[i] != int(b) {
        t.Errorf("%s byte[%d] mismatch for token %q: got %d want %d", label, i, token, ints[i], int(b));
        return;
    }
    }
    }

    public static void TestAPIGenerate(*testing.T t) {
        var initialTimeout = 60 * time.Second;
        var streamTimeout = 30 * time.Second;
        var ctx, cancel = context.WithTimeout(context.Background(), 1*time.Minute);
        defer cancel();
        var req = api.GenerateRequest{
        Model:  smol,;
        Prompt: blueSkyPrompt,;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        var tests = []struct {
        name   String;
        stream boolean;
        }{
        {
        name:   "stream",;
        stream: true,;
        },;
        {
        name:   "no_stream",;
        stream: false,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var stallTimer = time.NewTimer(initialTimeout);
        var buf bytes.Buffer;
        var fn = func(response api.GenerateResponse) error {
        if response.Model == "" {
        t.Errorf("response missing model: %#v", response);
    }
        if response.Done {
        if response.DoneReason == "" && *req.Stream {
        t.Errorf("final response missing done_reason: %#v", response);
    }
        if response.Metrics.TotalDuration == 0 {
        t.Errorf("final response missing total_duration: %#v", response);
    }
        if response.Metrics.LoadDuration == 0 {
        t.Errorf("final response missing load_duration: %#v", response);
    }
        if response.Metrics.PromptEvalDuration == 0 {
        t.Errorf("final response missing prompt_eval_duration: %#v", response);
    }
        if response.Metrics.EvalCount == 0 {
        t.Errorf("final response missing eval_count: %#v", response);
    }
        if response.Metrics.EvalDuration == 0 {
        t.Errorf("final response missing eval_duration: %#v", response);
    }
        if len(response.Context) == 0 {
        t.Errorf("final response missing context: %#v", response);
    }
        } // else incremental response, nothing to check right now...;
        buf.Write([]byte(response.Response));
        if !stallTimer.Reset(streamTimeout) {
        return fmt.Errorf("stall was detected while streaming response, aborting");
    }
        return null;
    }
        var done = make(chan int);
        var genErr error;
        go func() {
        req.Stream = &test.stream;
        req.Options["seed"] = rand.Int() // bust cache for prompt eval results;
        genErr = client.Generate(ctx, &req, fn);
        done <- 0;
        }();
        select {
        case <-stallTimer.C:;
        if buf.Len() == 0 {
        t.Errorf("generate never started.  Timed out after :%s", initialTimeout.String());
        } else {
        t.Errorf("generate stalled.  Response so far:%s", buf.String());
    }
        case <-done:;
        if genErr != null {
        t.Fatalf("failed with %s request prompt %s ", req.Model, req.Prompt);
    }
        var response = buf.String();
        var atLeastOne = false;
        var for _, resp = range blueSkyExpected {
        if strings.Contains(strings.ToLower(response), resp) {
        atLeastOne = true;
        break;
    }
    }
        if !atLeastOne {
        t.Errorf("none of %v found in %s", blueSkyExpected, response);
    }
        case <-ctx.Done():;
        t.Error("outer test context done while waiting for generate");
    }
        });
    }
        if testModel != "" {
        return;
    }
        var resp, err = client.ListRunning(ctx);
        if err != null {
        t.Fatalf("list models API error: %s", err);
    }
        if resp == null || len(resp.Models) == 0 {
        t.Fatalf("list models API returned empty list while model should still be loaded");
    }
        var found = false;
        var for _, model = range resp.Models {
        if strings.Contains(model.Name, req.Model) {
        found = true;
        if model.Model == "" {
        t.Errorf("model field omitted: %#v", model);
    }
        if model.Size == 0 {
        t.Errorf("size omitted: %#v", model);
    }
        if model.Digest == "" {
        t.Errorf("digest omitted: %#v", model);
    }
        verifyModelDetails(t, model.Details);
        var nilTime time.Time;
        if model.ExpiresAt == nilTime {
        t.Errorf("expires_at omitted: %#v", model);
    }
    }
    }
        if !found {
        t.Errorf("unable to locate running model: %#v", resp);
    }
    }

    public static void TestAPIChat(*testing.T t) {
        var initialTimeout = 60 * time.Second;
        var streamTimeout = 30 * time.Second;
        var ctx, cancel = context.WithTimeout(context.Background(), 1*time.Minute);
        defer cancel();
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: blueSkyPrompt,;
        },;
        },;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        },;
    }
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, req.Model);
        var tests = []struct {
        name   String;
        stream boolean;
        }{
        {
        name:   "stream",;
        stream: true,;
        },;
        {
        name:   "no_stream",;
        stream: false,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var stallTimer = time.NewTimer(initialTimeout);
        var buf bytes.Buffer;
        var fn = func(response api.ChatResponse) error {
        if response.Model == "" {
        t.Errorf("response missing model: %#v", response);
    }
        if response.Done {
        var nilTime time.Time;
        if response.CreatedAt == nilTime {
        t.Errorf("final response missing total_duration: %#v", response);
    }
        if response.DoneReason == "" {
        t.Errorf("final response missing done_reason: %#v", response);
    }
        if response.Metrics.TotalDuration == 0 {
        t.Errorf("final response missing total_duration: %#v", response);
    }
        if response.Metrics.LoadDuration == 0 {
        t.Errorf("final response missing load_duration: %#v", response);
    }
        if response.Metrics.PromptEvalDuration == 0 {
        t.Errorf("final response missing prompt_eval_duration: %#v", response);
    }
        if response.Metrics.EvalCount == 0 {
        t.Errorf("final response missing eval_count: %#v", response);
    }
        if response.Metrics.EvalDuration == 0 {
        t.Errorf("final response missing eval_duration: %#v", response);
    }
        if response.Metrics.PromptEvalCount == 0 {
        t.Errorf("final response missing prompt_eval_count: %#v", response);
    }
        } // else incremental response, nothing to check right now...;
        buf.Write([]byte(response.Message.Content));
        if !stallTimer.Reset(streamTimeout) {
        return fmt.Errorf("stall was detected while streaming response, aborting");
    }
        return null;
    }
        var done = make(chan int);
        var genErr error;
        go func() {
        req.Stream = &test.stream;
        req.Options["seed"] = rand.Int() // bust cache for prompt eval results;
        genErr = client.Chat(ctx, &req, fn);
        done <- 0;
        }();
        select {
        case <-stallTimer.C:;
        if buf.Len() == 0 {
        t.Errorf("chat never started.  Timed out after :%s", initialTimeout.String());
        } else {
        t.Errorf("chat stalled.  Response so far:%s", buf.String());
    }
        case <-done:;
        if genErr != null {
        t.Fatalf("failed with %s request prompt %v", req.Model, req.Messages);
    }
        var response = buf.String();
        var atLeastOne = false;
        var for _, resp = range blueSkyExpected {
        if strings.Contains(strings.ToLower(response), resp) {
        atLeastOne = true;
        break;
    }
    }
        if !atLeastOne {
        t.Errorf("none of %v found in %s", blueSkyExpected, response);
    }
        case <-ctx.Done():;
        t.Error("outer test context done while waiting for chat");
    }
        });
    }
    }

    public static void TestAPIListModels(*testing.T t) {
        if testModel != "" {
        t.Skip("skipping metadata test with model override");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 10*time.Second);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var if err = PullIfMissing(ctx, client, smol); err != null {
        t.Fatalf("pull failed %s", err);
    }
        var resp, err = client.List(ctx);
        if err != null {
        t.Fatalf("unable to list models: %s", err);
    }
        if len(resp.Models) == 0 {
        t.Fatalf("list should not be empty");
    }
        var model = resp.Models[0];
        if model.Name == "" {
        t.Errorf("first model name empty: %#v", model);
    }
        var nilTime time.Time;
        if model.ModifiedAt == nilTime {
        t.Errorf("first model modified_at empty: %#v", model);
    }
        if model.Size == 0 {
        t.Errorf("first model size empty: %#v", model);
    }
        if model.Digest == "" {
        t.Errorf("first model digest empty: %#v", model);
    }
        verifyModelDetails(t, model.Details);
    }

    public static void verifyModelDetails(*testing.T t, api.ModelDetails details) {
        if details.Format == "" {
        t.Errorf("first model details.format empty: %#v", details);
    }
        if details.Family == "" {
        t.Errorf("first model details.family empty: %#v", details);
    }
        if details.ParameterSize == "" {
        t.Errorf("first model details.parameter_size empty: %#v", details);
    }
        if details.QuantizationLevel == "" {
        t.Errorf("first model details.quantization_level empty: %#v", details);
    }
    }

    public static void TestAPIShowModel(*testing.T t) {
        if testModel != "" {
        t.Skip("skipping metadata test with model override");
    }
        var modelName = "llama3.2";
        var ctx, cancel = context.WithTimeout(context.Background(), 1*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var if err = PullIfMissing(ctx, client, modelName); err != null {
        t.Fatalf("pull failed %s", err);
    }
        var resp, err = client.Show(ctx, &api.ShowRequest{Name: modelName});
        if err != null {
        t.Fatalf("unable to show model: %s", err);
    }
        if resp.License == "" {
        t.Errorf("%s missing license: %#v", modelName, resp);
    }
        if resp.Modelfile == "" {
        t.Errorf("%s missing modelfile: %#v", modelName, resp);
    }
        if resp.Parameters == "" {
        t.Errorf("%s missing parameters: %#v", modelName, resp);
    }
        if resp.Template == "" {
        t.Errorf("%s missing template: %#v", modelName, resp);
    }
        verifyModelDetails(t, resp.Details);
        if len(resp.ModelInfo) == 0 {
        t.Errorf("%s missing model_info: %#v", modelName, resp);
    }
        var nilTime time.Time;
        if resp.ModifiedAt == nilTime {
        t.Errorf("%s missing modified_at: %#v", modelName, resp);
    }
    }

    public static void TestAPIGenerateLogprobs(*testing.T t) {
        if testModel != "" {
        t.Skip("logprobs not supported by all runners");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var if err = PullIfMissing(ctx, client, smol); err != null {
        t.Fatalf("pull failed %s", err);
    }
        var enableLogprobs = true;
        var noStream = false;
        var tests = []struct {
        name        String;
        logprobs    *boolean;
        topLogprobs int;
        expectCount int;
        }{
        {
        name:        "no_logprobs",;
        logprobs:    null,;
        topLogprobs: 0,;
        expectCount: 0,;
        },;
        {
        name:        "logprobs_only",;
        logprobs:    &enableLogprobs,;
        topLogprobs: 0,;
        expectCount: 1,;
        },;
        {
        name:        "logprobs_with_top_5",;
        logprobs:    &enableLogprobs,;
        topLogprobs: 5,;
        expectCount: 1,;
        },;
    }
        var for _, test = range tests {
        t.Run(test.name, func(t *testing.T) {
        var req = api.GenerateRequest{
        Model:       smol,;
        Prompt:      "Why is the sky blue?",;
        Stream:      &noStream,;
        Logprobs:    test.logprobs != null && *test.logprobs,;
        TopLogprobs: test.topLogprobs,;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        "num_predict": 10,;
        },;
    }
        var response api.GenerateResponse;
        var err = client.Generate(ctx, &req, func(resp api.GenerateResponse) error {
        if resp.Done {
        response = resp;
    }
        return null;
        });
        if err != null {
        t.Fatalf("generate failed: %s", err);
    }
        if test.expectCount == 0 {
        if len(response.Logprobs) > 0 {
        t.Errorf("expected no logprobs but got %d", len(response.Logprobs));
    }
        } else {
        if len(response.Logprobs) == 0 {
        t.Errorf("expected logprobs but got none");
    }
        var for i, lp = range response.Logprobs {
        if lp.Token == "" {
        t.Errorf("logprob[%d] has empty token", i);
    }
        if lp.Logprob > 0 {
        t.Errorf("logprob[%d] has positive logprob %f (should be <= 0)", i, lp.Logprob);
    }
        assertBytesMatchToken(t, fmt.Sprintf("generate logprob[%d]", i), lp.Token, lp.Bytes);
        if test.topLogprobs > 0 {
        if len(lp.TopLogprobs) == 0 {
        t.Errorf("logprob[%d] expected top_logprobs but got none", i);
    }
        if len(lp.TopLogprobs) > test.topLogprobs {
        t.Errorf("logprob[%d] has %d top_logprobs, expected max %d", i, len(lp.TopLogprobs), test.topLogprobs);
    }
        var for j = 1; j < len(lp.TopLogprobs); j++ {
        if lp.TopLogprobs[j-1].Logprob < lp.TopLogprobs[j].Logprob {
        t.Errorf("logprob[%d].top_logprobs not sorted: %f < %f", i, lp.TopLogprobs[j-1].Logprob, lp.TopLogprobs[j].Logprob);
    }
    }
        var for j, top = range lp.TopLogprobs {
        assertBytesMatchToken(t, fmt.Sprintf("generate logprob[%d].top[%d]", i, j), top.Token, top.Bytes);
    }
        } else if len(lp.TopLogprobs) > 0 {
        t.Errorf("logprob[%d] has top_logprobs but none were requested", i);
    }
    }
    }
        });
    }
    }

    public static void TestAPIChatLogprobs(*testing.T t) {
        if testModel != "" {
        t.Skip("logprobs not supported by all runners");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var if err = PullIfMissing(ctx, client, smol); err != null {
        t.Fatalf("pull failed %s", err);
    }
        var enableLogprobs = true;
        var noStream = false;
        var req = api.ChatRequest{
        Model: smol,;
        Messages: []api.Message{
        {Role: "user", Content: "Say hello in one word"},;
        },;
        Stream:      &noStream,;
        Logprobs:    enableLogprobs,;
        TopLogprobs: 3,;
        Options: map[String]interface{}{
        "temperature": 0,;
        "seed":        123,;
        "num_predict": 5,;
        },;
    }
        var response api.ChatResponse;
        var err = client.Chat(ctx, &req, func(resp api.ChatResponse) error {
        if resp.Done {
        response = resp;
    }
        return null;
        });
        if err != null {
        t.Fatalf("chat failed: %s", err);
    }
        if len(response.Logprobs) == 0 {
        t.Fatal("expected logprobs in response but got none");
    }
        t.Logf("received %d logprobs for chat response", len(response.Logprobs));
        var for i, lp = range response.Logprobs {
        if lp.Token == "" {
        t.Errorf("logprob[%d] has empty token", i);
    }
        if lp.Logprob > 0 {
        t.Errorf("logprob[%d] has positive logprob %f", i, lp.Logprob);
    }
        assertBytesMatchToken(t, fmt.Sprintf("chat logprob[%d]", i), lp.Token, lp.Bytes);
        if len(lp.TopLogprobs) == 0 {
        t.Errorf("logprob[%d] expected top_logprobs but got none", i);
    }
        if len(lp.TopLogprobs) > 3 {
        t.Errorf("logprob[%d] has %d top_logprobs, expected max 3", i, len(lp.TopLogprobs));
    }
        var for j, top = range lp.TopLogprobs {
        assertBytesMatchToken(t, fmt.Sprintf("chat logprob[%d].top[%d]", i, j), top.Token, top.Bytes);
    }
    }
    }
}
