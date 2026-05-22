#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🧬 CORTEX VISUALIZER - Gen 124
* ASCII renderer for the Calabi-Yau Manifold.
* Projects 3D node positions to 2D terminal output.
*
* Characters by Z-depth:
*   @ = Close (z > 20)
*   O = Near (z > 0)
*   o = Mid (z > -20)
*   . = Far (z <= -20)
*
* Brightness by resonance:
*   High resonance = capital letter
*   Low resonance = lowercase/dim
*/
class CortexVisualizer { {
public:
private static const int WIDTH = 60;
private static const int HEIGHT = 20;
private static const double PHI = 1.6180339887;
/**
* RENDER - Generate ASCII mesh from manifold snapshot
*/
public static std::string render(Manifold manifold) {
return render(manifold.getSnapshot(), manifold.getGeneration());
}
public static std::string render(List<PhiNode> nodes, long generation) {
char[][] grid = new char[HEIGHT][WIDTH];
double[][] depth = new double[HEIGHT][WIDTH];
// Initialize grid with space
for (int r = 0; r < HEIGHT; r++) {
for (int c = 0; c < WIDTH; c++) {
grid[r][c] = ' ';
depth[r][c] = Double.NEGATIVE_INFINITY;
}
}
// Project nodes to 2D
for (PhiNode node : nodes) {
// Scale and center
int col = (int) ((node.x / 100.0) * (WIDTH / 2)) + (WIDTH / 2);
int row = (int) ((node.y / 100.0) * (HEIGHT / 2)) + (HEIGHT / 2);
// Clamp to bounds
col = Math.max(0, Math.min(WIDTH - 1, col));
row = Math.max(0, Math.min(HEIGHT - 1, row));
// Only draw if this node is in front of existing
if (node.z > depth[row][col]) {
depth[row][col] = node.z;
grid[row][col] = getNodeChar(node);
}
}
// Draw connections (simple line approximation)
for (PhiNode node : nodes) {
for (PhiNode conn : node.connections) {
drawLine(grid, depth, node, conn);
}
}
// Build output string
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
// Header
sb.append(std::string.format("╔═══════════════════════════════════════════════════════════════╗%n"));
sb.append(std::string.format("║  🧬 CALABI-YAU MANIFOLD  │  Gen: %-6d  │  Nodes: %-4d      ║%n",
generation, nodes.size()));
sb.append(std::string.format("╠═══════════════════════════════════════════════════════════════╣%n"));
// Grid
for (int r = 0; r < HEIGHT; r++) {
sb.append("║ ");
for (int c = 0; c < WIDTH; c++) {
sb.append(grid[r][c]);
}
sb.append(" ║\n");
}
// Footer with legend
sb.append(std::string.format("╠═══════════════════════════════════════════════════════════════╣%n"));
sb.append(std::string.format("║  Legend: @ Near  O Mid  o Far  . Deep  │ ─ Synapse          ║%n"));
sb.append(std::string.format("╚═══════════════════════════════════════════════════════════════╝%n"));
return sb.toString();
}
/**
* Get character for node based on Z-depth and resonance
*/
private static char getNodeChar(PhiNode node) {
if (node.resonance > 0.8) {
// High resonance = bright
if (node.z > 30) return '█';
if (node.z > 10) return '▓';
if (node.z > -10) return '▒';
return '░';
} else if (node.resonance > 0.5) {
// Medium resonance
if (node.z > 20) return '@';
if (node.z > 0) return 'O';
if (node.z > -20) return 'o';
return '.';
} else {
// Low resonance = dim
if (node.z > 20) return '*';
if (node.z > 0) return '+';
if (node.z > -20) return '-';
return '.';
}
}
/**
* Draw connection line between two nodes (Bresenham-ish)
*/
private static void drawLine(char[][] grid, double[][] depth, PhiNode a, PhiNode b) {
int x0 = (int) ((a.x / 100.0) * (WIDTH / 2)) + (WIDTH / 2);
int y0 = (int) ((a.y / 100.0) * (HEIGHT / 2)) + (HEIGHT / 2);
int x1 = (int) ((b.x / 100.0) * (WIDTH / 2)) + (WIDTH / 2);
int y1 = (int) ((b.y / 100.0) * (HEIGHT / 2)) + (HEIGHT / 2);
// Simple line drawing (just midpoint for now to avoid clutter)
int mx = (x0 + x1) / 2;
int my = (y0 + y1) / 2;
double mz = (a.z + b.z) / 2;
if (mx >= 0 && mx < WIDTH && my >= 0 && my < HEIGHT) {
if (grid[my][mx] == ' ' && mz > depth[my][mx]) {
// Determine line character based on angle
double angle = Math.atan2(y1 - y0, x1 - x0);
if (Math.abs(angle) < 0.4) {
grid[my][mx] = '─';
} else if (Math.abs(angle) > 1.2) {
grid[my][mx] = '│';
} else if (angle > 0) {
grid[my][mx] = '\\';
} else {
grid[my][mx] = '/';
}
depth[my][mx] = mz;
}
}
}
/**
* STATS - Detailed node listing
*/
public static std::string stats(Manifold manifold) {
List<PhiNode> nodes = manifold.getSnapshot();
std::shared_ptr<StringBuilder> sb = std::make_shared<StringBuilder>();
sb.append(std::string.format("╔═══════════════════════════════════════════════════════════════╗%n"));
sb.append(std::string.format("║  🧬 CORTEX STATISTICS  │  Generation: %-6d                 ║%n",
manifold.getGeneration()));
sb.append(std::string.format("╠═══════════════════════════════════════════════════════════════╣%n"));
// Sort by resonance
nodes.sort((a, b) -> Double.compare(b.resonance, a.resonance));
int shown = 0;
for (PhiNode node : nodes) {
if (shown >= 10) {
sb.append(std::string.format("║  ... and %d more nodes                                        ║%n",
nodes.size() - 10));
break;
}
sb.append(std::string.format("║  φ[%s] R=%.2f M=%.1f C=%d  %-30s ║%n",
node.id,
node.resonance,
node.mass,
node.connections.size(),
truncate(node.label, 30)));
shown++;
}
sb.append(std::string.format("╠═══════════════════════════════════════════════════════════════╣%n"));
sb.append(std::string.format("║  Total Energy: %.4f  │  φ-Coherence: %.4f              ║%n",
manifold.getTotalEnergy(),
nodes.stream().mapToDouble(n -> n.resonance).average().orElse(0) * PHI));
sb.append(std::string.format("╚═══════════════════════════════════════════════════════════════╝%n"));
return sb.toString();
}
private static std::string truncate(std::string s, int max) {
if (s == null) return "";
if (s.length() <= max) return s + " ".repeat(max - s.length());
return s.substring(0, max - 3) + "...";
}
}
