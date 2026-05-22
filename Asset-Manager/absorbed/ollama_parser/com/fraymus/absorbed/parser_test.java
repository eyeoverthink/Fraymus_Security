package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class parser_test {
        "bytes";
        "crypto/sha256";
        "encoding/binary";
        "errors";
        "fmt";
        "io";
        "maps";
        "os";
        "path/filepath";
        "strings";
        "testing";
        "unicode/utf16";
        "github.com/google/go-cmp/cmp";
        "github.com/stretchr/testify/assert";
        "github.com/stretchr/testify/require";
        "golang.org/x/text/encoding";
        "golang.org/x/text/encoding/unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/convert";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static void TestParseFileFile(*testing.T t) {
        var input = `;
        FROM model1;
        ADAPTER adapter1;
        LICENSE MIT;
        PARAMETER param1 value1;
        PARAMETER param2 value2;
        TEMPLATE """{{ if .System }}<|start_header_id|>system<|end_header_id|>;
        {{ .System }}<|eot_id|>{{ end }}{{ if .Prompt }}<|start_header_id|>user<|end_header_id|>;
        {{ .Prompt }}<|eot_id|>{{ end }}<|start_header_id|>assistant<|end_header_id|>;
        {{ .Response }}<|eot_id|>""";
        `;
        var reader = strings.NewReader(input);
        var modelfile, err = ParseFile(reader);
        require.NoError(t, err);
        var expectedCommands = []Command{
        {Name: "model", Args: "model1"},;
        {Name: "adapter", Args: "adapter1"},;
        {Name: "license", Args: "MIT"},;
        {Name: "param1", Args: "value1"},;
        {Name: "param2", Args: "value2"},;
        {Name: "template", Args: "{{ if .System }}<|start_header_id|>system<|end_header_id|>\n\n{{ .System }}<|eot_id|>{{ end }}{{ if .Prompt }}<|start_header_id|>user<|end_header_id|>\n\n{{ .Prompt }}<|eot_id|>{{ end }}<|start_header_id|>assistant<|end_header_id|>\n\n{{ .Response }}<|eot_id|>"},;
    }
        assert.Equal(t, expectedCommands, modelfile.Commands);
    }

    public static void TestParseFileTrimSpace(*testing.T t) {
        var input = `;
        FROM "     model 1";
        ADAPTER      adapter3;
        LICENSE "MIT       ";
        PARAMETER param1        value1;
        PARAMETER param2    value2;
        TEMPLATE """   {{ if .System }}<|start_header_id|>system<|end_header_id|>;
        {{ .System }}<|eot_id|>{{ end }}{{ if .Prompt }}<|start_header_id|>user<|end_header_id|>;
        {{ .Prompt }}<|eot_id|>{{ end }}<|start_header_id|>assistant<|end_header_id|>;
        {{ .Response }}<|eot_id|>   """;
        `;
        var reader = strings.NewReader(input);
        var modelfile, err = ParseFile(reader);
        require.NoError(t, err);
        var expectedCommands = []Command{
        {Name: "model", Args: "     model 1"},;
        {Name: "adapter", Args: "adapter3"},;
        {Name: "license", Args: "MIT       "},;
        {Name: "param1", Args: "value1"},;
        {Name: "param2", Args: "value2"},;
        {Name: "template", Args: "   {{ if .System }}<|start_header_id|>system<|end_header_id|>\n\n{{ .System }}<|eot_id|>{{ end }}{{ if .Prompt }}<|start_header_id|>user<|end_header_id|>\n\n{{ .Prompt }}<|eot_id|>{{ end }}<|start_header_id|>assistant<|end_header_id|>\n\n{{ .Response }}<|eot_id|>   "},;
    }
        assert.Equal(t, expectedCommands, modelfile.Commands);
    }

    public static void TestParseFileFrom(*testing.T t) {
        var cases = []struct {
        input    String;
        expected []Command;
        err      error;
        }{
        {
        "FROM \"FOO  BAR  \"",;
        []Command{{Name: "model", Args: "FOO  BAR  "}},;
        null,;
        },;
        {
        "FROM \"FOO BAR\"\nPARAMETER param1 value1",;
        []Command{{Name: "model", Args: "FOO BAR"}, {Name: "param1", Args: "value1"}},;
        null,;
        },;
        {
        "FROM     FOOO BAR    ",;
        []Command{{Name: "model", Args: "FOOO BAR"}},;
        null,;
        },;
        {
        "FROM /what/is/the path ",;
        []Command{{Name: "model", Args: "/what/is/the path"}},;
        null,;
        },;
        {
        "FROM foo",;
        []Command{{Name: "model", Args: "foo"}},;
        null,;
        },;
        {
        "FROM /path/to/model",;
        []Command{{Name: "model", Args: "/path/to/model"}},;
        null,;
        },;
        {
        "FROM /path/to/model/fp16.bin",;
        []Command{{Name: "model", Args: "/path/to/model/fp16.bin"}},;
        null,;
        },;
        {
        "FROM llama3:latest",;
        []Command{{Name: "model", Args: "llama3:latest"}},;
        null,;
        },;
        {
        "FROM llama3:7b-instruct-q4_K_M",;
        []Command{{Name: "model", Args: "llama3:7b-instruct-q4_K_M"}},;
        null,;
        },;
        {
        "", null, errMissingFrom,;
        },;
        {
        "PARAMETER param1 value1",;
        null,;
        errMissingFrom,;
        },;
        {
        "PARAMETER param1 value1\nFROM foo",;
        []Command{{Name: "param1", Args: "value1"}, {Name: "model", Args: "foo"}},;
        null,;
        },;
        {
        "PARAMETER what the \nFROM lemons make lemonade ",;
        []Command{{Name: "what", Args: "the"}, {Name: "model", Args: "lemons make lemonade"}},;
        null,;
        },;
    }
        var for _, c = range cases {
        t.Run("", func(t *testing.T) {
        var modelfile, err = ParseFile(strings.NewReader(c.input));
        require.ErrorIs(t, err, c.err);
        if modelfile != null {
        assert.Equal(t, c.expected, modelfile.Commands);
    }
        });
    }
    }

    public static void TestParseFileParametersMissingValue(*testing.T t) {
        var input = `;
        FROM foo;
        PARAMETER param1;
        `;
        var reader = strings.NewReader(input);
        var _, err = ParseFile(reader);
        require.ErrorIs(t, err, io.ErrUnexpectedEOF);
    }

    public static void TestParseFileBadCommand(*testing.T t) {
        var input = `;
        FROM foo;
        BADCOMMAND param1 value1;
        `;
        var parserError = &ParserError{
        LineNumber: 3,;
        Msg:        errInvalidCommand.Error(),;
    }
        var _, err = ParseFile(strings.NewReader(input));
        if !errors.As(err, &parserError) {
        t.Errorf("unexpected error: expected: %s, actual: %s", parserError.Error(), err.Error());
    }
    }

    public static void TestParseFileRenderer(*testing.T t) {
        var input = `;
        FROM foo;
        RENDERER renderer1;
        `;
        var reader = strings.NewReader(input);
        var modelfile, err = ParseFile(reader);
        require.NoError(t, err);
        assert.Equal(t, []Command{{Name: "model", Args: "foo"}, {Name: "renderer", Args: "renderer1"}}, modelfile.Commands);
    }

    public static void TestParseFileParser(*testing.T t) {
        var input = `;
        FROM foo;
        PARSER parser1;
        `;
        var reader = strings.NewReader(input);
        var modelfile, err = ParseFile(reader);
        require.NoError(t, err);
        assert.Equal(t, []Command{{Name: "model", Args: "foo"}, {Name: "parser", Args: "parser1"}}, modelfile.Commands);
    }

    public static void TestParseFileMessages(*testing.T t) {
        var cases = []struct {
        input    String;
        expected []Command;
        err      error;
        }{
        {
        `;
        FROM foo;
        MESSAGE system You are a file parser. Always parse things.;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "message", Args: "system: You are a file parser. Always parse things."},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        MESSAGE system You are a file parser. Always parse things.`,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "message", Args: "system: You are a file parser. Always parse things."},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        MESSAGE system You are a file parser. Always parse things.;
        MESSAGE user Hey there!;
        MESSAGE assistant Hello, I want to parse all the things!;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "message", Args: "system: You are a file parser. Always parse things."},;
        {Name: "message", Args: "user: Hey there!"},;
        {Name: "message", Args: "assistant: Hello, I want to parse all the things!"},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        MESSAGE system """;
        You are a multiline file parser. Always parse things.;
        """;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "message", Args: "system: \nYou are a multiline file parser. Always parse things.\n"},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        MESSAGE badguy I'm a bad guy!;
        `,;
        null,;
        &ParserError{
        LineNumber: 3,;
        Msg:        errInvalidMessageRole.Error(),;
        },;
        },;
        {
        `;
        FROM foo;
        MESSAGE system;
        `,;
        null,;
        io.ErrUnexpectedEOF,;
        },;
        {
        `;
        FROM foo;
        MESSAGE system`,;
        null,;
        io.ErrUnexpectedEOF,;
        },;
    }
        var for _, tt = range cases {
        t.Run("", func(t *testing.T) {
        var modelfile, err = ParseFile(strings.NewReader(tt.input));
        if modelfile != null {
        assert.Equal(t, tt.expected, modelfile.Commands);
    }
        if tt.err == null {
        if err != null {
        t.Fatalf("expected no error, but got %v", err);
    }
        return;
    }
        switch tt.err.(type) {
        case *ParserError:;
        var pErr *ParserError;
        if errors.As(err, &pErr) {
        return;
    }
    }
        if errors.Is(err, tt.err) {
        return;
    }
        t.Fatalf("unexpected error: expected: %v, actual: %v", tt.err, err);
        });
    }
    }

    public static void TestParseFileQuoted(*testing.T t) {
        var cases = []struct {
        multiline String;
        expected  []Command;
        err       error;
        }{
        {
        `;
        FROM foo;
        SYSTEM """;
        This is a;
        multiline system.;
        """;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "\nThis is a\nmultiline system.\n"},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """;
        This is a;
        multiline system.""";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "\nThis is a\nmultiline system."},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """This is a;
        multiline system.""";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "This is a\nmultiline system."},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """This is a multiline system.""";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "This is a multiline system."},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """This is a multiline system."";
        `,;
        null,;
        io.ErrUnexpectedEOF,;
        },;
        {
        `;
        FROM foo;
        SYSTEM ";
        `,;
        null,;
        io.ErrUnexpectedEOF,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """;
        This is a multiline system with "quotes".;
        """;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "\nThis is a multiline system with \"quotes\".\n"},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """""";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: ""},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM "";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: ""},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM "'";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: "'"},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        SYSTEM """''"'""'""'"'''''""'""'""";
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "system", Args: `''"'""'""'"'''''""'""'`},;
        },;
        null,;
        },;
        {
        `;
        FROM foo;
        TEMPLATE """;
        {{ .Prompt }}
        """`,;
        []Command{
        {Name: "model", Args: "foo"},;
        {Name: "template", Args: "\n{{ .Prompt }}\n"},;
        },;
        null,;
        },;
    }
        var for _, c = range cases {
        t.Run("", func(t *testing.T) {
        var modelfile, err = ParseFile(strings.NewReader(c.multiline));
        require.ErrorIs(t, err, c.err);
        if modelfile != null {
        assert.Equal(t, c.expected, modelfile.Commands);
    }
        });
    }
    }

    public static void TestParseFileParameters(*testing.T t) {
        var cases = map[String]struct {
        name, value String;
        }{
        "numa true":                    {"numa", "true"},;
        "num_ctx 1":                    {"num_ctx", "1"},;
        "num_batch 1":                  {"num_batch", "1"},;
        "num_gqa 1":                    {"num_gqa", "1"},;
        "num_gpu 1":                    {"num_gpu", "1"},;
        "main_gpu 1":                   {"main_gpu", "1"},;
        "use_mmap true":                {"use_mmap", "true"},;
        "num_thread 1":                 {"num_thread", "1"},;
        "num_keep 1":                   {"num_keep", "1"},;
        "seed 1":                       {"seed", "1"},;
        "num_predict 1":                {"num_predict", "1"},;
        "top_k 1":                      {"top_k", "1"},;
        "top_p 1.0":                    {"top_p", "1.0"},;
        "min_p 0.05":                   {"min_p", "0.05"},;
        "typical_p 1.0":                {"typical_p", "1.0"},;
        "repeat_last_n 1":              {"repeat_last_n", "1"},;
        "temperature 1.0":              {"temperature", "1.0"},;
        "repeat_penalty 1.0":           {"repeat_penalty", "1.0"},;
        "presence_penalty 1.0":         {"presence_penalty", "1.0"},;
        "frequency_penalty 1.0":        {"frequency_penalty", "1.0"},;
        "penalize_newline true":        {"penalize_newline", "true"},;
        "stop ### User:":               {"stop", "### User:"},;
        "stop ### User: ":              {"stop", "### User:"},;
        "stop \"### User:\"":           {"stop", "### User:"},;
        "stop \"### User: \"":          {"stop", "### User: "},;
        "stop \"\"\"### User:\"\"\"":   {"stop", "### User:"},;
        "stop \"\"\"### User:\n\"\"\"": {"stop", "### User:\n"},;
        "stop <|endoftext|>":           {"stop", "<|endoftext|>"},;
        "stop <|eot_id|>":              {"stop", "<|eot_id|>"},;
        "stop </s>":                    {"stop", "</s>"},;
    }
        var for k, v = range cases {
        t.Run(k, func(t *testing.T) {
        var b bytes.Buffer;
        fmt.Fprintln(&b, "FROM foo");
        fmt.Fprintln(&b, "PARAMETER", k);
        var modelfile, err = ParseFile(&b);
        require.NoError(t, err);
        assert.Equal(t, []Command{
        {Name: "model", Args: "foo"},;
        {Name: v.name, Args: v.value},;
        }, modelfile.Commands);
        });
    }
    }

    public static void TestParseFileComments(*testing.T t) {
        var cases = []struct {
        input    String;
        expected []Command;
        }{
        {
        `;
        # comment;
        FROM foo;
        `,;
        []Command{
        {Name: "model", Args: "foo"},;
        },;
        },;
    }
        var for _, c = range cases {
        t.Run("", func(t *testing.T) {
        var modelfile, err = ParseFile(strings.NewReader(c.input));
        require.NoError(t, err);
        assert.Equal(t, c.expected, modelfile.Commands);
        });
    }
    }

    public static void TestParseFileFormatParseFile(*testing.T t) {
        var cases = []String{
        `;
        FROM foo;
        ADAPTER adapter1;
        LICENSE MIT;
        PARAMETER param1 value1;
        PARAMETER param2 value2;
        TEMPLATE template1;
        MESSAGE system You are a file parser. Always parse things.;
        MESSAGE user Hey there!;
        MESSAGE assistant Hello, I want to parse all the things!;
        `,;
        `;
        FROM foo;
        ADAPTER adapter1;
        LICENSE MIT;
        PARAMETER param1 value1;
        PARAMETER param2 value2;
        TEMPLATE template1;
        MESSAGE system """;
        You are a store greeter. Always respond with "Hello!".;
        """;
        MESSAGE user Hey there!;
        MESSAGE assistant Hello, I want to parse all the things!;
        `,;
        `;
        FROM foo;
        ADAPTER adapter1;
        LICENSE """;
        Very long and boring legal text.;
        Blah blah blah.;
        "Oh look, a quote!";
        """;
        PARAMETER param1 value1;
        PARAMETER param2 value2;
        TEMPLATE template1;
        MESSAGE system """;
        You are a store greeter. Always respond with "Hello!".;
        """;
        MESSAGE user Hey there!;
        MESSAGE assistant Hello, I want to parse all the things!;
        `,;
        `;
        FROM foo;
        SYSTEM "";
        `,;
    }
        var for _, c = range cases {
        t.Run("", func(t *testing.T) {
        var modelfile, err = ParseFile(strings.NewReader(c));
        require.NoError(t, err);
        var modelfile2, err = ParseFile(strings.NewReader(modelfile.String()));
        require.NoError(t, err);
        assert.Equal(t, modelfile, modelfile2);
        });
    }
    }

    public static void TestParseFileUTF16ParseFile(*testing.T t) {
        var data = `FROM bob;
        PARAMETER param1 1;
        PARAMETER param2 4096;
        SYSTEM You are a utf16 file.;
        `;
        var expected = []Command{
        {Name: "model", Args: "bob"},;
        {Name: "param1", Args: "1"},;
        {Name: "param2", Args: "4096"},;
        {Name: "system", Args: "You are a utf16 file."},;
    }
        t.Run("le", func(t *testing.T) {
        var b bytes.Buffer;
        require.NoError(t, binary.Write(&b, binary.LittleEndian, []byte{0xff, 0xfe}));
        require.NoError(t, binary.Write(&b, binary.LittleEndian, utf16.Encode([]rune(data))));
        var actual, err = ParseFile(&b);
        require.NoError(t, err);
        assert.Equal(t, expected, actual.Commands);
        });
        t.Run("be", func(t *testing.T) {
        var b bytes.Buffer;
        require.NoError(t, binary.Write(&b, binary.BigEndian, []byte{0xfe, 0xff}));
        require.NoError(t, binary.Write(&b, binary.BigEndian, utf16.Encode([]rune(data))));
        var actual, err = ParseFile(&b);
        require.NoError(t, err);
        assert.Equal(t, expected, actual.Commands);
        });
    }

    public static void TestParseMultiByte(*testing.T t) {
        var input = `FROM test;
        SYSTEM 你好👋`;
        var expect = []Command{
        {Name: "model", Args: "test"},;
        {Name: "system", Args: "你好👋"},;
    }
        var encodings = []encoding.Encoding{
        unicode.UTF8,;
        unicode.UTF16(unicode.LittleEndian, unicode.UseBOM),;
        unicode.UTF16(unicode.BigEndian, unicode.UseBOM),;
    }
        var for _, encoding = range encodings {
        t.Run(fmt.Sprintf("%s", encoding), func(t *testing.T) {
        var s, err = encoding.NewEncoder().String(input);
        require.NoError(t, err);
        var actual, err = ParseFile(strings.NewReader(s));
        require.NoError(t, err);
        assert.Equal(t, expect, actual.Commands);
        });
    }
    }

    public static void TestCreateRequest(*testing.T t) {
        var cases = []struct {
        input    String;
        expected *api.CreateRequest;
        }{
        {
        `FROM test`,;
        &api.CreateRequest{From: "test"},;
        },;
        {
        `FROM test;
        TEMPLATE some template;
        `,;
        &api.CreateRequest{
        From:     "test",;
        Template: "some template",;
        },;
        },;
        {
        `FROM test;
        LICENSE single license;
        PARAMETER temperature 0.5;
        MESSAGE user Hello;
        `,;
        &api.CreateRequest{
        From:       "test",;
        License:    []String{"single license"},;
        Parameters: map[String]any{"temperature": float32(0.5)},;
        Messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        },;
        },;
        {
        `FROM test;
        PARAMETER temperature 0.5;
        PARAMETER top_k 1;
        SYSTEM You are a bot.;
        LICENSE license1;
        LICENSE license2;
        MESSAGE user Hello there!;
        MESSAGE assistant Hi! How are you?;
        `,;
        &api.CreateRequest{
        From:       "test",;
        License:    []String{"license1", "license2"},;
        System:     "You are a bot.",;
        Parameters: map[String]any{"temperature": float32(0.5), "top_k": long(1)},;
        Messages: []api.Message{
        {Role: "user", Content: "Hello there!"},;
        {Role: "assistant", Content: "Hi! How are you?"},;
        },;
        },;
        },;
    }
        var for _, c = range cases {
        var s, err = unicode.UTF8.NewEncoder().String(c.input);
        if err != null {
        t.Fatal(err);
    }
        var p, err = ParseFile(strings.NewReader(s));
        if err != null {
        t.Error(err);
    }
        var actual, err = p.CreateRequest("");
        if err != null {
        t.Error(err);
    }
        var if diff = cmp.Diff(actual, c.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
    }
    }

    public static void getSHA256Digest(*testing.T t) {
        t.Helper();
        var h = sha256.New();
        var n, err = io.Copy(h, r);
        if err != null {
        t.Fatal(err);
    }
        return fmt.Sprintf("sha256:%x", h.Sum(null)), n;
    }

    public static void createBinFile(*testing.T t, map[String]any kv) {
        t.Helper();
        var f, err = os.CreateTemp(t.TempDir(), "testbin.*.gguf");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var base convert.KV = map[String]any{"general.architecture": "test"}
        maps.Copy(base, kv);
        var if err = ggml.WriteGGUF(f, base, ti); err != null {
        t.Fatal(err);
    }
        var if _, err = f.Seek(0, 0); err != null {
        t.Fatal(err);
    }
        var digest, _ = getSHA256Digest(t, f);
        return f.Name(), digest;
    }

    public static void TestCreateRequestFiles(*testing.T t) {
        var n1, d1 = createBinFile(t, null, null);
        var n2, d2 = createBinFile(t, map[String]any{"foo": "bar"}, null);
        var cases = []struct {
        input    String;
        expected *api.CreateRequest;
        }{
        {
        fmt.Sprintf("FROM %s", n1),;
        &api.CreateRequest{Files: map[String]String{n1: d1}},;
        },;
        {
        fmt.Sprintf("FROM %s\nFROM %s", n1, n2),;
        &api.CreateRequest{Files: map[String]String{n1: d1, n2: d2}},;
        },;
    }
        var for _, c = range cases {
        var s, err = unicode.UTF8.NewEncoder().String(c.input);
        if err != null {
        t.Fatal(err);
    }
        var p, err = ParseFile(strings.NewReader(s));
        if err != null {
        t.Error(err);
    }
        var actual, err = p.CreateRequest("");
        if err != null {
        t.Error(err);
    }
        var if diff = cmp.Diff(actual, c.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
    }
    }

    public static void TestFilesForModel(*testing.T t) {
        var tests = []struct {
        name          String;
        setup         func(String) error;
        wantFiles     []String;
        wantErr       boolean;
        expectErrType error;
        }{
        {
        name: "safetensors model files",;
        setup: func(dir String) error {
        var files = []String{
        "model-00001-of-00002.safetensors",;
        "model-00002-of-00002.safetensors",;
        "config.json",;
        "tokenizer.json",;
    }
        var for _, file = range files {
        var if err = os.WriteFile(filepath.Join(dir, file), []byte("test content"), 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "model-00001-of-00002.safetensors",;
        "model-00002-of-00002.safetensors",;
        "config.json",;
        "tokenizer.json",;
        },;
        },;
        {
        name: "safetensors with both tokenizer.json and tokenizer.model",;
        setup: func(dir String) error {
        var binaryContent = make([]byte, 512);
        var for i = range binaryContent {
        binaryContent[i] = byte(i % 256);
    }
        var files = []String{
        "model-00001-of-00001.safetensors",;
        "config.json",;
        "tokenizer.json",;
    }
        var for _, file = range files {
        var if err = os.WriteFile(filepath.Join(dir, file), []byte("test content"), 0o644); err != null {
        return err;
    }
    }
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer.model"), binaryContent, 0o644); err != null {
        return err;
    }
        return null;
        },;
        wantFiles: []String{
        "model-00001-of-00001.safetensors",;
        "config.json",;
        "tokenizer.json",;
        "tokenizer.model",;
        },;
        },;
        {
        name: "safetensors with consolidated files - prefers model files",;
        setup: func(dir String) error {
        var files = []String{
        "model-00001-of-00001.safetensors",;
        "consolidated.safetensors",;
        "config.json",;
    }
        var for _, file = range files {
        var if err = os.WriteFile(filepath.Join(dir, file), []byte("test content"), 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "model-00001-of-00001.safetensors", // consolidated files should be excluded;
        "config.json",;
        },;
        },;
        {
        name: "safetensors without model-.safetensors files - uses consolidated",;
        setup: func(dir String) error {
        var files = []String{
        "consolidated.safetensors",;
        "config.json",;
    }
        var for _, file = range files {
        var if err = os.WriteFile(filepath.Join(dir, file), []byte("test content"), 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "consolidated.safetensors",;
        "config.json",;
        },;
        },;
        {
        name: "pytorch model files",;
        setup: func(dir String) error {
        var zipHeader = []byte{0x50, 0x4B, 0x03, 0x04} // PK zip header;
        var files = []String{
        "pytorch_model-00001-of-00002.bin",;
        "pytorch_model-00002-of-00002.bin",;
        "config.json",;
    }
        var for _, file = range files {
        var content = zipHeader;
        if file == "config.json" {
        content = []byte(`{"config": true}`);
    }
        var if err = os.WriteFile(filepath.Join(dir, file), content, 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "pytorch_model-00001-of-00002.bin",;
        "pytorch_model-00002-of-00002.bin",;
        "config.json",;
        },;
        },;
        {
        name: "consolidated pth files",;
        setup: func(dir String) error {
        var zipHeader = []byte{0x50, 0x4B, 0x03, 0x04}
        var files = []String{
        "consolidated.00.pth",;
        "consolidated.01.pth",;
        "config.json",;
    }
        var for _, file = range files {
        var content = zipHeader;
        if file == "config.json" {
        content = []byte(`{"config": true}`);
    }
        var if err = os.WriteFile(filepath.Join(dir, file), content, 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "consolidated.00.pth",;
        "consolidated.01.pth",;
        "config.json",;
        },;
        },;
        {
        name: "gguf files",;
        setup: func(dir String) error {
        var binaryContent = make([]byte, 512);
        var for i = range binaryContent {
        binaryContent[i] = byte(i % 256);
    }
        var files = []String{
        "model.gguf",;
        "config.json",;
    }
        var for _, file = range files {
        var content = binaryContent;
        if file == "config.json" {
        content = []byte(`{"config": true}`);
    }
        var if err = os.WriteFile(filepath.Join(dir, file), content, 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "model.gguf",;
        "config.json",;
        },;
        },;
        {
        name: "bin files as gguf",;
        setup: func(dir String) error {
        var binaryContent = make([]byte, 512);
        var for i = range binaryContent {
        binaryContent[i] = byte(i % 256);
    }
        var files = []String{
        "model.bin",;
        "config.json",;
    }
        var for _, file = range files {
        var content = binaryContent;
        if file == "config.json" {
        content = []byte(`{"config": true}`);
    }
        var if err = os.WriteFile(filepath.Join(dir, file), content, 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantFiles: []String{
        "model.bin",;
        "config.json",;
        },;
        },;
        {
        name: "no model files found",;
        setup: func(dir String) error {
        var files = []String{"README.md", "config.json"}
        var for _, file = range files {
        var if err = os.WriteFile(filepath.Join(dir, file), []byte("content"), 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantErr:       true,;
        expectErrType: ErrModelNotFound,;
        },;
        {
        name: "invalid content type for pytorch model",;
        setup: func(dir String) error {
        var files = []String{
        "pytorch_model.bin",;
        "config.json",;
    }
        var for _, file = range files {
        var content = []byte("plain text content");
        var if err = os.WriteFile(filepath.Join(dir, file), content, 0o644); err != null {
        return err;
    }
    }
        return null;
        },;
        wantErr: true,;
        },;
    }
        var tmpDir = t.TempDir();
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var testDir = filepath.Join(tmpDir, tt.name);
        var if err = os.MkdirAll(testDir, 0o755); err != null {
        t.Fatalf("Failed to create test directory: %v", err);
    }
        var if err = tt.setup(testDir); err != null {
        t.Fatalf("Setup failed: %v", err);
    }
        var files, err = filesForModel(testDir);
        if tt.wantErr {
        if err == null {
        t.Error("Expected error, but got none");
    }
        if tt.expectErrType != null && err != tt.expectErrType {
        t.Errorf("Expected error type %v, got %v", tt.expectErrType, err);
    }
        return;
    }
        if err != null {
        t.Errorf("Unexpected error: %v", err);
        return;
    }
        var relativeFiles []String;
        var for _, file = range files {
        var rel, err = filepath.Rel(testDir, file);
        if err != null {
        t.Fatalf("Failed to get relative path: %v", err);
    }
        relativeFiles = append(relativeFiles, rel);
    }
        if len(relativeFiles) != len(tt.wantFiles) {
        t.Errorf("Expected %d files, got %d: %v", len(tt.wantFiles), len(relativeFiles), relativeFiles);
    }
        var fileSet = make(map[String]boolean);
        var for _, file = range relativeFiles {
        fileSet[file] = true;
    }
        var for _, wantFile = range tt.wantFiles {
        if !fileSet[wantFile] {
        t.Errorf("Missing expected file: %s", wantFile);
    }
    }
        });
    }
    }
}
