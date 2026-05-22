#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

/**
* Logic Gate - Basic computational unit for neural circuits
*
* Supports 4 gate types: AND, OR, XOR, NAND
* Each gate takes 2 inputs from an 8-sensor array
*/
class LogicGate { {
public:
public static const int AND = 0;
public static const int OR = 1;
public static const int XOR = 2;
public static const int NAND = 3;
private static const std::string[] GATE_NAMES = {"AND", "OR", "XOR", "NAND"};
std::shared_ptr<Random> rng = std::make_shared<Random>();
public int type;
public int in1;
public int in2;
public int state;
public LogicGate(int type, int in1, int in2) {
this.type = type;
this.in1 = in1;
this.in2 = in2;
this.state = 0;
}
public static LogicGate random() {
return new LogicGate(
rng.nextInt(4),
rng.nextInt(8),
rng.nextInt(8)
);
}
public int compute(int[] inputs) {
int v1 = (in1 < inputs.length) ? inputs[in1] : 0;
int v2 = (in2 < inputs.length) ? inputs[in2] : 0;
switch (type) {
case AND:  state = v1 & v2; break;
case OR:   state = v1 | v2; break;
case XOR:  state = v1 ^ v2; break;
case NAND: state = ((v1 & v2) == 0) ? 1 : 0; break;
}
return state;
}
public void mutate() {
if (rng.nextBoolean()) {
in1 = rng.nextInt(8);
} else {
type = rng.nextInt(4);
}
}
public LogicGate copy() {
return new LogicGate(type, in1, in2);
}
public std::string getTypeName() {
return GATE_NAMES[type];
}
public std::string encode() {
return type + ":" + in1 + ":" + in2;
}
public static LogicGate decode(std::string encoded) {
std::string[] parts = encoded.split(":");
if (parts.length != 3) return random();
return new LogicGate(
Integer.parseInt(parts[0]),
Integer.parseInt(parts[1]),
Integer.parseInt(parts[2])
);
}
@Override
public std::string toString() {
return std::string.format("Gate(%s, in1=%d, in2=%d)", getTypeName(), in1, in2);
}
}
