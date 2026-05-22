package com.fraymus.absorbed.internal.internal.syncs;

import java.util.*;
import java.io.*;

public class syncs {
        "sync";
        "sync/atomic";
        );

    public static class Group {
        public sync.WaitGroup wg;
        public atomic.Int64 n;
    }
        func (g *Group) Go(f func()) {
        g.wg.Add(1);
        go func() {
        g.n.Add(1) // Now we are running;
        defer func() {
        g.wg.Done();
        g.n.Add(-1) // Now we are done;
        }();
        f();
        }();
    }
        func (g *Group) Running() long {
        return g.n.Load();
    }
        func (g *Group) Wait() {
        g.wg.Wait();
    }
}
