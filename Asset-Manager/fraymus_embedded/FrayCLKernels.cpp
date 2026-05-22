#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 FRAYCL PRE-BUILT KERNELS
*
* Collection of common compute kernels for vector and matrix operations
* Ready-to-use kernels for neural network operations and scientific computing
*/
class FrayCLKernels { {
public:
/**
* Vector addition kernel: C = A + B
*
* @return Kernel function for vector addition
*/
public static FrayCLKernelFunction vectorAdd() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
float[] b = inputs.length > 1 ? inputs[1] : inputs[0];
return a[index] + b[index];
};
}
/**
* Vector subtraction kernel: C = A - B
*
* @return Kernel function for vector subtraction
*/
public static FrayCLKernelFunction vectorSub() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
float[] b = inputs[1];
return a[index] - b[index];
};
}
/**
* Vector multiplication kernel: C = A * B
*
* @return Kernel function for vector multiplication
*/
public static FrayCLKernelFunction vectorMul() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
float[] b = inputs[1];
return a[index] * b[index];
};
}
/**
* Scalar multiplication kernel: C = A * scalar
*
* @param scalar Scalar value to multiply
* @return Kernel function for scalar multiplication
*/
public static FrayCLKernelFunction scalarMul(float scalar) {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return a[index] * scalar;
};
}
/**
* Sigmoid activation kernel: C = sigmoid(A)
*
* @return Kernel function for sigmoid activation
*/
public static FrayCLKernelFunction sigmoid() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
float x = a[index];
return (float) (1.0 / (1.0 + Math.exp(-x)));
};
}
/**
* ReLU activation kernel: C = max(0, A)
*
* @return Kernel function for ReLU activation
*/
public static FrayCLKernelFunction relu() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return Math.max(0, a[index]);
};
}
/**
* Tanh activation kernel: C = tanh(A)
*
* @return Kernel function for tanh activation
*/
public static FrayCLKernelFunction tanh() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return (float) Math.tanh(a[index]);
};
}
/**
* Vector normalization kernel: C = A / ||A||
*
* @return Kernel function for vector normalization
*/
public static FrayCLKernelFunction normalize() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
// Compute L2 norm
float sum = 0;
for (float v : a) {
sum += v * v;
}
float norm = (float) Math.sqrt(sum);
if (norm == 0) return 0;
return a[index] / norm;
};
}
/**
* Square kernel: C = A^2
*
* @return Kernel function for squaring
*/
public static FrayCLKernelFunction square() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return a[index] * a[index];
};
}
/**
* Square root kernel: C = sqrt(A)
*
* @return Kernel function for square root
*/
public static FrayCLKernelFunction sqrt() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return (float) Math.sqrt(a[index]);
};
}
/**
* Absolute value kernel: C = |A|
*
* @return Kernel function for absolute value
*/
public static FrayCLKernelFunction abs() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return Math.abs(a[index]);
};
}
/**
* Custom identity kernel: C = A
* Useful for testing and data copying
*
* @return Kernel function for identity
*/
public static FrayCLKernelFunction identity() {
return (inputs, index, globalSize) -> {
float[] a = inputs[0];
return a[index];
};
}
}
