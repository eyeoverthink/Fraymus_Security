package fraymus.commands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all terminal commands
 * Provides command lookup, registration, and help system
 */
public class CommandRegistry {
    private static CommandRegistry instance = null;
    private final Map<String, Command> commands;
    private final Map<String, String> aliases; // alias -> primary command name
    
    private CommandRegistry() {
        commands = new ConcurrentHashMap<>();
        aliases = new ConcurrentHashMap<>();
    }
    
    public static synchronized CommandRegistry getInstance() {
        if (instance == null) {
            instance = new CommandRegistry();
        }
        return instance;
    }
    
    /**
     * Register a command
     */
    public void register(Command command) {
        String name = command.getName().toLowerCase();
        commands.put(name, command);
        
        // Register aliases
        for (String alias : command.getAliases()) {
            aliases.put(alias.toLowerCase(), name);
        }
    }
    
    /**
     * Unregister a command
     */
    public void unregister(String commandName) {
        String name = commandName.toLowerCase();
        Command cmd = commands.remove(name);
        
        if (cmd != null) {
            // Remove aliases
            for (String alias : cmd.getAliases()) {
                aliases.remove(alias.toLowerCase());
            }
        }
    }
    
    /**
     * Get a command by name or alias
     */
    public Command getCommand(String name) {
        String lowerName = name.toLowerCase();
        
        // Check direct command
        Command cmd = commands.get(lowerName);
        if (cmd != null) {
            return cmd;
        }
        
        // Check alias
        String primaryName = aliases.get(lowerName);
        if (primaryName != null) {
            return commands.get(primaryName);
        }
        
        return null;
    }
    
    /**
     * Check if a command exists
     */
    public boolean hasCommand(String name) {
        return getCommand(name) != null;
    }
    
    /**
     * Get all registered command names
     */
    public Set<String> getAllCommandNames() {
        return new TreeSet<>(commands.keySet());
    }
    
    /**
     * Get all commands
     */
    public Collection<Command> getAllCommands() {
        return commands.values();
    }
    
    /**
     * Execute a command by name
     */
    public boolean execute(String commandName, String args) {
        Command cmd = getCommand(commandName);
        if (cmd == null) {
            System.out.println("Unknown command: " + commandName);
            return false;
        }
        
        return cmd.execute(args);
    }
    
    /**
     * Show help for a specific command
     */
    public void showHelp(String commandName) {
        Command cmd = getCommand(commandName);
        if (cmd == null) {
            System.out.println("Unknown command: " + commandName);
            return;
        }
        
        cmd.showHelp();
    }
    
    /**
     * Show help for all commands
     */
    public void showAllHelp() {
        System.out.println("=== Available Commands ===");
        for (String name : getAllCommandNames()) {
            Command cmd = commands.get(name);
            System.out.printf("  %-20s %s%n", name, cmd.getDescription());
        }
        System.out.println("\nType 'help <command>' for detailed usage.");
    }
    
    /**
     * Clear all commands (useful for testing)
     */
    public void clear() {
        commands.clear();
        aliases.clear();
    }
}
