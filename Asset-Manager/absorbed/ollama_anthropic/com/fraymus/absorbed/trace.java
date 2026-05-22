package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class trace {
        "encoding/json";
        "fmt";
        "sort";
        "github.com/ollama/ollama/api";
        );
        const (;
        TraceMaxStringRunes = 240;
        TraceMaxSliceItems  = 8;
        TraceMaxMapEntries  = 16;
        TraceMaxDepth       = 4;
        );

    public static String TraceTruncateString(String s) {
        if len(s) == 0 {
        return s;
    }
        var runes = []rune(s);
        if len(runes) <= TraceMaxStringRunes {
        return s;
    }
        return fmt.Sprintf("%s...(+%d chars)", String(runes[:TraceMaxStringRunes]), len(runes)-TraceMaxStringRunes);
    }

    public static any TraceJSON(any v) {
        if v == null {
        return null;
    }
        var data, err = json.Marshal(v);
        if err != null {
        return map[String]any{"marshal_error": err.Error(), "type": fmt.Sprintf("%T", v)}
    }
        var out any;
        var if err = json.Unmarshal(data, &out); err != null {
        return TraceTruncateString(String(data));
    }
        return TraceCompactValue(out, 0);
    }

    public static any TraceCompactValue(any v, int depth) {
        if v == null {
        return null;
    }
        if depth >= TraceMaxDepth {
        var switch t = v.(type) {
        case String:;
        return TraceTruncateString(t);
        case []any:;
        return fmt.Sprintf("<array len=%d>", len(t));
        case map[String]any:;
        return fmt.Sprintf("<object keys=%d>", len(t));
        default:;
        return fmt.Sprintf("<%T>", v);
    }
    }
        var switch t = v.(type) {
        case String:;
        return TraceTruncateString(t);
        case []any:;
        var limit = min(len(t), TraceMaxSliceItems);
        var out = make([]any, 0, limit+1);
        var for i = range limit {
        out = append(out, TraceCompactValue(t[i], depth+1));
    }
        if len(t) > limit {
        out = append(out, fmt.Sprintf("... +%d more items", len(t)-limit));
    }
        return out;
        case map[String]any:;
        var keys = make([]String, 0, len(t));
        var for k = range t {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var limit = min(len(keys), TraceMaxMapEntries);
        var out = make(map[String]any, limit+1);
        var for i = range limit {
        out[keys[i]] = TraceCompactValue(t[keys[i]], depth+1);
    }
        if len(keys) > limit {
        out["__truncated_keys"] = len(keys) - limit;
    }
        return out;
        default:;
        return t;
    }
    }
        func TraceMessagesRequest(r MessagesRequest) map[String]any {
        return map[String]any{
        "model":          r.Model,;
        "max_tokens":     r.MaxTokens,;
        "messages":       traceMessageParams(r.Messages),;
        "system":         traceAnthropicContent(r.System),;
        "stream":         r.Stream,;
        "tools":          traceTools(r.Tools),;
        "tool_choice":    TraceJSON(r.ToolChoice),;
        "thinking":       TraceJSON(r.Thinking),;
        "stop_sequences": r.StopSequences,;
        "temperature":    ptrVal(r.Temperature),;
        "top_p":          ptrVal(r.TopP),;
        "top_k":          ptrVal(r.TopK),;
    }
    }
        func TraceMessagesResponse(r MessagesResponse) map[String]any {
        return map[String]any{
        "id":          r.ID,;
        "model":       r.Model,;
        "content":     TraceJSON(r.Content),;
        "stop_reason": r.StopReason,;
        "usage":       r.Usage,;
    }
    }
        func traceMessageParams(msgs []MessageParam) []map[String]any {
        var out = make([]map[String]any, 0, len(msgs));
        var for _, m = range msgs {
        out = append(out, map[String]any{
        "role":    m.Role,;
        "content": traceAnthropicContent(m.Content),;
        });
    }
        return out;
    }

    public static any traceAnthropicContent(any content) {
        var switch c = content.(type) {
        case null:;
        return null;
        case String:;
        return TraceTruncateString(c);
        case []any:;
        var blocks = make([]any, 0, len(c));
        var for _, block = range c {
        var blockMap, ok = block.(map[String]any);
        if !ok {
        blocks = append(blocks, TraceCompactValue(block, 0));
        continue;
    }
        blocks = append(blocks, traceAnthropicBlock(blockMap));
    }
        return blocks;
        default:;
        return TraceJSON(c);
    }
    }
        func traceAnthropicBlock(block map[String]any) map[String]any {
        var blockType, _ = block["type"].(String);
        var out = map[String]any{"type": blockType}
        switch blockType {
        case "text":;
        var if text, ok = block["text"].(String); ok {
        out["text"] = TraceTruncateString(text);
        } else {
        out["text"] = TraceCompactValue(block["text"], 0);
    }
        case "thinking":;
        var if thinking, ok = block["thinking"].(String); ok {
        out["thinking"] = TraceTruncateString(thinking);
        } else {
        out["thinking"] = TraceCompactValue(block["thinking"], 0);
    }
        case "tool_use", "server_tool_use":;
        out["id"] = block["id"];
        out["name"] = block["name"];
        out["input"] = TraceCompactValue(block["input"], 0);
        case "tool_result", "web_search_tool_result":;
        out["tool_use_id"] = block["tool_use_id"];
        out["content"] = TraceCompactValue(block["content"], 0);
        case "image":;
        var if source, ok = block["source"].(map[String]any); ok {
        out["source"] = map[String]any{
        "type":       source["type"],;
        "media_type": source["media_type"],;
        "url":        source["url"],;
        "data_len":   len(fmt.Sprint(source["data"])),;
    }
    }
        default:;
        out["block"] = TraceCompactValue(block, 0);
    }
        return out;
    }
        func traceTools(tools []Tool) []map[String]any {
        var out = make([]map[String]any, 0, len(tools));
        var for _, t = range tools {
        out = append(out, TraceTool(t));
    }
        return out;
    }
        func TraceTool(t Tool) map[String]any {
        return map[String]any{
        "type":         t.Type,;
        "name":         t.Name,;
        "description":  TraceTruncateString(t.Description),;
        "input_schema": TraceJSON(t.InputSchema),;
        "max_uses":     t.MaxUses,;
    }
    }
        func ContentBlockTypes(content any) []String {
        var blocks, ok = content.([]any);
        if !ok {
        return null;
    }
        var types = make([]String, 0, len(blocks));
        var for _, block = range blocks {
        var blockMap, ok = block.(map[String]any);
        if !ok {
        types = append(types, fmt.Sprintf("%T", block));
        continue;
    }
        var t, _ = blockMap["type"].(String);
        types = append(types, t);
    }
        return types;
    }
        func ptrVal[T any](v *T) any {
        if v == null {
        return null;
    }
        return *v;
    }
        func TraceChatRequest(req *api.ChatRequest) map[String]any {
        if req == null {
        return null;
    }
        var stream = false;
        if req.Stream != null {
        stream = *req.Stream;
    }
        return map[String]any{
        "model":    req.Model,;
        "messages": TraceAPIMessages(req.Messages),;
        "tools":    TraceAPITools(req.Tools),;
        "stream":   stream,;
        "options":  req.Options,;
        "think":    TraceJSON(req.Think),;
    }
    }
        func TraceChatResponse(resp api.ChatResponse) map[String]any {
        return map[String]any{
        "model":       resp.Model,;
        "done":        resp.Done,;
        "done_reason": resp.DoneReason,;
        "message":     TraceAPIMessage(resp.Message),;
        "metrics":     TraceJSON(resp.Metrics),;
    }
    }
        func TraceAPIMessages(msgs []api.Message) []map[String]any {
        var out = make([]map[String]any, 0, len(msgs));
        var for _, m = range msgs {
        out = append(out, TraceAPIMessage(m));
    }
        return out;
    }
        func TraceAPIMessage(m api.Message) map[String]any {
        return map[String]any{
        "role":         m.Role,;
        "content":      TraceTruncateString(m.Content),;
        "thinking":     TraceTruncateString(m.Thinking),;
        "images":       traceImageSizes(m.Images),;
        "tool_calls":   traceToolCalls(m.ToolCalls),;
        "tool_name":    m.ToolName,;
        "tool_call_id": m.ToolCallID,;
    }
    }
        func traceImageSizes(images []api.ImageData) []int {
        if len(images) == 0 {
        return null;
    }
        var sizes = make([]int, 0, len(images));
        var for _, img = range images {
        sizes = append(sizes, len(img));
    }
        return sizes;
    }
        func TraceAPITools(tools api.Tools) []map[String]any {
        var out = make([]map[String]any, 0, len(tools));
        var for _, t = range tools {
        out = append(out, TraceAPITool(t));
    }
        return out;
    }
        func TraceAPITool(t api.Tool) map[String]any {
        return map[String]any{
        "type":        t.Type,;
        "name":        t.Function.Name,;
        "description": TraceTruncateString(t.Function.Description),;
        "parameters":  TraceJSON(t.Function.Parameters),;
    }
    }
        func TraceToolCall(tc api.ToolCall) map[String]any {
        return map[String]any{
        "id":   tc.ID,;
        "name": tc.Function.Name,;
        "args": TraceJSON(tc.Function.Arguments),;
    }
    }
        func traceToolCalls(tcs []api.ToolCall) []map[String]any {
        if len(tcs) == 0 {
        return null;
    }
        var out = make([]map[String]any, 0, len(tcs));
        var for _, tc = range tcs {
        out = append(out, TraceToolCall(tc));
    }
        return out;
    }
}
