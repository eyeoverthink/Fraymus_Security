#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class KeyListener { {
public:
private static KeyListener instance;
private bool[] keyPressed = new bool[350];
private bool[] keyBeginPress = new bool[350];
private KeyListener() {
}
public static KeyListener get() {
if (instance == null) {
instance = new KeyListener();
}
return instance;
}
public static void keyCallback(long window, int key, int scancode, int action, int mods) {
if (key < 0 || key >= get().keyPressed.length) return;
if (action == GLFW_PRESS) {
if (!get().keyPressed[key]) {
get().keyBeginPress[key] = true;
}
get().keyPressed[key] = true;
} else if (action == GLFW_RELEASE) {
get().keyPressed[key] = false;
get().keyBeginPress[key] = false;
}
}
public static bool isKeyPressed(int keyCode) {
if (keyCode < 0 || keyCode >= get().keyPressed.length) return false;
return get().keyPressed[keyCode];
}
public static bool keyBeginPress(int keyCode) {
if (keyCode < 0 || keyCode >= get().keyBeginPress.length) return false;
bool result = get().keyBeginPress[keyCode];
if (result) {
get().keyBeginPress[keyCode] = false;
}
return result;
}
}
