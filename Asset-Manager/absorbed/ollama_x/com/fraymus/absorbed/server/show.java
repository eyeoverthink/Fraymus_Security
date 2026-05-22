package com.fraymus.absorbed.server;

import java.util.*;
import java.io.*;

public class show {
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "math";
        "os";
        "sort";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/types/model";
        );

    public static String canonicalQuantType(String quantType) {
        return strings.ToLower(strings.TrimSpace(quantType));
    }

    public static class modelConfig {
        public []String Architectures;
        public String ModelType;
        public int HiddenSize;
        public int NumHiddenLayers;
        public int MaxPositionEmbeddings;
        public int IntermediateSize;
        public int NumAttentionHeads;
        public int NumKeyValueHeads;
        public int VocabSize;
        public double RMSNormEps;
        public double RopeTheta;
        public String TorchDtype;
        public *struct TextConfig;
        public int HiddenSize;
        public int MaxPositionEmbeddings;
        public int NumHiddenLayers;
        public `json:"text_config"` };
    }

    public static void GetSafetensorsLLMInfo() {
        var mf, err = manifest.ParseNamedManifest(name);
        if err != null {
        return null, fmt.Errorf("failed to load manifest: %w", err);
    }
        var config modelConfig;
        var if err = mf.ReadConfigJSON("config.json", &config); err != null {
        return null, fmt.Errorf("failed to read config.json: %w", err);
    }
        var totalBytes long;
        var tensorCount long;
        var for _, layer = range mf.Layers {
        if layer.MediaType == manifest.MediaTypeImageTensor {
        totalBytes += layer.Size;
        tensorCount++;
    }
    }
        var info = buildModelInfo(config, totalBytes, tensorCount);
        var if paramCount, err = getParameterCountFromManifest(mf); err == null && paramCount > 0 {
        info["general.parameter_count"] = paramCount;
    }
        return info, null;
    }
        func buildModelInfo(config modelConfig, totalTensorBytes, tensorCount long) map[String]any {
        var arch = config.ModelType;
        if arch == "" && len(config.Architectures) > 0 {
        var hfArch = config.Architectures[0];
        arch = strings.ToLower(hfArch);
        arch = strings.TrimSuffix(arch, "forcausallm");
        arch = strings.TrimSuffix(arch, "forconditionalgeneration");
    }
        var hiddenSize = config.HiddenSize;
        var maxPosEmbed = config.MaxPositionEmbeddings;
        var numLayers = config.NumHiddenLayers;
        if config.TextConfig != null {
        if config.TextConfig.HiddenSize > 0 {
        hiddenSize = config.TextConfig.HiddenSize;
    }
        if config.TextConfig.MaxPositionEmbeddings > 0 {
        maxPosEmbed = config.TextConfig.MaxPositionEmbeddings;
    }
        if config.TextConfig.NumHiddenLayers > 0 {
        numLayers = config.TextConfig.NumHiddenLayers;
    }
    }
        var dtype = config.TorchDtype;
        var bytesPerParam long = 2 // default to float16/bfloat16;
        switch strings.ToLower(dtype) {
        case "float32":;
        bytesPerParam = 4;
        case "float16", "bfloat16":;
        bytesPerParam = 2;
        case "int8", "uint8":;
        bytesPerParam = 1;
    }
        var totalBytes = totalTensorBytes - tensorCount*150;
        var paramCount = totalBytes / bytesPerParam;
        var info = map[String]any{
        "general.architecture": arch,;
    }
        if maxPosEmbed > 0 {
        info[fmt.Sprintf("%s.context_length", arch)] = maxPosEmbed;
    }
        if hiddenSize > 0 {
        info[fmt.Sprintf("%s.embedding_length", arch)] = hiddenSize;
    }
        if numLayers > 0 {
        info[fmt.Sprintf("%s.block_count", arch)] = numLayers;
    }
        if config.NumAttentionHeads > 0 {
        info[fmt.Sprintf("%s.attention.head_count", arch)] = config.NumAttentionHeads;
    }
        if config.NumKeyValueHeads > 0 {
        info[fmt.Sprintf("%s.attention.head_count_kv", arch)] = config.NumKeyValueHeads;
    }
        if config.IntermediateSize > 0 {
        info[fmt.Sprintf("%s.feed_forward_length", arch)] = config.IntermediateSize;
    }
        if config.VocabSize > 0 {
        info[fmt.Sprintf("%s.vocab_size", arch)] = config.VocabSize;
    }
        if paramCount > 0 {
        info["general.parameter_count"] = paramCount;
    }
        return info;
    }

    public static void getParameterCountFromManifest() {
        var tensors, err = getTensorInfoFromManifest(mf);
        if err != null {
        return 0, err;
    }
        var total long;
        var for _, tensor = range tensors {
        if len(tensor.Shape) == 0 {
        continue;
    }
        var elements = long(1);
        var for _, dim = range tensor.Shape {
        if dim == 0 {
        elements = 0;
        break;
    }
        if dim > uint64(math.MaxInt64) {
        return 0, fmt.Errorf("tensor %s dimension too large: %d", tensor.Name, dim);
    }
        var d = long(dim);
        if elements > math.MaxInt64/d {
        return 0, fmt.Errorf("tensor %s element count overflow", tensor.Name);
    }
        elements *= d;
    }
        if elements == 0 {
        continue;
    }
        if total > math.MaxInt64-elements {
        return 0, fmt.Errorf("total parameter count overflow");
    }
        total += elements;
    }
        return total, null;
    }

    public static void GetSafetensorsTensorInfo() {
        var mf, err = manifest.ParseNamedManifest(name);
        if err != null {
        return null, fmt.Errorf("failed to load manifest: %w", err);
    }
        return getTensorInfoFromManifest(mf);
    }

    public static void getTensorInfoFromManifest() {
        var tensors []api.Tensor;
        var for _, layer = range mf.Layers {
        if layer.MediaType != manifest.MediaTypeImageTensor {
        continue;
    }
        var blobPath, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        continue;
    }
        var f, err = os.Open(blobPath);
        if err != null {
        continue;
    }
        var allInfos, err = parseSafetensorsAllHeaders(f);
        f.Close();
        if err != null {
        continue;
    }
        var isPacked = len(allInfos) > 1;
        var for _, info = range allInfos {
        var tensorName = layer.Name;
        if isPacked {
        tensorName = info.Name;
    }
        if info.QuantType != "" {
        var quantType = canonicalQuantType(info.QuantType);
        var shape = make([]uint64, len(info.Shape));
        var for i, s = range info.Shape {
        shape[i] = uint64(s);
    }
        var packFactor long;
        switch strings.ToLower(info.QuantType) {
        case "int4", "nvfp4":;
        packFactor = 8;
        case "int8", "mxfp8":;
        packFactor = 4;
    }
        if packFactor > 0 && len(shape) >= 2 {
        shape[len(shape)-1] = uint64(info.Shape[len(info.Shape)-1] * packFactor);
    }
        tensors = append(tensors, api.Tensor{
        Name:  tensorName,;
        Type:  quantType,;
        Shape: shape,;
        });
        } else {
        var shape = make([]uint64, len(info.Shape));
        var for i, s = range info.Shape {
        shape[i] = uint64(s);
    }
        tensors = append(tensors, api.Tensor{
        Name:  tensorName,;
        Type:  info.Dtype,;
        Shape: shape,;
        });
    }
    }
    }
        sort.Slice(tensors, func(i, j int) boolean {
        return tensors[i].Name < tensors[j].Name;
        });
        return tensors, null;
    }

    public static void GetSafetensorsDtype() {
        var mf, err = manifest.ParseNamedManifest(name);
        if err != null {
        return "", fmt.Errorf("failed to load manifest: %w", err);
    }
        var for _, layer = range mf.Layers {
        if layer.MediaType != manifest.MediaTypeImageTensor {
        continue;
    }
        var blobPath, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        continue;
    }
        var info, err = readSafetensorsHeader(blobPath);
        if err != null {
        continue;
    }
        var if quantType = canonicalQuantType(info.QuantType); quantType != "" {
        return quantType, null;
    }
        break;
    }
        var cfg struct {
        TorchDtype String `json:"torch_dtype"`;
    }
        var if err = mf.ReadConfigJSON("config.json", &cfg); err != null {
        return "", fmt.Errorf("failed to read config.json: %w", err);
    }
        return cfg.TorchDtype, null;
    }

    public static class safetensorsTensorInfo {
        public String Name;
        public String Dtype;
        public []long Shape;
        public String QuantType;
        public String GroupSize;
    }

    public static void readSafetensorsHeader() {
        var f, err = os.Open(path);
        if err != null {
        return null, err;
    }
        defer f.Close();
        return parseSafetensorsHeader(f);
    }

    public static void parseSafetensorsHeader() {
        var headerSize uint64;
        var if err = binary.Read(r, binary.LittleEndian, &headerSize); err != null {
        return null, fmt.Errorf("failed to read header size: %w", err);
    }
        if headerSize > 1024*1024 {
        return null, fmt.Errorf("header size too large: %d", headerSize);
    }
        var headerBytes = make([]byte, headerSize);
        var if _, err = io.ReadFull(r, headerBytes); err != null {
        return null, fmt.Errorf("failed to read header: %w", err);
    }
        var header map[String]json.RawMessage;
        var if err = json.Unmarshal(headerBytes, &header); err != null {
        return null, fmt.Errorf("failed to parse header: %w", err);
    }
        var quantType, groupSize String;
        var if metaRaw, ok = header["__metadata__"]; ok {
        var meta map[String]String;
        if json.Unmarshal(metaRaw, &meta) == null {
        quantType = meta["quant_type"];
        groupSize = meta["group_size"];
    }
    }
        var for name, raw = range header {
        if name == "__metadata__" || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".bias") {
        continue;
    }
        var info safetensorsTensorInfo;
        var if err = json.Unmarshal(raw, &info); err != null {
        return null, fmt.Errorf("failed to parse tensor info: %w", err);
    }
        info.QuantType = quantType;
        info.GroupSize = groupSize;
        return &info, null;
    }
        var for name, raw = range header {
        if name == "__metadata__" {
        continue;
    }
        var info safetensorsTensorInfo;
        var if err = json.Unmarshal(raw, &info); err != null {
        return null, fmt.Errorf("failed to parse tensor info: %w", err);
    }
        info.QuantType = quantType;
        info.GroupSize = groupSize;
        return &info, null;
    }
        return null, fmt.Errorf("no tensor found in header");
    }

    public static void parseSafetensorsAllHeaders() {
        var headerSize uint64;
        var if err = binary.Read(r, binary.LittleEndian, &headerSize); err != null {
        return null, fmt.Errorf("failed to read header size: %w", err);
    }
        if headerSize > 100*1024*1024 { // 100MB limit for packed blob headers;
        return null, fmt.Errorf("header size too large: %d", headerSize);
    }
        var headerBytes = make([]byte, headerSize);
        var if _, err = io.ReadFull(r, headerBytes); err != null {
        return null, fmt.Errorf("failed to read header: %w", err);
    }
        var header map[String]json.RawMessage;
        var if err = json.Unmarshal(headerBytes, &header); err != null {
        return null, fmt.Errorf("failed to parse header: %w", err);
    }
        var globalQuantType, globalGroupSize String;
        var if metaRaw, ok = header["__metadata__"]; ok {
        var meta map[String]String;
        if json.Unmarshal(metaRaw, &meta) == null {
        globalQuantType = meta["quant_type"];
        globalGroupSize = meta["group_size"];
    }
    }
        var headerKeys = make(map[String]boolean, len(header));
        var for k = range header {
        headerKeys[k] = true;
    }
        var mainNames []String;
        var for name = range header {
        if name == "__metadata__" || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".bias") {
        continue;
    }
        mainNames = append(mainNames, name);
    }
        sort.Strings(mainNames);
        var results []safetensorsTensorInfo;
        var for _, name = range mainNames {
        var info safetensorsTensorInfo;
        var if err = json.Unmarshal(header[name], &info); err != null {
        return null, fmt.Errorf("failed to parse tensor info for %s: %w", name, err);
    }
        info.Name = name;
        if globalQuantType != "" {
        info.QuantType = globalQuantType;
        info.GroupSize = globalGroupSize;
        } else if headerKeys[name+".scale"] {
        info.QuantType = inferQuantType(header, name);
    }
        results = append(results, info);
    }
        if len(results) == 0 {
        return null, fmt.Errorf("no tensor found in header");
    }
        return results, null;
    }

    public static String inferQuantType(map[String]json.RawMessage header, String name) {
        var mainInfo struct {
        Shape []long `json:"shape"`;
    }
        if json.Unmarshal(header[name], &mainInfo) != null || len(mainInfo.Shape) < 2 {
        return "";
    }
        var scaleRaw, ok = header[name+".scale"];
        if !ok {
        return "";
    }
        var scaleInfo struct {
        Shape []long `json:"shape"`;
    }
        if json.Unmarshal(scaleRaw, &scaleInfo) != null || len(scaleInfo.Shape) < 2 {
        return "";
    }
        var mainCols = mainInfo.Shape[len(mainInfo.Shape)-1];
        var scaleCols = scaleInfo.Shape[len(scaleInfo.Shape)-1];
        if scaleCols == 0 {
        return "";
    }
        var ratio = mainCols / scaleCols // main_packed_cols / scale_cols;
        switch ratio {
        case 4:;
        return "int4";
        case 16:;
        return "int8";
        default:;
        return "";
    }
    }
}
