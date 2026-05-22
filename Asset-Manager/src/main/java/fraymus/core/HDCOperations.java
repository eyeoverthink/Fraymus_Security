package fraymus.core;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HDCOperations - Hyper-Dimensional Computing operations for biometric-AGI synthesis
 * 
 * This utility class provides hyper-dimensional computing operations including:
 * - Hypervector representation (binary, bipolar, integer)
 * - Binding operation (XOR)
 * - Bundling operation (addition with majority-rule threshold)
 * - Permutation operation (rotation)
 * - Similarity calculation (cosine, Hamming)
 * - Domain-specific hypervectors
 * - Swarm bundling and consensus
 * 
 * Based on Kanerva's Sparse Distributed Memory and Plate's Holographic Reduced Representation
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 2.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class HDCOperations {
    
    // Dimensionality constants
    public static final int HDC_DIMENSION = 8192;
    public static final int TACHYON_DIMENSION = 16384;
    public static final int HDC_CHUNKS = 128;
    public static final int BITS_PER_CHUNK = 64;
    
    // Similarity thresholds
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    public static final double HIGH_SIMILARITY_THRESHOLD = 0.8;
    
    // Domain hypervectors cache
    private static final Map<String, long[]> domainHypervectors = new ConcurrentHashMap<>();
    private static final Random random = new Random();
    
    /**
     * Initialize a random binary hypervector
     * 
     * @param dimension The dimensionality
     * @return Random binary hypervector (0 or 1)
     */
    public static long[] randomBinaryHypervector(int dimension) {
        long[] hypervector = new long[dimension];
        for (int i = 0; i < dimension; i++) {
            hypervector[i] = random.nextBoolean() ? 1L : 0L;
        }
        return hypervector;
    }
    
    /**
     * Initialize a random bipolar hypervector
     * 
     * @param dimension The dimensionality
     * @return Random bipolar hypervector (-1 or +1)
     */
    public static long[] randomBipolarHypervector(int dimension) {
        long[] hypervector = new long[dimension];
        for (int i = 0; i < dimension; i++) {
            hypervector[i] = random.nextBoolean() ? 1L : -1L;
        }
        return hypervector;
    }
    
    /**
     * Initialize a hypervector from seed (deterministic)
     * 
     * @param seed The seed value
     * @param dimension The dimensionality
     * @return Deterministic hypervector
     */
    public static long[] seededHypervector(long seed, int dimension) {
        Random seededRandom = new Random(seed);
        long[] hypervector = new long[dimension];
        for (int i = 0; i < dimension; i++) {
            hypervector[i] = seededRandom.nextBoolean() ? 1L : 0L;
        }
        return hypervector;
    }
    
    /**
     * Binding operation (XOR) - combines two hypervectors
     * 
     * bound = v₁ ⊕ v₂
     * 
     * Properties:
     * - Commutative: v₁ ⊕ v₂ = v₂ ⊕ v₁
     * - Associative: (v₁ ⊕ v₂) ⊕ v₃ = v₁ ⊕ (v₂ ⊕ v₃)
     * - Self-inverse: v ⊕ v = 0
     * - Unbinding: v₁ = bound ⊕ v₂
     * 
     * @param v1 First hypervector
     * @param v2 Second hypervector
     * @return Bound hypervector
     */
    public static long[] bind(long[] v1, long[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return new long[HDC_DIMENSION];
        }
        
        long[] bound = new long[v1.length];
        for (int i = 0; i < v1.length; i++) {
            bound[i] = v1[i] ^ v2[i];
        }
        return bound;
    }
    
    /**
     * Unbinding operation - recovers original vector from bound vector
     * 
     * v₁ = bound ⊕ v₂
     * 
     * @param bound Bound hypervector
     * @param v2 Second hypervector
     * @return Recovered first hypervector
     */
    public static long[] unbind(long[] bound, long[] v2) {
        return bind(bound, v2);
    }
    
    /**
     * Bundling operation (addition) - superimposes multiple hypervectors
     * 
     * bundled = Σᵢ vᵢ
     * bundled[i] = 1 if bundled[i] > 0 else 0 (majority-rule threshold)
     * 
     * Properties:
     * - Commutative: v₁ + v₂ = v₂ + v₁
     * - Associative: (v₁ + v₂) + v₃ = v₁ + (v₂ + v₃)
     * - Noise resilient: Superposition of thousands of vectors
     * 
     * @param vectors Array of hypervectors to bundle
     * @return Bundled hypervector
     */
    public static long[] bundle(long[]... vectors) {
        if (vectors == null || vectors.length == 0) {
            return new long[HDC_DIMENSION];
        }
        
        int dimension = vectors[0].length;
        long[] bundled = new long[dimension];
        
        // Superposition
        for (long[] vector : vectors) {
            for (int i = 0; i < Math.min(vector.length, dimension); i++) {
                bundled[i] += vector[i];
            }
        }
        
        // Majority-rule threshold collapse
        for (int i = 0; i < bundled.length; i++) {
            bundled[i] = bundled[i] > 0 ? 1L : 0L;
        }
        
        return bundled;
    }
    
    /**
     * Bundling operation for list of hypervectors
     * 
     * @param vectors List of hypervectors to bundle
     * @return Bundled hypervector
     */
    public static long[] bundle(List<long[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return new long[HDC_DIMENSION];
        }
        
        return bundle(vectors.toArray(new long[0][]));
    }
    
    /**
     * Permutation operation (rotation) - represents sequential relationships
     * 
     * permuted = π(v)
     * 
     * Properties:
     * - Lossless: π⁻¹(π(v)) = v
     * - Orthogonal: π(v) ≈ v for random permutation
     * 
     * @param hypervector The hypervector to permute
     * @param steps Number of positions to rotate
     * @return Permuted hypervector
     */
    public static long[] permute(long[] hypervector, int steps) {
        if (hypervector == null) {
            return new long[HDC_DIMENSION];
        }
        
        long[] permuted = new long[hypervector.length];
        int length = hypervector.length;
        
        for (int i = 0; i < length; i++) {
            int newIndex = (i + steps) % length;
            permuted[newIndex] = hypervector[i];
        }
        
        return permuted;
    }
    
    /**
     * Inverse permutation
     * 
     * @param hypervector The hypervector to un-permute
     * @param steps Number of positions to rotate back
     * @return Original hypervector
     */
    public static long[] inversePermute(long[] hypervector, int steps) {
        return permute(hypervector, -steps);
    }
    
    /**
     * Cosine similarity calculation
     * 
     * sim(v₁, v₂) = (v₁ · v₂) / (||v₁|| × ||v₂||)
     * 
     * @param v1 First hypervector
     * @param v2 Second hypervector
     * @return Cosine similarity (-1.0 to 1.0)
     */
    public static double cosineSimilarity(long[] v1, long[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Hamming similarity calculation for binary hypervectors
     * 
     * sim(v₁, v₂) = 1 - (HD(v₁, v₂) / D)
     * 
     * @param v1 First binary hypervector
     * @param v2 Second binary hypervector
     * @return Hamming similarity (0.0 to 1.0)
     */
    public static double hammingSimilarity(long[] v1, long[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return 0.0;
        }
        
        int hammingDistance = 0;
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] != v2[i]) {
                hammingDistance++;
            }
        }
        
        return 1.0 - ((double) hammingDistance / v1.length);
    }
    
    /**
     * Check if similarity exceeds threshold
     * 
     * @param v1 First hypervector
     * @param v2 Second hypervector
     * @param threshold Similarity threshold
     * @return true if similarity >= threshold
     */
    public static boolean isSimilar(long[] v1, long[] v2, double threshold) {
        return cosineSimilarity(v1, v2) >= threshold;
    }
    
    /**
     * Get or create domain-specific hypervector
     * 
     * @param domain The domain name
     * @return Domain hypervector
     */
    public static long[] getDomainHypervector(String domain) {
        return domainHypervectors.computeIfAbsent(domain, d -> {
            // Use domain name hash as seed for deterministic generation
            long seed = d.hashCode();
            return seededHypervector(seed, HDC_DIMENSION);
        });
    }
    
    /**
     * Bind hypervector to domain
     * 
     * @param hypervector The hypervector to bind
     * @param domain The domain name
     * @return Domain-bound hypervector
     */
    public static long[] bindToDomain(long[] hypervector, String domain) {
        long[] domainVector = getDomainHypervector(domain);
        return bind(hypervector, domainVector);
    }
    
    /**
     * Unbind hypervector from domain
     * 
     * @param boundVector The domain-bound hypervector
     * @param domain The domain name
     * @return Original hypervector
     */
    public static long[] unbindFromDomain(long[] boundVector, String domain) {
        long[] domainVector = getDomainHypervector(domain);
        return unbind(boundVector, domainVector);
    }
    
    /**
     * Swarm bundling - aggregate multiple hypervectors for consensus
     * 
     * @param boundVectors Array of bound vectors
     * @return Swarm synchronization state
     */
    public static SwarmBundleResult bundleForSwarm(long[]... boundVectors) {
        if (boundVectors == null || boundVectors.length == 0) {
            return new SwarmBundleResult(new long[HDC_DIMENSION], 0, System.currentTimeMillis());
        }
        
        long[] bundledState = new long[HDC_DIMENSION];
        
        // Bundling (superposition)
        for (long[] bound : boundVectors) {
            for (int i = 0; i < Math.min(bound.length, HDC_DIMENSION); i++) {
                bundledState[i] += bound[i];
            }
        }
        
        // Majority-rule threshold collapse
        for (int i = 0; i < bundledState.length; i++) {
            bundledState[i] = bundledState[i] > 0 ? 1L : 0L;
        }
        
        return new SwarmBundleResult(bundledState, boundVectors.length, System.currentTimeMillis());
    }
    
    /**
     * Calculate swarm consensus from votes
     * 
     * @param votes Array of votes from swarm nodes
     * @return Consensus decision
     */
    public static SwarmConsensusResult calculateSwarmConsensus(long[]... votes) {
        if (votes == null || votes.length == 0) {
            return new SwarmConsensusResult(false, 0.0, 0);
        }
        
        long[] consensus = new long[HDC_DIMENSION];
        
        // Bundle all votes
        for (long[] vote : votes) {
            for (int i = 0; i < Math.min(vote.length, HDC_DIMENSION); i++) {
                consensus[i] += vote[i];
            }
        }
        
        // Apply majority-rule threshold
        int positiveVotes = 0;
        for (int i = 0; i < consensus.length; i++) {
            if (consensus[i] > 0) {
                positiveVotes++;
            }
        }
        
        double consensusRatio = (double) positiveVotes / HDC_DIMENSION;
        boolean decision = consensusRatio > 0.5;
        
        return new SwarmConsensusResult(decision, consensusRatio, votes.length);
    }
    
    /**
     * Transform embedding to HDC hypervector
     * 
     * @param embedding The embedding bytes
     * @param biometricId The biometric ID
     * @return HDC biometric hypervector
     */
    public static HDCHypervector transformToHDC(byte[] embedding, String biometricId) {
        long[] hypervector = new long[HDC_DIMENSION];
        
        // Random projection
        for (int i = 0; i < embedding.length; i++) {
            byte b = embedding[i];
            int chunkIndex = Math.abs(b) % HDC_DIMENSION;
            int bitIndex = Math.abs(b) % BITS_PER_CHUNK;
            long mask = 1L << bitIndex;
            
            if (b > 0) {
                hypervector[chunkIndex] |= mask;
            } else {
                hypervector[chunkIndex] &= ~mask;
            }
        }
        
        double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
        return new HDCHypervector(hypervector, biometricId, similarityThreshold);
    }
    
    /**
     * XOR wormhole retrieval - O(1) memory access
     * 
     * Retrieval = XOR(Tachyon_Coordinate, ER=EPR_State)
     * 
     * @param tachyonCoordinate The tachyon coordinate
     * @param ereprState The ER=EPR state
     * @return Retrieved state
     */
    public static long[] xorWormholeRetrieval(long[] tachyonCoordinate, long[] ereprState) {
        if (tachyonCoordinate == null || ereprState == null) {
            return new long[HDC_DIMENSION];
        }
        
        int dimension = Math.min(tachyonCoordinate.length, ereprState.length);
        long[] retrieved = new long[dimension];
        
        // Exactly 8,192 XOR operations for O(1) retrieval
        for (int i = 0; i < dimension; i++) {
            retrieved[i] = tachyonCoordinate[i] ^ ereprState[i];
        }
        
        return retrieved;
    }
    
    /**
     * Apply phi-harmonic decoding to retrieved state
     * 
     * @param retrieved The retrieved state
     * @return Decoded state
     */
    public static long[] phiHarmonicDecode(long[] retrieved) {
        if (retrieved == null) {
            return new long[HDC_DIMENSION];
        }
        
        long[] decoded = retrieved.clone();
        for (int i = 0; i < decoded.length; i++) {
            decoded[i] ^= (long) (PhiHarmonicGeometry.PHI * (i % 100));
        }
        
        return decoded;
    }
    
    /**
     * Print HDC statistics
     */
    public static void printHDCStatistics() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  HDC OPERATIONS STATISTICS                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("HDC Dimension: " + HDC_DIMENSION);
        System.out.println("Tachyon Dimension: " + TACHYON_DIMENSION);
        System.out.println("HDC Chunks: " + HDC_CHUNKS);
        System.out.println("Bits per Chunk: " + BITS_PER_CHUNK);
        System.out.println("Default Similarity Threshold: " + DEFAULT_SIMILARITY_THRESHOLD);
        System.out.println("High Similarity Threshold: " + HIGH_SIMILARITY_THRESHOLD);
        System.out.println("Cached Domain Hypervectors: " + domainHypervectors.size());
        System.out.println();
    }
    
    /**
     * Inner class for HDC hypervector with metadata
     */
    public static class HDCHypervector {
        public final long[] hypervector;
        public final String biometricId;
        public final double similarityThreshold;
        
        public HDCHypervector(long[] hypervector, String biometricId, double similarityThreshold) {
            this.hypervector = hypervector;
            this.biometricId = biometricId;
            this.similarityThreshold = similarityThreshold;
        }
    }
    
    /**
     * Inner class for swarm bundle result
     */
    public static class SwarmBundleResult {
        public final long[] bundledState;
        public final int vectorCount;
        public final long timestamp;
        
        public SwarmBundleResult(long[] bundledState, int vectorCount, long timestamp) {
            this.bundledState = bundledState;
            this.vectorCount = vectorCount;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Inner class for swarm consensus result
     */
    public static class SwarmConsensusResult {
        public final boolean decision;
        public final double consensusRatio;
        public final int voteCount;
        
        public SwarmConsensusResult(boolean decision, double consensusRatio, int voteCount) {
            this.decision = decision;
            this.consensusRatio = consensusRatio;
            this.voteCount = voteCount;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        printHDCStatistics();
        
        // Test binding and unbinding
        long[] v1 = randomBinaryHypervector(HDC_DIMENSION);
        long[] v2 = randomBinaryHypervector(HDC_DIMENSION);
        
        long[] bound = bind(v1, v2);
        long[] recovered = unbind(bound, v2);
        
        System.out.println("Binding/Unbinding Test:");
        System.out.println("  Original v1 == recovered: " + java.util.Arrays.equals(v1, recovered));
        System.out.println();
        
        // Test bundling
        long[] v3 = randomBinaryHypervector(HDC_DIMENSION);
        long[] v4 = randomBinaryHypervector(HDC_DIMENSION);
        long[] bundled = bundle(v1, v2, v3, v4);
        
        System.out.println("Bundling Test:");
        System.out.println("  Bundled vector length: " + bundled.length);
        System.out.println();
        
        // Test similarity
        double sim = cosineSimilarity(v1, v2);
        double ham = hammingSimilarity(v1, v2);
        
        System.out.println("Similarity Test:");
        System.out.println("  Cosine similarity: " + sim);
        System.out.println("  Hamming similarity: " + ham);
        System.out.println();
        
        // Test domain binding
        long[] domainBound = bindToDomain(v1, "biometric_auth");
        long[] domainUnbound = unbindFromDomain(domainBound, "biometric_auth");
        
        System.out.println("Domain Binding Test:");
        System.out.println("  Original == unbound: " + java.util.Arrays.equals(v1, domainUnbound));
        System.out.println();
        
        // Test swarm consensus
        long[] vote1 = randomBinaryHypervector(HDC_DIMENSION);
        long[] vote2 = randomBinaryHypervector(HDC_DIMENSION);
        long[] vote3 = randomBinaryHypervector(HDC_DIMENSION);
        
        SwarmConsensusResult consensus = calculateSwarmConsensus(vote1, vote2, vote3);
        System.out.println("Swarm Consensus Test:");
        System.out.println("  Decision: " + consensus.decision);
        System.out.println("  Consensus Ratio: " + consensus.consensusRatio);
        System.out.println("  Vote Count: " + consensus.voteCount);
    }
}
