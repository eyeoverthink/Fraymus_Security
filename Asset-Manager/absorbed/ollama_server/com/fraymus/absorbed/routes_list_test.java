package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_list_test {
        "encoding/json";
        "net/http";
        "slices";
        "testing";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/api";
        );

    public static void TestList(*testing.T t) {
        gin.SetMode(gin.TestMode);
        t.Setenv("OLLAMA_MODELS", t.TempDir());
        var expectNames = []String{
        "mistral:7b-instruct-q4_0",;
        "zephyr:7b-beta-q5_K_M",;
        "apple/OpenELM:latest",;
        "boreas:2b-code-v1.5-q6_K",;
        "notus:7b-v1-IQ2_S",;
        "mynamespace/apeliotes:latest",;
        "myhost/mynamespace/lips:code",;
    }
        var s Server;
        var for _, n = range expectNames {
        var _, digest = createBinFile(t, null, null);
        createRequest(t, s.CreateHandler, api.CreateRequest{
        Name:  n,;
        Files: map[String]String{"test.gguf": digest},;
        });
    }
        var w = createRequest(t, s.ListHandler, null);
        if w.Code != http.StatusOK {
        t.Fatalf("expected status code 200, actual %d", w.Code);
    }
        var resp api.ListResponse;
        var if err = json.NewDecoder(w.Body).Decode(&resp); err != null {
        t.Fatal(err);
    }
        if len(resp.Models) != len(expectNames) {
        t.Fatalf("expected %d models, actual %d", len(expectNames), len(resp.Models));
    }
        var actualNames = make([]String, len(resp.Models));
        var for i, m = range resp.Models {
        actualNames[i] = m.Name;
    }
        slices.Sort(actualNames);
        slices.Sort(expectNames);
        if !slices.Equal(actualNames, expectNames) {
        t.Fatalf("expected slices to be equal %v", actualNames);
    }
    }
}
