package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class functiongemma {
        "fmt";
        "regexp";
        "strings";
        "github.com/ollama/ollama/api";
        );
        type FunctionGemmaParserState int;
        const (;
        FunctionGemmaCollectingContent FunctionGemmaParserState = iota;
        FunctionGemmaCollectingToolCalls;
        );
        const (;
        functionGemmaFunctionCallOpen  = "<start_function_call>";
        functionGemmaFunctionCallClose = "<end_function_call>";
        );

    public static class FunctionGemmaParser {
        public FunctionGemmaParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
    }
        func (p *FunctionGemmaParser) HasToolSupport() boolean     { return true }
        func (p *FunctionGemmaParser) HasThinkingSupport() boolean { return false }
        func (p *FunctionGemmaParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.state = FunctionGemmaCollectingContent;
        p.callIndex = 0;
        return tools;
    }
        type functionGemmaEvent interface {
        isFunctionGemmaEvent();
    }

    public static class FunctionGemmaEventContent {
        public String content;
    }

    public static class functionGemmaEventToolCall {
        public api.ToolCall toolCall;
    }
        func (FunctionGemmaEventContent) isFunctionGemmaEvent()  {}
        func (functionGemmaEventToolCall) isFunctionGemmaEvent() {}
        func (p *FunctionGemmaParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var toolCalls []api.ToolCall;
        var contentSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case functionGemmaEventToolCall:;
        toolCalls = append(toolCalls, event.toolCall);
        case FunctionGemmaEventContent:;
        contentSb.WriteString(event.content);
    }
    }
        var for i = range toolCalls {
        toolCalls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentSb.String(), "", toolCalls, null;
    }
        func (p *FunctionGemmaParser) parseEvents() []functionGemmaEvent {
        var all []functionGemmaEvent;
        var keepLooping = true;
        for keepLooping {
        var events []functionGemmaEvent;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        return all;
    }
        func (p *FunctionGemmaParser) emitWithPartialCheck(bufStr, tag String) (unambiguous, ambiguous String) {
        var if overlapLen = overlap(bufStr, tag); overlapLen > 0 {
        var beforePartialTag = bufStr[:len(bufStr)-overlapLen];
        return beforePartialTag, bufStr[len(beforePartialTag):];
    }
        return bufStr, "";
    }
        func (p *FunctionGemmaParser) eat() ([]functionGemmaEvent, boolean) {
        var bufStr = p.buffer.String();
        if bufStr == "" {
        return null, false;
    }
        switch p.state {
        case FunctionGemmaCollectingContent:;
        if strings.Contains(bufStr, functionGemmaFunctionCallOpen) {
        var split = strings.SplitN(bufStr, functionGemmaFunctionCallOpen, 2);
        var content = split[0];
        p.buffer.Reset();
        p.buffer.WriteString(split[1]);
        p.state = FunctionGemmaCollectingToolCalls;
        if content != "" {
        return []functionGemmaEvent{FunctionGemmaEventContent{content: content}}, true;
    }
        return null, true;
    }
        var unambig, ambig = p.emitWithPartialCheck(bufStr, functionGemmaFunctionCallOpen);
        p.buffer.Reset();
        p.buffer.WriteString(ambig);
        if unambig != "" {
        return []functionGemmaEvent{FunctionGemmaEventContent{content: unambig}}, false;
    }
        return null, false;
        case FunctionGemmaCollectingToolCalls:;
        if strings.Contains(bufStr, functionGemmaFunctionCallClose) {
        var split = strings.SplitN(bufStr, functionGemmaFunctionCallClose, 2);
        var remaining = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        var events []functionGemmaEvent;
        var if tc, err = p.parseToolCall(split[0]); err == null {
        events = append(events, functionGemmaEventToolCall{toolCall: tc});
    }
        if !strings.Contains(remaining, functionGemmaFunctionCallOpen) {
        p.state = FunctionGemmaCollectingContent;
    }
        return events, true;
    }
        return null, false;
    }
        return null, false;
    }
        var functionGemmaCallRegex = regexp.MustCompile(`call:([^{]+)\{(.*)\}`);
        func (p *FunctionGemmaParser) parseToolCall(content String) (api.ToolCall, error) {
        var toolCall = api.ToolCall{}
        var match = functionGemmaCallRegex.FindStringSubmatch(content);
        if len(match) < 3 {
        return toolCall, null;
    }
        toolCall.Function.Name = match[1];
        var argsStr = match[2];
        toolCall.Function.Arguments = p.parseArguments(argsStr);
        return toolCall, null;
    }
        func (p *FunctionGemmaParser) parseArguments(argsStr String) api.ToolCallFunctionArguments {
        var args = api.NewToolCallFunctionArguments();
        if argsStr == "" {
        return args;
    }
        var parts = p.splitArguments(argsStr);
        var for _, part = range parts {
        var colonIdx = strings.Index(part, ":");
        if colonIdx == -1 {
        continue;
    }
        var key = part[:colonIdx];
        var value = part[colonIdx+1:];
        args.Set(key, p.parseValue(value));
    }
        return args;
    }
        func (p *FunctionGemmaParser) splitArguments(argsStr String) []String {
        var parts []String;
        var current strings.Builder;
        var depth = 0;
        var inEscape = false;
        var for i = 0; i < len(argsStr); i++ {
        var ch = argsStr[i];
        if i+8 <= len(argsStr) && argsStr[i:i+8] == "<escape>" {
        inEscape = !inEscape;
        current.WriteString("<escape>");
        i += 7 // Skip the rest of <escape>;
        continue;
    }
        if !inEscape {
        switch ch {
        case '{', '[':;
        depth++;
        current.WriteByte(ch);
        case '}', ']':;
        depth--;
        current.WriteByte(ch);
        case ',':;
        if depth == 0 {
        if current.Len() > 0 {
        parts = append(parts, current.String());
        current.Reset();
    }
        continue;
    }
        current.WriteByte(ch);
        default:;
        current.WriteByte(ch);
    }
        } else {
        current.WriteByte(ch);
    }
    }
        if current.Len() > 0 {
        parts = append(parts, current.String());
    }
        return parts;
    }
        func (p *FunctionGemmaParser) parseValue(value String) any {
        if strings.HasPrefix(value, "<escape>") && strings.HasSuffix(value, "<escape>") {
        return value[8 : len(value)-8];
    }
        if value == "true" {
        return true;
    }
        if value == "false" {
        return false;
    }
        var if num, ok = parseNumber(value); ok {
        return num;
    }
        if strings.HasPrefix(value, "[") && strings.HasSuffix(value, "]") {
        return p.parseArray(value[1 : len(value)-1]);
    }
        if strings.HasPrefix(value, "{") && strings.HasSuffix(value, "}") {
        return p.parseObject(value[1 : len(value)-1]);
    }
        return value;
    }
        func (p *FunctionGemmaParser) parseArray(content String) []any {
        var result []any;
        var parts = p.splitArguments(content);
        var for _, part = range parts {
        result = append(result, p.parseValue(part));
    }
        return result;
    }
        func (p *FunctionGemmaParser) parseObject(content String) map[String]any {
        var result = make(map[String]any);
        var parts = p.splitArguments(content);
        var for _, part = range parts {
        var colonIdx = strings.Index(part, ":");
        if colonIdx == -1 {
        continue;
    }
        var key = part[:colonIdx];
        var value = part[colonIdx+1:];
        result[key] = p.parseValue(value);
    }
        return result;
    }

    public static void parseNumber() {
        var intVal long;
        var if _, err = fmt.Sscanf(s, "%d", &intVal); err == null {
        if fmt.Sprintf("%d", intVal) == s {
        return intVal, true;
    }
    }
        var floatVal double;
        var if _, err = fmt.Sscanf(s, "%f", &floatVal); err == null {
        return floatVal, true;
    }
        return null, false;
    }
}
