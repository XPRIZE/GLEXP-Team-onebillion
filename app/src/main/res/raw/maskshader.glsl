precision mediump float;

uniform sampler2D u_TextureUnit;
uniform sampler2D u_TextureMask;

uniform vec4 u_BlendColour;
uniform float u_BlendReverse;
uniform float u_ScreenWidth;
uniform float u_ScreenHeight;
uniform float u_BlendMode;

uniform vec4 u_MaskFrame;

varying vec2 v_TextureCoordinates;

float is_greater(float x, float y)
{
  return max(sign(x - y), 0.0);
}

float is_lower(float x, float y)
{
  return max(sign(y - x), 0.0);
}


void main(void)
{
    vec4 screenLoc = gl_FragCoord;
    screenLoc.y = u_ScreenHeight - screenLoc.y;
    vec2 loc =  vec2((screenLoc.x-u_MaskFrame.x)/(u_MaskFrame.z-u_MaskFrame.x),(screenLoc.y-u_MaskFrame.y)/(u_MaskFrame.w-u_MaskFrame.y));

    vec4 texCol = texture2D(u_TextureUnit, v_TextureCoordinates);
    vec4 result = vec4(0.0,0.0,0.0,0.0);
    result.r = ((texCol.r * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.r;
    result.g = ((texCol.g * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.g;
    result.b = ((texCol.b * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.b;
    result.a = texCol.a  * u_BlendColour.a;

    vec4 mask = texture2D(u_TextureMask, loc);
    float maskAlpha = mask.a * is_greater(loc.x,0.0) * is_lower(loc.x,1.0) *is_greater(loc.y,0.0) * is_lower(loc.y,1.0);
    result *= abs(u_BlendReverse-maskAlpha);
    gl_FragColor = result;

}
