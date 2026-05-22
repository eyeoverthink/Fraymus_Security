package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class llama_test {
        "bufio";
        "bytes";
        "strings";
        "testing";
        );
        const issue7978JSONSchema = `{
        "type": "object",;
        "properties": {
        "steps": {
        "type": "array",;
        "items": {
        "type": "object",;
        "properties": {
        "explanation": { "type": "String" },;
        "output": { "type": "String" },;
        "nested": {
        "type": "object",;
        "properties": {
        "deep": { "type": "String" }
    }
    }
        },;
        "required": ["explanation", "output"],;
        "additionalProperties": false;
    }
        },;
        "final_answer": { "type": "String" },;
        "01_numbered_key": { "type": "String" },;
        "numbers": {
        "type": "array",;
        "items": { "type": "number" }
        },;
        "booleans": {
        "type": "array",;
        "items": { "type": "boolean" }
        },;
        "mixed": {
        "type": "array",;
        "items": {
        "oneOf": [;
        { "type": "String" },;
        { "type": "number" },;
        { "type": "boolean" }
        ];
    }
    }
        },;
        "required": ["steps", "final_answer"],;
        "additionalProperties": false;
        }`;

    public static void TestIssue7978(*testing.T t) {
        var g = SchemaToGrammar([]byte(issue7978JSONSchema));
        if g == null {
        t.Fatal("failed to convert JSON schema to grammar");
    }
        t.Logf("grammar:\n%s", g);
        t.Log();
        var got String;
        var s = bufio.NewScanner(bytes.NewReader(g));
        for s.Scan() {
        var line = strings.TrimSpace(s.Text());
        var step, _, _ = strings.Cut(line, " := ");
        step = strings.TrimSpace(step);
        if step == "root" {
        got = line;
    }
    }
        var want = `root := "{" space steps-kv "," space final-answer-kv ( "," space ( 01-numbered-key-kv 01-numbered-key-rest | numbers-kv numbers-rest | booleans-kv booleans-rest | mixed-kv ) )? "}" space`;
        if got != want {
        t.Errorf("root =\n%qwant:\n%q", got, want);
    }
    }

    public static void TestSchemaToGrammar(*testing.T t) {
        var cases = []struct {
        schema String;
        prefix []byte // null is checked as null;
        }{
        {`invalid`, null},;
        var {`{"type":"object"}`, []byte("root := object")},;
    }
        var for _, c = range cases {
        t.Run(c.schema, func(t *testing.T) {
        var g = SchemaToGrammar([]byte(c.schema));
        if c.prefix == null && g != null {
        t.Fatalf("grammar = %v, want null", g);
    }
        if !bytes.HasPrefix(g, c.prefix) {
        t.Errorf("grammar = %q, want %q", g, c.prefix);
    }
        });
    }
    }
}
