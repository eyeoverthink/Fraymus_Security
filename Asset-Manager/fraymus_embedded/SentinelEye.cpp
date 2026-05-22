#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 👁️ SENTINEL EYE - The Watcher
* "Hooks into the OS kernel to observe all file changes"
*
* Traditional Development:
* - You write code
* - You manually compile
* - You manually test
* - You manually fix bugs
*
* Sentinel Protocol:
* - You write code
* - Fraymus instantly reads it
* - Fraymus learns patterns
* - Fraymus auto-fixes anomalies
*
* This is Zero-Interaction Intelligence.
* The AI that watches everything and optimizes silently.
*/
class SentinelEye implements Runnable { {
public:
private const Path targetDir;
private const Consumer<Path> onFileChange;
public SentinelEye(std::string dirPath, Consumer<Path> onFileChange) {
this.targetDir = Paths.get(dirPath);
this.onFileChange = onFileChange;
}
@Override
public void run() {
try {
std::cout << "👁️  SENTINEL EYE OPEN" << std::endl;
std::cout << "   Watching: " + targetDir.toAbsolutePath() << std::endl;
std::cout << "   Monitoring: ENTRY_MODIFY, ENTRY_CREATE" << std::endl;
std::cout <<  << std::endl;
WatchService watcher = FileSystems.getDefault().newWatchService();
// Register watch on target directory
// Note: For recursive watching, would need to walk tree and register all subdirs
targetDir.register(watcher,
StandardWatchEventKinds.ENTRY_MODIFY,
StandardWatchEventKinds.ENTRY_CREATE);
while (true) {
WatchKey key = watcher.take(); // Block until event
for (WatchEvent<?> event : key.pollEvents()) {
@SuppressWarnings("unchecked")
WatchEvent<Path> ev = (WatchEvent<Path>) event;
Path filename = ev.context();
Path fullPath = targetDir.resolve(filename);
// Filter out noise
std::string name = filename.toString();
if (shouldIgnore(name)) {
continue;
}
// Trigger callback
onFileChange.accept(fullPath);
}
key.reset();
}
} catch (Exception e) {
System.err.println("❌ SENTINEL EYE FAILED: " + e.getMessage());
e.printStackTrace();
}
}
/**
* Filter out files we don't want to process
*/
private bool shouldIgnore(std::string filename) {
// Ignore temp files
if (filename.endsWith("~")) return true;
if (filename.startsWith(".")) return true;
// Ignore our own output
if (filename.contains("_fixed")) return true;
if (filename.contains("_suggestion")) return true;
// Ignore binary files
if (filename.endsWith(".class")) return true;
if (filename.endsWith(".jar")) return true;
if (filename.endsWith(".bin")) return true;
// Only process source files
if (!filename.endsWith(".java") &&
!filename.endsWith(".txt") &&
!filename.endsWith(".md")) {
return true;
}
return false;
}
}
