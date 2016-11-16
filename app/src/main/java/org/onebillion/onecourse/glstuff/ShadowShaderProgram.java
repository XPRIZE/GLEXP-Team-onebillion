package org.onebillion.onecourse.glstuff;

import org.onebillion.onecourse.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by michal on 19/08/16.
 */
public class ShadowShaderProgram extends ShaderProgram
{
    protected static final String U_SHADOW_OFFSET_X = "u_ShadowOffsetX";
    protected static final String U_SHADOW_OFFSET_Y = "u_ShadowOffsetY";
    protected static final String U_SHADOW_COLOUR = "u_ShadowColour";
    protected static final String U_MATRIX2 = "u_Matrix2";
    // Uniform locations
    private final int uMatrixLocation;
    private final int uMatrix2Location;
    private final int uShadowOffsetXLocation;
    private final int uShadowOffsetYLocation;
    private final int uTextureLocation;
    private final int uShadowColourLocation;
    private final int uBlendColourLocation;

    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public ShadowShaderProgram()
    {
        super(R.raw.shadowvertexshader,
                R.raw.shadowshader);
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uMatrix2Location = glGetUniformLocation(program, U_MATRIX2);
        uTextureLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uShadowOffsetXLocation = glGetUniformLocation(program, U_SHADOW_OFFSET_X);
        uShadowOffsetYLocation = glGetUniformLocation(program, U_SHADOW_OFFSET_Y);
        uShadowColourLocation = glGetUniformLocation(program, U_SHADOW_COLOUR);
        uBlendColourLocation = glGetUniformLocation(program, U_BLEND_COLOUR);
        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix,float[] matrix2, int textureId, float shadowOffsetX, float shadowOffsetY, float[] shadowColour, float[] blendColour)
    {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniformMatrix4fv(uMatrix2Location, 1, false, matrix2, 0);

        glActiveTexture(GL_TEXTURE0);

        glBindTexture(GL_TEXTURE_2D, textureId);

        glUniform1i(uTextureLocation, 0);

        glUniform1f(uShadowOffsetXLocation,shadowOffsetX);
        glUniform1f(uShadowOffsetYLocation,shadowOffsetY);
        glUniform4fv(uShadowColourLocation,1,shadowColour,0);
        glUniform4fv(uBlendColourLocation,1,blendColour,0);
    }

    public int getPositionAttributeLocation()
    {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation()
    {
        return aTextureCoordinatesLocation;
    }
}
