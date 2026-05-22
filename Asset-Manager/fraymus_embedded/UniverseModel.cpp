#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* 🌌 FRAYMUS UNIVERSE MODEL - THE ORRERY
* "The Orrery of Logic."
*
* MISSION: Model solar system gravity and visualize distances.
*
* ARCHITECTURE:
* - LogicCircuit → Newtonian Physics Processor
* - LazarusEngine → Time-Step Physics Integrator
* - FrayUI → Processing OpenGL Renderer (via absorbed core.jar)
*
* PHYSICS:
* F = G × (m₁ × m₂) / r²
*
* At every frame:
* 1. Calculate force vectors between all planet pairs
* 2. Apply forces to update velocity vectors
* 3. Use velocity to update position vectors
* 4. Render the result
*/
class UniverseModel extends PApplet { {
public:
// ═══════════════════════════════════════════════════════════════════
// SIMULATION CONSTANTS (Scaled for visualization, not real-world)
// ═══════════════════════════════════════════════════════════════════
private static const float G = 0.5f;  // Gravitational Constant (scaled)
private List<Planet> planets = new std::vector<>();
private Planet earthRef;  // Reference for distance calculations
private List<PVector> trails = new std::vector<>();  // Orbital trails
private bool showTrails = true;
private bool showForceVectors = true;
private bool paused = false;
private float timeScale = 1.0f;
// ═══════════════════════════════════════════════════════════════════
// PROCESSING LIFECYCLE
// ═══════════════════════════════════════════════════════════════════
public void settings() {
size(1400, 900);  // The "Window onto the Universe"
}
public void setup() {
frameRate(60);
// Initialize the Cosmos
// Planet(name, mass, x, y, vx, vy, color)
// THE SUN - Massive, stationary at center
Planet sun = new Planet("☀ Sun", 1000, width/2, height/2, 0, 0,
color(255, 200, 50), true);
// MERCURY - Closest, fastest
Planet mercury = new Planet("Mercury", 2, width/2 + 80, height/2, 0, 2.8f,
color(180, 180, 180), false);
// VENUS - Second planet
Planet venus = new Planet("Venus", 6, width/2 + 130, height/2, 0, 2.2f,
color(230, 180, 100), false);
// EARTH - Reference planet (blue marble)
Planet earth = new Planet("🌍 Earth", 10, width/2 + 200, height/2, 0, 1.8f,
color(50, 100, 255), false);
earthRef = earth;
// MARS - The red planet
Planet mars = new Planet("Mars", 5, width/2 + 280, height/2, 0, 1.5f,
color(200, 80, 50), false);
// JUPITER - Gas giant
Planet jupiter = new Planet("Jupiter", 80, width/2 + 400, height/2, 0, 1.1f,
color(200, 150, 100), false);
planets.add(sun);
planets.add(mercury);
planets.add(venus);
planets.add(earth);
planets.add(mars);
planets.add(jupiter);
textFont(createFont("Consolas", 14));
println("╔═══════════════════════════════════════════════════════════╗");
println("║      🌌 FRAYMUS ORRERY - GRAVITATIONAL SIMULATOR          ║");
println("╠═══════════════════════════════════════════════════════════╣");
println("║  PHYSICS: F = G × (m₁ × m₂) / r²                          ║");
println("║  ENGINE:  Time-Step Verlet Integration                    ║");
println("╠═══════════════════════════════════════════════════════════╣");
println("║  CONTROLS:                                                ║");
println("║  [SPACE] Pause/Resume    [T] Toggle Trails                ║");
println("║  [F] Toggle Force Vectors    [R] Reset                    ║");
println("║  [+/-] Time Scale    [CLICK] Add Moon                     ║");
println("╚═══════════════════════════════════════════════════════════╝");
}
public void draw() {
background(15, 15, 25);  // Deep space
// Draw starfield
drawStarfield();
// Draw orbital trails
if (showTrails) {
drawTrails();
}
if (!paused) {
// ═══════════════════════════════════════════════════════════
// 1. PHYSICS STEP (The Lazarus Engine Loop)
// ═══════════════════════════════════════════════════════════
for (int t = 0; t < timeScale; t++) {
for (Planet p : planets) {
p.calculateGravity(planets);
}
for (Planet p : planets) {
p.update();
}
}
}
// ═══════════════════════════════════════════════════════════════
// 2. RENDER STEP (The Visual Cortex)
// ═══════════════════════════════════════════════════════════════
// Draw gravity connection lines
drawGravityLines();
// Draw force vectors
if (showForceVectors) {
drawForceVectors();
}
// Draw planets
for (Planet p : planets) {
p.display();
// Record trail
if (showTrails && !p.isSun && frameCount % 3 == 0) {
trails.add(new PVector(p.pos.x, p.pos.y, p.col));
}
}
// ═══════════════════════════════════════════════════════════════
// 3. DATA VISUALIZATION (The Professor's Requirement)
// ═══════════════════════════════════════════════════════════════
drawDashboard();
drawControls();
// Limit trail length
while (trails.size() > 2000) {
trails.remove(0);
}
}
// ═══════════════════════════════════════════════════════════════════
// VISUALIZATION METHODS
// ═══════════════════════════════════════════════════════════════════
private void drawStarfield() {
noStroke();
for (int i = 0; i < 150; i++) {
float x = (noise(i * 0.1f) * width * 2) % width;
float y = (noise(i * 0.2f + 100) * height * 2) % height;
float brightness = noise(i * 0.05f + frameCount * 0.01f) * 100 + 100;
fill(brightness);
ellipse(x, y, 1 + (i % 3), 1 + (i % 3));
}
}
private void drawTrails() {
noFill();
strokeWeight(1);
for (PVector t : trails) {
stroke(t.z, 50);  // z stores color
point(t.x, t.y);
}
}
private void drawGravityLines() {
if (earthRef == null) return;
for (Planet p : planets) {
if (p == earthRef) continue;
// Draw connecting line with distance gradient
float dist = PVector.dist(earthRef.pos, p.pos);
stroke(100, 200, 100, map(dist, 0, 500, 150, 30));
strokeWeight(1);
line(earthRef.pos.x, earthRef.pos.y, p.pos.x, p.pos.y);
// Distance label at midpoint
PVector mid = PVector.add(earthRef.pos, p.pos).div(2);
fill(200, 255, 200);
textAlign(CENTER, CENTER);
textSize(11);
text(std::string.format("%.0f Mm", dist), mid.x, mid.y - 8);
}
}
private void drawForceVectors() {
strokeWeight(2);
for (Planet p : planets) {
if (p.isSun) continue;
// Draw velocity vector (cyan)
stroke(0, 255, 255, 150);
PVector velEnd = PVector.add(p.pos, PVector.mult(p.vel, 20));
line(p.pos.x, p.pos.y, velEnd.x, velEnd.y);
// Draw acceleration vector (magenta)
stroke(255, 0, 255, 150);
PVector accEnd = PVector.add(p.pos, PVector.mult(p.lastAcc, 500));
line(p.pos.x, p.pos.y, accEnd.x, accEnd.y);
}
}
private void drawDashboard() {
// Panel background
fill(20, 20, 30, 220);
noStroke();
rect(10, 10, 320, 220, 8);
// Title
fill(100, 200, 255);
textAlign(LEFT, TOP);
textSize(16);
text("🌌 GRAVITATIONAL DISTANCE TRACKER", 20, 20);
fill(150);
textSize(11);
text("Reference: Earth", 20, 42);
// Separator
stroke(100);
line(20, 58, 310, 58);
// Distance table
textSize(13);
int y = 70;
// Header
fill(180);
text("BODY", 25, y);
text("DISTANCE", 130, y);
text("GRAVITY (N)", 220, y);
y += 22;
for (Planet p : planets) {
if (p == earthRef) continue;
float dist = PVector.dist(earthRef.pos, p.pos);
float force = (G * earthRef.mass * p.mass) / (dist * dist);
// Color-coded planet name
fill(p.col);
text(p.name, 25, y);
// Distance value
fill(200, 255, 200);
text(std::string.format("%8.1f Mm", dist), 130, y);
// Gravitational force
fill(255, 200, 100);
text(std::string.format("%.4f", force), 220, y);
y += 20;
}
// Physics formula
fill(100);
textSize(10);
text("F = G × (m₁ × m₂) / r²", 20, 200);
text("G = " + G + " (scaled)", 180, 200);
}
private void drawControls() {
fill(20, 20, 30, 180);
noStroke();
rect(width - 260, height - 90, 250, 80, 8);
fill(180);
textAlign(LEFT, TOP);
textSize(11);
int y = height - 80;
text("[SPACE] " + (paused ? "Resume" : "Pause"), width - 250, y);
text("[T] Trails: " + (showTrails ? "ON" : "OFF"), width - 120, y);
y += 18;
text("[F] Forces: " + (showForceVectors ? "ON" : "OFF"), width - 250, y);
text("[R] Reset", width - 120, y);
y += 18;
text("[+/-] Time: " + timeScale + "x", width - 250, y);
text("[CLICK] Add Body", width - 120, y);
y += 18;
text("FPS: " + (int)frameRate, width - 250, y);
}
// ═══════════════════════════════════════════════════════════════════
// INPUT HANDLING
// ═══════════════════════════════════════════════════════════════════
public void keyPressed() {
if (key == ' ') {
paused = !paused;
} else if (key == 't' || key == 'T') {
showTrails = !showTrails;
if (!showTrails) trails.clear();
} else if (key == 'f' || key == 'F') {
showForceVectors = !showForceVectors;
} else if (key == 'r' || key == 'R') {
setup();
trails.clear();
} else if (key == '+' || key == '=') {
timeScale = min(timeScale + 0.5f, 5.0f);
} else if (key == '-') {
timeScale = max(timeScale - 0.5f, 0.5f);
}
}
public void mousePressed() {
// Add a small moon at click location
if (mouseButton == LEFT) {
// Calculate initial velocity perpendicular to Sun
std::shared_ptr<PVector> toSun = std::make_shared<PVector>(width/2 - mouseX, height/2 - mouseY);
float dist = toSun.mag();
// Orbital velocity for circular orbit
float orbitalV = sqrt(G * 1000 / dist);
std::shared_ptr<PVector> vel = std::make_shared<PVector>(-toSun.y, toSun.x).normalize().mult(orbitalV);
Planet moon = new Planet("Moon" + (planets.size() - 5), 1,
mouseX, mouseY, vel.x, vel.y,
color(180, 180, 200), false);
planets.add(moon);
}
}
// ═══════════════════════════════════════════════════════════════════
// PLANET CLASS - The Atom of the Simulation
// ═══════════════════════════════════════════════════════════════════
class Planet { {
public:
std::string name;
float mass;
PVector pos;
PVector vel;
PVector acc;
PVector lastAcc;  // For visualization
int col;
float radius;
bool isSun;
Planet(std::string n, float m, float x, float y, float vx, float vy, int c, bool sun) {
name = n;
mass = m;
pos = new PVector(x, y);
vel = new PVector(vx, vy);
acc = new PVector(0, 0);
lastAcc = new PVector(0, 0);
col = c;
radius = sqrt(mass) * 2;  // Visual size based on mass
isSun = sun;
}
void calculateGravity(List<Planet> allPlanets) {
acc.set(0, 0);  // Reset
for (Planet other : allPlanets) {
if (other == this) continue;
// Vector pointing from this to other
PVector force = PVector.sub(other.pos, this.pos);
float distanceSq = force.magSq();
// Prevent singularities and extreme forces
distanceSq = constrain(distanceSq, 100, 100000);
// F = G × (m1 × m2) / r²
float strength = (G * (this.mass * other.mass)) / distanceSq;
force.setMag(strength);
// a = F / m (Newton's 2nd Law)
PVector acceleration = PVector.div(force, this.mass);
this.acc.add(acceleration);
}
lastAcc.set(acc);  // Store for visualization
}
void update() {
if (isSun) return;  // Sun is stationary
vel.add(acc);  // v = v + a×dt (dt=1)
pos.add(vel);  // p = p + v×dt
}
void display() {
noStroke();
// Glow effect for Sun
if (isSun) {
for (int i = 5; i > 0; i--) {
fill(255, 200, 50, 30);
ellipse(pos.x, pos.y, radius * 2 + i * 15, radius * 2 + i * 15);
}
}
// Planet body
fill(col);
ellipse(pos.x, pos.y, radius * 2, radius * 2);
// Highlight
fill(255, 255, 255, 100);
ellipse(pos.x - radius * 0.3f, pos.y - radius * 0.3f, radius * 0.5f, radius * 0.5f);
// Name label
fill(255);
textAlign(CENTER, TOP);
textSize(12);
text(name, pos.x, pos.y + radius + 8);
}
}
// ═══════════════════════════════════════════════════════════════════
// MAIN - Launch the Orrery
// ═══════════════════════════════════════════════════════════════════
public static void main(std::string[] args) {
PApplet.main("fraymus.sim.UniverseModel");
}
}
