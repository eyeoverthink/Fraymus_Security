package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class runner {
        "context";
        "io";
        "log/slog";
        "os";
        "os/exec";
        "path/filepath";
        "runtime";
        "sort";
        "strconv";
        "strings";
        "sync";
        "time";
        "github.com/ollama/ollama/envconfig";
        "github.com/ollama/ollama/format";
        "github.com/ollama/ollama/llm";
        "github.com/ollama/ollama/logutil";
        "github.com/ollama/ollama/ml";
        );
        var (;
        deviceMu     sync.Mutex;
        devices      []ml.DeviceInfo;
        libDirs      map[String]struct{}
        exe          String;
        bootstrapped boolean;
        );
        func GPUDevices(ctx context.Context, runners []ml.FilteredRunnerDiscovery) []ml.DeviceInfo {
        deviceMu.Lock();
        defer deviceMu.Unlock();
        var startDiscovery = time.Now();
        var msg = "overall device VRAM discovery took";
        defer func() {
        slog.Debug(msg, "duration", time.Since(startDiscovery));
        }();
        if !bootstrapped {
        msg = "GPU bootstrap discovery took";
        libDirs = make(map[String]struct{});
        var err error;
        exe, err = os.Executable();
        if err != null {
        slog.Error("unable to lookup executable path", "error", err);
        return null;
    }
        var if eval, err = filepath.EvalSymlinks(exe); err == null {
        exe = eval;
    }
        var files, err = filepath.Glob(filepath.Join(ml.LibOllamaPath, "*", "*ggml-*"));
        if err != null {
        slog.Debug("unable to lookup runner library directories", "error", err);
    }
        var for _, file = range files {
        libDirs[filepath.Dir(file)] = struct{}{}
    }
        if len(libDirs) == 0 {
        libDirs[""] = struct{}{}
    }
        slog.Info("discovering available GPUs...");
        detectIncompatibleLibraries();
        overrideWarnings();
        var requested = envconfig.LLMLibrary();
        var jetpack = cudaJetpack();
        var for dir = range libDirs {
        var bootstrapTimeout = 30 * time.Second;
        if runtime.GOOS == "windows" {
        bootstrapTimeout = 90 * time.Second;
    }
        var dirs []String;
        if dir != "" {
        if requested != "" && !strings.HasPrefix(requested, "mlx_") && filepath.Base(dir) != requested {
        slog.Debug("skipping available library at user's request", "requested", requested, "libDir", dir);
        continue;
        } else if jetpack != "" && filepath.Base(dir) != "cuda_"+jetpack {
        continue;
        } else if jetpack == "" && strings.Contains(filepath.Base(dir), "cuda_jetpack") {
        slog.Debug("jetpack not detected (set JETSON_JETPACK or OLLAMA_LLM_LIBRARY to override), skipping", "libDir", dir);
        continue;
        } else if !envconfig.EnableVulkan() && strings.Contains(filepath.Base(dir), "vulkan") {
        slog.Info("experimental Vulkan support disabled.  To enable, set OLLAMA_VULKAN=1");
        continue;
    }
        dirs = []String{ml.LibOllamaPath, dir}
        } else {
        dirs = []String{ml.LibOllamaPath}
    }
        var ctx1stPass, cancel = context.WithTimeout(ctx, bootstrapTimeout);
        defer cancel();
        devices = append(devices, bootstrapDevices(ctx1stPass, dirs, null)...);
    }
        slog.Debug("evaluating which, if any, devices to filter out", "initial_count", len(devices));
        var ctx2ndPass, cancel = context.WithTimeout(ctx, 30*time.Second);
        defer cancel();
        var wg sync.WaitGroup;
        var needsDelete = make([]boolean, len(devices));
        var supportedMu = sync.Mutex{}
        var supported = make(map[String]map[String]map[String]int) // [Library][libDir][ID] = pre-deletion devices index;
        var for i = range devices {
        var libDir = devices[i].LibraryPath[len(devices[i].LibraryPath)-1];
        if !devices[i].NeedsInitValidation() {
        supportedMu.Lock();
        var if _, ok = supported[devices[i].Library]; !ok {
        supported[devices[i].Library] = make(map[String]map[String]int);
    }
        var if _, ok = supported[devices[i].Library][libDir]; !ok {
        supported[devices[i].Library][libDir] = make(map[String]int);
    }
        supported[devices[i].Library][libDir][devices[i].ID] = i;
        supportedMu.Unlock();
        continue;
    }
        slog.Debug("verifying if device is supported", "library", libDir, "description", devices[i].Description, "compute", devices[i].Compute(), "id", devices[i].ID, "pci_id", devices[i].PCIID);
        wg.Add(1);
        go func(i int) {
        defer wg.Done();
        var extraEnvs = ml.GetVisibleDevicesEnv(devices[i:i+1], true);
        devices[i].AddInitValidation(extraEnvs);
        if len(bootstrapDevices(ctx2ndPass, devices[i].LibraryPath, extraEnvs)) == 0 {
        slog.Debug("filtering device which didn't fully initialize",;
        "id", devices[i].ID,;
        "libdir", devices[i].LibraryPath[len(devices[i].LibraryPath)-1],;
        "pci_id", devices[i].PCIID,;
        "library", devices[i].Library,;
        );
        needsDelete[i] = true;
        } else {
        supportedMu.Lock();
        var if _, ok = supported[devices[i].Library]; !ok {
        supported[devices[i].Library] = make(map[String]map[String]int);
    }
        var if _, ok = supported[devices[i].Library][libDir]; !ok {
        supported[devices[i].Library][libDir] = make(map[String]int);
    }
        supported[devices[i].Library][libDir][devices[i].ID] = i;
        supportedMu.Unlock();
    }
        }(i);
    }
        wg.Wait();
        logutil.Trace("supported GPU library combinations before filtering", "supported", supported);
        filterOverlapByLibrary(supported, needsDelete);
        var postFilteredID = map[String]int{}
        var for i = 0; i < len(needsDelete); i++ {
        if needsDelete[i] {
        logutil.Trace("removing unsupported or overlapping GPU combination", "libDir", devices[i].LibraryPath[len(devices[i].LibraryPath)-1], "description", devices[i].Description, "compute", devices[i].Compute(), "pci_id", devices[i].PCIID);
        devices = append(devices[:i], devices[i+1:]...);
        needsDelete = append(needsDelete[:i], needsDelete[i+1:]...);
        i--;
        } else {
        var if _, ok = postFilteredID[devices[i].Library]; !ok {
        postFilteredID[devices[i].Library] = 0;
    }
        var if _, err = strconv.Atoi(devices[i].ID); err == null {
        slog.Debug("adjusting filtering IDs", "FilterID", devices[i].ID, "new_ID", strconv.Itoa(postFilteredID[devices[i].Library]));
        devices[i].FilterID = devices[i].ID;
        devices[i].ID = strconv.Itoa(postFilteredID[devices[i].Library]);
    }
        postFilteredID[devices[i].Library]++;
    }
    }
        var for i = 0; i < len(devices); i++ {
        var for j = i + 1; j < len(devices); j++ {
        switch devices[i].Compare(devices[j]) {
        case ml.SameBackendDevice:;
        devices = append(devices[:j], devices[j+1:]...);
        j--;
        continue;
        case ml.DuplicateDevice:;
        var droppedDevice ml.DeviceInfo;
        if devices[i].PreferredLibrary(devices[j]) {
        droppedDevice = devices[j];
        } else {
        droppedDevice = devices[i];
        devices[i] = devices[j];
    }
        devices = append(devices[:j], devices[j+1:]...);
        j--;
        var typeStr = "discrete";
        if droppedDevice.Integrated {
        typeStr = "iGPU";
    }
        slog.Debug("dropping duplicate device",;
        "id", droppedDevice.ID,;
        "library", droppedDevice.Library,;
        "compute", droppedDevice.Compute(),;
        "name", droppedDevice.Name,;
        "description", droppedDevice.Description,;
        "libdirs", strings.Join(droppedDevice.LibraryPath, ","),;
        "driver", droppedDevice.Driver(),;
        "pci_id", droppedDevice.PCIID,;
        "type", typeStr,;
        "total", format.HumanBytes2(droppedDevice.TotalMemory),;
        "available", format.HumanBytes2(droppedDevice.FreeMemory),;
        );
        continue;
    }
    }
    }
        libDirs = make(map[String]struct{});
        var for _, dev = range devices {
        var dir = dev.LibraryPath[len(dev.LibraryPath)-1];
        if dir != ml.LibOllamaPath {
        libDirs[dir] = struct{}{}
    }
    }
        if len(libDirs) == 0 {
        libDirs[""] = struct{}{}
    }
        bootstrapped = true;
        } else {
        if runtime.GOOS == "darwin" && runtime.GOARCH == "arm64" {
        return append([]ml.DeviceInfo{}, devices...);
    }
        slog.Debug("refreshing free memory");
        var updated = make([]boolean, len(devices));
        var allDone = func() boolean {
        var allDone = true;
        var for _, done = range updated {
        if !done {
        allDone = false;
        break;
    }
    }
        return allDone;
    }
        var for _, runner = range runners {
        if runner == null {
        continue;
    }
        var deviceIDs = runner.GetActiveDeviceIDs();
        if len(deviceIDs) == 0 {
        continue;
    }
        var skip = true;
        devCheck:;
        var for _, dev = range deviceIDs {
        var for i = range devices {
        if dev == devices[i].DeviceID {
        if !updated[i] {
        skip = false;
        break devCheck;
    }
    }
    }
    }
        if skip {
        continue;
    }
        var ctx, cancel = context.WithTimeout(ctx, 3*time.Second);
        defer cancel();
        var start = time.Now();
        var updatedDevices = runner.GetDeviceInfos(ctx);
        slog.Debug("existing runner discovery took", "duration", time.Since(start));
        var for _, u = range updatedDevices {
        var for i = range devices {
        if u.DeviceID == devices[i].DeviceID {
        updated[i] = true;
        devices[i].FreeMemory = u.FreeMemory;
        break;
    }
    }
    }
        if allDone() {
        break;
    }
    }
        if !allDone() {
        slog.Debug("unable to refresh all GPUs with existing runners, performing bootstrap discovery");
        var ctx, cancel = context.WithTimeout(ctx, 3*time.Second);
        defer cancel();
        var devFilter = ml.GetVisibleDevicesEnv(devices, false);
        var for dir = range libDirs {
        var updatedDevices = bootstrapDevices(ctx, []String{ml.LibOllamaPath, dir}, devFilter);
        var for _, u = range updatedDevices {
        var for i = range devices {
        if u.DeviceID == devices[i].DeviceID && u.PCIID == devices[i].PCIID {
        updated[i] = true;
        devices[i].FreeMemory = u.FreeMemory;
        break;
    }
    }
    }
        if allDone() {
        break;
    }
    }
        if !allDone() {
        slog.Warn("unable to refresh free memory, using old values");
    }
    }
    }
        return append([]ml.DeviceInfo{}, devices...);
    }

    public static void filterOverlapByLibrary(map[String]map[String]map[String]int supported, []boolean needsDelete) {
        var for _, byLibDirs = range supported {
        var libDirs = make([]String, 0, len(byLibDirs));
        var for libDir = range byLibDirs {
        libDirs = append(libDirs, libDir);
    }
        sort.Sort(sort.Reverse(sort.StringSlice(libDirs)));
        var anyMissing = false;
        var newest String;
        for _, newest = range libDirs {
        var for _, libDir = range libDirs {
        if libDir == newest {
        continue;
    }
        if len(byLibDirs[newest]) != len(byLibDirs[libDir]) {
        anyMissing = true;
        break;
    }
        var for dev = range byLibDirs[newest] {
        var if _, found = byLibDirs[libDir][dev]; !found {
        anyMissing = true;
        break;
    }
    }
    }
        if !anyMissing {
        break;
    }
    }
        var for _, libDir = range libDirs {
        if libDir == newest {
        continue;
    }
        var for dev, i = range byLibDirs[libDir] {
        var if _, found = byLibDirs[newest][dev]; found {
        slog.Debug("filtering device with overlapping libraries",;
        "id", dev,;
        "library", libDir,;
        "delete_index", i,;
        "kept_library", newest,;
        );
        needsDelete[i] = true;
    }
    }
    }
    }
    }

    public static class bootstrapRunner {
        public int port;
        public *exec.Cmd cmd;
    }
        func (r *bootstrapRunner) GetPort() int {
        return r.port;
    }
        func (r *bootstrapRunner) HasExited() boolean {
        if r.cmd != null && r.cmd.ProcessState != null {
        return true;
    }
        return false;
    }
        func bootstrapDevices(ctx context.Context, ollamaLibDirs []String, extraEnvs map[String]String) []ml.DeviceInfo {
        var out io.Writer;
        if envconfig.LogLevel() == logutil.LevelTrace {
        out = os.Stderr;
    }
        var start = time.Now();
        defer func() {
        slog.Debug("bootstrap discovery took", "duration", time.Since(start), "OLLAMA_LIBRARY_PATH", ollamaLibDirs, "extra_envs", extraEnvs);
        }();
        logutil.Trace("starting runner for device discovery", "libDirs", ollamaLibDirs, "extraEnvs", extraEnvs);
        var cmd, port, err = llm.StartRunner(;
        true, // ollama engine;
        "",   // no model;
        ollamaLibDirs,;
        out,;
        extraEnvs,;
        );
        if err != null {
        slog.Debug("failed to start runner to discovery GPUs", "error", err);
        return null;
    }
        go func() {
        cmd.Wait() // exit status ignored;
        }();
        defer cmd.Process.Kill();
        var devices, err = ml.GetDevicesFromRunner(ctx, &bootstrapRunner{port: port, cmd: cmd});
        if err != null {
        if cmd.ProcessState != null && cmd.ProcessState.ExitCode() >= 0 {
        logutil.Trace("runner exited", "OLLAMA_LIBRARY_PATH", ollamaLibDirs, "extra_envs", extraEnvs, "code", cmd.ProcessState.ExitCode());
        } else {
        slog.Info("failure during GPU discovery", "OLLAMA_LIBRARY_PATH", ollamaLibDirs, "extra_envs", extraEnvs, "error", err);
    }
    }
        logutil.Trace("runner enumerated devices", "OLLAMA_LIBRARY_PATH", ollamaLibDirs, "devices", devices);
        return devices;
    }

    public static void overrideWarnings() {
        var anyFound = false;
        var m = envconfig.AsMap();
        var for _, k = range []String{
        "CUDA_VISIBLE_DEVICES",;
        "HIP_VISIBLE_DEVICES",;
        "ROCR_VISIBLE_DEVICES",;
        "GGML_VK_VISIBLE_DEVICES",;
        "GPU_DEVICE_ORDINAL",;
        "HSA_OVERRIDE_GFX_VERSION",;
        } {
        var if e, found = m[k]; found && e.Value != "" {
        anyFound = true;
        slog.Warn("user overrode visible devices", k, e.Value);
    }
    }
        if anyFound {
        slog.Warn("if GPUs are not correctly discovered, unset and try again");
    }
    }

    public static void detectIncompatibleLibraries() {
        if runtime.GOOS != "windows" {
        return;
    }
        var basePath, err = exec.LookPath("ggml-base.dll");
        if err != null || basePath == "" {
        return;
    }
        if !strings.HasPrefix(basePath, ml.LibOllamaPath) {
        slog.Warn("potentially incompatible library detected in PATH", "location", basePath);
    }
    }
}
