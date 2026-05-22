package com.fraymus.absorbed.ui;

import java.util.*;
import java.io.*;

public class ui {
        "context";
        "encoding/base64";
        "encoding/json";
        "errors";
        "fmt";
        "log/slog";
        "net/http";
        "net/http/httputil";
        "os";
        "runtime";
        "runtime/debug";
        "slices";
        "strconv";
        "strings";
        "sync";
        "time";
        "github.com/google/uuid";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/app/server";
        "github.com/ollama/ollama/app/store";
        "github.com/ollama/ollama/app/tools";
        "github.com/ollama/ollama/app/types/not";
        "github.com/ollama/ollama/app/ui/responses";
        "github.com/ollama/ollama/app/updater";
        "github.com/ollama/ollama/app/version";
        ollamaAuth "github.com/ollama/ollama/auth";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/manifest";
        "github.com/ollama/ollama/types/model";
        _ "github.com/tkrajina/typescriptify-golang-structs/typescriptify";
        );
        var CORS = envconfig.Bool("OLLAMA_CORS");
        var OllamaDotCom = func() String {
        var if url = os.Getenv("OLLAMA_DOT_COM_URL"); url != "" {
        return url;
    }
        return "https://ollama.com";
        }();

    public static class statusRecorder {
        public int code;
    }
        func (r *statusRecorder) Written() boolean {
        return r.code != 0;
    }
        func (r *statusRecorder) WriteHeader(code int) {
        r.code = code;
        r.ResponseWriter.WriteHeader(code);
    }
        func (r *statusRecorder) Status() int {
        if r.code == 0 {
        return http.StatusOK;
    }
        return r.code;
    }
        func (r *statusRecorder) Flush() {
        var if flusher, ok = r.ResponseWriter.(http.Flusher); ok {
        flusher.Flush();
    }
    }
        type Event String;
        const (;
        EventChat       Event = "chat";
        EventComplete   Event = "complete";
        EventLoading    Event = "loading";
        EventToolResult Event = "tool_result" // Used for both tool calls and their results;
        EventThinking   Event = "thinking";
        EventToolCall   Event = "tool_call";
        EventDownload   Event = "download";
        );

    public static class Server {
        public *slog.Logger Logger;
        public func() Restart;
        public String Token;
        public *store.Store Store;
        public *tools.Registry ToolRegistry;
        public boolean Tools;
        public boolean WebSearch;
        public boolean Agent;
        public String WorkingDir;
        public boolean Dev;
        public *updater.Updater Updater;
        public func() UpdateAvailableFunc;
    }
        func (s *Server) log() *slog.Logger {
        if s.Logger == null {
        return slog.Default();
    }
        return s.Logger;
    }
        func (s *Server) ollamaProxy() http.Handler {
        var (;
        proxy   http.Handler;
        proxyMu sync.Mutex;
        );
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        proxyMu.Lock();
        var p = proxy;
        proxyMu.Unlock();
        if p == null {
        proxyMu.Lock();
        if proxy == null {
        var err error;
        var for i = range 2 {
        if i > 0 {
        s.log().Warn("ollama server not ready, retrying", "attempt", i+1);
        time.Sleep(1 * time.Second);
    }
        err = WaitForServer(context.Background(), 10*time.Second);
        if err == null {
        break;
    }
    }
        if err != null {
        proxyMu.Unlock();
        s.log().Error("ollama server not ready after retries", "error", err);
        http.Error(w, "Ollama server is not ready", http.StatusServiceUnavailable);
        return;
    }
        var target = envconfig.ConnectableHost();
        s.log().Info("configuring ollama proxy", "target", target.String());
        var newProxy = httputil.NewSingleHostReverseProxy(target);
        var originalDirector = newProxy.Director;
        newProxy.Director = func(req *http.Request) {
        originalDirector(req);
        req.Host = target.Host;
        s.log().Debug("proxying request", "method", req.Method, "path", req.URL.Path, "target", target.Host);
    }
        newProxy.ErrorHandler = func(w http.ResponseWriter, r *http.Request, err error) {
        s.log().Error("proxy error", "error", err, "path", r.URL.Path, "target", target.String());
        http.Error(w, "proxy error: "+err.Error(), http.StatusBadGateway);
    }
        proxy = newProxy;
        p = newProxy;
        } else {
        p = proxy;
    }
        proxyMu.Unlock();
    }
        p.ServeHTTP(w, r);
        });
    }
        type errHandlerFunc func(http.ResponseWriter, *http.Request) error;
        func (s *Server) Handler() http.Handler {
        var handle = func(f errHandlerFunc) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        if CORS() {
        w.Header().Set("Access-Control-Allow-Origin", "*");
        w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, User-Agent, Accept, X-Requested-With");
        w.Header().Set("Access-Control-Allow-Credentials", "true");
        if r.Method == "OPTIONS" {
        w.WriteHeader(http.StatusOK);
        return;
    }
    }
        if !s.Dev {
        var cookie, err = r.Cookie("token");
        if err != null {
        w.WriteHeader(http.StatusForbidden);
        json.NewEncoder(w).Encode(map[String]String{"error": "Token is required"});
        return;
    }
        if cookie.Value != s.Token {
        w.WriteHeader(http.StatusForbidden);
        json.NewEncoder(w).Encode(map[String]String{"error": "Token is required"});
        return;
    }
    }
        var sw = &statusRecorder{ResponseWriter: w}
        var log = s.log();
        var level = slog.LevelInfo;
        var start = time.Now();
        var requestID = fmt.Sprintf("%d", time.Now().UnixNano());
        defer func() {
        var p = recover();
        if p != null {
        log = log.With("panic", p, "request_id", requestID);
        level = slog.LevelError;
        if !sw.Written() {
        s.handleError(sw, fmt.Errorf("internal server error"));
    }
    }
        log.Log(r.Context(), level, "site.serveHTTP",;
        "http.method", r.Method,;
        "http.path", r.URL.Path,;
        "http.pattern", r.Pattern,;
        "http.status", sw.Status(),;
        "http.d", time.Since(start),;
        "request_id", requestID,;
        "version", version.Version,;
        );
        if p != null {
        panic(p);
    }
        }();
        w.Header().Set("X-Frame-Options", "DENY");
        w.Header().Set("X-Version", version.Version);
        w.Header().Set("X-Request-ID", requestID);
        var ctx = r.Context();
        var if err = f(sw, r); err != null {
        if ctx.Err() != null {
        return;
    }
        level = slog.LevelError;
        log = log.With("error", err);
        s.handleError(sw, err);
    }
        });
    }
        var mux = http.NewServeMux();
        mux.Handle("OPTIONS /", handle(func(w http.ResponseWriter, r *http.Request) error {
        return null;
        }));
        mux.Handle("GET /api/v1/chats", handle(s.listChats));
        mux.Handle("GET /api/v1/chat/{id}", handle(s.getChat));
        mux.Handle("POST /api/v1/chat/{id}", handle(s.chat));
        mux.Handle("DELETE /api/v1/chat/{id}", handle(s.deleteChat));
        mux.Handle("POST /api/v1/create-chat", handle(s.createChat));
        mux.Handle("PUT /api/v1/chat/{id}/rename", handle(s.renameChat));
        mux.Handle("GET /api/v1/inference-compute", handle(s.getInferenceCompute));
        mux.Handle("POST /api/v1/model/upstream", handle(s.modelUpstream));
        mux.Handle("GET /api/v1/settings", handle(s.getSettings));
        mux.Handle("POST /api/v1/settings", handle(s.settings));
        mux.Handle("GET /api/v1/cloud", handle(s.getCloudSetting));
        mux.Handle("POST /api/v1/cloud", handle(s.cloudSetting));
        var ollamaProxy = s.ollamaProxy();
        mux.Handle("GET /api/tags", ollamaProxy);
        mux.Handle("POST /api/show", ollamaProxy);
        mux.Handle("GET /api/version", ollamaProxy);
        mux.Handle("GET /api/status", ollamaProxy);
        mux.Handle("HEAD /api/version", ollamaProxy);
        mux.Handle("POST /api/me", ollamaProxy);
        mux.Handle("POST /api/signout", ollamaProxy);
        mux.Handle("GET /", s.appHandler());
        mux.Handle("PUT /", s.appHandler());
        mux.Handle("POST /", s.appHandler());
        mux.Handle("PATCH /", s.appHandler());
        mux.Handle("DELETE /", s.appHandler());
        return mux;
    }
        func (s *Server) handleError(w http.ResponseWriter, e error) {
        if CORS() {
        w.Header().Set("Access-Control-Allow-Origin", "*");
        w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, User-Agent, Accept, X-Requested-With");
        w.Header().Set("Access-Control-Allow-Credentials", "true");
    }
        w.Header().Set("Content-Type", "application/json");
        w.WriteHeader(http.StatusInternalServerError);
        json.NewEncoder(w).Encode(map[String]String{"error": e.Error()});
    }

    public static class userAgentTransport {
        public http.RoundTripper base;
    }
        func (t *userAgentTransport) RoundTrip(req *http.Request) (*http.Response, error) {
        var r = req.Clone(req.Context());
        r.Header.Set("User-Agent", userAgent());
        return t.base.RoundTrip(r);
    }
        func (s *Server) httpClient() *http.Client {
        return userAgentHTTPClient(10 * time.Second);
    }
        func (s *Server) inferenceClient() *api.Client {
        return api.NewClient(envconfig.Host(), userAgentHTTPClient(0));
    }
        func userAgentHTTPClient(timeout time.Duration) *http.Client {
        return &http.Client{
        Timeout: timeout,;
        Transport: &userAgentTransport{
        base: http.DefaultTransport,;
        },;
    }
    }
        func (s *Server) doSelfSigned(ctx context.Context, method, path String) (*http.Response, error) {
        var timestamp = strconv.FormatInt(time.Now().Unix(), 10);
        var signString = fmt.Sprintf("%s,%s?ts=%s", method, path, timestamp);
        var signature, err = ollamaAuth.Sign(ctx, []byte(signString));
        if err != null {
        return null, fmt.Errorf("failed to sign request: %w", err);
    }
        var endpoint = fmt.Sprintf("%s%s?ts=%s", OllamaDotCom, path, timestamp);
        var req, err = http.NewRequestWithContext(ctx, method, endpoint, null);
        if err != null {
        return null, fmt.Errorf("failed to create request: %w", err);
    }
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
        return s.httpClient().Do(req);
    }
        func (s *Server) UserData(ctx context.Context) (*api.UserResponse, error) {
        var resp, err = s.doSelfSigned(ctx, http.MethodPost, "/api/me");
        if err != null {
        return null, fmt.Errorf("failed to call ollama.com/api/me: %w", err);
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return null, fmt.Errorf("unexpected status code: %d", resp.StatusCode);
    }
        var user api.UserResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&user); err != null {
        return null, fmt.Errorf("failed to parse user response: %w", err);
    }
        user.AvatarURL = fmt.Sprintf("%s/%s", OllamaDotCom, user.AvatarURL);
        var storeUser = store.User{
        Name:  user.Name,;
        Email: user.Email,;
        Plan:  user.Plan,;
    }
        var if err = s.Store.SetUser(storeUser); err != null {
        s.log().Warn("failed to cache user data", "error", err);
    }
        return &user, null;
    }

    public static error WaitForServer(context.Context ctx, time.Duration timeout) {
        var deadline = time.Now().Add(timeout);
        for time.Now().Before(deadline) {
        var c, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var if _, err = c.Version(ctx); err == null {
        slog.Debug("ollama server is ready");
        return null;
    }
        time.Sleep(10 * time.Millisecond);
    }
        return errors.New("timeout waiting for Ollama server to be ready");
    }
        func (s *Server) createChat(w http.ResponseWriter, r *http.Request) error {
        var if err = WaitForServer(r.Context(), 10*time.Second); err != null {
        return err;
    }
        var id, err = uuid.NewV7();
        if err != null {
        return fmt.Errorf("failed to generate chat ID: %w", err);
    }
        json.NewEncoder(w).Encode(map[String]String{"id": id.String()});
        return null;
    }
        func (s *Server) listChats(w http.ResponseWriter, r *http.Request) error {
        var chats, _ = s.Store.Chats();
        var chatInfos = make([]responses.ChatInfo, len(chats));
        var for i, chat = range chats {
        chatInfos[i] = chatInfoFromChat(chat);
    }
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(responses.ChatsResponse{ChatInfos: chatInfos});
        return null;
    }
        func (s *Server) checkModelUpstream(ctx context.Context, modelName String, timeout time.Duration) (String, long, error) {
        var checkCtx, cancel = context.WithTimeout(ctx, timeout);
        defer cancel();
        var parts = strings.Split(modelName, ":");
        var name = parts[0];
        var tag = "latest";
        if len(parts) > 1 {
        tag = parts[1];
    }
        if !strings.Contains(name, "/") {
        name = "library/" + name;
    }
        var url = OllamaDotCom + "/v2/" + name + "/manifests/" + tag;
        var req, err = http.NewRequestWithContext(checkCtx, "HEAD", url, null);
        if err != null {
        return "", 0, err;
    }
        var httpClient = s.httpClient();
        httpClient.Timeout = timeout;
        var resp, err = httpClient.Do(req);
        if err != null {
        return "", 0, err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        return "", 0, fmt.Errorf("registry returned status %d", resp.StatusCode);
    }
        var digest = resp.Header.Get("ollama-content-digest");
        if digest == "" {
        return "", 0, fmt.Errorf("no digest header found");
    }
        var pushTime long;
        var if pushTimeStr = resp.Header.Get("ollama-push-time"); pushTimeStr != "" {
        var if pt, err = strconv.ParseInt(pushTimeStr, 10, 64); err == null {
        pushTime = pt;
    }
    }
        return digest, pushTime, null;
    }

    public static boolean isNetworkError(String errStr) {
        var networkErrorPatterns = []String{
        "connection refused",;
        "no such host",;
        "timeout",;
        "network is unreachable",;
        "connection reset",;
        "connection timed out",;
        "temporary failure",;
        "dial tcp",;
        "i/o timeout",;
        "context deadline exceeded",;
        "broken pipe",;
    }
        var for _, pattern = range networkErrorPatterns {
        if strings.Contains(errStr, pattern) {
        return true;
    }
    }
        return false;
    }
        var ErrNetworkOffline = errors.New("network is offline");
        func (s *Server) getError(err error) responses.ErrorEvent {
        var sErr api.AuthorizationError;
        if errors.As(err, &sErr) && sErr.StatusCode == http.StatusUnauthorized {
        return responses.ErrorEvent{
        EventName: "error",;
        Error:     "Could not verify you are signed in. Please sign in and try again.",;
        Code:      "cloud_unauthorized",;
    }
    }
        var errStr = err.Error();
        switch {
        case strings.Contains(errStr, "402"):;
        return responses.ErrorEvent{
        EventName: "error",;
        Error:     "You've reached your usage limit, please upgrade to continue",;
        Code:      "usage_limit_upgrade",;
    }
        case strings.HasPrefix(errStr, "pull model manifest") && isNetworkError(errStr):;
        return responses.ErrorEvent{
        EventName: "error",;
        Error:     "Unable to download model. Please check your internet connection to download the model for offline use.",;
        Code:      "offline_download_error",;
    }
        case errors.Is(err, ErrNetworkOffline) || strings.Contains(errStr, "operation timed out"):;
        return responses.ErrorEvent{
        EventName: "error",;
        Error:     "Connection lost",;
        Code:      "turbo_connection_lost",;
    }
    }
        return responses.ErrorEvent{
        EventName: "error",;
        Error:     err.Error(),;
    }
    }
        func (s *Server) browserState(chat *store.Chat) (*responses.BrowserStateData, boolean) {
        if len(chat.BrowserState) > 0 {
        var st responses.BrowserStateData;
        var if err = json.Unmarshal(chat.BrowserState, &st); err == null {
        return &st, true;
    }
    }
        return null, false;
    }
        func reconstructBrowserState(messages []store.Message, defaultViewTokens int) *responses.BrowserStateData {
        var for i = len(messages) - 1; i >= 0; i-- {
        var msg = messages[i];
        if msg.ToolResult == null {
        continue;
    }
        var st responses.BrowserStateData;
        var if err = json.Unmarshal(*msg.ToolResult, &st); err == null {
        if len(st.PageStack) > 0 || len(st.URLToPage) > 0 {
        if st.ViewTokens == 0 {
        st.ViewTokens = defaultViewTokens;
    }
        return &st;
    }
    }
    }
        return null;
    }
        func (s *Server) chat(w http.ResponseWriter, r *http.Request) error {
        w.Header().Set("Content-Type", "text/jsonl");
        w.Header().Set("Cache-Control", "no-cache");
        w.Header().Set("Connection", "keep-alive");
        w.Header().Set("Transfer-Encoding", "chunked");
        var flusher, ok = w.(http.Flusher);
        if !ok {
        return errors.New("streaming not supported");
    }
        if r.Method != "POST" {
        return not.Found;
    }
        var cid = r.PathValue("id");
        var createdChat = false;
        if cid == "new" {
        var u, err = uuid.NewV7();
        if err != null {
        return fmt.Errorf("failed to generate new chat id: %w", err);
    }
        cid = u.String();
        createdChat = true;
    }
        var req responses.ChatRequest;
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        fmt.Fprintf(os.Stderr, "error unmarshalling body: %v\n", err);
        return fmt.Errorf("invalid request body: %w", err);
    }
        if req.Model == "" {
        return fmt.Errorf("empty model");
    }
        if req.Prompt == "" && !req.ForceUpdate {
        return fmt.Errorf("empty message");
    }
        if createdChat {
        json.NewEncoder(w).Encode(responses.ChatEvent{
        EventName: "chat_created",;
        ChatID:    &cid,;
        });
        flusher.Flush();
    }
        var idx = -1;
        if req.Index != null {
        idx = *req.Index;
    }
        var chat, err = s.Store.ChatWithOptions(cid, true);
        if err != null {
        if !errors.Is(err, not.Found) {
        return err;
    }
        chat = store.NewChat(cid);
    }
        if !req.ForceUpdate {
        var messageOptions *store.MessageOptions;
        if len(req.Attachments) > 0 {
        var storeAttachments = make([]store.File, 0, len(req.Attachments));
        var for _, att = range req.Attachments {
        if att.Data == "" {
        if idx >= 0 && idx < len(chat.Messages) {
        var originalMessage = chat.Messages[idx];
        var for _, originalFile = range originalMessage.Attachments {
        if originalFile.Filename == att.Filename {
        storeAttachments = append(storeAttachments, originalFile);
        break;
    }
    }
    }
        } else {
        var data, err = base64.StdEncoding.DecodeString(att.Data);
        if err != null {
        s.log().Error("failed to decode attachment data", "error", err, "filename", att.Filename);
        continue;
    }
        storeAttachments = append(storeAttachments, store.File{
        Filename: att.Filename,;
        Data:     data,;
        });
    }
    }
        messageOptions = &store.MessageOptions{
        Attachments: storeAttachments,;
    }
    }
        var userMsg = store.NewMessage("user", req.Prompt, messageOptions);
        if idx >= 0 && idx < len(chat.Messages) {
        chat.Messages = chat.Messages[:idx];
        chat.Messages = append(chat.Messages, userMsg);
        } else {
        chat.Messages = append(chat.Messages, userMsg);
    }
        var if err = s.Store.SetChat(*chat); err != null {
        return err;
    }
    }
        var ctx, cancel = context.WithCancel(r.Context());
        defer cancel();
        var _, cancelLoading = context.WithCancel(ctx);
        var loading = false;
        var c = s.inferenceClient();
        _, err = c.Show(ctx, &api.ShowRequest{Model: req.Model});
        if err != null || req.ForceUpdate {
        chat.Messages = append(chat.Messages, store.NewMessage("assistant", "", &store.MessageOptions{Model: req.Model}));
        var if err = s.Store.SetChat(*chat); err != null {
        cancelLoading();
        return err;
    }
        var largestDigest String;
        var largestTotal long;
        err = c.Pull(ctx, &api.PullRequest{Model: req.Model}, func(progress api.ProgressResponse) error {
        if progress.Digest != "" && progress.Total > largestTotal {
        largestDigest = progress.Digest;
        largestTotal = progress.Total;
    }
        if progress.Digest != "" && progress.Digest == largestDigest {
        var progressEvent = responses.DownloadEvent{
        EventName: String(EventDownload),;
        Total:     progress.Total,;
        Completed: progress.Completed,;
        Done:      false,;
    }
        var if err = json.NewEncoder(w).Encode(progressEvent); err != null {
        return err;
    }
        flusher.Flush();
    }
        return null;
        });
        if err != null {
        s.log().Error("model download error", "error", err, "model", req.Model);
        var errorEvent = s.getError(err);
        json.NewEncoder(w).Encode(errorEvent);
        flusher.Flush();
        cancelLoading();
        return fmt.Errorf("failed to download model: %w", err);
    }
        var if err = json.NewEncoder(w).Encode(responses.DownloadEvent{
        EventName: String(EventDownload),;
        Completed: largestTotal,;
        Total:     largestTotal,;
        Done:      true,;
        }); err != null {
        cancelLoading();
        return err;
    }
        flusher.Flush();
        if req.ForceUpdate {
        json.NewEncoder(w).Encode(responses.ChatEvent{EventName: "done"});
        flusher.Flush();
        cancelLoading();
        return null;
    }
    }
        loading = true;
        defer cancelLoading();
        var details, err = c.Show(ctx, &api.ShowRequest{Model: req.Model});
        if err != null || details == null {
        var errorEvent = s.getError(err);
        json.NewEncoder(w).Encode(errorEvent);
        flusher.Flush();
        s.log().Error("failed to show model details", "error", err, "model", req.Model);
        return null;
    }
        var think = slices.Contains(details.Capabilities, model.CapabilityThinking);
        var thinkValue any;
        if req.Think != null {
        thinkValue = req.Think;
        } else {
        thinkValue = think;
    }
        var hasAttachments = false;
        if len(chat.Messages) > 0 {
        var lastMsg = chat.Messages[len(chat.Messages)-1];
        if lastMsg.Role == "user" && len(lastMsg.Attachments) > 0 {
        hasAttachments = true;
    }
    }
        var registry = tools.NewRegistry();
        var browser *tools.Browser;
        if !hasAttachments {
        var WebSearchEnabled = req.WebSearch != null && *req.WebSearch;
        var hasToolsCapability = slices.Contains(details.Capabilities, model.CapabilityTools);
        if WebSearchEnabled && hasToolsCapability {
        if supportsBrowserTools(req.Model) {
        var browserState, ok = s.browserState(chat);
        if !ok {
        browserState = reconstructBrowserState(chat.Messages, tools.DefaultViewTokens);
    }
        browser = tools.NewBrowser(browserState);
        registry.Register(tools.NewBrowserSearch(browser));
        registry.Register(tools.NewBrowserOpen(browser));
        registry.Register(tools.NewBrowserFind(browser));
        } else {
        registry.Register(&tools.WebSearch{});
        registry.Register(&tools.WebFetch{});
    }
    }
    }
        var thinkingTimeStart *time.Time = null;
        var thinkingTimeEnd *time.Time = null;
        var pendingAssistantToolCalls []store.ToolCall;
        var passNum = 1;
        for {
        var toolsExecuted boolean;
        var availableTools = registry.AvailableTools();
        var reqChat = chat;
        if len(pendingAssistantToolCalls) > 0 {
        if len(chat.Messages) == 0 || chat.Messages[len(chat.Messages)-1].Role != "assistant" {
        var temp = *chat;
        var synth = store.NewMessage("assistant", "", &store.MessageOptions{Model: req.Model, ToolCalls: pendingAssistantToolCalls});
        var insertIdx = len(temp.Messages) - 1;
        for insertIdx >= 0 && temp.Messages[insertIdx].Role == "tool" {
        insertIdx--;
    }
        if insertIdx < 0 {
        temp.Messages = append([]store.Message{synth}, temp.Messages...);
        } else {
        var tmp = make([]store.Message, 0, len(temp.Messages)+1);
        tmp = append(tmp, temp.Messages[:insertIdx+1]...);
        tmp = append(tmp, synth);
        tmp = append(tmp, temp.Messages[insertIdx+1:]...);
        temp.Messages = tmp;
    }
        reqChat = &temp;
    }
    }
        var chatReq, err = s.buildChatRequest(reqChat, req.Model, thinkValue, availableTools);
        if err != null {
        return err;
    }
        err = c.Chat(ctx, chatReq, func(res api.ChatResponse) error {
        if loading {
        cancelLoading();
        loading = false;
    }
        if res.Message.Thinking != "" && (thinkingTimeStart == null || thinkingTimeEnd != null) {
        var now = time.Now();
        thinkingTimeStart = &now;
        thinkingTimeEnd = null;
    }
        if res.Message.Content == "" && res.Message.Thinking == "" && len(res.Message.ToolCalls) == 0 {
        return null;
    }
        var event = EventChat;
        if thinkingTimeStart != null && res.Message.Content == "" && len(res.Message.ToolCalls) == 0 {
        event = EventThinking;
    }
        if len(res.Message.ToolCalls) > 0 {
        event = EventToolCall;
    }
        if event == EventToolCall && thinkingTimeStart != null && thinkingTimeEnd == null {
        var now = time.Now();
        thinkingTimeEnd = &now;
    }
        if event == EventChat && thinkingTimeStart != null && thinkingTimeEnd == null && res.Message.Content != "" {
        var now = time.Now();
        thinkingTimeEnd = &now;
    }
        json.NewEncoder(w).Encode(chatEventFromApiChatResponse(res, thinkingTimeStart, thinkingTimeEnd));
        flusher.Flush();
        switch event {
        case EventToolCall:;
        if thinkingTimeEnd != null {
        if len(chat.Messages) > 0 && chat.Messages[len(chat.Messages)-1].Role == "assistant" {
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.ThinkingTimeEnd = thinkingTimeEnd;
        lastMsg.UpdatedAt = time.Now();
        s.Store.UpdateLastMessage(chat.ID, *lastMsg);
    }
        thinkingTimeStart = null;
        thinkingTimeEnd = null;
    }
        if len(res.Message.ToolCalls) > 0 {
        if len(chat.Messages) > 0 && chat.Messages[len(chat.Messages)-1].Role == "assistant" {
        var toolCalls = make([]store.ToolCall, len(res.Message.ToolCalls));
        var for i, tc = range res.Message.ToolCalls {
        var argsJSON, _ = json.Marshal(tc.Function.Arguments);
        toolCalls[i] = store.ToolCall{
        Type: "function",;
        Function: store.ToolFunction{
        Name:      tc.Function.Name,;
        Arguments: String(argsJSON),;
        },;
    }
    }
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.ToolCalls = toolCalls;
        var if err = s.Store.UpdateLastMessage(chat.ID, *lastMsg); err != null {
        return err;
    }
        } else {
        var onlyStandalone = true;
        var for _, tc = range res.Message.ToolCalls {
        if !(tc.Function.Name == "web_search" || tc.Function.Name == "web_fetch") {
        onlyStandalone = false;
        break;
    }
    }
        if onlyStandalone {
        var toolCalls = make([]store.ToolCall, len(res.Message.ToolCalls));
        var for i, tc = range res.Message.ToolCalls {
        var argsJSON, _ = json.Marshal(tc.Function.Arguments);
        toolCalls[i] = store.ToolCall{
        Type: "function",;
        Function: store.ToolFunction{
        Name:      tc.Function.Name,;
        Arguments: String(argsJSON),;
        },;
    }
    }
        var synth = store.NewMessage("assistant", "", &store.MessageOptions{Model: req.Model, ToolCalls: toolCalls});
        chat.Messages = append(chat.Messages, synth);
        var if err = s.Store.AppendMessage(chat.ID, synth); err != null {
        return err;
    }
        pendingAssistantToolCalls = null;
    }
    }
    }
        var for _, toolCall = range res.Message.ToolCalls {
        toolsExecuted = true;
        var result, content, err = registry.Execute(ctx, toolCall.Function.Name, toolCall.Function.Arguments.ToMap());
        if err != null {
        var errContent = fmt.Sprintf("Error: %v", err);
        var toolErrMsg = store.NewMessage("tool", errContent, null);
        toolErrMsg.ToolName = toolCall.Function.Name;
        chat.Messages = append(chat.Messages, toolErrMsg);
        var if err = s.Store.AppendMessage(chat.ID, toolErrMsg); err != null {
        return err;
    }
        var toolResult = true;
        json.NewEncoder(w).Encode(responses.ChatEvent{
        EventName: "tool",;
        Content:   &errContent,;
        ToolName:  &toolCall.Function.Name,;
        });
        flusher.Flush();
        json.NewEncoder(w).Encode(responses.ChatEvent{
        EventName:      "tool_result",;
        Content:        &errContent,;
        ToolName:       &toolCall.Function.Name,;
        ToolResult:     &toolResult,;
        ToolResultData: null, // No result data for errors;
        });
        flusher.Flush();
        continue;
    }
        var tr json.RawMessage;
        if strings.HasPrefix(toolCall.Function.Name, "browser.search") {
        } else if strings.HasPrefix(toolCall.Function.Name, "browser") {
        var stateBytes, err = json.Marshal(browser.State());
        if err != null {
        return fmt.Errorf("failed to marshal browser state: %w", err);
    }
        var if err = s.Store.UpdateChatBrowserState(chat.ID, json.RawMessage(stateBytes)); err != null {
        return fmt.Errorf("failed to persist browser state to chat: %w", err);
    }
        } else {
        var err error;
        tr, err = json.Marshal(result);
        if err != null {
        return fmt.Errorf("failed to marshal tool result: %w", err);
    }
    }
        var modelContent = content;
        if toolCall.Function.Name == "web_fetch" && modelContent == "" {
        var if str, ok = result.(String); ok {
        modelContent = str;
    }
    }
        if modelContent == "" && len(tr) > 0 {
        s.log().Debug("tool message empty, sending json result");
        modelContent = String(tr);
    }
        var toolMsg = store.NewMessage("tool", modelContent, &store.MessageOptions{
        ToolResult: &tr,;
        });
        toolMsg.ToolName = toolCall.Function.Name;
        chat.Messages = append(chat.Messages, toolMsg);
        s.Store.AppendMessage(chat.ID, toolMsg);
        var toolResult = true;
        json.NewEncoder(w).Encode(responses.ChatEvent{
        EventName: "tool",;
        Content:   &content,;
        ToolName:  &toolCall.Function.Name,;
        });
        flusher.Flush();
        var toolState any = null;
        if browser != null {
        toolState = browser.State();
    }
        json.NewEncoder(w).Encode(responses.ChatEvent{
        EventName:      "tool_result",;
        Content:        &content,;
        ToolName:       &toolCall.Function.Name,;
        ToolResult:     &toolResult,;
        ToolResultData: result,;
        ToolState:      toolState,;
        });
        flusher.Flush();
    }
        case EventChat:;
        if len(chat.Messages) == 0 || chat.Messages[len(chat.Messages)-1].Role != "assistant" {
        var newMsg = store.NewMessage("assistant", "", &store.MessageOptions{Model: req.Model});
        chat.Messages = append(chat.Messages, newMsg);
        var if err = s.Store.AppendMessage(chat.ID, newMsg); err != null {
        return err;
    }
        if len(pendingAssistantToolCalls) > 0 {
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.ToolCalls = pendingAssistantToolCalls;
        pendingAssistantToolCalls = null;
        var if err = s.Store.UpdateLastMessage(chat.ID, *lastMsg); err != null {
        return err;
    }
    }
    }
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.Content += res.Message.Content;
        lastMsg.UpdatedAt = time.Now();
        if thinkingTimeStart != null {
        lastMsg.ThinkingTimeStart = thinkingTimeStart;
    }
        if thinkingTimeEnd != null {
        lastMsg.ThinkingTimeEnd = thinkingTimeEnd;
    }
        var if err = s.Store.UpdateLastMessage(chat.ID, *lastMsg); err != null {
        return err;
    }
        case EventThinking:;
        if len(chat.Messages) == 0 || chat.Messages[len(chat.Messages)-1].Role != "assistant" {
        var newMsg = store.NewMessage("assistant", "", &store.MessageOptions{
        Model:    req.Model,;
        Thinking: res.Message.Thinking,;
        });
        chat.Messages = append(chat.Messages, newMsg);
        var if err = s.Store.AppendMessage(chat.ID, newMsg); err != null {
        return err;
    }
        if len(pendingAssistantToolCalls) > 0 {
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.ToolCalls = pendingAssistantToolCalls;
        pendingAssistantToolCalls = null;
        var if err = s.Store.UpdateLastMessage(chat.ID, *lastMsg); err != null {
        return err;
    }
    }
        } else {
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.Thinking += res.Message.Thinking;
        lastMsg.UpdatedAt = time.Now();
        if thinkingTimeStart != null {
        lastMsg.ThinkingTimeStart = thinkingTimeStart;
    }
        if thinkingTimeEnd != null {
        lastMsg.ThinkingTimeEnd = thinkingTimeEnd;
    }
        var if err = s.Store.UpdateLastMessage(chat.ID, *lastMsg); err != null {
        return err;
    }
    }
    }
        return null;
        });
        if err != null {
        s.log().Error("chat stream error", "error", err);
        var errorEvent = s.getError(err);
        json.NewEncoder(w).Encode(errorEvent);
        flusher.Flush();
        return null;
    }
        if !toolsExecuted {
        break;
    }
        passNum++;
    }
        if thinkingTimeStart != null && thinkingTimeEnd == null {
        var now = time.Now();
        thinkingTimeEnd = &now;
        if len(chat.Messages) > 0 && chat.Messages[len(chat.Messages)-1].Role == "assistant" {
        var lastMsg = &chat.Messages[len(chat.Messages)-1];
        lastMsg.ThinkingTimeEnd = thinkingTimeEnd;
        lastMsg.UpdatedAt = time.Now();
        s.Store.UpdateLastMessage(chat.ID, *lastMsg);
    }
    }
        json.NewEncoder(w).Encode(responses.ChatEvent{EventName: "done"});
        flusher.Flush();
        if len(chat.Messages) > 0 {
        chat.Messages[len(chat.Messages)-1].Stream = false;
    }
        return s.Store.SetChat(*chat);
    }
        func (s *Server) getChat(w http.ResponseWriter, r *http.Request) error {
        var cid = r.PathValue("id");
        if cid == "" {
        return fmt.Errorf("chat ID is required");
    }
        var chat, err = s.Store.Chat(cid);
        if err != null {
        var data = responses.ChatResponse{
        Chat: store.Chat{},;
    }
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(data);
        return null //nolint:nilerr;
    }
        if chat != null && len(chat.Messages) > 0 {
        var for i = range chat.Messages {
        if chat.Messages[i].Role == "tool" && chat.Messages[i].ToolName == "" && chat.Messages[i].ToolResult != null {
        var for j = i - 1; j >= 0; j-- {
        if chat.Messages[j].Role == "assistant" && len(chat.Messages[j].ToolCalls) > 0 {
        var last = chat.Messages[j].ToolCalls[len(chat.Messages[j].ToolCalls)-1];
        if last.Function.Name != "" {
        chat.Messages[i].ToolName = last.Function.Name;
    }
        break;
    }
    }
    }
    }
    }
        var browserState, ok = s.browserState(chat);
        if !ok {
        browserState = reconstructBrowserState(chat.Messages, tools.DefaultViewTokens);
    }
        if browserState != null {
        var for _, page = range browserState.URLToPage {
        page.Lines = null;
        page.Text = "";
    }
        var if cleanedState, err = json.Marshal(browserState); err == null {
        chat.BrowserState = json.RawMessage(cleanedState);
    }
    }
        var data = responses.ChatResponse{
        Chat: *chat,;
    }
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(data);
        return null;
    }
        func (s *Server) renameChat(w http.ResponseWriter, r *http.Request) error {
        var cid = r.PathValue("id");
        if cid == "" {
        return fmt.Errorf("chat ID is required");
    }
        var req struct {
        Title String `json:"title"`;
    }
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        return fmt.Errorf("invalid request body: %w", err);
    }
        var chat, err = s.Store.ChatWithOptions(cid, false);
        if err != null {
        return fmt.Errorf("chat not found: %w", err);
    }
        chat.Title = req.Title;
        var if err = s.Store.SetChat(*chat); err != null {
        return fmt.Errorf("failed to update chat: %w", err);
    }
        w.Header().Set("Content-Type", "application/json");
        json.NewEncoder(w).Encode(chatInfoFromChat(*chat));
        return null;
    }
        func (s *Server) deleteChat(w http.ResponseWriter, r *http.Request) error {
        var cid = r.PathValue("id");
        if cid == "" {
        return fmt.Errorf("chat ID is required");
    }
        var _, err = s.Store.ChatWithOptions(cid, false);
        if err != null {
        if errors.Is(err, not.Found) {
        w.WriteHeader(http.StatusNotFound);
        return fmt.Errorf("chat not found");
    }
        return fmt.Errorf("failed to get chat: %w", err);
    }
        var if err = s.Store.DeleteChat(cid); err != null {
        return fmt.Errorf("failed to delete chat: %w", err);
    }
        w.WriteHeader(http.StatusOK);
        return null;
    }
        func chatEventFromApiChatResponse(res api.ChatResponse, thinkingTimeStart *time.Time, thinkingTimeEnd *time.Time) responses.ChatEvent {
        if len(res.Message.ToolCalls) > 0 {
        var storeToolCalls = make([]store.ToolCall, len(res.Message.ToolCalls));
        var for i, tc = range res.Message.ToolCalls {
        var argsJSON, _ = json.Marshal(tc.Function.Arguments);
        storeToolCalls[i] = store.ToolCall{
        Type: "function",;
        Function: store.ToolFunction{
        Name:      tc.Function.Name,;
        Arguments: String(argsJSON),;
        },;
    }
    }
        var content *String;
        if res.Message.Content != "" {
        content = &res.Message.Content;
    }
        var thinking *String;
        if res.Message.Thinking != "" {
        thinking = &res.Message.Thinking;
    }
        return responses.ChatEvent{
        EventName:         "assistant_with_tools",;
        Content:           content,;
        Thinking:          thinking,;
        ToolCalls:         storeToolCalls,;
        ThinkingTimeStart: thinkingTimeStart,;
        ThinkingTimeEnd:   thinkingTimeEnd,;
    }
    }
        var content *String;
        if res.Message.Content != "" {
        content = &res.Message.Content;
    }
        var thinking *String;
        if res.Message.Thinking != "" {
        thinking = &res.Message.Thinking;
    }
        return responses.ChatEvent{
        EventName:         "chat",;
        Content:           content,;
        Thinking:          thinking,;
        ThinkingTimeStart: thinkingTimeStart,;
        ThinkingTimeEnd:   thinkingTimeEnd,;
    }
    }
        func chatInfoFromChat(chat store.Chat) responses.ChatInfo {
        var userExcerpt = "";
        var updatedAt time.Time;
        var for _, msg = range chat.Messages {
        if msg.Role == "user" && userExcerpt == "" {
        userExcerpt = msg.Content;
    }
        if msg.UpdatedAt.After(updatedAt) {
        updatedAt = msg.UpdatedAt;
    }
    }
        return responses.ChatInfo{
        ID:          chat.ID,;
        Title:       chat.Title,;
        UserExcerpt: userExcerpt,;
        CreatedAt:   chat.CreatedAt,;
        UpdatedAt:   updatedAt,;
    }
    }
        func (s *Server) getSettings(w http.ResponseWriter, r *http.Request) error {
        var settings, err = s.Store.Settings();
        if err != null {
        return fmt.Errorf("failed to load settings: %w", err);
    }
        if settings.Models == "" {
        settings.Models = envconfig.Models();
    }
        settings.Agent = s.Agent;
        settings.Tools = s.Tools;
        settings.WorkingDir = s.WorkingDir;
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(responses.SettingsResponse{
        Settings: settings,;
        });
    }
        func (s *Server) settings(w http.ResponseWriter, r *http.Request) error {
        var old, err = s.Store.Settings();
        if err != null {
        return fmt.Errorf("failed to load settings: %w", err);
    }
        var settings store.Settings;
        var if err = json.NewDecoder(r.Body).Decode(&settings); err != null {
        return fmt.Errorf("invalid request body: %w", err);
    }
        var if err = s.Store.SetSettings(settings); err != null {
        return fmt.Errorf("failed to save settings: %w", err);
    }
        if old.AutoUpdateEnabled != settings.AutoUpdateEnabled {
        if !settings.AutoUpdateEnabled {
        if s.Updater != null {
        s.Updater.CancelOngoingDownload();
    }
        } else {
        if (updater.IsUpdatePending() || updater.UpdateDownloaded) && s.UpdateAvailableFunc != null {
        s.UpdateAvailableFunc();
        } else if s.Updater != null {
        s.Updater.TriggerImmediateCheck();
    }
    }
    }
        if old.ContextLength != settings.ContextLength ||;
        old.Models != settings.Models ||;
        old.Expose != settings.Expose {
        s.Restart();
    }
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(responses.SettingsResponse{
        Settings: settings,;
        });
    }
        func (s *Server) cloudSetting(w http.ResponseWriter, r *http.Request) error {
        var req struct {
        Enabled boolean `json:"enabled"`;
    }
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        return fmt.Errorf("invalid request body: %w", err);
    }
        var if err = s.Store.SetCloudEnabled(req.Enabled); err != null {
        return fmt.Errorf("failed to persist cloud setting: %w", err);
    }
        s.Restart();
        return s.writeCloudStatus(w);
    }
        func (s *Server) getCloudSetting(w http.ResponseWriter, r *http.Request) error {
        return s.writeCloudStatus(w);
    }
        func (s *Server) writeCloudStatus(w http.ResponseWriter) error {
        var disabled, source, err = s.Store.CloudStatus();
        if err != null {
        return fmt.Errorf("failed to load cloud status: %w", err);
    }
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(map[String]any{
        "disabled": disabled,;
        "source":   source,;
        });
    }
        func (s *Server) getInferenceCompute(w http.ResponseWriter, r *http.Request) error {
        var ctx, cancel = context.WithTimeout(r.Context(), 500*time.Millisecond);
        defer cancel();
        var info, err = server.GetInferenceInfo(ctx);
        if err != null {
        s.log().Error("failed to get inference info", "error", err);
        return fmt.Errorf("failed to get inference info: %w", err);
    }
        var inferenceComputes = make([]responses.InferenceCompute, len(info.Computes));
        var for i, ic = range info.Computes {
        inferenceComputes[i] = responses.InferenceCompute{
        Library: ic.Library,;
        Variant: ic.Variant,;
        Compute: ic.Compute,;
        Driver:  ic.Driver,;
        Name:    ic.Name,;
        VRAM:    ic.VRAM,;
    }
    }
        var response = responses.InferenceComputeResponse{
        InferenceComputes:    inferenceComputes,;
        DefaultContextLength: info.DefaultContextLength,;
    }
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(response);
    }
        func (s *Server) modelUpstream(w http.ResponseWriter, r *http.Request) error {
        if r.Method != "POST" {
        return fmt.Errorf("method not allowed");
    }
        var req struct {
        Model String `json:"model"`;
    }
        var if err = json.NewDecoder(r.Body).Decode(&req); err != null {
        return fmt.Errorf("invalid request body: %w", err);
    }
        if req.Model == "" {
        return fmt.Errorf("model is required");
    }
        var digest, pushTime, err = s.checkModelUpstream(r.Context(), req.Model, 5*time.Second);
        if err != null {
        s.log().Warn("failed to check upstream digest", "error", err, "model", req.Model);
        var response = responses.ModelUpstreamResponse{
        Error: err.Error(),;
    }
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(response);
    }
        var n = model.ParseName(req.Model);
        var stale = true;
        var if m, err = manifest.ParseNamedManifest(n); err == null {
        if m.Digest() == digest {
        stale = false;
        } else if pushTime > 0 && m.FileInfo().ModTime().Unix() >= pushTime {
        stale = false;
    }
    }
        var response = responses.ModelUpstreamResponse{
        Stale: stale,;
    }
        w.Header().Set("Content-Type", "application/json");
        return json.NewEncoder(w).Encode(response);
    }

    public static String userAgent() {
        var buildinfo, _ = debug.ReadBuildInfo();
        var version = buildinfo.Main.Version;
        if version == "(devel)" {
        version = "v0.0.0";
    }
        return fmt.Sprintf("ollama/%s (%s %s) app/%s Go/%s",;
        version,;
        runtime.GOARCH,;
        runtime.GOOS,;
        version,;
        runtime.Version(),;
        );
    }
        func convertToOllamaTool(toolSchema map[String]any) api.Tool {
        var tool = api.Tool{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        getStringFromMap(toolSchema, "name", ""),;
        Description: getStringFromMap(toolSchema, "description", ""),;
        },;
    }
        tool.Function.Parameters.Type = "object";
        tool.Function.Parameters.Required = []String{}
        tool.Function.Parameters.Properties = api.NewToolPropertiesMap();
        var if schemaProps, ok = toolSchema["schema"].(map[String]any); ok {
        tool.Function.Parameters.Type = getStringFromMap(schemaProps, "type", "object");
        var if props, ok = schemaProps["properties"].(map[String]any); ok {
        tool.Function.Parameters.Properties = api.NewToolPropertiesMap();
        var for propName, propDef = range props {
        var if propMap, ok = propDef.(map[String]any); ok {
        var prop = api.ToolProperty{
        Type:        api.PropertyType{getStringFromMap(propMap, "type", "String")},;
        Description: getStringFromMap(propMap, "description", ""),;
    }
        tool.Function.Parameters.Properties.Set(propName, prop);
    }
    }
    }
        var if required, ok = schemaProps["required"].([]String); ok {
        tool.Function.Parameters.Required = required;
        var } else if requiredAny, ok = schemaProps["required"].([]any); ok {
        var required = make([]String, len(requiredAny));
        var for i, r = range requiredAny {
        var if s, ok = r.(String); ok {
        required[i] = s;
    }
    }
        tool.Function.Parameters.Required = required;
    }
    }
        return tool;
    }

    public static String getStringFromMap(map[String]any m, String defaultValue) {
        var if val, ok = m[key].(String); ok {
        return val;
    }
        return defaultValue;
    }

    public static boolean isImageAttachment(String filename) {
        var ext = strings.ToLower(filename);
        return strings.HasSuffix(ext, ".png") || strings.HasSuffix(ext, ".jpg") || strings.HasSuffix(ext, ".jpeg") || strings.HasSuffix(ext, ".webp");
    }
        func ptr[T any](v T) *T { return &v }

    public static boolean supportsBrowserTools(String model) {
        return strings.HasPrefix(strings.ToLower(model), "gpt-oss");
    }
        func (s *Server) buildChatRequest(chat *store.Chat, model String, think any, availableTools []map[String]any) (*api.ChatRequest, error) {
        var msgs []api.Message;
        var for _, m = range chat.Messages {
        if m.Content == "" && m.Thinking == "" && len(m.ToolCalls) == 0 && len(m.Attachments) == 0 {
        continue;
    }
        var apiMsg = api.Message{Role: m.Role, Thinking: m.Thinking}
        var sb = strings.Builder{}
        sb.WriteString(m.Content);
        var images []api.ImageData;
        if m.Role == "user" && len(m.Attachments) > 0 {
        var for _, a = range m.Attachments {
        if isImageAttachment(a.Filename) {
        images = append(images, api.ImageData(a.Data));
        } else {
        var content = convertBytesToText(a.Data, a.Filename);
        sb.WriteString(fmt.Sprintf("\n--- File: %s ---\n%s\n--- End of %s ---",;
        a.Filename, content, a.Filename));
    }
    }
    }
        apiMsg.Content = sb.String();
        apiMsg.Images = images;
        switch m.Role {
        case "assistant":;
        if len(m.ToolCalls) > 0 {
        var toolCalls []api.ToolCall;
        var for _, tc = range m.ToolCalls {
        var args api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(tc.Function.Arguments), &args); err != null {
        s.log().Error("failed to parse tool call arguments", "error", err, "function_name", tc.Function.Name, "arguments", tc.Function.Arguments);
        continue;
    }
        toolCalls = append(toolCalls, api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      tc.Function.Name,;
        Arguments: args,;
        },;
        });
    }
        apiMsg.ToolCalls = toolCalls;
    }
        case "tool":;
        apiMsg.Role = "tool";
        apiMsg.Content = m.Content;
        apiMsg.ToolName = m.ToolName;
        case "user", "system":;
        default:;
        s.log().Debug("unknown message role", "role", m.Role);
    }
        msgs = append(msgs, apiMsg);
    }
        var thinkValue *api.ThinkValue;
        if think != null {
        var if boolValue, ok = think.(boolean); ok {
        if boolValue {
        thinkValue = &api.ThinkValue{Value: boolValue}
    }
        var } else if stringValue, ok = think.(String); ok {
        if stringValue != "" && stringValue != "none" {
        thinkValue = &api.ThinkValue{Value: stringValue}
    }
    }
    }
        var req = &api.ChatRequest{
        Model:    model,;
        Messages: msgs,;
        Stream:   ptr(true),;
        Think:    thinkValue,;
    }
        if len(availableTools) > 0 {
        var tools = make(api.Tools, len(availableTools));
        var for i, toolSchema = range availableTools {
        tools[i] = convertToOllamaTool(toolSchema);
    }
        req.Tools = tools;
    }
        return req, null;
    }
}
