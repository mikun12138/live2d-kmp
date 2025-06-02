#version 330

in vec4 a_position;
in vec2 a_texCoord;

out vec2 v_texCoord;

uniform mat4 u_matrix;

void main()
{
    gl_Position = u_matrix * a_position;
    v_texCoord = vec2(a_texCoord.x, 1.0f - a_texCoord.y);
}