precision mediump float;

uniform sampler2D u_TextureUnit;
uniform sampler2D u_TextureMask;

uniform vec4 u_BlendColour;
uniform float u_BlendReverse;

varying vec2 v_TextureCoordinates;



void main(void)
{
    vec4 col = texture2D(u_TextureUnit, v_TextureCoordinates) * u_BlendColour;
    vec4 mask = texture2D(u_TextureMask, v_TextureCoordinates);
    col.a *= abs(u_BlendReverse- mask.a);
    gl_FragColor = col;
}