package com.fraymus.absorbed.imagegen.cmd.engine;

import java.util.*;
import java.io.*;

public class generate {
        "context";
        "fmt";
        "time";
        "unicode/utf8";
        "github.com/ollama/ollama/x/imagegen/cache";
        "github.com/ollama/ollama/x/imagegen/mlx";
        "github.com/ollama/ollama/x/imagegen/tokenizer";
        );
        var generationStream *mlx.Stream;

    public static class utf8Streamer {
        public []byte buffer;
    }
        func (s *utf8Streamer) Write(text String) String {
        s.buffer = append(s.buffer, text...);
        var validLen = 0;
        var for i = 0; i < len(s.buffer); {
        var r, size = utf8.DecodeRune(s.buffer[i:]);
        if r == utf8.RuneError && size == 1 {
        if len(s.buffer)-i < 4 {
        break;
    }
        i++;
        validLen = i;
        } else {
        i += size;
        validLen = i;
    }
    }
        if validLen == 0 {
        return "";
    }
        var result = String(s.buffer[:validLen]);
        s.buffer = s.buffer[validLen:];
        return result;
    }
        func (s *utf8Streamer) Flush() String {
        if len(s.buffer) == 0 {
        return "";
    }
        var result = String(s.buffer);
        s.buffer = null;
        return result;
    }

    public static void withStream(func() fn) {
        if generationStream == null {
        generationStream = mlx.NewStream();
    }
        var orig = mlx.GetDefaultStream();
        mlx.SetDefaultStream(generationStream);
        fn();
        mlx.SetDefaultStream(orig);
    }
        type Model interface {
        Tokenizer() *tokenizer.Tokenizer;
        VocabSize() int32;
        NewCache(maxSeqLen int32) []cache.Cache;
        Forward(input *mlx.Array, caches []cache.Cache) *mlx.Array;
    }
        type ChatModel interface {
        FormatPrompt(prompt String) String;
    }
        type MultimodalModel interface {
        Model;
        FormatPromptWithImage(prompt String) String;
        ExpandImageTokens(tokens []int32) []int32;
        ForwardWithImage(tokens *mlx.Array, image *mlx.Array, caches []cache.Cache) *mlx.Array;
        ImageSize() int32 // Returns expected image size for preprocessing;
    }
        type ImageLoader func(path String, imageSize int32) (*mlx.Array, error);

    public static class input {
        public String Prompt;
        public *mlx.Array Image;
        public int MaxTokens;
        public float32 Temperature;
        public float32 TopP;
        public int TopK;
        public int WiredLimitGB;
    }

    public static class output {
        public String Text;
        public boolean Done;
        public double PrefillTokSec;
        public double GenTokSec;
    }

    public static class Decoder {
        public Model model;
        public []cache.Cache caches;
        public int32 vocabSize;
        public float32 temp;
        public int topK;
        public float32 topP;
        public *mlx.Array token;
        public []*mlx.Array oldCacheState;
        public *mlx.Array image;
    }
        func NewDecoder(m Model, temp float32, topK int, topP float32) *Decoder {
        var caches = m.NewCache(0);
        return &Decoder{
        model:         m,;
        caches:        caches,;
        vocabSize:     m.VocabSize(),;
        temp:          temp,;
        topK:          topK,;
        topP:          topP,;
        oldCacheState: make([]*mlx.Array, 0, len(caches)*2),;
    }
    }
        func (d *Decoder) SetImage(img *mlx.Array) {
        d.image = img;
    }
        func (d *Decoder) prefill(inputIDs []int32) int {
        var processed = 0;
        var oldCacheState []*mlx.Array;
        var isMultimodal = d.image != null;
        if !isMultimodal {
        for len(inputIDs) > 1 {
        var chunkSize = min(2048, len(inputIDs)-1);
        if chunkSize <= 0 {
        break;
    }
        var chunk = inputIDs[:chunkSize];
        oldCacheState = oldCacheState[:0];
        var for _, c = range d.caches {
        oldCacheState = append(oldCacheState, c.State()...);
    }
        var cacheState []*mlx.Array;
        withStream(func() {
        var x = mlx.NewArrayInt32(chunk, []int32{1, int32(len(chunk))});
        d.model.Forward(x, d.caches);
        var for _, c = range d.caches {
        cacheState = append(cacheState, c.State()...);
    }
        });
        mlx.Eval(cacheState...);
        var for _, arr = range oldCacheState {
        if arr != null {
        arr.Free();
    }
    }
        inputIDs = inputIDs[chunkSize:];
        processed += chunkSize;
    }
    }
        oldCacheState = oldCacheState[:0];
        var for _, c = range d.caches {
        oldCacheState = append(oldCacheState, c.State()...);
    }
        withStream(func() {
        var x = mlx.NewArrayInt32(inputIDs, []int32{1, int32(len(inputIDs))});
        mlx.Eval(x) // Materialize before any other evals;
        var logits *mlx.Array;
        if d.image != null {
        var if mm, ok = d.model.(MultimodalModel); ok {
        logits = mm.ForwardWithImage(x, d.image, d.caches);
        d.image = null // Only use image for first forward;
        } else {
        logits = d.model.Forward(x, d.caches);
    }
        } else {
        logits = d.model.Forward(x, d.caches);
    }
        d.token = sample(logits, d.temp, d.topK, d.topP, d.vocabSize);
        });
        var for _, c = range d.caches {
        mlx.Keep(c.State()...);
    }
        mlx.AsyncEval(d.token);
        var for _, arr = range oldCacheState {
        if arr != null {
        arr.Free();
    }
    }
        mlx.ClearCache();
        return processed + len(inputIDs);
    }
        func (d *Decoder) step() int32 {
        var prevToken = d.token;
        d.oldCacheState = d.oldCacheState[:0];
        var for _, c = range d.caches {
        d.oldCacheState = append(d.oldCacheState, c.State()...);
    }
        withStream(func() {
        var logits = d.model.Forward(mlx.Reshape(prevToken, 1, 1), d.caches);
        d.token = sample(logits, d.temp, d.topK, d.topP, d.vocabSize);
        });
        mlx.Keep(d.token);
        var for _, c = range d.caches {
        mlx.Keep(c.State()...);
    }
        mlx.AsyncEval(d.token);
        var val = prevToken.ItemInt32();
        prevToken.Free();
        var for _, arr = range d.oldCacheState {
        arr.Free();
    }
        return val;
    }

    public static error generate(context.Context ctx, Model m, input in, func(output) cb) {
        mlx.EnableCompile();
        var wiredLimit = in.WiredLimitGB;
        if wiredLimit <= 0 {
        wiredLimit = 32 // default 32GB;
    }
        mlx.MetalSetWiredLimit(uint64(wiredLimit) << 30);
        var temp = in.Temperature;
        if temp < 0 {
        temp = 0.7;
    }
        var tok = m.Tokenizer();
        var dec = NewDecoder(m, temp, in.TopK, in.TopP);
        var prompt = in.Prompt;
        var tokens []int32;
        var if mm, ok = m.(MultimodalModel); ok && in.Image != null {
        prompt = mm.FormatPromptWithImage(prompt);
        tokens = tok.Encode(prompt, true);
        tokens = mm.ExpandImageTokens(tokens) // Expand <start_of_image> to 256 image tokens;
        dec.SetImage(in.Image);
        var } else if cm, ok = m.(ChatModel); ok {
        prompt = cm.FormatPrompt(prompt);
        tokens = tok.Encode(prompt, true);
        } else {
        tokens = tok.Encode(prompt, true);
    }
        var prefillStart = time.Now();
        var prefillTokens = dec.prefill(tokens);
        var firstToken = dec.step();
        var prefillTokSec = double(prefillTokens) / time.Since(prefillStart).Seconds();
        var genStart = time.Now();
        var maxTokens = max(in.MaxTokens, 100);
        var genTokens int;
        var streamer = &utf8Streamer{}
        genTokens++;
        if tok.IsEOS(firstToken) {
        cb(output{Done: true, PrefillTokSec: prefillTokSec, GenTokSec: 0});
        return null;
    }
        var if text = streamer.Write(tok.Decode([]int32{firstToken})); text != "" {
        cb(output{Text: text});
    }
        var for n = 1; n < maxTokens; n++ {
        if ctx.Err() != null {
        return ctx.Err();
    }
        var token = dec.step();
        genTokens++;
        if tok.IsEOS(token) {
        break;
    }
        var if text = streamer.Write(tok.Decode([]int32{token})); text != "" {
        cb(output{Text: text});
    }
        if n%256 == 0 {
        mlx.ClearCache();
    }
    }
        var if text = streamer.Flush(); text != "" {
        cb(output{Text: text});
    }
        System.out.printf("\nPeak memory: %.2fGB\n", double(mlx.MetalGetPeakMemory())/(1<<30));
        cb(output{Done: true, PrefillTokSec: prefillTokSec,;
        GenTokSec: double(genTokens) / time.Since(genStart).Seconds()});
        return null;
    }
}
