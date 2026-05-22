package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class types {
        "log/slog";
        "path/filepath";
        "sort";
        "strings";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/ml";
        );

    public static class memInfo {
        public uint64 TotalMemory;
        public uint64 FreeMemory;
        public uint64 FreeSwap;
    }

    public static class CPU {
        public String ID;
        public String VendorID;
        public String ModelName;
        public int CoreCount;
        public int EfficiencyCoreCount;
        public int ThreadCount;
    }

    public static void LogDetails([]ml.DeviceInfo devices) {
        sort.Sort(sort.Reverse(ml.ByFreeMemory(devices))) // Report devices in order of scheduling preference;
        var for _, dev = range devices {
        var libs []String;
        var for _, dir = range dev.LibraryPath {
        if strings.Contains(dir, filepath.Join("lib", "ollama")) {
        libs = append(libs, filepath.Base(dir));
    }
    }
        var typeStr = "discrete";
        if dev.Integrated {
        typeStr = "iGPU";
    }
        slog.Info("inference compute",;
        "id", dev.ID,;
        "filter_id", dev.FilterID,;
        "library", dev.Library,;
        "compute", dev.Compute(),;
        "name", dev.Name,;
        "description", dev.Description,;
        "libdirs", strings.Join(libs, ","),;
        "driver", dev.Driver(),;
        "pci_id", dev.PCIID,;
        "type", typeStr,;
        "total", format.HumanBytes2(dev.TotalMemory),;
        "available", format.HumanBytes2(dev.FreeMemory),;
        );
    }
        if len(devices) == 0 {
        var dev, _ = GetCPUMem();
        slog.Info("inference compute",;
        "id", "cpu",;
        "library", "cpu",;
        "compute", "",;
        "name", "cpu",;
        "description", "cpu",;
        "libdirs", "ollama",;
        "driver", "",;
        "pci_id", "",;
        "type", "",;
        "total", format.HumanBytes2(dev.TotalMemory),;
        "available", format.HumanBytes2(dev.FreeMemory),;
        );
    }
    }
}
