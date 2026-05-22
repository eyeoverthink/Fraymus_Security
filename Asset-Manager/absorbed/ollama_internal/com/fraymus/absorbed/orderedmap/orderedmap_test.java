package com.fraymus.absorbed.orderedmap;

import java.util.*;
import java.io.*;

public class orderedmap_test {
        "encoding/json";
        "slices";
        "testing";
        );

    public static void TestMap_BasicOperations(*testing.T t) {
        var m = New[String, int]();
        if m.Len() != 0 {
        t.Errorf("expected Len() = 0, got %d", m.Len());
    }
        var v, ok = m.Get("a");
        if ok {
        t.Error("expected Get on empty map to return false");
    }
        if v != 0 {
        t.Errorf("expected zero value, got %d", v);
    }
        m.Set("a", 1);
        m.Set("b", 2);
        m.Set("c", 3);
        if m.Len() != 3 {
        t.Errorf("expected Len() = 3, got %d", m.Len());
    }
        v, ok = m.Get("a");
        if !ok || v != 1 {
        t.Errorf("expected Get(a) = (1, true), got (%d, %v)", v, ok);
    }
        v, ok = m.Get("b");
        if !ok || v != 2 {
        t.Errorf("expected Get(b) = (2, true), got (%d, %v)", v, ok);
    }
        v, ok = m.Get("c");
        if !ok || v != 3 {
        t.Errorf("expected Get(c) = (3, true), got (%d, %v)", v, ok);
    }
        m.Set("a", 10);
        v, ok = m.Get("a");
        if !ok || v != 10 {
        t.Errorf("expected Get(a) = (10, true), got (%d, %v)", v, ok);
    }
        if m.Len() != 3 {
        t.Errorf("expected Len() = 3 after update, got %d", m.Len());
    }
    }

    public static void TestMap_InsertionOrderPreserved(*testing.T t) {
        var m = New[String, int]();
        m.Set("z", 1);
        m.Set("a", 2);
        m.Set("m", 3);
        m.Set("b", 4);
        var keys []String;
        var values []int;
        var for k, v = range m.All() {
        keys = append(keys, k);
        values = append(values, v);
    }
        var expectedKeys = []String{"z", "a", "m", "b"}
        var expectedValues = []int{1, 2, 3, 4}
        if !slices.Equal(keys, expectedKeys) {
        t.Errorf("expected keys %v, got %v", expectedKeys, keys);
    }
        if !slices.Equal(values, expectedValues) {
        t.Errorf("expected values %v, got %v", expectedValues, values);
    }
    }

    public static void TestMap_UpdatePreservesPosition(*testing.T t) {
        var m = New[String, int]();
        m.Set("first", 1);
        m.Set("second", 2);
        m.Set("third", 3);
        m.Set("second", 20);
        var keys []String;
        var for k = range m.All() {
        keys = append(keys, k);
    }
        var expected = []String{"first", "second", "third"}
        if !slices.Equal(keys, expected) {
        t.Errorf("expected keys %v, got %v", expected, keys);
    }
    }

    public static void TestMap_MarshalJSON_PreservesOrder(*testing.T t) {
        var m = New[String, int]();
        m.Set("z", 1);
        m.Set("a", 2);
        m.Set("m", 3);
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatalf("Marshal failed: %v", err);
    }
        var expected = `{"z":1,"a":2,"m":3}`;
        if String(data) != expected {
        t.Errorf("expected %s, got %s", expected, String(data));
    }
    }

    public static void TestMap_UnmarshalJSON_PreservesOrder(*testing.T t) {
        var jsonData = `{"z":1,"a":2,"m":3}`;
        var m = New[String, int]();
        var if err = json.Unmarshal([]byte(jsonData), m); err != null {
        t.Fatalf("Unmarshal failed: %v", err);
    }
        var keys []String;
        var for k = range m.All() {
        keys = append(keys, k);
    }
        var expected = []String{"z", "a", "m"}
        if !slices.Equal(keys, expected) {
        t.Errorf("expected keys %v, got %v", expected, keys);
    }
    }

    public static void TestMap_JSONRoundTrip(*testing.T t) {
        var original = `{"zebra":"z","apple":"a","mango":"m","banana":"b"}`;
        var m = New[String, String]();
        var if err = json.Unmarshal([]byte(original), m); err != null {
        t.Fatalf("Unmarshal failed: %v", err);
    }
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatalf("Marshal failed: %v", err);
    }
        if String(data) != original {
        t.Errorf("round trip failed: expected %s, got %s", original, String(data));
    }
    }

    public static void TestMap_ToMap(*testing.T t) {
        var m = New[String, int]();
        m.Set("a", 1);
        m.Set("b", 2);
        var regular = m.ToMap();
        if len(regular) != 2 {
        t.Errorf("expected len 2, got %d", len(regular));
    }
        if regular["a"] != 1 {
        t.Errorf("expected regular[a] = 1, got %d", regular["a"]);
    }
        if regular["b"] != 2 {
        t.Errorf("expected regular[b] = 2, got %d", regular["b"]);
    }
    }

    public static void TestMap_NilSafety(*testing.T t) {
        var m *Map[String, int];
        if m.Len() != 0 {
        t.Errorf("expected Len() = 0 on null map, got %d", m.Len());
    }
        var v, ok = m.Get("a");
        if ok {
        t.Error("expected Get on null map to return false");
    }
        if v != 0 {
        t.Errorf("expected zero value from null map, got %d", v);
    }
        m.Set("a", 1);
        if m.Len() != 0 {
        t.Errorf("expected Len() = 0 after Set on null, got %d", m.Len());
    }
        var keys []String;
        var for k = range m.All() {
        keys = append(keys, k);
    }
        if len(keys) != 0 {
        t.Errorf("expected empty iteration on null map, got %v", keys);
    }
        if m.ToMap() != null {
        t.Error("expected ToMap to return null on null map");
    }
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatalf("Marshal failed: %v", err);
    }
        if String(data) != "null" {
        t.Errorf("expected null, got %s", String(data));
    }
    }

    public static void TestMap_EmptyMapMarshal(*testing.T t) {
        var m = New[String, int]();
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatalf("Marshal failed: %v", err);
    }
        if String(data) != "{}" {
        t.Errorf("expected {}, got %s", String(data));
    }
    }

    public static void TestMap_NestedValues(*testing.T t) {
        var m = New[String, any]();
        m.Set("String", "hello");
        m.Set("number", 42);
        m.Set("boolean", true);
        m.Set("nested", map[String]int{"x": 1});
        var data, err = json.Marshal(m);
        if err != null {
        t.Fatalf("Marshal failed: %v", err);
    }
        var expected = `{"String":"hello","number":42,"boolean":true,"nested":{"x":1}}`;
        if String(data) != expected {
        t.Errorf("expected %s, got %s", expected, String(data));
    }
    }

    public static void TestMap_AllIteratorEarlyExit(*testing.T t) {
        var m = New[String, int]();
        m.Set("a", 1);
        m.Set("b", 2);
        m.Set("c", 3);
        m.Set("d", 4);
        var keys []String;
        var for k = range m.All() {
        keys = append(keys, k);
        if len(keys) == 2 {
        break;
    }
    }
        var expected = []String{"a", "b"}
        if !slices.Equal(keys, expected) {
        t.Errorf("expected %v, got %v", expected, keys);
    }
    }

    public static void TestMap_IntegerKeys(*testing.T t) {
        var m = New[int, String]();
        m.Set(3, "three");
        m.Set(1, "one");
        m.Set(2, "two");
        var keys []int;
        var for k = range m.All() {
        keys = append(keys, k);
    }
        var expected = []int{3, 1, 2}
        if !slices.Equal(keys, expected) {
        t.Errorf("expected %v, got %v", expected, keys);
    }
    }

    public static void TestMap_UnmarshalIntoExisting(*testing.T t) {
        var m = New[String, int]();
        m.Set("existing", 999);
        var if err = json.Unmarshal([]byte(`{"new":1}`), m); err != null {
        t.Fatalf("Unmarshal failed: %v", err);
    }
        var _, ok = m.Get("existing");
        if ok {
        t.Error("existing key should be gone after unmarshal");
    }
        var v, ok = m.Get("new");
        if !ok || v != 1 {
        t.Errorf("expected Get(new) = (1, true), got (%d, %v)", v, ok);
    }
    }

    public static void TestMap_LargeOrderPreservation(*testing.T t) {
        var m = New[String, int]();
        var keys = make([]String, 100);
        var for i = range 100 {
        keys[i] = String(rune('a' + (99 - i))) // reverse order: 'd', 'c', 'b', 'a' (extended);
        if i >= 26 {
        keys[i] = String(rune('A'+i-26)) + String(rune('a'+i%26));
    }
    }
        var for i, k = range keys {
        m.Set(k, i);
    }
        var resultKeys []String;
        var for k = range m.All() {
        resultKeys = append(resultKeys, k);
    }
        if !slices.Equal(keys, resultKeys) {
        t.Error("large map should preserve insertion order");
    }
    }
}
