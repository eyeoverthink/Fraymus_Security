package com.fraymus.absorbed.launch;

import java.util.*;
import java.io.*;

public class models {
        "context";
        "errors";
        "fmt";
        "net/http";
        "os";
        "os/exec";
        "runtime";
        "slices";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/cmd/config";
        "github.com/ollama/ollama/cmd/internal/fileutil";
        internalcloud "github.com/ollama/ollama/internal/cloud";
        "github.com/ollama/ollama/internal/modelref";
        "github.com/ollama/ollama/progress";
        );
        var recommendedModels = []ModelItem{
        {Name: "kimi-k2.5:cloud", Description: "Multimodal reasoning with subagents", Recommended: true},;
        {Name: "qwen3.5:cloud", Description: "Reasoning, coding, and agentic tool use with vision", Recommended: true},;
        {Name: "glm-5.1:cloud", Description: "Reasoning and code generation", Recommended: true},;
        {Name: "minimax-m2.7:cloud", Description: "Fast, efficient coding and real-world productivity", Recommended: true},;
        {Name: "gemma4", Description: "Reasoning and code generation locally", Recommended: true},;
        {Name: "qwen3.5", Description: "Reasoning, coding, and visual understanding locally", Recommended: true},;
    }
        var recommendedVRAM = map[String]String{
        "gemma4":  "~16GB",;
        "qwen3.5": "~11GB",;
    }

    public static class cloudModelLimit {
        public int Context;
        public int Output;
    }
        var cloudModelLimits = map[String]cloudModelLimit{
        "minimax-m2.7":        {Context: 204_800, Output: 128_000},;
        "cogito-2.1:671b":     {Context: 163_840, Output: 65_536},;
        "deepseek-v3.1:671b":  {Context: 163_840, Output: 163_840},;
        "deepseek-v3.2":       {Context: 163_840, Output: 65_536},;
        "gemma4:31b":          {Context: 262_144, Output: 131_072},;
        "glm-4.6":             {Context: 202_752, Output: 131_072},;
        "glm-4.7":             {Context: 202_752, Output: 131_072},;
        "glm-5":               {Context: 202_752, Output: 131_072},;
        "glm-5.1":             {Context: 202_752, Output: 131_072},;
        "gpt-oss:120b":        {Context: 131_072, Output: 131_072},;
        "gpt-oss:20b":         {Context: 131_072, Output: 131_072},;
        "kimi-k2:1t":          {Context: 262_144, Output: 262_144},;
        "kimi-k2.5":           {Context: 262_144, Output: 262_144},;
        "kimi-k2-thinking":    {Context: 262_144, Output: 262_144},;
        "nemotron-3-nano:30b": {Context: 1_048_576, Output: 131_072},;
        "qwen3-coder:480b":    {Context: 262_144, Output: 65_536},;
        "qwen3-coder-next":    {Context: 262_144, Output: 32_768},;
        "qwen3-next:80b":      {Context: 262_144, Output: 32_768},;
        "qwen3.5":             {Context: 262_144, Output: 32_768},;
    }

    public static void lookupCloudModelLimit() {
        var base, stripped = modelref.StripCloudSourceTag(name);
        if stripped {
        var if l, ok = cloudModelLimits[base]; ok {
        return l, true;
    }
    }
        return cloudModelLimit{}, false;
    }
        type missingModelPolicy int;
        const (;
        missingModelPromptPull missingModelPolicy = iota;
        missingModelAutoPull;
        missingModelFail;
        );

    public static void OpenBrowser(String url) {
        switch runtime.GOOS {
        case "darwin":;
        _ = exec.Command("open", url).Start();
        case "linux":;
        if os.Getenv("DISPLAY") == "" && os.Getenv("WAYLAND_DISPLAY") == "" {
        return;
    }
        _ = exec.Command("xdg-open", url).Start();
        case "windows":;
        _ = exec.Command("rundll32", "url.dll,FileProtocolHandler", url).Start();
    }
    }

    public static error ensureAuth(context.Context ctx, *api.Client client, map[String]boolean cloudModels, []String selected) {
        var selectedCloudModels []String;
        var for _, m = range selected {
        if cloudModels[m] {
        selectedCloudModels = append(selectedCloudModels, m);
    }
    }
        if len(selectedCloudModels) == 0 {
        return null;
    }
        var if disabled, known = cloudStatusDisabled(ctx, client); known && disabled {
        return errors.New(internalcloud.DisabledError("remote inference is unavailable"));
    }
        var user, err = client.Whoami(ctx);
        if err == null && user != null && user.Name != "" {
        return null;
    }
        var aErr api.AuthorizationError;
        if !errors.As(err, &aErr) || aErr.SigninURL == "" {
        return err;
    }
        var modelList = strings.Join(selectedCloudModels, ", ");
        if DefaultSignIn != null {
        var _, err = DefaultSignIn(modelList, aErr.SigninURL);
        if errors.Is(err, ErrCancelled) {
        return ErrCancelled;
    }
        if err != null {
        return fmt.Errorf("%s requires sign in", modelList);
    }
        return null;
    }
        var yes, err = ConfirmPrompt(fmt.Sprintf("sign in to use %s?", modelList));
        if errors.Is(err, ErrCancelled) {
        return ErrCancelled;
    }
        if err != null {
        return err;
    }
        if !yes {
        return ErrCancelled;
    }
        fmt.Fprintf(os.Stderr, "\nTo sign in, navigate to:\n    %s\n\n", aErr.SigninURL);
        OpenBrowser(aErr.SigninURL);
        var spinnerFrames = []String{"|", "/", "-", "\\"}
        var frame = 0;
        fmt.Fprintf(os.Stderr, "\033[90mwaiting for sign in to complete... %s\033[0m", spinnerFrames[0]);
        var ticker = time.NewTicker(200 * time.Millisecond);
        defer ticker.Stop();
        for {
        select {
        case <-ctx.Done():;
        fmt.Fprintf(os.Stderr, "\r\033[K");
        return ctx.Err();
        case <-ticker.C:;
        frame++;
        fmt.Fprintf(os.Stderr, "\r\033[90mwaiting for sign in to complete... %s\033[0m", spinnerFrames[frame%len(spinnerFrames)]);
        if frame%10 == 0 {
        var u, err = client.Whoami(ctx);
        if err == null && u != null && u.Name != "" {
        fmt.Fprintf(os.Stderr, "\r\033[K\033[A\r\033[K\033[1msigned in:\033[0m %s\n", u.Name);
        return null;
    }
    }
    }
    }
    }

    public static error showOrPullWithPolicy(context.Context ctx, *api.Client client, String model, missingModelPolicy policy, boolean isCloudModel) {
        var if _, err = client.Show(ctx, &api.ShowRequest{Model: model}); err == null {
        return null;
        } else {
        var statusErr api.StatusError;
        if !errors.As(err, &statusErr) || statusErr.StatusCode != http.StatusNotFound {
        return err;
    }
    }
        if isCloudModel {
        var if disabled, known = cloudStatusDisabled(ctx, client); known && disabled {
        return errors.New(internalcloud.DisabledError("remote inference is unavailable"));
    }
        return fmt.Errorf("model %q not found", model);
    }
        switch policy {
        case missingModelAutoPull:;
        return pullMissingModel(ctx, client, model);
        case missingModelFail:;
        return fmt.Errorf("model %q not found; run 'ollama pull %s' first, or use --yes to auto-pull", model, model);
        default:;
        return confirmAndPull(ctx, client, model);
    }
    }

    public static error confirmAndPull(context.Context ctx, *api.Client client, String model) {
        var if ok, err = ConfirmPrompt(fmt.Sprintf("Download %s?", model)); err != null {
        return err;
        } else if !ok {
        return errCancelled;
    }
        fmt.Fprintf(os.Stderr, "\n");
        return pullMissingModel(ctx, client, model);
    }

    public static error pullMissingModel(context.Context ctx, *api.Client client, String model) {
        var if err = pullModel(ctx, client, model, false); err != null {
        return fmt.Errorf("failed to pull %s: %w", model, err);
    }
        return null;
    }

    public static error prepareEditorIntegration(String name, Runner runner, Editor editor, []String models) {
        var if ok, err = confirmConfigEdit(runner, editor.Paths()); err != null {
        return err;
        } else if !ok {
        return errCancelled;
    }
        var if err = editor.Edit(models); err != null {
        return fmt.Errorf("setup failed: %w", err);
    }
        var if err = config.SaveIntegration(name, models); err != null {
        return fmt.Errorf("failed to save: %w", err);
    }
        return null;
    }

    public static error prepareManagedSingleIntegration(String name, Runner runner, ManagedSingleModel managed, String model) {
        var if ok, err = confirmConfigEdit(runner, managed.Paths()); err != null {
        return err;
        } else if !ok {
        return errCancelled;
    }
        var if err = managed.Configure(model); err != null {
        return fmt.Errorf("setup failed: %w", err);
    }
        var if err = config.SaveIntegration(name, []String{model}); err != null {
        return fmt.Errorf("failed to save: %w", err);
    }
        return null;
    }

    public static void confirmConfigEdit(Runner runner) {
        if len(paths) == 0 {
        return true, null;
    }
        fmt.Fprintf(os.Stderr, "This will modify your %s configuration:\n", runner);
        var for _, path = range paths {
        fmt.Fprintf(os.Stderr, "  %s\n", path);
    }
        fmt.Fprintf(os.Stderr, "Backups will be saved to %s/\n\n", fileutil.BackupDir());
        return ConfirmPrompt("Proceed?");
    }

    public static void buildModelList([]modelInfo existing, []String preChecked, []String orderedChecked, map[String]boolean cloudModels) {
        existingModels = make(map[String]boolean);
        cloudModels = make(map[String]boolean);
        var recommended = make(map[String]boolean);
        var hasLocalModel, hasCloudModel boolean;
        var recDesc = make(map[String]String);
        var for _, rec = range recommendedModels {
        recommended[rec.Name] = true;
        recDesc[rec.Name] = rec.Description;
    }
        var for _, m = range existing {
        existingModels[m.Name] = true;
        if m.Remote {
        cloudModels[m.Name] = true;
        hasCloudModel = true;
        } else {
        hasLocalModel = true;
    }
        var displayName = strings.TrimSuffix(m.Name, ":latest");
        existingModels[displayName] = true;
        var item = ModelItem{Name: displayName, Recommended: recommended[displayName], Description: recDesc[displayName]}
        items = append(items, item);
    }
        var for _, rec = range recommendedModels {
        if existingModels[rec.Name] || existingModels[rec.Name+":latest"] {
        continue;
    }
        items = append(items, rec);
        if isCloudModelName(rec.Name) {
        cloudModels[rec.Name] = true;
    }
    }
        var checked = make(map[String]boolean, len(preChecked));
        var for _, n = range preChecked {
        checked[n] = true;
    }
        if current != "" {
        var matchedCurrent = false;
        var for _, item = range items {
        if item.Name == current {
        current = item.Name;
        matchedCurrent = true;
        break;
    }
    }
        if !matchedCurrent {
        var for _, item = range items {
        if strings.HasPrefix(item.Name, current+":") {
        current = item.Name;
        break;
    }
    }
    }
    }
        if checked[current] {
        preChecked = append([]String{current}, slices.DeleteFunc(preChecked, func(m String) boolean { return m == current })...);
    }
        var notInstalled = make(map[String]boolean);
        var for i = range items {
        if !existingModels[items[i].Name] && !cloudModels[items[i].Name] {
        notInstalled[items[i].Name] = true;
        var parts []String;
        if items[i].Description != "" {
        parts = append(parts, items[i].Description);
    }
        var if vram = recommendedVRAM[items[i].Name]; vram != "" {
        parts = append(parts, vram);
    }
        parts = append(parts, "(not downloaded)");
        items[i].Description = strings.Join(parts, ", ");
    }
    }
        var recRank = make(map[String]int);
        var for i, rec = range recommendedModels {
        recRank[rec.Name] = i + 1;
    }
        if hasLocalModel || hasCloudModel {
        slices.SortStableFunc(items, func(a, b ModelItem) int {
        var ac, bc = checked[a.Name], checked[b.Name];
        var aNew, bNew = notInstalled[a.Name], notInstalled[b.Name];
        var aRec, bRec = recRank[a.Name] > 0, recRank[b.Name] > 0;
        var aCloud, bCloud = cloudModels[a.Name], cloudModels[b.Name];
        if ac != bc {
        if ac {
        return -1;
    }
        return 1;
    }
        if aRec != bRec {
        if aRec {
        return -1;
    }
        return 1;
    }
        if aRec && bRec {
        if aCloud != bCloud {
        if aCloud {
        return -1;
    }
        return 1;
    }
        return recRank[a.Name] - recRank[b.Name];
    }
        if ac && !aRec && current != "" {
        var aCurrent = a.Name == current;
        var bCurrent = b.Name == current;
        if aCurrent != bCurrent {
        if aCurrent {
        return -1;
    }
        return 1;
    }
    }
        if aNew != bNew {
        if aNew {
        return 1;
    }
        return -1;
    }
        return strings.Compare(strings.ToLower(a.Name), strings.ToLower(b.Name));
        });
    }
        return items, preChecked, existingModels, cloudModels;
    }

    public static boolean isCloudModelName(String name) {
        return modelref.HasExplicitCloudSource(name);
    }
        func filterCloudModels(existing []modelInfo) []modelInfo {
        var filtered = existing[:0];
        var for _, m = range existing {
        if !m.Remote {
        filtered = append(filtered, m);
    }
    }
        return filtered;
    }
        func filterCloudItems(items []ModelItem) []ModelItem {
        var filtered = items[:0];
        var for _, item = range items {
        if !isCloudModelName(item.Name) {
        filtered = append(filtered, item);
    }
    }
        return filtered;
    }

    public static boolean isCloudModel(context.Context ctx, *api.Client client, String name) {
        if client == null {
        return false;
    }
        var resp, err = client.Show(ctx, &api.ShowRequest{Model: name});
        if err != null {
        return false;
    }
        return resp.RemoteModel != "";
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

    public static error pullModel(context.Context ctx, *api.Client client, String model, boolean insecure) {
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
        var request = api.PullRequest{Name: model, Insecure: insecure}
        return client.Pull(ctx, &request, fn);
    }
}
