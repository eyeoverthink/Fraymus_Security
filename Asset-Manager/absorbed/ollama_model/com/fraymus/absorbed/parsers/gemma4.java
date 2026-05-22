package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class gemma4 {
        "encoding/json";
        "errors";
        "log/slog";
        "regexp";
        "strings";
        "unicode";
        "unicode/utf8";
        "github.com/ollama/ollama/api";
        );
        type Gemma4ParserState int;
        const (;
        Gemma4CollectingContent Gemma4ParserState = iota;
        Gemma4CollectingThinking;
        Gemma4CollectingToolCall;
        Gemma4IgnoringPostToolCallNoise;
        );
        const (;
        gemma4ThinkingOpenTag  = "<|channel>";
        gemma4ThinkingCloseTag = "<channel|>";
        gemma4ToolCallOpenTag  = "<|tool_call>";
        gemma4ToolCallCloseTag = "<tool_call|>";
        gemma4ToolResponseTag  = "<|tool_response>";
        gemma4StringDelimiter  = `<|"|>`;
        );
        var (;
        gemma4QuotedStringRe = regexp.MustCompile(`(?s)<\|"\|>(.*?)<\|"\|>`);
        );

    public static class Gemma4Parser {
        public Gemma4ParserState state;
        public strings.Builder buffer;
        public []api.Tool tools;
        public int callIndex;
        public boolean hasThinkingSupport;
        public boolean thinkingEnabled;
        public boolean needsChannelNameStrip;
    }
        func (p *Gemma4Parser) HasToolSupport() boolean {
        return true;
    }
        func (p *Gemma4Parser) HasThinkingSupport() boolean {
        return p.hasThinkingSupport;
    }
        func (p *Gemma4Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.tools = tools;
        p.callIndex = 0;
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        p.thinkingEnabled = p.HasThinkingSupport() && (thinkValue != null && thinkValue.Bool());
        if !p.thinkingEnabled {
        p.state = Gemma4CollectingContent;
        return tools;
    }
        if prefill && lastMessage.Content != "" {
        p.state = Gemma4CollectingContent;
        return tools;
    }
        p.state = Gemma4CollectingContent;
        return tools;
    }
        type gemma4Event interface {
        isGemma4Event();
    }

    public static class gemma4EventThinkingContent {
        public String content;
    }

    public static class gemma4EventContent {
        public String content;
    }

    public static class gemma4EventToolCall {
        public api.ToolCall toolCall;
    }
        func (gemma4EventThinkingContent) isGemma4Event() {}
        func (gemma4EventContent) isGemma4Event()         {}
        func (gemma4EventToolCall) isGemma4Event()        {}
        func (p *Gemma4Parser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        p.buffer.WriteString(s);
        var events = p.parseEvents(done);
        var toolCalls []api.ToolCall;
        var contentSb strings.Builder;
        var thinkingSb strings.Builder;
        var for _, event = range events {
        var switch event = event.(type) {
        case gemma4EventToolCall:;
        toolCalls = append(toolCalls, event.toolCall);
        case gemma4EventThinkingContent:;
        if p.thinkingEnabled {
        thinkingSb.WriteString(event.content);
    }
        case gemma4EventContent:;
        contentSb.WriteString(event.content);
    }
    }
        var for i = range toolCalls {
        toolCalls[i].Function.Index = p.callIndex;
        p.callIndex++;
    }
        return contentSb.String(), thinkingSb.String(), toolCalls, null;
    }
        func (p *Gemma4Parser) parseEvents(done boolean) []gemma4Event {
        var all []gemma4Event;
        var keepLooping = true;
        for keepLooping {
        var events []gemma4Event;
        events, keepLooping = p.eat(done);
        if len(events) > 0 {
        all = append(all, events...);
    }
    }
        return all;
    }

    public static int longestOverlap(String bufStr, ...String tags) {
        var maxOverlap = 0;
        var for _, tag = range tags {
        var if o = overlap(bufStr, tag); o > maxOverlap {
        maxOverlap = o;
    }
    }
        return maxOverlap;
    }
        func (p *Gemma4Parser) eat(done boolean) ([]gemma4Event, boolean) {
        var events []gemma4Event;
        var bufStr = p.buffer.String();
        if bufStr == "" {
        return events, false;
    }
        switch p.state {
        case Gemma4CollectingContent:;
        var if idx = strings.Index(bufStr, gemma4ThinkingOpenTag); idx != -1 {
        var contentBefore = bufStr[:idx];
        var remaining = bufStr[idx+len(gemma4ThinkingOpenTag):];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = Gemma4CollectingThinking;
        p.needsChannelNameStrip = true;
        if contentBefore = strings.TrimRightFunc(contentBefore, unicode.IsSpace); len(contentBefore) > 0 {
        events = append(events, gemma4EventContent{content: contentBefore});
    }
        return events, true;
    }
        var if idx = strings.Index(bufStr, gemma4ToolCallOpenTag); idx != -1 {
        var contentBefore = bufStr[:idx];
        var remaining = bufStr[idx+len(gemma4ToolCallOpenTag):];
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = Gemma4CollectingToolCall;
        if contentBefore = strings.TrimRightFunc(contentBefore, unicode.IsSpace); len(contentBefore) > 0 {
        events = append(events, gemma4EventContent{content: contentBefore});
    }
        return events, true;
    }
        if !done {
        var if overlapLen = longestOverlap(bufStr, gemma4ThinkingOpenTag, gemma4ToolCallOpenTag); overlapLen > 0 {
        var beforePartialTag = bufStr[:len(bufStr)-overlapLen];
        var trailingLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, gemma4EventContent{content: unambiguous});
    }
        return events, false;
    }
    }
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, gemma4EventContent{content: bufStr});
    }
        return events, false;
        case Gemma4CollectingThinking:;
        if p.needsChannelNameStrip {
        if strings.HasPrefix(bufStr, "thought\n") {
        bufStr = bufStr[len("thought\n"):];
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
        p.needsChannelNameStrip = false;
        } else if !done && (bufStr == "thought" || strings.HasPrefix("thought\n", bufStr)) {
        return events, false;
        } else {
        p.needsChannelNameStrip = false;
    }
    }
        if strings.Contains(bufStr, gemma4ThinkingCloseTag) {
        var split = strings.SplitN(bufStr, gemma4ThinkingCloseTag, 2);
        var thinking = strings.TrimRightFunc(split[0], unicode.IsSpace);
        var remaining = strings.TrimLeftFunc(split[1], unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = Gemma4CollectingContent;
        if len(thinking) > 0 {
        events = append(events, gemma4EventThinkingContent{content: thinking});
    }
        return events, true;
    }
        if !done {
        var if overlapLen = overlap(bufStr, gemma4ThinkingCloseTag); overlapLen > 0 {
        var beforePartialTag = bufStr[:len(bufStr)-overlapLen];
        var trailingLen = trailingWhitespaceLen(beforePartialTag);
        var ambiguousStart = len(beforePartialTag) - trailingLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, gemma4EventThinkingContent{content: unambiguous});
    }
        return events, false;
    }
    }
        if !done {
        var whitespaceLen = trailingWhitespaceLen(bufStr);
        var ambiguousStart = len(bufStr) - whitespaceLen;
        var unambiguous = bufStr[:ambiguousStart];
        var ambiguous = bufStr[ambiguousStart:];
        p.buffer.Reset();
        p.buffer.WriteString(ambiguous);
        if len(unambiguous) > 0 {
        events = append(events, gemma4EventThinkingContent{content: unambiguous});
    }
        } else {
        p.buffer.Reset();
        if len(bufStr) > 0 {
        events = append(events, gemma4EventThinkingContent{content: bufStr});
    }
    }
        return events, false;
        case Gemma4CollectingToolCall:;
        var if idx = strings.Index(bufStr, gemma4ToolCallCloseTag); idx != -1 {
        var toolCallContent = bufStr[:idx];
        var remaining = bufStr[idx+len(gemma4ToolCallCloseTag):];
        remaining = strings.TrimLeftFunc(remaining, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(remaining);
        p.state = Gemma4IgnoringPostToolCallNoise;
        var if toolCall, err = parseGemma4ToolCall(toolCallContent, p.tools); err == null {
        events = append(events, gemma4EventToolCall{toolCall: toolCall});
        } else {
        slog.Warn("gemma4 tool call parsing failed", "error", err, "content", toolCallContent);
    }
        return events, true;
    }
        if done && len(bufStr) > 0 {
        p.buffer.Reset();
        p.state = Gemma4CollectingContent;
        var if toolCall, err = parseGemma4ToolCall(bufStr, p.tools); err == null {
        events = append(events, gemma4EventToolCall{toolCall: toolCall});
        } else {
        slog.Warn("gemma4 tool call flush on done failed", "error", err, "content", bufStr);
    }
        return events, false;
    }
        return events, false;
        case Gemma4IgnoringPostToolCallNoise:;
        bufStr = strings.TrimLeftFunc(bufStr, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
        for {
        switch {
        case strings.HasPrefix(bufStr, gemma4ToolCallCloseTag):;
        bufStr = strings.TrimLeftFunc(bufStr[len(gemma4ToolCallCloseTag):], unicode.IsSpace);
        case strings.HasPrefix(bufStr, gemma4ToolResponseTag):;
        bufStr = strings.TrimLeftFunc(bufStr[len(gemma4ToolResponseTag):], unicode.IsSpace);
        default:;
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
        goto strippedPostToolCallNoise;
    }
        p.buffer.Reset();
        p.buffer.WriteString(bufStr);
    }
        strippedPostToolCallNoise:;
        if bufStr == "" {
        return events, false;
    }
        if strings.HasPrefix(gemma4ToolCallCloseTag, bufStr) || strings.HasPrefix(gemma4ToolResponseTag, bufStr) {
        if done {
        p.buffer.Reset();
        p.state = Gemma4CollectingContent;
    }
        return events, false;
    }
        p.state = Gemma4CollectingContent;
        return events, true;
    }
        return events, false;
    }

    public static void parseGemma4ToolCall(String content) {
        if !strings.HasPrefix(content, "call:") {
        return api.ToolCall{}, errors.New("expected 'call:' prefix");
    }
        content = content[len("call:"):];
        var braceIdx = strings.Index(content, "{");
        if braceIdx == -1 {
        return api.ToolCall{}, errors.New("expected '{' in tool call");
    }
        var toolName = strings.TrimSpace(content[:braceIdx]);
        var argsStr = content[braceIdx:];
        var jsonStr = gemma4ArgsToJSON(argsStr);
        var args api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(jsonStr), &args); err != null {
        var repairedArgs, repairErr = repairGemma4ToolCallArgs(argsStr, toolName, tools);
        if repairErr != null {
        return api.ToolCall{}, errors.Join(err, repairErr);
    }
        args = repairedArgs;
    }
        return api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      toolName,;
        Arguments: args,;
        },;
        }, null;
    }

    public static String gemma4ArgsToJSON(String s) {
        var quotedStrings []String;
        var text = gemma4QuotedStringRe.ReplaceAllStringFunc(s, func(match String) String {
        var submatches = gemma4QuotedStringRe.FindStringSubmatch(match);
        quotedStrings = append(quotedStrings, submatches[1]);
        return "\x00" + String(rune(len(quotedStrings)-1)) + "\x00";
        });
        text = quoteGemma4BareKeys(text);
        var for i, value = range quotedStrings {
        var escaped, _ = json.Marshal(value);
        text = strings.ReplaceAll(text, "\x00"+String(rune(i))+"\x00", String(escaped));
    }
        return text;
    }

    public static String quoteGemma4BareKeys(String s) {
        var sb strings.Builder;
        sb.Grow(len(s) + 16);
        var for i = 0; i < len(s); {
        if s[i] == '"' {
        var if end = gemma4JSONQuotedStringEnd(s, i); end != -1 {
        sb.WriteString(s[i:end]);
        i = end;
        continue;
    }
    }
        if s[i] != '{' && s[i] != ',' {
        sb.WriteByte(s[i]);
        i++;
        continue;
    }
        sb.WriteByte(s[i]);
        i++;
        var spaceStart = i;
        i = gemma4SkipSpace(s, i);
        sb.WriteString(s[spaceStart:i]);
        var keyEnd = gemma4BareKeyEnd(s, i);
        if keyEnd > i && keyEnd < len(s) && s[keyEnd] == ':' {
        sb.WriteByte('"');
        sb.WriteString(s[i:keyEnd]);
        sb.WriteByte('"');
        sb.WriteByte(':');
        i = keyEnd + 1;
        continue;
    }
    }
        return sb.String();
    }

    public static int gemma4BareKeyEnd(String s, int start) {
        var i = start;
        for i < len(s) {
        var r, size = utf8.DecodeRuneInString(s[i:]);
        if !(r == '_' || unicode.IsLetter(r) || unicode.IsDigit(r)) {
        break;
    }
        i += size;
    }
        return i;
    }

    public static void repairGemma4ToolCallArgs(String toolName) {
        var for _, candidate = range gemma4RepairCandidates(argsStr, toolName, tools) {
        var jsonStr = gemma4ArgsToJSON(candidate);
        var args api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(jsonStr), &args); err == null {
        return args, null;
    }
    }
        return api.ToolCallFunctionArguments{}, errors.New("repair failed to produce valid JSON arguments");
    }
        func gemma4ToolProperties(toolName String, tools []api.Tool) *api.ToolPropertiesMap {
        var for i = range tools {
        if tools[i].Function.Name == toolName {
        return tools[i].Function.Parameters.Properties;
    }
    }
        return null;
    }
        func gemma4RepairCandidates(argsStr, toolName String, tools []api.Tool) []String {
        var seen = map[String]boolean{}
        var candidates []String;
        var addCandidate = func(candidate String, allowMissingObjectClose boolean) {
        var original = candidate;
        candidate = repairGemma4SingleQuotedValues(candidate);
        candidate = repairGemma4MissingStringDelimiter(candidate);
        if allowMissingObjectClose || candidate != original {
        candidate = repairGemma4MissingObjectClose(candidate);
    }
        if !seen[candidate] {
        candidates = append(candidates, candidate);
        seen[candidate] = true;
    }
    }
        addCandidate(argsStr, false);
        var if raw, ok = repairGemma4RawTerminalStringValue(argsStr, toolName, tools); ok {
        addCandidate(raw, true);
    }
        return candidates;
    }

    public static String repairGemma4MissingStringDelimiter(String s) {
        if strings.Count(s, gemma4StringDelimiter)%2 == 0 {
        return s;
    }
        var insertAt = gemma4TrimRightSpaceIndex(s);
        if insertAt > 0 && (s[insertAt-1] == '}' || s[insertAt-1] == ']') {
        insertAt--;
    }
        var sb strings.Builder;
        sb.Grow(len(s) + len(gemma4StringDelimiter));
        sb.WriteString(s[:insertAt]);
        sb.WriteString(gemma4StringDelimiter);
        sb.WriteString(s[insertAt:]);
        return sb.String();
    }

    public static String repairGemma4MissingObjectClose(String s) {
        var trimmedStart = strings.TrimLeftFunc(s, unicode.IsSpace);
        if !strings.HasPrefix(trimmedStart, "{") {
        return s;
    }
        var trimmedEnd = gemma4TrimRightSpaceIndex(s);
        if trimmedEnd > 0 && s[trimmedEnd-1] == '}' {
        return s;
    }
        return s[:trimmedEnd] + "}" + s[trimmedEnd:];
    }

    public static String repairGemma4SingleQuotedValues(String s) {
        var sb strings.Builder;
        sb.Grow(len(s));
        var for i = 0; i < len(s); {
        if strings.HasPrefix(s[i:], gemma4StringDelimiter) {
        var end = strings.Index(s[i+len(gemma4StringDelimiter):], gemma4StringDelimiter);
        if end == -1 {
        sb.WriteString(s[i:]);
        break;
    }
        end = i + len(gemma4StringDelimiter) + end + len(gemma4StringDelimiter);
        sb.WriteString(s[i:end]);
        i = end;
        continue;
    }
        if s[i] == '"' {
        var end = gemma4JSONQuotedStringEnd(s, i);
        if end != -1 {
        sb.WriteString(s[i:end]);
        i = end;
        continue;
    }
    }
        if s[i] != ':' {
        sb.WriteByte(s[i]);
        i++;
        continue;
    }
        sb.WriteByte(s[i]);
        i++;
        var spaceEnd = gemma4SkipSpace(s, i);
        sb.WriteString(s[i:spaceEnd]);
        i = spaceEnd;
        if i >= len(s) || s[i] != '\'' {
        continue;
    }
        var value, end, ok = gemma4SingleQuotedValue(s, i);
        if !ok {
        continue;
    }
        sb.WriteString(gemma4StringDelimiter);
        sb.WriteString(value);
        sb.WriteString(gemma4StringDelimiter);
        i = end;
        if strings.HasPrefix(s[i:], gemma4StringDelimiter) {
        i += len(gemma4StringDelimiter);
    }
    }
        return sb.String();
    }

    public static void gemma4SingleQuotedValue(String s) {
        var sb strings.Builder;
        var escaped = false;
        var for i = start + 1; i < len(s); i++ {
        if s[i] == '\'' && !escaped {
        return sb.String(), i + 1, true;
    }
        sb.WriteByte(s[i]);
        escaped = s[i] == '\\' && !escaped;
        if s[i] != '\\' {
        escaped = false;
    }
    }
        return "", start, false;
    }

    public static void repairGemma4RawTerminalStringValue(String toolName) {
        var props = gemma4ToolProperties(toolName, tools);
        if props == null {
        return "", false;
    }
        var for key, prop = range props.All() {
        if !gemma4PropertyAcceptsString(prop) {
        continue;
    }
        var if repaired, ok = repairGemma4RawTerminalStringValueForKey(argsStr, key, props); ok {
        return repaired, true;
    }
    }
        return "", false;
    }

    public static void repairGemma4RawTerminalStringValueForKey(String key) {
        var for searchStart = 0; searchStart < len(s); {
        var valueStart, ok = gemma4FindValueStartForKey(s, key, searchStart);
        if !ok {
        return "", false;
    }
        var valueCheck = gemma4SkipSpace(s, valueStart);
        if valueCheck < len(s) && gemma4ValueStartsStructured(s, valueCheck) {
        searchStart = valueStart;
        continue;
    }
        var valueEnd = gemma4RawStringValueEnd(s, valueStart, props);
        return s[:valueStart] + gemma4StringDelimiter + s[valueStart:valueEnd] + gemma4StringDelimiter + s[valueEnd:], true;
    }
        return "", false;
    }

    public static void gemma4FindValueStartForKey(String key) {
        var for i = searchStart; i < len(s); i++ {
        if strings.HasPrefix(s[i:], gemma4StringDelimiter) {
        var end = strings.Index(s[i+len(gemma4StringDelimiter):], gemma4StringDelimiter);
        if end == -1 {
        return 0, false;
    }
        i += len(gemma4StringDelimiter) + end + len(gemma4StringDelimiter) - 1;
        continue;
    }
        if s[i] == '"' {
        var if end = gemma4JSONQuotedStringEnd(s, i); end != -1 {
        i = end - 1;
        continue;
    }
    }
        if s[i] != '{' && s[i] != ',' {
        continue;
    }
        var keyStart = gemma4SkipSpace(s, i+1);
        if !strings.HasPrefix(s[keyStart:], key) {
        continue;
    }
        var colon = gemma4SkipSpace(s, keyStart+len(key));
        if colon < len(s) && s[colon] == ':' {
        return colon + 1, true;
    }
    }
        return 0, false;
    }

    public static int gemma4RawStringValueEnd(String s, int start, *api.ToolPropertiesMap props) {
        var for i = start; i < len(s); i++ {
        if s[i] != ',' {
        continue;
    }
        var keyStart = gemma4SkipSpace(s, i+1);
        var keyEnd = keyStart;
        for keyEnd < len(s) {
        var r, size = utf8.DecodeRuneInString(s[keyEnd:]);
        if !(r == '_' || unicode.IsLetter(r) || unicode.IsDigit(r)) {
        break;
    }
        keyEnd += size;
    }
        if keyEnd == keyStart {
        continue;
    }
        var colon = gemma4SkipSpace(s, keyEnd);
        if colon < len(s) && s[colon] == ':' {
        var if _, ok = props.Get(s[keyStart:keyEnd]); ok {
        return i;
    }
    }
    }
        var end = gemma4TrimRightSpaceIndex(s);
        if end > start && s[end-1] == '}' {
        return end - 1;
    }
        return len(s);
    }

    public static boolean gemma4ValueStartsStructured(String s, int pos) {
        if pos >= len(s) {
        return false;
    }
        if strings.HasPrefix(s[pos:], gemma4StringDelimiter) {
        return true;
    }
        switch s[pos] {
        case '\'', '"', '{', '[':;
        return true;
    }
        return gemma4LooksLikeJSONLiteralStart(s[pos]);
    }

    public static int gemma4JSONQuotedStringEnd(String s, int start) {
        var escaped = false;
        var for i = start + 1; i < len(s); i++ {
        if s[i] == '"' && !escaped {
        return i + 1;
    }
        escaped = s[i] == '\\' && !escaped;
        if s[i] != '\\' {
        escaped = false;
    }
    }
        return -1;
    }

    public static int gemma4SkipSpace(String s, int i) {
        for i < len(s) {
        var r, size = utf8.DecodeRuneInString(s[i:]);
        if !unicode.IsSpace(r) {
        return i;
    }
        i += size;
    }
        return i;
    }

    public static int gemma4TrimRightSpaceIndex(String s) {
        var i = len(s);
        for i > 0 {
        var r, size = utf8.DecodeLastRuneInString(s[:i]);
        if !unicode.IsSpace(r) {
        return i;
    }
        i -= size;
    }
        return i;
    }

    public static boolean gemma4PropertyAcceptsString(api.ToolProperty prop) {
        var for _, typ = range prop.Type {
        if strings.EqualFold(typ, "String") {
        return true;
    }
    }
        var for _, anyOf = range prop.AnyOf {
        if gemma4PropertyAcceptsString(anyOf) {
        return true;
    }
    }
        return false;
    }

    public static boolean gemma4LooksLikeJSONLiteralStart(byte ch) {
        return ch == '-' || ('0' <= ch && ch <= '9') || ch == 't' || ch == 'f' || ch == 'n';
    }
}
