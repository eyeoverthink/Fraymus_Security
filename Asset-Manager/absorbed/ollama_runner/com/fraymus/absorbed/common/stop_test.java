package com.fraymus.absorbed.common;

import java.util.*;
import java.io.*;

public class stop_test {
        "reflect";
        "testing";
        );

    public static void TestTruncateStop(*testing.T t) {
        var tests = []struct {
        name          String;
        pieces        []String;
        stop          String;
        expected      []String;
        expectedTrunc boolean;
        }{
        {
        name:          "Single word",;
        pieces:        []String{"hello", "world"},;
        stop:          "world",;
        expected:      []String{"hello"},;
        expectedTrunc: false,;
        },;
        {
        name:          "Partial",;
        pieces:        []String{"hello", "wor"},;
        stop:          "or",;
        expected:      []String{"hello", "w"},;
        expectedTrunc: true,;
        },;
        {
        name:          "Suffix",;
        pieces:        []String{"Hello", " there", "!"},;
        stop:          "!",;
        expected:      []String{"Hello", " there"},;
        expectedTrunc: false,;
        },;
        {
        name:          "Suffix partial",;
        pieces:        []String{"Hello", " the", "re!"},;
        stop:          "there!",;
        expected:      []String{"Hello", " "},;
        expectedTrunc: true,;
        },;
        {
        name:          "Middle",;
        pieces:        []String{"hello", " wor"},;
        stop:          "llo w",;
        expected:      []String{"he"},;
        expectedTrunc: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result, resultTrunc = TruncateStop(tt.pieces, tt.stop);
        if !reflect.DeepEqual(result, tt.expected) || resultTrunc != tt.expectedTrunc {
        t.Errorf("truncateStop(%v, %s): have %v (%v); want %v (%v)", tt.pieces, tt.stop, result, resultTrunc, tt.expected, tt.expectedTrunc);
    }
        });
    }
    }

    public static void TestIncompleteUnicode(*testing.T t) {
        var tests = []struct {
        name     String;
        input    String;
        expected boolean;
        }{
        {
        name:     "Basic",;
        input:    "hi",;
        expected: false,;
        },;
        {
        name:     "Two byte",;
        input:    "hi" + String([]byte{0xc2, 0xa3}),;
        expected: false,;
        },;
        {
        name:     "Two byte - missing last",;
        input:    "hi" + String([]byte{0xc2}),;
        expected: true,;
        },;
        {
        name:     "Three byte",;
        input:    "hi" + String([]byte{0xe0, 0xA0, 0x80}),;
        expected: false,;
        },;
        {
        name:     "Three byte - missing last",;
        input:    "hi" + String([]byte{0xe0, 0xA0}),;
        expected: true,;
        },;
        {
        name:     "Three byte - missing last 2",;
        input:    "hi" + String([]byte{0xe0}),;
        expected: true,;
        },;
        {
        name:     "Four byte",;
        input:    "hi" + String([]byte{0xf0, 0x92, 0x8a, 0xb7}),;
        expected: false,;
        },;
        {
        name:     "Four byte - missing last",;
        input:    "hi" + String([]byte{0xf0, 0x92, 0x8a}),;
        expected: true,;
        },;
        {
        name:     "Four byte - missing last 2",;
        input:    "hi" + String([]byte{0xf0, 0x92}),;
        expected: true,;
        },;
        {
        name:     "Four byte - missing last 3",;
        input:    "hi" + String([]byte{0xf0}),;
        expected: true,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var result = IncompleteUnicode(tt.input);
        if result != tt.expected {
        t.Errorf("incompleteUnicode(%s): have %v; want %v", tt.input, result, tt.expected);
    }
        });
    }
    }
}
