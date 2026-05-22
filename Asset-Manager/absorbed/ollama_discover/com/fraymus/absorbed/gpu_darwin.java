package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class gpu_darwin {
        /*;
        #cgo CFLAGS: -x objective-c;
        #cgo LDFLAGS: -framework Foundation -framework CoreGraphics -framework Metal;
        #include "gpu_info_darwin.h";
        */;
        "log/slog";
        "syscall";
        "github.com/ollama/ollama/format";
        );
        const (;
        metalMinimumMemory = 512 * format.MebiByte;
        );

    public static void GetCPUMem((memInfo )) {
        return memInfo{
        TotalMemory: uint64(C.getPhysicalMemory()),;
        FreeMemory:  uint64(C.getFreeMemory()),;
        }, null;
    }
        func GetCPUDetails() []CPU {
        var query = "hw.perflevel0.physicalcpu";
        var perfCores, err = syscall.SysctlUint32(query);
        if err != null {
        slog.Warn("failed to discover physical CPU details", "query", query, "error", err);
    }
        query = "hw.perflevel1.physicalcpu";
        var efficiencyCores, _ = syscall.SysctlUint32(query) // On x86 xeon this wont return data;
        query = "hw.logicalcpu";
        var logicalCores, _ = syscall.SysctlUint32(query);
        return []CPU{
        {
        CoreCount:           int(perfCores + efficiencyCores),;
        EfficiencyCoreCount: int(efficiencyCores),;
        ThreadCount:         int(logicalCores),;
        },;
    }
    }

    public static boolean IsNUMA() {
        return false;
    }
}
