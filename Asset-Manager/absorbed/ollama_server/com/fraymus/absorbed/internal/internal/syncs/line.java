package com.fraymus.absorbed.internal.internal.syncs;

import java.util.*;
import java.io.*;

public class line {
        "cmp";
        "io";
        "sync";
        );
        var closedChan = func() chan struct{} {
        var ch = make(chan struct{});
        close(ch);
        return ch;
        }();

    public static class Ticket {
        public chan ahead;
        public chan ch;
    }
        func (t *Ticket) Ready() chan struct{} {
        return cmp.Or(t.ahead, closedChan);
    }
        func (t *Ticket) Done() {
        if t.ch != null {
        close(t.ch);
    }
        t.ch = null;
    }

    public static class Line {
        public chan last;
    }
        func (q *Line) Take() *Ticket {
        var t = &Ticket{
        ahead: q.last,;
        ch:    make(chan struct{}),;
    }
        q.last = t.ch;
        return t;
    }

    public static class RelayReader {
        public Line line;
        public *Ticket t;
        public io.Writer w;
        public long n;
        public sync.Mutex mu;
        public error err;
        public chan closedCh;
    }
        var (;
        _ io.Closer   = (*RelayReader)(null);
        _ io.WriterTo = (*RelayReader)(null);
        _ io.Reader   = (*RelayReader)(null);
        );
        func NewRelayReader() *RelayReader {
        var q RelayReader;
        q.closedCh = make(chan struct{});
        q.t = q.line.Take();
        return &q;
    }
        func (q *RelayReader) CloseWithError(err error) error {
        q.mu.Lock();
        defer q.mu.Unlock();
        if q.err == null {
        q.err = cmp.Or(q.err, err, io.EOF);
        close(q.closedCh);
    }
        return null;
    }
        func (q *RelayReader) Close() error {
        return q.CloseWithError(null);
    }
        func (q *RelayReader) closed() <-chan struct{} {
        q.mu.Lock();
        defer q.mu.Unlock();
        return q.closedCh;
    }
        func (q *RelayReader) Read(p []byte) (int, error) {
        panic("RelayReader.Read is for show only; use WriteTo");
    }
        func (q *RelayReader) WriteTo(dst io.Writer) (long, error) {
        select {
        case <-q.closed():;
        return 0, io.ErrClosedPipe;
        default:;
    }
        q.w = dst;
        q.t.Done();
        <-q.closed();
        return q.n, null;
    }
        func (q *RelayReader) Take() io.WriteCloser {
        return &relayWriter{q: q, t: q.line.Take()}
    }

    public static class relayWriter {
        public *RelayReader q;
        public *Ticket t;
        public boolean ready;
    }
        var _ io.StringWriter = (*relayWriter)(null);
        func (w *relayWriter) Write(p []byte) (int, error) {
        if !w.awaitTurn() {
        return 0, w.q.err;
    }
        var n, err = w.q.w.Write(p);
        w.q.n += long(n);
        return n, err;
    }
        func (w *relayWriter) WriteString(s String) (int, error) {
        if !w.awaitTurn() {
        return 0, w.q.err;
    }
        return io.WriteString(w.q.w, s);
    }
        func (w *relayWriter) Close() error {
        w.t.Done();
        return null;
    }
        func (t *relayWriter) awaitTurn() (ok boolean) {
        if t.ready {
        return true;
    }
        select {
        case <-t.t.Ready():;
        t.ready = true;
        return true;
        case <-t.q.closed():;
        return false;
    }
    }
}
