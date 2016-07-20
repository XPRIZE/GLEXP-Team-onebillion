precision mediump float;

uniform sampler2D u_TextureUnit;
uniform sampler2D u_TextureMask;

uniform vec4 u_BlendColour;
uniform float u_BlendReverse;
uniform float u_ScreenWidth;
uniform float u_ScreenHeight;

varying vec2 v_TextureCoordinates;



void main(void)
{
    vec2 loc =  vec2(gl_FragCoord.x/u_ScreenWidth,1.0-(gl_FragCoord.y/u_ScreenHeight));
    vec4 col = texture2D(u_TextureUnit,v_TextureCoordinates) * u_BlendColour;
    vec4 mask = texture2D(u_TextureMask, loc);
    col.a *= abs(u_BlendReverse- mask.a);
    gl_FragColor = col;
}