package fraymus;

import jade.Window;

/**
 * FRAYMUS UNIFIED LAUNCHER
 * 
 * Provides multiple launch modes for the Fraymus system:
 * - runFraymusEngineV2: Launch the Engine V2 with ImGui UI
 * - runFraynixBoot: Launch Fraynix boot sequence
 * - runSfaBridge: Launch SFA bridge only (HTTP/WebSocket server)
 * - runAll: Launch all components
 */
public class FraymusLauncher {
    
    public enum LaunchMode {
        ENGINE_V2,
        FRAYNIX_BOOT,
        SFA_BRIDGE,
        ALL
    }
    
    /**
     * Main entry point with mode selection
     */
    public static void main(String[] args) {
        LaunchMode mode = parseLaunchMode(args);
        
        switch (mode) {
            case ENGINE_V2:
                runFraymusEngineV2();
                break;
            case FRAYNIX_BOOT:
                runFraynixBoot();
                break;
            case SFA_BRIDGE:
                runSfaBridge();
                break;
            case ALL:
                runAll();
                break;
        }
    }
    
    /**
     * Parse launch mode from command line arguments
     */
    private static LaunchMode parseLaunchMode(String[] args) {
        if (args.length == 0) {
            return LaunchMode.ENGINE_V2; // Default mode
        }
        
        String mode = args[0].toLowerCase();
        switch (mode) {
            case "engine":
            case "enginev2":
            case "v2":
                return LaunchMode.ENGINE_V2;
            case "fraynix":
            case "boot":
                return LaunchMode.FRAYNIX_BOOT;
            case "bridge":
            case "sfa":
                return LaunchMode.SFA_BRIDGE;
            case "all":
                return LaunchMode.ALL;
            default:
                System.out.println("Unknown mode: " + mode);
                System.out.println("Usage: java FraymusLauncher [engine|fraynix|bridge|all]");
                System.out.println("  engine  - Run Fraymus Engine V2 (default)");
                System.out.println("  fraynix - Run Fraynix boot sequence");
                System.out.println("  bridge  - Run SFA bridge only");
                System.out.println("  all     - Run all components");
                System.exit(1);
                return LaunchMode.ENGINE_V2;
        }
    }
    
    /**
     * Launch Fraymus Engine V2 with ImGui UI
     */
    public static void runFraymusEngineV2() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🧬 FRAYMUS ENGINE V2 LAUNCHER                              ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Starting Engine V2 with ImGui UI...");
        
        try {
            Window.get().run();
        } catch (Exception e) {
            System.err.println("Failed to start Engine V2: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Launch Fraynix boot sequence
     */
    public static void runFraynixBoot() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🌀 FRAYNIX BOOT SEQUENCE                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Fraynix boot sequence not yet implemented.");
        System.out.println("Use 'engine' mode to run Engine V2 instead.");
        System.exit(0);
    }
    
    /**
     * Launch SFA bridge only (HTTP/WebSocket server without UI)
     */
    public static void runSfaBridge() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🌐 SFA BRIDGE MODE                                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Starting SFA bridge (HTTP/WebSocket server)...");
        
        try {
            // Initialize NerveCenter for HTTP/WebSocket server
            // Note: This requires ExperimentManager to be initialized first
            // For now, this is a placeholder - full implementation would require
            // initializing the core components without the ImGui UI
            fraymus.NerveCenter.getInstance();
            
            System.out.println("SFA Bridge online:");
            System.out.println("  WebSocket: ws://localhost:8887");
            System.out.println("  HTTP: http://localhost:8888/api/engine/*");
            System.out.println();
            System.out.println("Press Ctrl+C to stop.");
            
            // Keep the server running
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Failed to start SFA bridge: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Launch all components
     */
    public static void runAll() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🚀 ALL COMPONENTS MODE                                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Starting all components...");
        System.out.println("Note: This mode will launch Engine V2 which includes the bridge.");
        System.out.println();
        
        // For now, 'all' mode just runs Engine V2 since it includes the bridge
        runFraymusEngineV2();
    }
}
