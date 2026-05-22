package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class template_test {
        "bufio";
        "bytes";
        "encoding/json";
        "io";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        "time";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static void TestNamed(*testing.T t) {
        var f, err = os.Open(filepath.Join("testdata", "templates.jsonl"));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var scanner = bufio.NewScanner(f);
        for scanner.Scan() {
        var ss map[String]String;
        var if err = json.Unmarshal(scanner.Bytes(), &ss); err != null {
        t.Fatal(err);
    }
        var for k, v = range ss {
        t.Run(k, func(t *testing.T) {
        var kv = ggml.KV{"tokenizer.chat_template": v}
        var s = kv.ChatTemplate();
        var r, err = Named(s);
        if err != null {
        t.Fatal(err);
    }
        if r.Name != k {
        t.Errorf("expected %q, got %q", k, r.Name);
    }
        var b bytes.Buffer;
        var if _, err = io.Copy(&b, r.Reader()); err != null {
        t.Fatal(err);
    }
        var tmpl, err = Parse(b.String());
        if err != null {
        t.Fatal(err);
    }
        if tmpl.Tree.Root.String() == "" {
        t.Errorf("empty %s template", k);
    }
        });
    }
    }
    }

    public static void TestTemplate(*testing.T t) {
        var cases = make(map[String][]api.Message);
        var for _, mm = range [][]api.Message{
        {
        {Role: "user", Content: "Hello, how are you?"},;
        },;
        {
        {Role: "user", Content: "Hello, how are you?"},;
        {Role: "assistant", Content: "I'm doing great. How can I help you today?"},;
        {Role: "user", Content: "I'd like to show off how chat templating works!"},;
        },;
        {
        {Role: "system", Content: "You are a helpful assistant."},;
        {Role: "user", Content: "Hello, how are you?"},;
        {Role: "assistant", Content: "I'm doing great. How can I help you today?"},;
        {Role: "user", Content: "I'd like to show off how chat templating works!"},;
        },;
        } {
        var roles []String;
        var for _, m = range mm {
        roles = append(roles, m.Role);
    }
        cases[strings.Join(roles, "-")] = mm;
    }
        var matches, err = filepath.Glob("*.gotmpl");
        if err != null {
        t.Fatal(err);
    }
        var for _, match = range matches {
        t.Run(match, func(t *testing.T) {
        var bts, err = os.ReadFile(match);
        if err != null {
        t.Fatal(err);
    }
        var tmpl, err = Parse(String(bts));
        if err != null {
        t.Fatal(err);
    }
        var for n, tt = range cases {
        var actual bytes.Buffer;
        t.Run(n, func(t *testing.T) {
        var if err = tmpl.Execute(&actual, Values{Messages: tt}); err != null {
        t.Fatal(err);
    }
        var expect, err = os.ReadFile(filepath.Join("testdata", match, n));
        if err != null {
        t.Fatal(err);
    }
        var bts = actual.Bytes();
        if slices.Contains([]String{"chatqa.gotmpl", "llama2-chat.gotmpl", "mistral-instruct.gotmpl", "openchat.gotmpl", "vicuna.gotmpl"}, match) && bts[len(bts)-1] == ' ' {
        t.Log("removing trailing space from output");
        bts = bts[:len(bts)-1];
    }
        var if diff = cmp.Diff(bts, expect); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
        t.Run("legacy", func(t *testing.T) {
        t.Skip("legacy outputs are currently default outputs");
        var legacy bytes.Buffer;
        var if err = tmpl.Execute(&legacy, Values{Messages: tt, forceLegacy: true}); err != null {
        t.Fatal(err);
    }
        var legacyBytes = legacy.Bytes();
        if slices.Contains([]String{"chatqa.gotmpl", "openchat.gotmpl", "vicuna.gotmpl"}, match) && legacyBytes[len(legacyBytes)-1] == ' ' {
        t.Log("removing trailing space from legacy output");
        legacyBytes = legacyBytes[:len(legacyBytes)-1];
        } else if slices.Contains([]String{"codellama-70b-instruct.gotmpl", "llama2-chat.gotmpl", "mistral-instruct.gotmpl"}, match) {
        t.Skip("legacy outputs cannot be compared to messages outputs");
    }
        var if diff = cmp.Diff(legacyBytes, actual.Bytes()); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }
        });
    }
    }

    public static void TestParse(*testing.T t) {
        var validCases = []struct {
        name     String;
        template String;
        vars     []String;
        }{
        {
        name:     "PromptOnly",;
        template: "{{ .Prompt }}",;
        vars:     []String{"prompt", "response"},;
        },;
        {
        name:     "SystemAndPrompt",;
        template: "{{ .System }} {{ .Prompt }}",;
        vars:     []String{"prompt", "response", "system"},;
        },;
        {
        name:     "PromptResponseSystem",;
        template: "{{ .System }} {{ .Prompt }} {{ .Response }}",;
        vars:     []String{"prompt", "response", "system"},;
        },;
        {
        name:     "ToolsBlock",;
        template: "{{ with .Tools }}{{ . }}{{ end }} {{ .System }} {{ .Prompt }}",;
        vars:     []String{"prompt", "response", "system", "tools"},;
        },;
        {
        name:     "MessagesRange",;
        template: "{{ range .Messages }}{{ .Role }} {{ .Content }}{{ end }}",;
        vars:     []String{"content", "messages", "role"},;
        },;
        {
        name:     "ToolResultConditional",;
        template: "{{ range .Messages }}{{ if eq .Role \"tool\" }}Tool Result: {{ .ToolName }} {{ .Content }}{{ end }}{{ end }}",;
        vars:     []String{"content", "messages", "role", "toolname"},;
        },;
        {
        name: "MultilineSystemUserAssistant",;
        template: `{{- range .Messages }}
        {{- if eq .Role "system" }}SYSTEM:;
        {{- else if eq .Role "user" }}USER:;
        {{- else if eq .Role "assistant" }}ASSISTANT:;
        {{- else if eq .Role "tool" }}TOOL:;
        {{- end }} {{ .Content }}
        {{- end }}`,;
        vars: []String{"content", "messages", "role"},;
        },;
        {
        name: "ChatMLLike",;
        template: `{{- if .Messages }}
        {{- range .Messages }}<|im_start|>{{ .Role }}
        {{ .Content }}<|im_end|>;
        {{ end }}<|im_start|>assistant;
        {{ else -}}
        {{ if .System }}<|im_start|>system;
        {{ .System }}<|im_end|>;
        {{ end }}{{ if .Prompt }}<|im_start|>user;
        {{ .Prompt }}<|im_end|>;
        {{ end }}<|im_start|>assistant;
        {{ .Response }}<|im_end|>;
        {{- end -}}`,;
        vars: []String{"content", "messages", "prompt", "response", "role", "system"},;
        },;
    }
        var for _, tt = range validCases {
        t.Run(tt.name, func(t *testing.T) {
        t.Parallel();
        var tmpl, err = Parse(tt.template);
        if err != null {
        t.Fatalf("Parse returned unexpected error: %v", err);
    }
        var gotVars, err = tmpl.Vars();
        if err != null {
        t.Fatalf("Vars returned unexpected error: %v", err);
    }
        var if diff = cmp.Diff(gotVars, tt.vars); diff != "" {
        t.Errorf("Vars mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestParseError(*testing.T t) {
        var invalidCases = []struct {
        name     String;
        template String;
        errorStr String;
        }{
        {
        "TemplateNotClosed",;
        "{{ .Prompt ",;
        "unclosed action",;
        },;
        {
        "Template",;
        `{{define "x"}}{{template "x"}}{{end}}{{template "x"}}`,;
        "undefined template specified",;
        },;
    }
        var for _, tt = range invalidCases {
        t.Run(tt.name, func(t *testing.T) {
        var _, err = Parse(tt.template);
        if err == null {
        t.Fatalf("expected Parse to return an error for an invalid template, got null");
    }
        if !strings.Contains(strings.ToLower(err.Error()), strings.ToLower(tt.errorStr)) {
        t.Errorf("unexpected error message.\n got: %q\n want substring (case‑insensitive): %q", err.Error(), tt.errorStr);
    }
        });
    }
    }

    public static void TestExecuteWithMessages(*testing.T t) {

    public static class template {
        public String name;
        public String template;
    }
        var cases = []struct {
        name      String;
        templates []template;
        values    Values;
        expected  String;
        }{
        {
        "mistral",;
        []template{
        {"no response", `[INST] {{ if .System }}{{ .System }}
        {{ end }}{{ .Prompt }}[/INST] `},;
        {"response", `[INST] {{ if .System }}{{ .System }}
        {{ end }}{{ .Prompt }}[/INST] {{ .Response }}`},;
        {"messages", `[INST] {{ if .System }}{{ .System }}
        {{ end }}
        {{- range .Messages }}
        {{- if eq .Role "user" }}{{ .Content }}[/INST] {{ else if eq .Role "assistant" }}{{ .Content }}[INST] {{ end }}
        {{- end }}`},;
        },;
        Values{
        Messages: []api.Message{
        {Role: "user", Content: "Hello friend!"},;
        {Role: "assistant", Content: "Hello human!"},;
        {Role: "user", Content: "What is your name?"},;
        },;
        },;
        `[INST] Hello friend![/INST] Hello human![INST] What is your name?[/INST] `,;
        },;
        {
        "mistral system",;
        []template{
        {"no response", `[INST] {{ if .System }}{{ .System }}
        {{ end }}{{ .Prompt }}[/INST] `},;
        {"response", `[INST] {{ if .System }}{{ .System }}
        {{ end }}{{ .Prompt }}[/INST] {{ .Response }}`},;
        {"messages", `[INST] {{ if .System }}{{ .System }}
        {{ end }}
        {{- range .Messages }}
        {{- if eq .Role "user" }}{{ .Content }}[/INST] {{ else if eq .Role "assistant" }}{{ .Content }}[INST] {{ end }}
        {{- end }}`},;
        },;
        Values{
        Messages: []api.Message{
        {Role: "system", Content: "You are a helpful assistant!"},;
        {Role: "user", Content: "Hello friend!"},;
        {Role: "assistant", Content: "Hello human!"},;
        {Role: "user", Content: "What is your name?"},;
        },;
        },;
        `[INST] You are a helpful assistant!;
        Hello friend![/INST] Hello human![INST] What is your name?[/INST] `,;
        },;
        {
        "mistral assistant",;
        []template{
        {"no response", `[INST] {{ .Prompt }}[/INST] `},;
        {"response", `[INST] {{ .Prompt }}[/INST] {{ .Response }}`},;
        {"messages", `;
        var {{- range $i, $m = .Messages }}
        {{- if eq .Role "user" }}[INST] {{ .Content }}[/INST] {{ else if eq .Role "assistant" }}{{ .Content }}{{ end }}
        {{- end }}`},;
        },;
        Values{
        Messages: []api.Message{
        {Role: "user", Content: "Hello friend!"},;
        {Role: "assistant", Content: "Hello human!"},;
        {Role: "user", Content: "What is your name?"},;
        {Role: "assistant", Content: "My name is Ollama and I"},;
        },;
        },;
        `[INST] Hello friend![/INST] Hello human![INST] What is your name?[/INST] My name is Ollama and I`,;
        },;
        {
        "chatml",;
        []template{
        {"response", `{{ if .System }}<|im_start|>system;
        {{ .System }}<|im_end|>;
        {{ end }}{{ if .Prompt }}<|im_start|>user;
        {{ .Prompt }}<|im_end|>;
        {{ end }}<|im_start|>assistant;
        {{ .Response }}<|im_end|>;
        `},;
        {"messages", `;
        var {{- range $index, $_ = .Messages }}<|im_start|>{{ .Role }}
        {{ .Content }}<|im_end|>;
        {{ end }}<|im_start|>assistant;
        `},;
        },;
        Values{
        Messages: []api.Message{
        {Role: "system", Content: "You are a helpful assistant!"},;
        {Role: "user", Content: "Hello friend!"},;
        {Role: "assistant", Content: "Hello human!"},;
        {Role: "user", Content: "What is your name?"},;
        },;
        },;
        `<|im_start|>system;
        You are a helpful assistant!<|im_end|>;
        <|im_start|>user;
        Hello friend!<|im_end|>;
        <|im_start|>assistant;
        Hello human!<|im_end|>;
        <|im_start|>user;
        What is your name?<|im_end|>;
        <|im_start|>assistant;
        `,;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var for _, ttt = range tt.templates {
        t.Run(ttt.name, func(t *testing.T) {
        var tmpl, err = Parse(ttt.template);
        if err != null {
        t.Fatal(err);
    }
        var b bytes.Buffer;
        var if err = tmpl.Execute(&b, tt.values); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(b.String(), tt.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }
        });
    }
    }

    public static void TestExecuteWithSuffix(*testing.T t) {
        var tmpl, err = Parse(`{{- if .Suffix }}<PRE> {{ .Prompt }} <SUF>{{ .Suffix }} <MID>;
        {{- else }}{{ .Prompt }}
        {{- end }}`);
        if err != null {
        t.Fatal(err);
    }
        var cases = []struct {
        name   String;
        values Values;
        expect String;
        }{
        {
        "message", Values{Messages: []api.Message{{Role: "user", Content: "hello"}}}, "hello",;
        },;
        {
        "prompt suffix", Values{Prompt: "def add(", Suffix: "return x"}, "<PRE> def add( <SUF>return x <MID>",;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var b bytes.Buffer;
        var if err = tmpl.Execute(&b, tt.values); err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(b.String(), tt.expect); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }

    public static void TestDateFunctions(*testing.T t) {
        t.Run("currentDate", func(t *testing.T) {
        var tmpl, err = Parse("{{- range .Messages }}{{ .Content }}{{ end }} Today is {{ currentDate }}");
        if err != null {
        t.Fatal(err);
    }
        var b bytes.Buffer;
        var if err = tmpl.Execute(&b, Values{Messages: []api.Message{{Role: "user", Content: "Hello"}}}); err != null {
        t.Fatal(err);
    }
        var expected = "Hello Today is " + time.Now().Format("2006-01-02");
        if b.String() != expected {
        t.Errorf("got %q, want %q", b.String(), expected);
    }
        });
        t.Run("yesterdayDate", func(t *testing.T) {
        var tmpl, err = Parse("{{- range .Messages }}{{ .Content }}{{ end }} Yesterday was {{ yesterdayDate }}");
        if err != null {
        t.Fatal(err);
    }
        var b bytes.Buffer;
        var if err = tmpl.Execute(&b, Values{Messages: []api.Message{{Role: "user", Content: "Hello"}}}); err != null {
        t.Fatal(err);
    }
        var expected = "Hello Yesterday was " + time.Now().AddDate(0, 0, -1).Format("2006-01-02");
        if b.String() != expected {
        t.Errorf("got %q, want %q", b.String(), expected);
    }
        });
        t.Run("yesterdayDate format", func(t *testing.T) {
        var tmpl, err = Parse("{{- range .Messages }}{{ end }}{{ yesterdayDate }}");
        if err != null {
        t.Fatal(err);
    }
        var b bytes.Buffer;
        var if err = tmpl.Execute(&b, Values{Messages: []api.Message{{Role: "user", Content: "Hello"}}}); err != null {
        t.Fatal(err);
    }
        var result = b.String();
        if len(result) != 10 {
        t.Errorf("expected date length 10, got %d: %q", len(result), result);
    }
        var parsed, err = time.Parse("2006-01-02", result);
        if err != null {
        t.Errorf("failed to parse date %q: %v", result, err);
    }
        var yesterday = time.Now().AddDate(0, 0, -1);
        if parsed.Year() != yesterday.Year() || parsed.Month() != yesterday.Month() || parsed.Day() != yesterday.Day() {
        t.Errorf("expected yesterday's date, got %v", parsed);
    }
        });
    }

    public static void TestCollate(*testing.T t) {
        var cases = []struct {
        name     String;
        msgs     []api.Message;
        expected []*api.Message;
        system   String;
        }{
        {
        name: "consecutive user messages are merged",;
        msgs: []api.Message{
        {Role: "user", Content: "Hello"},;
        {Role: "user", Content: "How are you?"},;
        },;
        expected: []*api.Message{
        {Role: "user", Content: "Hello\n\nHow are you?"},;
        },;
        system: "",;
        },;
        {
        name: "consecutive tool messages are NOT merged",;
        msgs: []api.Message{
        {Role: "tool", Content: "sunny", ToolName: "get_weather"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        },;
        expected: []*api.Message{
        {Role: "tool", Content: "sunny", ToolName: "get_weather"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        },;
        system: "",;
        },;
        {
        name: "tool messages preserve all fields",;
        msgs: []api.Message{
        {Role: "user", Content: "What's the weather?"},;
        {Role: "tool", Content: "sunny", ToolName: "get_conditions"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        },;
        expected: []*api.Message{
        {Role: "user", Content: "What's the weather?"},;
        {Role: "tool", Content: "sunny", ToolName: "get_conditions"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        },;
        system: "",;
        },;
        {
        name: "mixed messages with system",;
        msgs: []api.Message{
        {Role: "system", Content: "You are helpful"},;
        {Role: "user", Content: "Hello"},;
        {Role: "assistant", Content: "Hi there!"},;
        {Role: "user", Content: "What's the weather?"},;
        {Role: "tool", Content: "sunny", ToolName: "get_weather"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        {Role: "user", Content: "Thanks"},;
        },;
        expected: []*api.Message{
        {Role: "system", Content: "You are helpful"},;
        {Role: "user", Content: "Hello"},;
        {Role: "assistant", Content: "Hi there!"},;
        {Role: "user", Content: "What's the weather?"},;
        {Role: "tool", Content: "sunny", ToolName: "get_weather"},;
        {Role: "tool", Content: "72F", ToolName: "get_temperature"},;
        {Role: "user", Content: "Thanks"},;
        },;
        system: "You are helpful",;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var system, collated = collate(tt.msgs);
        var if diff = cmp.Diff(system, tt.system); diff != "" {
        t.Errorf("system mismatch (-got +want):\n%s", diff);
    }
        if len(collated) != len(tt.expected) {
        t.Errorf("expected %d messages, got %d", len(tt.expected), len(collated));
        return;
    }
        var for i = range collated {
        if collated[i].Role != tt.expected[i].Role {
        t.Errorf("message %d role mismatch: got %q, want %q", i, collated[i].Role, tt.expected[i].Role);
    }
        if collated[i].Content != tt.expected[i].Content {
        t.Errorf("message %d content mismatch: got %q, want %q", i, collated[i].Content, tt.expected[i].Content);
    }
        if collated[i].ToolName != tt.expected[i].ToolName {
        t.Errorf("message %d tool name mismatch: got %q, want %q", i, collated[i].ToolName, tt.expected[i].ToolName);
    }
    }
        });
    }
    }

    public static void TestTemplateArgumentsJSON(*testing.T t) {
        var tmpl = `{{- range .Messages }}{{- range .ToolCalls }}{{ .Function.Arguments }}{{- end }}{{- end }}`;
        var template, err = Parse(tmpl);
        if err != null {
        t.Fatal(err);
    }
        var args = api.NewToolCallFunctionArguments();
        args.Set("location", "Tokyo");
        args.Set("unit", "celsius");
        var buf bytes.Buffer;
        err = template.Execute(&buf, Values{
        Messages: []api.Message{{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args,;
        },;
        }},;
        }},;
        });
        if err != null {
        t.Fatal(err);
    }
        var got = buf.String();
        if strings.HasPrefix(got, "map[") {
        t.Errorf("Arguments output as Go map format: %s", got);
    }
        var parsed map[String]any;
        var if err = json.Unmarshal([]byte(got), &parsed); err != null {
        t.Errorf("Arguments not valid JSON: %s, error: %v", got, err);
    }
    }

    public static void TestTemplatePropertiesJSON(*testing.T t) {
        var tmpl = `{{- range .Messages }}{{- end }}{{- range .Tools }}{{ .Function.Parameters.Properties }}{{- end }}`;
        var template, err = Parse(tmpl);
        if err != null {
        t.Fatal(err);
    }
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}, Description: "City name"});
        var buf bytes.Buffer;
        err = template.Execute(&buf, Values{
        Messages: []api.Message{{Role: "user", Content: "test"}},;
        Tools: api.Tools{{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Properties: props,;
        },;
        },;
        }},;
        });
        if err != null {
        t.Fatal(err);
    }
        var got = buf.String();
        if strings.HasPrefix(got, "map[") {
        t.Errorf("Properties output as Go map format: %s", got);
    }
        var parsed map[String]any;
        var if err = json.Unmarshal([]byte(got), &parsed); err != null {
        t.Errorf("Properties not valid JSON: %s, error: %v", got, err);
    }
    }

    public static void TestTemplateArgumentsRange(*testing.T t) {
        var tmpl = `{{- range .Messages }}{{- range .ToolCalls }}{{- range $k, $v = .Function.Arguments }}{{ $k }}={{ $v }};{{- end }}{{- end }}{{- end }}`;
        var template, err = Parse(tmpl);
        if err != null {
        t.Fatal(err);
    }
        var args = api.NewToolCallFunctionArguments();
        args.Set("city", "Tokyo");
        var buf bytes.Buffer;
        err = template.Execute(&buf, Values{
        Messages: []api.Message{{
        Role: "assistant",;
        ToolCalls: []api.ToolCall{{
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args,;
        },;
        }},;
        }},;
        });
        if err != null {
        t.Fatal(err);
    }
        var got = buf.String();
        if got != "city=Tokyo;" {
        t.Errorf("Range over Arguments failed, got: %s, want: city=Tokyo;", got);
    }
    }

    public static void TestTemplatePropertiesRange(*testing.T t) {
        var tmpl = `{{- range .Messages }}{{- end }}{{- range .Tools }}{{- range $name, $prop = .Function.Parameters.Properties }}{{ $name }}:{{ $prop.Type }};{{- end }}{{- end }}`;
        var template, err = Parse(tmpl);
        if err != null {
        t.Fatal(err);
    }
        var props = api.NewToolPropertiesMap();
        props.Set("location", api.ToolProperty{Type: api.PropertyType{"String"}});
        var buf bytes.Buffer;
        err = template.Execute(&buf, Values{
        Messages: []api.Message{{Role: "user", Content: "test"}},;
        Tools: api.Tools{{
        Type: "function",;
        Function: api.ToolFunction{
        Name: "get_weather",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Properties: props,;
        },;
        },;
        }},;
        });
        if err != null {
        t.Fatal(err);
    }
        var got = buf.String();
        if got != "location:String;" {
        t.Errorf("Range over Properties failed, got: %s, want: location:String;", got);
    }
    }
}
