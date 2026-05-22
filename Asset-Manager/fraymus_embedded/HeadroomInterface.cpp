#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 📺 HEADROOM INTERFACE
* "The Controller of the Signal"
*
* This class controls the live broadcast. It: {
public:
* 1. Launches the Python broadcaster (HeadroomNode.py)
* 2. Sends speech commands (text-to-speech)
* 3. Triggers visual thought generation (LTX-Video)
* 4. Pushes thought images to the broadcast
* 5. Controls glitch intensity and neural activity
*
* The AI doesn't just respond - it BROADCASTS.
*/
class HeadroomInterface { {
public:
private PrintWriter signal;
private Process broadcast;
private Thread outputReader;
private AtomicBoolean broadcasting;
// Statistics
private int totalBroadcasts = 0;
private int totalThoughts = 0;
public HeadroomInterface() {
this.broadcasting = new AtomicBoolean(false);
}
/**
* GO LIVE - Start the broadcast
*/
public void goLive() {
if (broadcasting.get()) {
std::cout << "📺 Already broadcasting" << std::endl;
return;
}
try {
std::cout << "╔═══════════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║          📺 HEADROOM INTERFACE: GOING LIVE                    ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Check if Python script exists
Path scriptPath = Paths.get("HeadroomNode.py");
if (!Files.exists(scriptPath)) {
std::cout << "❌ HeadroomNode.py not found in current directory" << std::endl;
std::cout << "   Expected: " + scriptPath.toAbsolutePath() << std::endl;
return;
}
// Launch the Python Broadcaster
std::shared_ptr<ProcessBuilder> pb = std::make_shared<ProcessBuilder>("python", "HeadroomNode.py");
pb.redirectErrorStream(true);
broadcast = pb.start();
signal = new PrintWriter(broadcast.getOutputStream(), true);
broadcasting.set(true);
// Start thread to read broadcaster output
outputReader = new Thread(() -> {
try {
BufferedReader reader = new BufferedReader(
new InputStreamReader(broadcast.getInputStream())
);
std::string line;
while ((line = reader.readLine()) != null) {
std::cout << "   [BROADCAST] " + line << std::endl;
}
} catch (Exception e) {
if (broadcasting.get()) {
std::cout << "   [BROADCAST ERROR] " + e.getMessage() << std::endl;
}
}
}, "BroadcastReader");
outputReader.setDaemon(true);
outputReader.start();
// Give broadcaster time to initialize
Thread.sleep(1000);
std::cout << "✅ BROADCAST LIVE" << std::endl;
std::cout << "   A window should appear showing the signal" << std::endl;
std::cout <<  << std::endl;
} catch (Exception e) {
std::cout << "❌ FAILED TO GO LIVE: " + e.getMessage() << std::endl;
e.printStackTrace();
broadcasting.set(false);
}
}
/**
* BROADCAST - Speak text and show visual thought
*
* @param thought The text to speak
* @param visualConcept The concept to visualize
*/
public void broadcast(std::string thought, std::string visualConcept) {
broadcast(thought, visualConcept, 0.5);
}
/**
* BROADCAST - Speak text and show visual thought with neural activity
*
* @param thought The text to speak
* @param visualConcept The concept to visualize
* @param neuralActivity Neural activity level (0.0 = calm, 1.0 = intense)
*/
public void broadcast(std::string thought, std::string visualConcept, double neuralActivity) {
if (!broadcasting.get()) {
std::cout << "⚠️ Not broadcasting. Call goLive() first." << std::endl;
return;
}
totalBroadcasts++;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "📺 BROADCASTING" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "   Speech: " + thought << std::endl;
std::cout << "   Visual: " + visualConcept << std::endl;
std::cout << "   Neural: " + std::string.format("%.2f", neuralActivity) << std::endl;
std::cout <<  << std::endl;
try {
// 1. Send speech command
std::string speechJson = std::string.format(
"{\"speak\": \"%s\", \"neural\": %.2f}",
escapeJson(thought),
neuralActivity
);
signal.println(speechJson);
signal.flush();
// 2. Generate visual reflection (async)
if (VisualCortex.isAvailable()) {
new Thread(() -> {
try {
std::cout << "   🎥 Generating visual thought..." << std::endl;
// Calculate entropy from neural activity
double entropy = neuralActivity;
double consciousness = 1.0 - entropy;
// Generate video/image
VisualCortex.dream(
visualConcept,
entropy,
1.618033988749895,
consciousness
);
totalThoughts++;
// Find the most recent generated file
Path outputDir = Paths.get("dreamscape_output");
if (Files.exists(outputDir)) {
// Get most recent file
Path latestFile = Files.list(outputDir)
.filter(p -> p.toString().endsWith(".mp4"))
.max((p1, p2) -> {
try {
return Files.getLastModifiedTime(p1)
.compareTo(Files.getLastModifiedTime(p2));
} catch (Exception e) {
return 0;
}
})
.orElse(null);
if (latestFile != null) {
// Extract first frame as image for display
// For now, just send the video path
std::string imgJson = std::string.format(
"{\"thought_img\": \"%s\"}",
escapeJson(latestFile.toString())
);
signal.println(imgJson);
signal.flush();
std::cout << "   ✅ Visual thought sent to broadcast" << std::endl;
}
}
} catch (Exception e) {
std::cout << "   ⚠️ Visual generation failed: " + e.getMessage() << std::endl;
}
}, "VisualGenerator").start();
} else {
std::cout << "   ⚠️ Visual Cortex not available (text-only mode)" << std::endl;
}
} catch (Exception e) {
std::cout << "❌ BROADCAST ERROR: " + e.getMessage() << std::endl;
e.printStackTrace();
}
}
/**
* SPEAK - Just speak without visual (faster)
*/
public void speak(std::string text) {
if (!broadcasting.get()) {
std::cout << "⚠️ Not broadcasting. Call goLive() first." << std::endl;
return;
}
std::string json = std::string.format("{\"speak\": \"%s\"}", escapeJson(text));
signal.println(json);
signal.flush();
}
/**
* GLITCH - Trigger glitch effect
*/
public void glitch(double intensity) {
if (!broadcasting.get()) return;
std::string json = std::string.format("{\"glitch\": %.2f}", intensity);
signal.println(json);
signal.flush();
}
/**
* SET NEURAL ACTIVITY - Control the AI's "excitement level"
*/
public void setNeuralActivity(double level) {
if (!broadcasting.get()) return;
std::string json = std::string.format("{\"neural\": %.2f}", level);
signal.println(json);
signal.flush();
}
/**
* SHOW THOUGHT - Display a pre-generated image
*/
public void showThought(std::string imagePath) {
if (!broadcasting.get()) return;
std::string json = std::string.format("{\"thought_img\": \"%s\"}", escapeJson(imagePath));
signal.println(json);
signal.flush();
}
/**
* OFF AIR - Stop the broadcast
*/
public void offAir() {
if (!broadcasting.get()) {
return;
}
std::cout <<  << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << "📺 GOING OFF AIR" << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout << getStats() << std::endl;
std::cout << "═══════════════════════════════════════════════════════════════" << std::endl;
std::cout <<  << std::endl;
broadcasting.set(false);
if (signal != null) {
signal.close();
}
if (broadcast != null) {
broadcast.destroy();
}
}
/**
* Get broadcast statistics
*/
public std::string getStats() {
return std::string.format("""
📺 BROADCAST STATISTICS
Total broadcasts: %d
Visual thoughts: %d
Status: %s
""",
totalBroadcasts, totalThoughts,
broadcasting.get() ? "ON AIR" : "OFF AIR");
}
/**
* Check if currently broadcasting
*/
public bool isLive() {
return broadcasting.get();
}
/**
* Escape JSON special characters
*/
private std::string escapeJson(std::string str) {
return str.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
}
