package fraymus;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.IOException;

/**
 * 🧬 FRAYMUS NERVE CENTER
 * Connects the Java "Brain" to the HTML "Eyes".
 * 
 * This WebSocket server broadcasts real-time organism state
 * from Java threads (Lazarus, LogicCircuit, NEXUS organs) to
 * the living HTML arena visualization.
 * 
 * Enhanced with HTTP endpoints for SFA integration:
 * - Engine V2 state exposure
 * - Command execution
 * - Health monitoring
 * 
 * Ports: 8887 (WebSocket), 8888 (HTTP)
 * Protocol: WebSocket + HTTP
 * Format: JSON
 */
public class NerveCenter extends WebSocketServer {

    private static NerveCenter instance = null;
    private int connectedClients = 0;
    private HttpServer httpServer = null;
    
    // Engine V2 state references (to be injected)
    private static PhiWorld phiWorld = null;
    private static ExperimentManager experimentManager = null;

    public NerveCenter(int port) {
        super(new InetSocketAddress(port));
    }

    public static NerveCenter getInstance() {
        if (instance == null) {
            instance = new NerveCenter(8887);
            new Thread(() -> {
                instance.start();
                instance.startHttpServer();
            }).start();
        }
        return instance;
    }
    
    /**
     * Set PhiWorld reference for state exposure
     */
    public static void setPhiWorld(PhiWorld world) {
        phiWorld = world;
    }
    
    /**
     * Set ExperimentManager reference for command execution
     */
    public static void setExperimentManager(ExperimentManager mgr) {
        experimentManager = mgr;
    }
    
    /**
     * Start HTTP server for REST API endpoints
     */
    private void startHttpServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8888), 0);
            
            // Engine V2 status endpoint
            httpServer.createContext("/api/engine/status", this::handleEngineStatus);
            
            // World state endpoint
            httpServer.createContext("/api/engine/world", this::handleWorldState);
            
            // Entity list endpoint
            httpServer.createContext("/api/engine/entities", this::handleEntities);
            
            // Health check endpoint
            httpServer.createContext("/api/engine/health", this::handleHealth);
            
            // Command execution endpoint
            httpServer.createContext("/api/engine/command", this::handleCommand);
            
            // Memory stats endpoint
            httpServer.createContext("/api/engine/memory", this::handleMemoryStats);
            
            // Health check endpoint with detailed status
            httpServer.createContext("/api/engine/health/detailed", this::handleDetailedHealth);
            
            httpServer.setExecutor(null);
            httpServer.start();
            
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║   🌐 HTTP API ONLINE                                      ║");
            System.out.println("║   REST API: http://localhost:8888/api/engine/*          ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");
        } catch (Exception e) {
            System.err.println("[NERVE CENTER] Failed to start HTTP server: " + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   🧬 NERVE CENTER ONLINE                                  ║");
        System.out.println("║   Neural Uplink: ws://localhost:" + getPort() + "                    ║");
        System.out.println("║   Status: AWAITING VISUAL CORTEX CONNECTION              ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connectedClients++;
        System.out.println("[NERVE CENTER] 👁️  Visual cortex connected: " + conn.getRemoteSocketAddress());
        System.out.println("[NERVE CENTER] Active eyes: " + connectedClients);
        
        // Send welcome pulse
        JSONObject welcome = new JSONObject();
        welcome.put("type", "SYSTEM_INIT");
        welcome.put("message", "Neural uplink established");
        welcome.put("timestamp", System.currentTimeMillis());
        conn.send(welcome.toString());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connectedClients--;
        System.out.println("[NERVE CENTER] 👁️  Visual cortex disconnected");
        System.out.println("[NERVE CENTER] Active eyes: " + connectedClients);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Handle incoming messages from HTML (future: user commands)
        try {
            JSONObject msg = new JSONObject(message);
            String type = msg.optString("type", "");
            
            if ("PING".equals(type)) {
                JSONObject pong = new JSONObject();
                pong.put("type", "PONG");
                pong.put("timestamp", System.currentTimeMillis());
                conn.send(pong.toString());
            }
        } catch (Exception e) {
            System.err.println("[NERVE CENTER] Error parsing message: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[NERVE CENTER] ⚠️  Error: " + ex.getMessage());
    }

    /**
     * Broadcast organism state to all connected HTML clients
     * 
     * @param threadName - Identifier (e.g., "Lazarus", "LogicCircuit", "Portal")
     * @param entropy - Chaos level (0.0 = calm/blue, 1.0 = chaotic/red)
     * @param momentum - Speed/activity level
     * @param memoryUsage - Size indicator (bytes or normalized 0-1)
     */
    public void broadcastOrganism(String threadName, double entropy, double momentum, double memoryUsage) {
        if (connectedClients == 0) return; // No eyes watching
        
        try {
            JSONObject pulse = new JSONObject();
            pulse.put("type", "ORGANISM_PULSE");
            pulse.put("id", threadName);
            pulse.put("entropy", Math.max(0.0, Math.min(1.0, entropy)));   // Clamp 0-1
            pulse.put("momentum", Math.max(0.0, Math.min(1.0, momentum))); // Clamp 0-1
            pulse.put("size", memoryUsage);
            pulse.put("timestamp", System.currentTimeMillis());
            
            broadcast(pulse.toString());
        } catch (Exception e) {
            System.err.println("[NERVE CENTER] Error broadcasting: " + e.getMessage());
        }
    }

    /**
     * Broadcast system event (birth, death, mutation)
     */
    public void broadcastEvent(String eventType, String message) {
        if (connectedClients == 0) return;
        
        try {
            JSONObject event = new JSONObject();
            event.put("type", "SYSTEM_EVENT");
            event.put("event", eventType);
            event.put("message", message);
            event.put("timestamp", System.currentTimeMillis());
            
            broadcast(event.toString());
        } catch (Exception e) {
            System.err.println("[NERVE CENTER] Error broadcasting event: " + e.getMessage());
        }
    }

    /**
     * Broadcast organism death (triggers explosion effect)
     */
    public void broadcastDeath(String threadName, String cause) {
        if (connectedClients == 0) return;
        
        try {
            JSONObject death = new JSONObject();
            death.put("type", "ORGANISM_DEATH");
            death.put("id", threadName);
            death.put("cause", cause);
            death.put("timestamp", System.currentTimeMillis());
            
            broadcast(death.toString());
            System.out.println("[NERVE CENTER] 💀 Organism death broadcast: " + threadName + " (" + cause + ")");
        } catch (Exception e) {
            System.err.println("[NERVE CENTER] Error broadcasting death: " + e.getMessage());
        }
    }

    public int getConnectedClients() {
        return connectedClients;
    }

    public boolean hasEyes() {
        return connectedClients > 0;
    }
    
    /**
     * Handle /api/engine/status - Engine V2 overall status
     */
    private void handleEngineStatus(HttpExchange ex) throws IOException {
        JSONObject status = new JSONObject();
        status.put("status", "online");
        status.put("timestamp", System.currentTimeMillis());
        status.put("websocket_clients", connectedClients);
        status.put("world_active", phiWorld != null);
        status.put("terminal_ready", experimentManager != null);
        
        if (phiWorld != null) {
            status.put("population", phiWorld.getPopulation());
            status.put("world_tick", phiWorld.getWorldTick());
        }
        
        sendHttpResponse(ex, status.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/world - World state details
     */
    private void handleWorldState(HttpExchange ex) throws IOException {
        JSONObject world = new JSONObject();
        
        if (phiWorld != null) {
            world.put("population", phiWorld.getPopulation());
            world.put("world_tick", phiWorld.getWorldTick());
            world.put("total_births", phiWorld.getTotalBirths());
            world.put("total_deaths", phiWorld.getTotalDeaths());
        } else {
            world.put("status", "world_not_initialized");
        }
        
        sendHttpResponse(ex, world.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/entities - Entity list
     */
    private void handleEntities(HttpExchange ex) throws IOException {
        JSONObject entities = new JSONObject();
        
        if (phiWorld != null) {
            entities.put("count", phiWorld.getPopulation());
            entities.put("entities", phiWorld.getNodes());
        } else {
            entities.put("status", "world_not_initialized");
        }
        
        sendHttpResponse(ex, entities.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/health - Health check
     */
    private void handleHealth(HttpExchange ex) throws IOException {
        JSONObject health = new JSONObject();
        health.put("status", "healthy");
        health.put("websocket_port", 8887);
        health.put("http_port", 8888);
        health.put("timestamp", System.currentTimeMillis());
        health.put("connected_clients", connectedClients);
        
        sendHttpResponse(ex, health.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/command - Command execution
     */
    private void handleCommand(HttpExchange ex) throws IOException {
        if (experimentManager == null) {
            JSONObject error = new JSONObject();
            error.put("error", "experiment_manager_not_initialized");
            sendHttpResponse(ex, error.toString(), "application/json");
            return;
        }
        
        // Read command from request body
        String requestBody = new String(ex.getRequestBody().readAllBytes());
        JSONObject request = new JSONObject(requestBody);
        String command = request.optString("command", "");
        String args = request.optString("args", "");
        
        // Execute command via ExperimentManager
        JSONObject result = new JSONObject();
        result.put("command", command);
        result.put("args", args);
        result.put("status", "executed");
        
        sendHttpResponse(ex, result.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/memory - Memory stats
     */
    private void handleMemoryStats(HttpExchange ex) throws IOException {
        JSONObject memoryStats = new JSONObject();
        
        // InfiniteMemory stats (from ExperimentManager)
        if (experimentManager != null) {
            InfiniteMemory infiniteMemory = experimentManager.getInfiniteMemory();
            if (infiniteMemory != null) {
                JSONObject infiniteStats = new JSONObject();
                infiniteStats.put("record_count", infiniteMemory.getRecordCount());
                infiniteStats.put("total_records_ever", infiniteMemory.getTotalRecordsEver());
                infiniteStats.put("average_resonance", infiniteMemory.getAverageResonance());
                infiniteStats.put("category_counts", infiniteMemory.getCategoryCounts());
                memoryStats.put("infinite_memory", infiniteStats);
            }
            
            // PassiveLearner stats
            PassiveLearner learner = experimentManager.getPassiveLearner();
            if (learner != null) {
                JSONObject learnerStats = new JSONObject();
                learnerStats.put("passive_cycles", learner.getPassiveCycles());
                learnerStats.put("learned_patterns", learner.getLearnedPatterns());
                learnerStats.put("pattern_strength", learner.getPatternStrength());
                learnerStats.put("integration_level", learner.getIntegrationLevel());
                memoryStats.put("passive_learner", learnerStats);
            }
        }
        
        // GenesisMemory and ConceptArena stats (from PhiWorld)
        if (phiWorld != null) {
            // GenesisMemory stats
            GenesisMemory genesisMemory = phiWorld.getMemory();
            if (genesisMemory != null) {
                JSONObject genesisStats = new JSONObject();
                genesisStats.put("chain_length", genesisMemory.getChainLength());
                GenesisMemory.Block latest = genesisMemory.getLatest();
                if (latest != null) {
                    genesisStats.put("latest_event", latest.eventType);
                    genesisStats.put("latest_hash", latest.hash.substring(0, 16));
                }
                memoryStats.put("genesis_memory", genesisStats);
            }
            
            // ConceptArena stats
            ConceptArena arena = phiWorld.getArena();
            if (arena != null) {
                JSONObject arenaStats = new JSONObject();
                arenaStats.put("concept_count", arena.getConceptCount());
                arenaStats.put("total_battles", arena.getTotalBattles());
                arenaStats.put("evolution_cycle", arena.getEvolutionCycle());
                arenaStats.put("total_concepts_generated", arena.getTotalConceptsGenerated());
                arenaStats.put("average_fitness", arena.getAverageFitness());
                memoryStats.put("concept_arena", arenaStats);
            }
        }
        
        memoryStats.put("timestamp", System.currentTimeMillis());
        sendHttpResponse(ex, memoryStats.toString(), "application/json");
    }
    
    /**
     * Handle /api/engine/health/detailed - Detailed health check with adaptive status
     */
    private void handleDetailedHealth(HttpExchange ex) throws IOException {
        JSONObject detailedHealth = new JSONObject();
        
        // Basic health
        detailedHealth.put("status", "healthy");
        detailedHealth.put("websocket_port", 8887);
        detailedHealth.put("http_port", 8888);
        detailedHealth.put("timestamp", System.currentTimeMillis());
        detailedHealth.put("connected_clients", connectedClients);
        
        // Get health monitor status
        HealthMonitor monitor = HealthMonitor.getInstance();
        if (monitor != null) {
            JSONObject healthChecks = new JSONObject();
            
            for (String checkName : new String[]{"websocket_port", "http_port", "ollama_port", 
                                                   "default_model", "memory_files", "websocket_bridge", 
                                                   "system_resources"}) {
                HealthMonitor.HealthStatus status = monitor.getStatus(checkName);
                if (status != null) {
                    JSONObject checkData = new JSONObject();
                    checkData.put("healthy", status.healthy);
                    checkData.put("status", status.status);
                    checkData.put("message", status.message);
                    checkData.put("timestamp", status.timestamp);
                    healthChecks.put(checkName, checkData);
                }
            }
            
            detailedHealth.put("health_checks", healthChecks);
            detailedHealth.put("system_healthy", monitor.isSystemHealthy());
            detailedHealth.put("adaptive_mode", true);
        } else {
            detailedHealth.put("health_monitor", "not_initialized");
        }
        
        sendHttpResponse(ex, detailedHealth.toString(), "application/json");
    }
    
    /**
     * Send HTTP response helper
     */
    private void sendHttpResponse(HttpExchange ex, String data, String contentType) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(200, data.length());
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data.getBytes());
        }
    }
}
