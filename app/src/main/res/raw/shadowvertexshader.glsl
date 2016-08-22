uniform mat4 u_Matrix;
uniform mat4 u_Matrix2;
uniform float u_ShadowOffsetX,u_ShadowOffsetY;
attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

varying vec2 v_TextureCoordinates;

void main()                    
{                            
	v_TextureCoordinates = a_TextureCoordinates;
    vec4 pos = u_Matrix2 * a_Position;
    pos.x += u_ShadowOffsetX;
    pos.y += u_ShadowOffsetY;
    gl_Position = u_Matrix * pos;
}