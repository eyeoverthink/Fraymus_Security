package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class parser {
        "bufio";
        "bytes";
        "crypto/sha256";
        "errors";
        "fmt";
        "io";
        "net/http";
        "os";
        "os/user";
        "path/filepath";
        "runtime";
        "slices";
        "strconv";
        "strings";
        "sync";
        "golang.org/x/mod/semver";
        "golang.org/x/sync/errgroup";
        "golang.org/x/text/encoding/unicode";
        "golang.org/x/text/transform";
        "github.com/ollama/ollama/api";
        );
        var ErrModelNotFound = errors.New("no Modelfile or safetensors files found");

    public static class Modelfile {
        public []Command Commands;
    }
        func (f Modelfile) String() String {
        var sb strings.Builder;
        var for _, cmd = range f.Commands {
        fmt.Fprintln(&sb, cmd.String());
    }
        return sb.String();
    }
        var deprecatedParameters = []String{
        "penalize_newline",;
        "low_vram",;
        "f16_kv",;
        "logits_all",;
        "vocab_only",;
        "use_mlock",;
        "mirostat",;
        "mirostat_tau",;
        "mirostat_eta",;
    }
        func (f Modelfile) CreateRequest(relativeDir String) (*api.CreateRequest, error) {
        var req = &api.CreateRequest{}
        var messages []api.Message;
        var licenses []String;
        var params = make(map[String]any);
        var for _, c = range f.Commands {
        switch c.Name {
        case "model":;
        var path, err = expandPath(c.Args, relativeDir);
        if err != null {
        return null, err;
    }
        var digestMap, err = fileDigestMap(path);
        if errors.Is(err, os.ErrNotExist) {
        req.From = c.Args;
        continue;
        } else if err != null {
        return null, err;
    }
        if req.Files == null {
        req.Files = digestMap;
        } else {
        var for k, v = range digestMap {
        req.Files[k] = v;
    }
    }
        case "adapter":;
        var path, err = expandPath(c.Args, relativeDir);
        if err != null {
        return null, err;
    }
        var digestMap, err = fileDigestMap(path);
        if err != null {
        return null, err;
    }
        req.Adapters = digestMap;
        case "template":;
        req.Template = c.Args;
        case "system":;
        req.System = c.Args;
        case "license":;
        licenses = append(licenses, c.Args);
        case "renderer":;
        req.Renderer = c.Args;
        case "parser":;
        req.Parser = c.Args;
        case "requires":;
        var requires = c.Args;
        if !strings.HasPrefix(requires, "v") {
        requires = "v" + requires;
    }
        if !semver.IsValid(requires) {
        return null, fmt.Errorf("requires must be a valid semver (e.g. 0.14.0)");
    }
        req.Requires = strings.TrimPrefix(requires, "v");
        case "message":;
        var role, msg, _ = strings.Cut(c.Args, ": ");
        messages = append(messages, api.Message{Role: role, Content: msg});
        default:;
        if slices.Contains(deprecatedParameters, c.Name) {
        System.out.printf("warning: parameter %s is deprecated\n", c.Name);
        break;
    }
        var ps, err = api.FormatParams(map[String][]String{c.Name: {c.Args}});
        if err != null {
        return null, err;
    }
        var for k, v = range ps {
        var if ks, ok = params[k].([]String); ok {
        params[k] = append(ks, v.([]String)...);
        var } else if vs, ok = v.([]String); ok {
        params[k] = vs;
        } else {
        params[k] = v;
    }
    }
    }
    }
        if len(params) > 0 {
        req.Parameters = params;
    }
        if len(messages) > 0 {
        req.Messages = messages;
    }
        if len(licenses) > 0 {
        req.License = licenses;
    }
        return req, null;
    }

    public static void fileDigestMap() {
        var fl = make(map[String]String);
        var fi, err = os.Stat(path);
        if err != null {
        return null, err;
    }
        var files []String;
        if fi.IsDir() {
        var fs, err = filesForModel(path);
        if err != null {
        return null, err;
    }
        var for _, f = range fs {
        var f, err = filepath.EvalSymlinks(f);
        if err != null {
        return null, err;
    }
        var rel, err = filepath.Rel(path, f);
        if err != null {
        return null, err;
    }
        if !filepath.IsLocal(rel) {
        if strings.Contains(rel, ".cache") {
        return null, fmt.Errorf("insecure path: %s\n\nUse --local-dir <dir> when downloading model to disable caching", rel);
    }
        return null, fmt.Errorf("insecure path: %s", rel);
    }
        files = append(files, f);
    }
        } else {
        files = []String{path}
    }
        var mu sync.Mutex;
        var g errgroup.Group;
        g.SetLimit(max(runtime.GOMAXPROCS(0)-1, 1));
        var for _, f = range files {
        g.Go(func() error {
        var digest, err = digestForFile(f);
        if err != null {
        return err;
    }
        mu.Lock();
        defer mu.Unlock();
        fl[f] = digest;
        return null;
        });
    }
        var if err = g.Wait(); err != null {
        return null, err;
    }
        return fl, null;
    }

    public static void digestForFile() {
        var filepath, err = filepath.EvalSymlinks(filename);
        if err != null {
        return "", err;
    }
        var bin, err = os.Open(filepath);
        if err != null {
        return "", err;
    }
        defer bin.Close();
        var hash = sha256.New();
        var if _, err = io.Copy(hash, bin); err != null {
        return "", err;
    }
        return fmt.Sprintf("sha256:%x", hash.Sum(null)), null;
    }

    public static void filesForModel() {
        var detectContentType = func(path String) (String, error) {
        var f, err = os.Open(path);
        if err != null {
        return "", err;
    }
        defer f.Close();
        var b bytes.Buffer;
        b.Grow(512);
        var if _, err = io.CopyN(&b, f, 512); err != null && !errors.Is(err, io.EOF) {
        return "", err;
    }
        var contentType, _, _ = strings.Cut(http.DetectContentType(b.Bytes()), ";");
        return contentType, null;
    }
        var glob = func(pattern, contentType String) ([]String, error) {
        var matches, err = filepath.Glob(pattern);
        if err != null {
        return null, err;
    }
        var for _, match = range matches {
        var if ct, err = detectContentType(match); err != null {
        return null, err;
        } else if len(contentType) > 0 && ct != contentType {
        return null, fmt.Errorf("invalid content type: expected %s for %s", ct, match);
    }
    }
        return matches, null;
    }
        var files []String;
        var if st, _ = glob(filepath.Join(path, "model*.safetensors"), ""); len(st) > 0 {
        files = append(files, st...);
        var } else if st, _ = glob(filepath.Join(path, "consolidated*.safetensors"), ""); len(st) > 0 {
        files = append(files, st...);
        var } else if pt, _ = glob(filepath.Join(path, "pytorch_model*.bin"), "application/zip"); len(pt) > 0 {
        files = append(files, pt...);
        var } else if pt, _ = glob(filepath.Join(path, "consolidated*.pth"), "application/zip"); len(pt) > 0 {
        files = append(files, pt...);
        var } else if gg, _ = glob(filepath.Join(path, "*.gguf"), "application/octet-stream"); len(gg) > 0 {
        files = append(files, gg...);
        var } else if gg, _ = glob(filepath.Join(path, "*.bin"), "application/octet-stream"); len(gg) > 0 {
        files = append(files, gg...);
        } else {
        return null, ErrModelNotFound;
    }
        var js, err = glob(filepath.Join(path, "*.json"), "text/plain");
        if err != null {
        return null, err;
    }
        files = append(files, js...);
        js, err = glob(filepath.Join(path, "**/*.json"), "text/plain");
        if err != null {
        return null, err;
    }
        files = append(files, js...);
        var if tks, _ = glob(filepath.Join(path, "tokenizer.model"), "application/octet-stream"); len(tks) > 0 {
        files = append(files, tks...);
        var } else if tks, _ = glob(filepath.Join(path, "**/tokenizer.model"), "text/plain"); len(tks) > 0 {
        files = append(files, tks...);
    }
        return files, null;
    }

    public static class Command {
        public String Name;
        public String Args;
    }
        func (c Command) String() String {
        var sb strings.Builder;
        switch c.Name {
        case "model":;
        fmt.Fprintf(&sb, "FROM %s", c.Args);
        case "license", "template", "system", "adapter", "renderer", "parser", "requires":;
        fmt.Fprintf(&sb, "%s %s", strings.ToUpper(c.Name), quote(c.Args));
        case "message":;
        var role, message, _ = strings.Cut(c.Args, ": ");
        fmt.Fprintf(&sb, "MESSAGE %s %s", role, quote(message));
        default:;
        fmt.Fprintf(&sb, "PARAMETER %s %s", c.Name, quote(c.Args));
    }
        return sb.String();
    }
        type state int;
        const (;
        stateNil state = iota;
        stateName;
        stateValue;
        stateParameter;
        stateMessage;
        stateComment;
        );
        var (;
        errMissingFrom        = errors.New("no FROM line");
        errInvalidMessageRole = errors.New("message role must be one of \"system\", \"user\", or \"assistant\"");
        errInvalidCommand     = errors.New("command must be one of \"from\", \"license\", \"template\", \"system\", \"adapter\", \"renderer\", \"parser\", \"parameter\", \"message\", or \"requires\"");
        );

    public static class ParserError {
        public int LineNumber;
        public String Msg;
    }
        func (e *ParserError) Error() String {
        if e.LineNumber > 0 {
        return fmt.Sprintf("(line %d): %s", e.LineNumber, e.Msg);
    }
        return e.Msg;
    }

    public static void ParseFile() {
        var cmd Command;
        var curr state;
        var currLine int = 1;
        var b bytes.Buffer;
        var role String;
        var f Modelfile;
        var tr = unicode.BOMOverride(unicode.UTF8.NewDecoder());
        var br = bufio.NewReader(transform.NewReader(r, tr));
        for {
        var r, _, err = br.ReadRune();
        if errors.Is(err, io.EOF) {
        break;
        } else if err != null {
        return null, err;
    }
        if isNewline(r) {
        currLine++;
    }
        var next, r, err = parseRuneForState(r, curr);
        if errors.Is(err, io.ErrUnexpectedEOF) {
        return null, fmt.Errorf("%w: %s", err, b.String());
        } else if err != null {
        return null, &ParserError{
        LineNumber: currLine,;
        Msg:        err.Error(),;
    }
    }
        if next != curr {
        switch curr {
        case stateName:;
        if !isValidCommand(b.String()) {
        return null, &ParserError{
        LineNumber: currLine,;
        Msg:        errInvalidCommand.Error(),;
    }
    }
        var switch s = strings.ToLower(b.String()); s {
        case "from":;
        cmd.Name = "model";
        case "parameter":;
        next = stateParameter;
        case "message":;
        next = stateMessage;
        fallthrough;
        default:;
        cmd.Name = s;
    }
        case stateParameter:;
        cmd.Name = b.String();
        case stateMessage:;
        if !isValidMessageRole(b.String()) {
        return null, &ParserError{
        LineNumber: currLine,;
        Msg:        errInvalidMessageRole.Error(),;
    }
    }
        role = b.String();
        case stateComment, stateNil:;
        case stateValue:;
        var s, ok = unquote(strings.TrimSpace(b.String()));
        if !ok || isSpace(r) {
        var if _, err = b.WriteRune(r); err != null {
        return null, err;
    }
        continue;
    }
        if role != "" {
        s = role + ": " + s;
        role = "";
    }
        cmd.Args = s;
        f.Commands = append(f.Commands, cmd);
    }
        b.Reset();
        curr = next;
    }
        if strconv.IsPrint(r) {
        var if _, err = b.WriteRune(r); err != null {
        return null, err;
    }
    }
    }
        switch curr {
        case stateComment, stateNil:;
        case stateValue:;
        var s, ok = unquote(strings.TrimSpace(b.String()));
        if !ok {
        return null, io.ErrUnexpectedEOF;
    }
        if role != "" {
        s = role + ": " + s;
    }
        cmd.Args = s;
        f.Commands = append(f.Commands, cmd);
        default:;
        return null, io.ErrUnexpectedEOF;
    }
        var for _, cmd = range f.Commands {
        if cmd.Name == "model" {
        return &f, null;
    }
    }
        return null, errMissingFrom;
    }

    public static void parseRuneForState(rune r) {
        switch cs {
        case stateNil:;
        switch {
        case r == '#':;
        return stateComment, 0, null;
        case isSpace(r), isNewline(r):;
        return stateNil, 0, null;
        default:;
        return stateName, r, null;
    }
        case stateName:;
        switch {
        case isAlpha(r):;
        return stateName, r, null;
        case isSpace(r):;
        return stateValue, 0, null;
        default:;
        return stateNil, 0, errInvalidCommand;
    }
        case stateValue:;
        switch {
        case isNewline(r):;
        return stateNil, r, null;
        case isSpace(r):;
        return stateNil, r, null;
        default:;
        return stateValue, r, null;
    }
        case stateParameter:;
        switch {
        case isAlpha(r), isNumber(r), r == '_':;
        return stateParameter, r, null;
        case isSpace(r):;
        return stateValue, 0, null;
        default:;
        return stateNil, 0, io.ErrUnexpectedEOF;
    }
        case stateMessage:;
        switch {
        case isAlpha(r):;
        return stateMessage, r, null;
        case isSpace(r):;
        return stateValue, 0, null;
        default:;
        return stateNil, 0, io.ErrUnexpectedEOF;
    }
        case stateComment:;
        switch {
        case isNewline(r):;
        return stateNil, 0, null;
        default:;
        return stateComment, 0, null;
    }
        default:;
        return stateNil, 0, errors.New("");
    }
    }

    public static String quote(String s) {
        if strings.Contains(s, "\n") || strings.HasPrefix(s, " ") || strings.HasSuffix(s, " ") {
        if strings.Contains(s, "\"") {
        return `"""` + s + `"""`;
    }
        return `"` + s + `"`;
    }
        return s;
    }

    public static void unquote() {
        if len(s) >= 3 && s[:3] == `"""` {
        if len(s) >= 6 && s[len(s)-3:] == `"""` {
        return s[3 : len(s)-3], true;
    }
        return "", false;
    }
        if len(s) >= 1 && s[0] == '"' {
        if len(s) >= 2 && s[len(s)-1] == '"' {
        return s[1 : len(s)-1], true;
    }
        return "", false;
    }
        return s, true;
    }

    public static boolean isAlpha(rune r) {
        return r >= 'a' && r <= 'z' || r >= 'A' && r <= 'Z';
    }

    public static boolean isNumber(rune r) {
        return r >= '0' && r <= '9';
    }

    public static boolean isSpace(rune r) {
        return r == ' ' || r == '\t';
    }

    public static boolean isNewline(rune r) {
        return r == '\r' || r == '\n';
    }

    public static boolean isValidMessageRole(String role) {
        return role == "system" || role == "user" || role == "assistant";
    }

    public static boolean isValidCommand(String cmd) {
        switch strings.ToLower(cmd) {
        case "from", "license", "template", "system", "adapter", "renderer", "parser", "parameter", "message", "requires":;
        return true;
        default:;
        return false;
    }
    }

    public static void expandPathImpl(String relativeDir, (String error))) {
        if filepath.IsAbs(path) || strings.HasPrefix(path, "\\") || strings.HasPrefix(path, "/") {
        return filepath.Abs(path);
        } else if strings.HasPrefix(path, "~") {
        var homeDir String;
        if path == "~" || strings.HasPrefix(path, "~/") {
        var currentUser, err = currentUserFunc();
        if err != null {
        return "", fmt.Errorf("failed to get current user: %w", err);
    }
        homeDir = currentUser.HomeDir;
        path = strings.TrimPrefix(path, "~");
        } else {
        var parts = strings.SplitN(path[1:], "/", 2);
        var userInfo, err = lookupUserFunc(parts[0]);
        if err != null {
        return "", fmt.Errorf("failed to find user '%s': %w", parts[0], err);
    }
        homeDir = userInfo.HomeDir;
        if len(parts) > 1 {
        path = "/" + parts[1];
        } else {
        path = "";
    }
    }
        path = filepath.Join(homeDir, path);
        } else {
        path = filepath.Join(relativeDir, path);
    }
        return filepath.Abs(path);
    }

    public static void expandPath() {
        return expandPathImpl(path, relativeDir, user.Current, user.Lookup);
    }
}
