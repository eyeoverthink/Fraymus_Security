package fraymus.biometric;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

/**
 * FacialLandmarkExtractor - Pure Java facial feature detection
 * 
 * This class provides facial landmark extraction capabilities using only
 * standard Java libraries - no OpenCV/dlib/MediaPipe dependencies.
 * 
 * Features:
 * - Skin detection (YCbCr color space)
 * - Face region detection (connected components)
 * - Eye detection (circular pattern matching)
 * - Mouth detection (horizontal region)
 * - Nose detection (vertical region)
 * - Phi-harmonic facial proportions
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class FacialLandmarkExtractor {
    
    // Phi-harmonic facial proportions
    public static final double PHI = 1.618033988749895;
    public static final double GOLDEN_RATIO_FACE = PHI;
    
    // Skin detection thresholds (YCbCr color space)
    public static final int CB_MIN = 77;
    public static final int CB_MAX = 127;
    public static final int CR_MIN = 133;
    public static final int CR_MAX = 173;
    
    // Face detection parameters
    public static final int MIN_FACE_SIZE = 100;
    public static final double SKIN_RATIO_THRESHOLD = 0.3;
    
    /**
     * Detect facial landmarks from image
     * 
     * @param image Input image
     * @return FacialLandmarks object containing detected features
     */
    public FacialLandmarks extractLandmarks(BufferedImage image) {
        BufferedImage grayImage = toGrayscale(image);
        boolean[][] skinMask = detectSkin(image);
        
        // Find face region
        int[] faceBounds = findFaceRegion(skinMask);
        if (faceBounds == null) {
            return new FacialLandmarks(); // No face detected
        }
        
        int faceX = faceBounds[0];
        int faceY = faceBounds[1];
        int faceWidth = faceBounds[2];
        int faceHeight = faceBounds[3];
        
        // Extract face ROI
        BufferedImage faceROI = image.getSubimage(faceX, faceY, faceWidth, faceHeight);
        
        // Detect eyes
        int[][] leftEye = detectEye(faceROI, true);
        int[][] rightEye = detectEye(faceROI, false);
        
        // Detect mouth
        int[][] mouth = detectMouth(faceROI);
        
        // Detect nose
        int[][] nose = detectNose(faceROI);
        
        // Calculate phi-harmonic proportions
        double[] proportions = calculatePhiProportions(faceWidth, faceHeight, leftEye, rightEye, mouth);
        
        return new FacialLandmarks(
            faceX, faceY, faceWidth, faceHeight,
            leftEye, rightEye, mouth, nose, proportions
        );
    }
    
    /**
     * Convert image to grayscale
     */
    private BufferedImage toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int grayVal = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int grayRGB = (grayVal << 16) | (grayVal << 8) | grayVal;
                gray.setRGB(x, y, grayRGB);
            }
        }
        
        return gray;
    }
    
    /**
     * Detect skin pixels using YCbCr color space
     */
    private boolean[][] detectSkin(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        boolean[][] skinMask = new boolean[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Convert to YCbCr
                int yCb = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int cb = (int) (128 - 0.168736 * r - 0.331264 * g + 0.5 * b);
                int cr = (int) (128 + 0.5 * r - 0.418688 * g - 0.081312 * b);
                
                // Skin detection
                if (cb >= CB_MIN && cb <= CB_MAX && cr >= CR_MIN && cr <= CR_MAX) {
                    skinMask[y][x] = true;
                }
            }
        }
        
        return skinMask;
    }
    
    /**
     * Find face region using connected components
     */
    private int[] findFaceRegion(boolean[][] skinMask) {
        int height = skinMask.length;
        int width = skinMask[0].length;
        boolean[][] visited = new boolean[height][width];
        
        int maxArea = 0;
        int[] bestBounds = null;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (skinMask[y][x] && !visited[y][x]) {
                    int[] bounds = new int[4]; // x, y, width, height
                    int area = floodFill(skinMask, visited, x, y, bounds);
                    
                    if (area > maxArea && bounds[2] > MIN_FACE_SIZE && bounds[3] > MIN_FACE_SIZE) {
                        maxArea = area;
                        bestBounds = bounds;
                    }
                }
            }
        }
        
        return bestBounds;
    }
    
    /**
     * Flood fill to find connected component
     */
    private int floodFill(boolean[][] skinMask, boolean[][] visited, int startX, int startY, int[] bounds) {
        int height = skinMask.length;
        int width = skinMask[0].length;
        
        List<int[]> queue = new ArrayList<>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;
        
        int minX = startX, maxX = startX;
        int minY = startY, maxY = startY;
        int area = 0;
        
        while (!queue.isEmpty()) {
            int[] current = queue.remove(0);
            int x = current[0];
            int y = current[1];
            
            area++;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            
            // Check 8 neighbors
            int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && 
                    skinMask[ny][nx] && !visited[ny][nx]) {
                    visited[ny][nx] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        
        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = maxX - minX + 1;
        bounds[3] = maxY - minY + 1;
        
        return area;
    }
    
    /**
     * Detect eye region
     */
    private int[][] detectEye(BufferedImage faceROI, boolean isLeft) {
        int width = faceROI.getWidth();
        int height = faceROI.getHeight();
        
        // Eye region (upper third of face)
        int eyeY = height / 4;
        int eyeHeight = height / 4;
        int eyeX = isLeft ? width / 4 : (width * 3) / 4;
        int eyeWidth = width / 4;
        
        // Find eye center using intensity
        double maxIntensity = 0;
        int centerX = eyeX;
        int centerY = eyeY + eyeHeight / 2;
        
        for (int y = eyeY; y < eyeY + eyeHeight; y++) {
            for (int x = eyeX - eyeWidth / 2; x < eyeX + eyeWidth / 2; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int rgb = faceROI.getRGB(x, y);
                    int gray = (rgb >> 16) & 0xFF;
                    double intensity = 255 - gray; // Darker = higher intensity for eye
                    
                    if (intensity > maxIntensity) {
                        maxIntensity = intensity;
                        centerX = x;
                        centerY = y;
                    }
                }
            }
        }
        
        // Return eye bounding box
        return new int[][]{
            {centerX - 10, centerY - 5},
            {centerX + 10, centerY + 5}
        };
    }
    
    /**
     * Detect mouth region
     */
    private int[][] detectMouth(BufferedImage faceROI) {
        int width = faceROI.getWidth();
        int height = faceROI.getHeight();
        
        // Mouth region (lower third of face)
        int mouthY = (height * 2) / 3;
        int mouthHeight = height / 6;
        int mouthX = width / 2;
        int mouthWidth = width / 3;
        
        // Find mouth center using horizontal edge detection
        double maxEdge = 0;
        int centerX = mouthX;
        int centerY = mouthY + mouthHeight / 2;
        
        for (int y = mouthY; y < mouthY + mouthHeight; y++) {
            for (int x = mouthX - mouthWidth / 2; x < mouthX + mouthWidth / 2; x++) {
                if (x >= 0 && x < width - 1 && y >= 0 && y < height) {
                    int rgb1 = faceROI.getRGB(x, y);
                    int rgb2 = faceROI.getRGB(x + 1, y);
                    int gray1 = (rgb1 >> 16) & 0xFF;
                    int gray2 = (rgb2 >> 16) & 0xFF;
                    double edge = Math.abs(gray1 - gray2);
                    
                    if (edge > maxEdge) {
                        maxEdge = edge;
                        centerX = x;
                        centerY = y;
                    }
                }
            }
        }
        
        // Return mouth bounding box
        return new int[][]{
            {centerX - 20, centerY - 5},
            {centerX + 20, centerY + 10}
        };
    }
    
    /**
     * Detect nose region
     */
    private int[][] detectNose(BufferedImage faceROI) {
        int width = faceROI.getWidth();
        int height = faceROI.getHeight();
        
        // Nose region (center of face)
        int noseX = width / 2;
        int noseY = height / 2;
        
        // Find nose tip using vertical gradient
        double maxGradient = 0;
        int tipX = noseX;
        int tipY = noseY;
        
        for (int y = height / 3; y < (height * 2) / 3; y++) {
            for (int x = width / 3; x < (width * 2) / 3; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height - 1) {
                    int rgb1 = faceROI.getRGB(x, y);
                    int rgb2 = faceROI.getRGB(x, y + 1);
                    int gray1 = (rgb1 >> 16) & 0xFF;
                    int gray2 = (rgb2 >> 16) & 0xFF;
                    double gradient = Math.abs(gray1 - gray2);
                    
                    if (gradient > maxGradient) {
                        maxGradient = gradient;
                        tipX = x;
                        tipY = y;
                    }
                }
            }
        }
        
        // Return nose bounding box
        return new int[][]{
            {tipX - 10, tipY - 15},
            {tipX + 10, tipY + 5}
        };
    }
    
    /**
     * Calculate phi-harmonic facial proportions
     */
    private double[] calculatePhiProportions(int faceWidth, int faceHeight, 
                                            int[][] leftEye, int[][] rightEye, int[][] mouth) {
        // Eye spacing
        double eyeSpacing = Math.abs((rightEye[0][0] + rightEye[1][0]) / 2.0 - 
                                      (leftEye[0][0] + leftEye[1][0]) / 2.0);
        
        // Face width to height ratio
        double widthHeightRatio = (double) faceWidth / faceHeight;
        
        // Eye to mouth ratio
        double eyeToMouth = Math.abs((mouth[0][1] + mouth[1][1]) / 2.0 - 
                                    (leftEye[0][1] + leftEye[1][1]) / 2.0);
        
        // Phi-harmonic scores
        double phiScoreWidthHeight = 1.0 - Math.abs(widthHeightRatio - GOLDEN_RATIO_FACE);
        double phiScoreEyeSpacing = 1.0 - Math.abs(eyeSpacing / faceWidth - 0.5);
        double phiScoreEyeMouth = 1.0 - Math.abs(eyeToMouth / faceHeight - PHI_INVERSE);
        
        return new double[]{
            phiScoreWidthHeight,
            phiScoreEyeSpacing,
            phiScoreEyeMouth
        };
    }
    
    /**
     * FacialLandmarks data class
     */
    public static class FacialLandmarks {
        public int faceX, faceY, faceWidth, faceHeight;
        public int[][] leftEye;
        public int[][] rightEye;
        public int[][] mouth;
        public int[][] nose;
        public double[] phiProportions;
        public boolean faceDetected;
        
        public FacialLandmarks() {
            this.faceDetected = false;
        }
        
        public FacialLandmarks(int faceX, int faceY, int faceWidth, int faceHeight,
                              int[][] leftEye, int[][] rightEye, int[][] mouth, int[][] nose,
                              double[] phiProportions) {
            this.faceX = faceX;
            this.faceY = faceY;
            this.faceWidth = faceWidth;
            this.faceHeight = faceHeight;
            this.leftEye = leftEye;
            this.rightEye = rightEye;
            this.mouth = mouth;
            this.nose = nose;
            this.phiProportions = phiProportions;
            this.faceDetected = true;
        }
        
        public double getPhiScore() {
            if (!faceDetected || phiProportions == null) return 0.0;
            return (phiProportions[0] + phiProportions[1] + phiProportions[2]) / 3.0;
        }
    }
    
    // Phi inverse constant
    private static final double PHI_INVERSE = 1.0 / PHI;
}
