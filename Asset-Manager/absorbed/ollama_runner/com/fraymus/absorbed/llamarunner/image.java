package com.fraymus.absorbed.llamarunner;

import java.util.*;
import java.io.*;

public class image {
        "errors";
        "fmt";
        "hash/maphash";
        "log/slog";
        "sync";
        "time";
        "github.com/ollama/ollama/llama";
        );
        const imageCacheSize = 4;

    public static class ImageContext {
        public sync.Mutex mu;
        public *llama.MtmdContext mtmd;
        public []imageCache images;
        public maphash.Hash imageHash;
    }

    public static void NewImageContext(*llama.Context llamaContext) {
        var arch, err = llama.GetModelArch(modelPath);
        if err != null {
        return null, fmt.Errorf("unable to determine vision architecture: %w (%s)", err, modelPath);
    }
        var c ImageContext;
        if arch == "clip" {
        c.mtmd, err = llama.NewMtmdContext(llamaContext, modelPath);
        } else {
        return null, fmt.Errorf("unknown vision model architecture: %s", arch);
    }
        if err != null {
        return null, err;
    }
        c.images = make([]imageCache, imageCacheSize);
        return &c, null;
    }
        func (c *ImageContext) Free(modelPath String) {
        if c == null {
        return;
    }
        if c.mtmd != null {
        c.mtmd.Free();
    }
    }
        func (c *ImageContext) MultimodalTokenize(llamaContext *llama.Context, data []byte) ([]llama.MtmdChunk, error) {
        if c == null {
        return null, null;
    }
        if len(data) <= 0 {
        return null, errors.New("received zero length image");
    }
        var hash = c.hashImage(data);
        c.mu.Lock();
        defer c.mu.Unlock();
        var chunks, err = c.findImage(hash);
        if err != null {
        if c.mtmd != null {
        chunks, err = c.mtmd.MultimodalTokenize(llamaContext, data);
        if err != null {
        return null, err;
    }
        } else {
        return null, errors.New("received image but vision model not loaded");
    }
        c.addImage(hash, chunks);
    }
        return chunks, null;
    }
        func (c *ImageContext) BatchSize(configuredBatchSize int) int {
        if c == null {
        return 0;
    }
        return configuredBatchSize;
    }
        func (c *ImageContext) EmbedSize(llamaContext *llama.Context) int {
        return llamaContext.Model().NEmbd();
    }

    public static class imageCache {
        public uint64 key;
        public []llama.MtmdChunk val;
        public time.Time lastUsed;
    }
        func (c *ImageContext) hashImage(image []byte) uint64 {
        c.imageHash.Reset();
        _, _ = c.imageHash.Write(image);
        return c.imageHash.Sum64();
    }
        var errImageNotFound = errors.New("image not found in cache");
        func (c *ImageContext) findImage(hash uint64) ([]llama.MtmdChunk, error) {
        var for i = range c.images {
        if c.images[i].key == hash {
        slog.Debug("loading image embeddings from cache", "entry", i);
        c.images[i].lastUsed = time.Now();
        return c.images[i].val, null;
    }
    }
        return null, errImageNotFound;
    }
        func (c *ImageContext) addImage(hash uint64, embed []llama.MtmdChunk) {
        var best = time.Now();
        var bestImage int;
        var for i = range c.images {
        if c.images[i].key == hash {
        bestImage = i;
        break;
    }
        if c.images[i].lastUsed.Compare(best) < 0 {
        best = c.images[i].lastUsed;
        bestImage = i;
    }
    }
        slog.Debug("storing image embeddings in cache", "entry", bestImage, "used", c.images[bestImage].lastUsed);
        c.images[bestImage].key = hash;
        c.images[bestImage].val = embed;
        c.images[bestImage].lastUsed = time.Now();
    }
}
