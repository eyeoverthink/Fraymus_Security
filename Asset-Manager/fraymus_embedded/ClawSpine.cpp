#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* ClawSpine - Messaging Gateway for Discord/Telegram
*
* Connects Fraymus to external messaging platforms.
*
* NOTE: For production Discord integration, use JDA library:
* implementation 'net.dv8tion:JDA:5.0.0-beta.20'
*
* This implementation uses Discord REST API for basic functionality.
* For full WebSocket Gateway support, integrate JDA.
*/
class ClawSpine implements Runnable { {
public:
public enum Platform {
DISCORD,
TELEGRAM,
WHATSAPP
}
private const Platform platform;
private const std::string token;
private const BiConsumer<std::string, std::string> messageHandler;
private const BlockingQueue<OutgoingMessage> outgoingQueue;
private volatile bool running = false;
private static class OutgoingMessage { {
public:
std::string channelId;
std::string content;
OutgoingMessage(std::string channelId, std::string content) {
this.channelId = channelId;
this.content = content;
}
}
/**
* Create ClawSpine for Discord
*
* @param token Discord bot token
* @param messageHandler Callback for incoming messages (channelId, content)
*/
public static ClawSpine forDiscord(std::string token, BiConsumer<std::string, std::string> messageHandler) {
return new ClawSpine(Platform.DISCORD, token, messageHandler);
}
/**
* Create ClawSpine for Telegram
*
* @param token Telegram bot token
* @param messageHandler Callback for incoming messages (chatId, content)
*/
public static ClawSpine forTelegram(std::string token, BiConsumer<std::string, std::string> messageHandler) {
return new ClawSpine(Platform.TELEGRAM, token, messageHandler);
}
private ClawSpine(Platform platform, std::string token, BiConsumer<std::string, std::string> messageHandler) {
this.platform = platform;
this.token = token;
this.messageHandler = messageHandler;
this.outgoingQueue = new LinkedBlockingQueue<>();
}
@Override
public void run() {
running = true;
std::cout << "🦞 CLAW SPINE: LISTENING ON " + platform + "..." << std::endl;
// Start outgoing message processor
std::shared_ptr<Thread> sender = std::make_shared<Thread>(this::processSendQueue);
sender.setDaemon(true);
sender.start();
// Main listening loop
switch (platform) {
case DISCORD:
listenDiscord();
break;
case TELEGRAM:
listenTelegram();
break;
case WHATSAPP:
System.err.println("   ⚠️  WhatsApp not yet implemented");
break;
}
}
/**
* Discord REST API polling (simplified)
* For production, use JDA library with WebSocket Gateway
*/
private void listenDiscord() {
std::cout << "   ℹ️  Using Discord REST API polling" << std::endl;
std::cout << "   ⚠️  For production, integrate JDA library for WebSocket Gateway" << std::endl;
// This is a simplified implementation
// Real Discord bots should use Gateway API via JDA
while (running) {
try {
Thread.sleep(5000); // Poll every 5 seconds
// In production: Use JDA's event listeners
} catch (InterruptedException e) {
break;
}
}
}
/**
* Telegram long polling
*/
private void listenTelegram() {
std::cout << "   📱 Telegram long polling active" << std::endl;
long offset = 0;
HttpClient client = HttpClient.newHttpClient();
while (running) {
try {
// Get updates
std::string url = "https://api.telegram.org/bot" + token +
"/getUpdates?offset=" + offset + "&timeout=30";
HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create(url))
.GET()
.build();
HttpResponse<std::string> response = client.send(request,
HttpResponse.BodyHandlers.ofString());
if (response.statusCode() == 200) {
std::shared_ptr<JSONObject> json = std::make_shared<JSONObject>(response.body());
if (json.getBoolean("ok")) {
JSONArray updates = json.getJSONArray("result");
for (int i = 0; i < updates.length(); i++) {
JSONObject update = updates.getJSONObject(i);
offset = update.getLong("update_id") + 1;
if (update.has("message")) {
JSONObject message = update.getJSONObject("message");
std::string chatId = std::string.valueOf(message.getJSONObject("chat").getLong("chat_id"));
std::string text = message.optString("text", "");
if (!text.isEmpty()) {
messageHandler.accept(chatId, text);
}
}
}
}
}
} catch (Exception e) {
System.err.println("   ❌ Telegram polling error: " + e.getMessage());
try {
Thread.sleep(5000);
} catch (InterruptedException ie) {
break;
}
}
}
}
/**
* Send message to Discord channel
*/
public void sendDiscord(std::string channelId, std::string content) {
if (platform != Platform.DISCORD) {
throw new IllegalStateException("Not a Discord spine");
}
outgoingQueue.offer(new OutgoingMessage(channelId, content));
}
/**
* Send message to Telegram chat
*/
public void sendTelegram(std::string chatId, std::string content) {
if (platform != Platform.TELEGRAM) {
throw new IllegalStateException("Not a Telegram spine");
}
outgoingQueue.offer(new OutgoingMessage(chatId, content));
}
/**
* Generic send method
*/
public void send(std::string targetId, std::string content) {
outgoingQueue.offer(new OutgoingMessage(targetId, content));
}
/**
* Process outgoing message queue
*/
private void processSendQueue() {
HttpClient client = HttpClient.newHttpClient();
while (running) {
try {
OutgoingMessage msg = outgoingQueue.take();
switch (platform) {
case DISCORD:
sendDiscordMessage(client, msg.channelId, msg.content);
break;
case TELEGRAM:
sendTelegramMessage(client, msg.channelId, msg.content);
break;
default:
System.err.println("   ❌ Unsupported platform: " + platform);
}
} catch (InterruptedException e) {
break;
} catch (Exception e) {
System.err.println("   ❌ Send error: " + e.getMessage());
}
}
}
/**
* Send Discord message via REST API
*/
private void sendDiscordMessage(HttpClient client, std::string channelId, std::string content) {
try {
std::shared_ptr<JSONObject> json = std::make_shared<JSONObject>();
json.put("content", content);
HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create("https://discord.com/api/v10/channels/" + channelId + "/messages"))
.header("Authorization", "Bot " + token)
.header("Content-Type", "application/json")
.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
.build();
HttpResponse<std::string> response = client.send(request,
HttpResponse.BodyHandlers.ofString());
if (response.statusCode() != 200) {
System.err.println("   ❌ Discord send failed: " + response.statusCode());
}
} catch (Exception e) {
System.err.println("   ❌ Discord send error: " + e.getMessage());
}
}
/**
* Send Telegram message via REST API
*/
private void sendTelegramMessage(HttpClient client, std::string chatId, std::string content) {
try {
std::shared_ptr<JSONObject> json = std::make_shared<JSONObject>();
json.put("chat_id", chatId);
json.put("text", content);
HttpRequest request = HttpRequest.newBuilder()
.uri(URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
.header("Content-Type", "application/json")
.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
.build();
HttpResponse<std::string> response = client.send(request,
HttpResponse.BodyHandlers.ofString());
if (response.statusCode() != 200) {
System.err.println("   ❌ Telegram send failed: " + response.statusCode());
}
} catch (Exception e) {
System.err.println("   ❌ Telegram send error: " + e.getMessage());
}
}
/**
* Stop the spine
*/
public void stop() {
running = false;
std::cout << "   🛑 Claw Spine stopped" << std::endl;
}
/**
* Check if running
*/
public bool isRunning() {
return running;
}
/**
* Get platform
*/
public Platform getPlatform() {
return platform;
}
/**
* Get statistics
*/
public std::string getStats() {
return std::string.format(
"Platform: %s | Running: %s | Queue: %d messages",
platform, running, outgoingQueue.size()
);
}
}
