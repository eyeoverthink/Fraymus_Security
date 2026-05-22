package fraymus.ultimate;

import java.util.*;
import java.util.concurrent.*;

/**
 * AudioProcessor - Audio Processing and Analysis Engine
 * 
 * Handles audio processing, analysis, and feature extraction for the Ultimate Agent.
 * Integrates with AUM agency for audio operations.
 * 
 * Features:
 * - Audio signal processing with phi-harmonic optimization
 * - Spectral analysis and feature extraction
 * - Voice activity detection
 * - Audio segmentation and classification
 * - Real-time audio stream processing
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class AudioProcessor {
    
    private static final double PHI = 1.618033988749895;
    private static final int SAMPLE_RATE = 44100; // 44.1 kHz standard
    private static final int FRAME_SIZE = 1024; // FFT frame size
    
    // Audio processing state
    private Queue<double[]> audioBuffer;
    private Map<Long, double[]> audioTimestamps;
    private boolean isProcessing;
    private int frameCount;
    
    // Spectral analysis
    private Map<String, double[]> spectralFeatures;
    private Map<String, Double> audioMetrics;
    
    // Voice activity detection
    private boolean voiceActive;
    private double voiceThreshold;
    private int voiceFrameCount;
    
    // Audio classification
    private Map<String, Double> classProbabilities;
    
    public AudioProcessor() {
        this.audioBuffer = new LinkedList<>();
        this.audioTimestamps = new ConcurrentHashMap<>();
        this.spectralFeatures = new ConcurrentHashMap<>();
        this.audioMetrics = new ConcurrentHashMap<>();
        this.classProbabilities = new ConcurrentHashMap<>();
        this.frameCount = 0;
        this.isProcessing = false;
        this.voiceActive = false;
        this.voiceThreshold = 0.1; // 10% energy threshold
        this.voiceFrameCount = 0;
        
        initializeAudioMetrics();
    }
    
    /**
     * Initialize audio metrics with phi-harmonic values
     */
    private void initializeAudioMetrics() {
        audioMetrics.put("energy", 0.0);
        audioMetrics.put("spectral_centroid", 0.0);
        audioMetrics.put("spectral_rolloff", 0.0);
        audioMetrics.put("zero_crossing_rate", 0.0);
        audioMetrics.put("mfcc_phi", PHI);
        
        // Initialize class probabilities
        classProbabilities.put("speech", 0.0);
        classProbabilities.put("music", 0.0);
        classProbabilities.put("noise", 0.0);
        classProbabilities.put("silence", 0.0);
    }
    
    /**
     * Process audio frame
     */
    public void processFrame(double[] samples, long timestamp) {
        audioBuffer.add(samples);
        audioTimestamps.put(timestamp, samples);
        frameCount++;
        
        // Extract spectral features
        extractSpectralFeatures(samples);
        
        // Voice activity detection
        detectVoiceActivity(samples);
        
        // Audio classification
        classifyAudio(samples);
        
        // Update audio metrics
        updateAudioMetrics(samples);
    }
    
    /**
     * Extract spectral features from audio frame
     */
    private void extractSpectralFeatures(double[] samples) {
        // Compute FFT (simulated - in production use actual FFT)
        double[] spectrum = computeSpectrum(samples);
        spectralFeatures.put("spectrum_" + frameCount, spectrum);
        
        // Compute spectral centroid
        double spectralCentroid = computeSpectralCentroid(spectrum);
        audioMetrics.put("spectral_centroid", spectralCentroid);
        
        // Compute spectral rolloff
        double spectralRolloff = computeSpectralRolloff(spectrum);
        audioMetrics.put("spectral_rolloff", spectralRolloff);
    }
    
    /**
     * Compute spectrum (simulated FFT)
     */
    private double[] computeSpectrum(double[] samples) {
        int n = samples.length;
        double[] spectrum = new double[n / 2];
        
        // Simulated FFT using phi-harmonic modulation
        for (int k = 0; k < n / 2; k++) {
            double real = 0;
            double imag = 0;
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * k * i / n;
                real += samples[i] * Math.cos(angle);
                imag -= samples[i] * Math.sin(angle);
            }
            spectrum[k] = Math.sqrt(real * real + imag * imag) / n;
        }
        
        return spectrum;
    }
    
    /**
     * Compute spectral centroid
     */
    private double computeSpectralCentroid(double[] spectrum) {
        double numerator = 0;
        double denominator = 0;
        
        for (int i = 0; i < spectrum.length; i++) {
            numerator += i * spectrum[i];
            denominator += spectrum[i];
        }
        
        return denominator > 0 ? numerator / denominator : 0;
    }
    
    /**
     * Compute spectral rolloff (85% energy point)
     */
    private double computeSpectralRolloff(double[] spectrum) {
        double totalEnergy = 0;
        for (double value : spectrum) {
            totalEnergy += value;
        }
        
        double energyThreshold = 0.85 * totalEnergy;
        double cumulativeEnergy = 0;
        
        for (int i = 0; i < spectrum.length; i++) {
            cumulativeEnergy += spectrum[i];
            if (cumulativeEnergy >= energyThreshold) {
                return (double) i / spectrum.length;
            }
        }
        
        return 1.0;
    }
    
    /**
     * Detect voice activity
     */
    private void detectVoiceActivity(double[] samples) {
        double energy = computeEnergy(samples);
        audioMetrics.put("energy", energy);
        
        boolean currentVoiceActive = energy > voiceThreshold;
        
        if (currentVoiceActive) {
            voiceFrameCount++;
            if (voiceFrameCount > 3) { // Sustained voice for 3 frames
                voiceActive = true;
            }
        } else {
            voiceFrameCount = 0;
            if (voiceFrameCount == 0 && voiceFrameCount < 3) {
                voiceActive = false;
            }
        }
    }
    
    /**
     * Compute energy of audio frame
     */
    private double computeEnergy(double[] samples) {
        double energy = 0;
        for (double sample : samples) {
            energy += sample * sample;
        }
        return Math.sqrt(energy / samples.length);
    }
    
    /**
     * Classify audio (speech, music, noise, silence)
     */
    private void classifyAudio(double[] samples) {
        double energy = audioMetrics.get("energy");
        double spectralCentroid = audioMetrics.get("spectral_centroid");
        
        // Phi-harmonic classification
        double speechProb = 0;
        double musicProb = 0;
        double noiseProb = 0;
        double silenceProb = 0;
        
        if (energy < voiceThreshold) {
            silenceProb = 1.0;
        } else {
            // Speech: moderate energy, moderate spectral centroid
            double speechScore = 1.0 / (1.0 + Math.abs(spectralCentroid - 0.3));
            speechProb = speechScore * PHI;
            
            // Music: high energy, high spectral centroid
            double musicScore = 1.0 / (1.0 + Math.abs(spectralCentroid - 0.6));
            musicProb = musicScore * energy;
            
            // Noise: moderate energy, low spectral centroid
            double noiseScore = 1.0 / (1.0 + Math.abs(spectralCentroid - 0.1));
            noiseProb = noiseScore * (1.0 / PHI);
            
            // Normalize
            double total = speechProb + musicProb + noiseProb;
            speechProb /= total;
            musicProb /= total;
            noiseProb /= total;
        }
        
        classProbabilities.put("speech", speechProb);
        classProbabilities.put("music", musicProb);
        classProbabilities.put("noise", noiseProb);
        classProbabilities.put("silence", silenceProb);
    }
    
    /**
     * Update audio metrics
     */
    private void updateAudioMetrics(double[] samples) {
        // Zero crossing rate
        double zeroCrossings = 0;
        for (int i = 1; i < samples.length; i++) {
            if ((samples[i] >= 0 && samples[i-1] < 0) || (samples[i] < 0 && samples[i-1] >= 0)) {
                zeroCrossings++;
            }
        }
        audioMetrics.put("zero_crossing_rate", zeroCrossings / samples.length);
        
        // Phi-harmonic MFCC-like feature
        double mfccPhi = PHI * Math.log(1 + audioMetrics.get("energy"));
        audioMetrics.put("mfcc_phi", mfccPhi);
    }
    
    /**
     * Get dominant audio class
     */
    public String getDominantClass() {
        String dominant = "silence";
        double maxProb = 0;
        
        for (Map.Entry<String, Double> entry : classProbabilities.entrySet()) {
            if (entry.getValue() > maxProb) {
                maxProb = entry.getValue();
                dominant = entry.getKey();
            }
        }
        
        return dominant;
    }
    
    /**
     * Get audio statistics
     */
    public String getAudioStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AUDIO PROCESSOR STATISTICS ===\n");
        sb.append("Frame Count: ").append(frameCount).append("\n");
        sb.append("Sample Rate: ").append(SAMPLE_RATE).append(" Hz\n");
        sb.append("Frame Size: ").append(FRAME_SIZE).append(" samples\n");
        sb.append("Voice Active: ").append(voiceActive).append("\n");
        sb.append("Voice Frame Count: ").append(voiceFrameCount).append("\n");
        sb.append("\n=== AUDIO METRICS ===\n");
        sb.append("Energy: ").append(String.format("%.4f", audioMetrics.get("energy"))).append("\n");
        sb.append("Spectral Centroid: ").append(String.format("%.4f", audioMetrics.get("spectral_centroid"))).append("\n");
        sb.append("Spectral Rolloff: ").append(String.format("%.4f", audioMetrics.get("spectral_rolloff"))).append("\n");
        sb.append("Zero Crossing Rate: ").append(String.format("%.4f", audioMetrics.get("zero_crossing_rate"))).append("\n");
        sb.append("MFCC Phi: ").append(String.format("%.4f", audioMetrics.get("mfcc_phi"))).append("\n");
        sb.append("\n=== CLASS PROBABILITIES ===\n");
        for (Map.Entry<String, Double> entry : classProbabilities.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(String.format("%.2f%%", entry.getValue() * 100)).append("\n");
        }
        sb.append("Dominant Class: ").append(getDominantClass()).append("\n");
        return sb.toString();
    }
    
    /**
     * Clear audio buffer
     */
    public void clearAudioBuffer() {
        audioBuffer.clear();
        audioTimestamps.clear();
        spectralFeatures.clear();
        frameCount = 0;
        voiceFrameCount = 0;
    }
    
    /**
     * Get current audio metrics
     */
    public Map<String, Double> getAudioMetrics() {
        return new HashMap<>(audioMetrics);
    }
    
    /**
     * Get class probabilities
     */
    public Map<String, Double> getClassProbabilities() {
        return new HashMap<>(classProbabilities);
    }
}
