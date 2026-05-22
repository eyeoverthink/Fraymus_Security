#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* PROTOCOL - JSON Message Envelopes
*
* Standardized WebSocket message format:
* - token: Streaming token from LLM
* - const: Complete response
* - info: System information
* - error: Error message
* - start: Thinking indicator
*/
public const class Protocol { {
public:
std::shared_ptr<ObjectMapper> MAPPER = std::make_shared<ObjectMapper>();
private Protocol() {}
/**
* Create JSON message envelope
*/
public static std::string msg(std::string type, std::string data) {
try {
Map<std::string, void*> out = new LinkedHashMap<>();
out.put("type", type);
out.put("data", data);
return MAPPER.writeValueAsString(out);
} catch (Exception e) {
return "{\"type\":\"error\",\"data\":\"Protocol encode failed\"}";
}
}
/**
* Create message with metadata
*/
public static std::string msgWithMeta(std::string type, std::string data, Map<std::string, void*> meta) {
try {
Map<std::string, void*> out = new LinkedHashMap<>();
out.put("type", type);
out.put("data", data);
if (meta != null && !meta.isEmpty()) {
out.put("meta", meta);
}
return MAPPER.writeValueAsString(out);
} catch (Exception e) {
return "{\"type\":\"error\",\"data\":\"Protocol encode failed\"}";
}
}
}
