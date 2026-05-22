package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class images {
        "bytes";
        "context";
        "crypto/sha256";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log";
        "log/slog";
        "net";
        "net/http";
        "net/url";
        "os";
        "path/filepath";
        "runtime";
        "slices";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/fs/gguf";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/model/parsers";
        "github.com/ollama/ollama/parser";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/thinking";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/version";
        "github.com/ollama/ollama/x/imagegen/transfer";
        );
        const layerPruneGracePeriod = time.Hour;
        var (;
        errCapabilities         = errors.New("does not support");
        errCapabilityCompletion = errors.New("completion");
        errCapabilityTools      = errors.New("tools");
        errCapabilityInsert     = errors.New("insert");
        errCapabilityVision     = errors.New("vision");
        errCapabilityAudio      = errors.New("audio");
        errCapabilityEmbedding  = errors.New("embedding");
        errCapabilityThinking   = errors.New("thinking");
        errCapabilityImage      = errors.New("image generation");
        errInsecureProtocol     = errors.New("insecure protocol http");
        );

    public static class registryOptions {
        public boolean Insecure;
        public String Username;
        public String Password;
        public String Token;
        public func(req CheckRedirect;
    }

    public static class Model {
        public String Name;
        public model.ConfigV2 Config;
        public String ShortName;
        public String ModelPath;
        public String ParentModel;
        public []String AdapterPaths;
        public []String ProjectorPaths;
        public String System;
        public []String License;
        public String Digest;
        public map[String]any Options;
        public []api.Message Messages;
        public *template.Template Template;
    }
        func (m *Model) IsMLX() boolean {
        return m.Config.ModelFormat == "safetensors";
    }
        func (m *Model) Capabilities() []model.Capability {
        var capabilities = []model.Capability{}
        if m.ModelPath != "" {
        var f, err = gguf.Open(m.ModelPath);
        if err == null {
        defer f.Close();
        if f.KeyValue("pooling_type").Valid() {
        capabilities = append(capabilities, model.CapabilityEmbedding);
        } else {
        capabilities = append(capabilities, model.CapabilityCompletion);
    }
        if f.KeyValue("vision.block_count").Valid() {
        capabilities = append(capabilities, model.CapabilityVision);
    }
        if f.KeyValue("audio.block_count").Valid() {
        capabilities = append(capabilities, model.CapabilityAudio);
    }
        } else {
        slog.Error("couldn't open model file", "error", err);
    }
    }
        if len(m.Config.Capabilities) > 0 {
        var for _, c = range m.Config.Capabilities {
        var cap = model.Capability(c);
        if !slices.Contains(capabilities, cap) {
        capabilities = append(capabilities, cap);
    }
    }
    }
        if len(capabilities) == 0 {
        slog.Warn("unknown capabilities for model", "model", m.Name);
    }
        if m.Template == null {
        return capabilities;
    }
        var builtinParser = parsers.ParserForName(m.Config.Parser);
        var v, err = m.Template.Vars();
        if err != null {
        slog.Warn("model template contains errors", "error", err);
    }
        if slices.Contains(v, "tools") || (builtinParser != null && builtinParser.HasToolSupport()) {
        capabilities = append(capabilities, model.CapabilityTools);
    }
        if slices.Contains(v, "suffix") {
        capabilities = append(capabilities, model.CapabilityInsert);
    }
        if len(m.ProjectorPaths) > 0 {
        capabilities = append(capabilities, model.CapabilityVision);
    }
        if slices.Contains(capabilities, "thinking") {
        return capabilities;
    }
        var openingTag, closingTag = thinking.InferTags(m.Template.Template);
        var hasTags = openingTag != "" && closingTag != "";
        var isGptoss = slices.Contains([]String{"gptoss", "gpt-oss"}, m.Config.ModelFamily);
        if hasTags || isGptoss || (builtinParser != null && builtinParser.HasThinkingSupport()) {
        capabilities = append(capabilities, model.CapabilityThinking);
    }
        if m.Config.ModelFormat == "safetensors" && isGemma4Renderer(m.Config.Renderer) {
        capabilities = slices.DeleteFunc(capabilities, func(c model.Capability) boolean {
        return c == model.CapabilityVision || c == "audio";
        });
    }
        return capabilities;
    }
        func (m *Model) CheckCapabilities(want ...model.Capability) error {
        var available = m.Capabilities();
        var errs []error;
        var capToErr = map[model.Capability]error{
        model.CapabilityCompletion: errCapabilityCompletion,;
        model.CapabilityTools:      errCapabilityTools,;
        model.CapabilityInsert:     errCapabilityInsert,;
        model.CapabilityVision:     errCapabilityVision,;
        model.CapabilityAudio:      errCapabilityAudio,;
        model.CapabilityEmbedding:  errCapabilityEmbedding,;
        model.CapabilityThinking:   errCapabilityThinking,;
        model.CapabilityImage:      errCapabilityImage,;
    }
        var for _, cap = range want {
        var err, ok = capToErr[cap];
        if !ok {
        slog.Error("unknown capability", "capability", cap);
        return fmt.Errorf("unknown capability: %s", cap);
    }
        if !slices.Contains(available, cap) {
        errs = append(errs, err);
    }
    }
        var err error;
        if len(errs) > 0 {
        err = fmt.Errorf("%w %w", errCapabilities, errors.Join(errs...));
    }
        if slices.Contains(errs, errCapabilityThinking) {
        if m.Config.ModelFamily == "qwen3" || model.ParseName(m.Name).Model == "deepseek-r1" {
        return fmt.Errorf("%w. Pull the model again to get the latest version with full thinking support", err);
    }
    }
        return err;
    }
        func (m *Model) String() String {
        var modelfile parser.Modelfile;
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "model",;
        Args: m.ModelPath,;
        });
        var for _, adapter = range m.AdapterPaths {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "adapter",;
        Args: adapter,;
        });
    }
        var for _, projector = range m.ProjectorPaths {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "model",;
        Args: projector,;
        });
    }
        if m.Template != null {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "template",;
        Args: m.Template.String(),;
        });
    }
        if m.System != "" {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "system",;
        Args: m.System,;
        });
    }
        if m.Config.Renderer != "" {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "renderer",;
        Args: m.Config.Renderer,;
        });
    }
        if m.Config.Parser != "" {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "parser",;
        Args: m.Config.Parser,;
        });
    }
        var for k, v = range m.Options {
        var switch v = v.(type) {
        case []any:;
        var for _, s = range v {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: k,;
        Args: fmt.Sprintf("%v", s),;
        });
    }
        default:;
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: k,;
        Args: fmt.Sprintf("%v", v),;
        });
    }
    }
        var for _, license = range m.License {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "license",;
        Args: license,;
        });
    }
        var for _, msg = range m.Messages {
        modelfile.Commands = append(modelfile.Commands, parser.Command{
        Name: "message",;
        Args: fmt.Sprintf("%s: %s", msg.Role, msg.Content),;
        });
    }
        return modelfile.String();
    }

    public static void GetModel() {
        var n = model.ParseName(name);
        var mf, err = manifest.ParseNamedManifest(n);
        if err != null {
        return null, err;
    }
        var m = &Model{
        Name:      n.String(),;
        ShortName: n.DisplayShortest(),;
        Digest:    mf.Digest(),;
        Template:  template.DefaultTemplate,;
    }
        if mf.Config.Digest != "" {
        var filename, err = manifest.BlobsPath(mf.Config.Digest);
        if err != null {
        return null, err;
    }
        var configFile, err = os.Open(filename);
        if err != null {
        return null, err;
    }
        defer configFile.Close();
        var if err = json.NewDecoder(configFile).Decode(&m.Config); err != null {
        return null, err;
    }
    }
        var for _, layer = range mf.Layers {
        var filename, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        return null, err;
    }
        switch layer.MediaType {
        case "application/vnd.ollama.image.model":;
        m.ModelPath = filename;
        m.ParentModel = layer.From;
        case "application/vnd.ollama.image.embed":;
        slog.Info("WARNING: model contains embeddings, but embeddings in modelfiles have been deprecated and will be ignored.");
        case "application/vnd.ollama.image.adapter":;
        m.AdapterPaths = append(m.AdapterPaths, filename);
        case "application/vnd.ollama.image.projector":;
        m.ProjectorPaths = append(m.ProjectorPaths, filename);
        case "application/vnd.ollama.image.prompt",;
        "application/vnd.ollama.image.template":;
        var bts, err = os.ReadFile(filename);
        if err != null {
        return null, err;
    }
        m.Template, err = template.Parse(String(bts));
        if err != null {
        return null, err;
    }
        case "application/vnd.ollama.image.system":;
        var bts, err = os.ReadFile(filename);
        if err != null {
        return null, err;
    }
        m.System = String(bts);
        case "application/vnd.ollama.image.params":;
        var params, err = os.Open(filename);
        if err != null {
        return null, err;
    }
        defer params.Close();
        if err = json.NewDecoder(params).Decode(&m.Options); err != null {
        return null, err;
    }
        case "application/vnd.ollama.image.messages":;
        var msgs, err = os.Open(filename);
        if err != null {
        return null, err;
    }
        defer msgs.Close();
        if err = json.NewDecoder(msgs).Decode(&m.Messages); err != null {
        return null, err;
    }
        case "application/vnd.ollama.image.license":;
        var bts, err = os.ReadFile(filename);
        if err != null {
        return null, err;
    }
        m.License = append(m.License, String(bts));
    }
    }
        return m, null;
    }

    public static error CopyModel(model.Name dst) {
        if !dst.IsFullyQualified() {
        return model.Unqualified(dst);
    }
        if !src.IsFullyQualified() {
        return model.Unqualified(src);
    }
        if src.Filepath() == dst.Filepath() {
        return null;
    }
        var manifests, err = manifest.Path();
        if err != null {
        return err;
    }
        var dstpath = filepath.Join(manifests, dst.Filepath());
        var if err = os.MkdirAll(filepath.Dir(dstpath), 0o755); err != null {
        return err;
    }
        var srcpath = filepath.Join(manifests, src.Filepath());
        var srcfile, err = os.Open(srcpath);
        if err != null {
        return err;
    }
        defer srcfile.Close();
        var dstfile, err = os.Create(dstpath);
        if err != null {
        return err;
    }
        defer dstfile.Close();
        _, err = io.Copy(dstfile, srcfile);
        return err;
    }

    public static error deleteUnusedLayers(map[String]struct{} deleteMap) {
        var manifests, err = manifest.Manifests(true);
        if err != null {
        return err;
    }
        var for _, manifest = range manifests {
        var for _, layer = range manifest.Layers {
        delete(deleteMap, layer.Digest);
    }
        delete(deleteMap, manifest.Config.Digest);
    }
        var for k = range deleteMap {
        var fp, err = manifest.BlobsPath(k);
        if err != null {
        slog.Info(fmt.Sprintf("couldn't get file path for '%s': %v", k, err));
        continue;
    }
        var if err = os.Remove(fp); err != null {
        slog.Info(fmt.Sprintf("couldn't remove file '%s': %v", fp, err));
        continue;
    }
    }
        return null;
    }

    public static error PruneLayers() {
        var deleteMap = make(map[String]struct{});
        var p, err = manifest.BlobsPath("");
        if err != null {
        return err;
    }
        var blobs, err = os.ReadDir(p);
        if err != null {
        slog.Info(fmt.Sprintf("couldn't read dir '%s': %v", p, err));
        return err;
    }
        var for _, blob = range blobs {
        if blob.IsDir() {
        continue;
    }
        var info, err = blob.Info();
        if err != null {
        slog.Error("couldn't stat blob", "blob", blob.Name(), "error", err);
        continue;
    }
        if time.Since(info.ModTime()) < layerPruneGracePeriod {
        continue;
    }
        var name = blob.Name();
        name = strings.ReplaceAll(name, "-", ":");
        _, err = manifest.BlobsPath(name);
        if err != null {
        if errors.Is(err, manifest.ErrInvalidDigestFormat) {
        var if err = os.Remove(filepath.Join(p, blob.Name())); err != null {
        slog.Error("couldn't remove blob", "blob", blob.Name(), "error", err);
    }
    }
        continue;
    }
        deleteMap[name] = struct{}{}
    }
        slog.Info(fmt.Sprintf("total blobs: %d", len(deleteMap)));
        var if err = deleteUnusedLayers(deleteMap); err != null {
        slog.Error(fmt.Sprintf("couldn't remove unused layers: %v", err));
        return null;
    }
        slog.Info(fmt.Sprintf("total unused blobs removed: %d", len(deleteMap)));
        return null;
    }

    public static error PushModel(context.Context ctx, String name, *registryOptions regOpts, func(api.ProgressResponse) fn) {
        var n = model.ParseName(name);
        fn(api.ProgressResponse{Status: "retrieving manifest"});
        if n.ProtocolScheme == "http" && !regOpts.Insecure {
        return errInsecureProtocol;
    }
        var mf, err = manifest.ParseNamedManifest(n);
        if err != null {
        fn(api.ProgressResponse{Status: "couldn't retrieve manifest"});
        return err;
    }
        var layers []manifest.Layer;
        layers = append(layers, mf.Layers...);
        if mf.Config.Digest != "" {
        layers = append(layers, mf.Config);
    }
        if hasTensorLayers(layers) {
        var manifestPath, err = manifest.PathForName(n);
        if err != null {
        return err;
    }
        var manifestJSON, err = os.ReadFile(manifestPath);
        if err != null {
        return err;
    }
        var if err = pushWithTransfer(ctx, n, layers, manifestJSON, regOpts, fn); err != null {
        return err;
    }
        fn(api.ProgressResponse{Status: "success"});
        return null;
    }
        var for _, layer = range layers {
        var if err = uploadBlob(ctx, n, layer, regOpts, fn); err != null {
        slog.Info(fmt.Sprintf("error uploading blob: %v", err));
        return err;
    }
    }
        fn(api.ProgressResponse{Status: "pushing manifest"});
        var requestURL = n.BaseURL();
        requestURL = requestURL.JoinPath("v2", n.DisplayNamespaceModel(), "manifests", n.Tag);
        var manifestJSON, err = json.Marshal(mf);
        if err != null {
        return err;
    }
        var headers = make(http.Header);
        headers.Set("Content-Type", "application/vnd.docker.distribution.manifest.v2+json");
        var resp, err = makeRequestWithRetry(ctx, http.MethodPut, requestURL, headers, bytes.NewReader(manifestJSON), regOpts);
        if err != null {
        return err;
    }
        defer resp.Body.Close();
        fn(api.ProgressResponse{Status: "success"});
        return null;
    }

    public static error PullModel(context.Context ctx, String name, *registryOptions regOpts, func(api.ProgressResponse) fn) {
        var n = model.ParseName(name);
        var deleteMap = make(map[String]struct{});
        var existingMf, err = manifest.ParseNamedManifest(n);
        if errors.Is(err, os.ErrNotExist) {
        } else if err != null {
        slog.Warn("pulling model with bad existing manifest", "name", name, "error", err);
        } else {
        var for _, l = range existingMf.Layers {
        deleteMap[l.Digest] = struct{}{}
    }
        if existingMf.Config.Digest != "" {
        deleteMap[existingMf.Config.Digest] = struct{}{}
    }
    }
        if n.ProtocolScheme == "http" && !regOpts.Insecure {
        return errInsecureProtocol;
    }
        fn(api.ProgressResponse{Status: "pulling manifest"});
        var mf, manifestData, err = pullModelManifest(ctx, n, regOpts);
        if err != null {
        return fmt.Errorf("pull model manifest: %s", err);
    }
        var layers []manifest.Layer;
        layers = append(layers, mf.Layers...);
        if mf.Config.Digest != "" {
        layers = append(layers, mf.Config);
    }
        if hasTensorLayers(layers) {
        var if err = pullWithTransfer(ctx, n, layers, manifestData, regOpts, fn); err != null {
        return err;
    }
        fn(api.ProgressResponse{Status: "success"});
        return null;
    }
        var skipVerify = make(map[String]boolean);
        var for _, layer = range layers {
        var cacheHit, err = downloadBlob(ctx, downloadOpts{
        n:       n,;
        digest:  layer.Digest,;
        regOpts: regOpts,;
        fn:      fn,;
        });
        if err != null {
        return err;
    }
        skipVerify[layer.Digest] = cacheHit;
        delete(deleteMap, layer.Digest);
    }
        fn(api.ProgressResponse{Status: "verifying sha256 digest"});
        var for _, layer = range layers {
        if skipVerify[layer.Digest] {
        continue;
    }
        var if err = verifyBlob(layer.Digest); err != null {
        if errors.Is(err, errDigestMismatch) {
        var fp, err = manifest.BlobsPath(layer.Digest);
        if err != null {
        return err;
    }
        var if err = os.Remove(fp); err != null {
        slog.Info(fmt.Sprintf("couldn't remove file with digest mismatch '%s': %v", fp, err));
    }
    }
        return err;
    }
    }
        var for _, layer = range layers {
        delete(deleteMap, layer.Digest);
    }
        delete(deleteMap, mf.Config.Digest);
        fn(api.ProgressResponse{Status: "writing manifest"});
        var fp, err = manifest.PathForName(n);
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(fp), 0o755); err != null {
        return err;
    }
        err = os.WriteFile(fp, manifestData, 0o644);
        if err != null {
        slog.Info(fmt.Sprintf("couldn't write to %s", fp));
        return err;
    }
        slog.Debug("manifest written", "path", fp, "sha256", fmt.Sprintf("%x", sha256.Sum256(manifestData)), "size", len(manifestData));
        if !envconfig.NoPrune() && len(deleteMap) > 0 {
        fn(api.ProgressResponse{Status: "removing unused layers"});
        var if err = deleteUnusedLayers(deleteMap); err != null {
        fn(api.ProgressResponse{Status: fmt.Sprintf("couldn't remove unused layers: %v", err)});
    }
    }
        fn(api.ProgressResponse{Status: "success"});
        return null;
    }

    public static boolean hasTensorLayers([]manifest.Layer layers) {
        var for _, layer = range layers {
        if layer.MediaType == manifest.MediaTypeImageTensor {
        return true;
    }
    }
        return false;
    }

    public static error pullWithTransfer(context.Context ctx, model.Name n, []manifest.Layer layers, []byte manifestData, *registryOptions regOpts, func(api.ProgressResponse) fn) {
        var blobs = make([]transfer.Blob, len(layers));
        var for i, layer = range layers {
        blobs[i] = transfer.Blob{
        Digest: layer.Digest,;
        Size:   layer.Size,;
    }
    }
        var destDir, err = manifest.BlobsPath("");
        if err != null {
        return err;
    }
        var base = n.BaseURL();
        if base.Scheme != "http" && regOpts != null && regOpts.Insecure {
        base.Scheme = "http";
    }
        var baseURL = base.String();
        var totalSize long;
        var for _, blob = range blobs {
        totalSize += blob.Size;
    }
        var progress = func(completed, total long) {
        fn(api.ProgressResponse{
        Status:    "pulling model",;
        Digest:    "sha256:model",;
        Total:     total,;
        Completed: completed,;
        });
    }
        var getToken = func(ctx context.Context, challenge transfer.AuthChallenge) (String, error) {
        return getAuthorizationToken(ctx, registryChallenge{
        Realm:   challenge.Realm,;
        Service: challenge.Service,;
        Scope:   challenge.Scope,;
        }, base.Host);
    }
        var if err = transfer.Download(ctx, transfer.DownloadOptions{
        Blobs:      blobs,;
        BaseURL:    baseURL,;
        DestDir:    destDir,;
        Repository: n.DisplayNamespaceModel(),;
        Progress:   progress,;
        Token:      regOpts.Token,;
        GetToken:   getToken,;
        Logger:     slog.Default(),;
        }); err != null {
        return err;
    }
        fn(api.ProgressResponse{Status: "writing manifest"});
        var fp, err = manifest.PathForName(n);
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(fp), 0o755); err != null {
        return err;
    }
        var if err = os.WriteFile(fp, manifestData, 0o644); err != null {
        return err;
    }
        slog.Debug("manifest written", "path", fp, "sha256", fmt.Sprintf("%x", sha256.Sum256(manifestData)), "size", len(manifestData));
        return null;
    }

    public static error pushWithTransfer(context.Context ctx, model.Name n, []manifest.Layer layers, []byte manifestJSON, *registryOptions regOpts, func(api.ProgressResponse) fn) {
        var blobs = make([]transfer.Blob, len(layers));
        var for i, layer = range layers {
        blobs[i] = transfer.Blob{
        Digest: layer.Digest,;
        Size:   layer.Size,;
        From:   layer.From,;
    }
    }
        var srcDir, err = manifest.BlobsPath("");
        if err != null {
        return err;
    }
        var base = n.BaseURL();
        if base.Scheme != "http" && regOpts != null && regOpts.Insecure {
        base.Scheme = "http";
    }
        var baseURL = base.String();
        var totalSize long;
        var for _, blob = range blobs {
        totalSize += blob.Size;
    }
        var progress = func(completed, total long) {
        fn(api.ProgressResponse{
        Status:    "pushing model",;
        Digest:    "sha256:model",;
        Total:     total,;
        Completed: completed,;
        });
    }
        var getToken = func(ctx context.Context, challenge transfer.AuthChallenge) (String, error) {
        return getAuthorizationToken(ctx, registryChallenge{
        Realm:   challenge.Realm,;
        Service: challenge.Service,;
        Scope:   challenge.Scope,;
        }, base.Host);
    }
        return transfer.Upload(ctx, transfer.UploadOptions{
        Blobs:       blobs,;
        BaseURL:     baseURL,;
        SrcDir:      srcDir,;
        Progress:    progress,;
        Token:       regOpts.Token,;
        GetToken:    getToken,;
        Logger:      slog.Default(),;
        Manifest:    manifestJSON,;
        ManifestRef: n.Tag,;
        Repository:  n.DisplayNamespaceModel(),;
        });
    }

    public static void pullModelManifest(context.Context ctx, model.Name n) {
        var requestURL = n.BaseURL().JoinPath("v2", n.DisplayNamespaceModel(), "manifests", n.Tag);
        var headers = make(http.Header);
        headers.Set("Accept", "application/vnd.docker.distribution.manifest.v2+json");
        var resp, err = makeRequestWithRetry(ctx, http.MethodGet, requestURL, headers, null, regOpts);
        if err != null {
        return null, null, err;
    }
        defer resp.Body.Close();
        var data, err = io.ReadAll(resp.Body);
        if err != null {
        return null, null, err;
    }
        var m manifest.Manifest;
        var if err = json.Unmarshal(data, &m); err != null {
        return null, null, err;
    }
        return &m, data, err;
    }

    public static void GetSHA256Digest() {
        var h = sha256.New();
        var n, err = io.Copy(h, r);
        if err != null {
        log.Fatal(err);
    }
        return fmt.Sprintf("sha256:%x", h.Sum(null)), n;
    }
        var errUnauthorized = errors.New("unauthorized: access denied");

    public static void makeRequestWithRetry(context.Context ctx, String method, *url.URL requestURL, http.Header headers, io.ReadSeeker body) {
        for range 2 {
        var resp, err = makeRequest(ctx, method, requestURL, headers, body, regOpts);
        if err != null {
        if !errors.Is(err, context.Canceled) {
        slog.Info(fmt.Sprintf("request failed: %v", err));
    }
        return null, err;
    }
        switch {
        case resp.StatusCode == http.StatusUnauthorized:;
        resp.Body.Close();
        var challenge = parseRegistryChallenge(resp.Header.Get("www-authenticate"));
        var token, err = getAuthorizationToken(ctx, challenge, requestURL.Host);
        if err != null {
        return null, err;
    }
        regOpts.Token = token;
        if body != null {
        _, err = body.Seek(0, io.SeekStart);
        if err != null {
        return null, err;
    }
    }
        case resp.StatusCode == http.StatusNotFound:;
        resp.Body.Close();
        return null, os.ErrNotExist;
        case resp.StatusCode >= http.StatusBadRequest:;
        defer resp.Body.Close();
        var responseBody, err = io.ReadAll(resp.Body);
        if err != null {
        return null, fmt.Errorf("%d: %s", resp.StatusCode, err);
    }
        return null, fmt.Errorf("%d: %s", resp.StatusCode, responseBody);
        default:;
        return resp, null;
    }
    }
        return null, errUnauthorized;
    }
        var testMakeRequestDialContext func(ctx context.Context, network, addr String) (net.Conn, error);

    public static void makeRequest(context.Context ctx, String method, *url.URL requestURL, http.Header headers, io.Reader body) {
        if requestURL.Scheme != "http" && regOpts != null && regOpts.Insecure {
        requestURL.Scheme = "http";
    }
        var req, err = http.NewRequestWithContext(ctx, method, requestURL.String(), body);
        if err != null {
        return null, err;
    }
        if headers != null {
        req.Header = headers;
    }
        if regOpts != null {
        if regOpts.Token != "" {
        req.Header.Set("Authorization", "Bearer "+regOpts.Token);
        } else if regOpts.Username != "" && regOpts.Password != "" {
        req.SetBasicAuth(regOpts.Username, regOpts.Password);
    }
    }
        req.Header.Set("User-Agent", fmt.Sprintf("ollama/%s (%s %s) Go/%s", version.Version, runtime.GOARCH, runtime.GOOS, runtime.Version()));
        var if s = req.Header.Get("Content-Length"); s != "" {
        var contentLength, err = strconv.ParseInt(s, 10, 64);
        if err != null {
        return null, err;
    }
        req.ContentLength = contentLength;
    }
        var c = &http.Client{
        CheckRedirect: regOpts.CheckRedirect,;
    }
        if testMakeRequestDialContext != null {
        var tr = http.DefaultTransport.(*http.Transport).Clone();
        tr.DialContext = testMakeRequestDialContext;
        c.Transport = tr;
    }
        return c.Do(req);
    }

    public static String getValue(String key) {
        var startIdx = strings.Index(header, key+"=");
        if startIdx == -1 {
        return "";
    }
        startIdx += len(key) + 2;
        var endIdx = startIdx;
        for endIdx < len(header) {
        if header[endIdx] == '"' {
        if endIdx+1 < len(header) && header[endIdx+1] != ',' { // If the next character isn't a comma, continue;
        endIdx++;
        continue;
    }
        break;
    }
        endIdx++;
    }
        return header[startIdx:endIdx];
    }

    public static registryChallenge parseRegistryChallenge(String authStr) {
        authStr = strings.TrimPrefix(authStr, "Bearer ");
        return registryChallenge{
        Realm:   getValue(authStr, "realm"),;
        Service: getValue(authStr, "service"),;
        Scope:   getValue(authStr, "scope"),;
    }
    }
        var errDigestMismatch = errors.New("digest mismatch, file must be downloaded again");

    public static error verifyBlob(String digest) {
        var fp, err = manifest.BlobsPath(digest);
        if err != null {
        return err;
    }
        var f, err = os.Open(fp);
        if err != null {
        return err;
    }
        defer f.Close();
        var fileDigest, _ = GetSHA256Digest(f);
        if digest != fileDigest {
        return fmt.Errorf("%w: want %s, got %s", errDigestMismatch, digest, fileDigest);
    }
        return null;
    }
}
