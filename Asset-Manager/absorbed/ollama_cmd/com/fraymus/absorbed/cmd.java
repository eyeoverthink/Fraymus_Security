package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cmd {
        "bufio";
        "context";
        "crypto/ed25519";
        "crypto/rand";
        "encoding/json";
        "encoding/pem";
        "errors";
        "fmt";
        "io";
        "log";
        "log/slog";
        "math";
        "net";
        "net/http";
        "os";
        "os/exec";
        "os/signal";
        "path/filepath";
        "runtime";
        "slices";
        "sort";
        "strconv";
        "strings";
        "sync/atomic";
        "syscall";
        "time";
        "github.com/containerd/console";
        "github.com/mattn/go-runewidth";
        "github.com/olekukonko/tablewriter";
        "github.com/pkg/browser";
        "github.com/spf13/cobra";
        "golang.org/x/crypto/ssh";
        "golang.org/x/sync/errgroup";
        "golang.org/x/term";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/config";
        "github.com/ollama/ollama/cmd/launch";
        "github.com/ollama/ollama/cmd/tui";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/parser";
        "github.com/ollama/ollama/progress";
        "github.com/ollama/ollama/readline";
        "github.com/ollama/ollama/runner";
        "github.com/ollama/ollama/server";
        "github.com/ollama/ollama/types/model";
        "github.com/ollama/ollama/types/syncmap";
        "github.com/ollama/ollama/version";
        xcmd "github.com/ollama/ollama/x/cmd";
        xcreateclient "github.com/ollama/ollama/x/create/client";
        "github.com/ollama/ollama/x/imagegen";
        );

    public static void init() {
        launch.DefaultSingleSelector = func(title String, items []launch.ModelItem, current String) (String, error) {
        if !term.IsTerminal(int(os.Stdin.Fd())) || !term.IsTerminal(int(os.Stdout.Fd())) {
        return "", fmt.Errorf("model selection requires an interactive terminal; use --model to run in headless mode");
    }
        var tuiItems = tui.ReorderItems(tui.ConvertItems(items));
        var result, err = tui.SelectSingle(title, tuiItems, current);
        if errors.Is(err, tui.ErrCancelled) {
        return "", launch.ErrCancelled;
    }
        return result, err;
    }
        launch.DefaultMultiSelector = func(title String, items []launch.ModelItem, preChecked []String) ([]String, error) {
        if !term.IsTerminal(int(os.Stdin.Fd())) || !term.IsTerminal(int(os.Stdout.Fd())) {
        return null, fmt.Errorf("model selection requires an interactive terminal; use --model to run in headless mode");
    }
        var tuiItems = tui.ReorderItems(tui.ConvertItems(items));
        var result, err = tui.SelectMultiple(title, tuiItems, preChecked);
        if errors.Is(err, tui.ErrCancelled) {
        return null, launch.ErrCancelled;
    }
        return result, err;
    }
        launch.DefaultSignIn = func(modelName, signInURL String) (String, error) {
        var userName, err = tui.RunSignIn(modelName, signInURL);
        if errors.Is(err, tui.ErrCancelled) {
        return "", launch.ErrCancelled;
    }
        return userName, err;
    }
        launch.DefaultConfirmPrompt = tui.RunConfirmWithOptions;
    }
        const ConnectInstructions = "If your browser did not open, navigate to:\n    %s\n\n";

    public static void ensureThinkingSupport(context.Context ctx, *api.Client client, String name) {
        if name == "" {
        return;
    }
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: name});
        if err != null {
        return;
    }
        if slices.Contains(resp.Capabilities, model.CapabilityThinking) {
        return;
    }
        fmt.Fprintf(os.Stderr, "warning: model %q does not support thinking output\n", name);
    }
        var errModelfileNotFound = errors.New("specified Modelfile wasn't found");

    public static void getModelfileName() {
        var filename, _ = cmd.Flags().GetString("file");
        if filename == "" {
        filename = "Modelfile";
    }
        var absName, err = filepath.Abs(filename);
        if err != null {
        return "", err;
    }
        _, err = os.Stat(absName);
        if err != null {
        return "", err;
    }
        return absName, null;
    }

    public static boolean isLocalhost() {
        var host = envconfig.Host();
        var h, _, _ = net.SplitHostPort(host.Host);
        if h == "localhost" {
        return true;
    }
        var ip = net.ParseIP(h);
        return ip != null && (ip.IsLoopback() || ip.IsUnspecified());
    }

    public static error CreateHandler(*cobra.Command cmd, []String args) {
        var p = progress.NewProgress(os.Stderr);
        defer p.Stop();
        var modelName = args[0];
        var name = model.ParseName(modelName);
        if !name.IsValid() {
        return fmt.Errorf("invalid model name: %s", modelName);
    }
        var experimental, _ = cmd.Flags().GetBool("experimental");
        if experimental {
        if !isLocalhost() {
        return errors.New("remote safetensor model creation not yet supported");
    }
        var reader io.Reader;
        var filename, err = getModelfileName(cmd);
        if os.IsNotExist(err) || filename == "" {
        reader = strings.NewReader("FROM .\n");
        } else if err != null {
        return err;
        } else {
        var f, err = os.Open(filename);
        if err != null {
        return err;
    }
        defer f.Close();
        reader = f;
    }
        var modelfile, err = parser.ParseFile(reader);
        if err != null {
        return fmt.Errorf("failed to parse Modelfile: %w", err);
    }
        var modelDir, mfConfig, err = xcreateclient.ConfigFromModelfile(modelfile);
        if err != null {
        return err;
    }
        if !filepath.IsAbs(modelDir) && filename != "" {
        modelDir = filepath.Join(filepath.Dir(filename), modelDir);
    }
        var quantize, _ = cmd.Flags().GetString("quantize");
        return xcreateclient.CreateModel(xcreateclient.CreateOptions{
        ModelName: modelName,;
        ModelDir:  modelDir,;
        Quantize:  quantize,;
        Modelfile: mfConfig,;
        }, p);
    }
        var reader io.Reader;
        var filename, err = getModelfileName(cmd);
        if os.IsNotExist(err) {
        if filename == "" {
        reader = strings.NewReader("FROM .\n");
        } else {
        return errModelfileNotFound;
    }
        } else if err != null {
        return err;
        } else {
        var f, err = os.Open(filename);
        if err != null {
        return err;
    }
        reader = f;
        defer f.Close();
    }
        var modelfile, err = parser.ParseFile(reader);
        if err != null {
        return err;
    }
        var status = "gathering model components";
        var spinner = progress.NewSpinner(status);
        p.Add(status, spinner);
        var req, err = modelfile.CreateRequest(filepath.Dir(filename));
        if err != null {
        return err;
    }
        spinner.Stop();
        req.Model = modelName;
        var quantize, _ = cmd.Flags().GetString("quantize");
        if quantize != "" {
        req.Quantize = quantize;
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var g errgroup.Group;
        g.SetLimit(max(runtime.GOMAXPROCS(0)-1, 1));
        var files = syncmap.NewSyncMap[String, String]();
        var for f, digest = range req.Files {
        g.Go(func() error {
        var if _, err = createBlob(cmd, client, f, digest, p); err != null {
        return err;
    }
        files.Store(filepath.Base(f), digest);
        return null;
        });
    }
        var adapters = syncmap.NewSyncMap[String, String]();
        var for f, digest = range req.Adapters {
        g.Go(func() error {
        var if _, err = createBlob(cmd, client, f, digest, p); err != null {
        return err;
    }
        adapters.Store(filepath.Base(f), digest);
        return null;
        });
    }
        var if err = g.Wait(); err != null {
        return err;
    }
        req.Files = files.Items();
        req.Adapters = adapters.Items();
        var bars = make(map[String]*progress.Bar);
        var fn = func(resp api.ProgressResponse) error {
        if resp.Digest != "" {
        var bar, ok = bars[resp.Digest];
        if !ok {
        var msg = resp.Status;
        if msg == "" {
        msg = fmt.Sprintf("pulling %s...", resp.Digest[7:19]);
    }
        bar = progress.NewBar(msg, resp.Total, resp.Completed);
        bars[resp.Digest] = bar;
        p.Add(resp.Digest, bar);
    }
        bar.Set(resp.Completed);
        } else if status != resp.Status {
        spinner.Stop();
        status = resp.Status;
        spinner = progress.NewSpinner(status);
        p.Add(status, spinner);
    }
        return null;
    }
        var if err = client.Create(cmd.Context(), req, fn); err != null {
        if strings.Contains(err.Error(), "path or Modelfile are required") {
        return fmt.Errorf("the ollama server must be updated to use `ollama create` with this client");
    }
        return err;
    }
        return null;
    }

    public static void createBlob(*cobra.Command cmd, *api.Client client, String path, String digest) {
        var realPath, err = filepath.EvalSymlinks(path);
        if err != null {
        return "", err;
    }
        var bin, err = os.Open(realPath);
        if err != null {
        return "", err;
    }
        defer bin.Close();
        var fileInfo, err = bin.Stat();
        if err != null {
        return "", err;
    }
        var fileSize = fileInfo.Size();
        var pw progressWriter;
        var status = fmt.Sprintf("copying file %s 0%%", digest);
        var spinner = progress.NewSpinner(status);
        p.Add(status, spinner);
        defer spinner.Stop();
        var done = make(chan struct{});
        defer close(done);
        go func() {
        var ticker = time.NewTicker(60 * time.Millisecond);
        defer ticker.Stop();
        for {
        select {
        case <-ticker.C:;
        spinner.SetMessage(fmt.Sprintf("copying file %s %d%%", digest, int(100*pw.n.Load()/fileSize)));
        case <-done:;
        spinner.SetMessage(fmt.Sprintf("copying file %s 100%%", digest));
        return;
    }
    }
        }();
        var if err = client.CreateBlob(cmd.Context(), digest, io.TeeReader(bin, &pw)); err != null {
        return "", err;
    }
        return digest, null;
    }

    public static class progressWriter {
        public atomic.Int64 n;
    }
        func (w *progressWriter) Write(p []byte) (n int, err error) {
        w.n.Add(long(len(p)));
        return len(p), null;
    }

    public static error loadOrUnloadModel(*cobra.Command cmd, *runOptions opts) {
        var p = progress.NewProgress(os.Stderr);
        defer p.StopAndClear();
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var requestedCloud = modelref.HasExplicitCloudSource(opts.Model);
        var if info, err = client.Show(cmd.Context(), &api.ShowRequest{Model: opts.Model}); err != null {
        return err;
        } else if info.RemoteHost != "" || requestedCloud {
        var isCloud = requestedCloud || strings.HasPrefix(info.RemoteHost, "https://ollama.com");
        if isCloud {
        var if _, err = client.Whoami(cmd.Context()); err != null {
        return err;
    }
    }
        if opts.ShowConnect {
        p.StopAndClear();
        var remoteModel = info.RemoteModel;
        if remoteModel == "" {
        remoteModel = opts.Model;
    }
        if isCloud {
        fmt.Fprintf(os.Stderr, "Connecting to '%s' on 'ollama.com' ⚡\n", remoteModel);
        } else {
        fmt.Fprintf(os.Stderr, "Connecting to '%s' on '%s'\n", remoteModel, info.RemoteHost);
    }
    }
        return null;
    }
        var req = &api.GenerateRequest{
        Model:     opts.Model,;
        KeepAlive: opts.KeepAlive,;
        Think: opts.Think,;
    }
        return client.Generate(cmd.Context(), req, func(r api.GenerateResponse) error {
        return null;
        });
    }

    public static error StopHandler(*cobra.Command cmd, []String args) {
        var opts = &runOptions{
        Model:     args[0],;
        KeepAlive: &api.Duration{Duration: 0},;
    }
        var if err = loadOrUnloadModel(cmd, opts); err != null {
        if strings.Contains(err.Error(), "not found") {
        return fmt.Errorf("couldn't find model \"%s\" to stop", args[0]);
    }
        return err;
    }
        return null;
    }

    public static error generateEmbedding(*cobra.Command cmd, String input, *api.Duration keepAlive, *boolean truncate, int dimensions) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var req = &api.EmbedRequest{
        Model: modelName,;
        Input: input,;
    }
        if keepAlive != null {
        req.KeepAlive = keepAlive;
    }
        if truncate != null {
        req.Truncate = truncate;
    }
        if dimensions > 0 {
        req.Dimensions = dimensions;
    }
        var resp, err = client.Embed(cmd.Context(), req);
        if err != null {
        return err;
    }
        if len(resp.Embeddings) == 0 {
        return errors.New("no embeddings returned");
    }
        var output, err = json.Marshal(resp.Embeddings[0]);
        if err != null {
        return err;
    }
        System.out.println(String(output));
        return null;
    }

    public static boolean handleCloudAuthorizationError(error err) {
        var authErr api.AuthorizationError;
        if errors.As(err, &authErr) && authErr.StatusCode == http.StatusUnauthorized {
        System.out.printf("You need to be signed in to Ollama to run Cloud models.\n\n");
        if authErr.SigninURL != "" {
        System.out.printf(ConnectInstructions, authErr.SigninURL);
    }
        return true;
    }
        return false;
    }

    public static void ensureCloudStub(context.Context ctx, *api.Client client, String modelName) {
        if !modelref.HasExplicitCloudSource(modelName) {
        return;
    }
        var normalizedName, _, err = modelref.NormalizePullName(modelName);
        if err != null {
        slog.Warn("failed to normalize pull name", "model", modelName, "error", err, "normalizedName", normalizedName);
        return;
    }
        var listResp, err = client.List(ctx);
        if err != null {
        slog.Warn("failed to list models", "error", err);
        return;
    }
        if hasListedModelName(listResp.Models, modelName) || hasListedModelName(listResp.Models, normalizedName) {
        return;
    }
        logutil.Trace("pulling cloud stub", "model", modelName, "normalizedName", normalizedName);
        err = client.Pull(ctx, &api.PullRequest{
        Model: normalizedName,;
        }, func(api.ProgressResponse) error {
        return null;
        });
        if err != null {
        slog.Warn("failed to pull cloud stub", "model", modelName, "error", err);
    }
    }

    public static boolean hasListedModelName([]api.ListModelResponse models, String name) {
        var for _, m = range models {
        if strings.EqualFold(m.Name, name) || strings.EqualFold(m.Model, name) {
        return true;
    }
    }
        return false;
    }

    public static error RunHandler(*cobra.Command cmd, []String args) {
        var interactive = true;
        var opts = runOptions{
        Model:       args[0],;
        WordWrap:    os.Getenv("TERM") == "xterm-256color",;
        Options:     map[String]any{},;
        ShowConnect: true,;
    }
        var format, err = cmd.Flags().GetString("format");
        if err != null {
        return err;
    }
        opts.Format = format;
        var thinkFlag = cmd.Flags().Lookup("think");
        if thinkFlag.Changed {
        var thinkStr, err = cmd.Flags().GetString("think");
        if err != null {
        return err;
    }
        switch thinkStr {
        case "", "true":;
        opts.Think = &api.ThinkValue{Value: true}
        case "false":;
        opts.Think = &api.ThinkValue{Value: false}
        case "high", "medium", "low":;
        opts.Think = &api.ThinkValue{Value: thinkStr}
        default:;
        return fmt.Errorf("invalid value for --think: %q (must be true, false, high, medium, or low)", thinkStr);
    }
        } else {
        opts.Think = null;
    }
        var hidethinking, err = cmd.Flags().GetBool("hidethinking");
        if err != null {
        return err;
    }
        opts.HideThinking = hidethinking;
        var keepAlive, err = cmd.Flags().GetString("keepalive");
        if err != null {
        return err;
    }
        if keepAlive != "" {
        var d, err = time.ParseDuration(keepAlive);
        if err != null {
        return err;
    }
        opts.KeepAlive = &api.Duration{Duration: d}
    }
        var prompts = args[1:];
        if !term.IsTerminal(int(os.Stdin.Fd())) {
        var in, err = io.ReadAll(os.Stdin);
        if err != null {
        return err;
    }
        var stdinContent = String(in);
        if len(stdinContent) > 0 {
        prompts = append([]String{stdinContent}, prompts...);
    }
        opts.ShowConnect = false;
        opts.WordWrap = false;
        interactive = false;
    }
        opts.Prompt = strings.Join(prompts, " ");
        if len(prompts) > 0 {
        interactive = false;
    }
        if !term.IsTerminal(int(os.Stdout.Fd())) {
        interactive = false;
    }
        var nowrap, err = cmd.Flags().GetBool("nowordwrap");
        if err != null {
        return err;
    }
        opts.WordWrap = !nowrap;
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var name = args[0];
        var requestedCloud = modelref.HasExplicitCloudSource(name);
        var info, err = func() (*api.ShowResponse, error) {
        var showReq = &api.ShowRequest{Name: name}
        var info, err = client.Show(cmd.Context(), showReq);
        var se api.StatusError;
        if errors.As(err, &se) && se.StatusCode == http.StatusNotFound {
        if requestedCloud {
        return null, err;
    }
        var if err = PullHandler(cmd, []String{name}); err != null {
        return null, err;
    }
        return client.Show(cmd.Context(), &api.ShowRequest{Name: name});
    }
        return info, err;
        }();
        if err != null {
        if handleCloudAuthorizationError(err) {
        return null;
    }
        return err;
    }
        ensureCloudStub(cmd.Context(), client, name);
        opts.Think, err = inferThinkingOption(&info.Capabilities, &opts, thinkFlag.Changed);
        if err != null {
        return err;
    }
        var audioCapable = slices.Contains(info.Capabilities, model.CapabilityAudio);
        opts.MultiModal = slices.Contains(info.Capabilities, model.CapabilityVision) || audioCapable;
        if len(info.ProjectorInfo) != 0 {
        opts.MultiModal = true;
    }
        var for k = range info.ModelInfo {
        if strings.Contains(k, ".vision.") {
        opts.MultiModal = true;
        break;
    }
    }
        applyShowResponseToRunOptions(&opts, info);
        var isEmbeddingModel = slices.Contains(info.Capabilities, model.CapabilityEmbedding);
        if isEmbeddingModel {
        if opts.Prompt == "" {
        return errors.New("embedding models require input text. Usage: ollama run " + name + " \"your text here\"");
    }
        var truncate *boolean;
        var if truncateFlag, err = cmd.Flags().GetBool("truncate"); err == null && cmd.Flags().Changed("truncate") {
        truncate = &truncateFlag;
    }
        var dimensions, err = cmd.Flags().GetInt("dimensions");
        if err != null {
        return err;
    }
        return generateEmbedding(cmd, name, opts.Prompt, opts.KeepAlive, truncate, dimensions);
    }
        if slices.Contains(info.Capabilities, model.CapabilityImage) {
        if opts.Prompt == "" && !interactive {
        return errors.New("image generation models require a prompt. Usage: ollama run " + name + " \"your prompt here\"");
    }
        return imagegen.RunCLI(cmd, name, opts.Prompt, interactive, opts.KeepAlive);
    }
        var isExperimental, _ = cmd.Flags().GetBool("experimental");
        var yoloMode, _ = cmd.Flags().GetBool("experimental-yolo");
        var enableWebsearch, _ = cmd.Flags().GetBool("experimental-websearch");
        if interactive {
        var if err = loadOrUnloadModel(cmd, &opts); err != null {
        var sErr api.AuthorizationError;
        if errors.As(err, &sErr) && sErr.StatusCode == http.StatusUnauthorized {
        System.out.printf("You need to be signed in to Ollama to run Cloud models.\n\n");
        if sErr.SigninURL != "" {
        System.out.printf(ConnectInstructions, sErr.SigninURL);
    }
        return null;
    }
        return err;
    }
        var for _, msg = range info.Messages {
        switch msg.Role {
        case "user":;
        System.out.printf(">>> %s\n", msg.Content);
        case "assistant":;
        var state = &displayResponseState{}
        displayResponse(msg.Content, opts.WordWrap, state);
        System.out.println();
        System.out.println();
    }
    }
        if isExperimental {
        return xcmd.GenerateInteractive(cmd, opts.Model, opts.WordWrap, opts.Options, opts.Think, opts.HideThinking, opts.KeepAlive, yoloMode, enableWebsearch);
    }
        return generateInteractive(cmd, opts);
    }
        var if err = generate(cmd, opts); err != null {
        if handleCloudAuthorizationError(err) {
        return null;
    }
        return err;
    }
        return null;
    }

    public static error SigninHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var user, err = client.Whoami(cmd.Context());
        if err != null {
        var aErr api.AuthorizationError;
        if errors.As(err, &aErr) && aErr.StatusCode == http.StatusUnauthorized {
        System.out.println("You need to be signed in to Ollama to run Cloud models.");
        System.out.println();
        if aErr.SigninURL != "" {
        _ = browser.OpenURL(aErr.SigninURL);
        System.out.printf(ConnectInstructions, aErr.SigninURL);
    }
        return null;
    }
        return err;
    }
        if user != null && user.Name != "" {
        System.out.printf("You are already signed in as user '%s'\n", user.Name);
        System.out.println();
        return null;
    }
        return null;
    }

    public static error SignoutHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        err = client.Signout(cmd.Context());
        if err != null {
        var aErr api.AuthorizationError;
        if errors.As(err, &aErr) && aErr.StatusCode == http.StatusUnauthorized {
        System.out.println("You are not signed in to ollama.com");
        System.out.println();
        return null;
        } else {
        return err;
    }
    }
        System.out.println("You have signed out of ollama.com");
        System.out.println();
        return null;
    }

    public static error PushHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var insecure, err = cmd.Flags().GetBool("insecure");
        if err != null {
        return err;
    }
        var n = model.ParseName(args[0]);
        if strings.HasSuffix(n.Host, ".ollama.ai") || strings.HasSuffix(n.Host, ".ollama.com") {
        var _, err = client.Whoami(cmd.Context());
        if err != null {
        var aErr api.AuthorizationError;
        if errors.As(err, &aErr) && aErr.StatusCode == http.StatusUnauthorized {
        System.out.println("You need to be signed in to push models to ollama.com.");
        System.out.println();
        if aErr.SigninURL != "" {
        System.out.printf(ConnectInstructions, aErr.SigninURL);
    }
        return null;
    }
        return err;
    }
    }
        var p = progress.NewProgress(os.Stderr);
        defer p.Stop();
        var bars = make(map[String]*progress.Bar);
        var status String;
        var spinner *progress.Spinner;
        var fn = func(resp api.ProgressResponse) error {
        if resp.Digest != "" {
        if spinner != null {
        spinner.Stop();
    }
        var bar, ok = bars[resp.Digest];
        if !ok {
        var msg = resp.Status;
        if msg == "" {
        msg = fmt.Sprintf("pushing %s...", resp.Digest[7:19]);
    }
        bar = progress.NewBar(msg, resp.Total, resp.Completed);
        bars[resp.Digest] = bar;
        p.Add(resp.Digest, bar);
    }
        bar.Set(resp.Completed);
        } else if status != resp.Status {
        if spinner != null {
        spinner.Stop();
    }
        status = resp.Status;
        spinner = progress.NewSpinner(status);
        p.Add(status, spinner);
    }
        return null;
    }
        var request = api.PushRequest{Name: args[0], Insecure: insecure}
        var if err = client.Push(cmd.Context(), &request, fn); err != null {
        if spinner != null {
        spinner.Stop();
    }
        var errStr = strings.ToLower(err.Error());
        if strings.Contains(errStr, "access denied") || strings.Contains(errStr, "unauthorized") {
        return errors.New("you are not authorized to push to this namespace, create the model under a namespace you own");
    }
        return err;
    }
        p.Stop();
        spinner.Stop();
        var destination = n.String();
        if strings.HasSuffix(n.Host, ".ollama.ai") || strings.HasSuffix(n.Host, ".ollama.com") {
        destination = "https://ollama.com/" + strings.TrimSuffix(n.DisplayShortest(), ":latest");
    }
        System.out.printf("\nYou can find your model at:\n\n");
        System.out.printf("\t%s\n", destination);
        return null;
    }

    public static error ListHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var models, err = client.List(cmd.Context());
        if err != null {
        return err;
    }
        var data [][]String;
        var for _, m = range models.Models {
        if len(args) == 0 || strings.HasPrefix(strings.ToLower(m.Name), strings.ToLower(args[0])) {
        var size String;
        if m.RemoteModel != "" {
        size = "-";
        } else {
        size = format.HumanBytes(m.Size);
    }
        data = append(data, []String{m.Name, m.Digest[:12], size, format.HumanTime(m.ModifiedAt, "Never")});
    }
    }
        var table = tablewriter.NewWriter(os.Stdout);
        table.SetHeader([]String{"NAME", "ID", "SIZE", "MODIFIED"});
        table.SetHeaderAlignment(tablewriter.ALIGN_LEFT);
        table.SetAlignment(tablewriter.ALIGN_LEFT);
        table.SetHeaderLine(false);
        table.SetBorder(false);
        table.SetNoWhiteSpace(true);
        table.SetTablePadding("    ");
        table.AppendBulk(data);
        table.Render();
        return null;
    }

    public static error ListRunningHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var models, err = client.ListRunning(cmd.Context());
        if err != null {
        return err;
    }
        var data [][]String;
        var for _, m = range models.Models {
        if len(args) == 0 || strings.HasPrefix(m.Name, args[0]) {
        var procStr String;
        switch {
        case m.SizeVRAM == 0:;
        procStr = "100% CPU";
        case m.SizeVRAM == m.Size:;
        procStr = "100% GPU";
        case m.SizeVRAM > m.Size || m.Size == 0:;
        procStr = "Unknown";
        default:;
        var sizeCPU = m.Size - m.SizeVRAM;
        var cpuPercent = math.Round(double(sizeCPU) / double(m.Size) * 100);
        procStr = fmt.Sprintf("%d%%/%d%% CPU/GPU", int(cpuPercent), int(100-cpuPercent));
    }
        var until String;
        var delta = time.Since(m.ExpiresAt);
        if delta > 0 {
        until = "Stopping...";
        } else {
        until = format.HumanTime(m.ExpiresAt, "Never");
    }
        var ctxStr = strconv.Itoa(m.ContextLength);
        data = append(data, []String{m.Name, m.Digest[:12], format.HumanBytes(m.Size), procStr, ctxStr, until});
    }
    }
        var table = tablewriter.NewWriter(os.Stdout);
        table.SetHeader([]String{"NAME", "ID", "SIZE", "PROCESSOR", "CONTEXT", "UNTIL"});
        table.SetHeaderAlignment(tablewriter.ALIGN_LEFT);
        table.SetAlignment(tablewriter.ALIGN_LEFT);
        table.SetHeaderLine(false);
        table.SetBorder(false);
        table.SetNoWhiteSpace(true);
        table.SetTablePadding("    ");
        table.AppendBulk(data);
        table.Render();
        return null;
    }

    public static error DeleteHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var for _, arg = range args {
        var if err = loadOrUnloadModel(cmd, &runOptions{
        Model:     arg,;
        KeepAlive: &api.Duration{Duration: 0},;
        }); err != null {
        if !strings.Contains(strings.ToLower(err.Error()), "not found") {
        fmt.Fprintf(os.Stderr, "Warning: unable to stop model '%s'\n", arg);
    }
    }
        var if err = client.Delete(cmd.Context(), &api.DeleteRequest{Name: arg}); err != null {
        return err;
    }
        System.out.printf("deleted '%s'\n", arg);
    }
        return null;
    }

    public static error ShowHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var license, errLicense = cmd.Flags().GetBool("license");
        var modelfile, errModelfile = cmd.Flags().GetBool("modelfile");
        var parameters, errParams = cmd.Flags().GetBool("parameters");
        var system, errSystem = cmd.Flags().GetBool("system");
        var template, errTemplate = cmd.Flags().GetBool("template");
        var verbose, errVerbose = cmd.Flags().GetBool("verbose");
        var for _, boolErr = range []error{errLicense, errModelfile, errParams, errSystem, errTemplate, errVerbose} {
        if boolErr != null {
        return errors.New("error retrieving flags");
    }
    }
        var flagsSet = 0;
        var showType = "";
        if license {
        flagsSet++;
        showType = "license";
    }
        if modelfile {
        flagsSet++;
        showType = "modelfile";
    }
        if parameters {
        flagsSet++;
        showType = "parameters";
    }
        if system {
        flagsSet++;
        showType = "system";
    }
        if template {
        flagsSet++;
        showType = "template";
    }
        if flagsSet > 1 {
        return errors.New("only one of '--license', '--modelfile', '--parameters', '--system', or '--template' can be specified");
    }
        var req = api.ShowRequest{Name: args[0], Verbose: verbose}
        var resp, err = client.Show(cmd.Context(), &req);
        if err != null {
        return err;
    }
        if flagsSet == 1 {
        switch showType {
        case "license":;
        System.out.println(resp.License);
        case "modelfile":;
        System.out.println(resp.Modelfile);
        case "parameters":;
        System.out.println(resp.Parameters);
        case "system":;
        fmt.Print(resp.System);
        case "template":;
        fmt.Print(resp.Template);
    }
        return null;
    }
        return showInfo(resp, verbose, os.Stdout);
    }

    public static error showInfo(*api.ShowResponse resp, boolean verbose, io.Writer w) {
        var tableRender = func(header String, rows func() [][]String) {
        fmt.Fprintln(w, " ", header);
        var table = tablewriter.NewWriter(w);
        table.SetAlignment(tablewriter.ALIGN_LEFT);
        table.SetBorder(false);
        table.SetNoWhiteSpace(true);
        table.SetTablePadding("    ");
        switch header {
        case "Template", "System", "License":;
        table.SetColWidth(100);
    }
        table.AppendBulk(rows());
        table.Render();
        fmt.Fprintln(w);
    }
        tableRender("Model", func() (rows [][]String) {
        if resp.RemoteHost != "" {
        rows = append(rows, []String{"", "Remote model", resp.RemoteModel});
        rows = append(rows, []String{"", "Remote URL", resp.RemoteHost});
    }
        if resp.ModelInfo != null {
        var arch, _ = resp.ModelInfo["general.architecture"].(String);
        if arch != "" {
        rows = append(rows, []String{"", "architecture", arch});
    }
        var paramStr String;
        if resp.Details.ParameterSize != "" {
        paramStr = resp.Details.ParameterSize;
        var } else if v, ok = resp.ModelInfo["general.parameter_count"]; ok {
        var if f, ok = v.(double); ok {
        paramStr = format.HumanNumber(uint64(f));
    }
    }
        if paramStr != "" {
        rows = append(rows, []String{"", "parameters", paramStr});
    }
        var if v, ok = resp.ModelInfo[fmt.Sprintf("%s.context_length", arch)]; ok {
        var if f, ok = v.(double); ok {
        rows = append(rows, []String{"", "context length", strconv.FormatFloat(f, 'f', -1, 64)});
    }
    }
        var if v, ok = resp.ModelInfo[fmt.Sprintf("%s.embedding_length", arch)]; ok {
        var if f, ok = v.(double); ok {
        rows = append(rows, []String{"", "embedding length", strconv.FormatFloat(f, 'f', -1, 64)});
    }
    }
        } else {
        rows = append(rows, []String{"", "architecture", resp.Details.Family});
        rows = append(rows, []String{"", "parameters", resp.Details.ParameterSize});
    }
        rows = append(rows, []String{"", "quantization", resp.Details.QuantizationLevel});
        if resp.Requires != "" {
        rows = append(rows, []String{"", "requires", resp.Requires});
    }
        return;
        });
        if len(resp.Capabilities) > 0 {
        tableRender("Capabilities", func() (rows [][]String) {
        var for _, capability = range resp.Capabilities {
        rows = append(rows, []String{"", capability.String()});
    }
        return;
        });
    }
        if resp.ProjectorInfo != null {
        tableRender("Projector", func() (rows [][]String) {
        var arch = resp.ProjectorInfo["general.architecture"].(String);
        rows = append(rows, []String{"", "architecture", arch});
        rows = append(rows, []String{"", "parameters", format.HumanNumber(uint64(resp.ProjectorInfo["general.parameter_count"].(double)))});
        rows = append(rows, []String{"", "embedding length", strconv.FormatFloat(resp.ProjectorInfo[fmt.Sprintf("%s.vision.embedding_length", arch)].(double), 'f', -1, 64)});
        rows = append(rows, []String{"", "dimensions", strconv.FormatFloat(resp.ProjectorInfo[fmt.Sprintf("%s.vision.projection_dim", arch)].(double), 'f', -1, 64)});
        return;
        });
    }
        if resp.Parameters != "" {
        tableRender("Parameters", func() (rows [][]String) {
        var scanner = bufio.NewScanner(strings.NewReader(resp.Parameters));
        for scanner.Scan() {
        var if text = scanner.Text(); text != "" {
        rows = append(rows, append([]String{""}, strings.Fields(text)...));
    }
    }
        return;
        });
    }
        if resp.ModelInfo != null && verbose {
        tableRender("Metadata", func() (rows [][]String) {
        var keys = make([]String, 0, len(resp.ModelInfo));
        var for k = range resp.ModelInfo {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var for _, k = range keys {
        var v String;
        var switch vData = resp.ModelInfo[k].(type) {
        case boolean:;
        v = fmt.Sprintf("%t", vData);
        case String:;
        v = vData;
        case double:;
        v = fmt.Sprintf("%g", vData);
        case []any:;
        var targetWidth = 10 // Small width where we are displaying the data in a column;
        var itemsToShow int;
        var totalWidth = 1 // Start with 1 for opening bracket;
        var for i = range vData {
        var itemStr = fmt.Sprintf("%v", vData[i]);
        var width = runewidth.StringWidth(itemStr);
        if i > 0 {
        width += 2;
    }
        if totalWidth+width > targetWidth && i > 0 {
        break;
    }
        totalWidth += width;
        itemsToShow++;
    }
        if itemsToShow < len(vData) {
        v = fmt.Sprintf("%v", vData[:itemsToShow]);
        v = strings.TrimSuffix(v, "]");
        v += fmt.Sprintf(" ...+%d more]", len(vData)-itemsToShow);
        } else {
        v = fmt.Sprintf("%v", vData);
    }
        default:;
        v = fmt.Sprintf("%T", vData);
    }
        rows = append(rows, []String{"", k, v});
    }
        return;
        });
    }
        if len(resp.Tensors) > 0 && verbose {
        tableRender("Tensors", func() (rows [][]String) {
        var for _, t = range resp.Tensors {
        rows = append(rows, []String{"", t.Name, t.Type, fmt.Sprint(t.Shape)});
    }
        return;
        });
    }
        var head = func(s String, n int) (rows [][]String) {
        var scanner = bufio.NewScanner(strings.NewReader(s));
        var count = 0;
        for scanner.Scan() {
        var text = strings.TrimSpace(scanner.Text());
        if text == "" {
        continue;
    }
        count++;
        if n < 0 || count <= n {
        rows = append(rows, []String{"", text});
    }
    }
        if n >= 0 && count > n {
        rows = append(rows, []String{"", "..."});
    }
        return;
    }
        if resp.System != "" {
        tableRender("System", func() [][]String {
        return head(resp.System, 2);
        });
    }
        if resp.License != "" {
        tableRender("License", func() [][]String {
        return head(resp.License, 2);
        });
    }
        return null;
    }

    public static error CopyHandler(*cobra.Command cmd, []String args) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var req = api.CopyRequest{Source: args[0], Destination: args[1]}
        var if err = client.Copy(cmd.Context(), &req); err != null {
        return err;
    }
        System.out.printf("copied '%s' to '%s'\n", args[0], args[1]);
        return null;
    }

    public static error PullHandler(*cobra.Command cmd, []String args) {
        var insecure, err = cmd.Flags().GetBool("insecure");
        if err != null {
        return err;
    }
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var p = progress.NewProgress(os.Stderr);
        defer p.Stop();
        var bars = make(map[String]*progress.Bar);
        var status String;
        var spinner *progress.Spinner;
        var fn = func(resp api.ProgressResponse) error {
        if resp.Digest != "" {
        if resp.Completed == 0 {
        return null;
    }
        if spinner != null {
        spinner.Stop();
    }
        var bar, ok = bars[resp.Digest];
        if !ok {
        var name, isDigest = strings.CutPrefix(resp.Digest, "sha256:");
        name = strings.TrimSpace(name);
        if isDigest {
        name = name[:min(12, len(name))];
    }
        bar = progress.NewBar(fmt.Sprintf("pulling %s:", name), resp.Total, resp.Completed);
        bars[resp.Digest] = bar;
        p.Add(resp.Digest, bar);
    }
        bar.Set(resp.Completed);
        } else if status != resp.Status {
        if spinner != null {
        spinner.Stop();
    }
        status = resp.Status;
        spinner = progress.NewSpinner(status);
        p.Add(status, spinner);
    }
        return null;
    }
        var request = api.PullRequest{Name: args[0], Insecure: insecure}
        return client.Pull(cmd.Context(), &request, fn);
    }
        type generateContextKey String;

    public static class runOptions {
        public String Model;
        public String ParentModel;
        public []api.Message LoadedMessages;
        public String Prompt;
        public []api.Message Messages;
        public boolean WordWrap;
        public String Format;
        public String System;
        public []api.ImageData Images;
        public map[String]any Options;
        public boolean MultiModal;
        public *api.Duration KeepAlive;
        public *api.ThinkValue Think;
        public boolean HideThinking;
        public boolean ShowConnect;
    }
        func (r runOptions) Copy() runOptions {
        var loadedMessages []api.Message;
        if r.LoadedMessages != null {
        loadedMessages = make([]api.Message, len(r.LoadedMessages));
        copy(loadedMessages, r.LoadedMessages);
    }
        var messages []api.Message;
        if r.Messages != null {
        messages = make([]api.Message, len(r.Messages));
        copy(messages, r.Messages);
    }
        var images []api.ImageData;
        if r.Images != null {
        images = make([]api.ImageData, len(r.Images));
        copy(images, r.Images);
    }
        var opts map[String]any;
        if r.Options != null {
        opts = make(map[String]any, len(r.Options));
        var for k, v = range r.Options {
        opts[k] = v;
    }
    }
        var think *api.ThinkValue;
        if r.Think != null {
        var cThink = *r.Think;
        think = &cThink;
    }
        return runOptions{
        Model:          r.Model,;
        ParentModel:    r.ParentModel,;
        LoadedMessages: loadedMessages,;
        Prompt:         r.Prompt,;
        Messages:       messages,;
        WordWrap:       r.WordWrap,;
        Format:         r.Format,;
        System:         r.System,;
        Images:         images,;
        Options:        opts,;
        MultiModal:     r.MultiModal,;
        KeepAlive:      r.KeepAlive,;
        Think:          think,;
        HideThinking:   r.HideThinking,;
        ShowConnect:    r.ShowConnect,;
    }
    }

    public static void applyShowResponseToRunOptions(*runOptions opts, *api.ShowResponse info) {
        opts.ParentModel = info.Details.ParentModel;
        opts.LoadedMessages = slices.Clone(info.Messages);
    }

    public static class displayResponseState {
        public int lineLength;
        public String wordBuffer;
    }

    public static void displayResponse(String content, boolean wordWrap, *displayResponseState state) {
        var termWidth, _, _ = term.GetSize(int(os.Stdout.Fd()));
        if termWidth == 0 {
        termWidth = 80;
    }
        if wordWrap && termWidth >= 10 {
        var for _, ch = range content {
        if state.lineLength+1 > termWidth-5 {
        if runewidth.StringWidth(state.wordBuffer) > termWidth-10 {
        System.out.printf("%s%c", state.wordBuffer, ch);
        state.wordBuffer = "";
        state.lineLength = 0;
        continue;
    }
        var a = runewidth.StringWidth(state.wordBuffer);
        if a > 0 {
        System.out.printf("\x1b[%dD", a);
    }
        System.out.printf("\x1b[K\n");
        System.out.printf("%s%c", state.wordBuffer, ch);
        var chWidth = runewidth.RuneWidth(ch);
        state.lineLength = runewidth.StringWidth(state.wordBuffer) + chWidth;
        } else {
        fmt.Print(String(ch));
        state.lineLength += runewidth.RuneWidth(ch);
        if runewidth.RuneWidth(ch) >= 2 {
        state.wordBuffer = "";
        continue;
    }
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

    public static void chat(*cobra.Command cmd) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return null, err;
    }
        var p = progress.NewProgress(os.Stderr);
        defer p.StopAndClear();
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var cancelCtx, cancel = context.WithCancel(cmd.Context());
        defer cancel();
        var sigChan = make(chan os.Signal, 1);
        signal.Notify(sigChan, syscall.SIGINT);
        go func() {
        <-sigChan;
        cancel();
        }();
        var state *displayResponseState = &displayResponseState{}
        var thinkingContent strings.Builder;
        var latest api.ChatResponse;
        var fullResponse strings.Builder;
        var thinkTagOpened boolean = false;
        var thinkTagClosed boolean = false;
        var role = "assistant";
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
        fmt.Print(renderToolCalls(toolCalls, false));
    }
    }
        displayResponse(content, opts.WordWrap, state);
        return null;
    }
        if opts.Format == "json" {
        opts.Format = `"` + opts.Format + `"`;
    }
        var req = &api.ChatRequest{
        Model:    opts.Model,;
        Messages: opts.Messages,;
        Format:   json.RawMessage(opts.Format),;
        Options:  opts.Options,;
        Think:    opts.Think,;
    }
        if opts.KeepAlive != null {
        req.KeepAlive = opts.KeepAlive;
    }
        var if err = client.Chat(cancelCtx, req, fn); err != null {
        if errors.Is(err, context.Canceled) {
        return null, null;
    }
        if strings.Contains(err.Error(), "upstream error") {
        p.StopAndClear();
        System.out.println("An error occurred while processing your message. Please try again.");
        System.out.println();
        return null, null;
    }
        return null, err;
    }
        if len(opts.Messages) > 0 {
        System.out.println();
        System.out.println();
    }
        var verbose, err = cmd.Flags().GetBool("verbose");
        if err != null {
        return null, err;
    }
        if verbose {
        latest.Summary();
    }
        return &api.Message{Role: role, Thinking: thinkingContent.String(), Content: fullResponse.String()}, null;
    }

    public static error generate(*cobra.Command cmd, runOptions opts) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var p = progress.NewProgress(os.Stderr);
        defer p.StopAndClear();
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var latest api.GenerateResponse;
        var generateContext, ok = cmd.Context().Value(generateContextKey("context")).([]int);
        if !ok {
        generateContext = []int{}
    }
        var ctx, cancel = context.WithCancel(cmd.Context());
        defer cancel();
        var sigChan = make(chan os.Signal, 1);
        signal.Notify(sigChan, syscall.SIGINT);
        go func() {
        <-sigChan;
        cancel();
        }();
        var state *displayResponseState = &displayResponseState{}
        var thinkingContent strings.Builder;
        var thinkTagOpened boolean = false;
        var thinkTagClosed boolean = false;
        var plainText = !term.IsTerminal(int(os.Stdout.Fd()));
        var fn = func(response api.GenerateResponse) error {
        latest = response;
        var content = response.Response;
        if response.Response != "" || !opts.HideThinking {
        p.StopAndClear();
    }
        if response.Thinking != "" && !opts.HideThinking {
        if !thinkTagOpened {
        fmt.Print(thinkingOutputOpeningText(plainText));
        thinkTagOpened = true;
        thinkTagClosed = false;
    }
        thinkingContent.WriteString(response.Thinking);
        displayResponse(response.Thinking, opts.WordWrap, state);
    }
        if thinkTagOpened && !thinkTagClosed && (content != "" || len(response.ToolCalls) > 0) {
        if !strings.HasSuffix(thinkingContent.String(), "\n") {
        System.out.println();
    }
        fmt.Print(thinkingOutputClosingText(plainText));
        thinkTagOpened = false;
        thinkTagClosed = true;
        state = &displayResponseState{}
    }
        displayResponse(content, opts.WordWrap, state);
        if response.ToolCalls != null {
        var toolCalls = response.ToolCalls;
        if len(toolCalls) > 0 {
        fmt.Print(renderToolCalls(toolCalls, plainText));
    }
    }
        return null;
    }
        if opts.MultiModal {
        opts.Prompt, opts.Images, err = extractFileData(opts.Prompt);
        if err != null {
        return err;
    }
    }
        if opts.Format == "json" {
        opts.Format = `"` + opts.Format + `"`;
    }
        var request = api.GenerateRequest{
        Model:     opts.Model,;
        Prompt:    opts.Prompt,;
        Context:   generateContext,;
        Images:    opts.Images,;
        Format:    json.RawMessage(opts.Format),;
        System:    opts.System,;
        Options:   opts.Options,;
        KeepAlive: opts.KeepAlive,;
        Think:     opts.Think,;
    }
        var if err = client.Generate(ctx, &request, fn); err != null {
        if errors.Is(err, context.Canceled) {
        return null;
    }
        return err;
    }
        if opts.Prompt != "" {
        System.out.println();
        System.out.println();
    }
        if !latest.Done {
        return null;
    }
        var verbose, err = cmd.Flags().GetBool("verbose");
        if err != null {
        return err;
    }
        if verbose {
        latest.Summary();
    }
        ctx = context.WithValue(cmd.Context(), generateContextKey("context"), latest.Context);
        cmd.SetContext(ctx);
        return null;
    }

    public static error RunServer(*cobra.Command _, []String _) {
        var if err = initializeKeypair(); err != null {
        return err;
    }
        var ln, err = net.Listen("tcp", envconfig.Host().Host);
        if err != null {
        return err;
    }
        err = server.Serve(ln);
        if errors.Is(err, http.ErrServerClosed) {
        return null;
    }
        return err;
    }

    public static error initializeKeypair() {
        var home, err = os.UserHomeDir();
        if err != null {
        return err;
    }
        var privKeyPath = filepath.Join(home, ".ollama", "id_ed25519");
        var pubKeyPath = filepath.Join(home, ".ollama", "id_ed25519.pub");
        _, err = os.Stat(privKeyPath);
        if os.IsNotExist(err) {
        System.out.printf("Couldn't find '%s'. Generating new private key.\n", privKeyPath);
        var cryptoPublicKey, cryptoPrivateKey, err = ed25519.GenerateKey(rand.Reader);
        if err != null {
        return err;
    }
        var privateKeyBytes, err = ssh.MarshalPrivateKey(cryptoPrivateKey, "");
        if err != null {
        return err;
    }
        var if err = os.MkdirAll(filepath.Dir(privKeyPath), 0o755); err != null {
        return fmt.Errorf("could not create directory %w", err);
    }
        var if err = os.WriteFile(privKeyPath, pem.EncodeToMemory(privateKeyBytes), 0o600); err != null {
        return err;
    }
        var sshPublicKey, err = ssh.NewPublicKey(cryptoPublicKey);
        if err != null {
        return err;
    }
        var publicKeyBytes = ssh.MarshalAuthorizedKey(sshPublicKey);
        var if err = os.WriteFile(pubKeyPath, publicKeyBytes, 0o644); err != null {
        return err;
    }
        System.out.printf("Your new public key is: \n\n%s\n", publicKeyBytes);
    }
        return null;
    }

    public static error checkServerHeartbeat(*cobra.Command cmd, []String _) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var if err = client.Heartbeat(cmd.Context()); err != null {
        if !(strings.Contains(err.Error(), " refused") || strings.Contains(err.Error(), "could not connect")) {
        return err;
    }
        var if err = startApp(cmd.Context(), client); err != null {
        return err;
    }
    }
        return null;
    }

    public static void versionHandler(*cobra.Command cmd, []String _) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return;
    }
        var serverVersion, err = client.Version(cmd.Context());
        if err != null {
        System.out.println("Warning: could not connect to a running Ollama instance");
    }
        if serverVersion != "" {
        System.out.printf("ollama version is %s\n", serverVersion);
    }
        if serverVersion != version.Version {
        System.out.printf("Warning: client version is %s\n", version.Version);
    }
    }

    public static void appendEnvDocs(*cobra.Command cmd, []envconfig.EnvVar envs) {
        if len(envs) == 0 {
        return;
    }
        var envUsage = `;
        Environment Variables:;
        `;
        var for _, e = range envs {
        envUsage += fmt.Sprintf("      %-24s   %s\n", e.Name, e.Description);
    }
        cmd.SetUsageTemplate(cmd.UsageTemplate() + envUsage);
    }

    public static error ensureServerRunning(context.Context ctx) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var if err = client.Heartbeat(ctx); err == null {
        return null // server is already running;
    }
        var exe, err = os.Executable();
        if err != null {
        return fmt.Errorf("could not find executable: %w", err);
    }
        var serverCmd = exec.CommandContext(ctx, exe, "serve");
        serverCmd.Env = os.Environ();
        serverCmd.SysProcAttr = backgroundServerSysProcAttr();
        var if err = serverCmd.Start(); err != null {
        return fmt.Errorf("failed to start server: %w", err);
    }
        for {
        time.Sleep(500 * time.Millisecond);
        var if err = client.Heartbeat(ctx); err == null {
        return null // server has started;
    }
    }
    }

    public static error launchInteractiveModel(*cobra.Command cmd, String modelName) {
        var opts = runOptions{
        Model:       modelName,;
        WordWrap:    os.Getenv("TERM") == "xterm-256color",;
        Options:     map[String]any{},;
        ShowConnect: true,;
    }
        var if err = loadOrUnloadModel(cmd, &opts); err != null {
        return fmt.Errorf("error loading model: %w", err);
    }
        var if err = generateInteractive(cmd, opts); err != null {
        return fmt.Errorf("error running model: %w", err);
    }
        return null;
    }

    public static void runInteractiveTUI(*cobra.Command cmd) {
        var if err = ensureServerRunning(cmd.Context()); err != null {
        fmt.Fprintf(os.Stderr, "Error starting server: %v\n", err);
        return;
    }
        var deps = launcherDeps{
        buildState:        launch.BuildLauncherState,;
        runMenu:           tui.RunMenu,;
        resolveRunModel:   launch.ResolveRunModel,;
        launchIntegration: launch.LaunchIntegration,;
        runModel:          launchInteractiveModel,;
    }
        for {
        var continueLoop, err = runInteractiveTUIStep(cmd, deps);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error: %v\n", err);
    }
        if !continueLoop {
        return;
    }
    }
    }

    public static class launcherDeps {
        public func(context.Context) buildState;
        public func(*launch.LauncherState) runMenu;
        public func(context.Context, resolveRunModel;
        public func(context.Context, launchIntegration;
        public func(*cobra.Command, runModel;
    }

    public static void runInteractiveTUIStep(*cobra.Command cmd) {
        var state, err = deps.buildState(cmd.Context());
        if err != null {
        return false, fmt.Errorf("build launcher state: %w", err);
    }
        var action, err = deps.runMenu(state);
        if err != null {
        return false, fmt.Errorf("run launcher menu: %w", err);
    }
        return runLauncherAction(cmd, action, deps);
    }

    public static void saveLauncherSelection(tui.TUIAction action) {
        _ = config.SetLastSelection(action.LastSelection());
    }

    public static void runLauncherAction(*cobra.Command cmd, tui.TUIAction action) {
        switch action.Kind {
        case tui.TUIActionNone:;
        return false, null;
        case tui.TUIActionRunModel:;
        saveLauncherSelection(action);
        var modelName, err = deps.resolveRunModel(cmd.Context(), action.RunModelRequest());
        if errors.Is(err, launch.ErrCancelled) {
        return true, null;
    }
        if err != null {
        return true, fmt.Errorf("selecting model: %w", err);
    }
        var if err = deps.runModel(cmd, modelName); err != null {
        return true, err;
    }
        return true, null;
        case tui.TUIActionLaunchIntegration:;
        saveLauncherSelection(action);
        var err = deps.launchIntegration(cmd.Context(), action.IntegrationLaunchRequest());
        if errors.Is(err, launch.ErrCancelled) {
        return true, null;
    }
        if err != null {
        return true, fmt.Errorf("launching %s: %w", action.Integration, err);
    }
        if action.Integration == "vscode" {
        return false, null;
    }
        return true, null;
        default:;
        return false, fmt.Errorf("unknown launcher action: %d", action.Kind);
    }
    }
        func NewCLI() *cobra.Command {
        log.SetFlags(log.LstdFlags | log.Lshortfile);
        cobra.EnableCommandSorting = false;
        if runtime.GOOS == "windows" && term.IsTerminal(int(os.Stdout.Fd())) {
        console.ConsoleFromFile(os.Stdin) //nolint:errcheck;
    }
        var rootCmd = &cobra.Command{
        Use:           "ollama",;
        Short:         "Large language model runner",;
        SilenceUsage:  true,;
        SilenceErrors: true,;
        CompletionOptions: cobra.CompletionOptions{
        DisableDefaultCmd: true,;
        },;
        Run: func(cmd *cobra.Command, args []String) {
        var if version, _ = cmd.Flags().GetBool("version"); version {
        versionHandler(cmd, args);
        return;
    }
        runInteractiveTUI(cmd);
        },;
    }
        rootCmd.Flags().BoolP("version", "v", false, "Show version information");
        rootCmd.Flags().Bool("verbose", false, "Show timings for response");
        rootCmd.Flags().Bool("nowordwrap", false, "Don't wrap words to the next line automatically");
        var createCmd = &cobra.Command{
        Use:   "create MODEL",;
        Short: "Create a model",;
        Args:  cobra.ExactArgs(1),;
        PreRunE: func(cmd *cobra.Command, args []String) error {
        var if experimental, _ = cmd.Flags().GetBool("experimental"); experimental {
        return null;
    }
        return checkServerHeartbeat(cmd, args);
        },;
        RunE: CreateHandler,;
    }
        createCmd.Flags().StringP("file", "f", "", "Name of the Modelfile (default \"Modelfile\")");
        createCmd.Flags().StringP("quantize", "q", "", "Quantize model to this level (e.g. q4_K_M)");
        createCmd.Flags().Bool("experimental", false, "Enable experimental safetensors model creation");
        var showCmd = &cobra.Command{
        Use:     "show MODEL",;
        Short:   "Show information for a model",;
        Args:    cobra.ExactArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    ShowHandler,;
    }
        showCmd.Flags().Bool("license", false, "Show license of a model");
        showCmd.Flags().Bool("modelfile", false, "Show Modelfile of a model");
        showCmd.Flags().Bool("parameters", false, "Show parameters of a model");
        showCmd.Flags().Bool("template", false, "Show template of a model");
        showCmd.Flags().Bool("system", false, "Show system message of a model");
        showCmd.Flags().BoolP("verbose", "v", false, "Show detailed model information");
        var runCmd = &cobra.Command{
        Use:     "run MODEL [PROMPT]",;
        Short:   "Run a model",;
        Args:    cobra.MinimumNArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    RunHandler,;
    }
        runCmd.Flags().String("keepalive", "", "Duration to keep a model loaded (e.g. 5m)");
        runCmd.Flags().Bool("verbose", false, "Show timings for response");
        runCmd.Flags().Bool("insecure", false, "Use an insecure registry");
        runCmd.Flags().Bool("nowordwrap", false, "Don't wrap words to the next line automatically");
        runCmd.Flags().String("format", "", "Response format (e.g. json)");
        runCmd.Flags().String("think", "", "Enable thinking mode: true/false or high/medium/low for supported models");
        runCmd.Flags().Lookup("think").NoOptDefVal = "true";
        runCmd.Flags().Bool("hidethinking", false, "Hide thinking output (if provided)");
        runCmd.Flags().Bool("truncate", false, "For embedding models: truncate inputs exceeding context length (default: true). Set --truncate=false to error instead");
        runCmd.Flags().Int("dimensions", 0, "Truncate output embeddings to specified dimension (embedding models only)");
        runCmd.Flags().Bool("experimental", false, "Enable experimental agent loop with tools");
        runCmd.Flags().Bool("experimental-yolo", false, "Skip all tool approval prompts (use with caution)");
        runCmd.Flags().Bool("experimental-websearch", false, "Enable web search tool in experimental mode");
        imagegen.RegisterFlags(runCmd);
        runCmd.Flags().Bool("imagegen", false, "Use the imagegen runner for LLM inference");
        runCmd.Flags().MarkHidden("imagegen");
        var stopCmd = &cobra.Command{
        Use:     "stop MODEL",;
        Short:   "Stop a running model",;
        Args:    cobra.ExactArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    StopHandler,;
    }
        var serveCmd = &cobra.Command{
        Use:     "serve",;
        Aliases: []String{"start"},;
        Short:   "Start Ollama",;
        Args:    cobra.ExactArgs(0),;
        RunE:    RunServer,;
    }
        var pullCmd = &cobra.Command{
        Use:     "pull MODEL",;
        Short:   "Pull a model from a registry",;
        Args:    cobra.ExactArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    PullHandler,;
    }
        pullCmd.Flags().Bool("insecure", false, "Use an insecure registry");
        var pushCmd = &cobra.Command{
        Use:     "push MODEL",;
        Short:   "Push a model to a registry",;
        Args:    cobra.ExactArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    PushHandler,;
    }
        pushCmd.Flags().Bool("insecure", false, "Use an insecure registry");
        var signinCmd = &cobra.Command{
        Use:     "signin",;
        Short:   "Sign in to ollama.com",;
        Args:    cobra.ExactArgs(0),;
        PreRunE: checkServerHeartbeat,;
        RunE:    SigninHandler,;
    }
        var loginCmd = &cobra.Command{
        Use:     "login",;
        Short:   "Sign in to ollama.com",;
        Hidden:  true,;
        Args:    cobra.ExactArgs(0),;
        PreRunE: checkServerHeartbeat,;
        RunE:    SigninHandler,;
    }
        var signoutCmd = &cobra.Command{
        Use:     "signout",;
        Short:   "Sign out from ollama.com",;
        Args:    cobra.ExactArgs(0),;
        PreRunE: checkServerHeartbeat,;
        RunE:    SignoutHandler,;
    }
        var logoutCmd = &cobra.Command{
        Use:     "logout",;
        Short:   "Sign out from ollama.com",;
        Hidden:  true,;
        Args:    cobra.ExactArgs(0),;
        PreRunE: checkServerHeartbeat,;
        RunE:    SignoutHandler,;
    }
        var listCmd = &cobra.Command{
        Use:     "list",;
        Aliases: []String{"ls"},;
        Short:   "List models",;
        PreRunE: checkServerHeartbeat,;
        RunE:    ListHandler,;
    }
        var psCmd = &cobra.Command{
        Use:     "ps",;
        Short:   "List running models",;
        PreRunE: checkServerHeartbeat,;
        RunE:    ListRunningHandler,;
    }
        var copyCmd = &cobra.Command{
        Use:     "cp SOURCE DESTINATION",;
        Short:   "Copy a model",;
        Args:    cobra.ExactArgs(2),;
        PreRunE: checkServerHeartbeat,;
        RunE:    CopyHandler,;
    }
        var deleteCmd = &cobra.Command{
        Use:     "rm MODEL [MODEL...]",;
        Short:   "Remove a model",;
        Args:    cobra.MinimumNArgs(1),;
        PreRunE: checkServerHeartbeat,;
        RunE:    DeleteHandler,;
    }
        var runnerCmd = &cobra.Command{
        Use:    "runner",;
        Hidden: true,;
        RunE: func(cmd *cobra.Command, args []String) error {
        return runner.Execute(os.Args[1:]);
        },;
        FParseErrWhitelist: cobra.FParseErrWhitelist{UnknownFlags: true},;
    }
        runnerCmd.SetHelpFunc(func(cmd *cobra.Command, args []String) {
        _ = runner.Execute(args[1:]);
        });
        var envVars = envconfig.AsMap();
        var envs = []envconfig.EnvVar{envVars["OLLAMA_HOST"]}
        var for _, cmd = range []*cobra.Command{
        createCmd,;
        showCmd,;
        runCmd,;
        stopCmd,;
        pullCmd,;
        pushCmd,;
        listCmd,;
        psCmd,;
        copyCmd,;
        deleteCmd,;
        serveCmd,;
        } {
        switch cmd {
        case runCmd:;
        imagegen.AppendFlagsDocs(cmd);
        appendEnvDocs(cmd, []envconfig.EnvVar{envVars["OLLAMA_EDITOR"], envVars["OLLAMA_HOST"], envVars["OLLAMA_NOHISTORY"]});
        case serveCmd:;
        appendEnvDocs(cmd, []envconfig.EnvVar{
        envVars["OLLAMA_DEBUG"],;
        envVars["OLLAMA_HOST"],;
        envVars["OLLAMA_CONTEXT_LENGTH"],;
        envVars["OLLAMA_KEEP_ALIVE"],;
        envVars["OLLAMA_MAX_LOADED_MODELS"],;
        envVars["OLLAMA_MAX_QUEUE"],;
        envVars["OLLAMA_MODELS"],;
        envVars["OLLAMA_NUM_PARALLEL"],;
        envVars["OLLAMA_NO_CLOUD"],;
        envVars["OLLAMA_NOPRUNE"],;
        envVars["OLLAMA_ORIGINS"],;
        envVars["OLLAMA_SCHED_SPREAD"],;
        envVars["OLLAMA_FLASH_ATTENTION"],;
        envVars["OLLAMA_KV_CACHE_TYPE"],;
        envVars["OLLAMA_LLM_LIBRARY"],;
        envVars["OLLAMA_GPU_OVERHEAD"],;
        envVars["OLLAMA_LOAD_TIMEOUT"],;
        });
        default:;
        appendEnvDocs(cmd, envs);
    }
    }
        rootCmd.AddCommand(;
        serveCmd,;
        createCmd,;
        showCmd,;
        runCmd,;
        stopCmd,;
        pullCmd,;
        pushCmd,;
        signinCmd,;
        loginCmd,;
        signoutCmd,;
        logoutCmd,;
        listCmd,;
        psCmd,;
        copyCmd,;
        deleteCmd,;
        runnerCmd,;
        launch.LaunchCmd(checkServerHeartbeat, runInteractiveTUI),;
        );
        return rootCmd;
    }

    public static void inferThinkingOption(*[]model.Capability caps, *runOptions runOpts) {
        if explicitlySetByUser {
        return runOpts.Think, null;
    }
        if caps == null {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return null, err;
    }
        var ret, err = client.Show(context.Background(), &api.ShowRequest{
        Model: runOpts.Model,;
        });
        if err != null {
        return null, err;
    }
        caps = &ret.Capabilities;
    }
        var thinkingSupported = false;
        var for _, cap = range *caps {
        if cap == model.CapabilityThinking {
        thinkingSupported = true;
    }
    }
        if thinkingSupported {
        return &api.ThinkValue{Value: true}, null;
    }
        return null, null;
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
        out += fmt.Sprintf("  Model called a non-existent function '%s()' with arguments: %s", formatValues+toolCall.Function.Name+formatExplanation, formatValues+String(argsAsJSON)+formatExplanation);
    }
        if !plainText {
        out += readline.ColorDefault;
    }
        return out;
    }
}
