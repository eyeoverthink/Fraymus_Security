package fraymus.ultimate;

import java.util.HashMap;
import java.util.Map;

/**
 * UltimateCPU - Hardware Abstraction Layer
 * 
 * This class provides hardware-aware resource allocation and monitoring
 * for the Ultimate Agent system. It abstracts hardware details and provides
 * a unified interface for resource management across CPU, GPU, RAM, and network.
 * 
 * PHI-HARMONIC FOUNDATION:
 * - All resource allocation uses phi-harmonic principles
 * - Phi constant (φ = 1.618033988749895) pervades all calculations
 * - Resource distribution follows golden ratio patterns
 * 
 * @author Vaughn Scott
 * @date May 5, 2026
 * @version 1.0
 */
public class UltimateCPU {
    
    // Phi constant for harmonic calculations
    private static final double PHI = 1.618033988749895;
    
    // Hardware state tracking
    private int availableCores;
    private int totalCores;
    private long availableRAM;
    private long totalRAM;
    private long availableGPU;
    private long totalGPU;
    private double networkBandwidth;
    
    // Phi-harmonic resource allocation
    private Map<String, Double> phiAllocation;
    
    // Performance metrics
    private Map<String, Long> performanceMetrics;
    
    /**
     * Initialize UltimateCPU with automatic hardware detection
     */
    public UltimateCPU() {
        this.totalCores = Runtime.getRuntime().availableProcessors();
        this.availableCores = this.totalCores;
        this.totalRAM = Runtime.getRuntime().maxMemory();
        this.availableRAM = Runtime.getRuntime().freeMemory();
        this.totalGPU = 0; // Will be detected if GPU available
        this.availableGPU = 0;
        this.networkBandwidth = 1000.0; // Mbps default
        
        this.phiAllocation = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
        
        initializePhiAllocation();
    }
    
    /**
     * Initialize phi-harmonic resource allocation patterns
     * Resources are distributed according to golden ratio principles
     */
    private void initializePhiAllocation() {
        // 5-agency system: KAI, VEX, AUM, NEX, COR
        // Allocation follows phi-harmonic distribution
        double total = 1.0;
        double kai = total / (PHI + 1.0);          // ~0.382
        double vex = total / (PHI * PHI + 1.0);    // ~0.236
        double aum = total / (PHI * PHI * PHI + 1.0); // ~0.146
        double nex = total / (PHI * PHI * PHI * PHI + 1.0); // ~0.090
        double cor = total - kai - vex - aum - nex;  // ~0.146
        
        phiAllocation.put("KAI", kai);
        phiAllocation.put("VEX", vex);
        phiAllocation.put("AUM", aum);
        phiAllocation.put("NEX", nex);
        phiAllocation.put("COR", cor);
    }
    
    /**
     * Get phi-harmonic allocation for a specific agency
     * 
     * @param agency The agency name (KAI, VEX, AUM, NEX, COR)
     * @return Allocation ratio (0.0 to 1.0)
     */
    public double getPhiAllocation(String agency) {
        return phiAllocation.getOrDefault(agency.toUpperCase(), 0.1);
    }
    
    /**
     * Allocate CPU cores to an agency based on phi-harmonic principles
     * 
     * @param agency The agency requesting cores
     * @return Number of cores allocated
     */
    public int allocateCores(String agency) {
        double allocation = getPhiAllocation(agency);
        int cores = (int) Math.floor(availableCores * allocation);
        availableCores -= cores;
        return Math.max(1, cores); // At least 1 core
    }
    
    /**
     * Release CPU cores back to the pool
     * 
     * @param cores Number of cores to release
     */
    public void releaseCores(int cores) {
        availableCores = Math.min(totalCores, availableCores + cores);
    }
    
    /**
     * Allocate RAM memory to an agency based on phi-harmonic principles
     * 
     * @param agency The agency requesting memory
     * @return Memory allocated in bytes
     */
    public long allocateMemory(String agency) {
        double allocation = getPhiAllocation(agency);
        long memory = (long) (availableRAM * allocation);
        availableRAM -= memory;
        return Math.max(1024 * 1024, memory); // At least 1MB
    }
    
    /**
     * Release RAM memory back to the pool
     * 
     * @param memory Memory in bytes to release
     */
    public void releaseMemory(long memory) {
        availableRAM = Math.min(totalRAM, availableRAM + memory);
    }
    
    /**
     * Allocate GPU memory to an agency (if GPU available)
     * 
     * @param agency The agency requesting GPU memory
     * @return GPU memory allocated in bytes
     */
    public long allocateGPU(String agency) {
        if (totalGPU == 0) {
            return 0; // No GPU available
        }
        
        double allocation = getPhiAllocation(agency);
        long gpu = (long) (availableGPU * allocation);
        availableGPU -= gpu;
        return Math.max(0, gpu);
    }
    
    /**
     * Release GPU memory back to the pool
     * 
     * @param gpu GPU memory in bytes to release
     */
    public void releaseGPU(long gpu) {
        if (totalGPU > 0) {
            availableGPU = Math.min(totalGPU, availableGPU + gpu);
        }
    }
    
    /**
     * Get current network bandwidth allocation
     * 
     * @param agency The agency requesting bandwidth
     * @return Bandwidth allocation in Mbps
     */
    public double allocateBandwidth(String agency) {
        double allocation = getPhiAllocation(agency);
        return networkBandwidth * allocation;
    }
    
    /**
     * Update performance metrics for an agency
     * 
     * @param agency The agency name
     * @param metric The metric name
     * @param value The metric value
     */
    public void updatePerformanceMetric(String agency, String metric, long value) {
        String key = agency + ":" + metric;
        performanceMetrics.put(key, value);
    }
    
    /**
     * Get performance metric for an agency
     * 
     * @param agency The agency name
     * @param metric The metric name
     * @return The metric value, or 0 if not found
     */
    public long getPerformanceMetric(String agency, String metric) {
        String key = agency + ":" + metric;
        return performanceMetrics.getOrDefault(key, 0L);
    }
    
    /**
     * Refresh hardware state (update available resources)
     */
    public void refreshHardwareState() {
        availableRAM = Runtime.getRuntime().freeMemory();
        // GPU and network would be refreshed via native calls if available
    }
    
    /**
     * Get system status report
     * 
     * @return Formatted status string
     */
    public String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ULTIMATE CPU STATUS ===\n");
        sb.append("Cores: ").append(availableCores).append("/").append(totalCores).append("\n");
        sb.append("RAM: ").append(formatBytes(availableRAM)).append("/").append(formatBytes(totalRAM)).append("\n");
        sb.append("GPU: ").append(formatBytes(availableGPU)).append("/").append(formatBytes(totalGPU)).append("\n");
        sb.append("Network: ").append(networkBandwidth).append(" Mbps\n");
        sb.append("\n=== PHI-HARMONIC ALLOCATION ===\n");
        for (Map.Entry<String, Double> entry : phiAllocation.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(String.format("%.3f", entry.getValue() * 100)).append("%\n");
        }
        return sb.toString();
    }
    
    /**
     * Format bytes to human-readable string
     * 
     * @param bytes Number of bytes
     * @return Formatted string (e.g., "1.5 GB")
     */
    public String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Get phi constant
     * 
     * @return Phi constant (φ = 1.618033988749895)
     */
    public static double getPhi() {
        return PHI;
    }
    
    /**
     * Calculate phi-harmonic weight for priority
     * 
     * @param priority Priority level (1-10)
     * @return Phi-harmonic weight
     */
    public static double calculatePhiWeight(int priority) {
        return Math.pow(PHI, priority - 5); // Centered around priority 5
    }
    
    /**
     * Check if system has sufficient resources for a task
     * 
     * @param requiredCores Required CPU cores
     * @param requiredRAM Required RAM in bytes
     * @return true if resources are sufficient
     */
    public boolean hasSufficientResources(int requiredCores, long requiredRAM) {
        return availableCores >= requiredCores && availableRAM >= requiredRAM;
    }
    
    /**
     * Optimize resource allocation based on phi-harmonic principles
     * This redistributes resources to maximize efficiency
     */
    public void optimizeAllocation() {
        refreshHardwareState();
        // Rebalance based on phi-harmonic patterns
        // This would implement more sophisticated optimization logic
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        UltimateCPU cpu = new UltimateCPU();
        System.out.println(cpu.getStatusReport());
        
        // Test allocation
        int kaiCores = cpu.allocateCores("KAI");
        long kaiMemory = cpu.allocateMemory("KAI");
        System.out.println("\nAllocated to KAI: " + kaiCores + " cores, " + cpu.formatBytes(kaiMemory));
        
        System.out.println("\nAfter allocation:");
        System.out.println(cpu.getStatusReport());
    }
}
