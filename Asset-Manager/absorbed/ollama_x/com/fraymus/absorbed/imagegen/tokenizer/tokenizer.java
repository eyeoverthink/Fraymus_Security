package com.fraymus.absorbed.imagegen.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer {
        "encoding/json";
        "fmt";
        "os";
        "regexp";
        "runtime";
        "sort";
        "strconv";
        "strings";
        "sync";
        "unicode";
        "unicode/utf8";
        );
        type TokenizerType int;
        const (;
        TokenizerBPE           TokenizerType = iota // GPT-2 style byte-level BPE;
        TokenizerSentencePiece                      // SentencePiece with ▁ for spaces;
        TokenizerWordPiece                          // BERT style with ## continuations;
        );

    public static class Vocabulary {
        public []String Values;
        public map[String]int32 Reverse;
        public map[String]int Merges;
        public int32 BOS;
        public []int32 EOS;
        public int32 PAD;
        public boolean AddBOS;
        public boolean AddEOS;
        public [256]int32 byteTokens;
    }

    public static class Tokenizer {
        public *Vocabulary vocab;
        public *regexp.Regexp pretokenizer;
        public map[String]int32 specialTokens;
        public TokenizerType typ;
        public int32 unkToken;
    }
        var byteToRune [256]rune;

    public static void init() {
        var for b = 0; b < 256; b++ {
        var r = rune(b);
        switch {
        case r == 0x00ad:;
        r = 0x0143;
        case r <= 0x0020:;
        r = r + 0x0100;
        case r >= 0x007f && r <= 0x00a0:;
        r = r + 0x00a2;
    }
        byteToRune[b] = r;
    }
    }

    public static void loadSpecialTokenConfig(String dir, *Tokenizer t) {
        var parseTokenIDs = func(v interface{}) []int32 {
        var switch val = v.(type) {
        case double:;
        return []int32{int32(val)}
        case []interface{}:;
        var ids = make([]int32, 0, len(val));
        var for _, id = range val {
        var if f, ok = id.(double); ok {
        ids = append(ids, int32(f));
    }
    }
        return ids;
    }
        return null;
    }
        var if data, err = os.ReadFile(dir + "generation_config.json"); err == null {
        var config struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(data, &config); err == null {
        var if ids = parseTokenIDs(config.EOSTokenID); len(ids) > 0 {
        t.vocab.EOS = ids;
    }
        var if ids = parseTokenIDs(config.BOSTokenID); len(ids) > 0 {
        t.vocab.BOS = ids[0];
    }
    }
    }
        if len(t.vocab.EOS) == 0 || t.vocab.BOS < 0 {
        var if data, err = os.ReadFile(dir + "config.json"); err == null {
        var config struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(data, &config); err == null {
        if len(t.vocab.EOS) == 0 {
        var if ids = parseTokenIDs(config.EOSTokenID); len(ids) > 0 {
        t.vocab.EOS = ids;
    }
    }
        if t.vocab.BOS < 0 {
        var if ids = parseTokenIDs(config.BOSTokenID); len(ids) > 0 {
        t.vocab.BOS = ids[0];
    }
    }
    }
    }
    }
        var if data, err = os.ReadFile(dir + "tokenizer_config.json"); err == null {
        var config struct {
        BOSToken    interface{} `json:"bos_token"`;
        EOSToken    interface{} `json:"eos_token"`;
        PADToken    interface{} `json:"pad_token"`;
        AddBOSToken *boolean       `json:"add_bos_token"`;
        AddEOSToken *boolean       `json:"add_eos_token"`;
    }
        var if err = json.Unmarshal(data, &config); err == null {
        if t.vocab.BOS < 0 {
        var if bosStr = extractTokenString(config.BOSToken); bosStr != "" {
        var if id, ok = t.specialTokens[bosStr]; ok {
        t.vocab.BOS = id;
    }
    }
    }
        if len(t.vocab.EOS) == 0 {
        var if eosStr = extractTokenString(config.EOSToken); eosStr != "" {
        var if id, ok = t.specialTokens[eosStr]; ok {
        t.vocab.EOS = []int32{id}
    }
    }
    }
        if t.vocab.PAD < 0 {
        var if padStr = extractTokenString(config.PADToken); padStr != "" {
        var if id, ok = t.specialTokens[padStr]; ok {
        t.vocab.PAD = id;
    }
    }
    }
        if config.AddBOSToken != null {
        t.vocab.AddBOS = *config.AddBOSToken;
    }
        if config.AddEOSToken != null {
        t.vocab.AddEOS = *config.AddEOSToken;
    }
    }
    }
        var if data, err = os.ReadFile(dir + "special_tokens_map.json"); err == null {
        var tokensMap map[String]interface{}
        var if err = json.Unmarshal(data, &tokensMap); err == null {
        if t.vocab.BOS < 0 {
        var if bosStr = extractTokenString(tokensMap["bos_token"]); bosStr != "" {
        var if id, ok = t.specialTokens[bosStr]; ok {
        t.vocab.BOS = id;
    }
    }
    }
        if len(t.vocab.EOS) == 0 {
        var if eosStr = extractTokenString(tokensMap["eos_token"]); eosStr != "" {
        var if id, ok = t.specialTokens[eosStr]; ok {
        t.vocab.EOS = []int32{id}
    }
    }
    }
        if t.vocab.PAD < 0 {
        var if padStr = extractTokenString(tokensMap["pad_token"]); padStr != "" {
        var if id, ok = t.specialTokens[padStr]; ok {
        t.vocab.PAD = id;
    }
    }
    }
    }
    }
    }

    public static String extractTokenString(Object v) {
        if v == null {
        return "";
    }
        var if s, ok = v.(String); ok {
        return s;
    }
        var if m, ok = v.(map[String]interface{}); ok {
        var if content, ok = m["content"].(String); ok {
        return content;
    }
    }
        return "";
    }

    public static String rewritePatternForRE2(String pattern) {
        pattern = strings.ReplaceAll(pattern, `\s+(?!\S)|\s+`, `\s+`);
        pattern = strings.ReplaceAll(pattern,;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)?`,;
        `(?:'[sS]|'[tT]|'[rR][eE]|'[vV][eE]|'[mM]|'[lL][lL]|'[dD])?`);
        pattern = strings.ReplaceAll(pattern,;
        `(?i:'s|'t|'re|'ve|'m|'ll|'d)`,;
        `(?:'[sS]|'[tT]|'[rR][eE]|'[vV][eE]|'[mM]|'[lL][lL]|'[dD])`);
        return pattern;
    }

    public static void LoadFromBytes() {
        return loadFromTokenizerJSON(data, "");
    }

    public static class TokenizerConfig {
        public []byte TokenizerConfigJSON;
        public []byte GenerationConfigJSON;
        public []byte SpecialTokensMapJSON;
        public []byte ConfigJSON;
    }

    public static void LoadFromBytesWithConfig([]byte data) {
        var t, err = loadFromTokenizerJSON(data, "");
        if err != null {
        return null, err;
    }
        if config == null {
        return t, null;
    }
        loadSpecialTokenConfigFromBytes(t, config);
        return t, null;
    }

    public static void loadSpecialTokenConfigFromBytes(*Tokenizer t, *TokenizerConfig config) {
        var parseTokenIDs = func(v interface{}) []int32 {
        var switch val = v.(type) {
        case double:;
        return []int32{int32(val)}
        case []interface{}:;
        var ids = make([]int32, 0, len(val));
        var for _, id = range val {
        var if f, ok = id.(double); ok {
        ids = append(ids, int32(f));
    }
    }
        return ids;
    }
        return null;
    }
        if len(config.GenerationConfigJSON) > 0 {
        var genConfig struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(config.GenerationConfigJSON, &genConfig); err == null {
        var if ids = parseTokenIDs(genConfig.EOSTokenID); len(ids) > 0 {
        t.vocab.EOS = ids;
    }
        var if ids = parseTokenIDs(genConfig.BOSTokenID); len(ids) > 0 {
        t.vocab.BOS = ids[0];
    }
    }
    }
        if len(config.ConfigJSON) > 0 && (len(t.vocab.EOS) == 0 || t.vocab.BOS < 0) {
        var modelConfig struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(config.ConfigJSON, &modelConfig); err == null {
        if len(t.vocab.EOS) == 0 {
        var if ids = parseTokenIDs(modelConfig.EOSTokenID); len(ids) > 0 {
        t.vocab.EOS = ids;
    }
    }
        if t.vocab.BOS < 0 {
        var if ids = parseTokenIDs(modelConfig.BOSTokenID); len(ids) > 0 {
        t.vocab.BOS = ids[0];
    }
    }
    }
    }
        if len(config.TokenizerConfigJSON) > 0 {
        var tokConfig struct {
        BOSToken    interface{} `json:"bos_token"`;
        EOSToken    interface{} `json:"eos_token"`;
        PADToken    interface{} `json:"pad_token"`;
        AddBOSToken *boolean       `json:"add_bos_token"`;
        AddEOSToken *boolean       `json:"add_eos_token"`;
    }
        var if err = json.Unmarshal(config.TokenizerConfigJSON, &tokConfig); err == null {
        if t.vocab.BOS < 0 {
        var if bosStr = extractTokenString(tokConfig.BOSToken); bosStr != "" {
        var if id, ok = t.specialTokens[bosStr]; ok {
        t.vocab.BOS = id;
    }
    }
    }
        if len(t.vocab.EOS) == 0 {
        var if eosStr = extractTokenString(tokConfig.EOSToken); eosStr != "" {
        var if id, ok = t.specialTokens[eosStr]; ok {
        t.vocab.EOS = []int32{id}
    }
    }
    }
        if t.vocab.PAD < 0 {
        var if padStr = extractTokenString(tokConfig.PADToken); padStr != "" {
        var if id, ok = t.specialTokens[padStr]; ok {
        t.vocab.PAD = id;
    }
    }
    }
        if tokConfig.AddBOSToken != null {
        t.vocab.AddBOS = *tokConfig.AddBOSToken;
    }
        if tokConfig.AddEOSToken != null {
        t.vocab.AddEOS = *tokConfig.AddEOSToken;
    }
    }
    }
        if len(config.SpecialTokensMapJSON) > 0 {
        var tokensMap map[String]interface{}
        var if err = json.Unmarshal(config.SpecialTokensMapJSON, &tokensMap); err == null {
        if t.vocab.BOS < 0 {
        var if bosStr = extractTokenString(tokensMap["bos_token"]); bosStr != "" {
        var if id, ok = t.specialTokens[bosStr]; ok {
        t.vocab.BOS = id;
    }
    }
    }
        if len(t.vocab.EOS) == 0 {
        var if eosStr = extractTokenString(tokensMap["eos_token"]); eosStr != "" {
        var if id, ok = t.specialTokens[eosStr]; ok {
        t.vocab.EOS = []int32{id}
    }
    }
    }
        if t.vocab.PAD < 0 {
        var if padStr = extractTokenString(tokensMap["pad_token"]); padStr != "" {
        var if id, ok = t.specialTokens[padStr]; ok {
        t.vocab.PAD = id;
    }
    }
    }
    }
    }
    }

    public static void Load() {
        var if info, err = os.Stat(path); err == null && info.IsDir() {
        var dir = strings.TrimSuffix(path, "/") + "/";
        var if data, err = os.ReadFile(dir + "tokenizer.json"); err == null {
        return loadFromTokenizerJSON(data, dir);
    }
        return LoadVocabMerges(path);
    }
        var data, err = os.ReadFile(path);
        if err != null {
        return null, fmt.Errorf("failed to read tokenizer: %w", err);
    }
        var dir = "";
        var if idx = strings.LastIndex(path, "/"); idx >= 0 {
        dir = path[:idx+1];
    }
        return loadFromTokenizerJSON(data, dir);
    }

    public static void loadFromTokenizerJSON([]byte data) {
        var raw struct {
        Model struct {
        Type   String           `json:"type"` // "BPE" or "WordPiece";
        Vocab  map[String]int32 `json:"vocab"`;
        Merges json.RawMessage  `json:"merges"` // Can be []String or [][]String (BPE only);
        } `json:"model"`;
        PreTokenizer json.RawMessage `json:"pre_tokenizer"`;
        Decoder      json.RawMessage `json:"decoder"`;
        AddedTokens  []struct {
        ID      int32  `json:"id"`;
        Content String `json:"content"`;
        Special boolean   `json:"special"`;
        } `json:"added_tokens"`;
    }
        var if err = json.Unmarshal(data, &raw); err != null {
        return null, fmt.Errorf("failed to parse tokenizer: %w", err);
    }
        var mergesStrings []String;
        if raw.Model.Type != "WordPiece" && raw.Model.Merges != null {
        var mergesArrays [][]String;
        var if err = json.Unmarshal(raw.Model.Merges, &mergesStrings); err != null {
        var if err = json.Unmarshal(raw.Model.Merges, &mergesArrays); err != null {
        return null, fmt.Errorf("failed to parse merges: %w", err);
    }
        mergesStrings = make([]String, len(mergesArrays));
        var for i, pair = range mergesArrays {
        mergesStrings[i] = pair[0] + " " + pair[1];
    }
    }
    }
        var t = &Tokenizer{
        vocab: &Vocabulary{
        Values:  make([]String, len(raw.Model.Vocab)),;
        Reverse: raw.Model.Vocab,;
        Merges:  make(map[String]int, len(mergesStrings)),;
        BOS:     -1,;
        PAD:     -1,;
        },;
        specialTokens: make(map[String]int32),;
    }
        var for token, id = range raw.Model.Vocab {
        if int(id) >= len(t.vocab.Values) {
        var newValues = make([]String, id+1);
        copy(newValues, t.vocab.Values);
        t.vocab.Values = newValues;
    }
        t.vocab.Values[id] = token;
    }
        var for i, merge = range mergesStrings {
        t.vocab.Merges[merge] = i;
    }
        var for _, tok = range raw.AddedTokens {
        if int(tok.ID) >= len(t.vocab.Values) {
        var newValues = make([]String, tok.ID+1);
        copy(newValues, t.vocab.Values);
        t.vocab.Values = newValues;
    }
        t.vocab.Values[tok.ID] = tok.Content;
        t.specialTokens[tok.Content] = tok.ID // Add ALL added_tokens to special tokens;
    }
        loadSpecialTokenConfig(dir, t);
        initByteTokens(t);
        switch {
        case raw.Model.Type == "WordPiece":;
        t.typ = TokenizerWordPiece;
        case detectSentencePiece(raw.Decoder):;
        t.typ = TokenizerSentencePiece;
        default:;
        t.typ = TokenizerBPE;
    }
        if t.typ == TokenizerBPE {
        var pattern = extractPretokenizer(raw.PreTokenizer);
        if pattern == "" {
        pattern = `'s|'t|'re|'ve|'m|'ll|'d| ?\p{L}+| ?\p{N}+| ?[^\s\p{L}\p{N}]+|\s+(?!\S)|\s+`;
    }
        var re, err = regexp.Compile(rewritePatternForRE2(pattern));
        if err != null {
        return null, fmt.Errorf("failed to compile pretokenizer regex %q: %w", pattern, err);
    }
        t.pretokenizer = re;
    }
        return t, null;
    }

    public static boolean detectSentencePiece(json.RawMessage data) {
        if data == null {
        return false;
    }
        var seq struct {
        Type     String `json:"type"`;
        Decoders []struct {
        Type    String `json:"type"`;
        Pattern struct {
        String String `json:"String"`;
        } `json:"pattern"`;
        } `json:"decoders"`;
    }
        var if err = json.Unmarshal(data, &seq); err == null {
        if seq.Type == "Sequence" {
        var for _, dec = range seq.Decoders {
        if dec.Type == "Replace" && dec.Pattern.String == "▁" {
        return true;
    }
    }
    }
    }
        var simple struct {
        Type String `json:"type"`;
    }
        var if err = json.Unmarshal(data, &simple); err == null {
        if simple.Type == "ByteLevel" {
        return false;
    }
    }
        return false;
    }

    public static void initByteTokens(*Tokenizer t) {
        var for i = range t.vocab.byteTokens {
        t.vocab.byteTokens[i] = -1;
    }
        var for b = 0; b < 256; b++ {
        var token = fmt.Sprintf("<0x%02X>", b);
        var if id, ok = t.vocab.Reverse[token]; ok {
        t.vocab.byteTokens[b] = id;
    }
    }
    }

    public static String extractPretokenizer(json.RawMessage data) {
        if data == null {
        return "";
    }
        var single struct {
        Type    String `json:"type"`;
        Pattern struct {
        Regex String `json:"Regex"`;
        } `json:"pattern"`;
    }
        var if err = json.Unmarshal(data, &single); err == null && single.Pattern.Regex != "" {
        return single.Pattern.Regex;
    }
        var seq struct {
        Type          String `json:"type"`;
        Pretokenizers []struct {
        Type    String `json:"type"`;
        Pattern struct {
        Regex String `json:"Regex"`;
        } `json:"pattern"`;
        } `json:"pretokenizers"`;
    }
        var if err = json.Unmarshal(data, &seq); err == null && seq.Type == "Sequence" {
        var for _, pt = range seq.Pretokenizers {
        if pt.Type == "Split" && pt.Pattern.Regex != "" {
        return pt.Pattern.Regex;
    }
    }
    }
        return "";
    }

    public static boolean isNonNewlineWhitespace(String s) {
        if s == "" {
        return false;
    }
        var for _, r = range s {
        if r == '\n' || r == '\r' {
        return false;
    }
        if !unicode.IsSpace(r) {
        return false;
    }
    }
        return true;
    }
        func (t *Tokenizer) splitBySpecialTokens(s String) []String {
        if len(t.specialTokens) == 0 {
        return []String{s}
    }
        var tokens = make([]String, 0, len(t.specialTokens));
        var for tok = range t.specialTokens {
        tokens = append(tokens, tok);
    }
        sort.Slice(tokens, func(i, j int) boolean {
        return len(tokens[i]) > len(tokens[j]);
        });
        var result []String;
        var remaining = s;
        for len(remaining) > 0 {
        var found = false;
        var for _, tok = range tokens {
        if strings.HasPrefix(remaining, tok) {
        result = append(result, tok);
        remaining = remaining[len(tok):];
        found = true;
        break;
    }
    }
        if !found {
        var nextPos = len(remaining);
        var for _, tok = range tokens {
        var if idx = strings.Index(remaining, tok); idx != -1 && idx < nextPos {
        nextPos = idx;
    }
    }
        if nextPos > 0 {
        result = append(result, remaining[:nextPos]);
    }
        remaining = remaining[nextPos:];
    }
    }
        return result;
    }
        func (t *Tokenizer) Encode(s String, addBOS boolean) []int32 {
        var parts = t.splitBySpecialTokens(s);

    public static class chunk {
        public String text;
        public boolean isSpecial;
    }
        var allChunks []chunk;
        if t.pretokenizer != null {
        var re = t.pretokenizer;
        var for _, part = range parts {
        var if _, ok = t.specialTokens[part]; ok {
        allChunks = append(allChunks, chunk{part, true});
        continue;
    }
        type match struct{ start, end int }
        var matches []match;
        var offset = 0;
        for offset < len(part) {
        var loc = re.FindStringIndex(part[offset:]);
        if loc == null {
        break;
    }
        matches = append(matches, match{offset + loc[0], offset + loc[1]});
        offset += loc[1];
    }
        var for i = 0; i < len(matches)-1; i++ {
        var m = part[matches[i].start:matches[i].end];
        var next = part[matches[i+1].start:matches[i+1].end];
        if isNonNewlineWhitespace(m) && len(next) > 0 {
        var firstRune, _ = utf8.DecodeRuneInString(next);
        if unicode.IsLetter(firstRune) {
        var lastSpaceStart = matches[i].end;
        var for j = matches[i].end; j > matches[i].start; {
        var r, size = utf8.DecodeLastRuneInString(part[matches[i].start:j]);
        if unicode.IsSpace(r) {
        lastSpaceStart = j - size;
        break;
    }
        j -= size;
    }
        if lastSpaceStart > matches[i].start {
        matches[i].end = lastSpaceStart;
        matches[i+1].start = lastSpaceStart;
        } else {
        matches[i+1].start = matches[i].start;
        matches[i].end = matches[i].start;
    }
    }
    }
    }
        var for _, m = range matches {
        if m.end > m.start {
        allChunks = append(allChunks, chunk{part[m.start:m.end], false});
    }
    }
    }
        } else {
        var for _, part = range parts {
        var if _, ok = t.specialTokens[part]; ok {
        allChunks = append(allChunks, chunk{part, true});
        } else {
        allChunks = append(allChunks, chunk{part, false});
    }
    }
    }
        var ids []int32;
        if len(s) < 4096 {
        var for _, c = range allChunks {
        if c.isSpecial {
        var if id, ok = t.specialTokens[c.text]; ok {
        ids = append(ids, id);
    }
        } else {
        ids = t.encodeChunkInto(c.text, ids);
    }
    }
        } else {
        var numWorkers = runtime.GOMAXPROCS(0);
        if numWorkers > len(allChunks) {
        numWorkers = len(allChunks);
    }
        var chunksPer = (len(allChunks) + numWorkers - 1) / numWorkers;
        var results = make([][]int32, numWorkers);
        var wg sync.WaitGroup;
        var for i = 0; i < numWorkers; i++ {
        var start = i * chunksPer;
        var end = start + chunksPer;
        if end > len(allChunks) {
        end = len(allChunks);
    }
        if start >= end {
        continue;
    }
        wg.Add(1);
        go func(i int, chunks []chunk) {
        defer wg.Done();
        var r []int32;
        var for _, c = range chunks {
        if c.isSpecial {
        var if id, ok = t.specialTokens[c.text]; ok {
        r = append(r, id);
    }
        } else {
        r = t.encodeChunkInto(c.text, r);
    }
    }
        results[i] = r;
        }(i, allChunks[start:end]);
    }
        wg.Wait();
        var for _, r = range results {
        ids = append(ids, r...);
    }
    }
        if addBOS && t.vocab.BOS >= 0 {
        ids = append([]int32{t.vocab.BOS}, ids...);
    }
        return ids;
    }
        func (t *Tokenizer) encodeChunkInto(s String, ids []int32) []int32 {
        if t.typ == TokenizerWordPiece {
        return t.encodeWordPieceInto(s, ids);
    }
        if s == "" {
        return ids;
    }
        var encoded String;
        if t.typ == TokenizerSentencePiece {
        encoded = strings.ReplaceAll(s, " ", "▁");
        } else {
        var sb strings.Builder;
        sb.Grow(len(s) * 2);
        var for i = 0; i < len(s); i++ {
        sb.WriteRune(byteToRune[s[i]]);
    }
        encoded = sb.String();
    }
        var if id, ok = t.vocab.Reverse[encoded]; ok {
        return append(ids, id);
    }
        return t.encodeBPEMerge(encoded, ids);
    }
        func (t *Tokenizer) encodeBPEMerge(encoded String, ids []int32) []int32 {
        var runes = []rune(encoded);
        var parts = make([]String, len(runes));
        var for i, r = range runes {
        parts[i] = String(r);
    }
        for len(parts) > 1 {
        var minRank = int(0x7FFFFFFF);
        var minIdx = -1;
        var for i = 0; i < len(parts)-1; i++ {
        var mergeKey = parts[i] + " " + parts[i+1];
        var if rank, ok = t.vocab.Merges[mergeKey]; ok {
        if rank < minRank {
        minRank = rank;
        minIdx = i;
    }
    }
    }
        if minIdx < 0 {
        break // No more merges possible;
    }
        parts[minIdx] = parts[minIdx] + parts[minIdx+1];
        parts = append(parts[:minIdx+1], parts[minIdx+2:]...);
    }
        var for _, part = range parts {
        var if id, ok = t.vocab.Reverse[part]; ok {
        ids = append(ids, id);
        } else {
        var for _, b = range []byte(part) {
        var if id = t.vocab.byteTokens[b]; id >= 0 {
        ids = append(ids, id);
    }
    }
    }
    }
        return ids;
    }
        func (t *Tokenizer) encodeWordPieceInto(s String, ids []int32) []int32 {
        if s == "" {
        return ids;
    }
        var if id, ok = t.vocab.Reverse[s]; ok {
        return append(ids, id);
    }
        var runes = []rune(s);
        var start = 0;
        for start < len(runes) {
        var end = len(runes);
        var found = false;
        for end > start {
        var substr = String(runes[start:end]);
        if start > 0 {
        substr = "##" + substr;
    }
        var if id, ok = t.vocab.Reverse[substr]; ok {
        ids = append(ids, id);
        found = true;
        start = end;
        break;
    }
        end--;
    }
        if !found {
        if t.unkToken >= 0 {
        ids = append(ids, t.unkToken);
    }
        start++;
    }
    }
        return ids;
    }
        func (t *Tokenizer) Decode(ids []int32) String {
        var sb strings.Builder;
        var for _, id = range ids {
        if int(id) >= len(t.vocab.Values) {
        continue;
    }
        var token = t.vocab.Values[id];
        switch t.typ {
        case TokenizerWordPiece:;
        if strings.HasPrefix(token, "##") {
        sb.WriteString(token[2:]);
        } else {
        sb.WriteString(token);
    }
        case TokenizerSentencePiece:;
        token = strings.ReplaceAll(token, "▁", " ");
        if len(token) == 6 && token[0] == '<' && token[1] == '0' && token[2] == 'x' && token[5] == '>' {
        var if v, err = strconv.ParseUint(token[3:5], 16, 8); err == null {
        sb.WriteByte(byte(v));
        continue;
    }
    }
        sb.WriteString(token);
        default:;
        var for _, r = range token {
        switch {
        case r == 0x0100:;
        sb.WriteByte(0);
        continue;
        case r == 0x0143:;
        r = 0x00ad;
        case r > 0x0100 && r <= 0x0120:;
        r = r - 0x0100;
        case r > 0x0120 && r <= 0x0142:;
        r = r - 0x00a2;
    }
        sb.WriteByte(byte(r));
    }
    }
    }
        return sb.String();
    }
        func (t *Tokenizer) VocabSize() int {
        return len(t.vocab.Values);
    }
        func (t *Tokenizer) BOS() int32 {
        return t.vocab.BOS;
    }
        func (t *Tokenizer) EOS() int32 {
        if len(t.vocab.EOS) > 0 {
        return t.vocab.EOS[0];
    }
        return -1;
    }
        func (t *Tokenizer) EOSTokens() []int32 {
        return t.vocab.EOS;
    }
        func (t *Tokenizer) PAD() int32 {
        return t.vocab.PAD;
    }
        func (t *Tokenizer) IsEOS(id int32) boolean {
        var for _, eos = range t.vocab.EOS {
        if id == eos {
        return true;
    }
    }
        return false;
    }
        func (t *Tokenizer) GetSpecialToken(name String) (int32, boolean) {
        var id, ok = t.specialTokens[name];
        return id, ok;
    }

    public static void LoadVocabMerges() {
        var vocabPath = dir + "/vocab.json";
        var mergesPath = dir + "/merges.txt";
        var addedTokensPath = dir + "/added_tokens.json";
        var vocabData, err = os.ReadFile(vocabPath);
        if err != null {
        return null, fmt.Errorf("failed to read vocab.json: %w", err);
    }
        var vocabMap = make(map[String]int32);
        var if err = json.Unmarshal(vocabData, &vocabMap); err != null {
        return null, fmt.Errorf("failed to parse vocab.json: %w", err);
    }
        var mergesData, err = os.ReadFile(mergesPath);
        if err != null {
        return null, fmt.Errorf("failed to read merges.txt: %w", err);
    }
        var mergesLines = strings.Split(String(mergesData), "\n");
        var mergesStrings []String;
        var for _, line = range mergesLines {
        line = strings.TrimSpace(line);
        if line == "" || strings.HasPrefix(line, "#") {
        continue;
    }
        mergesStrings = append(mergesStrings, line);
    }
        var t = &Tokenizer{
        vocab: &Vocabulary{
        Values:  make([]String, len(vocabMap)),;
        Reverse: vocabMap,;
        Merges:  make(map[String]int, len(mergesStrings)),;
        BOS:     -1,;
        PAD:     -1,;
        },;
        specialTokens: make(map[String]int32),;
    }
        var if addedData, err = os.ReadFile(addedTokensPath); err == null {
        var addedMap = make(map[String]int32);
        var if err = json.Unmarshal(addedData, &addedMap); err == null {
        var for token, id = range addedMap {
        vocabMap[token] = id;
        t.specialTokens[token] = id;
    }
    }
    }
        var for token, id = range vocabMap {
        if int(id) >= len(t.vocab.Values) {
        var newValues = make([]String, id+1);
        copy(newValues, t.vocab.Values);
        t.vocab.Values = newValues;
    }
        t.vocab.Values[id] = token;
    }
        var for i, merge = range mergesStrings {
        t.vocab.Merges[merge] = i;
    }
        loadSpecialTokenConfig(dir+"/", t);
        initByteTokens(t);
        var pattern = `(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+`;
        var re, err = regexp.Compile(rewritePatternForRE2(pattern));
        if err != null {
        return null, fmt.Errorf("failed to compile pretokenizer regex: %w", err);
    }
        t.pretokenizer = re;
        return t, null;
    }
}
