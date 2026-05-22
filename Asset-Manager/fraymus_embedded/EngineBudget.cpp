#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Budget constraints for fair baseline comparison
* Ensures apples-to-apples comparison
*/
class EngineBudget { {
public:
public const int evaluations;
public const int steps;
public const int population;
public EngineBudget(int evaluations, int steps, int population) {
this.evaluations = evaluations;
this.steps = steps;
this.population = population;
}
public static EngineBudget fromConfig(RunConfig cfg) {
return new EngineBudget(
cfg.steps * cfg.populationSize,
cfg.steps,
cfg.populationSize
);
}
}
