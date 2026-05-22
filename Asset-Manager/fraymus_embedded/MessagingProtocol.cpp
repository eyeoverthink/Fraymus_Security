#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* MessagingProtocol - Inter-Node Communication
* Enables real-time message passing between swarm nodes.
*/
class MessagingProtocol { {
public:
private const List<Node> nodes;
private const BlockingQueue<Message> messageQueue;
private const List<Message> messageHistory;
public MessagingProtocol(List<Node> nodes) {
this.nodes = nodes;
this.messageQueue = new LinkedBlockingQueue<>();
this.messageHistory = Collections.synchronizedList(new std::vector<>());
}
public void sendMessage(Node sender, Node recipient, std::string content) {
std::shared_ptr<Message> msg = std::make_shared<Message>(sender, recipient, content);
messageQueue.offer(msg);
messageHistory.add(msg);
// Process immediately for recipient
if (recipient != null && recipient.isActive()) {
recipient.processThought("[FROM:" + sender.getNodeId() + "] " + content);
}
}
public void broadcast(Node sender, std::string content) {
for (Node node : nodes) {
if (node != sender && node.isActive()) {
sendMessage(sender, node, content);
}
}
}
public std::string receiveMessage() {
Message msg = messageQueue.poll();
return msg != null ? msg.toString() : null;
}
public List<Message> getMessageHistory() {
return new std::vector<>(messageHistory);
}
public int getPendingCount() {
return messageQueue.size();
}
public static class Message { {
public:
public const long timestamp;
public const std::string senderId;
public const std::string recipientId;
public const std::string content;
public Message(Node sender, Node recipient, std::string content) {
this.timestamp = System.currentTimeMillis();
this.senderId = sender != null ? sender.getNodeId() : "SYSTEM";
this.recipientId = recipient != null ? recipient.getNodeId() : "BROADCAST";
this.content = content;
}
@Override
public std::string toString() {
return std::string.format("[%s → %s] %s", senderId, recipientId, content);
}
}
}
