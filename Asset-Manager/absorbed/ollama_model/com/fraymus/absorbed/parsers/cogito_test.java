package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class cogito_test {
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestCogitoParser(*testing.T t) {
        var tests = []struct {
        name              String;
        input             String;
        expectedContent   String;
        expectedThinking  String;
        expectedToolCalls []api.ToolCall;
        tools             []api.Tool;
        lastMessage       *api.Message;
        }{
        {
        name:             "simple_content",;
        input:            "This is a simple response.",;
        expectedContent:  "This is a simple response.",;
        expectedThinking: "",;
        },;
        {
        name:             "thinking_only",;
        input:            "This is thinking content.</think>This is response content.",;
        expectedContent:  "This is response content.",;
        expectedThinking: "This is thinking content.",;
        },;
        {
        name: "tool_call_simple",;
        input: `<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{"location":"Paris"}
        ` + "```" + `<｜tool▁call▁end｜><｜tool▁calls▁end｜>`,;
        expectedToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index: 0,;
        Name:  "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {Type: api.PropertyType{"String"}},;
        }),;
        },;
        },;
        },;
        },;
        },;
        {
        name: "thinking_with_tool_call",;
        input: `I need to check the weather.</think><｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{"location":"Paris"}
        ` + "```" + `<｜tool▁call▁end｜><｜tool▁calls▁end｜>`,;
        expectedContent:  "I need to check the weather.</think>",;
        expectedThinking: "", // No thinking when tools are present (Cogito-specific behavior);
        expectedToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {Type: api.PropertyType{"String"}},;
        }),;
        },;
        },;
        },;
        },;
        },;
        {
        name: "multiple_tool_calls",;
        input: `<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{"location":"Paris"}
        ` + "```" + `<｜tool▁call▁end｜>;
        <｜tool▁call▁begin｜>function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{"location":"London"}
        ` + "```" + `<｜tool▁call▁end｜><｜tool▁calls▁end｜>`,;
        expectedToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index: 1,;
        Name:  "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "London",;
        }),;
        },;
        },;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: testPropsMap(map[String]api.ToolProperty{
        "location": {Type: api.PropertyType{"String"}},;
        }),;
        },;
        },;
        },;
        },;
        },;
        {
        name: "complex_tool_arguments",;
        input: `<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>process_data;
        ` + "```json\n" + `{"items":["item1","item2"],"config":{"enabled":true,"threshold":0.95},"count":42}
        ` + "```" + `<｜tool▁call▁end｜><｜tool▁calls▁end｜>`,;
        expectedToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "process_data",;
        Arguments: testArgs(map[String]any{
        "items":  []any{"item1", "item2"},;
        "config": map[String]any{"enabled": true, "threshold": 0.95},;
        "count":  42.0,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:             "tool_output_parsing",;
        input:            `<｜tool▁outputs▁begin｜><｜tool▁output▁begin｜>{"temperature": 22, "condition": "sunny"}<｜tool▁output▁end｜><｜tool▁outputs▁end｜>`,;
        expectedContent:  "",;
        expectedThinking: "",;
        },;
        {
        name: "thinking_with_multiline_content",;
        input: `This is line 1;
        This is line 2;
        This is line 3</think>Final response here.`,;
        expectedContent:  "Final response here.",;
        expectedThinking: "This is line 1\nThis is line 2\nThis is line 3",;
        },;
        {
        name:             "no_thinking_simple",;
        input:            "This is content.",;
        expectedContent:  "This is content.",;
        expectedThinking: "",;
        },;
        {
        name:            "prefill_content_only",;
        input:           "Continuing from previous content.",;
        expectedContent: "Continuing from previous content.",;
        lastMessage: &api.Message{
        Role:    "assistant",;
        Content: "Previous content",;
        },;
        },;
        {
        name:             "prefill_with_thinking",;
        input:            "Continuing thinking</think>Continuing content.",;
        expectedContent:  "Continuing content.",;
        expectedThinking: "Continuing thinking",;
        lastMessage: &api.Message{
        Role: "assistant",;
        },;
        },;
        {
        name:             "nested_think_tags_in_thinking",;
        input:            "I'm thinking <think>nested</think> more thinking</think>Final content.",;
        expectedContent:  "more thinking</think>Final content.",;
        expectedThinking: "I'm thinking <think>nested",;
        },;
        {
        name:             "multiple_think_close_tags",;
        input:            "First thinking</think>Content</think>More content.",;
        expectedContent:  "Content</think>More content.",;
        expectedThinking: "First thinking",;
        },;
        {
        name:             "empty_thinking_content",;
        input:            "</think>Just content here.",;
        expectedContent:  "</think>Just content here.",;
        expectedThinking: "",;
        },;
        {
        name:             "thinking_disabled_with_think_tags",;
        input:            "Content with </think> tags should be treated as content.",;
        expectedContent:  "Content with </think> tags should be treated as content.",;
        expectedThinking: "",;
        lastMessage: &api.Message{
        Role:    "assistant",;
        Content: "existing", // Forces non-thinking mode;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var hasThinking = tt.expectedThinking != "";
        var parser = &CogitoParser{}                                                  // it has thinking support;
        parser.Init(tt.tools, tt.lastMessage, &api.ThinkValue{Value: hasThinking}) // but we should set it with the request that the user wants;
        var content, thinking, toolCalls, err = parser.Add(tt.input, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        var if diff = cmp.Diff(tt.expectedContent, content); diff != "" {
        t.Errorf("content mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedThinking, thinking); diff != "" {
        t.Errorf("thinking mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedToolCalls, toolCalls, argsComparer); diff != "" {
        t.Errorf("tool calls mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestCogitoParser_Streaming(*testing.T t) {
        var parser = &CogitoParser{}
        parser.Init(null, null, &api.ThinkValue{Value: true});
        var chunks = []String{
        "This is ",;
        "thinking content",;
        ".</think>This is ",;
        "content.<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>test_tool\n```json\n{\"arg\":\"value\"}\n```<｜tool▁call▁end｜><｜tool▁calls▁end｜>",;
    }
        var finalContent, finalThinking strings.Builder;
        var finalToolCalls []api.ToolCall;
        var for i, chunk = range chunks {
        var done = i == len(chunks)-1;
        var content, thinking, toolCalls, err = parser.Add(chunk, done);
        if err != null {
        t.Fatalf("Add() error on chunk %d: %v", i, err);
    }
        finalContent.WriteString(content);
        finalThinking.WriteString(thinking);
        finalToolCalls = append(finalToolCalls, toolCalls...);
    }
        var expectedContent = "This is content.";
        var expectedThinking = "This is thinking content.";
        var expectedToolCalls = []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "test_tool",;
        Arguments: testArgs(map[String]any{
        "arg": "value",;
        }),;
        },;
        },;
    }
        if finalContent.String() != expectedContent {
        t.Errorf("expected content %q, got %q", expectedContent, finalContent.String());
    }
        if finalThinking.String() != expectedThinking {
        t.Errorf("expected thinking %q, got %q", expectedThinking, finalThinking.String());
    }
        var if diff = cmp.Diff(expectedToolCalls, finalToolCalls, argsComparer); diff != "" {
        t.Errorf("tool calls mismatch (-want +got):\n%s", diff);
    }
    }

    public static void TestCogitoParser_StreamingEdgeCases(*testing.T t) {
        var tests = []struct {
        name               String;
        chunks             []String;
        expectedContent    String;
        expectedThinking   String;
        expectedToolCalls  []api.ToolCall;
        hasThinkingSupport boolean;
        }{
        {
        name: "split_thinking_tag",;
        chunks: []String{
        "This is thinking content</thi",;
        "nk>This is content.",;
        },;
        expectedContent:    "This is content.",;
        expectedThinking:   "This is thinking content",;
        hasThinkingSupport: true,;
        },;
        {
        name: "split_tool_calls_begin_tag_conservative_parsing",;
        chunks: []String{
        "Content before<｜tool▁calls▁beg",;
        "in｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>test\n```json\n{}\n```<｜tool▁call▁end｜><｜tool▁calls▁end｜>",;
        },;
        expectedContent:    "Content before<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>test\n```json\n{}\n```<｜tool▁call▁end｜><｜tool▁calls▁end｜>",;
        expectedToolCalls:  null,;
        hasThinkingSupport: false,;
        },;
        {
        name: "thinking_disabled_with_split_tags",;
        chunks: []String{
        "Content with </thi",;
        "nk> should be treated as content.",;
        },;
        expectedContent:    "Content with </think> should be treated as content.",;
        expectedThinking:   "",;
        hasThinkingSupport: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &CogitoParser{}
        parser.Init(null, null, &api.ThinkValue{Value: tt.hasThinkingSupport});
        var finalContent, finalThinking strings.Builder;
        var finalToolCalls []api.ToolCall;
        var for i, chunk = range tt.chunks {
        var done = i == len(tt.chunks)-1;
        var content, thinking, toolCalls, err = parser.Add(chunk, done);
        if err != null {
        t.Fatalf("Add() error on chunk %d: %v", i, err);
    }
        finalContent.WriteString(content);
        finalThinking.WriteString(thinking);
        finalToolCalls = append(finalToolCalls, toolCalls...);
    }
        if finalContent.String() != tt.expectedContent {
        t.Errorf("expected content %q, got %q", tt.expectedContent, finalContent.String());
    }
        if finalThinking.String() != tt.expectedThinking {
        t.Errorf("expected thinking %q, got %q", tt.expectedThinking, finalThinking.String());
    }
        var if diff = cmp.Diff(tt.expectedToolCalls, finalToolCalls, argsComparer); diff != "" {
        t.Errorf("tool calls mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestCogitoParser_HasToolSupport(*testing.T t) {
        var parser = &CogitoParser{}
        if !parser.HasToolSupport() {
        t.Error("CogitoParser should support tools");
    }
    }

    public static void TestCogitoParser_Init(*testing.T t) {
        var parser = &CogitoParser{}
        var tools = []api.Tool{
        {Function: api.ToolFunction{Name: "test_tool"}},;
    }
        var lastMessage = &api.Message{Role: "assistant", Content: "previous"}
        var returnedTools = parser.Init(tools, lastMessage, null);
        if len(returnedTools) != len(tools) {
        t.Errorf("expected %d tools returned, got %d", len(tools), len(returnedTools));
    }
    }

    public static void TestCogitoParser_parseToolCallContent(*testing.T t) {
        var tests = []struct {
        name        String;
        content     String;
        expected    api.ToolCall;
        expectError boolean;
        }{
        {
        name: "valid_tool_call_standard_format",;
        content: `function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{"location":"Paris"}
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        expectError: false,;
        },;
        {
        name: "valid_tool_call_complex_args",;
        content: `function<｜tool▁sep｜>process_data;
        ` + "```json\n" + `{"items":["item1","item2"],"config":{"enabled":true},"count":42}
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "process_data",;
        Arguments: testArgs(map[String]any{
        "items":  []any{"item1", "item2"},;
        "config": map[String]any{"enabled": true},;
        "count":  42.0,;
        }),;
        },;
        },;
        expectError: false,;
        },;
        {
        name: "valid_tool_call_empty_args",;
        content: `function<｜tool▁sep｜>no_args_tool;
        ` + "```json\n" + `{}
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "no_args_tool",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        expectError: false,;
        },;
        {
        name:        "missing_separator",;
        content:     `functionget_weather` + "```json\n" + `{"location":"Paris"}` + "\n```",;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name:        "invalid_function_type",;
        content:     `not_function<｜tool▁sep｜>get_weather` + "```json\n" + `{"location":"Paris"}` + "\n```",;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name:        "missing_json_block_start",;
        content:     `function<｜tool▁sep｜>get_weather{"location":"Paris"}` + "```",;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name:        "missing_json_block_end",;
        content:     `function<｜tool▁sep｜>get_weather` + "```json\n" + `{"location":"Paris"}`,;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name:        "invalid_json",;
        content:     `function<｜tool▁sep｜>get_weather` + "```json\n" + `{location:Paris}` + "\n```",;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name:        "empty_function_type",;
        content:     `<｜tool▁sep｜>get_weather` + "```json\n" + `{"location":"Paris"}` + "\n```",;
        expected:    api.ToolCall{},;
        expectError: true,;
        },;
        {
        name: "tool_with_spaces_in_name",;
        content: `function<｜tool▁sep｜>  get_weather;
        ` + "```json\n" + `{"location":"Paris"}
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        expectError: false,;
        },;
        {
        name: "tool_with_multiline_json",;
        content: `function<｜tool▁sep｜>get_weather;
        ` + "```json\n" + `{
        "location": "Paris",;
        "units": "metric";
    }
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        "units":    "metric",;
        }),;
        },;
        },;
        expectError: false,;
        },;
        {
        name: "tool_with_nested_objects",;
        content: `function<｜tool▁sep｜>complex_tool;
        ` + "```json\n" + `{"nested":{"deep":{"value":123}}}
        ` + "```",;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "complex_tool",;
        Arguments: testArgs(map[String]any{
        "nested": map[String]any{
        "deep": map[String]any{
        "value": 123.0,;
        },;
        },;
        }),;
        },;
        },;
        expectError: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &CogitoParser{}
        var result, err = parser.parseToolCallContent(tt.content);
        if tt.expectError {
        if err == null {
        t.Errorf("expected error but got none");
    }
        return;
    }
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        var if diff = cmp.Diff(tt.expected, result, argsComparer); diff != "" {
        t.Errorf("tool call mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }
}
