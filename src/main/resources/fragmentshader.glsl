#version 330
out vec4 FragColor;
in vec2 interpolatedTexCoord;
uniform sampler2D textureAtlas;
void main()
{
    FragColor = texture(textureAtlas, interpolatedTexCoord);
}
