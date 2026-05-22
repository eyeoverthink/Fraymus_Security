#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* CLAW PARTICLE - The Physical Representation
*
* In the GravityEngine, OpenClaw isn't software - it's a High-Mass Particle.
* It has immense gravity, pulling "Task" particles toward it.
* When they collide, the task executes.
*/
class ClawParticle extends PhiSuit<std::string> { {
public:
private const ClawConnector nerve;
private bool isBusy = false;
public ClawParticle(int x, int y, int z) {
super("OPEN_CLAW_AGENT", x, y, z);
this.nerve = new ClawConnector();
this.a = 100; // Massive gravity well (amplitude)
}
/**
* The Fusion Logic: When a Task hits the Claw, EXECUTE IT.
* Called by GravityEngine when particles are close enough.
*/
public void executeTask(PhiSuit<?> other) {
// Only react to High-Energy Tasks
if (other.getLabel().startsWith("TASK_") && !isBusy) {
std::cout << "⚡ KINETIC CAPTURE: Claw caught " + other.getLabel() << std::endl;
this.isBusy = true;
this.a = 100; // Spike amplitude during work
// Execute in background (don't block physics loop)
new Thread(() -> {
std::string result = nerve.dispatch(other.get().toString(), "Priority: Critical");
std::cout << "   > RESULT: " + result << std::endl;
this.isBusy = false;
this.a = 10; // Cool down
}).start();
}
}
}
