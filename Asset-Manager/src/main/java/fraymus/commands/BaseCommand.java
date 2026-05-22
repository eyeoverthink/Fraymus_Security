package fraymus.commands;

/**
 * Base abstract class for commands with common terminal output methods
 */
public abstract class BaseCommand implements Command {
    
    protected void print(String text) {
        fraymus.CommandTerminal.print(text);
    }
    
    protected void printSuccess(String text) {
        fraymus.CommandTerminal.printSuccess(text);
    }
    
    protected void printError(String text) {
        fraymus.CommandTerminal.printError(text);
    }
    
    protected void printInfo(String text) {
        fraymus.CommandTerminal.printInfo(text);
    }
    
    protected void printHighlight(String text) {
        fraymus.CommandTerminal.printHighlight(text);
    }
    
    protected void printWarning(String text) {
        fraymus.CommandTerminal.printWarning(text);
    }
}
