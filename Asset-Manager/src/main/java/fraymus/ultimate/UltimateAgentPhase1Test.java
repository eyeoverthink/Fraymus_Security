package fraymus.ultimate;

/**
 * Ultimate Agent Phase 1 Test
 * 
 * This class tests the synapse communication between all Phase 1 components:
 * - UltimateCPU (hardware abstraction)
 * - UnifiedStateLattice (state management)
 * - UnifiedSynapse (communication bus)
 * - CorpusCallosumUltimate (enhanced corpus callosum)
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 1.0
 */
public class UltimateAgentPhase1Test {
    
    public static void main(String[] args) {
        System.out.println("=== ULTIMATE AGENT PHASE 1 TEST ===\n");
        
        // Test 1: UltimateCPU
        System.out.println("Test 1: UltimateCPU");
        testUltimateCPU();
        
        // Test 2: UnifiedStateLattice
        System.out.println("\nTest 2: UnifiedStateLattice");
        testUnifiedStateLattice();
        
        // Test 3: UnifiedSynapse
        System.out.println("\nTest 3: UnifiedSynapse");
        testUnifiedSynapse();
        
        // Test 4: CorpusCallosumUltimate
        System.out.println("\nTest 4: CorpusCallosumUltimate");
        testCorpusCallosumUltimate();
        
        // Test 5: Integration Test
        System.out.println("\nTest 5: Integration Test (All Components)");
        testIntegration();
        
        System.out.println("\n=== ALL TESTS COMPLETE ===");
    }
    
    private static void testUltimateCPU() {
        UltimateCPU cpu = new UltimateCPU();
        System.out.println(cpu.getStatusReport());
        
        // Test phi-harmonic allocation
        double kaiAlloc = cpu.getPhiAllocation("KAI");
        double vexAlloc = cpu.getPhiAllocation("VEX");
        System.out.println("KAI allocation: " + kaiAlloc);
        System.out.println("VEX allocation: " + vexAlloc);
        System.out.println("Sum: " + (kaiAlloc + vexAlloc));
    }
    
    private static void testUnifiedStateLattice() {
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        
        // Test node creation and updates
        lattice.updateNode("KAI:thought", "Hello world", 0.9);
        lattice.updateNode("VEX:image", "nebula.png", 0.8);
        lattice.updateNode("AUM:speech", "Hello", 0.7);
        
        System.out.println(lattice.getStatistics());
        
        // Test agency states
        lattice.updateAgencyState("KAI", "current_task", "reasoning");
        lattice.updateAgencyState("VEX", "current_task", "visualization");
        
        System.out.println("KAI state: " + lattice.getAgencyState("KAI").getStates());
        
        // Test snapshot
        UnifiedStateLattice.StateSnapshot snapshot = lattice.createSnapshot("test_snapshot");
        System.out.println("Snapshot created: " + snapshot.getSnapshotId());
    }
    
    private static void testUnifiedSynapse() {
        UnifiedSynapse synapse = new UnifiedSynapse();
        
        // Subscribe agencies
        synapse.subscribe("KAI", UnifiedSynapse.SynapseType.DATA, msg -> {
            System.out.println("KAI received: " + msg.getData());
        });
        
        synapse.subscribe("VEX", UnifiedSynapse.SynapseType.DATA, msg -> {
            System.out.println("VEX received: " + msg.getData());
        });
        
        // Send messages
        synapse.sendMessage("KAI", "VEX", UnifiedSynapse.SynapseType.DATA, "Test data", 8);
        synapse.sendMessage("VEX", "KAI", UnifiedSynapse.SynapseType.DATA, "Response data", 7);
        
        // Check synapse strength
        double strength = synapse.getSynapseStrength("KAI", "VEX");
        System.out.println("Synapse strength KAI->VEX: " + strength);
        
        try {
            Thread.sleep(200); // Wait for message processing
        } catch (InterruptedException e) {}
        
        synapse.shutdown();
    }
    
    private static void testCorpusCallosumUltimate() {
        UnifiedSynapse synapse = new UnifiedSynapse();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        System.out.println(callosum.getStatistics());
        
        // Test activation
        callosum.processActivation("KAI", "reasoning task");
        callosum.processActivation("VEX", "visualization task");
        
        System.out.println("\nAfter activation:");
        System.out.println(callosum.getStatistics());
        
        // Test synchronization
        callosum.synchronizeStates();
        System.out.println("\nAfter synchronization:");
        
        synapse.shutdown();
    }
    
    private static void testIntegration() {
        // Initialize all components
        UltimateCPU cpu = new UltimateCPU();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        UnifiedSynapse synapse = new UnifiedSynapse();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        System.out.println("All components initialized successfully");
        
        // Test resource allocation
        int kaiCores = cpu.allocateCores("KAI");
        long kaiMemory = cpu.allocateMemory("KAI");
        System.out.println("KAI allocated: " + kaiCores + " cores, " + cpu.formatBytes(kaiMemory));
        
        // Test state updates
        lattice.updateNode("KAI:task", "reasoning", 0.9);
        lattice.updateNode("VEX:task", "visualization", 0.8);
        System.out.println("State lattice updated");
        
        // Test synapse communication
        synapse.subscribe("AUM", UnifiedSynapse.SynapseType.DATA, msg -> {
            lattice.updateNode("AUM:received", msg.getData(), 0.8);
        });
        
        synapse.sendMessage("KAI", "AUM", UnifiedSynapse.SynapseType.DATA, "Test message", 9);
        System.out.println("Synapse message sent");
        
        // Test corpus callosum learning
        callosum.processActivation("KAI", "task1");
        callosum.processActivation("VEX", "task2");
        callosum.synchronizeStates();
        System.out.println("Corpus callosum synchronized");
        
        // Wait for processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        
        // Final statistics
        System.out.println("\nFinal Statistics:");
        System.out.println("CPU: " + cpu.getStatusReport());
        System.out.println("Lattice: " + lattice.getStatistics());
        System.out.println("Callosum: " + callosum.getStatistics());
        
        synapse.shutdown();
    }
}
