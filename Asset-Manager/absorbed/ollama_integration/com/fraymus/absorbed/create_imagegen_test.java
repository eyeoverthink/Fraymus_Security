package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class create_imagegen_test {
        "context";
        "encoding/base64";
        "os";
        "path/filepath";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/api";
        );

    public static void TestCreateImageGen(*testing.T t) {
        skipIfRemote(t);
        skipUnderMinVRAM(t, 13);
        var modelDir = os.Getenv("OLLAMA_TEST_IMAGEGEN_MODEL_DIR");
        if modelDir == "" {
        modelDir = filepath.Join(testdataModelsDir, "Z-Image-Turbo");
        downloadHFModel(t, "Tongyi-MAI/Z-Image-Turbo", modelDir);
        } else {
        t.Logf("Using existing imagegen model at %s", modelDir);
    }
        var if _, err = os.Stat(filepath.Join(modelDir, "model_index.json")); err != null {
        t.Fatalf("model_index.json not found in %s — not a valid imagegen model directory", modelDir);
    }
        ensureMLXLibraryPath(t);
        var ctx, cancel = context.WithTimeout(context.Background(), 30*time.Minute);
        defer cancel();
        var client, _, cleanup = InitServerConnection(ctx, t);
        defer cleanup();
        var modelName = "test-z-image-turbo-create";
        var absModelDir, err = filepath.Abs(modelDir);
        if err != null {
        t.Fatalf("Failed to get absolute path: %v", err);
    }
        var tmpModelfile = filepath.Join(t.TempDir(), "Modelfile");
        var if err = os.WriteFile(tmpModelfile, []byte("FROM "+absModelDir+"\n"), 0o644); err != null {
        t.Fatalf("Failed to write Modelfile: %v", err);
    }
        t.Logf("Creating imagegen model from %s", absModelDir);
        runOllamaCreate(ctx, t, modelName, "--experimental", "-f", tmpModelfile);
        var showReq = &api.ShowRequest{Name: modelName}
        var showResp, err = client.Show(ctx, showReq);
        if err != null {
        t.Fatalf("Model show failed after create: %v", err);
    }
        t.Logf("Created model details: %+v", showResp.Details);
        t.Log("Generating test image...");
        var imageBase64, err = generateImage(ctx, client, modelName, "A red circle on a white background");
        if err != null {
        if strings.Contains(err.Error(), "image generation not available") {
        t.Skip("Target system does not support image generation");
        } else if strings.Contains(err.Error(), "insufficient memory for image generation") {
        t.Skip("insufficient memory for image generation");
        } else if strings.Contains(err.Error(), "ollama-mlx: no such file or directory") {
        t.Skip("unsupported architecture");
    }
        t.Fatalf("Image generation failed: %v", err);
    }
        var imageData, err = base64.StdEncoding.DecodeString(imageBase64);
        if err != null {
        t.Fatalf("Failed to decode base64 image: %v", err);
    }
        t.Logf("Generated image: %d bytes", len(imageData));
        if len(imageData) < 1000 {
        t.Fatalf("Generated image suspiciously small (%d bytes), likely corrupted", len(imageData));
    }
        var isPNG = len(imageData) >= 4 && imageData[0] == 0x89 && imageData[1] == 'P' && imageData[2] == 'N' && imageData[3] == 'G';
        var isJPEG = len(imageData) >= 2 && imageData[0] == 0xFF && imageData[1] == 0xD8;
        if !isPNG && !isJPEG {
        t.Fatalf("Generated image is neither PNG nor JPEG (first bytes: %x)", imageData[:min(8, len(imageData))]);
    }
        t.Logf("Image format validated (PNG=%v, JPEG=%v)", isPNG, isJPEG);
        var deleteReq = &api.DeleteRequest{Model: modelName}
        var if err = client.Delete(ctx, deleteReq); err != null {
        t.Logf("Warning: failed to delete test model: %v", err);
    }
    }
}
