package fraymus.compute;

/**
 * 🧬 FRAYCL DEVICE
 * 
 * Represents a compute device (CPU cores in this pure Java implementation)
 * Abstraction of hardware capabilities for compute operations
 */
public class FrayCLDevice {
    
    private final String name;
    private final int computeUnits;
    private final long maxMemory;
    private final int maxWorkGroupSize;
    private final long maxClockFrequency;
    
    /**
     * Create a FrayCL device representing the host CPU
     */
    public FrayCLDevice() {
        this.name = System.getProperty("os.name") + " " + System.getProperty("os.arch") + " CPU";
        this.computeUnits = Runtime.getRuntime().availableProcessors();
        this.maxMemory = Runtime.getRuntime().maxMemory();
        this.maxWorkGroupSize = computeUnits * 256; // Simulated work group size
        this.maxClockFrequency = estimateClockFrequency();
    }
    
    /**
     * Estimate CPU clock frequency (in MHz)
     * This is a rough approximation based on system properties
     * 
     * @return Estimated clock frequency in MHz
     */
    private long estimateClockFrequency() {
        // This is a simplified estimation
        // Real clock frequency detection would require native code
        return 3000; // Default to 3 GHz assumption
    }
    
    /**
     * Get device name
     * 
     * @return Device name string
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get number of compute units (CPU cores)
     * 
     * @return Number of compute units
     */
    public int getComputeUnits() {
        return computeUnits;
    }
    
    /**
     * Get maximum memory available
     * 
     * @return Maximum memory in bytes
     */
    public long getMaxMemory() {
        return maxMemory;
    }
    
    /**
     * Get maximum work group size
     * 
     * @return Maximum work group size
     */
    public int getMaxWorkGroupSize() {
        return maxWorkGroupSize;
    }
    
    /**
     * Get maximum clock frequency
     * 
     * @return Clock frequency in MHz
     */
    public long getMaxClockFrequency() {
        return maxClockFrequency;
    }
    
    /**
     * Get device information as string
     * 
     * @return Device information
     */
    @Override
    public String toString() {
        return "FrayCLDevice{" +
                "name='" + name + '\'' +
                ", computeUnits=" + computeUnits +
                ", maxMemory=" + (maxMemory / (1024 * 1024)) + " MB" +
                ", maxWorkGroupSize=" + maxWorkGroupSize +
                ", maxClockFrequency=" + maxClockFrequency + " MHz" +
                '}';
    }
}
