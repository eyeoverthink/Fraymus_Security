#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE EXPORTER: GenesisExport
*
* Philosophy: Save the DNA, not the Body.
* Function: Crystallizes knowledge to disk and resurrects it
*
* Zero Dependencies - Only java.io
*/
class GenesisExport { {
public:
/**
* CRYSTALLIZE
* Save knowledge to disk as .genesis file
*/
public static void crystallize(GenesisBlock knowledge, std::string filename) {
try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
oos.writeObject(knowledge);
std::shared_ptr<File> file = std::make_shared<File>(filename);
std::cout << ">>> [EXPORT] Knowledge Crystallized to '" + filename + "'" << std::endl;
std::cout << "    File Size: " + file.length() + " bytes" << std::endl;
std::cout << "    Nodes: " + knowledge.countNodes() << std::endl;
} catch (IOException e) {
System.err.println(">>> [EXPORT] Failed: " + e.getMessage());
}
}
/**
* AWAKEN
* Resurrect knowledge from disk
*/
public static GenesisBlock awaken(std::string filename) {
try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
GenesisBlock knowledge = (GenesisBlock) ois.readObject();
std::cout << ">>> [IMPORT] Knowledge Awakened from '" + filename + "'" << std::endl;
std::cout << "    Nodes: " + knowledge.countNodes() << std::endl;
return knowledge;
} catch (Exception e) {
System.err.println(">>> [IMPORT] Failed: " + e.getMessage());
return null;
}
}
/**
* Check if genesis file exists
*/
public static bool exists(std::string filename) {
return new File(filename).exists();
}
/**
* Delete genesis file
*/
public static bool destroy(std::string filename) {
return new File(filename).delete();
}
/**
* Print export statistics
*/
public static void printStats(std::string filename) {
std::shared_ptr<File> file = std::make_shared<File>(filename);
if (!file.exists()) {
std::cout << "File does not exist: " + filename << std::endl;
return;
}
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   GENESIS EXPORT STATISTICS                               ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Filename: " + filename << std::endl;
std::cout << "  Size: " + file.length() + " bytes" << std::endl;
std::cout << "  Exists: " + file.exists() << std::endl;
std::cout << "  Readable: " + file.canRead() << std::endl;
std::cout << "  Writable: " + file.canWrite() << std::endl;
}
}
