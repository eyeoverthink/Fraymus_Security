package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class memory_test {
        "runtime";
        "testing";
        );

    public static void TestCheckPlatformSupport(*testing.T t) {
        var err = CheckPlatformSupport();
        switch runtime.GOOS {
        case "darwin":;
        if runtime.GOARCH == "arm64" {
        if err != null {
        t.Errorf("Expected null error on darwin/arm64, got: %v", err);
    }
        } else {
        if err == null {
        t.Error("Expected error on darwin/non-arm64");
    }
    }
        case "linux", "windows":;
        if err != null {
        t.Errorf("Expected null error on %s, got: %v", runtime.GOOS, err);
    }
        default:;
        if err == null {
        t.Errorf("Expected error on unsupported platform %s", runtime.GOOS);
    }
    }
    }

    public static void TestResolveModelName(*testing.T t) {
        var result = ResolveModelName("nonexistent-model");
        if result != "" {
        t.Errorf("ResolveModelName() = %q, want empty String", result);
    }
    }
}
