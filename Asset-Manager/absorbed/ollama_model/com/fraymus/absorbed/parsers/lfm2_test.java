package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class lfm2_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestLFM2Parser(*testing.T t) {
        var tests = []struct {
        name             String;
        input            String;
        expectedContent  String;
        expectedThinking String;
        expectedCalls    []api.ToolCall;
        hasThinking      boolean;
        }{
        {
        name:            "simple_content",;
        input:           "Hello, how are you?",;
        expectedContent: "Hello, how are you?",;
        hasThinking:     false,;
        },;
        {
        name:             "thinking_content",;
        input:            "I need to think about this...</think>The answer is 42.",;
        expectedThinking: "I need to think about this...",;
        expectedContent:  "The answer is 42.",;
        hasThinking:      true,;
        },;
        {
        name:             "thinking_with_newlines",;
        input:            "Let me think:\n- Point 1\n- Point 2</think>\n\nHere's my answer.",;
        expectedThinking: "Let me think:\n- Point 1\n- Point 2",;
        expectedContent:  "Here's my answer.",;
        hasThinking:      true,;
        },;
        {
        name:            "tool_call_simple",;
        input:           "I'll check the weather.<|tool_call_start|>[get_weather(location=\"Paris\")]<|tool_call_end|>",;
        expectedContent: "I'll check the weather.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:            "multiple_tool_calls",;
        input:           "Getting weather for both cities.<|tool_call_start|>[get_weather(location=\"Paris\")]<|tool_call_end|><|tool_call_start|>[get_weather(location=\"London\")]<|tool_call_end|>",;
        expectedContent: "Getting weather for both cities.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index: 0,;
        Name:  "get_weather",;
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
        hasThinking: false,;
        },;
        {
        name:            "complex_tool_arguments",;
        input:           "Processing data.<|tool_call_start|>[process_data(items=['item1','item2'], config={'enabled': True, 'threshold': 0.95})]<|tool_call_end|>",;
        expectedContent: "Processing data.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "process_data",;
        Arguments: testArgs(map[String]any{
        "items":  []interface{}{"item1", "item2"},;
        "config": map[String]interface{}{"enabled": true, "threshold": 0.95},;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "thinking_with_tool_call",;
        input:            "Let me check the weather...</think>I'll get that for you.<|tool_call_start|>[get_weather(location=\"Paris\")]<|tool_call_end|>",;
        expectedThinking: "Let me check the weather...",;
        expectedContent:  "I'll get that for you.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        hasThinking: true,;
        },;
        {
        name:            "empty_content",;
        input:           "",;
        expectedContent: "",;
        hasThinking:     false,;
        },;
        {
        name:             "only_thinking",;
        input:            "Just thinking content</think>",;
        expectedThinking: "Just thinking content",;
        expectedContent:  "",;
        hasThinking:      true,;
        },;
        {
        name:            "unicode_content",;
        input:           "مرحبا بالعالم! 你好世界! 🌍",;
        expectedContent: "مرحبا بالعالم! 你好世界! 🌍",;
        hasThinking:     false,;
        },;
        {
        name:            "newlines_and_whitespace",;
        input:           "Line 1\n\nLine 3\t\tTabbed content",;
        expectedContent: "Line 1\n\nLine 3\t\tTabbed content",;
        hasThinking:     false,;
        },;
        {
        name:             "thinking_with_unicode",;
        input:            "我在思考这个问题...</think>答案是42。",;
        expectedThinking: "我在思考这个问题...",;
        expectedContent:  "答案是42。",;
        hasThinking:      true,;
        },;
        {
        name:            "tool_call_with_text_args",;
        input:           "Searching for information.<|tool_call_start|>[search(query='beijing weather', language='zh')]<|tool_call_end|>",;
        expectedContent: "Searching for information.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "search",;
        Arguments: testArgs(map[String]any{
        "query":    "beijing weather",;
        "language": "zh",;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "thinking_with_special_chars",;
        input:            "Let me calculate: 2+2=4 & 3*3=9...</think>The results are correct!",;
        expectedThinking: "Let me calculate: 2+2=4 & 3*3=9...",;
        expectedContent:  "The results are correct!",;
        hasThinking:      true,;
        },;
        {
        name:            "empty_tool_call_args",;
        input:           "Pinging server.<|tool_call_start|>[ping()]<|tool_call_end|>",;
        expectedContent: "Pinging server.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "ping",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:            "python_style_tool_call",;
        input:           "Let me check that.<|tool_call_start|>[get_candidate_status(candidate_id=\"12345\")]<|tool_call_end|>",;
        expectedContent: "Let me check that.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_candidate_status",;
        Arguments: testArgs(map[String]any{
        "candidate_id": "12345",;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:            "python_style_multiple_calls",;
        input:           "Running commands.<|tool_call_start|>[bash(command='ls'),bash(command='pwd')]<|tool_call_end|>",;
        expectedContent: "Running commands.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Index: 0,;
        Name:  "bash",;
        Arguments: testArgs(map[String]any{
        "command": "ls",;
        }),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Index: 1,;
        Name:  "bash",;
        Arguments: testArgs(map[String]any{
        "command": "pwd",;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "thinking_then_python_tool_call",;
        input:            "I should check the status...</think>Let me look that up.<|tool_call_start|>[get_status(id=\"123\")]<|tool_call_end|>",;
        expectedThinking: "I should check the status...",;
        expectedContent:  "Let me look that up.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_status",;
        Arguments: testArgs(map[String]any{
        "id": "123",;
        }),;
        },;
        },;
        },;
        hasThinking: true,;
        },;
        {
        name:            "python_style_no_args",;
        input:           "Pinging.<|tool_call_start|>[ping()]<|tool_call_end|>",;
        expectedContent: "Pinging.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "ping",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:            "python_style_mixed_types",;
        input:           "Processing.<|tool_call_start|>[process(name=\"test\", count=42, enabled=true)]<|tool_call_end|>",;
        expectedContent: "Processing.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "process",;
        Arguments: testArgs(map[String]any{
        "name":    "test",;
        "count":   long(42),;
        "enabled": true,;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:            "tool_call_only_no_content",;
        input:           "<|tool_call_start|>[check()]<|tool_call_end|>",;
        expectedContent: "",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "check",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "thinking_directly_to_tool_call",;
        input:            "Let me run this command...</think><|tool_call_start|>[bash(command='ls')]<|tool_call_end|>",;
        expectedThinking: "Let me run this command...",;
        expectedContent:  "",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "ls",;
        }),;
        },;
        },;
        },;
        hasThinking: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &LFM2Parser{hasThinkingSupport: tt.hasThinking}
        parser.Init([]api.Tool{}, null, &api.ThinkValue{Value: tt.hasThinking});
        var content, thinking, calls, err = parser.Add(tt.input, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        var if diff = cmp.Diff(tt.expectedContent, content); diff != "" {
        t.Errorf("Content mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedThinking, thinking); diff != "" {
        t.Errorf("Thinking mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedCalls, calls, argsComparer); diff != "" {
        t.Errorf("Tool calls mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestLFM2Parser_Streaming(*testing.T t) {
        var tests = []struct {
        name             String;
        chunks           []String;
        expectedContent  String;
        expectedThinking String;
        expectedCalls    []api.ToolCall;
        hasThinking      boolean;
        }{
        {
        name:            "streaming_simple_content",;
        chunks:          []String{"Hello, ", "how are ", "you?"},;
        expectedContent: "Hello, how are you?",;
        hasThinking:     false,;
        },;
        {
        name:             "streaming_thinking",;
        chunks:           []String{"I need to ", "think about this", "...</think>", "The answer is 42."},;
        expectedThinking: "I need to think about this...",;
        expectedContent:  "The answer is 42.",;
        hasThinking:      true,;
        },;
        {
        name:            "streaming_tool_call",;
        chunks:          []String{"I'll check weather.", "<|tool_call_start|>", "[get_weather(", "location=\"Paris\")]", "<|tool_call_end|>"},;
        expectedContent: "I'll check weather.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "streaming_thinking_with_partial_tag",;
        chunks:           []String{"Thinking about this", "...</", "think>", "Done thinking."},;
        expectedThinking: "Thinking about this...",;
        expectedContent:  "Done thinking.",;
        hasThinking:      true,;
        },;
        {
        name:            "streaming_unicode_content",;
        chunks:          []String{"مرحبا ", "بالعالم! ", "你好", "世界!"},;
        expectedContent: "مرحبا بالعالم! 你好世界!",;
        hasThinking:     false,;
        },;
        {
        name:            "streaming_tool_call_with_split_python",;
        chunks:          []String{"Processing.", "<|tool_call_start|>", "[calc(", "x=42, ", "y=24)]", "<|tool_call_end|>"},;
        expectedContent: "Processing.",;
        expectedCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "calc",;
        Arguments: testArgs(map[String]any{
        "x": long(42),;
        "y": long(24),;
        }),;
        },;
        },;
        },;
        hasThinking: false,;
        },;
        {
        name:             "streaming_thinking_whitespace_after_tag",;
        chunks:           []String{"<think>", "\n\n  ", "Actual thinking content", "</think>", "Response"},;
        expectedThinking: "Actual thinking content",;
        expectedContent:  "Response",;
        hasThinking:      true,;
        },;
        {
        name:             "streaming_whitespace_after_close_tag",;
        chunks:           []String{"<think>Thinking</think>", "\n\n\n", "Response content"},;
        expectedThinking: "Thinking",;
        expectedContent:  "Response content",;
        hasThinking:      true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &LFM2Parser{hasThinkingSupport: tt.hasThinking}
        parser.Init([]api.Tool{}, null, &api.ThinkValue{Value: tt.hasThinking});
        var allContent, allThinking String;
        var allCalls []api.ToolCall;
        var for i, chunk = range tt.chunks {
        var done = i == len(tt.chunks)-1;
        var content, thinking, calls, err = parser.Add(chunk, done);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        allContent += content;
        allThinking += thinking;
        allCalls = append(allCalls, calls...);
    }
        var if diff = cmp.Diff(tt.expectedContent, allContent); diff != "" {
        t.Errorf("Content mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedThinking, allThinking); diff != "" {
        t.Errorf("Thinking mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedCalls, allCalls, argsComparer); diff != "" {
        t.Errorf("Tool calls mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestLFM2Parser_HasThinkingSupport(*testing.T t) {
        var tests = []struct {
        name            String;
        hasThinking     boolean;
        expectedSupport boolean;
        }{
        {
        name:            "thinking_enabled",;
        hasThinking:     true,;
        expectedSupport: true,;
        },;
        {
        name:            "thinking_disabled",;
        hasThinking:     false,;
        expectedSupport: false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &LFM2Parser{hasThinkingSupport: tt.hasThinking}
        var if got = parser.HasThinkingSupport(); got != tt.expectedSupport {
        t.Errorf("HasThinkingSupport() = %v, want %v", got, tt.expectedSupport);
    }
        });
    }
    }

    public static void TestLFM2Parser_HasToolSupport(*testing.T t) {
        var parser = &LFM2Parser{}
        if !parser.HasToolSupport() {
        t.Error("HasToolSupport() should return true");
    }
    }

    public static void TestLFM2Parser_Init(*testing.T t) {
        var parser = &LFM2Parser{hasThinkingSupport: true}
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "test_tool",;
        },;
        },;
    }
        var returnedTools = parser.Init(tools, null, &api.ThinkValue{Value: true});
        var if diff = cmp.Diff(tools, returnedTools, toolsComparer); diff != "" {
        t.Errorf("Init() returned tools mismatch (-want +got):\n%s", diff);
    }
        if parser.state != LFM2CollectingThinking {
        t.Errorf("Expected initial state to be LFM2CollectingThinking, got %v", parser.state);
    }
    }

    public static void TestLFM2Parser_parseToolCallContent(*testing.T t) {
        var tests = []struct {
        name        String;
        content     String;
        expected    api.ToolCall;
        expectError boolean;
        }{
        {
        name:    "python_style_single_call",;
        content: `get_weather(location="Paris")`,;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        {
        name:    "python_style_with_brackets",;
        content: `[get_weather(location="Paris")]`,;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_weather",;
        Arguments: testArgs(map[String]any{
        "location": "Paris",;
        }),;
        },;
        },;
        },;
        {
        name:    "python_style_complex_arguments",;
        content: `process(items=['a', 'b'], config={'enabled': True})`,;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "process",;
        Arguments: testArgs(map[String]any{
        "items":  []any{"a", "b"},;
        "config": map[String]any{"enabled": true},;
        }),;
        },;
        },;
        },;
        {
        name:    "python_style_empty_arguments",;
        content: `ping()`,;
        expected: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "ping",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        {
        name:        "missing_parenthesis",;
        content:     `get_weather location="Paris")`,;
        expectError: true,;
        },;
        {
        name:        "invalid_argument_format",;
        content:     `bash(command)`,;
        expectError: true,;
        },;
    }
        var parser = &LFM2Parser{}
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = parser.parseToolCallContent(tt.content);
        if tt.expectError {
        if err == null {
        t.Error("Expected error but got none");
    }
        return;
    }
        if err != null {
        t.Fatalf("Unexpected error: %v", err);
    }
        var if diff = cmp.Diff(tt.expected, result, argsComparer); diff != "" {
        t.Errorf("parseToolCallContent() mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestLFM2Parser_parseToolCallsContent(*testing.T t) {
        var tests = []struct {
        name        String;
        content     String;
        expected    []api.ToolCall;
        expectError boolean;
        }{
        {
        name:    "multiple_python_style_calls",;
        content: `[bash(command='curl google.com'),bash(command='curl example.com')]`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "curl google.com",;
        }),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "curl example.com",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "python_style_complex_literals",;
        content: `[AskUserQuestion(question="What's up?", headers=['Hello!', 'How can I help you?'], options=['Debugging help', 'Code writing assistance'], multiSelect=False, metadata={'priority': 1, 'active': True})]`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "AskUserQuestion",;
        Arguments: testArgs(map[String]any{
        "question":    "What's up?",;
        "headers":     []any{"Hello!", "How can I help you?"},;
        "options":     []any{"Debugging help", "Code writing assistance"},;
        "multiSelect": false,;
        "metadata":    map[String]any{"priority": double(1), "active": true},;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "single_python_style_call",;
        content: `bash(command='ls -la')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "ls -la",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "single_bracketed_call",;
        content: `[bash(command='pwd')]`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "pwd",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "single_call_with_orphan_end_tag",;
        content: `[bash(command='ls')]<|tool_call_end|>`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "ls",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "single_call_with_wrapper_tags",;
        content: `<|tool_call_start|>[bash(command='pwd')]<|tool_call_end|>`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "pwd",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "multiple_different_functions",;
        content: `[get_weather(location='Paris'),search(query='news')]`,;
        expected: []api.ToolCall{
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
        Name: "search",;
        Arguments: testArgs(map[String]any{
        "query": "news",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "nested_parentheses_in_arg",;
        content: `bash(command='echo "(hello)"')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": `echo "(hello)"`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "comma_inside_quotes",;
        content: `bash(command='echo "hello, world"')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": `echo "hello, world"`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "equals_inside_quotes",;
        content: `bash(command='export FOO=bar')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": `export FOO=bar`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "double_quotes_with_single_inside",;
        content: `bash(command="echo 'hello'")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": `echo 'hello'`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "multiple_args",;
        content: `bash(command='ls', flag='-la', count=42)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": "ls",;
        "flag":    "-la",;
        "count":   long(42),;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "no_args",;
        content: `ping()`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "ping",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        },;
        },;
        },;
        {
        name:    "three_calls",;
        content: `[a(x='1'),b(y='2'),c(z='3')]`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "a",;
        Arguments: testArgs(map[String]any{"x": "1"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name:      "b",;
        Arguments: testArgs(map[String]any{"y": "2"}),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name:      "c",;
        Arguments: testArgs(map[String]any{"z": "3"}),;
        },;
        },;
        },;
        },;
        {
        name:    "escaped_quote_in_value",;
        content: `bash(command='echo \'hello\'')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "bash",;
        Arguments: testArgs(map[String]any{
        "command": `echo \'hello\'`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "docs_example_candidate_status",;
        content: `[get_candidate_status(candidate_id="12345")]`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_candidate_status",;
        Arguments: testArgs(map[String]any{
        "candidate_id": "12345",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "boolean_true_arg",;
        content: `configure(enabled=true)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "configure",;
        Arguments: testArgs(map[String]any{
        "enabled": true,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "boolean_false_arg",;
        content: `configure(enabled=false)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "configure",;
        Arguments: testArgs(map[String]any{
        "enabled": false,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "float_arg",;
        content: `set_threshold(value=0.95)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "set_threshold",;
        Arguments: testArgs(map[String]any{
        "value": 0.95,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "negative_number_arg",;
        content: `adjust(offset=-10)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "adjust",;
        Arguments: testArgs(map[String]any{
        "offset": long(-10),;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "mixed_arg_types",;
        content: `process(name="test", count=42, ratio=3.14, active=true)`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "process",;
        Arguments: testArgs(map[String]any{
        "name":   "test",;
        "count":  long(42),;
        "ratio":  3.14,;
        "active": true,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "newline_in_string_arg",;
        content: `write_file(content="line1\nline2\nline3")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "write_file",;
        Arguments: testArgs(map[String]any{
        "content": "line1\\nline2\\nline3",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "empty_string_arg",;
        content: `search(query="")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "search",;
        Arguments: testArgs(map[String]any{
        "query": "",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "underscore_function_name",;
        content: `get_user_profile(user_id="abc123")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "get_user_profile",;
        Arguments: testArgs(map[String]any{
        "user_id": "abc123",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "whitespace_around_args",;
        content: `func( arg1 = "value1" , arg2 = 42 )`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "func",;
        Arguments: testArgs(map[String]any{
        "arg1": "value1",;
        "arg2": long(42),;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "json_in_string_arg",;
        content: `send_data(payload='{"key": "value", "num": 123}')`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "send_data",;
        Arguments: testArgs(map[String]any{
        "payload": `{"key": "value", "num": 123}`,;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "url_in_arg",;
        content: `fetch(url="https://example.com/api?foo=bar&baz=qux")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "fetch",;
        Arguments: testArgs(map[String]any{
        "url": "https://example.com/api?foo=bar&baz=qux",;
        }),;
        },;
        },;
        },;
        },;
        {
        name:    "path_with_spaces",;
        content: `read_file(path="/home/user/My Documents/file.txt")`,;
        expected: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name: "read_file",;
        Arguments: testArgs(map[String]any{
        "path": "/home/user/My Documents/file.txt",;
        }),;
        },;
        },;
        },;
        },;
    }
        var parser = &LFM2Parser{}
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = parser.parseToolCallsContent(tt.content);
        if tt.expectError {
        if err == null {
        t.Error("Expected error but got none");
    }
        return;
    }
        if err != null {
        t.Fatalf("Unexpected error: %v", err);
    }
        var if diff = cmp.Diff(tt.expected, result, argsComparer); diff != "" {
        t.Errorf("parseToolCallsContent() mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestLFM2Parser_EdgeCases(*testing.T t) {
        var tests = []struct {
        name             String;
        input            String;
        expectedContent  String;
        expectedThinking String;
        hasThinking      boolean;
        }{
        {
        name:             "multiple_think_close_tags",;
        input:            "First thought</think>Second thought</think>Final content",;
        expectedThinking: "First thought",;
        expectedContent:  "Second thought</think>Final content",;
        hasThinking:      true,;
        },;
        {
        name:             "empty_thinking_content",;
        input:            "</think>Just content",;
        expectedThinking: "",;
        expectedContent:  "Just content",;
        hasThinking:      true,;
        },;
        {
        name:            "thinking_disabled_with_think_tags",;
        input:           "Some content</think>More content",;
        expectedContent: "Some content</think>More content",;
        hasThinking:     false,;
        },;
        {
        name:            "whitespace_only_content",;
        input:           "   \n\t   ",;
        expectedContent: "   \n\t   ",;
        hasThinking:     false,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &LFM2Parser{hasThinkingSupport: tt.hasThinking}
        parser.Init([]api.Tool{}, null, &api.ThinkValue{Value: tt.hasThinking});
        var content, thinking, _, err = parser.Add(tt.input, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        var if diff = cmp.Diff(tt.expectedContent, content); diff != "" {
        t.Errorf("Content mismatch (-want +got):\n%s", diff);
    }
        var if diff = cmp.Diff(tt.expectedThinking, thinking); diff != "" {
        t.Errorf("Thinking mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void TestLFM2Parser_BareToolCallFallback(*testing.T t) {
        var parser = &LFM2Parser{}
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add(`[get_weather(location="Paris")]`, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
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
        t.Fatalf("expected tool name get_weather, got %q", calls[0].Function.Name);
    }
    }

    public static void TestLFM2Parser_BareUnknownToolCallDoesNotParse(*testing.T t) {
        var parser = &LFM2Parser{}
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: false});
        var input = `[unknown_tool(location="Paris")]`;
        var content, _, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        if content != input {
        t.Fatalf("expected content to be preserved, got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestLFM2Parser_ImagePlaceholdersPreserved(*testing.T t) {
        var tests = []struct {
        name  String;
        input String;
        }{
        {
        name:  "indexed_img_placeholder",;
        input: "[img-0]describe this image",;
        },;
        {
        name:  "template_image_placeholder",;
        input: "<image>describe this image",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = &LFM2Parser{}
        var tools = []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name: "bash",;
        },;
        },;
    }
        parser.Init(tools, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add(tt.input, true);
        if err != null {
        t.Fatalf("Add() error = %v", err);
    }
        if content != tt.input {
        t.Fatalf("expected content %q, got %q", tt.input, content);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
        });
    }
    }
}
