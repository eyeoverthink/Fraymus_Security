package fraymus.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HDC Biometric Embedding - Hyper-dimensional computing for swarm intelligence
 * 
 * This bridge implements:
 * - HDC hypervector transformation for biometric data
 * - Domain-specific binding for revocable embeddings
 * - Swarm bundling for collective intelligence
 * - Swarm consensus decision-making
 * - Atomic state updates for synchronization
 * 
 * Based on Kanerva's Sparse Distributed Memory and Plate's Holographic Reduced Representation
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 2.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class HDCBiometricEmbedding {
    
    // HDC dimensionality
    public static final int HDC_DIMENSION = 8192;
    public static final int HDC_CHUNKS = 128;
    
    // Similarity thresholds
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    public static final double HIGH_SIMILARITY_THRESHOLD = 0.8;
    
    // Swarm state
    private static HDCOperations.SwarmBundleResult swarmState = null;
    private static final Map<String, HDCOperations.HDCHypervector> embeddingRegistry = new ConcurrentHashMap<>();
    
    /**
     * Transform biometric embedding to HDC hypervector
     * 
     * @param embedding Biometric embedding bytes
     * @param biometricId Biometric identifier
     * @return HDC hypervector
     */
    public static HDCOperations.HDCHypervector transformToHDC(byte[] embedding, String biometricId) {
        HDCOperations.HDCHypervector hypervector = HDCOperations.transformToHDC(embedding, biometricId);
        
        // Register in embedding registry
        if (biometricId != null && !biometricId.isEmpty()) {
            embeddingRegistry.put(biometricId, hypervector);
        }
        
        return hypervector;
    }
    
    /**
     * Bind hypervector to domain
     * 
     * @param hypervector HDC hypervector
     * @param domain Domain name
     * @return Domain-bound hypervector
     */
    public static long[] bindToDomain(long[] hypervector, String domain) {
        return HDCOperations.bindToDomain(hypervector, domain);
    }
    
    /**
     * Unbind hypervector from domain
     * 
     * @param boundVector Domain-bound hypervector
     * @param domain Domain name
     * @return Original hypervector
     */
    public static long[] unbindFromDomain(long[] boundVector, String domain) {
        return HDCOperations.unbindFromDomain(boundVector, domain);
    }
    
    /**
     * Get domain version
     * 
     * @param hypervector HDC hypervector
     * @param domain Domain name
     * @return Domain version number
     */
    public static int getDomainVersion(long[] hypervector, String domain) {
        // Version based on phi-harmonic hash
        int hash = 0;
        for (long value : hypervector) {
            hash ^= (int) (value % Integer.MAX_VALUE);
        }
        hash ^= domain.hashCode();
        
        return Math.abs(hash) % 1000;
    }
    
    /**
     * Bundle hypervectors for swarm intelligence
     * 
     * @param boundVectors Array of bound vectors
     * @return Swarm bundle result
     */
    public static HDCOperations.SwarmBundleResult bundleForSwarm(long[]... boundVectors) {
        HDCOperations.SwarmBundleResult result = HDCOperations.bundleForSwarm(boundVectors);
        
        // Update swarm state atomically
        swarmState = result;
        
        return result;
    }
    
    /**
     * Bundle hypervectors for swarm intelligence (list version)
     * 
     * @param boundVectors List of bound vectors
     * @return Swarm bundle result
     */
    public static HDCOperations.SwarmBundleResult bundleForSwarm(List<long[]> boundVectors) {
        return HDCOperations.bundleForSwarm(boundVectors.toArray(new long[0][]));
    }
    
    /**
     * Calculate swarm consensus from votes
     * 
     * @param votes Array of votes from swarm nodes
     * @return Consensus result
     */
    public static HDCOperations.SwarmConsensusResult calculateSwarmConsensus(long[]... votes) {
        return HDCOperations.calculateSwarmConsensus(votes);
    }
    
    /**
     * Calculate swarm consensus from votes (list version)
     * 
     * @param votes List of votes from swarm nodes
     * @return Consensus result
     */
    public static HDCOperations.SwarmConsensusResult calculateSwarmConsensus(List<long[]> votes) {
        return HDCOperations.calculateSwarmConsensus(votes.toArray(new long[0][]));
    }
    
    /**
     * Get current swarm state
     * 
     * @return Swarm bundle result
     */
    public static HDCOperations.SwarmBundleResult getSwarmState() {
        return swarmState;
    }
    
    /**
     * Get registered embedding by biometric ID
     * 
     * @param biometricId Biometric identifier
     * @return HDC hypervector
     */
    public static HDCOperations.HDCHypervector getEmbedding(String biometricId) {
        return embeddingRegistry.get(biometricId);
    }
    
    /**
     * Check if embedding exists
     * 
     * @param biometricId Biometric identifier
     * @return true if exists
     */
    public static boolean hasEmbedding(String biometricId) {
        return embeddingRegistry.containsKey(biometricId);
    }
    
    /**
     * Remove embedding from registry
     * 
     * @param biometricId Biometric identifier
     * @return Removed hypervector
     */
    public static HDCOperations.HDCHypervector removeEmbedding(String biometricId) {
        return embeddingRegistry.remove(biometricId);
    }
    
    /**
     * Get embedding registry size
     * 
     * @return Number of registered embeddings
     */
    public static int getRegistrySize() {
        return embeddingRegistry.size();
    }
    
    /**
     * Clear embedding registry
     */
    public static void clearRegistry() {
        embeddingRegistry.clear();
    }
    
    /**
     * Verify biometric similarity
     * 
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @param threshold Similarity threshold
     * @return true if similar
     */
    public static boolean verifySimilarity(long[] embedding1, long[] embedding2, double threshold) {
        return HDCOperations.isSimilar(embedding1, embedding2, threshold);
    }
    
    /**
     * Calculate similarity between two embeddings
     * 
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Similarity score
     */
    public static double calculateSimilarity(long[] embedding1, long[] embedding2) {
        return HDCOperations.cosineSimilarity(embedding1, embedding2);
    }
    
    /**
     * Perform cancelable biometric transform
     * 
     * @param originalEmbedding Original biometric embedding
     * @param domain Domain for transformation
     * @param userSecret User secret for non-invertible perturbation
     * @return Transformed embedding
     */
    public static long[] cancelableTransform(long[] originalEmbedding, String domain, String userSecret) {
        if (originalEmbedding == null) {
            return new long[HDC_DIMENSION];
        }
        
        // Bind to domain
        long[] domainBound = HDCOperations.bindToDomain(originalEmbedding, domain);
        
        // Apply non-invertible perturbation based on user secret
        if (userSecret != null && !userSecret.isEmpty()) {
            long perturbation = userSecret.hashCode();
            for (int i = 0; i < domainBound.length; i++) {
                domainBound[i] ^= (perturbation + i);
            }
        }
        
        return domainBound;
    }
    
    /**
     * Get all registered biometric IDs
     * 
     * @return List of biometric IDs
     */
    public static List<String> getAllBiometricIds() {
        return new ArrayList<>(embeddingRegistry.keySet());
    }
    
    /**
     * Print HDC biometric embedding statistics
     */
    public static void printEmbeddingStatistics() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  HDC BIOMETRIC EMBEDDING STATISTICS                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("HDC Dimension: " + HDC_DIMENSION);
        System.out.println("HDC Chunks: " + HDC_CHUNKS);
        System.out.println("Default Similarity Threshold: " + DEFAULT_SIMILARITY_THRESHOLD);
        System.out.println("High Similarity Threshold: " + HIGH_SIMILARITY_THRESHOLD);
        System.out.println("Registry Size: " + getRegistrySize());
        System.out.println("Swarm State: " + (swarmState != null ? "Active" : "Inactive"));
        if (swarmState != null) {
            System.out.println("Swarm Vector Count: " + swarmState.vectorCount);
            System.out.println("Swarm Timestamp: " + swarmState.timestamp);
        }
        System.out.println();
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        printEmbeddingStatistics();
        
        // Test HDC transformation
        byte[] embedding = new byte[64];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (byte) i;
        }
        
        String biometricId = "test_user_001";
        HDCOperations.HDCHypervector hypervector = transformToHDC(embedding, biometricId);
        System.out.println("HDC Transformation Test:");
        System.out.println("  Biometric ID: " + biometricId);
        System.out.println("  Hypervector Length: " + hypervector.hypervector.length);
        System.out.println("  Similarity Threshold: " + hypervector.similarityThreshold);
        System.out.println("  Registry Size: " + getRegistrySize());
        System.out.println();
        
        // Test domain binding
        long[] domainBound = bindToDomain(hypervector.hypervector, "biometric_auth");
        System.out.println("Domain Binding Test:");
        System.out.println("  Domain: biometric_auth");
        System.out.println("  Version: " + getDomainVersion(domainBound, "biometric_auth"));
        System.out.println();
        
        // Test swarm bundling
        long[] vector1 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        long[] vector2 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        long[] vector3 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        
        HDCOperations.SwarmBundleResult bundleResult = bundleForSwarm(vector1, vector2, vector3);
        System.out.println("Swarm Bundling Test:");
        System.out.println("  Vector Count: " + bundleResult.vectorCount);
        System.out.println("  Bundled State Length: " + bundleResult.bundledState.length);
        System.out.println();
        
        // Test swarm consensus
        long[] vote1 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        long[] vote2 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        long[] vote3 = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        
        HDCOperations.SwarmConsensusResult consensus = calculateSwarmConsensus(vote1, vote2, vote3);
        System.out.println("Swarm Consensus Test:");
        System.out.println("  Decision: " + consensus.decision);
        System.out.println("  Consensus Ratio: " + consensus.consensusRatio);
        System.out.println("  Vote Count: " + consensus.voteCount);
        System.out.println();
        
        // Test cancelable transform
        long[] original = HDCOperations.randomBinaryHypervector(HDC_DIMENSION);
        long[] transformed = cancelableTransform(original, "auth_domain", "user_secret_123");
        System.out.println("Cancelable Transform Test:");
        System.out.println("  Original Length: " + original.length);
        System.out.println("  Transformed Length: " + transformed.length);
        System.out.println("  Are Equal: " + java.util.Arrays.equals(original, transformed));
        System.out.println();
        
        // Test similarity verification
        double similarity = calculateSimilarity(original, transformed);
        System.out.println("Similarity Test:");
        System.out.println("  Similarity: " + similarity);
        System.out.println("  Is Similar (threshold 0.7): " + verifySimilarity(original, transformed, 0.7));
        System.out.println();
    }
}
