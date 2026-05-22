package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class glm46 {
        "context";
        "encoding/xml";
        "fmt";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type glm46ParserState int;
        const (;
        glm46ParserState_LookingForThinkingOpen glm46ParserState = iota;
        glm46ParserState_ThinkingStartedEatingWhitespace;
        glm46ParserState_CollectingThinking;
        glm46ParserState_ThinkingDoneEatingWhitespace;
        glm46ParserState_CollectingContent;
        glm46ParserState_ToolStartedEatingWhitespace;
        glm46ParserState_CollectingToolContent;
        );
        const (;
        glm46ThinkingOpenTag  = "<think>";
        glm46ThinkingCloseTag = "</think>";
        glm46ToolOpenTag      = "<tool_call>";
        glm46ToolCloseTag     = "</tool_call>";
        );

    public static class GLM46Parser {
        public glm46ParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
    }
        func (p *GLM46Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *GLM46Parser) HasThinkingSupport() boolean {
        return true;
    }
        func (p *GLM46Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        return tools;
    }
        type glm46Event interface {
        isGLM46Event();
    }

    public static class glm46EventContent {
        public String content;
    }
        func (glm46EventContent) isGLM46Event() {}

    public static class glm46EventRawToolCall {
        public String raw;
    }
        func (glm46EventRawToolCall) isGLM46Event() {}

    public static class glm46EventThinkingContent {
        public String content;
    }
        func (glm46EventThinkingContent) isGLM46Event() {}
        func (p *GLM46Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents();
        var toolCalls []api.ToolCall;
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case glm46EventRawToolCall:;
        var toolCall, err = parseGLM46ToolCall(event, p.tools);
        if err != null {
        slog.Warn("glm-4.6 tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        toolCall.Function.Index = p.callIndex;
        p.callIndex++;
        toolCalls = append(toolCalls, toolCall);
        case glm46EventThinkingContent:;
        thinkingSb.WriteString(event.content);
        case glm46EventContent:;
        contentSb.WriteString(event.content);
    }
    }
        return contentSb.String(), thinkingSb.String(), toolCalls, null;
    }
        func (p *GLM46Parser) parseEvents() []glm46Event {
        var all []glm46Event;
        var keepLooping = true;
        for keepLooping {
        var events []glm46Event;
        events, keepLooping = p.eat();
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "glm-4.6 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String());
    }
        return all;
    }
        func (p *GLM46Parser) eatLeadingWhitespaceAndTransitionTo(nextState glm46ParserState) ([]glm46Event, boolean) {
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        p.buffer.Reset();
        if trimmed == "" {
        return null, false // Still only whitespace, keep waiting for more input;
    }
        p.state = nextState;
        p.buffer.WriteString(trimmed);
        return null, true // Successfully transitioned;
    }

    public static void glm46SplitAtTag(*GLM46Parser p, String tag) {
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
        func (p *GLM46Parser) eat() ([]glm46Event, boolean) {
        var events []glm46Event;
        switch p.state {
        case glm46ParserState_LookingForThinkingOpen:;
        var trimmed = strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace);
        if strings.HasPrefix(trimmed, glm46ThinkingOpenTag) {
        var after = strings.TrimPrefix(trimmed, glm46ThinkingOpenTag);
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(after);
        if after == "" {
        p.state = glm46ParserState_ThinkingStartedEatingWhitespace;
        } else {
        p.state = glm46ParserState_CollectingThinking;
    }
        return events, true;
        } else if strings.HasPrefix(glm46ThinkingOpenTag, trimmed) {
        return events, false;
        } else if trimmed == "" {
        return events, false;
        } else {
        p.state = glm46ParserState_CollectingContent;
        return events, true;
    }
        case glm46ParserState_ThinkingStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(glm46ParserState_CollectingThinking);
        case glm46ParserState_CollectingThinking:;
        var acc = p.buffer.String();
        if strings.Contains(acc, glm46ThinkingCloseTag) {
        var thinking, remaining = glm46SplitAtTag(p, glm46ThinkingCloseTag, true);
        if len(thinking) > 0 {
        events = append(events, glm46EventThinkingContent{content: thinking});
    }
        if remaining == "" {
        p.state = glm46ParserState_ThinkingDoneEatingWhitespace;
        } else {
        p.state = glm46ParserState_CollectingContent;
    }
        return events, true;
        var } else if overlapLen = overlap(acc, glm46ThinkingCloseTag); overlapLen > 0 {
        var beforePartialTag = acc[:len(acc)-overlapLen];
        var trailingWhitespaceLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWhitespaceLen;
        var unambiguous = acc[:ambiguousStart];
        var ambiguous = acc[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, glm46EventThinkingContent{content: unambiguous});
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
        events = append(events, glm46EventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
        case glm46ParserState_ThinkingDoneEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(glm46ParserState_CollectingContent);
        case glm46ParserState_CollectingContent:;
        if strings.Contains(p.buffer.String(), glm46ToolOpenTag) {
        var before, after = glm46SplitAtTag(p, glm46ToolOpenTag, true);
        if len(before) > 0 {
        events = append(events, glm46EventContent{content: before});
    }
        if after == "" {
        p.state = glm46ParserState_ToolStartedEatingWhitespace;
        } else {
        p.state = glm46ParserState_CollectingToolContent;
    }
        return events, true;
        var } else if overlapLen = overlap(p.buffer.String(), glm46ToolOpenTag); overlapLen > 0 {
        var beforePartialTag = p.buffer.String()[:len(p.buffer.String())-overlapLen];
        var trailingWhitespaceLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWhitespaceLen;
        var unambiguous = p.buffer.String()[:ambiguousStart];
        var ambiguous = p.buffer.String()[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, glm46EventContent{content: unambiguous});
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
        events = append(events, glm46EventContent{content: unambiguous});
    }
        return events, false;
    }
        case glm46ParserState_ToolStartedEatingWhitespace:;
        return p.eatLeadingWhitespaceAndTransitionTo(glm46ParserState_CollectingToolContent);
        case glm46ParserState_CollectingToolContent:;
        var acc = p.buffer.String();
        if strings.Contains(acc, glm46ToolCloseTag) {
        var toolContent, _ = glm46SplitAtTag(p, glm46ToolCloseTag, true);
        if len(toolContent) == 0 {
        slog.Warn("glm46 tool call closing tag found but no content before it");
    }
        events = append(events, glm46EventRawToolCall{raw: toolContent});
        p.state = glm46ParserState_CollectingContent;
        return events, true;
        } else {
        return events, false;
    }
        default:;
        panic("unreachable");
    }
    }

    public static class GLMToolCallXML {
        public xml.Name XMLName;
        public String Content;
        public []String Keys;
        public []String Values;
    }

    public static String escapeGLM46Content(String s) {
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
        type repairPhase int;
        const (;
        phaseArgKeyOpen  repairPhase = iota // expecting <arg_key>;
        phaseArgKeyClose                    // expecting </arg_key>;
        phaseArgValOpen                     // expecting <arg_value>;
        phaseArgValClose                    // expecting </arg_value>;
        phaseCount                          // number of phases;
        );

    public static String repairGLM46XML(String s) {
        var tagCycle = [phaseCount]String{"<arg_key>", "</arg_key>", "<arg_value>", "</arg_value>"}
        var findNextTag = func(s String) (int, String) {
        var bestIdx = -1;
        var bestTag = "";
        var for _, tag = range tagCycle {
        var if idx = strings.Index(s, tag); idx != -1 && (bestIdx == -1 || idx < bestIdx) {
        bestIdx = idx;
        bestTag = tag;
    }
    }
        return bestIdx, bestTag;
    }
        var tagIndex = func(tag String) repairPhase {
        var for i, t = range tagCycle {
        if t == tag {
        return repairPhase(i);
    }
    }
        return -1;
    }
        var result strings.Builder;
        var idx, firstTag = findNextTag(s);
        if idx == -1 {
        return s;
    }
        var prefix = s[:idx];
        s = s[idx:];
        var phase = phaseArgKeyOpen;
        if firstTag != "<arg_key>" {
        var if spIdx = strings.IndexFunc(prefix, unicode.IsSpace); spIdx != -1 {
        result.WriteString(prefix[:spIdx]);
        var keyContent = strings.TrimLeftFunc(prefix[spIdx:], unicode.IsSpace);
        result.WriteString("<arg_key>");
        result.WriteString(keyContent);
        phase = phaseArgKeyClose;
        } else {
        result.WriteString(prefix);
    }
        } else {
        result.WriteString(prefix);
    }
        for len(s) > 0 {
        var idx, found = findNextTag(s);
        var expected = tagCycle[phase];
        var isOpen = phase%2 == 0 // even phases are opening tags;
        if idx == -1 {
        if isOpen {
        break;
    }
        result.WriteString(s);
        result.WriteString(expected);
        phase = (phase + 1) % phaseCount;
        break;
    }
        if found == expected {
        result.WriteString(s[:idx]);
        result.WriteString(expected);
        s = s[idx+len(expected):];
        phase = (phase + 1) % phaseCount;
        continue;
    }
        var foundIdx = tagIndex(found);
        if isOpen && idx > 0 {
        result.WriteString(expected);
        result.WriteString(s[:idx]);
        phase = (phase + 1) % phaseCount // now expecting closing;
        s = s[idx:];
        continue;
    }
        for phase != foundIdx {
        var tag = tagCycle[phase];
        if phase%2 == 0 {
        result.WriteString(tag);
        } else {
        if (phase+1)%phaseCount == foundIdx && idx > 0 {
        result.WriteString(s[:idx]);
        s = s[idx:];
        idx = 0;
    }
        result.WriteString(tag);
    }
        phase = (phase + 1) % phaseCount;
    }
    }
        switch phase {
        case phaseArgKeyClose: // after <arg_key>, expecting text/</arg_key>;
        result.WriteString("</arg_key>");
        result.WriteString("<arg_value>");
        result.WriteString("</arg_value>");
        case phaseArgValOpen: // after </arg_key>, expecting <arg_value>;
        result.WriteString("<arg_value>");
        result.WriteString("</arg_value>");
        case phaseArgValClose: // after <arg_value>, expecting text/</arg_value>;
        result.WriteString("</arg_value>");
    }
        return result.String();
    }

    public static void parseGLM46ToolCall(glm46EventRawToolCall raw) {
        var escaped = escapeGLM46Content(raw.raw);
        var xmlString = "<tool_call>" + escaped + "</tool_call>";
        var parsed GLMToolCallXML;
        var if err = xml.Unmarshal([]byte(xmlString), &parsed); err != null {
        parsed = GLMToolCallXML{}
        var repaired = "<tool_call>" + repairGLM46XML(escaped) + "</tool_call>";
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
}
