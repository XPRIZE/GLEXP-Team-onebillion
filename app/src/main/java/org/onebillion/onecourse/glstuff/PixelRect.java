package org.onebillion.onecourse.glstuff;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by alan on 23/03/2017.
 */

public class PixelRect
{
    static final int BYTES_PER_FLOAT = 4;
    static int POSITION_COMPONENT_COUNT = 3;
    static int STRIDE = (POSITION_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    float vertices[] = {
            -1,1,0,0,0,
            -1,-1,0,0,1,
            1,-1,0,1,1,
            -1,1,0,0,0
    };
    VertexArray vertexArray;
    PixelShaderProgram shaderProgram;

    public PixelRect(PixelShaderProgram shaderProgram)
    {
        this.shaderProgram = shaderProgram;
    }

    public static void fillOutRectVertexData(float vertices[],float l,float t,float r,float b,int stride)
    {
        int idx = 0;
        vertices[idx] = l;
        vertices[idx+1] = t;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = l;
        vertices[idx+1] = b;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = r;
        vertices[idx+1] = t;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = r;
        vertices[idx+1] = b;
        vertices[idx+2] = 0f;
    }


    public void draw(OBRenderer renderer, float l, float t, float r, float b, Bitmap mask)
    {
        fillOutRectVertexData(vertices,l,t,r,b,POSITION_COMPONENT_COUNT);
        if (vertexArray == null)
            vertexArray = new VertexArray(vertices);
        else
            vertexArray.put(vertices);

        shaderProgram.useProgram();
        bindData(shaderProgram);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        glBindTexture(GL_TEXTURE_2D, renderer.textureObjectId(1));
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        texImage2D(GL_TEXTURE_2D,0,mask,0);

        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
    }

    public void bindData(PixelShaderProgram program)
    {
        vertexArray.setVertexAttribPointer(
                0,
                program.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
    }

}
