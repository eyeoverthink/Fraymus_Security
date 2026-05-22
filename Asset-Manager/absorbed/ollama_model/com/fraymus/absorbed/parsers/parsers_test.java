package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class parsers_test {
        "strings";
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static class mockParser {
        public String name;
    }
        func (m *mockParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        return tools;
    }
        func (m *mockParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        return "mock:" + s, "", null, null;
    }
        func (m *mockParser) HasToolSupport() boolean {
        return false;
    }
        func (m *mockParser) HasThinkingSupport() boolean {
        return false;
    }

    public static void TestRegisterCustomParser(*testing.T t) {
        Register("custom-parser", func() Parser {
        return &mockParser{name: "custom"}
        });
        var parser = ParserForName("custom-parser");
        if parser == null {
        t.Fatal("expected parser to be registered");
    }
        var content, _, _, err = parser.Add("test", false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if content != "mock:test" {
        t.Errorf("expected 'mock:test', got %q", content);
    }
    }

    public static void TestBuiltInParsersStillWork(*testing.T t) {
        var tests = []struct {
        name String;
        }{
        {"passthrough"},;
        {"qwen3"},;
        {"qwen3-thinking"},;
        {"qwen3-coder"},;
        {"lfm2"},;
        {"lfm2-thinking"},;
        {"qwen3.5"},;
        {"harmony"},;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var parser = ParserForName(tt.name);
        if parser == null {
        t.Fatalf("expected built-in parser %q to exist", tt.name);
    }
        });
    }
    }

    public static void TestOverrideBuiltInParser(*testing.T t) {
        Register("passthrough", func() Parser {
        return &mockParser{name: "override"}
        });
        var parser = ParserForName("passthrough");
        if parser == null {
        t.Fatal("expected parser to exist");
    }
        var content, _, _, err = parser.Add("test", false);
        if err != null {
        t.Fatalf("unexpected error: %v", err);
    }
        if content != "mock:test" {
        t.Errorf("expected 'mock:test' from override, got %q", content);
    }
    }

    public static void TestUnknownParserReturnsNil(*testing.T t) {
        var parser = ParserForName("nonexistent-parser");
        if parser != null {
        t.Error("expected null for unknown parser");
    }
    }

    public static void TestSplitAtTag(*testing.T t) {
        var tests = []struct {
        name       String;
        input      String;
        tag        String;
        trimAfter  boolean;
        wantBefore String;
        wantAfter  String;
        wantSB     String // expected content of strings.Builder after operation;
        }{
        {
        name:       "basic split with trimAfter true",;
        input:      "hello <!-- split --> world",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "hello",;
        wantAfter:  "world",;
        wantSB:     "world",;
        },;
        {
        name:       "basic split with trimAfter false",;
        input:      "hello <!-- split -->   world",;
        tag:        "<!-- split -->",;
        trimAfter:  false,;
        wantBefore: "hello",;
        wantAfter:  "   world",;
        wantSB:     "   world",;
        },;
        {
        name:       "tag at beginning with trimAfter true",;
        input:      "<!-- split -->world",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "",;
        wantAfter:  "world",;
        wantSB:     "world",;
        },;
        {
        name:       "tag at beginning with trimAfter false",;
        input:      "<!-- split -->   world",;
        tag:        "<!-- split -->",;
        trimAfter:  false,;
        wantBefore: "",;
        wantAfter:  "   world",;
        wantSB:     "   world",;
        },;
        {
        name:       "tag at end with trimAfter true",;
        input:      "hello <!-- split -->",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "hello",;
        wantAfter:  "",;
        wantSB:     "",;
        },;
        {
        name:       "tag at end with trimAfter false",;
        input:      "hello <!-- split -->",;
        tag:        "<!-- split -->",;
        trimAfter:  false,;
        wantBefore: "hello",;
        wantAfter:  "",;
        wantSB:     "",;
        },;
        {
        name:       "multiple tags splits at first occurrence",;
        input:      "hello <!-- split --> world <!-- split --> end",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "hello",;
        wantAfter:  "world <!-- split --> end",;
        wantSB:     "world <!-- split --> end",;
        },;
        {
        name:       "tag not present",;
        input:      "hello world",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "hello world",;
        wantAfter:  "",;
        wantSB:     "",;
        },;
        {
        name:       "empty input",;
        input:      "",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "",;
        wantAfter:  "",;
        wantSB:     "",;
        },;
        {
        name:       "only whitespace before tag",;
        input:      "   \t\n<!-- split -->world",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "",;
        wantAfter:  "world",;
        wantSB:     "world",;
        },;
        {
        name:       "only whitespace after tag with trimAfter true",;
        input:      "hello<!-- split -->   \t\n",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "hello",;
        wantAfter:  "",;
        wantSB:     "",;
        },;
        {
        name:       "only whitespace after tag with trimAfter false",;
        input:      "hello<!-- split -->   \t\n",;
        tag:        "<!-- split -->",;
        trimAfter:  false,;
        wantBefore: "hello",;
        wantAfter:  "   \t\n",;
        wantSB:     "   \t\n",;
        },;
        {
        name:       "complex whitespace trimming",;
        input:      "  hello \t\n <!-- split --> \n\t world  ",;
        tag:        "<!-- split -->",;
        trimAfter:  true,;
        wantBefore: "  hello",;
        wantAfter:  "world  ",;
        wantSB:     "world  ",;
        },;
        {
        name:       "tag with special characters",;
        input:      "text <tag attr=\"value\"> more text",;
        tag:        "<tag attr=\"value\">",;
        trimAfter:  true,;
        wantBefore: "text",;
        wantAfter:  "more text",;
        wantSB:     "more text",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var sb = &strings.Builder{}
        sb.WriteString(tt.input);
        var before, after = splitAtTag(sb, tt.tag, tt.trimAfter);
        if before != tt.wantBefore {
        t.Errorf("splitAtTag() before = %q, want %q", before, tt.wantBefore);
    }
        if after != tt.wantAfter {
        t.Errorf("splitAtTag() after = %q, want %q", after, tt.wantAfter);
    }
        if sb.String() != tt.wantSB {
        t.Errorf("strings.Builder after split = %q, want %q", sb.String(), tt.wantSB);
    }
        });
    }
    }
}
