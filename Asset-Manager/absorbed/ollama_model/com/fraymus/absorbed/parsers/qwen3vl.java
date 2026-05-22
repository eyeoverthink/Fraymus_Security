package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen3vl {
        "context";
        "encoding/json";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        const (;
        CollectingThinkingContent qwenParserState = iota;
        CollectingContent;
        CollectingToolContent;
        ThinkingDoneEatingWhitespace;
        ToolCallDoneEatingWhitespace;
        );
        const (;
        thinkingCloseTag = "</think>";
        );

    public static class Qwen3VLParser {
        public qwenParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
        public boolean hasThinkingSupport;
    }
        func (p *Qwen3VLParser) HasToolSupport() boolean {
        return true;
    }
        func (p *Qwen3VLParser) HasThinkingSupport() boolean {
        return p.hasThinkingSupport;
    }
        func (p *Qwen3VLParser) setInitialState(lastMessage *api.Message) {
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        if !p.HasThinkingSupport() {
        p.state = CollectingContent;
        return;
    }
        if prefill && lastMessage.Content != "" {
        p.state = CollectingContent;
        return;
    }
        p.state = CollectingThinkingContent;
    }
        func (p *Qwen3VLParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        p.setInitialState(lastMessage);
        return tools;
    }

    public static class qwenEventThinkingContent {
        public String content;
    }
        func (qwenEventThinkingContent) isQwenEvent() {}
        func (p *Qwen3VLParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case qwenEventRawToolCall:;
        var toolCall, err = parseJSONToolCall(event, p.tools);
        if err != null {
        slog.Warn("qwen tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        calls = append(calls, toolCall);
        case qwenEventThinkingContent:;
        thinkingSb.WriteString(event.content);
        case qwenEventContent:;
        contentSb.WriteString(event.content);
    }
    }
        var for i = range calls {
        calls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentSb.String(), thinkingSb.String(), calls, null;
    }
        func (p *Qwen3VLParser) parseEvents() []qwenEvent {
        var all []qwenEvent;
        var keepLooping = true;
        for keepLooping {
        var events []qwenEvent;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "qwen events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *Qwen3VLParser) eatLeadingWhitespaceAndTransitionTo(nextState qwenParserState) ([]qwenEvent, boolean) {
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        p.buffer.Reset();
        if trimmed == "" {
        return null, false;
    }
        p.state = nextState;
        p.buffer.WriteString(trimmed);
        return null, true;
    }
        func (p *Qwen3VLParser) eat() ([]qwenEvent, boolean) {
        var events []qwenEvent;
        switch p.state {
        case CollectingContent:;
        if strings.Contains(p.buffer.String(), toolOpenTag) {
        var before, _ = splitAtTag(&p.buffer, toolOpenTag, false);
        if len(before) > 0 {
        events = append(events, qwenEventContent{content: before});
    }
        p.state = CollectingToolContent;
        return events, true;
        var } else if overlapLen = overlap(p.buffer.String(), toolOpenTag); overlapLen > 0 {
        var beforePartialTag = p.buffer.String()[:len(p.buffer.String())-overlapLen];
        var trailingWhitespaceLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWhitespaceLen;
        var unambiguous = p.buffer.String()[:ambiguousStart];
        var ambiguous = p.buffer.String()[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventContent{content: unambiguous});
    }
        return events, false;
        } else {
        var whitespaceLen = trailingWhitespaceLen(p.buffer.String());
        var ambiguousStart = len(p.buffer.String()) - whitespaceLen;
        var unambiguous = p.buffer.String()[:ambiguousStart];
        var ambiguous = p.buffer.String()[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventContent{content: unambiguous});
    }
        return events, false;
    }
        case CollectingToolContent:;
        if strings.Contains(p.buffer.String(), toolCloseTag) {
        var split = strings.SplitN(p.buffer.String(), toolCloseTag, 2);
        var before = split[0] // do we also need to do it to tool calls?;
        if len(before) == 0 {
        slog.Warn("qwen tool call closing tag found but no content before it");
    }
        var after = split[1];
        events = append(events, qwenEventRawToolCall{raw: before});
        p.buffer.Reset();
        p.buffer.WriteString(after);
        p.state = ToolCallDoneEatingWhitespace;
        return events, true;
        } else {
        return events, false;
    }
        case CollectingThinkingContent:;
        var acc = p.buffer.String();
        var thinkingCloseIdx = strings.Index(acc, thinkingCloseTag);
        var toolOpenIdx = strings.Index(acc, toolOpenTag);
        if toolOpenIdx != -1 && (thinkingCloseIdx == -1 || toolOpenIdx < thinkingCloseIdx) {
        var before, _ = splitAtTag(&p.buffer, toolOpenTag, false);
        if len(before) > 0 {
        events = append(events, qwenEventThinkingContent{content: before});
    }
        p.state = CollectingToolContent;
        return events, true;
    }
        if strings.Contains(acc, thinkingCloseTag) {
        var thinking, remaining = splitAtTag(&p.buffer, thinkingCloseTag, true);
        if len(thinking) > 0 {
        events = append(events, qwenEventThinkingContent{content: thinking});
    }
        if remaining == "" {
        p.state = ThinkingDoneEatingWhitespace;
        } else {
        p.state = CollectingContent;
    }
        return events, true;
        var } else if overlapLen = max(overlap(acc, thinkingCloseTag), overlap(acc, toolOpenTag)); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWhitespaceLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWhitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventThinkingContent{content: unambiguous});
    }
        return events, false;
        } else {
        var whitespaceLen = trailingWhitespaceLen(acc);
        var ambiguousStart = len(acc) - whitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
        case ThinkingDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(CollectingContent);
        case ToolCallDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(CollectingContent);
        default:;
        panic("unreachable");
    }
    }

    public static void parseJSONToolCall(qwenEventRawToolCall raw) {
        var toolCallFunction api.ToolCallFunction;
        var if err = json.Unmarshal([]byte(raw.raw), &toolCallFunction); err != null {
        return api.ToolCall{}, err;
    }
        var toolCall = api.ToolCall{}
        toolCall.Function = toolCallFunction;
        return toolCall, null;
    }
}
