package fraymus.biometric;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

/**
 * BiometricExtractor - Convert raw data to mathematical representations
 * 
 * This class converts raw biometric data (images, audio, fingerprints)
 * into mathematical representations compatible with the FRAYMUS system:
 * - 2D trajectories for Langevin liveness detection
 * - 10,000-dimensional hypervectors for HDC processing
 * - Phi-harmonic embeddings for consciousness integration
 * - Feature vectors for pattern matching
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class BiometricExtractor {
    
    // Phi-harmonic constants
    public static final double PHI = 1.618033988749895;
    public static final int HYPERVECTOR_DIMENSION = 10000;
    
    /**
     * Extract 2D trajectory from image for Langevin liveness detection
     * 
     * @param image Input image
     * @return 2D trajectory array [y][x] with intensity values
     */
    public double[][] extractTrajectory(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] trajectory = new double[height][width];
        
        // Extract grayscale intensity values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                trajectory[y][x] = gray / 255.0; // Normalize to [0, 1]
            }
        }
        
        return trajectory;
    }
    
    /**
     * Extract trajectory from edge-detected image
     * 
     * @param edgeImage Edge-detected image
     * @return Edge trajectory
     */
    public double[][] extractEdgeTrajectory(BufferedImage edgeImage) {
        return extractTrajectory(edgeImage);
    }
    
    /**
     * Extract 10,000-dimensional hypervector from image
     * 
     * @param image Input image
     * @return Hypervector representation
     */
    public double[] extractHypervector(BufferedImage image) {
        double[] hypervector = new double[HYPERVECTOR_DIMENSION];
        
        // Extract features
        double[][] trajectory = extractTrajectory(image);
        int width = trajectory[0].length;
        int height = trajectory.length;
        
        // Fill hypervector with phi-harmonic sampling
        for (int i = 0; i < HYPERVECTOR_DIMENSION; i++) {
            // Phi-harmonic sampling pattern
            double phiSample = (i * PHI) % 1.0;
            int x = (int) (phiSample * width);
            int y = (int) ((phiSample * PHI) % 1.0 * height);
            
            if (x >= 0 && x < width && y >= 0 && y < height) {
                hypervector[i] = trajectory[y][x];
            } else {
                hypervector[i] = 0.0;
            }
        }
        
        return hypervector;
    }
    
    /**
     * Extract hypervector from audio data
     * 
     * @param audioBytes Audio data (16-bit samples)
     * @return Hypervector representation
     */
    public double[] extractAudioHypervector(byte[] audioBytes) {
        double[] hypervector = new double[HYPERVECTOR_DIMENSION];
        
        // Convert to samples
        short[] samples = bytesToSamples(audioBytes);
        
        // Phi-harmonic sampling
        for (int i = 0; i < HYPERVECTOR_DIMENSION; i++) {
            double phiSample = (i * PHI) % 1.0;
            int idx = (int) (phiSample * samples.length);
            
            if (idx >= 0 && idx < samples.length) {
                hypervector[i] = samples[idx] / 32768.0; // Normalize to [-1, 1]
            } else {
                hypervector[i] = 0.0;
            }
        }
        
        return hypervector;
    }
    
    /**
     * Extract hypervector from fingerprint minutiae
     * 
     * @param minutiae List of fingerprint minutiae
     * @return Hypervector representation
     */
    public double[] extractFingerprintHypervector(List<FingerprintScanner.Minutia> minutiae) {
        double[] hypervector = new double[HYPERVECTOR_DIMENSION];
        
        // Encode minutiae positions and types
        for (int i = 0; i < HYPERVECTOR_DIMENSION; i++) {
            double phiSample = (i * PHI) % 1.0;
            int idx = (int) (phiSample * minutiae.size());
            
            if (idx >= 0 && idx < minutiae.size()) {
                FingerprintScanner.Minutia m = minutiae.get(idx);
                // Encode position and type
                hypervector[i] = (m.x + m.y) / 1000.0 + (m.type == FingerprintScanner.MinutiaType.BIFURCATION ? 0.5 : 0.0);
            } else {
                hypervector[i] = 0.0;
            }
        }
        
        return hypervector;
    }
    
    /**
     * Extract hypervector from facial landmarks
     * 
     * @param landmarks Facial landmarks
     * @return Hypervector representation
     */
    public double[] extractFacialHypervector(FacialLandmarkExtractor.FacialLandmarks landmarks) {
        double[] hypervector = new double[HYPERVECTOR_DIMENSION];
        
        if (!landmarks.faceDetected) {
            return hypervector;
        }
        
        // Encode facial features
        for (int i = 0; i < HYPERVECTOR_DIMENSION; i++) {
            double phiSample = (i * PHI) % 1.0;
            
            // Cycle through facial features
            int featureIdx = (int) (phiSample * 4); // 4 features: face, eyes, mouth, nose
            double value = 0.0;
            
            switch (featureIdx) {
                case 0: // Face bounds
                    value = (landmarks.faceX + landmarks.faceY + landmarks.faceWidth + landmarks.faceHeight) / 1000.0;
                    break;
                case 1: // Eyes
                    if (landmarks.leftEye != null && landmarks.rightEye != null) {
                        value = (landmarks.leftEye[0][0] + landmarks.rightEye[0][0]) / 1000.0;
                    }
                    break;
                case 2: // Mouth
                    if (landmarks.mouth != null) {
                        value = (landmarks.mouth[0][0] + landmarks.mouth[1][0]) / 1000.0;
                    }
                    break;
                case 3: // Nose
                    if (landmarks.nose != null) {
                        value = (landmarks.nose[0][0] + landmarks.nose[1][0]) / 1000.0;
                    }
                    break;
            }
            
            hypervector[i] = value;
        }
        
        return hypervector;
    }
    
    /**
     * Extract phi-harmonic embedding from hypervector
     * 
     * @param hypervector Input hypervector
     * @return Phi-harmonic embedding (reduced dimension)
     */
    public double[] extractPhiEmbedding(double[] hypervector) {
        int embeddingDim = (int) (HYPERVECTOR_DIMENSION / PHI); // ~6180 dimensions
        double[] embedding = new double[embeddingDim];
        
        // Phi-harmonic dimensionality reduction
        for (int i = 0; i < embeddingDim; i++) {
            double sum = 0.0;
            int count = 0;
            
            // Sample from hypervector using phi-harmonic pattern
            for (int j = 0; j < 10; j++) {
                int srcIdx = (int) ((i + j * PHI) % HYPERVECTOR_DIMENSION);
                sum += hypervector[srcIdx];
                count++;
            }
            
            embedding[i] = sum / count;
        }
        
        return embedding;
    }
    
    /**
     * Extract feature vector for pattern matching
     * 
     * @param hypervector Input hypervector
     * @return Feature vector (statistical features)
     */
    public double[] extractFeatureVector(double[] hypervector) {
        double[] features = new double[10];
        
        // Calculate statistics
        double sum = 0, sumSq = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        
        for (double val : hypervector) {
            sum += val;
            sumSq += val * val;
            min = Math.min(min, val);
            max = Math.max(max, val);
        }
        
        double mean = sum / hypervector.length;
        double variance = (sumSq / hypervector.length) - (mean * mean);
        double stdDev = Math.sqrt(Math.max(0, variance));
        
        // Feature vector: [mean, stdDev, min, max, skewness, kurtosis, entropy, energy, phi-resonance, zero-crossing-rate]
        features[0] = mean;
        features[1] = stdDev;
        features[2] = min;
        features[3] = max;
        features[4] = calculateSkewness(hypervector, mean, stdDev);
        features[5] = calculateKurtosis(hypervector, mean, stdDev);
        features[6] = calculateEntropy(hypervector);
        features[7] = calculateEnergy(hypervector);
        features[8] = calculatePhiResonance(hypervector);
        features[9] = calculateZeroCrossingRate(hypervector);
        
        return features;
    }
    
    /**
     * Calculate skewness
     */
    private double calculateSkewness(double[] data, double mean, double stdDev) {
        if (stdDev == 0) return 0;
        
        double sum = 0;
        for (double val : data) {
            sum += Math.pow((val - mean) / stdDev, 3);
        }
        
        return sum / data.length;
    }
    
    /**
     * Calculate kurtosis
     */
    private double calculateKurtosis(double[] data, double mean, double stdDev) {
        if (stdDev == 0) return 0;
        
        double sum = 0;
        for (double val : data) {
            sum += Math.pow((val - mean) / stdDev, 4);
        }
        
        return (sum / data.length) - 3;
    }
    
    /**
     * Calculate entropy
     */
    private double calculateEntropy(double[] data) {
        double entropy = 0;
        
        for (double val : data) {
            if (val > 0) {
                entropy -= val * Math.log(val);
            }
        }
        
        return entropy;
    }
    
    /**
     * Calculate energy
     */
    private double calculateEnergy(double[] data) {
        double energy = 0;
        
        for (double val : data) {
            energy += val * val;
        }
        
        return energy / data.length;
    }
    
    /**
     * Calculate phi-harmonic resonance
     */
    private double calculatePhiResonance(double[] data) {
        double resonance = 0;
        
        for (int i = 0; i < data.length; i++) {
            double phiPhase = (i * PHI) % 1.0;
            resonance += data[i] * Math.cos(2 * Math.PI * phiPhase);
        }
        
        return Math.abs(resonance) / data.length;
    }
    
    /**
     * Calculate zero-crossing rate
     */
    private double calculateZeroCrossingRate(double[] data) {
        int crossings = 0;
        
        for (int i = 1; i < data.length; i++) {
            if ((data[i] >= 0 && data[i-1] < 0) || (data[i] < 0 && data[i-1] >= 0)) {
                crossings++;
            }
        }
        
        return (double) crossings / data.length;
    }
    
    /**
     * Convert bytes to 16-bit samples
     */
    private short[] bytesToSamples(byte[] audioBytes) {
        short[] samples = new short[audioBytes.length / 2];
        
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (short) ((audioBytes[i * 2] & 0xFF) | (audioBytes[i * 2 + 1] << 8));
        }
        
        return samples;
    }
    
    /**
     * Biometric data container
     */
    public static class BiometricData {
        public double[][] trajectory;
        public double[] hypervector;
        public double[] phiEmbedding;
        public double[] featureVector;
        public String biometricType;
        public long timestamp;
        
        public BiometricData(String biometricType, double[][] trajectory, double[] hypervector) {
            this.biometricType = biometricType;
            this.trajectory = trajectory;
            this.hypervector = hypervector;
            this.phiEmbedding = null;
            this.featureVector = null;
            this.timestamp = System.currentTimeMillis();
        }
        
        public void computeEmbeddings(BiometricExtractor extractor) {
            if (hypervector != null) {
                this.phiEmbedding = extractor.extractPhiEmbedding(hypervector);
                this.featureVector = extractor.extractFeatureVector(hypervector);
            }
        }
    }
}
