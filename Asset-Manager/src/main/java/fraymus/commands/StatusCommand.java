package fraymus.commands;

import fraymus.ExperimentManager;

/**
 * Status command - shows system status
 */
public class StatusCommand extends BaseCommand {
    
    private static ExperimentManager experimentManager;
    
    public static void setExperimentManager(ExperimentManager mgr) {
        experimentManager = mgr;
    }
    
    @Override
    public String getName() {
        return "status";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"stat", "st"};
    }
    
    @Override
    public String getDescription() {
        return "Show system status";
    }
    
    @Override
    public String getUsage() {
        return "status";
    }
    
    @Override
    public boolean execute(String args) {
        if (experimentManager == null) {
            printError("ExperimentManager not initialized");
            return false;
        }
        
        printHighlight("═══ SYSTEM STATUS ═══");
        print("World: " + experimentManager.getWorld().getNodes().size() + " entities");
        print("Memory: " + experimentManager.getInfiniteMemory().getRecordCount() + " records");
        print("Learner: " + experimentManager.getPassiveLearner().getPassiveCycles() + " cycles");
        print("Genome: " + experimentManager.getQRGenome().getGenomeSize() + " codons");
        print("Model: " + experimentManager.getCurrentModel());
        
        return true;
    }
    
    @Override
    public void showHelp() {
        printHighlight("═══ STATUS COMMAND ═══");
        print("  status            Show system status");
        print("");
        print("  Aliases: stat, st");
        print("");
        print("  Displays:");
        print("    - Entity count");
        print("    - Memory records");
        print("    - Learner cycles");
        print("    - Genome size");
        print("    - Current model");
    }
}
