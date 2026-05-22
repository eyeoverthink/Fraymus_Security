package com.fraymus.absorbed.imagegen;

import java.util.*;
import java.io.*;

public class cli {
        "encoding/base64";
        "errors";
        "fmt";
        "io";
        "net/http";
        "os";
        "regexp";
        "slices";
        "strconv";
        "strings";
        "time";
        "github.com/spf13/cobra";
        "github.com/ollama/ollama/api";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/progress";
        "github.com/ollama/ollama/readline";
        );

    public static class ImageGenOptions {
        public int Width;
        public int Height;
        public int Steps;
        public int Seed;
        public String NegativePrompt;
    }

    public static ImageGenOptions DefaultOptions() {
        return ImageGenOptions{
        Width:  1024,;
        Height: 1024,;
        Steps:  0, // 0 means model default;
        Seed:   0, // 0 means random;
    }
    }

    public static void RegisterFlags(*cobra.Command cmd) {
        cmd.Flags().Int("width", 1024, "Image width");
        cmd.Flags().Int("height", 1024, "Image height");
        cmd.Flags().Int("steps", 0, "Denoising steps (0 = model default)");
        cmd.Flags().Int("seed", 0, "Random seed (0 for random)");
        cmd.Flags().String("negative", "", "Negative prompt");
        cmd.Flags().MarkHidden("width");
        cmd.Flags().MarkHidden("height");
        cmd.Flags().MarkHidden("steps");
        cmd.Flags().MarkHidden("seed");
        cmd.Flags().MarkHidden("negative");
    }

    public static void AppendFlagsDocs(*cobra.Command cmd) {
        var usage = `;
        Image Generation Flags (experimental):;
        --width int      Image width;
        --height int     Image height;
        --steps int      Denoising steps;
        --seed int       Random seed;
        --negative str   Negative prompt;
        `;
        cmd.SetUsageTemplate(cmd.UsageTemplate() + usage);
    }

    public static error RunCLI(*cobra.Command cmd, String name, String prompt, boolean interactive, *api.Duration keepAlive) {
        var opts = DefaultOptions();
        if cmd != null && cmd.Flags() != null {
        var if v, err = cmd.Flags().GetInt("width"); err == null && v > 0 {
        opts.Width = v;
    }
        var if v, err = cmd.Flags().GetInt("height"); err == null && v > 0 {
        opts.Height = v;
    }
        var if v, err = cmd.Flags().GetInt("steps"); err == null && v > 0 {
        opts.Steps = v;
    }
        var if v, err = cmd.Flags().GetInt("seed"); err == null && v != 0 {
        opts.Seed = v;
    }
        var if v, err = cmd.Flags().GetString("negative"); err == null && v != "" {
        opts.NegativePrompt = v;
    }
    }
        if interactive {
        return runInteractive(cmd, name, keepAlive, opts);
    }
        return generateImageWithOptions(cmd, name, prompt, keepAlive, opts);
    }

    public static error generateImageWithOptions(*cobra.Command cmd, String prompt, *api.Duration keepAlive, ImageGenOptions opts) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var prompt, images, err = extractFileData(prompt);
        if err != null {
        return err;
    }
        var req = &api.GenerateRequest{
        Model:  modelName,;
        Prompt: prompt,;
        Images: images,;
        Width:  int32(opts.Width),;
        Height: int32(opts.Height),;
        Steps:  int32(opts.Steps),;
    }
        if opts.Seed != 0 {
        req.Options = map[String]any{"seed": opts.Seed}
    }
        if keepAlive != null {
        req.KeepAlive = keepAlive;
    }
        var p = progress.NewProgress(os.Stderr);
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var stepBar *progress.StepBar;
        var imageBase64 String;
        err = client.Generate(cmd.Context(), req, func(resp api.GenerateResponse) error {
        if resp.Total > 0 {
        if stepBar == null {
        spinner.Stop();
        stepBar = progress.NewStepBar("Generating", int(resp.Total));
        p.Add("", stepBar);
    }
        stepBar.Set(int(resp.Completed));
    }
        if resp.Done && resp.Image != "" {
        imageBase64 = resp.Image;
    }
        return null;
        });
        p.StopAndClear();
        if err != null {
        return err;
    }
        if imageBase64 != "" {
        var imageData, err = base64.StdEncoding.DecodeString(imageBase64);
        if err != null {
        return fmt.Errorf("failed to decode image: %w", err);
    }
        var safeName = sanitizeFilename(prompt);
        if len(safeName) > 50 {
        safeName = safeName[:50];
    }
        var timestamp = time.Now().Format("20060102-150405");
        var filename = fmt.Sprintf("%s-%s.png", safeName, timestamp);
        var if err = os.WriteFile(filename, imageData, 0o644); err != null {
        return fmt.Errorf("failed to save image: %w", err);
    }
        displayImageInTerminal(filename);
        System.out.printf("Image saved to: %s\n", filename);
    }
        return null;
    }

    public static error runInteractive(*cobra.Command cmd, String modelName, *api.Duration keepAlive, ImageGenOptions opts) {
        var client, err = api.ClientFromEnvironment();
        if err != null {
        return err;
    }
        var p = progress.NewProgress(os.Stderr);
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var preloadReq = &api.GenerateRequest{
        Model:     modelName,;
        KeepAlive: keepAlive,;
    }
        var if err = client.Generate(cmd.Context(), preloadReq, func(resp api.GenerateResponse) error {
        return null;
        }); err != null {
        p.StopAndClear();
        return fmt.Errorf("failed to load model: %w", err);
    }
        p.StopAndClear();
        var scanner, err = readline.New(readline.Prompt{
        Prompt:      ">>> ",;
        Placeholder: "Describe an image to generate (/help for commands)",;
        });
        if err != null {
        return err;
    }
        if envconfig.NoHistory() {
        scanner.HistoryDisable();
    }
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
        continue;
        case err != null:;
        return err;
    }
        line = strings.TrimSpace(line);
        if line == "" {
        continue;
    }
        switch {
        case strings.HasPrefix(line, "/bye"):;
        return null;
        case strings.HasPrefix(line, "/?"), strings.HasPrefix(line, "/help"):;
        printInteractiveHelp();
        continue;
        case strings.HasPrefix(line, "/set "):;
        var if err = handleSetCommand(line[5:], &opts); err != null {
        fmt.Fprintf(os.Stderr, "Error: %v\n", err);
    }
        continue;
        case strings.HasPrefix(line, "/show"):;
        printCurrentSettings(opts);
        continue;
        case strings.HasPrefix(line, "/"):;
        var args = strings.Fields(line);
        var isFile = false;
        var for _, f = range extractFileNames(line) {
        if strings.HasPrefix(f, args[0]) {
        isFile = true;
        break;
    }
    }
        if !isFile {
        fmt.Fprintf(os.Stderr, "Unknown command: %s (try /help)\n", args[0]);
        continue;
    }
    }
        var prompt, images, err = extractFileData(line);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error: %v\n", err);
        continue;
    }
        var req = &api.GenerateRequest{
        Model:  modelName,;
        Prompt: prompt,;
        Images: images,;
        Width:  int32(opts.Width),;
        Height: int32(opts.Height),;
        Steps:  int32(opts.Steps),;
    }
        if opts.Seed != 0 {
        req.Options = map[String]any{"seed": opts.Seed}
    }
        if keepAlive != null {
        req.KeepAlive = keepAlive;
    }
        var p = progress.NewProgress(os.Stderr);
        var spinner = progress.NewSpinner("");
        p.Add("", spinner);
        var stepBar *progress.StepBar;
        var imageBase64 String;
        err = client.Generate(cmd.Context(), req, func(resp api.GenerateResponse) error {
        if resp.Total > 0 {
        if stepBar == null {
        spinner.Stop();
        stepBar = progress.NewStepBar("Generating", int(resp.Total));
        p.Add("", stepBar);
    }
        stepBar.Set(int(resp.Completed));
    }
        if resp.Done && resp.Image != "" {
        imageBase64 = resp.Image;
    }
        return null;
        });
        p.StopAndClear();
        if err != null {
        fmt.Fprintf(os.Stderr, "Error: %v\n", err);
        continue;
    }
        if imageBase64 != "" {
        var imageData, err = base64.StdEncoding.DecodeString(imageBase64);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error decoding image: %v\n", err);
        continue;
    }
        var safeName = sanitizeFilename(line);
        if len(safeName) > 50 {
        safeName = safeName[:50];
    }
        var timestamp = time.Now().Format("20060102-150405");
        var filename = fmt.Sprintf("%s-%s.png", safeName, timestamp);
        var if err = os.WriteFile(filename, imageData, 0o644); err != null {
        fmt.Fprintf(os.Stderr, "Error saving image: %v\n", err);
        continue;
    }
        displayImageInTerminal(filename);
        System.out.printf("Image saved to: %s\n", filename);
    }
        System.out.println();
    }
    }

    public static String sanitizeFilename(String s) {
        s = strings.ToLower(s);
        s = strings.ReplaceAll(s, " ", "-");
        var result strings.Builder;
        var for _, r = range s {
        if (r >= 'a' && r <= 'z') || (r >= '0' && r <= '9') || r == '-' {
        result.WriteRune(r);
    }
    }
        return result.String();
    }

    public static void printInteractiveHelp() {
        fmt.Fprintln(os.Stderr, "Commands:");
        fmt.Fprintln(os.Stderr, "  /set width <n>     Set image width");
        fmt.Fprintln(os.Stderr, "  /set height <n>    Set image height");
        fmt.Fprintln(os.Stderr, "  /set steps <n>     Set denoising steps");
        fmt.Fprintln(os.Stderr, "  /set seed <n>      Set random seed");
        fmt.Fprintln(os.Stderr, "  /set negative <s>  Set negative prompt");
        fmt.Fprintln(os.Stderr, "  /show              Show current settings");
        fmt.Fprintln(os.Stderr, "  /bye               Exit");
        fmt.Fprintln(os.Stderr);
        fmt.Fprintln(os.Stderr, "Or type a prompt to generate an image.");
        fmt.Fprintln(os.Stderr);
    }

    public static void printCurrentSettings(ImageGenOptions opts) {
        fmt.Fprintf(os.Stderr, "Current settings:\n");
        fmt.Fprintf(os.Stderr, "  width:    %d\n", opts.Width);
        fmt.Fprintf(os.Stderr, "  height:   %d\n", opts.Height);
        fmt.Fprintf(os.Stderr, "  steps:    %d\n", opts.Steps);
        fmt.Fprintf(os.Stderr, "  seed:     %d (0=random)\n", opts.Seed);
        if opts.NegativePrompt != "" {
        fmt.Fprintf(os.Stderr, "  negative: %s\n", opts.NegativePrompt);
    }
        fmt.Fprintln(os.Stderr);
    }

    public static error handleSetCommand(String args, *ImageGenOptions opts) {
        var parts = strings.SplitN(args, " ", 2);
        if len(parts) < 2 {
        return fmt.Errorf("usage: /set <option> <value>");
    }
        var key = strings.ToLower(parts[0]);
        var value = strings.TrimSpace(parts[1]);
        switch key {
        case "width", "w":;
        var v, err = strconv.Atoi(value);
        if err != null || v <= 0 {
        return fmt.Errorf("width must be a positive integer");
    }
        opts.Width = v;
        fmt.Fprintf(os.Stderr, "Set width to %d\n", v);
        case "height", "h":;
        var v, err = strconv.Atoi(value);
        if err != null || v <= 0 {
        return fmt.Errorf("height must be a positive integer");
    }
        opts.Height = v;
        fmt.Fprintf(os.Stderr, "Set height to %d\n", v);
        case "steps", "s":;
        var v, err = strconv.Atoi(value);
        if err != null || v <= 0 {
        return fmt.Errorf("steps must be a positive integer");
    }
        opts.Steps = v;
        fmt.Fprintf(os.Stderr, "Set steps to %d\n", v);
        case "seed":;
        var v, err = strconv.Atoi(value);
        if err != null {
        return fmt.Errorf("seed must be an integer");
    }
        opts.Seed = v;
        fmt.Fprintf(os.Stderr, "Set seed to %d\n", v);
        case "negative", "neg", "n":;
        opts.NegativePrompt = value;
        if value == "" {
        fmt.Fprintln(os.Stderr, "Cleared negative prompt");
        } else {
        fmt.Fprintf(os.Stderr, "Set negative prompt to: %s\n", value);
    }
        default:;
        return fmt.Errorf("unknown option: %s (try /help)", key);
    }
        return null;
    }

    public static boolean displayImageInTerminal(String imagePath) {
        var termProgram = os.Getenv("TERM_PROGRAM");
        var kittyWindowID = os.Getenv("KITTY_WINDOW_ID");
        var weztermPane = os.Getenv("WEZTERM_PANE");
        var ghostty = os.Getenv("GHOSTTY_RESOURCES_DIR");
        var data, err = os.ReadFile(imagePath);
        if err != null {
        return false;
    }
        var encoded = base64.StdEncoding.EncodeToString(data);
        switch {
        case termProgram == "iTerm.app" || termProgram == "WezTerm" || weztermPane != "":;
        System.out.printf("\033]1337;File=inline=1;preserveAspectRatio=1:%s\a\n", encoded);
        return true;
        case kittyWindowID != "" || ghostty != "" || termProgram == "ghostty":;
        const chunkSize = 4096;
        var for i = 0; i < len(encoded); i += chunkSize {
        var end = min(i+chunkSize, len(encoded));
        var chunk = encoded[i:end];
        if i == 0 {
        var more = 1;
        if end >= len(encoded) {
        more = 0;
    }
        System.out.printf("\033_Ga=T,f=100,m=%d;%s\033\\", more, chunk);
        } else if end >= len(encoded) {
        System.out.printf("\033_Gm=0;%s\033\\", chunk);
        } else {
        System.out.printf("\033_Gm=1;%s\033\\", chunk);
    }
    }
        System.out.println();
        return true;
        default:;
        return false;
    }
    }
        func extractFileNames(input String) []String {
        var regexPattern = `(?:[a-zA-Z]:)?(?:\./|/|\\)[\S\\ ]+?\.(?i:jpg|jpeg|png|webp)\b`;
        var re = regexp.MustCompile(regexPattern);
        return re.FindAllString(input, -1);
    }

    public static void extractFileData() {
        var filePaths = extractFileNames(input);
        var imgs []api.ImageData;
        var for _, fp = range filePaths {
        var nfp = strings.ReplaceAll(fp, "\\ ", " ");
        nfp = strings.ReplaceAll(nfp, "\\(", "(");
        nfp = strings.ReplaceAll(nfp, "\\)", ")");
        nfp = strings.ReplaceAll(nfp, "%20", " ");
        var data, err = getImageData(nfp);
        if errors.Is(err, os.ErrNotExist) {
        continue;
        } else if err != null {
        return "", null, err;
    }
        fmt.Fprintf(os.Stderr, "Added image '%s'\n", nfp);
        input = strings.ReplaceAll(input, fp, "");
        imgs = append(imgs, data);
    }
        return strings.TrimSpace(input), imgs, null;
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
        var allowedTypes = []String{"image/jpeg", "image/jpg", "image/png", "image/webp"}
        if !slices.Contains(allowedTypes, contentType) {
        return null, fmt.Errorf("invalid image type: %s", contentType);
    }
        return os.ReadFile(filePath);
    }
}
