package fraymus.core;

import java.util.ArrayList;
import java.util.List;

/**
 * PhiHarmonicGeometry - Phi-harmonic mathematical operations for biometric-AGI synthesis
 * 
 * This utility class provides phi-harmonic calculations including:
 * - Golden ratio (φ) constants and operations
 * - Golden angle calculations
 * - Phi-resonance calculations
 * - Phi-enhanced Douglas-Peucker algorithm for trajectory simplification
 * - Tachyon coordinate transformations
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 2.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class PhiHarmonicGeometry {
    
    // Phi constants
    public static final double PHI = 1.618033988749895;
    public static final double PHI_SQUARED = 2.618033988749895;
    public static final double PHI_INVERSE = 0.618033988749895;
    public static final double GOLDEN_ANGLE_DEGREES = 137.5077640500378546463487;
    public static final double GOLDEN_ANGLE_RADIANS = 2.39996322972865332;
    
    // Optimal resonance threshold
    public static final double OPTIMAL_RESONANCE = 0.756;
    
    // Tachyon dimensionality
    public static final int TACHYON_DIMENSION = 16384;
    public static final int HDC_DIMENSION = 8192;
    public static final int HDC_CHUNKS = 128;
    
    /**
     * Calculate phi power: φ^k
     * 
     * @param power The exponent
     * @return φ raised to the given power
     */
    public static double phiPower(double power) {
        return Math.pow(PHI, power);
    }
    
    /**
     * Calculate phi inverse power: (1/φ)^k
     * 
     * @param power The exponent
     * @return φ inverse raised to the given power
     */
    public static double phiInversePower(double power) {
        return Math.pow(PHI_INVERSE, power);
    }
    
    /**
     * Calculate phi-resonance for a set of facial ratios
     * 
     * R = Σᵢ (|fᵢ - φ| / φ) / N
     * 
     * @param facialRatios Array of facial ratios
     * @return Phi-resonance score (lower is better, optimal ≈ 0.756)
     */
    public static double calculatePhiResonance(double[] facialRatios) {
        if (facialRatios == null || facialRatios.length == 0) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (double ratio : facialRatios) {
            sum += Math.abs(ratio - PHI) / PHI;
        }
        
        return sum / facialRatios.length;
    }
    
    /**
     * Calculate phi-resonance for a list of facial ratios
     * 
     * @param facialRatios List of facial ratios
     * @return Phi-resonance score
     */
    public static double calculatePhiResonance(List<Double> facialRatios) {
        if (facialRatios == null || facialRatios.isEmpty()) {
            return 0.0;
        }
        
        double[] array = facialRatios.stream().mapToDouble(Double::doubleValue).toArray();
        return calculatePhiResonance(array);
    }
    
    /**
     * Check if phi-resonance is optimal
     * 
     * @param resonance The phi-resonance score
     * @return true if resonance is within optimal range
     */
    public static boolean isOptimalResonance(double resonance) {
        return Math.abs(resonance - OPTIMAL_RESONANCE) < 0.1;
    }
    
    /**
     * Calculate golden angle chunk index for hypervector mapping
     * 
     * @param index The item index
     * @param numChunks Number of chunks
     * @return Chunk index based on golden angle
     */
    public static int goldenAngleChunkIndex(int index, int numChunks) {
        return (int) ((index * GOLDEN_ANGLE_DEGREES) % numChunks);
    }
    
    /**
     * Calculate golden angle rotation steps
     * 
     * @param deviation Golden angle deviation
     * @param multiplier Rotation multiplier
     * @return Number of rotation steps
     */
    public static int goldenAngleRotationSteps(double deviation, int multiplier) {
        return (int) (deviation * multiplier);
    }
    
    /**
     * Phi-enhanced Douglas-Peucker algorithm for trajectory simplification
     * 
     * ε = φ × d_max
     * 
     * @param points List of points (each point is a double array)
     * @return Simplified list of points
     */
    public static List<double[]> phiDouglasPeucker(List<double[]> points) {
        if (points == null || points.size() <= 2) {
            return new ArrayList<>(points);
        }
        
        // Calculate max distance
        double dMax = 0.0;
        int index = 0;
        int end = points.size() - 1;
        
        for (int i = 1; i < end; i++) {
            double d = perpendicularDistance(points.get(i), points.get(0), points.get(end));
            if (d > dMax) {
                dMax = d;
                index = i;
            }
        }
        
        // Phi-enhanced epsilon threshold
        double epsilon = PHI * dMax;
        
        if (dMax > epsilon) {
            List<double[]> rec1 = phiDouglasPeucker(points.subList(0, index + 1));
            List<double[]> rec2 = phiDouglasPeucker(points.subList(index, end + 1));
            
            // Combine results (avoid duplicate at index)
            List<double[]> result = new ArrayList<>(rec1);
            result.addAll(rec2.subList(1, rec2.size()));
            return result;
        } else {
            List<double[]> result = new ArrayList<>();
            result.add(points.get(0));
            result.add(points.get(end));
            return result;
        }
    }
    
    /**
     * Calculate perpendicular distance from point to line
     * 
     * @param point The point
     * @param lineStart Start of line
     * @param lineEnd End of line
     * @return Perpendicular distance
     */
    private static double perpendicularDistance(double[] point, double[] lineStart, double[] lineEnd) {
        if (point.length < 2 || lineStart.length < 2 || lineEnd.length < 2) {
            return 0.0;
        }
        
        double x0 = point[0];
        double y0 = point[1];
        double x1 = lineStart[0];
        double y1 = lineStart[1];
        double x2 = lineEnd[0];
        double y2 = lineEnd[1];
        
        // Line equation: Ax + By + C = 0
        double A = y1 - y2;
        double B = x2 - x1;
        double C = x1 * y2 - x2 * y1;
        
        // Perpendicular distance: |Ax0 + By0 + C| / sqrt(A² + B²)
        return Math.abs(A * x0 + B * y0 + C) / Math.sqrt(A * A + B * B);
    }
    
    /**
     * Calculate phi-harmonic coordinate for tachyon mapping
     * 
     * Tachyon_Coordinate = φ^k × Feature_Vector
     * 
     * @param featureVector The feature vector
     * @param phiResonance Phi-resonance score
     * @return Phi-harmonic coordinate
     */
    public static double[] calculateTachyonCoordinate(double[] featureVector, double phiResonance) {
        if (featureVector == null) {
            return new double[TACHYON_DIMENSION];
        }
        
        double phiPower = Math.pow(PHI, phiResonance * 10);
        double[] coordinate = new double[TACHYON_DIMENSION];
        
        for (int i = 0; i < Math.min(featureVector.length, TACHYON_DIMENSION); i++) {
            coordinate[i] = phiPower * featureVector[i];
        }
        
        return coordinate;
    }
    
    /**
     * Apply phi-harmonic symmetry modulation
     * 
     * @param hypervector The hypervector to modulate
     * @param symmetryScore Symmetry score (0.0 to 1.0)
     * @return Modulated hypervector
     */
    public static long[] applyPhiSymmetryModulation(long[] hypervector, double symmetryScore) {
        if (hypervector == null) {
            return new long[HDC_DIMENSION];
        }
        
        long[] result = hypervector.clone();
        long modulation = (long) (symmetryScore * Long.MAX_VALUE);
        
        for (int i = 0; i < result.length; i++) {
            if (i % GOLDEN_ANGLE_DEGREES == 0) {
                result[i] ^= modulation;
            }
        }
        
        return result;
    }
    
    /**
     * Rotate hypervector by golden angle
     * 
     * @param hypervector The hypervector to rotate
     * @param rotationSteps Number of rotation steps
     * @return Rotated hypervector
     */
    public static long[] rotateHypervector(long[] hypervector, int rotationSteps) {
        if (hypervector == null) {
            return new long[HDC_DIMENSION];
        }
        
        long[] result = new long[hypervector.length];
        int length = hypervector.length;
        
        for (int i = 0; i < length; i++) {
            int newIndex = (i + rotationSteps) % length;
            result[newIndex] = hypervector[i];
        }
        
        return result;
    }
    
    /**
     * Calculate phi-harmonic similarity between two vectors
     * 
     * @param v1 First vector
     * @param v2 Second vector
     * @return Similarity score (0.0 to 1.0)
     */
    public static double phiHarmonicSimilarity(double[] v1, double[] v2) {
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
        
        double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        
        // Apply phi-harmonic weighting
        return cosineSimilarity * PHI_INVERSE;
    }
    
    /**
     * Calculate fractal scale for hierarchical level
     * 
     * scale = φ^(-i)
     * 
     * @param level The hierarchical level (0-based)
     * @return Fractal scale
     */
    public static double fractalScale(int level) {
        return Math.pow(PHI_INVERSE, level);
    }
    
    /**
     * Calculate coupling strength between scales
     * 
     * coupling = φ^(-|i-j|)
     * 
     * @param level1 First level
     * @param level2 Second level
     * @return Coupling strength
     */
    public static double couplingStrength(int level1, int level2) {
        return Math.pow(PHI_INVERSE, Math.abs(level1 - level2));
    }
    
    /**
     * Print phi statistics
     */
    public static void printPhiStatistics() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  PHI-HARMONIC GEOMETRY STATISTICS                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Phi (φ): " + PHI);
        System.out.println("Phi²: " + PHI_SQUARED);
        System.out.println("1/φ: " + PHI_INVERSE);
        System.out.println("Golden Angle (degrees): " + GOLDEN_ANGLE_DEGREES);
        System.out.println("Golden Angle (radians): " + GOLDEN_ANGLE_RADIANS);
        System.out.println("Optimal Resonance: " + OPTIMAL_RESONANCE);
        System.out.println("Tachyon Dimension: " + TACHYON_DIMENSION);
        System.out.println("HDC Dimension: " + HDC_DIMENSION);
        System.out.println("HDC Chunks: " + HDC_CHUNKS);
        System.out.println();
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        printPhiStatistics();
        
        // Test phi-resonance calculation
        double[] facialRatios = {1.6, 1.62, 1.58, 1.59, 1.61};
        double resonance = calculatePhiResonance(facialRatios);
        System.out.println("Phi-Resonance Test: " + resonance);
        System.out.println("Is Optimal: " + isOptimalResonance(resonance));
        System.out.println();
        
        // Test phi power
        System.out.println("Phi^5: " + phiPower(5));
        System.out.println("(1/φ)^3: " + phiInversePower(3));
        System.out.println();
        
        // Test fractal scale
        for (int i = 0; i < 5; i++) {
            System.out.println("Fractal Scale Level " + i + ": " + fractalScale(i));
        }
        System.out.println();
        
        // Test coupling strength
        System.out.println("Coupling (0,1): " + couplingStrength(0, 1));
        System.out.println("Coupling (0,2): " + couplingStrength(0, 2));
        System.out.println("Coupling (1,3): " + couplingStrength(1, 3));
    }
}
