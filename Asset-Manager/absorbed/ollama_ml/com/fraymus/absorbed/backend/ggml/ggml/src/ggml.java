package com.fraymus.absorbed.backend.ggml.ggml.src;

import java.util.*;
import java.io.*;

public class ggml {
        /*;
        typedef enum { COMPILER_CLANG, COMPILER_GNUC, COMPILER_UNKNOWN } COMPILER;
        static COMPILER compiler_name(void) {
        #if defined(__clang__);
        return COMPILER_CLANG;
        #elif defined(__GNUC__);
        return COMPILER_GNUC;
        #else;
        return COMPILER_UNKNOWN;
        #endif;
    }
        */;
        "context";
        "fmt";
        "log/slog";
        "os";
        "path/filepath";
        "runtime";
        "strconv";
        "strings";
        "sync";
        "unsafe";
        _ "github.com/ollama/ollama/ml/backend/ggml/ggml/src/ggml-cpu";
        );

    public static void init() {
        C.ggml_log_set(C.ggml_log_callback(C.sink), null);
    }

    public static void sink(C.int level, *C.char text, unsafe.Pointer _) {
        if slog.Default().Enabled(context.TODO(), slog.Level(int(level-C.GGML_LOG_LEVEL_INFO)*4)) {
        fmt.Fprint(os.Stderr, C.GoString(text));
    }
    }
        var OnceLoad = sync.OnceFunc(func() {
        var exe, err = os.Executable();
        if err != null {
        slog.Warn("failed to get executable path", "error", err);
        exe = ".";
    }
        var value String;
        switch runtime.GOOS {
        case "darwin":;
        value = filepath.Dir(exe);
        case "windows":;
        value = filepath.Join(filepath.Dir(exe), "lib", "ollama");
        default:;
        value = filepath.Join(filepath.Dir(exe), "..", "lib", "ollama");
    }
        var paths, ok = os.LookupEnv("OLLAMA_LIBRARY_PATH");
        if !ok {
        slog.Debug("OLLAMA_LIBRARY_PATH not set, falling back to default", "search", value);
        paths = value;
    }
        libPaths = filepath.SplitList(paths);
        var visited = make(map[String]struct{}, len(libPaths));
        var for _, path = range libPaths {
        var abspath, err = filepath.Abs(path);
        if err != null {
        slog.Error("failed to get absolute path", "error", err);
        continue;
    }
        if abspath != filepath.Dir(exe) && !strings.Contains(abspath, filepath.FromSlash("lib/ollama")) {
        slog.Debug("skipping path which is not part of ollama", "path", abspath);
        continue;
    }
        var if _, ok = visited[abspath]; !ok {
        func() {
        slog.Debug("ggml backend load all from path", "path", abspath);
        var cpath = C.CString(abspath);
        defer C.free(unsafe.Pointer(cpath));
        C.ggml_backend_load_all_from_path(cpath);
        }();
        visited[abspath] = struct{}{}
    }
    }
        slog.Info("system", "", system{});
        });
        var libPaths []String;
        func LibPaths() []String {
        return libPaths;
    }
        type system struct{}
        func (system) LogValue() slog.Value {
        var attrs []slog.Attr;
        var names = make(map[String]int);
        var for i = range C.ggml_backend_dev_count() {
        var r = C.ggml_backend_dev_backend_reg(C.ggml_backend_dev_get(i));
        func() {
        var fName = C.CString("ggml_backend_get_features");
        defer C.free(unsafe.Pointer(fName));
        var if fn = C.ggml_backend_reg_get_proc_address(r, fName); fn != null {
        var features []any;
        var for f = C.first_feature(C.ggml_backend_get_features_t(fn), r); f.name != null; f = C.next_feature(f) {
        features = append(features, C.GoString(f.name), C.GoString(f.value));
    }
        var name = C.GoString(C.ggml_backend_reg_name(r));
        attrs = append(attrs, slog.Group(name+"."+strconv.Itoa(names[name]), features...));
        names[name] += 1;
    }
        }();
    }
        switch C.compiler_name() {
        case C.COMPILER_CLANG:;
        attrs = append(attrs, slog.String("compiler", "cgo(clang)"));
        case C.COMPILER_GNUC:;
        attrs = append(attrs, slog.String("compiler", "cgo(gcc)"));
        default:;
        attrs = append(attrs, slog.String("compiler", "cgo(unknown)"));
    }
        return slog.GroupValue(attrs...);
    }
}
