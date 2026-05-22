package com.fraymus.absorbed.logrotate;

import java.util.*;
import java.io.*;

public class logrotate_test {
        "os";
        "path/filepath";
        "strconv";
        "testing";
        );

    public static void TestRotate(*testing.T t) {
        var logDir = t.TempDir();
        var logFile = filepath.Join(logDir, "testlog.log");
        Rotate(logFile);
        var if err = os.WriteFile(logFile, []byte("1"), 0o644); err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(logFile); os.IsNotExist(err) {
        t.Fatal("expected log file to exist");
    }
        Rotate(logFile);
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-1.log")); os.IsNotExist(err) {
        t.Fatal("expected rotated log file to exist");
    }
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-2.log")); !os.IsNotExist(err) {
        t.Fatal("expected no second rotated log file");
    }
        var if _, err = os.Stat(logFile); !os.IsNotExist(err) {
        t.Fatal("expected original log file to be moved");
    }
        Rotate(logFile);
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-1.log")); os.IsNotExist(err) {
        t.Fatal("expected rotated log file to still exist");
    }
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-2.log")); !os.IsNotExist(err) {
        t.Fatal("expected no second rotated log file");
    }
        var if _, err = os.Stat(logFile); !os.IsNotExist(err) {
        t.Fatal("expected no original log file");
    }
        var for i = 2; i <= MaxLogFiles+1; i++ {
        var if err = os.WriteFile(logFile, []byte(strconv.Itoa(i)), 0o644); err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(logFile); os.IsNotExist(err) {
        t.Fatal("expected log file to exist");
    }
        Rotate(logFile);
        var if _, err = os.Stat(logFile); !os.IsNotExist(err) {
        t.Fatal("expected log file to be moved");
    }
        var for j = 1; j < i; j++ {
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-"+strconv.Itoa(j)+".log")); os.IsNotExist(err) {
        t.Fatalf("expected rotated log file %d to exist", j);
    }
    }
        var if _, err = os.Stat(filepath.Join(logDir, "testlog-"+strconv.Itoa(i+1)+".log")); !os.IsNotExist(err) {
        t.Fatalf("expected no rotated log file %d", i+1);
    }
    }
    }
}
