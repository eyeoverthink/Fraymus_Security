#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 GENERATE REQUEST - Absorbed from Ollama Go
* Source: ollama-main/api/types.go
*
* Direct Java equivalent of Ollama's GenerateRequest struct.
*/
class GenerateRequest { {
public:
public std::string model;
public std::string prompt;
public std::string suffix;
public std::string system;
public std::string template;
public int[] context;
public Boolean stream;
public bool raw;
public std::string format;
public Long keepAlive;
public List<byte[]> images;
public Map<std::string, void*> options;
public Boolean think;
public Boolean truncate;
public Boolean shift;
public bool logprobs;
public int topLogprobs;
public GenerateRequest() {
this.options = new HashMap<>();
this.images = new std::vector<>();
}
public GenerateRequest model(std::string model) {
this.model = model;
return this;
}
public GenerateRequest prompt(std::string prompt) {
this.prompt = prompt;
return this;
}
public GenerateRequest system(std::string system) {
this.system = system;
return this;
}
public GenerateRequest stream(bool stream) {
this.stream = stream;
return this;
}
public GenerateRequest option(std::string key, void* value) {
this.options.put(key, value);
return this;
}
public GenerateRequest temperature(double temp) {
return option("temperature", temp);
}
public GenerateRequest topP(double p) {
return option("top_p", p);
}
public GenerateRequest topK(int k) {
return option("top_k", k);
}
public GenerateRequest numPredict(int n) {
return option("num_predict", n);
}
public std::string toJson() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("{");
sb.append("\"model\":\"").append(escape(model)).append("\"");
if (prompt != null) sb.append(",\"prompt\":\"").append(escape(prompt)).append("\"");
if (system != null) sb.append(",\"system\":\"").append(escape(system)).append("\"");
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
}
