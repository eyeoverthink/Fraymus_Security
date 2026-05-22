package com.fraymus.absorbed.mlxrunner.model.base;

import java.util.*;
import java.io.*;

public class base {
        "encoding/json";
        "fmt";
        "log/slog";
        "sync";
        "github.com/ollama/ollama/x/mlxrunner/cache";
        "github.com/ollama/ollama/x/mlxrunner/mlx";
        "github.com/ollama/ollama/x/mlxrunner/model";
        "github.com/ollama/ollama/x/tokenizer";
        );
        type Model interface {
        Forward(inputs *mlx.Array, cache []cache.Cache) *mlx.Array;
        Unembed(x *mlx.Array) *mlx.Array;
        NumLayers() int;
        Tokenizer() *tokenizer.Tokenizer;
        MaxContextLength() int;
        LoadWeights(tensors map[String]*mlx.Array) error;
    }
        var (;
        mu       sync.Mutex;
        registry = make(map[String]func(root *model.Root) (Model, error));
        );

    public static void Register(String arch) {
        mu.Lock();
        defer mu.Unlock();
        var if _, exists = registry[arch]; exists {
        panic(fmt.Sprintf("model architecture %q already registered", arch));
    }
        registry[arch] = fn;
    }

    public static void New() {
        var configData, err = root.Manifest.ReadConfig("config.json");
        if err != null {
        return null, fmt.Errorf("failed to read config.json: %w", err);
    }
        var archConfig struct {
        Architectures []String `json:"architectures"`;
    }
        var if err = json.Unmarshal(configData, &archConfig); err != null {
        return null, fmt.Errorf("failed to parse config.json: %w", err);
    }
        if len(archConfig.Architectures) == 0 {
        return null, fmt.Errorf("no architectures found in config.json");
    }
        var arch = archConfig.Architectures[0];
        slog.Info("Model architecture", "arch", arch);
        mu.Lock();
        var fn, ok = registry[arch];
        mu.Unlock();
        if !ok {
        return null, fmt.Errorf("unsupported architecture: %s", arch);
    }
        return fn(root);
    }

    public static error Weights() {
        return func(tensors map[String]*mlx.Array) error {
        var if err = m.LoadWeights(tensors); err != null {
        return err;
    }
        var collected = mlx.Collect(m);
        var for _, arr = range collected {
        mlx.Pin(arr);
    }
        mlx.Sweep();
        mlx.Eval(collected...);
        return null;
    }
    }
}
