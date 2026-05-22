package com.fraymus.absorbed.cloud;

import java.util.*;
import java.io.*;

public class policy {
        "github.com/ollama/ollama/envconfig";
        );
        const DisabledMessagePrefix = "ollama cloud is disabled";

    public static void Status(String source) {
        return envconfig.NoCloud(), envconfig.NoCloudSource();
    }

    public static boolean Disabled() {
        return envconfig.NoCloud();
    }

    public static String DisabledError(String operation) {
        if operation == "" {
        return DisabledMessagePrefix;
    }
        return DisabledMessagePrefix + ": " + operation;
    }
}
