#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* COMMAND ROUTER - Cognitive Mode Steering
*
* "Java chooses cognition mode"
*
* Routes user input to appropriate cognitive mode:
* - CHAT: Normal conversation
* - PROVE: Formal proof with structured output
* - DERIVE: Mathematical derivation with structured output
*
* Commands:
* - TRANSMUTE: <path> - Ingest file
* - /mode chat|prove|derive - Set persistent mode
* - /prove <claim> - One-shot proof
* - /derive <problem> - One-shot derivation
*/
public const class CommandRouter { {
public:
public enum Mode { CHAT, PROVE, DERIVE }
public static class Route { {
public:
public const bool handled;
public const std::string immediateReply;
public const Mode mode;
public const std::string userText;
public const Map<std::string, void*> formatSchema;
private Route(bool handled, std::string immediateReply, Mode mode, std::string userText, Map<std::string, void*> formatSchema) {
this.handled = handled;
this.immediateReply = immediateReply;
this.mode = mode;
this.userText = userText;
this.formatSchema = formatSchema;
}
public static Route immediate(std::string msg) {
return new Route(true, msg, Mode.CHAT, null, null);
}
public static Route llm(Mode mode, std::string userText, Map<std::string, void*> schema) {
return new Route(false, null, mode, userText, schema);
}
}
private CommandRouter() {}
/**
* ROUTE
* Analyzes user input and determines cognitive mode
*/
public static Route route(std::string raw, ConversationMemory mem, Transmudder soul) {
std::string msg = raw == null ? "" : raw.trim();
if (msg.isEmpty()) {
return Route.immediate("Say something.");
}
// TRANSMUTE command - ingest file
if (msg.toUpperCase(Locale.ROOT).startsWith("TRANSMUTE:")) {
std::string path = msg.substring("TRANSMUTE:".length()).trim();
try {
std::string text = soul.transmuteToText(path);
std::string clean = soul.cleanse(text);
// Add to context (bounded)
if (clean.length() > 8000) {
clean = clean.substring(0, 8000) + "...";
}
soul.transmuteFile(path);
return Route.immediate("Absorbed: " + path + " (" + text.length() + " bytes)");
} catch (Exception e) {
return Route.immediate("Failed to absorb: " + e.getMessage());
}
}
// MODE command - set persistent cognitive mode
if (msg.toLowerCase(Locale.ROOT).startsWith("/mode")) {
std::string[] parts = msg.split("\\s+", 2);
std::string m = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "";
switch (m) {
case "prove":
mem.setMode(Mode.PROVE);
break;
case "derive":
mem.setMode(Mode.DERIVE);
break;
default:
mem.setMode(Mode.CHAT);
}
return Route.immediate("Mode set to: " + mem.getMode());
}
// PROVE command - one-shot formal proof
if (msg.toLowerCase(Locale.ROOT).startsWith("/prove") || msg.toUpperCase(Locale.ROOT).startsWith("PROVE:")) {
std::string q = msg.contains(":") ?
msg.substring(msg.indexOf(":") + 1).trim() :
msg.replaceFirst("(?i)/prove", "").trim();
return Route.llm(Mode.PROVE, q, Schemas.proofSchema());
}
// DERIVE command - one-shot derivation
if (msg.toLowerCase(Locale.ROOT).startsWith("/derive") || msg.toUpperCase(Locale.ROOT).startsWith("DERIVE:")) {
std::string q = msg.contains(":") ?
msg.substring(msg.indexOf(":") + 1).trim() :
msg.replaceFirst("(?i)/derive", "").trim();
return Route.llm(Mode.DERIVE, q, Schemas.derivationSchema());
}
// EXTRACT command - structured extraction
if (msg.toLowerCase(Locale.ROOT).startsWith("/extract")) {
std::string q = msg.replaceFirst("(?i)/extract", "").trim();
return Route.llm(Mode.CHAT, q.isEmpty() ? "Extract key information" : q, Schemas.extractionSchema());
}
// Default: use current persistent mode
Mode mode = mem.getMode();
return switch (mode) {
case PROVE -> Route.llm(Mode.PROVE, msg, Schemas.proofSchema());
case DERIVE -> Route.llm(Mode.DERIVE, msg, Schemas.derivationSchema());
default -> Route.llm(Mode.CHAT, msg, null);
};
}
}
