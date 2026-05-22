#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* THE PREDATOR PROTOCOL: METAMATERIAL RAY TRACING
* Simulates a photon moving through a Transformation Optics grid.
* "I see nothing, Captain."
*
* Visual proof of how light bends around a cloaked object using φ.
*/
class CloakVis { {
public:
private static const double PHI = 1.6180339887;
private static const int GRID_SIZE = 21;
private static const double CLOAK_RADIUS = 5.0;  // R2 - outer
private static const double OBJECT_RADIUS = 2.0; // R1 - inner
private static const int CENTER = GRID_SIZE / 2;
public static void main(std::string[] args) {
std::cout << "╔═══════════════════════════════════════════════════════════╗" << std::endl;
std::cout << "║      THE PREDATOR PROTOCOL - Metamaterial Cloak           ║" << std::endl;
std::cout << "║      \"I see nothing, Captain.\"                            ║" << std::endl;
std::cout << "╠═══════════════════════════════════════════════════════════╣" << std::endl;
std::cout << "║  Target: [LOCKED]     Cloak: [ACTIVE]    φ = " + PHI << std::endl;
std::cout << "╚═══════════════════════════════════════════════════════════╝" << std::endl;
std::cout <<  << std::endl;
// Fire multiple photons at different heights
char[][] display = new char[GRID_SIZE][GRID_SIZE];
initializeGrid(display);
// Fire photons from multiple Y positions
for (int startY = 5; startY <= 15; startY += 2) {
firePhoton(display, startY);
}
printGrid(display);
std::cout <<  << std::endl;
std::cout << "Legend: # = void* (hidden)  : = Cloak field  • = Photon path  C = Bent path" << std::endl;
}
private static void firePhoton(char[][] display, int startY) {
double photonX = 0;
double photonY = startY;
double angle = 0; // Moving straight right (0 radians)
for (int t = 0; t < GRID_SIZE + 10; t++) {
// 1. Update position
photonX += Math.cos(angle);
photonY += Math.sin(angle);
// Boundary check
if (photonX < 0 || photonX >= GRID_SIZE || photonY < 0 || photonY >= GRID_SIZE) {
break;
}
// 2. Calculate distance from center
double dist = Math.sqrt(Math.pow(photonX - CENTER, 2) + Math.pow(photonY - CENTER, 2));
// 3. Apply Transformation Optics
if (dist < CLOAK_RADIUS && dist > OBJECT_RADIUS) {
// Inside the Metamaterial - bend the light
// The deflection is proportional to how deep we are in the cloak
// Uses φ for the golden ratio deflection factor
double deflection = (CLOAK_RADIUS - dist) / CLOAK_RADIUS * 0.3 * PHI;
// Bend away from center
if (photonY < CENTER) {
angle -= deflection;
} else if (photonY > CENTER) {
angle += deflection;
}
// Mark bent path
plot(display, (int) photonX, (int) photonY, 'C');
} else if (dist <= OBJECT_RADIUS) {
// FAILURE: Hit the object
plot(display, (int) photonX, (int) photonY, 'X');
break;
} else {
// Empty space - straighten out after exiting cloak
if (photonX > CENTER + CLOAK_RADIUS) {
// Gradually return to straight path
angle *= 0.8;
}
plot(display, (int) photonX, (int) photonY, '•');
}
}
}
private static void initializeGrid(char[][] grid) {
for (int y = 0; y < GRID_SIZE; y++) {
for (int x = 0; x < GRID_SIZE; x++) {
double dist = Math.sqrt(Math.pow(x - CENTER, 2) + Math.pow(y - CENTER, 2));
if (dist <= OBJECT_RADIUS) {
grid[y][x] = '#'; // The void*
} else if (dist <= CLOAK_RADIUS) {
grid[y][x] = ':'; // The Cloak Field
} else {
grid[y][x] = ' '; // Empty Space
}
}
}
}
private static void plot(char[][] grid, int x, int y, char c) {
if (x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE) {
// Don't overwrite object or if already marked with more important char
if (grid[y][x] == ' ' || grid[y][x] == ':') {
grid[y][x] = c;
}
}
}
private static void printGrid(char[][] grid) {
// Top border
std::cout << "   ";
for (int x = 0; x < GRID_SIZE; x++) {
std::cout << x % 10;
}
std::cout <<  << std::endl;
for (int y = 0; y < GRID_SIZE; y++) {
System.out.printf("%2d ", y);
for (int x = 0; x < GRID_SIZE; x++) {
char c = grid[y][x];
// Color coding via ANSI (works in most terminals)
switch (c) {
case '#': std::cout << "\u001B[31m" + c + "\u001B[0m"; break; // Red - object
case ':': std::cout << "\u001B[34m" + c + "\u001B[0m"; break; // Blue - cloak
case 'C': std::cout << "\u001B[33m" + c + "\u001B[0m"; break; // Yellow - bent
case '•': std::cout << "\u001B[32m" + c + "\u001B[0m"; break; // Green - path
case 'X': std::cout << "\u001B[31mX\u001B[0m"; break;         // Red - hit
default: std::cout << c;
}
}
std::cout <<  << std::endl;
}
}
// Static method to run simulation and return result
public static std::string runSimulation() {
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
char[][] display = new char[GRID_SIZE][GRID_SIZE];
initializeGrid(display);
for (int startY = 5; startY <= 15; startY += 2) {
firePhoton(display, startY);
}
for (int y = 0; y < GRID_SIZE; y++) {
for (int x = 0; x < GRID_SIZE; x++) {
sb.append(display[y][x]);
}
sb.append('\n');
}
return sb.toString();
}
}
