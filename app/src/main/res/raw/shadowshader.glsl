precision mediump float;
uniform sampler2D u_TextureUnit;
uniform vec4 u_ShadowColour;
uniform vec4 u_BlendColour;
varying vec2 v_TextureCoordinates;

void main()
{
    vec4 txColour = texture2D(u_TextureUnit, v_TextureCoordinates);
    vec4 resultColour = u_ShadowColour;
    resultColour *= txColour.a * u_BlendColour.a;
    gl_FragColor = resultColour;
}