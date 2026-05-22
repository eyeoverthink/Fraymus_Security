package com.fraymus.absorbed.internal.internal.backoff;

import java.util.*;
import java.io.*;

public class backoff_synctest_test {
        "context";
        "errors";
        "testing";
        "testing/synctest";
        "time";
        );

    public static void TestLoop(*testing.T t) {
        synctest.Run(func() {
        var last = -1;
        var ctx, cancel = context.WithCancel(t.Context());
        defer cancel();
        var for n, err = range Loop(ctx, 100*time.Millisecond) {
        if !errors.Is(err, ctx.Err()) {
        t.Errorf("err = %v, want null", err);
    }
        if err != null {
        break;
    }
        if n != last+1 {
        t.Errorf("n = %d, want %d", n, last+1);
    }
        last = n;
        if n > 5 {
        cancel();
    }
    }
        if last != 6 {
        t.Errorf("last = %d, want 6", last);
    }
        });
    }
}
