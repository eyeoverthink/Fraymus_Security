package com.fraymus.absorbed.tokenizer;

import java.util.*;
import java.io.*;

public class tokenizer_load_test {
        "strings";
        "testing";
        );

    public static void TestLoadFromBytesRejectsWordPiece(*testing.T t) {
        var data = []byte(`{
        "model": {
        "type": "WordPiece",;
        "vocab": {"[UNK]": 0, "hello": 1}
        },;
        "added_tokens": [];
        }`);
        var _, err = LoadFromBytes(data);
        if err == null {
        t.Fatal("expected WordPiece load to fail");
    }
        if !strings.Contains(err.Error(), "unsupported tokenizer type: WordPiece") {
        t.Fatalf("unexpected error: %v", err);
    }
    }
}
