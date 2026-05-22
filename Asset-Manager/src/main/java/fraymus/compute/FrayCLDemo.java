package fraymus.compute;

/**
 * 🧬 FRAYCL DEMO
 * 
 * Demonstration of FrayCL compute abstraction
 * Shows how to use pure Java compute without external OpenCL libraries
 */
public class FrayCLDemo {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧬 FRAYCL COMPUTE ABSTRACTION DEMO                  ║");
        System.out.println("║           Pure Java OpenCL Alternative                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Create compute context
        FrayCLContext context = new FrayCLContext();
        
        // Test 1: Vector Addition
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: Vector Addition");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        int size = 1000000;
        float[] a = new float[size];
        float[] b = new float[size];
        
        // Initialize arrays
        for (int i = 0; i < size; i++) {
            a[i] = i;
            b[i] = i * 2;
        }
        
        // Create buffers
        FrayCLBuffer bufferA = context.createFloatBuffer(size);
        FrayCLBuffer bufferB = context.createFloatBuffer(size);
        FrayCLBuffer bufferC = context.createFloatBuffer(size);
        
        // Write data
        bufferA.writeFloatArray(a);
        bufferB.writeFloatArray(b);
        
        // Create kernel
        FrayCLKernel addKernel = context.createKernel("vector_add", FrayCLKernels.vectorAdd());
        
        // Execute kernel
        long startTime = System.currentTimeMillis();
        context.execute(addKernel, new FrayCLBuffer[]{bufferA, bufferB}, new FrayCLBuffer[]{bufferC});
        long elapsed = System.currentTimeMillis() - startTime;
        
        // Read result
        float[] result = bufferC.readFloatArray();
        
        // Verify result
        boolean correct = true;
        for (int i = 0; i < Math.min(10, size); i++) {
            if (result[i] != a[i] + b[i]) {
                correct = false;
                break;
            }
        }
        
        System.out.println("   Size: " + size + " elements");
        System.out.println("   Time: " + elapsed + "ms");
        System.out.println("   Result verification: " + (correct ? "✅ PASS" : "❌ FAIL"));
        System.out.println("   Sample output: " + result[0] + ", " + result[1] + ", " + result[2] + "...");
        System.out.println();
        
        // Test 2: Sigmoid Activation
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: Sigmoid Activation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        FrayCLBuffer bufferD = context.createFloatBuffer(size);
        FrayCLKernel sigmoidKernel = context.createKernel("sigmoid", FrayCLKernels.sigmoid());
        
        startTime = System.currentTimeMillis();
        context.execute(sigmoidKernel, new FrayCLBuffer[]{bufferA}, new FrayCLBuffer[]{bufferD});
        elapsed = System.currentTimeMillis() - startTime;
        
        float[] sigmoidResult = bufferD.readFloatArray();
        
        System.out.println("   Size: " + size + " elements");
        System.out.println("   Time: " + elapsed + "ms");
        System.out.println("   Sample output: " + sigmoidResult[0] + ", " + sigmoidResult[1] + ", " + sigmoidResult[2] + "...");
        System.out.println();
        
        // Test 3: Custom Lambda Kernel
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: Custom Lambda Kernel (x^2 + 1)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        FrayCLKernel customKernel = context.createKernel("custom_square_plus_one", 
            (inputs, index, globalSize) -> {
                float[] data = inputs[0];
                float x = data[index];
                return x * x + 1;
            });
        
        FrayCLBuffer bufferE = context.createFloatBuffer(size);
        
        startTime = System.currentTimeMillis();
        context.execute(customKernel, new FrayCLBuffer[]{bufferA}, new FrayCLBuffer[]{bufferE});
        elapsed = System.currentTimeMillis() - startTime;
        
        float[] customResult = bufferE.readFloatArray();
        
        System.out.println("   Size: " + size + " elements");
        System.out.println("   Time: " + elapsed + "ms");
        System.out.println("   Sample output: " + customResult[0] + ", " + customResult[1] + ", " + customResult[2] + "...");
        System.out.println();
        
        // Cleanup
        bufferA.release();
        bufferB.release();
        bufferC.release();
        bufferD.release();
        bufferE.release();
        context.release();
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ FRAYCL DEMO COMPLETE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
