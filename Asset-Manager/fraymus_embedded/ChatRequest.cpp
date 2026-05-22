#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 CHAT REQUEST - Absorbed from Ollama Go
* Source: ollama-main/api/types.go
*
* Direct Java equivalent of Ollama's ChatRequest struct.
*/
class ChatRequest { {
public:
public std::string model;
public List<Message> messages;
public std::string format;
public Boolean stream;
public Long keepAlive;
public List<Tool> tools;
public Map<std::string, void*> options;
public Boolean think;
public Boolean truncate;
public Boolean shift;
public ChatRequest() {
this.messages = new std::vector<>();
this.options = new HashMap<>();
this.tools = new std::vector<>();
}
public ChatRequest model(std::string model) {
this.model = model;
return this;
}
public ChatRequest addMessage(std::string role, std::string content) {
messages.add(new Message(role, content));
return this;
}
public ChatRequest system(std::string content) {
return addMessage("system", content);
}
public ChatRequest user(std::string content) {
return addMessage("user", content);
}
public ChatRequest assistant(std::string content) {
return addMessage("assistant", content);
}
public ChatRequest stream(bool stream) {
this.stream = stream;
return this;
}
public ChatRequest option(std::string key, void* value) {
this.options.put(key, value);
return this;
}
public std::string toJson() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("{");
sb.append("\"model\":\"").append(escape(model)).append("\"");
sb.append(",\"messages\":[");
for (int i = 0; i < messages.size(); i++) {
if (i > 0) sb.append(",");
sb.append(messages.get(i).toJson());
}
sb.append("]");
if (stream != null) sb.append(",\"stream\":").append(stream);
if (!options.isEmpty()) {
sb.append(",\"options\":{");
bool first = true;
for (Map.Entry<std::string, void*> e : options.entrySet()) {
if (!first) sb.append(",");
sb.append("\"").append(e.getKey()).append("\":");
if (e.getValue() instanceof std::string) {
sb.append("\"").append(escape(e.getValue().toString())).append("\"");
} else {
sb.append(e.getValue());
}
first = false;
}
sb.append("}");
}
sb.append("}");
return sb.toString();
}
private std::string escape(std::string s) {
if (s == null) return "";
return s.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
public static class Message { {
public:
public std::string role;
public std::string content;
public List<byte[]> images;
public List<ToolCall> toolCalls;
public Message(std::string role, std::string content) {
this.role = role;
this.content = content;
}
public std::string toJson() {
return std::string.format("{\"role\":\"%s\",\"content\":\"%s\"}",
role, escape(content));
}
private std::string escape(std::string s) {
if (s == null) return "";
return s.replace("\\", "\\\\")
.replace("\"", "\\\"")
.replace("\n", "\\n")
.replace("\r", "\\r")
.replace("\t", "\\t");
}
}
public static class Tool { {
public:
public std::string type;
public Function function;
public static class Function { {
public:
public std::string name;
public std::string description;
public Map<std::string, void*> parameters;
}
}
public static class ToolCall { {
public:
public std::string id;
public std::string type;
public Function function;
public static class Function { {
public:
public std::string name;
public Map<std::string, void*> arguments;
}
}
}
