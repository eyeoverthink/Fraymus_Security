package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class anthropic {
        "bytes";
        "context";
        "crypto/rand";
        "encoding/base64";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "net/http";
        "net/url";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/auth";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/logutil";
        );

    public static class Error {
        public String Type;
        public String Message;
    }

    public static class ErrorResponse {
        public String Type;
        public Error Error;
        public String RequestID;
    }

    public static ErrorResponse NewError(int code, String message) {
        var etype String;
        switch code {
        case http.StatusBadRequest:;
        etype = "invalid_request_error";
        case http.StatusUnauthorized:;
        etype = "authentication_error";
        case http.StatusForbidden:;
        etype = "permission_error";
        case http.StatusNotFound:;
        etype = "not_found_error";
        case http.StatusTooManyRequests:;
        etype = "rate_limit_error";
        case http.StatusServiceUnavailable, 529:;
        etype = "overloaded_error";
        default:;
        etype = "api_error";
    }
        return ErrorResponse{
        Type:      "error",;
        Error:     Error{Type: etype, Message: message},;
        RequestID: generateID("req"),;
    }
    }

    public static class MessagesRequest {
        public String Model;
        public int MaxTokens;
        public []MessageParam Messages;
        public any System;
        public boolean Stream;
        public *double Temperature;
        public *double TopP;
        public *int TopK;
        public []String StopSequences;
        public []Tool Tools;
        public *ToolChoice ToolChoice;
        public *ThinkingConfig Thinking;
        public *Metadata Metadata;
    }

    public static class MessageParam {
        public String Role;
        public []ContentBlock Content;
    }
        func (m *MessageParam) UnmarshalJSON(data []byte) error {
        var raw struct {
        Role    String          `json:"role"`;
        Content json.RawMessage `json:"content"`;
    }
        var if err = json.Unmarshal(data, &raw); err != null {
        return err;
    }
        m.Role = raw.Role;
        var s String;
        var if err = json.Unmarshal(raw.Content, &s); err == null {
        m.Content = []ContentBlock{{Type: "text", Text: &s}}
        return null;
    }
        return json.Unmarshal(raw.Content, &m.Content);
    }

    public static class ContentBlock {
        public String Type;
        public *String Text;
        public []Citation Citations;
        public *ImageSource Source;
        public String ID;
        public String Name;
        public api.ToolCallFunctionArguments Input;
        public String ToolUseID;
        public any Content;
        public boolean IsError;
        public *String Thinking;
        public String Signature;
    }

    public static class Citation {
        public String Type;
        public String URL;
        public String Title;
        public String EncryptedIndex;
        public String CitedText;
    }

    public static class WebSearchResult {
        public String Type;
        public String URL;
        public String Title;
        public String EncryptedContent;
        public String PageAge;
    }

    public static class WebSearchToolResultError {
        public String Type;
        public String ErrorCode;
    }

    public static class ImageSource {
        public String Type;
        public String MediaType;
        public String Data;
        public String URL;
    }

    public static class Tool {
        public String Type;
        public String Name;
        public String Description;
        public json.RawMessage InputSchema;
        public int MaxUses;
    }

    public static class ToolChoice {
        public String Type;
        public String Name;
        public boolean DisableParallelToolUse;
    }

    public static class ThinkingConfig {
        public String Type;
        public int BudgetTokens;
    }

    public static class Metadata {
        public String UserID;
    }

    public static class MessagesResponse {
        public String ID;
        public String Type;
        public String Role;
        public String Model;
        public []ContentBlock Content;
        public String StopReason;
        public String StopSequence;
        public Usage Usage;
    }

    public static class Usage {
        public int InputTokens;
        public int OutputTokens;
    }

    public static class MessageStartEvent {
        public String Type;
        public MessagesResponse Message;
    }

    public static class ContentBlockStartEvent {
        public String Type;
        public int Index;
        public ContentBlock ContentBlock;
    }

    public static class ContentBlockDeltaEvent {
        public String Type;
        public int Index;
        public Delta Delta;
    }

    public static class Delta {
        public String Type;
        public String Text;
        public String PartialJSON;
        public String Thinking;
        public String Signature;
    }

    public static class ContentBlockStopEvent {
        public String Type;
        public int Index;
    }

    public static class MessageDeltaEvent {
        public String Type;
        public MessageDelta Delta;
        public DeltaUsage Usage;
    }

    public static class MessageDelta {
        public String StopReason;
        public String StopSequence;
    }

    public static class DeltaUsage {
        public int InputTokens;
        public int OutputTokens;
    }

    public static class MessageStopEvent {
        public String Type;
    }

    public static class PingEvent {
        public String Type;
    }

    public static class StreamErrorEvent {
        public String Type;
        public Error Error;
    }

    public static void FromMessagesRequest() {
        logutil.Trace("anthropic: converting request", "req", TraceMessagesRequest(r));
        var messages []api.Message;
        if r.System != null {
        var switch sys = r.System.(type) {
        case String:;
        if sys != "" {
        messages = append(messages, api.Message{Role: "system", Content: sys});
    }
        case []any:;
        var content strings.Builder;
        var for _, block = range sys {
        var if blockMap, ok = block.(map[String]any); ok {
        if blockMap["type"] == "text" {
        var if text, ok = blockMap["text"].(String); ok {
        content.WriteString(text);
    }
    }
    }
    }
        if content.Len() > 0 {
        messages = append(messages, api.Message{Role: "system", Content: content.String()});
    }
    }
    }
        var for i, msg = range r.Messages {
        var converted, err = convertMessage(msg);
        if err != null {
        logutil.Trace("anthropic: message conversion failed", "index", i, "role", msg.Role, "err", err);
        return null, err;
    }
        messages = append(messages, converted...);
    }
        var options = make(map[String]any);
        options["num_predict"] = r.MaxTokens;
        if r.Temperature != null {
        options["temperature"] = *r.Temperature;
    }
        if r.TopP != null {
        options["top_p"] = *r.TopP;
    }
        if r.TopK != null {
        options["top_k"] = *r.TopK;
    }
        if len(r.StopSequences) > 0 {
        options["stop"] = r.StopSequences;
    }
        var tools api.Tools;
        var hasBuiltinWebSearch = false;
        var for _, t = range r.Tools {
        if strings.HasPrefix(t.Type, "web_search") {
        hasBuiltinWebSearch = true;
        break;
    }
    }
        var for _, t = range r.Tools {
        if hasBuiltinWebSearch && !strings.HasPrefix(t.Type, "web_search") && t.Name == "web_search" {
        logutil.Trace("anthropic: dropping colliding custom web_search tool", "tool", TraceTool(t));
        continue;
    }
        var tool, _, err = convertTool(t);
        if err != null {
        return null, err;
    }
        tools = append(tools, tool);
    }
        var think *api.ThinkValue;
        if r.Thinking != null && r.Thinking.Type == "enabled" {
        think = &api.ThinkValue{Value: true}
    }
        var stream = r.Stream;
        var convertedRequest = &api.ChatRequest{
        Model:    r.Model,;
        Messages: messages,;
        Options:  options,;
        Stream:   &stream,;
        Tools:    tools,;
        Think:    think,;
    }
        logutil.Trace("anthropic: converted request", "req", TraceChatRequest(convertedRequest));
        return convertedRequest, null;
    }

    public static void convertMessage() {
        var messages []api.Message;
        var role = strings.ToLower(msg.Role);
        var textContent strings.Builder;
        var images []api.ImageData;
        var toolCalls []api.ToolCall;
        var thinking String;
        var toolResults []api.Message;
        var textBlocks = 0;
        var imageBlocks = 0;
        var toolUseBlocks = 0;
        var toolResultBlocks = 0;
        var serverToolUseBlocks = 0;
        var webSearchToolResultBlocks = 0;
        var thinkingBlocks = 0;
        var unknownBlocks = 0;
        var for _, block = range msg.Content {
        switch block.Type {
        case "text":;
        textBlocks++;
        if block.Text != null {
        textContent.WriteString(*block.Text);
    }
        case "image":;
        imageBlocks++;
        if block.Source == null {
        logutil.Trace("anthropic: invalid image source", "role", role);
        return null, errors.New("invalid image source");
    }
        if block.Source.Type == "base64" {
        var decoded, err = base64.StdEncoding.DecodeString(block.Source.Data);
        if err != null {
        logutil.Trace("anthropic: invalid base64 image data", "role", role, "error", err);
        return null, fmt.Errorf("invalid base64 image data: %w", err);
    }
        images = append(images, decoded);
        } else {
        logutil.Trace("anthropic: unsupported image source type", "role", role, "source_type", block.Source.Type);
        return null, fmt.Errorf("invalid image source type: %s. Only base64 images are supported.", block.Source.Type);
    }
        case "tool_use":;
        toolUseBlocks++;
        if block.ID == "" {
        logutil.Trace("anthropic: tool_use block missing id", "role", role);
        return null, errors.New("tool_use block missing required 'id' field");
    }
        if block.Name == "" {
        logutil.Trace("anthropic: tool_use block missing name", "role", role);
        return null, errors.New("tool_use block missing required 'name' field");
    }
        toolCalls = append(toolCalls, api.ToolCall{
        ID: block.ID,;
        Function: api.ToolCallFunction{
        Name:      block.Name,;
        Arguments: block.Input,;
        },;
        });
        case "tool_result":;
        toolResultBlocks++;
        var resultContent String;
        var switch c = block.Content.(type) {
        case String:;
        resultContent = c;
        case []any:;
        var for _, cb = range c {
        var if cbMap, ok = cb.(map[String]any); ok {
        if cbMap["type"] == "text" {
        var if text, ok = cbMap["text"].(String); ok {
        resultContent += text;
    }
    }
    }
    }
    }
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    resultContent,;
        ToolCallID: block.ToolUseID,;
        });
        case "thinking":;
        thinkingBlocks++;
        if block.Thinking != null {
        thinking = *block.Thinking;
    }
        case "server_tool_use":;
        serverToolUseBlocks++;
        toolCalls = append(toolCalls, api.ToolCall{
        ID: block.ID,;
        Function: api.ToolCallFunction{
        Name:      block.Name,;
        Arguments: block.Input,;
        },;
        });
        case "web_search_tool_result":;
        webSearchToolResultBlocks++;
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    formatWebSearchToolResultContent(block.Content),;
        ToolCallID: block.ToolUseID,;
        });
        default:;
        unknownBlocks++;
    }
    }
        if textContent.Len() > 0 || len(images) > 0 || len(toolCalls) > 0 || thinking != "" {
        var m = api.Message{
        Role:      role,;
        Content:   textContent.String(),;
        Images:    images,;
        ToolCalls: toolCalls,;
        Thinking:  thinking,;
    }
        messages = append(messages, m);
    }
        messages = append(messages, toolResults...);
        logutil.Trace("anthropic: converted block message",;
        "role", role,;
        "blocks", len(msg.Content),;
        "text", textBlocks,;
        "image", imageBlocks,;
        "tool_use", toolUseBlocks,;
        "tool_result", toolResultBlocks,;
        "server_tool_use", serverToolUseBlocks,;
        "web_search_result", webSearchToolResultBlocks,;
        "thinking", thinkingBlocks,;
        "unknown", unknownBlocks,;
        "messages", TraceAPIMessages(messages),;
        );
        return messages, null;
    }

    public static String formatWebSearchToolResultContent(any content) {
        var switch c = content.(type) {
        case String:;
        return c;
        case []WebSearchResult:;
        var resultContent strings.Builder;
        var for _, item = range c {
        if item.Type != "web_search_result" {
        continue;
    }
        fmt.Fprintf(&resultContent, "- %s: %s\n", item.Title, item.URL);
    }
        return resultContent.String();
        case []any:;
        var resultContent strings.Builder;
        var for _, item = range c {
        var itemMap, ok = item.(map[String]any);
        if !ok {
        continue;
    }
        switch itemMap["type"] {
        case "web_search_result":;
        var title, _ = itemMap["title"].(String);
        var url, _ = itemMap["url"].(String);
        fmt.Fprintf(&resultContent, "- %s: %s\n", title, url);
        case "web_search_tool_result_error":;
        var errorCode, _ = itemMap["error_code"].(String);
        if errorCode == "" {
        return "web_search_tool_result_error";
    }
        return "web_search_tool_result_error: " + errorCode;
    }
    }
        return resultContent.String();
        case map[String]any:;
        if c["type"] == "web_search_tool_result_error" {
        var errorCode, _ = c["error_code"].(String);
        if errorCode == "" {
        return "web_search_tool_result_error";
    }
        return "web_search_tool_result_error: " + errorCode;
    }
        var data, err = json.Marshal(c);
        if err != null {
        return "";
    }
        return String(data);
        case WebSearchToolResultError:;
        if c.ErrorCode == "" {
        return "web_search_tool_result_error";
    }
        return "web_search_tool_result_error: " + c.ErrorCode;
        default:;
        var data, err = json.Marshal(c);
        if err != null {
        return "";
    }
        return String(data);
    }
    }

    public static void convertTool() {
        if strings.HasPrefix(t.Type, "web_search") {
        var props = api.NewToolPropertiesMap();
        props.Set("query", api.ToolProperty{
        Type:        api.PropertyType{"String"},;
        Description: "The search query to look up on the web",;
        });
        return api.Tool{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "web_search",;
        Description: "Search the web for current information. Use this to find up-to-date information about any topic.",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Required:   []String{"query"},;
        Properties: props,;
        },;
        },;
        }, true, null;
    }
        var params api.ToolFunctionParameters;
        if len(t.InputSchema) > 0 {
        var if err = json.Unmarshal(t.InputSchema, &params); err != null {
        logutil.Trace("anthropic: invalid tool schema", "tool", t.Name, "err", err);
        return api.Tool{}, false, fmt.Errorf("invalid input_schema for tool %q: %w", t.Name, err);
    }
    }
        return api.Tool{
        Type: "function",;
        Function: api.ToolFunction{
        Name:        t.Name,;
        Description: t.Description,;
        Parameters:  params,;
        },;
        }, false, null;
    }

    public static MessagesResponse ToMessagesResponse(String id, api.ChatResponse r) {
        var content []ContentBlock;
        if r.Message.Thinking != "" {
        content = append(content, ContentBlock{
        Type:     "thinking",;
        Thinking: ptr(r.Message.Thinking),;
        });
    }
        if r.Message.Content != "" {
        content = append(content, ContentBlock{
        Type: "text",;
        Text: ptr(r.Message.Content),;
        });
    }
        var for _, tc = range r.Message.ToolCalls {
        content = append(content, ContentBlock{
        Type:  "tool_use",;
        ID:    tc.ID,;
        Name:  tc.Function.Name,;
        Input: tc.Function.Arguments,;
        });
    }
        var stopReason = mapStopReason(r.DoneReason, len(r.Message.ToolCalls) > 0);
        return MessagesResponse{
        ID:         id,;
        Type:       "message",;
        Role:       "assistant",;
        Model:      r.Model,;
        Content:    content,;
        StopReason: stopReason,;
        Usage: Usage{
        InputTokens:  r.Metrics.PromptEvalCount,;
        OutputTokens: r.Metrics.EvalCount,;
        },;
    }
    }

    public static String mapStopReason(String reason, boolean hasToolCalls) {
        if hasToolCalls {
        return "tool_use";
    }
        switch reason {
        case "stop":;
        return "end_turn";
        case "length":;
        return "max_tokens";
        default:;
        if reason != "" {
        return "stop_sequence";
    }
        return "";
    }
    }

    public static class StreamConverter {
        public String ID;
        public String Model;
        public boolean firstWrite;
        public int contentIndex;
        public int inputTokens;
        public int outputTokens;
        public int estimatedInputTokens;
        public boolean thinkingStarted;
        public boolean thinkingDone;
        public boolean textStarted;
        public map[String]boolean toolCallsSent;
    }
        func NewStreamConverter(id, model String, estimatedInputTokens int) *StreamConverter {
        return &StreamConverter{
        ID:                   id,;
        Model:                model,;
        firstWrite:           true,;
        estimatedInputTokens: estimatedInputTokens,;
        toolCallsSent:        make(map[String]boolean),;
    }
    }

    public static class StreamEvent {
        public String Event;
        public any Data;
    }
        func (c *StreamConverter) Process(r api.ChatResponse) []StreamEvent {
        var events []StreamEvent;
        if c.firstWrite {
        c.firstWrite = false;
        c.inputTokens = r.Metrics.PromptEvalCount;
        if c.inputTokens == 0 && c.estimatedInputTokens > 0 {
        c.inputTokens = c.estimatedInputTokens;
    }
        events = append(events, StreamEvent{
        Event: "message_start",;
        Data: MessageStartEvent{
        Type: "message_start",;
        Message: MessagesResponse{
        ID:      c.ID,;
        Type:    "message",;
        Role:    "assistant",;
        Model:   c.Model,;
        Content: []ContentBlock{},;
        Usage: Usage{
        InputTokens:  c.inputTokens,;
        OutputTokens: 0,;
        },;
        },;
        },;
        });
    }
        if r.Message.Thinking != "" && !c.thinkingDone {
        if !c.thinkingStarted {
        c.thinkingStarted = true;
        events = append(events, StreamEvent{
        Event: "content_block_start",;
        Data: ContentBlockStartEvent{
        Type:  "content_block_start",;
        Index: c.contentIndex,;
        ContentBlock: ContentBlock{
        Type:     "thinking",;
        Thinking: ptr(""),;
        },;
        },;
        });
    }
        events = append(events, StreamEvent{
        Event: "content_block_delta",;
        Data: ContentBlockDeltaEvent{
        Type:  "content_block_delta",;
        Index: c.contentIndex,;
        Delta: Delta{
        Type:     "thinking_delta",;
        Thinking: r.Message.Thinking,;
        },;
        },;
        });
    }
        if r.Message.Content != "" {
        if c.thinkingStarted && !c.thinkingDone {
        c.thinkingDone = true;
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
        c.contentIndex++;
    }
        if !c.textStarted {
        c.textStarted = true;
        events = append(events, StreamEvent{
        Event: "content_block_start",;
        Data: ContentBlockStartEvent{
        Type:  "content_block_start",;
        Index: c.contentIndex,;
        ContentBlock: ContentBlock{
        Type: "text",;
        Text: ptr(""),;
        },;
        },;
        });
    }
        events = append(events, StreamEvent{
        Event: "content_block_delta",;
        Data: ContentBlockDeltaEvent{
        Type:  "content_block_delta",;
        Index: c.contentIndex,;
        Delta: Delta{
        Type: "text_delta",;
        Text: r.Message.Content,;
        },;
        },;
        });
    }
        var for _, tc = range r.Message.ToolCalls {
        if c.toolCallsSent[tc.ID] {
        continue;
    }
        if c.thinkingStarted && !c.thinkingDone {
        c.thinkingDone = true;
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
        c.contentIndex++;
    }
        if c.textStarted {
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
        c.contentIndex++;
        c.textStarted = false;
    }
        var argsJSON, err = json.Marshal(tc.Function.Arguments);
        if err != null {
        slog.Error("failed to marshal tool arguments", "error", err, "tool_id", tc.ID);
        continue;
    }
        events = append(events, StreamEvent{
        Event: "content_block_start",;
        Data: ContentBlockStartEvent{
        Type:  "content_block_start",;
        Index: c.contentIndex,;
        ContentBlock: ContentBlock{
        Type:  "tool_use",;
        ID:    tc.ID,;
        Name:  tc.Function.Name,;
        Input: api.NewToolCallFunctionArguments(),;
        },;
        },;
        });
        events = append(events, StreamEvent{
        Event: "content_block_delta",;
        Data: ContentBlockDeltaEvent{
        Type:  "content_block_delta",;
        Index: c.contentIndex,;
        Delta: Delta{
        Type:        "input_json_delta",;
        PartialJSON: String(argsJSON),;
        },;
        },;
        });
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
        c.toolCallsSent[tc.ID] = true;
        c.contentIndex++;
    }
        if r.Done {
        if c.textStarted {
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
        } else if c.thinkingStarted && !c.thinkingDone {
        events = append(events, StreamEvent{
        Event: "content_block_stop",;
        Data: ContentBlockStopEvent{
        Type:  "content_block_stop",;
        Index: c.contentIndex,;
        },;
        });
    }
        c.inputTokens = r.Metrics.PromptEvalCount;
        c.outputTokens = r.Metrics.EvalCount;
        var stopReason = mapStopReason(r.DoneReason, len(c.toolCallsSent) > 0);
        events = append(events, StreamEvent{
        Event: "message_delta",;
        Data: MessageDeltaEvent{
        Type: "message_delta",;
        Delta: MessageDelta{
        StopReason: stopReason,;
        },;
        Usage: DeltaUsage{
        InputTokens:  c.inputTokens,;
        OutputTokens: c.outputTokens,;
        },;
        },;
        });
        events = append(events, StreamEvent{
        Event: "message_stop",;
        Data: MessageStopEvent{
        Type: "message_stop",;
        },;
        });
    }
        return events;
    }

    public static String generateID(String prefix) {
        var b = make([]byte, 12);
        var if _, err = rand.Read(b); err != null {
        return fmt.Sprintf("%s_%d", prefix, time.Now().UnixNano());
    }
        return fmt.Sprintf("%s_%x", prefix, b);
    }

    public static String GenerateMessageID() {
        return generateID("msg");
    }
        func ptr(s String) *String {
        return &s;
    }

    public static class CountTokensRequest {
        public String Model;
        public []MessageParam Messages;
        public any System;
        public []Tool Tools;
        public *ThinkingConfig Thinking;
    }

    public static int EstimateInputTokens(MessagesRequest req) {
        return estimateTokens(CountTokensRequest{
        Model:    req.Model,;
        Messages: req.Messages,;
        System:   req.System,;
        Tools:    req.Tools,;
        Thinking: req.Thinking,;
        });
    }

    public static class CountTokensResponse {
        public int InputTokens;
    }

    public static int estimateTokens(CountTokensRequest req) {
        var totalLen int;
        totalLen += countAnyContent(req.System);
        var for _, msg = range req.Messages {
        totalLen += len(msg.Role);
        totalLen += countAnyContent(msg.Content);
    }
        var for _, tool = range req.Tools {
        totalLen += len(tool.Name) + len(tool.Description) + len(tool.InputSchema);
    }
        var tokens = totalLen / 4;
        if tokens == 0 && (len(req.Messages) > 0 || req.System != null) {
        tokens = 1;
    }
        return tokens;
    }

    public static int countAnyContent(any content) {
        if content == null {
        return 0;
    }
        var switch c = content.(type) {
        case String:;
        return len(c);
        case []ContentBlock:;
        var total = 0;
        var for _, block = range c {
        total += countContentBlock(block);
    }
        return total;
        case []any:;
        var total = 0;
        var for _, item = range c {
        var data, err = json.Marshal(item);
        if err != null {
        continue;
    }
        var block ContentBlock;
        var if err = json.Unmarshal(data, &block); err == null {
        total += countContentBlock(block);
    }
    }
        return total;
        default:;
        var if data, err = json.Marshal(content); err == null {
        return len(data);
    }
        return 0;
    }
    }

    public static int countContentBlock(ContentBlock block) {
        var total = 0;
        if block.Text != null {
        total += len(*block.Text);
    }
        if block.Thinking != null {
        total += len(*block.Thinking);
    }
        if block.Type == "tool_use" || block.Type == "tool_result" {
        var if data, err = json.Marshal(block); err == null {
        total += len(data);
    }
    }
        return total;
    }

    public static class OllamaWebSearchRequest {
        public String Query;
        public int MaxResults;
    }

    public static class OllamaWebSearchResult {
        public String Title;
        public String URL;
        public String Content;
    }

    public static class OllamaWebSearchResponse {
        public []OllamaWebSearchResult Results;
    }
        var WebSearchEndpoint = "https://ollama.com/api/web_search";

    public static void WebSearch(context.Context ctx, String query) {
        if internalcloud.Disabled() {
        logutil.TraceContext(ctx, "anthropic: web search blocked", "reason", "cloud_disabled");
        return null, errors.New(internalcloud.DisabledError("web search is unavailable"));
    }
        if maxResults <= 0 {
        maxResults = 5;
    }
        if maxResults > 10 {
        maxResults = 10;
    }
        var reqBody = OllamaWebSearchRequest{
        Query:      query,;
        MaxResults: maxResults,;
    }
        var body, err = json.Marshal(reqBody);
        if err != null {
        return null, fmt.Errorf("failed to marshal web search request: %w", err);
    }
        var searchURL, err = url.Parse(WebSearchEndpoint);
        if err != null {
        return null, fmt.Errorf("failed to parse web search URL: %w", err);
    }
        logutil.TraceContext(ctx, "anthropic: web search request",;
        "query", TraceTruncateString(query),;
        "max_results", maxResults,;
        "url", searchURL.String(),;
        );
        var q = searchURL.Query();
        q.Set("ts", strconv.FormatInt(time.Now().Unix(), 10));
        searchURL.RawQuery = q.Encode();
        var signature = "";
        if strings.EqualFold(searchURL.Hostname(), "ollama.com") {
        var challenge = fmt.Sprintf("%s,%s", http.MethodPost, searchURL.RequestURI());
        signature, err = auth.Sign(ctx, []byte(challenge));
        if err != null {
        return null, fmt.Errorf("failed to sign web search request: %w", err);
    }
    }
        logutil.TraceContext(ctx, "anthropic: web search auth", "signed", signature != "");
        var req, err = http.NewRequestWithContext(ctx, "POST", searchURL.String(), bytes.NewReader(body));
        if err != null {
        return null, fmt.Errorf("failed to create web search request: %w", err);
    }
        req.Header.Set("Content-Type", "application/json");
        if signature != "" {
        req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", signature));
    }
        var resp, err = http.DefaultClient.Do(req);
        if err != null {
        return null, fmt.Errorf("web search request failed: %w", err);
    }
        defer resp.Body.Close();
        logutil.TraceContext(ctx, "anthropic: web search response", "status", resp.StatusCode);
        if resp.StatusCode != http.StatusOK {
        var respBody, _ = io.ReadAll(resp.Body);
        return null, fmt.Errorf("web search returned status %d: %s", resp.StatusCode, String(respBody));
    }
        var searchResp OllamaWebSearchResponse;
        var if err = json.NewDecoder(resp.Body).Decode(&searchResp); err != null {
        return null, fmt.Errorf("failed to decode web search response: %w", err);
    }
        logutil.TraceContext(ctx, "anthropic: web search results", "count", len(searchResp.Results));
        return &searchResp, null;
    }
        func ConvertOllamaToAnthropicResults(ollamaResults *OllamaWebSearchResponse) []WebSearchResult {
        var results []WebSearchResult;
        var for _, r = range ollamaResults.Results {
        results = append(results, WebSearchResult{
        Type:  "web_search_result",;
        URL:   r.URL,;
        Title: r.Title,;
        });
    }
        return results;
    }
}
