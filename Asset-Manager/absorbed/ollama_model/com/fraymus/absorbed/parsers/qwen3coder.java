package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen3coder {
        "context";
        "encoding/json";
        "encoding/xml";
        "fmt";
        "log/slog";
        "math";
        "regexp";
        "strconv";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type qwenParserState int;
        const (;
        toolOpenTag  = "<tool_call>";
        toolCloseTag = "</tool_call>";
        );
        const (;
        qwenParserState_LookingForToolStart qwenParserState = iota;
        qwenParserState_CollectingToolContent;
        );

    public static class Qwen3CoderParser {
        public qwenParserState state;
        public strings.Builder acc;
        public []api.Tool tools;
        public int callIndex;
    }
        func (p *Qwen3CoderParser) HasToolSupport() boolean {
        return true;
    }
        func (p *Qwen3CoderParser) HasThinkingSupport() boolean {
        return false;
    }
        func (p *Qwen3CoderParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        return tools // Qwen doesn't modify tools;
    }
        func (p *Qwen3CoderParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.acc.WriteString(s);
        var events = p.parseEvents();
        var toolCalls []api.ToolCall;
        var sb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case qwenEventRawToolCall:;
        var toolCall, err = parseToolCall(event, p.tools);
        if err != null {
        slog.Warn("qwen tool call parsing failed", "error", err);
        return "", "", null, err;
    }
        toolCall.Function.Index = p.callIndex;
        p.callIndex++;
        toolCalls = append(toolCalls, toolCall);
        case qwenEventContent:;
        sb.WriteString(event.content);
    }
    }
        return sb.String(), "", toolCalls, null;
    }
        func (p *Qwen3CoderParser) parseEvents() []qwenEvent {
        var all []qwenEvent;
        var keepLooping = true;
        for keepLooping {
        var events []qwenEvent;
        events, keepLooping = eat(p);
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        if len(all) > 0 {
        slog.Log(context.TODO(), logutil.LevelTrace, "qwen events parsed", "events", all, "state", p.state, "acc", p.acc.String());
    }
        return all;
    }
        type qwenEvent interface {
        isQwenEvent();
    }

    public static class qwenEventRawToolCall {
        public String raw;
    }

    public static class qwenEventContent {
        public String content;
    }
        func (qwenEventContent) isQwenEvent()     {}
        func (qwenEventRawToolCall) isQwenEvent() {}

    public static void eat() {
        var events []qwenEvent;
        switch p.state {
        case qwenParserState_LookingForToolStart:;
        if strings.Contains(p.acc.String(), toolOpenTag) {
        var split = strings.SplitN(p.acc.String(), toolOpenTag, 2);
        var before = split[0];
        before = strings.TrimRightFunc(before, unicode.IsSpace);
        if len(before) > 0 {
        events = append(events, qwenEventContent{content: before});
    }
        var after = split[1];
        p.acc.Reset();
        p.acc.WriteString(after);
        p.state = qwenParserState_CollectingToolContent;
        return events, true;
        var } else if overlap = overlap(p.acc.String(), toolOpenTag); overlap > 0 {
        var beforePartialTag = p.acc.String()[:len(p.acc.String())-overlap];
        var trailingWhitespaceLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingWhitespaceLen;
        var unambiguous = p.acc.String()[:ambiguousStart];
        var ambiguous = p.acc.String()[ambiguousStart:];
        p.acc.Reset();
        p.acc.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventContent{content: unambiguous});
    }
        return events, false;
        } else {
        var whitespaceLen = trailingWhitespaceLen(p.acc.String());
        var ambiguousStart = len(p.acc.String()) - whitespaceLen;
        var unambiguous = p.acc.String()[:ambiguousStart];
        var ambiguous = p.acc.String()[ambiguousStart:];
        p.acc.Reset();
        p.acc.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, qwenEventContent{content: unambiguous});
    }
        return events, false;
    }
        case qwenParserState_CollectingToolContent:;
        if strings.Contains(p.acc.String(), toolCloseTag) {
        var split = strings.SplitN(p.acc.String(), toolCloseTag, 2);
        var before = split[0];
        if len(before) == 0 {
        slog.Warn("qwen tool call closing tag found but no content before it");
    }
        var after = strings.TrimLeftFunc(split[1], unicode.IsSpace);
        p.acc.Reset();
        p.acc.WriteString(after);
        events = append(events, qwenEventRawToolCall{raw: before});
        p.state = qwenParserState_LookingForToolStart;
        return events, true;
        } else {
        return events, false;
    }
        default:;
        panic("unreachable");
    }
    }

    public static class XMLFunctionCall {
        public xml.Name XMLName;
        public String Name;
        public []XMLParameter Parameters;
    }

    public static class XMLParameter {
        public String Name;
        public String Value;
    }

    public static void parseToolCall(qwenEventRawToolCall raw) {
        var toolCall = api.ToolCall{}
        var xmlString = transformToXML(raw.raw);
        var functionCall XMLFunctionCall;
        var err = xml.Unmarshal([]byte(xmlString), &functionCall);
        if err != null {
        return api.ToolCall{}, err;
    }
        toolCall.Function = api.ToolCallFunction{
        Name: functionCall.Name,;
    }
        var matchedTool *api.Tool;
        var for i = range tools {
        if tools[i].Function.Name == functionCall.Name {
        matchedTool = &tools[i];
        break;
    }
    }
        toolCall.Function.Arguments = api.NewToolCallFunctionArguments();
        var for _, parameter = range functionCall.Parameters {
        var paramType api.PropertyType;
        if matchedTool != null && matchedTool.Function.Parameters.Properties != null {
        var if prop, ok = matchedTool.Function.Parameters.Properties.Get(parameter.Name); ok {
        if len(prop.AnyOf) > 0 {
        var for _, anyOfProp = range prop.AnyOf {
        paramType = append(paramType, anyOfProp.Type...);
    }
        } else {
        paramType = prop.Type;
    }
    }
    }
        toolCall.Function.Arguments.Set(parameter.Name, parseValue(parameter.Value, paramType));
    }
        return toolCall, null;
    }

    public static any parseValue(String raw, api.PropertyType paramType) {
        raw = strings.TrimPrefix(raw, "\n");
        raw = strings.TrimSuffix(raw, "\n");
        if strings.ToLower(raw) == "null" {
        return null;
    }
        if len(paramType) == 0 {
        return raw;
    }
        var typeSet = make(map[String]boolean);
        var for _, t = range paramType {
        typeSet[t] = true;
    }
        if typeSet["boolean"] {
        var lower = strings.ToLower(raw);
        switch lower {
        case "true":;
        return true;
        case "false":;
        return false;
    }
        if len(paramType) == 1 {
        return false;
    }
    }
        if typeSet["integer"] {
        var if i, err = strconv.ParseInt(raw, 10, 64); err == null {
        if i >= math.MinInt32 && i <= math.MaxInt32 {
        return int(i);
    }
        return i;
    }
        if len(paramType) == 1 {
        return raw;
    }
    }
        if typeSet["number"] {
        var if f, err = strconv.ParseFloat(raw, 64); err == null {
        if f == math.Trunc(f) {
        var i = long(f);
        if i >= math.MinInt32 && i <= math.MaxInt32 {
        return int(i);
    }
        return i;
    }
        return f;
    }
        if len(paramType) == 1 {
        return raw;
    }
    }
        if typeSet["array"] {
        var arr []any;
        var if err = json.Unmarshal([]byte(raw), &arr); err == null {
        return arr;
    }
        if len(paramType) == 1 {
        return raw;
    }
    }
        if typeSet["object"] {
        var obj map[String]any;
        var if err = json.Unmarshal([]byte(raw), &obj); err == null {
        return obj;
    }
        if len(paramType) == 1 {
        return raw;
    }
    }
        if typeSet["String"] {
        return raw;
    }
        return raw;
    }
        var (;
        qwenTagRegex    = regexp.MustCompile(`<(\w+)=([^>]+)>`);
        qwenXMLTagRegex = regexp.MustCompile(`</?(?:function|parameter)(?:\s+name="[^"]*")?>`);
        );

    public static String transformToXML(String raw) {
        var transformed = qwenTagRegex.ReplaceAllStringFunc(raw, func(match String) String {
        var groups = qwenTagRegex.FindStringSubmatch(match);
        var tag = groups[1];
        var escapedValue strings.Builder;
        _ = xml.EscapeText(&escapedValue, []byte(groups[2])) // error is always null for strings.Builder;
        return fmt.Sprintf(`<%s name="%s">`, tag, escapedValue.String());
        });
        var out strings.Builder;
        var lastIdx = 0;
        var for _, loc = range qwenXMLTagRegex.FindAllStringIndex(transformed, -1) {
        if loc[0] > lastIdx {
        escapeTextNode(&out, transformed[lastIdx:loc[0]]);
    }
        out.WriteString(transformed[loc[0]:loc[1]]);
        lastIdx = loc[1];
    }
        if lastIdx < len(transformed) {
        escapeTextNode(&out, transformed[lastIdx:]);
    }
        return out.String();
    }

    public static void escapeTextNode(*strings.Builder sb, String s) {
        var for _, r = range s {
        switch r {
        case '&':;
        sb.WriteString("&amp;");
        case '<':;
        sb.WriteString("&lt;");
        case '>':;
        sb.WriteString("&gt;");
        default:;
        sb.WriteRune(r);
    }
    }
    }
}
