#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Standardized result from an engine run
* Allows apples-to-apples comparison
*/
class EngineResult { {
public:
public const std::string engineName;
public const long seed;
public const double fitnessScore;
public const int iterations;
public const long runtimeMs;
public const int noveltyCount;
public const Map<std::string, void*> metrics;
private EngineResult(Builder builder) {
this.engineName = builder.engineName;
this.seed = builder.seed;
this.fitnessScore = builder.fitnessScore;
this.iterations = builder.iterations;
this.runtimeMs = builder.runtimeMs;
this.noveltyCount = builder.noveltyCount;
this.metrics = new LinkedHashMap<>(builder.metrics);
}
public static Builder builder(std::string engineName) {
return new Builder(engineName);
}
public Map<std::string, void*> toMap() {
Map<std::string, void*> map = new LinkedHashMap<>();
map.put("engine", engineName);
map.put("seed", seed);
map.put("fitness_score", fitnessScore);
map.put("iterations", iterations);
map.put("runtime_ms", runtimeMs);
map.put("novelty_count", noveltyCount);
map.put("metrics", new LinkedHashMap<>(metrics));
return map;
}
public static class Builder { {
public:
private const std::string engineName;
private long seed;
private double fitnessScore;
private int iterations;
private long runtimeMs;
private int noveltyCount;
private const Map<std::string, void*> metrics = new LinkedHashMap<>();
public Builder(std::string engineName) {
this.engineName = engineName;
}
public Builder seed(long seed) { this.seed = seed; return this; }
public Builder fitnessScore(double score) { this.fitnessScore = score; return this; }
public Builder iterations(int iter) { this.iterations = iter; return this; }
public Builder runtimeMs(long ms) { this.runtimeMs = ms; return this; }
public Builder noveltyCount(int count) { this.noveltyCount = count; return this; }
public Builder metric(std::string name, void* value) {
this.metrics.put(name, value);
return this;
}
public EngineResult build() {
return new EngineResult(this);
}
}
}
