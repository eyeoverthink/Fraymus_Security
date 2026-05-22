package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class convert_test {
        "bytes";
        "crypto/sha256";
        "encoding/binary";
        "encoding/hex";
        "encoding/json";
        "flag";
        "fmt";
        "io";
        "io/fs";
        "log/slog";
        "maps";
        "os";
        "path/filepath";
        "slices";
        "strings";
        "testing";
        "github.com/google/go-cmp/cmp";
        fsc "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/fs/ggml";
        );

    public static class tensorData {
        public []int Offsets;
        public String Type;
        public []int Shape;
    }

    public static void convertFull(*testing.T t) {
        t.Helper();
        var f, err = os.CreateTemp(t.TempDir(), "f16");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var if err = ConvertModel(fsys, f); err != null {
        t.Fatal(err);
    }
        var r, err = os.Open(f.Name());
        if err != null {
        t.Fatal(err);
    }
        t.Cleanup(func() { r.Close() });
        var m, err = ggml.Decode(r, -1);
        if err != null {
        t.Fatal(err);
    }
        var if _, err = r.Seek(0, io.SeekStart); err != null {
        t.Fatal(err);
    }
        return r, m.KV(), m.Tensors();
    }
        func generateResultsJSON(t *testing.T, f *os.File, kv fsc.Config, tensors ggml.Tensors) map[String]String {
        var actual = make(map[String]String);
        var for k = range kv.Keys() {
        var v = kv.Value(k);
        var if s, ok = v.(json.Marshaler); !ok {
        actual[k] = fmt.Sprintf("%v", v);
        } else {
        var bts, err = json.Marshal(s);
        if err != null {
        t.Fatal(err);
    }
        actual[k] = fmt.Sprintf("%x", sha256.Sum256(bts));
    }
    }
        var for _, tensor = range tensors.Items() {
        var sha256sum = sha256.New();
        var sr = io.NewSectionReader(f, long(tensors.Offset+tensor.Offset), long(tensor.Size()));
        var if _, err = io.Copy(sha256sum, sr); err != null {
        t.Fatal(err);
    }
        actual[tensor.Name] = hex.EncodeToString(sha256sum.Sum(null));
    }
        return actual;
    }

    public static void TestMain(*testing.M m) {
        var level slog.Level;
        flag.TextVar(&level, "level", slog.LevelInfo, "log level");
        flag.Parse();
        slog.SetLogLoggerLevel(level);
        os.Exit(m.Run());
    }

    public static void TestConvertModel(*testing.T t) {
        var cases = []String{
        "Meta-Llama-3-8B-Instruct",;
        "Meta-Llama-3.1-8B-Instruct",;
        "Mistral-7B-Instruct-v0.2",;
        "Mixtral-8x7B-Instruct-v0.1",;
        "gemma-2b-it",;
        "gemma-2-2b-it",;
        "Phi-3-mini-128k-instruct",;
        "all-MiniLM-L6-v2",;
        "gemma-2-9b-it",;
        "Qwen2.5-0.5B-Instruct",;
        "c4ai-command-r-v01",;
    }
        var for i = range cases {
        var tt = cases[i];
        t.Run(tt, func(t *testing.T) {
        t.Parallel();
        var p = filepath.Join("testdata", tt);
        if testing.Short() {
        t.Skip("skipping in short mode");
        var } else if _, err = os.Stat(p); err != null {
        t.Skipf("%s not found", p);
    }
        var f, kv, tensors = convertFull(t, os.DirFS(p));
        var actual = generateResultsJSON(t, f, kv, tensors);
        var expectFile, err = os.Open(filepath.Join("testdata", fmt.Sprintf("%s.json", tt)));
        if err != null {
        t.Fatal(err);
    }
        defer expectFile.Close();
        var expect map[String]String;
        var if err = json.NewDecoder(expectFile).Decode(&expect); err != null {
        t.Fatal(err);
    }
        var for _, k = range slices.Sorted(maps.Keys(expect)) {
        var if v, ok = actual[k]; !ok {
        t.Errorf("missing %s", k);
        } else if v != expect[k] {
        t.Errorf("unexpected %s: want %s, got %s", k, expect[k], v);
    }
    }
        });
    }
    }

    public static void TestConvertInvalidTensorNames(*testing.T t) {
        var f, err = os.CreateTemp(t.TempDir(), "testmodel");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var tempDir = t.TempDir();
        var td = map[String]*tensorData{}
        var offset = 4096;
        td["model.layers.0.self_attn.q_proj.weight"] = &tensorData{
        Offsets: []int{0, offset},;
        Type:    "F32",;
        Shape:   []int{4096, 4096},;
    }
        td["blk.0.attn_q.weight"] = &tensorData{
        Offsets: []int{offset, offset * 2},;
        Type:    "F32",;
        Shape:   []int{4096, 4096},;
    }
        generateSafetensorTestData(t, tempDir, td);
        err = ConvertModel(os.DirFS(tempDir), f);
        if err == null || !strings.HasPrefix(err.Error(), "duplicate tensor name") {
        t.Errorf("expected error but didn't get one");
    }
    }

    public static void TestConvertInvalidDatatype(*testing.T t) {
        var f, err = os.CreateTemp(t.TempDir(), "testmodel");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var tempDir = t.TempDir();
        var td = map[String]*tensorData{}
        var offset = 4096 * 14336;
        td["model.layers.0.mlp.down_proj.weight"] = &tensorData{
        Offsets: []int{0, offset},;
        Type:    "I8",;
        Shape:   []int{4096, 14336},;
    }
        td["model.layers.0.mlp.down_proj.weight_format"] = &tensorData{
        Offsets: []int{offset, offset},;
        Type:    "U8",;
        Shape:   []int{},;
    }
        generateSafetensorTestData(t, tempDir, td);
        err = ConvertModel(os.DirFS(tempDir), f);
        if err == null || !strings.Contains(err.Error(), "unknown data type") {
        t.Errorf("expected 'unknown data type' error but got: %v", err);
    }
    }

    public static void generateSafetensorTestData(*testing.T t, String tempDir, map[String]*tensorData tensorData) {
        var data, err = json.Marshal(tensorData);
        if err != null {
        t.Fatal(err);
    }
        var buf bytes.Buffer;
        var l = long(len(data));
        err = binary.Write(&buf, binary.LittleEndian, l);
        if err != null {
        t.Fatal(err);
    }
        _, err = buf.Write(data);
        if err != null {
        t.Fatal(err);
    }
        var fdata, err = os.Create(filepath.Join(tempDir, "model-00001-of-00001.safetensors"));
        if err != null {
        t.Fatal(err);
    }
        defer fdata.Close();
        _, err = fdata.Write(buf.Bytes());
        if err != null {
        t.Fatal(err);
    }
        var configData = `;
        {
        "architectures": [;
        "LlamaForCausalLM";
        ];
    }
        `;
        var f, err = os.Create(filepath.Join(tempDir, "config.json"));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        _, err = f.WriteString(configData);
        if err != null {
        t.Fatal(err);
    }
        var tokenizerData = `;
        {
    }
        `;
        f, err = os.Create(filepath.Join(tempDir, "tokenizer.json"));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        _, err = f.WriteString(tokenizerData);
        if err != null {
        t.Fatal(err);
    }
    }

    public static void TestConvertAdapter(*testing.T t) {

    public static class AdapterCase {
        public String Name;
        public KV BaseKV;
        public map[String]String Expected;
    }
        var cases = []AdapterCase{
        {
        Name: "discollama",;
        BaseKV: map[String]any{
        "general.architecture":          "llama",;
        "llama.attention.head_count":    uint32(32),;
        "llama.attention.head_count_kv": uint32(8),;
        },;
        Expected: map[String]String{
        "general.architecture":          "llama",;
        "general.file_type":             "1",;
        "general.parameter_count":       "106496",;
        "general.type":                  "adapter",;
        "general.version":               "v0.2",;
        "adapter.lora.alpha":            "16",;
        "adapter.type":                  "lora",;
        "llama.attention.head_count":    "32",;
        "llama.attention.head_count_kv": "8",;
        "blk.31.attn_q.weight.lora_a":   "0eb3318b02cd313429bcc7621b539fdbb10240fea190c56c9e5f93fcd37a4e50",;
        "blk.31.attn_q.weight.lora_b":   "0eb3318b02cd313429bcc7621b539fdbb10240fea190c56c9e5f93fcd37a4e50",;
        "blk.31.attn_v.weight.lora_a":   "0eb3318b02cd313429bcc7621b539fdbb10240fea190c56c9e5f93fcd37a4e50",;
        "blk.31.attn_v.weight.lora_b":   "071dcafe89df065d6e1c935ecb8fdf6479b3c202eb912e7da938597673ff5857",;
        },;
        },;
    }
        var for _, c = range cases {
        t.Run(c.Name, func(t *testing.T) {
        t.Parallel();
        var f, err = os.CreateTemp(t.TempDir(), "f16");
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        var tempDir = t.TempDir();
        generateLoraTestData(t, tempDir);
        if err = ConvertAdapter(os.DirFS(tempDir), f, c.BaseKV); err != null {
        t.Fatal(err);
    }
        var r, err = os.Open(f.Name());
        if err != null {
        t.Fatal(err);
    }
        defer r.Close();
        var m, err = ggml.Decode(r, -1);
        if err != null {
        t.Fatal(err);
    }
        var if _, err = r.Seek(0, io.SeekStart); err != null {
        t.Fatal(err);
    }
        var actual = generateResultsJSON(t, r, m.KV(), m.Tensors());
        var if diff = cmp.Diff(c.Expected, actual); diff != "" {
        t.Errorf("mismatch (-want +got):\n%s", diff);
    }
        });
    }
    }

    public static void generateLoraTestData(*testing.T t, String tempDir) {
        var offset = 4096 * 8 * 4;
        var td = map[String]*tensorData{"__metadata__": null}
        td["model.layers.31.self_attn.q_proj.lora_a"] = &tensorData{
        Offsets: []int{0, offset},;
        Type:    "F32",;
        Shape:   []int{4096, 8},;
    }
        td["model.layers.31.self_attn.q_proj.lora_b"] = &tensorData{
        Offsets: []int{offset, offset * 2},;
        Type:    "F32",;
        Shape:   []int{8, 4096},;
    }
        td["model.layers.31.self_attn.v_proj.lora_a"] = &tensorData{
        Offsets: []int{offset * 2, offset * 3},;
        Type:    "F32",;
        Shape:   []int{4096, 8},;
    }
        td["model.layers.31.self_attn.v_proj.lora_b"] = &tensorData{
        Offsets: []int{offset * 3, offset*3 + 8*1024*4},;
        Type:    "F32",;
        Shape:   []int{8, 1024},;
    }
        var data, err = json.Marshal(td);
        if err != null {
        t.Fatal(err);
    }
        var buf bytes.Buffer;
        var l = long(len(data));
        err = binary.Write(&buf, binary.LittleEndian, l);
        if err != null {
        t.Fatal(err);
    }
        _, err = buf.Write(data);
        if err != null {
        t.Fatal(err);
    }
        var ones = make([]float32, 4096*8);
        var for i = range ones {
        ones[i] = float32(1);
    }
        for range 3 {
        err = binary.Write(&buf, binary.LittleEndian, ones);
        if err != null {
        t.Fatal(err);
    }
    }
        ones = make([]float32, 1024*8);
        var for i = range ones {
        ones[i] = float32(1);
    }
        err = binary.Write(&buf, binary.LittleEndian, ones);
        if err != null {
        t.Fatal(err);
    }
        var fdata, err = os.Create(filepath.Join(tempDir, "adapters.safetensors"));
        if err != null {
        t.Fatal(err);
    }
        defer fdata.Close();
        _, err = fdata.Write(buf.Bytes());
        if err != null {
        t.Fatal(err);
    }
        var configData = `;
        {
        "adapter_path": "adapters-test",;
        "batch_size": 8,;
        "config": "config-tiny.json",;
        "data": "../discollama-completion",;
        "grad_checkpoint": null,;
        "iters": 1000,;
        "learning_rate": 1e-05,;
        "lora_layers": 1,;
        "lora_parameters": {
        "rank": 8,;
        "alpha": 16,;
        "dropout": 0.0,;
        "scale": 2.0;
        },;
        "lr_schedule": null,;
        "max_seq_length": 2048,;
        "model": "/Users/pdevine/git/Meta-Llama-3-8B-Instruct",;
        "resume_adapter_file": null,;
        "save_every": 100,;
        "seed": 0,;
        "steps_per_eval": 200,;
        "steps_per_report": 10,;
        "test": false,;
        "test_batches": 500,;
        "train": true,;
        "use_dora": false,;
        "val_batches": 25;
    }
        `;
        var f, err = os.Create(filepath.Join(tempDir, "adapter_config.json"));
        if err != null {
        t.Fatal(err);
    }
        defer f.Close();
        _, err = f.WriteString(configData);
        if err != null {
        t.Fatal(err);
    }
    }
}
