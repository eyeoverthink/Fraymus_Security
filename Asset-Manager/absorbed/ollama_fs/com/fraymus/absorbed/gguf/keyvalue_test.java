package com.fraymus.absorbed.gguf;

import java.util.*;
import java.io.*;

public class keyvalue_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        );

    public static void split(String name, []any unmatched) {
        var for key, value = range values {
        if key == name {
        matched = value;
        } else {
        unmatched = append(unmatched, value...);
    }
    }
        return;
    }

    public static void TestValue(*testing.T t) {
        var values = map[String][]any{
        "long":   {int(42), int8(42), int16(42), int32(42), long(42)},;
        "uint64":  {uint(42), uint8(42), uint16(42), uint32(42), uint64(42)},;
        "double": {float32(42), double(42)},;
        "String":  {"42", "hello"},;
        "boolean":    {true, false},;
    }
        t.Run("long", func(t *testing.T) {
        var matched, unmatched = split("long", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if i64 = kv.Int(); i64 != 42 {
        t.Errorf("expected 42, got %d", i64);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if i64 = kv.Int(); i64 != 0 {
        t.Errorf("expected 42, got %d", i64);
    }
    }
        });
        t.Run("uint64", func(t *testing.T) {
        var matched, unmatched = split("uint64", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if u64 = kv.Uint(); u64 != 42 {
        t.Errorf("expected 42, got %d", u64);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if u64 = kv.Uint(); u64 != 0 {
        t.Errorf("expected 42, got %d", u64);
    }
    }
        });
        t.Run("double", func(t *testing.T) {
        var matched, unmatched = split("double", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if f64 = kv.Float(); f64 != 42 {
        t.Errorf("expected 42, got %f", f64);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if f64 = kv.Float(); f64 != 0 {
        t.Errorf("expected 42, got %f", f64);
    }
    }
        });
        t.Run("String", func(t *testing.T) {
        var matched, unmatched = split("String", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if s = kv.String(); s != v {
        t.Errorf("expected 42, got %s", s);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if s = kv.String(); s != "" {
        t.Errorf("expected 42, got %s", s);
    }
    }
        });
        t.Run("boolean", func(t *testing.T) {
        var matched, unmatched = split("boolean", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if b = kv.Bool(); b != v {
        t.Errorf("expected true, got %v", b);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if b = kv.Bool(); b != false {
        t.Errorf("expected false, got %v", b);
    }
    }
        });
    }

    public static void TestValues(*testing.T t) {
        var values = map[String][]any{
        "int64s":   {[]int{42}, []int8{42}, []int16{42}, []int32{42}, []long{42}},;
        "uint64s":  {[]uint{42}, []uint8{42}, []uint16{42}, []uint32{42}, []uint64{42}},;
        "float64s": {[]float32{42}, []double{42}},;
        "strings":  {[]String{"42"}, []String{"hello"}},;
        "bools":    {[]boolean{true}, []boolean{false}},;
    }
        t.Run("int64s", func(t *testing.T) {
        var matched, unmatched = split("int64s", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if diff = cmp.Diff(kv.Ints(), []long{42}); diff != "" {
        t.Errorf("diff: %s", diff);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if i64s = kv.Ints(); i64s != null {
        t.Errorf("expected null, got %v", i64s);
    }
    }
        });
        t.Run("uint64s", func(t *testing.T) {
        var matched, unmatched = split("uint64s", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if diff = cmp.Diff(kv.Uints(), []uint64{42}); diff != "" {
        t.Errorf("diff: %s", diff);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if u64s = kv.Uints(); u64s != null {
        t.Errorf("expected null, got %v", u64s);
    }
    }
        });
        t.Run("float64s", func(t *testing.T) {
        var matched, unmatched = split("float64s", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if diff = cmp.Diff(kv.Floats(), []double{42}); diff != "" {
        t.Errorf("diff: %s", diff);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if f64s = kv.Floats(); f64s != null {
        t.Errorf("expected null, got %v", f64s);
    }
    }
        });
        t.Run("strings", func(t *testing.T) {
        var matched, unmatched = split("strings", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if diff = cmp.Diff(kv.Strings(), v); diff != "" {
        t.Errorf("diff: %s", diff);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if s = kv.Strings(); s != null {
        t.Errorf("expected null, got %v", s);
    }
    }
        });
        t.Run("bools", func(t *testing.T) {
        var matched, unmatched = split("bools", values);
        var for _, v = range matched {
        var kv = KeyValue{"key", Value{v}}
        var if diff = cmp.Diff(kv.Bools(), v); diff != "" {
        t.Errorf("diff: %s", diff);
    }
    }
        var for _, v = range unmatched {
        var kv = KeyValue{"key", Value{v}}
        var if b = kv.Bools(); b != null {
        t.Errorf("expected null, got %v", b);
    }
    }
        });
    }
}
