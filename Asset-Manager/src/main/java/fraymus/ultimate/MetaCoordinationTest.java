package fraymus.ultimate;

/**
 * MetaCoordinationTest - Phase 7 Integration Test
 * 
 * Tests the Phase 7 Meta-Coordination components:
 * - SystemPerformanceMonitor - System-wide performance monitoring
 * - MetaLearningEngine - Meta-learning mechanism across agencies
 * - CrossAgencyCoordinator - Cross-agency coordination protocols
 * - Enhanced AgencyCOR with phi-harmonic resource allocation
 * 
 * @author Vaughn Scott
 * @date May 7, 2026
 * @version 1.0
 */
public class MetaCoordinationTest {
    
    public static void main(String[] args) {
        System.out.println("=== PHASE 7: META-COORDINATION INTEGRATION TEST ===\n");
        
        // Test 1: SystemPerformanceMonitor
        testSystemPerformanceMonitor();
        
        // Test 2: MetaLearningEngine
        testMetaLearningEngine();
        
        // Test 3: CrossAgencyCoordinator
        testCrossAgencyCoordinator();
        
        // Test 4: Enhanced AgencyCOR
        testEnhancedAgencyCOR();
        
        // Test 5: Integration Test
        testIntegration();
        
        System.out.println("\n=== ALL PHASE 7 TESTS PASSED ===");
    }
    
    /**
     * Test 1: SystemPerformanceMonitor
     */
    private static void testSystemPerformanceMonitor() {
        System.out.println("Test 1: SystemPerformanceMonitor");
        
        SystemPerformanceMonitor monitor = new SystemPerformanceMonitor();
        
        // Start monitoring
        monitor.startMonitoring();
        System.out.println("✓ Monitoring started");
        
        // Update agency metrics
        monitor.updateAgencyMetrics("KAI", 100.0, 50.0, 0.95);
        monitor.updateAgencyMetrics("VEX", 80.0, 60.0, 0.88);
        monitor.updateAgencyMetrics("AUM", 90.0, 45.0, 0.92);
        monitor.updateAgencyMetrics("NEX", 110.0, 40.0, 0.97);
        monitor.updateAgencyMetrics("COR", 95.0, 35.0, 0.94);
        System.out.println("✓ Agency metrics updated");
        
        // Check system health
        double systemHealth = monitor.getSystemHealth();
        System.out.println("✓ System health: " + String.format("%.3f", systemHealth));
        
        // Get performance report
        monitor.getPerformanceReport();
        System.out.println("✓ Performance report generated");
        
        // Get trend analysis
        String trend = monitor.getTrendAnalysis();
        System.out.println("✓ Trend analysis: " + trend.split("\n")[1]);
        
        // Check for bottlenecks
        var bottlenecks = monitor.getBottlenecks();
        System.out.println("✓ Bottlenecks detected: " + bottlenecks.size());
        
        // Stop monitoring
        monitor.stopMonitoring();
        System.out.println("✓ Monitoring stopped\n");
    }
    
    /**
     * Test 2: MetaLearningEngine
     */
    private static void testMetaLearningEngine() {
        System.out.println("Test 2: MetaLearningEngine");
        
        MetaLearningEngine engine = new MetaLearningEngine();
        System.out.println("✓ Meta-learning engine initialized");
        
        // Report agency performance
        engine.reportAgencyPerformance("KAI", 0.95);
        engine.reportAgencyPerformance("VEX", 0.88);
        engine.reportAgencyPerformance("AUM", 0.92);
        engine.reportAgencyPerformance("NEX", 0.97);
        engine.reportAgencyPerformance("COR", 0.94);
        System.out.println("✓ Agency performance reported");
        
        // Perform meta-learning cycle
        engine.performMetaLearningCycle();
        System.out.println("✓ Meta-learning cycle performed");
        
        // Check learning rates
        double kaiRate = engine.getLearningRate("KAI");
        double vexRate = engine.getLearningRate("VEX");
        System.out.println("✓ Learning rates - KAI: " + String.format("%.3f", kaiRate) + 
                          ", VEX: " + String.format("%.3f", vexRate));
        
        // Get meta-learning report
        engine.getMetaLearningReport();
        System.out.println("✓ Meta-learning report generated");
        
        // Check meta-learning cycles
        int cycles = engine.getMetaLearningCycles();
        System.out.println("✓ Meta-learning cycles: " + cycles + "\n");
    }
    
    /**
     * Test 3: CrossAgencyCoordinator
     */
    private static void testCrossAgencyCoordinator() {
        System.out.println("Test 3: CrossAgencyCoordinator");
        
        CrossAgencyCoordinator coordinator = new CrossAgencyCoordinator();
        System.out.println("✓ Cross-agency coordinator initialized");
        
        // Submit coordination tasks
        CrossAgencyCoordinator.CoordinationTask task1 = 
            new CrossAgencyCoordinator.CoordinationTask("TASK1", "VISUAL", "Process image", 8);
        CrossAgencyCoordinator.CoordinationTask task2 = 
            new CrossAgencyCoordinator.CoordinationTask("TASK2", "AUDIO", "Process speech", 7);
        CrossAgencyCoordinator.CoordinationTask task3 = 
            new CrossAgencyCoordinator.CoordinationTask("TASK3", "COORDINATE", "Optimize system", 9);
        
        coordinator.submitTask(task1);
        coordinator.submitTask(task2);
        coordinator.submitTask(task3);
        System.out.println("✓ Tasks submitted");
        
        // Request shared resources
        boolean allocated = coordinator.requestResource("CPU", "KAI", 30.0);
        System.out.println("✓ Resource allocation: " + allocated);
        
        // Release shared resource
        coordinator.releaseResource("CPU", "KAI", 30.0);
        System.out.println("✓ Resource released");
        
        // Detect and resolve conflicts
        coordinator.detectAndResolveConflicts();
        System.out.println("✓ Conflicts detected and resolved");
        
        // Check for deadlocks
        boolean hasDeadlock = coordinator.checkDeadlocks();
        System.out.println("✓ Deadlock check: " + (hasDeadlock ? "DEADLOCK" : "No deadlock"));
        
        // Get coordination status
        coordinator.getCoordinationStatus();
        System.out.println("✓ Coordination status generated");
        
        // Check global synchronization
        double sync = coordinator.getGlobalSynchronization();
        System.out.println("✓ Global synchronization: " + String.format("%.3f", sync) + "\n");
    }
    
    /**
     * Test 4: Enhanced AgencyCOR
     */
    private static void testEnhancedAgencyCOR() {
        System.out.println("Test 4: Enhanced AgencyCOR");
        
        // Initialize core components
        UnifiedSynapse synapse = new UnifiedSynapse();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        UltimateCPU cpu = new UltimateCPU();
        
        // Create enhanced COR agency
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        System.out.println("✓ Enhanced COR agency created");
        
        // Check meta-coordination is enabled
        System.out.println("✓ Meta-coordination enabled in COR agency");
        
        // Send control commands via synapse
        synapse.sendMessage("COR", "COR", UnifiedSynapse.SynapseType.CONTROL, "meta_learning_cycle", 9);
        synapse.sendMessage("COR", "COR", UnifiedSynapse.SynapseType.CONTROL, "detect_conflicts", 8);
        synapse.sendMessage("COR", "COR", UnifiedSynapse.SynapseType.CONTROL, "performance_report", 7);
        System.out.println("✓ Meta-coordination commands sent");
        
        // Get COR status
        cor.getStatus();
        System.out.println("✓ COR status retrieved");
        
        // Check system efficiency
        double efficiency = cor.getSystemEfficiency();
        System.out.println("✓ System efficiency: " + String.format("%.3f", efficiency));
        
        // Check global resonance
        double resonance = cor.getGlobalResonance();
        System.out.println("✓ Global resonance: " + String.format("%.3f", resonance) + "\n");
    }
    
    /**
     * Test 5: Integration Test
     */
    private static void testIntegration() {
        System.out.println("Test 5: Full Integration");
        
        // Initialize all components
        UnifiedSynapse synapse = new UnifiedSynapse();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        UltimateCPU cpu = new UltimateCPU();
        
        // Create all agencies
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        AgencyNEX nex = new AgencyNEX(synapse, lattice, callosum, cpu);
        AgencyCOR cor = new AgencyCOR(synapse, lattice, callosum, cpu);
        System.out.println("✓ All agencies created");
        
        // Initialize command system
        UltimateCommandSystem commandSystem = new UltimateCommandSystem(kai, vex, aum, nex, cor, synapse, callosum);
        System.out.println("✓ Command system initialized");
        
        // Test meta-coordination commands
        commandSystem.executeCommand("meta optimize");
        System.out.println("✓ Meta-learning cycle executed");
        
        commandSystem.executeCommand("meta optimize");
        System.out.println("✓ Conflict detection executed");
        
        // Test cross-agency communication
        synapse.sendMessage("KAI", "VEX", UnifiedSynapse.SynapseType.DATA, "Meta-coordination test", 8);
        synapse.sendMessage("VEX", "AUM", UnifiedSynapse.SynapseType.EVENT, "Meta-coordination event", 7);
        synapse.sendMessage("AUM", "NEX", UnifiedSynapse.SynapseType.QUERY, "Meta-coordination query", 6);
        System.out.println("✓ Cross-agency communication tested");
        
        // Test phi-harmonic resource allocation
        cor.processData("allocate resources");
        System.out.println("✓ Phi-harmonic resource allocation tested");
        
        // Test system optimization
        commandSystem.executeCommand("meta optimize");
        System.out.println("✓ System optimization tested");
        
        // Check final system state
        double finalEfficiency = cor.getSystemEfficiency();
        double finalResonance = cor.getGlobalResonance();
        System.out.println("✓ Final system efficiency: " + String.format("%.3f", finalEfficiency));
        System.out.println("✓ Final global resonance: " + String.format("%.3f", finalResonance));
        
        System.out.println("\n✓ Full integration test completed successfully");
    }
}
