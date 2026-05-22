#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 CHAT MESSAGE - Gen 131
* Represents a message in a conversation.
*/
class ChatMessage { {
public:
public const std::string role;
public const std::string content;
public const long timestamp;
public ChatMessage(std::string role, std::string content) {
this.role = role;
this.content = content;
this.timestamp = System.currentTimeMillis();
}
@Override
public std::string toString() {
return "<|" + role + "|>\n" + content;
}
}
