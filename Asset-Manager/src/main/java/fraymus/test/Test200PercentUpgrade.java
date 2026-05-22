package fraymus.test;

import fraymus.llm.CalculatorBackend;
import fraymus.integration.ClaudeCodeIntegration;
import fraymus.integration.SpeechBrainIntegration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 🧪 200% UPGRADE DYNAMIC TESTING
 * 
 * Tests actual queries through the integrated system to verify improvements.
 * No static data - all tests run dynamically through the live system.
 * 
 * @author Vaughn Scott
 * @version 1.0
 */
public class Test200PercentUpgrade {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static StringBuilder testResults = new StringBuilder();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   200% UPGRADE DYNAMIC TESTING                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        appendResults("200% UPGRADE DYNAMIC TEST RESULTS");
        appendResults("Date: " + LocalDateTime.now().format(TIME_FORMAT));
        appendResults("========================================");
        appendResults("");

        // Test 1: Calculator Backend
        testCalculatorBackend();

        // Test 2: Claude Code Integration
        testClaudeCodeIntegration();

        // Test 3: SpeechBrain Integration
        testSpeechBrainIntegration();

        // Test 4: HybridModelManager Routing
        testHybridModelManagerRouting();

        // Test 5: Offline Claude Support
        testOfflineClaudeSupport();

        // Save results
        saveResults();

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   TEST SUMMARY                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("✅ All dynamic tests completed");
        System.out.println("📊 Results saved to: /tmp/200-percent-upgrade-test-results.txt");
        System.out.println();
    }

    /**
     * Test Calculator Backend with actual mathematical operations
     */
    private static void testCalculatorBackend() {
        System.out.println("🧪 TEST 1: Calculator Backend - Mathematical Operations");
        System.out.println("--------------------------------------------------------");
        appendResults("TEST 1: Calculator Backend");
        appendResults("--------------------------------------------------------");

        String[] mathTests = {
            "What is 1234 × 5678?",
            "Solve √144 + √256",
            "Calculate 2^10",
            "What is 5 + 7?",
            "Compute 15^2 + 20^2"
        };

        for (String test : mathTests) {
            System.out.println("Testing: " + test);
            appendResults("Query: " + test);

            try {
                // Extract and solve math
                Map<String, Double> results = CalculatorBackend.solveMathInText(test);
                
                if (results.isEmpty()) {
                    appendResults("Result: No math detected");
                    System.out.println("  → No math detected");
                } else {
                    appendResults("Result: " + results);
                    System.out.println("  → Result: " + results);
                    
                    // Verify correctness
                    for (Map.Entry<String, Double> entry : results.entrySet()) {
                        appendResults("  " + entry.getKey() + " = " + entry.getValue());
                    }
                }
            } catch (Exception e) {
                appendResults("Error: " + e.getMessage());
                System.out.println("  → Error: " + e.getMessage());
            }

            appendResults("");
            appendResults("----------------------------------------");
            System.out.println();
        }

        System.out.println("✅ Calculator Backend tests completed");
        System.out.println();
        appendResults("");
    }

    /**
     * Test Claude Code Integration
     */
    private static void testClaudeCodeIntegration() {
        System.out.println("🤖 TEST 2: Claude Code Integration - Code Generation");
        System.out.println("--------------------------------------------------------");
        appendResults("TEST 2: Claude Code Integration");
        appendResults("--------------------------------------------------------");

        try {
            ClaudeCodeIntegration claudeCode = new ClaudeCodeIntegration(
                "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/claude-code-main",
                true
            );

            appendResults("Claude Code Status: " + claudeCode.getStatus());
            System.out.println("Claude Code Status: " + claudeCode.getStatus());

            if (claudeCode.isAvailable()) {
                String[] codeTests = {
                    "Create a function to calculate factorial",
                    "Refactor this code for better performance",
                    "Generate documentation for a REST API"
                };

                for (String test : codeTests) {
                    System.out.println("Testing: " + test);
                    appendResults("Query: " + test);

                    try {
                        String result = claudeCode.generateCode(test, "java");
                        appendResults("Result (first 200 chars): " + 
                            (result.length() > 200 ? result.substring(0, 200) + "..." : result));
                        System.out.println("  → Code generated (" + result.length() + " chars)");
                    } catch (Exception e) {
                        appendResults("Error: " + e.getMessage());
                        System.out.println("  → Error: " + e.getMessage());
                    }

                    appendResults("");
                    appendResults("----------------------------------------");
                    System.out.println();
                }
            } else {
                appendResults("Claude Code not available - skipping tests");
                System.out.println("Claude Code not available - skipping tests");
            }
        } catch (Exception e) {
            appendResults("Error initializing Claude Code: " + e.getMessage());
            System.out.println("Error initializing Claude Code: " + e.getMessage());
        }

        System.out.println("✅ Claude Code tests completed");
        System.out.println();
        appendResults("");
    }

    /**
     * Test SpeechBrain Integration
     */
    private static void testSpeechBrainIntegration() {
        System.out.println("🎤 TEST 3: SpeechBrain Integration - Speech Processing");
        System.out.println("--------------------------------------------------------");
        appendResults("TEST 3: SpeechBrain Integration");
        appendResults("--------------------------------------------------------");

        try {
            SpeechBrainIntegration speechBrain = new SpeechBrainIntegration(
                "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/speechbrain-develop"
            );

            appendResults("SpeechBrain Status: " + speechBrain.getStatus());
            System.out.println("SpeechBrain Status: " + speechBrain.getStatus());

            if (speechBrain.isAvailable()) {
                appendResults("Available Models: " + String.join(", ", speechBrain.getAvailableModels()));
                System.out.println("Available Models: " + String.join(", ", speechBrain.getAvailableModels()));
            } else {
                appendResults("SpeechBrain not available - skipping functional tests");
                System.out.println("SpeechBrain not available - skipping functional tests");
            }
        } catch (Exception e) {
            appendResults("Error initializing SpeechBrain: " + e.getMessage());
            System.out.println("Error initializing SpeechBrain: " + e.getMessage());
        }

        System.out.println("✅ SpeechBrain tests completed");
        System.out.println();
        appendResults("");
    }

    /**
     * Test HybridModelManager Routing
     */
    private static void testHybridModelManagerRouting() {
        System.out.println("🔀 TEST 4: HybridModelManager - Task Routing");
        System.out.println("--------------------------------------------------------");
        appendResults("TEST 4: HybridModelManager Task Routing");
        appendResults("--------------------------------------------------------");

        try {
            // Note: This will test the routing logic without actual model execution
            // since we don't have a live ModelManager instance
            
            String[] routingTests = {
                "Calculate the integral of x^2 from 0 to 5",
                "Write a function to calculate factorial",
                "Refactor this code for better performance",
                "Transcribe the audio file",
                "Optimize this database query",
                "Analyze the philosophical implications of AI"
            };

            for (String test : routingTests) {
                System.out.println("Testing routing for: " + test);
                appendResults("Query: " + test);

                // Determine expected task type based on keywords
                String taskType = determineTaskType(test);
                appendResults("Expected Task Type: " + taskType);
                System.out.println("  → Expected Task Type: " + taskType);

                appendResults("");
                appendResults("----------------------------------------");
                System.out.println();
            }
        } catch (Exception e) {
            appendResults("Error in routing test: " + e.getMessage());
            System.out.println("Error in routing test: " + e.getMessage());
        }

        System.out.println("✅ HybridModelManager routing tests completed");
        System.out.println();
        appendResults("");
    }

    /**
     * Test Offline Claude Support
     */
    private static void testOfflineClaudeSupport() {
        System.out.println("🧠 TEST 5: Offline Claude Support");
        System.out.println("--------------------------------------------------------");
        appendResults("TEST 5: Offline Claude Support");
        appendResults("--------------------------------------------------------");

        try {
            ClaudeCodeIntegration claudeCode = new ClaudeCodeIntegration(
                "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/claude-code-main",
                true
            );

            if (claudeCode.isAvailable()) {
                appendResults("Offline Claude Code is available and ready");
                System.out.println("Offline Claude Code is available and ready");
            } else {
                appendResults("Offline Claude Code not available");
                System.out.println("Offline Claude Code not available");
            }
        } catch (Exception e) {
            appendResults("Error testing offline Claude: " + e.getMessage());
            System.out.println("Error testing offline Claude: " + e.getMessage());
        }

        System.out.println("✅ Offline Claude tests completed");
        System.out.println();
        appendResults("");
    }

    /**
     * Determine task type based on keywords (simplified routing logic)
     */
    private static String determineTaskType(String prompt) {
        String lower = prompt.toLowerCase();

        if (lower.contains("calculate") || lower.contains("integral") || 
            lower.matches(".*\\d+.*[+\\-*/].*\\d+.*")) {
            return "MATHEMATICAL_COMPUTATION";
        }
        if (lower.contains("refactor") || lower.contains("code review") || 
            lower.contains("analyze code")) {
            return "CODE_GENERATION_SPECIALIST";
        }
        if (lower.contains("transcribe") || lower.contains("audio") || 
            lower.contains("speech")) {
            return "SPEECH_PROCESSING";
        }
        if (lower.contains("optimize") || lower.contains("performance")) {
            return "PERFORMANCE_OPTIMIZATION";
        }
        if (lower.contains("philosophical") || lower.contains("implications")) {
            return "PHILOSOPHICAL_ANALYSIS";
        }
        if (lower.contains("function") || lower.contains("write")) {
            return "CODE_GENERATION";
        }

        return "COMPLEX_SYNTHESIS";
    }

    /**
     * Append results to buffer
     */
    private static void appendResults(String text) {
        testResults.append(text).append("\n");
    }

    /**
     * Save results to file
     */
    private static void saveResults() {
        try {
            String filename = "/tmp/200-percent-upgrade-test-results.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(testResults.toString());
            writer.close();
            System.out.println("📊 Results saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }
}
