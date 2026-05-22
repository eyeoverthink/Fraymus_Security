package fraymus.ultimate;

import java.util.*;

/**
 * Audio Integration Test
 * Tests the audio processing, speech synthesis, speech recognition, and knowledge extraction systems.
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class AudioIntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("=== AUDIO INTEGRATION TEST ===\n");
        
        // Initialize Phase 1 components
        System.out.println("Initializing components...");
        UltimateCPU cpu = new UltimateCPU();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        UnifiedSynapse synapse = new UnifiedSynapse();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        // Initialize Phase 2 agencies
        System.out.println("Initializing agencies...");
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyAUM aum = new AgencyAUM(synapse, lattice, callosum, cpu);
        
        // Initialize audio components
        System.out.println("Initializing audio components...");
        AudioProcessor audioProcessor = new AudioProcessor();
        SpeechSynthesis speechSynthesis = new SpeechSynthesis();
        SpeechRecognition speechRecognition = new SpeechRecognition();
        AudioKnowledgeExtractor knowledgeExtractor = new AudioKnowledgeExtractor();
        
        System.out.println();
        
        // Test 1: Audio Processing
        System.out.println("Test 1: Audio Processing Engine");
        double[] audioSamples = generateTestAudio(1024);
        audioProcessor.processFrame(audioSamples, System.currentTimeMillis());
        System.out.println(audioProcessor.getAudioStatistics());
        System.out.println();
        
        // Test 2: Speech Synthesis
        System.out.println("Test 2: Speech Synthesis (Fish Audio S2)");
        double[] synthesized = speechSynthesis.synthesize("Hello world, this is a test", "default");
        System.out.println("Speech synthesized successfully");
        System.out.println(speechSynthesis.getSynthesisStatistics());
        System.out.println();
        
        // Test 3: Speech Recognition
        System.out.println("Test 3: Speech Recognition");
        SpeechRecognition.RecognitionResult result = speechRecognition.recognize(audioSamples);
        System.out.println("Recognized text: " + result.getText());
        System.out.println("Confidence: " + String.format("%.2f%%", result.getConfidence() * 100));
        System.out.println("Speaker: " + result.getSpeakerId());
        System.out.println();
        
        // Test 4: Speaker Registration
        System.out.println("Test 4: Speaker Registration");
        speechRecognition.registerSpeaker("speaker1", "Test User", audioSamples);
        SpeechRecognition.SpeakerProfile profile = speechRecognition.getSpeakerProfile("speaker1");
        System.out.println("Speaker registered: " + profile.getName());
        System.out.println("Sample count: " + profile.getSampleCount());
        System.out.println();
        
        // Test 5: Knowledge Extraction
        System.out.println("Test 5: Audio Knowledge Extraction");
        AudioKnowledgeExtractor.ExtractionResult extractionResult = 
            knowledgeExtractor.extractKnowledge("The technology is amazing and makes me happy");
        System.out.println("Dominant topic: " + extractionResult.getDominantTopic());
        System.out.println("Sentiment: " + String.format("%.3f", extractionResult.getSentiment()));
        System.out.println("Phi harmony: " + String.format("%.3f", extractionResult.getPhiHarmony()));
        System.out.println("Concepts extracted: " + extractionResult.getConcepts());
        System.out.println();
        
        // Test 6: AUM Agency Integration
        System.out.println("Test 6: AUM Agency Audio Integration");
        aum.processData("speak Hello from AUM agency");
        aum.processData("listen");
        aum.processData("analyze_audio test_audio");
        aum.processData("extract_knowledge The system is working perfectly");
        System.out.println(aum.getStatus());
        System.out.println();
        
        // Test 7: Voice Profile Creation
        System.out.println("Test 7: Custom Voice Profile");
        SpeechSynthesis.VoiceProfile customVoice = speechSynthesis.createVoiceProfile(
            "custom", SpeechSynthesis.VoiceStyle.PHI_HARMONIC, 1.2, 1.0, 0.9);
        System.out.println("Custom voice created: " + customVoice.getVoiceId());
        System.out.println("Style: " + customVoice.getStyle());
        System.out.println("Pitch: " + String.format("%.2f", customVoice.getPitch()));
        System.out.println("Phi modulation: " + String.format("%.3f", customVoice.getPhiModulation()));
        System.out.println();
        
        // Test 8: Knowledge Graph
        System.out.println("Test 8: Knowledge Graph");
        knowledgeExtractor.extractKnowledge("Technology and science are important");
        knowledgeExtractor.extractKnowledge("The art of music is beautiful");
        knowledgeExtractor.extractKnowledge("Philosophy helps us understand meaning");
        System.out.println(knowledgeExtractor.getExtractionStatistics());
        System.out.println();
        
        // Test 9: Audio Classification
        System.out.println("Test 9: Audio Classification");
        for (int i = 0; i < 10; i++) {
            double[] frame = generateTestAudio(1024);
            audioProcessor.processFrame(frame, System.currentTimeMillis());
        }
        System.out.println("Dominant class: " + audioProcessor.getDominantClass());
        System.out.println("Class probabilities: " + audioProcessor.getClassProbabilities());
        System.out.println();
        
        // Cleanup
        kai.shutdown();
        aum.shutdown();
        synapse.shutdown();
        audioProcessor.clearAudioBuffer();
        speechRecognition.clearHistory();
        knowledgeExtractor.clearHistory();
        
        System.out.println("=== AUDIO INTEGRATION TEST COMPLETE ===");
    }
    
    /**
     * Generate test audio samples
     */
    private static double[] generateTestAudio(int length) {
        double[] audio = new double[length];
        for (int i = 0; i < length; i++) {
            // Generate 440 Hz sine wave (A4 note)
            audio[i] = Math.sin(2 * Math.PI * 440 * i / 44100) * 0.5;
            // Add some harmonics
            audio[i] += Math.sin(2 * Math.PI * 880 * i / 44100) * 0.25;
            audio[i] += Math.sin(2 * Math.PI * 1320 * i / 44100) * 0.125;
        }
        return audio;
    }
}
