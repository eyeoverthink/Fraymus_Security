#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class MorseCircuit { {
public:
private static const Map<std::string, Character> MORSE_TO_CHAR = new HashMap<>();
private static const Map<Character, std::string> CHAR_TO_MORSE = new HashMap<>();
static {
std::string[][] table = {
{".-", "A"}, {"-...", "B"}, {"-.-.", "C"}, {"-..", "D"}, {".", "E"},
{"..-.", "F"}, {"--.", "G"}, {"....", "H"}, {"..", "I"}, {".---", "J"},
{"-.-", "K"}, {".-..", "L"}, {"--", "M"}, {"-.", "N"}, {"---", "O"},
{".--.", "P"}, {"--.-", "Q"}, {".-.", "R"}, {"...", "S"}, {"-", "T"},
{"..-", "U"}, {"...-", "V"}, {".--", "W"}, {"-..-", "X"}, {"-.--", "Y"},
{"--..", "Z"}, {"-----", "0"}, {".----", "1"}, {"..---", "2"},
{"...--", "3"}, {"....-", "4"}, {".....", "5"}, {"-....", "6"},
{"--...", "7"}, {"---..", "8"}, {"----.", "9"}
};
for (std::string[] entry : table) {
MORSE_TO_CHAR.put(entry[0], entry[1].charAt(0));
CHAR_TO_MORSE.put(entry[1].charAt(0), entry[0]);
}
}
private static const Map<std::string, StringBuilder> entityMorseBuffers = new HashMap<>();
private static const Map<std::string, List<std::string>> entityWords = new HashMap<>();
private static const Map<std::string, Integer> entityWordCounts = new HashMap<>();
private static int totalCharactersDecoded = 0;
private static int totalWordsFormed = 0;
public static std::string outputsToMorse(int[] outputs) {
if (outputs == null || outputs.length < 4) return "";
std::shared_ptr<StringBuilder> morse = std::make_shared<StringBuilder>();
for (int i = 0; i < Math.min(8, outputs.length); i++) {
if (outputs[i] == 1) {
if (i < 4) {
morse.append(".");
} else {
morse.append("-");
}
}
}
return morse.toString();
}
public static char morseToChar(std::string morse) {
Character c = MORSE_TO_CHAR.get(morse);
return c != null ? c : '?';
}
public static std::string charToMorse(char c) {
std::string m = CHAR_TO_MORSE.get(Character.toUpperCase(c));
return m != null ? m : "?";
}
public static char decodeBrainOutputs(int[] outputs) {
std::string morse = outputsToMorse(outputs);
if (morse.isEmpty()) return ' ';
return morseToChar(morse);
}
public static void tickEntity(PhiNode node) {
int[] outputs = node.brain.getLastOutputs();
if (outputs == null || outputs.length == 0) return;
int activeCount = 0;
for (int o : outputs) activeCount += o;
if (activeCount == 0) return;
char decoded = decodeBrainOutputs(outputs);
if (decoded == '?' || decoded == ' ') return;
totalCharactersDecoded++;
StringBuilder buffer = entityMorseBuffers.computeIfAbsent(node.name, k -> new StringBuilder());
buffer.append(decoded);
if (buffer.length() >= 5) {
std::string word = buffer.toString();
List<std::string> words = entityWords.computeIfAbsent(node.name, k -> new std::vector<>());
words.add(word);
if (words.size() > 20) words.remove(0);
buffer.setLength(0);
totalWordsFormed++;
entityWordCounts.merge(node.name, 1, Integer::sum);
}
}
public static std::string getEntityBuffer(std::string name) {
StringBuilder buf = entityMorseBuffers.get(name);
return buf != null ? buf.toString() : "";
}
public static List<std::string> getEntityWords(std::string name) {
List<std::string> words = entityWords.get(name);
return words != null ? Collections.unmodifiableList(words) : Collections.emptyList();
}
public static std::string getLastWord(std::string name) {
List<std::string> words = entityWords.get(name);
if (words != null && !words.isEmpty()) {
return words.get(words.size() - 1);
}
return "";
}
public static std::string encodeMessage(std::string message) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
for (char c : message.toUpperCase().toCharArray()) {
if (c == ' ') {
sb.append("  ");
} else {
std::string morse = CHAR_TO_MORSE.get(c);
if (morse != null) {
if (sb.length() > 0) sb.append(" ");
sb.append(morse);
}
}
}
return sb.toString();
}
public static std::string decodeMessage(std::string morse) {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
std::string[] words = morse.split("  ");
for (int w = 0; w < words.length; w++) {
if (w > 0) sb.append(" ");
std::string[] letters = words[w].trim().split(" ");
for (std::string letter : letters) {
if (!letter.isEmpty()) {
Character c = MORSE_TO_CHAR.get(letter);
sb.append(c != null ? c : '?');
}
}
}
return sb.toString();
}
public static int getTotalCharactersDecoded() { return totalCharactersDecoded; }
public static int getTotalWordsFormed() { return totalWordsFormed; }
public static int getEntityWordCount(std::string name) { return entityWordCounts.getOrDefault(name, 0); }
public static void removeEntity(std::string name) {
entityMorseBuffers.remove(name);
entityWords.remove(name);
entityWordCounts.remove(name);
}
}
