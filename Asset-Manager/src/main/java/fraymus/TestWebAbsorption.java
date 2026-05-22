package fraymus;

import fraymus.absorption.URLAbsorber;
import fraymus.knowledge.AkashicRecord;

/**
 * Test URLAbsorber integration
 */
public class TestWebAbsorption {
    public static void main(String[] args) {
        System.out.println("🌐 Testing URLAbsorber Integration");
        System.out.println("=====================================\n");
        
        AkashicRecord akashic = new AkashicRecord();
        URLAbsorber webAbsorber = new URLAbsorber(akashic);
        
        // Test with a simple, reliable URL (HTTP to avoid SSL issues)
        String testUrl = "http://example.com";
        
        System.out.println("Absorbing: " + testUrl);
        webAbsorber.absorb(testUrl);
        
        System.out.println("\n📊 AkashicRecord Status:");
        System.out.println("Total blocks: " + akashic.getPersistedBlockCount());
        System.out.println("Categories: " + akashic.getCategoryCount());
        
        // Test query
        System.out.println("\n🔍 Testing query for 'example':");
        var results = akashic.query("example");
        System.out.println("Found " + results.size() + " matching blocks");
        
        if (!results.isEmpty()) {
            System.out.println("\nFirst result:");
            System.out.println(results.get(0).content);
        }
        
        // Test with Apache Arrow Wikipedia (will fail SSL, but that's expected)
        System.out.println("\n\n🌐 Testing Apache Arrow Wikipedia (HTTPS - will fail SSL):");
        String arrowUrl = "https://en.wikipedia.org/wiki/Apache_Arrow";
        System.out.println("Absorbing: " + arrowUrl);
        webAbsorber.absorb(arrowUrl);
        
        System.out.println("\n📊 Final AkashicRecord Status:");
        System.out.println("Total blocks: " + akashic.getPersistedBlockCount());
        System.out.println("Categories: " + akashic.getCategoryCount());
        
        // Test query for Apache Arrow
        System.out.println("\n🔍 Testing query for 'Apache Arrow':");
        var arrowResults = akashic.query("Apache Arrow");
        System.out.println("Found " + arrowResults.size() + " matching blocks");
        
        // Show first few absorbed blocks
        if (!arrowResults.isEmpty()) {
            System.out.println("\n📄 Sample absorbed content:");
            for (int i = 0; i < Math.min(3, arrowResults.size()); i++) {
                System.out.println("\n--- Block " + (i+1) + " ---");
                System.out.println(arrowResults.get(i).category + ": " + arrowResults.get(i).content.substring(0, Math.min(200, arrowResults.get(i).content.length())));
            }
        }
    }
}
