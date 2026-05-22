#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 FRAYCL DEMO
*
* Demonstration of FrayCL compute abstraction
* Shows how to use pure Java compute without external OpenCL libraries
*/
class FrayCLDemo { {
public:
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║           🧬 FRAYCL COMPUTE ABSTRACTION DEMO                  ║" << std::endl;
std::cout << "║           Pure Java OpenCL Alternative                       ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Create compute context
std::shared_ptr<FrayCLContext> context = std::make_shared<FrayCLContext>();
// Test 1: Vector Addition
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
std::cout << "TEST 1: Vector Addition" << std::endl;
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
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
bool correct = true;
for (int i = 0; i < Math.min(10, size); i++) {
if (result[i] != a[i] + b[i]) {
correct = false;
break;
}
}
std::cout << "   Size: " + size + " elements" << std::endl;
std::cout << "   Time: " + elapsed + "ms" << std::endl;
std::cout << "   Result verification: " + (correct ? "✅ PASS" : "❌ FAIL") << std::endl;
std::cout << "   Sample output: " + result[0] + ", " + result[1] + ", " + result[2] + "..." << std::endl;
std::cout <<  << std::endl;
// Test 2: Sigmoid Activation
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
std::cout << "TEST 2: Sigmoid Activation" << std::endl;
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
FrayCLBuffer bufferD = context.createFloatBuffer(size);
FrayCLKernel sigmoidKernel = context.createKernel("sigmoid", FrayCLKernels.sigmoid());
startTime = System.currentTimeMillis();
context.execute(sigmoidKernel, new FrayCLBuffer[]{bufferA}, new FrayCLBuffer[]{bufferD});
elapsed = System.currentTimeMillis() - startTime;
float[] sigmoidResult = bufferD.readFloatArray();
std::cout << "   Size: " + size + " elements" << std::endl;
std::cout << "   Time: " + elapsed + "ms" << std::endl;
std::cout << "   Sample output: " + sigmoidResult[0] + ", " + sigmoidResult[1] + ", " + sigmoidResult[2] + "..." << std::endl;
std::cout <<  << std::endl;
// Test 3: Custom Lambda Kernel
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
std::cout << "TEST 3: Custom Lambda Kernel (x^2 + 1)" << std::endl;
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
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
std::cout << "   Size: " + size + " elements" << std::endl;
std::cout << "   Time: " + elapsed + "ms" << std::endl;
std::cout << "   Sample output: " + customResult[0] + ", " + customResult[1] + ", " + customResult[2] + "..." << std::endl;
std::cout <<  << std::endl;
// Cleanup
bufferA.release();
bufferB.release();
bufferC.release();
bufferD.release();
bufferE.release();
context.release();
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
std::cout << "✅ FRAYCL DEMO COMPLETE" << std::endl;
std::cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << std::endl;
}
}
