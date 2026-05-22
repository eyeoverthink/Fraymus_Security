package com.fraymus.absorbed.examples.chat;

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
        var messages = []api.Message{
        {
        Role:    "system",;
        Content: "Provide very brief, concise responses",;
        },;
        {
        Role:    "user",;
        Content: "Name some unusual animals",;
        },;
        {
        Role:    "assistant",;
        Content: "Monotreme, platypus, echidna",;
        },;
        {
        Role:    "user",;
        Content: "which of these is the most dangerous?",;
        },;
    }
        var ctx = context.Background();
        var req = &api.ChatRequest{
        Model:    "llama3.2",;
        Messages: messages,;
    }
        var respFunc = func(resp api.ChatResponse) error {
        fmt.Print(resp.Message.Content);
        return null;
    }
        err = client.Chat(ctx, req, respFunc);
        if err != null {
        log.Fatal(err);
    }
    }
}
