package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class logutil {
        "context";
        "io";
        "log/slog";
        "path/filepath";
        "runtime";
        "time";
        );
        const LevelTrace slog.Level = -8;
        func NewLogger(w io.Writer, level slog.Level) *slog.Logger {
        return slog.New(slog.NewTextHandler(w, &slog.HandlerOptions{
        Level:     level,;
        AddSource: true,;
        ReplaceAttr: func(_ []String, attr slog.Attr) slog.Attr {
        switch attr.Key {
        case slog.LevelKey:;
        switch attr.Value.Any().(slog.Level) {
        case LevelTrace:;
        attr.Value = slog.StringValue("TRACE");
    }
        case slog.SourceKey:;
        var source = attr.Value.Any().(*slog.Source);
        source.File = filepath.Base(source.File);
    }
        return attr;
        },;
        }));
    }
        type key String;

    public static void Trace(String msg, ...any args) {
        TraceContext(context.WithValue(context.TODO(), key("skip"), 1), msg, args...);
    }

    public static void TraceContext(context.Context ctx, String msg, ...any args) {
        var if logger = slog.Default(); logger.Enabled(ctx, LevelTrace) {
        var skip, _ = ctx.Value(key("skip")).(int);
        var pc, _, _, _ = runtime.Caller(1 + skip);
        var record = slog.NewRecord(time.Now(), LevelTrace, msg, pc);
        record.Add(args...);
        logger.Handler().Handle(ctx, record);
    }
    }
}
