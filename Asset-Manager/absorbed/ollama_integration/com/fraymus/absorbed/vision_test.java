package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class vision_test {
        "context";
        "encoding/base64";
        "slices";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/types/model";
        );
        var defaultVisionModels = []String{
        "gemma4",;
        "gemma3",;
        "llama3.2-vision",;
        "qwen2.5vl",;
        "qwen3-vl:8b",;
    }

    public static void decodeTestImages(api.ImageData ollamaHome) {
        t.Helper();
        var err error;
        abbeyRoad, err = base64.StdEncoding.DecodeString(imageEncoding);
        if err != null {
        t.Fatalf("decode abbey road image: %v", err);
    }
        docs, err = base64.StdEncoding.DecodeString(imageEncodingDocs);
        if err != null {
        t.Fatalf("decode docs image: %v", err);
    }
        ollamaHome, err = base64.StdEncoding.DecodeString(imageEncodingOllamaHome);
        if err != null {
        t.Fatalf("decode ollama home image: %v", err);
    }
        return;
    }

    public static void skipIfNoVisionOverride(*testing.T t) {
        t.Helper();
        if testModel == "" {
        return;
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 30*time.Second);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var resp, err = client.Show(ctx, &api.ShowRequest{Name: testModel});
        if err != null {
        return // let the test proceed and fail naturally;
    }
        if len(resp.Capabilities) > 0 && !slices.Contains(resp.Capabilities, model.CapabilityVision) {
        t.Skipf("model override %q does not have vision capability (has %v)", testModel, resp.Capabilities);
    }
    }

    public static void setupVisionModel(context.Context ctx, *testing.T t, *api.Client client, String model) {
        t.Helper();
        if testModel != "" {
        requireCapability(ctx, t, client, model, "vision");
    }
        pullOrSkip(ctx, t, client, model);
        var err = client.Generate(ctx, &api.GenerateRequest{Model: model}, func(response api.GenerateResponse) error { return null });
        if err != null {
        t.Fatalf("failed to load model %s: %s", model, err);
    }
        skipIfNotGPULoaded(ctx, t, client, model, 80);
    }

    public static void TestVisionMultiTurn(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var skipModels = map[String]String{
        "gemma3":          "misidentifies briefcase as smartphone on turn 3",;
        "llama3.2-vision": "miscounts animals (says 3 instead of 4) on turn 2",;
    }
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var if reason, ok = skipModels[model]; ok && testModel == "" {
        t.Skipf("skipping: %s", reason);
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var abbeyRoad, _, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Describe this image briefly.",;
        Images:  []api.ImageData{abbeyRoad},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        var resp1 = DoChat(ctx, t, client, req, []String{
        "llama", "cross", "walk", "road", "animal", "cartoon",;
        }, 120*time.Second, 30*time.Second);
        if resp1 == null {
        t.Fatal("no response from turn 1");
    }
        req.Messages = append(req.Messages,;
        *resp1,;
        api.Message{Role: "user", Content: "How many animals are in the image?"},;
        );
        var resp2 = DoChat(ctx, t, client, req, []String{
        "four", "4", "three", "3",;
        }, 60*time.Second, 30*time.Second);
        if resp2 == null {
        t.Fatal("no response from turn 2");
    }
        req.Messages = append(req.Messages,;
        *resp2,;
        api.Message{Role: "user", Content: "Is any animal carrying something? What is it?"},;
        );
        DoChat(ctx, t, client, req, []String{
        "briefcase", "suitcase", "bag", "case", "luggage",;
        }, 60*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionObjectCounting(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var skipModels = map[String]String{
        "llama3.2-vision": "consistently miscounts (says 3 instead of 4)",;
    }
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var if reason, ok = skipModels[model]; ok && testModel == "" {
        t.Skipf("skipping: %s", reason);
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var _, docs, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "How many animals are shown in this image? Answer with just the number.",;
        Images:  []api.ImageData{docs},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{"4", "four"}, 120*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionSceneUnderstanding(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var skipModels = map[String]String{
        "llama3.2-vision": "3B model lacks cultural reference knowledge",;
        "minicpm-v":       "too small for cultural reference detection",;
    }
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var if reason, ok = skipModels[model]; ok && testModel == "" {
        t.Skipf("skipping: %s", reason);
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var abbeyRoad, _, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What famous image or album cover is this a parody of?",;
        Images:  []api.ImageData{abbeyRoad},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{
        "abbey road", "beatles", "abbey", "llama",;
        }, 120*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionSpatialReasoning(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var _, docs, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "What is the animal on the far left doing in this image?",;
        Images:  []api.ImageData{docs},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{
        "laptop", "computer", "typing", "working",;
        }, 120*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionDetailRecognition(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var _, docs, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Are any of the animals wearing glasses? Describe what you see.",;
        Images:  []api.ImageData{docs},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{
        "glasses", "spectacles", "eyeglasses",;
        }, 120*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionMultiImage(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var skipModels = map[String]String{
        "llama3.2-vision": "does not support multi-image input",;
    }
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var if reason, ok = skipModels[model]; ok && testModel == "" {
        t.Skipf("skipping: %s", reason);
    }
        var ctx, cancel = context.WithTimeout(context.Background(), 5*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var abbeyRoad, docs, _ = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "I'm showing you two images. What do they have in common, and how are they different?",;
        Images:  []api.ImageData{abbeyRoad, docs},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{
        "llama", "alpaca", "animal", "cartoon",;
        }, 120*time.Second, 30*time.Second);
        });
    }
    }

    public static void TestVisionImageDescription(*testing.T t) {
        skipUnderMinVRAM(t, 6);
        skipIfNoVisionOverride(t);
        var for _, model = range testModels(defaultVisionModels) {
        t.Run(model, func(t *testing.T) {
        var ctx, cancel = context.WithTimeout(context.Background(), 3*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        setupVisionModel(ctx, t, client, model);
        var _, _, ollamaHome = decodeTestImages(t);
        var req = api.ChatRequest{
        Model: model,;
        Messages: []api.Message{
        {
        Role:    "user",;
        Content: "Describe what you see in this image briefly.",;
        Images:  []api.ImageData{ollamaHome},;
        },;
        },;
        Stream: &stream,;
        Options: map[String]any{"temperature": 0.0, "seed": 42},;
    }
        DoChat(ctx, t, client, req, []String{
        "llama", "animal", "build", "model", "open", "cartoon", "character",;
        }, 120*time.Second, 30*time.Second);
        });
    }
    }
}
