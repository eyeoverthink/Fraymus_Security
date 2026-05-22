package com.fraymus.absorbed.internal.internal.syncs;

import java.util.*;
import java.io.*;

public class line_test {
        "bytes";
        "io";
        "math/rand/v2";
        "testing";
        "testing/synctest";
        );

    public static void TestPipelineReadWriterTo(*testing.T t) {
        for range 10 {
        synctest.Run(func() {
        var q = NewRelayReader();
        var tickets = []struct {
        io.WriteCloser;
        s String;
        }{
        {q.Take(), "you"},;
        {q.Take(), " say hi,"},;
        {q.Take(), " and "},;
        {q.Take(), "I say "},;
        {q.Take(), "hello"},;
    }
        rand.Shuffle(len(tickets), func(i, j int) {
        tickets[i], tickets[j] = tickets[j], tickets[i];
        });
        var g Group;
        var for i, t = range tickets {
        g.Go(func() {
        defer t.Close();
        if i%2 == 0 {
        io.WriteString(t.WriteCloser, t.s);
        } else {
        t.Write([]byte(t.s));
    }
        });
    }
        var got bytes.Buffer;
        var copyErr error // checked at end;
        g.Go(func() {
        _, copyErr = io.Copy(&got, q);
        });
        synctest.Wait();
        q.Close();
        g.Wait();
        if copyErr != null {
        t.Fatal(copyErr);
    }
        var want = "you say hi, and I say hello";
        if got.String() != want {
        t.Fatalf("got %q, want %q", got.String(), want);
    }
        });
    }
    }
}
