package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class olmo3_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestOlmo3Parser(*testing.T t) {
        var tests = []struct {
        name             String;
        input            String;
        expectedContent  String;
        expectedThinking String;
        expectedCalls    []api.ToolCall;
        }{
        {
        name:            "simple content",;
        input:           "Hello, how can I help you?",;
        expectedContent: "Hello, how can I help you?",;
        },;
        {
        name:  "simple tool call",;
        input: `<function_calls>get_weather(location="San Francisco")</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "San Francisco"}),;
        },;
        },;
        },;
        },;
        {
        name:            "content then tool call",;
        input:           `Let me check the weather.<function_calls>get_weather(location="NYC")</function_calls>`,;
        expectedContent: "Let me check the weather.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "NYC"}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with multiple arguments",;
        input: `<function_calls>book_flight(from="SFO", to="NYC", date="2024-01-15")</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "book_flight",;
        Arguments: testArgs(map[String]any{
        "from": "SFO",;
        "to":   "NYC",;
        "date": "2024-01-15",;
        }),;
        },;
        },;
        },;
        },;
        {
        name: "multiple tool calls",;
        input: `<function_calls>get_weather(location="San Francisco");
        get_weather(location="New York")</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index:     0,;
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "San Francisco"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index:     1,;
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "New York"}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with numeric argument",;
        input: `<function_calls>set_temperature(value=72)</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "set_temperature",;
        Arguments: testArgs(map[String]any{"value": long(72)}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with float argument",;
        input: `<function_calls>set_price(amount=19.99)</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "set_price",;
        Arguments: testArgs(map[String]any{"amount": 19.99}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with boolean argument",;
        input: `<function_calls>toggle_setting(enabled=true)</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "toggle_setting",;
        Arguments: testArgs(map[String]any{"enabled": true}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with null argument",;
        input: `<function_calls>clear_value(field=null)</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "clear_value",;
        Arguments: testArgs(map[String]any{"field": null}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with array argument",;
        input: `<function_calls>process_items(items=["apple", "banana", "cherry"])</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "process_items",;
        Arguments: testArgs(map[String]any{"items": []any{"apple", "banana", "cherry"}}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with dict argument",;
        input: `<function_calls>update_config(settings={"theme": "dark", "fontSize": 14})</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "update_config",;
        Arguments: testArgs(map[String]any{
        "settings": map[String]any{
        "theme":    "dark",;
        "fontSize": long(14),;
        },;
        }),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with nested dict",;
        input: `<function_calls>create_request(data={"user": {"name": "John", "age": 30}, "active": true})</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "create_request",;
        Arguments: testArgs(map[String]any{
        "data": map[String]any{
        "user": map[String]any{
        "name": "John",;
        "age":  long(30),;
        },;
        "active": true,;
        },;
        }),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with no arguments",;
        input: `<function_calls>get_current_time()</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_current_time",;
        Arguments: testArgs(map[String]any{}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with single quotes",;
        input: `<function_calls>search(query='hello world')</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "search",;
        Arguments: testArgs(map[String]any{"query": "hello world"}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with escaped quotes",;
        input: `<function_calls>search(query="say \"hello\"")</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "search",;
        Arguments: testArgs(map[String]any{"query": `say "hello"`}),;
        },;
        },;
        },;
        },;
        {
        name:  "tool call with mixed argument types",;
        input: `<function_calls>create_user(name="John", age=30, active=true)</function_calls>`,;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "create_user",;
        Arguments: testArgs(map[String]any{
        "name":   "John",;
        "age":    long(30),;
        "active": true,;
        }),;
        },;
        },;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var p = &Olmo3Parser{}
        p.Init(null, null, null);
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

    public static void TestOlmo3Parser_Streaming(*testing.T t) {
        var tests = []struct {
        name            String;
        chunks          []String;
        expectedContent String;
        expectedCalls   []api.ToolCall;
        }{
        {
        name:            "streaming content",;
        chunks:          []String{"Hello, ", "how ", "can I help?"},;
        expectedContent: "Hello, how can I help?",;
        },;
        {
        name:   "streaming tool call",;
        chunks: []String{"<function_", "calls>get_weather", "(location=\"SF\")", "</function_calls>"},;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "SF"}),;
        },;
        },;
        },;
        },;
        {
        name:            "streaming content then tool call",;
        chunks:          []String{"Let me check.", "<function_calls>", "get_weather(location=\"NYC\")", "</function_calls>"},;
        expectedContent: "Let me check.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "NYC"}),;
        },;
        },;
        },;
        },;
        {
        name:   "tool call tag split across chunks",;
        chunks: []String{"<func", "tion_calls>test()</function_calls>"},;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "test",;
        Arguments: testArgs(map[String]any{}),;
        },;
        },;
        },;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var p = &Olmo3Parser{}
        p.Init(null, null, null);
        var allContent String;
        var allCalls []api.ToolCall;
        var for _, chunk = range tt.chunks {
        var content, _, calls, err = p.Add(chunk, false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        allContent += content;
        allCalls = append(allCalls, calls...);
    }
        var content, _, calls, err = p.Add("", true);
        if err != null {
        t.Fatalf("unexpected error on done: %v", err);
    }
        allContent += content;
        allCalls = append(allCalls, calls...);
        var if diff = cmp.Diff(allContent, tt.expectedContent); diff != "" {
        t.Errorf("content mismatch (-got +want):\n%s", diff);
    }
        var if diff = cmp.Diff(allCalls, tt.expectedCalls, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestOlmo3Parser_HasToolSupport(*testing.T t) {
        var p = &Olmo3Parser{}
        if !p.HasToolSupport() {
        t.Error("expected HasToolSupport to return true");
    }
    }

    public static void TestOlmo3Parser_HasThinkingSupport(*testing.T t) {
        var p = &Olmo3Parser{}
        if p.HasThinkingSupport() {
        t.Error("expected HasThinkingSupport to return false");
    }
    }

    public static void TestParseOlmo3FunctionCalls(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected []api.ToolCall;
        wantErr  boolean;
        }{
        {
        name:  "simple call",;
        input: `get_weather(location="SF")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "SF"}),;
        },;
        },;
        },;
        },;
        {
        name:  "multiple args",;
        input: `send_email(to="user@example.com", subject="Hello", body="Test message")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "send_email",;
        Arguments: testArgs(map[String]any{
        "to":      "user@example.com",;
        "subject": "Hello",;
        "body":    "Test message",;
        }),;
        },;
        },;
        },;
        },;
        {
        name: "multiple calls with newlines",;
        input: `get_weather(location="SF");
        get_time(timezone="PST")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: testArgs(map[String]any{"location": "SF"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name:      "get_time",;
        Arguments: testArgs(map[String]any{"timezone": "PST"}),;
        },;
        },;
        },;
        },;
        {
        name:     "empty input",;
        input:    "",;
        expected: null,;
        },;
        {
        name:     "whitespace only",;
        input:    "   \n   ",;
        expected: null,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var calls, err = parseOlmo3FunctionCalls(tt.input);
        if (err != null) != tt.wantErr {
        t.Errorf("parseOlmo3FunctionCalls() error = %v, wantErr %v", err, tt.wantErr);
        return;
    }
        var if diff = cmp.Diff(calls, tt.expected, argsComparer); diff != "" {
        t.Errorf("calls mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestParseOlmo3Value(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected any;
        }{
        {"String double quotes", `"hello"`, "hello"},;
        {"String single quotes", `'hello'`, "hello"},;
        {"integer", "42", long(42)},;
        {"negative integer", "-10", long(-10)},;
        {"float", "3.14", 3.14},;
        {"boolean true", "true", true},;
        {"boolean True", "True", true},;
        {"boolean false", "false", false},;
        {"null", "null", null},;
        {"None", "None", null},;
        {"empty array", "[]", []any{}},;
        {"array with strings", `["a", "b"]`, []any{"a", "b"}},;
        {"array with numbers", "[1, 2, 3]", []any{long(1), long(2), long(3)}},;
        {"empty object", "{}", map[String]any{}},;
        {"simple object", `{"name": "John"}`, map[String]any{"name": "John"}},;
        {"object with number", `{"age": 30}`, map[String]any{"age": long(30)}},;
        {"object with multiple keys", `{"a": 1, "b": 2}`, map[String]any{"a": long(1), "b": long(2)}},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = parseOlmo3Value(tt.input);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        var if diff = cmp.Diff(result, tt.expected); diff != "" {
        t.Errorf("value mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }
}
