precision mediump float;

uniform sampler2D u_TextureUnit;
uniform sampler2D u_TextureMask;

uniform vec4 u_BlendColour;
uniform float u_BlendReverse;
uniform float u_ScreenWidth;
uniform float u_ScreenHeight;

uniform vec4 u_MaskFrame;

varying vec2 v_TextureCoordinates;



void main(void)
{
    vec4 screenLoc = gl_FragCoord;
    screenLoc.y = u_ScreenHeight - screenLoc.y;
    vec2 loc =  vec2((screenLoc.x-u_MaskFrame.x)/(u_MaskFrame.z-u_MaskFrame.x),(screenLoc.y-u_MaskFrame.y)/(u_MaskFrame.w-u_MaskFrame.y));

    vec4 col = texture2D(u_TextureUnit,v_TextureCoordinates) * u_BlendColour;
    float maskAlpha = 0.0;
    if(loc.x>=0.0 && loc.x <=1.0 && loc.y>=0.0 && loc.y<=1.0)
    {
        vec4 mask = texture2D(u_TextureMask, loc);
        maskAlpha = mask.a;
    }
    col *= abs(u_BlendReverse-maskAlpha);
    gl_FragColor = col;

}