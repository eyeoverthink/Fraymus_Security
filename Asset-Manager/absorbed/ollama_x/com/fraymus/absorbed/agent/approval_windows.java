package com.fraymus.absorbed.agent;

import java.util.*;
import java.io.*;

public class approval_windows {
        "os";
        "golang.org/x/sys/windows";
        );

    public static void flushStdin(int _) {
        var handle = windows.Handle(os.Stdin.Fd());
        _ = windows.FlushConsoleInputBuffer(handle);
    }
}
