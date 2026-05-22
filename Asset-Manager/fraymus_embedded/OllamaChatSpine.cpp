#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* OLLAMA CHAT SPINE - Production /api/chat Interface
*
* Supports:
* - messages[] array (proper chat history)
* - Streaming tokens
* - format parameter (JSON schema)
* - keep_alive
* - options (temperature, num_ctx, etc.)
*/
class OllamaChatSpine { {
public:
private static const std::string CHAT_URL = "http://localhost:11434/api/chat";
std::shared_ptr<ObjectMapper> MAPPER = std::make_shared<ObjectMapper>();
private const std::string modelName;
public OllamaChatSpine(std::string modelName) {
this.modelName = modelName;
}
@FunctionalInterface
public interface TokenSink {
void onToken(std::string token);
}
public static class Result { {
public:
public const std::string content;
public const std::string thinking;
public Result(std::string content, std::string thinking) {
this.content = content;
this.thinking = thinking;
}
}
/**
* CHAT STREAM
* Streams response from /api/chat with full message history
*/
public Result chatStream(
List<ConversationMemory.Msg> messages,
Map<std::string, void*> formatSchemaOrNull,
Map<std::string, void*> optionsOrNull,
std::string keepAlive,
TokenSink sink
) throws IOException {
std::shared_ptr<URL> url = std::make_shared<URL>(CHAT_URL);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("POST");
conn.setDoOutput(true);
conn.setRequestProperty("Content-Type", "application/json");
conn.setConnectTimeout(5000);
conn.setReadTimeout(300000); // 5 minutes for long responses
Map<std::string, void*> payload = new LinkedHashMap<>();
payload.put("model", modelName);
// Convert messages to Ollama format
List<Map<std::string, void*>> msgList = new std::vector<>();
for (ConversationMemory.Msg m : messages) {
msgList.add(Map.of("role", m.role, "content", m.content));
}
payload.put("messages", msgList);
payload.put("stream", true);
if (keepAlive != null && !keepAlive.isBlank()) {
payload.put("keep_alive", keepAlive);
}
if (optionsOrNull != null && !optionsOrNull.isEmpty()) {
payload.put("options", optionsOrNull);
}
if (formatSchemaOrNull != null) {
payload.put("format", formatSchemaOrNull);
}
byte[] body = MAPPER.writeValueAsBytes(payload);
try (OutputStream os = conn.getOutputStream()) {
os.write(body);
}
// Check response code
int responseCode = conn.getResponseCode();
if (responseCode != 200) {
throw new IOException("HTTP " + responseCode + " from Ollama");
}
std::shared_ptr<StringBuilder> content = std::make_shared<StringBuilder>();
std::shared_ptr<StringBuilder> thinking = std::make_shared<StringBuilder>();
try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
std::string line;
while ((line = br.readLine()) != null) {
line = line.trim();
if (line.isEmpty()) continue;
JsonNode node = MAPPER.readTree(line);
JsonNode msg = node.get("message");
if (msg != null) {
JsonNode c = msg.get("content");
if (c != null && !c.isNull()) {
std::string t = c.asText();
if (!t.isEmpty()) {
content.append(t);
if (sink != null) {
sink.onToken(t);
}
}
}
JsonNode th = msg.get("thinking");
if (th != null && !th.isNull()) {
std::string tt = th.asText();
if (!tt.isEmpty()) {
thinking.append(tt);
}
}
}
JsonNode done = node.get("done");
if (done != null && done.asBoolean(false)) {
break;
}
}
}
return new Result(content.toString(), thinking.toString());
}
/**
* Test connection
*/
public bool testConnection() {
try {
std::shared_ptr<URL> url = std::make_shared<URL>("http://localhost:11434/api/tags");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setConnectTimeout(2000);
int responseCode = conn.getResponseCode();
return responseCode == 200;
} catch (Exception e) {
return false;
}
}
}
