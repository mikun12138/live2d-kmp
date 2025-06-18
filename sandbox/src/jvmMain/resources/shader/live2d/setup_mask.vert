#version 330

in vec4 a_position;
in vec2 a_texCoord;

out vec2 v_texCoord;
out vec4 v_myPos;

uniform mat4 u_clipMatrix;

void main() {
    gl_Position = u_clipMatrix * a_position;
    v_myPos = u_clipMatrix * a_position;
    v_texCoord = vec2(a_texCoord.x, 1.0f - a_texCoord.y);
}

