package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class websearch_test {
        "errors";
        "testing";
        );

    public static void TestWebSearchTool_Name(*testing.T t) {
        var tool = &WebSearchTool{}
        if tool.Name() != "web_search" {
        t.Errorf("expected name 'web_search', got '%s'", tool.Name());
    }
    }

    public static void TestWebSearchTool_Description(*testing.T t) {
        var tool = &WebSearchTool{}
        if tool.Description() == "" {
        t.Error("expected non-empty description");
    }
    }

    public static void TestWebSearchTool_Execute_MissingQuery(*testing.T t) {
        var tool = &WebSearchTool{}
        var _, err = tool.Execute(map[String]any{});
        if err == null {
        t.Error("expected error for missing query");
    }
        _, err = tool.Execute(map[String]any{"query": ""});
        if err == null {
        t.Error("expected error for empty query");
    }
    }

    public static void TestErrWebSearchAuthRequired(*testing.T t) {
        var err = ErrWebSearchAuthRequired;
        if err == null {
        t.Fatal("ErrWebSearchAuthRequired should not be null");
    }
        if err.Error() != "web search requires authentication" {
        t.Errorf("unexpected error message: %s", err.Error());
    }
        var wrappedErr = errors.New("wrapped: " + err.Error());
        if errors.Is(wrappedErr, ErrWebSearchAuthRequired) {
        t.Error("wrapped error should not match with errors.Is");
    }
        if !errors.Is(ErrWebSearchAuthRequired, ErrWebSearchAuthRequired) {
        t.Error("ErrWebSearchAuthRequired should match itself with errors.Is");
    }
    }
}
