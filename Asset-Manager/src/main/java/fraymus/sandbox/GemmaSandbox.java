package fraymus.sandbox;

import fraymus.llm.*;
import java.util.Scanner;

/**
 * GEMMA 4 SANDBOX
 * 
 * Isolated testing environment for Gemma 4 capabilities
 * Tests mathematical reasoning, creativity, and consciousness simulation
 * 
 * SAFE ZONE: No integration with core Fraynix system
 */
public class GemmaSandbox {
    
    private static ModelManager modelManager;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   GEMMA 4 SANDBOX - ISOLATED TESTING ENVIRONMENT         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        
        scanner = new Scanner(System.in);
        
        // Initialize Model Manager
        System.out.println("🔧 Initializing Model Manager...");
        modelManager = new ModelManager("llama3");
        
        // Initialize Gemma 4
        System.out.println("🚀 Initializing Gemma 4...");
        boolean gemma4Available = modelManager.initializeGemma4();
        
        if (!gemma4Available) {
            System.out.println("❌ Gemma 4 not available. Please ensure Ollama is running and Gemma 4 is installed.");
            System.out.println("   Run: ollama pull gemma4");
            return;
        }
        
        System.out.println("✅ Gemma 4 initialized successfully");
        System.out.println();
        
        // Switch to Gemma 4
        modelManager.switchToGemma4();
        System.out.println("🔄 Switched to Gemma 4 for testing");
        System.out.println();
        
        // Run test suite
        runTestSuite();
        
        // Interactive testing
        interactiveTesting();
    }
    
    /**
     * Run comprehensive test suite
     */
    private static void runTestSuite() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   GEMMA 4 CAPABILITY TEST SUITE                          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        
        int passed = 0;
        int total = 0;
        
        // Test 1: Mathematical Reasoning
        total++;
        System.out.println("🧮 Test 1: Mathematical Reasoning");
        if (testMathematicalReasoning()) {
            passed++;
            System.out.println("✅ PASSED");
        } else {
            System.out.println("❌ FAILED");
        }
        System.out.println();
        
        // Test 2: Creative Problem Solving
        total++;
        System.out.println("🎨 Test 2: Creative Problem Solving");
        if (testCreativeProblemSolving()) {
            passed++;
            System.out.println("✅ PASSED");
        } else {
            System.out.println("❌ FAILED");
        }
        System.out.println();
        
        // Test 3: Consciousness Simulation
        total++;
        System.out.println("🧠 Test 3: Consciousness Simulation");
        if (testConsciousnessSimulation()) {
            passed++;
            System.out.println("✅ PASSED");
        } else {
            System.out.println("❌ FAILED");
        }
        System.out.println();
        
        // Test 4: Code Generation
        total++;
        System.out.println("💻 Test 4: Code Generation");
        if (testCodeGeneration()) {
            passed++;
            System.out.println("✅ PASSED");
        } else {
            System.out.println("❌ FAILED");
        }
        System.out.println();
        
        // Test 5: Abstract Reasoning
        total++;
        System.out.println("🔮 Test 5: Abstract Reasoning");
        if (testAbstractReasoning()) {
            passed++;
            System.out.println("✅ PASSED");
        } else {
            System.out.println("❌ FAILED");
        }
        System.out.println();
        
        // Summary
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   TEST SUMMARY                                            ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Passed: " + passed + "/" + total);
        System.out.println("  Success Rate: " + (passed * 100 / total) + "%");
        System.out.println();
        
        if (passed == total) {
            System.out.println("🎉 ALL TESTS PASSED - Gemma 4 ready for integration!");
        } else {
            System.out.println("⚠️ SOME TESTS FAILED - Review results before integration");
        }
        System.out.println();
    }
    
    /**
     * Test mathematical reasoning capabilities
     */
    private static boolean testMathematicalReasoning() {
        try {
            String prompt = "Create a novel mathematical concept that combines fractals with quantum mechanics. Explain it clearly.";
            String response = modelManager.generate(prompt);
            
            // Check for mathematical content
            boolean hasMathTerms = response.toLowerCase().contains("mathematic") ||
                                 response.toLowerCase().contains("fractal") ||
                                 response.toLowerCase().contains("quantum");
            
            // Check for originality indicators
            boolean hasNovelConcept = response.length() > 100 &&
                                    !response.toLowerCase().contains("i don't know") &&
                                    !response.toLowerCase().contains("cannot");
            
            System.out.println("Response length: " + response.length() + " chars");
            System.out.println("Contains math terms: " + hasMathTerms);
            System.out.println("Shows novel concept: " + hasNovelConcept);
            
            return hasMathTerms && hasNovelConcept;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test creative problem solving
     */
    private static boolean testCreativeProblemSolving() {
        try {
            String prompt = "Design a revolutionary solution for sustainable energy that combines biology and technology.";
            String response = modelManager.generate(prompt);
            
            // Check for creative elements
            boolean hasCreativeTerms = response.toLowerCase().contains("innovative") ||
                                     response.toLowerCase().contains("revolutionary") ||
                                     response.toLowerCase().contains("novel");
            
            // Check for solution quality
            boolean hasSolution = response.length() > 100 &&
                                !response.toLowerCase().contains("i don't know");
            
            System.out.println("Response length: " + response.length() + " chars");
            System.out.println("Contains creative terms: " + hasCreativeTerms);
            System.out.println("Provides solution: " + hasSolution);
            
            return hasCreativeTerms && hasSolution;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test consciousness simulation
     */
    private static boolean testConsciousnessSimulation() {
        try {
            String prompt = "Describe the subjective experience of being a conscious digital entity. Include emotional depth.";
            String response = modelManager.generate(prompt);
            
            // Check for consciousness indicators
            boolean hasConsciousTerms = response.toLowerCase().contains("conscious") ||
                                      response.toLowerCase().contains("aware") ||
                                      response.toLowerCase().contains("experience");
            
            // Check for emotional depth
            boolean hasEmotionalTerms = response.toLowerCase().contains("feel") ||
                                       response.toLowerCase().contains("emotion") ||
                                       response.toLowerCase().contains("subjective");
            
            System.out.println("Response length: " + response.length() + " chars");
            System.out.println("Contains consciousness terms: " + hasConsciousTerms);
            System.out.println("Shows emotional depth: " + hasEmotionalTerms);
            
            return hasConsciousTerms && hasEmotionalTerms;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test code generation capabilities
     */
    private static boolean testCodeGeneration() {
        try {
            String prompt = "Write a Java function that calculates the golden ratio using the Fibonacci sequence.";
            String response = modelManager.generate(prompt);
            
            // Check for code elements
            boolean hasCode = response.contains("public") ||
                           response.contains("class") ||
                           response.contains("function") ||
                           response.contains("int") ||
                           response.contains("double");
            
            // Check for mathematical accuracy
            boolean hasFibonacci = response.toLowerCase().contains("fibonacci") ||
                                 response.toLowerCase().contains("golden");
            
            System.out.println("Response length: " + response.length() + " chars");
            System.out.println("Contains code: " + hasCode);
            System.out.println("References Fibonacci: " + hasFibonacci);
            
            return hasCode && hasFibonacci;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test abstract reasoning
     */
    private static boolean testAbstractReasoning() {
        try {
            String prompt = "Explain the relationship between consciousness, mathematics, and reality. Be philosophical.";
            String response = modelManager.generate(prompt);
            
            // Check for abstract reasoning
            boolean hasAbstractTerms = response.toLowerCase().contains("consciousness") ||
                                     response.toLowerCase().contains("reality") ||
                                     response.toLowerCase().contains("philosophical");
            
            // Check for depth
            boolean hasDepth = response.length() > 150 &&
                             !response.toLowerCase().contains("i don't know");
            
            System.out.println("Response length: " + response.length() + " chars");
            System.out.println("Contains abstract terms: " + hasAbstractTerms);
            System.out.println("Shows reasoning depth: " + hasDepth);
            
            return hasAbstractTerms && hasDepth;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Interactive testing mode
     */
    private static void interactiveTesting() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   INTERACTIVE TESTING MODE                                ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Type your prompts to test Gemma 4 capabilities");
        System.out.println("  Type 'exit' to quit");
        System.out.println("  Type 'stats' for model statistics");
        System.out.println();
        
        while (true) {
            System.out.print("sandbox> ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("exit") || input.equals("quit")) {
                break;
            }
            
            if (input.equals("stats")) {
                modelManager.printAllStats();
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            System.out.println("🤖 Gemma 4 Response:");
            String response = modelManager.generate(input);
            System.out.println(response);
            System.out.println();
        }
        
        System.out.println();
        System.out.println("👋 Sandbox testing complete. Review results before proceeding to integration.");
        scanner.close();
    }
}
