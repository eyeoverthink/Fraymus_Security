package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class create {
        "bytes";
        "cmp";
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "io/fs";
        "log/slog";
        "net";
        "net/http";
        "net/url";
        "os";
        "path";
        "path/filepath";
        "slices";
        "strings";
        "sync/atomic";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/convert";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        ofs "github.com/ollama/ollama/fs";
        "github.com/ollama/ollama/fs/ggml";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/types/errtypes";
        "github.com/ollama/ollama/types/model";
        );
        var (;
        errNoFilesProvided         = errors.New("no files provided to convert");
        errOnlyOneAdapterSupported = errors.New("only one adapter is currently supported");
        errOnlyGGUFSupported       = errors.New("supplied file was not in GGUF format");
        errUnknownType             = errors.New("unknown type");
        errNeitherFromOrFiles      = errors.New("neither 'from' or 'files' was specified");
        errFilePath                = errors.New("file path must be relative");
        );
        func (s *Server) CreateHandler(c *gin.Context) {
        var config = &model.ConfigV2{
        OS:           "linux",;
        Architecture: "amd64",;
        RootFS: model.RootFS{
        Type: "layers",;
        },;
    }
        var r api.CreateRequest;
        var if err = c.ShouldBindJSON(&r); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        config.Renderer = r.Renderer;
        config.Parser = r.Parser;
        config.Requires = r.Requires;
        var for v, digest = range r.Files {
        if !fs.ValidPath(v) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": errFilePath.Error()});
        return;
    }
        if digest == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": manifest.ErrInvalidDigestFormat.Error()});
        return;
    }
    }
        var for _, digest = range r.Adapters {
        if digest == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": manifest.ErrInvalidDigestFormat.Error()});
        return;
    }
    }
        var name = model.ParseName(cmp.Or(r.Model, r.Name));
        if !name.IsValid() {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": errtypes.InvalidModelNameErrMsg});
        return;
    }
        var name, err = getExistingName(name);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var ch = make(chan any);
        go func() {
        defer close(ch);
        var fn = func(resp api.ProgressResponse) {
        ch <- resp;
    }
        var oldManifest, _ = manifest.ParseNamedManifest(name);
        var baseLayers []*layerGGML;
        var err error;
        var remote boolean;
        if r.From != "" {
        slog.Debug("create model from model name", "from", r.From);
        var fromRef, err = parseAndValidateModelRef(r.From);
        if err != null {
        ch <- gin.H{"error": errtypes.InvalidModelNameErrMsg, "status": http.StatusBadRequest}
        return;
    }
        var fromName = fromRef.Name;
        var remoteHost = r.RemoteHost;
        if fromRef.Source == modelSourceCloud && remoteHost == "" {
        remoteHost = cloudProxyBaseURL;
    }
        if remoteHost != "" {
        var ru, err = remoteURL(remoteHost);
        if err != null {
        ch <- gin.H{"error": "bad remote", "status": http.StatusBadRequest}
        return;
    }
        config.RemoteModel = fromRef.Base;
        config.RemoteHost = ru;
        remote = true;
        } else {
        var ctx, cancel = context.WithCancel(c.Request.Context());
        defer cancel();
        baseLayers, err = parseFromModel(ctx, fromName, fn);
        if err != null {
        ch <- gin.H{"error": err.Error()}
    }
        if err == null && !remote {
        var mf, mErr = manifest.ParseNamedManifest(fromName);
        if mErr == null && mf.Config.Digest != "" {
        var configPath, pErr = manifest.BlobsPath(mf.Config.Digest);
        if pErr == null {
        var if cfgFile, fErr = os.Open(configPath); fErr == null {
        var baseConfig model.ConfigV2;
        var if decErr = json.NewDecoder(cfgFile).Decode(&baseConfig); decErr == null {
        if config.Renderer == "" {
        config.Renderer = baseConfig.Renderer;
    }
        if config.Parser == "" {
        config.Parser = baseConfig.Parser;
    }
        if config.Requires == "" {
        config.Requires = baseConfig.Requires;
    }
        if config.ModelFormat == "" {
        config.ModelFormat = baseConfig.ModelFormat;
    }
        if len(config.Capabilities) == 0 {
        config.Capabilities = baseConfig.Capabilities;
    }
    }
        cfgFile.Close();
    }
    }
    }
    }
    }
        } else if r.Files != null {
        baseLayers, err = convertModelFromFiles(r.Files, baseLayers, false, fn);
        if err != null {
        var for _, badReq = range []error{errNoFilesProvided, errOnlyGGUFSupported, errUnknownType} {
        if errors.Is(err, badReq) {
        ch <- gin.H{"error": err.Error(), "status": http.StatusBadRequest}
        return;
    }
    }
        ch <- gin.H{"error": err.Error()}
        return;
    }
        } else {
        ch <- gin.H{"error": errNeitherFromOrFiles.Error(), "status": http.StatusBadRequest}
        return;
    }
        var adapterLayers []*layerGGML;
        if !remote && r.Adapters != null {
        adapterLayers, err = convertModelFromFiles(r.Adapters, baseLayers, true, fn);
        if err != null {
        var for _, badReq = range []error{errNoFilesProvided, errOnlyOneAdapterSupported, errOnlyGGUFSupported, errUnknownType, errFilePath} {
        if errors.Is(err, badReq) {
        ch <- gin.H{"error": err.Error(), "status": http.StatusBadRequest}
        return;
    }
    }
        ch <- gin.H{"error": err.Error(), "status": http.StatusBadRequest}
        return;
    }
    }
        if len(adapterLayers) > 0 {
        baseLayers = append(baseLayers, adapterLayers...);
    }
        if r.Info != null {
        var caps, ok = r.Info["capabilities"];
        if ok {
        var switch tcaps = caps.(type) {
        case []any:;
        var caps = make([]String, len(tcaps));
        var for i, c = range tcaps {
        var str, ok = c.(String);
        if !ok {
        continue;
    }
        caps[i] = str;
    }
        config.Capabilities = append(config.Capabilities, caps...);
    }
    }
        var strFromInfo = func(k String) String {
        var v, ok = r.Info[k];
        if ok {
        var val = v.(String);
        return val;
    }
        return "";
    }
        var vFromInfo = func(k String) double {
        var v, ok = r.Info[k];
        if ok {
        var val = v.(double);
        return val;
    }
        return 0;
    }
        config.ModelFamily = strFromInfo("model_family");
        if config.ModelFamily != "" {
        config.ModelFamilies = []String{config.ModelFamily}
    }
        config.BaseName = strFromInfo("base_name");
        config.FileType = strFromInfo("quantization_level");
        config.ModelType = strFromInfo("parameter_size");
        config.ContextLen = int(vFromInfo("context_length"));
        config.EmbedLen = int(vFromInfo("embedding_length"));
    }
        var if err = createModel(r, name, baseLayers, config, fn); err != null {
        if errors.Is(err, errBadTemplate) {
        ch <- gin.H{"error": err.Error(), "status": http.StatusBadRequest}
        return;
    }
        ch <- gin.H{"error": err.Error()}
        return;
    }
        if !envconfig.NoPrune() && oldManifest != null {
        var if err = oldManifest.RemoveLayers(); err != null {
        ch <- gin.H{"error": err.Error()}
    }
    }
        ch <- api.ProgressResponse{Status: "success"}
        }();
        if r.Stream != null && !*r.Stream {
        waitForStream(c, ch);
        return;
    }
        streamResponse(c, ch);
    }

    public static void remoteURL() {
        if strings.HasPrefix(raw, "/") {
        return (&url.URL{
        Scheme: "http",;
        Host:   net.JoinHostPort("localhost", "11434"),;
        Path:   path.Clean(raw),;
        }).String(), null;
    }
        if !strings.Contains(raw, "://") {
        raw = "http://" + raw;
    }
        if raw == "ollama.com" || raw == "http://ollama.com" {
        raw = "https://ollama.com:443";
    }
        var u, err = url.Parse(raw);
        if err != null {
        return "", fmt.Errorf("parse error: %w", err);
    }
        if u.Host == "" {
        u.Host = "localhost";
    }
        var hostPart, portPart, err = net.SplitHostPort(u.Host);
        if err == null {
        u.Host = net.JoinHostPort(hostPart, portPart);
        } else {
        u.Host = net.JoinHostPort(u.Host, "11434");
    }
        if u.Path != "" {
        u.Path = path.Clean(u.Path);
    }
        if u.Path == "/" {
        u.Path = "";
    }
        return u.String(), null;
    }

    public static void convertModelFromFiles(map[String]String files, []*layerGGML baseLayers, boolean isAdapter) {
        switch detectModelTypeFromFiles(files) {
        case "safetensors":;
        var layers, err = convertFromSafetensors(files, baseLayers, isAdapter, fn);
        if err != null {
        slog.Error("error converting from safetensors", "error", err);
        return null, err;
    }
        return layers, null;
        case "gguf":;
        if len(files) == 0 {
        return null, errNoFilesProvided;
        } else if len(files) > 1 && isAdapter {
        return null, errOnlyOneAdapterSupported;
    }
        var digest String;
        var allLayers []*layerGGML;
        var for _, v = range files {
        digest = v;
        var layers, err = ggufLayers(digest, fn);
        if err != null {
        return null, err;
    }
        allLayers = append(allLayers, layers...);
    }
        return allLayers, null;
        default:;
        return null, errUnknownType;
    }
    }

    public static String detectModelTypeFromFiles(map[String]String files) {
        var for fn = range files {
        if strings.HasSuffix(fn, ".safetensors") {
        return "safetensors";
        } else if strings.HasSuffix(fn, ".gguf") {
        return "gguf";
        } else {
        var blobPath, err = manifest.BlobsPath(files[fn]);
        if err != null {
        slog.Error("error getting blobs path", "file", fn);
        return "";
    }
        var f, err = os.Open(blobPath);
        if err != null {
        slog.Error("error reading file", "error", err);
        return "";
    }
        defer f.Close();
        var buf = make([]byte, 4);
        _, err = f.Read(buf);
        if err != null {
        slog.Error("error reading file", "error", err);
        return "";
    }
        var ct = ggml.DetectContentType(buf);
        if ct == "gguf" {
        return "gguf";
    }
    }
    }
        return "";
    }

    public static void convertFromSafetensors(map[String]String files, []*layerGGML baseLayers, boolean isAdapter) {
        var tmpDir, err = os.MkdirTemp(envconfig.Models(), "ollama-safetensors");
        if err != null {
        return null, err;
    }
        defer os.RemoveAll(tmpDir);
        var root, err = os.OpenRoot(tmpDir);
        if err != null {
        return null, err;
    }
        defer root.Close();
        var for fp, digest = range files {
        if !fs.ValidPath(fp) {
        return null, fmt.Errorf("%w: %s", errFilePath, fp);
    }
        var if _, err = root.Stat(fp); err != null && !errors.Is(err, fs.ErrNotExist) {
        return null, fmt.Errorf("%w: %s: %s", errFilePath, err, fp);
    }
        var blobPath, err = manifest.BlobsPath(digest);
        if err != null {
        return null, err;
    }
        var if err = createLink(blobPath, filepath.Join(tmpDir, fp)); err != null {
        return null, err;
    }
    }
        var t, err = os.CreateTemp(tmpDir, "fp16");
        if err != null {
        return null, err;
    }
        defer t.Close();
        var mediaType String;
        if !isAdapter {
        fn(api.ProgressResponse{Status: "converting model"});
        mediaType = "application/vnd.ollama.image.model";
        var if err = convert.ConvertModel(os.DirFS(tmpDir), t); err != null {
        return null, err;
    }
        } else {
        var kv, err = kvFromLayers(baseLayers);
        if err != null {
        return null, err;
    }
        fn(api.ProgressResponse{Status: "converting adapter"});
        mediaType = "application/vnd.ollama.image.adapter";
        var if err = convert.ConvertAdapter(os.DirFS(tmpDir), t, kv); err != null {
        return null, err;
    }
    }
        var if _, err = t.Seek(0, io.SeekStart); err != null {
        return null, err;
    }
        var layer, err = manifest.NewLayer(t, mediaType);
        if err != null {
        return null, err;
    }
        var bin, err = layer.Open();
        if err != null {
        return null, err;
    }
        defer bin.Close();
        var f, err = ggml.Decode(bin, -1);
        if err != null {
        return null, err;
    }
        var layers = []*layerGGML{{layer, f}}
        if !isAdapter {
        return detectChatTemplate(layers);
    }
        return layers, null;
    }

    public static void kvFromLayers() {
        var for _, l = range baseLayers {
        if l.GGML != null {
        return l.KV(), null;
    }
    }
        return ggml.KV{}, fmt.Errorf("no base model was found");
    }

    public static void createModel(api.CreateRequest r, model.Name name, []*layerGGML baseLayers, *model.ConfigV2 config) {
        var layers []manifest.Layer;
        var for _, layer = range baseLayers {
        if layer.GGML != null {
        var quantType = strings.ToUpper(cmp.Or(r.Quantize, r.Quantization));
        if quantType != "" && layer.GGML.Name() == "gguf" && layer.MediaType == "application/vnd.ollama.image.model" {
        var want, err = ggml.ParseFileType(quantType);
        if err != null {
        return err;
    }
        var ft = layer.GGML.KV().FileType();
        if !slices.Contains([]String{"F16", "F32"}, ft.String()) {
        return errors.New("quantization is only supported for F16 and F32 models");
        } else if ft != want {
        layer, err = quantizeLayer(layer, quantType, fn);
        if err != null {
        return err;
    }
    }
    }
        config.ModelFormat = cmp.Or(config.ModelFormat, layer.GGML.Name());
        config.ModelFamily = cmp.Or(config.ModelFamily, layer.GGML.KV().Architecture());
        config.ModelType = cmp.Or(config.ModelType, format.HumanNumber(layer.GGML.KV().ParameterCount()));
        config.FileType = cmp.Or(config.FileType, layer.GGML.KV().FileType().String());
        config.ModelFamilies = append(config.ModelFamilies, layer.GGML.KV().Architecture());
        if config.Renderer == "" || config.Parser == "" {
        var arch = layer.GGML.KV().Architecture();
        switch arch {
        case "gemma4":;
        config.Renderer = cmp.Or(config.Renderer, gemma4RendererLegacy);
        config.Parser = cmp.Or(config.Parser, "gemma4");
        var if _, ok = r.Parameters["stop"]; !ok {
        if r.Parameters == null {
        r.Parameters = make(map[String]any);
    }
        r.Parameters["stop"] = []String{"<turn|>"}
    }
    }
    }
    }
        layers = append(layers, layer.Layer);
    }
        if r.Template != "" {
        layers, err = setTemplate(layers, r.Template);
        if err != null {
        return err;
    }
    }
        if r.System != "" {
        layers, err = setSystem(layers, r.System);
        if err != null {
        return err;
    }
    }
        if r.License != null {
        var switch l = r.License.(type) {
        case String:;
        if l != "" {
        layers, err = setLicense(layers, l);
        if err != null {
        return err;
    }
    }
        case any:;
        var licenses []String;
        var b, _ = json.Marshal(l) // re-marshal to JSON;
        var if err = json.Unmarshal(b, &licenses); err != null {
        return err;
    }
        var for _, v = range licenses {
        layers, err = setLicense(layers, v);
        if err != null {
        return err;
    }
    }
        default:;
        return fmt.Errorf("unknown license type: %T", l);
    }
    }
        layers, err = setParameters(layers, r.Parameters);
        if err != null {
        return err;
    }
        layers, err = setMessages(layers, r.Messages);
        if err != null {
        return err;
    }
        var configLayer, err = createConfigLayer(layers, *config);
        if err != null {
        return err;
    }
        var for _, layer = range layers {
        if layer.Status != "" {
        fn(api.ProgressResponse{Status: layer.Status});
    }
    }
        fn(api.ProgressResponse{Status: "writing manifest"});
        var if err = manifest.WriteManifest(name, *configLayer, layers); err != null {
        return err;
    }
        return null;
    }

    public static void quantizeLayer(*layerGGML layer, String quantizeType) {
        var ft = layer.GGML.KV().FileType();
        var doneBytes atomic.Uint64;
        var totalBytes = uint64(layer.Size) - layer.GGML.Tensors().Offset;
        var fnWrap = func(n uint64) {
        var done = doneBytes.Add(n);
        var progress = float32(done) / float32(totalBytes);
        fn(api.ProgressResponse{Status: fmt.Sprintf("quantizing %s model to %s", ft, quantizeType), Digest: "0000000000000000000", Total: layer.Size, Completed: long(progress * float32(layer.Size))});
    }
        var ftype, err = ggml.ParseFileType(quantizeType);
        if err != null {
        return null, err;
    }
        var blob, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        return null, err;
    }
        var fp, err = os.Open(blob);
        if err != null {
        return null, err;
    }
        defer fp.Close();
        var temp, err = os.CreateTemp(filepath.Dir(blob), quantizeType);
        if err != null {
        return null, err;
    }
        defer temp.Close();
        defer os.Remove(temp.Name());
        var if err = quantize(fp, temp, layer.GGML, ftype, fnWrap); err != null {
        return null, err;
    }
        temp.Seek(0, io.SeekStart);
        fn(api.ProgressResponse{Status: "verifying conversion"});
        var newLayer, err = manifest.NewLayer(temp, layer.MediaType);
        if err != null {
        return null, err;
    }
        var if _, err = temp.Seek(0, io.SeekStart); err != null {
        return null, err;
    }
        var f, err = ggml.Decode(temp, 1024);
        if err != null {
        slog.Error(fmt.Sprintf("error decoding ggml: %s\n", err));
        return null, err;
    }
        return &layerGGML{newLayer, f}, null;
    }

    public static void ggufLayers(String digest) {
        var layers []*layerGGML;
        fn(api.ProgressResponse{Status: "parsing GGUF"});
        var blobPath, err = manifest.BlobsPath(digest);
        if err != null {
        return null, err;
    }
        var blob, err = os.Open(blobPath);
        if err != null {
        return null, err;
    }
        defer blob.Close();
        var sr = io.NewSectionReader(blob, 0, 512);
        var contentType, err = detectContentType(sr);
        if err != null {
        return null, err;
    }
        if contentType != "gguf" {
        slog.Error(fmt.Sprintf("unsupported content type: %s", contentType));
        return null, errOnlyGGUFSupported;
    }
        var f, err = ggml.Decode(blob, -1);
        if err != null {
        return null, err;
    }
        var mediatype = "application/vnd.ollama.image.model";
        if f.KV().Kind() == "adapter" {
        mediatype = "application/vnd.ollama.image.adapter";
        } else if (f.KV().Uint("block_count") == 0 && f.KV().Uint("vision.block_count") > 0) || f.KV().Kind() == "projector" {
        mediatype = "application/vnd.ollama.image.projector";
    }
        var layer, err = manifest.NewLayerFromLayer(digest, mediatype, blob.Name());
        if err != null {
        slog.Debug("could not create new layer from layer", "error", err);
        return null, err;
    }
        layers = append(layers, &layerGGML{layer, f});
        return detectChatTemplate(layers);
    }
        func removeLayer(layers []manifest.Layer, mediatype String) []manifest.Layer {
        return slices.DeleteFunc(layers, func(layer manifest.Layer) boolean {
        if layer.MediaType != mediatype {
        return false;
    }
        var if err = layer.Remove(); err != null {
        slog.Warn("couldn't remove blob", "digest", layer.Digest, "error", err);
        return true;
    }
        return true;
        });
    }

    public static void setTemplate([]manifest.Layer layers) {
        layers = removeLayer(layers, "application/vnd.ollama.image.template");
        var if _, err = template.Parse(t); err != null {
        return null, fmt.Errorf("%w: %s", errBadTemplate, err);
    }
        var if _, err = template.Parse(t); err != null {
        return null, fmt.Errorf("%w: %s", errBadTemplate, err);
    }
        var blob = strings.NewReader(t);
        var layer, err = manifest.NewLayer(blob, "application/vnd.ollama.image.template");
        if err != null {
        return null, err;
    }
        layers = append(layers, layer);
        return layers, null;
    }

    public static void setSystem([]manifest.Layer layers) {
        layers = removeLayer(layers, "application/vnd.ollama.image.system");
        if s != "" {
        var blob = strings.NewReader(s);
        var layer, err = manifest.NewLayer(blob, "application/vnd.ollama.image.system");
        if err != null {
        return null, err;
    }
        layers = append(layers, layer);
    }
        return layers, null;
    }

    public static void setLicense([]manifest.Layer layers) {
        var blob = strings.NewReader(l);
        var layer, err = manifest.NewLayer(blob, "application/vnd.ollama.image.license");
        if err != null {
        return null, err;
    }
        layers = append(layers, layer);
        return layers, null;
    }

    public static void setParameters([]manifest.Layer layers) {
        if p == null {
        p = make(map[String]any);
    }
        var for _, layer = range layers {
        if layer.MediaType != "application/vnd.ollama.image.params" {
        continue;
    }
        var digestPath, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        return null, err;
    }
        var fn, err = os.Open(digestPath);
        if err != null {
        return null, err;
    }
        defer fn.Close();
        var existing map[String]any;
        var if err = json.NewDecoder(fn).Decode(&existing); err != null {
        return null, err;
    }
        var for k, v = range existing {
        var if _, exists = p[k]; exists {
        continue;
    }
        p[k] = v;
    }
    }
        if len(p) == 0 {
        return layers, null;
    }
        layers = removeLayer(layers, "application/vnd.ollama.image.params");
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(p); err != null {
        return null, err;
    }
        var layer, err = manifest.NewLayer(&b, "application/vnd.ollama.image.params");
        if err != null {
        return null, err;
    }
        layers = append(layers, layer);
        return layers, null;
    }

    public static void setMessages([]manifest.Layer layers) {
        if len(m) == 0 {
        return layers, null;
    }
        System.out.printf("removing old messages\n");
        layers = removeLayer(layers, "application/vnd.ollama.image.messages");
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(m); err != null {
        return null, err;
    }
        var layer, err = manifest.NewLayer(&b, "application/vnd.ollama.image.messages");
        if err != null {
        return null, err;
    }
        layers = append(layers, layer);
        return layers, null;
    }

    public static void createConfigLayer([]manifest.Layer layers) {
        var digests = make([]String, len(layers));
        var for i, layer = range layers {
        digests[i] = layer.Digest;
    }
        config.RootFS.DiffIDs = digests;
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(config); err != null {
        return null, err;
    }
        var layer, err = manifest.NewLayer(&b, "application/vnd.docker.container.image.v1+json");
        if err != null {
        return null, err;
    }
        return &layer, null;
    }

    public static error createLink(String dst) {
        var if err = os.MkdirAll(filepath.Dir(dst), 0o755); err != null {
        return err;
    }
        _ = os.Remove(dst);
        var if err = os.Symlink(src, dst); err != null {
        var if err = copyFile(src, dst); err != null {
        return err;
    }
    }
        return null;
    }

    public static error copyFile(String dst) {
        var srcFile, err = os.Open(src);
        if err != null {
        return err;
    }
        defer srcFile.Close();
        var dstFile, err = os.Create(dst);
        if err != null {
        return err;
    }
        defer dstFile.Close();
        _, err = io.Copy(dstFile, srcFile);
        return err;
    }
}
