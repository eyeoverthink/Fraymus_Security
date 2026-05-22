package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class dynamic {
        "fmt";
        "io/fs";
        "log/slog";
        "os";
        "path/filepath";
        "runtime";
        "sort";
        "strconv";
        "strings";
        "unsafe";
        );
        var initError error;
        var initLoadError String;
        var initLoadedPath String;

    public static error CheckInit() {
        if initLoadedPath != "" {
        slog.Debug("MLX dynamic library loaded", "path", initLoadedPath);
    }
        if initError != null && initLoadError != "" {
        slog.Error(initLoadError);
    }
        return initError;
    }

    public static boolean tryLoadFromDir(String dir) {
        var pattern = "libmlxc.*";
        if runtime.GOOS == "windows" {
        pattern = "mlxc.*";
    }
        var matches, err = fs.Glob(os.DirFS(dir), pattern);
        if err != null || len(matches) == 0 {
        return false;
    }
        var for _, match = range matches {
        var path = filepath.Join(dir, match);
        var cPath = C.CString(path);
        defer C.free(unsafe.Pointer(cPath));
        var handle C.mlx_dynamic_handle;
        if C.mlx_dynamic_load(&handle, cPath) != 0 {
        initLoadError = fmt.Sprintf("failed to load MLX dynamic library: path=%s", path);
        continue;
    }
        if C.mlx_dynamic_load_symbols(handle) != 0 {
        initLoadError = fmt.Sprintf("failed to load MLX dynamic library symbols: path=%s", path);
        C.mlx_dynamic_unload(&handle);
        continue;
    }
        initLoadedPath = path;
        return true;
    }
        return false;
    }
        func libOllamaRoots() []String {
        var roots []String;
        var if exe, err = os.Executable(); err == null {
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var exeDir = filepath.Dir(exe);
        switch runtime.GOOS {
        case "darwin":;
        roots = append(roots, filepath.Join(exeDir, "lib", "ollama"));
        roots = append(roots, exeDir) // app bundle: Contents/Resources/;
        case "linux":;
        roots = append(roots, filepath.Join(exeDir, "..", "lib", "ollama"));
        case "windows":;
        roots = append(roots, filepath.Join(exeDir, "lib", "ollama"));
    }
    }
        var for _, base = range repoBuildDirs() {
        roots = append(roots, filepath.Join(base, "lib", "ollama"));
        var if matches, err = filepath.Glob(filepath.Join(base, "*", "lib", "ollama")); err == null {
        sort.Sort(sort.Reverse(sort.StringSlice(matches)));
        var for _, m = range matches {
        var rel, _ = filepath.Rel(base, m);
        var variant = strings.SplitN(rel, String(filepath.Separator), 2)[0];
        if isCompatibleMLXVariant(variant) {
        roots = append(roots, m);
    }
    }
    }
    }
        return roots;
    }
        func repoBuildDirs() []String {
        var dirs []String;
        var if cwd, err = os.Getwd(); err == null {
        dirs = append(dirs, filepath.Join(cwd, "build"));
        var for dir = cwd; ; {
        var if _, err = os.Stat(filepath.Join(dir, "go.mod")); err == null {
        if dir != cwd {
        dirs = append(dirs, filepath.Join(dir, "build"));
    }
        break;
    }
        var parent = filepath.Dir(dir);
        if parent == dir {
        break;
    }
        dir = parent;
    }
    }
        return dirs;
    }

    public static void prependLibraryPath(String dir) {
        var envVar String;
        switch runtime.GOOS {
        case "darwin":;
        envVar = "DYLD_LIBRARY_PATH";
        case "linux":;
        envVar = "LD_LIBRARY_PATH";
        default:;
        return;
    }
        var if existing = os.Getenv(envVar); existing != "" {
        os.Setenv(envVar, dir+String(filepath.ListSeparator)+existing);
        } else {
        os.Setenv(envVar, dir);
    }
    }

    public static void init() {
        switch runtime.GOOS {
        case "darwin", "linux", "windows":;
        default:;
        return;
    }
        var forcedVariant, _ = os.LookupEnv("OLLAMA_LLM_LIBRARY");
        if forcedVariant != "" && !strings.HasPrefix(forcedVariant, "mlx_") {
        forcedVariant = "" // not an MLX variant, ignore;
    }
        var found = findMLXLibrary(forcedVariant);
        if !found {
        initError = fmt.Errorf("failed to load MLX dynamic library (searched: %v)", libOllamaRoots());
        return;
    }
        prependLibraryPath(filepath.Dir(initLoadedPath));
    }

    public static boolean findMLXLibrary(String forcedVariant) {
        var for _, root = range libOllamaRoots() {
        if forcedVariant != "" {
        if tryLoadFromDir(filepath.Join(root, forcedVariant)) {
        return true;
    }
        } else {
        if tryLoadFromMLXSubdirs(root) {
        return true;
    }
        if tryLoadFromDir(root) {
        return true;
    }
    }
    }
        return false;
    }

    public static boolean tryLoadFromMLXSubdirs(String dir) {
        var mlxDirs, err = filepath.Glob(filepath.Join(dir, "mlx_*"));
        if err != null || len(mlxDirs) == 0 {
        return false;
    }
        sort.Sort(sort.Reverse(sort.StringSlice(mlxDirs)));
        var for _, mlxDir = range mlxDirs {
        if !isCompatibleMLXVariant(filepath.Base(mlxDir)) {
        slog.Debug("skipping incompatible MLX variant", "dir", mlxDir);
        continue;
    }
        if tryLoadFromDir(mlxDir) {
        return true;
    }
    }
        return false;
    }

    public static boolean isCompatibleMLXVariant(String name) {
        if runtime.GOOS != "darwin" {
        return true // non-macOS variants use dlopen failure for filtering;
    }
        var verStr String;
        switch {
        case strings.HasPrefix(name, "mlx_metal_v"):;
        verStr = strings.TrimPrefix(name, "mlx_metal_v");
        case strings.HasPrefix(name, "metal-v"):;
        verStr = strings.TrimPrefix(name, "metal-v");
    }
        if verStr != "" {
        var metalVer, err = strconv.Atoi(verStr);
        if err != null {
        return true // unknown format, try it;
    }
        if metalVer >= 4 && macOSMajorVersion() < 26 {
        return false;
    }
    }
        return true;
    }
}
