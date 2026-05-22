#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CONFIG - Command-line Arguments Parser
*
* Supports:
* --model <name>      Chat model (default: llama3)
* --embed <name>      Embedding model (default: embeddinggemma)
* --port <number>     WebSocket port (default: 8887)
* --index <path>      Index path and exit
*/
class Config { {
public:
public std::string chatModel = "llama3";
public std::string embedModel = "embeddinggemma";
public int port = 8887;
public std::string indexPath = null;
public static Config fromArgs(std::string[] args) {
std::shared_ptr<Config> c = std::make_shared<Config>();
for (int i = 0; i < args.length; i++) {
switch (args[i]) {
case "--model":
if (i + 1 < args.length) {
c.chatModel = args[++i];
}
break;
case "--embed":
if (i + 1 < args.length) {
c.embedModel = args[++i];
}
break;
case "--port":
if (i + 1 < args.length) {
c.port = Integer.parseInt(args[++i]);
}
break;
case "--index":
if (i + 1 < args.length) {
c.indexPath = args[++i];
}
break;
}
}
return c;
}
@Override
public std::string toString() {
return "Config{chatModel='" + chatModel + "', embedModel='" + embedModel +
"', port=" + port + ", indexPath='" + indexPath + "'}";
}
}
