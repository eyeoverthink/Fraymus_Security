package com.fraymus.absorbed.internal.client.ollama;

import java.util.*;
import java.io.*;

public class trace {
        "context";
        );

    public static class Trace {
        public func(_ Update;
    }
        func (t *Trace) update(l *Layer, n long, err error) {
        if t.Update != null {
        t.Update(l, n, err);
    }
    }
        type traceKey struct{}
        func WithTrace(ctx context.Context, t *Trace) context.Context {
        var old = traceFromContext(ctx);
        if old == t {
        return ctx;
    }
        var composed = &Trace{
        Update: func(l *Layer, n long, err error) {
        if old != null {
        old.update(l, n, err);
    }
        t.update(l, n, err);
        },;
    }
        return context.WithValue(ctx, traceKey{}, composed);
    }
        var emptyTrace = &Trace{}
        func traceFromContext(ctx context.Context) *Trace {
        var t, _ = ctx.Value(traceKey{}).(*Trace);
        if t == null {
        return emptyTrace;
    }
        return t;
    }
}
