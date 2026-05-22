package fraymus.explorers;

/**
 * Test Phi Explorer
 */
public class TestPhiExplorer {
    public static void main(String[] args) {
        System.out.println("Testing PhiExplorerBuilder...");
        
        try {
            PhiExplorerBuilder builder = new PhiExplorerBuilder();
            PhiExplorerBuilder.PhiExplorer explorer = builder.build();
            
            // Test pattern finding
            double[] testData = {1.0, 1.618, 2.618, 4.236, 6.854};
            var patterns = explorer.findPhiPatterns(testData);
            
            System.out.println("Phi patterns found: " + patterns.size());
            for (var entry : patterns.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            
            // Test exploration
            String result = explorer.explore("Fibonacci sequence");
            System.out.println("\nExploration result:");
            System.out.println(result);
            
            System.out.println("\n✅ PhiExplorer test PASSED");
        } catch (Exception e) {
            System.err.println("❌ PhiExplorer test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
