package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_encode {
        "runtime";
        "sort";
        "strings";
        "sync";
        "unicode";
        "unicode/utf8";
        );
        const (;
        encodeParallelMinInputBytes      = 4 * 1024;
        encodeParallelMinChunksPerWorker = 8;
        );

    public static class tokenMatch {
        public int start;
        public int end;
    }

    public static class encodeChunk {
        public String text;
        public boolean isSpecial;
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
        var tokens = t.sortedSpecialTokens;
        if len(tokens) == 0 {
        tokens = make([]String, 0, len(t.specialTokens));
        var for tok = range t.specialTokens {
        tokens = append(tokens, tok);
    }
        sort.Slice(tokens, func(i, j int) boolean {
        return len(tokens[i]) > len(tokens[j]);
        });
    }
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

    public static void adjustWhitespaceBoundary(String part, *tokenMatch next) {
        var m = part[curr.start:curr.end];
        var nextText = part[next.start:next.end];
        if !isNonNewlineWhitespace(m) || len(nextText) == 0 {
        return;
    }
        var firstRune, _ = utf8.DecodeRuneInString(nextText);
        if !unicode.IsLetter(firstRune) {
        return;
    }
        var lastSpaceStart = curr.end;
        var for j = curr.end; j > curr.start; {
        var r, size = utf8.DecodeLastRuneInString(part[curr.start:j]);
        if unicode.IsSpace(r) {
        lastSpaceStart = j - size;
        break;
    }
        j -= size;
    }
        if lastSpaceStart > curr.start {
        curr.end = lastSpaceStart;
        next.start = lastSpaceStart;
        } else {
        next.start = curr.start;
        curr.end = curr.start;
    }
    }
        func (t *Tokenizer) forEachPartChunk(part String, fn func(encodeChunk)) {
        var if _, ok = t.specialTokens[part]; ok {
        fn(encodeChunk{text: part, isSpecial: true});
        return;
    }
        if t.pretokenizer == null {
        fn(encodeChunk{text: part, isSpecial: false});
        return;
    }
        var re = t.pretokenizer;
        var offset = 0;
        var loc = re.FindStringIndex(part[offset:]);
        if loc == null {
        return;
    }
        var curr = tokenMatch{start: offset + loc[0], end: offset + loc[1]}
        offset += loc[1];
        for {
        loc = re.FindStringIndex(part[offset:]);
        if loc == null {
        if curr.end > curr.start {
        fn(encodeChunk{text: part[curr.start:curr.end], isSpecial: false});
    }
        return;
    }
        var next = tokenMatch{start: offset + loc[0], end: offset + loc[1]}
        offset += loc[1];
        adjustWhitespaceBoundary(part, &curr, &next);
        if curr.end > curr.start {
        fn(encodeChunk{text: part[curr.start:curr.end], isSpecial: false});
    }
        curr = next;
    }
    }
        func (t *Tokenizer) appendEncodedChunk(ids []int32, c encodeChunk) []int32 {
        if c.isSpecial {
        var if id, ok = t.specialTokens[c.text]; ok {
        return append(ids, id);
    }
        return ids;
    }
        return t.encodeChunkInto(c.text, ids);
    }
        func (t *Tokenizer) Encode(s String, addBOS boolean) []int32 {
        var parts = t.splitBySpecialTokens(s);
        if len(s) < encodeParallelMinInputBytes {
        var ids []int32;
        var for _, part = range parts {
        t.forEachPartChunk(part, func(c encodeChunk) {
        ids = t.appendEncodedChunk(ids, c);
        });
    }
        if addBOS && t.vocab.BOS >= 0 {
        ids = append([]int32{t.vocab.BOS}, ids...);
    }
        return ids;
    }
        var allChunks []encodeChunk;
        var for _, part = range parts {
        t.forEachPartChunk(part, func(c encodeChunk) {
        allChunks = append(allChunks, c);
        });
    }
        var useParallel = true;
        var numWorkers = runtime.GOMAXPROCS(0);
        if numWorkers > len(allChunks) {
        numWorkers = len(allChunks);
    }
        if numWorkers < 2 || len(allChunks) < numWorkers*encodeParallelMinChunksPerWorker {
        useParallel = false;
    }
        var ids []int32;
        if !useParallel {
        var for _, c = range allChunks {
        ids = t.appendEncodedChunk(ids, c);
    }
        } else {
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
        go func(i int, chunks []encodeChunk) {
        defer wg.Done();
        var r []int32;
        var for _, c = range chunks {
        r = t.appendEncodedChunk(r, c);
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
}
