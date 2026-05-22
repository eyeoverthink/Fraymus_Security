package fraymus.integration;

import java.io.*;
import java.util.*;

/**
 * 🎤 SPEECHBRAIN INTEGRATION
 * 
 * Integrates SpeechBrain capabilities into Fraynix for speech processing.
 * SpeechBrain is a PyTorch-based speech processing toolkit with 262+ modules.
 * 
 * Capabilities:
 * - Automatic Speech Recognition (ASR)
 * - Text-to-Speech (TTS)
 * - Speaker Recognition
 * - Voice Enhancement
 * - Speech Translation
 * 
 * @author Vaughn Scott
 * @version 1.0
 */
public class SpeechBrainIntegration {
    
    private String speechBrainPath;
    private String pythonPath;
    private boolean gpuEnabled;
    private String modelPath;
    
    /**
     * Supported speech tasks
     */
    public enum SpeechTask {
        ASR,              // Automatic Speech Recognition
        TTS,              // Text-to-Speech
        SPEAKER_RECOGNITION,  // Speaker Identification
        VOICE_ENHANCEMENT,    // Voice Enhancement
        SPEECH_TRANSLATION    // Speech Translation
    }
    
    /**
     * Initialize SpeechBrain integration
     */
    public SpeechBrainIntegration(String speechBrainPath) {
        this.speechBrainPath = speechBrainPath;
        this.pythonPath = "python3"; // Default
        this.gpuEnabled = false;
        this.modelPath = speechBrainPath + "/speechbrain/pretrained";
    }
    
    /**
     * Enable GPU acceleration
     */
    public void enableGPU() {
        this.gpuEnabled = true;
    }
    
    /**
     * Set custom Python path
     */
    public void setPythonPath(String path) {
        this.pythonPath = path;
    }
    
    /**
     * Convert speech to text (ASR)
     */
    public String speechToText(String audioFilePath) {
        try {
            String command = buildSpeechBrainCommand(SpeechTask.ASR, audioFilePath);
            return executeCommand(command);
        } catch (Exception e) {
            return "[SPEECHBRAIN ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Convert text to speech (TTS)
     */
    public String textToSpeech(String text, String outputPath) {
        try {
            String command = buildSpeechBrainCommand(SpeechTask.TTS, text, outputPath);
            return executeCommand(command);
        } catch (Exception e) {
            return "[SPEECHBRAIN ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Recognize speaker from audio
     */
    public String recognizeSpeaker(String audioFilePath) {
        try {
            String command = buildSpeechBrainCommand(SpeechTask.SPEAKER_RECOGNITION, audioFilePath);
            return executeCommand(command);
        } catch (Exception e) {
            return "[SPEECHBRAIN ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Enhance voice quality
     */
    public String enhanceVoice(String inputAudioPath, String outputPath) {
        try {
            String command = buildSpeechBrainCommand(SpeechTask.VOICE_ENHANCEMENT, inputAudioPath, outputPath);
            return executeCommand(command);
        } catch (Exception e) {
            return "[SPEECHBRAIN ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Translate speech
     */
    public String translateSpeech(String audioFilePath, String targetLanguage) {
        try {
            String command = buildSpeechBrainCommand(SpeechTask.SPEECH_TRANSLATION, audioFilePath, targetLanguage);
            return executeCommand(command);
        } catch (Exception e) {
            return "[SPEECHBRAIN ERROR] " + e.getMessage();
        }
    }
    
    /**
     * Build SpeechBrain command
     */
    private String buildSpeechBrainCommand(SpeechTask task, String... args) {
        StringBuilder command = new StringBuilder();
        command.append("cd ").append(speechBrainPath).append(" && ");
        command.append(pythonPath);
        
        switch (task) {
            case ASR:
                command.append(" -m speechbrain.inference.ASR");
                command.append(" EncoderDecoderASR.from_hparams(source='speechbrain/asr-crdnn-librispeech')");
                command.append(" --audio_path ").append(args[0]);
                break;
                
            case TTS:
                command.append(" -m speechbrain.inference.TTS");
                command.append(" Tacotron2.from_hparams(source='speechbrain/tts-tacotron2-ljspeech')");
                command.append(" --text \"").append(escapeForShell(args[0])).append("\"");
                command.append(" --output ").append(args[1]);
                break;
                
            case SPEAKER_RECOGNITION:
                command.append(" -m speechbrain.inference.speaker_recognition");
                command.append(" SpeakerRecognition.from_hparams(source='speechbrain/spkrec-ecapa-voxceleb')");
                command.append(" --audio_path ").append(args[0]);
                break;
                
            case VOICE_ENHANCEMENT:
                command.append(" -m speechbrain.inference.enhancement");
                command.append(" SpectralMaskEnhancement.from_hparams('speechbrain/mtl-mimic-voicebank')");
                command.append(" --input ").append(args[0]);
                command.append(" --output ").append(args[1]);
                break;
                
            case SPEECH_TRANSLATION:
                command.append(" -m speechbrain.inference.S2ST");
                command.append(" S2STTransformer.from_hparams('speechbrain/s2st-transformer-en-es')");
                command.append(" --audio_path ").append(args[0]);
                command.append(" --target_language ").append(args[1]);
                break;
        }
        
        if (gpuEnabled) {
            command.append(" --device cuda");
        } else {
            command.append(" --device cpu");
        }
        
        return command.toString();
    }
    
    /**
     * Execute command and return output
     */
    private String executeCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code: " + exitCode);
        }
        
        return output.toString();
    }
    
    /**
     * Escape content for shell
     */
    private String escapeForShell(String content) {
        return content.replace("\"", "\\\"").replace("'", "\\'");
    }
    
    /**
     * Check if SpeechBrain is available
     */
    public boolean isAvailable() {
        try {
            File path = new File(speechBrainPath);
            return path.exists() && path.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get available models
     */
    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>();
        models.add("speechbrain/asr-crdnn-librispeech");
        models.add("speechbrain/tts-tacotron2-ljspeech");
        models.add("speechbrain/spkrec-ecapa-voxceleb");
        models.add("speechbrain/mtl-mimic-voicebank");
        models.add("speechbrain/s2st-transformer-en-es");
        return models;
    }
    
    /**
     * Get status of SpeechBrain integration
     */
    public String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🎤 SPEECHBRAIN INTEGRATION STATUS\n");
        status.append("═══════════════════════════════════════════════════════════════\n");
        status.append("Path: ").append(speechBrainPath).append("\n");
        status.append("Available: ").append(isAvailable() ? "✅ YES" : "❌ NO").append("\n");
        status.append("GPU: ").append(gpuEnabled ? "✅ ENABLED" : "❌ DISABLED").append("\n");
        status.append("Python: ").append(pythonPath).append("\n");
        status.append("Model Path: ").append(modelPath).append("\n");
        status.append("Available Models: ").append(getAvailableModels().size()).append("\n");
        return status.toString();
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        SpeechBrainIntegration speechBrain = new SpeechBrainIntegration(
            "/Users/vaughnscott/Documents/java-memory-V1-main/Asset-Manager/speechbrain-develop"
        );
        
        System.out.println(speechBrain.getStatus());
        
        if (speechBrain.isAvailable()) {
            System.out.println("\n🧪 TEST: Available models");
            System.out.println("Models: " + String.join(", ", speechBrain.getAvailableModels()));
        }
    }
}
