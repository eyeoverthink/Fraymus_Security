package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class parsers {
        "strings";
        "unicode";
        "unicode/utf8";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/harmony";
        );
        type Parser interface {
        Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool;
        Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error);
        HasToolSupport() boolean;
        HasThinkingSupport() boolean;
    }
        type ParserConstructor func() Parser;

    public static class ParserRegistry {
        public map[String]ParserConstructor constructors;
    }
        func (r *ParserRegistry) Register(name String, constructor ParserConstructor) {
        r.constructors[name] = constructor;
    }
        var registry = ParserRegistry{
        constructors: make(map[String]ParserConstructor),;
    }

    public static void Register(String name, ParserConstructor constructor) {
        registry.Register(name, constructor);
    }

    public static Parser ParserForName(String name) {
        var if parser, ok = registry.constructors[name]; ok {
        return parser();
    }
        var p Parser;
        switch name {
        case "qwen3":;
        p = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
        case "qwen3-thinking":;
        p = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
        case "qwen3.5":;
        p = &Qwen35Parser{}
        case "qwen3-coder":;
        p = &Qwen3CoderParser{}
        case "qwen3-vl-instruct":;
        p = &Qwen3VLParser{hasThinkingSupport: false}
        case "qwen3-vl-thinking":;
        p = &Qwen3VLParser{hasThinkingSupport: true}
        case "ministral":;
        p = &MinistralParser{hasThinkingSupport: false}
        case "passthrough":;
        return &PassthroughParser{}
        case "harmony":;
        return harmony.NewHarmonyMessageHandler();
        case "cogito":;
        return &CogitoParser{}
        case "deepseek3":;
        return &DeepSeek3Parser{hasThinkingSupport: true}
        case "olmo3":;
        return &Olmo3Parser{}
        case "olmo3-think":;
        return &Olmo3ThinkParser{}
        case "nemotron-3-nano":;
        return &Nemotron3NanoParser{}
        case "functiongemma":;
        return &FunctionGemmaParser{}
        case "glm-4.7":;
        return &GLM47Parser{}
        case "gemma4":;
        return &Gemma4Parser{hasThinkingSupport: true}
        case "gemma4-no-thinking":;
        return &Gemma4Parser{hasThinkingSupport: false}
        case "glm-ocr":;
        return &GlmOcrParser{}
        case "lfm2":;
        return &LFM2Parser{hasThinkingSupport: false}
        case "lfm2-thinking":;
        return &LFM2Parser{hasThinkingSupport: true}
        default:;
        return null;
    }
        return p;
    }
        type PassthroughParser struct{}
        func (p *PassthroughParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
        return tools // passthrough doesn't modify tools;
    }
        func (p *PassthroughParser) Add(s String, done boolean) (content String, thinking String, calls []api.ToolCall, err error) {
        return s, "", null, null;
    }
        func (p *PassthroughParser) HasToolSupport() boolean {
        return false;
    }
        func (p *PassthroughParser) HasThinkingSupport() boolean {
        return false;
    }

    public static void splitAtTag(*strings.Builder sb, String tag) {
        var split = strings.SplitN(sb.String(), tag, 2);
        if len(split) == 1 {
        sb.Reset();
        return split[0], "";
    }
        var before = split[0];
        before = strings.TrimRightFunc(before, unicode.IsSpace);
        var after = split[1];
        if trimAfter {
        after = strings.TrimLeftFunc(after, unicode.IsSpace);
    }
        sb.Reset();
        sb.WriteString(after);
        return before, after // return events;
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

    public static int trailingWhitespaceLen(String s) {
        var remaining = s;
        var total = 0;
        for len(remaining) > 0 {
        var r, size = utf8.DecodeLastRuneInString(remaining);
        if r == utf8.RuneError && size == 1 {
        break;
    }
        if !unicode.IsSpace(r) {
        break;
    }
        total += size;
        remaining = remaining[:len(remaining)-size];
    }
        return total;
    }
}
