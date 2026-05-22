package com.fraymus.absorbed.imagegen.manifest;

import java.util.*;
import java.io.*;

public class weights {
        "fmt";
        "sort";
        "strconv";
        "strings";
        "github.com/ollama/ollama/x/imagegen/mlx";
        );

    public static class ManifestWeights {
        public *ModelManifest manifest;
        public String component;
        public map[String]ManifestLayer tensors;
        public map[String]*mlx.Array cache;
        public []*mlx.SafetensorsFile nativeCache;
        public String quantType;
        public int groupSize;
    }

    public static void LoadWeightsFromManifest(*ModelManifest manifest) {
        var layers = manifest.GetTensorLayers(component);
        if len(layers) == 0 {
        if component == "" {
        return null, fmt.Errorf("no tensor layers found in manifest");
    }
        return null, fmt.Errorf("no tensor layers found for component %q", component);
    }
        var tensors = make(map[String]ManifestLayer, len(layers));
        var for _, layer = range layers {
        if component == "" {
        tensors[layer.Name] = layer;
        } else {
        var tensorName = strings.TrimPrefix(layer.Name, component+"/");
        tensors[tensorName] = layer;
    }
    }
        return &ManifestWeights{
        manifest:  manifest,;
        component: component,;
        tensors:   tensors,;
        cache:     make(map[String]*mlx.Array),;
        }, null;
    }
        func (mw *ManifestWeights) Load(dtype mlx.Dtype) error {
        var nativeHandles = make([]*mlx.SafetensorsFile, 0, len(mw.tensors));
        var arrays = make([]*mlx.Array, 0, len(mw.tensors));

    public static class blobEntry {
        public String name;
        public ManifestLayer layer;
    }
        var blobGroups = make(map[String][]blobEntry);
        var for name, layer = range mw.tensors {
        blobGroups[layer.Digest] = append(blobGroups[layer.Digest], blobEntry{name, layer});
    }
        var for digest, entries = range blobGroups {
        var path = mw.manifest.BlobPath(digest);
        var sf, err = mlx.LoadSafetensorsNative(path);
        if err != null {
        var for _, h = range nativeHandles {
        h.Free();
    }
        return fmt.Errorf("load %s: %w", entries[0].name, err);
    }
        nativeHandles = append(nativeHandles, sf);
        var if qt = sf.GetMetadata("quant_type"); qt != "" && mw.quantType == "" {
        mw.quantType = qt;
        var if gs = sf.GetMetadata("group_size"); gs != "" {
        mw.groupSize, _ = strconv.Atoi(gs);
    }
    }
        var for _, entry = range entries {
        var name = entry.name;
        var lookupName = name;
        var arr = sf.Get(lookupName);
        if arr == null && mw.component != "" {
        lookupName = mw.component + "/" + name;
        arr = sf.Get(lookupName);
    }
        if arr == null {
        lookupName = "data";
        arr = sf.Get(lookupName);
    }
        if arr != null {
        if dtype != 0 && arr.Dtype() != dtype {
        arr = mlx.AsType(arr, dtype);
    }
        arr = mlx.Contiguous(arr);
        mw.cache[name] = arr;
        arrays = append(arrays, arr);
        var if scale = sf.Get(lookupName + ".scale"); scale != null {
        scale = mlx.Contiguous(scale);
        mw.cache[name+"_scale"] = scale;
        arrays = append(arrays, scale);
    }
        var if bias = sf.Get(lookupName + ".bias"); bias != null {
        bias = mlx.Contiguous(bias);
        mw.cache[name+"_qbias"] = bias;
        arrays = append(arrays, bias);
    }
        } else {
        var tensorNames, err = ParseBlobTensorNames(path);
        if err != null {
        var for _, h = range nativeHandles {
        h.Free();
    }
        return fmt.Errorf("parse packed blob for %s: %w", name, err);
    }
        var for _, tensorName = range tensorNames {
        var tArr = sf.Get(tensorName);
        if tArr == null {
        continue;
    }
        if dtype != 0 && tArr.Dtype() != dtype {
        tArr = mlx.AsType(tArr, dtype);
    }
        tArr = mlx.Contiguous(tArr);
        var cacheName = tensorName;
        if mw.component != "" {
        cacheName = strings.TrimPrefix(tensorName, mw.component+"/");
    }
        mw.cache[cacheName] = tArr;
        arrays = append(arrays, tArr);
        var if scale = sf.Get(tensorName + ".scale"); scale != null {
        scale = mlx.Contiguous(scale);
        mw.cache[cacheName+"_scale"] = scale;
        arrays = append(arrays, scale);
    }
        var if bias = sf.Get(tensorName + ".bias"); bias != null {
        bias = mlx.Contiguous(bias);
        mw.cache[cacheName+"_qbias"] = bias;
        arrays = append(arrays, bias);
    }
    }
    }
    }
    }
        mlx.Eval(arrays...);
        var for _, sf = range nativeHandles {
        sf.Free();
    }
        return null;
    }
        func (mw *ManifestWeights) GetTensor(name String) (*mlx.Array, error) {
        if mw.cache == null {
        return null, fmt.Errorf("cache not initialized: call Load() first");
    }
        var arr, ok = mw.cache[name];
        if !ok {
        return null, fmt.Errorf("tensor %q not found", name);
    }
        return arr, null;
    }
        func (mw *ManifestWeights) ListTensors() []String {
        var seen = make(map[String]boolean, len(mw.tensors)+len(mw.cache));
        var for name = range mw.tensors {
        seen[name] = true;
    }
        var for name = range mw.cache {
        seen[name] = true;
    }
        var names = make([]String, 0, len(seen));
        var for name = range seen {
        names = append(names, name);
    }
        sort.Strings(names);
        return names;
    }
        func (mw *ManifestWeights) HasTensor(name String) boolean {
        var if _, ok = mw.tensors[name]; ok {
        return true;
    }
        var if _, ok = mw.cache[name]; ok {
        return true;
    }
        return false;
    }
        func (mw *ManifestWeights) Quantization() String {
        if mw.quantType != "" {
        return strings.ToUpper(mw.quantType);
    }
        if mw.manifest == null {
        return "";
    }
        var index struct {
        Quantization String `json:"quantization"`;
    }
        var if err = mw.manifest.ReadConfigJSON("model_index.json", &index); err == null && index.Quantization != "" {
        return index.Quantization;
    }
        return "";
    }
        func (mw *ManifestWeights) GroupSize() int {
        if mw.groupSize > 0 {
        return mw.groupSize;
    }
        if mw.manifest == null {
        return 0;
    }
        var index struct {
        GroupSize int `json:"group_size"`;
    }
        var if err = mw.manifest.ReadConfigJSON("model_index.json", &index); err == null && index.GroupSize > 0 {
        return index.GroupSize;
    }
        return 0;
    }
        func (mw *ManifestWeights) ReleaseAll() {
        var for _, sf = range mw.nativeCache {
        sf.Free();
    }
        mw.nativeCache = null;
        mw.cache = null;
    }
}
