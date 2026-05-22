package fraymus.explorers;

/**
 * Test Knowledge Explorer
 */
public class TestKnowledgeExplorer {
    public static void main(String[] args) {
        System.out.println("Testing KnowledgeExplorer...");
        
        try {
            KnowledgeExplorer explorer = new KnowledgeExplorer(new fraymus.knowledge.AkashicRecord());
            
            // Test search functionality
            var results = explorer.search("phi");
            System.out.println("Search results for 'phi': " + results.size());
            
            System.out.println("\n✅ KnowledgeExplorer test PASSED");
        } catch (Exception e) {
            System.err.println("❌ KnowledgeExplorer test FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
