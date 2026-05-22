package fraymus.biometric;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * VoiceRecorder - Pure Java audio capture using javax.sound.sampled
 * 
 * This class provides audio recording capabilities using only
 * standard Java libraries - no external audio libraries.
 * 
 * Features:
 * - Audio capture from microphone
 * - Configurable sample rate and format
 * - Voice activity detection
 * - Audio quality assessment
 * - Phi-harmonic frequency analysis
 * - Save to WAV format
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class VoiceRecorder {
    
    // Phi-harmonic audio constants
    public static final double PHI = 1.618033988749895;
    public static final double PHI_INVERSE = 1.0 / PHI;
    
    // Audio configuration
    public static final float SAMPLE_RATE = 16000.0f; // 16 kHz for voice
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 1; // Mono
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false;
    
    // Voice activity detection thresholds
    public static final double VAD_THRESHOLD = 0.05;
    public static final int VAD_SILENCE_FRAMES = 20;
    
    // Recording state
    private TargetDataLine targetLine;
    private boolean isRecording = false;
    private ByteArrayOutputStream audioBuffer;
    private long recordingStartTime;
    private int frameCount;
    
    /**
     * Initialize voice recorder
     * 
     * @return true if initialization successful
     */
    public boolean initialize() {
        try {
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
            );
            
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Audio line not supported");
                return false;
            }
            
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            return true;
            
        } catch (LineUnavailableException e) {
            System.err.println("Error initializing audio: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Start recording
     */
    public void startRecording() {
        if (targetLine == null) {
            if (!initialize()) {
                return;
            }
        }
        
        try {
            AudioFormat format = targetLine.getFormat();
            targetLine.open(format);
            targetLine.start();
            
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            frameCount = 0;
            audioBuffer = new ByteArrayOutputStream();
            
            // Start capture thread
            new Thread(this::captureAudio).start();
            
        } catch (LineUnavailableException e) {
            System.err.println("Error starting recording: " + e.getMessage());
        }
    }
    
    /**
     * Stop recording
     * 
     * @return Recorded audio bytes
     */
    public byte[] stopRecording() {
        isRecording = false;
        
        if (targetLine != null && targetLine.isOpen()) {
            targetLine.stop();
            targetLine.close();
        }
        
        if (audioBuffer != null) {
            return audioBuffer.toByteArray();
        }
        
        return new byte[0];
    }
    
    /**
     * Capture audio in background thread
     */
    private void captureAudio() {
        byte[] buffer = new byte[4096];
        
        while (isRecording) {
            int bytesRead = targetLine.read(buffer, 0, buffer.length);
            
            if (bytesRead > 0) {
                audioBuffer.write(buffer, 0, bytesRead);
                frameCount += bytesRead / 2; // 16-bit samples = 2 bytes
            }
        }
    }
    
    /**
     * Save recorded audio to WAV file
     * 
     * @param audioBytes Audio data
     * @param filePath Output file path
     * @throws IOException if file cannot be written
     */
    public void saveToWAV(byte[] audioBytes, String filePath) throws IOException {
        AudioFormat format = new AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE_IN_BITS,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        );
        
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
        
        File outputFile = new File(filePath);
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
    }
    
    /**
     * Load audio from WAV file
     * 
     * @param filePath Input file path
     * @return Audio data
     * @throws IOException if file cannot be read
     * @throws UnsupportedAudioFileException if format not supported
     */
    public byte[] loadFromWAV(String filePath) throws IOException, UnsupportedAudioFileException {
        File inputFile = new File(filePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputFile);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        
        while ((bytesRead = audioStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        
        audioStream.close();
        return baos.toByteArray();
    }
    
    /**
     * Perform voice activity detection
     * 
     * @param audioBytes Audio data
     * @return List of speech segments [startFrame, endFrame]
     */
    public List<int[]> detectVoiceActivity(byte[] audioBytes) {
        List<int[]> segments = new ArrayList<>();
        
        // Convert to samples
        short[] samples = bytesToSamples(audioBytes);
        
        boolean inSpeech = false;
        int speechStart = 0;
        int silenceCount = 0;
        
        for (int i = 0; i < samples.length; i++) {
            double energy = calculateFrameEnergy(samples, i, 160); // 10ms frame at 16kHz
            
            if (energy > VAD_THRESHOLD) {
                if (!inSpeech) {
                    inSpeech = true;
                    speechStart = i;
                    silenceCount = 0;
                } else {
                    silenceCount = 0;
                }
            } else {
                if (inSpeech) {
                    silenceCount++;
                    if (silenceCount > VAD_SILENCE_FRAMES) {
                        // End of speech segment
                        segments.add(new int[]{speechStart, i - silenceCount});
                        inSpeech = false;
                    }
                }
            }
        }
        
        // Add final segment if still in speech
        if (inSpeech) {
            segments.add(new int[]{speechStart, samples.length});
        }
        
        return segments;
    }
    
    /**
     * Calculate frame energy
     */
    private double calculateFrameEnergy(short[] samples, int start, int frameSize) {
        double sum = 0;
        int count = 0;
        
        for (int i = start; i < Math.min(start + frameSize, samples.length); i++) {
            sum += samples[i] * samples[i];
            count++;
        }
        
        if (count == 0) return 0;
        return Math.sqrt(sum / count) / 32768.0; // Normalize to [0, 1]
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
     * Convert samples to bytes
     */
    private byte[] samplesToBytes(short[] samples) {
        byte[] audioBytes = new byte[samples.length * 2];
        
        for (int i = 0; i < samples.length; i++) {
            audioBytes[i * 2] = (byte) (samples[i] & 0xFF);
            audioBytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        
        return audioBytes;
    }
    
    /**
     * Calculate phi-harmonic frequency analysis
     * 
     * @param audioBytes Audio data
     * @return Frequency analysis results
     */
    public FrequencyAnalysis analyzeFrequencies(byte[] audioBytes) {
        short[] samples = bytesToSamples(audioBytes);
        
        // Simple FFT-like frequency analysis (simplified)
        int fftSize = 1024;
        double[] magnitudes = new double[fftSize / 2];
        
        for (int i = 0; i < Math.min(fftSize, samples.length); i++) {
            magnitudes[i] = Math.abs(samples[i]) / 32768.0;
        }
        
        // Find dominant frequency
        double maxMagnitude = 0;
        int dominantBin = 0;
        
        for (int i = 1; i < magnitudes.length; i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude = magnitudes[i];
                dominantBin = i;
            }
        }
        
        double dominantFrequency = (dominantBin * SAMPLE_RATE) / fftSize;
        
        // Phi-harmonic quality score
        double phiScore = calculatePhiHarmonicScore(dominantFrequency, magnitudes);
        
        return new FrequencyAnalysis(dominantFrequency, maxMagnitude, phiScore, magnitudes);
    }
    
    /**
     * Calculate phi-harmonic quality score
     */
    private double calculatePhiHarmonicScore(double dominantFrequency, double[] magnitudes) {
        // Check if dominant frequency is in phi-harmonic range (e.g., 432 Hz, 528 Hz)
        double[] phiFrequencies = {432.0, 528.0, 639.0, 741.0, 852.0, 963.0};
        
        double minDistance = Double.MAX_VALUE;
        for (double phiFreq : phiFrequencies) {
            double distance = Math.abs(dominantFrequency - phiFreq);
            minDistance = Math.min(minDistance, distance);
        }
        
        // Score based on proximity to phi-harmonic frequencies
        double harmonicScore = Math.max(0, 1.0 - minDistance / 100.0);
        
        // Combine with overall energy distribution
        double energyScore = calculateEnergyBalance(magnitudes);
        
        return harmonicScore * PHI + energyScore * PHI_INVERSE;
    }
    
    /**
     * Calculate energy balance across frequencies
     */
    private double calculateEnergyBalance(double[] magnitudes) {
        double totalEnergy = 0;
        for (double mag : magnitudes) {
            totalEnergy += mag;
        }
        
        if (totalEnergy == 0) return 0;
        
        // Calculate entropy-like measure
        double entropy = 0;
        for (double mag : magnitudes) {
            if (mag > 0) {
                double p = mag / totalEnergy;
                entropy -= p * Math.log(p);
            }
        }
        
        // Normalize entropy
        return entropy / Math.log(magnitudes.length);
    }
    
    /**
     * Get recording duration in milliseconds
     */
    public long getRecordingDuration() {
        if (recordingStartTime == 0) return 0;
        return System.currentTimeMillis() - recordingStartTime;
    }
    
    /**
     * Get frame count
     */
    public int getFrameCount() {
        return frameCount;
    }
    
    /**
     * Check if recording
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Frequency analysis data class
     */
    public static class FrequencyAnalysis {
        public double dominantFrequency;
        public double dominantMagnitude;
        public double phiHarmonicScore;
        public double[] frequencyMagnitudes;
        
        public FrequencyAnalysis(double dominantFrequency, double dominantMagnitude, 
                               double phiHarmonicScore, double[] frequencyMagnitudes) {
            this.dominantFrequency = dominantFrequency;
            this.dominantMagnitude = dominantMagnitude;
            this.phiHarmonicScore = phiHarmonicScore;
            this.frequencyMagnitudes = frequencyMagnitudes;
        }
    }
}
