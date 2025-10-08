#version 330
out vec4 FragColor;  // saída: cor do pixel
in vec3 vColor;
void main()
{
    FragColor = vec4(vColor, 1.0);
}
