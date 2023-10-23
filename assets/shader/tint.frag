#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec4 u_tintColor;

void main()
{
    vec4 spriteColor = v_color * texture2D(u_texture, v_texCoords);
    vec3 blendedColor = mix(spriteColor.rgb, u_tintColor.rgb, u_tintColor.a);
    gl_FragColor = vec4(blendedColor.rgb, spriteColor.a);
}
