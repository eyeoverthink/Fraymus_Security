package fraymus.commands;

import fraymus.ExperimentManager;

/**
 * Ask command - query the neural network
 */
public class AskCommand extends BaseCommand {
    
    private static ExperimentManager experimentManager;
    
    public static void setExperimentManager(ExperimentManager mgr) {
        experimentManager = mgr;
    }
    
    @Override
    public String getName() {
        return "ask";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"query", "q"};
    }
    
    @Override
    public String getDescription() {
        return "Query the Phi Neural Network";
    }
    
    @Override
    public String getUsage() {
        return "ask <question>";
    }
    
    @Override
    public boolean execute(String args) {
        if (experimentManager == null) {
            printError("ExperimentManager not initialized");
            return false;
        }
        
        if (args == null || args.trim().isEmpty()) {
            printError("Usage: ask <question>");
            return false;
        }
        
        String question = args.trim();
        printInfo("Querying neural network: " + question);
        
        try {
            String response = experimentManager.getNeuralNet().query(question);
            printSuccess("Response: " + response);
            return true;
        } catch (Exception e) {
            printError("Query failed: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void showHelp() {
        printHighlight("═══ ASK COMMAND ═══");
        print("  ask <question>    Query the Phi Neural Network");
        print("");
        print("  Aliases: query, q");
        print("");
        print("  Examples:");
        print("    ask What is the meaning of phi?");
        print("    ask Explain resonance");
        print("    query How does entanglement work?");
    }
}
