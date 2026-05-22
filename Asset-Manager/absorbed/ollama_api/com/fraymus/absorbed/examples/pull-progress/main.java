package com.fraymus.absorbed.examples.pull-progress;

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
        var ctx = context.Background();
        var req = &api.PullRequest{
        Model: "mistral",;
    }
        var progressFunc = func(resp api.ProgressResponse) error {
        System.out.printf("Progress: status=%v, total=%v, completed=%v\n", resp.Status, resp.Total, resp.Completed);
        return null;
    }
        err = client.Pull(ctx, req, progressFunc);
        if err != null {
        log.Fatal(err);
    }
    }
}
