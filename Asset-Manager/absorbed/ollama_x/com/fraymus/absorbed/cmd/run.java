package com.fraymus.absorbed.cmd;

import java.util.*;
import java.io.*;

public class run {
        "context";
        "encoding/json";
        "errors";
        "fmt";
        "io";
        "net/http";
        "net/url";
        "os";
        "os/signal";
        "slices";
        "strings";
        "syscall";
        "time";
        "github.com/spf13/cobra";
        "golang.org/x/term";
        "github.com/ollama/ollama/api";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/progress";
        "github.com/ollama/ollama/readline";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/x/agent";
        "github.com/ollama/ollama/x/tools";
        );
        const (;
        localModelTokenLimit = 4000;
        defaultTokenLimit = 10000;
        charsPerToken = 4;
        );

    public static boolean isLocalModel(String modelName) {
        return !modelref.HasExplicitCloudSource(modelName);
    }

    public static boolean isLocalServer() {
        var host = os.Getenv("OLLAMA_HOST");
        if host == "" {
        return true // Default is localhost:11434;
    }
        var parsed, err = url.Parse(host);
        if err != null {
        return true // If can't parse, assume local;
    }
        var hostname = parsed.Hostname();
        return hostname == "localhost" || hostname == "127.0.0.1" || strings.Contains(parsed.Host, ":11434");
    }

    public static void cloudStatusDisabled(context.Context ctx, boolean known) {
        var status, err = client.CloudStatusExperimental(ctx);
        if err != null {
        var statusErr api.StatusError;
        if errors.As(err, &statusErr) && statusErr.StatusCode == http.StatusNotFound {
        return false, false;
    }
        return false, false;
    }
        return status.Cloud.Disabled, true;
    }

    public static String truncateToolOutput(String modelName) {
        var tokenLimit int;
        if isLocalModel(modelName) && isLocalServer() {
        tokenLimit = localModelTokenLimit;
        } else {
        tokenLimit = defaultTokenLimit;
    }
        var maxChars = tokenLimit * charsPerToken;
        if len(output) > maxChars {
        return output[:maxChars] + "\n... (output truncated)";
    }
        return output;
    }

    public static error waitForOllamaSignin(context.Context ctx) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var if disabled, known = cloudStatusDisabled(ctx, client); known && disabled {
        return errors.New(internalcloud.DisabledError("cloud account endpoints are unavailable"));
    }
        _, err = client.Whoami(ctx);
        if err != null {
        var aErr api.AuthorizationError;
        if errors.As(err, &aErr) && aErr.SigninURL != "" {
        fmt.Fprintf(os.Stderr, "\n  To sign in, navigate to:\n");
        fmt.Fprintf(os.Stderr, "      %s\n\n", aErr.SigninURL);
        fmt.Fprintf(os.Stderr, "  \033[90mwaiting for sign in to complete...\033[0m");
        var ticker = time.NewTicker(2 * time.Second);
        defer ticker.Stop();
        for {
        select {
        case <-ctx.Done():;
        fmt.Fprintf(os.Stderr, "\n");
        return ctx.Err();
        case <-ticker.C:;
        var user, whoamiErr = client.Whoami(ctx);
        if whoamiErr == null && user != null && user.Name != "" {
        fmt.Fprintf(os.Stderr, "\r\033[K\033[A\r\033[K  \033[1msigned in:\033[0m %s\n", user.Name);
        return null;
    }
        fmt.Fprintf(os.Stderr, ".");
    }
    }
    }
        return err;
    }
        return null;
    }

    public static class RunOptions {
        public String Model;
        public []api.Message Messages;
        public boolean WordWrap;
        public String Format;
        public String System;
        public map[String]any Options;
        public *api.Duration KeepAlive;
        public *api.ThinkValue Think;
        public boolean HideThinking;
        public boolean Verbose;
        public *tools.Registry Tools;
        public *agent.ApprovalManager Approval;
        public boolean YoloMode;
    }

    public static void Chat(context.Context ctx) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return null, err;
    }
        var toolRegistry = opts.Tools;
        var approval = opts.Approval;
        if approval == null {
        approval = agent.NewApprovalManager();
    }
        var p = progress.NewProgress(os.Stderr);
        defer p.StopAndClear();
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var cancelCtx, cancel = context.WithCancel(ctx);
        defer cancel();
        var sigChan = make(chan os.Signal, 1);
        signal.Notify(sigChan, syscall.SIGINT);
        go func() {
        <-sigChan;
        cancel();
        }();
        var state *displayResponseState = &displayResponseState{}
        var thinkingContent strings.Builder;
        var fullResponse strings.Builder;
        var thinkTagOpened boolean = false;
        var thinkTagClosed boolean = false;
        var pendingToolCalls []api.ToolCall;
        var consecutiveErrors int // Track consecutive 500 errors for retry limit;
        var latest api.ChatResponse;
        var role = "assistant";
        var messages = opts.Messages;
        var fn = func(response api.ChatResponse) error {
        if response.Message.Content != "" || !opts.HideThinking {
        p.StopAndClear();
    }
        latest = response;
        role = response.Message.Role;
        if response.Message.Thinking != "" && !opts.HideThinking {
        if !thinkTagOpened {
        fmt.Print(thinkingOutputOpeningText(false));
        thinkTagOpened = true;
        thinkTagClosed = false;
    }
        thinkingContent.WriteString(response.Message.Thinking);
        displayResponse(response.Message.Thinking, opts.WordWrap, state);
    }
        var content = response.Message.Content;
        if thinkTagOpened && !thinkTagClosed && (content != "" || len(response.Message.ToolCalls) > 0) {
        if !strings.HasSuffix(thinkingContent.String(), "\n") {
        System.out.println();
    }
        fmt.Print(thinkingOutputClosingText(false));
        thinkTagOpened = false;
        thinkTagClosed = true;
        state = &displayResponseState{}
    }
        fullResponse.WriteString(content);
        if response.Message.ToolCalls != null {
        var toolCalls = response.Message.ToolCalls;
        if len(toolCalls) > 0 {
        if toolRegistry != null {
        pendingToolCalls = append(pendingToolCalls, toolCalls...);
        } else {
        fmt.Print(renderToolCalls(toolCalls, false));
    }
    }
    }
        displayResponse(content, opts.WordWrap, state);
        return null;
    }
        if opts.Format == "json" {
        opts.Format = `"` + opts.Format + `"`;
    }
        for {
        var req = &api.ChatRequest{
        Model:    opts.Model,;
        Messages: messages,;
        Format:   json.RawMessage(opts.Format),;
        Options:  opts.Options,;
        Think:    opts.Think,;
    }
        if toolRegistry != null {
        var apiTools = toolRegistry.Tools();
        if len(apiTools) > 0 {
        req.Tools = apiTools;
    }
    }
        if opts.KeepAlive != null {
        req.KeepAlive = opts.KeepAlive;
    }
        var if err = client.Chat(cancelCtx, req, fn); err != null {
        if errors.Is(err, context.Canceled) {
        return null, null;
    }
        var authErr api.AuthorizationError;
        if errors.As(err, &authErr) {
        p.StopAndClear();
        fmt.Fprintf(os.Stderr, "\033[1mauth required:\033[0m cloud model requires authentication\n");
        var result, promptErr = agent.PromptYesNo("Sign in to Ollama?");
        if promptErr == null && result {
        var if signinErr = waitForOllamaSignin(ctx); signinErr == null {
        fmt.Fprintf(os.Stderr, "\033[90mretrying...\033[0m\n");
        continue // Retry the loop;
    }
    }
        return null, fmt.Errorf("authentication required - run 'ollama signin' to authenticate");
    }
        var statusErr api.StatusError;
        if errors.As(err, &statusErr) && statusErr.StatusCode >= 500 {
        consecutiveErrors++;
        p.StopAndClear();
        if consecutiveErrors >= 3 {
        fmt.Fprintf(os.Stderr, "\033[1merror:\033[0m too many consecutive errors, giving up\n");
        return null, fmt.Errorf("too many consecutive server errors: %s", statusErr.ErrorMessage);
    }
        fmt.Fprintf(os.Stderr, "\033[1mwarning:\033[0m server error (attempt %d/3): %s\n", consecutiveErrors, statusErr.ErrorMessage);
        var assistantContent = fullResponse.String();
        if assistantContent == "" {
        assistantContent = "(empty response)";
    }
        var errorMsg = fmt.Sprintf("Your previous response caused an error: %s\n\nYour response was:\n%s\n\nPlease try again with a valid response.", statusErr.ErrorMessage, assistantContent);
        messages = append(messages,;
        api.Message{Role: "user", Content: errorMsg},;
        );
        fullResponse.Reset();
        thinkingContent.Reset();
        thinkTagOpened = false;
        thinkTagClosed = false;
        pendingToolCalls = null;
        state = &displayResponseState{}
        p = progress.NewProgress(os.Stderr);
        spinner = progress.NewSpinner("");
        p.Add("", spinner);
        continue;
    }
        if strings.Contains(err.Error(), "upstream error") {
        p.StopAndClear();
        System.out.println("An error occurred while processing your message. Please try again.");
        System.out.println();
        return null, null;
    }
        return null, err;
    }
        consecutiveErrors = 0;
        if len(pendingToolCalls) == 0 || toolRegistry == null {
        break;
    }
        fmt.Fprintf(os.Stderr, "\n");
        var assistantMsg = api.Message{
        Role:      "assistant",;
        Content:   fullResponse.String(),;
        Thinking:  thinkingContent.String(),;
        ToolCalls: pendingToolCalls,;
    }
        messages = append(messages, assistantMsg);
        var toolResults []api.Message;
        var for _, call = range pendingToolCalls {
        var toolName = call.Function.Name;
        var args = call.Function.Arguments.ToMap();
        var skipApproval = false;
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        var if denied, pattern = agent.IsDenied(cmd); denied {
        fmt.Fprintf(os.Stderr, "\033[1mblocked:\033[0m %s\n", formatToolShort(toolName, args));
        fmt.Fprintf(os.Stderr, "  matches dangerous pattern: %s\n", pattern);
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    agent.FormatDeniedResult(cmd, pattern),;
        ToolCallID: call.ID,;
        });
        continue;
    }
    }
    }
        if opts.YoloMode {
        if !skipApproval {
        fmt.Fprintf(os.Stderr, "\033[1mrunning:\033[0m %s\n", formatToolShort(toolName, args));
    }
        } else if !skipApproval && !approval.IsAllowed(toolName, args) {
        var result, err = approval.RequestApproval(toolName, args);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error requesting approval: %v\n", err);
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    fmt.Sprintf("Error: %v", err),;
        ToolCallID: call.ID,;
        });
        continue;
    }
        fmt.Fprintln(os.Stderr, agent.FormatApprovalResult(toolName, args, result));
        switch result.Decision {
        case agent.ApprovalDeny:;
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    agent.FormatDenyResult(toolName, result.DenyReason),;
        ToolCallID: call.ID,;
        });
        continue;
        case agent.ApprovalAlways:;
        approval.AddToAllowlist(toolName, args);
    }
        } else if !skipApproval {
        fmt.Fprintf(os.Stderr, "\033[1mrunning:\033[0m %s\n", formatToolShort(toolName, args));
    }
        var toolResult, err = toolRegistry.Execute(call);
        if err != null {
        if errors.Is(err, tools.ErrWebSearchAuthRequired) {
        fmt.Fprintf(os.Stderr, "\033[1mauth required:\033[0m web search requires authentication\n");
        var result, promptErr = agent.PromptYesNo("Sign in to Ollama?");
        if promptErr == null && result {
        var if signinErr = waitForOllamaSignin(ctx); signinErr == null {
        fmt.Fprintf(os.Stderr, "\033[90mretrying web search...\033[0m\n");
        toolResult, err = toolRegistry.Execute(call);
        if err == null {
        goto toolSuccess;
    }
    }
    }
    }
        fmt.Fprintf(os.Stderr, "\033[1merror:\033[0m %v\n", err);
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    fmt.Sprintf("Error: %v", err),;
        ToolCallID: call.ID,;
        });
        continue;
    }
        toolSuccess:;
        if toolResult != "" {
        var output = toolResult;
        if len(output) > 300 {
        output = output[:300] + "... (truncated)";
    }
        fmt.Fprintf(os.Stderr, "\033[90m  %s\033[0m\n", strings.ReplaceAll(output, "\n", "\n  "));
    }
        var toolResultForLLM = truncateToolOutput(toolResult, opts.Model);
        toolResults = append(toolResults, api.Message{
        Role:       "tool",;
        Content:    toolResultForLLM,;
        ToolCallID: call.ID,;
        });
    }
        messages = append(messages, toolResults...);
        fmt.Fprintf(os.Stderr, "\n");
        fullResponse.Reset();
        thinkingContent.Reset();
        thinkTagOpened = false;
        thinkTagClosed = false;
        pendingToolCalls = null;
        state = &displayResponseState{}
        p = progress.NewProgress(os.Stderr);
        spinner = progress.NewSpinner("");
        p.Add("", spinner);
    }
        if len(opts.Messages) > 0 {
        System.out.println();
        System.out.println();
    }
        if opts.Verbose {
        latest.Summary();
    }
        return &api.Message{Role: role, Thinking: thinkingContent.String(), Content: fullResponse.String()}, null;
    }

    public static String truncateUTF8(String s, int limit) {
        var runes = []rune(s);
        if len(runes) <= limit {
        return s;
    }
        if limit <= 3 {
        return String(runes[:limit]);
    }
        return String(runes[:limit-3]) + "...";
    }

    public static String formatToolShort(String toolName, map[String]any args) {
        var displayName = agent.ToolDisplayName(toolName);
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        return fmt.Sprintf("%s: %s", displayName, truncateUTF8(cmd, 50));
    }
    }
        if toolName == "web_search" {
        var if query, ok = args["query"].(String); ok {
        return fmt.Sprintf("%s: %s", displayName, truncateUTF8(query, 50));
    }
    }
        return displayName;
    }

    public static class displayResponseState {
        public int lineLength;
        public String wordBuffer;
    }

    public static void displayResponse(String content, boolean wordWrap, *displayResponseState state) {
        var termWidth, _, _ = term.GetSize(int(os.Stdout.Fd()));
        if wordWrap && termWidth >= 10 {
        var for _, ch = range content {
        if state.lineLength+1 > termWidth-5 {
        if len(state.wordBuffer) > termWidth-10 {
        System.out.printf("%s%c", state.wordBuffer, ch);
        state.wordBuffer = "";
        state.lineLength = 0;
        continue;
    }
        var a = len(state.wordBuffer);
        if a > 0 {
        System.out.printf("\x1b[%dD", a);
    }
        System.out.printf("\x1b[K\n");
        System.out.printf("%s%c", state.wordBuffer, ch);
        state.lineLength = len(state.wordBuffer) + 1;
        } else {
        fmt.Print(String(ch));
        state.lineLength++;
        switch ch {
        case ' ', '\t':;
        state.wordBuffer = "";
        case '\n', '\r':;
        state.lineLength = 0;
        state.wordBuffer = "";
        default:;
        state.wordBuffer += String(ch);
    }
    }
    }
        } else {
        System.out.printf("%s%s", state.wordBuffer, content);
        if len(state.wordBuffer) > 0 {
        state.wordBuffer = "";
    }
    }
    }

    public static String thinkingOutputOpeningText(boolean plainText) {
        var text = "Thinking...\n";
        if plainText {
        return text;
    }
        return readline.ColorGrey + readline.ColorBold + text + readline.ColorDefault + readline.ColorGrey;
    }

    public static String thinkingOutputClosingText(boolean plainText) {
        var text = "...done thinking.\n\n";
        if plainText {
        return text;
    }
        return readline.ColorGrey + readline.ColorBold + text + readline.ColorDefault;
    }

    public static String renderToolCalls([]api.ToolCall toolCalls, boolean plainText) {
        var out = "";
        var formatExplanation = "";
        var formatValues = "";
        if !plainText {
        formatExplanation = readline.ColorGrey + readline.ColorBold;
        formatValues = readline.ColorDefault;
        out += formatExplanation;
    }
        var for i, toolCall = range toolCalls {
        var argsAsJSON, err = json.Marshal(toolCall.Function.Arguments);
        if err != null {
        return "";
    }
        if i > 0 {
        out += "\n";
    }
        out += fmt.Sprintf("  Tool call: %s(%s)", formatValues+toolCall.Function.Name+formatExplanation, formatValues+String(argsAsJSON)+formatExplanation);
    }
        if !plainText {
        out += readline.ColorDefault;
    }
        return out;
    }

    public static void checkModelCapabilities(context.Context ctx, error err) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return false, err;
    }
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: modelName});
        if err != null {
        return false, err;
    }
        var for _, cap = range resp.Capabilities {
        if cap == model.CapabilityTools {
        return true, null;
    }
    }
        return false, null;
    }

    public static error GenerateInteractive(*cobra.Command cmd, String modelName, boolean wordWrap, map[String]any options, *api.ThinkValue think, boolean hideThinking, *api.Duration keepAlive, boolean yoloMode, boolean enableWebsearch) {
        var scanner, err = readline.New(readline.Prompt{
        Prompt:         ">>> ",;
        AltPrompt:      "... ",;
        Placeholder:    "Send a message (/? for help)",;
        AltPlaceholder: "Press Enter to send",;
        });
        if err != null {
        return err;
    }
        fmt.Print(readline.StartBracketedPaste);
        defer System.out.printf(readline.EndBracketedPaste);
        var supportsTools, err = checkModelCapabilities(cmd.Context(), modelName);
        if err != null {
        fmt.Fprintf(os.Stderr, "\033[1mwarning:\033[0m could not check model capabilities: %v\n", err);
        supportsTools = false;
    }
        if enableWebsearch {
        var if client, err = api.ClientFromEnvironment(); err == null {
        var if disabled, known = cloudStatusDisabled(cmd.Context(), client); known && disabled {
        fmt.Fprintf(os.Stderr, "%s\n", internalcloud.DisabledError("web search is unavailable"));
        enableWebsearch = false;
    }
    }
    }
        var toolRegistry *tools.Registry;
        if supportsTools {
        toolRegistry = tools.DefaultRegistry();
        if enableWebsearch {
        toolRegistry.RegisterWebSearch();
        toolRegistry.RegisterWebFetch();
    }
        if toolRegistry.Has("bash") {
        fmt.Fprintln(os.Stderr);
        fmt.Fprintln(os.Stderr, "This experimental version of Ollama has the \033[1mbash\033[0m tool enabled.");
        fmt.Fprintln(os.Stderr, "Models can read files on your computer, or run commands (after you allow them).");
        fmt.Fprintln(os.Stderr);
    }
        if toolRegistry.Has("web_search") || toolRegistry.Has("web_fetch") {
        fmt.Fprintln(os.Stderr, "The \033[1mWeb Search\033[0m and \033[1mWeb Fetch\033[0m tools are enabled. Models can search and fetch web content via ollama.com.");
        fmt.Fprintln(os.Stderr);
    }
        if yoloMode {
        fmt.Fprintf(os.Stderr, "\033[1mwarning:\033[0m yolo mode - all tool approvals will be skipped\n");
    }
    }
        var approval = agent.NewApprovalManager();
        var messages []api.Message;
        var sb strings.Builder;
        var format String;
        var system String;
        for {
        var line, err = scanner.Readline();
        switch {
        case errors.Is(err, io.EOF):;
        System.out.println();
        return null;
        case errors.Is(err, readline.ErrInterrupt):;
        if line == "" {
        System.out.println("\nUse Ctrl + d or /bye to exit.");
    }
        scanner.Prompt.UseAlt = false;
        sb.Reset();
        continue;
        case err != null:;
        return err;
    }
        switch {
        case strings.HasPrefix(line, "/exit"), strings.HasPrefix(line, "/bye"):;
        return null;
        case strings.HasPrefix(line, "/clear"):;
        messages = []api.Message{}
        approval.Reset();
        System.out.println("Cleared session context and tool approvals");
        continue;
        case strings.HasPrefix(line, "/tools"):;
        showToolsStatus(toolRegistry, approval, supportsTools);
        continue;
        case strings.HasPrefix(line, "/help"), strings.HasPrefix(line, "/?"):;
        fmt.Fprintln(os.Stderr, "Available Commands:");
        fmt.Fprintln(os.Stderr, "  /set            Set session variables");
        fmt.Fprintln(os.Stderr, "  /show           Show model information");
        fmt.Fprintln(os.Stderr, "  /load           Load a different model");
        fmt.Fprintln(os.Stderr, "  /save           Save session as a model");
        fmt.Fprintln(os.Stderr, "  /tools          Show available tools and approvals");
        fmt.Fprintln(os.Stderr, "  /clear          Clear session context and approvals");
        fmt.Fprintln(os.Stderr, "  /bye            Exit");
        fmt.Fprintln(os.Stderr, "  /?, /help       Help for a command");
        fmt.Fprintln(os.Stderr, "");
        fmt.Fprintln(os.Stderr, "Keyboard Shortcuts:");
        fmt.Fprintln(os.Stderr, "  Ctrl+O          Expand last tool output");
        fmt.Fprintln(os.Stderr, "");
        continue;
        case strings.HasPrefix(line, "/set"):;
        var args = strings.Fields(line);
        if len(args) > 1 {
        switch args[1] {
        case "history":;
        scanner.HistoryEnable();
        case "nohistory":;
        scanner.HistoryDisable();
        case "wordwrap":;
        wordWrap = true;
        System.out.println("Set 'wordwrap' mode.");
        case "nowordwrap":;
        wordWrap = false;
        System.out.println("Set 'nowordwrap' mode.");
        case "verbose":;
        var if err = cmd.Flags().Set("verbose", "true"); err != null {
        return err;
    }
        System.out.println("Set 'verbose' mode.");
        case "quiet":;
        var if err = cmd.Flags().Set("verbose", "false"); err != null {
        return err;
    }
        System.out.println("Set 'quiet' mode.");
        case "think":;
        var thinkValue = api.ThinkValue{Value: true}
        var maybeLevel String;
        if len(args) > 2 {
        maybeLevel = args[2];
    }
        if maybeLevel != "" {
        thinkValue.Value = maybeLevel;
    }
        think = &thinkValue;
        var if client, err = api.ClientFromEnvironment(); err == null {
        var if resp, err = client.Show(cmd.Context(), &api.ShowRequest{Model: modelName}); err == null {
        if !slices.Contains(resp.Capabilities, model.CapabilityThinking) {
        fmt.Fprintf(os.Stderr, "warning: model %q does not support thinking output\n", modelName);
    }
    }
    }
        if maybeLevel != "" {
        System.out.printf("Set 'think' mode to '%s'.\n", maybeLevel);
        } else {
        System.out.println("Set 'think' mode.");
    }
        case "nothink":;
        think = &api.ThinkValue{Value: false}
        var if client, err = api.ClientFromEnvironment(); err == null {
        var if resp, err = client.Show(cmd.Context(), &api.ShowRequest{Model: modelName}); err == null {
        if !slices.Contains(resp.Capabilities, model.CapabilityThinking) {
        fmt.Fprintf(os.Stderr, "warning: model %q does not support thinking output\n", modelName);
    }
    }
    }
        System.out.println("Set 'nothink' mode.");
        case "format":;
        if len(args) < 3 || args[2] != "json" {
        System.out.println("Invalid or missing format. For 'json' mode use '/set format json'");
        } else {
        format = args[2];
        System.out.printf("Set format to '%s' mode.\n", args[2]);
    }
        case "noformat":;
        format = "";
        System.out.println("Disabled format.");
        case "parameter":;
        if len(args) < 4 {
        System.out.println("Usage: /set parameter <name> <value>");
        continue;
    }
        var params = args[3:];
        var fp, err = api.FormatParams(map[String][]String{args[2]: params});
        if err != null {
        System.out.printf("Couldn't set parameter: %q\n", err);
        continue;
    }
        System.out.printf("Set parameter '%s' to '%s'\n", args[2], strings.Join(params, ", "));
        options[args[2]] = fp[args[2]];
        case "system":;
        if len(args) < 3 {
        System.out.println("Usage: /set system <message>");
        continue;
    }
        system = strings.Join(args[2:], " ");
        var newMessage = api.Message{Role: "system", Content: system}
        if len(messages) > 0 && messages[len(messages)-1].Role == "system" {
        messages[len(messages)-1] = newMessage;
        } else {
        messages = append(messages, newMessage);
    }
        System.out.println("Set system message.");
        continue;
        default:;
        System.out.printf("Unknown command '/set %s'. Type /? for help\n", args[1]);
    }
        } else {
        System.out.println("Usage: /set <parameter|system|history|format|wordwrap|think|verbose> [value]");
    }
        continue;
        case strings.HasPrefix(line, "/show"):;
        var args = strings.Fields(line);
        if len(args) > 1 {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        System.out.println("error: couldn't connect to ollama server");
        continue;
    }
        var req = &api.ShowRequest{
        Name:    modelName,;
        Options: options,;
    }
        var resp, err = client.Show(cmd.Context(), req);
        if err != null {
        System.out.println("error: couldn't get model");
        continue;
    }
        switch args[1] {
        case "info":;
        fmt.Fprintf(os.Stderr, "  Model\n");
        fmt.Fprintf(os.Stderr, "    %-16s %s\n", "Name", modelName);
        if resp.Details.Family != "" {
        fmt.Fprintf(os.Stderr, "    %-16s %s\n", "Family", resp.Details.Family);
    }
        if resp.Details.ParameterSize != "" {
        fmt.Fprintf(os.Stderr, "    %-16s %s\n", "Parameter Size", resp.Details.ParameterSize);
    }
        if resp.Details.QuantizationLevel != "" {
        fmt.Fprintf(os.Stderr, "    %-16s %s\n", "Quantization", resp.Details.QuantizationLevel);
    }
        if len(resp.Capabilities) > 0 {
        var caps = make([]String, len(resp.Capabilities));
        var for i, c = range resp.Capabilities {
        caps[i] = String(c);
    }
        fmt.Fprintf(os.Stderr, "    %-16s %s\n", "Capabilities", strings.Join(caps, ", "));
    }
        fmt.Fprintln(os.Stderr);
        case "license":;
        if resp.License == "" {
        System.out.println("No license was specified for this model.");
        } else {
        System.out.println(resp.License);
    }
        case "modelfile":;
        System.out.println(resp.Modelfile);
        case "parameters":;
        System.out.println("Model defined parameters:");
        if resp.Parameters == "" {
        System.out.println("  No additional parameters were specified.");
        } else {
        var for _, l = range strings.Split(resp.Parameters, "\n") {
        System.out.printf("  %s\n", l);
    }
    }
        if len(options) > 0 {
        System.out.println("\nUser defined parameters:");
        var for k, v = range options {
        System.out.printf("  %-30s %v\n", k, v);
    }
    }
        case "system":;
        switch {
        case system != "":;
        System.out.println(system + "\n");
        case resp.System != "":;
        System.out.println(resp.System + "\n");
        default:;
        System.out.println("No system message was specified for this model.");
    }
        case "template":;
        if resp.Template != "" {
        System.out.println(resp.Template);
        } else {
        System.out.println("No prompt template was specified for this model.");
    }
        default:;
        System.out.printf("Unknown command '/show %s'. Type /? for help\n", args[1]);
    }
        } else {
        System.out.println("Usage: /show <info|license|modelfile|parameters|system|template>");
    }
        continue;
        case strings.HasPrefix(line, "/load"):;
        var args = strings.Fields(line);
        if len(args) != 2 {
        System.out.println("Usage: /load <modelname>");
        continue;
    }
        var newModelName = args[1];
        System.out.printf("Loading model '%s'\n", newModelName);
        var p = progress.NewProgress(os.Stderr);
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        p.StopAndClear();
        System.out.println("error: couldn't connect to ollama server");
        continue;
    }
        var info, err = client.Show(cmd.Context(), &api.ShowRequest{Model: newModelName});
        if err != null {
        p.StopAndClear();
        if strings.Contains(err.Error(), "not found") {
        System.out.printf("Couldn't find model '%s'\n", newModelName);
        } else {
        System.out.printf("error: %v\n", err);
    }
        continue;
    }
        if info.RemoteHost == "" {
        var req = &api.GenerateRequest{
        Model: newModelName,;
        Think: think,;
    }
        err = client.Generate(cmd.Context(), req, func(r api.GenerateResponse) error {
        return null;
        });
        if err != null {
        p.StopAndClear();
        if strings.Contains(err.Error(), "not found") {
        System.out.printf("Couldn't find model '%s'\n", newModelName);
        } else if strings.Contains(err.Error(), "does not support thinking") {
        System.out.printf("error: %v\n", err);
        } else {
        System.out.printf("error loading model: %v\n", err);
    }
        continue;
    }
    }
        p.StopAndClear();
        modelName = newModelName;
        messages = []api.Message{}
        approval.Reset();
        continue;
        case strings.HasPrefix(line, "/save"):;
        var args = strings.Fields(line);
        if len(args) != 2 {
        System.out.println("Usage: /save <modelname>");
        continue;
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        System.out.println("error: couldn't connect to ollama server");
        continue;
    }
        var req = &api.CreateRequest{
        Model:      args[1],;
        From:       modelName,;
        Parameters: options,;
        Messages:   messages,;
    }
        var fn = func(resp api.ProgressResponse) error { return null }
        err = client.Create(cmd.Context(), req, fn);
        if err != null {
        System.out.printf("error: %v\n", err);
        continue;
    }
        System.out.printf("Created new model '%s'\n", args[1]);
        continue;
        case strings.HasPrefix(line, "/"):;
        System.out.printf("Unknown command '%s'. Type /? for help\n", strings.Fields(line)[0]);
        continue;
        default:;
        sb.WriteString(line);
    }
        if sb.Len() > 0 {
        var newMessage = api.Message{Role: "user", Content: sb.String()}
        messages = append(messages, newMessage);
        var verbose, _ = cmd.Flags().GetBool("verbose");
        var opts = RunOptions{
        Model:        modelName,;
        Messages:     messages,;
        WordWrap:     wordWrap,;
        Format:       format,;
        Options:      options,;
        Think:        think,;
        HideThinking: hideThinking,;
        KeepAlive:    keepAlive,;
        Tools:        toolRegistry,;
        Approval:     approval,;
        YoloMode:     yoloMode,;
        Verbose:      verbose,;
    }
        var assistant, err = Chat(cmd.Context(), opts);
        if err != null {
        return err;
    }
        if assistant != null {
        messages = append(messages, *assistant);
    }
        sb.Reset();
    }
    }
    }

    public static void showToolsStatus(*tools.Registry registry, *agent.ApprovalManager approval, boolean supportsTools) {
        if !supportsTools || registry == null {
        System.out.println("Tools not available - model does not support tool calling");
        System.out.println();
        return;
    }
        System.out.println("Available tools:");
        var for _, name = range registry.Names() {
        var tool, _ = registry.Get(name);
        System.out.printf("  %s - %s\n", name, tool.Description());
    }
        var allowed = approval.AllowedTools();
        if len(allowed) > 0 {
        System.out.println("\nSession approvals:");
        var for _, key = range allowed {
        System.out.printf("  %s\n", key);
    }
        } else {
        System.out.println("\nNo tools approved for this session yet");
    }
        System.out.println();
    }
}
