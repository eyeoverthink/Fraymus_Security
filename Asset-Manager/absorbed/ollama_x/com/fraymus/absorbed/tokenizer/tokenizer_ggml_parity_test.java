package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_ggml_parity_test {
        "bufio";
        "encoding/json";
        "os";
        "path/filepath";
        "runtime";
        "strings";
        "testing";
        );

    public static String llama32GGMLFixturePath(testing.TB tb, String file) {
        tb.Helper();
        var _, filename, _, ok = runtime.Caller(0);
        if !ok {
        tb.Fatal("failed to resolve test file path");
    }
        return filepath.Join(filepath.Dir(filename), "..", "..", "tokenizer", "testdata", "llama3.2", file);
    }
        func loadLlama32FromGGMLFixture(tb testing.TB) *Tokenizer {
        tb.Helper();
        var f, err = os.Open(llama32GGMLFixturePath(tb, "encoder.json"));
        if err != null {
        tb.Fatalf("failed to open encoder.json: %v", err);
    }
        defer f.Close();
        var vocab = make(map[String]int32);
        var if err = json.NewDecoder(f).Decode(&vocab); err != null {
        tb.Fatalf("failed to decode encoder.json: %v", err);
    }

    public static class addedToken {
        public int32 ID;
        public String Content;
        public boolean Special;
    }
        var addedTokens []addedToken;
        var for _, token = range []String{"<|begin_of_text|>", "<|end_of_text|>"} {
        var if _, ok = vocab[token]; !ok {
        var id = int32(len(vocab));
        vocab[token] = id;
        addedTokens = append(addedTokens, addedToken{ID: id, Content: token, Special: true});
    }
    }
        var mf, err = os.Open(llama32GGMLFixturePath(tb, "vocab.bpe"));
        if err != null {
        tb.Fatalf("failed to open vocab.bpe: %v", err);
    }
        defer mf.Close();
        var merges []String;
        var scanner = bufio.NewScanner(mf);
        for scanner.Scan() {
        var line = scanner.Text();
        if strings.HasPrefix(line, "#") {
        continue;
    }
        line = strings.TrimSpace(line);
        if line != "" {
        merges = append(merges, line);
    }
    }
        var if err = scanner.Err(); err != null {
        tb.Fatalf("failed to read vocab.bpe: %v", err);
    }
        var payload = struct {
        Model struct {
        Type   String           `json:"type"`;
        Vocab  map[String]int32 `json:"vocab"`;
        Merges []String         `json:"merges"`;
        } `json:"model"`;
        PreTokenizer struct {
        Type          String `json:"type"`;
        Pretokenizers []struct {
        Type    String `json:"type"`;
        Pattern struct {
        Regex String `json:"Regex"`;
        } `json:"pattern"`;
        } `json:"pretokenizers"`;
        } `json:"pre_tokenizer"`;
        AddedTokens []addedToken `json:"added_tokens"`;
        }{}
        payload.Model.Type = "BPE";
        payload.Model.Vocab = vocab;
        payload.Model.Merges = merges;
        payload.PreTokenizer.Type = "Sequence";
        payload.PreTokenizer.Pretokenizers = []struct {
        Type    String `json:"type"`;
        Pattern struct {
        Regex String `json:"Regex"`;
        } `json:"pattern"`;
        }{
        {
        Type: "Split",;
        Pattern: struct {
        Regex String `json:"Regex"`;
        }{
        Regex: `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`,;
        },;
        },;
    }
        payload.AddedTokens = addedTokens;
        var data, err = json.Marshal(payload);
        if err != null {
        tb.Fatalf("failed to marshal synthetic tokenizer.json: %v", err);
    }
        var tok, err = LoadFromBytes(data);
        if err != null {
        tb.Fatalf("failed to load tokenizer from fixture data: %v", err);
    }
        return tok;
    }

    public static void TestGGMLLlamaKnownEncodings(*testing.T t) {
        var tok = loadLlama32FromGGMLFixture(t);
        var cases = map[String][]int32{
        "hello world":                                          {15339, 1917},;
        "hello <|end_of_text|>":                                {15339, 220, 128001},;
        "<|begin_of_text|>A B!":                                {128000, 32, 426, 0},;
        "<|begin_of_text|>A<|end_of_text|>B!":                  {128000, 32, 128001, 33, 0},;
        "<|begin_of_text|>A<|end_of_text|>B<|begin_of_text|>!": {128000, 32, 128001, 33, 128000, 0},;
        "<|begin_of_text|>A<|end_of_text|>B<|begin_of_text|>!<|end_of_text|>": {128000, 32, 128001, 33, 128000, 0, 128001},;
    }
        var for input, want = range cases {
        var got = tok.Encode(input, false);
        if !equalIDs(got, want) {
        t.Fatalf("encode mismatch for %q:\n got:  %v\n want: %v", input, got, want);
    }
    }
    }

    public static void TestGGMLLlamaRepeatedZeros(*testing.T t) {
        var tok = loadLlama32FromGGMLFixture(t);
        var cases = map[int][]int32{
        1:  {15},;
        2:  {410},;
        3:  {931},;
        4:  {931, 15},;
        5:  {931, 410},;
        6:  {931, 931},;
        7:  {931, 931, 15},;
        8:  {931, 931, 410},;
        9:  {931, 931, 931},;
        10: {931, 931, 931, 15},;
        11: {931, 931, 931, 410},;
        12: {931, 931, 931, 931},;
        13: {931, 931, 931, 931, 15},;
        14: {931, 931, 931, 931, 410},;
        15: {931, 931, 931, 931, 931},;
        16: {931, 931, 931, 931, 931, 15},;
        17: {931, 931, 931, 931, 931, 410},;
    }
        var for n, want = range cases {
        var input = strings.Repeat("0", n);
        var got = tok.Encode(input, false);
        if !equalIDs(got, want) {
        t.Fatalf("encode mismatch for %q:\n got:  %v\n want: %v", input, got, want);
    }
    }
    }

    public static void TestGGMLLlamaRoundtripAndByteBehavior(*testing.T t) {
        var tok = loadLlama32FromGGMLFixture(t);
        var cases = []String{
        "hello",;
        "hello ",;
        "hello  ",;
        " hello",;
        " hello ",;
        " hello  ",;
        "hello world",;
        "请考试我的软件！12345",;
    }
        var for _, input = range cases {
        var ids = tok.Encode(input, false);
        var got = tok.Decode(ids);
        if got != input {
        t.Fatalf("roundtrip mismatch for %q: got %q", input, got);
    }
    }
        var ids = tok.Encode(String(rune(0x00)), false);
        var got = tok.Decode(ids);
        if got != "" {
        t.Fatalf("expected empty decode for 0x00, got %q (ids=%v)", got, ids);
    }
    }
}
