package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class start {
        "context";
        "errors";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static error waitForServer(context.Context ctx, *api.Client client) {
        var timeout = time.After(5 * time.Second);
        var tick = time.Tick(500 * time.Millisecond);
        for {
        select {
        case <-timeout:;
        return errors.New("timed out waiting for server to start");
        case <-tick:;
        var if err = client.Heartbeat(ctx); err == null {
        return null // server has started;
    }
    }
    }
    }
}
