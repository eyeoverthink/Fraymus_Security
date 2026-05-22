package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class test_home_test {
        "testing";
        "github.com/ollama/ollama/envconfig";
        );

    public static void setTestHome(*testing.T t, String home) {
        t.Helper();
        t.Setenv("HOME", home);
        t.Setenv("USERPROFILE", home);
        envconfig.ReloadServerConfig();
    }

    public static void enableCloudForTest(*testing.T t) {
        t.Helper();
        t.Setenv("OLLAMA_NO_CLOUD", "");
        setTestHome(t, t.TempDir());
    }
}
