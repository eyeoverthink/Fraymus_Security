package fraymus.commands;

import fraymus.ExperimentManager;
import fraymus.PhiNode;

/**
 * Spawn command - create new entities
 */
public class SpawnCommand extends BaseCommand {
    
    private static ExperimentManager experimentManager;
    
    public static void setExperimentManager(ExperimentManager mgr) {
        experimentManager = mgr;
    }
    
    @Override
    public String getName() {
        return "spawn";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"create", "add"};
    }
    
    @Override
    public String getDescription() {
        return "Spawn a new entity in the world";
    }
    
    @Override
    public String getUsage() {
        return "spawn <name> [x] [y] [vx] [vy] [energy]";
    }
    
    @Override
    public boolean execute(String args) {
        if (experimentManager == null) {
            printError("ExperimentManager not initialized");
            return false;
        }
        
        String[] parts = args.trim().split("\\s+");
        if (parts.length < 1 || parts[0].isEmpty()) {
            printError("Usage: spawn <name> [x] [y] [vx] [vy] [energy]");
            return false;
        }
        
        String name = parts[0];
        double x = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;
        double y = parts.length > 2 ? Double.parseDouble(parts[2]) : 0.0;
        double vx = parts.length > 3 ? Double.parseDouble(parts[3]) : (Math.random() - 0.5);
        double vy = parts.length > 4 ? Double.parseDouble(parts[4]) : (Math.random() - 0.5);
        double energy = parts.length > 5 ? Double.parseDouble(parts[5]) : 1.0;
        
        PhiNode node = new PhiNode(name, (float) x, (float) y);
        node.vx = (float) vx;
        node.vy = (float) vy;
        node.energy = (float) energy;
        
        experimentManager.getWorld().addNode(node);
        printSuccess("Spawned entity: " + name + " at (" + x + ", " + y + ")");
        
        return true;
    }
    
    @Override
    public void showHelp() {
        printHighlight("═══ SPAWN COMMAND ═══");
        print("  spawn <name> [x] [y] [vx] [vy] [energy]");
        print("");
        print("  Aliases: create, add");
        print("");
        print("  Parameters:");
        print("    name    Entity name (required)");
        print("    x       X position (default: 0)");
        print("    y       Y position (default: 0)");
        print("    vx      X velocity (default: random)");
        print("    vy      Y velocity (default: random)");
        print("    energy  Energy level (default: 1.0)");
        print("");
        print("  Examples:");
        print("    spawn Alpha");
        print("    spawn Beta 10 20");
        print("    spawn Gamma -30 40 0.5 -0.3 0.9");
    }
}
