#version 300 es

precision highp float;

in vec2 v_texCoord;
in vec4 v_myPos;

uniform sampler2D s_texture0;
uniform vec4 u_channelFlag;
uniform vec4 u_baseColor;

out vec4 fragColor;

void main()
{
    float isInside = step(u_baseColor.x, v_myPos.x / v_myPos.w)
        * step(u_baseColor.y, v_myPos.y / v_myPos.w)
        * step(v_myPos.x / v_myPos.w, u_baseColor.z)
        * step(v_myPos.y / v_myPos.w, u_baseColor.w);
    fragColor = u_channelFlag * texture(s_texture0, v_texCoord).a * isInside;
}