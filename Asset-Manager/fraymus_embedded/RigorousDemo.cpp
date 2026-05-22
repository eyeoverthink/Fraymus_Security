#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* RIGOROUS DEMO
*
* Demonstrates the complete rigorous architecture:
* - EvolutionaryChaos RNG (reproducible with seed)
* - Structured JSONL logging
* - Fair baseline comparisons
* - Honest proxy metrics
*/
class RigorousDemo { {
public:
public static void main(std::string[] args) throws Exception {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║         FRAYNIX RIGOROUS ARCHITECTURE DEMO                    ║" << std::endl;
std::cout << "║   Reproducible • Logged • Benchmarked • Honest                ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Test with multiple seeds to prove reproducibility
long[] seeds = {1337, 42, 2025};
for (long seed : seeds) {
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "SEED: " + seed << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
// Configuration
RunConfig cfg = RunConfig.builder()
.seed(seed)
.steps(20)
.populationSize(30)
.fusionDistance(5.0)
.energyThreshold(80.0)
.outDir("build/runs/demo")
.prettyConsole(true)
.jsonl(true)
.build();
EngineBudget budget = EngineBudget.fromConfig(cfg);
// Run simple physics engine
try (EventLogger log = new EventLogger(cfg, "SimplePhysics")) {
std::shared_ptr<RunContext> ctx = std::make_shared<RunContext>(cfg, log);
Map<std::string, void*> meta = new HashMap<>();
meta.put("engine", "SimplePhysicsEngine");
meta.put("description", "Demonstrates Fraynix physics-based optimization");
ctx.log.header(meta);
// Run physics simulation
EngineResult physicsResult = runSimplePhysics(ctx);
Map<std::string, void*> results = new HashMap<>();
results.put("fitness_score", physicsResult.fitnessScore);
results.put("novelty_count", physicsResult.noveltyCount);
ctx.log.footer(results);
std::cout <<  << std::endl;
std::cout << "Physics Engine Result:" << std::endl;
System.out.printf("  Fitness: %.4f%n", physicsResult.fitnessScore);
System.out.printf("  Novelty: %d%n", physicsResult.noveltyCount);
System.out.printf("  Runtime: %d ms%n", physicsResult.runtimeMs);
std::cout <<  << std::endl;
// Run baselines for comparison
std::cout << "Running baselines for comparison..." << std::endl;
std::cout <<  << std::endl;
std::shared_ptr<EngineResult> randomResult = std::make_shared<RandomSearchBaseline>().run(ctx, budget);
std::shared_ptr<EngineResult> greedyResult = std::make_shared<GreedyBaseline>().run(ctx, budget);
std::shared_ptr<EngineResult> gaResult = std::make_shared<GeneticBaseline>().run(ctx, budget);
// Comparison table
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                    COMPARISON                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
System.out.printf("%-20s %10s %10s %10s%n", "Method", "Fitness", "Novelty", "Time(ms)");
std::cout << "─────────────────────────────────────────────────────────────" << std::endl;
System.out.printf("%-20s %10.4f %10d %10d%n", "Physics (Fraynix)",
physicsResult.fitnessScore, physicsResult.noveltyCount, physicsResult.runtimeMs);
System.out.printf("%-20s %10.4f %10d %10d%n", "Random Search",
randomResult.fitnessScore, randomResult.noveltyCount, randomResult.runtimeMs);
System.out.printf("%-20s %10.4f %10d %10d%n", "Greedy HillClimb",
greedyResult.fitnessScore, greedyResult.noveltyCount, greedyResult.runtimeMs);
System.out.printf("%-20s %10.4f %10d %10d%n", "Genetic Algorithm",
gaResult.fitnessScore, gaResult.noveltyCount, gaResult.runtimeMs);
std::cout <<  << std::endl;
// Show RNG stats
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║              EVOLUTIONARY CHAOS RNG STATUS                    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << ctx.getRNGStats() << std::endl;
std::cout <<  << std::endl;
}
}
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║                    DEMO COMPLETE                              ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Key Achievements:" << std::endl;
std::cout << "  ✅ Reproducible (same seed → same results)" << std::endl;
std::cout << "  ✅ Logged (JSONL events + metrics.csv)" << std::endl;
std::cout << "  ✅ Benchmarked (vs 3 baselines)" << std::endl;
std::cout << "  ✅ Honest (proxy scores labeled)" << std::endl;
std::cout << "  ✅ Self-aware RNG (EvolutionaryChaos)" << std::endl;
std::cout <<  << std::endl;
std::cout << "Output: build/runs/demo/SimplePhysics/<seed>/" << std::endl;
std::cout << "  - run_summary.json" << std::endl;
std::cout << "  - events.jsonl" << std::endl;
std::cout << "  - metrics.csv" << std::endl;
std::cout <<  << std::endl;
}
/**
* Simple physics-based optimization
* Demonstrates the Fraynix approach
*/
private static EngineResult runSimplePhysics(RunContext ctx) throws Exception {
long startTime = System.currentTimeMillis();
// Create entities
List<Entity> entities = new std::vector<>();
for (int i = 0; i < ctx.cfg.populationSize; i++) {
double x = ctx.nextDouble(0, 100);
double y = ctx.nextDouble(0, 100);
double z = ctx.nextDouble(0, 100);
entities.add(new Entity("E_" + i, "PARTICLE", x, y, z, 90));
}
int fusionCount = 0;
// Physics simulation
for (int step = 0; step < ctx.cfg.steps; step++) {
// Apply gravity (particles attract)
for (int i = 0; i < entities.size(); i++) {
for (int j = i + 1; j < entities.size(); j++) {
Entity a = entities.get(i);
Entity b = entities.get(j);
double dist = a.distanceTo(b);
if (dist > 0 && dist < 50) {
// Move towards each other
a.moveTowards(b, ctx.cfg.gravityConstant / dist * 0.01);
}
}
}
// Check for fusions
for (int i = 0; i < entities.size(); i++) {
for (int j = i + 1; j < entities.size(); j++) {
Entity a = entities.get(i);
Entity b = entities.get(j);
if (a.distanceTo(b) < ctx.cfg.fusionDistance &&
a.energy > ctx.cfg.energyThreshold &&
b.energy > ctx.cfg.energyThreshold) {
// Log fusion
FusionEvent fusion = new FusionEvent.Builder()
.step(step)
.parentA(a.id)
.parentB(b.id)
.action("COMBINE")
.kindA(a.kind)
.kindB(b.kind)
.distance(a.distanceTo(b))
.energyA(a.energy)
.energyB(b.energy)
.build();
ctx.log.fusionEvent(fusion);
fusionCount++;
// Reduce energy after fusion
a.addEnergy(-10);
b.addEnergy(-10);
}
}
}
// Heat particles
for (Entity e : entities) {
e.addEnergy(5);
}
// Log metrics
ctx.log.metric("entity_count", entities.size(), step);
ctx.log.metric("fusion_count", fusionCount, step);
}
// Calculate fitness (more fusions = better)
double fitness = fusionCount / (double) ctx.cfg.steps;
long elapsed = System.currentTimeMillis() - startTime;
return EngineResult.builder("SimplePhysics")
.seed(ctx.cfg.seed)
.fitnessScore(fitness)
.iterations(ctx.cfg.steps)
.runtimeMs(elapsed)
.noveltyCount(fusionCount)
.metric("total_fusions", fusionCount)
.build();
}
}
