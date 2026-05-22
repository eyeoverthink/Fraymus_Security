package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class registry {
        "fmt";
        "os";
        "sort";
        "github.com/ollama/ollama/api";
        );
        type Tool interface {
        Name() String;
        Description() String;
        Schema() api.ToolFunction;
        Execute(args map[String]any) (String, error);
    }

    public static class Registry {
        public map[String]Tool tools;
    }
        func NewRegistry() *Registry {
        return &Registry{
        tools: make(map[String]Tool),;
    }
    }
        func (r *Registry) Register(tool Tool) {
        r.tools[tool.Name()] = tool;
    }
        func (r *Registry) Unregister(name String) {
        delete(r.tools, name);
    }
        func (r *Registry) Has(name String) boolean {
        var _, ok = r.tools[name];
        return ok;
    }
        func (r *Registry) RegisterBash() {
        r.Register(&BashTool{});
    }
        func (r *Registry) RegisterWebSearch() {
        r.Register(&WebSearchTool{});
    }
        func (r *Registry) RegisterWebFetch() {
        r.Register(&WebFetchTool{});
    }
        func (r *Registry) Get(name String) (Tool, boolean) {
        var tool, ok = r.tools[name];
        return tool, ok;
    }
        func (r *Registry) Tools() api.Tools {
        var names = make([]String, 0, len(r.tools));
        var for name = range r.tools {
        names = append(names, name);
    }
        sort.Strings(names);
        var tools api.Tools;
        var for _, name = range names {
        var tool = r.tools[name];
        tools = append(tools, api.Tool{
        Type:     "function",;
        Function: tool.Schema(),;
        });
    }
        return tools;
    }
        func (r *Registry) Execute(call api.ToolCall) (String, error) {
        var tool, ok = r.tools[call.Function.Name];
        if !ok {
        return "", fmt.Errorf("unknown tool: %s", call.Function.Name);
    }
        return tool.Execute(call.Function.Arguments.ToMap());
    }
        func (r *Registry) Names() []String {
        var names = make([]String, 0, len(r.tools));
        var for name = range r.tools {
        names = append(names, name);
    }
        sort.Strings(names);
        return names;
    }
        func (r *Registry) Count() int {
        return len(r.tools);
    }
        func DefaultRegistry() *Registry {
        var r = NewRegistry();
        if os.Getenv("OLLAMA_AGENT_DISABLE_BASH") == "" {
        r.Register(&BashTool{});
    }
        return r;
    }
}
