#version 330
out vec4 FragColor;  // saída: cor do pixel
in vec3 vertexPosition;
void main()
{
    FragColor = vec4((vertexPosition/vec3(2.0))+vec3(0.5), 1.0);  // cor laranja
}
