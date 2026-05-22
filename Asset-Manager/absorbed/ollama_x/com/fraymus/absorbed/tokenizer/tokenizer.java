package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer {
        type TokenizerType int;
        const (;
        TokenizerBPE           TokenizerType = iota // GPT-2 style byte-level BPE;
        TokenizerSentencePiece                      // SentencePiece with ▁ for spaces;
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
        public []String sortedSpecialTokens;
        public TokenizerType typ;
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
        func (t *Tokenizer) VocabSize() int {
        return len(t.vocab.Values);
    }
        func (t *Tokenizer) BOS() int32 {
        return t.vocab.BOS;
    }
        func (t *Tokenizer) AddBOS() boolean {
        return t.vocab.AddBOS;
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
}
