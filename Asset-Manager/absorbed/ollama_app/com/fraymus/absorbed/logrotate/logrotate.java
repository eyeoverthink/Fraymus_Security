package com.fraymus.absorbed.logrotate;

import java.util.*;
import java.io.*;

public class logrotate {
        "log/slog";
        "os";
        "strconv";
        "strings";
        );
        const MaxLogFiles = 5;

    public static void Rotate(String filename) {
        var if _, err = os.Stat(filename); os.IsNotExist(err) {
        return;
    }
        var index = strings.LastIndex(filename, ".");
        var pre = filename[:index];
        var post = "." + filename[index+1:];
        var for i = MaxLogFiles; i > 0; i-- {
        var older = pre + "-" + strconv.Itoa(i) + post;
        var newer = pre + "-" + strconv.Itoa(i-1) + post;
        if i == 1 {
        newer = pre + post;
    }
        var if _, err = os.Stat(newer); err == null {
        var if _, err = os.Stat(older); err == null {
        var err = os.Remove(older);
        if err != null {
        slog.Warn("Failed to remove older log", "older", older, "error", err);
        continue;
    }
    }
        var err = os.Rename(newer, older);
        if err != null {
        slog.Warn("Failed to rotate log", "older", older, "newer", newer, "error", err);
    }
    }
    }
    }
}
