package fraymus.compute;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🧬 FRAYCL CONTEXT
 * 
 * Sovereign compute abstraction layer - pure Java OpenCL alternative
 * No external dependencies, complete control over compute infrastructure
 * 
 * ARCHITECTURE:
 * - Uses ForkJoinPool for parallel task execution
 * - DirectByteBuffer for device-like memory management
 * - Java lambdas as compute kernels
 * - Portable across all JVM implementations
 */
public class FrayCLContext {
    
    private final FrayCLDevice device;
    private final ForkJoinPool executor;
    private final AtomicLong bufferIdCounter;
    private final AtomicLong kernelIdCounter;
    
    /**
     * Create a new FrayCL compute context
     */
    public FrayCLContext() {
        this.device = new FrayCLDevice();
        this.executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        this.bufferIdCounter = new AtomicLong(0);
        this.kernelIdCounter = new AtomicLong(0);
        
        System.out.println(">>> [FRAYCL] Context initialized");
        System.out.println("   Device: " + device.getName());
        System.out.println("   Compute Units: " + device.getComputeUnits());
        System.out.println("   Max Memory: " + (device.getMaxMemory() / (1024 * 1024)) + " MB");
    }
    
    /**
     * Create a memory buffer
     * 
     * @param size Buffer size in bytes
     * @return FrayCLBuffer instance
     */
    public FrayCLBuffer createBuffer(long size) {
        long id = bufferIdCounter.incrementAndGet();
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
        buffer.order(ByteOrder.nativeOrder());
        
        System.out.println(">>> [FRAYCL] Buffer created: ID=" + id + ", Size=" + size + " bytes");
        return new FrayCLBuffer(id, buffer, this);
    }
    
    /**
     * Create a typed buffer for float arrays
     * 
     * @param elements Number of float elements
     * @return FrayCLBuffer instance
     */
    public FrayCLBuffer createFloatBuffer(int elements) {
        return createBuffer(elements * 4L); // 4 bytes per float
    }
    
    /**
     * Create a compute kernel from a Java lambda
     * 
     * @param name Kernel name
     * @param kernelFunction Lambda function implementing the kernel
     * @return FrayCLKernel instance
     */
    public FrayCLKernel createKernel(String name, FrayCLKernelFunction kernelFunction) {
        long id = kernelIdCounter.incrementAndGet();
        System.out.println(">>> [FRAYCL] Kernel created: ID=" + id + ", Name=" + name);
        return new FrayCLKernel(id, name, kernelFunction, this);
    }
    
    /**
     * Execute a kernel on buffers
     * 
     * @param kernel Kernel to execute
     * @param input Input buffer
     * @param output Output buffer
     */
    public void execute(FrayCLKernel kernel, FrayCLBuffer input, FrayCLBuffer output) {
        execute(kernel, new FrayCLBuffer[]{input}, new FrayCLBuffer[]{output});
    }
    
    /**
     * Execute a kernel with multiple buffers
     * 
     * @param kernel Kernel to execute
     * @param inputs Input buffers
     * @param outputs Output buffers
     */
    public void execute(FrayCLKernel kernel, FrayCLBuffer[] inputs, FrayCLBuffer[] outputs) {
        long startTime = System.currentTimeMillis();
        
        try {
            kernel.execute(inputs, outputs);
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println(">>> [FRAYCL] Kernel executed: " + kernel.getName() + " in " + elapsed + "ms");
        } catch (Exception e) {
            System.err.println(">>> [FRAYCL] Kernel execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the compute device
     * 
     * @return FrayCLDevice instance
     */
    public FrayCLDevice getDevice() {
        return device;
    }
    
    /**
     * Get the thread pool executor
     * 
     * @return ForkJoinPool instance
     */
    public ForkJoinPool getExecutor() {
        return executor;
    }
    
    /**
     * Release context resources
     */
    public void release() {
        executor.shutdown();
        System.out.println(">>> [FRAYCL] Context released");
    }
}
