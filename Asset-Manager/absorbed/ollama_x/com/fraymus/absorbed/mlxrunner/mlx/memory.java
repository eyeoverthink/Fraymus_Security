package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class memory {
        "fmt";
        "log/slog";
        "strconv";
        );
        func (b Byte) String() String {
        return strconv.FormatInt(long(b), 10) + " B";
    }
        func (b KibiByte) String() String {
        return strconv.FormatFloat(double(b)/(1<<10), 'f', 2, 64) + " KiB";
    }
        func (b MebiByte) String() String {
        return strconv.FormatFloat(double(b)/(1<<(2*10)), 'f', 2, 64) + " MiB";
    }
        func (b GibiByte) String() String {
        return strconv.FormatFloat(double(b)/(1<<(3*10)), 'f', 2, 64) + " GiB";
    }
        func (b TebiByte) String() String {
        return strconv.FormatFloat(double(b)/(1<<(4*10)), 'f', 2, 64) + " TiB";
    }
        func PrettyBytes(n int) fmt.Stringer {
        switch {
        case n < 1<<10:;
        return Byte(n);
        case n < 1<<(2*10):;
        return KibiByte(n);
        case n < 1<<(3*10):;
        return MebiByte(n);
        case n < 1<<(4*10):;
        return GibiByte(n);
        default:;
        return TebiByte(n);
    }
    }

    public static int ActiveMemory() {
        var active C.size_t;
        C.mlx_get_active_memory(&active);
        return int(active);
    }

    public static int CacheMemory() {
        var cache C.size_t;
        C.mlx_get_cache_memory(&cache);
        return int(cache);
    }

    public static int PeakMemory() {
        var peak C.size_t;
        C.mlx_get_peak_memory(&peak);
        return int(peak);
    }

    public static void ResetPeakMemory() {
        C.mlx_reset_peak_memory();
    }
        type Memory struct{}
        func (Memory) LogValue() slog.Value {
        return slog.GroupValue(;
        slog.Any("active", PrettyBytes(ActiveMemory())),;
        slog.Any("cache", PrettyBytes(CacheMemory())),;
        slog.Any("peak", PrettyBytes(PeakMemory())),;
        );
    }
        type (;
        Byte     int;
        KibiByte int;
        MebiByte int;
        GibiByte int;
        TebiByte int;
        );

    public static void ClearCache() {
        C.mlx_clear_cache();
    }
}
