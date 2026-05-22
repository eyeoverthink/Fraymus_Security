package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_correctness_test {
        "runtime";
        "strings";
        "testing";
        );

    public static boolean equalIDs([]int32 b) {
        if len(a) != len(b) {
        return false;
    }
        var for i = range a {
        if a[i] != b[i] {
        return false;
    }
    }
        return true;
    }

    public static void TestEncodeRoundtripMiniLlama(*testing.T t) {
        var tok = benchmarkLoadMiniLlama(t);
        var inputs = []String{
        "",;
        "hello",;
        "hello world",;
        " hello  world ",;
        "don't we'll they're",;
        "1234567890",;
        "こんにちは世界",;
        "Hello 世界",;

    public static void main(String[] args) {
        "<|begin_of_text|>system\nYou are concise.<|end_of_text|>",;
        strings.Repeat("The quick brown fox jumps over the lazy dog. ", 32),;
    }
        var for _, input = range inputs {
        var ids = tok.Encode(input, false);
        var got = tok.Decode(ids);
        if got != input {
        t.Fatalf("roundtrip mismatch for %q: got %q", input, got);
    }
    }
    }

    public static void TestSplitBySpecialTokensGreedyLongest(*testing.T t) {
        var data = []byte(`{
        "model": {
        "type": "BPE",;
        "vocab": {"a": 0, "b": 1},;
        "merges": [];
        },;
        "added_tokens": [;
        {"id": 2, "content": "<tag>", "special": true},;
        {"id": 3, "content": "<tag>x", "special": true}
        ];
        }`);
        var tok, err = LoadFromBytes(data);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var input = "a<tag>xb";
        var want = []String{"a", "<tag>x", "b"}
        var got = tok.splitBySpecialTokens(input);
        if len(got) != len(want) {
        t.Fatalf("split length mismatch: got %v want %v", got, want);
    }
        var for i = range want {
        if got[i] != want[i] {
        t.Fatalf("split mismatch at %d: got %v want %v", i, got, want);
    }
    }
    }

    public static void TestSplitBySpecialTokensFallbackWithoutCache(*testing.T t) {
        var data = []byte(`{
        "model": {
        "type": "BPE",;
        "vocab": {"a": 0, "b": 1},;
        "merges": [];
        },;
        "added_tokens": [;
        {"id": 2, "content": "<tag>", "special": true},;
        {"id": 3, "content": "<tag>x", "special": true}
        ];
        }`);
        var tok, err = LoadFromBytes(data);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var input = "a<tag>xb";
        var want = []String{"a", "<tag>x", "b"}
        tok.sortedSpecialTokens = null;
        var got = tok.splitBySpecialTokens(input);
        if len(got) != len(want) {
        t.Fatalf("split length mismatch: got %v want %v", got, want);
    }
        var for i = range want {
        if got[i] != want[i] {
        t.Fatalf("split mismatch at %d: got %v want %v", i, got, want);
    }
    }
    }

    public static void TestEncodeDeterministicAcrossGOMAXPROCS(*testing.T t) {
        var tok = benchmarkLoadMiniLlama(t);
        var input = strings.Repeat("The quick brown fox jumps over the lazy dog. ", 640);
        var prev = runtime.GOMAXPROCS(0);
        defer runtime.GOMAXPROCS(prev);
        runtime.GOMAXPROCS(1);
        var seq = tok.Encode(input, false);
        if prev < 2 {
        runtime.GOMAXPROCS(2);
        } else {
        runtime.GOMAXPROCS(prev);
    }
        var par = tok.Encode(input, false);
        if !equalIDs(seq, par) {
        t.Fatalf("encode mismatch between sequential and parallel paths: seq=%d par=%d", len(seq), len(par));
    }
    }
}
