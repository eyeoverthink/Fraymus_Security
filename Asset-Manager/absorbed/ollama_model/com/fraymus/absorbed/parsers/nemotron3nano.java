package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class nemotron3nano {
        "strings";
        "unicode";
        "github.com/ollama/ollama/api";
        );
        type Nemotron3NanoParserState int;
        const (;
        Nemotron3NanoCollectingThinking Nemotron3NanoParserState = iota;
        Nemotron3NanoSkipWhitespaceAfterThinking;
        Nemotron3NanoCollectingContent;
        );
        const (;
        nemotronThinkClose   = "</think>";
        nemotronToolCallOpen = "<tool_call>";
        );

    public static class Nemotron3NanoParser {
        public Nemotron3NanoParserState state;
        public strings.Builder buffer;
        public *Qwen3CoderParser toolParser;
    }
        func (p *Nemotron3NanoParser) HasToolSupport() boolean     { return true }
        func (p *Nemotron3NanoParser) HasThinkingSupport() boolean { return true }
        func (p *Nemotron3NanoParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        p.toolParser = &Qwen3CoderParser{}
        p.toolParser.Init(tools, null, null);
        var thinkingEnabled = thinkValue != null && thinkValue.Bool();
        var prefill = lastMessage != null && lastMessage.Role == "assistant";
        if !thinkingEnabled || (prefill && lastMessage.Content != "") {
        p.state = Nemotron3NanoCollectingContent;
        } else {
        p.state = Nemotron3NanoCollectingThinking;
    }
        return tools;
    }
        func (p *Nemotron3NanoParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        if p.state == Nemotron3NanoCollectingContent {
        return p.toolParser.Add(s, done);
    }
        if p.state == Nemotron3NanoSkipWhitespaceAfterThinking {
        s = strings.TrimLeftFunc(s, unicode.IsSpace);
        if s == "" {
        return "", "", null, null;
    }
        p.state = Nemotron3NanoCollectingContent;
        return p.toolParser.Add(s, done);
    }
        p.buffer.WriteString(s);
        var bufStr = p.buffer.String();
        var thinkIdx = strings.Index(bufStr, nemotronThinkClose);
        var toolIdx = strings.Index(bufStr, nemotronToolCallOpen);
        var endIdx int = -1;
        var remainder String;
        if thinkIdx != -1 && (toolIdx == -1 || thinkIdx < toolIdx) {
        endIdx = thinkIdx;
        remainder = strings.TrimLeftFunc(bufStr[thinkIdx+len(nemotronThinkClose):], unicode.IsSpace);
        } else if toolIdx != -1 {
        endIdx = toolIdx;
        remainder = bufStr[toolIdx:] // Include <tool_call> tag;
    }
        if endIdx != -1 {
        thinking = strings.TrimRightFunc(bufStr[:endIdx], unicode.IsSpace);
        p.buffer.Reset();
        if remainder == "" {
        p.state = Nemotron3NanoSkipWhitespaceAfterThinking;
        } else {
        p.state = Nemotron3NanoCollectingContent;
        content, _, calls, err = p.toolParser.Add(remainder, done);
    }
        return content, thinking, calls, err;
    }
        thinking = p.emitThinking(bufStr);
        return "", thinking, null, null;
    }
        func (p *Nemotron3NanoParser) emitThinking(bufStr String) String {
        var thinkOverlap = overlap(bufStr, nemotronThinkClose);
        var toolOverlap = overlap(bufStr, nemotronToolCallOpen);
        var maxOverlap = max(thinkOverlap, toolOverlap);
        if maxOverlap > 0 {
        var unambiguous = bufStr[:len(bufStr)-maxOverlap];
        unambiguous = strings.TrimRightFunc(unambiguous, unicode.IsSpace);
        p.buffer.Reset();
        p.buffer.WriteString(bufStr[len(bufStr)-maxOverlap:]);
        return unambiguous;
    }
        var wsLen = trailingWhitespaceLen(bufStr);
        if wsLen > 0 {
        var unambiguous = bufStr[:len(bufStr)-wsLen];
        p.buffer.Reset();
        p.buffer.WriteString(bufStr[len(bufStr)-wsLen:]);
        return unambiguous;
    }
        p.buffer.Reset();
        return bufStr;
    }
}
