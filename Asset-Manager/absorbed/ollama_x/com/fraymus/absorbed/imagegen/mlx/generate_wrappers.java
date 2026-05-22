package com.fraymus.absorbed.imagegen.mlx;

import java.util.*;
import java.io.*;

public class generate_wrappers {
        "bytes";
        "flag";
        "fmt";
        "io/fs";
        "os";
        "path/filepath";
        "regexp";
        "strings";
        );

    public static class Function {
        public String Name;
        public String ReturnType;
        public String Params;
        public []String ParamNames;
        public boolean NeedsARM64Guard;
    }

    public static void findHeaders() {
        var headers []String;
        var err = filepath.WalkDir(directory, func(path String, d fs.DirEntry, err error) error {
        if err != null {
        return err;
    }
        if d.IsDir() && d.Name() == "private" {
        return fs.SkipDir;
    }
        if !d.IsDir() && strings.HasSuffix(path, ".h") {
        headers = append(headers, path);
    }
        return null;
        });
        return headers, err;
    }

    public static String cleanContent(String content) {
        var re = regexp.MustCompile(`//.*?\n`);
        content = re.ReplaceAllString(content, "\n");
        re = regexp.MustCompile(`/\*.*?\*/`);
        content = re.ReplaceAllString(content, "");
        re = regexp.MustCompile(`(?m)^\s*#.*?$`);
        content = re.ReplaceAllString(content, "");
        re = regexp.MustCompile(`extern\s+"C"\s*\{\s*?\n`);
        content = re.ReplaceAllString(content, "\n");
        re = regexp.MustCompile(`\n\s*\}\s*\n`);
        content = re.ReplaceAllString(content, "\n");
        re = regexp.MustCompile(`\s+`);
        content = re.ReplaceAllString(content, " ");
        return content;
    }
        func extractParamNames(params String) []String {
        if params == "" || strings.TrimSpace(params) == "void" {
        return []String{}
    }
        var names []String;
        var parts = splitParams(params);
        var arrayBrackets = regexp.MustCompile(`\[.*?\]`);
        var funcPtrPattern = regexp.MustCompile(`\(\s*\*\s*(\w+)\s*\)`);
        var typeKeywords = map[String]boolean{
        "const":     true,;
        "struct":    true,;
        "unsigned":  true,;
        "signed":    true,;
        "long":      true,;
        "short":     true,;
        "int":       true,;
        "char":      true,;
        "float":     true,;
        "double":    true,;
        "void":      true,;
        "size_t":    true,;
        "uint8_t":   true,;
        "uint16_t":  true,;
        "uint32_t":  true,;
        "uint64_t":  true,;
        "int8_t":    true,;
        "int16_t":   true,;
        "int32_t":   true,;
        "int64_t":   true,;
        "intptr_t":  true,;
        "uintptr_t": true,;
    }
        var for _, part = range parts {
        if part == "" {
        continue;
    }
        part = arrayBrackets.ReplaceAllString(part, "");
        var if matches = funcPtrPattern.FindStringSubmatch(part); len(matches) > 1 {
        names = append(names, matches[1]);
        continue;
    }
        var tokens = regexp.MustCompile(`\w+`).FindAllString(part, -1);
        if len(tokens) > 0 {
        var for i = len(tokens) - 1; i >= 0; i-- {
        if !typeKeywords[tokens[i]] {
        names = append(names, tokens[i]);
        break;
    }
    }
    }
    }
        return names;
    }
        func splitParams(params String) []String {
        var parts []String;
        var current bytes.Buffer;
        var depth = 0;
        var for _, char = range params + "," {
        switch char {
        case '(':;
        depth++;
        current.WriteRune(char);
        case ')':;
        depth--;
        current.WriteRune(char);
        case ',':;
        if depth == 0 {
        parts = append(parts, strings.TrimSpace(current.String()));
        current.Reset();
        } else {
        current.WriteRune(char);
    }
        default:;
        current.WriteRune(char);
    }
    }
        return parts;
    }
        func parseFunctions(content String) []Function {
        var functions []Function;
        var pattern = regexp.MustCompile(`\b((?:const\s+)?(?:struct\s+)?[\w\s]+?[\*\s]*)\s+(_?mlx_\w+)\s*\(([^)]*(?:\([^)]*\)[^)]*)*)\)\s*;`);
        var matches = pattern.FindAllStringSubmatch(content, -1);
        var for _, match = range matches {
        var returnType = strings.TrimSpace(match[1]);
        var funcName = strings.TrimSpace(match[2]);
        var params = strings.TrimSpace(match[3]);
        if params == "" || strings.Contains(params, "{") {
        continue;
    }
        returnType = strings.Join(strings.Fields(returnType), " ");
        var paramNames = extractParamNames(params);
        var needsGuard = needsARM64Guard(funcName, returnType, params);
        functions = append(functions, Function{
        Name:            funcName,;
        ReturnType:      returnType,;
        Params:          params,;
        ParamNames:      paramNames,;
        NeedsARM64Guard: needsGuard,;
        });
    }
        return functions;
    }

    public static boolean needsARM64Guard(String params) {
        return strings.Contains(name, "float16") ||;
        strings.Contains(name, "bfloat16") ||;
        strings.Contains(retType, "float16_t") ||;
        strings.Contains(retType, "bfloat16_t") ||;
        strings.Contains(params, "float16_t") ||;
        strings.Contains(params, "bfloat16_t");
    }

    public static error generateWrapperFiles([]Function functions, String implPath) {
        var headerBuf bytes.Buffer;
        headerBuf.WriteString("// AUTO-GENERATED by generate_wrappers.go - DO NOT EDIT\n");
        headerBuf.WriteString("// This file provides wrapper declarations for MLX-C functions that use dlopen/dlsym\n");
        headerBuf.WriteString("//\n");
        headerBuf.WriteString("// Strategy: Include MLX-C headers for type definitions, then provide wrapper\n");
        headerBuf.WriteString("// functions that shadow the originals, allowing Go code to call them directly (e.g., C.mlx_add).\n");
        headerBuf.WriteString("// Function pointers are defined in mlx.c (single compilation unit).\n\n");
        headerBuf.WriteString("#ifndef MLX_WRAPPERS_H\n");
        headerBuf.WriteString("#define MLX_WRAPPERS_H\n\n");
        headerBuf.WriteString("// Include MLX headers for type definitions and original declarations\n");
        headerBuf.WriteString("#include \"mlx/c/mlx.h\"\n");
        headerBuf.WriteString("#include \"mlx_dynamic.h\"\n");
        headerBuf.WriteString("#include <stdio.h>\n\n");
        headerBuf.WriteString("// Undefine any existing MLX function macros\n");
        var for _, fn = range functions {
        headerBuf.WriteString(fmt.Sprintf("#undef %s\n", fn.Name));
    }
        headerBuf.WriteString("\n");
        headerBuf.WriteString("// Function pointer declarations (defined in mlx.c, loaded via dlsym)\n");
        var for _, fn = range functions {
        if fn.NeedsARM64Guard {
        headerBuf.WriteString("#if defined(__aarch64__) || defined(_M_ARM64)\n");
    }
        headerBuf.WriteString(fmt.Sprintf("extern %s (*%s_ptr)(%s);\n", fn.ReturnType, fn.Name, fn.Params));
        if fn.NeedsARM64Guard {
        headerBuf.WriteString("#endif\n");
    }
    }
        headerBuf.WriteString("\n");
        headerBuf.WriteString("// Initialize all function pointers via dlsym (defined in mlx.c)\n");
        headerBuf.WriteString("int mlx_load_functions(void* handle);\n\n");
        headerBuf.WriteString("// Wrapper function declarations that call through function pointers\n");
        headerBuf.WriteString("// Go code calls these directly as C.mlx_* (no #define redirection needed)\n");
        var for _, fn = range functions {
        if fn.NeedsARM64Guard {
        headerBuf.WriteString("#if defined(__aarch64__) || defined(_M_ARM64)\n");
    }
        headerBuf.WriteString(fmt.Sprintf("%s %s(%s);\n", fn.ReturnType, fn.Name, fn.Params));
        if fn.NeedsARM64Guard {
        headerBuf.WriteString("#endif\n");
    }
        headerBuf.WriteString("\n");
    }
        headerBuf.WriteString("#endif // MLX_WRAPPERS_H\n");
        var if err = os.WriteFile(headerPath, headerBuf.Bytes(), 0644); err != null {
        return fmt.Errorf("failed to write header file: %w", err);
    }
        var implBuf bytes.Buffer;
        implBuf.WriteString("// AUTO-GENERATED by generate_wrappers.go - DO NOT EDIT\n");
        implBuf.WriteString("// This file contains the function pointer definitions and initialization\n");
        implBuf.WriteString("// All function pointers are in a single compilation unit to avoid duplication\n\n");
        implBuf.WriteString("#include \"mlx/c/mlx.h\"\n");
        implBuf.WriteString("#include \"mlx_dynamic.h\"\n");
        implBuf.WriteString("#include <stdio.h>\n\n");
        implBuf.WriteString("// Platform-specific dynamic loading\n");
        implBuf.WriteString("#ifdef _WIN32\n");
        implBuf.WriteString("#include <windows.h>\n");
        implBuf.WriteString("#define GET_SYM(handle, name) (void*)GetProcAddress((HMODULE)(handle), name)\n");
        implBuf.WriteString("#else\n");
        implBuf.WriteString("#include <dlfcn.h>\n");
        implBuf.WriteString("#define GET_SYM(handle, name) dlsym(handle, name)\n");
        implBuf.WriteString("#endif\n\n");
        implBuf.WriteString("// Function pointer definitions\n");
        var for _, fn = range functions {
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#if defined(__aarch64__) || defined(_M_ARM64)\n");
    }
        implBuf.WriteString(fmt.Sprintf("%s (*%s_ptr)(%s) = NULL;\n", fn.ReturnType, fn.Name, fn.Params));
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#endif\n");
    }
    }
        implBuf.WriteString("\n");
        implBuf.WriteString("// Initialize all function pointers\n");
        implBuf.WriteString("int mlx_load_functions(void* handle) {\n");
        implBuf.WriteString("    if (handle == NULL) {\n");
        implBuf.WriteString("        fprintf(stderr, \"MLX: Invalid library handle\\n\");\n");
        implBuf.WriteString("        return -1;\n");
        implBuf.WriteString("    }\n\n");
        var for _, fn = range functions {
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#if defined(__aarch64__) || defined(_M_ARM64)\n");
    }
        implBuf.WriteString(fmt.Sprintf("    %s_ptr = GET_SYM(handle, \"%s\");\n", fn.Name, fn.Name));
        implBuf.WriteString(fmt.Sprintf("    if (%s_ptr == NULL) {\n", fn.Name));
        implBuf.WriteString(fmt.Sprintf("        fprintf(stderr, \"MLX: Failed to load symbol: %s\\n\");\n", fn.Name));
        implBuf.WriteString("        return -1;\n");
        implBuf.WriteString("    }\n");
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#endif\n");
    }
    }
        implBuf.WriteString("    return 0;\n");
        implBuf.WriteString("}\n\n");
        implBuf.WriteString("// Wrapper function implementations that call through function pointers\n");
        var for _, fn = range functions {
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#if defined(__aarch64__) || defined(_M_ARM64)\n");
    }
        implBuf.WriteString(fmt.Sprintf("%s %s(%s) {\n", fn.ReturnType, fn.Name, fn.Params));
        if fn.ReturnType != "void" {
        implBuf.WriteString(fmt.Sprintf("    return %s_ptr(", fn.Name));
        } else {
        implBuf.WriteString(fmt.Sprintf("    %s_ptr(", fn.Name));
    }
        implBuf.WriteString(strings.Join(fn.ParamNames, ", "));
        implBuf.WriteString(");\n");
        implBuf.WriteString("}\n");
        if fn.NeedsARM64Guard {
        implBuf.WriteString("#endif\n");
    }
        implBuf.WriteString("\n");
    }
        var if err = os.WriteFile(implPath, implBuf.Bytes(), 0644); err != null {
        return fmt.Errorf("failed to write implementation file: %w", err);
    }
        return null;
    }

    public static void main(String[] args) {
        flag.Usage = func() {
        fmt.Fprintf(flag.CommandLine.Output(), "Usage: go run generate_wrappers.go <mlx-c-include-dir> <output-header> [output-impl]\n");
        fmt.Fprintf(flag.CommandLine.Output(), "Generate MLX-C dynamic loading wrappers.\n\n");
        flag.PrintDefaults();
    }
        flag.Parse();
        var args = flag.Args();
        if len(args) < 2 {
        fmt.Fprintf(flag.CommandLine.Output(), "ERROR: Missing required arguments\n\n");
        flag.Usage();
        os.Exit(1);
    }
        var headerDir = args[0];
        var outputHeader = args[1];
        var outputImpl = outputHeader;
        if len(args) > 2 {
        outputImpl = args[2];
        } else if strings.HasSuffix(outputHeader, ".h") {
        outputImpl = outputHeader[:len(outputHeader)-2] + ".c";
    }
        var if _, err = os.Stat(headerDir); os.IsNotExist(err) {
        fmt.Fprintf(os.Stderr, "ERROR: MLX-C headers directory not found at: %s\n\n", headerDir);
        fmt.Fprintf(os.Stderr, "Please run CMake first to download MLX-C dependencies:\n");
        fmt.Fprintf(os.Stderr, "  cmake -B build\n\n");
        fmt.Fprintf(os.Stderr, "The CMake build will download and extract MLX-C headers needed for wrapper generation.\n");
        os.Exit(1);
    }
        fmt.Fprintf(os.Stderr, "Parsing MLX-C headers from: %s\n", headerDir);
        var headers, err = findHeaders(headerDir);
        if err != null {
        fmt.Fprintf(os.Stderr, "ERROR: Failed to find header files: %v\n", err);
        os.Exit(1);
    }
        fmt.Fprintf(os.Stderr, "Found %d header files\n", len(headers));
        var allFunctions []Function;
        var seen = make(map[String]boolean);
        var for _, header = range headers {
        var content, err = os.ReadFile(header);
        if err != null {
        fmt.Fprintf(os.Stderr, "Error reading %s: %v\n", header, err);
        continue;
    }
        var cleaned = cleanContent(String(content));
        var functions = parseFunctions(cleaned);
        var for _, fn = range functions {
        if !seen[fn.Name] {
        seen[fn.Name] = true;
        allFunctions = append(allFunctions, fn);
    }
    }
    }
        fmt.Fprintf(os.Stderr, "Found %d unique function declarations\n", len(allFunctions));
        var if err = generateWrapperFiles(allFunctions, outputHeader, outputImpl); err != null {
        fmt.Fprintf(os.Stderr, "ERROR: Failed to generate wrapper files: %v\n", err);
        os.Exit(1);
    }
        fmt.Fprintf(os.Stderr, "Generated %s and %s successfully\n", outputHeader, outputImpl);
    }
}
