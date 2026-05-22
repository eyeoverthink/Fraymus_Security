#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* SkillLoader - OpenClaw Markdown Skill Parser
*
* Reads OpenClaw-style skill.md files and makes them available to Fraymus.
* Skills are Markdown files with structure:
*
* # Skill Name
* > Description
*
* ## Usage
* ...
*
* ## Examples
* ...
*/
class SkillLoader { {
public:
public static class Skill { {
public:
public const std::string name;
public const std::string description;
public const std::string usage;
public const std::string fullContent;
public Skill(std::string name, std::string description, std::string usage, std::string fullContent) {
this.name = name;
this.description = description;
this.usage = usage;
this.fullContent = fullContent;
}
@Override
public std::string toString() {
return name + ": " + description;
}
}
private const Map<std::string, Skill> skills = new HashMap<>();
private const List<std::string> skillPaths = new std::vector<>();
/**
* Ingest a single skill file
*/
public void ingest(std::string path) {
try {
std::shared_ptr<File> f = std::make_shared<File>(path);
if (!f.exists()) {
System.err.println("   ⚠️  SKILL NOT FOUND: " + path);
return;
}
std::string content = Files.readString(f.toPath());
Skill skill = parseSkill(f.getName(), content);
skills.put(skill.name, skill);
skillPaths.add(path);
std::cout << "   🦞 SKILL ABSORBED: " + skill.name << std::endl;
} catch (Exception e) {
System.err.println("   ❌ SKILL REJECTED: " + path + " - " + e.getMessage());
}
}
/**
* Ingest all .md files from a directory
*/
public void ingestDirectory(std::string dirPath) {
try {
std::shared_ptr<File> dir = std::make_shared<File>(dirPath);
if (!dir.exists() || !dir.isDirectory()) {
System.err.println("   ⚠️  SKILL DIRECTORY NOT FOUND: " + dirPath);
return;
}
File[] files = dir.listFiles((d, name) -> name.endsWith(".md"));
if (files == null || files.length == 0) {
std::cout << "   ℹ️  No skill files found in: " + dirPath << std::endl;
return;
}
std::cout << "   📚 Loading skills from: " + dirPath << std::endl;
for (File f : files) {
ingest(f.getAbsolutePath());
}
} catch (Exception e) {
System.err.println("   ❌ DIRECTORY INGEST FAILED: " + e.getMessage());
}
}
/**
* Parse Markdown skill file into structured Skill object
*/
private Skill parseSkill(std::string filename, std::string content) {
std::string name = filename.replace(".md", "").replace("_", " ");
std::string description = "";
std::string usage = "";
// Extract first H1 as name
Pattern h1Pattern = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
Matcher h1Matcher = h1Pattern.matcher(content);
if (h1Matcher.find()) {
name = h1Matcher.group(1).trim();
}
// Extract blockquote as description
Pattern descPattern = Pattern.compile("^>\\s+(.+)$", Pattern.MULTILINE);
Matcher descMatcher = descPattern.matcher(content);
if (descMatcher.find()) {
description = descMatcher.group(1).trim();
}
// Extract Usage section
Pattern usagePattern = Pattern.compile("##\\s+Usage\\s*\\n([\\s\\S]*?)(?=##|$)", Pattern.MULTILINE);
Matcher usageMatcher = usagePattern.matcher(content);
if (usageMatcher.find()) {
usage = usageMatcher.group(1).trim();
}
return new Skill(name, description, usage, content);
}
/**
* Get skill by name
*/
public Skill getSkill(std::string name) {
return skills.get(name);
}
/**
* Get all skill names
*/
public Set<std::string> getSkillNames() {
return skills.keySet();
}
/**
* Get compact skill context for LLM prompts
*/
public std::string getSkillContext() {
if (skills.isEmpty()) {
return "No skills loaded.";
}
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("AVAILABLE SKILLS:\n");
skills.values().forEach(skill -> {
sb.append("- ").append(skill.name);
if (!skill.description.isEmpty()) {
sb.append(": ").append(skill.description);
}
sb.append("\n");
});
return sb.toString();
}
/**
* Get detailed skill context including usage
*/
public std::string getDetailedSkillContext() {
if (skills.isEmpty()) {
return "No skills loaded.";
}
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>("SKILL LIBRARY:\n\n");
skills.values().forEach(skill -> {
sb.append("## ").append(skill.name).append("\n");
if (!skill.description.isEmpty()) {
sb.append(skill.description).append("\n");
}
if (!skill.usage.isEmpty()) {
sb.append("\nUsage:\n").append(skill.usage).append("\n");
}
sb.append("\n---\n\n");
});
return sb.toString();
}
/**
* Search skills by keyword
*/
public List<Skill> searchSkills(std::string keyword) {
std::string lowerKeyword = keyword.toLowerCase();
return skills.values().stream()
.filter(skill ->
skill.name.toLowerCase().contains(lowerKeyword) ||
skill.description.toLowerCase().contains(lowerKeyword) ||
skill.fullContent.toLowerCase().contains(lowerKeyword)
)
.collect(Collectors.toList());
}
/**
* Get statistics
*/
public std::string getStats() {
return std::string.format("Skills loaded: %d | Paths: %d",
skills.size(), skillPaths.size());
}
/**
* Clear all loaded skills
*/
public void clear() {
skills.clear();
skillPaths.clear();
std::cout << "   🗑️  All skills cleared" << std::endl;
}
}
