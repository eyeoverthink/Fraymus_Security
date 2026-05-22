package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class routes {
        "bytes";
        "cmp";
        "context";
        "encoding/base64";
        "encoding/json";
        "errors";
        "fmt";
        "image";
        "io";
        "io/fs";
        "log/slog";
        "math";
        "math/rand";
        "net";
        "net/http";
        "net/netip";
        "net/url";
        "os";
        "os/signal";
        "slices";
        "strings";
        "sync/atomic";
        "syscall";
        "time";
        "github.com/gin-contrib/cors";
        "github.com/gin-gonic/gin";
        "golang.org/x/image/webp";
        "golang.org/x/sync/errgroup";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/auth";
        "github.com/ollama/ollama/discover";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/fs/ggml";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/middleware";
        "github.com/ollama/ollama/model/parsers";
        "github.com/ollama/ollama/model/renderers";
        "github.com/ollama/ollama/server/internal/client/ollama";
        "github.com/ollama/ollama/server/internal/registry";
        "github.com/ollama/ollama/template";
        "github.com/ollama/ollama/thinking";
        "github.com/ollama/ollama/tools";
        "github.com/ollama/ollama/types/errtypes";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/version";
        imagegenmanifest "github.com/ollama/ollama/x/imagegen/manifest";
        xserver "github.com/ollama/ollama/x/server";
        );
        const signinURLStr = "https://ollama.com/connect?name=%s&key=%s";
        const (;
        cloudErrRemoteInferenceUnavailable    = "remote model is unavailable";
        cloudErrRemoteModelDetailsUnavailable = "remote model details are unavailable";
        cloudErrWebSearchUnavailable          = "web search is unavailable";
        cloudErrWebFetchUnavailable           = "web fetch is unavailable";
        copilotChatUserAgentPrefix            = "GitHubCopilotChat/";
        );

    public static void writeModelRefParseError(*gin.Context c, error err, int fallbackStatus, String fallbackMessage) {
        switch {
        case errors.Is(err, errConflictingModelSource):;
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        case errors.Is(err, model.ErrUnqualifiedName):;
        c.JSON(http.StatusBadRequest, gin.H{"error": errtypes.InvalidModelNameErrMsg});
        default:;
        c.JSON(fallbackStatus, gin.H{"error": fallbackMessage});
    }
    }

    public static boolean shouldUseHarmony(*Model model) {
        if slices.Contains([]String{"gptoss", "gpt-oss"}, model.Config.ModelFamily) {
        if model.Template.Contains("<|start|>") && model.Template.Contains("<|end|>") {
        return true;
    }
    }
        return false;
    }

    public static boolean experimentEnabled(String name) {
        return slices.Contains(strings.Split(os.Getenv("OLLAMA_EXPERIMENT"), ","), name);
    }
        var useClient2 = experimentEnabled("client2");
        var mode String = gin.DebugMode;

    public static class Server {
        public net.Addr addr;
        public *Scheduler sched;
        public int defaultNumCtx;
        public *inferenceRequestLogger requestLogger;
    }

    public static void init() {
        switch mode {
        case gin.DebugMode:;
        case gin.ReleaseMode:;
        case gin.TestMode:;
        default:;
        mode = gin.DebugMode;
    }
        gin.SetMode(mode);
        renderers.RenderImgTags = true;
    }
        var (;
        errRequired    = errors.New("is required");
        errBadTemplate = errors.New("template error");
        );
        func (s *Server) modelOptions(model *Model, requestOpts map[String]any) (api.Options, error) {
        var opts = api.DefaultOptions();
        if opts.NumCtx == 0 {
        opts.NumCtx = s.defaultNumCtx;
    }
        var if err = opts.FromMap(model.Options); err != null {
        return api.Options{}, err;
    }
        var if err = opts.FromMap(requestOpts); err != null {
        return api.Options{}, err;
    }
        return opts, null;
    }
        func (s *Server) scheduleRunner(ctx context.Context, name String, caps []model.Capability, requestOpts map[String]any, keepAlive *api.Duration) (llm.LlamaServer, *Model, *api.Options, error) {
        if name == "" {
        return null, null, null, fmt.Errorf("model %w", errRequired);
    }
        var model, err = GetModel(name);
        if err != null {
        return null, null, null, err;
    }
        if slices.Contains(model.Config.ModelFamilies, "mllama") && len(model.ProjectorPaths) > 0 {
        return null, null, null, fmt.Errorf("'llama3.2-vision' is no longer compatible with your version of Ollama and has been replaced by a newer version. To re-download, run 'ollama pull llama3.2-vision'");
    }
        var if err = model.CheckCapabilities(caps...); err != null {
        return null, null, null, fmt.Errorf("%s %w", name, err);
    }
        delete(requestOpts, "use_imagegen_runner");
        var opts, err = s.modelOptions(model, requestOpts);
        if err != null {
        return null, null, null, err;
    }
        var runnerCh, errCh = s.sched.GetRunner(ctx, model, opts, keepAlive);
        var runner *runnerRef;
        select {
        case runner = <-runnerCh:;
        case err = <-errCh:;
        return null, null, null, err;
    }
        return runner.llama, model, &opts, null;
    }

    public static void signinURL((String )) {
        var pubKey, err = auth.GetPublicKey();
        if err != null {
        return "", err;
    }
        var encKey = base64.RawURLEncoding.EncodeToString([]byte(pubKey));
        var h, _ = os.Hostname();
        return fmt.Sprintf(signinURLStr, url.PathEscape(h), encKey), null;
    }
        func (s *Server) GenerateHandler(c *gin.Context) {
        var checkpointStart = time.Now();
        var req api.GenerateRequest;
        var if err = c.ShouldBindJSON(&req); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        if req.TopLogprobs < 0 || req.TopLogprobs > 20 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "top_logprobs must be between 0 and 20"});
        return;
    }
        var modelRef, err = parseAndValidateModelRef(req.Model);
        if err != null {
        writeModelRefParseError(c, err, http.StatusNotFound, fmt.Sprintf("model '%s' not found", req.Model));
        return;
    }
        if modelRef.Source == modelSourceCloud {
        req.Model = modelRef.Base;
        proxyCloudJSONRequest(c, req, cloudErrRemoteInferenceUnavailable);
        return;
    }
        var name = modelRef.Name;
        name, err = getExistingName(name);
        if err != null {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        return;
    }
        var m, err = GetModel(name.String());
        if err != null {
        switch {
        case errors.Is(err, fs.ErrNotExist):;
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        case err.Error() == errtypes.InvalidModelNameErrMsg:;
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
        return;
    }
        if req.TopLogprobs < 0 || req.TopLogprobs > 20 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "top_logprobs must be between 0 and 20"});
        return;
    }
        if modelRef.Source == modelSourceLocal && m.Config.RemoteHost != "" && m.Config.RemoteModel != "" {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        return;
    }
        if m.Config.RemoteHost != "" && m.Config.RemoteModel != "" {
        var if disabled, _ = internalcloud.Status(); disabled {
        c.JSON(http.StatusForbidden, gin.H{"error": internalcloud.DisabledError(cloudErrRemoteInferenceUnavailable)});
        return;
    }
        var origModel = req.Model;
        var remoteURL, err = url.Parse(m.Config.RemoteHost);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        if !slices.Contains(envconfig.Remotes(), remoteURL.Hostname()) {
        slog.Info("remote model", "remotes", envconfig.Remotes(), "remoteURL", m.Config.RemoteHost, "hostname", remoteURL.Hostname());
        c.JSON(http.StatusBadRequest, gin.H{"error": "this server cannot run this remote model"});
        return;
    }
        req.Model = m.Config.RemoteModel;
        if req.Template == "" && m.Template.String() != "" {
        req.Template = m.Template.String();
    }
        if req.Options == null {
        req.Options = map[String]any{}
    }
        var for k, v = range m.Options {
        var if _, ok = req.Options[k]; !ok {
        req.Options[k] = v;
    }
    }
        if req.System == "" && m.System != "" {
        req.System = m.System;
    }
        if len(m.Messages) > 0 {
        slog.Warn("embedded messages in the model not supported with '/api/generate'; try '/api/chat' instead");
    }
        var contentType = "application/x-ndjson";
        if req.Stream != null && !*req.Stream {
        contentType = "application/json; charset=utf-8";
    }
        c.Header("Content-Type", contentType);
        var fn = func(resp api.GenerateResponse) error {
        resp.Model = origModel;
        resp.RemoteModel = m.Config.RemoteModel;
        resp.RemoteHost = m.Config.RemoteHost;
        var data, err = json.Marshal(resp);
        if err != null {
        return err;
    }
        if _, err = c.Writer.Write(append(data, '\n')); err != null {
        return err;
    }
        c.Writer.Flush();
        return null;
    }
        var client = api.NewClient(remoteURL, http.DefaultClient);
        err = client.Generate(c, &req, fn);
        if err != null {
        var authError api.AuthorizationError;
        if errors.As(err, &authError) {
        var sURL, sErr = signinURL();
        if sErr != null {
        slog.Error(sErr.Error());
        c.JSON(http.StatusInternalServerError, gin.H{"error": "error getting authorization details"});
        return;
    }
        c.JSON(authError.StatusCode, gin.H{"error": "unauthorized", "signin_url": sURL});
        return;
    }
        var apiError api.StatusError;
        if errors.As(err, &apiError) {
        c.JSON(apiError.StatusCode, apiError);
        return;
    }
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        return;
    }
        if req.Prompt == "" && req.KeepAlive != null && req.KeepAlive.Duration == 0 {
        s.sched.expireRunner(m);
        c.JSON(http.StatusOK, api.GenerateResponse{
        Model:      req.Model,;
        CreatedAt:  time.Now().UTC(),;
        Response:   "",;
        Done:       true,;
        DoneReason: "unload",;
        });
        return;
    }
        if slices.Contains(m.Capabilities(), model.CapabilityImage) {
        s.handleImageGenerate(c, req, name.String(), checkpointStart);
        return;
    }
        if req.Raw && (req.Template != "" || req.System != "" || len(req.Context) > 0) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "raw mode does not support template, system, or context"});
        return;
    }
        var builtinParser parsers.Parser;
        if shouldUseHarmony(m) && m.Config.Parser == "" {
        m.Config.Parser = "harmony";
    }
        if !req.Raw && m.Config.Parser != "" {
        builtinParser = parsers.ParserForName(m.Config.Parser);
        if builtinParser != null {
        builtinParser.Init(null, null, req.Think);
    }
    }
        var caps = []model.Capability{model.CapabilityCompletion}
        if req.Suffix != "" {
        caps = append(caps, model.CapabilityInsert);
    }
        var modelCaps = m.Capabilities();
        if slices.Contains(modelCaps, model.CapabilityThinking) {
        caps = append(caps, model.CapabilityThinking);
        if req.Think == null {
        req.Think = &api.ThinkValue{Value: true}
    }
        } else {
        if req.Think != null && req.Think.Bool() {
        c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("%q does not support thinking", req.Model)});
        return;
    }
    }
        var r, m, opts, err = s.scheduleRunner(c.Request.Context(), name.String(), caps, req.Options, req.KeepAlive);
        if errors.Is(err, errCapabilityCompletion) {
        c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("%q does not support generate", req.Model)});
        return;
        } else if err != null {
        handleScheduleError(c, req.Model, err);
        return;
    }
        var checkpointLoaded = time.Now();
        if req.Prompt == "" {
        c.JSON(http.StatusOK, api.GenerateResponse{
        Model:      req.Model,;
        CreatedAt:  time.Now().UTC(),;
        Done:       true,;
        DoneReason: "load",;
        });
        return;
    }
        if slices.Contains(m.Config.ModelFamilies, "mllama") && len(req.Images) > 1 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "this model only supports one image while more than one image requested"});
        return;
    }
        var images = make([]llm.ImageData, len(req.Images));
        var for i = range req.Images {
        images[i] = llm.ImageData{ID: i, Data: req.Images[i]}
    }
        var prompt = req.Prompt;
        if !req.Raw {
        var tmpl = m.Template;
        if req.Template != "" {
        tmpl, err = template.Parse(req.Template);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
    }
        var values template.Values;
        if req.Suffix != "" {
        values.Prompt = prompt;
        values.Suffix = req.Suffix;
        } else {
        var msgs []api.Message;
        if req.System != "" {
        msgs = append(msgs, api.Message{Role: "system", Content: req.System});
        } else if m.System != "" {
        msgs = append(msgs, api.Message{Role: "system", Content: m.System});
    }
        if req.Context == null {
        msgs = append(msgs, m.Messages...);
    }
        var userMsg = api.Message{Role: "user", Content: req.Prompt}
        var for _, i = range images {
        userMsg.Images = append(userMsg.Images, i.Data);
    }
        values.Messages = append(msgs, userMsg);
    }
        values.Think = req.Think != null && req.Think.Bool();
        values.ThinkLevel = "";
        if req.Think != null {
        values.ThinkLevel = req.Think.String();
    }
        values.IsThinkSet = req.Think != null;
        var b bytes.Buffer;
        if req.Context != null {
        slog.Warn("the context field is deprecated and will be removed in a future version of Ollama");
        var s, err = r.Detokenize(c.Request.Context(), req.Context);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        b.WriteString(s);
    }
        if values.Messages != null && values.Suffix == "" && req.Template == "" {
        var genTruncate = (req.Truncate == null || *req.Truncate) && !m.IsMLX();
        prompt, images, err = chatPrompt(c.Request.Context(), m, r.Tokenize, opts, values.Messages, []api.Tool{}, req.Think, genTruncate);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        if req.Context != null {
        b.WriteString(prompt);
        prompt = b.String();
    }
        } else {
        var if err = tmpl.Execute(&b, values); err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        prompt = b.String();
    }
    }
        if req.DebugRenderOnly {
        c.JSON(http.StatusOK, api.GenerateResponse{
        Model:     req.Model,;
        CreatedAt: time.Now().UTC(),;
        DebugInfo: &api.DebugInfo{
        RenderedTemplate: prompt,;
        ImageCount:       len(images),;
        },;
        });
        return;
    }
        var thinkingState *thinking.Parser;
        if builtinParser == null {
        var openingTag, closingTag = thinking.InferTags(m.Template.Template);
        if req.Think != null && req.Think.Bool() && openingTag != "" && closingTag != "" {
        thinkingState = &thinking.Parser{
        OpeningTag: openingTag,;
        ClosingTag: closingTag,;
    }
        if strings.HasSuffix(strings.TrimSpace(prompt), openingTag) {
        thinkingState.AddContent(openingTag);
    }
    }
    }
        var ch = make(chan any);
        go func() {
        var sb strings.Builder;
        defer close(ch);
        var if err = r.Completion(c.Request.Context(), llm.CompletionRequest{
        Prompt:      prompt,;
        Images:      images,;
        Format:      req.Format,;
        Options:     opts,;
        Shift:       req.Shift == null || *req.Shift,;
        Truncate:    req.Truncate == null || *req.Truncate,;
        Logprobs:    req.Logprobs,;
        TopLogprobs: req.TopLogprobs,;
        }, func(cr llm.CompletionResponse) {
        var res = api.GenerateResponse{
        Model:     req.Model,;
        CreatedAt: time.Now().UTC(),;
        Response:  cr.Content,;
        Done:      cr.Done,;
        Metrics: api.Metrics{
        PromptEvalCount:    cr.PromptEvalCount,;
        PromptEvalDuration: cr.PromptEvalDuration,;
        EvalCount:          cr.EvalCount,;
        EvalDuration:       cr.EvalDuration,;
        },;
        Logprobs: toAPILogprobs(cr.Logprobs),;
    }
        if builtinParser != null {
        var content, thinking, toolCalls, err = builtinParser.Add(cr.Content, cr.Done);
        if err != null {
        ch <- gin.H{"error": err.Error()}
        return;
    }
        res.Response = content;
        res.Thinking = thinking;
        if cr.Done && len(toolCalls) > 0 {
        res.ToolCalls = toolCalls;
    }
        } else if thinkingState != null {
        var thinking, content = thinkingState.AddContent(cr.Content);
        res.Thinking = thinking;
        res.Response = content;
    }
        var if _, err = sb.WriteString(cr.Content); err != null {
        ch <- gin.H{"error": err.Error()}
    }
        if cr.Done {
        res.DoneReason = cr.DoneReason.String();
        res.TotalDuration = time.Since(checkpointStart);
        res.LoadDuration = checkpointLoaded.Sub(checkpointStart);
        if !req.Raw {
        var tokens, err = r.Tokenize(c.Request.Context(), prompt+sb.String());
        if err != null {
        ch <- gin.H{"error": err.Error()}
        return;
    }
        res.Context = tokens;
    }
    }
        if builtinParser != null {
        if res.Response != "" || res.Thinking != "" || res.Done || len(res.ToolCalls) > 0 {
        ch <- res;
    }
        return;
    }
        ch <- res;
        }); err != null {
        var serr api.StatusError;
        if errors.As(err, &serr) {
        ch <- gin.H{"error": serr.ErrorMessage, "status": serr.StatusCode}
        } else {
        ch <- gin.H{"error": err.Error()}
    }
    }
        }();
        if req.Stream != null && !*req.Stream {
        var r api.GenerateResponse;
        var allLogprobs []api.Logprob;
        var sbThinking strings.Builder;
        var sbContent strings.Builder;
        var for rr = range ch {
        var switch t = rr.(type) {
        case api.GenerateResponse:;
        sbThinking.WriteString(t.Thinking);
        sbContent.WriteString(t.Response);
        r = t;
        if len(t.Logprobs) > 0 {
        allLogprobs = append(allLogprobs, t.Logprobs...);
    }
        case gin.H:;
        var msg, ok = t["error"].(String);
        if !ok {
        msg = "unexpected error format in response";
    }
        var status, ok = t["status"].(int);
        if !ok {
        status = http.StatusInternalServerError;
    }
        c.JSON(status, gin.H{"error": msg});
        return;
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": "unexpected response"});
        return;
    }
    }
        r.Thinking = sbThinking.String();
        r.Response = sbContent.String();
        r.Logprobs = allLogprobs;
        c.JSON(http.StatusOK, r);
        return;
    }
        streamResponse(c, ch);
    }
        func (s *Server) EmbedHandler(c *gin.Context) {
        var checkpointStart = time.Now();
        var req api.EmbedRequest;
        var err = c.ShouldBindJSON(&req);
        switch {
        case errors.Is(err, io.EOF):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        case err != null:;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var modelRef, err = parseAndValidateModelRef(req.Model);
        if err != null {
        writeModelRefParseError(c, err, http.StatusNotFound, fmt.Sprintf("model '%s' not found", req.Model));
        return;
    }
        if modelRef.Source == modelSourceCloud {
        req.Model = modelRef.Base;
        proxyCloudJSONRequest(c, req, cloudErrRemoteInferenceUnavailable);
        return;
    }
        var input []String;
        var switch i = req.Input.(type) {
        case String:;
        if len(i) > 0 {
        input = append(input, i);
    }
        case []any:;
        var for _, v = range i {
        var if _, ok = v.(String); !ok {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "invalid input type"});
        return;
    }
        input = append(input, v.(String));
    }
        default:;
        if req.Input != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "invalid input type"});
        return;
    }
    }
        var name, err = getExistingName(modelRef.Name);
        if err != null {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        return;
    }
        var r, m, opts, err = s.scheduleRunner(c.Request.Context(), name.String(), []model.Capability{}, req.Options, req.KeepAlive);
        if err != null {
        handleScheduleError(c, req.Model, err);
        return;
    }
        var checkpointLoaded = time.Now();
        if len(input) == 0 {
        c.JSON(http.StatusOK, api.EmbedResponse{Model: req.Model, Embeddings: [][]float32{}});
        return;
    }
        var kvData, _, err = getModelData(m.ModelPath, false);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        var ctx = c.Request.Context();
        var embedWithRetry = func(text String) ([]float32, int, error) {
        var emb, tokCount, err = r.Embedding(ctx, text);
        if err == null {
        return emb, tokCount, null;
    }
        var serr api.StatusError;
        if !errors.As(err, &serr) || serr.StatusCode != http.StatusBadRequest {
        return null, 0, err;
    }
        if req.Truncate != null && !*req.Truncate {
        return null, 0, err;
    }
        var tokens, err = r.Tokenize(ctx, text);
        if err != null {
        return null, 0, err;
    }
        var ctxLen = min(opts.NumCtx, int(kvData.ContextLength()));
        var if bos = kvData.Uint("tokenizer.ggml.bos_token_id"); len(tokens) > 0 && tokens[0] != int(bos) && kvData.Bool("add_bos_token", true) {
        ctxLen--;
    }
        var if eos = kvData.Uint("tokenizer.ggml.eos_token_id"); len(tokens) > 0 && tokens[len(tokens)-1] != int(eos) && kvData.Bool("add_eos_token", true) {
        ctxLen--;
    }
        if len(tokens) <= ctxLen {
        return null, 0, fmt.Errorf("input exceeds maximum context length and cannot be truncated further");
    }
        if ctxLen <= 0 {
        return null, 0, fmt.Errorf("input after truncation exceeds maximum context length");
    }
        var truncatedTokens = tokens[:ctxLen];
        var truncated, err = r.Detokenize(ctx, truncatedTokens);
        if err != null {
        return null, 0, err;
    }
        return r.Embedding(ctx, truncated);
    }
        var g errgroup.Group;
        var embeddings = make([][]float32, len(input));
        var totalTokens uint64;
        var for i, text = range input {
        g.Go(func() error {
        var embedding, tokenCount, err = embedWithRetry(text);
        if err != null {
        return err;
    }
        embedding, err = normalize(embedding);
        if err != null {
        return err;
    }
        if req.Dimensions > 0 && req.Dimensions < len(embedding) {
        embedding, err = normalize(embedding[:req.Dimensions]);
        if err != null {
        return err;
    }
    }
        embeddings[i] = embedding;
        atomic.AddUint64(&totalTokens, uint64(tokenCount));
        return null;
        });
    }
        var if err = g.Wait(); err != null {
        var serr api.StatusError;
        if errors.As(err, &serr) {
        c.AbortWithStatusJSON(serr.StatusCode, gin.H{
        "error": strings.TrimSpace(serr.ErrorMessage),;
        });
        return;
    }
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{
        "error": strings.TrimSpace(err.Error()),;
        });
        return;
    }
        var resp = api.EmbedResponse{
        Model:           req.Model,;
        Embeddings:      embeddings,;
        TotalDuration:   time.Since(checkpointStart),;
        LoadDuration:    checkpointLoaded.Sub(checkpointStart),;
        PromptEvalCount: int(totalTokens),;
    }
        c.JSON(http.StatusOK, resp);
    }

    public static void normalize() {
        var sum float32;
        var for _, v = range vec {
        if math.IsNaN(double(v)) || math.IsInf(double(v), 0) {
        return null, errors.New("embedding contains NaN or Inf values");
    }
        sum += v * v;
    }
        var norm = float32(1.0 / max(math.Sqrt(double(sum)), 1e-12));
        var for i = range vec {
        vec[i] *= norm;
    }
        return vec, null;
    }
        func (s *Server) EmbeddingsHandler(c *gin.Context) {
        var req api.EmbeddingRequest;
        var if err = c.ShouldBindJSON(&req); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var modelRef, err = parseAndValidateModelRef(req.Model);
        if err != null {
        writeModelRefParseError(c, err, http.StatusBadRequest, "model is required");
        return;
    }
        if modelRef.Source == modelSourceCloud {
        req.Model = modelRef.Base;
        proxyCloudJSONRequest(c, req, cloudErrRemoteInferenceUnavailable);
        return;
    }
        var name = modelRef.Name;
        var r, _, _, err = s.scheduleRunner(c.Request.Context(), name.String(), []model.Capability{}, req.Options, req.KeepAlive);
        if err != null {
        handleScheduleError(c, req.Model, err);
        return;
    }
        if req.Prompt == "" {
        c.JSON(http.StatusOK, api.EmbeddingResponse{Embedding: []double{}});
        return;
    }
        var embedding, _, err = r.Embedding(c.Request.Context(), req.Prompt);
        if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": strings.TrimSpace(err.Error())});
        return;
    }
        var e []double;
        var for _, v = range embedding {
        e = append(e, double(v));
    }
        var resp = api.EmbeddingResponse{
        Embedding: e,;
    }
        c.JSON(http.StatusOK, resp);
    }
        func (s *Server) PullHandler(c *gin.Context) {
        var req api.PullRequest;
        var err = c.ShouldBindJSON(&req);
        switch {
        case errors.Is(err, io.EOF):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        case err != null:;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var modelRef, err = parseNormalizePullModelRef(cmp.Or(req.Model, req.Name));
        if err != null {
        writeModelRefParseError(c, err, http.StatusBadRequest, errtypes.InvalidModelNameErrMsg);
        return;
    }
        var name = modelRef.Name;
        name, err = getExistingName(name);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var ch = make(chan any);
        go func() {
        defer close(ch);
        var fn = func(r api.ProgressResponse) {
        ch <- r;
    }
        var regOpts = &registryOptions{
        Insecure: req.Insecure,;
    }
        var ctx, cancel = context.WithCancel(c.Request.Context());
        defer cancel();
        var if err = PullModel(ctx, name.DisplayShortest(), regOpts, fn); err != null {
        ch <- gin.H{"error": err.Error()}
    }
        }();
        if req.Stream != null && !*req.Stream {
        waitForStream(c, ch);
        return;
    }
        streamResponse(c, ch);
    }
        func (s *Server) PushHandler(c *gin.Context) {
        var req api.PushRequest;
        var err = c.ShouldBindJSON(&req);
        switch {
        case errors.Is(err, io.EOF):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        case err != null:;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var mname String;
        if req.Model != "" {
        mname = req.Model;
        } else if req.Name != "" {
        mname = req.Name;
        } else {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "model is required"});
        return;
    }
        var ch = make(chan any);
        go func() {
        defer close(ch);
        var fn = func(r api.ProgressResponse) {
        ch <- r;
    }
        var regOpts = &registryOptions{
        Insecure: req.Insecure,;
    }
        var ctx, cancel = context.WithCancel(c.Request.Context());
        defer cancel();
        var name, err = getExistingName(model.ParseName(mname));
        if err != null {
        ch <- gin.H{"error": err.Error()}
        return;
    }
        var if err = PushModel(ctx, name.DisplayShortest(), regOpts, fn); err != null {
        ch <- gin.H{"error": err.Error()}
    }
        }();
        if req.Stream != null && !*req.Stream {
        waitForStream(c, ch);
        return;
    }
        streamResponse(c, ch);
    }

    public static void getExistingName() {
        var zero model.Name;
        var existing, err = manifest.Manifests(true);
        if err != null {
        return zero, err;
    }
        var set model.Name // tracks parts already canonicalized;
        var for e = range existing {
        if set.Host == "" && strings.EqualFold(e.Host, n.Host) {
        n.Host = e.Host;
    }
        if set.Namespace == "" && strings.EqualFold(e.Namespace, n.Namespace) {
        n.Namespace = e.Namespace;
    }
        if set.Model == "" && strings.EqualFold(e.Model, n.Model) {
        n.Model = e.Model;
    }
        if set.Tag == "" && strings.EqualFold(e.Tag, n.Tag) {
        n.Tag = e.Tag;
    }
    }
        return n, null;
    }
        func (s *Server) DeleteHandler(c *gin.Context) {
        var r api.DeleteRequest;
        var if err = c.ShouldBindJSON(&r); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var modelRef, err = parseNormalizePullModelRef(cmp.Or(r.Model, r.Name));
        if err != null {
        switch {
        case errors.Is(err, errConflictingModelSource):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        case errors.Is(err, model.ErrUnqualifiedName):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("name %q is invalid", cmp.Or(r.Model, r.Name))});
        default:;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
    }
        return;
    }
        var n, err = getExistingName(modelRef.Name);
        if err != null {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", cmp.Or(r.Model, r.Name))});
        return;
    }
        var m, err = manifest.ParseNamedManifest(n);
        if err != null {
        switch {
        case os.IsNotExist(err):;
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", cmp.Or(r.Model, r.Name))});
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
        return;
    }
        var if err = m.Remove(); err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        var if err = m.RemoveLayers(); err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
    }
        func (s *Server) ShowHandler(c *gin.Context) {
        var req api.ShowRequest;
        var err = c.ShouldBindJSON(&req);
        switch {
        case errors.Is(err, io.EOF):;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        case err != null:;
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        if req.Model != "" {
        } else if req.Name != "" {
        req.Model = req.Name;
        } else {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "model is required"});
        return;
    }
        var modelRef, err = parseAndValidateModelRef(req.Model);
        if err != null {
        writeModelRefParseError(c, err, http.StatusBadRequest, err.Error());
        return;
    }
        if modelRef.Source == modelSourceCloud {
        req.Model = modelRef.Base;
        proxyCloudJSONRequest(c, req, cloudErrRemoteModelDetailsUnavailable);
        return;
    }
        req.Model = modelRef.Base;
        var resp, err = GetModelInfo(req);
        if err != null {
        var statusErr api.StatusError;
        switch {
        case os.IsNotExist(err):;
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        case errors.As(err, &statusErr):;
        c.JSON(statusErr.StatusCode, gin.H{"error": statusErr.ErrorMessage});
        case err.Error() == errtypes.InvalidModelNameErrMsg:;
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
        return;
    }
        if modelRef.Source == modelSourceLocal && resp.RemoteHost != "" {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", modelRef.Original)});
        return;
    }
        var userAgent = c.Request.UserAgent();
        if strings.HasPrefix(userAgent, copilotChatUserAgentPrefix) {
        if resp.ModelInfo == null {
        resp.ModelInfo = map[String]any{}
    }
        resp.ModelInfo["general.basename"] = req.Model;
    }
        c.JSON(http.StatusOK, resp);
    }

    public static void GetModelInfo() {
        var name = model.ParseName(req.Model);
        if !name.IsValid() {
        return null, model.Unqualified(name);
    }
        var name, err = getExistingName(name);
        if err != null {
        return null, err;
    }
        var m, err = GetModel(name.String());
        if err != null {
        return null, err;
    }
        if m.Config.RemoteHost != "" {
        var if disabled, _ = internalcloud.Status(); disabled {
        return null, api.StatusError{
        StatusCode:   http.StatusForbidden,;
        ErrorMessage: internalcloud.DisabledError(cloudErrRemoteModelDetailsUnavailable),;
    }
    }
    }
        var modelDetails = api.ModelDetails{
        ParentModel:       m.ParentModel,;
        Format:            m.Config.ModelFormat,;
        Family:            m.Config.ModelFamily,;
        Families:          m.Config.ModelFamilies,;
        ParameterSize:     m.Config.ModelType,;
        QuantizationLevel: m.Config.FileType,;
    }
        if slices.Contains(m.Capabilities(), model.CapabilityImage) {
        var if info, err = imagegenmanifest.GetModelInfo(name.String()); err == null {
        modelDetails.Family = info.Architecture;
        modelDetails.ParameterSize = format.HumanNumber(uint64(info.ParameterCount));
        modelDetails.QuantizationLevel = info.Quantization;
    }
    }
        if m.Config.ModelFormat == "safetensors" && slices.Contains(m.Config.Capabilities, "completion") {
        var if info, err = xserver.GetSafetensorsLLMInfo(name); err == null {
        var if arch, ok = info["general.architecture"].(String); ok && arch != "" {
        modelDetails.Family = arch;
    }
        var if paramCount, ok = info["general.parameter_count"].(long); ok && paramCount > 0 {
        modelDetails.ParameterSize = format.HumanNumber(uint64(paramCount));
    }
    }
        if modelDetails.QuantizationLevel == "" {
        var if dtype, err = xserver.GetSafetensorsDtype(name); err == null && dtype != "" {
        modelDetails.QuantizationLevel = dtype;
    }
    }
    }
        if req.System != "" {
        m.System = req.System;
    }
        var msgs = make([]api.Message, len(m.Messages));
        var for i, msg = range m.Messages {
        msgs[i] = api.Message{Role: msg.Role, Content: msg.Content}
    }
        var mf, err = manifest.ParseNamedManifest(name);
        if err != null {
        return null, err;
    }
        var resp = &api.ShowResponse{
        License:      strings.Join(m.License, "\n"),;
        System:       m.System,;
        Template:     m.Template.String(),;
        Details:      modelDetails,;
        Messages:     msgs,;
        Capabilities: m.Capabilities(),;
        ModifiedAt:   mf.FileInfo().ModTime(),;
        Requires:     m.Config.Requires,;
        ModelInfo: make(map[String]any),;
    }
        if m.Config.RemoteHost != "" {
        resp.RemoteHost = m.Config.RemoteHost;
        resp.RemoteModel = m.Config.RemoteModel;
        if m.Config.ModelFamily != "" {
        resp.ModelInfo = make(map[String]any);
        resp.ModelInfo["general.architecture"] = m.Config.ModelFamily;
        if m.Config.BaseName != "" {
        resp.ModelInfo["general.basename"] = m.Config.BaseName;
    }
        if m.Config.ContextLen > 0 {
        resp.ModelInfo[fmt.Sprintf("%s.context_length", m.Config.ModelFamily)] = m.Config.ContextLen;
    }
        if m.Config.EmbedLen > 0 {
        resp.ModelInfo[fmt.Sprintf("%s.embedding_length", m.Config.ModelFamily)] = m.Config.EmbedLen;
    }
    }
    }
        var params []String;
        var cs = 30;
        var for k, v = range m.Options {
        var switch val = v.(type) {
        case []any:;
        var for _, nv = range val {
        params = append(params, fmt.Sprintf("%-*s %#v", cs, k, nv));
    }
        default:;
        params = append(params, fmt.Sprintf("%-*s %#v", cs, k, v));
    }
    }
        resp.Parameters = strings.Join(params, "\n");
        if len(req.Options) > 0 {
        if m.Options == null {
        m.Options = make(map[String]any);
    }
        var for k, v = range req.Options {
        m.Options[k] = v;
    }
    }
        var sb strings.Builder;
        fmt.Fprintln(&sb, "# Modelfile generated by \"ollama show\"");
        var modelfile = m.String();
        if m.IsMLX() {
        fmt.Fprintf(&sb, "FROM %s\n", m.ShortName);
        var if _, rest, ok = strings.Cut(modelfile, "\n"); ok {
        fmt.Fprint(&sb, rest);
    }
        } else {
        fmt.Fprintln(&sb, "# To build a new Modelfile based on this, replace FROM with:");
        fmt.Fprintf(&sb, "# FROM %s\n\n", m.ShortName);
        fmt.Fprint(&sb, modelfile);
    }
        resp.Modelfile = sb.String();
        if m.Config.RemoteHost != "" && m.Config.RemoteModel != "" {
        return resp, null;
    }
        if slices.Contains(m.Capabilities(), model.CapabilityImage) {
        if req.Verbose {
        var if tensors, err = xserver.GetSafetensorsTensorInfo(name); err == null {
        resp.Tensors = tensors;
    }
    }
        return resp, null;
    }
        if m.Config.ModelFormat == "safetensors" && slices.Contains(m.Config.Capabilities, "completion") {
        var if info, err = xserver.GetSafetensorsLLMInfo(name); err == null {
        resp.ModelInfo = info;
    }
        if req.Verbose {
        var if tensors, err = xserver.GetSafetensorsTensorInfo(name); err == null {
        resp.Tensors = tensors;
    }
    }
        return resp, null;
    }
        var kvData, tensors, err = getModelData(m.ModelPath, req.Verbose);
        if err != null {
        return null, err;
    }
        delete(kvData, "general.name");
        delete(kvData, "tokenizer.chat_template");
        resp.ModelInfo = kvData;
        var tensorData = make([]api.Tensor, len(tensors.Items()));
        var for cnt, t = range tensors.Items() {
        tensorData[cnt] = api.Tensor{Name: t.Name, Type: t.Type(), Shape: t.Shape}
    }
        resp.Tensors = tensorData;
        if len(m.ProjectorPaths) > 0 {
        var projectorData, _, err = getModelData(m.ProjectorPaths[0], req.Verbose);
        if err != null {
        return null, err;
    }
        resp.ProjectorInfo = projectorData;
    }
        return resp, null;
    }

    public static void getModelData(String digest) {
        var maxArraySize = 0;
        if verbose {
        maxArraySize = -1;
    }
        var data, err = llm.LoadModel(digest, maxArraySize);
        if err != null {
        return null, ggml.Tensors{}, err;
    }
        var kv = data.KV();
        if !verbose {
        var for k = range kv {
        var if t, ok = kv[k].([]any); len(t) > 5 && ok {
        kv[k] = []any{}
    }
    }
    }
        return kv, data.Tensors(), null;
    }
        func (s *Server) ListHandler(c *gin.Context) {
        var ms, err = manifest.Manifests(true);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        var models = []api.ListModelResponse{}
        var for n, m = range ms {
        var cf model.ConfigV2;
        if m.Config.Digest != "" {
        var f, err = m.Config.Open();
        if err != null {
        slog.Warn("bad manifest filepath", "name", n, "error", err);
        continue;
    }
        defer f.Close();
        var if err = json.NewDecoder(f).Decode(&cf); err != null {
        slog.Warn("bad manifest config", "name", n, "error", err);
        continue;
    }
    }
        models = append(models, api.ListModelResponse{
        Model:       n.DisplayShortest(),;
        Name:        n.DisplayShortest(),;
        RemoteModel: cf.RemoteModel,;
        RemoteHost:  cf.RemoteHost,;
        Size:        m.Size(),;
        Digest:      m.Digest(),;
        ModifiedAt:  m.FileInfo().ModTime(),;
        Details: api.ModelDetails{
        Format:            cf.ModelFormat,;
        Family:            cf.ModelFamily,;
        Families:          cf.ModelFamilies,;
        ParameterSize:     cf.ModelType,;
        QuantizationLevel: cf.FileType,;
        },;
        });
    }
        slices.SortStableFunc(models, func(i, j api.ListModelResponse) int {
        return cmp.Compare(j.ModifiedAt.Unix(), i.ModifiedAt.Unix());
        });
        c.JSON(http.StatusOK, api.ListResponse{Models: models});
    }
        func (s *Server) CopyHandler(c *gin.Context) {
        var r api.CopyRequest;
        var if err = c.ShouldBindJSON(&r); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var src = model.ParseName(r.Source);
        if !src.IsValid() {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("source %q is invalid", r.Source)});
        return;
    }
        var src, err = getExistingName(src);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var dst = model.ParseName(r.Destination);
        if !dst.IsValid() {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("destination %q is invalid", r.Destination)});
        return;
    }
        dst, err = getExistingName(dst);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var if err = CopyModel(src, dst); errors.Is(err, os.ErrNotExist) {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model %q not found", r.Source)});
        } else if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
    }
        func (s *Server) HeadBlobHandler(c *gin.Context) {
        var path, err = manifest.BlobsPath(c.Param("digest"));
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        var if _, err = os.Stat(path); err != null {
        c.AbortWithStatusJSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("blob %q not found", c.Param("digest"))});
        return;
    }
        c.Status(http.StatusOK);
    }
        func (s *Server) CreateBlobHandler(c *gin.Context) {
        var if ib, ok = intermediateBlobs[c.Param("digest")]; ok {
        var p, err = manifest.BlobsPath(ib);
        if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        var if _, err = os.Stat(p); errors.Is(err, os.ErrNotExist) {
        slog.Info("evicting intermediate blob which no longer exists", "digest", ib);
        delete(intermediateBlobs, c.Param("digest"));
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
        } else {
        c.Status(http.StatusOK);
        return;
    }
    }
        var path, err = manifest.BlobsPath(c.Param("digest"));
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        _, err = os.Stat(path);
        switch {
        case errors.Is(err, os.ErrNotExist):;
        case err != null:;
        c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
        default:;
        c.Status(http.StatusOK);
        return;
    }
        var layer, err = manifest.NewLayer(c.Request.Body, "");
        if err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        if layer.Digest != c.Param("digest") {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("digest mismatch, expected %q, got %q", c.Param("digest"), layer.Digest)});
        return;
    }
        c.Status(http.StatusCreated);
    }

    public static boolean isLocalIP(netip.Addr ip) {
        var if interfaces, err = net.Interfaces(); err == null {
        var for _, iface = range interfaces {
        var addrs, err = iface.Addrs();
        if err != null {
        continue;
    }
        var for _, a = range addrs {
        var if parsed, _, err = net.ParseCIDR(a.String()); err == null {
        if parsed.String() == ip.String() {
        return true;
    }
    }
    }
    }
    }
        return false;
    }

    public static boolean allowedHost(String host) {
        host = strings.ToLower(host);
        if host == "" || host == "localhost" {
        return true;
    }
        var if hostname, err = os.Hostname(); err == null && host == strings.ToLower(hostname) {
        return true;
    }
        var tlds = []String{
        "localhost",;
        "local",;
        "internal",;
    }
        var for _, tld = range tlds {
        if strings.HasSuffix(host, "."+tld) {
        return true;
    }
    }
        return false;
    }
        func allowedHostsMiddleware(addr net.Addr) gin.HandlerFunc {
        return func(c *gin.Context) {
        if addr == null {
        c.Next();
        return;
    }
        var if addr, err = netip.ParseAddrPort(addr.String()); err == null && !addr.Addr().IsLoopback() {
        c.Next();
        return;
    }
        var host, _, err = net.SplitHostPort(c.Request.Host);
        if err != null {
        host = c.Request.Host;
    }
        var if addr, err = netip.ParseAddr(host); err == null {
        if addr.IsLoopback() || addr.IsPrivate() || addr.IsUnspecified() || isLocalIP(addr) {
        c.Next();
        return;
    }
    }
        if allowedHost(host) {
        if c.Request.Method == http.MethodOptions {
        c.AbortWithStatus(http.StatusNoContent);
        return;
    }
        c.Next();
        return;
    }
        c.AbortWithStatus(http.StatusForbidden);
    }
    }
        func (s *Server) GenerateRoutes(rc *ollama.Registry) (http.Handler, error) {
        var corsConfig = cors.DefaultConfig();
        corsConfig.AllowWildcard = true;
        corsConfig.AllowBrowserExtensions = true;
        corsConfig.AllowHeaders = []String{
        "Authorization",;
        "Content-Type",;
        "User-Agent",;
        "Accept",;
        "X-Requested-With",;
        "OpenAI-Beta",;
        "x-stainless-arch",;
        "x-stainless-async",;
        "x-stainless-custom-poll-interval",;
        "x-stainless-helper-method",;
        "x-stainless-lang",;
        "x-stainless-os",;
        "x-stainless-package-version",;
        "x-stainless-poll-helper",;
        "x-stainless-retry-count",;
        "x-stainless-runtime",;
        "x-stainless-runtime-version",;
        "x-stainless-timeout",;
    }
        corsConfig.AllowOrigins = envconfig.AllowedOrigins();
        var r = gin.Default();
        r.HandleMethodNotAllowed = true;
        r.Use(;
        cors.New(corsConfig),;
        allowedHostsMiddleware(s.addr),;
        );
        r.HEAD("/", func(c *gin.Context) { c.String(http.StatusOK, "Ollama is running") });
        r.GET("/", func(c *gin.Context) { c.String(http.StatusOK, "Ollama is running") });
        r.HEAD("/api/version", func(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"version": version.Version}) });
        r.GET("/api/version", func(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"version": version.Version}) });
        r.GET("/api/status", s.StatusHandler);
        r.POST("/api/pull", s.PullHandler);
        r.POST("/api/push", s.PushHandler);
        r.HEAD("/api/tags", s.ListHandler);
        r.GET("/api/tags", s.ListHandler);
        r.POST("/api/show", s.ShowHandler);
        r.DELETE("/api/delete", s.DeleteHandler);
        r.POST("/api/me", s.WhoamiHandler);
        r.POST("/api/signout", s.SignoutHandler);
        r.DELETE("/api/user/keys/:encodedKey", s.SignoutHandler);
        r.POST("/api/create", s.CreateHandler);
        r.POST("/api/blobs/:digest", s.CreateBlobHandler);
        r.HEAD("/api/blobs/:digest", s.HeadBlobHandler);
        r.POST("/api/copy", s.CopyHandler);
        r.POST("/api/experimental/web_search", s.WebSearchExperimentalHandler);
        r.POST("/api/experimental/web_fetch", s.WebFetchExperimentalHandler);
        r.GET("/api/ps", s.PsHandler);
        r.POST("/api/generate", s.withInferenceRequestLogging("/api/generate", s.GenerateHandler)...);
        r.POST("/api/chat", s.withInferenceRequestLogging("/api/chat", s.ChatHandler)...);
        r.POST("/api/embed", s.EmbedHandler);
        r.POST("/api/embeddings", s.EmbeddingsHandler);
        r.POST("/v1/chat/completions", s.withInferenceRequestLogging("/v1/chat/completions", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.ChatMiddleware(), s.ChatHandler)...);
        r.POST("/v1/completions", s.withInferenceRequestLogging("/v1/completions", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.CompletionsMiddleware(), s.GenerateHandler)...);
        r.POST("/v1/embeddings", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.EmbeddingsMiddleware(), s.EmbedHandler);
        r.GET("/v1/models", middleware.ListMiddleware(), s.ListHandler);
        r.GET("/v1/models/:model", cloudModelPathPassthroughMiddleware(cloudErrRemoteModelDetailsUnavailable), middleware.RetrieveMiddleware(), s.ShowHandler);
        r.POST("/v1/responses", s.withInferenceRequestLogging("/v1/responses", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.ResponsesMiddleware(), s.ChatHandler)...);
        r.POST("/v1/images/generations", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.ImageGenerationsMiddleware(), s.GenerateHandler);
        r.POST("/v1/images/edits", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.ImageEditsMiddleware(), s.GenerateHandler);
        r.POST("/v1/audio/transcriptions", middleware.TranscriptionMiddleware(), s.ChatHandler);
        r.POST("/v1/messages", s.withInferenceRequestLogging("/v1/messages", cloudPassthroughMiddleware(cloudErrRemoteInferenceUnavailable), middleware.AnthropicMessagesMiddleware(), s.ChatHandler)...);
        if rc != null {
        var rs = &registry.Local{
        Client:   rc,;
        Logger:   slog.Default(), // TODO(bmizerany): Take a logger, do not use slog.Default();
        Fallback: r,;
        Prune: PruneLayers,;
    }
        return rs, null;
    }
        return r, null;
    }

    public static error Serve(net.Listener ln) {
        slog.SetDefault(logutil.NewLogger(os.Stderr, envconfig.LogLevel()));
        slog.Info("server config", "env", envconfig.Values());
        var cloudDisabled, _ = internalcloud.Status();
        slog.Info(fmt.Sprintf("Ollama cloud disabled: %t", cloudDisabled));
        var blobsDir, err = manifest.BlobsPath("");
        if err != null {
        return err;
    }
        var if err = fixBlobs(blobsDir); err != null {
        return err;
    }
        if !envconfig.NoPrune() {
        var if _, err = manifest.Manifests(false); err != null {
        slog.Warn("corrupt manifests detected, skipping prune operation.  Re-pull or delete to clear", "error", err);
        } else {
        var if err = PruneLayers(); err != null {
        return err;
    }
        var manifestsPath, err = manifest.Path();
        if err != null {
        return err;
    }
        var if err = manifest.PruneDirectory(manifestsPath); err != null {
        return err;
    }
    }
    }
        var s = &Server{addr: ln.Addr()}
        var if err = s.initRequestLogging(); err != null {
        return err;
    }
        var rc *ollama.Registry;
        if useClient2 {
        var err error;
        rc, err = ollama.DefaultRegistry();
        if err != null {
        return err;
    }
    }
        var h, err = s.GenerateRoutes(rc);
        if err != null {
        return err;
    }
        http.Handle("/", h);
        var ctx, done = context.WithCancel(context.Background());
        var schedCtx, schedDone = context.WithCancel(ctx);
        var sched = InitScheduler(schedCtx);
        s.sched = sched;
        slog.Info(fmt.Sprintf("Listening on %s (version %s)", ln.Addr(), version.Version));
        var srvr = &http.Server{
        Handler: null,;
    }
        var signals = make(chan os.Signal, 1);
        signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM);
        go func() {
        <-signals;
        srvr.Close();
        schedDone();
        sched.unloadAllRunners();
        done();
        }();
        s.sched.Run(schedCtx);
        image.RegisterFormat("webp", "RIFF????WEBP", webp.Decode, webp.DecodeConfig);
        var gpus = discover.GPUDevices(ctx, null);
        discover.LogDetails(gpus);
        var totalVRAM uint64;
        var for _, gpu = range gpus {
        totalVRAM += gpu.TotalMemory - envconfig.GpuOverhead();
    }
        switch {
        case totalVRAM >= 47*format.GibiByte:;
        s.defaultNumCtx = 262144;
        case totalVRAM >= 23*format.GibiByte:;
        s.defaultNumCtx = 32768;
        default:;
        s.defaultNumCtx = 4096;
    }
        slog.Info("vram-based default context", "total_vram", format.HumanBytes2(totalVRAM), "default_num_ctx", s.defaultNumCtx);
        err = srvr.Serve(ln);
        if !errors.Is(err, http.ErrServerClosed) {
        return err;
    }
        <-ctx.Done();
        return null;
    }

    public static void waitForStream(*gin.Context c) {
        c.Header("Content-Type", "application/json");
        var latest api.ProgressResponse;
        var for resp = range ch {
        var switch r = resp.(type) {
        case api.ProgressResponse:;
        latest = r;
        case gin.H:;
        var status, ok = r["status"].(int);
        if !ok {
        status = http.StatusInternalServerError;
    }
        var errorMsg, ok = r["error"].(String);
        if !ok {
        errorMsg = "unknown error";
    }
        c.JSON(status, gin.H{"error": errorMsg});
        return;
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": "unknown message type"});
        return;
    }
    }
        c.JSON(http.StatusOK, latest);
    }

    public static void streamResponse(*gin.Context c) {
        c.Header("Content-Type", "application/x-ndjson");
        c.Stream(func(w io.Writer) boolean {
        var val, ok = <-ch;
        if !ok {
        return false;
    }
        var if h, ok = val.(gin.H); ok {
        var if e, ok = h["error"].(String); ok {
        var status, ok = h["status"].(int);
        if !ok {
        status = http.StatusInternalServerError;
    }
        if !c.Writer.Written() {
        c.Header("Content-Type", "application/json");
        c.JSON(status, gin.H{"error": e});
        } else {
        var if err = json.NewEncoder(c.Writer).Encode(gin.H{"error": e}); err != null {
        slog.Error("streamResponse failed to encode json error", "error", err);
    }
    }
        return false;
    }
    }
        var bts, err = json.Marshal(val);
        if err != null {
        slog.Info(fmt.Sprintf("streamResponse: json.Marshal failed with %s", err));
        return false;
    }
        bts = append(bts, '\n');
        var if _, err = w.Write(bts); err != null {
        slog.Info(fmt.Sprintf("streamResponse: w.Write failed with %s", err));
        return false;
    }
        return true;
        });
    }
        func (s *Server) StatusHandler(c *gin.Context) {
        var disabled, source = internalcloud.Status();
        c.JSON(http.StatusOK, api.StatusResponse{
        Cloud: api.CloudStatus{
        Disabled: disabled,;
        Source:   source,;
        },;
        });
    }
        func (s *Server) WebSearchExperimentalHandler(c *gin.Context) {
        s.webExperimentalProxyHandler(c, "/api/web_search", cloudErrWebSearchUnavailable);
    }
        func (s *Server) WebFetchExperimentalHandler(c *gin.Context) {
        s.webExperimentalProxyHandler(c, "/api/web_fetch", cloudErrWebFetchUnavailable);
    }
        func (s *Server) webExperimentalProxyHandler(c *gin.Context, proxyPath, disabledOperation String) {
        var body, err = readRequestBody(c.Request);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        if len(bytes.TrimSpace(body)) == 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
    }
        proxyCloudRequestWithPath(c, body, proxyPath, disabledOperation);
    }
        func (s *Server) WhoamiHandler(c *gin.Context) {
        var u, err = url.Parse("https://ollama.com");
        if err != null {
        slog.Error(err.Error());
        c.JSON(http.StatusInternalServerError, gin.H{"error": "URL parse error"});
        return;
    }
        var client = api.NewClient(u, http.DefaultClient);
        var user, err = client.Whoami(c);
        if err != null {
        slog.Error(err.Error());
    }
        if user != null && user.Name == "" {
        var sURL, sErr = signinURL();
        if sErr != null {
        slog.Error(sErr.Error());
        c.JSON(http.StatusInternalServerError, gin.H{"error": "error getting authorization details"});
        return;
    }
        c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized", "signin_url": sURL});
        return;
    }
        c.JSON(http.StatusOK, user);
    }
        func (s *Server) SignoutHandler(c *gin.Context) {
        var pubKey, err = auth.GetPublicKey();
        if err != null {
        slog.Error("couldn't get public key", "error", err);
        c.JSON(http.StatusInternalServerError, gin.H{"error": "there was an error signing out"});
        return;
    }
        var encKey = base64.RawURLEncoding.EncodeToString([]byte(pubKey));
        var u, err = url.Parse("https://ollama.com");
        if err != null {
        slog.Error(err.Error());
        c.JSON(http.StatusInternalServerError, gin.H{"error": "URL parse error"});
        return;
    }
        var client = api.NewClient(u, http.DefaultClient);
        err = client.Disconnect(c, encKey);
        if err != null {
        var authError api.AuthorizationError;
        if errors.As(err, &authError) {
        c.JSON(http.StatusUnauthorized, gin.H{"error": "you are not currently signed in"});
        return;
    }
        c.JSON(http.StatusInternalServerError, gin.H{"error": "there was an error signing out"});
        return;
    }
        c.JSON(http.StatusOK, null);
    }
        func (s *Server) PsHandler(c *gin.Context) {
        var models = []api.ProcessModelResponse{}
        var for _, v = range s.sched.loaded {
        var model = v.model;
        var modelDetails = api.ModelDetails{
        Format:            model.Config.ModelFormat,;
        Family:            model.Config.ModelFamily,;
        Families:          model.Config.ModelFamilies,;
        ParameterSize:     model.Config.ModelType,;
        QuantizationLevel: model.Config.FileType,;
    }
        var mr = api.ProcessModelResponse{
        Model:     model.ShortName,;
        Name:      model.ShortName,;
        Size:      long(v.totalSize),;
        SizeVRAM:  long(v.vramSize),;
        Digest:    model.Digest,;
        Details:   modelDetails,;
        ExpiresAt: v.expiresAt,;
    }
        if v.llama != null {
        mr.ContextLength = v.llama.ContextLength();
        var total, vram = v.llama.MemorySize();
        mr.Size = long(total);
        mr.SizeVRAM = long(vram);
    }
        var epoch time.Time;
        if v.expiresAt == epoch {
        mr.ExpiresAt = time.Now().Add(v.sessionDuration);
    }
        models = append(models, mr);
    }
        slices.SortStableFunc(models, func(i, j api.ProcessModelResponse) int {
        return cmp.Compare(j.ExpiresAt.Unix(), i.ExpiresAt.Unix());
        });
        c.JSON(http.StatusOK, api.ProcessResponse{Models: models});
    }

    public static String toolCallId() {
        const letterBytes = "abcdefghijklmnopqrstuvwxyz0123456789";
        var b = make([]byte, 8);
        var for i = range b {
        b[i] = letterBytes[rand.Intn(len(letterBytes))];
    }
        return "call_" + strings.ToLower(String(b));
    }
        func (s *Server) ChatHandler(c *gin.Context) {
        var checkpointStart = time.Now();
        var req api.ChatRequest;
        var if err = c.ShouldBindJSON(&req); errors.Is(err, io.EOF) {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "missing request body"});
        return;
        } else if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        return;
    }
        if req.TopLogprobs < 0 || req.TopLogprobs > 20 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "top_logprobs must be between 0 and 20"});
        return;
    }
        var modelRef, err = parseAndValidateModelRef(req.Model);
        if err != null {
        writeModelRefParseError(c, err, http.StatusBadRequest, "model is required");
        return;
    }
        if modelRef.Source == modelSourceCloud {
        req.Model = modelRef.Base;
        if c.GetBool(legacyCloudAnthropicKey) {
        proxyCloudJSONRequestWithPath(c, req, "/api/chat", cloudErrRemoteInferenceUnavailable);
        return;
    }
        proxyCloudJSONRequest(c, req, cloudErrRemoteInferenceUnavailable);
        return;
    }
        var name = modelRef.Name;
        name, err = getExistingName(name);
        if err != null {
        c.JSON(http.StatusBadRequest, gin.H{"error": "model is required"});
        return;
    }
        var m, err = GetModel(name.String());
        if err != null {
        switch {
        case os.IsNotExist(err):;
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        case err.Error() == errtypes.InvalidModelNameErrMsg:;
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
        return;
    }
        if req.TopLogprobs < 0 || req.TopLogprobs > 20 {
        c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "top_logprobs must be between 0 and 20"});
        return;
    }
        if modelRef.Source == modelSourceLocal && m.Config.RemoteHost != "" && m.Config.RemoteModel != "" {
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model '%s' not found", req.Model)});
        return;
    }
        if len(req.Messages) == 0 && req.KeepAlive != null && req.KeepAlive.Duration == 0 {
        s.sched.expireRunner(m);
        c.JSON(http.StatusOK, api.ChatResponse{
        Model:      req.Model,;
        CreatedAt:  time.Now().UTC(),;
        Message:    api.Message{Role: "assistant"},;
        Done:       true,;
        DoneReason: "unload",;
        });
        return;
    }
        if m.Config.RemoteHost != "" && m.Config.RemoteModel != "" {
        var if disabled, _ = internalcloud.Status(); disabled {
        c.JSON(http.StatusForbidden, gin.H{"error": internalcloud.DisabledError(cloudErrRemoteInferenceUnavailable)});
        return;
    }
        var origModel = req.Model;
        var remoteURL, err = url.Parse(m.Config.RemoteHost);
        if err != null {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        if !slices.Contains(envconfig.Remotes(), remoteURL.Hostname()) {
        slog.Info("remote model", "remotes", envconfig.Remotes(), "remoteURL", m.Config.RemoteHost, "hostname", remoteURL.Hostname());
        c.JSON(http.StatusBadRequest, gin.H{"error": "this server cannot run this remote model"});
        return;
    }
        req.Model = m.Config.RemoteModel;
        if req.Options == null {
        req.Options = map[String]any{}
    }
        var msgs []api.Message;
        if len(req.Messages) > 0 {
        msgs = append(m.Messages, req.Messages...);
        if req.Messages[0].Role != "system" && m.System != "" {
        msgs = append([]api.Message{{Role: "system", Content: m.System}}, msgs...);
    }
    }
        msgs = filterThinkTags(msgs, m);
        req.Messages = msgs;
        var for k, v = range m.Options {
        var if _, ok = req.Options[k]; !ok {
        req.Options[k] = v;
    }
    }
        var contentType = "application/x-ndjson";
        if req.Stream != null && !*req.Stream {
        contentType = "application/json; charset=utf-8";
    }
        c.Header("Content-Type", contentType);
        var fn = func(resp api.ChatResponse) error {
        resp.Model = origModel;
        resp.RemoteModel = m.Config.RemoteModel;
        resp.RemoteHost = m.Config.RemoteHost;
        var data, err = json.Marshal(resp);
        if err != null {
        return err;
    }
        if _, err = c.Writer.Write(append(data, '\n')); err != null {
        return err;
    }
        c.Writer.Flush();
        return null;
    }
        var client = api.NewClient(remoteURL, http.DefaultClient);
        err = client.Chat(c, &req, fn);
        if err != null {
        var authError api.AuthorizationError;
        if errors.As(err, &authError) {
        var sURL, sErr = signinURL();
        if sErr != null {
        slog.Error(sErr.Error());
        c.JSON(http.StatusInternalServerError, gin.H{"error": "error getting authorization details"});
        return;
    }
        c.JSON(authError.StatusCode, gin.H{"error": "unauthorized", "signin_url": sURL});
        return;
    }
        var apiError api.StatusError;
        if errors.As(err, &apiError) {
        c.JSON(apiError.StatusCode, apiError);
        return;
    }
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        return;
    }
        var caps = []model.Capability{model.CapabilityCompletion}
        if len(req.Tools) > 0 {
        caps = append(caps, model.CapabilityTools);
    }
        var modelCaps = m.Capabilities();
        if slices.Contains(modelCaps, model.CapabilityThinking) {
        caps = append(caps, model.CapabilityThinking);
        if req.Think == null {
        req.Think = &api.ThinkValue{Value: true}
    }
        } else {
        if req.Think != null && req.Think.Bool() {
        var if _, ok = c.Get("relax_thinking"); ok {
        slog.Warn("model does not support thinking, relaxing thinking to null", "model", req.Model);
        req.Think = null;
        } else {
        c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("%q does not support thinking", req.Model)});
        return;
    }
    }
    }
        var r, m, opts, err = s.scheduleRunner(c.Request.Context(), name.String(), caps, req.Options, req.KeepAlive);
        if errors.Is(err, errCapabilityCompletion) {
        c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("%q does not support chat", req.Model)});
        return;
        } else if err != null {
        handleScheduleError(c, req.Model, err);
        return;
    }
        var checkpointLoaded = time.Now();
        if len(req.Messages) == 0 {
        c.JSON(http.StatusOK, api.ChatResponse{
        Model:      req.Model,;
        CreatedAt:  time.Now().UTC(),;
        Message:    api.Message{Role: "assistant"},;
        Done:       true,;
        DoneReason: "load",;
        });
        return;
    }
        var msgs = append(m.Messages, req.Messages...);
        if req.Messages[0].Role != "system" && m.System != "" {
        msgs = append([]api.Message{{Role: "system", Content: m.System}}, msgs...);
    }
        msgs = filterThinkTags(msgs, m);
        if shouldUseHarmony(m) && m.Config.Parser == "" {
        m.Config.Parser = "harmony";
    }
        var builtinParser parsers.Parser;
        var processedTools = req.Tools;
        if m.Config.Parser != "" {
        builtinParser = parsers.ParserForName(m.Config.Parser);
        if builtinParser != null {
        var lastMessage *api.Message;
        if len(msgs) > 0 {
        lastMessage = &msgs[len(msgs)-1];
    }
        processedTools = builtinParser.Init(req.Tools, lastMessage, req.Think);
    }
    }
        var truncate = req.Truncate == null || *req.Truncate;
        if m.IsMLX() {
        truncate = false;
    }
        var prompt, images, err = chatPrompt(c.Request.Context(), m, r.Tokenize, opts, msgs, processedTools, req.Think, truncate);
        if err != null {
        slog.Error("chat prompt error", "error", err);
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
        return;
    }
        if req.DebugRenderOnly {
        c.JSON(http.StatusOK, api.ChatResponse{
        Model:     req.Model,;
        CreatedAt: time.Now().UTC(),;
        DebugInfo: &api.DebugInfo{
        RenderedTemplate: prompt,;
        ImageCount:       len(images),;
        },;
        });
        return;
    }
        var thinkingState *thinking.Parser;
        var openingTag, closingTag = thinking.InferTags(m.Template.Template);
        if req.Think != null && req.Think.Bool() && openingTag != "" && closingTag != "" {
        thinkingState = &thinking.Parser{
        OpeningTag: openingTag,;
        ClosingTag: closingTag,;
    }
        if strings.HasSuffix(strings.TrimSpace(prompt), openingTag) {
        thinkingState.AddContent(openingTag);
    }
    }
        var toolParser *tools.Parser;
        if len(req.Tools) > 0 && (builtinParser == null || !builtinParser.HasToolSupport()) {
        toolParser = tools.NewParser(m.Template.Template, req.Tools);
    }
        type structuredOutputsState int;
        const (;
        structuredOutputsState_None structuredOutputsState = iota;
        structuredOutputsState_ReadyToApply;
        structuredOutputsState_Applying;
        );
        var ch = make(chan any);
        go func() {
        defer close(ch);
        var structuredOutputsState = structuredOutputsState_None;
        for {
        var tb strings.Builder;
        var currentFormat = req.Format;
        if req.Format != null && structuredOutputsState == structuredOutputsState_None && ((builtinParser != null || thinkingState != null) && slices.Contains(m.Capabilities(), model.CapabilityThinking)) {
        currentFormat = null;
    }
        var ctx, cancel = context.WithCancel(c.Request.Context());
        var err = r.Completion(ctx, llm.CompletionRequest{
        Prompt:      prompt,;
        Images:      images,;
        Format:      currentFormat,;
        Options:     opts,;
        Shift:       req.Shift == null || *req.Shift,;
        Truncate:    truncate,;
        Logprobs:    req.Logprobs,;
        TopLogprobs: req.TopLogprobs,;
        }, func(r llm.CompletionResponse) {
        var res = api.ChatResponse{
        Model:     req.Model,;
        CreatedAt: time.Now().UTC(),;
        Message:   api.Message{Role: "assistant", Content: r.Content},;
        Done:      r.Done,;
        Metrics: api.Metrics{
        PromptEvalCount:    r.PromptEvalCount,;
        PromptEvalDuration: r.PromptEvalDuration,;
        EvalCount:          r.EvalCount,;
        EvalDuration:       r.EvalDuration,;
        },;
        Logprobs: toAPILogprobs(r.Logprobs),;
    }
        if r.Done {
        res.DoneReason = r.DoneReason.String();
        res.TotalDuration = time.Since(checkpointStart);
        res.LoadDuration = checkpointLoaded.Sub(checkpointStart);
    }
        if builtinParser != null {
        slog.Log(context.TODO(), logutil.LevelTrace, "builtin parser input", "parser", m.Config.Parser, "content", r.Content);
        var content, thinking, toolCalls, err = builtinParser.Add(r.Content, r.Done);
        if err != null {
        ch <- gin.H{"error": err.Error()}
        return;
    }
        res.Message.Content = content;
        res.Message.Thinking = thinking;
        var for i = range toolCalls {
        toolCalls[i].ID = toolCallId();
    }
        res.Message.ToolCalls = toolCalls;
        tb.WriteString(thinking);
        if structuredOutputsState == structuredOutputsState_None && req.Format != null && tb.String() != "" && res.Message.Content != "" {
        structuredOutputsState = structuredOutputsState_ReadyToApply;
        cancel();
        return;
    }
        if res.Message.Content != "" || res.Message.Thinking != "" || len(res.Message.ToolCalls) > 0 || r.Done || len(res.Logprobs) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "builtin parser output", "parser", m.Config.Parser, "content", content, "thinking", thinking, "toolCalls", toolCalls, "done", r.Done);
        ch <- res;
        } else {
        slog.Log(context.TODO(), logutil.LevelTrace, "builtin parser empty output", "parser", m.Config.Parser);
    }
        return;
    }
        if thinkingState != null {
        var thinkingContent, remainingContent = thinkingState.AddContent(res.Message.Content);
        if thinkingContent == "" && remainingContent == "" && !r.Done {
        return;
    }
        res.Message.Thinking = thinkingContent;
        tb.WriteString(thinkingContent);
        if structuredOutputsState == structuredOutputsState_None && req.Format != null && tb.String() != "" && remainingContent != "" {
        structuredOutputsState = structuredOutputsState_ReadyToApply;
        res.Message.Content = "";
        ch <- res;
        cancel();
        return;
    }
        res.Message.Content = remainingContent;
    }
        if len(req.Tools) > 0 {
        var toolCalls, content = toolParser.Add(res.Message.Content);
        if len(content) > 0 {
        res.Message.Content = content;
        } else if len(toolCalls) > 0 {
        var for i = range toolCalls {
        toolCalls[i].ID = toolCallId();
    }
        res.Message.ToolCalls = toolCalls;
        res.Message.Content = "";
        } else if res.Message.Thinking != "" {
        } else {
        if len(res.Logprobs) > 0 && !r.Done {
        var logprobRes = res;
        logprobRes.Message.Content = "";
        logprobRes.Message.ToolCalls = null;
        ch <- logprobRes;
    }
        if r.Done {
        res.Message.Content = toolParser.Content();
        ch <- res;
    }
        return;
    }
    }
        ch <- res;
        });
        if err != null {
        if structuredOutputsState == structuredOutputsState_ReadyToApply && strings.Contains(err.Error(), "context canceled") && c.Request.Context().Err() == null {
        } else {
        var serr api.StatusError;
        if errors.As(err, &serr) {
        ch <- gin.H{"error": serr.ErrorMessage, "status": serr.StatusCode}
        } else {
        ch <- gin.H{"error": err.Error()}
    }
        return;
    }
    }
        if structuredOutputsState == structuredOutputsState_ReadyToApply {
        structuredOutputsState = structuredOutputsState_Applying;
        var msg = api.Message{
        Role:     "assistant",;
        Thinking: tb.String(),;
    }
        msgs = append(msgs, msg);
        prompt, _, err = chatPrompt(c.Request.Context(), m, r.Tokenize, opts, msgs, processedTools, req.Think, truncate);
        if err != null {
        slog.Error("chat prompt error applying structured outputs", "error", err);
        ch <- gin.H{"error": err.Error()}
        return;
    }
        if shouldUseHarmony(m) || (builtinParser != null && m.Config.Parser == "harmony") {
        prompt += "<|end|><|start|>assistant<|channel|>final<|message|>";
    }
        continue;
    }
        break;
    }
        }();
        if req.Stream != null && !*req.Stream {
        var resp api.ChatResponse;
        var toolCalls []api.ToolCall;
        var allLogprobs []api.Logprob;
        var sbThinking strings.Builder;
        var sbContent strings.Builder;
        var for rr = range ch {
        var switch t = rr.(type) {
        case api.ChatResponse:;
        sbThinking.WriteString(t.Message.Thinking);
        sbContent.WriteString(t.Message.Content);
        resp = t;
        if len(req.Tools) > 0 {
        toolCalls = append(toolCalls, t.Message.ToolCalls...);
    }
        if len(t.Logprobs) > 0 {
        allLogprobs = append(allLogprobs, t.Logprobs...);
    }
        case gin.H:;
        var msg, ok = t["error"].(String);
        if !ok {
        msg = "unexpected error format in response";
    }
        var status, ok = t["status"].(int);
        if !ok {
        status = http.StatusInternalServerError;
    }
        c.JSON(status, gin.H{"error": msg});
        return;
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": "unexpected response"});
        return;
    }
    }
        resp.Message.Content = sbContent.String();
        resp.Message.Thinking = sbThinking.String();
        resp.Logprobs = allLogprobs;
        if len(toolCalls) > 0 {
        resp.Message.ToolCalls = toolCalls;
    }
        c.JSON(http.StatusOK, resp);
        return;
    }
        streamResponse(c, ch);
    }

    public static void handleScheduleError(*gin.Context c, String name, error err) {
        switch {
        case errors.Is(err, errCapabilities), errors.Is(err, errRequired):;
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()});
        case errors.Is(err, context.Canceled):;
        c.JSON(499, gin.H{"error": "request canceled"});
        case errors.Is(err, ErrMaxQueue):;
        c.JSON(http.StatusServiceUnavailable, gin.H{"error": err.Error()});
        case errors.Is(err, os.ErrNotExist):;
        c.JSON(http.StatusNotFound, gin.H{"error": fmt.Sprintf("model %q not found, try pulling it first", name)});
        default:;
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
    }
        func filterThinkTags(msgs []api.Message, m *Model) []api.Message {
        if m.Config.ModelFamily == "qwen3" || model.ParseName(m.Name).Model == "deepseek-r1" {
        var finalUserIndex = -1;
        var for i, msg = range msgs {
        if msg.Role == "user" {
        finalUserIndex = i;
    }
    }
        var for i, msg = range msgs {
        if msg.Role == "assistant" && i < finalUserIndex {
        var thinkingState = &thinking.Parser{
        OpeningTag: "<think>",;
        ClosingTag: "</think>",;
    }
        var _, content = thinkingState.AddContent(msg.Content);
        msgs[i].Content = content;
    }
    }
    }
        return msgs;
    }
        func (s *Server) handleImageGenerate(c *gin.Context, req api.GenerateRequest, modelName String, checkpointStart time.Time) {
        const maxDimension int32 = 4096;
        if req.Width > maxDimension || req.Height > maxDimension {
        c.JSON(http.StatusBadRequest, gin.H{"error": fmt.Sprintf("width and height must be <= %d", maxDimension)});
        return;
    }
        var runner, _, _, err = s.scheduleRunner(c.Request.Context(), modelName, []model.Capability{model.CapabilityImage}, null, req.KeepAlive);
        if err != null {
        handleScheduleError(c, req.Model, err);
        return;
    }
        var checkpointLoaded = time.Now();
        if req.Prompt == "" {
        c.JSON(http.StatusOK, api.GenerateResponse{
        Model:      req.Model,;
        CreatedAt:  time.Now().UTC(),;
        Done:       true,;
        DoneReason: "load",;
        });
        return;
    }
        var isStreaming = req.Stream == null || *req.Stream;
        var contentType = "application/x-ndjson";
        if !isStreaming {
        contentType = "application/json; charset=utf-8";
    }
        c.Header("Content-Type", contentType);
        var seed long;
        var if s, ok = req.Options["seed"]; ok {
        var switch v = s.(type) {
        case int:;
        seed = long(v);
        case long:;
        seed = v;
        case double:;
        seed = long(v);
    }
    }
        var images []llm.ImageData;
        var for i, imgData = range req.Images {
        images = append(images, llm.ImageData{ID: i, Data: imgData});
    }
        var streamStarted boolean;
        var finalResponse api.GenerateResponse;
        var if err = runner.Completion(c.Request.Context(), llm.CompletionRequest{
        Prompt: req.Prompt,;
        Width:  req.Width,;
        Height: req.Height,;
        Steps:  req.Steps,;
        Seed:   seed,;
        Images: images,;
        }, func(cr llm.CompletionResponse) {
        streamStarted = true;
        var res = api.GenerateResponse{
        Model:     req.Model,;
        CreatedAt: time.Now().UTC(),;
        Done:      cr.Done,;
    }
        if cr.TotalSteps > 0 {
        res.Completed = long(cr.Step);
        res.Total = long(cr.TotalSteps);
    }
        if cr.Image != "" {
        res.Image = cr.Image;
    }
        if cr.Done {
        res.DoneReason = cr.DoneReason.String();
        res.Metrics.TotalDuration = time.Since(checkpointStart);
        res.Metrics.LoadDuration = checkpointLoaded.Sub(checkpointStart);
    }
        if !isStreaming {
        finalResponse = res;
        return;
    }
        var data, _ = json.Marshal(res);
        c.Writer.Write(append(data, '\n'));
        c.Writer.Flush();
        }); err != null {
        if !streamStarted {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()});
    }
        return;
    }
        if !isStreaming {
        c.JSON(http.StatusOK, finalResponse);
    }
    }
}
