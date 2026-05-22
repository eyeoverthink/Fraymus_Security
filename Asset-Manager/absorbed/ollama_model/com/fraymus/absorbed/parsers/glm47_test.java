package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class glm47_test {
        "reflect";
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestGLM47ParserAdd(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init([]api.Tool{
        tool("calculate", map[String]api.ToolProperty{
        "count":   {Type: api.PropertyType{"integer"}},;
        "enabled": {Type: api.PropertyType{"boolean"}},;
        }),;
        }, null, null);
        var content, thinking, calls, err = parser.Add("plan</think>Answer<tool_call>calculate<arg_key>count</arg_key><arg_value>3</arg_value><arg_key>enabled</arg_key><arg_value>true</arg_value></tool_call>", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "plan" {
        t.Fatalf("expected thinking 'plan', got %q", thinking);
    }
        if content != "Answer" {
        t.Fatalf("expected content 'Answer', got %q", content);
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 tool call, got %d", len(calls));
    }
        var expectedArgs = args(`{"count": 3, "enabled": true}`);
        if !toolCallEqual(api.ToolCall{Function: api.ToolCallFunction{Arguments: calls[0].Function.Arguments}}, api.ToolCall{Function: api.ToolCallFunction{Arguments: expectedArgs}}) {
        t.Fatalf("expected args %#v, got %#v", expectedArgs.ToMap(), calls[0].Function.Arguments.ToMap());
    }
    }

    public static void TestGLM47ParserNoThinkingContent(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init(null, null, null);
        var content, thinking, calls, err = parser.Add("</think>Plain answer", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if content != "Plain answer" {
        t.Fatalf("expected content 'Plain answer', got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestGLM47ParserThinkingDisabled(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init(null, null, &api.ThinkValue{Value: false});
        var content, thinking, calls, err = parser.Add("Plain answer", true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        if thinking != "" {
        t.Fatalf("expected empty thinking, got %q", thinking);
    }
        if content != "Plain answer" {
        t.Fatalf("expected content 'Plain answer', got %q", content);
    }
        if len(calls) != 0 {
        t.Fatalf("expected no tool calls, got %d", len(calls));
    }
    }

    public static void TestGLM47ParserToolCallEscaping(*testing.T t) {
        var toolCall, err = parseGLM46ToolCall(glm46EventRawToolCall{raw: `exec;
        <arg_key>expr</arg_key>;
        <arg_value>a < b && c > d</arg_value>`}, null);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        var expected = api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "exec",;
        Arguments: args(`{"expr": "a < b && c > d"}`),;
        },;
    }
        if !reflect.DeepEqual(toolCall, expected) {
        t.Fatalf("expected %#v, got %#v", expected, toolCall);
    }
    }

    public static void TestGLM47ParserToolCallIndexing(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init(null, null, null);
        var input = `plan</think>;
        <tool_call>first<arg_key>a</arg_key><arg_value>1</arg_value></tool_call>;
        <tool_call>second<arg_key>b</arg_key><arg_value>2</arg_value></tool_call>;
        <tool_call>third<arg_key>c</arg_key><arg_value>3</arg_value></tool_call>`;
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

    public static void TestGLM47ParserToolCallIndexingStreaming(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init(null, null, null);
        var all []api.ToolCall;
        var _, _, calls, err = parser.Add("plan</think><tool_call>first<arg_key>a</arg_key><arg_value>1</arg_value></tool_call><tool_call>second<arg_key>b</arg_key>", false);
        if err != null {
        t.Fatalf("step 1 parse failed: %v", err);
    }
        all = append(all, calls...);
        _, _, calls, err = parser.Add("<arg_value>2</arg_value></tool_call><tool_call>third<arg_key>c</arg_key><arg_value>3</arg_value></tool_call>", true);
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

    public static void TestGLM47ParserToolCallIndexResetOnInit(*testing.T t) {
        var parser = GLM47Parser{}
        parser.Init(null, null, null);
        var _, _, _, err = parser.Add("plan</think><tool_call>first<arg_key>a</arg_key><arg_value>1</arg_value></tool_call>", true);
        if err != null {
        t.Fatalf("first parse failed: %v", err);
    }
        parser.Init(null, null, null);
        var _, _, calls, err = parser.Add("plan</think><tool_call>second<arg_key>b</arg_key><arg_value>2</arg_value></tool_call>", true);
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
