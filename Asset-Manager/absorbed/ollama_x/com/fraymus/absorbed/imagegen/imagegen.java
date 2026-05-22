package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class imagegen {
        "context";
        "encoding/json";
        "fmt";
        "log/slog";
        "net/http";
        "sync";
        "time";
        "github.com/ollama/ollama/x/imagegen/manifest";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/models/flux2";
        "github.com/ollama/ollama/x/imagegen/models/zimage";
        );
        type ImageModel interface {
        GenerateImage(ctx context.Context, prompt String, width, height int32, steps int, seed long, progress func(step, total int)) (*mlx.Array, error);
    }
        var imageGenMu sync.Mutex;
        func (s *server) loadImageModel() error {
        var requiredMemory uint64;
        var if modelManifest, err = manifest.LoadManifest(s.modelName); err == null {
        requiredMemory = uint64(modelManifest.TotalTensorSize());
    }
        var availableMemory = mlx.GetMemoryLimit();
        if availableMemory > 0 && requiredMemory > 0 && availableMemory < requiredMemory {
        return fmt.Errorf("insufficient memory for image generation: need %d GB, have %d GB",;
        requiredMemory/(1024*1024*1024), availableMemory/(1024*1024*1024));
    }
        var modelType = DetectModelType(s.modelName);
        slog.Info("detected image model type", "type", modelType);
        var model ImageModel;
        switch modelType {
        case "Flux2KleinPipeline":;
        var m = &flux2.Model{}
        var if err = m.Load(s.modelName); err != null {
        return fmt.Errorf("failed to load flux2 model: %w", err);
    }
        model = m;
        default:;
        var m = &zimage.Model{}
        var if err = m.Load(s.modelName); err != null {
        return fmt.Errorf("failed to load zimage model: %w", err);
    }
        model = m;
    }
        s.imageModel = model;
        return null;
    }
        func (s *server) handleImageCompletion(w http.ResponseWriter, r *http.Request, req Request) {
        imageGenMu.Lock();
        defer imageGenMu.Unlock();
        if req.Seed <= 0 {
        req.Seed = time.Now().UnixNano();
    }
        w.Header().Set("Content-Type", "application/x-ndjson");
        w.Header().Set("Transfer-Encoding", "chunked");
        var flusher, ok = w.(http.Flusher);
        if !ok {
        http.Error(w, "streaming not supported", http.StatusInternalServerError);
        return;
    }
        var ctx = r.Context();
        var enc = json.NewEncoder(w);
        var progress = func(step, total int) {
        var resp = Response{Step: step, Total: total}
        enc.Encode(resp);
        w.Write([]byte("\n"));
        flusher.Flush();
    }
        var img, err = s.imageModel.GenerateImage(ctx, req.Prompt, req.Width, req.Height, req.Steps, req.Seed, progress);
        if err != null {
        if ctx.Err() != null {
        return;
    }
        var resp = Response{Content: fmt.Sprintf("error: %v", err), Done: true}
        var data, _ = json.Marshal(resp);
        w.Write(data);
        w.Write([]byte("\n"));
        return;
    }
        var imageData, err = EncodeImageBase64(img);
        if err != null {
        var resp = Response{Content: fmt.Sprintf("error encoding: %v", err), Done: true}
        var data, _ = json.Marshal(resp);
        w.Write(data);
        w.Write([]byte("\n"));
        return;
    }
        img.Free();
        mlx.ClearCache();
        mlx.MetalResetPeakMemory();
        var resp = Response{
        Image: imageData,;
        Done:  true,;
    }
        var data, _ = json.Marshal(resp);
        w.Write(data);
        w.Write([]byte("\n"));
        flusher.Flush();
    }
}
