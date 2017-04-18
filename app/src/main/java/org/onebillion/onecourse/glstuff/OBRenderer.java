package org.onebillion.onecourse.glstuff;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

/**
 * Created by alan on 29/04/16.
 */
public class OBRenderer implements GLSurfaceView.Renderer
{
    public final int[] textureObjectIds = new int[4];
    public List<TextureUnit> textureUnits = new ArrayList<>();
    public ShaderProgram colourProgram,textureProgram, maskProgram, surfaceProgram, shadowProgram;
    public TextureRect textureRect;
    public GradientRect gradientRect;
    public float[] projectionMatrix = new float[16];
    public int w,h;
    public OBSectionController transitionScreenL,transitionScreenR;
    public float transitionFrac;

    public class TextureUnit
    {
        public int textureID;
        Bitmap lastBitmap;
        public TextureUnit(int id)
        {
            textureID = id;
        }
    }
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        colourProgram = new ColorShaderProgram();
        textureProgram = new TextureShaderProgram();
        maskProgram = new MaskShaderProgram();
        surfaceProgram = new SurfaceShaderProgram();
        shadowProgram = new ShadowShaderProgram();

        glGenTextures(4, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            Log.w("onSurfaceCreated", "Could not generate a new OpenGL texture object.");
            return;
        }

        textureRect = new TextureRect();
        gradientRect = new GradientRect();

        bindStandardTextureForId(textureObjectIds[0]);
        bindStandardTextureForId(textureObjectIds[1]);
        bindExternalTextureForId(textureObjectIds[2]);
        bindStandardTextureForId(textureObjectIds[3]);

        for (int i = 0;i < textureObjectIds.length;i++)
        {
            textureUnits.add(new TextureUnit(textureObjectIds[i]));
        }
    }

    public void bindStandardTextureForId(int id)
    {
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void bindExternalTextureForId(int id)
    {
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void resetViewport()
    {
        glViewport(0,0,w,h);
    }
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
        w = width;
        h = height;
        MainActivity.mainActivity.updateGraphicScale(w, h);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        glClear(GL_COLOR_BUFFER_BIT);
        colourProgram.useProgram();
        if (transitionScreenR != null && transitionScreenL != null)
        {
            renderTwoScreens(transitionScreenL,transitionScreenR,transitionFrac);
            return;
        }
        resetViewport();
        int uMatrixLocation = glGetUniformLocation(colourProgram.program, colourProgram.U_MATRIX);
        orthoM(projectionMatrix, 0, 0, w, h, 0, -1024f, 1024f);
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
        OBSectionController controller = MainActivity.mainViewController.topController();
        if (controller != null)
            controller.render(this);
        MainActivity.mainViewController.render(this);

    }

    public int textureObjectId(int i)
    {
        //return textureObjectIds[i];
        return textureUnits.get(i).textureID;
    }

    public void renderTwoScreens(OBSectionController screenl,OBSectionController screenr,float frac)
    {
        int fw = (int)(frac * w);
        if (fw > 0)
        {
            screenl.setViewPort(0,0,fw,h);
            glViewport(0, 0, fw, h);
            colourProgram.useProgram();
            orthoM(projectionMatrix,0, w - fw, w, h, 0, -1024f, 1024f);
            int uMatrixLocation = glGetUniformLocation(colourProgram.program, colourProgram.U_MATRIX);
            glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
            screenl.render(this);
        }
        if (fw < w)
        {
            screenr.setViewPort(fw,0,w-fw,h);
            glViewport(fw, 0, w-fw, h);
            colourProgram.useProgram();
            orthoM(projectionMatrix,0,0, w - fw, h, 0, -1024f, 1024f);
            int uMatrixLocation = glGetUniformLocation(colourProgram.program, colourProgram.U_MATRIX);
            glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
            screenr.render(this);
        }
    }
}
