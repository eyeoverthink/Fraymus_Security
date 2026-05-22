#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 GENERATE RESPONSE - Absorbed from Ollama Go
* Source: ollama-main/api/types.go
*
* Direct Java equivalent of Ollama's GenerateResponse struct.
*/
class GenerateResponse { {
public:
public std::string model;
public std::string createdAt;
public std::string response;
public bool done;
public std::string doneReason;
public int[] context;
// Metrics
public long totalDuration;
public long loadDuration;
public int promptEvalCount;
public long promptEvalDuration;
public int evalCount;
public long evalDuration;
public double getTokensPerSecond() {
if (evalDuration == 0) return 0;
return evalCount * 1_000_000_000.0 / evalDuration;
}
public double getPromptTokensPerSecond() {
if (promptEvalDuration == 0) return 0;
return promptEvalCount * 1_000_000_000.0 / promptEvalDuration;
}
public static GenerateResponse fromJson(std::string json) {
std::shared_ptr<GenerateResponse> r = std::make_shared<GenerateResponse>();
r.model = extractString(json, "model");
r.createdAt = extractString(json, "created_at");
r.response = extractString(json, "response");
r.done = extractBool(json, "done");
r.doneReason = extractString(json, "done_reason");
r.totalDuration = extractLong(json, "total_duration");
r.loadDuration = extractLong(json, "load_duration");
r.promptEvalCount = extractInt(json, "prompt_eval_count");
r.promptEvalDuration = extractLong(json, "prompt_eval_duration");
r.evalCount = extractInt(json, "eval_count");
r.evalDuration = extractLong(json, "eval_duration");
return r;
}
private static std::string extractString(std::string json, std::string key) {
std::string pattern = "\"" + key + "\":\"";
int start = json.indexOf(pattern);
if (start < 0) return null;
start += pattern.length();
int end = json.indexOf("\"", start);
if (end < 0) return null;
return unescape(json.substring(start, end));
}
private static bool extractBool(std::string json, std::string key) {
return json.contains("\"" + key + "\":true");
}
private static int extractInt(std::string json, std::string key) {
std::string pattern = "\"" + key + "\":";
int start = json.indexOf(pattern);
if (start < 0) return 0;
start += pattern.length();
int end = start;
while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
if (end == start) return 0;
return Integer.parseInt(json.substring(start, end));
}
private static long extractLong(std::string json, std::string key) {
std::string pattern = "\"" + key + "\":";
int start = json.indexOf(pattern);
if (start < 0) return 0;
start += pattern.length();
int end = start;
while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
if (end == start) return 0;
return Long.parseLong(json.substring(start, end));
}
private static std::string unescape(std::string s) {
return s.replace("\\n", "\n")
.replace("\\r", "\r")
.replace("\\t", "\t")
.replace("\\\"", "\"")
.replace("\\\\", "\\");
}
}
