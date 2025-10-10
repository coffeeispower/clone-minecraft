#version 330
layout (location = 0) in vec3 aPos;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;
void main() {
    gl_Position = projectionMatrix * viewMatrix * transformMatrix * vec4(aPos, 1.0);
}

