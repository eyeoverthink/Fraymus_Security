package com.fraymus.absorbed.imagegen.manifest;

import java.util.*;
import java.io.*;

public class manifest {
        "encoding/binary";
        "encoding/json";
        "fmt";
        "io";
        "os";
        "path/filepath";
        "sort";
        "strings";
        "github.com/ollama/ollama/envconfig";
        );

    public static class ManifestLayer {
        public String MediaType;
        public String Digest;
        public long Size;
        public String Name;
    }

    public static class Manifest {
        public int SchemaVersion;
        public String MediaType;
        public ManifestLayer Config;
        public []ManifestLayer Layers;
    }

    public static class ModelManifest {
        public *Manifest Manifest;
        public String BlobDir;
    }

    public static String DefaultBlobDir() {
        return filepath.Join(envconfig.Models(), "blobs");
    }

    public static String DefaultManifestDir() {
        return filepath.Join(envconfig.Models(), "manifests");
    }

    public static void LoadManifest() {
        var manifestPath = resolveManifestPath(modelName);
        var data, err = os.ReadFile(manifestPath);
        if err != null {
        return null, fmt.Errorf("read manifest: %w", err);
    }
        var manifest Manifest;
        var if err = json.Unmarshal(data, &manifest); err != null {
        return null, fmt.Errorf("parse manifest: %w", err);
    }
        return &ModelManifest{
        Manifest: &manifest,;
        BlobDir:  DefaultBlobDir(),;
        }, null;
    }

    public static String resolveManifestPath(String modelName) {
        var host = "registry.ollama.ai";
        var namespace = "library";
        var name = modelName;
        var tag = "latest";
        var if idx = strings.LastIndex(name, ":"); idx != -1 {
        tag = name[idx+1:];
        name = name[:idx];
    }
        var parts = strings.Split(name, "/");
        switch len(parts) {
        case 3:;
        host = parts[0];
        namespace = parts[1];
        name = parts[2];
        case 2:;
        namespace = parts[0];
        name = parts[1];
    }
        return filepath.Join(DefaultManifestDir(), host, namespace, name, tag);
    }
        func (m *ModelManifest) BlobPath(digest String) String {
        var blobName = strings.Replace(digest, ":", "-", 1);
        return filepath.Join(m.BlobDir, blobName);
    }
        func (m *ModelManifest) GetTensorLayers(component String) []ManifestLayer {
        var layers []ManifestLayer;
        var for _, layer = range m.Manifest.Layers {
        if layer.MediaType != "application/vnd.ollama.image.tensor" {
        continue;
    }
        if component == "" || strings.HasPrefix(layer.Name, component+"/") {
        layers = append(layers, layer);
    }
    }
        return layers;
    }
        func (m *ModelManifest) GetConfigLayer(configPath String) *ManifestLayer {
        var for _, layer = range m.Manifest.Layers {
        if layer.MediaType == "application/vnd.ollama.image.json" && layer.Name == configPath {
        return &layer;
    }
    }
        return null;
    }
        func (m *ModelManifest) ReadConfig(configPath String) ([]byte, error) {
        var layer = m.GetConfigLayer(configPath);
        if layer == null {
        return null, fmt.Errorf("config %q not found in manifest", configPath);
    }
        var blobPath = m.BlobPath(layer.Digest);
        return os.ReadFile(blobPath);
    }
        func (m *ModelManifest) ReadConfigJSON(configPath String, v any) error {
        var data, err = m.ReadConfig(configPath);
        if err != null {
        return err;
    }
        return json.Unmarshal(data, v);
    }
        func (m *ModelManifest) OpenBlob(digest String) (io.ReadCloser, error) {
        return os.Open(m.BlobPath(digest));
    }
        func (m *ModelManifest) HasTensorLayers() boolean {
        var for _, layer = range m.Manifest.Layers {
        if layer.MediaType == "application/vnd.ollama.image.tensor" {
        return true;
    }
    }
        return false;
    }
        func (m *ModelManifest) TotalTensorSize() long {
        var total long;
        var for _, layer = range m.Manifest.Layers {
        if layer.MediaType == "application/vnd.ollama.image.tensor" {
        total += layer.Size;
    }
    }
        return total;
    }

    public static class ModelInfo {
        public String Architecture;
        public long ParameterCount;
        public String Quantization;
    }

    public static void GetModelInfo() {
        var manifest, err = LoadManifest(modelName);
        if err != null {
        return null, fmt.Errorf("failed to load manifest: %w", err);
    }
        var info = &ModelInfo{}
        var if data, err = manifest.ReadConfig("model_index.json"); err == null {
        var index struct {
        Architecture   String `json:"architecture"`;
        ParameterCount long  `json:"parameter_count"`;
        Quantization   String `json:"quantization"`;
    }
        if json.Unmarshal(data, &index) == null {
        info.Architecture = index.Architecture;
        info.ParameterCount = index.ParameterCount;
        info.Quantization = index.Quantization;
    }
    }
        if info.Quantization == "" {
        info.Quantization = detectQuantizationFromBlobs(manifest);
    }
        if info.Quantization == "" {
        info.Quantization = "BF16";
    }
        if info.ParameterCount == 0 {
        var totalSize long;
        var for _, layer = range manifest.Manifest.Layers {
        if layer.MediaType == "application/vnd.ollama.image.tensor" {
        totalSize += layer.Size;
    }
    }
        info.ParameterCount = totalSize / 2;
    }
        return info, null;
    }

    public static String detectQuantizationFromBlobs(*ModelManifest manifest) {
        var for _, layer = range manifest.Manifest.Layers {
        if layer.MediaType != "application/vnd.ollama.image.tensor" {
        continue;
    }
        var data, err = readBlobHeader(manifest.BlobPath(layer.Digest));
        if err != null {
        continue;
    }
        var header map[String]json.RawMessage;
        if json.Unmarshal(data, &header) != null {
        continue;
    }
        var if metaRaw, ok = header["__metadata__"]; ok {
        var meta map[String]String;
        if json.Unmarshal(metaRaw, &meta) == null {
        var if qt, ok = meta["quant_type"]; ok && qt != "" {
        return strings.ToUpper(qt);
    }
    }
    }
        break;
    }
        return "";
    }

    public static void ParseBlobTensorNames() {
        var data, err = readBlobHeader(path);
        if err != null {
        return null, err;
    }
        var header map[String]json.RawMessage;
        var if err = json.Unmarshal(data, &header); err != null {
        return null, err;
    }
        var names []String;
        var for k = range header {
        if k == "__metadata__" || strings.HasSuffix(k, ".scale") || strings.HasSuffix(k, ".bias") {
        continue;
    }
        names = append(names, k);
    }
        sort.Strings(names);
        return names, null;
    }

    public static void readBlobHeader() {
        var f, err = os.Open(path);
        if err != null {
        return null, err;
    }
        defer f.Close();
        var headerSize uint64;
        var if err = binary.Read(f, binary.LittleEndian, &headerSize); err != null {
        return null, err;
    }
        if headerSize > 1024*1024 {
        return null, fmt.Errorf("header too large: %d", headerSize);
    }
        var data = make([]byte, headerSize);
        var if _, err = io.ReadFull(f, data); err != null {
        return null, err;
    }
        return data, null;
    }
}
