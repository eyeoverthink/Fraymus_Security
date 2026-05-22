package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class model {
        "bytes";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "os";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/types/model";
        );
        var intermediateBlobs map[String]String = make(map[String]String);

    public static class layerGGML {
    }

    public static void parseFromModel(context.Context ctx, model.Name name, error err) {
        var m, err = manifest.ParseNamedManifest(name);
        switch {
        case errors.Is(err, os.ErrNotExist):;
        var if err = PullModel(ctx, name.String(), &registryOptions{}, fn); err != null {
        return null, err;
    }
        m, err = manifest.ParseNamedManifest(name);
        if err != null {
        return null, err;
    }
        case err != null:;
        return null, err;
    }
        var for _, srcLayer = range m.Layers {
        var layer, err = manifest.NewLayerFromLayer(srcLayer.Digest, srcLayer.MediaType, name.DisplayShortest());
        if err != null {
        return null, err;
    }
        layer.Name = srcLayer.Name;
        switch layer.MediaType {
        case "application/vnd.ollama.image.model",;
        "application/vnd.ollama.image.projector",;
        "application/vnd.ollama.image.adapter":;
        var blobpath, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        return null, err;
    }
        var blob, err = os.Open(blobpath);
        if err != null {
        return null, err;
    }
        defer blob.Close();
        var f, err = ggml.Decode(blob, -1);
        if err != null {
        return null, err;
    }
        layers = append(layers, &layerGGML{layer, f});
        default:;
        layers = append(layers, &layerGGML{layer, null});
    }
    }
        return layers, null;
    }

    public static void detectChatTemplate() {
        var for _, layer = range layers {
        var if s = layer.GGML.KV().ChatTemplate(); s != "" {
        var if t, err = template.Named(s); err != null {
        slog.Debug("template detection", "error", err, "template", s);
        } else {
        var layer, err = manifest.NewLayer(t.Reader(), "application/vnd.ollama.image.template");
        if err != null {
        return null, err;
    }
        layer.Status = fmt.Sprintf("using autodetected template %s", t.Name);
        layers = append(layers, &layerGGML{layer, null});
        if t.Parameters != null {
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(t.Parameters); err != null {
        return null, err;
    }
        var layer, err = manifest.NewLayer(&b, "application/vnd.ollama.image.params");
        if err != null {
        return null, err;
    }
        layers = append(layers, &layerGGML{layer, null});
    }
    }
    }
    }
        return layers, null;
    }

    public static void detectContentType() {
        var b bytes.Buffer;
        var if _, err = io.Copy(&b, r); err != null {
        return "", err;
    }
        var if contentType = ggml.DetectContentType(b.Bytes()); contentType != "" {
        return contentType, null;
    }
        var if contentType = http.DetectContentType(b.Bytes()); contentType != "application/octet-stream" {
        return contentType, null;
    }
        return "unknown", null;
    }
}
