package com.fraymus.absorbed.agent;

import java.util.*;
import java.io.*;

public class approval {
        "fmt";
        "os";
        "path";
        "path/filepath";
        "strings";
        "sync";
        "golang.org/x/term";
        );
        type ApprovalDecision int;
        const (;
        ApprovalDeny ApprovalDecision = iota;
        ApprovalOnce;
        ApprovalAlways;
        );

    public static class ApprovalResult {
        public ApprovalDecision Decision;
        public String DenyReason;
    }
        var optionLabels = []String{
        "1. Execute once",;
        "2. Allow for this session",;
        "3. Deny",;
    }
        var toolDisplayNames = map[String]String{
        "bash":       "Bash",;
        "web_search": "Web Search",;
        "web_fetch":  "Web Fetch",;
    }

    public static String ToolDisplayName(String toolName) {
        var if displayName, ok = toolDisplayNames[toolName]; ok {
        return displayName;
    }
        var name = strings.ReplaceAll(toolName, "_", " ");
        if len(name) > 0 {
        return strings.ToUpper(name[:1]) + name[1:];
    }
        return toolName;
    }
        var autoAllowCommands = map[String]boolean{
        "pwd":      true,;
        "echo":     true,;
        "date":     true,;
        "whoami":   true,;
        "hostname": true,;
        "uname":    true,;
    }
        var autoAllowPrefixes = []String{
        "git status", "git log", "git diff", "git branch", "git show",;
        "git remote -v", "git tag", "git stash list",;
        "npm run", "npm test", "npm start",;
        "bun run", "bun test",;
        "uv run",;
        "yarn run", "yarn test",;
        "pnpm run", "pnpm test",;
        "go list", "go version", "go env",;
        "npm list", "npm ls", "npm version",;
        "pip list", "pip show",;
        "cargo tree", "cargo version",;
        "go build", "go test", "go fmt", "go vet",;
        "make", "cmake",;
        "cargo build", "cargo test", "cargo check",;
    }
        var denyPatterns = []String{
        "rm -rf", "rm -fr",;
        "mkfs", "dd if=", "dd of=",;
        "shred",;
        "> /dev/", ">/dev/",;
        "sudo ", "su ", "doas ",;
        "chmod 777", "chmod -R 777",;
        "chown ", "chgrp ",;
        "curl -d", "curl --data", "curl -X POST", "curl -X PUT",;
        "wget --post",;
        "nc ", "netcat ",;
        "scp ", "rsync ",;
        "history",;
        ".bash_history", ".zsh_history",;
        ".ssh/id_rsa", ".ssh/id_dsa", ".ssh/id_ecdsa", ".ssh/id_ed25519",;
        ".ssh/config",;
        ".aws/credentials", ".aws/config",;
        ".gnupg/",;
        "/etc/shadow", "/etc/passwd",;
        ":(){ :|:& };:", // fork bomb;
        "chmod +s",      // setuid;
        "mkfifo",;
    }
        var denyPathPatterns = []String{
        ".env",;
        ".env.local",;
        ".env.production",;
        "credentials.json",;
        "secrets.json",;
        "secrets.yaml",;
        "secrets.yml",;
        ".pem",;
        ".key",;
    }

    public static class ApprovalManager {
        public map[String]boolean allowlist;
        public map[String]boolean prefixes;
        public sync.RWMutex mu;
    }
        func NewApprovalManager() *ApprovalManager {
        return &ApprovalManager{
        allowlist: make(map[String]boolean),;
        prefixes:  make(map[String]boolean),;
    }
    }

    public static boolean IsAutoAllowed(String command) {
        command = strings.TrimSpace(command);
        var fields = strings.Fields(command);
        if len(fields) > 0 && autoAllowCommands[fields[0]] {
        return true;
    }
        var for _, prefix = range autoAllowPrefixes {
        if strings.HasPrefix(command, prefix) {
        return true;
    }
    }
        return false;
    }

    public static void IsDenied() {
        var commandLower = strings.ToLower(command);
        var for _, pattern = range denyPatterns {
        if strings.Contains(commandLower, strings.ToLower(pattern)) {
        return true, pattern;
    }
    }
        var for _, pattern = range denyPathPatterns {
        if strings.Contains(commandLower, strings.ToLower(pattern)) {
        return true, pattern;
    }
    }
        return false, "";
    }

    public static String FormatDeniedResult(String command, String pattern) {
        return fmt.Sprintf("Command blocked: this command matches a dangerous pattern (%s) and cannot be executed. If this command is necessary, please ask the user to run it manually.", pattern);
    }

    public static String extractBashPrefix(String command) {
        var parts = strings.Split(command, "|");
        var firstCmd = strings.TrimSpace(parts[0]);
        var fields = strings.Fields(firstCmd);
        if len(fields) < 2 {
        return "";
    }
        var baseCmd = fields[0];
        var safeCommands = map[String]boolean{
        "cat": true, "ls": true, "head": true, "tail": true,;
        "less": true, "more": true, "file": true, "wc": true,;
        "grep": true, "find": true, "tree": true, "stat": true,;
        "sed": true,;
    }
        if !safeCommands[baseCmd] {
        return "";
    }
        var for _, arg = range fields[1:] {
        if strings.HasPrefix(arg, "-") {
        continue;
    }
        if isNumeric(arg) {
        continue;
    }
        if !strings.Contains(arg, "/") && !strings.Contains(arg, "\\") && !strings.HasPrefix(arg, ".") {
        continue;
    }
        arg = strings.ReplaceAll(arg, "\\", "/");
        if path.IsAbs(arg) {
        return "" // Absolute path - don't create prefix;
    }
        var cleaned = path.Clean(arg);
        if strings.HasPrefix(cleaned, "..") {
        return "" // Path escapes - don't create prefix;
    }
        if strings.Contains(arg, "..") {
        var origBase = strings.SplitN(arg, "/", 2)[0];
        var cleanedBase = strings.SplitN(cleaned, "/", 2)[0];
        if origBase != cleanedBase {
        return "" // Path escaped to sibling directory;
    }
    }
        var isDir = strings.HasSuffix(arg, "/");
        var dir String;
        if isDir {
        dir = cleaned;
        } else {
        dir = path.Dir(cleaned);
    }
        if dir == "." {
        return fmt.Sprintf("%s:./", baseCmd);
    }
        return fmt.Sprintf("%s:%s/", baseCmd, dir);
    }
        var for _, arg = range fields[1:] {
        if strings.HasPrefix(arg, "-") {
        continue;
    }
        if isNumeric(arg) {
        continue;
    }
        return fmt.Sprintf("%s:./", baseCmd);
    }
        return "";
    }

    public static boolean isNumeric(String s) {
        var for _, c = range s {
        if c < '0' || c > '9' {
        return false;
    }
    }
        return len(s) > 0;
    }

    public static boolean isCommandOutsideCwd(String command) {
        var cwd, err = os.Getwd();
        if err != null {
        return false // Can't determine, assume safe;
    }
        var parts = strings.FieldsFunc(command, func(r rune) boolean {
        return r == '|' || r == ';' || r == '&';
        });
        var for _, part = range parts {
        part = strings.TrimSpace(part);
        var fields = strings.Fields(part);
        if len(fields) == 0 {
        continue;
    }
        var for _, arg = range fields[1:] {
        if strings.HasPrefix(arg, "-") {
        continue;
    }
        if strings.HasPrefix(arg, "/") || strings.HasPrefix(arg, "\\") {
        return true;
    }
        if filepath.IsAbs(arg) {
        var absPath = filepath.Clean(arg);
        if !strings.HasPrefix(absPath, cwd) {
        return true;
    }
        continue;
    }
        if strings.HasPrefix(arg, "..") {
        var absPath = filepath.Join(cwd, arg);
        absPath = filepath.Clean(absPath);
        if !strings.HasPrefix(absPath, cwd) {
        return true;
    }
    }
        if strings.HasPrefix(arg, "~") {
        var home, err = os.UserHomeDir();
        if err == null && !strings.HasPrefix(home, cwd) {
        return true;
    }
    }
    }
    }
        return false;
    }

    public static String AllowlistKey(String toolName, map[String]any args) {
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        return fmt.Sprintf("bash:%s", cmd);
    }
    }
        return toolName;
    }
        func (a *ApprovalManager) IsAllowed(toolName String, args map[String]any) boolean {
        a.mu.RLock();
        defer a.mu.RUnlock();
        var key = AllowlistKey(toolName, args);
        if a.allowlist[key] {
        return true;
    }
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        var prefix = extractBashPrefix(cmd);
        if prefix != "" {
        if a.prefixes[prefix] {
        return true;
    }
        if a.matchesHierarchicalPrefix(prefix) {
        return true;
    }
    }
    }
    }
        if toolName != "bash" && a.allowlist[toolName] {
        return true;
    }
        return false;
    }
        func (a *ApprovalManager) matchesHierarchicalPrefix(currentPrefix String) boolean {
        var colonIdx = strings.Index(currentPrefix, ":");
        if colonIdx == -1 {
        return false;
    }
        var currentCmd = currentPrefix[:colonIdx];
        var currentPath = currentPrefix[colonIdx+1:];
        var for storedPrefix = range a.prefixes {
        var storedColonIdx = strings.Index(storedPrefix, ":");
        if storedColonIdx == -1 {
        continue;
    }
        var storedCmd = storedPrefix[:storedColonIdx];
        var storedPath = storedPrefix[storedColonIdx+1:];
        if currentCmd != storedCmd {
        continue;
    }
        if strings.HasPrefix(currentPath, storedPath) {
        return true;
    }
    }
        return false;
    }
        func (a *ApprovalManager) AddToAllowlist(toolName String, args map[String]any) {
        a.mu.Lock();
        defer a.mu.Unlock();
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        var prefix = extractBashPrefix(cmd);
        if prefix != "" {
        a.prefixes[prefix] = true;
        return;
    }
        a.allowlist[fmt.Sprintf("bash:%s", cmd)] = true;
        return;
    }
    }
        a.allowlist[toolName] = true;
    }
        func (a *ApprovalManager) RequestApproval(toolName String, args map[String]any) (ApprovalResult, error) {
        var toolDisplay = formatToolDisplay(toolName, args);
        var fd = int(os.Stdin.Fd());
        var oldState, err = term.MakeRaw(fd);
        if err != null {
        return a.fallbackApproval(toolDisplay);
    }
        flushStdin(fd);
        var isWarning = false;
        var warningMsg String;
        var allowlistInfo String;
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        if isCommandOutsideCwd(cmd) {
        isWarning = true;
        warningMsg = "command targets paths outside project";
    }
        var if prefix = extractBashPrefix(cmd); prefix != "" {
        var colonIdx = strings.Index(prefix, ":");
        if colonIdx != -1 {
        var cmdName = prefix[:colonIdx];
        var dirPath = prefix[colonIdx+1:];
        if dirPath != "./" {
        allowlistInfo = fmt.Sprintf("%s in %s directory (includes subdirs)", cmdName, dirPath);
        } else {
        allowlistInfo = fmt.Sprintf("%s in %s directory", cmdName, dirPath);
    }
    }
    }
    }
    }
        var selected, denyReason, err = runSelector(fd, oldState, toolDisplay, isWarning, warningMsg, allowlistInfo);
        if err != null {
        term.Restore(fd, oldState);
        return ApprovalResult{Decision: ApprovalDeny}, err;
    }
        term.Restore(fd, oldState);
        switch selected {
        case -1: // Ctrl+C cancelled;
        return ApprovalResult{Decision: ApprovalDeny, DenyReason: "cancelled"}, null;
        case 0:;
        return ApprovalResult{Decision: ApprovalOnce}, null;
        case 1:;
        return ApprovalResult{Decision: ApprovalAlways}, null;
        default:;
        return ApprovalResult{Decision: ApprovalDeny, DenyReason: denyReason}, null;
    }
    }

    public static String formatToolDisplay(String toolName, map[String]any args) {
        var sb strings.Builder;
        var displayName = ToolDisplayName(toolName);
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        sb.WriteString(fmt.Sprintf("Tool: %s\n", displayName));
        sb.WriteString(fmt.Sprintf("Command: %s", cmd));
        return sb.String();
    }
    }
        if toolName == "web_search" {
        var if query, ok = args["query"].(String); ok {
        sb.WriteString(fmt.Sprintf("Tool: %s\n", displayName));
        sb.WriteString(fmt.Sprintf("Query: %s\n", query));
        sb.WriteString("Uses internet via ollama.com");
        return sb.String();
    }
    }
        if toolName == "web_fetch" {
        var if url, ok = args["url"].(String); ok {
        sb.WriteString(fmt.Sprintf("Tool: %s\n", displayName));
        sb.WriteString(fmt.Sprintf("URL: %s\n", url));
        sb.WriteString("Uses internet via ollama.com");
        return sb.String();
    }
    }
        sb.WriteString(fmt.Sprintf("Tool: %s", displayName));
        if len(args) > 0 {
        sb.WriteString("\nArguments: ");
        var first = true;
        var for k, v = range args {
        if !first {
        sb.WriteString(", ");
    }
        sb.WriteString(fmt.Sprintf("%s=%v", k, v));
        first = false;
    }
    }
        return sb.String();
    }

    public static class selectorState {
        public String toolDisplay;
        public int selected;
        public int totalLines;
        public int termWidth;
        public int termHeight;
        public int boxWidth;
        public int innerWidth;
        public String denyReason;
        public boolean isWarning;
        public String warningMessage;
        public String allowlistInfo;
    }

    public static void runSelector(int fd, *term.State oldState, String toolDisplay, boolean isWarning, String warningMessage) {
        var state = &selectorState{
        toolDisplay:    toolDisplay,;
        selected:       0,;
        isWarning:      isWarning,;
        warningMessage: warningMessage,;
        allowlistInfo:  allowlistInfo,;
    }
        state.termWidth, state.termHeight, _ = term.GetSize(fd);
        if state.termWidth < 20 {
        state.termWidth = 80 // fallback;
    }
        state.boxWidth = (state.termWidth * 90) / 100;
        if state.boxWidth > 60 {
        state.boxWidth = 60;
    }
        if state.boxWidth < 24 {
        state.boxWidth = 24;
    }
        if state.boxWidth > state.termWidth-1 {
        state.boxWidth = state.termWidth - 1;
    }
        state.innerWidth = state.boxWidth - 4 // account for "│ " and " │";
        state.totalLines = calculateTotalLines(state);
        fmt.Fprint(os.Stderr, "\033[?25l");
        defer fmt.Fprint(os.Stderr, "\033[?25h") // Show cursor when done;
        renderSelectorBox(state);
        var numOptions = len(optionLabels);
        for {
        var buf = make([]byte, 8);
        var n, err = os.Stdin.Read(buf);
        if err != null {
        clearSelectorBox(state);
        return 2, "", err;
    }
        var for i = 0; i < n; i++ {
        var ch = buf[i];
        if ch == 27 && i+2 < n && buf[i+1] == '[' {
        var oldSelected = state.selected;
        switch buf[i+2] {
        case 'A': // Up arrow;
        if state.selected > 0 {
        state.selected--;
    }
        case 'B': // Down arrow;
        if state.selected < numOptions-1 {
        state.selected++;
    }
    }
        if oldSelected != state.selected {
        updateSelectorOptions(state);
    }
        i += 2 // Skip the rest of escape sequence;
        continue;
    }
        switch {
        case ch == 3:;
        clearSelectorBox(state);
        return -1, "", null // -1 indicates cancelled;
        case ch == 13:;
        clearSelectorBox(state);
        if state.selected == 2 { // Deny;
        return 2, state.denyReason, null;
    }
        return state.selected, "", null;
        case ch >= '1' && ch <= '3':;
        var selected = int(ch - '1');
        clearSelectorBox(state);
        if selected == 2 { // Deny;
        return 2, state.denyReason, null;
    }
        return selected, "", null;
        case ch == 127 || ch == 8:;
        if len(state.denyReason) > 0 {
        var runes = []rune(state.denyReason);
        state.denyReason = String(runes[:len(runes)-1]);
        updateReasonInput(state);
    }
        case ch == 27:;
        if len(state.denyReason) > 0 {
        state.denyReason = "";
        updateReasonInput(state);
    }
        case ch >= 32 && ch < 127:;
        var maxLen = state.innerWidth - 2;
        if maxLen < 10 {
        maxLen = 10;
    }
        if len(state.denyReason) < maxLen {
        state.denyReason += String(ch);
        if state.selected != 2 {
        state.selected = 2;
        updateSelectorOptions(state);
        } else {
        updateReasonInput(state);
    }
    }
    }
    }
    }
    }
        func wrapText(text String, maxWidth int) []String {
        if maxWidth < 5 {
        maxWidth = 5;
    }
        var lines []String;
        var for _, line = range strings.Split(text, "\n") {
        if len(line) <= maxWidth {
        lines = append(lines, line);
        continue;
    }
        for len(line) > maxWidth {
        var breakAt = maxWidth;
        var for i = maxWidth; i > maxWidth/2; i-- {
        if i < len(line) && line[i] == ' ' {
        breakAt = i;
        break;
    }
    }
        lines = append(lines, line[:breakAt]);
        line = strings.TrimLeft(line[breakAt:], " ");
    }
        if len(line) > 0 {
        lines = append(lines, line);
    }
    }
        return lines;
    }
        func getHintLines(state *selectorState) []String {
        var hint = "up/down select, enter confirm, 1-3 quick select, ctrl+c cancel";
        if state.termWidth >= len(hint)+1 {
        return []String{hint}
    }
        return wrapText(hint, state.termWidth-1);
    }

    public static int calculateTotalLines(*selectorState state) {
        var toolLines = strings.Split(state.toolDisplay, "\n");
        var hintLines = getHintLines(state);
        var warningLines = 0;
        if state.isWarning {
        warningLines = 2 // warning line + blank line after;
    }
        return warningLines + len(toolLines) + 1 + len(optionLabels) + 1 + len(hintLines);
    }

    public static void renderSelectorBox(*selectorState state) {
        var toolLines = strings.Split(state.toolDisplay, "\n");
        var hintLines = getHintLines(state);
        if state.isWarning {
        if state.warningMessage != "" {
        fmt.Fprintf(os.Stderr, "\033[1mwarning:\033[0m %s\033[K\r\n", state.warningMessage);
        } else {
        fmt.Fprintf(os.Stderr, "\033[1mwarning:\033[0m command targets paths outside project\033[K\r\n");
    }
        fmt.Fprintf(os.Stderr, "\033[K\r\n") // blank line after warning;
    }
        var for _, line = range toolLines {
        fmt.Fprintf(os.Stderr, "%s\033[K\r\n", line);
    }
        fmt.Fprintf(os.Stderr, "\033[K\r\n");
        var for i, label = range optionLabels {
        if i == 2 {
        var denyLabel = "3. Deny: ";
        var inputDisplay = state.denyReason;
        if inputDisplay == "" {
        inputDisplay = "\033[90m(optional reason)\033[0m";
    }
        if i == state.selected {
        fmt.Fprintf(os.Stderr, "  \033[1m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
        } else {
        fmt.Fprintf(os.Stderr, "  \033[37m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
    }
        } else {
        var displayLabel = label;
        if i == 1 && state.allowlistInfo != "" {
        displayLabel = fmt.Sprintf("%s  \033[90m%s\033[0m", label, state.allowlistInfo);
    }
        if i == state.selected {
        fmt.Fprintf(os.Stderr, "  \033[1m%s\033[0m\033[K\r\n", displayLabel);
        } else {
        fmt.Fprintf(os.Stderr, "  \033[37m%s\033[0m\033[K\r\n", displayLabel);
    }
    }
    }
        fmt.Fprintf(os.Stderr, "\033[K\r\n");
        var for i, line = range hintLines {
        if i == len(hintLines)-1 {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K", line);
        } else {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K\r\n", line);
    }
    }
    }

    public static void updateSelectorOptions(*selectorState state) {
        var hintLines = getHintLines(state);
        var linesToMove = len(hintLines) - 1 + 1 + len(optionLabels);
        fmt.Fprintf(os.Stderr, "\033[%dA\r", linesToMove);
        var for i, label = range optionLabels {
        if i == 2 {
        var denyLabel = "3. Deny: ";
        var inputDisplay = state.denyReason;
        if inputDisplay == "" {
        inputDisplay = "\033[90m(optional reason)\033[0m";
    }
        if i == state.selected {
        fmt.Fprintf(os.Stderr, "  \033[1m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
        } else {
        fmt.Fprintf(os.Stderr, "  \033[37m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
    }
        } else {
        var displayLabel = label;
        if i == 1 && state.allowlistInfo != "" {
        displayLabel = fmt.Sprintf("%s  \033[90m%s\033[0m", label, state.allowlistInfo);
    }
        if i == state.selected {
        fmt.Fprintf(os.Stderr, "  \033[1m%s\033[0m\033[K\r\n", displayLabel);
        } else {
        fmt.Fprintf(os.Stderr, "  \033[37m%s\033[0m\033[K\r\n", displayLabel);
    }
    }
    }
        fmt.Fprintf(os.Stderr, "\033[K\r\n");
        var for i, line = range hintLines {
        if i == len(hintLines)-1 {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K", line);
        } else {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K\r\n", line);
    }
    }
    }

    public static void updateReasonInput(*selectorState state) {
        var hintLines = getHintLines(state);
        var linesToMove = len(hintLines) - 1 + 1 + 1;
        fmt.Fprintf(os.Stderr, "\033[%dA\r", linesToMove);
        var denyLabel = "3. Deny: ";
        var inputDisplay = state.denyReason;
        if inputDisplay == "" {
        inputDisplay = "\033[90m(optional reason)\033[0m";
    }
        if state.selected == 2 {
        fmt.Fprintf(os.Stderr, "  \033[1m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
        } else {
        fmt.Fprintf(os.Stderr, "  \033[37m%s\033[0m%s\033[K\r\n", denyLabel, inputDisplay);
    }
        fmt.Fprintf(os.Stderr, "\033[K\r\n");
        var for i, line = range hintLines {
        if i == len(hintLines)-1 {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K", line);
        } else {
        fmt.Fprintf(os.Stderr, "\033[90m%s\033[0m\033[K\r\n", line);
    }
    }
    }

    public static void clearSelectorBox(*selectorState state) {
        fmt.Fprint(os.Stderr, "\r\033[K");
        for range state.totalLines - 1 {
        fmt.Fprint(os.Stderr, "\033[A\033[K");
    }
        fmt.Fprint(os.Stderr, "\r");
    }
        func (a *ApprovalManager) fallbackApproval(toolDisplay String) (ApprovalResult, error) {
        fmt.Fprintln(os.Stderr);
        fmt.Fprintln(os.Stderr, toolDisplay);
        fmt.Fprintln(os.Stderr);
        fmt.Fprintln(os.Stderr, "[1] Execute once  [2] Allow for this session  [3] Deny");
        fmt.Fprint(os.Stderr, "choice: ");
        var input String;
        fmt.Scanln(&input);
        switch input {
        case "1":;
        return ApprovalResult{Decision: ApprovalOnce}, null;
        case "2":;
        return ApprovalResult{Decision: ApprovalAlways}, null;
        default:;
        fmt.Fprint(os.Stderr, "Reason (optional): ");
        var reason String;
        fmt.Scanln(&reason);
        return ApprovalResult{Decision: ApprovalDeny, DenyReason: reason}, null;
    }
    }
        func (a *ApprovalManager) Reset() {
        a.mu.Lock();
        defer a.mu.Unlock();
        a.allowlist = make(map[String]boolean);
        a.prefixes = make(map[String]boolean);
    }
        func (a *ApprovalManager) AllowedTools() []String {
        a.mu.RLock();
        defer a.mu.RUnlock();
        var tools = make([]String, 0, len(a.allowlist)+len(a.prefixes));
        var for tool = range a.allowlist {
        tools = append(tools, tool);
    }
        var for prefix = range a.prefixes {
        tools = append(tools, prefix+"*");
    }
        return tools;
    }

    public static String FormatApprovalResult(String toolName, map[String]any args, ApprovalResult result) {
        var label String;
        var displayName = ToolDisplayName(toolName);
        switch result.Decision {
        case ApprovalOnce:;
        label = "Approved";
        case ApprovalAlways:;
        label = "Always allowed";
        case ApprovalDeny:;
        label = "Denied";
    }
        if toolName == "bash" {
        var if cmd, ok = args["command"].(String); ok {
        if len(cmd) > 40 {
        cmd = cmd[:37] + "...";
    }
        return fmt.Sprintf("\033[1m%s:\033[0m %s: %s", label, displayName, cmd);
    }
    }
        if toolName == "web_search" {
        var if query, ok = args["query"].(String); ok {
        if len(query) > 40 {
        query = query[:37] + "...";
    }
        return fmt.Sprintf("\033[1m%s:\033[0m %s: %s", label, displayName, query);
    }
    }
        if toolName == "web_fetch" {
        var if url, ok = args["url"].(String); ok {
        if len(url) > 50 {
        url = url[:47] + "...";
    }
        return fmt.Sprintf("\033[1m%s:\033[0m %s: %s", label, displayName, url);
    }
    }
        return fmt.Sprintf("\033[1m%s:\033[0m %s", label, displayName);
    }

    public static String FormatDenyResult(String toolName, String reason) {
        if reason != "" {
        return fmt.Sprintf("User denied execution of %s. Reason: %s", toolName, reason);
    }
        return fmt.Sprintf("User denied execution of %s.", toolName);
    }

    public static void PromptYesNo() {
        var fd = int(os.Stdin.Fd());
        var oldState, err = term.MakeRaw(fd);
        if err != null {
        return false, err;
    }
        defer term.Restore(fd, oldState);
        var selected = 0 // 0 = Yes, 1 = No;
        var options = []String{"Yes", "No"}
        fmt.Fprint(os.Stderr, "\033[?25l");
        defer fmt.Fprint(os.Stderr, "\033[?25h");
        var renderYesNo = func() {
        fmt.Fprintf(os.Stderr, "\r\033[K");
        fmt.Fprintf(os.Stderr, "%s  ", question);
        var for i, opt = range options {
        if i == selected {
        fmt.Fprintf(os.Stderr, "\033[1m%s\033[0m  ", opt);
        } else {
        fmt.Fprintf(os.Stderr, "\033[37m%s\033[0m  ", opt);
    }
    }
    }
        renderYesNo();
        var buf = make([]byte, 3);
        for {
        var n, err = os.Stdin.Read(buf);
        if err != null {
        return false, err;
    }
        if n == 1 {
        switch buf[0] {
        case 'y', 'Y':;
        selected = 0;
        renderYesNo();
        case 'n', 'N':;
        selected = 1;
        renderYesNo();
        case '\r', '\n': // Enter;
        fmt.Fprintf(os.Stderr, "\r\033[K") // Clear line;
        return selected == 0, null;
        case 3: // Ctrl+C;
        fmt.Fprintf(os.Stderr, "\r\033[K");
        return false, null;
        case 27: // Escape - could be arrow key;
        continue;
    }
        } else if n == 3 && buf[0] == 27 && buf[1] == 91 {
        switch buf[2] {
        case 'D': // Left;
        if selected > 0 {
        selected--;
    }
        renderYesNo();
        case 'C': // Right;
        if selected < len(options)-1 {
        selected++;
    }
        renderYesNo();
    }
    }
    }
    }
}
