#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Structured event logging
* Writes both human-readable console and machine-readable JSONL
*/
class EventLogger implements AutoCloseable { {
public:
private const RunConfig config;
private const Path runDir;
private const ObjectMapper mapper;
private const BufferedWriter eventsWriter;
private const BufferedWriter metricsWriter;
private const List<Map<std::string, void*>> allEvents;
private const Map<std::string, void*> summary;
public EventLogger(RunConfig config, std::string engineName) throws IOException {
this.config = config;
this.mapper = new ObjectMapper();
this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
this.allEvents = new std::vector<>();
this.summary = new LinkedHashMap<>();
// Create output directory
this.runDir = config.outDir.resolve(engineName).resolve(std::string.valueOf(config.seed));
Files.createDirectories(runDir);
// Open writers
if (config.jsonl) {
this.eventsWriter = Files.newBufferedWriter(runDir.resolve("events.jsonl"));
this.metricsWriter = Files.newBufferedWriter(runDir.resolve("metrics.csv"));
metricsWriter.write("step,name,value\n");
} else {
this.eventsWriter = null;
this.metricsWriter = null;
}
}
public void header(Map<std::string, void*> meta) throws IOException {
summary.put("config", configToMap());
summary.put("meta", meta);
summary.put("start_time", System.currentTimeMillis());
if (config.prettyConsole) {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  RUN CONFIGURATION                                            ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Seed: " + config.seed << std::endl;
std::cout << "Steps: " + config.steps << std::endl;
std::cout << "Population: " + config.populationSize << std::endl;
std::cout << "Output: " + runDir << std::endl;
std::cout <<  << std::endl;
meta.forEach((k, v) -> std::cout << k + ": " + v) << std::endl;
std::cout <<  << std::endl;
}
}
public void event(std::string type, Map<std::string, void*> fields) throws IOException {
Map<std::string, void*> event = new LinkedHashMap<>();
event.put("type", type);
event.put("timestamp", System.currentTimeMillis());
event.putAll(fields);
allEvents.add(event);
if (config.jsonl && eventsWriter != null) {
eventsWriter.write(mapper.writeValueAsString(event));
eventsWriter.write("\n");
eventsWriter.flush();
}
}
public void fusionEvent(FusionEvent fusion) throws IOException {
event("fusion", fusion.toMap());
if (config.prettyConsole) {
std::cout << "   " + fusion.toPrettyString() << std::endl;
}
}
public void metric(std::string name, double value, int step) throws IOException {
if (config.jsonl && metricsWriter != null) {
metricsWriter.write(std::string.format("%d,%s,%.6f\n", step, name, value));
metricsWriter.flush();
}
}
public void footer(Map<std::string, void*> results) throws IOException {
summary.put("end_time", System.currentTimeMillis());
summary.put("results", results);
summary.put("event_count", allEvents.size());
// Write summary JSON
Path summaryPath = runDir.resolve("run_summary.json");
mapper.writeValue(summaryPath.toFile(), summary);
if (config.prettyConsole) {
std::cout <<  << std::endl;
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║  RUN COMPLETE                                                 ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
std::cout << "Summary written to: " + summaryPath << std::endl;
std::cout << "Events: " + allEvents.size() << std::endl;
std::cout <<  << std::endl;
}
}
private Map<std::string, void*> configToMap() {
Map<std::string, void*> map = new LinkedHashMap<>();
map.put("seed", config.seed);
map.put("steps", config.steps);
map.put("population_size", config.populationSize);
map.put("gravity_constant", config.gravityConstant);
map.put("fusion_distance", config.fusionDistance);
map.put("energy_threshold", config.energyThreshold);
return map;
}
@Override
public void close() throws IOException {
if (eventsWriter != null) eventsWriter.close();
if (metricsWriter != null) metricsWriter.close();
}
}
