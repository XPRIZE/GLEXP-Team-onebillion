package com.maq.xprize.onecourse.glstuff;

import com.maq.xprize.onecourse.R;

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
    protected static final String U_MASK_FRAME = "u_MaskFrame";

    // Uniform locations
    private final int uMatrixLocation;
    private final int uMainTextureLocation;
    private final int uMaskTextureLocation;
    private final int uBlendColourLocation;
    private final int uBlendReverseLocation;
    private final int uScreenWidthLocation;
    private final int uScreenHeightLocation;
    private final int uMaskFrameLocation;
    private final int uBlendModeLocation;

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

        uScreenWidthLocation = glGetUniformLocation(program, U_SCREEN_WIDTH);
        uScreenHeightLocation = glGetUniformLocation(program, U_SCREEN_HEIGHT);

        uMaskFrameLocation = glGetUniformLocation(program, U_MASK_FRAME);

        uBlendModeLocation = glGetUniformLocation(program, U_BLEND_MODE);
    }

    public void setUniforms(float[] matrix, int textureId, int textureId2, float[] blendColour, float blendMode, float blendReverse, float width, float height, float[] maskFrame)
    {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        glActiveTexture(GL_TEXTURE0);

        glBindTexture(GL_TEXTURE_2D, textureId);

        glUniform1i(uMainTextureLocation, 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureId2);
        glUniform1i(uMaskTextureLocation, 1);

        glUniform4fv(uBlendColourLocation,1,blendColour,0);
        glUniform1f(uBlendReverseLocation,blendReverse);

        glUniform1f(uScreenWidthLocation,width);
        glUniform1f(uScreenHeightLocation,height);

        glUniform4fv(uMaskFrameLocation,1,maskFrame,0);

        glUniform1f(uBlendModeLocation,blendMode);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
