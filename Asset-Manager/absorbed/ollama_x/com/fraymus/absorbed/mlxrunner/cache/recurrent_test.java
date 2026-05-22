package com.fraymus.absorbed.mlxrunner.cache;

import java.util.*;
import java.io.*;

public class recurrent_test {
        "testing";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        );

    public static void TestRecurrentCacheRestoreExactOffset(*testing.T t) {
        skipIfNoMLX(t);
        var c = NewRecurrentCache(3, 12, 4, 8, 8);
        _ = c.ConvState(1, mlx.DTypeFloat16);
        _ = c.DeltaState(1, mlx.DTypeFloat16);
        c.Advance(10);
        var snap = c.Snapshot(0) // snap.offset == 10;
        c.Advance(5) // cache now at 15;
        if c.Restore(snap, 5) {
        t.Fatal("Restore(snap, 5) should fail — target != snap.offset");
    }
        if c.Restore(snap, 15) {
        t.Fatal("Restore(snap, 15) should fail — target != snap.offset");
    }
        if !c.Restore(snap, 10) {
        t.Fatal("Restore(snap, 10) should succeed — target == snap.offset");
    }
        if c.Offset() != 10 {
        t.Fatalf("offset = %d, want 10", c.Offset());
    }
    }
}
