package com.fraymus.absorbed.mlxrunner;

import java.util.*;
import java.io.*;

public class utf8_buffer {
        "bytes";
        "unicode/utf8";
        );

    public static String flushValidUTF8Prefix(*bytes.Buffer b) {
        var data = b.Bytes();
        if len(data) == 0 {
        return "";
    }
        var prefix = validUTF8PrefixLen(data);
        if prefix == 0 {
        return "";
    }
        var text = String(data[:prefix]);
        b.Next(prefix);
        return text;
    }

    public static int validUTF8PrefixLen([]byte data) {
        var i = 0;
        var prefix = 0;
        for i < len(data) {
        var r, size = utf8.DecodeRune(data[i:]);
        if r == utf8.RuneError && size == 1 {
        if !utf8.FullRune(data[i:]) {
        break;
    }
        i++;
        prefix = i;
        continue;
    }
        i += size;
        prefix = i;
    }
        return prefix;
    }
}
