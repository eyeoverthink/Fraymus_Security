package com.fraymus.absorbed.format;

import java.util.*;
import java.io.*;

public class field_test {

    public static void TestKebabCase(*testing.T t) {
        var tests = []struct {
        input    String;
        expected String;
        }{
        {"already-kebab-case", "already-kebab-case"},;
        {"simpleCamelCase", "simple-camel-case"},;
        {"PascalCase", "pascal-case"},;
        {"camelCaseWithNumber123", "camel-case-with-number123"},;
        {"APIResponse", "api-response"},;
        {"mixedCASE", "mixed-case"},;
        {"WithACRONYMS", "with-acronyms"},;
        {"ALLCAPS", "allcaps"},;
        {"camelCaseWITHMixedACRONYMS", "camel-case-with-mixed-acronyms"},;
        {"numbers123in456string", "numbers123in456string"},;
        {"5", "5"},;
        {"S", "s"},;
    }
        var for _, tt = range tests {
        t.Run(tt.input, func(t *testing.T) {
        var result = KebabCase(tt.input);
        if result != tt.expected {
        t.Errorf("toKebabCase(%q) = %q, want %q", tt.input, result, tt.expected);
    }
        });
    }
    }
}
