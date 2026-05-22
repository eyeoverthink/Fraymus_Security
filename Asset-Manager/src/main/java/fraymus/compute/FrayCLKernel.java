package fraymus.compute;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * 🧬 FRAYCL KERNEL
 * 
 * Compute kernel that executes a function on data in parallel
 * Uses ForkJoinPool for parallel task execution
 */
public class FrayCLKernel {
    
    private final long id;
    private final String name;
    private final FrayCLKernelFunction function;
    private final FrayCLContext context;
    
    /**
     * Create a FrayCL kernel
     * 
     * @param id Kernel ID
     * @param name Kernel name
     * @param function Kernel function to execute
     * @param context Parent context
     */
    public FrayCLKernel(long id, String name, FrayCLKernelFunction function, FrayCLContext context) {
        this.id = id;
        this.name = name;
        this.function = function;
        this.context = context;
    }
    
    /**
     * Get kernel ID
     * 
     * @return Kernel ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Get kernel name
     * 
     * @return Kernel name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Execute kernel with single input and output buffer
     * 
     * @param input Input buffer
     * @param output Output buffer
     */
    public void execute(FrayCLBuffer input, FrayCLBuffer output) {
        execute(new FrayCLBuffer[]{input}, new FrayCLBuffer[]{output});
    }
    
    /**
     * Execute kernel with multiple input and output buffers
     * 
     * @param inputs Input buffers
     * @param outputs Output buffers
     */
    public void execute(FrayCLBuffer[] inputs, FrayCLBuffer[] outputs) {
        if (inputs.length == 0 || outputs.length == 0) {
            throw new IllegalArgumentException("Inputs and outputs cannot be empty");
        }
        
        // Read input data
        float[][] inputData = new float[inputs.length][];
        for (int i = 0; i < inputs.length; i++) {
            inputData[i] = inputs[i].readFloatArray();
        }
        
        // Determine output size
        int globalSize = inputData[0].length;
        float[] outputData = new float[globalSize];
        
        // Execute kernel in parallel
        ForkJoinPool executor = context.getExecutor();
        KernelTask task = new KernelTask(inputData, outputData, 0, globalSize);
        executor.invoke(task);
        
        // Write output data
        outputs[0].writeFloatArray(outputData);
    }
    
    /**
     * Internal task for parallel kernel execution
     */
    private class KernelTask extends RecursiveAction {
        
        private final float[][] inputs;
        private final float[] output;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 1000;
        
        KernelTask(float[][] inputs, float[] output, int start, int end) {
            this.inputs = inputs;
            this.output = output;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                // Execute sequentially for small tasks
                for (int i = start; i < end; i++) {
                    output[i] = function.execute(inputs, i, inputs[0].length);
                }
            } else {
                // Split task for parallel execution
                int mid = (start + end) / 2;
                KernelTask left = new KernelTask(inputs, output, start, mid);
                KernelTask right = new KernelTask(inputs, output, mid, end);
                invokeAll(left, right);
            }
        }
    }
    
    /**
     * Execute kernel with custom global size
     * 
     * @param inputs Input buffers
     * @param outputs Output buffers
     * @param globalSize Total work items
     */
    public void execute(FrayCLBuffer[] inputs, FrayCLBuffer[] outputs, int globalSize) {
        if (inputs.length == 0 || outputs.length == 0) {
            throw new IllegalArgumentException("Inputs and outputs cannot be empty");
        }
        
        // Read input data
        float[][] inputData = new float[inputs.length][];
        for (int i = 0; i < inputs.length; i++) {
            inputData[i] = inputs[i].readFloatArray();
        }
        
        // Create output array
        float[] outputData = new float[globalSize];
        
        // Execute kernel in parallel
        ForkJoinPool executor = context.getExecutor();
        KernelTask task = new KernelTask(inputData, outputData, 0, globalSize);
        executor.invoke(task);
        
        // Write output data
        outputs[0].writeFloatArray(outputData);
    }
    
    @Override
    public String toString() {
        return "FrayCLKernel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
