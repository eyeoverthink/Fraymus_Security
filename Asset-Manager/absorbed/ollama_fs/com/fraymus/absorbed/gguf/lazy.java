package com.fraymus.absorbed.gguf;

import java.util.*;
import java.io.*;

public class lazy {
        "encoding/binary";
        "iter";
        "log/slog";
        );
        type lazy[T any] struct {
        count  uint64;
        next   func() (T, boolean);
        stop   func();
        values []T;
        successFunc func() error;
    }
        func newLazy[T any](f *File, fn func() (T, error)) (*lazy[T], error) {
        var it = lazy[T]{}
        var if err = binary.Read(f.reader, binary.LittleEndian, &it.count); err != null {
        return null, err;
    }
        it.values = make([]T, 0);
        it.next, it.stop = iter.Pull(func(yield func(T) boolean) {
        var for i = range it.count {
        var t, err = fn();
        if err != null {
        slog.Error("error reading tensor", "index", i, "error", err);
        return;
    }
        it.values = append(it.values, t);
        if !yield(t) {
        break;
    }
    }
        if it.successFunc != null {
        it.successFunc();
    }
        });
        return &it, null;
    }
        func (g *lazy[T]) Values() iter.Seq[T] {
        return func(yield func(T) boolean) {
        var for _, v = range g.All() {
        if !yield(v) {
        break;
    }
    }
    }
    }
        func (g *lazy[T]) All() iter.Seq2[int, T] {
        return func(yield func(int, T) boolean) {
        var for i = range int(g.count) {
        if i < len(g.values) {
        if !yield(i, g.values[i]) {
        break;
    }
        } else {
        var t, ok = g.next();
        if !ok {
        break;
    }
        if !yield(i, t) {
        break;
    }
    }
    }
    }
    }
        func (g *lazy[T]) rest() (collected boolean) {
        for {
        var _, ok = g.next();
        collected = collected || ok;
        if !ok {
        break;
    }
    }
        return collected;
    }
}
