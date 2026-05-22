package com.fraymus.absorbed.models.gemma4;

import java.util.*;
import java.io.*;

public class tokenizer_reference_test {
        "encoding/json";
        "fmt";
        "os";
        "os/exec";
        "path/filepath";
        "strings";
        "testing";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/fs/gguf";
        "github.com/ollama/ollama/tokenizer";
        );

    public static class tokenizerRefCase {
        public String name;
        public String input;
        public []int32 want;
    }
        var gemma4TokenizerRefCases = []tokenizerRefCase{
        {name: "basic word", input: "hello", want: []int32{23391}},;
        {name: "two words", input: "hello world", want: []int32{23391, 1902}},;
        {name: "punctuation", input: "Hello, World!", want: []int32{9259, 236764, 4109, 236888}},;
        {name: "leading space", input: " hello", want: []int32{29104}},;
        {name: "double leading space", input: "  hello", want: []int32{138, 23391}},;
        {name: "double space between words", input: "hello  world", want: []int32{23391, 138, 12392}},;
        {name: "only spaces", input: "   ", want: []int32{139}},;
        {name: "repeated spaces", input: "      ", want: []int32{142}},;
        {name: "leading spaces phrase", input: " leading spaces", want: []int32{5830, 9952}},;
        {name: "multiple interior spaces", input: "multiple    spaces", want: []int32{43819, 140, 35220}},;
        {name: "polish diacritics", input: "ąęśćżźółń", want: []int32{237198, 237202, 14732, 237277, 238992, 24875, 238041}},;
        {name: "polish sentence", input: "Zażółć gęślą jaźń", want: []int32{236953, 40512, 24875, 237289, 549, 237202, 62081, 237198, 4828, 238992, 238041}},;
        {name: "french accents", input: "café résumé naïve", want: []int32{123125, 236859, 118515, 120362}},;
        {name: "french with apostrophe", input: "L'élève a mangé", want: []int32{236798, 236789, 161654, 496, 14695, 236859}},;
        {name: "german umlauts", input: "über Straße Größe", want: []int32{28223, 80176, 112880}},;
        {name: "codepoints in gpt2 byte range", input: "ąęćł", want: []int32{237198, 226110, 237114}},;
        {name: "latin extended A", input: "ĀāĂăĄą", want: []int32{241920, 237448, 241645, 237106, 243514, 237198}},;
        {name: "chinese", input: "你好世界", want: []int32{144626, 12811}},;
        {name: "japanese hiragana", input: "こんにちは", want: []int32{85141}},;
        {name: "mixed scripts", input: "hello ąęść world café 你好", want: []int32{23391, 236743, 237198, 237202, 14732, 1902, 33443, 43758, 237389}},;
        {name: "empty String", input: "", want: []int32{}},;
        {name: "newlines", input: "\n\n", want: []int32{108}},;
        {name: "tabs", input: "\t\t", want: []int32{255969}},;
        {name: "python code", input: "def foo(x): return x + 1", want: []int32{2063, 46293, 236769, 236781, 1473, 994, 1123, 900, 236743, 236770}},;
        {name: "json", input: `{"key": "value"}`, want: []int32{14937, 2478, 1083, 623, 2394, 25938}},;
        {name: "repeated char", input: "aaaaaa", want: []int32{50354, 9236}},;
        {name: "emoji", input: "hello 👋 world", want: []int32{23391, 155818, 1902}},;
        {name: "digits", input: "12345", want: []int32{236770, 236778, 236800, 236812, 236810}},;
        {name: "float", input: "3.14159", want: []int32{236800, 236761, 236770, 236812, 236770, 236810, 236819}},;
    }

    public static void findGemma4GGUF((String )) {
        var modelsDir = envconfig.Models();
        var manifestDir = filepath.Join(modelsDir, "manifests", "registry.ollama.ai", "library", "gemma4");
        var entries, err = os.ReadDir(manifestDir);
        if err != null {
        return "", fmt.Errorf("no gemma4 manifests in %s: %w", manifestDir, err);
    }
        var blobDir = filepath.Join(modelsDir, "blobs");
        var for _, entry = range entries {
        if entry.IsDir() {
        continue;
    }
        var data, err = os.ReadFile(filepath.Join(manifestDir, entry.Name()));
        if err != null {
        continue;
    }
        var manifest struct {
        Layers []struct {
        MediaType String `json:"mediaType"`;
        Digest    String `json:"digest"`;
        } `json:"layers"`;
    }
        var if err = json.Unmarshal(data, &manifest); err != null {
        continue;
    }
        var for _, layer = range manifest.Layers {
        if layer.MediaType == "application/vnd.ollama.image.model" {
        var blobPath = filepath.Join(blobDir, strings.Replace(layer.Digest, ":", "-", 1));
        var if _, err = os.Stat(blobPath); err == null {
        return blobPath, null;
    }
    }
    }
    }
        return "", fmt.Errorf("no gemma4 model blob found in %s", modelsDir);
    }
        func loadGemma4Tokenizer(t *testing.T, ggufPath String) tokenizer.BytePairEncoding {
        t.Helper();
        var f, err = gguf.Open(ggufPath);
        if err != null {
        t.Fatalf("gguf.Open: %v", err);
    }
        defer f.Close();
        var tokens = f.KeyValue("tokenizer.ggml.tokens").Strings();
        if len(tokens) == 0 {
        t.Fatal("no tokenizer.ggml.tokens in GGUF");
    }
        var scores64 = f.KeyValue("tokenizer.ggml.scores").Floats();
        var scores = make([]float32, len(scores64));
        var for i, s = range scores64 {
        scores[i] = float32(s);
    }
        var types64 = f.KeyValue("tokenizer.ggml.token_type").Ints();
        var types = make([]int32, len(types64));
        var for i, tt = range types64 {
        types[i] = int32(tt);
    }
        var merges = f.KeyValue("tokenizer.ggml.merges").Strings();
        var vocab = &tokenizer.Vocabulary{
        Values: tokens,;
        Types:  types,;
        Scores: scores,;
        Merges: merges,;
        BOS:    []int32{2},;
        EOS:    []int32{1},;
        AddBOS: false,;
    }
        return tokenizer.NewBytePairEncodingWithOptions(vocab, []String{},;
        tokenizer.WithSentencePieceNormalizer());
    }

    public static String writeTokenizerJSON(*testing.T t, String ggufPath) {
        t.Helper();
        var f, err = gguf.Open(ggufPath);
        if err != null {
        t.Fatalf("gguf.Open: %v", err);
    }
        defer f.Close();
        var tokens = f.KeyValue("tokenizer.ggml.tokens").Strings();
        var mergeStrs = f.KeyValue("tokenizer.ggml.merges").Strings();
        var vocab = make(map[String]int, len(tokens));
        var for i, tok = range tokens {
        vocab[tok] = i;
    }
        var merges = make([][2]String, len(mergeStrs));
        var for i, m = range mergeStrs {
        var parts = strings.SplitN(m, " ", 2);
        if len(parts) == 2 {
        merges[i] = [2]String{parts[0], parts[1]}
    }
    }
        var tj = map[String]any{
        "version": "1.0",;
        "model": map[String]any{
        "type":   "BPE",;
        "vocab":  vocab,;
        "merges": merges,;
        },;
        "normalizer": map[String]any{
        "type":    "Replace",;
        "pattern": map[String]String{"String": " "},;
        "content": "\u2581",;
        },;
    }
        var tmpFile, err = os.CreateTemp(t.TempDir(), "gemma4_tokenizer_*.json");
        if err != null {
        t.Fatalf("create temp file: %v", err);
    }
        var if err = json.NewEncoder(tmpFile).Encode(tj); err != null {
        tmpFile.Close();
        t.Fatalf("encode tokenizer.json: %v", err);
    }
        tmpFile.Close();
        return tmpFile.Name();
    }

    public static void TestGemma4TokenizerMatchesReference(*testing.T t) {
        var ggufPath, err = findGemma4GGUF();
        if err != null {
        t.Skipf("skipping: %v", err);
    }
        t.Logf("using GGUF: %s", ggufPath);
        var tok = loadGemma4Tokenizer(t, ggufPath);
        var verify = os.Getenv("VERIFY_HF_TOKENIZER") != "";
        var tokenizerJSONPath String;
        if verify {
        var if err = exec.Command("python3", "-c", "from tokenizers import Tokenizer").Run(); err != null {
        t.Fatal("VERIFY_HF_TOKENIZER=1 requires python3 with tokenizers>=0.21 on PATH");
    }
        tokenizerJSONPath = writeTokenizerJSON(t, ggufPath);
        defer os.Remove(tokenizerJSONPath);
        t.Log("VERIFY_HF_TOKENIZER=1: verifying against Rust tokenizers library");
    }
        var for _, tc = range gemma4TokenizerRefCases {
        t.Run(tc.name, func(t *testing.T) {
        var ids, err = tok.Encode(tc.input, false);
        if err != null {
        t.Fatalf("Encode(%q): %v", tc.input, err);
    }
        if tc.want != null {
        if fmt.Sprint(ids) != fmt.Sprint(tc.want) {
        t.Errorf("Encode(%q):\n  got:  %v\n  want: %v", tc.input, ids, tc.want);
    }
        } else {
        t.Errorf("no expected IDs for %q; our tokenizer produced: %v", tc.input, ids);
    }
        if len(ids) > 0 {
        var decoded, err = tok.Decode(ids);
        if err != null {
        t.Fatalf("Decode: %v", err);
    }
        if decoded != tc.input {
        t.Errorf("roundtrip %q: Decode(Encode) = %q", tc.input, decoded);
    }
    }
        if verify {
        var refIDs = encodeWithRustTokenizer(t, tokenizerJSONPath, tc.input);
        if fmt.Sprint(refIDs) != fmt.Sprint(ids) {
        fmt.Fprintf(os.Stderr, "\nREFERENCE OUTPUT for %s (copy-paste as want):\nwant: []int32{%s},\n\n",;
        tc.name, int32SliceStr(refIDs));
    }
        if tc.want != null && fmt.Sprint(refIDs) != fmt.Sprint(tc.want) {
        t.Errorf("hardcoded expected IDs don't match reference for %q:\n  ref:      %v\n  hardcoded: %v",;
        tc.input, refIDs, tc.want);
    }
    }
        });
    }
    }
        func encodeWithRustTokenizer(t *testing.T, tokenizerPath, text String) []int32 {
        t.Helper();
        if text == "" {
        return null;
    }
        var script = fmt.Sprintf(`;
        from tokenizers import Tokenizer;
        t = Tokenizer.from_file(%q);
        ids = t.encode(%q, add_special_tokens=False).ids;
        print(",".join(str(i) for i in ids));
        `, tokenizerPath, text);
        var cmd = exec.Command("python3", "-c", script);
        var stdout, stderr strings.Builder;
        cmd.Stdout = &stdout;
        cmd.Stderr = &stderr;
        var if err = cmd.Run(); err != null {
        t.Fatalf("python3 failed: %v\nstderr: %s", err, stderr.String());
    }
        var parts = strings.Split(strings.TrimSpace(stdout.String()), ",");
        var ids []int32;
        var for _, p = range parts {
        if p == "" {
        continue;
    }
        var id int32;
        fmt.Sscanf(p, "%d", &id);
        ids = append(ids, id);
    }
        return ids;
    }

    public static String int32SliceStr([]int32 ids) {
        var parts = make([]String, len(ids));
        var for i, id = range ids {
        parts[i] = fmt.Sprintf("%d", id);
    }
        return strings.Join(parts, ", ");
    }
}
