package fraymus.commands;

import fraymus.ExperimentManager;

/**
 * Initializes and registers all modular commands
 */
public class CommandInitializer {
    
    private static boolean initialized = false;
    
    /**
     * Initialize all commands with the CommandRegistry
     */
    public static synchronized void initialize(ExperimentManager experimentManager) {
        if (initialized) {
            return;
        }
        
        CommandRegistry registry = CommandRegistry.getInstance();
        
        // Set ExperimentManager references for commands that need it
        StatusCommand.setExperimentManager(experimentManager);
        SpawnCommand.setExperimentManager(experimentManager);
        AskCommand.setExperimentManager(experimentManager);
        
        // Register core commands
        registry.register(new HelpCommand());
        registry.register(new StatusCommand());
        registry.register(new SpawnCommand());
        registry.register(new AskCommand());
        registry.register(new SelfTestCommand());
        
        // TODO: Register more commands as they are migrated to the new system
        // registry.register(new LearnCommand());
        // registry.register(new MemoryCommand());
        // registry.register(new GenomeCommand());
        // registry.register(new OllamaCommand());
        // etc.
        
        initialized = true;
    }
    
    /**
     * Check if commands have been initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
