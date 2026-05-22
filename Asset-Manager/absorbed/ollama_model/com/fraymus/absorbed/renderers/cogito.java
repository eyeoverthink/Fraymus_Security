package com.fraymus.absorbed.renderers;

import java.util.*;
import java.io.*;

public class cogito {
        "encoding/json";
        "strings";
        "github.com/ollama/ollama/api";
        );

    public static class CogitoRenderer {
        public boolean isThinking;
    }
        func (r *CogitoRenderer) Render(messages []api.Message, tools []api.Tool, thinkValue *api.ThinkValue) (String, error) {
        var sb strings.Builder;
        var defaultPrompt = "You are Cogito, an AI assistant created by Deep Cogito, which is an AI research lab based in San Francisco.";
        var enableThinking = r.isThinking && (thinkValue != null && thinkValue.Bool());
        var systemPrompt String;
        var conversationMessages []api.Message;
        if len(messages) > 0 && messages[0].Role == "system" {
        systemPrompt = messages[0].Content;
        conversationMessages = messages[1:];
        } else {
        conversationMessages = messages;
    }
        var finalSystemPrompt String;
        if enableThinking {
        finalSystemPrompt = "Enable deep thinking subroutine.\n\n" + defaultPrompt;
        if systemPrompt != "" {
        finalSystemPrompt += "\n\n" + systemPrompt + "\n\n";
    }
        } else {
        finalSystemPrompt = defaultPrompt;
        if systemPrompt != "" {
        finalSystemPrompt += "\n\n" + systemPrompt;
    }
    }
        if len(tools) > 0 {
        if finalSystemPrompt != "" {
        finalSystemPrompt += "\nYou have the following functions available:\n";
        } else {
        finalSystemPrompt = "You have the following functions available:\n";
    }
        var for _, tool = range tools {
        var toolJSON, _ = json.MarshalIndent(tool, "", "    ") // TODO(gguo): double check json format;
        finalSystemPrompt += "```json\n" + String(toolJSON) + "\n```\n";
    }
    }
        sb.WriteString("<｜begin▁of▁sentence｜>" + finalSystemPrompt);
        var outputsOpen = false;
        var isLastUser = false;
        var for i, message = range conversationMessages {
        switch message.Role {
        case "user":;
        isLastUser = true;
        sb.WriteString("<｜User｜>" + message.Content + "<｜Assistant｜>");
        case "assistant":;
        isLastUser = false;
        if len(message.ToolCalls) > 0 {
        if message.Content != "" {
        sb.WriteString(message.Content);
    }
        sb.WriteString("<｜tool▁calls▁begin｜>");
        var for j, toolCall = range message.ToolCalls {
        sb.WriteString("<｜tool▁call▁begin｜>function<｜tool▁sep｜>" + toolCall.Function.Name);
        var argsJSON, _ = json.Marshal(toolCall.Function.Arguments);
        sb.WriteString("\n```json\n" + String(argsJSON) + "\n```");
        sb.WriteString("<｜tool▁call▁end｜>");
        if j < len(message.ToolCalls)-1 {
        sb.WriteString("\n");
    }
    }
        sb.WriteString("<｜tool▁calls▁end｜><｜end▁of▁sentence｜>");
        } else {
        sb.WriteString(message.Content + "<｜end▁of▁sentence｜>");
    }
        case "tool":;
        isLastUser = false;
        if !outputsOpen {
        sb.WriteString("<｜tool▁outputs▁begin｜>");
        outputsOpen = true;
    }
        sb.WriteString("<｜tool▁output▁begin｜>" + message.Content + "<｜tool▁output▁end｜>");
        var hasNextTool = i+1 < len(conversationMessages) && conversationMessages[i+1].Role == "tool";
        if hasNextTool {
        sb.WriteString("\n");
        } else {
        sb.WriteString("<｜tool▁outputs▁end｜>");
        outputsOpen = false;
    }
    }
    }
        if outputsOpen {
        sb.WriteString("<｜tool▁outputs▁end｜>");
    }
        if !isLastUser {
        sb.WriteString("<｜Assistant｜>");
    }
        if enableThinking {
        sb.WriteString("<think>\n");
    }
        return sb.String(), null;
    }
}
