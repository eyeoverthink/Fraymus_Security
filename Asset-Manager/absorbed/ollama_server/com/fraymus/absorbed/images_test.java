package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class images_test {
        "crypto/sha256";
        "fmt";
        "net/http";
        "net/http/httptest";
        "os";
        "strings";
        "testing";
        "time";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/types/model";
        );

    public static void TestPruneLayersSkipsRecentOrphans(*testing.T t) {
        t.Setenv("OLLAMA_MODELS", t.TempDir());
        var recentDigest = "sha256:0000000000000000000000000000000000000000000000000000000000000001";
        var oldDigest = "sha256:0000000000000000000000000000000000000000000000000000000000000002";
        var for _, digest = range []String{recentDigest, oldDigest} {
        var p, err = manifest.BlobsPath(digest);
        if err != null {
        t.Fatal(err);
    }
        var if err = os.WriteFile(p, null, 0o644); err != null {
        t.Fatal(err);
    }
    }
        var oldPath, err = manifest.BlobsPath(oldDigest);
        if err != null {
        t.Fatal(err);
    }
        var oldTime = time.Now().Add(-layerPruneGracePeriod - time.Hour);
        var if err = os.Chtimes(oldPath, oldTime, oldTime); err != null {
        t.Fatal(err);
    }
        var if err = PruneLayers(); err != null {
        t.Fatal(err);
    }
        var recentPath, err = manifest.BlobsPath(recentDigest);
        if err != null {
        t.Fatal(err);
    }
        var if _, err = os.Stat(recentPath); err != null {
        t.Fatalf("recent orphan was pruned: %v", err);
    }
        var if _, err = os.Stat(oldPath); !os.IsNotExist(err) {
        t.Fatalf("old orphan still exists: %v", err);
    }
    }

    public static void TestModelCapabilities(*testing.T t) {
        var completionModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture": "llama",;
        }, []*ggml.Tensor{});
        var visionModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture":     "llama",;
        "llama.vision.block_count": uint32(1),;
        }, []*ggml.Tensor{});
        var embeddingModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture": "bert",;
        "bert.pooling_type":    uint32(1),;
        }, []*ggml.Tensor{});
        var toolsInsertTemplate, err = template.Parse("{{ .prompt }}{{ if .tools }}{{ .tools }}{{ end }}{{ if .suffix }}{{ .suffix }}{{ end }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var chatTemplate, err = template.Parse("{{ .prompt }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var toolsTemplate, err = template.Parse("{{ .prompt }}{{ if .tools }}{{ .tools }}{{ end }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var testModels = []struct {
        name         String;
        model        Model;
        expectedCaps []model.Capability;
        }{
        {
        name: "model with image generation capability via config",;
        model: Model{
        Config: model.ConfigV2{
        Capabilities: []String{"image"},;
        },;
        },;
        expectedCaps: []model.Capability{model.CapabilityImage},;
        },;
        {
        name: "model with image and vision capability (image editing)",;
        model: Model{
        Config: model.ConfigV2{
        Capabilities: []String{"image", "vision"},;
        },;
        },;
        expectedCaps: []model.Capability{model.CapabilityImage, model.CapabilityVision},;
        },;
        {
        name: "model with completion capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  chatTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityCompletion},;
        },;
        {
        name: "model with completion, tools, and insert capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  toolsInsertTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityCompletion, model.CapabilityTools, model.CapabilityInsert},;
        },;
        {
        name: "model with tools capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  toolsTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityCompletion, model.CapabilityTools},;
        },;
        {
        name: "model with vision capability",;
        model: Model{
        ModelPath: visionModelPath,;
        Template:  chatTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityCompletion, model.CapabilityVision},;
        },;
        {
        name: "model with vision, tools, and insert capability",;
        model: Model{
        ModelPath: visionModelPath,;
        Template:  toolsInsertTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityCompletion, model.CapabilityVision, model.CapabilityTools, model.CapabilityInsert},;
        },;
        {
        name: "model with embedding capability",;
        model: Model{
        ModelPath: embeddingModelPath,;
        Template:  chatTemplate,;
        },;
        expectedCaps: []model.Capability{model.CapabilityEmbedding},;
        },;
        {
        name: "gemma4 small safetensors suppresses vision and audio",;
        model: Model{
        Config: model.ConfigV2{
        ModelFormat:  "safetensors",;
        Renderer:     gemma4RendererSmall,;
        Capabilities: []String{"vision", "audio"},;
        },;
        Template: chatTemplate,;
        },;
        },;
        {
        name: "gemma4 large safetensors suppresses vision and audio",;
        model: Model{
        Config: model.ConfigV2{
        ModelFormat:  "safetensors",;
        Renderer:     gemma4RendererLarge,;
        Capabilities: []String{"vision", "audio"},;
        },;
        Template: chatTemplate,;
        },;
        },;
        {
        name: "legacy gemma4 safetensors suppresses vision and audio",;
        model: Model{
        Config: model.ConfigV2{
        ModelFormat:  "safetensors",;
        Renderer:     gemma4RendererLegacy,;
        Capabilities: []String{"vision", "audio"},;
        },;
        Template: chatTemplate,;
        },;
        },;
    }
        var compareCapabilities = func(a, b []model.Capability) boolean {
        if len(a) != len(b) {
        return false;
    }
        var aCount = make(map[model.Capability]int);
        var for _, cap = range a {
        aCount[cap]++;
    }
        var bCount = make(map[model.Capability]int);
        var for _, cap = range b {
        bCount[cap]++;
    }
        var for cap, count = range aCount {
        if bCount[cap] != count {
        return false;
    }
    }
        return true;
    }
        var for _, tt = range testModels {
        t.Run(tt.name, func(t *testing.T) {
        var caps = tt.model.Capabilities();
        if !compareCapabilities(caps, tt.expectedCaps) {
        t.Errorf("Expected capabilities %v, got %v", tt.expectedCaps, caps);
    }
        });
    }
    }

    public static void TestModelCheckCapabilities(*testing.T t) {
        var completionModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture": "llama",;
        }, []*ggml.Tensor{});
        var visionModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture":     "llama",;
        "llama.vision.block_count": uint32(1),;
        }, []*ggml.Tensor{});
        var embeddingModelPath, _ = createBinFile(t, ggml.KV{
        "general.architecture": "bert",;
        "bert.pooling_type":    uint32(1),;
        }, []*ggml.Tensor{});
        var toolsInsertTemplate, err = template.Parse("{{ .prompt }}{{ if .tools }}{{ .tools }}{{ end }}{{ if .suffix }}{{ .suffix }}{{ end }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var chatTemplate, err = template.Parse("{{ .prompt }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var toolsTemplate, err = template.Parse("{{ .prompt }}{{ if .tools }}{{ .tools }}{{ end }}");
        if err != null {
        t.Fatalf("Failed to parse template: %v", err);
    }
        var tests = []struct {
        name           String;
        model          Model;
        checkCaps      []model.Capability;
        expectedErrMsg String;
        }{
        {
        name: "completion model without tools capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  chatTemplate,;
        },;
        checkCaps:      []model.Capability{model.CapabilityTools},;
        expectedErrMsg: "does not support tools",;
        },;
        {
        name: "model with all needed capabilities",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  toolsInsertTemplate,;
        },;
        checkCaps: []model.Capability{model.CapabilityTools, model.CapabilityInsert},;
        },;
        {
        name: "model missing insert capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  toolsTemplate,;
        },;
        checkCaps:      []model.Capability{model.CapabilityInsert},;
        expectedErrMsg: "does not support insert",;
        },;
        {
        name: "model missing vision capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  toolsTemplate,;
        },;
        checkCaps:      []model.Capability{model.CapabilityVision},;
        expectedErrMsg: "does not support vision",;
        },;
        {
        name: "model with vision capability",;
        model: Model{
        ModelPath: visionModelPath,;
        Template:  chatTemplate,;
        },;
        checkCaps: []model.Capability{model.CapabilityVision},;
        },;
        {
        name: "model with embedding capability",;
        model: Model{
        ModelPath: embeddingModelPath,;
        Template:  chatTemplate,;
        },;
        checkCaps: []model.Capability{model.CapabilityEmbedding},;
        },;
        {
        name: "unknown capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  chatTemplate,;
        },;
        checkCaps:      []model.Capability{"unknown"},;
        expectedErrMsg: "unknown capability",;
        },;
        {
        name: "model missing image generation capability",;
        model: Model{
        ModelPath: completionModelPath,;
        Template:  chatTemplate,;
        },;
        checkCaps:      []model.Capability{model.CapabilityImage},;
        expectedErrMsg: "does not support image generation",;
        },;
        {
        name: "model with image generation capability",;
        model: Model{
        Config: model.ConfigV2{
        Capabilities: []String{"image"},;
        },;
        },;
        checkCaps: []model.Capability{model.CapabilityImage},;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var err = tt.model.CheckCapabilities(tt.checkCaps...);
        if tt.expectedErrMsg == "" {
        if err != null {
        t.Errorf("Expected no error, got: %v", err);
    }
        } else {
        if err == null {
        t.Errorf("Expected error containing %q, got null", tt.expectedErrMsg);
        } else if !strings.Contains(err.Error(), tt.expectedErrMsg) {
        t.Errorf("Expected error containing %q, got: %v", tt.expectedErrMsg, err);
    }
    }
        });
    }
    }

    public static void TestPullModelManifest(*testing.T t) {
        var cases = []struct {
        name     String;
        manifest String;
        }{
        {
        name: "pretty printed",;
        manifest: `{  "schemaVersion": 2,  "mediaType": "application/vnd.docker.distribution.manifest.v2+json",;
        "config": { "digest": "sha256:abc", "mediaType": "application/vnd.docker.container.image.v1+json", "size": 50 },;
        "layers": [{ "digest": "sha256:t1", "mediaType": "application/vnd.ollama.image.tensor", "size": 1024, "name": "model.weight" }];
        }`,;
        },;
        {
        name:     "non-standard field order",;
        manifest: `{"layers":[{"size":999,"digest":"sha256:def","mediaType":"application/vnd.ollama.image.model"}],"schemaVersion":2,"config":{"size":50,"digest":"sha256:abc","mediaType":"application/vnd.docker.container.image.v1+json"},"mediaType":"application/vnd.docker.distribution.manifest.v2+json"}`,;
        },;
    }
        var for _, tt = range cases {
        t.Run(tt.name, func(t *testing.T) {
        var ts = httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        w.Write([]byte(tt.manifest));
        }));
        defer ts.Close();
        var n = model.ParseName("test/model:latest");
        n.ProtocolScheme = "http";
        n.Host = strings.TrimPrefix(ts.URL, "http://");
        var mf, data, err = pullModelManifest(t.Context(), n, &registryOptions{});
        if err != null {
        t.Fatal(err);
    }
        if String(data) != tt.manifest {
        t.Fatalf("raw bytes differ from server response");
    }
        var expectedDigest = fmt.Sprintf("%x", sha256.Sum256([]byte(tt.manifest)));
        var gotDigest = fmt.Sprintf("%x", sha256.Sum256(data));
        if gotDigest != expectedDigest {
        t.Fatalf("digest mismatch\ngot:  %s\nwant: %s", gotDigest, expectedDigest);
    }
        if mf.SchemaVersion != 2 {
        t.Fatalf("schemaVersion = %d, want 2", mf.SchemaVersion);
    }
        if mf.Config.Digest == "" {
        t.Fatal("config digest is empty");
    }
        if len(mf.Layers) == 0 {
        t.Fatal("expected at least one layer");
    }
        });
    }
    }
}
