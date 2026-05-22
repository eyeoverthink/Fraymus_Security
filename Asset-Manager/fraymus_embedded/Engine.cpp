#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Standardized interface for all optimization engines
*/
public interface Engine {
/**
* Engine name for logging and identification
*/
std::string name();
/**
* Run the engine with given context
* Returns results for comparison
*/
EngineResult run(RunContext ctx) throws Exception;
}
