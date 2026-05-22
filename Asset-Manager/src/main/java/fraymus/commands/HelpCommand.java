package fraymus.commands;

/**
 * Help command - shows help for all commands or specific command
 */
public class HelpCommand extends BaseCommand {
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"?", "h"};
    }
    
    @Override
    public String getDescription() {
        return "Show help for commands";
    }
    
    @Override
    public String getUsage() {
        return "help [command]";
    }
    
    @Override
    public boolean execute(String args) {
        CommandRegistry registry = CommandRegistry.getInstance();
        
        if (args == null || args.trim().isEmpty()) {
            registry.showAllHelp();
            return true;
        }
        
        registry.showHelp(args.trim());
        return true;
    }
    
    @Override
    public void showHelp() {
        printHighlight("═══ HELP COMMAND ═══");
        print("  help              Show all available commands");
        print("  help <command>    Show detailed help for a specific command");
        print("");
        print("  Aliases: ?, h");
        print("");
        print("  Examples:");
        print("    help");
        print("    help status");
        print("    help spawn");
    }
}
