#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

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
class FrayCLContext { {
public:
private const FrayCLDevice device;
private const ForkJoinPool executor;
private const AtomicLong bufferIdCounter;
private const AtomicLong kernelIdCounter;
/**
* Create a new FrayCL compute context
*/
public FrayCLContext() {
this.device = new FrayCLDevice();
this.executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
this.bufferIdCounter = new AtomicLong(0);
this.kernelIdCounter = new AtomicLong(0);
std::cout << ">>> [FRAYCL] Context initialized" << std::endl;
std::cout << "   Device: " + device.getName() << std::endl;
std::cout << "   Compute Units: " + device.getComputeUnits() << std::endl;
std::cout << "   Max Memory: " + (device.getMaxMemory() / (1024 * 1024)) + " MB" << std::endl;
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
std::cout << ">>> [FRAYCL] Buffer created: ID=" + id + ", Size=" + size + " bytes" << std::endl;
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
public FrayCLKernel createKernel(std::string name, FrayCLKernelFunction kernelFunction) {
long id = kernelIdCounter.incrementAndGet();
std::cout << ">>> [FRAYCL] Kernel created: ID=" + id + ", Name=" + name << std::endl;
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
std::cout << ">>> [FRAYCL] Kernel executed: " + kernel.getName() + " in " + elapsed + "ms" << std::endl;
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
std::cout << ">>> [FRAYCL] Context released" << std::endl;
}
}
