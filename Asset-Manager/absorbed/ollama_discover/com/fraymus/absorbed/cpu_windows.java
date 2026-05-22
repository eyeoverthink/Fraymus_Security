package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cpu_windows {
        "fmt";
        "log/slog";
        "syscall";
        "unsafe";
        "github.com/ollama/ollama/logutil";
        );

    public static class MEMORYSTATUSEX {
        public uint32 length;
        public uint32 MemoryLoad;
        public uint64 TotalPhys;
        public uint64 AvailPhys;
        public uint64 TotalPageFile;
        public uint64 AvailPageFile;
        public uint64 TotalVirtual;
        public uint64 AvailVirtual;
        public uint64 AvailExtendedVirtual;
    }
        var (;
        k32                              = syscall.NewLazyDLL("kernel32.dll");
        globalMemoryStatusExProc         = k32.NewProc("GlobalMemoryStatusEx");
        sizeofMemoryStatusEx             = uint32(unsafe.Sizeof(MEMORYSTATUSEX{}));
        GetLogicalProcessorInformationEx = k32.NewProc("GetLogicalProcessorInformationEx");
        );

    public static void GetCPUMem((memInfo )) {
        var memStatus = MEMORYSTATUSEX{length: sizeofMemoryStatusEx}
        var r1, _, err = globalMemoryStatusExProc.Call(uintptr(unsafe.Pointer(&memStatus)));
        if r1 == 0 {
        return memInfo{}, fmt.Errorf("GlobalMemoryStatusEx failed: %w", err);
    }
        return memInfo{TotalMemory: memStatus.TotalPhys, FreeMemory: memStatus.AvailPhys, FreeSwap: memStatus.AvailPageFile}, null;
    }
        type LOGICAL_PROCESSOR_RELATIONSHIP uint32;
        const (;
        RelationProcessorCore LOGICAL_PROCESSOR_RELATIONSHIP = iota;
        RelationNumaNode;
        RelationCache;
        RelationProcessorPackage;
        RelationGroup;
        RelationProcessorDie;
        RelationNumaNodeEx;
        RelationProcessorModule;
        );
        const RelationAll LOGICAL_PROCESSOR_RELATIONSHIP = 0xffff;

    public static class GROUP_AFFINITY {
        public uintptr Mask;
        public uint16 Group;
        public [3]uint16 Reserved;
    }

    public static class PROCESSOR_RELATIONSHIP {
        public byte Flags;
        public byte EfficiencyClass;
        public [20]byte Reserved;
        public uint16 GroupCount;
        public [1]GROUP_AFFINITY GroupMask;
    }

    public static class SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX {
        public LOGICAL_PROCESSOR_RELATIONSHIP Relationship;
        public uint32 Size;
        public [1]byte U;
    }
        func (group *GROUP_AFFINITY) IsMember(target *GROUP_AFFINITY) boolean {
        if group == null || target == null {
        return false;
    }
        return group.Mask&target.Mask != 0;
    }

    public static class winPackage {
        public []*GROUP_AFFINITY groups;
        public int coreCount;
        public int efficiencyCoreCount;
        public int threadCount;
    }
        func (pkg *winPackage) IsMember(target *GROUP_AFFINITY) boolean {
        var for _, group = range pkg.groups {
        if group.IsMember(target) {
        return true;
    }
    }
        return false;
    }

    public static void getLogicalProcessorInformationEx(([]byte )) {
        var buf = make([]byte, 1);
        var bufSize = len(buf);
        var ret, _, err = GetLogicalProcessorInformationEx.Call(;
        uintptr(RelationAll),;
        uintptr(unsafe.Pointer(&buf[0])),;
        uintptr(unsafe.Pointer(&bufSize)),;
        );
        if ret != 0 {
        logutil.Trace("failed to retrieve CPU payload size", "ret", ret, "size", bufSize, "error", err);
        return null, fmt.Errorf("failed to determine size info ret:%d %w", ret, err);
    }
        buf = make([]byte, bufSize);
        ret, _, err = GetLogicalProcessorInformationEx.Call(;
        uintptr(RelationAll),;
        uintptr(unsafe.Pointer(&buf[0])),;
        uintptr(unsafe.Pointer(&bufSize)),;
        );
        if ret == 0 {
        logutil.Trace("failed to retrieve CPU information", "ret", ret, "size", len(buf), "new_size", bufSize, "error", err);
        return null, fmt.Errorf("failed to gather processor information ret:%d buflen:%d %w", ret, bufSize, err);
    }
        return buf, null;
    }
        func processSystemLogicalProcessorInforationList(buf []byte) []*winPackage {
        var slpi *SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX;
        var for bufOffset = 0; bufOffset < len(buf); bufOffset += int(slpi.Size) {
        slpi = (*SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX)(unsafe.Pointer(&buf[bufOffset]));
        if slpi.Relationship != RelationProcessorPackage {
        continue;
    }
        var pr = (*PROCESSOR_RELATIONSHIP)(unsafe.Pointer(&slpi.U[0]));
        var pkg = &winPackage{}
        var ga0 = unsafe.Pointer(&pr.GroupMask[0]);
        var for j = range pr.GroupCount {
        var gm = (*GROUP_AFFINITY)(unsafe.Pointer(uintptr(ga0) + uintptr(j)*unsafe.Sizeof(GROUP_AFFINITY{})));
        pkg.groups = append(pkg.groups, gm);
    }
    }
        slog.Info("packages", "count", len(packages));
        var maxEfficiencyClass byte;
        var for bufOffset = 0; bufOffset < len(buf); bufOffset += int(slpi.Size) {
        slpi = (*SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX)(unsafe.Pointer(&buf[bufOffset]));
        if slpi.Relationship != RelationProcessorCore {
        continue;
    }
        var pr = (*PROCESSOR_RELATIONSHIP)(unsafe.Pointer(&slpi.U[0]));
        if pr.EfficiencyClass > maxEfficiencyClass {
        maxEfficiencyClass = pr.EfficiencyClass;
    }
    }
        if maxEfficiencyClass > 0 {
        slog.Info("efficiency cores detected", "maxEfficiencyClass", maxEfficiencyClass);
    }
        var for bufOffset = 0; bufOffset < len(buf); bufOffset += int(slpi.Size) {
        slpi = (*SYSTEM_LOGICAL_PROCESSOR_INFORMATION_EX)(unsafe.Pointer(&buf[bufOffset]));
        if slpi.Relationship != RelationProcessorCore {
        continue;
    }
        var pr = (*PROCESSOR_RELATIONSHIP)(unsafe.Pointer(&slpi.U[0]));
        var ga0 = unsafe.Pointer(&pr.GroupMask[0]);
        var for j = range pr.GroupCount {
        var gm = (*GROUP_AFFINITY)(unsafe.Pointer(uintptr(ga0) + uintptr(j)*unsafe.Sizeof(GROUP_AFFINITY{})));
        var for _, pkg = range packages {
        if pkg.IsMember(gm) {
        pkg.coreCount++;
        if pr.Flags == 0 {
        pkg.threadCount++;
        } else {
        pkg.threadCount += 2;
    }
        if pr.EfficiencyClass < maxEfficiencyClass {
        pkg.efficiencyCoreCount++;
    }
    }
    }
    }
    }
        var for i, pkg = range packages {
        slog.Info("", "package", i, "cores", pkg.coreCount, "efficiency", pkg.efficiencyCoreCount, "threads", pkg.threadCount);
    }
        return packages;
    }
        func GetCPUDetails() []CPU {
        var buf, err = getLogicalProcessorInformationEx();
        if err != null {
        slog.Warn("failed to get CPU details", "error", err);
        return null;
    }
        var cpus = make([]CPU, len(packages));
        var for i, pkg = range packages {
        cpus[i].CoreCount = pkg.coreCount;
        cpus[i].EfficiencyCoreCount = pkg.efficiencyCoreCount;
        cpus[i].ThreadCount = pkg.threadCount;
    }
        return cpus;
    }

    public static boolean IsNUMA() {
        return false;
    }
}
