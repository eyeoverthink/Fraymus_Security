package fraymus.llm;

import fraymus.knowledge.AkashicRecord;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * MODEL HARVESTER: SCRAPE IT. ABSORB IT. RETAIN IT. ADAPT. INCORPORATE.
 *
 * Every free model on Ollama — deepseek, gemma, llama, codellama, phi,
 * mistral, qwen, starcoder — discovered, pulled, registered, absorbed.
 *
 * No manual model switching. The mind uses whatever it needs.
 */
public class ModelHarvester {

    private static final String OLLAMA_HOST = "http://localhost:11434";
    private static final int TIMEOUT_MS = 300000; // 5 min for pulls

    // Free models worth harvesting — add more as they appear
    private static final String[] HARVEST_TARGETS = {
        "deepseek-r1",
        "gemma3",
        "gemma4",
        "llama3",
        "llama3.1",
        "codellama",
        "phi4",
        "mistral",
        "qwen2.5",
        "starcoder2",
        "granite3.2",
        "nomic-embed-text"
    };

    private final ModelManager modelManager;
    private final AkashicRecord akashic;
    private final List<String> localModels = new ArrayList<>();
    private final List<String> registeredModels = new ArrayList<>();
    private final List<String> failedPulls = new ArrayList<>();
    private int discoveredCount = 0;
    private int pulledCount = 0;
    private int registeredCount = 0;
    private int absorbedCount = 0;

    public ModelHarvester(ModelManager modelManager, AkashicRecord akashic) {
        this.modelManager = modelManager;
        this.akashic = akashic;
    }

    // ==========================================
    // DISCOVER: what's already on the machine?
    // ==========================================
    public List<String> discoverLocalModels() {
        localModels.clear();
        try {
            URL url = URI.create(OLLAMA_HOST + "/api/tags").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                String json = readStream(conn.getInputStream());
                // Parse model names from JSON (lightweight, no external deps)
                int idx = 0;
                while ((idx = json.indexOf("\"name\"", idx)) != -1) {
                    int start = json.indexOf("\"", idx + 6) + 1;
                    int end = json.indexOf("\"", start);
                    if (end > start) {
                        String name = json.substring(start, end);
                        localModels.add(name);
                        discoveredCount++;
                    }
                    idx = end + 1;
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("   !! Ollama not reachable: " + e.getMessage());
        }
        return localModels;
    }

    // ==========================================
    // REGISTER: wire every local model into ModelManager
    // ==========================================
    public int registerAllLocal() {
        registeredModels.clear();
        registeredCount = 0;

        for (String fullName : localModels) {
            // Derive short name: "deepseek-r1:latest" -> "deepseek-r1"
            String shortName = fullName.contains(":") ? fullName.substring(0, fullName.indexOf(":")) : fullName;

            // Skip if already registered
            boolean alreadyRegistered = false;
            for (String existing : modelManager.getRegisteredModels()) {
                if (existing.equalsIgnoreCase(shortName) || existing.equalsIgnoreCase(fullName)) {
                    alreadyRegistered = true;
                    break;
                }
            }

            if (!alreadyRegistered) {
                try {
                    OllamaModelAdapter adapter = new OllamaModelAdapter(fullName);
                    modelManager.registerModel(shortName, adapter);
                    registeredModels.add(shortName);
                    registeredCount++;
                } catch (Exception e) {
                    // skip broken model
                }
            }
        }
        return registeredCount;
    }

    // ==========================================
    // PULL: grab models we don't have yet
    // ==========================================
    public boolean pullModel(String modelName) {
        System.out.println("   >> Pulling " + modelName + "...");
        try {
            URL url = URI.create(OLLAMA_HOST + "/api/pull").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);

            String body = "{\"name\":\"" + modelName + "\",\"stream\":false}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                // Read response to completion
                readStream(conn.getInputStream());
                pulledCount++;
                System.out.println("   >> " + modelName + " pulled successfully");
                return true;
            } else {
                failedPulls.add(modelName);
                System.out.println("   !! " + modelName + " pull failed (HTTP " + code + ")");
                return false;
            }
        } catch (Exception e) {
            failedPulls.add(modelName);
            System.out.println("   !! " + modelName + " pull failed: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // ABSORB: feed model knowledge into AkashicRecord
    // ==========================================
    public void absorbModelKnowledge() {
        if (akashic == null) return;

        for (String model : localModels) {
            String shortName = model.contains(":") ? model.substring(0, model.indexOf(":")) : model;

            // Store model as a concept
            akashic.addBlock("MODEL", shortName + " | Local Ollama model available for inference");

            // Categorize by capability
            String capability = classifyModel(shortName);
            akashic.addBlock("MODEL_CAPABILITY", shortName + " -> " + capability);
            absorbedCount++;
        }
    }

    // ==========================================
    // HARVEST: the full pipeline — discover, pull missing, register, absorb
    // ==========================================
    public HarvestReport harvest(boolean pullMissing) {
        long t0 = System.currentTimeMillis();

        // 1. Discover what we have
        discoverLocalModels();

        // 2. Pull missing targets (if requested)
        if (pullMissing) {
            Set<String> localShortNames = new HashSet<>();
            for (String m : localModels) {
                localShortNames.add(m.contains(":") ? m.substring(0, m.indexOf(":")) : m);
            }
            for (String target : HARVEST_TARGETS) {
                if (!localShortNames.contains(target)) {
                    if (pullModel(target)) {
                        localModels.add(target + ":latest");
                    }
                }
            }
        }

        // 3. Register all into ModelManager
        registerAllLocal();

        // 4. Absorb knowledge into AkashicRecord
        absorbModelKnowledge();

        long elapsed = System.currentTimeMillis() - t0;
        return new HarvestReport(discoveredCount, pulledCount, registeredCount,
                absorbedCount, failedPulls.size(), elapsed, localModels, registeredModels);
    }

    // ==========================================
    // SELECT: pick best model for a task type
    // ==========================================
    public String selectModelForTask(String taskDescription) {
        String lower = taskDescription.toLowerCase();

        // Code tasks
        if (lower.contains("code") || lower.contains("function") || lower.contains("program") ||
            lower.contains("debug") || lower.contains("refactor")) {
            return findFirstAvailable("codellama", "starcoder2", "deepseek-r1", "qwen2.5");
        }

        // Math / reasoning
        if (lower.contains("math") || lower.contains("reason") || lower.contains("logic") ||
            lower.contains("proof") || lower.contains("calculate")) {
            return findFirstAvailable("deepseek-r1", "phi4", "qwen2.5", "gemma4");
        }

        // Creative / writing
        if (lower.contains("write") || lower.contains("story") || lower.contains("creative") ||
            lower.contains("poem") || lower.contains("essay")) {
            return findFirstAvailable("gemma4", "llama3.1", "llama3", "mistral");
        }

        // Embedding / search
        if (lower.contains("embed") || lower.contains("similar") || lower.contains("search")) {
            return findFirstAvailable("nomic-embed-text", "gemma4");
        }

        // Default: best general model available
        return findFirstAvailable("deepseek-r1", "gemma4", "llama3.1", "llama3", "mistral");
    }

    private String findFirstAvailable(String... candidates) {
        Set<String> localShort = new HashSet<>();
        for (String m : localModels) {
            localShort.add(m.contains(":") ? m.substring(0, m.indexOf(":")) : m);
        }
        for (String c : candidates) {
            if (localShort.contains(c)) return c;
        }
        // Fall back to whatever's registered
        String[] registered = modelManager.getRegisteredModels();
        return registered.length > 0 ? registered[0] : "default";
    }

    // ==========================================
    // CLASSIFY model by capability
    // ==========================================
    private String classifyModel(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("code") || lower.contains("starcoder")) return "CODE_GENERATION";
        if (lower.contains("deepseek")) return "REASONING_AND_CODE";
        if (lower.contains("gemma")) return "GENERAL_AND_CREATIVE";
        if (lower.contains("llama")) return "GENERAL_PURPOSE";
        if (lower.contains("phi")) return "REASONING";
        if (lower.contains("mistral")) return "GENERAL_AND_INSTRUCTION";
        if (lower.contains("qwen")) return "MULTILINGUAL_AND_CODE";
        if (lower.contains("nomic") || lower.contains("embed")) return "EMBEDDING";
        if (lower.contains("granite")) return "ENTERPRISE";
        return "GENERAL";
    }

    // ==========================================
    // UTIL
    // ==========================================
    private String readStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // ==========================================
    // ACCESSORS
    // ==========================================
    public List<String> getLocalModels() { return Collections.unmodifiableList(localModels); }
    public List<String> getRegisteredModels() { return Collections.unmodifiableList(registeredModels); }
    public int getDiscoveredCount() { return discoveredCount; }
    public int getRegisteredCount() { return registeredCount; }

    public String getStatus() {
        return String.format("ModelHarvester: %d discovered, %d registered, %d absorbed, %d failed",
            discoveredCount, registeredCount, absorbedCount, failedPulls.size());
    }

    // ==========================================
    // REPORT
    // ==========================================
    public static class HarvestReport {
        public final int discovered;
        public final int pulled;
        public final int registered;
        public final int absorbed;
        public final int failed;
        public final long elapsedMs;
        public final List<String> localModels;
        public final List<String> newlyRegistered;

        public HarvestReport(int discovered, int pulled, int registered, int absorbed,
                             int failed, long elapsedMs,
                             List<String> localModels, List<String> newlyRegistered) {
            this.discovered = discovered;
            this.pulled = pulled;
            this.registered = registered;
            this.absorbed = absorbed;
            this.failed = failed;
            this.elapsedMs = elapsedMs;
            this.localModels = localModels;
            this.newlyRegistered = newlyRegistered;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Harvest complete (").append(elapsedMs).append("ms)\n");
            sb.append("  Discovered: ").append(discovered).append(" local models\n");
            if (pulled > 0) sb.append("  Pulled: ").append(pulled).append(" new models\n");
            sb.append("  Registered: ").append(registered).append(" into ModelManager\n");
            sb.append("  Absorbed: ").append(absorbed).append(" model records into AkashicRecord\n");
            if (failed > 0) sb.append("  Failed: ").append(failed).append(" pulls\n");
            sb.append("  Available models:");
            for (String m : localModels) {
                String shortName = m.contains(":") ? m.substring(0, m.indexOf(":")) : m;
                sb.append("\n    - ").append(shortName);
            }
            return sb.toString();
        }
    }
}
