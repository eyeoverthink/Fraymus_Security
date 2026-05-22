package fraymus.biometric;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import fraymus.core.PhiHarmonicGeometry;

/**
 * ImageProcessor - Custom image processing without OpenCV
 * 
 * This class provides image processing capabilities using only
 * standard Java libraries - no OpenCV/JavaCV dependencies.
 * 
 * Features:
 * - Grayscale conversion
 * - Edge detection (Sobel operator)
 * - Gaussian blur
 * - Feature extraction (corners, edges, regions)
 * - Phi-harmonic thresholding
 * - Noise reduction
 * 
 * @author Vaughn Scott
 * @date May 9, 2026
 * @version 1.0
 * @patent VS-PoQC-19046423-φ⁷⁵-2025
 */
public class ImageProcessor {
    
    // Phi-harmonic thresholds
    public static final double EDGE_THRESHOLD = PhiHarmonicGeometry.PHI_INVERSE; // 0.618
    public static final double CORNER_THRESHOLD = 0.75;
    public static final int BLUR_KERNEL_SIZE = 5;
    
    // Sobel kernels for edge detection
    private static final int[][] SOBEL_X = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static final int[][] SOBEL_Y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
    
    /**
     * Convert image to grayscale
     * 
     * @param image Input image
     * @return Grayscale image
     */
    public BufferedImage toGrayscale(BufferedImage image) {
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
     * Apply Gaussian blur
     * 
     * @param image Input image
     * @param kernelSize Kernel size (must be odd)
     * @return Blurred image
     */
    public BufferedImage gaussianBlur(BufferedImage image, int kernelSize) {
        if (kernelSize % 2 == 0) kernelSize++;
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage blurred = new BufferedImage(width, height, image.getType());
        
        // Create Gaussian kernel
        double[][] kernel = createGaussianKernel(kernelSize, 1.0);
        
        // Apply blur
        for (int y = kernelSize / 2; y < height - kernelSize / 2; y++) {
            for (int x = kernelSize / 2; x < width - kernelSize / 2; x++) {
                double sum = 0;
                double weightSum = 0;
                
                for (int ky = -kernelSize / 2; ky <= kernelSize / 2; ky++) {
                    for (int kx = -kernelSize / 2; kx <= kernelSize / 2; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        int gray = (rgb >> 16) & 0xFF;
                        double weight = kernel[ky + kernelSize / 2][kx + kernelSize / 2];
                        sum += gray * weight;
                        weightSum += weight;
                    }
                }
                
                int blurredVal = (int) (sum / weightSum);
                int blurredRGB = (blurredVal << 16) | (blurredVal << 8) | blurredVal;
                blurred.setRGB(x, y, blurredRGB);
            }
        }
        
        return blurred;
    }
    
    /**
     * Create Gaussian kernel
     * 
     * @param size Kernel size
     * @param sigma Standard deviation
     * @return Gaussian kernel
     */
    private double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0;
        int center = size / 2;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double dy = y - center;
                double value = Math.exp(-(dx * dx + dy * dy) / (2 * sigma * sigma));
                kernel[y][x] = value;
                sum += value;
            }
        }
        
        // Normalize
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= sum;
            }
        }
        
        return kernel;
    }
    
    /**
     * Apply Sobel edge detection
     * 
     * @param image Input grayscale image
     * @return Edge magnitude image
     */
    public BufferedImage sobelEdgeDetection(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage edges = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double gx = 0;
                double gy = 0;
                
                // Apply Sobel kernels
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        int gray = (rgb >> 16) & 0xFF;
                        gx += gray * SOBEL_X[ky + 1][kx + 1];
                        gy += gray * SOBEL_Y[ky + 1][kx + 1];
                    }
                }
                
                double magnitude = Math.sqrt(gx * gx + gy * gy);
                int edgeVal = (int) Math.min(255, magnitude);
                int edgeRGB = (edgeVal << 16) | (edgeVal << 8) | edgeVal;
                edges.setRGB(x, y, edgeRGB);
            }
        }
        
        return edges;
    }
    
    /**
     * Extract edge points as trajectory (for Langevin liveness detection)
     * 
     * @param edgeImage Edge-detected image
     * @return 2D trajectory array [y][x] with edge intensity
     */
    public double[][] extractEdgeTrajectory(BufferedImage edgeImage) {
        int width = edgeImage.getWidth();
        int height = edgeImage.getHeight();
        double[][] trajectory = new double[height][width];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = edgeImage.getRGB(x, y);
                int edgeVal = (rgb >> 16) & 0xFF;
                trajectory[y][x] = edgeVal / 255.0;
            }
        }
        
        return trajectory;
    }
    
    /**
     * Detect corners using simple corner detection
     * 
     * @param image Input image
     * @param threshold Corner threshold
     * @return Array of corner points [x, y]
     */
    public int[][] detectCorners(BufferedImage image, double threshold) {
        List<int[]> corners = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Simple corner detection based on intensity changes
        for (int y = 5; y < height - 5; y++) {
            for (int x = 5; x < width - 5; x++) {
                int center = (image.getRGB(x, y) >> 16) & 0xFF;
                
                // Check 8 directions
                int[] directions = {
                    (image.getRGB(x - 1, y) >> 16) & 0xFF,
                    (image.getRGB(x + 1, y) >> 16) & 0xFF,
                    (image.getRGB(x, y - 1) >> 16) & 0xFF,
                    (image.getRGB(x, y + 1) >> 16) & 0xFF,
                    (image.getRGB(x - 1, y - 1) >> 16) & 0xFF,
                    (image.getRGB(x + 1, y - 1) >> 16) & 0xFF,
                    (image.getRGB(x - 1, y + 1) >> 16) & 0xFF,
                    (image.getRGB(x + 1, y + 1) >> 16) & 0xFF
                };
                
                // Calculate variance
                double mean = 0;
                for (int d : directions) mean += d;
                mean /= 8;
                
                double variance = 0;
                for (int d : directions) variance += (d - mean) * (d - mean);
                variance /= 8;
                
                if (variance > threshold * 255 * 255) {
                    corners.add(new int[]{x, y});
                }
            }
        }
        
        return corners.toArray(new int[0][]);
    }
    
    /**
     * Extract region of interest
     * 
     * @param image Input image
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     * @return ROI image
     */
    public BufferedImage extractROI(BufferedImage image, int x, int y, int width, int height) {
        return image.getSubimage(x, y, width, height);
    }
    
    /**
     * Apply phi-harmonic thresholding
     * 
     * @param image Input image
     * @return Thresholded image
     */
    public BufferedImage phiThreshold(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage thresholded = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                int thresholdVal = gray > (EDGE_THRESHOLD * 255) ? 255 : 0;
                int thresholdRGB = (thresholdVal << 16) | (thresholdVal << 8) | thresholdVal;
                thresholded.setRGB(x, y, thresholdRGB);
            }
        }
        
        return thresholded;
    }
    
    /**
     * Reduce noise using median filter
     * 
     * @param image Input image
     * @param kernelSize Kernel size
     * @return Denoised image
     */
    public BufferedImage medianFilter(BufferedImage image, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage filtered = new BufferedImage(width, height, image.getType());
        
        for (int y = kernelSize / 2; y < height - kernelSize / 2; y++) {
            for (int x = kernelSize / 2; x < width - kernelSize / 2; x++) {
                List<Integer> values = new ArrayList<>();
                
                for (int ky = -kernelSize / 2; ky <= kernelSize / 2; ky++) {
                    for (int kx = -kernelSize / 2; kx <= kernelSize / 2; kx++) {
                        int rgb = image.getRGB(x + kx, y + ky);
                        int gray = (rgb >> 16) & 0xFF;
                        values.add(gray);
                    }
                }
                
                java.util.Collections.sort(values);
                int median = values.get(values.size() / 2);
                int medianRGB = (median << 16) | (median << 8) | median;
                filtered.setRGB(x, y, medianRGB);
            }
        }
        
        return filtered;
    }
    
    /**
     * Calculate image histogram
     * 
     * @param image Input image
     * @return Histogram array (256 bins)
     */
    public int[] calculateHistogram(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                histogram[gray]++;
            }
        }
        
        return histogram;
    }
    
    /**
     * Calculate phi-harmonic contrast
     * 
     * @param histogram Image histogram
     * @return Contrast score
     */
    public double calculatePhiContrast(int[] histogram) {
        double total = 0;
        double sum = 0;
        
        for (int i = 0; i < histogram.length; i++) {
            total += histogram[i];
            sum += i * histogram[i];
        }
        
        double mean = sum / total;
        double variance = 0;
        
        for (int i = 0; i < histogram.length; i++) {
            variance += histogram[i] * (i - mean) * (i - mean);
        }
        
        variance /= total;
        double stdDev = Math.sqrt(variance);
        
        // Phi-harmonic contrast
        return stdDev / 128.0; // Normalize by max stdDev
    }
}
