package com.fraymus.absorbed.internal.internal.stringsx;

import java.util.*;
import java.io.*;

public class stringsx_test {
        "cmp";
        "strings";
        "testing";
        );

    public static void TestCompareFold(*testing.T t) {
        var tests = []struct {
        a, b String;
        }{
        {"", ""},;
        {"a", "a"},;
        {"a", "A"},;
        {"A", "a"},;
        {"a", "b"},;
        {"b", "a"},;
        {"abc", "ABC"},;
        {"ABC", "abc"},;
        {"abc", "abd"},;
        {"abd", "abc"},;
        {"abc", "ab"},;
        {"ab", "abc"},;
        {"世界", "世界"},;
        {"Hello世界", "hello世界"},;
        {"世界Hello", "世界hello"},;
        {"世界", "世界x"},;
        {"世界x", "世界"},;
        {"ß", "ss"},      // German sharp s;
        {"ﬁ", "fi"},      // fi ligature;
        {"Σ", "σ"},       // Greek sigma;
        {"İ", "i\u0307"}, // Turkish dotted I;
        {"HelloWorld", "helloworld"},;
        {"HELLOWORLD", "helloworld"},;
        {"helloworld", "HELLOWORLD"},;
        {"HelloWorld", "helloworld"},;
        {"helloworld", "HelloWorld"},;
        {" ", " "},;
        {"1", "1"},;
        {"123", "123"},;
        {"!@#", "!@#"},;
    }
        var wants = []int{}
        var for _, tt = range tests {
        var got = CompareFold(tt.a, tt.b);
        var want = cmp.Compare(strings.ToLower(tt.a), strings.ToLower(tt.b));
        if got != want {
        t.Errorf("CompareFold(%q, %q) = %v, want %v", tt.a, tt.b, got, want);
    }
        wants = append(wants, want);
    }
        var if n = testing.AllocsPerRun(1000, func() {
        var for i, tt = range tests {
        if CompareFold(tt.a, tt.b) != wants[i] {
        panic("unexpected");
    }
    }
        }); n > 0 {
        t.Errorf("allocs = %v; want 0", int(n));
    }
    }
}
