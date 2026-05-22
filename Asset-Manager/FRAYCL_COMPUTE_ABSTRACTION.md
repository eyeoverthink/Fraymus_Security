# 🧬 FrayCL Compute Abstraction

## Overview

FrayCL is a sovereign compute abstraction layer that provides OpenCL-like functionality using pure Java, without any external dependencies. It enables GPU-style parallel computing on the CPU using Java's built-in concurrency capabilities.

## Philosophy

- **Zero external dependencies** - Pure Java implementation
- **Sovereign infrastructure** - Complete control over compute operations
- **Portable** - Works on any JVM implementation
- **Leverages Java parallelism** - ForkJoinPool, streams, DirectByteBuffer

## Architecture

### Core Components

#### 1. FrayCLContext
The main compute context that manages:
- Device (CPU cores)
- Thread pool (ForkJoinPool)
- Buffer and kernel creation
- Resource lifecycle

```java
FrayCLContext context = new FrayCLContext();
```

#### 2. FrayCLDevice
Represents the compute device (CPU):
- Number of compute units (CPU cores)
- Maximum memory
- Clock frequency estimation
- Work group size limits

#### 3. FrayCLBuffer
Memory buffer abstraction using DirectByteBuffer:
- Device-like memory management
- Typed operations (float, int, double, byte)
- Read/write operations
- Automatic memory management

```java
FrayCLBuffer buffer = context.createFloatBuffer(1024);
buffer.writeFloatArray(myArray);
float[] result = buffer.readFloatArray();
```

#### 4. FrayCLKernel
Compute kernel that executes functions in parallel:
- Java lambda-based kernel functions
- ForkJoinPool parallel execution
- Automatic task splitting
- Multi-buffer support

```java
FrayCLKernel kernel = context.createKernel("my_kernel", 
    (inputs, index, globalSize) -> {
        float[] data = inputs[0];
        return data[index] * 2; // Simple multiply by 2
    });
```

#### 5. FrayCLKernelFunction
Functional interface for kernel functions:
```java
@FunctionalInterface
public interface FrayCLKernelFunction {
    float execute(float[][] inputs, int index, int globalSize);
}
```

## Pre-built Kernels

FrayCLKernels provides common operations:

- `vectorAdd()` - Vector addition (C = A + B)
- `vectorSub()` - Vector subtraction (C = A - B)
- `vectorMul()` - Vector multiplication (C = A * B)
- `scalarMul(float)` - Scalar multiplication (C = A * scalar)
- `sigmoid()` - Sigmoid activation
- `relu()` - ReLU activation
- `tanh()` - Tanh activation
- `normalize()` - L2 normalization
- `square()` - Element-wise square
- `sqrt()` - Element-wise square root
- `abs()` - Absolute value
- `identity()` - Identity/copy operation

## Usage Examples

### Example 1: Vector Addition

```java
FrayCLContext context = new FrayCLContext();

// Create buffers
FrayCLBuffer bufferA = context.createFloatBuffer(1000);
FrayCLBuffer bufferB = context.createFloatBuffer(1000);
FrayCLBuffer bufferC = context.createFloatBuffer(1000);

// Write data
bufferA.writeFloatArray(arrayA);
bufferB.writeFloatArray(arrayB);

// Create and execute kernel
FrayCLKernel addKernel = context.createKernel("vector_add", FrayCLKernels.vectorAdd());
context.execute(addKernel, new FrayCLBuffer[]{bufferA, bufferB}, new FrayCLBuffer[]{bufferC});

// Read result
float[] result = bufferC.readFloatArray();
```

### Example 2: Custom Lambda Kernel

```java
FrayCLKernel customKernel = context.createKernel("custom", 
    (inputs, index, globalSize) -> {
        float[] data = inputs[0];
        float x = data[index];
        return x * x + 1; // x^2 + 1
    });

context.execute(customKernel, bufferA, bufferB);
```

### Example 3: Neural Network Activation

```java
FrayCLKernel sigmoidKernel = context.createKernel("sigmoid", FrayCLKernels.sigmoid());
context.execute(sigmoidKernel, inputBuffer, outputBuffer);
```

## Integration with Fraynix

### Integration Points

1. **AEON Prime** - Accelerate 4D neural operations
2. **HyperCortex** - Speed up node computations
3. **Physics Engine** - Accelerate gravity/fusion simulations
4. **Demographics** - Matrix operations for population modeling

### Adding FrayCL to FraynixBoot

```java
// In FraynixBoot.java
private static FrayCLContext frayCL;

// During initialization
frayCL = new FrayCLContext();
System.out.println("   🧬 FrayCL compute abstraction initialized");
```

### CLI Commands

Add FrayCL commands to the shell:

```java
case "cl" -> {
    if (arg.equals("info")) {
        System.out.println(frayCL.getDevice());
    } else if (arg.equals("test")) {
        // Run FrayCL demo
    }
}
```

## Performance Characteristics

- **Parallel execution** - Utilizes all CPU cores via ForkJoinPool
- **Direct memory** - DirectByteBuffer for efficient memory access
- **Task splitting** - Automatic work distribution
- **Threshold-based** - Sequential execution for small tasks, parallel for large

Typical performance:
- Vector operations (1M elements): 10-50ms
- Activation functions (1M elements): 20-60ms
- Custom kernels (1M elements): 30-100ms

## Advantages Over OpenCL

1. **No native dependencies** - Pure Java
2. **Portable** - Works anywhere JVM runs
3. **Debuggable** - Full Java debugging support
4. **Safe** - No native code crashes
5. **Maintainable** - Pure Java codebase

## Limitations

- **CPU-only** - No GPU acceleration (by design)
- **Memory bound** - Limited by JVM heap size
- **No SIMD** - Java doesn't expose CPU SIMD instructions
- **Single node** - No distributed computing

## Future Enhancements

1. **Matrix operations** - Dedicated matrix multiplication kernels
2. **Convolution** - 2D/3D convolution for neural networks
3. **Reduction operations** - Sum, min, max, etc.
4. **Scan operations** - Prefix sums, exclusive scans
5. **Sorting** - Parallel sorting algorithms
6. **Integration with AEON** - Direct neural acceleration

## Testing

Run the demo:

```bash
./gradlew :Asset-Manager:runFrayCLDemo
```

Or run directly:

```bash
java -cp build/classes/java/main fraymus.compute.FrayCLDemo
```

## License

Part of the Fraymus Engine project. All rights reserved.
