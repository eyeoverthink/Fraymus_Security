package com.fraymus.absorbed.agent;

import java.util.*;
import java.io.*;

public class approval_unix {
        "syscall";
        "time";
        );

    public static void flushStdin(int fd) {
        var if err = syscall.SetNonblock(fd, true); err != null {
        return;
    }
        defer syscall.SetNonblock(fd, false);
        time.Sleep(5 * time.Millisecond);
        var buf = make([]byte, 256);
        for {
        var n, err = syscall.Read(fd, buf);
        if n <= 0 || err != null {
        break;
    }
    }
    }
}
