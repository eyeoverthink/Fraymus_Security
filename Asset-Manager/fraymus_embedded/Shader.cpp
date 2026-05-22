#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class Shader { {
public:
private int shaderProgramID;
private bool beingUsed = false;
private std::string vertexSource;
private std::string fragmentSource;
private std::string filepath;
public Shader(std::string filepath) {
this.filepath = filepath;
try {
std::string source = new std::string(Files.readAllBytes(Paths.get(filepath)));
std::string[] splitString = source.split("(#type)( )+");
int index = source.indexOf("#type") + 6;
int eol = source.indexOf("\n", index);
std::string firstPattern = source.substring(index, eol).trim();
index = source.indexOf("#type", eol) + 6;
eol = source.indexOf("\n", index);
std::string secondPattern = source.substring(index, eol).trim();
if (firstPattern.equals("vertex")) {
vertexSource = source.substring(source.indexOf("\n", source.indexOf("#type") + 6) + 1,
source.indexOf("#type", source.indexOf("#type") + 6));
} else if (firstPattern.equals("fragment")) {
fragmentSource = source.substring(source.indexOf("\n", source.indexOf("#type") + 6) + 1,
source.indexOf("#type", source.indexOf("#type") + 6));
} else {
throw new IOException("Unexpected token '" + firstPattern + "' in shader file: " + filepath);
}
if (secondPattern.equals("vertex")) {
vertexSource = source.substring(source.indexOf("\n", source.lastIndexOf("#type")) + 1);
} else if (secondPattern.equals("fragment")) {
fragmentSource = source.substring(source.indexOf("\n", source.lastIndexOf("#type")) + 1);
} else {
throw new IOException("Unexpected token '" + secondPattern + "' in shader file: " + filepath);
}
} catch (IOException e) {
e.printStackTrace();
assert false : "Error: Could not open file for shader: '" + filepath + "'";
}
}
public void compile() {
int vertexID, fragmentID;
vertexID = glCreateShader(GL_VERTEX_SHADER);
glShaderSource(vertexID, vertexSource);
glCompileShader(vertexID);
int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
if (success == GL_FALSE) {
int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
System.err.println("ERROR: '" + filepath + "'\n\tVertex shader compilation failed.");
System.err.println(glGetShaderInfoLog(vertexID, len));
assert false : "";
}
fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
glShaderSource(fragmentID, fragmentSource);
glCompileShader(fragmentID);
success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
if (success == GL_FALSE) {
int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
System.err.println("ERROR: '" + filepath + "'\n\tFragment shader compilation failed.");
System.err.println(glGetShaderInfoLog(fragmentID, len));
assert false : "";
}
shaderProgramID = glCreateProgram();
glAttachShader(shaderProgramID, vertexID);
glAttachShader(shaderProgramID, fragmentID);
glLinkProgram(shaderProgramID);
success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
if (success == GL_FALSE) {
int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
System.err.println("ERROR: '" + filepath + "'\n\tShader program linking failed.");
System.err.println(glGetProgramInfoLog(shaderProgramID, len));
assert false : "";
}
glDeleteShader(vertexID);
glDeleteShader(fragmentID);
}
public void use() {
if (!beingUsed) {
glUseProgram(shaderProgramID);
beingUsed = true;
}
}
public void detach() {
glUseProgram(0);
beingUsed = false;
}
public void uploadMat4f(std::string varName, Matrix4f mat4) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
mat4.get(matBuffer);
glUniformMatrix4fv(varLocation, false, matBuffer);
}
public void uploadMat3f(std::string varName, Matrix3f mat3) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
mat3.get(matBuffer);
glUniformMatrix3fv(varLocation, false, matBuffer);
}
public void uploadVec4f(std::string varName, Vector4f vec) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
}
public void uploadVec3f(std::string varName, Vector3f vec) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
glUniform3f(varLocation, vec.x, vec.y, vec.z);
}
public void uploadVec2f(std::string varName, Vector2f vec) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
glUniform2f(varLocation, vec.x, vec.y);
}
public void uploadFloat(std::string varName, float val) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
glUniform1f(varLocation, val);
}
public void uploadInt(std::string varName, int val) {
int varLocation = glGetUniformLocation(shaderProgramID, varName);
use();
glUniform1i(varLocation, val);
}
}
