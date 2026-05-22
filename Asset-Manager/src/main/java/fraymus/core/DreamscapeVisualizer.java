package fraymus.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * 🎥 DREAMSCAPE VISUALIZER - Converting Logic into Light
 * 
 * Translates Fraymus quantum states into high-fidelity images using Stable Diffusion
 * 
 * Architecture:
 * - Java layer packages state as JSON
 * - Invokes Python subprocess (VideoCortex.py)
 * - Python translates to visual prompt and generates image
 */
public class DreamscapeVisualizer {
    
    private static final String PYTHON_SCRIPT = "VideoCortex.py";
    private static boolean available = false;
    private static int totalDreams = 0;
    private static int successfulDreams = 0;
    
    static {
        checkAvailability();
    }
    
    /**
     * Check if Python and dependencies are available
     */
    private static void checkAvailability() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process p = pb.start();
            p.waitFor();
            available = (p.exitValue() == 0);
            
            if (available) {
                System.out.println("[DREAMSCAPE] ✅ Python available");
            } else {
                System.out.println("[DREAMSCAPE] ⚠️  Python not available - Dreamscape disabled");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("[DREAMSCAPE] ⚠️  Python check failed: " + e.getMessage());
            available = false;
        }
    }
    
    /**
     * Generate a dream from quantum state
     * 
     * @param concept The concept/description of the state
     * @param entropy Chaos vs Order (0.0 - 1.0)
     * @param phi Golden ratio constant
     * @param consciousness Luminosity/awareness (0.0 - 1.0)
     */
    public static void dream(String concept, double entropy, double phi, double consciousness) {
        if (!available) {
            System.out.println("[DREAMSCAPE] ⚠️  Not available - Python or dependencies missing");
            return;
        }
        
        totalDreams++;
        
        try {
            // 1. Package state as JSON
            String jsonState = String.format(
                "{\"concept\":\"%s\", \"entropy\":%.6f, \"phi\":%.6f, \"consciousness\":%.6f}",
                escapeJson(concept), entropy, phi, consciousness
            );
            
            System.out.println("[DREAMSCAPE] 🎥 Generating image: " + concept);
            System.out.println("[DREAMSCAPE]    Entropy: " + String.format("%.3f", entropy));
            System.out.println("[DREAMSCAPE]    Phi: " + String.format("%.6f", phi));
            System.out.println("[DREAMSCAPE]    Consciousness: " + String.format("%.3f", consciousness));
            
            // 2. Invoke Python subprocess
            ProcessBuilder pb = new ProcessBuilder("python", PYTHON_SCRIPT);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            // 3. Send state via stdin
            java.io.OutputStream os = p.getOutputStream();
            os.write(jsonState.getBytes());
            os.flush();
            os.close();
            
            // 4. Stream output logs
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   [DREAM] " + line);
            }
            
            int exitCode = p.waitFor();
            
            if (exitCode == 0) {
                successfulDreams++;
                System.out.println("[DREAMSCAPE] ✅ Image generated successfully");
            } else {
                System.out.println("[DREAMSCAPE] ❌ Image generation failed (exit code: " + exitCode + ")");
            }
            
        } catch (IOException e) {
            System.err.println("[DREAMSCAPE] ❌ IO Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[DREAMSCAPE] ❌ Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Generate a dream with custom resolution
     * 
     * @param concept The concept/description
     * @param entropy Chaos vs Order (0.0 - 1.0)
     * @param phi Golden ratio constant
     * @param consciousness Luminosity/awareness (0.0 - 1.0)
     * @param width Image width
     * @param height Image height
     */
    public static void dreamCustom(String concept, double entropy, double phi, double consciousness,
                                    int width, int height) {
        if (!available) {
            System.out.println("[DREAMSCAPE] ⚠️  Not available - Python or dependencies missing");
            return;
        }
        
        totalDreams++;
        
        try {
            // 1. Package state as JSON
            String jsonState = String.format(
                "{\"concept\":\"%s\", \"entropy\":%.6f, \"phi\":%.6f, \"consciousness\":%.6f}",
                escapeJson(concept), entropy, phi, consciousness
            );
            
            System.out.println("[DREAMSCAPE] 🎥 Generating custom image: " + concept);
            System.out.println("[DREAMSCAPE]    Resolution: " + width + "x" + height);
            
            // 2. Invoke Python subprocess with custom parameters
            ProcessBuilder pb = new ProcessBuilder(
                "python", PYTHON_SCRIPT,
                "--width", String.valueOf(width),
                "--height", String.valueOf(height)
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            // 3. Send state via stdin
            java.io.OutputStream os = p.getOutputStream();
            os.write(jsonState.getBytes());
            os.flush();
            os.close();
            
            // 4. Stream output logs
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   [DREAM] " + line);
            }
            
            int exitCode = p.waitFor();
            
            if (exitCode == 0) {
                successfulDreams++;
                System.out.println("[DREAMSCAPE] ✅ Image generated successfully");
            } else {
                System.out.println("[DREAMSCAPE] ❌ Image generation failed (exit code: " + exitCode + ")");
            }
            
        } catch (IOException e) {
            System.err.println("[DREAMSCAPE] ❌ IO Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[DREAMSCAPE] ❌ Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Escape JSON special characters
     */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * Check if Dreamscape is available
     */
    public static boolean isAvailable() {
        return available;
    }
    
    /**
     * Get total dreams attempted
     */
    public static int getTotalDreams() {
        return totalDreams;
    }
    
    /**
     * Get successful dreams
     */
    public static int getSuccessfulDreams() {
        return successfulDreams;
    }
    
    /**
     * Get success rate
     */
    public static double getSuccessRate() {
        return totalDreams > 0 ? (double) successfulDreams / totalDreams : 0.0;
    }
    
    /**
     * Print statistics
     */
    public static void printStats() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   🎥 DREAMSCAPE STATISTICS                                ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("  Available: " + (available ? "Yes" : "No"));
        System.out.println("  Total Dreams: " + totalDreams);
        System.out.println("  Successful: " + successfulDreams);
        System.out.println("  Success Rate: " + String.format("%.1f%%", getSuccessRate() * 100));
    }
}
