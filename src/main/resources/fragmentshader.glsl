#version 330
out vec4 FragColor;
in vec2 interpolatedTexCoord;
uniform sampler2D testTexture;
void main()
{
    FragColor = texture(testTexture, interpolatedTexCoord);
}
