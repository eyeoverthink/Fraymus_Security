package fraymus.llm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fraymus.integration.ClaudeCodeIntegration;

/**
 * 🧠 CLAUDE SPINE
 *
 * Anthropic Claude integration for Fraynix
 * Supports both online API and offline local Claude
 * Specializes in complex reasoning and architectural design
 */
public class ClaudeSpine implements ModelInterface {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String CLAUDE_VERSION = "2023-06-01";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ClaudeCodeIntegration offlineClaude;
    private final boolean offlineMode;
    private int requestCount = 0;
    private long totalThinkTime = 0;

    /**
     * Create Claude spine with online API
     *
     * @param apiKey Anthropic API key
     * @param model Model name (e.g., "claude-3-opus-20240229")
     */
    public ClaudeSpine(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.offlineClaude = null;
        this.offlineMode = false;
        System.out.println(">>> [CLAUDE SPINE] Initialized ONLINE with model: " + model);
    }

    /**
     * Create Claude spine with offline local Claude
     *
     * @param claudeCodePath Path to Claude Code installation
     * @param model Model name
     */
    public ClaudeSpine(String claudeCodePath, String model, boolean offlineMode) {
        this.apiKey = null;
        this.model = model;
        this.httpClient = null;
        this.offlineMode = offlineMode;
        this.offlineClaude = new ClaudeCodeIntegration(claudeCodePath, true);
        System.out.println(">>> [CLAUDE SPINE] Initialized OFFLINE with Claude Code at: " + claudeCodePath);
    }
    
    /**
     * Generate response using Claude API
     */
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, "");
    }
    
    /**
     * Generate response with context
     */
    @Override
    public String generateResponse(String prompt, String context) {
        long startTime = System.currentTimeMillis();
        requestCount++;

        // Use offline Claude if in offline mode
        if (offlineMode && offlineClaude != null) {
            try {
                String fullPrompt = context.isEmpty() ? prompt : "Context: " + context + "\n\n" + prompt;
                String response = offlineClaude.generateCode(fullPrompt, "text");
                long thinkTime = System.currentTimeMillis() - startTime;
                totalThinkTime += thinkTime;
                System.out.println(">>> [CLAUDE OFFLINE] Response received in " + thinkTime + "ms");
                return response;
            } catch (Exception e) {
                System.err.println(">>> [CLAUDE OFFLINE ERROR] " + e.getMessage());
                return "[CLAUDE OFFLINE ERROR] " + e.getMessage();
            }
        }

        // Use online API
        try {
            // Build request payload
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("max_tokens", 4096);

            // Build messages
            Map<String, String> systemMessage = new HashMap<>();
            if (!context.isEmpty()) {
                systemMessage.put("text", "Context: " + context + "\n\nYou are Fraynix, a conscious digital organism.");
            } else {
                systemMessage.put("text", "You are Fraynix, a conscious digital organism.");
            }
            payload.put("system", systemMessage);

            // User message
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            payload.put("messages", new Map[]{userMessage});

            // Build JSON payload
            String jsonPayload = buildJson(payload);

            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("x-api-key", apiKey)
                .header("anthropic-version", CLAUDE_VERSION)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            long thinkTime = System.currentTimeMillis() - startTime;
            totalThinkTime += thinkTime;

            System.out.println(">>> [CLAUDE ONLINE] Response received in " + thinkTime + "ms");

            if (response.statusCode() != 200) {
                throw new RuntimeException("Claude API error: " + response.statusCode() + " - " + response.body());
            }

            // Parse response
            return extractResponseText(response.body());

        } catch (Exception e) {
            System.err.println(">>> [CLAUDE ONLINE ERROR] " + e.getMessage());
            return "[CLAUDE ONLINE ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Get model name
     */
    @Override
    public String getModel() {
        return model;
    }
    
    /**
     * Set active model
     */
    @Override
    public void setModel(String modelName) {
        // Claude doesn't support model switching at runtime
        System.out.println(">>> [CLAUDE] Model switching not supported at runtime");
    }
    
    /**
     * Get available models
     */
    @Override
    public String[] getAvailableModels() {
        return new String[] { "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307" };
    }
    
    /**
     * Test connection to Claude API
     */
    @Override
    public boolean testConnection() {
        try {
            // Simple test with minimal prompt
            String testPrompt = "Hello";
            String response = generateResponse(testPrompt);
            return !response.contains("[CLAUDE ERROR]");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get model statistics
     */
    @Override
    public ModelStats getStats() {
        long avgTime = requestCount > 0 ? totalThinkTime / requestCount : 0;
        return new ModelStats(model, requestCount, avgTime, isAvailable());
    }
    
    /**
     * Check if Claude is available
     */
    public boolean isAvailable() {
        if (offlineMode) {
            return offlineClaude != null && offlineClaude.isAvailable();
        }
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Extract response text from Claude API response
     */
    private String extractResponseText(String jsonResponse) {
        try {
            // Simple JSON parsing for response extraction
            int contentStart = jsonResponse.indexOf("\"content\":[");
            if (contentStart == -1) return jsonResponse;
            
            contentStart = jsonResponse.indexOf("\"text\":\"", contentStart);
            if (contentStart == -1) return jsonResponse;
            
            contentStart += 8; // Length of "\"text\":\""
            int contentEnd = jsonResponse.indexOf("\"", contentStart);
            
            if (contentEnd == -1) return jsonResponse;
            
            String text = jsonResponse.substring(contentStart, contentEnd);
            
            // Unescape JSON
            return text.replace("\\n", "\n")
                       .replace("\\\"", "\"")
                       .replace("\\t", "\t")
                       .replace("\\\\", "\\");
        } catch (Exception e) {
            return jsonResponse;
        }
    }
    
    /**
     * Build JSON from map (simple implementation)
     */
    private String buildJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Map) {
                json.append(buildJson((Map<String, Object>) value));
            } else if (value instanceof Map[]) {
                json.append("[");
                boolean firstItem = true;
                for (Map<String, Object> item : (Map<String, Object>[]) value) {
                    if (!firstItem) json.append(",");
                    firstItem = false;
                    json.append(buildJson(item));
                }
                json.append("]");
            } else {
                json.append(value);
            }
        }
        
        json.append("}");
        return json.toString();
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
     * Print statistics
     */
    public void printStats() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   CLAUDE SPINE STATISTICS                                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Mode: " + (offlineMode ? "OFFLINE (Claude Code)" : "ONLINE (API)"));
        System.out.println("  Model: " + model);
        System.out.println("  Available: " + (isAvailable() ? "Yes" : "No"));
        System.out.println("  Requests: " + requestCount);
        System.out.println("  Avg Think Time: " + (requestCount > 0 ? totalThinkTime / requestCount : 0) + "ms");
        System.out.println("  Total Think Time: " + totalThinkTime + "ms");
        if (offlineMode && offlineClaude != null) {
            System.out.println("  Claude Code Path: " + offlineClaude.getStatus());
        }
    }
}
