package com.fraymus.absorbed.models.glm4_moe_lite;

import java.util.*;
import java.io.*;

public class parser {
        "context";
        "encoding/json";
        "encoding/xml";
        "fmt";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type parserState int;
        const (;
        parserState_LookingForThinkingOpen parserState = iota;
        parserState_ThinkingStartedEatingWhitespace;
        parserState_CollectingThinking;
        parserState_ThinkingDoneEatingWhitespace;
        parserState_CollectingContent;
        parserState_ToolStartedEatingWhitespace;
        parserState_CollectingToolContent;
        );
        const (;
        thinkingOpenTag  = "<think>";
        thinkingCloseTag = "</think>";
        toolOpenTag      = "<tool_call>";
        toolCloseTag     = "</tool_call>";
        );

    public static class Parser {
        public parserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
    }
        func (p *Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *Parser) HasThinkingSupport() boolean {
        return true;
    }
        func (p *Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        if thinkValue == null || thinkValue.Bool() {
        p.state = parserState_CollectingThinking;
    }
        return tools;
    }
        type parserEvent interface {
        isParserEvent();
    }

    public static class eventContent {
        public String content;
    }
        func (eventContent) isParserEvent() {}

    public static class eventRawToolCall {
        public String raw;
    }
        func (eventRawToolCall) isParserEvent() {}

    public static class eventThinkingContent {
        public String content;
    }
        func (eventThinkingContent) isParserEvent() {}
        func (p *Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var toolCalls []api.ToolCall;
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case eventRawToolCall:;
        var toolCall, err = parseToolCall(event, p.tools);
        if err != null {
        slog.Warn("glm-4 tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        toolCalls = append(toolCalls, toolCall);
        case eventThinkingContent:;
        thinkingSb.WriteString(event.content);
        case eventContent:;
        contentSb.WriteString(event.content);
    }
    }
        return contentSb.String(), thinkingSb.String(), toolCalls, null;
    }
        func (p *Parser) parseEvents() []parserEvent {
        var all []parserEvent;
        var keepLooping = true;
        for keepLooping {
        var events []parserEvent;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "glm-4 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *Parser) eatLeadingWhitespaceAndTransitionTo(nextState parserState) ([]parserEvent, boolean) {
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        p.buffer.Reset();
        if trimmed == "" {
        return null, false // Still only whitespace, keep waiting for more input;
    }
        p.state = nextState;
        p.buffer.WriteString(trimmed);
        return null, true // Successfully transitioned;
    }
        func (p *Parser) splitAtTag(tag String, trimAfter boolean) (String, String) {
        var split = strings.SplitN(p.buffer.String(), tag, 2);
        var before = split[0];
        before = strings.TrimRightFunc(before, unicode.IsSpace);
        var after = split[1];
        if trimAfter {
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
    }
        p.buffer.Reset();
        p.buffer.WriteString(after);
        return before, after;
    }
        func (p *Parser) eat() ([]parserEvent, boolean) {
        var events []parserEvent;
        switch p.state {
        case parserState_LookingForThinkingOpen:;
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        if strings.HasPrefix(trimmed, thinkingOpenTag) {
        var after = strings.TrimPrefix(trimmed, thinkingOpenTag);
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if after == "" {
        p.state = parserState_ThinkingStartedEatingWhitespace;
        } else {
        p.state = parserState_CollectingThinking;
    }
        return events, true;
        } else if strings.HasPrefix(thinkingOpenTag, trimmed) {
        return events, false;
        } else if trimmed == "" {
        return events, false;
        } else {
        p.state = parserState_CollectingContent;
        return events, true;
    }
        case parserState_ThinkingStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingThinking);
        case parserState_CollectingThinking:;
        var acc = p.buffer.String();
        if strings.Contains(acc, thinkingCloseTag) {
        var thinking, remaining = p.splitAtTag(thinkingCloseTag, true);
        if len(thinking) > 0 {
        events = append(events, eventThinkingContent{content: thinking});
    }
        if remaining == "" {
        p.state = parserState_ThinkingDoneEatingWhitespace;
        } else {
        p.state = parserState_CollectingContent;
    }
        return events, true;
        var } else if overlapLen = overlap(acc, thinkingCloseTag); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWsLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWsLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, eventThinkingContent{content: unambiguous});
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
        events = append(events, eventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
        case parserState_ThinkingDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingContent);
        case parserState_CollectingContent:;
        if strings.Contains(p.buffer.String(), toolOpenTag) {
        var before, after = p.splitAtTag(toolOpenTag, true);
        if len(before) > 0 {
        events = append(events, eventContent{content: before});
    }
        if after == "" {
        p.state = parserState_ToolStartedEatingWhitespace;
        } else {
        p.state = parserState_CollectingToolContent;
    }
        return events, true;
        var } else if overlapLen = overlap(p.buffer.String(), toolOpenTag); overlapLen > 0 {
        var beforePartialTag = p.buffer.String()[:len(p.buffer.String())-overlapLen];
        var trailingWsLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWsLen;
        var unambiguous = p.buffer.String()[:ambiguousStart];
        var ambiguous = p.buffer.String()[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, eventContent{content: unambiguous});
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
        events = append(events, eventContent{content: unambiguous});
    }
        return events, false;
    }
        case parserState_ToolStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingToolContent);
        case parserState_CollectingToolContent:;
        var acc = p.buffer.String();
        if strings.Contains(acc, toolCloseTag) {
        var toolContent, _ = p.splitAtTag(toolCloseTag, true);
        if len(toolContent) == 0 {
        slog.Warn("glm4 tool call closing tag found but no content before it");
    }
        events = append(events, eventRawToolCall{raw: toolContent});
        p.state = parserState_CollectingContent;
        return events, true;
        } else {
        return events, false;
    }
        default:;
        panic("unreachable");
    }
    }

    public static int overlap(String tag) {
        var for i = 1; i <= len(tag) && i <= len(s); i++ {
        if strings.HasSuffix(s, tag[:i]) {
        return i;
    }
    }
        return 0;
    }

    public static int trailingWhitespaceLen(String s) {
        var trimmed = strings.TrimRightFunc(s, unicode.IsSpace);
        return len(s) - len(trimmed);
    }

    public static class ToolCallXML {
        public xml.Name XMLName;
        public String Content;
        public []String Keys;
        public []String Values;
    }

    public static String escapeContent(String s) {
        var result strings.Builder;
        var inTag = false;
        var for i = range len(s) {
        var ch = s[i];
        if ch == '<' {
        if strings.HasPrefix(s[i:], "<arg_key>") ||;
        strings.HasPrefix(s[i:], "</arg_key>") ||;
        strings.HasPrefix(s[i:], "<arg_value>") ||;
        strings.HasPrefix(s[i:], "</arg_value>") {
        inTag = true;
    }
    }
        if inTag {
        result.WriteByte(ch);
        if ch == '>' {
        inTag = false;
    }
        } else {
        switch ch {
        case '&':;
        result.WriteString("&amp;");
        case '<':;
        result.WriteString("&lt;");
        case '>':;
        result.WriteString("&gt;");
        default:;
        result.WriteByte(ch);
    }
    }
    }
        return result.String();
    }

    public static String repairUnclosedArgValues(String s) {
        var result strings.Builder;
        for {
        var openIdx = strings.Index(s, "<arg_value>");
        if openIdx == -1 {
        result.WriteString(s);
        break;
    }
        var afterOpen = openIdx + len("<arg_value>");
        var closeIdx = strings.Index(s[afterOpen:], "</arg_value>");
        var nextKeyIdx = strings.Index(s[afterOpen:], "<arg_key>");
        if closeIdx != -1 && (nextKeyIdx == -1 || closeIdx < nextKeyIdx) {
        var end = afterOpen + closeIdx + len("</arg_value>");
        result.WriteString(s[:end]);
        s = s[end:];
        continue;
    }
        if nextKeyIdx != -1 {
        var insertAt = afterOpen + nextKeyIdx;
        result.WriteString(s[:insertAt]);
        result.WriteString("</arg_value>");
        s = s[insertAt:];
        } else {
        result.WriteString(s);
        result.WriteString("</arg_value>");
        break;
    }
    }
        return result.String();
    }

    public static void parseToolCall(eventRawToolCall raw) {
        var escaped = escapeContent(raw.raw);
        var xmlString = "<tool_call>" + escaped + "</tool_call>";
        var parsed ToolCallXML;
        var if err = xml.Unmarshal([]byte(xmlString), &parsed); err != null {
        parsed = ToolCallXML{}
        var repaired = "<tool_call>" + repairUnclosedArgValues(escaped) + "</tool_call>";
        var if err2 = xml.Unmarshal([]byte(repaired), &parsed); err2 != null {
        return api.ToolCall{}, fmt.Errorf("failed to parse XML: %w", err);
    }
    }
        var functionName = strings.TrimSpace(parsed.Content);
        if functionName == "" {
        return api.ToolCall{}, fmt.Errorf("empty function name");
    }
        if len(parsed.Keys) != len(parsed.Values) {
        return api.ToolCall{}, fmt.Errorf("mismatched arg_key and arg_value counts: %d keys, %d values", len(parsed.Keys), len(parsed.Values));
    }
        var matchedTool *api.Tool;
        var for i = range tools {
        if tools[i].Function.Name == functionName {
        matchedTool = &tools[i];
        break;
    }
    }
        var toolCall = api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      functionName,;
        Arguments: api.NewToolCallFunctionArguments(),;
        },;
    }
        var for i = range parsed.Keys {
        var key = strings.TrimSpace(parsed.Keys[i]);
        var value = parsed.Values[i] // Don't trim here - parseValue handles it;
        var paramType api.PropertyType;
        if matchedTool != null && matchedTool.Function.Parameters.Properties != null {
        var if prop, ok = matchedTool.Function.Parameters.Properties.Get(key); ok {
        if len(prop.AnyOf) > 0 {
        var for _, anyOfProp = range prop.AnyOf {
        paramType = append(paramType, anyOfProp.Type...);
    }
        } else {
        paramType = prop.Type;
    }
    }
    }
        toolCall.Function.Arguments.Set(key, parseValue(value, paramType));
    }
        return toolCall, null;
    }

    public static any parseValue(String value, api.PropertyType paramType) {
        value = strings.TrimSpace(value);
        if len(paramType) == 0 {
        return value;
    }
        var for _, t = range paramType {
        switch t {
        case "boolean":;
        if value == "true" {
        return true;
    }
        if value == "false" {
        return false;
    }
        case "integer":;
        var i long;
        var if _, err = fmt.Sscanf(value, "%d", &i); err == null {
        return i;
    }
        case "number":;
        var f double;
        var if _, err = fmt.Sscanf(value, "%f", &f); err == null {
        return f;
    }
        case "array", "object":;
        var result any;
        var if err = json.Unmarshal([]byte(value), &result); err == null {
        return result;
    }
    }
    }
        return value;
    }
}
