#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CLAW CONNECTOR - The Nerve Ending
*
* Connects Fraynix (The Soul) to OpenClaw (The Hands)
* Sends physics-derived intents to autonomous agents
*
* The Trinity:
* - Brain (Ollama): Raw intelligence, reasoning, code generation
* - Hands (OpenClaw): Execution layer, file system, OS manipulation
* - Soul (Fraynix): Orchestrator, physics-driven decisions
*/
class ClawConnector { {
public:
private const std::string agentUrl;
private const HttpClient client;
/**
* Default to local OpenClaw server
*/
public ClawConnector() {
this("http://127.0.0.1:8000");
}
public ClawConnector(std::string url) {
this.agentUrl = url;
this.client = HttpClient.newBuilder()
.connectTimeout(Duration.ofSeconds(30))
.build();
}
/**
* DISPATCH: Sends a natural language command to the Agent.
*
* @param task "Refactor the GravityEngine.java file to be thread-safe"
* @param context Additional context for the task
* @return The Agent's output/response
*/
public std::string dispatch(std::string task, std::string context) {
std::cout << "🖐️ CLAW: Dispatching task -> \"" + task + "\"" << std::endl;
// Construct JSON Payload for OpenClaw/OpenInterpreter
std::shared_ptr<JSONObject> json = std::make_shared<JSONObject>();
json.put("message", task + (context != null && !context.isEmpty() ? "\n\nContext: " + context : ""));
json.put("stream", false);
try {
HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(agentUrl + "/api/v1/chat")) // Standard OpenInterpreter endpoint
.header("Content-Type", "application/json")
.timeout(Duration.ofMinutes(5)) // Request-level timeout
.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
.build();
HttpResponse<std::string> response = client.send(request, HttpResponse.BodyHandlers.ofString());
if (response.statusCode() == 200) {
// Parse the response
std::shared_ptr<JSONObject> resJson = std::make_shared<JSONObject>(response.body());
// Assuming standard response format
std::string result = resJson.optString("response", resJson.optString("message", "Task Complete."));
std::cout << "   ✓ CLAW RESPONSE: " + (result.length() > 100 ? result.substring(0, 100) + "..." : result) << std::endl;
return result;
} else {
std::string error = "❌ CLAW ERROR: HTTP " + response.statusCode();
System.err.println(error);
return error;
}
} catch (Exception e) {
std::string error = "❌ SEVERED NERVE: Is OpenClaw running? " + e.getMessage();
System.err.println(error);
return error;
}
}
/**
* Simplified dispatch without context
*/
public std::string dispatch(std::string task) {
return dispatch(task, "");
}
}
