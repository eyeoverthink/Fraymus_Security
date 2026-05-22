package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class nemotron3nano_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestNemotron3NanoParser(*testing.T t) {
        var tests = []struct {
        name             String;
        input            String;
        thinkValue       *api.ThinkValue;
        expectedContent  String;
        expectedThinking String;
        expectedCalls    []api.ToolCall;
        }{
        {
        name:             "thinking then content",;
        input:            "Let me think about this...</think>\nHere is my answer.",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Let me think about this...",;
        expectedContent:  "Here is my answer.",;
        },;
        {
        name:             "thinking with newlines",;
        input:            "Step 1: Analyze\nStep 2: Process\nStep 3: Conclude</think>\nThe answer is 42.",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Step 1: Analyze\nStep 2: Process\nStep 3: Conclude",;
        expectedContent:  "The answer is 42.",;
        },;
        {
        name:             "thinking then tool call",;
        input:            "I should check the weather...</think>\n<tool_call>\n<function=get_weather>\n<parameter=city>\nParis\n</parameter>\n</function>\n</tool_call>",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "I should check the weather...",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
        },;
        },;
        {
        name:             "thinking content then tool call",;
        input:            "Let me think...</think>\nI'll check for you.\n<tool_call>\n<function=search>\n<parameter=query>\ntest\n</parameter>\n</function>\n</tool_call>",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Let me think...",;
        expectedContent:  "I'll check for you.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "search",;
        Arguments: testArgs(map[String]any{"query": "test"}),;
        },;
        },;
        },;
        },;
        {
        name:             "empty thinking block - immediate close",;
        input:            "</think>\nHere is my answer.",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "",;
        expectedContent:  "Here is my answer.",;
        },;
        {
        name:            "thinking disabled but model outputs think close anyway",;
        input:           "</think>\nSome content after spurious tag.",;
        thinkValue:      &api.ThinkValue{Value: false},;
        expectedContent: "</think>\nSome content after spurious tag.",;
        },;
        {
        name:             "thinking with only whitespace after close tag",;
        input:            "My thoughts...</think>   \n\t\n   Content here.",;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "My thoughts...",;
        expectedContent:  "Content here.",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, tt.thinkValue);
        var content, thinking, calls, err = p.Add(tt.input, false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        var finalContent, finalThinking, finalCalls, err = p.Add("", true);
        if err != null {
        t.Fatalf("unexpected error on done: %v", err);
    }
        content += finalContent;
        thinking += finalThinking;
        calls = append(calls, finalCalls...);
        var if diff = cmp.Diff(content, tt.expectedContent); diff != "" {
        t.Errorf("content mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(thinking, tt.expectedThinking); diff != "" {
        t.Errorf("thinking mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(calls, tt.expectedCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestNemotron3NanoParser_Streaming(*testing.T t) {
        var tests = []struct {
        name             String;
        chunks           []String;
        thinkValue       *api.ThinkValue;
        expectedContent  String;
        expectedThinking String;
        expectedCalls    []api.ToolCall;
        }{
        {
        name:             "streaming thinking then content - granular",;
        chunks:           []String{"Let", " me", " th", "ink", " about", " this", "...", "<", "/", "think", ">", "\n", "Here", " is", " my", " answer", "."},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Let me think about this...",;
        expectedContent:  "Here is my answer.",;
        },;
        {
        name:             "streaming thinking with newlines - granular",;
        chunks:           []String{"Step", " 1", ":", " Ana", "lyze\n", "Step", " 2", ":", " Pro", "cess", "</", "thi", "nk>", "\n", "The", " ans", "wer."},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Step 1: Analyze\nStep 2: Process",;
        expectedContent:  "The answer.",;
        },;
        {
        name:             "thinking close tag split character by character",;
        chunks:           []String{"I", "'", "m", " ", "t", "h", "i", "n", "k", "i", "n", "g", ".", ".", ".", "<", "/", "t", "h", "i", "n", "k", ">", "\n", "D", "o", "n", "e", "!"},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "I'm thinking...",;
        expectedContent:  "Done!",;
        },;
        {
        name:             "multiple whitespace after think tag - separate chunks",;
        chunks:           []String{"Thinking...", "</think>", "\n", "\n", " ", "Content here."},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Thinking...",;
        expectedContent:  "Content here.",;
        },;
        {
        name:             "thinking then content then tool call - streaming",;
        chunks:           []String{"Ana", "lyzing", " your", " request", "...", "</", "think", ">\n", "I'll", " check", " that", " for", " you", ".", "\n", "<tool", "_call", ">\n", "<function", "=search", ">\n", "<parameter", "=query", ">\n", "test", " query", "\n</", "parameter", ">\n", "</function", ">\n", "</tool", "_call", ">"},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Analyzing your request...",;
        expectedContent:  "I'll check that for you.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "search",;
        Arguments: testArgs(map[String]any{"query": "test query"}),;
        },;
        },;
        },;
        },;
        {
        name:             "empty thinking block",;
        chunks:           []String{"</think>", "\n", "Just content."},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "",;
        expectedContent:  "Just content.",;
        },;
        {
        name:             "tool call immediately after think close - no content",;
        chunks:           []String{"Analyzing...", "</think>", "\n", "<tool_call>", "\n<function=test>\n</function>\n", "</tool_call>"},;
        thinkValue:       &api.ThinkValue{Value: true},;
        expectedThinking: "Analyzing...",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "test",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, tt.thinkValue);
        var allContent String;
        var allThinking String;
        var allCalls []api.ToolCall;
        var for _, chunk = range tt.chunks {
        var content, thinking, calls, err = p.Add(chunk, false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        allContent += content;
        allThinking += thinking;
        allCalls = append(allCalls, calls...);
    }
        var content, thinking, calls, err = p.Add("", true);
        if err != null {
        t.Fatalf("unexpected error on done: %v", err);
    }
        allContent += content;
        allThinking += thinking;
        allCalls = append(allCalls, calls...);
        var if diff = cmp.Diff(allContent, tt.expectedContent); diff != "" {
        t.Errorf("content mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(allThinking, tt.expectedThinking); diff != "" {
        t.Errorf("thinking mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(allCalls, tt.expectedCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestNemotron3NanoParser_HasToolSupport(*testing.T t) {
        var p = &Nemotron3NanoParser{}
        if !p.HasToolSupport() {
        t.Error("expected HasToolSupport to return true");
    }
    }

    public static void TestNemotron3NanoParser_HasThinkingSupport(*testing.T t) {
        var p = &Nemotron3NanoParser{}
        if !p.HasThinkingSupport() {
        t.Error("expected HasThinkingSupport to return true");
    }
    }

    public static void TestNemotron3NanoParser_Init(*testing.T t) {
        t.Run("starts in thinking state when enabled", func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, &api.ThinkValue{Value: true});
        if p.state != Nemotron3NanoCollectingThinking {
        t.Errorf("expected state Nemotron3NanoCollectingThinking, got %v", p.state);
    }
        });
        t.Run("starts in content state when thinking disabled", func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, &api.ThinkValue{Value: false});
        if p.state != Nemotron3NanoCollectingContent {
        t.Errorf("expected state Nemotron3NanoCollectingContent, got %v", p.state);
    }
        });
        t.Run("starts in content state when null thinkValue", func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, null);
        if p.state != Nemotron3NanoCollectingContent {
        t.Errorf("expected state Nemotron3NanoCollectingContent, got %v", p.state);
    }
        });
        t.Run("starts in content state with assistant prefill", func(t *testing.T) {
        var p = &Nemotron3NanoParser{}
        var prefill = &api.Message{Role: "assistant", Content: "Starting..."}
        p.Init(null, prefill, &api.ThinkValue{Value: true});
        if p.state != Nemotron3NanoCollectingContent {
        t.Errorf("expected state Nemotron3NanoCollectingContent, got %v", p.state);
    }
        });
    }

    public static void TestNemotron3NanoParser_WithTools(*testing.T t) {
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Type: "object",;
        Properties: testPropsMap(map[String]api.ToolProperty{
        "city": {Type: api.PropertyType{"String"}},;
        }),;
        },;
        },;
        },;
    }
        var p = &Nemotron3NanoParser{}
        var returnedTools = p.Init(tools, null, null);
        var if diff = cmp.Diff(returnedTools, tools, toolsComparer); diff != "" {
        t.Errorf("tools mismatch (-got +want):\n%s", diff);
    }
        var input = "<tool_call>\n<function=get_weather>\n<parameter=city>\nParis\n</parameter>\n</function>\n</tool_call>";
        var _, _, calls, err = p.Add(input, true);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        var expectedCalls = []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
    }
        var if diff = cmp.Diff(calls, expectedCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
    }

    public static void TestNemotron3NanoParser_ToolCallWithoutThinkClose(*testing.T t) {
        var chunks = []String{
        "Let", " me", " analyze", " this", ".", "\n",;
        "<tool_call>", "\n",;
        "<function=get_weather>", "\n",;
        "<parameter=city>", "Paris", "</parameter>", "\n",;
        "</function>", "\n",;
        "</tool_call>",;
    }
        var p = &Nemotron3NanoParser{}
        p.Init(null, null, &api.ThinkValue{Value: true}) // thinking ENABLED but model doesn't output </think>;
        var allContent String;
        var allThinking String;
        var allCalls []api.ToolCall;
        var for _, chunk = range chunks {
        var content, thinking, calls, err = p.Add(chunk, false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        allContent += content;
        allThinking += thinking;
        allCalls = append(allCalls, calls...);
    }
        var content, thinking, calls, err = p.Add("", true);
        if err != null {
        t.Fatalf("unexpected error on done: %v", err);
    }
        allContent += content;
        allThinking += thinking;
        allCalls = append(allCalls, calls...);
        var expectedThinking = "Let me analyze this.";
        var expectedCalls = []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
    }
        if allContent != "" {
        t.Errorf("expected no content (text was streamed as thinking), got: %q", allContent);
    }
        var if diff = cmp.Diff(allThinking, expectedThinking); diff != "" {
        t.Errorf("thinking mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(allCalls, expectedCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
    }
}
