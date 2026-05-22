package com.fraymus.simulation;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Base64;
import java.security.*;

/**
 * WORKING ADVANCED AGORA: Based on proven GenesisSandbox with enhancements
 * 
 * Uses the working 2-bit encoding from GenesisSandbox but adds:
 * - Cryptographic signing
 * - Multi-platform simulation
 * - Dead-drop system
 * - Network broadcast (simplified)
 */
public class WorkingAdvancedAgora {
    
    // Use the proven working invisible alphabet from GenesisSandbox
    private static final String[] QUAD_BITS = {"\u200B", "\u200C", "\u200D", "\u2060"}; 
    
    // Simulated social platforms
    private static final Map<String, List<String>> platforms = new ConcurrentHashMap<>();
    private static final Map<String, String> deadDrops = new ConcurrentHashMap<>();
    
    // Simple RSA key pair for signing
    private static final KeyPair keyPair;
    
    static {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024); // Smaller for demo
            keyPair = kpg.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate keys", e);
        }
        
        // Initialize platforms
        platforms.put("twitter", new CopyOnWriteArrayList<>());
        platforms.put("reddit", new CopyOnWriteArrayList<>());
        platforms.put("discord", new CopyOnWriteArrayList<>());
    }
    
    /**
     * WORKING FORGE: Based on GenesisSandbox's proven algorithm
     */
    public static String forgeWorkingHyperText(String carrierText, String payload, String platform) {
        System.out.println("--- FORGING WORKING HYPER-GLYPH ---");
        System.out.println("Platform: " + platform);
        System.out.println("Carrier: \"" + carrierText + "\"");
        
        try {
            // 1. Proper RSA signing using the generated keyPair
            String signedPayload = signPayload(payload);
            
            // 2. Use the working Base64 + Binary encoding from GenesisSandbox
            String b64Payload = Base64.getEncoder().encodeToString(signedPayload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder binary = new StringBuilder();
            for (char c : b64Payload.toCharArray()) {
                binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
            }

            StringBuilder stegoText = new StringBuilder();
            int payloadPtr = 0;
            int textPtr = 0;
            
            // Use the proven working weaving from GenesisSandbox
            while (textPtr < carrierText.length()) {
                stegoText.append(carrierText.charAt(textPtr));
                
                if (payloadPtr < binary.length() - 1) {
                    String twoBits = binary.substring(payloadPtr, payloadPtr + 2);
                    int index = Integer.parseInt(twoBits, 2);
                    stegoText.append(QUAD_BITS[index]); 
                    payloadPtr += 2;
                }
                textPtr++;
            }
            
            // Add remaining payload
            while (payloadPtr < binary.length() - 1) {
                String twoBits = binary.substring(payloadPtr, payloadPtr + 2);
                int index = Integer.parseInt(twoBits, 2);
                stegoText.append(QUAD_BITS[index]);
                payloadPtr += 2;
            }
            
            System.out.println("Payload layers: Original → Sign → Base64 → Binary → Zero-width");
            System.out.println("Final length: " + stegoText.length() + " (visible: " + carrierText.length() + ")");
            
            return stegoText.toString();
            
        } catch (Exception e) {
            System.out.println("[ERROR] Forging failed: " + e.getMessage());
            return carrierText;
        }
    }
    
    /**
     * WORKING EXTRACTION: Based on GenesisSandbox's proven algorithm
     */
    public static String extractWorkingHyperText(String stegoText, String platform) {
        try {
            StringBuilder binary = new StringBuilder();
            
            // Use the proven working extraction from GenesisSandbox
            for (char c : stegoText.toCharArray()) {
                String s = String.valueOf(c);
                for (int i = 0; i < 4; i++) {
                    if (s.equals(QUAD_BITS[i])) {
                        binary.append(String.format("%2s", Integer.toBinaryString(i)).replace(' ', '0'));
                    }
                }
            }
            
            if (binary.length() == 0) return null;

            StringBuilder b64Output = new StringBuilder();
            for (int i = 0; i < binary.length(); i += 8) {
                if (i + 8 <= binary.length()) {
                    String byteStr = binary.substring(i, i + 8);
                    b64Output.append((char) Integer.parseInt(byteStr, 2));
                }
            }
            
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(b64Output.toString());
                String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
                
                // Proper RSA signature verification
                String originalPayload = verifyAndExtract(decoded);
                return originalPayload != null ? originalPayload : "[SIGNATURE VERIFICATION FAILED]";
                
            } catch (IllegalArgumentException e) {
                return "[CORRUPTED PAYLOAD]";
            }
            
        } catch (Exception e) {
            return "[EXTRACTION ERROR: " + e.getMessage() + "]";
        }
    }
    
    /**
     * SOCIAL PLATFORM SIMULATION
     */
    public static void postToSocialPlatform(String platform, String content) {
        List<String> feed = platforms.get(platform);
        if (feed != null) {
            feed.add(content);
            System.out.println("[SOCIAL] Posted to " + platform + " (feed size: " + feed.size() + ")");
        }
    }
    
    /**
     * RSA SIGNING METHODS
     */
    private static String signPayload(String payload) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(payload.getBytes(StandardCharsets.UTF_8));
        byte[] signature = sig.sign();
        return payload + "|" + Base64.getEncoder().encodeToString(signature);
    }
    
    private static String verifyAndExtract(String signedPayload) throws Exception {
        String[] parts = signedPayload.split("\\|", 2);
        if (parts.length != 2) return null;
        
        String payload = parts[0];
        String signatureB64 = parts[1];
        
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(keyPair.getPublic());
        sig.update(payload.getBytes(StandardCharsets.UTF_8));
        
        byte[] signature = Base64.getDecoder().decode(signatureB64);
        return sig.verify(signature) ? payload : null;
    }
    
    /**
     * DEAD-DROP SYSTEM
     */
    public static void createDeadDrop(String location, String message) {
        String encoded = forgeWorkingHyperText("Weather is nice today.", message, "dead_drop");
        deadDrops.put(location, encoded);
        System.out.println("[DEAD-DROP] Created at: " + location);
    }
    
    public static String checkDeadDrop(String location) {
        String encoded = deadDrops.get(location);
        if (encoded != null) {
            String extracted = extractWorkingHyperText(encoded, "dead_drop");
            System.out.println("[DEAD-DROP] Retrieved from: " + location);
            return extracted;
        }
        return null;
    }
    
    /**
     * MAIN DEMONSTRATION
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   WORKING ADVANCED AGORA: PROVEN STEGANOGRAPHY        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        // Create working steganographic messages
        String secret1 = "OPERATION:PHOENIX|TARGET:AGORA|TIME:0300|EXTRACT";
        String secret2 = "MEETING:CENTRAL_PARK|BENCH:WEST|PACKAGE:DELIVERED";
        String secret3 = "ENCRYPTION_KEY:AES256|PASSWORD:CHANGEME|SERVER:BACKUP";
        
        // Forge messages using the working algorithm
        String tweet = forgeWorkingHyperText(
            "Just had an amazing coffee at the new place downtown! Highly recommend! ☕", 
            secret1, 
            "twitter"
        );
        
        String redditPost = forgeWorkingHyperText(
            "TIL that the golden ratio appears everywhere in nature. Mathematics is beautiful! 🌻", 
            secret2, 
            "reddit"
        );
        
        String discordMsg = forgeWorkingHyperText(
            "Anyone up for some gaming tonight? Thinking of trying that new RPG release! 🎮", 
            secret3, 
            "discord"
        );
        
        // Post to platforms
        postToSocialPlatform("twitter", tweet);
        postToSocialPlatform("reddit", redditPost);
        postToSocialPlatform("discord", discordMsg);
        
        // Create dead drops
        createDeadDrop("central_park_bench_7", "CONTACT:SPY|CODE:RED|URGENT");
        createDeadDrop("library_book_42", "INTEL:ENEMY|POSITION:NORTH|MOVE_OUT");
        
        // Simulate other nodes scanning
        System.out.println("\n=== SIMULATING OTHER NODES SCANNING ===");
        
        for (Map.Entry<String, List<String>> entry : platforms.entrySet()) {
            String platform = entry.getKey();
            List<String> feed = entry.getValue();
            
            System.out.println("\n[" + platform.toUpperCase() + " SCANNER]");
            for (int i = 0; i < feed.size(); i++) {
                String post = feed.get(i);
                String extracted = extractWorkingHyperText(post, platform);
                
                if (extracted != null && !extracted.startsWith("[")) {
                    System.out.println("  Post " + i + ": SECRET DETECTED");
                    System.out.println("  → " + extracted);
                } else if (extracted != null) {
                    System.out.println("  Post " + i + ": CORRUPT OR FORGED");
                }
            }
        }
        
        // Check dead drops
        System.out.println("\n=== DEAD-DROP RETRIEVAL ===");
        String drop1 = checkDeadDrop("central_park_bench_7");
        String drop2 = checkDeadDrop("library_book_42");
        
        if (drop1 != null) System.out.println("Drop 1: " + drop1);
        if (drop2 != null) System.out.println("Drop 2: " + drop2);
        
        System.out.println("\n✓ Working Advanced Agora demonstration complete");
        System.out.println("  - Proven steganographic encoding (based on GenesisSandbox)");
        System.out.println("  - Simple cryptographic signing");
        System.out.println("  - Multi-platform simulation");
        System.out.println("  - Dead-drop system operational");
        
        // Keep running briefly
        Thread.sleep(2000);
    }
}
