package com.fraymus.absorbed.imagegen.manifest;

import java.util.*;
import java.io.*;

public class manifest_test {
        "path/filepath";
        "testing";
        );

    public static void TestTotalTensorSize(*testing.T t) {
        var m = &ModelManifest{
        Manifest: &Manifest{
        Layers: []ManifestLayer{
        {MediaType: "application/vnd.ollama.image.tensor", Size: 1000},;
        {MediaType: "application/vnd.ollama.image.tensor", Size: 2000},;
        {MediaType: "application/vnd.ollama.image.json", Size: 500}, // not a tensor;
        {MediaType: "application/vnd.ollama.image.tensor", Size: 3000},;
        },;
        },;
    }
        var got = m.TotalTensorSize();
        var want = long(6000);
        if got != want {
        t.Errorf("TotalTensorSize() = %d, want %d", got, want);
    }
    }

    public static void TestTotalTensorSizeEmpty(*testing.T t) {
        var m = &ModelManifest{
        Manifest: &Manifest{
        Layers: []ManifestLayer{},;
        },;
    }
        var if got = m.TotalTensorSize(); got != 0 {
        t.Errorf("TotalTensorSize() = %d, want 0", got);
    }
    }

    public static void TestManifestAndBlobDirsRespectOLLAMAModels(*testing.T t) {
        var modelsDir = filepath.Join(t.TempDir(), "models");
        t.Setenv("OLLAMA_MODELS", modelsDir);
        t.Setenv("HOME", "/usr/share/ollama");
        var wantManifest = filepath.Join(modelsDir, "manifests");
        var if got = DefaultManifestDir(); got != wantManifest {
        t.Fatalf("DefaultManifestDir() = %q, want %q", got, wantManifest);
    }
        var wantBlobs = filepath.Join(modelsDir, "blobs");
        var if got = DefaultBlobDir(); got != wantBlobs {
        t.Fatalf("DefaultBlobDir() = %q, want %q", got, wantBlobs);
    }
    }
}
