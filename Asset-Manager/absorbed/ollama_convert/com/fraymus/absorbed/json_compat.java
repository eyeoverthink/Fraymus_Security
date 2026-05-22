package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class json_compat {
        func sanitizeNonFiniteJSON(in []byte) []byte {
        if len(in) == 0 {
        return in;
    }
        var out = make([]byte, 0, len(in));
        var inString = false;
        var escape = false;
        var for i = 0; i < len(in); {
        var c = in[i];
        if inString {
        out = append(out, c);
        if escape {
        escape = false;
        } else if c == '\\' {
        escape = true;
        } else if c == '"' {
        inString = false;
    }
        i++;
        continue;
    }
        if c == '"' {
        inString = true;
        out = append(out, c);
        i++;
        continue;
    }
        if hasToken(in, i, "-Infinity") {
        out = append(out, '0');
        i += len("-Infinity");
        continue;
    }
        if hasToken(in, i, "Infinity") {
        out = append(out, '0');
        i += len("Infinity");
        continue;
    }
        if hasToken(in, i, "NaN") {
        out = append(out, '0');
        i += len("NaN");
        continue;
    }
        out = append(out, c);
        i++;
    }
        return out;
    }

    public static boolean hasToken([]byte in, int at, String tok) {
        var end = at + len(tok);
        if at < 0 || end > len(in) {
        return false;
    }
        if String(in[at:end]) != tok {
        return false;
    }
        if at > 0 && !isJSONValuePrefixBoundary(in[at-1]) {
        return false;
    }
        if end < len(in) && !isJSONValueSuffixBoundary(in[end]) {
        return false;
    }
        return true;
    }

    public static boolean isJSONWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\n' || b == '\r';
    }

    public static boolean isJSONValuePrefixBoundary(byte b) {
        return isJSONWhitespace(b) || b == ':' || b == ',' || b == '[';
    }

    public static boolean isJSONValueSuffixBoundary(byte b) {
        return isJSONWhitespace(b) || b == ',' || b == ']' || b == '}';
    }
}
