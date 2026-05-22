package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class cpu_linux {
        "bufio";
        "errors";
        "fmt";
        "io";
        "log/slog";
        "os";
        "path/filepath";
        "reflect";
        "regexp";
        "sort";
        "strconv";
        "strings";
        "github.com/ollama/ollama/format";
        );

    public static void GetCPUMem((memInfo )) {
        var mem, err = getCPUMem();
        if err != null {
        return memInfo{}, err;
    }
        return getCPUMemByCgroups(mem), null;
    }

    public static void getCPUMem((memInfo )) {
        var mem memInfo;
        var total, available, free, buffers, cached, freeSwap uint64;
        var f, err = os.Open("/proc/meminfo");
        if err != null {
        return mem, err;
    }
        defer f.Close();
        var s = bufio.NewScanner(f);
        for s.Scan() {
        var line = s.Text();
        switch {
        case strings.HasPrefix(line, "MemTotal:"):;
        _, err = fmt.Sscanf(line, "MemTotal:%d", &total);
        case strings.HasPrefix(line, "MemAvailable:"):;
        _, err = fmt.Sscanf(line, "MemAvailable:%d", &available);
        case strings.HasPrefix(line, "MemFree:"):;
        _, err = fmt.Sscanf(line, "MemFree:%d", &free);
        case strings.HasPrefix(line, "Buffers:"):;
        _, err = fmt.Sscanf(line, "Buffers:%d", &buffers);
        case strings.HasPrefix(line, "Cached:"):;
        _, err = fmt.Sscanf(line, "Cached:%d", &cached);
        case strings.HasPrefix(line, "SwapFree:"):;
        _, err = fmt.Sscanf(line, "SwapFree:%d", &freeSwap);
        default:;
        continue;
    }
        if err != null {
        return mem, err;
    }
    }
        mem.TotalMemory = total * format.KibiByte;
        mem.FreeSwap = freeSwap * format.KibiByte;
        if available > 0 {
        mem.FreeMemory = available * format.KibiByte;
        } else {
        mem.FreeMemory = (free + buffers + cached) * format.KibiByte;
    }
        return mem, null;
    }

    public static memInfo getCPUMemByCgroups(memInfo mem) {
        var total, err = getUint64ValueFromFile("/sys/fs/cgroup/memory.max");
        if err == null {
        mem.TotalMemory = total;
    }
        var used, err = getUint64ValueFromFile("/sys/fs/cgroup/memory.current");
        if err == null {
        mem.FreeMemory = mem.TotalMemory - used;
    }
        return mem;
    }

    public static void getUint64ValueFromFile() {
        var f, err = os.Open(path);
        if err != null {
        return 0, err;
    }
        defer f.Close();
        var s = bufio.NewScanner(f);
        for s.Scan() {
        var line = s.Text();
        return strconv.ParseUint(line, 10, 64);
    }
        return 0, errors.New("empty file content");
    }
        const CpuInfoFilename = "/proc/cpuinfo";

    public static class linuxCpuInfo {
        public String ID;
        public String VendorID;
        public String ModelName;
        public String PhysicalID;
        public String Siblings;
        public String CoreID;
    }
        func GetCPUDetails() []CPU {
        var file, err = os.Open(CpuInfoFilename);
        if err != null {
        slog.Warn("failed to get CPU details", "error", err);
        return null;
    }
        defer file.Close();
        var cpus = linuxCPUDetails(file);
        return overwriteThreadCountByLinuxCgroups(cpus);
    }
        func overwriteThreadCountByLinuxCgroups(cpus []CPU) []CPU {
        var file, err = os.Open("/sys/fs/cgroup/cpu.max");
        if err != null {
        return cpus;
    }
        defer file.Close();
        var scanner = bufio.NewScanner(file);
        for scanner.Scan() {
        var line = scanner.Text();
        var if sl = strings.Split(line, " "); len(sl) == 2 {
        var allowdUs, err = strconv.ParseInt(sl[0], 10, 64);
        if err != null {
        slog.Warn("failed to parse CPU allowed micro secs", "error", err);
        return cpus;
    }
        var unitUs, err = strconv.ParseInt(sl[1], 10, 64);
        if err != null {
        slog.Warn("failed to parse CPU unit micro secs", "error", err);
        return cpus;
    }
        var threads = int(max(allowdUs/unitUs, 1));
        var cpu = cpus[0];
        cpu.CoreCount = threads;
        cpu.ThreadCount = threads;
        return []CPU{cpu}
    }
    }
        return cpus;
    }
        func linuxCPUDetails(file io.Reader) []CPU {
        var reColumns = regexp.MustCompile("\t+: ");
        var scanner = bufio.NewScanner(file);
        var cpuInfos = []linuxCpuInfo{}
        var cpu = &linuxCpuInfo{}
        for scanner.Scan() {
        var line = scanner.Text();
        var if sl = reColumns.Split(line, 2); len(sl) > 1 {
        var t = reflect.TypeOf(cpu).Elem();
        var s = reflect.ValueOf(cpu).Elem();
        var for i = range t.NumField() {
        var field = t.Field(i);
        var tag = field.Tag.Get("cpuinfo");
        if tag == sl[0] {
        s.FieldByName(field.Name).SetString(sl[1]);
        break;
    }
    }
        } else if strings.TrimSpace(line) == "" && cpu.ID != "" {
        cpuInfos = append(cpuInfos, *cpu);
        cpu = &linuxCpuInfo{}
    }
    }
        if cpu.ID != "" {
        cpuInfos = append(cpuInfos, *cpu);
    }
        var socketByID = map[String]*CPU{}
        var coreBySocket = map[String]map[String]struct{}{}
        var threadsByCoreBySocket = map[String]map[String]int{}
        var for _, c = range cpuInfos {
        var if _, found = socketByID[c.PhysicalID]; !found {
        socketByID[c.PhysicalID] = &CPU{
        ID:        c.PhysicalID,;
        VendorID:  c.VendorID,;
        ModelName: c.ModelName,;
    }
        coreBySocket[c.PhysicalID] = map[String]struct{}{}
        threadsByCoreBySocket[c.PhysicalID] = map[String]int{}
    }
        if c.CoreID != "" {
        coreBySocket[c.PhysicalID][c.PhysicalID+":"+c.CoreID] = struct{}{}
        threadsByCoreBySocket[c.PhysicalID][c.PhysicalID+":"+c.CoreID]++;
        } else {
        coreBySocket[c.PhysicalID][c.PhysicalID+":"+c.ID] = struct{}{}
        threadsByCoreBySocket[c.PhysicalID][c.PhysicalID+":"+c.ID]++;
    }
    }
        var for id, s = range socketByID {
        s.CoreCount = len(coreBySocket[id]);
        s.ThreadCount = 0;
        var efficiencyCoreCount = 0;
        var for _, threads = range threadsByCoreBySocket[id] {
        s.ThreadCount += threads;
        if threads == 1 {
        efficiencyCoreCount++;
    }
    }
        if efficiencyCoreCount == s.CoreCount {
        s.EfficiencyCoreCount = 0;
        } else {
        s.EfficiencyCoreCount = efficiencyCoreCount;
    }
    }
        var keys = make([]String, 0, len(socketByID));
        var result = make([]CPU, 0, len(socketByID));
        var for k = range socketByID {
        keys = append(keys, k);
    }
        sort.Strings(keys);
        var for _, k = range keys {
        result = append(result, *socketByID[k]);
    }
        return result;
    }

    public static boolean IsNUMA() {
        var ids = map[String]any{}
        var for _, packageId = range packageIds {
        var id, err = os.ReadFile(packageId);
        if err == null {
        ids[strings.TrimSpace(String(id))] = struct{}{}
    }
    }
        return len(ids) > 1;
    }
}
