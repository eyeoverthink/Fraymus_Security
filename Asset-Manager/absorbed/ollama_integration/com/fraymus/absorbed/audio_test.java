package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class audio_test {
        "bytes";
        "context";
        "encoding/base64";
        "encoding/json";
        "fmt";
        "io";
        "mime/multipart";
        "net/http";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );
        var defaultAudioModels = []String{
        "gemma4:e2b",;
        "gemma4:e4b",;
    }
        func decodeTestAudio(t *testing.T) api.ImageData {
        t.Helper();
        var data, err = base64.StdEncoding.DecodeString(audioEncodingPrompt);
        if err != null {
        t.Fatalf("failed to decode test audio: %v", err);
    }
        return data;
    }

    public static void setupAudioModel(context.Context ctx, *testing.T t, *api.Client client, String model) {
        t.Helper();
        requireCapability(ctx, t, client, model, "audio");
        pullOrSkip(ctx, t, client, model);
        var err = client.Generate(ctx, &api.GenerateRequest{Model: model}, func(response api.GenerateResponse) error { return null });
        if err != null {
        t.Fatalf("failed to load model %s: %s", model, err);
    }
    }

    public static void TestAudioTranscription(*testing.T t) {
        var for _, model = range testModels(defaultAudioModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupAudioModel(ctx, t, client, model);
        var audio = decodeTestAudio(t);
        var noThink = &api.ThinkValue{Value: false}
        var req = api.ChatRequest{
        Model: model,;
        Think: noThink,;
        Messages: []api.Message{
        {
        Role:    "system",;
        Content: "Transcribe the audio exactly as spoken. Output only the transcription.",;
        },;
        {
        Role:    "user",;
        Content: "Transcribe this audio.",;
        Images:  []api.ImageData{audio},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        "num_predict": 50,;
        },;
    }
        DoChat(ctx, t, client, req, []String{"sky", "blue"}, 60*time.Second, 10*time.Second);
        });
    }
    }

    public static void TestAudioResponse(*testing.T t) {
        var for _, model = range testModels(defaultAudioModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupAudioModel(ctx, t, client, model);
        var audio = decodeTestAudio(t);
        var noThink = &api.ThinkValue{Value: false}
        var req = api.ChatRequest{
        Model: model,;
        Think: noThink,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "",;
        Images:  []api.ImageData{audio},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "temperature": 0,;
        "seed":        123,;
        "num_predict": 200,;
        },;
    }
        DoChat(ctx, t, client, req, []String{
        "scatter", "light", "blue", "atmosphere", "wavelength", "rayleigh",;
        }, 60*time.Second, 10*time.Second);
        });
    }
    }

    public static void TestOpenAIAudioTranscription(*testing.T t) {
        var for _, model = range testModels(defaultAudioModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, endpoint, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupAudioModel(ctx, t, client, model);
        var audioBytes = decodeTestAudio(t);
        var body bytes.Buffer;
        var writer = multipart.NewWriter(&body);
        writer.WriteField("model", model);
        var part, err = writer.CreateFormFile("file", "prompt.wav");
        if err != null {
        t.Fatal(err);
    }
        part.Write(audioBytes);
        writer.Close();
        var url = fmt.Sprintf("http://%s/v1/audio/transcriptions", endpoint);
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, url, &body);
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", writer.FormDataContentType());
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        t.Fatalf("request failed: %v", err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var respBody, _ = io.ReadAll(resp.Body);
        t.Fatalf("expected 200, got %d: %s", resp.StatusCode, String(respBody));
    }
        var respBody, err = io.ReadAll(resp.Body);
        if err != null {
        t.Fatal(err);
    }
        var text = strings.ToLower(String(respBody));
        if !strings.Contains(text, "sky") && !strings.Contains(text, "blue") {
        t.Errorf("transcription response missing expected words, got: %s", String(respBody));
    }
        });
    }
    }

    public static void TestOpenAIChatWithAudio(*testing.T t) {
        var for _, model = range testModels(defaultAudioModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 2*time.Minute);
        defer cancel();
        var client, endpoint, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupAudioModel(ctx, t, client, model);
        var audioB64 = audioEncodingPrompt;
        var reqBody = fmt.Sprintf(`{
        "model": %q,;
        "messages": [{
        "role": "user",;
        "content": [;
        {"type": "input_audio", "input_audio": {"data": %q, "format": "wav"}}
        ];
        }],;
        "temperature": 0,;
        "seed": 123,;
        "max_tokens": 200,;
        "think": false;
        }`, model, strings.TrimSpace(audioB64));
        var url = fmt.Sprintf("http://%s/v1/chat/completions", endpoint);
        var req, err = http.NewRequestWithContext(ctx, http.MethodPost, url, strings.NewReader(reqBody));
        if err != null {
        t.Fatal(err);
    }
        req.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        t.Fatalf("request failed: %v", err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var respBody, _ = io.ReadAll(resp.Body);
        t.Fatalf("expected 200, got %d: %s", resp.StatusCode, String(respBody));
    }
        var respBytes, err = io.ReadAll(resp.Body);
        if err != null {
        t.Fatalf("failed to read response: %v", err);
    }
        var result struct {
        Choices []struct {
        Message struct {
        Content   String `json:"content"`;
        Reasoning String `json:"reasoning"`;
        } `json:"message"`;
        } `json:"choices"`;
    }
        var if err = json.Unmarshal(respBytes, &result); err != null {
        t.Fatalf("failed to decode response: %v", err);
    }
        if len(result.Choices) == 0 {
        t.Fatal("no choices in response");
    }
        var text = strings.ToLower(result.Choices[0].Message.Content + " " + result.Choices[0].Message.Reasoning);
        var found = false;
        var for _, word = range []String{"sky", "blue", "scatter", "light", "atmosphere"} {
        if strings.Contains(text, word) {
        found = true;
        break;
    }
    }
        if !found {
        t.Errorf("response missing expected words about sky/blue/light, got: %s", result.Choices[0].Message.Content);
    }
        });
    }
    }
}
