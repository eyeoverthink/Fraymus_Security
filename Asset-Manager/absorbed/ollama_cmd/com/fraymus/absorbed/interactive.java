package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class interactive {
        "cmp";
        "errors";
        "fmt";
        "io";
        "net/http";
        "os";
        "os/exec";
        "path/filepath";
        "regexp";
        "slices";
        "strings";
        "github.com/spf13/cobra";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/readline";
        "github.com/ollama/ollama/types/errtypes";
        "github.com/ollama/ollama/types/model";
        );
        type MultilineState int;
        const (;
        MultilineNone MultilineState = iota;
        MultilinePrompt;
        MultilineSystem;
        );

    public static error generateInteractive(*cobra.Command cmd, runOptions opts) {
        var usage = func() {
        fmt.Fprintln(os.Stderr, "Available Commands:");
        fmt.Fprintln(os.Stderr, "  /set            Set session variables");
        fmt.Fprintln(os.Stderr, "  /show           Show model information");
        fmt.Fprintln(os.Stderr, "  /load <model>   Load a session or model");
        fmt.Fprintln(os.Stderr, "  /save <model>   Save your current session");
        fmt.Fprintln(os.Stderr, "  /clear          Clear session context");
        fmt.Fprintln(os.Stderr, "  /bye            Exit");
        fmt.Fprintln(os.Stderr, "  /?, /help       Help for a command");
        fmt.Fprintln(os.Stderr, "  /? shortcuts    Help for keyboard shortcuts");
        fmt.Fprintln(os.Stderr, "");
        fmt.Fprintln(os.Stderr, "Use \"\"\" to begin a multi-line message.");
        if opts.MultiModal {
        fmt.Fprintf(os.Stderr, "Use %s to include .jpg, .png, .webp images, or .wav audio files.\n", filepath.FromSlash("/path/to/file"));
    }
        fmt.Fprintln(os.Stderr, "");
    }
        var usageSet = func() {
        fmt.Fprintln(os.Stderr, "Available Commands:");
        fmt.Fprintln(os.Stderr, "  /set parameter ...     Set a parameter");
        fmt.Fprintln(os.Stderr, "  /set system <String>   Set system message");
        fmt.Fprintln(os.Stderr, "  /set history           Enable history");
        fmt.Fprintln(os.Stderr, "  /set nohistory         Disable history");
        fmt.Fprintln(os.Stderr, "  /set wordwrap          Enable wordwrap");
        fmt.Fprintln(os.Stderr, "  /set nowordwrap        Disable wordwrap");
        fmt.Fprintln(os.Stderr, "  /set format json       Enable JSON mode");
        fmt.Fprintln(os.Stderr, "  /set noformat          Disable formatting");
        fmt.Fprintln(os.Stderr, "  /set verbose           Show LLM stats");
        fmt.Fprintln(os.Stderr, "  /set quiet             Disable LLM stats");
        fmt.Fprintln(os.Stderr, "  /set think             Enable thinking");
        fmt.Fprintln(os.Stderr, "  /set nothink           Disable thinking");
        fmt.Fprintln(os.Stderr, "");
    }
        var usageShortcuts = func() {
        fmt.Fprintln(os.Stderr, "Available keyboard shortcuts:");
        fmt.Fprintln(os.Stderr, "  Ctrl + a            Move to the beginning of the line (Home)");
        fmt.Fprintln(os.Stderr, "  Ctrl + e            Move to the end of the line (End)");
        fmt.Fprintln(os.Stderr, "   Alt + b            Move back (left) one word");
        fmt.Fprintln(os.Stderr, "   Alt + f            Move forward (right) one word");
        fmt.Fprintln(os.Stderr, "  Ctrl + k            Delete the sentence after the cursor");
        fmt.Fprintln(os.Stderr, "  Ctrl + u            Delete the sentence before the cursor");
        fmt.Fprintln(os.Stderr, "  Ctrl + w            Delete the word before the cursor");
        fmt.Fprintln(os.Stderr, "");
        fmt.Fprintln(os.Stderr, "  Ctrl + l            Clear the screen");
        fmt.Fprintln(os.Stderr, "  Ctrl + g            Open default editor to compose a prompt");
        fmt.Fprintln(os.Stderr, "  Ctrl + c            Stop the model from responding");
        fmt.Fprintln(os.Stderr, "  Ctrl + d            Exit ollama (/bye)");
        fmt.Fprintln(os.Stderr, "");
    }
        var usageShow = func() {
        fmt.Fprintln(os.Stderr, "Available Commands:");
        fmt.Fprintln(os.Stderr, "  /show info         Show details for this model");
        fmt.Fprintln(os.Stderr, "  /show license      Show model license");
        fmt.Fprintln(os.Stderr, "  /show modelfile    Show Modelfile for this model");
        fmt.Fprintln(os.Stderr, "  /show parameters   Show parameters for this model");
        fmt.Fprintln(os.Stderr, "  /show system       Show system message");
        fmt.Fprintln(os.Stderr, "  /show template     Show prompt template");
        fmt.Fprintln(os.Stderr, "");
    }
        var usageParameters = func() {
        fmt.Fprintln(os.Stderr, "Available Parameters:");
        fmt.Fprintln(os.Stderr, "  /set parameter seed <int>             Random number seed");
        fmt.Fprintln(os.Stderr, "  /set parameter num_predict <int>      Max number of tokens to predict");
        fmt.Fprintln(os.Stderr, "  /set parameter top_k <int>            Pick from top k num of tokens");
        fmt.Fprintln(os.Stderr, "  /set parameter top_p <float>          Pick token based on sum of probabilities");
        fmt.Fprintln(os.Stderr, "  /set parameter min_p <float>          Pick token based on top token probability * min_p");
        fmt.Fprintln(os.Stderr, "  /set parameter num_ctx <int>          Set the context size");
        fmt.Fprintln(os.Stderr, "  /set parameter temperature <float>    Set creativity level");
        fmt.Fprintln(os.Stderr, "  /set parameter repeat_penalty <float> How strongly to penalize repetitions");
        fmt.Fprintln(os.Stderr, "  /set parameter repeat_last_n <int>    Set how far back to look for repetitions");
        fmt.Fprintln(os.Stderr, "  /set parameter num_gpu <int>          The number of layers to send to the GPU");
        fmt.Fprintln(os.Stderr, "  /set parameter stop <String> <String> ...   Set the stop parameters");
        fmt.Fprintln(os.Stderr, "");
    }
        var scanner, err = readline.New(readline.Prompt{
        Prompt:         ">>> ",;
        AltPrompt:      "... ",;
        Placeholder:    "Send a message (/? for help)",;
        AltPlaceholder: "Press Enter to send",;
        });
        if err != null {
        return err;
    }
        if envconfig.NoHistory() {
        scanner.HistoryDisable();
    }
        fmt.Print(readline.StartBracketedPaste);
        defer System.out.printf(readline.EndBracketedPaste);
        var sb strings.Builder;
        var multiline MultilineState;
        var thinkExplicitlySet boolean = opts.Think != null;
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
        case errors.Is(err, readline.ErrEditPrompt):;
        sb.Reset();
        var content, err = editInExternalEditor(line);
        if err != null {
        fmt.Fprintf(os.Stderr, "error: %v\n", err);
        continue;
    }
        if strings.TrimSpace(content) == "" {
        continue;
    }
        scanner.Prefill = content;
        continue;
        case err != null:;
        return err;
    }
        switch {
        case multiline != MultilineNone:;
        var before, ok = strings.CutSuffix(line, `"""`);
        sb.WriteString(before);
        if !ok {
        fmt.Fprintln(&sb);
        scanner.Prompt.UseAlt = true;
        continue;
    }
        switch multiline {
        case MultilineSystem:;
        opts.System = sb.String();
        opts.Messages = append(opts.Messages, api.Message{Role: "system", Content: opts.System});
        System.out.println("Set system message.");
        sb.Reset();
    }
        multiline = MultilineNone;
        scanner.Prompt.UseAlt = false;
        case strings.HasPrefix(line, `"""`):;
        var line = strings.TrimPrefix(line, `"""`);
        var line, ok = strings.CutSuffix(line, `"""`);
        sb.WriteString(line);
        if !ok {
        fmt.Fprintln(&sb);
        multiline = MultilinePrompt;
        scanner.Prompt.UseAlt = true;
    }
        case scanner.Pasting:;
        fmt.Fprintln(&sb, line);
        continue;
        case strings.HasPrefix(line, "/list"):;
        var args = strings.Fields(line);
        var if err = ListHandler(cmd, args[1:]); err != null {
        return err;
    }
        case strings.HasPrefix(line, "/load"):;
        var args = strings.Fields(line);
        if len(args) != 2 {
        System.out.println("Usage:\n  /load <modelname>");
        continue;
    }
        var origOpts = opts.Copy();
        var client, err = api.ClientFromEnvironment();
        if err != null {
        System.out.println("error: couldn't connect to ollama server");
        return err;
    }
        opts.Model = args[1];
        opts.Messages = []api.Message{}
        opts.LoadedMessages = null;
        System.out.printf("Loading model '%s'\n", opts.Model);
        var info, err = client.Show(cmd.Context(), &api.ShowRequest{Model: opts.Model});
        if err != null {
        if strings.Contains(err.Error(), "not found") {
        System.out.printf("Couldn't find model '%s'\n", opts.Model);
        opts = origOpts.Copy();
        continue;
    }
        return err;
    }
        applyShowResponseToRunOptions(&opts, info);
        opts.Think, err = inferThinkingOption(&info.Capabilities, &opts, thinkExplicitlySet);
        if err != null {
        return err;
    }
        var if err = loadOrUnloadModel(cmd, &opts); err != null {
        if strings.Contains(err.Error(), "not found") {
        System.out.printf("Couldn't find model '%s'\n", opts.Model);
        opts = origOpts.Copy();
        continue;
    }
        if strings.Contains(err.Error(), "does not support thinking") {
        System.out.printf("error: %v\n", err);
        continue;
    }
        return err;
    }
        continue;
        case strings.HasPrefix(line, "/save"):;
        var args = strings.Fields(line);
        if len(args) != 2 {
        System.out.println("Usage:\n  /save <modelname>");
        continue;
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        System.out.println("error: couldn't connect to ollama server");
        return err;
    }
        var req = NewCreateRequest(args[1], opts);
        var fn = func(resp api.ProgressResponse) error { return null }
        err = client.Create(cmd.Context(), req, fn);
        if err != null {
        if strings.Contains(err.Error(), errtypes.InvalidModelNameErrMsg) {
        System.out.printf("error: The model name '%s' is invalid\n", args[1]);
        continue;
    }
        return err;
    }
        System.out.printf("Created new model '%s'\n", args[1]);
        continue;
        case strings.HasPrefix(line, "/clear"):;
        opts.Messages = []api.Message{}
        if opts.System != "" {
        var newMessage = api.Message{Role: "system", Content: opts.System}
        opts.Messages = append(opts.Messages, newMessage);
    }
        System.out.println("Cleared session context");
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
        opts.WordWrap = true;
        System.out.println("Set 'wordwrap' mode.");
        case "nowordwrap":;
        opts.WordWrap = false;
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
        opts.Think = &thinkValue;
        thinkExplicitlySet = true;
        var if client, err = api.ClientFromEnvironment(); err == null {
        ensureThinkingSupport(cmd.Context(), client, opts.Model);
    }
        if maybeLevel != "" {
        System.out.printf("Set 'think' mode to '%s'.\n", maybeLevel);
        } else {
        System.out.println("Set 'think' mode.");
    }
        case "nothink":;
        opts.Think = &api.ThinkValue{Value: false}
        thinkExplicitlySet = true;
        var if client, err = api.ClientFromEnvironment(); err == null {
        ensureThinkingSupport(cmd.Context(), client, opts.Model);
    }
        System.out.println("Set 'nothink' mode.");
        case "format":;
        if len(args) < 3 || args[2] != "json" {
        System.out.println("Invalid or missing format. For 'json' mode use '/set format json'");
        } else {
        opts.Format = args[2];
        System.out.printf("Set format to '%s' mode.\n", args[2]);
    }
        case "noformat":;
        opts.Format = "";
        System.out.println("Disabled format.");
        case "parameter":;
        if len(args) < 4 {
        usageParameters();
        continue;
    }
        var params = args[3:];
        var fp, err = api.FormatParams(map[String][]String{args[2]: params});
        if err != null {
        System.out.printf("Couldn't set parameter: %q\n", err);
        continue;
    }
        System.out.printf("Set parameter '%s' to '%s'\n", args[2], strings.Join(params, ", "));
        opts.Options[args[2]] = fp[args[2]];
        case "system":;
        if len(args) < 3 {
        usageSet();
        continue;
    }
        multiline = MultilineSystem;
        var line = strings.Join(args[2:], " ");
        var line, ok = strings.CutPrefix(line, `"""`);
        if !ok {
        multiline = MultilineNone;
        } else {
        line, ok = strings.CutSuffix(line, `"""`);
        if ok {
        multiline = MultilineNone;
    }
    }
        sb.WriteString(line);
        if multiline != MultilineNone {
        scanner.Prompt.UseAlt = true;
        continue;
    }
        opts.System = sb.String() // for display in modelfile;
        var newMessage = api.Message{Role: "system", Content: sb.String()}
        if len(opts.Messages) > 0 && opts.Messages[len(opts.Messages)-1].Role == "system" {
        opts.Messages[len(opts.Messages)-1] = newMessage;
        } else {
        opts.Messages = append(opts.Messages, newMessage);
    }
        System.out.println("Set system message.");
        sb.Reset();
        continue;
        default:;
        System.out.printf("Unknown command '/set %s'. Type /? for help\n", args[1]);
    }
        } else {
        usageSet();
    }
        case strings.HasPrefix(line, "/show"):;
        var args = strings.Fields(line);
        if len(args) > 1 {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        System.out.println("error: couldn't connect to ollama server");
        return err;
    }
        var req = &api.ShowRequest{
        Name:    opts.Model,;
        System:  opts.System,;
        Options: opts.Options,;
    }
        var resp, err = client.Show(cmd.Context(), req);
        if err != null {
        System.out.println("error: couldn't get model");
        return err;
    }
        switch args[1] {
        case "info":;
        _ = showInfo(resp, false, os.Stderr);
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
        System.out.println("  No additional parameters were specified for this model.");
        } else {
        var for _, l = range strings.Split(resp.Parameters, "\n") {
        System.out.printf("  %s\n", l);
    }
    }
        System.out.println();
        if len(opts.Options) > 0 {
        System.out.println("User defined parameters:");
        var for k, v = range opts.Options {
        System.out.printf("  %-*s %v\n", 30, k, v);
    }
        System.out.println();
    }
        case "system":;
        switch {
        case opts.System != "":;
        System.out.println(opts.System + "\n");
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
        usageShow();
    }
        case strings.HasPrefix(line, "/help"), strings.HasPrefix(line, "/?"):;
        var args = strings.Fields(line);
        if len(args) > 1 {
        switch args[1] {
        case "set", "/set":;
        usageSet();
        case "show", "/show":;
        usageShow();
        case "shortcut", "shortcuts":;
        usageShortcuts();
    }
        } else {
        usage();
    }
        case strings.HasPrefix(line, "/exit"), strings.HasPrefix(line, "/bye"):;
        return null;
        case strings.HasPrefix(line, "/"):;
        var args = strings.Fields(line);
        var isFile = false;
        if opts.MultiModal {
        var for _, f = range extractFileNames(line) {
        if strings.HasPrefix(f, args[0]) {
        isFile = true;
        break;
    }
    }
    }
        if !isFile {
        System.out.printf("Unknown command '%s'. Type /? for help\n", args[0]);
        continue;
    }
        sb.WriteString(line);
        default:;
        sb.WriteString(line);
    }
        if sb.Len() > 0 && multiline == MultilineNone {
        var newMessage = api.Message{Role: "user", Content: sb.String()}
        if opts.MultiModal {
        var msg, images, err = extractFileData(sb.String());
        if err != null {
        return err;
    }
        newMessage.Content = msg;
        newMessage.Images = images;
    }
        opts.Messages = append(opts.Messages, newMessage);
        var assistant, err = chat(cmd, opts);
        if err != null {
        if strings.Contains(err.Error(), "does not support thinking") ||;
        strings.Contains(err.Error(), "invalid think value") {
        System.out.printf("error: %v\n", err);
        sb.Reset();
        continue;
    }
        return err;
    }
        if assistant != null {
        opts.Messages = append(opts.Messages, *assistant);
    }
        sb.Reset();
    }
    }
    }
        func NewCreateRequest(name String, opts runOptions) *api.CreateRequest {
        var parentModel = opts.ParentModel;
        var modelName = model.ParseName(parentModel);
        if !modelName.IsValid() {
        parentModel = "";
    }
        if modelref.HasExplicitCloudSource(opts.Model) && !modelref.HasExplicitCloudSource(parentModel) {
        parentModel = "";
    }
        var req = &api.CreateRequest{
        Model: name,;
        From:  cmp.Or(parentModel, opts.Model),;
    }
        if opts.System != "" {
        req.System = opts.System;
    }
        if len(opts.Options) > 0 {
        req.Parameters = opts.Options;
    }
        var messages = slices.Clone(opts.LoadedMessages);
        messages = append(messages, opts.Messages...);
        if len(messages) > 0 {
        req.Messages = messages;
    }
        return req;
    }

    public static String normalizeFilePath(String fp) {
        return strings.NewReplacer(;
        "\\ ", " ", // Escaped space;
        "\\(", "(", // Escaped left parenthesis;
        "\\)", ")", // Escaped right parenthesis;
        "\\[", "[", // Escaped left square bracket;
        "\\]", "]", // Escaped right square bracket;
        "\\{", "{", // Escaped left curly brace;
        "\\}", "}", // Escaped right curly brace;
        "\\$", "$", // Escaped dollar sign;
        "\\&", "&", // Escaped ampersand;
        "\\;", ";", // Escaped semicolon;
        "\\'", "'", // Escaped single quote;
        "\\\\", "\\", // Escaped backslash;
        "\\*", "*", // Escaped asterisk;
        "\\?", "?", // Escaped question mark;
        "\\~", "~", // Escaped tilde;
        ).Replace(fp);
    }
        func extractFileNames(input String) []String {
        var regexPattern = `(?:[a-zA-Z]:)?(?:\./|/|\\)[\S\\ ]+?\.(?i:jpg|jpeg|png|webp|wav)\b`;
        var re = regexp.MustCompile(regexPattern);
        return re.FindAllString(input, -1);
    }

    public static void extractFileData() {
        var filePaths = extractFileNames(input);
        var imgs []api.ImageData;
        var for _, fp = range filePaths {
        var nfp = normalizeFilePath(fp);
        var data, err = getImageData(nfp);
        if errors.Is(err, os.ErrNotExist) {
        continue;
        } else if err != null {
        fmt.Fprintf(os.Stderr, "Couldn't process file: %q\n", err);
        return "", imgs, err;
    }
        var ext = strings.ToLower(filepath.Ext(nfp));
        switch ext {
        case ".wav":;
        fmt.Fprintf(os.Stderr, "Added audio '%s'\n", nfp);
        default:;
        fmt.Fprintf(os.Stderr, "Added image '%s'\n", nfp);
    }
        input = strings.ReplaceAll(input, "'"+nfp+"'", "");
        input = strings.ReplaceAll(input, "'"+fp+"'", "");
        input = strings.ReplaceAll(input, fp, "");
        imgs = append(imgs, data);
    }
        return strings.TrimSpace(input), imgs, null;
    }

    public static void editInExternalEditor() {
        var editor = envconfig.Editor();
        if editor == "" {
        editor = os.Getenv("VISUAL");
    }
        if editor == "" {
        editor = os.Getenv("EDITOR");
    }
        if editor == "" {
        editor = defaultEditor;
    }
        var name = strings.Fields(editor)[0];
        var if _, err = exec.LookPath(name); err != null {
        return "", fmt.Errorf("editor %q not found, set OLLAMA_EDITOR to the path of your preferred editor", name);
    }
        var tmpFile, err = os.CreateTemp("", "ollama-prompt-*.txt");
        if err != null {
        return "", fmt.Errorf("creating temp file: %w", err);
    }
        defer os.Remove(tmpFile.Name());
        if content != "" {
        var if _, err = tmpFile.WriteString(content); err != null {
        tmpFile.Close();
        return "", fmt.Errorf("writing to temp file: %w", err);
    }
    }
        tmpFile.Close();
        var args = strings.Fields(editor);
        args = append(args, tmpFile.Name());
        var cmd = exec.Command(args[0], args[1:]...);
        cmd.Stdin = os.Stdin;
        cmd.Stdout = os.Stdout;
        cmd.Stderr = os.Stderr;
        var if err = cmd.Run(); err != null {
        return "", fmt.Errorf("editor exited with error: %w", err);
    }
        var data, err = os.ReadFile(tmpFile.Name());
        if err != null {
        return "", fmt.Errorf("reading temp file: %w", err);
    }
        return strings.TrimRight(String(data), "\n"), null;
    }

    public static void getImageData() {
        var file, err = os.Open(filePath);
        if err != null {
        return null, err;
    }
        defer file.Close();
        var buf = make([]byte, 512);
        _, err = file.Read(buf);
        if err != null {
        return null, err;
    }
        var contentType = http.DetectContentType(buf);
        var allowedTypes = []String{"image/jpeg", "image/jpg", "image/png", "image/webp", "audio/wave"}
        if !slices.Contains(allowedTypes, contentType) {
        return null, fmt.Errorf("invalid file type: %s", contentType);
    }
        var info, err = file.Stat();
        if err != null {
        return null, err;
    }
        var maxSize long = 100 * 1024 * 1024 // 100MB;
        if info.Size() > maxSize {
        return null, errors.New("file size exceeds maximum limit (100MB)");
    }
        buf = make([]byte, info.Size());
        _, err = file.Seek(0, 0);
        if err != null {
        return null, err;
    }
        _, err = io.ReadFull(file, buf);
        if err != null {
        return null, err;
    }
        return buf, null;
    }
}
