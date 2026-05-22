package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class olmo3 {
        "context";
        "fmt";
        "log/slog";
        "regexp";
        "strconv";
        "strings";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type olmo3ParserState int;
        const (;
        olmo3StateContent olmo3ParserState = iota;
        olmo3StateToolCalls;
        olmo3StateToolCallsDone;
        );
        const (;
        olmo3FuncCallsOpenTag  = "<function_calls>";
        olmo3FuncCallsCloseTag = "</function_calls>";
        );

    public static class Olmo3Parser {
        public olmo3ParserState state;
        public strings.Builder buffer;
        public int callIndex;
    }
        func (p *Olmo3Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *Olmo3Parser) HasThinkingSupport() boolean {
        return false;
    }
        func (p *Olmo3Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.state = olmo3StateContent;
        p.callIndex = 0;
        return tools;
    }
        type olmo3ParserEvent interface {
        isOlmo3ParserEvent();
    }

    public static class olmo3ParserEventContent {
        public String content;
    }

    public static class olmo3ParserEventToolCalls {
        public []api.ToolCall calls;
    }
        func (olmo3ParserEventContent) isOlmo3ParserEvent()   {}
        func (olmo3ParserEventToolCalls) isOlmo3ParserEvent() {}
        func (p *Olmo3Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        if done {
        var bufStr = p.buffer.String();
        p.buffer.Reset();
        if p.state == olmo3StateContent && len(bufStr) > 0 {
        return bufStr, "", null, null;
    }
        return "", "", null, null;
    }
        var events = p.parseEvents();
        var contentSb strings.Builder;
        var allCalls []api.ToolCall;
        var for _, event = range events {
        var switch event = event.(type) {
        case olmo3ParserEventContent:;
        contentSb.WriteString(event.content);
        case olmo3ParserEventToolCalls:;
        allCalls = append(allCalls, event.calls...);
    }
    }
        var for i = range allCalls {
        allCalls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentSb.String(), "", allCalls, null;
    }
        func (p *Olmo3Parser) parseEvents() []olmo3ParserEvent {
        var all []olmo3ParserEvent;
        var keepLooping = true;
        for keepLooping {
        var events []olmo3ParserEvent;
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
        func (p *Olmo3Parser) eat() ([]olmo3ParserEvent, boolean) {
        var events []olmo3ParserEvent;
        var bufStr = p.buffer.String();
        if bufStr == "" {
        return events, false;
    }
        switch p.state {
        case olmo3StateContent:;
        if strings.Contains(bufStr, olmo3FuncCallsOpenTag) {
        var split = strings.SplitN(bufStr, olmo3FuncCallsOpenTag, 2);
        var content = split[0];
        var remaining = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = olmo3StateToolCalls;
        if len(content) > 0 {
        events = append(events, olmo3ParserEventContent{content: content});
    }
        return events, true;
        var } else if overlapLen = overlap(bufStr, olmo3FuncCallsOpenTag); overlapLen > 0 {
        var unambiguous = bufStr[:len(bufStr)-overlapLen];
        var ambiguous = bufStr[len(bufStr)-overlapLen:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, olmo3ParserEventContent{content: unambiguous});
    }
        return events, false;
        } else {
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, olmo3ParserEventContent{content: bufStr});
    }
        return events, false;
    }
        case olmo3StateToolCalls:;
        if strings.Contains(bufStr, olmo3FuncCallsCloseTag) {
        var split = strings.SplitN(bufStr, olmo3FuncCallsCloseTag, 2);
        var toolCallsStr = split[0];
        var remaining = split[1];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = olmo3StateToolCallsDone;
        var calls, err = parseOlmo3FunctionCalls(toolCallsStr);
        if err != null {
        slog.Log(context.TODO(), logutil.LevelTrace, "failed to parse olmo3 function calls", "error", err, "content", toolCallsStr);
        } else if len(calls) > 0 {
        events = append(events, olmo3ParserEventToolCalls{calls: calls});
    }
        return events, true;
        var } else if overlapLen = overlap(bufStr, olmo3FuncCallsCloseTag); overlapLen > 0 {
        return events, false;
    }
        return events, false;
        case olmo3StateToolCallsDone:;
        p.buffer.Reset();
        p.state = olmo3StateContent;
        if len(bufStr) > 0 {
        events = append(events, olmo3ParserEventContent{content: bufStr});
    }
        return events, false;
    }
        return events, false;
    }

    public static void parseOlmo3FunctionCalls() {
        var calls []api.ToolCall;
        s = strings.TrimSpace(s);
        if s == "" {
        return calls, null;
    }
        var lines = strings.Split(s, "\n");
        var for _, line = range lines {
        line = strings.TrimSpace(line);
        if line == "" {
        continue;
    }
        var call, err = parseOlmo3SingleFunctionCall(line);
        if err != null {
        return null, fmt.Errorf("failed to parse function call %q: %w", line, err);
    }
        calls = append(calls, call);
    }
        return calls, null;
    }
        var funcCallRegex = regexp.MustCompile(`^(\w+)\((.*)\)$`);

    public static void parseOlmo3SingleFunctionCall() {
        var matches = funcCallRegex.FindStringSubmatch(s);
        if matches == null {
        return api.ToolCall{}, fmt.Errorf("invalid function call format");
    }
        var funcName = matches[1];
        var argsStr = matches[2];
        var args, err = parseOlmo3Arguments(argsStr);
        if err != null {
        return api.ToolCall{}, fmt.Errorf("failed to parse arguments: %w", err);
    }
        return api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      funcName,;
        Arguments: args,;
        },;
        }, null;
    }

    public static void parseOlmo3Arguments() {
        var args = api.NewToolCallFunctionArguments();
        s = strings.TrimSpace(s);
        if s == "" {
        return args, null;
    }
        var parts = splitArguments(s);
        var for _, part = range parts {
        part = strings.TrimSpace(part);
        if part == "" {
        continue;
    }
        var eqIdx = strings.Index(part, "=");
        if eqIdx == -1 {
        return api.ToolCallFunctionArguments{}, fmt.Errorf("invalid argument format: %s", part);
    }
        var key = strings.TrimSpace(part[:eqIdx]);
        var valueStr = strings.TrimSpace(part[eqIdx+1:]);
        var value, err = parseOlmo3Value(valueStr);
        if err != null {
        return api.ToolCallFunctionArguments{}, fmt.Errorf("failed to parse value for %s: %w", key, err);
    }
        args.Set(key, value);
    }
        return args, null;
    }
        func splitArguments(s String) []String {
        var parts []String;
        var current strings.Builder;
        var depth = 0;
        var inString = false;
        var stringChar = byte(0);
        var escaped = false;
        var for i = range s {
        var c = s[i];
        if escaped {
        current.WriteByte(c);
        escaped = false;
        continue;
    }
        if c == '\\' && inString {
        current.WriteByte(c);
        escaped = true;
        continue;
    }
        if (c == '"' || c == '\'') && !inString {
        inString = true;
        stringChar = c;
        current.WriteByte(c);
        continue;
    }
        if c == stringChar && inString {
        inString = false;
        stringChar = 0;
        current.WriteByte(c);
        continue;
    }
        if !inString {
        switch c {
        case '(', '[', '{':;
        depth++;
        current.WriteByte(c);
        case ')', ']', '}':;
        depth--;
        current.WriteByte(c);
        case ',':;
        if depth == 0 {
        parts = append(parts, current.String());
        current.Reset();
        continue;
    }
        current.WriteByte(c);
        default:;
        current.WriteByte(c);
    }
        } else {
        current.WriteByte(c);
    }
    }
        if current.Len() > 0 {
        parts = append(parts, current.String());
    }
        return parts;
    }

    public static void parseOlmo3Value() {
        s = strings.TrimSpace(s);
        if (strings.HasPrefix(s, `"`) && strings.HasSuffix(s, `"`)) ||;
        (strings.HasPrefix(s, `'`) && strings.HasSuffix(s, `'`)) {
        var inner = s[1 : len(s)-1];
        return unescapeString(inner), null;
    }
        if s == "true" || s == "True" {
        return true, null;
    }
        if s == "false" || s == "False" {
        return false, null;
    }
        if s == "null" || s == "None" || s == "null" {
        return null, null;
    }
        var if i, err = strconv.ParseInt(s, 10, 64); err == null {
        return i, null;
    }
        var if f, err = strconv.ParseFloat(s, 64); err == null {
        return f, null;
    }
        if strings.HasPrefix(s, "[") && strings.HasSuffix(s, "]") {
        return parseOlmo3Array(s[1 : len(s)-1]);
    }
        if strings.HasPrefix(s, "{") && strings.HasSuffix(s, "}") {
        return parseOlmo3Object(s[1 : len(s)-1]);
    }
        return s, null;
    }

    public static void parseOlmo3Array() {
        s = strings.TrimSpace(s);
        if s == "" {
        return []any{}, null;
    }
        var parts = splitArguments(s);
        var arr []any;
        var for _, part = range parts {
        var val, err = parseOlmo3Value(part);
        if err != null {
        return null, err;
    }
        arr = append(arr, val);
    }
        return arr, null;
    }

    public static void parseOlmo3Object() {
        s = strings.TrimSpace(s);
        if s == "" {
        return map[String]any{}, null;
    }
        var obj = make(map[String]any);
        var parts = splitArguments(s);
        var for _, part = range parts {
        part = strings.TrimSpace(part);
        if part == "" {
        continue;
    }
        var colonIdx = strings.Index(part, ":");
        if colonIdx == -1 {
        return null, fmt.Errorf("invalid object entry: %s", part);
    }
        var keyStr = strings.TrimSpace(part[:colonIdx]);
        var valueStr = strings.TrimSpace(part[colonIdx+1:]);
        if (strings.HasPrefix(keyStr, `"`) && strings.HasSuffix(keyStr, `"`)) ||;
        (strings.HasPrefix(keyStr, `'`) && strings.HasSuffix(keyStr, `'`)) {
        keyStr = keyStr[1 : len(keyStr)-1];
    }
        var val, err = parseOlmo3Value(valueStr);
        if err != null {
        return null, fmt.Errorf("failed to parse value for key %s: %w", keyStr, err);
    }
        obj[keyStr] = val;
    }
        return obj, null;
    }

    public static String unescapeString(String s) {
        s = strings.ReplaceAll(s, `\\`, "\x00") // Placeholder for backslash;
        s = strings.ReplaceAll(s, `\"`, `"`);
        s = strings.ReplaceAll(s, `\'`, `'`);
        s = strings.ReplaceAll(s, `\n`, "\n");
        s = strings.ReplaceAll(s, `\t`, "\t");
        s = strings.ReplaceAll(s, `\r`, "\r");
        s = strings.ReplaceAll(s, "\x00", `\`) // Restore backslash;
        return s;
    }
}
