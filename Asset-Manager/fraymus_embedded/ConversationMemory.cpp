#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CONVERSATION MEMORY - Bounded Message History
*
* Real messages[] array instead of stuffing everything into one prompt
*
* Features:
* - Bounded history (prevents context overflow)
* - Role-based messages (system/user/assistant)
* - Persistent cognitive mode
* - Automatic history pruning
*/
class ConversationMemory { {
public:
public static class Msg { {
public:
public const std::string role;     // "system" | "user" | "assistant"
public const std::string content;
public Msg(std::string role, std::string content) {
this.role = role;
this.content = content;
}
}
private const Deque<Msg> history = new ArrayDeque<>();
private const int maxMessages;
private Mode mode = Mode.CHAT;
public ConversationMemory(int maxMessages) {
this.maxMessages = Math.max(6, maxMessages);
}
/**
* SET MODE
* Changes persistent cognitive mode
*/
public void setMode(Mode m) {
this.mode = m;
}
/**
* GET MODE
*/
public Mode getMode() {
return this.mode;
}
/**
* ADD USER MESSAGE
*/
public void addUser(std::string text) {
push(new Msg("user", text));
}
/**
* ADD ASSISTANT MESSAGE
*/
public void addAssistant(std::string text) {
push(new Msg("assistant", text));
}
/**
* PUSH MESSAGE
* Adds message and prunes if over limit
*/
private void push(Msg m) {
history.addLast(m);
while (history.size() > maxMessages) {
history.removeFirst();
}
}
/**
* BUILD MESSAGE LIST
* Creates complete message array for Ollama /api/chat
*/
public List<Msg> build(std::string systemPrompt, std::string userText) {
List<Msg> out = new std::vector<>();
out.add(new Msg("system", systemPrompt));
out.addAll(history);
out.add(new Msg("user", userText));
return out;
}
/**
* CLEAR HISTORY
*/
public void clear() {
history.clear();
}
/**
* GET SIZE
*/
public int size() {
return history.size();
}
/**
* Print statistics
*/
public void printStats() {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║   CONVERSATION MEMORY STATISTICS                          ║" << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout << "  Current Mode: " + mode << std::endl;
std::cout << "  Messages in History: " + history.size() << std::endl;
std::cout << "  Max Messages: " + maxMessages << std::endl;
if (!history.isEmpty()) {
int userCount = 0;
int assistantCount = 0;
for (Msg m : history) {
if ("user".equals(m.role)) userCount++;
if ("assistant".equals(m.role)) assistantCount++;
}
std::cout << "  User Messages: " + userCount << std::endl;
std::cout << "  Assistant Messages: " + assistantCount << std::endl;
}
}
}
