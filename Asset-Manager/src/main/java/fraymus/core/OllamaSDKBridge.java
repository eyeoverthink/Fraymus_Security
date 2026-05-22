package fraymus.core;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * OLLAMA SDK BRIDGE: OFFICIAL SDK INTEGRATION
 * 
 * Uses the official ollama4j Java SDK for robust LLM integration
 * Currently using HTTP calls as fallback until SDK API is verified
 * 
 * TODO: Upgrade to use io.github.ollama4j.OllamaAPI once dependency is downloaded
 */
public class OllamaSDKBridge {
    
    private static final String OLLAMA_HOST = "http://localhost:11434";
    private static final int TIMEOUT_MS = 120000;
    
    private String model;
    private boolean connected = false;
    private final Gson gson = new Gson();
    
    // Statistics
    private long totalCalls = 0;
    private long successfulCalls = 0;
    private long failedCalls = 0;
    
    public OllamaSDKBridge(String modelName) {
        this.model = modelName;
        System.out.println("   🎤 OLLAMA SDK BRIDGE INITIALIZING...");
        System.out.println("      Model: " + model);
        System.out.println("      Host: " + OLLAMA_HOST);
        
        testConnection();
    }
    
    private void testConnection() {
        try {
            URL url = new URL(OLLAMA_HOST + "/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                connected = true;
                System.out.println("      ✓ Ollama connection verified");
            } else {
                System.out.println("      !! Ollama returned status: " + responseCode);
            }
            conn.disconnect();
            
        } catch (java.net.ConnectException e) {
            System.out.println("      !! Cannot connect to Ollama");
            System.out.println("      Start with: ollama serve");
        } catch (Exception e) {
            System.out.println("      !! Connection test failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate response using Ollama API
     * 
     * @param prompt The prompt to send to the model
     * @return Generated response
     */
    public String generate(String prompt) {
        totalCalls++;
        
        if (!connected) {
            failedCalls++;
            return "[Ollama not connected]";
        }
        
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("prompt", prompt);
            requestBody.addProperty("stream", false);
            
            URL url = new URL(OLLAMA_HOST + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                        if (json.has("response")) {
                            response.append(json.get("response").getAsString());
                        }
                    }
                    successfulCalls++;
                    return response.toString();
                }
            } else {
                failedCalls++;
                return "[HTTP " + responseCode + "]";
            }
            
        } catch (Exception e) {
            failedCalls++;
            System.err.println("Generation failed: " + e.getMessage());
            return "[Error: " + e.getMessage() + "]";
        }
    }
    
    /**
     * Simple chat with system prompt and user message
     * 
     * @param systemPrompt System context
     * @param userPrompt User message
     * @return Assistant's response
     */
    public String speak(String systemPrompt, String userPrompt) {
        String fullPrompt = "System: " + systemPrompt + "\nUser: " + userPrompt;
        return generate(fullPrompt);
    }
    
    /**
     * Get connection status
     * 
     * @return true if connected to Ollama
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get statistics
     * 
     * @return Statistics string
     */
    public String getStats() {
        return String.format("Calls: %d | Success: %d | Failed: %d | Success Rate: %.1f%%",
            totalCalls, successfulCalls, failedCalls,
            totalCalls > 0 ? (successfulCalls * 100.0 / totalCalls) : 0.0);
    }
    
    /**
     * Set the model to use
     * 
     * @param modelName Model name
     */
    public void setModel(String modelName) {
        this.model = modelName;
        System.out.println("Model changed to: " + model);
    }
    
    /**
     * Get current model name
     * 
     * @return Current model name
     */
    public String getModel() {
        return model;
    }
}
