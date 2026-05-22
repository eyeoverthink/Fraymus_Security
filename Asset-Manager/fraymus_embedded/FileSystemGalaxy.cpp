#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* FILE SYSTEM GALAXY - The Galaxy Mapper
*
* Turns your Project Folder into a Physics Simulation.
* Walks your directory, creates a NanoAgent for every file,
* and places them in 3D space based on file type and size.
*/
class FileSystemGalaxy { {
public:
private const GravityEngine universe;
std::shared_ptr<Random> rng = std::make_shared<Random>();
private int agentCount = 0;
public FileSystemGalaxy(GravityEngine universe) {
this.universe = universe;
}
public void ingest(std::string rootPath) {
std::cout << "🌌 MAPPING UNIVERSE: " + rootPath << std::endl;
std::shared_ptr<File> root = std::make_shared<File>(rootPath);
mapRecursively(root, 0, 0, 0);
std::cout << "✨ GALAXY MAPPED: " + agentCount + " nano-agents spawned" << std::endl;
}
private void mapRecursively(File dir, int cx, int cy, int cz) {
File[] files = dir.listFiles();
if (files == null) return;
for (File f : files) {
// Skip build directories and hidden files
if (f.getName().startsWith(".") ||
f.getName().equals("build") ||
f.getName().equals("node_modules") ||
f.getName().equals("target")) {
continue;
}
// Random offset for "Orbit"
int x = cx + rng.nextInt(20) - 10;
int y = cy + rng.nextInt(20) - 10;
int z = cz + rng.nextInt(20) - 10;
if (f.isDirectory()) {
// Directories are Centers of Gravity
mapRecursively(f, x, y, z);
} else if (f.getName().endsWith(".java")) {
// Java Files become Living Nano-Agents
std::shared_ptr<NanoAgent> agent = std::make_shared<NanoAgent>(f, x, y, z);
// Start the agent's life thread
new Thread(agent).start();
// Note: NanoAgent auto-registers via SpatialNode constructor
agentCount++;
std::cout << "   ⭐ Star Born: " + f.getName() + " at (" + x + "," + y + "," + z + ")" << std::endl;
}
}
}
public int getAgentCount() {
return agentCount;
}
}
