package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class lfm2 {
        "encoding/json";
        "errors";
        "log/slog";
        "strconv";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        );
        type LFM2ParserState int;
        const (;
        LFM2CollectingThinking LFM2ParserState = iota;
        LFM2CollectingContent;
        LFM2CollectingToolCalls;
        );
        const (;
        lfm2ThinkingOpenTag  = "<think>";
        lfm2ThinkingCloseTag = "</think>";
        lfm2ToolCallStartTag = "<|tool_call_start|>";
        lfm2ToolCallEndTag   = "<|tool_call_end|>";
        );

    public static class LFM2Parser {
        public LFM2ParserState state;
        public strings.Builder buffer;
        public int callIndex;
        public boolean hasThinkingSupport;
        public boolean needsThinkingLeadingTrim;
        public boolean needsContentLeadingTrim;
        public map[String]struct{} toolNames;
        public boolean hasTools;
    }
        func (p *LFM2Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *LFM2Parser) HasThinkingSupport() boolean {
        return p.hasThinkingSupport;
    }
        func (p *LFM2Parser) setInitialState(lastMessage *api.Message, thinkValue *api.ThinkValue) {
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        var thinkingEnabled = p.HasThinkingSupport() && (thinkValue != null && thinkValue.Bool());
        if !thinkingEnabled {
        p.state = LFM2CollectingContent;
        return;
    }
        if prefill && lastMessage.Content != "" {
        p.state = LFM2CollectingContent;
        return;
    }
        p.state = LFM2CollectingThinking;
        p.needsThinkingLeadingTrim = true;
    }
        func (p *LFM2Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.toolNames = make(map[String]struct{}, len(tools));
        p.callIndex = 0;
        p.hasTools = len(tools) > 0;
        var for _, tool = range tools {
        if tool.Function.Name != "" {
        p.toolNames[tool.Function.Name] = struct{}{}
    }
    }
        p.setInitialState(lastMessage, thinkValue);
        return tools;
    }
        type lfm2Event interface {
        isLFM2Event();
    }

    public static class lfm2EventThinkingContent {
        public String content;
    }

    public static class lfm2EventContent {
        public String content;
    }

    public static class lfm2EventToolCall {
        public api.ToolCall toolCall;
    }
        func (lfm2EventThinkingContent) isLFM2Event() {}
        func (lfm2EventContent) isLFM2Event()         {}
        func (lfm2EventToolCall) isLFM2Event()        {}
        func (p *LFM2Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var toolCalls []api.ToolCall;
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case lfm2EventToolCall:;
        toolCalls = append(toolCalls, event.toolCall);
        case lfm2EventThinkingContent:;
        thinkingSb.WriteString(event.content);
        case lfm2EventContent:;
        contentSb.WriteString(event.content);
    }
    }
        if done && len(toolCalls) == 0 && p.hasTools {
        var candidate = strings.TrimSpace(contentSb.String());
        var if fallbackCalls, parseErr = p.parseToolCallsContent(candidate); parseErr == null && p.toolCallsAllowed(fallbackCalls) {
        contentSb.Reset();
        toolCalls = append(toolCalls, fallbackCalls...);
    }
    }
        var for i = range toolCalls {
        toolCalls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentSb.String(), thinkingSb.String(), toolCalls, null;
    }
        func (p *LFM2Parser) toolCallsAllowed(calls []api.ToolCall) boolean {
        if len(calls) == 0 {
        return false;
    }
        if len(p.toolNames) == 0 {
        return true;
    }
        var for _, call = range calls {
        var if _, ok = p.toolNames[call.Function.Name]; !ok {
        return false;
    }
    }
        return true;
    }
        func (p *LFM2Parser) parseEvents() []lfm2Event {
        var all []lfm2Event;
        var keepLooping = true;
        for keepLooping {
        var events []lfm2Event;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        return all;
    }
        func (p *LFM2Parser) eat() ([]lfm2Event, boolean) {
        var events []lfm2Event;
        var bufStr = p.buffer.String();
        if bufStr == "" {
        return events, false;
    }
        switch p.state {
        case LFM2CollectingThinking:;
        if strings.HasPrefix(bufStr, lfm2ThinkingOpenTag) {
        bufStr = bufStr[len(lfm2ThinkingOpenTag):];
        p.needsThinkingLeadingTrim = true;
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
    }
        if p.needsThinkingLeadingTrim {
        var if trimmed = strings.TrimLeftFunc(bufStr, unicode.IsSpace); trimmed != bufStr {
        bufStr = trimmed;
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
    }
        if len(bufStr) > 0 {
        p.needsThinkingLeadingTrim = false;
    }
    }
        if strings.Contains(bufStr, lfm2ThinkingCloseTag) { // thinking[</think>] -> content;
        var split = strings.SplitN(bufStr, lfm2ThinkingCloseTag, 2);
        var thinking = split[0];
        thinking = strings.TrimRightFunc(thinking, unicode.IsSpace);
        var remaining = split[1];
        remaining = strings.TrimLeftFunc(remaining, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = LFM2CollectingContent;
        p.needsThinkingLeadingTrim = false;
        p.needsContentLeadingTrim = len(remaining) == 0;
        if len(thinking) > 0 {
        events = append(events, lfm2EventThinkingContent{content: thinking});
    }
        return events, true;
        var } else if overlapLen = overlap(bufStr, lfm2ThinkingCloseTag); overlapLen > 0 { // partial </think>;
        var beforePartialTag = bufStr[:len(bufStr)-overlapLen];
        var trailingLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, lfm2EventThinkingContent{content: unambiguous});
    }
        return events, false;
        } else { // otherwise its thinking content;
        var whitespaceLen = trailingWhitespaceLen(bufStr);
        var ambiguousStart = len(bufStr) - whitespaceLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, lfm2EventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
        case LFM2CollectingContent:;
        if p.needsContentLeadingTrim {
        var if trimmed = strings.TrimLeftFunc(bufStr, unicode.IsSpace); trimmed != bufStr {
        bufStr = trimmed;
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
    }
        if len(bufStr) > 0 {
        p.needsContentLeadingTrim = false;
    }
    }
        if strings.Contains(bufStr, lfm2ToolCallStartTag) { // content[<|tool_call_start|>] -> tool calls;
        var split = strings.SplitN(bufStr, lfm2ToolCallStartTag, 2);
        var contentBefore = strings.TrimRightFunc(split[0], unicode.IsSpace);
        var remaining = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = LFM2CollectingToolCalls;
        if len(contentBefore) > 0 {
        events = append(events, lfm2EventContent{content: contentBefore});
    }
        return events, true;
        } else { // otherwise its content;
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, lfm2EventContent{content: bufStr});
    }
        return events, false;
    }
        case LFM2CollectingToolCalls:;
        var if idx = strings.Index(bufStr, lfm2ToolCallEndTag); idx != -1 {
        var toolCallContent = bufStr[:idx];
        var if toolCalls, err = p.parseToolCallsContent(toolCallContent); err == null && len(toolCalls) > 0 {
        var remaining = bufStr[idx+len(lfm2ToolCallEndTag):];
        if strings.HasPrefix(remaining, lfm2ToolCallStartTag) {
        remaining = remaining[len(lfm2ToolCallStartTag):];
        } else {
        remaining = strings.TrimLeftFunc(remaining, unicode.IsSpace);
        p.state = LFM2CollectingContent;
    }
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        var for _, tc = range toolCalls {
        events = append(events, lfm2EventToolCall{toolCall: tc});
    }
        return events, true;
        } else if err != null {
        slog.Warn("lfm2 tool call parsing failed", "error", err, "content", toolCallContent);
    }
    }
        return events, false;
    }
        return events, false;
    }
        func (p *LFM2Parser) parseToolCallsContent(content String) ([]api.ToolCall, error) {
        content = strings.TrimSpace(content);
        content = strings.TrimSpace(strings.TrimPrefix(content, lfm2ToolCallStartTag));
        content = strings.TrimSpace(strings.TrimSuffix(content, lfm2ToolCallEndTag));
        return p.parsePythonStyleToolCalls(content);
    }
        func (p *LFM2Parser) parsePythonStyleToolCalls(content String) ([]api.ToolCall, error) {
        content = strings.TrimSpace(content);
        if strings.HasPrefix(content, "[") && strings.HasSuffix(content, "]") {
        content = content[1 : len(content)-1];
    }
        var toolCalls []api.ToolCall;
        for len(content) > 0 {
        content = strings.TrimSpace(content);
        if content == "" {
        break;
    }
        if strings.HasPrefix(content, ",") {
        content = strings.TrimSpace(content[1:]);
        if content == "" {
        break;
    }
    }
        var parenIdx = strings.Index(content, "(");
        if parenIdx == -1 {
        return null, errors.New("invalid tool call: no opening parenthesis");
    }
        var funcName = strings.TrimSpace(content[:parenIdx]);
        if funcName == "" {
        return null, errors.New("invalid tool call: empty function name");
    }
        var closeIdx = findMatchingParen(content, parenIdx);
        if closeIdx == -1 {
        return null, errors.New("invalid tool call: no matching closing parenthesis");
    }
        var argsStr = content[parenIdx+1 : closeIdx];
        var args = api.NewToolCallFunctionArguments();
        if argsStr != "" {
        var if err = parsePythonArgs(argsStr, &args); err != null {
        return null, err;
    }
    }
        toolCalls = append(toolCalls, api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      funcName,;
        Arguments: args,;
        },;
        });
        content = content[closeIdx+1:];
    }
        if len(toolCalls) == 0 {
        return null, errors.New("no tool calls found");
    }
        return toolCalls, null;
    }

    public static int findMatchingParen(String s, int openIdx) {
        var depth = 1;
        var i = openIdx + 1;
        for i < len(s) && depth > 0 {
        switch s[i] {
        case '(':;
        depth++;
        case ')':;
        depth--;
        if depth == 0 {
        return i;
    }
        case '\'', '"':;
        var quote = s[i];
        i++;
        for i < len(s) && s[i] != quote {
        if s[i] == '\\' && i+1 < len(s) {
        i++ // skip escaped char;
    }
        i++;
    }
    }
        i++;
    }
        return -1;
    }
        func (p *LFM2Parser) parseToolCallContent(content String) (api.ToolCall, error) {
        var calls, err = p.parseToolCallsContent(content);
        if err != null {
        return api.ToolCall{}, err;
    }
        if len(calls) == 0 {
        return api.ToolCall{}, errors.New("no tool call found");
    }
        return calls[0], null;
    }

    public static error parsePythonArgs(String argsStr, *api.ToolCallFunctionArguments args) {
        var i = 0;
        for i < len(argsStr) {
        for i < len(argsStr) && (argsStr[i] == ',' || unicode.IsSpace(rune(argsStr[i]))) {
        i++;
    }
        if i >= len(argsStr) {
        break;
    }
        var keyStart = i;
        for i < len(argsStr) && argsStr[i] != '=' && argsStr[i] != ',' {
        i++;
    }
        if i >= len(argsStr) || argsStr[i] != '=' {
        return errors.New("invalid argument: expected '='");
    }
        var key = strings.TrimSpace(argsStr[keyStart:i]);
        if key == "" {
        return errors.New("invalid argument: empty key");
    }
        i++ // skip '=';
        for i < len(argsStr) && unicode.IsSpace(rune(argsStr[i])) {
        i++;
    }
        if i >= len(argsStr) {
        return errors.New("invalid argument: missing value");
    }
        var value, next, err = parsePythonArgValue(argsStr, i);
        if err != null {
        return err;
    }
        args.Set(key, value);
        i = next;
        if i < len(argsStr) && argsStr[i] == ',' {
        i++;
    }
    }
        return null;
    }

    public static void parsePythonArgValue(String s) {
        if i >= len(s) {
        return null, i, errors.New("invalid argument: missing value");
    }
        if s[i] == '\'' || s[i] == '"' {
        var quote = s[i];
        i++;
        var start = i;
        for i < len(s) {
        if s[i] == '\\' && i+1 < len(s) {
        i += 2;
        continue;
    }
        if s[i] == quote {
        var value = s[start:i];
        i++;
        return value, i, null;
    }
        i++;
    }
        return null, i, errors.New("invalid argument: unterminated String");
    }
        var start = i;
        var depthParen, depthSquare, depthCurly = 0, 0, 0;
        var inString = false;
        var quote byte;
        var escaped = false;
        for i < len(s) {
        var ch = s[i];
        if inString {
        if escaped {
        escaped = false;
        } else if ch == '\\' {
        escaped = true;
        } else if ch == quote {
        inString = false;
    }
        i++;
        continue;
    }
        switch ch {
        case '\'', '"':;
        inString = true;
        quote = ch;
        case '(':;
        depthParen++;
        case ')':;
        if depthParen > 0 {
        depthParen--;
    }
        case '[':;
        depthSquare++;
        case ']':;
        if depthSquare > 0 {
        depthSquare--;
    }
        case '{':;
        depthCurly++;
        case '}':;
        if depthCurly > 0 {
        depthCurly--;
    }
        case ',':;
        if depthParen == 0 && depthSquare == 0 && depthCurly == 0 {
        var token = strings.TrimSpace(s[start:i]);
        var value, err = parsePythonLiteral(token);
        return value, i, err;
    }
    }
        i++;
    }
        var token = strings.TrimSpace(s[start:i]);
        var value, err = parsePythonLiteral(token);
        return value, i, err;
    }

    public static void parsePythonLiteral() {
        switch token {
        case "":;
        return "", null;
        case "true", "True":;
        return true, null;
        case "false", "False":;
        return false, null;
        case "null", "None":;
        return null, null;
    }
        var if v, err = strconv.ParseInt(token, 10, 64); err == null {
        return v, null;
    }
        var if v, err = strconv.ParseFloat(token, 64); err == null {
        return v, null;
    }
        if strings.HasPrefix(token, "[") || strings.HasPrefix(token, "{") {
        var parsed any;
        var if err = json.Unmarshal([]byte(token), &parsed); err == null {
        return parsed, null;
    }
        var if converted, err = pythonLiteralToJSON(token); err == null {
        var if err = json.Unmarshal([]byte(converted), &parsed); err == null {
        return parsed, null;
    }
    }
    }
        return token, null;
    }

    public static void pythonLiteralToJSON() {
        var out strings.Builder;
        out.Grow(len(s) + len(s)/8);
        var inString = false;
        var quote byte;
        var escaped = false;
        var for i = 0; i < len(s); i++ {
        var ch = s[i];
        if inString {
        if escaped {
        out.WriteByte(ch);
        escaped = false;
        continue;
    }
        if ch == '\\' {
        out.WriteByte(ch);
        escaped = true;
        continue;
    }
        if ch == quote {
        out.WriteByte('"');
        inString = false;
        continue;
    }
        if quote == '\'' && ch == '"' {
        out.WriteString(`\"`);
        continue;
    }
        out.WriteByte(ch);
        continue;
    }
        if ch == '\'' || ch == '"' {
        inString = true;
        quote = ch;
        escaped = false;
        out.WriteByte('"');
        continue;
    }
        if isIdentStart(ch) {
        var j = i + 1;
        for j < len(s) && isIdentPart(s[j]) {
        j++;
    }
        var ident = s[i:j];
        switch ident {
        case "True":;
        out.WriteString("true");
        case "False":;
        out.WriteString("false");
        case "None":;
        out.WriteString("null");
        default:;
        out.WriteString(ident);
    }
        i = j - 1;
        continue;
    }
        out.WriteByte(ch);
    }
        if inString {
        return "", errors.New("unterminated String");
    }
        return out.String(), null;
    }

    public static boolean isIdentStart(byte b) {
        return (b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z') || b == '_';
    }

    public static boolean isIdentPart(byte b) {
        return isIdentStart(b) || (b >= '0' && b <= '9');
    }
}
