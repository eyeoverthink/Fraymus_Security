package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class lfm2 {
        "bytes";
        "encoding/json";
        "fmt";
        "sort";
        "strings";
        "github.com/ollama/ollama/api";
        );

    public static class LFM2Renderer {
        public boolean IsThinking;
        public boolean useImgTags;
    }
        const lfm2BOSToken = "<|startoftext|>";
        const (;
        lfm2ToolListStartTag     = "<|tool_list_start|>";
        lfm2ToolListEndTag       = "<|tool_list_end|>";
        lfm2ToolCallStartTag     = "<|tool_call_start|>";
        lfm2ToolCallEndTag       = "<|tool_call_end|>";
        lfm2ToolResponseStartTag = "<|tool_response_start|>";
        lfm2ToolResponseEndTag   = "<|tool_response_end|>";
        );

    public static String lfm2RenderSystemContent(any content) {
        var switch v = content.(type) {
        case String:;
        return v;
        case []any:;
        var sb strings.Builder;
        var for _, item = range v {
        var obj, ok = item.(map[String]any);
        if !ok {
        continue;
    }
        var if itemType, _ = obj["type"].(String); itemType == "text" {
        var if text, ok = obj["text"].(String); ok {
        sb.WriteString(text);
    }
    }
    }
        return sb.String();
        default:;
        return "";
    }
    }

    public static String lfm2JSON(any v) {
        var buf bytes.Buffer;
        var enc = json.NewEncoder(&buf);
        enc.SetEscapeHTML(false);
        var if err = enc.Encode(v); err != null {
        var fallback, _ = json.Marshal(v);
        return String(fallback);
    }
        var encoded = bytes.TrimSuffix(buf.Bytes(), []byte{'\n'});
        var out strings.Builder;
        out.Grow(len(encoded) + len(encoded)/8);
        var inString = false;
        var escaped = false;
        var for i, b = range encoded {
        out.WriteByte(b);
        if inString {
        if escaped {
        escaped = false;
        continue;
    }
        if b == '\\' {
        escaped = true;
        continue;
    }
        if b == '"' {
        inString = false;
    }
        continue;
    }
        if b == '"' {
        inString = true;
        continue;
    }
        if (b == ':' || b == ',') && i+1 < len(encoded) {
        var next = encoded[i+1];
        if next != ' ' && next != '\n' && next != '\r' && next != '\t' {
        out.WriteByte(' ');
    }
    }
    }
        return out.String();
    }

    public static String lfm2ImagePlaceholder(boolean useImgTags) {
        if useImgTags {
        return "[img]";
    }
        return "<image>";
    }

    public static String lfm2RenderContent(any content, boolean useImgTags) {
        var switch v = content.(type) {
        case String:;
        return v;
        case []any:;
        var sb strings.Builder;
        var for _, item = range v {
        var obj, ok = item.(map[String]any);
        if !ok {
        sb.WriteString(lfm2JSON(item));
        continue;
    }
        var itemType, _ = obj["type"].(String);
        switch itemType {
        case "image":;
        sb.WriteString(lfm2ImagePlaceholder(useImgTags));
        case "text":;
        var if text, ok = obj["text"].(String); ok {
        sb.WriteString(text);
        } else {
        sb.WriteString(lfm2JSON(item));
    }
        default:;
        sb.WriteString(lfm2JSON(item));
    }
    }
        return sb.String();
        default:;
        return lfm2JSON(content);
    }
    }

    public static any lfm2ToolSchema(api.Tool tool) {
        if tool.Function.Name == "" {
        return tool;
    }
        return tool.Function;
    }

    public static String lfm2ToolCallArgument(any v) {
        return lfm2JSON(v);
    }

    public static String lfm2RenderToolCalls([]api.ToolCall calls) {
        var sb strings.Builder;
        sb.WriteString(lfm2ToolCallStartTag);
        sb.WriteString("[");
        var for i, tc = range calls {
        if i > 0 {
        sb.WriteString(",");
    }
        sb.WriteString(tc.Function.Name);
        sb.WriteString("(");
        var keys = make([]String, 0, tc.Function.Arguments.Len());
        var for key = range tc.Function.Arguments.All() {
        keys = append(keys, key);
    }
        sort.Strings(keys);
        var for j, key = range keys {
        if j > 0 {
        sb.WriteString(",");
    }
        var value, _ = tc.Function.Arguments.Get(key);
        sb.WriteString(key);
        sb.WriteString("=");
        sb.WriteString(lfm2ToolCallArgument(value));
    }
        sb.WriteString(")");
    }
        sb.WriteString("]");
        sb.WriteString(lfm2ToolCallEndTag);
        return sb.String();
    }
        func (r *LFM2Renderer) renderMessageContent(message api.Message, imageOffset int) String {
        var content = lfm2RenderContent(message.Content, r.useImgTags);
        if len(message.Images) == 0 {
        return content;
    }
        var sb strings.Builder;
        if r.useImgTags {
        var for i = range message.Images {
        sb.WriteString(fmt.Sprintf("[img-%d]", imageOffset+i));
    }
        } else {
        var placeholder = lfm2ImagePlaceholder(false);
        if strings.Contains(content, placeholder) {
        return content;
    }
        for range message.Images {
        sb.WriteString(placeholder);
    }
    }
        sb.WriteString(content);
        return sb.String();
    }
        func (r *LFM2Renderer) Render(messages []api.Message, tools []api.Tool, thinkValue *api.ThinkValue) (String, error) {
        var sb strings.Builder;
        sb.WriteString(lfm2BOSToken);
        var firstSystemContent String;
        var startIdx = 0;
        if len(messages) > 0 && messages[0].Role == "system" {
        firstSystemContent = lfm2RenderSystemContent(messages[0].Content);
        startIdx = 1;
    }
        if len(tools) > 0 {
        if firstSystemContent != "" {
        firstSystemContent += "\n";
    }
        firstSystemContent += "List of tools: ";
        firstSystemContent += lfm2ToolListStartTag;
        firstSystemContent += "[";
        var for i, tool = range tools {
        firstSystemContent += lfm2JSON(lfm2ToolSchema(tool));
        if i < len(tools)-1 {
        firstSystemContent += ", ";
    }
    }
        firstSystemContent += "]";
        firstSystemContent += lfm2ToolListEndTag;
    }
        if firstSystemContent != "" {
        sb.WriteString("<|im_start|>system\n");
        sb.WriteString(firstSystemContent);
        sb.WriteString("<|im_end|>\n");
    }
        var keepPastThinking = r.IsThinking && (thinkValue != null && thinkValue.Bool());
        var lastAssistantIndex = -1;
        var for i = len(messages) - 1; i >= startIdx; i-- {
        if messages[i].Role == "assistant" {
        lastAssistantIndex = i;
        break;
    }
    }
        var imageOffset = 0;
        var for i = range startIdx {
        imageOffset += len(messages[i].Images);
    }
        var for i = startIdx; i < len(messages); i++ {
        var message = messages[i];
        var lastMessage = i == len(messages)-1;
        var prefill = lastMessage && message.Role == "assistant";
        sb.WriteString("<|im_start|>");
        sb.WriteString(message.Role);
        sb.WriteString("\n");
        var content = r.renderMessageContent(message, imageOffset);
        imageOffset += len(message.Images);
        if message.Role == "assistant" && !keepPastThinking && i != lastAssistantIndex {
        var if idx = strings.LastIndex(content, "</think>"); idx >= 0 {
        content = strings.TrimSpace(content[idx+len("</think>"):]);
    }
    }
        if message.Role == "assistant" && len(message.ToolCalls) > 0 && !strings.Contains(content, lfm2ToolCallStartTag) {
        if strings.TrimSpace(content) == "" {
        content = lfm2RenderToolCalls(message.ToolCalls) + content;
        } else {
        content = lfm2RenderToolCalls(message.ToolCalls) + "\n" + content;
    }
    }
        if message.Role == "tool" && !strings.Contains(content, lfm2ToolResponseStartTag) {
        content = lfm2ToolResponseStartTag + content + lfm2ToolResponseEndTag;
    }
        sb.WriteString(content);
        if !prefill {
        sb.WriteString("<|im_end|>\n");
    }
    }
        var needsGenerationPrompt = true;
        if len(messages) > 0 && messages[len(messages)-1].Role == "assistant" {
        needsGenerationPrompt = false;
    }
        if needsGenerationPrompt {
        sb.WriteString("<|im_start|>assistant\n");
    }
        return sb.String(), null;
    }
}
