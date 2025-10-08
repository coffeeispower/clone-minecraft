#version 330
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 texCoord;
out vec2 interpolatedTexCoord;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;
void main() {
    interpolatedTexCoord = texCoord;
    gl_Position = projectionMatrix * viewMatrix * transformMatrix * vec4(aPos, 1.0);
}

