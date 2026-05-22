package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class olmo3_think {
        "context";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type olmo3ThinkParserState int;
        const (;
        olmo3CollectingThink olmo3ThinkParserState = iota;
        olmo3CollectingContent;
        );
        const (;
        olmo3ThinkCloseTag = "</think>";
        );

    public static class Olmo3ThinkParser {
        public olmo3ThinkParserState state;
        public strings.Builder buffer;
    }
        func (p *Olmo3ThinkParser) HasToolSupport() boolean {
        return false;
    }
        func (p *Olmo3ThinkParser) HasThinkingSupport() boolean {
        return true;
    }
        func (p *Olmo3ThinkParser) setInitialState(lastMessage *api.Message) {
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        if prefill && lastMessage.Content != "" {
        p.state = olmo3CollectingContent;
        return;
    }
        p.state = olmo3CollectingThink;
    }
        func (p *Olmo3ThinkParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.setInitialState(lastMessage);
        return tools;
    }
        type olmo3Event interface {
        isOlmo3Event();
    }

    public static class olmo3EventThinkContent {
        public String content;
    }

    public static class olmo3EventContent {
        public String content;
    }
        func (olmo3EventThinkContent) isOlmo3Event() {}
        func (olmo3EventContent) isOlmo3Event()      {}
        func (p *Olmo3ThinkParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case olmo3EventThinkContent:;
        thinkingSb.WriteString(event.content);
        case olmo3EventContent:;
        contentSb.WriteString(event.content);
    }
    }
        return contentSb.String(), thinkingSb.String(), null, null;
    }
        func (p *Olmo3ThinkParser) parseEvents() []olmo3Event {
        var all []olmo3Event;
        var keepLooping = true;
        for keepLooping {
        var events []olmo3Event;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "olmo3 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *Olmo3ThinkParser) eat() ([]olmo3Event, boolean) {
        var events []olmo3Event;
        var bufStr = p.buffer.String();
        if bufStr == "" {
        return events, false;
    }
        switch p.state {
        case olmo3CollectingThink:;
        if strings.Contains(bufStr, olmo3ThinkCloseTag) {
        var split = strings.SplitN(bufStr, olmo3ThinkCloseTag, 2);
        var thinking = strings.TrimRightFunc(split[0], unicode.IsSpace);
        var remaining = strings.TrimLeftFunc(split[1], unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = olmo3CollectingContent;
        if len(thinking) > 0 {
        events = append(events, olmo3EventThinkContent{content: thinking});
    }
        return events, true;
        var } else if overlapLen = overlap(bufStr, olmo3ThinkCloseTag); overlapLen > 0 {
        var beforePartialTag = bufStr[:len(bufStr)-overlapLen];
        var trailingLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, olmo3EventThinkContent{content: unambiguous});
    }
        return events, false;
        } else {
        var whitespaceLen = trailingWhitespaceLen(bufStr);
        var ambiguousStart = len(bufStr) - whitespaceLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, olmo3EventThinkContent{content: unambiguous});
    }
        return events, false;
    }
        case olmo3CollectingContent:;
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, olmo3EventContent{content: bufStr});
    }
        return events, false;
    }
        return events, false;
    }
}
