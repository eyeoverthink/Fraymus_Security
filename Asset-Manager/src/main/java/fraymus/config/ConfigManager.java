package fraymus.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized configuration manager for secrets and environment variables
 * Reads from system environment variables with fallback to defaults
 */
public class ConfigManager {
    
    private static ConfigManager instance = null;
    private final Map<String, String> cache;
    
    private ConfigManager() {
        cache = new HashMap<>();
        loadEnvironmentVariables();
    }
    
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * Load all environment variables into cache
     */
    private void loadEnvironmentVariables() {
        // OpenAI API Key
        cache.put("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
        
        // Discord Bot Token
        cache.put("DISCORD_BOT_TOKEN", System.getenv("DISCORD_BOT_TOKEN"));
        
        // Telegram Bot Token
        cache.put("TELEGRAM_BOT_TOKEN", System.getenv("TELEGRAM_BOT_TOKEN"));
        
        // Ollama Endpoint
        cache.put("OLLAMA_ENDPOINT", System.getenv("OLLAMA_ENDPOINT"));
        
        // Custom API endpoints
        cache.put("API_ENDPOINT", System.getenv("API_ENDPOINT"));
        
        // Database connection strings
        cache.put("MONGODB_URI", System.getenv("MONGODB_URI"));
        cache.put("POSTGRES_URI", System.getenv("POSTGRES_URI"));
        
        // Encryption keys
        cache.put("ENCRYPTION_KEY", System.getenv("ENCRYPTION_KEY"));
    }
    
    /**
     * Get a configuration value
     * @param key Configuration key
     * @return Value or null if not set
     */
    public String get(String key) {
        return cache.get(key);
    }
    
    /**
     * Get a configuration value with fallback
     * @param key Configuration key
     * @param defaultValue Default value if not set
     * @return Value or default
     */
    public String get(String key, String defaultValue) {
        String value = cache.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get OpenAI API key
     */
    public String getOpenAIKey() {
        return get("OPENAI_API_KEY");
    }
    
    /**
     * Get Discord bot token
     */
    public String getDiscordToken() {
        return get("DISCORD_BOT_TOKEN");
    }
    
    /**
     * Get Telegram bot token
     */
    public String getTelegramToken() {
        return get("TELEGRAM_BOT_TOKEN");
    }
    
    /**
     * Get Ollama endpoint
     */
    public String getOllamaEndpoint() {
        return get("OLLAMA_ENDPOINT", "http://localhost:11434");
    }
    
    /**
     * Get MongoDB URI
     */
    public String getMongoUri() {
        return get("MONGODB_URI", "mongodb://localhost:27017");
    }
    
    /**
     * Get encryption key
     */
    public String getEncryptionKey() {
        return get("ENCRYPTION_KEY");
    }
    
    /**
     * Check if a configuration value is set
     */
    public boolean isSet(String key) {
        String value = cache.get(key);
        return value != null && !value.isEmpty();
    }
    
    /**
     * Reload environment variables (useful for testing)
     */
    public void reload() {
        cache.clear();
        loadEnvironmentVariables();
    }
    
    /**
     * Print all configuration status (for debugging, not for production)
     */
    public void printStatus() {
        System.out.println("=== CONFIGURATION STATUS ===");
        System.out.println("OPENAI_API_KEY: " + (isSet("OPENAI_API_KEY") ? "SET" : "NOT SET"));
        System.out.println("DISCORD_BOT_TOKEN: " + (isSet("DISCORD_BOT_TOKEN") ? "SET" : "NOT SET"));
        System.out.println("TELEGRAM_BOT_TOKEN: " + (isSet("TELEGRAM_BOT_TOKEN") ? "SET" : "NOT SET"));
        System.out.println("OLLAMA_ENDPOINT: " + getOllamaEndpoint());
        System.out.println("MONGODB_URI: " + (isSet("MONGODB_URI") ? "SET" : "NOT SET"));
        System.out.println("ENCRYPTION_KEY: " + (isSet("ENCRYPTION_KEY") ? "SET" : "NOT SET"));
    }
}
