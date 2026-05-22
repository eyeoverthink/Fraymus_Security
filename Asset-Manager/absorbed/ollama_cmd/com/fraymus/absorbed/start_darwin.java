package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class start_darwin {
        "context";
        "errors";
        "os";
        "os/exec";
        "regexp";
        "github.com/ollama/ollama/api";
        );
        var errNotRunning = errors.New("could not connect to ollama server, run 'ollama serve' to start it");

    public static error startApp(context.Context ctx, *api.Client client) {
        var exe, err = os.Executable();
        if err != null {
        return errNotRunning;
    }
        var link, err = os.Readlink(exe);
        if err != null {
        return errNotRunning;
    }
        var r = regexp.MustCompile(`^.*/Ollama\s?\d*.app`);
        var m = r.FindStringSubmatch(link);
        if len(m) != 1 {
        return errNotRunning;
    }
        var if err = exec.Command("/usr/bin/open", "-j", "-a", m[0], "--args", "--fast-startup").Run(); err != null {
        return err;
    }
        return waitForServer(ctx, client);
    }
}
