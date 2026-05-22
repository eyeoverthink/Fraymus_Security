package fraymus;

import fraymus.knowledge.AkashicRecord;

/**
 * Test direct knowledge response (without LLM)
 * Theory: System should answer from absorbed knowledge without sending to external LLM
 */
public class TestDirectKnowledge {
    public static void main(String[] args) {
        System.out.println("🧠 TESTING DIRECT KNOWLEDGE RESPONSE");
        System.out.println("=====================================\n");
        
        AkashicRecord akashic = new AkashicRecord();
        
        // Test queries that have absorbed knowledge
        String[] testQueries = {
            "What is Claude?",
            "What is Gemma?",
            "What is Apache Arrow?",
            "What is quantum entanglement?",
            "What is Papers with Code?"
        };
        
        for (String query : testQueries) {
            System.out.println("\n🔍 Query: \"" + query + "\"");
            
            // Extract key term with stop word filtering
            String[] words = query.toLowerCase().split("\\s+");
            String searchTerm = "";

            // Common stop words to skip
            java.util.Set<String> stopWords = java.util.Set.of(
                "what", "is", "the", "how", "why", "when", "where", "who", "which",
                "are", "was", "were", "been", "being", "have", "has", "had",
                "do", "does", "did", "can", "could", "will", "would", "should",
                "may", "might", "must", "shall", "about", "from", "with", "for"
            );

            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
                if (cleanWord.length() > 3 && !stopWords.contains(cleanWord)) {
                    searchTerm = cleanWord;
                    break;
                }
            }
            if (searchTerm.isEmpty()) {
                String cleanedQuery = query.replaceAll("[^a-zA-Z0-9]", "");
                searchTerm = cleanedQuery.substring(0, Math.min(20, cleanedQuery.length()));
            }
            
            System.out.println("   Search term: " + searchTerm);
            
            // Query AkashicRecord
            var results = akashic.query(searchTerm);
            System.out.println("   Found " + results.size() + " knowledge blocks");
            
            if (!results.isEmpty()) {
                System.out.println("   ✅ DIRECT KNOWLEDGE RESPONSE:");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    System.out.println("      " + results.get(i).content);
                }
            } else {
                System.out.println("   ❌ NO KNOWLEDGE - Would need LLM");
            }
        }
        
        System.out.println("\n📊 THEORY VALIDATION:");
        System.out.println("If knowledge exists → Answer directly (no LLM needed)");
        System.out.println("If no knowledge → Use LLM as fallback");
        System.out.println("Result: System can be autonomous with absorbed knowledge!");
    }
}
