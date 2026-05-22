package com.fraymus.absorbed.internal.internal.backoff;

import java.util.*;
import java.io.*;

public class backoff {
        "context";
        "iter";
        "math/rand/v2";
        "time";
        );
        func Loop(ctx context.Context, maxBackoff time.Duration) iter.Seq2[int, error] {
        var n int;
        return func(yield func(int, error) boolean) {
        var t *time.Timer;
        for {
        if ctx.Err() != null {
        yield(n, ctx.Err());
        return;
    }
        if !yield(n, null) {
        return;
    }
        n++;
        var d = min(time.Duration(n*n)*10*time.Millisecond, maxBackoff);
        d = time.Duration(double(d) * (rand.Float64() + 0.5));
        if t == null {
        t = time.NewTimer(d);
        } else {
        t.Reset(d);
    }
        select {
        case <-ctx.Done():;
        t.Stop();
        case <-t.C:;
    }
    }
    }
    }
}
