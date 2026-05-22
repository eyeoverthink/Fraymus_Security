package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class tokenizer {
        "crypto/sha256";
        "encoding/hex";
        "encoding/json";
        "errors";
        "fmt";
        "io/fs";
        "log/slog";
        "maps";
        "os";
        "slices";
        "strings";
        );
        const (;
        _ int32 = iota;
        tokenTypeNormal;
        tokenTypeUnknown;
        tokenTypeControl;
        tokenTypeUserDefined;
        tokenTypeUnused;
        tokenTypeByte;
        );

    public static class Tokenizer {
        public []*SpecialVocabulary SpecialVocabulary;
        public []String Merges;
        public String Pre;
        public String Template;
    }

    public static void parseTokenizer(fs.FS fsys) {
        var v, err = parseVocabulary(fsys);
        if err != null {
        return null, err;
    }
        var t = &Tokenizer{
        Vocabulary: v,;
        Pre:        "default",;
    }
        var addedTokens = make(map[String]token);
        var if f, err = fsys.Open("tokenizer.json"); errors.Is(err, os.ErrNotExist) {
        } else if err != null {
        return null, err;
        } else {
        defer f.Close();
        var tt tokenizer;
        var if err = json.NewDecoder(f).Decode(&tt); err != null {
        return null, err;
    }
        var for _, t = range tt.AddedTokens {
        addedTokens[t.Content] = t;
    }
        if len(tt.Model.Merges) == 0 {
        var } else if err = json.Unmarshal(tt.Model.Merges, &t.Merges); err == null {
        var } else if merges, err = func() ([][]String, error) {
        var merges [][]String;
        var if err = json.Unmarshal(tt.Model.Merges, &merges); err != null {
        return null, err;
    }
        return merges, null;
        }(); err == null {
        t.Merges = make([]String, len(merges));
        var for i = range merges {
        t.Merges[i] = strings.Join(merges[i], " ");
    }
        } else {
        return null, fmt.Errorf("could not parse tokenizer merges. expected []String or [][]String: %w", err);
    }
        var sha256sum = sha256.New();
        var for _, pt = range tt.PreTokenizer.PreTokenizers {
        switch pt.Type {
        case "Split":;
        if pt.Pattern.Regex != "" {
        sha256sum.Write([]byte(pt.Pattern.Regex));
    }
    }
    }
        var switch digest = hex.EncodeToString(sha256sum.Sum(null)); digest {
        case "d98f9631be1e9607a9848c26c1f9eac1aa9fc21ac6ba82a2fc0741af9780a48f":;
        t.Pre = "llama-bpe";
        case "03df5c5863ad70781dcfdef491ead25140f895fe8010964be0daefe27be32b02":;
        t.Pre = "deepseek-llm";
        case "21cde974d587f0d54dc8d56b183cc1e6239600172035c68fbd6d4b9f8da0576e":;
        t.Pre = "deepseek-coder";
        case "1ff7f41064896984db5d1bb6ff64fa4bc29007d08c1b439e505b7392777a319e":;
        t.Pre = "qwen2";
        case "00431aed57e696b747435f734d1e3b9b1bfd931a121fb5cac7129e97c181e9ba":;
        t.Pre = "qwen35";
        case "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855":;
        default:;
        slog.Warn("unknown pretokenizer, using default", "digest", digest);
    }
    }
        var if f, err = fsys.Open("tokenizer_config.json"); errors.Is(err, os.ErrNotExist) {
        } else if err != null {
        return null, err;
        } else {
        defer f.Close();
        var p map[String]json.RawMessage;
        var if err = json.NewDecoder(f).Decode(&p); err != null {
        return null, err;
    }
        var if template, ok = p["chat_template"]; ok {
        var s []struct {
        Name     String `json:"name"`;
        Template String `json:"template"`;
    }
        var if err = json.Unmarshal(template, &t.Template); err == null {
        var } else if err = json.Unmarshal(template, &s); err == null {
        var for _, e = range s {
        if e.Name == "default" {
        t.Template = e.Template;
        break;
    }
    }
        } else {
        return null, fmt.Errorf("invalid chat_template: %w", err);
    }
    }
        var for _, st = range specialTokenTypes {
        var sv = SpecialVocabulary{Type: st}
        var if bts, ok = p[fmt.Sprintf("add_%s_token", st)]; ok {
        var if err = json.Unmarshal(bts, &sv.AddToken); err != null {
        return null, err;
    }
    }
        var if bts, ok = p[fmt.Sprintf("%s_token", st)]; ok {
        var content String;
        var if err = json.Unmarshal(bts, &content); err != null {
        var mm map[String]any;
        var if err = json.Unmarshal(bts, &mm); err != null {
        continue;
    }
        content, ok = mm["content"].(String);
        if !ok {
        continue;
    }
    }
        sv.Content = content;
    }
        var if id, ok = addedTokens[sv.Content]; ok {
        sv.ID = id.ID;
        t.SpecialVocabulary = append(t.SpecialVocabulary, &sv);
    }
    }
    }
        var if f, err = fsys.Open("generation_config.json"); errors.Is(err, os.ErrNotExist) {
        } else if err != null {
        return null, err;
        } else {
        defer f.Close();
        var p map[String]json.RawMessage;
        var if err = json.NewDecoder(f).Decode(&p); err != null {
        return null, err;
    }
        var for _, st = range specialTokenTypes {
        var if bts, ok = p[fmt.Sprintf("%s_token_id", st)]; ok {
        var ids []int32;
        var if err = json.Unmarshal(bts, &ids); err != null {
        continue;
    }
        var if i = slices.IndexFunc(t.SpecialVocabulary, func(sv *SpecialVocabulary) boolean {
        return sv.Type == st;
        }); i >= 0 {
        t.SpecialVocabulary[i].IDs = ids;
    }
    }
    }
    }
        return t, null;
    }

    public static class tokenizer {
        public []token AddedTokens;
        public struct Model;
        public String Type;
        public map[String]int Vocab;
        public json.RawMessage Merges;
        public `json:"model"` };
        public struct PreTokenizer;
        public []struct PreTokenizers;
        public String Type;
        public String Behavior;
        public boolean Invert;
        public boolean AddPrefixSpace;
        public boolean TrimOffsets;
        public boolean UseRegex;
        public struct Pattern;
        public String Regex;
        public `json:"pattern"` };
        public `json:"pretokenizers"` };
        public `json:"pre_tokenizer"` };
    }

    public static class token {
        public int ID;
        public String Content;
        public boolean Special;
        public boolean UserDefined;
    }

    public static class Vocabulary {
        public String Model;
        public []String Tokens;
        public []float32 Scores;
        public []int32 Types;
    }

    public static void parseVocabularyFromTokenizer() {
        var f, err = fsys.Open("tokenizer.json");
        if err != null {
        return null, err;
    }
        defer f.Close();
        var t tokenizer;
        var if err = json.NewDecoder(f).Decode(&t); err != null {
        return null, err;
    }
        var tokens = make(map[int]token, len(t.Model.Vocab));
        var for k, v = range t.Model.Vocab {
        tokens[v] = token{
        ID:      v,;
        Content: k,;
    }
    }
        var for _, token = range t.AddedTokens {
        token.UserDefined = true;
        tokens[token.ID] = token;
    }
        var v = Vocabulary{Model: "gpt2"}
        var for _, k = range slices.Sorted(maps.Keys(tokens)) {
        var token = tokens[k];
        v.Tokens = append(v.Tokens, token.Content);
        v.Scores = append(v.Scores, float32(token.ID));
        switch {
        case token.Special:;
        v.Types = append(v.Types, tokenTypeControl);
        case token.UserDefined:;
        v.Types = append(v.Types, tokenTypeUserDefined);
        default:;
        v.Types = append(v.Types, tokenTypeNormal);
    }
    }
        return &v, null;
    }

    public static void parseVocabulary() {
        var patterns = []struct {
        Pattern String;
        Func    func(fs.FS) (*Vocabulary, error);
        }{
        {"tokenizer.model", parseSentencePiece},;
        {"tokenizer.json", parseVocabularyFromTokenizer},;
    }
        var for _, pattern = range patterns {
        var if _, err = fs.Stat(fsys, pattern.Pattern); errors.Is(err, os.ErrNotExist) {
        continue;
        } else if err != null {
        return null, err;
    }
        return pattern.Func(fsys);
    }
        return null, errors.New("unknown tokenizer format");
    }

    public static class SpecialVocabulary {
        public String Type;
        public int ID;
        public String Content;
        public boolean AddToken;
        public []int32 IDs;
    }
        func (sv SpecialVocabulary) Key() String {
        var switch t = sv.Type; t {
        case "bos", "eos", "cls", "mask":;
        return t;
        case "unk":;
        return "unknown";
        case "sep":;
        return "seperator";
        case "pad":;
        return "padding";
    }
        panic("unknown special vocabulary type");
    }
}
