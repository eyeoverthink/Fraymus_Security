#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class AssetPool { {
public:
private static Map<std::string, Shader> shaders = new HashMap<>();
private static Map<std::string, Texture> textures = new HashMap<>();
public static Shader getShader(std::string resourceName) {
std::shared_ptr<File> file = std::make_shared<File>(resourceName);
std::string key = file.getAbsolutePath();
if (shaders.containsKey(key)) {
return shaders.get(key);
} else {
std::shared_ptr<Shader> shader = std::make_shared<Shader>(resourceName);
shader.compile();
shaders.put(key, shader);
return shader;
}
}
public static Texture getTexture(std::string resourceName) {
std::shared_ptr<File> file = std::make_shared<File>(resourceName);
std::string key = file.getAbsolutePath();
if (textures.containsKey(key)) {
return textures.get(key);
} else {
std::shared_ptr<Texture> texture = std::make_shared<Texture>();
texture.init(resourceName);
textures.put(key, texture);
return texture;
}
}
}
