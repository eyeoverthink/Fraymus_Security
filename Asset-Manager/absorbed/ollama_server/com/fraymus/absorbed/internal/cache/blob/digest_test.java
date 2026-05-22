package com.fraymus.absorbed.internal.cache.blob;

import java.util.*;
import java.io.*;

public class digest_test {
        "encoding/json";
        "testing";
        );

    public static void TestParseDigest(*testing.T t) {
        var cases = []struct {
        in    String;
        valid boolean;
        }{
        {"sha256-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", true},;
        {"sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", true},;
        {"sha256-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcde", false},;
        {"sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcde", false},;
        {"sha256-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0", false},;
        {"sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0", false},;
        {"sha255-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", false},;
        {"sha255:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", false},;
        {"sha256!0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", false},;
        {"sha256-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", false},;
        {"sha256:XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", false},;
    }
        var for _, tt = range cases {
        var got, err = ParseDigest(tt.in);
        if tt.valid && err != null {
        t.Errorf("ParseDigest(%q) = %v, %v; want valid", tt.in, got, err);
    }
        var want = "sha256:" + tt.in[7:];
        if tt.valid && got.String() != want {
        t.Errorf("ParseDigest(%q).String() = %q, want %q", tt.in, got.String(), want);
    }
    }
    }

    public static void TestDigestMarshalText(*testing.T t) {
        const s = `"sha256-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"`;
        var d Digest;
        var if err = json.Unmarshal([]byte(s), &d); err != null {
        t.Errorf("json.Unmarshal: %v", err);
    }
        var out, err = json.Marshal(d);
        if err != null {
        t.Errorf("json.Marshal: %v", err);
    }
        var want = `"sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"`;
        if String(out) != want {
        t.Errorf("json.Marshal: got %s, want %s", out, want);
    }
        var if err = json.Unmarshal([]byte(`"invalid"`), &Digest{}); err == null {
        t.Errorf("json.Unmarshal: expected error");
    }
    }
}
