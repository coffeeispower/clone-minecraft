#version 330
out vec4 FragColor;
in vec2 interpolatedTexCoord;
uniform sampler2D tex;

uniform int digit; // 0–9
void main()
{
    vec2 uv = interpolatedTexCoord;
    uv.x = (uv.x / 10.0) + (float(digit) * (1. / 10.));
    vec4 textureColor = texture(tex, uv);
    FragColor = textureColor;
}
