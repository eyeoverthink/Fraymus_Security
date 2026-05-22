package fraymus.ultimate;

/**
 * Ultimate Agent Phase 2 Test
 * 
 * This class tests the agency integration with synapse system:
 * - Agency base class functionality
 * - All 5 agencies (KAI, VEX, AUM, NEX, COR)
 * - Inter-agency communication via synapse
 * - Corpus callosum integration
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 1.0
 */
public class UltimateAgentPhase2Test {
    
    public static void main(String[] args) {
        System.out.println("=== ULTIMATE AGENT PHASE 2 TEST ===\n");
        
        // Initialize all Phase 1 components
        System.out.println("Initializing Phase 1 components...");
        UltimateCPU cpu = new UltimateCPU();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        UnifiedSynapse synapse = new UnifiedSynapse();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        System.out.println("Phase 1 components initialized.\n");
        
        // Test 1: Agency Creation
        System.out.println("Test 1: Agency Creation");
        testAgencyCreation(synapse, lattice, callosum, cpu);
        
        // Test 2: Agency-Synapse Integration
        System.out.println("\nTest 2: Agency-Synapse Integration");
        testAgencySynapseIntegration(synapse, lattice, callosum, cpu);
        
        // Test 3: Inter-Agency Communication
        System.out.println("\nTest 3: Inter-Agency Communication");
        testInterAgencyCommunication(synapse, lattice, callosum, cpu);
        
        // Test 4: Corpus Callosum Integration
        System.out.println("\nTest 4: Corpus Callosum Integration");
        testCorpusCallosumIntegration(synapse, lattice, callosum, cpu);
        
        // Test 5: Full System Integration
        System.out.println("\nTest 5: Full System Integration");
        testFullSystemIntegration(synapse, lattice, callosum, cpu);
        
        // Cleanup
        synapse.shutdown();
        
        System.out.println("\n=== ALL PHASE 2 TESTS COMPLETE ===");
    }
    
    private static void testAgencyCreation(UnifiedSynapse synapse, UnifiedStateLattice lattice,
                                           CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        // Create all agencies
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        AgencyNEX nex = new AgencyNEX(synapse, lattice, callosum, cpu);
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        
        System.out.println("KAI created: " + kai.getAgencyName());
        System.out.println("VEX created: " + vex.getAgencyName());
        System.out.println("AUM created: " + aum.getAgencyName());
        System.out.println("NEX created: " + nex.getAgencyName());
        System.out.println("COR created: " + cor.getAgencyName());
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        aum.shutdown();
        nex.shutdown();
        cor.shutdown();
    }
    
    private static void testAgencySynapseIntegration(UnifiedSynapse synapse, UnifiedStateLattice lattice,
                                                     CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        
        // Test synapse subscription
        System.out.println("Testing synapse subscription...");
        synapse.sendMessage("KAI", "VEX", UnifiedSynapse.SynapseType.DATA, "Test data", 8);
        
        // Check synapse strength
        double strength = synapse.getSynapseStrength("KAI", "VEX");
        System.out.println("Synapse strength KAI->VEX: " + strength);
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
    }
    
    private static void testInterAgencyCommunication(UnifiedSynapse synapse, UnifiedStateLattice lattice,
                                                      CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        AgencyNEX nex = new AgencyNEX(synapse, lattice, callosum, cpu);
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        
        System.out.println("Testing KAI -> VEX communication...");
        synapse.sendMessage("KAI", "VEX", UnifiedSynapse.SynapseType.DATA, "visual task", 8);
        
        System.out.println("Testing KAI -> AUM communication...");
        synapse.sendMessage("KAI", "AUM", UnifiedSynapse.SynapseType.DATA, "audio task", 8);
        
        System.out.println("Testing COR -> all agencies sync...");
        synapse.sendMessage("COR", "KAI", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "VEX", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "AUM", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "NEX", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        
        // Wait for processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        
        System.out.println("Inter-agency communication test complete.");
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        aum.shutdown();
        nex.shutdown();
        cor.shutdown();
    }
    
    private static void testCorpusCallosumIntegration(UnifiedSynapse synapse, UnifiedStateLattice lattice,
                                                       CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        
        System.out.println("Testing Hebbian learning...");
        callosum.processActivation("KAI", "task1");
        callosum.processActivation("VEX", "task2");
        
        System.out.println("Testing state synchronization...");
        callosum.synchronizeStates();
        
        System.out.println(callosum.getStatistics());
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
    }
    
    private static void testFullSystemIntegration(UnifiedSynapse synapse, UnifiedStateLattice lattice,
                                                    CorpusCallosumUltimate callosum, UltimateCPU cpu) {
        // Create all agencies
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        AgencyNEX nex = new AgencyNEX(synapse, lattice, callosum, cpu);
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        
        System.out.println("All agencies created and initialized.");
        
        // Test CPU allocation
        System.out.println("\nTesting CPU allocation...");
        int kaiCores = cpu.allocateCores("KAI");
        long kaiMemory = cpu.allocateMemory("KAI");
        System.out.println("KAI allocated: " + kaiCores + " cores, " + cpu.formatBytes(kaiMemory));
        
        // Test state lattice
        System.out.println("\nTesting state lattice...");
        lattice.updateNode("KAI:status", "active", 0.9);
        lattice.updateNode("VEX:status", "ready", 0.8);
        System.out.println(lattice.getStatistics());
        
        // Test corpus callosum
        System.out.println("\nTesting corpus callosum...");
        callosum.processActivation("KAI", "reasoning");
        callosum.processActivation("VEX", "visualization");
        callosum.synchronizeStates();
        
        // Test agency status
        System.out.println("\nAgency Status:");
        System.out.println(kai.getStatus());
        System.out.println(cor.getStatus());
        
        // Test synapse communication
        System.out.println("\nTesting synapse communication...");
        synapse.sendMessage("COR", "KAI", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "VEX", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "AUM", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        synapse.sendMessage("COR", "NEX", UnifiedSynapse.SynapseType.CONTROL, "sync", 9);
        
        // Wait for processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        
        System.out.println("\nFull system integration test complete.");
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        aum.shutdown();
        nex.shutdown();
        cor.shutdown();
    }
}
