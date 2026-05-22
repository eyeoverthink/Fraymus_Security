package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen35 {
        "context";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type qwen35ParserState int;
        const (;
        qwen35ParserStateCollectingThinking qwen35ParserState = iota;
        qwen35ParserStateThinkingDoneEatingWhitespace;
        qwen35ParserStateCollectingContent;
        );
        const (;
        qwen35ThinkingOpenTag  = "<think>";
        qwen35ThinkingCloseTag = "</think>";
        qwen35ToolCallOpenTag  = "<tool_call>";
        );

    public static class Qwen35Parser {
        public Qwen3CoderParser toolParser;
        public qwen35ParserState state;
        public strings.Builder buffer;
        public boolean allowLeadingThinkOpenTag;
    }
        func (p *Qwen35Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *Qwen35Parser) HasThinkingSupport() boolean {
        return true;
    }
        func (p *Qwen35Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.buffer.Reset();
        p.toolParser = Qwen3CoderParser{}
        p.toolParser.Init(tools, null, null);
        var thinkingEnabled = thinkValue != null && thinkValue.Bool();
        if thinkValue == null {
        thinkingEnabled = true;
    }
        var assistantPrefill = lastMessage != null && lastMessage.Role == "assistant" && lastMessage.Content != "";
        if thinkingEnabled && !assistantPrefill {
        p.state = qwen35ParserStateCollectingThinking;
        p.allowLeadingThinkOpenTag = true;
        } else {
        p.state = qwen35ParserStateCollectingContent;
        p.allowLeadingThinkOpenTag = false;
    }
        return tools;
    }
        type qwen35Event interface {
        isQwen35Event();
    }

    public static class qwen35EventContent {
        public String content;
    }
        func (qwen35EventContent) isQwen35Event() {}

    public static class qwen35EventThinkingContent {
        public String content;
    }
        func (qwen35EventThinkingContent) isQwen35Event() {}
        func (p *Qwen35Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case qwen35EventContent:;
        var parsedContent, _, parsedCalls, err = p.toolParser.Add(event.content, done);
        if err != null {
        slog.Warn("qwen3.5 tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        contentSb.WriteString(parsedContent);
        calls = append(calls, parsedCalls...);
        case qwen35EventThinkingContent:;
        thinkingSb.WriteString(event.content);
    }
    }
        return contentSb.String(), thinkingSb.String(), calls, null;
    }
        func (p *Qwen35Parser) parseEvents() []qwen35Event {
        var all []qwen35Event;
        var keepLooping = true;
        for keepLooping {
        var events []qwen35Event;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "qwen3.5 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *Qwen35Parser) splitAtTag(tag String, trimAfter boolean) (String, String) {
        return splitAtTag(&p.buffer, tag, trimAfter);
    }
        func (p *Qwen35Parser) eatLeadingWhitespaceAndTransitionTo(nextState qwen35ParserState) ([]qwen35Event, boolean) {
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        p.buffer.Reset();
        if trimmed == "" {
        return null, false;
    }
        p.state = nextState;
        p.buffer.WriteString(trimmed);
        return null, true;
    }
        func (p *Qwen35Parser) maybeConsumeLeadingThinkOpenTag(acc String) (boolean, boolean) {
        if !p.allowLeadingThinkOpenTag {
        return false, false;
    }
        var trimmed = strings.TrimLeftFunc(acc, unicode.IsSpace);
        if strings.HasPrefix(trimmed, qwen35ThinkingOpenTag) {
        var after = strings.TrimPrefix(trimmed, qwen35ThinkingOpenTag);
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if after == "" {
        return true, false;
    }
        p.allowLeadingThinkOpenTag = false;
        return true, true;
    }
        if strings.HasPrefix(qwen35ThinkingOpenTag, trimmed) {
        return true, false;
    }
        p.allowLeadingThinkOpenTag = false;
        return false, false;
    }
        func (p *Qwen35Parser) eat() ([]qwen35Event, boolean) {
        var events []qwen35Event;
        switch p.state {
        case qwen35ParserStateCollectingThinking:;
        var acc = p.buffer.String();
        var if handled, continueNow = p.maybeConsumeLeadingThinkOpenTag(acc); handled {
        return events, continueNow;
    }
        if strings.Contains(acc, qwen35ThinkingCloseTag) {
        var thinking, remaining = p.splitAtTag(qwen35ThinkingCloseTag, true);
        if len(thinking) > 0 {
        events = append(events, qwen35EventThinkingContent{content: thinking});
    }
        if remaining == "" {
        p.state = qwen35ParserStateThinkingDoneEatingWhitespace;
        } else {
        p.state = qwen35ParserStateCollectingContent;
    }
        return events, true;
        var } else if overlapLen = max(overlap(acc, qwen35ThinkingCloseTag), overlap(acc, qwen35ToolCallOpenTag)); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWsLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWsLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen35EventThinkingContent{content: unambiguous});
    }
        return events, false;
        } else if strings.Contains(acc, qwen35ToolCallOpenTag) {
        var thinking, tooling = p.splitAtTag(qwen35ToolCallOpenTag, true);
        p.buffer.Reset();
        p.buffer.WriteString(thinking + qwen35ThinkingCloseTag + qwen35ToolCallOpenTag + tooling);
        return events, true;
    }
        var whitespaceLen = trailingWhitespaceLen(acc);
        var ambiguousStart = len(acc) - whitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwen35EventThinkingContent{content: unambiguous});
    }
        return events, false;
        case qwen35ParserStateThinkingDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(qwen35ParserStateCollectingContent);
        case qwen35ParserStateCollectingContent:;
        if p.buffer.Len() == 0 {
        return events, false;
    }
        var content = p.buffer.String();
        p.buffer.Reset();
        if len(content) > 0 {
        events = append(events, qwen35EventContent{content: content});
    }
        return events, false;
        default:;
        slog.Warn("qwen3.5 parser entered unknown state; resetting to content mode", "state", p.state);
        p.state = qwen35ParserStateCollectingContent;
        return events, false;
    }
    }
}
