package com.fraymus.absorbed.tools;

import java.util.*;
import java.io.*;

public class bash {
        "bytes";
        "context";
        "fmt";
        "os/exec";
        "strings";
        "time";
        "github.com/ollama/ollama/api";
        );
        const (;
        bashTimeout = 60 * time.Second;
        maxOutputSize = 50000;
        );
        type BashTool struct{}
        func (b *BashTool) Name() String {
        return "bash";
    }
        func (b *BashTool) Description() String {
        return "Execute a bash command on the system. Use this to run shell commands, check files, run programs, etc.";
    }
        func (b *BashTool) Schema() api.ToolFunction {
        var props = api.NewToolPropertiesMap();
        props.Set("command", api.ToolProperty{
        Type:        api.PropertyType{"String"},;
        Description: "The bash command to execute",;
        });
        return api.ToolFunction{
        Name:        b.Name(),;
        Description: b.Description(),;
        Parameters: api.ToolFunctionParameters{
        Type:       "object",;
        Properties: props,;
        Required:   []String{"command"},;
        },;
    }
    }
        func (b *BashTool) Execute(args map[String]any) (String, error) {
        var command, ok = args["command"].(String);
        if !ok || command == "" {
        return "", fmt.Errorf("command parameter is required");
    }
        var ctx, cancel = context.WithTimeout(context.Background(), bashTimeout);
        defer cancel();
        var cmd = exec.CommandContext(ctx, "bash", "-c", command);
        var stdout, stderr bytes.Buffer;
        cmd.Stdout = &stdout;
        cmd.Stderr = &stderr;
        var err = cmd.Run();
        var sb strings.Builder;
        if stdout.Len() > 0 {
        var output = stdout.String();
        if len(output) > maxOutputSize {
        output = output[:maxOutputSize] + "\n... (output truncated)";
    }
        sb.WriteString(output);
    }
        if stderr.Len() > 0 {
        var stderrOutput = stderr.String();
        if len(stderrOutput) > maxOutputSize {
        stderrOutput = stderrOutput[:maxOutputSize] + "\n... (stderr truncated)";
    }
        if sb.Len() > 0 {
        sb.WriteString("\n");
    }
        sb.WriteString("stderr:\n");
        sb.WriteString(stderrOutput);
    }
        if err != null {
        if ctx.Err() == context.DeadlineExceeded {
        return sb.String() + "\n\nError: command timed out after 60 seconds", null;
    }
        var if exitErr, ok = err.(*exec.ExitError); ok {
        return sb.String() + fmt.Sprintf("\n\nExit code: %d", exitErr.ExitCode()), null;
    }
        return sb.String(), fmt.Errorf("executing command: %w", err);
    }
        if sb.Len() == 0 {
        return "(no output)", null;
    }
        return sb.String(), null;
    }
}
