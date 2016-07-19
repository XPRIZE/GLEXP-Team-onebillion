package org.onebillion.xprz.glstuff;

import org.onebillion.xprz.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
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
 * Created by michal on 01/07/16.
 */
public class MaskShaderProgram extends ShaderProgram
{
    protected static final String U_TEXTURE_MASK = "u_TextureMask";
    protected static final String U_BLEND_REVERSE = "u_BlendReverse";
    protected static final String U_SCREEN_WIDTH = "u_ScreenWidth";
    protected static final String U_SCREEN_HEIGHT = "u_ScreenHeight";
    // Uniform locations
    private final int uMatrixLocation;
    private final int uMainTextureLocation;
    private final int uMaskTextureLocation;
    private final int uBlendColourLocation;
    private final int uBlendReverseLocation;
    private final int uScreenWidth;
    private final int uScreenHeight;
    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public MaskShaderProgram()
    {
        super(R.raw.texturevertexshader,
                R.raw.maskshader);

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uMainTextureLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uMaskTextureLocation = glGetUniformLocation(program, U_TEXTURE_MASK);
        uBlendColourLocation = glGetUniformLocation(program, U_BLEND_COLOUR);
        uBlendReverseLocation = glGetUniformLocation(program, U_BLEND_REVERSE);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        uScreenWidth = glGetUniformLocation(program, U_SCREEN_WIDTH);
        uScreenHeight = glGetUniformLocation(program, U_SCREEN_HEIGHT);
    }

    public void setUniforms(float[] matrix, int textureId, int textureId2, float[] blendColour, float blendReverse, float width, float height)
    {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uMainTextureLocation, 0);


        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureId2);
        glUniform1i(uMaskTextureLocation, 1);

        glUniform4fv(uBlendColourLocation,1,blendColour,0);
        glUniform1f(uBlendReverseLocation,blendReverse);

        glUniform1f(uScreenWidth,width);
        glUniform1f(uScreenHeight,height);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
