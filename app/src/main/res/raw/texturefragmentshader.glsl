precision mediump float;

uniform sampler2D u_TextureUnit;
uniform vec4 u_BlendColour;
uniform float u_BlendMode;

varying vec2 v_TextureCoordinates;

void main()
{
    vec4 texCol = texture2D(u_TextureUnit, v_TextureCoordinates);
    vec4 result = vec4(0.0,0.0,0.0,0.0);
    result.r = ((texCol.r * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.r;
    result.g = ((texCol.g * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.g;
    result.b = ((texCol.b * u_BlendMode) + (texCol.a * abs(1.0-u_BlendMode))) * u_BlendColour.b;
    result.a = texCol.a  * u_BlendColour.a;

    gl_FragColor = result;
}