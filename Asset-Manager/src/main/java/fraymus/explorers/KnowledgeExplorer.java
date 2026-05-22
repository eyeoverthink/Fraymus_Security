package fraymus.explorers;

import fraymus.knowledge.AkashicRecord;
import java.util.List;
import java.util.Scanner;

/**
 * KNOWLEDGE EXPLORER
 * 
 * Interactive knowledge base explorer that queries the AkashicRecord.
 * Allows users to search and retrieve relevant information.
 * 
 * "Tap into the vast knowledge stored within the AkashicRecord."
 */
public class KnowledgeExplorer {
    
    private final AkashicRecord akashicRecord;
    private final Scanner keyboard;
    private final TextDisplay display;
    
    public KnowledgeExplorer(AkashicRecord akashicRecord) {
        this.akashicRecord = akashicRecord;
        this.keyboard = new Scanner(System.in);
        this.display = new TextDisplay();
    }
    
    /**
     * Start interactive exploration loop
     */
    public void start() {
        display.print("╔═══════════════════════════════════════════════════════════════╗");
        display.print("║           KNOWLEDGE EXPLORER                                   ║");
        display.print("║           Query the Akashic Record                            ║");
        display.print("╚═══════════════════════════════════════════════════════════════╝");
        display.print("");
        
        while (true) {
            display.print("Enter your query (or 'exit' to quit): ");
            String userQuery = keyboard.nextLine();
            
            if (userQuery.equalsIgnoreCase("exit")) {
                display.print("Exiting Knowledge Explorer...");
                break;
            }
            
            List<KnowledgeChunk> results = search(userQuery);
            displayResults(results);
        }
    }
    
    /**
     * Search the AkashicRecord for relevant knowledge
     */
    public List<KnowledgeChunk> search(String query) {
        // Query the AkashicRecord using the provided query
        // Return a list of relevant KnowledgeChunks
        List<AkashicRecord.KnowledgeBlock> blocks = akashicRecord.query(query);
        
        // Convert to KnowledgeChunk format
        List<KnowledgeChunk> chunks = new java.util.ArrayList<>();
        for (AkashicRecord.KnowledgeBlock block : blocks) {
            chunks.add(new KnowledgeChunk(block.category, block.content, 1.0));
        }
        return chunks;
    }
    
    /**
     * Display search results
     */
    private void displayResults(List<KnowledgeChunk> results) {
        if (results.isEmpty()) {
            display.print("No results found.");
            return;
        }
        
        display.print("");
        display.print("═══════════════════════════════════════════════════════════════");
        display.print("RESULTS (" + results.size() + " found):");
        display.print("═══════════════════════════════════════════════════════════════");
        
        for (int i = 0; i < results.size(); i++) {
            KnowledgeChunk chunk = results.get(i);
            display.print("");
            display.print("[" + (i + 1) + "] " + chunk.getCategory());
            display.print("    " + chunk.getContent());
            display.print("    Relevance: " + chunk.getRelevance());
        }
        
        display.print("═══════════════════════════════════════════════════════════════");
        display.print("");
    }
    
    /**
     * Text display interface
     */
    private static class TextDisplay {
        public void print(String text) {
            System.out.println(text);
        }
    }
    
    /**
     * Knowledge chunk wrapper
     */
    public static class KnowledgeChunk {
        private final String category;
        private final String content;
        private final double relevance;
        
        public KnowledgeChunk(String category, String content, double relevance) {
            this.category = category;
            this.content = content;
            this.relevance = relevance;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getContent() {
            return content;
        }
        
        public double getRelevance() {
            return relevance;
        }
    }
}
