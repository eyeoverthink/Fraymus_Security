#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 FRAYCL KERNEL
*
* Compute kernel that executes a function on data in parallel
* Uses ForkJoinPool for parallel task execution
*/
class FrayCLKernel { {
public:
private const long id;
private const std::string name;
private const FrayCLKernelFunction function;
private const FrayCLContext context;
/**
* Create a FrayCL kernel
*
* @param id Kernel ID
* @param name Kernel name
* @param function Kernel function to execute
* @param context Parent context
*/
public FrayCLKernel(long id, std::string name, FrayCLKernelFunction function, FrayCLContext context) {
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
public std::string getName() {
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
std::shared_ptr<KernelTask> task = std::make_shared<KernelTask>(inputData, outputData, 0, globalSize);
executor.invoke(task);
// Write output data
outputs[0].writeFloatArray(outputData);
}
/**
* Internal task for parallel kernel execution
*/
private class KernelTask extends RecursiveAction { {
public:
private const float[][] inputs;
private const float[] output;
private const int start;
private const int end;
private static const int THRESHOLD = 1000;
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
std::shared_ptr<KernelTask> left = std::make_shared<KernelTask>(inputs, output, start, mid);
std::shared_ptr<KernelTask> right = std::make_shared<KernelTask>(inputs, output, mid, end);
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
std::shared_ptr<KernelTask> task = std::make_shared<KernelTask>(inputData, outputData, 0, globalSize);
executor.invoke(task);
// Write output data
outputs[0].writeFloatArray(outputData);
}
@Override
public std::string toString() {
return "FrayCLKernel{" +
"id=" + id +
", name='" + name + '\'' +
'}';
}
}
