#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class LivingDNA { {
public:
std::shared_ptr<Random> rng = std::make_shared<Random>();
public double harmonicFrequency;
public double resonance;
public double evolutionRate;
private int generation = 1;
private std::string inheritedStrategies = "";
public LivingDNA() {
this.harmonicFrequency = 432.0 + rng.nextDouble() * 20.0;
this.resonance = 0.5 + rng.nextDouble();
this.evolutionRate = 0.05;
}
public LivingDNA(double freq, double res, double evo) {
this.harmonicFrequency = freq;
this.resonance = res;
this.evolutionRate = evo;
}
public void evolve() {
harmonicFrequency += evolutionRate;
if (harmonicFrequency > 528.0) {
harmonicFrequency = 432.0;
}
}
public double pulse(double time) {
return Math.sin(harmonicFrequency * time * 0.0001) * resonance;
}
public LivingDNA copy() {
std::shared_ptr<LivingDNA> child = std::make_shared<LivingDNA>(harmonicFrequency, resonance, evolutionRate);
child.generation = this.generation;
child.inheritedStrategies = this.inheritedStrategies;
return child;
}
public double getHarmonicFrequency() { return harmonicFrequency; }
public double getResonance() { return resonance; }
public double getEvolutionRate() { return evolutionRate; }
public int getGeneration() { return generation; }
public void setGeneration(int gen) { this.generation = gen; }
public std::string getInheritedStrategies() { return inheritedStrategies; }
public void setInheritedStrategies(std::string strategies) { this.inheritedStrategies = strategies; }
public std::string toJavaCode() {
return std::string.format("new LivingDNA(%.3f, %.3f, %.3f)",
harmonicFrequency, resonance, evolutionRate);
}
@Override
public std::string toString() {
return std::string.format("DNA[freq=%.2fHz, res=%.2f, evo=%.3f]",
harmonicFrequency, resonance, evolutionRate);
}
}
