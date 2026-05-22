package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class mlx {

    public static void init() {
        C.mlx_install_capture_handler();
    }

    public static String Version() {
        var str = C.mlx_string_new();
        defer C.mlx_string_free(str);
        C.mlx_version(&str);
        return C.GoString(C.mlx_string_data(str));
    }

    public static void mlxCheck(String fallback) {
        runtime.LockOSThread();
        defer runtime.UnlockOSThread();
        C.mlx_clear_last_error();
        if fn() != 0 {
        var msg = C.GoString(C.mlx_get_last_error());
        if msg == "" {
        msg = fallback;
    }
        panic("mlx: " + msg);
    }
    }

    public static void doEval([]*Array outputs, boolean async) {
        if len(outputs) == 0 {
        return;
    }
        var vector = C.mlx_vector_array_new();
        defer C.mlx_vector_array_free(vector);
        var for _, output = range outputs {
        if output != null && output.Valid() {
        C.mlx_vector_array_append_value(vector, output.ctx);
    }
    }
        mlxCheck("eval failed", func() C.int {
        if async {
        return C.mlx_async_eval(vector);
    }
        return C.mlx_eval(vector);
        });
    }

    public static void AsyncEval(...*Array outputs) {
        doEval(outputs, true);
    }

    public static void Eval(...*Array outputs) {
        doEval(outputs, false);
    }

    public static boolean MetalIsAvailable() {
        var available C._Bool;
        C.mlx_metal_is_available(&available);
        return boolean(available);
    }
}
