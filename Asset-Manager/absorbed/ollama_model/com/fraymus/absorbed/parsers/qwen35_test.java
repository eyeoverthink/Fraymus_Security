package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen35_test {
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestQwen35ParserXMLToolCall(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: func() *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        props.Set("days", api.ToolProperty{Type: api.PropertyType{"integer"}});
        return props;
        }(),;
        },;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: false});
        var input = "<tool_call><function=get_weather><parameter=location>\nSan Francisco\n</parameter><parameter=days>\n3\n</parameter></function></tool_call>";
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
        var days, ok = calls[0].Function.Arguments.Get("days");
        if !ok || days != 3 {
        t.Fatalf("expected days %d, got %v", 3, days);
    }
    }

    public static void TestQwen35ParserThinkingWithExplicitOpeningTag(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
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

    public static void TestQwen35ParserAssistantPrefillStartsInContent(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var last = &api.Message{Role: "assistant", Content: "Prefilled response start"}
        parser.Init(null, last, null);
        var content, thinking, calls, err = parser.Add(" and continued", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no thinking for assistant prefill continuation, got %q", thinking);
    }
        if content != " and continued" {
        t.Fatalf("expected content %q, got %q", " and continued", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserToolCallEmittedInThinkingIsParsed(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: func() *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        return props;
        }(),;
        },;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: true});
        var input = `Need weather lookup<tool_call><function=get_weather><parameter=location>;
        SF;
        </parameter></function></tool_call>`;
        var content, thinking, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "Need weather lookup" {
        t.Fatalf("expected thinking %q, got %q", "Need weather lookup", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
        var location, ok = calls[0].Function.Arguments.Get("location");
        if !ok || location != "SF" {
        t.Fatalf("expected location %q, got %v", "SF", location);
    }
    }

    public static void TestQwen35ParserToolCallEmittedInThinkingIsParsedWhenToolCallTagIsSplitAcrossChunks(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: func() *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        return props;
        }(),;
        },;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Need weather lookup<tool_c", false);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "Need weather lookup" {
        t.Fatalf("expected thinking %q, got %q", "Need weather lookup", thinking);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls in first chunk, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add(`all><function=get_weather><parameter=location>;
        SF;
        </parameter></function></tool_call>`, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "" {
        t.Fatalf("expected no additional thinking, got %q", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
        var location, ok = calls[0].Function.Arguments.Get("location");
        if !ok || location != "SF" {
        t.Fatalf("expected location %q, got %v", "SF", location);
    }
    }

    public static void TestQwen35ParserFakeoutPartialToolCallThenThinkCloseAcrossChunks(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: func() *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        return props;
        }(),;
        },;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Need weather lookup<tool_c", false);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "Need weather lookup" {
        t.Fatalf("expected thinking %q, got %q", "Need weather lookup", thinking);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls in first chunk, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add("</thi", false);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "<tool_c" {
        t.Fatalf("expected thinking %q, got %q", "<tool_c", thinking);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls in second chunk, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add("nk>", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "" {
        t.Fatalf("expected no additional thinking in third chunk, got %q", thinking);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls in third chunk, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserToolCallAfterThinkingCloseIsParsed(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: func() *api.ToolPropertiesMap {
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        return props;
        }(),;
        },;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: true});
        var input = `Need weather lookup</think><tool_call><function=get_weather><parameter=location>;
        SF;
        </parameter></function></tool_call>`;
        var content, thinking, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if thinking != "Need weather lookup" {
        t.Fatalf("expected thinking %q, got %q", "Need weather lookup", thinking);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call after </think>, got %d", len(calls));
    }
        if calls[0].Function.Name != "get_weather" {
        t.Fatalf("expected tool name %q, got %q", "get_weather", calls[0].Function.Name);
    }
        var location, ok = calls[0].Function.Arguments.Get("location");
        if !ok || location != "SF" {
        t.Fatalf("expected location %q, got %v", "SF", location);
    }
    }

    public static void TestQwen35ParserThinkingDisabledPassesContentThrough(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add("Plain answer without think close tag.", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if content != "Plain answer without think close tag." {
        t.Fatalf("expected content %q, got %q", "Plain answer without think close tag.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserThinkingDisabledWithCloseTagTreatsAsContent(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add("</think>Some content after spurious tag.", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if content != "</think>Some content after spurious tag." {
        t.Fatalf("expected content %q, got %q", "</think>Some content after spurious tag.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserLeadingThinkCloseProducesContent(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("</think>The final answer.", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if content != "The final answer." {
        t.Fatalf("expected content %q, got %q", "The final answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserStreamingSplitThinkCloseTag(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Reasoning text</thi", false);
        if err != null {
        t.Fatalf("parse failed on first chunk: %v", err);
    }
        if thinking != "Reasoning text" {
        t.Fatalf("expected thinking %q, got %q", "Reasoning text", thinking);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add("nk>The final answer.", true);
        if err != null {
        t.Fatalf("parse failed on second chunk: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no additional thinking on second chunk, got %q", thinking);
    }
        if content != "The final answer." {
        t.Fatalf("expected content %q, got %q", "The final answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserStreamingEatsWhitespaceAfterThinkClose(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Reasoning</think>", false);
        if err != null {
        t.Fatalf("parse failed on first chunk: %v", err);
    }
        if thinking != "Reasoning" {
        t.Fatalf("expected thinking %q, got %q", "Reasoning", thinking);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add("\n \t", false);
        if err != null {
        t.Fatalf("parse failed on whitespace chunk: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no thinking on whitespace chunk, got %q", thinking);
    }
        if content != "" {
        t.Fatalf("expected whitespace after </think> to be eaten, got content %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
        content, thinking, calls, err = parser.Add("The final answer.", true);
        if err != null {
        t.Fatalf("parse failed on content chunk: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected no additional thinking, got %q", thinking);
    }
        if content != "The final answer." {
        t.Fatalf("expected content %q, got %q", "The final answer.", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestQwen35ParserThinkingTruncatedWithoutCloseTag(*testing.T t) {
        var parser = ParserForName("qwen3.5");
        if parser == null {
        t.Fatal("expected qwen3.5 parser");
    }
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var content, thinking, calls, err = parser.Add("Reasoning that never closes", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "Reasoning that never closes" {
        t.Fatalf("expected thinking %q, got %q", "Reasoning that never closes", thinking);
    }
        if content != "" {
        t.Fatalf("expected empty content, got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }
}
