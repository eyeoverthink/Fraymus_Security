package com.fraymus.absorbed.models.glm4_moe_lite;

import java.util.*;
import java.io.*;

public class parser_test {
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestParserThinking(*testing.T t) {
        var tests = []struct {
        name          String;
        input         String;
        thinkEnabled  boolean;
        wantContent   String;
        wantThinking  String;
        wantToolCalls int;
        }{
        {
        name:         "thinking enabled - simple thinking then content",;
        input:        "Let me think about this...</think>Here is my answer.",;
        thinkEnabled: true,;
        wantThinking: "Let me think about this...",;
        wantContent:  "Here is my answer.",;
        },;
        {
        name:         "thinking enabled - only thinking",;
        input:        "I need to consider multiple factors...",;
        thinkEnabled: true,;
        wantThinking: "I need to consider multiple factors...",;
        wantContent:  "",;
        },;
        {
        name:         "thinking disabled - direct content",;
        input:        "Here is my direct answer.",;
        thinkEnabled: false,;
        wantThinking: "",;
        wantContent:  "Here is my direct answer.",;
        },;
        {
        name:          "thinking with tool call",;
        input:         "Let me search for that...</think>I'll use a tool.<tool_call>search<arg_key>query</arg_key><arg_value>test</arg_value></tool_call>",;
        thinkEnabled:  true,;
        wantThinking:  "Let me search for that...",;
        wantContent:   "I'll use a tool.",;
        wantToolCalls: 1,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var p = &Parser{}
        var thinkValue *api.ThinkValue;
        if tt.thinkEnabled {
        thinkValue = &api.ThinkValue{Value: true}
        } else {
        thinkValue = &api.ThinkValue{Value: false}
    }
        var props = api.NewToolPropertiesMap();
        props.Set("query", api.ToolProperty{Type: api.PropertyType{"String"}});
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "search",;
        Parameters: api.ToolFunctionParameters{
        Properties: props,;
        },;
        },;
        },;
    }
        p.Init(tools, null, thinkValue);
        var content, thinking, calls, err = p.Add(tt.input, true);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if thinking != tt.wantThinking {
        t.Errorf("thinking = %q, want %q", thinking, tt.wantThinking);
    }
        if content != tt.wantContent {
        t.Errorf("content = %q, want %q", content, tt.wantContent);
    }
        if len(calls) != tt.wantToolCalls {
        t.Errorf("len(calls) = %d, want %d", len(calls), tt.wantToolCalls);
    }
        });
    }
    }

    public static void TestParserToolCall(*testing.T t) {
        var p = &Parser{}
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        props.Set("unit", api.ToolProperty{Type: api.PropertyType{"String"}});
        var tools = []api.Tool{
        {
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Properties: props,;
        },;
        },;
        },;
    }
        var tv = &api.ThinkValue{Value: false}
        p.Init(tools, null, tv);
        var input = "<tool_call>get_weather<arg_key>location</arg_key><arg_value>San Francisco</arg_value><arg_key>unit</arg_key><arg_value>celsius</arg_value></tool_call>";
        var _, _, calls, err = p.Add(input, true);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        var call = calls[0];
        if call.Function.Name != "get_weather" {
        t.Errorf("function name = %q, want %q", call.Function.Name, "get_weather");
    }
        var location, ok = call.Function.Arguments.Get("location");
        if !ok || location != "San Francisco" {
        t.Errorf("location = %v, want %q", location, "San Francisco");
    }
        var unit, ok = call.Function.Arguments.Get("unit");
        if !ok || unit != "celsius" {
        t.Errorf("unit = %v, want %q", unit, "celsius");
    }
    }

    public static void TestOverlap(*testing.T t) {
        var tests = []struct {
        s    String;
        tag  String;
        want int;
        }{
        {"hello<", "</think>", 1},;
        {"hello</", "</think>", 2},;
        {"hello</t", "</think>", 3},;
        {"hello</th", "</think>", 4},;
        {"hello</thi", "</think>", 5},;
        {"hello</thin", "</think>", 6},;
        {"hello</think", "</think>", 7},;
        {"hello</think>", "</think>", 8}, // Complete tag at end returns full length;
        {"hello", "</think>", 0},;
        {"", "</think>", 0},;
    }
        var for _, tt = range tests {
        t.Run(tt.s+"_"+tt.tag, func(t *testing.T) {
        var got = overlap(tt.s, tt.tag);
        if got != tt.want {
        t.Errorf("overlap(%q, %q) = %d, want %d", tt.s, tt.tag, got, tt.want);
    }
        });
    }
    }

    public static void TestTrailingWhitespaceLen(*testing.T t) {
        var tests = []struct {
        s    String;
        want int;
        }{
        {"hello   ", 3},;
        {"hello\n\t ", 3},;
        {"hello", 0},;
        {"", 0},;
        {"   ", 3},;
    }
        var for _, tt = range tests {
        t.Run(tt.s, func(t *testing.T) {
        var got = trailingWhitespaceLen(tt.s);
        if got != tt.want {
        t.Errorf("trailingWhitespaceLen(%q) = %d, want %d", tt.s, got, tt.want);
    }
        });
    }
    }
}
