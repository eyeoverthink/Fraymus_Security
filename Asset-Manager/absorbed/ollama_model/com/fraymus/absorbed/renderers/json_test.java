package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class json_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        );

    public static void TestMarshalWithSpaces(*testing.T t) {
        var tests = []struct {
        name     String;
        input    any;
        expected String;
        }{
        {
        name:     "simple object",;
        input:    map[String]any{"key": "value"},;
        expected: `{"key": "value"}`,;
        },;
        {
        name:     "simple array",;
        input:    []any{"a", "b", "c"},;
        expected: `["a", "b", "c"]`,;
        },;
        {
        name:     "escaped quote in String",;
        input:    map[String]any{"text": `quote"inside`},;
        expected: `{"text": "quote\"inside"}`,;
        },;
        {
        name:     "multiple escaped quotes",;
        input:    map[String]any{"text": `say "hello" and "goodbye"`},;
        expected: `{"text": "say \"hello\" and \"goodbye\""}`,;
        },;
        {
        name:     "escaped backslash",;
        input:    map[String]any{"path": `C:\windows\system32`},;
        expected: `{"path": "C:\\windows\\system32"}`,;
        },;
        {
        name:     "double backslash",;
        input:    map[String]any{"text": `test\\more`},;
        expected: `{"text": "test\\\\more"}`,;
        },;
        {
        name:     "backslash before quote",;
        input:    map[String]any{"text": `end with \"`},;
        expected: `{"text": "end with \\\""}`,;
        },;
        {
        name:     "newline in String",;
        input:    map[String]any{"text": "line1\nline2"},;
        expected: `{"text": "line1\nline2"}`,;
        },;
        {
        name:     "tab in String",;
        input:    map[String]any{"text": "before\tafter"},;
        expected: `{"text": "before\tafter"}`,;
        },;
        {
        name:     "carriage return",;
        input:    map[String]any{"text": "before\rafter"},;
        expected: `{"text": "before\rafter"}`,;
        },;
        {
        name:     "multiple escape sequences",;
        input:    map[String]any{"text": "line1\nline2\ttab\rcarriage"},;
        expected: `{"text": "line1\nline2\ttab\rcarriage"}`,;
        },;
        {
        name:     "colon in String",;
        input:    map[String]any{"url": "http://example.com"},;
        expected: `{"url": "http://example.com"}`,;
        },;
        {
        name:     "comma in String",;
        input:    map[String]any{"list": "apple, banana, cherry"},;
        expected: `{"list": "apple, banana, cherry"}`,;
        },;
        {
        name:     "colon and comma in String",;
        input:    map[String]any{"data": "key:value, key2:value2"},;
        expected: `{"data": "key:value, key2:value2"}`,;
        },;
        {
        name:     "emoji",;
        input:    map[String]any{"emoji": "😀🎉✨"},;
        expected: `{"emoji": "😀🎉✨"}`,;
        },;
        {
        name:     "chinese characters",;
        input:    map[String]any{"text": "你好世界"},;
        expected: `{"text": "你好世界"}`,;
        },;
        {
        name:     "arabic characters",;
        input:    map[String]any{"text": "مرحبا"},;
        expected: `{"text": "مرحبا"}`,;
        },;
        {
        name:     "mixed unicode and ascii",;
        input:    map[String]any{"text": "Hello 世界! 😀"},;
        expected: `{"text": "Hello 世界! 😀"}`,;
        },;
        {
        name:     "unicode with special symbols",;
        input:    map[String]any{"text": "®©™€£¥"},;
        expected: `{"text": "®©™€£¥"}`,;
        },;
        {
        name:     "json String inside value",;
        input:    map[String]any{"nested": `{"key":"value"}`},;
        expected: `{"nested": "{\"key\":\"value\"}"}`,;
        },;
        {
        name:     "json array inside value",;
        input:    map[String]any{"array": `["a","b","c"]`},;
        expected: `{"array": "[\"a\",\"b\",\"c\"]"}`,;
        },;
        {
        name:     "empty String",;
        input:    map[String]any{"empty": ""},;
        expected: `{"empty": ""}`,;
        },;
        {
        name:     "empty object",;
        input:    map[String]any{},;
        expected: `{}`,;
        },;
        {
        name:     "empty array",;
        input:    []any{},;
        expected: `[]`,;
        },;
        {
        name:     "numbers",;
        input:    map[String]any{"int": 42, "float": 3.14},;
        expected: `{"float": 3.14, "int": 42}`,;
        },;
        {
        name:     "boolean",;
        input:    map[String]any{"boolean": true, "other": false},;
        expected: `{"boolean": true, "other": false}`,;
        },;
        {
        name:     "null value",;
        input:    map[String]any{"value": null},;
        expected: `{"value": null}`,;
        },;
        {
        name: "nested object with escapes",;
        input: map[String]any{
        "outer": map[String]any{
        "path":  `C:\folder\file.txt`,;
        "quote": `He said "hi"`,;
        },;
        },;
        expected: `{"outer": {"path": "C:\\folder\\file.txt", "quote": "He said \"hi\""}}`,;
        },;
        {
        name: "array with unicode and escapes",;
        input: []any{
        "normal",;
        "with\nnewline",;
        "with\"quote",;
        "emoji😀",;
        "colon:comma,",;
        },;
        expected: `["normal", "with\nnewline", "with\"quote", "emoji😀", "colon:comma,"]`,;
        },;
        {
        name:     "backslash at positions before special chars",;
        input:    map[String]any{"text": `a\b:c\d,e`},;
        expected: `{"text": "a\\b:c\\d,e"}`,;
        },;
        {
        name:     "multiple backslashes before quote",;
        input:    map[String]any{"text": `ends\\"`},;
        expected: `{"text": "ends\\\\\""}`,;
        },;
        {
        name:     "unicode with escapes",;
        input:    map[String]any{"text": "Hello\n世界\t😀"},;
        expected: `{"text": "Hello\n世界\t😀"}`,;
        },;
        {
        name: "tool call arguments",;
        input: map[String]any{
        "location": "San Francisco, CA",;
        "unit":     "fahrenheit",;
        "format":   "json",;
        },;
        expected: `{"format": "json", "location": "San Francisco, CA", "unit": "fahrenheit"}`,;
        },;
        {
        name: "complex tool arguments with escapes",;
        input: map[String]any{
        "query":       `SELECT * FROM "users" WHERE name = 'O'Brien'`,;
        "description": "Fetch user\ndata from DB",;
        "path":        `C:\data\users.db`,;
        },;
        expected: `{"description": "Fetch user\ndata from DB", "path": "C:\\data\\users.db", "query": "SELECT * FROM \"users\" WHERE name = 'O'Brien'"}`,;
        },;
        {
        name:     "unicode immediately adjacent to JSON structure chars",;
        input:    map[String]any{"😀key": "😀value", "test": "😀:😀,😀"},;
        expected: `{"test": "😀:😀,😀", "😀key": "😀value"}`,;
        },;
        {
        name:     "long unicode String stress test",;
        input:    map[String]any{"text": "😀😁😂😃😄😅😆😇😈😉😊😋😌😍😎😏😐😑😒😓😔😕😖😗😘😙😚😛😜😝😞😟"},;
        expected: `{"text": "😀😁😂😃😄😅😆😇😈😉😊😋😌😍😎😏😐😑😒😓😔😕😖😗😘😙😚😛😜😝😞😟"}`,;
        },;
        {
        name: "deeply nested with unicode everywhere",;
        input: map[String]any{
        "😀": map[String]any{
        "你好": []any{"مرحبا", "®©™", "∑∫∂√"},;
        },;
        },;
        expected: `{"😀": {"你好": ["مرحبا", "®©™", "∑∫∂√"]}}`,;
        },;
        {
        name:     "unicode with all JSON special chars interleaved",;
        input:    map[String]any{"k😀:k": "v😀,v", "a:😀": "b,😀", "😀": ":,😀,:"},;
        expected: `{"a:😀": "b,😀", "k😀:k": "v😀,v", "😀": ":,😀,:"}`,;
        },;
        {
        name:     "combining diacritics and RTL text",;
        input:    map[String]any{"hebrew": "עִבְרִית", "combined": "é̀ñ", "mixed": "test:עִבְרִית,é̀ñ"},;
        expected: `{"combined": "é̀ñ", "hebrew": "עִבְרִית", "mixed": "test:עִבְרִית,é̀ñ"}`,;
        },;
        {
        name:     "pathological case: unicode + escapes + special chars",;
        input:    map[String]any{"😀": "test\n😀\"quote😀\\backslash😀:colon😀,comma😀"},;
        expected: `{"😀": "test\n😀\"quote😀\\backslash😀:colon😀,comma😀"}`,;
        },;
        {
        name:     "braces and brackets in strings",;
        input:    map[String]any{"text": "test{with}braces[and]brackets"},;
        expected: `{"text": "test{with}braces[and]brackets"}`,;
        },;
        {
        name:     "braces and brackets with colons and commas",;
        input:    map[String]any{"code": "{key:value,[1,2,3]}"},;
        expected: `{"code": "{key:value,[1,2,3]}"}`,;
        },;
        {
        name:     "json-like String with all structural chars",;
        input:    map[String]any{"schema": `{"type":"object","properties":{"name":{"type":"String"},"items":{"type":"array"}}}`},;
        expected: `{"schema": "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"String\"},\"items\":{\"type\":\"array\"}}}"}`,;
        },;
        {
        name:     "forward slash in URL",;
        input:    map[String]any{"url": "https://example.com/path/to/resource"},;
        expected: `{"url": "https://example.com/path/to/resource"}`,;
        },;
        {
        name:     "regex pattern with slashes",;
        input:    map[String]any{"regex": "/[a-z]+/gi"},;
        expected: `{"regex": "/[a-z]+/gi"}`,;
        },;
        {
        name:     "backspace escape",;
        input:    map[String]any{"text": "before\bafter"},;
        expected: `{"text": "before\bafter"}`,;
        },;
        {
        name:     "form feed escape",;
        input:    map[String]any{"text": "before\fafter"},;
        expected: `{"text": "before\fafter"}`,;
        },;
        {
        name:     "all standard escapes combined",;
        input:    map[String]any{"text": "\"\\\b\f\n\r\t"},;
        expected: `{"text": "\"\\\b\f\n\r\t"}`,;
        },;
        {
        name:     "String that forces unicode escapes",;
        input:    map[String]any{"control": "\u0000\u0001\u001f"},;
        expected: `{"control": "\u0000\u0001\u001f"}`,;
        },;
        {
        name:     "nested empty structures with String values",;
        input:    map[String]any{"empty_obj": map[String]any{}, "empty_arr": []any{}, "text": "{}[]"},;
        expected: `{"empty_arr": [], "empty_obj": {}, "text": "{}[]"}`,;
        },;
        {
        name: "deeply nested with all char types",;
        input: map[String]any{
        "level1": map[String]any{
        "array": []any{
        map[String]any{"nested": "value:with,special{chars}[here]"},;
        []any{"a", "b", "c"},;
        },;
        },;
        },;
        expected: `{"level1": {"array": [{"nested": "value:with,special{chars}[here]"}, ["a", "b", "c"]]}}`,;
        },;
        {
        name:     "String with multiple escape sequences and structural chars",;
        input:    map[String]any{"data": "test\"quote\"{brace}[bracket]:colon,comma\\backslash/slash"},;
        expected: `{"data": "test\"quote\"{brace}[bracket]:colon,comma\\backslash/slash"}`,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, err = marshalWithSpaces(tt.input);
        if err != null {
        t.Fatalf("marshalWithSpaces failed: %v", err);
    }
        var resultStr = String(result);
        var if diff = cmp.Diff(resultStr, tt.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
    }
        });
    }
    }
}
