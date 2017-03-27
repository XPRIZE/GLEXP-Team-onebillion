package org.onebillion.onecourse.controls;

/**
 * Created by alan on 16/03/2017.
 */

import android.opengl.GLES20;
import android.os.SystemClock;

import org.onebillion.onecourse.glstuff.ColorShaderProgram;
import org.onebillion.onecourse.glstuff.GradientRect;
import org.onebillion.onecourse.glstuff.MaskShaderProgram;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.PixelRect;
import org.onebillion.onecourse.glstuff.PixelShaderProgram;
import org.onebillion.onecourse.glstuff.ShadowShaderProgram;
import org.onebillion.onecourse.glstuff.TextureShaderProgram;
import org.onebillion.onecourse.mainui.OBViewController;
import org.onebillion.onecourse.utils.OBUtils;

public class OBShaderControl extends OBControl
{
    public PixelShaderProgram shaderProgram;
    public PixelRect pixelRect;
    long starttm = SystemClock.uptimeMillis();


    public void render (OBRenderer renderer, OBViewController vc, float[] modelViewMatrix)
    {
        if (!hidden && bounds().width() > 0 && bounds().height() > 0)
        {
            matrix3dForDraw();
            if (doubleSided)
            {
                GLES20.glDisable(GLES20.GL_CULL_FACE);
            }
            else
            {
                GLES20.glEnable(GLES20.GL_CULL_FACE);
            }
            //
            android.opengl.Matrix.multiplyMM(tempMatrix, 0, modelViewMatrix, 0, modelMatrix, 0);
            //
            shaderProgram.useProgram();
            long tm = SystemClock.uptimeMillis();
            float secs = (tm - starttm) / 1000f;
            if (dynamicMask && maskControl != null)
            {
                float[] maskFrame = new float[4];
                maskFrame[0] = maskControl.frame().left+vc.viewPortLeft;
                maskFrame[1] = maskControl.frame().top+vc.viewPortTop;
                maskFrame[2] = maskControl.frame().right+vc.viewPortLeft;
                maskFrame[3] = maskControl.frame().bottom+vc.viewPortTop;
                shaderProgram.setUniforms(tempMatrix,secs,renderer.textureObjectIds[1],maskControlReversed ? 1.0f : 0.0f,renderer.h, maskFrame);
            }
            else
                shaderProgram.setUniforms(tempMatrix,secs);
            if (pixelRect == null)
                pixelRect = new PixelRect(shaderProgram);
            pixelRect.draw(renderer, 0, 0, bounds.right - bounds.left, bounds.bottom - bounds.top);

        }
    }

}
