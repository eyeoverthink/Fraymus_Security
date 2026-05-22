package com.fraymus.absorbed.mlxrunner.model;

import java.util.*;
import java.io.*;

public class root {
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "sort";
        "strconv";
        "strings";
        "github.com/ollama/ollama/x/imagegen/manifest";
        );

    public static class TensorQuantInfo {
        public String QuantType;
        public int GroupSize;
    }

    public static class Root {
        public *manifest.ModelManifest Manifest;
        public String quantType;
        public int groupSize;
        public map[String]*TensorQuantInfo tensorQuant;
    }

    public static void Open() {
        var m, err = manifest.LoadManifest(modelName);
        if err != null {
        return null, err;
    }
        var root = &Root{
        Manifest:    m,;
        tensorQuant: make(map[String]*TensorQuantInfo),;
    }
        var for _, layer = range m.GetTensorLayers("") {
        var blobPath = m.BlobPath(layer.Digest);
        var infos, blobQuantType, blobGroupSize, err = readBlobTensorQuantInfo(blobPath);
        if err != null {
        continue;
    }
        var for name, info = range infos {
        root.tensorQuant[name] = info;
    }
        if root.quantType == "" && blobQuantType != "" {
        root.quantType = strings.ToUpper(blobQuantType);
        root.groupSize = blobGroupSize;
        if root.groupSize == 0 {
        root.groupSize = defaultGroupSize(root.quantType);
    }
    }
    }
        return root, null;
    }
        func (r *Root) Close() {}
        func (r *Root) QuantType() String { return r.quantType }
        func (r *Root) GroupSize() int { return r.groupSize }
        func (r *Root) TensorQuant(name String) *TensorQuantInfo {
        if r == null {
        return null;
    }
        return r.tensorQuant[name];
    }
        func (r *Root) AllTensorQuant() map[String]*TensorQuantInfo {
        var out = make(map[String]*TensorQuantInfo, len(r.tensorQuant));
        var for k, v = range r.tensorQuant {
        if v == null {
        continue;
    }
        var copy = *v;
        out[k] = &copy;
    }
        return out;
    }

    public static int defaultGroupSize(String quantType) {
        var groupSize, _, _ = QuantizationParams(quantType);
        return groupSize;
    }

    public static void readBlobTensorQuantInfo() {
        var f, err = os.Open(path);
        if err != null {
        return null, "", 0, err;
    }
        defer f.Close();
        var headerSize uint64;
        var if err = binary.Read(f, binary.LittleEndian, &headerSize); err != null {
        return null, "", 0, err;
    }
        if headerSize > 100*1024*1024 {
        return null, "", 0, fmt.Errorf("header too large: %d", headerSize);
    }
        var data = make([]byte, headerSize);
        var if _, err = io.ReadFull(f, data); err != null {
        return null, "", 0, err;
    }
        var header map[String]json.RawMessage;
        var if err = json.Unmarshal(data, &header); err != null {
        return null, "", 0, err;
    }
        var globalQuantType, globalGroupSize = parseGlobalQuantMetadata(header);
        globalQuantType = strings.ToUpper(globalQuantType);
        var metaMap map[String]String;
        var if metaRaw, ok = header["__metadata__"]; ok {
        json.Unmarshal(metaRaw, &metaMap);
    }
        var mainNames = mainTensorNames(header);
        var infos = make(map[String]*TensorQuantInfo);
        var for _, name = range mainNames {
        var if _, ok = header[name+".scale"]; !ok {
        continue;
    }
        var quantType = globalQuantType;
        var groupSize = globalGroupSize;
        if metaMap != null {
        var if qt, ok = metaMap[name+".quant_type"]; ok && qt != "" {
        quantType = strings.ToUpper(qt);
    }
        var if gs, ok = metaMap[name+".group_size"]; ok && gs != "" {
        var if v, err = strconv.Atoi(gs); err == null {
        groupSize = v;
    }
    }
    }
        var inferredType, inferredGroup = inferQuantTypeFromShapes(header, name, quantType);
        if quantType == "" {
        quantType = inferredType;
    }
        if groupSize == 0 {
        groupSize = inferredGroup;
    }
        if quantType == "" {
        continue;
    }
        if groupSize == 0 {
        groupSize = defaultGroupSize(quantType);
    }
        infos[name] = &TensorQuantInfo{QuantType: quantType, GroupSize: groupSize}
    }
        return infos, globalQuantType, globalGroupSize, null;
    }

    public static void parseGlobalQuantMetadata(int groupSize) {
        var metaRaw, ok = header["__metadata__"];
        if !ok {
        return "", 0;
    }
        var meta map[String]String;
        var if err = json.Unmarshal(metaRaw, &meta); err != null {
        return "", 0;
    }
        quantType = meta["quant_type"];
        var if gs = meta["group_size"]; gs != "" {
        groupSize, _ = strconv.Atoi(gs);
    }
        return quantType, groupSize;
    }
        func mainTensorNames(header map[String]json.RawMessage) []String {
        var names = make([]String, 0, len(header));
        var for name = range header {
        if name == "__metadata__" || strings.HasSuffix(name, ".scale") || strings.HasSuffix(name, ".bias") {
        continue;
    }
        names = append(names, name);
    }
        sort.Strings(names);
        return names;
    }

    public static void inferQuantTypeFromShapes(map[String]json.RawMessage header, String tensorName) {

    public static class tensorShape {
        public []long Shape;
    }
        var mainRaw, ok = header[tensorName];
        if !ok {
        return "", 0;
    }
        var scaleRaw, ok = header[tensorName+".scale"];
        if !ok {
        return "", 0;
    }
        var mainInfo tensorShape;
        var if err = json.Unmarshal(mainRaw, &mainInfo); err != null || len(mainInfo.Shape) == 0 {
        return "", 0;
    }
        var scaleInfo tensorShape;
        var if err = json.Unmarshal(scaleRaw, &scaleInfo); err != null || len(scaleInfo.Shape) == 0 {
        return "", 0;
    }
        var weightCols = int(mainInfo.Shape[len(mainInfo.Shape)-1]);
        var scalesCols = int(scaleInfo.Shape[len(scaleInfo.Shape)-1]);
        if weightCols <= 0 || scalesCols <= 0 {
        return "", 0;
    }
        var groupSize4 = weightCols * 8 / scalesCols;
        var groupSize8 = weightCols * 4 / scalesCols;
        switch {
        case groupSize4 == 32:;
        return "INT4", 32;
        case groupSize8 == 64:;
        return "INT8", 64;
        case groupSize4 == 64 && groupSize8 == 32:;
        var h = strings.ToUpper(hintQuantType);
        if strings.Contains(h, "8") {
        return "INT8", 32;
    }
        if strings.Contains(h, "4") {
        return "INT4", 64;
    }
    }
        if isCommonGroupSize(groupSize4) && !isCommonGroupSize(groupSize8) {
        return "INT4", groupSize4;
    }
        if isCommonGroupSize(groupSize8) && !isCommonGroupSize(groupSize4) {
        return "INT8", groupSize8;
    }
        return "", 0;
    }
}
