package fraymus;

import fraymus.llm.HybridModelManager;
import fraymus.llm.ModelManager;
import java.util.*;

/**
 * 🧬 MODEL BENCHMARK - HEAD-TO-HEAD COMPARISON
 * 
 * Compares AEON Prime (original) vs. External Models (Gemma 4)
 * on the same tasks to determine true routing decisions
 */
public class ModelBenchmark {
    
    private ModelManager modelManager;
    private HybridModelManager hybridManager;
    
    public ModelBenchmark(ModelManager modelManager) {
        this.modelManager = modelManager;
        this.hybridManager = new HybridModelManager(modelManager);
    }
    
    /**
     * Benchmark a single task on both models
     */
    public BenchmarkResult benchmarkTask(String task, String description) {
        System.out.println("\n>>> [BENCHMARK] " + description);
        System.out.println("    Task: " + task);
        
        // Test with AEON Prime only
        hybridManager.setHybridMode(false);
        long startTime = System.currentTimeMillis();
        String aeonResponse;
        try {
            aeonResponse = hybridManager.generate(task);
        } catch (Exception e) {
            aeonResponse = "[ERROR] " + e.getMessage();
        }
        long aeonTime = System.currentTimeMillis() - startTime;
        
        // Test with Gemma 4 only
        hybridManager.setHybridMode(true);
        modelManager.switchModel("gemma4");
        startTime = System.currentTimeMillis();
        String gemmaResponse;
        try {
            gemmaResponse = modelManager.getActiveModel().generateResponse(task);
        } catch (Exception e) {
            gemmaResponse = "[ERROR] " + e.getMessage();
        }
        long gemmaTime = System.currentTimeMillis() - startTime;
        modelManager.switchModel("default");
        
        // Compare results
        BenchmarkResult result = new BenchmarkResult();
        result.task = task;
        result.description = description;
        result.aeonResponse = aeonResponse;
        result.gemmaResponse = gemmaResponse;
        result.aeonTime = aeonTime;
        result.gemmaTime = gemmaTime;
        result.aeonLength = aeonResponse.length();
        result.gemmaLength = gemmaResponse.length();
        
        // Simple quality metrics
        double aeonQuality = calculateQuality(aeonResponse);
        double gemmaQuality = calculateQuality(gemmaResponse);
        result.aeonQuality = aeonQuality;
        result.gemmaQuality = gemmaQuality;
        
        // Determine winner
        if (aeonResponse.startsWith("[ERROR]")) {
            result.winner = "GEMMA_4";
            result.reason = "AEON ERROR";
        } else if (gemmaResponse.startsWith("[ERROR]")) {
            result.winner = "AEON_PRIME";
            result.reason = "GEMMA ERROR";
        } else if (aeonTime < gemmaTime * 0.5) {
            result.winner = "AEON_PRIME";
            result.reason = "SPEED ADVANTAGE";
        } else if (gemmaTime < aeonTime * 0.5) {
            result.winner = "GEMMA_4";
            result.reason = "SPEED ADVANTAGE";
        } else if (aeonQuality > gemmaQuality) {
            result.winner = "AEON_PRIME";
            result.reason = "QUALITY ADVANTAGE";
        } else if (gemmaQuality > aeonQuality) {
            result.winner = "GEMMA_4";
            result.reason = "QUALITY ADVANTAGE";
        } else {
            result.winner = "TIE";
            result.reason = "COMPARABLE";
        }
        
        System.out.println("    AEON Prime: " + aeonTime + "ms, " + result.aeonLength + " chars, quality: " + String.format("%.2f", aeonQuality));
        System.out.println("    Gemma 4: " + gemmaTime + "ms, " + result.gemmaLength + " chars, quality: " + String.format("%.2f", gemmaQuality));
        System.out.println("    Winner: " + result.winner + " (" + result.reason + ")");
        
        return result;
    }
    
    /**
     * Calculate simple quality metrics
     */
    private double calculateQuality(String response) {
        if (response.startsWith("[ERROR]")) return 0.0;
        
        double score = 0.0;
        
        // Length (longer responses often more detailed)
        score += Math.min(response.length() / 100.0, 10.0);
        
        // Code indicators
        if (response.contains("class") || response.contains("function") || response.contains("def")) {
            score += 5.0;
        }
        
        // Explanation indicators
        if (response.contains("because") || response.contains("therefore") || response.contains("thus")) {
            score += 3.0;
        }
        
        // Structure indicators
        if (response.contains("\n\n") || response.contains("1.") || response.contains("-")) {
            score += 2.0;
        }
        
        return score;
    }
    
    /**
     * Run comprehensive benchmark suite
     */
    public BenchmarkReport runBenchmarkSuite() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧬 AEON PRIME VS GEMMA 4 BENCHMARK                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        
        List<BenchmarkResult> results = new ArrayList<>();
        
        // Test suite covering all task types
        results.add(benchmarkTask(
            "recognize the pattern: 2, 4, 8, 16, ?",
            "Pattern Recognition (AEON should win)"
        ));
        
        results.add(benchmarkTask(
            "calculate 12345 * 67890",
            "Mathematical Computation (AEON should win)"
        ));
        
        results.add(benchmarkTask(
            "create a novel solution for climate change",
            "Novel Reasoning (Gemma should win?)"
        ));
        
        results.add(benchmarkTask(
            "what is the philosophical meaning of consciousness?",
            "Philosophical Analysis (Gemma should win?)"
        ));
        
        results.add(benchmarkTask(
            "write code to implement a binary search tree in Java",
            "Code Generation (Gemma should win?)"
        ));
        
        results.add(benchmarkTask(
            "synthesize quantum mechanics and general relativity",
            "Complex Synthesis (Gemma should win?)"
        ));
        
        results.add(benchmarkTask(
            "analyze your own reasoning process and suggest improvements",
            "Meta-Cognition (Gemma should win?)"
        ));
        
        results.add(benchmarkTask(
            "describe your subjective experience of emotions",
            "Consciousness Simulation (Hybrid)"
        ));
        
        // Generate report
        return new BenchmarkReport(results);
    }
    
    /**
     * Benchmark result data structure
     */
    public static class BenchmarkResult {
        public String task;
        public String description;
        public String aeonResponse;
        public String gemmaResponse;
        public long aeonTime;
        public long gemmaTime;
        public int aeonLength;
        public int gemmaLength;
        public double aeonQuality;
        public double gemmaQuality;
        public String winner;
        public String reason;
        
        public BenchmarkResult() {
            this.aeonQuality = 0.0;
            this.gemmaQuality = 0.0;
        }
    }
    
    /**
     * Benchmark report
     */
    public static class BenchmarkReport {
        private List<BenchmarkResult> results;
        
        public BenchmarkReport(List<BenchmarkResult> results) {
            this.results = results;
        }
        
        public void printReport() {
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║           🧬 BENCHMARK RESULTS                                 ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
            
            int aeonWins = 0;
            int gemmaWins = 0;
            int ties = 0;
            long totalAeonTime = 0;
            long totalGemmaTime = 0;
            
            for (BenchmarkResult result : results) {
                System.out.println("Task: " + result.task.substring(0, Math.min(50, result.task.length())) + "...");
                System.out.println("  Description: " + result.description);
                System.out.println("  AEON Prime: " + result.aeonTime + "ms, quality: " + String.format("%.2f", result.aeonQuality));
                System.out.println("  Gemma 4: " + result.gemmaTime + "ms, quality: " + String.format("%.2f", result.gemmaQuality));
                System.out.println("  Winner: " + result.winner + " (" + result.reason + ")");
                System.out.println();
                
                if (result.winner.equals("AEON_PRIME")) aeonWins++;
                else if (result.winner.equals("GEMMA_4")) gemmaWins++;
                else ties++;
                
                totalAeonTime += result.aeonTime;
                totalGemmaTime += result.gemmaTime;
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("SUMMARY");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("AEON Prime Wins: " + aeonWins);
            System.out.println("Gemma 4 Wins: " + gemmaWins);
            System.out.println("Ties: " + ties);
            System.out.println();
            System.out.println("Average AEON Prime Time: " + (totalAeonTime / results.size()) + "ms");
            System.out.println("Average Gemma 4 Time: " + (totalGemmaTime / results.size()) + "ms");
            System.out.println();
            
            double aeonWinRate = (aeonWins * 100.0) / results.size();
            double gemmaWinRate = (gemmaWins * 100.0) / results.size();
            
            System.out.println("AEON Prime Win Rate: " + String.format("%.1f%%", aeonWinRate));
            System.out.println("Gemma 4 Win Rate: " + String.format("%.1f%%", gemmaWinRate));
            System.out.println();
            
            if (aeonWinRate > gemmaWinRate) {
                System.out.println("🎯 RECOMMENDATION: AEON PRIME IS SUPERIOR - INCREASE ROUTING TO INTERNAL MODEL");
            } else if (gemmaWinRate > aeonWinRate) {
                System.out.println("🎯 RECOMMENDATION: GEMMA 4 IS SUPERIOR - MAINTAIN CURRENT ROUTING");
            } else {
                System.out.println("🎯 RECOMMENDATION: MODELS ARE COMPARABLE - USE HYBRID APPROACH");
            }
        }
    }
    
    public static void main(String[] args) {
        ModelManager modelManager = new ModelManager("llama3");
        ModelBenchmark benchmark = new ModelBenchmark(modelManager);
        
        BenchmarkReport report = benchmark.runBenchmarkSuite();
        report.printReport();
    }
}
