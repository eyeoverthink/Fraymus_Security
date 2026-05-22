package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class functiongemma_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/stretchr/testify/assert";
        );

    public static void TestFunctionGemmaParser(*testing.T t) {
        var tests = []struct {
        name          String;
        chunks        []String;
        tools         []api.Tool;
        expectedCalls []api.ToolCall;
        expectedText  String;
        }{
        {
        name:          "plain_content",;
        chunks:        []String{"H", "e", "l", "l", "o", ",", " ", "w", "o", "r", "l", "d", "!"},;
        expectedCalls: null,;
        expectedText:  "Hello, world!",;
        },;
        {
        name: "simple_tool_call",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "weather", "{",;
        "city", ":", "<", "escape", ">", "Paris", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        tools: []api.Tool{
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
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "content_before_tool_call",;
        chunks: []String{
        "L", "et", " ", "me", " ", "check", ".",;
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "weather", "{",;
        "city", ":", "<", "escape", ">", "Paris", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
        },;
        expectedText: "Let me check.",;
        },;
        {
        name: "numeric_arguments",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "add", "{",;
        "a", ":", "1", ",", "b", ":", "2",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "add",;
        Arguments: testArgs(map[String]any{"a": long(1), "b": long(2)}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "boolean_arguments",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "set", "_", "flag", "{",;
        "enabled", ":", "true", ",", "verbose", ":", "false",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "set_flag",;
        Arguments: testArgs(map[String]any{"enabled": true, "verbose": false}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "multiple_tool_calls",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "weather", "{",;
        "city", ":", "<", "escape", ">", "Paris", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "weather", "{",;
        "city", ":", "<", "escape", ">", "London", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index:     0,;
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index:     1,;
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "London"}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "array_argument",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "process", "{",;
        "items", ":", "[",;
        "<", "escape", ">", "a", "<", "escape", ">", ",",;
        "<", "escape", ">", "b", "<", "escape", ">", ",",;
        "<", "escape", ">", "c", "<", "escape", ">",;
        "]",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "process",;
        Arguments: testArgs(map[String]any{"items": []any{"a", "b", "c"}}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "object_argument",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "update", "{",;
        "data", ":", "{",;
        "name", ":", "<", "escape", ">", "test", "<", "escape", ">", ",",;
        "value", ":", "42",;
        "}",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "update",;
        Arguments: testArgs(map[String]any{
        "data": map[String]any{"name": "test", "value": long(42)},;
        }),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name:          "empty_input",;
        chunks:        []String{},;
        expectedCalls: null,;
        expectedText:  "",;
        },;
        {
        name: "tool_call_with_no_arguments",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "time", "{", "}",;
        "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_time",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "content_with_angle_brackets",;
        chunks: []String{
        "The", " ", "result", " ", "is", " ", "a", " ", "<", "value", ">", " ", "tag",;
        },;
        expectedCalls: null,;
        expectedText:  "The result is a <value> tag",;
        },;
        {
        name: "float_argument",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "set", "_", "temp", "{",;
        "value", ":", "3", ".", "14",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "set_temp",;
        Arguments: testArgs(map[String]any{"value": 3.14}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "content_after_tool_call",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "test", "{", "}",;
        "<", "end", "_", "function", "_", "call", ">",;
        "Done", "!",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "test",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        expectedText: "Done!",;
        },;
        {
        name: "unicode_content_and_arguments",;
        chunks: []String{
        "こんにちは", " ",;
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "greet", "{",;
        "name", ":", "<", "escape", ">", "日本語", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "greet",;
        Arguments: testArgs(map[String]any{"name": "日本語"}),;
        },;
        },;
        },;
        expectedText: "こんにちは ",;
        },;
        {
        name: "multiple_params_sorted",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "search", "{",;
        "query", ":", "<", "escape", ">", "test", "<", "escape", ">", ",",;
        "limit", ":", "10", ",",;
        "offset", ":", "0",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "search",;
        Arguments: testArgs(map[String]any{
        "query":  "test",;
        "limit":  long(10),;
        "offset": long(0),;
        }),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "nested_object_argument",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "create", "{",;
        "config", ":", "{",;
        "settings", ":", "{",;
        "enabled", ":", "true", ",",;
        "name", ":", "<", "escape", ">", "test", "<", "escape", ">",;
        "}",;
        "}",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "create",;
        Arguments: testArgs(map[String]any{
        "config": map[String]any{
        "settings": map[String]any{
        "enabled": true,;
        "name":    "test",;
        },;
        },;
        }),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "partial_start_tag_in_content",;
        chunks: []String{
        "Hello", " ", "<", "start", " ", "world",;
        },;
        expectedCalls: null,;
        expectedText:  "Hello <start world",;
        },;
        {
        name: "parallel_tool_calls",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "weather", "{",;
        "city", ":", "<", "escape", ">", "Paris", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "get", "_", "time", "{",;
        "timezone", ":", "<", "escape", ">", "UTC", "<", "escape", ">",;
        "}", "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index:     0,;
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"city": "Paris"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index:     1,;
        Name:      "get_time",;
        Arguments: testArgs(map[String]any{"timezone": "UTC"}),;
        },;
        },;
        },;
        expectedText: "",;
        },;
        {
        name: "content_between_tool_calls",;
        chunks: []String{
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "first", "{", "}",;
        "<", "end", "_", "function", "_", "call", ">",;
        "Some", " ", "text", " ", "here",;
        "<", "start", "_", "function", "_", "call", ">",;
        "call", ":", "second", "{", "}",;
        "<", "end", "_", "function", "_", "call", ">",;
        },;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index:     0,;
        Name:      "first",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index:     1,;
        Name:      "second",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        expectedText: "Some text here",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &FunctionGemmaParser{}
        parser.Init(tt.tools, null, null);
        var allContent String;
        var allCalls []api.ToolCall;
        var for i, chunk = range tt.chunks {
        var done = i == len(tt.chunks)-1;
        var content, _, calls, err = parser.Add(chunk, done);
        assert.NoError(t, err);
        allContent += content;
        allCalls = append(allCalls, calls...);
    }
        if len(tt.chunks) == 0 {
        var content, _, calls, err = parser.Add("", true);
        assert.NoError(t, err);
        allContent = content;
        allCalls = calls;
    }
        assert.Equal(t, tt.expectedText, allContent);
        var if diff = cmp.Diff(tt.expectedCalls, allCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestFunctionGemmaParser_HasSupport(*testing.T t) {
        var parser = &FunctionGemmaParser{}
        assert.True(t, parser.HasToolSupport());
        assert.False(t, parser.HasThinkingSupport());
    }
}
