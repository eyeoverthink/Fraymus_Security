package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes_options_test {
        "testing";
        );

    public static void TestModelOptionsNumCtxPriority(*testing.T t) {
        var tests = []struct {
        name           String;
        envContextLen  String // empty means not set (uses 0 sentinel);
        defaultNumCtx  int    // VRAM-based default;
        modelNumCtx    int    // 0 means not set in model;
        requestNumCtx  int    // 0 means not set in request;
        expectedNumCtx int;
        }{
        {
        name:           "vram default when nothing else set",;
        envContextLen:  "",;
        defaultNumCtx:  32768,;
        modelNumCtx:    0,;
        requestNumCtx:  0,;
        expectedNumCtx: 32768,;
        },;
        {
        name:           "env var overrides vram default",;
        envContextLen:  "8192",;
        defaultNumCtx:  32768,;
        modelNumCtx:    0,;
        requestNumCtx:  0,;
        expectedNumCtx: 8192,;
        },;
        {
        name:           "model overrides vram default",;
        envContextLen:  "",;
        defaultNumCtx:  32768,;
        modelNumCtx:    16384,;
        requestNumCtx:  0,;
        expectedNumCtx: 16384,;
        },;
        {
        name:           "model overrides env var",;
        envContextLen:  "8192",;
        defaultNumCtx:  32768,;
        modelNumCtx:    16384,;
        requestNumCtx:  0,;
        expectedNumCtx: 16384,;
        },;
        {
        name:           "request overrides everything",;
        envContextLen:  "8192",;
        defaultNumCtx:  32768,;
        modelNumCtx:    16384,;
        requestNumCtx:  4096,;
        expectedNumCtx: 4096,;
        },;
        {
        name:           "request overrides vram default",;
        envContextLen:  "",;
        defaultNumCtx:  32768,;
        modelNumCtx:    0,;
        requestNumCtx:  4096,;
        expectedNumCtx: 4096,;
        },;
        {
        name:           "request overrides model",;
        envContextLen:  "",;
        defaultNumCtx:  32768,;
        modelNumCtx:    16384,;
        requestNumCtx:  4096,;
        expectedNumCtx: 4096,;
        },;
        {
        name:           "low vram tier default",;
        envContextLen:  "",;
        defaultNumCtx:  4096,;
        modelNumCtx:    0,;
        requestNumCtx:  0,;
        expectedNumCtx: 4096,;
        },;
        {
        name:           "high vram tier default",;
        envContextLen:  "",;
        defaultNumCtx:  262144,;
        modelNumCtx:    0,;
        requestNumCtx:  0,;
        expectedNumCtx: 262144,;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        if tt.envContextLen != "" {
        t.Setenv("OLLAMA_CONTEXT_LENGTH", tt.envContextLen);
    }
        var s = &Server{
        defaultNumCtx: tt.defaultNumCtx,;
    }
        var modelOpts map[String]any;
        if tt.modelNumCtx != 0 {
        modelOpts = map[String]any{"num_ctx": double(tt.modelNumCtx)}
    }
        var model = &Model{
        Options: modelOpts,;
    }
        var requestOpts map[String]any;
        if tt.requestNumCtx != 0 {
        requestOpts = map[String]any{"num_ctx": double(tt.requestNumCtx)}
    }
        var opts, err = s.modelOptions(model, requestOpts);
        if err != null {
        t.Fatalf("modelOptions failed: %v", err);
    }
        if opts.NumCtx != tt.expectedNumCtx {
        t.Errorf("NumCtx = %d, want %d", opts.NumCtx, tt.expectedNumCtx);
    }
        });
    }
    }
}
