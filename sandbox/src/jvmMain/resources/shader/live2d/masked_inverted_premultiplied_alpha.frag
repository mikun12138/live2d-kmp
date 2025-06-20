#version 330

in vec2 v_texCoord;
in vec4 v_clipPos;
uniform sampler2D s_texture0;
uniform sampler2D s_texture1;
uniform vec4 u_channelFlag;
uniform vec4 u_baseColor;
uniform vec4 u_multiplyColor;
uniform vec4 u_screenColor;

void main()
{
    vec4 texColor = texture2D(s_texture0, v_texCoord);
    texColor.rgb = texColor.rgb * u_multiplyColor.rgb;
    texColor.rgb = (texColor.rgb + u_screenColor.rgb * texColor.a) - (texColor.rgb * u_screenColor.rgb);
    vec4 col_formask = texColor * u_baseColor;
    vec4 clipMask = (1.0 - texture2D(s_texture1, v_clipPos.xy / v_clipPos.w)) * u_channelFlag;
    float maskVal = clipMask.r + clipMask.g + clipMask.b + clipMask.a;
    col_formask = col_formask * (1.0 - maskVal);
    gl_FragColor = col_formask;
}