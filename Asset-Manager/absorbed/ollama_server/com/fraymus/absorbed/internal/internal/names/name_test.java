package com.fraymus.absorbed.internal.internal.names;

import java.util.*;
import java.io.*;

public class name_test {
        "strings";
        "testing";
        );

    public static void TestParseName(*testing.T t) {
        var cases = []struct {
        in   String;
        want Name;
        }{
        {"", Name{}},;
        {"m:t", Name{m: "m", t: "t"}},;
        {"m", Name{m: "m"}},;
        {"/m", Name{m: "m"}},;
        {"/n/m:t", Name{n: "n", m: "m", t: "t"}},;
        {"n/m", Name{n: "n", m: "m"}},;
        {"n/m:t", Name{n: "n", m: "m", t: "t"}},;
        {"n/m", Name{n: "n", m: "m"}},;
        {"n/m", Name{n: "n", m: "m"}},;
        {strings.Repeat("m", MaxNameLength+1), Name{}},;
        {"h/n/m:t", Name{h: "h", n: "n", m: "m", t: "t"}},;
        {"ollama.com/library/_:latest", Name{h: "ollama.com", n: "library", m: "_", t: "latest"}},;
    }
        var for _, tt = range cases {
        t.Run(tt.in, func(t *testing.T) {
        var got = Parse(tt.in);
        if got.Compare(tt.want) != 0 {
        t.Errorf("parseName(%q) = %#v, want %q", tt.in, got, tt.want);
    }
        });
    }
    }

    public static void TestString(*testing.T t) {
        var cases = []String{
        "",;
        "m:t",;
        "m:t",;
        "m",;
        "n/m",;
        "n/m:t",;
        "n/m",;
        "n/m",;
        "h/n/m:t",;
        "ollama.com/library/_:latest",;
        "/m",;
        "/n/m:t",;
    }
        var for _, s = range cases {
        t.Run(s, func(t *testing.T) {
        s = strings.TrimPrefix(s, "/");
        var if g = Parse(s).String(); g != s {
        t.Errorf("parse(%q).String() = %q", s, g);
    }
        });
    }
    }

    public static void TestParseExtended(*testing.T t) {
        var cases = []struct {
        in String;
        wantScheme String;
        wantName   Name;
        wantDigest String;
        }{
        {"", "", Name{}, ""},;
        {"m", "", Name{m: "m"}, ""},;
        {"http://m", "http", Name{m: "m"}, ""},;
        {"http+insecure://m", "http+insecure", Name{m: "m"}, ""},;
        {"http://m@sha256:deadbeef", "http", Name{m: "m"}, "sha256:deadbeef"},;
    }
        var for _, tt = range cases {
        t.Run(tt.in, func(t *testing.T) {
        var scheme, name, digest = Split(tt.in);
        var n = Parse(name);
        if scheme != tt.wantScheme || n.Compare(tt.wantName) != 0 || digest != tt.wantDigest {
        t.Errorf("ParseExtended(%q) = %q, %#v, %q, want %q, %#v, %q", tt.in, scheme, name, digest, tt.wantScheme, tt.wantName, tt.wantDigest);
    }
        });
    }
    }

    public static void TestMerge(*testing.T t) {
        var cases = []struct {
        a, b String;
        want String;
        }{
        {"", "", ""},;
        {"m", "", "m"},;
        {"", "m", ""},;
        {"x", "y", "x"},;
        {"o.com/n/m:t", "o.com/n/m:t", "o.com/n/m:t"},;
        {"o.com/n/m:t", "o.com/n/_:t", "o.com/n/m:t"},;
        {"bmizerany/smol", "ollama.com/library/_:latest", "ollama.com/bmizerany/smol:latest"},;
        {"localhost:8080/bmizerany/smol", "ollama.com/library/_:latest", "localhost:8080/bmizerany/smol:latest"},;
    }
        var for _, tt = range cases {
        t.Run("", func(t *testing.T) {
        var a, b = Parse(tt.a), Parse(tt.b);
        var got = Merge(a, b);
        if got.Compare(Parse(tt.want)) != 0 {
        t.Errorf("merge(%q, %q) = %#v, want %q", tt.a, tt.b, got, tt.want);
    }
        });
    }
    }

    public static void TestParseStringRoundTrip(*testing.T t) {
        var cases = []String{
        "",;
        "m",;
        "m:t",;
        "n/m",;
        "n/m:t",;
        "n/m:t",;
        "n/m",;
        "n/m",;
        "h/n/m:t",;
        "ollama.com/library/_:latest",;
    }
        var for _, s = range cases {
        t.Run(s, func(t *testing.T) {
        var if got = Parse(s).String(); got != s {
        t.Errorf("parse(%q).String() = %q", s, got);
    }
        });
    }
    }
        var junkName Name;

    public static void BenchmarkParseName(*testing.B b) {
        b.ReportAllocs();
        for range b.N {
        junkName = Parse("h/n/m:t");
    }
    }
        const (;
        part80  = "88888888888888888888888888888888888888888888888888888888888888888888888888888888";
        part350 = "33333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333";
        );
        var testCases = map[String]boolean{ // name -> valid;
        "": false,;
        "_why/_the/_lucky:_stiff": true,;
        "h/n/m:t": true,;
        "host/namespace/model:tag": true,;
        "host/namespace/model":     true,;
        "namespace/model":          true,;
        "model":                    true,;
        part80 + "/" + part80 + "/" + part80 + ":" + part80:  true,;
        part350 + "/" + part80 + "/" + part80 + ":" + part80: true,;
        part80 + "/" + part80 + "/" + part80 + ":" + part350:       false,;
        "x" + part350 + "/" + part80 + "/" + part80 + ":" + part80: false,;
        "h/nn/mm:t": true, // bare minimum part sizes;
        "m":     true,;
        "n/m:":  true,;
        "h/n/m": true,;
        "@t":    false,;
        "m@d":   false,;
        "^":      false,;
        "mm:":    true,;
        "/nn/mm": true,;
        "//":     false, // empty model;
        "//mm":   true,;
        "hh//":   false, // empty model;
        "//mm:@": false,;
        "00@":    false,;
        "@":      false,;
        "-hh/nn/mm:tt": false,;
        "hh/-nn/mm:tt": false,;
        "hh/nn/-mm:tt": false,;
        "hh/nn/mm:-tt": false,;
        "-h": false,;
        "host:https/namespace/model:tag": true,;
        "host/name:space/model:tag": false,;
    }

    public static void TestParseNameValidation(*testing.T t) {
        var for s, valid = range testCases {
        var got = Parse(s);
        if got.IsValid() != valid {
        t.Logf("got: %v", got);
        t.Errorf("Parse(%q).IsValid() = %v; want !%[2]v", s, got.IsValid());
    }
    }
    }
}
