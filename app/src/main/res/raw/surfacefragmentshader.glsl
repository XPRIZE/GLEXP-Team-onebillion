#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES u_TextureUnit;
uniform vec4 u_BlendColour;

varying vec2 v_TextureCoordinates;

void main(){
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates) * u_BlendColour;
}