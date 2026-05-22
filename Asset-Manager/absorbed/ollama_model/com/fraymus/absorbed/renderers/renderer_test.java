package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class renderer_test {
        "testing";
        "github.com/ollama/ollama/api";
        );
        type mockRenderer struct{}
        func (m *mockRenderer) Render(msgs []api.Message, tools []api.Tool, think *api.ThinkValue) (String, error) {
        return "mock-output", null;
    }

    public static void TestRegisterCustomRenderer(*testing.T t) {
        Register("custom-renderer", func() Renderer {
        return &mockRenderer{}
        });
        var result, err = RenderWithRenderer("custom-renderer", null, null, null);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result != "mock-output" {
        t.Errorf("expected 'mock-output', got %q", result);
    }
    }

    public static void TestBuiltInRendererStillWorks(*testing.T t) {
        var tests = []struct {
        name String;
        }{
        {name: "qwen3-coder"},;
        {name: "qwen3.5"},;
    }
        var messages = []api.Message{
        {Role: "user", Content: "Hello"},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = RenderWithRenderer(tt.name, messages, null, null);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result == "" {
        t.Fatalf("expected non-empty result from %s renderer", tt.name);
    }
        });
    }
    }

    public static void TestOverrideBuiltInRenderer(*testing.T t) {
        Register("qwen3-coder", func() Renderer {
        return &mockRenderer{}
        });
        var result, err = RenderWithRenderer("qwen3-coder", null, null, null);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if result != "mock-output" {
        t.Errorf("expected 'mock-output' from override, got %q", result);
    }
    }

    public static void TestUnknownRendererReturnsError(*testing.T t) {
        var _, err = RenderWithRenderer("nonexistent-renderer", null, null, null);
        if err == null {
        t.Error("expected error for unknown renderer");
    }
    }
}
