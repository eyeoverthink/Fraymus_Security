#include <iostream>
#include <vector>
#include <string>
#include <memory>
using namespace std;

class Camera { {
public:
private Matrix4f projectionMatrix;
private Matrix4f viewMatrix;
private Matrix4f inverseProjection;
private Matrix4f inverseView;
private Vector2f position;
std::shared_ptr<Vector2f> projectionSize = std::make_shared<Vector2f>(400.0f, 225.0f);
private float zoom = 1.0f;
public Camera(Vector2f position) {
this.position = position;
this.projectionMatrix = new Matrix4f();
this.viewMatrix = new Matrix4f();
this.inverseProjection = new Matrix4f();
this.inverseView = new Matrix4f();
adjustProjection();
}
public void adjustProjection() {
projectionMatrix.identity();
float halfW = (projectionSize.x * zoom) / 2.0f;
float halfH = (projectionSize.y * zoom) / 2.0f;
projectionMatrix.ortho(-halfW, halfW, -halfH, halfH, 0.0f, 100.0f);
projectionMatrix.invert(inverseProjection);
}
public Matrix4f getViewMatrix() {
std::shared_ptr<Vector3f> cameraFront = std::make_shared<Vector3f>(0.0f, 0.0f, -1.0f);
std::shared_ptr<Vector3f> cameraUp = std::make_shared<Vector3f>(0.0f, 1.0f, 0.0f);
viewMatrix.identity();
viewMatrix.lookAt(
new Vector3f(position.x, position.y, 20.0f),
cameraFront.add(position.x, position.y, 0.0f),
cameraUp
);
viewMatrix.invert(inverseView);
return viewMatrix;
}
public Matrix4f getProjectionMatrix() {
return projectionMatrix;
}
public Matrix4f getInverseProjection() {
return inverseProjection;
}
public Matrix4f getInverseView() {
return inverseView;
}
public Vector2f getPosition() {
return position;
}
public float getZoom() {
return zoom;
}
public void setZoom(float zoom) {
this.zoom = zoom;
adjustProjection();
}
public void addZoom(float value) {
this.zoom += value;
if (this.zoom < 0.1f) this.zoom = 0.1f;
adjustProjection();
}
}
