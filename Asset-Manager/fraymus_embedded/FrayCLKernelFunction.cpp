#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 FRAYCL KERNEL FUNCTION
*
* Functional interface for compute kernels
* Allows Java lambdas to be used as compute kernels
*/
@FunctionalInterface
public interface FrayCLKernelFunction {
/**
* Execute kernel function on multiple input arrays
*
* @param inputs Array of input data arrays
* @param index Current work item index
* @param globalSize Total work items
* @return Output value
*/
float execute(float[][] inputs, int index, int globalSize);
}
