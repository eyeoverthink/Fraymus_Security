#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE TEMPORAL ARCHIVE
* "Every version is a victory."
*/
class TemporalArchive { {
public:
private static const std::string ARCHIVE_DIR = "fraymus_history/";
public TemporalArchive() {
std::shared_ptr<File> dir = std::make_shared<File>(ARCHIVE_DIR);
if (!dir.exists()) dir.mkdirs();
std::cout << "⏳ TEMPORAL ARCHIVE ACTIVE. HISTORY IS BEING WRITTEN." << std::endl;
}
public void preserve(std::string eventType, void* stateObject) {
std::string id = UUID.randomUUID().toString().substring(0, 8);
long timestamp = Instant.now().getEpochSecond();
new Thread(() -> saveToDisk(id, timestamp, eventType, stateObject)).start();
}
private void saveToDisk(std::string id, long timestamp, std::string type, void* data) {
std::string filename = std::string.format("%s%d_%s_%s.txt",
ARCHIVE_DIR, timestamp, type.replaceAll(" ", "_"), id);
try (FileWriter writer = new FileWriter(filename)) {
writer.write("ID: " + id + "\n");
writer.write("Timestamp: " + timestamp + "\n");
writer.write("Type: " + type + "\n");
writer.write("Data: " + data.toString() + "\n");
std::cout << "   [ARCHIVE] >> Fossilized: " + type << std::endl;
} catch (Exception e) { e.printStackTrace(); }
}
}
