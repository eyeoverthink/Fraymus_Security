package fraymus.commands;

import java.util.List;

/**
 * Self-test command - runs tests on all registered commands
 */
public class SelfTestCommand extends BaseCommand {
    
    @Override
    public String getName() {
        return "selftest";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"test", "verify"};
    }
    
    @Override
    public String getDescription() {
        return "Run self-tests on all commands";
    }
    
    @Override
    public String getUsage() {
        return "selftest [command]";
    }
    
    @Override
    public boolean execute(String args) {
        if (args == null || args.trim().isEmpty()) {
            // Test all commands
            printHighlight("═══ RUNNING COMMAND SELF-TESTS ═══");
            List<CommandSelfTest.TestResult> results = CommandSelfTest.runAllTests();
            CommandSelfTest.printResults(results);
            return true;
        }
        
        // Test specific command
        String commandName = args.trim();
        printHighlight("═══ TESTING COMMAND: " + commandName + " ═══");
        CommandSelfTest.TestResult result = CommandSelfTest.testCommand(commandName);
        
        if (result.passed) {
            printSuccess("✓ PASS: " + result.message + " (" + result.durationMs + "ms)");
        } else {
            printError("✗ FAIL: " + result.message + " (" + result.durationMs + "ms)");
        }
        
        return result.passed;
    }
    
    @Override
    public void showHelp() {
        printHighlight("═══ SELF-TEST COMMAND ═══");
        print("  selftest           Run self-tests on all commands");
        print("  selftest <cmd>     Test a specific command");
        print("");
        print("  Aliases: test, verify");
        print("");
        print("  This command verifies that all registered commands");
        print("  can execute without errors. Useful for validation");
        print("  after adding new commands or making changes.");
    }
}
