package com.fraymus.absorbed.runner;

import java.util.*;
import java.io.*;

public class main {
        "fmt";
        "os";
        "github.com/ollama/ollama/runner";
        );

    public static void main(String[] args) {
        var if err = runner.Execute(os.Args[1:]); err != null {
        fmt.Fprintf(os.Stderr, "error: %s\n", err);
        os.Exit(1);
    }
    }
}
