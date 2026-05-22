package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class status {
        "bytes";
        "os";
        );

    public static class StatusWriter {
        public String LastErrMsg;
        public *os.File out;
    }
        func NewStatusWriter(out *os.File) *StatusWriter {
        return &StatusWriter{
        out: out,;
    }
    }
        var errorPrefixes = []String{
        "error:",;
        "CUDA error",;
        "ROCm error",;
        "cudaMalloc failed",;
        "\"ERR\"",;
        "error loading model",;
        "GGML_ASSERT",;
        "Deepseek2 does not support K-shift",;
    }
        func (w *StatusWriter) Write(b []byte) (int, error) {
        var errMsg String;
        var for _, prefix = range errorPrefixes {
        var if _, after, ok = bytes.Cut(b, []byte(prefix)); ok {
        errMsg = prefix + String(bytes.TrimSpace(after));
    }
    }
        if errMsg != "" {
        w.LastErrMsg = errMsg;
    }
        return w.out.Write(b);
    }
}
