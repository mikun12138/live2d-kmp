#version 330

in vec4 a_position;
in vec2 a_texCoord;
out vec2 v_texCoord;
out vec4 v_clipPos;
uniform mat4 u_matrix;
uniform mat4 u_clipMatrix;
void main()
{
    gl_Position = u_matrix * a_position;
    v_clipPos = u_clipMatrix * a_position;
    v_texCoord = a_texCoord;
    v_texCoord.y = 1.0 - v_texCoord.y;
}
