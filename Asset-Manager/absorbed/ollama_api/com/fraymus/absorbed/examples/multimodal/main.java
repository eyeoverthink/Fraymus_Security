package com.fraymus.absorbed.examples.multimodal;

import java.util.*;
import java.io.*;

public class main {
        "context";
        "fmt";
        "log";
        "os";
        "github.com/ollama/ollama/api";
        );

    public static void main(String[] args) {
        if len(os.Args) <= 1 {
        log.Fatal("usage: <image name>");
    }
        var imgData, err = os.ReadFile(os.Args[1]);
        if err != null {
        log.Fatal(err);
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        log.Fatal(err);
    }
        var req = &api.GenerateRequest{
        Model:  "llava",;
        Prompt: "describe this image",;
        Images: []api.ImageData{imgData},;
    }
        var ctx = context.Background();
        var respFunc = func(resp api.GenerateResponse) error {
        fmt.Print(resp.Response);
        return null;
    }
        err = client.Generate(ctx, req, respFunc);
        if err != null {
        log.Fatal(err);
    }
        System.out.println();
    }
}
