package fraymus.ultimate;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * OllamaBridge - Custom HTTP Communication with Ollama
 * 
 * Zero-dependency HTTP client for Ollama integration.
 * Lazy initialization: tries local first, falls back to cloud.
 * Supports gemma4, deepseek-r1, llama3, and custom eyeoverthink/Fraymus model.
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class OllamaBridge {
    
    private static final double PHI = 1.618033988749895;
    private static final String LOCAL_HOST = "localhost";
    private static final int LOCAL_PORT = 11434;
    private static final String CLOUD_HOST = "api.ollama.com";
    
    private boolean localAvailable;
    private String currentHost;
    private int currentPort;
    private Map<String, ModelStats> modelStats;
    private ExecutorService executor;
    
    public OllamaBridge() {
        this.localAvailable = false;
        this.currentHost = LOCAL_HOST;
        this.currentPort = LOCAL_PORT;
        this.modelStats = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(4);
        
        // Initialize with phi-harmonic default stats
        modelStats.put("gemma4", new ModelStats("gemma4", PHI, 1000));
        modelStats.put("deepseek-r1", new ModelStats("deepseek-r1", PHI * PHI, 2000));
        modelStats.put("llama3", new ModelStats("llama3", 1.0, 500));
        modelStats.put("llava", new ModelStats("llava", PHI, 1500));
        modelStats.put("eyeoverthink/Fraymus", new ModelStats("eyeoverthink/Fraymus", PHI * PHI * PHI, 800));
        
        checkLocalAvailability();
    }
    
    /**
     * Check if local Ollama is available
     */
    private void checkLocalAvailability() {
        try {
            HttpURLConnection conn = createConnection("http://" + LOCAL_HOST + ":" + LOCAL_PORT + "/api/tags", "GET");
            int responseCode = conn.getResponseCode();
            localAvailable = (responseCode == 200);
            if (localAvailable) {
                currentHost = LOCAL_HOST;
                currentPort = LOCAL_PORT;
            }
            conn.disconnect();
        } catch (Exception e) {
            localAvailable = false;
            currentHost = CLOUD_HOST;
            currentPort = 443;
        }
    }
    
    /**
     * Create HTTP connection (custom implementation, no external dependencies)
     */
    private HttpURLConnection createConnection(String urlStr, String method) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        return conn;
    }
    
    /**
     * Generate response using Ollama model
     */
    public String generateResponse(String model, String prompt, double temperature) {
        long startTime = System.currentTimeMillis();
        
        try {
            String url = String.format("http://%s:%d/api/generate", currentHost, currentPort);
            HttpURLConnection conn = createConnection(url, "POST");
            conn.setDoOutput(true);
            
            // Build JSON payload manually (no external JSON library)
            String payload = buildGeneratePayload(model, prompt, temperature);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            
            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            long latency = System.currentTimeMillis() - startTime;
            updateModelStats(model, latency, true);
            
            return parseResponse(response.toString());
            
        } catch (Exception e) {
            // Fallback to cloud if local fails
            if (localAvailable) {
                localAvailable = false;
                currentHost = CLOUD_HOST;
                currentPort = 443;
                return generateResponse(model, prompt, temperature);
            }
            
            long latency = System.currentTimeMillis() - startTime;
            updateModelStats(model, latency, false);
            
            return "[Ollama Error: " + e.getMessage() + "]";
        }
    }
    
    /**
     * Build JSON payload for generate endpoint (manual JSON construction)
     */
    private String buildGeneratePayload(String model, String prompt, double temperature) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"").append(escapeJson(model)).append("\",");
        sb.append("\"prompt\":\"").append(escapeJson(prompt)).append("\",");
        sb.append("\"stream\":false,");
        sb.append("\"options\":{");
        sb.append("\"temperature\":").append(temperature).append(",");
        sb.append("\"num_predict\":512");
        sb.append("}}");
        return sb.toString();
    }
    
    /**
     * Escape JSON string (manual implementation)
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Parse Ollama response (manual JSON parsing)
     */
    private String parseResponse(String json) {
        // Simple extraction of "response" field
        int responseStart = json.indexOf("\"response\":\"");
        if (responseStart == -1) return json;
        
        responseStart += 12; // Skip "response":"
        int responseEnd = json.indexOf("\"", responseStart);
        if (responseEnd == -1) return json.substring(responseStart);
        
        return json.substring(responseStart, responseEnd);
    }
    
    /**
     * Update model statistics
     */
    private void updateModelStats(String model, long latency, boolean success) {
        ModelStats stats = modelStats.get(model);
        if (stats != null) {
            stats.update(latency, success);
        }
    }
    
    /**
     * Get model statistics
     */
    public ModelStats getModelStats(String model) {
        return modelStats.get(model);
    }
    
    /**
     * Get all model statistics
     */
    public Map<String, ModelStats> getAllModelStats() {
        return new HashMap<>(modelStats);
    }
    
    /**
     * Check if local Ollama is available
     */
    public boolean isLocalAvailable() {
        return localAvailable;
    }
    
    /**
     * Shutdown executor
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    /**
     * Model statistics inner class
     */
    public static class ModelStats {
        private String model;
        private double performanceScore;
        private long avgLatency;
        private long totalRequests;
        private long successfulRequests;
        
        public ModelStats(String model, double performanceScore, long avgLatency) {
            this.model = model;
            this.performanceScore = performanceScore;
            this.avgLatency = avgLatency;
            this.totalRequests = 0;
            this.successfulRequests = 0;
        }
        
        public void update(long latency, boolean success) {
            totalRequests++;
            if (success) {
                successfulRequests++;
                // Exponential moving average for latency
                avgLatency = (long) (0.9 * avgLatency + 0.1 * latency);
                // Update performance score (phi-harmonic)
                double latencyScore = 1.0 / (1.0 + avgLatency / 1000.0);
                performanceScore = 0.9 * performanceScore + 0.1 * latencyScore * PHI;
            }
        }
        
        public String getModel() { return model; }
        public double getPerformanceScore() { return performanceScore; }
        public long getAvgLatency() { return avgLatency; }
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulRequests() { return successfulRequests; }
        public double getSuccessRate() { 
            return totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("%s: Performance=%.3f, Latency=%dms, Success=%.2f%% (%d/%d)",
                              model, performanceScore, avgLatency, getSuccessRate() * 100,
                              successfulRequests, totalRequests);
        }
    }
}
