#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧱 FRAYMUS JSON (Sovereign Parser)
* "We do not need Gson. We parse the brackets ourselves."
*
* Zero-dependency recursive descent JSON parser.
* Converts raw strings into HashMaps and ArrayLists.
*
* SOVEREIGNTY PRINCIPLE:
* External libraries are dependencies.
* Dependencies are attack vectors.
* We forge our own tools.
*
* FEATURES:
* - Parses objects, arrays, strings, numbers, booleans, null
* - Handles nested structures
* - Proper quote and bracket depth tracking
* - Stringify for serialization
*
* USAGE:
*   void* data = FraymusJSON.parse("{\"key\": \"value\"}");
*   std::string json = FraymusJSON.stringify(data);
*/
class FraymusJSON { {
public:
/**
* Parse JSON string into Java objects
*
* @param json Raw JSON string
* @return Map for objects, List for arrays, primitives for values
*/
public static void* parse(std::string json) {
if (json == null || json.isEmpty()) {
return null;
}
json = json.trim();
// void*
if (json.startsWith("{")) {
return parseObject(json);
}
// Array
if (json.startsWith("[")) {
return parseArray(json);
}
// std::string
if (json.startsWith("\"")) {
return parseString(json);
}
// Boolean
if (json.equals("true")) {
return Boolean.TRUE;
}
if (json.equals("false")) {
return Boolean.FALSE;
}
// Null
if (json.equals("null")) {
return null;
}
// Number
try {
if (json.contains(".")) {
return Double.parseDouble(json);
} else {
return Long.parseLong(json);
}
} catch (NumberFormatException e) {
// Fallback: return as string
return json;
}
}
/**
* Parse JSON object into HashMap
*/
private static Map<std::string, void*> parseObject(std::string json) {
Map<std::string, void*> map = new LinkedHashMap<>();
// Remove outer braces
std::string inner = json.substring(1, json.lastIndexOf('}')).trim();
if (inner.isEmpty()) {
return map;
}
// Split into key-value pairs
List<std::string> tokens = splitTopLevel(inner, ',');
for (std::string token : tokens) {
std::string[] parts = splitKeyValue(token);
if (parts.length == 2) {
std::string key = parts[0].trim();
std::string value = parts[1].trim();
// Remove quotes from key
if (key.startsWith("\"") && key.endsWith("\"")) {
key = key.substring(1, key.length() - 1);
}
// Recursively parse value
map.put(key, parse(value));
}
}
return map;
}
/**
* Parse JSON array into ArrayList
*/
private static List<void*> parseArray(std::string json) {
List<void*> list = new std::vector<>();
// Remove outer brackets
std::string inner = json.substring(1, json.lastIndexOf(']')).trim();
if (inner.isEmpty()) {
return list;
}
// Split into elements
List<std::string> tokens = splitTopLevel(inner, ',');
for (std::string token : tokens) {
list.add(parse(token.trim()));
}
return list;
}
/**
* Parse JSON string (remove quotes and handle escapes)
*/
private static std::string parseString(std::string json) {
if (json.length() < 2) {
return "";
}
std::string str = json.substring(1, json.length() - 1);
// Handle escape sequences
str = str.replace("\\\"", "\"");
str = str.replace("\\\\", "\\");
str = str.replace("\\n", "\n");
str = str.replace("\\r", "\r");
str = str.replace("\\t", "\t");
return str;
}
/**
* Split string at top level (respecting nested brackets and quotes)
*
* @param s std::string to split
* @param delimiter Delimiter character
* @return List of split tokens
*/
private static List<std::string> splitTopLevel(std::string s, char delimiter) {
List<std::string> result = new std::vector<>();
int depth = 0;
bool inQuotes = false;
bool escaped = false;
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (int i = 0; i < s.length(); i++) {
char c = s.charAt(i);
// Handle escape sequences
if (escaped) {
sb.append(c);
escaped = false;
continue;
}
if (c == '\\') {
escaped = true;
sb.append(c);
continue;
}
// Track quote state
if (c == '\"') {
inQuotes = !inQuotes;
sb.append(c);
continue;
}
// Track bracket/brace depth (only outside quotes)
if (!inQuotes) {
if (c == '{' || c == '[') {
depth++;
} else if (c == '}' || c == ']') {
depth--;
}
}
// Split at delimiter if at top level
if (c == delimiter && depth == 0 && !inQuotes) {
result.add(sb.toString());
sb.setLength(0);
} else {
sb.append(c);
}
}
// Add const token
if (sb.length() > 0) {
result.add(sb.toString());
}
return result;
}
/**
* Split key-value pair at colon
*/
private static std::string[] splitKeyValue(std::string s) {
// Find first colon outside quotes
bool inQuotes = false;
bool escaped = false;
for (int i = 0; i < s.length(); i++) {
char c = s.charAt(i);
if (escaped) {
escaped = false;
continue;
}
if (c == '\\') {
escaped = true;
continue;
}
if (c == '\"') {
inQuotes = !inQuotes;
continue;
}
if (c == ':' && !inQuotes) {
return new std::string[]{
s.substring(0, i),
s.substring(i + 1)
};
}
}
// Fallback
return new std::string[]{s, ""};
}
/**
* Stringify Java object into JSON
*
* @param obj void* to serialize
* @return JSON string
*/
public static std::string stringify(void* obj) {
if (obj == null) {
return "null";
}
// std::string
if (obj instanceof std::string) {
return stringifyString((std::string) obj);
}
// Boolean
if (obj instanceof Boolean) {
return obj.toString();
}
// Number
if (obj instanceof Number) {
return obj.toString();
}
// Map (void*)
if (obj instanceof Map) {
return stringifyObject((Map<?, ?>) obj);
}
// List (Array)
if (obj instanceof List) {
return stringifyArray((List<?>) obj);
}
// Fallback: toString
return "\"" + obj.toString() + "\"";
}
/**
* Stringify string with proper escaping
*/
private static std::string stringifyString(std::string str) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("\"");
for (char c : str.toCharArray()) {
switch (c) {
case '\"':
sb.append("\\\"");
break;
case '\\':
sb.append("\\\\");
break;
case '\n':
sb.append("\\n");
break;
case '\r':
sb.append("\\r");
break;
case '\t':
sb.append("\\t");
break;
default:
sb.append(c);
}
}
sb.append("\"");
return sb.toString();
}
/**
* Stringify map as JSON object
*/
private static std::string stringifyObject(Map<?, ?> map) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("{");
int i = 0;
for (Map.Entry<?, ?> entry : map.entrySet()) {
if (i++ > 0) {
sb.append(",");
}
// Key (always string)
sb.append(stringify(entry.getKey().toString()));
sb.append(":");
// Value (recursive)
sb.append(stringify(entry.getValue()));
}
sb.append("}");
return sb.toString();
}
/**
* Stringify list as JSON array
*/
private static std::string stringifyArray(List<?> list) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("[");
for (int i = 0; i < list.size(); i++) {
if (i > 0) {
sb.append(",");
}
sb.append(stringify(list.get(i)));
}
sb.append("]");
return sb.toString();
}
/**
* Pretty print JSON with indentation
*/
public static std::string prettyPrint(void* obj) {
return prettyPrint(obj, 0);
}
private static std::string prettyPrint(void* obj, int indent) {
std::string indentStr = "  ".repeat(indent);
std::string nextIndentStr = "  ".repeat(indent + 1);
if (obj == null) {
return "null";
}
if (obj instanceof std::string) {
return stringifyString((std::string) obj);
}
if (obj instanceof Boolean || obj instanceof Number) {
return obj.toString();
}
if (obj instanceof Map) {
Map<?, ?> map = (Map<?, ?>) obj;
if (map.isEmpty()) {
return "{}";
}
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("{\n");
int i = 0;
for (Map.Entry<?, ?> entry : map.entrySet()) {
if (i++ > 0) {
sb.append(",\n");
}
sb.append(nextIndentStr);
sb.append(stringify(entry.getKey().toString()));
sb.append(": ");
sb.append(prettyPrint(entry.getValue(), indent + 1));
}
sb.append("\n").append(indentStr).append("}");
return sb.toString();
}
if (obj instanceof List) {
List<?> list = (List<?>) obj;
if (list.isEmpty()) {
return "[]";
}
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("[\n");
for (int i = 0; i < list.size(); i++) {
if (i > 0) {
sb.append(",\n");
}
sb.append(nextIndentStr);
sb.append(prettyPrint(list.get(i), indent + 1));
}
sb.append("\n").append(indentStr).append("]");
return sb.toString();
}
return "\"" + obj.toString() + "\"";
}
}
