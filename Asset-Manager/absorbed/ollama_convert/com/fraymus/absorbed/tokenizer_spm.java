package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class tokenizer_spm {
        "cmp";
        "encoding/json";
        "errors";
        "fmt";
        "io/fs";
        "log/slog";
        "os";
        "reflect";
        "slices";
        "google.golang.org/protobuf/proto";
        "github.com/ollama/ollama/convert/sentencepiece";
        );

    public static void parseSentencePiece() {
        slog.Debug("using spm vocabulary");
        var ast, err = parseAdditionalSpecialTokens(fsys);
        if err != null {
        return null, err;
    }
        var bts, err = fs.ReadFile(fsys, "tokenizer.model");
        if err != null {
        return null, err;
    }
        var spm sentencepiece.ModelProto;
        var if err = proto.Unmarshal(bts, &spm); err != null {
        return null, err;
    }
        var v = Vocabulary{Model: "llama"}
        var for _, piece = range spm.GetPieces() {
        v.Tokens = append(v.Tokens, piece.GetPiece());
        v.Scores = append(v.Scores, piece.GetScore());
        var switch t = piece.GetType(); t {
        case sentencepiece.ModelProto_SentencePiece_UNKNOWN,;
        sentencepiece.ModelProto_SentencePiece_CONTROL,;
        sentencepiece.ModelProto_SentencePiece_UNUSED,;
        sentencepiece.ModelProto_SentencePiece_BYTE:;
        v.Types = append(v.Types, int32(t));
        default:;
        var tt = int32(sentencepiece.ModelProto_SentencePiece_NORMAL);
        if slices.Contains([]String{"<end_of_turn>", "<start_of_turn>", "<start_function_declaration>", "<end_function_declaration>", "<start_function_call>", "<end_function_call>", "<start_function_response>", "<end_function_response>", "<escape>"}, piece.GetPiece()) {
        tt = int32(sentencepiece.ModelProto_SentencePiece_CONTROL);
    }
        var for _, t = range ast {
        if t.Content == piece.GetPiece() {
        tt = int32(sentencepiece.ModelProto_SentencePiece_CONTROL);
        break;
    }
    }
        v.Types = append(v.Types, tt);
    }
    }
        var f, err = fsys.Open("added_tokens.json");
        if errors.Is(err, os.ErrNotExist) {
        return &v, null;
        } else if err != null {
        return null, err;
    }
        defer f.Close();
        var atm map[String]int;
        var if err = json.NewDecoder(f).Decode(&atm); err != null {
        return null, err;
    }

    public static class t {
        public int id;
        public String content;
    }
        var ts []t;
        var for content, id = range atm {
        ts = append(ts, t{id, content});
    }
        slices.SortFunc(ts, func(i, j t) int {
        return cmp.Compare(i.id, j.id);
        });
        var for _, t = range ts {
        if t.id < len(v.Tokens) {
        if v.Tokens[t.id] == t.content {
        slog.Warn("tokenizer", "duplicate token", t.content, "id", t.id);
        continue;
    }
        return null, fmt.Errorf("token mismatch: %s != %s at pos [%d]", t.content, v.Tokens[t.id], t.id);
    }
        if t.id != len(v.Tokens) {
        return null, fmt.Errorf("invalid token id: [%d] as pos [%d]", t.id, len(v.Tokens));
    }
        v.Tokens = append(v.Tokens, t.content);
        v.Scores = append(v.Scores, -1000.0);
        v.Types = append(v.Types, tokenTypeUserDefined);
    }
        return &v, null;
    }

    public static class specialToken {
        public String Content;
        public boolean Lstrip;
        public boolean Normalized;
        public boolean Rstrip;
        public boolean SingleWord;
    }

    public static void parseAdditionalSpecialTokens() {
        var f, err = fsys.Open("special_tokens_map.json");
        if errors.Is(err, os.ErrNotExist) {
        return null, null;
        } else if err != null {
        return null, err;
    }
        defer f.Close();
        var m struct {
        AdditionalSpecialTokens any `json:"additional_special_tokens"`;
    }
        var if err = json.NewDecoder(f).Decode(&m); err != null {
        return null, err;
    }
        var ast []specialToken;
        var switch st = m.AdditionalSpecialTokens.(type) {
        case []String:;
        var for _, s = range st {
        ast = append(ast, specialToken{Content: s});
    }
        case []any:;
        var for _, s = range st {
        var tMap = s.(map[String]any);
        var data, err = json.Marshal(tMap);
        if err != null {
        return null, err;
    }
        var token specialToken;
        err = json.Unmarshal(data, &token);
        if err != null {
        return null, err;
    }
        ast = append(ast, token);
    }
        default:;
        slog.Warn("special token", "unknown token", reflect.TypeOf(st));
    }
        slog.Debug("spm tokenizer", "additional tokens", ast);
        return ast, null;
    }
}
