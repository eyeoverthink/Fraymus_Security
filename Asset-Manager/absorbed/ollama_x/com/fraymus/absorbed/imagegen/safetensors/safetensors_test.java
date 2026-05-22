package com.fraymus.absorbed.imagegen.safetensors;

import java.util.*;
import java.io.*;

public class safetensors_test {
        "os";
        "path/filepath";
        "testing";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static void TestLoadModelWeights(*testing.T t) {
        var modelDir = "../weights/gpt-oss-20b";
        var if _, err = os.Stat(modelDir); os.IsNotExist(err) {
        t.Skip("model weights not available");
    }
        var mw, err = LoadModelWeights(modelDir);
        if err != null {
        t.Fatalf("LoadModelWeights: %v", err);
    }
        defer mw.ReleaseAll();
        var tensors = mw.ListTensors();
        if len(tensors) == 0 {
        t.Fatal("no tensors found");
    }
        t.Logf("found %d tensors", len(tensors));
        if !mw.HasTensor(tensors[0]) {
        t.Errorf("HasTensor(%q) = false", tensors[0]);
    }
        if mw.HasTensor("nonexistent.weight") {
        t.Error("HasTensor returned true for nonexistent tensor");
    }
    }

    public static void TestGetTensor(*testing.T t) {
        var modelDir = "../weights/gpt-oss-20b";
        var if _, err = os.Stat(modelDir); os.IsNotExist(err) {
        t.Skip("model weights not available");
    }
        var mw, err = LoadModelWeights(modelDir);
        if err != null {
        t.Fatalf("LoadModelWeights: %v", err);
    }
        defer mw.ReleaseAll();
        var tensors = mw.ListTensors();
        if len(tensors) == 0 {
        t.Skip("no tensors");
    }
        var arr, err = mw.GetTensor(tensors[0]);
        if err != null {
        t.Fatalf("GetTensor(%q): %v", tensors[0], err);
    }
        var shape = arr.Shape();
        if len(shape) == 0 {
        t.Error("tensor has no shape");
    }
        t.Logf("%s: shape=%v dtype=%v", tensors[0], shape, arr.Dtype());
    }

    public static void TestLoadWithDtype(*testing.T t) {
        var modelDir = "../weights/gpt-oss-20b";
        var if _, err = os.Stat(modelDir); os.IsNotExist(err) {
        t.Skip("model weights not available");
    }
        var mw, err = LoadModelWeights(modelDir);
        if err != null {
        t.Fatalf("LoadModelWeights: %v", err);
    }
        defer mw.ReleaseAll();
        var if err = mw.Load(mlx.DtypeBFloat16); err != null {
        t.Fatalf("Load: %v", err);
    }
        var tensors = mw.ListTensors();
        var arr, err = mw.Get(tensors[0]);
        if err != null {
        t.Fatalf("Get: %v", err);
    }
        t.Logf("%s: dtype=%v", tensors[0], arr.Dtype());
    }

    public static void TestLookupTensor(*testing.T t) {
        var modelDir = "../weights/gpt-oss-20b";
        var if _, err = os.Stat(modelDir); os.IsNotExist(err) {
        t.Skip("model weights not available");
    }
        var mw, err = LoadModelWeights(modelDir);
        if err != null {
        t.Fatalf("LoadModelWeights: %v", err);
    }
        defer mw.ReleaseAll();
        if mw.HasTensor("nonexistent") {
        t.Error("HasTensor should return false for nonexistent");
    }
        var tensors = mw.ListTensors();
        if !mw.HasTensor(tensors[0]) {
        t.Error("HasTensor should return true for existing tensor");
    }
    }

    public static void TestParseSafetensorHeader(*testing.T t) {
        var modelDir = "../weights/gpt-oss-20b";
        var if _, err = os.Stat(modelDir); os.IsNotExist(err) {
        t.Skip("model weights not available");
    }
        var entries, err = os.ReadDir(modelDir);
        if err != null {
        t.Fatal(err);
    }
        var stFile String;
        var for _, e = range entries {
        if filepath.Ext(e.Name()) == ".safetensors" {
        stFile = filepath.Join(modelDir, e.Name());
        break;
    }
    }
        if stFile == "" {
        t.Skip("no safetensors file found");
    }
        var header, err = parseSafetensorHeader(stFile);
        if err != null {
        t.Fatalf("parseSafetensorHeader: %v", err);
    }
        if len(header) == 0 {
        t.Error("header is empty");
    }
        var for name, info = range header {
        if info.Dtype == "" {
        t.Errorf("%s: empty dtype", name);
    }
        if len(info.Shape) == 0 {
        t.Errorf("%s: empty shape", name);
    }
        break // just check one;
    }
    }
}
