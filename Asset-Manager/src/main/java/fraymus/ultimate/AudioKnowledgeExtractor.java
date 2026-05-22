package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * AudioKnowledgeExtractor - Knowledge Extraction from Audio Streams
 * 
 * Extracts knowledge, concepts, and insights from audio streams.
 * Integrates with AUM agency for continuous learning from audio.
 * 
 * Features:
 * - Concept extraction from speech
 * - Topic modeling
 * - Sentiment analysis
 * - Knowledge graph construction
 * - Phi-harmonic knowledge scoring
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class AudioKnowledgeExtractor {
    
    private static final double PHI = 1.618033988749895;
    
    // Knowledge node
    public static class KnowledgeNode {
        private String concept;
        private double relevance;
        private double phiScore;
        private List<String> relatedConcepts;
        private int occurrenceCount;
        private long firstSeen;
        private long lastSeen;
        
        public KnowledgeNode(String concept) {
            this.concept = concept;
            this.relevance = 0.0;
            this.phiScore = 0.0;
            this.relatedConcepts = new ArrayList<>();
            this.occurrenceCount = 0;
            this.firstSeen = System.currentTimeMillis();
            this.lastSeen = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getConcept() { return concept; }
        public double getRelevance() { return relevance; }
        public void setRelevance(double relevance) { this.relevance = relevance; }
        public double getPhiScore() { return phiScore; }
        public void setPhiScore(double phiScore) { this.phiScore = phiScore; }
        public List<String> getRelatedConcepts() { return relatedConcepts; }
        public void addRelatedConcept(String concept) { relatedConcepts.add(concept); }
        public int getOccurrenceCount() { return occurrenceCount; }
        public void incrementOccurrence() { occurrenceCount++; }
        public long getFirstSeen() { return firstSeen; }
        public long getLastSeen() { return lastSeen; }
        public void updateLastSeen() { lastSeen = System.currentTimeMillis(); }
    }
    
    // Knowledge extraction result
    public static class ExtractionResult {
        private List<String> concepts;
        private String dominantTopic;
        private double sentiment;
        private Map<String, Double> topicDistribution;
        private double phiHarmony;
        
        public ExtractionResult() {
            this.concepts = new ArrayList<>();
            this.topicDistribution = new HashMap<>();
        }
        
        // Getters and setters
        public List<String> getConcepts() { return concepts; }
        public String getDominantTopic() { return dominantTopic; }
        public void setDominantTopic(String dominantTopic) { this.dominantTopic = dominantTopic; }
        public double getSentiment() { return sentiment; }
        public void setSentiment(double sentiment) { this.sentiment = sentiment; }
        public Map<String, Double> getTopicDistribution() { return topicDistribution; }
        public double getPhiHarmony() { return phiHarmony; }
        public void setPhiHarmony(double phiHarmony) { this.phiHarmony = phiHarmony; }
    }
    
    // Knowledge state
    private Map<String, KnowledgeNode> knowledgeGraph;
    private Map<String, Double> topicFrequencies;
    private List<ExtractionResult> extractionHistory;
    private int extractionCount;
    
    // Sentiment lexicon (simplified)
    private Map<String, Double> sentimentLexicon;
    
    public AudioKnowledgeExtractor() {
        this.knowledgeGraph = new ConcurrentHashMap<>();
        this.topicFrequencies = new ConcurrentHashMap<>();
        this.extractionHistory = new ArrayList<>();
        this.extractionCount = 0;
        this.sentimentLexicon = new ConcurrentHashMap<>();
        
        // Initialize sentiment lexicon
        initializeSentimentLexicon();
    }
    
    /**
     * Initialize sentiment lexicon
     */
    private void initializeSentimentLexicon() {
        // Positive words
        sentimentLexicon.put("good", 1.0);
        sentimentLexicon.put("great", 1.0);
        sentimentLexicon.put("excellent", 1.0);
        sentimentLexicon.put("happy", 1.0);
        sentimentLexicon.put("love", 1.0);
        sentimentLexicon.put("amazing", 1.0);
        
        // Negative words
        sentimentLexicon.put("bad", -1.0);
        sentimentLexicon.put("terrible", -1.0);
        sentimentLexicon.put("sad", -1.0);
        sentimentLexicon.put("hate", -1.0);
        sentimentLexicon.put("angry", -1.0);
        sentimentLexicon.put("worst", -1.0);
    }
    
    /**
     * Extract knowledge from text
     */
    public ExtractionResult extractKnowledge(String text) {
        ExtractionResult result = new ExtractionResult();
        extractionCount++;
        
        // Extract concepts
        List<String> concepts = extractConcepts(text);
        result.getConcepts().addAll(concepts);
        
        // Update knowledge graph
        updateKnowledgeGraph(concepts);
        
        // Analyze topics
        String dominantTopic = analyzeTopics(text);
        result.setDominantTopic(dominantTopic);
        
        // Analyze sentiment
        double sentiment = analyzeSentiment(text);
        result.setSentiment(sentiment);
        
        // Compute topic distribution
        result.getTopicDistribution().putAll(computeTopicDistribution(text));
        
        // Compute phi harmony
        double phiHarmony = computePhiHarmony(concepts);
        result.setPhiHarmony(phiHarmony);
        
        // Store in history
        extractionHistory.add(result);
        
        return result;
    }
    
    /**
     * Extract concepts from text
     */
    private List<String> extractConcepts(String text) {
        List<String> concepts = new ArrayList<>();
        String[] words = text.toLowerCase().split("\\s+");
        
        // Extract significant words (length > 3)
        for (String word : words) {
            if (word.length() > 3) {
                concepts.add(word);
            }
        }
        
        // Phi-harmonic filtering
        List<String> filtered = new ArrayList<>();
        for (int i = 0; i < concepts.size(); i++) {
            double phiWeight = Math.pow(PHI, (i % 7) / 7.0);
            if (phiWeight > 1.0) {
                filtered.add(concepts.get(i));
            }
        }
        
        return filtered;
    }
    
    /**
     * Update knowledge graph
     */
    private void updateKnowledgeGraph(List<String> concepts) {
        for (int i = 0; i < concepts.size(); i++) {
            String concept = concepts.get(i);
            
            KnowledgeNode node = knowledgeGraph.get(concept);
            if (node == null) {
                node = new KnowledgeNode(concept);
                knowledgeGraph.put(concept, node);
            }
            
            node.incrementOccurrence();
            node.updateLastSeen();
            
            // Update phi score based on occurrence
            node.setPhiScore(Math.log(1 + node.getOccurrenceCount()) * PHI);
            
            // Add related concepts
            for (int j = 0; j < concepts.size(); j++) {
                if (i != j) {
                    node.addRelatedConcept(concepts.get(j));
                }
            }
        }
    }
    
    /**
     * Analyze topics
     */
    private String analyzeTopics(String text) {
        // Simplified topic analysis
        String[] words = text.toLowerCase().split("\\s+");
        
        // Topic keywords
        Map<String, String> topicKeywords = new HashMap<>();
        topicKeywords.put("technology", "technology tech computer software");
        topicKeywords.put("science", "science research experiment data");
        topicKeywords.put("philosophy", "philosophy meaning life consciousness");
        topicKeywords.put("art", "art creative design visual");
        topicKeywords.put("music", "music sound audio melody");
        
        // Score each topic
        String bestTopic = "general";
        double bestScore = 0;
        
        for (Map.Entry<String, String> entry : topicKeywords.entrySet()) {
            String topic = entry.getKey();
            String keywords = entry.getValue();
            
            double score = 0;
            for (String word : words) {
                if (keywords.contains(word)) {
                    score += 1.0;
                }
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTopic = topic;
            }
        }
        
        return bestTopic;
    }
    
    /**
     * Analyze sentiment
     */
    private double analyzeSentiment(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        double sentiment = 0;
        int count = 0;
        
        for (String word : words) {
            Double wordSentiment = sentimentLexicon.get(word);
            if (wordSentiment != null) {
                sentiment += wordSentiment;
                count++;
            }
        }
        
        return count > 0 ? sentiment / count : 0.0;
    }
    
    /**
     * Compute topic distribution
     */
    private Map<String, Double> computeTopicDistribution(String text) {
        Map<String, Double> distribution = new HashMap<>();
        
        String[] topics = {"technology", "science", "philosophy", "art", "music", "general"};
        for (String topic : topics) {
            distribution.put(topic, 0.0);
        }
        
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            for (String topic : topics) {
                if (topic.contains(word) || word.contains(topic.substring(0, 3))) {
                    distribution.put(topic, distribution.get(topic) + 1.0);
                }
            }
        }
        
        // Normalize
        double total = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            for (String topic : topics) {
                distribution.put(topic, distribution.get(topic) / total);
            }
        }
        
        return distribution;
    }
    
    /**
     * Compute phi harmony of concepts
     */
    private double computePhiHarmony(List<String> concepts) {
        if (concepts.isEmpty()) return 0.0;
        
        double harmony = 0;
        for (int i = 0; i < concepts.size(); i++) {
            String concept = concepts.get(i);
            KnowledgeNode node = knowledgeGraph.get(concept);
            
            if (node != null) {
                harmony += node.getPhiScore();
            } else {
                harmony += PHI; // New concept gets phi score
            }
        }
        
        return harmony / concepts.size();
    }
    
    /**
     * Get knowledge node
     */
    public KnowledgeNode getKnowledgeNode(String concept) {
        return knowledgeGraph.get(concept);
    }
    
    /**
     * Get all knowledge nodes
     */
    public Map<String, KnowledgeNode> getKnowledgeGraph() {
        return new HashMap<>(knowledgeGraph);
    }
    
    /**
     * Get top concepts by occurrence
     */
    public List<String> getTopConcepts(int limit) {
        return knowledgeGraph.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().getOccurrenceCount(), a.getValue().getOccurrenceCount()))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get extraction statistics
     */
    public String getExtractionStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AUDIO KNOWLEDGE EXTRACTOR STATISTICS ===\n");
        sb.append("Extraction Count: ").append(extractionCount).append("\n");
        sb.append("Knowledge Graph Size: ").append(knowledgeGraph.size()).append("\n");
        sb.append("Extraction History: ").append(extractionHistory.size()).append("\n");
        sb.append("\n=== TOP CONCEPTS ===\n");
        List<String> topConcepts = getTopConcepts(10);
        for (int i = 0; i < topConcepts.size(); i++) {
            String concept = topConcepts.get(i);
            KnowledgeNode node = knowledgeGraph.get(concept);
            sb.append(String.format("%d. %s (%d occurrences, Phi=%.3f)\n", 
                              i + 1, concept, node.getOccurrenceCount(), node.getPhiScore()));
        }
        return sb.toString();
    }
    
    /**
     * Clear extraction history
     */
    public void clearHistory() {
        extractionHistory.clear();
    }
}
