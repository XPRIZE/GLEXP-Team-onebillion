package com.maq.xprize.onecourse.glstuff;

import android.os.SystemClock;

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
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by alan on 16/03/2017.
 */

public class PixelShaderProgram extends ShaderProgram
{
    protected static final String U_TIME = "uTime";
    protected static final String U_RESOLUTION = "uResolution";

    protected static final String U_TEXTURE_MASK = "u_TextureMask";
    protected static final String U_BLEND_REVERSE = "u_BlendReverse";
    protected static final String U_SCREEN_HEIGHT = "u_ScreenHeight";
    protected static final String U_MASK_FRAME = "u_MaskFrame";

    private final int uMatrixLocation;
    private final int aPositionLocation;
    private final int aColorLocation;
    private final int uTimeLocation;
    private final int uResolutionLocation;
    private final float xres,yres;

    private int uMaskTextureLocation = 0;
    private int uBlendReverseLocation = 0;
    private int uScreenHeightLocation = 0;
    private int uMaskFrameLocation = 0;
    private int uBlendModeLocation = 0;

    Boolean maskMode = true;

    public PixelShaderProgram(int fragmentShaderResourceId,float xres,float yres,Boolean masked)
    {
        super(R.raw.vertexshader, fragmentShaderResourceId);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        uTimeLocation = glGetUniformLocation(program, U_TIME);
        uResolutionLocation = glGetUniformLocation(program, U_RESOLUTION);
        this.xres = xres;
        this.yres = yres;
        maskMode = masked;
        if (maskMode)
        {
            uMaskTextureLocation = glGetUniformLocation(program, U_TEXTURE_MASK);
            uBlendReverseLocation = glGetUniformLocation(program, U_BLEND_REVERSE);
            uScreenHeightLocation = glGetUniformLocation(program, U_SCREEN_HEIGHT);
            uMaskFrameLocation = glGetUniformLocation(program, U_MASK_FRAME);
            uBlendModeLocation = glGetUniformLocation(program, U_BLEND_MODE);
        }
    }

    public void setUniforms(float[] matrix,float secs)
    {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform1f(uTimeLocation,secs);
        glUniform2f(uResolutionLocation,xres,yres);
    }

    public void setUniforms(float[] matrix,float secs,int textureId2, float blendReverse, float height, float[] maskFrame)
    {

        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform1f(uTimeLocation,secs);
        glUniform2f(uResolutionLocation,xres,yres);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureId2);
        glUniform1i(uMaskTextureLocation, 1);

        glUniform1f(uBlendReverseLocation,blendReverse);

        glUniform1f(uScreenHeightLocation,height);

        glUniform4fv(uMaskFrameLocation,1,maskFrame,0);

    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

}
