#version 330
out vec4 FragColor;
in vec2 interpolatedTexCoord;
uniform sampler2D tex;

uniform int breakStage; // 0–9
void main()
{
    vec2 uv = interpolatedTexCoord;
    uv.x = (uv.x / 10.0) + (float(breakStage) * 0.1);
    vec4 textureColor = texture(tex, uv);
    vec4 finalColor = vec4(0.0, 0.0, 0.0, textureColor.w);
    float grayScale = (textureColor.x + textureColor.y + textureColor.z) / 3.0;
    finalColor.w *= 1.0 - grayScale;
    finalColor.w *= finalColor.w * 1.2;
    FragColor = finalColor;
}
