#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Baseline optimization method for comparison
*/
public interface Baseline {
/**
* Baseline name for identification
*/
std::string name();
/**
* Run baseline with given context and budget
*/
EngineResult run(RunContext ctx, EngineBudget budget) throws Exception;
}
