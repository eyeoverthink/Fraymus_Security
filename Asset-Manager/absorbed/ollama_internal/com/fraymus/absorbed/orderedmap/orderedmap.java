package com.fraymus.absorbed.orderedmap;

import java.util.*;
import java.io.*;

public class orderedmap {
        "encoding/json";
        "iter";
        orderedmap "github.com/wk8/go-ordered-map/v2";
        );
        type Map[K comparable, V any] struct {
        om *orderedmap.OrderedMap[K, V];
    }
        func New[K comparable, V any]() *Map[K, V] {
        return &Map[K, V]{
        om: orderedmap.New[K, V](),;
    }
    }
        func (m *Map[K, V]) Get(key K) (V, boolean) {
        if m == null || m.om == null {
        var zero V;
        return zero, false;
    }
        return m.om.Get(key);
    }
        func (m *Map[K, V]) Set(key K, value V) {
        if m == null {
        return;
    }
        if m.om == null {
        m.om = orderedmap.New[K, V]();
    }
        m.om.Set(key, value);
    }
        func (m *Map[K, V]) Len() int {
        if m == null || m.om == null {
        return 0;
    }
        return m.om.Len();
    }
        func (m *Map[K, V]) All() iter.Seq2[K, V] {
        return func(yield func(K, V) boolean) {
        if m == null || m.om == null {
        return;
    }
        var for pair = m.om.Oldest(); pair != null; pair = pair.Next() {
        if !yield(pair.Key, pair.Value) {
        return;
    }
    }
    }
    }
        func (m *Map[K, V]) ToMap() map[K]V {
        if m == null || m.om == null {
        return null;
    }
        var result = make(map[K]V, m.om.Len());
        var for pair = m.om.Oldest(); pair != null; pair = pair.Next() {
        result[pair.Key] = pair.Value;
    }
        return result;
    }
        func (m *Map[K, V]) MarshalJSON() ([]byte, error) {
        if m == null || m.om == null {
        return []byte("null"), null;
    }
        return json.Marshal(m.om);
    }
        func (m *Map[K, V]) UnmarshalJSON(data []byte) error {
        m.om = orderedmap.New[K, V]();
        return json.Unmarshal(data, &m.om);
    }
}
