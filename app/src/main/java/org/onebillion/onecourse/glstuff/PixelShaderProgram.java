package org.onebillion.onecourse.glstuff;

import android.os.SystemClock;

import org.onebillion.onecourse.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by alan on 16/03/2017.
 */

public class PixelShaderProgram extends ShaderProgram
{
    protected static final String U_TIME = "uTime";
    protected static final String U_RESOLUTION = "uResolution";

    private final int uMatrixLocation;
    private final int aPositionLocation;
    private final int aColorLocation;
    private final int uTimeLocation;
    private final int uResolutionLocation;
    private final float xres,yres;

    public PixelShaderProgram(int fragmentShaderResourceId,float xres,float yres)
    {
        super(R.raw.vertexshader, fragmentShaderResourceId);
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        uTimeLocation = glGetUniformLocation(program, U_TIME);
        uResolutionLocation = glGetUniformLocation(program, U_RESOLUTION);
        this.xres = xres;
        this.yres = yres;
    }

    public void setUniforms(float[] matrix)
    {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        long tm = SystemClock.uptimeMillis();
        float secs = tm / 1000f;
        glUniform1f(uTimeLocation,secs);
        glUniform2f(uResolutionLocation,xres,yres);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

}
