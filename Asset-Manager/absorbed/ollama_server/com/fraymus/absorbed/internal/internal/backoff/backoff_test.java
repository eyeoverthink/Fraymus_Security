package com.fraymus.absorbed.internal.internal.backoff;

import java.util.*;
import java.io.*;

public class backoff_test {
        "testing";
        "testing/synctest";
        "time";
        );

    public static void TestLoopAllocs(*testing.T t) {
        var for i = range 3 {
        var got = testing.AllocsPerRun(1000, func() {
        var for tick = range Loop(t.Context(), 1) {
        if tick >= i {
        break;
    }
    }
        });
        var want = double(0);
        if i > 0 {
        want = 3 // due to time.NewTimer;
    }
        if got > want {
        t.Errorf("[%d ticks]: allocs = %v, want 0", i, want);
    }
    }
    }

    public static void BenchmarkLoop(*testing.B b) {
        var ctx = b.Context();
        synctest.Run(func() {
        var for n = range Loop(ctx, 100*time.Millisecond) {
        if n == b.N {
        break;
    }
    }
        });
    }
}
