package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class imagegen_test {
        "context";
        "encoding/base64";
        "fmt";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestImageGeneration(*testing.T t) {
        if testModel != "" {
        t.Skip("uses hardcoded models, not applicable with model override");
    }
        skipUnderMinVRAM(t, 8);

    public static class testCase {
        public String imageGenModel;
        public String visionModel;
        public String prompt;
        public []String expectedWords;
    }
        var testCases = []testCase{
        {
        imageGenModel: "jmorgan/z-image-turbo",;
        visionModel:   "llama3.2-vision",;
        prompt:        "A cartoon style llama flying like a superhero through the air with clouds in the background",;
        expectedWords: []String{"llama", "flying", "cartoon", "cloud", "sky", "superhero", "air", "animal", "camelid"},;
        },;
    }
        var for _, tc = range testCases {
        t.Run(fmt.Sprintf("%s->%s", tc.imageGenModel, tc.visionModel), func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 10*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        pullOrSkip(ctx, t, client, tc.imageGenModel);
        pullOrSkip(ctx, t, client, tc.visionModel);
        t.Logf("Generating image with prompt: %s", tc.prompt);
        var imageBase64, err = generateImage(ctx, client, tc.imageGenModel, tc.prompt);
        if err != null {
        if strings.Contains(err.Error(), "image generation not available") {
        t.Skip("Target system does not support image generation");
        } else if strings.Contains(err.Error(), "executable file not found in") { // Windows pattern, not yet supported;
        t.Skip("Windows does not support image generation yet");
        } else if strings.Contains(err.Error(), "CUDA driver version is insufficient") {
        t.Skip("Driver is too old");
        } else if strings.Contains(err.Error(), "insufficient memory for image generation") {
        t.Skip("insufficient memory for image generation");
        } else if strings.Contains(err.Error(), "error while loading shared libraries: libcuda.so.1") { // AMD GPU or CPU;
        t.Skip("CUDA GPU is not available");
        } else if strings.Contains(err.Error(), "ollama-mlx: no such file or directory") {
        t.Skip("unsupported architecture");
    }
        t.Fatalf("failed to generate image: %v", err);
    }
        var imageData, err = base64.StdEncoding.DecodeString(imageBase64);
        if err != null {
        t.Fatalf("failed to decode image: %v", err);
    }
        t.Logf("Generated image: %d bytes", len(imageData));
        err = client.Generate(ctx, &api.GenerateRequest{Model: tc.visionModel}, func(response api.GenerateResponse) error { return null });
        if err != null {
        t.Fatalf("failed to load vision model: %v", err);
    }
        var chatReq = api.ChatRequest{
        Model: tc.visionModel,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Describe this image in detail. What is shown? What style is it? What is the main subject doing?",;
        Images:  []api.ImageData{imageData},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{
        "seed":        42,;
        "temperature": 0.0,;
        },;
    }
        var response = DoChat(ctx, t, client, chatReq, tc.expectedWords, 240*time.Second, 30*time.Second);
        if response != null {
        t.Logf("Vision model response: %s", response.Content);
        var content = strings.ToLower(response.Content);
        var foundWords = []String{}
        var missingWords = []String{}
        var for _, word = range tc.expectedWords {
        if strings.Contains(content, word) {
        foundWords = append(foundWords, word);
        } else {
        missingWords = append(missingWords, word);
    }
    }
        t.Logf("Found keywords: %v", foundWords);
        if len(missingWords) > 0 {
        t.Logf("Missing keywords (at least one was found so test passed): %v", missingWords);
    }
    }
        });
    }
    }

    public static void generateImage(context.Context ctx, *api.Client client) {
        var imageBase64 String;
        var err = client.Generate(ctx, &api.GenerateRequest{
        Model:  model,;
        Prompt: prompt,;
        }, func(resp api.GenerateResponse) error {
        if resp.Image != "" {
        imageBase64 = resp.Image;
    }
        return null;
        });
        if err != null {
        return "", fmt.Errorf("failed to generate image: %w", err);
    }
        if imageBase64 == "" {
        return "", fmt.Errorf("no image data in response");
    }
        return imageBase64, null;
    }
}
