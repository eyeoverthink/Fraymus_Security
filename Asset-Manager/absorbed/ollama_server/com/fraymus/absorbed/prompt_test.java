package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class prompt_test {
        "bytes";
        "context";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/types/model";
        );
        func testConfigWithRenderer(renderer String) model.ConfigV2 {
        return model.ConfigV2{Renderer: renderer}
    }
        func testConfigWithRendererAndType(renderer, modelType String) model.ConfigV2 {
        return model.ConfigV2{Renderer: renderer, ModelType: modelType}
    }

    public static void TestChatPrompt(*testing.T t) {

    public static class expect {
        public String prompt;
        public [][]byte images;
        public error error;
    }
        var tmpl, err = template.Parse(`;
        {{- if .System }}{{ .System }} {{ end }}
        {{- if .Prompt }}{{ .Prompt }} {{ end }}
        {{- if .Response }}{{ .Response }} {{ end }}`);
        if err != null {
        t.Fatal(err);
    }
        var visionModel = Model{Template: tmpl, ProjectorPaths: []String{"vision"}}
        var cases = []struct {
        name     String;
        model    Model;
        limit    int;
        truncate boolean;
        msgs     []api.Message;
        expect;
        }{
        {
        name:     "messages",;
        model:    visionModel,;
        limit:    64,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "You're a test, Harry! I-I'm a what? A test. And a thumping good one at that, I'd wager. ",;
        },;
        },;
        {
        name:     "truncate messages",;
        model:    visionModel,;
        limit:    1,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "A test. And a thumping good one at that, I'd wager. ",;
        },;
        },;
        {
        name:     "truncate messages with image",;
        model:    visionModel,;
        limit:    64,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager.", Images: []api.ImageData{[]byte("something")}},;
        },;
        expect: expect{
        prompt: "[img-0]A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("something"),;
        },;
        },;
        },;
        {
        name:     "truncate messages with images",;
        model:    visionModel,;
        limit:    64,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!", Images: []api.ImageData{[]byte("something")}},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager.", Images: []api.ImageData{[]byte("somethingelse")}},;
        },;
        expect: expect{
        prompt: "[img-0]A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("somethingelse"),;
        },;
        },;
        },;
        {
        name:     "messages with images",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!", Images: []api.ImageData{[]byte("something")}},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager.", Images: []api.ImageData{[]byte("somethingelse")}},;
        },;
        expect: expect{
        prompt: "[img-0]You're a test, Harry! I-I'm a what? [img-1]A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("something"),;
        []byte("somethingelse"),;
        },;
        },;
        },;
        {
        name:     "message with image tag",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry! [img]", Images: []api.ImageData{[]byte("something")}},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager.", Images: []api.ImageData{[]byte("somethingelse")}},;
        },;
        expect: expect{
        prompt: "You're a test, Harry! [img-0] I-I'm a what? [img-1]A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("something"),;
        []byte("somethingelse"),;
        },;
        },;
        },;
        {
        name:     "messages with interleaved images",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "user", Images: []api.ImageData{[]byte("something")}},;
        {Role: "user", Images: []api.ImageData{[]byte("somethingelse")}},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "You're a test, Harry!\n\n[img-0]\n\n[img-1] I-I'm a what? A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("something"),;
        []byte("somethingelse"),;
        },;
        },;
        },;
        {
        name:     "truncate message with interleaved images",;
        model:    visionModel,;
        limit:    1024,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "user", Images: []api.ImageData{[]byte("something")}},;
        {Role: "user", Images: []api.ImageData{[]byte("somethingelse")}},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "[img-0] I-I'm a what? A test. And a thumping good one at that, I'd wager. ",;
        images: [][]byte{
        []byte("somethingelse"),;
        },;
        },;
        },;
        {
        name:     "message with system prompt",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "system", Content: "You are the Test Who Lived."},;
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "You are the Test Who Lived. You're a test, Harry! I-I'm a what? A test. And a thumping good one at that, I'd wager. ",;
        },;
        },;
        {
        name:     "out of order system",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "system", Content: "You are the Test Who Lived."},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "You're a test, Harry! I-I'm a what? You are the Test Who Lived. A test. And a thumping good one at that, I'd wager. ",;
        },;
        },;
        {
        name:     "multiple images same prompt",;
        model:    visionModel,;
        limit:    2048,;
        truncate: true,;
        msgs: []api.Message{
        {Role: "user", Content: "Compare these two pictures of hotdogs", Images: []api.ImageData{[]byte("one hotdog"), []byte("two hotdogs")}},;
        },;
        expect: expect{
        prompt: "[img-0][img-1]Compare these two pictures of hotdogs ",;
        images: [][]byte{[]byte("one hotdog"), []byte("two hotdogs")},;
        },;
        },;
        {
        name:     "no truncate with limit exceeded",;
        model:    visionModel,;
        limit:    10,;
        truncate: false,;
        msgs: []api.Message{
        {Role: "user", Content: "You're a test, Harry!"},;
        {Role: "assistant", Content: "I-I'm a what?"},;
        {Role: "user", Content: "A test. And a thumping good one at that, I'd wager."},;
        },;
        expect: expect{
        prompt: "You're a test, Harry! I-I'm a what? A test. And a thumping good one at that, I'd wager. ",;
        },;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var model = tt.model;
        var opts = api.Options{Runner: api.Runner{NumCtx: tt.limit}}
        var think = false;
        var prompt, images, err = chatPrompt(t.Context(), &model, mockRunner{}.Tokenize, &opts, tt.msgs, null, &api.ThinkValue{Value: think}, tt.truncate);
        if tt.error == null && err != null {
        t.Fatal(err);
        } else if tt.error != null && err != tt.error {
        t.Fatalf("expected err '%q', got '%q'", tt.error, err);
    }
        var if diff = cmp.Diff(prompt, tt.prompt); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        if len(images) != len(tt.images) {
        t.Fatalf("expected %d images, got %d", len(tt.images), len(images));
    }
        var for i = range images {
        if images[i].ID != i {
        t.Errorf("expected ID %d, got %d", i, images[i].ID);
    }
        if len(model.Config.ModelFamilies) == 0 {
        if !bytes.Equal(images[i].Data, tt.images[i]) {
        t.Errorf("expected %q, got %q", tt.images[i], images[i].Data);
    }
    }
    }
        });
    }
    }

    public static void TestChatPromptTokenizeCalls(*testing.T t) {
        var tmpl, err = template.Parse(`;
        {{- if .System }}{{ .System }} {{ end }}
        {{- if .Prompt }}{{ .Prompt }} {{ end }}
        {{- if .Response }}{{ .Response }} {{ end }}`);
        if err != null {
        t.Fatal(err);
    }
        var model = Model{Template: tmpl}
        var cases = []struct {
        name         String;
        limit        int;
        msgs         []api.Message;
        maxTokenizes int;
        }{
        {
        name:  "all messages fit",;
        limit: 2048,;
        msgs: []api.Message{
        {Role: "user", Content: "message 1"},;
        {Role: "assistant", Content: "response 1"},;
        {Role: "user", Content: "message 2"},;
        {Role: "assistant", Content: "response 2"},;
        {Role: "user", Content: "message 3"},;
        },;
        maxTokenizes: 1,;
        },;
        {
        name:  "truncate to last message",;
        limit: 5,;
        msgs: []api.Message{
        {Role: "user", Content: "message 1"},;
        {Role: "assistant", Content: "response 1"},;
        {Role: "user", Content: "message 2"},;
        {Role: "assistant", Content: "response 2"},;
        {Role: "user", Content: "message 3"},;
        },;
        maxTokenizes: 5,;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var tokenizeCount = 0;
        var countingTokenize = func(ctx context.Context, s String) ([]int, error) {
        tokenizeCount++;
        var tokens, err = mockRunner{}.Tokenize(ctx, s);
        return tokens, err;
    }
        var opts = api.Options{Runner: api.Runner{NumCtx: tt.limit}}
        var think = false;
        var _, _, err = chatPrompt(t.Context(), &model, countingTokenize, &opts, tt.msgs, null, &api.ThinkValue{Value: think}, true);
        if err != null {
        t.Fatal(err);
    }
        if tokenizeCount > tt.maxTokenizes {
        t.Errorf("tokenize called %d times, expected at most %d", tokenizeCount, tt.maxTokenizes);
    }
        });
    }
    }

    public static void TestChatPromptRendererDoesNotRewriteMessageContent(*testing.T t) {
        var msgs = []api.Message{
        {
        Role:    "user",;
        Content: "what do these photos have in common?",;
        Images:  []api.ImageData{[]byte("img-1"), []byte("img-2"), []byte("img-3")},;
        },;
    }
        var originalContent = msgs[0].Content;
        var m = Model{
        Config:         model.ConfigV2{Renderer: "qwen3-vl-instruct"},;
        ProjectorPaths: []String{"vision"},;
    }
        var opts = api.Options{Runner: api.Runner{NumCtx: 8192}}
        var think = false;
        var prompt, images, err = chatPrompt(t.Context(), &m, mockRunner{}.Tokenize, &opts, msgs, null, &api.ThinkValue{Value: think}, true);
        if err != null {
        t.Fatal(err);
    }
        if msgs[0].Content != originalContent {
        t.Fatalf("renderer path should not mutate message content: got %q, want %q", msgs[0].Content, originalContent);
    }
        var if got, want = len(images), 3; got != want {
        t.Fatalf("len(images) = %d, want %d", got, want);
    }
        if prompt == "" {
        t.Fatal("prompt is empty");
    }
    }

    public static void TestChatPromptGLMOcrRendererAddsImageTags(*testing.T t) {
        var msgs = []api.Message{
        {
        Role:    "user",;
        Content: "extract text",;
        Images:  []api.ImageData{[]byte("img-1"), []byte("img-2")},;
        },;
    }
        var m = Model{
        Config:         model.ConfigV2{Renderer: "glm-ocr"},;
        ProjectorPaths: []String{"vision"},;
    }
        var opts = api.Options{Runner: api.Runner{NumCtx: 8192}}
        var think = false;
        var prompt, images, err = chatPrompt(t.Context(), &m, mockRunner{}.Tokenize, &opts, msgs, null, &api.ThinkValue{Value: think}, true);
        if err != null {
        t.Fatal(err);
    }
        var if got, want = len(images), 2; got != want {
        t.Fatalf("len(images) = %d, want %d", got, want);
    }
        if !strings.Contains(prompt, "<|user|>\n[img-0][img-1]extract text") {
        t.Fatalf("prompt missing glm-ocr image tags, got: %q", prompt);
    }
    }

    public static void TestRenderPromptResolvesDynamicGemma4Renderer(*testing.T t) {
        var msgs = []api.Message{{Role: "user", Content: "Hello"}}
        var tests = []struct {
        name  String;
        model Model;
        want  String;
        }{
        {
        name: "small from name",;
        model: Model{
        Name:      "gemma4:e4b",;
        ShortName: "gemma4:e4b",;
        Config:    testConfigWithRenderer(gemma4RendererLegacy),;
        },;
        want: "<bos><|turn>user\nHello<turn|>\n<|turn>model\n",;
        },;
        {
        name: "large from model type",;
        model: Model{
        Config: testConfigWithRendererAndType(gemma4RendererLegacy, "25.2B"),;
        },;
        want: "<bos><|turn>user\nHello<turn|>\n<|turn>model\n<|channel>thought\n<channel|>",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got, err = renderPrompt(&tt.model, msgs, null, null);
        if err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(got, tt.want); diff != "" {
        t.Fatalf("rendered prompt mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }
}
