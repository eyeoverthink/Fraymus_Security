package fraymus.ultimate;

import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Visual Integration Test
 * Tests the facial fingerprinting, avatar ownership, and video cortex systems.
 * 
 * @author Vaughn Scott
 * @date May 6, 2026
 */
public class VisualIntegrationTest {
    
    public static void main(String[] args) {
        System.out.println("=== VISUAL INTEGRATION TEST ===\n");
        
        // Initialize Phase 1 components
        System.out.println("Initializing components...");
        UltimateCPU cpu = new UltimateCPU();
        UnifiedStateLattice lattice = new UnifiedStateLattice();
        UnifiedSynapse synapse = new UnifiedSynapse();
        CorpusCallosumUltimate callosum = new CorpusCallosumUltimate(synapse, lattice);
        
        // Initialize Phase 2 agencies
        System.out.println("Initializing agencies...");
        AgencyKAI kai = new AgencyKAI(synapse, lattice, callosum, cpu);
        AgencyVEX vex = new AgencyVEX(synapse, lattice, callosum, cpu);
        
        // Initialize facial recognition components
        System.out.println("Initializing facial recognition...");
        FacialFingerprint facialFingerprint = new FacialFingerprint();
        AvatarOwnership avatarOwnership = new AvatarOwnership(facialFingerprint);
        QuantumFacialFingerprint quantumFingerprint = new QuantumFacialFingerprint(facialFingerprint);
        VideoCortex videoCortex = new VideoCortex(facialFingerprint);
        
        System.out.println();
        
        // Test 1: Facial Fingerprint Extraction
        System.out.println("Test 1: Facial Fingerprint Extraction");
        BufferedImage testFace = createTestImage(640, 480);
        facialFingerprint.loadImage(testFace);
        System.out.println(facialFingerprint.getProfileStatistics());
        System.out.println();
        
        // Test 2: Avatar Ownership
        System.out.println("Test 2: Avatar Ownership System");
        AvatarOwnership.OwnedAvatar avatar = avatarOwnership.createOwnedAvatar(
            "test_user", testFace, AvatarOwnership.AvatarStyle.CARTOON);
        System.out.println("Avatar created for: " + avatar.getOwnerId());
        System.out.println("Ownership Certificate: " + avatar.getOwnershipCertificate());
        System.out.println("Phi Resonance: " + String.format("%.3f", avatar.getPhiResonance()));
        System.out.println();
        
        // Test 3: Quantum Fingerprinting
        System.out.println("Test 3: Quantum Facial Fingerprinting");
        FacialFingerprint.BiometricProfile profile = facialFingerprint.getCurrentProfile();
        QuantumFacialFingerprint.QuantumState quantumState = 
            quantumFingerprint.generateQuantumFingerprint("test_user", profile);
        System.out.println("Quantum Signature: " + quantumState.getQuantumSignature());
        System.out.println("Phi Resonance: " + String.format("%.3f", quantumState.getPhiResonance()));
        System.out.println("Quantum Depth: " + quantumState.getQuantumDepth());
        System.out.println("Continuity Score: " + String.format("%.3f", quantumState.getContinuityScore()));
        System.out.println();
        
        // Test 4: Face Verification
        System.out.println("Test 4: Face Verification");
        facialFingerprint.registerFace("test_user", profile);
        double similarity = facialFingerprint.verifyFace("test_user", profile);
        System.out.println("Face Similarity: " + String.format("%.2f%%", similarity * 100));
        boolean continuity = quantumFingerprint.verifyContinuity("test_user");
        System.out.println("Continuity Verified: " + continuity);
        System.out.println();
        
        // Test 5: VEX Agency Integration
        System.out.println("Test 5: VEX Agency Visual Integration");
        vex.processData("face_register test_user");
        vex.processData("avatar_create test_user");
        vex.processData("face_verify test_user");
        System.out.println(vex.getStatus());
        System.out.println();
        
        // Test 6: Video Cortex
        System.out.println("Test 6: Video Cortex Processing");
        for (int i = 0; i < 10; i++) {
            BufferedImage frame = createTestImage(320, 240);
            videoCortex.processFrame(frame, System.currentTimeMillis());
        }
        System.out.println(videoCortex.getFrameStatistics());
        System.out.println();
        
        // Test 7: Video Analysis
        System.out.println("Test 7: Video Analysis");
        BufferedImage[] frames = new BufferedImage[5];
        for (int i = 0; i < 5; i++) {
            frames[i] = createTestImage(320, 240);
        }
        var analysis = videoCortex.analyzeVideo(frames);
        System.out.println("Video Analysis Results:");
        System.out.println("Frame Count: " + analysis.get("frame_count"));
        System.out.println("Avg Eye Separation: " + String.format("%.2f", analysis.get("avg_eye_separation")));
        System.out.println("Avg Symmetry: " + String.format("%.3f", analysis.get("avg_symmetry")));
        System.out.println("Phi Harmony: " + String.format("%.3f", ((double[])analysis.get("avg_golden_ratio"))[6]));
        System.out.println();
        
        // Test 8: Ownership Statistics
        System.out.println("Test 8: Ownership Statistics");
        System.out.println(avatarOwnership.getOwnershipStatistics());
        System.out.println();
        
        // Test 9: Quantum Statistics
        System.out.println("Test 9: Quantum Fingerprint Statistics");
        System.out.println(quantumFingerprint.getQuantumStatistics());
        System.out.println();
        
        // Cleanup
        kai.shutdown();
        vex.shutdown();
        synapse.shutdown();
        videoCortex.clearFrameBuffer();
        
        System.out.println("=== VISUAL INTEGRATION TEST COMPLETE ===");
    }
    
    /**
     * Create test image (simulated face)
     */
    private static BufferedImage createTestImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // Fill with gradient background
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) ((x + y) * 255.0 / (width + height));
                g.setColor(new Color(gray, gray, gray));
                g.fillRect(x, y, 1, 1);
            }
        }
        
        // Draw simulated face features
        g.setColor(Color.BLACK);
        // Left eye
        g.fillOval(width / 3 - 20, height / 3 - 10, 40, 20);
        // Right eye
        g.fillOval(2 * width / 3 - 20, height / 3 - 10, 40, 20);
        // Nose
        g.fillPolygon(new int[]{width/2 - 10, width/2 + 10, width/2}, 
                     new int[]{height/2, height/2, height/2 + 30}, 3);
        // Mouth
        g.drawArc(width/3, 2*height/3, width/3, height/6, 180, 180);
        
        g.dispose();
        return image;
    }
}
