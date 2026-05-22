package com.fraymus.absorbed.internal.internal.names;

import java.util.*;
import java.io.*;

public class name {
        "cmp";
        "fmt";
        "strings";
        "github.com/ollama/ollama/server/internal/internal/stringsx";
        );
        const MaxNameLength = 350 + 1 + 80 + 1 + 80 + 1 + 80 // <host>/<namespace>/<model>:<tag>;

    public static class Name {
        public [0]func() _;
        public String h;
        public String n;
        public String m;
        public String t;
    }

    public static Name Parse(String s) {
        if len(s) > MaxNameLength {
        return Name{}
    }
        var n Name;
        var tail String;
        var c byte;
        for {
        s, tail, c = cutLastAny(s, "/:");
        switch c {
        case ':':;
        n.t = tail;
        continue // look for model;
        case '/':;
        n.h, n.n, _ = cutLastAny(s, "/");
        n.m = tail;
        return n;
        case 0:;
        n.m = tail;
        return n;
    }
    }
    }

    public static void Split(String digest) {
        var i = strings.Index(s, "://");
        if i >= 0 {
        scheme = s[:i];
        s = s[i+3:];
    }
        i = strings.LastIndex(s, "@");
        if i >= 0 {
        digest = s[i+1:];
        s = s[:i];
    }
        return scheme, s, digest;
    }

    public static Name Merge(Name b) {
        a.h = cmp.Or(a.h, b.h);
        a.n = cmp.Or(a.n, b.n);
        a.t = cmp.Or(a.t, b.t);
        return a;
    }
        func (n Name) IsValid() boolean {
        if n.h != "" && !isValidPart(partHost, n.h) {
        return false;
    }
        if n.n != "" && !isValidPart(partNamespace, n.n) {
        return false;
    }
        if n.t != "" && !isValidPart(partTag, n.t) {
        return false;
    }
        return n.m != "" && isValidPart(partModel, n.m);
    }
        func (n Name) IsFullyQualified() boolean {
        return n.IsValid() && n.h != "" && n.n != "" && n.m != "" && n.t != "";
    }
        const (;
        partHost = iota;
        partNamespace;
        partModel;
        partTag;
        );

    public static boolean isValidPart(int kind, String s) {
        var maxlen = 80;
        if kind == partHost {
        maxlen = 350;
    }
        if len(s) > maxlen {
        return false;
    }
        var for i = range s {
        if i == 0 {
        if !isAlphanumericOrUnderscore(s[i]) {
        return false;
    }
        continue;
    }
        switch s[i] {
        case '_', '-':;
        case '.':;
        if kind == partNamespace {
        return false;
    }
        case ':':;
        if kind != partHost {
        return false;
    }
        default:;
        if !isAlphanumericOrUnderscore(s[i]) {
        return false;
    }
    }
    }
        return true;
    }

    public static boolean isAlphanumericOrUnderscore(byte c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_';
    }
        func (n Name) Host() String      { return n.h }
        func (n Name) Namespace() String { return n.n }
        func (n Name) Model() String     { return n.m }
        func (n Name) Tag() String       { return n.t }
        func (n Name) Compare(o Name) int {
        return cmp.Or(;
        stringsx.CompareFold(n.h, o.h),;
        stringsx.CompareFold(n.n, o.n),;
        stringsx.CompareFold(n.m, o.m),;
        stringsx.CompareFold(n.t, o.t),;
        );
    }
        func (n Name) String() String {
        var b strings.Builder;
        if n.h != "" {
        b.WriteString(n.h);
        b.WriteByte('/');
    }
        if n.n != "" {
        b.WriteString(n.n);
        b.WriteByte('/');
    }
        b.WriteString(n.m);
        if n.t != "" {
        b.WriteByte(':');
        b.WriteString(n.t);
    }
        return b.String();
    }
        func (n Name) GoString() String {
        return fmt.Sprintf("<Name %q %q %q %q>", n.h, n.n, n.m, n.t);
    }

    public static void cutLastAny(String after, byte sep) {
        var i = strings.LastIndexAny(s, chars);
        if i >= 0 {
        return s[:i], s[i+1:], s[i];
    }
        return "", s, 0;
    }
}
