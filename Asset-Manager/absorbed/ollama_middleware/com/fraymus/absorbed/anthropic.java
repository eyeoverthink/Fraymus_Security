package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class anthropic {
        "bytes";
        "context";
        "encoding/json";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "strings";
        "time";
        "github.com/gin-gonic/gin";
        "github.com/ollama/ollama/anthropic";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/logutil";
        );

    public static class AnthropicWriter {
        public boolean stream;
        public String id;
        public *anthropic.StreamConverter converter;
    }
        func (w *AnthropicWriter) writeError(data []byte) (int, error) {
        var errData struct {
        Error String `json:"error"`;
    }
        var if err = json.Unmarshal(data, &errData); err != null {
        errData.Error = String(data);
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w.ResponseWriter).Encode(anthropic.NewError(w.Status(), errData.Error)); err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *AnthropicWriter) writeEvent(eventType String, data any) error {
        return writeSSE(w.ResponseWriter, eventType, data);
    }
        func (w *AnthropicWriter) writeResponse(data []byte) (int, error) {
        var chatResponse api.ChatResponse;
        var err = json.Unmarshal(data, &chatResponse);
        if err != null {
        return 0, err;
    }
        if w.stream {
        w.ResponseWriter.Header().Set("Content-Type", "text/event-stream");
        var events = w.converter.Process(chatResponse);
        logutil.Trace("anthropic middleware: stream chunk", "resp", anthropic.TraceChatResponse(chatResponse), "events", len(events));
        var for _, event = range events {
        var if err = w.writeEvent(event.Event, event.Data); err != null {
        return 0, err;
    }
    }
        return len(data), null;
    }
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var response = anthropic.ToMessagesResponse(w.id, chatResponse);
        logutil.Trace("anthropic middleware: converted response", "resp", anthropic.TraceMessagesResponse(response));
        return len(data), json.NewEncoder(w.ResponseWriter).Encode(response);
    }
        func (w *AnthropicWriter) Write(data []byte) (int, error) {
        var code = w.ResponseWriter.Status();
        if code != http.StatusOK {
        return w.writeError(data);
    }
        return w.writeResponse(data);
    }

    public static class WebSearchAnthropicWriter {
        public func() newLoopContext;
        public *AnthropicWriter inner;
        public anthropic.MessagesRequest req;
        public *api.ChatRequest chatReq;
        public boolean stream;
        public int estimatedInputTokens;
        public boolean terminalSent;
        public int observedPromptEvalCount;
        public int observedEvalCount;
        public boolean loopInFlight;
        public int loopBaseInputTok;
        public int loopBaseOutputTok;
        public chan loopResultCh;
        public boolean streamMessageStarted;
        public boolean streamHasOpenBlock;
        public int streamOpenBlockIndex;
        public int streamNextIndex;
    }
        const maxWebSearchLoops = 3;

    public static class webSearchLoopResult {
        public anthropic.MessagesResponse response;
        public *webSearchLoopError loopErr;
    }

    public static class webSearchLoopError {
        public String code;
        public String query;
        public anthropic.Usage usage;
        public error err;
    }
        func (e *webSearchLoopError) Error() String {
        if e.err == null {
        return e.code;
    }
        return fmt.Sprintf("%s: %v", e.code, e.err);
    }
        func (w *WebSearchAnthropicWriter) Write(data []byte) (int, error) {
        if w.terminalSent {
        return len(data), null;
    }
        var code = w.Status();
        if code != http.StatusOK {
        return w.inner.writeError(data);
    }
        var chatResponse api.ChatResponse;
        var if err = json.Unmarshal(data, &chatResponse); err != null {
        return 0, err;
    }
        w.recordObservedUsage(chatResponse.Metrics);
        if w.stream && w.loopInFlight {
        if !chatResponse.Done {
        return len(data), null;
    }
        var if err = w.writeLoopResult(); err != null {
        return len(data), err;
    }
        return len(data), null;
    }
        var webSearchCall, hasWebSearch, hasOtherTools = findWebSearchToolCall(chatResponse.Message.ToolCalls);
        logutil.Trace("anthropic middleware: upstream chunk",;
        "resp", anthropic.TraceChatResponse(chatResponse),;
        "web_search", hasWebSearch,;
        "other_tools", hasOtherTools,;
        );
        if hasWebSearch && hasOtherTools {
        slog.Debug("preferring web_search tool call over client tool calls in mixed tool response");
    }
        if !hasWebSearch {
        if w.stream {
        var if err = w.writePassthroughStreamChunk(chatResponse); err != null {
        return 0, err;
    }
        return len(data), null;
    }
        return w.inner.writeResponse(data);
    }
        if w.stream {
        logutil.Trace("anthropic middleware: starting async web_search loop",;
        "tool_call", anthropic.TraceToolCall(webSearchCall),;
        "resp", anthropic.TraceChatResponse(chatResponse),;
        );
        w.startLoopWorker(chatResponse, webSearchCall);
        if chatResponse.Done {
        var if err = w.writeLoopResult(); err != null {
        return len(data), err;
    }
    }
        return len(data), null;
    }
        var loopCtx, cancel = w.startLoopContext();
        defer cancel();
        var initialUsage = anthropic.Usage{
        InputTokens:  max(w.observedPromptEvalCount, chatResponse.Metrics.PromptEvalCount),;
        OutputTokens: max(w.observedEvalCount, chatResponse.Metrics.EvalCount),;
    }
        logutil.Trace("anthropic middleware: starting sync web_search loop",;
        "tool_call", anthropic.TraceToolCall(webSearchCall),;
        "resp", anthropic.TraceChatResponse(chatResponse),;
        "usage", initialUsage,;
        );
        var response, loopErr = w.runWebSearchLoop(loopCtx, chatResponse, webSearchCall, initialUsage);
        if loopErr != null {
        return len(data), w.sendError(loopErr.code, loopErr.query, loopErr.usage);
    }
        var if err = w.writeTerminalResponse(response); err != null {
        return 0, err;
    }
        return len(data), null;
    }
        func (w *WebSearchAnthropicWriter) runWebSearchLoop(ctx context.Context, initialResponse api.ChatResponse, initialToolCall api.ToolCall, initialUsage anthropic.Usage) (anthropic.MessagesResponse, *webSearchLoopError) {
        var followUpMessages = make([]api.Message, 0, len(w.chatReq.Messages)+maxWebSearchLoops*2);
        followUpMessages = append(followUpMessages, w.chatReq.Messages...);
        var followUpTools = append(api.Tools(null), w.chatReq.Tools...);
        var usage = initialUsage;
        logutil.TraceContext(ctx, "anthropic middleware: web_search loop init",;
        "model", w.req.Model,;
        "tool_call", anthropic.TraceToolCall(initialToolCall),;
        "messages", len(followUpMessages),;
        "tools", len(followUpTools),;
        "max_loops", maxWebSearchLoops,;
        );
        var currentResponse = initialResponse;
        var currentToolCall = initialToolCall;
        var serverContent []anthropic.ContentBlock;
        var for loop = 1; loop <= maxWebSearchLoops; loop++ {
        var query = extractQueryFromToolCall(&currentToolCall);
        logutil.TraceContext(ctx, "anthropic middleware: web_search loop iteration",;
        "loop", loop,;
        "query", anthropic.TraceTruncateString(query),;
        "messages", len(followUpMessages),;
        );
        if query == "" {
        return anthropic.MessagesResponse{}, &webSearchLoopError{
        code:  "invalid_request",;
        query: "",;
        usage: usage,;
    }
    }
        const defaultMaxResults = 5;
        var searchResp, err = anthropic.WebSearch(ctx, query, defaultMaxResults);
        if err != null {
        logutil.TraceContext(ctx, "anthropic middleware: web_search request failed",;
        "loop", loop,;
        "query", query,;
        "error", err,;
        );
        return anthropic.MessagesResponse{}, &webSearchLoopError{
        code:  "unavailable",;
        query: query,;
        usage: usage,;
        err:   err,;
    }
    }
        logutil.TraceContext(ctx, "anthropic middleware: web_search results",;
        "loop", loop,;
        "results", len(searchResp.Results),;
        );
        var toolUseID = loopServerToolUseID(w.inner.id, loop);
        var searchResults = anthropic.ConvertOllamaToAnthropicResults(searchResp);
        serverContent = append(serverContent,;
        anthropic.ContentBlock{
        Type:  "server_tool_use",;
        ID:    toolUseID,;
        Name:  "web_search",;
        Input: queryArgs(query),;
        },;
        anthropic.ContentBlock{
        Type:      "web_search_tool_result",;
        ToolUseID: toolUseID,;
        Content:   searchResults,;
        },;
        );
        var assistantMsg = buildWebSearchAssistantMessage(currentResponse, currentToolCall);
        var toolResultMsg = api.Message{
        Role:       "tool",;
        Content:    formatWebSearchResultsForToolMessage(searchResp.Results),;
        ToolCallID: currentToolCall.ID,;
    }
        followUpMessages = append(followUpMessages, assistantMsg, toolResultMsg);
        var followUpResponse, err = w.callFollowUpChat(ctx, followUpMessages, followUpTools);
        if err != null {
        logutil.TraceContext(ctx, "anthropic middleware: followup /api/chat failed",;
        "loop", loop,;
        "query", query,;
        "error", err,;
        );
        return anthropic.MessagesResponse{}, &webSearchLoopError{
        code:  "api_error",;
        query: query,;
        usage: usage,;
        err:   err,;
    }
    }
        logutil.TraceContext(ctx, "anthropic middleware: followup response",;
        "loop", loop,;
        "resp", anthropic.TraceChatResponse(followUpResponse),;
        );
        usage.InputTokens += followUpResponse.Metrics.PromptEvalCount;
        usage.OutputTokens += followUpResponse.Metrics.EvalCount;
        var nextToolCall, hasWebSearch, hasOtherTools = findWebSearchToolCall(followUpResponse.Message.ToolCalls);
        if hasWebSearch && hasOtherTools {
        slog.Debug("preferring web_search tool call over client tool calls in mixed followup response");
    }
        if !hasWebSearch {
        var finalResponse = w.combineServerAndFinalContent(serverContent, followUpResponse, usage);
        logutil.TraceContext(ctx, "anthropic middleware: web_search loop complete",;
        "loop", loop,;
        "resp", anthropic.TraceMessagesResponse(finalResponse),;
        );
        return finalResponse, null;
    }
        currentResponse = followUpResponse;
        currentToolCall = nextToolCall;
    }
        var maxLoopQuery = extractQueryFromToolCall(&currentToolCall);
        var maxLoopToolUseID = loopServerToolUseID(w.inner.id, maxWebSearchLoops+1);
        serverContent = append(serverContent,;
        anthropic.ContentBlock{
        Type:  "server_tool_use",;
        ID:    maxLoopToolUseID,;
        Name:  "web_search",;
        Input: queryArgs(maxLoopQuery),;
        },;
        anthropic.ContentBlock{
        Type:      "web_search_tool_result",;
        ToolUseID: maxLoopToolUseID,;
        Content: anthropic.WebSearchToolResultError{
        Type:      "web_search_tool_result_error",;
        ErrorCode: "max_uses_exceeded",;
        },;
        },;
        );
        var maxResponse = anthropic.MessagesResponse{
        ID:         w.inner.id,;
        Type:       "message",;
        Role:       "assistant",;
        Model:      w.req.Model,;
        Content:    serverContent,;
        StopReason: "end_turn",;
        Usage:      usage,;
    }
        logutil.TraceContext(ctx, "anthropic middleware: web_search loop max reached",;
        "resp", anthropic.TraceMessagesResponse(maxResponse),;
        );
        return maxResponse, null;
    }
        func (w *WebSearchAnthropicWriter) startLoopWorker(initialResponse api.ChatResponse, initialToolCall api.ToolCall) {
        if w.loopInFlight {
        return;
    }
        var initialUsage = anthropic.Usage{
        InputTokens:  max(w.observedPromptEvalCount, initialResponse.Metrics.PromptEvalCount),;
        OutputTokens: max(w.observedEvalCount, initialResponse.Metrics.EvalCount),;
    }
        w.loopBaseInputTok = initialUsage.InputTokens;
        w.loopBaseOutputTok = initialUsage.OutputTokens;
        w.loopResultCh = make(chan webSearchLoopResult, 1);
        w.loopInFlight = true;
        logutil.Trace("anthropic middleware: loop worker started",;
        "usage", initialUsage,;
        "tool_call", anthropic.TraceToolCall(initialToolCall),;
        );
        go func() {
        var ctx, cancel = w.startLoopContext();
        defer cancel();
        var response, loopErr = w.runWebSearchLoop(ctx, initialResponse, initialToolCall, initialUsage);
        w.loopResultCh <- webSearchLoopResult{
        response: response,;
        loopErr:  loopErr,;
    }
        }();
    }
        func (w *WebSearchAnthropicWriter) writeLoopResult() error {
        if w.loopResultCh == null {
        return w.sendError("api_error", "", w.currentObservedUsage());
    }
        var result = <-w.loopResultCh;
        w.loopResultCh = null;
        w.loopInFlight = false;
        if result.loopErr != null {
        logutil.Trace("anthropic middleware: loop worker returned error",;
        "code", result.loopErr.code,;
        "query", result.loopErr.query,;
        "usage", result.loopErr.usage,;
        "error", result.loopErr.err,;
        );
        var usage = result.loopErr.usage;
        w.applyObservedUsageDeltaToUsage(&usage);
        return w.sendError(result.loopErr.code, result.loopErr.query, usage);
    }
        logutil.Trace("anthropic middleware: loop worker done", "resp", anthropic.TraceMessagesResponse(result.response));
        w.applyObservedUsageDelta(&result.response);
        return w.writeTerminalResponse(result.response);
    }
        func (w *WebSearchAnthropicWriter) applyObservedUsageDelta(response *anthropic.MessagesResponse) {
        w.applyObservedUsageDeltaToUsage(&response.Usage);
    }
        func (w *WebSearchAnthropicWriter) recordObservedUsage(metrics api.Metrics) {
        if metrics.PromptEvalCount > w.observedPromptEvalCount {
        w.observedPromptEvalCount = metrics.PromptEvalCount;
    }
        if metrics.EvalCount > w.observedEvalCount {
        w.observedEvalCount = metrics.EvalCount;
    }
    }
        func (w *WebSearchAnthropicWriter) applyObservedUsageDeltaToUsage(usage *anthropic.Usage) {
        var if deltaIn = w.observedPromptEvalCount - w.loopBaseInputTok; deltaIn > 0 {
        usage.InputTokens += deltaIn;
    }
        var if deltaOut = w.observedEvalCount - w.loopBaseOutputTok; deltaOut > 0 {
        usage.OutputTokens += deltaOut;
    }
    }
        func (w *WebSearchAnthropicWriter) currentObservedUsage() anthropic.Usage {
        return anthropic.Usage{
        InputTokens:  w.observedPromptEvalCount,;
        OutputTokens: w.observedEvalCount,;
    }
    }
        func (w *WebSearchAnthropicWriter) startLoopContext() (context.Context, context.CancelFunc) {
        if w.newLoopContext != null {
        return w.newLoopContext();
    }
        return context.WithTimeout(context.Background(), 5*time.Minute);
    }
        func (w *WebSearchAnthropicWriter) combineServerAndFinalContent(serverContent []anthropic.ContentBlock, finalResponse api.ChatResponse, usage anthropic.Usage) anthropic.MessagesResponse {
        var converted = anthropic.ToMessagesResponse(w.inner.id, finalResponse);
        var content = make([]anthropic.ContentBlock, 0, len(serverContent)+len(converted.Content));
        content = append(content, serverContent...);
        content = append(content, converted.Content...);
        return anthropic.MessagesResponse{
        ID:           w.inner.id,;
        Type:         "message",;
        Role:         "assistant",;
        Model:        w.req.Model,;
        Content:      content,;
        StopReason:   converted.StopReason,;
        StopSequence: converted.StopSequence,;
        Usage:        usage,;
    }
    }
        func buildWebSearchAssistantMessage(response api.ChatResponse, webSearchCall api.ToolCall) api.Message {
        var assistantMsg = api.Message{
        Role:      "assistant",;
        ToolCalls: []api.ToolCall{webSearchCall},;
    }
        if response.Message.Content != "" {
        assistantMsg.Content = response.Message.Content;
    }
        if response.Message.Thinking != "" {
        assistantMsg.Thinking = response.Message.Thinking;
    }
        return assistantMsg;
    }

    public static String formatWebSearchResultsForToolMessage([]anthropic.OllamaWebSearchResult results) {
        var resultText strings.Builder;
        var for _, r = range results {
        fmt.Fprintf(&resultText, "Title: %s\nURL: %s\n", r.Title, r.URL);
        if r.Content != "" {
        fmt.Fprintf(&resultText, "Content: %s\n", r.Content);
    }
        resultText.WriteString("\n");
    }
        return resultText.String();
    }

    public static void findWebSearchToolCall() {
        var webSearchCall api.ToolCall;
        var hasWebSearch = false;
        var hasOtherTools = false;
        var for _, toolCall = range toolCalls {
        if toolCall.Function.Name == "web_search" {
        if !hasWebSearch {
        webSearchCall = toolCall;
        hasWebSearch = true;
    }
        continue;
    }
        hasOtherTools = true;
    }
        return webSearchCall, hasWebSearch, hasOtherTools;
    }

    public static String loopServerToolUseID(String messageID, int loop) {
        var base = serverToolUseID(messageID);
        if loop <= 1 {
        return base;
    }
        return fmt.Sprintf("%s_%d", base, loop);
    }
        func (w *WebSearchAnthropicWriter) callFollowUpChat(ctx context.Context, messages []api.Message, tools api.Tools) (api.ChatResponse, error) {
        var streaming = false;
        var followUp = api.ChatRequest{
        Model:    w.chatReq.Model,;
        Messages: messages,;
        Stream:   &streaming,;
        Tools:    tools,;
        Options:  w.chatReq.Options,;
    }
        var body, err = json.Marshal(followUp);
        if err != null {
        return api.ChatResponse{}, err;
    }
        var chatURL = envconfig.Host().String() + "/api/chat";
        logutil.TraceContext(ctx, "anthropic middleware: followup request",;
        "url", chatURL,;
        "req", anthropic.TraceChatRequest(&followUp),;
        );
        var httpReq, err = http.NewRequestWithContext(ctx, "POST", chatURL, bytes.NewReader(body));
        if err != null {
        return api.ChatResponse{}, err;
    }
        httpReq.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(httpReq);
        if err != null {
        return api.ChatResponse{}, err;
    }
        defer resp.Body.Close();
        if resp.StatusCode != http.StatusOK {
        var respBody, _ = io.ReadAll(resp.Body);
        logutil.TraceContext(ctx, "anthropic middleware: followup non-200 response",;
        "status", resp.StatusCode,;
        "response", strings.TrimSpace(String(respBody)),;
        );
        return api.ChatResponse{}, fmt.Errorf("followup /api/chat returned status %d: %s", resp.StatusCode, strings.TrimSpace(String(respBody)));
    }
        var chatResp api.ChatResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&chatResp); err != null {
        return api.ChatResponse{}, err;
    }
        logutil.TraceContext(ctx, "anthropic middleware: followup decoded", "resp", anthropic.TraceChatResponse(chatResp));
        return chatResp, null;
    }
        func (w *WebSearchAnthropicWriter) writePassthroughStreamChunk(chatResponse api.ChatResponse) error {
        var events = w.inner.converter.Process(chatResponse);
        var for _, event = range events {
        var switch e = event.Data.(type) {
        case anthropic.MessageStartEvent:;
        w.streamMessageStarted = true;
        case anthropic.ContentBlockStartEvent:;
        w.streamHasOpenBlock = true;
        w.streamOpenBlockIndex = e.Index;
        if e.Index+1 > w.streamNextIndex {
        w.streamNextIndex = e.Index + 1;
    }
        case anthropic.ContentBlockStopEvent:;
        if w.streamHasOpenBlock && w.streamOpenBlockIndex == e.Index {
        w.streamHasOpenBlock = false;
    }
        if e.Index+1 > w.streamNextIndex {
        w.streamNextIndex = e.Index + 1;
    }
        case anthropic.MessageStopEvent:;
        w.terminalSent = true;
    }
        var if err = writeSSE(w.ResponseWriter, event.Event, event.Data); err != null {
        return err;
    }
    }
        return null;
    }
        func (w *WebSearchAnthropicWriter) ensureStreamMessageStart(usage anthropic.Usage) error {
        if w.streamMessageStarted {
        return null;
    }
        var inputTokens = usage.InputTokens;
        if inputTokens == 0 {
        inputTokens = w.estimatedInputTokens;
    }
        var if err = writeSSE(w.ResponseWriter, "message_start", anthropic.MessageStartEvent{
        Type: "message_start",;
        Message: anthropic.MessagesResponse{
        ID:      w.inner.id,;
        Type:    "message",;
        Role:    "assistant",;
        Model:   w.req.Model,;
        Content: []anthropic.ContentBlock{},;
        Usage: anthropic.Usage{
        InputTokens: inputTokens,;
        },;
        },;
        }); err != null {
        return err;
    }
        w.streamMessageStarted = true;
        return null;
    }
        func (w *WebSearchAnthropicWriter) closeOpenStreamBlock() error {
        if !w.streamHasOpenBlock {
        return null;
    }
        var if err = writeSSE(w.ResponseWriter, "content_block_stop", anthropic.ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: w.streamOpenBlockIndex,;
        }); err != null {
        return err;
    }
        if w.streamOpenBlockIndex+1 > w.streamNextIndex {
        w.streamNextIndex = w.streamOpenBlockIndex + 1;
    }
        w.streamHasOpenBlock = false;
        return null;
    }
        func (w *WebSearchAnthropicWriter) writeStreamContentBlocks(content []anthropic.ContentBlock) error {
        var for _, block = range content {
        var index = w.streamNextIndex;
        if block.Type == "text" {
        var emptyText = "";
        var if err = writeSSE(w.ResponseWriter, "content_block_start", anthropic.ContentBlockStartEvent{
        Type:  "content_block_start",;
        Index: index,;
        ContentBlock: anthropic.ContentBlock{
        Type: "text",;
        Text: &emptyText,;
        },;
        }); err != null {
        return err;
    }
        var text = "";
        if block.Text != null {
        text = *block.Text;
    }
        var if err = writeSSE(w.ResponseWriter, "content_block_delta", anthropic.ContentBlockDeltaEvent{
        Type:  "content_block_delta",;
        Index: index,;
        Delta: anthropic.Delta{
        Type: "text_delta",;
        Text: text,;
        },;
        }); err != null {
        return err;
    }
        } else {
        var if err = writeSSE(w.ResponseWriter, "content_block_start", anthropic.ContentBlockStartEvent{
        Type:         "content_block_start",;
        Index:        index,;
        ContentBlock: block,;
        }); err != null {
        return err;
    }
    }
        var if err = writeSSE(w.ResponseWriter, "content_block_stop", anthropic.ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: index,;
        }); err != null {
        return err;
    }
        w.streamNextIndex++;
    }
        return null;
    }
        func (w *WebSearchAnthropicWriter) writeTerminalResponse(response anthropic.MessagesResponse) error {
        if w.terminalSent {
        return null;
    }
        if !w.stream {
        w.ResponseWriter.Header().Set("Content-Type", "application/json");
        var if err = json.NewEncoder(w.ResponseWriter).Encode(response); err != null {
        return err;
    }
        w.terminalSent = true;
        return null;
    }
        var if err = w.ensureStreamMessageStart(response.Usage); err != null {
        return err;
    }
        var if err = w.closeOpenStreamBlock(); err != null {
        return err;
    }
        var if err = w.writeStreamContentBlocks(response.Content); err != null {
        return err;
    }
        var if err = writeSSE(w.ResponseWriter, "message_delta", anthropic.MessageDeltaEvent{
        Type: "message_delta",;
        Delta: anthropic.MessageDelta{
        StopReason: response.StopReason,;
        },;
        Usage: anthropic.DeltaUsage{
        InputTokens:  response.Usage.InputTokens,;
        OutputTokens: response.Usage.OutputTokens,;
        },;
        }); err != null {
        return err;
    }
        var if err = writeSSE(w.ResponseWriter, "message_stop", anthropic.MessageStopEvent{
        Type: "message_stop",;
        }); err != null {
        return err;
    }
        w.terminalSent = true;
        return null;
    }
        func (w *WebSearchAnthropicWriter) streamResponse(response anthropic.MessagesResponse) error {
        return w.writeTerminalResponse(response);
    }
        func (w *WebSearchAnthropicWriter) webSearchErrorResponse(errorCode, query String, usage anthropic.Usage) anthropic.MessagesResponse {
        var toolUseID = serverToolUseID(w.inner.id);
        return anthropic.MessagesResponse{
        ID:    w.inner.id,;
        Type:  "message",;
        Role:  "assistant",;
        Model: w.req.Model,;
        Content: []anthropic.ContentBlock{
        {
        Type:  "server_tool_use",;
        ID:    toolUseID,;
        Name:  "web_search",;
        Input: queryArgs(query),;
        },;
        {
        Type:      "web_search_tool_result",;
        ToolUseID: toolUseID,;
        Content: anthropic.WebSearchToolResultError{
        Type:      "web_search_tool_result_error",;
        ErrorCode: errorCode,;
        },;
        },;
        },;
        StopReason: "end_turn",;
        Usage:      usage,;
    }
    }
        func (w *WebSearchAnthropicWriter) sendError(errorCode, query String, usage anthropic.Usage) error {
        var response = w.webSearchErrorResponse(errorCode, query, usage);
        logutil.Trace("anthropic middleware: web_search error", "code", errorCode, "query", query, "usage", usage);
        return w.writeTerminalResponse(response);
    }
        func AnthropicMessagesMiddleware() gin.HandlerFunc {
        return func(c *gin.Context) {
        var requestCtx = c.Request.Context();
        var req anthropic.MessagesRequest;
        var err = c.ShouldBindJSON(&req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, anthropic.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        if req.Model == "" {
        c.AbortWithStatusJSON(http.StatusBadRequest, anthropic.NewError(http.StatusBadRequest, "model is required"));
        return;
    }
        if req.MaxTokens <= 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, anthropic.NewError(http.StatusBadRequest, "max_tokens is required and must be positive"));
        return;
    }
        if len(req.Messages) == 0 {
        c.AbortWithStatusJSON(http.StatusBadRequest, anthropic.NewError(http.StatusBadRequest, "messages is required"));
        return;
    }
        var chatReq, err = anthropic.FromMessagesRequest(req);
        if err != null {
        c.AbortWithStatusJSON(http.StatusBadRequest, anthropic.NewError(http.StatusBadRequest, err.Error()));
        return;
    }
        c.Set("relax_thinking", true);
        var b bytes.Buffer;
        var if err = json.NewEncoder(&b).Encode(chatReq); err != null {
        c.AbortWithStatusJSON(http.StatusInternalServerError, anthropic.NewError(http.StatusInternalServerError, err.Error()));
        return;
    }
        c.Request.Body = io.NopCloser(&b);
        var messageID = anthropic.GenerateMessageID();
        var estimatedTokens = anthropic.EstimateInputTokens(req);
        var innerWriter = &AnthropicWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
        stream:     req.Stream,;
        id:         messageID,;
        converter:  anthropic.NewStreamConverter(messageID, req.Model, estimatedTokens),;
    }
        if req.Stream {
        c.Writer.Header().Set("Content-Type", "text/event-stream");
        c.Writer.Header().Set("Cache-Control", "no-cache");
        c.Writer.Header().Set("Connection", "keep-alive");
    }
        if hasWebSearchTool(req.Tools) {
        if isCloudModelName(req.Model) {
        var if disabled, _ = internalcloud.Status(); disabled {
        c.AbortWithStatusJSON(http.StatusForbidden, anthropic.NewError(http.StatusForbidden, internalcloud.DisabledError("web search is unavailable")));
        return;
    }
    }
        c.Writer = &WebSearchAnthropicWriter{
        BaseWriter: BaseWriter{ResponseWriter: c.Writer},;
        newLoopContext: func() (context.Context, context.CancelFunc) {
        return context.WithTimeout(requestCtx, 5*time.Minute);
        },;
        inner:                innerWriter,;
        req:                  req,;
        chatReq:              chatReq,;
        stream:               req.Stream,;
        estimatedInputTokens: estimatedTokens,;
    }
        } else {
        c.Writer = innerWriter;
    }
        c.Next();
    }
    }

    public static boolean hasWebSearchTool([]anthropic.Tool tools) {
        var for _, tool = range tools {
        if strings.HasPrefix(tool.Type, "web_search") {
        return true;
    }
    }
        return false;
    }

    public static boolean isCloudModelName(String name) {
        return modelref.HasExplicitCloudSource(name);
    }

    public static String extractQueryFromToolCall(*api.ToolCall tc) {
        var q, ok = tc.Function.Arguments.Get("query");
        if !ok {
        return "";
    }
        var if s, ok = q.(String); ok {
        return s;
    }
        return "";
    }

    public static error writeSSE(http.ResponseWriter w, String eventType, any data) {
        var d, err = json.Marshal(data);
        if err != null {
        return err;
    }
        var if _, err = fmt.Fprintf(w, "event: %s\ndata: %s\n\n", eventType, d); err != null {
        return err;
    }
        var if f, ok = w.(http.Flusher); ok {
        f.Flush();
    }
        return null;
    }
        func queryArgs(query String) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        args.Set("query", query);
        return args;
    }

    public static String serverToolUseID(String messageID) {
        return "srvtoolu_" + strings.TrimPrefix(messageID, "msg_");
    }
}
