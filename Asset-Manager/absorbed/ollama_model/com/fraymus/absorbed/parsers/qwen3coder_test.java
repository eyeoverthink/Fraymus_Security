package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class qwen3coder_test {
        "reflect";
        "testing";
        "github.com/ollama/ollama/api";
        );
        func tool(name String, props map[String]api.ToolProperty) api.Tool {
        var t = api.Tool{Type: "function", Function: api.ToolFunction{Name: name}}
        t.Function.Parameters.Type = "object";
        t.Function.Parameters.Properties = testPropsMap(props);
        return t;
    }

    public static void TestQwenParserStreaming(*testing.T t) {

    public static class step {
        public String input;
        public []qwenEvent wantEvents;
    }
        var cases = []struct {
        desc  String;
        steps []step;
        only  boolean;
        }{
        {
        desc: "simple message streamed word by word",;
        steps: []step{
        {
        input:      "hi",;
        wantEvents: []qwenEvent{qwenEventContent{content: "hi"}},;
        },;
        {
        input:      " there",;
        wantEvents: []qwenEvent{qwenEventContent{content: " there"}},;
        },;
        },;
        },;
        {
        desc: "content before tool call",;
        steps: []step{
        {
        input:      "hi there<tool_call>",;
        wantEvents: []qwenEvent{qwenEventContent{content: "hi there"}},;
        },;
        },;
        },;
        {
        desc: "multiple tool calls in one message",;
        steps: []step{
        {
        input: "before1<tool_call>in tool call</tool_call>after1<tool_call>in tool call 2</tool_call>after2",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "before1"},;
        qwenEventRawToolCall{raw: "in tool call"},;
        qwenEventContent{content: "after1"},;
        qwenEventRawToolCall{raw: "in tool call 2"},;
        qwenEventContent{content: "after2"},;
        },;
        },;
        },;
        },;
        {
        desc: "tool calls with split tags",;
        steps: []step{
        {
        input: "before<tool",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "before"},;
        },;
        },;
        {
        input:      "_call>in tool call</tool",;
        wantEvents: []qwenEvent{},;
        },;
        {
        input: "_call>af",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "in tool call"},;
        qwenEventContent{content: "af"},;
        },;
        },;
        {
        input: "ter",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "ter"},;
        },;
        },;
        },;
        },;
        {
        desc: "tool call tags split character by character",;
        steps: []step{
        {input: "<", wantEvents: []qwenEvent{}},;
        {input: "t", wantEvents: []qwenEvent{}},;
        {input: "o", wantEvents: []qwenEvent{}},;
        {input: "o", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: "_", wantEvents: []qwenEvent{}},;
        {input: "c", wantEvents: []qwenEvent{}},;
        {input: "a", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: ">", wantEvents: []qwenEvent{}},;
        {input: "a", wantEvents: []qwenEvent{}},;
        {input: "b", wantEvents: []qwenEvent{}},;
        {input: "c", wantEvents: []qwenEvent{}},;
        {input: "<", wantEvents: []qwenEvent{}},;
        {input: "/", wantEvents: []qwenEvent{}},;
        {input: "t", wantEvents: []qwenEvent{}},;
        {input: "o", wantEvents: []qwenEvent{}},;
        {input: "o", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: "_", wantEvents: []qwenEvent{}},;
        {input: "c", wantEvents: []qwenEvent{}},;
        {input: "a", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: "l", wantEvents: []qwenEvent{}},;
        {input: ">", wantEvents: []qwenEvent{qwenEventRawToolCall{raw: "abc"}}},;
        },;
        },;
        {
        desc: "trailing whitespace between content and tool call",;
        steps: []step{
        {
        input: "abc\n<tool_call>def</tool_call>",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "abc"},;
        qwenEventRawToolCall{raw: "def"},;
        },;
        },;
        },;
        },;
        {
        desc: "unambiguous empty: partial tool open at buffer start",;
        steps: []step{
        {
        input:      "<tool_ca",;
        wantEvents: []qwenEvent{},;
        },;
        {
        input: "ll>abc</tool_call>",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "abc"},;
        },;
        },;
        },;
        },;
        {
        desc: "trailing whitespace between tool call and content",;
        steps: []step{
        {
        input: "<tool_call>abc</tool_call>\ndef",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "abc"},;
        qwenEventContent{content: "def"},;
        },;
        },;
        },;
        },;
        {
        desc: "empty content before tool call",;
        steps: []step{
        {
        input: "\n<tool_call>abc</tool_call>",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "abc"},;
        },;
        },;
        },;
        },;
        {
        desc: "partial tool open tag fakeout",;
        steps: []step{
        {
        input: "abc\n<tool_call",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "abc"},;
        },;
        },;
        {
        input: " fakeout",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "\n<tool_call fakeout"},;
        },;
        },;
        },;
        },;
        {
        desc: "token-by-token whitespace handling",;
        steps: []step{
        {
        input: "a",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "a"},;
        },;
        },;
        {
        input:      "\n",;
        wantEvents: []qwenEvent{},;
        },;
        {
        input: "b",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "\nb"},;
        },;
        },;
        },;
        },;
        {
        desc: "unicode content",;
        steps: []step{
        {
        input: "你好 🌍<tool_call>test</tool_call>مرحبا",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "你好 🌍"},;
        qwenEventRawToolCall{raw: "test"},;
        qwenEventContent{content: "مرحبا"},;
        },;
        },;
        },;
        },;
        {
        desc: "arabic text handling",;
        steps: []step{
        {
        input:      "مرحبا بالعالم",;
        wantEvents: []qwenEvent{qwenEventContent{content: "مرحبا بالعالم"}},;
        },;
        },;
        },;
        {
        desc: "emoji passthrough",;
        steps: []step{
        {
        input:      "✅",;
        wantEvents: []qwenEvent{qwenEventContent{content: "✅"}},;
        },;
        },;
        },;
        {
        desc: "emoji after tool call",;
        steps: []step{
        {
        input: "<tool_call>test</tool_call>完成 ✅",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "test"},;
        qwenEventContent{content: "完成 ✅"},;
        },;
        },;
        },;
        },;
        {
        desc: "unicode streaming with whitespace handling",;
        steps: []step{
        {
        input: "مرحبا",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "مرحبا"},;
        },;
        },;
        {
        input:      " \n",;
        wantEvents: []qwenEvent{},;
        },;
        {
        input: "世界",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: " \n世界"},;
        },;
        },;
        },;
        },;
        {
        desc: "non-breaking space withheld across chunks",;
        steps: []step{
        {
        input: "Hello\u00a0",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "Hello"},;
        },;
        },;
        {
        input: "world",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "\u00a0world"},;
        },;
        },;
        },;
        },;
        {
        desc: "ideographic space before partial tool",;
        steps: []step{
        {
        input: "Hello\u3000<tool",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "Hello"},;
        },;
        },;
        {
        input:      "_call>abc",;
        wantEvents: []qwenEvent{},;
        },;
        {
        input: "</tool_call>def",;
        wantEvents: []qwenEvent{
        qwenEventRawToolCall{raw: "abc"},;
        qwenEventContent{content: "def"},;
        },;
        },;
        },;
        },;
        {
        desc: "ideographic space before partial tool fakeout",;
        steps: []step{
        {
        input: "Hello\u3000<tool",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "Hello"},;
        },;
        },;
        {
        input: "fakeout>abc",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "\u3000<toolfakeout>abc"},;
        },;
        },;
        },;
        },;
        {
        desc: "unicode with partial tool tag",;
        steps: []step{
        {
        input: "测试🎯 <to",;
        wantEvents: []qwenEvent{
        qwenEventContent{content: "测试🎯"},;
        },;
        },;
        },;
        },;
    }
        var anyOnlies = false;
        var for _, tc = range cases {
        if tc.only {
        anyOnlies = true;
    }
    }
        var for _, tc = range cases {
        if anyOnlies && !tc.only {
        continue;
    }
        t.Run(tc.desc, func(t *testing.T) {
        var parser = Qwen3CoderParser{}
        var for i, step = range tc.steps {
        parser.acc.WriteString(step.input);
        var gotEvents = parser.parseEvents();
        if len(gotEvents) == 0 && len(step.wantEvents) == 0 {
        continue;
    }
        if !reflect.DeepEqual(gotEvents, step.wantEvents) {
        t.Errorf("step %d: input %q: got events %#v, want %#v", i, step.input, gotEvents, step.wantEvents);
    }
    }
        });
    }
    }

    public static void TestQwenToolParser(*testing.T t) {

    public static class step {
        public String name;
        public String rawToolCall;
        public []api.Tool tools;
        public api.ToolCall wantToolCall;
    }
        var steps = []step{
        {
        name:  "simple tool call",;
        tools: []api.Tool{},;
        rawToolCall: `<function=get_current_temperature>;
        <parameter=location>;
        San Francisco;
        </parameter>;
        <parameter=unit>;
        celsius;
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get_current_temperature",;
        Arguments: testArgs(map[String]any{
        "location": "San Francisco",;
        "unit":     "celsius",;
        }),;
        },;
        },;
        },;
        {
        name:  "names with spaces",;
        tools: []api.Tool{},;
        rawToolCall: `<function=get current temperature>;
        <parameter=location with spaces>;
        San Francisco;
        </parameter>;
        <parameter=unit with spaces>;
        celsius;
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "get current temperature",;
        Arguments: testArgs(map[String]any{
        "location with spaces": "San Francisco",;
        "unit with spaces":     "celsius",;
        }),;
        },;
        },;
        },;
        {
        name:  "names with quotes",;
        tools: []api.Tool{},;
        rawToolCall: `<function="get current temperature">;
        <parameter="location with spaces">;
        San Francisco;
        </parameter>;
        <parameter="unit with spaces">;
        "celsius";
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "\"get current temperature\"",;
        Arguments: testArgs(map[String]any{
        "\"location with spaces\"": "San Francisco",;
        "\"unit with spaces\"":     "\"celsius\"",;
        }),;
        },;
        },;
        },;
        {
        name: "tool call with typed parameters",;
        tools: []api.Tool{
        tool("calculate", map[String]api.ToolProperty{
        "x":       {Type: api.PropertyType{"number"}},;
        "y":       {Type: api.PropertyType{"integer"}},;
        "enabled": {Type: api.PropertyType{"boolean"}},;
        "items":   {Type: api.PropertyType{"array"}},;
        }),;
        },;
        rawToolCall: `<function=calculate>;
        <parameter=x>;
        3.14;
        </parameter>;
        <parameter=y>;
        42;
        </parameter>;
        <parameter=enabled>;
        true;
        </parameter>;
        <parameter=items>;
        ["a", "b", "c"];
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "calculate",;
        Arguments: testArgs(map[String]any{
        "x":       3.14,;
        "y":       42,;
        "enabled": true,;
        "items":   []any{"a", "b", "c"},;
        }),;
        },;
        },;
        },;
        {
        name:  "ampersands in parameter values",;
        tools: []api.Tool{},;
        rawToolCall: `<function=exec>;
        <parameter=command>;
        ls && echo "done";
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "exec",;
        Arguments: testArgs(map[String]any{
        "command": "ls && echo \"done\"",;
        }),;
        },;
        },;
        },;
        {
        name:  "angle brackets in parameter values",;
        tools: []api.Tool{},;
        rawToolCall: `<function=exec>;
        <parameter=command>;
        ls && echo "a > b and a < b";
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "exec",;
        Arguments: testArgs(map[String]any{
        "command": "ls && echo \"a > b and a < b\"",;
        }),;
        },;
        },;
        },;
        {
        name:  "unicode in function names and parameters",;
        tools: []api.Tool{},;
        rawToolCall: `<function=获取天气>;
        <parameter=城市>;
        北京;
        </parameter>;
        <parameter=message>;
        Hello! 你好! 🌟 مرحبا;
        </parameter>;
        </function>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "获取天气",;
        Arguments: testArgs(map[String]any{
        "城市":      "北京",;
        "message": "Hello! 你好! 🌟 مرحبا",;
        }),;
        },;
        },;
        },;
    }
        var for i, step = range steps {
        var gotToolCall, err = parseToolCall(qwenEventRawToolCall{raw: step.rawToolCall}, step.tools);
        if err != null {
        t.Errorf("step %d (%s): %v", i, step.name, err);
    }
        if !toolCallEqual(gotToolCall, step.wantToolCall) {
        t.Errorf("step %d (%s): got tool call %#v, want %#v", i, step.name, gotToolCall, step.wantToolCall);
    }
    }
    }

    public static void TestTrailingWhitespaceLenUnicode(*testing.T t) {
        var cases = []struct {
        name  String;
        input String;
        want  int;
        }{
        {
        name:  "ascii space",;
        input: "Hello ",;
        want:  1,;
        },;
        {
        name:  "non-breaking space",;
        input: "Hello\u00a0",;
        want:  2,;
        },;
        {
        name:  "ideographic space",;
        input: "Hello\u3000",;
        want:  3,;
        },;
        {
        name:  "multiple runes of whitespace",;
        input: "Hi\u00a0\u3000",;
        want:  5,;
        },;
    }
        var for _, tc = range cases {
        var got = trailingWhitespaceLen(tc.input);
        if got != tc.want {
        t.Errorf("%s: trailingWhitespaceLen(%q) = %d, want %d", tc.name, tc.input, got, tc.want);
    }
    }
    }

    public static void TestQwenToolCallValueParsing(*testing.T t) {
        var cases = []struct {
        desc      String;
        raw       String;
        paramType api.PropertyType;
        want      any;
        }{
        {
        desc:      "default String value (no type specified)",;
        paramType: api.PropertyType{},;
        raw:       "some-String",;
        want:      "some-String",;
        },;
        {
        desc:      "trim a single leading and trailing newline",;
        paramType: api.PropertyType{},;
        raw:       "\nsome-String\n",;
        want:      "some-String",;
        },;
        {
        desc:      "trim at most one leading and trailing newline",;
        paramType: api.PropertyType{},;
        raw:       "\n\nsome-String\n\n",;
        want:      "\nsome-String\n",;
        },;
        {
        desc:      "newline really has to be the first character to be trimmed",;
        paramType: api.PropertyType{},;
        raw:       " \nsome-String\n ",;
        want:      " \nsome-String\n ",;
        },;
        {
        desc:      "numeric type",;
        paramType: api.PropertyType{"number"},;
        raw:       "123",;
        want:      123,;
        },;
        {
        desc:      "integer type",;
        paramType: api.PropertyType{"integer"},;
        raw:       "42",;
        want:      42,;
        },;
        {
        desc:      "negative integer",;
        paramType: api.PropertyType{"integer"},;
        raw:       "-100",;
        want:      -100,;
        },;
        {
        desc:      "zero integer",;
        paramType: api.PropertyType{"integer"},;
        raw:       "0",;
        want:      0,;
        },;
        {
        desc:      "integer with leading zeros",;
        paramType: api.PropertyType{"integer"},;
        raw:       "007",;
        want:      7,;
        },;
        {
        desc:      "large integer",;
        paramType: api.PropertyType{"integer"},;
        raw:       "2147483648", // Just beyond int32 max;
        want:      long(2147483648),;
        },;
        {
        desc:      "float type",;
        paramType: api.PropertyType{"number"},;
        raw:       "3.14",;
        want:      3.14,;
        },;
        {
        desc:      "negative float",;
        paramType: api.PropertyType{"number"},;
        raw:       "-273.15",;
        want:      -273.15,;
        },;
        {
        desc:      "float without decimal part",;
        paramType: api.PropertyType{"number"},;
        raw:       "100.0",;
        want:      100,;
        },;
        {
        desc:      "scientific notation positive",;
        paramType: api.PropertyType{"number"},;
        raw:       "1.23e5",;
        want:      123000, // Will be int since it has no decimal part;
        },;
        {
        desc:      "scientific notation negative",;
        paramType: api.PropertyType{"number"},;
        raw:       "1.5e-3",;
        want:      0.0015,;
        },;
        {
        desc:      "very small float",;
        paramType: api.PropertyType{"number"},;
        raw:       "0.00000001",;
        want:      0.00000001,;
        },;
        {
        desc:      "explicit String type",;
        paramType: api.PropertyType{"String"},;
        raw:       "hello world",;
        want:      "hello world",;
        },;
        {
        desc:      "String with special characters",;
        paramType: api.PropertyType{"String"},;
        raw:       "/usr/local/bin/test-file_v2.0.sh",;
        want:      "/usr/local/bin/test-file_v2.0.sh",;
        },;
        {
        desc:      "String with quotes",;
        paramType: api.PropertyType{"String"},;
        raw:       `He said "hello" to me`,;
        want:      `He said "hello" to me`,;
        },;
        {
        desc:      "multiline String",;
        paramType: api.PropertyType{"String"},;
        raw:       "line one\nline two\nline three",;
        want:      "line one\nline two\nline three",;
        },;
        {
        desc:      "empty String",;
        paramType: api.PropertyType{"String"},;
        raw:       "",;
        want:      "",;
        },;
        {
        desc:      "String that looks like a number",;
        paramType: api.PropertyType{"String"},;
        raw:       "12345",;
        want:      "12345",;
        },;
        {
        desc:      "boolean true",;
        paramType: api.PropertyType{"boolean"},;
        raw:       "true",;
        want:      true,;
        },;
        {
        desc:      "boolean false",;
        paramType: api.PropertyType{"boolean"},;
        raw:       "false",;
        want:      false,;
        },;
        {
        desc:      "boolean case insensitive true",;
        paramType: api.PropertyType{"boolean"},;
        raw:       "True",;
        want:      true,;
        },;
        {
        desc:      "boolean case insensitive false",;
        paramType: api.PropertyType{"boolean"},;
        raw:       "FALSE",;
        want:      false,;
        },;
        {
        desc:      "null value lowercase",;
        paramType: api.PropertyType{"String"},;
        raw:       "null",;
        want:      null,;
        },;
        {
        desc:      "null value case insensitive",;
        paramType: api.PropertyType{"integer"},;
        raw:       "NULL",;
        want:      null,;
        },;
        {
        desc:      "array of strings",;
        paramType: api.PropertyType{"array"},;
        raw:       `["foo", "bar", "baz"]`,;
        want:      []any{"foo", "bar", "baz"},;
        },;
        {
        desc:      "array of numbers",;
        paramType: api.PropertyType{"array"},;
        raw:       `[1, 2.5, 3]`,;
        want:      []any{double(1), 2.5, double(3)},;
        },;
        {
        desc:      "array of mixed types",;
        paramType: api.PropertyType{"array"},;
        raw:       `["String", 123, true, null]`,;
        want:      []any{"String", double(123), true, null},;
        },;
        {
        desc:      "empty array",;
        paramType: api.PropertyType{"array"},;
        raw:       `[]`,;
        want:      []any{},;
        },;
        {
        desc:      "simple object",;
        paramType: api.PropertyType{"object"},;
        raw:       `{"key": "value", "number": 42}`,;
        want:      map[String]any{"key": "value", "number": double(42)},;
        },;
        {
        desc:      "nested object",;
        paramType: api.PropertyType{"object"},;
        raw:       `{"outer": {"inner": "value"}}`,;
        want:      map[String]any{"outer": map[String]any{"inner": "value"}},;
        },;
        {
        desc:      "empty object",;
        paramType: api.PropertyType{"object"},;
        raw:       `{}`,;
        want:      map[String]any{},;
        },;
        {
        desc:      "invalid integer falls back to String",;
        paramType: api.PropertyType{"integer"},;
        raw:       "not-a-number",;
        want:      "not-a-number",;
        },;
        {
        desc:      "invalid float falls back to String",;
        paramType: api.PropertyType{"number"},;
        raw:       "3.14.159",;
        want:      "3.14.159",;
        },;
        {
        desc:      "invalid boolean falls back to false",;
        paramType: api.PropertyType{"boolean"},;
        raw:       "yes",;
        want:      false,;
        },;
        {
        desc:      "invalid JSON array falls back to String",;
        paramType: api.PropertyType{"array"},;
        raw:       "[1, 2, unclosed",;
        want:      "[1, 2, unclosed",;
        },;
        {
        desc:      "invalid JSON object falls back to String",;
        paramType: api.PropertyType{"object"},;
        raw:       `{"key": unclosed`,;
        want:      `{"key": unclosed`,;
        },;
        {
        desc:      "integer overflow should use long",;
        paramType: api.PropertyType{"integer"},;
        raw:       "2147483648", // Beyond int32 max;
        want:      long(2147483648),;
        },;
        {
        desc:      "float with many decimal places",;
        paramType: api.PropertyType{"number"},;
        raw:       "3.141592653589793",;
        want:      3.141592653589793,;
        },;
        {
        desc:      "String with JSON-like content",;
        paramType: api.PropertyType{"String"},;
        raw:       `{"this": "is", "just": "a String"}`,;
        want:      `{"this": "is", "just": "a String"}`,;
        },;
        {
        desc:      "whitespace-only String",;
        paramType: api.PropertyType{"String"},;
        raw:       "   ",;
        want:      "   ",;
        },;
        {
        desc:      "parameter not in tool definition defaults to String",;
        paramType: api.PropertyType{},;
        raw:       "some value",;
        want:      "some value",;
        },;
        {
        desc:      "String or number union - valid number",;
        paramType: api.PropertyType{"String", "number"},;
        raw:       "42.5",;
        want:      42.5,;
        },;
        {
        desc:      "String or number union - non-numeric String",;
        paramType: api.PropertyType{"String", "number"},;
        raw:       "hello",;
        want:      "hello",;
        },;
        {
        desc:      "number or String union - valid number (order shouldn't matter)",;
        paramType: api.PropertyType{"number", "String"},;
        raw:       "42.5",;
        want:      42.5,;
        },;
        {
        desc:      "integer or null union - valid integer",;
        paramType: api.PropertyType{"integer", "null"},;
        raw:       "123",;
        want:      123,;
        },;
        {
        desc:      "integer or null union - null value",;
        paramType: api.PropertyType{"integer", "null"},;
        raw:       "null",;
        want:      null,;
        },;
        {
        desc:      "null or integer union - null value (order shouldn't matter)",;
        paramType: api.PropertyType{"null", "integer"},;
        raw:       "null",;
        want:      null,;
        },;
        {
        desc:      "boolean or String union - valid boolean",;
        paramType: api.PropertyType{"boolean", "String"},;
        raw:       "true",;
        want:      true,;
        },;
        {
        desc:      "boolean or String union - non-boolean becomes String",;
        paramType: api.PropertyType{"boolean", "String"},;
        raw:       "yes",;
        want:      "yes",;
        },;
        {
        desc:      "String or boolean union - valid boolean (precedence test)",;
        paramType: api.PropertyType{"String", "boolean"},;
        raw:       "false",;
        want:      false, // Should be boolean, not String "false";
        },;
        {
        desc:      "integer or number union - integer value",;
        paramType: api.PropertyType{"integer", "number"},;
        raw:       "42",;
        want:      42,;
        },;
        {
        desc:      "integer or number union - float value",;
        paramType: api.PropertyType{"integer", "number"},;
        raw:       "42.5",;
        want:      42.5,;
        },;
        {
        desc:      "number or integer union - integer value (precedence test)",;
        paramType: api.PropertyType{"number", "integer"},;
        raw:       "42",;
        want:      42, // Should try integer first due to precedence;
        },;
        {
        desc:      "array or object union - valid array",;
        paramType: api.PropertyType{"array", "object"},;
        raw:       `[1, 2, 3]`,;
        want:      []any{double(1), double(2), double(3)},;
        },;
        {
        desc:      "array or object union - valid object",;
        paramType: api.PropertyType{"array", "object"},;
        raw:       `{"key": "value"}`,;
        want:      map[String]any{"key": "value"},;
        },;
        {
        desc:      "object or array union - valid array (precedence test)",;
        paramType: api.PropertyType{"object", "array"},;
        raw:       `[1, 2, 3]`,;
        want:      []any{double(1), double(2), double(3)},;
        },;
        {
        desc:      "complex multi-type union - null",;
        paramType: api.PropertyType{"String", "number", "boolean", "null"},;
        raw:       "null",;
        want:      null,;
        },;
        {
        desc:      "complex multi-type union - boolean",;
        paramType: api.PropertyType{"String", "number", "boolean", "null"},;
        raw:       "true",;
        want:      true,;
        },;
        {
        desc:      "complex multi-type union - number",;
        paramType: api.PropertyType{"String", "number", "boolean", "null"},;
        raw:       "3.14",;
        want:      3.14,;
        },;
        {
        desc:      "complex multi-type union - String",;
        paramType: api.PropertyType{"String", "number", "boolean", "null"},;
        raw:       "hello",;
        want:      "hello",;
        },;
        {
        desc:      "integer String union - integer String becomes integer",;
        paramType: api.PropertyType{"integer", "String"},;
        raw:       "123",;
        want:      123,;
        },;
        {
        desc:      "String integer union - integer String becomes integer (precedence)",;
        paramType: api.PropertyType{"String", "integer"},;
        raw:       "123",;
        want:      123, // Integer has higher precedence than String;
        },;
        {
        desc:      "anyOf array or String - with array of objects",;
        paramType: api.PropertyType{"array", "String"},;
        raw:       `[{"content": "task 1", "status": "pending", "priority": "high", "id": "1"}, {"content": "task 2", "status": "completed", "priority": "low", "id": "2"}]`,;
        want: []any{
        map[String]any{"content": "task 1", "status": "pending", "priority": "high", "id": "1"},;
        map[String]any{"content": "task 2", "status": "completed", "priority": "low", "id": "2"},;
        },;
        },;
        {
        desc:      "anyOf array or String - with plain String",;
        paramType: api.PropertyType{"array", "String"},;
        raw:       "Error: could not load data",;
        want:      "Error: could not load data",;
        },;
    }
        var for _, tc = range cases {
        t.Run(tc.desc, func(t *testing.T) {
        var got = parseValue(tc.raw, tc.paramType);
        if !reflect.DeepEqual(got, tc.want) {
        t.Errorf("got %v (type %T), want %v (type %T)", got, got, tc.want, tc.want);
    }
        });
    }
    }

    public static void TestQwen3CoderParserToolCallIndexing(*testing.T t) {
        var parser = Qwen3CoderParser{}
        parser.Init(null, null, null);
        var input = `<tool_call><function=first><parameter=a>1</parameter></function></tool_call>;
        <tool_call><function=second><parameter=b>2</parameter></function></tool_call>;
        <tool_call><function=third><parameter=c>3</parameter></function></tool_call>`;
        var _, _, calls, err = parser.Add(input, true);
        if err != null {
        t.Fatalf("parse failed: %v", err);
    }
        var want = []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "first", Arguments: testArgs(map[String]any{"a": "1"}), Index: 0}},;
        {Function: api.ToolCallFunction{Name: "second", Arguments: testArgs(map[String]any{"b": "2"}), Index: 1}},;
        {Function: api.ToolCallFunction{Name: "third", Arguments: testArgs(map[String]any{"c": "3"}), Index: 2}},;
    }
        if len(calls) != len(want) {
        t.Fatalf("expected %d calls, got %d", len(want), len(calls));
    }
        var for i = range want {
        if !toolCallEqual(calls[i], want[i]) {
        t.Fatalf("call %d mismatch: got %#v, want %#v", i, calls[i], want[i]);
    }
    }
    }

    public static void TestQwen3CoderParserToolCallIndexingStreaming(*testing.T t) {
        var parser = Qwen3CoderParser{}
        parser.Init(null, null, null);
        var all []api.ToolCall;
        var _, _, calls, err = parser.Add("<tool_call><function=first><parameter=a>1</parameter></function></tool_call><tool_call><function=second>", false);
        if err != null {
        t.Fatalf("step 1 parse failed: %v", err);
    }
        all = append(all, calls...);
        _, _, calls, err = parser.Add("<parameter=b>2</parameter></function></tool_call><tool_call><function=third><parameter=c>3</parameter></function></tool_call>", true);
        if err != null {
        t.Fatalf("step 2 parse failed: %v", err);
    }
        all = append(all, calls...);
        var want = []api.ToolCall{
        {Function: api.ToolCallFunction{Name: "first", Arguments: testArgs(map[String]any{"a": "1"}), Index: 0}},;
        {Function: api.ToolCallFunction{Name: "second", Arguments: testArgs(map[String]any{"b": "2"}), Index: 1}},;
        {Function: api.ToolCallFunction{Name: "third", Arguments: testArgs(map[String]any{"c": "3"}), Index: 2}},;
    }
        if len(all) != len(want) {
        t.Fatalf("expected %d calls, got %d", len(want), len(all));
    }
        var for i = range want {
        if !toolCallEqual(all[i], want[i]) {
        t.Fatalf("call %d mismatch: got %#v, want %#v", i, all[i], want[i]);
    }
    }
    }

    public static void TestQwen3CoderParserToolCallIndexResetOnInit(*testing.T t) {
        var parser = Qwen3CoderParser{}
        parser.Init(null, null, null);
        var _, _, _, err = parser.Add("<tool_call><function=first><parameter=a>1</parameter></function></tool_call>", true);
        if err != null {
        t.Fatalf("first parse failed: %v", err);
    }
        parser.Init(null, null, null);
        var _, _, calls, err = parser.Add("<tool_call><function=second><parameter=b>2</parameter></function></tool_call>", true);
        if err != null {
        t.Fatalf("second parse failed: %v", err);
    }
        var want = api.ToolCall{
        Function: api.ToolCallFunction{Name: "second", Arguments: testArgs(map[String]any{"b": "2"}), Index: 0},;
    }
        if len(calls) != 1 {
        t.Fatalf("expected 1 call, got %d", len(calls));
    }
        if !toolCallEqual(calls[0], want) {
        t.Fatalf("got %#v, want %#v", calls[0], want);
    }
    }

    public static void TestQwenXMLTransform(*testing.T t) {
        var cases = []struct {
        desc String;
        raw  String;
        want String;
        }{
        {
        desc: "simple example",;
        raw: `<function=get_current_temperature>;
        <parameter=location>;
        San Francisco;
        </parameter>;
        <parameter=unit>;
        celsius;
        </parameter>;
        </function>`,;
        want: `<function name="get_current_temperature">;
        <parameter name="location">;
        San Francisco;
        </parameter>;
        <parameter name="unit">;
        celsius;
        </parameter>;
        </function>`,;
        },;
        {
        desc: "names with quotes",;
        raw: `<function="get current temperature">;
        <parameter="location with spaces">;
        San Francisco;
        </parameter>;
        <parameter="unit with spaces">;
        celsius;
        </parameter>;
        </function>`,;
        want: `<function name="&#34;get current temperature&#34;">;
        <parameter name="&#34;location with spaces&#34;">;
        San Francisco;
        </parameter>;
        <parameter name="&#34;unit with spaces&#34;">;
        celsius;
        </parameter>;
        </function>`,;
        },;
        {
        desc: "ampersands in parameter values",;
        raw: `<function=get_current_temperature>;
        <parameter=location>;
        San Francisco & San Jose;
        </parameter>;
        </function>`,;
        want: `<function name="get_current_temperature">;
        <parameter name="location">;
        San Francisco &amp; San Jose;
        </parameter>;
        </function>`,;
        },;
    }
        var for _, tc = range cases {
        var got = transformToXML(tc.raw);
        if got != tc.want {
        t.Errorf("got %q, want %q", got, tc.want);
    }
    }
    }

    public static void TestTrailingWhitespaceLen(*testing.T t) {
        var cases = []struct {
        desc String;
        s    String;
        want int;
        }{
        {desc: "no whitespace", s: "abc", want: 0},;
        {desc: "trailing whitespace", s: "abc ", want: 1},;
        {desc: "trailing whitespace with newlines", s: "abc \n", want: 2},;
        {desc: "only whitespace", s: " \n  ", want: 4},;
        {desc: "leading whitespace doesn't count", s: " \n abc", want: 0},;
        {desc: "unicode with trailing space", s: "测试🎯 ", want: 1},;
        {desc: "unicode with trailing tab and newline", s: "مرحبا\t\n", want: 2},;
    }
        var for _, tc = range cases {
        var got = trailingWhitespaceLen(tc.s);
        if got != tc.want {
        t.Errorf("got %d, want %d", got, tc.want);
    }
    }
    }

    public static void TestOverlapFunction(*testing.T t) {
        var cases = []struct {
        desc  String;
        s     String;
        delim String;
        want  int;
        }{
        {desc: "no overlap", s: "hello", delim: "<tool", want: 0},;
        {desc: "full overlap", s: "hello<tool", delim: "<tool>", want: 5},;
        {desc: "partial overlap", s: "hello<to", delim: "<tool>", want: 3},;
        {desc: "unicode with partial overlap", s: "测试🎯<to", delim: "<tool>", want: 3},;
        {desc: "unicode String with no overlap", s: "مرحبا", delim: "<tool>", want: 0},;
        {desc: "unicode at boundary", s: "世界<", delim: "<tool>", want: 1},;
        {desc: "unicode delimiter single rune", s: "hello🔧", delim: "🔧工具", want: len("🔧")},;
        {desc: "unicode delimiter multiple runes", s: "hello🔧工", delim: "🔧工具", want: len("🔧工")},;
    }
        var for _, tc = range cases {
        t.Run(tc.desc, func(t *testing.T) {
        var got = overlap(tc.s, tc.delim);
        if got != tc.want {
        t.Errorf("overlap(%q, %q) = %d, want %d", tc.s, tc.delim, got, tc.want);
    }
        });
    }
    }
}
