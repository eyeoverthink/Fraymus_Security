package fraymus.ultimate;

/**
 * MultiModelOrchestrator Test
 * Tests the intelligent model selection and LLM routing system.
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class MultiModelOrchestratorTest {
    
    public static void main(String[] args) {
        System.out.println("=== MULTIMODEL ORCHESTRATOR TEST ===\n");
        
        // Initialize Phase 1 components
        System.out.println("Initializing components...");
        UltimateCPU cpu = new UltimateCPU();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        UnifiedSynapse synapse = new UnifiedSynapse();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        // Initialize Phase 2 agencies
        System.out.println("Initializing agencies...");
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        AgencyNEX nex = new AgencyNEX(synapse, lattice, callosum, cpu);
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        
        // Initialize Ollama Bridge
        System.out.println("Initializing Ollama Bridge...");
        OllamaBridge ollama = new OllamaBridge();
        System.out.println("Local Ollama Available: " + ollama.isLocalAvailable());
        System.out.println();
        
        // Initialize MultiModelOrchestrator
        System.out.println("Initializing MultiModelOrchestrator...\n");
        MultiModelOrchestrator orchestrator = new MultiModelOrchestrator(synapse, lattice, kai, ollama);
        
        // Test 1: Model Selection
        System.out.println("Test 1: Model Selection");
        System.out.println("Simple text task -> " + orchestrator.selectModel(
            MultiModelOrchestrator.TaskType.TEXT, 
            MultiModelOrchestrator.Complexity.SIMPLE, 
            "Hello world"));
        System.out.println("Complex math task -> " + orchestrator.selectModel(
            MultiModelOrchestrator.TaskType.MATH, 
            MultiModelOrchestrator.Complexity.COMPLEX, 
            "Solve this equation"));
        System.out.println("Vision task -> " + orchestrator.selectModel(
            MultiModelOrchestrator.TaskType.VISION, 
            MultiModelOrchestrator.Complexity.MEDIUM, 
            "Analyze this image"));
        System.out.println();
        
        // Test 2: Hybrid Synthesis
        System.out.println("Test 2: Hybrid Synthesis");
        String synthesisResult = orchestrator.hybridSynthesis(
            "What is the meaning of life?",
            MultiModelOrchestrator.TaskType.PHILOSOPHY,
            MultiModelOrchestrator.Complexity.COMPLEX);
        System.out.println("Hybrid synthesis result: " + synthesisResult);
        System.out.println();
        
        // Test 3: Ollama Statistics
        System.out.println("Test 3: Ollama Statistics");
        System.out.println(orchestrator.getStatistics());
        System.out.println();
        
        // Test 4: Direct Ollama Bridge Test
        System.out.println("Test 4: Direct Ollama Bridge");
        System.out.println("Testing gemma4 model...");
        String gemma4Response = ollama.generateResponse("gemma4", "Say hello", 0.7);
        System.out.println(gemma4Response);
        System.out.println();
        
        System.out.println("Testing llama3 model...");
        String llama3Response = ollama.generateResponse("llama3", "What is 2+2?", 0.5);
        System.out.println(llama3Response);
        System.out.println();
        
        // Test 5: Model Statistics from Ollama
        System.out.println("Test 5: Model Statistics from Ollama");
        for (String model : new String[]{"gemma4", "deepseek-r1", "llama3", "llava", "eyeoverthink/Fraymus"}) {
            OllamaBridge.ModelStats stats = ollama.getModelStats(model);
            if (stats != null) {
                System.out.println(stats);
            }
        }
        System.out.println();
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        aum.shutdown();
        nex.shutdown();
        cor.shutdown();
        synapse.shutdown();
        ollama.shutdown();
        
        System.out.println("\n=== MULTIMODEL ORCHESTRATOR TEST COMPLETE ===");
    }
}
