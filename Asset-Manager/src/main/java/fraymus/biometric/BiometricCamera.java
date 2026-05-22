package fraymus.biometric;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * BiometricCamera - Pure Java camera/image capture system
 * 
 * This class provides biometric image capture capabilities using only
 * standard Java libraries (javax.imageio, java.awt.image) - no OpenCV/JavaCV dependencies.
 * 
 * Features:
 * - Image file capture (JPG, PNG)
 * - Screen capture (java.awt.Robot)
 * - Frame sequence capture for motion analysis
 * - Image format conversion
 * - Phi-harmonic image quality assessment
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class BiometricCamera {
    
    // Phi-harmonic quality thresholds
    public static final double PHI_IMAGE_QUALITY = 0.618033988749895;
    public static final int MIN_IMAGE_WIDTH = 640;
    public static final int MIN_IMAGE_HEIGHT = 480;
    public static final double MIN_ASPECT_RATIO = 0.75; // 4:3 minimum
    
    // Supported formats
    public static final String[] SUPPORTED_FORMATS = {"JPG", "JPEG", "PNG", "BMP"};
    
    // Capture state
    private boolean capturing = false;
    private int frameCount = 0;
    private List<BufferedImage> frameBuffer = new ArrayList<>();
    
    /**
     * Capture single image from file
     * 
     * @param filePath Path to image file
     * @return Captured BufferedImage
     * @throws IOException if file cannot be read
     */
    public BufferedImage captureFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Unsupported image format: " + filePath);
        }
        
        // Validate image quality
        validateImageQuality(image);
        
        return image;
    }
    
    /**
     * Capture screen region using java.awt.Robot
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of capture region
     * @param height Height of capture region
     * @return Captured BufferedImage
     * @throws Exception if capture fails
     */
    public BufferedImage captureScreenRegion(int x, int y, int width, int height) throws Exception {
        java.awt.Robot robot = new java.awt.Robot();
        java.awt.Rectangle rect = new java.awt.Rectangle(x, y, width, height);
        BufferedImage image = robot.createScreenCapture(rect);
        
        validateImageQuality(image);
        
        return image;
    }
    
    /**
     * Capture full screen
     * 
     * @return Captured BufferedImage
     * @throws Exception if capture fails
     */
    public BufferedImage captureFullScreen() throws Exception {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        return captureScreenRegion(0, 0, screenSize.width, screenSize.height);
    }
    
    /**
     * Start frame sequence capture for motion analysis
     * 
     * @param maxFrames Maximum frames to capture
     * @param intervalMs Interval between frames in milliseconds
     */
    public void startSequenceCapture(int maxFrames, long intervalMs) {
        capturing = true;
        frameCount = 0;
        frameBuffer.clear();
        
        new Thread(() -> {
            try {
                while (capturing && frameCount < maxFrames) {
                    BufferedImage frame = captureFullScreen();
                    frameBuffer.add(frame);
                    frameCount++;
                    Thread.sleep(intervalMs);
                }
            } catch (Exception e) {
                System.err.println("Capture error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Stop sequence capture
     * 
     * @return List of captured frames
     */
    public List<BufferedImage> stopSequenceCapture() {
        capturing = false;
        return new ArrayList<>(frameBuffer);
    }
    
    /**
     * Convert BufferedImage to byte array
     * 
     * @param image Image to convert
     * @param format Output format (JPG, PNG, etc.)
     * @return Byte array of image data
     * @throws IOException if conversion fails
     */
    public byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
    
    /**
     * Convert byte array to BufferedImage
     * 
     * @param bytes Byte array of image data
     * @return BufferedImage
     * @throws IOException if conversion fails
     */
    public BufferedImage bytesToImage(byte[] bytes) throws IOException {
        return ImageIO.read(new java.io.ByteArrayInputStream(bytes));
    }
    
    /**
     * Save image to file
     * 
     * @param image Image to save
     * @param filePath Output file path
     * @param format Image format (JPG, PNG, etc.)
     * @throws IOException if save fails
     */
    public void saveImage(BufferedImage image, String filePath, String format) throws IOException {
        File outputFile = new File(filePath);
        ImageIO.write(image, format, outputFile);
    }
    
    /**
     * Validate image quality using phi-harmonic thresholds
     * 
     * @param image Image to validate
     * @throws IllegalArgumentException if image fails quality check
     */
    private void validateImageQuality(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double aspectRatio = (double) height / width;
        
        if (width < MIN_IMAGE_WIDTH || height < MIN_IMAGE_HEIGHT) {
            throw new IllegalArgumentException(
                String.format("Image too small: %dx%d (minimum: %dx%d)", 
                    width, height, MIN_IMAGE_WIDTH, MIN_IMAGE_HEIGHT));
        }
        
        if (aspectRatio < MIN_ASPECT_RATIO) {
            throw new IllegalArgumentException(
                String.format("Invalid aspect ratio: %.2f (minimum: %.2f)", 
                    aspectRatio, MIN_ASPECT_RATIO));
        }
    }
    
    /**
     * Calculate phi-harmonic image quality score
     * 
     * @param image Image to analyze
     * @return Quality score (0.0 to 1.0)
     */
    public double calculateQualityScore(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Resolution score (normalized)
        double resolutionScore = Math.min(1.0, (width * height) / (1920.0 * 1080.0));
        
        // Aspect ratio score (closer to 16:9 is better)
        double aspectRatio = (double) width / height;
        double idealRatio = 16.0 / 9.0;
        double aspectScore = 1.0 - Math.abs(aspectRatio - idealRatio) / idealRatio;
        
        // Phi-harmonic combination
        double qualityScore = (resolutionScore * PHI_IMAGE_QUALITY) + (aspectScore * (1 - PHI_IMAGE_QUALITY));
        
        return Math.max(0.0, Math.min(1.0, qualityScore));
    }
    
    /**
     * Extract grayscale pixel array from image
     * 
     * @param image Source image
     * @return 2D array of grayscale values (0-255)
     */
    public double[][] extractGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] grayscale = new double[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // Standard luminance formula
                grayscale[y][x] = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            }
        }
        
        return grayscale;
    }
    
    /**
     * Get camera status
     * 
     * @return Status string
     */
    public String getStatus() {
        if (capturing) {
            return String.format("CAPTURING - Frame: %d/%d", frameCount, frameBuffer.size());
        }
        return "IDLE";
    }
    
    /**
     * Check if currently capturing
     * 
     * @return true if capturing
     */
    public boolean isCapturing() {
        return capturing;
    }
    
    /**
     * Get frame count
     * 
     * @return Number of captured frames
     */
    public int getFrameCount() {
        return frameCount;
    }
}
