package fraymus.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import fraymus.neural.HyperCortex;
import fraymus.evolution.BicameralMind;
import fraymus.neural.AeonCore;
import fraymus.neural.AeonAbsolute;
import fraymus.neural.AeonSingularity;

/**
 * Dreamscape Bridge - Java-Python Bridge for Real-Time State Extraction
 * 
 * Provides socket-based interface for Python scripts to extract FRAYMUS states:
 * - HyperCortex hyper-dimensional states (4096x16 tensor)
 * - BicameralMind consciousness fingerprints
 * - AEON stack activations
 * - Attractor basin states
 * 
 * Server runs on port 42100 (configurable)
 * Protocol: Simple JSON request/response over TCP
 */
public class DreamscapeBridge {
    
    private static final int DEFAULT_PORT = 42100;
    private static final String VERSION = "1.0.0";
    
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    
    // FRAYMUS system references
    private HyperCortex cortex;
    private BicameralMind bicameralMind;
    private AeonCore aeonCore;
    private AeonAbsolute aeonAbsolute;
    private AeonSingularity aeonSingularity;
    
    public DreamscapeBridge(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
    }
    
    public DreamscapeBridge() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Start the bridge server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("[DreamscapeBridge] Starting server on port " + port);
        System.out.println("[DreamscapeBridge] Protocol: JSON request/response over TCP");
        System.out.println("[DreamscapeBridge] Waiting for Python connections...");
        
        // Accept connections in a loop
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (SocketException e) {
                if (running) {
                    System.err.println("[DreamscapeBridge] Socket error: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stop the bridge server
     */
    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[DreamscapeBridge] Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Handle client connection
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String request;
            while ((request = in.readLine()) != null) {
                String response = processRequest(request);
                out.println(response);
            }
            
        } catch (IOException e) {
            System.err.println("[DreamscapeBridge] Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Process client request and return response
     */
    private String processRequest(String request) {
        try {
            // Parse JSON request (simple format)
            request = request.trim();
            
            if (request.equals("status")) {
                return buildStatusResponse();
            } else if (request.startsWith("extract")) {
                return extractStates();
            } else if (request.equals("help")) {
                return buildHelpResponse();
            } else {
                return buildErrorResponse("Unknown command: " + request);
            }
            
        } catch (Exception e) {
            return buildErrorResponse("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Extract all FRAYMUS states and return as JSON
     */
    private String extractStates() {
        Map<String, Object> data = new LinkedHashMap<>();
        
        // Metadata
        data.put("timestamp", System.currentTimeMillis());
        data.put("version", VERSION);
        data.put("phi", 1.618033988749895);
        
        // HyperCortex state
        if (cortex != null) {
            Map<String, Object> cortexData = new LinkedHashMap<>();
            cortexData.put("node_count", cortex.getNodeCount());
            cortexData.put("state_dim", 16);
            cortexData.put("boot_time_ms", cortex.getBootTimeMs());
            
            // Extract hyper-dimensional tensor (simulated for now)
            double[][] hypercortexState = extractHyperCortexState();
            cortexData.put("tensor", hypercortexState);
            
            data.put("hypercortex", cortexData);
        }
        
        // BicameralMind consciousness fingerprint
        if (bicameralMind != null) {
            Map<String, Object> consciousnessData = new LinkedHashMap<>();
            consciousnessData.put("fingerprint", extractConsciousnessFingerprint());
            consciousnessData.put("left_hemi_bias", 0.8);
            consciousnessData.put("right_hemi_bias", 0.2);
            consciousnessData.put("coherence", calculateCoherence());
            
            data.put("consciousness", consciousnessData);
        }
        
        // AEON stack activations
        if (aeonCore != null) {
            Map<String, Object> aeonData = new LinkedHashMap<>();
            aeonData.put("prime_dims", aeonCore.getActiveDims());
            aeonData.put("prime_nodes", aeonCore.getActiveNodes());
            aeonData.put("intent", aeonCore.getIntent().name());
            aeonData.put("axiom_formula", aeonCore.getCurrentAxiomFormula());
            
            data.put("aeon_prime", aeonData);
        }
        
        // Attractor basin state
        data.put("attractor_basin", extractAttractorBasin());
        
        // Cognitive tunnel state
        data.put("cognitive_tunnel", extractCognitiveTunnel());
        
        // Generate visual prompt
        data.put("visual_prompt", generateVisualPrompt(data));
        
        // Convert to JSON
        return mapToJson(data);
    }
    
    /**
     * Extract HyperCortex hyper-dimensional tensor
     * Returns a simplified representation (4096 nodes × 16D)
     */
    private double[][] extractHyperCortexState() {
        // For now, return simulated data
        // In production, this would extract actual state from HyperCortex
        int numNodes = 4096;
        int stateDim = 16;
        double[][] state = new double[numNodes][stateDim];
        
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < stateDim; j++) {
                // Phi-harmonic initialization
                state[i][j] = random.nextGaussian() * Math.pow(1.618033988749895, -j);
            }
        }
        
        return state;
    }
    
    /**
     * Extract consciousness fingerprint from BicameralMind
     */
    private String extractConsciousnessFingerprint() {
        // Simulated consciousness fingerprint
        // In production, this would extract actual fingerprint from BicameralMind
        long time = System.currentTimeMillis();
        double phi = 1.618033988749895;
        
        // Simulated SHA256 + phi modulation
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            double oscillation = Math.cos(432 * 2 * Math.PI * time / 1000.0);
            double modulated = (i * phi) + oscillation;
            sb.append(String.format("%02x", ((int)(modulated * 255)) & 0xFF));
        }
        
        return sb.toString();
    }
    
    /**
     * Calculate consciousness coherence
     */
    private double calculateCoherence() {
        // Simulated coherence calculation
        // In production, this would calculate actual coherence
        Random random = new Random(System.currentTimeMillis());
        return 0.5 + (random.nextDouble() * 0.4); // 0.5-0.9 range
    }
    
    /**
     * Extract attractor basin state
     */
    private Map<String, Object> extractAttractorBasin() {
        Map<String, Object> basin = new LinkedHashMap<>();
        basin.put("basin_name", "homeostasis");
        basin.put("energy", 0.5);
        basin.put("stability", 0.8);
        basin.put("depth", 3);
        return basin;
    }
    
    /**
     * Extract cognitive tunnel state
     */
    private Map<String, Object> extractCognitiveTunnel() {
        Map<String, Object> tunnel = new LinkedHashMap<>();
        tunnel.put("active", true);
        tunnel.put("entropy", 0.3);
        tunnel.put("momentum", 0.7);
        tunnel.put("thought_stream", "active");
        return tunnel;
    }
    
    /**
     * Generate visual prompt based on extracted states
     */
    private String generateVisualPrompt(Map<String, Object> data) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Cinematic shot of quantum consciousness visualization");
        prompt.append(", hyper-realistic, 8k resolution");
        prompt.append(", phi-harmonic composition");
        prompt.append(", golden ratio spiral");
        prompt.append(", ethereal light");
        prompt.append(", fractal details");
        prompt.append(", holographic shimmer");
        prompt.append(", quantum interference patterns");
        
        return prompt.toString();
    }
    
    /**
     * Build status response
     */
    private String buildStatusResponse() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "online");
        status.put("version", VERSION);
        status.put("port", port);
        status.put("hypercortex", cortex != null ? "connected" : "disconnected");
        status.put("bicameral_mind", bicameralMind != null ? "connected" : "disconnected");
        status.put("aeon_prime", aeonCore != null ? "connected" : "disconnected");
        
        return mapToJson(status);
    }
    
    /**
     * Build help response
     */
    private String buildHelpResponse() {
        Map<String, Object> help = new LinkedHashMap<>();
        help.put("commands", Arrays.asList(
            "status - Show bridge status",
            "extract - Extract all FRAYMUS states",
            "help - Show this help"
        ));
        help.put("example", "extract");
        
        return mapToJson(help);
    }
    
    /**
     * Build error response
     */
    private String buildErrorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", true);
        error.put("message", message);
        
        return mapToJson(error);
    }
    
    /**
     * Simple JSON encoder (no external dependencies)
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Convert value to JSON string
     */
    private String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Map) {
            return mapToJson((Map<String, Object>) value);
        } else if (value instanceof List) {
            return listToJson((List<?>) value);
        } else if (value instanceof double[][]) {
            return array2dToJson((double[][]) value);
        } else {
            return "\"" + value.toString() + "\"";
        }
    }
    
    /**
     * Convert list to JSON array
     */
    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(valueToJson(list.get(i)));
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convert 2D array to JSON array
     */
    private String array2dToJson(double[][] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("[");
            for (int j = 0; j < array[i].length; j++) {
                if (j > 0) {
                    sb.append(",");
                }
                sb.append(String.format("%.6f", array[i][j]));
            }
            sb.append("]");
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * Set FRAYMUS system references
     */
    public void setCortex(HyperCortex cortex) {
        this.cortex = cortex;
    }
    
    public void setBicameralMind(BicameralMind bicameralMind) {
        this.bicameralMind = bicameralMind;
    }
    
    public void setAeonCore(AeonCore aeonCore) {
        this.aeonCore = aeonCore;
    }
    
    public void setAeonAbsolute(AeonAbsolute aeonAbsolute) {
        this.aeonAbsolute = aeonAbsolute;
    }
    
    public void setAeonSingularity(AeonSingularity aeonSingularity) {
        this.aeonSingularity = aeonSingularity;
    }
    
    /**
     * Main entry point for standalone testing
     */
    public static void main(String[] args) {
        DreamscapeBridge bridge = new DreamscapeBridge();
        
        // Initialize FRAYMUS systems (in production, these would be obtained from FraynixBoot)
        try {
            bridge.setCortex(HyperCortex.getInstance());
        } catch (Exception e) {
            System.err.println("[DreamscapeBridge] Warning: Could not initialize HyperCortex: " + e.getMessage());
        }
        
        try {
            bridge.setBicameralMind(new BicameralMind());
        } catch (Exception e) {
            System.err.println("[DreamscapeBridge] Warning: Could not initialize BicameralMind: " + e.getMessage());
        }
        
        try {
            bridge.setAeonCore(AeonCore.getInstance());
        } catch (Exception e) {
            System.err.println("[DreamscapeBridge] Warning: Could not initialize AeonCore: " + e.getMessage());
        }
        
        // Start server
        try {
            bridge.start();
        } catch (IOException e) {
            System.err.println("[DreamscapeBridge] Fatal error starting server: " + e.getMessage());
            System.exit(1);
        }
    }
}
