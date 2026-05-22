package fraymus.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Self-test framework for commands
 * Tests each command to verify it can execute without errors
 */
public class CommandSelfTest {
    
    public static class TestResult {
        public final String commandName;
        public final boolean passed;
        public final String message;
        public final long durationMs;
        
        public TestResult(String commandName, boolean passed, String message, long durationMs) {
            this.commandName = commandName;
            this.passed = passed;
            this.message = message;
            this.durationMs = durationMs;
        }
    }
    
    /**
     * Run self-tests for all registered commands
     */
    public static List<TestResult> runAllTests() {
        List<TestResult> results = new ArrayList<>();
        CommandRegistry registry = CommandRegistry.getInstance();
        
        for (String commandName : registry.getAllCommandNames()) {
            TestResult result = testCommand(commandName);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Test a single command
     */
    public static TestResult testCommand(String commandName) {
        long startTime = System.currentTimeMillis();
        CommandRegistry registry = CommandRegistry.getInstance();
        Command cmd = registry.getCommand(commandName);
        
        if (cmd == null) {
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult(commandName, false, "Command not found in registry", duration);
        }
        
        try {
            // Test with empty args first
            boolean result = cmd.execute("");
            long duration = System.currentTimeMillis() - startTime;
            
            if (result) {
                return new TestResult(commandName, true, "Executed successfully with empty args", duration);
            } else {
                return new TestResult(commandName, false, "Command returned false with empty args", duration);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult(commandName, false, "Exception: " + e.getMessage(), duration);
        }
    }
    
    /**
     * Print test results to console
     */
    public static void printResults(List<TestResult> results) {
        int passed = 0;
        int failed = 0;
        
        System.out.println("=== COMMAND SELF-TEST RESULTS ===");
        System.out.println();
        
        for (TestResult result : results) {
            String status = result.passed ? "✓ PASS" : "✗ FAIL";
            System.out.printf("%-20s %s (%dms)%n", result.commandName, status, result.durationMs);
            
            if (!result.passed) {
                System.out.println("  Reason: " + result.message);
                failed++;
            } else {
                passed++;
            }
        }
        
        System.out.println();
        System.out.println("Total: " + results.size() + " | Passed: " + passed + " | Failed: " + failed);
    }
}
