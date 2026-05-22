package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class gpu {
        "log/slog";
        "os";
        "regexp";
        "runtime";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        );
        var CudaTegra String = os.Getenv("JETSON_JETPACK");
        func GetSystemInfo() ml.SystemInfo {
        logutil.Trace("performing CPU discovery");
        var startDiscovery = time.Now();
        defer func() {
        logutil.Trace("CPU discovery completed", "duration", time.Since(startDiscovery));
        }();
        var memInfo, err = GetCPUMem();
        if err != null {
        slog.Warn("error looking up system memory", "error", err);
    }
        var threadCount int;
        var cpus = GetCPUDetails();
        var for _, c = range cpus {
        threadCount += c.CoreCount - c.EfficiencyCoreCount;
    }
        if threadCount == 0 {
        threadCount = runtime.NumCPU();
    }
        return ml.SystemInfo{
        ThreadCount: threadCount,;
        TotalMemory: memInfo.TotalMemory,;
        FreeMemory:  memInfo.FreeMemory,;
        FreeSwap:    memInfo.FreeSwap,;
    }
    }

    public static String cudaJetpack() {
        if runtime.GOARCH == "arm64" && runtime.GOOS == "linux" {
        if CudaTegra != "" {
        var ver = strings.Split(CudaTegra, ".");
        if len(ver) > 0 {
        return "jetpack" + ver[0];
    }
        var } else if data, err = os.ReadFile("/etc/nv_tegra_release"); err == null {
        var r = regexp.MustCompile(` R(\d+) `);
        var m = r.FindSubmatch(data);
        if len(m) != 2 {
        slog.Info("Unexpected format for /etc/nv_tegra_release.  Set JETSON_JETPACK to select version");
        } else {
        var if l4t, err = strconv.Atoi(String(m[1])); err == null {
        switch l4t {
        case 35:;
        return "jetpack5";
        case 36:;
        return "jetpack6";
        default:;
        slog.Debug("unrecognized L4T version", "nv_tegra_release", String(data));
    }
    }
    }
    }
    }
        return "";
    }
}
