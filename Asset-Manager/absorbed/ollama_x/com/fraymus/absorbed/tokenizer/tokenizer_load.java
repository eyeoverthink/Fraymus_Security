package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_load {
        "encoding/json";
        "fmt";
        "regexp";
        "sort";
        "strings";
        );

    public static class TokenizerConfig {
        public []byte TokenizerConfigJSON;
        public []byte GenerationConfigJSON;
        public []byte SpecialTokensMapJSON;
        public []byte ConfigJSON;
    }

    public static void LoadFromBytes() {
        return loadFromTokenizerJSON(data);
    }

    public static void LoadFromBytesWithConfig([]byte data) {
        var t, err = loadFromTokenizerJSON(data);
        if err != null {
        return null, err;
    }
        if config == null {
        return t, null;
    }
        loadSpecialTokenConfigFromBytes(t, config);
        return t, null;
    }

    public static void loadFromTokenizerJSON() {
        var raw struct {
        Model struct {
        Type   String           `json:"type"` // "BPE";
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
        if raw.Model.Type != "BPE" {
        return null, fmt.Errorf("unsupported tokenizer type: %s", raw.Model.Type);
    }
        var mergesStrings []String;
        if raw.Model.Merges != null {
        var mergesArrays [][]String;
        var if err = json.Unmarshal(raw.Model.Merges, &mergesStrings); err != null {
        var if err = json.Unmarshal(raw.Model.Merges, &mergesArrays); err != null {
        return null, fmt.Errorf("failed to parse merges: %w", err);
    }
        mergesStrings = make([]String, len(mergesArrays));
        var for i, pair = range mergesArrays {
        if len(pair) != 2 {
        return null, fmt.Errorf("failed to parse merges: expected merge pair of length 2, got %d", len(pair));
    }
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
        initByteTokens(t);
        switch {
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
        cacheSortedSpecialTokens(t);
        return t, null;
    }

    public static void cacheSortedSpecialTokens(*Tokenizer t) {
        if len(t.specialTokens) == 0 {
        t.sortedSpecialTokens = null;
        return;
    }
        var tokens = make([]String, 0, len(t.specialTokens));
        var for tok = range t.specialTokens {
        tokens = append(tokens, tok);
    }
        sort.Slice(tokens, func(i, j int) boolean {
        return len(tokens[i]) > len(tokens[j]);
        });
        t.sortedSpecialTokens = tokens;
    }

    public static class specialTokenConfigData {
        public []byte tokenizerConfigJSON;
        public []byte generationConfigJSON;
        public []byte specialTokensMapJSON;
        public []byte configJSON;
    }

    public static void applySpecialTokenConfig(*Tokenizer t, specialTokenConfigData config) {
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
        if len(config.generationConfigJSON) > 0 {
        var genConfig struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(config.generationConfigJSON, &genConfig); err == null {
        var if ids = parseTokenIDs(genConfig.EOSTokenID); len(ids) > 0 {
        t.vocab.EOS = ids;
    }
        var if ids = parseTokenIDs(genConfig.BOSTokenID); len(ids) > 0 {
        t.vocab.BOS = ids[0];
    }
    }
    }
        if len(config.configJSON) > 0 && (len(t.vocab.EOS) == 0 || t.vocab.BOS < 0) {
        var modelConfig struct {
        EOSTokenID interface{} `json:"eos_token_id"`;
        BOSTokenID interface{} `json:"bos_token_id"`;
    }
        var if err = json.Unmarshal(config.configJSON, &modelConfig); err == null {
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
        if len(config.tokenizerConfigJSON) > 0 {
        var tokConfig struct {
        BOSToken    interface{} `json:"bos_token"`;
        EOSToken    interface{} `json:"eos_token"`;
        PADToken    interface{} `json:"pad_token"`;
        AddBOSToken *boolean       `json:"add_bos_token"`;
        AddEOSToken *boolean       `json:"add_eos_token"`;
    }
        var if err = json.Unmarshal(config.tokenizerConfigJSON, &tokConfig); err == null {
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
        if len(config.specialTokensMapJSON) > 0 {
        var tokensMap map[String]interface{}
        var if err = json.Unmarshal(config.specialTokensMapJSON, &tokensMap); err == null {
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

    public static void loadSpecialTokenConfigFromBytes(*Tokenizer t, *TokenizerConfig config) {
        applySpecialTokenConfig(t, specialTokenConfigData{
        tokenizerConfigJSON:  config.TokenizerConfigJSON,;
        generationConfigJSON: config.GenerationConfigJSON,;
        specialTokensMapJSON: config.SpecialTokensMapJSON,;
        configJSON:           config.ConfigJSON,;
        });
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
}
