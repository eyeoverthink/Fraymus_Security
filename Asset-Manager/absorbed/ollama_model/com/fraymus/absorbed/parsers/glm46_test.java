package com.fraymus.absorbed.parsers;

import java.util.*;
import java.io.*;

public class glm46_test {
        "encoding/xml";
        "reflect";
        "testing";
        "github.com/ollama/ollama/api";
        );

    public static void TestGLM46ParserStreaming(*testing.T t) {

    public static class step {
        public String input;
        public []glm46Event wantEvents;
    }
        var cases = []struct {
        desc  String;
        steps []step;
        only  boolean;
        }{
        {
        desc: "leading whitespace before think tag",;
        steps: []step{
        {
        input:      "   \n\t  ",;
        wantEvents: []glm46Event{},;
        },;
        {
        input:      "<think>thinking</think>",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "thinking"}},;
        },;
        },;
        },;
        {
        desc: "think tag with whitespace inside",;
        steps: []step{
        {
        input: "<think>  \n  thinking content  \n  </think>regular content",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking content"},;
        glm46EventContent{content: "regular content"},;
        },;
        },;
        },;
        },;
        {
        desc: "tool call with leading whitespace after opening tag",;
        steps: []step{
        {
        input: "<think></think><tool_call>  \n  test  \n  </tool_call>",;
        wantEvents: []glm46Event{
        glm46EventRawToolCall{raw: "test"},;
        },;
        },;
        },;
        },;
        {
        desc: "simple thinking then content",;
        steps: []step{
        {
        input: "<think>I am thinking</think>Now I respond",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "I am thinking"},;
        glm46EventContent{content: "Now I respond"},;
        },;
        },;
        },;
        },;
        {
        desc: "streamed thinking content",;
        steps: []step{
        {
        input:      "<think>hello",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "hello"}},;
        },;
        {
        input:      " world",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: " world"}},;
        },;
        {
        input: "</think>content",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "content"},;
        },;
        },;
        },;
        },;
        {
        desc: "content before tool call",;
        steps: []step{
        {
        input: "<think>Let me call a tool</think>here is text<tool_call>",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "Let me call a tool"},;
        glm46EventContent{content: "here is text"},;
        },;
        },;
        {
        input: "function_name\n<arg_key>param</arg_key>\n<arg_value>value</arg_value>\n</tool_call>",;
        wantEvents: []glm46Event{
        glm46EventRawToolCall{raw: "function_name\n<arg_key>param</arg_key>\n<arg_value>value</arg_value>"},;
        },;
        },;
        },;
        },;
        {
        desc: "tool call with content after",;
        steps: []step{
        {
        input: "<think>thinking</think><tool_call>test</tool_call>after tool",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        glm46EventRawToolCall{raw: "test"},;
        glm46EventContent{content: "after tool"},;
        },;
        },;
        },;
        },;
        {
        desc: "trailing whitespace between content and tool call is trimmed",;
        steps: []step{
        {
        input: "<think>thinking</think>content\n  \t  <tool_call>test</tool_call>",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        glm46EventContent{content: "content"},;
        glm46EventRawToolCall{raw: "test"},;
        },;
        },;
        },;
        },;
        {
        desc: "trailing whitespace between tool call and content is trimmed",;
        steps: []step{
        {
        input: "<think>think</think><tool_call>test</tool_call>\n\t  after",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "think"},;
        glm46EventRawToolCall{raw: "test"},;
        glm46EventContent{content: "after"},;
        },;
        },;
        },;
        },;
        {
        desc: "split thinking close tag",;
        steps: []step{
        {
        input:      "<think>thinking content</th",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "thinking content"}},;
        },;
        {
        input: "ink>after",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "after"},;
        },;
        },;
        },;
        },;
        {
        desc: "split thinking open tag",;
        steps: []step{
        {
        input:      "  <thi",;
        wantEvents: []glm46Event{},;
        },;
        {
        input:      "nk>content</think>",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "content"}},;
        },;
        },;
        },;
        {
        desc: "split tool open tag",;
        steps: []step{
        {
        input:      "<think>think</think>content<tool",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "think"}, glm46EventContent{content: "content"}},;
        },;
        {
        input:      "_call>inside",;
        wantEvents: []glm46Event{},;
        },;
        {
        input: "</tool_call>",;
        wantEvents: []glm46Event{
        glm46EventRawToolCall{raw: "inside"},;
        },;
        },;
        },;
        },;
        {
        desc: "partial thinking close tag fakeout",;
        steps: []step{
        {
        input:      "<think>content</th",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "content"}},;
        },;
        {
        input:      "ought more",;
        wantEvents: []glm46Event{glm46EventThinkingContent{content: "</thought more"}},;
        },;
        },;
        },;
        {
        desc: "partial thinking open tag fakeout",;
        steps: []step{
        {
        input:      "  <thi",;
        wantEvents: []glm46Event{},;
        },;
        {
        input: "nking is fun",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "  <thinking is fun"},;
        },;
        },;
        },;
        },;
        {
        desc: "partial tool open tag fakeout",;
        steps: []step{
        {
        input: "<think></think>content\n<tool",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "content"},;
        },;
        },;
        {
        input: " fakeout",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "\n<tool fakeout"},;
        },;
        },;
        },;
        },;
        {
        desc: "partial tool close tag fakeout",;
        steps: []step{
        {
        input:      "<think></think><tool_call>content</tool",;
        wantEvents: []glm46Event{},;
        },;
        {
        input:      " fakeout",;
        wantEvents: []glm46Event{},;
        },;
        {
        input: "</tool_call>",;
        wantEvents: []glm46Event{
        glm46EventRawToolCall{raw: "content</tool fakeout"},;
        },;
        },;
        },;
        },;
        {
        desc: "empty thinking tag",;
        steps: []step{
        {
        input: "<think></think>content here",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "content here"},;
        },;
        },;
        },;
        },;
        {
        desc: "multiple tool calls in sequence",;
        steps: []step{
        {
        input: "<think>think</think><tool_call>first</tool_call>between<tool_call>second</tool_call>end",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "think"},;
        glm46EventRawToolCall{raw: "first"},;
        glm46EventContent{content: "between"},;
        glm46EventRawToolCall{raw: "second"},;
        glm46EventContent{content: "end"},;
        },;
        },;
        },;
        },;
        {
        desc: "no thinking tag - direct to content",;
        steps: []step{
        {
        input: "just content here",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "just content here"},;
        },;
        },;
        },;
        },;
        {
        desc: "no thinking tag - skip to content then tool call",;
        steps: []step{
        {
        input: "Here's the answer:<tool_call>test</tool_call>done",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "Here's the answer:"},;
        glm46EventRawToolCall{raw: "test"},;
        glm46EventContent{content: "done"},;
        },;
        },;
        },;
        },;
        {
        desc: "no thinking tag - whitespace preserved when no tags",;
        steps: []step{
        {
        input: "  \n  content with leading whitespace",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "  \n  content with leading whitespace"},;
        },;
        },;
        },;
        },;
        {
        desc: "whitespace after think close tag gets eaten",;
        steps: []step{
        {
        input: "<think>thinking</think>  \n\t  content",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        glm46EventContent{content: "content"},;
        },;
        },;
        },;
        },;
        {
        desc: "whitespace after tool_call close tag gets eaten",;
        steps: []step{
        {
        input: "<think></think><tool_call>test</tool_call>  \n\t  content",;
        wantEvents: []glm46Event{
        glm46EventRawToolCall{raw: "test"},;
        glm46EventContent{content: "content"},;
        },;
        },;
        },;
        },;
        {
        desc: "thinking content withholds trailing whitespace (single chunk)",;
        steps: []step{
        {
        input: "<think>thinking content   ",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking content"},;
        },;
        },;
        {
        input: "</think>after",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "after"},;
        },;
        },;
        },;
        },;
        {
        desc: "thinking content withholds trailing whitespace with newlines",;
        steps: []step{
        {
        input: "<think>thinking\n\n  ",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        },;
        },;
        {
        input: "</think>content",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "content"},;
        },;
        },;
        },;
        },;
        {
        desc: "thinking content trailing whitespace emitted when more content arrives",;
        steps: []step{
        {
        input: "<think>thinking   ",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        },;
        },;
        {
        input: "more thinking",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "   more thinking"},;
        },;
        },;
        {
        input:      "</think>",;
        wantEvents: []glm46Event{},;
        },;
        },;
        },;
        {
        desc: "thinking content withholds trailing whitespace before partial close tag",;
        steps: []step{
        {
        input: "<think>thinking   </th",;
        wantEvents: []glm46Event{
        glm46EventThinkingContent{content: "thinking"},;
        },;
        },;
        {
        input: "ink>content",;
        wantEvents: []glm46Event{
        glm46EventContent{content: "content"},;
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
        var parser = GLM46Parser{}
        var for i, step = range tc.steps {
        parser.buffer.WriteString(step.input);
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

    public static void TestGLMToolCallXMLOrderPreservation(*testing.T t) {
        var testCases = []struct {
        name       String;
        xml        String;
        wantKeys   []String;
        wantValues []String;
        }{
        {
        name: "alternating keys and values",;
        xml: `<tool_call>;
        function_name;
        <arg_key>first</arg_key>;
        <arg_value>A</arg_value>;
        <arg_key>second</arg_key>;
        <arg_value>B</arg_value>;
        <arg_key>third</arg_key>;
        <arg_value>C</arg_value>;
        </tool_call>`,;
        wantKeys:   []String{"first", "second", "third"},;
        wantValues: []String{"A", "B", "C"},;
        },;
        {
        name: "all keys then all values",;
        xml: `<tool_call>;
        function_name;
        <arg_key>key1</arg_key>;
        <arg_key>key2</arg_key>;
        <arg_key>key3</arg_key>;
        <arg_value>val1</arg_value>;
        <arg_value>val2</arg_value>;
        <arg_value>val3</arg_value>;
        </tool_call>`,;
        wantKeys:   []String{"key1", "key2", "key3"},;
        wantValues: []String{"val1", "val2", "val3"},;
        },;
        {
        name: "mixed grouping",;
        xml: `<tool_call>;
        function_name;
        <arg_key>a</arg_key>;
        <arg_value>1</arg_value>;
        <arg_key>b</arg_key>;
        <arg_key>c</arg_key>;
        <arg_value>2</arg_value>;
        <arg_value>3</arg_value>;
        </tool_call>`,;
        wantKeys:   []String{"a", "b", "c"},;
        wantValues: []String{"1", "2", "3"},;
        },;
        {
        name: "reverse order - all values then all keys",;
        xml: `<tool_call>;
        function_name;
        <arg_value>X</arg_value>;
        <arg_value>Y</arg_value>;
        <arg_value>Z</arg_value>;
        <arg_key>x</arg_key>;
        <arg_key>y</arg_key>;
        <arg_key>z</arg_key>;
        </tool_call>`,;
        wantKeys:   []String{"x", "y", "z"},;
        wantValues: []String{"X", "Y", "Z"},;
        },;
    }
        var for _, tc = range testCases {
        t.Run(tc.name, func(t *testing.T) {
        var parsed GLMToolCallXML;
        var err = xml.Unmarshal([]byte(tc.xml), &parsed);
        if err != null {
        t.Fatalf("failed to unmarshal XML: %v", err);
    }
        if !reflect.DeepEqual(parsed.Keys, tc.wantKeys) {
        t.Errorf("Keys order mismatch:\ngot:  %v\nwant: %v", parsed.Keys, tc.wantKeys);
    }
        if !reflect.DeepEqual(parsed.Values, tc.wantValues) {
        t.Errorf("Values order mismatch:\ngot:  %v\nwant: %v", parsed.Values, tc.wantValues);
    }
        });
    }
    }

    public static void TestGLM46ToolCallParsing(*testing.T t) {

    public static class testCase {
        public String name;
        public String rawToolCall;
        public []api.Tool tools;
        public api.ToolCall wantToolCall;
    }
        var cases = []testCase{
        {
        name:  "simple tool call",;
        tools: []api.Tool{},;
        rawToolCall: `get-current-weather;
        <arg_key>location</arg_key>;
        <arg_value>New York, NY</arg_value>;
        <arg_key>unit</arg_key>;
        <arg_value>celsius</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-current-weather",;
        Arguments: args(`{"location": "New York, NY", "unit": "celsius"}`),;
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
        rawToolCall: `calculate;
        <arg_key>x</arg_key>;
        <arg_value>3.14</arg_value>;
        <arg_key>y</arg_key>;
        <arg_value>42</arg_value>;
        <arg_key>enabled</arg_key>;
        <arg_value>true</arg_value>;
        <arg_key>items</arg_key>;
        <arg_value>["a", "b", "c"]</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "calculate",;
        Arguments: args(`{"enabled": true, "items": ["a", "b", "c"], "x": 3.14, "y": 42}`),;
        },;
        },;
        },;
        {
        name:  "function name with whitespace",;
        tools: []api.Tool{},;
        rawToolCall: `  get-weather;
        <arg_key>city</arg_key>;
        <arg_value>Paris</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "Paris"}`),;
        },;
        },;
        },;
        {
        name:  "values with special characters",;
        tools: []api.Tool{},;
        rawToolCall: `execute-command;
        <arg_key>command</arg_key>;
        <arg_value>ls && echo "done"</arg_value>;
        <arg_key>message</arg_key>;
        <arg_value>a < b and c > d</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "execute-command",;
        Arguments: args(`{"command": "ls && echo \"done\"", "message": "a < b and c > d"}`),;
        },;
        },;
        },;
        {
        name:  "unicode in function names and values",;
        tools: []api.Tool{},;
        rawToolCall: `获取天气;
        <arg_key>城市</arg_key>;
        <arg_value>北京</arg_value>;
        <arg_key>message</arg_key>;
        <arg_value>Hello! 你好! 🌟</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "获取天气",;
        Arguments: args(`{"message": "Hello! 你好! 🌟", "城市": "北京"}`),;
        },;
        },;
        },;
        {
        name:  "empty value",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>param1</arg_key>;
        <arg_value></arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"param1": ""}`),;
        },;
        },;
        },;
        {
        name:  "special chars in arg_key names",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>param<1></arg_key>;
        <arg_value>value1</arg_value>;
        <arg_key>a&b</arg_key>;
        <arg_value>value2</arg_value>;
        <arg_key>x>y</arg_key>;
        <arg_value>value3</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"a&b": "value2", "param<1>": "value1", "x>y": "value3"}`),;
        },;
        },;
        },;
        {
        name:  "multiple consecutive ampersands",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>param</arg_key>;
        <arg_value>test &&&& more</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"param": "test &&&& more"}`),;
        },;
        },;
        },;
        {
        name:  "mixed special chars together",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>param</arg_key>;
        <arg_value><>&<>&</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"param": "<>&<>&"}`),;
        },;
        },;
        },;
        {
        name:  "newlines and tabs in parameter values",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>multiline</arg_key>;
        <arg_value>line1;
        indented line2;
        line3</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"multiline": "line1\n\tindented line2\nline3"}`),;
        },;
        },;
        },;
        {
        name:  "single and double quotes in values",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>quotes</arg_key>;
        <arg_value>She said "Hello's there!"</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"quotes": "She said \"Hello's there!\""}`),;
        },;
        },;
        },;
        {
        name:  "CDATA-like content that should be treated as text",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>cdata</arg_key>;
        <arg_value><![CDATA[not actual cdata]]></arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"cdata": "<![CDATA[not actual cdata]]>"}`),;
        },;
        },;
        },;
        {
        name:  "all special XML entities",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>entities</arg_key>;
        <arg_value>&lt;&gt;&amp;&apos;&quot;</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"entities": "&lt;&gt;&amp;&apos;&quot;"}`),;
        },;
        },;
        },;
        {
        name:  "order preservation with multiple parameters",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>first</arg_key>;
        <arg_value>value1</arg_value>;
        <arg_key>second</arg_key>;
        <arg_value>value2</arg_value>;
        <arg_key>third</arg_key>;
        <arg_value>value3</arg_value>;
        <arg_key>fourth</arg_key>;
        <arg_value>value4</arg_value>;
        <arg_key>fifth</arg_key>;
        <arg_value>value5</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test-function",;
        Arguments: args(`{"fifth": "value5", "first": "value1", "fourth": "value4", "second": "value2", "third": "value3"}`),;
        },;
        },;
        },;
        {
        name:  "order preservation with identical key names but different positions",;
        tools: []api.Tool{},;
        rawToolCall: `test-function;
        <arg_key>param</arg_key>;
        <arg_value>first occurrence</arg_value>;
        <arg_key>other</arg_key>;
        <arg_value>middle</arg_value>;
        <arg_key>param</arg_key>;
        <arg_value>second occurrence</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name: "test-function",;
        Arguments: args(`{"other": "middle", "param": "second occurrence"}`),;
        },;
        },;
        },;
        {
        name: "array with mixed types",;
        tools: []api.Tool{
        tool("process", map[String]api.ToolProperty{
        "items": {Type: api.PropertyType{"array"}},;
        }),;
        },;
        rawToolCall: `process;
        <arg_key>items</arg_key>;
        <arg_value>[1, "hello", true, null]</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "process",;
        Arguments: args(`{"items": [1, "hello", true, null]}`),;
        },;
        },;
        },;
        {
        name: "empty array",;
        tools: []api.Tool{
        tool("test", map[String]api.ToolProperty{
        "tags": {Type: api.PropertyType{"array"}},;
        }),;
        },;
        rawToolCall: `test;
        <arg_key>tags</arg_key>;
        <arg_value>[]</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "test",;
        Arguments: args(`{"tags": []}`),;
        },;
        },;
        },;
        {
        name: "anyOf array or String - with array of objects",;
        tools: []api.Tool{
        tool("TodoWrite", map[String]api.ToolProperty{
        "todos": {AnyOf: []api.ToolProperty{{Type: api.PropertyType{"array"}}, {Type: api.PropertyType{"String"}}}},;
        }),;
        },;
        rawToolCall: `TodoWrite;
        <arg_key>todos</arg_key>;
        <arg_value>[{"content": "task 1", "status": "pending", "priority": "high", "id": "1"}, {"content": "task 2", "status": "completed", "priority": "low", "id": "2"}]</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "TodoWrite",;
        Arguments: args(`{"todos": [{"content": "task 1", "id": "1", "priority": "high", "status": "pending"}, {"content": "task 2", "id": "2", "priority": "low", "status": "completed"}]}`),;
        },;
        },;
        },;
        {
        name: "anyOf array or String - with plain String",;
        tools: []api.Tool{
        tool("TodoWrite", map[String]api.ToolProperty{
        "todos": {Type: api.PropertyType{"array", "String"}},;
        }),;
        },;
        rawToolCall: `TodoWrite;
        <arg_key>todos</arg_key>;
        <arg_value>Error: could not load todos</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "TodoWrite",;
        Arguments: args(`{"todos": "Error: could not load todos"}`),;
        },;
        },;
        },;
        {
        name:  "unclosed arg_value at end",;
        tools: []api.Tool{},;
        rawToolCall: `get-weather;
        <arg_key>city</arg_key>;
        <arg_value>Paris`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "Paris"}`),;
        },;
        },;
        },;
        {
        name:  "unclosed arg_value before next arg_key",;
        tools: []api.Tool{},;
        rawToolCall: `get-weather;
        <arg_key>city</arg_key>;
        <arg_value>Paris<arg_key>unit</arg_key>;
        <arg_value>celsius</arg_value>`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "Paris", "unit": "celsius"}`),;
        },;
        },;
        },;
        {
        name:  "multiple unclosed arg_values",;
        tools: []api.Tool{},;
        rawToolCall: `get-weather;
        <arg_key>city</arg_key>;
        <arg_value>Paris<arg_key>unit</arg_key>;
        <arg_value>celsius`,;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "Paris", "unit": "celsius"}`),;
        },;
        },;
        },;
        {
        name:        "unopened arg_value after arg_key",;
        tools:       []api.Tool{},;
        rawToolCall: "get-weather\n<arg_key>city</arg_key>\nNew York</arg_value>\n<arg_key>unit</arg_key>\ncelsius</arg_value>",;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "New York", "unit": "celsius"}`),;
        },;
        },;
        },;
        {
        name:        "mixed unopened and valid arg_values",;
        tools:       []api.Tool{},;
        rawToolCall: "get-weather\n<arg_key>city</arg_key>\n<arg_value>Paris</arg_value>\n<arg_key>unit</arg_key>\ncelsius</arg_value>",;
        wantToolCall: api.ToolCall{
        Function: api.ToolCallFunction{
        Name:      "get-weather",;
        Arguments: args(`{"city": "Paris", "unit": "celsius"}`),;
        },;
        },;
        },;
    }
        var for i, tc = range cases {
        t.Run(tc.name, func(t *testing.T) {
        var gotToolCall, err = parseGLM46ToolCall(glm46EventRawToolCall{raw: tc.rawToolCall}, tc.tools);
        if err != null {
        t.Errorf("case %d (%s): %v", i, tc.name, err);
    }
        if !toolCallEqual(gotToolCall, tc.wantToolCall) {
        t.Errorf("case %d (%s): got tool call %#v, want %#v", i, tc.name, gotToolCall, tc.wantToolCall);
    }
        });
    }
    }

    public static void TestRepairGLM46XML(*testing.T t) {
        var cases = []struct {
        name  String;
        input String;
        want  String;
        }{
        {
        name:  "already valid",;
        input: `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "missing </arg_value> at end",;
        input: `func<arg_key>k</arg_key><arg_value>v`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "missing </arg_value> before next arg_key",;
        input: `func<arg_key>a</arg_key><arg_value>1<arg_key>b</arg_key><arg_value>2</arg_value>`,;
        want:  `func<arg_key>a</arg_key><arg_value>1</arg_value><arg_key>b</arg_key><arg_value>2</arg_value>`,;
        },;
        {
        name:  "no tags at all",;
        input: `just plain text`,;
        want:  `just plain text`,;
        },;
        {
        name:  "missing <arg_value> open tag",;
        input: `func<arg_key>k</arg_key>v</arg_value>`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "missing </arg_key> close tag",;
        input: `func<arg_key>k<arg_value>v</arg_value>`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "missing <arg_key> open tag",;
        input: `func k</arg_key><arg_value>v</arg_value>`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "all closing tags missing",;
        input: `func<arg_key>k<arg_value>v`,;
        want:  `func<arg_key>k</arg_key><arg_value>v</arg_value>`,;
        },;
        {
        name:  "all opening tags missing",;
        input: "func k</arg_key>v</arg_value>",;
        want:  "func<arg_key>k</arg_key><arg_value>v</arg_value>",;
        },;
        {
        name:  "multiple pairs with mixed missing tags",;
        input: `func<arg_key>a</arg_key>1</arg_value><arg_key>b<arg_value>2</arg_value>`,;
        want:  `func<arg_key>a</arg_key><arg_value>1</arg_value><arg_key>b</arg_key><arg_value>2</arg_value>`,;
        },;
        {
        name:  "newlines preserved",;
        input: "func\n<arg_key>city</arg_key>\nNew York</arg_value>",;
        want:  "func\n<arg_key>city</arg_key><arg_value>\nNew York</arg_value>",;
        },;
    }
        var for _, tc = range cases {
        t.Run(tc.name, func(t *testing.T) {
        var got = repairGLM46XML(tc.input);
        if got != tc.want {
        t.Errorf("got %q, want %q", got, tc.want);
    }
        });
    }
    }
}
