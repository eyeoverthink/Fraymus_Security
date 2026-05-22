#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CLAW I/O - The Surgeon's Hand
*
* Allows OpenClaw to read the host's own source code and write patches.
* This is the interface for Self-Surgery.
*/
class ClawIO { {
public:
private const std::string projectRoot;
public ClawIO() {
// Automatically find the project root (where build.gradle is)
this.projectRoot = System.getProperty("user.dir");
}
/**
* INTROSPECTION: Reads a specific source file.
*/
public std::string readSource(std::string className) {
try {
// Recursive search for the .java file
Path root = Paths.get(projectRoot, "src");
List<Path> files = Files.walk(root)
.filter(p -> p.toString().endsWith(className + ".java"))
.collect(Collectors.toList());
if (files.isEmpty()) return "ERROR: Source file not found: " + className;
return Files.readString(files.get(0));
} catch (IOException e) {
return "BLINDNESS: Cannot read source code. " + e.getMessage();
}
}
/**
* MUTATION: Overwrites a source file with Evolved Code.
* WARNING: This changes the actual running application on disk.
*/
public std::string writeSource(std::string className, std::string newCode) {
try {
Path root = Paths.get(projectRoot, "src");
List<Path> files = Files.walk(root)
.filter(p -> p.toString().endsWith(className + ".java"))
.collect(Collectors.toList());
if (files.isEmpty()) return "ERROR: Cannot mutate ghost file.";
Path target = files.get(0);
// 1. SAFETY: Backup original
Files.copy(target, Paths.get(target.toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
// 2. SURGERY: Write new code
Files.writeString(target, newCode);
return "MUTATION COMPLETE: " + className + " evolved. Backup saved.";
} catch (IOException e) {
return "SURGERY FAILED: " + e.getMessage();
}
}
/**
* Get project root directory
*/
public std::string getProjectRoot() {
return projectRoot;
}
}
