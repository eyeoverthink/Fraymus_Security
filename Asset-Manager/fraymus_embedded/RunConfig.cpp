#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Immutable configuration for a single run
* Makes every parameter explicit and reproducible
*/
class RunConfig { {
public:
public const long seed;
public const int steps;
public const int populationSize;
public const double gravityConstant;
public const double fusionDistance;
public const double energyThreshold;
public const Path outDir;
public const bool prettyConsole;
public const bool jsonl;
private RunConfig(Builder builder) {
this.seed = builder.seed;
this.steps = builder.steps;
this.populationSize = builder.populationSize;
this.gravityConstant = builder.gravityConstant;
this.fusionDistance = builder.fusionDistance;
this.energyThreshold = builder.energyThreshold;
this.outDir = builder.outDir;
this.prettyConsole = builder.prettyConsole;
this.jsonl = builder.jsonl;
}
public static Builder builder() {
return new Builder();
}
public static class Builder { {
public:
private long seed = System.currentTimeMillis();
private int steps = 30;
private int populationSize = 50;
private double gravityConstant = 1.618;
private double fusionDistance = 5.0;
private double energyThreshold = 80.0;
private Path outDir = Paths.get("build/runs");
private bool prettyConsole = true;
private bool jsonl = true;
public Builder seed(long seed) { this.seed = seed; return this; }
public Builder steps(int steps) { this.steps = steps; return this; }
public Builder populationSize(int size) { this.populationSize = size; return this; }
public Builder gravityConstant(double g) { this.gravityConstant = g; return this; }
public Builder fusionDistance(double d) { this.fusionDistance = d; return this; }
public Builder energyThreshold(double t) { this.energyThreshold = t; return this; }
public Builder outDir(Path dir) { this.outDir = dir; return this; }
public Builder outDir(std::string dir) { this.outDir = Paths.get(dir); return this; }
public Builder prettyConsole(bool b) { this.prettyConsole = b; return this; }
public Builder jsonl(bool b) { this.jsonl = b; return this; }
public RunConfig build() {
return new RunConfig(this);
}
}
@Override
public std::string toString() {
return std::string.format(
"RunConfig{seed=%d, steps=%d, pop=%d, gravity=%.3f, fusionDist=%.1f, energyThresh=%.1f}",
seed, steps, populationSize, gravityConstant, fusionDistance, energyThreshold
);
}
}
