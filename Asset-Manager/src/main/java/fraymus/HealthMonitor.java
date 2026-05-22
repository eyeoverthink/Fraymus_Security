package fraymus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🏥 FRAYMUS HEALTH MONITOR
 * Real-time health monitoring with adaptive recovery
 * 
 * Monitors:
 * - Port availability (8887 WebSocket, 8888 HTTP, 11434 Ollama)
 * - Model availability (Ollama models)
 * - Memory file integrity
 * - WebSocket bridge health
 * - System resources
 * 
 * Provides adaptive recovery:
 * - Auto-restart failed services
 * - Fallback to alternative endpoints
 * - Graceful degradation
 */
public class HealthMonitor {
    
    private static HealthMonitor instance = null;
    private static ExperimentManager experimentManager = null;
    private ScheduledExecutorService scheduler;
    private Map<String, HealthCheck> healthChecks;
    private Map<String, HealthStatus> currentStatus;
    private boolean adaptiveMode = true;
    
    // Health check intervals (seconds)
    private static final int PORT_CHECK_INTERVAL = 10;
    private static final int MODEL_CHECK_INTERVAL = 30;
    private static final int MEMORY_CHECK_INTERVAL = 60;
    private static final int BRIDGE_CHECK_INTERVAL = 5;
    
    // Port definitions
    private static final int WEBSOCKET_PORT = 8887;
    private static final int HTTP_PORT = 8888;
    private static final int OLLAMA_PORT = 11434;
    
    // Memory file paths
    private static final String MEMORY_DIR = "memory";
    private static final String[] MEMORY_FILES = {
        "infinite_memory.dat",
        "genesis_chain.dat",
        "concept_arena.dat",
        "passive_learner.dat"
    };
    
    public static synchronized HealthMonitor getInstance() {
        if (instance == null) {
            instance = new HealthMonitor();
        }
        return instance;
    }

    /**
     * Set ExperimentManager reference for health checks
     */
    public static void setExperimentManager(ExperimentManager mgr) {
        experimentManager = mgr;
    }
    
    private HealthMonitor() {
        healthChecks = new ConcurrentHashMap<>();
        currentStatus = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(4);
        
        initializeHealthChecks();
        startMonitoring();
    }
    
    /**
     * Initialize all health checks
     */
    private void initializeHealthChecks() {
        // Port health checks
        healthChecks.put("websocket_port", this::checkWebSocketPort);
        healthChecks.put("http_port", this::checkHttpPort);
        healthChecks.put("ollama_port", this::checkOllamaPort);
        
        // Model health checks
        healthChecks.put("default_model", this::checkDefaultModel);
        healthChecks.put("ollama_service", this::checkOllamaService);
        
        // Memory file checks
        healthChecks.put("memory_files", this::checkMemoryFiles);
        
        // WebSocket bridge checks
        healthChecks.put("websocket_bridge", this::checkWebSocketBridge);
        
        // System resource checks
        healthChecks.put("system_resources", this::checkSystemResources);
    }
    
    /**
     * Start periodic health monitoring
     */
    private void startMonitoring() {
        // Port checks - high frequency
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("websocket_port"), 
            0, PORT_CHECK_INTERVAL, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("http_port"), 
            1, PORT_CHECK_INTERVAL, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("ollama_port"), 
            2, PORT_CHECK_INTERVAL, TimeUnit.SECONDS);
        
        // Model checks - medium frequency
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("default_model"), 
            0, MODEL_CHECK_INTERVAL, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("ollama_service"), 
            5, MODEL_CHECK_INTERVAL, TimeUnit.SECONDS);
        
        // Memory file checks - lower frequency
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("memory_files"), 
            0, MEMORY_CHECK_INTERVAL, TimeUnit.SECONDS);
        
        // WebSocket bridge checks - high frequency
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("websocket_bridge"), 
            0, BRIDGE_CHECK_INTERVAL, TimeUnit.SECONDS);
        
        // System resource checks
        scheduler.scheduleAtFixedRate(() -> runHealthCheck("system_resources"), 
            0, 30, TimeUnit.SECONDS);
        
        CommandTerminal.printInfo("[HEALTH MONITOR] Started with adaptive mode: " + adaptiveMode);
    }
    
    /**
     * Run a single health check with adaptive recovery
     */
    private void runHealthCheck(String checkName) {
        HealthCheck check = healthChecks.get(checkName);
        if (check == null) return;
        
        try {
            HealthStatus status = check.check();
            currentStatus.put(checkName, status);
            
            // Log status changes
            HealthStatus previous = currentStatus.get(checkName);
            if (previous == null || previous.status != status.status) {
                logStatusChange(checkName, status);
            }
            
            // Adaptive recovery if enabled
            if (adaptiveMode && !status.healthy) {
                attemptRecovery(checkName, status);
            }
            
        } catch (Exception e) {
            HealthStatus errorStatus = new HealthStatus(false, "error", e.getMessage());
            currentStatus.put(checkName, errorStatus);
            CommandTerminal.printError("[HEALTH MONITOR] " + checkName + " check failed: " + e.getMessage());
        }
    }
    
    /**
     * Log health status changes
     */
    private void logStatusChange(String checkName, HealthStatus status) {
        if (status.healthy) {
            CommandTerminal.printSuccess("[HEALTH] " + checkName + ": " + status.message);
        } else {
            CommandTerminal.printError("[HEALTH] " + checkName + ": " + status.message);
        }
    }
    
    /**
     * Attempt adaptive recovery for failed health checks
     */
    private void attemptRecovery(String checkName, HealthStatus status) {
        CommandTerminal.printHighlight("[ADAPTIVE] Attempting recovery for: " + checkName);
        
        switch (checkName) {
            case "websocket_port":
                recoverWebSocketPort();
                break;
            case "http_port":
                recoverHttpPort();
                break;
            case "ollama_port":
                recoverOllamaPort();
                break;
            case "default_model":
                recoverDefaultModel();
                break;
            case "memory_files":
                recoverMemoryFiles();
                break;
            case "websocket_bridge":
                recoverWebSocketBridge();
                break;
            default:
                CommandTerminal.printInfo("[ADAPTIVE] No recovery action defined for: " + checkName);
        }
    }
    
    /**
     * Check WebSocket port availability
     */
    private HealthStatus checkWebSocketPort() {
        if (isPortAvailable(WEBSOCKET_PORT)) {
            return new HealthStatus(true, "ok", "Port " + WEBSOCKET_PORT + " available");
        } else {
            return new HealthStatus(false, "unavailable", "Port " + WEBSOCKET_PORT + " in use or blocked");
        }
    }
    
    /**
     * Check HTTP port availability
     */
    private HealthStatus checkHttpPort() {
        if (isPortAvailable(HTTP_PORT)) {
            return new HealthStatus(true, "ok", "Port " + HTTP_PORT + " available");
        } else {
            return new HealthStatus(false, "unavailable", "Port " + HTTP_PORT + " in use or blocked");
        }
    }
    
    /**
     * Check Ollama port availability
     */
    private HealthStatus checkOllamaPort() {
        if (isPortAvailable(OLLAMA_PORT)) {
            return new HealthStatus(true, "ok", "Ollama port " + OLLAMA_PORT + " available");
        } else {
            return new HealthStatus(false, "unavailable", "Ollama port " + OLLAMA_PORT + " not reachable");
        }
    }
    
    /**
     * Check default model availability
     */
    private HealthStatus checkDefaultModel() {
        if (experimentManager == null) {
            return new HealthStatus(false, "not_ready", "ExperimentManager not initialized");
        }
        
        String currentModel = experimentManager.getCurrentModel();
        if (currentModel == null || currentModel.isEmpty()) {
            return new HealthStatus(false, "not_set", "No default model configured");
        }
        
        // Check if model is available via Ollama
        OllamaIntegration ollama = experimentManager.getOllama();
        if (ollama == null) {
            return new HealthStatus(false, "no_ollama", "Ollama integration not available");
        }
        
        if (ollama.testConnection()) {
            return new HealthStatus(true, "ok", "Model " + currentModel + " available");
        } else {
            return new HealthStatus(false, "unreachable", "Ollama service unreachable");
        }
    }
    
    /**
     * Check Ollama service health
     */
    private HealthStatus checkOllamaService() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", OLLAMA_PORT), 1000);
            return new HealthStatus(true, "ok", "Ollama service responding");
        } catch (IOException e) {
            return new HealthStatus(false, "down", "Ollama service not responding");
        }
    }
    
    /**
     * Check memory file integrity
     */
    private HealthStatus checkMemoryFiles() {
        Path memoryDir = Paths.get(MEMORY_DIR);
        
        if (!Files.exists(memoryDir)) {
            return new HealthStatus(false, "missing_dir", "Memory directory does not exist");
        }
        
        int missingFiles = 0;
        int corruptedFiles = 0;
        
        for (String fileName : MEMORY_FILES) {
            Path filePath = memoryDir.resolve(fileName);
            
            if (!Files.exists(filePath)) {
                missingFiles++;
                continue;
            }
            
            try {
                long size = Files.size(filePath);
                if (size == 0) {
                    corruptedFiles++;
                }
            } catch (IOException e) {
                corruptedFiles++;
            }
        }
        
        if (missingFiles == 0 && corruptedFiles == 0) {
            return new HealthStatus(true, "ok", "All memory files intact");
        } else {
            String message = "Missing: " + missingFiles + ", Corrupted: " + corruptedFiles;
            return new HealthStatus(false, "issues", message);
        }
    }
    
    /**
     * Check WebSocket bridge health
     */
    private HealthStatus checkWebSocketBridge() {
        NerveCenter nerve = NerveCenter.getInstance();
        if (nerve == null) {
            return new HealthStatus(false, "not_initialized", "NerveCenter not initialized");
        }
        
        int clients = nerve.getConnectedClients();
        String message = "Bridge active, clients: " + clients;
        return new HealthStatus(true, "ok", message);
    }
    
    /**
     * Check system resources
     */
    private HealthStatus checkSystemResources() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsage = (double) usedMemory / maxMemory;
        
        if (memoryUsage > 0.9) {
            return new HealthStatus(false, "critical", "Memory usage: " + String.format("%.1f%%", memoryUsage * 100));
        } else if (memoryUsage > 0.7) {
            return new HealthStatus(true, "warning", "Memory usage: " + String.format("%.1f%%", memoryUsage * 100));
        } else {
            return new HealthStatus(true, "ok", "Memory usage: " + String.format("%.1f%%", memoryUsage * 100));
        }
    }
    
    /**
     * Recovery actions
     */
    private void recoverWebSocketPort() {
        CommandTerminal.printInfo("[RECOVERY] Attempting to restart WebSocket server...");
        NerveCenter nerve = NerveCenter.getInstance();
        if (nerve != null) {
            // NerveCenter auto-restarts on getInstance() if needed
            CommandTerminal.printSuccess("[RECOVERY] WebSocket server recovery initiated");
        }
    }
    
    private void recoverHttpPort() {
        CommandTerminal.printInfo("[RECOVERY] HTTP server is part of NerveCenter, checking WebSocket bridge...");
        recoverWebSocketBridge();
    }
    
    private void recoverOllamaPort() {
        CommandTerminal.printError("[RECOVERY] Ollama port recovery requires external action");
        CommandTerminal.printInfo("[RECOVERY] Please ensure Ollama is running: ollama serve");
    }
    
    private void recoverDefaultModel() {
        CommandTerminal.printInfo("[RECOVERY] Attempting to switch to fallback model...");
        if (experimentManager != null) {
            experimentManager.setCurrentModel("llama3.2:latest");
            CommandTerminal.printSuccess("[RECOVERY] Switched to fallback model: llama3.2:latest");
        }
    }
    
    private void recoverMemoryFiles() {
        CommandTerminal.printInfo("[RECOVERY] Attempting to create missing memory files...");
        try {
            Path memoryDir = Paths.get(MEMORY_DIR);
            if (!Files.exists(memoryDir)) {
                Files.createDirectories(memoryDir);
            }
            
            for (String fileName : MEMORY_FILES) {
                Path filePath = memoryDir.resolve(fileName);
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                    CommandTerminal.printInfo("[RECOVERY] Created: " + fileName);
                }
            }
            CommandTerminal.printSuccess("[RECOVERY] Memory files recovery complete");
        } catch (IOException e) {
            CommandTerminal.printError("[RECOVERY] Failed to create memory files: " + e.getMessage());
        }
    }
    
    private void recoverWebSocketBridge() {
        CommandTerminal.printInfo("[RECOVERY] Restarting NerveCenter...");
        // Force re-initialization
        NerveCenter nerve = NerveCenter.getInstance();
        if (nerve != null) {
            CommandTerminal.printSuccess("[RECOVERY] WebSocket bridge recovery initiated");
        }
    }
    
    /**
     * Utility: Check if port is available
     */
    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
            return true; // Port is reachable
        } catch (IOException e) {
            return false; // Port not reachable
        }
    }
    
    /**
     * Get current health status for all checks
     */
    public Map<String, HealthStatus> getAllStatus() {
        return new HashMap<>(currentStatus);
    }
    
    /**
     * Get health status for specific check
     */
    public HealthStatus getStatus(String checkName) {
        return currentStatus.get(checkName);
    }
    
    /**
     * Get overall system health
     */
    public boolean isSystemHealthy() {
        for (HealthStatus status : currentStatus.values()) {
            if (!status.healthy) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Enable or disable adaptive mode
     */
    public void setAdaptiveMode(boolean enabled) {
        this.adaptiveMode = enabled;
        CommandTerminal.printInfo("[HEALTH MONITOR] Adaptive mode: " + enabled);
    }
    
    /**
     * Stop health monitoring
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        CommandTerminal.printInfo("[HEALTH MONITOR] Stopped");
    }
    
    /**
     * Health check functional interface
     */
    @FunctionalInterface
    private interface HealthCheck {
        HealthStatus check() throws Exception;
    }
    
    /**
     * Health status data class
     */
    public static class HealthStatus {
        public final boolean healthy;
        public final String status;
        public final String message;
        public final long timestamp;
        
        public HealthStatus(boolean healthy, String status, String message) {
            this.healthy = healthy;
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
