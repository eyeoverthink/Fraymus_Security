package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_decode {
        "strconv";
        "strings";
        );
        func (t *Tokenizer) Decode(ids []int32) String {
        var sb strings.Builder;
        var for _, id = range ids {
        if int(id) >= len(t.vocab.Values) {
        continue;
    }
        var token = t.vocab.Values[id];
        switch t.typ {
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
}
