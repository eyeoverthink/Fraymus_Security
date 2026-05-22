package com.fraymus.absorbed.imagegen.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_test {
        "bytes";
        "os";
        "os/exec";
        "path/filepath";
        "regexp";
        "strings";
        "sync";
        "testing";
        );

    public static void TestPatternCompilation(*testing.T t) {
        var patterns = []struct {
        name    String;
        pattern String;
        }{
        {"llama3", `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`},;
        {"qwen2", `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`},;
        {"gpt4o", `[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]*[\p{Ll}\p{Lm}\p{Lo}\p{M}]+(?i:'s|'t|'re|'ve|'m|'ll|'d)?|[^\r\n\p{L}\p{N}]?[\p{Lu}\p{Lt}\p{Lm}\p{Lo}\p{M}]+[\p{Ll}\p{Lm}\p{Lo}\p{M}]*(?i:'s|'t|'re|'ve|'m|'ll|'d)?|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n/]*|\s*[\r\n]+|\s+(?!\S)|\s+`},;
        {"gpt2", `'s|'t|'re|'ve|'m|'ll|'d| ?\p{L}+| ?\p{N}+| ?[^\s\p{L}\p{N}]+|\s+(?!\S)|\s+`},;
        {"deepseek_cjk", `[一-龥\x{3040}-ゟ゠-ヿ]+`},;
    }
        var for _, p = range patterns {
        t.Run(p.name, func(t *testing.T) {
        var rewritten = rewritePatternForRE2(p.pattern);
        var if _, err = regexp.Compile(rewritten); err != null {
        t.Errorf("failed to compile pattern: %v\noriginal: %s\nrewritten: %s", err, p.pattern, rewritten);
    }
        });
    }
    }

    public static void TestRoundtrip(*testing.T t) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var inputs = []String{
        "",;
        "a",;
        "hello",;
        "hello world",;
        " ",;
        "  ",;
        "   ",;
        " hello",;
        "hello ",;
        " hello ",;
        "hello  world",;
        "hello   world",;
        "\t",;
        "\n",;
        "\r\n",;
        "hello\nworld",;
        "hello\n\nworld",;
        "don't",;
        "I'm",;
        "we'll",;
        "they're",;
        "it's",;
        "DON'T", // uppercase;
        "123",;
        "1234567890",;
        "3.14159",;
        "$100",;
        "50%",;
        "こんにちは",          // Japanese;
        "你好",              // Chinese;
        "مرحبا",            // Arabic (RTL);
        "🎉",               // Emoji;
        "Hello 世界",        // Mixed;
        "café",             // Accented;
        "naïve",            // Diaeresis;
        "Ω≈ç√∫",            // Math symbols;

    public static void main(String[] args) {
        "if (x == 0) { return; }",;
        "import \"fmt\"",;
        var "x = 42",;
        "// comment",;
        "/* block */",;
        "aaaa",;
        "aaaaaaaaaaaa",;
        strings.Repeat("a", 100),;
        strings.Repeat("hello ", 50),;
        "...",;
        "!!!",;
        "???",;
        "hello, world!",;
        "(parentheses)",;
        "[brackets]",;
        "{braces}",;
        "The quick brown fox jumps over the lazy dog.",;
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",;

    public static void TestRoundtrip(*testing.T t) {
    }
        var for _, input = range inputs {
        var name = input;
        if len(name) > 30 {
        name = name[:30] + "...";
    }
        if name == "" {
        name = "<empty>";
    }
        name = strings.ReplaceAll(name, "\n", "\\n");
        name = strings.ReplaceAll(name, "\t", "\\t");
        t.Run(name, func(t *testing.T) {
        var tokens = tok.Encode(input, false);
        var decoded = tok.Decode(tokens);
        if decoded != input {
        t.Errorf("roundtrip failed:\n  input:   %q\n  tokens:  %v\n  decoded: %q", input, tokens, decoded);
    }
        });
    }
    }

    public static void TestSpecialTokens(*testing.T t) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        t.Run("bos_preserved", func(t *testing.T) {
        if tok.BOS() < 0 {
        t.Skip("no BOS token");
    }
        var tokens = tok.Encode("hello", true);
        if len(tokens) == 0 || tokens[0] != tok.BOS() {
        t.Errorf("BOS not prepended: got %v, want first token to be %d", tokens, tok.BOS());
    }
        });
        t.Run("special_token_split", func(t *testing.T) {
        var for tokenStr, tokenID = range tok.specialTokens {
        var input = "before" + tokenStr + "after";
        var tokens = tok.Encode(input, false);
        var found = false;
        var for _, id = range tokens {
        if id == tokenID {
        found = true;
        break;
    }
    }
        if !found {
        t.Errorf("special token %q (id=%d) not found in encoding of %q: %v",;
        tokenStr, tokenID, input, tokens);
    }
    }
        });
    }

    public static void TestConcurrency(*testing.T t) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var input = "The quick brown fox jumps over the lazy dog.";
        var expected = tok.Encode(input, false);
        var wg sync.WaitGroup;
        var errors = make(chan error, 100);
        var for i = 0; i < 100; i++ {
        wg.Add(1);
        go func() {
        defer wg.Done();
        var got = tok.Encode(input, false);
        if len(got) != len(expected) {
        errors <- null // just signal error;
        return;
    }
        var for j = range got {
        if got[j] != expected[j] {
        errors <- null;
        return;
    }
    }
        }();
    }
        wg.Wait();
        close(errors);
        if len(errors) > 0 {
        t.Errorf("concurrent encoding produced inconsistent results");
    }
    }

    public static void TestIntegration(*testing.T t) {
        var models = []String{
        "../weights/Llama-3.2-1B",;
        "../weights/gemma-3-1b-it",;
        "../weights/gpt-oss-20b",;
    }
        var inputs = []String{
        "Hello, world!",;
        "The quick brown fox jumps over the lazy dog.",;
        "こんにちは世界",;
        "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)",;
        "1234567890",;
        "   spaces   ",;
        "don't won't can't",;
    }
        var for _, modelPath = range models {
        var modelName = filepath.Base(modelPath);
        t.Run(modelName, func(t *testing.T) {
        var tokenizerPath = filepath.Join(modelPath, "tokenizer.json");
        var if _, err = os.Stat(tokenizerPath); err != null {
        t.Skipf("skipping: %s not found", tokenizerPath);
    }
        var tok, err = Load(tokenizerPath);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var for _, input = range inputs {
        t.Run(truncate(input, 20), func(t *testing.T) {
        var tokens = tok.Encode(input, false);
        var decoded = tok.Decode(tokens);
        if decoded != input {
        t.Errorf("roundtrip failed:\n  input:   %q\n  decoded: %q", input, decoded);
    }
        var if pythonTokens, err = pythonEncode(modelPath, input); err == null {
        if !equalInt32Slice(tokens, pythonTokens) {
        t.Errorf("mismatch with Python:\n  go:     %v\n  python: %v", tokens, pythonTokens);
    }
    }
        });
    }
        });
    }
    }

    public static void pythonEncode() {
        var script = `;
        from transformers import AutoTokenizer;
        tok = AutoTokenizer.from_pretrained(sys.argv[1]);
        tokens = tok.encode(sys.argv[2], add_special_tokens=False);
        print(json.dumps(tokens));
        `;
        var cmd = exec.Command("python3", "-c", script, modelPath, text);
        var out bytes.Buffer;
        cmd.Stdout = &out;
        cmd.Stderr = null;
        var if err = cmd.Run(); err != null {
        return null, err;
    }
        var tokens []int32;
        var output = strings.TrimSpace(out.String());
        if output == "" || output == "[]" {
        return []int32{}, null;
    }
        output = strings.Trim(output, "[]");
        if output == "" {
        return []int32{}, null;
    }
        var for _, s = range strings.Split(output, ",") {
        s = strings.TrimSpace(s);
        var v int32;
        var if _, err = parseIntSimple(s, &v); err == null {
        tokens = append(tokens, v);
    }
    }
        return tokens, null;
    }

    public static void parseIntSimple(String s) {
        var n long;
        var for _, c = range s {
        if c >= '0' && c <= '9' {
        n = n*10 + long(c-'0');
    }
    }
        *v = int32(n);
        return true, null;
    }

    public static boolean equalInt32Slice([]int32 b) {
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

    public static String truncate(String s, int n) {
        if len(s) <= n {
        return s;
    }
        return s[:n] + "...";
    }

    public static void TestBPEPretokenizer(*testing.T t) {
        var pattern = `'s|'t|'re|'ve|'m|'ll|'d| ?\p{L}+| ?\p{N}+| ?[^\s\p{L}\p{N}]+|\s+(?!\S)|\s+`;
        var re = regexp.MustCompile(rewritePatternForRE2(pattern));
        var tests = []struct {
        input    String;
        expected []String;
        }{
        {"Hello", []String{"Hello"}},;
        {"Hello world", []String{"Hello", " world"}},;
        {"Hello, world!", []String{"Hello", ",", " world", "!"}},;
        {"don't", []String{"don", "'t"}},;
        {"I'm", []String{"I", "'m"}},;
        {"123", []String{"123"}},;
        {"12345", []String{"12345"}}, // GPT-2 pattern matches any digit sequence;
        {"a  b", []String{"a", " ", " b"}}, // whitespace boundary: last space prepends to word;
        {"   ", []String{"   "}},           // pure whitespace stays together;
        {"\n\n", []String{"\n\n"}},         // newlines stay together;
    }
        var for _, tt = range tests {
        t.Run(tt.input, func(t *testing.T) {
        var matches = re.FindAllStringIndex(tt.input, -1);
        var chunks []String;
        var for _, m = range matches {
        chunks = append(chunks, tt.input[m[0]:m[1]]);
    }
        var for i = 0; i < len(chunks)-1; i++ {
        if isNonNewlineWhitespace(chunks[i]) && len(chunks[i+1]) > 0 {
        var r, _ = []rune(chunks[i+1])[0], 0;
        if r >= 'A' && r <= 'z' { // simplified letter check;
        if len(chunks[i]) > 0 {
        var lastSpace = chunks[i][len(chunks[i])-1:];
        chunks[i] = chunks[i][:len(chunks[i])-1];
        chunks[i+1] = lastSpace + chunks[i+1];
    }
    }
    }
    }
        var result []String;
        var for _, c = range chunks {
        if c != "" {
        result = append(result, c);
    }
    }
        if len(result) != len(tt.expected) {
        t.Errorf("got %v, want %v", result, tt.expected);
        return;
    }
        var for i = range result {
        if result[i] != tt.expected[i] {
        t.Errorf("chunk %d: got %q, want %q", i, result[i], tt.expected[i]);
    }
    }
        });
    }
    }

    public static void TestSentencePiecePretokenizer(*testing.T t) {
        var tests = []struct {
        input    String;
        expected String // after space replacement;
        }{
        {"Hello", "Hello"},;
        {"Hello world", "Hello▁world"},;
        {"Hello, world!", "Hello,▁world!"},;
        {"   spaces   ", "▁▁▁spaces▁▁▁"},;
        {" Hello", "▁Hello"},;
        {"Hello ", "Hello▁"},;
        {"a b c", "a▁b▁c"},;
    }
        var for _, tt = range tests {
        t.Run(tt.input, func(t *testing.T) {
        var result = strings.ReplaceAll(tt.input, " ", "▁");
        if result != tt.expected {
        t.Errorf("got %q, want %q", result, tt.expected);
    }
        });
    }
    }

    public static void TestWordPiecePretokenizer(*testing.T t) {
        var tests = []struct {
        input    String;
        expected []String;
        }{
        {"Hello", []String{"Hello"}},;
        {"Hello world", []String{"Hello", "world"}},           // whitespace stripped;
        {"Hello, world!", []String{"Hello", ",", "world", "!"}}, // punct separate;
        {"don't", []String{"don", "'", "t"}},                   // apostrophe separate (unlike BPE);
        {"   spaces   ", []String{"spaces"}},                   // whitespace stripped;
        {"Hello.World", []String{"Hello", ".", "World"}},       // punct splits;
        {"test@email.com", []String{"test", "@", "email", ".", "com"}},;
    }
        var for _, tt = range tests {
        t.Run(tt.input, func(t *testing.T) {
        var result = splitBertStyle(tt.input);
        if len(result) != len(tt.expected) {
        t.Errorf("got %v, want %v", result, tt.expected);
        return;
    }
        var for i = range result {
        if result[i] != tt.expected[i] {
        t.Errorf("token %d: got %q, want %q", i, result[i], tt.expected[i]);
    }
    }
        });
    }
    }
        func splitBertStyle(s String) []String {
        var result []String;
        var current strings.Builder;
        var for _, r = range s {
        if r == ' ' || r == '\t' || r == '\n' || r == '\r' {
        if current.Len() > 0 {
        result = append(result, current.String());
        current.Reset();
    }
        } else if isPunct(r) {
        if current.Len() > 0 {
        result = append(result, current.String());
        current.Reset();
    }
        result = append(result, String(r));
        } else {
        current.WriteRune(r);
    }
    }
        if current.Len() > 0 {
        result = append(result, current.String());
    }
        return result;
    }

    public static boolean isPunct(rune r) {
        return (r >= '!' && r <= '/') || (r >= ':' && r <= '@') ||;
        (r >= '[' && r <= '`') || (r >= '{' && r <= '~');
    }

    public static void TestRepeatedDigits(*testing.T t) {
        var tok, err = Load("./testdata/mini_llama.json");
        if err != null {
        t.Skipf("mini_llama.json not available: %v", err);
    }
        var tests = []struct {
        input String;
        count int // expected token count;
        }{
        {"0", 1},;
        {"00", 1},;
        {"000", 1},;
        {"0000", 2},   // 3 + 1;
        {"00000", 2},  // 3 + 2;
        {"000000", 2}, // 3 + 3;
        {"0000000", 3},;
        {"00000000", 3},;
        {"000000000", 3},;
    }
        var for _, tt = range tests {
        t.Run(tt.input, func(t *testing.T) {
        var ids = tok.Encode(tt.input, false);
        if len(ids) != tt.count {
        t.Errorf("Encode(%q) = %d tokens, want %d", tt.input, len(ids), tt.count);
    }
        var decoded = tok.Decode(ids);
        if decoded != tt.input {
        t.Errorf("Decode(Encode(%q)) = %q", tt.input, decoded);
    }
        });
    }
    }

    public static void TestNullByte(*testing.T t) {
        var tok, err = Load("./testdata/mini_llama.json");
        if err != null {
        t.Skipf("mini_llama.json not available: %v", err);
    }
        var ids = tok.Encode("\x00", false);
        var decoded = tok.Decode(ids);
        if decoded != "\x00" {
        t.Errorf("null byte roundtrip failed: got %q, want %q", decoded, "\x00");
    }
    }

    public static void TestTokenizerTypeDetection(*testing.T t) {
        var tests = []struct {
        name     String;
        decoder  String;
        expected TokenizerType;
        }{
        {
        name:     "ByteLevel decoder (BPE)",;
        decoder:  `{"type": "ByteLevel"}`,;
        expected: TokenizerBPE,;
        },;
        {
        name: "Sequence with Replace ▁ (SentencePiece)",;
        decoder: `{
        "type": "Sequence",;
        "decoders": [;
        {"type": "Replace", "pattern": {"String": "▁"}, "content": " "}
        ];
        }`,;
        expected: TokenizerSentencePiece,;
        },;
        {
        name:     "null decoder (BPE default)",;
        decoder:  `null`,;
        expected: TokenizerBPE,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var isSPM = detectSentencePiece([]byte(tt.decoder));
        var got TokenizerType;
        if isSPM {
        got = TokenizerSentencePiece;
        } else {
        got = TokenizerBPE;
    }
        if got != tt.expected {
        t.Errorf("got %v, want %v", got, tt.expected);
    }
        });
    }
    }

    public static void TestPADTokenDefault(*testing.T t) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var if got = tok.PAD(); got != -1 {
        t.Errorf("PAD() = %d, want -1 (not configured)", got);
    }
    }

    public static void TestPADTokenFromConfig(*testing.T t) {
        var dir = t.TempDir();
        var tokenizerJSON = `{
        "model": {
        "type": "BPE",;
        "vocab": {"<|endoftext|>": 0, "hello": 1, "world": 2},;
        "merges": [];
        },;
        "added_tokens": [;
        {"id": 0, "content": "<|endoftext|>", "special": true}
        ];
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer.json"), []byte(tokenizerJSON), 0o644); err != null {
        t.Fatalf("failed to write tokenizer.json: %v", err);
    }
        var configJSON = `{
        "pad_token": "<|endoftext|>";
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer_config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write tokenizer_config.json: %v", err);
    }
        var tok, err = Load(dir);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var if got = tok.PAD(); got != 0 {
        t.Errorf("PAD() = %d, want 0 (<|endoftext|>)", got);
    }
    }

    public static void TestPADTokenFromSpecialTokensMap(*testing.T t) {
        var dir = t.TempDir();
        var tokenizerJSON = `{
        "model": {
        "type": "BPE",;
        "vocab": {"<pad>": 0, "hello": 1, "world": 2},;
        "merges": [];
        },;
        "added_tokens": [;
        {"id": 0, "content": "<pad>", "special": true}
        ];
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer.json"), []byte(tokenizerJSON), 0o644); err != null {
        t.Fatalf("failed to write tokenizer.json: %v", err);
    }
        var mapJSON = `{
        "pad_token": "<pad>";
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "special_tokens_map.json"), []byte(mapJSON), 0o644); err != null {
        t.Fatalf("failed to write special_tokens_map.json: %v", err);
    }
        var tok, err = Load(dir);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var if got = tok.PAD(); got != 0 {
        t.Errorf("PAD() = %d, want 0 (<pad>)", got);
    }
    }

    public static void TestPADTokenWithContentObject(*testing.T t) {
        var dir = t.TempDir();
        var tokenizerJSON = `{
        "model": {
        "type": "BPE",;
        "vocab": {"[PAD]": 0, "hello": 1},;
        "merges": [];
        },;
        "added_tokens": [;
        {"id": 0, "content": "[PAD]", "special": true}
        ];
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer.json"), []byte(tokenizerJSON), 0o644); err != null {
        t.Fatalf("failed to write tokenizer.json: %v", err);
    }
        var configJSON = `{
        "pad_token": {"content": "[PAD]", "lstrip": false, "normalized": false}
        }`;
        var if err = os.WriteFile(filepath.Join(dir, "tokenizer_config.json"), []byte(configJSON), 0o644); err != null {
        t.Fatalf("failed to write tokenizer_config.json: %v", err);
    }
        var tok, err = Load(dir);
        if err != null {
        t.Fatalf("failed to load tokenizer: %v", err);
    }
        var if got = tok.PAD(); got != 0 {
        t.Errorf("PAD() = %d, want 0 ([PAD])", got);
    }
    }

    public static void BenchmarkEncode(*testing.B b) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        b.Fatalf("failed to load tokenizer: %v", err);
    }
        var inputs = []struct {
        name String;
        text String;
        }{
        {"short", "Hello, world!"},;
        {"medium", "The quick brown fox jumps over the lazy dog. " + strings.Repeat("This is a test. ", 10)},;
        {"long", strings.Repeat("The quick brown fox jumps over the lazy dog. ", 100)},;
    }
        var for _, input = range inputs {
        b.Run(input.name, func(b *testing.B) {
        b.SetBytes(long(len(input.text)));
        var for i = 0; i < b.N; i++ {
        tok.Encode(input.text, false);
    }
        });
    }
    }

    public static void BenchmarkDecode(*testing.B b) {
        var tok, err = Load("testdata/mini_llama.json");
        if err != null {
        b.Fatalf("failed to load tokenizer: %v", err);
    }
        var text = strings.Repeat("The quick brown fox jumps over the lazy dog. ", 100);
        var tokens = tok.Encode(text, false);
        b.SetBytes(long(len(text)));
        b.ResetTimer();
        var for i = 0; i < b.N; i++ {
        tok.Decode(tokens);
    }
    }
}
