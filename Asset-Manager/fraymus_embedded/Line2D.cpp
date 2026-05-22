#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class Line2D { {
public:
private Vector2f from;
private Vector2f to;
private Vector3f color;
private int lifetime;
public Line2D(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
this.from = from;
this.to = to;
this.color = color;
this.lifetime = lifetime;
}
public int beginFrame() {
this.lifetime--;
return this.lifetime;
}
public Vector2f getFrom() {
return this.from;
}
public Vector2f getTo() {
return this.to;
}
public Vector3f getColor() {
return this.color;
}
public int getLifetime() {
return this.lifetime;
}
}
