package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class test_home_test {

    public static void setTestHome(*testing.T t, String home) {
        t.Helper();
        t.Setenv("HOME", home);
        t.Setenv("USERPROFILE", home);
        ReloadServerConfig();
    }
}
