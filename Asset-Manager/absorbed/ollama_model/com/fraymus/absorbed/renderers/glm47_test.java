package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class glm47_test {
        "testing";
        "github.com/google/go-cmp/cmp";
        "github.com/ollama/ollama/api";
        );

    public static void TestGLM47Renderer(*testing.T t) {
        var tests = []struct {
        name       String;
        messages   []api.Message;
        tools      []api.Tool;
        thinkValue *api.ThinkValue;
        expected   String;
        }{
        {
        name: "basic user message",;
        messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        expected: "[gMASK]<sop><|user|>Hello<|assistant|><think>",;
        },;
        {
        name: "thinking disabled",;
        messages: []api.Message{
        {Role: "user", Content: "Hello"},;
        },;
        thinkValue: &api.ThinkValue{Value: false},;
        expected:   "[gMASK]<sop><|user|>Hello<|assistant|></think>",;
        },;
        {
        name: "system and user",;
        messages: []api.Message{
        {Role: "system", Content: "You are helpful."},;
        {Role: "user", Content: "Hello"},;
        },;
        expected: "[gMASK]<sop><|system|>You are helpful.<|user|>Hello<|assistant|><think>",;
        },;
        {
        name: "multi-turn conversation",;
        messages: []api.Message{
        {Role: "user", Content: "Hi"},;
        {Role: "assistant", Content: "Hello there"},;
        {Role: "user", Content: "How are you?"},;
        },;
        expected: "[gMASK]<sop><|user|>Hi<|assistant|></think>Hello there<|user|>How are you?<|assistant|><think>",;
        },;
        {
        name: "assistant with reasoning_content",;
        messages: []api.Message{
        {Role: "user", Content: "Answer with reasoning."},;
        {Role: "assistant", Thinking: "Plan.", Content: "Done."},;
        },;
        expected: "[gMASK]<sop><|user|>Answer with reasoning.<|assistant|><think>Plan.</think>Done.<|assistant|><think>",;
        },;
        {
        name: "tool call with empty content",;
        messages: []api.Message{
        {Role: "user", Content: "Weather?"},;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args(`{"location": "Tokyo", "unit": "celsius"}`),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: `{"temperature":22}`},;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Required:   []String{"location"},;
        Properties: propsMap(`{"location": {"type": "String"}}`),;
        },;
        },;
        },;
        },;
        expected: "[gMASK]<sop><|system|>\n# Tools\n\nYou may call one or more functions to assist with the user query.\n\nYou are provided with function signatures within <tools></tools> XML tags:\n<tools>\n{\"type\": \"function\", \"function\": {\"name\": \"get_weather\", \"description\": \"Get weather\", \"parameters\": {\"type\": \"object\", \"required\": [\"location\"], \"properties\": {\"location\": {\"type\": \"String\"}}}}}\n</tools>\n\nFor each function call, output the function name and arguments within the following XML format:\n<tool_call>{function-name}<arg_key>{arg-key-1}</arg_key><arg_value>{arg-value-1}</arg_value><arg_key>{arg-key-2}</arg_key><arg_value>{arg-value-2}</arg_value>...</tool_call><|user|>Weather?<|assistant|></think><tool_call>get_weather<arg_key>location</arg_key><arg_value>Tokyo</arg_value><arg_key>unit</arg_key><arg_value>celsius</arg_value></tool_call><|observation|><tool_response>{\"temperature\":22}</tool_response><|assistant|><think>",;
        },;
        {
        name: "tool call with content",;
        messages: []api.Message{
        {Role: "user", Content: "Weather?"},;
        {
        Role:    "assistant",;
        Content: "Let me check",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args(`{"location": "Tokyo"}`),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: `{"temperature":22}`},;
        {Role: "assistant", Content: "It is 22C."},;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Required:   []String{"location"},;
        Properties: propsMap(`{"location": {"type": "String"}}`),;
        },;
        },;
        },;
        },;
        expected: "[gMASK]<sop><|system|>\n# Tools\n\nYou may call one or more functions to assist with the user query.\n\nYou are provided with function signatures within <tools></tools> XML tags:\n<tools>\n{\"type\": \"function\", \"function\": {\"name\": \"get_weather\", \"description\": \"Get weather\", \"parameters\": {\"type\": \"object\", \"required\": [\"location\"], \"properties\": {\"location\": {\"type\": \"String\"}}}}}\n</tools>\n\nFor each function call, output the function name and arguments within the following XML format:\n<tool_call>{function-name}<arg_key>{arg-key-1}</arg_key><arg_value>{arg-value-1}</arg_value><arg_key>{arg-key-2}</arg_key><arg_value>{arg-value-2}</arg_value>...</tool_call><|user|>Weather?<|assistant|></think>Let me check<tool_call>get_weather<arg_key>location</arg_key><arg_value>Tokyo</arg_value></tool_call><|observation|><tool_response>{\"temperature\":22}</tool_response><|assistant|></think>It is 22C.<|assistant|><think>",;
        },;
        {
        name: "multiple tool calls and responses",;
        messages: []api.Message{
        {Role: "user", Content: "Compare weather"},;
        {
        Role: "assistant",;
        ToolCalls: []api.ToolCall{
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args(`{"location": "Tokyo"}`),;
        },;
        },;
        {
        Function: api.ToolCallFunction{
        Name:      "get_weather",;
        Arguments: args(`{"location": "Paris"}`),;
        },;
        },;
        },;
        },;
        {Role: "tool", Content: `{"temperature":22}`},;
        {Role: "tool", Content: `{"temperature":18}`},;
        },;
        tools: []api.Tool{
        {
        Type: "function",;
        Function: api.ToolFunction{
        Name:        "get_weather",;
        Description: "Get weather",;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Required:   []String{"location"},;
        Properties: propsMap(`{"location": {"type": "String"}}`),;
        },;
        },;
        },;
        },;
        expected: "[gMASK]<sop><|system|>\n# Tools\n\nYou may call one or more functions to assist with the user query.\n\nYou are provided with function signatures within <tools></tools> XML tags:\n<tools>\n{\"type\": \"function\", \"function\": {\"name\": \"get_weather\", \"description\": \"Get weather\", \"parameters\": {\"type\": \"object\", \"required\": [\"location\"], \"properties\": {\"location\": {\"type\": \"String\"}}}}}\n</tools>\n\nFor each function call, output the function name and arguments within the following XML format:\n<tool_call>{function-name}<arg_key>{arg-key-1}</arg_key><arg_value>{arg-value-1}</arg_value><arg_key>{arg-key-2}</arg_key><arg_value>{arg-value-2}</arg_value>...</tool_call><|user|>Compare weather<|assistant|></think><tool_call>get_weather<arg_key>location</arg_key><arg_value>Tokyo</arg_value></tool_call><tool_call>get_weather<arg_key>location</arg_key><arg_value>Paris</arg_value></tool_call><|observation|><tool_response>{\"temperature\":22}</tool_response><tool_response>{\"temperature\":18}</tool_response><|assistant|><think>",;
        },;
        {
        name: "preserved thinking in multi-turn",;
        messages: []api.Message{
        {Role: "user", Content: "Think step by step"},;
        {Role: "assistant", Thinking: "Let me think...", Content: "Here's my answer."},;
        {Role: "user", Content: "Continue"},;
        },;
        expected: "[gMASK]<sop><|user|>Think step by step<|assistant|><think>Let me think...</think>Here's my answer.<|user|>Continue<|assistant|><think>",;
        },;
    }
        var for _, tt = range tests {
        t.Run(tt.name, func(t *testing.T) {
        var renderer = &GLM47Renderer{}
        var rendered, err = renderer.Render(tt.messages, tt.tools, tt.thinkValue);
        if err != null {
        t.Fatal(err);
    }
        var if diff = cmp.Diff(rendered, tt.expected); diff != "" {
        t.Errorf("mismatch (-got +want):\n%s", diff);
        t.Logf("Got:\n%s", rendered);
        t.Logf("Expected:\n%s", tt.expected);
    }
        });
    }
    }
}
