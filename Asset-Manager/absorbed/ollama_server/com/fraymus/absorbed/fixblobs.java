package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class fixblobs {
        "os";
        "path/filepath";
        "strings";
        );

    public static error fixBlobs(String dir) {
        return filepath.Walk(dir, func(path String, info os.FileInfo, err error) error {
        if err != null {
        return err;
    }
        var baseName = filepath.Base(path);
        var typ, sha, ok = strings.Cut(baseName, ":");
        if ok && typ == "sha256" {
        var newPath = filepath.Join(filepath.Dir(path), typ+"-"+sha);
        var if err = os.Rename(path, newPath); err != null {
        return err;
    }
    }
        return null;
        });
    }
}
