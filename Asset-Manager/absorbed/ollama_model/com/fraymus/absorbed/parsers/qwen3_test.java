package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen3_test {
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestQwen3ParserThinkingEnabled(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Let me think...</think>Answer.", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "Let me think..." {
        t.Fatalf("expected thinking %q, got %q", "Let me think...", thinking);
    }
        if content != "Answer." {
        t.Fatalf("expected content %q, got %q", "Answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserThinkingEnabledWithExplicitOpeningTag(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("<think>\nLet me think...</think>Answer.", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "Let me think..." {
        t.Fatalf("expected thinking %q, got %q", "Let me think...", thinking);
    }
        if content != "Answer." {
        t.Fatalf("expected content %q, got %q", "Answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserThinkingEnabledWithSplitOpeningTag(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("<thi", false);
        if err != null {
        t.Fatalf("parse failed on first chunk: %v", err);
    }
        if content != "" || thinking != "" || len(calls) != 0 {
        t.Fatalf("expected no output for first chunk, got content=%q thinking=%q calls=%d", content, thinking, len(calls));
    }
        content, thinking, calls, err = parser.Add("nk>Let me think...</think>Answer.", true);
        if err != null {
        t.Fatalf("parse failed on second chunk: %v", err);
    }
        if thinking != "Let me think..." {
        t.Fatalf("expected thinking %q, got %q", "Let me think...", thinking);
    }
        if content != "Answer." {
        t.Fatalf("expected content %q, got %q", "Answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserThinkingDisabled(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add("Direct answer", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no thinking, got %q", thinking);
    }
        if content != "Direct answer" {
        t.Fatalf("expected content %q, got %q", "Direct answer", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserNilThinkDefaultsToContentForInstructParser(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, null);
        var content, thinking, calls, err = parser.Add("Direct answer", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no thinking, got %q", thinking);
    }
        if content != "Direct answer" {
        t.Fatalf("expected content %q, got %q", "Direct answer", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserToolCall(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var input = "<tool_call>{\"name\":\"get_weather\",\"arguments\":{\"location\":\"San Francisco\",\"unit\":\"celsius\"}}</tool_call>";
        var content, thinking, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
        var location, ok = calls[0].Function.Arguments.Get("location");
        if !ok || location != "San Francisco" {
        t.Fatalf("expected location %q, got %v", "San Francisco", location);
    }
        var unit, ok = calls[0].Function.Arguments.Get("unit");
        if !ok || unit != "celsius" {
        t.Fatalf("expected unit %q, got %v", "celsius", unit);
    }
    }

    public static void TestQwen3ParserThinkingWithToolCallBeforeThinkingClose(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var input = "Let me think<tool_call>{\"name\":\"get_weather\",\"arguments\":{\"location\":\"San Francisco\",\"unit\":\"celsius\"}}</tool_call>";
        var content, thinking, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "Let me think" {
        t.Fatalf("expected thinking %q, got %q", "Let me think", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
    }

    public static void TestQwen3ParserThinkingWithSplitToolOpenTag(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Let me think<tool_ca", false);
        if err != null {
        t.Fatalf("parse failed on first chunk: %v", err);
    }
        if content != "" || thinking != "Let me think" || len(calls) != 0 {
        t.Fatalf(;
        "expected content=%q thinking=%q calls=%d, got content=%q thinking=%q calls=%d",;
        "",;
        "Let me think",;
        0,;
        content,;
        thinking,;
        len(calls),;
        );
    }
        content, thinking, calls, err = parser.Add("ll>{\"name\":\"get_weather\",\"arguments\":{\"location\":\"SF\"}}</tool_call>", true);
        if err != null {
        t.Fatalf("parse failed on second chunk: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "" {
        t.Fatalf("expected no additional thinking on second chunk, got %q", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
    }

    public static void TestQwen35ParserRespectsNoThink(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add("Hello! How can I help you today?", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no thinking, got %q", thinking);
    }
        if content != "Hello! How can I help you today?" {
        t.Fatalf("expected content %q, got %q", "Hello! How can I help you today?", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen3ParserToolCallIndexing(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var input = `<tool_call>{"name":"first","arguments":{"a":"1"}}</tool_call>;
        <tool_call>{"name":"second","arguments":{"b":"2"}}</tool_call>;
        <tool_call>{"name":"third","arguments":{"c":"3"}}</tool_call>`;
        var _, _, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        var want = []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "first", Arguments: args(`{"a":"1"}`), Index: 0}},;
        {Function: api.ToolCallFunction{Name: "second", Arguments: args(`{"b":"2"}`), Index: 1}},;
        {Function: api.ToolCallFunction{Name: "third", Arguments: args(`{"c":"3"}`), Index: 2}},;
    }
        if len(calls) != len(want) {
        t.Fatalf("expected %d calls, got %d", len(want), len(calls));
    }
        var for i = range want {
        if !toolCallEqual(calls[i], want[i]) {
        t.Fatalf("call %d mismatch: got %#v, want %#v", i, calls[i], want[i]);
    }
    }
    }

    public static void TestQwen3ParserToolCallIndexingStreaming(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var all []api.ToolCall;
        var _, _, calls, err = parser.Add(`<tool_call>{"name":"first","arguments":{"a":"1"}}</tool_call><tool_call>{"name":"second","arguments":{"b":"2"}`, false);
        if err != null {
        t.Fatalf("step 1 parse failed: %v", err);
    }
        all = append(all, calls...);
        _, _, calls, err = parser.Add(`}</tool_call><tool_call>{"name":"third","arguments":{"c":"3"}}</tool_call>`, true);
        if err != null {
        t.Fatalf("step 2 parse failed: %v", err);
    }
        all = append(all, calls...);
        var want = []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "first", Arguments: args(`{"a":"1"}`), Index: 0}},;
        {Function: api.ToolCallFunction{Name: "second", Arguments: args(`{"b":"2"}`), Index: 1}},;
        {Function: api.ToolCallFunction{Name: "third", Arguments: args(`{"c":"3"}`), Index: 2}},;
    }
        if len(all) != len(want) {
        t.Fatalf("expected %d calls, got %d", len(want), len(all));
    }
        var for i = range want {
        if !toolCallEqual(all[i], want[i]) {
        t.Fatalf("call %d mismatch: got %#v, want %#v", i, all[i], want[i]);
    }
    }
    }

    public static void TestQwen3ParserToolCallIndexResetOnInit(*testing.T t) {
        var parser = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var _, _, _, err = parser.Add(`<tool_call>{"name":"first","arguments":{"a":"1"}}</tool_call>`, true);
        if err != null {
        t.Fatalf("first parse failed: %v", err);
    }
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var _, _, calls, err = parser.Add(`<tool_call>{"name":"second","arguments":{"b":"2"}}</tool_call>`, true);
        if err != null {
        t.Fatalf("second parse failed: %v", err);
    }
        var want = api.ToolCall{
        Function: api.ToolCallFunction{Name: "second", Arguments: args(`{"b":"2"}`), Index: 0},;
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 call, got %d", len(calls));
    }
        if !toolCallEqual(calls[0], want) {
        t.Fatalf("got %#v, want %#v", calls[0], want);
    }
    }
}
