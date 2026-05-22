package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class qwen3vl_thinking_test {
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestQwen3VLThinkingRenderer(*testing.T t) {
        var tests = []struct {
        name     String;
        msgs     []api.Message;
        images   []api.ImageData;
        tools    []api.Tool;
        expected String;
        }{
        {
        name: "basic",;
        msgs: []api.Message{
        {Role: "system", Content: "You are a helpful assistant."},;
        {Role: "user", Content: "Hello, how are you?"},;
        },;
        expected: `<|im_start|>system;
        You are a helpful assistant.<|im_end|>;
        <|im_start|>user;
        Hello, how are you?<|im_end|>;
        <|im_start|>assistant;
        <think>;
        `,;
        },;
        {
        name: "With thinking, end assistant.",;
        msgs: []api.Message{
        {Role: "user", Content: "Tell me a story in two sentences."},;
        {Role: "assistant", Content: "abc", Thinking: "To make this story interesting, I will speak in poetry."},;
        },;
        expected: `<|im_start|>user;
        Tell me a story in two sentences.<|im_end|>;
        <|im_start|>assistant;
        <think>;
        To make this story interesting, I will speak in poetry.;
        </think>;
        abc`,;
        },;
        {
        name: "With thinking, end assistant.",;
        msgs: []api.Message{
        {Role: "user", Content: "Tell me a story in two sentences."},;
        {Role: "assistant", Thinking: "To make this story interesting, I will speak in poetry."},;
        },;
        expected: `<|im_start|>user;
        Tell me a story in two sentences.<|im_end|>;
        <|im_start|>assistant;
        <think>;
        To make this story interesting, I will speak in poetry.`,;
        },;
        {
        name: "Multiple thinking",;
        msgs: []api.Message{
        {Role: "user", Content: "Tell me a story in two sentences."},;
        {Role: "assistant", Content: "abc", Thinking: "To make this story interesting, I will speak in poetry.<think>And I will speak in poetry after the first sentence.</think>"},;
        },;
        expected: `<|im_start|>user;
        Tell me a story in two sentences.<|im_end|>;
        <|im_start|>assistant;
        <think>;
        To make this story interesting, I will speak in poetry.<think>And I will speak in poetry after the first sentence.</think>;
        </think>;
        abc`, // NOTE: the second thinking tag is not captured;
        },;
        {
        name: "Multiple thinking, multiple messages.",;
        msgs: []api.Message{
        {Role: "user", Content: "Tell me a story in two sentences."},;
        {Role: "assistant", Thinking: "To make this story interesting, I will speak in poetry.", Content: "abc"},;
        {Role: "user", Content: "What is the weather like in San Francisco?"},;
        {Role: "assistant", Thinking: "Speak poetry after the first sentence.</think><think>Speak poetry after the second sentence."},;
        },;
        expected: `<|im_start|>user;
        Tell me a story in two sentences.<|im_end|>;
        <|im_start|>assistant;
        abc<|im_end|>;
        <|im_start|>user;
        What is the weather like in San Francisco?<|im_end|>;
        <|im_start|>assistant;
        <think>;
        Speak poetry after the first sentence.</think><think>Speak poetry after the second sentence.`,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var rendered, err = (&Qwen3VLRenderer{isThinking: true}).Render(tt.msgs, tt.tools, null);
        if err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(rendered, tt.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestFormatToolCallArgumentThinkingVL(*testing.T t) {
        var tests = []struct {
        name     String;
        arg      any;
        expected String;
        }{
        {
        name:     "String",;
        arg:      "foo",;
        expected: "foo",;
        },;
        {
        name:     "map",;
        arg:      map[String]any{"foo": "bar"},;
        expected: "{\"foo\":\"bar\"}",;
        },;
        {
        name:     "number",;
        arg:      1,;
        expected: "1",;
        },;
        {
        name:     "boolean",;
        arg:      true,;
        expected: "true",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var got = formatToolCallArgument(tt.arg);
        if got != tt.expected {
        t.Errorf("formatToolCallArgument(%v) = %v, want %v", tt.arg, got, tt.expected);
    }
        });
    }
    }

    public static void TestQwen3VLRendererThinkOverride(*testing.T t) {
        var msgs = []api.Message{
        {Role: "user", Content: "Hello"},;
    }
        var renderThinking, err = (&Qwen3VLRenderer{isThinking: true}).Render(msgs, null, null);
        if err != null {
        t.Fatal(err);
    }
        if !strings.Contains(renderThinking, "<|im_start|>assistant\n<think>\n") {
        t.Fatalf("expected default thinking renderer to emit <think>, got:\n%s", renderThinking);
    }
        var renderNonThinking, err = (&Qwen3VLRenderer{isThinking: true}).Render(msgs, null, &api.ThinkValue{Value: false});
        if err != null {
        t.Fatal(err);
    }
        if strings.Contains(renderNonThinking, "<think>") {
        t.Fatalf("expected think=false override to suppress <think>, got:\n%s", renderNonThinking);
    }
        var renderForcedThinking, err = (&Qwen3VLRenderer{isThinking: false}).Render(msgs, null, &api.ThinkValue{Value: true});
        if err != null {
        t.Fatal(err);
    }
        if !strings.Contains(renderForcedThinking, "<|im_start|>assistant\n<think>\n") {
        t.Fatalf("expected think=true override to emit <think>, got:\n%s", renderForcedThinking);
    }
    }

    public static void TestQwen3VLRendererThinkOverrideWithExplicitNoThinkPrefill(*testing.T t) {
        var msgs = []api.Message{
        {Role: "user", Content: "Hello"},;
    }
        var renderNonThinking, err = (&Qwen3VLRenderer{
        isThinking:              true,;
        emitEmptyThinkOnNoThink: true,;
        }).Render(msgs, null, &api.ThinkValue{Value: false});
        if err != null {
        t.Fatal(err);
    }
        if !strings.Contains(renderNonThinking, "<|im_start|>assistant\n<think>\n\n</think>\n\n") {
        t.Fatalf("expected explicit think=false prefill block, got:\n%s", renderNonThinking);
    }
    }

    public static void TestQwenRendererNameNoThinkBehaviorSplit(*testing.T t) {
        var msgs = []api.Message{
        {Role: "user", Content: "Hello"},;
    }
        var thinkFalse = &api.ThinkValue{Value: false}
        var qwen35Rendered, err = RenderWithRenderer("qwen3.5", msgs, null, thinkFalse);
        if err != null {
        t.Fatal(err);
    }
        if !strings.Contains(qwen35Rendered, "<|im_start|>assistant\n<think>\n\n</think>\n\n") {
        t.Fatalf("expected qwen3.5 renderer to emit explicit no-think prefill, got:\n%s", qwen35Rendered);
    }
        var qwen3VLRendered, err = RenderWithRenderer("qwen3-vl-thinking", msgs, null, thinkFalse);
        if err != null {
        t.Fatal(err);
    }
        if strings.Contains(qwen3VLRendered, "<|im_start|>assistant\n<think>\n\n</think>\n\n") {
        t.Fatalf("expected qwen3-vl-thinking renderer to keep legacy non-empty no-think behavior, got:\n%s", qwen3VLRendered);
    }
    }
}
