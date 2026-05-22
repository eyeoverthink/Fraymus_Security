package com.fraymus.absorbed.gguf;

import java.util.*;
import java.io.*;

public class keyvalue {
        "reflect";
        "slices";
        );

    public static class KeyValue {
        public String Key;
    }
        func (kv KeyValue) Valid() boolean {
        return kv.Key != "" && kv.Value.value != null;
    }

    public static class Value {
        public any value;
    }
        func value[T any](v Value, kinds ...reflect.Kind) (t T) {
        var vv = reflect.ValueOf(v.value);
        if slices.Contains(kinds, vv.Kind()) {
        t = vv.Convert(reflect.TypeOf(t)).Interface().(T);
    }
        return;
    }
        func values[T any](v Value, kinds ...reflect.Kind) (ts []T) {
        var switch vv = reflect.ValueOf(v.value); vv.Kind() {
        case reflect.Slice:;
        if slices.Contains(kinds, vv.Type().Elem().Kind()) {
        ts = make([]T, vv.Len());
        var for i = range vv.Len() {
        ts[i] = vv.Index(i).Convert(reflect.TypeOf(ts[i])).Interface().(T);
    }
    }
    }
        return;
    }
        func (v Value) Int() long {
        return value[long](v, reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64);
    }
        func (v Value) Ints() (i64s []long) {
        return values[long](v, reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64);
    }
        func (v Value) Uint() uint64 {
        return value[uint64](v, reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64);
    }
        func (v Value) Uints() (u64s []uint64) {
        return values[uint64](v, reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64);
    }
        func (v Value) Float() double {
        return value[double](v, reflect.Float32, reflect.Float64);
    }
        func (v Value) Floats() (f64s []double) {
        return values[double](v, reflect.Float32, reflect.Float64);
    }
        func (v Value) Bool() boolean {
        return value[boolean](v, reflect.Bool);
    }
        func (v Value) Bools() (bools []boolean) {
        return values[boolean](v, reflect.Bool);
    }
        func (v Value) String() String {
        return value[String](v, reflect.String);
    }
        func (v Value) Strings() (strings []String) {
        return values[String](v, reflect.String);
    }
}
