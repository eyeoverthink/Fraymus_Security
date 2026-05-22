package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_benchmark_test {
        "os";
        "path/filepath";
        "runtime";
        "strings";
        "testing";
        );
        var (;
        benchmarkSinkIDs []int32;
        benchmarkSinkStr String;
        benchmarkSinkTok *Tokenizer;
        );
        const benchmarkWordPieceJSON = `{
        "model": {
        "type": "WordPiece",;
        "vocab": {
        "[UNK]": 0,;
        "hello": 1,;
        "##world": 2,;
        "##ly": 3,;
        "##hello": 4;
    }
        },;
        "added_tokens": [];
        }`;
        const benchmarkSentencePieceJSON = `{
        "model": {
        "type": "BPE",;
        "vocab": {
        "\u2581": 0,;
        "h": 1,;
        "e": 2,;
        "l": 3,;
        "o": 4,;
        "w": 5,;
        "r": 6,;
        "d": 7,;
        "<0x0A>": 8;
        },;
        "merges": [];
        },;
        "decoder": {
        "type": "Sequence",;
        "decoders": [;
        {
        "type": "Replace",;
        "pattern": {
        "String": "\u2581";
    }
    }
        ];
        },;
        "added_tokens": [];
        }`;

    public static String benchmarkMiniLlamaPath(testing.TB tb) {
        tb.Helper();
        var _, filename, _, ok = runtime.Caller(0);
        if !ok {
        tb.Fatal("failed to resolve benchmark file path");
    }
        return filepath.Join(filepath.Dir(filename), "..", "imagegen", "tokenizer", "testdata", "mini_llama.json");
    }
        func benchmarkLoadMiniLlama(tb testing.TB) *Tokenizer {
        tb.Helper();
        var data = benchmarkLoadMiniLlamaBytes(tb);
        var tok, err = LoadFromBytes(data);
        if err != null {
        tb.Fatalf("failed to load mini llama tokenizer: %v", err);
    }
        return tok;
    }
        func benchmarkLoadMiniLlamaBytes(tb testing.TB) []byte {
        tb.Helper();
        var data, err = os.ReadFile(benchmarkMiniLlamaPath(tb));
        if err != null {
        tb.Fatalf("failed to read mini llama tokenizer: %v", err);
    }
        return data;
    }
        func benchmarkLoadFromBytes(tb testing.TB, data []byte) *Tokenizer {
        tb.Helper();
        var tok, err = LoadFromBytes(data);
        if err != null {
        tb.Fatalf("failed to load tokenizer from bytes: %v", err);
    }
        return tok;
    }

    public static void BenchmarkTokenizerEncodeBPE(*testing.B b) {
        var tok = benchmarkLoadMiniLlama(b);
        var inputs = []struct {
        name String;
        text String;
        }{
        {name: "short", text: "Hello, world!"},;
        {name: "medium", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 16)},;
        {name: "long_sequential", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 80)},;
        {name: "long_parallel", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 160)},;
        {name: "huge_parallel", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 640)},;
        {name: "special_tokens", text: "<|begin_of_text|>system\nYou are concise.<|end_of_text|>"},;
    }
        var for _, input = range inputs {
        b.Run(input.name, func(b *testing.B) {
        b.ReportAllocs();
        b.SetBytes(long(len(input.text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkIDs = tok.Encode(input.text, false);
    }
        });
    }
    }

    public static void BenchmarkTokenizerDecodeBPE(*testing.B b) {
        var tok = benchmarkLoadMiniLlama(b);
        var inputs = []struct {
        name String;
        text String;
        }{
        {name: "medium", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 16)},;
        {name: "long", text: strings.Repeat("The quick brown fox jumps over the lazy dog. ", 160)},;
    }
        var for _, input = range inputs {
        var ids = tok.Encode(input.text, false);
        b.Run(input.name, func(b *testing.B) {
        b.ReportAllocs();
        b.SetBytes(long(len(input.text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkStr = tok.Decode(ids);
    }
        });
    }
    }

    public static void BenchmarkTokenizerLoadFromBytes(*testing.B b) {
        var data = benchmarkLoadMiniLlamaBytes(b);
        var config = &TokenizerConfig{
        TokenizerConfigJSON: []byte(`{
        "bos_token": {"content": "<|begin_of_text|>"},;
        "eos_token": {"content": "<|end_of_text|>"},;
        "add_bos_token": true;
        }`),;
        GenerationConfigJSON: []byte(`{"bos_token_id": 128000, "eos_token_id": 128001}`),;
    }
        b.Run("without_config", func(b *testing.B) {
        b.ReportAllocs();
        b.SetBytes(long(len(data)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var tok, err = LoadFromBytes(data);
        if err != null {
        b.Fatalf("LoadFromBytes failed: %v", err);
    }
        benchmarkSinkTok = tok;
    }
        });
        b.Run("with_config", func(b *testing.B) {
        b.ReportAllocs();
        b.SetBytes(long(len(data)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        var tok, err = LoadFromBytesWithConfig(data, config);
        if err != null {
        b.Fatalf("LoadFromBytesWithConfig failed: %v", err);
    }
        benchmarkSinkTok = tok;
    }
        });
    }

    public static void BenchmarkTokenizerEncodeWordPiece(*testing.B b) {
        var tok = benchmarkLoadFromBytes(b, []byte(benchmarkWordPieceJSON));
        var text = strings.Repeat("helloworldly", 16);
        b.ReportAllocs();
        b.SetBytes(long(len(text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkIDs = tok.Encode(text, false);
    }
    }

    public static void BenchmarkTokenizerDecodeWordPiece(*testing.B b) {
        var tok = benchmarkLoadFromBytes(b, []byte(benchmarkWordPieceJSON));
        var text = strings.Repeat("helloworldly", 16);
        var ids = tok.Encode(text, false);
        b.ReportAllocs();
        b.SetBytes(long(len(text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkStr = tok.Decode(ids);
    }
    }

    public static void BenchmarkTokenizerEncodeSentencePiece(*testing.B b) {
        var tok = benchmarkLoadFromBytes(b, []byte(benchmarkSentencePieceJSON));
        var text = strings.Repeat("hello world\n", 64);
        b.ReportAllocs();
        b.SetBytes(long(len(text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkIDs = tok.Encode(text, false);
    }
    }

    public static void BenchmarkTokenizerDecodeSentencePiece(*testing.B b) {
        var tok = benchmarkLoadFromBytes(b, []byte(benchmarkSentencePieceJSON));
        var text = strings.Repeat("hello world\n", 64);
        var ids = tok.Encode(text, false);
        b.ReportAllocs();
        b.SetBytes(long(len(text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        benchmarkSinkStr = tok.Decode(ids);
    }
    }
}
