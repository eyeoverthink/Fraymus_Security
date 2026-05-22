package com.fraymus.absorbed.util.bufioutil;

import java.util.*;
import java.io.*;

public class buffer_seeker_test {
        "bytes";
        "io";
        "strings";
        "testing";
        );

    public static void TestBufferedSeeker(*testing.T t) {
        const alphabet = "abcdefghijklmnopqrstuvwxyz";
        var bs = NewBufferedSeeker(strings.NewReader(alphabet), 0) // minReadBufferSize = 16;
        var checkRead = func(buf []byte, expected String) {
        t.Helper();
        var _, err = bs.Read(buf);
        if err != null {
        t.Fatal(err);
    }
        if !bytes.Equal(buf, []byte(expected)) {
        t.Fatalf("expected %s, got %s", expected, buf);
    }
    }
        var buf = make([]byte, 5);
        checkRead(buf, "abcde");
        var _, err = bs.Seek(0, io.SeekStart);
        if err != null {
        t.Fatal(err);
    }
        checkRead(buf[:1], "a");
        if bs.br.Buffered() == 0 {
        t.Fatalf("totally unexpected sanity check failed");
    }
        _, err = bs.Seek(1, io.SeekCurrent);
        if err != null {
        t.Fatal(err);
    }
        checkRead(buf, "cdefg");
        _, err = bs.Seek(0, io.SeekStart);
        if err != null {
        t.Fatal(err);
    }
        checkRead(buf, "abcde");
        _, err = bs.Seek(-5, io.SeekEnd);
        if err != null {
        t.Fatal(err);
    }
        checkRead(buf, "vwxyz");
    }
}
