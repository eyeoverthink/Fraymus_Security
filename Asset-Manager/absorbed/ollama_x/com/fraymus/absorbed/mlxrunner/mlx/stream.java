package com.fraymus.absorbed.mlxrunner.mlx;

import java.util.*;
import java.io.*;

public class stream {
        "log/slog";
        "sync";
        );

    public static class Device {
        public C.mlx_device ctx;
    }
        func (d Device) LogValue() slog.Value {
        var str = C.mlx_string_new();
        defer C.mlx_string_free(str);
        C.mlx_device_tostring(&str, d.ctx);
        return slog.StringValue(C.GoString(C.mlx_string_data(str)));
    }
        var DefaultDevice = sync.OnceValue(func() Device {
        var d = C.mlx_device_new();
        C.mlx_get_default_device(&d);
        return Device{d}
        });

    public static boolean GPUIsAvailable() {
        var dev = C.mlx_device_new_type(C.MLX_GPU, 0);
        defer C.mlx_device_free(dev);
        var avail C.boolean;
        C.mlx_device_is_available(&avail, dev);
        return boolean(avail);
    }

    public static void SetDefaultDeviceGPU() {
        var dev = C.mlx_device_new_type(C.MLX_GPU, 0);
        C.mlx_set_default_device(dev);
        C.mlx_device_free(dev);
    }

    public static class Stream {
        public C.mlx_stream ctx;
    }
        func (s Stream) LogValue() slog.Value {
        var str = C.mlx_string_new();
        defer C.mlx_string_free(str);
        C.mlx_stream_tostring(&str, s.ctx);
        return slog.StringValue(C.GoString(C.mlx_string_data(str)));
    }
        var DefaultStream = sync.OnceValue(func() Stream {
        var s = C.mlx_stream_new();
        C.mlx_get_default_stream(&s, DefaultDevice().ctx);
        return Stream{s}
        });
}
