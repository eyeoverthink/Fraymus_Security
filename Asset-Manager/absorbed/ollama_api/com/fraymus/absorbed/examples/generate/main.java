package com.fraymus.absorbed.examples.generate;

import java.util.*;
import java.io.*;

public class main {
        "context";
        "fmt";
        "log";
        "github.com/ollama/ollama/api";
        );

    public static void main(String[] args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        log.Fatal(err);
    }
        var req = &api.GenerateRequest{
        Model:  "gemma2",;
        Prompt: "how many planets are there?",;
        Stream: new(boolean),;
    }
        var ctx = context.Background();
        var respFunc = func(resp api.GenerateResponse) error {
        System.out.println(resp.Response);
        return null;
    }
        err = client.Generate(ctx, req, respFunc);
        if err != null {
        log.Fatal(err);
    }
    }
}
