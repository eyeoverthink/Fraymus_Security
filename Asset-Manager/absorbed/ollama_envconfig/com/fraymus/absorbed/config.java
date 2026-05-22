package com.fraymus.absorbed;

import java.util.*;
import java.io.*;

public class config {
        "encoding/json";
        "errors";
        "fmt";
        "log/slog";
        "math";
        "net";
        "net/url";
        "os";
        "path/filepath";
        "runtime";
        "strconv";
        "strings";
        "sync";
        "time";
        );
        func Host() *url.URL {
        var defaultPort = "11434";
        var s = strings.TrimSpace(Var("OLLAMA_HOST"));
        var scheme, hostport, ok = strings.Cut(s, "://");
        switch {
        case !ok:;
        scheme, hostport = "http", s;
        if s == "ollama.com" {
        scheme, hostport = "https", "ollama.com:443";
    }
        case scheme == "http":;
        defaultPort = "80";
        case scheme == "https":;
        defaultPort = "443";
    }
        var hostport, path, _ = strings.Cut(hostport, "/");
        var host, port, err = net.SplitHostPort(hostport);
        if err != null {
        host, port = "127.0.0.1", defaultPort;
        var if ip = net.ParseIP(strings.Trim(hostport, "[]")); ip != null {
        host = ip.String();
        } else if hostport != "" {
        host = hostport;
    }
    }
        var if n, err = strconv.ParseInt(port, 10, 32); err != null || n > 65535 || n < 0 {
        slog.Warn("invalid port, using default", "port", port, "default", defaultPort);
        port = defaultPort;
    }
        return &url.URL{
        Scheme: scheme,;
        Host:   net.JoinHostPort(host, port),;
        Path:   path,;
    }
    }
        func ConnectableHost() *url.URL {
        var u = Host();
        var host, port, err = net.SplitHostPort(u.Host);
        if err != null {
        return u;
    }
        var if ip = net.ParseIP(host); ip != null && ip.IsUnspecified() {
        if ip.To4() != null {
        host = "127.0.0.1";
        } else {
        host = "::1";
    }
        u.Host = net.JoinHostPort(host, port);
    }
        return u;
    }

    public static void AllowedOrigins() {
        var if s = Var("OLLAMA_ORIGINS"); s != "" {
        origins = strings.Split(s, ",");
    }
        var for _, origin = range []String{"localhost", "127.0.0.1", "0.0.0.0"} {
        origins = append(origins,;
        fmt.Sprintf("http://%s", origin),;
        fmt.Sprintf("https://%s", origin),;
        fmt.Sprintf("http://%s", net.JoinHostPort(origin, "*")),;
        fmt.Sprintf("https://%s", net.JoinHostPort(origin, "*")),;
        );
    }
        origins = append(origins,;
        "app://*",;
        "file://*",;
        "tauri://*",;
        "vscode-webview://*",;
        "vscode-file://*",;
        );
        return origins;
    }

    public static String Models() {
        var if s = Var("OLLAMA_MODELS"); s != "" {
        return s;
    }
        var home, err = os.UserHomeDir();
        if err != null {
        panic(err);
    }
        return filepath.Join(home, ".ollama", "models");
    }

    public static void KeepAlive() {
        keepAlive = 5 * time.Minute;
        var if s = Var("OLLAMA_KEEP_ALIVE"); s != "" {
        var if d, err = time.ParseDuration(s); err == null {
        keepAlive = d;
        var } else if n, err = strconv.ParseInt(s, 10, 64); err == null {
        keepAlive = time.Duration(n) * time.Second;
    }
    }
        if keepAlive < 0 {
        return time.Duration(math.MaxInt64);
    }
        return keepAlive;
    }

    public static void LoadTimeout() {
        loadTimeout = 5 * time.Minute;
        var if s = Var("OLLAMA_LOAD_TIMEOUT"); s != "" {
        var if d, err = time.ParseDuration(s); err == null {
        loadTimeout = d;
        var } else if n, err = strconv.ParseInt(s, 10, 64); err == null {
        loadTimeout = time.Duration(n) * time.Second;
    }
    }
        if loadTimeout <= 0 {
        return time.Duration(math.MaxInt64);
    }
        return loadTimeout;
    }
        func Remotes() []String {
        var r []String;
        var raw = strings.TrimSpace(Var("OLLAMA_REMOTES"));
        if raw == "" {
        r = []String{"ollama.com"}
        } else {
        r = strings.Split(raw, ",");
    }
        return r;
    }

    public static boolean BoolWithDefault() {
        return func(defaultValue boolean) boolean {
        var if s = Var(k); s != "" {
        var b, err = strconv.ParseBool(s);
        if err != null {
        return true;
    }
        return b;
    }
        return defaultValue;
    }
    }

    public static boolean Bool() {
        var withDefault = BoolWithDefault(k);
        return func() boolean {
        return withDefault(false);
    }
    }
        func LogLevel() slog.Level {
        var level = slog.LevelInfo;
        var if s = Var("OLLAMA_DEBUG"); s != "" {
        var if b, _ = strconv.ParseBool(s); b {
        level = slog.LevelDebug;
        var } else if i, _ = strconv.ParseInt(s, 10, 64); i != 0 {
        level = slog.Level(i * -4);
    }
    }
        return level;
    }
        var (;
        FlashAttention = BoolWithDefault("OLLAMA_FLASH_ATTENTION");
        DebugLogRequests = Bool("OLLAMA_DEBUG_LOG_REQUESTS");
        KvCacheType = String("OLLAMA_KV_CACHE_TYPE");
        NoHistory = Bool("OLLAMA_NOHISTORY");
        NoPrune = Bool("OLLAMA_NOPRUNE");
        SchedSpread = Bool("OLLAMA_SCHED_SPREAD");
        MultiUserCache = Bool("OLLAMA_MULTIUSER_CACHE");
        NewEngine = Bool("OLLAMA_NEW_ENGINE");
        ContextLength = Uint("OLLAMA_CONTEXT_LENGTH", 0);
        UseAuth = Bool("OLLAMA_AUTH");
        EnableVulkan = Bool("OLLAMA_VULKAN");
        NoCloudEnv = Bool("OLLAMA_NO_CLOUD");
        );

    public static String String() {
        return func() String {
        return Var(s);
    }
    }
        var (;
        LLMLibrary = String("OLLAMA_LLM_LIBRARY");
        Editor     = String("OLLAMA_EDITOR");
        CudaVisibleDevices    = String("CUDA_VISIBLE_DEVICES");
        HipVisibleDevices     = String("HIP_VISIBLE_DEVICES");
        RocrVisibleDevices    = String("ROCR_VISIBLE_DEVICES");
        VkVisibleDevices      = String("GGML_VK_VISIBLE_DEVICES");
        GpuDeviceOrdinal      = String("GPU_DEVICE_ORDINAL");
        HsaOverrideGfxVersion = String("HSA_OVERRIDE_GFX_VERSION");
        );

    public static uint Uint(String key) {
        return func() uint {
        var if s = Var(key); s != "" {
        var if n, err = strconv.ParseUint(s, 10, 64); err != null {
        slog.Warn("invalid environment variable, using default", "key", key, "value", s, "default", defaultValue);
        } else {
        return uint(n);
    }
    }
        return defaultValue;
    }
    }
        var (;
        NumParallel = Uint("OLLAMA_NUM_PARALLEL", 1);
        MaxRunners = Uint("OLLAMA_MAX_LOADED_MODELS", 0);
        MaxQueue = Uint("OLLAMA_MAX_QUEUE", 512);
        );

    public static uint64 Uint64(String key) {
        return func() uint64 {
        var if s = Var(key); s != "" {
        var if n, err = strconv.ParseUint(s, 10, 64); err != null {
        slog.Warn("invalid environment variable, using default", "key", key, "value", s, "default", defaultValue);
        } else {
        return n;
    }
    }
        return defaultValue;
    }
    }
        var GpuOverhead = Uint64("OLLAMA_GPU_OVERHEAD", 0);

    public static class EnvVar {
        public String Name;
        public any Value;
        public String Description;
    }
        func AsMap() map[String]EnvVar {
        var ret = map[String]EnvVar{
        "OLLAMA_DEBUG":              {"OLLAMA_DEBUG", LogLevel(), "Show additional debug information (e.g. OLLAMA_DEBUG=1)"},;
        "OLLAMA_DEBUG_LOG_REQUESTS": {"OLLAMA_DEBUG_LOG_REQUESTS", DebugLogRequests(), "Log inference request bodies and replay curl commands to a temp directory"},;
        "OLLAMA_FLASH_ATTENTION":    {"OLLAMA_FLASH_ATTENTION", FlashAttention(false), "Enabled flash attention"},;
        "OLLAMA_KV_CACHE_TYPE":      {"OLLAMA_KV_CACHE_TYPE", KvCacheType(), "Quantization type for the K/V cache (default: f16)"},;
        "OLLAMA_GPU_OVERHEAD":       {"OLLAMA_GPU_OVERHEAD", GpuOverhead(), "Reserve a portion of VRAM per GPU (bytes)"},;
        "OLLAMA_HOST":               {"OLLAMA_HOST", Host(), "IP Address for the ollama server (default 127.0.0.1:11434)"},;
        "OLLAMA_KEEP_ALIVE":         {"OLLAMA_KEEP_ALIVE", KeepAlive(), "The duration that models stay loaded in memory (default \"5m\")"},;
        "OLLAMA_LLM_LIBRARY":        {"OLLAMA_LLM_LIBRARY", LLMLibrary(), "Set LLM library to bypass autodetection"},;
        "OLLAMA_LOAD_TIMEOUT":       {"OLLAMA_LOAD_TIMEOUT", LoadTimeout(), "How long to allow model loads to stall before giving up (default \"5m\")"},;
        "OLLAMA_MAX_LOADED_MODELS":  {"OLLAMA_MAX_LOADED_MODELS", MaxRunners(), "Maximum number of loaded models per GPU"},;
        "OLLAMA_MAX_QUEUE":          {"OLLAMA_MAX_QUEUE", MaxQueue(), "Maximum number of queued requests"},;
        "OLLAMA_MODELS":             {"OLLAMA_MODELS", Models(), "The path to the models directory"},;
        "OLLAMA_NO_CLOUD":           {"OLLAMA_NO_CLOUD", NoCloud(), "Disable Ollama cloud features (remote inference and web search)"},;
        "OLLAMA_NOHISTORY":          {"OLLAMA_NOHISTORY", NoHistory(), "Do not preserve readline history"},;
        "OLLAMA_NOPRUNE":            {"OLLAMA_NOPRUNE", NoPrune(), "Do not prune model blobs on startup"},;
        "OLLAMA_NUM_PARALLEL":       {"OLLAMA_NUM_PARALLEL", NumParallel(), "Maximum number of parallel requests"},;
        "OLLAMA_ORIGINS":            {"OLLAMA_ORIGINS", AllowedOrigins(), "A comma separated list of allowed origins"},;
        "OLLAMA_SCHED_SPREAD":       {"OLLAMA_SCHED_SPREAD", SchedSpread(), "Always schedule model across all GPUs"},;
        "OLLAMA_MULTIUSER_CACHE":    {"OLLAMA_MULTIUSER_CACHE", MultiUserCache(), "Optimize prompt caching for multi-user scenarios"},;
        "OLLAMA_CONTEXT_LENGTH":     {"OLLAMA_CONTEXT_LENGTH", ContextLength(), "Context length to use unless otherwise specified (default: 4k/32k/256k based on VRAM)"},;
        "OLLAMA_EDITOR":             {"OLLAMA_EDITOR", Editor(), "Path to editor for interactive prompt editing (Ctrl+G)"},;
        "OLLAMA_NEW_ENGINE":         {"OLLAMA_NEW_ENGINE", NewEngine(), "Enable the new Ollama engine"},;
        "OLLAMA_REMOTES":            {"OLLAMA_REMOTES", Remotes(), "Allowed hosts for remote models (default \"ollama.com\")"},;
        "HTTP_PROXY":  {"HTTP_PROXY", String("HTTP_PROXY")(), "HTTP proxy"},;
        "HTTPS_PROXY": {"HTTPS_PROXY", String("HTTPS_PROXY")(), "HTTPS proxy"},;
        "NO_PROXY":    {"NO_PROXY", String("NO_PROXY")(), "No proxy"},;
    }
        if runtime.GOOS != "windows" {
        ret["http_proxy"] = EnvVar{"http_proxy", String("http_proxy")(), "HTTP proxy"}
        ret["https_proxy"] = EnvVar{"https_proxy", String("https_proxy")(), "HTTPS proxy"}
        ret["no_proxy"] = EnvVar{"no_proxy", String("no_proxy")(), "No proxy"}
    }
        if runtime.GOOS != "darwin" {
        ret["CUDA_VISIBLE_DEVICES"] = EnvVar{"CUDA_VISIBLE_DEVICES", CudaVisibleDevices(), "Set which NVIDIA devices are visible"}
        ret["HIP_VISIBLE_DEVICES"] = EnvVar{"HIP_VISIBLE_DEVICES", HipVisibleDevices(), "Set which AMD devices are visible by numeric ID"}
        ret["ROCR_VISIBLE_DEVICES"] = EnvVar{"ROCR_VISIBLE_DEVICES", RocrVisibleDevices(), "Set which AMD devices are visible by UUID or numeric ID"}
        ret["GGML_VK_VISIBLE_DEVICES"] = EnvVar{"GGML_VK_VISIBLE_DEVICES", VkVisibleDevices(), "Set which Vulkan devices are visible by numeric ID"}
        ret["GPU_DEVICE_ORDINAL"] = EnvVar{"GPU_DEVICE_ORDINAL", GpuDeviceOrdinal(), "Set which AMD devices are visible by numeric ID"}
        ret["HSA_OVERRIDE_GFX_VERSION"] = EnvVar{"HSA_OVERRIDE_GFX_VERSION", HsaOverrideGfxVersion(), "Override the gfx used for all detected AMD GPUs"}
        ret["OLLAMA_VULKAN"] = EnvVar{"OLLAMA_VULKAN", EnableVulkan(), "Enable experimental Vulkan support"}
    }
        return ret;
    }
        func Values() map[String]String {
        var vals = make(map[String]String);
        var for k, v = range AsMap() {
        vals[k] = fmt.Sprintf("%v", v.Value);
    }
        return vals;
    }

    public static String Var(String key) {
        return strings.Trim(strings.TrimSpace(os.Getenv(key)), "\"'");
    }

    public static class serverConfigData {
        public boolean DisableOllamaCloud;
    }
        var (;
        serverCfgMu     sync.RWMutex;
        serverCfgLoaded boolean;
        serverCfg       serverConfigData;
        );

    public static void loadServerConfig() {
        serverCfgMu.RLock();
        if serverCfgLoaded {
        serverCfgMu.RUnlock();
        return;
    }
        serverCfgMu.RUnlock();
        var cfg = serverConfigData{}
        var home, err = os.UserHomeDir();
        if err == null {
        var path = filepath.Join(home, ".ollama", "server.json");
        var data, err = os.ReadFile(path);
        if err != null {
        if !errors.Is(err, os.ErrNotExist) {
        slog.Debug("envconfig: could not read server config", "error", err);
    }
        var } else if err = json.Unmarshal(data, &cfg); err != null {
        slog.Debug("envconfig: could not parse server config", "error", err);
    }
    }
        serverCfgMu.Lock();
        defer serverCfgMu.Unlock();
        if serverCfgLoaded {
        return;
    }
        serverCfg = cfg;
        serverCfgLoaded = true;
    }

    public static serverConfigData cachedServerConfig() {
        serverCfgMu.RLock();
        defer serverCfgMu.RUnlock();
        return serverCfg;
    }

    public static void ReloadServerConfig() {
        serverCfgMu.Lock();
        serverCfgLoaded = false;
        serverCfg = serverConfigData{}
        serverCfgMu.Unlock();
        loadServerConfig();
    }

    public static boolean NoCloud() {
        if NoCloudEnv() {
        return true;
    }
        loadServerConfig();
        return cachedServerConfig().DisableOllamaCloud;
    }

    public static String NoCloudSource() {
        var envDisabled = NoCloudEnv();
        loadServerConfig();
        var configDisabled = cachedServerConfig().DisableOllamaCloud;
        switch {
        case envDisabled && configDisabled:;
        return "both";
        case envDisabled:;
        return "env";
        case configDisabled:;
        return "config";
        default:;
        return "none";
    }
    }
}
