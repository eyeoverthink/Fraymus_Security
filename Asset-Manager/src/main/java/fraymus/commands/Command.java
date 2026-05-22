package fraymus.commands;

/**
 * Command interface for modular terminal commands
 */
public interface Command {
    /**
     * Get the command name (e.g., "help", "status")
     */
    String getName();
    
    /**
     * Get command aliases (alternative names)
     */
    default String[] getAliases() {
        return new String[0];
    }
    
    /**
     * Get command description
     */
    String getDescription();
    
    /**
     * Get command usage syntax
     */
    default String getUsage() {
        return getName();
    }
    
    /**
     * Execute the command with given arguments
     * @param args Command arguments (everything after the command name)
     * @return true if successful, false otherwise
     */
    boolean execute(String args);
    
    /**
     * Show help for this command
     */
    default void showHelp() {
        System.out.println("Usage: " + getUsage());
        System.out.println("Description: " + getDescription());
    }
}
