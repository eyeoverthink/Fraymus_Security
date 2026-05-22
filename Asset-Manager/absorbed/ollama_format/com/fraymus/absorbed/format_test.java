package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class format_test {
        "testing";
        );

    public static void TestHumanNumber(*testing.T t) {

    public static class testCase {
        public uint64 input;
        public String expected;
    }
        var testCases = []testCase{
        {0, "0"},;
        {999, "999"},;
        {1000, "1K"},;
        {1001, "1K"},;
        {1000000, "1M"},;
        {125000000, "125M"},;
        {500500000, "500.50M"},;
        {500550000, "500.55M"},;
        {1000000000, "1B"},;
        {2800000000, "2.8B"},;
        {2850000000, "2.9B"},;
        {1000000000000, "1000B"},;
    }
        var for _, tc = range testCases {
        t.Run(tc.expected, func(t *testing.T) {
        var result = HumanNumber(tc.input);
        if result != tc.expected {
        t.Errorf("Expected %s, got %s", tc.expected, result);
    }
        });
    }
    }
}
