#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Immutable snapshot of HyperFormer state.
* Used for secure serialization in Cortical Stack Protocol.
*/
public record FraymusState(
long globalSeed,
Map<std::string, HyperVector> prototypes,
MultiScaleMemory.State memory
) implements Serializable {
private static const long serialVersionUID = 1L;
}
