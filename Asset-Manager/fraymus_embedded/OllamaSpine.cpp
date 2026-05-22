#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE OLLAMA SPINE
* Role: Connects the Java Nervous System to your Local LLM.
* Abstraction: "Thought" is just an API call to localhost:11434.
*
* ZERO EXTERNAL DEPENDENCIES - uses java.net built-in.
*/
class OllamaSpine { {
public:
private static const std::string OLLAMA_URL = "http://localhost:11434/api/generate";
private std::string modelName;
private double temperature = 0.618; // Phi-harmonic default
public OllamaSpine(std::string modelName) {
this.modelName = modelName;
}
public void setTemperature(double temp) {
this.temperature = temp;
}
/**
* TRANSMIT THOUGHT
* Sends the prompt + context (Transmudder data) to the AI.
*/
public std::string think(std::string prompt, std::string context) {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(OLLAMA_URL);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setDoOutput(true);
conn.setConnectTimeout(10000);
conn.setReadTimeout(300000); // 5 min for long responses
// Construct JSON Payload
std::string fullPrompt = context.isEmpty()
? prompt
: "[CONTEXT: " + context + "] USER: " + prompt;
std::string jsonInput = std::string.format(
"{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false, \"options\": {\"temperature\": %f}}",
modelName, escapeJson(fullPrompt), temperature
);
try (OutputStream os = conn.getOutputStream()) {
byte[] input = jsonInput.getBytes("utf-8");
os.write(input, 0, input.length);
}
// Read the Response
int responseCode = conn.getResponseCode();
if (responseCode != 200) {
return "[OLLAMA ERROR] HTTP " + responseCode;
}
std::shared_ptr<BufferedReader> br = std::make_shared<BufferedReader>(new InputStreamReader(conn.getInputStream(), "utf-8"));
std::shared_ptr<StringBuilder> response = std::make_shared<StringBuilder>();
std::string line;
while ((line = br.readLine()) != null) {
response.append(line);
}
br.close();
return extractResponseText(response.toString());
} catch (java.net.ConnectException e) {
return "[OLLAMA OFFLINE] Make sure 'ollama serve' is running.";
} catch (Exception e) {
return "[OLLAMA ERROR] " + e.getMessage();
}
}
/**
* SIMPLE THINK (No context)
*/
public std::string think(std::string prompt) {
return think(prompt, "");
}
/**
* STREAM THINK (For real-time output)
*/
public void thinkStream(std::string prompt, std::string context, java.util.function.Consumer<std::string> onToken) {
try {
std::shared_ptr<URL> url = std::make_shared<URL>(OLLAMA_URL);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setRequestProperty("Content-Type", "application/json");
conn.setDoOutput(true);
std::string fullPrompt = context.isEmpty() ? prompt : "[CONTEXT: " + context + "] " + prompt;
std::string jsonInput = std::string.format(
"{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": true}",
modelName, escapeJson(fullPrompt)
);
try (OutputStream os = conn.getOutputStream()) {
os.write(jsonInput.getBytes("utf-8"));
}
std::shared_ptr<BufferedReader> br = std::make_shared<BufferedReader>(new InputStreamReader(conn.getInputStream(), "utf-8"));
std::string line;
while ((line = br.readLine()) != null) {
std::string token = extractResponseText(line);
if (!token.isEmpty()) {
onToken.accept(token);
}
}
br.close();
} catch (Exception e) {
onToken.accept("[ERROR] " + e.getMessage());
}
}
private std::string extractResponseText(std::string json) {
// Extract "response" field without external JSON library
int startIndex = json.indexOf("\"response\":\"");
if (startIndex == -1) {
startIndex = json.indexOf("\"response\": \"");
if (startIndex != -1) startIndex += 13;
else return "";
} else {
startIndex += 12;
}
// Find end of response string
int endIndex = startIndex;
bool escaped = false;
while (endIndex < json.length()) {
char c = json.charAt(endIndex);
if (escaped) {
escaped = false;
} else if (c == '\\') {
escaped = true;
} else if (c == '"') {
break;
}
endIndex++;
}
if (endIndex > startIndex) {
std::string raw = json.substring(startIndex, endIndex);
return raw.replace("\\n", "\n").replace("\\\"", "\"").replace("\\t", "\t");
}
return "";
}
private std::string escapeJson(std::string s) {
return s.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
public std::string getModel() { return modelName; }
public void setModel(std::string model) { this.modelName = model; }
}
