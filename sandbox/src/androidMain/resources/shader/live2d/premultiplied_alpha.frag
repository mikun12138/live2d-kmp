#version 300 es

precision highp float;

in vec2 v_texCoord;
uniform sampler2D s_texture0;
uniform vec4 u_baseColor;
uniform vec4 u_multiplyColor;
uniform vec4 u_screenColor;

out vec4 fragColor;

void main()
{
    vec4 texColor = texture(s_texture0, v_texCoord);
    texColor.rgb = texColor.rgb * u_multiplyColor.rgb;
    texColor.rgb = (texColor.rgb + u_screenColor.rgb * texColor.a) - (texColor.rgb * u_screenColor.rgb);
    fragColor = texColor * u_baseColor;
//    gl_FragColor = vec4((texColor * u_baseColor).rgb * (texColor * u_baseColor).a, (texColor * u_baseColor).a);
}
