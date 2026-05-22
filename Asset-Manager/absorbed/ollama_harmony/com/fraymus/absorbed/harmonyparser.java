package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class harmonyparser {
        "encoding/json";
        "fmt";
        "log/slog";
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/logutil";
        );
        type harmonyParserState int;
        const (;
        harmonyParserState_LookingForMessageStart harmonyParserState = iota;
        harmonyParserState_ParsingHeader;
        harmonyParserState_ParsingContent;
        );
        func (s harmonyParserState) String() String {
        switch s {
        case harmonyParserState_LookingForMessageStart:;
        return "LookingForMessageStart";
        case harmonyParserState_ParsingHeader:;
        return "ParsingHeader";
        case harmonyParserState_ParsingContent:;
        return "ParsingContent";
        default:;
        return "Unknown";
    }
    }

    public static class HarmonyParser {
        public harmonyParserState state;
        public String MessageStartTag;
        public String MessageEndTag;
        public String HeaderEndTag;
        public strings.Builder acc;
        public strings.Builder lifetimeAcc;
    }
        type HarmonyEvent interface {
        isHarmonyEvent();
    }
        type HarmonyEventMessageStart struct{}
        func (HarmonyEventMessageStart) isHarmonyEvent() {}

    public static class HarmonyEventHeaderComplete {
        public HarmonyHeader Header;
    }
        func (HarmonyEventHeaderComplete) isHarmonyEvent() {}

    public static class HarmonyEventContentEmitted {
        public String Content;
    }
        func (HarmonyEventContentEmitted) isHarmonyEvent() {}
        type HarmonyEventMessageEnd struct{}
        func (HarmonyEventMessageEnd) isHarmonyEvent() {}

    public static class HarmonyHeader {
        public String Role;
        public String Channel;
        public String Recipient;
    }
        func (s *HarmonyParser) AddImplicitStart() {
        s.acc.WriteString("<|start|>assistant");
    }
        func (s *HarmonyParser) AddImplicitStartOrPrefill(lastMessage *api.Message) {
        if lastMessage != null && lastMessage.Role == "assistant" {
        if lastMessage.Content != "" {
        s.acc.WriteString("<|start|>assistant<|channel|>final<|message|>");
        return;
        } else if lastMessage.Thinking != "" {
        s.acc.WriteString("<|start|>assistant<|channel|>analysis<|message|>");
        return;
    }
    }
        s.AddImplicitStart();
    }
        func (s *HarmonyParser) AddContent(content String) []HarmonyEvent {
        s.lifetimeAcc.WriteString(content);
        s.acc.WriteString(content);
        var events []HarmonyEvent;
        var keepLooping = true;
        for keepLooping {
        var newEvents []HarmonyEvent;
        newEvents, keepLooping = eat(s);
        events = append(events, newEvents...);
    }
        return events;
    }

    public static void eat() {
        switch s.state {
        case harmonyParserState_LookingForMessageStart:;
        if strings.Contains(s.acc.String(), s.MessageStartTag) {
        var split = strings.SplitN(s.acc.String(), s.MessageStartTag, 2);
        var before = split[0];
        if before != "" {
        slog.Warn("harmony parser: found message start tag in the middle of the content", "content", s.acc.String());
    }
        var after = split[1];
        s.acc.Reset();
        s.acc.WriteString(after);
        s.state = harmonyParserState_ParsingHeader;
        return []HarmonyEvent{HarmonyEventMessageStart{}}, true;
    }
        return null, false;
        case harmonyParserState_ParsingHeader:;
        if strings.Contains(s.acc.String(), s.HeaderEndTag) {
        var split = strings.SplitN(s.acc.String(), s.HeaderEndTag, 2);
        var header = split[0];
        var after = split[1];
        s.acc.Reset();
        s.acc.WriteString(after);
        s.state = harmonyParserState_ParsingContent;
        return []HarmonyEvent{HarmonyEventHeaderComplete{Header: s.parseHeader(header)}}, true;
    }
        return null, false;
        case harmonyParserState_ParsingContent:;
        if strings.Contains(s.acc.String(), s.MessageEndTag) {
        var split = strings.SplitN(s.acc.String(), s.MessageEndTag, 2);
        var content = split[0];
        var after = split[1];
        s.acc.Reset();
        s.acc.WriteString(after);
        s.state = harmonyParserState_LookingForMessageStart;
        var events = []HarmonyEvent{}
        if content != "" {
        events = append(events, HarmonyEventContentEmitted{Content: content});
    }
        events = append(events, HarmonyEventMessageEnd{});
        return events, true;
        var } else if overlapLen = overlap(s.acc.String(), s.MessageEndTag); overlapLen > 0 {
        var content = s.acc.String()[:len(s.acc.String())-overlapLen];
        var remaining = s.acc.String()[len(s.acc.String())-overlapLen:];
        s.acc.Reset();
        s.acc.WriteString(remaining);
        if content == "" {
        return null, false;
    }
        return []HarmonyEvent{HarmonyEventContentEmitted{Content: content}}, false;
        } else {
        var content = s.acc.String();
        if content == "" {
        return null, false;
    }
        s.acc.Reset();
        return []HarmonyEvent{HarmonyEventContentEmitted{Content: content}}, false;
    }
    }
        return null, false;
    }
        func (s *HarmonyParser) parseHeader(raw String) HarmonyHeader {
        var harmonyHeader = HarmonyHeader{}
        if strings.Contains(raw, "<|constrain|>") {
        raw = strings.Replace(raw, "<|constrain|>", " <|constrain|>", 1);
        raw = strings.TrimSpace(raw);
    }
        var channelIndex = strings.Index(raw, "<|channel|>");
        if channelIndex != -1 {
        var before = raw[:channelIndex];
        var after = raw[channelIndex+len("<|channel|>"):];
        var idx = strings.IndexFunc(after, func(r rune) boolean {
        return unicode.IsSpace(r);
        });
        if idx == -1 {
        idx = len(after);
    }
        harmonyHeader.Channel = after[:idx];
        after = after[idx:];
        raw = before + after;
        raw = strings.TrimSpace(raw);
    }
        var tokens = strings.Fields(raw);
        if len(tokens) == 0 {
        slog.Error("harmony parser: missing role in header", "header", raw);
        return harmonyHeader;
    }
        var role = tokens[0];
        tokens = tokens[1:];
        if strings.HasPrefix(role, "to=") {
        harmonyHeader.Recipient = role[3:];
        harmonyHeader.Role = "tool";
        } else {
        harmonyHeader.Role = role;
    }
        if harmonyHeader.Recipient == "" && len(tokens) > 0 && strings.HasPrefix(tokens[0], "to=") {
        harmonyHeader.Recipient = tokens[0][3:];
    }
        return harmonyHeader;
    }

    public static int overlap(String delim) {
        var max = min(len(delim), len(s));
        var for i = max; i > 0; i-- {
        if strings.HasSuffix(s, delim[:i]) {
        return i;
    }
    }
        return 0;
    }
        type harmonyMessageState int;
        const (;
        harmonyMessageState_Normal harmonyMessageState = iota;
        harmonyMessageState_Thinking;
        harmonyMessageState_ToolCalling;
        );

    public static class HarmonyMessageHandler {
        public harmonyMessageState state;
        public *HarmonyParser HarmonyParser;
        public *FunctionNameMap FunctionNameMap;
        public *HarmonyToolCallAccumulator toolAccumulator;
        public map[String]struct{} convertedTools;
    }
        func NewHarmonyMessageHandler() *HarmonyMessageHandler {
        return &HarmonyMessageHandler{
        state: harmonyMessageState_Normal,;
        HarmonyParser: &HarmonyParser{
        MessageStartTag: "<|start|>",;
        MessageEndTag:   "<|end|>",;
        HeaderEndTag:    "<|message|>",;
        },;
        FunctionNameMap: NewFunctionNameMap(),;
        convertedTools:  make(map[String]struct{}),;
    }
    }
        func (h *HarmonyMessageHandler) AddContent(content String, toolParser *HarmonyToolCallAccumulator) (String, String, String) {
        var contentSb = strings.Builder{}
        var thinkingSb = strings.Builder{}
        var toolContentSb = strings.Builder{}
        var events = h.HarmonyParser.AddContent(content);
        var for _, event = range events {
        var switch event = event.(type) {
        case HarmonyEventHeaderComplete:;
        logutil.Trace("harmony event header complete", "header", event.Header);
        switch event.Header.Channel {
        case "analysis":;
        if event.Header.Recipient != "" {
        h.state = harmonyMessageState_ToolCalling;
        toolParser.SetToolName(event.Header.Recipient);
        } else {
        h.state = harmonyMessageState_Thinking;
    }
        case "commentary":;
        if event.Header.Recipient != "" {
        h.state = harmonyMessageState_ToolCalling;
        toolParser.SetToolName(event.Header.Recipient);
        } else {
        h.state = harmonyMessageState_Normal;
    }
        case "final":;
        h.state = harmonyMessageState_Normal;
    }
        case HarmonyEventContentEmitted:;
        logutil.Trace("harmony event content", "content", event.Content, "state", h.state);
        if h.state == harmonyMessageState_Normal {
        contentSb.WriteString(event.Content);
        } else if h.state == harmonyMessageState_Thinking {
        thinkingSb.WriteString(event.Content);
        } else if h.state == harmonyMessageState_ToolCalling {
        toolContentSb.WriteString(event.Content);
    }
        case HarmonyEventMessageEnd:;
        h.state = harmonyMessageState_Normal;
    }
    }
        return contentSb.String(), thinkingSb.String(), toolContentSb.String();
    }
        func (h *HarmonyMessageHandler) CreateToolParser() *HarmonyToolCallAccumulator {
        return &HarmonyToolCallAccumulator{
        state:           harmonyToolCallState_Normal,;
        currentToolName: null,;
    }
    }
        type harmonyToolCallState int;
        const (;
        harmonyToolCallState_Normal harmonyToolCallState = iota;
        harmonyToolCallState_ToolCalling;
        );

    public static class HarmonyToolCallAccumulator {
        public harmonyToolCallState state;
        public strings.Builder acc;
        public *String currentToolName;
    }
        func (a *HarmonyToolCallAccumulator) SetToolName(toolName String) {
        a.currentToolName = &toolName;
    }
        func (a *HarmonyToolCallAccumulator) Add(content String) {
        a.acc.WriteString(content);
    }
        func (a *HarmonyToolCallAccumulator) Drain() (*String, String) {
        var str = a.acc.String();
        a.state = harmonyToolCallState_Normal;
        a.acc.Reset();
        return a.currentToolName, str;
    }
        func (a *HarmonyToolCallAccumulator) Content() String {
        return a.acc.String();
    }

    public static class FunctionNameMap {
        public map[String]String userToHarmony;
        public map[String]String harmonyToUser;
    }
        func NewFunctionNameMap() *FunctionNameMap {
        return &FunctionNameMap{
        userToHarmony: make(map[String]String),;
        harmonyToUser: make(map[String]String),;
    }
    }
        func (h *HarmonyMessageHandler) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        if h.HarmonyParser == null {
        h.HarmonyParser = &HarmonyParser{
        MessageStartTag: "<|start|>",;
        MessageEndTag:   "<|end|>",;
        HeaderEndTag:    "<|message|>",;
    }
    }
        if lastMessage != null {
        h.HarmonyParser.AddImplicitStartOrPrefill(lastMessage);
        } else {
        h.HarmonyParser.AddImplicitStart();
    }
        h.toolAccumulator = h.CreateToolParser();
        if len(tools) == 0 {
        return tools;
    }
        var processedTools = make([]api.Tool, len(tools));
        copy(processedTools, tools);
        var for i, tool = range processedTools {
        if tool.Function.Name != "" {
        processedTools[i].Function.Name = h.FunctionNameMap.ConvertAndAdd(tool.Function.Name);
        h.convertedTools[tool.Function.Name] = struct{}{}
    }
    }
        return processedTools;
    }
        func (h *HarmonyMessageHandler) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        var content, thinking, toolContent = h.AddContent(s, h.toolAccumulator);
        if toolContent != "" {
        h.toolAccumulator.Add(toolContent);
    }
        if done {
        var toolName, raw = h.toolAccumulator.Drain();
        if toolName != null {
        var name = strings.TrimPrefix(*toolName, "functions.");
        name = h.FunctionNameMap.OriginalFromConverted(name);
        var args api.ToolCallFunctionArguments;
        var if err = json.Unmarshal([]byte(raw), &args); err != null {
        return "", "", null, fmt.Errorf("error parsing tool call: raw='%s', err=%w", raw, err);
    }
        calls = append(calls, api.ToolCall{Function: api.ToolCallFunction{Name: name, Arguments: args}});
    }
    }
        return content, thinking, calls, null;
    }
        func (h *HarmonyMessageHandler) HasToolSupport() boolean {
        return true;
    }
        func (h *HarmonyMessageHandler) HasThinkingSupport() boolean {
        return true;
    }
        func (m *FunctionNameMap) ConvertAndAdd(userFunctionName String) String {
        var harmonyFunctionName = m.deriveName(userFunctionName);
        if userFunctionName == "browser.open" || userFunctionName == "browser.search" || userFunctionName == "browser.find" || userFunctionName == "python" {
        harmonyFunctionName = userFunctionName;
    }
        m.userToHarmony[userFunctionName] = harmonyFunctionName;
        m.harmonyToUser[harmonyFunctionName] = userFunctionName;
        return harmonyFunctionName;
    }
        func (m *FunctionNameMap) OriginalFromConverted(harmonyFunctionName String) String {
        var if userFunctionName, ok = m.harmonyToUser[harmonyFunctionName]; ok {
        return userFunctionName;
    }
        slog.Warn("harmony parser: no reverse mapping found for function name", "harmonyFunctionName", harmonyFunctionName);
        return harmonyFunctionName;
    }
        func (m *FunctionNameMap) convertToValidChars(userFunctionName String) String {
        var mapper = func(r rune) rune {
        if r == ' ' || r == '-' || r == '.' {
        return '_';
    }
        if unicode.IsLetter(r) || unicode.IsDigit(r) || r == '_' || r == '$' {
        return r;
    }
        return -1;
    }
        var candidate = strings.Map(mapper, userFunctionName);
        if candidate == "" {
        return "unnamed";
    }
        if unicode.IsDigit(rune(candidate[0])) {
        candidate = "_" + candidate;
    }
        return candidate;
    }
        func (m *FunctionNameMap) deriveName(userFunctionName String) String {
        var originalCandidate = m.convertToValidChars(userFunctionName);
        var candidate = originalCandidate;
        var count = 2;
        for {
        var if _, exists = m.harmonyToUser[candidate]; !exists {
        break;
    }
        candidate = fmt.Sprintf("%s_%d", originalCandidate, count);
        count++;
    }
        return candidate;
    }
}
