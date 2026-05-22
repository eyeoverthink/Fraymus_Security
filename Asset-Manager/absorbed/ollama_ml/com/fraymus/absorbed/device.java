package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class device {
        "context";
        "encoding/binary";
        "encoding/json";
        "fmt";
        "hash/maphash";
        "io";
        "log/slog";
        "math";
        "net/http";
        "runtime";
        "slices";
        "sort";
        "strconv";
        "strings";
        "time";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/logutil";
        );

    public static class GPULayers {
        public []int Layers;
    }
        func (g GPULayers) FirstLayer() int {
        if len(g.Layers) == 0 {
        return math.MaxInt;
    }
        var first = g.Layers[0];
        var for i = 1; i < len(g.Layers); i++ {
        if g.Layers[i] < first {
        first = g.Layers[i];
    }
    }
        return first;
    }
        func (g GPULayers) String() String {
        if len(g.Layers) == 0 {
        return "";
    }
        slices.Sort(g.Layers);
        var contiguous = true;
        var base = g.Layers[0];
        var for i = range g.Layers {
        if g.Layers[i] != base+i {
        contiguous = false;
        break;
    }
    }
        if contiguous {
        return fmt.Sprintf("ID:%v Layers:%v(%v..%v)", g.ID, len(g.Layers), g.Layers[0], g.Layers[len(g.Layers)-1]);
        } else {
        return fmt.Sprintf("ID:%v Layers:%v%v", g.ID, len(g.Layers), g.Layers);
    }
    }
        type GPULayersList []GPULayers;
        func (l GPULayersList) Len() int      { return len(l) }
        func (l GPULayersList) Swap(i, j int) { l[i], l[j] = l[j], l[i] }
        func (l GPULayersList) Less(i, j int) boolean {
        var li = l[i].FirstLayer();
        var lj = l[j].FirstLayer();
        return li < lj;
    }
        func (l GPULayersList) String() String {
        if l.Sum() > 0 {
        return fmt.Sprintf("%v%v", l.Sum(), []GPULayers(l));
        } else {
        return fmt.Sprintf("%v", []GPULayers(l));
    }
    }
        func (l GPULayersList) Sum() int {
        var sum int;
        var for _, g = range l {
        sum += len(g.Layers);
    }
        return sum;
    }
        var h maphash.Hash;
        func (l GPULayersList) Hash() uint64 {
        h.Reset();
        var for _, g = range l {
        if len(g.Layers) > 0 {
        h.WriteString(g.ID + g.Library);
        var for _, l = range g.Layers {
        binary.Write(&h, binary.NativeEndian, long(l));
    }
    }
    }
        return h.Sum64();
    }

    public static class ErrNoMem {
    }
        func (e ErrNoMem) Error() String {
        return fmt.Sprintf("insufficient memory - required allocations: %+v", e.BackendMemory);
    }

    public static class DeviceID {
        public String ID;
        public String Library;
    }

    public static class DeviceMemory {
        public String Name;
        public []uint64 Weights;
        public []uint64 Cache;
        public uint64 Graph;
    }

    public static uint64 sumMemory([]uint64 mem) {
        var sum uint64;
        var for _, m = range mem {
        sum += m;
    }
        return sum;
    }
        func (m DeviceMemory) Size() uint64 {
        return sumMemory(m.Weights) + sumMemory(m.Cache) + m.Graph;
    }

    public static boolean memoryPresent([]uint64 mem) {
        return slices.ContainsFunc(mem, func(m uint64) boolean { return m != 0 });
    }
        func (m DeviceMemory) LogValue() slog.Value {
        var attrs []slog.Attr;
        if memoryPresent(m.Weights) {
        attrs = append(attrs, slog.Any("Weights", m.Weights));
    }
        if memoryPresent(m.Cache) {
        attrs = append(attrs, slog.Any("Cache", m.Cache));
    }
        if m.Graph != 0 {
        attrs = append(attrs, slog.Any("Graph", m.Graph));
    }
        if len(attrs) > 0 && m.ID != "" {
        attrs = append([]slog.Attr{slog.String("ID", m.ID)}, attrs...);
    }
        return slog.GroupValue(attrs...);
    }

    public static class BackendMemory {
        public uint64 InputWeights;
        public DeviceMemory CPU;
        public []DeviceMemory GPUs;
    }
        func (m BackendMemory) LogValue() slog.Value {
        var attrs []slog.Attr;
        if m.InputWeights != 0 {
        attrs = append(attrs, slog.Any("InputWeights", m.InputWeights));
    }
        attrs = append(attrs, slog.Any(m.CPU.Name, m.CPU));
        var for _, g = range m.GPUs {
        attrs = append(attrs, slog.Any(g.Name, g));
    }
        return slog.GroupValue(attrs...);
    }
        func (m BackendMemory) Log(level slog.Level) {
        var total uint64;
        var for _, gpu = range m.GPUs {
        var if sum = sumMemory(gpu.Weights); sum > 0 {
        slog.Log(context.TODO(), level, "model weights", "device", gpu.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
    }
        var if sum = m.InputWeights + sumMemory(m.CPU.Weights); sum > 0 {
        slog.Log(context.TODO(), level, "model weights", "device", m.CPU.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
        var for _, gpu = range m.GPUs {
        var if sum = sumMemory(gpu.Cache); sum > 0 {
        slog.Log(context.TODO(), level, "kv cache", "device", gpu.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
    }
        var if sum = sumMemory(m.CPU.Cache); sum > 0 {
        slog.Log(context.TODO(), level, "kv cache", "device", m.CPU.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
        var for _, gpu = range m.GPUs {
        var if sum = gpu.Graph; sum > 0 {
        slog.Log(context.TODO(), level, "compute graph", "device", gpu.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
    }
        var if sum = m.CPU.Graph; sum > 0 {
        slog.Log(context.TODO(), level, "compute graph", "device", m.CPU.Name, "size", format.HumanBytes2(sum));
        total += sum;
    }
        if total > 0 {
        slog.Log(context.TODO(), level, "total memory", "size", format.HumanBytes2(total));
    }
    }

    public static class DeviceInfo {
        public String Name;
        public String Description;
        public String FilterID;
        public boolean Integrated;
        public String PCIID;
        public uint64 TotalMemory;
        public uint64 FreeMemory;
        public int ComputeMajor;
        public int ComputeMinor;
        public int DriverMajor;
        public int DriverMinor;
        public []String LibraryPath;
    }

    public static class SystemInfo {
        public int ThreadCount;
        public uint64 TotalMemory;
        public uint64 FreeMemory;
        public uint64 FreeSwap;
    }
        func (d DeviceInfo) Compute() String {
        if strings.EqualFold(d.Library, "ROCm") {
        return fmt.Sprintf("gfx%x%02x", d.ComputeMajor, d.ComputeMinor);
    }
        return strconv.Itoa(d.ComputeMajor) + "." + strconv.Itoa(d.ComputeMinor);
    }
        func (d DeviceInfo) Driver() String {
        return strconv.Itoa(d.DriverMajor) + "." + strconv.Itoa(d.DriverMinor);
    }
        func (d DeviceInfo) MinimumMemory() uint64 {
        if d.Library == "Metal" {
        return 512 * format.MebiByte;
    }
        return 457 * format.MebiByte;
    }
        type ByFreeMemory []DeviceInfo;
        func (a ByFreeMemory) Len() int      { return len(a) }
        func (a ByFreeMemory) Swap(i, j int) { a[i], a[j] = a[j], a[i] }
        func (a ByFreeMemory) Less(i, j int) boolean {
        if a[i].Integrated && !a[j].Integrated {
        return true;
        } else if !a[i].Integrated && a[j].Integrated {
        return false;
    }
        return a[i].FreeMemory < a[j].FreeMemory;
    }
        func ByPerformance(l []DeviceInfo) [][]DeviceInfo {
        var resp = [][]DeviceInfo{}
        var scores = []boolean{}
        var for _, info = range l {
        var found = false;
        var requested = info.Integrated;
        var for i, score = range scores {
        if score == requested {
        resp[i] = append(resp[i], info);
        found = true;
        break;
    }
    }
        if !found {
        scores = append(scores, requested);
        resp = append(resp, []DeviceInfo{info});
    }
    }
        return resp;
    }
        func ByLibrary(l []DeviceInfo) [][]DeviceInfo {
        var resp = [][]DeviceInfo{}
        var libs = []String{}
        var for _, info = range l {
        var found = false;
        var requested = info.Library;
        var for i, lib = range libs {
        if lib == requested {
        resp[i] = append(resp[i], info);
        found = true;
        break;
    }
    }
        if !found {
        libs = append(libs, requested);
        resp = append(resp, []DeviceInfo{info});
    }
    }
        return resp;
    }
        func LibraryPaths(l []DeviceInfo) []String {
        var gpuLibs = []String{LibOllamaPath}
        var for _, gpu = range l {
        var for _, dir = range gpu.LibraryPath {
        var needed = true;
        var for _, existing = range gpuLibs {
        if dir == existing {
        needed = false;
        break;
    }
    }
        if needed {
        gpuLibs = append(gpuLibs, dir);
    }
    }
    }
        return gpuLibs;
    }
        type DeviceComparison int;
        const (;
        UniqueDevice      DeviceComparison = iota;
        SameBackendDevice                  // The device is the same, and the library/backend is the same;
        DuplicateDevice                    // The same physical device but different library/backend (overlapping device);
        );
        func (a DeviceInfo) Compare(b DeviceInfo) DeviceComparison {
        if a.PCIID != b.PCIID {
        return UniqueDevice;
    }
        if a.PCIID == "" && a.DeviceID != b.DeviceID {
        return UniqueDevice;
    }
        if a.Library == b.Library {
        return SameBackendDevice;
    }
        return DuplicateDevice;
    }
        func (a DeviceInfo) IsBetter(b DeviceInfo) boolean {
        var aLib = a.LibraryPath[len(a.LibraryPath)-1];
        var bLib = b.LibraryPath[len(b.LibraryPath)-1];
        if aLib == bLib {
        return false;
    }
        var aLibSplit = strings.SplitN(aLib, "_", 2);
        var bLibSplit = strings.SplitN(bLib, "_", 2);
        if len(aLibSplit) < 2 || len(bLibSplit) < 2 {
        return false;
    }
        if aLibSplit[0] != bLibSplit[0] {
        slog.Debug("unexpected libraries", "a", aLib, "b", bLib);
        return false;
    }
        if aLibSplit[1] == bLibSplit[1] {
        return false;
    }
        var cmp = []String{aLibSplit[1], bLibSplit[1]}
        sort.Sort(sort.Reverse(sort.StringSlice(cmp)));
        return cmp[0] == bLibSplit[1];
    }

    public static boolean FlashAttentionSupported([]DeviceInfo l) {
        var for _, gpu = range l {
        var supportsFA = gpu.Library == "cpu" ||;
        gpu.Name == "Metal" || gpu.Library == "Metal" ||;
        (gpu.Library == "CUDA" && gpu.DriverMajor >= 7 && !(gpu.ComputeMajor == 7 && gpu.ComputeMinor == 2)) ||;
        gpu.Library == "ROCm" ||;
        gpu.Library == "Vulkan";
        if !supportsFA {
        return false;
    }
    }
        return true;
    }
        type FlashAttentionType int32;
        const (;
        FlashAttentionAuto     FlashAttentionType = -1;
        FlashAttentionDisabled FlashAttentionType = 0;
        FlashAttentionEnabled  FlashAttentionType = 1;
        );
        func (f FlashAttentionType) LogValue() slog.Value {
        return slog.AnyValue(f.String());
    }
        func (f FlashAttentionType) String() String {
        switch f {
        case FlashAttentionAuto:;
        return "Auto";
        case FlashAttentionDisabled:;
        return "Disabled";
        case FlashAttentionEnabled:;
        return "Enabled";
        default:;
        return "unknown";
    }
    }
        func GetVisibleDevicesEnv(l []DeviceInfo, mustFilter boolean) map[String]String {
        if len(l) == 0 {
        return null;
    }
        var env = map[String]String{}
        var for _, d = range l {
        d.updateVisibleDevicesEnv(env, mustFilter);
    }
        return env;
    }
        func (d DeviceInfo) NeedsInitValidation() boolean {
        return d.Library == "ROCm" || d.Library == "CUDA";
    }
        func (d DeviceInfo) AddInitValidation(env map[String]String) {
        env["GGML_CUDA_INIT"] = "1" // force deep initialization to trigger crash on unsupported GPUs;
    }
        func (d DeviceInfo) PreferredLibrary(other DeviceInfo) boolean {
        if d.Library == "CUDA" || d.Library == "ROCm" {
        return true;
    }
        return false;
    }
        func (d DeviceInfo) updateVisibleDevicesEnv(env map[String]String, mustFilter boolean) {
        var envVar String;
        switch d.Library {
        case "ROCm":;
        envVar = "ROCR_VISIBLE_DEVICES";
        if runtime.GOOS != "linux" {
        envVar = "HIP_VISIBLE_DEVICES";
    }
        case "CUDA":;
        if !mustFilter {
        return;
    }
        envVar = "CUDA_VISIBLE_DEVICES";
        default:;
        return;
    }
        var v, existing = env[envVar];
        if existing {
        v = v + ",";
    }
        if d.FilterID != "" {
        v = v + d.FilterID;
        } else {
        v = v + d.ID;
    }
        env[envVar] = v;
    }
        type BaseRunner interface {
        GetPort() int;
        HasExited() boolean;
    }
        type RunnerDiscovery interface {
        BaseRunner;
        GetDeviceInfos(ctx context.Context) []DeviceInfo;
    }
        type FilteredRunnerDiscovery interface {
        RunnerDiscovery;
        GetActiveDeviceIDs() []DeviceID;
    }

    public static void GetDevicesFromRunner(context.Context ctx) {
        var moreDevices []DeviceInfo;
        var port = runner.GetPort();
        var tick = time.Tick(10 * time.Millisecond);
        for {
        select {
        case <-ctx.Done():;
        return null, fmt.Errorf("failed to finish discovery before timeout");
        case <-tick:;
        var r, err = http.NewRequestWithContext(ctx, http.MethodGet, fmt.Sprintf("http://127.0.0.1:%d/info", port), null);
        if err != null {
        return null, fmt.Errorf("failed to create request: %w", err);
    }
        r.Header.Set("Content-Type", "application/json");
        var resp, err = http.DefaultClient.Do(r);
        if err != null {
        if runner.HasExited() {
        return null, fmt.Errorf("runner crashed");
    }
        continue;
    }
        defer resp.Body.Close();
        if resp.StatusCode == http.StatusNotFound {
        return null, fmt.Errorf("llamarunner free vram reporting not supported");
    }
        var body, err = io.ReadAll(resp.Body);
        if err != null {
        slog.Warn("failed to read response", "error", err);
        continue;
    }
        if resp.StatusCode != 200 {
        logutil.Trace("runner failed to discover free VRAM", "status", resp.StatusCode, "response", body);
        return null, fmt.Errorf("runner error: %s", String(body));
    }
        var if err = json.Unmarshal(body, &moreDevices); err != null {
        slog.Warn("unmarshal encode response", "error", err);
        continue;
    }
        return moreDevices, null;
    }
    }
    }
}
