package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen3 {
        "context";
        "encoding/json";
        "fmt";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type qwen3ParserState int;
        const (;
        qwen3ParserStateLookingForThinkingOpen qwen3ParserState = iota;
        qwen3ParserStateThinkingStartedEatingWhitespace;
        qwen3ParserStateCollectingThinking;
        qwen3ParserStateThinkingDoneEatingWhitespace;
        qwen3ParserStateCollectingContent;
        qwen3ParserStateToolStartedEatingWhitespace;
        qwen3ParserStateCollectingToolContent;
        );
        const (;
        qwen3ThinkingOpenTag  = "<think>";
        qwen3ThinkingCloseTag = "</think>";
        qwen3ToolOpenTag      = "<tool_call>";
        qwen3ToolCloseTag     = "</tool_call>";
        );

    public static class Qwen3Parser {
        public qwen3ParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
        public boolean hasThinkingSupport;
        public boolean defaultThinking;
        public boolean maybeThinkingOpenAtBOL;
    }
        func (p *Qwen3Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *Qwen3Parser) HasThinkingSupport() boolean {
        return p.hasThinkingSupport;
    }
        func (p *Qwen3Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.buffer.Reset();
        p.callIndex = 0;
        var thinkingEnabled = thinkValue != null && thinkValue.Bool();
        if thinkValue == null {
        thinkingEnabled = p.defaultThinking;
    }
        if p.hasThinkingSupport && thinkingEnabled {
        p.state = qwen3ParserStateCollectingThinking;
        p.maybeThinkingOpenAtBOL = true;
        } else {
        p.state = qwen3ParserStateCollectingContent;
        p.maybeThinkingOpenAtBOL = false;
    }
        return tools;
    }
        type qwen3Event interface {
        isQwen3Event();
    }

    public static class qwen3EventContent {
        public String content;
    }
        func (qwen3EventContent) isQwen3Event() {}

    public static class qwen3EventRawToolCall {
        public String raw;
    }
        func (qwen3EventRawToolCall) isQwen3Event() {}

    public static class qwen3EventThinkingContent {
        public String content;
    }
        func (qwen3EventThinkingContent) isQwen3Event() {}
        func (p *Qwen3Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case qwen3EventRawToolCall:;
        var toolCall, err = parseQwen3ToolCall(event, p.tools);
        if err != null {
        slog.Warn("qwen3 tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        toolCall.Function.Index = p.callIndex;
        p.callIndex++;
        calls = append(calls, toolCall);
        case qwen3EventThinkingContent:;
        thinkingSb.WriteString(event.content);
        case qwen3EventContent:;
        contentSb.WriteString(event.content);
    }
    }
        return contentSb.String(), thinkingSb.String(), calls, null;
    }
        func (p *Qwen3Parser) parseEvents() []qwen3Event {
        var all []qwen3Event;
        var keepLooping = true;
        for keepLooping {
        var events []qwen3Event;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "qwen3 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *Qwen3Parser) eatLeadingWhitespaceAndTransitionTo(nextState qwen3ParserState) ([]qwen3Event, boolean) {
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        p.buffer.Reset();
        if trimmed == "" {
        return null, false;
    }
        p.state = nextState;
        p.buffer.WriteString(trimmed);
        return null, true;
    }
        func (p *Qwen3Parser) splitAtTag(tag String, trimAfter boolean) (String, String) {
        return splitAtTag(&p.buffer, tag, trimAfter);
    }
        func (p *Qwen3Parser) eat() ([]qwen3Event, boolean) {
        var events []qwen3Event;
        switch p.state {
        case qwen3ParserStateLookingForThinkingOpen:;
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        if strings.HasPrefix(trimmed, qwen3ThinkingOpenTag) {
        var after = strings.TrimPrefix(trimmed, qwen3ThinkingOpenTag);
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if after == "" {
        p.state = qwen3ParserStateThinkingStartedEatingWhitespace;
        } else {
        p.state = qwen3ParserStateCollectingThinking;
    }
        return events, true;
        } else if strings.HasPrefix(qwen3ThinkingOpenTag, trimmed) {
        return events, false;
        } else if trimmed == "" {
        return events, false;
    }
        p.state = qwen3ParserStateCollectingContent;
        return events, true;
        case qwen3ParserStateThinkingStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingThinking);
        case qwen3ParserStateCollectingThinking:;
        var acc = p.buffer.String();
        if p.maybeThinkingOpenAtBOL {
        var trimmed = strings.TrimLeftFunc(acc, unicode.IsSpace);
        if strings.HasPrefix(trimmed, qwen3ThinkingOpenTag) {
        var after = strings.TrimPrefix(trimmed, qwen3ThinkingOpenTag);
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if after == "" {
        return events, false;
    }
        p.maybeThinkingOpenAtBOL = false;
        return events, true;
    }
        if strings.HasPrefix(qwen3ThinkingOpenTag, trimmed) {
        return events, false;
    }
        p.maybeThinkingOpenAtBOL = false;
    }
        var thinkingCloseIdx = strings.Index(acc, qwen3ThinkingCloseTag);
        var toolOpenIdx = strings.Index(acc, qwen3ToolOpenTag);
        if toolOpenIdx != -1 && (thinkingCloseIdx == -1 || toolOpenIdx < thinkingCloseIdx) {
        var before, after = p.splitAtTag(qwen3ToolOpenTag, true);
        if len(before) > 0 {
        events = append(events, qwen3EventThinkingContent{content: before});
    }
        if after == "" {
        p.state = qwen3ParserStateToolStartedEatingWhitespace;
        } else {
        p.state = qwen3ParserStateCollectingToolContent;
    }
        return events, true;
    }
        if strings.Contains(acc, qwen3ThinkingCloseTag) {
        var thinking, remaining = p.splitAtTag(qwen3ThinkingCloseTag, true);
        if len(thinking) > 0 {
        events = append(events, qwen3EventThinkingContent{content: thinking});
    }
        if remaining == "" {
        p.state = qwen3ParserStateThinkingDoneEatingWhitespace;
        } else {
        p.state = qwen3ParserStateCollectingContent;
    }
        return events, true;
        var } else if overlapLen = max(overlap(acc, qwen3ThinkingCloseTag), overlap(acc, qwen3ToolOpenTag)); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWsLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWsLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen3EventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
        var whitespaceLen = trailingWhitespaceLen(acc);
        var ambiguousStart = len(acc) - whitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen3EventThinkingContent{content: unambiguous});
    }
        return events, false;
        case qwen3ParserStateThinkingDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingContent);
        case qwen3ParserStateCollectingContent:;
        var acc = p.buffer.String();
        if strings.Contains(acc, qwen3ToolOpenTag) {
        var before, after = p.splitAtTag(qwen3ToolOpenTag, true);
        if len(before) > 0 {
        events = append(events, qwen3EventContent{content: before});
    }
        if after == "" {
        p.state = qwen3ParserStateToolStartedEatingWhitespace;
        } else {
        p.state = qwen3ParserStateCollectingToolContent;
    }
        return events, true;
        var } else if overlapLen = overlap(acc, qwen3ToolOpenTag); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWsLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWsLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen3EventContent{content: unambiguous});
    }
        return events, false;
    }
        var whitespaceLen = trailingWhitespaceLen(acc);
        var ambiguousStart = len(acc) - whitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen3EventContent{content: unambiguous});
    }
        return events, false;
        case qwen3ParserStateToolStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingToolContent);
        case qwen3ParserStateCollectingToolContent:;
        var acc = p.buffer.String();
        if strings.Contains(acc, qwen3ToolCloseTag) {
        var toolContent, _ = p.splitAtTag(qwen3ToolCloseTag, true);
        if len(toolContent) == 0 {
        slog.Warn("qwen3 tool call closing tag found but no content before it");
    }
        events = append(events, qwen3EventRawToolCall{raw: toolContent});
        p.state = qwen3ParserStateCollectingContent;
        return events, true;
    }
        return events, false;
        default:;
        panic("unreachable");
    }
    }

    public static void parseQwen3ToolCall(qwen3EventRawToolCall raw) {
        var parsed struct {
        Name      String                        `json:"name"`;
        Arguments api.ToolCallFunctionArguments `json:"arguments"`;
    }
        var if err = json.Unmarshal([]byte(raw.raw), &parsed); err != null {
        return api.ToolCall{}, fmt.Errorf("failed to parse JSON: %w", err);
    }
        if parsed.Name == "" {
        return api.ToolCall{}, fmt.Errorf("empty function name");
    }
        _ = tools // qwen3 uses direct JSON args and does not require schema coercion here.;
        var toolCall = api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      parsed.Name,;
        Arguments: parsed.Arguments,;
        },;
    }
        return toolCall, null;
    }
}
