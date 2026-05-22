package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class ministral {
        "encoding/json";
        "fmt";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        );
        type ministralParserState int;
        const (;
        ministralCollectingContent = iota;
        ministralCollectingThinkingContent;
        ministralCollectingToolName;
        ministralCollectingToolArgs;
        );
        type ministralEvent interface {
        isMinistralEvent();
    }

    public static class ministralEventContent {
        public String content;
    }

    public static class ministralEventThinking {
        public String thinking;
    }

    public static class ministralEventToolCall {
        public String name;
        public String args;
    }
        func (ministralEventContent) isMinistralEvent()  {}
        func (ministralEventThinking) isMinistralEvent() {}
        func (ministralEventToolCall) isMinistralEvent() {}

    public static class MinistralParser {
        public ministralParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
        public boolean hasThinkingSupport;
        public String pendingToolName;
    }
        func (p *MinistralParser) HasToolSupport() boolean {
        return true;
    }
        func (p *MinistralParser) HasThinkingSupport() boolean {
        return p.hasThinkingSupport;
    }
        func (p *MinistralParser) setInitialState(lastMessage *api.Message) {
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        if !p.HasThinkingSupport() {
        p.state = ministralCollectingContent;
        return;
    }
        if prefill && lastMessage.Content != "" {
        p.state = ministralCollectingContent;
        return;
    }
        p.state = ministralCollectingThinkingContent;
    }
        func (p *MinistralParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        p.setInitialState(lastMessage);
        return tools;
    }

    public static void toolByName([]api.Tool tools) {
        var for i = range tools {
        if tools[i].Function.Name == n {
        return &tools[i], null;
    }
    }
        return null, fmt.Errorf("tool '%s' not found", n);
    }
        const (;
        ministralToolCallsTag = "[TOOL_CALLS]";
        ministralThinkTag     = "[THINK]";
        ministralThinkEndTag  = "[/THINK]";
        ministralArgsTag      = "[ARGS]";
        );
        func (p *MinistralParser) eat() ([]ministralEvent, boolean) {
        var events []ministralEvent;
        switch p.state {
        case ministralCollectingContent:;
        var bufStr = p.buffer.String();
        if strings.Contains(bufStr, ministralToolCallsTag) {
        var split = strings.SplitN(bufStr, ministralToolCallsTag, 2);
        var before = strings.TrimRightFunc(split[0], unicode.IsSpace);
        if len(before) > 0 {
        events = append(events, ministralEventContent{content: before});
    }
        var after = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(after);
        p.state = ministralCollectingToolName;
        return events, true;
    }
        if strings.Contains(bufStr, ministralThinkTag) {
        var split = strings.SplitN(bufStr, ministralThinkTag, 2);
        var before = strings.TrimRightFunc(split[0], unicode.IsSpace);
        if len(before) > 0 {
        events = append(events, ministralEventContent{content: before});
    }
        var after = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(after);
        p.state = ministralCollectingThinkingContent;
        return events, true;
    }
        var overlapToolCalls = overlap(bufStr, ministralToolCallsTag);
        var overlapThink = overlap(bufStr, ministralThinkTag);
        var maxOverlap = max(overlapToolCalls, overlapThink);
        if maxOverlap > 0 {
        var beforePartialTag = bufStr[:len(bufStr)-maxOverlap];
        var trailingWS = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWS;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, ministralEventContent{content: unambiguous});
    }
        return events, false;
    }
        var whitespaceLen = trailingWhitespaceLen(bufStr);
        var ambiguousStart = len(bufStr) - whitespaceLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, ministralEventContent{content: unambiguous});
    }
        return events, false;
        case ministralCollectingThinkingContent:;
        var bufStr = p.buffer.String();
        if strings.Contains(bufStr, ministralThinkEndTag) {
        var split = strings.SplitN(bufStr, ministralThinkEndTag, 2);
        var thinkingContent = split[0];
        var after = strings.TrimLeftFunc(split[1], unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if len(thinkingContent) > 0 {
        events = append(events, ministralEventThinking{thinking: thinkingContent});
    }
        p.state = ministralCollectingContent;
        return events, true;
    }
        var if overlapLen = overlap(bufStr, ministralThinkEndTag); overlapLen > 0 {
        var unambiguous = bufStr[:len(bufStr)-overlapLen];
        var ambiguous = bufStr[len(bufStr)-overlapLen:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, ministralEventThinking{thinking: unambiguous});
    }
        return events, false;
    }
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, ministralEventThinking{thinking: bufStr});
    }
        return events, false;
        case ministralCollectingToolName:;
        var bufStr = p.buffer.String();
        if strings.Contains(bufStr, ministralArgsTag) {
        var split = strings.SplitN(bufStr, ministralArgsTag, 2);
        var toolName = split[0];
        var after = split[1];
        p.pendingToolName = toolName;
        p.buffer.Reset();
        p.buffer.WriteString(after);
        p.state = ministralCollectingToolArgs;
        return events, true;
    }
        return events, false;
        case ministralCollectingToolArgs:;
        var bufStr = p.buffer.String();
        var jsonEnd = findJSONEnd(bufStr);
        if jsonEnd != -1 {
        var jsonStr = bufStr[:jsonEnd+1];
        var remaining = bufStr[jsonEnd+1:];
        events = append(events, ministralEventToolCall{
        name: p.pendingToolName,;
        args: jsonStr,;
        });
        p.pendingToolName = "";
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = ministralCollectingContent;
        return events, true;
    }
        return events, false;
        default:;
        panic("unexpected ministral event");
    }
    }
        func (p *MinistralParser) parseEvents() []ministralEvent {
        var all []ministralEvent;
        var keepLooping = true;
        for keepLooping {
        var events []ministralEvent;
        events, keepLooping = p.eat();
        all = append(all, events...);
    }
        return all;
    }
        func (p *MinistralParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var contentBuilder, thinkingBuilder strings.Builder;
        var toolCalls []api.ToolCall;
        var for _, event = range events {
        var switch e = event.(type) {
        case ministralEventContent:;
        contentBuilder.WriteString(e.content);
        case ministralEventThinking:;
        thinkingBuilder.WriteString(e.thinking);
        case ministralEventToolCall:;
        var tool, toolErr = toolByName(p.tools, e.name);
        if toolErr != null {
        return contentBuilder.String(), thinkingBuilder.String(), toolCalls, toolErr;
    }
        var args api.ToolCallFunctionArguments;
        var if jsonErr = json.Unmarshal([]byte(e.args), &args); jsonErr != null {
        return contentBuilder.String(), thinkingBuilder.String(), toolCalls, jsonErr;
    }
        toolCalls = append(toolCalls, api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      tool.Function.Name,;
        Arguments: args,;
        },;
        });
    }
    }
        var for i = range toolCalls {
        toolCalls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentBuilder.String(), thinkingBuilder.String(), toolCalls, null;
    }

    public static int findJSONEnd(String s) {
        var depth = 0;
        var inString = false;
        var escaped = false;
        var for i, r = range s {
        if inString {
        switch {
        case escaped:;
        escaped = false;
        case r == '\\':;
        escaped = true;
        case r == '"':;
        inString = false;
    }
        continue;
    }
        switch r {
        case '"':;
        inString = true;
        case '{', '[':;
        depth++;
        case '}', ']':;
        depth--;
        if depth == 0 {
        return i;
    }
    }
    }
        return -1;
    }
}
