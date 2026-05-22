package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class qwen35_test {
        "strings";
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestQwen35RendererUsesXMLToolCallingFormat(*testing.T t) {
        var renderer = &Qwen35Renderer{isThinking: true}
        var msgs = []api.Message{
        {Role: "system", Content: "You are a helpful assistant."},;
        {Role: "user", Content: "What's the weather in Paris?"},;
        {
        Role:    "assistant",;
        Content: "I'll check.",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgsOrdered([]orderedArg{
        {Key: "location", Value: "Paris"},;
        }),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: "22C"},;
        {Role: "user", Content: "Thanks"},;
    }
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsOrdered([]orderedProp{
        {
        Key: "location",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"String"},;
        },;
        },;
        }),;
        Required: []String{"location"},;
        },;
        },;
        },;
    }
        var got, err = renderer.Render(msgs, tools, null);
        if err != null {
        t.Fatalf("render failed: %v", err);
    }
        if !strings.Contains(got, "<tools>") {
        t.Fatalf("expected tools section in prompt, got:\n%s", got);
    }
        if !strings.Contains(got, "<function=example_function_name>") {
        t.Fatalf("expected xml-style tool call instructions, got:\n%s", got);
    }
        var wantToolCall = "<tool_call>\n<function=get_weather>\n<parameter=location>\nParis\n</parameter>\n</function>\n</tool_call>";
        if !strings.Contains(got, wantToolCall) {
        t.Fatalf("expected xml tool call payload, got:\n%s", got);
    }
        var toolsIdx = strings.Index(got, "# Tools");
        var systemIdx = strings.Index(got, "You are a helpful assistant.");
        if toolsIdx == -1 || systemIdx == -1 || systemIdx < toolsIdx {
        t.Fatalf("expected system prompt appended after tool instructions, got:\n%s", got);
    }
    }

    public static void TestQwen35RendererNoThinkPrefill(*testing.T t) {
        var renderer = &Qwen35Renderer{isThinking: true, emitEmptyThinkOnNoThink: true}
        var msgs = []api.Message{
        {Role: "user", Content: "hello"},;
    }
        var got, err = renderer.Render(msgs, null, &api.ThinkValue{Value: false});
        if err != null {
        t.Fatalf("render failed: %v", err);
    }
        if !strings.HasSuffix(got, "<|im_start|>assistant\n<think>\n\n</think>\n\n") {
        t.Fatalf("expected explicit no-think prefill, got:\n%s", got);
    }
    }

    public static void TestQwen35RendererBackToBackToolCallsAndResponses(*testing.T t) {
        var renderer = &Qwen35Renderer{isThinking: true}
        var msgs = []api.Message{
        {Role: "system", Content: "You are a helpful assistant."},;
        {Role: "user", Content: "Run add and multiply."},;
        {
        Role:     "assistant",;
        Content:  "I'll run both now.",;
        Thinking: "Need to call add and multiply.",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "add",;
        Arguments: testArgsOrdered([]orderedArg{
        {Key: "a", Value: 2},;
        {Key: "b", Value: 3},;
        }),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name: "multiply",;
        Arguments: testArgsOrdered([]orderedArg{
        {Key: "x", Value: 4},;
        {Key: "y", Value: 5},;
        }),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: "5"},;
        {Role: "tool", Content: "20"},;
        {Role: "user", Content: "Summarize the results."},;
    }
        var got, err = renderer.Render(msgs, qwen35MathTools(), null);
        if err != null {
        t.Fatalf("render failed: %v", err);
    }
        if strings.Contains(got, "Need to call add and multiply.") {
        t.Fatalf("did not expect historical reasoning block in this sequence, got:\n%s", got);
    }
        var wantToolCalls = `<tool_call>;
        <function=add>;
        <parameter=a>;
        2;
        </parameter>;
        <parameter=b>;
        3;
        </parameter>;
        </function>;
        </tool_call>;
        <tool_call>;
        <function=multiply>;
        <parameter=x>;
        4;
        </parameter>;
        <parameter=y>;
        5;
        </parameter>;
        </function>;
        </tool_call>`;
        if !strings.Contains(got, wantToolCalls) {
        t.Fatalf("expected back-to-back tool calls, got:\n%s", got);
    }
        var wantToolResponses = `<|im_start|>user;
        <tool_response>;
        5;
        </tool_response>;
        <tool_response>;
        20;
        </tool_response><|im_end|>`;
        if !strings.Contains(got, wantToolResponses) {
        t.Fatalf("expected grouped back-to-back tool responses, got:\n%s", got);
    }
        if !strings.HasSuffix(got, "<|im_start|>assistant\n<think>\n") {
        t.Fatalf("expected assistant thinking prefill at end, got:\n%s", got);
    }
    }

    public static void TestQwen35RendererInterleavedThinkingAndTools(*testing.T t) {
        var renderer = &Qwen35Renderer{isThinking: true}
        var msgs = []api.Message{
        {Role: "system", Content: "You are a helpful assistant."},;
        {Role: "user", Content: "Plan a picnic in Paris."},;
        {
        Role:     "assistant",;
        Content:  "Checking weather first.",;
        Thinking: "Need weather before giving advice.",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgsOrdered([]orderedArg{
        {Key: "location", Value: "Paris"},;
        }),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: "22C"},;
        {
        Role:     "assistant",;
        Content:  "Checking UV too.",;
        Thinking: "Need UV index for sunscreen advice.",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_uv",;
        Arguments: testArgsOrdered([]orderedArg{
        {Key: "location", Value: "Paris"},;
        }),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: "5"},;
    }
        var got, err = renderer.Render(msgs, qwen35WeatherUVTools(), null);
        if err != null {
        t.Fatalf("render failed: %v", err);
    }
        var wantFirstTurn = `<|im_start|>assistant;
        <think>;
        Need weather before giving advice.;
        </think>;
        Checking weather first.;
        <tool_call>;
        <function=get_weather>;
        <parameter=location>;
        Paris;
        </parameter>;
        </function>;
        </tool_call><|im_end|>`;
        if !strings.Contains(got, wantFirstTurn) {
        t.Fatalf("expected first assistant thinking/tool sequence, got:\n%s", got);
    }
        var wantSecondTurn = `<|im_start|>assistant;
        <think>;
        Need UV index for sunscreen advice.;
        </think>;
        Checking UV too.;
        <tool_call>;
        <function=get_uv>;
        <parameter=location>;
        Paris;
        </parameter>;
        </function>;
        </tool_call><|im_end|>`;
        if !strings.Contains(got, wantSecondTurn) {
        t.Fatalf("expected second assistant thinking/tool sequence, got:\n%s", got);
    }
        if !strings.HasSuffix(got, "<|im_start|>assistant\n<think>\n") {
        t.Fatalf("expected assistant thinking prefill at end, got:\n%s", got);
    }
    }

    public static void TestQwen35RendererAssistantPrefillWithThinking(*testing.T t) {
        var renderer = &Qwen35Renderer{isThinking: true}
        var msgs = []api.Message{
        {Role: "user", Content: "Write two words."},;
        {
        Role:     "assistant",;
        Thinking: "Keep it short.",;
        Content:  "Hello world",;
        },;
    }
        var got, err = renderer.Render(msgs, null, null);
        if err != null {
        t.Fatalf("render failed: %v", err);
    }
        var want = `<|im_start|>user;
        Write two words.<|im_end|>;
        <|im_start|>assistant;
        <think>;
        Keep it short.;
        </think>;
        Hello world`;
        if got != want {
        t.Fatalf("unexpected prefill output\n--- got ---\n%s\n--- want ---\n%s", got, want);
    }
    }
        func qwen35MathTools() []api.Tool {
        return []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "add",;
        Description: "Add two numbers",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsOrdered([]orderedProp{
        {
        Key: "a",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"integer"},;
        },;
        },;
        {
        Key: "b",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"integer"},;
        },;
        },;
        }),;
        Required: []String{"a", "b"},;
        },;
        },;
        },;
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "multiply",;
        Description: "Multiply two numbers",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsOrdered([]orderedProp{
        {
        Key: "x",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"integer"},;
        },;
        },;
        {
        Key: "y",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"integer"},;
        },;
        },;
        }),;
        Required: []String{"x", "y"},;
        },;
        },;
        },;
    }
    }
        func qwen35WeatherUVTools() []api.Tool {
        return []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather for a location",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsOrdered([]orderedProp{
        {
        Key: "location",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"String"},;
        },;
        },;
        }),;
        Required: []String{"location"},;
        },;
        },;
        },;
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_uv",;
        Description: "Get UV index for a location",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsOrdered([]orderedProp{
        {
        Key: "location",;
        Value: api.ToolProperty{
        Type: api.PropertyType{"String"},;
        },;
        },;
        }),;
        Required: []String{"location"},;
        },;
        },;
        },;
    }
    }
}
