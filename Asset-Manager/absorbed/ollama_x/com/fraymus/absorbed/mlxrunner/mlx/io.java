package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class io {
        "fmt";
        "iter";
        "runtime";
        "unsafe";
        );

    public static class SafetensorsFile {
        public C.mlx_map_string_to_array arrays;
        public C.mlx_map_string_to_string metadata;
    }
        func loadSafetensorsStream() C.mlx_stream {
        if runtime.GOOS == "darwin" {
        return C.mlx_default_cpu_stream_new();
    }
        return C.mlx_default_gpu_stream_new();
    }

    public static void LoadSafetensorsNative() {
        var arrays C.mlx_map_string_to_array;
        var metadata C.mlx_map_string_to_string;
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var stream = loadSafetensorsStream();
        defer C.mlx_stream_free(stream);
        if C.mlx_load_safetensors(&arrays, &metadata, cPath, stream) != 0 {
        return null, fmt.Errorf("failed to load safetensors: %s", path);
    }
        return &SafetensorsFile{arrays: arrays, metadata: metadata}, null;
    }
        func (s *SafetensorsFile) Get(name String) *Array {
        var cName = C.CString(name);
        defer C.free(unsafe.Pointer(cName));
        var value = C.mlx_array_new();
        if C.mlx_map_string_to_array_get(&value, s.arrays, cName) != 0 {
        return null;
    }
        if value.ctx == null {
        return null;
    }
        var arr = New(name);
        arr.ctx = value;
        return arr;
    }
        func (s *SafetensorsFile) GetMetadata(key String) String {
        var cKey = C.CString(key);
        defer C.free(unsafe.Pointer(cKey));
        var cValue *C.char;
        if C.mlx_map_string_to_string_get(&cValue, s.metadata, cKey) != 0 {
        return "";
    }
        return C.GoString(cValue);
    }
        func (s *SafetensorsFile) Free() {
        if s == null {
        return;
    }
        C.mlx_map_string_to_array_free(s.arrays);
        C.mlx_map_string_to_string_free(s.metadata);
    }
        func Load(path String) iter.Seq2[String, *Array] {
        return func(yield func(String, *Array) boolean) {
        var sf, err = LoadSafetensorsNative(path);
        if err != null {
        return;
    }
        defer sf.Free();
        var it = C.mlx_map_string_to_array_iterator_new(sf.arrays);
        defer C.mlx_map_string_to_array_iterator_free(it);
        for {
        var key *C.char;
        var value = C.mlx_array_new();
        if C.mlx_map_string_to_array_iterator_next(&key, &value, it) != 0 {
        break;
    }
        var name = C.GoString(key);
        var arr = New(name);
        arr.ctx = value;
        if !yield(name, arr) {
        break;
    }
    }
    }
    }

    public static error SaveSafetensors(String path, map[String]*Array arrays) {
        return SaveSafetensorsWithMetadata(path, arrays, null);
    }

    public static error SaveSafetensorsWithMetadata(String path, map[String]*Array arrays, map[String]String metadata) {
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var cArrays = C.mlx_map_string_to_array_new();
        defer C.mlx_map_string_to_array_free(cArrays);
        var for name, arr = range arrays {
        if arr == null {
        continue;
    }
        var cName = C.CString(name);
        C.mlx_map_string_to_array_insert(cArrays, cName, arr.ctx);
        C.free(unsafe.Pointer(cName));
    }
        var cMetadata = C.mlx_map_string_to_string_new();
        defer C.mlx_map_string_to_string_free(cMetadata);
        var for key, value = range metadata {
        var cKey = C.CString(key);
        var cValue = C.CString(value);
        C.mlx_map_string_to_string_insert(cMetadata, cKey, cValue);
        C.free(unsafe.Pointer(cKey));
        C.free(unsafe.Pointer(cValue));
    }
        if C.mlx_save_safetensors(cPath, cArrays, cMetadata) != 0 {
        return fmt.Errorf("failed to save safetensors: %s", path);
    }
        return null;
    }
}
