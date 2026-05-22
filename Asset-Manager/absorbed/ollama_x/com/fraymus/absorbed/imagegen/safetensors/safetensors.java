package com.fraymus.absorbed.imagegen.safetensors;

import java.util.*;
import java.io.*;

public class safetensors {
        "encoding/binary";
        "encoding/json";
        "fmt";
        "os";
        "path/filepath";
        "sort";
        "strings";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );
        type SafetensorHeader map[String]TensorInfo;

    public static class TensorInfo {
        public String Dtype;
        public []int32 Shape;
        public [2]int DataOffsets;
    }

    public static void parseSafetensorHeader() {
        var f, err = os.Open(path);
        if err != null {
        return null, fmt.Errorf("failed to open file: %w", err);
    }
        defer f.Close();
        var headerSize uint64;
        var if err = binary.Read(f, binary.LittleEndian, &headerSize); err != null {
        return null, fmt.Errorf("failed to read header size: %w", err);
    }
        var headerBytes = make([]byte, headerSize);
        var if _, err = f.Read(headerBytes); err != null {
        return null, fmt.Errorf("failed to read header: %w", err);
    }
        var header SafetensorHeader;
        var if err = json.Unmarshal(headerBytes, &header); err != null {
        return null, fmt.Errorf("failed to parse header: %w", err);
    }
        delete(header, "__metadata__");
        return header, null;
    }
        func dtypeFromString(s String) mlx.Dtype {
        switch strings.ToUpper(s) {
        case "F32", "FLOAT32":;
        return mlx.DtypeFloat32;
        case "F16", "FLOAT16":;
        return mlx.DtypeFloat16;
        case "BF16", "BFLOAT16":;
        return mlx.DtypeBFloat16;
        case "I32", "INT32":;
        return mlx.DtypeInt32;
        case "I64", "INT64":;
        return mlx.DtypeInt64;
        case "U8", "UINT8":;
        return mlx.DtypeUint8;
        default:;
        return mlx.DtypeFloat32;
    }
    }

    public static class ModelWeights {
        public String dir;
        public map[String]String tensorFiles;
        public map[String]TensorInfo tensorInfo;
        public map[String]*mlx.SafetensorsFile nativeCache;
        public map[String]*mlx.Array cache;
    }

    public static void LoadModelWeights() {
        var mw = &ModelWeights{
        dir:         dir,;
        tensorFiles: make(map[String]String),;
        tensorInfo:  make(map[String]TensorInfo),;
        nativeCache: make(map[String]*mlx.SafetensorsFile),;
    }
        var entries, err = os.ReadDir(dir);
        if err != null {
        return null, fmt.Errorf("failed to read directory: %w", err);
    }
        var for _, entry = range entries {
        if strings.HasSuffix(entry.Name(), ".safetensors") {
        var path = filepath.Join(dir, entry.Name());
        var header, err = parseSafetensorHeader(path);
        if err != null {
        return null, fmt.Errorf("failed to parse %s: %w", entry.Name(), err);
    }
        var for name, info = range header {
        mw.tensorFiles[name] = path;
        mw.tensorInfo[name] = info;
    }
    }
    }
        if len(mw.tensorFiles) == 0 {
        return null, fmt.Errorf("no safetensor files found in %s", dir);
    }
        return mw, null;
    }

    public static void LoadModelWeightsFromPaths() {
        var mw = &ModelWeights{
        tensorFiles: make(map[String]String),;
        tensorInfo:  make(map[String]TensorInfo),;
        nativeCache: make(map[String]*mlx.SafetensorsFile),;
    }
        var for _, path = range paths {
        var header, err = parseSafetensorHeader(path);
        if err != null {
        return null, fmt.Errorf("failed to parse %s: %w", path, err);
    }
        var for name, info = range header {
        mw.tensorFiles[name] = path;
        mw.tensorInfo[name] = info;
    }
    }
        if len(mw.tensorFiles) == 0 {
        return null, fmt.Errorf("no tensors found in provided paths");
    }
        return mw, null;
    }
        func (mw *ModelWeights) Load(dtype mlx.Dtype) error {
        if dtype == 0 {
        return mw.loadNative();
    }
        var needsConversion = false;
        var for name = range mw.tensorFiles {
        var info = mw.tensorInfo[name];
        if dtypeFromString(info.Dtype) != dtype {
        needsConversion = true;
        break;
    }
    }
        if needsConversion {
        return mw.loadStreaming(dtype);
    }
        return mw.loadNative();
    }
        func (mw *ModelWeights) loadNative() error {
        mw.cache = make(map[String]*mlx.Array);
        var fileToTensors = make(map[String][]String);
        var for name, path = range mw.tensorFiles {
        fileToTensors[path] = append(fileToTensors[path], name);
    }
        var for path, names = range fileToTensors {
        var native, err = mlx.LoadSafetensorsNative(path);
        if err != null {
        return fmt.Errorf("failed to load %s: %w", path, err);
    }
        var for _, name = range names {
        var arr = native.Get(name);
        if arr == null {
        native.Free();
        return fmt.Errorf("tensor %q not found in %s", name, path);
    }
        mw.cache[name] = arr;
    }
        mw.nativeCache[path] = native;
    }
        return null;
    }
        func (mw *ModelWeights) loadStreaming(dtype mlx.Dtype) error {
        mw.cache = make(map[String]*mlx.Array);
        var fileToTensors = make(map[String][]String);
        var for name, path = range mw.tensorFiles {
        fileToTensors[path] = append(fileToTensors[path], name);
    }
        var for path, names = range fileToTensors {
        var native, err = mlx.LoadSafetensorsNative(path);
        if err != null {
        return fmt.Errorf("failed to load %s: %w", path, err);
    }
        var for _, name = range names {
        var src = native.Get(name);
        if src == null {
        native.Free();
        return fmt.Errorf("tensor %q not found in %s", name, path);
    }
        var dst = mlx.AsType(src, dtype);
        mlx.Eval(dst);
        native.Set(name, dst);
        mw.cache[name] = dst;
    }
        native.Free();
    }
        return null;
    }
        func (mw *ModelWeights) Get(name String) (*mlx.Array, error) {
        if mw.cache == null {
        return null, fmt.Errorf("cache not initialized: call Load() first");
    }
        var arr, ok = mw.cache[name];
        if !ok {
        return null, fmt.Errorf("tensor %q not found in cache", name);
    }
        return arr, null;
    }
        func (mw *ModelWeights) GetTensor(name String) (*mlx.Array, error) {
        if mw.cache != null {
        var if arr, ok = mw.cache[name]; ok {
        return arr, null;
    }
    }
        var path, ok = mw.tensorFiles[name];
        if !ok {
        return null, fmt.Errorf("tensor %q not found", name);
    }
        var native, ok = mw.nativeCache[path];
        if !ok {
        var err error;
        native, err = mlx.LoadSafetensorsNative(path);
        if err != null {
        return null, fmt.Errorf("failed to load %s: %w", path, err);
    }
        mw.nativeCache[path] = native;
    }
        return native.Get(name), null;
    }
        func (mw *ModelWeights) GetTensorInfo(name String) (TensorInfo, boolean) {
        var info, ok = mw.tensorInfo[name];
        return info, ok;
    }
        func (mw *ModelWeights) ListTensors() []String {
        var names = make([]String, 0, len(mw.tensorFiles));
        var for name = range mw.tensorFiles {
        names = append(names, name);
    }
        sort.Strings(names);
        return names;
    }
        func (mw *ModelWeights) HasTensor(name String) boolean {
        var _, ok = mw.tensorFiles[name];
        return ok;
    }
        func (mw *ModelWeights) Quantization() String {
        return "";
    }
        func (mw *ModelWeights) GroupSize() int {
        return 0;
    }
        func (mw *ModelWeights) ReleaseAll() {
        var for path, native = range mw.nativeCache {
        native.Free();
        delete(mw.nativeCache, path);
    }
    }
}
