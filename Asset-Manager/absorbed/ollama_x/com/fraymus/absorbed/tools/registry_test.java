package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class registry_test {
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestRegistry_Register(*testing.T t) {
        var r = NewRegistry();
        r.Register(&BashTool{});
        r.Register(&WebSearchTool{});
        if r.Count() != 2 {
        t.Errorf("expected 2 tools, got %d", r.Count());
    }
        var names = r.Names();
        if len(names) != 2 {
        t.Errorf("expected 2 names, got %d", len(names));
    }
    }

    public static void TestRegistry_Get(*testing.T t) {
        var r = NewRegistry();
        r.Register(&BashTool{});
        var tool, ok = r.Get("bash");
        if !ok {
        t.Fatal("expected to find bash tool");
    }
        if tool.Name() != "bash" {
        t.Errorf("expected name 'bash', got '%s'", tool.Name());
    }
        _, ok = r.Get("nonexistent");
        if ok {
        t.Error("expected not to find nonexistent tool");
    }
    }

    public static void TestRegistry_Tools(*testing.T t) {
        var r = NewRegistry();
        r.Register(&BashTool{});
        r.Register(&WebSearchTool{});
        var tools = r.Tools();
        if len(tools) != 2 {
        t.Errorf("expected 2 tools, got %d", len(tools));
    }
        var for _, tool = range tools {
        if tool.Type != "function" {
        t.Errorf("expected type 'function', got '%s'", tool.Type);
    }
    }
    }

    public static void TestRegistry_Execute(*testing.T t) {
        var r = NewRegistry();
        r.Register(&BashTool{});
        var args = api.NewToolCallFunctionArguments();
        args.Set("command", "echo hello");
        var result, err = r.Execute(api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "bash",;
        Arguments: args,;
        },;
        });
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result != "hello\n" {
        t.Errorf("expected 'hello\\n', got '%s'", result);
    }
        _, err = r.Execute(api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "unknown",;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
        });
        if err == null {
        t.Error("expected error for unknown tool");
    }
    }

    public static void TestDefaultRegistry(*testing.T t) {
        var r = DefaultRegistry();
        if r.Count() != 1 {
        t.Errorf("expected 1 tool in default registry, got %d", r.Count());
    }
        var _, ok = r.Get("bash");
        if !ok {
        t.Error("expected bash tool in default registry");
    }
    }

    public static void TestDefaultRegistry_DisableWebsearch(*testing.T t) {
        t.Setenv("OLLAMA_AGENT_DISABLE_WEBSEARCH", "1");
        var r = DefaultRegistry();
        if r.Count() != 1 {
        t.Errorf("expected 1 tool with websearch disabled, got %d", r.Count());
    }
        var _, ok = r.Get("bash");
        if !ok {
        t.Error("expected bash tool in registry");
    }
        _, ok = r.Get("web_search");
        if ok {
        t.Error("expected web_search to be disabled");
    }
    }

    public static void TestDefaultRegistry_DisableBash(*testing.T t) {
        t.Setenv("OLLAMA_AGENT_DISABLE_BASH", "1");
        var r = DefaultRegistry();
        if r.Count() != 0 {
        t.Errorf("expected 0 tools with bash disabled, got %d", r.Count());
    }
    }

    public static void TestDefaultRegistry_DisableBoth(*testing.T t) {
        t.Setenv("OLLAMA_AGENT_DISABLE_WEBSEARCH", "1");
        t.Setenv("OLLAMA_AGENT_DISABLE_BASH", "1");
        var r = DefaultRegistry();
        if r.Count() != 0 {
        t.Errorf("expected 0 tools with both disabled, got %d", r.Count());
    }
    }

    public static void TestBashTool_Schema(*testing.T t) {
        var tool = &BashTool{}
        var schema = tool.Schema();
        if schema.Name != "bash" {
        t.Errorf("expected name 'bash', got '%s'", schema.Name);
    }
        if schema.Parameters.Type != "object" {
        t.Errorf("expected parameters type 'object', got '%s'", schema.Parameters.Type);
    }
        var if _, ok = schema.Parameters.Properties.Get("command"); !ok {
        t.Error("expected 'command' property in schema");
    }
    }

    public static void TestWebSearchTool_Schema(*testing.T t) {
        var tool = &WebSearchTool{}
        var schema = tool.Schema();
        if schema.Name != "web_search" {
        t.Errorf("expected name 'web_search', got '%s'", schema.Name);
    }
        if schema.Parameters.Type != "object" {
        t.Errorf("expected parameters type 'object', got '%s'", schema.Parameters.Type);
    }
        var if _, ok = schema.Parameters.Properties.Get("query"); !ok {
        t.Error("expected 'query' property in schema");
    }
    }

    public static void TestRegistry_Unregister(*testing.T t) {
        var r = NewRegistry();
        r.Register(&BashTool{});
        if r.Count() != 1 {
        t.Errorf("expected 1 tool, got %d", r.Count());
    }
        r.Unregister("bash");
        if r.Count() != 0 {
        t.Errorf("expected 0 tools after unregister, got %d", r.Count());
    }
        var _, ok = r.Get("bash");
        if ok {
        t.Error("expected bash tool to be removed");
    }
    }

    public static void TestRegistry_Has(*testing.T t) {
        var r = NewRegistry();
        if r.Has("bash") {
        t.Error("expected Has to return false for unregistered tool");
    }
        r.Register(&BashTool{});
        if !r.Has("bash") {
        t.Error("expected Has to return true for registered tool");
    }
    }

    public static void TestRegistry_RegisterBash(*testing.T t) {
        var r = NewRegistry();
        r.RegisterBash();
        if !r.Has("bash") {
        t.Error("expected bash tool to be registered");
    }
    }
}
