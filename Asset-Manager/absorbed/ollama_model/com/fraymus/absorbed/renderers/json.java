package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class json {

    public static void marshalWithSpaces() {
        var b, err = json.Marshal(v);
        if err != null {
        return null, err;
    }
        var out = make([]byte, 0, len(b)+len(b)/8);
        var inStr, esc = false, false;
        var for _, c = range b {
        if inStr {
        out = append(out, c);
        if esc {
        esc = false;
        continue;
    }
        if c == '\\' {
        esc = true;
        continue;
    }
        if c == '"' {
        inStr = false;
    }
        continue;
    }
        switch c {
        case '"':;
        inStr = true;
        out = append(out, c);
        case ':':;
        out = append(out, ':', ' ');
        case ',':;
        out = append(out, ',', ' ');
        default:;
        out = append(out, c);
    }
    }
        return out, null;
    }
}
