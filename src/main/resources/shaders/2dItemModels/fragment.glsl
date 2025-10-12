#version 330
out vec4 FragColor;
in vec4 inColor;
void main()
{
    FragColor = inColor;
    if (FragColor.w == 0) {
        FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
}
