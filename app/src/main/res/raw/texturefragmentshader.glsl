precision mediump float;

uniform sampler2D u_TextureUnit;
uniform vec4 u_BlendColour;

varying vec2 v_TextureCoordinates;

void main()
{
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates) * u_BlendColour;
}