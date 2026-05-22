package fraymus.ultimate;

/**
 * UltimateCommandSystem Test
 * Tests the unified command interface for all agencies.
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 */
public class UltimateCommandSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== ULTIMATE COMMAND SYSTEM TEST ===\n");
        
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
        
        // Initialize command system
        System.out.println("Initializing command system...\n");
        UltimateCommandSystem cmd = new UltimateCommandSystem(kai, vex, aum, nex, cor, synapse, callosum);
        
        // Test AGENCY commands
        System.out.println("Test 1: AGENCY Commands");
        System.out.println(cmd.executeCommand("agency select KAI"));
        System.out.println(cmd.executeCommand("agency status"));
        System.out.println(cmd.executeCommand("agency route test task"));
        
        // Test SYNAPSE commands
        System.out.println("\nTest 2: SYNAPSE Commands");
        System.out.println(cmd.executeCommand("synapse connect KAI VEX"));
        System.out.println(cmd.executeCommand("synapse send VEX test message"));
        System.out.println(cmd.executeCommand("synapse query test"));
        
        // Test CORPUS commands
        System.out.println("\nTest 3: CORPUS Commands");
        System.out.println(cmd.executeCommand("corpus weights"));
        System.out.println(cmd.executeCommand("corpus optimize"));
        System.out.println(cmd.executeCommand("corpus consciousness"));
        
        // Test VISION commands
        System.out.println("\nTest 4: VISION Commands");
        System.out.println(cmd.executeCommand("vision generate sunset"));
        System.out.println(cmd.executeCommand("vision stream animation"));
        System.out.println(cmd.executeCommand("vision analyze image"));
        
        // Test AUDIO commands
        System.out.println("\nTest 5: AUDIO Commands");
        System.out.println(cmd.executeCommand("audio speak hello world"));
        System.out.println(cmd.executeCommand("audio listen"));
        System.out.println(cmd.executeCommand("audio analyze audio_file"));
        
        // Test LEARNING commands
        System.out.println("\nTest 6: LEARNING Commands");
        System.out.println(cmd.executeCommand("learn from data_source"));
        System.out.println(cmd.executeCommand("evolve system"));
        System.out.println(cmd.executeCommand("optimize component"));
        
        // Test META commands
        System.out.println("\nTest 7: META Commands");
        System.out.println(cmd.executeCommand("meta status"));
        System.out.println(cmd.executeCommand("meta route complex task"));
        System.out.println(cmd.executeCommand("meta optimize"));
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        aum.shutdown();
        nex.shutdown();
        cor.shutdown();
        synapse.shutdown();
        
        System.out.println("\n=== COMMAND SYSTEM TEST COMPLETE ===");
    }
}
